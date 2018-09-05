CREATE TABLE `k8s_auth_server`.`configfile_item` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id主键,自动生成',
  `configfile_id` varchar(64) NOT NULL COMMENT '外键,配置id',
  `path` varchar(512) DEFAULT NULL COMMENT '路径',
  `content` mediumtext NOT NULL COMMENT '文件内容',
  `file_name` varchar(128) DEFAULT NULL COMMENT '文件名',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=41 DEFAULT CHARSET=utf8;

UPDATE `k8s_auth_server`.`user` SET `real_name`='admin' WHERE `id`='1';

ALTER TABLE k8s_auth_server.`configfile`
  DROP COLUMN `item`,
  DROP COLUMN `path`;

INSERT into k8s_auth_server.configfile_item(`configfile_id`, `path`, `content`, `file_name`) select id,path,item,name FROM `k8s_auth_server`.configfile;

ALTER TABLE tenant_cluster_quota ADD storage_quotas VARCHAR(255) SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '集群租户的所有存储配额信息（name1_quota1_total1，name2_quota2_total2...）'

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
VALUES ('cicd_type_merge', 'true', 'cicd', 'admin');

ALTER TABLE `k8s_auth_server`.`cicd_stage_type` ADD COLUMN `index` TINYINT(4);
ALTER TABLE `k8s_auth_server`.`cicd_stage_type` ADD COLUMN `status` TINYINT(4);
UPDATE `k8s_auth_server`.`cicd_stage_type` SET `index`=1, `status`=1 WHERE template_type=0;
UPDATE `k8s_auth_server`.`cicd_stage_type` SET `index`=2, `status`=1 WHERE template_type=3;
UPDATE `k8s_auth_server`.`cicd_stage_type` SET `index`=3, `status`=1 WHERE template_type=1;
UPDATE `k8s_auth_server`.`cicd_stage_type` SET `index`=4, `status`=0 WHERE template_type=5;
UPDATE `k8s_auth_server`.`cicd_stage_type` SET `index`=10, `status`=1 WHERE template_type=6;
UPDATE `k8s_auth_server`.`cicd_stage_type` SET `index`=8, `status`=1 WHERE template_type=2;
UPDATE `k8s_auth_server`.`cicd_stage_type` SET `index`=9, `status`=0 WHERE template_type=8;
UPDATE `k8s_auth_server`.`cicd_stage_type` SET `index`=10, `status`=1 WHERE template_type=6;

ALTER TABLE k8s_auth_server.`user` ADD
login_fail_time VARCHAR(255) COMMENT '登陆失败时间',
ADD login_fail_count TINYINT(1)COMMENT '登陆失败次数';

INSERT INTO k8s_auth_server.`system_config` (config_name,config_value,config_type,create_user) VALUES ('loginFailTimeLimit','1800','login','admin');
INSERT INTO k8s_auth_server.`system_config` (config_name,config_value,config_type,create_user)  VALUES ('loginFailCountLimit','10','login','admin');
INSERT INTO k8s_auth_server.`system_config` (config_name,config_value,config_type,create_user)  VALUES ('SingleTimeLimit','60','login','admin');

UPDATE k8s_auth_server.url_dic SET module='whitelist',resource='whitelist' WHERE url = '/users/*/password';