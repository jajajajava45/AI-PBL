package com.pbl.selection.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学生志愿表（每轮最多 3 名老师，跨实训室，平行志愿）
 */
@Data
@TableName("student_choice")
public class StudentChoice {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long roundId;
    private Long studentId;
    private Long teacherId;

    /** 志愿顺序 1~3（仅展示用，业务上平行志愿） */
    private Integer choiceRank;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer deleted;
}
