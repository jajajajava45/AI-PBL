package com.pbl.selection.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.pbl.selection.common.BizException;
import com.pbl.selection.common.Enums;
import com.pbl.selection.entity.AdjustmentWish;
import com.pbl.selection.entity.FlowStage;
import com.pbl.selection.entity.SelectionRound;
import com.pbl.selection.entity.Student;
import com.pbl.selection.entity.SysUser;
import com.pbl.selection.entity.Teacher;
import com.pbl.selection.mapper.MatchResultMapper;
import com.pbl.selection.mapper.SelectionRoundMapper;
import com.pbl.selection.mapper.StudentMapper;
import com.pbl.selection.mapper.SysUserMapper;
import com.pbl.selection.mapper.TeacherMapper;
import com.pbl.selection.mapper.TeacherPickMapper;
import com.pbl.selection.vo.ChoiceVO;
import com.pbl.selection.vo.DiversionRecordVO;
import com.pbl.selection.vo.InterviewVO;
import com.pbl.selection.vo.MatchVO;
import com.pbl.selection.vo.StudentStatusVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 学生端聚合服务：综合状态（首页大接口）
 */
@Service
@RequiredArgsConstructor
public class PortalService {

    private final StudentMapper studentMapper;
    private final SysUserMapper userMapper;
    private final SelectionRoundMapper roundMapper;
    private final FlowService flowService;
    private final MatchResultMapper matchResultMapper;
    private final TeacherMapper teacherMapper;
    private final ChoiceService choiceService;
    private final InterviewService interviewService;
    private final TeacherPickMapper teacherPickMapper;
    private final FinalChoiceService finalChoiceService;
    private final DiversionService diversionService;
    private final WaitPoolService waitPoolService;

    public StudentStatusVO status(Long userId) {
        Student s = requireStudent(userId);
        SysUser u = userMapper.selectById(s.getUserId());

        StudentStatusVO vo = new StudentStatusVO();
        StudentStatusVO.StudentBrief sb = new StudentStatusVO.StudentBrief();
        sb.setId(s.getId());
        sb.setStudentNo(s.getStudentNo());
        sb.setRealName(u == null ? null : u.getRealName());
        sb.setMajor(s.getMajor());
        sb.setGrade(s.getGrade());
        sb.setClassName(s.getClassName());
        vo.setStudent(sb);

        MatchVO matched = matchResultMapper.selectMatchVOs(s.getId(), null, null, Enums.MatchStatus.MATCHED.code)
                .stream().findFirst().orElse(null);
        var wp = waitPoolService.getByStudent(s.getId());

        SelectionRound round = flowService.getCurrentRound();
        FlowStage stage = round == null ? null : flowService.getCurrentStage(round.getId());

        // ===== 本轮数据 =====
        List<ChoiceVO> choices = List.of();
        AdjustmentWish wish = null;
        List<InterviewVO> interviews = List.of();
        List<DiversionRecordVO> divRecords = List.of();
        Map<String, Object> pickersData = Map.of();
        boolean needFinal = false;
        if (round != null) {
            choices = choiceService.myChoices(s.getId(), round.getId());
            wish = choiceService.myWish(s.getId(), round.getId());
            interviews = interviewService.listForStudent(s.getId(), round.getId());
            divRecords = diversionService.records(round.getId(), s.getId());
            pickersData = finalChoiceService.pickers(s.getId(), round.getId());
            needFinal = Boolean.TRUE.equals(pickersData.get("needFinal"));
        }

        // ===== 状态推导 =====
        StudentStatusVO.StatusBrief status = new StudentStatusVO.StatusBrief();
        Enums.StudentStatus st = deriveStatus(round, stage, choices, wish, divRecords,
                pickersData, needFinal, matched, wp);
        status.setCode(st.code);
        status.setText(st.text);
        vo.setStatus(status);

        // ===== 轮次/阶段 =====
        if (round != null) {
            StudentStatusVO.RoundBrief rb = new StudentStatusVO.RoundBrief();
            rb.setId(round.getId());
            rb.setRoundNo(round.getRoundNo());
            rb.setRoundType(round.getRoundType());
            rb.setName(round.getName());
            vo.setRound(rb);
        }
        if (stage != null) {
            StudentStatusVO.StageBrief stb = new StudentStatusVO.StageBrief();
            stb.setStageName(stage.getStageName());
            Enums.StageName sn = Enums.StageName.of(stage.getStageName());
            stb.setStageText(sn == null ? stage.getStageName() : sn.text);
            stb.setStageStatus(stage.getStatus());
            vo.setStage(stb);
        }

        vo.setMatched(matched);
        if (wp != null) {
            StudentStatusVO.WaitPoolBrief wb = new StudentStatusVO.WaitPoolBrief();
            wb.setStatus(wp.getStatus());
            wb.setStatusText(wp.getStatus() != null && wp.getStatus() == Enums.WaitPoolStatus.SETTLED.code
                    ? "已安置"
                    : (wp.getStatus() != null && wp.getStatus() == Enums.WaitPoolStatus.INTERVIEWED.code
                    ? "已约谈" : "待约谈"));
            wb.setInterviewAt(wp.getInterviewAt());
            wb.setSettleRemark(wp.getSettleRemark());
            if (wp.getSettleTeacherId() != null) {
                Teacher t = teacherMapper.selectById(wp.getSettleTeacherId());
                if (t != null) {
                    SysUser tu = userMapper.selectById(t.getUserId());
                    wb.setSettleTeacherName(tu == null ? null : tu.getRealName());
                }
            }
            vo.setWaitPool(wb);
        }

        // ===== 当前轮详情 =====
        if (round != null) {
            StudentStatusVO.CurrentRound cur = new StudentStatusVO.CurrentRound();
            cur.setChoices(choices);
            if (wish != null) {
                StudentStatusVO.WishBrief wb2 = new StudentStatusVO.WishBrief();
                wb2.setAllowInternal(wish.getAllowInternal());
                wb2.setAllowCrossRoom(wish.getAllowCrossRoom());
                cur.setWish(wb2);
            }
            cur.setInterviews(interviews);
            cur.setPickers(castPickers(pickersData.get("pickers")));
            cur.setFinalChoice((StudentStatusVO.FinalChoiceBrief) pickersData.get("finalChoice"));
            cur.setDiversionRecords(divRecords);
            vo.setCurrentRound(cur);
        }
        return vo;
    }

