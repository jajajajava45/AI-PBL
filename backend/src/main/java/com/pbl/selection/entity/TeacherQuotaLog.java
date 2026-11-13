package com.pbl.selection.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 老师名额变动日志（审计）
 */
@Data
@TableName("teacher_quota_log")
public class TeacherQuotaLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long teacherId;
    private Integer quotaBefore;
    private Integer quotaAfter;
    private String reason;
    private Long operatorId;
    private LocalDateTime createdAt;
    private Integer deleted;
}
