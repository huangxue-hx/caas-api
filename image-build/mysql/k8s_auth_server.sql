/*
Navicat MySQL Data Transfer

Source Server         : 10.10.124.152
Source Server Version : 50635
Source Host           : 10.10.124.152:30306
Source Database       : k8s_auth_server

Target Server Type    : MYSQL
Target Server Version : 50635
File Encoding         : 65001

Date: 2018-01-25 14:41:44
*/
DROP DATABASE IF EXISTS `k8s_auth_server`;
CREATE DATABASE `k8s_auth_server`;

use `k8s_auth_server`;

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for application_service
-- ----------------------------
DROP TABLE IF EXISTS `application_service`;
CREATE TABLE `application_service` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `application_id` int(11) NOT NULL,
  `service_id` int(11) NOT NULL,
  `status` int(11) DEFAULT NULL,
  `is_external` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of application_service
-- ----------------------------
INSERT INTO `application_service` VALUES ('1', '1', '1', '0', '0');
INSERT INTO `application_service` VALUES ('2', '2', '2', '0', '0');
INSERT INTO `application_service` VALUES ('3', '3', '3', '0', '0');
INSERT INTO `application_service` VALUES ('4', '4', '4', '0', '0');
INSERT INTO `application_service` VALUES ('5', '5', '5', '0', '0');
INSERT INTO `application_service` VALUES ('6', '6', '6', '0', '0');
INSERT INTO `application_service` VALUES ('7', '7', '7', '0', '0');
INSERT INTO `application_service` VALUES ('8', '8', '8', '0', '0');
INSERT INTO `application_service` VALUES ('9', '9', '9', '0', '0');
INSERT INTO `application_service` VALUES ('10', '10', '10', '0', '0');
INSERT INTO `application_service` VALUES ('11', '11', '11', '0', '0');
INSERT INTO `application_service` VALUES ('12', '12', '12', '0', '0');
INSERT INTO `application_service` VALUES ('13', '13', '13', '0', '0');
INSERT INTO `application_service` VALUES ('14', '13', '14', '0', '0');
INSERT INTO `application_service` VALUES ('15', '13', '15', '0', '0');
INSERT INTO `application_service` VALUES ('16', '13', '16', '0', '0');
INSERT INTO `application_service` VALUES ('17', '13', '17', '0', '0');
INSERT INTO `application_service` VALUES ('18', '13', '18', '0', '0');
INSERT INTO `application_service` VALUES ('19', '13', '19', '0', '0');
INSERT INTO `application_service` VALUES ('20', '12', '20', '0', '0');
-- ----------------------------
-- Table structure for application_templates
-- ----------------------------
DROP TABLE IF EXISTS `application_templates`;
CREATE TABLE `application_templates` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) binary NOT NULL,
  `tag` varchar(45) DEFAULT NULL,
  `details` varchar(512) DEFAULT NULL,
  `status` int(1) DEFAULT NULL,
  `tenant` varchar(45) DEFAULT NULL,
  `create_user` varchar(45) DEFAULT NULL,
  `create_time` timestamp NULL DEFAULT NULL,
  `update_time` timestamp NULL DEFAULT NULL,
  `is_deploy` int(1) DEFAULT NULL,
  `image_list` varchar(2048) DEFAULT NULL,
  `is_public` tinyint(4) DEFAULT NULL,
  `project_id` varchar(64) DEFAULT NULL,
  `namespace_id` varchar(64) DEFAULT NULL,
  `cluster_id` varchar(64) DEFAULT NULL COMMENT '集群Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of application_templates
-- ----------------------------
INSERT INTO `application_templates` VALUES ('1', 'Tomcat', '8.0', '', '0', 'all', 'admin', '2017-08-11 14:18:36', '2017-08-11 14:18:36', '1', 'onlineshop/tomcat', '0', 'all', '', '');
INSERT INTO `application_templates` VALUES ('2', 'Redis', '3.2-alpine', '', '0', 'all', 'admin', '2017-08-11 14:44:14', '2017-08-11 14:18:36', '1', 'onlineshop/redis', '0', 'all', '', '');
INSERT INTO `application_templates` VALUES ('3', 'WordPress', '4.8.0-php7.1-fpm-alpine', '', '0', 'all', 'admin', '2017-08-11 15:31:54', '2017-08-11 14:18:36', '1', 'onlineshop/wordpress', '0', 'all', '', '');
INSERT INTO `application_templates` VALUES ('4', 'InfluxDB', 'v1.3.0', '', '0', 'all', 'admin', '2017-08-11 16:12:19', '2017-08-11 14:18:36', '1', 'onlineshop/influxdb', '0', 'all', '', '');
INSERT INTO `application_templates` VALUES ('5', 'MySQL', '5.7.6', '', '0', 'all', 'admin', '2017-08-11 18:03:10', '2017-08-11 14:18:36', '1', 'onlineshop/mysqls', '0', 'all', '', '');
INSERT INTO `application_templates` VALUES ('7', 'Mongodb', 'v3.5', '', '0', 'all', 'admin', '2017-09-05 17:02:28', '2017-08-11 14:18:36', null, 'onlineshop/mongodb', '0', 'all', '', '');
INSERT INTO `application_templates` VALUES ('8', 'Rabbitmq', '3.6.11', '', '0', 'all', 'admin', '2017-09-05 17:03:30', '2017-08-11 14:18:36', null, 'onlineshop/rabbitmq', '0', 'all', '', '');
INSERT INTO `application_templates` VALUES ('9', 'Nginx', 'latest', '', '0', 'all', 'admin', '2017-09-05 17:05:19', '2017-08-11 14:18:36', null, 'onlineshop/nginx', '0', 'all', '', '');
INSERT INTO `application_templates` VALUES ('10', 'Websphere', '8.5.5.9-install', '', '0', 'all', 'admin', '2017-09-05 17:06:07', '2017-08-11 14:18:36', null, 'onlineshop/websphere', '0', 'all', '', '');
INSERT INTO `application_templates` VALUES ('11', 'Elasticsearch', 'v2.4.1-1', '', '0', 'all', 'admin', '2017-09-05 17:07:25', '2017-08-11 14:18:36', null, 'onlineshop/elasticsearch', '0', 'all', '', '');
INSERT INTO `application_templates` VALUES ('12', 'RedisCluster', 'latest-v2', '', '0', 'all', 'admin', '2018-05-08 10:44:42', '2018-05-08 10:44:42', NULL, 'onlineshop/redis-master,onlineshop/redis-slave', '0', 'all', '', '');
INSERT INTO `application_templates` VALUES ('13', 'Fabric', '0.6', '', '0', 'all', 'admin', '2017-08-18 14:44:42', '2017-08-11 14:18:36', '1', 'onlineshop/fabric-peer,onlineshop/fabric-membersrvc', '0', 'all', '', '');

