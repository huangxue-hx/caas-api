/*
Navicat MySQL Data Transfer

Source Server         : 10.10.124.48
Source Server Version : 50635
Source Host           : 10.10.124.48:30306
Source Database       : k8s_server_mysql

Target Server Type    : MYSQL
Target Server Version : 50635
File Encoding         : 65001

Date: 2018-01-26 10:56:21
*/

DROP DATABASE IF EXISTS `k8s_server_mysql`;
CREATE DATABASE `k8s_server_mysql`;
use k8s_server_mysql;

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for alarm_receiver
-- ----------------------------
DROP TABLE IF EXISTS `alarm_receiver`;
CREATE TABLE `alarm_receiver` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `alarm_type` varchar(45) NOT NULL,
  `monitor_id` int(11) NOT NULL,
  `user_id` int(11) DEFAULT NULL,
  `user_name` varchar(45) DEFAULT NULL,
  `user_real_name` varchar(45) NOT NULL,
  `user_email` varchar(45) DEFAULT NULL,
  `user_mobile` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uni_alarm_user` (`alarm_type`, `monitor_id`, `user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for alarm_record
-- ----------------------------
DROP TABLE IF EXISTS `alarm_record`;
CREATE TABLE `alarm_record` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `alarm_type` varchar(45) NOT NULL COMMENT '报警类型， 应用日志报警，组件日志报警，租户资源使用率报警等',
  `alarm_type_name` varchar(45) NOT NULL COMMENT '报警类型名称',
  `cluster` varchar(45) DEFAULT NULL,
  `tenant_name` varchar(45) DEFAULT NULL,
  `namespace` varchar(45) DEFAULT NULL,
  `service` varchar(45) DEFAULT NULL,
  `container` varchar(45) DEFAULT NULL,
  `node` varchar(30) DEFAULT NULL,
  `target` varchar(45) DEFAULT NULL,
  `usage_threshold` varchar(45) DEFAULT NULL,
  `times_threshold` varchar(64) DEFAULT NULL,
  `result_times` int(11) unsigned DEFAULT NULL,
  `rates` varchar(100) DEFAULT NULL,
  `status` tinyint(2) NOT NULL COMMENT '0 - 新建，1-已通知，2-已查看，3-已处理',
  `remark` varchar(500) DEFAULT NULL,
  `reason` varchar(300) DEFAULT NULL,
  `reason_en` varchar(500) DEFAULT NULL,
  `receiver_names` varchar(200) DEFAULT NULL,
  `receiver_usernames` varchar(100) DEFAULT NULL COMMENT '告警接收人用户名',
  `mail_record_id` int(11) DEFAULT NULL,
  `monitor_id` int(11) NOT NULL,
  `created_time` datetime NOT NULL COMMENT '创建时间',
  `handle_time` datetime DEFAULT NULL,
  `handle_user` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_time` (`created_time`),
  KEY `idx_node` (`alarm_type`,`node`),
  KEY `idx_container` (`alarm_type`,`namespace`,`container`),
  KEY `idx_monitor` (`monitor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='报警附属信息';

