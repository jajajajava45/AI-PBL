package com.pbl.selection.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 调剂分流候选学生
 */
@Data
public class DiversionCandidateVO {

    private Long studentId;
    private String studentNo;
    private String studentName;
    private String major;
    private String className;

    private Integer allowInternal;
    private Integer allowCrossRoom;

    /** 本轮面试平均分 */
    private BigDecimal avgScore;
}
