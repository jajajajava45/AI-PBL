package com.pbl.selection.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 调剂意愿表（学生在某轮的调剂意愿，可多选/单选/不选）
 */
@Data
@TableName("adjustment_wish")
public class AdjustmentWish {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long roundId;
    private Long studentId;

    /** 服从实训室内调剂 */
    private Integer allowInternal;

    /** 服从校内调剂 */
    private Integer allowCrossRoom;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer deleted;
}
