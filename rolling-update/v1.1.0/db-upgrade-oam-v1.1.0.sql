use k8s_server_mysql;
ALTER TABLE `k8s_server_mysql`.`alarm_receiver`
ADD COLUMN `receiver_type` VARCHAR(45) NULL AFTER `user_mobile`,
ADD COLUMN `group_id` VARCHAR(45) NULL AFTER `receiver_type`;

alter table `k8s_server_mysql`.resource_monitor add column volume_flag tinyint(1) DEFAULT '0';
alter table `k8s_server_mysql`.resource_monitor add column volume_usage_threshold tinyint(2) DEFAULT NULL;
alter table `k8s_server_mysql`.resource_monitor add column volume_unit_minute_time smallint(4) unsigned DEFAULT NULL COMMENT '单位时间内(分钟)';
alter table `k8s_server_mysql`.resource_monitor add column volume_threshold_times smallint(4) unsigned DEFAULT NULL COMMENT '单位时间内超过使用率阈值次数';
alter table `k8s_server_mysql`.resource_monitor add column volume_limits varchar(255) DEFAULT NULL;

ALTER TABLE `k8s_server_mysql`.`resource_monitor`
CHANGE COLUMN `namespace` `namespace` VARCHAR(128) NOT NULL COMMENT '分区名' ;

ALTER TABLE `k8s_server_mysql`.`app_log_monitor`
CHANGE COLUMN `namespace` `namespace` VARCHAR(128) NOT NULL COMMENT '分区名' ;

ALTER TABLE `k8s_server_mysql`.`alarm_record`
CHANGE COLUMN `namespace` `namespace` VARCHAR(128) NULL DEFAULT NULL ;

ALTER TABLE `k8s_server_mysql`.`alarm_record`
CHANGE COLUMN `tenant_name` `tenant_name` VARCHAR(128) NULL DEFAULT NULL ;

ALTER TABLE `k8s_server_mysql`.`resource_monitor`
CHANGE COLUMN `tenant_name` `tenant_name` VARCHAR(128) NULL DEFAULT NULL ;

ALTER TABLE `k8s_server_mysql`.`resource_monitor` 
CHANGE COLUMN `tenant_alias_name` `tenant_alias_name` VARCHAR(100) NULL ;

-------------------------------------sprint1----------------------------------------------
DELETE FROM `k8s_server_mysql`.`app_config` WHERE (`name`='log.keep.days');
ALTER TABLE `k8s_server_mysql`.`app_config`
ADD COLUMN `data_center`  varchar(100) NULL AFTER `modified_time`,
ADD COLUMN `cluster_id`  varchar(100) NULL AFTER `data_center`,
DROP INDEX `uni_name` ,
ADD UNIQUE INDEX `uni_name` (`grouping`, `name`, `is_deleted`, `data_center`, `cluster_id`) USING BTREE ;

INSERT INTO `k8s_server_mysql`.`app_config` (`name`, `value`, `grouping`, `comment`, `is_deleted`, `created_user`, `modified_user`, `created_time`, `modified_time`, `data_center`, `cluster_id`) VALUES ('log.keep.days', '30', 'default', NULL, '0', 'xfliang', 'xfliang', '2018-01-11 08:38:28', '2018-07-04 14:18:43', 'dc-yantai', 'dc-yantai--dev');
INSERT INTO `k8s_server_mysql`.`app_config` (`name`, `value`, `grouping`, `comment`, `is_deleted`, `created_user`, `modified_user`, `created_time`, `modified_time`, `data_center`, `cluster_id`) VALUES ('log.keep.days', '30', 'default', NULL, '0', 'xfliang', 'xfliang', '2018-01-11 08:38:28', '2018-07-04 14:18:43', 'dc-yantai', 'dc-yantai--qas');
INSERT INTO `k8s_server_mysql`.`app_config` (`name`, `value`, `grouping`, `comment`, `is_deleted`, `created_user`, `modified_user`, `created_time`, `modified_time`, `data_center`, `cluster_id`) VALUES ('log.keep.days', '30', 'default', NULL, '0', 'xfliang', 'xfliang', '2018-01-11 08:38:28', '2018-07-04 14:18:43', 'dc-yantai', 'dc-yantai--uat');
INSERT INTO `k8s_server_mysql`.`app_config` (`name`, `value`, `grouping`, `comment`, `is_deleted`, `created_user`, `modified_user`, `created_time`, `modified_time`, `data_center`, `cluster_id`) VALUES ('log.keep.days', '30', 'default', NULL, '0', 'xfliang', 'xfliang', '2018-01-11 08:38:28', '2018-07-04 14:18:43', 'dc-yantai', 'dc-yantai--prd');
INSERT INTO `k8s_server_mysql`.`app_config` (`name`, `value`, `grouping`, `comment`, `is_deleted`, `created_user`, `modified_user`, `created_time`, `modified_time`, `data_center`, `cluster_id`) VALUES ('log.keep.days', '30', 'default', NULL, '0', 'xfliang', 'xfliang', '2018-01-11 08:38:28', '2018-07-04 14:18:43', 'cluster-top', 'cluster-top--top');


