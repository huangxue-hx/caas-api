use `k8s_auth_server`;
ALTER TABLE `url_dic`
MODIFY COLUMN `id`  int(11) NOT NULL AUTO_INCREMENT FIRST ;

INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) SELECT '/tenants/*/projects/*/repositories/*/images/*/tags/*/syncImage', 'delivery', 'image' FROM dual WHERE not exists (select * from `k8s_auth_server`.`url_dic` where `k8s_auth_server`.`url_dic`.`url` = '/tenants/*/projects/*/repositories/*/images/*/tags/*/syncImage');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) SELECT '/tenants/*/projects/*/repositories/*/images/*/tags/*/syncclusters', 'delivery', 'image' FROM dual WHERE not exists (select * from `k8s_auth_server`.`url_dic` where `k8s_auth_server`.`url_dic`.`url` = '/tenants/*/projects/*/repositories/*/images/*/tags/*/syncclusters');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) SELECT '/clusters/*/nodes/*/schedule', 'infrastructure', 'node' FROM dual WHERE not exists (select * from `k8s_auth_server`.`url_dic` where `k8s_auth_server`.`url_dic`.`url` = '/clusters/*/nodes/*/schedule');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) SELECT '/clusters/*/nodes/*/drainPod', 'infrastructure', 'node' FROM dual WHERE not exists (select * from `k8s_auth_server`.`url_dic` where `k8s_auth_server`.`url_dic`.`url` = '/clusters/*/nodes/*/drainPod');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) SELECT '/clusters/*/nodes/*/drainProgress', 'infrastructure', 'node' FROM dual WHERE not exists (select * from `k8s_auth_server`.`url_dic` where `k8s_auth_server`.`url_dic`.`url` = '/clusters/*/nodes/*/drainProgress');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) SELECT '/tenants/*/msf/apps', 'msf', 'msf' FROM dual WHERE not exists (select * from `k8s_auth_server`.`url_dic` where `k8s_auth_server`.`url_dic`.`url` = '/tenants/*/msf/apps');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) SELECT '/tenants/*/msf/deploys', 'msf', 'msf' FROM dual WHERE not exists (select * from `k8s_auth_server`.`url_dic` where `k8s_auth_server`.`url_dic`.`url` = '/tenants/*/msf/deploys');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) SELECT '/tenants/*/msf/deploys/*/start', 'msf', 'msf' FROM dual WHERE not exists (select * from `k8s_auth_server`.`url_dic` where `k8s_auth_server`.`url_dic`.`url` = '/tenants/*/msf/deploys/*/start');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) SELECT '/tenants/*/msf/deploys/*/stop', 'msf', 'msf' FROM dual WHERE not exists (select * from `k8s_auth_server`.`url_dic` where `k8s_auth_server`.`url_dic`.`url` = '/tenants/*/msf/deploys/*/stop');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) SELECT '/tenants/*/msf/deploys/*/scale', 'msf', 'msf' FROM dual WHERE not exists (select * from `k8s_auth_server`.`url_dic` where `k8s_auth_server`.`url_dic`.`url` = '/tenants/*/msf/deploys/*/scale');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) SELECT '/tenants/*/msf/deploys/*/pods', 'msf', 'msf' FROM dual WHERE not exists (select * from `k8s_auth_server`.`url_dic` where `k8s_auth_server`.`url_dic`.`url` = '/tenants/*/msf/deploys/*/pods');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) SELECT '/tenants/*/msf/deploys/*/events', 'msf', 'msf' FROM dual WHERE not exists (select * from `k8s_auth_server`.`url_dic` where `k8s_auth_server`.`url_dic`.`url` = '/tenants/*/msf/deploys/*/events');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) SELECT '/tenants/*/msf/deploys/*/containers', 'msf', 'msf' FROM dual WHERE not exists (select * from `k8s_auth_server`.`url_dic` where `k8s_auth_server`.`url_dic`.`url` = '/tenants/*/msf/deploys/*/containers');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) SELECT '/tenants/*/msf/deploys/*/applogs/filenames', 'msf', 'msf' FROM dual WHERE not exists (select * from `k8s_auth_server`.`url_dic` where `k8s_auth_server`.`url_dic`.`url` = '/tenants/*/msf/deploys/*/applogs/filenames');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) SELECT '/tenants/*/msf/deploys/*/applogs/stderrlogs', 'msf', 'msf' FROM dual WHERE not exists (select * from `k8s_auth_server`.`url_dic` where `k8s_auth_server`.`url_dic`.`url` = '/tenants/*/msf/deploys/*/applogs/stderrlogs');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) SELECT '/tenants/*/msf/deploys/*/logfile/*/export', 'msf', 'msf' FROM dual WHERE not exists (select * from `k8s_auth_server`.`url_dic` where `k8s_auth_server`.`url_dic`.`url` = '/tenants/*/msf/deploys/*/logfile/*/export');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) SELECT '/tenants/*/msf/deploys/rules', 'msf', 'msf' FROM dual WHERE not exists (select * from `k8s_auth_server`.`url_dic` where `k8s_auth_server`.`url_dic`.`url` = '/tenants/*/msf/deploys/rules');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) SELECT '/tenants/*/msf/deploys/*/applogs', 'msf', 'msf' FROM dual WHERE not exists (select * from `k8s_auth_server`.`url_dic` where `k8s_auth_server`.`url_dic`.`url` = '/tenants/*/msf/deploys/*/applogs');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) SELECT '/tenants/*/projects/*/cicdjobs/*/stageresult', 'cicd', 'cicdmgr' FROM dual WHERE not exists (select * from `k8s_auth_server`.`url_dic` where `k8s_auth_server`.`url_dic`.`url` = '/tenants/*/projects/*/cicdjobs/*/stageresult');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) SELECT '/tenants/*/projects/*/cicdjobs/*/stages/*/log', 'cicd', 'cicdmgr' FROM dual WHERE not exists (select * from `k8s_auth_server`.`url_dic` where `k8s_auth_server`.`url_dic`.`url` = '/tenants/*/projects/*/cicdjobs/*/stages/*/log');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) SELECT '/tenants/*/projects/*/svctemplates/*/checkResource', 'delivery', 'template' FROM dual WHERE not exists (select * from `k8s_auth_server`.`url_dic` where `k8s_auth_server`.`url_dic`.`url` = '/tenants/*/projects/*/svctemplates/*/checkResource');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) SELECT '/tenants/*/projects/*/apptemplates/*/checkResource', 'delivery', 'template' FROM dual WHERE not exists (select * from `k8s_auth_server`.`url_dic` where `k8s_auth_server`.`url_dic`.`url` = '/tenants/*/projects/*/apptemplates/*/checkResource');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) SELECT '/clusters/*/nodes/*/addNode', 'infrastructure', 'node' FROM dual WHERE not exists (select * from `k8s_auth_server`.`url_dic` where `k8s_auth_server`.`url_dic`.`url` = '/clusters/*/nodes/*/addNode');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) SELECT '/clusters/*/nodes/*/removeNode', 'infrastructure', 'node' FROM dual WHERE not exists (select * from `k8s_auth_server`.`url_dic` where `k8s_auth_server`.`url_dic`.`url` = '/clusters/*/nodes/*/removeNode');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) SELECT '/tenants/*/members', 'tenant', 'tenantmgr' FROM dual WHERE not exists (select * from `k8s_auth_server`.`url_dic` where `k8s_auth_server`.`url_dic`.`url` = '/tenants/*/members');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) SELECT '/tenants/*/privateNodeList', 'tenant', 'tenantmgr' FROM dual WHERE not exists (select * from `k8s_auth_server`.`url_dic` where `k8s_auth_server`.`url_dic`.`url` = '/tenants/*/privateNodeList');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) SELECT '/tenants/*/namespaces/*/addNodes', 'tenant', 'tenantmgr' FROM dual WHERE not exists (select * from `k8s_auth_server`.`url_dic` where `k8s_auth_server`.`url_dic`.`url` = '/tenants/*/namespaces/*/addNodes');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) SELECT '/tenants/projects/addUser', 'tenant', 'tenantmgr' FROM dual WHERE not exists (select * from `k8s_auth_server`.`url_dic` where `k8s_auth_server`.`url_dic`.`url` = '/tenants/projects/addUser');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) SELECT '/roles/*/copy', 'system', 'system' FROM dual WHERE not exists (select * from `k8s_auth_server`.`url_dic` where `k8s_auth_server`.`url_dic`.`url` = '/roles/*/copy');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) SELECT '/cicd/jobs/*/webhook', 'whitelist', 'whitelist' FROM dual WHERE not exists (select * from `k8s_auth_server`.`url_dic` where `k8s_auth_server`.`url_dic`.`url` = '/cicd/jobs/*/webhook');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) SELECT '/tenants/*/projects/*/cicdjobs/*/rename', 'cicd', 'cicdmgr' FROM dual WHERE not exists (select * from `k8s_auth_server`.`url_dic` where `k8s_auth_server`.`url_dic`.`url` = '/tenants/*/projects/*/cicdjobs/*/rename');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) SELECT '/tenants/*/projects/*/cicdjobs/*/stages/updateCredentials', 'cicd', 'cicdmgr' FROM dual WHERE not exists (select * from `k8s_auth_server`.`url_dic` where `k8s_auth_server`.`url_dic`.`url` = '/tenants/*/projects/*/cicdjobs/*/stages/updateCredentials');

