package com.pbl.selection.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学生终选表（从 ≥2 位选中老师中终选一位）
 */
@Data
@TableName("student_final_choice")
public class StudentFinalChoice {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long roundId;
    private Long studentId;
    private Long teacherId;

    /** 0=学生主动 1=超时自动 */
    private Integer timeout;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer deleted;
}
