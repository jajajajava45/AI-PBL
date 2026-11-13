package com.pbl.selection.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.pbl.selection.common.BizException;
import com.pbl.selection.common.Enums;
import com.pbl.selection.entity.DiversionRecord;
import com.pbl.selection.entity.SelectionRound;
import com.pbl.selection.entity.TeacherPick;
import com.pbl.selection.entity.WaitPool;
import com.pbl.selection.mapper.DiversionRecordMapper;
import com.pbl.selection.mapper.SelectionRoundMapper;
import com.pbl.selection.mapper.StudentChoiceMapper;
import com.pbl.selection.mapper.TeacherMapper;
import com.pbl.selection.mapper.TeacherPickMapper;
import com.pbl.selection.mapper.WaitPoolMapper;
import com.pbl.selection.vo.DiversionCandidateVO;
import com.pbl.selection.vo.DiversionRecordVO;
import com.pbl.selection.vo.SystemPlaceResultVO;
import com.pbl.selection.vo.TeacherCardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 调剂分流服务（每轮录取后）：
 *  第 1 步：调剂范围内老师主动选择（老师操作，选中即录取）
 *  第 2 步：系统统筹（管理员触发）：实训室内 → 全校两级优先级
 *  第 3 步：收尾（轮次结束）：未安置学生流转下一轮 / 最后一轮进待选池
 */
@Service
@RequiredArgsConstructor
public class DiversionService {

    private final FlowService flowService;
    private final SelectionRoundMapper roundMapper;
    private final TeacherMapper teacherMapper;
    private final StudentChoiceMapper studentChoiceMapper;
    private final TeacherPickMapper teacherPickMapper;
    private final DiversionRecordMapper diversionRecordMapper;
    private final WaitPoolMapper waitPoolMapper;
    private final MatchService matchService;
    private final QuotaService quotaService;
    private final PickService pickService;
    private final ChoiceService choiceService;

    /** 老师视角：调剂范围内可选择的候选学生 */
    public List<DiversionCandidateVO> candidatesForTeacher(Long teacherId) {
        SelectionRound round = flowService.requireCurrentStage(Enums.StageName.DIVERSION);
        var teacher = pickService.requireTeacher(teacherId);
        return studentChoiceMapper.selectDiversionCandidates(round.getId(), teacher.getTrainingRoomId());
    }

    /**
     * 老师在调剂范围内选择学生 → 立即录取（match_source=2）
     */
    @Transactional
    public void teacherPick(Long teacherId, Long studentId) {
        SelectionRound round = flowService.requireCurrentStage(Enums.StageName.DIVERSION);
        var teacher = pickService.requireTeacher(teacherId);
        pickService.requirePickableStudent(studentId);

        List<DiversionCandidateVO> candidates =
                studentChoiceMapper.selectDiversionCandidates(round.getId(), teacher.getTrainingRoomId());
        DiversionCandidateVO candidate = candidates.stream()
                .filter(c -> c.getStudentId().equals(studentId))
                .findFirst()
                .orElseThrow(() -> BizException.conflict("该学生不在您的调剂范围内或不满足调剂条件"));

        quotaService.checkRemainingOrThrow(teacher, round.getId());

        // 调剂范围判定：实训室内（志愿老师所在实训室）优先标记
        List<Long> wishRooms = studentChoiceMapper.selectWishRoomIds(studentId, round.getId());
        int scopeType = candidate.getAllowInternal() != null && candidate.getAllowInternal() == 1
                && wishRooms.contains(teacher.getTrainingRoomId())
                ? Enums.ScopeType.INTERNAL.code : Enums.ScopeType.CROSS.code;

        TeacherPick pick = new TeacherPick();
        pick.setRoundId(round.getId());
        pick.setTeacherId(teacherId);
        pick.setStudentId(studentId);
        pick.setPickSource(Enums.PickSource.DIVERSION.code);
        pick.setDeleted(0);
        teacherPickMapper.insert(pick);

        DiversionRecord record = new DiversionRecord();
        record.setRoundId(round.getId());
        record.setStudentId(studentId);
        record.setStep(Enums.DiversionStep.TEACHER_SELECT.code);
        record.setTeacherId(teacherId);
        record.setScopeType(scopeType);
        record.setResult(Enums.DiversionResult.PLACED.code);
        record.setDeleted(0);
        diversionRecordMapper.insert(record);

        matchService.createMatch(studentId, teacherId, round.getId(), Enums.MatchSource.TEACHER_DIVERT.code);
    }