UPDATE `k8s_auth_server`.`url_dic` SET `id`='181', `url`='/tenants/*/projects/*/deploys/*/container/file/uploadToNode', `module`='appcenter', `resource`='app' WHERE (`id`='181');

DROP PROCEDURE IF EXISTS schema_change;
DELIMITER //
CREATE PROCEDURE schema_change() BEGIN
IF NOT EXISTS (SELECT column_name FROM information_schema.columns WHERE column_name = 'log' AND table_name = 'cicd_stage_build') THEN
   ALTER TABLE `cicd_stage_build`
   ADD COLUMN `log` mediumtext AFTER `test_url`;
END IF;
END//
DELIMITER ;
CALL schema_change();



ALTER TABLE `tenant_cluster_quota`
MODIFY COLUMN `cpu_quota`  double(11,1) NOT NULL DEFAULT 0 COMMENT '集群租户的cpu配额(core)' AFTER `cluster_id`,
MODIFY COLUMN `memory_quota`  double(11,1) NOT NULL DEFAULT 0 COMMENT '集群租户的内存配额(MB)' AFTER `cpu_quota`,
MODIFY COLUMN `pv_quota`  double(11,1) NOT NULL DEFAULT 0 COMMENT '集群租户的存储配额(MB)' AFTER `memory_quota`;

DROP PROCEDURE IF EXISTS schema_change;
DELIMITER //
CREATE PROCEDURE schema_change() BEGIN
IF NOT EXISTS (SELECT column_name FROM information_schema.statistics WHERE index_name = 'user' AND table_name = 'user_group_relation' AND table_schema = 'k8s_auth_server') THEN
    ALTER TABLE `user_group_relation`
    ADD INDEX `user` (`userid`) USING BTREE ;
END IF;
END//
DELIMITER ;
CALL schema_change();

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

DROP PROCEDURE IF EXISTS schema_change;
DELIMITER //
CREATE PROCEDURE schema_change() BEGIN
IF NOT EXISTS (SELECT column_name FROM information_schema.columns WHERE column_name = 'cluster_alias_name' AND table_name = 'namespace') THEN
   ALTER TABLE `namespace`
   ADD COLUMN `cluster_alias_name`  varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '集群别名' AFTER `cluster_id`;
END IF;
END//
DELIMITER ;
CALL schema_change();

UPDATE k8s_auth_server.application_templates SET `name` = 'Fabric' WHERE id = 13;
UPDATE `k8s_auth_server`.`resource_menu` SET `name`='Dockerfile', `name_en`='Dockerfile' WHERE (`id`='19');
UPDATE `k8s_auth_server`.`resource_menu` SET `name_en`='Dependence' WHERE (`id`='21');
UPDATE `k8s_auth_server`.`resource_menu` SET `name`='环境管理' WHERE (`id`='22');
UPDATE `k8s_auth_server`.`resource_menu` SET `name`='守护进程',`module`='daemonset' WHERE (`id`='14');

ALTER TABLE `installprogress_installprogress`
MODIFY COLUMN `cluster_id`  varchar(64) NOT NULL AFTER `install_status`;

DROP PROCEDURE IF EXISTS schema_change;
DELIMITER //
CREATE PROCEDURE schema_change() BEGIN
IF NOT EXISTS (SELECT column_name FROM information_schema.columns WHERE column_name = 'harbor_project_name' AND table_name = 'image_clean_rule') THEN
   ALTER TABLE `image_clean_rule`
   ADD COLUMN `harbor_project_name`  varchar(64) NULL COMMENT '镜像仓库名称' AFTER `repository_id`;
END IF;
END//
DELIMITER ;
CALL schema_change();

DROP PROCEDURE IF EXISTS schema_change;
DELIMITER //
CREATE PROCEDURE schema_change() BEGIN
IF NOT EXISTS (SELECT column_name FROM information_schema.columns WHERE column_name = 'last_backup_time' AND table_name = 'log_backup_rule') THEN
   ALTER TABLE `log_backup_rule`
   ADD COLUMN `last_backup_time`  datetime NULL COMMENT '上次备份时间' AFTER `max_restore_speed`;
END IF;
END//
DELIMITER ;
CALL schema_change();

INSERT INTO `k8s_auth_server`.`cicd_build_environment` (id,name,image,is_public) SELECT 0, 'default', 'library/jenkins-slave-java:latest', NULL FROM dual WHERE not exists (select * from `k8s_auth_server`.`cicd_build_environment` where `k8s_auth_server`.`cicd_build_environment`.`id` = 0);

UPDATE `cicd_build_environment` SET id=0 WHERE name='default' AND project_id is null AND cluster_id is NULL;

DROP PROCEDURE IF EXISTS schema_change;
DELIMITER //
CREATE PROCEDURE schema_change() BEGIN
IF EXISTS (SELECT column_name FROM information_schema.columns WHERE column_name = 'unit_minute_time' AND table_name = 'resource_monitor' AND table_schema = 'k8s_server_mysql') THEN
    ALTER TABLE k8s_server_mysql.`resource_monitor`
    DROP COLUMN `unit_minute_time`;
END IF;
END//
DELIMITER ;
CALL schema_change();

DROP PROCEDURE IF EXISTS schema_change;
DELIMITER //
CREATE PROCEDURE schema_change() BEGIN
IF EXISTS (SELECT column_name FROM information_schema.columns WHERE column_name = 'threshold_times' AND table_name = 'resource_monitor' AND table_schema = 'k8s_server_mysql') THEN
   ALTER TABLE k8s_server_mysql.`resource_monitor`
   DROP COLUMN `threshold_times`;
END IF;
END//
DELIMITER ;
CALL schema_change();

DROP PROCEDURE IF EXISTS schema_change;
DELIMITER //
CREATE PROCEDURE schema_change() BEGIN
IF EXISTS (SELECT column_name FROM information_schema.columns WHERE column_name = 'to_email' AND table_name = 'resource_monitor' AND table_schema = 'k8s_server_mysql') THEN
   ALTER TABLE k8s_server_mysql.`resource_monitor`
   DROP COLUMN `to_email`;
END IF;
END//
DELIMITER ;
CALL schema_change();

