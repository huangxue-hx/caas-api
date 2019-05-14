use k8s_auth_server;

ALTER TABLE `k8s_auth_server`.`url_dic`
ADD UNIQUE INDEX `url_UNIQUE` (`url` ASC);

UPDATE `k8s_auth_server`.`user` SET `real_name`='admin' WHERE `id`='1';

CREATE TABLE `k8s_auth_server`.`configfile_item` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id主键,自动生成',
  `configfile_id` varchar(64) NOT NULL COMMENT '外键,配置id',
  `path` varchar(512) DEFAULT NULL COMMENT '路径',
  `content` mediumtext NOT NULL COMMENT '文件内容',
  `file_name` varchar(128) DEFAULT NULL COMMENT '文件名',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT into k8s_auth_server.configfile_item(`configfile_id`, `path`, `content`, `file_name`) select id,path,item,name FROM `k8s_auth_server`.configfile;
update configfile_item set file_name = REVERSE(left(REVERSE(path),instr(REVERSE(path),'/')-1)) where id>0;
update configfile_item set path = left(path,LENGTH(path)-instr(REVERSE(path),'/')+1) where id > 0;

ALTER TABLE k8s_auth_server.`configfile`
  DROP COLUMN `item`,
  DROP COLUMN `path`;

ALTER TABLE tenant_cluster_quota ADD storage_quotas VARCHAR(512) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '集群租户的所有存储配额信息（name1_quota1_total1，name2_quota2_total2...）';

INSERT INTO system_config (config_name, config_value, config_type, create_user)
VALUES ('provisionerImageName', '/k8s-deploy/nfs-client-provisioner:v2.1.0', 'nfs-provisioner', 'admin');

INSERT INTO system_config (config_name, config_value, config_type, create_user)
VALUES ('cephRBDImageName', '/k8s-deploy/rbd-provisioner:latest', 'ceph-rbd-provisioner', 'admin');

INSERT INTO system_config (config_name, config_value, config_type, create_user)
VALUES ('recycleImageName', '/k8s-deploy/busybox', 'recycle-pod', 'admin');

INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`)
VALUES ('/tenants/*/projects/*/pvc', 'appcenter', 'volume');

INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`)
VALUES ('/tenants/*/projects/*/pvc/*/recycle', 'appcenter', 'volume');

INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`)
VALUES ('/tenants/*/projects/*/dependence/*/filelist', 'cicd', 'env');

INSERT INTO `k8s_auth_server`.`system_config`(`config_name`, `config_value`, `config_type`)
VALUES ('pdb.minAvailable', '50%', 'pdb'), ('pdb.maxUnavailable', '50%', 'pdb');

INSERT INTO `k8s_auth_server`.`url_dic`(`url`, `module`, `resource`)
VALUES ('/tenants/*/projects/*/dependence/storage', 'cicd', 'cicdmgr');

INSERT INTO system_config (config_name, config_value, config_type, create_user)
VALUES ('dependenceUploadImageName', '/k8s-deploy/centos:7', 'dependence', 'admin');

INSERT INTO system_config (config_name, config_value, config_type, create_user)
VALUES ('dependenceUploadImageCmd', '["tail","-f","/etc/hosts"]', 'dependence', 'admin');

INSERT INTO `k8s_auth_server`.`url_dic`(`url`, `module`, `resource`)
VALUES ('/privilege/group', 'whitelist', 'whitelist');

INSERT INTO `k8s_auth_server`.`url_dic`(`url`, `module`, `resource`)
VALUES ('/privilege/group/*/user', 'whitelist', 'whitelist');

INSERT INTO `k8s_auth_server`.`url_dic`(`url`, `module`, `resource`)
VALUES ('/tenants/*/strategy', 'tenant', 'tenantmgr');

INSERT INTO `k8s_auth_server`.`url_dic`(`url`, `module`, `resource`)
VALUES ('/tenants/*/projects/*/user', 'whitelist', 'whitelist');

DROP TABLE IF EXISTS `k8s_auth_server`.`data_privilege_group`;
CREATE TABLE `k8s_auth_server`.`data_privilege_group` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `type` tinyint(4) DEFAULT NULL,
  `tenant_id` varchar(255) DEFAULT NULL,
  `project_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `k8s_auth_server`.`data_privilege_group_mapping`;
CREATE TABLE `k8s_auth_server`.`data_privilege_group_mapping` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `data_name` varchar(255) DEFAULT NULL COMMENT '数据名称',
  `resource_type_id` int(11) DEFAULT NULL COMMENT '1-APPLICATION,2-SERVICE,3-CONFIGFILE,4-EXTERNALSERVICE,5-STORAGE,6-PIPELINE',
  `project_id` varchar(64) DEFAULT NULL COMMENT '项目id',
  `cluster_id` varchar(64) DEFAULT NULL COMMENT '集群id',
  `namespace` varchar(255) DEFAULT NULL COMMENT '分区',
  `privilege_type` int(11) DEFAULT NULL COMMENT '1-RO,2-RW',
  `parent_id` int(11) DEFAULT NULL COMMENT '父资源id',
  `group_id` int(11) DEFAULT NULL COMMENT '权限组id',
  `creator_id` int(11) DEFAULT NULL COMMENT '创建者',
  PRIMARY KEY (`id`),
  KEY `data_INDEX` (`data_name`,`resource_type_id`,`namespace`,`privilege_type`,`project_id`,`cluster_id`) USING BTREE,
  KEY `groupid_INDEX` (`group_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `k8s_auth_server`.`data_privilege_group_member`;
CREATE TABLE `k8s_auth_server`.`data_privilege_group_member` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `group_id` int(11) DEFAULT NULL COMMENT '权限组id',
  `member_type` int(11) DEFAULT NULL COMMENT '组员类型，0-人，1-组',
  `member_id` int(11) DEFAULT NULL COMMENT '组员id',
  `username` varchar(255) DEFAULT NULL COMMENT '组员用户名',
  PRIMARY KEY (`id`),
  KEY `GROUP_TYPE_ID_INDEX` (`group_id`,`member_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `k8s_auth_server`.`data_privilege_strategy`;
CREATE TABLE `k8s_auth_server`.`data_privilege_strategy` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `scope_type` tinyint(4) DEFAULT NULL COMMENT '0-tenant',
  `scope_id` varchar(64) DEFAULT NULL COMMENT '权限范围对象id',
  `resource_type_id` varchar(255) DEFAULT NULL,
  `strategy` tinyint(4) DEFAULT NULL COMMENT '1-封闭,2-半开放,3-开放',
  PRIMARY KEY (`id`),
  KEY `strategy_INDEX` (`scope_id`,`scope_type`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `k8s_auth_server`.`data_resource_url`;
CREATE TABLE `k8s_auth_server`.`data_resource_url` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `url` varchar(255) DEFAULT NULL COMMENT '数据权限拦截接口',
  `method` varchar(255) DEFAULT NULL COMMENT '接口方法',
  `resource_type_id` int(11) DEFAULT NULL COMMENT '1-APPLICATION,2-SERVICE,3-CONFIGFILE,4-EXTERNALSERVICE,5-STORAGE,6-PIPELINE',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


INSERT INTO `k8s_auth_server`.`data_resource_url` VALUES ('1', '/tenants/*/projects/*/apps/*/deploys', 'POST', '1');
INSERT INTO `k8s_auth_server`.`data_resource_url` VALUES ('2', '/tenants/*/projects/*/apps/*', 'GET', '1');
INSERT INTO `k8s_auth_server`.`data_resource_url` VALUES ('3', '/tenants/*/projects/*/apps/*', 'DELETE', '1');
INSERT INTO `k8s_auth_server`.`data_resource_url` VALUES ('4', '/tenants/*/projects/*/apps/*/start', 'POST', '1');
INSERT INTO `k8s_auth_server`.`data_resource_url` VALUES ('5', '/tenants/*/projects/*/apps/*/stop', 'POST', '1');
INSERT INTO `k8s_auth_server`.`data_resource_url` VALUES ('6', '/tenants/*/projects/*/deploys/*/autoscale', 'PUT', '2');
INSERT INTO `k8s_auth_server`.`data_resource_url` VALUES ('7', '/tenants/*/projects/*/deploys/*/container/file/upload', 'POST', '2');
INSERT INTO `k8s_auth_server`.`data_resource_url` VALUES ('8', '/tenants/*/projects/*/deploys/*/stop', 'POST', '2');
INSERT INTO `k8s_auth_server`.`data_resource_url` VALUES ('9', '/tenants/*/projects/*/deploys/*/canaryupdate/cancel', 'PUT', '2');
INSERT INTO `k8s_auth_server`.`data_resource_url` VALUES ('10', '/tenants/*/projects/*/deploys/*/canaryUpdate', 'PUT', '2');
INSERT INTO `k8s_auth_server`.`data_resource_url` VALUES ('11', '/tenants/*/projects/*/deploys/*/container/file/upload/history', 'POST', '2');
INSERT INTO `k8s_auth_server`.`data_resource_url` VALUES ('12', '/tenants/*/projects/*/deploys/*/bluegreen', 'GET', '2');
INSERT INTO `k8s_auth_server`.`data_resource_url` VALUES ('13', '/tenants/*/projects/*/deploys/*/bluegreen/switchflow', 'POST', '2');
INSERT INTO `k8s_auth_server`.`data_resource_url` VALUES ('14', '/tenants/*/projects/*/deploys/*/autoscale', 'POST', '2');
INSERT INTO `k8s_auth_server`.`data_resource_url` VALUES ('15', '/tenants/*/projects/*/deploys/*/autoscale', 'DELETE', '2');
INSERT INTO `k8s_auth_server`.`data_resource_url` VALUES ('16', '/tenants/*/projects/*/deploys/*/pods', 'GET', '2');
INSERT INTO `k8s_auth_server`.`data_resource_url` VALUES ('17', '/tenants/*/projects/*/deploys/*/bluegreen/confirm', 'POST', '2');
INSERT INTO `k8s_auth_server`.`data_resource_url` VALUES ('18', '/tenants/*/projects/*/deploys/*/start', 'POST', '2');
INSERT INTO `k8s_auth_server`.`data_resource_url` VALUES ('19', '/tenants/*/projects/*/deploys/*/canaryupdate/resume', 'PUT', '2');
INSERT INTO `k8s_auth_server`.`data_resource_url` VALUES ('20', '/tenants/*/projects/*/deploys/*/ingress', 'POST', '2');
INSERT INTO `k8s_auth_server`.`data_resource_url` VALUES ('21', '/tenants/*/projects/*/deploys/*/containers', 'GET', '2');
INSERT INTO `k8s_auth_server`.`data_resource_url` VALUES ('22', '/tenants/*/projects/*/deploys/*/container/files', 'GET', '2');
INSERT INTO `k8s_auth_server`.`data_resource_url` VALUES ('23', '/tenants/*/projects/*/deploys/*/updatestatus', 'GET', '2');
INSERT INTO `k8s_auth_server`.`data_resource_url` VALUES ('24', '/tenants/*/projects/*/deploys/*/container/file/uploadToNode', 'POST', '2');
INSERT INTO `k8s_auth_server`.`data_resource_url` VALUES ('25', '/tenants/*/projects/*/deploys/*/canaryrollback', 'PUT', '2');
INSERT INTO `k8s_auth_server`.`data_resource_url` VALUES ('26', '/tenants/*/projects/*/deploys/*/scale', 'POST', '2');
INSERT INTO `k8s_auth_server`.`data_resource_url` VALUES ('27', '/tenants/*/projects/*/deploys/*/events', 'GET', '2');
INSERT INTO `k8s_auth_server`.`data_resource_url` VALUES ('28', '/tenants/*/projects/*/deploys/*/bluegreen/cancel', 'POST', '2');
INSERT INTO `k8s_auth_server`.`data_resource_url` VALUES ('29', '/tenants/*/projects/*/deploys/*/reversions/detail', 'GET', '2');
INSERT INTO `k8s_auth_server`.`data_resource_url` VALUES ('30', '/tenants/*/projects/*/deploys/rules', 'GET', '2');
INSERT INTO `k8s_auth_server`.`data_resource_url` VALUES ('31', '/tenants/*/projects/*/deploys/*', 'GET', '2');
INSERT INTO `k8s_auth_server`.`data_resource_url` VALUES ('32', '/tenants/*/projects/*/deploys/*', 'PUT', '2');
INSERT INTO `k8s_auth_server`.`data_resource_url` VALUES ('33', '/tenants/*/projects/*/deploys/*', 'DELETE', '2');
INSERT INTO `k8s_auth_server`.`data_resource_url` VALUES ('34', '/tenants/*/projects/*/deploys/*/rules', 'PUT', '2');
INSERT INTO `k8s_auth_server`.`data_resource_url` VALUES ('35', '/tenants/*/projects/*/deploys/*/rules', 'DELETE', '2');
INSERT INTO `k8s_auth_server`.`data_resource_url` VALUES ('36', '/tenants/*/projects/*/deploys/*/ingress', 'DELETE', '2');
INSERT INTO `k8s_auth_server`.`data_resource_url` VALUES ('37', '/tenants/*/projects/*/deploys/*/bluegreen', 'PUT', '2');

INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/roles/initHarborRole', 'system', 'system');

ALTER TABLE `k8s_auth_server`.`user`
ADD COLUMN `is_ldap_user` TINYINT(1) NULL COMMENT '是否通过ldap登录记录的用户' AFTER `phone`;

INSERT INTO `k8s_auth_server`.`system_config` (config_name, config_value, config_type, create_user)
VALUES ('cicd_type_merge', 'false', 'cicd', 'admin');

ALTER TABLE `k8s_auth_server`.`cicd_stage_type` ADD COLUMN `index` TINYINT(4);
ALTER TABLE `k8s_auth_server`.`cicd_stage_type` ADD COLUMN `status` TINYINT(4);
UPDATE `k8s_auth_server`.`cicd_stage_type` SET `index`=1, `status`=1 WHERE template_type=0;
UPDATE `k8s_auth_server`.`cicd_stage_type` SET `index`=2, `status`=1 WHERE template_type=3;
UPDATE `k8s_auth_server`.`cicd_stage_type` SET `index`=3, `status`=1 WHERE template_type=1;
UPDATE `k8s_auth_server`.`cicd_stage_type` SET `index`=4, `status`=1 WHERE template_type=7;
UPDATE `k8s_auth_server`.`cicd_stage_type` SET `index`=10, `status`=1 WHERE template_type=6;
UPDATE `k8s_auth_server`.`cicd_stage_type` SET `index`=8, `status`=1 WHERE template_type=2;
UPDATE `k8s_auth_server`.`cicd_stage_type` SET `index`=9, `status`=1 WHERE template_type=8;
UPDATE `k8s_auth_server`.`cicd_stage_type` SET `index`=10, `status`=1 WHERE template_type=6;

ALTER TABLE k8s_auth_server.`user` ADD
login_fail_time VARCHAR(255) COMMENT '登陆失败时间',
ADD login_fail_count TINYINT(1)COMMENT '登陆失败次数';

INSERT INTO k8s_auth_server.`system_config` (config_name,config_value,config_type,create_user) VALUES ('loginFailTimeLimit','1800','login','admin');
INSERT INTO k8s_auth_server.`system_config` (config_name,config_value,config_type,create_user)  VALUES ('loginFailCountLimit','10','login','admin');
INSERT INTO k8s_auth_server.`system_config` (config_name,config_value,config_type,create_user)  VALUES ('SingleTimeLimit','60','login','admin');

UPDATE k8s_auth_server.url_dic SET module='whitelist',resource='whitelist' WHERE url = '/users/*/password';

INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/tenants/*/projects/*/configmap/*/services', 'appcenter', 'configmap');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/tenants/*/projects/*/configmap/*/deploy', 'appcenter', 'configmap');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/tenants/*/projects/*/configmap/*/tags', 'appcenter', 'configmap');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/tenants/*/projects/*/configmap/*/detail', 'appcenter', 'configmap');

INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/privilege/groupNames', 'whitelist', 'whitelist');

ALTER TABLE `k8s_auth_server`.`configfile`
ADD COLUMN `update_time` VARCHAR(255) AFTER `create_time`;


ALTER TABLE `k8s_auth_server`.`cicd_stage`
ADD COLUMN `dest_cluster_id` VARCHAR(255) NULL AFTER `update_time`;

ALTER TABLE `k8s_auth_server`.`cicd_stage`
ADD COLUMN `repository_id` VARCHAR(255) NULL AFTER `dest_cluster_id`;

INSERT INTO `k8s_auth_server`.`url_dic` (url,module,resource) VALUE ('/tenants/*/clusterquotas','tenant','tenantmgr');

INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`)
VALUES ('/tenants/*/projects/*/statefulsets', 'appcenter', 'app');

INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`)
VALUES ('/tenants/*/projects/*/statefulsets/*', 'appcenter', 'app');

INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`)
VALUES ('/tenants/*/projects/*/statefulsets/*/start', 'appcenter', 'app');

INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`)
VALUES ('/tenants/*/projects/*/statefulsets/*/stop', 'appcenter', 'app');

INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`)
VALUES ('/tenants/*/projects/*/statefulsets/*/scale', 'appcenter', 'app');

INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`)
VALUES ('/tenants/*/projects/*/statefulsets/*/containers', 'appcenter', 'app');

INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`)
VALUES ('/tenants/*/projects/*/statefulsets/*/events', 'appcenter', 'app');

INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`)
VALUES ('/tenants/*/projects/*/statefulsets/*/pods', 'appcenter', 'app');

INSERT INTO k8s_auth_server.`url_dic` (`url`,`module`,`resource`) VALUES ('/tenants/*/projects/*/deploys/*/applogs/containerfiles','log','applog');

INSERT INTO `k8s_auth_server`.`url_dic` ( `url`, `module`, `resource`) VALUES ('/clusters/storages', 'infrastructure', 'clustermgr');

INSERT INTO `k8s_auth_server`.`url_dic` ( `url`, `module`, `resource`) VALUES ('/tenants/*/projects/*/repositories/*/briefinfo', 'delivery', 'repository');

INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/clusters/*/ingressController', 'infrastructure', 'clustermgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/clusters/*/ingressController/portRange', 'infrastructure', 'clustermgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/tenants/*/ingressControllerNames', 'tenant', 'basic');


DROP TABLE IF EXISTS `ingress_controller_port`;
CREATE TABLE `ingress_controller_port` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `name` varchar(64) NOT NULL COMMENT 'ingress-controller名称',
  `cluster_id` varchar(64) NOT NULL COMMENT 'ingress-controller所属集群',
  `http_port` int(3) DEFAULT NULL COMMENT 'ingress-controller的http端口',
  `https_port` int(3) DEFAULT NULL COMMENT 'ingress-controller的https端口',
  `health_port` int(5) DEFAULT NULL COMMENT 'ingress-controller的health端口',
  `status_port` int(5) DEFAULT NULL COMMENT 'ingress-controller的status端口',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE `tenant_cluster_quota`
ADD COLUMN `ic_names` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '负载均衡器名称，多个以“，”分割';

INSERT INTO k8s_auth_server.`url_dic` (`url`,`module`,`resource`)
VALUES ('/clusters/*/storage','infrastructure','clustermar');

ALTER TABLE `k8s_auth_server`.`cicd_stage` MODIFY `stage_name` VARCHAR(100);


UPDATE `k8s_auth_server`.`resource_menu` SET `weight` = `weight`+1 WHERE `weight`>=14;

INSERT INTO `k8s_auth_server`.`resource_menu` (`id`,`name`, `name_en`, `type`, `url`, `weight`, `create_time`, `update_time`, `available`, `icon_name`, `isparent`, `parent_rmid`, `module`)
VALUES ('32', '有状态服务', 'StatefulSet', 'menu', 'statefulSet', '14', NOW(), NOW(), '1', '', '0', '6', 'app');

UPDATE `k8s_auth_server`.`resource_menu_role` SET `weight` = `weight`+1 WHERE `weight`>=14;

INSERT INTO `k8s_auth_server`.`resource_menu_role` (`weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`)
SELECT '14',NOW(), NOW(),'1',role_id, '32' FROM `k8s_auth_server`.`resource_menu_role` WHERE `rmid`=13 AND `available`=1 GROUP BY `role_id`;

UPDATE `k8s_auth_server`.`service_templates` SET `deployment_content`='[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[],\"command\":[],\"configmap\":[],\"env\":[{\"key\":\"TZ\",\"value\":\"Asia/Shanghai\"}],\"img\":\"onlineshop/mysql\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"mysql\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"3306\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"1000m\",\"memory\":\"1024\"},\"storage\":[],\"tag\":\"5.7.6\"}],\"hostName\":\"\",\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"mysql\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]' WHERE `id`='5';


ALTER TABLE `k8s_auth_server`.`service_templates`
ADD COLUMN `service_account` VARCHAR(255) NULL DEFAULT NULL AFTER `cluster_id`;

UPDATE `k8s_auth_server`.`service_templates` SET `deployment_content` = '[{"annotation":"","automountServiceAccountToken":false,"clusterIP":"","containers":[{"args":[],"command":[],"configmap":[],"env":[{"key":"MINIMUM_MASTER_NODES","name":"","value":"1"},{"key":"TZ","name":"","value":"Asia/Shanghai"}],"imagePullPolicy":"Always","img":"onlineshop/elasticsearch","lifecycle":null,"limit":null,"livenessProbe":null,"log":"","name":"elasticsearch","ports":[{"containerPort":"","expose":"true","port":"9200","protocol":"TCP"},{"containerPort":"","expose":"true","port":"9300","protocol":"TCP"}],"readinessProbe":null,"resource":{"cpu":"2000m","currentRate":0,"memory":"4096"},"securityContext":{"add":[],"drop":[],"privileged":false,"security":false},"storage":[{"bindOne":false,"capacity":"5Gi","clusterId":"","emptyDir":"","gitUrl":"","hostPath":"","namespace":"","path":"/data","projectId":"","pvcName":"es-data","readOnly":false,"revision":"","serviceName":"","serviceType":"","tenantId":"","type":"nfs","volumeName":""},{"bindOne":false,"capacity":"5Gi","clusterId":"","emptyDir":"","gitUrl":"","hostPath":"","namespace":"","path":"/data-backup","projectId":"","pvcName":"es-data-backup","readOnly":false,"revision":"","serviceName":"","serviceType":"","tenantId":"","type":"nfs","volumeName":""}],"syncTimeZone":false,"tag":"v6.2.5-1"}],"hostIPC":false,"hostName":"","hostNetwork":false,"hostPID":false,"instance":"1","labels":"","logPath":"","logService":"","name":"elasticsearch","namespace":"","nodeAffinity":[],"nodeSelector":"","podAffinity":null,"podAntiAffinity":null,"podDisperse":null,"projectId":"","restartPolicy":"Always","serviceAccount":"","serviceAccountName":"","sessionAffinity":""}]' WHERE (`id` = '11' AND `name` = 'elasticsearch');
UPDATE `k8s_auth_server`.`service_templates` SET `service_account` = 'onlineshop' WHERE (`id` = '11' AND `name` = 'elasticsearch');

