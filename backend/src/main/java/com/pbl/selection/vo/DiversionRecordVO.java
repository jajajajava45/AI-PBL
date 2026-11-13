package com.pbl.selection.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 分流记录（带学生/老师姓名）
 */
@Data
public class DiversionRecordVO {

    private Long id;
    private Long roundId;
    private Long studentId;
    private String studentNo;
    private String studentName;

    /** 1=调剂老师选择 2=系统统筹-实训室内 3=系统统筹-全校 */
    private Integer step;

    private Long teacherId;
    private String teacherName;

    /** 1=实训室内 2=校内 */
    private Integer scopeType;

    /** 1=成功安置 2=未安置流转 3=彻底失败 */
    private Integer result;

    private LocalDateTime createdAt;
}
