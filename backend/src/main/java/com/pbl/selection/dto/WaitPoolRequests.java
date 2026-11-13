package com.pbl.selection.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

public final class WaitPoolRequests {

    private WaitPoolRequests() {
    }

    /** 标记约谈 */
    @Data
    public static class InterviewRequest {
        @NotNull(message = "约谈时间不能为空")
        private LocalDateTime interviewAt;
    }

    /** 人工安置 */
    @Data
    public static class SettleRequest {
        @NotNull(message = "安置老师不能为空")
        private Long teacherId;

        private String remark;
    }
}
