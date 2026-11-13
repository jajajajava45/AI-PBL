package com.pbl.selection.vo;

import lombok.Data;

/**
 * 学生志愿（带老师信息）
 */
@Data
public class ChoiceVO {

    private Long id;
    private Long roundId;
    private Long studentId;
    private Long teacherId;
    private Integer choiceRank;
    private String teacherName;
    private String title;
    private String researchDirection;
    private String roomName;
}
