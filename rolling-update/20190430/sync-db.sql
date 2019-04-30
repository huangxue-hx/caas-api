alter table user add `crowd_user_id` int(11) DEFAULT NULL;

INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/users/sync', 'system', 'system');