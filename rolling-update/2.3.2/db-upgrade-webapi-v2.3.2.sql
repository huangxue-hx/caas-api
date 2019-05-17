-- crowd用户认证
alter table user add `crowd_user_id` int(11) DEFAULT NULL;

INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/users/sync', 'system', 'system');
insert into k8s_auth_server.url_dic(url,module,resource) values('/system/configs/crowd','system','system');

-- debug功能