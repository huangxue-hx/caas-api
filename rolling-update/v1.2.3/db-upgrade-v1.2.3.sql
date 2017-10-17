--添加字段：区分应用模板公私有
ALTER TABLE `k8s_auth_server`.`business_templates` 
ADD COLUMN `is_public` TINYINT NULL DEFAULT 0 AFTER `image_list`;
--添加字段：区分服务模板公私有
ALTER TABLE `k8s_auth_server`.`service_templates` 
ADD COLUMN `is_public` TINYINT NULL DEFAULT 0 AFTER `image_list`;
--修改websphere模板
update k8s_auth_server.service_templates set deployment_content='[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[],\"command\":[],\"configmap\":[],\"env\":[],\"img\":\"onlineshop/websphere-traditional\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"websphere\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"30300\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"9080\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"1000m\",\"memory\":\"1024\"},\"securityContext\":{\"add\":[],\"drop\":[],\"privileged\":false,\"security\":false},\"storage\":[],\"tag\":\"8.5.5.9-install\"}],\"hostIPC\":false,\"hostName\":\"\",\"hostPID\":false,\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"websphere\",\"namespace\":\"garydemo-garyns\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]' where id=10 and name='websphere'