-- ----------------------------
-- Table structure for tenant_cluster_quota
-- ----------------------------
DROP TABLE IF EXISTS `tenant_cluster_quota`;
CREATE TABLE `tenant_cluster_quota` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id主键',
  `tenant_id` varchar(64) NOT NULL COMMENT '租户id',
  `cluster_id` varchar(64) NOT NULL COMMENT '集群id',
  `cpu_quota` double(16,2) NOT NULL DEFAULT '0.00' COMMENT '集群租户的cpu配额(core)',
  `memory_quota` double(16,2) NOT NULL DEFAULT '0.00' COMMENT '集群租户的内存配额(MB)',
  `pv_quota` double(11,2) NOT NULL DEFAULT '0.00' COMMENT '集群租户的存储配额(MB)',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `cluster_name` varchar(64) DEFAULT NULL COMMENT '集群名',
  `reserve1` varchar(255) DEFAULT NULL COMMENT '预留字段',
  PRIMARY KEY (`id`),
  KEY `tenant` (`tenant_id`,`cluster_id`,`reserve1`) USING BTREE,
  KEY `cluster` (`cluster_id`,`reserve1`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for cicd_build_environment
-- ----------------------------
DROP TABLE IF EXISTS `cicd_build_environment`;
CREATE TABLE `cicd_build_environment` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `name` varchar(255) DEFAULT NULL COMMENT '环境名',
  `image` varchar(255) DEFAULT NULL COMMENT '镜像名',
  `project_id` varchar(64) DEFAULT NULL COMMENT '项目id',
  `cluster_id` varchar(64) DEFAULT NULL COMMENT '集群id',
  `is_public` tinyint(1) DEFAULT NULL COMMENT '是否公有',
  `create_user` varchar(255) DEFAULT NULL COMMENT '创建',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_user` varchar(255) DEFAULT NULL COMMENT '修改用户',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT;

-- ----------------------------
-- Records of cicd_build_environment
-- ----------------------------
INSERT INTO `cicd_build_environment`(id,name,image,is_public) VALUES(0, 'default', 'library/jenkins-slave-java:latest', NULL);

-- ----------------------------
-- Table structure for cicd_docker_file
-- ----------------------------
DROP TABLE IF EXISTS `cicd_docker_file`;
CREATE TABLE `cicd_docker_file` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL COMMENT '名称',
  `tenant` varchar(50) DEFAULT NULL,
  `project_id` varchar(50) NOT NULL COMMENT '租户',
  `cluster_id` varchar(64) DEFAULT NULL,
  `content` text NOT NULL COMMENT '内容',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT;

-- ----------------------------
-- Records of cicd_docker_file
-- ----------------------------

-- ----------------------------
-- Table structure for cicd_job
-- ----------------------------
DROP TABLE IF EXISTS `cicd_job`;
CREATE TABLE `cicd_job` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `uuid` varchar(32) DEFAULT NULL,
  `name` varchar(100) DEFAULT NULL COMMENT '流水线名',
  `description` varchar(1000) DEFAULT NULL COMMENT '流水线描述',
  `type` varchar(2) DEFAULT NULL COMMENT '流水线类型',
  `tenant_id` varchar(50) DEFAULT NULL,
  `project_id` varchar(50) DEFAULT NULL COMMENT '项目Id',
  `cluster_id` varchar(64) DEFAULT NULL COMMENT '集群Id',
  `notification` tinyint(1) DEFAULT NULL COMMENT '是否通知，1-是，0-否',
  `success_notification` tinyint(1) DEFAULT NULL COMMENT '构建成功是否通知，1-是，0-否',
  `fail_notification` tinyint(1) DEFAULT NULL COMMENT '构建失败是否通知，1-是，0-否',
  `mail` varchar(1000) DEFAULT NULL COMMENT '通知邮件列表',
  `create_user` varchar(100) DEFAULT NULL COMMENT '创建人',
  `update_user` varchar(100) DEFAULT NULL COMMENT '更新人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `last_build_num` int(11) DEFAULT NULL COMMENT '最新构建数',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT;

-- ----------------------------
-- Records of cicd_job
-- ----------------------------

-- ----------------------------
-- Table structure for cicd_job_build
-- ----------------------------
DROP TABLE IF EXISTS `cicd_job_build`;
CREATE TABLE `cicd_job_build` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `job_id` int(11) DEFAULT NULL,
  `build_num` int(11) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `start_time` datetime DEFAULT NULL,
  `duration` varchar(20) DEFAULT NULL,
  `start_user` varchar(20) DEFAULT NULL,
  `log` longtext,
  PRIMARY KEY (`id`),
  KEY `job_num` (`job_id`,`build_num`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of cicd_job_build
-- ----------------------------

-- ----------------------------
-- Table structure for cicd_parameter
-- ----------------------------
DROP TABLE IF EXISTS `cicd_parameter`;
CREATE TABLE `cicd_parameter` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `job_id` int(11) NOT NULL COMMENT '流水线id',
  `type` int(2) NOT NULL COMMENT '参数类型，1-单字符串，2-多选项下拉框',
  `name` varchar(255) NOT NULL COMMENT '参数名',
  `value` varchar(1024) NOT NULL COMMENT '参数值',
  `description` varchar(255) DEFAULT NULL COMMENT '参数描述',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=36 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of cicd_parameter
-- ----------------------------

-- ----------------------------
-- Table structure for cicd_stage
-- ----------------------------
DROP TABLE IF EXISTS `cicd_stage`;
CREATE TABLE `cicd_stage` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `job_id` int(11) NOT NULL COMMENT '流水线id',
  `stage_order` int(11) DEFAULT NULL COMMENT '步骤顺序',
  `stage_type_id` varchar(20) DEFAULT NULL COMMENT '步骤类型id',
  `stage_name` varchar(20) DEFAULT NULL COMMENT '步骤名称',
  `repository_type` varchar(10) DEFAULT NULL COMMENT '代码仓库类型',
  `repository_url` varchar(250) DEFAULT NULL COMMENT '代码仓库地址',
  `repository_branch` varchar(50) DEFAULT NULL COMMENT '分支',
  `credentials_username` varchar(50) DEFAULT NULL COMMENT '仓库用户名',
  `credentials_password` varchar(50) DEFAULT NULL COMMENT '仓库密码',
  `environment_change` tinyint(1) DEFAULT NULL,
  `build_environment_id` int(11) DEFAULT NULL COMMENT '环境id',
  `environment_variables` varchar(1000) DEFAULT NULL COMMENT '环境变量',
  `use_dependency` int(1) DEFAULT NULL COMMENT '是否使用依赖，1-是，2-否',
  `dependences` varchar(1000) DEFAULT NULL COMMENT '依赖列表',
  `dockerfile_type` int(1) DEFAULT NULL COMMENT 'Dockerfile来源类型',
  `base_image` varchar(50) DEFAULT NULL COMMENT '基础镜像名',
  `dockerfile_id` int(11) DEFAULT NULL COMMENT 'Dcokerfile Id',
  `dockerfile_path` varchar(250) DEFAULT NULL COMMENT 'Dockerfile地址',
  `image_type` varchar(10) DEFAULT NULL,
  `image_name` varchar(50) DEFAULT NULL COMMENT '镜像名',
  `image_tag_type` varchar(50) DEFAULT NULL COMMENT '镜像tag类型',
  `image_base_tag` varchar(50) DEFAULT NULL COMMENT '基础镜像tag',
  `image_increase_tag` varchar(50) DEFAULT NULL COMMENT '递增tag',
  `image_tag` varchar(50) DEFAULT NULL COMMENT '镜像tag',
  `namespace` varchar(50) DEFAULT NULL COMMENT '分区',
  `origin_stage_id` int(11) DEFAULT NULL COMMENT 'ci镜像来源步骤id',
  `service_name` varchar(50) DEFAULT NULL COMMENT '服务名',
  `container_name` varchar(50) DEFAULT NULL COMMENT '容器名称',
  `service_template_name` varchar(50) DEFAULT NULL COMMENT '应用模板id',
  `service_template_tag` varchar(50) DEFAULT NULL COMMENT '服务模板id',
  `harbor_project` varchar(50) DEFAULT NULL,
  `deploy_type` varchar(10) DEFAULT NULL COMMENT '发布方式，0-模板发布，1-灰度，2-蓝绿',
  `configuration` text COMMENT '配置列表',
  `instances` int(11) DEFAULT NULL,
  `max_surge` int(11) DEFAULT NULL,
  `max_unavailable` int(11) DEFAULT NULL,
  `command` varchar(4096) DEFAULT NULL COMMENT '脚本命令',
  `suite_id` varchar(50) DEFAULT NULL COMMENT '静态扫描或测试套件id',
  `create_user` varchar(50) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_user` varchar(50) CHARACTER SET utf8mb4 DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `job_id` (`job_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT;

-- ----------------------------
-- Records of cicd_stage
-- ----------------------------

-- ----------------------------
-- Table structure for cicd_stage_build
-- ----------------------------
DROP TABLE IF EXISTS `cicd_stage_build`;
CREATE TABLE `cicd_stage_build` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `job_id` int(11) DEFAULT NULL COMMENT '流水线',
  `stage_id` int(11) DEFAULT NULL COMMENT '步骤id',
  `build_num` int(11) DEFAULT NULL COMMENT '构建次数',
  `status` varchar(20) DEFAULT NULL COMMENT '状态',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `duration` varchar(20) DEFAULT NULL COMMENT '运行时间',
  `stage_name` varchar(255) DEFAULT NULL COMMENT '步骤名称',
  `stage_order` int(11) DEFAULT NULL COMMENT '步骤顺序',
  `stage_type_id` int(11) DEFAULT NULL,
  `stage_type` varchar(255) DEFAULT NULL COMMENT '步骤类型',
  `stage_template_type_id` int(11) DEFAULT NULL,
  `image` varchar(255) DEFAULT NULL COMMENT '部署镜像',
  `test_result` varchar(255) DEFAULT NULL COMMENT '集成测试结果',
  `test_url` varchar(255) DEFAULT NULL COMMENT '集成测试结果链接',
  `log` mediumtext,
  PRIMARY KEY (`id`),
  KEY `stage_num` (`stage_id`,`build_num`) USING BTREE,
  KEY `jobid` (`job_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT;

-- ----------------------------
-- Records of cicd_stage_build
-- ----------------------------

-- ----------------------------
-- Table structure for cicd_stage_type
-- ----------------------------
DROP TABLE IF EXISTS `cicd_stage_type`;
CREATE TABLE `cicd_stage_type` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `name` varchar(100) DEFAULT NULL COMMENT '步骤类型名称',
  `type` varchar(2) DEFAULT NULL COMMENT 'CICD类型',
  `template_type` int(2) DEFAULT NULL COMMENT '步骤模板类型',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT;

-- ----------------------------
-- Records of cicd_stage_type
-- ----------------------------
INSERT INTO `cicd_stage_type` VALUES ('1', '代码检出/编译', 'ci', '0');
INSERT INTO `cicd_stage_type` VALUES ('2', '单元测试', 'ci', '3');
INSERT INTO `cicd_stage_type` VALUES ('3', '镜像构建', 'ci', '1');
INSERT INTO `cicd_stage_type` VALUES ('4', '静态扫描', 'ci', '7');
INSERT INTO `cicd_stage_type` VALUES ('5', '自定义', 'ci', '6');
INSERT INTO `cicd_stage_type` VALUES ('6', '应用部署', 'cd', '2');
INSERT INTO `cicd_stage_type` VALUES ('7', '集成测试', 'cd', '8');
INSERT INTO `cicd_stage_type` VALUES ('8', '自定义', 'cd', '6');

-- ----------------------------
-- Table structure for cicd_trigger
-- ----------------------------
DROP TABLE IF EXISTS `cicd_trigger`;
CREATE TABLE `cicd_trigger` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `job_id` int(11) DEFAULT NULL COMMENT '流水线id',
  `is_valid` tinyint(1) DEFAULT NULL COMMENT '是否生效',
  `type` int(1) DEFAULT NULL COMMENT '类型：1-定时，2-pollSCM，3-webhook，4-流水线触发',
  `is_customised` int(1) DEFAULT NULL COMMENT '是否自定义cron',
  `cron_exp` varchar(255) DEFAULT NULL COMMENT 'cron表达式',
  `trigger_job_id` int(11) DEFAULT NULL,
  `trigger_image` varchar(255) DEFAULT NULL COMMENT '触发镜像',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_job_id` (`job_id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of cicd_trigger
-- ----------------------------

-- ----------------------------
-- Table structure for configfile
-- ----------------------------
DROP TABLE IF EXISTS `configfile`;
CREATE TABLE `configfile` (
  `id` varchar(64) NOT NULL,
  `name` varchar(255) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `tags` varchar(255) NOT NULL,
  `create_time` varchar(255) DEFAULT NULL,
  `project_id` varchar(64) NOT NULL,
  `tenant_id` varchar(64) DEFAULT NULL,
  `cluster_id` varchar(64) DEFAULT NULL,
  `cluster_name` varchar(64) DEFAULT NULL,
  `reponame` varchar(255) DEFAULT NULL,
  `creator` varchar(255) DEFAULT NULL,
  `item` MEDIUMTEXT NOT NULL,
  `path` varchar(512) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_name` (`cluster_id`, `project_id`, `name`, `tags`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of configfile
-- ----------------------------

-- ----------------------------
-- Table structure for django_migrations
-- ----------------------------
DROP TABLE IF EXISTS `django_migrations`;
CREATE TABLE `django_migrations` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `app` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `applied` datetime(6) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of django_migrations
-- ----------------------------

-- ----------------------------
-- Table structure for external_type
-- ----------------------------
DROP TABLE IF EXISTS `external_type`;
CREATE TABLE `external_type` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `type` varchar(255) NOT NULL DEFAULT '' COMMENT '外部服务类型',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of external_type
-- ----------------------------
INSERT INTO `external_type` VALUES ('1', '全部');
INSERT INTO `external_type` VALUES ('2', 'mysql');
INSERT INTO `external_type` VALUES ('3', 'oracle');
INSERT INTO `external_type` VALUES ('4', 'zookeeper');
INSERT INTO `external_type` VALUES ('5', 'storm');
INSERT INTO `external_type` VALUES ('6', 'flume');
INSERT INTO `external_type` VALUES ('7', 'redis');
INSERT INTO `external_type` VALUES ('8', 'memcached');
INSERT INTO `external_type` VALUES ('9', 'kafka');
INSERT INTO `external_type` VALUES ('10', 'other');

-- ----------------------------
-- Table structure for file_upload_container
-- ----------------------------
DROP TABLE IF EXISTS `file_upload_container`;
CREATE TABLE `file_upload_container` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `container_file_path` varchar(500) NOT NULL DEFAULT '',
  `file_name` varchar(500) NOT NULL DEFAULT '',
  `user_id` bigint(20) DEFAULT NULL,
  `namespace` varchar(500) NOT NULL DEFAULT '',
  `deployment` varchar(500) NOT NULL DEFAULT '',
  `pod` varchar(500) NOT NULL DEFAULT '',
  `container` varchar(500) NOT NULL DEFAULT '',
  `phase` int(11) DEFAULT NULL COMMENT '标记文件上传阶段（1：上传到节点,2：上传到容器）',
  `status` varchar(45) DEFAULT NULL COMMENT 'failed, success, doing',
  `err_msg` varchar(500) DEFAULT NULL COMMENT '错误信息',
  `create_time` timestamp NULL DEFAULT NULL,
  `update_time` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of file_upload_container
-- ----------------------------

-- ----------------------------
-- Table structure for image_clean_rule
-- ----------------------------
DROP TABLE IF EXISTS `image_clean_rule`;
CREATE TABLE `image_clean_rule` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(256) DEFAULT NULL COMMENT '规则名称',
  `type` tinyint(1) NOT NULL COMMENT '1-针对仓库规则，2-针对镜像规则',
  `repository_id` int(11) NOT NULL COMMENT '镜像仓库名',
  `harbor_project_name` varchar(64) DEFAULT NULL COMMENT '镜像仓库名称',
  `repo_name` varchar(64) DEFAULT NULL COMMENT '具体镜像名，type=2时有值',
  `keep_tag_count` smallint(4) DEFAULT NULL COMMENT '需要保留的版本数量',
  `time_before` smallint(4) DEFAULT NULL COMMENT '清除这个时间点之前创建的镜像',
  `tag_name_exclude` varchar(64) DEFAULT NULL COMMENT '版本号包含此文本的排除，不会被清理',
  `created_time` datetime NOT NULL,
  `updated_time` datetime DEFAULT NULL COMMENT '更新时间',
  `user_name` varchar(64) DEFAULT NULL COMMENT '操作人',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of image_clean_rule
-- ----------------------------

-- ----------------------------
-- Table structure for image_repository
-- ----------------------------
DROP TABLE IF EXISTS `image_repository`;
CREATE TABLE `image_repository` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `harbor_project_id` int(11) DEFAULT NULL COMMENT 'harbor仓库id',
  `harbor_project_name` varchar(64) NOT NULL COMMENT 'harbor仓库名称',
  `repository_name` varchar(255) DEFAULT NULL COMMENT '用户自定义镜像仓库名，可中文',
  `tenant_id` varchar(255) DEFAULT NULL COMMENT '租户id',
  `cluster_id` varchar(64) DEFAULT NULL COMMENT '集群id',
  `cluster_name` varchar(64) DEFAULT NULL COMMENT '集群名称',
  `harbor_host` varchar(64) DEFAULT NULL,
  `project_id` varchar(64) DEFAULT NULL COMMENT '项目id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `is_default` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否默认镜像仓库',
  `is_public` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否公有仓库',
  `is_normal` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否正常，0-不正常（harbor创建仓库失败），1-正常',
  PRIMARY KEY (`id`),
  UNIQUE KEY ` uniq_harbor_project_name` (`harbor_project_name`,`harbor_host`) USING BTREE,
  KEY `idx_cluster_project` (`project_id`,`cluster_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for installprogress_installprogress
-- ----------------------------
DROP TABLE IF EXISTS `installprogress_installprogress`;
CREATE TABLE `installprogress_installprogress` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(30) NOT NULL,
  `progress` int(11) NOT NULL,
  `install_status` varchar(255) DEFAULT NULL,
  `cluster_id` varchar(64) NOT NULL,
  `error_msg` varchar(255) DEFAULT NULL COMMENT '错误信息',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of installprogress_installprogress
-- ----------------------------

-- ----------------------------
-- Table structure for local_privilege
-- ----------------------------
DROP TABLE IF EXISTS `local_privilege`;
CREATE TABLE `local_privilege` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增主键id',
  `local_role_id` int(11) DEFAULT NULL COMMENT '局部角色编号',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `resource_type` varchar(64) DEFAULT NULL COMMENT '资源类型',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `available` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否可用',
  `reserve1` varchar(255) DEFAULT NULL COMMENT '预留字段',
  `reserve2` varchar(255) DEFAULT NULL COMMENT '预留字段',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='局部角色权限规则定义表';

-- ----------------------------
-- Records of local_privilege
-- ----------------------------

-- ----------------------------
-- Table structure for local_role
-- ----------------------------
DROP TABLE IF EXISTS `local_role`;
CREATE TABLE `local_role` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增长主键id',
  `name` varchar(64) DEFAULT NULL COMMENT '角色名',
  `description` varchar(255) DEFAULT NULL COMMENT '角色描述',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `available` tinyint(1) DEFAULT NULL COMMENT '是否可用',
  `project_id` varchar(64) DEFAULT NULL COMMENT '集群id',
  `namespaces` varchar(64) DEFAULT NULL COMMENT '分区名（预留暂时不用）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='局部角色表';

-- ----------------------------
-- Records of local_role
-- ----------------------------

-- ----------------------------
-- Table structure for local_role_privilege
-- ----------------------------
DROP TABLE IF EXISTS `local_role_privilege`;
CREATE TABLE `local_role_privilege` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增主键id',
  `resource_type` varchar(64) DEFAULT NULL COMMENT '资源类型',
  `resource_id` varchar(64) DEFAULT NULL COMMENT '资源id',
  `local_role_id` int(11) DEFAULT NULL COMMENT '局部角色id',
  `condition_value` varchar(512) DEFAULT NULL COMMENT '条件实例',
  `condition_type` smallint(4) DEFAULT NULL COMMENT '条件类型：1-json ；2-自定义',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `available` tinyint(1) DEFAULT '0' COMMENT '是否可用',
  `reserve2` varchar(255) DEFAULT NULL COMMENT '预留字段',
  `reserve1` varchar(255) DEFAULT NULL COMMENT '预留字段',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='局部角色权限实例表';

-- ----------------------------
-- Records of local_role_privilege
-- ----------------------------

-- ----------------------------
-- Table structure for local_user_role_relationship
-- ----------------------------
DROP TABLE IF EXISTS `local_user_role_relationship`;
CREATE TABLE `local_user_role_relationship` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增主键id',
  `user_name` varchar(64) DEFAULT NULL COMMENT '用户名',
  `project_id` varchar(64) DEFAULT NULL COMMENT '项目id',
  `local_role_id` int(11) DEFAULT NULL COMMENT '局部角色编号',
  `has_local_role` tinyint(1) DEFAULT '0' COMMENT '是否有局部角色',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `available` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否可用',
  `reserve1` varchar(255) DEFAULT NULL COMMENT '预留字段',
  `reserve2` varchar(255) DEFAULT NULL COMMENT '预留字段',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='局部角色用户关系表';

-- ----------------------------
-- Records of local_user_role_relationship
-- ----------------------------

-- ----------------------------
-- Table structure for log_backup_rule
-- ----------------------------
DROP TABLE IF EXISTS `log_backup_rule`;
CREATE TABLE `log_backup_rule` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `cluster_id` varchar(64) NOT NULL COMMENT '集群编号',
  `backup_dir` varchar(255) DEFAULT NULL COMMENT '备份目录',
  `days_before` int(11) DEFAULT NULL COMMENT '备份多少天前的日志',
  `days_duration` int(11) DEFAULT NULL COMMENT '每次备份需要备份多少天的日志作为一个存储快照',
  `max_snapshot_speed` varchar(32) DEFAULT NULL COMMENT '创建存储快照传输到nfs的最大速率，单位mb',
  `max_restore_speed` varchar(32) DEFAULT NULL COMMENT '恢复存储快照到es服务的最大速率，单位mb',
  `last_backup_time` datetime DEFAULT NULL COMMENT '上次备份时间',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `available` tinyint(1) NOT NULL COMMENT '是否可用',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of log_backup_rule
-- ----------------------------

-- ----------------------------
-- Table structure for message
-- ----------------------------
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `app_key` varchar(255) NOT NULL,
  `server_url` varchar(255) NOT NULL,
  `app_secret` varchar(255) NOT NULL,
  `nonce` varchar(255) NOT NULL,
  `templateid` varchar(255) NOT NULL,
  `annotation` varchar(255) DEFAULT NULL COMMENT '短信接口供应商',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of message
-- ----------------------------

-- ----------------------------
-- Table structure for msf_instance
-- ----------------------------
DROP TABLE IF EXISTS `msf_instance`;
CREATE TABLE `msf_instance` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增序号',
  `tenant_id` varchar(255) NOT NULL COMMENT '租户Id，对应tenant_binding_new表的tenant_id',
  `cluster_id` varchar(64) NOT NULL COMMENT '集群Id,对应cluster表的主键id',
  `namespace_id` varchar(255) NOT NULL COMMENT '分区Id,对应namespace表的namespace_id',
  `instance_id` varchar(255) NOT NULL COMMENT '微服务组件实例Id',
  `replicas` int(11) NOT NULL COMMENT '微服务组件副本数',
  `cpu` varchar(255) DEFAULT NULL COMMENT '微服务组件实例CPU',
  `memory` varchar(255) DEFAULT NULL COMMENT '微服务组件内存',
  `service_name` varchar(255) NOT NULL COMMENT '微服务组件对应k8s的Service名称',
  `deployment_name` varchar(255) NOT NULL COMMENT '微服务组件对应k8s的Deployment名称',
  `loadbalance_port` varchar(64) DEFAULT NULL COMMENT '微服务组件对外暴露的负载均衡端口',
  `task_id` varchar(255) DEFAULT NULL COMMENT '任务Id，对应msf_operation_task表的task_id',
  `status` int(11) DEFAULT NULL COMMENT '组件状态',
  `create_time` timestamp NULL DEFAULT NULL COMMENT '记录创建时间',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '记录更新时间',
  `content` varchar(1024) DEFAULT NULL COMMENT '微服务初始化组件的请求参数',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of msf_instance
-- ----------------------------

-- ----------------------------
-- Table structure for msf_operation_task
-- ----------------------------
DROP TABLE IF EXISTS `msf_operation_task`;
CREATE TABLE `msf_operation_task` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增序号',
  `task_id` varchar(255) NOT NULL COMMENT '任务Id',
  `status` int(11) DEFAULT NULL COMMENT '任务状态，1：成功；2：失败；3：进行中',
  `task_type` int(11) NOT NULL COMMENT '任务类型，0:部署；1：删除；2：重置',
  `error_msg` varchar(1024) DEFAULT NULL COMMENT '任务执行错误信息',
  `namespace_id` varchar(64) DEFAULT NULL COMMENT '分区Id，对应namespace表的namespace_id',
  `app_template_id` int(11) DEFAULT NULL COMMENT '对应应用模板Id',
  `create_time` timestamp NULL DEFAULT NULL COMMENT '记录创建时间',
  `update_time` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of msf_operation_task
-- ----------------------------

-- ----------------------------
-- Table structure for namespace
-- ----------------------------
DROP TABLE IF EXISTS `namespace`;
CREATE TABLE `namespace` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'namespace主键',
  `alias_name` varchar(128) DEFAULT NULL COMMENT '分区别名',
  `namespace_name` varchar(128) NOT NULL COMMENT 'namespace名字',
  `namespace_id` varchar(64) NOT NULL COMMENT 'namespace id',
  `tenant_id` varchar(50) NOT NULL COMMENT '租户id',
  `cluster_id` varchar(128) NOT NULL COMMENT '集群id',
  `cluster_alias_name` varchar(128) DEFAULT NULL COMMENT '集群别名',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `is_private` tinyint(1) DEFAULT NULL COMMENT '是否私有分区',
  `cluster_name` varchar(128) DEFAULT NULL COMMENT '集群名',
  `reserve1` varchar(255) DEFAULT NULL COMMENT '预留字段',
  `reserve2` varchar(255) DEFAULT NULL COMMENT '预留字段',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of namespace
-- ----------------------------

-- ----------------------------
-- Table structure for network
-- ----------------------------
DROP TABLE IF EXISTS `network`;
CREATE TABLE `network` (
  `tenantid` varchar(255) NOT NULL,
  `tenantname` varchar(255) NOT NULL,
  `networkid` varchar(255) NOT NULL,
  `networkname` varchar(255) NOT NULL,
  `createtime` datetime DEFAULT NULL,
  `updatetime` datetime DEFAULT NULL,
  `annotation` varchar(255) DEFAULT NULL,
  `id` int(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of network
-- ----------------------------

-- ----------------------------
-- Table structure for network_topology
-- ----------------------------
DROP TABLE IF EXISTS `network_topology`;
CREATE TABLE `network_topology` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `net_id` varchar(255) NOT NULL,
  `net_name` varchar(255) NOT NULL,
  `topology` varchar(255) NOT NULL,
  `createtime` datetime DEFAULT NULL,
  `updatetime` datetime DEFAULT NULL,
  `destinationid` varchar(255) NOT NULL,
  `destinationname` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of network_topology
-- ----------------------------

-- ----------------------------
-- Table structure for node_drain_progress
-- ----------------------------
DROP TABLE IF EXISTS `node_drain_progress`;
CREATE TABLE `node_drain_progress` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `node_name` varchar(64) NOT NULL,
  `status` varchar(16) NOT NULL,
  `pod_total_num` int(11) NOT NULL,
  `progress` varchar(1024) DEFAULT NULL,
  `cluster_id` varchar(64) NOT NULL,
  `error_msg` text DEFAULT NULL,
  `create_time` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of node_drain_progress
-- ----------------------------

-- ----------------------------
-- Table structure for nodeport_cluster_usage
-- ----------------------------
DROP TABLE IF EXISTS `nodeport_cluster_usage`;
CREATE TABLE `nodeport_cluster_usage` (
  `nodeport` int(11) NOT NULL,
  `cluster_id` varchar(64) NOT NULL,
  `status` int(11) NOT NULL,
  `create_time` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of nodeport_cluster_usage
-- ----------------------------

-- ----------------------------
-- Table structure for private_partition
-- ----------------------------
DROP TABLE IF EXISTS `private_partition`;
CREATE TABLE `private_partition` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `tenant_id` varchar(255) DEFAULT NULL,
  `tenant_name` varchar(255) NOT NULL,
  `namespace` varchar(255) NOT NULL,
  `is_private` int(10) NOT NULL DEFAULT '0' COMMENT '是否是私有分区，0共享分区，1表示私有分区',
  `label` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of private_partition
-- ----------------------------

/*
Navicat MySQL Data Transfer

Source Server         : 10.10.124.46
Source Server Version : 50635
Source Host           : 10.10.124.46:30306
Source Database       : k8s_auth_server

Target Server Type    : MYSQL
Target Server Version : 50635
File Encoding         : 65001

Date: 2018-04-09 19:37:26
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for privilege
-- ----------------------------
DROP TABLE IF EXISTS `privilege`;
CREATE TABLE `privilege` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `module` varchar(64) NOT NULL COMMENT '模块',
  `module_name` varchar(64) NOT NULL COMMENT '权限中文名称',
  `resource` varchar(64) NOT NULL COMMENT '资源名称',
  `resource_name` varchar(64) NOT NULL COMMENT '资源中文名称',
  `privilege` varchar(16) NOT NULL COMMENT '权限,post/get/delete/put/execute',
  `privilege_name` varchar(128) NOT NULL COMMENT '权限中文描述',
  `remark` varchar(128) DEFAULT NULL COMMENT '说明',
  `remark_name` varchar(128) DEFAULT NULL COMMENT '说明',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否可用 1可用 0不可用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_privilege` (`module`,`resource`,`privilege`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=95 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of privilege
-- ----------------------------
INSERT INTO `privilege` VALUES ('1', 'dashboard', '总览', 'overview', '总览', 'get', '总览查询', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('2', 'infrastructure', '基础设施', 'clustermgr', '集群管理', 'get', '集群查询', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('3', 'infrastructure', '基础设施', 'clustermgr', '集群管理', 'create', '集群添加', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('4', 'infrastructure', '基础设施', 'clustermgr', '集群管理', 'update', '集群修改', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('5', 'infrastructure', '基础设施', 'clustermgr', '集群管理', 'delete', '集群删除', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('6', 'infrastructure', '基础设施', 'node', '集群节点', 'get', '主机查询', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('7', 'infrastructure', '基础设施', 'node', '集群节点', 'create', '主机添加', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('8', 'infrastructure', '基础设施', 'node', '集群节点', 'update', '主机修改', 'node label modification', '主机标签修改', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('9', 'infrastructure', '基础设施', 'node', '集群节点', 'delete', '主机删除', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('10', 'tenant', '租户', 'basic', '基础操作', 'get', '租户列表查询', 'query tenant list', '查询租户列表', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('11', 'tenant', '租户', 'basic', '基础操作', 'create', '租户创建', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('12', 'tenant', '租户', 'basic', '基础操作', 'update', '租户修改', 'change the tenant administrator，update tenant quotas', '更改租户管理员，修改配额', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('13', 'tenant', '租户', 'basic', '基础操作', 'delete', '租户删除', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('14', 'tenant', '租户', 'tenantmgr', '租户管理', 'get', '查询', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('15', 'tenant', '租户', 'tenantmgr', '租户管理', 'create', '增加', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('16', 'tenant', '租户', 'tenantmgr', '租户管理', 'update', '修改', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('17', 'tenant', '租户', 'tenantmgr', '租户管理', 'delete', '删除', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('18', 'tenant', '租户', 'projectmgr', '项目管理', 'get', '查询', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('19', 'tenant', '租户', 'projectmgr', '项目管理', 'create', '增加', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('20', 'tenant', '租户', 'projectmgr', '项目管理', 'update', '修改', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('21', 'tenant', '租户', 'projectmgr', '项目管理', 'delete', '删除', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('22', 'tenant', '租户', 'networkpolicy', '网络策略', 'get', '网络白名单查询', '', '', '0', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('23', 'tenant', '租户', 'networkpolicy', '网络策略', 'create', '网络白名单创建', '', '', '0', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('24', 'tenant', '租户', 'networkpolicy', '网络策略', 'update', '网络白名单修改', '', '', '0', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('25', 'tenant', '租户', 'networkpolicy', '网络策略', 'delete', '网络白名单删除', '', '', '0', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('26', 'delivery', '交付中心', 'repository', '镜像仓库', 'get', '镜像仓库查询', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('27', 'delivery', '交付中心', 'repository', '镜像仓库', 'create', '镜像仓库创建', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('28', 'delivery', '交付中心', 'repository', '镜像仓库', 'update', '镜像仓库修改', 'update the mirrored warehouse quota', '镜像仓库配额修改', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('29', 'delivery', '交付中心', 'repository', '镜像仓库', 'delete', '镜像仓库删除', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('30', 'delivery', '交付中心', 'image', '镜像', 'get', '镜像查询/下载', 'query image details、image security analysis and download', '查看镜像详情、镜像安全分析以及镜像下载', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('31', 'delivery', '交付中心', 'image', '镜像', 'create', '镜像上传', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('32', 'delivery', '交付中心', 'image', '镜像', 'update', '镜像推送', '镜像推送到其他环境对应的仓库', '镜像推送到其他环境对应的仓库', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('33', 'delivery', '交付中心', 'image', '镜像', 'delete', '镜像删除', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('34', 'delivery', '交付中心', 'repositorymgr', '镜像仓库管理', 'get', '查询', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('35', 'delivery', '交付中心', 'repositorymgr', '镜像仓库管理', 'create', '增加', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('36', 'delivery', '交付中心', 'repositorymgr', '镜像仓库管理', 'update', '修改', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('37', 'delivery', '交付中心', 'repositorymgr', '镜像仓库管理', 'delete', '删除', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('38', 'delivery', '交付中心', 'template', '模板', 'get', '服务模板查询', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('39', 'delivery', '交付中心', 'template', '模板', 'create', '服务模板创建', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('40', 'delivery', '交付中心', 'template', '模板', 'update', '服务模板修改', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('41', 'delivery', '交付中心', 'template', '模板', 'delete', '服务模板删除', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('42', 'delivery', '交付中心', 'template', '模板', 'execute', '服务模板发布', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('43', 'delivery', '交付中心', 'onlineshop', '应用商店', 'get', '应用商店查询', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('44', 'appcenter', '应用中心', 'app', '应用（服务）', 'get', '查询', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('45', 'appcenter', '应用中心', 'app', '应用（服务）', 'create', '创建', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('46', 'appcenter', '应用中心', 'app', '应用（服务）', 'update', '修改', '修改副本数、容器配额以及服务启停、升级回滚操作', '修改副本数、容器配额以及服务启停、升级回滚操作', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('47', 'appcenter', '应用中心', 'app', '应用（服务）', 'delete', '删除', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('48', 'appcenter', '应用中心', 'app', '应用（服务）', 'execute', '服务控制台', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('49', 'appcenter', '应用中心', 'daemonset', '守护进程服务', 'get', 'daemonset服务查询', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('50', 'appcenter', '应用中心', 'daemonset', '守护进程服务', 'create', 'daemonset服务创建', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('51', 'appcenter', '应用中心', 'daemonset', '守护进程服务', 'delete', 'daemonset服务修改', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('52', 'appcenter', '应用中心', 'daemonset', '守护进程服务', 'update', 'daemonset服务删除', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('53', 'appcenter', '应用中心', 'autoscale', '弹性伸缩', 'get', '弹性伸缩查询', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('54', 'appcenter', '应用中心', 'autoscale', '弹性伸缩', 'create', '弹性伸缩创建', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('55', 'appcenter', '应用中心', 'autoscale', '弹性伸缩', 'update', '弹性伸缩修改', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('56', 'appcenter', '应用中心', 'autoscale', '弹性伸缩', 'delete', '弹性伸缩删除', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('57', 'appcenter', '应用中心', 'configmap', '配置文件', 'get', '配置文件查询', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('58', 'appcenter', '应用中心', 'configmap', '配置文件', 'create', '配置文件创建', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('59', 'appcenter', '应用中心', 'configmap', '配置文件', 'update', '配置文件修改', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('60', 'appcenter', '应用中心', 'configmap', '配置文件', 'delete', '配置文件删除', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('61', 'appcenter', '应用中心', 'volume', '存储', 'get', '存储查询', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('62', 'appcenter', '应用中心', 'volume', '存储', 'create', '存储创建', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('63', 'appcenter', '应用中心', 'volume', '存储', 'update', '存储修改', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('64', 'appcenter', '应用中心', 'volume', '存储', 'delete', '存储删除', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('65', 'appcenter', '应用中心', 'externalservice', '外部服务', 'get', '外部服务查询', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('66', 'appcenter', '应用中心', 'externalservice', '外部服务', 'create', '外部服务创建', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('67', 'appcenter', '应用中心', 'externalservice', '外部服务', 'update', '外部服务修改', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('68', 'appcenter', '应用中心', 'externalservice', '外部服务', 'delete', '外部服务删除', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('69', 'cicd', '持续集成交付', 'cicdmgr', 'CICD流水线', 'get', '持续发布流水线查询', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('70', 'cicd', '持续集成交付', 'cicdmgr', 'CICD流水线', 'create', '持续发布流水线创建', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('71', 'cicd', '持续集成交付', 'cicdmgr', 'CICD流水线', 'update', '持续发布流水线修改', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('72', 'cicd', '持续集成交付', 'cicdmgr', 'CICD流水线', 'delete', '持续发布流水线删除', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('73', 'cicd', '持续集成交付', 'cicdmgr', 'CICD流水线', 'execute', '持续发布流水线运行', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('74', 'cicd', '持续集成交付', 'env', '流水线配置管理', 'get', '环境查询', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('75', 'cicd', '持续集成交付', 'env', '流水线配置管理', 'create', '环境创建', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('76', 'cicd', '持续集成交付', 'env', '流水线配置管理', 'delete', '环境删除', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('77', 'cicd', '持续集成交付', 'env', '流水线配置管理', 'update', '环境修改', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('78', 'log', '日志中心', 'applog', '应用日志', 'get', '应用日志查询', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('79', 'log', '日志中心', 'auditlog', '审计日志', 'get', '操作审计查询', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('80', 'log', '日志中心', 'systemlog', '系统日志', 'get', '系统日志查询', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('81', 'log', '日志中心', 'snapshotrule', '日志备份', 'get', '日志备份查询', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('82', 'log', '日志中心', 'snapshotrule', '日志备份', 'create', '日志备份创建', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('83', 'log', '日志中心', 'snapshotrule', '日志备份', 'update', '日志备份修改', '规则修改以及启停操作', '规则修改以及启停操作', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('84', 'log', '日志中心', 'snapshotrule', '日志备份', 'delete', '日志备份删除', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('85', 'alarm', '告警中心', 'alarmrule', '告警规则', 'get', '告警规则查询', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('86', 'alarm', '告警中心', 'alarmrule', '告警规则', 'create', '告警规则创建', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('87', 'alarm', '告警中心', 'alarmrule', '告警规则', 'update', '告警规则修改', '修改告警规则以及规则启动和停止操作', '修改告警规则以及规则启动和停止操作', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('88', 'alarm', '告警中心', 'alarmrule', '告警规则', 'delete', '告警规则删除', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('89', 'alarm', '告警中心', 'alarmhandle', '告警处理', 'get', '告警记录查询', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('90', 'alarm', '告警中心', 'alarmhandle', '告警处理', 'update', '告警记录处理', '', '', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('91', 'system', '系统设置', 'config', '系统设置', 'get', '系统设置查询', '用户及用户群组查看', '用户及用户群组查看', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('92', 'system', '系统设置', 'config', '系统设置', 'create', '系统设置创建', '用户及用户群组创建', '用户及用户群组创建', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('93', 'system', '系统设置', 'config', '系统设置', 'update', '系统设置修改', '用户及用户群组修改', '用户及用户群组修改', '1', '2018-01-13 06:31:00', null);
INSERT INTO `privilege` VALUES ('94', 'system', '系统设置', 'config', '系统设置', 'delete', '系统设置删除', '用户及用户群组删除', '用户及用户群组删除', '1', '2018-01-13 06:31:00', null);

-- ----------------------------
-- Table structure for project
-- ----------------------------
DROP TABLE IF EXISTS `project`;
CREATE TABLE `project` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `tenant_id` varchar(64) NOT NULL COMMENT '租户id',
  `project_id` varchar(64) NOT NULL COMMENT '项目Id,对应CDP项目主键（projectId字段）',
  `project_system_code` varchar(64) DEFAULT NULL COMMENT '项目编码,对应CDP项目编码（projectCode字段， 基于CDP项目简称字段，36进制生成）',
  `alias_name` varchar(128) DEFAULT NULL COMMENT '别名',
  `project_name` varchar(64) NOT NULL COMMENT '项目名称,对应CDP项目名称（projectName字段）',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `annotation` varchar(512) DEFAULT NULL COMMENT '备注',
  `pm_usernames` varchar(255) DEFAULT NULL,
  `update_user_account` varchar(64) DEFAULT NULL COMMENT '修改项目的用户账号,对应CDP项目修改人（reviseUserName字段）',
  `update_user_id` varchar(64) DEFAULT NULL COMMENT '修改项目的用户Id,对应CDP项目修改人（reviseUserId字段）',
  `update_user_name` varchar(64) DEFAULT NULL COMMENT '修改项目的用户名称,对应CDP项目修改人（reviseUserName字段）',
  `create_user_account` varchar(64) DEFAULT NULL COMMENT '创建项目的用户账号,对应CDP项目修改人（reviseUserAccount字段）',
  `create_user_id` varchar(64) DEFAULT NULL COMMENT '创建项目的用户Id,对应CDP项目创建人ID（createUserId字段',
  `create_user_name` varchar(64) DEFAULT NULL COMMENT '创建项目的用户名称,对应CDP项目创建人名称（createUserName字段）',
  `reserve1` varchar(255) DEFAULT NULL COMMENT '预留字段',
  `reserve2` varchar(255) DEFAULT NULL COMMENT '预留字段',
  PRIMARY KEY (`id`),
  UNIQUE KEY `project_id` (`project_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of project
-- ----------------------------

-- ----------------------------
-- Table structure for project_privilege
-- ----------------------------
DROP TABLE IF EXISTS `project_privilege`;
CREATE TABLE `project_privilege` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `privilege` varchar(64) DEFAULT NULL COMMENT '权限',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `module` varchar(64) DEFAULT NULL COMMENT '模块',
  `available` tinyint(1) DEFAULT NULL COMMENT '是否可用 1可用 0不可用',
  `mark` varchar(64) DEFAULT NULL COMMENT '说明',
  `is_parent` tinyint(1) DEFAULT NULL COMMENT '是否为父节点',
  `parent_id` int(11) DEFAULT NULL COMMENT '权限id',
  `reserve1` varchar(255) DEFAULT NULL COMMENT '预留字段',
  `reserve2` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of project_privilege
-- ----------------------------

-- ----------------------------
-- Table structure for project_role_privilege
-- ----------------------------
DROP TABLE IF EXISTS `project_role_privilege`;
CREATE TABLE `project_role_privilege` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `privilege` varchar(64) DEFAULT NULL COMMENT '权限',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `module` varchar(64) DEFAULT NULL COMMENT '模块',
  `status` tinyint(1) DEFAULT NULL COMMENT '状态',
  `mark` varchar(64) DEFAULT NULL COMMENT '说明',
  `is_parent` tinyint(1) DEFAULT NULL COMMENT '是否为父节点',
  `project_role_id` int(11) DEFAULT NULL,
  `rp_id` int(11) DEFAULT NULL COMMENT '角色权限id',
  `parent_rpid` int(11) DEFAULT NULL COMMENT '父角色权限id',
  `reserve1` varchar(255) DEFAULT NULL COMMENT '预留字段',
  `reserve2` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of project_role_privilege
-- ----------------------------

-- ----------------------------
-- Table structure for resource
-- ----------------------------
DROP TABLE IF EXISTS `resource`;
CREATE TABLE `resource` (
  `id` int(10) NOT NULL AUTO_INCREMENT COMMENT '序号',
  `name` varchar(100) NOT NULL COMMENT '资源名称',
  `type` varchar(10) NOT NULL DEFAULT 'menu' COMMENT '类型',
  `url` varchar(100) NOT NULL COMMENT '资源路径',
  `parent_id` int(10) DEFAULT '0' COMMENT '父节点',
  `parent_ids` varchar(100) DEFAULT NULL COMMENT '父节点字符串',
  `weight` int(100) DEFAULT '0' COMMENT '权重',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `available` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否可用 1可用 0不可用',
  `trans_name` varchar(45) DEFAULT NULL COMMENT '译名',
  `icon_name` varchar(45) DEFAULT NULL COMMENT '图标名',
  `role` varchar(255) DEFAULT NULL,
  `isParent` int(1) DEFAULT NULL,
  `parent_rpid` int(100) DEFAULT NULL,
  `rpid` int(100) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=210 DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT;

-- ----------------------------
-- Records of resource
-- ----------------------------
INSERT INTO `resource` VALUES ('1', '总览', 'menu', 'overview', '0', null, '1', null, '2017-09-26 16:36:39', '1', null, 'menu-icon mi-overview', 'admin', '1', '0', '1');
INSERT INTO `resource` VALUES ('2', '集群', 'menu', 'cluster', '0', null, '2', null, '2017-09-26 16:36:39', '1', null, 'menu-icon mi-cluster', 'admin', '1', '0', '2');
INSERT INTO `resource` VALUES ('3', '租户管理', 'menu', 'tenant', '0', null, '3', null, '2017-09-26 16:36:39', '1', null, 'menu-icon mi-tenant', 'admin', '1', '0', '3');
INSERT INTO `resource` VALUES ('4', '应用中心', 'menu', '', '0', null, '4', null, '2017-09-26 16:36:39', '1', null, 'menu-icon mi-application', 'admin', '1', '0', '4');
INSERT INTO `resource` VALUES ('5', 'CICD', 'menu', '', '0', null, '6', null, '2017-09-26 16:36:39', '1', null, 'menu-icon mi-cicd', 'admin', '1', '0', '5');
INSERT INTO `resource` VALUES ('6', '交付中心', 'menu', '', '0', null, '5', null, '2017-09-26 16:36:39', '1', null, 'menu-icon mi-centers', 'admin', '1', '0', '6');
INSERT INTO `resource` VALUES ('7', '日志管理', 'menu', '', '0', null, '7', null, '2017-09-26 16:36:39', '1', null, 'menu-icon mi-audit', 'admin', '1', '0', '7');
INSERT INTO `resource` VALUES ('8', '告警中心', 'menu', '', '0', null, '8', null, '2017-09-26 16:36:39', '1', null, 'menu-icon mi-alarm', 'admin', '1', '0', '8');
INSERT INTO `resource` VALUES ('9', '应用', 'menu', 'manageList', '4', '4/9', '9', null, null, '1', null, '', 'admin', '0', '4', '9');
INSERT INTO `resource` VALUES ('10', '服务', 'menu', 'manageApplyList', '4', '4/10', '10', null, null, '1', null, '', 'admin', '0', '4', '10');
INSERT INTO `resource` VALUES ('11', '外部服务', 'menu', 'externalService', '4', '4/11', '11', null, null, '1', null, '', 'admin', '0', '4', '11');
INSERT INTO `resource` VALUES ('12', '配置中心', 'menu', 'configcenter', '4', '4/12', '12', null, null, '1', null, '', 'admin', '0', '4', '12');
INSERT INTO `resource` VALUES ('13', '存储', 'menu', 'storageScheme', '4', '4/13', '13', null, null, '1', null, '', 'admin', '0', '4', '13');
INSERT INTO `resource` VALUES ('14', '批量任务', 'menu', 'job', '4', '4/14', '14', null, null, '0', null, '', 'admin', '0', '4', '14');
INSERT INTO `resource` VALUES ('15', 'Docker file', 'menu', 'dockerfileList', '5', '5/15', '15', null, null, '1', null, null, 'admin', '0', '5', '15');
INSERT INTO `resource` VALUES ('16', '流水线', 'menu', 'pipelineList', '5', '5/16', '16', null, null, '1', null, null, 'admin', '0', '5', '16');
INSERT INTO `resource` VALUES ('17', '依赖管理', 'menu', 'dependenceList', '5', '5/17', '17', null, null, '1', null, '', 'admin', '0', '5', '17');
INSERT INTO `resource` VALUES ('18', '镜像仓库', 'menu', 'mirrorContent', '6', '6/18', '18', null, null, '1', null, '', 'admin', '0', '6', '18');
INSERT INTO `resource` VALUES ('19', '应用商店', 'menu', 'deliveryStore', '6', '6/19', '19', null, null, '1', null, '', 'admin', '0', '6', '19');
INSERT INTO `resource` VALUES ('20', '模板管理', 'menu', 'manageAll', '6', '6/20', '20', null, null, '1', null, '', 'admin', '0', '6', '20');
INSERT INTO `resource` VALUES ('21', '操作审计', 'menu', 'adminAudit', '7', '7/21', '21', null, null, '1', null, '', 'admin', '0', '7', '21');
INSERT INTO `resource` VALUES ('22', '日志查询', 'menu', 'logQuery', '7', '7/22', '22', null, null, '1', null, '', 'admin', '0', '7', '22');
INSERT INTO `resource` VALUES ('23', '告警规则', 'menu', 'alarmList', '8', '8/23', '23', null, null, '1', null, '', 'admin', '0', '8', '23');
INSERT INTO `resource` VALUES ('24', '告警处理中心', 'menu', 'alarmHandingList', '8', '8/24', '24', null, null, '1', null, '', 'admin', '0', '8', '24');
INSERT INTO `resource` VALUES ('25', '系统设置', 'menu', 'system', '0', '', '9', null, '2017-09-26 16:36:39', '1', null, 'menu-icon mi-config', 'admin', '1', '0', '25');
INSERT INTO `resource` VALUES ('33', '我的租户', 'menu', 'tenant', '0', null, '25', null, '2017-12-07 10:49:51', '1', null, 'menu-icon mi-tenant', 'dev', '1', '0', '1');
INSERT INTO `resource` VALUES ('34', '应用中心', 'menu', '', '0', null, '1', null, '2017-09-20 21:39:34', '1', null, 'menu-icon mi-application', 'dev', '1', '0', '2');
INSERT INTO `resource` VALUES ('35', 'CICD', 'menu', '', '0', null, '3', null, '2017-09-26 14:39:33', '1', null, 'menu-icon mi-cicd', 'dev', '1', '0', '3');
INSERT INTO `resource` VALUES ('36', '交付中心', 'menu', '', '0', null, '2', null, '2017-09-26 14:39:33', '1', null, 'menu-icon mi-centers', 'dev', '1', '0', '4');
INSERT INTO `resource` VALUES ('37', '日志管理', 'menu', '', '0', null, '4', null, '2017-09-13 16:53:06', '1', null, 'menu-icon mi-audit', 'dev', '1', '0', '5');
INSERT INTO `resource` VALUES ('38', '告警中心', 'menu', '', '0', null, '5', null, '2017-09-26 14:39:33', '1', null, 'menu-icon mi-alarm', 'dev', '1', '0', '6');
INSERT INTO `resource` VALUES ('39', '应用', 'menu', 'manageList', '2', '34/39', '31', null, '2017-09-20 21:39:34', '1', null, '', 'dev', '0', '2', '7');
INSERT INTO `resource` VALUES ('40', '服务', 'menu', 'manageApplyList', '2', '34/40', '32', null, '2017-09-22 19:13:10', '1', null, '', 'dev', '0', '2', '8');
INSERT INTO `resource` VALUES ('41', '外部服务', 'menu', 'externalService', '2', '34/41', '33', null, '2017-09-26 14:35:13', '1', null, '', 'dev', '0', '2', '9');
INSERT INTO `resource` VALUES ('42', '配置中心', 'menu', 'configcenter', '2', '34/42', '34', null, '2017-09-22 19:13:10', '1', null, '', 'dev', '0', '2', '10');
INSERT INTO `resource` VALUES ('43', '存储', 'menu', 'storageScheme', '2', '34/43', '35', null, '2017-09-20 22:01:16', '1', null, '', 'dev', '0', '2', '11');
INSERT INTO `resource` VALUES ('44', '批量任务', 'menu', 'job', '2', '34/44', '36', null, null, '0', null, '', 'dev', '0', '2', '12');
INSERT INTO `resource` VALUES ('45', 'Docker file', 'menu', 'dockerfileList', '3', '35/45', '37', null, '2017-09-26 14:39:33', '1', null, null, 'dev', '0', '3', '13');
INSERT INTO `resource` VALUES ('46', '流水线', 'menu', 'pipelineList', '3', '35/46', '38', null, '2017-09-26 14:39:33', '1', null, null, 'dev', '0', '3', '14');
INSERT INTO `resource` VALUES ('47', '依赖管理', 'menu', 'dependenceList', '3', '35/47', '39', null, '2017-09-26 14:39:33', '1', null, '', 'dev', '0', '3', '15');
INSERT INTO `resource` VALUES ('48', '镜像仓库', 'menu', 'mirrorContent', '4', '36/48', '40', null, '2017-09-26 18:49:42', '1', null, '', 'dev', '0', '4', '16');
INSERT INTO `resource` VALUES ('49', '应用商店', 'menu', 'deliveryStore', '4', '36/49', '41', null, '2017-09-26 16:41:30', '1', null, '', 'dev', '0', '4', '17');
INSERT INTO `resource` VALUES ('50', '模板管理', 'menu', 'manageAll', '4', '36/50', '42', null, '2017-09-26 14:39:33', '1', null, '', 'dev', '0', '4', '18');
INSERT INTO `resource` VALUES ('51', '操作审计', 'menu', 'adminAudit', '5', '37/51', '43', null, '2017-12-07 10:49:51', '1', null, '', 'dev', '0', '5', '19');
INSERT INTO `resource` VALUES ('52', '告警规则', 'menu', 'alarmList', '6', '38/52', '44', null, '2017-09-26 14:39:33', '1', null, '', 'dev', '0', '6', '20');
INSERT INTO `resource` VALUES ('53', '告警处理中心', 'menu', 'alarmHandingList', '6', '38/53', '45', null, '2017-09-26 14:39:33', '1', null, '', 'dev', '0', '6', '21');
INSERT INTO `resource` VALUES ('63', '我的租户', 'menu', 'tenant', '0', null, '25', null, '2017-09-26 18:49:38', '0', null, 'menu-icon mi-tenant', 'ops', '1', '0', '26');
INSERT INTO `resource` VALUES ('64', '应用中心', 'menu', '', '0', null, '26', null, '2017-09-26 11:21:38', '1', null, 'menu-icon mi-application', 'ops', '1', '0', '4');
INSERT INTO `resource` VALUES ('65', 'CICD', 'menu', '', '0', null, '27', null, '2017-09-26 18:49:39', '0', null, 'menu-icon mi-cicd', 'ops', '1', '0', '5');
INSERT INTO `resource` VALUES ('66', '交付中心', 'menu', '', '0', null, '28', null, '2017-09-26 11:21:43', '1', null, 'menu-icon mi-centers', 'ops', '1', '0', '6');
INSERT INTO `resource` VALUES ('67', '日志管理', 'menu', '', '0', null, '29', null, '2017-08-22 17:09:15', '1', null, 'menu-icon mi-audit', 'ops', '1', '0', '7');
INSERT INTO `resource` VALUES ('68', '告警中心', 'menu', '', '0', null, '30', null, '2017-09-26 18:49:39', '1', null, 'menu-icon mi-alarm', 'ops', '1', '0', '8');
INSERT INTO `resource` VALUES ('69', '应用', 'menu', 'manageList', '34', '34/39', '31', null, '2017-09-26 11:21:38', '1', null, '', 'ops', '0', '4', '9');
INSERT INTO `resource` VALUES ('70', '服务', 'menu', 'manageApplyList', '34', '34/40', '32', null, '2017-09-26 11:21:43', '1', null, '', 'ops', '0', '4', '10');
INSERT INTO `resource` VALUES ('71', '外部服务', 'menu', 'externalService', '34', '34/41', '33', null, '2017-09-26 11:21:43', '1', null, '', 'ops', '0', '4', '11');
INSERT INTO `resource` VALUES ('72', '配置中心', 'menu', 'configcenter', '34', '34/42', '34', null, '2017-09-26 11:21:43', '1', null, '', 'ops', '0', '4', '12');
INSERT INTO `resource` VALUES ('73', '存储', 'menu', 'storageScheme', '34', '34/43', '35', null, '2017-09-26 11:21:43', '1', null, '', 'ops', '0', '4', '13');
INSERT INTO `resource` VALUES ('74', '批量任务', 'menu', 'job', '34', '34/44', '36', null, null, '0', null, '', 'ops', '0', '4', '14');
INSERT INTO `resource` VALUES ('75', 'Docker file', 'menu', 'dockerfileList', '35', '35/45', '37', null, '2017-09-26 18:49:39', '0', null, null, 'ops', '0', '5', '15');
INSERT INTO `resource` VALUES ('76', '流水线', 'menu', 'pipelineList', '35', '35/46', '38', null, '2017-09-26 18:49:39', '0', null, null, 'ops', '0', '5', '16');
INSERT INTO `resource` VALUES ('77', '依赖管理', 'menu', 'dependenceList', '35', '35/47', '39', null, '2017-09-26 18:49:39', '0', null, '', 'ops', '0', '5', '17');
INSERT INTO `resource` VALUES ('78', '镜像仓库', 'menu', 'mirrorContent', '36', '36/48', '40', null, '2017-09-26 18:49:43', '1', null, '', 'ops', '0', '6', '18');
INSERT INTO `resource` VALUES ('79', '应用商店', 'menu', 'deliveryStore', '36', '36/49', '41', null, '2017-09-26 11:21:43', '1', null, '', 'ops', '0', '6', '19');
INSERT INTO `resource` VALUES ('80', '模板管理', 'menu', 'manageAll', '36', '36/50', '42', null, '2017-09-26 11:21:43', '1', null, '', 'ops', '0', '6', '20');
INSERT INTO `resource` VALUES ('81', '操作审计', 'menu', 'adminAudit', '37', '37/51', '43', null, '2017-08-22 17:09:15', '0', null, '', 'ops', '0', '7', '21');
INSERT INTO `resource` VALUES ('82', '告警规则', 'menu', 'alarmList', '38', '38/52', '44', null, '2017-09-26 18:49:39', '1', null, '', 'ops', '0', '8', '23');
INSERT INTO `resource` VALUES ('83', '告警处理中心', 'menu', 'alarmHandingList', '38', '38/53', '45', null, '2017-09-26 18:49:39', '1', null, '', 'ops', '0', '8', '24');
INSERT INTO `resource` VALUES ('87', '应用中心', 'menu', '', '0', null, '4', null, null, '0', null, 'menu-icon mi-application', 'default', '1', '0', '4');
INSERT INTO `resource` VALUES ('88', 'CICD', 'menu', '', '0', null, '5', null, null, '0', null, 'menu-icon mi-cicd', 'default', '1', '0', '5');
INSERT INTO `resource` VALUES ('89', '交付中心', 'menu', '', '0', null, '6', null, null, '0', null, 'menu-icon mi-centers', 'default', '1', '0', '6');
INSERT INTO `resource` VALUES ('90', '日志管理', 'menu', '', '0', null, '7', null, null, '0', null, 'menu-icon mi-audit', 'default', '1', '0', '7');
INSERT INTO `resource` VALUES ('91', '告警中心', 'menu', '', '0', null, '8', null, null, '0', null, 'menu-icon mi-alarm', 'default', '1', '0', '8');
INSERT INTO `resource` VALUES ('92', '应用', 'menu', 'manageList', '0', '4/9', '9', null, null, '0', null, null, 'default', '0', '4', '9');
INSERT INTO `resource` VALUES ('93', '服务', 'menu', 'manageApplyList', '0', '4/10', '10', null, null, '0', null, null, 'default', '0', '4', '10');
INSERT INTO `resource` VALUES ('94', '外部服务', 'menu', 'externalService', '0', '4/11', '11', null, null, '0', null, null, 'default', '0', '4', '11');
INSERT INTO `resource` VALUES ('96', '配置中心', 'menu', 'configcenter', '0', '4/12', '12', null, null, '0', null, null, 'default', '0', '4', '12');
INSERT INTO `resource` VALUES ('97', '存储', 'menu', 'storageScheme', '0', '4/13', '13', null, null, '0', null, null, 'default', '0', '4', '13');
INSERT INTO `resource` VALUES ('98', '批量任务', 'menu', 'job', '0', '4/14', '14', null, null, '0', null, null, 'default', '0', '4', '14');
INSERT INTO `resource` VALUES ('99', 'Docker file', 'menu', 'dockerfileList', '0', '5/15', '15', null, null, '0', null, null, 'default', '0', '5', '15');
INSERT INTO `resource` VALUES ('100', '流水线', 'menu', 'pipelineList', '0', '5/16', '16', null, null, '0', null, null, 'default', '0', '5', '16');
INSERT INTO `resource` VALUES ('101', '依赖管理', 'menu', 'dependenceList', '0', '5/17', '17', null, null, '0', null, null, 'default', '0', '5', '17');
INSERT INTO `resource` VALUES ('102', '镜像管理', 'menu', 'mirrorContent', '0', '6/18', '18', null, null, '0', null, null, 'default', '0', '6', '18');
INSERT INTO `resource` VALUES ('103', '应用商店', 'menu', 'deliveryStore', '0', '6/19', '19', null, null, '0', null, null, 'default', '0', '6', '19');
INSERT INTO `resource` VALUES ('104', '模板管理', 'menu', 'manageAll', '0', '6/20', '20', null, null, '0', null, null, 'default', '0', '6', '20');
INSERT INTO `resource` VALUES ('105', '操作审计', 'menu', 'adminAudit', '0', '7/21', '21', null, null, '0', null, null, 'default', '0', '7', '21');
INSERT INTO `resource` VALUES ('106', '日志查询', 'menu', 'logQuery', '0', '7/22', '22', null, null, '0', null, null, 'default', '0', '7', '22');
INSERT INTO `resource` VALUES ('107', '告警规则', 'menu', 'alarmList', '0', '8/23', '23', null, null, '0', null, null, 'default', '0', '8', '23');
INSERT INTO `resource` VALUES ('108', '告警处理中心', 'menu', 'alarmHandingList', '0', '8/24', '24', null, null, '0', null, null, 'default', '0', '8', '24');
INSERT INTO `resource` VALUES ('110', '我的租户', 'menu', 'tenant', '0', null, '3', null, null, '0', null, 'menu-icon mi-tenant', 'default', '1', '0', '26');
INSERT INTO `resource` VALUES ('111', '我的租户', 'menu', 'tenant', '0', null, '25', null, '2017-09-25 18:28:39', '1', null, 'menu-icon mi-tenant', 'tm', '1', '0', '1');
INSERT INTO `resource` VALUES ('112', '应用中心', 'menu', '', '0', null, '26', null, '2017-09-25 17:50:40', '1', null, 'menu-icon mi-application', 'tm', '1', '0', '2');
INSERT INTO `resource` VALUES ('113', 'CICD', 'menu', '', '0', null, '27', null, '2017-09-25 18:28:39', '1', null, 'menu-icon mi-cicd', 'tm', '1', '0', '3');
INSERT INTO `resource` VALUES ('114', '交付中心', 'menu', '', '0', null, '28', null, '2017-09-20 21:37:48', '1', null, 'menu-icon mi-centers', 'tm', '1', '0', '4');
INSERT INTO `resource` VALUES ('115', '日志管理', 'menu', '', '0', null, '29', null, '2017-09-13 16:02:57', '1', null, 'menu-icon mi-audit', 'tm', '1', '0', '5');
INSERT INTO `resource` VALUES ('116', '告警中心', 'menu', '', '0', null, '30', null, '2017-09-22 19:11:52', '1', null, 'menu-icon mi-alarm', 'tm', '1', '0', '6');
INSERT INTO `resource` VALUES ('117', '应用', 'menu', 'manageList', '2', '34/39', '31', null, '2017-09-25 17:50:40', '1', null, '', 'tm', '0', '2', '7');
INSERT INTO `resource` VALUES ('118', '服务', 'menu', 'manageApplyList', '2', '34/40', '32', null, '2017-09-25 17:50:54', '1', null, '', 'tm', '0', '2', '8');
INSERT INTO `resource` VALUES ('119', '外部服务', 'menu', 'externalService', '2', '34/41', '33', null, '2017-09-25 17:50:40', '1', null, '', 'tm', '0', '2', '9');
INSERT INTO `resource` VALUES ('120', '配置中心', 'menu', 'configcenter', '2', '34/42', '34', null, '2017-09-25 17:50:54', '1', null, '', 'tm', '0', '2', '10');
INSERT INTO `resource` VALUES ('121', '存储', 'menu', 'storageScheme', '2', '34/43', '35', null, '2017-09-25 17:50:54', '1', null, '', 'tm', '0', '2', '11');
INSERT INTO `resource` VALUES ('122', '批量任务', 'menu', 'job', '2', '34/44', '36', null, null, '0', null, '', 'tm', '0', '2', '12');
INSERT INTO `resource` VALUES ('123', 'Docker file', 'menu', 'dockerfileList', '3', '35/45', '37', null, '2017-09-25 18:28:39', '1', null, null, 'tm', '0', '3', '13');
INSERT INTO `resource` VALUES ('124', '流水线', 'menu', 'pipelineList', '3', '35/46', '38', null, '2017-09-25 18:28:39', '1', null, null, 'tm', '0', '3', '14');
INSERT INTO `resource` VALUES ('125', '依赖管理', 'menu', 'dependenceList', '3', '35/47', '39', null, '2017-09-25 18:28:39', '1', null, '', 'tm', '0', '3', '15');
INSERT INTO `resource` VALUES ('126', '镜像仓库', 'menu', 'mirrorContent', '4', '36/48', '40', null, '2017-09-25 18:09:18', '1', null, '', 'tm', '0', '4', '16');
INSERT INTO `resource` VALUES ('127', '应用商店', 'menu', 'deliveryStore', '4', '36/49', '41', null, '2017-09-25 17:50:54', '1', null, '', 'tm', '0', '4', '17');
INSERT INTO `resource` VALUES ('128', '模板管理', 'menu', 'manageAll', '4', '36/50', '42', null, '2017-09-20 21:37:48', '1', null, '', 'tm', '0', '4', '18');
INSERT INTO `resource` VALUES ('129', '操作审计', 'menu', 'adminAudit', '5', '37/51', '43', null, '2017-08-22 17:09:15', '1', null, '', 'tm', '0', '5', '19');
INSERT INTO `resource` VALUES ('130', '告警规则', 'menu', 'alarmList', '6', '38/52', '44', null, '2017-09-22 19:11:53', '1', null, '', 'tm', '0', '6', '20');
INSERT INTO `resource` VALUES ('131', '告警处理中心', 'menu', 'alarmHandingList', '6', '38/53', '45', null, '2017-09-22 19:11:53', '1', null, '', 'tm', '0', '6', '21');
INSERT INTO `resource` VALUES ('133', '告警规则', 'menu', 'alarmList', '0', '8/23', '23', null, null, '0', null, null, 'tm', '0', '8', '23');
INSERT INTO `resource` VALUES ('134', '告警处理中心', 'menu', 'alarmHandingList', '0', '8/24', '24', null, null, '0', null, null, 'tm', '0', '8', '24');
INSERT INTO `resource` VALUES ('140', '应用中心', 'menu', '', '0', null, '4', null, '2017-09-26 14:39:34', '1', null, 'menu-icon mi-application', 'tester', '1', '0', '4');
INSERT INTO `resource` VALUES ('141', 'CICD', 'menu', '', '0', null, '5', null, '2017-09-25 18:40:49', '0', null, 'menu-icon mi-cicd', 'tester', '1', '0', '5');
INSERT INTO `resource` VALUES ('142', '交付中心', 'menu', '', '0', null, '6', null, '2017-09-20 21:40:07', '1', null, 'menu-icon mi-centers', 'tester', '1', '0', '6');
INSERT INTO `resource` VALUES ('143', '日志管理', 'menu', '', '0', null, '7', null, '2017-09-12 19:49:01', '1', null, 'menu-icon mi-audit', 'tester', '1', '0', '7');
INSERT INTO `resource` VALUES ('144', '告警中心', 'menu', '', '0', null, '8', null, '2017-09-20 21:01:13', '1', null, 'menu-icon mi-alarm', 'tester', '1', '0', '8');
INSERT INTO `resource` VALUES ('145', '应用', 'menu', 'manageList', '0', '4/9', '9', null, '2017-09-26 14:39:34', '1', null, null, 'tester', '0', '4', '9');
INSERT INTO `resource` VALUES ('146', '服务', 'menu', 'manageApplyList', '0', '4/10', '10', null, '2017-09-26 14:39:34', '1', null, null, 'tester', '0', '4', '10');
INSERT INTO `resource` VALUES ('147', '外部服务', 'menu', 'externalService', '0', '4/11', '11', null, '2017-09-26 14:39:34', '1', null, null, 'tester', '0', '4', '11');
INSERT INTO `resource` VALUES ('148', '配置中心', 'menu', 'configcenter', '0', '4/12', '12', null, '2017-09-26 14:39:34', '1', null, null, 'tester', '0', '4', '12');
INSERT INTO `resource` VALUES ('149', '存储', 'menu', 'storageScheme', '0', '4/13', '13', null, '2017-09-26 14:39:34', '1', null, null, 'tester', '0', '4', '13');
INSERT INTO `resource` VALUES ('150', '批量任务', 'menu', 'job', '0', '4/14', '14', null, null, '0', null, null, 'tester', '0', '4', '14');
INSERT INTO `resource` VALUES ('151', 'Docker file', 'menu', 'dockerfileList', '0', '5/15', '15', null, '2017-09-25 18:40:49', '0', null, null, 'tester', '0', '5', '15');
INSERT INTO `resource` VALUES ('152', '流水线', 'menu', 'pipelineList', '0', '5/16', '16', null, '2017-09-25 18:40:49', '0', null, null, 'tester', '0', '5', '16');
INSERT INTO `resource` VALUES ('153', '依赖管理', 'menu', 'dependenceList', '0', '5/17', '17', null, '2017-09-25 18:40:49', '0', null, null, 'tester', '0', '5', '17');
INSERT INTO `resource` VALUES ('154', '镜像管理', 'menu', 'mirrorContent', '0', '6/18', '18', null, null, '1', null, null, 'tester', '0', '6', '18');
INSERT INTO `resource` VALUES ('155', '应用商店', 'menu', 'deliveryStore', '0', '6/19', '19', null, '2017-09-20 21:40:07', '1', null, null, 'tester', '0', '6', '19');
INSERT INTO `resource` VALUES ('156', '模板管理', 'menu', 'manageAll', '0', '6/20', '20', null, '2017-09-20 21:40:07', '1', null, null, 'tester', '0', '6', '20');
INSERT INTO `resource` VALUES ('157', '操作审计', 'menu', 'adminAudit', '0', '7/21', '21', null, null, '1', null, null, 'tester', '0', '7', '21');
INSERT INTO `resource` VALUES ('158', '日志查询', 'menu', 'logQuery', '0', '7/22', '22', null, null, '1', null, null, 'tester', '0', '7', '22');
INSERT INTO `resource` VALUES ('159', '告警规则', 'menu', 'alarmList', '0', '8/23', '23', null, '2017-09-20 21:01:13', '1', null, null, 'tester', '0', '8', '23');
INSERT INTO `resource` VALUES ('160', '告警处理中心', 'menu', 'alarmHandingList', '0', '8/24', '24', null, '2017-09-20 21:01:13', '1', null, null, 'tester', '0', '8', '24');
INSERT INTO `resource` VALUES ('162', '我的租户', 'menu', 'tenant', '0', null, '3', null, '2017-09-26 09:57:35', '0', null, 'menu-icon mi-tenant', 'tester', '1', '0', '26');
INSERT INTO `resource` VALUES ('163', '日志查询', 'menu', 'logQuery', '7', '7/22', '22', null, null, '1', null, '', 'dev', '0', '5', '22');
INSERT INTO `resource` VALUES ('164', '日志查询', 'menu', 'logQuery', '7', '7/22', '22', null, null, '1', null, '', 'ops', '0', '7', '25');
INSERT INTO `resource` VALUES ('165', '日志查询', 'menu', 'logQuery', '7', '7/22', '22', null, null, '1', null, '', 'tm', '0', '5', '25');
INSERT INTO `resource` VALUES ('166', '应用中心', 'menu', '', '0', null, '4', '2017-12-13 09:09:58', '2017-12-13 09:09:58', '1', null, 'menu-icon mi-application', 'pm', '1', '0', '4');
INSERT INTO `resource` VALUES ('167', 'CICD', 'menu', '', '0', null, '5', '2017-12-13 09:09:58', '2017-12-13 09:09:58', '1', null, 'menu-icon mi-cicd', 'pm', '1', '0', '5');
INSERT INTO `resource` VALUES ('168', '交付中心', 'menu', '', '0', null, '6', '2017-12-13 09:09:58', '2017-12-13 09:09:58', '1', null, 'menu-icon mi-centers', 'pm', '1', '0', '6');
INSERT INTO `resource` VALUES ('169', '日志管理', 'menu', '', '0', null, '7', '2017-12-13 09:09:58', '2017-12-13 09:09:58', '1', null, 'menu-icon mi-audit', 'pm', '1', '0', '7');
INSERT INTO `resource` VALUES ('170', '告警中心', 'menu', '', '0', null, '8', '2017-12-13 09:09:58', '2017-12-13 09:09:58', '0', null, 'menu-icon mi-alarm', 'pm', '1', '0', '8');
INSERT INTO `resource` VALUES ('171', '应用', 'menu', 'manageList', '0', '4/9', '9', '2017-12-13 09:09:58', '2017-12-13 09:09:58', '1', null, null, 'pm', '0', '4', '9');
INSERT INTO `resource` VALUES ('172', '服务', 'menu', 'manageApplyList', '0', '4/10', '10', '2017-12-13 09:09:58', '2017-12-13 09:09:58', '1', null, null, 'pm', '0', '4', '10');
INSERT INTO `resource` VALUES ('173', '外部服务', 'menu', 'externalService', '0', '4/11', '11', '2017-12-13 09:09:58', '2017-12-13 09:09:58', '1', null, null, 'pm', '0', '4', '11');
INSERT INTO `resource` VALUES ('174', '配置中心', 'menu', 'configcenter', '0', '4/12', '12', '2017-12-13 09:09:58', '2017-12-13 09:09:58', '1', null, null, 'pm', '0', '4', '12');
INSERT INTO `resource` VALUES ('175', '存储', 'menu', 'storageScheme', '0', '4/13', '13', '2017-12-13 09:09:58', '2017-12-13 09:09:58', '1', null, null, 'pm', '0', '4', '13');
INSERT INTO `resource` VALUES ('176', '批量任务', 'menu', 'job', '0', '4/14', '14', '2017-12-13 09:09:58', '2017-12-13 09:09:58', '0', null, null, 'pm', '0', '4', '14');
INSERT INTO `resource` VALUES ('177', 'Docker file', 'menu', 'dockerfileList', '0', '5/15', '15', '2017-12-13 09:09:58', '2017-12-13 09:09:58', '1', null, null, 'pm', '0', '5', '15');
INSERT INTO `resource` VALUES ('178', '流水线', 'menu', 'pipelineList', '0', '5/16', '16', '2017-12-13 09:09:58', '2017-12-13 09:09:58', '1', null, null, 'pm', '0', '5', '16');
INSERT INTO `resource` VALUES ('179', '依赖管理', 'menu', 'dependenceList', '0', '5/17', '17', '2017-12-13 09:09:58', '2017-12-13 09:09:58', '1', null, null, 'pm', '0', '5', '17');
INSERT INTO `resource` VALUES ('180', '镜像管理', 'menu', 'mirrorContent', '0', '6/18', '18', '2017-12-13 09:09:58', '2017-12-13 09:09:58', '0', null, null, 'pm', '0', '6', '18');
INSERT INTO `resource` VALUES ('181', '应用商店', 'menu', 'deliveryStore', '0', '6/19', '19', '2017-12-13 09:09:58', '2017-12-13 09:09:58', '1', null, null, 'pm', '0', '6', '19');
INSERT INTO `resource` VALUES ('182', '模板管理', 'menu', 'manageAll', '0', '6/20', '20', '2017-12-13 09:09:58', '2017-12-13 09:09:58', '1', null, null, 'pm', '0', '6', '20');
INSERT INTO `resource` VALUES ('183', '操作审计', 'menu', 'adminAudit', '0', '7/21', '21', '2017-12-13 09:09:58', '2017-12-13 09:09:58', '1', null, null, 'pm', '0', '7', '21');
INSERT INTO `resource` VALUES ('184', '日志查询', 'menu', 'logQuery', '0', '7/22', '22', '2017-12-13 09:09:58', '2017-12-13 09:09:58', '1', null, null, 'pm', '0', '7', '22');
INSERT INTO `resource` VALUES ('185', '告警规则', 'menu', 'alarmList', '0', '8/23', '23', '2017-12-13 09:09:58', '2017-12-13 09:09:58', '0', null, null, 'pm', '0', '8', '23');
INSERT INTO `resource` VALUES ('186', '告警处理中心', 'menu', 'alarmHandingList', '0', '8/24', '24', '2017-12-13 09:09:58', '2017-12-13 09:09:58', '0', null, null, 'pm', '0', '8', '24');
INSERT INTO `resource` VALUES ('187', '我的租户', 'menu', 'tenant', '0', null, '3', '2017-12-13 09:09:58', '2017-12-13 09:09:58', '0', null, 'menu-icon mi-tenant', 'pm', '1', '0', '26');
INSERT INTO `resource` VALUES ('188', '应用中心', 'menu', '', '0', null, '4', '2017-12-26 20:47:49', '2017-12-26 20:47:49', '0', null, 'menu-icon mi-application', 'test', '1', '0', '4');
INSERT INTO `resource` VALUES ('189', 'CICD', 'menu', '', '0', null, '5', '2017-12-26 20:47:49', '2017-12-26 20:47:49', '0', null, 'menu-icon mi-cicd', 'test', '1', '0', '5');
INSERT INTO `resource` VALUES ('190', '交付中心', 'menu', '', '0', null, '6', '2017-12-26 20:47:49', '2017-12-26 20:47:49', '0', null, 'menu-icon mi-centers', 'test', '1', '0', '6');
INSERT INTO `resource` VALUES ('191', '日志管理', 'menu', '', '0', null, '7', '2017-12-26 20:47:49', '2017-12-26 20:47:49', '0', null, 'menu-icon mi-audit', 'test', '1', '0', '7');
INSERT INTO `resource` VALUES ('192', '告警中心', 'menu', '', '0', null, '8', '2017-12-26 20:47:49', '2017-12-26 20:47:58', '1', null, 'menu-icon mi-alarm', 'test', '1', '0', '8');
INSERT INTO `resource` VALUES ('193', '应用', 'menu', 'manageList', '0', '4/9', '9', '2017-12-26 20:47:49', '2017-12-26 20:47:49', '0', null, null, 'test', '0', '4', '9');
INSERT INTO `resource` VALUES ('194', '服务', 'menu', 'manageApplyList', '0', '4/10', '10', '2017-12-26 20:47:49', '2017-12-26 20:47:49', '0', null, null, 'test', '0', '4', '10');
INSERT INTO `resource` VALUES ('195', '外部服务', 'menu', 'externalService', '0', '4/11', '11', '2017-12-26 20:47:49', '2017-12-26 20:47:49', '0', null, null, 'test', '0', '4', '11');
INSERT INTO `resource` VALUES ('196', '配置中心', 'menu', 'configcenter', '0', '4/12', '12', '2017-12-26 20:47:49', '2017-12-26 20:47:49', '0', null, null, 'test', '0', '4', '12');
INSERT INTO `resource` VALUES ('197', '存储', 'menu', 'storageScheme', '0', '4/13', '13', '2017-12-26 20:47:49', '2017-12-26 20:47:49', '0', null, null, 'test', '0', '4', '13');
INSERT INTO `resource` VALUES ('198', '批量任务', 'menu', 'job', '0', '4/14', '14', '2017-12-26 20:47:49', '2017-12-26 20:47:49', '0', null, null, 'test', '0', '4', '14');
INSERT INTO `resource` VALUES ('199', 'Docker file', 'menu', 'dockerfileList', '0', '5/15', '15', '2017-12-26 20:47:49', '2017-12-26 20:47:49', '0', null, null, 'test', '0', '5', '15');
INSERT INTO `resource` VALUES ('200', '流水线', 'menu', 'pipelineList', '0', '5/16', '16', '2017-12-26 20:47:49', '2017-12-26 20:47:49', '0', null, null, 'test', '0', '5', '16');
INSERT INTO `resource` VALUES ('201', '依赖管理', 'menu', 'dependenceList', '0', '5/17', '17', '2017-12-26 20:47:49', '2017-12-26 20:47:49', '0', null, null, 'test', '0', '5', '17');
INSERT INTO `resource` VALUES ('202', '镜像管理', 'menu', 'mirrorContent', '0', '6/18', '18', '2017-12-26 20:47:49', '2017-12-26 20:47:49', '0', null, null, 'test', '0', '6', '18');
INSERT INTO `resource` VALUES ('203', '应用商店', 'menu', 'deliveryStore', '0', '6/19', '19', '2017-12-26 20:47:49', '2017-12-26 20:47:49', '0', null, null, 'test', '0', '6', '19');
INSERT INTO `resource` VALUES ('204', '模板管理', 'menu', 'manageAll', '0', '6/20', '20', '2017-12-26 20:47:49', '2017-12-26 20:47:49', '0', null, null, 'test', '0', '6', '20');
INSERT INTO `resource` VALUES ('205', '操作审计', 'menu', 'adminAudit', '0', '7/21', '21', '2017-12-26 20:47:49', '2017-12-26 20:47:49', '0', null, null, 'test', '0', '7', '21');
INSERT INTO `resource` VALUES ('206', '日志查询', 'menu', 'logQuery', '0', '7/22', '22', '2017-12-26 20:47:49', '2017-12-26 20:47:58', '1', null, null, 'test', '0', '7', '22');
INSERT INTO `resource` VALUES ('207', '告警规则', 'menu', 'alarmList', '0', '8/23', '23', '2017-12-26 20:47:49', '2017-12-26 20:47:58', '1', null, null, 'test', '0', '8', '23');
INSERT INTO `resource` VALUES ('208', '告警处理中心', 'menu', 'alarmHandingList', '0', '8/24', '24', '2017-12-26 20:47:49', '2017-12-26 20:47:58', '1', null, null, 'test', '0', '8', '24');
INSERT INTO `resource` VALUES ('209', '我的租户', 'menu', 'tenant', '0', null, '3', '2017-12-26 20:47:49', '2017-12-26 20:47:49', '0', null, 'menu-icon mi-tenant', 'test', '1', '0', '26');

-- ----------------------------
-- Table structure for resource_custom
-- ----------------------------
DROP TABLE IF EXISTS `resource_custom`;
CREATE TABLE `resource_custom` (
  `id` int(10) NOT NULL AUTO_INCREMENT COMMENT '序号',
  `name` varchar(100) NOT NULL COMMENT '资源名称',
  `type` varchar(10) NOT NULL DEFAULT 'menu' COMMENT '类型',
  `url` varchar(100) NOT NULL COMMENT '资源路径',
  `parent_id` int(10) DEFAULT '0' COMMENT '父节点',
  `parent_ids` varchar(100) DEFAULT NULL COMMENT '父节点字符串',
  `weight` int(100) DEFAULT '0' COMMENT '权重',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `available` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否可用 1可用 0不可用',
  `trans_name` varchar(45) DEFAULT NULL COMMENT '译名',
  `icon_name` varchar(45) DEFAULT NULL COMMENT '图标名',
  `role` varchar(255) DEFAULT NULL,
  `isParent` int(1) DEFAULT NULL,
  `parent_rpid` int(100) DEFAULT NULL,
  `rpid` int(100) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=166 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of resource_custom
-- ----------------------------
INSERT INTO `resource_custom` VALUES ('1', '总览', 'menu', 'overview', '0', null, '1', null, '2017-09-26 16:36:39', '1', null, 'menu-icon mi-overview', 'admin', '1', '0', '1');
INSERT INTO `resource_custom` VALUES ('2', '集群', 'menu', 'cluster', '0', null, '2', null, '2017-09-26 16:36:39', '1', null, 'menu-icon mi-cluster', 'admin', '1', '0', '2');
INSERT INTO `resource_custom` VALUES ('3', '租户管理', 'menu', 'tenant', '0', null, '3', null, '2017-09-26 16:36:39', '1', null, 'menu-icon mi-tenant', 'admin', '1', '0', '3');
INSERT INTO `resource_custom` VALUES ('4', '应用中心', 'menu', '', '0', null, '4', null, '2017-09-26 16:36:39', '1', null, 'menu-icon mi-application', 'admin', '1', '0', '4');
INSERT INTO `resource_custom` VALUES ('5', 'CICD', 'menu', '', '0', null, '6', null, '2017-09-26 16:36:39', '1', null, 'menu-icon mi-cicd', 'admin', '1', '0', '5');
INSERT INTO `resource_custom` VALUES ('6', '交付中心', 'menu', '', '0', null, '5', null, '2017-09-26 16:36:39', '1', null, 'menu-icon mi-centers', 'admin', '1', '0', '6');
INSERT INTO `resource_custom` VALUES ('7', '日志管理', 'menu', '', '0', null, '7', null, '2017-09-26 16:36:39', '1', null, 'menu-icon mi-audit', 'admin', '1', '0', '7');
INSERT INTO `resource_custom` VALUES ('8', '告警中心', 'menu', '', '0', null, '8', null, '2017-09-26 16:36:39', '1', null, 'menu-icon mi-alarm', 'admin', '1', '0', '8');
INSERT INTO `resource_custom` VALUES ('9', '应用', 'menu', 'manageList', '4', '4/9', '9', null, null, '1', null, '', 'admin', '0', '4', '9');
INSERT INTO `resource_custom` VALUES ('10', '服务', 'menu', 'manageApplyList', '4', '4/10', '10', null, null, '1', null, '', 'admin', '0', '4', '10');
INSERT INTO `resource_custom` VALUES ('11', '外部服务', 'menu', 'externalService', '4', '4/11', '11', null, null, '1', null, '', 'admin', '0', '4', '11');
INSERT INTO `resource_custom` VALUES ('12', '配置中心', 'menu', 'configcenter', '4', '4/12', '12', null, null, '1', null, '', 'admin', '0', '4', '12');
INSERT INTO `resource_custom` VALUES ('13', '存储', 'menu', 'storageScheme', '4', '4/13', '13', null, null, '1', null, '', 'admin', '0', '4', '13');
INSERT INTO `resource_custom` VALUES ('14', '批量任务', 'menu', 'job', '4', '4/14', '14', null, null, '0', null, '', 'admin', '0', '4', '14');
INSERT INTO `resource_custom` VALUES ('15', 'Docker file', 'menu', 'dockerfileList', '5', '5/15', '15', null, null, '1', null, null, 'admin', '0', '5', '15');
INSERT INTO `resource_custom` VALUES ('16', '流水线', 'menu', 'pipelineList', '5', '5/16', '16', null, null, '1', null, null, 'admin', '0', '5', '16');
INSERT INTO `resource_custom` VALUES ('17', '依赖管理', 'menu', 'dependenceList', '5', '5/17', '17', null, null, '1', null, '', 'admin', '0', '5', '17');
INSERT INTO `resource_custom` VALUES ('18', '镜像仓库', 'menu', 'mirrorContent', '6', '6/18', '18', null, null, '1', null, '', 'admin', '0', '6', '18');
INSERT INTO `resource_custom` VALUES ('19', '应用商店', 'menu', 'deliveryStore', '6', '6/19', '19', null, null, '1', null, '', 'admin', '0', '6', '19');
INSERT INTO `resource_custom` VALUES ('20', '模板管理', 'menu', 'manageAll', '6', '6/20', '20', null, null, '1', null, '', 'admin', '0', '6', '20');
INSERT INTO `resource_custom` VALUES ('21', '操作审计', 'menu', 'adminAudit', '7', '7/21', '21', null, null, '1', null, '', 'admin', '0', '7', '21');
INSERT INTO `resource_custom` VALUES ('22', '日志查询', 'menu', 'logQuery', '7', '7/22', '22', null, null, '1', null, '', 'admin', '0', '7', '22');
INSERT INTO `resource_custom` VALUES ('23', '告警规则', 'menu', 'alarmList', '8', '8/23', '23', null, null, '1', null, '', 'admin', '0', '8', '23');
INSERT INTO `resource_custom` VALUES ('24', '告警处理中心', 'menu', 'alarmHandingList', '8', '8/24', '24', null, null, '1', null, '', 'admin', '0', '8', '24');
INSERT INTO `resource_custom` VALUES ('25', '系统设置', 'menu', 'system', '0', '', '9', null, '2017-09-26 16:36:39', '1', null, 'menu-icon mi-config', 'admin', '1', '0', '25');
INSERT INTO `resource_custom` VALUES ('33', '我的租户', 'menu', 'tenant', '0', null, '25', null, '2017-09-26 18:49:37', '0', null, 'menu-icon mi-tenant', 'dev', '1', '0', '1');
INSERT INTO `resource_custom` VALUES ('34', '应用中心', 'menu', '', '0', null, '1', null, '2017-09-20 21:39:34', '1', null, 'menu-icon mi-application', 'dev', '1', '0', '2');
INSERT INTO `resource_custom` VALUES ('35', 'CICD', 'menu', '', '0', null, '3', null, '2017-09-26 14:39:33', '1', null, 'menu-icon mi-cicd', 'dev', '1', '0', '3');
INSERT INTO `resource_custom` VALUES ('36', '交付中心', 'menu', '', '0', null, '2', null, '2017-09-26 14:39:33', '1', null, 'menu-icon mi-centers', 'dev', '1', '0', '4');
INSERT INTO `resource_custom` VALUES ('37', '日志管理', 'menu', '', '0', null, '4', null, '2017-09-13 16:53:06', '1', null, 'menu-icon mi-audit', 'dev', '1', '0', '5');
INSERT INTO `resource_custom` VALUES ('38', '告警中心', 'menu', '', '0', null, '5', null, '2017-09-26 14:39:33', '1', null, 'menu-icon mi-alarm', 'dev', '1', '0', '6');
INSERT INTO `resource_custom` VALUES ('39', '应用', 'menu', 'manageList', '2', '34/39', '31', null, '2017-09-20 21:39:34', '1', null, '', 'dev', '0', '2', '7');
INSERT INTO `resource_custom` VALUES ('40', '服务', 'menu', 'manageApplyList', '2', '34/40', '32', null, '2017-09-22 19:13:10', '1', null, '', 'dev', '0', '2', '8');
INSERT INTO `resource_custom` VALUES ('41', '外部服务', 'menu', 'externalService', '2', '34/41', '33', null, '2017-09-26 14:35:13', '1', null, '', 'dev', '0', '2', '9');
INSERT INTO `resource_custom` VALUES ('42', '配置中心', 'menu', 'configcenter', '2', '34/42', '34', null, '2017-09-22 19:13:10', '1', null, '', 'dev', '0', '2', '10');
INSERT INTO `resource_custom` VALUES ('43', '存储', 'menu', 'storageScheme', '2', '34/43', '35', null, '2017-09-20 22:01:16', '1', null, '', 'dev', '0', '2', '11');
INSERT INTO `resource_custom` VALUES ('44', '批量任务', 'menu', 'job', '2', '34/44', '36', null, null, '0', null, '', 'dev', '0', '2', '12');
INSERT INTO `resource_custom` VALUES ('45', 'Docker file', 'menu', 'dockerfileList', '3', '35/45', '37', null, '2017-09-26 14:39:33', '1', null, null, 'dev', '0', '3', '13');
INSERT INTO `resource_custom` VALUES ('46', '流水线', 'menu', 'pipelineList', '3', '35/46', '38', null, '2017-09-26 14:39:33', '1', null, null, 'dev', '0', '3', '14');
INSERT INTO `resource_custom` VALUES ('47', '依赖管理', 'menu', 'dependenceList', '3', '35/47', '39', null, '2017-09-26 14:39:33', '1', null, '', 'dev', '0', '3', '15');
INSERT INTO `resource_custom` VALUES ('48', '镜像仓库', 'menu', 'mirrorContent', '4', '36/48', '40', null, '2017-09-26 19:14:54', '1', null, '', 'dev', '0', '4', '16');
INSERT INTO `resource_custom` VALUES ('49', '应用商店', 'menu', 'deliveryStore', '4', '36/49', '41', null, '2017-09-26 16:41:30', '1', null, '', 'dev', '0', '4', '17');
INSERT INTO `resource_custom` VALUES ('50', '模板管理', 'menu', 'manageAll', '4', '36/50', '42', null, '2017-09-26 14:39:33', '1', null, '', 'dev', '0', '4', '18');
INSERT INTO `resource_custom` VALUES ('51', '操作审计', 'menu', 'adminAudit', '5', '37/51', '43', null, '2017-08-22 17:09:15', '0', null, '', 'dev', '0', '5', '19');
INSERT INTO `resource_custom` VALUES ('52', '告警规则', 'menu', 'alarmList', '6', '38/52', '44', null, '2017-09-26 14:39:33', '1', null, '', 'dev', '0', '6', '20');
INSERT INTO `resource_custom` VALUES ('53', '告警处理中心', 'menu', 'alarmHandingList', '6', '38/53', '45', null, '2017-09-26 14:39:33', '1', null, '', 'dev', '0', '6', '21');
INSERT INTO `resource_custom` VALUES ('63', '我的租户', 'menu', 'tenant', '0', null, '25', null, '2017-09-26 18:49:38', '0', null, 'menu-icon mi-tenant', 'ops', '1', '0', '26');
INSERT INTO `resource_custom` VALUES ('64', '应用中心', 'menu', '', '0', null, '26', null, '2017-09-26 11:21:38', '1', null, 'menu-icon mi-application', 'ops', '1', '0', '4');
INSERT INTO `resource_custom` VALUES ('65', 'CICD', 'menu', '', '0', null, '27', null, '2017-09-26 18:49:39', '0', null, 'menu-icon mi-cicd', 'ops', '1', '0', '5');
INSERT INTO `resource_custom` VALUES ('66', '交付中心', 'menu', '', '0', null, '28', null, '2017-09-26 11:21:43', '1', null, 'menu-icon mi-centers', 'ops', '1', '0', '6');
INSERT INTO `resource_custom` VALUES ('67', '日志管理', 'menu', '', '0', null, '29', null, '2017-08-22 17:09:15', '1', null, 'menu-icon mi-audit', 'ops', '1', '0', '7');
INSERT INTO `resource_custom` VALUES ('68', '告警中心', 'menu', '', '0', null, '30', null, '2017-09-26 18:49:39', '1', null, 'menu-icon mi-alarm', 'ops', '1', '0', '8');
INSERT INTO `resource_custom` VALUES ('69', '应用', 'menu', 'manageList', '34', '34/39', '31', null, '2017-09-26 11:21:38', '1', null, '', 'ops', '0', '4', '9');
INSERT INTO `resource_custom` VALUES ('70', '服务', 'menu', 'manageApplyList', '34', '34/40', '32', null, '2017-09-26 11:21:43', '1', null, '', 'ops', '0', '4', '10');
INSERT INTO `resource_custom` VALUES ('71', '外部服务', 'menu', 'externalService', '34', '34/41', '33', null, '2017-09-26 11:21:43', '1', null, '', 'ops', '0', '4', '11');
INSERT INTO `resource_custom` VALUES ('72', '配置中心', 'menu', 'configcenter', '34', '34/42', '34', null, '2017-09-26 11:21:43', '1', null, '', 'ops', '0', '4', '12');
INSERT INTO `resource_custom` VALUES ('73', '存储', 'menu', 'storageScheme', '34', '34/43', '35', null, '2017-09-26 11:21:43', '1', null, '', 'ops', '0', '4', '13');
INSERT INTO `resource_custom` VALUES ('74', '批量任务', 'menu', 'job', '34', '34/44', '36', null, null, '0', null, '', 'ops', '0', '4', '14');
INSERT INTO `resource_custom` VALUES ('75', 'Docker file', 'menu', 'dockerfileList', '35', '35/45', '37', null, '2017-09-26 18:49:39', '0', null, null, 'ops', '0', '5', '15');
INSERT INTO `resource_custom` VALUES ('76', '流水线', 'menu', 'pipelineList', '35', '35/46', '38', null, '2017-09-26 18:49:39', '0', null, null, 'ops', '0', '5', '16');
INSERT INTO `resource_custom` VALUES ('77', '依赖管理', 'menu', 'dependenceList', '35', '35/47', '39', null, '2017-09-26 18:49:39', '0', null, '', 'ops', '0', '5', '17');
INSERT INTO `resource_custom` VALUES ('78', '镜像仓库', 'menu', 'mirrorContent', '36', '36/48', '40', null, '2017-09-26 19:15:02', '1', null, '', 'ops', '0', '6', '18');
INSERT INTO `resource_custom` VALUES ('79', '应用商店', 'menu', 'deliveryStore', '36', '36/49', '41', null, '2017-09-26 11:21:43', '1', null, '', 'ops', '0', '6', '19');
INSERT INTO `resource_custom` VALUES ('80', '模板管理', 'menu', 'manageAll', '36', '36/50', '42', null, '2017-09-26 11:21:43', '1', null, '', 'ops', '0', '6', '20');
INSERT INTO `resource_custom` VALUES ('81', '操作审计', 'menu', 'adminAudit', '37', '37/51', '43', null, '2017-08-22 17:09:15', '0', null, '', 'ops', '0', '7', '21');
INSERT INTO `resource_custom` VALUES ('82', '告警规则', 'menu', 'alarmList', '38', '38/52', '44', null, '2017-09-26 18:49:39', '1', null, '', 'ops', '0', '8', '23');
INSERT INTO `resource_custom` VALUES ('83', '告警处理中心', 'menu', 'alarmHandingList', '38', '38/53', '45', null, '2017-09-26 18:49:39', '1', null, '', 'ops', '0', '8', '24');
INSERT INTO `resource_custom` VALUES ('87', '应用中心', 'menu', '', '0', null, '4', null, null, '0', null, 'menu-icon mi-application', 'default', '1', '0', '4');
INSERT INTO `resource_custom` VALUES ('88', 'CICD', 'menu', '', '0', null, '5', null, null, '0', null, 'menu-icon mi-cicd', 'default', '1', '0', '5');
INSERT INTO `resource_custom` VALUES ('89', '交付中心', 'menu', '', '0', null, '6', null, null, '0', null, 'menu-icon mi-centers', 'default', '1', '0', '6');
INSERT INTO `resource_custom` VALUES ('90', '日志管理', 'menu', '', '0', null, '7', null, null, '0', null, 'menu-icon mi-audit', 'default', '1', '0', '7');
INSERT INTO `resource_custom` VALUES ('91', '告警中心', 'menu', '', '0', null, '8', null, null, '0', null, 'menu-icon mi-alarm', 'default', '1', '0', '8');
INSERT INTO `resource_custom` VALUES ('92', '应用', 'menu', 'manageList', '0', '4/9', '9', null, null, '0', null, null, 'default', '0', '4', '9');
INSERT INTO `resource_custom` VALUES ('93', '服务', 'menu', 'manageApplyList', '0', '4/10', '10', null, null, '0', null, null, 'default', '0', '4', '10');
INSERT INTO `resource_custom` VALUES ('94', '外部服务', 'menu', 'externalService', '0', '4/11', '11', null, null, '0', null, null, 'default', '0', '4', '11');
INSERT INTO `resource_custom` VALUES ('96', '配置中心', 'menu', 'configcenter', '0', '4/12', '12', null, null, '0', null, null, 'default', '0', '4', '12');
INSERT INTO `resource_custom` VALUES ('97', '存储', 'menu', 'storageScheme', '0', '4/13', '13', null, null, '0', null, null, 'default', '0', '4', '13');
INSERT INTO `resource_custom` VALUES ('98', '批量任务', 'menu', 'job', '0', '4/14', '14', null, null, '0', null, null, 'default', '0', '4', '14');
INSERT INTO `resource_custom` VALUES ('99', 'Docker file', 'menu', 'dockerfileList', '0', '5/15', '15', null, null, '0', null, null, 'default', '0', '5', '15');
INSERT INTO `resource_custom` VALUES ('100', '流水线', 'menu', 'pipelineList', '0', '5/16', '16', null, null, '0', null, null, 'default', '0', '5', '16');
INSERT INTO `resource_custom` VALUES ('101', '依赖管理', 'menu', 'dependenceList', '0', '5/17', '17', null, null, '0', null, null, 'default', '0', '5', '17');
INSERT INTO `resource_custom` VALUES ('102', '镜像管理', 'menu', 'mirrorContent', '0', '6/18', '18', null, null, '0', null, null, 'default', '0', '6', '18');
INSERT INTO `resource_custom` VALUES ('103', '应用商店', 'menu', 'deliveryStore', '0', '6/19', '19', null, null, '0', null, null, 'default', '0', '6', '19');
INSERT INTO `resource_custom` VALUES ('104', '模板管理', 'menu', 'manageAll', '0', '6/20', '20', null, null, '0', null, null, 'default', '0', '6', '20');
INSERT INTO `resource_custom` VALUES ('105', '操作审计', 'menu', 'adminAudit', '0', '7/21', '21', null, null, '0', null, null, 'default', '0', '7', '21');
INSERT INTO `resource_custom` VALUES ('106', '日志查询', 'menu', 'logQuery', '0', '7/22', '22', null, null, '0', null, null, 'default', '0', '7', '22');
INSERT INTO `resource_custom` VALUES ('107', '告警规则', 'menu', 'alarmList', '0', '8/23', '23', null, null, '0', null, null, 'default', '0', '8', '23');
INSERT INTO `resource_custom` VALUES ('108', '告警处理中心', 'menu', 'alarmHandingList', '0', '8/24', '24', null, null, '0', null, null, 'default', '0', '8', '24');
INSERT INTO `resource_custom` VALUES ('110', '我的租户', 'menu', 'tenant', '0', null, '3', null, null, '0', null, 'menu-icon mi-tenant', 'default', '1', '0', '26');
INSERT INTO `resource_custom` VALUES ('111', '我的租户', 'menu', 'tenant', '0', null, '25', null, '2017-09-25 18:28:39', '1', null, 'menu-icon mi-tenant', 'tm', '1', '0', '1');
INSERT INTO `resource_custom` VALUES ('112', '应用中心', 'menu', '', '0', null, '26', null, '2017-09-25 17:50:40', '1', null, 'menu-icon mi-application', 'tm', '1', '0', '2');
INSERT INTO `resource_custom` VALUES ('113', 'CICD', 'menu', '', '0', null, '27', null, '2017-09-25 18:28:39', '1', null, 'menu-icon mi-cicd', 'tm', '1', '0', '3');
INSERT INTO `resource_custom` VALUES ('114', '交付中心', 'menu', '', '0', null, '28', null, '2017-09-20 21:37:48', '1', null, 'menu-icon mi-centers', 'tm', '1', '0', '4');
INSERT INTO `resource_custom` VALUES ('115', '日志管理', 'menu', '', '0', null, '29', null, '2017-09-13 16:02:57', '1', null, 'menu-icon mi-audit', 'tm', '1', '0', '5');
INSERT INTO `resource_custom` VALUES ('116', '告警中心', 'menu', '', '0', null, '30', null, '2017-09-22 19:11:52', '1', null, 'menu-icon mi-alarm', 'tm', '1', '0', '6');
INSERT INTO `resource_custom` VALUES ('117', '应用', 'menu', 'manageList', '2', '34/39', '31', null, '2017-09-25 17:50:40', '1', null, '', 'tm', '0', '2', '7');
INSERT INTO `resource_custom` VALUES ('118', '服务', 'menu', 'manageApplyList', '2', '34/40', '32', null, '2017-09-25 17:50:54', '1', null, '', 'tm', '0', '2', '8');
INSERT INTO `resource_custom` VALUES ('119', '外部服务', 'menu', 'externalService', '2', '34/41', '33', null, '2017-09-25 17:50:40', '1', null, '', 'tm', '0', '2', '9');
INSERT INTO `resource_custom` VALUES ('120', '配置中心', 'menu', 'configcenter', '2', '34/42', '34', null, '2017-09-25 17:50:54', '1', null, '', 'tm', '0', '2', '10');
INSERT INTO `resource_custom` VALUES ('121', '存储', 'menu', 'storageScheme', '2', '34/43', '35', null, '2017-09-25 17:50:54', '1', null, '', 'tm', '0', '2', '11');
INSERT INTO `resource_custom` VALUES ('122', '批量任务', 'menu', 'job', '2', '34/44', '36', null, null, '0', null, '', 'tm', '0', '2', '12');
INSERT INTO `resource_custom` VALUES ('123', 'Docker file', 'menu', 'dockerfileList', '3', '35/45', '37', null, '2017-09-25 18:28:39', '1', null, null, 'tm', '0', '3', '13');
INSERT INTO `resource_custom` VALUES ('124', '流水线', 'menu', 'pipelineList', '3', '35/46', '38', null, '2017-09-25 18:28:39', '1', null, null, 'tm', '0', '3', '14');
INSERT INTO `resource_custom` VALUES ('125', '依赖管理', 'menu', 'dependenceList', '3', '35/47', '39', null, '2017-09-25 18:28:39', '1', null, '', 'tm', '0', '3', '15');
INSERT INTO `resource_custom` VALUES ('126', '镜像仓库', 'menu', 'mirrorContent', '4', '36/48', '40', null, '2017-09-25 18:09:18', '1', null, '', 'tm', '0', '4', '16');
INSERT INTO `resource_custom` VALUES ('127', '应用商店', 'menu', 'deliveryStore', '4', '36/49', '41', null, '2017-09-25 17:50:54', '1', null, '', 'tm', '0', '4', '17');
INSERT INTO `resource_custom` VALUES ('128', '模板管理', 'menu', 'manageAll', '4', '36/50', '42', null, '2017-09-20 21:37:48', '1', null, '', 'tm', '0', '4', '18');
INSERT INTO `resource_custom` VALUES ('129', '操作审计', 'menu', 'adminAudit', '5', '37/51', '43', null, '2017-08-22 17:09:15', '1', null, '', 'tm', '0', '5', '19');
INSERT INTO `resource_custom` VALUES ('130', '告警规则', 'menu', 'alarmList', '6', '38/52', '44', null, '2017-09-22 19:11:53', '1', null, '', 'tm', '0', '6', '20');
INSERT INTO `resource_custom` VALUES ('131', '告警处理中心', 'menu', 'alarmHandingList', '6', '38/53', '45', null, '2017-09-22 19:11:53', '1', null, '', 'tm', '0', '6', '21');
INSERT INTO `resource_custom` VALUES ('133', '告警规则', 'menu', 'alarmList', '0', '8/23', '23', null, null, '0', null, null, 'tm', '0', '8', '23');
INSERT INTO `resource_custom` VALUES ('134', '告警处理中心', 'menu', 'alarmHandingList', '0', '8/24', '24', null, null, '0', null, null, 'tm', '0', '8', '24');
INSERT INTO `resource_custom` VALUES ('140', '应用中心', 'menu', '', '0', null, '4', null, '2017-09-26 14:39:34', '1', null, 'menu-icon mi-application', 'tester', '1', '0', '4');
INSERT INTO `resource_custom` VALUES ('141', 'CICD', 'menu', '', '0', null, '5', null, '2017-09-25 18:40:49', '0', null, 'menu-icon mi-cicd', 'tester', '1', '0', '5');
INSERT INTO `resource_custom` VALUES ('142', '交付中心', 'menu', '', '0', null, '6', null, '2017-09-20 21:40:07', '1', null, 'menu-icon mi-centers', 'tester', '1', '0', '6');
INSERT INTO `resource_custom` VALUES ('143', '日志管理', 'menu', '', '0', null, '7', null, '2017-09-12 19:49:01', '1', null, 'menu-icon mi-audit', 'tester', '1', '0', '7');
INSERT INTO `resource_custom` VALUES ('144', '告警中心', 'menu', '', '0', null, '8', null, '2017-09-20 21:01:13', '1', null, 'menu-icon mi-alarm', 'tester', '1', '0', '8');
INSERT INTO `resource_custom` VALUES ('145', '应用', 'menu', 'manageList', '0', '4/9', '9', null, '2017-09-26 14:39:34', '1', null, null, 'tester', '0', '4', '9');
INSERT INTO `resource_custom` VALUES ('146', '服务', 'menu', 'manageApplyList', '0', '4/10', '10', null, '2017-09-26 14:39:34', '1', null, null, 'tester', '0', '4', '10');
INSERT INTO `resource_custom` VALUES ('147', '外部服务', 'menu', 'externalService', '0', '4/11', '11', null, '2017-09-26 14:39:34', '1', null, null, 'tester', '0', '4', '11');
INSERT INTO `resource_custom` VALUES ('148', '配置中心', 'menu', 'configcenter', '0', '4/12', '12', null, '2017-09-26 14:39:34', '1', null, null, 'tester', '0', '4', '12');
INSERT INTO `resource_custom` VALUES ('149', '存储', 'menu', 'storageScheme', '0', '4/13', '13', null, '2017-09-26 14:39:34', '1', null, null, 'tester', '0', '4', '13');
INSERT INTO `resource_custom` VALUES ('150', '批量任务', 'menu', 'job', '0', '4/14', '14', null, null, '0', null, null, 'tester', '0', '4', '14');
INSERT INTO `resource_custom` VALUES ('151', 'Docker file', 'menu', 'dockerfileList', '0', '5/15', '15', null, '2017-09-25 18:40:49', '0', null, null, 'tester', '0', '5', '15');
INSERT INTO `resource_custom` VALUES ('152', '流水线', 'menu', 'pipelineList', '0', '5/16', '16', null, '2017-09-25 18:40:49', '0', null, null, 'tester', '0', '5', '16');
INSERT INTO `resource_custom` VALUES ('153', '依赖管理', 'menu', 'dependenceList', '0', '5/17', '17', null, '2017-09-25 18:40:49', '0', null, null, 'tester', '0', '5', '17');
INSERT INTO `resource_custom` VALUES ('154', '镜像管理', 'menu', 'mirrorContent', '0', '6/18', '18', null, null, '1', null, null, 'tester', '0', '6', '18');
INSERT INTO `resource_custom` VALUES ('155', '应用商店', 'menu', 'deliveryStore', '0', '6/19', '19', null, '2017-09-20 21:40:07', '1', null, null, 'tester', '0', '6', '19');
INSERT INTO `resource_custom` VALUES ('156', '模板管理', 'menu', 'manageAll', '0', '6/20', '20', null, '2017-09-20 21:40:07', '1', null, null, 'tester', '0', '6', '20');
INSERT INTO `resource_custom` VALUES ('157', '操作审计', 'menu', 'adminAudit', '0', '7/21', '21', null, null, '1', null, null, 'tester', '0', '7', '21');
INSERT INTO `resource_custom` VALUES ('158', '日志查询', 'menu', 'logQuery', '0', '7/22', '22', null, null, '1', null, null, 'tester', '0', '7', '22');
INSERT INTO `resource_custom` VALUES ('159', '告警规则', 'menu', 'alarmList', '0', '8/23', '23', null, '2017-09-20 21:01:13', '1', null, null, 'tester', '0', '8', '23');
INSERT INTO `resource_custom` VALUES ('160', '告警处理中心', 'menu', 'alarmHandingList', '0', '8/24', '24', null, '2017-09-20 21:01:13', '1', null, null, 'tester', '0', '8', '24');
INSERT INTO `resource_custom` VALUES ('162', '我的租户', 'menu', 'tenant', '0', null, '3', null, '2017-09-26 09:57:35', '0', null, 'menu-icon mi-tenant', 'tester', '1', '0', '26');
INSERT INTO `resource_custom` VALUES ('163', '日志查询', 'menu', 'logQuery', '7', '7/22', '22', null, null, '1', null, '', 'dev', '0', '5', '22');
INSERT INTO `resource_custom` VALUES ('164', '日志查询', 'menu', 'logQuery', '7', '7/22', '22', null, null, '1', null, '', 'ops', '0', '7', '25');
INSERT INTO `resource_custom` VALUES ('165', '日志查询', 'menu', 'logQuery', '7', '7/22', '22', null, null, '1', null, '', 'tm', '0', '5', '25');

/*
Navicat MySQL Data Transfer

Source Server         : 10.10.124.46
Source Server Version : 50635
Source Host           : 10.10.124.46:30306
Source Database       : k8s_auth_server

Target Server Type    : MYSQL
Target Server Version : 50635
File Encoding         : 65001

Date: 2018-04-09 19:50:35
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for resource_menu
-- ----------------------------
DROP TABLE IF EXISTS `resource_menu`;
CREATE TABLE `resource_menu` (
  `id` int(10) NOT NULL AUTO_INCREMENT COMMENT '序号',
  `name` varchar(100) NOT NULL COMMENT '资源名称',
  `name_en` varchar(100) NOT NULL COMMENT '资源名称',
  `type` varchar(10) NOT NULL DEFAULT 'menu' COMMENT '类型',
  `url` varchar(100) NOT NULL COMMENT '资源路径',
  `weight` int(100) DEFAULT '0' COMMENT '权重',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `available` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否可用 1可用 0不可用',
  `icon_name` varchar(45) DEFAULT NULL COMMENT '图标名',
  `isparent` tinyint(1) DEFAULT NULL COMMENT '是否为父节点 1父节点 0子节点',
  `parent_rmid` int(11) DEFAULT NULL,
  `module` varchar(100) DEFAULT NULL COMMENT '模块名',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `name` (`name`,`url`,`parent_rmid`,`module`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT;

-- ----------------------------
-- Records of resource_menu
-- ----------------------------
INSERT INTO `resource_menu` VALUES ('1', '总览', 'Overview', 'menu', 'overview', '1', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', 'menu-icon mi-overview', '1', '0', 'dashboard');
INSERT INTO `resource_menu` VALUES ('2', '集群', 'Cluster', 'menu', 'cluster', '2', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', 'menu-icon mi-cluster', '1', '0', 'infrastructure');
INSERT INTO `resource_menu` VALUES ('3', '租户管理', 'Tenant Management', 'menu', 'tenant', '3', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', 'menu-icon mi-tenant', '1', '0', 'tenant');
INSERT INTO `resource_menu` VALUES ('4', '我的租户', 'My Tenant', 'menu', 'mytenant', '4', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', 'menu-icon mi-tenant', '1', '0', 'tenant');
INSERT INTO `resource_menu` VALUES ('5', '我的项目', 'My Project', 'menu', 'project', '5', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', 'menu-icon mi-tenant', '1', '0', 'tenant');
INSERT INTO `resource_menu` VALUES ('6', '应用中心', 'Application Center', 'menu', '', '6', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', 'menu-icon mi-application', '1', '0', 'appcenter');
INSERT INTO `resource_menu` VALUES ('7', 'CICD', 'CICD', 'menu', '', '7', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', 'menu-icon mi-cicd', '1', '0', 'cicd');
INSERT INTO `resource_menu` VALUES ('8', '交付中心', 'Deliver Center', 'menu', '', '8', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', 'menu-icon mi-centers', '1', '0', 'delivery');
INSERT INTO `resource_menu` VALUES ('9', '日志管理', 'Log Management', 'menu', '', '9', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', 'menu-icon mi-audit', '1', '0', 'log');
INSERT INTO `resource_menu` VALUES ('10', '告警中心', 'Alarm Center', 'menu', '', '10', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', 'menu-icon mi-alarm', '1', '0', 'alarm');
INSERT INTO `resource_menu` VALUES ('11', '系统设置', 'System Settings', 'menu', 'system', '11', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', 'menu-icon mi-config', '1', '0', 'system');
INSERT INTO `resource_menu` VALUES ('12', '应用', 'Application', 'menu', 'app', '12', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '', '0', '6', 'app');
INSERT INTO `resource_menu` VALUES ('13', '服务', 'Service', 'menu', 'service', '13', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '', '0', '6', 'app');
INSERT INTO `resource_menu` VALUES ('14', '守护进程', 'Daemonset', 'menu', 'daemonset', '14', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '', '0', '6', 'daemonset');
INSERT INTO `resource_menu` VALUES ('15', '外部服务', 'External Service', 'menu', 'externalService', '15', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '', '0', '6', 'externalservice');
INSERT INTO `resource_menu` VALUES ('16', '配置中心', 'Configuration Center', 'menu', 'configCenter', '16', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '', '0', '6', 'configmap');
INSERT INTO `resource_menu` VALUES ('17', '存储', 'Storage', 'menu', 'storage', '17', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '', '0', '6', 'volume');
INSERT INTO `resource_menu` VALUES ('18', '批量任务', 'Batch Job', 'menu', 'batchjob', '18', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '0', '', '0', '6', 'job');
INSERT INTO `resource_menu` VALUES ('19', 'Dockerfile', 'Dockerfile', 'menu', 'dockerfile', '19', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '', '0', '7', 'cicdmgr');
INSERT INTO `resource_menu` VALUES ('20', '流水线', 'Pipeline', 'menu', 'pipeline', '20', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '', '0', '7', 'cicdmgr');
INSERT INTO `resource_menu` VALUES ('21', '依赖管理', 'Dependence', 'menu', 'dependence', '21', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '', '0', '7', 'env');
INSERT INTO `resource_menu` VALUES ('22', '环境管理', 'Environment', 'menu', 'environment', '22', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '', '0', '7', 'env');
INSERT INTO `resource_menu` VALUES ('23', '镜像仓库', 'Image Repository', 'menu', 'imageRepository', '23', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '', '0', '8', 'repository');
INSERT INTO `resource_menu` VALUES ('24', '应用商店', 'APP Store', 'menu', 'appStore', '24', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '', '0', '8', 'onlineshop');
INSERT INTO `resource_menu` VALUES ('25', '模板管理', 'Template Management', 'menu', 'manageAll', '25', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '', '0', '8', 'template');
INSERT INTO `resource_menu` VALUES ('26', '操作审计', 'Audit', 'menu', 'logAudit', '26', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '', '0', '9', 'auditlog');
INSERT INTO `resource_menu` VALUES ('27', '日志查询', 'Log Query', 'menu', 'logQuery', '27', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '', '0', '9', 'applog');
INSERT INTO `resource_menu` VALUES ('28', '告警规则', 'Alarm Rules', 'menu', 'alarmRules', '28', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '', '0', '10', 'alarmrule');
INSERT INTO `resource_menu` VALUES ('29', '告警处理中心', 'Alarm processing center', 'menu', 'alarmProcessing', '29', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '', '0', '10', 'alarmhandle');
INSERT INTO `resource_menu` VALUES ('30', '日志备份', 'Log Backup', 'menu', 'logBackup', '30', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '', '0', '9', 'snapshotrule');
INSERT INTO `resource_menu` VALUES ('31', '系统日志', 'Ssystem Log', 'menu', 'systemLog', '31', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '', '0', '9', 'systemlog');

/*
Navicat MySQL Data Transfer

Source Server         : 10.10.124.46
Source Server Version : 50635
Source Host           : 10.10.124.46:30306
Source Database       : k8s_auth_server

Target Server Type    : MYSQL
Target Server Version : 50635
File Encoding         : 65001

Date: 2018-04-09 19:52:18
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for resource_menu_role
-- ----------------------------
DROP TABLE IF EXISTS `resource_menu_role`;
CREATE TABLE `resource_menu_role` (
  `id` int(10) NOT NULL AUTO_INCREMENT COMMENT '序号',
  `weight` int(100) DEFAULT '0' COMMENT '权重',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `available` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否可用 1可用 0不可用',
  `role_id` int(11) DEFAULT NULL,
  `rmid` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `role_menu` (`role_id`,`rmid`) USING BTREE,
  KEY `roleid` (`role_id`,`available`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=218 DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT;

-- ----------------------------
-- Records of resource_menu_role
-- ----------------------------
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('1', '1', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '1', '1');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('2', '2', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '1', '2');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('3', '3', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '1', '3');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('4', '4', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '0', '1', '4');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('5', '5', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '0', '1', '5');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('6', '6', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '1', '6');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('7', '7', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '1', '7');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('8', '8', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '1', '8');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('9', '9', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '1', '9');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('10', '10', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '1', '10');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('11', '11', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '1', '11');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('12', '12', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '1', '12');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('13', '13', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '1', '13');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('14', '14', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '1', '14');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('15', '15', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '1', '15');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('16', '16', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '1', '16');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('17', '17', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '1', '17');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('18', '18', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '0', '1', '18');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('19', '19', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '1', '19');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('20', '20', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '1', '20');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('21', '21', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '1', '21');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('22', '22', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '1', '22');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('23', '23', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '1', '23');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('24', '24', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '1', '24');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('25', '25', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '1', '25');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('26', '26', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '1', '26');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('27', '27', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '1', '27');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('28', '28', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '1', '28');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('29', '29', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '1', '29');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('30', '30', '2018-02-12 05:52:00', '2018-02-12 14:54:00', '1', '1', '30');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('31', '31', '2018-02-12 05:52:00', '2018-02-12 14:54:00', '1', '1', '31');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('32', '1', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '0', '2', '1');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('33', '2', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '0', '2', '2');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('34', '3', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '0', '2', '3');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('35', '4', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '2', '4');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('36', '5', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '0', '2', '5');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('37', '6', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '2', '6');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('38', '7', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '2', '7');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('39', '8', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '2', '8');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('40', '9', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '2', '9');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('41', '10', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '2', '10');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('42', '11', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '0', '2', '11');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('43', '12', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '2', '12');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('44', '13', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '2', '13');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('45', '14', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '0', '2', '14');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('46', '15', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '2', '15');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('47', '16', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '2', '16');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('48', '17', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '2', '17');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('49', '18', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '0', '2', '18');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('50', '19', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '2', '19');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('51', '20', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '2', '20');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('52', '21', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '2', '21');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('53', '22', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '2', '22');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('54', '23', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '2', '23');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('55', '24', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '2', '24');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('56', '25', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '2', '25');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('57', '26', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '2', '26');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('58', '27', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '2', '27');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('59', '28', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '2', '28');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('60', '29', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '2', '29');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('61', '30', '2018-02-27 03:02:00', '2018-03-02 09:24:00', '1', '2', '30');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('62', '31', '2018-02-27 03:02:00', '2018-03-02 09:24:00', '0', '2', '31');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('63', '1', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '0', '3', '1');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('64', '2', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '0', '3', '2');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('65', '3', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '0', '3', '3');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('66', '4', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '0', '3', '4');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('67', '5', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '3', '5');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('68', '6', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '3', '6');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('69', '7', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '3', '7');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('70', '8', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '3', '8');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('71', '9', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '3', '9');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('72', '10', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '3', '10');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('73', '11', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '0', '3', '11');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('74', '12', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '3', '12');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('75', '13', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '3', '13');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('76', '14', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '0', '3', '14');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('77', '15', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '3', '15');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('78', '16', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '3', '16');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('79', '17', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '3', '17');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('80', '18', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '0', '3', '18');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('81', '19', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '3', '19');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('82', '20', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '3', '20');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('83', '21', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '3', '21');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('84', '22', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '3', '22');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('85', '23', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '3', '23');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('86', '24', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '3', '24');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('87', '25', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '3', '25');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('88', '26', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '3', '26');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('89', '27', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '3', '27');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('90', '28', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '3', '28');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('91', '29', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '3', '29');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('92', '30', '2018-02-27 03:02:00', '2018-03-02 09:24:00', '1', '3', '30');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('93', '31', '2018-02-27 03:02:00', '2018-03-02 09:24:00', '0', '3', '31');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('94', '1', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '0', '4', '1');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('95', '2', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '0', '4', '2');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('96', '3', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '0', '4', '3');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('97', '4', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '0', '4', '4');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('98', '5', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '4', '5');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('99', '6', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '4', '6');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('100', '7', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '4', '7');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('101', '8', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '4', '8');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('102', '9', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '4', '9');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('103', '10', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '4', '10');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('104', '11', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '0', '4', '11');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('105', '12', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '4', '12');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('106', '13', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '4', '13');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('107', '14', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '0', '4', '14');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('108', '15', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '4', '15');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('109', '16', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '4', '16');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('110', '17', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '4', '17');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('111', '18', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '0', '4', '18');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('112', '19', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '4', '19');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('113', '20', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '4', '20');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('114', '21', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '4', '21');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('115', '22', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '4', '22');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('116', '23', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '4', '23');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('117', '24', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '4', '24');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('118', '25', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '4', '25');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('119', '26', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '4', '26');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('120', '27', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '4', '27');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('121', '28', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '4', '28');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('122', '29', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '4', '29');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('123', '30', '2018-02-27 03:02:00', '2018-03-02 09:24:00', '1', '4', '30');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('124', '31', '2018-02-27 03:02:00', '2018-03-02 09:24:00', '0', '4', '31');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('125', '1', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '0', '5', '1');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('126', '2', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '0', '5', '2');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('127', '3', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '0', '5', '3');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('128', '4', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '0', '5', '4');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('129', '5', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '5', '5');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('130', '6', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '5', '6');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('131', '7', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '5', '7');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('132', '8', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '5', '8');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('133', '9', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '5', '9');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('134', '10', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '5', '10');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('135', '11', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '0', '5', '11');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('136', '12', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '5', '12');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('137', '13', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '5', '13');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('138', '14', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '0', '5', '14');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('139', '15', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '5', '15');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('140', '16', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '5', '16');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('141', '17', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '5', '17');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('142', '18', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '0', '5', '18');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('143', '19', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '5', '19');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('144', '20', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '5', '20');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('145', '21', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '5', '21');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('146', '22', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '5', '22');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('147', '23', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '5', '23');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('148', '24', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '5', '24');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('149', '25', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '5', '25');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('150', '26', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '5', '26');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('151', '27', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '5', '27');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('152', '28', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '5', '28');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('153', '29', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '5', '29');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('154', '30', '2018-02-27 03:02:00', '2018-03-02 09:24:00', '1', '5', '30');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('155', '31', '2018-02-27 03:02:00', '2018-03-02 09:24:00', '0', '5', '31');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('156', '1', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '0', '6', '1');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('157', '2', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '0', '6', '2');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('158', '3', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '0', '6', '3');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('159', '4', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '0', '6', '4');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('160', '5', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '6', '5');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('161', '6', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '6', '6');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('162', '7', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '6', '7');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('163', '8', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '6', '8');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('164', '9', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '6', '9');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('165', '10', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '6', '10');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('166', '11', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '0', '6', '11');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('167', '12', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '6', '12');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('168', '13', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '6', '13');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('169', '14', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '0', '6', '14');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('170', '15', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '6', '15');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('171', '16', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '6', '16');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('172', '17', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '6', '17');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('173', '18', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '0', '6', '18');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('174', '19', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '6', '19');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('175', '20', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '6', '20');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('176', '21', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '6', '21');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('177', '22', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '6', '22');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('178', '23', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '6', '23');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('179', '24', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '6', '24');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('180', '25', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '6', '25');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('181', '26', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '6', '26');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('182', '27', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '6', '27');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('183', '28', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '6', '28');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('184', '29', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '6', '29');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('185', '30', '2018-02-27 03:02:00', '2018-03-02 09:24:00', '1', '6', '30');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('186', '31', '2018-02-27 03:02:00', '2018-03-02 09:24:00', '1', '6', '31');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('187', '1', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '0', '7', '1');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('188', '2', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '0', '7', '2');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('189', '3', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '0', '7', '3');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('190', '4', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '0', '7', '4');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('191', '5', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '7', '5');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('192', '6', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '7', '6');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('193', '7', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '7', '7');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('194', '8', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '7', '8');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('195', '9', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '7', '9');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('196', '10', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '7', '10');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('197', '11', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '0', '7', '11');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('198', '12', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '7', '12');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('199', '13', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '7', '13');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('200', '14', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '0', '7', '14');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('201', '15', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '7', '15');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('202', '16', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '7', '16');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('203', '17', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '7', '17');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('204', '18', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '0', '7', '18');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('205', '19', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '7', '19');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('206', '20', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '7', '20');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('207', '21', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '7', '21');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('208', '22', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '7', '22');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('209', '23', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '7', '23');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('210', '24', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '7', '24');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('211', '25', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '7', '25');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('212', '26', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '7', '26');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('213', '27', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '7', '27');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('214', '28', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '7', '28');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('215', '29', '2017-09-26 16:36:00', '2017-09-26 16:36:00', '1', '7', '29');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('216', '30', '2018-02-27 03:02:00', '2018-03-02 09:24:00', '1', '7', '30');
INSERT INTO `k8s_auth_server`.`resource_menu_role` (`id`, `weight`, `create_time`, `update_time`, `available`, `role_id`, `rmid`) VALUES ('217', '31', '2018-02-27 03:02:00', '2018-03-02 09:24:00', '0', '7', '31');


-- ----------------------------
-- Table structure for role_new
-- ----------------------------
DROP TABLE IF EXISTS `role_new`;
CREATE TABLE `role_new` (
  `id` int(10) NOT NULL AUTO_INCREMENT COMMENT '序号',
  `name` varchar(64) DEFAULT NULL COMMENT '角色名称',
  `nick_name` varchar(128) DEFAULT NULL COMMENT '描述',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `available` tinyint(1) DEFAULT '1' COMMENT '是否可用 1可用 0不可用',
  `cluster_ids` varchar(1024) DEFAULT NULL COMMENT '集群id',
  `namespace_names` varchar(512) DEFAULT NULL COMMENT '分区名',
  `reserve1` varchar(255) DEFAULT NULL COMMENT '预留字段',
  `reserve2` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `name_UNIQUE` (`name`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT;

-- ----------------------------
-- Records of role_new
-- ----------------------------
INSERT INTO `role_new` VALUES ('1', 'admin', '系统管理员', null, null, '1', '', null, null, null);
INSERT INTO `role_new` VALUES ('2', 'tm', '租户管理员', null, '2017-09-11 15:08:00', '1', '', null, null, null);
INSERT INTO `role_new` VALUES ('3', 'pm', '项目管理员', '2017-12-13 09:09:58', '2018-01-22 02:48:29', '1', '', null, null, null);
INSERT INTO `role_new` VALUES ('4', 'dev', '开发人员', '2018-01-17 20:50:09', '2017-09-11 15:58:58', '0', '', '', '', '');
INSERT INTO `role_new` VALUES ('5', 'test', '测试人员', '2018-01-17 20:49:38', '2017-09-11 15:58:58', '0', '', '', '', '');
INSERT INTO `role_new` VALUES ('6', 'ops', '运维人员', '2018-01-17 20:50:29', '2017-09-11 15:58:58', '0', '', '', '', '');
INSERT INTO `role_new` VALUES ('7', 'uat', 'UAT', '2018-01-17 20:50:29', '2017-09-11 15:58:58', '0', '', '', '', '');

/*
Navicat MySQL Data Transfer

Source Server         : 10.10.124.46
Source Server Version : 50635
Source Host           : 10.10.124.46:30306
Source Database       : k8s_auth_server

Target Server Type    : MYSQL
Target Server Version : 50635
File Encoding         : 65001

Date: 2018-04-09 19:40:53
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for role_privilege_new
-- ----------------------------
DROP TABLE IF EXISTS `role_privilege_new`;
CREATE TABLE `role_privilege_new` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `status` tinyint(1) DEFAULT NULL COMMENT '状态',
  `role_id` int(11) DEFAULT NULL,
  `pid` int(11) DEFAULT NULL COMMENT '权限表中的id',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `reserve1` varchar(255) DEFAULT NULL COMMENT '预留字段',
  `reserve2` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `role_id` (`role_id`,`pid`) USING BTREE,
  KEY `角色id` (`role_id`,`status`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=659 DEFAULT CHARSET=utf8;


-- ----------------------------
-- Records of role_privilege_new
-- ----------------------------
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('1', '1', '1', '1', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('2', '1', '1', '2', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('3', '1', '1', '3', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('4', '1', '1', '4', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('5', '1', '1', '5', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('6', '1', '1', '6', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('7', '1', '1', '7', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('8', '1', '1', '8', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('9', '1', '1', '9', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('10', '1', '1', '10', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('11', '1', '1', '11', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('12', '1', '1', '12', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('13', '1', '1', '13', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('14', '1', '1', '14', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('15', '1', '1', '15', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('16', '1', '1', '16', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('17', '1', '1', '17', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('18', '1', '1', '18', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('19', '1', '1', '19', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('20', '1', '1', '20', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('21', '1', '1', '21', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('22', '1', '1', '22', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('23', '1', '1', '23', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('24', '1', '1', '24', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('25', '1', '1', '25', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('26', '1', '1', '26', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('27', '1', '1', '27', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('28', '1', '1', '28', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('29', '1', '1', '29', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('30', '1', '1', '30', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('31', '1', '1', '31', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('32', '1', '1', '32', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('33', '1', '1', '33', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('34', '1', '1', '34', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('35', '1', '1', '35', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('36', '1', '1', '36', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('37', '1', '1', '37', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('38', '1', '1', '38', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('39', '1', '1', '39', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('40', '1', '1', '40', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('41', '1', '1', '41', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('42', '1', '1', '42', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('43', '1', '1', '43', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('44', '1', '1', '44', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('45', '1', '1', '45', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('46', '1', '1', '46', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('47', '1', '1', '47', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('48', '1', '1', '48', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('49', '1', '1', '49', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('50', '1', '1', '50', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('51', '1', '1', '51', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('52', '1', '1', '52', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('53', '1', '1', '53', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('54', '1', '1', '54', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('55', '1', '1', '55', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('56', '1', '1', '56', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('57', '1', '1', '57', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('58', '1', '1', '58', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('59', '1', '1', '59', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('60', '1', '1', '60', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('61', '1', '1', '61', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('62', '1', '1', '62', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('63', '1', '1', '63', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('64', '1', '1', '64', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('65', '1', '1', '65', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('66', '1', '1', '66', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('67', '1', '1', '67', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('68', '1', '1', '68', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('69', '1', '1', '69', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('70', '1', '1', '70', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('71', '1', '1', '71', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('72', '1', '1', '72', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('73', '1', '1', '73', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('74', '1', '1', '74', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('75', '1', '1', '75', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('76', '1', '1', '76', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('77', '1', '1', '77', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('78', '1', '1', '78', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('79', '1', '1', '79', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('80', '1', '1', '80', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('81', '1', '1', '81', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('82', '1', '1', '82', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('83', '1', '1', '83', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('84', '1', '1', '84', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('85', '1', '1', '85', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('86', '1', '1', '86', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('87', '1', '1', '87', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('88', '1', '1', '88', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('89', '1', '1', '89', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('90', '1', '1', '90', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('91', '1', '1', '91', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('92', '1', '1', '92', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('93', '1', '1', '93', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('94', '1', '1', '94', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('95', '0', '2', '1', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('96', '0', '2', '2', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('97', '0', '2', '3', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('98', '0', '2', '4', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('99', '0', '2', '5', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('100', '0', '2', '6', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('101', '0', '2', '7', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('102', '0', '2', '8', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('103', '0', '2', '9', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('104', '0', '2', '10', '2018-01-13 07:13:00', '2018-03-05 03:07:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('105', '0', '2', '11', '2018-01-13 07:13:00', '2018-03-05 03:07:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('106', '0', '2', '12', '2018-01-13 07:13:00', '2018-03-05 03:07:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('107', '0', '2', '13', '2018-01-13 07:13:00', '2018-03-05 03:07:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('108', '1', '2', '14', '2018-01-13 07:13:00', '2018-03-05 03:07:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('109', '1', '2', '15', '2018-01-13 07:13:00', '2018-03-05 03:07:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('110', '1', '2', '16', '2018-01-13 07:13:00', '2018-03-05 03:07:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('111', '1', '2', '17', '2018-01-13 07:13:00', '2018-03-05 03:07:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('112', '1', '2', '18', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('113', '1', '2', '19', '2018-01-13 07:13:00', '2018-03-03 07:55:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('114', '1', '2', '20', '2018-01-13 07:13:00', '2018-03-03 07:55:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('115', '1', '2', '21', '2018-01-13 07:13:00', '2018-03-03 07:55:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('116', '0', '2', '22', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('117', '0', '2', '23', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('118', '0', '2', '24', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('119', '0', '2', '25', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('120', '1', '2', '26', '2018-01-13 07:13:00', '2018-03-05 03:04:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('121', '1', '2', '27', '2018-01-13 07:13:00', '2018-03-05 03:04:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('122', '1', '2', '28', '2018-01-13 07:13:00', '2018-03-05 03:04:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('123', '1', '2', '29', '2018-01-13 07:13:00', '2018-03-05 03:04:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('124', '1', '2', '30', '2018-01-13 07:13:00', '2018-03-05 03:04:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('125', '1', '2', '31', '2018-01-13 07:13:00', '2018-03-05 03:04:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('126', '1', '2', '32', '2018-01-13 07:13:00', '2018-03-05 03:04:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('127', '1', '2', '33', '2018-01-13 07:13:00', '2018-03-05 03:04:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('128', '1', '2', '34', '2018-01-13 07:13:00', '2018-03-05 03:04:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('129', '1', '2', '35', '2018-01-13 07:13:00', '2018-03-05 03:04:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('130', '1', '2', '36', '2018-01-13 07:13:00', '2018-03-05 03:04:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('131', '1', '2', '37', '2018-01-13 07:13:00', '2018-03-05 03:04:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('132', '1', '2', '38', '2018-01-13 07:13:00', '2018-03-05 03:04:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('133', '1', '2', '39', '2018-01-13 07:13:00', '2018-03-05 03:04:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('134', '1', '2', '40', '2018-01-13 07:13:00', '2018-03-05 03:04:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('135', '1', '2', '41', '2018-01-13 07:13:00', '2018-03-05 03:04:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('136', '1', '2', '42', '2018-01-13 07:13:00', '2018-03-05 03:04:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('137', '1', '2', '43', '2018-01-13 07:13:00', '2018-03-05 03:04:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('138', '1', '2', '44', '2018-01-13 07:13:00', '2018-03-08 07:55:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('139', '1', '2', '45', '2018-01-13 07:13:00', '2018-03-08 07:55:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('140', '1', '2', '46', '2018-01-13 07:13:00', '2018-03-08 07:55:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('141', '1', '2', '47', '2018-01-13 07:13:00', '2018-03-08 07:55:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('142', '1', '2', '48', '2018-01-13 07:13:00', '2018-03-08 07:55:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('143', '0', '2', '49', '2018-01-13 07:13:00', '2018-03-08 08:26:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('144', '0', '2', '50', '2018-01-13 07:13:00', '2018-03-08 08:26:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('145', '0', '2', '51', '2018-01-13 07:13:00', '2018-03-08 08:26:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('146', '0', '2', '52', '2018-01-13 07:13:00', '2018-03-08 08:26:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('147', '1', '2', '53', '2018-01-13 07:13:00', '2018-03-08 07:55:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('148', '1', '2', '54', '2018-01-13 07:13:00', '2018-03-08 07:55:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('149', '1', '2', '55', '2018-01-13 07:13:00', '2018-03-08 07:55:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('150', '1', '2', '56', '2018-01-13 07:13:00', '2018-03-08 07:55:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('151', '1', '2', '57', '2018-01-13 07:13:00', '2018-03-08 07:55:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('152', '1', '2', '58', '2018-01-13 07:13:00', '2018-03-08 07:55:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('153', '1', '2', '59', '2018-01-13 07:13:00', '2018-03-08 07:55:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('154', '1', '2', '60', '2018-01-13 07:13:00', '2018-03-08 07:55:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('155', '1', '2', '61', '2018-01-13 07:13:00', '2018-03-08 07:55:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('156', '1', '2', '62', '2018-01-13 07:13:00', '2018-03-08 07:55:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('157', '1', '2', '63', '2018-01-13 07:13:00', '2018-03-08 07:55:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('158', '1', '2', '64', '2018-01-13 07:13:00', '2018-03-08 07:55:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('159', '1', '2', '65', '2018-01-13 07:13:00', '2018-03-08 07:54:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('160', '1', '2', '66', '2018-01-13 07:13:00', '2018-03-08 07:54:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('161', '1', '2', '67', '2018-01-13 07:13:00', '2018-03-08 07:54:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('162', '1', '2', '68', '2018-01-13 07:13:00', '2018-03-08 07:54:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('163', '1', '2', '69', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('164', '1', '2', '70', '2018-01-13 07:13:00', '2018-03-03 07:55:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('165', '1', '2', '71', '2018-01-13 07:13:00', '2018-03-03 07:55:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('166', '1', '2', '72', '2018-01-13 07:13:00', '2018-03-03 07:55:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('167', '1', '2', '73', '2018-01-13 07:13:00', '2018-03-03 07:55:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('168', '1', '2', '74', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('169', '1', '2', '75', '2018-01-13 07:13:00', '2018-03-03 07:55:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('170', '1', '2', '76', '2018-01-13 07:13:00', '2018-03-03 07:55:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('171', '1', '2', '77', '2018-01-13 07:13:00', '2018-03-03 07:55:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('172', '1', '2', '78', '2018-01-13 07:13:00', '2018-03-03 07:55:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('173', '1', '2', '79', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('174', '0', '2', '80', '2018-01-13 07:13:00', '2018-03-29 14:05:22', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('175', '1', '2', '81', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('176', '1', '2', '82', '2018-01-13 07:13:00', '2018-03-03 07:55:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('177', '1', '2', '83', '2018-01-13 07:13:00', '2018-03-03 07:55:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('178', '1', '2', '84', '2018-01-13 07:13:00', '2018-03-03 07:55:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('179', '1', '2', '85', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('180', '1', '2', '86', '2018-01-13 07:13:00', '2018-03-03 06:15:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('181', '1', '2', '87', '2018-01-13 07:13:00', '2018-03-03 07:55:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('182', '1', '2', '88', '2018-01-13 07:13:00', '2018-03-03 07:55:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('183', '1', '2', '89', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('184', '1', '2', '90', '2018-01-13 07:13:00', '2018-03-03 07:55:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('185', '0', '2', '91', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('186', '0', '2', '92', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('187', '0', '2', '93', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('188', '0', '2', '94', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('189', '0', '3', '1', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('190', '0', '3', '2', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('191', '0', '3', '3', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('192', '0', '3', '4', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('193', '0', '3', '5', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('194', '0', '3', '6', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('195', '0', '3', '7', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('196', '0', '3', '8', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('197', '0', '3', '9', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('198', '0', '3', '10', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('199', '0', '3', '11', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('200', '0', '3', '12', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('201', '0', '3', '13', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('202', '0', '3', '14', '2018-01-13 07:13:00', '2018-03-15 11:32:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('203', '0', '3', '15', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('204', '0', '3', '16', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('205', '0', '3', '17', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('206', '1', '3', '18', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('207', '1', '3', '19', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('208', '1', '3', '20', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('209', '1', '3', '21', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('210', '0', '3', '22', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('211', '0', '3', '23', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('212', '0', '3', '24', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('213', '0', '3', '25', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('214', '1', '3', '26', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('215', '1', '3', '27', '2018-01-13 07:13:00', '2018-03-03 07:56:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('216', '1', '3', '28', '2018-01-13 07:13:00', '2018-03-03 07:56:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('217', '1', '3', '29', '2018-01-13 07:13:00', '2018-03-03 07:56:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('218', '1', '3', '30', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('219', '1', '3', '31', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('220', '1', '3', '32', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('221', '1', '3', '33', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('222', '0', '3', '34', '2018-01-13 07:13:00', '2018-03-01 13:23:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('223', '0', '3', '35', '2018-01-13 07:13:00', '2018-03-03 07:56:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('224', '0', '3', '36', '2018-01-13 07:13:00', '2018-03-03 07:56:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('225', '0', '3', '37', '2018-01-13 07:13:00', '2018-03-03 07:56:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('226', '1', '3', '38', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('227', '1', '3', '39', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('228', '1', '3', '40', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('229', '1', '3', '41', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('230', '1', '3', '42', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('231', '1', '3', '43', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('232', '1', '3', '44', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('233', '1', '3', '45', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('234', '1', '3', '46', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('235', '1', '3', '47', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('236', '1', '3', '48', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('237', '0', '3', '49', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('238', '0', '3', '50', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('239', '0', '3', '51', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('240', '0', '3', '52', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('241', '1', '3', '53', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('242', '1', '3', '54', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('243', '1', '3', '55', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('244', '1', '3', '56', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('245', '1', '3', '57', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('246', '1', '3', '58', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('247', '1', '3', '59', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('248', '1', '3', '60', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('249', '1', '3', '61', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('250', '1', '3', '62', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('251', '1', '3', '63', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('252', '1', '3', '64', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('253', '1', '3', '65', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('254', '1', '3', '66', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('255', '1', '3', '67', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('256', '1', '3', '68', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('257', '1', '3', '69', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('258', '1', '3', '70', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('259', '1', '3', '71', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('260', '1', '3', '72', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('261', '1', '3', '73', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('262', '1', '3', '74', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('263', '1', '3', '75', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('264', '1', '3', '76', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('265', '1', '3', '77', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('266', '1', '3', '78', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('267', '1', '3', '79', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('268', '0', '3', '80', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('269', '1', '3', '81', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('270', '1', '3', '82', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('271', '1', '3', '83', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('272', '1', '3', '84', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('273', '1', '3', '85', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('274', '1', '3', '86', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('275', '1', '3', '87', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('276', '1', '3', '88', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('277', '1', '3', '89', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('278', '1', '3', '90', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('279', '0', '3', '91', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('280', '0', '3', '92', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('281', '0', '3', '93', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('282', '0', '3', '94', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('283', '0', '4', '1', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('284', '0', '4', '2', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('285', '0', '4', '3', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('286', '0', '4', '4', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('287', '0', '4', '5', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('288', '0', '4', '6', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('289', '0', '4', '7', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('290', '0', '4', '8', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('291', '0', '4', '9', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('292', '0', '4', '10', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('293', '0', '4', '11', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('294', '0', '4', '12', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('295', '0', '4', '13', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('296', '0', '4', '14', '2018-01-13 07:13:00', '2018-03-15 11:35:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('297', '0', '4', '15', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('298', '0', '4', '16', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('299', '0', '4', '17', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('300', '1', '4', '18', '2018-01-13 07:13:00', '2018-03-16 06:24:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('301', '0', '4', '19', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('302', '0', '4', '20', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('303', '0', '4', '21', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('304', '0', '4', '22', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('305', '0', '4', '23', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('306', '0', '4', '24', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('307', '0', '4', '25', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('308', '1', '4', '26', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('309', '1', '4', '27', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('310', '1', '4', '28', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('311', '1', '4', '29', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('312', '1', '4', '30', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('313', '1', '4', '31', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('314', '1', '4', '32', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('315', '1', '4', '33', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('316', '0', '4', '34', '2018-01-13 07:13:00', '2018-01-18 08:21:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('317', '0', '4', '35', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('318', '0', '4', '36', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('319', '0', '4', '37', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('320', '1', '4', '38', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('321', '1', '4', '39', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('322', '1', '4', '40', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('323', '1', '4', '41', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('324', '1', '4', '42', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('325', '1', '4', '43', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('326', '1', '4', '44', '2018-01-13 07:13:00', '2018-03-16 06:24:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('327', '1', '4', '45', '2018-01-13 07:13:00', '2018-03-16 06:24:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('328', '1', '4', '46', '2018-01-13 07:13:00', '2018-03-16 06:24:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('329', '1', '4', '47', '2018-01-13 07:13:00', '2018-03-16 06:24:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('330', '1', '4', '48', '2018-01-13 07:13:00', '2018-03-16 06:24:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('331', '0', '4', '49', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('332', '0', '4', '50', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('333', '0', '4', '51', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('334', '0', '4', '52', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('335', '0', '4', '53', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('336', '0', '4', '54', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('337', '0', '4', '55', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('338', '0', '4', '56', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('339', '1', '4', '57', '2018-01-13 07:13:00', '2018-03-16 06:24:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('340', '1', '4', '58', '2018-01-13 07:13:00', '2018-03-16 06:24:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('341', '1', '4', '59', '2018-01-13 07:13:00', '2018-03-16 06:24:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('342', '1', '4', '60', '2018-01-13 07:13:00', '2018-03-16 06:24:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('343', '1', '4', '61', '2018-01-13 07:13:00', '2018-03-16 06:24:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('344', '1', '4', '62', '2018-01-13 07:13:00', '2018-03-16 06:24:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('345', '1', '4', '63', '2018-01-13 07:13:00', '2018-03-16 06:24:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('346', '1', '4', '64', '2018-01-13 07:13:00', '2018-03-16 06:24:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('347', '1', '4', '65', '2018-01-13 07:13:00', '2018-03-16 06:24:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('348', '1', '4', '66', '2018-01-13 07:13:00', '2018-03-16 06:24:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('349', '1', '4', '67', '2018-01-13 07:13:00', '2018-03-16 06:24:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('350', '1', '4', '68', '2018-01-13 07:13:00', '2018-03-16 06:24:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('351', '1', '4', '69', '2018-01-13 07:13:00', '2018-03-16 06:24:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('352', '1', '4', '70', '2018-01-13 07:13:00', '2018-03-16 06:24:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('353', '1', '4', '71', '2018-01-13 07:13:00', '2018-03-16 06:24:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('354', '1', '4', '72', '2018-01-13 07:13:00', '2018-03-16 06:24:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('355', '1', '4', '73', '2018-01-13 07:13:00', '2018-03-16 06:24:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('356', '1', '4', '74', '2018-01-13 07:13:00', '2018-03-16 06:24:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('357', '1', '4', '75', '2018-01-13 07:13:00', '2018-03-16 06:24:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('358', '1', '4', '76', '2018-01-13 07:13:00', '2018-03-16 06:24:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('359', '1', '4', '77', '2018-01-13 07:13:00', '2018-03-16 06:24:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('360', '1', '4', '78', '2018-01-13 07:13:00', '2018-03-16 06:24:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('361', '1', '4', '79', '2018-01-13 07:13:00', '2018-03-16 06:24:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('362', '0', '4', '80', '2018-01-13 07:13:00', '2018-03-16 06:24:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('363', '1', '4', '81', '2018-01-13 07:13:00', '2018-03-16 06:24:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('364', '1', '4', '82', '2018-01-13 07:13:00', '2018-03-16 06:24:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('365', '1', '4', '83', '2018-01-13 07:13:00', '2018-03-16 06:24:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('366', '1', '4', '84', '2018-01-13 07:13:00', '2018-03-16 06:24:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('367', '1', '4', '85', '2018-01-13 07:13:00', '2018-03-16 06:24:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('368', '1', '4', '86', '2018-01-13 07:13:00', '2018-03-16 06:24:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('369', '1', '4', '87', '2018-01-13 07:13:00', '2018-03-16 06:24:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('370', '1', '4', '88', '2018-01-13 07:13:00', '2018-03-16 06:24:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('371', '1', '4', '89', '2018-01-13 07:13:00', '2018-03-16 06:24:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('372', '1', '4', '90', '2018-01-13 07:13:00', '2018-03-16 06:24:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('373', '0', '4', '91', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('374', '0', '4', '92', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('375', '0', '4', '93', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('376', '0', '4', '94', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('377', '0', '5', '1', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('378', '0', '5', '2', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('379', '0', '5', '3', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('380', '0', '5', '4', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('381', '0', '5', '5', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('382', '0', '5', '6', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('383', '0', '5', '7', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('384', '0', '5', '8', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('385', '0', '5', '9', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('386', '0', '5', '10', '2018-01-13 07:13:00', '2018-03-08 08:44:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('387', '0', '5', '11', '2018-01-13 07:13:00', '2018-03-08 08:44:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('388', '0', '5', '12', '2018-01-13 07:13:00', '2018-03-08 08:44:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('389', '0', '5', '13', '2018-01-13 07:13:00', '2018-03-08 08:44:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('390', '0', '5', '14', '2018-01-13 07:13:00', '2018-03-15 11:18:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('391', '0', '5', '15', '2018-01-13 07:13:00', '2018-03-08 08:44:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('392', '0', '5', '16', '2018-01-13 07:13:00', '2018-03-08 08:44:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('393', '0', '5', '17', '2018-01-13 07:13:00', '2018-03-08 08:44:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('394', '1', '5', '18', '2018-01-13 07:13:00', '2018-03-08 08:43:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('395', '0', '5', '19', '2018-01-13 07:13:00', '2018-03-08 08:44:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('396', '0', '5', '20', '2018-01-13 07:13:00', '2018-03-08 08:44:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('397', '0', '5', '21', '2018-01-13 07:13:00', '2018-03-08 08:44:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('398', '0', '5', '22', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('399', '0', '5', '23', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('400', '0', '5', '24', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('401', '0', '5', '25', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('402', '1', '5', '26', '2018-01-13 07:13:00', '2018-03-08 08:43:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('403', '1', '5', '27', '2018-01-13 07:13:00', '2018-03-08 08:43:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('404', '1', '5', '28', '2018-01-13 07:13:00', '2018-03-08 08:43:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('405', '1', '5', '29', '2018-01-13 07:13:00', '2018-03-08 08:43:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('406', '1', '5', '30', '2018-01-13 07:13:00', '2018-03-08 08:43:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('407', '1', '5', '31', '2018-01-13 07:13:00', '2018-03-08 08:43:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('408', '1', '5', '32', '2018-01-13 07:13:00', '2018-03-08 08:43:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('409', '1', '5', '33', '2018-01-13 07:13:00', '2018-03-08 08:43:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('410', '0', '5', '34', '2018-01-13 07:13:00', '2018-03-08 08:44:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('411', '0', '5', '35', '2018-01-13 07:13:00', '2018-03-08 08:44:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('412', '0', '5', '36', '2018-01-13 07:13:00', '2018-03-08 08:44:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('413', '0', '5', '37', '2018-01-13 07:13:00', '2018-03-08 08:44:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('414', '1', '5', '38', '2018-01-13 07:13:00', '2018-03-08 08:43:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('415', '1', '5', '39', '2018-01-13 07:13:00', '2018-03-08 08:43:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('416', '1', '5', '40', '2018-01-13 07:13:00', '2018-03-08 08:43:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('417', '1', '5', '41', '2018-01-13 07:13:00', '2018-03-08 08:43:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('418', '1', '5', '42', '2018-01-13 07:13:00', '2018-03-08 08:43:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('419', '1', '5', '43', '2018-01-13 07:13:00', '2018-03-08 08:43:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('420', '1', '5', '44', '2018-01-13 07:13:00', '2018-03-08 08:43:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('421', '1', '5', '45', '2018-01-13 07:13:00', '2018-03-08 08:43:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('422', '1', '5', '46', '2018-01-13 07:13:00', '2018-03-08 08:43:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('423', '1', '5', '47', '2018-01-13 07:13:00', '2018-03-08 08:43:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('424', '1', '5', '48', '2018-01-13 07:13:00', '2018-03-08 08:43:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('425', '0', '5', '49', '2018-01-13 07:13:00', '2018-03-08 08:44:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('426', '0', '5', '50', '2018-01-13 07:13:00', '2018-03-08 08:44:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('427', '0', '5', '51', '2018-01-13 07:13:00', '2018-03-08 08:44:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('428', '0', '5', '52', '2018-01-13 07:13:00', '2018-03-08 08:44:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('429', '0', '5', '53', '2018-01-13 07:13:00', '2018-03-08 08:44:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('430', '0', '5', '54', '2018-01-13 07:13:00', '2018-03-08 08:44:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('431', '0', '5', '55', '2018-01-13 07:13:00', '2018-03-08 08:44:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('432', '0', '5', '56', '2018-01-13 07:13:00', '2018-03-08 08:44:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('433', '1', '5', '57', '2018-01-13 07:13:00', '2018-03-08 08:43:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('434', '1', '5', '58', '2018-01-13 07:13:00', '2018-03-08 08:43:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('435', '1', '5', '59', '2018-01-13 07:13:00', '2018-03-08 08:43:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('436', '1', '5', '60', '2018-01-13 07:13:00', '2018-03-08 08:43:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('437', '1', '5', '61', '2018-01-13 07:13:00', '2018-03-08 08:43:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('438', '1', '5', '62', '2018-01-13 07:13:00', '2018-03-08 08:43:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('439', '1', '5', '63', '2018-01-13 07:13:00', '2018-03-08 08:43:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('440', '1', '5', '64', '2018-01-13 07:13:00', '2018-03-08 08:43:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('441', '1', '5', '65', '2018-01-13 07:13:00', '2018-03-08 08:43:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('442', '1', '5', '66', '2018-01-13 07:13:00', '2018-03-08 08:43:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('443', '1', '5', '67', '2018-01-13 07:13:00', '2018-03-08 08:43:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('444', '1', '5', '68', '2018-01-13 07:13:00', '2018-03-08 08:43:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('445', '1', '5', '69', '2018-01-13 07:13:00', '2018-03-08 08:43:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('446', '1', '5', '70', '2018-01-13 07:13:00', '2018-03-08 08:43:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('447', '1', '5', '71', '2018-01-13 07:13:00', '2018-03-08 08:43:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('448', '1', '5', '72', '2018-01-13 07:13:00', '2018-03-08 08:43:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('449', '1', '5', '73', '2018-01-13 07:13:00', '2018-03-08 08:43:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('450', '1', '5', '74', '2018-01-13 07:13:00', '2018-03-08 08:43:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('451', '1', '5', '75', '2018-01-13 07:13:00', '2018-03-08 08:43:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('452', '1', '5', '76', '2018-01-13 07:13:00', '2018-03-08 08:43:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('453', '1', '5', '77', '2018-01-13 07:13:00', '2018-03-08 08:43:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('454', '1', '5', '78', '2018-01-13 07:13:00', '2018-03-08 08:43:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('455', '1', '5', '79', '2018-01-13 07:13:00', '2018-03-08 08:43:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('456', '0', '5', '80', '2018-01-13 07:13:00', '2018-03-29 12:41:27', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('457', '1', '5', '81', '2018-01-13 07:13:00', '2018-03-08 08:43:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('458', '1', '5', '82', '2018-01-13 07:13:00', '2018-03-08 08:43:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('459', '1', '5', '83', '2018-01-13 07:13:00', '2018-03-08 08:43:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('460', '1', '5', '84', '2018-01-13 07:13:00', '2018-03-08 08:43:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('461', '1', '5', '85', '2018-01-13 07:13:00', '2018-03-08 08:43:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('462', '1', '5', '86', '2018-01-13 07:13:00', '2018-03-08 08:43:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('463', '1', '5', '87', '2018-01-13 07:13:00', '2018-03-08 08:43:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('464', '1', '5', '88', '2018-01-13 07:13:00', '2018-03-08 08:43:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('465', '1', '5', '89', '2018-01-13 07:13:00', '2018-03-08 08:43:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('466', '1', '5', '90', '2018-01-13 07:13:00', '2018-03-08 08:43:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('467', '0', '5', '91', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('468', '0', '5', '92', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('469', '0', '5', '93', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('470', '0', '5', '94', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('471', '0', '6', '1', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('472', '0', '6', '2', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('473', '0', '6', '3', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('474', '0', '6', '4', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('475', '0', '6', '5', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('476', '0', '6', '6', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('477', '0', '6', '7', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('478', '0', '6', '8', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('479', '0', '6', '9', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('480', '0', '6', '10', '2018-01-13 07:13:00', '2018-02-26 11:15:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('481', '0', '6', '11', '2018-01-13 07:13:00', '2018-02-26 11:15:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('482', '0', '6', '12', '2018-01-13 07:13:00', '2018-02-26 11:15:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('483', '0', '6', '13', '2018-01-13 07:13:00', '2018-02-26 11:15:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('484', '0', '6', '14', '2018-01-13 07:13:00', '2018-03-15 11:34:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('485', '0', '6', '15', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('486', '0', '6', '16', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('487', '0', '6', '17', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('488', '1', '6', '18', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('489', '0', '6', '19', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('490', '0', '6', '20', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('491', '0', '6', '21', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('492', '0', '6', '22', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('493', '0', '6', '23', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('494', '0', '6', '24', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('495', '0', '6', '25', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('496', '1', '6', '26', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('497', '1', '6', '27', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('498', '1', '6', '28', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('499', '1', '6', '29', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('500', '1', '6', '30', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('501', '1', '6', '31', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('502', '1', '6', '32', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('503', '1', '6', '33', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('504', '0', '6', '34', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('505', '0', '6', '35', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('506', '0', '6', '36', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('507', '0', '6', '37', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('508', '1', '6', '38', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('509', '1', '6', '39', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('510', '1', '6', '40', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('511', '1', '6', '41', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('512', '1', '6', '42', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('513', '1', '6', '43', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('514', '1', '6', '44', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('515', '1', '6', '45', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('516', '1', '6', '46', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('517', '1', '6', '47', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('518', '1', '6', '48', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('519', '0', '6', '49', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('520', '0', '6', '50', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('521', '0', '6', '51', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('522', '0', '6', '52', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('523', '1', '6', '53', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('524', '1', '6', '54', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('525', '1', '6', '55', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('526', '1', '6', '56', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('527', '1', '6', '57', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('528', '1', '6', '58', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('529', '1', '6', '59', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('530', '1', '6', '60', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('531', '1', '6', '61', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('532', '1', '6', '62', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('533', '1', '6', '63', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('534', '1', '6', '64', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('535', '1', '6', '65', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('536', '1', '6', '66', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('537', '1', '6', '67', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('538', '1', '6', '68', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('539', '1', '6', '69', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('540', '1', '6', '70', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('541', '1', '6', '71', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('542', '1', '6', '72', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('543', '1', '6', '73', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('544', '1', '6', '74', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('545', '1', '6', '75', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('546', '1', '6', '76', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('547', '1', '6', '77', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('548', '1', '6', '78', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('549', '1', '6', '79', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('550', '1', '6', '80', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('551', '1', '6', '81', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('552', '1', '6', '82', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('553', '1', '6', '83', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('554', '1', '6', '84', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('555', '1', '6', '85', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('556', '1', '6', '86', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('557', '1', '6', '87', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('558', '1', '6', '88', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('559', '1', '6', '89', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('560', '1', '6', '90', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('561', '0', '6', '91', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('562', '0', '6', '92', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('563', '0', '6', '93', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('564', '0', '6', '94', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('565', '0', '7', '1', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('566', '0', '7', '2', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('567', '0', '7', '3', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('568', '0', '7', '4', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('569', '0', '7', '5', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('570', '0', '7', '6', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('571', '0', '7', '7', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('572', '0', '7', '8', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('573', '0', '7', '9', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('574', '0', '7', '10', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('575', '0', '7', '11', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('576', '0', '7', '12', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('577', '0', '7', '13', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('578', '0', '7', '14', '2018-01-13 07:13:00', '2018-03-15 11:34:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('579', '0', '7', '15', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('580', '0', '7', '16', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('581', '0', '7', '17', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('582', '1', '7', '18', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('583', '0', '7', '19', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('584', '0', '7', '20', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('585', '0', '7', '21', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('586', '0', '7', '22', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('587', '0', '7', '23', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('588', '0', '7', '24', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('589', '0', '7', '25', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('590', '1', '7', '26', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('591', '1', '7', '27', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('592', '1', '7', '28', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('593', '1', '7', '29', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('594', '1', '7', '30', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('595', '1', '7', '31', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('596', '1', '7', '32', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('597', '1', '7', '33', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('598', '0', '7', '34', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('599', '0', '7', '35', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('600', '0', '7', '36', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('601', '0', '7', '37', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('602', '1', '7', '38', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('603', '1', '7', '39', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('604', '1', '7', '40', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('605', '1', '7', '41', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('606', '1', '7', '42', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('607', '1', '7', '43', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('608', '1', '7', '44', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('609', '1', '7', '45', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('610', '1', '7', '46', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('611', '1', '7', '47', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('612', '1', '7', '48', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('613', '0', '7', '49', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('614', '0', '7', '50', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('615', '0', '7', '51', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('616', '0', '7', '52', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('617', '1', '7', '53', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('618', '1', '7', '54', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('619', '1', '7', '55', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('620', '1', '7', '56', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('621', '1', '7', '57', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('622', '1', '7', '58', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('623', '1', '7', '59', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('624', '1', '7', '60', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('625', '1', '7', '61', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('626', '1', '7', '62', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('627', '1', '7', '63', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('628', '1', '7', '64', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('629', '1', '7', '65', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('630', '1', '7', '66', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('631', '1', '7', '67', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('632', '1', '7', '68', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('633', '1', '7', '69', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('634', '1', '7', '70', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('635', '1', '7', '71', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('636', '1', '7', '72', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('637', '1', '7', '73', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('638', '1', '7', '74', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('639', '1', '7', '75', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('640', '1', '7', '76', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('641', '1', '7', '77', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('642', '1', '7', '78', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('643', '1', '7', '79', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('644', '0', '7', '80', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('645', '1', '7', '81', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('646', '1', '7', '82', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('647', '1', '7', '83', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('648', '1', '7', '84', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('649', '1', '7', '85', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('650', '1', '7', '86', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('651', '1', '7', '87', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('652', '1', '7', '88', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('653', '1', '7', '89', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('654', '1', '7', '90', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('655', '0', '7', '91', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('656', '0', '7', '92', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('657', '0', '7', '93', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');
INSERT INTO `k8s_auth_server`.`role_privilege_new` (`id`, `status`, `role_id`, `pid`, `create_time`, `update_time`, `reserve1`, `reserve2`) VALUES ('658', '0', '7', '94', '2018-01-13 07:13:00', '2018-01-13 07:13:00', '', '');


/*
Navicat MySQL Data Transfer

Source Server         : 10.10.124.46
Source Server Version : 50635
Source Host           : 10.10.124.46:30306
Source Database       : k8s_auth_server

Target Server Type    : MYSQL
Target Server Version : 50635
File Encoding         : 65001

Date: 2018-04-09 19:46:54
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for role_privilege_new_replication
-- ----------------------------
DROP TABLE IF EXISTS `role_privilege_new_replication`;
CREATE TABLE `role_privilege_new_replication` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `status` tinyint(1) DEFAULT NULL COMMENT '状态',
  `role_id` int(11) DEFAULT NULL,
  `pid` int(11) DEFAULT NULL COMMENT '权限表中的id',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `reserve1` varchar(255) DEFAULT NULL COMMENT '预留字段',
  `reserve2` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `role_privilege` (`role_id`,`pid`) USING BTREE,
  KEY `角色id` (`role_id`,`status`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=659 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of role_privilege_new_replication
-- ----------------------------
INSERT INTO `role_privilege_new_replication` VALUES ('1', '1', '1', '1', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('2', '1', '1', '2', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('3', '1', '1', '3', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('4', '1', '1', '4', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('5', '1', '1', '5', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('6', '1', '1', '6', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('7', '1', '1', '7', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('8', '1', '1', '8', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('9', '1', '1', '9', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('10', '1', '1', '10', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('11', '1', '1', '11', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('12', '1', '1', '12', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('13', '1', '1', '13', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('14', '1', '1', '14', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('15', '1', '1', '15', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('16', '1', '1', '16', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('17', '1', '1', '17', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('18', '1', '1', '18', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('19', '1', '1', '19', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('20', '1', '1', '20', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('21', '1', '1', '21', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('22', '1', '1', '22', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('23', '1', '1', '23', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('24', '1', '1', '24', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('25', '1', '1', '25', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('26', '1', '1', '26', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('27', '1', '1', '27', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('28', '1', '1', '28', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('29', '1', '1', '29', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('30', '1', '1', '30', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('31', '1', '1', '31', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('32', '1', '1', '32', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('33', '1', '1', '33', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('34', '1', '1', '34', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('35', '1', '1', '35', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('36', '1', '1', '36', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('37', '1', '1', '37', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('38', '1', '1', '38', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('39', '1', '1', '39', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('40', '1', '1', '40', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('41', '1', '1', '41', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('42', '1', '1', '42', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('43', '1', '1', '43', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('44', '1', '1', '44', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('45', '1', '1', '45', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('46', '1', '1', '46', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('47', '1', '1', '47', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('48', '1', '1', '48', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('49', '1', '1', '49', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('50', '1', '1', '50', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('51', '1', '1', '51', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('52', '1', '1', '52', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('53', '1', '1', '53', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('54', '1', '1', '54', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('55', '1', '1', '55', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('56', '1', '1', '56', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('57', '1', '1', '57', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('58', '1', '1', '58', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('59', '1', '1', '59', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('60', '1', '1', '60', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('61', '1', '1', '61', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('62', '1', '1', '62', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('63', '1', '1', '63', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('64', '1', '1', '64', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('65', '1', '1', '65', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('66', '1', '1', '66', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('67', '1', '1', '67', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('68', '1', '1', '68', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('69', '1', '1', '69', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('70', '1', '1', '70', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('71', '1', '1', '71', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('72', '1', '1', '72', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('73', '1', '1', '73', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('74', '1', '1', '74', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('75', '1', '1', '75', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('76', '1', '1', '76', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('77', '1', '1', '77', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('78', '1', '1', '78', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('79', '1', '1', '79', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('80', '1', '1', '80', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('81', '1', '1', '81', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('82', '1', '1', '82', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('83', '1', '1', '83', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('84', '1', '1', '84', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('85', '1', '1', '85', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('86', '1', '1', '86', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('87', '1', '1', '87', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('88', '1', '1', '88', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('89', '1', '1', '89', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('90', '1', '1', '90', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('91', '1', '1', '91', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('92', '1', '1', '92', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('93', '1', '1', '93', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('94', '1', '1', '94', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('95', '0', '2', '1', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('96', '0', '2', '2', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('97', '0', '2', '3', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('98', '0', '2', '4', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('99', '0', '2', '5', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('100', '0', '2', '6', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('101', '0', '2', '7', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('102', '0', '2', '8', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('103', '0', '2', '9', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('104', '0', '2', '10', '2018-01-13 07:13:00', '2018-03-05 03:07:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('105', '0', '2', '11', '2018-01-13 07:13:00', '2018-03-05 03:07:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('106', '0', '2', '12', '2018-01-13 07:13:00', '2018-03-05 03:07:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('107', '0', '2', '13', '2018-01-13 07:13:00', '2018-03-05 03:07:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('108', '1', '2', '14', '2018-01-13 07:13:00', '2018-03-05 03:07:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('109', '1', '2', '15', '2018-01-13 07:13:00', '2018-03-05 03:07:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('110', '1', '2', '16', '2018-01-13 07:13:00', '2018-03-05 03:07:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('111', '1', '2', '17', '2018-01-13 07:13:00', '2018-03-05 03:07:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('112', '1', '2', '18', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('113', '1', '2', '19', '2018-01-13 07:13:00', '2018-03-03 07:55:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('114', '1', '2', '20', '2018-01-13 07:13:00', '2018-03-03 07:55:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('115', '1', '2', '21', '2018-01-13 07:13:00', '2018-03-03 07:55:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('116', '0', '2', '22', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('117', '0', '2', '23', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('118', '0', '2', '24', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('119', '0', '2', '25', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('120', '1', '2', '26', '2018-01-13 07:13:00', '2018-03-05 03:04:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('121', '1', '2', '27', '2018-01-13 07:13:00', '2018-03-05 03:04:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('122', '1', '2', '28', '2018-01-13 07:13:00', '2018-03-05 03:04:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('123', '1', '2', '29', '2018-01-13 07:13:00', '2018-03-05 03:04:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('124', '1', '2', '30', '2018-01-13 07:13:00', '2018-03-05 03:04:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('125', '1', '2', '31', '2018-01-13 07:13:00', '2018-03-05 03:04:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('126', '1', '2', '32', '2018-01-13 07:13:00', '2018-03-05 03:04:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('127', '1', '2', '33', '2018-01-13 07:13:00', '2018-03-05 03:04:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('128', '1', '2', '34', '2018-01-13 07:13:00', '2018-03-05 03:04:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('129', '1', '2', '35', '2018-01-13 07:13:00', '2018-03-05 03:04:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('130', '1', '2', '36', '2018-01-13 07:13:00', '2018-03-05 03:04:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('131', '1', '2', '37', '2018-01-13 07:13:00', '2018-03-05 03:04:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('132', '1', '2', '38', '2018-01-13 07:13:00', '2018-03-05 03:04:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('133', '1', '2', '39', '2018-01-13 07:13:00', '2018-03-05 03:04:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('134', '1', '2', '40', '2018-01-13 07:13:00', '2018-03-05 03:04:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('135', '1', '2', '41', '2018-01-13 07:13:00', '2018-03-05 03:04:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('136', '1', '2', '42', '2018-01-13 07:13:00', '2018-03-05 03:04:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('137', '1', '2', '43', '2018-01-13 07:13:00', '2018-03-05 03:04:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('138', '1', '2', '44', '2018-01-13 07:13:00', '2018-03-08 07:55:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('139', '1', '2', '45', '2018-01-13 07:13:00', '2018-03-08 07:55:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('140', '1', '2', '46', '2018-01-13 07:13:00', '2018-03-08 07:55:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('141', '1', '2', '47', '2018-01-13 07:13:00', '2018-03-08 07:55:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('142', '1', '2', '48', '2018-01-13 07:13:00', '2018-03-08 07:55:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('143', '0', '2', '49', '2018-01-13 07:13:00', '2018-03-08 08:26:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('144', '0', '2', '50', '2018-01-13 07:13:00', '2018-03-08 08:26:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('145', '0', '2', '51', '2018-01-13 07:13:00', '2018-03-08 08:26:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('146', '0', '2', '52', '2018-01-13 07:13:00', '2018-03-08 08:26:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('147', '1', '2', '53', '2018-01-13 07:13:00', '2018-03-08 07:55:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('148', '1', '2', '54', '2018-01-13 07:13:00', '2018-03-08 07:55:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('149', '1', '2', '55', '2018-01-13 07:13:00', '2018-03-08 07:55:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('150', '1', '2', '56', '2018-01-13 07:13:00', '2018-03-08 07:55:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('151', '1', '2', '57', '2018-01-13 07:13:00', '2018-03-08 07:55:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('152', '1', '2', '58', '2018-01-13 07:13:00', '2018-03-08 07:55:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('153', '1', '2', '59', '2018-01-13 07:13:00', '2018-03-08 07:55:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('154', '1', '2', '60', '2018-01-13 07:13:00', '2018-03-08 07:55:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('155', '1', '2', '61', '2018-01-13 07:13:00', '2018-03-08 07:55:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('156', '1', '2', '62', '2018-01-13 07:13:00', '2018-03-08 07:55:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('157', '1', '2', '63', '2018-01-13 07:13:00', '2018-03-08 07:55:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('158', '1', '2', '64', '2018-01-13 07:13:00', '2018-03-08 07:55:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('159', '1', '2', '65', '2018-01-13 07:13:00', '2018-03-08 07:54:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('160', '1', '2', '66', '2018-01-13 07:13:00', '2018-03-08 07:54:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('161', '1', '2', '67', '2018-01-13 07:13:00', '2018-03-08 07:54:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('162', '1', '2', '68', '2018-01-13 07:13:00', '2018-03-08 07:54:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('163', '1', '2', '69', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('164', '1', '2', '70', '2018-01-13 07:13:00', '2018-03-03 07:55:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('165', '1', '2', '71', '2018-01-13 07:13:00', '2018-03-03 07:55:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('166', '1', '2', '72', '2018-01-13 07:13:00', '2018-03-03 07:55:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('167', '1', '2', '73', '2018-01-13 07:13:00', '2018-03-03 07:55:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('168', '1', '2', '74', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('169', '1', '2', '75', '2018-01-13 07:13:00', '2018-03-03 07:55:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('170', '1', '2', '76', '2018-01-13 07:13:00', '2018-03-03 07:55:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('171', '1', '2', '77', '2018-01-13 07:13:00', '2018-03-03 07:55:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('172', '1', '2', '78', '2018-01-13 07:13:00', '2018-03-03 07:55:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('173', '1', '2', '79', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('174', '0', '2', '80', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('175', '1', '2', '81', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('176', '1', '2', '82', '2018-01-13 07:13:00', '2018-03-03 07:55:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('177', '1', '2', '83', '2018-01-13 07:13:00', '2018-03-03 07:55:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('178', '1', '2', '84', '2018-01-13 07:13:00', '2018-03-03 07:55:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('179', '1', '2', '85', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('180', '1', '2', '86', '2018-01-13 07:13:00', '2018-03-03 06:15:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('181', '1', '2', '87', '2018-01-13 07:13:00', '2018-03-03 07:55:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('182', '1', '2', '88', '2018-01-13 07:13:00', '2018-03-03 07:55:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('183', '1', '2', '89', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('184', '1', '2', '90', '2018-01-13 07:13:00', '2018-03-03 07:55:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('185', '0', '2', '91', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('186', '0', '2', '92', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('187', '0', '2', '93', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('188', '0', '2', '94', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('189', '0', '3', '1', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('190', '0', '3', '2', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('191', '0', '3', '3', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('192', '0', '3', '4', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('193', '0', '3', '5', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('194', '0', '3', '6', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('195', '0', '3', '7', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('196', '0', '3', '8', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('197', '0', '3', '9', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('198', '0', '3', '10', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('199', '0', '3', '11', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('200', '0', '3', '12', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('201', '0', '3', '13', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('202', '0', '3', '14', '2018-01-13 07:13:00', '2018-03-15 11:32:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('203', '0', '3', '15', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('204', '0', '3', '16', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('205', '0', '3', '17', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('206', '1', '3', '18', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('207', '1', '3', '19', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('208', '1', '3', '20', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('209', '1', '3', '21', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('210', '0', '3', '22', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('211', '0', '3', '23', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('212', '0', '3', '24', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('213', '0', '3', '25', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('214', '1', '3', '26', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('215', '1', '3', '27', '2018-01-13 07:13:00', '2018-03-03 07:56:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('216', '1', '3', '28', '2018-01-13 07:13:00', '2018-03-03 07:56:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('217', '1', '3', '29', '2018-01-13 07:13:00', '2018-03-03 07:56:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('218', '1', '3', '30', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('219', '1', '3', '31', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('220', '1', '3', '32', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('221', '1', '3', '33', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('222', '0', '3', '34', '2018-01-13 07:13:00', '2018-03-01 13:23:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('223', '0', '3', '35', '2018-01-13 07:13:00', '2018-03-03 07:56:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('224', '0', '3', '36', '2018-01-13 07:13:00', '2018-03-03 07:56:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('225', '0', '3', '37', '2018-01-13 07:13:00', '2018-03-03 07:56:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('226', '1', '3', '38', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('227', '1', '3', '39', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('228', '1', '3', '40', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('229', '1', '3', '41', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('230', '1', '3', '42', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('231', '1', '3', '43', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('232', '1', '3', '44', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('233', '1', '3', '45', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('234', '1', '3', '46', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('235', '1', '3', '47', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('236', '1', '3', '48', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('237', '0', '3', '49', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('238', '0', '3', '50', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('239', '0', '3', '51', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('240', '0', '3', '52', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('241', '1', '3', '53', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('242', '1', '3', '54', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('243', '1', '3', '55', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('244', '1', '3', '56', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('245', '1', '3', '57', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('246', '1', '3', '58', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('247', '1', '3', '59', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('248', '1', '3', '60', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('249', '1', '3', '61', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('250', '1', '3', '62', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('251', '1', '3', '63', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('252', '1', '3', '64', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('253', '1', '3', '65', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('254', '1', '3', '66', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('255', '1', '3', '67', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('256', '1', '3', '68', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('257', '1', '3', '69', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('258', '1', '3', '70', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('259', '1', '3', '71', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('260', '1', '3', '72', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('261', '1', '3', '73', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('262', '1', '3', '74', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('263', '1', '3', '75', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('264', '1', '3', '76', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('265', '1', '3', '77', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('266', '1', '3', '78', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('267', '1', '3', '79', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('268', '0', '3', '80', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('269', '1', '3', '81', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('270', '1', '3', '82', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('271', '1', '3', '83', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('272', '1', '3', '84', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('273', '1', '3', '85', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('274', '1', '3', '86', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('275', '1', '3', '87', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('276', '1', '3', '88', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('277', '1', '3', '89', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('278', '1', '3', '90', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('279', '0', '3', '91', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('280', '0', '3', '92', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('281', '0', '3', '93', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('282', '0', '3', '94', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('283', '0', '4', '1', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('284', '0', '4', '2', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('285', '0', '4', '3', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('286', '0', '4', '4', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('287', '0', '4', '5', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('288', '0', '4', '6', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('289', '0', '4', '7', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('290', '0', '4', '8', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('291', '0', '4', '9', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('292', '0', '4', '10', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('293', '0', '4', '11', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('294', '0', '4', '12', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('295', '0', '4', '13', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('296', '0', '4', '14', '2018-01-13 07:13:00', '2018-03-15 11:35:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('297', '0', '4', '15', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('298', '0', '4', '16', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('299', '0', '4', '17', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('300', '1', '4', '18', '2018-01-13 07:13:00', '2018-03-16 06:24:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('301', '0', '4', '19', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('302', '0', '4', '20', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('303', '0', '4', '21', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('304', '0', '4', '22', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('305', '0', '4', '23', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('306', '0', '4', '24', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('307', '0', '4', '25', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('308', '1', '4', '26', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('309', '1', '4', '27', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('310', '1', '4', '28', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('311', '1', '4', '29', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('312', '1', '4', '30', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('313', '1', '4', '31', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('314', '1', '4', '32', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('315', '1', '4', '33', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('316', '0', '4', '34', '2018-01-13 07:13:00', '2018-01-18 08:21:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('317', '0', '4', '35', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('318', '0', '4', '36', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('319', '0', '4', '37', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('320', '1', '4', '38', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('321', '1', '4', '39', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('322', '1', '4', '40', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('323', '1', '4', '41', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('324', '1', '4', '42', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('325', '1', '4', '43', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('326', '1', '4', '44', '2018-01-13 07:13:00', '2018-03-16 06:24:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('327', '1', '4', '45', '2018-01-13 07:13:00', '2018-03-16 06:24:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('328', '1', '4', '46', '2018-01-13 07:13:00', '2018-03-16 06:24:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('329', '1', '4', '47', '2018-01-13 07:13:00', '2018-03-16 06:24:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('330', '1', '4', '48', '2018-01-13 07:13:00', '2018-03-16 06:24:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('331', '0', '4', '49', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('332', '0', '4', '50', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('333', '0', '4', '51', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('334', '0', '4', '52', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('335', '0', '4', '53', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('336', '0', '4', '54', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('337', '0', '4', '55', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('338', '0', '4', '56', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('339', '1', '4', '57', '2018-01-13 07:13:00', '2018-03-16 06:24:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('340', '1', '4', '58', '2018-01-13 07:13:00', '2018-03-16 06:24:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('341', '1', '4', '59', '2018-01-13 07:13:00', '2018-03-16 06:24:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('342', '1', '4', '60', '2018-01-13 07:13:00', '2018-03-16 06:24:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('343', '1', '4', '61', '2018-01-13 07:13:00', '2018-03-16 06:24:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('344', '1', '4', '62', '2018-01-13 07:13:00', '2018-03-16 06:24:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('345', '1', '4', '63', '2018-01-13 07:13:00', '2018-03-16 06:24:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('346', '1', '4', '64', '2018-01-13 07:13:00', '2018-03-16 06:24:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('347', '1', '4', '65', '2018-01-13 07:13:00', '2018-03-16 06:24:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('348', '1', '4', '66', '2018-01-13 07:13:00', '2018-03-16 06:24:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('349', '1', '4', '67', '2018-01-13 07:13:00', '2018-03-16 06:24:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('350', '1', '4', '68', '2018-01-13 07:13:00', '2018-03-16 06:24:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('351', '1', '4', '69', '2018-01-13 07:13:00', '2018-03-16 06:24:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('352', '1', '4', '70', '2018-01-13 07:13:00', '2018-03-16 06:24:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('353', '1', '4', '71', '2018-01-13 07:13:00', '2018-03-16 06:24:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('354', '1', '4', '72', '2018-01-13 07:13:00', '2018-03-16 06:24:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('355', '1', '4', '73', '2018-01-13 07:13:00', '2018-03-16 06:24:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('356', '1', '4', '74', '2018-01-13 07:13:00', '2018-03-16 06:24:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('357', '1', '4', '75', '2018-01-13 07:13:00', '2018-03-16 06:24:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('358', '1', '4', '76', '2018-01-13 07:13:00', '2018-03-16 06:24:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('359', '1', '4', '77', '2018-01-13 07:13:00', '2018-03-16 06:24:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('360', '1', '4', '78', '2018-01-13 07:13:00', '2018-03-16 06:24:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('361', '1', '4', '79', '2018-01-13 07:13:00', '2018-03-16 06:24:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('362', '0', '4', '80', '2018-01-13 07:13:00', '2018-03-16 06:24:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('363', '1', '4', '81', '2018-01-13 07:13:00', '2018-03-16 06:24:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('364', '1', '4', '82', '2018-01-13 07:13:00', '2018-03-16 06:24:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('365', '1', '4', '83', '2018-01-13 07:13:00', '2018-03-16 06:24:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('366', '1', '4', '84', '2018-01-13 07:13:00', '2018-03-16 06:24:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('367', '1', '4', '85', '2018-01-13 07:13:00', '2018-03-16 06:24:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('368', '1', '4', '86', '2018-01-13 07:13:00', '2018-03-16 06:24:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('369', '1', '4', '87', '2018-01-13 07:13:00', '2018-03-16 06:24:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('370', '1', '4', '88', '2018-01-13 07:13:00', '2018-03-16 06:24:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('371', '1', '4', '89', '2018-01-13 07:13:00', '2018-03-16 06:24:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('372', '1', '4', '90', '2018-01-13 07:13:00', '2018-03-16 06:24:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('373', '0', '4', '91', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('374', '0', '4', '92', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('375', '0', '4', '93', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('376', '0', '4', '94', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('377', '0', '5', '1', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('378', '0', '5', '2', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('379', '0', '5', '3', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('380', '0', '5', '4', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('381', '0', '5', '5', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('382', '0', '5', '6', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('383', '0', '5', '7', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('384', '0', '5', '8', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('385', '0', '5', '9', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('386', '0', '5', '10', '2018-01-13 07:13:00', '2018-03-08 08:44:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('387', '0', '5', '11', '2018-01-13 07:13:00', '2018-03-08 08:44:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('388', '0', '5', '12', '2018-01-13 07:13:00', '2018-03-08 08:44:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('389', '0', '5', '13', '2018-01-13 07:13:00', '2018-03-08 08:44:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('390', '0', '5', '14', '2018-01-13 07:13:00', '2018-03-15 11:18:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('391', '0', '5', '15', '2018-01-13 07:13:00', '2018-03-08 08:44:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('392', '0', '5', '16', '2018-01-13 07:13:00', '2018-03-08 08:44:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('393', '0', '5', '17', '2018-01-13 07:13:00', '2018-03-08 08:44:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('394', '1', '5', '18', '2018-01-13 07:13:00', '2018-03-08 08:43:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('395', '0', '5', '19', '2018-01-13 07:13:00', '2018-03-08 08:44:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('396', '0', '5', '20', '2018-01-13 07:13:00', '2018-03-08 08:44:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('397', '0', '5', '21', '2018-01-13 07:13:00', '2018-03-08 08:44:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('398', '0', '5', '22', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('399', '0', '5', '23', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('400', '0', '5', '24', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('401', '0', '5', '25', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('402', '1', '5', '26', '2018-01-13 07:13:00', '2018-03-08 08:43:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('403', '1', '5', '27', '2018-01-13 07:13:00', '2018-03-08 08:43:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('404', '1', '5', '28', '2018-01-13 07:13:00', '2018-03-08 08:43:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('405', '1', '5', '29', '2018-01-13 07:13:00', '2018-03-08 08:43:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('406', '1', '5', '30', '2018-01-13 07:13:00', '2018-03-08 08:43:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('407', '1', '5', '31', '2018-01-13 07:13:00', '2018-03-08 08:43:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('408', '1', '5', '32', '2018-01-13 07:13:00', '2018-03-08 08:43:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('409', '1', '5', '33', '2018-01-13 07:13:00', '2018-03-08 08:43:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('410', '0', '5', '34', '2018-01-13 07:13:00', '2018-03-08 08:44:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('411', '0', '5', '35', '2018-01-13 07:13:00', '2018-03-08 08:44:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('412', '0', '5', '36', '2018-01-13 07:13:00', '2018-03-08 08:44:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('413', '0', '5', '37', '2018-01-13 07:13:00', '2018-03-08 08:44:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('414', '1', '5', '38', '2018-01-13 07:13:00', '2018-03-08 08:43:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('415', '1', '5', '39', '2018-01-13 07:13:00', '2018-03-08 08:43:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('416', '1', '5', '40', '2018-01-13 07:13:00', '2018-03-08 08:43:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('417', '1', '5', '41', '2018-01-13 07:13:00', '2018-03-08 08:43:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('418', '1', '5', '42', '2018-01-13 07:13:00', '2018-03-08 08:43:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('419', '1', '5', '43', '2018-01-13 07:13:00', '2018-03-08 08:43:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('420', '1', '5', '44', '2018-01-13 07:13:00', '2018-03-08 08:43:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('421', '1', '5', '45', '2018-01-13 07:13:00', '2018-03-08 08:43:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('422', '1', '5', '46', '2018-01-13 07:13:00', '2018-03-08 08:43:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('423', '1', '5', '47', '2018-01-13 07:13:00', '2018-03-08 08:43:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('424', '1', '5', '48', '2018-01-13 07:13:00', '2018-03-08 08:43:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('425', '0', '5', '49', '2018-01-13 07:13:00', '2018-03-08 08:44:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('426', '0', '5', '50', '2018-01-13 07:13:00', '2018-03-08 08:44:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('427', '0', '5', '51', '2018-01-13 07:13:00', '2018-03-08 08:44:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('428', '0', '5', '52', '2018-01-13 07:13:00', '2018-03-08 08:44:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('429', '0', '5', '53', '2018-01-13 07:13:00', '2018-03-08 08:44:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('430', '0', '5', '54', '2018-01-13 07:13:00', '2018-03-08 08:44:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('431', '0', '5', '55', '2018-01-13 07:13:00', '2018-03-08 08:44:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('432', '0', '5', '56', '2018-01-13 07:13:00', '2018-03-08 08:44:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('433', '1', '5', '57', '2018-01-13 07:13:00', '2018-03-08 08:43:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('434', '1', '5', '58', '2018-01-13 07:13:00', '2018-03-08 08:43:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('435', '1', '5', '59', '2018-01-13 07:13:00', '2018-03-08 08:43:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('436', '1', '5', '60', '2018-01-13 07:13:00', '2018-03-08 08:43:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('437', '1', '5', '61', '2018-01-13 07:13:00', '2018-03-08 08:43:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('438', '1', '5', '62', '2018-01-13 07:13:00', '2018-03-08 08:43:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('439', '1', '5', '63', '2018-01-13 07:13:00', '2018-03-08 08:43:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('440', '1', '5', '64', '2018-01-13 07:13:00', '2018-03-08 08:43:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('441', '1', '5', '65', '2018-01-13 07:13:00', '2018-03-08 08:43:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('442', '1', '5', '66', '2018-01-13 07:13:00', '2018-03-08 08:43:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('443', '1', '5', '67', '2018-01-13 07:13:00', '2018-03-08 08:43:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('444', '1', '5', '68', '2018-01-13 07:13:00', '2018-03-08 08:43:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('445', '1', '5', '69', '2018-01-13 07:13:00', '2018-03-08 08:43:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('446', '1', '5', '70', '2018-01-13 07:13:00', '2018-03-08 08:43:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('447', '1', '5', '71', '2018-01-13 07:13:00', '2018-03-08 08:43:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('448', '1', '5', '72', '2018-01-13 07:13:00', '2018-03-08 08:43:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('449', '1', '5', '73', '2018-01-13 07:13:00', '2018-03-08 08:43:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('450', '1', '5', '74', '2018-01-13 07:13:00', '2018-03-08 08:43:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('451', '1', '5', '75', '2018-01-13 07:13:00', '2018-03-08 08:43:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('452', '1', '5', '76', '2018-01-13 07:13:00', '2018-03-08 08:43:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('453', '1', '5', '77', '2018-01-13 07:13:00', '2018-03-08 08:43:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('454', '1', '5', '78', '2018-01-13 07:13:00', '2018-03-08 08:43:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('455', '1', '5', '79', '2018-01-13 07:13:00', '2018-03-08 08:43:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('456', '0', '5', '80', '2018-01-13 07:13:00', '2018-03-08 08:43:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('457', '1', '5', '81', '2018-01-13 07:13:00', '2018-03-08 08:43:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('458', '1', '5', '82', '2018-01-13 07:13:00', '2018-03-08 08:43:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('459', '1', '5', '83', '2018-01-13 07:13:00', '2018-03-08 08:43:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('460', '1', '5', '84', '2018-01-13 07:13:00', '2018-03-08 08:43:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('461', '1', '5', '85', '2018-01-13 07:13:00', '2018-03-08 08:43:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('462', '1', '5', '86', '2018-01-13 07:13:00', '2018-03-08 08:43:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('463', '1', '5', '87', '2018-01-13 07:13:00', '2018-03-08 08:43:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('464', '1', '5', '88', '2018-01-13 07:13:00', '2018-03-08 08:43:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('465', '1', '5', '89', '2018-01-13 07:13:00', '2018-03-08 08:43:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('466', '1', '5', '90', '2018-01-13 07:13:00', '2018-03-08 08:43:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('467', '0', '5', '91', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('468', '0', '5', '92', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('469', '0', '5', '93', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('470', '0', '5', '94', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('471', '0', '6', '1', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('472', '0', '6', '2', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('473', '0', '6', '3', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('474', '0', '6', '4', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('475', '0', '6', '5', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('476', '0', '6', '6', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('477', '0', '6', '7', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('478', '0', '6', '8', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('479', '0', '6', '9', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('480', '0', '6', '10', '2018-01-13 07:13:00', '2018-02-26 11:15:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('481', '0', '6', '11', '2018-01-13 07:13:00', '2018-02-26 11:15:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('482', '0', '6', '12', '2018-01-13 07:13:00', '2018-02-26 11:15:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('483', '0', '6', '13', '2018-01-13 07:13:00', '2018-02-26 11:15:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('484', '0', '6', '14', '2018-01-13 07:13:00', '2018-03-15 11:34:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('485', '0', '6', '15', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('486', '0', '6', '16', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('487', '0', '6', '17', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('488', '1', '6', '18', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('489', '0', '6', '19', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('490', '0', '6', '20', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('491', '0', '6', '21', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('492', '0', '6', '22', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('493', '0', '6', '23', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('494', '0', '6', '24', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('495', '0', '6', '25', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('496', '1', '6', '26', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('497', '1', '6', '27', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('498', '1', '6', '28', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('499', '1', '6', '29', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('500', '1', '6', '30', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('501', '1', '6', '31', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('502', '1', '6', '32', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('503', '1', '6', '33', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('504', '0', '6', '34', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('505', '0', '6', '35', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('506', '0', '6', '36', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('507', '0', '6', '37', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('508', '1', '6', '38', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('509', '1', '6', '39', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('510', '1', '6', '40', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('511', '1', '6', '41', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('512', '1', '6', '42', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('513', '1', '6', '43', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('514', '1', '6', '44', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('515', '1', '6', '45', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('516', '1', '6', '46', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('517', '1', '6', '47', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('518', '1', '6', '48', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('519', '0', '6', '49', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('520', '0', '6', '50', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('521', '0', '6', '51', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('522', '0', '6', '52', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('523', '1', '6', '53', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('524', '1', '6', '54', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('525', '1', '6', '55', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('526', '1', '6', '56', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('527', '1', '6', '57', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('528', '1', '6', '58', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('529', '1', '6', '59', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('530', '1', '6', '60', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('531', '1', '6', '61', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('532', '1', '6', '62', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('533', '1', '6', '63', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('534', '1', '6', '64', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('535', '1', '6', '65', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('536', '1', '6', '66', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('537', '1', '6', '67', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('538', '1', '6', '68', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('539', '1', '6', '69', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('540', '1', '6', '70', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('541', '1', '6', '71', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('542', '1', '6', '72', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('543', '1', '6', '73', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('544', '1', '6', '74', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('545', '1', '6', '75', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('546', '1', '6', '76', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('547', '1', '6', '77', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('548', '1', '6', '78', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('549', '1', '6', '79', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('550', '1', '6', '80', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('551', '1', '6', '81', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('552', '1', '6', '82', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('553', '1', '6', '83', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('554', '1', '6', '84', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('555', '1', '6', '85', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('556', '1', '6', '86', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('557', '1', '6', '87', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('558', '1', '6', '88', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('559', '1', '6', '89', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('560', '1', '6', '90', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('561', '0', '6', '91', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('562', '0', '6', '92', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('563', '0', '6', '93', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('564', '0', '6', '94', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('565', '0', '7', '1', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('566', '0', '7', '2', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('567', '0', '7', '3', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('568', '0', '7', '4', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('569', '0', '7', '5', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('570', '0', '7', '6', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('571', '0', '7', '7', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('572', '0', '7', '8', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('573', '0', '7', '9', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('574', '0', '7', '10', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('575', '0', '7', '11', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('576', '0', '7', '12', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('577', '0', '7', '13', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('578', '0', '7', '14', '2018-01-13 07:13:00', '2018-03-15 11:34:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('579', '0', '7', '15', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('580', '0', '7', '16', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('581', '0', '7', '17', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('582', '1', '7', '18', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('583', '0', '7', '19', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('584', '0', '7', '20', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('585', '0', '7', '21', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('586', '0', '7', '22', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('587', '0', '7', '23', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('588', '0', '7', '24', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('589', '0', '7', '25', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('590', '1', '7', '26', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('591', '1', '7', '27', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('592', '1', '7', '28', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('593', '1', '7', '29', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('594', '1', '7', '30', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('595', '1', '7', '31', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('596', '1', '7', '32', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('597', '1', '7', '33', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('598', '0', '7', '34', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('599', '0', '7', '35', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('600', '0', '7', '36', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('601', '0', '7', '37', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('602', '1', '7', '38', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('603', '1', '7', '39', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('604', '1', '7', '40', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('605', '1', '7', '41', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('606', '1', '7', '42', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('607', '1', '7', '43', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('608', '1', '7', '44', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('609', '1', '7', '45', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('610', '1', '7', '46', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('611', '1', '7', '47', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('612', '1', '7', '48', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('613', '0', '7', '49', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('614', '0', '7', '50', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('615', '0', '7', '51', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('616', '0', '7', '52', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('617', '1', '7', '53', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('618', '1', '7', '54', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('619', '1', '7', '55', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('620', '1', '7', '56', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('621', '1', '7', '57', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('622', '1', '7', '58', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('623', '1', '7', '59', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('624', '1', '7', '60', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('625', '1', '7', '61', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('626', '1', '7', '62', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('627', '1', '7', '63', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('628', '1', '7', '64', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('629', '1', '7', '65', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('630', '1', '7', '66', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('631', '1', '7', '67', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('632', '1', '7', '68', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('633', '1', '7', '69', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('634', '1', '7', '70', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('635', '1', '7', '71', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('636', '1', '7', '72', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('637', '1', '7', '73', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('638', '1', '7', '74', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('639', '1', '7', '75', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('640', '1', '7', '76', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('641', '1', '7', '77', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('642', '1', '7', '78', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('643', '1', '7', '79', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('644', '0', '7', '80', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('645', '1', '7', '81', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('646', '1', '7', '82', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('647', '1', '7', '83', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('648', '1', '7', '84', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('649', '1', '7', '85', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('650', '1', '7', '86', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('651', '1', '7', '87', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('652', '1', '7', '88', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('653', '1', '7', '89', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('654', '1', '7', '90', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('655', '0', '7', '91', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('656', '0', '7', '92', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('657', '0', '7', '93', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);
INSERT INTO `role_privilege_new_replication` VALUES ('658', '0', '7', '94', '2018-01-13 07:13:00', '2018-01-13 07:13:00', null, null);


-- ----------------------------
-- Table structure for service_templates
-- ----------------------------
DROP TABLE IF EXISTS `service_templates`;
CREATE TABLE `service_templates` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) DEFAULT NULL,
  `tag` varchar(45) DEFAULT NULL,
  `details` varchar(256) DEFAULT NULL,
  `deployment_content` longtext,
  `image_list` varchar(512) DEFAULT NULL,
  `is_public` tinyint(4) DEFAULT '0',
  `ingress_content` text,
  `status` int(1) DEFAULT NULL,
  `tenant` varchar(45) DEFAULT NULL,
  `create_user` varchar(45) DEFAULT NULL,
  `create_time` timestamp NULL DEFAULT NULL,
  `flag` int(1) DEFAULT '0',
  `node_selector` varchar(225) DEFAULT NULL,
  `project_id` varchar(64) DEFAULT NULL,
  `cluster_id` varchar(64) DEFAULT NULL COMMENT '集群Id',
  PRIMARY KEY (`id`),
  KEY `project_cluster` (`project_id`,`cluster_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of service_templates
-- ----------------------------
INSERT INTO `service_templates` VALUES ('1', 'tomcat', '1.0', '', '[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[],\"command\":[],\"configmap\":[],\"env\":[{\"key\":\"JAVA_OPT\",\"value\":\"-Xmx512m\"},{\"key\":\"TZ\",\"value\":\"Asia/Shanghai\"}],\"img\":\"onlineshop/tomcat\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"tomcat\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"8080\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"500m\",\"memory\":\"512\"},\"storage\":[],\"tag\":\"v8.0\"}],\"hostName\":\"\",\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"tomcat\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]', 'onlineshop/tomcat', '0', '', '1', 'all', 'admin', '2017-08-11 14:18:36', '0', 'HarmonyCloud_Status=C', '', null);
INSERT INTO `service_templates` VALUES ('2', 'redis', '1.0', '', '[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[],\"command\":[],\"configmap\":[],\"env\":[{\"key\":\"TZ\",\"value\":\"Asia/Shanghai\"}],\"img\":\"onlineshop/redis\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"redis\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"6379\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"100m\",\"memory\":\"128\"},\"storage\":[],\"tag\":\"3.2-alpine\"}],\"hostName\":\"\",\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"redis\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]', 'onlineshop/redis', '0', '', '1', 'all', 'admin', '2017-08-11 14:44:14', '0', 'HarmonyCloud_Status=C', null, null);
INSERT INTO `service_templates` VALUES ('3', 'wordpress', '1.0', '', '[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[],\"command\":[],\"configmap\":[],\"env\":[{\"key\":\"TZ\",\"value\":\"Asia/Shanghai\"}],\"img\":\"onlineshop/wordpress\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"wordpress\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"80\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"500m\",\"memory\":\"512\"},\"storage\":[],\"tag\":\"4.8.0-php7.1-fpm-alpine\"}],\"hostName\":\"\",\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"wordpress\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]', 'onlineshop/wordpress', '0', '', '1', 'all', 'admin', '2017-08-11 15:31:54', '0', 'HarmonyCloud_Status=C', null, null);
INSERT INTO `service_templates` VALUES ('4', 'influxdb', '1.0', '', '[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[],\"command\":[],\"configmap\":[],\"env\":[{\"key\":\"TZ\",\"value\":\"Asia/Shanghai\"}],\"img\":\"onlineshop/influxdb\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"influxdb2\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"8083\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"8086\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"500m\",\"memory\":\"512\"},\"storage\":[],\"tag\":\"1.3.0-alpine\"}],\"hostName\":\"\",\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"influxdb2\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]', 'onlineshop/influxdb', '0', '', '1', 'all', 'admin', '2017-08-11 17:30:21', '0', 'HarmonyCloud_Status=C', null, null);
INSERT INTO `service_templates` VALUES ('5', 'mysql', '1.0', '', '[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[],\"command\":[],\"configmap\":[],\"env\":[{\"key\":\"MYSQL_ROOT_PASSWORD\",\"value\":\"123456\"},{\"key\":\"TZ\",\"value\":\"Asia/Shanghai\"}],\"img\":\"onlineshop/mysql\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"mysql\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"1433\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"1000m\",\"memory\":\"1024\"},\"storage\":[],\"tag\":\"5.7.6\"}],\"hostName\":\"\",\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"mysql\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]', 'onlineshop/mysqls', '0', '', '1', 'all', 'admin', '2017-08-11 18:03:10', '0', 'HarmonyCloud_Status=C', null, null);
INSERT INTO `service_templates` VALUES ('6', 'webhook', '1.0', '', '[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[],\"command\":[],\"configmap\":[],\"env\":[{\"key\":\"TZ\",\"value\":\"Asia/Shanghai\"}],\"img\":\"onlineshop/webhook\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"webhook\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"1433\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"1000m\",\"memory\":\"1024\"},\"storage\":[],\"tag\":\"2.6.5\"}],\"hostName\":\"\",\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"webhook\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]', 'onlineshop/webhook', '0', '', '1', 'all', 'admin', '2017-08-12 15:54:52', '0', 'HarmonyCloud_Status=C', null, null);
INSERT INTO `service_templates` VALUES ('7', 'mongodb', '1.0', '', '[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[],\"command\":[],\"configmap\":[],\"env\":[{\"key\":\"TZ\",\"value\":\"Asia/Shanghai\"}],\"imagePullPolicy\":\"Always\",\"img\":\"onlineshop/mongodb\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"mongodb\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"27017\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"100m\",\"memory\":\"128\"},\"securityContext\":{\"add\":[],\"drop\":[],\"privileged\":false,\"security\":false},\"storage\":[],\"tag\":\"v3.5\"}],\"hostIPC\":false,\"hostName\":\"\",\"hostPID\":false,\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"mongodb\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]', 'onlineshop/mongodb', '0', '', '1', 'all', 'admin', '2017-09-07 19:10:27', '0', 'HarmonyCloud_Status=C', null, null);
INSERT INTO `service_templates` VALUES ('8', 'rabbitmq', '1.0', '', '[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[],\"command\":[],\"configmap\":[],\"env\":[{\"key\":\"TZ\",\"value\":\"Asia/Shanghai\"}],\"imagePullPolicy\":\"Always\",\"img\":\"onlineshop/rabbitmq\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"rabbitmq\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"4369\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"5672\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"5671\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"15672\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"25672\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"500m\",\"memory\":\"512\"},\"securityContext\":{\"add\":[],\"drop\":[],\"privileged\":false,\"security\":false},\"storage\":[],\"tag\":\"3.6.11\"}],\"hostIPC\":false,\"hostName\":\"\",\"hostPID\":false,\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"rabbitmq\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]', 'onlineshop/rabbitmq', '0', '', '1', 'all', 'admin', '2017-09-09 10:44:14', '0', 'HarmonyCloud_Status=C', null, null);
INSERT INTO `service_templates` VALUES ('9', 'nginx', '1.0', '', '[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[],\"command\":[],\"configmap\":[],\"env\":[{\"key\":\"TZ\",\"value\":\"Asia/Shanghai\"}],\"imagePullPolicy\":\"Always\",\"img\":\"onlineshop/nginx\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"nginxcon\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"80\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"100m\",\"memory\":\"128\"},\"securityContext\":{\"add\":[],\"drop\":[],\"privileged\":false,\"security\":false},\"storage\":[],\"tag\":\"latest\"}],\"hostIPC\":false,\"hostName\":\"\",\"hostPID\":false,\"instance\":\"2\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"nginxsvc\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]', 'onlineshop/nginx', '0', '', '1', 'all', 'admin', '2017-09-06 15:26:07', '0', 'HarmonyCloud_Status=C', null, null);
INSERT INTO `service_templates` VALUES ('10', 'websphere', '1.0', '', '[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[],\"command\":[],\"configmap\":[],\"env\":[{\"key\":\"TZ\",\"value\":\"Asia/Shanghai\"}],\"img\":\"onlineshop/websphere-traditional\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"websphere\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"9043\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"9080\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"1000m\",\"memory\":\"1024\"},\"securityContext\":{\"add\":[],\"drop\":[],\"privileged\":false,\"security\":false},\"storage\":[],\"tag\":\"8.5.5.9-install\"}],\"hostIPC\":false,\"hostName\":\"\",\"hostPID\":false,\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"websphere\",\"namespace\":\"garydemo-garyns\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]', 'onlineshop/websphere-traditional', '0', '', '1', 'all', 'admin', '2017-09-06 10:01:33', '0', 'HarmonyCloud_Status=C', null, null);
INSERT INTO `service_templates` VALUES ('11', 'elasticsearch', '1.0', '', '[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[],\"command\":[],\"configmap\":[],\"env\":[{\"key\":\"JAVA_OPTS\",\"value\":\"-Xmx2048m\"},{\"key\":\"TZ\",\"value\":\"Asia/Shanghai\"}],\"imagePullPolicy\":\"Always\",\"img\":\"onlineshop/elasticsearch\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"elasticsearch\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"9200\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"9300\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"1000m\",\"memory\":\"2048\"},\"securityContext\":{\"add\":[],\"drop\":[],\"privileged\":false,\"security\":false},\"storage\":[],\"tag\":\"v2.4.1-1\"}],\"hostIPC\":false,\"hostName\":\"\",\"hostPID\":false,\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"elasticsearch\",\"namespace\":\"garydemo-garyns\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]', 'onlineshop/elasticsearch', '0', '', '1', 'all', 'admin', '2017-09-07 19:25:49', '0', 'HarmonyCloud_Status=C', null, null);
INSERT INTO `service_templates` VALUES ('12', 'redis-master', '1.0', '', '[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[],\"command\":[],\"configmap\":[],\"env\":[{\"key\":\"TZ\",\"value\":\"Asia/Shanghai\"}],\"img\":\"onlineshop/redis-master\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"redis-master\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"6379\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"500m\",\"memory\":\"512\"},\"storage\":[],\"tag\":\"latest-v2\"}],\"hostName\":\"\",\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"redis-master\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]', 'onlineshop/redis-master', '0', '', '1', 'all', 'admin', '2018-05-08 10:44:14', '0', 'HarmonyCloud_Status=C', null, null);
INSERT INTO `service_templates` VALUES ('13', 'vp0', '1.0', '', '[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[\"-c\",\"sleep 10; peer node start\"],\"command\":[\"sh\"],\"configmap\":[],\"env\":[{\"key\":\"CORE_PEER_ID\",\"name\":\"\",\"value\":\"vp0\"},{\"key\":\"CORE_SECURITY_ENROLLID\",\"name\":\"\",\"value\":\"test_vp0\"},{\"key\":\"CORE_SECURITY_ENROLLSECRET\",\"name\":\"\",\"value\":\"MwYpmSRjupbT\"},{\"key\":\"CORE_PEER_ADDRESSAUTODETECT\",\"name\":\"\",\"value\":\"true\"},{\"key\":\"CORE_PEER_NETWORKID\",\"name\":\"\",\"value\":\"dev\"},{\"key\":\"CORE_LOGGING_LEVEL\",\"name\":\"\",\"value\":\"info\"},{\"key\":\"CORE_PEER_VALIDATOR_CONSENSUS_PLUGIN\",\"name\":\"\",\"value\":\"pbft\"},{\"key\":\"CORE_PBFT_GENERAL_N\",\"name\":\"\",\"value\":\"6\"},{\"key\":\"CORE_PBFT_GENERAL_TIMEOUT_REQUEST\",\"name\":\"\",\"value\":\"2s\"},{\"key\":\"CORE_SECURITY_ENABLED\",\"name\":\"\",\"value\":\"true\"},{\"key\":\"CORE_PEER_PKI_ECA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"},{\"key\":\"CORE_PEER_PKI_TCA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"},{\"key\":\"CORE_PEER_PKI_TLSCA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"},{\"key\":\"TZ\",\"name\":\"\",\"value\":\"Asia/Shanghai\"}],\"img\":\"onlineshop/fabric-peer\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"vp0\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7050\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7051\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7053\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7052\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7054\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7055\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7056\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7057\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7058\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7059\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"500m\",\"memory\":\"512\"},\"storage\":[{\"emptyDir\":\"\",\"gitUrl\":\"\",\"hostPath\":\"/var/run/docker.sock\",\"path\":\"/var/run/docker.sock\",\"pvcBindOne\":\"true\",\"pvcCapacity\":\"\",\"pvcName\":\"\",\"pvcTenantid\":\"b04c83f0c7fd4914b2e7ddf2ef309a0b\",\"readOnly\":\"false\",\"revision\":\"\",\"type\":\"hostPath\",\"volume\":\"\"}],\"tag\":\"0.6\"}],\"hostName\":\"\",\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"vp0\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]', 'onlineshop/fabric-peer', '0', '', '1', 'all', 'admin', '2017-08-18 14:44:42', '0', 'HarmonyCloud_Status=C', null, null);
INSERT INTO `service_templates` VALUES ('14', 'vp1', '1.0', '', '[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[\"-c\",\"sleep 10; peer node start\"],\"command\":[\"sh\"],\"configmap\":[],\"env\":[{\"key\":\"CORE_PEER_ID\",\"name\":\"\",\"value\":\"vp1\"},{\"key\":\"CORE_PEER_DISCOVERY_ROOTNODE\",\"name\":\"\",\"value\":\"vp0:7051\"},{\"key\":\"CORE_SECURITY_ENROLLID\",\"name\":\"\",\"value\":\"test_vp1\"},{\"key\":\"CORE_SECURITY_ENROLLSECRET\",\"name\":\"\",\"value\":\"5wgHK9qqYaPy\"},{\"key\":\"CORE_PEER_ADDRESSAUTODETECT\",\"name\":\"\",\"value\":\"true\"},{\"key\":\"CORE_PEER_NETWORKID\",\"name\":\"\",\"value\":\"dev\"},{\"key\":\"CORE_LOGGING_LEVEL\",\"name\":\"\",\"value\":\"info\"},{\"key\":\"CORE_PEER_VALIDATOR_CONSENSUS_PLUGIN\",\"name\":\"\",\"value\":\"pbft\"},{\"key\":\"CORE_PBFT_GENERAL_N\",\"name\":\"\",\"value\":\"6\"},{\"key\":\"CORE_PBFT_GENERAL_TIMEOUT_REQUEST\",\"name\":\"\",\"value\":\"2s\"},{\"key\":\"CORE_SECURITY_ENABLED\",\"name\":\"\",\"value\":\"true\"},{\"key\":\"CORE_PEER_PKI_ECA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"},{\"key\":\"CORE_PEER_PKI_TCA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"},{\"key\":\"CORE_PEER_PKI_TLSCA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"},{\"key\":\"TZ\",\"name\":\"\",\"value\":\"Asia/Shanghai\"}],\"img\":\"onlineshop/fabric-peer\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"vp1\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7050\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7051\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7052\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7053\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7054\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7055\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7056\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7057\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7058\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7059\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"500m\",\"memory\":\"512\"},\"storage\":[{\"emptyDir\":\"\",\"gitUrl\":\"\",\"hostPath\":\"/var/run/docker.sock\",\"path\":\"/var/run/docker.sock\",\"pvcBindOne\":\"true\",\"pvcCapacity\":\"\",\"pvcName\":\"\",\"pvcTenantid\":\"b04c83f0c7fd4914b2e7ddf2ef309a0b\",\"readOnly\":\"false\",\"revision\":\"\",\"type\":\"hostPath\",\"volume\":\"\"}],\"tag\":\"0.6\"}],\"hostName\":\"\",\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"vp1\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]', 'onlineshop/fabric-peer', '0', '', '1', 'all', 'admin', '2017-08-18 14:44:42', '0', 'HarmonyCloud_Status=C', null, null);
INSERT INTO `service_templates` VALUES ('15', 'vp2', '1.0', '', '[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[\"-c\",\"sleep 10; peer node start\"],\"command\":[\"sh\"],\"configmap\":[],\"env\":[{\"key\":\"CORE_PEER_ID\",\"name\":\"\",\"value\":\"vp2\"},{\"key\":\"CORE_PEER_DISCOVERY_ROOTNODE\",\"name\":\"\",\"value\":\"vp0:7051\"},{\"key\":\"CORE_SECURITY_ENROLLID\",\"name\":\"\",\"value\":\"test_vp2\"},{\"key\":\"CORE_SECURITY_ENROLLSECRET\",\"name\":\"\",\"value\":\"vQelbRvja7cJ\"},{\"key\":\"CORE_PEER_ADDRESSAUTODETECT\",\"name\":\"\",\"value\":\"true\"},{\"key\":\"CORE_PEER_NETWORKID\",\"name\":\"\",\"value\":\"dev\"},{\"key\":\"CORE_LOGGING_LEVEL\",\"name\":\"\",\"value\":\"info\"},{\"key\":\"CORE_PEER_VALIDATOR_CONSENSUS_PLUGIN\",\"name\":\"\",\"value\":\"pbft\"},{\"key\":\"CORE_PBFT_GENERAL_N\",\"name\":\"\",\"value\":\"6\"},{\"key\":\"CORE_PBFT_GENERAL_TIMEOUT_REQUEST\",\"name\":\"\",\"value\":\"2s\"},{\"key\":\"CORE_SECURITY_ENABLED\",\"name\":\"\",\"value\":\"true\"},{\"key\":\"CORE_PEER_PKI_ECA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"},{\"key\":\"CORE_PEER_PKI_TCA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"},{\"key\":\"CORE_PEER_PKI_TLSCA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"},{\"key\":\"TZ\",\"name\":\"\",\"value\":\"Asia/Shanghai\"}],\"img\":\"onlineshop/fabric-peer\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"vp2\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7050\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7051\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7052\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7053\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7054\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7055\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7056\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7057\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7058\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7059\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"500m\",\"memory\":\"512\"},\"storage\":[{\"emptyDir\":\"\",\"gitUrl\":\"\",\"hostPath\":\"/var/run/docker.sock\",\"path\":\"/var/run/docker.sock\",\"pvcBindOne\":\"true\",\"pvcCapacity\":\"\",\"pvcName\":\"\",\"pvcTenantid\":\"b04c83f0c7fd4914b2e7ddf2ef309a0b\",\"readOnly\":\"false\",\"revision\":\"\",\"type\":\"hostPath\",\"volume\":\"\"}],\"tag\":\"0.6\"}],\"hostName\":\"\",\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"vp2\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]', 'onlineshop/fabric-peer', '0', '', '1', 'all', 'admin', '2017-08-18 14:44:42', '0', 'HarmonyCloud_Status=C', null, null);
INSERT INTO `service_templates` VALUES ('16', 'vp3', '1.0', '', '[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[\"-c\",\"sleep 10; peer node start\"],\"command\":[\"sh\"],\"configmap\":[],\"env\":[{\"key\":\"CORE_PEER_ID\",\"name\":\"\",\"value\":\"vp3\"},{\"key\":\"CORE_PEER_DISCOVERY_ROOTNODE\",\"name\":\"\",\"value\":\"vp0:7051\"},{\"key\":\"CORE_SECURITY_ENROLLID\",\"name\":\"\",\"value\":\"test_vp3\"},{\"key\":\"CORE_SECURITY_ENROLLSECRET\",\"name\":\"\",\"value\":\"9LKqKH5peurL\"},{\"key\":\"CORE_PEER_ADDRESSAUTODETECT\",\"name\":\"\",\"value\":\"true\"},{\"key\":\"CORE_PEER_NETWORKID\",\"name\":\"\",\"value\":\"dev\"},{\"key\":\"CORE_LOGGING_LEVEL\",\"name\":\"\",\"value\":\"info\"},{\"key\":\"CORE_PEER_VALIDATOR_CONSENSUS_PLUGIN\",\"name\":\"\",\"value\":\"pbft\"},{\"key\":\"CORE_PBFT_GENERAL_N\",\"name\":\"\",\"value\":\"6\"},{\"key\":\"CORE_PBFT_GENERAL_TIMEOUT_REQUEST\",\"name\":\"\",\"value\":\"2s\"},{\"key\":\"CORE_SECURITY_ENABLED\",\"name\":\"\",\"value\":\"true\"},{\"key\":\"CORE_PEER_PKI_ECA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"},{\"key\":\"CORE_PEER_PKI_TCA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"},{\"key\":\"CORE_PEER_PKI_TLSCA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"},{\"key\":\"TZ\",\"name\":\"\",\"value\":\"Asia/Shanghai\"}],\"img\":\"onlineshop/fabric-peer\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"vp3\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7050\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7051\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7052\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7053\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7054\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7055\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7056\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7057\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7058\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7059\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"500m\",\"memory\":\"512\"},\"storage\":[{\"emptyDir\":\"\",\"gitUrl\":\"\",\"hostPath\":\"/var/run/docker.sock\",\"path\":\"/var/run/docker.sock\",\"pvcBindOne\":\"true\",\"pvcCapacity\":\"\",\"pvcName\":\"\",\"pvcTenantid\":\"b04c83f0c7fd4914b2e7ddf2ef309a0b\",\"readOnly\":\"false\",\"revision\":\"\",\"type\":\"hostPath\",\"volume\":\"\"}],\"tag\":\"0.6\"}],\"hostName\":\"\",\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"vp3\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]', 'onlineshop/fabric-peer', '0', '', '1', 'all', 'admin', '2017-08-18 14:44:42', '0', 'HarmonyCloud_Status=C', null, null);
INSERT INTO `service_templates` VALUES ('17', 'vp4', '1.0', '', '[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[\"-c\",\"sleep 10; peer node start\"],\"command\":[\"sh\"],\"configmap\":[],\"env\":[{\"key\":\"CORE_PEER_ID\",\"name\":\"\",\"value\":\"vp4\"},{\"key\":\"CORE_SECURITY_ENROLLID\",\"name\":\"\",\"value\":\"test_vp4\"},{\"key\":\"CORE_SECURITY_ENROLLSECRET\",\"name\":\"\",\"value\":\"Pqh90CEW5juZ\"},{\"key\":\"CORE_PEER_DISCOVERY_ROOTNODE\",\"name\":\"\",\"value\":\"vp0:7051\"},{\"key\":\"CORE_PEER_ADDRESSAUTODETECT\",\"name\":\"\",\"value\":\"true\"},{\"key\":\"CORE_PEER_NETWORKID\",\"name\":\"\",\"value\":\"dev\"},{\"key\":\"CORE_LOGGING_LEVEL\",\"name\":\"\",\"value\":\"info\"},{\"key\":\"CORE_PEER_VALIDATOR_CONSENSUS_PLUGIN\",\"name\":\"\",\"value\":\"pbft\"},{\"key\":\"CORE_PBFT_GENERAL_N\",\"name\":\"\",\"value\":\"6\"},{\"key\":\"CORE_PBFT_GENERAL_TIMEOUT_REQUEST\",\"name\":\"\",\"value\":\"2s\"},{\"key\":\"CORE_SECURITY_ENABLED\",\"name\":\"\",\"value\":\"true\"},{\"key\":\"CORE_PEER_PKI_ECA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"},{\"key\":\"CORE_PEER_PKI_TCA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"},{\"key\":\"CORE_PEER_PKI_TLSCA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"},{\"key\":\"TZ\",\"name\":\"\",\"value\":\"Asia/Shanghai\"}],\"img\":\"onlineshop/fabric-peer\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"vp4\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7050\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7051\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7052\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7053\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7054\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7055\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7056\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7057\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7058\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7059\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"500m\",\"memory\":\"512\"},\"storage\":[{\"emptyDir\":\"\",\"gitUrl\":\"\",\"hostPath\":\"/var/run/docker.sock\",\"path\":\"/var/run/docker.sock\",\"pvcBindOne\":\"true\",\"pvcCapacity\":\"\",\"pvcName\":\"\",\"pvcTenantid\":\"b04c83f0c7fd4914b2e7ddf2ef309a0b\",\"readOnly\":\"false\",\"revision\":\"\",\"type\":\"hostPath\",\"volume\":\"\"}],\"tag\":\"0.6\"}],\"hostName\":\"\",\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"vp4\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]', 'onlineshop/fabric-peer', '0', '', '1', 'all', 'admin', '2017-08-18 14:44:42', '0', 'HarmonyCloud_Status=C', null, null);
INSERT INTO `service_templates` VALUES ('18', 'vp5', '1.0', '', '[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[\"-c\",\"sleep 10; peer node start\"],\"command\":[\"sh\"],\"configmap\":[],\"env\":[{\"key\":\"CORE_PEER_PKI_ECA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"},{\"key\":\"CORE_PEER_PKI_TCA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"},{\"key\":\"CORE_PEER_PKI_TLSCA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"},{\"key\":\"CORE_PEER_ID\",\"name\":\"\",\"value\":\"vp5\"},{\"key\":\"CORE_SECURITY_ENROLLID\",\"name\":\"\",\"value\":\"test_vp5\"},{\"key\":\"CORE_PBFT_GENERAL_N\",\"name\":\"\",\"value\":\"6\"},{\"key\":\"CORE_SECURITY_ENROLLSECRET\",\"name\":\"\",\"value\":\"FfdvDkAdY81P\"},{\"key\":\"CORE_PEER_DISCOVERY_ROOTNODE\",\"name\":\"\",\"value\":\"vp0:7051\"},{\"key\":\"CORE_PEER_ADDRESSAUTODETECT\",\"name\":\"\",\"value\":\"true\"},{\"key\":\"CORE_PEER_NETWORKID\",\"name\":\"\",\"value\":\"dev\"},{\"key\":\"CORE_LOGGING_LEVEL\",\"name\":\"\",\"value\":\"info\"},{\"key\":\"CORE_PEER_VALIDATOR_CONSENSUS_PLUGIN\",\"name\":\"\",\"value\":\"pbft\"},{\"key\":\"CORE_PBFT_GENERAL_TIMEOUT_REQUEST\",\"name\":\"\",\"value\":\"2s\"},{\"key\":\"CORE_SECURITY_ENABLED\",\"name\":\"\",\"value\":\"true\"},{\"key\":\"TZ\",\"name\":\"\",\"value\":\"Asia/Shanghai\"}],\"img\":\"onlineshop/fabric-peer\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"vp5\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7050\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7051\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7053\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7052\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7054\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7055\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7056\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7057\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7058\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7059\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"500m\",\"memory\":\"512\"},\"storage\":[{\"emptyDir\":\"\",\"gitUrl\":\"\",\"hostPath\":\"/var/run/docker.sock\",\"path\":\"/var/run/docker.sock\",\"pvcBindOne\":\"true\",\"pvcCapacity\":\"\",\"pvcName\":\"\",\"pvcTenantid\":\"b04c83f0c7fd4914b2e7ddf2ef309a0b\",\"readOnly\":\"false\",\"revision\":\"\",\"type\":\"hostPath\",\"volume\":\"\"}],\"tag\":\"0.6\"}],\"hostName\":\"\",\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"vp5\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]', 'onlineshop/fabric-peer', '0', '', '1', 'all', 'admin', '2017-08-18 14:44:42', '0', 'HarmonyCloud_Status=C', null, null);
INSERT INTO `service_templates` VALUES ('19', 'membersrvc', '1.0', '', '[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[],\"command\":[\"membersrvc\"],\"configmap\":[],\"env\":[{\"key\":\"CORE_LOGGING_SERVER\",\"name\":\"\",\"value\":\"debug\"},{\"key\":\"TZ\",\"name\":\"\",\"value\":\"Asia/Shanghai\"}],\"img\":\"onlineshop/fabric-membersrvc\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"membersrvc\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7054\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"500m\",\"memory\":\"512\"},\"storage\":[{\"emptyDir\":\"\",\"gitUrl\":\"\",\"hostPath\":\"/var/run/docker.sock\",\"path\":\"/var/run/docker.sock\",\"pvcBindOne\":\"true\",\"pvcCapacity\":\"\",\"pvcName\":\"\",\"pvcTenantid\":\"b04c83f0c7fd4914b2e7ddf2ef309a0b\",\"readOnly\":\"false\",\"revision\":\"\",\"type\":\"hostPath\",\"volume\":\"\"}],\"tag\":\"0.6\"}],\"hostName\":\"\",\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"membersrvc\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]', 'onlineshop/fabric-membersrvc', '0', '', '1', 'all', 'admin', '2017-08-18 14:44:42', '0', 'HarmonyCloud_Status=C', null, null);
INSERT INTO `service_templates` VALUES ('20', 'redis-slave', '1.0', '', '[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[],\"command\":[],\"configmap\":[],\"env\":[{\"key\":\"GET_HOSTS_FROM\",\"value\":\"env\"},{\"key\":\"TZ\",\"value\":\"Asia/Shanghai\"}],\"img\":\"onlineshop/redis-slave\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"redis-slave\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"6379\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"500m\",\"memory\":\"512\"},\"storage\":[],\"tag\":\"latest-v2\"}],\"hostName\":\"\",\"instance\":\"2\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"redis-slave\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]', 'onlineshop/redis-slave', '0', '', '1', 'all', 'admin', '2018-05-08 10:44:14', '0', 'HarmonyCloud_Status=C', null, null);

-- ----------------------------
-- Table structure for system_config
-- ----------------------------
DROP TABLE IF EXISTS `system_config`;
CREATE TABLE `system_config` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `config_name` varchar(255) NOT NULL,
  `config_value` varchar(255) NOT NULL,
  `config_type` varchar(255) NOT NULL,
  `create_user` varchar(255) DEFAULT NULL,
  `update_user` varchar(255) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `config_name` (`config_name`)
) ENGINE=InnoDB AUTO_INCREMENT=45 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of system_config
-- ----------------------------

-- ----------------------------
-- Table structure for tenant_binding_new
-- ----------------------------
DROP TABLE IF EXISTS `tenant_binding_new`;
CREATE TABLE `tenant_binding_new` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '序号(对应devops的sysId)',
  `tenant_system_code` varchar(64) DEFAULT NULL COMMENT '租户编码，对应CDP项目编码（projectCode字段， 基于CDP项目简称字段，36进制生成）',
  `tenant_id` varchar(64) NOT NULL COMMENT '租户id(对应devops的projectId',
  `tenant_name` varchar(64) NOT NULL COMMENT '租户名称(对应sysName)',
  `tm_usernames` varchar(255) NOT NULL COMMENT 'tm用户名列表',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `annotation` varchar(512) DEFAULT NULL COMMENT '备注(remark)',
  `update_user_account` varchar(64) DEFAULT NULL COMMENT '修改租户的用户账号,对应CDP项目修改人（reviseUserName字段）',
  `update_user_id` varchar(64) DEFAULT NULL COMMENT '修改租户的用户Id,对应CDP项目修改人（reviseUserId字段）',
  `update_user_name` varchar(64) DEFAULT NULL COMMENT '修改租户的用户名称,对应CDP项目修改人（reviseUserName字段）',
  `create_user_account` varchar(64) DEFAULT NULL COMMENT '创建租户的用户账号,对应CDP项目修改人（reviseUserAccount字段）',
  `create_user_id` varchar(64) DEFAULT NULL COMMENT '创建租户的用户Id,对应CDP项目创建人ID（createUserId字段',
  `create_user_name` varchar(64) DEFAULT NULL COMMENT '创建租户的用户名称,对应CDP项目创建人名称（createUserName字段）',
  `alias_name` varchar(128) DEFAULT NULL COMMENT '别名',
  `reserve1` varchar(255) DEFAULT NULL COMMENT '预留字段',
  `reserve2` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `tenant_id_UNIQUE` (`tenant_id`),
  UNIQUE KEY `tenant_name_UNIQUE` (`tenant_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of tenant_binding_new
-- ----------------------------

-- ----------------------------
-- Table structure for url_dic
-- ----------------------------
DROP TABLE IF EXISTS `url_dic`;
CREATE TABLE `url_dic` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `url` varchar(255) DEFAULT NULL,
  `module` varchar(255) DEFAULT NULL,
  `resource` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_url` (`url`,`module`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of url_dic
-- ----------------------------
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('1', '/clusterstemplates', 'infrastructure', 'clustermgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('2', '/tenants/*/projects/*/apptemplates/*/tags', 'delivery', 'template');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('3', '/tenants/*/projects/*/deploys/*/rules', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('4', '/tenants/*/projects/*/cicdjobs/*/stages/stagetypes', 'cicd', 'cicdmgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('5', '/users/usersfile/export', 'system', 'system');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('6', '/tenants/*/projects/*/repositories/*/images', 'delivery', 'image');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('7', '/tenant/*/project/*/switchProject', 'whitelist', 'whitelist');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('8', '/tenants/*/projects/*/repositories/*/cleanrules', 'delivery', 'repositorymgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('9', '/tenants/*/projects/*/extservices/extsvctypes', 'appcenter', 'externalservice');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('10', '/tenants/*/projects/*/apptemplates/*/addsvctemplate', 'delivery', 'template');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('11', '/clusters/*/nodes/*/monitor', 'infrastructure', 'node');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('12', '/users/status/normal', 'system', 'system');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('13', '/tenants/*/projects/*/deploys/*/checkname', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('14', '/tenants/*/projects/*/dependence', 'cicd', 'env');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('15', '/users/*/group/searchgroup', 'system', 'system');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('16', '/clusters/*/pod/*/monitor', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('17', '/users/departments/*/status/active', 'system', 'system');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('18', '/clusters/*/daemonsets', 'appcenter', 'daemonset');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('19', '/tenants/*/projects/*/deploys/*/container/file/upload', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('20', '/users/groups/same', 'system', 'system');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('21', '/tenants/*/projects/*/repositories/*/images/*/download', 'delivery', 'image');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('22', '/tenants/*/projects/*/cicdjobs/*/result', 'cicd', 'cicdmgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('23', '/autoscale', 'appcenter', 'autoscale');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('24', '/tenants/*/projects/*/repositories/*/enable', 'delivery', 'repositorymgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('25', '/tenants/*/projects/*/deploys/*/applogs/filenames', 'log', 'applog');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('26', '/tenants/*/projects/*/dockerfile/page', 'cicd', 'cicdmgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('27', '/harbor/*/replicationpolicies/*/enable', 'delivery', 'repositorymgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('28', '/users/*/password', 'system', 'system');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('29', '/tenants/*/projects/*/cicdjobs/*/stages/*/result', 'cicd', 'cicdmgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('30', '/tenants/*/projects/*/repositories/*/images/*/tags/*/detail', 'delivery', 'image');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('31', '/roles/*/menu', 'whitelist', 'whitelist');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('32', '/tenants/*/projects/*/deploys/*/applogs', 'log', 'applog');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('33', '/users/departments/*/status/pause', 'system', 'system');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('34', '/msf/namespace/delete', 'msf', 'msf');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('35', '/roles/*/switchRole', 'whitelist', 'whitelist');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('36', '/tenants/*/projects/*/deploys/*/container/file/upload/status', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('37', '/roles/*/privilege/reset', 'system', 'system');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('38', '/tenants/*/projects/*/repositories/search', 'delivery', 'repository');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('39', '/clusters/*/nodes/online', 'infrastructure', 'node');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('40', '/tenants/*/projects/*/cicdjobs/sonarconfig', 'cicd', 'cicdmgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('41', '/localroles/conditions', 'tenant', 'projectmgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('42', '/clusters/*/nodes/*/label/available', 'infrastructure', 'node');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('43', '/tenants/*/namespace/*/pod/label', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('44', '/users/groups/searchuser', 'system', 'system');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('45', '/tenants/*/quota', 'tenant', 'basic');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('46', '/tenants/*/projects/*/svctemplates/images', 'delivery', 'template');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('47', '/tenants/*/projects/*/apptemplates/*/updatesvctemplate', 'delivery', 'template');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('48', '/msf/deleteInstances', 'msf', 'msf');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('49', '/tenants/*/projects/*/deploys/*/reversions', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('50', '/tenants/*/projects/*/configmap/latest', 'appcenter', 'configmap');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('51', '/users/groups', 'system', 'system');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('52', '/tenants/*/projects/*/deploys/*/stop', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('53', '/clusters/*/nodes/*/label/init', 'infrastructure', 'node');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('54', '/users/usersfile/import', 'system', 'system');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('55', '/tenants/*/projects/*/deploys/*/canaryupdate/cancel', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('56', '/tenants/*/projects/*/cicdjobs/*/start', 'cicd', 'cicdmgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('57', '/users/status/admin', 'system', 'system');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('58', '/users/*/email', 'system', 'system');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('59', '/clusters/*/daemonsets/*/events', 'appcenter', 'daemonset');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('60', '/openapi/app/stats', 'whitelist', 'whitelist');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('61', '/users/*/status', 'system', 'system');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('62', '/harbor/*/replicationpolicies/*/status', 'delivery', 'repositorymgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('63', '/secret/checked', 'whitelist', 'whitelist');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('64', '/tenants/*/projects/*/deploys/*/canaryUpdate', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('65', '/tenants/*/projects/*/cicdjobs/validateName', 'cicd', 'cicdmgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('66', '/tenants/*/projects/*/deploys/*/container/file/upload/history', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('67', '/tenants/*/projects/*/deploys/*/applogs/logfile/*/export', 'log', 'applog');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('68', '/tenants/*/projects/*/deploys/*/bluegreen', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('69', '/dashboard/clusters/*/monitors', 'overview', 'overview');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('70', '/dashboard/clusters/*/events', 'overview', 'overview');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('71', '/tenants/*/projects/*/deploys/*/bluegreen/switchflow', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('72', '/tenants/*/projects/*/deploys/*/routers', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('73', '/tenants/*/namespaces/*/quota', 'tenant', 'tenantmgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('74', '/tenants/*/projects/*/deploys/ingress', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('75', '/tenants/*/projects/*/deploys/*/pods', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('76', '/users/auth/logout', 'whitelist', 'whitelist');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('77', '/openapi/cicd/postBuild', 'whitelist', 'whitelist');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('78', '/tenants/*/projects/*/deploys/*/bluegreen/confirm', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('79', '/tenants/*/projects/*/cicdjobs/*/stage/*/sonarstage', 'cicd', 'cicdmgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('80', '/tenants/*/projects/*/repositories/*/clairstatics', 'delivery', 'repositorymgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('81', '/tenants/*/projects/*/deploys/*/start', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('82', '/users/*/phone', 'system', 'system');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('83', '/msf/queryNamespaces', 'whitelist', 'whitelist');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('84', '/dashboard/clusters/*/pods', 'overview', 'overview');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('85', '/openapi/cicd/preBuild', 'whitelist', 'whitelist');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('86', '/system/auditlogs/module', 'log', 'auditlog');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('87', '/harbor/*/replicationpolicies/syncimage', 'delivery', 'repositorymgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('88', '/tenants/*/projects/*/apptemplates/*/yaml', 'delivery', 'template');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('89', '/harbor/*/replicationtargets/*/replicationpolicies/detail', 'delivery', 'repositorymgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('90', '/openapi/namespace/containers', 'whitelist', 'whitelist');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('91', '/clusters/entry', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('92', '/harbor/*/replicationpolicies/partialpolicies', 'delivery', 'repositorymgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('93', '/msf/queryInstances', 'whitelist', 'whitelist');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('94', '/tenants', 'tenant', 'basic');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('95', '/users/*/type', 'system', 'system');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('96', '/tenants/*/projects/*/deploys/*/container/file/maxsize', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('97', '/clusters/*/nodes/*/event', 'infrastructure', 'node');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('98', '/tenants/*/projects/*/deploys/*/applogs/stderrlogs', 'log', 'applog');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('99', '/tenants/*/projects/*/apptemplates', 'delivery', 'template');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('100', '/tenants/*/projects/*/cicdjobs/*/parameters', 'cicd', 'cicdmgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('101', '/clusters/daemonsets', 'appcenter', 'daemonset');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('102', '/clusters/*/daemonsets/*/pods', 'appcenter', 'daemonset');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('103', '/dashboard/clusters/*/nodeLicense', 'overview', 'overview');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('104', '/tenants/*/projects/*/jobs/*/reRun', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('105', '/clusters/*/resource/usage', 'infrastructure', 'clustermgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('106', '/clusters/*/resource/info', 'infrastructure', 'clustermgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('107', '/tenants/*/projects/*/cicdjobs/*/yaml', 'cicd', 'cicdmgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('108', '/dashboard/clusters/*/infras', 'overview', 'overview');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('109', '/tenants/*/projects/*/svctemplates/*/status', 'delivery', 'template');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('110', '/clusters/clusterNodeSize', 'infrastructure', 'node');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('111', '/tenants/*/tms', 'tenant', 'basic');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('112', '/tenants/*/projects/*/deploys/*/canaryupdate/resume', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('113', '/users/departments/*/status/unauthorized', 'system', 'system');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('114', '/tenants/*/projects/*/deploys/*/ingress', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('115', '/tenants/*/networks/nettopology', 'tenant', 'tenantmgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('116', '/localroles/resourcetypes/*/rules', 'tenant', 'projectmgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('117', '/roles/privilege', 'system', 'system');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('118', '/roles/*/clusters', 'whitelist', 'whitelist');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('119', '/clusters/*/nodes/available', 'infrastructure', 'node');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('120', '/system/configs/trialtime', 'whitelist', 'whitelist');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('121', '/system/configs', 'system', 'system');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('122', '/users/auth/token', 'whitelist', 'whitelist');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('123', '/tenants/*/projects/*/deploys/ports/check', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('124', '/tenants/*/projects/*/cicdjobs/*/triggers', 'cicd', 'cicdmgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('125', '/clusters/*/components/status', 'overview', 'overview');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('126', '/tenants/*/projects/*/deploys', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('127', '/tenants/*/projects/*/deploys/*/containers', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('128', '/project/devOpsSyncUser', 'whitelist', 'whitelist');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('129', '/dashboard/clusters/*/alarms', 'overview', 'overview');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('130', '/tenants/*/projects/*/dependence/*/file', 'cicd', 'env');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('131', '/users/departments/*/status/normal', 'system', 'system');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('132', '/tenants/*/projects/*/deploys/*/linklogs/errortransactions', 'log', 'applog');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('133', '/tenants/*/projects/*/deploys/*/autoscale', 'appcenter', 'autoscale');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('134', '/tenants/*/projects/*/repositories/images/search', 'delivery', 'image');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('135', '/users/*/password/reset', 'system', 'system');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('136', '/tenants/*/projects/*/deploys/*/terminal', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('137', '/clusters/*/volumeprovider', 'appcenter', 'volume');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('138', '/dashboard/clusters/*/sum', 'overview', 'overview');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('139', '/tenants/*/namespaces', 'tenant', 'tenantmgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('140', '/tenants/*/projects/*/repositories/*/repositoryquota', 'delivery', 'repository');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('141', '/tenants/*/projects/*/svctemplates/tags', 'delivery', 'template');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('142', '/openapi/urlmapping', 'whitelist', 'whitelist');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('143', '/tenants/*/projects/*/repositories/images/first', 'delivery', 'image');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('144', '/system/auditlogs/count', 'log', 'auditlog');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('145', '/localroles', 'tenant', 'projectmgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('146', '/tenants/*/networks/removeBingSubnet', 'tenant', 'tenantmgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('147', '/tenants/projects/apptemplates', 'delivery', 'onlineshop');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('148', '/tenants/*/projects/*/cicdjobs/*/log', 'cicd', 'cicdmgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('149', '/tenants/*/projects/*/extservices', 'appcenter', 'externalservice');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('150', '/clusters', 'whitelist', 'whitelist');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('151', '/tenants/*/projects/*/svctemplates/*/deploys', 'delivery', 'template');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('152', '/tenants/*/projects/*/deploys/*/container/files', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('153', '/harbor/*/replicationtargets/ping', 'delivery', 'repositorymgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('154', '/tenants/*/projects/*/deploys/*/container/file/upload/record', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('155', '/tenants/*/projects/*/cicdjobs/*/notification', 'cicd', 'cicdmgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('156', '/harbor/server', 'delivery', 'repositorymgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('157', '/tenants/*/projects/*/cicdjobs', 'cicd', 'cicdmgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('158', '/tenants/importCdsUserAccount', 'whitelist', 'whitelist');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('159', '/tenants/*/projects/*/apps/*/deploys', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('160', '/tenants/*/projects/*/configmap/checkName', 'appcenter', 'configmap');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('161', '/harbor/*/replicationpolicies/policyjobs', 'delivery', 'repositorymgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('162', '/users/current', 'whitelist', 'whitelist');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('163', '/harbor/*/replicationpolicies/*/policyjobs', 'delivery', 'repositorymgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('164', '/tenants/*/projects/*/cicdjobs/conditionparams', 'cicd', 'cicdmgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('165', '/tenants/*/projects', 'tenant', 'tenantmgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('166', '/tenants/*/projects/*/deploys/*/updatestatus', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('167', '/users/departments/*/status/summary', 'system', 'system');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('168', '/tenants/*/projects/*/pvs', 'appcenter', 'volume');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('169', '/tenants/*/projects/*/pvs/*/recycle', 'appcenter', 'volume');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('170', '/tenants/*/projects/*/cicdjobs/*/stop', 'cicd', 'cicdmgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('171', '/clusters/count', 'overview', 'overview');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('172', '/harbor/*/replicationtargets/*/replicationpolicies', 'delivery', 'repositorymgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('173', '/roles', 'system', 'system');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('174', '/tenants/*/projects/*/jobs/*/start', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('175', '/tenants/*/projects/*/repositories/*/images/upload', 'delivery', 'image');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('176', '/tenants/*/networks/*/nettopology', 'tenant', 'tenantmgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('177', '/clusters/*/nodes', 'infrastructure', 'node');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('178', '/harbor/*/replicationtargets', 'delivery', 'repositorymgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('179', '/tenants/*/projects/*/cicdjobs/*/webhooktrigger', 'cicd', 'cicdmgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('180', '/tenants/*/projects/*/configmap', 'appcenter', 'configmap');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('181', '/tenants/*/projects/*/deploys/*/container/file/uploadToNode', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('182', '/tenants/*/projects/*/cicdjobs/*/stage/*/conditions', 'cicd', 'cicdmgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('183', '/roles/*/disable', 'system', 'system');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('184', '/msf/namespace/instances', 'msf', 'msf');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('185', '/clusters/*/events/watch', 'overview', 'overview');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('186', '/msf/namespace/deployments', 'msf', 'msf');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('187', '/clusters/*/nodes/errorstatus', 'infrastructure', 'node');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('188', '/users/status/unauthorized', 'system', 'system');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('189', '/clusters/nodes/labels', 'infrastructure', 'node');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('190', '/localroles/*/users', 'tenant', 'projectmgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('191', '/tenants/*/projects/*/deploys/*/linklogs/transactiontraces', 'log', 'applog');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('192', '/roles/*/privilege', 'system', 'system');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('193', '/tenants/*/projects/*/cicdjobs/*/images', 'cicd', 'cicdmgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('194', '/clusters/*/pod/*/container/*/monitor', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('195', '/tenants/*/projects/*/deploys/*/linklogs/erroranalysis', 'log', 'applog');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('196', '/clusters/*/monitor/process/status', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('197', '/tenants/*/projects/*/projectmember', 'tenant', 'projectmgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('198', '/tenants/*/namespaces/detail', 'tenant', 'tenantmgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('199', '/roles/*/enable', 'system', 'system');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('200', '/users/status/summary', 'system', 'system');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('201', '/tenants/*/projects/*/svctemplates/deploys', 'delivery', 'template');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('202', '/localroles/*/privilege', 'tenant', 'projectmgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('203', '/tenants/importCdsSystem', 'whitelist', 'whitelist');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('204', '/openapi/cicdjobs/stages', 'whitelist', 'whitelist');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('205', '/localroles/projects/*/users', 'tenant', 'projectmgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('206', '/users/*/detail', 'system', 'system');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('207', '/openapi/cicd/stageSync', 'whitelist', 'whitelist');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('208', '/users/*/realname', 'system', 'system');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('209', '/clusters/*/nodes/*/label', 'infrastructure', 'node');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('210', '/openapi/kubemodule/status', 'whitelist', 'whitelist');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('211', '/tenants/*/switchTenant', 'whitelist', 'whitelist');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('212', '/tenants/*/projects/*/env', 'cicd', 'env');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('213', '/clusters/*/nodes/*/label/status', 'infrastructure', 'node');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('214', '/tenants/*/projects/*/deploys/*/linklogs/pod', 'log', 'applog');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('215', '/tenants/*/projects/*/deploys/ports', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('216', '/clusters/*/events/overview', 'overview', 'overview');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('217', '/tenants/*/projects/*/jobs/*/stop', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('218', '/tenants/*/projects/*/cicdjobs/*/stages', 'cicd', 'cicdmgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('219', '/datacenters', 'infrastructure', 'clustermgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('220', '/system/configs/ldap', 'system', 'system');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('221', '/tenants/*/projects/*/deploys/*/container/file/upload/progress', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('222', '/tenants/*/projects/*/cicdjobs/validateCredential', 'cicd', 'cicdmgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('223', '/clusters/cache', 'whitelist', 'whitelist');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('224', '/msf/tenants', 'whitelist', 'whitelist');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('225', '/users/withoutgroup', 'system', 'system');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('226', '/clusters/*/nodes/*/detail', 'infrastructure', 'node');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('227', '/tenants/*/projects/*/jobs', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('228', '/localroles/*/rules', 'tenant', 'projectmgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('229', '/tenants/*/projects/*/apps/*/topo', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('230', '/clusters/domain', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('231', '/tenants/*/projects/*/deploys/*/canaryrollback', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('232', '/tenants/*/projects/*/deploys/*/linklogs', 'log', 'applog');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('233', '/users/auth/getUserName', 'system', 'system');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('234', '/harbor/*/replicationpolicies', 'delivery', 'repositorymgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('235', '/msf/namespace/reset', 'msf', 'msf');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('236', '/tenants/*/projects/*/deploys/*/scale', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('237', '/tenants/*/projects/*/configmap/search', 'appcenter', 'configmap');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('238', '/msf/tasks', 'whitelist', 'whitelist');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('239', '/tenants/*/projects/*/dockerfile', 'cicd', 'cicdmgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('240', '/tenants/*/projects/*/repositories', 'delivery', 'repository');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('241', '/tenants/*/projects/*/apps', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('242', '/clusters/*/nodes/*/pod', 'infrastructure', 'node');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('243', '/tenants/*/projects/*/apptemplates/*/status', 'delivery', 'template');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('244', '/clusters/*/resource/allocate', 'infrastructure', 'clustermgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('245', '/system/auditlogs', 'log', 'auditlog');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('246', '/tenants/*/projects/*/jobs/*/parallelism', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('247', '/tenants/*/projects/*/svctemplates/search', 'delivery', 'template');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('248', '/tenants/*/projects/*/svctemplates', 'delivery', 'template');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('249', '/users/auth/login', 'whitelist', 'whitelist');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('250', '/tenants/*/projects/*/deploys/*/terminal/terminalmessage', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('251', '/tenants/*/projects/*/deploys/*/events', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('252', '/tenants/*/projects/*/deploys/containers', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('253', '/tenants/*/projects/*/deploys/*/bluegreen/cancel', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('254', '/users/menu', 'whitelist', 'whitelist');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('255', '/tenants/*/projects/*/apptemplates/*/deploys', 'delivery', 'template');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('256', '/clusters/*/nodes/*/status', 'infrastructure', 'node');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('257', '/tenants/*/projects/*/deploys/*/canaryupdate/pause', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('258', '/clusters/*/events', 'overview', 'overview');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('259', '/users', 'system', 'system');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('260', '/users/status/active', 'system', 'system');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('261', '/tenants/*/projects/*/deploys/*/reversions/detail', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('262', '/tenants/*/projects/*/repositories/*/images/*/tags', 'delivery', 'image');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('263', '/tenants/*/networks', 'tenant', 'tenantmgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('264', '/openapi/node/restartevents', 'whitelist', 'whitelist');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('265', '/tenants/*/projects/*/cicdjobs/*/result/*/delete', 'cicd', 'cicdmgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('266', '/users/status/pause', 'system', 'system');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('267', '/tenants/*/projects/*/yaml', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('268', '/tenants/*/projects/*/repositories/*/detail', 'delivery', 'repository');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('269', '/snapshotrules', 'log', 'snapshotrule');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('270', '/tenants/*/projects/*/repositories/*/summary', 'delivery', 'repository');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('271', '/tenants/*/projects/*/repositories/userselect', 'delivery', 'repository');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('272', '/tenants/project/removeUser', 'whitelist', 'whitelist');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('273', '/tenants/project/addUser', 'whitelist', 'whitelist');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('274', '/tenants/addUser', 'whitelist', 'whitelist');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('275', '/tenants/addProject', 'whitelist', 'whitelist');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('276', '/tenants/*/projects/*/repositories/images', 'delivery', 'image');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('277', '/harbor/harborprojects/overview', 'delivery', 'repository');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('278', '/harbor/*/overview', 'delivery', 'repository');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('279', '/tenants/*/projects/*/cicdjobs/testsuites', 'cicd', 'cicdmgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('280', '/cicdjobs/stage/*/result/*/testcallback', 'whitelist', 'whitelist');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('281', '/localroles/projects', 'tenant', 'projectmgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('282', '/tenants/*/projects/*/deploys/rules', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('284', '/tenants/*/projects/*/projectmember/projectRole', 'tenant', 'projectmgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('285', '/tenants/*/projects/*/repositories/cleanrules', 'delivery', 'repositorymgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('286', '/dashboard/clusters/*/namespaces', 'overview', 'overview');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('287', '/tenants/*/projects/*/projectmember/*/projectRole', 'tenant', 'projectmgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('288', '/localroles/userNames', 'tenant', 'projectmgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('289', '/tenants/*/namespaces/*/removeNodes', 'tenant', 'tenantmgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('290', '/snapshotrules/*/start', 'log', 'snapshotrule');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('291', '/snapshotrules/*/stop', 'log', 'snapshotrule');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('292', '/snapshotrules/snapshots', 'log', 'snapshotrule');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('293', '/dashboard/clusters/*/component/pods', 'overview', 'overview');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('294', '/tenants/audit', 'log', 'auditlog');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('295', '/clusters/*/status', 'infrastructure', 'node');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('296', '/tenants/*/apps', 'tenant', 'tenantmgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('297', '/tenants/*/projects/*/repositories/*/images/*/tags/*/syncImage', 'delivery', 'image');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('299', '/clusters/*/nodes/*/schedule', 'infrastructure', 'node');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('302', '/clusters/*/nodes/*/drainPod', 'infrastructure', 'node');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('303', '/clusters/*/nodes/*/drainProgress', 'infrastructure', 'node');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('304', '/tenants/*/msf/apps', 'msf', 'msf');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('305', '/tenants/*/msf/deploys', 'msf', 'msf');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('306', '/tenants/*/msf/deploys/*/start', 'msf', 'msf');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('307', '/tenants/*/msf/deploys/*/stop', 'msf', 'msf');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('308', '/tenants/*/msf/deploys/*/scale', 'msf', 'msf');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('309', '/tenants/*/msf/deploys/*/pods', 'msf', 'msf');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('310', '/tenants/*/msf/deploys/*/events', 'msf', 'msf');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('311', '/tenants/*/msf/deploys/*/containers', 'msf', 'msf');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('312', '/tenants/*/msf/deploys/*/applogs/filenames', 'msf', 'msf');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('313', '/tenants/*/msf/deploys/*/applogs/stderrlogs', 'msf', 'msf');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('314', '/tenants/*/msf/deploys/*/logfile/*/export', 'msf', 'msf');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('315', '/tenants/*/msf/deploys/rules', 'msf', 'msf');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('316', '/tenants/*/msf/deploys/*/applogs', 'msf', 'msf');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('317', '/tenants/*/projects/*/cicdjobs/*/stageresult', 'cicd', 'cicdmgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('318', '/tenants/*/projects/*/cicdjobs/*/stages/*/log', 'cicd', 'cicdmgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('319', '/tenants/*/projects/*/deploys/*/hpa', 'appcenter', 'autoscale');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('320', '/tenants/*/projects/*/repositories/*/images/*/tags/*/syncclusters', 'delivery', 'image');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('321', '/tenants/*/projects/*/svctemplates/*/checkResource', 'delivery', 'template');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('322', '/tenants/*/projects/*/apptemplates/*/checkResource', 'delivery', 'template');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('323', '/clusters/*/nodes/*/addNode', 'infrastructure', 'node');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('324', '/clusters/*/nodes/*/removeNode', 'infrastructure', 'node');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('325', '/tenants/*/members', 'tenant', 'tenantmgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('326', '/tenants/*/privateNodeList', 'tenant', 'tenantmgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('327', '/tenants/*/namespaces/*/addNodes', 'tenant', 'tenantmgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('328', '/tenants/projects/addUser', 'tenant', 'tenantmgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('329', '/roles/*/copy', 'system', 'system');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('330', '/tenants/*/projects/*/svctemplates/*/checkname', 'delivery', 'template');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('331', '/tenants/*/projects/*/repositories/*/images/*/pull', 'delivery', 'image');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('332', '/tenants/*/projects/*/repositories/*/images/*/local', 'delivery', 'image');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('333', '/cicd/jobs/*/webhook', 'whitelist', 'whitelist');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('334', '/tenants/*/projects/*/apptemplates/*/checkname', 'delivery', 'template');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('335', '/tenants/*/projects/*/deploys/*/applogs/export', 'log', 'applog');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('336', '/harbor/*/gc', 'delivery', 'repositorymgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('337', '/snapshotrules/snapshots/restored', 'log', 'snapshotrule');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('338', '/tenants/*/projects/*/apps/yaml', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('339', '/tenants/*/projects/*/cicdjobs/*/rename', 'cicd', 'cicdmgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('340', '/openapi/getUrlDic', 'whitelist', 'whitelist');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('341', '/system/configs/maintenance', 'system', 'system');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('342', '/tenants/*/projects/*/apps/*/start', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('343', '/tenants/*/projects/*/apps/*/stop', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('344', '/clusters/*/namespaces/*/deploys/*/logs', 'log', 'applog');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('345', '/clusters/*/namespaces/*/deploys/*/logs/export', 'log', 'applog');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('346', '/clusters/*/namespaces/*/deploys/*/logs/filenames', 'log', 'applog');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('347', '/tenants/*/projects/*/configmap/content', 'appcenter', 'configmap');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('348', '/tenants/*/projects/*/cicdjobs/*/stages/updateCredentials', 'cicd', 'cicdmgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('349', '/users/switchLanguage', 'whitelist', 'whitelist');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('350', '/tenants/*/projects/*/pvs/*/release', 'appcenter', 'volume');
INSERT INTO `k8s_auth_server`.`url_dic` (`id`, `url`, `module`, `resource`) VALUES ('351', '/system/configs/cicd', 'system', 'system');


-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `token` varchar(255) DEFAULT NULL,
  `create_time` timestamp NULL DEFAULT NULL,
  `token_create` timestamp NULL DEFAULT NULL,
  `is_admin` tinyint(1) DEFAULT '0',
  `update_time` timestamp NULL DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `real_name` varchar(255) DEFAULT NULL,
  `is_machine` tinyint(1) DEFAULT '0',
  `comment` varchar(255) DEFAULT NULL,
  `pause` varchar(255) DEFAULT NULL COMMENT '用户的状态',
  `phone` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username_UNIQUE` (`username`),
  UNIQUE KEY `email_UNIQUE` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=47 DEFAULT CHARSET=utf8 AVG_ROW_LENGTH=910;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES ('1', 'admin', 'A0A475CF454CF9A06979034098167B9E', '330957b867a3462ea457bec41410624b', null, '2016-11-03 14:48:37', '1', '2017-06-16 15:43:27', null, null, '1', null, 'normal', null);
INSERT INTO `user` VALUES ('39', 'xfliang', null, null, null, null, '1', null, 'xfliang@whchem.com', '梁晓峰', '0', null, 'normal', '1111');

-- ----------------------------
-- Table structure for user_group
-- ----------------------------
DROP TABLE IF EXISTS `user_group`;
CREATE TABLE `user_group` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `groupname` varchar(255) NOT NULL,
  `user_group_describe` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `groupname_UNIQUE` (`groupname`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of user_group
-- ----------------------------

-- ----------------------------
-- Table structure for user_group_relation
-- ----------------------------
DROP TABLE IF EXISTS `user_group_relation`;
CREATE TABLE `user_group_relation` (
  `userid` bigint(20) NOT NULL,
  `groupid` int(11) NOT NULL,
  KEY `group` (`groupid`),
  KEY `user` (`userid`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of user_group_relation
-- ----------------------------

-- ----------------------------
-- Table structure for user_project
-- ----------------------------
DROP TABLE IF EXISTS `user_project`;
CREATE TABLE `user_project` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `tenant_id` varchar(64) NOT NULL COMMENT '租户id',
  `project_id` varchar(64) NOT NULL,
  `username` varchar(64) NOT NULL COMMENT '用户名',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '创建时间',
  `email` varchar(64) DEFAULT NULL,
  `nick_name` varchar(64) DEFAULT NULL COMMENT '显示名称',
  `reserve1` varchar(255) DEFAULT NULL COMMENT '预留字段',
  `reserve2` varchar(255) DEFAULT NULL COMMENT '预留字段',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of user_project
-- ----------------------------

-- ----------------------------
-- Table structure for user_role_relationship
-- ----------------------------
DROP TABLE IF EXISTS `user_role_relationship`;
CREATE TABLE `user_role_relationship` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `tenant_id` varchar(64) DEFAULT NULL COMMENT '租户id',
  `username` varchar(64) NOT NULL COMMENT '用户名',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `project_id` varchar(64) DEFAULT NULL COMMENT '项目id',
  `role_id` int(11) DEFAULT NULL COMMENT '角色id',
  `has_local_role` tinyint(1) DEFAULT NULL COMMENT '在项目下是否有局部角色',
  `reserve1` varchar(255) DEFAULT NULL COMMENT '预留字段',
  `reserve2` varchar(255) DEFAULT NULL COMMENT '预留字段',
  PRIMARY KEY (`id`),
  KEY `project` (`project_id`) USING BTREE,
  KEY `username` (`username`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of user_role_relationship
-- ----------------------------

-- ----------------------------
-- Table structure for user_tenant
-- ----------------------------
DROP TABLE IF EXISTS `user_tenant`;
CREATE TABLE `user_tenant` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `tenantid` varchar(255) NOT NULL,
  `username` varchar(255) NOT NULL,
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `istm` int(11) DEFAULT '0',
  `role` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of user_tenant
-- ----------------------------

-- ----------------------------
-- Table structure for tenant_private_node
-- ----------------------------
DROP TABLE IF EXISTS `tenant_private_node`;
CREATE TABLE `tenant_private_node` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id主键',
  `tenant_id` varchar(64) NOT NULL COMMENT '租户id',
  `cluster_id` varchar(64) NOT NULL COMMENT '集群id',
  `namespace` varchar(64) DEFAULT NULL COMMENT '分区名',
  `node_name` varchar(64) NOT NULL COMMENT '节点名',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `reserve1` varchar(255) DEFAULT NULL COMMENT '预留字段',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



DROP TABLE IF EXISTS `transfer_step`;
CREATE TABLE `transfer_step` (
  `step_id` int(64) NOT NULL COMMENT '步骤id',
  `step_name` varchar(64) NOT NULL COMMENT '步骤名称',
  `percent` varchar(64) NOT NULL COMMENT '百分比',
  PRIMARY KEY (`step_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



-- ----------------------------
-- Table structure for transfer_cluster_backup
-- ----------------------------
DROP TABLE IF EXISTS `transfer_cluster_backup`;
CREATE TABLE `transfer_cluster_backup` (
  `id` int(255) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `tenant_id` varchar(30) NOT NULL COMMENT '租户id',
  `namespace_num` int(30) NOT NULL COMMENT '第几次迁移分区',
  `deploy_num` int(30) NOT NULL COMMENT '第几次迁移项目',
  `err_msg` varchar(64) DEFAULT NULL COMMENT '错误原因',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `transfer_cluster_id` varchar(64) NOT NULL COMMENT '目标集群id',
  `is_continue` tinyint(4) DEFAULT '0' COMMENT '是否断点续传 0:不是 1:是',
  `is_default` tinyint(4) DEFAULT '0' COMMENT '是否是增量迁移 0:不是 1:是',
  `transfer_cluster_percent` varchar(64) DEFAULT NULL COMMENT '迁移集群的百分比',
  `project_id` varchar(64) DEFAULT NULL COMMENT '迁移到那个项目下',
  `err_namespace` varchar(255) DEFAULT NULL COMMENT '当前迁移失败的分区',
  `err_deploy` varchar(255) DEFAULT NULL COMMENT '当前迁移失败的应用',
  `old_cluster_id` varchar(64) DEFAULT NULL COMMENT '旧集群id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=74 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `transfer_cluster`;
CREATE TABLE `transfer_cluster` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `tenant_id` varchar(30) NOT NULL COMMENT '租户id',
  `cluster_id` varchar(30) NOT NULL COMMENT '目标集群id',
  `old_cluster_id` varchar(30) NOT NULL COMMENT '原集群id',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `is_transfer` tinyint(30) NOT NULL DEFAULT '0' COMMENT '是否迁移过 0:未迁移过 1:已迁移过',
  `is_continue` tinyint(30) NOT NULL DEFAULT '0' COMMENT '是否断点续传过 0:未断电续传 1:已断点续传',
  `is_err` int(4) NOT NULL DEFAULT '0' COMMENT '是否成功 0:成功 1:失败',
  `percent` varchar(255) DEFAULT NULL COMMENT '百分比',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `transfer_bind_namespace`;
CREATE TABLE `transfer_bind_namespace` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `current_namespace` varchar(255) NOT NULL COMMENT '原分区名称',
  `create_namespace` varchar(255) NOT NULL COMMENT '新建分区名称',
  `cluster_id` varchar(30) NOT NULL COMMENT '目标集群id',
  `tenant_id` varchar(30) NOT NULL COMMENT '租户id',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `is_delete` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除 0:未删除 1:已删除',
  `is_default` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否是默认名称分区 0:不是 1:是',
  `status` int(30) NOT NULL DEFAULT '0' COMMENT '状态 0:未迁移 1:已迁移',
  `err_msg` varchar(30) DEFAULT NULL COMMENT '错误原因',
  `namespace_num` int(30) NOT NULL DEFAULT '0' COMMENT '第几次迁移分区',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=231 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `transfer_bind_deploy`;
CREATE TABLE `transfer_bind_deploy` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `namespace` varchar(30) NOT NULL COMMENT '分区名称',
  `cluster_id` varchar(30) NOT NULL COMMENT '目标集群id',
  `deploy_name` varchar(30) NOT NULL COMMENT '服务名称',
  `step_id` int(11) DEFAULT NULL COMMENT '步骤id',
  `tenant_id` varchar(30) NOT NULL COMMENT '租户id',
  `project_id` varchar(30) NOT NULL COMMENT '项目id',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `status` int(30) NOT NULL DEFAULT '0' COMMENT '服务前移状态 0:未迁移 1:已迁移',
  `err_msg` varchar(30) DEFAULT NULL COMMENT '错误原因',
  `is_delete` tinyint(4) DEFAULT '0' COMMENT '是否删除 0:未删除 1:已删除',
  `deploy_num` int(30) DEFAULT NULL COMMENT '第几次迁移应用',
  `old_cluster_id` varchar(64) NOT NULL COMMENT '旧的集群id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=232 DEFAULT CHARSET=utf8;