DROP PROCEDURE IF EXISTS schema_change;
DELIMITER //
CREATE PROCEDURE schema_change() BEGIN
IF EXISTS (SELECT column_name FROM information_schema.columns WHERE column_name = 'cc_email' AND table_name = 'resource_monitor' AND table_schema = 'k8s_server_mysql') THEN
   ALTER TABLE k8s_server_mysql.`resource_monitor`
   DROP COLUMN `cc_email`;
END IF;
END//
DELIMITER ;
CALL schema_change();

DROP PROCEDURE IF EXISTS schema_change;
DELIMITER //
CREATE PROCEDURE schema_change() BEGIN
IF NOT EXISTS (SELECT column_name FROM information_schema.columns WHERE column_name = 'cpu_unit_minute_time' AND table_name = 'resource_monitor' AND table_schema = 'k8s_server_mysql') THEN
   ALTER TABLE k8s_server_mysql.`resource_monitor`
   ADD COLUMN `cpu_unit_minute_time`  smallint(4) NULL COMMENT 'cpu告警阈值监控单位时间（分钟）' AFTER `disk_flag`;
END IF;
END//
DELIMITER ;
CALL schema_change();

DROP PROCEDURE IF EXISTS schema_change;
DELIMITER //
CREATE PROCEDURE schema_change() BEGIN
IF NOT EXISTS (SELECT column_name FROM information_schema.columns WHERE column_name = 'cpu_threshold_times' AND table_name = 'resource_monitor' AND table_schema = 'k8s_server_mysql') THEN
   ALTER TABLE k8s_server_mysql.`resource_monitor`
   ADD COLUMN `cpu_threshold_times`  smallint(4) NULL COMMENT 'cpu告警阈值，出现次数' AFTER `cpu_unit_minute_time`;
END IF;
END//
DELIMITER ;
CALL schema_change();

DROP PROCEDURE IF EXISTS schema_change;
DELIMITER //
CREATE PROCEDURE schema_change() BEGIN
IF NOT EXISTS (SELECT column_name FROM information_schema.columns WHERE column_name = 'mem_unit_minute_time' AND table_name = 'resource_monitor' AND table_schema = 'k8s_server_mysql') THEN
   ALTER TABLE k8s_server_mysql.`resource_monitor`
   ADD COLUMN `mem_unit_minute_time`  smallint(4) NULL COMMENT '内存告警阈值监控单位时间（分钟）' AFTER `cpu_threshold_times`;
END IF;
END//
DELIMITER ;
CALL schema_change();

DROP PROCEDURE IF EXISTS schema_change;
DELIMITER //
CREATE PROCEDURE schema_change() BEGIN
IF NOT EXISTS (SELECT column_name FROM information_schema.columns WHERE column_name = 'mem_threshold_times' AND table_name = 'resource_monitor' AND table_schema = 'k8s_server_mysql') THEN
   ALTER TABLE k8s_server_mysql.`resource_monitor`
   ADD COLUMN `mem_threshold_times`  smallint(4) NULL COMMENT '内存告警阈值，单位时间出现次数' AFTER `mem_unit_minute_time`;
END IF;
END//
DELIMITER ;
CALL schema_change();

DROP PROCEDURE IF EXISTS schema_change;
DELIMITER //
CREATE PROCEDURE schema_change() BEGIN
IF NOT EXISTS (SELECT column_name FROM information_schema.columns WHERE column_name = 'disk_unit_minute_time' AND table_name = 'resource_monitor' AND table_schema = 'k8s_server_mysql') THEN
   ALTER TABLE k8s_server_mysql.`resource_monitor`
   ADD COLUMN `disk_unit_minute_time`  smallint(4) NULL COMMENT '磁盘告警阈值监控单位时间（分钟）' AFTER `mem_threshold_times`;
END IF;
END//
DELIMITER ;
CALL schema_change();

DROP PROCEDURE IF EXISTS schema_change;
DELIMITER //
CREATE PROCEDURE schema_change() BEGIN
IF NOT EXISTS (SELECT column_name FROM information_schema.columns WHERE column_name = 'disk_threshold_times' AND table_name = 'resource_monitor' AND table_schema = 'k8s_server_mysql') THEN
   ALTER TABLE k8s_server_mysql.`resource_monitor`
   ADD COLUMN `disk_threshold_times`  smallint(4) NULL COMMENT '磁盘告警阈值，单位时间出现次数' AFTER `disk_unit_minute_time`;
END IF;
END//
DELIMITER ;
CALL schema_change();

DROP PROCEDURE IF EXISTS schema_change;
DELIMITER //
CREATE PROCEDURE schema_change() BEGIN
IF NOT EXISTS (SELECT column_name FROM information_schema.statistics WHERE index_name = 'uni_log_monitor' AND table_name = 'app_log_monitor' AND table_schema = 'k8s_server_mysql') THEN
    ALTER TABLE k8s_server_mysql.`app_log_monitor`
    ADD UNIQUE INDEX `uni_log_monitor` (`service`, `namespace`, `container`) USING BTREE ;
END IF;
END//
DELIMITER ;
CALL schema_change();

DROP PROCEDURE IF EXISTS schema_change;
DELIMITER //
CREATE PROCEDURE schema_change() BEGIN
IF EXISTS (SELECT column_name FROM information_schema.columns WHERE column_name = 'unit_minute_time' AND table_name = 'app_log_monitor' AND table_schema = 'k8s_server_mysql') THEN
    ALTER TABLE k8s_server_mysql.`app_log_monitor`
    DROP COLUMN `unit_minute_time`;
END IF;
END//
DELIMITER ;
CALL schema_change();

DROP PROCEDURE IF EXISTS schema_change;
DELIMITER //
CREATE PROCEDURE schema_change() BEGIN
IF EXISTS (SELECT column_name FROM information_schema.columns WHERE column_name = 'threshold' AND table_name = 'app_log_monitor' AND table_schema = 'k8s_server_mysql') THEN
    ALTER TABLE k8s_server_mysql.`app_log_monitor`
    DROP COLUMN `threshold`;
END IF;
END//
DELIMITER ;
CALL schema_change();

ALTER TABLE k8s_server_mysql.`app_log_monitor`
MODIFY COLUMN `keyword`  varchar(1000) CHARACTER SET utf8 NOT NULL COMMENT '日志中关键字' AFTER `container`;

ALTER TABLE k8s_server_mysql.`alarm_record`
MODIFY COLUMN `times_threshold`  varchar(64) CHARACTER SET utf8 NULL DEFAULT NULL AFTER `usage_threshold`;

DROP PROCEDURE IF EXISTS schema_change;
DELIMITER //
CREATE PROCEDURE schema_change() BEGIN
IF NOT EXISTS (SELECT column_name FROM information_schema.statistics WHERE index_name = 'uni_alarm_user' AND table_name = 'alarm_receiver' AND table_schema = 'k8s_server_mysql') THEN
    ALTER TABLE k8s_server_mysql.`alarm_receiver`
    ADD UNIQUE INDEX `uni_alarm_user` (`alarm_type`, `monitor_id`, `user_id`) USING BTREE ;
END IF;
END//
DELIMITER ;
CALL schema_change();

DROP PROCEDURE IF EXISTS schema_change;
DELIMITER //
CREATE PROCEDURE schema_change() BEGIN
IF NOT EXISTS (SELECT column_name FROM information_schema.columns WHERE column_name = 'tenant_alias_name' AND table_name = 'app_log_monitor' AND table_schema = 'k8s_server_mysql') THEN
   ALTER TABLE k8s_server_mysql.`app_log_monitor`
   ADD COLUMN `tenant_alias_name`  varchar(100) NULL AFTER `tenant_name`;
END IF;
END//
DELIMITER ;
CALL schema_change();

