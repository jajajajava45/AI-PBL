package com.pbl.selection.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 流程总览（轮次 + 阶段）
 */
public final class FlowVO {

    private FlowVO() {
    }

    @Data
    public static class Stage {
        private Long id;
        private String stageName;
        private String stageText;
        private Integer stageOrder;
        private Integer stageStatus;
        private String stageStatusText;
        private LocalDateTime startAt;
        private LocalDateTime endAt;
        private String remark;
    }

    @Data
    public static class Round {
        private Long id;
        private Integer roundNo;
        private Integer roundType;
        private String name;
        private Integer roundStatus;
        private String roundStatusText;
        private List<Stage> stages;
    }

    @Data
    public static class Overview {
        private Round currentRound;
        private Stage currentStage;
        private List<Round> rounds;
    }
}
