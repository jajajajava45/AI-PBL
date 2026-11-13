package com.pbl.selection.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 老师表（扩展用户信息，关联实训室，含名额上限）
 */
@Data
@TableName("teacher")
public class Teacher {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long trainingRoomId;
    private String teacherNo;
    private String title;
    private String researchDirection;
    private String bio;

    /** 名额上限（约 16，略有差异） */
    private Integer quota;

    /** 1=活跃 0=不参与 */
    private Integer status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer deleted;
}
