use k8s_auth_server;

INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`)
VALUES ('/clusters/label/nodes', 'system', 'system');

INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`)
VALUES ('/clusters/nodes/groups', 'infrastructure', 'clustermgr');

INSERT INTO `k8s_auth_server`.`resource_menu_role` (`weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`)
VALUES ('41', '2019-01-14 17:28:00', '2019-01-14 17:28:00', '1', '1', '41');

INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/system/api-server/auditlogs', 'log', 'auditlog');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/system/api-server/auditlogs/count', 'log', 'auditlog');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/system/api-server/auditlogs/namespaces', 'log', 'auditlog');

INSERT INTO `k8s_auth_server`.`resource_menu` (`name`, `name_en`, `type`, `url`, `weight`, `create_time`, `update_time`, `available`, `icon_name`, `isparent`, `parent_rmid`, `module`)
VALUES ('集群审计', 'k8s Audit', 'menu', 'k8sAuditLog', '41', '2019-01-14 16:47:00', '2019-01-14 16:50:56', '1', '', '0', '9', 'k8sauditlog');

INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/tenants/*/projects/*/deploys/monitor', 'system', 'system');

INSERT INTO `k8s_auth_server`.`resource_menu` (`name`, `name_en`, `type`, `url`, `weight`, `create_time`, `update_time`, `available`, `icon_name`, `isparent`, `parent_rmid`, `module`)
VALUES ('监控中心', 'Monitor Center', 'menu', 'monitorCenter', '42', '2019-02-14 16:47:00', '2019-02-14 16:50:56', '1', '', '1', '0', 'monitorcenter');

INSERT INTO `k8s_auth_server`.`resource_menu_role` (`weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`)
VALUES ('42', '2019-01-14 17:28:00', '2019-01-14 17:28:00', '1', '1', '42');

INSERT INTO `k8s_auth_server`.`resource_menu_role` (`weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`)
VALUES ('42', '2019-01-14 17:28:00', '2019-01-14 17:28:00', '1', '2', '42');

INSERT INTO `k8s_auth_server`.`url_dic`(`url`, `module`, `resource`) VALUES ('/tenants/*/projects/*/ippools', 'tenant', 'ippoolsmgr');
INSERT INTO `k8s_auth_server`.`url_dic`(`url`, `module`, `resource`) VALUES ('/tenants/*/projects/*/ippools/checkcluster', 'tenant', 'ippoolsmgr');
INSERT INTO `k8s_auth_server`.`url_dic`(`url`, `module`, `resource`) VALUES ('/tenants/*/clusters', 'tenant', 'basic');

-- ----------------------------
-- Table structure for project_ip_pool
-- ----------------------------
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `project_ip_pool`;
CREATE TABLE `project_ip_pool` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '资源池id',
  `name` varchar(64) NOT NULL COMMENT '资源池名称',
  `tenant_id` varchar(64) NOT NULL COMMENT '租户id',
  `project_id` varchar(64) NOT NULL COMMENT '项目id',
  `cluster_id` varchar(64) NOT NULL COMMENT '集群id',
  `cidr` varchar(20) NOT NULL COMMENT 'DIDR',
  `subnet` varchar(20) NOT NULL COMMENT '子网掩码',
  `gateway` int(11) NOT NULL COMMENT '网关',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='项目ip资源池';

SET FOREIGN_KEY_CHECKS = 1;

