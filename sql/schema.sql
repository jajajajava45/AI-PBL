-- =====================================================================
-- AI+PBL 师生双向选择系统 — 数据库 DDL
-- 数据库：MySQL 8.x  |  字符集：utf8mb4  |  引擎：InnoDB
-- 日期：2026-11-13
-- =====================================================================

CREATE DATABASE IF NOT EXISTS `leb-sele`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE `leb-sele`;

-- =====================================================================
-- 1. 用户表 sys_user — 学生 / 老师 / 管理员通用登录账号
-- =====================================================================
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username`      VARCHAR(64)     NOT NULL                 COMMENT '登录名（学号/工号/管理员名）',
  `password`      VARCHAR(255)    NOT NULL                 COMMENT '密码（BCrypt 加密）',
  `real_name`     VARCHAR(64)     NOT NULL                 COMMENT '真实姓名',
  `role`          TINYINT         NOT NULL                 COMMENT '角色：1=学生 2=老师 3=管理员',
  `phone`         VARCHAR(32)     DEFAULT NULL             COMMENT '手机号',
  `email`         VARCHAR(128)    DEFAULT NULL             COMMENT '邮箱',
  `avatar`        VARCHAR(512)    DEFAULT NULL             COMMENT '头像 URL',
  `enabled`       TINYINT(1)      NOT NULL DEFAULT 1       COMMENT '是否启用：1=启用 0=禁用',
  `last_login_at` DATETIME        DEFAULT NULL             COMMENT '最后登录时间',
  `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`       TINYINT(1)      NOT NULL DEFAULT 0       COMMENT '软删除：0=未删除 1=已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';


-- =====================================================================
-- 2. 实训室表 training_room
-- =====================================================================
DROP TABLE IF EXISTS `training_room`;
CREATE TABLE `training_room` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name`        VARCHAR(128)    NOT NULL                 COMMENT '实训室名称',
  `code`        VARCHAR(32)     NOT NULL                 COMMENT '实训室编号',
  `description` TEXT            DEFAULT NULL             COMMENT '实训室简介',
  `status`      TINYINT         NOT NULL DEFAULT 1       COMMENT '状态：1=启用 0=停用',
  `created_at`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT(1)      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实训室表';


