package com.pbl.selection.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InterviewScheduleRequest {

    @NotNull(message = "学生不能为空")
    private Long studentId;

    @NotNull(message = "面试时间不能为空")
    private LocalDateTime interviewTime;

    private String location;
}
