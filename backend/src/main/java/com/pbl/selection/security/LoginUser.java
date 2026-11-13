package com.pbl.selection.security;

/**
 * 登录用户主体（存入 SecurityContext）
 */
public record LoginUser(
        Long userId,        // sys_user.id
        String username,
        String realName,
        int role            // 1=学生 2=老师 3=管理员
) {

    public static final String ROLE_STUDENT = "ROLE_STUDENT";
    public static final String ROLE_TEACHER = "ROLE_TEACHER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    public String authority() {
        return switch (role) {
            case 1 -> ROLE_STUDENT;
            case 2 -> ROLE_TEACHER;
            case 3 -> ROLE_ADMIN;
            default -> throw new IllegalArgumentException("未知角色: " + role);
        };
    }
}
