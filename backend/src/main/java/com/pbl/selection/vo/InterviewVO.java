package com.pbl.selection.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 面试记录（带学生/老师姓名）
 */
@Data
public class InterviewVO {

    private Long id;
    private Long roundId;
    private Long studentId;
    private String studentNo;
    private String studentName;
    private String major;
    private String className;
    private Long teacherId;
    private String teacherName;

    private LocalDateTime interviewTime;
    private String location;

    /** 0=待面试 1=已安排 2=已完成 3=已取消 */
    private Integer status;

    private BigDecimal score;
    private String feedback;
}
