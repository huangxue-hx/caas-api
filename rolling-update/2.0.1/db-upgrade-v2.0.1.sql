ALTER TABLE k8s_auth_server.`user` ADD
login_fail_time VARCHAR(255) COMMENT '登陆失败时间',
ADD login_fail_count TINYINT(1)COMMENT '登陆失败次数';
#分别是账号锁定时间，失败次数上限，单位时间内
INSERT INTO k8s_auth_server.`system_config` (config_name,config_value,config_type,create_user) VALUES ('loginFailTimeLimit','1800','login','admin');
INSERT INTO k8s_auth_server.`system_config` (config_name,config_value,config_type,create_user)  VALUES ('loginFailCountLimit','10','login','admin');
INSERT INTO k8s_auth_server.`system_config` (config_name,config_value,config_type,create_user)  VALUES ('SingleTimeLimit','60','login','admin');