    /** 管理端分流候选总览：补充分流范围与当前状态文本 */
    public List<DiversionCandidateVO> adminCandidates(Long roundId) {
        List<DiversionCandidateVO> list = studentChoiceMapper.selectAdminDiversionCandidates(roundId);
        for (DiversionCandidateVO c : list) {
            boolean internal = c.getAllowInternal() != null && c.getAllowInternal() == 1;
            boolean cross = c.getAllowCrossRoom() != null && c.getAllowCrossRoom() == 1;
            c.setScopeText(cross ? (internal ? "实训室内+校内" : "校内") : (internal ? "实训室内" : "未选择"));
            c.setStatusText(c.getPlaced() != null && c.getPlaced() == 1 ? "已安置" : "未被老师选走");
        }
        return list;
    }

    /**
     * 系统统筹（管理员触发，US-37）：
     *  候选按 面试平均分 desc → 志愿提交时间 asc；
     *  第一优先级：志愿老师所属实训室内有名额老师（服从校内者也参与）；
     *  第二优先级：全校有名额老师（仅服从校内学生）；
     *  仅服从实训室内、第一级失败 → 不进第二级。
     */
    @Transactional
    public SystemPlaceResultVO systemPlace(Long roundId) {
        SelectionRound round = flowService.requireCurrentStage(Enums.StageName.DIVERSION);
        if (!round.getId().equals(roundId)) {
            throw BizException.conflict("轮次不匹配：当前分流轮次为第 " + round.getRoundNo() + " 轮");
        }

        List<DiversionCandidateVO> candidates = studentChoiceMapper.selectSystemCandidates(round.getId());
        candidates.sort(Comparator
                .comparing((DiversionCandidateVO c) -> c.getAvgScore() == null
                        ? java.math.BigDecimal.valueOf(-1) : c.getAvgScore(), Comparator.reverseOrder())
                .thenComparing(c -> c.getSubmitAt() == null
                        ? LocalDateTime.MAX : c.getSubmitAt()));

        // 老师剩余名额基线（TeacherCardVO.remaining 已含"暂选未落地"占用）
        Map<Long, TeacherCardVO> cards = teacherMapper.selectTeacherCards(null, round.getId(), true).stream()
                .collect(Collectors.toMap(TeacherCardVO::getId, Function.identity()));
        Map<Long, Integer> remaining = cards.values().stream()
                .collect(Collectors.toMap(TeacherCardVO::getId, c -> c.getRemaining() == null ? 0 : c.getRemaining()));

        int internalPlaced = 0;
        int crossPlaced = 0;
        List<DiversionCandidateVO> unplaced = new ArrayList<>();

        for (DiversionCandidateVO c : candidates) {
            boolean placed = false;

            // 第一优先级：实训室内
            List<Long> wishRooms = studentChoiceMapper.selectWishRoomIds(c.getStudentId(), round.getId());
            TeacherCardVO t1 = cards.values().stream()
                    .filter(card -> wishRooms.contains(card.getRoomId()))
                    .filter(card -> remaining.getOrDefault(card.getId(), 0) > 0)
                    .sorted(Comparator.comparingInt((TeacherCardVO card) -> -remaining.get(card.getId()))
                            .thenComparing(TeacherCardVO::getId))
                    .findFirst().orElse(null);
            if (t1 != null) {
                writeRecord(round.getId(), c.getStudentId(), Enums.DiversionStep.SYSTEM_INTERNAL,
                        t1.getId(), Enums.ScopeType.INTERNAL, Enums.DiversionResult.PLACED);
                matchService.createMatch(c.getStudentId(), t1.getId(), round.getId(), Enums.MatchSource.SYSTEM_DIVERT.code);
                remaining.merge(t1.getId(), -1, Integer::sum);
                internalPlaced++;
                placed = true;
            } else {
                writeRecord(round.getId(), c.getStudentId(), Enums.DiversionStep.SYSTEM_INTERNAL,
                        null, Enums.ScopeType.INTERNAL, Enums.DiversionResult.PASSED);
            }

            // 第二优先级：全校（仅服从校内学生）
            if (!placed && c.getAllowCrossRoom() != null && c.getAllowCrossRoom() == 1) {
                TeacherCardVO t2 = cards.values().stream()
                        .filter(card -> remaining.getOrDefault(card.getId(), 0) > 0)
                        .sorted(Comparator.comparingInt((TeacherCardVO card) -> -remaining.get(card.getId()))
                                .thenComparing(TeacherCardVO::getId))
                        .findFirst().orElse(null);
                if (t2 != null) {
                    writeRecord(round.getId(), c.getStudentId(), Enums.DiversionStep.SYSTEM_CROSS,
                            t2.getId(), Enums.ScopeType.CROSS, Enums.DiversionResult.PLACED);
                    matchService.createMatch(c.getStudentId(), t2.getId(), round.getId(), Enums.MatchSource.SYSTEM_DIVERT.code);
                    remaining.merge(t2.getId(), -1, Integer::sum);
                    crossPlaced++;
                    placed = true;
                } else {
                    writeRecord(round.getId(), c.getStudentId(), Enums.DiversionStep.SYSTEM_CROSS,
                            null, Enums.ScopeType.CROSS, Enums.DiversionResult.PASSED);
                }
            }

            if (!placed) {
                unplaced.add(c);
            }
        }

        SystemPlaceResultVO vo = new SystemPlaceResultVO();
        vo.setInternalPlaced(internalPlaced);
        vo.setCrossPlaced(crossPlaced);
        vo.setUnplaced(unplaced);
        return vo;
    }

