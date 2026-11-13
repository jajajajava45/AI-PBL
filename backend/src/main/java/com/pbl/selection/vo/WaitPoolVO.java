package com.pbl.selection.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 待选池记录（带学生信息）
 */
@Data
public class WaitPoolVO {

    private Long id;
    private Long studentId;
    private String studentNo;
    private String studentName;
    private String major;
    private String className;
    private String phone;
    private String email;

    private Long enterRound;
    private LocalDateTime enterAt;
    private LocalDateTime interviewAt;

    /** 0=待约谈 1=已约谈 2=已安置 */
    private Integer status;

    private Long settleTeacherId;
    private String settleTeacherName;
    private String settleRemark;
    private LocalDateTime settleAt;
}
