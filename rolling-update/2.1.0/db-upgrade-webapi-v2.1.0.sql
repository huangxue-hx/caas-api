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
) ENGINE=InnoDB AUTO_INCREMENT=41 DEFAULT CHARSET=utf8;

INSERT into k8s_auth_server.configfile_item(`configfile_id`, `path`, `content`, `file_name`) select id,path,item,name FROM `k8s_auth_server`.configfile;

ALTER TABLE k8s_auth_server.`configfile`
  DROP COLUMN `item`,
  DROP COLUMN `path`;

ALTER TABLE tenant_cluster_quota ADD storage_quotas VARCHAR(255) DEFAULT '' COMMENT '集群租户的所有存储配额信息（name1_quota1_total1，name2_quota2_total2...）';

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
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8;

ALTER TABLE `tenant_cluster_quota`
ADD COLUMN `ic_names` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '负载均衡器名称，多个以“，”分割';

INSERT INTO k8s_auth_server.`url_dic` (`url`,`module`,`resource`)
VALUES ('/clusters/*/storage','infrastructure','clustermar');

ALTER TABLE `k8s_auth_server`.`cicd_stage` MODIFY `stage_name` VARCHAR(100);

ALTER TABLE `tenant_cluster_quota` MODIFY  COLUMN storage_quotas VARCHAR(512) COMMENT '集群租户的所有存储配额信息（name1-quota1-total1，name2-quota2-total2，···）'


UPDATE `k8s_auth_server`.`resource_menu` SET `weight` = `weight`+1 WHERE `weight`>=14;

INSERT INTO `k8s_auth_server`.`resource_menu` (`id`,`name`, `name_en`, `type`, `url`, `weight`, `create_time`, `update_time`, `available`, `icon_name`, `isparent`, `parent_rmid`, `module`)
VALUES ('32', '有状态服务', 'StatefulSet', 'menu', 'statefulSet', '14', NOW(), NOW(), '1', '', '0', '6', 'app');

UPDATE `k8s_auth_server`.`resource_menu_role` SET `weight` = `weight`+1 WHERE `weight`>=14;

INSERT INTO `k8s_auth_server`.`resource_menu_role` (`weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('14', NOW(), NOW(), '0', '1', '32');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('14', NOW(), NOW(), '0', '2', '32');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('14', NOW(), NOW(), '0', '3', '32');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('14', NOW(), NOW(), '0', '4', '32');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('14', NOW(), NOW(), '0', '5', '32');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('14', NOW(), NOW(), '0', '6', '32');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('14', NOW(), NOW(), '0', '7', '32');

UPDATE `k8s_auth_server`.`service_templates` SET `deployment_content`='[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[],\"command\":[],\"configmap\":[],\"env\":[{\"key\":\"TZ\",\"value\":\"Asia/Shanghai\"}],\"img\":\"onlineshop/mysql\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"mysql\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"3306\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"1000m\",\"memory\":\"1024\"},\"storage\":[],\"tag\":\"5.7.6\"}],\"hostName\":\"\",\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"mysql\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]' WHERE `id`='5';

/*
 harbor标签的增删，镜像打label，复制规则重名判断
*/
INSERT INTO `url_dic` (`url`, `module`, `resource`)  VALUES  ('/tenants/*/projects/*/repositories/label','delivery','repositorymgr');
INSERT INTO `url_dic` (`url`, `module`, `resource`) VALUES  ('/tenants/*/projects/*/repositories/img/label','delivery','repositorymgr');
INSERT INTO `url_dic` (`url`, `module`, `resource`) VALUES  ('/harbor/*/replicationpolicies/checkname','delivery','repositorymgr');
/*
 获取规则详情，手动开启规则复制
 */
INSERT INTO `url_dic` (`url`, `module`, `resource`) VALUES  ('/harbor/*/replicationpolicies/*/detail','delivery','repositorymgr');
INSERT INTO `url_dic` (`url`, `module`, `resource`) VALUES  ('/harbor/*/replicationpolicies/*/copy','delivery','repositorymgr');

CREATE TABLE k8s_auth_server.cicd_repository_user (
	`id` INT (11) NOT NULL AUTO_INCREMENT PRIMARY KEY,
	`name` VARCHAR (255) NOT NULL,
	`username` VARCHAR (255) NOT NULL,
	`password` VARCHAR (255) NOT NULL,
	`type` VARCHAR (255) NOT NULL,
	`comment` VARCHAR (255)
);
INSERT INTO url_dic (url,module,resource) VALUES ('/tenants/*/projects/*/repositry/user','cicd','cicdmgr');
INSERT INTO url_dic (url,module,resource) VALUES ('/tenants/*/cicd/repositry/user','cicd','cicdmgr');


ALTER TABLE `cicd_stage`
ADD COLUMN `user_id` int(11) AFTER `update_time`;

-- -----------------------------------------------2018.12.02-wanhua--------------------------------------------------------------

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
DROP TABLE IF EXISTS `istio_global_configure`;
CREATE TABLE `istio_global_configure` (
  `id` int(4) NOT NULL AUTO_INCREMENT,
  `cluster_id` varchar(128) CHARACTER SET utf8 DEFAULT NULL COMMENT '集群id',
  `cluster_name` varchar(128) CHARACTER SET utf8 DEFAULT NULL COMMENT '集群名称',
  `switch_status` int(2) DEFAULT NULL COMMENT '开关状态(0关闭 1开启)',
  `operator_id` bigint(20) DEFAULT NULL COMMENT '操作人员id',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '最近一次更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=latin1;

-- ----------------------------
-- Table structure for url_dic
-- ----------------------------
INSERT INTO `k8s_auth_server`.`url_dic`(url,module,resource) values('/clusters/*/istiopolicyswitch','appcenter','app');
INSERT INTO `k8s_auth_server`.`url_dic`(url,module,resource) values('/tenants/*/namespaces/*/istiopolicyswitch','appcenter','app');

-- 策略概览表（rule_overview）
DROP TABLE IF EXISTS `rule_overview`;
CREATE TABLE `rule_overview` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
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
  `user_id` varchar(128) DEFAULT NULL COMMENT '最近一次更新该策略用户id',
  `create_time` timestamp DEFAULT NULL COMMENT '策略创建时间',
  `update_time` timestamp DEFAULT NULL COMMENT '策略最近一次更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='策略概览表';

-- 策略中资源信息表（rule_detail）
DROP TABLE IF EXISTS `rule_detail`;
CREATE TABLE `rule_detail` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `rule_id` varchar(32) NOT NULL COMMENT '策略关联id',
  `rule_detail_order` int(2) DEFAULT NULL COMMENT '资源创建顺序',
  `rule_detail_content` blob DEFAULT NULL COMMENT '策略中资源对象yaml',
  `create_time` timestamp DEFAULT NULL COMMENT '策略创建时间',
  `update_time` timestamp DEFAULT NULL COMMENT '策略最近一次更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='策略中资源信息表';

INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/tenants/*/projects/*/deploys/*/istiopolicies', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/tenants/*/projects/*/deploys/*/istiopolicies/*', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/tenants/*/projects/*/deploys/*/istiopolicies/*/open', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/tenants/*/projects/*/deploys/*/istiopolicies/*/close', 'appcenter', 'app');

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