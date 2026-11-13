package com.pbl.selection.common;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 业务枚举常量（数据库中以 TINYINT 编码存储）
 */
public final class Enums {

    private Enums() {
    }

    /** 角色 */
    public enum Role {
        STUDENT(1, "学生"),
        TEACHER(2, "老师"),
        ADMIN(3, "管理员");

        public final int code;
        public final String text;

        Role(int code, String text) {
            this.code = code;
            this.text = text;
        }

        private static final Map<Integer, Role> MAP =
                Arrays.stream(values()).collect(Collectors.toMap(e -> e.code, Function.identity()));

        public static Role of(int code) {
            return MAP.get(code);
        }
    }

    /** 轮次类型 */
    public enum RoundType {
        INITIAL(1, "初选"),
        SUPPLEMENT(2, "补录");

        public final int code;
        public final String text;

        RoundType(int code, String text) {
            this.code = code;
            this.text = text;
        }
    }

    /** 轮次状态 */
    public enum RoundStatus {
        NOT_STARTED(0, "未开始"),
        IN_PROGRESS(1, "进行中"),
        FINISHED(2, "已结束");

        public final int code;
        public final String text;

        RoundStatus(int code, String text) {
            this.code = code;
            this.text = text;
        }
    }

    /** 流程阶段名（与 flow_stage.stage_name 对应） */
    public enum StageName {
        CHOICE_OPEN(1, "志愿填报"),
        CHOICE_CLOSED(2, "志愿关闭"),
        INTERVIEW(3, "面试"),
        TEACHER_PICK(4, "老师选学生"),
        STUDENT_FINAL(5, "学生终选"),
        DIVERSION(6, "调剂分流"),
        COMPLETED(7, "本轮完成");

        public final int order;
        public final String text;

        StageName(int order, String text) {
            this.order = order;
            this.text = text;
        }

        private static final Map<String, StageName> MAP =
                Arrays.stream(values()).collect(Collectors.toMap(Enum::name, Function.identity()));

        public static StageName of(String name) {
            return MAP.get(name);
        }
    }

    /** 阶段状态 */
    public enum StageStatus {
        NOT_STARTED(0, "未开始"),
        IN_PROGRESS(1, "进行中"),
        FINISHED(2, "已结束");

        public final int code;
        public final String text;

        StageStatus(int code, String text) {
            this.code = code;
            this.text = text;
        }
    }

    /** 老师选学生来源 */
    public enum PickSource {
        CHOICE(1, "学生志愿填报"),
        DIVERSION(2, "调剂范围内选择");

        public final int code;
        public final String text;

        PickSource(int code, String text) {
            this.code = code;
            this.text = text;
        }
    }

    /** 匹配来源 */
    public enum MatchSource {
        NORMAL(1, "正常匹配"),
        TEACHER_DIVERT(2, "调剂老师选择"),
        SYSTEM_DIVERT(3, "系统统筹"),
        MANUAL_SETTLE(4, "待选池人工安置");

        public final int code;
        public final String text;

        MatchSource(int code, String text) {
            this.code = code;
            this.text = text;
        }

        private static final Map<Integer, MatchSource> MAP =
                Arrays.stream(values()).collect(Collectors.toMap(e -> e.code, Function.identity()));

        public static MatchSource of(int code) {
            return MAP.get(code);
        }
    }

    /** 匹配状态 */
    public enum MatchStatus {
        CANCELLED(0, "已取消"),
        MATCHED(1, "已匹配");

        public final int code;
        public final String text;

        MatchStatus(int code, String text) {
            this.code = code;
            this.text = text;
        }
    }

    /** 面试状态 */
    public enum InterviewStatus {
        PENDING(0, "待安排"),
        SCHEDULED(1, "已安排"),
        COMPLETED(2, "已完成"),
        CANCELLED(3, "已取消");

        public final int code;
        public final String text;

        InterviewStatus(int code, String text) {
            this.code = code;
            this.text = text;
        }
    }

    /** 待选池状态 */
    public enum WaitPoolStatus {
        PENDING(0, "待约谈"),
        INTERVIEWED(1, "已约谈"),
        SETTLED(2, "已安置");

        public final int code;
        public final String text;

        WaitPoolStatus(int code, String text) {
            this.code = code;
            this.text = text;
        }
    }

    /** 分流步骤 */
    public enum DiversionStep {
        TEACHER_SELECT(1, "调剂范围内老师选择"),
        SYSTEM_INTERNAL(2, "系统统筹-实训室内"),
        SYSTEM_CROSS(3, "系统统筹-全校");

        public final int code;
        public final String text;

        DiversionStep(int code, String text) {
            this.code = code;
            this.text = text;
        }
    }

    /** 分流单步结果 */
    public enum DiversionResult {
        PLACED(1, "成功安置"),
        PASSED(2, "未安置，流转下一步"),
        FAILED(3, "彻底失败");

        public final int code;
        public final String text;

        DiversionResult(int code, String text) {
            this.code = code;
            this.text = text;
        }
    }

    /** 调剂范围 */
    public enum ScopeType {
        INTERNAL(1, "实训室内"),
        CROSS(2, "校内");

        public final int code;
        public final String text;

        ScopeType(int code, String text) {
            this.code = code;
            this.text = text;
        }
    }

    /** 学生综合状态码（实时推导，非落库字段） */
    public enum StudentStatus {
        NOT_CHOSEN("NOT_CHOSEN", "未填报志愿"),
        CHOICE_SUBMITTED("CHOICE_SUBMITTED", "已提交志愿"),
        INTERVIEWING("INTERVIEWING", "面试中"),
        WAIT_TEACHER_PICK("WAIT_TEACHER_PICK", "等待老师选择"),
        PICKED("PICKED", "已被老师选中"),
        WAIT_FINAL("WAIT_FINAL", "待终选"),
        MATCHED("MATCHED", "已匹配"),
        DIVERSION_TEACHER_SELECT("DIVERSION_TEACHER_SELECT", "分流中-等待调剂老师选择"),
        DIVERSION_SYSTEM("DIVERSION_SYSTEM", "分流中-等待系统统筹"),
        TO_NEXT_ROUND("TO_NEXT_ROUND", "流转下一轮补录"),
        WAIT_POOL("WAIT_POOL", "待选池"),
        SETTLED("SETTLED", "已安置");

        public final String code;
        public final String text;

        StudentStatus(String code, String text) {
            this.code = code;
            this.text = text;
        }
    }
}
