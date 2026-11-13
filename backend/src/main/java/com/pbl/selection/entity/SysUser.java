package com.pbl.selection.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统用户表（学生/老师/管理员通用账号）
 */
@Data
@TableName("sys_user")
public class SysUser {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 登录名（学号/工号/管理员名） */
    private String username;

    /** BCrypt 加密密码（不序列化到前端） */
    @JsonIgnore
    private String password;

    private String realName;

    /** 1=学生 2=老师 3=管理员 */
    private Integer role;

    private String phone;
    private String email;
    private String avatar;

    /** 1=启用 0=禁用 */
    private Integer enabled;

    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** 软删除（配合 MP 全局逻辑删除） */
    private Integer deleted;
}