DROP PROCEDURE IF EXISTS schema_change;
DELIMITER //
CREATE PROCEDURE schema_change() BEGIN
IF NOT EXISTS (SELECT column_name FROM information_schema.columns WHERE column_name = 'tenant_alias_name' AND table_name = 'resource_monitor' AND table_schema = 'k8s_server_mysql') THEN
   ALTER TABLE k8s_server_mysql.`resource_monitor`
   ADD COLUMN `tenant_alias_name`  varchar(100) NULL AFTER `tenant_name`;
END IF;
END//
DELIMITER ;
CALL schema_change();

UPDATE k8s_auth_server.application_templates
SET tag = '5.7.6'
WHERE
	id = 5;

UPDATE k8s_auth_server.service_templates
SET deployment_content = '[{"annotation":"","clusterIP":"","containers":[{"args":[],"command":[],"configmap":[],"env":[{"key":"TZ","value":"Asia/Shanghai"}],"img":"onlineshop/mysql","livenessProbe":null,"log":"","name":"mysql","ports":[{"containerPort":"","expose":"true","port":"1433","protocol":"TCP"}],"readinessProbe":null,"resource":{"cpu":"1000m","memory":"1024"},"storage":[],"tag":"5.7.6"}],"hostName":"","instance":"1","labels":"","logPath":"","logService":"","name":"mysql","namespace":"","nodeSelector":"HarmonyCloud_Status=C","restartPolicy":"Always","sessionAffinity":""}]'
WHERE
	id = 5;

ALTER TABLE `cicd_docker_file`
MODIFY COLUMN `name`  varchar(100) CHARACTER SET utf8 NOT NULL;

DROP PROCEDURE IF EXISTS schema_change;
DELIMITER //
CREATE PROCEDURE schema_change() BEGIN
IF NOT EXISTS (SELECT column_name FROM information_schema.columns WHERE column_name = 'description' AND table_name = 'cicd_parameter' AND table_schema = 'k8s_auth_server') THEN
    ALTER TABLE `cicd_parameter`
    ADD COLUMN `description` varchar(255) CHARACTER SET utf8 DEFAULT NULL COMMENT '参数描述' AFTER `value`;
END IF;
END//
DELIMITER ;
CALL schema_change();


ALTER TABLE `cicd_stage`
MODIFY COLUMN `command`  varchar(4096) CHARACTER SET utf8 DEFAULT NULL COMMENT '脚本命令';

-- 老版本升级一定要执行 **************************** 开始
UPDATE `k8s_auth_server`.`tenant_cluster_quota` SET `reserve1`='normal' WHERE reserve1 IS NULL OR reserve1 = '';
-- 老版本升级一定要执行 **************************** 结束

DROP PROCEDURE IF EXISTS schema_change;
DELIMITER //
CREATE PROCEDURE schema_change() BEGIN
IF NOT EXISTS (SELECT column_name FROM information_schema.statistics WHERE index_name = 'tenant_cluster' AND table_name = 'namespace' AND table_schema = 'k8s_auth_server') THEN
    ALTER TABLE `namespace`
    ADD INDEX `tenant_cluster` (`tenant_id`, `cluster_id`) USING BTREE ;
END IF;
END//
DELIMITER ;
CALL schema_change();

DROP PROCEDURE IF EXISTS schema_change;
DELIMITER //
CREATE PROCEDURE schema_change() BEGIN
IF NOT EXISTS (SELECT column_name FROM information_schema.statistics WHERE index_name = 'ns' AND table_name = 'namespace' AND table_schema = 'k8s_auth_server') THEN
    ALTER TABLE `namespace`
    ADD INDEX `ns` (`namespace_name`) USING BTREE ;
END IF;
END//
DELIMITER ;
CALL schema_change();

DROP PROCEDURE IF EXISTS schema_change;
DELIMITER //
CREATE PROCEDURE schema_change() BEGIN
IF NOT EXISTS (SELECT column_name FROM information_schema.statistics WHERE index_name = 'roleid' AND table_name = 'resource_menu_role' AND table_schema = 'k8s_auth_server') THEN
    ALTER TABLE `resource_menu_role`
    ADD INDEX `roleid` (`role_id`, `available`) USING BTREE ;
END IF;
END//
DELIMITER ;
CALL schema_change();

DROP PROCEDURE IF EXISTS schema_change;
DELIMITER //
CREATE PROCEDURE schema_change() BEGIN
IF NOT EXISTS (SELECT column_name FROM information_schema.statistics WHERE index_name = 'tenant' AND table_name = 'tenant_cluster_quota' AND table_schema = 'k8s_auth_server') THEN
    ALTER TABLE `tenant_cluster_quota`
    ADD INDEX `tenant` (`tenant_id`, `cluster_id`, `reserve1`) USING BTREE ;
END IF;
END//
DELIMITER ;
CALL schema_change();

DROP PROCEDURE IF EXISTS schema_change;
DELIMITER //
CREATE PROCEDURE schema_change() BEGIN
IF NOT EXISTS (SELECT column_name FROM information_schema.statistics WHERE index_name = 'cluster' AND table_name = 'tenant_cluster_quota' AND table_schema = 'k8s_auth_server') THEN
    ALTER TABLE `tenant_cluster_quota`
    ADD INDEX `cluster` (`cluster_id`, `reserve1`) USING BTREE ;
END IF;
END//
DELIMITER ;
CALL schema_change();

DROP PROCEDURE IF EXISTS schema_change;
DELIMITER //
CREATE PROCEDURE schema_change() BEGIN
IF NOT EXISTS (SELECT column_name FROM information_schema.statistics WHERE index_name = 'job_num' AND table_name = 'cicd_job_build' AND table_schema = 'k8s_auth_server') THEN
    ALTER TABLE `cicd_job_build`
    ADD INDEX `job_num` (`job_id`, `build_num`) USING BTREE ;
END IF;
END//
DELIMITER ;
CALL schema_change();

DROP PROCEDURE IF EXISTS schema_change;
DELIMITER //
CREATE PROCEDURE schema_change() BEGIN
IF NOT EXISTS (SELECT column_name FROM information_schema.statistics WHERE index_name = 'stage_num' AND table_name = 'cicd_stage_build' AND table_schema = 'k8s_auth_server') THEN
    ALTER TABLE `cicd_stage_build`
    ADD INDEX `stage_num` (`stage_id`, `build_num`) USING BTREE ;
END IF;
END//
DELIMITER ;
CALL schema_change();

DROP PROCEDURE IF EXISTS schema_change;
DELIMITER //
CREATE PROCEDURE schema_change() BEGIN
IF NOT EXISTS (SELECT column_name FROM information_schema.statistics WHERE index_name = 'jobid' AND table_name = 'cicd_stage_build' AND table_schema = 'k8s_auth_server') THEN
    ALTER TABLE `cicd_stage_build`
    ADD INDEX `jobid` (`job_id`) USING BTREE ;
END IF;
END//
DELIMITER ;
CALL schema_change();

DROP PROCEDURE IF EXISTS schema_change;
DELIMITER //
CREATE PROCEDURE schema_change() BEGIN
IF NOT EXISTS (SELECT column_name FROM information_schema.columns WHERE column_name = 'trigger_image' AND table_name = 'cicd_trigger' AND table_schema = 'k8s_auth_server') THEN
    ALTER TABLE `cicd_trigger`
    ADD COLUMN `trigger_image` VARCHAR(255) CHARACTER SET utf8 DEFAULT NULL COMMENT '触发镜像';
END IF;
END//
DELIMITER ;
CALL schema_change();

DROP PROCEDURE IF EXISTS schema_change;
DELIMITER //
CREATE PROCEDURE schema_change() BEGIN
IF NOT EXISTS (SELECT column_name FROM information_schema.statistics WHERE index_name = 'name' AND table_name = 'resource_menu' AND table_schema = 'k8s_auth_server') THEN
    ALTER TABLE `resource_menu`
    ADD UNIQUE INDEX `name` (`name`, `url`, `parent_rmid`, `module`) USING BTREE ;
