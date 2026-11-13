package com.pbl.selection.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.pbl.selection.common.BizException;
import com.pbl.selection.common.Enums;
import com.pbl.selection.entity.StudentFinalChoice;
import com.pbl.selection.entity.Teacher;
import com.pbl.selection.entity.TeacherPick;
import com.pbl.selection.mapper.StudentFinalChoiceMapper;
import com.pbl.selection.mapper.TeacherMapper;
import com.pbl.selection.mapper.TeacherPickMapper;
import com.pbl.selection.vo.InterviewVO;
import com.pbl.selection.vo.StudentStatusVO;
import com.pbl.selection.vo.TeacherCardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 学生终选服务：≥2 位老师选中 → 学生终选 1 位；=1 直接匹配；=0 未录取
 */
@Service
@RequiredArgsConstructor
public class FinalChoiceService {

    private final FlowService flowService;
    private final TeacherPickMapper teacherPickMapper;
    private final StudentFinalChoiceMapper finalChoiceMapper;
    private final TeacherMapper teacherMapper;
    private final MatchService matchService;
    private final QuotaService quotaService;
    private final InterviewService interviewService;
    private final ChoiceService choiceService;

    /** 选中我的老师 + 是否需要终选 */
    public Map<String, Object> pickers(Long studentId, Long roundId) {
        List<TeacherPick> picks = activePicks(studentId, roundId);
        Map<Long, TeacherCardVO> cardMap = teacherCardMap(roundId);

        List<StudentStatusVO.PickerBrief> briefs = picks.stream().map(p -> {
            StudentStatusVO.PickerBrief b = new StudentStatusVO.PickerBrief();
            b.setTeacherId(p.getTeacherId());
            TeacherCardVO card = cardMap.get(p.getTeacherId());
            b.setTeacherName(card == null ? null : card.getRealName());
            b.setRoomName(card == null ? null : card.getRoomName());
            return b;
        }).toList();

        StudentFinalChoice fc = getFinalChoice(studentId, roundId);
        boolean needFinal = picks.size() >= 2 && fc == null && !matchService.isMatched(studentId);

        StudentStatusVO.FinalChoiceBrief fcb = null;
        if (fc != null) {
            fcb = new StudentStatusVO.FinalChoiceBrief();
            fcb.setTeacherId(fc.getTeacherId());
            fcb.setTeacherName(cardMap.containsKey(fc.getTeacherId())
                    ? cardMap.get(fc.getTeacherId()).getRealName() : null);
            fcb.setTimeout(fc.getTimeout());
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("pickers", briefs);
        data.put("finalChoice", fcb);
        data.put("needFinal", needFinal);
        return data;
    }

    /**
     * 提交终选（≥2 位选中时）→ 立即写入匹配结果
     */
    @Transactional
    public void submit(Long studentId, Long teacherId) {
        var round = flowService.requireCurrentStage(Enums.StageName.STUDENT_FINAL);
        if (matchService.isMatched(studentId)) {
            throw BizException.conflict("您已匹配，无需终选");
        }
        StudentFinalChoice existing = getFinalChoice(studentId, round.getId());
        if (existing != null) {
            throw BizException.conflict("您已提交终选，不能修改");
        }
        List<TeacherPick> picks = activePicks(studentId, round.getId());
        if (picks.size() < 2) {
            throw BizException.conflict("仅 1 位老师选中您时无需终选（系统直接匹配）");
        }
        if (picks.stream().noneMatch(p -> p.getTeacherId().equals(teacherId))) {
            throw BizException.conflict("只能从选中您的老师中终选");
        }
        Teacher teacher = teacherMapper.selectById(teacherId);
        if (teacher == null || teacher.getDeleted() == 1) {
            throw BizException.notFound("老师不存在");
        }
        quotaService.checkRemainingOrThrow(teacher, round.getId());

        StudentFinalChoice fc = new StudentFinalChoice();
        fc.setRoundId(round.getId());
        fc.setStudentId(studentId);
        fc.setTeacherId(teacherId);
        fc.setTimeout(0);
        fc.setDeleted(0);
        finalChoiceMapper.insert(fc);

        matchService.createMatch(studentId, teacherId, round.getId(), Enums.MatchSource.NORMAL.code);
        releaseOtherPicks(studentId, round.getId(), teacherId);
    }

    /**
     * 终选自动处理（幂等，可在阶段开启时与关闭时各调用一次）：
     *  - 恰好 1 位老师选中 → 直接匹配
     *  - ≥2 位选中且未终选：closing=true 时视为超时，按 面试平均分 desc → 剩余名额 desc 自动分配（timeout=1）
     *  - 老师名额已满时该生保持未录取 → 进入分流/下一轮
     */
    @Transactional
    public AutoFinalResult resolveFinal(Long roundId, boolean closing) {
        int autoMatched = 0;
        int autoTimeout = 0;

        List<Long> studentIds = choiceService.participantStudentIds(roundId);
        // 确定性排序：面试平均分降序（无分靠后）→ 学生 id 升序
        record Sort(Long studentId, BigDecimal avgScore) {
        }
        List<Sort> sorted = studentIds.stream()
                .map(sid -> new Sort(sid, avgScore(sid, roundId)))
                .sorted(Comparator.comparing((Sort s) -> s.avgScore() == null
                                        ? BigDecimal.valueOf(-1) : s.avgScore(),
                                Comparator.reverseOrder())
                        .thenComparing(Sort::studentId))
                .toList();

        for (Sort s : sorted) {
            Long studentId = s.studentId();
            if (matchService.isMatched(studentId)) {
                continue;
            }
            List<TeacherPick> picks = activePicks(studentId, roundId);
            if (picks.isEmpty()) {
                continue; // 未被任何老师选中 → 分流候选
            }
            if (picks.size() == 1) {
                if (tryMatch(studentId, picks.get(0).getTeacherId(), roundId)) {
                    autoMatched++;
                }
                continue;
            }
            // ≥2 位选中
            StudentFinalChoice fc = getFinalChoice(studentId, roundId);
            if (fc != null) {
                // 已提交终选但尚未落地（名额曾满）→ 重试
                if (tryMatch(studentId, fc.getTeacherId(), roundId)) {
                    autoMatched++;
                }
                continue;
            }
            if (!closing) {
                continue; // 阶段进行中，等待学生自主终选
            }
            // 超时自动分配
            List<InterviewVO> interviews = interviewService.listForStudent(studentId, roundId);
            Map<Long, BigDecimal> scoreByTeacher = interviews.stream()
                    .filter(i -> i.getScore() != null)
                    .collect(Collectors.toMap(InterviewVO::getTeacherId, InterviewVO::getScore, (a, b) -> a));

            List<Long> pickTeacherIds = picks.stream()
                    .sorted(Comparator.comparing(TeacherPick::getCreatedAt))
                    .map(TeacherPick::getTeacherId).toList();
            Map<Long, Teacher> teacherMap = teacherMapper.selectBatchIds(pickTeacherIds).stream()
                    .collect(Collectors.toMap(Teacher::getId, Function.identity()));

            Teacher chosen = pickTeacherIds.stream()
                    .map(teacherMap::get)
                    .filter(Objects::nonNull)
                    .filter(t -> quotaService.remaining(t, roundId) > 0)
                    .max(Comparator
                            .comparing((Teacher t) -> scoreByTeacher.getOrDefault(t.getId(), BigDecimal.ZERO))
                            .thenComparing(t -> quotaService.remaining(t, roundId)))
                    .orElse(null);
            if (chosen != null) {
                StudentFinalChoice auto = new StudentFinalChoice();
                auto.setRoundId(roundId);
                auto.setStudentId(studentId);
                auto.setTeacherId(chosen.getId());
                auto.setTimeout(1);
                auto.setDeleted(0);
                finalChoiceMapper.insert(auto);
                matchService.createMatch(studentId, chosen.getId(), roundId, Enums.MatchSource.NORMAL.code);
                releaseOtherPicks(studentId, roundId, chosen.getId());
                autoMatched++;
                autoTimeout++;
            }
        }
        return new AutoFinalResult(autoMatched, autoTimeout);
    }

    public record AutoFinalResult(int autoMatched, int autoTimeout) {
    }

    private boolean tryMatch(Long studentId, Long teacherId, Long roundId) {
        Teacher t = teacherMapper.selectById(teacherId);
        if (t == null || quotaService.remaining(t, roundId) <= 0) {
            return false;
        }
        matchService.createMatch(studentId, teacherId, roundId, Enums.MatchSource.NORMAL.code);
        releaseOtherPicks(studentId, roundId, teacherId);
        return true;
    }

    /** 软删该生本轮除 keepTeacherId 外的暂选记录 */
    private void releaseOtherPicks(Long studentId, Long roundId, Long keepTeacherId) {
        teacherPickMapper.delete(Wrappers.<TeacherPick>lambdaQuery()
                .eq(TeacherPick::getRoundId, roundId)
                .eq(TeacherPick::getStudentId, studentId)
                .eq(TeacherPick::getPickSource, Enums.PickSource.CHOICE.code)
                .ne(TeacherPick::getTeacherId, keepTeacherId));
    }

    private List<TeacherPick> activePicks(Long studentId, Long roundId) {
        return teacherPickMapper.selectList(Wrappers.<TeacherPick>lambdaQuery()
                .eq(TeacherPick::getRoundId, roundId)
                .eq(TeacherPick::getStudentId, studentId)
                .eq(TeacherPick::getPickSource, Enums.PickSource.CHOICE.code));
    }

    private StudentFinalChoice getFinalChoice(Long studentId, Long roundId) {
        return finalChoiceMapper.selectOne(Wrappers.<StudentFinalChoice>lambdaQuery()
                .eq(StudentFinalChoice::getRoundId, roundId)
                .eq(StudentFinalChoice::getStudentId, studentId).last("LIMIT 1"));
    }

    private BigDecimal avgScore(Long studentId, Long roundId) {
        List<BigDecimal> scores = interviewService.listForStudent(studentId, roundId).stream()
                .map(InterviewVO::getScore).filter(Objects::nonNull).toList();
        if (scores.isEmpty()) {
            return null;
        }
        return scores.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(scores.size()), 2, RoundingMode.HALF_UP);
    }

    private Map<Long, TeacherCardVO> teacherCardMap(Long roundId) {
        return teacherMapper.selectTeacherCards(null, roundId, false).stream()
                .collect(Collectors.toMap(TeacherCardVO::getId, Function.identity()));
    }
}
