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