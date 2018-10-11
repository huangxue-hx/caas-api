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