END IF;
END//
DELIMITER ;
CALL schema_change();

DROP PROCEDURE IF EXISTS schema_change;
DELIMITER //
CREATE PROCEDURE schema_change() BEGIN
IF NOT EXISTS (SELECT column_name FROM information_schema.statistics WHERE index_name = 'role_privilege' AND table_name = 'role_privilege_new' AND table_schema = 'k8s_auth_server') THEN
    ALTER TABLE `role_privilege_new`
    ADD UNIQUE INDEX `role_privilege` (`role_id`, `pid`) USING BTREE ;
END IF;
END//
DELIMITER ;
CALL schema_change();

DROP PROCEDURE IF EXISTS schema_change;
DELIMITER //
CREATE PROCEDURE schema_change() BEGIN
IF NOT EXISTS (SELECT column_name FROM information_schema.statistics WHERE index_name = 'role_privilege' AND table_name = 'role_privilege_new_replication' AND table_schema = 'k8s_auth_server') THEN
    ALTER TABLE `role_privilege_new_replication`
    ADD UNIQUE INDEX `role_privilege` (`role_id`, `pid`) USING BTREE ;
END IF;
END//
DELIMITER ;
CALL schema_change();

DROP PROCEDURE IF EXISTS schema_change;
DELIMITER //
CREATE PROCEDURE schema_change() BEGIN
IF NOT EXISTS (SELECT column_name FROM information_schema.statistics WHERE index_name = 'role_menu' AND table_name = 'resource_menu_role' AND table_schema = 'k8s_auth_server') THEN
    ALTER TABLE `resource_menu_role`
    ADD UNIQUE INDEX `role_menu` (`role_id`, `rmid`) USING BTREE ;
END IF;
END//
DELIMITER ;
CALL schema_change();

INSERT INTO `k8s_auth_server`.`privilege` (`id`, `module`, `module_name`, `resource`, `resource_name`, `privilege`, `privilege_name`, `remark`, `remark_name`, `status`, `create_time`, `update_time`) SELECT '94', 'log', '日志中心', 'systemlog', '系统日志', 'get', '系统日志查询', '', '', '1', '2018-04-09 08:31:00', NULL FROM dual WHERE not exists (select * from `k8s_auth_server`.`privilege` where `k8s_auth_server`.`privilege`.`id` = 94);

INSERT INTO `k8s_auth_server`.`role_privilege_new` (`status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) SELECT '1', '1', '94', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '' FROM dual WHERE not exists (select * from `k8s_auth_server`.`role_privilege_new` where `k8s_auth_server`.`role_privilege_new`.`status` = '1' and `k8s_auth_server`.`role_privilege_new`.`role_id` = '1' and `k8s_auth_server`.`role_privilege_new`.`pid` = '94');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) SELECT '0', '2', '94', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '' FROM dual WHERE not exists (select * from `k8s_auth_server`.`role_privilege_new` where `k8s_auth_server`.`role_privilege_new`.`status` = '0' and `k8s_auth_server`.`role_privilege_new`.`role_id` = '2' and `k8s_auth_server`.`role_privilege_new`.`pid` = '94');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) SELECT '0', '3', '94', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '' FROM dual WHERE not exists (select * from `k8s_auth_server`.`role_privilege_new` where `k8s_auth_server`.`role_privilege_new`.`status` = '0' and `k8s_auth_server`.`role_privilege_new`.`role_id` = '3' and `k8s_auth_server`.`role_privilege_new`.`pid` = '94');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) SELECT '0', '4', '94', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '' FROM dual WHERE not exists (select * from `k8s_auth_server`.`role_privilege_new` where `k8s_auth_server`.`role_privilege_new`.`status` = '0' and `k8s_auth_server`.`role_privilege_new`.`role_id` = '4' and `k8s_auth_server`.`role_privilege_new`.`pid` = '94');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) SELECT '0', '5', '94', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '' FROM dual WHERE not exists (select * from `k8s_auth_server`.`role_privilege_new` where `k8s_auth_server`.`role_privilege_new`.`status` = '0' and `k8s_auth_server`.`role_privilege_new`.`role_id` = '5' and `k8s_auth_server`.`role_privilege_new`.`pid` = '94');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) SELECT '1', '6', '94', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '' FROM dual WHERE not exists (select * from `k8s_auth_server`.`role_privilege_new` where `k8s_auth_server`.`role_privilege_new`.`status` = '1' and `k8s_auth_server`.`role_privilege_new`.`role_id` = '6' and `k8s_auth_server`.`role_privilege_new`.`pid` = '94');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) SELECT '0', '7', '94', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '' FROM dual WHERE not exists (select * from `k8s_auth_server`.`role_privilege_new` where `k8s_auth_server`.`role_privilege_new`.`status` = '0' and `k8s_auth_server`.`role_privilege_new`.`role_id` = '7' and `k8s_auth_server`.`role_privilege_new`.`pid` = '94');

INSERT INTO `k8s_auth_server`.`resource_menu` (`id`, `name`, `name_en`, `type`, `url`, `weight`, `create_time`, `update_time`, `available`, `icon_name`, `isparent`, `parent_rmid`, `module`) SELECT '30', '日志备份', 'Log Backup', 'menu', 'logBackup', '30', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '', '0', '9', 'snapshotrule' FROM dual WHERE not exists (select * from `k8s_auth_server`.`resource_menu` where `k8s_auth_server`.`resource_menu`.`id` = '30');
INSERT INTO `k8s_auth_server`.`resource_menu` (`id`, `name`, `name_en`, `type`, `url`, `weight`, `create_time`, `update_time`, `available`, `icon_name`, `isparent`, `parent_rmid`, `module`) SELECT '31', '系统日志', 'Ssystem Log', 'menu', 'systemLog', '31', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '', '0', '9', 'systemlog' FROM dual WHERE not exists (select * from `k8s_auth_server`.`resource_menu` where `k8s_auth_server`.`resource_menu`.`id` = '31');

INSERT INTO `k8s_auth_server`.`resource_menu_role` (`weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) SELECT '30', '2018-02-12 05:52:00', '2018-02-12 14:54:00', '1', '1', '30' FROM dual WHERE not exists (select * from `k8s_auth_server`.`resource_menu_role` where `k8s_auth_server`.`resource_menu_role`.`weight` = '30' and `k8s_auth_server`.`resource_menu_role`.`available` = '1' and `k8s_auth_server`.`resource_menu_role`.`role_id` = '1' and `k8s_auth_server`.`resource_menu_role`.`rmid` = '30');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) SELECT '30', '2018-02-12 05:52:00', '2018-02-12 14:54:00', '1', '2', '30' FROM dual WHERE not exists (select * from `k8s_auth_server`.`resource_menu_role` where `k8s_auth_server`.`resource_menu_role`.`weight` = '30' and `k8s_auth_server`.`resource_menu_role`.`available` = '1' and `k8s_auth_server`.`resource_menu_role`.`role_id` = '2' and `k8s_auth_server`.`resource_menu_role`.`rmid` = '30');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) SELECT '30', '2018-02-12 05:52:00', '2018-02-12 14:54:00', '1', '3', '30' FROM dual WHERE not exists (select * from `k8s_auth_server`.`resource_menu_role` where `k8s_auth_server`.`resource_menu_role`.`weight` = '30' and `k8s_auth_server`.`resource_menu_role`.`available` = '1' and `k8s_auth_server`.`resource_menu_role`.`role_id` = '3' and `k8s_auth_server`.`resource_menu_role`.`rmid` = '30');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) SELECT '30', '2018-02-12 05:52:00', '2018-02-12 14:54:00', '1', '4', '30' FROM dual WHERE not exists (select * from `k8s_auth_server`.`resource_menu_role` where `k8s_auth_server`.`resource_menu_role`.`weight` = '30' and `k8s_auth_server`.`resource_menu_role`.`available` = '1' and `k8s_auth_server`.`resource_menu_role`.`role_id` = '4' and `k8s_auth_server`.`resource_menu_role`.`rmid` = '30');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) SELECT '30', '2018-02-12 05:52:00', '2018-02-12 14:54:00', '1', '5', '30' FROM dual WHERE not exists (select * from `k8s_auth_server`.`resource_menu_role` where `k8s_auth_server`.`resource_menu_role`.`weight` = '30' and `k8s_auth_server`.`resource_menu_role`.`available` = '1' and `k8s_auth_server`.`resource_menu_role`.`role_id` = '5' and `k8s_auth_server`.`resource_menu_role`.`rmid` = '30');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) SELECT '30', '2018-02-12 05:52:00', '2018-02-12 14:54:00', '1', '6', '30' FROM dual WHERE not exists (select * from `k8s_auth_server`.`resource_menu_role` where `k8s_auth_server`.`resource_menu_role`.`weight` = '30' and `k8s_auth_server`.`resource_menu_role`.`available` = '1' and `k8s_auth_server`.`resource_menu_role`.`role_id` = '6' and `k8s_auth_server`.`resource_menu_role`.`rmid` = '30');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) SELECT '30', '2018-02-12 05:52:00', '2018-02-12 14:54:00', '1', '7', '30' FROM dual WHERE not exists (select * from `k8s_auth_server`.`resource_menu_role` where `k8s_auth_server`.`resource_menu_role`.`weight` = '30' and `k8s_auth_server`.`resource_menu_role`.`available` = '1' and `k8s_auth_server`.`resource_menu_role`.`role_id` = '7' and `k8s_auth_server`.`resource_menu_role`.`rmid` = '30');

