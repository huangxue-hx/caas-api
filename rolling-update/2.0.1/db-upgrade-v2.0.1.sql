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

INSERT into k8s_auth_server.configfile_item(`configfile_id`, `path`, `content`, `file_name`) select id,path,item,name FROM `k8s_auth_server`.configfile

ALTER TABLE tenant_cluster_quota ADD storage_quotas VARCHAR(255) SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '集群租户的所有存储配额信息（name1_quota1_total1，name2_quota2_total2...）'

INSERT INTO system_config (config_name, config_value, config_type, create_user)
VALUES ('provisionerImageName', '/k8s-deploy/nfs-client-provisioner:v2.1.0', 'nfs-provisioner', 'admin');

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
VALUES ('dependenceUploadImageName', '/k8s-deploy/busybox:latest', 'dependence', 'admin');

INSERT INTO system_config (config_name, config_value, config_type, create_user)
VALUES ('dependenceUploadImageCmd', '["/bin/sh","-c","top"]', 'dependence', 'admin');