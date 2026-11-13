package com.pbl.selection.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ConfigUpdateRequest {

    @NotBlank(message = "配置值不能为空")
    private String value;
}
