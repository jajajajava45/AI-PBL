package com.pbl.selection.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.pbl.selection.common.BizException;
import com.pbl.selection.common.Result;
import com.pbl.selection.dto.InterviewScheduleRequest;
import com.pbl.selection.dto.InterviewScoreRequest;
import com.pbl.selection.entity.Teacher;
import com.pbl.selection.mapper.MatchResultMapper;
import com.pbl.selection.mapper.TeacherMapper;
import com.pbl.selection.security.LoginUser;
import com.pbl.selection.service.ChoiceService;
import com.pbl.selection.service.DiversionService;
import com.pbl.selection.service.FlowService;
import com.pbl.selection.service.InterviewService;
import com.pbl.selection.service.PickService;
import com.pbl.selection.service.QuotaService;
import com.pbl.selection.vo.DiversionCandidateVO;
import com.pbl.selection.vo.InterviewVO;
import com.pbl.selection.vo.MatchVO;
import com.pbl.selection.vo.PickVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 老师端接口（US-20~US-30）
 */
@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER')")
public class TeacherController {

    private final TeacherMapper teacherMapper;
    private final com.pbl.selection.mapper.SysUserMapper sysUserMapper;
    private final com.pbl.selection.mapper.TrainingRoomMapper trainingRoomMapper;
    private final MatchResultMapper matchResultMapper;
    private final FlowService flowService;
    private final QuotaService quotaService;
    private final PickService pickService;
    private final InterviewService interviewService;
    private final DiversionService diversionService;
    private final ChoiceService choiceService;

    /** 老师档案（由 user id → teacher） */
    private Teacher me(LoginUser user) {
        Teacher t = teacherMapper.selectOne(Wrappers.<Teacher>lambdaQuery()
                .eq(Teacher::getUserId, user.userId()).last("LIMIT 1"));
        if (t == null) {
            throw BizException.notFound("老师档案不存在");
        }
        return t;
    }

    private Long currentRoundId() {
        var round = flowService.getCurrentRound();
        return round == null ? null : round.getId();
    }

    /** 我的档案与名额 */
    @GetMapping("/profile")
    public Result<Map<String, Object>> profile(@AuthenticationPrincipal LoginUser user) {
        Teacher t = me(user);
        Long roundId = currentRoundId();
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("teacherId", t.getId());
        p.put("teacherNo", t.getTeacherNo());
        com.pbl.selection.entity.SysUser tu = sysUserMapper.selectById(t.getUserId());
        p.put("realName", tu == null ? null : tu.getRealName());
        com.pbl.selection.entity.TrainingRoom room = t.getTrainingRoomId() == null
                ? null : trainingRoomMapper.selectById(t.getTrainingRoomId());
        p.put("roomName", room == null ? null : room.getName());
        p.put("title", t.getTitle());
        p.put("researchDirection", t.getResearchDirection());
        p.put("bio", t.getBio());
        p.put("roomId", t.getTrainingRoomId());
        p.put("quota", t.getQuota());
        p.put("used", quotaService.usedQuota(t.getId(), roundId));
        p.put("remaining", quotaService.remaining(t, roundId));
        return Result.ok(p);
    }

    /** 选我做志愿的学生（默认当前轮） */
    @GetMapping("/candidates")
    public Result<List<PickVO>> candidates(@AuthenticationPrincipal LoginUser user,
                                           @RequestParam(required = false) Long roundId) {
        Teacher t = me(user);
        return Result.ok(pickService.candidates(t.getId(), roundId == null ? currentRoundId() : roundId));
    }

    /** 本轮我的面试记录 */
    @GetMapping("/interviews")
    public Result<List<InterviewVO>> interviews(@AuthenticationPrincipal LoginUser user,
                                                @RequestParam(required = false) Long roundId) {
        Teacher t = me(user);
        return Result.ok(interviewService.listForTeacher(t.getId(), roundId == null ? currentRoundId() : roundId));
    }

    /** 安排面试 */
    @PostMapping("/interviews")
    public Result<Void> schedule(@AuthenticationPrincipal LoginUser user,
                                 @Valid @RequestBody InterviewScheduleRequest req) {
        Teacher t = me(user);
        interviewService.schedule(t.getId(), req);
        return Result.ok();
    }

    /** 录入面试成绩/评价 */
    @PutMapping("/interviews/{id}")
    public Result<Void> score(@AuthenticationPrincipal LoginUser user,
                              @PathVariable Long id,
                              @RequestBody InterviewScoreRequest req) {
        Teacher t = me(user);
        interviewService.recordScore(t.getId(), id, req);
        return Result.ok();
    }

    /** 我本轮已选学生（含调剂来源） */
    @GetMapping("/picks")
    public Result<List<PickVO>> picks(@AuthenticationPrincipal LoginUser user,
                                      @RequestParam(required = false) Long roundId) {
        Teacher t = me(user);
        return Result.ok(pickService.myPicks(t.getId(), roundId == null ? currentRoundId() : roundId));
    }

    /** 阶段③ 选择学生（暂选） */
    @PostMapping("/picks")
    public Result<Void> pick(@AuthenticationPrincipal LoginUser user,
                             @RequestBody Map<String, Long> body) {
        Teacher t = me(user);
        Long studentId = body.get("studentId");
        if (studentId == null) {
            throw BizException.conflict("studentId 不能为空");
        }
        pickService.pick(t.getId(), studentId);
        return Result.ok();
    }

    /** 撤销暂选 */
    @DeleteMapping("/picks/{studentId}")
    public Result<Void> unpick(@AuthenticationPrincipal LoginUser user,
                               @PathVariable Long studentId) {
        Teacher t = me(user);
        pickService.unpick(t.getId(), studentId);
        return Result.ok();
    }

    /** 调剂候选学生（DIVERSION 阶段，调剂范围内未录取且服从调剂） */
    @GetMapping("/diversion/candidates")
    public Result<List<DiversionCandidateVO>> diversionCandidates(@AuthenticationPrincipal LoginUser user) {
        Teacher t = me(user);
        return Result.ok(diversionService.candidatesForTeacher(t.getId()));
    }

    /** 调剂选择学生 → 立即录取 */
    @PostMapping("/diversion/picks")
    public Result<Void> diversionPick(@AuthenticationPrincipal LoginUser user,
                                      @RequestBody Map<String, Long> body) {
        Teacher t = me(user);
        Long studentId = body.get("studentId");
        if (studentId == null) {
            throw BizException.conflict("studentId 不能为空");
        }
        diversionService.teacherPick(t.getId(), studentId);
        return Result.ok();
    }

    /** 我的最终学生名单（含调剂来的学生） */
    @GetMapping("/students")
    public Result<List<MatchVO>> students(@AuthenticationPrincipal LoginUser user) {
        Teacher t = me(user);
        return Result.ok(matchResultMapper.selectMatchVOs(null, t.getId(), null,
                com.pbl.selection.common.Enums.MatchStatus.MATCHED.code));
    }
}
