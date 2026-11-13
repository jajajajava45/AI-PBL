package com.pbl.selection.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 分流记录表（每轮后调剂分流的完整过程）
 */
@Data
@TableName("diversion_record")
public class DiversionRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 从哪一轮后进入分流 */
    private Long roundId;
    private Long studentId;

    /** 1=TEACHER_SELECT 2=SYSTEM_INTERNAL 3=SYSTEM_CROSS */
    private Integer step;

    /** 此步骤中若被某位老师选中 / 系统分配的老师 */
    private Long teacherId;

    /** 1=实训室内 2=校内 */
    private Integer scopeType;

    /** 1=成功安置 2=未选中（流转下一步） 3=彻底失败 */
    private Integer result;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer deleted;
}
