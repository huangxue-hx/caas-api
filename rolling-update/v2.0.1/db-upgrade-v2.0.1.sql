--更新应用商店(集群组件)
ALTER TABLE `k8s_auth_server`.`service_templates`
ADD COLUMN `service_account` VARCHAR(255) NULL DEFAULT NULL AFTER `cluster_id`;

UPDATE `k8s_auth_server`.`service_templates` SET `deployment_content` = '[{"annotation":"","clusterIP":"","containers":[{"args":[],"command":[],"configmap":[],"env":[{"key":"TZ","value":"Asia/Shanghai"},{"key":"MINIMUM_MASTER_NODES","value":"1"}],"imagePullPolicy":"Always","img":"onlineshop/elasticsearch","livenessProbe":null,"log":"","name":"elasticsearch","ports":[{"containerPort":"","expose":"true","port":"9200","protocol":"TCP"},{"containerPort":"","expose":"true","port":"9300","protocol":"TCP"}],"readinessProbe":null,"resource":{"cpu":"1000m","memory":"1024"},"securityContext":{"add":[],"drop":[],"privileged":false,"security":false},"storage":[],"tag":"v6.2.5-1"}],"hostIPC":false,"hostName":"","hostPID":false,"instance":"1","labels":"","logPath":"","logService":"","name":"elasticsearch","namespace":"garydemo-garyns","nodeSelector":"HarmonyCloud_Status=C","restartPolicy":"Always","sessionAffinity":""}]' WHERE (`id` = '11' AND `name` = 'elasticsearch');
UPDATE `k8s_auth_server`.`service_templates` SET `service_account` = 'onlineshop' WHERE (`id` = '11' AND `name` = 'elasticsearch');