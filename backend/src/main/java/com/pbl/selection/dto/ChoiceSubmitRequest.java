package com.pbl.selection.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 提交/修改志愿（全量覆盖），含调剂意愿
 */
@Data
public class ChoiceSubmitRequest {

    /** 志愿老师 id，1~3 个（平行志愿不分先后） */
    @NotEmpty(message = "请至少选择 1 名志愿老师")
    private List<Long> teacherIds;

    /** 服从实训室内调剂（可多选/单选/不选） */
    private Boolean allowInternal = false;

    /** 服从校内调剂 */
    private Boolean allowCrossRoom = false;
}
