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
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/tenants/*/projects/*/cicdjobs/*/rename', 'cicd', 'cicdmgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/tenants/*/projects/*/cicdjobs/*/stages/updateCredentials', 'cicd', 'cicdmgr');

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

ALTER TABLE `cicd_parameter`
ADD COLUMN `description` varchar(255) CHARACTER SET utf8 DEFAULT NULL COMMENT '参数描述' AFTER `value`;

ALTER TABLE `cicd_stage`
MODIFY COLUMN `command`  varchar(4096) CHARACTER SET utf8 DEFAULT NULL COMMENT '脚本命令';

-- 老版本升级一定要执行 **************************** 开始
UPDATE `k8s_auth_server`.`tenant_cluster_quota` SET `reserve1`='normal' WHERE reserve1 IS NULL OR reserve1 = '';
-- 老版本升级一定要执行 **************************** 结束

ALTER TABLE `namespace`
ADD INDEX `tenant_cluster` (`tenant_id`, `cluster_id`) USING BTREE ;
ALTER TABLE `namespace`
ADD INDEX `ns` (`namespace_name`) USING BTREE ;
ALTER TABLE `resource_menu_role`
ADD INDEX `roleid` (`role_id`, `available`) USING BTREE ;
ALTER TABLE `tenant_cluster_quota`
ADD INDEX `tenant` (`tenant_id`, `cluster_id`, `reserve1`) USING BTREE ,
ADD INDEX `cluster` (`cluster_id`, `reserve1`) USING BTREE ;
ALTER TABLE `cicd_job_build`
ADD INDEX `job_num` (`job_id`, `build_num`) USING BTREE ;
ALTER TABLE `cicd_stage_build`
ADD INDEX `stage_num` (`stage_id`, `build_num`) USING BTREE ,
ADD INDEX `jobid` (`job_id`) USING BTREE ;


ALTER TABLE `cicd_trigger`
ADD COLUMN `trigger_image` VARCHAR(255) CHARACTER SET utf8 DEFAULT NULL COMMENT '触发镜像';

ALTER TABLE `resource_menu`
ADD UNIQUE INDEX `name` (`name`, `url`, `parent_rmid`, `module`) USING BTREE ;
ALTER TABLE `role_privilege_new`
ADD UNIQUE INDEX `role_privilege` (`role_id`, `pid`) USING BTREE ;
ALTER TABLE `role_privilege_new_replication`
ADD UNIQUE INDEX `role_privilege` (`role_id`, `pid`) USING BTREE ;
ALTER TABLE `resource_menu_role`
ADD UNIQUE INDEX `role_menu` (`role_id`, `rmid`) USING BTREE ;

INSERT IGNORE INTO `k8s_auth_server`.`privilege` (`id`, `module`, `module_name`, `resource`, `resource_name`, `privilege`, `privilege_name`, `remark`, `remark_name`, `status`, `create_time`, `update_time`) VALUES ('94', 'log', '日志中心', 'systemlog', '系统日志', 'get', '系统日志查询', '', '', '1', '2018-04-09 08:31:00', NULL);