    /** 我的最终结果（含全部历史分流记录，按轮次分组） */
    public Map<String, Object> myResult(Long userId) {
        Student s = requireStudent(userId);
        MatchVO matched = matchResultMapper.selectMatchVOs(s.getId(), null, null, Enums.MatchStatus.MATCHED.code)
                .stream().findFirst().orElse(null);
        List<DiversionRecordVO> allRecords = diversionService.records(null, s.getId());

        Map<Long, SelectionRound> rounds = roundMapper.selectList(
                        Wrappers.<SelectionRound>lambdaQuery().orderByAsc(SelectionRound::getRoundNo))
                .stream().collect(Collectors.toMap(SelectionRound::getId, Function.identity()));

        // 按轮次聚合（记录已按时间倒序，聚合时保持首次出现顺序）
        Map<Long, List<DiversionRecordVO>> grouped = new LinkedHashMap<>();
        for (DiversionRecordVO r : allRecords) {
            grouped.computeIfAbsent(r.getRoundId(), k -> new ArrayList<>()).add(r);
        }
        List<Map<String, Object>> history = new ArrayList<>();
        grouped.forEach((roundId, records) -> {
            SelectionRound round = rounds.get(roundId);
            Map<String, Object> g = new LinkedHashMap<>();
            g.put("roundId", roundId);
            g.put("roundNo", round == null ? null : round.getRoundNo());
            g.put("roundName", round == null ? null : round.getName());
            g.put("records", records);
            history.add(g);
        });

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("matched", matched);
        data.put("diversionHistory", history);
        return data;
    }

    // ==================== 私有辅助 ====================

    private Student requireStudent(Long userId) {
        Student s = studentMapper.selectOne(Wrappers.<Student>lambdaQuery()
                .eq(Student::getUserId, userId).last("LIMIT 1"));
        if (s == null) {
            throw BizException.notFound("学生档案不存在");
        }
        return s;
    }

    @SuppressWarnings("unchecked")
    private Enums.StudentStatus deriveStatus(SelectionRound round, FlowStage stage, List<ChoiceVO> choices,
                                             AdjustmentWish wish, List<DiversionRecordVO> divRecords,
                                             Map<String, Object> pickersData, boolean needFinal,
                                             MatchVO matched, com.pbl.selection.entity.WaitPool wp) {
        if (wp != null) {
            return wp.getStatus() != null && wp.getStatus() == Enums.WaitPoolStatus.SETTLED.code
                    ? Enums.StudentStatus.SETTLED : Enums.StudentStatus.WAIT_POOL;
        }
        if (matched != null) {
            return Enums.StudentStatus.MATCHED;
        }
        if (round == null) {
            // 已有轮次启动过（进行中/已结束）而当前无进行中的轮次 → 处于轮次间隔或全部结束，视为流转
            Long everStarted = roundMapper.selectCount(Wrappers.<SelectionRound>lambdaQuery()
                    .ne(SelectionRound::getStatus, Enums.RoundStatus.NOT_STARTED.code));
            return everStarted > 0 ? Enums.StudentStatus.TO_NEXT_ROUND : Enums.StudentStatus.NOT_CHOSEN;
        }
        if (choices.isEmpty()) {
            return Enums.StudentStatus.NOT_CHOSEN;
        }
        String stageName = stage == null ? "" : stage.getStageName();
        List<StudentStatusVO.PickerBrief> pickers = castPickers(pickersData.get("pickers"));
        return switch (stageName) {
            case "CHOICE_OPEN", "CHOICE_CLOSED" -> Enums.StudentStatus.CHOICE_SUBMITTED;
            case "INTERVIEW" -> Enums.StudentStatus.INTERVIEWING;
            case "TEACHER_PICK" -> pickers.isEmpty()
                    ? Enums.StudentStatus.WAIT_TEACHER_PICK : Enums.StudentStatus.PICKED;
            case "STUDENT_FINAL" -> pickers.isEmpty() ? Enums.StudentStatus.WAIT_TEACHER_PICK
                    : (needFinal ? Enums.StudentStatus.WAIT_FINAL : Enums.StudentStatus.PICKED);
            case "DIVERSION" -> {
                boolean obey = wish != null && ((wish.getAllowInternal() != null && wish.getAllowInternal() == 1)
                        || (wish.getAllowCrossRoom() != null && wish.getAllowCrossRoom() == 1));
                if (!obey) {
                    yield Enums.StudentStatus.TO_NEXT_ROUND;
                }
                yield divRecords.isEmpty()
                        ? Enums.StudentStatus.DIVERSION_TEACHER_SELECT : Enums.StudentStatus.DIVERSION_SYSTEM;
            }
            default -> Enums.StudentStatus.TO_NEXT_ROUND;
        };
    }

    @SuppressWarnings("unchecked")
    private List<StudentStatusVO.PickerBrief> castPickers(Object o) {
        return o == null ? List.of() : (List<StudentStatusVO.PickerBrief>) o;
    }
}
