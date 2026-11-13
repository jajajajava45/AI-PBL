package com.pbl.selection.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 新建老师（POST）/ 更新老师（PUT，id 以路径传入）
 */
@Data
public class TeacherUpsertRequest {

    // ==== POST 必填 ====
    @NotBlank(message = "登录名不能为空")
    private String username;

    private String password;          // POST 必填（默认 123456 由服务端兜底）

    @NotBlank(message = "姓名不能为空")
    private String realName;

    private String phone;
    private String email;

    // ==== 老师扩展 ====
    private Long roomId;              // POST 必填
    private String teacherNo;         // POST 必填
    private String title;
    private String researchDirection;
    private String bio;
    private Integer quota;            // 名额上限
    private Integer status;           // 1=活跃 0=不参与
}
