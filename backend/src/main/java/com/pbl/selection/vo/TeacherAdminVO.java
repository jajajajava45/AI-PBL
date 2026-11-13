package com.pbl.selection.vo;

import lombok.Data;

/**
 * 管理端老师列表项
 */
@Data
public class TeacherAdminVO {

    private Long id;
    /** 关联账号 sys_user.id（启停用账号 / 重置密码使用） */
    private Long userId;
    private String username;
    private String teacherNo;
    private String realName;
    private String phone;
    private String email;
    private Integer enabled;
    private String title;
    private String researchDirection;
    private String bio;
    private Integer quota;
    private Integer status;
    private Long roomId;
    private String roomName;
}
