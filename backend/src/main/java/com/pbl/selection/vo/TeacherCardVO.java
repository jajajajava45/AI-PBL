package com.pbl.selection.vo;

import lombok.Data;

/**
 * 老师卡片（含实时剩余名额）— 学生浏览 / 管理端使用
 */
@Data
public class TeacherCardVO {

    private Long id;
    private String teacherNo;
    private String realName;
    private String title;
    private String researchDirection;
    private String bio;

    private Long roomId;
    private String roomName;

    /** 名额上限 */
    private Integer quota;

    /** 剩余名额 = 上限 − 已录取数 − 本轮未落地暂选数 */
    private Integer remaining;

    /** 1=活跃 0=不参与 */
    private Integer status;
}
