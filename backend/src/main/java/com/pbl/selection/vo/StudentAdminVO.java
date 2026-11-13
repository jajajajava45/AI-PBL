package com.pbl.selection.vo;

import lombok.Data;

/**
 * 管理端学生列表项
 */
@Data
public class StudentAdminVO {

    private Long id;
    /** 关联账号 sys_user.id（启停用账号 / 重置密码使用） */
    private Long userId;
    private String username;
    private String studentNo;
    private String realName;
    private String phone;
    private String email;
    private Integer enabled;
    private String major;
    private String grade;
    private String className;
}