INSERT INTO `k8s_auth_server`.`resource_menu_role` (`weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) SELECT '31', '2018-02-12 05:52:00', '2018-02-12 14:54:00', '1', '1', '31' FROM dual WHERE not exists (select * from `k8s_auth_server`.`resource_menu_role` where `k8s_auth_server`.`resource_menu_role`.`weight` = '31' and `k8s_auth_server`.`resource_menu_role`.`available` = '1' and `k8s_auth_server`.`resource_menu_role`.`role_id` = '1' and `k8s_auth_server`.`resource_menu_role`.`rmid` = '31');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) SELECT '31', '2018-02-12 05:52:00', '2018-02-12 14:54:00', '0', '2', '31' FROM dual WHERE not exists (select * from `k8s_auth_server`.`resource_menu_role` where `k8s_auth_server`.`resource_menu_role`.`weight` = '31' and `k8s_auth_server`.`resource_menu_role`.`available` = '0' and `k8s_auth_server`.`resource_menu_role`.`role_id` = '2' and `k8s_auth_server`.`resource_menu_role`.`rmid` = '31');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) SELECT '31', '2018-02-12 05:52:00', '2018-02-12 14:54:00', '0', '3', '31' FROM dual WHERE not exists (select * from `k8s_auth_server`.`resource_menu_role` where `k8s_auth_server`.`resource_menu_role`.`weight` = '31' and `k8s_auth_server`.`resource_menu_role`.`available` = '0' and `k8s_auth_server`.`resource_menu_role`.`role_id` = '3' and `k8s_auth_server`.`resource_menu_role`.`rmid` = '31');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) SELECT '31', '2018-02-12 05:52:00', '2018-02-12 14:54:00', '0', '4', '31' FROM dual WHERE not exists (select * from `k8s_auth_server`.`resource_menu_role` where `k8s_auth_server`.`resource_menu_role`.`weight` = '31' and `k8s_auth_server`.`resource_menu_role`.`available` = '0' and `k8s_auth_server`.`resource_menu_role`.`role_id` = '4' and `k8s_auth_server`.`resource_menu_role`.`rmid` = '31');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) SELECT '31', '2018-02-12 05:52:00', '2018-02-12 14:54:00', '0', '5', '31' FROM dual WHERE not exists (select * from `k8s_auth_server`.`resource_menu_role` where `k8s_auth_server`.`resource_menu_role`.`weight` = '31' and `k8s_auth_server`.`resource_menu_role`.`available` = '0' and `k8s_auth_server`.`resource_menu_role`.`role_id` = '5' and `k8s_auth_server`.`resource_menu_role`.`rmid` = '31');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) SELECT '31', '2018-02-12 05:52:00', '2018-02-12 14:54:00', '1', '6', '31' FROM dual WHERE not exists (select * from `k8s_auth_server`.`resource_menu_role` where `k8s_auth_server`.`resource_menu_role`.`weight` = '31' and `k8s_auth_server`.`resource_menu_role`.`available` = '1' and `k8s_auth_server`.`resource_menu_role`.`role_id` = '6' and `k8s_auth_server`.`resource_menu_role`.`rmid` = '31');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) SELECT '31', '2018-02-12 05:52:00', '2018-02-12 14:54:00', '0', '7', '31' FROM dual WHERE not exists (select * from `k8s_auth_server`.`resource_menu_role` where `k8s_auth_server`.`resource_menu_role`.`weight` = '31' and `k8s_auth_server`.`resource_menu_role`.`available` = '0' and `k8s_auth_server`.`resource_menu_role`.`role_id` = '7' and `k8s_auth_server`.`resource_menu_role`.`rmid` = '31');


INSERT INTO `k8s_auth_server`.`role_privilege_new_replication` (`status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) SELECT '1', '1', '94', '2018-01-13 07:13:00', '2018-01-13 07:13:00', NULL, NULL FROM dual WHERE not exists (select * from `k8s_auth_server`.`role_privilege_new_replication` where `k8s_auth_server`.`role_privilege_new_replication`.`status` = '1' and `k8s_auth_server`.`role_privilege_new_replication`.`role_id` = '1' and `k8s_auth_server`.`role_privilege_new_replication`.`pid` = '94');
INSERT INTO `k8s_auth_server`.`role_privilege_new_replication` (`status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) SELECT '0', '2', '94', '2018-01-13 07:13:00', '2018-01-13 07:13:00', NULL, NULL FROM dual WHERE not exists (select * from `k8s_auth_server`.`role_privilege_new_replication` where `k8s_auth_server`.`role_privilege_new_replication`.`status` = '0' and `k8s_auth_server`.`role_privilege_new_replication`.`role_id` = '2' and `k8s_auth_server`.`role_privilege_new_replication`.`pid` = '94');
INSERT INTO `k8s_auth_server`.`role_privilege_new_replication` (`status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) SELECT '0', '3', '94', '2018-01-13 07:13:00', '2018-01-13 07:13:00', NULL, NULL FROM dual WHERE not exists (select * from `k8s_auth_server`.`role_privilege_new_replication` where `k8s_auth_server`.`role_privilege_new_replication`.`status` = '0' and `k8s_auth_server`.`role_privilege_new_replication`.`role_id` = '3' and `k8s_auth_server`.`role_privilege_new_replication`.`pid` = '94');
INSERT INTO `k8s_auth_server`.`role_privilege_new_replication` (`status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) SELECT '0', '4', '94', '2018-01-13 07:13:00', '2018-01-13 07:13:00', NULL, NULL FROM dual WHERE not exists (select * from `k8s_auth_server`.`role_privilege_new_replication` where `k8s_auth_server`.`role_privilege_new_replication`.`status` = '0' and `k8s_auth_server`.`role_privilege_new_replication`.`role_id` = '4' and `k8s_auth_server`.`role_privilege_new_replication`.`pid` = '94');
INSERT INTO `k8s_auth_server`.`role_privilege_new_replication` (`status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) SELECT '0', '5', '94', '2018-01-13 07:13:00', '2018-01-13 07:13:00', NULL, NULL FROM dual WHERE not exists (select * from `k8s_auth_server`.`role_privilege_new_replication` where `k8s_auth_server`.`role_privilege_new_replication`.`status` = '0' and `k8s_auth_server`.`role_privilege_new_replication`.`role_id` = '5' and `k8s_auth_server`.`role_privilege_new_replication`.`pid` = '94');
INSERT INTO `k8s_auth_server`.`role_privilege_new_replication` (`status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) SELECT '1', '6', '94', '2018-01-13 07:13:00', '2018-01-13 07:13:00', NULL, NULL FROM dual WHERE not exists (select * from `k8s_auth_server`.`role_privilege_new_replication` where `k8s_auth_server`.`role_privilege_new_replication`.`status` = '1' and `k8s_auth_server`.`role_privilege_new_replication`.`role_id` = '6' and `k8s_auth_server`.`role_privilege_new_replication`.`pid` = '94');
INSERT INTO `k8s_auth_server`.`role_privilege_new_replication` (`status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) SELECT '0', '7', '94', '2018-01-13 07:13:00', '2018-01-13 07:13:00', NULL, NULL FROM dual WHERE not exists (select * from `k8s_auth_server`.`role_privilege_new_replication` where `k8s_auth_server`.`role_privilege_new_replication`.`status` = '0' and `k8s_auth_server`.`role_privilege_new_replication`.`role_id` = '7' and `k8s_auth_server`.`role_privilege_new_replication`.`pid` = '94');

