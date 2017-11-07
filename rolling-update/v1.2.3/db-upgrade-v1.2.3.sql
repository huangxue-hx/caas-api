--添加字段：区分应用模板公私有
ALTER TABLE `k8s_auth_server`.`business_templates` 
ADD COLUMN `is_public` TINYINT NULL DEFAULT 0 AFTER `image_list`;
--添加字段：区分服务模板公私有9
ALTER TABLE `k8s_auth_server`.`service_templates` 
ADD COLUMN `is_public` TINYINT NULL DEFAULT 0 AFTER `image_list`;
--修改websphere模板
update k8s_auth_server.service_templates set deployment_content='[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[],\"command\":[],\"configmap\":[],\"env\":[],\"img\":\"onlineshop/websphere-traditional\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"websphere\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"9043\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"9080\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"1000m\",\"memory\":\"1024\"},\"securityContext\":{\"add\":[],\"drop\":[],\"privileged\":false,\"security\":false},\"storage\":[],\"tag\":\"8.5.5.9-install\"}],\"hostIPC\":false,\"hostName\":\"\",\"hostPID\":false,\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"websphere\",\"namespace\":\"garydemo-garyns\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]' where id>0 and name='websphere';

insert into k8s_auth_server.system_config(config_name, config_value,config_type,create_user,update_user,create_time,update_time) values('apm_master_ip', 'harmonycloud', 'fulllink', 'admin', 'admin', now(),now());
insert into k8s_auth_server.system_config(config_name, config_value,config_type,create_user,update_user,create_time,update_time) values('apm_http_url', 'https://apm.harmonycloud.cn', 'fulllink', 'admin', 'admin', now(),now());
insert into k8s_auth_server.system_config(config_name, config_value,config_type,create_user,update_user,create_time,update_time) values('apm_username', 'hcapm', 'fulllink', 'admin', 'admin', now(),now());
insert into k8s_auth_server.system_config(config_name, config_value,config_type,create_user,update_user,create_time,update_time) values('apm_password', '1qaz@WSX', 'fulllink', 'admin', 'admin', now(),now());

--OAM 告警表结构更改：
ALTER TABLE `k8s_server_mysql`.`alarm_record`
ADD COLUMN `receiver_usernames` VARCHAR(100) NULL COMMENT '告警接收人用户名' AFTER `receiver_names`;

