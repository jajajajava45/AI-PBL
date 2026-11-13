package com.pbl.selection.controller;

import com.pbl.selection.common.Result;
import com.pbl.selection.dto.WaitPoolRequests;
import com.pbl.selection.security.LoginUser;
import com.pbl.selection.service.WaitPoolService;
import com.pbl.selection.vo.WaitPoolVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理端：待选池（US-38/39）
 */
@RestController
@RequestMapping("/api/admin/wait-pool")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminWaitPoolController {

    private final WaitPoolService waitPoolService;

    /** 待选池列表（status: 0=待约谈 1=已约谈 2=已安置） */
    @GetMapping
    public Result<List<WaitPoolVO>> list(@RequestParam(required = false) Integer status) {
        return Result.ok(waitPoolService.list(status));
    }

    /** 标记已约谈 */
    @PutMapping("/{studentId}/interview")
    public Result<Void> interview(@PathVariable Long studentId,
                                  @Valid @RequestBody WaitPoolRequests.InterviewRequest req) {
        waitPoolService.markInterview(studentId, req.getInterviewAt());
        return Result.ok();
    }

    /** 人工安置 → match_source=4 */
    @PostMapping("/{studentId}/settle")
    public Result<Void> settle(@AuthenticationPrincipal LoginUser admin,
                               @PathVariable Long studentId,
                               @Valid @RequestBody WaitPoolRequests.SettleRequest req) {
        waitPoolService.settle(studentId, req.getTeacherId(), req.getRemark(), admin.userId());
        return Result.ok();
    }
}