    /**
     * 轮次收尾（离开 DIVERSION 阶段时调用）：
     *  未安置学生 → 存在下一轮则流转（无需落库）；最后一轮 → 写入待选池；轮次置为已结束
     */
    @Transactional
    public Map<String, Integer> finalizeRound(SelectionRound round) {
        List<Long> participants = studentChoiceMapper.participantStudentIdsUpToRound(round.getRoundNo());
        List<Long> unplaced = participants.stream()
                .filter(sid -> !matchService.isMatched(sid))
                .filter(sid -> waitPoolMapper.selectCount(Wrappers.<WaitPool>lambdaQuery()
                        .eq(WaitPool::getStudentId, sid)) == 0)
                .toList();

        int toNextRound = 0;
        int toWaitPool = 0;

        SelectionRound next = roundMapper.selectOne(Wrappers.<SelectionRound>lambdaQuery()
                .eq(SelectionRound::getRoundNo, round.getRoundNo() + 1)
                .last("LIMIT 1"));
        if (next != null) {
            toNextRound = unplaced.size();
        } else {
            for (Long sid : unplaced) {
                WaitPool wp = new WaitPool();
                wp.setStudentId(sid);
                wp.setEnterRound(round.getId());
                wp.setEnterAt(LocalDateTime.now());
                wp.setStatus(Enums.WaitPoolStatus.PENDING.code);
                wp.setDeleted(0);
                waitPoolMapper.insert(wp);
                toWaitPool++;
            }
        }

        round.setStatus(Enums.RoundStatus.FINISHED.code);
        roundMapper.updateById(round);
        return Map.of("toWaitPool", toWaitPool, "toNextRound", toNextRound);
    }

    /** 分流记录查询 */
    public List<DiversionRecordVO> records(Long roundId, Long studentId) {
        return diversionRecordMapper.selectRecordVOs(roundId, studentId);
    }

    private void writeRecord(Long roundId, Long studentId, Enums.DiversionStep step,
                             Long teacherId, Enums.ScopeType scope, Enums.DiversionResult result) {
        DiversionRecord record = new DiversionRecord();
        record.setRoundId(roundId);
        record.setStudentId(studentId);
        record.setStep(step.code);
        record.setTeacherId(teacherId);
        record.setScopeType(scope.code);
        record.setResult(result.code);
        record.setDeleted(0);
        diversionRecordMapper.insert(record);
    }
}
