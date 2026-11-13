-- 测试库 leb-sele-test 复位脚本：清空全流程流水数据，轮次状态复位为未开始
-- 用法：mysql -uroot -p < test/reset.sql
USE `leb-sele-test`;
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE `flow_stage`;
TRUNCATE TABLE `adjustment_wish`;
TRUNCATE TABLE `student_choice`;
TRUNCATE TABLE `interview`;
TRUNCATE TABLE `teacher_pick`;
TRUNCATE TABLE `student_final_choice`;
TRUNCATE TABLE `diversion_record`;
TRUNCATE TABLE `match_result`;
TRUNCATE TABLE `wait_pool`;
TRUNCATE TABLE `teacher_quota_log`;
UPDATE `selection_round` SET `status` = 0, `choice_start_at` = NULL, `choice_end_at` = NULL;
SET FOREIGN_KEY_CHECKS = 1;
