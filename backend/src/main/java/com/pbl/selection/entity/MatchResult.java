package com.pbl.selection.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 最终匹配结果表（学生最终归属哪位老师）
 */
@Data
@TableName("match_result")
public class MatchResult {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long studentId;
    private Long teacherId;

    /** 在哪一轮确定的（含分流/统筹轮次） */
    private Long finalRoundId;

    /** 1=NORMAL 2=TEACHER_DIVERT 3=SYSTEM_DIVERT 4=MANUAL_SETTLE */
    private Integer matchSource;

    /** 1=MATCHED 0=CANCELLED */
    private Integer matchStatus;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer deleted;
}