INSERT IGNORE INTO `k8s_auth_server`.`role_privilege_new` (`status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('1', '1', '94', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT IGNORE INTO `k8s_auth_server`.`role_privilege_new` (`status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('0', '2', '94', '2018-01-13 07:13:00', '2018-03-29 14:05:22', '', '');
INSERT IGNORE INTO `k8s_auth_server`.`role_privilege_new` (`status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('0', '3', '94', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT IGNORE INTO `k8s_auth_server`.`role_privilege_new` (`status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('0', '4', '94', '2018-01-13 07:13:00', '2018-03-16 06:24:00', '', '');
INSERT IGNORE INTO `k8s_auth_server`.`role_privilege_new` (`status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('0', '5', '94', '2018-01-13 07:13:00', '2018-03-29 12:41:27', '', '');
INSERT IGNORE INTO `k8s_auth_server`.`role_privilege_new` (`status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('1', '6', '94', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT IGNORE INTO `k8s_auth_server`.`role_privilege_new` (`status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('0', '7', '94', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');

INSERT IGNORE INTO `k8s_auth_server`.`resource_menu` (`id`, `name`, `name_en`, `type`, `url`, `weight`, `create_time`, `update_time`, `available`, `icon_name`, `isparent`, `parent_rmid`, `module`) VALUES ('30', '日志备份', 'Log Backup', 'menu', 'logBackup', '30', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '', '0', '9', 'snapshotrule');
INSERT IGNORE INTO `k8s_auth_server`.`resource_menu` (`id`, `name`, `name_en`, `type`, `url`, `weight`, `create_time`, `update_time`, `available`, `icon_name`, `isparent`, `parent_rmid`, `module`) VALUES ('31', '系统日志', 'Ssystem Log', 'menu', 'systemLog', '31', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '', '0', '9', 'systemlog');

INSERT IGNORE INTO `k8s_auth_server`.`resource_menu_role` (`weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('30', '2018-02-12 05:52:00', '2018-02-12 14:54:00', '1', '1', '30');
INSERT IGNORE INTO `k8s_auth_server`.`resource_menu_role` (`weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('30', '2018-02-27 03:02:00', '2018-03-02 09:24:00', '1', '2', '30');
INSERT IGNORE INTO `k8s_auth_server`.`resource_menu_role` (`weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('30', '2018-02-27 03:02:00', '2018-03-02 09:24:00', '1', '3', '30');
INSERT IGNORE INTO `k8s_auth_server`.`resource_menu_role` (`weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('30', '2018-02-27 03:02:00', '2018-03-02 09:24:00', '1', '4', '30');
INSERT IGNORE INTO `k8s_auth_server`.`resource_menu_role` (`weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('30', '2018-02-27 03:02:00', '2018-03-02 09:24:00', '1', '5', '30');
INSERT IGNORE INTO `k8s_auth_server`.`resource_menu_role` (`weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('30', '2018-02-27 03:02:00', '2018-03-02 09:24:00', '1', '6', '30');
INSERT IGNORE INTO `k8s_auth_server`.`resource_menu_role` (`weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('30', '2018-02-27 03:02:00', '2018-03-02 09:24:00', '1', '7', '30');

INSERT IGNORE INTO `k8s_auth_server`.`resource_menu_role` (`weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('31', '2018-02-12 05:52:00', '2018-02-12 14:54:00', '1', '1', '31');
INSERT IGNORE INTO `k8s_auth_server`.`resource_menu_role` (`weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('31', '2018-02-27 03:02:00', '2018-03-02 09:24:00', '0', '2', '31');
INSERT IGNORE INTO `k8s_auth_server`.`resource_menu_role` (`weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('31', '2018-02-27 03:02:00', '2018-03-02 09:24:00', '0', '3', '31');
INSERT IGNORE INTO `k8s_auth_server`.`resource_menu_role` (`weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('31', '2018-02-27 03:02:00', '2018-03-02 09:24:00', '0', '4', '31');
INSERT IGNORE INTO `k8s_auth_server`.`resource_menu_role` (`weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('31', '2018-02-27 03:02:00', '2018-03-02 09:24:00', '0', '5', '31');
INSERT IGNORE INTO `k8s_auth_server`.`resource_menu_role` (`weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('31', '2018-02-27 03:02:00', '2018-03-02 09:24:00', '1', '6', '31');
INSERT IGNORE INTO `k8s_auth_server`.`resource_menu_role` (`weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('31', '2018-02-27 03:02:00', '2018-03-02 09:24:00', '0', '7', '31');


INSERT IGNORE INTO `k8s_auth_server`.`role_privilege_new_replication` (`status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('1', '1', '94', '2018-01-13 07:13:00', '2018-01-13 07:13:00', NULL, NULL);
INSERT IGNORE INTO `k8s_auth_server`.`role_privilege_new_replication` (`status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('0', '2', '94', '2018-01-13 07:13:00', '2018-01-13 07:13:00', NULL, NULL);
INSERT IGNORE INTO `k8s_auth_server`.`role_privilege_new_replication` (`status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('0', '3', '94', '2018-01-13 07:13:00', '2018-01-13 07:13:00', NULL, NULL);
INSERT IGNORE INTO `k8s_auth_server`.`role_privilege_new_replication` (`status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('0', '4', '94', '2018-01-13 07:13:00', '2018-03-16 06:24:00', NULL, NULL);
INSERT IGNORE INTO `k8s_auth_server`.`role_privilege_new_replication` (`status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('0', '5', '94', '2018-01-13 07:13:00', '2018-03-08 08:43:00', NULL, NULL);
INSERT IGNORE INTO `k8s_auth_server`.`role_privilege_new_replication` (`status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('1', '6', '94', '2018-01-13 07:13:00', '2018-01-13 07:13:00', NULL, NULL);
INSERT IGNORE INTO `k8s_auth_server`.`role_privilege_new_replication` (`status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('0', '7', '94', '2018-01-13 07:13:00', '2018-01-13 07:13:00', NULL, NULL);

--4月9日-end

ALTER TABLE `node_drain_progress`
MODIFY COLUMN `error_msg`  text CHARACTER SET utf8 COLLATE utf8_general_ci NULL AFTER `cluster_id`;

--4月14月
ALTER TABLE `cicd_stage_build`
ADD INDEX `build_num` (`build_num`) USING BTREE ;
ALTER TABLE `cicd_job_build`
ADD INDEX `build_num` (`build_num`) USING BTREE ;
ALTER TABLE `cicd_job`
ADD INDEX `project_id` (`project_id`, `cluster_id`) USING BTREE ,
ADD INDEX `name` (`name`) USING BTREE ;

--4月16日
ALTER TABLE `cicd_job`
ADD COLUMN `description`  varchar(1000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '流水线描述' AFTER `name`;
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

--4月17日
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/system/configs/cicd', 'system', 'system');

ALTER TABLE `configfile`
DROP INDEX `uniq_name` ,
ADD UNIQUE INDEX `uniq_name` (`cluster_id`, `project_id`, `name`, `tags`) USING BTREE ;

--4月19
UPDATE `k8s_auth_server`.`role_privilege_new_replication` SET `status`='0' WHERE pid>=81 AND pid <=84 AND role_id>1;

--更新应用商店
UPDATE k8s_auth_server.service_templates
SET deployment_content = '[{"annotation":"","clusterIP":"","containers":[{"args":[],"command":[],"configmap":[],"env":[{"key":"JAVA_OPT","value":"-Xmx512m"},{"key":"TZ","value":"Asia/Shanghai"}],"img":"onlineshop/tomcat","livenessProbe":null,"log":"","name":"tomcat","ports":[{"containerPort":"","expose":"true","port":"8080","protocol":"TCP"}],"readinessProbe":null,"resource":{"cpu":"500m","memory":"512"},"storage":[],"tag":"v8.0"}],"hostName":"","instance":"1","labels":"","logPath":"","logService":"","name":"tomcat","namespace":"","nodeSelector":"HarmonyCloud_Status=C","restartPolicy":"Always","sessionAffinity":""}]'
WHERE
	id = 1;

UPDATE k8s_auth_server.service_templates
SET deployment_content = '[{"annotation":"","clusterIP":"","containers":[{"args":[],"command":[],"configmap":[],"env":[{"key":"TZ","value":"Asia/Shanghai"}],"img":"onlineshop/websphere-traditional","livenessProbe":null,"log":"","name":"websphere","ports":[{"containerPort":"","expose":"true","port":"9043","protocol":"TCP"},{"containerPort":"","expose":"true","port":"9080","protocol":"TCP"},{"containerPort":"","expose":"true","port":"9443","protocol":"TCP"},{"containerPort":"","expose":"true","port":"9060","protocol":"TCP"}],"readinessProbe":null,"resource":{"cpu":"1000m","memory":"1024"},"securityContext":{"add":[],"drop":[],"privileged":false,"security":false},"storage":[],"tag":"8.5.5.9-install"}],"hostIPC":false,"hostName":"","hostPID":false,"instance":"1","labels":"","logPath":"","logService":"","name":"websphere","namespace":"garydemo-garyns","nodeSelector":"HarmonyCloud_Status=C","restartPolicy":"Always","sessionAffinity":""}]'
WHERE
	id = 10;


UPDATE k8s_auth_server.service_templates
SET deployment_content = '[{"annotation":"","clusterIP":"","containers":[{"args":[],"command":[],"configmap":[],"env":[{"key":"TZ","value":"Asia/Shanghai"}],"img":"onlineshop/influxdb","livenessProbe":null,"log":"","name":"influxdb2","ports":[{"containerPort":"","expose":"true","port":"8086","protocol":"TCP"},{"containerPort":"","expose":"true","port":"8083","protocol":"TCP"}],"readinessProbe":null,"resource":{"cpu":"500m","memory":"512"},"storage":[],"tag":"1.3.0-alpine"}],"hostName":"","instance":"1","labels":"","logPath":"","logService":"","name":"influxdb2","namespace":"","nodeSelector":"HarmonyCloud_Status=C","restartPolicy":"Always","sessionAffinity":""}]'
WHERE
	id = 4;

UPDATE k8s_auth_server.service_templates
SET deployment_content = '[{"annotation":"","clusterIP":"","containers":[{"args":[],"command":[],"configmap":[],"env":[{"key":"TZ","value":"Asia/Shanghai"}],"imagePullPolicy":"Always","img":"onlineshop/mongodb","livenessProbe":null,"log":"","name":"mongodb","ports":[{"containerPort":"","expose":"true","port":"27017","protocol":"TCP"}],"readinessProbe":null,"resource":{"cpu":"100m","memory":"128"},"securityContext":{"add":[],"drop":[],"privileged":false,"security":false},"storage":[],"tag":"v3.5"}],"hostIPC":false,"hostName":"","hostPID":false,"instance":"1","labels":"","logPath":"","logService":"","name":"mongodb","namespace":"","nodeSelector":"HarmonyCloud_Status=C","restartPolicy":"Always","sessionAffinity":""}]'
WHERE
	id = 7;

UPDATE k8s_auth_server.service_templates
SET deployment_content = '[{"annotation":"","clusterIP":"","containers":[{"args":[],"command":[],"configmap":[],"env":[{"key":"TZ","value":"Asia/Shanghai"}],"imagePullPolicy":"Always","img":"onlineshop/rabbitmq","livenessProbe":null,"log":"","name":"rabbitmq","ports":[{"containerPort":"","expose":"true","port":"4369","protocol":"TCP"},{"containerPort":"","expose":"true","port":"5672","protocol":"TCP"},{"containerPort":"","expose":"true","port":"5671","protocol":"TCP"},{"containerPort":"","expose":"true","port":"15672","protocol":"TCP"},{"containerPort":"","expose":"true","port":"25672","protocol":"TCP"}],"readinessProbe":null,"resource":{"cpu":"500m","memory":"512"},"securityContext":{"add":[],"drop":[],"privileged":false,"security":false},"storage":[],"tag":"3.6.11"}],"hostIPC":false,"hostName":"","hostPID":false,"instance":"1","labels":"","logPath":"","logService":"","name":"rabbitmq","namespace":"","nodeSelector":"HarmonyCloud_Status=C","restartPolicy":"Always","sessionAffinity":""}]'
WHERE
	id = 8;

ALTER TABLE `user_role_relationship`
ADD INDEX `project` (`project_id`) USING BTREE ,
ADD INDEX `username` (`username`) USING BTREE ;
ALTER TABLE `service_templates`
ADD INDEX `project_cluster` (`project_id`, `cluster_id`) USING BTREE ;

UPDATE k8s_auth_server.service_templates
SET deployment_content = '[{"annotation":"","clusterIP":"","containers":[{"args":[],"command":[],"configmap":[],"env":[{"key":"MYSQL_ROOT_PASSWORD","value":"123456"},{"key":"TZ","value":"Asia/Shanghai"}],"img":"onlineshop/mysql","livenessProbe":null,"log":"","name":"mysql","ports":[{"containerPort":"","expose":"true","port":"3306","protocol":"TCP"}],"readinessProbe":null,"resource":{"cpu":"1000m","memory":"1024"},"storage":[],"tag":"5.7.6"}],"hostName":"","instance":"1","labels":"","logPath":"","logService":"","name":"mysql","namespace":"","nodeSelector":"HarmonyCloud_Status=C","restartPolicy":"Always","sessionAffinity":""}]'
WHERE
	id = 5;

UPDATE k8s_auth_server.service_templates
SET deployment_content = '[{"annotation":"","clusterIP":"","containers":[{"args":[],"command":[],"configmap":[],"env":[{"key":"JAVA_OPTS","value":"-Xmx2048m"},{"key":"TZ","value":"Asia/Shanghai"}],"imagePullPolicy":"Always","img":"onlineshop/elasticsearch","livenessProbe":null,"log":"","name":"elasticsearch","ports":[{"containerPort":"","expose":"true","port":"9200","protocol":"TCP"},{"containerPort":"","expose":"true","port":"9300","protocol":"TCP"}],"readinessProbe":null,"resource":{"cpu":"1000m","memory":"2048"},"securityContext":{"add":[],"drop":[],"privileged":false,"security":false},"storage":[],"tag":"v2.4.1-1"}],"hostIPC":false,"hostName":"","hostPID":false,"instance":"1","labels":"","logPath":"","logService":"","name":"elasticsearch","namespace":"garydemo-garyns","nodeSelector":"HarmonyCloud_Status=C","restartPolicy":"Always","sessionAffinity":""}]'
WHERE
	id = 11;

ALTER TABLE k8s_auth_server.configfile MODIFY COLUMN item MEDIUMTEXT;

--增加应用商店内容
INSERT INTO k8s_auth_server.application_templates VALUES('12', 'RedisCluster', 'latest-v2', '', '0', 'all', 'admin', '2018-05-08 10:44:42', '2018-05-08 10:44:42', NULL, 'onlineshop/redis-master,onlineshop/redis-slave', '0', 'all', '', '');

INSERT INTO k8s_auth_server.service_templates VALUES
('12', 'redis-master', '1.0', '', '[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[],\"command\":[],\"configmap\":[],\"env\":[{\"key\":\"TZ\",\"value\":\"Asia/Shanghai\"}],\"img\":\"onlineshop/redis-master\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"redis-master\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"6379\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"500m\",\"memory\":\"512\"},\"storage\":[],\"tag\":\"latest-v2\"}],\"hostName\":\"\",\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"redis-master\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]', 'onlineshop/redis-master', '0', '', '1', 'all', 'admin', '2018-05-08 10:44:14', '0', 'HarmonyCloud_Status=C', null, null);

INSERT INTO k8s_auth_server.service_templates(id, name, tag, details, deployment_content, image_list, is_public, ingress_content, status,
tenant, create_user, create_time, flag, node_selector, project_id, cluster_id)
SELECT AUTO_INCREMENT, 'redis-slave','1.0', '', '[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[],\"command\":[],\"configmap\":[],\"env\":[{\"key\":\"GET_HOSTS_FROM\",\"value\":\"env\"},{\"key\":\"TZ\",\"value\":\"Asia/Shanghai\"}],\"img\":\"onlineshop/redis-slave\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"redis-slave\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"6379\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"500m\",\"memory\":\"512\"},\"storage\":[],\"tag\":\"latest-v2\"}],\"hostName\":\"\",\"instance\":\"2\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"redis-slave\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]', 'onlineshop/redis-slave', '0', '', '1', 'all', 'admin', '2018-05-08 10:44:14', '0', 'HarmonyCloud_Status=C', null, null FROM information_schema.`TABLES` WHERE TABLE_SCHEMA='k8s_auth_server' AND TABLE_NAME='service_templates';

INSERT INTO k8s_auth_server.application_service(id, application_id, service_id, status, is_external)
 SELECT AUTO_INCREMENT, '12', '12', '0', '0' FROM information_schema.`TABLES` WHERE TABLE_SCHEMA='k8s_auth_server' AND TABLE_NAME='application_service';

 INSERT INTO k8s_auth_server.application_service(id, application_id, service_id, status, is_external)
 SELECT AUTO_INCREMENT, '12', max(k8s_auth_server.service_templates.id), '0', '0' FROM k8s_auth_server.service_templates, information_schema.`TABLES` WHERE TABLE_SCHEMA='k8s_auth_server' AND TABLE_NAME='application_service';

--更新redis应用商店的记录
UPDATE k8s_auth_server.service_templates
SET deployment_content = '[{"annotation":"","clusterIP":"","containers":[{"args":[],"command":[],"configmap":[],"env":[{"key":"TZ","value":"Asia/Shanghai"}],"img":"onlineshop/redis","livenessProbe":null,"log":"","name":"redis","ports":[{"containerPort":"","expose":"true","port":"6379","protocol":"TCP"}],"readinessProbe":null,"resource":{"cpu":"100m","memory":"128"},"storage":[],"tag":"3.2-alpine"}],"hostName":"","instance":"1","labels":"","logPath":"","logService":"","name":"redis","namespace":"","nodeSelector":"HarmonyCloud_Status=C","restartPolicy":"Always","sessionAffinity":""}]'
WHERE
	id = 2;