-- =====================================================================
-- 3. 老师表 teacher — 扩展用户信息，关联实训室，含名额上限
-- =====================================================================
DROP TABLE IF EXISTS `teacher`;
CREATE TABLE `teacher` (
  `id`                 BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`            BIGINT UNSIGNED NOT NULL                 COMMENT '关联 sys_user.id',
  `training_room_id`   BIGINT UNSIGNED NOT NULL                 COMMENT '所属实训室',
  `teacher_no`         VARCHAR(32)     NOT NULL                 COMMENT '工号',
  `title`              VARCHAR(32)     DEFAULT NULL             COMMENT '职称',
  `research_direction` VARCHAR(255)    DEFAULT NULL             COMMENT '研究方向',
  `bio`                TEXT            DEFAULT NULL             COMMENT '个人简介',
  `quota`              INT UNSIGNED    NOT NULL DEFAULT 16      COMMENT '名额上限（约 16，略有差异）',
  `status`             TINYINT         NOT NULL DEFAULT 1       COMMENT '状态：1=活跃 0=不参与',
  `created_at`         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`            TINYINT(1)      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  UNIQUE KEY `uk_teacher_no` (`teacher_no`),
  KEY `idx_training_room` (`training_room_id`),
  CONSTRAINT `fk_teacher_user`   FOREIGN KEY (`user_id`)          REFERENCES `sys_user` (`id`),
  CONSTRAINT `fk_teacher_room`   FOREIGN KEY (`training_room_id`)  REFERENCES `training_room` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='老师表';


-- =====================================================================
-- 4. 学生表 student — 扩展用户信息
-- =====================================================================
DROP TABLE IF EXISTS `student`;
CREATE TABLE `student` (
  `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`      BIGINT UNSIGNED NOT NULL                 COMMENT '关联 sys_user.id',
  `student_no`   VARCHAR(32)     NOT NULL                 COMMENT '学号',
  `major`        VARCHAR(64)     DEFAULT NULL             COMMENT '专业',
  `grade`        VARCHAR(16)     DEFAULT NULL             COMMENT '年级',
  `class_name`   VARCHAR(64)     DEFAULT NULL             COMMENT '班级',
  `created_at`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`      TINYINT(1)      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  UNIQUE KEY `uk_student_no` (`student_no`),
  CONSTRAINT `fk_student_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生表';


-- =====================================================================
-- 5. 录取轮次表 selection_round — 第一轮 / 第二轮补录 / 第三轮补录
-- =====================================================================
DROP TABLE IF EXISTS `selection_round`;
CREATE TABLE `selection_round` (
  `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `round_no`        TINYINT         NOT NULL                 COMMENT '轮次编号：1=第一轮 2=第二轮补录 3=第三轮补录',
  `round_type`      TINYINT         NOT NULL                 COMMENT '轮次类型：1=初选(INITIAL) 2=补录(SUPPLEMENT)',
  `name`            VARCHAR(64)     NOT NULL                 COMMENT '轮次名称，如 "第一轮初选"、"第二轮补录"',
  `choice_start_at` DATETIME        DEFAULT NULL             COMMENT '志愿填报开始时间',
  `choice_end_at`   DATETIME        DEFAULT NULL             COMMENT '志愿填报截止时间',
  `status`          TINYINT         NOT NULL DEFAULT 0       COMMENT '状态：0=未开始 1=进行中 2=已结束',
  `created_at`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`         TINYINT(1)      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_round_no` (`round_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='录取轮次表';


-- =====================================================================
-- 6. 流程阶段表 flow_stage — 每轮内的阶段控制
-- =====================================================================
DROP TABLE IF EXISTS `flow_stage`;
CREATE TABLE `flow_stage` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `round_id`    BIGINT UNSIGNED NOT NULL                 COMMENT '关联 selection_round.id',
  `stage_name`  VARCHAR(32)     NOT NULL                 COMMENT '阶段名：CHOICE_OPEN/INTERVIEW/TEACHER_PICK/STUDENT_FINAL/DIVERSION/COMPLETED',
  `stage_order` TINYINT         NOT NULL                 COMMENT '阶段顺序 1~6',
  `start_at`    DATETIME        DEFAULT NULL             COMMENT '开始时间（可为空，管理员手动触发）',
  `end_at`      DATETIME        DEFAULT NULL             COMMENT '结束时间',
  `status`      TINYINT         NOT NULL DEFAULT 0       COMMENT '状态：0=未开始 1=进行中 2=已结束',
  `remark`      VARCHAR(255)    DEFAULT NULL             COMMENT '备注',
  `created_at`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT(1)      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_round_stage` (`round_id`, `stage_name`),
  KEY `idx_round_status` (`round_id`, `status`),
  CONSTRAINT `fk_stage_round` FOREIGN KEY (`round_id`) REFERENCES `selection_round` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程阶段表';


-- =====================================================================
-- 7. 调剂意愿表 adjustment_wish — 学生在某轮的调剂意愿
-- =====================================================================
DROP TABLE IF EXISTS `adjustment_wish`;
CREATE TABLE `adjustment_wish` (
  `id`                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `round_id`          BIGINT UNSIGNED NOT NULL                 COMMENT '关联 selection_round.id',
  `student_id`        BIGINT UNSIGNED NOT NULL                 COMMENT '关联 student.id',
  `allow_internal`    TINYINT(1)      NOT NULL DEFAULT 0       COMMENT '服从实训室内调剂：0=否 1=是',
  `allow_cross_room`  TINYINT(1)      NOT NULL DEFAULT 0       COMMENT '服从校内调剂：0=否 1=是',
  `created_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`           TINYINT(1)      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_round_student` (`round_id`, `student_id`),
  CONSTRAINT `fk_adj_round`   FOREIGN KEY (`round_id`)   REFERENCES `selection_round` (`id`),
  CONSTRAINT `fk_adj_student` FOREIGN KEY (`student_id`) REFERENCES `student` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='调剂意愿表';


-- =====================================================================
-- 8. 学生志愿表 student_choice — 每轮最多 3 名老师，跨实训室，平行志愿
-- =====================================================================
DROP TABLE IF EXISTS `student_choice`;
CREATE TABLE `student_choice` (
  `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `round_id`     BIGINT UNSIGNED NOT NULL                 COMMENT '关联 selection_round.id',
  `student_id`   BIGINT UNSIGNED NOT NULL                 COMMENT '关联 student.id',
  `teacher_id`   BIGINT UNSIGNED NOT NULL                 COMMENT '关联 teacher.id',
  `choice_rank`  TINYINT         NOT NULL                 COMMENT '志愿顺序 1~3（仅展示用，业务上平行志愿）',
  `created_at`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`      TINYINT(1)      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_round_student_teacher` (`round_id`, `student_id`, `teacher_id`),
  KEY `idx_round_teacher` (`round_id`, `teacher_id`),
  KEY `idx_student` (`student_id`),
  CONSTRAINT `fk_choice_round`   FOREIGN KEY (`round_id`)   REFERENCES `selection_round` (`id`),
  CONSTRAINT `fk_choice_student` FOREIGN KEY (`student_id`) REFERENCES `student` (`id`),
  CONSTRAINT `fk_choice_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `teacher` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生志愿表';


-- =====================================================================
-- 9. 面试记录表 interview — 学生与志愿老师的面试
-- =====================================================================
DROP TABLE IF EXISTS `interview`;
CREATE TABLE `interview` (
  `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `round_id`     BIGINT UNSIGNED NOT NULL                 COMMENT '关联 selection_round.id',
  `student_id`   BIGINT UNSIGNED NOT NULL                 COMMENT '关联 student.id',
  `teacher_id`   BIGINT UNSIGNED NOT NULL                 COMMENT '关联 teacher.id',
  `interview_time` DATETIME      DEFAULT NULL             COMMENT '面试时间（老师安排）',
  `location`     VARCHAR(128)    DEFAULT NULL             COMMENT '面试地点',
  `status`       TINYINT         NOT NULL DEFAULT 0       COMMENT '状态：0=待面试 1=已安排 2=已完成 3=已取消',
  `score`        DECIMAL(5,2)    DEFAULT NULL             COMMENT '面试成绩（可选，用于系统统筹排序）',
  `feedback`     TEXT            DEFAULT NULL             COMMENT '老师对学生的面试评价',
  `remark`       VARCHAR(255)    DEFAULT NULL             COMMENT '备注',
  `created_at`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`      TINYINT(1)      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_round_student_teacher` (`round_id`, `student_id`, `teacher_id`),
  KEY `idx_round_teacher` (`round_id`, `teacher_id`),
  KEY `idx_student` (`student_id`),
  CONSTRAINT `fk_interview_round`   FOREIGN KEY (`round_id`)   REFERENCES `selection_round` (`id`),
  CONSTRAINT `fk_interview_student` FOREIGN KEY (`student_id`) REFERENCES `student` (`id`),
  CONSTRAINT `fk_interview_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `teacher` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='面试记录表';


-- =====================================================================
-- 10. 老师选学生表 teacher_pick — 老师在该轮选中的学生
-- =====================================================================
DROP TABLE IF EXISTS `teacher_pick`;
CREATE TABLE `teacher_pick` (
  `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `round_id`     BIGINT UNSIGNED NOT NULL                 COMMENT '关联 selection_round.id',
  `teacher_id`   BIGINT UNSIGNED NOT NULL                 COMMENT '选人的老师',
  `student_id`   BIGINT UNSIGNED NOT NULL                 COMMENT '被选的学生',
  `pick_source`  TINYINT         NOT NULL DEFAULT 1       COMMENT '来源：1=学生主动填报该老师 2=调剂范围内选的学生',
  `created_at`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`      TINYINT(1)      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_round_teacher_student_source` (`round_id`, `teacher_id`, `student_id`, `pick_source`),
  KEY `idx_round_student` (`round_id`, `student_id`),
  CONSTRAINT `fk_pick_round`   FOREIGN KEY (`round_id`)   REFERENCES `selection_round` (`id`),
  CONSTRAINT `fk_pick_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `teacher` (`id`),
  CONSTRAINT `fk_pick_student` FOREIGN KEY (`student_id`) REFERENCES `student` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='老师选学生表';


-- =====================================================================
-- 11. 学生终选表 student_final_choice — 从 ≥2 位选中老师中终选一位
-- =====================================================================
DROP TABLE IF EXISTS `student_final_choice`;
CREATE TABLE `student_final_choice` (
  `id`             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `round_id`       BIGINT UNSIGNED NOT NULL                 COMMENT '关联 selection_round.id',
  `student_id`     BIGINT UNSIGNED NOT NULL                 COMMENT '关联 student.id',
  `teacher_id`     BIGINT UNSIGNED NOT NULL                 COMMENT '最终选择的老师',
  `timeout`        TINYINT(1)      NOT NULL DEFAULT 0       COMMENT '是否超时自动选择：0=学生主动 1=超时自动',
  `created_at`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`        TINYINT(1)      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_round_student` (`round_id`, `student_id`),
  CONSTRAINT `fk_final_round`   FOREIGN KEY (`round_id`)   REFERENCES `selection_round` (`id`),
  CONSTRAINT `fk_final_student` FOREIGN KEY (`student_id`) REFERENCES `student` (`id`),
  CONSTRAINT `fk_final_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `teacher` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生终选表';


-- =====================================================================
-- 12. 分流记录表 diversion_record — 每轮后调剂分流的完整过程
-- =====================================================================
DROP TABLE IF EXISTS `diversion_record`;
CREATE TABLE `diversion_record` (
  `id`             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `round_id`       BIGINT UNSIGNED NOT NULL                 COMMENT '关联 selection_round.id（从哪一轮后进入分流）',
  `student_id`     BIGINT UNSIGNED NOT NULL                 COMMENT '关联 student.id',
  `step`           TINYINT         NOT NULL                 COMMENT '分流步骤：1=TEACHER_SELECT 2=SYSTEM_INTERNAL 3=SYSTEM_CROSS',
  `teacher_id`     BIGINT UNSIGNED DEFAULT NULL             COMMENT '此步骤中若被某位老师选中 / 系统分配的老师',
  `scope_type`     TINYINT         DEFAULT NULL             COMMENT '调剂范围：1=实训室内 2=校内（记录该步骤使用的范围）',
  `result`         TINYINT         NOT NULL                 COMMENT '本步骤结果：1=成功安置 2=未选中（流转下一步） 3=彻底失败',
  `created_at`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`        TINYINT(1)      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_round_student` (`round_id`, `student_id`),
  KEY `idx_teacher` (`teacher_id`),
  CONSTRAINT `fk_div_round`   FOREIGN KEY (`round_id`)   REFERENCES `selection_round` (`id`),
  CONSTRAINT `fk_div_student` FOREIGN KEY (`student_id`) REFERENCES `student` (`id`),
  CONSTRAINT `fk_div_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `teacher` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分流记录表';


-- =====================================================================
-- 13. 最终匹配结果表 match_result — 学生最终归属哪位老师
-- =====================================================================
DROP TABLE IF EXISTS `match_result`;
CREATE TABLE `match_result` (
  `id`             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `student_id`     BIGINT UNSIGNED NOT NULL                 COMMENT '关联 student.id',
  `teacher_id`     BIGINT UNSIGNED NOT NULL                 COMMENT '最终归属的老师',
  `final_round_id` BIGINT UNSIGNED NOT NULL                 COMMENT '在哪一轮确定的（含分流/统筹轮次）',
  `match_source`   TINYINT         NOT NULL                 COMMENT '来源：1=NORMAL 正常匹配 2=TEACHER_DIVERT 调剂老师选 3=SYSTEM_DIVERT 系统统筹 4=MANUAL_SETTLE 待选池人工安置',
  `match_status`   TINYINT         NOT NULL DEFAULT 1       COMMENT '状态：1=MATCHED 已匹配 0=CANCELLED 已取消',
  `created_at`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`        TINYINT(1)      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_student` (`student_id`, `deleted`),
  KEY `idx_teacher_status` (`teacher_id`, `match_status`),
  CONSTRAINT `fk_match_student` FOREIGN KEY (`student_id`)     REFERENCES `student` (`id`),
  CONSTRAINT `fk_match_teacher` FOREIGN KEY (`teacher_id`)     REFERENCES `teacher` (`id`),
  CONSTRAINT `fk_match_round`   FOREIGN KEY (`final_round_id`) REFERENCES `selection_round` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='最终匹配结果表';


-- =====================================================================
-- 14. 待选池表 wait_pool — 最后一轮后未安置的学生
-- =====================================================================
DROP TABLE IF EXISTS `wait_pool`;
CREATE TABLE `wait_pool` (
  `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `student_id`   BIGINT UNSIGNED NOT NULL                 COMMENT '关联 student.id',
  `enter_round`  BIGINT UNSIGNED NOT NULL                 COMMENT '从哪一轮分流后进入待选池',
  `enter_at`     DATETIME        NOT NULL                 COMMENT '进入时间',
  `interview_at` DATETIME        DEFAULT NULL             COMMENT '约谈时间',
  `status`       TINYINT         NOT NULL DEFAULT 0       COMMENT '状态：0=PENDING 待约谈 1=INTERVIEWED 已约谈 2=SETTLED 已安置',
  `settle_teacher_id` BIGINT UNSIGNED DEFAULT NULL         COMMENT '人工安置的老师',
  `settle_by`    BIGINT UNSIGNED DEFAULT NULL             COMMENT '操作的管理员用户 ID',
  `settle_remark` VARCHAR(255)    DEFAULT NULL             COMMENT '安置备注',
  `settle_at`    DATETIME        DEFAULT NULL             COMMENT '安置时间',
  `created_at`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`      TINYINT(1)      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_student` (`student_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_wait_student` FOREIGN KEY (`student_id`) REFERENCES `student` (`id`),
  CONSTRAINT `fk_wait_round`   FOREIGN KEY (`enter_round`)  REFERENCES `selection_round` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='待选池表';


-- =====================================================================
-- 15. 老师名额变动日志 teacher_quota_log — 审计
-- =====================================================================
DROP TABLE IF EXISTS `teacher_quota_log`;
CREATE TABLE `teacher_quota_log` (
  `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `teacher_id`   BIGINT UNSIGNED NOT NULL                 COMMENT '老师',
  `quota_before` INT UNSIGNED    DEFAULT NULL             COMMENT '变动前名额',
  `quota_after`  INT UNSIGNED    NOT NULL                 COMMENT '变动后名额',
  `reason`       VARCHAR(128)    DEFAULT NULL             COMMENT '变动原因：ADMIN_MODIFY / SYSTERM_DIVERT 等',
  `operator_id`  BIGINT UNSIGNED DEFAULT NULL             COMMENT '操作人（管理员用户 ID）',
  `created_at`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted`      TINYINT(1)      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_teacher` (`teacher_id`),
  CONSTRAINT `fk_quotalog_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `teacher` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='老师名额变动日志';


-- =====================================================================
-- 16. 系统配置表 system_config
-- =====================================================================
DROP TABLE IF EXISTS `system_config`;
CREATE TABLE `system_config` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `config_key`  VARCHAR(64)     NOT NULL                 COMMENT '配置键',
  `config_value`VARCHAR(512)    NOT NULL                 COMMENT '配置值',
  `config_desc` VARCHAR(255)    DEFAULT NULL             COMMENT '说明',
  `created_at`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT(1)      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';


-- =====================================================================
-- 初始数据：实训室、系统配置、示例轮次
-- =====================================================================

-- 6 个实训室
INSERT INTO `training_room` (`name`, `code`, `description`) VALUES
  ('智能软件开发实训室', 'ROOM-SD',   '聚焦 AI 应用开发与工程实践'),
  ('数据分析与可视化实训室', 'ROOM-DA', '数据采集、挖掘与可视化方向'),
  ('人工智能模型实训室', 'ROOM-AI',    '大模型训练、微调与部署方向'),
  ('机器人与嵌入式实训室', 'ROOM-RB',  '智能硬件与嵌入式开发方向'),
  ('产品与交互设计实训室', 'ROOM-UI',  '产品设计、交互与前端方向'),
  ('网络与安全实训室', 'ROOM-NS',       '网络架构、信息安全方向');

-- 系统配置
INSERT INTO `system_config` (`config_key`, `config_value`, `config_desc`) VALUES
  ('max_supplement_rounds',    '2',    '补录轮数（不含第一轮），默认 2 轮（共 3 轮）'),
  ('max_choices_per_round',    '3',    '每轮最多可选老师数'),
  ('teacher_default_quota',    '16',   '老师默认名额上限'),
  ('final_choice_timeout_min', '60',   '学生终选超时时间（分钟）'),
  ('choice_modify_enabled',    'true', '志愿关闭前是否允许修改');

-- 示例轮次配置
INSERT INTO `selection_round` (`round_no`, `round_type`, `name`) VALUES
  (1, 1, '第一轮初选'),
  (2, 2, '第二轮补录'),
  (3, 2, '第三轮补录');