ALTER TABLE `node_drain_progress`
MODIFY COLUMN `error_msg`  text CHARACTER SET utf8 COLLATE utf8_general_ci NULL AFTER `cluster_id`;

DROP PROCEDURE IF EXISTS schema_change;
DELIMITER //
CREATE PROCEDURE schema_change() BEGIN
IF NOT EXISTS (SELECT column_name FROM information_schema.statistics WHERE index_name = 'build_num' AND table_name = 'cicd_stage_build' AND table_schema = 'k8s_auth_server') THEN
    ALTER TABLE `cicd_stage_build`
    ADD INDEX `build_num` (`build_num`) USING BTREE ;
END IF;
END//
DELIMITER ;
CALL schema_change();

DROP PROCEDURE IF EXISTS schema_change;
DELIMITER //
CREATE PROCEDURE schema_change() BEGIN
IF NOT EXISTS (SELECT column_name FROM information_schema.statistics WHERE index_name = 'build_num' AND table_name = 'cicd_job_build' AND table_schema = 'k8s_auth_server') THEN
    ALTER TABLE `cicd_job_build`
    ADD INDEX `build_num` (`build_num`) USING BTREE ;
END IF;
END//
DELIMITER ;
CALL schema_change();

DROP PROCEDURE IF EXISTS schema_change;
DELIMITER //
CREATE PROCEDURE schema_change() BEGIN
IF NOT EXISTS (SELECT column_name FROM information_schema.statistics WHERE index_name = 'project_id' AND table_name = 'cicd_job' AND table_schema = 'k8s_auth_server') THEN
    ALTER TABLE `cicd_job`
    ADD INDEX `project_id` (`project_id`, `cluster_id`) USING BTREE ;
END IF;
END//
DELIMITER ;
CALL schema_change();

DROP PROCEDURE IF EXISTS schema_change;
DELIMITER //
CREATE PROCEDURE schema_change() BEGIN
IF NOT EXISTS (SELECT column_name FROM information_schema.statistics WHERE index_name = 'name' AND table_name = 'cicd_job' AND table_schema = 'k8s_auth_server') THEN
    ALTER TABLE `cicd_job`
    ADD INDEX `name` (`name`) USING BTREE ;
END IF;
END//
DELIMITER ;
CALL schema_change();

DROP PROCEDURE IF EXISTS schema_change;
DELIMITER //
CREATE PROCEDURE schema_change() BEGIN
IF NOT EXISTS (SELECT column_name FROM information_schema.columns WHERE column_name = 'description' AND table_name = 'cicd_job' AND table_schema = 'k8s_auth_server') THEN
    ALTER TABLE `cicd_job`
    ADD COLUMN `description`  varchar(1000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '流水线描述' AFTER `name`;
END IF;
END//
DELIMITER ;
CALL schema_change();

ALTER TABLE `project`
MODIFY COLUMN `pm_usernames`  varchar(512) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL AFTER `annotation`;

ALTER TABLE `tenant_binding_new`
MODIFY COLUMN `tm_usernames`  varchar(512) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'tm用户名列表' AFTER `tenant_name`;

UPDATE `k8s_auth_server`.`resource_menu` SET `name_en`='Configuration' WHERE (`id`='16');
UPDATE `k8s_auth_server`.`resource_menu` SET `name_en`='Dependence' WHERE (`id`='21');
UPDATE `k8s_auth_server`.`resource_menu` SET `name_en`='Environment' WHERE (`id`='22');
UPDATE `k8s_auth_server`.`resource_menu` SET `name_en`='Template' WHERE (`id`='25');
UPDATE `k8s_auth_server`.`resource_menu` SET `name_en`='Alarm processing' WHERE (`id`='29');
UPDATE `k8s_auth_server`.`resource_menu` SET `name_en`='System Log' WHERE (`id`='31');

INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) SELECT '/system/configs/cicd', 'system', 'system' FROM dual WHERE not exists (select * from `k8s_auth_server`.`url_dic` where `k8s_auth_server`.`url_dic`.`url` = '/system/configs/cicd');

DROP PROCEDURE IF EXISTS schema_change;
DELIMITER //
CREATE PROCEDURE schema_change() BEGIN
IF NOT EXISTS (SELECT column_name FROM information_schema.statistics WHERE index_name = 'uniq_name' AND table_name = 'configfile' AND table_schema = 'k8s_auth_server') THEN
    ALTER TABLE `configfile`
    ADD UNIQUE INDEX `uniq_name` (`cluster_id`, `project_id`, `name`, `tags`) USING BTREE ;
END IF;
END//
DELIMITER ;
CALL schema_change();

UPDATE `k8s_auth_server`.`role_privilege_new_replication` SET `status`='0' WHERE pid>=81 AND pid <=84 AND role_id>1;

UPDATE k8s_auth_server.service_templates
SET deployment_content = '[{"annotation":"","clusterIP":"","containers":[{"args":[],"command":[],"configmap":[],"env":[{"key":"JAVA_OPT","value":"-Xmx512m"},{"key":"TZ","value":"Asia/Shanghai"}],"img":"onlineshop/tomcat","livenessProbe":null,"log":"","name":"tomcat","ports":[{"containerPort":"","expose":"true","port":"8080","protocol":"TCP"}],"readinessProbe":null,"resource":{"cpu":"500m","memory":"512"},"storage":[],"tag":"v8.0"}],"hostName":"","instance":"1","labels":"","logPath":"","logService":"","name":"tomcat","namespace":"","nodeSelector":"HarmonyCloud_Status=C","restartPolicy":"Always","sessionAffinity":""}]'
WHERE
	id = 1;

UPDATE k8s_auth_server.service_templates
SET deployment_content = '[{"annotation":"","clusterIP":"","containers":[{"args":[],"command":[],"configmap":[],"env":[{"key":"TZ","value":"Asia/Shanghai"}],"img":"onlineshop/websphere-traditional","livenessProbe":null,"log":"","name":"websphere","ports":[{"containerPort":"","expose":"true","port":"9043","protocol":"TCP"},{"containerPort":"","expose":"true","port":"9080","protocol":"TCP"},{"containerPort":"","expose":"true","port":"9443","protocol":"TCP"},{"containerPort":"","expose":"true","port":"9060","protocol":"TCP"}],"readinessProbe":null,"resource":{"cpu":"1000m","memory":"1024"},"securityContext":{"add":[],"drop":[],"privileged":false,"security":false},"storage":[],"tag":"8.5.5.9-install"}],"hostIPC":false,"hostName":"","hostPID":false,"instance":"1","labels":"","logPath":"","logService":"","name":"websphere","namespace":"garydemo-garyns","nodeSelector":"HarmonyCloud_Status=C","restartPolicy":"Always","sessionAffinity":""}]'
WHERE
	id = 10;

