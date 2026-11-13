package com.pbl.selection.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 录入面试成绩/评价（均可选，status=2 表示标记已完成）
 */
@Data
public class InterviewScoreRequest {

    private BigDecimal score;

    private String feedback;

    private Integer status;
}