-- ----------------------------
-- Table structure for app_config
-- ----------------------------
DROP TABLE IF EXISTS `app_config`;
CREATE TABLE `app_config` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `value` varchar(200) NOT NULL,
  `grouping` varchar(45) NOT NULL DEFAULT 'default' COMMENT '配置分组',
  `comment` varchar(100) DEFAULT NULL,
  `is_deleted` tinyint(1) NOT NULL,
  `created_user` varchar(45) NOT NULL,
  `modified_user` varchar(45) NOT NULL,
  `created_time` datetime NOT NULL,
  `modified_time` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uni_name` (`grouping`,`name`,`is_deleted`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8 COMMENT='应用相关配置';

-- ----------------------------
-- Records of app_config
-- ----------------------------
INSERT INTO `app_config` VALUES ('1', 'smtp', '', 'alert.mail', null, '0', 'admin', 'admin', '2018-01-11 08:38:28', '2018-01-11 08:38:28');
INSERT INTO `app_config` VALUES ('2', 'port', '', 'alert.mail', null, '0', 'admin', 'admin', '2018-01-11 08:38:28', '2018-01-11 08:38:28');
INSERT INTO `app_config` VALUES ('3', 'user', '', 'alert.mail', null, '0', 'admin', 'admin', '2018-01-11 08:38:28', '2018-01-11 08:38:28');
INSERT INTO `app_config` VALUES ('4', 'password', '', 'alert.mail', null, '0', 'admin', 'admin', '2018-01-11 08:38:28', '2018-01-11 08:38:28');
INSERT INTO `app_config` VALUES ('5', 'from', '', 'alert.mail', null, '0', 'admin', 'admin', '2018-01-11 08:38:28', '2018-01-11 08:38:28');
INSERT INTO `app_config` VALUES ('6', 'log.keep.days', '30', 'default', null, '0', 'admin', 'admin', '2018-01-11 08:38:28', '2018-01-11 08:38:28');
INSERT INTO `app_config` VALUES ('7', 'alarm.keep.days', '180', 'default', null, '0', 'admin', 'admin', '2018-01-11 08:38:28', '2018-01-11 08:38:28');
INSERT INTO `app_config` VALUES ('8', 'module.monitor.duration', '300', 'default', null, '0', 'admin', 'admin', '2018-01-11 08:38:28', '2018-01-11 08:38:28');

-- ----------------------------
-- Table structure for app_log_monitor
-- ----------------------------
DROP TABLE IF EXISTS `app_log_monitor`;
CREATE TABLE `app_log_monitor` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `alarm_name` varchar(100) DEFAULT NULL,
  `tenant_id` varchar(48) NOT NULL,
  `tenant_name` varchar(100) NOT NULL,
  `tenant_alias_name` varchar(100) NOT NULL,
  `project_id` varchar(64) DEFAULT NULL,
  `project_name` varchar(64) DEFAULT NULL,
  `cluster_id` varchar(64) DEFAULT NULL,
  `cluster_name` varchar(64) DEFAULT NULL,
  `namespace` varchar(48) NOT NULL COMMENT '分区名',
  `service` varchar(45) DEFAULT NULL,
  `container` varchar(48) NOT NULL COMMENT '应用容器名',
  `keyword` varchar(1000) NOT NULL COMMENT '日志中关键字及对应的告警阈值',
  `alert_interval_time` smallint(4) unsigned NOT NULL COMMENT '告警间隔时间',
  `send_alert_time` datetime DEFAULT NULL COMMENT '告警时间',
  `to_email` varchar(100) DEFAULT NULL COMMENT '告警接收邮箱地址',
  `receiver_names` varchar(200) DEFAULT NULL,
  `created_time` datetime NOT NULL COMMENT '创建时间',
  `modified_time` datetime NOT NULL COMMENT '修改时间',
  `created_user` varchar(48) DEFAULT NULL COMMENT '创建人',
  `modified_user` varchar(48) DEFAULT NULL COMMENT '修改人',
  `status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '0-启用，1-停用',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uni_log_monitor` (`service`, `namespace`, `container`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for cluster_monitor
-- ----------------------------
DROP TABLE IF EXISTS `cluster_monitor`;
CREATE TABLE `cluster_monitor` (
  `type` varchar(32) NOT NULL,
  `related_monitor_id` int(11) NOT NULL COMMENT '对应集群资源监控id，状态和重启告警在集群资源告警里设置',
  `is_open` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否开启告警 0-关闭，1-开启',
  `receiver_names` varchar(45) NOT NULL COMMENT '告警接收人',
  `alert_interval_time` smallint(4) NOT NULL DEFAULT '10' COMMENT '告警间隔',
  `send_alert_time` timestamp NULL DEFAULT NULL COMMENT '最后一次告警时间',
  `created_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modified_time` timestamp NOT NULL,
  `created_user` varchar(32) DEFAULT NULL,
  `modified_user` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for mail_record
-- ----------------------------
DROP TABLE IF EXISTS `mail_record`;
CREATE TABLE `mail_record` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `biz_type` varchar(45) NOT NULL COMMENT '邮件类型',
  `subject` varchar(100) NOT NULL COMMENT '邮件标题',
  `content` text COMMENT '邮件内容',
  `to_email` varchar(100) NOT NULL COMMENT '报警接收邮箱',
  `cc_email` varchar(100) DEFAULT NULL COMMENT '报警抄送邮箱',
  `status` tinyint(2) NOT NULL COMMENT '0 - 新建， 1-发送成功， 2-发送失败',
  `send_time` datetime DEFAULT NULL COMMENT '发送邮件时间',
  `created_time` datetime NOT NULL COMMENT '创建时间',
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_create_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='邮件报警记录';

-- ----------------------------
-- Table structure for message
-- ----------------------------
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message` (
  `id` int(255) NOT NULL AUTO_INCREMENT,
  `app_key` varchar(255) NOT NULL,
  `server_url` varchar(255) NOT NULL,
  `app_secret` varchar(255) NOT NULL,
  `nonce` varchar(255) NOT NULL,
  `templateid` varchar(255) NOT NULL,
  `annotation` varchar(255) DEFAULT NULL COMMENT '短信接口供应商',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for message_record
-- ----------------------------
DROP TABLE IF EXISTS `message_record`;
CREATE TABLE `message_record` (
  `id` int(255) NOT NULL AUTO_INCREMENT,
  `mobiles` varchar(255) NOT NULL,
  `params` varchar(255) DEFAULT NULL COMMENT '参数',
  `status` int(255) DEFAULT '0' COMMENT '发送状态 0未发送 1 发送成功 2 发送失败',
  `create_time` timestamp NOT NULL ON UPDATE CURRENT_TIMESTAMP,
  `send_time` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` varchar(255) DEFAULT NULL COMMENT '保留字段',
  `messageid` int(255) NOT NULL COMMENT '消息模板id',
  PRIMARY KEY (`id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for resource_monitor
-- ----------------------------
DROP TABLE IF EXISTS `resource_monitor`;
CREATE TABLE `resource_monitor` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `alarm_name` varchar(45) DEFAULT NULL,
  `type` varchar(45) NOT NULL COMMENT '资源监控类型， 0-容器资源监控，1-集群组件监控，2-主机监控',
  `tenant_id` varchar(48) NOT NULL COMMENT '租户',
  `tenant_name` varchar(45) DEFAULT NULL,
  `tenant_alias_name` varchar(100) NOT NULL,
  `project_id` varchar(64) DEFAULT NULL,
  `project_name` varchar(64) DEFAULT NULL,
  `cluster_id` varchar(64) DEFAULT NULL,
  `cluster_name` varchar(64) DEFAULT NULL,
  `namespace` varchar(48) NOT NULL COMMENT '分区名',
  `service` varchar(45) DEFAULT NULL,
  `container` varchar(45) NOT NULL,
  `cpu_usage_threshold` tinyint(2) unsigned DEFAULT NULL COMMENT 'cpu使用率阈值',
  `mem_usage_threshold` tinyint(2) unsigned DEFAULT NULL COMMENT 'memory使用率阈值',
  `disk_usage_threshold` tinyint(2) DEFAULT NULL,
  `alarm_flag` tinyint(1) DEFAULT '0',
  `cpu_flag` tinyint(1) NOT NULL DEFAULT '0',
  `mem_flag` tinyint(1) NOT NULL DEFAULT '0',
  `disk_flag` tinyint(1) NOT NULL DEFAULT '0',
  `cpu_unit_minute_time` smallint(4) DEFAULT NULL COMMENT 'cpu告警阈值监控单位时间（分钟）',
  `cpu_threshold_times` smallint(4) DEFAULT NULL COMMENT 'cpu告警阈值，出现次数',
  `mem_unit_minute_time` smallint(4) DEFAULT NULL COMMENT '内存告警阈值监控单位时间（分钟）',
  `mem_threshold_times` smallint(4) DEFAULT NULL COMMENT '内存告警阈值，单位时间出现次数',
  `disk_unit_minute_time` smallint(4) DEFAULT NULL COMMENT '磁盘告警阈值监控单位时间（分钟）',
  `disk_threshold_times` smallint(4) DEFAULT NULL COMMENT '磁盘告警阈值，单位时间出现次数',
  `alert_interval_time` smallint(4) unsigned DEFAULT NULL COMMENT '告警间隔时间',
  `send_alert_time` datetime DEFAULT NULL COMMENT '最后一次告警时间',
  `receiver_names` varchar(200) DEFAULT NULL,
  `created_time` datetime NOT NULL COMMENT '创建时间',
  `modified_time` datetime NOT NULL COMMENT '修改时间',
  `created_user` varchar(48) DEFAULT NULL COMMENT '创建人',
  `modified_user` varchar(48) DEFAULT NULL COMMENT '修改人',
  `status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '0开启，1停止',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uni_resource_monitor` (`type`,`tenant_id`,`namespace`,`service`,`container`) USING BTREE,
  KEY `idx_time` (`modified_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='租户cpu，mem资源使用率超过阈值告警配置';

-- ----------------------------
-- Table structure for resource_usage_rate
-- ----------------------------
DROP TABLE IF EXISTS `resource_usage_rate`;
CREATE TABLE `resource_usage_rate` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `alarm_id` int(11) NOT NULL,
  `limit_value` double(20,2) DEFAULT NULL,
  `usage_value` double(20,2) DEFAULT NULL,
  `rate` double(5,2) NOT NULL,
  `occur_time` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_alarm_id` (`alarm_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='资源告警使用率明细';

-- ----------------------------



