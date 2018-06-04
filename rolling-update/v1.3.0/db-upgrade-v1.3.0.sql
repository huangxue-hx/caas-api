ALTER TABLE `url_dic`
MODIFY COLUMN `id`  int(11) NOT NULL AUTO_INCREMENT FIRST ;
INSERT INTO `url_dic`(url,module,resource) VALUES ('/tenants/*/projects/*/repositories/*/images/*/tags/*/syncImage', 'delivery', 'image');
INSERT INTO `url_dic`(url,module,resource) VALUES ('/tenants/*/projects/*/repositories/*/images/*/tags/*/syncclusters', 'delivery', 'image');
INSERT INTO `url_dic`(url,module,resource) VALUES ('/clusters/*/nodes/*/schedule',  'infrastructure', 'node');
INSERT INTO `url_dic`(url,module,resource) VALUES ('/clusters/*/nodes/*/drainPod',  'infrastructure', 'node');
INSERT INTO `url_dic`(url,module,resource) VALUES ('/clusters/*/nodes/*/drainProgress',  'infrastructure', 'node');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/tenants/*/msf/apps', 'msf', 'msf');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/tenants/*/msf/deploys', 'msf', 'msf');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/tenants/*/msf/deploys/*/start', 'msf', 'msf');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/tenants/*/msf/deploys/*/stop', 'msf', 'msf');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/tenants/*/msf/deploys/*/scale', 'msf', 'msf');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/tenants/*/msf/deploys/*/pods', 'msf', 'msf');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/tenants/*/msf/deploys/*/events', 'msf', 'msf');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/tenants/*/msf/deploys/*/containers', 'msf', 'msf');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/tenants/*/msf/deploys/*/applogs/filenames', 'msf', 'msf');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/tenants/*/msf/deploys/*/applogs/stderrlogs', 'msf', 'msf');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/tenants/*/msf/deploys/*/logfile/*/export', 'msf', 'msf');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/tenants/*/msf/deploys/rules', 'msf', 'msf');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/tenants/*/msf/deploys/*/applogs', 'msf', 'msf');

UPDATE `k8s_auth_server`.`url_dic` SET `id`='181', `url`='/tenants/*/projects/*/deploys/*/container/file/uploadToNode', `module`='appcenter', `resource`='app' WHERE (`id`='181');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/tenants/*/projects/*/cicdjobs/*/stageresult', 'cicd', 'cicdmgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/tenants/*/projects/*/cicdjobs/*/stages/*/log', 'cicd', 'cicdmgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/tenants/*/projects/*/svctemplates/*/checkResource', 'delivery', 'template');
INSERT INTO `k8s_auth_server`.`url_dic` ( `url`, `module`, `resource`) VALUES ('/tenants/*/projects/*/apptemplates/*/checkResource', 'delivery', 'template');

INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/clusters/*/nodes/*/addNode', 'infrastructure', 'node');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/clusters/*/nodes/*/removeNode', 'infrastructure', 'node');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/tenants/*/members', 'tenant', 'tenantmgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/tenants/*/privateNodeList', 'tenant', 'tenantmgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/tenants/*/namespaces/*/addNodes', 'tenant', 'tenantmgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/tenants/projects/addUser', 'tenant', 'tenantmgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/roles/*/copy', 'system', 'system');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/cicd/jobs/*/webhook', 'whitelist', 'whitelist');

ALTER TABLE `cicd_stage_build`
ADD COLUMN `log` mediumtext AFTER `test_url`;

ALTER TABLE `tenant_cluster_quota`
MODIFY COLUMN `cpu_quota`  double(11,1) NOT NULL DEFAULT 0 COMMENT '集群租户的cpu配额(core)' AFTER `cluster_id`,
MODIFY COLUMN `memory_quota`  double(11,1) NOT NULL DEFAULT 0 COMMENT '集群租户的内存配额(MB)' AFTER `cpu_quota`,
MODIFY COLUMN `pv_quota`  double(11,1) NOT NULL DEFAULT 0 COMMENT '集群租户的存储配额(MB)' AFTER `memory_quota`;

ALTER TABLE `user_group_relation`
DROP INDEX `user` ,
ADD INDEX `user` (`userid`) USING BTREE ;

DROP TABLE IF EXISTS `node_drain_progress`;
CREATE TABLE `node_drain_progress` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `node_name` varchar(64) NOT NULL,
  `status` varchar(16) NOT NULL,
  `pod_total_num` int(11) NOT NULL,
  `progress` varchar(1024) DEFAULT NULL,
  `cluster_id` varchar(64) NOT NULL,
  `error_msg` varchar(1024) DEFAULT NULL,
  `create_time` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
