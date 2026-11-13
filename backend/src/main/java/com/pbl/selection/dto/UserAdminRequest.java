package com.pbl.selection.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

public final class UserAdminRequest {

    private UserAdminRequest() {
    }

    /** 启用/禁用账号 */
    @Data
    public static class EnabledRequest {
        @NotNull(message = "enabled 不能为空")
        private Boolean enabled;
    }

    /** 重置密码 */
    @Data
    public static class ResetPasswordRequest {
        @NotBlank(message = "新密码不能为空")
        private String newPassword;
    }
}
