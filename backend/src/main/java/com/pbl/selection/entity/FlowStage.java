package com.pbl.selection.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 流程阶段表（每轮内的阶段控制）
 */
@Data
@TableName("flow_stage")
public class FlowStage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long roundId;

    /** CHOICE_OPEN/CHOICE_CLOSED/INTERVIEW/TEACHER_PICK/STUDENT_FINAL/DIVERSION/COMPLETED */
    private String stageName;

    private Integer stageOrder;

    private LocalDateTime startAt;
    private LocalDateTime endAt;

    /** 0=未开始 1=进行中 2=已结束 */
    private Integer status;

    private String remark;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer deleted;
}
