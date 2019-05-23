use k8s_auth_server;
-- debug功能
-- ----------------------------
--  Table structure for `debug_state`
-- ----------------------------
DROP TABLE IF EXISTS `debug_state`;
CREATE TABLE `debug_state` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(255) NOT NULL,
  `state` varchar(255) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `pod_name` varchar(255) DEFAULT NULL,
  `namespace` varchar(255) DEFAULT NULL,
  `service` varchar(255) DEFAULT NULL,
  `port` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8;


insert into `k8s_auth_server`.`url_dic` ( `module`, `url`, `resource`) values ( 'namespace', '/users/namespace/username/*', 'namespace');
insert into `k8s_auth_server`.`url_dic` ( `module`, `url`, `resource`) values ( 'namespace', '/users/namespace/username', 'namespace');
insert into `k8s_auth_server`.`url_dic` ( `module`, `url`, `resource`) values ( 'debug', '/namespaces/*/services/*/debug/start', 'debug');
insert into `k8s_auth_server`.`url_dic` ( `module`, `url`, `resource`) values ( 'debug', '/namespaces/*/services/*/debug/end', 'debug');
insert into `k8s_auth_server`.`url_dic` ( `module`, `url`, `resource`) values ( 'debug', '/namespaces/*/services/*/debug/command', 'debug');
insert into `k8s_auth_server`.`url_dic` ( `module`, `url`, `resource`) values ( 'debug', '/namespaces/*/services/*/debug/download/*', 'debug');
insert into `k8s_auth_server`.`url_dic` ( `module`, `url`, `resource`) values ( 'debug', '/namespaces/*/services/*/debug/test/link', 'debug');
insert into `k8s_auth_server`.`url_dic` ( `module`, `url`, `resource`) values ( 'debug', '/namespaces/*/services/*/debug/download', 'debug');
insert into `k8s_auth_server`.`url_dic` ( `module`, `url`, `resource`) values ( 'namespace', '/users/namespaces', 'namespace');
insert into `k8s_auth_server`.`url_dic` ( `module`, `url`, `resource`) values ( 'debug', '/users/debug/test', 'debug');
insert into `k8s_auth_server`.`url_dic` ( `module`, `url`, `resource`) values ( 'debug', '/namespaces/*/services/*/debug/test/service', 'debug');
insert into `k8s_auth_server`.`url_dic` ( `module`, `url`, `resource`) values ( 'service', '/users/namespaces/*/services', 'service');
-- crowd用户认证
alter table user add `crowd_user_id` int(11) DEFAULT NULL;

INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/users/sync', 'system', 'system');
insert into k8s_auth_server.url_dic(url,module,resource) values('/system/configs/crowd','system','system');
