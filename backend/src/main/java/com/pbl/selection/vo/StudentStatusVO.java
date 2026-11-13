package com.pbl.selection.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 学生综合状态聚合（学生端首页）
 */
@Data
public class StudentStatusVO {

    private StudentBrief student;
    private StatusBrief status;
    private RoundBrief round;
    private StageBrief stage;
    private MatchVO matched;
    private WaitPoolBrief waitPool;
    private CurrentRound currentRound;

    @Data
    public static class StudentBrief {
        private Long id;
        private String studentNo;
        private String realName;
        private String major;
        private String grade;
        private String className;
    }

    @Data
    public static class StatusBrief {
        private String code;
        private String text;
    }

    @Data
    public static class RoundBrief {
        private Long id;
        private Integer roundNo;
        private Integer roundType;
        private String name;
    }

    @Data
    public static class StageBrief {
        private String stageName;
        private String stageText;
        private Integer stageStatus;
    }

    @Data
    public static class WishBrief {
        private Integer allowInternal;
        private Integer allowCrossRoom;
    }

    @Data
    public static class PickerBrief {
        private Long teacherId;
        private String teacherName;
        private String roomName;
    }

    @Data
    public static class FinalChoiceBrief {
        private Long teacherId;
        private String teacherName;
        private Integer timeout;
    }

    @Data
    public static class WaitPoolBrief {
        private Integer status;
        private String statusText;
        private LocalDateTime interviewAt;
        private String settleTeacherName;
        private String settleRemark;
    }

    @Data
    public static class CurrentRound {
        private List<ChoiceVO> choices;
        private WishBrief wish;
        private List<InterviewVO> interviews;
        /** 选中我的老师（本轮阶段 ≥ TEACHER_PICK 时返回） */
        private List<PickerBrief> pickers;
        private FinalChoiceBrief finalChoice;
        private List<DiversionRecordVO> diversionRecords;
    }
}
