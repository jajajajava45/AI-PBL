package com.pbl.selection.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 最终匹配结果（带学生/老师/实训室/轮次信息）
 */
@Data
public class MatchVO {

    private Long id;
    private Long studentId;
    private String studentNo;
    private String studentName;
    private String major;
    private String className;

    private Long teacherId;
    private String teacherName;
    private String roomName;

    private Long finalRoundId;
    private Integer roundNo;
    private String roundName;

    /** 1=正常匹配 2=调剂老师选择 3=系统统筹 4=人工安置 */
    private Integer matchSource;

    /** 1=已匹配 0=已取消 */
    private Integer matchStatus;

    private LocalDateTime createdAt;
}
