--添加字段：区分应用模板公私有
ALTER TABLE `k8s_auth_server`.`business_templates` 
ADD COLUMN `is_public` TINYINT NULL DEFAULT 0 AFTER `image_list`;
--添加字段：区分服务模板公私有
ALTER TABLE `k8s_auth_server`.`service_templates` 
ADD COLUMN `is_public` TINYINT NULL DEFAULT 0 AFTER `image_list`;
