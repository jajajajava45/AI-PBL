package com.pbl.selection.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StudentUpsertRequest {

    // ==== POST 必填 ====
    @NotBlank(message = "登录名不能为空")
    private String username;

    private String password;

    @NotBlank(message = "姓名不能为空")
    private String realName;

    private String phone;
    private String email;

    // ==== 学生扩展 ====
    private String studentNo;         // POST 必填
    private String major;
    private String grade;
    private String className;
}