UPDATE k8s_auth_server.service_templates
SET deployment_content = '[{"annotation":"","clusterIP":"","containers":[{"args":[],"command":[],"configmap":[],"env":[{"key":"redis-server","value":"--requirepass 123456"},{"key":"TZ","value":"Asia/Shanghai"}],"img":"onlineshop/redis","livenessProbe":null,"log":"","name":"redis","ports":[{"containerPort":"","expose":"true","port":"6379","protocol":"TCP"}],"readinessProbe":null,"resource":{"cpu":"100m","memory":"128"},"storage":[],"tag":"3.2-alpine"}],"hostName":"","instance":"1","labels":"","logPath":"","logService":"","name":"redis","namespace":"","nodeSelector":"HarmonyCloud_Status=C","restartPolicy":"Always","sessionAffinity":""}]'
WHERE
	id = 2;

UPDATE k8s_auth_server.service_templates
SET deployment_content = '[{"annotation":"","clusterIP":"","containers":[{"args":[],"command":[],"configmap":[],"env":[{"key":"TZ","value":"Asia/Shanghai"}],"img":"onlineshop/influxdb","livenessProbe":null,"log":"","name":"influxdb2","ports":[{"containerPort":"","expose":"true","port":"8086","protocol":"TCP"},{"containerPort":"","expose":"true","port":"8083","protocol":"TCP"}],"readinessProbe":null,"resource":{"cpu":"500m","memory":"512"},"storage":[],"tag":"1.3.0-alpine"}],"hostName":"","instance":"1","labels":"","logPath":"","logService":"","name":"influxdb2","namespace":"","nodeSelector":"HarmonyCloud_Status=C","restartPolicy":"Always","sessionAffinity":""}]'
WHERE
	id = 4;

UPDATE k8s_auth_server.service_templates
SET deployment_content = '[{"annotation":"","clusterIP":"","containers":[{"args":["$MYSQL_PORT_3306_TCP_PORT","$MYSQL_PORT_3306_TCP_ADDR","$MYSQL_ENV_MYSQL_ROOT_PASSWORD"],"command":["mysql -P","mysql -h","mysql -uroot -p"],"configmap":[],"env":[{"key":"TZ","value":"Asia/Shanghai"}],"img":"onlineshop/mysql","livenessProbe":null,"log":"","name":"mysql","ports":[{"containerPort":"","expose":"true","port":"3306","protocol":"TCP"}],"readinessProbe":null,"resource":{"cpu":"1000m","memory":"1024"},"storage":[],"tag":"5.7.6"}],"hostName":"","instance":"1","labels":"","logPath":"","logService":"","name":"mysql","namespace":"","nodeSelector":"HarmonyCloud_Status=C","restartPolicy":"Always","sessionAffinity":""}]'
WHERE
	id = 5;

UPDATE k8s_auth_server.service_templates
SET deployment_content = '[{"annotation":"","clusterIP":"","containers":[{"args":[],"command":[],"configmap":[],"env":[{"key":"TZ","value":"Asia/Shanghai"}],"imagePullPolicy":"Always","img":"onlineshop/mongodb","livenessProbe":null,"log":"","name":"mongodb","ports":[{"containerPort":"","expose":"true","port":"27017","protocol":"TCP"}],"readinessProbe":null,"resource":{"cpu":"100m","memory":"128"},"securityContext":{"add":[],"drop":[],"privileged":false,"security":false},"storage":[],"tag":"v3.5"}],"hostIPC":false,"hostName":"","hostPID":false,"instance":"1","labels":"","logPath":"","logService":"","name":"mongodb","namespace":"","nodeSelector":"HarmonyCloud_Status=C","restartPolicy":"Always","sessionAffinity":""}]'
WHERE
	id = 7;

UPDATE k8s_auth_server.service_templates
SET deployment_content = '[{"annotation":"","clusterIP":"","containers":[{"args":[],"command":[],"configmap":[],"env":[{"key":"JAVA_OPT","value":"-Xmx512m"},{"key":"TZ","value":"Asia/Shanghai"}],"imagePullPolicy":"Always","img":"onlineshop/elasticsearch","livenessProbe":null,"log":"","name":"elasticsearch","ports":[{"containerPort":"","expose":"true","port":"9200","protocol":"TCP"},{"containerPort":"","expose":"true","port":"9300","protocol":"TCP"}],"readinessProbe":null,"resource":{"cpu":"500m","memory":"512"},"securityContext":{"add":[],"drop":[],"privileged":false,"security":false},"storage":[],"tag":"v2.4.1-1"}],"hostIPC":false,"hostName":"","hostPID":false,"instance":"1","labels":"","logPath":"","logService":"","name":"elasticsearch","namespace":"garydemo-garyns","nodeSelector":"HarmonyCloud_Status=C","restartPolicy":"Always","sessionAffinity":""}]'
WHERE
	id = 11;

UPDATE k8s_auth_server.service_templates
SET deployment_content = '[{"annotation":"","clusterIP":"","containers":[{"args":[],"command":[],"configmap":[],"env":[{"key":"TZ","value":"Asia/Shanghai"}],"imagePullPolicy":"Always","img":"onlineshop/rabbitmq","livenessProbe":null,"log":"","name":"rabbitmq","ports":[{"containerPort":"","expose":"true","port":"4369","protocol":"TCP"},{"containerPort":"","expose":"true","port":"5672","protocol":"TCP"},{"containerPort":"","expose":"true","port":"5671","protocol":"TCP"},{"containerPort":"","expose":"true","port":"15672","protocol":"TCP"},{"containerPort":"","expose":"true","port":"25672","protocol":"TCP"}],"readinessProbe":null,"resource":{"cpu":"500m","memory":"512"},"securityContext":{"add":[],"drop":[],"privileged":false,"security":false},"storage":[],"tag":"3.6.11"}],"hostIPC":false,"hostName":"","hostPID":false,"instance":"1","labels":"","logPath":"","logService":"","name":"rabbitmq","namespace":"","nodeSelector":"HarmonyCloud_Status=C","restartPolicy":"Always","sessionAffinity":""}]'
WHERE
	id = 8;

DROP PROCEDURE IF EXISTS schema_change;
DELIMITER //
CREATE PROCEDURE schema_change() BEGIN
IF NOT EXISTS (SELECT column_name FROM information_schema.statistics WHERE index_name = 'project' AND table_name = 'user_role_relationship' AND table_schema = 'k8s_auth_server') THEN
    ALTER TABLE `user_role_relationship`
    ADD INDEX `project` (`project_id`) USING BTREE ;
END IF;
END//
DELIMITER ;
CALL schema_change();

DROP PROCEDURE IF EXISTS schema_change;
DELIMITER //
CREATE PROCEDURE schema_change() BEGIN
IF NOT EXISTS (SELECT column_name FROM information_schema.statistics WHERE index_name = 'username' AND table_name = 'user_role_relationship' AND table_schema = 'k8s_auth_server') THEN
    ALTER TABLE `user_role_relationship`
    ADD INDEX `username` (`username`) USING BTREE ;
END IF;
END//
DELIMITER ;
CALL schema_change();

DROP PROCEDURE IF EXISTS schema_change;
DELIMITER //
CREATE PROCEDURE schema_change() BEGIN
IF NOT EXISTS (SELECT column_name FROM information_schema.statistics WHERE index_name = 'project_cluster' AND table_name = 'service_templates' AND table_schema = 'k8s_auth_server') THEN
    ALTER TABLE `service_templates`
    ADD INDEX `project_cluster` (`project_id`, `cluster_id`) USING BTREE ;
END IF;
END//
DELIMITER ;
CALL schema_change();