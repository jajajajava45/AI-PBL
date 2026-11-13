package com.pbl.selection.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.pbl.selection.common.BizException;
import com.pbl.selection.common.Result;
import com.pbl.selection.dto.ChoiceSubmitRequest;
import com.pbl.selection.dto.FinalChoiceRequest;
import com.pbl.selection.entity.SelectionRound;
import com.pbl.selection.entity.Student;
import com.pbl.selection.mapper.StudentMapper;
import com.pbl.selection.security.LoginUser;
import com.pbl.selection.service.ChoiceService;
import com.pbl.selection.service.FinalChoiceService;
import com.pbl.selection.service.FlowService;
import com.pbl.selection.service.InterviewService;
import com.pbl.selection.service.PortalService;
import com.pbl.selection.vo.ChoiceVO;
import com.pbl.selection.vo.InterviewVO;
import com.pbl.selection.vo.StudentStatusVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 学生端接口（US-01~US-19）
 */
@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentController {

    private final StudentMapper studentMapper;
    private final PortalService portalService;
    private final ChoiceService choiceService;
    private final InterviewService interviewService;
    private final FinalChoiceService finalChoiceService;
    private final FlowService flowService;

    /** 学生档案（由 user id → student） */
    private Student me(LoginUser user) {
        Student s = studentMapper.selectOne(Wrappers.<Student>lambdaQuery()
                .eq(Student::getUserId, user.userId()).last("LIMIT 1"));
        if (s == null) {
            throw BizException.notFound("学生档案不存在");
        }
        return s;
    }

    private Long currentRoundId() {
        SelectionRound round = flowService.getCurrentRound();
        return round == null ? null : round.getId();
    }

    /** 综合状态（首页聚合） */
    @GetMapping("/status")
    public Result<StudentStatusVO> status(@AuthenticationPrincipal LoginUser user) {
        return Result.ok(portalService.status(user.userId()));
    }

    /** 本轮我的志愿 */
    @GetMapping("/choices")
    public Result<List<ChoiceVO>> choices(@AuthenticationPrincipal LoginUser user) {
        Student s = me(user);
        return Result.ok(choiceService.myChoices(s.getId(), currentRoundId()));
    }

    /** 提交/修改志愿（全量覆盖，含调剂意愿）— 仅 CHOICE_OPEN 阶段 */
    @PostMapping("/choices")
    public Result<Void> submitChoices(@AuthenticationPrincipal LoginUser user,
                                      @Valid @RequestBody ChoiceSubmitRequest req) {
        Student s = me(user);
        choiceService.submit(s.getId(), req);
        return Result.ok();
    }

    /** 本轮我的面试 */
    @GetMapping("/interviews")
    public Result<List<InterviewVO>> interviews(@AuthenticationPrincipal LoginUser user) {
        Student s = me(user);
        return Result.ok(interviewService.listForStudent(s.getId(), currentRoundId()));
    }

    /** 选中我的老师（终选依据） */
    @GetMapping("/pickers")
    public Result<Map<String, Object>> pickers(@AuthenticationPrincipal LoginUser user) {
        Student s = me(user);
        return Result.ok(finalChoiceService.pickers(s.getId(), currentRoundId()));
    }

    /** 学生终选 — 仅 STUDENT_FINAL 阶段 */
    @PostMapping("/final-choice")
    public Result<Void> finalChoice(@AuthenticationPrincipal LoginUser user,
                                    @Valid @RequestBody FinalChoiceRequest req) {
        Student s = me(user);
        finalChoiceService.submit(s.getId(), req.getTeacherId());
        return Result.ok();
    }

    /** 我的最终结果（含历史分流记录） */
    @GetMapping("/result")
    public Result<Map<String, Object>> result(@AuthenticationPrincipal LoginUser user) {
        return Result.ok(portalService.myResult(user.userId()));
    }
}