--对接日志全链路,添加日志查询模块：
INSERT INTO `k8s_auth_server`.`role_privilege` (`id`, `role`, `update_time`, `first_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`) VALUES ('883', 'dev', '2017-11-07 10:36:05', 'logQuery', '1', '日志查询', '354', '1', '357');
INSERT INTO `k8s_auth_server`.`role_privilege` (`id`, `role`, `privilege`, `update_time`, `first_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`) VALUES ('884', 'dev', 'list', '2017-11-07 10:36:05', 'logQuery', '1', '日志查询', '357', '0', '358');
INSERT INTO `k8s_auth_server`.`role_privilege` (`id`, `role`, `update_time`, `first_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`) VALUES ('885', 'default', '2017-11-07 10:36:05', 'logQuery', '1', '日志查询', '354', '1', '357');
INSERT INTO `k8s_auth_server`.`role_privilege` (`id`, `role`, `privilege`, `update_time`, `first_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`) VALUES ('886', 'default', 'list', '2017-11-07 10:36:05', 'logQuery', '1', '日志查询', '357', '0', '358');
INSERT INTO `k8s_auth_server`.`role_privilege` (`id`, `role`, `update_time`, `first_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`) VALUES ('887', 'ops', '2017-11-07 10:36:05', 'logQuery', '1', '日志查询', '354', '1', '357');
INSERT INTO `k8s_auth_server`.`role_privilege` (`id`, `role`, `privilege`, `update_time`, `first_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`) VALUES ('888', 'ops', 'list', '2017-11-07 10:36:05', 'logQuery', '1', '日志查询', '357', '0', '358');
INSERT INTO `k8s_auth_server`.`role_privilege` (`id`, `role`, `update_time`, `first_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`) VALUES ('889', 'tester', '2017-11-07 10:36:05', 'logQuery', '1', '日志查询', '354', '1', '357');
INSERT INTO `k8s_auth_server`.`role_privilege` (`id`, `role`, `privilege`, `update_time`, `first_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`) VALUES ('890', 'tester', 'list', '2017-11-07 10:36:05', 'logQuery', '1', '日志查询', '357', '0', '358');
INSERT INTO `k8s_auth_server`.`role_privilege` (`id`, `role`, `update_time`, `first_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`) VALUES ('891', 'tm', '2017-11-07 10:36:05', 'logQuery', '1', '日志查询', '354', '1', '357');
INSERT INTO `k8s_auth_server`.`role_privilege` (`id`, `role`, `privilege`, `update_time`, `first_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`) VALUES ('892', 'tm', 'list', '2017-11-07 10:36:05', 'logQuery', '1', '日志查询', '357', '0', '358');
INSERT INTO `k8s_auth_server`.`role_privilege` (`id`, `role`, `update_time`, `first_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`) VALUES ('893', 'admin', '2017-11-07 10:36:05', 'logQuery', '1', '日志查询', '354', '1', '357');
INSERT INTO `k8s_auth_server`.`role_privilege` (`id`, `role`, `privilege`, `update_time`, `first_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`) VALUES ('894', 'admin', 'list', '2017-11-07 10:36:05', 'logQuery', '1', '日志查询', '357', '0', '358');
UPDATE `k8s_auth_server`.`role_privilege` SET `parent_rpid`='0' WHERE `id`='715';
UPDATE `k8s_auth_server`.`role_privilege` SET `parent_rpid`='0' WHERE `id`='569';
UPDATE `k8s_auth_server`.`role_privilege` SET `parent_rpid`='0' WHERE `id`='277';
UPDATE `k8s_auth_server`.`role_privilege` SET `parent_rpid`='0' WHERE `id`='131';
UPDATE `k8s_auth_server`.`role_privilege` SET `parent_rpid`='0' WHERE `id`='423';


INSERT INTO `k8s_auth_server`.`role_privilege_custom` (`id`, `role`, `update_time`, `first_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`) VALUES ('883', 'dev', '2017-11-07 10:36:05', 'logQuery', '1', '日志查询', '354', '1', '357');
INSERT INTO `k8s_auth_server`.`role_privilege_custom` (`id`, `role`, `privilege`, `update_time`, `first_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`) VALUES ('884', 'dev', 'list', '2017-11-07 10:36:05', 'logQuery', '1', '日志查询', '357', '0', '358');
INSERT INTO `k8s_auth_server`.`role_privilege_custom` (`id`, `role`, `update_time`, `first_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`) VALUES ('885', 'default', '2017-11-07 10:36:05', 'logQuery', '1', '日志查询', '354', '1', '357');
INSERT INTO `k8s_auth_server`.`role_privilege_custom` (`id`, `role`, `privilege`, `update_time`, `first_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`) VALUES ('886', 'default', 'list', '2017-11-07 10:36:05', 'logQuery', '1', '日志查询', '357', '0', '358');
INSERT INTO `k8s_auth_server`.`role_privilege_custom` (`id`, `role`, `update_time`, `first_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`) VALUES ('887', 'ops', '2017-11-07 10:36:05', 'logQuery', '1', '日志查询', '354', '1', '357');
INSERT INTO `k8s_auth_server`.`role_privilege_custom` (`id`, `role`, `privilege`, `update_time`, `first_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`) VALUES ('888', 'ops', 'list', '2017-11-07 10:36:05', 'logQuery', '1', '日志查询', '357', '0', '358');
INSERT INTO `k8s_auth_server`.`role_privilege_custom` (`id`, `role`, `update_time`, `first_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`) VALUES ('889', 'tester', '2017-11-07 10:36:05', 'logQuery', '1', '日志查询', '354', '1', '357');
INSERT INTO `k8s_auth_server`.`role_privilege_custom` (`id`, `role`, `privilege`, `update_time`, `first_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`) VALUES ('890', 'tester', 'list', '2017-11-07 10:36:05', 'logQuery', '1', '日志查询', '357', '0', '358');
INSERT INTO `k8s_auth_server`.`role_privilege_custom` (`id`, `role`, `update_time`, `first_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`) VALUES ('891', 'tm', '2017-11-07 10:36:05', 'logQuery', '1', '日志查询', '354', '1', '357');
INSERT INTO `k8s_auth_server`.`role_privilege_custom` (`id`, `role`, `privilege`, `update_time`, `first_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`) VALUES ('892', 'tm', 'list', '2017-11-07 10:36:05', 'logQuery', '1', '日志查询', '357', '0', '358');
INSERT INTO `k8s_auth_server`.`role_privilege_custom` (`id`, `role`, `update_time`, `first_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`) VALUES ('893', 'admin', '2017-11-07 10:36:05', 'logQuery', '1', '日志查询', '354', '1', '357');
INSERT INTO `k8s_auth_server`.`role_privilege_custom` (`id`, `role`, `privilege`, `update_time`, `first_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`) VALUES ('894', 'admin', 'list', '2017-11-07 10:36:05', 'logQuery', '1', '日志查询', '357', '0', '358');
UPDATE `k8s_auth_server`.`role_privilege_custom` SET `parent_rpid`='0' WHERE `id`='715';
UPDATE `k8s_auth_server`.`role_privilege_custom` SET `parent_rpid`='0' WHERE `id`='569';
UPDATE `k8s_auth_server`.`role_privilege_custom` SET `parent_rpid`='0' WHERE `id`='277';
UPDATE `k8s_auth_server`.`role_privilege_custom` SET `parent_rpid`='0' WHERE `id`='131';
UPDATE `k8s_auth_server`.`role_privilege_custom` SET `parent_rpid`='0' WHERE `id`='423';