INSERT INTO `k8s_auth_server`.`service_templates` (`name`, `tag`, `deployment_content`, `image_list`, `is_public`, `status`, `tenant`, `create_user`, `create_time`, `flag`, `node_selector`) VALUES ('rocketmq', '4.2.8', '[{\"annotation\":\"\",\"automountServiceAccountToken\":false,\"clusterIP\":\"\",\"containers\":[{\"args\":[],\"command\":[],\"configmap\":[],\"env\":[{\"key\":\"TZ\",\"name\":\"\",\"value\":\"Asia/Shanghai\"}],\"imagePullPolicy\":\"IfNotPresent\",\"img\":\"onlineshop/rocketmq-broker\",\"lifecycle\":null,\"limit\":null,\"livenessProbe\":null,\"log\":\"\",\"name\":\"rocketmq-broker\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"10909\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"10911\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"2000m\",\"currentRate\":0,\"memory\":\"4096\"},\"securityContext\":{\"add\":[],\"drop\":[],\"privileged\":false,\"security\":false},\"storage\":[{\"bindOne\":false,\"capacity\":\"5Gi\",\"clusterId\":\"\",\"emptyDir\":\"\",\"gitUrl\":\"\",\"hostPath\":\"\",\"namespace\":\"\",\"path\":\"/opt/logs\",\"projectId\":\"\",\"pvcName\":\"rocketmq-broker-logs\",\"readOnly\":false,\"revision\":\"\",\"serviceName\":\"\",\"serviceType\":\"\",\"tenantId\":\"\",\"type\":\"nfs\",\"volumeName\":\"\"},{\"bindOne\":false,\"capacity\":\"5Gi\",\"clusterId\":\"\",\"emptyDir\":\"\",\"gitUrl\":\"\",\"hostPath\":\"\",\"namespace\":\"\",\"path\":\"/opt/store\",\"projectId\":\"\",\"pvcName\":\"rocketmq-broker-store\",\"readOnly\":false,\"revision\":\"\",\"serviceName\":\"\",\"serviceType\":\"\",\"tenantId\":\"\",\"type\":\"nfs\",\"volumeName\":\"\"}],\"syncTimeZone\":false,\"tag\":\"4.2.0-k8s\"},{\"args\":[],\"command\":[],\"configmap\":[],\"env\":[{\"key\":\"TZ\",\"name\":\"\",\"value\":\"Asia/Shanghai\"}],\"imagePullPolicy\":\"IfNotPresent\",\"img\":\"onlineshop/rocketmq-namesrv\",\"lifecycle\":null,\"limit\":null,\"livenessProbe\":null,\"log\":\"\",\"name\":\"rocketmq-namesrv\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"9876\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"2000m\",\"currentRate\":0,\"memory\":\"4096\"},\"securityContext\":{\"add\":[],\"drop\":[],\"privileged\":false,\"security\":false},\"storage\":[{\"bindOne\":false,\"capacity\":\"5Gi\",\"clusterId\":\"\",\"emptyDir\":\"\",\"gitUrl\":\"\",\"hostPath\":\"\",\"namespace\":\"\",\"path\":\"/opt/logs\",\"projectId\":\"\",\"pvcName\":\"rocketmq-namesrv-logs\",\"readOnly\":false,\"revision\":\"\",\"serviceName\":\"\",\"serviceType\":\"\",\"tenantId\":\"\",\"type\":\"nfs\",\"volumeName\":\"\"},{\"bindOne\":false,\"capacity\":\"5Gi\",\"clusterId\":\"\",\"emptyDir\":\"\",\"gitUrl\":\"\",\"hostPath\":\"\",\"namespace\":\"\",\"path\":\"/opt/store\",\"projectId\":\"\",\"pvcName\":\"rocketmq-namesrv-store\",\"readOnly\":false,\"revision\":\"\",\"serviceName\":\"\",\"serviceType\":\"\",\"tenantId\":\"\",\"type\":\"nfs\",\"volumeName\":\"\"}],\"syncTimeZone\":false,\"tag\":\"4.2.0-k8s\"}],\"hostIPC\":false,\"hostName\":\"\",\"hostNetwork\":false,\"hostPID\":false,\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"rocketmq\",\"namespace\":\"\",\"nodeAffinity\":[],\"nodeSelector\":\"\",\"podAffinity\":null,\"podAntiAffinity\":null,\"podDisperse\":null,\"projectId\":\"\",\"restartPolicy\":\"Always\",\"serviceAccount\":\"\",\"serviceAccountName\":\"\",\"sessionAffinity\":\"\"}]', 'onlineshop/rocketmq-broker,onlineshop/rocketmq-namesrv', '0', '1', 'all', 'admin', '2018-08-06 18:11:03', '0', 'HarmonyCloud_Status=C');
INSERT INTO `k8s_auth_server`.`service_templates` (`name`, `tag`, `deployment_content`, `image_list`, `is_public`, `status`, `tenant`, `create_user`, `create_time`, `flag`, `node_selector`) VALUES ('mysql-master', '5.7.22', '[{\"annotation\":\"\",\"automountServiceAccountToken\":false,\"clusterIP\":\"\",\"containers\":[{\"args\":[],\"command\":[],\"configmap\":[],\"env\":[{\"key\":\"MYSQL_ROOT_PASSWORD\",\"name\":\"\",\"value\":\"123456\"},{\"key\":\"MYSQL_REPLICATION_USER\",\"name\":\"\",\"value\":\"repl\"},{\"key\":\"MYSQL_REPLICAITON_PASSWORD\",\"name\":\"\",\"value\":\"123456\"},{\"key\":\"TZ\",\"name\":\"\",\"value\":\"Asia/Shanghai\"}],\"imagePullPolicy\":\"IfNotPresent\",\"img\":\"onlineshop/mysql-master\",\"lifecycle\":null,\"limit\":null,\"livenessProbe\":null,\"log\":\"\",\"name\":\"mysql-master\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"3306\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"2000m\",\"currentRate\":0,\"memory\":\"4096\"},\"securityContext\":{\"add\":[],\"drop\":[],\"privileged\":false,\"security\":false},\"storage\":[{\"bindOne\":false,\"capacity\":\"5Gi\",\"clusterId\":\"\",\"emptyDir\":\"\",\"gitUrl\":\"\",\"hostPath\":\"\",\"namespace\":\"\",\"path\":\"/var/lib/mysql\",\"projectId\":\"\",\"pvcName\":\"mysql-master-data\",\"readOnly\":false,\"revision\":\"\",\"serviceName\":\"\",\"serviceType\":\"\",\"tenantId\":\"\",\"type\":\"nfs\",\"volumeName\":\"\"}],\"syncTimeZone\":false,\"tag\":\"0.1\"}],\"hostIPC\":false,\"hostName\":\"\",\"hostNetwork\":false,\"hostPID\":false,\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"mysql-master\",\"namespace\":\"\",\"nodeAffinity\":[],\"nodeSelector\":\"\",\"podAffinity\":null,\"podAntiAffinity\":null,\"podDisperse\":null,\"projectId\":\"\",\"restartPolicy\":\"Always\",\"serviceAccount\":\"\",\"serviceAccountName\":\"\",\"sessionAffinity\":\"\"}]', 'onlineshop/mysql-master', '0', '1', 'all', 'admin', '2018-08-06 19:06:51', '0', 'HarmonyCloud_Status=C');
INSERT INTO `k8s_auth_server`.`service_templates` (`name`, `tag`, `deployment_content`, `image_list`, `is_public`, `status`, `tenant`, `create_user`, `create_time`, `flag`, `node_selector`) VALUES ('mysql-slave', '5.7.22', '[{\"annotation\":\"\",\"automountServiceAccountToken\":false,\"clusterIP\":\"\",\"containers\":[{\"args\":[],\"command\":[],\"configmap\":[],\"env\":[{\"key\":\"MYSQL_ROOT_PASSWORD\",\"name\":\"\",\"value\":\"123456\"},{\"key\":\"MYSQL_REPLICATION_USER\",\"name\":\"\",\"value\":\"repl\"},{\"key\":\"MYSQL_REPLICAITON_PASSWORD\",\"name\":\"\",\"value\":\"123456\"},{\"key\":\"TZ\",\"name\":\"\",\"value\":\"Asia/Shanghai\"}],\"imagePullPolicy\":\"IfNotPresent\",\"img\":\"onlineshop/mysql-slave\",\"lifecycle\":null,\"limit\":null,\"livenessProbe\":null,\"log\":\"\",\"name\":\"mysql-slave\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"3306\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"2000m\",\"currentRate\":0,\"memory\":\"4096\"},\"securityContext\":{\"add\":[],\"drop\":[],\"privileged\":false,\"security\":false},\"storage\":[{\"bindOne\":false,\"capacity\":\"5Gi\",\"clusterId\":\"\",\"emptyDir\":\"\",\"gitUrl\":\"\",\"hostPath\":\"\",\"namespace\":\"\",\"path\":\"/var/lib/mysql\",\"projectId\":\"\",\"pvcName\":\"mysql-slave-data\",\"readOnly\":false,\"revision\":\"\",\"serviceName\":\"\",\"serviceType\":\"\",\"tenantId\":\"\",\"type\":\"nfs\",\"volumeName\":\"\"}],\"syncTimeZone\":false,\"tag\":\"0.1\"}],\"hostIPC\":false,\"hostName\":\"\",\"hostNetwork\":false,\"hostPID\":false,\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"mysql-slave\",\"namespace\":\"\",\"nodeAffinity\":[],\"nodeSelector\":\"\",\"podAffinity\":null,\"podAntiAffinity\":null,\"podDisperse\":null,\"projectId\":\"\",\"restartPolicy\":\"Always\",\"serviceAccount\":\"\",\"serviceAccountName\":\"\",\"sessionAffinity\":\"\"}]', 'onlineshop/mysql-slave', '0', '1', 'all', 'admin', '2018-08-06 19:06:51', '0', 'HarmonyCloud_Status=C');

INSERT INTO `k8s_auth_server`.`application_templates` (`name`, `tag`, `status`, `tenant`, `create_user`, `create_time`, `image_list`, `is_public`, `project_id`) VALUES ('Rocketmq', '4.2.8', '0', 'all', 'admin', '2018-08-06 18:11:03', 'onlineshop/rocketmq-broker,onlineshop/rocketmq-namesrv', '0', 'all');
INSERT INTO `k8s_auth_server`.`application_templates` (`name`, `tag`, `status`, `tenant`, `create_user`, `create_time`, `image_list`, `is_public`, `project_id`) VALUES ('Mysql-Cluster', '5.7.22', '0', 'all', 'admin', '2018-08-06 19:06:51', 'onlineshop/mysql-master,onlineshop/mysql-slave', '0', 'all');
UPDATE `k8s_auth_server`.`application_templates` SET `tag`='v6.2.5-1' WHERE (`id`='11' AND `name` = 'Elasticsearch');

INSERT INTO `k8s_auth_server`.`application_service` (`application_id`, `service_id`, `status`, `is_external`) VALUES((SELECT `id` FROM `k8s_auth_server`.`application_templates` WHERE (`name` = 'Rocketmq')),(SELECT `id` FROM `k8s_auth_server`.`service_templates` WHERE (`name` = 'rocketmq')),0,0);
INSERT INTO `k8s_auth_server`.`application_service` (`application_id`, `service_id`, `status`, `is_external`) VALUES((SELECT `id` FROM `k8s_auth_server`.`application_templates` WHERE (`name` = 'Mysql-Cluster')),(SELECT `id` FROM `k8s_auth_server`.`service_templates` WHERE (`name` = 'mysql-master')),0,0);
INSERT INTO `k8s_auth_server`.`application_service` (`application_id`, `service_id`, `status`, `is_external`) VALUES((SELECT `id` FROM `k8s_auth_server`.`application_templates` WHERE (`name` = 'Mysql-Cluster')),(SELECT `id` FROM `k8s_auth_server`.`service_templates` WHERE (`name` = 'mysql-slave')),0,0);


-- -----------------------------------------------sprint1---------------------------------------------------------------
DROP TABLE IF EXISTS `k8s_auth_server`.`app_store`;
CREATE TABLE `k8s_auth_server`.`app_store` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) DEFAULT NULL,
  `tag` varchar(45) DEFAULT NULL,
  `details` varchar(512) DEFAULT NULL,
  `type` varchar(45) NULL DEFAULT NULL,
  `create_user` varchar(45) DEFAULT NULL,
  `create_time` timestamp NULL DEFAULT NULL,
  `update_time` timestamp NULL DEFAULT NULL,
  `image` varchar(2048) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `k8s_auth_server`.`app_store_service`;
