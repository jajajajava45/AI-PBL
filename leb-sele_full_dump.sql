-- MySQL dump 10.13  Distrib 8.0.28, for Win64 (x86_64)
--
-- Host: localhost    Database: leb-sele
-- ------------------------------------------------------
-- Server version	8.0.28

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `leb-sele`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `leb-sele` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `leb-sele`;

--
-- Table structure for table `adjustment_wish`
--

DROP TABLE IF EXISTS `adjustment_wish`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `adjustment_wish` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `round_id` bigint unsigned NOT NULL COMMENT '关联 selection_round.id',
  `student_id` bigint unsigned NOT NULL COMMENT '关联 student.id',
  `allow_internal` tinyint(1) NOT NULL DEFAULT '0' COMMENT '服从实训室内调剂：0=否 1=是',
  `allow_cross_room` tinyint(1) NOT NULL DEFAULT '0' COMMENT '服从校内调剂：0=否 1=是',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_round_student` (`round_id`,`student_id`),
  KEY `fk_adj_student` (`student_id`),
  CONSTRAINT `fk_adj_round` FOREIGN KEY (`round_id`) REFERENCES `selection_round` (`id`),
  CONSTRAINT `fk_adj_student` FOREIGN KEY (`student_id`) REFERENCES `student` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='调剂意愿表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `adjustment_wish`
--

LOCK TABLES `adjustment_wish` WRITE;
/*!40000 ALTER TABLE `adjustment_wish` DISABLE KEYS */;
INSERT INTO `adjustment_wish` VALUES (1,1,1,0,0,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(2,1,2,0,0,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(3,1,3,0,0,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(4,1,4,0,0,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(5,1,5,0,0,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(6,1,6,0,0,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(7,1,7,1,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(8,1,8,1,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(9,1,9,1,0,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(10,1,10,1,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(11,1,11,0,0,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(12,1,12,0,0,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(13,1,13,1,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(14,1,14,1,0,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(15,1,15,1,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(16,1,16,1,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(17,2,17,1,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(18,2,18,1,0,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(19,2,19,1,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(20,2,20,1,0,'2026-11-13 15:50:29','2026-11-13 15:50:29',0);
/*!40000 ALTER TABLE `adjustment_wish` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `diversion_record`
--

DROP TABLE IF EXISTS `diversion_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `diversion_record` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `round_id` bigint unsigned NOT NULL COMMENT '关联 selection_round.id（从哪一轮后进入分流）',
  `student_id` bigint unsigned NOT NULL COMMENT '关联 student.id',
  `step` tinyint NOT NULL COMMENT '分流步骤：1=TEACHER_SELECT 2=SYSTEM_INTERNAL 3=SYSTEM_CROSS',
  `teacher_id` bigint unsigned DEFAULT NULL COMMENT '此步骤中若被某位老师选中 / 系统分配的老师',
  `scope_type` tinyint DEFAULT NULL COMMENT '调剂范围：1=实训室内 2=校内（记录该步骤使用的范围）',
  `result` tinyint NOT NULL COMMENT '本步骤结果：1=成功安置 2=未选中（流转下一步） 3=彻底失败',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_round_student` (`round_id`,`student_id`),
  KEY `idx_teacher` (`teacher_id`),
  KEY `fk_div_student` (`student_id`),
  CONSTRAINT `fk_div_round` FOREIGN KEY (`round_id`) REFERENCES `selection_round` (`id`),
  CONSTRAINT `fk_div_student` FOREIGN KEY (`student_id`) REFERENCES `student` (`id`),
  CONSTRAINT `fk_div_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `teacher` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='分流记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `diversion_record`
--

