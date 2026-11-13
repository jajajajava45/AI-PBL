package com.pbl.selection.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 老师选学生记录（带学生信息）
 */
@Data
public class PickVO {

    private Long id;
    private Long roundId;
    private Long teacherId;
    private Long studentId;
    private String studentNo;
    private String studentName;
    private String major;
    private String className;

    /** 1=学生志愿填报 2=调剂范围内选择 */
    private Integer pickSource;

    /** 本轮面试平均分（可为空） */
    private BigDecimal avgScore;

    private LocalDateTime createdAt;
}