ALTER TABLE `namespace`
ADD COLUMN `cluster_alias_name`  varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '集群别名' AFTER `cluster_id`;
UPDATE k8s_auth_server.application_templates SET `name` = 'Fabric' WHERE id = 13;
UPDATE `k8s_auth_server`.`resource_menu` SET `name`='Dockerfile', `name_en`='Dockerfile' WHERE (`id`='19');
UPDATE `k8s_auth_server`.`resource_menu` SET `name_en`='Dependence' WHERE (`id`='21');
UPDATE `k8s_auth_server`.`resource_menu` SET `name`='环境管理' WHERE (`id`='22');
UPDATE `k8s_auth_server`.`resource_menu` SET `name`='守护进程',`module`='daemonset' WHERE (`id`='14');

ALTER TABLE `installprogress_installprogress`
MODIFY COLUMN `cluster_id`  varchar(64) NOT NULL AFTER `install_status`;

ALTER TABLE `image_clean_rule`
ADD COLUMN `harbor_project_name`  varchar(64) NULL COMMENT '镜像仓库名称' AFTER `repository_id`;

ALTER TABLE `log_backup_rule`
ADD COLUMN `last_backup_time`  datetime NULL COMMENT '上次备份时间' AFTER `max_restore_speed`;

INSERT INTO `cicd_build_environment`(id,name,image,is_public) VALUES(0, 'default', 'library/jenkins-slave-java:latest', NULL);
UPDATE `cicd_build_environment` SET id=0 WHERE name='default' AND project_id is null AND cluster_id is NULL;

ALTER TABLE k8s_server_mysql.`resource_monitor`
DROP COLUMN `unit_minute_time`,
DROP COLUMN `threshold_times`,
DROP COLUMN `to_email`,
DROP COLUMN `cc_email`,
ADD COLUMN `cpu_unit_minute_time`  smallint(4) NULL COMMENT 'cpu告警阈值监控单位时间（分钟）' AFTER `disk_flag`,
ADD COLUMN `cpu_threshold_times`  smallint(4) NULL COMMENT 'cpu告警阈值，出现次数' AFTER `cpu_unit_minute_time`,
ADD COLUMN `mem_unit_minute_time`  smallint(4) NULL COMMENT '内存告警阈值监控单位时间（分钟）' AFTER `cpu_threshold_times`,
ADD COLUMN `mem_threshold_times`  smallint(4) NULL COMMENT '内存告警阈值，单位时间出现次数' AFTER `mem_unit_minute_time`,
ADD COLUMN `disk_unit_minute_time`  smallint(4) NULL COMMENT '磁盘告警阈值监控单位时间（分钟）' AFTER `mem_threshold_times`,
ADD COLUMN `disk_threshold_times`  smallint(4) NULL COMMENT '磁盘告警阈值，单位时间出现次数' AFTER `disk_unit_minute_time`;

ALTER TABLE k8s_server_mysql.`app_log_monitor`
DROP INDEX `uni_log_monitor` ,
ADD UNIQUE INDEX `uni_log_monitor` (`service`, `namespace`, `container`) USING BTREE ;

ALTER TABLE k8s_server_mysql.`app_log_monitor`
DROP COLUMN `unit_minute_time`,
DROP COLUMN `threshold`,
MODIFY COLUMN `keyword`  varchar(1000) CHARACTER SET utf8 NOT NULL COMMENT '日志中关键字' AFTER `container`;

ALTER TABLE k8s_server_mysql.`alarm_record`
MODIFY COLUMN `times_threshold`  varchar(64) CHARACTER SET utf8 NULL DEFAULT NULL AFTER `usage_threshold`;

ALTER TABLE k8s_server_mysql.`alarm_receiver`
DROP INDEX `uni_alarm_user` ,
ADD UNIQUE INDEX `uni_alarm_user` (`alarm_type`, `monitor_id`, `user_id`) USING BTREE ;

ALTER TABLE k8s_server_mysql.`app_log_monitor`
ADD COLUMN `tenant_alias_name`  varchar(100) NULL AFTER `tenant_name`;
ALTER TABLE k8s_server_mysql.`resource_monitor`
ADD COLUMN `tenant_alias_name`  varchar(100) NULL AFTER `tenant_name`;