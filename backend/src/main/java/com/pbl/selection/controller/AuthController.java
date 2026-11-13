package com.pbl.selection.controller;

import com.pbl.selection.common.Result;
import com.pbl.selection.dto.ChangePasswordRequest;
import com.pbl.selection.dto.LoginRequest;
import com.pbl.selection.security.LoginUser;
import com.pbl.selection.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 认证接口
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** 登录（无需 token） */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest req) {
        return Result.ok(authService.login(req));
    }

    /** 当前用户信息 */
    @GetMapping("/me")
    public Result<Map<String, Object>> me(@AuthenticationPrincipal LoginUser user) {
        return Result.ok(authService.me(user.userId()));
    }

    /** 修改密码 */
    @PostMapping("/change-password")
    public Result<Void> changePassword(@AuthenticationPrincipal LoginUser user,
                                       @Valid @RequestBody ChangePasswordRequest req) {
        authService.changePassword(user.userId(), req);
        return Result.ok();
    }
}
