package com.pbl.selection.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FinalChoiceRequest {

    @NotNull(message = "请选择老师")
    private Long teacherId;
}