ALTER TABLE `alarm_record`
ADD COLUMN `cluster_id`  varchar(128) NULL AFTER `alarm_type_name`,
ADD COLUMN `cluster_master`  varchar(128) NULL AFTER `cluster_id`;
-- ----------------------需要修改为集群对应的信息----------------
update `alarm_record` set cluster_id='cluster-top--top', cluster_master='oaplcs-tmt01.ap.whchem.com' where cluster='top';
update `alarm_record` set cluster_id='dc-yantai--dev', cluster_master='oaplcs-dmt01.ap.whchem.com' where cluster='dev';
update `alarm_record` set cluster_id='dc-yantai--qas', cluster_master='oaplcs-qmt01.ap.whchem.com' where cluster='qas';
update `alarm_record` set cluster_id='dc-yantai--uat', cluster_master='apisuat.caas.whchem.com' where cluster='uat';
update `alarm_record` set cluster_id='dc-yantai--prd', cluster_master='apis.caas.whchem.com' where cluster='prd';
-- -----------------------------------sprint2----------------------------------------------

-- -----------------------------------sprint3----------------------------------------------
DROP TABLE IF EXISTS `k8s_server_mysql`.`istio_event_monitor`;
CREATE TABLE `k8s_server_mysql`.`istio_event_monitor` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `alarm_name` varchar(45) DEFAULT NULL,
  `type` varchar(45) NOT NULL COMMENT '告警规则类型ISTIO_EVENT_MONITOR',
  `tenant_id` varchar(48) NOT NULL COMMENT '租户',
  `tenant_name` varchar(128) DEFAULT NULL,
  `tenant_alias_name` varchar(100) DEFAULT NULL,
  `project_id` varchar(64) NOT NULL,
  `project_name` varchar(64) DEFAULT NULL,
  `cluster_id` varchar(64) NOT NULL,
  `cluster_name` varchar(64) DEFAULT NULL,
  `namespace` varchar(128) NOT NULL COMMENT '分区名',
  `service` varchar(45) NOT NULL,
  `unit_minute_time` smallint(4) NOT NULL COMMENT '监控单位时间（分钟）',
  `threshold_times` smallint(4) NOT NULL COMMENT '单位时间出现次数',
  `alert_interval_time` smallint(4) unsigned NOT NULL COMMENT '告警间隔时间',
  `send_alert_time` datetime DEFAULT NULL COMMENT '最后一次告警时间',
  `receiver_names` varchar(200) DEFAULT NULL,
  `created_time` datetime NOT NULL COMMENT '创建时间',
  `modified_time` datetime NOT NULL COMMENT '修改时间',
  `status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '0开启，1停止',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_service` (`project_id`,`service`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=90 DEFAULT CHARSET=utf8 COMMENT='istio事件告警规则';

DROP TABLE IF EXISTS `k8s_server_mysql`.`istio_event`;
CREATE TABLE `k8s_server_mysql`.`istio_event` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `event_type` varchar(45) NOT NULL,
  `source_service_namespace` varchar(128) DEFAULT NULL COMMENT '请求方服务分区名',
  `source_service_name` varchar(128) DEFAULT NULL COMMENT '请求方服务名',
  `dest_service_namespace` varchar(128) NOT NULL COMMENT '服务提供方服务分区名',
  `dest_service_name` varchar(128) NOT NULL COMMENT '服务提供方服务名',
  `cluster_id` varchar(64) NOT NULL,
  `cluster_name` varchar(64) DEFAULT NULL,
  `event_time` datetime NOT NULL COMMENT '事件时间，一分钟的最后一秒',
  `event_count` int(11) NOT NULL COMMENT '一分钟内发生的事件次数',
  PRIMARY KEY (`id`),
  KEY `idx_event` (`dest_service_namespace`,`event_time`,`dest_service_name`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=572 DEFAULT CHARSET=utf8 COMMENT='istio事件记录';

-- -----------------------------------sprint-test1----------------------------------------------
ALTER TABLE k8s_server_mysql.resource_monitor
ADD COLUMN `service_type`  varchar(45) NULL COMMENT '服务类型，Deployment/StatefulSet' AFTER `namespace`;
update k8s_server_mysql.resource_monitor set service_type = 'Deployment' where type='CONTAINER_RESOURCE_MONITOR';
update k8s_server_mysql.resource_monitor set service_type = '' where type<>'CONTAINER_RESOURCE_MONITOR';
ALTER TABLE `resource_monitor`
DROP INDEX `uni_resource_monitor` ,
ADD UNIQUE INDEX `uni_resource_monitor` (`type`, `namespace`, `service_type`, `service`, `container`) USING BTREE ;

ALTER TABLE k8s_server_mysql.app_log_monitor
ADD COLUMN `service_type`  varchar(45) NULL COMMENT '服务类型，Deployment/StatefulSet' AFTER `namespace`;
update k8s_server_mysql.app_log_monitor set service_type = 'Deployment';
ALTER TABLE `app_log_monitor`
DROP INDEX `uni_log_monitor` ,
ADD UNIQUE INDEX `uni_log_monitor` (`service_type`, `service`, `namespace`, `container`) USING BTREE ;

-- -----------------------------------sprint-test2----------------------------------------------
-- 告警规则开启状态1，关闭0
update k8s_server_mysql.app_log_monitor set status = 2 where status =0;
update k8s_server_mysql.app_log_monitor set status = 0 where status =1;
update k8s_server_mysql.app_log_monitor set status = 1 where status =2;
update k8s_server_mysql.resource_monitor set status = 2 where status =0;
update k8s_server_mysql.resource_monitor set status = 0 where status =1;
update k8s_server_mysql.resource_monitor set status = 1 where status =2;

update k8s_server_mysql.istio_event_monitor set status = 2 where status =0;
update k8s_server_mysql.istio_event_monitor set status = 0 where status =1;
update k8s_server_mysql.istio_event_monitor set status = 1 where status =2;

ALTER TABLE k8s_server_mysql.`resource_monitor`
MODIFY COLUMN `status`  tinyint(1) NOT NULL DEFAULT 1 COMMENT '1开启，0停止' AFTER `modified_user`;

ALTER TABLE k8s_server_mysql.`app_log_monitor`
MODIFY COLUMN `status`  tinyint(1) NOT NULL DEFAULT 1 COMMENT '1-启用，0-停用' AFTER `modified_user`;
-- -------------------------2019.1.16-----------------------
ALTER TABLE k8s_server_mysql.`istio_event_monitor`
MODIFY COLUMN `alarm_name`  varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL AFTER `id`,
MODIFY COLUMN `project_name`  varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL AFTER `project_id`,
MODIFY COLUMN `cluster_name`  varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL AFTER `cluster_id`,
MODIFY COLUMN `service`  varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL AFTER `namespace`;


-- ------20190126-----------
ALTER TABLE k8s_server_mysql.`alarm_record`
DROP INDEX `idx_container`,
DROP INDEX `idx_monitor`,
DROP INDEX `idx_node` ,
ADD INDEX `idx_cluater` (`cluster_id`, `alarm_type`) USING BTREE ;

-- -----------20190218----------------
ALTER TABLE `mail_record`
MODIFY COLUMN `to_email`  varchar(1024) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '报警接收邮箱' AFTER `content`,
MODIFY COLUMN `cc_email`  varchar(1024) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '报警抄送邮箱' AFTER `to_email`;

ALTER TABLE `alarm_record`
MODIFY COLUMN `receiver_names`  varchar(1024) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL AFTER `reason_en`,
MODIFY COLUMN `receiver_usernames`  varchar(1024) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '告警接收人用户名' AFTER `receiver_names`;

ALTER TABLE `mail_record`
MODIFY COLUMN `status`  tinyint(2) NOT NULL COMMENT '0 - 新建， 1-发送成功， 2-发送失败, 3-邮箱地址错误' AFTER `cc_email`;

-- ------------20190221---------------------
ALTER TABLE `istio_event`
ADD COLUMN `is_alarmed`  tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已发送告警，0-未通知，1-已通知' AFTER `event_count`;