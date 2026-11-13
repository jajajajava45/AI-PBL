package com.pbl.selection.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 录取轮次表（第一轮 / 第二轮补录 / 第三轮补录）
 */
@Data
@TableName("selection_round")
public class SelectionRound {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 轮次编号：1=第一轮 2=第二轮补录 3=第三轮补录 */
    private Integer roundNo;

    /** 1=初选(INITIAL) 2=补录(SUPPLEMENT) */
    private Integer roundType;

    private String name;

    private LocalDateTime choiceStartAt;
    private LocalDateTime choiceEndAt;

    /** 0=未开始 1=进行中 2=已结束 */
    private Integer status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer deleted;
}
