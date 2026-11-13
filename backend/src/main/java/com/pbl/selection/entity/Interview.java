package com.pbl.selection.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 面试记录表（学生与志愿老师的面试）
 */
@Data
@TableName("interview")
public class Interview {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long roundId;
    private Long studentId;
    private Long teacherId;

    private LocalDateTime interviewTime;
    private String location;

    /** 0=待面试 1=已安排 2=已完成 3=已取消 */
    private Integer status;

    /** 面试成绩（可选，用于系统统筹排序） */
    private BigDecimal score;

    private String feedback;
    private String remark;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer deleted;
}
