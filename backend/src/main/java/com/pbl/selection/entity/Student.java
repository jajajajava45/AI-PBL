package com.pbl.selection.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学生表（扩展用户信息）
 */
@Data
@TableName("student")
public class Student {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String studentNo;
    private String major;
    private String grade;
    private String className;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer deleted;
}