LOCK TABLES `diversion_record` WRITE;
/*!40000 ALTER TABLE `diversion_record` DISABLE KEYS */;
INSERT INTO `diversion_record` VALUES (1,1,7,1,1,2,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(2,1,8,1,11,2,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(3,1,9,2,NULL,1,2,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(4,1,10,2,NULL,1,2,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(5,1,10,3,2,2,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(6,1,13,2,NULL,1,2,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(7,1,13,3,1,2,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(8,1,14,2,NULL,1,2,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(9,1,15,2,NULL,1,2,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(10,1,15,3,NULL,2,2,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(11,1,16,2,NULL,1,2,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(12,1,16,3,NULL,2,2,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(13,2,18,2,NULL,1,2,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(14,2,18,3,4,2,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(15,2,20,1,11,2,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(16,2,15,2,NULL,1,2,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(17,2,15,3,NULL,2,2,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(18,2,16,2,NULL,1,2,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(19,2,16,3,NULL,2,2,'2026-11-13 15:50:29','2026-11-13 15:50:29',0);
/*!40000 ALTER TABLE `diversion_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `flow_stage`
--

DROP TABLE IF EXISTS `flow_stage`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flow_stage` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `round_id` bigint unsigned NOT NULL COMMENT '关联 selection_round.id',
  `stage_name` varchar(32) NOT NULL COMMENT '阶段名：CHOICE_OPEN/INTERVIEW/TEACHER_PICK/STUDENT_FINAL/DIVERSION/COMPLETED',
  `stage_order` tinyint NOT NULL COMMENT '阶段顺序 1~6',
  `start_at` datetime DEFAULT NULL COMMENT '开始时间（可为空，管理员手动触发）',
  `end_at` datetime DEFAULT NULL COMMENT '结束时间',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0=未开始 1=进行中 2=已结束',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_round_stage` (`round_id`,`stage_name`),
  KEY `idx_round_status` (`round_id`,`status`),
  CONSTRAINT `fk_stage_round` FOREIGN KEY (`round_id`) REFERENCES `selection_round` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='流程阶段表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flow_stage`
--

LOCK TABLES `flow_stage` WRITE;
/*!40000 ALTER TABLE `flow_stage` DISABLE KEYS */;
INSERT INTO `flow_stage` VALUES (1,1,'CHOICE_OPEN',1,'2026-11-14 09:00:00','2026-11-14 22:00:00',2,'第一轮志愿填报','2026-11-13 15:50:29','2026-11-13 15:50:29',0),(2,1,'CHOICE_CLOSED',2,'2026-11-14 22:00:00','2026-11-15 08:00:00',2,'志愿关闭','2026-11-13 15:50:29','2026-11-13 15:50:29',0),(3,1,'INTERVIEW',3,'2026-11-15 09:00:00','2026-11-15 18:00:00',2,'第一轮面试','2026-11-13 15:50:29','2026-11-13 15:50:29',0),(4,1,'TEACHER_PICK',4,'2026-11-16 09:00:00','2026-11-16 20:00:00',2,'老师选学生','2026-11-13 15:50:29','2026-11-13 15:50:29',0),(5,1,'STUDENT_FINAL',5,'2026-11-17 09:00:00','2026-11-17 18:00:00',2,'学生终选','2026-11-13 15:50:29','2026-11-13 15:50:29',0),(6,1,'DIVERSION',6,'2026-11-18 09:00:00','2026-11-18 18:00:00',2,'调剂分流 + 系统统筹','2026-11-13 15:50:29','2026-11-13 15:50:29',0),(7,1,'COMPLETED',7,'2026-11-19 00:00:00',NULL,2,'第一轮完成','2026-11-13 15:50:29','2026-11-13 15:50:29',0),(8,2,'CHOICE_OPEN',1,'2026-11-19 09:00:00','2026-11-19 22:00:00',2,'第二轮志愿重填','2026-11-13 15:50:29','2026-11-13 15:50:29',0),(9,2,'CHOICE_CLOSED',2,'2026-11-19 22:00:00','2026-11-20 08:00:00',2,'志愿关闭','2026-11-13 15:50:29','2026-11-13 15:50:29',0),(10,2,'INTERVIEW',3,'2026-11-20 09:00:00','2026-11-20 18:00:00',2,'第二轮面试','2026-11-13 15:50:29','2026-11-13 15:50:29',0),(11,2,'TEACHER_PICK',4,'2026-11-21 09:00:00','2026-11-21 20:00:00',2,'老师选学生','2026-11-13 15:50:29','2026-11-13 15:50:29',0),(12,2,'STUDENT_FINAL',5,'2026-11-22 09:00:00','2026-11-22 18:00:00',2,'学生终选','2026-11-13 15:50:29','2026-11-13 15:50:29',0),(13,2,'DIVERSION',6,'2026-11-23 09:00:00','2026-11-23 18:00:00',2,'调剂分流','2026-11-13 15:50:29','2026-11-13 15:50:29',0),(14,2,'COMPLETED',7,'2026-11-24 00:00:00',NULL,2,'第二轮完成','2026-11-13 15:50:29','2026-11-13 15:50:29',0),(15,3,'CHOICE_OPEN',1,'2026-11-24 09:00:00','2026-11-24 22:00:00',2,'第三轮志愿重填','2026-11-13 15:50:29','2026-11-13 15:50:29',0),(16,3,'CHOICE_CLOSED',2,'2026-11-24 22:00:00','2026-11-25 08:00:00',2,'志愿关闭','2026-11-13 15:50:29','2026-11-13 15:50:29',0),(17,3,'INTERVIEW',3,'2026-11-25 09:00:00','2026-11-25 18:00:00',2,'第三轮面试','2026-11-13 15:50:29','2026-11-13 15:50:29',0),(18,3,'TEACHER_PICK',4,'2026-11-26 09:00:00','2026-11-26 18:00:00',2,'老师选学生（无人选）','2026-11-13 15:50:29','2026-11-13 15:50:29',0),(19,3,'STUDENT_FINAL',5,'2026-11-26 18:00:01','2026-11-26 18:00:02',2,'无人终选','2026-11-13 15:50:29','2026-11-13 15:50:29',0),(20,3,'DIVERSION',6,'2026-11-26 18:00:03','2026-11-26 18:00:04',2,'统筹失败→待选池','2026-11-13 15:50:29','2026-11-13 15:50:29',0),(21,3,'COMPLETED',7,'2026-11-27 00:00:00',NULL,2,'全部完成','2026-11-13 15:50:29','2026-11-13 15:50:29',0);
/*!40000 ALTER TABLE `flow_stage` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `interview`
--

DROP TABLE IF EXISTS `interview`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `interview` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `round_id` bigint unsigned NOT NULL COMMENT '关联 selection_round.id',
  `student_id` bigint unsigned NOT NULL COMMENT '关联 student.id',
  `teacher_id` bigint unsigned NOT NULL COMMENT '关联 teacher.id',
  `interview_time` datetime DEFAULT NULL COMMENT '面试时间（老师安排）',
  `location` varchar(128) DEFAULT NULL COMMENT '面试地点',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0=待面试 1=已安排 2=已完成 3=已取消',
  `score` decimal(5,2) DEFAULT NULL COMMENT '面试成绩（可选，用于系统统筹排序）',
  `feedback` text COMMENT '老师对学生的面试评价',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_round_student_teacher` (`round_id`,`student_id`,`teacher_id`),
  KEY `idx_round_teacher` (`round_id`,`teacher_id`),
  KEY `idx_student` (`student_id`),
  KEY `fk_interview_teacher` (`teacher_id`),
  CONSTRAINT `fk_interview_round` FOREIGN KEY (`round_id`) REFERENCES `selection_round` (`id`),
  CONSTRAINT `fk_interview_student` FOREIGN KEY (`student_id`) REFERENCES `student` (`id`),
  CONSTRAINT `fk_interview_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `teacher` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=85 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='面试记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `interview`
--

LOCK TABLES `interview` WRITE;
/*!40000 ALTER TABLE `interview` DISABLE KEYS */;
INSERT INTO `interview` VALUES (1,1,1,1,'2026-11-15 09:00:00','SD 实训室 A301',2,85.50,'基本功扎实，编码规范好',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(2,1,1,2,'2026-11-15 10:00:00','SD 实训室 A302',2,82.00,'前端基础不错',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(3,1,1,3,'2026-11-15 11:00:00','DA 实训室 B201',2,80.00,'对数据仓库有了解',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(4,1,2,2,'2026-11-15 09:30:00','SD 实训室 A302',2,88.00,'React 源码有研究',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(5,1,2,4,'2026-11-15 10:30:00','DA 实训室 B202',2,83.00,'ECharts 熟练',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(6,1,2,9,'2026-11-15 11:30:00','UI 实训室 C101',2,79.00,'产品思维尚可',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(7,1,3,3,'2026-11-15 09:00:00','DA 实训室 B201',2,90.00,'数据湖架构有实践',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(8,1,3,4,'2026-11-15 10:00:00','DA 实训室 B202',2,87.00,'可视化作品不错',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(9,1,3,11,'2026-11-15 11:00:00','NS 实训室 D401',2,75.00,'网络基础一般',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(10,1,4,5,'2026-11-15 09:00:00','AI 实训室 B301',2,95.00,'大模型原理理解深刻',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(11,1,4,6,'2026-11-15 10:00:00','AI 实训室 B302',2,92.00,'LoRA 微调有实战',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(12,1,4,1,'2026-11-15 11:00:00','SD 实训室 A301',2,80.00,'基础还行',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(13,1,5,7,'2026-11-15 13:00:00','RB 实训室 D201',2,93.00,'STM32 驱动有经验',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(14,1,5,8,'2026-11-15 14:00:00','RB 实训室 D202',2,91.00,'ROS SLAM 理解深入',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(15,1,5,5,'2026-11-15 15:00:00','AI 实训室 B301',2,85.00,'AI 了解但非方向',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(16,1,6,5,'2026-11-15 13:30:00','AI 实训室 B301',2,88.00,'大模型部署有实践',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(17,1,6,7,'2026-11-15 14:30:00','RB 实训室 D201',2,89.00,'嵌入式经验丰富',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(18,1,6,3,'2026-11-15 15:30:00','DA 实训室 B201',2,78.00,'数据方向一般',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(19,1,7,5,'2026-11-15 09:00:00','AI 实训室 B301',2,82.00,'基础不错',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(20,1,7,6,'2026-11-15 10:00:00','AI 实训室 B302',2,80.00,'微调方向有兴趣',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(21,1,7,1,'2026-11-15 11:00:00','SD 实训室 A301',2,76.00,'编码中等',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(22,1,8,5,'2026-11-15 09:30:00','AI 实训室 B301',2,78.00,'AI 基础一般',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(23,1,8,6,'2026-11-15 10:30:00','AI 实训室 B302',2,77.00,'需要培养',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(24,1,8,11,'2026-11-15 11:30:00','NS 实训室 D401',2,83.00,'Web 安全不错',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(25,1,9,7,'2026-11-15 09:00:00','RB 实训室 D201',2,85.00,'嵌入式不错',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(26,1,9,8,'2026-11-15 10:00:00','RB 实训室 D202',2,84.00,'机器人方向可以',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(27,1,9,4,'2026-11-15 11:00:00','DA 实训室 B202',2,72.00,'可视化需要补',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(28,1,10,5,'2026-11-15 09:30:00','AI 实训室 B301',2,81.00,'AI 有兴趣',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(29,1,10,7,'2026-11-15 10:30:00','RB 实训室 D201',2,80.00,'嵌入式一般',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(30,1,10,12,'2026-11-15 11:30:00','NS 实训室 D402',2,74.00,'网络基础尚可',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(31,1,11,9,'2026-11-15 09:00:00','UI 实训室 C101',2,70.00,'产品文档写得一般',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(32,1,11,10,'2026-11-15 10:00:00','UI 实训室 C102',2,72.00,'Figma 还需练习',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(33,1,11,1,'2026-11-15 11:00:00','SD 实训室 A301',2,68.00,'编程较弱',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(34,1,12,11,'2026-11-15 09:30:00','NS 实训室 D401',2,65.00,'网络基础薄弱',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(35,1,12,12,'2026-11-15 10:30:00','NS 实训室 D402',2,67.00,'K8s 了解少',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(36,1,12,2,'2026-11-15 11:30:00','SD 实训室 A302',2,70.00,'前端基础',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(37,1,13,5,'2026-11-15 13:00:00','AI 实训室 B301',2,79.00,'AI 基础',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(38,1,13,6,'2026-11-15 14:00:00','AI 实训室 B302',2,76.00,'微调方向',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(39,1,13,3,'2026-11-15 15:00:00','DA 实训室 B201',2,74.00,'数仓一般',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(40,1,14,7,'2026-11-15 13:30:00','RB 实训室 D201',2,82.00,'嵌入式不错',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(41,1,14,8,'2026-11-15 14:30:00','RB 实训室 D202',2,80.00,'机器人方向',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(42,1,14,2,'2026-11-15 15:30:00','SD 实训室 A302',2,71.00,'前端基础',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(43,1,15,5,'2026-11-15 09:00:00','AI 实训室 B301',2,55.00,'AI 概念模糊',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(44,1,15,6,'2026-11-15 10:00:00','AI 实训室 B302',2,58.00,'基本概念不清',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(45,1,15,1,'2026-11-15 11:00:00','SD 实训室 A301',2,60.00,'编程基础弱',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(46,1,16,7,'2026-11-15 09:30:00','RB 实训室 D201',2,52.00,'机械基础差',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(47,1,16,8,'2026-11-15 10:30:00','RB 实训室 D202',2,54.00,'机器人不懂',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(48,1,16,5,'2026-11-15 11:30:00','AI 实训室 B301',2,56.00,'AI 不了解',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(49,2,11,9,'2026-11-20 09:00:00','UI 实训室 C101',2,75.00,'比上次好一些',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(50,2,11,10,'2026-11-20 10:00:00','UI 实训室 C102',2,78.00,'Figma 进步了',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(51,2,11,5,'2026-11-20 11:00:00','AI 实训室 B301',2,70.00,'AI 还是一般',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(52,2,12,11,'2026-11-20 09:30:00','NS 实训室 D401',2,72.00,'网络基础提升',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(53,2,12,12,'2026-11-20 10:30:00','NS 实训室 D402',2,74.00,'K8s 开始入门',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(54,2,12,3,'2026-11-20 11:30:00','DA 实训室 B201',2,68.00,'数仓较弱',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(55,2,9,3,'2026-11-20 13:00:00','DA 实训室 B201',2,82.00,'数分方向可以',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(56,2,9,4,'2026-11-20 14:00:00','DA 实训室 B202',2,80.00,'可视化不错',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(57,2,9,1,'2026-11-20 15:00:00','SD 实训室 A301',2,75.00,'编码一般',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(58,2,14,1,'2026-11-20 13:30:00','SD 实训室 A301',2,80.00,'编码不错',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(59,2,14,2,'2026-11-20 14:30:00','SD 实训室 A302',2,78.00,'前端基础',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(60,2,14,9,'2026-11-20 15:30:00','UI 实训室 C101',2,70.00,'产品方向一般',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(61,2,15,1,'2026-11-20 09:00:00','SD 实训室 A301',2,58.00,'基础差',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(62,2,15,4,'2026-11-20 10:00:00','DA 实训室 B202',2,60.00,'一般',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(63,2,15,11,'2026-11-20 11:00:00','NS 实训室 D401',2,55.00,'较弱',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(64,2,16,12,'2026-11-20 09:30:00','NS 实训室 D402',2,54.00,'一般',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(65,2,16,10,'2026-11-20 10:30:00','UI 实训室 C102',2,56.00,'较弱',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(66,2,16,6,'2026-11-20 11:30:00','AI 实训室 B302',2,53.00,'很差',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(67,2,17,3,'2026-11-20 13:00:00','DA 实训室 B201',2,85.00,'数分不错',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(68,2,17,4,'2026-11-20 14:00:00','DA 实训室 B202',2,82.00,'可视化可以',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(69,2,17,11,'2026-11-20 15:00:00','NS 实训室 D401',2,76.00,'安全方向一般',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(70,2,18,2,'2026-11-20 13:30:00','SD 实训室 A302',2,83.00,'前端不错',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(71,2,18,4,'2026-11-20 14:30:00','DA 实训室 B202',2,80.00,'可视化',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(72,2,18,9,'2026-11-20 15:30:00','UI 实训室 C101',2,72.00,'产品一般',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(73,2,19,11,'2026-11-20 09:00:00','NS 实训室 D401',2,80.00,'安全方向不错',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(74,2,19,12,'2026-11-20 10:00:00','NS 实训室 D402',2,78.00,'网络方向',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(75,2,19,3,'2026-11-20 11:00:00','DA 实训室 B201',2,70.00,'数分一般',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(76,2,20,7,'2026-11-20 09:30:00','RB 实训室 D201',2,76.00,'嵌入式一般',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(77,2,20,8,'2026-11-20 10:30:00','RB 实训室 D202',2,75.00,'机器人',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(78,2,20,6,'2026-11-20 11:30:00','AI 实训室 B302',2,65.00,'AI 较弱',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(79,3,15,3,'2026-11-25 09:00:00','DA 实训室 B201',2,62.00,'还是一般',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(80,3,15,4,'2026-11-25 10:00:00','DA 实训室 B202',2,60.00,'一般',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(81,3,15,11,'2026-11-25 11:00:00','NS 实训室 D401',2,58.00,'较弱',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(82,3,16,11,'2026-11-25 09:30:00','NS 实训室 D401',2,55.00,'差',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(83,3,16,12,'2026-11-25 10:30:00','NS 实训室 D402',2,54.00,'很差',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(84,3,16,3,'2026-11-25 11:30:00','DA 实训室 B201',2,52.00,'非常弱',NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0);
/*!40000 ALTER TABLE `interview` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `match_result`
--

DROP TABLE IF EXISTS `match_result`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `match_result` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `student_id` bigint unsigned NOT NULL COMMENT '关联 student.id',
  `teacher_id` bigint unsigned NOT NULL COMMENT '最终归属的老师',
  `final_round_id` bigint unsigned NOT NULL COMMENT '在哪一轮确定的（含分流/统筹轮次）',
  `match_source` tinyint NOT NULL COMMENT '来源：1=NORMAL 正常匹配 2=TEACHER_DIVERT 调剂老师选 3=SYSTEM_DIVERT 系统统筹 4=MANUAL_SETTLE 待选池人工安置',
  `match_status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1=MATCHED 已匹配 0=CANCELLED 已取消',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_student` (`student_id`,`deleted`),
  KEY `idx_teacher_status` (`teacher_id`,`match_status`),
  KEY `fk_match_round` (`final_round_id`),
  CONSTRAINT `fk_match_round` FOREIGN KEY (`final_round_id`) REFERENCES `selection_round` (`id`),
  CONSTRAINT `fk_match_student` FOREIGN KEY (`student_id`) REFERENCES `student` (`id`),
  CONSTRAINT `fk_match_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `teacher` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='最终匹配结果表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `match_result`
--

LOCK TABLES `match_result` WRITE;
/*!40000 ALTER TABLE `match_result` DISABLE KEYS */;
INSERT INTO `match_result` VALUES (1,1,1,1,1,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(2,2,2,1,1,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(3,3,3,1,1,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(4,4,5,1,1,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(5,5,7,1,1,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(6,6,5,1,1,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(7,7,1,1,2,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(8,8,11,1,2,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(9,10,2,1,3,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(10,9,3,2,1,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(11,11,9,2,1,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(12,12,11,2,1,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(13,13,1,1,3,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(14,14,1,2,1,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(15,17,3,2,1,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(16,18,4,2,3,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(17,19,11,2,1,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(18,20,11,2,2,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(19,15,6,3,4,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0);
/*!40000 ALTER TABLE `match_result` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `selection_round`
--

DROP TABLE IF EXISTS `selection_round`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `selection_round` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `round_no` tinyint NOT NULL COMMENT '轮次编号：1=第一轮 2=第二轮补录 3=第三轮补录',
  `round_type` tinyint NOT NULL COMMENT '轮次类型：1=初选(INITIAL) 2=补录(SUPPLEMENT)',
  `name` varchar(64) NOT NULL COMMENT '轮次名称，如 第一轮初选、第二轮补录',
  `choice_start_at` datetime DEFAULT NULL COMMENT '志愿填报开始时间',
  `choice_end_at` datetime DEFAULT NULL COMMENT '志愿填报截止时间',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0=未开始 1=进行中 2=已结束',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_round_no` (`round_no`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='录取轮次表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `selection_round`
--

LOCK TABLES `selection_round` WRITE;
/*!40000 ALTER TABLE `selection_round` DISABLE KEYS */;
INSERT INTO `selection_round` VALUES (1,1,1,'第一轮初选',NULL,NULL,0,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(2,2,2,'第二轮补录',NULL,NULL,0,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(3,3,2,'第三轮补录',NULL,NULL,0,'2026-11-13 15:50:29','2026-11-13 15:50:29',0);
/*!40000 ALTER TABLE `selection_round` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `student`
--

DROP TABLE IF EXISTS `student`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `student` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint unsigned NOT NULL COMMENT '关联 sys_user.id',
  `student_no` varchar(32) NOT NULL COMMENT '学号',
  `major` varchar(64) DEFAULT NULL COMMENT '专业',
  `grade` varchar(16) DEFAULT NULL COMMENT '年级',
  `class_name` varchar(64) DEFAULT NULL COMMENT '班级',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  UNIQUE KEY `uk_student_no` (`student_no`),
  CONSTRAINT `fk_student_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='学生表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `student`
--

LOCK TABLES `student` WRITE;
/*!40000 ALTER TABLE `student` DISABLE KEYS */;
INSERT INTO `student` VALUES (1,201,'20260001','软件工程','2024','软工1班','2026-11-13 15:50:29','2026-11-13 15:50:29',0),(2,202,'20260002','软件工程','2024','软工1班','2026-11-13 15:50:29','2026-11-13 15:50:29',0),(3,203,'20260003','数据科学','2024','数科1班','2026-11-13 15:50:29','2026-11-13 15:50:29',0),(4,204,'20260004','人工智能','2024','AI1班','2026-11-13 15:50:29','2026-11-13 15:50:29',0),(5,205,'20260005','人工智能','2024','AI1班','2026-11-13 15:50:29','2026-11-13 15:50:29',0),(6,206,'20260006','机械工程','2024','机电1班','2026-11-13 15:50:29','2026-11-13 15:50:29',0),(7,207,'20260007','数据科学','2024','数科1班','2026-11-13 15:50:29','2026-11-13 15:50:29',0),(8,208,'20260008','网络工程','2024','网工1班','2026-11-13 15:50:29','2026-11-13 15:50:29',0),(9,209,'20260009','软件工程','2024','软工1班','2026-11-13 15:50:29','2026-11-13 15:50:29',0),(10,210,'20260010','人工智能','2024','AI1班','2026-11-13 15:50:29','2026-11-13 15:50:29',0),(11,211,'20260011','产品设计','2024','设艺1班','2026-11-13 15:50:29','2026-11-13 15:50:29',0),(12,212,'20260012','网络工程','2024','网工1班','2026-11-13 15:50:29','2026-11-13 15:50:29',0),(13,213,'20260013','数据科学','2024','数科1班','2026-11-13 15:50:29','2026-11-13 15:50:29',0),(14,214,'20260014','软件工程','2024','软工1班','2026-11-13 15:50:29','2026-11-13 15:50:29',0),(15,215,'20260015','人工智能','2024','AI1班','2026-11-13 15:50:29','2026-11-13 15:50:29',0),(16,216,'20260016','机械工程','2024','机电1班','2026-11-13 15:50:29','2026-11-13 15:50:29',0),(17,217,'20260017','软件工程','2024','软工1班','2026-11-13 15:50:29','2026-11-13 15:50:29',0),(18,218,'20260018','数据科学','2024','数科1班','2026-11-13 15:50:29','2026-11-13 15:50:29',0),(19,219,'20260019','网络工程','2024','网工1班','2026-11-13 15:50:29','2026-11-13 15:50:29',0),(20,220,'20260020','人工智能','2024','AI1班','2026-11-13 15:50:29','2026-11-13 15:50:29',0);
/*!40000 ALTER TABLE `student` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `student_choice`
--

DROP TABLE IF EXISTS `student_choice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `student_choice` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `round_id` bigint unsigned NOT NULL COMMENT '关联 selection_round.id',
  `student_id` bigint unsigned NOT NULL COMMENT '关联 student.id',
  `teacher_id` bigint unsigned NOT NULL COMMENT '关联 teacher.id',
  `choice_rank` tinyint NOT NULL COMMENT '志愿顺序 1~3（仅展示用，业务上平行志愿）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_round_student_teacher` (`round_id`,`student_id`,`teacher_id`),
  KEY `idx_round_teacher` (`round_id`,`teacher_id`),
  KEY `idx_student` (`student_id`),
  KEY `fk_choice_teacher` (`teacher_id`),
  CONSTRAINT `fk_choice_round` FOREIGN KEY (`round_id`) REFERENCES `selection_round` (`id`),
  CONSTRAINT `fk_choice_student` FOREIGN KEY (`student_id`) REFERENCES `student` (`id`),
  CONSTRAINT `fk_choice_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `teacher` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=79 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='学生志愿表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `student_choice`
--

LOCK TABLES `student_choice` WRITE;
/*!40000 ALTER TABLE `student_choice` DISABLE KEYS */;
INSERT INTO `student_choice` VALUES (1,1,1,1,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(2,1,1,2,2,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(3,1,1,3,3,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(4,1,2,2,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(5,1,2,4,2,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(6,1,2,9,3,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(7,1,3,3,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(8,1,3,4,2,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(9,1,3,11,3,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(10,1,4,5,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(11,1,4,6,2,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(12,1,4,1,3,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(13,1,5,7,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(14,1,5,8,2,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(15,1,5,5,3,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(16,1,6,5,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(17,1,6,7,2,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(18,1,6,3,3,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(19,1,7,5,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(20,1,7,6,2,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(21,1,7,1,3,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(22,1,8,5,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(23,1,8,6,2,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(24,1,8,11,3,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(25,1,9,7,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(26,1,9,8,2,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(27,1,9,4,3,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(28,1,10,5,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(29,1,10,7,2,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(30,1,10,12,3,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(31,1,11,9,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(32,1,11,10,2,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(33,1,11,1,3,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(34,1,12,11,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(35,1,12,12,2,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(36,1,12,2,3,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(37,1,13,5,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(38,1,13,6,2,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(39,1,13,3,3,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(40,1,14,7,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(41,1,14,8,2,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(42,1,14,2,3,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(43,2,11,9,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(44,2,11,10,2,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(45,2,11,5,3,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(46,2,12,11,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(47,2,12,12,2,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(48,2,12,3,3,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(49,2,9,3,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(50,2,9,4,2,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(51,2,9,1,3,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(52,2,14,1,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(53,2,14,2,2,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(54,2,14,9,3,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(55,2,15,1,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(56,2,15,4,2,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(57,2,15,11,3,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(58,2,16,12,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(59,2,16,10,2,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(60,2,16,6,3,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(61,2,17,3,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(62,2,17,4,2,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(63,2,17,11,3,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(64,2,18,2,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(65,2,18,4,2,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(66,2,18,9,3,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(67,2,19,11,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(68,2,19,12,2,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(69,2,19,3,3,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(70,2,20,7,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(71,2,20,8,2,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(72,2,20,6,3,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(73,3,15,3,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(74,3,15,4,2,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(75,3,15,11,3,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(76,3,16,11,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(77,3,16,12,2,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(78,3,16,3,3,'2026-11-13 15:50:29','2026-11-13 15:50:29',0);
/*!40000 ALTER TABLE `student_choice` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `student_final_choice`
--

DROP TABLE IF EXISTS `student_final_choice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `student_final_choice` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `round_id` bigint unsigned NOT NULL COMMENT '关联 selection_round.id',
  `student_id` bigint unsigned NOT NULL COMMENT '关联 student.id',
  `teacher_id` bigint unsigned NOT NULL COMMENT '最终选择的老师',
  `timeout` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否超时自动选择：0=学生主动 1=超时自动',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_round_student` (`round_id`,`student_id`),
  KEY `fk_final_student` (`student_id`),
  KEY `fk_final_teacher` (`teacher_id`),
  CONSTRAINT `fk_final_round` FOREIGN KEY (`round_id`) REFERENCES `selection_round` (`id`),
  CONSTRAINT `fk_final_student` FOREIGN KEY (`student_id`) REFERENCES `student` (`id`),
  CONSTRAINT `fk_final_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `teacher` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='学生终选表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `student_final_choice`
--

LOCK TABLES `student_final_choice` WRITE;
/*!40000 ALTER TABLE `student_final_choice` DISABLE KEYS */;
INSERT INTO `student_final_choice` VALUES (1,1,4,5,0,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(2,1,5,7,0,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(3,1,6,5,0,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(4,2,17,3,0,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(5,2,19,11,0,'2026-11-13 15:50:29','2026-11-13 15:50:29',0);
/*!40000 ALTER TABLE `student_final_choice` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_user`
--

DROP TABLE IF EXISTS `sys_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` varchar(64) NOT NULL COMMENT '登录名（学号/工号/管理员名）',
  `password` varchar(255) NOT NULL COMMENT '密码（BCrypt 加密）',
  `real_name` varchar(64) NOT NULL COMMENT '真实姓名',
  `role` tinyint NOT NULL COMMENT '角色：1=学生 2=老师 3=管理员',
  `phone` varchar(32) DEFAULT NULL COMMENT '手机号',
  `email` varchar(128) DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(512) DEFAULT NULL COMMENT '头像 URL',
  `enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用：1=启用 0=禁用',
  `last_login_at` datetime DEFAULT NULL COMMENT '最后登录时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '软删除：0=未删除 1=已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_role` (`role`)
) ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user`
--

LOCK TABLES `sys_user` WRITE;
/*!40000 ALTER TABLE `sys_user` DISABLE KEYS */;
INSERT INTO `sys_user` VALUES (101,'T-SD01','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','林工',2,'13800000101','lin.gong@demo.edu',NULL,1,NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(102,'T-SD02','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','赵前端',2,'13800000102','zhao.fe@demo.edu',NULL,1,NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(103,'T-DA01','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','钱数分',2,'13800000103','qian.da@demo.edu',NULL,1,NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(104,'T-DA02','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','孙可视化',2,'13800000104','sun.viz@demo.edu',NULL,1,NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(105,'T-AI01','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','李大模型',2,'13800000105','li.llm@demo.edu',NULL,1,NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(106,'T-AI02','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','王微调',2,'13800000106','王.ft@demo.edu',NULL,1,NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(107,'T-RB01','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','陈嵌入式',2,'13800000107','chen.emb@demo.edu',NULL,1,NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(108,'T-RB02','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','周机器人',2,'13800000108','zhou.rb@demo.edu',NULL,1,NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(109,'T-UI01','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','吴产品',2,'13800000109','wu.pm@demo.edu',NULL,1,NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(110,'T-UI02','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','郑交互',2,'13800000110','zheng.ix@demo.edu',NULL,1,NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(111,'T-NS01','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','冯安全',2,'13800000111','feng.sec@demo.edu',NULL,1,NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(112,'T-NS02','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','褚网络',2,'13800000112','chu.net@demo.edu',NULL,1,NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(201,'S-A01','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','张一',1,'13900000201','zhangyi@demo.edu',NULL,1,NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(202,'S-A02','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','王二',1,'13900000202','wanger@demo.edu',NULL,1,NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(203,'S-A03','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','李三',1,'13900000203','lisan@demo.edu',NULL,1,NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(204,'S-B01','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','赵四',1,'13900000204','zhaosi@demo.edu',NULL,1,NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(205,'S-B02','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','孙五',1,'13900000205','sunwu@demo.edu',NULL,1,NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(206,'S-B03','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','周六',1,'13900000206','zhouliu@demo.edu',NULL,1,NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(207,'S-C01','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','吴七',1,'13900000207','wuqi@demo.edu',NULL,1,NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(208,'S-C02','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','郑八',1,'13900000208','zhengba@demo.edu',NULL,1,NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(209,'S-D01','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','冯九',1,'13900000209','fengjiu@demo.edu',NULL,1,NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(210,'S-D02','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','褚十',1,'13900000210','chushi@demo.edu',NULL,1,NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(211,'S-E01','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','卫晓',1,'13900000211','weixiao@demo.edu',NULL,1,NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(212,'S-E02','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','蒋晓',1,'13900000212','jiangxiao@demo.edu',NULL,1,NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(213,'S-F01','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','沈琳',1,'13900000213','shenlin@demo.edu',NULL,1,NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(214,'S-F02','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','韩雪',1,'13900000214','hanxue@demo.edu',NULL,1,NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(215,'S-G01','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','杨阳',1,'13900000215','yangyang@demo.edu',NULL,1,NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(216,'S-G02','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','朱珠',1,'13900000216','zhuzhu@demo.edu',NULL,1,NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(217,'S-H01','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','秦华',1,'13900000217','qinhua@demo.edu',NULL,1,NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(218,'S-H02','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','尤优',1,'13900000218','youyou@demo.edu',NULL,1,NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(219,'S-I01','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','许安',1,'13900000219','xuan@demo.edu',NULL,1,NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(220,'S-I02','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','何平',1,'13900000220','heping@demo.edu',NULL,1,NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(999,'admin','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','超级管理员',3,'13800000000','admin@demo.edu',NULL,1,NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0);
/*!40000 ALTER TABLE `sys_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `system_config`
--

DROP TABLE IF EXISTS `system_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `system_config` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `config_key` varchar(64) NOT NULL COMMENT '配置键',
  `config_value` varchar(512) NOT NULL COMMENT '配置值',
  `config_desc` varchar(255) DEFAULT NULL COMMENT '说明',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `system_config`
--

LOCK TABLES `system_config` WRITE;
/*!40000 ALTER TABLE `system_config` DISABLE KEYS */;
INSERT INTO `system_config` VALUES (1,'max_supplement_rounds','2','补录轮数（不含第一轮）','2026-11-13 15:50:29','2026-11-13 15:50:29',0),(2,'max_choices_per_round','3','每轮最多可选老师数','2026-11-13 15:50:29','2026-11-13 15:50:29',0),(3,'teacher_default_quota','16','老师默认名额上限','2026-11-13 15:50:29','2026-11-13 15:50:29',0),(4,'final_choice_timeout_min','60','终选超时分钟','2026-11-13 15:50:29','2026-11-13 15:50:29',0),(5,'choice_modify_enabled','true','志愿关闭前是否允许修改','2026-11-13 15:50:29','2026-11-13 15:50:29',0);
/*!40000 ALTER TABLE `system_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `teacher`
--

DROP TABLE IF EXISTS `teacher`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `teacher` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint unsigned NOT NULL COMMENT '关联 sys_user.id',
  `training_room_id` bigint unsigned NOT NULL COMMENT '所属实训室',
  `teacher_no` varchar(32) NOT NULL COMMENT '工号',
  `title` varchar(32) DEFAULT NULL COMMENT '职称',
  `research_direction` varchar(255) DEFAULT NULL COMMENT '研究方向',
  `bio` text COMMENT '个人简介',
  `quota` int unsigned NOT NULL DEFAULT '16' COMMENT '名额上限（约 16，略有差异）',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1=活跃 0=不参与',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  UNIQUE KEY `uk_teacher_no` (`teacher_no`),
  KEY `idx_training_room` (`training_room_id`),
  CONSTRAINT `fk_teacher_room` FOREIGN KEY (`training_room_id`) REFERENCES `training_room` (`id`),
  CONSTRAINT `fk_teacher_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='老师表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `teacher`
--

LOCK TABLES `teacher` WRITE;
/*!40000 ALTER TABLE `teacher` DISABLE KEYS */;
INSERT INTO `teacher` VALUES (1,101,1,'TSD01','副教授','Java 后端与微服务','10 年企业级开发经验，专注后端架构',15,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(2,102,1,'TSD02','讲师','React / Vue 前端','前端框架专家，热爱开源',16,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(3,103,2,'TDA01','教授','大数据与数据仓库','数据湖架构设计专家',15,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(4,104,2,'TDA02','副教授','BI 与可视化大屏','ECharts / D3.js 深度使用者',16,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(5,105,3,'TAI01','教授','大模型训练与部署','曾就职 OpenAI，方向 LLM 推理优化',3,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(6,106,3,'TAI02','副教授','大模型微调与 RAG','LoRA / QLoRA / RAG Pipeline',3,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(7,107,4,'TRB01','副教授','STM32 / RTOS','嵌入式底层驱动开发',2,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(8,108,4,'TRB02','讲师','ROS 机器人操作系统','移动机器人导航与 SLAM',2,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(9,109,5,'TUI01','教授','产品需求与用户研究','擅长产品文档撰写与用户调研',15,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(10,110,5,'TUI02','讲师','Figma / 交互设计','高级 UI/UX 设计师',16,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(11,111,6,'TNS01','副教授','Web 安全与渗透测试','资深白帽黑客，CTF 出题人',15,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(12,112,6,'TNS02','讲师','云原生网络','Kubernetes 网络与 Service Mesh',16,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0);
/*!40000 ALTER TABLE `teacher` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `teacher_pick`
--

DROP TABLE IF EXISTS `teacher_pick`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `teacher_pick` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `round_id` bigint unsigned NOT NULL COMMENT '关联 selection_round.id',
  `teacher_id` bigint unsigned NOT NULL COMMENT '选人的老师',
  `student_id` bigint unsigned NOT NULL COMMENT '被选的学生',
  `pick_source` tinyint NOT NULL DEFAULT '1' COMMENT '来源：1=学生主动填报该老师 2=调剂范围内选的学生',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_round_teacher_student_source` (`round_id`,`teacher_id`,`student_id`,`pick_source`),
  KEY `idx_round_student` (`round_id`,`student_id`),
  KEY `fk_pick_teacher` (`teacher_id`),
  KEY `fk_pick_student` (`student_id`),
  CONSTRAINT `fk_pick_round` FOREIGN KEY (`round_id`) REFERENCES `selection_round` (`id`),
  CONSTRAINT `fk_pick_student` FOREIGN KEY (`student_id`) REFERENCES `student` (`id`),
  CONSTRAINT `fk_pick_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `teacher` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='老师选学生表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `teacher_pick`
--

LOCK TABLES `teacher_pick` WRITE;
/*!40000 ALTER TABLE `teacher_pick` DISABLE KEYS */;
INSERT INTO `teacher_pick` VALUES (1,1,1,1,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(2,1,2,2,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(3,1,3,3,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(4,1,5,4,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(5,1,5,6,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(6,1,6,4,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(7,1,6,5,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(8,1,7,5,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(9,1,7,6,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(10,1,8,5,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(11,2,9,11,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(12,2,11,12,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(13,2,3,9,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(14,2,1,14,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(15,2,3,17,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(16,2,4,17,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(17,2,11,19,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(18,2,12,19,1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0);
/*!40000 ALTER TABLE `teacher_pick` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `teacher_quota_log`
--

DROP TABLE IF EXISTS `teacher_quota_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `teacher_quota_log` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `teacher_id` bigint unsigned NOT NULL COMMENT '老师',
  `quota_before` int unsigned DEFAULT NULL COMMENT '变动前名额',
  `quota_after` int unsigned NOT NULL COMMENT '变动后名额',
  `reason` varchar(128) DEFAULT NULL COMMENT '变动原因：ADMIN_MODIFY / SYSTERM_DIVERT 等',
  `operator_id` bigint unsigned DEFAULT NULL COMMENT '操作人（管理员用户 ID）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_teacher` (`teacher_id`),
  CONSTRAINT `fk_quotalog_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `teacher` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='老师名额变动日志';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `teacher_quota_log`
--

LOCK TABLES `teacher_quota_log` WRITE;
/*!40000 ALTER TABLE `teacher_quota_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `teacher_quota_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `training_room`
--

DROP TABLE IF EXISTS `training_room`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `training_room` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(128) NOT NULL COMMENT '实训室名称',
  `code` varchar(32) NOT NULL COMMENT '实训室编号',
  `description` text COMMENT '实训室简介',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1=启用 0=停用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='实训室表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `training_room`
--

LOCK TABLES `training_room` WRITE;
/*!40000 ALTER TABLE `training_room` DISABLE KEYS */;
INSERT INTO `training_room` VALUES (1,'智能软件开发实训室','ROOM-SD','聚焦 AI 应用开发与工程实践',1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(2,'数据分析与可视化实训室','ROOM-DA','数据采集、挖掘与可视化方向',1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(3,'人工智能模型实训室','ROOM-AI','大模型训练、微调与部署方向',1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(4,'机器人与嵌入式实训室','ROOM-RB','智能硬件与嵌入式开发方向',1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(5,'产品与交互设计实训室','ROOM-UI','产品设计、交互与前端方向',1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0),(6,'网络与安全实训室','ROOM-NS','网络架构、信息安全方向',1,'2026-11-13 15:50:29','2026-11-13 15:50:29',0);
/*!40000 ALTER TABLE `training_room` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wait_pool`
--

DROP TABLE IF EXISTS `wait_pool`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wait_pool` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `student_id` bigint unsigned NOT NULL COMMENT '关联 student.id',
  `enter_round` bigint unsigned NOT NULL COMMENT '从哪一轮分流后进入待选池',
  `enter_at` datetime NOT NULL COMMENT '进入时间',
  `interview_at` datetime DEFAULT NULL COMMENT '约谈时间',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0=PENDING 待约谈 1=INTERVIEWED 已约谈 2=SETTLED 已安置',
  `settle_teacher_id` bigint unsigned DEFAULT NULL COMMENT '人工安置的老师',
  `settle_by` bigint unsigned DEFAULT NULL COMMENT '操作的管理员用户 ID',
  `settle_remark` varchar(255) DEFAULT NULL COMMENT '安置备注',
  `settle_at` datetime DEFAULT NULL COMMENT '安置时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_student` (`student_id`),
  KEY `idx_status` (`status`),
  KEY `fk_wait_round` (`enter_round`),
  CONSTRAINT `fk_wait_round` FOREIGN KEY (`enter_round`) REFERENCES `selection_round` (`id`),
  CONSTRAINT `fk_wait_student` FOREIGN KEY (`student_id`) REFERENCES `student` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='待选池表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wait_pool`
--

LOCK TABLES `wait_pool` WRITE;
/*!40000 ALTER TABLE `wait_pool` DISABLE KEYS */;
INSERT INTO `wait_pool` VALUES (1,15,3,'2026-11-26 00:00:00','2026-11-27 10:00:00',2,6,999,'约谈后学生同意去 AI 方向继续学习','2026-11-27 14:00:00','2026-11-13 15:50:29','2026-11-13 15:50:29',0),(2,16,3,'2026-11-26 00:00:00',NULL,0,NULL,NULL,NULL,NULL,'2026-11-13 15:50:29','2026-11-13 15:50:29',0);
/*!40000 ALTER TABLE `wait_pool` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'leb-sele'
--

--
-- Dumping routines for database 'leb-sele'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-11-13 16:02:57
