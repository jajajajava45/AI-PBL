package com.pbl.selection.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    /** 本轮志愿提交时间（排序用） */
    private LocalDateTime submitAt;

    /** 调剂范围文本（实训室内 / 校内）— 管理端分流总览用 */
    private String scopeText;

    /** 是否已被安置：1=是 0=否 — 管理端分流总览用 */
    private Integer placed;

    /** 当前状态文本（未被老师选走 / 已安置） */
    private String statusText;
}
