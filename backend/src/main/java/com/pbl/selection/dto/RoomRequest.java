package com.pbl.selection.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RoomRequest {

    @NotBlank(message = "实训室名称不能为空")
    private String name;

    private String code;
    private String description;
    private Integer status;
}