CREATE TABLE `k8s_auth_server`.`app_store_service` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `app_id` INT NULL,
  `service_id` INT NULL,
  PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `k8s_auth_server`.`privilege`(`id`,`module`,`module_name`, `resource`, `resource_name`, `privilege`, `privilege_name`, `status`)
VALUES(95, 'delivery', '交付中心', 'onlineshop', '应用商店', 'create', '应用商店创建', 1),
(96, 'delivery', '交付中心', 'onlineshop', '应用商店', 'update', '应用商店修改', 1),
(97, 'delivery', '交付中心', 'onlineshop', '应用商店', 'delete', '应用商店删除', 1);

INSERT INTO `k8s_auth_server`.`role_privilege_new`(`status`, `role_id`, `pid`)
SELECT CASE r.id WHEN 1 THEN 1 ELSE 0 END, r.id, p.id
FROM role_new r
JOIN privilege p WHERE p.id IN(95,96,97);

INSERT INTO `k8s_auth_server`.`role_privilege_new_replication`(`status`, `role_id`, `pid`)
SELECT CASE r.id WHEN 1 THEN 1 ELSE 0 END, r.id, p.id
FROM role_new r
JOIN privilege p WHERE r.id<=7 AND p.id IN(95,96,97);


INSERT INTO `k8s_auth_server`.`app_store`(`id`, `name`, `tag`, `type`, `create_user`, `create_time`, `image`)
VALUES
(1, 'Tomcat', '8.0', 'webservice', 'admin', CURRENT_TIMESTAMP, 'tomcat.png'),
(2, 'Redis', '3.2-alpine', 'database', 'admin', CURRENT_TIMESTAMP, 'redis.png'),
(3, 'WordPress', '4.8.0-php7.1-fpm-alpine', 'webservice', 'admin', CURRENT_TIMESTAMP, 'wp.png'),
(4, 'InfluxDB', 'v1.3.0', 'database', 'admin', CURRENT_TIMESTAMP, 'influxdb.png'),
(5, 'MySQL', '5.7.6', 'database', 'admin', CURRENT_TIMESTAMP, 'mysql.png'),
(6, 'Mongodb', 'v3.5', 'database', 'admin', CURRENT_TIMESTAMP, 'mongodb.png'),
(7, 'Rabbitmq', '3.6.11', 'am', 'admin', CURRENT_TIMESTAMP, 'rabbit.png'),
(8, 'Nginx', 'latest', 'webservice', 'admin', CURRENT_TIMESTAMP, 'nginx.png'),
(9, 'Websphere', '8.5.5.9-install', 'webservice', 'admin', CURRENT_TIMESTAMP, 'websphere.png'),
(10, 'Elasticsearch', 'v6.2.5-1', 'database', 'admin', CURRENT_TIMESTAMP, 'elastic.png'),
(11, 'Fabric', '0.6', 'hyper', 'admin', CURRENT_TIMESTAMP, 'fabric.png'),
(12, 'Rocketmq', '4.2.8', 'am', 'admin', CURRENT_TIMESTAMP, 'rocketmq.png'),
(13, 'Mysql-Cluster', '5.7.22', 'database', 'admin', CURRENT_TIMESTAMP, 'mysql-cluster.png');

INSERT INTO `k8s_auth_server`.`app_store_service`(`app_id`, `service_id`)
VALUES(1, 1),(2, 2),(3,3),(4,4),(5,5),(6,7),(7,8),(8,9),(9,10),(10,11),(11,13),(11,14),(11,15),(11,16),(11,17),(11,18),(11,19);
INSERT INTO `k8s_auth_server`.`app_store_service`(`app_id`, `service_id`)
SELECT 12,`id` FROM `k8s_auth_server`.`service_templates` WHERE name='rocketmq' AND `project_id` is NULL;
INSERT INTO `k8s_auth_server`.`app_store_service`(`app_id`, `service_id`)
SELECT 13,`id` FROM `k8s_auth_server`.`service_templates` WHERE name in('mysql-master','mysql-slave') AND `project_id` is NULL;

INSERT INTO `k8s_auth_server`.`url_dic`(`url`, `module`, `resource`)
VALUES
('/tenants/projects/apptemplates/validate/*', 'delivery', 'onlineshop'),
('/tenants/projects/apptemplates/*/tags', 'delivery', 'onlineshop'),
('/tenants/projects/apptemplates/upload', 'delivery', 'onlineshop');

DELETE FROM `k8s_auth_server`.`application_service`
WHERE `application_id` IN (SELECT `id` FROM `k8s_auth_server`.`application_templates` WHERE `tenant`='all' AND `project_id`='all');
DELETE FROM `k8s_auth_server`.`application_templates` WHERE `tenant`='all' AND `project_id`='all';

-- Table structure for istio_global_configure
-- ----------------------------



-- -----------------------------------------------sprint2---------------------------------------------------------------

INSERT INTO `data_resource_url` VALUES ('38', '/tenants/*/projects/*/cicdjobs/*', 'GET', '6');
INSERT INTO `data_resource_url` VALUES ('39', '/tenants/*/projects/*/cicdjobs/*/images', 'GET', '6');
INSERT INTO `data_resource_url` VALUES ('40', '/tenants/*/projects/*/cicdjobs/*/log', 'GET', '6');
INSERT INTO `data_resource_url` VALUES ('41', '/tenants/*/projects/*/cicdjobs/*/notification', 'GET', '6');
INSERT INTO `data_resource_url` VALUES ('42', '/tenants/*/projects/*/cicdjobs/*/notification', 'PUT', '6');
INSERT INTO `data_resource_url` VALUES ('43', '/tenants/*/projects/*/cicdjobs/*/parameters', 'GET', '6');
INSERT INTO `data_resource_url` VALUES ('44', '/tenants/*/projects/*/cicdjobs/*/parameters', 'POST', '6');
INSERT INTO `data_resource_url` VALUES ('45', '/tenants/*/projects/*/cicdjobs/*/rename', 'POST', '6');
INSERT INTO `data_resource_url` VALUES ('46', '/tenants/*/projects/*/cicdjobs/*/result', 'GET', '6');
INSERT INTO `data_resource_url` VALUES ('47', '/tenants/*/projects/*/cicdjobs/*/result/*/delete', 'DELETE', '6');
INSERT INTO `data_resource_url` VALUES ('48', '/tenants/*/projects/*/cicdjobs/*/stages', 'GET', '6');
INSERT INTO `data_resource_url` VALUES ('49', '/tenants/*/projects/*/cicdjobs/*/stages', 'POST', '6');
INSERT INTO `data_resource_url` VALUES ('50', '/tenants/*/projects/*/cicdjobs/*/stages', 'PUT', '6');
INSERT INTO `data_resource_url` VALUES ('51', '/tenants/*/projects/*/cicdjobs/*/stages/*', 'DELETE', '6');
INSERT INTO `data_resource_url` VALUES ('52', '/tenants/*/projects/*/cicdjobs/*/stages/*/log', 'GET', '6');
INSERT INTO `data_resource_url` VALUES ('53', '/tenants/*/projects/*/cicdjobs/*/stages/*/result', 'GET', '6');
INSERT INTO `data_resource_url` VALUES ('54', '/tenants/*/projects/*/cicdjobs/*/stages/updateCredentials', 'PUT', '6');
INSERT INTO `data_resource_url` VALUES ('55', '/tenants/*/projects/*/cicdjobs/*/start', 'PATCH', '6');
INSERT INTO `data_resource_url` VALUES ('56', '/tenants/*/projects/*/cicdjobs/*/stop', 'PATCH', '6');
INSERT INTO `data_resource_url` VALUES ('57', '/tenants/*/projects/*/cicdjobs/*/triggers', 'GET', '6');
INSERT INTO `data_resource_url` VALUES ('58', '/tenants/*/projects/*/cicdjobs/*/triggers', 'PUT', '6');
INSERT INTO `data_resource_url` VALUES ('59', '/tenants/*/projects/*/cicdjobs/*/yaml', 'GET', '6');
INSERT INTO `data_resource_url` VALUES ('60', '/tenants/*/projects/*/cicdjobs/*', 'PUT', '6');
INSERT INTO `data_resource_url` VALUES ('61', '/tenants/*/projects/*/configmap/*/detail', 'GET', '3');
INSERT INTO `data_resource_url` VALUES ('62', '/tenants/*/projects/*/configmap/*/deploy', 'POST', '3');
INSERT INTO `data_resource_url` VALUES ('63', '/tenants/*/projects/*/configmap/*/services', 'GET', '3');
INSERT INTO `data_resource_url` VALUES ('64', '/tenants/*/projects/*/configmap/*/tags', 'GET', '3');
INSERT INTO `data_resource_url` VALUES ('65', '/tenants/*/projects/*/configmap', 'DELETE', '3');
INSERT INTO `data_resource_url` VALUES ('66', '/tenants/*/projects/*/configmap', 'PUT', '3');
INSERT INTO `data_resource_url` VALUES ('67', '/tenants/*/projects/*/configmap', 'POST', '3');
INSERT INTO `data_resource_url` VALUES ('68', '/tenants/*/projects/*/configmap/*', 'DELETE', '3');
INSERT INTO `data_resource_url` VALUES ('69', '/tenants/*/projects/*/configmap/latest', 'GET', '3');
update k8s_auth_server.url_dic set url ='/clusters/*/ingresscontrollers' where url='/clusters/*/ingressController';
update k8s_auth_server.url_dic set url ='/clusters/*/ingresscontrollers/portrange' where url='/clusters/*/ingressController/portRange';
ALTER TABLE k8s_auth_server.tenant_cluster_quota
MODIFY COLUMN `ic_names`  varchar(512) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '负载均衡器名称，多个以逗号分割' AFTER `reserve1`;
DROP TABLE IF EXISTS `ingress_controller_port`;


UPDATE `k8s_auth_server`.`cicd_stage_type` SET `index` = 4, `status`=1 WHERE `template_type`=7;
UPDATE `k8s_auth_server`.`cicd_stage_type` SET `index` = 9, `status`=1 WHERE `template_type`=8;

ALTER TABLE `k8s_auth_server`.`service_templates` ADD COLUMN `service_type` TINYINT(4) DEFAULT 0;

-- -----------------------------------------------sprint3---------------------------------------------------------------
DELETE FROM k8s_auth_server.url_dic where  url like '/tenants/*/projects/*/deploys/*/linklogs%' and id>1;
INSERT INTO k8s_auth_server.`url_dic` (`url`,`module`,`resource`) VALUES ('/tenants/*/projects/*/apps/*/linklogs/errortransactions','log','applog');
INSERT INTO k8s_auth_server.`url_dic` (`url`,`module`,`resource`) VALUES ('/tenants/*/projects/*/apps/*/linklogs/transactiontraces','log','applog');
INSERT INTO k8s_auth_server.`url_dic` (`url`,`module`,`resource`) VALUES ('/tenants/*/projects/*/apps/*/linklogs/erroranalysis','log','applog');
INSERT INTO k8s_auth_server.`url_dic` (`url`,`module`,`resource`) VALUES ('/tenants/*/projects/*/apps/*/linklogs/pod','log','applog');
INSERT INTO k8s_auth_server.`url_dic` (`url`,`module`,`resource`) VALUES ('/tenants/*/projects/*/apps/*/linklogs','log','applog');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/system/configs/localuserflag', 'whitelist', 'whitelist');

