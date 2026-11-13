package com.pbl.selection.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 待选池表（最后一轮后未安置的学生，学校约谈后人工安置）
 */
@Data
@TableName("wait_pool")
public class WaitPool {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long studentId;

    /** 从哪一轮分流后进入待选池 */
    private Long enterRound;

    private LocalDateTime enterAt;
    private LocalDateTime interviewAt;

    /** 0=PENDING 1=INTERVIEWED 2=SETTLED */
    private Integer status;

    private Long settleTeacherId;
    private Long settleBy;
    private String settleRemark;
    private LocalDateTime settleAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer deleted;
}