DROP TABLE IF EXISTS `transfer_step`;
CREATE TABLE `transfer_step` (
  `step_id` int(64) NOT NULL COMMENT '步骤id',
  `step_name` varchar(64) NOT NULL COMMENT '步骤名称',
  `percent` varchar(64) NOT NULL COMMENT '百分比',
  PRIMARY KEY (`step_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



-- ----------------------------
-- Table structure for transfer_cluster_backup
-- ----------------------------
DROP TABLE IF EXISTS `transfer_cluster_backup`;
CREATE TABLE `transfer_cluster_backup` (
  `id` int(255) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `tenant_id` varchar(30) NOT NULL COMMENT '租户id',
  `namespace_num` int(30) NOT NULL COMMENT '第几次迁移分区',
  `deploy_num` int(30) NOT NULL COMMENT '第几次迁移项目',
  `err_msg` varchar(64) DEFAULT NULL COMMENT '错误原因',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `transfer_cluster_id` varchar(64) NOT NULL COMMENT '目标集群id',
  `is_continue` tinyint(4) DEFAULT '0' COMMENT '是否断点续传 0:不是 1:是',
  `is_default` tinyint(4) DEFAULT '0' COMMENT '是否是增量迁移 0:不是 1:是',
  `transfer_cluster_percent` varchar(64) DEFAULT NULL COMMENT '迁移集群的百分比',
  `project_id` varchar(64) DEFAULT NULL COMMENT '迁移到那个项目下',
  `err_namespace` varchar(255) DEFAULT NULL COMMENT '当前迁移失败的分区',
  `err_deploy` varchar(255) DEFAULT NULL COMMENT '当前迁移失败的应用',
  `old_cluster_id` varchar(64) DEFAULT NULL COMMENT '旧集群id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=74 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `transfer_cluster`;
CREATE TABLE `transfer_cluster` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `tenant_id` varchar(30) NOT NULL COMMENT '租户id',
  `cluster_id` varchar(30) NOT NULL COMMENT '目标集群id',
  `old_cluster_id` varchar(30) NOT NULL COMMENT '原集群id',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `is_transfer` tinyint(30) NOT NULL DEFAULT '0' COMMENT '是否迁移过 0:未迁移过 1:已迁移过',
  `is_continue` tinyint(30) NOT NULL DEFAULT '0' COMMENT '是否断点续传过 0:未断电续传 1:已断点续传',
  `is_err` int(4) NOT NULL DEFAULT '0' COMMENT '是否成功 0:成功 1:失败',
  `percent` varchar(255) DEFAULT NULL COMMENT '百分比',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `transfer_bind_namespace`;
CREATE TABLE `transfer_bind_namespace` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `current_namespace` varchar(255) NOT NULL COMMENT '原分区名称',
  `create_namespace` varchar(255) NOT NULL COMMENT '新建分区名称',
  `cluster_id` varchar(30) NOT NULL COMMENT '目标集群id',
  `tenant_id` varchar(30) NOT NULL COMMENT '租户id',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `is_delete` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除 0:未删除 1:已删除',
  `is_default` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否是默认名称分区 0:不是 1:是',
  `status` int(30) NOT NULL DEFAULT '0' COMMENT '状态 0:未迁移 1:已迁移',
  `err_msg` varchar(30) DEFAULT NULL COMMENT '错误原因',
  `namespace_num` int(30) NOT NULL DEFAULT '0' COMMENT '第几次迁移分区',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=231 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `transfer_bind_deploy`;
CREATE TABLE `transfer_bind_deploy` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `namespace` varchar(30) NOT NULL COMMENT '分区名称',
  `cluster_id` varchar(30) NOT NULL COMMENT '目标集群id',
  `deploy_name` varchar(30) NOT NULL COMMENT '服务名称',
  `step_id` int(11) DEFAULT NULL COMMENT '步骤id',
  `tenant_id` varchar(30) NOT NULL COMMENT '租户id',
  `project_id` varchar(30) NOT NULL COMMENT '项目id',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `status` int(30) NOT NULL DEFAULT '0' COMMENT '服务前移状态 0:未迁移 1:已迁移',
  `err_msg` varchar(30) DEFAULT NULL COMMENT '错误原因',
  `is_delete` tinyint(4) DEFAULT '0' COMMENT '是否删除 0:未删除 1:已删除',
  `deploy_num` int(30) DEFAULT NULL COMMENT '第几次迁移应用',
  `old_cluster_id` varchar(64) NOT NULL COMMENT '旧的集群id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=232 DEFAULT CHARSET=utf8;