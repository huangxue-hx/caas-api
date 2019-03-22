use k8s_auth_server;

INSERT INTO `k8s_auth_server`.`resource_menu_role` (`weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`)
VALUES ('41', '2019-01-14 17:28:00', '2019-01-14 17:28:00', '1', '1', '41');

INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/system/api-server/auditlogs', 'log', 'auditlog');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/system/api-server/auditlogs/count', 'log', 'auditlog');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/system/api-server/auditlogs/namespaces', 'log', 'auditlog');

INSERT INTO `k8s_auth_server`.`resource_menu` (`name`, `name_en`, `type`, `url`, `weight`, `create_time`, `update_time`, `available`, `icon_name`, `isparent`, `parent_rmid`, `module`)
VALUES ('集群审计', 'k8s Audit', 'menu', 'k8sAuditLog', '41', '2019-01-14 16:47:00', '2019-01-14 16:50:56', '1', '', '0', '9', 'k8sauditlog');