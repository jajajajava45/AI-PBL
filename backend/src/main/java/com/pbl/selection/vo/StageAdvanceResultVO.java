package com.pbl.selection.vo;

import lombok.Data;

/**
 * 阶段推进结果（含自动处理统计）
 */
@Data
public class StageAdvanceResultVO {

    private String fromStage;
    private String toStage;

    /** 终选收尾：自动匹配数（含被 1 位选中直接匹配 + 超时自动分配） */
    private int autoMatched;
    /** 终选超时自动分配数 */
    private int autoTimeout;
    /** 分流收尾：进入待选池人数 */
    private int toWaitPool;
    /** 分流收尾：流转下一轮人数 */
    private int toNextRound;
}