UPDATE `k8s_auth_server`.`resource` SET `available`='1' WHERE `id`='22';
UPDATE `k8s_auth_server`.`resource` SET `available`='1' WHERE `id`='37';
UPDATE `k8s_auth_server`.`resource` SET `parent_rpid`='-5' WHERE `id`='51';
INSERT INTO `k8s_auth_server`.`resource` (`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `available`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES ('163', '日志查询', 'menu', 'logQuery', '7', '7/22', '22', '1', '', 'dev', '0', '5', '22');
UPDATE `k8s_auth_server`.`resource` SET `available`='1' WHERE `id`='67';
UPDATE `k8s_auth_server`.`resource` SET `parent_rpid`='-7' WHERE `id`='81';
INSERT INTO `k8s_auth_server`.`resource` (`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `available`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES ('164', '日志查询', 'menu', 'logQuery', '7', '7/22', '22', '1', '', 'ops', '0', '7', '25');
UPDATE `k8s_auth_server`.`resource` SET `parent_rpid`='-7' WHERE `id`='105';
UPDATE `k8s_auth_server`.`resource` SET `available`='1' WHERE `id`='115';
UPDATE `k8s_auth_server`.`resource` SET `parent_rpid`='-5' WHERE `id`='129';
INSERT INTO `k8s_auth_server`.`resource` (`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `available`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES ('165', '日志查询', 'menu', 'logQuery', '7', '7/22', '22', '1', '', 'tm', '0', '5', '25');
UPDATE `k8s_auth_server`.`resource` SET `available`='1' WHERE `id`='143';
UPDATE `k8s_auth_server`.`resource` SET `parent_rpid`='-7' WHERE `id`='157';
UPDATE `k8s_auth_server`.`resource` SET `available`='1' WHERE `id`='158';

UPDATE `k8s_auth_server`.`resource_custom` SET `available`='1' WHERE `id`='22';
UPDATE `k8s_auth_server`.`resource_custom` SET `available`='1' WHERE `id`='37';
UPDATE `k8s_auth_server`.`resource_custom` SET `parent_rpid`='-5' WHERE `id`='51';
INSERT INTO `k8s_auth_server`.`resource_custom` (`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `available`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES ('163', '日志查询', 'menu', 'logQuery', '7', '7/22', '22', '1', '', 'dev', '0', '5', '22');
UPDATE `k8s_auth_server`.`resource_custom` SET `available`='1' WHERE `id`='67';
UPDATE `k8s_auth_server`.`resource_custom` SET `parent_rpid`='-7' WHERE `id`='81';
INSERT INTO `k8s_auth_server`.`resource_custom` (`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `available`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES ('164', '日志查询', 'menu', 'logQuery', '7', '7/22', '22', '1', '', 'ops', '0', '7', '25');
UPDATE `k8s_auth_server`.`resource_custom` SET `parent_rpid`='-7' WHERE `id`='105';
UPDATE `k8s_auth_server`.`resource_custom` SET `available`='1' WHERE `id`='115';
UPDATE `k8s_auth_server`.`resource_custom` SET `parent_rpid`='-5' WHERE `id`='129';
INSERT INTO `k8s_auth_server`.`resource_custom` (`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `available`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES ('165', '日志查询', 'menu', 'logQuery', '7', '7/22', '22', '1', '', 'tm', '0', '5', '25');
UPDATE `k8s_auth_server`.`resource_custom` SET `available`='1' WHERE `id`='143';
UPDATE `k8s_auth_server`.`resource_custom` SET `parent_rpid`='-7' WHERE `id`='157';
UPDATE `k8s_auth_server`.`resource_custom` SET `available`='1' WHERE `id`='158';


UPDATE `k8s_auth_server`.`resource` SET `parent_rpid`='5' WHERE `id`='51';
UPDATE `k8s_auth_server`.`resource` SET `parent_rpid`='7' WHERE `id`='81';
UPDATE `k8s_auth_server`.`resource` SET `parent_rpid`='7' WHERE `id`='105';
UPDATE `k8s_auth_server`.`resource` SET `parent_rpid`='5' WHERE `id`='129';
UPDATE `k8s_auth_server`.`resource` SET `parent_rpid`='7' WHERE `id`='157';


UPDATE `k8s_auth_server`.`resource_custom` SET `parent_rpid`='5' WHERE `id`='51';
UPDATE `k8s_auth_server`.`resource_custom` SET `parent_rpid`='7' WHERE `id`='81';
UPDATE `k8s_auth_server`.`resource_custom` SET `parent_rpid`='7' WHERE `id`='105';
UPDATE `k8s_auth_server`.`resource_custom` SET `parent_rpid`='5' WHERE `id`='129';
UPDATE `k8s_auth_server`.`resource_custom` SET `parent_rpid`='7' WHERE `id`='157';

--对接日志全链路,修改操作审计url：
UPDATE `k8s_auth_server`.`resource` SET `url`='adminAudit' WHERE `id`='21';
UPDATE `k8s_auth_server`.`resource` SET `url`='adminAudit' WHERE `id`='51';
UPDATE `k8s_auth_server`.`resource` SET `url`='adminAudit' WHERE `id`='81';
UPDATE `k8s_auth_server`.`resource` SET `url`='adminAudit' WHERE `id`='105';
UPDATE `k8s_auth_server`.`resource` SET `url`='adminAudit' WHERE `id`='129';
UPDATE `k8s_auth_server`.`resource` SET `url`='adminAudit' WHERE `id`='157';

UPDATE `k8s_auth_server`.`resource_custom` SET `url`='adminAudit' WHERE `id`='21';
UPDATE `k8s_auth_server`.`resource_custom` SET `url`='adminAudit' WHERE `id`='51';
UPDATE `k8s_auth_server`.`resource_custom` SET `url`='adminAudit' WHERE `id`='81';
UPDATE `k8s_auth_server`.`resource_custom` SET `url`='adminAudit' WHERE `id`='105';
UPDATE `k8s_auth_server`.`resource_custom` SET `url`='adminAudit' WHERE `id`='129';
UPDATE `k8s_auth_server`.`resource_custom` SET `url`='adminAudit' WHERE `id`='157';