-- -----------------------------------------------Istio Begin---------------------------------------------------------------
DROP TABLE IF EXISTS `istio_global_configure`;
CREATE TABLE `istio_global_configure` (
  `id` int(4) NOT NULL AUTO_INCREMENT,
  `cluster_id` varchar(128) CHARACTER SET utf8 DEFAULT NULL COMMENT '集群id',
  `cluster_name` varchar(128) CHARACTER SET utf8 DEFAULT NULL COMMENT '集群名称',
  `switch_status` int(2) DEFAULT NULL COMMENT '开关状态(0关闭 1开启)',
  `user_name` varchar(255) DEFAULT NULL COMMENT '最近一次更新该策略用户名',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '最近一次更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 策略概览表（rule_overview）
DROP TABLE IF EXISTS `rule_overview`;
CREATE TABLE `rule_overview` (
  `rule_name` varchar(100) NOT NULL COMMENT '策略名称',
  `rule_id` varchar(32) NOT NULL COMMENT '策略关联id',
  `rule_type` varchar(20) DEFAULT NULL COMMENT '策略类型',
  `rule_scope` varchar(10) DEFAULT NULL COMMENT '策略作用范围（0：全局）',
  `rule_cluster_id` varchar(128) NOT NULL COMMENT '集群id',
  `rule_ns` varchar(128) DEFAULT NULL COMMENT '集群中命名空间',
  `rule_svc` varchar(128) DEFAULT NULL COMMENT '策略挂载服务名称',
  `rule_source_num` int(2) DEFAULT NULL COMMENT '正常状态下策略创建资源对象个数',
  `switch_status` int(1) DEFAULT 1 COMMENT '策略开关状态(0:关闭；1:开启)',
  `data_status` int(2) DEFAULT 0 COMMENT '策略状态(0:正常)',
  `data_err_loc` int(2) DEFAULT 0 COMMENT '策略异常位置',
  `user_name` varchar(255) DEFAULT NULL COMMENT '最近一次更新该策略用户名',
  `create_time` timestamp DEFAULT NULL COMMENT '策略创建时间',
  `update_time` timestamp DEFAULT NULL COMMENT '策略最近一次更新时间',
  PRIMARY KEY (`rule_cluster_id`,`rule_ns`,`rule_svc`,`rule_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='策略概览表';

-- 策略中资源信息表（rule_detail）
DROP TABLE IF EXISTS `rule_detail`;
CREATE TABLE `rule_detail` (
  `rule_id` varchar(32) NOT NULL COMMENT '策略关联id',
  `rule_detail_order` int(2) DEFAULT NULL COMMENT '资源创建顺序',
  `rule_detail_content` blob DEFAULT NULL COMMENT '策略中资源对象yaml',
  `create_time` timestamp DEFAULT NULL COMMENT '策略创建时间',
  `update_time` timestamp DEFAULT NULL COMMENT '策略最近一次更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='策略中资源信息表';

INSERT INTO `k8s_auth_server`.`url_dic`(url,module,resource) values('/clusters/*/istiopolicyswitch','appcenter','app');
INSERT INTO `k8s_auth_server`.`url_dic`(url,module,resource) values('/tenants/*/namespaces/*/istiopolicyswitch','appcenter','app');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/tenants/*/projects/*/deploys/*/istiopolicies', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/tenants/*/projects/*/deploys/*/istiopolicies/*', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/tenants/*/projects/*/deploys/*/istiopolicies/*/open', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/tenants/*/projects/*/deploys/*/istiopolicies/*/close', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/tenants/*/projects/*/serviceentries/externalserviceentry', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/tenants/*/projects/*/serviceentries/internalserviceentry', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/tenants/*/projects/*/serviceentries/*', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/tenants/*/projects/*/serviceentries', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/clusters/istiocluster', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) values('/tenants/*/namespaces/*/istiopolicies','appcenter','app');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) values('/tenants/*/projects/*/deploys/*/istiopolicies/desServiceVersions','appcenter','app');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) values('/tenants/*/projects/*/deploys/*/istiopolicies/sourceServiceVersions','appcenter','app');
INSERT INTO `k8s_auth_server`.`url_dic`(url,module,resource) values('/clusters/*/externalserviceentries','appcenter','app');
INSERT INTO `k8s_auth_server`.`url_dic`(url,module,resource) values('/clusters/*/externalserviceentries/*','appcenter','app');

-- -----------------------------------------------Istio End---------------------------------------------------------------

-- 日志查询接口更改
UPDATE `k8s_auth_server`.`url_dic` SET `url` = '/tenants/*/projects/*/apps/*/applogs/filenames' WHERE `url` = '/tenants/*/projects/*/deploys/*/applogs/filenames';
UPDATE `k8s_auth_server`.`url_dic` SET `url` = '/tenants/*/projects/*/apps/*/applogs' WHERE `url` = '/tenants/*/projects/*/deploys/*/applogs';
UPDATE `k8s_auth_server`.`url_dic` SET `url` = '/tenants/*/projects/*/apps/*/applogs/stderrlogs' WHERE `url` = '/tenants/*/projects/*/deploys/*/applogs/stderrlogs';
UPDATE `k8s_auth_server`.`url_dic` SET `url` = '/tenants/*/projects/*/apps/*/applogs/export' WHERE `url` = '/tenants/*/projects/*/deploys/*/applogs/export';
UPDATE `k8s_auth_server`.`url_dic` SET `url` = '/tenants/*/projects/*/apps/*/applogs/containerfiles' WHERE `url` = '/tenants/*/projects/*/deploys/*/applogs/containerfiles';

-- -----------------------------sprint-test2--------------------------------------------

-- 2019.1.8
-- 应用商店rbac逻辑及表结构修改
ALTER TABLE `k8s_auth_server`.`service_templates` DROP COLUMN `service_account`;
ALTER TABLE `k8s_auth_server`.`app_store` ADD COLUMN `service_account`  varchar(45) NULL AFTER `image`;

-- 2019.1.9
-- 应用商店中间件集群方案
-- delete
DELETE FROM k8s_auth_server.app_store_service where app_id = (SELECT `id` FROM `k8s_auth_server`.`app_store` WHERE `name`='Elasticsearch' AND `tag` = 'v6.2.5-1');
DELETE FROM k8s_auth_server.app_store_service where app_id = (SELECT `id` FROM `k8s_auth_server`.`app_store` WHERE `name`='Mysql-Cluster' AND `tag` = '5.7.22');
DELETE FROM k8s_auth_server.app_store WHERE `name`='Elasticsearch' AND `tag` = 'v6.2.5-1';
DELETE FROM k8s_auth_server.app_store WHERE `name`='Mysql-Cluster' AND `tag` = '5.7.22';
DELETE FROM k8s_auth_server.service_templates where `name`='elasticsearch' AND `tenant`='all';
DELETE FROM k8s_auth_server.service_templates where `name`='mysql-master' AND `tenant`='all';
DELETE FROM k8s_auth_server.service_templates where `name`='mysql-slave' AND `tenant`='all';

-- MongoDB-Cluster
INSERT INTO `k8s_auth_server`.`app_store` (`name`, `tag`, `type`, `create_user`, `create_time`, `image`, `service_account`)
VALUES ('MongoDB-Cluster', 'v3.5', 'database', 'admin', NOW(), 'k8smongodb.png', 'onlineshop');
INSERT INTO `k8s_auth_server`.`service_templates` (`name`, `tag`, `deployment_content`, `image_list`, `is_public`, `status`, `tenant`, `create_user`, `create_time`, `flag`, `node_selector`, `service_type`)
VALUES ('mongodb-replicaset', 'v3.5', '[{"annotation":"","automountServiceAccountToken":false,"clusterIP":"","containers":[{"args":[],"command":["sh","-c","mongod --replSet rs0 --bind_ip 0.0.0.0 --smallfiles --noprealloc"],"configmap":[],"env":[{"key":"TZ","name":"","type":"","value":"Asia/Shanghai"}],"imagePullPolicy":"IfNotPresent","img":"onlineshop/mongodb","lifecycle":null,"limit":null,"livenessProbe":null,"log":"","name":"mongodb","parentResourceType":"StatefulSet","ports":[{"containerPort":"","expose":"true","name":"","port":"27017","protocol":"TCP"}],"readinessProbe":null,"resource":{"cpu":"500m","currentRate":0,"gpu":"","memory":"512"},"securityContext":{"add":[],"drop":[],"privileged":false,"security":false},"storage":[{"bindOne":false,"capacity":"1Gi","clusterId":"","emptyDir":"Disk","gitUrl":"","hostPath":"","namespace":"","path":"/data/db","projectId":"","pvcName":"","readOnly":false,"revision":"","serviceName":"","serviceType":"","storageClassName":"","storageClassType":"","tenantId":"","type":"emptyDir","volumeName":"nedfwuzb"}],"syncTimeZone":false,"tag":"v3.5-1"},{"args":[],"command":[],"configmap":[],"env":[{"key":"MONGO_SIDECAR_POD_LABELS","name":"","type":"equal","value":"role=mongo,environment=env"},{"key":"KUBERNETES_MONGO_SERVICE_NAME","name":"","type":"equal","value":"mongodb-replicaset"},{"key":"TZ","name":"","type":"","value":"Asia/Shanghai"}],"imagePullPolicy":"IfNotPresent","img":"onlineshop/mongo-k8s-sidecar","lifecycle":null,"limit":null,"livenessProbe":null,"log":"","name":"mongo-k8s-sidecar","parentResourceType":"StatefulSet","ports":[{"containerPort":"","expose":"true","name":"","port":"1","protocol":"TCP"}],"readinessProbe":null,"resource":{"cpu":"100m","currentRate":0,"gpu":"","memory":"128"},"securityContext":null,"storage":[],"syncTimeZone":false,"tag":"latest"}],"deployVersion":"","hostAliases":[],"hostIPC":false,"hostName":"","hostNetwork":false,"hostPID":false,"initContainers":[],"instance":"3","labels":"role=mongo,environment=env","logPath":"","logService":"","name":"mongodb-replicaset","namespace":"","nodeAffinity":[],"nodeSelector":"","podAffinity":null,"podAntiAffinity":null,"podDisperse":{"label":"","namespace":"","namespaceAliasName":"","required":true,"type":""},"podGroupSchedule":null,"podManagementPolicy":"OrderedReady","projectId":"","pullDependence":null,"restartPolicy":"Always","serviceAccount":"","serviceAccountName":"","serviceDependence":null,"sessionAffinity":""}]', 'onlineshop/mongodb,onlineshop/mongo-k8s-sidecar', '0', '1', 'all', 'admin', NOW(), '0', 'HarmonyCloud_Status=C', '1');
INSERT INTO `k8s_auth_server`.`app_store_service` (`app_id`, `service_id`) VALUES ((SELECT `id` FROM `k8s_auth_server`.`app_store` WHERE `name`='MongoDB-Cluster' AND `tag`='v3.5'), (SELECT `id` FROM `k8s_auth_server`.`service_templates` WHERE `name`='mongodb-replicaset' AND `tag`='v3.5' AND `tenant`='all'));

-- MySQL-Cluster
INSERT INTO `k8s_auth_server`.`app_store` (`name`, `tag`, `type`, `create_user`, `create_time`, `image`, `service_account`)
VALUES ('MySQL-Cluster', '5.7', 'database', 'admin', NOW(), 'mysql-cluster.png', '');
INSERT INTO `k8s_auth_server`.`service_templates` (`name`, `tag`, `deployment_content`, `image_list`, `is_public`, `status`, `tenant`, `create_user`, `create_time`, `flag`, `node_selector`, `service_type`)
VALUES ('mysql', '5.7', '[{\"annotation\":\"\",\"automountServiceAccountToken\":false,\"clusterIP\":\"\",\"containers\":[{\"args\":[],\"command\":[],\"configmap\":[],\"env\":[{\"key\":\"MYSQL_ALLOW_EMPTY_PASSWORD\",\"name\":\"\",\"type\":\"equal\",\"value\":\"1\"},{\"key\":\"TZ\",\"name\":\"\",\"type\":\"\",\"value\":\"Asia/Shanghai\"}],\"imagePullPolicy\":\"IfNotPresent\",\"img\":\"onlineshop/mysql\",\"lifecycle\":null,\"limit\":null,\"livenessProbe\":{\"empty\":false,\"exec\":{\"command\":[\"mysqladmin\",\"ping\"]},\"failureThreshold\":10,\"httpGet\":null,\"initialDelaySeconds\":30,\"periodSeconds\":10,\"successThreshold\":1,\"tcpSocket\":null,\"timeoutSeconds\":5},\"log\":\"\",\"name\":\"mysql\",\"parentResourceType\":\"\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"name\":\"TCP\",\"port\":\"3306\",\"protocol\":\"TCP\"}],\"readinessProbe\":{\"empty\":false,\"exec\":{\"command\":[\"mysql\",\"-h\",\"127.0.0.1\",\"-e\",\"SELECT 1\"]},\"failureThreshold\":10,\"httpGet\":null,\"initialDelaySeconds\":30,\"periodSeconds\":10,\"successThreshold\":1,\"tcpSocket\":null,\"timeoutSeconds\":5},\"resource\":{\"cpu\":\"500m\",\"currentRate\":0,\"gpu\":\"\",\"memory\":\"512\"},\"securityContext\":{\"add\":[],\"drop\":[],\"privileged\":false,\"security\":false},\"storage\":[{\"bindOne\":false,\"capacity\":\"1Gi\",\"clusterId\":\"\",\"emptyDir\":\"Disk\",\"gitUrl\":\"\",\"hostPath\":\"\",\"namespace\":\"\",\"path\":\"/etc/mysql/conf.d\",\"projectId\":\"\",\"pvcName\":\"\",\"readOnly\":false,\"revision\":\"\",\"serviceName\":\"\",\"serviceType\":\"\",\"storageClassName\":\"\",\"storageClassType\":\"\",\"tenantId\":\"\",\"type\":\"emptyDir\",\"volumeName\":\"bjjtkzke\"},{\"bindOne\":false,\"capacity\":\"1Gi\",\"clusterId\":\"\",\"emptyDir\":\"Disk\",\"gitUrl\":\"\",\"hostPath\":\"\",\"namespace\":\"\",\"path\":\"/var/lib/mysql\",\"projectId\":\"\",\"pvcName\":\"\",\"readOnly\":false,\"revision\":\"\",\"serviceName\":\"\",\"serviceType\":\"\",\"storageClassName\":\"\",\"storageClassType\":\"\",\"tenantId\":\"\",\"type\":\"emptyDir\",\"volumeName\":\"rssrkfol\"}],\"syncTimeZone\":false,\"tag\":\"5.7\"},{\"args\":[],\"command\":[\"bash\",\"-c\",\"set -ex\\ncd /var/lib/mysql\\n\\nif [[ -f xtrabackup_slave_info ]]; then\\n  mv xtrabackup_slave_info change_master_to.sql.in\\n  rm -f xtrabackup_binlog_info\\nelif [[ -f xtrabackup_binlog_info ]]; then\\n  [[ `cat xtrabackup_binlog_info` =~ ^(.*?)[[:space:]]+(.*?)$ ]] || exit 1\\n  rm xtrabackup_binlog_info\\n  echo \\\"CHANGE MASTER TO MASTER_LOG_FILE=\'${BASH_REMATCH[1]}\',\\\\\\n        MASTER_LOG_POS=${BASH_REMATCH[2]}\\\" > change_master_to.sql.in\\nfi\\n\\nif [[ -f change_master_to.sql.in ]]; then\\n  echo \\\"Waiting for mysqld to be ready (accepting connections)\\\"\\n  until mysql -h 127.0.0.1 -e \\\"SELECT 1\\\"; do sleep 1; done\\n\\n  echo \\\"Initializing replication from clone position\\\"\\n  mv change_master_to.sql.in change_master_to.sql.orig\\n  mysql -h 127.0.0.1 <<EOF\\n$(<change_master_to.sql.orig),\\n  MASTER_HOST=\'mysql-0.mysql\',\\n  MASTER_USER=\'root\',\\n  MASTER_PASSWORD=\'\',\\n  MASTER_CONNECT_RETRY=10;\\nSTART SLAVE;\\nEOF\\nfi\\n\\nexec ncat --listen --keep-open --send-only --max-conns=1 3307 -c \\\\\\n  \\\"xtrabackup --backup --slave-info --stream=xbstream --host=127.0.0.1 --user=root\\\"\"],\"configmap\":[],\"env\":[{\"key\":\"TZ\",\"name\":\"\",\"type\":\"\",\"value\":\"Asia/Shanghai\"}],\"imagePullPolicy\":\"Always\",\"img\":\"onlineshop/xtrabackup\",\"lifecycle\":null,\"limit\":null,\"livenessProbe\":null,\"log\":\"\",\"name\":\"xtrabackup\",\"parentResourceType\":\"\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"name\":\"TCP\",\"port\":\"3307\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"100m\",\"currentRate\":0,\"gpu\":\"\",\"memory\":\"128\"},\"securityContext\":{\"add\":[],\"drop\":[],\"privileged\":false,\"security\":false},\"storage\":[{\"bindOne\":false,\"capacity\":\"1Gi\",\"clusterId\":\"\",\"emptyDir\":\"Disk\",\"gitUrl\":\"\",\"hostPath\":\"\",\"namespace\":\"\",\"path\":\"/etc/mysql/conf.d\",\"projectId\":\"\",\"pvcName\":\"\",\"readOnly\":false,\"revision\":\"\",\"serviceName\":\"\",\"serviceType\":\"\",\"storageClassName\":\"\",\"storageClassType\":\"\",\"tenantId\":\"\",\"type\":\"emptyDir\",\"volumeName\":\"bjjtkzke\"},{\"bindOne\":false,\"capacity\":\"1Gi\",\"clusterId\":\"\",\"emptyDir\":\"Disk\",\"gitUrl\":\"\",\"hostPath\":\"\",\"namespace\":\"\",\"path\":\"/var/lib/mysql\",\"projectId\":\"\",\"pvcName\":\"\",\"readOnly\":false,\"revision\":\"\",\"serviceName\":\"\",\"serviceType\":\"\",\"storageClassName\":\"\",\"storageClassType\":\"\",\"tenantId\":\"\",\"type\":\"emptyDir\",\"volumeName\":\"rssrkfol\"}],\"syncTimeZone\":false,\"tag\":\"latest\"}],\"deployVersion\":\"\",\"hostAliases\":[],\"hostIPC\":false,\"hostName\":\"\",\"hostNetwork\":false,\"hostPID\":false,\"initContainers\":[{\"args\":[],\"command\":[\"bash\",\"-c\",\"set -ex\\n[[ `hostname` =~ -([0-9]+)$ ]] || exit 1\\nordinal=${BASH_REMATCH[1]}\\necho [mysqld] > /mnt/conf.d/server-id.cnf\\necho server-id=$((100 + $ordinal)) >> /mnt/conf.d/server-id.cnf\\nif [[ $ordinal -eq 0 ]]; then\\n  cp /mnt/config-map/master.cnf /mnt/conf.d/\\nelse\\n  cp /mnt/config-map/slave.cnf /mnt/conf.d/\\nfi\"],\"configmap\":[{\"configMapId\":\"5eeca32435ab4597\",\"file\":\"master.cnf\",\"name\":\"mysql-config\",\"path\":\"/mnt/config-map/\",\"tag\":\"1.0\",\"value\":\"# Apply this config only on the master.\\n[mysqld]\\nlog-bin\"},{\"configMapId\":\"5eeca32435ab4597\",\"file\":\"slave.cnf\",\"name\":\"mysql-config\",\"path\":\"/mnt/config-map/\",\"tag\":\"1.0\",\"value\":\"# Apply this config only on slaves\\n[mysqld]\\nsuper-read-only\"}],\"env\":[{\"key\":\"TZ\",\"name\":\"\",\"type\":\"\",\"value\":\"Asia/Shanghai\"}],\"imagePullPolicy\":\"IfNotPresent\",\"img\":\"onlineshop/mysql\",\"lifecycle\":null,\"limit\":null,\"livenessProbe\":null,\"log\":\"\",\"name\":\"init-mysql\",\"parentResourceType\":\"\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"name\":\"\",\"port\":\"1\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":null,\"securityContext\":{\"add\":[],\"drop\":[],\"privileged\":false,\"security\":false},\"storage\":[{\"bindOne\":false,\"capacity\":\"1Gi\",\"clusterId\":\"\",\"emptyDir\":\"Disk\",\"gitUrl\":\"\",\"hostPath\":\"\",\"namespace\":\"\",\"path\":\"/mnt/conf.d\",\"projectId\":\"\",\"pvcName\":\"\",\"readOnly\":false,\"revision\":\"\",\"serviceName\":\"\",\"serviceType\":\"\",\"storageClassName\":\"\",\"storageClassType\":\"\",\"tenantId\":\"\",\"type\":\"emptyDir\",\"volumeName\":\"bjjtkzke\"}],\"syncTimeZone\":false,\"tag\":\"5.7\"},{\"args\":[],\"command\":[\"bash\",\"-c\",\"set -ex\\n[[ `hostname` =~ ^([a-zA-Z0-9-]+)-([0-9]+)$ ]]\\nservicename=${BASH_REMATCH[1]}\\n[[ -d /var/lib/mysql/mysql ]] && exit 0\\n[[ `hostname` =~ -([0-9]+)$ ]] || exit 1\\nordinal=${BASH_REMATCH[1]}\\n[[ $ordinal -eq 0 ]] && exit 0\\nncat --recv-only $servicename-$(($ordinal-1)).$servicename 3307 | xbstream -x -C /var/lib/mysql\\nxtrabackup --prepare --target-dir=/var/lib/mysql\"],\"configmap\":[],\"env\":[{\"key\":\"TZ\",\"name\":\"\",\"type\":\"\",\"value\":\"Asia/Shanghai\"}],\"imagePullPolicy\":\"Always\",\"img\":\"onlineshop/xtrabackup\",\"lifecycle\":null,\"limit\":null,\"livenessProbe\":null,\"log\":\"\",\"name\":\"clone-mysql\",\"parentResourceType\":\"\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"name\":\"\",\"port\":\"2\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":null,\"securityContext\":{\"add\":[],\"drop\":[],\"privileged\":false,\"security\":false},\"storage\":[{\"bindOne\":false,\"capacity\":\"1Gi\",\"clusterId\":\"\",\"emptyDir\":\"Disk\",\"gitUrl\":\"\",\"hostPath\":\"\",\"namespace\":\"\",\"path\":\"/etc/mysql/conf.d\",\"projectId\":\"\",\"pvcName\":\"\",\"readOnly\":false,\"revision\":\"\",\"serviceName\":\"\",\"serviceType\":\"\",\"storageClassName\":\"\",\"storageClassType\":\"\",\"tenantId\":\"\",\"type\":\"emptyDir\",\"volumeName\":\"bjjtkzke\"},{\"bindOne\":false,\"capacity\":\"1Gi\",\"clusterId\":\"\",\"emptyDir\":\"Disk\",\"gitUrl\":\"\",\"hostPath\":\"\",\"namespace\":\"\",\"path\":\"/var/lib/mysql\",\"projectId\":\"\",\"pvcName\":\"\",\"readOnly\":false,\"revision\":\"\",\"serviceName\":\"\",\"serviceType\":\"\",\"storageClassName\":\"\",\"storageClassType\":\"\",\"tenantId\":\"\",\"type\":\"emptyDir\",\"volumeName\":\"rssrkfol\"}],\"syncTimeZone\":false,\"tag\":\"latest\"}],\"instance\":\"3\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"mysql\",\"namespace\":\"\",\"nodeAffinity\":[],\"nodeSelector\":\"\",\"podAffinity\":null,\"podAntiAffinity\":null,\"podDisperse\":{\"label\":\"\",\"namespace\":\"\",\"namespaceAliasName\":\"\",\"required\":true,\"type\":\"\"},\"podGroupSchedule\":null,\"podManagementPolicy\":\"OrderedReady\",\"projectId\":\"\",\"pullDependence\":null,\"restartPolicy\":\"Always\",\"serviceAccount\":\"\",\"serviceAccountName\":\"\",\"serviceDependence\":null,\"sessionAffinity\":\"\"}]'
, 'onlineshop/mysql,onlineshop/xtrabackup', '0', '1', 'all', 'admin', NOW(), '0', 'HarmonyCloud_Status=C', '1');
INSERT INTO `k8s_auth_server`.`app_store_service` (`app_id`, `service_id`)
VALUES ((SELECT `id` FROM `k8s_auth_server`.`app_store` WHERE `name`='MySQL-Cluster' AND `tag`='5.7'), (SELECT `id` FROM `k8s_auth_server`.`service_templates` WHERE `name`='mysql' AND `tag`='5.7' AND `tenant`='all'));

-- RabbitMQ-Cluster
INSERT INTO `k8s_auth_server`.`app_store` (`name`, `tag`, `type`, `create_user`, `create_time`, `image`, `service_account`)
VALUES ('RabbitMQ-Cluster', '3.7.8', 'am', 'admin', NOW(), 'rabbit.png', 'onlineshop');
INSERT INTO `k8s_auth_server`.`service_templates` (`name`, `tag`, `deployment_content`, `image_list`, `is_public`, `status`, `tenant`, `create_user`, `create_time`, `flag`, `node_selector`, `service_type`)
VALUES ('rabbitmq', '3.7.8', '[{"annotation":"","automountServiceAccountToken":false,"clusterIP":"","containers":[{"args":[],"command":[],"configmap":[],"env":[{"key":"RABBITMQ_USE_LONGNAME","name":"","type":"equal","value":"true"},{"key":"NAMESPACE","name":"","type":"from","value":"metadata.namespace"},{"key":"RABBITMQ_ERLANG_COOKIE","name":"","type":"equal","value":"mycookie"},{"key":"K8S_SERVICE_NAME","name":"","type":"equal","value":"rabbitmq"},{"key":"HOSTNAME","name":"","type":"from","value":"metadata.name"},{"key":"RABBITMQ_NODENAME","name":"","type":"equal","value":"rabbit@$(HOSTNAME).$(K8S_SERVICE_NAME).$(NAMESPACE).svc.cluster.local"},{"key":"TZ","name":"","type":"","value":"Asia/Shanghai"}],"imagePullPolicy":"IfNotPresent","img":"onlineshop/rabbitmq","lifecycle":null,"limit":null,"livenessProbe":{"empty":false,"exec":{"command":["rabbitmqctl","status"]},"failureThreshold":10,"httpGet":null,"initialDelaySeconds":30,"periodSeconds":10,"successThreshold":1,"tcpSocket":null,"timeoutSeconds":10},"log":"","name":"rabbitmq","parentResourceType":"","ports":[{"containerPort":"","expose":"true","name":"TCP","port":"15672","protocol":"TCP"},{"containerPort":"","expose":"true","name":"TCP","port":"5672","protocol":"TCP"}],"readinessProbe":{"empty":false,"exec":{"command":["rabbitmqctl","status"]},"failureThreshold":10,"httpGet":null,"initialDelaySeconds":30,"periodSeconds":10,"successThreshold":1,"tcpSocket":null,"timeoutSeconds":10},"resource":{"cpu":"500m","currentRate":0,"gpu":"","memory":"512"},"securityContext":{"add":[],"drop":[],"privileged":false,"security":false},"storage":[{"bindOne":false,"capacity":"","clusterId":"","emptyDir":"","gitUrl":"","hostPath":"/changmetosc","namespace":"","path":"/var/lib/rabbitmq/mnesia","projectId":"","pvcName":"","readOnly":false,"revision":"","serviceName":"","serviceType":"","storageClassName":"","storageClassType":"","tenantId":"","type":"hostPath","volumeName":""}],"syncTimeZone":false,"tag":"3.7.8-management"}],"deployVersion":"","hostAliases":[],"hostIPC":false,"hostName":"","hostNetwork":false,"hostPID":false,"initContainers":[],"instance":"2","labels":"","logPath":"","logService":"","name":"rabbitmq","namespace":"","nodeAffinity":[],"nodeSelector":"","podAffinity":null,"podAntiAffinity":null,"podDisperse":{"label":"","namespace":"","namespaceAliasName":"","required":true,"type":""},"podGroupSchedule":null,"podManagementPolicy":"OrderedReady","projectId":"","pullDependence":null,"restartPolicy":"Always","serviceAccount":"","serviceAccountName":"","serviceDependence":null,"sessionAffinity":""}]', 'onlineshop/rabbitmq', '0', '1', 'all', 'admin', NOW(), '0', 'HarmonyCloud_Status=C', '1');
INSERT INTO `k8s_auth_server`.`app_store_service` (`app_id`, `service_id`)
VALUES ((SELECT `id` FROM `k8s_auth_server`.`app_store` WHERE `name`='RabbitMQ-Cluster' AND `tag`='3.7.8'), (SELECT `id` FROM `k8s_auth_server`.`service_templates` WHERE `name`='rabbitmq' AND `tag`='3.7.8' AND `tenant`='all'));

-- Kafka-Cluster
INSERT INTO `k8s_auth_server`.`app_store` (`name`, `tag`, `type`, `create_user`, `create_time`, `image`, `service_account`)
VALUES ('Kafka-Cluster', '2.1.0', 'am', 'admin', NOW(), 'kafka.png', '');
INSERT INTO `k8s_auth_server`.`service_templates` (`name`, `tag`, `deployment_content`, `image_list`, `is_public`, `status`, `tenant`, `create_user`, `create_time`, `flag`, `node_selector`, `service_type`)
VALUES ('zookeeper', 'v3', '[{"annotation":"","automountServiceAccountToken":false,"clusterIP":"","containers":[{"args":[],"command":["sh","-c","zkGenConfig.sh && zkServer.sh start-foreground"],"configmap":[],"env":[{"key":"ZK_REPLICAS","name":"","type":"equal","value":"3"},{"key":"ZK_HEAP_SIZE","name":"","type":"equal","value":"1G"},{"key":"ZK_TICK_TIME","name":"","type":"equal","value":"2000"},{"key":"ZK_INIT_LIMIT","name":"","type":"equal","value":"10"},{"key":"ZK_SYNC_LIMIT","name":"","type":"equal","value":"2000"},{"key":"ZK_MAX_CLIENT_CNXNS","name":"","type":"equal","value":"60"},{"key":"ZK_SNAP_RETAIN_COUNT","name":"","type":"equal","value":"3"},{"key":"ZK_PURGE_INTERVAL","name":"","type":"equal","value":"0"},{"key":"ZK_CLIENT_PORT","name":"","type":"equal","value":"2181"},{"key":"ZK_SERVER_PORT","name":"","type":"equal","value":"2888"},{"key":"ZK_ELECTION","name":"","type":"equal","value":"3888"},{"key":"TZ","name":"","type":"","value":"Asia/Shanghai"}],"imagePullPolicy":"IfNotPresent","img":"onlineshop/k8szk","lifecycle":null,"limit":null,"livenessProbe":{"empty":false,"exec":{"command":["zkOk.sh"]},"failureThreshold":5,"httpGet":null,"initialDelaySeconds":10,"periodSeconds":10,"successThreshold":1,"tcpSocket":null,"timeoutSeconds":5},"log":"","name":"k8szk","parentResourceType":"","ports":[{"containerPort":"","expose":"true","name":"TCP","port":"2181","protocol":"TCP"},{"containerPort":"","expose":"true","name":"TCP","port":"2888","protocol":"TCP"},{"containerPort":"","expose":"true","name":"TCP","port":"3888","protocol":"TCP"}],"readinessProbe":{"empty":false,"exec":{"command":["zkOk.sh"]},"failureThreshold":5,"httpGet":null,"initialDelaySeconds":10,"periodSeconds":10,"successThreshold":1,"tcpSocket":null,"timeoutSeconds":5},"resource":{"cpu":"1000m","currentRate":0,"gpu":"","memory":"1536"},"securityContext":{"add":[],"drop":[],"privileged":true,"security":true},"storage":[{"bindOne":false,"capacity":"","clusterId":"","emptyDir":"","gitUrl":"","hostPath":"/changeme","namespace":"","path":"/var/lib/zookeeper","projectId":"","pvcName":"","readOnly":false,"revision":"","serviceName":"","serviceType":"","storageClassName":"","storageClassType":"","tenantId":"","type":"hostPath","volumeName":""}],"syncTimeZone":false,"tag":"v3"}],"deployVersion":"","hostAliases":[],"hostIPC":false,"hostName":"","hostNetwork":false,"hostPID":false,"initContainers":[],"instance":"3","labels":"","logPath":"","logService":"","name":"zookeeper","namespace":"","nodeAffinity":[],"nodeSelector":"","podAffinity":null,"podAntiAffinity":null,"podDisperse":{"label":"","namespace":"","namespaceAliasName":"","required":true,"type":""},"podGroupSchedule":null,"podManagementPolicy":"OrderedReady","projectId":"","pullDependence":null,"restartPolicy":"Always","serviceAccount":"","serviceAccountName":"","serviceDependence":null,"sessionAffinity":""}]', 'onlineshop/k8szk', '0', '1', 'all', 'admin', NOW(), '0', 'HarmonyCloud_Status=C', '1');
INSERT INTO `k8s_auth_server`.`app_store_service` (`app_id`, `service_id`)
VALUES ((SELECT `id` FROM `k8s_auth_server`.`app_store` WHERE `name`='Kafka-Cluster' AND `tag`='2.1.0'), (SELECT `id` FROM `k8s_auth_server`.`service_templates` WHERE `name`='zookeeper' AND `tag`='v3' AND `tenant`='all'));
INSERT INTO `k8s_auth_server`.`service_templates` (`name`, `tag`, `deployment_content`, `image_list`, `is_public`, `status`, `tenant`, `create_user`, `create_time`, `flag`, `node_selector`, `service_type`)
VALUES ('kafka', '2.1.0', '[{"annotation":"","automountServiceAccountToken":false,"clusterIP":"","containers":[{"args":[],"command":["sh","-c","exec kafka-server-start.sh /opt/kafka/config/server.properties --override broker.id=${HOSTNAME##*-} --override listeners=PLAINTEXT://:9093 --override zookeeper.connect=$ZK_SERVICE.$NAMESPACE.svc.cluster.local:2181 --override log.dir=/var/lib/kafka --override auto.create.topics.enable=true --override auto.leader.rebalance.enable=true --override background.threads=10 --override compression.type=producer --override delete.topic.enable=false --override leader.imbalance.check.interval.seconds=300 --override leader.imbalance.per.broker.percentage=10 --override log.flush.interval.messages=9223372036854775807 --override log.flush.offset.checkpoint.interval.ms=60000 --override log.flush.scheduler.interval.ms=9223372036854775807 --override log.retention.bytes=-1 --override log.retention.hours=168 --override log.roll.hours=168 --override log.roll.jitter.hours=0 --override log.segment.bytes=1073741824 --override log.segment.delete.delay.ms=60000 --override message.max.bytes=1000012 --override min.insync.replicas=1 --override num.io.threads=8 --override num.network.threads=3 --override num.recovery.threads.per.data.dir=1 --override num.replica.fetchers=1 --override offset.metadata.max.bytes=4096 --override offsets.commit.required.acks=-1 --override offsets.commit.timeout.ms=5000 --override offsets.load.buffer.size=5242880 --override offsets.retention.check.interval.ms=600000 --override offsets.retention.minutes=1440 --override offsets.topic.compression.codec=0 --override offsets.topic.num.partitions=50 --override offsets.topic.replication.factor=3 --override offsets.topic.segment.bytes=104857600 --override queued.max.requests=500 --override quota.consumer.default=9223372036854775807 --override quota.producer.default=9223372036854775807 --override replica.fetch.min.bytes=1 --override replica.fetch.wait.max.ms=500 --override replica.high.watermark.checkpoint.interval.ms=5000 --override replica.lag.time.max.ms=10000 --override replica.socket.receive.buffer.bytes=65536 --override replica.socket.timeout.ms=30000 --override request.timeout.ms=30000 --override socket.receive.buffer.bytes=102400 --override socket.request.max.bytes=104857600 --override socket.send.buffer.bytes=102400 --override unclean.leader.election.enable=true --override zookeeper.session.timeout.ms=6000 --override zookeeper.set.acl=false --override broker.id.generation.enable=true --override connections.max.idle.ms=600000 --override controlled.shutdown.enable=true --override controlled.shutdown.max.retries=3 --override controlled.shutdown.retry.backoff.ms=5000 --override controller.socket.timeout.ms=30000 --override default.replication.factor=1 --override fetch.purgatory.purge.interval.requests=1000 --override group.max.session.timeout.ms=300000 --override group.min.session.timeout.ms=6000 --override inter.broker.protocol.version=0.11.0-IV0 --override log.cleaner.backoff.ms=15000 --override log.cleaner.dedupe.buffer.size=134217728 --override log.cleaner.delete.retention.ms=86400000 --override log.cleaner.enable=true --override log.cleaner.io.buffer.load.factor=0.9 --override log.cleaner.io.buffer.size=524288 --override log.cleaner.io.max.bytes.per.second=1.7976931348623157E308 --override log.cleaner.min.cleanable.ratio=0.5 --override log.cleaner.min.compaction.lag.ms=0 --override log.cleaner.threads=1 --override log.cleanup.policy=delete --override log.index.interval.bytes=4096 --override log.index.size.max.bytes=10485760 --override log.message.timestamp.difference.max.ms=9223372036854775807 --override log.message.timestamp.type=CreateTime --override log.preallocate=false --override log.retention.check.interval.ms=300000 --override max.connections.per.ip=2147483647 --override num.partitions=1 --override producer.purgatory.purge.interval.requests=1000 --override replica.fetch.backoff.ms=1000 --override replica.fetch.max.bytes=1048576 --override replica.fetch.response.max.bytes=10485760 --override reserved.broker.max.id=1000"],"configmap":[],"env":[{"key":"KAFKA_HEAP_OPTS","name":"","type":"equal","value":"-Xmx512M -Xms512M"},{"key":"KAFKA_OPTS","name":"","type":"equal","value":"-Dlogging.level=INFO"},{"key":"ZK_SERVICE","name":"","type":"equal","value":"zookeeper"},{"key":"NAMESPACE","name":"","type":"from","value":"metadata.namespace"},{"key":"TZ","name":"","type":"","value":"Asia/Shanghai"}],"imagePullPolicy":"IfNotPresent","img":"onlineshop/kafka-sts","lifecycle":null,"limit":null,"livenessProbe":null,"log":"","name":"k8skafka","parentResourceType":"","ports":[{"containerPort":"","expose":"true","name":"TCP","port":"9093","protocol":"TCP"}],"readinessProbe":null,"resource":{"cpu":"1000m","currentRate":0,"gpu":"","memory":"1024"},"securityContext":{"add":[],"drop":[],"privileged":true,"security":true},"storage":[{"bindOne":false,"capacity":"","clusterId":"","emptyDir":"","gitUrl":"","hostPath":"/changeme","namespace":"","path":"/var/lib/kafka","projectId":"","pvcName":"","readOnly":false,"revision":"","serviceName":"","serviceType":"","storageClassName":"","storageClassType":"","tenantId":"","type":"hostPath","volumeName":""}],"syncTimeZone":false,"tag":"v2.1.0"}],"deployVersion":"","hostAliases":[],"hostIPC":false,"hostName":"","hostNetwork":false,"hostPID":false,"initContainers":[{"args":[],"command":["/bin/bash","-c","/root/shell_script/ncat.sh zookeeper 2181 1 10 5"],"configmap":[],"env":[{"key":"TZ","name":"","type":"","value":"Asia/Shanghai"}],"imagePullPolicy":"IfNotPresent","img":"onlineshop/alpine-net","lifecycle":null,"limit":null,"livenessProbe":null,"log":"","name":"checkzk","parentResourceType":"","ports":[{"containerPort":"","expose":"true","name":"","port":"1","protocol":"TCP"}],"readinessProbe":null,"resource":null,"securityContext":{"add":[],"drop":[],"privileged":false,"security":false},"storage":[],"syncTimeZone":false,"tag":"1.0"}],"instance":"3","labels":"","logPath":"","logService":"","name":"kafka","namespace":"","nodeAffinity":[],"nodeSelector":"","podAffinity":null,"podAntiAffinity":null,"podDisperse":{"label":"","namespace":"","namespaceAliasName":"","required":true,"type":""},"podGroupSchedule":null,"podManagementPolicy":"OrderedReady","projectId":"","pullDependence":null,"restartPolicy":"Always","serviceAccount":"","serviceAccountName":"","serviceDependence":null,"sessionAffinity":""}]', 'onlineshop/kafka-sts', '0', '1', 'all', 'admin', NOW(), '0', 'HarmonyCloud_Status=C', '1');
INSERT INTO `k8s_auth_server`.`app_store_service` (`app_id`, `service_id`)
VALUES ((SELECT `id` FROM `k8s_auth_server`.`app_store` WHERE `name`='Kafka-Cluster' AND `tag`='2.1.0'), (SELECT `id` FROM `k8s_auth_server`.`service_templates` WHERE `name`='kafka' AND `tag`='2.1.0' AND `tenant`='all'));

-- Elasticsearch
INSERT INTO `k8s_auth_server`.`app_store` (`name`, `tag`, `type`, `create_user`, `create_time`, `image`, `service_account`)
VALUES ('Elasticsearch', 'v6.3.2', 'database', 'admin', NOW(), 'elastic.png', 'onlineshop');
INSERT INTO `k8s_auth_server`.`service_templates` (`name`, `tag`, `deployment_content`, `image_list`, `is_public`, `status`, `tenant`, `create_user`, `create_time`, `flag`, `node_selector`, `service_type`)
VALUES ('elasticsearch', 'v6.3.2', '[{"annotation":"","automountServiceAccountToken":false,"clusterIP":"","containers":[{"args":[],"command":[],"configmap":[],"env":[{"key":"ELASTICSEARCH_SERVICE_NAME","name":"","type":"equal","value":"elasticsearch"},{"key":"CLUSTER_NAME","name":"","type":"equal","value":"elasticsearch"},{"key":"MINIMUM_MASTER_NODES","name":"","type":"equal","value":"1"},{"key":"NAMESPACE","name":"","type":"from","value":"metadata.namespace"},{"key":"TZ","name":"","type":"","value":"Asia/Shanghai"}],"imagePullPolicy":"IfNotPresent","img":"onlineshop/elasticsearch","lifecycle":null,"limit":null,"livenessProbe":null,"log":"","name":"elasticsearch","parentResourceType":"","ports":[{"containerPort":"","expose":"true","name":"","port":"9200","protocol":"TCP"},{"containerPort":"","expose":"true","name":"","port":"9300","protocol":"TCP"}],"readinessProbe":null,"resource":{"cpu":"1000m","currentRate":0,"gpu":"","memory":"2048"},"securityContext":{"add":[],"drop":[],"privileged":false,"security":false},"storage":[{"bindOne":false,"capacity":"","clusterId":"","emptyDir":"","gitUrl":"","hostPath":"/tmp/data","namespace":"","path":"/data","projectId":"","pvcName":"","readOnly":false,"revision":"","serviceName":"","serviceType":"","storageClassName":"","storageClassType":"","tenantId":"","type":"hostPath","volumeName":""},{"bindOne":false,"capacity":"","clusterId":"","emptyDir":"","gitUrl":"","hostPath":"/tmp/data-backup","namespace":"","path":"/data-backup","projectId":"","pvcName":"","readOnly":false,"revision":"","serviceName":"","serviceType":"","storageClassName":"","storageClassType":"","tenantId":"","type":"hostPath","volumeName":""}],"syncTimeZone":false,"tag":"v6.3.2"}],"deployVersion":"","hostAliases":[],"hostIPC":false,"hostName":"","hostNetwork":false,"hostPID":false,"initContainers":[{"args":[],"command":["/sbin/sysctl","-w","vm.max_map_count=262144"],"configmap":[],"env":[{"key":"TZ","name":"","type":"","value":"Asia/Shanghai"}],"imagePullPolicy":"Always","img":"onlineshop/alpine","lifecycle":null,"limit":null,"livenessProbe":null,"log":"","name":"elasticsearch-init","parentResourceType":"","ports":[{"containerPort":"","expose":"true","name":"","port":"1","protocol":"TCP"}],"readinessProbe":null,"resource":null,"securityContext":{"add":[],"drop":[],"privileged":true,"security":true},"storage":[],"syncTimeZone":false,"tag":"latest"}],"instance":"2","labels":"","logPath":"","logService":"","name":"elasticsearch","namespace":"","nodeAffinity":[],"nodeSelector":"","podAffinity":null,"podAntiAffinity":null,"podDisperse":{"label":"","namespace":"","namespaceAliasName":"","required":true,"type":""},"podGroupSchedule":null,"podManagementPolicy":"Parallel","projectId":"","pullDependence":null,"restartPolicy":"Always","serviceAccount":"","serviceAccountName":"","serviceDependence":null,"sessionAffinity":""}]', 'onlineshop/elasticsearch', '0', '1', 'all', 'admin', NOW(), '0', 'HarmonyCloud_Status=C', '1');
INSERT INTO `k8s_auth_server`.`app_store_service` (`app_id`, `service_id`)
VALUES ((SELECT `id` FROM `k8s_auth_server`.`app_store` WHERE `name`='Elasticsearch' AND `tag`='v6.3.2'), (SELECT `id` FROM `k8s_auth_server`.`service_templates` WHERE `name`='elasticsearch' AND `tag`='v6.3.2' AND `tenant`='all'));


UPDATE `k8s_auth_server`.`system_config`
SET `config_value`='/k8s-deploy/nfs-client-provisioner:v3.1.0'
WHERE `config_name`='provisionerImageName'
AND `config_type`='nfs-provisioner';


update k8s_auth_server.resource_menu set name_en='Deliver Center' where name='交付中心';

-- -------20190122---------
INSERT INTO `k8s_auth_server`.`url_dic`(url,module,resource) values('/roles/*/datacenters/*/switch','whitelist','whitelist');
INSERT INTO `k8s_auth_server`.`url_dic`(url,module,resource) values('/roles/*/datacenters','whitelist','whitelist');

-- -------20190123---------
INSERT INTO `k8s_auth_server`.`url_dic`(url,module,resource) values('/tenants/*/projects/*/repositories/*/syncclusters','delivery','repository');

-- -------20190124---------
INSERT INTO `k8s_auth_server`.`url_dic` (url,module,resource) VALUES ('/tenants/*/projects/*/deploys/*/annotation', 'appcenter', 'app');

-- ------20190131----------
INSERT INTO `k8s_auth_server`.`url_dic` (url,module,resource) VALUES ('/openapi/clusters/*/namespaces/*/pvcs', 'infrastructure', 'clustermar');

-- -----20190201-----------
ALTER TABLE `cicd_stage`
MODIFY COLUMN `configuration`  mediumtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '配置列表' AFTER `deploy_type`;

ALTER TABLE `role_new`
MODIFY COLUMN `name`  varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '角色名称' AFTER `id`,
MODIFY COLUMN `nick_name`  varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '描述' AFTER `name`;

ALTER TABLE project_ip_pool MODIFY `cidr` varchar(20) DEFAULT NULL COMMENT 'CIDR';
ALTER TABLE project_ip_pool MODIFY `gateway` int(11) DEFAULT NULL COMMENT '网关';

INSERT INTO `k8s_auth_server`.`url_dic` ( `url`, `module`, `resource`) VALUES ( '/tenants/*/projects/*/repositories/*/images/*/tags/*/syncImage', 'delivery', 'image');