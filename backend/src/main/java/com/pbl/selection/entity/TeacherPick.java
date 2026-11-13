package com.pbl.selection.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 老师选学生表（老师在该轮选中的学生）
 */
@Data
@TableName("teacher_pick")
public class TeacherPick {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long roundId;
    private Long teacherId;
    private Long studentId;

    /** 1=学生主动填报该老师 2=调剂范围内选的学生 */
    private Integer pickSource;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer deleted;
}
