
INSERT INTO k8s_auth_server.application_templates VALUES('12', 'RedisCluster', 'latest-v2', '', '0', 'all', 'admin', '2018-05-08 10:44:42', '2018-05-08 10:44:42', NULL, 'onlineshop/redis-master,onlineshop/redis-slave', '0', 'all', '', '');

INSERT INTO k8s_auth_server.service_templates VALUES
('12', 'redis-master', '1.0', '', '[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[],\"command\":[],\"configmap\":[],\"env\":[{\"key\":\"TZ\",\"value\":\"Asia/Shanghai\"}],\"img\":\"onlineshop/redis-master\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"redis-master\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"6379\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"500m\",\"memory\":\"512\"},\"storage\":[],\"tag\":\"latest-v2\"}],\"hostName\":\"\",\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"redis-master\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]', 'onlineshop/redis-master', '0', '', '1', 'all', 'admin', '2018-05-08 10:44:14', '0', 'HarmonyCloud_Status=C', null, null);

INSERT INTO k8s_auth_server.service_templates(id, name, tag, details, deployment_content, image_list, is_public, ingress_content, status,
tenant, create_user, create_time, flag, node_selector, project_id, cluster_id)
SELECT AUTO_INCREMENT, 'redis-slave','1.0', '', '[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[],\"command\":[],\"configmap\":[],\"env\":[{\"key\":\"GET_HOSTS_FROM\",\"value\":\"env\"},{\"key\":\"TZ\",\"value\":\"Asia/Shanghai\"}],\"img\":\"onlineshop/redis-slave\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"redis-slave\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"6379\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"500m\",\"memory\":\"512\"},\"storage\":[],\"tag\":\"latest-v2\"}],\"hostName\":\"\",\"instance\":\"2\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"redis-slave\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]', 'onlineshop/redis-slave', '0', '', '1', 'all', 'admin', '2018-05-08 10:44:14', '0', 'HarmonyCloud_Status=C', null, null FROM information_schema.`TABLES` WHERE TABLE_SCHEMA='k8s_auth_server' AND TABLE_NAME='service_templates';

INSERT INTO k8s_auth_server.application_service(id, application_id, service_id, status, is_external)
 SELECT AUTO_INCREMENT, '12', '12', '0', '0' FROM information_schema.`TABLES` WHERE TABLE_SCHEMA='k8s_auth_server' AND TABLE_NAME='application_service';

 INSERT INTO k8s_auth_server.application_service(id, application_id, service_id, status, is_external)
 SELECT AUTO_INCREMENT, '12', max(k8s_auth_server.service_templates.id), '0', '0' FROM k8s_auth_server.service_templates, information_schema.`TABLES` WHERE TABLE_SCHEMA='k8s_auth_server' AND TABLE_NAME='application_service';


UPDATE k8s_auth_server.service_templates
SET deployment_content = '[{"annotation":"","clusterIP":"","containers":[{"args":[],"command":[],"configmap":[],"env":[{"key":"TZ","value":"Asia/Shanghai"}],"img":"onlineshop/redis","livenessProbe":null,"log":"","name":"redis","ports":[{"containerPort":"","expose":"true","port":"6379","protocol":"TCP"}],"readinessProbe":null,"resource":{"cpu":"100m","memory":"128"},"storage":[],"tag":"3.2-alpine"}],"hostName":"","instance":"1","labels":"","logPath":"","logService":"","name":"redis","namespace":"","nodeSelector":"HarmonyCloud_Status=C","restartPolicy":"Always","sessionAffinity":""}]'
WHERE
	id = 2;

ALTER TABLE k8s_auth_server.configfile MODIFY COLUMN item MEDIUMTEXT;