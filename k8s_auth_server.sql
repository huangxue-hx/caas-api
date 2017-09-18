DROP DATABASE IF EXISTS `k8s_auth_server`;
CREATE DATABASE `k8s_auth_server`;

use `k8s_auth_server`;
-- MySQL dump 10.13  Distrib 5.7.17, for Win64 (x86_64)
--
-- Host: 10.10.101.153    Database: k8s_auth_server
-- ------------------------------------------------------
-- Server version	5.6.35

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `auth_user`
--

DROP TABLE IF EXISTS `auth_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `auth_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `password` varchar(45) NOT NULL,
  `harbor_id` varchar(45) NOT NULL,
  `role_name` varchar(45) NOT NULL DEFAULT 'admin',
  PRIMARY KEY (`id`),
  UNIQUE KEY `auth_name_UNIQUE` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='第三方用户认证之后插入Harbor中的用户';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `auth_user`
--

LOCK TABLES `auth_user` WRITE;
/*!40000 ALTER TABLE `auth_user` DISABLE KEYS */;
INSERT INTO `auth_user` VALUES (1,'testaaa','123456','330','admin');
/*!40000 ALTER TABLE `auth_user` ENABLE KEYS */;
--
-- Table structure for table `business`
--

DROP TABLE IF EXISTS `business`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `business` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `template_id` int(11) DEFAULT NULL,
  `name` varchar(45) CHARACTER SET latin1 DEFAULT NULL,
  `details` varchar(250) DEFAULT NULL,
  `namespaces` varchar(45) CHARACTER SET latin1 DEFAULT NULL,
  `user` varchar(45) CHARACTER SET latin1 DEFAULT NULL,
  `create_time` timestamp NULL DEFAULT NULL,
  `tenant` varchar(45) CHARACTER SET latin1 DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `business`
--

LOCK TABLES `business` WRITE;
/*!40000 ALTER TABLE `business` DISABLE KEYS */;
/*!40000 ALTER TABLE `business` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `business_service`
--

DROP TABLE IF EXISTS `business_service`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `business_service` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `business_id` int(11) NOT NULL,
  `service_id` int(11) NOT NULL,
  `status` int(1) DEFAULT NULL,
  `is_external` int(1) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `business_service`
--

LOCK TABLES `business_service` WRITE;
/*!40000 ALTER TABLE `business_service` DISABLE KEYS */;
INSERT INTO `business_service` (`id`,`business_id`,`service_id`,`status`,`is_external`) VALUES (1,1,1,0,0),(2,2,2,0,0),(3,3,3,0,0),(4,4,4,0,0),(5,5,5,0,0),(6,6,6,0,0),(7,7,7,0,0),(8,8,8,0,0),(9,9,9,0,0),(10,10,10,0,0),(11,11,11,0,0),(13,13,13,0,0),(14,13,14,0,0),(15,13,15,0,0),(16,13,16,0,0),(17,13,17,0,0),(18,13,18,0,0),(19,13,19,0,0);
/*!40000 ALTER TABLE `business_service` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `business_templates`
--

DROP TABLE IF EXISTS `business_templates`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `business_templates` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) CHARACTER SET latin1 DEFAULT NULL,
  `tag` varchar(45) CHARACTER SET latin1 DEFAULT NULL,
  `details` varchar(512) DEFAULT NULL,
  `status` int(1) DEFAULT NULL,
  `tenant` varchar(45) CHARACTER SET latin1 DEFAULT NULL,
  `create_user` varchar(45) CHARACTER SET latin1 DEFAULT NULL,
  `create_time` timestamp NULL DEFAULT NULL,
  `update_time` timestamp NULL DEFAULT NULL,
  `is_deploy` int(1) DEFAULT NULL,
  `image_list` varchar(2048) CHARACTER SET latin1 DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `business_templates`
--

LOCK TABLES `business_templates` WRITE;
/*!40000 ALTER TABLE `business_templates` DISABLE KEYS */;
INSERT INTO `business_templates` (`id`,`name`,`tag`,`details`,`status`,`tenant`,`create_user`,`create_time`,`update_time`,`is_deploy`,`image_list`) VALUES (1,'Tomcat','8.0',NULL,0,'all','admin','2017-08-11 14:18:36',NULL,1,'onlineshop/tomcat'),(2,'Redis','3.2-alpine',NULL,0,'all','admin','2017-08-11 14:44:14',NULL,1,'onlineshop/redis'),(3,'WordPress','4.8.0-php7.1-fpm-alpine',NULL,0,'all','admin','2017-08-11 15:31:54',NULL,1,'onlineshop/wordpress'),(4,'InfluxDB','v1.3.0',NULL,0,'all','admin','2017-08-11 16:12:19',NULL,1,'onlineshop/influxdb'),(5,'MySQL','v1',NULL,0,'all','admin','2017-08-11 18:03:10',NULL,1,'onlineshop/mysqls'),(6,'Webhook','2.6.5',NULL,0,'all','admin','2017-08-12 15:54:52',NULL,NULL,'onlineshop/webhook'),(7,'Mongodb','v3.5',NULL,0,'all','admin','2017-09-05 17:02:28',NULL,NULL,'onlineshop/mongodb'),(8,'Rabbitmq','3.6.11',NULL,0,'all','admin','2017-09-05 17:03:30',NULL,NULL,'onlineshop/rabbitmq'),(9,'Nginx','latest',NULL,0,'all','admin','2017-09-05 17:05:19',NULL,NULL,'onlineshop/nginx'),(10,'Websphere','8.5.5.9-install',NULL,0,'all','admin','2017-09-05 17:06:07',NULL,NULL,'onlineshop/websphere'),(11,'Elasticsearch','v2.4.1-1',NULL,0,'all','admin','2017-09-05 17:07:25',NULL,NULL,'onlineshop/elasticsearch'),(13,'Fabric0.6','0.6',NULL,0,'all','admin','2017-08-18 14:44:42',NULL,1,'onlineshop/fabric-peer,onlineshop/fabric-membersrvc');
/*!40000 ALTER TABLE `business_templates` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cicd_build_environment`
--

DROP TABLE IF EXISTS `cicd_build_environment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cicd_build_environment` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `image` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cicd_build_environment`
--

LOCK TABLES `cicd_build_environment` WRITE;
/*!40000 ALTER TABLE `cicd_build_environment` DISABLE KEYS */;
INSERT INTO `cicd_build_environment` VALUES (1,'java8','k8s-deploy/jenkins-slave-java:latest'),(2,'node8','k8s-deploy/jenkins-slave-node:8'),(3,'python2.7','k8s-deploy/jenkins-slave-python:2.7'),(4,'python3.6','k8s-deploy/jenkins-slave-python:3.6');
/*!40000 ALTER TABLE `cicd_build_environment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cicd_docker_file`
--

DROP TABLE IF EXISTS `cicd_docker_file`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cicd_docker_file` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '名称',
  `tenant` varchar(50) NOT NULL COMMENT '租户',
  `content` text NOT NULL COMMENT '内容',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cicd_docker_file`
--

LOCK TABLES `cicd_docker_file` WRITE;
/*!40000 ALTER TABLE `cicd_docker_file` DISABLE KEYS */;
/*!40000 ALTER TABLE `cicd_docker_file` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cicd_docker_file_job_stage`
--

DROP TABLE IF EXISTS `cicd_docker_file_job_stage`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cicd_docker_file_job_stage` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `docker_file_id` int(11) NOT NULL,
  `job_id` int(11) DEFAULT NULL,
  `stage_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cicd_docker_file_job_stage`
--

LOCK TABLES `cicd_docker_file_job_stage` WRITE;
/*!40000 ALTER TABLE `cicd_docker_file_job_stage` DISABLE KEYS */;
/*!40000 ALTER TABLE `cicd_docker_file_job_stage` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cicd_job`
--

DROP TABLE IF EXISTS `cicd_job`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cicd_job` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) DEFAULT NULL,
  `tenant` varchar(255) DEFAULT NULL,
  `tenant_id` varchar(255) DEFAULT NULL,
  `notification` int(1) DEFAULT NULL,
  `success_notification` int(1) DEFAULT NULL,
  `fail_notification` int(1) DEFAULT NULL,
  `mail` varchar(1000) DEFAULT NULL,
  `trigger` int(1) DEFAULT NULL,
  `poll_scm` int(1) DEFAULT NULL,
  `poll_scm_customize` int(1) DEFAULT NULL,
  `cron_exp_for_poll_scm` varchar(255) DEFAULT NULL,
  `create_user` varchar(100) DEFAULT NULL,
  `update_user` varchar(100) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `last_build_num` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cicd_job`
--

LOCK TABLES `cicd_job` WRITE;
/*!40000 ALTER TABLE `cicd_job` DISABLE KEYS */;
/*!40000 ALTER TABLE `cicd_job` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cicd_job_build`
--

DROP TABLE IF EXISTS `cicd_job_build`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cicd_job_build` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `job_id` int(11) DEFAULT NULL,
  `build_num` int(11) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `start_time` datetime DEFAULT NULL,
  `duration` varchar(20) DEFAULT NULL,
  `start_user` varchar(20) DEFAULT NULL,
  `log` longtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cicd_job_build`
--

LOCK TABLES `cicd_job_build` WRITE;
/*!40000 ALTER TABLE `cicd_job_build` DISABLE KEYS */;
/*!40000 ALTER TABLE `cicd_job_build` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cicd_stage`
--

DROP TABLE IF EXISTS `cicd_stage`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cicd_stage` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `job_id` int(11) NOT NULL,
  `stage_order` int(11) DEFAULT NULL,
  `stage_type_id` varchar(20) DEFAULT NULL,
  `stage_name` varchar(20) DEFAULT NULL,
  `repository_type` varchar(10) DEFAULT NULL,
  `repository_url` varchar(250) DEFAULT NULL,
  `repository_branch` varchar(50) DEFAULT NULL,
  `credentials_username` varchar(50) DEFAULT NULL,
  `credentials_password` varchar(50) DEFAULT NULL,
  `credentials_id` varchar(50) DEFAULT NULL,
  `build_environment` varchar(50) DEFAULT NULL,
  `environment_variables` varchar(1000) DEFAULT NULL,
  `use_dependency` int(1) DEFAULT NULL,
  `dependences` varchar(1000) DEFAULT NULL,
  `dockerfile_type` int(1) DEFAULT NULL,
  `base_image` varchar(50) DEFAULT NULL,
  `dockerfile_id` int(11) DEFAULT NULL,
  `dockerfile_path` varchar(250) DEFAULT NULL,
  `image_name` varchar(50) DEFAULT NULL,
  `image_tag_type` varchar(50) DEFAULT NULL,
  `image_base_tag` varchar(50) DEFAULT NULL,
  `image_increase_tag` varchar(50) DEFAULT NULL,
  `image_tag` varchar(50) DEFAULT NULL,
  `harbor_project` varchar(50) DEFAULT NULL,
  `namespace` varchar(50) DEFAULT NULL,
  `service_name` varchar(50) DEFAULT NULL,
  `container_name` varchar(50) DEFAULT NULL,
  `command` varchar(1000) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cicd_stage`
--

LOCK TABLES `cicd_stage` WRITE;
/*!40000 ALTER TABLE `cicd_stage` DISABLE KEYS */;
/*!40000 ALTER TABLE `cicd_stage` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cicd_stage_build`
--

DROP TABLE IF EXISTS `cicd_stage_build`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cicd_stage_build` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `job_id` int(11) DEFAULT NULL,
  `stage_id` int(11) DEFAULT NULL,
  `build_num` int(11) DEFAULT NULL,
  `status` varchar(20) DEFAULT NULL,
  `start_time` datetime DEFAULT NULL,
  `duration` varchar(20) DEFAULT NULL,
  `log` longtext,
  `tag` varchar(255) CHARACTER SET latin1 DEFAULT NULL,
  `stage_name` varchar(255) DEFAULT NULL,
  `stage_order` int(11) DEFAULT NULL,
  `stage_type` varchar(255) DEFAULT NULL,
  `image` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cicd_stage_build`
--

LOCK TABLES `cicd_stage_build` WRITE;
/*!40000 ALTER TABLE `cicd_stage_build` DISABLE KEYS */;
/*!40000 ALTER TABLE `cicd_stage_build` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cicd_stage_type`
--

DROP TABLE IF EXISTS `cicd_stage_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cicd_stage_type` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) DEFAULT NULL,
  `user_defined` int(1) DEFAULT NULL,
  `tenant_id` varchar(255) DEFAULT NULL,
  `template_type` int(2) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=８ DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cicd_stage_type`
--

LOCK TABLES `cicd_stage_type` WRITE;
/*!40000 ALTER TABLE `cicd_stage_type` DISABLE KEYS */;
INSERT INTO `cicd_stage_type` VALUES (1,'代码检出/编译',0,NULL,0),(2,'单元测试',0,NULL,4),(3,'镜像构建',0,NULL,1),(4,'应用部署',0,NULL,2),(5,'集成测试',0,NULL,4),(6,'自定义',0,NULL,3),(7,'代码扫描',0,NULL,５);
/*!40000 ALTER TABLE `cicd_stage_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cicd_stage_sonar`
--

DROP TABLE IF EXISTS `cicd_stage_sonar`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cicd_stage_sonar` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `stage_id` int(11) NOT NULL,
  `qualitygates_id` int(11) DEFAULT NULL,
  `project_name` varchar(100) DEFAULT NULL,
  `project_key` varchar(100) DEFAULT NULL,
  `sonar_property` varchar(800) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cicd_docker_file_job_stage`
--

LOCK TABLES `cicd_docker_file_job_stage` WRITE;
/*!40000 ALTER TABLE `cicd_docker_file_job_stage` DISABLE KEYS */;
/*!40000 ALTER TABLE `cicd_docker_file_job_stage` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cluster`
--

DROP TABLE IF EXISTS `cluster`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cluster` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `host` varchar(255) NOT NULL,
  `protocol` varchar(255) NOT NULL,
  `auth_type` varchar(255) NOT NULL,
  `username` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `machine_token` varchar(255) NOT NULL,
  `port` varchar(255) NOT NULL,
  `entry_point` varchar(255) NOT NULL,
  `haproxy_version` varchar(255) NOT NULL,
  `influxdb_url` varchar(255) NOT NULL,
  `influxdb_db` varchar(255) NOT NULL,
  `influxdb_version` varchar(255) NOT NULL,
  `es_host` varchar(255) NOT NULL,
  `es_port` int(11) NOT NULL,
  `es_cluster_name` varchar(255) NOT NULL,
  `es_version` varchar(255) NOT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  UNIQUE KEY `host` (`host`)
) ENGINE=InnoDB AUTO_INCREMENT=201 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;


--
-- Table structure for table `cluster_domain`
--

DROP TABLE IF EXISTS `cluster_domain`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cluster_domain` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `domain` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cluster_domain`
--

LOCK TABLES `cluster_domain` WRITE;
/*!40000 ALTER TABLE `cluster_domain` DISABLE KEYS */;
INSERT INTO `cluster_domain` VALUES (3,'harmonycloud.com');
/*!40000 ALTER TABLE `cluster_domain` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `configfile`
--

DROP TABLE IF EXISTS `configfile`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `configfile` (
  `id` varchar(64) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `tags` varchar(255) DEFAULT NULL,
  `create_time` varchar(255) DEFAULT NULL,
  `tenant` varchar(255) DEFAULT NULL,
  `reponame` varchar(255) DEFAULT NULL,
  `creator` varchar(255) DEFAULT NULL,
  `item` text,
  `path` varchar(512) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `configfile`
--

LOCK TABLES `configfile` WRITE;
/*!40000 ALTER TABLE `configfile` DISABLE KEYS */;
/*!40000 ALTER TABLE `configfile` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `django_migrations`
--

DROP TABLE IF EXISTS `django_migrations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `django_migrations` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `app` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `applied` datetime(6) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `django_migrations`
--

LOCK TABLES `django_migrations` WRITE;
/*!40000 ALTER TABLE `django_migrations` DISABLE KEYS */;
/*!40000 ALTER TABLE `django_migrations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `external_type`
--

DROP TABLE IF EXISTS `external_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `external_type` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `type` varchar(255) NOT NULL DEFAULT '' COMMENT '外部服务类型',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `external_type`
--

LOCK TABLES `external_type` WRITE;
/*!40000 ALTER TABLE `external_type` DISABLE KEYS */;
INSERT INTO `external_type` VALUES (1,'全部'),(2,'mysql'),(3,'oracle'),(4,'zookeeper'),(5,'storm'),(6,'flume'),(7,'redis'),(8,'memcached'),(9,'kafka'),(10,'other');
/*!40000 ALTER TABLE `external_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `file_upload_container`
--

DROP TABLE IF EXISTS `file_upload_container`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `file_upload_container` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `container_file_path` varchar(500) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `file_name` varchar(500) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `user_id` bigint(20) DEFAULT NULL,
  `namespace` varchar(500) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `deployment` varchar(500) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `pod` varchar(500) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `container` varchar(500) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `phase` int(11) DEFAULT NULL COMMENT '标记文件上传阶段（1：上传到节点,2：上传到容器）',
  `status` varchar(45) DEFAULT NULL COMMENT 'failed, success, doing',
  `err_msg` varchar(500) DEFAULT NULL COMMENT '错误信息',
  `create_time` timestamp NULL DEFAULT NULL,
  `update_time` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `file_upload_container`
--

LOCK TABLES `file_upload_container` WRITE;
/*!40000 ALTER TABLE `file_upload_container` DISABLE KEYS */;
/*!40000 ALTER TABLE `file_upload_container` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `harborProject_tenant`
--

DROP TABLE IF EXISTS `harborProject_tenant`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `harborProject_tenant` (
  `id` int(10) NOT NULL AUTO_INCREMENT COMMENT '序号',
  `harborProject_id` int(10) NOT NULL COMMENT 'harbor projectId',
  `tenant_id` varchar(255) NOT NULL COMMENT '租户id',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `tenant_name` varchar(255) DEFAULT NULL COMMENT '租户名称',
  `harbor_project_name` varchar(255) DEFAULT NULL,
  `isPublic` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `harborProject_id_UNIQUE` (`harborProject_id`)
) ENGINE=InnoDB AUTO_INCREMENT=151 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `harborProject_tenant`
--

LOCK TABLES `harborProject_tenant` WRITE;
/*!40000 ALTER TABLE `harborProject_tenant` DISABLE KEYS */;
INSERT INTO `harborProject_tenant` VALUES (1,3,'1234',NULL,NULL,'onlineshop',1),(108,2,'99999999','2017-06-16 12:18:10',NULL,'k8s-deploy',0);
/*!40000 ALTER TABLE `harborProject_tenant` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `harbor_user`
--

DROP TABLE IF EXISTS `harbor_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `harbor_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=263 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `harbor_user`
--

LOCK TABLES `harbor_user` WRITE;
/*!40000 ALTER TABLE `harbor_user` DISABLE KEYS */;
/*!40000 ALTER TABLE `harbor_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `installprogress_installprogress`
--

DROP TABLE IF EXISTS `installprogress_installprogress`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `installprogress_installprogress` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(30) NOT NULL,
  `progress` int(11) NOT NULL,
  `install_status` varchar(255) DEFAULT NULL,
  `cluster_id` int(11) NOT NULL,
  `error_msg` varchar(255) CHARACTER SET utf8 DEFAULT NULL COMMENT '错误信息',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `installprogress_installprogress`
--

LOCK TABLES `installprogress_installprogress` WRITE;
/*!40000 ALTER TABLE `installprogress_installprogress` DISABLE KEYS */;
/*!40000 ALTER TABLE `installprogress_installprogress` ENABLE KEYS */;
UNLOCK TABLES;


--
-- Table structure for table `ldap_config`
--

DROP TABLE IF EXISTS `ldap_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ldap_config` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `ip` varchar(255) NOT NULL,
  `port` varchar(255) NOT NULL,
  `base` varchar(255) NOT NULL,
  `userdn` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `is_on` int(1) NOT NULL DEFAULT '0',
  `create_user` varchar(255) DEFAULT NULL,
  `update_user` varchar(255) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ldap_config`
--

LOCK TABLES `ldap_config` WRITE;
/*!40000 ALTER TABLE `ldap_config` DISABLE KEYS */;
/*!40000 ALTER TABLE `ldap_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `message`
--

DROP TABLE IF EXISTS `message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `message` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `app_key` varchar(255) NOT NULL,
  `server_url` varchar(255) NOT NULL,
  `app_secret` varchar(255) NOT NULL,
  `nonce` varchar(255) NOT NULL,
  `templateid` varchar(255) NOT NULL,
  `annotation` varchar(255) CHARACTER SET utf8 DEFAULT NULL COMMENT '短信接口供应商',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `message`
--

LOCK TABLES `message` WRITE;
/*!40000 ALTER TABLE `message` DISABLE KEYS */;
INSERT INTO `message` VALUES (1,'e28545b26aa3fa384d07924d9164cc85','https://api.netease.im/sms/sendtemplate.action暂停使用','c0bfefaa0d88','12345','3056657','网易云信');
/*!40000 ALTER TABLE `message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `namespace_bind_subnet`
--

DROP TABLE IF EXISTS `namespace_bind_subnet`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `namespace_bind_subnet` (
  `id` int(10) NOT NULL AUTO_INCREMENT COMMENT '序号',
  `namespace` varchar(45) DEFAULT NULL COMMENT 'k8s namespace',
  `subnet_id` varchar(255) NOT NULL COMMENT '子网id',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `net_id` varchar(255) NOT NULL COMMENT '父网id',
  `subnet_name` varchar(255) DEFAULT NULL,
  `binding` int(255) DEFAULT '0' COMMENT '是否绑定 0 false 1 true',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `namespace_bind_subnet`
--

LOCK TABLES `namespace_bind_subnet` WRITE;
/*!40000 ALTER TABLE `namespace_bind_subnet` DISABLE KEYS */;
/*!40000 ALTER TABLE `namespace_bind_subnet` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `network`
--

DROP TABLE IF EXISTS `network`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `network`
--

LOCK TABLES `network` WRITE;
/*!40000 ALTER TABLE `network` DISABLE KEYS */;
/*!40000 ALTER TABLE `network` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `network_topology`
--

DROP TABLE IF EXISTS `network_topology`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `network_topology`
--

LOCK TABLES `network_topology` WRITE;
/*!40000 ALTER TABLE `network_topology` DISABLE KEYS */;
/*!40000 ALTER TABLE `network_topology` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `nodeport`
--

DROP TABLE IF EXISTS `nodeport`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `nodeport` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `nodeport` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `nodeport` (`nodeport`)
) ENGINE=InnoDB AUTO_INCREMENT=52 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `nodeport`
--

LOCK TABLES `nodeport` WRITE;
/*!40000 ALTER TABLE `nodeport` DISABLE KEYS */;
INSERT INTO `nodeport` VALUES (1,'30101'),(2,'30102'),(3,'30103'),(4,'30104'),(5,'30105'),(6,'30106'),(7,'30107'),(8,'30108'),(9,'30109'),(10,'30110'),(11,'30111'),(12,'30112'),(13,'30113'),(14,'30114'),(15,'30115'),(16,'30116'),(17,'30117'),(18,'30118'),(19,'30119'),(20,'30120'),(21,'30121'),(22,'30122'),(23,'30123'),(24,'30124'),(25,'30125'),(26,'30126'),(27,'30127'),(28,'30128'),(29,'30129'),(30,'30130'),(31,'30131'),(32,'30132'),(33,'30133'),(34,'30134'),(35,'30135'),(36,'30136'),(37,'30137'),(38,'30138'),(39,'30139'),(40,'30140'),(41,'30141'),(42,'30142'),(43,'30143'),(44,'30144'),(45,'30145'),(46,'30146'),(47,'30147'),(48,'30148'),(49,'30149'),(50,'30150'),(51,'30151'),(52,'30152'),(53,'30153'),(54,'30154'),(55,'30155'),(56,'30156'),(57,'30157'),(58,'30158'),(59,'30159'),(60,'30160'),(61,'30161'),(62,'30162'),(63,'30163'),(64,'30164'),(65,'30165'),(66,'30166'),(67,'30167'),(68,'30168'),(69,'30169'),(70,'30170'),(71,'30171'),(72,'30172'),(73,'30173'),(74,'30174'),(75,'30175'),(76,'30176'),(77,'30177'),(78,'30178'),(79,'30179'),(80,'30180'),(81,'30181'),(82,'30182'),(83,'30183'),(84,'30184'),(85,'30185'),(86,'30186'),(87,'30187'),(88,'30188'),(89,'30189'),(90,'30190'),(91,'30191'),(92,'30192'),(93,'30193'),(94,'30194'),(95,'30195'),(96,'30196'),(97,'30197'),(98,'30198'),(99,'30199'),(100,'30200'),(101,'30201'),(102,'30202'),(103,'30203'),(104,'30204'),(105,'30205'),(106,'30206'),(107,'30207'),(108,'30208'),(109,'30209'),(110,'30210'),(111,'30211'),(112,'30212'),(113,'30213'),(114,'30214'),(115,'30215'),(116,'30216'),(117,'30217'),(118,'30218'),(119,'30219'),(120,'30220'),(121,'30221'),(122,'30222'),(123,'30223'),(124,'30224'),(125,'30225'),(126,'30226'),(127,'30227'),(128,'30228'),(129,'30229'),(130,'30230'),(131,'30231'),(132,'30232'),(133,'30233'),(134,'30234'),(135,'30235'),(136,'30236'),(137,'30237'),(138,'30238'),(139,'30239'),(140,'30240'),(141,'30241'),(142,'30242'),(143,'30243'),(144,'30244'),(145,'30245'),(146,'30246'),(147,'30247'),(148,'30248'),(149,'30249'),(150,'30250'),(151,'30251'),(152,'30252'),(153,'30253'),(154,'30254'),(155,'30255'),(156,'30256'),(157,'30257'),(158,'30258'),(159,'30259'),(160,'30260'),(161,'30261'),(162,'30262'),(163,'30263'),(164,'30264'),(165,'30265'),(166,'30266'),(167,'30267'),(168,'30268'),(169,'30269'),(170,'30270'),(171,'30271'),(172,'30272'),(173,'30273'),(174,'30274'),(175,'30275'),(176,'30276'),(177,'30277'),(178,'30278'),(179,'30279'),(180,'30280'),(181,'30281'),(182,'30282'),(183,'30283'),(184,'30284'),(185,'30285'),(186,'30286'),(187,'30287'),(188,'30288'),(189,'30289'),(190,'30290'),(191,'30291'),(192,'30292'),(193,'30293'),(194,'30294'),(195,'30295'),(196,'30296'),(197,'30297'),(198,'30298'),(199,'30299'),(200,'30300');
/*!40000 ALTER TABLE `nodeport` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `nodeport_cluster`
--

DROP TABLE IF EXISTS `nodeport_cluster`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `nodeport_cluster` (
  `nodeportId` int(11) NOT NULL,
  `clusterId` int(11) NOT NULL,
  `status` int(11) NOT NULL,
  KEY `cluster` (`clusterId`),
  KEY `port` (`nodeportId`),
  CONSTRAINT `cluster` FOREIGN KEY (`clusterId`) REFERENCES `cluster` (`id`),
  CONSTRAINT `port` FOREIGN KEY (`nodeportId`) REFERENCES `nodeport` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `nodeport_cluster`
--

LOCK TABLES `nodeport_cluster` WRITE;
/*!40000 ALTER TABLE `nodeport_cluster` DISABLE KEYS */;
/*!40000 ALTER TABLE `nodeport_cluster` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `private_partition`
--

DROP TABLE IF EXISTS `private_partition`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `private_partition` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `tenant_id` varchar(255) DEFAULT NULL,
  `tenant_name` varchar(255) NOT NULL,
  `namespace` varchar(255) NOT NULL,
  `is_private` int(10) NOT NULL DEFAULT '0' COMMENT '是否是私有分区，0共享分区，1表示私有分区',
  `label` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `private_partition`
--

LOCK TABLES `private_partition` WRITE;
/*!40000 ALTER TABLE `private_partition` DISABLE KEYS */;
/*!40000 ALTER TABLE `private_partition` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `resource`
--

/*
 Navicat Premium Data Transfer

 Source Server         : 10.10.101.74
 Source Server Type    : MySQL
 Source Server Version : 50635
 Source Host           : 10.10.101.74:30306
 Source Schema         : k8s_auth_server

 Target Server Type    : MySQL
 Target Server Version : 50635
 File Encoding         : 65001

 Date: 16/09/2017 14:47:16
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for resource
-- ----------------------------
DROP TABLE IF EXISTS `resource`;
CREATE TABLE `resource`  (
  `id` int(10) NOT NULL AUTO_INCREMENT COMMENT '序号',
  `name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '资源名称',
  `type` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT 'menu' COMMENT '类型',
  `url` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '资源路径',
  `parent_id` int(10) DEFAULT 0 COMMENT '父节点',
  `parent_ids` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '父节点字符串',
  `weight` int(100) DEFAULT 0 COMMENT '权重',
  `create_time` datetime(0) DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(0) DEFAULT NULL COMMENT '更新时间',
  `available` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否可用 1可用 0不可用',
  `trans_name` varchar(45) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '译名',
  `icon_name` varchar(45) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '图标名',
  `role` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  `isParent` int(1) DEFAULT NULL,
  `parent_rpid` int(100) DEFAULT NULL,
  `rpid` int(100) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 163 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of resource
-- ----------------------------
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (1, '总览', 'menu', 'overview', 0, NULL, 1, NULL, '2017-09-13 14:35:02', 1, NULL, 'menu-icon mi-overview', 'admin', 1, 0, 1);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (2, '集群', 'menu', 'cluster', 0, NULL, 2, NULL, '2017-09-13 14:35:02', 1, NULL, 'menu-icon mi-cluster', 'admin', 1, 0, 2);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (3, '租户管理', 'menu', 'tenant', 0, NULL, 3, NULL, '2017-09-13 14:35:02', 1, NULL, 'menu-icon mi-tenant', 'admin', 1, 0, 3);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (4, '应用中心', 'menu', '', 0, NULL, 4, NULL, '2017-09-13 14:35:02', 1, NULL, 'menu-icon mi-application', 'admin', 1, 0, 4);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (5, 'CICD', 'menu', '', 0, NULL, 5, NULL, '2017-09-13 14:35:03', 1, NULL, 'menu-icon mi-cicd', 'admin', 1, 0, 5);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (6, '交付中心', 'menu', '', 0, NULL, 6, NULL, '2017-09-13 14:35:03', 1, NULL, 'menu-icon mi-centers', 'admin', 1, 0, 6);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (7, '日志管理', 'menu', '', 0, NULL, 8, NULL, '2017-09-13 14:35:03', 1, NULL, 'menu-icon mi-audit', 'admin', 1, 0, 7);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (8, '告警中心', 'menu', '', 0, NULL, 7, NULL, '2017-09-13 14:35:03', 1, NULL, 'menu-icon mi-alarm', 'admin', 1, 0, 8);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (9, '应用', 'menu', 'manageList', 4, '4/9', 9, NULL, NULL, 1, NULL, '', 'admin', 0, 4, 9);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (10, '服务', 'menu', 'manageApplyList', 4, '4/10', 10, NULL, NULL, 1, NULL, '', 'admin', 0, 4, 10);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (11, '外部服务', 'menu', 'externalService', 4, '4/11', 11, NULL, NULL, 1, NULL, '', 'admin', 0, 4, 11);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (12, '配置中心', 'menu', 'configcenter', 4, '4/12', 12, NULL, NULL, 1, NULL, '', 'admin', 0, 4, 12);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (13, '存储', 'menu', 'storageScheme', 4, '4/13', 13, NULL, NULL, 1, NULL, '', 'admin', 0, 4, 13);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (14, '批量任务', 'menu', 'job', 4, '4/14', 14, NULL, NULL, 0, NULL, '', 'admin', 0, 4, 14);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (15, 'Docker file', 'menu', 'dockerfileList', 5, '5/15', 15, NULL, NULL, 1, NULL, NULL, 'admin', 0, 5, 15);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (16, '流水线', 'menu', 'pipelineList', 5, '5/16', 16, NULL, NULL, 1, NULL, NULL, 'admin', 0, 5, 16);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (17, '依赖管理', 'menu', 'dependenceList', 5, '5/17', 17, NULL, NULL, 1, NULL, '', 'admin', 0, 5, 17);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (18, '镜像仓库', 'menu', 'mirrorContent', 6, '6/18', 18, NULL, NULL, 1, NULL, '', 'admin', 0, 6, 18);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (19, '应用商店', 'menu', 'deliveryStore', 6, '6/19', 19, NULL, NULL, 1, NULL, '', 'admin', 0, 6, 19);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (20, '模板管理', 'menu', 'manageAll', 6, '6/20', 20, NULL, NULL, 1, NULL, '', 'admin', 0, 6, 20);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (21, '操作审计', 'menu', 'audit', 7, '7/21', 21, NULL, NULL, 1, NULL, '', 'admin', 0, 7, 21);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (22, '日志查询', 'menu', 'logQuery', 7, '7/22', 22, NULL, NULL, 0, NULL, '', 'admin', 0, 7, 22);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (23, '告警规则', 'menu', 'alarmList', 8, '8/23', 23, NULL, NULL, 1, NULL, '', 'admin', 0, 8, 23);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (24, '告警处理中心', 'menu', 'alarmHandingList', 8, '8/24', 24, NULL, NULL, 1, NULL, '', 'admin', 0, 8, 24);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (25, '系统设置', 'menu', 'system', 0, '', 9, NULL, '2017-09-13 14:35:03', 1, NULL, 'menu-icon mi-config', 'admin', 1, 0, 25);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (33, '我的租户', 'menu', 'tenant', 0, NULL, 25, NULL, '2017-09-13 11:07:53', 1, NULL, 'menu-icon mi-tenant', 'dev', 1, 0, 1);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (34, '应用中心', 'menu', '', 0, NULL, 1, NULL, '2017-09-13 11:14:40', 1, NULL, 'menu-icon mi-application', 'dev', 1, 0, 2);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (35, 'CICD', 'menu', '', 0, NULL, 3, NULL, '2017-09-13 16:53:06', 1, NULL, 'menu-icon mi-cicd', 'dev', 1, 0, 3);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (36, '交付中心', 'menu', '', 0, NULL, 2, NULL, '2017-09-13 10:22:57', 1, NULL, 'menu-icon mi-centers', 'dev', 1, 0, 4);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (37, '日志管理', 'menu', '', 0, NULL, 4, NULL, '2017-09-13 16:53:06', 1, NULL, 'menu-icon mi-audit', 'dev', 1, 0, 5);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (38, '告警中心', 'menu', '', 0, NULL, 5, NULL, '2017-09-13 16:53:06', 1, NULL, 'menu-icon mi-alarm', 'dev', 1, 0, 6);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (39, '应用', 'menu', 'manageList', 2, '34/39', 31, NULL, NULL, 1, NULL, '', 'dev', 0, 2, 7);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (40, '服务', 'menu', 'manageApplyList', 2, '34/40', 32, NULL, NULL, 1, NULL, '', 'dev', 0, 2, 8);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (41, '外部服务', 'menu', 'externalService', 2, '34/41', 33, NULL, NULL, 1, NULL, '', 'dev', 0, 2, 9);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (42, '配置中心', 'menu', 'configcenter', 2, '34/42', 34, NULL, NULL, 1, NULL, '', 'dev', 0, 2, 10);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (43, '存储', 'menu', 'storageScheme', 2, '34/43', 35, NULL, NULL, 1, NULL, '', 'dev', 0, 2, 11);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (44, '批量任务', 'menu', 'job', 2, '34/44', 36, NULL, NULL, 0, NULL, '', 'dev', 0, 2, 12);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (45, 'Docker file', 'menu', 'dockerfileList', 3, '35/45', 37, NULL, NULL, 1, NULL, NULL, 'dev', 0, 3, 13);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (46, '流水线', 'menu', 'pipelineList', 3, '35/46', 38, NULL, NULL, 1, NULL, NULL, 'dev', 0, 3, 14);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (47, '依赖管理', 'menu', 'dependenceList', 3, '35/47', 39, NULL, NULL, 1, NULL, '', 'dev', 0, 3, 15);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (48, '镜像仓库', 'menu', 'mirrorContent', 4, '36/48', 40, NULL, NULL, 1, NULL, '', 'dev', 0, 4, 16);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (49, '应用商店', 'menu', 'deliveryStore', 4, '36/49', 41, NULL, NULL, 1, NULL, '', 'dev', 0, 4, 17);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (50, '模板管理', 'menu', 'manageAll', 4, '36/50', 42, NULL, NULL, 1, NULL, '', 'dev', 0, 4, 18);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (51, '操作审计', 'menu', 'logQuery', 5, '37/51', 43, NULL, '2017-08-22 17:09:15', 0, NULL, '', 'dev', 0, 5, 19);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (52, '告警规则', 'menu', 'alarmList', 6, '38/52', 44, NULL, NULL, 1, NULL, '', 'dev', 0, 6, 20);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (53, '告警处理中心', 'menu', 'alarmHandingList', 6, '38/53', 45, NULL, NULL, 1, NULL, '', 'dev', 0, 6, 21);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (54, '系统设置', 'menu', 'system', 0, '', 6, NULL, '2017-09-13 16:53:06', 0, NULL, 'menu-icon mi-config', 'dev', 1, 0, 22);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (63, '我的租户', 'menu', 'tenant', 0, NULL, 25, NULL, '2017-09-12 15:50:55', 1, NULL, 'menu-icon mi-tenant', 'ops', 1, 0, 26);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (64, '应用中心', 'menu', '', 0, NULL, 26, NULL, '2017-08-26 16:50:06', 1, NULL, 'menu-icon mi-application', 'ops', 1, 0, 4);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (65, 'CICD', 'menu', '', 0, NULL, 27, NULL, '2017-08-26 16:50:06', 1, NULL, 'menu-icon mi-cicd', 'ops', 1, 0, 5);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (66, '交付中心', 'menu', '', 0, NULL, 28, NULL, '2017-08-26 16:50:06', 1, NULL, 'menu-icon mi-centers', 'ops', 1, 0, 6);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (67, '日志管理', 'menu', '', 0, NULL, 29, NULL, '2017-08-22 17:09:15', 1, NULL, 'menu-icon mi-audit', 'ops', 1, 0, 7);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (68, '告警中心', 'menu', '', 0, NULL, 30, NULL, '2017-08-26 16:50:33', 1, NULL, 'menu-icon mi-alarm', 'ops', 1, 0, 8);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (69, '应用', 'menu', 'manageList', 34, '34/39', 31, NULL, NULL, 1, NULL, '', 'ops', 0, 4, 9);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (70, '服务', 'menu', 'manageApplyList', 34, '34/40', 32, NULL, NULL, 1, NULL, '', 'ops', 0, 4, 10);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (71, '外部服务', 'menu', 'externalService', 34, '34/41', 33, NULL, NULL, 1, NULL, '', 'ops', 0, 4, 11);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (72, '配置中心', 'menu', 'configcenter', 34, '34/42', 34, NULL, NULL, 1, NULL, '', 'ops', 0, 4, 12);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (73, '存储', 'menu', 'storageScheme', 34, '34/43', 35, NULL, NULL, 1, NULL, '', 'ops', 0, 4, 13);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (74, '批量任务', 'menu', 'job', 34, '34/44', 36, NULL, NULL, 0, NULL, '', 'ops', 0, 4, 14);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (75, 'Docker file', 'menu', 'dockerfileList', 35, '35/45', 37, NULL, NULL, 1, NULL, NULL, 'ops', 0, 5, 15);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (76, '流水线', 'menu', 'pipelineList', 35, '35/46', 38, NULL, NULL, 1, NULL, NULL, 'ops', 0, 5, 16);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (77, '依赖管理', 'menu', 'dependenceList', 35, '35/47', 39, NULL, NULL, 1, NULL, '', 'ops', 0, 5, 17);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (78, '镜像仓库', 'menu', 'mirrorContent', 36, '36/48', 40, NULL, NULL, 1, NULL, '', 'ops', 0, 6, 18);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (79, '应用商店', 'menu', 'deliveryStore', 36, '36/49', 41, NULL, NULL, 1, NULL, '', 'ops', 0, 6, 19);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (80, '模板管理', 'menu', 'manageAll', 36, '36/50', 42, NULL, NULL, 1, NULL, '', 'ops', 0, 6, 20);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (81, '操作审计', 'menu', 'logQuery', 37, '37/51', 43, NULL, '2017-08-22 17:09:15', 0, NULL, '', 'ops', 0, 7, 21);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (82, '告警规则', 'menu', 'alarmList', 38, '38/52', 44, NULL, NULL, 1, NULL, '', 'ops', 0, 8, 23);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (83, '告警处理中心', 'menu', 'alarmHandingList', 38, '38/53', 45, NULL, NULL, 1, NULL, '', 'ops', 0, 8, 24);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (84, '总览', 'menu', 'overview', 0, NULL, 1, NULL, NULL, 0, NULL, 'menu-icon mi-overview', 'default', 1, 0, 1);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (85, '集群', 'menu', 'cluster', 0, NULL, 2, NULL, NULL, 0, NULL, 'menu-icon mi-cluster', 'default', 1, 0, 2);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (87, '应用中心', 'menu', '', 0, NULL, 4, NULL, NULL, 0, NULL, 'menu-icon mi-application', 'default', 1, 0, 4);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (88, 'CICD', 'menu', '', 0, NULL, 5, NULL, NULL, 0, NULL, 'menu-icon mi-cicd', 'default', 1, 0, 5);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (89, '交付中心', 'menu', '', 0, NULL, 6, NULL, NULL, 0, NULL, 'menu-icon mi-centers', 'default', 1, 0, 6);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (90, '日志管理', 'menu', '', 0, NULL, 7, NULL, NULL, 0, NULL, 'menu-icon mi-audit', 'default', 1, 0, 7);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (91, '告警中心', 'menu', '', 0, NULL, 8, NULL, NULL, 0, NULL, 'menu-icon mi-alarm', 'default', 1, 0, 8);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (92, '应用', 'menu', 'manageList', 0, '4/9', 9, NULL, NULL, 0, NULL, NULL, 'default', 0, 4, 9);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (93, '服务', 'menu', 'manageApplyList', 0, '4/10', 10, NULL, NULL, 0, NULL, NULL, 'default', 0, 4, 10);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (94, '外部服务', 'menu', 'externalService', 0, '4/11', 11, NULL, NULL, 0, NULL, NULL, 'default', 0, 4, 11);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (95, '系统设置', 'menu', 'system', 0, '', 46, NULL, NULL, 0, NULL, 'menu-icon mi-config', 'ops', 1, 0, 22);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (96, '配置中心', 'menu', 'configcenter', 0, '4/12', 12, NULL, NULL, 0, NULL, NULL, 'default', 0, 4, 12);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (97, '存储', 'menu', 'storageScheme', 0, '4/13', 13, NULL, NULL, 0, NULL, NULL, 'default', 0, 4, 13);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (98, '批量任务', 'menu', 'job', 0, '4/14', 14, NULL, NULL, 0, NULL, NULL, 'default', 0, 4, 14);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (99, 'Docker file', 'menu', 'dockerfileList', 0, '5/15', 15, NULL, NULL, 0, NULL, NULL, 'default', 0, 5, 15);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (100, '流水线', 'menu', 'pipelineList', 0, '5/16', 16, NULL, NULL, 0, NULL, NULL, 'default', 0, 5, 16);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (101, '依赖管理', 'menu', 'dependenceList', 0, '5/17', 17, NULL, NULL, 0, NULL, NULL, 'default', 0, 5, 17);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (102, '镜像管理', 'menu', 'mirrorContent', 0, '6/18', 18, NULL, NULL, 0, NULL, NULL, 'default', 0, 6, 18);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (103, '应用商店', 'menu', 'deliveryStore', 0, '6/19', 19, NULL, NULL, 0, NULL, NULL, 'default', 0, 6, 19);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (104, '模板管理', 'menu', 'manageAll', 0, '6/20', 20, NULL, NULL, 0, NULL, NULL, 'default', 0, 6, 20);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (105, '操作审计', 'menu', 'audit', 0, '7/21', 21, NULL, NULL, 0, NULL, NULL, 'default', 0, 7, 21);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (106, '日志查询', 'menu', 'logQuery', 0, '7/22', 22, NULL, NULL, 0, NULL, NULL, 'default', 0, 7, 22);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (107, '告警规则', 'menu', 'alarmList', 0, '8/23', 23, NULL, NULL, 0, NULL, NULL, 'default', 0, 8, 23);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (108, '告警处理中心', 'menu', 'alarmHandingList', 0, '8/24', 24, NULL, NULL, 0, NULL, NULL, 'default', 0, 8, 24);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (109, '系统设置', 'menu', 'system', 0, NULL, 45, NULL, NULL, 0, NULL, 'menu-icon mi-config', 'default', 1, 0, 25);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (110, '我的租户', 'menu', 'tenant', 0, NULL, 3, NULL, NULL, 0, NULL, 'menu-icon mi-tenant', 'default', 1, 0, 26);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (111, '我的租户', 'menu', 'tenant', 0, NULL, 25, NULL, '2017-09-13 10:34:01', 1, NULL, 'menu-icon mi-tenant', 'tm', 1, 0, 1);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (112, '应用中心', 'menu', '', 0, NULL, 26, NULL, '2017-09-13 10:34:01', 1, NULL, 'menu-icon mi-application', 'tm', 1, 0, 2);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (113, 'CICD', 'menu', '', 0, NULL, 27, NULL, '2017-09-13 11:10:19', 1, NULL, 'menu-icon mi-cicd', 'tm', 1, 0, 3);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (114, '交付中心', 'menu', '', 0, NULL, 28, NULL, NULL, 1, NULL, 'menu-icon mi-centers', 'tm', 1, 0, 4);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (115, '日志管理', 'menu', '', 0, NULL, 29, NULL, '2017-09-13 16:02:57', 1, NULL, 'menu-icon mi-audit', 'tm', 1, 0, 5);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (116, '告警中心', 'menu', '', 0, NULL, 30, NULL, '2017-09-13 10:34:01', 1, NULL, 'menu-icon mi-alarm', 'tm', 1, 0, 6);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (117, '应用', 'menu', 'manageList', 2, '34/39', 31, NULL, NULL, 1, NULL, '', 'tm', 0, 2, 7);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (118, '服务', 'menu', 'manageApplyList', 2, '34/40', 32, NULL, NULL, 1, NULL, '', 'tm', 0, 2, 8);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (119, '外部服务', 'menu', 'externalService', 2, '34/41', 33, NULL, NULL, 1, NULL, '', 'tm', 0, 2, 9);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (120, '配置中心', 'menu', 'configcenter', 2, '34/42', 34, NULL, NULL, 1, NULL, '', 'tm', 0, 2, 10);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (121, '存储', 'menu', 'storageScheme', 2, '34/43', 35, NULL, NULL, 1, NULL, '', 'tm', 0, 2, 11);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (122, '批量任务', 'menu', 'job', 2, '34/44', 36, NULL, NULL, 0, NULL, '', 'tm', 0, 2, 12);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (123, 'Docker file', 'menu', 'dockerfileList', 3, '35/45', 37, NULL, NULL, 1, NULL, NULL, 'tm', 0, 3, 13);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (124, '流水线', 'menu', 'pipelineList', 3, '35/46', 38, NULL, NULL, 1, NULL, NULL, 'tm', 0, 3, 14);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (125, '依赖管理', 'menu', 'dependenceList', 3, '35/47', 39, NULL, NULL, 1, NULL, '', 'tm', 0, 3, 15);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (126, '镜像仓库', 'menu', 'mirrorContent', 4, '36/48', 40, NULL, NULL, 1, NULL, '', 'tm', 0, 4, 16);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (127, '应用商店', 'menu', 'deliveryStore', 4, '36/49', 41, NULL, NULL, 1, NULL, '', 'tm', 0, 4, 17);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (128, '模板管理', 'menu', 'manageAll', 4, '36/50', 42, NULL, NULL, 1, NULL, '', 'tm', 0, 4, 18);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (129, '操作审计', 'menu', 'logQuery', 5, '37/51', 43, NULL, '2017-08-22 17:09:15', 1, NULL, '', 'tm', 0, 5, 19);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (130, '告警规则', 'menu', 'alarmList', 6, '38/52', 44, NULL, NULL, 1, NULL, '', 'tm', 0, 6, 20);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (131, '告警处理中心', 'menu', 'alarmHandingList', 6, '38/53', 45, NULL, NULL, 1, NULL, '', 'tm', 0, 6, 21);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (132, '系统设置', 'menu', 'system', 0, '', 45, NULL, '2017-09-13 16:02:57', 0, NULL, 'menu-icon mi-config', 'tm', 1, 0, 22);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (133, '告警规则', 'menu', 'alarmList', 0, '8/23', 23, NULL, NULL, 0, NULL, NULL, 'tm', 0, 8, 23);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (134, '告警处理中心', 'menu', 'alarmHandingList', 0, '8/24', 24, NULL, NULL, 0, NULL, NULL, 'tm', 0, 8, 24);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (135, '系统设置', 'menu', 'system', 0, NULL, 45, NULL, NULL, 0, NULL, 'menu-icon mi-config', 'tm', 1, 0, 25);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (137, '总览', 'menu', 'overview', 0, NULL, 1, NULL, NULL, 0, NULL, 'menu-icon mi-overview', 'tester', 1, 0, 1);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (138, '集群', 'menu', 'cluster', 0, NULL, 2, NULL, NULL, 0, NULL, 'menu-icon mi-cluster', 'tester', 1, 0, 2);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (139, '租户管理', 'menu', 'tenant', 0, NULL, 1, NULL, NULL, 0, NULL, 'menu-icon mi-tenant', 'tester', 1, 0, 3);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (140, '应用中心', 'menu', '', 0, NULL, 4, NULL, '2017-09-11 14:47:21', 1, NULL, 'menu-icon mi-application', 'tester', 1, 0, 4);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (141, 'CICD', 'menu', '', 0, NULL, 5, NULL, '2017-09-12 19:49:01', 1, NULL, 'menu-icon mi-cicd', 'tester', 1, 0, 5);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (142, '交付中心', 'menu', '', 0, NULL, 6, NULL, '2017-09-11 14:37:32', 1, NULL, 'menu-icon mi-centers', 'tester', 1, 0, 6);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (143, '日志管理', 'menu', '', 0, NULL, 7, NULL, '2017-09-12 19:49:01', 1, NULL, 'menu-icon mi-audit', 'tester', 1, 0, 7);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (144, '告警中心', 'menu', '', 0, NULL, 8, NULL, '2017-09-13 16:54:01', 1, NULL, 'menu-icon mi-alarm', 'tester', 1, 0, 8);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (145, '应用', 'menu', 'manageList', 0, '4/9', 9, NULL, NULL, 1, NULL, NULL, 'tester', 0, 4, 9);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (146, '服务', 'menu', 'manageApplyList', 0, '4/10', 10, NULL, NULL, 1, NULL, NULL, 'tester', 0, 4, 10);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (147, '外部服务', 'menu', 'externalService', 0, '4/11', 11, NULL, NULL, 1, NULL, NULL, 'tester', 0, 4, 11);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (148, '配置中心', 'menu', 'configcenter', 0, '4/12', 12, NULL, NULL, 1, NULL, NULL, 'tester', 0, 4, 12);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (149, '存储', 'menu', 'storageScheme', 0, '4/13', 13, NULL, NULL, 1, NULL, NULL, 'tester', 0, 4, 13);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (150, '批量任务', 'menu', 'job', 0, '4/14', 14, NULL, NULL, 0, NULL, NULL, 'tester', 0, 4, 14);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (151, 'Docker file', 'menu', 'dockerfileList', 0, '5/15', 15, NULL, NULL, 1, NULL, NULL, 'tester', 0, 5, 15);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (152, '流水线', 'menu', 'pipelineList', 0, '5/16', 16, NULL, NULL, 1, NULL, NULL, 'tester', 0, 5, 16);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (153, '依赖管理', 'menu', 'dependenceList', 0, '5/17', 17, NULL, NULL, 1, NULL, NULL, 'tester', 0, 5, 17);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (154, '镜像管理', 'menu', 'mirrorContent', 0, '6/18', 18, NULL, NULL, 1, NULL, NULL, 'tester', 0, 6, 18);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (155, '应用商店', 'menu', 'deliveryStore', 0, '6/19', 19, NULL, NULL, 1, NULL, NULL, 'tester', 0, 6, 19);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (156, '模板管理', 'menu', 'manageAll', 0, '6/20', 20, NULL, NULL, 1, NULL, NULL, 'tester', 0, 6, 20);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (157, '操作审计', 'menu', 'audit', 0, '7/21', 21, NULL, NULL, 1, NULL, NULL, 'tester', 0, 7, 21);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (158, '日志查询', 'menu', 'logQuery', 0, '7/22', 22, NULL, NULL, 0, NULL, NULL, 'tester', 0, 7, 22);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (159, '告警规则', 'menu', 'alarmList', 0, '8/23', 23, NULL, NULL, 1, NULL, NULL, 'tester', 0, 8, 23);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (160, '告警处理中心', 'menu', 'alarmHandingList', 0, '8/24', 24, NULL, NULL, 1, NULL, NULL, 'tester', 0, 8, 24);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (161, '系统设置', 'menu', 'system', 0, NULL, 45, NULL, '2017-09-12 18:41:11', 0, NULL, 'menu-icon mi-config', 'tester', 1, 0, 25);
INSERT INTO `k8s_auth_server`.`resource`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (162, '我的租户', 'menu', 'tenant', 0, NULL, 3, NULL, '2017-09-11 14:37:32', 1, NULL, 'menu-icon mi-tenant', 'tester', 1, 0, 26);

SET FOREIGN_KEY_CHECKS = 1;

--
-- Table structure for table `resource_custom`
--

DROP TABLE IF EXISTS `resource_custom`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `resource_custom` (
  `id` int(10) NOT NULL AUTO_INCREMENT COMMENT '序号',
  `name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '资源名称',
  `type` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT 'menu' COMMENT '类型',
  `url` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '资源路径',
  `parent_id` int(10) DEFAULT 0 COMMENT '父节点',
  `parent_ids` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '父节点字符串',
  `weight` int(100) DEFAULT 0 COMMENT '权重',
  `create_time` datetime(0) DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(0) DEFAULT NULL COMMENT '更新时间',
  `available` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否可用 1可用 0不可用',
  `trans_name` varchar(45) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '译名',
  `icon_name` varchar(45) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '图标名',
  `role` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  `isParent` int(1) DEFAULT NULL,
  `parent_rpid` int(100) DEFAULT NULL,
  `rpid` int(100) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `resource_custom`
--

LOCK TABLES `resource_custom` WRITE;
/*!40000 ALTER TABLE `resource_custom` DISABLE KEYS */;
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (1, '总览', 'menu', 'overview', 0, NULL, 1, NULL, '2017-09-13 14:35:02', 1, NULL, 'menu-icon mi-overview', 'admin', 1, 0, 1);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (2, '集群', 'menu', 'cluster', 0, NULL, 2, NULL, '2017-09-13 14:35:02', 1, NULL, 'menu-icon mi-cluster', 'admin', 1, 0, 2);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (3, '租户管理', 'menu', 'tenant', 0, NULL, 3, NULL, '2017-09-13 14:35:02', 1, NULL, 'menu-icon mi-tenant', 'admin', 1, 0, 3);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (4, '应用中心', 'menu', '', 0, NULL, 4, NULL, '2017-09-13 14:35:02', 1, NULL, 'menu-icon mi-application', 'admin', 1, 0, 4);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (5, 'CICD', 'menu', '', 0, NULL, 5, NULL, '2017-09-13 14:35:03', 1, NULL, 'menu-icon mi-cicd', 'admin', 1, 0, 5);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (6, '交付中心', 'menu', '', 0, NULL, 6, NULL, '2017-09-13 14:35:03', 1, NULL, 'menu-icon mi-centers', 'admin', 1, 0, 6);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (7, '日志管理', 'menu', '', 0, NULL, 8, NULL, '2017-09-13 14:35:03', 1, NULL, 'menu-icon mi-audit', 'admin', 1, 0, 7);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (8, '告警中心', 'menu', '', 0, NULL, 7, NULL, '2017-09-13 14:35:03', 1, NULL, 'menu-icon mi-alarm', 'admin', 1, 0, 8);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (9, '应用', 'menu', 'manageList', 4, '4/9', 9, NULL, NULL, 1, NULL, '', 'admin', 0, 4, 9);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (10, '服务', 'menu', 'manageApplyList', 4, '4/10', 10, NULL, NULL, 1, NULL, '', 'admin', 0, 4, 10);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (11, '外部服务', 'menu', 'externalService', 4, '4/11', 11, NULL, NULL, 1, NULL, '', 'admin', 0, 4, 11);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (12, '配置中心', 'menu', 'configcenter', 4, '4/12', 12, NULL, NULL, 1, NULL, '', 'admin', 0, 4, 12);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (13, '存储', 'menu', 'storageScheme', 4, '4/13', 13, NULL, NULL, 1, NULL, '', 'admin', 0, 4, 13);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (14, '批量任务', 'menu', 'job', 4, '4/14', 14, NULL, NULL, 0, NULL, '', 'admin', 0, 4, 14);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (15, 'Docker file', 'menu', 'dockerfileList', 5, '5/15', 15, NULL, NULL, 1, NULL, NULL, 'admin', 0, 5, 15);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (16, '流水线', 'menu', 'pipelineList', 5, '5/16', 16, NULL, NULL, 1, NULL, NULL, 'admin', 0, 5, 16);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (17, '依赖管理', 'menu', 'dependenceList', 5, '5/17', 17, NULL, NULL, 1, NULL, '', 'admin', 0, 5, 17);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (18, '镜像仓库', 'menu', 'mirrorContent', 6, '6/18', 18, NULL, NULL, 1, NULL, '', 'admin', 0, 6, 18);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (19, '应用商店', 'menu', 'deliveryStore', 6, '6/19', 19, NULL, NULL, 1, NULL, '', 'admin', 0, 6, 19);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (20, '模板管理', 'menu', 'manageAll', 6, '6/20', 20, NULL, NULL, 1, NULL, '', 'admin', 0, 6, 20);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (21, '操作审计', 'menu', 'audit', 7, '7/21', 21, NULL, NULL, 1, NULL, '', 'admin', 0, 7, 21);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (22, '日志查询', 'menu', 'logQuery', 7, '7/22', 22, NULL, NULL, 0, NULL, '', 'admin', 0, 7, 22);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (23, '告警规则', 'menu', 'alarmList', 8, '8/23', 23, NULL, NULL, 1, NULL, '', 'admin', 0, 8, 23);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (24, '告警处理中心', 'menu', 'alarmHandingList', 8, '8/24', 24, NULL, NULL, 1, NULL, '', 'admin', 0, 8, 24);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (25, '系统设置', 'menu', 'system', 0, '', 9, NULL, '2017-09-13 14:35:03', 1, NULL, 'menu-icon mi-config', 'admin', 1, 0, 25);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (33, '我的租户', 'menu', 'tenant', 0, NULL, 1, NULL, '2017-09-13 11:07:53', 1, NULL, 'menu-icon mi-tenant', 'dev', 1, 0, 1);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (34, '应用中心', 'menu', '', 0, NULL, 2, NULL, '2017-09-13 11:14:40', 1, NULL, 'menu-icon mi-application', 'dev', 1, 0, 2);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (35, 'CICD', 'menu', '', 0, NULL, 3, NULL, '2017-09-13 16:53:06', 1, NULL, 'menu-icon mi-cicd', 'dev', 1, 0, 3);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (36, '交付中心', 'menu', '', 0, NULL, 2, NULL, '2017-09-13 10:22:57', 1, NULL, 'menu-icon mi-centers', 'dev', 1, 0, 4);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (37, '日志管理', 'menu', '', 0, NULL, 4, NULL, '2017-09-13 16:53:06', 1, NULL, 'menu-icon mi-audit', 'dev', 1, 0, 5);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (38, '告警中心', 'menu', '', 0, NULL, 5, NULL, '2017-09-13 16:53:06', 1, NULL, 'menu-icon mi-alarm', 'dev', 1, 0, 6);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (39, '应用', 'menu', 'manageList', 2, '34/39', 31, NULL, NULL, 1, NULL, '', 'dev', 0, 2, 7);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (40, '服务', 'menu', 'manageApplyList', 2, '34/40', 32, NULL, NULL, 1, NULL, '', 'dev', 0, 2, 8);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (41, '外部服务', 'menu', 'externalService', 2, '34/41', 33, NULL, NULL, 1, NULL, '', 'dev', 0, 2, 9);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (42, '配置中心', 'menu', 'configcenter', 2, '34/42', 34, NULL, NULL, 1, NULL, '', 'dev', 0, 2, 10);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (43, '存储', 'menu', 'storageScheme', 2, '34/43', 35, NULL, NULL, 1, NULL, '', 'dev', 0, 2, 11);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (44, '批量任务', 'menu', 'job', 2, '34/44', 36, NULL, NULL, 0, NULL, '', 'dev', 0, 2, 12);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (45, 'Docker file', 'menu', 'dockerfileList', 3, '35/45', 37, NULL, NULL, 1, NULL, NULL, 'dev', 0, 3, 13);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (46, '流水线', 'menu', 'pipelineList', 3, '35/46', 38, NULL, NULL, 1, NULL, NULL, 'dev', 0, 3, 14);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (47, '依赖管理', 'menu', 'dependenceList', 3, '35/47', 39, NULL, NULL, 1, NULL, '', 'dev', 0, 3, 15);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (48, '镜像仓库', 'menu', 'mirrorContent', 4, '36/48', 40, NULL, NULL, 1, NULL, '', 'dev', 0, 4, 16);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (49, '应用商店', 'menu', 'deliveryStore', 4, '36/49', 41, NULL, NULL, 1, NULL, '', 'dev', 0, 4, 17);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (50, '模板管理', 'menu', 'manageAll', 4, '36/50', 42, NULL, NULL, 1, NULL, '', 'dev', 0, 4, 18);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (51, '操作审计', 'menu', 'logQuery', 5, '37/51', 43, NULL, '2017-08-22 17:09:15', 0, NULL, '', 'dev', 0, 5, 19);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (52, '告警规则', 'menu', 'alarmList', 6, '38/52', 44, NULL, NULL, 1, NULL, '', 'dev', 0, 6, 20);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (53, '告警处理中心', 'menu', 'alarmHandingList', 6, '38/53', 45, NULL, NULL, 1, NULL, '', 'dev', 0, 6, 21);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (54, '系统设置', 'menu', 'system', 0, '', 6, NULL, '2017-09-13 16:53:06', 0, NULL, 'menu-icon mi-config', 'dev', 1, 0, 22);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (63, '我的租户', 'menu', 'tenant', 0, NULL, 25, NULL, '2017-09-12 15:50:55', 1, NULL, 'menu-icon mi-tenant', 'ops', 1, 0, 26);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (64, '应用中心', 'menu', '', 0, NULL, 26, NULL, '2017-08-26 16:50:06', 1, NULL, 'menu-icon mi-application', 'ops', 1, 0, 4);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (65, 'CICD', 'menu', '', 0, NULL, 27, NULL, '2017-08-26 16:50:06', 1, NULL, 'menu-icon mi-cicd', 'ops', 1, 0, 5);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (66, '交付中心', 'menu', '', 0, NULL, 28, NULL, '2017-08-26 16:50:06', 1, NULL, 'menu-icon mi-centers', 'ops', 1, 0, 6);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (67, '日志管理', 'menu', '', 0, NULL, 29, NULL, '2017-08-22 17:09:15', 1, NULL, 'menu-icon mi-audit', 'ops', 1, 0, 7);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (68, '告警中心', 'menu', '', 0, NULL, 30, NULL, '2017-08-26 16:50:33', 1, NULL, 'menu-icon mi-alarm', 'ops', 1, 0, 8);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (69, '应用', 'menu', 'manageList', 34, '34/39', 31, NULL, NULL, 1, NULL, '', 'ops', 0, 4, 9);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (70, '服务', 'menu', 'manageApplyList', 34, '34/40', 32, NULL, NULL, 1, NULL, '', 'ops', 0, 4, 10);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (71, '外部服务', 'menu', 'externalService', 34, '34/41', 33, NULL, NULL, 1, NULL, '', 'ops', 0, 4, 11);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (72, '配置中心', 'menu', 'configcenter', 34, '34/42', 34, NULL, NULL, 1, NULL, '', 'ops', 0, 4, 12);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (73, '存储', 'menu', 'storageScheme', 34, '34/43', 35, NULL, NULL, 1, NULL, '', 'ops', 0, 4, 13);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (74, '批量任务', 'menu', 'job', 34, '34/44', 36, NULL, NULL, 0, NULL, '', 'ops', 0, 4, 14);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (75, 'Docker file', 'menu', 'dockerfileList', 35, '35/45', 37, NULL, NULL, 1, NULL, NULL, 'ops', 0, 5, 15);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (76, '流水线', 'menu', 'pipelineList', 35, '35/46', 38, NULL, NULL, 1, NULL, NULL, 'ops', 0, 5, 16);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (77, '依赖管理', 'menu', 'dependenceList', 35, '35/47', 39, NULL, NULL, 1, NULL, '', 'ops', 0, 5, 17);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (78, '镜像仓库', 'menu', 'mirrorContent', 36, '36/48', 40, NULL, NULL, 1, NULL, '', 'ops', 0, 6, 18);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (79, '应用商店', 'menu', 'deliveryStore', 36, '36/49', 41, NULL, NULL, 1, NULL, '', 'ops', 0, 6, 19);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (80, '模板管理', 'menu', 'manageAll', 36, '36/50', 42, NULL, NULL, 1, NULL, '', 'ops', 0, 6, 20);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (81, '操作审计', 'menu', 'logQuery', 37, '37/51', 43, NULL, '2017-08-22 17:09:15', 0, NULL, '', 'ops', 0, 7, 21);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (82, '告警规则', 'menu', 'alarmList', 38, '38/52', 44, NULL, NULL, 1, NULL, '', 'ops', 0, 8, 23);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (83, '告警处理中心', 'menu', 'alarmHandingList', 38, '38/53', 45, NULL, NULL, 1, NULL, '', 'ops', 0, 8, 24);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (84, '总览', 'menu', 'overview', 0, NULL, 1, NULL, NULL, 0, NULL, 'menu-icon mi-overview', 'default', 1, 0, 1);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (85, '集群', 'menu', 'cluster', 0, NULL, 2, NULL, NULL, 0, NULL, 'menu-icon mi-cluster', 'default', 1, 0, 2);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (87, '应用中心', 'menu', '', 0, NULL, 4, NULL, NULL, 0, NULL, 'menu-icon mi-application', 'default', 1, 0, 4);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (88, 'CICD', 'menu', '', 0, NULL, 5, NULL, NULL, 0, NULL, 'menu-icon mi-cicd', 'default', 1, 0, 5);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (89, '交付中心', 'menu', '', 0, NULL, 6, NULL, NULL, 0, NULL, 'menu-icon mi-centers', 'default', 1, 0, 6);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (90, '日志管理', 'menu', '', 0, NULL, 7, NULL, NULL, 0, NULL, 'menu-icon mi-audit', 'default', 1, 0, 7);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (91, '告警中心', 'menu', '', 0, NULL, 8, NULL, NULL, 0, NULL, 'menu-icon mi-alarm', 'default', 1, 0, 8);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (92, '应用', 'menu', 'manageList', 0, '4/9', 9, NULL, NULL, 0, NULL, NULL, 'default', 0, 4, 9);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (93, '服务', 'menu', 'manageApplyList', 0, '4/10', 10, NULL, NULL, 0, NULL, NULL, 'default', 0, 4, 10);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (94, '外部服务', 'menu', 'externalService', 0, '4/11', 11, NULL, NULL, 0, NULL, NULL, 'default', 0, 4, 11);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (95, '系统设置', 'menu', 'system', 0, '', 46, NULL, NULL, 0, NULL, 'menu-icon mi-config', 'ops', 1, 0, 22);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (96, '配置中心', 'menu', 'configcenter', 0, '4/12', 12, NULL, NULL, 0, NULL, NULL, 'default', 0, 4, 12);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (97, '存储', 'menu', 'storageScheme', 0, '4/13', 13, NULL, NULL, 0, NULL, NULL, 'default', 0, 4, 13);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (98, '批量任务', 'menu', 'job', 0, '4/14', 14, NULL, NULL, 0, NULL, NULL, 'default', 0, 4, 14);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (99, 'Docker file', 'menu', 'dockerfileList', 0, '5/15', 15, NULL, NULL, 0, NULL, NULL, 'default', 0, 5, 15);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (100, '流水线', 'menu', 'pipelineList', 0, '5/16', 16, NULL, NULL, 0, NULL, NULL, 'default', 0, 5, 16);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (101, '依赖管理', 'menu', 'dependenceList', 0, '5/17', 17, NULL, NULL, 0, NULL, NULL, 'default', 0, 5, 17);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (102, '镜像管理', 'menu', 'mirrorContent', 0, '6/18', 18, NULL, NULL, 0, NULL, NULL, 'default', 0, 6, 18);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (103, '应用商店', 'menu', 'deliveryStore', 0, '6/19', 19, NULL, NULL, 0, NULL, NULL, 'default', 0, 6, 19);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (104, '模板管理', 'menu', 'manageAll', 0, '6/20', 20, NULL, NULL, 0, NULL, NULL, 'default', 0, 6, 20);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (105, '操作审计', 'menu', 'audit', 0, '7/21', 21, NULL, NULL, 0, NULL, NULL, 'default', 0, 7, 21);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (106, '日志查询', 'menu', 'logQuery', 0, '7/22', 22, NULL, NULL, 0, NULL, NULL, 'default', 0, 7, 22);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (107, '告警规则', 'menu', 'alarmList', 0, '8/23', 23, NULL, NULL, 0, NULL, NULL, 'default', 0, 8, 23);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (108, '告警处理中心', 'menu', 'alarmHandingList', 0, '8/24', 24, NULL, NULL, 0, NULL, NULL, 'default', 0, 8, 24);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (109, '系统设置', 'menu', 'system', 0, NULL, 45, NULL, NULL, 0, NULL, 'menu-icon mi-config', 'default', 1, 0, 25);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (110, '我的租户', 'menu', 'tenant', 0, NULL, 3, NULL, NULL, 0, NULL, 'menu-icon mi-tenant', 'default', 1, 0, 26);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (111, '我的租户', 'menu', 'tenant', 0, NULL, 25, NULL, '2017-09-13 10:34:01', 1, NULL, 'menu-icon mi-tenant', 'tm', 1, 0, 1);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (112, '应用中心', 'menu', '', 0, NULL, 26, NULL, '2017-09-13 10:34:01', 1, NULL, 'menu-icon mi-application', 'tm', 1, 0, 2);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (113, 'CICD', 'menu', '', 0, NULL, 27, NULL, '2017-09-13 11:10:19', 1, NULL, 'menu-icon mi-cicd', 'tm', 1, 0, 3);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (114, '交付中心', 'menu', '', 0, NULL, 28, NULL, NULL, 1, NULL, 'menu-icon mi-centers', 'tm', 1, 0, 4);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (115, '日志管理', 'menu', '', 0, NULL, 29, NULL, '2017-09-13 16:02:57', 1, NULL, 'menu-icon mi-audit', 'tm', 1, 0, 5);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (116, '告警中心', 'menu', '', 0, NULL, 30, NULL, '2017-09-13 10:34:01', 1, NULL, 'menu-icon mi-alarm', 'tm', 1, 0, 6);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (117, '应用', 'menu', 'manageList', 2, '34/39', 31, NULL, NULL, 1, NULL, '', 'tm', 0, 2, 7);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (118, '服务', 'menu', 'manageApplyList', 2, '34/40', 32, NULL, NULL, 1, NULL, '', 'tm', 0, 2, 8);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (119, '外部服务', 'menu', 'externalService', 2, '34/41', 33, NULL, NULL, 1, NULL, '', 'tm', 0, 2, 9);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (120, '配置中心', 'menu', 'configcenter', 2, '34/42', 34, NULL, NULL, 1, NULL, '', 'tm', 0, 2, 10);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (121, '存储', 'menu', 'storageScheme', 2, '34/43', 35, NULL, NULL, 1, NULL, '', 'tm', 0, 2, 11);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (122, '批量任务', 'menu', 'job', 2, '34/44', 36, NULL, NULL, 0, NULL, '', 'tm', 0, 2, 12);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (123, 'Docker file', 'menu', 'dockerfileList', 3, '35/45', 37, NULL, NULL, 1, NULL, NULL, 'tm', 0, 3, 13);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (124, '流水线', 'menu', 'pipelineList', 3, '35/46', 38, NULL, NULL, 1, NULL, NULL, 'tm', 0, 3, 14);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (125, '依赖管理', 'menu', 'dependenceList', 3, '35/47', 39, NULL, NULL, 1, NULL, '', 'tm', 0, 3, 15);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (126, '镜像仓库', 'menu', 'mirrorContent', 4, '36/48', 40, NULL, NULL, 1, NULL, '', 'tm', 0, 4, 16);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (127, '应用商店', 'menu', 'deliveryStore', 4, '36/49', 41, NULL, NULL, 1, NULL, '', 'tm', 0, 4, 17);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (128, '模板管理', 'menu', 'manageAll', 4, '36/50', 42, NULL, NULL, 1, NULL, '', 'tm', 0, 4, 18);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (129, '操作审计', 'menu', 'logQuery', 5, '37/51', 43, NULL, '2017-08-22 17:09:15', 1, NULL, '', 'tm', 0, 5, 19);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (130, '告警规则', 'menu', 'alarmList', 6, '38/52', 44, NULL, NULL, 1, NULL, '', 'tm', 0, 6, 20);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (131, '告警处理中心', 'menu', 'alarmHandingList', 6, '38/53', 45, NULL, NULL, 1, NULL, '', 'tm', 0, 6, 21);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (132, '系统设置', 'menu', 'system', 0, '', 45, NULL, '2017-09-13 16:02:57', 0, NULL, 'menu-icon mi-config', 'tm', 1, 0, 22);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (133, '告警规则', 'menu', 'alarmList', 0, '8/23', 23, NULL, NULL, 0, NULL, NULL, 'tm', 0, 8, 23);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (134, '告警处理中心', 'menu', 'alarmHandingList', 0, '8/24', 24, NULL, NULL, 0, NULL, NULL, 'tm', 0, 8, 24);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (135, '系统设置', 'menu', 'system', 0, NULL, 45, NULL, NULL, 0, NULL, 'menu-icon mi-config', 'tm', 1, 0, 25);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (137, '总览', 'menu', 'overview', 0, NULL, 1, NULL, NULL, 0, NULL, 'menu-icon mi-overview', 'tester', 1, 0, 1);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (138, '集群', 'menu', 'cluster', 0, NULL, 2, NULL, NULL, 0, NULL, 'menu-icon mi-cluster', 'tester', 1, 0, 2);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (139, '租户管理', 'menu', 'tenant', 0, NULL, 1, NULL, NULL, 0, NULL, 'menu-icon mi-tenant', 'tester', 1, 0, 3);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (140, '应用中心', 'menu', '', 0, NULL, 4, NULL, '2017-09-11 14:47:21', 1, NULL, 'menu-icon mi-application', 'tester', 1, 0, 4);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (141, 'CICD', 'menu', '', 0, NULL, 5, NULL, '2017-09-12 19:49:01', 1, NULL, 'menu-icon mi-cicd', 'tester', 1, 0, 5);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (142, '交付中心', 'menu', '', 0, NULL, 6, NULL, '2017-09-11 14:37:32', 1, NULL, 'menu-icon mi-centers', 'tester', 1, 0, 6);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (143, '日志管理', 'menu', '', 0, NULL, 7, NULL, '2017-09-12 19:49:01', 1, NULL, 'menu-icon mi-audit', 'tester', 1, 0, 7);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (144, '告警中心', 'menu', '', 0, NULL, 8, NULL, '2017-09-13 16:54:01', 1, NULL, 'menu-icon mi-alarm', 'tester', 1, 0, 8);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (145, '应用', 'menu', 'manageList', 0, '4/9', 9, NULL, NULL, 1, NULL, NULL, 'tester', 0, 4, 9);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (146, '服务', 'menu', 'manageApplyList', 0, '4/10', 10, NULL, NULL, 1, NULL, NULL, 'tester', 0, 4, 10);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (147, '外部服务', 'menu', 'externalService', 0, '4/11', 11, NULL, NULL, 1, NULL, NULL, 'tester', 0, 4, 11);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (148, '配置中心', 'menu', 'configcenter', 0, '4/12', 12, NULL, NULL, 1, NULL, NULL, 'tester', 0, 4, 12);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (149, '存储', 'menu', 'storageScheme', 0, '4/13', 13, NULL, NULL, 1, NULL, NULL, 'tester', 0, 4, 13);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (150, '批量任务', 'menu', 'job', 0, '4/14', 14, NULL, NULL, 0, NULL, NULL, 'tester', 0, 4, 14);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (151, 'Docker file', 'menu', 'dockerfileList', 0, '5/15', 15, NULL, NULL, 1, NULL, NULL, 'tester', 0, 5, 15);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (152, '流水线', 'menu', 'pipelineList', 0, '5/16', 16, NULL, NULL, 1, NULL, NULL, 'tester', 0, 5, 16);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (153, '依赖管理', 'menu', 'dependenceList', 0, '5/17', 17, NULL, NULL, 1, NULL, NULL, 'tester', 0, 5, 17);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (154, '镜像管理', 'menu', 'mirrorContent', 0, '6/18', 18, NULL, NULL, 1, NULL, NULL, 'tester', 0, 6, 18);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (155, '应用商店', 'menu', 'deliveryStore', 0, '6/19', 19, NULL, NULL, 1, NULL, NULL, 'tester', 0, 6, 19);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (156, '模板管理', 'menu', 'manageAll', 0, '6/20', 20, NULL, NULL, 1, NULL, NULL, 'tester', 0, 6, 20);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (157, '操作审计', 'menu', 'audit', 0, '7/21', 21, NULL, NULL, 1, NULL, NULL, 'tester', 0, 7, 21);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (158, '日志查询', 'menu', 'logQuery', 0, '7/22', 22, NULL, NULL, 1, NULL, NULL, 'tester', 0, 7, 22);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (159, '告警规则', 'menu', 'alarmList', 0, '8/23', 23, NULL, NULL, 1, NULL, NULL, 'tester', 0, 8, 23);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (160, '告警处理中心', 'menu', 'alarmHandingList', 0, '8/24', 24, NULL, NULL, 1, NULL, NULL, 'tester', 0, 8, 24);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (161, '系统设置', 'menu', 'system', 0, NULL, 45, NULL, '2017-09-12 18:41:11', 0, NULL, 'menu-icon mi-config', 'tester', 1, 0, 25);
INSERT INTO `k8s_auth_server`.`resource_custom`(`id`, `name`, `type`, `url`, `parent_id`, `parent_ids`, `weight`, `create_time`, `update_time`, `available`, `trans_name`, `icon_name`, `role`, `isParent`, `parent_rpid`, `rpid`) VALUES (162, '我的租户', 'menu', 'tenant', 0, NULL, 3, NULL, '2017-09-11 14:37:32', 1, NULL, 'menu-icon mi-tenant', 'tester', 1, 0, 26);

/*!40000 ALTER TABLE `resource_custom` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `role`
--
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
-- ----------------------------
-- Table structure for role
-- ----------------------------
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role`  (
  `id` int(10) NOT NULL AUTO_INCREMENT COMMENT '序号',
  `name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '角色名称',
  `description` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '描述',
  `resource_ids` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '资源id',
  `create_time` datetime(0) DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(0) DEFAULT NULL COMMENT '更新时间',
  `available` tinyint(1) DEFAULT 1 COMMENT '是否可用 1可用 0不可用',
  `second_resource_ids` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `name_UNIQUE`(`name`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of role
-- ----------------------------
INSERT INTO `k8s_auth_server`.`role`(`id`, `name`, `description`, `resource_ids`, `create_time`, `update_time`, `available`, `second_resource_ids`) VALUES (1, 'admin', '系统管理员', '10000,11000,30000,31000,40000,41000,44000,44100,44200,50000,51000,52000,53000,54000,55000,60000,61000,61100,61200,61300,61400,61500,61600,61700,62000,62100,62200,62300,62400,62500,62600,63000,70000,71000', NULL, NULL, 1, NULL);
INSERT INTO `k8s_auth_server`.`role`(`id`, `name`, `description`, `resource_ids`, `create_time`, `update_time`, `available`, `second_resource_ids`) VALUES (2, 'tm', '租户管理员', '10000,11000,40000,42000', NULL, '2017-09-11 15:08:00', 1, NULL);
INSERT INTO `k8s_auth_server`.`role`(`id`, `name`, `description`, `resource_ids`, `create_time`, `update_time`, `available`, `second_resource_ids`) VALUES (3, 'dev', '开发人员', '10000,12000,40000,43000', NULL, NULL, 1, NULL);
INSERT INTO `k8s_auth_server`.`role`(`id`, `name`, `description`, `resource_ids`, `create_time`, `update_time`, `available`, `second_resource_ids`) VALUES (4, 'ops', '运维人员', '10000,12000,20000,21000,22000,23000,24000,24100,24200', NULL, '2017-09-11 15:58:58', 1, '');
INSERT INTO `k8s_auth_server`.`role`(`id`, `name`, `description`, `resource_ids`, `create_time`, `update_time`, `available`, `second_resource_ids`) VALUES (5, 'tester', '测试人员', '10000,12000,20000,21000,22000,23000,24000,24100,24200', NULL, '2017-09-11 15:58:49', 1, NULL);

SET FOREIGN_KEY_CHECKS = 1;
/*!40101 SET character_set_client = @saved_cs_client */;
--
-- Table structure for table `role_privilege`
--

/*
 Navicat Premium Data Transfer

 Source Server         : 10.10.101.74
 Source Server Type    : MySQL
 Source Server Version : 50635
 Source Host           : 10.10.101.74:30306
 Source Schema         : k8s_auth_server

 Target Server Type    : MySQL
 Target Server Version : 50635
 File Encoding         : 65001

 Date: 16/09/2017 13:32:25
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for role_privilege
-- ----------------------------
DROP TABLE IF EXISTS `role_privilege`;
CREATE TABLE `role_privilege`  (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `role` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '角色名',
  `privilege` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '权限',
  `update_time` timestamp(0) DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(0),
  `first_module` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '一级模块',
  `second_module` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '二级模块',
  `third_module` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '三级模块',
  `status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '权限的状态 0 未启用 1 启用',
  `mark` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '权限说明',
  `parent_rpid` int(100) DEFAULT NULL,
  `isParent` tinyint(1) DEFAULT 0 COMMENT '是否是父节点 0 子节点 1 父节点',
  `rpid` int(10) DEFAULT NULL,
  `parentid` int(10) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `parent_rpid`(`parent_rpid`) USING BTREE,
  INDEX `rpid`(`rpid`) USING BTREE,
  INDEX `role`(`role`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1353 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of role_privilege
-- ----------------------------
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1, 'dev', '', '2017-09-13 16:53:58', 'tenant', NULL, NULL, 1, '租户管理', 0, 1, 1, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (2, 'dev', '', '2017-09-13 16:53:58', 'tenant', '', NULL, 1, '租户', 1, 1, 2, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (3, 'dev', 'list', '2017-09-13 16:53:58', 'tenant', NULL, NULL, 1, '租户查询', 2, 0, 3, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (4, 'dev', 'create', '2017-09-13 16:53:58', 'tenant', '', NULL, 1, '租户创建', 2, 0, 4, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (5, 'dev', 'delete', '2017-09-13 16:53:58', 'tenant', '', NULL, 1, '租户删除', 2, 0, 5, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (6, 'dev', '', '2017-09-13 16:53:58', 'namespace', NULL, NULL, 1, '分区', 1, 1, 6, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (7, 'dev', 'create', '2017-09-13 16:53:58', 'namespace', '', NULL, 1, '租户分区创建', 6, 0, 7, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (8, 'dev', 'delete', '2017-09-13 16:53:58', 'namespace', '', NULL, 1, '租户分区删除', 6, 0, 8, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (9, 'dev', 'update', '2017-09-13 16:53:58', 'namespace', '', NULL, 1, '租户分区修改', 6, 0, 9, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (10, 'dev', 'list', '2017-09-13 16:53:58', 'namespace', '', NULL, 1, '租户分区查询', 6, 0, 10, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (11, 'dev', '', '2017-09-13 16:53:58', 'mirror', NULL, NULL, 1, '镜像仓库', 1, 1, 11, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (12, 'dev', 'list', '2017-09-13 16:53:58', 'mirror', '', NULL, 1, '租户镜像仓库查询', 11, 0, 12, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (13, 'dev', 'create', '2017-09-13 16:53:58', 'mirror', '', NULL, 1, '租户镜像仓库创建', 11, 0, 13, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (14, 'dev', 'update', '2017-09-13 16:53:58', 'mirror', '', NULL, 1, '租户镜像仓库修改', 11, 0, 14, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (15, 'dev', 'delete', '2017-09-13 16:53:58', 'mirror', '', NULL, 1, '租户镜像仓库删除', 11, 0, 15, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (16, 'dev', '', '2017-09-13 16:53:58', 'user', '', NULL, 1, '用户', 1, 1, 16, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (17, 'dev', 'create', '2017-09-13 16:53:58', 'user', '', NULL, 1, '租户用户添加', 16, 0, 17, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (18, 'dev', 'list', '2017-09-13 16:53:58', 'user', '', NULL, 1, '租户用户查询', 16, 0, 18, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (19, 'dev', 'delete', '2017-09-13 16:53:58', 'user', '', NULL, 1, '租户用户移除', 16, 0, 19, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (20, 'dev', 'update', '2017-09-13 16:53:58', 'user', NULL, NULL, 1, '租户用户修改', 16, 0, 20, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (21, 'dev', 'detail', '2017-09-13 16:53:58', 'namespace', '', NULL, 1, '租户分区详情', 6, 0, 21, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (101, 'dev', '', '2017-09-13 16:53:58', 'application', NULL, NULL, 1, '应用中心', 0, 1, 101, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (102, 'dev', '', '2017-09-13 16:53:58', 'application', NULL, NULL, 1, '应用', 101, 1, 102, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (103, 'dev', 'create', '2017-09-13 16:53:58', 'application', NULL, NULL, 1, '应用创建', 102, 0, 103, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (104, 'dev', 'list', '2017-09-13 16:53:58', 'application', NULL, NULL, 1, '应用列表', 102, 0, 104, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (105, 'dev', 'start', '2017-09-13 16:53:58', 'application', NULL, NULL, 1, '应用启动', 102, 0, 105, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (106, 'dev', 'pause', '2017-09-13 16:53:58', 'application', NULL, NULL, 1, '应用暂停', 102, 0, 106, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (107, 'dev', 'delete', '2017-09-13 16:53:58', 'application', NULL, NULL, 1, '应用删除', 102, 0, 107, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (108, 'dev', 'detail', '2017-09-13 16:53:58', 'application', NULL, NULL, 1, '应用详情', 102, 0, 108, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (111, 'dev', '', '2017-09-13 16:53:58', 'service', NULL, NULL, 1, '服务', 101, 1, 111, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (112, 'dev', '', '2017-09-13 16:53:58', 'basic', NULL, NULL, 1, '服务基本信息', 111, 1, 112, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (113, 'dev', 'create', '2017-09-13 16:53:58', 'basic', NULL, NULL, 1, '服务创建', 112, 0, 113, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (114, 'dev', 'start', '2017-09-13 16:53:58', 'basic', NULL, NULL, 1, '服务启动', 112, 0, 114, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (115, 'dev', 'release', '2017-09-13 16:53:58', 'basic', NULL, NULL, 1, '服务发布', 112, 0, 115, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (116, 'dev', 'autoScaling', '2017-09-13 16:53:58', 'basic', NULL, NULL, 1, '服务自动伸缩配置', 112, 0, 116, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (117, 'dev', 'rollback', '2017-09-13 16:53:58', 'basic', NULL, NULL, 1, '服务回滚', 112, 0, 117, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (118, 'dev', 'delete', '2017-09-13 16:53:58', 'basic', NULL, NULL, 1, '服务删除', 112, 0, 118, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (119, 'dev', 'stop', '2017-09-13 16:53:58', 'basic', NULL, NULL, 1, '服务停止', 112, 0, 119, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (120, 'dev', 'console', '2017-09-13 16:53:58', 'basic', NULL, NULL, 1, '服务控制台', 112, 0, 120, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (121, 'dev', 'update', '2017-09-13 16:53:58', 'basic', NULL, NULL, 1, '服务修改', 112, 0, 121, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (122, 'dev', '', '2017-09-13 16:53:58', 'log', NULL, NULL, 1, '服务日志信息', 111, 1, 122, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (123, 'dev', 'get', '2017-09-13 16:53:58', 'log', NULL, NULL, 1, '服务日志查询', 122, 0, 123, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (124, 'dev', 'export', '2017-09-13 16:53:58', 'log', NULL, NULL, 1, '服务日志导出', 122, 0, 124, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (125, 'dev', '', '2017-09-13 16:53:58', 'monitor', NULL, NULL, 1, '服务监控信息', 111, 1, 125, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (126, 'dev', 'get', '2017-09-13 16:53:58', 'monitor', NULL, NULL, 1, '服务监控信息查询', 125, 0, 126, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (127, 'dev', '', '2017-09-13 16:53:58', 'candyUpdate', NULL, NULL, 1, '灰度升级', 111, 1, 127, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (128, 'dev', 'start', '2017-09-13 16:53:58', 'candyUpdate', NULL, NULL, 1, '灰度升级开启', 127, 0, 128, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (129, 'dev', 'edit', '2017-09-13 16:53:58', 'candyUpdate', NULL, NULL, 1, '编辑灰度升级信息', 127, 0, 129, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (130, 'dev', 'rollback', '2017-09-13 16:53:58', 'candyUpdate', NULL, NULL, 1, '回滚到未开始升级的版本', 127, 0, 130, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (131, 'dev', 'update', '2017-09-13 16:53:58', 'candyUpdate', NULL, NULL, 1, '全部升级到新版本', 127, 0, 131, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (132, 'dev', 'upload', '2017-09-13 16:53:58', 'basic', NULL, NULL, 1, '上传文件到容器', 112, 0, 132, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (133, 'dev', '', '2017-09-13 16:53:58', 'external', NULL, NULL, 1, '外部服务', 101, 1, 133, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (134, 'dev', 'create', '2017-09-13 16:53:58', 'external', NULL, NULL, 1, '新建外部服务', 133, 0, 134, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (135, 'dev', 'list', '2017-09-13 16:53:58', 'external', NULL, NULL, 1, '外部服务列表查询', 133, 0, 135, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (136, 'dev', 'update', '2017-09-13 16:53:58', 'external', NULL, NULL, 1, '外部服务修改', 133, 0, 136, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (137, 'dev', 'delete', '2017-09-13 16:53:58', 'external', NULL, NULL, 1, '外部服务删除', 133, 0, 137, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (138, 'dev', '', '2017-09-13 16:53:58', 'configCenter', NULL, NULL, 1, '配置中心', 101, 1, 138, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (139, 'dev', 'create', '2017-09-13 16:53:58', 'configCenter', NULL, NULL, 1, '创建配置文件', 138, 0, 139, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (140, 'dev', 'list', '2017-09-13 16:53:58', 'configCenter', NULL, NULL, 1, '配置文件列表查询', 138, 0, 140, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (141, 'dev', 'update', '2017-09-13 16:53:58', 'configCenter', NULL, NULL, 1, '配置文件修改', 138, 0, 141, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (143, 'dev', 'delete', '2017-09-13 16:53:58', 'configCenter', NULL, NULL, 1, '配置文件删除', 138, 0, 143, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (144, 'dev', 'get', '2017-09-13 16:53:58', 'configCenter', NULL, NULL, 1, '配置文件查看', 138, 0, 144, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (145, 'dev', '', '2017-09-13 16:53:58', 'storage', NULL, NULL, 1, '存储', 101, 1, 145, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (146, 'dev', 'list', '2017-09-13 16:53:58', 'storage', NULL, NULL, 1, '存储列表查询', 145, 0, 146, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (147, 'dev', 'create', '2017-09-13 16:53:58', 'storage', NULL, NULL, 1, '存储创建', 145, 0, 147, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (148, 'dev', 'update', '2017-09-13 16:53:58', 'storage', NULL, NULL, 1, '存储更新', 145, 0, 148, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (149, 'dev', 'delete', '2017-09-13 16:53:58', 'storage', NULL, NULL, 1, '存储删除', 145, 0, 149, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (201, 'dev', '', '2017-09-13 16:53:58', 'cicd', NULL, NULL, 1, 'CICD', 0, 1, 201, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (202, 'dev', '', '2017-09-13 16:53:58', 'dockerFile', NULL, NULL, 1, 'Docker file', 201, 1, 202, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (203, 'dev', 'list', '2017-09-13 16:53:58', 'dockerFile', NULL, NULL, 1, 'DockerFile列表查看', 202, 0, 203, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (204, 'dev', 'get', '2017-09-13 16:53:58', 'dockerFile', NULL, NULL, 1, 'DockerFile查看', 202, 0, 204, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (205, 'dev', 'create', '2017-09-13 16:53:58', 'dockerFile', NULL, NULL, 1, 'DockerFile创建', 202, 0, 205, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (206, 'dev', 'update', '2017-09-13 16:53:58', 'dockerFile', NULL, NULL, 1, 'DockerFile修改', 202, 0, 206, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (207, 'dev', 'delete', '2017-09-13 16:53:58', 'dockerFile', NULL, NULL, 1, 'DockerFile删除', 202, 0, 207, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (208, 'dev', '', '2017-09-13 16:53:58', 'pipeline', NULL, NULL, 1, '流水线', 201, 1, 208, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (209, 'dev', '', '2017-09-13 16:53:58', 'basic', NULL, NULL, 1, '流水线基本信息管理', 208, 1, 209, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (210, 'dev', 'list', '2017-09-13 16:53:58', 'basic', NULL, NULL, 1, '流水线列表查询', 209, 0, 210, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (211, 'dev', 'get', '2017-09-13 16:53:58', 'basic', NULL, NULL, 1, '流水线查询', 209, 0, 211, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (212, 'dev', 'create', '2017-09-13 16:53:58', 'basic', NULL, NULL, 1, '流水线创建', 209, 0, 212, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (213, 'dev', 'start', '2017-09-13 16:53:58', 'basic', NULL, NULL, 1, '流水线任务执行', 209, 0, 213, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (214, 'dev', 'planJob', '2017-09-13 16:53:58', 'basic', NULL, NULL, 1, '持续集成', 209, 0, 214, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (215, 'dev', '', '2017-09-13 16:53:58', 'job', NULL, NULL, 1, '流水线job', 208, 1, 215, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (216, 'dev', 'list', '2017-09-13 16:53:58', 'job', NULL, NULL, 1, 'job列表查询', 215, 0, 216, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (217, 'dev', 'get', '2017-09-13 16:53:59', 'job', NULL, NULL, 1, 'job查询', 215, 0, 217, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (218, 'dev', 'create', '2017-09-13 16:53:59', 'job', NULL, NULL, 1, 'job创建', 215, 0, 218, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (219, 'dev', 'update', '2017-09-13 16:53:59', 'job', NULL, NULL, 1, 'job更新', 215, 0, 219, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (220, 'dev', 'delete', '2017-09-13 16:53:59', 'job', NULL, NULL, 1, 'job删除', 215, 0, 220, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (221, 'dev', 'log', '2017-09-13 16:53:59', 'job', NULL, NULL, 1, 'job日志查询', 215, 0, 221, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (222, 'dev', 'log', '2017-09-13 16:53:59', 'basic', NULL, NULL, 1, '流水线日志查询', 209, 0, 222, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (223, 'dev', '', '2017-09-13 16:53:59', 'dependence', NULL, NULL, 1, '依赖管理', 201, 1, 223, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (224, 'dev', 'list', '2017-09-13 16:53:59', 'dependence', NULL, NULL, 1, '依赖列表查询', 223, 0, 224, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (225, 'dev', 'create', '2017-09-13 16:53:59', 'dependence', NULL, NULL, 1, '依赖创建', 223, 0, 225, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (226, 'dev', 'delete', '2017-09-13 16:53:59', 'dependence', NULL, NULL, 1, '依赖删除', 223, 0, 226, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (227, 'dev', '', '2017-09-13 16:53:59', 'deliveryCenter', NULL, NULL, 1, '交付中心', 0, 1, 227, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (228, 'dev', '', '2017-09-13 16:53:59', 'mirrorRepertory', NULL, NULL, 1, '镜像管理', 227, 1, 228, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (229, 'dev', '', '2017-09-13 16:53:59', 'basic', NULL, NULL, 1, '镜像仓库基本信息', 228, 1, 229, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (230, 'dev', 'list', '2017-09-13 16:53:59', 'basic', NULL, NULL, 1, '镜像仓库镜像列表查询', 229, 0, 230, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (231, 'dev', 'create', '2017-09-13 16:53:59', 'basic', NULL, NULL, 1, '镜像仓库创建', 229, 0, 231, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (232, 'dev', 'delete', '2017-09-13 16:53:59', 'basic', NULL, NULL, 1, '镜像仓库删除', 229, 0, 232, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (233, 'dev', 'detail', '2017-09-13 16:53:59', 'basic', NULL, NULL, 1, '镜像仓库详情', 229, 0, 233, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (234, 'dev', '', '2017-09-13 16:53:59', 'mirror', NULL, NULL, 1, '镜像', 228, 1, 234, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (235, 'dev', '', '2017-09-13 16:53:59', 'history', NULL, NULL, 1, '镜像历史版本', 234, 1, 235, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (236, 'dev', 'delete', '2017-09-13 16:53:59', 'history', NULL, NULL, 1, '镜像历史版本删除', 235, 0, 236, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (237, 'dev', '', '2017-09-13 16:53:59', 'basic', NULL, NULL, 1, '镜像基本信息', 234, 1, 237, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (238, 'dev', 'delete', '2017-09-13 09:01:36', 'basic', NULL, NULL, 1, '镜像删除', 237, 0, 238, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (239, 'dev', 'push', '2017-09-13 09:01:36', 'basic', NULL, NULL, 1, '镜像push', 237, 0, 239, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (240, 'dev', '', '2017-09-13 16:53:59', 'synchronized', NULL, NULL, 1, '镜像仓库同步', 228, 1, 240, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (241, 'dev', 'create', '2017-09-13 16:53:59', 'synchronized', NULL, NULL, 1, '仓库同步规则创建', 240, 0, 241, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (242, 'dev', 'delete', '2017-09-13 16:53:59', 'synchronized', NULL, NULL, 1, '仓库同步规则删除', 240, 0, 242, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (243, 'dev', 'status', '2017-09-13 16:53:59', 'synchronized', NULL, NULL, 1, '仓库同步开启或暂停', 240, 0, 243, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (244, 'dev', 'update', '2017-09-13 16:53:59', 'synchronized', NULL, NULL, 1, '同步设置', 240, 0, 244, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (245, 'dev', 'update', '2017-09-13 16:53:59', 'basic', NULL, NULL, 1, '镜像仓库配额修改', 229, 0, 245, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (246, 'dev', '', '2017-09-13 16:53:59', 'shop', NULL, NULL, 1, '应用商店', 227, 1, 246, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (247, 'dev', 'delivery', '2017-09-13 16:53:59', 'shop', NULL, NULL, 1, '应用商店应用发布', 246, 0, 247, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (248, 'dev', '', '2017-09-13 16:53:59', 'template', NULL, NULL, 1, '模板管理', 227, 1, 248, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (249, 'dev', '', '2017-09-13 16:53:59', 'basic', NULL, NULL, 1, '模板基本信息', 248, 1, 249, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (250, 'dev', 'delete', '2017-09-13 16:53:59', 'basic', NULL, NULL, 1, '应用模板删除', 249, 0, 250, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (251, 'dev', 'delivery', '2017-09-13 16:53:59', 'basic', NULL, NULL, 1, '发布应用', 249, 0, 251, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (252, 'dev', 'detail', '2017-09-13 16:53:59', 'basic', NULL, NULL, 1, '模板详情', 249, 0, 252, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (253, 'dev', 'update', '2017-09-13 16:53:59', 'basic', NULL, NULL, 1, '模板更新', 249, 0, 253, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (354, 'dev', '', '2017-09-13 16:53:59', 'logs', NULL, NULL, 1, '日志管理', 0, 1, 354, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (355, 'dev', '', '2017-09-13 16:53:59', 'audit', NULL, NULL, 1, '操作审计', 354, 1, 355, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (356, 'dev', 'list', '2017-09-13 16:53:59', 'audit', NULL, NULL, 1, '查询操作审计', 355, 0, 356, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (400, 'dev', '', '2017-09-13 16:53:59', 'alarmCenter', NULL, NULL, 1, '告警中心', 0, 1, 400, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (401, 'dev', '', '2017-09-13 16:53:59', 'alarmRule', NULL, NULL, 1, '告警规则', 400, 1, 401, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (402, 'dev', 'create', '2017-09-13 16:53:59', 'alarmRule', NULL, NULL, 1, '创建告警规则', 401, 0, 402, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (403, 'dev', 'delete', '2017-09-13 16:53:59', 'alarmRule', NULL, NULL, 1, '删除告警规则', 401, 0, 403, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (404, 'dev', 'update', '2017-09-13 16:53:59', 'alarmRule', NULL, NULL, 1, '修改告警规则', 401, 0, 404, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (405, 'dev', 'pause', '2017-09-13 16:53:59', 'alarmRule', NULL, NULL, 1, '暂停/启动告警规则', 401, 0, 405, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (406, 'dev', 'list', '2017-09-13 16:53:59', 'alarmRule', NULL, NULL, 1, '查询告警规则', 401, 0, 406, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (407, 'dev', '', '2017-09-13 16:53:59', 'alarmProcessCenter', NULL, NULL, 1, '告警处理中心', 400, 1, 407, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (408, 'dev', 'list', '2017-09-13 16:53:59', 'alarmProcessCenter', NULL, NULL, 1, '报警处理查询', 407, 0, 408, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (409, 'dev', 'update', '2017-09-13 16:53:59', 'alarmProcessCenter', NULL, NULL, 1, '报警处理', 407, 0, 409, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (410, 'dev', '', '2017-09-13 16:53:59', 'systemConfig', NULL, NULL, 0, '系统设置', 0, 1, 410, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (411, 'dev', 'list', '2017-09-13 16:53:59', 'alarmProcessCenter', NULL, NULL, 0, '查询系统设置', 410, 0, 411, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (412, 'dev', 'update', '2017-09-13 16:53:59', 'alarmProcessCenter', NULL, NULL, 0, '更新系统设置', 410, 0, 412, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (512, 'default', '', '2017-09-13 09:15:15', 'tenant', NULL, NULL, 0, '租户管理', 0, 1, 1, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (513, 'default', '', '2017-09-13 09:15:15', 'tenant', NULL, NULL, 0, '租户', 1, 1, 2, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (514, 'default', 'list', '2017-09-13 09:15:15', 'tenant', NULL, NULL, 0, '租户查询', 2, 0, 3, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (515, 'default', 'create', '2017-09-13 09:15:15', 'tenant', NULL, NULL, 0, '租户创建', 2, 0, 4, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (516, 'default', 'delete', '2017-09-13 09:15:15', 'tenant', NULL, NULL, 0, '租户删除', 2, 0, 5, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (517, 'default', '', '2017-09-13 09:15:15', 'namespace', NULL, NULL, 0, '分区', 1, 1, 6, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (518, 'default', 'create', '2017-09-13 09:15:15', 'namespace', NULL, NULL, 0, '租户分区创建', 6, 0, 7, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (519, 'default', 'delete', '2017-09-13 09:15:15', 'namespace', NULL, NULL, 0, '租户分区删除', 6, 0, 8, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (520, 'default', 'update', '2017-09-13 09:15:15', 'namespace', NULL, NULL, 0, '租户分区修改', 6, 0, 9, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (521, 'default', 'list', '2017-09-13 09:15:15', 'namespace', NULL, NULL, 0, '租户分区查询', 6, 0, 10, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (522, 'default', '', '2017-09-13 09:15:15', 'mirror', NULL, NULL, 0, '镜像仓库', 1, 1, 11, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (523, 'default', 'list', '2017-09-13 09:15:15', 'mirror', NULL, NULL, 0, '租户镜像仓库查询', 11, 0, 12, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (524, 'default', 'create', '2017-09-13 09:15:15', 'mirror', NULL, NULL, 0, '租户镜像仓库创建', 11, 0, 13, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (525, 'default', 'update', '2017-09-13 09:15:16', 'mirror', NULL, NULL, 0, '租户镜像仓库修改', 11, 0, 14, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (526, 'default', 'delete', '2017-09-13 09:15:16', 'mirror', NULL, NULL, 0, '租户镜像仓库删除', 11, 0, 15, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (527, 'default', '', '2017-09-13 09:15:16', 'user', NULL, NULL, 0, '用户', 1, 1, 16, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (528, 'default', 'create', '2017-09-13 09:15:16', 'user', NULL, NULL, 0, '租户用户添加', 16, 0, 17, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (529, 'default', 'list', '2017-09-13 09:15:16', 'user', NULL, NULL, 0, '租户用户查询', 16, 0, 18, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (530, 'default', 'delete', '2017-09-13 09:15:16', 'user', NULL, NULL, 0, '租户用户移除', 16, 0, 19, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (531, 'default', 'update', '2017-09-13 09:15:16', 'user', NULL, NULL, 0, '租户用户修改', 16, 0, 20, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (532, 'default', 'detail', '2017-09-13 09:15:16', 'namespace', NULL, NULL, 0, '租户分区详情', 6, 0, 21, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (533, 'default', '', '2017-09-13 09:15:16', 'application', NULL, NULL, 0, '应用中心', 0, 1, 101, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (534, 'default', '', '2017-09-13 09:15:16', 'application', NULL, NULL, 0, '应用', 101, 1, 102, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (535, 'default', 'create', '2017-09-13 09:15:16', 'application', NULL, NULL, 0, '应用创建', 102, 0, 103, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (536, 'default', 'list', '2017-09-13 09:15:16', 'application', NULL, NULL, 0, '应用列表', 102, 0, 104, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (537, 'default', 'start', '2017-09-13 09:15:16', 'application', NULL, NULL, 0, '应用启动', 102, 0, 105, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (538, 'default', 'pause', '2017-09-13 09:15:16', 'application', NULL, NULL, 0, '应用暂停', 102, 0, 106, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (539, 'default', 'delete', '2017-09-13 09:15:16', 'application', NULL, NULL, 0, '应用删除', 102, 0, 107, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (540, 'default', 'detail', '2017-09-13 09:15:16', 'application', NULL, NULL, 0, '应用详情', 102, 0, 108, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (541, 'default', '', '2017-09-13 09:15:16', 'service', NULL, NULL, 0, '服务', 101, 1, 111, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (542, 'default', '', '2017-09-13 09:15:16', 'basic', NULL, NULL, 0, '服务基本信息', 111, 1, 112, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (543, 'default', 'create', '2017-09-13 09:15:16', 'basic', NULL, NULL, 0, '服务创建', 112, 0, 113, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (544, 'default', 'start', '2017-09-13 09:15:16', 'basic', NULL, NULL, 0, '服务启动', 112, 0, 114, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (545, 'default', 'release', '2017-09-13 09:15:16', 'basic', NULL, NULL, 0, '服务发布', 112, 0, 115, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (546, 'default', 'autoScaling', '2017-09-13 09:15:16', 'basic', NULL, NULL, 0, '服务自动伸缩配置', 112, 0, 116, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (547, 'default', 'rollback', '2017-09-13 09:15:16', 'basic', NULL, NULL, 0, '服务回滚', 112, 0, 117, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (548, 'default', 'delete', '2017-09-13 09:15:16', 'basic', NULL, NULL, 0, '服务删除', 112, 0, 118, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (549, 'default', 'stop', '2017-09-13 09:15:16', 'basic', NULL, NULL, 0, '服务停止', 112, 0, 119, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (550, 'default', 'console', '2017-09-13 09:15:16', 'basic', NULL, NULL, 0, '服务控制台', 112, 0, 120, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (551, 'default', 'update', '2017-09-13 09:15:16', 'basic', NULL, NULL, 0, '服务修改', 112, 0, 121, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (552, 'default', '', '2017-09-13 09:15:16', 'log', NULL, NULL, 0, '服务日志信息', 111, 1, 122, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (553, 'default', 'get', '2017-09-13 09:15:16', 'log', NULL, NULL, 0, '服务日志查询', 122, 0, 123, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (554, 'default', 'export', '2017-09-13 09:15:16', 'log', NULL, NULL, 0, '服务日志导出', 122, 0, 124, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (555, 'default', '', '2017-09-13 09:15:16', 'monitor', NULL, NULL, 0, '服务监控信息', 111, 1, 125, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (556, 'default', 'get', '2017-09-13 09:15:16', 'monitor', NULL, NULL, 0, '服务监控信息查询', 125, 0, 126, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (557, 'default', '', '2017-09-13 09:15:16', 'candyUpdate', NULL, NULL, 0, '灰度升级', 111, 1, 127, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (558, 'default', 'start', '2017-09-13 09:15:16', 'candyUpdate', NULL, NULL, 0, '灰度升级开启', 127, 0, 128, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (559, 'default', 'edit', '2017-09-13 09:15:16', 'candyUpdate', NULL, NULL, 0, '编辑灰度升级信息', 127, 0, 129, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (560, 'default', 'rollback', '2017-09-13 09:15:16', 'candyUpdate', NULL, NULL, 0, '回滚到未开始升级的版本', 127, 0, 130, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (561, 'default', 'update', '2017-09-13 09:15:16', 'candyUpdate', NULL, NULL, 0, '全部升级到新版本', 127, 0, 131, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (562, 'default', 'upload', '2017-09-13 09:15:16', 'basic', NULL, NULL, 0, '上传文件到容器', 112, 0, 132, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (563, 'default', '', '2017-09-13 09:15:16', 'external', NULL, NULL, 0, '外部服务', 101, 1, 133, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (564, 'default', 'create', '2017-09-13 09:15:16', 'external', NULL, NULL, 0, '新建外部服务', 133, 0, 134, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (565, 'default', 'list', '2017-09-13 09:15:16', 'external', NULL, NULL, 0, '外部服务列表查询', 133, 0, 135, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (566, 'default', 'update', '2017-09-13 09:15:16', 'external', NULL, NULL, 0, '外部服务修改', 133, 0, 136, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (567, 'default', 'delete', '2017-09-13 09:15:16', 'external', NULL, NULL, 0, '外部服务删除', 133, 0, 137, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (568, 'default', '', '2017-09-13 09:15:16', 'configCenter', NULL, NULL, 0, '配置中心', 101, 1, 138, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (569, 'default', 'create', '2017-09-13 09:15:16', 'configCenter', NULL, NULL, 0, '创建配置文件', 138, 0, 139, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (570, 'default', 'list', '2017-09-13 09:15:16', 'configCenter', NULL, NULL, 0, '配置文件列表查询', 138, 0, 140, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (571, 'default', 'update', '2017-09-13 09:15:16', 'configCenter', NULL, NULL, 0, '配置文件修改', 138, 0, 141, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (572, 'default', 'delete', '2017-09-13 09:15:16', 'configCenter', NULL, NULL, 0, '配置文件删除', 138, 0, 143, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (573, 'default', 'get', '2017-09-13 09:15:16', 'configCenter', NULL, NULL, 0, '配置文件查看', 138, 0, 144, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (574, 'default', '', '2017-09-13 09:15:16', 'storage', NULL, NULL, 0, '存储', 101, 1, 145, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (575, 'default', 'list', '2017-09-13 09:15:16', 'storage', NULL, NULL, 0, '存储列表查询', 145, 0, 146, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (576, 'default', 'create', '2017-09-13 09:15:16', 'storage', NULL, NULL, 0, '存储创建', 145, 0, 147, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (577, 'default', 'update', '2017-09-13 09:15:16', 'storage', NULL, NULL, 0, '存储更新', 145, 0, 148, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (578, 'default', 'delete', '2017-09-13 09:15:16', 'storage', NULL, NULL, 0, '存储删除', 145, 0, 149, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (579, 'default', '', '2017-09-13 09:15:16', 'cicd', NULL, NULL, 0, 'CICD', 0, 1, 201, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (580, 'default', '', '2017-09-13 09:15:16', 'dockerFile', NULL, NULL, 0, 'Docker file', 201, 1, 202, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (581, 'default', 'list', '2017-09-13 09:15:16', 'dockerFile', NULL, NULL, 0, 'DockerFile列表查看', 202, 0, 203, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (582, 'default', 'get', '2017-09-13 09:15:17', 'dockerFile', NULL, NULL, 0, 'DockerFile查看', 202, 0, 204, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (583, 'default', 'create', '2017-09-13 09:15:17', 'dockerFile', NULL, NULL, 0, 'DockerFile创建', 202, 0, 205, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (584, 'default', 'update', '2017-09-13 09:15:17', 'dockerFile', NULL, NULL, 0, 'DockerFile修改', 202, 0, 206, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (585, 'default', 'delete', '2017-09-13 09:15:17', 'dockerFile', NULL, NULL, 0, 'DockerFile删除', 202, 0, 207, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (586, 'default', '', '2017-09-13 09:15:17', 'pipeline', NULL, NULL, 0, '流水线', 201, 1, 208, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (587, 'default', '', '2017-09-13 09:15:17', 'basic', NULL, NULL, 0, '流水线基本信息管理', 208, 1, 209, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (588, 'default', 'list', '2017-09-13 09:15:17', 'basic', NULL, NULL, 0, '流水线列表查询', 209, 0, 210, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (589, 'default', 'get', '2017-09-13 09:15:17', 'basic', NULL, NULL, 0, '流水线查询', 209, 0, 211, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (590, 'default', 'create', '2017-09-13 09:15:17', 'basic', NULL, NULL, 0, '流水线创建', 209, 0, 212, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (591, 'default', 'start', '2017-09-13 09:15:17', 'basic', NULL, NULL, 0, '流水线任务执行', 209, 0, 213, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (592, 'default', 'planJob', '2017-09-13 09:15:17', 'basic', NULL, NULL, 0, '持续集成', 209, 0, 214, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (593, 'default', '', '2017-09-13 09:15:17', 'job', NULL, NULL, 0, '流水线job', 208, 1, 215, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (594, 'default', 'list', '2017-09-13 09:15:17', 'job', NULL, NULL, 0, 'job列表查询', 215, 0, 216, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (595, 'default', 'get', '2017-09-13 09:15:17', 'job', NULL, NULL, 0, 'job查询', 215, 0, 217, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (596, 'default', 'create', '2017-09-13 09:15:17', 'job', NULL, NULL, 0, 'job创建', 215, 0, 218, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (597, 'default', 'update', '2017-09-13 09:15:17', 'job', NULL, NULL, 0, 'job更新', 215, 0, 219, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (598, 'default', 'delete', '2017-09-13 09:15:17', 'job', NULL, NULL, 0, 'job删除', 215, 0, 220, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (599, 'default', 'log', '2017-09-13 09:15:17', 'job', NULL, NULL, 0, 'job日志查询', 215, 0, 221, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (600, 'default', 'log', '2017-09-13 09:15:17', 'basic', NULL, NULL, 0, '流水线日志查询', 209, 0, 222, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (601, 'default', '', '2017-09-13 09:15:17', 'dependence', NULL, NULL, 0, '依赖管理', 201, 1, 223, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (602, 'default', 'list', '2017-09-13 09:15:17', 'dependence', NULL, NULL, 0, '依赖列表查询', 223, 0, 224, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (603, 'default', 'create', '2017-09-13 09:15:17', 'dependence', NULL, NULL, 0, '依赖创建', 223, 0, 225, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (604, 'default', 'delete', '2017-09-13 09:15:17', 'dependence', NULL, NULL, 0, '依赖删除', 223, 0, 226, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (605, 'default', '', '2017-09-13 09:15:17', 'deliveryCenter', NULL, NULL, 0, '交付中心', 0, 1, 227, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (606, 'default', '', '2017-09-13 09:15:17', 'mirrorRepertory', NULL, NULL, 0, '镜像管理', 227, 1, 228, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (607, 'default', '', '2017-09-13 09:15:17', 'basic', NULL, NULL, 0, '镜像仓库基本信息', 228, 1, 229, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (608, 'default', 'list', '2017-09-13 09:15:17', 'basic', NULL, NULL, 0, '镜像仓库镜像列表查询', 229, 0, 230, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (609, 'default', 'create', '2017-09-13 09:15:17', 'basic', NULL, NULL, 0, '镜像仓库创建', 229, 0, 231, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (610, 'default', 'delete', '2017-09-13 09:15:17', 'basic', NULL, NULL, 0, '镜像仓库删除', 229, 0, 232, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (611, 'default', 'detail', '2017-09-13 09:15:17', 'basic', NULL, NULL, 0, '镜像仓库详情', 229, 0, 233, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (612, 'default', '', '2017-09-13 09:15:17', 'mirror', NULL, NULL, 0, '镜像', 228, 1, 234, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (613, 'default', '', '2017-09-13 09:15:17', 'history', NULL, NULL, 0, '镜像历史版本', 234, 1, 235, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (614, 'default', 'delete', '2017-09-13 09:15:17', 'history', NULL, NULL, 0, '镜像历史版本删除', 235, 0, 236, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (615, 'default', '', '2017-09-13 09:15:17', 'basic', NULL, NULL, 0, '镜像基本信息', 234, 1, 237, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (616, 'default', 'delete', '2017-09-13 09:15:17', 'basic', NULL, NULL, 0, '镜像删除', 237, 0, 238, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (617, 'default', 'push', '2017-09-13 09:15:17', 'basic', NULL, NULL, 0, '镜像push', 237, 0, 239, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (618, 'default', '', '2017-09-13 09:15:17', 'synchronized', NULL, NULL, 0, '镜像仓库同步', 228, 1, 240, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (619, 'default', 'create', '2017-09-13 09:15:17', 'synchronized', NULL, NULL, 0, '仓库同步规则创建', 240, 0, 241, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (620, 'default', 'delete', '2017-09-13 09:15:17', 'synchronized', NULL, NULL, 0, '仓库同步规则删除', 240, 0, 242, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (621, 'default', 'status', '2017-09-13 09:15:17', 'synchronized', NULL, NULL, 0, '仓库同步开启或暂停', 240, 0, 243, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (622, 'default', 'update', '2017-09-13 09:15:17', 'synchronized', NULL, NULL, 0, '同步设置', 240, 0, 244, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (623, 'default', 'update', '2017-09-13 09:15:17', 'basic', NULL, NULL, 0, '镜像仓库配额修改', 229, 0, 245, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (624, 'default', '', '2017-09-13 09:15:17', 'shop', NULL, NULL, 0, '应用商店', 227, 1, 246, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (625, 'default', 'delivery', '2017-09-13 09:15:17', 'shop', NULL, NULL, 0, '应用商店应用发布', 246, 0, 247, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (626, 'default', '', '2017-09-13 09:15:17', 'template', NULL, NULL, 0, '模板管理', 227, 1, 248, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (627, 'default', '', '2017-09-13 09:15:17', 'basic', NULL, NULL, 0, '模板基本信息', 248, 1, 249, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (628, 'default', 'delete', '2017-09-13 09:15:17', 'basic', NULL, NULL, 0, '应用模板删除', 249, 0, 250, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (629, 'default', 'delivery', '2017-09-13 09:15:17', 'basic', NULL, NULL, 0, '发布应用', 249, 0, 251, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (630, 'default', 'detail', '2017-09-13 09:15:17', 'basic', NULL, NULL, 0, '模板详情', 249, 0, 252, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (631, 'default', 'update', '2017-09-13 09:15:17', 'basic', NULL, NULL, 0, '模板更新', 249, 0, 253, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (632, 'default', '', '2017-09-13 09:15:17', 'logs', NULL, NULL, 0, '日志管理', 0, 1, 354, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (633, 'default', '', '2017-09-13 09:15:17', 'audit', NULL, NULL, 0, '操作审计', 354, 1, 355, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (634, 'default', 'list', '2017-09-13 09:15:17', 'audit', NULL, NULL, 0, '查询操作审计', 355, 0, 356, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (635, 'default', '', '2017-09-13 09:15:17', 'alarmCenter', NULL, NULL, 0, '告警中心', 0, 1, 400, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (636, 'default', '', '2017-09-13 09:15:17', 'alarmRule', NULL, NULL, 0, '告警规则', 400, 1, 401, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (637, 'default', 'create', '2017-09-13 09:15:17', 'alarmRule', NULL, NULL, 0, '创建告警规则', 401, 0, 402, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (638, 'default', 'delete', '2017-09-13 09:15:17', 'alarmRule', NULL, NULL, 0, '删除告警规则', 401, 0, 403, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (639, 'default', 'update', '2017-09-13 09:15:17', 'alarmRule', NULL, NULL, 0, '修改告警规则', 401, 0, 404, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (640, 'default', 'pause', '2017-09-13 09:15:17', 'alarmRule', NULL, NULL, 0, '暂停/启动告警规则', 401, 0, 405, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (641, 'default', 'list', '2017-09-13 09:15:17', 'alarmRule', NULL, NULL, 0, '查询告警规则', 401, 0, 406, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (642, 'default', '', '2017-09-13 09:15:17', 'alarmProcessCenter', NULL, NULL, 0, '告警处理中心', 400, 1, 407, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (643, 'default', 'list', '2017-09-13 09:15:17', 'alarmProcessCenter', NULL, NULL, 0, '报警处理查询', 407, 0, 408, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (644, 'default', 'update', '2017-09-13 09:15:17', 'alarmProcessCenter', NULL, NULL, 0, '报警处理', 407, 0, 409, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (645, 'default', '', '2017-09-13 16:53:59', 'systemConfig', NULL, NULL, 0, '系统设置', 0, 1, 410, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (646, 'default', 'list', '2017-09-13 16:53:59', 'alarmProcessCenter', NULL, NULL, 0, '查询系统设置', 410, 0, 411, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (647, 'default', 'update', '2017-09-13 16:53:59', 'alarmProcessCenter', NULL, NULL, 0, '更新系统设置', 410, 0, 412, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (747, 'ops', '', '2017-09-13 16:53:59', 'tenant', NULL, NULL, 1, '租户管理', 0, 1, 1, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (748, 'ops', '', '2017-09-13 16:53:59', 'tenant', NULL, NULL, 1, '租户', 1, 1, 2, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (749, 'ops', 'list', '2017-09-13 16:53:59', 'tenant', NULL, NULL, 1, '租户查询', 2, 0, 3, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (750, 'ops', 'create', '2017-09-13 16:53:59', 'tenant', NULL, NULL, 1, '租户创建', 2, 0, 4, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (751, 'ops', 'delete', '2017-09-13 16:53:59', 'tenant', NULL, NULL, 1, '租户删除', 2, 0, 5, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (752, 'ops', '', '2017-09-13 16:53:59', 'namespace', NULL, NULL, 1, '分区', 1, 1, 6, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (753, 'ops', 'create', '2017-09-13 16:53:59', 'namespace', NULL, NULL, 1, '租户分区创建', 6, 0, 7, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (754, 'ops', 'delete', '2017-09-13 16:53:59', 'namespace', NULL, NULL, 1, '租户分区删除', 6, 0, 8, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (755, 'ops', 'update', '2017-09-13 16:53:59', 'namespace', NULL, NULL, 1, '租户分区修改', 6, 0, 9, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (756, 'ops', 'list', '2017-09-13 16:53:59', 'namespace', NULL, NULL, 1, '租户分区查询', 6, 0, 10, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (757, 'ops', '', '2017-09-13 16:53:59', 'mirror', NULL, NULL, 1, '镜像仓库', 1, 1, 11, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (758, 'ops', 'list', '2017-09-13 16:53:59', 'mirror', NULL, NULL, 1, '租户镜像仓库查询', 11, 0, 12, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (759, 'ops', 'create', '2017-09-13 16:53:59', 'mirror', NULL, NULL, 1, '租户镜像仓库创建', 11, 0, 13, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (760, 'ops', 'update', '2017-09-13 16:53:59', 'mirror', NULL, NULL, 1, '租户镜像仓库修改', 11, 0, 14, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (761, 'ops', 'delete', '2017-09-13 16:53:59', 'mirror', NULL, NULL, 1, '租户镜像仓库删除', 11, 0, 15, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (762, 'ops', '', '2017-09-13 16:53:59', 'user', NULL, NULL, 1, '用户', 1, 1, 16, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (763, 'ops', 'create', '2017-09-13 16:53:59', 'user', NULL, NULL, 1, '租户用户添加', 16, 0, 17, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (764, 'ops', 'list', '2017-09-13 16:53:59', 'user', NULL, NULL, 1, '租户用户查询', 16, 0, 18, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (765, 'ops', 'delete', '2017-09-13 16:53:59', 'user', NULL, NULL, 1, '租户用户移除', 16, 0, 19, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (766, 'ops', 'update', '2017-09-13 16:53:59', 'user', NULL, NULL, 1, '租户用户修改', 16, 0, 20, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (767, 'ops', 'detail', '2017-09-13 16:53:59', 'namespace', NULL, NULL, 1, '租户分区详情', 6, 0, 21, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (768, 'ops', '', '2017-09-13 16:53:59', 'application', NULL, NULL, 1, '应用中心', 0, 1, 101, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (769, 'ops', '', '2017-09-13 16:53:59', 'application', NULL, NULL, 1, '应用', 101, 1, 102, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (770, 'ops', 'create', '2017-09-13 16:53:59', 'application', NULL, NULL, 1, '应用创建', 102, 0, 103, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (771, 'ops', 'list', '2017-09-13 16:53:59', 'application', NULL, NULL, 1, '应用列表', 102, 0, 104, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (772, 'ops', 'start', '2017-09-13 16:53:59', 'application', NULL, NULL, 1, '应用启动', 102, 0, 105, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (773, 'ops', 'pause', '2017-09-13 16:53:59', 'application', NULL, NULL, 1, '应用暂停', 102, 0, 106, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (774, 'ops', 'delete', '2017-09-13 16:53:59', 'application', NULL, NULL, 1, '应用删除', 102, 0, 107, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (775, 'ops', 'detail', '2017-09-13 16:53:59', 'application', NULL, NULL, 1, '应用详情', 102, 0, 108, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (776, 'ops', '', '2017-09-13 16:53:59', 'service', NULL, NULL, 1, '服务', 101, 1, 111, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (777, 'ops', '', '2017-09-13 16:53:59', 'basic', NULL, NULL, 1, '服务基本信息', 111, 1, 112, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (778, 'ops', 'create', '2017-09-13 16:53:59', 'basic', NULL, NULL, 1, '服务创建', 112, 0, 113, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (779, 'ops', 'start', '2017-09-13 16:53:59', 'basic', NULL, NULL, 1, '服务启动', 112, 0, 114, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (780, 'ops', 'release', '2017-09-13 16:53:59', 'basic', NULL, NULL, 1, '服务发布', 112, 0, 115, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (781, 'ops', 'autoScaling', '2017-09-13 16:53:59', 'basic', NULL, NULL, 1, '服务自动伸缩配置', 112, 0, 116, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (782, 'ops', 'rollback', '2017-09-13 16:53:59', 'basic', NULL, NULL, 1, '服务回滚', 112, 0, 117, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (783, 'ops', 'delete', '2017-09-13 16:53:59', 'basic', NULL, NULL, 1, '服务删除', 112, 0, 118, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (784, 'ops', 'stop', '2017-09-13 16:53:59', 'basic', NULL, NULL, 1, '服务停止', 112, 0, 119, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (785, 'ops', 'console', '2017-09-13 16:53:59', 'basic', NULL, NULL, 1, '服务控制台', 112, 0, 120, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (786, 'ops', 'update', '2017-09-13 16:53:59', 'basic', NULL, NULL, 1, '服务修改', 112, 0, 121, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (787, 'ops', '', '2017-09-13 16:53:59', 'log', NULL, NULL, 1, '服务日志信息', 111, 1, 122, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (788, 'ops', 'get', '2017-09-13 16:53:59', 'log', NULL, NULL, 1, '服务日志查询', 122, 0, 123, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (789, 'ops', 'export', '2017-09-13 16:53:59', 'log', NULL, NULL, 1, '服务日志导出', 122, 0, 124, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (790, 'ops', '', '2017-09-13 16:53:59', 'monitor', NULL, NULL, 1, '服务监控信息', 111, 1, 125, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (791, 'ops', 'get', '2017-09-13 16:53:59', 'monitor', NULL, NULL, 1, '服务监控信息查询', 125, 0, 126, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (792, 'ops', '', '2017-09-13 16:53:59', 'candyUpdate', NULL, NULL, 1, '灰度升级', 111, 1, 127, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (793, 'ops', 'start', '2017-09-13 16:53:59', 'candyUpdate', NULL, NULL, 1, '灰度升级开启', 127, 0, 128, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (794, 'ops', 'edit', '2017-09-13 16:53:59', 'candyUpdate', NULL, NULL, 1, '编辑灰度升级信息', 127, 0, 129, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (795, 'ops', 'rollback', '2017-09-13 16:53:59', 'candyUpdate', NULL, NULL, 1, '回滚到未开始升级的版本', 127, 0, 130, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (796, 'ops', 'update', '2017-09-13 16:53:59', 'candyUpdate', NULL, NULL, 1, '全部升级到新版本', 127, 0, 131, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (797, 'ops', 'upload', '2017-09-13 16:53:59', 'basic', NULL, NULL, 1, '上传文件到容器', 112, 0, 132, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (798, 'ops', '', '2017-09-13 16:53:59', 'external', NULL, NULL, 1, '外部服务', 101, 1, 133, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (799, 'ops', 'create', '2017-09-13 16:53:59', 'external', NULL, NULL, 1, '新建外部服务', 133, 0, 134, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (800, 'ops', 'list', '2017-09-13 16:53:59', 'external', NULL, NULL, 1, '外部服务列表查询', 133, 0, 135, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (801, 'ops', 'update', '2017-09-13 16:53:59', 'external', NULL, NULL, 1, '外部服务修改', 133, 0, 136, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (802, 'ops', 'delete', '2017-09-13 16:53:59', 'external', NULL, NULL, 1, '外部服务删除', 133, 0, 137, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (803, 'ops', '', '2017-09-13 16:53:59', 'configCenter', NULL, NULL, 1, '配置中心', 101, 1, 138, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (804, 'ops', 'create', '2017-09-13 16:53:59', 'configCenter', NULL, NULL, 1, '创建配置文件', 138, 0, 139, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (805, 'ops', 'list', '2017-09-13 16:53:59', 'configCenter', NULL, NULL, 1, '配置文件列表查询', 138, 0, 140, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (806, 'ops', 'update', '2017-09-13 16:53:59', 'configCenter', NULL, NULL, 1, '配置文件修改', 138, 0, 141, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (807, 'ops', 'delete', '2017-09-13 16:53:59', 'configCenter', NULL, NULL, 1, '配置文件删除', 138, 0, 143, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (808, 'ops', 'get', '2017-09-13 16:53:59', 'configCenter', NULL, NULL, 1, '配置文件查看', 138, 0, 144, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (809, 'ops', '', '2017-09-13 16:53:59', 'storage', NULL, NULL, 1, '存储', 101, 1, 145, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (810, 'ops', 'list', '2017-09-13 16:53:59', 'storage', NULL, NULL, 1, '存储列表查询', 145, 0, 146, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (811, 'ops', 'create', '2017-09-13 16:53:59', 'storage', NULL, NULL, 1, '存储创建', 145, 0, 147, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (812, 'ops', 'update', '2017-09-13 16:53:59', 'storage', NULL, NULL, 1, '存储更新', 145, 0, 148, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (813, 'ops', 'delete', '2017-09-13 16:53:59', 'storage', NULL, NULL, 1, '存储删除', 145, 0, 149, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (814, 'ops', '', '2017-09-13 16:53:59', 'cicd', NULL, NULL, 1, 'CICD', 0, 1, 201, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (815, 'ops', '', '2017-09-13 16:53:59', 'dockerFile', NULL, NULL, 1, 'Docker file', 201, 1, 202, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (816, 'ops', 'list', '2017-09-13 16:53:59', 'dockerFile', NULL, NULL, 1, 'DockerFile列表查看', 202, 0, 203, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (817, 'ops', 'get', '2017-09-13 16:53:59', 'dockerFile', NULL, NULL, 1, 'DockerFile查看', 202, 0, 204, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (818, 'ops', 'create', '2017-09-13 16:53:59', 'dockerFile', NULL, NULL, 1, 'DockerFile创建', 202, 0, 205, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (819, 'ops', 'update', '2017-09-13 16:53:59', 'dockerFile', NULL, NULL, 1, 'DockerFile修改', 202, 0, 206, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (820, 'ops', 'delete', '2017-09-13 16:53:59', 'dockerFile', NULL, NULL, 1, 'DockerFile删除', 202, 0, 207, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (821, 'ops', '', '2017-09-13 16:53:59', 'pipeline', NULL, NULL, 1, '流水线', 201, 1, 208, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (822, 'ops', '', '2017-09-13 16:53:59', 'basic', NULL, NULL, 1, '流水线基本信息管理', 208, 1, 209, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (823, 'ops', 'list', '2017-09-13 16:53:59', 'basic', NULL, NULL, 1, '流水线列表查询', 209, 0, 210, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (824, 'ops', 'get', '2017-09-13 16:53:59', 'basic', NULL, NULL, 1, '流水线查询', 209, 0, 211, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (825, 'ops', 'create', '2017-09-13 16:53:59', 'basic', NULL, NULL, 1, '流水线创建', 209, 0, 212, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (826, 'ops', 'start', '2017-09-13 16:53:59', 'basic', NULL, NULL, 1, '流水线任务执行', 209, 0, 213, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (827, 'ops', 'planJob', '2017-09-13 16:53:59', 'basic', NULL, NULL, 1, '持续集成', 209, 0, 214, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (828, 'ops', '', '2017-09-13 16:53:59', 'job', NULL, NULL, 1, '流水线job', 208, 1, 215, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (829, 'ops', 'list', '2017-09-13 16:53:59', 'job', NULL, NULL, 1, 'job列表查询', 215, 0, 216, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (830, 'ops', 'get', '2017-09-13 16:53:59', 'job', NULL, NULL, 1, 'job查询', 215, 0, 217, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (831, 'ops', 'create', '2017-09-13 16:53:59', 'job', NULL, NULL, 1, 'job创建', 215, 0, 218, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (832, 'ops', 'update', '2017-09-13 16:53:59', 'job', NULL, NULL, 1, 'job更新', 215, 0, 219, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (833, 'ops', 'delete', '2017-09-13 16:53:59', 'job', NULL, NULL, 1, 'job删除', 215, 0, 220, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (834, 'ops', 'log', '2017-09-13 16:53:59', 'job', NULL, NULL, 1, 'job日志查询', 215, 0, 221, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (835, 'ops', 'log', '2017-09-13 16:53:59', 'basic', NULL, NULL, 1, '流水线日志查询', 209, 0, 222, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (836, 'ops', '', '2017-09-13 16:53:59', 'dependence', NULL, NULL, 1, '依赖管理', 201, 1, 223, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (837, 'ops', 'list', '2017-09-13 16:53:59', 'dependence', NULL, NULL, 1, '依赖列表查询', 223, 0, 224, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (838, 'ops', 'create', '2017-09-13 16:53:59', 'dependence', NULL, NULL, 1, '依赖创建', 223, 0, 225, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (839, 'ops', 'delete', '2017-09-13 16:53:59', 'dependence', NULL, NULL, 1, '依赖删除', 223, 0, 226, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (840, 'ops', '', '2017-09-13 16:53:59', 'deliveryCenter', NULL, NULL, 1, '交付中心', 0, 1, 227, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (841, 'ops', '', '2017-09-13 16:53:59', 'mirrorRepertory', NULL, NULL, 1, '镜像管理', 227, 1, 228, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (842, 'ops', '', '2017-09-13 16:53:59', 'basic', NULL, NULL, 1, '镜像仓库基本信息', 228, 1, 229, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (843, 'ops', 'list', '2017-09-13 16:53:59', 'basic', NULL, NULL, 1, '镜像仓库镜像列表查询', 229, 0, 230, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (844, 'ops', 'create', '2017-09-13 16:53:59', 'basic', NULL, NULL, 1, '镜像仓库创建', 229, 0, 231, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (845, 'ops', 'delete', '2017-09-13 16:53:59', 'basic', NULL, NULL, 1, '镜像仓库删除', 229, 0, 232, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (846, 'ops', 'detail', '2017-09-13 16:53:59', 'basic', NULL, NULL, 1, '镜像仓库详情', 229, 0, 233, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (847, 'ops', '', '2017-09-13 16:53:59', 'mirror', NULL, NULL, 1, '镜像', 228, 1, 234, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (848, 'ops', '', '2017-09-13 16:53:59', 'history', NULL, NULL, 1, '镜像历史版本', 234, 1, 235, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (849, 'ops', 'delete', '2017-09-13 16:53:59', 'history', NULL, NULL, 1, '镜像历史版本删除', 235, 0, 236, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (850, 'ops', '', '2017-09-13 16:53:59', 'basic', NULL, NULL, 1, '镜像基本信息', 234, 1, 237, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (851, 'ops', 'delete', '2017-09-13 09:02:05', 'basic', NULL, NULL, 1, '镜像删除', 237, 0, 238, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (852, 'ops', 'push', '2017-09-13 09:02:05', 'basic', NULL, NULL, 1, '镜像push', 237, 0, 239, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (853, 'ops', '', '2017-09-13 16:53:59', 'synchronized', NULL, NULL, 1, '镜像仓库同步', 228, 1, 240, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (854, 'ops', 'create', '2017-09-13 16:53:59', 'synchronized', NULL, NULL, 1, '仓库同步规则创建', 240, 0, 241, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (855, 'ops', 'delete', '2017-09-13 16:53:59', 'synchronized', NULL, NULL, 1, '仓库同步规则删除', 240, 0, 242, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (856, 'ops', 'status', '2017-09-13 16:53:59', 'synchronized', NULL, NULL, 1, '仓库同步开启或暂停', 240, 0, 243, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (857, 'ops', 'update', '2017-09-13 16:53:59', 'synchronized', NULL, NULL, 1, '同步设置', 240, 0, 244, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (858, 'ops', 'update', '2017-09-13 16:53:59', 'basic', NULL, NULL, 1, '镜像仓库配额修改', 229, 0, 245, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (859, 'ops', '', '2017-09-13 16:53:59', 'shop', NULL, NULL, 1, '应用商店', 227, 1, 246, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (860, 'ops', 'delivery', '2017-09-13 16:53:59', 'shop', NULL, NULL, 1, '应用商店应用发布', 246, 0, 247, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (861, 'ops', '', '2017-09-13 16:53:59', 'template', NULL, NULL, 1, '模板管理', 227, 1, 248, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (862, 'ops', '', '2017-09-13 16:53:59', 'basic', NULL, NULL, 1, '模板基本信息', 248, 1, 249, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (863, 'ops', 'delete', '2017-09-13 16:53:59', 'basic', NULL, NULL, 1, '应用模板删除', 249, 0, 250, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (864, 'ops', 'delivery', '2017-09-13 16:53:59', 'basic', NULL, NULL, 1, '发布应用', 249, 0, 251, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (865, 'ops', 'detail', '2017-09-13 16:53:59', 'basic', NULL, NULL, 1, '模板详情', 249, 0, 252, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (866, 'ops', 'update', '2017-09-13 16:53:59', 'basic', NULL, NULL, 1, '模板更新', 249, 0, 253, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (867, 'ops', '', '2017-09-13 16:53:59', 'logs', NULL, NULL, 1, '日志管理', 0, 1, 354, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (868, 'ops', '', '2017-09-13 16:53:59', 'audit', NULL, NULL, 1, '操作审计', 354, 1, 355, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (869, 'ops', 'list', '2017-09-13 16:53:59', 'audit', NULL, NULL, 1, '查询操作审计', 355, 0, 356, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (870, 'ops', '', '2017-09-13 16:53:59', 'alarmCenter', NULL, NULL, 1, '告警中心', 0, 1, 400, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (871, 'ops', '', '2017-09-13 16:53:59', 'alarmRule', NULL, NULL, 1, '告警规则', 400, 1, 401, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (872, 'ops', 'create', '2017-09-13 16:53:59', 'alarmRule', NULL, NULL, 1, '创建告警规则', 401, 0, 402, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (873, 'ops', 'delete', '2017-09-13 16:53:59', 'alarmRule', NULL, NULL, 1, '删除告警规则', 401, 0, 403, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (874, 'ops', 'update', '2017-09-13 16:53:59', 'alarmRule', NULL, NULL, 1, '修改告警规则', 401, 0, 404, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (875, 'ops', 'pause', '2017-09-13 16:53:59', 'alarmRule', NULL, NULL, 1, '暂停/启动告警规则', 401, 0, 405, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (876, 'ops', 'list', '2017-09-13 16:53:59', 'alarmRule', NULL, NULL, 1, '查询告警规则', 401, 0, 406, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (877, 'ops', '', '2017-09-13 16:53:59', 'alarmProcessCenter', NULL, NULL, 1, '告警处理中心', 400, 1, 407, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (878, 'ops', 'list', '2017-09-13 16:53:59', 'alarmProcessCenter', NULL, NULL, 1, '报警处理查询', 407, 0, 408, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (879, 'ops', 'update', '2017-09-13 16:53:59', 'alarmProcessCenter', NULL, NULL, 1, '报警处理', 407, 0, 409, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (880, 'ops', '', '2017-09-13 16:53:59', 'systemConfig', NULL, NULL, 0, '系统设置', 0, 1, 410, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (881, 'ops', 'list', '2017-09-13 16:53:59', 'alarmProcessCenter', NULL, NULL, 0, '查询系统设置', 410, 0, 411, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (882, 'ops', 'update', '2017-09-13 16:53:59', 'alarmProcessCenter', NULL, NULL, 0, '更新系统设置', 410, 0, 412, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (982, 'tester', '', '2017-09-13 16:53:59', 'tenant', NULL, NULL, 1, '租户管理', 0, 1, 1, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (983, 'tester', '', '2017-09-13 16:53:59', 'tenant', NULL, NULL, 1, '租户', 1, 1, 2, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (984, 'tester', 'list', '2017-09-13 16:53:59', 'tenant', NULL, NULL, 1, '租户查询', 2, 0, 3, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (985, 'tester', 'create', '2017-09-13 16:53:59', 'tenant', NULL, NULL, 1, '租户创建', 2, 0, 4, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (986, 'tester', 'delete', '2017-09-13 16:53:59', 'tenant', NULL, NULL, 1, '租户删除', 2, 0, 5, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (987, 'tester', '', '2017-09-13 16:53:59', 'namespace', NULL, NULL, 1, '分区', 1, 1, 6, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (988, 'tester', 'create', '2017-09-13 16:53:59', 'namespace', NULL, NULL, 1, '租户分区创建', 6, 0, 7, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (989, 'tester', 'delete', '2017-09-13 16:53:59', 'namespace', NULL, NULL, 1, '租户分区删除', 6, 0, 8, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (990, 'tester', 'update', '2017-09-13 16:53:59', 'namespace', NULL, NULL, 1, '租户分区修改', 6, 0, 9, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (991, 'tester', 'list', '2017-09-13 16:53:59', 'namespace', NULL, NULL, 1, '租户分区查询', 6, 0, 10, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (992, 'tester', '', '2017-09-13 16:53:59', 'mirror', NULL, NULL, 1, '镜像仓库', 1, 1, 11, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (993, 'tester', 'list', '2017-09-13 16:53:59', 'mirror', NULL, NULL, 1, '租户镜像仓库查询', 11, 0, 12, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (994, 'tester', 'create', '2017-09-13 16:53:59', 'mirror', NULL, NULL, 1, '租户镜像仓库创建', 11, 0, 13, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (995, 'tester', 'update', '2017-09-13 16:53:59', 'mirror', NULL, NULL, 1, '租户镜像仓库修改', 11, 0, 14, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (996, 'tester', 'delete', '2017-09-13 16:54:00', 'mirror', NULL, NULL, 1, '租户镜像仓库删除', 11, 0, 15, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (997, 'tester', '', '2017-09-13 16:54:00', 'user', NULL, NULL, 1, '用户', 1, 1, 16, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (998, 'tester', 'create', '2017-09-13 16:54:00', 'user', NULL, NULL, 1, '租户用户添加', 16, 0, 17, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (999, 'tester', 'list', '2017-09-13 16:54:00', 'user', NULL, NULL, 1, '租户用户查询', 16, 0, 18, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1000, 'tester', 'delete', '2017-09-13 16:54:00', 'user', NULL, NULL, 1, '租户用户移除', 16, 0, 19, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1001, 'tester', 'update', '2017-09-13 16:54:00', 'user', NULL, NULL, 1, '租户用户修改', 16, 0, 20, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1002, 'tester', 'detail', '2017-09-13 16:54:00', 'namespace', NULL, NULL, 1, '租户分区详情', 6, 0, 21, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1003, 'tester', '', '2017-09-13 16:54:00', 'application', NULL, NULL, 1, '应用中心', 0, 1, 101, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1004, 'tester', '', '2017-09-13 16:54:00', 'application', NULL, NULL, 1, '应用', 101, 1, 102, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1005, 'tester', 'create', '2017-09-13 16:54:00', 'application', NULL, NULL, 1, '应用创建', 102, 0, 103, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1006, 'tester', 'list', '2017-09-13 16:54:00', 'application', NULL, NULL, 1, '应用列表', 102, 0, 104, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1007, 'tester', 'start', '2017-09-13 16:54:00', 'application', NULL, NULL, 1, '应用启动', 102, 0, 105, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1008, 'tester', 'pause', '2017-09-13 16:54:00', 'application', NULL, NULL, 1, '应用暂停', 102, 0, 106, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1009, 'tester', 'delete', '2017-09-13 16:54:00', 'application', NULL, NULL, 1, '应用删除', 102, 0, 107, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1010, 'tester', 'detail', '2017-09-13 16:54:00', 'application', NULL, NULL, 1, '应用详情', 102, 0, 108, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1011, 'tester', '', '2017-09-13 16:54:00', 'service', NULL, NULL, 1, '服务', 101, 1, 111, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1012, 'tester', '', '2017-09-13 16:54:00', 'basic', NULL, NULL, 1, '服务基本信息', 111, 1, 112, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1013, 'tester', 'create', '2017-09-13 16:54:00', 'basic', NULL, NULL, 1, '服务创建', 112, 0, 113, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1014, 'tester', 'start', '2017-09-13 16:54:00', 'basic', NULL, NULL, 1, '服务启动', 112, 0, 114, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1015, 'tester', 'release', '2017-09-13 16:54:00', 'basic', NULL, NULL, 1, '服务发布', 112, 0, 115, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1016, 'tester', 'autoScaling', '2017-09-13 16:54:00', 'basic', NULL, NULL, 1, '服务自动伸缩配置', 112, 0, 116, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1017, 'tester', 'rollback', '2017-09-13 16:54:00', 'basic', NULL, NULL, 1, '服务回滚', 112, 0, 117, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1018, 'tester', 'delete', '2017-09-13 16:54:00', 'basic', NULL, NULL, 1, '服务删除', 112, 0, 118, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1019, 'tester', 'stop', '2017-09-13 16:54:00', 'basic', NULL, NULL, 1, '服务停止', 112, 0, 119, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1020, 'tester', 'console', '2017-09-13 16:54:00', 'basic', NULL, NULL, 1, '服务控制台', 112, 0, 120, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1021, 'tester', 'update', '2017-09-13 16:54:00', 'basic', NULL, NULL, 1, '服务修改', 112, 0, 121, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1022, 'tester', '', '2017-09-13 16:54:00', 'log', NULL, NULL, 1, '服务日志信息', 111, 1, 122, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1023, 'tester', 'get', '2017-09-13 16:54:00', 'log', NULL, NULL, 1, '服务日志查询', 122, 0, 123, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1024, 'tester', 'export', '2017-09-13 16:54:00', 'log', NULL, NULL, 1, '服务日志导出', 122, 0, 124, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1025, 'tester', '', '2017-09-13 16:54:00', 'monitor', NULL, NULL, 1, '服务监控信息', 111, 1, 125, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1026, 'tester', 'get', '2017-09-13 16:54:00', 'monitor', NULL, NULL, 1, '服务监控信息查询', 125, 0, 126, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1027, 'tester', '', '2017-09-13 16:54:00', 'candyUpdate', NULL, NULL, 1, '灰度升级', 111, 1, 127, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1028, 'tester', 'start', '2017-09-13 16:54:00', 'candyUpdate', NULL, NULL, 1, '灰度升级开启', 127, 0, 128, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1029, 'tester', 'edit', '2017-09-13 16:54:00', 'candyUpdate', NULL, NULL, 1, '编辑灰度升级信息', 127, 0, 129, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1030, 'tester', 'rollback', '2017-09-13 16:54:00', 'candyUpdate', NULL, NULL, 1, '回滚到未开始升级的版本', 127, 0, 130, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1031, 'tester', 'update', '2017-09-13 16:54:00', 'candyUpdate', NULL, NULL, 1, '全部升级到新版本', 127, 0, 131, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1032, 'tester', 'upload', '2017-09-13 16:54:00', 'basic', NULL, NULL, 1, '上传文件到容器', 112, 0, 132, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1033, 'tester', '', '2017-09-13 16:54:00', 'external', NULL, NULL, 1, '外部服务', 101, 1, 133, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1034, 'tester', 'create', '2017-09-13 16:54:00', 'external', NULL, NULL, 1, '新建外部服务', 133, 0, 134, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1035, 'tester', 'list', '2017-09-13 16:54:00', 'external', NULL, NULL, 1, '外部服务列表查询', 133, 0, 135, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1036, 'tester', 'update', '2017-09-13 16:54:00', 'external', NULL, NULL, 1, '外部服务修改', 133, 0, 136, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1037, 'tester', 'delete', '2017-09-13 16:54:00', 'external', NULL, NULL, 1, '外部服务删除', 133, 0, 137, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1038, 'tester', '', '2017-09-13 16:54:00', 'configCenter', NULL, NULL, 1, '配置中心', 101, 1, 138, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1039, 'tester', 'create', '2017-09-13 16:54:00', 'configCenter', NULL, NULL, 1, '创建配置文件', 138, 0, 139, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1040, 'tester', 'list', '2017-09-13 16:54:00', 'configCenter', NULL, NULL, 1, '配置文件列表查询', 138, 0, 140, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1041, 'tester', 'update', '2017-09-13 16:54:00', 'configCenter', NULL, NULL, 1, '配置文件修改', 138, 0, 141, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1042, 'tester', 'delete', '2017-09-13 16:54:00', 'configCenter', NULL, NULL, 1, '配置文件删除', 138, 0, 143, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1043, 'tester', 'get', '2017-09-13 16:54:00', 'configCenter', NULL, NULL, 1, '配置文件查看', 138, 0, 144, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1044, 'tester', '', '2017-09-13 16:54:00', 'storage', NULL, NULL, 1, '存储', 101, 1, 145, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1045, 'tester', 'list', '2017-09-13 16:54:00', 'storage', NULL, NULL, 1, '存储列表查询', 145, 0, 146, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1046, 'tester', 'create', '2017-09-13 16:54:00', 'storage', NULL, NULL, 1, '存储创建', 145, 0, 147, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1047, 'tester', 'update', '2017-09-13 16:54:00', 'storage', NULL, NULL, 1, '存储更新', 145, 0, 148, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1048, 'tester', 'delete', '2017-09-13 16:54:00', 'storage', NULL, NULL, 1, '存储删除', 145, 0, 149, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1049, 'tester', '', '2017-09-13 16:54:00', 'cicd', NULL, NULL, 1, 'CICD', 0, 1, 201, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1050, 'tester', '', '2017-09-13 16:54:00', 'dockerFile', NULL, NULL, 1, 'Docker file', 201, 1, 202, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1051, 'tester', 'list', '2017-09-13 16:54:00', 'dockerFile', NULL, NULL, 1, 'DockerFile列表查看', 202, 0, 203, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1052, 'tester', 'get', '2017-09-13 16:54:00', 'dockerFile', NULL, NULL, 1, 'DockerFile查看', 202, 0, 204, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1053, 'tester', 'create', '2017-09-13 16:54:00', 'dockerFile', NULL, NULL, 1, 'DockerFile创建', 202, 0, 205, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1054, 'tester', 'update', '2017-09-13 16:54:00', 'dockerFile', NULL, NULL, 1, 'DockerFile修改', 202, 0, 206, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1055, 'tester', 'delete', '2017-09-13 16:54:00', 'dockerFile', NULL, NULL, 1, 'DockerFile删除', 202, 0, 207, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1056, 'tester', '', '2017-09-13 16:54:00', 'pipeline', NULL, NULL, 1, '流水线', 201, 1, 208, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1057, 'tester', '', '2017-09-13 16:54:00', 'basic', NULL, NULL, 1, '流水线基本信息管理', 208, 1, 209, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1058, 'tester', 'list', '2017-09-13 16:54:00', 'basic', NULL, NULL, 1, '流水线列表查询', 209, 0, 210, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1059, 'tester', 'get', '2017-09-13 16:54:00', 'basic', NULL, NULL, 1, '流水线查询', 209, 0, 211, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1060, 'tester', 'create', '2017-09-13 16:54:00', 'basic', NULL, NULL, 1, '流水线创建', 209, 0, 212, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1061, 'tester', 'start', '2017-09-13 16:54:00', 'basic', NULL, NULL, 1, '流水线任务执行', 209, 0, 213, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1062, 'tester', 'planJob', '2017-09-13 16:54:00', 'basic', NULL, NULL, 1, '持续集成', 209, 0, 214, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1063, 'tester', '', '2017-09-13 16:54:00', 'job', NULL, NULL, 1, '流水线job', 208, 1, 215, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1064, 'tester', 'list', '2017-09-13 16:54:00', 'job', NULL, NULL, 1, 'job列表查询', 215, 0, 216, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1065, 'tester', 'get', '2017-09-13 16:54:00', 'job', NULL, NULL, 1, 'job查询', 215, 0, 217, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1066, 'tester', 'create', '2017-09-13 16:54:00', 'job', NULL, NULL, 1, 'job创建', 215, 0, 218, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1067, 'tester', 'update', '2017-09-13 16:54:00', 'job', NULL, NULL, 1, 'job更新', 215, 0, 219, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1068, 'tester', 'delete', '2017-09-13 16:54:00', 'job', NULL, NULL, 1, 'job删除', 215, 0, 220, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1069, 'tester', 'log', '2017-09-13 16:54:00', 'job', NULL, NULL, 1, 'job日志查询', 215, 0, 221, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1070, 'tester', 'log', '2017-09-13 16:54:00', 'basic', NULL, NULL, 1, '流水线日志查询', 209, 0, 222, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1071, 'tester', '', '2017-09-13 16:54:00', 'dependence', NULL, NULL, 1, '依赖管理', 201, 1, 223, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1072, 'tester', 'list', '2017-09-13 16:54:00', 'dependence', NULL, NULL, 1, '依赖列表查询', 223, 0, 224, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1073, 'tester', 'create', '2017-09-13 16:54:00', 'dependence', NULL, NULL, 1, '依赖创建', 223, 0, 225, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1074, 'tester', 'delete', '2017-09-13 16:54:00', 'dependence', NULL, NULL, 1, '依赖删除', 223, 0, 226, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1075, 'tester', '', '2017-09-13 16:54:00', 'deliveryCenter', NULL, NULL, 1, '交付中心', 0, 1, 227, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1076, 'tester', '', '2017-09-13 16:54:00', 'mirrorRepertory', NULL, NULL, 1, '镜像管理', 227, 1, 228, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1077, 'tester', '', '2017-09-13 16:54:00', 'basic', NULL, NULL, 1, '镜像仓库基本信息', 228, 1, 229, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1078, 'tester', 'list', '2017-09-13 16:54:00', 'basic', NULL, NULL, 1, '镜像仓库镜像列表查询', 229, 0, 230, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1079, 'tester', 'create', '2017-09-13 16:54:00', 'basic', NULL, NULL, 1, '镜像仓库创建', 229, 0, 231, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1080, 'tester', 'delete', '2017-09-13 16:54:00', 'basic', NULL, NULL, 1, '镜像仓库删除', 229, 0, 232, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1081, 'tester', 'detail', '2017-09-13 16:54:00', 'basic', NULL, NULL, 1, '镜像仓库详情', 229, 0, 233, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1082, 'tester', '', '2017-09-13 16:54:00', 'mirror', NULL, NULL, 1, '镜像', 228, 1, 234, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1083, 'tester', '', '2017-09-13 16:54:00', 'history', NULL, NULL, 1, '镜像历史版本', 234, 1, 235, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1084, 'tester', 'delete', '2017-09-13 16:54:00', 'history', NULL, NULL, 1, '镜像历史版本删除', 235, 0, 236, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1085, 'tester', '', '2017-09-13 16:54:00', 'basic', NULL, NULL, 1, '镜像基本信息', 234, 1, 237, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1086, 'tester', 'delete', '2017-09-13 09:02:17', 'basic', NULL, NULL, 1, '镜像删除', 237, 0, 238, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1087, 'tester', 'push', '2017-09-13 09:02:17', 'basic', NULL, NULL, 1, '镜像push', 237, 0, 239, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1088, 'tester', '', '2017-09-13 16:54:00', 'synchronized', NULL, NULL, 1, '镜像仓库同步', 228, 1, 240, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1089, 'tester', 'create', '2017-09-13 16:54:00', 'synchronized', NULL, NULL, 1, '仓库同步规则创建', 240, 0, 241, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1090, 'tester', 'delete', '2017-09-13 16:54:00', 'synchronized', NULL, NULL, 1, '仓库同步规则删除', 240, 0, 242, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1091, 'tester', 'status', '2017-09-13 16:54:00', 'synchronized', NULL, NULL, 1, '仓库同步开启或暂停', 240, 0, 243, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1092, 'tester', 'update', '2017-09-13 16:54:00', 'synchronized', NULL, NULL, 1, '同步设置', 240, 0, 244, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1093, 'tester', 'update', '2017-09-13 16:54:00', 'basic', NULL, NULL, 1, '镜像仓库配额修改', 229, 0, 245, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1094, 'tester', '', '2017-09-13 16:54:00', 'shop', NULL, NULL, 1, '应用商店', 227, 1, 246, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1095, 'tester', 'delivery', '2017-09-13 16:54:00', 'shop', NULL, NULL, 1, '应用商店应用发布', 246, 0, 247, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1096, 'tester', '', '2017-09-13 16:54:00', 'template', NULL, NULL, 1, '模板管理', 227, 1, 248, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1097, 'tester', '', '2017-09-13 16:54:00', 'basic', NULL, NULL, 1, '模板基本信息', 248, 1, 249, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1098, 'tester', 'delete', '2017-09-13 16:54:00', 'basic', NULL, NULL, 1, '应用模板删除', 249, 0, 250, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1099, 'tester', 'delivery', '2017-09-13 16:54:00', 'basic', NULL, NULL, 1, '发布应用', 249, 0, 251, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1100, 'tester', 'detail', '2017-09-13 16:54:00', 'basic', NULL, NULL, 1, '模板详情', 249, 0, 252, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1101, 'tester', 'update', '2017-09-13 16:54:00', 'basic', NULL, NULL, 1, '模板更新', 249, 0, 253, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1102, 'tester', '', '2017-09-13 16:54:00', 'logs', NULL, NULL, 1, '日志管理', 0, 1, 354, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1103, 'tester', '', '2017-09-13 16:54:00', 'audit', NULL, NULL, 1, '操作审计', 354, 1, 355, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1104, 'tester', 'list', '2017-09-13 16:54:00', 'audit', NULL, NULL, 1, '查询操作审计', 355, 0, 356, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1105, 'tester', '', '2017-09-13 16:54:00', 'alarmCenter', NULL, NULL, 1, '告警中心', 0, 1, 400, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1106, 'tester', '', '2017-09-13 16:54:00', 'alarmRule', NULL, NULL, 1, '告警规则', 400, 1, 401, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1107, 'tester', 'create', '2017-09-13 16:54:00', 'alarmRule', NULL, NULL, 1, '创建告警规则', 401, 0, 402, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1108, 'tester', 'delete', '2017-09-13 16:54:00', 'alarmRule', NULL, NULL, 1, '删除告警规则', 401, 0, 403, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1109, 'tester', 'update', '2017-09-13 16:54:00', 'alarmRule', NULL, NULL, 1, '修改告警规则', 401, 0, 404, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1110, 'tester', 'pause', '2017-09-13 16:54:00', 'alarmRule', NULL, NULL, 1, '暂停/启动告警规则', 401, 0, 405, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1111, 'tester', 'list', '2017-09-13 16:54:00', 'alarmRule', NULL, NULL, 1, '查询告警规则', 401, 0, 406, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1112, 'tester', '', '2017-09-13 16:54:00', 'alarmProcessCenter', NULL, NULL, 1, '告警处理中心', 400, 1, 407, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1113, 'tester', 'list', '2017-09-13 16:54:00', 'alarmProcessCenter', NULL, NULL, 1, '报警处理查询', 407, 0, 408, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1114, 'tester', 'update', '2017-09-13 16:54:00', 'alarmProcessCenter', NULL, NULL, 1, '报警处理', 407, 0, 409, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1115, 'tester', '', '2017-09-13 16:54:00', 'systemConfig', NULL, NULL, 0, '系统设置', 0, 1, 410, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1116, 'tester', 'list', '2017-09-13 16:54:00', 'alarmProcessCenter', NULL, NULL, 0, '查询系统设置', 410, 0, 411, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1117, 'tester', 'update', '2017-09-13 16:54:00', 'alarmProcessCenter', NULL, NULL, 0, '更新系统设置', 410, 0, 412, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1217, 'tm', '', '2017-09-13 16:54:00', 'tenant', NULL, NULL, 1, '租户管理', 0, 1, 1, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1218, 'tm', '', '2017-09-13 16:54:00', 'tenant', NULL, NULL, 1, '租户', 1, 1, 2, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1219, 'tm', 'list', '2017-09-13 16:54:00', 'tenant', NULL, NULL, 1, '租户查询', 2, 0, 3, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1220, 'tm', 'create', '2017-09-13 16:54:00', 'tenant', NULL, NULL, 1, '租户创建', 2, 0, 4, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1221, 'tm', 'delete', '2017-09-13 16:54:00', 'tenant', NULL, NULL, 1, '租户删除', 2, 0, 5, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1222, 'tm', '', '2017-09-13 16:54:00', 'namespace', NULL, NULL, 1, '分区', 1, 1, 6, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1223, 'tm', 'create', '2017-09-13 16:54:00', 'namespace', NULL, NULL, 1, '租户分区创建', 6, 0, 7, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1224, 'tm', 'delete', '2017-09-13 16:54:00', 'namespace', NULL, NULL, 1, '租户分区删除', 6, 0, 8, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1225, 'tm', 'update', '2017-09-13 16:54:00', 'namespace', NULL, NULL, 1, '租户分区修改', 6, 0, 9, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1226, 'tm', 'list', '2017-09-13 16:54:00', 'namespace', NULL, NULL, 1, '租户分区查询', 6, 0, 10, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1227, 'tm', '', '2017-09-13 16:54:00', 'mirror', NULL, NULL, 1, '镜像仓库', 1, 1, 11, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1228, 'tm', 'list', '2017-09-13 16:54:00', 'mirror', NULL, NULL, 1, '租户镜像仓库查询', 11, 0, 12, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1229, 'tm', 'create', '2017-09-13 16:54:00', 'mirror', NULL, NULL, 1, '租户镜像仓库创建', 11, 0, 13, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1230, 'tm', 'update', '2017-09-13 16:54:00', 'mirror', NULL, NULL, 1, '租户镜像仓库修改', 11, 0, 14, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1231, 'tm', 'delete', '2017-09-13 16:54:00', 'mirror', NULL, NULL, 1, '租户镜像仓库删除', 11, 0, 15, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1232, 'tm', '', '2017-09-13 16:54:00', 'user', NULL, NULL, 1, '用户', 1, 1, 16, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1233, 'tm', 'create', '2017-09-13 16:54:00', 'user', NULL, NULL, 1, '租户用户添加', 16, 0, 17, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1234, 'tm', 'list', '2017-09-13 16:54:00', 'user', NULL, NULL, 1, '租户用户查询', 16, 0, 18, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1235, 'tm', 'delete', '2017-09-13 16:54:00', 'user', NULL, NULL, 1, '租户用户移除', 16, 0, 19, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1236, 'tm', 'update', '2017-09-13 16:54:00', 'user', NULL, NULL, 1, '租户用户修改', 16, 0, 20, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1237, 'tm', 'detail', '2017-09-13 16:54:00', 'namespace', NULL, NULL, 1, '租户分区详情', 6, 0, 21, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1238, 'tm', '', '2017-09-13 16:54:00', 'application', NULL, NULL, 1, '应用中心', 0, 1, 101, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1239, 'tm', '', '2017-09-13 16:54:00', 'application', NULL, NULL, 1, '应用', 101, 1, 102, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1240, 'tm', 'create', '2017-09-13 16:54:00', 'application', NULL, NULL, 1, '应用创建', 102, 0, 103, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1241, 'tm', 'list', '2017-09-13 16:54:00', 'application', NULL, NULL, 1, '应用列表', 102, 0, 104, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1242, 'tm', 'start', '2017-09-13 16:54:00', 'application', NULL, NULL, 1, '应用启动', 102, 0, 105, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1243, 'tm', 'pause', '2017-09-13 16:54:00', 'application', NULL, NULL, 1, '应用暂停', 102, 0, 106, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1244, 'tm', 'delete', '2017-09-13 16:54:00', 'application', NULL, NULL, 1, '应用删除', 102, 0, 107, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1245, 'tm', 'detail', '2017-09-13 16:54:00', 'application', NULL, NULL, 1, '应用详情', 102, 0, 108, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1246, 'tm', '', '2017-09-13 16:54:00', 'service', NULL, NULL, 1, '服务', 101, 1, 111, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1247, 'tm', '', '2017-09-13 16:54:00', 'basic', NULL, NULL, 1, '服务基本信息', 111, 1, 112, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1248, 'tm', 'create', '2017-09-13 16:54:00', 'basic', NULL, NULL, 1, '服务创建', 112, 0, 113, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1249, 'tm', 'start', '2017-09-13 16:54:00', 'basic', NULL, NULL, 1, '服务启动', 112, 0, 114, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1250, 'tm', 'release', '2017-09-13 16:54:00', 'basic', NULL, NULL, 1, '服务发布', 112, 0, 115, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1251, 'tm', 'autoScaling', '2017-09-13 16:54:00', 'basic', NULL, NULL, 1, '服务自动伸缩配置', 112, 0, 116, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1252, 'tm', 'rollback', '2017-09-13 16:54:00', 'basic', NULL, NULL, 1, '服务回滚', 112, 0, 117, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1253, 'tm', 'delete', '2017-09-13 16:54:00', 'basic', NULL, NULL, 1, '服务删除', 112, 0, 118, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1254, 'tm', 'stop', '2017-09-13 16:54:00', 'basic', NULL, NULL, 1, '服务停止', 112, 0, 119, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1255, 'tm', 'console', '2017-09-13 16:54:00', 'basic', NULL, NULL, 1, '服务控制台', 112, 0, 120, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1256, 'tm', 'update', '2017-09-13 16:54:00', 'basic', NULL, NULL, 1, '服务修改', 112, 0, 121, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1257, 'tm', '', '2017-09-13 16:54:00', 'log', NULL, NULL, 1, '服务日志信息', 111, 1, 122, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1258, 'tm', 'get', '2017-09-13 16:54:00', 'log', NULL, NULL, 1, '服务日志查询', 122, 0, 123, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1259, 'tm', 'export', '2017-09-13 16:54:00', 'log', NULL, NULL, 1, '服务日志导出', 122, 0, 124, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1260, 'tm', '', '2017-09-13 16:54:00', 'monitor', NULL, NULL, 1, '服务监控信息', 111, 1, 125, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1261, 'tm', 'get', '2017-09-13 16:54:00', 'monitor', NULL, NULL, 1, '服务监控信息查询', 125, 0, 126, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1262, 'tm', '', '2017-09-13 16:54:00', 'candyUpdate', NULL, NULL, 1, '灰度升级', 111, 1, 127, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1263, 'tm', 'start', '2017-09-13 16:54:00', 'candyUpdate', NULL, NULL, 1, '灰度升级开启', 127, 0, 128, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1264, 'tm', 'edit', '2017-09-13 16:54:00', 'candyUpdate', NULL, NULL, 1, '编辑灰度升级信息', 127, 0, 129, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1265, 'tm', 'rollback', '2017-09-13 16:54:00', 'candyUpdate', NULL, NULL, 1, '回滚到未开始升级的版本', 127, 0, 130, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1266, 'tm', 'update', '2017-09-13 16:54:00', 'candyUpdate', NULL, NULL, 1, '全部升级到新版本', 127, 0, 131, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1267, 'tm', 'upload', '2017-09-13 16:54:00', 'basic', NULL, NULL, 1, '上传文件到容器', 112, 0, 132, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1268, 'tm', '', '2017-09-13 16:54:00', 'external', NULL, NULL, 1, '外部服务', 101, 1, 133, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1269, 'tm', 'create', '2017-09-13 16:54:00', 'external', NULL, NULL, 1, '新建外部服务', 133, 0, 134, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1270, 'tm', 'list', '2017-09-13 16:54:00', 'external', NULL, NULL, 1, '外部服务列表查询', 133, 0, 135, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1271, 'tm', 'update', '2017-09-13 16:54:00', 'external', NULL, NULL, 1, '外部服务修改', 133, 0, 136, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1272, 'tm', 'delete', '2017-09-13 16:54:00', 'external', NULL, NULL, 1, '外部服务删除', 133, 0, 137, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1273, 'tm', '', '2017-09-13 16:54:00', 'configCenter', NULL, NULL, 1, '配置中心', 101, 1, 138, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1274, 'tm', 'create', '2017-09-13 16:54:00', 'configCenter', NULL, NULL, 1, '创建配置文件', 138, 0, 139, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1275, 'tm', 'list', '2017-09-13 16:54:00', 'configCenter', NULL, NULL, 1, '配置文件列表查询', 138, 0, 140, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1276, 'tm', 'update', '2017-09-13 16:54:00', 'configCenter', NULL, NULL, 1, '配置文件修改', 138, 0, 141, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1277, 'tm', 'delete', '2017-09-13 16:54:00', 'configCenter', NULL, NULL, 1, '配置文件删除', 138, 0, 143, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1278, 'tm', 'get', '2017-09-13 16:54:00', 'configCenter', NULL, NULL, 1, '配置文件查看', 138, 0, 144, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1279, 'tm', '', '2017-09-13 16:54:00', 'storage', NULL, NULL, 1, '存储', 101, 1, 145, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1280, 'tm', 'list', '2017-09-13 16:54:00', 'storage', NULL, NULL, 1, '存储列表查询', 145, 0, 146, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1281, 'tm', 'create', '2017-09-13 16:54:00', 'storage', NULL, NULL, 1, '存储创建', 145, 0, 147, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1282, 'tm', 'update', '2017-09-13 16:54:00', 'storage', NULL, NULL, 1, '存储更新', 145, 0, 148, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1283, 'tm', 'delete', '2017-09-13 16:54:00', 'storage', NULL, NULL, 1, '存储删除', 145, 0, 149, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1284, 'tm', '', '2017-09-13 16:54:00', 'cicd', NULL, NULL, 1, 'CICD', 0, 1, 201, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1285, 'tm', '', '2017-09-13 16:54:00', 'dockerFile', NULL, NULL, 1, 'Docker file', 201, 1, 202, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1286, 'tm', 'list', '2017-09-13 16:54:00', 'dockerFile', NULL, NULL, 1, 'DockerFile列表查看', 202, 0, 203, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1287, 'tm', 'get', '2017-09-13 16:54:00', 'dockerFile', NULL, NULL, 1, 'DockerFile查看', 202, 0, 204, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1288, 'tm', 'create', '2017-09-13 16:54:00', 'dockerFile', NULL, NULL, 1, 'DockerFile创建', 202, 0, 205, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1289, 'tm', 'update', '2017-09-13 16:54:00', 'dockerFile', NULL, NULL, 1, 'DockerFile修改', 202, 0, 206, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1290, 'tm', 'delete', '2017-09-13 16:54:00', 'dockerFile', NULL, NULL, 1, 'DockerFile删除', 202, 0, 207, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1291, 'tm', '', '2017-09-13 16:54:00', 'pipeline', NULL, NULL, 1, '流水线', 201, 1, 208, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1292, 'tm', '', '2017-09-13 16:54:00', 'basic', NULL, NULL, 1, '流水线基本信息管理', 208, 1, 209, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1293, 'tm', 'list', '2017-09-13 16:54:00', 'basic', NULL, NULL, 1, '流水线列表查询', 209, 0, 210, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1294, 'tm', 'get', '2017-09-13 16:54:00', 'basic', NULL, NULL, 1, '流水线查询', 209, 0, 211, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1295, 'tm', 'create', '2017-09-13 16:54:00', 'basic', NULL, NULL, 1, '流水线创建', 209, 0, 212, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1296, 'tm', 'start', '2017-09-13 16:54:00', 'basic', NULL, NULL, 1, '流水线任务执行', 209, 0, 213, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1297, 'tm', 'planJob', '2017-09-13 16:54:00', 'basic', NULL, NULL, 1, '持续集成', 209, 0, 214, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1298, 'tm', '', '2017-09-13 16:54:00', 'job', NULL, NULL, 1, '流水线job', 208, 1, 215, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1299, 'tm', 'list', '2017-09-13 16:54:00', 'job', NULL, NULL, 1, 'job列表查询', 215, 0, 216, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1300, 'tm', 'get', '2017-09-13 16:54:00', 'job', NULL, NULL, 1, 'job查询', 215, 0, 217, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1301, 'tm', 'create', '2017-09-13 16:54:00', 'job', NULL, NULL, 1, 'job创建', 215, 0, 218, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1302, 'tm', 'update', '2017-09-13 16:54:00', 'job', NULL, NULL, 1, 'job更新', 215, 0, 219, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1303, 'tm', 'delete', '2017-09-13 16:54:00', 'job', NULL, NULL, 1, 'job删除', 215, 0, 220, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1304, 'tm', 'log', '2017-09-13 16:54:00', 'job', NULL, NULL, 1, 'job日志查询', 215, 0, 221, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1305, 'tm', 'log', '2017-09-13 16:54:00', 'basic', NULL, NULL, 1, '流水线日志查询', 209, 0, 222, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1306, 'tm', '', '2017-09-13 16:54:00', 'dependence', NULL, NULL, 1, '依赖管理', 201, 1, 223, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1307, 'tm', 'list', '2017-09-13 16:54:00', 'dependence', NULL, NULL, 1, '依赖列表查询', 223, 0, 224, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1308, 'tm', 'create', '2017-09-13 16:54:00', 'dependence', NULL, NULL, 1, '依赖创建', 223, 0, 225, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1309, 'tm', 'delete', '2017-09-13 16:54:00', 'dependence', NULL, NULL, 1, '依赖删除', 223, 0, 226, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1310, 'tm', '', '2017-09-13 16:54:00', 'deliveryCenter', NULL, NULL, 1, '交付中心', 0, 1, 227, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1311, 'tm', '', '2017-09-13 16:54:00', 'mirrorRepertory', NULL, NULL, 1, '镜像管理', 227, 1, 228, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1312, 'tm', '', '2017-09-13 16:54:00', 'basic', NULL, NULL, 1, '镜像仓库基本信息', 228, 1, 229, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1313, 'tm', 'list', '2017-09-13 16:54:00', 'basic', NULL, NULL, 1, '镜像仓库镜像列表查询', 229, 0, 230, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1314, 'tm', 'create', '2017-09-13 16:54:00', 'basic', NULL, NULL, 1, '镜像仓库创建', 229, 0, 231, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1315, 'tm', 'delete', '2017-09-13 16:54:00', 'basic', NULL, NULL, 1, '镜像仓库删除', 229, 0, 232, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1316, 'tm', 'detail', '2017-09-13 16:54:00', 'basic', NULL, NULL, 1, '镜像仓库详情', 229, 0, 233, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1317, 'tm', '', '2017-09-13 16:54:00', 'mirror', NULL, NULL, 1, '镜像', 228, 1, 234, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1318, 'tm', '', '2017-09-13 16:54:00', 'history', NULL, NULL, 1, '镜像历史版本', 234, 1, 235, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1319, 'tm', 'delete', '2017-09-13 16:54:00', 'history', NULL, NULL, 1, '镜像历史版本删除', 235, 0, 236, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1320, 'tm', '', '2017-09-13 16:54:00', 'basic', NULL, NULL, 1, '镜像基本信息', 234, 1, 237, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1321, 'tm', 'delete', '2017-09-13 09:01:53', 'basic', NULL, NULL, 1, '镜像删除', 237, 0, 238, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1322, 'tm', 'push', '2017-09-13 09:01:53', 'basic', NULL, NULL, 1, '镜像push', 237, 0, 239, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1323, 'tm', '', '2017-09-13 16:54:00', 'synchronized', NULL, NULL, 1, '镜像仓库同步', 228, 1, 240, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1324, 'tm', 'create', '2017-09-13 16:54:00', 'synchronized', NULL, NULL, 1, '仓库同步规则创建', 240, 0, 241, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1325, 'tm', 'delete', '2017-09-13 16:54:00', 'synchronized', NULL, NULL, 1, '仓库同步规则删除', 240, 0, 242, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1326, 'tm', 'status', '2017-09-13 16:54:00', 'synchronized', NULL, NULL, 1, '仓库同步开启或暂停', 240, 0, 243, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1327, 'tm', 'update', '2017-09-13 16:54:00', 'synchronized', NULL, NULL, 1, '同步设置', 240, 0, 244, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1328, 'tm', 'update', '2017-09-13 16:54:00', 'basic', NULL, NULL, 1, '镜像仓库配额修改', 229, 0, 245, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1329, 'tm', '', '2017-09-13 16:54:00', 'shop', NULL, NULL, 1, '应用商店', 227, 1, 246, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1330, 'tm', 'delivery', '2017-09-13 16:54:00', 'shop', NULL, NULL, 1, '应用商店应用发布', 246, 0, 247, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1331, 'tm', '', '2017-09-13 16:54:00', 'template', NULL, NULL, 1, '模板管理', 227, 1, 248, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1332, 'tm', '', '2017-09-13 16:54:00', 'basic', NULL, NULL, 1, '模板基本信息', 248, 1, 249, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1333, 'tm', 'delete', '2017-09-13 16:54:00', 'basic', NULL, NULL, 1, '应用模板删除', 249, 0, 250, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1334, 'tm', 'delivery', '2017-09-13 16:54:00', 'basic', NULL, NULL, 1, '发布应用', 249, 0, 251, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1335, 'tm', 'detail', '2017-09-13 16:54:00', 'basic', NULL, NULL, 1, '模板详情', 249, 0, 252, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1336, 'tm', 'update', '2017-09-13 16:54:00', 'basic', NULL, NULL, 1, '模板更新', 249, 0, 253, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1337, 'tm', '', '2017-09-13 16:54:00', 'logs', NULL, NULL, 1, '日志管理', 0, 1, 354, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1338, 'tm', '', '2017-09-13 16:54:00', 'audit', NULL, NULL, 1, '操作审计', 354, 1, 355, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1339, 'tm', 'list', '2017-09-13 16:54:00', 'audit', NULL, NULL, 1, '查询操作审计', 355, 0, 356, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1340, 'tm', '', '2017-09-13 16:54:00', 'alarmCenter', NULL, NULL, 1, '告警中心', 0, 1, 400, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1341, 'tm', '', '2017-09-13 16:54:00', 'alarmRule', NULL, NULL, 1, '告警规则', 400, 1, 401, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1342, 'tm', 'create', '2017-09-13 16:54:00', 'alarmRule', NULL, NULL, 1, '创建告警规则', 401, 0, 402, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1343, 'tm', 'delete', '2017-09-13 16:54:00', 'alarmRule', NULL, NULL, 1, '删除告警规则', 401, 0, 403, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1344, 'tm', 'update', '2017-09-13 16:54:00', 'alarmRule', NULL, NULL, 1, '修改告警规则', 401, 0, 404, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1345, 'tm', 'pause', '2017-09-13 16:54:00', 'alarmRule', NULL, NULL, 1, '暂停/启动告警规则', 401, 0, 405, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1346, 'tm', 'list', '2017-09-13 16:54:00', 'alarmRule', NULL, NULL, 1, '查询告警规则', 401, 0, 406, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1347, 'tm', '', '2017-09-13 16:54:00', 'alarmProcessCenter', NULL, NULL, 1, '告警处理中心', 400, 1, 407, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1348, 'tm', 'list', '2017-09-13 16:54:00', 'alarmProcessCenter', NULL, NULL, 1, '报警处理查询', 407, 0, 408, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1349, 'tm', 'update', '2017-09-13 16:54:00', 'alarmProcessCenter', NULL, NULL, 1, '报警处理', 407, 0, 409, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1350, 'tm', '', '2017-09-13 16:54:00', 'systemConfig', NULL, NULL, 0, '系统设置', 0, 1, 410, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1351, 'tm', 'list', '2017-09-13 16:54:00', 'alarmProcessCenter', NULL, NULL, 0, '查询系统设置', 410, 0, 411, NULL);
INSERT INTO `k8s_auth_server`.`role_privilege`(`id`, `role`, `privilege`, `update_time`, `first_module`, `second_module`, `third_module`, `status`, `mark`, `parent_rpid`, `isParent`, `rpid`, `parentid`) VALUES (1352, 'tm', 'update', '2017-09-13 16:54:00', 'alarmProcessCenter', NULL, NULL, 0, '更新系统设置', 410, 0, 412, NULL);

SET FOREIGN_KEY_CHECKS = 1;
/*
 Navicat Premium Data Transfer

 Source Server         : 10.10.101.74
 Source Server Type    : MySQL
 Source Server Version : 50635
 Source Host           : 10.10.101.74:30306
 Source Schema         : k8s_auth_server

 Target Server Type    : MySQL
 Target Server Version : 50635
 File Encoding         : 65001

 Date: 16/09/2017 13:49:59
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for role_privilege_custom
-- ----------------------------
DROP TABLE IF EXISTS `role_privilege_custom`;
CREATE TABLE `role_privilege_custom`  (
   `id` int(10) NOT NULL AUTO_INCREMENT,
  `role` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '角色名',
  `privilege` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '权限',
  `update_time` timestamp(0) DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(0),
  `first_module` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '一级模块',
  `second_module` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '二级模块',
  `third_module` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '三级模块',
  `status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '权限的状态 0 未启用 1 启用',
  `mark` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '权限说明',
  `parent_rpid` int(100) DEFAULT NULL,
  `isParent` tinyint(1) DEFAULT 0 COMMENT '是否是父节点 0 子节点 1 父节点',
  `rpid` int(10) DEFAULT NULL,
  `parentid` int(10) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `parent_rpid`(`parent_rpid`) USING BTREE,
  INDEX `rpid`(`rpid`) USING BTREE,
  INDEX `role`(`role`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1353 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of role_privilege_custom
-- ----------------------------
INSERT INTO `role_privilege_custom` VALUES (1, 'dev', '', '2017-09-13 16:02:53', 'tenant', NULL, NULL, 1, '租户管理', 0, 1, 1, NULL);
INSERT INTO `role_privilege_custom` VALUES (2, 'dev', '', '2017-09-13 16:02:53', 'tenant', '', NULL, 1, '租户', 1, 1, 2, NULL);
INSERT INTO `role_privilege_custom` VALUES (3, 'dev', 'list', '2017-09-13 16:02:53', 'tenant', NULL, NULL, 1, '租户查询', 2, 0, 3, NULL);
INSERT INTO `role_privilege_custom` VALUES (4, 'dev', 'create', '2017-09-13 16:02:53', 'tenant', '', NULL, 1, '租户创建', 2, 0, 4, NULL);
INSERT INTO `role_privilege_custom` VALUES (5, 'dev', 'delete', '2017-09-13 16:02:53', 'tenant', '', NULL, 1, '租户删除', 2, 0, 5, NULL);
INSERT INTO `role_privilege_custom` VALUES (6, 'dev', '', '2017-09-13 16:02:53', 'namespace', NULL, NULL, 1, '分区', 1, 1, 6, NULL);
INSERT INTO `role_privilege_custom` VALUES (7, 'dev', 'create', '2017-09-13 16:02:53', 'namespace', '', NULL, 1, '租户分区创建', 6, 0, 7, NULL);
INSERT INTO `role_privilege_custom` VALUES (8, 'dev', 'delete', '2017-09-13 16:02:53', 'namespace', '', NULL, 1, '租户分区删除', 6, 0, 8, NULL);
INSERT INTO `role_privilege_custom` VALUES (9, 'dev', 'update', '2017-09-13 16:02:53', 'namespace', '', NULL, 1, '租户分区修改', 6, 0, 9, NULL);
INSERT INTO `role_privilege_custom` VALUES (10, 'dev', 'list', '2017-09-13 16:02:53', 'namespace', '', NULL, 1, '租户分区查询', 6, 0, 10, NULL);
INSERT INTO `role_privilege_custom` VALUES (11, 'dev', '', '2017-09-13 16:02:53', 'mirror', NULL, NULL, 1, '镜像仓库', 1, 1, 11, NULL);
INSERT INTO `role_privilege_custom` VALUES (12, 'dev', 'list', '2017-09-13 16:02:53', 'mirror', '', NULL, 1, '租户镜像仓库查询', 11, 0, 12, NULL);
INSERT INTO `role_privilege_custom` VALUES (13, 'dev', 'create', '2017-09-13 16:02:53', 'mirror', '', NULL, 1, '租户镜像仓库创建', 11, 0, 13, NULL);
INSERT INTO `role_privilege_custom` VALUES (14, 'dev', 'update', '2017-09-13 16:02:53', 'mirror', '', NULL, 1, '租户镜像仓库修改', 11, 0, 14, NULL);
INSERT INTO `role_privilege_custom` VALUES (15, 'dev', 'delete', '2017-09-13 16:02:53', 'mirror', '', NULL, 1, '租户镜像仓库删除', 11, 0, 15, NULL);
INSERT INTO `role_privilege_custom` VALUES (16, 'dev', '', '2017-09-13 16:02:53', 'user', '', NULL, 1, '用户', 1, 1, 16, NULL);
INSERT INTO `role_privilege_custom` VALUES (17, 'dev', 'create', '2017-09-13 16:02:53', 'user', '', NULL, 1, '租户用户添加', 16, 0, 17, NULL);
INSERT INTO `role_privilege_custom` VALUES (18, 'dev', 'list', '2017-09-13 16:02:53', 'user', '', NULL, 1, '租户用户查询', 16, 0, 18, NULL);
INSERT INTO `role_privilege_custom` VALUES (19, 'dev', 'delete', '2017-09-13 16:02:53', 'user', '', NULL, 1, '租户用户移除', 16, 0, 19, NULL);
INSERT INTO `role_privilege_custom` VALUES (20, 'dev', 'update', '2017-09-13 16:02:53', 'user', NULL, NULL, 1, '租户用户修改', 16, 0, 20, NULL);
INSERT INTO `role_privilege_custom` VALUES (21, 'dev', 'detail', '2017-09-13 16:02:53', 'namespace', '', NULL, 1, '租户分区详情', 6, 0, 21, NULL);
INSERT INTO `role_privilege_custom` VALUES (101, 'dev', '', '2017-09-13 16:02:53', 'application', NULL, NULL, 1, '应用中心', 0, 1, 101, NULL);
INSERT INTO `role_privilege_custom` VALUES (102, 'dev', '', '2017-09-13 16:02:53', 'application', NULL, NULL, 1, '应用', 101, 1, 102, NULL);
INSERT INTO `role_privilege_custom` VALUES (103, 'dev', 'create', '2017-09-13 16:02:53', 'application', NULL, NULL, 1, '应用创建', 102, 0, 103, NULL);
INSERT INTO `role_privilege_custom` VALUES (104, 'dev', 'list', '2017-09-13 16:02:53', 'application', NULL, NULL, 1, '应用列表', 102, 0, 104, NULL);
INSERT INTO `role_privilege_custom` VALUES (105, 'dev', 'start', '2017-09-13 16:02:53', 'application', NULL, NULL, 1, '应用启动', 102, 0, 105, NULL);
INSERT INTO `role_privilege_custom` VALUES (106, 'dev', 'pause', '2017-09-13 16:02:53', 'application', NULL, NULL, 1, '应用暂停', 102, 0, 106, NULL);
INSERT INTO `role_privilege_custom` VALUES (107, 'dev', 'delete', '2017-09-13 16:02:53', 'application', NULL, NULL, 1, '应用删除', 102, 0, 107, NULL);
INSERT INTO `role_privilege_custom` VALUES (108, 'dev', 'detail', '2017-09-13 16:02:53', 'application', NULL, NULL, 1, '应用详情', 102, 0, 108, NULL);
INSERT INTO `role_privilege_custom` VALUES (111, 'dev', '', '2017-09-13 16:02:53', 'service', NULL, NULL, 1, '服务', 101, 1, 111, NULL);
INSERT INTO `role_privilege_custom` VALUES (112, 'dev', '', '2017-09-13 16:02:53', 'basic', NULL, NULL, 1, '服务基本信息', 111, 1, 112, NULL);
INSERT INTO `role_privilege_custom` VALUES (113, 'dev', 'create', '2017-09-13 16:02:53', 'basic', NULL, NULL, 1, '服务创建', 112, 0, 113, NULL);
INSERT INTO `role_privilege_custom` VALUES (114, 'dev', 'start', '2017-09-13 16:02:53', 'basic', NULL, NULL, 1, '服务启动', 112, 0, 114, NULL);
INSERT INTO `role_privilege_custom` VALUES (115, 'dev', 'release', '2017-09-13 16:02:53', 'basic', NULL, NULL, 1, '服务发布', 112, 0, 115, NULL);
INSERT INTO `role_privilege_custom` VALUES (116, 'dev', 'autoScaling', '2017-09-13 16:02:53', 'basic', NULL, NULL, 1, '服务自动伸缩配置', 112, 0, 116, NULL);
INSERT INTO `role_privilege_custom` VALUES (117, 'dev', 'rollback', '2017-09-13 16:02:53', 'basic', NULL, NULL, 1, '服务回滚', 112, 0, 117, NULL);
INSERT INTO `role_privilege_custom` VALUES (118, 'dev', 'delete', '2017-09-13 16:02:53', 'basic', NULL, NULL, 1, '服务删除', 112, 0, 118, NULL);
INSERT INTO `role_privilege_custom` VALUES (119, 'dev', 'stop', '2017-09-13 16:02:53', 'basic', NULL, NULL, 1, '服务停止', 112, 0, 119, NULL);
INSERT INTO `role_privilege_custom` VALUES (120, 'dev', 'console', '2017-09-13 16:02:53', 'basic', NULL, NULL, 1, '服务控制台', 112, 0, 120, NULL);
INSERT INTO `role_privilege_custom` VALUES (121, 'dev', 'update', '2017-09-13 16:02:53', 'basic', NULL, NULL, 1, '服务修改', 112, 0, 121, NULL);
INSERT INTO `role_privilege_custom` VALUES (122, 'dev', '', '2017-09-13 16:02:53', 'log', NULL, NULL, 1, '服务日志信息', 111, 1, 122, NULL);
INSERT INTO `role_privilege_custom` VALUES (123, 'dev', 'get', '2017-09-13 16:02:53', 'log', NULL, NULL, 1, '服务日志查询', 122, 0, 123, NULL);
INSERT INTO `role_privilege_custom` VALUES (124, 'dev', 'export', '2017-09-13 16:02:53', 'log', NULL, NULL, 1, '服务日志导出', 122, 0, 124, NULL);
INSERT INTO `role_privilege_custom` VALUES (125, 'dev', '', '2017-09-13 16:02:53', 'monitor', NULL, NULL, 1, '服务监控信息', 111, 1, 125, NULL);
INSERT INTO `role_privilege_custom` VALUES (126, 'dev', 'get', '2017-09-13 16:02:53', 'monitor', NULL, NULL, 1, '服务监控信息查询', 125, 0, 126, NULL);
INSERT INTO `role_privilege_custom` VALUES (127, 'dev', '', '2017-09-13 16:02:53', 'candyUpdate', NULL, NULL, 1, '灰度升级', 111, 1, 127, NULL);
INSERT INTO `role_privilege_custom` VALUES (128, 'dev', 'start', '2017-09-13 16:02:53', 'candyUpdate', NULL, NULL, 1, '灰度升级开启', 127, 0, 128, NULL);
INSERT INTO `role_privilege_custom` VALUES (129, 'dev', 'edit', '2017-09-13 16:02:53', 'candyUpdate', NULL, NULL, 1, '编辑灰度升级信息', 127, 0, 129, NULL);
INSERT INTO `role_privilege_custom` VALUES (130, 'dev', 'rollback', '2017-09-13 16:02:53', 'candyUpdate', NULL, NULL, 1, '回滚到未开始升级的版本', 127, 0, 130, NULL);
INSERT INTO `role_privilege_custom` VALUES (131, 'dev', 'update', '2017-09-13 16:02:53', 'candyUpdate', NULL, NULL, 1, '全部升级到新版本', 127, 0, 131, NULL);
INSERT INTO `role_privilege_custom` VALUES (132, 'dev', 'upload', '2017-09-13 16:02:53', 'basic', NULL, NULL, 1, '上传文件到容器', 112, 0, 132, NULL);
INSERT INTO `role_privilege_custom` VALUES (133, 'dev', '', '2017-09-13 16:02:53', 'external', NULL, NULL, 1, '外部服务', 101, 1, 133, NULL);
INSERT INTO `role_privilege_custom` VALUES (134, 'dev', 'create', '2017-09-13 16:02:53', 'external', NULL, NULL, 1, '新建外部服务', 133, 0, 134, NULL);
INSERT INTO `role_privilege_custom` VALUES (135, 'dev', 'list', '2017-09-13 16:02:53', 'external', NULL, NULL, 1, '外部服务列表查询', 133, 0, 135, NULL);
INSERT INTO `role_privilege_custom` VALUES (136, 'dev', 'update', '2017-09-13 16:02:53', 'external', NULL, NULL, 1, '外部服务修改', 133, 0, 136, NULL);
INSERT INTO `role_privilege_custom` VALUES (137, 'dev', 'delete', '2017-09-13 16:02:53', 'external', NULL, NULL, 1, '外部服务删除', 133, 0, 137, NULL);
INSERT INTO `role_privilege_custom` VALUES (138, 'dev', '', '2017-09-13 16:02:53', 'configCenter', NULL, NULL, 1, '配置中心', 101, 1, 138, NULL);
INSERT INTO `role_privilege_custom` VALUES (139, 'dev', 'create', '2017-09-13 16:02:53', 'configCenter', NULL, NULL, 1, '创建配置文件', 138, 0, 139, NULL);
INSERT INTO `role_privilege_custom` VALUES (140, 'dev', 'list', '2017-09-13 16:02:53', 'configCenter', NULL, NULL, 1, '配置文件列表查询', 138, 0, 140, NULL);
INSERT INTO `role_privilege_custom` VALUES (141, 'dev', 'update', '2017-09-13 16:02:53', 'configCenter', NULL, NULL, 1, '配置文件修改', 138, 0, 141, NULL);
INSERT INTO `role_privilege_custom` VALUES (143, 'dev', 'delete', '2017-09-13 16:02:53', 'configCenter', NULL, NULL, 1, '配置文件删除', 138, 0, 143, NULL);
INSERT INTO `role_privilege_custom` VALUES (144, 'dev', 'get', '2017-09-13 16:02:53', 'configCenter', NULL, NULL, 1, '配置文件查看', 138, 0, 144, NULL);
INSERT INTO `role_privilege_custom` VALUES (145, 'dev', '', '2017-09-13 16:02:53', 'storage', NULL, NULL, 1, '存储', 101, 1, 145, NULL);
INSERT INTO `role_privilege_custom` VALUES (146, 'dev', 'list', '2017-09-13 16:02:53', 'storage', NULL, NULL, 1, '存储列表查询', 145, 0, 146, NULL);
INSERT INTO `role_privilege_custom` VALUES (147, 'dev', 'create', '2017-09-13 16:02:53', 'storage', NULL, NULL, 1, '存储创建', 145, 0, 147, NULL);
INSERT INTO `role_privilege_custom` VALUES (148, 'dev', 'update', '2017-09-13 16:02:53', 'storage', NULL, NULL, 1, '存储更新', 145, 0, 148, NULL);
INSERT INTO `role_privilege_custom` VALUES (149, 'dev', 'delete', '2017-09-13 16:02:53', 'storage', NULL, NULL, 1, '存储删除', 145, 0, 149, NULL);
INSERT INTO `role_privilege_custom` VALUES (201, 'dev', '', '2017-09-13 16:02:53', 'cicd', NULL, NULL, 1, 'CICD', 0, 1, 201, NULL);
INSERT INTO `role_privilege_custom` VALUES (202, 'dev', '', '2017-09-13 08:45:54', 'dockerFile', NULL, NULL, 1, 'Docker file', 201, 1, 202, NULL);
INSERT INTO `role_privilege_custom` VALUES (203, 'dev', 'list', '2017-09-13 16:02:53', 'dockerFile', NULL, NULL, 1, 'DockerFile列表查看', 202, 0, 203, NULL);
INSERT INTO `role_privilege_custom` VALUES (204, 'dev', 'get', '2017-09-13 16:02:53', 'dockerFile', NULL, NULL, 1, 'DockerFile查看', 202, 0, 204, NULL);
INSERT INTO `role_privilege_custom` VALUES (205, 'dev', 'create', '2017-09-13 16:02:53', 'dockerFile', NULL, NULL, 1, 'DockerFile创建', 202, 0, 205, NULL);
INSERT INTO `role_privilege_custom` VALUES (206, 'dev', 'update', '2017-09-13 16:02:53', 'dockerFile', NULL, NULL, 1, 'DockerFile修改', 202, 0, 206, NULL);
INSERT INTO `role_privilege_custom` VALUES (207, 'dev', 'delete', '2017-09-13 16:02:53', 'dockerFile', NULL, NULL, 1, 'DockerFile删除', 202, 0, 207, NULL);
INSERT INTO `role_privilege_custom` VALUES (208, 'dev', '', '2017-09-13 08:45:54', 'pipeline', NULL, NULL, 1, '流水线', 201, 1, 208, NULL);
INSERT INTO `role_privilege_custom` VALUES (209, 'dev', '', '2017-09-13 16:02:53', 'basic', NULL, NULL, 1, '流水线基本信息管理', 208, 1, 209, NULL);
INSERT INTO `role_privilege_custom` VALUES (210, 'dev', 'list', '2017-09-13 16:02:53', 'basic', NULL, NULL, 1, '流水线列表查询', 209, 0, 210, NULL);
INSERT INTO `role_privilege_custom` VALUES (211, 'dev', 'get', '2017-09-13 16:02:53', 'basic', NULL, NULL, 1, '流水线查询', 209, 0, 211, NULL);
INSERT INTO `role_privilege_custom` VALUES (212, 'dev', 'create', '2017-09-13 16:02:53', 'basic', NULL, NULL, 1, '流水线创建', 209, 0, 212, NULL);
INSERT INTO `role_privilege_custom` VALUES (213, 'dev', 'start', '2017-09-13 16:02:53', 'basic', NULL, NULL, 1, '流水线任务执行', 209, 0, 213, NULL);
INSERT INTO `role_privilege_custom` VALUES (214, 'dev', 'planJob', '2017-09-13 16:02:53', 'basic', NULL, NULL, 1, '持续集成', 209, 0, 214, NULL);
INSERT INTO `role_privilege_custom` VALUES (215, 'dev', '', '2017-09-13 16:02:53', 'job', NULL, NULL, 1, '流水线job', 208, 1, 215, NULL);
INSERT INTO `role_privilege_custom` VALUES (216, 'dev', 'list', '2017-09-13 16:02:53', 'job', NULL, NULL, 1, 'job列表查询', 215, 0, 216, NULL);
INSERT INTO `role_privilege_custom` VALUES (217, 'dev', 'get', '2017-09-13 16:02:53', 'job', NULL, NULL, 1, 'job查询', 215, 0, 217, NULL);
INSERT INTO `role_privilege_custom` VALUES (218, 'dev', 'create', '2017-09-13 16:02:53', 'job', NULL, NULL, 1, 'job创建', 215, 0, 218, NULL);
INSERT INTO `role_privilege_custom` VALUES (219, 'dev', 'update', '2017-09-13 16:02:53', 'job', NULL, NULL, 1, 'job更新', 215, 0, 219, NULL);
INSERT INTO `role_privilege_custom` VALUES (220, 'dev', 'delete', '2017-09-13 16:02:53', 'job', NULL, NULL, 1, 'job删除', 215, 0, 220, NULL);
INSERT INTO `role_privilege_custom` VALUES (221, 'dev', 'log', '2017-09-13 16:02:53', 'job', NULL, NULL, 1, 'job日志查询', 215, 0, 221, NULL);
INSERT INTO `role_privilege_custom` VALUES (222, 'dev', 'log', '2017-09-13 16:02:53', 'basic', NULL, NULL, 1, '流水线日志查询', 209, 0, 222, NULL);
INSERT INTO `role_privilege_custom` VALUES (223, 'dev', '', '2017-09-13 16:02:53', 'dependence', NULL, NULL, 1, '依赖管理', 201, 1, 223, NULL);
INSERT INTO `role_privilege_custom` VALUES (224, 'dev', 'list', '2017-09-13 16:02:53', 'dependence', NULL, NULL, 1, '依赖列表查询', 223, 0, 224, NULL);
INSERT INTO `role_privilege_custom` VALUES (225, 'dev', 'create', '2017-09-13 16:02:53', 'dependence', NULL, NULL, 1, '依赖创建', 223, 0, 225, NULL);
INSERT INTO `role_privilege_custom` VALUES (226, 'dev', 'delete', '2017-09-13 16:02:53', 'dependence', NULL, NULL, 1, '依赖删除', 223, 0, 226, NULL);
INSERT INTO `role_privilege_custom` VALUES (227, 'dev', '', '2017-09-13 16:02:53', 'deliveryCenter', NULL, NULL, 1, '交付中心', 0, 1, 227, NULL);
INSERT INTO `role_privilege_custom` VALUES (228, 'dev', '', '2017-09-13 08:45:54', 'mirrorRepertory', NULL, NULL, 1, '镜像管理', 227, 1, 228, NULL);
INSERT INTO `role_privilege_custom` VALUES (229, 'dev', '', '2017-09-13 16:02:53', 'basic', NULL, NULL, 1, '镜像仓库基本信息', 228, 1, 229, NULL);
INSERT INTO `role_privilege_custom` VALUES (230, 'dev', 'list', '2017-09-13 16:02:53', 'basic', NULL, NULL, 1, '镜像仓库镜像列表查询', 229, 0, 230, NULL);
INSERT INTO `role_privilege_custom` VALUES (231, 'dev', 'create', '2017-09-13 16:02:53', 'basic', NULL, NULL, 1, '镜像仓库创建', 229, 0, 231, NULL);
INSERT INTO `role_privilege_custom` VALUES (232, 'dev', 'delete', '2017-09-13 16:02:53', 'basic', NULL, NULL, 1, '镜像仓库删除', 229, 0, 232, NULL);
INSERT INTO `role_privilege_custom` VALUES (233, 'dev', 'detail', '2017-09-13 16:02:53', 'basic', NULL, NULL, 1, '镜像仓库详情', 229, 0, 233, NULL);
INSERT INTO `role_privilege_custom` VALUES (234, 'dev', '', '2017-09-13 16:02:53', 'mirror', NULL, NULL, 1, '镜像', 228, 1, 234, NULL);
INSERT INTO `role_privilege_custom` VALUES (235, 'dev', '', '2017-09-13 16:02:53', 'history', NULL, NULL, 1, '镜像历史版本', 234, 1, 235, NULL);
INSERT INTO `role_privilege_custom` VALUES (236, 'dev', 'delete', '2017-09-13 16:02:53', 'history', NULL, NULL, 1, '镜像历史版本删除', 235, 0, 236, NULL);
INSERT INTO `role_privilege_custom` VALUES (237, 'dev', '', '2017-09-13 16:02:53', 'basic', NULL, NULL, 1, '镜像基本信息', 234, 1, 237, NULL);
INSERT INTO `role_privilege_custom` VALUES (238, 'dev', 'delete', '2017-09-13 16:02:53', 'basic', NULL, NULL, 1, '镜像删除', 235, 0, 238, NULL);
INSERT INTO `role_privilege_custom` VALUES (239, 'dev', 'push', '2017-09-13 16:02:53', 'basic', NULL, NULL, 1, '镜像push', 235, 0, 239, NULL);
INSERT INTO `role_privilege_custom` VALUES (240, 'dev', '', '2017-09-13 16:02:53', 'synchronized', NULL, NULL, 1, '镜像仓库同步', 228, 1, 240, NULL);
INSERT INTO `role_privilege_custom` VALUES (241, 'dev', 'create', '2017-09-13 16:02:53', 'synchronized', NULL, NULL, 1, '仓库同步规则创建', 240, 0, 241, NULL);
INSERT INTO `role_privilege_custom` VALUES (242, 'dev', 'delete', '2017-09-13 16:02:53', 'synchronized', NULL, NULL, 1, '仓库同步规则删除', 240, 0, 242, NULL);
INSERT INTO `role_privilege_custom` VALUES (243, 'dev', 'status', '2017-09-13 16:02:53', 'synchronized', NULL, NULL, 1, '仓库同步开启或暂停', 240, 0, 243, NULL);
INSERT INTO `role_privilege_custom` VALUES (244, 'dev', 'update', '2017-09-13 16:02:53', 'synchronized', NULL, NULL, 1, '同步设置', 240, 0, 244, NULL);
INSERT INTO `role_privilege_custom` VALUES (245, 'dev', 'update', '2017-09-13 16:02:53', 'basic', NULL, NULL, 1, '镜像仓库配额修改', 229, 0, 245, NULL);
INSERT INTO `role_privilege_custom` VALUES (246, 'dev', '', '2017-09-13 16:02:53', 'shop', NULL, NULL, 1, '应用商店', 227, 1, 246, NULL);
INSERT INTO `role_privilege_custom` VALUES (247, 'dev', 'delivery', '2017-09-13 16:02:53', 'shop', NULL, NULL, 1, '应用商店应用发布', 246, 0, 247, NULL);
INSERT INTO `role_privilege_custom` VALUES (248, 'dev', '', '2017-09-13 08:45:55', 'template', NULL, NULL, 1, '模板管理', 227, 1, 248, NULL);
INSERT INTO `role_privilege_custom` VALUES (249, 'dev', '', '2017-09-13 16:02:53', 'basic', NULL, NULL, 1, '模板基本信息', 248, 1, 249, NULL);
INSERT INTO `role_privilege_custom` VALUES (250, 'dev', 'delete', '2017-09-13 16:02:53', 'basic', NULL, NULL, 1, '应用模板删除', 249, 0, 250, NULL);
INSERT INTO `role_privilege_custom` VALUES (251, 'dev', 'delivery', '2017-09-13 16:02:53', 'basic', NULL, NULL, 1, '发布应用', 249, 0, 251, NULL);
INSERT INTO `role_privilege_custom` VALUES (252, 'dev', 'detail', '2017-09-13 16:02:53', 'basic', NULL, NULL, 1, '模板详情', 249, 0, 252, NULL);
INSERT INTO `role_privilege_custom` VALUES (253, 'dev', 'update', '2017-09-13 16:02:53', 'basic', NULL, NULL, 1, '模板更新', 249, 0, 253, NULL);
INSERT INTO `role_privilege_custom` VALUES (354, 'dev', '', '2017-09-13 16:02:53', 'logs', NULL, NULL, 1, '日志管理', 0, 1, 354, NULL);
INSERT INTO `role_privilege_custom` VALUES (355, 'dev', '', '2017-09-13 16:02:53', 'audit', NULL, NULL, 1, '操作审计', 354, 1, 355, NULL);
INSERT INTO `role_privilege_custom` VALUES (356, 'dev', 'list', '2017-09-13 16:02:53', 'audit', NULL, NULL, 1, '查询操作审计', 355, 0, 356, NULL);
INSERT INTO `role_privilege_custom` VALUES (400, 'dev', '', '2017-09-13 16:02:53', 'alarmCenter', NULL, NULL, 1, '告警中心', 0, 1, 400, NULL);
INSERT INTO `role_privilege_custom` VALUES (401, 'dev', '', '2017-09-13 16:02:53', 'alarmRule', NULL, NULL, 1, '告警规则', 400, 1, 401, NULL);
INSERT INTO `role_privilege_custom` VALUES (402, 'dev', 'create', '2017-09-13 16:02:53', 'alarmRule', NULL, NULL, 1, '创建告警规则', 401, 0, 402, NULL);
INSERT INTO `role_privilege_custom` VALUES (403, 'dev', 'delete', '2017-09-13 16:02:53', 'alarmRule', NULL, NULL, 1, '删除告警规则', 401, 0, 403, NULL);
INSERT INTO `role_privilege_custom` VALUES (404, 'dev', 'update', '2017-09-13 16:02:53', 'alarmRule', NULL, NULL, 1, '修改告警规则', 401, 0, 404, NULL);
INSERT INTO `role_privilege_custom` VALUES (405, 'dev', 'pause', '2017-09-13 16:02:53', 'alarmRule', NULL, NULL, 1, '暂停/启动告警规则', 401, 0, 405, NULL);
INSERT INTO `role_privilege_custom` VALUES (406, 'dev', 'list', '2017-09-13 16:02:53', 'alarmRule', NULL, NULL, 1, '查询告警规则', 401, 0, 406, NULL);
INSERT INTO `role_privilege_custom` VALUES (407, 'dev', '', '2017-09-13 16:02:53', 'alarmProcessCenter', NULL, NULL, 1, '告警处理中心', 400, 1, 407, NULL);
INSERT INTO `role_privilege_custom` VALUES (408, 'dev', 'list', '2017-09-13 16:02:53', 'alarmProcessCenter', NULL, NULL, 1, '报警处理查询', 407, 0, 408, NULL);
INSERT INTO `role_privilege_custom` VALUES (409, 'dev', 'update', '2017-09-13 16:02:53', 'alarmProcessCenter', NULL, NULL, 1, '报警处理', 407, 0, 409, NULL);
INSERT INTO `role_privilege_custom` VALUES (410, 'dev', '', '2017-09-13 16:02:53', 'systemConfig', NULL, NULL, 0, '系统设置', 0, 1, 410, NULL);
INSERT INTO `role_privilege_custom` VALUES (411, 'dev', 'list', '2017-09-13 16:02:53', 'alarmProcessCenter', NULL, NULL, 0, '查询系统设置', 410, 0, 411, NULL);
INSERT INTO `role_privilege_custom` VALUES (412, 'dev', 'update', '2017-09-13 16:02:53', 'alarmProcessCenter', NULL, NULL, 0, '更新系统设置', 410, 0, 412, NULL);
INSERT INTO `role_privilege_custom` VALUES (512, 'default', '', '2017-09-13 16:02:53', 'tenant', NULL, NULL, 1, '租户管理', 0, 1, 1, NULL);
INSERT INTO `role_privilege_custom` VALUES (513, 'default', '', '2017-09-13 16:02:53', 'tenant', NULL, NULL, 1, '租户', 1, 1, 2, NULL);
INSERT INTO `role_privilege_custom` VALUES (514, 'default', 'list', '2017-09-13 16:02:53', 'tenant', NULL, NULL, 1, '租户查询', 2, 0, 3, NULL);
INSERT INTO `role_privilege_custom` VALUES (515, 'default', 'create', '2017-09-13 16:02:53', 'tenant', NULL, NULL, 1, '租户创建', 2, 0, 4, NULL);
INSERT INTO `role_privilege_custom` VALUES (516, 'default', 'delete', '2017-09-13 16:02:53', 'tenant', NULL, NULL, 1, '租户删除', 2, 0, 5, NULL);
INSERT INTO `role_privilege_custom` VALUES (517, 'default', '', '2017-09-13 16:02:53', 'namespace', NULL, NULL, 1, '分区', 1, 1, 6, NULL);
INSERT INTO `role_privilege_custom` VALUES (518, 'default', 'create', '2017-09-13 16:02:53', 'namespace', NULL, NULL, 1, '租户分区创建', 6, 0, 7, NULL);
INSERT INTO `role_privilege_custom` VALUES (519, 'default', 'delete', '2017-09-13 16:02:53', 'namespace', NULL, NULL, 1, '租户分区删除', 6, 0, 8, NULL);
INSERT INTO `role_privilege_custom` VALUES (520, 'default', 'update', '2017-09-13 16:02:53', 'namespace', NULL, NULL, 1, '租户分区修改', 6, 0, 9, NULL);
INSERT INTO `role_privilege_custom` VALUES (521, 'default', 'list', '2017-09-13 16:02:53', 'namespace', NULL, NULL, 1, '租户分区查询', 6, 0, 10, NULL);
INSERT INTO `role_privilege_custom` VALUES (522, 'default', '', '2017-09-13 16:02:53', 'mirror', NULL, NULL, 1, '镜像仓库', 1, 1, 11, NULL);
INSERT INTO `role_privilege_custom` VALUES (523, 'default', 'list', '2017-09-13 16:02:53', 'mirror', NULL, NULL, 1, '租户镜像仓库查询', 11, 0, 12, NULL);
INSERT INTO `role_privilege_custom` VALUES (524, 'default', 'create', '2017-09-13 16:02:53', 'mirror', NULL, NULL, 1, '租户镜像仓库创建', 11, 0, 13, NULL);
INSERT INTO `role_privilege_custom` VALUES (525, 'default', 'update', '2017-09-13 16:02:53', 'mirror', NULL, NULL, 1, '租户镜像仓库修改', 11, 0, 14, NULL);
INSERT INTO `role_privilege_custom` VALUES (526, 'default', 'delete', '2017-09-13 16:02:53', 'mirror', NULL, NULL, 1, '租户镜像仓库删除', 11, 0, 15, NULL);
INSERT INTO `role_privilege_custom` VALUES (527, 'default', '', '2017-09-13 16:02:53', 'user', NULL, NULL, 1, '用户', 1, 1, 16, NULL);
INSERT INTO `role_privilege_custom` VALUES (528, 'default', 'create', '2017-09-13 16:02:53', 'user', NULL, NULL, 1, '租户用户添加', 16, 0, 17, NULL);
INSERT INTO `role_privilege_custom` VALUES (529, 'default', 'list', '2017-09-13 16:02:53', 'user', NULL, NULL, 1, '租户用户查询', 16, 0, 18, NULL);
INSERT INTO `role_privilege_custom` VALUES (530, 'default', 'delete', '2017-09-13 16:02:53', 'user', NULL, NULL, 1, '租户用户移除', 16, 0, 19, NULL);
INSERT INTO `role_privilege_custom` VALUES (531, 'default', 'update', '2017-09-13 16:02:53', 'user', NULL, NULL, 1, '租户用户修改', 16, 0, 20, NULL);
INSERT INTO `role_privilege_custom` VALUES (532, 'default', 'detail', '2017-09-13 16:02:53', 'namespace', NULL, NULL, 1, '租户分区详情', 6, 0, 21, NULL);
INSERT INTO `role_privilege_custom` VALUES (533, 'default', '', '2017-09-13 16:02:53', 'application', NULL, NULL, 1, '应用中心', 0, 1, 101, NULL);
INSERT INTO `role_privilege_custom` VALUES (534, 'default', '', '2017-09-13 16:02:53', 'application', NULL, NULL, 1, '应用', 101, 1, 102, NULL);
INSERT INTO `role_privilege_custom` VALUES (535, 'default', 'create', '2017-09-13 16:02:53', 'application', NULL, NULL, 1, '应用创建', 102, 0, 103, NULL);
INSERT INTO `role_privilege_custom` VALUES (536, 'default', 'list', '2017-09-13 16:02:53', 'application', NULL, NULL, 1, '应用列表', 102, 0, 104, NULL);
INSERT INTO `role_privilege_custom` VALUES (537, 'default', 'start', '2017-09-13 16:02:53', 'application', NULL, NULL, 1, '应用启动', 102, 0, 105, NULL);
INSERT INTO `role_privilege_custom` VALUES (538, 'default', 'pause', '2017-09-13 16:02:53', 'application', NULL, NULL, 1, '应用暂停', 102, 0, 106, NULL);
INSERT INTO `role_privilege_custom` VALUES (539, 'default', 'delete', '2017-09-13 16:02:53', 'application', NULL, NULL, 1, '应用删除', 102, 0, 107, NULL);
INSERT INTO `role_privilege_custom` VALUES (540, 'default', 'detail', '2017-09-13 16:02:53', 'application', NULL, NULL, 1, '应用详情', 102, 0, 108, NULL);
INSERT INTO `role_privilege_custom` VALUES (541, 'default', '', '2017-09-13 16:02:53', 'service', NULL, NULL, 1, '服务', 101, 1, 111, NULL);
INSERT INTO `role_privilege_custom` VALUES (542, 'default', '', '2017-09-13 16:02:53', 'basic', NULL, NULL, 1, '服务基本信息', 111, 1, 112, NULL);
INSERT INTO `role_privilege_custom` VALUES (543, 'default', 'create', '2017-09-13 16:02:53', 'basic', NULL, NULL, 1, '服务创建', 112, 0, 113, NULL);
INSERT INTO `role_privilege_custom` VALUES (544, 'default', 'start', '2017-09-13 16:02:53', 'basic', NULL, NULL, 1, '服务启动', 112, 0, 114, NULL);
INSERT INTO `role_privilege_custom` VALUES (545, 'default', 'release', '2017-09-13 16:02:53', 'basic', NULL, NULL, 1, '服务发布', 112, 0, 115, NULL);
INSERT INTO `role_privilege_custom` VALUES (546, 'default', 'autoScaling', '2017-09-13 16:02:53', 'basic', NULL, NULL, 1, '服务自动伸缩配置', 112, 0, 116, NULL);
INSERT INTO `role_privilege_custom` VALUES (547, 'default', 'rollback', '2017-09-13 16:02:53', 'basic', NULL, NULL, 1, '服务回滚', 112, 0, 117, NULL);
INSERT INTO `role_privilege_custom` VALUES (548, 'default', 'delete', '2017-09-13 16:02:53', 'basic', NULL, NULL, 1, '服务删除', 112, 0, 118, NULL);
INSERT INTO `role_privilege_custom` VALUES (549, 'default', 'stop', '2017-09-13 16:02:53', 'basic', NULL, NULL, 1, '服务停止', 112, 0, 119, NULL);
INSERT INTO `role_privilege_custom` VALUES (550, 'default', 'console', '2017-09-13 16:02:53', 'basic', NULL, NULL, 1, '服务控制台', 112, 0, 120, NULL);
INSERT INTO `role_privilege_custom` VALUES (551, 'default', 'update', '2017-09-13 16:02:53', 'basic', NULL, NULL, 1, '服务修改', 112, 0, 121, NULL);
INSERT INTO `role_privilege_custom` VALUES (552, 'default', '', '2017-09-13 16:02:53', 'log', NULL, NULL, 1, '服务日志信息', 111, 1, 122, NULL);
INSERT INTO `role_privilege_custom` VALUES (553, 'default', 'get', '2017-09-13 16:02:53', 'log', NULL, NULL, 1, '服务日志查询', 122, 0, 123, NULL);
INSERT INTO `role_privilege_custom` VALUES (554, 'default', 'export', '2017-09-13 16:02:53', 'log', NULL, NULL, 1, '服务日志导出', 122, 0, 124, NULL);
INSERT INTO `role_privilege_custom` VALUES (555, 'default', '', '2017-09-13 16:02:53', 'monitor', NULL, NULL, 1, '服务监控信息', 111, 1, 125, NULL);
INSERT INTO `role_privilege_custom` VALUES (556, 'default', 'get', '2017-09-13 16:02:53', 'monitor', NULL, NULL, 1, '服务监控信息查询', 125, 0, 126, NULL);
INSERT INTO `role_privilege_custom` VALUES (557, 'default', '', '2017-09-13 16:02:53', 'candyUpdate', NULL, NULL, 1, '灰度升级', 111, 1, 127, NULL);
INSERT INTO `role_privilege_custom` VALUES (558, 'default', 'start', '2017-09-13 16:02:53', 'candyUpdate', NULL, NULL, 1, '灰度升级开启', 127, 0, 128, NULL);
INSERT INTO `role_privilege_custom` VALUES (559, 'default', 'edit', '2017-09-13 16:02:53', 'candyUpdate', NULL, NULL, 1, '编辑灰度升级信息', 127, 0, 129, NULL);
INSERT INTO `role_privilege_custom` VALUES (560, 'default', 'rollback', '2017-09-13 16:02:53', 'candyUpdate', NULL, NULL, 1, '回滚到未开始升级的版本', 127, 0, 130, NULL);
INSERT INTO `role_privilege_custom` VALUES (561, 'default', 'update', '2017-09-13 16:02:53', 'candyUpdate', NULL, NULL, 1, '全部升级到新版本', 127, 0, 131, NULL);
INSERT INTO `role_privilege_custom` VALUES (562, 'default', 'upload', '2017-09-13 16:02:53', 'basic', NULL, NULL, 1, '上传文件到容器', 112, 0, 132, NULL);
INSERT INTO `role_privilege_custom` VALUES (563, 'default', '', '2017-09-13 16:02:53', 'external', NULL, NULL, 1, '外部服务', 101, 1, 133, NULL);
INSERT INTO `role_privilege_custom` VALUES (564, 'default', 'create', '2017-09-13 16:02:53', 'external', NULL, NULL, 1, '新建外部服务', 133, 0, 134, NULL);
INSERT INTO `role_privilege_custom` VALUES (565, 'default', 'list', '2017-09-13 16:02:53', 'external', NULL, NULL, 1, '外部服务列表查询', 133, 0, 135, NULL);
INSERT INTO `role_privilege_custom` VALUES (566, 'default', 'update', '2017-09-13 16:02:53', 'external', NULL, NULL, 1, '外部服务修改', 133, 0, 136, NULL);
INSERT INTO `role_privilege_custom` VALUES (567, 'default', 'delete', '2017-09-13 16:02:53', 'external', NULL, NULL, 1, '外部服务删除', 133, 0, 137, NULL);
INSERT INTO `role_privilege_custom` VALUES (568, 'default', '', '2017-09-13 16:02:53', 'configCenter', NULL, NULL, 1, '配置中心', 101, 1, 138, NULL);
INSERT INTO `role_privilege_custom` VALUES (569, 'default', 'create', '2017-09-13 16:02:53', 'configCenter', NULL, NULL, 1, '创建配置文件', 138, 0, 139, NULL);
INSERT INTO `role_privilege_custom` VALUES (570, 'default', 'list', '2017-09-13 16:02:53', 'configCenter', NULL, NULL, 1, '配置文件列表查询', 138, 0, 140, NULL);
INSERT INTO `role_privilege_custom` VALUES (571, 'default', 'update', '2017-09-13 16:02:53', 'configCenter', NULL, NULL, 1, '配置文件修改', 138, 0, 141, NULL);
INSERT INTO `role_privilege_custom` VALUES (572, 'default', 'delete', '2017-09-13 16:02:53', 'configCenter', NULL, NULL, 1, '配置文件删除', 138, 0, 143, NULL);
INSERT INTO `role_privilege_custom` VALUES (573, 'default', 'get', '2017-09-13 16:02:53', 'configCenter', NULL, NULL, 1, '配置文件查看', 138, 0, 144, NULL);
INSERT INTO `role_privilege_custom` VALUES (574, 'default', '', '2017-09-13 16:02:53', 'storage', NULL, NULL, 1, '存储', 101, 1, 145, NULL);
INSERT INTO `role_privilege_custom` VALUES (575, 'default', 'list', '2017-09-13 16:02:53', 'storage', NULL, NULL, 1, '存储列表查询', 145, 0, 146, NULL);
INSERT INTO `role_privilege_custom` VALUES (576, 'default', 'create', '2017-09-13 16:02:53', 'storage', NULL, NULL, 1, '存储创建', 145, 0, 147, NULL);
INSERT INTO `role_privilege_custom` VALUES (577, 'default', 'update', '2017-09-13 16:02:53', 'storage', NULL, NULL, 1, '存储更新', 145, 0, 148, NULL);
INSERT INTO `role_privilege_custom` VALUES (578, 'default', 'delete', '2017-09-13 16:02:53', 'storage', NULL, NULL, 1, '存储删除', 145, 0, 149, NULL);
INSERT INTO `role_privilege_custom` VALUES (579, 'default', '', '2017-09-13 16:02:53', 'cicd', NULL, NULL, 1, 'CICD', 0, 1, 201, NULL);
INSERT INTO `role_privilege_custom` VALUES (580, 'default', '', '2017-09-13 08:26:12', 'dockerFile', NULL, NULL, 1, 'Docker file', 201, 1, 202, NULL);
INSERT INTO `role_privilege_custom` VALUES (581, 'default', 'list', '2017-09-13 16:02:53', 'dockerFile', NULL, NULL, 1, 'DockerFile列表查看', 202, 0, 203, NULL);
INSERT INTO `role_privilege_custom` VALUES (582, 'default', 'get', '2017-09-13 16:02:53', 'dockerFile', NULL, NULL, 1, 'DockerFile查看', 202, 0, 204, NULL);
INSERT INTO `role_privilege_custom` VALUES (583, 'default', 'create', '2017-09-13 16:02:53', 'dockerFile', NULL, NULL, 1, 'DockerFile创建', 202, 0, 205, NULL);
INSERT INTO `role_privilege_custom` VALUES (584, 'default', 'update', '2017-09-13 16:02:53', 'dockerFile', NULL, NULL, 1, 'DockerFile修改', 202, 0, 206, NULL);
INSERT INTO `role_privilege_custom` VALUES (585, 'default', 'delete', '2017-09-13 16:02:53', 'dockerFile', NULL, NULL, 1, 'DockerFile删除', 202, 0, 207, NULL);
INSERT INTO `role_privilege_custom` VALUES (586, 'default', '', '2017-09-13 08:26:12', 'pipeline', NULL, NULL, 1, '流水线', 201, 1, 208, NULL);
INSERT INTO `role_privilege_custom` VALUES (587, 'default', '', '2017-09-13 16:02:53', 'basic', NULL, NULL, 1, '流水线基本信息管理', 208, 1, 209, NULL);
INSERT INTO `role_privilege_custom` VALUES (588, 'default', 'list', '2017-09-13 16:02:53', 'basic', NULL, NULL, 1, '流水线列表查询', 209, 0, 210, NULL);
INSERT INTO `role_privilege_custom` VALUES (589, 'default', 'get', '2017-09-13 16:02:53', 'basic', NULL, NULL, 1, '流水线查询', 209, 0, 211, NULL);
INSERT INTO `role_privilege_custom` VALUES (590, 'default', 'create', '2017-09-13 16:02:53', 'basic', NULL, NULL, 1, '流水线创建', 209, 0, 212, NULL);
INSERT INTO `role_privilege_custom` VALUES (591, 'default', 'start', '2017-09-13 16:02:53', 'basic', NULL, NULL, 1, '流水线任务执行', 209, 0, 213, NULL);
INSERT INTO `role_privilege_custom` VALUES (592, 'default', 'planJob', '2017-09-13 16:02:53', 'basic', NULL, NULL, 1, '持续集成', 209, 0, 214, NULL);
INSERT INTO `role_privilege_custom` VALUES (593, 'default', '', '2017-09-13 16:02:53', 'job', NULL, NULL, 1, '流水线job', 208, 1, 215, NULL);
INSERT INTO `role_privilege_custom` VALUES (594, 'default', 'list', '2017-09-13 16:02:53', 'job', NULL, NULL, 1, 'job列表查询', 215, 0, 216, NULL);
INSERT INTO `role_privilege_custom` VALUES (595, 'default', 'get', '2017-09-13 16:02:53', 'job', NULL, NULL, 1, 'job查询', 215, 0, 217, NULL);
INSERT INTO `role_privilege_custom` VALUES (596, 'default', 'create', '2017-09-13 16:02:53', 'job', NULL, NULL, 1, 'job创建', 215, 0, 218, NULL);
INSERT INTO `role_privilege_custom` VALUES (597, 'default', 'update', '2017-09-13 16:02:53', 'job', NULL, NULL, 1, 'job更新', 215, 0, 219, NULL);
INSERT INTO `role_privilege_custom` VALUES (598, 'default', 'delete', '2017-09-13 16:02:53', 'job', NULL, NULL, 1, 'job删除', 215, 0, 220, NULL);
INSERT INTO `role_privilege_custom` VALUES (599, 'default', 'log', '2017-09-13 16:02:53', 'job', NULL, NULL, 1, 'job日志查询', 215, 0, 221, NULL);
INSERT INTO `role_privilege_custom` VALUES (600, 'default', 'log', '2017-09-13 16:02:53', 'basic', NULL, NULL, 1, '流水线日志查询', 209, 0, 222, NULL);
INSERT INTO `role_privilege_custom` VALUES (601, 'default', '', '2017-09-13 16:02:53', 'dependence', NULL, NULL, 1, '依赖管理', 201, 1, 223, NULL);
INSERT INTO `role_privilege_custom` VALUES (602, 'default', 'list', '2017-09-13 16:02:53', 'dependence', NULL, NULL, 1, '依赖列表查询', 223, 0, 224, NULL);
INSERT INTO `role_privilege_custom` VALUES (603, 'default', 'create', '2017-09-13 16:02:53', 'dependence', NULL, NULL, 1, '依赖创建', 223, 0, 225, NULL);
INSERT INTO `role_privilege_custom` VALUES (604, 'default', 'delete', '2017-09-13 16:02:53', 'dependence', NULL, NULL, 1, '依赖删除', 223, 0, 226, NULL);
INSERT INTO `role_privilege_custom` VALUES (605, 'default', '', '2017-09-13 16:02:53', 'deliveryCenter', NULL, NULL, 1, '交付中心', 0, 1, 227, NULL);
INSERT INTO `role_privilege_custom` VALUES (606, 'default', '', '2017-09-13 08:26:12', 'mirrorRepertory', NULL, NULL, 1, '镜像管理', 227, 1, 228, NULL);
INSERT INTO `role_privilege_custom` VALUES (607, 'default', '', '2017-09-13 16:02:53', 'basic', NULL, NULL, 1, '镜像仓库基本信息', 228, 1, 229, NULL);
INSERT INTO `role_privilege_custom` VALUES (608, 'default', 'list', '2017-09-13 16:02:53', 'basic', NULL, NULL, 1, '镜像仓库镜像列表查询', 229, 0, 230, NULL);
INSERT INTO `role_privilege_custom` VALUES (609, 'default', 'create', '2017-09-13 16:02:53', 'basic', NULL, NULL, 1, '镜像仓库创建', 229, 0, 231, NULL);
INSERT INTO `role_privilege_custom` VALUES (610, 'default', 'delete', '2017-09-13 16:02:53', 'basic', NULL, NULL, 1, '镜像仓库删除', 229, 0, 232, NULL);
INSERT INTO `role_privilege_custom` VALUES (611, 'default', 'detail', '2017-09-13 16:02:53', 'basic', NULL, NULL, 1, '镜像仓库详情', 229, 0, 233, NULL);
INSERT INTO `role_privilege_custom` VALUES (612, 'default', '', '2017-09-13 16:02:53', 'mirror', NULL, NULL, 1, '镜像', 228, 1, 234, NULL);
INSERT INTO `role_privilege_custom` VALUES (613, 'default', '', '2017-09-13 16:02:53', 'history', NULL, NULL, 1, '镜像历史版本', 234, 1, 235, NULL);
INSERT INTO `role_privilege_custom` VALUES (614, 'default', 'delete', '2017-09-13 16:02:53', 'history', NULL, NULL, 1, '镜像历史版本删除', 235, 0, 236, NULL);
INSERT INTO `role_privilege_custom` VALUES (615, 'default', '', '2017-09-13 16:02:53', 'basic', NULL, NULL, 1, '镜像基本信息', 234, 1, 237, NULL);
INSERT INTO `role_privilege_custom` VALUES (616, 'default', 'delete', '2017-09-13 16:02:53', 'basic', NULL, NULL, 1, '镜像删除', 237, 0, 238, NULL);
INSERT INTO `role_privilege_custom` VALUES (617, 'default', 'push', '2017-09-13 16:02:53', 'basic', NULL, NULL, 1, '镜像push', 237, 0, 239, NULL);
INSERT INTO `role_privilege_custom` VALUES (618, 'default', '', '2017-09-13 16:02:53', 'synchronized', NULL, NULL, 1, '镜像仓库同步', 228, 1, 240, NULL);
INSERT INTO `role_privilege_custom` VALUES (619, 'default', 'create', '2017-09-13 16:02:53', 'synchronized', NULL, NULL, 1, '仓库同步规则创建', 240, 0, 241, NULL);
INSERT INTO `role_privilege_custom` VALUES (620, 'default', 'delete', '2017-09-13 16:02:53', 'synchronized', NULL, NULL, 1, '仓库同步规则删除', 240, 0, 242, NULL);
INSERT INTO `role_privilege_custom` VALUES (621, 'default', 'status', '2017-09-13 16:02:53', 'synchronized', NULL, NULL, 1, '仓库同步开启或暂停', 240, 0, 243, NULL);
INSERT INTO `role_privilege_custom` VALUES (622, 'default', 'update', '2017-09-13 16:02:53', 'synchronized', NULL, NULL, 1, '同步设置', 240, 0, 244, NULL);
INSERT INTO `role_privilege_custom` VALUES (623, 'default', 'update', '2017-09-13 16:02:53', 'basic', NULL, NULL, 1, '镜像仓库配额修改', 229, 0, 245, NULL);
INSERT INTO `role_privilege_custom` VALUES (624, 'default', '', '2017-09-13 16:02:53', 'shop', NULL, NULL, 1, '应用商店', 227, 1, 246, NULL);
INSERT INTO `role_privilege_custom` VALUES (625, 'default', 'delivery', '2017-09-13 16:02:53', 'shop', NULL, NULL, 1, '应用商店应用发布', 246, 0, 247, NULL);
INSERT INTO `role_privilege_custom` VALUES (626, 'default', '', '2017-09-13 08:26:12', 'template', NULL, NULL, 1, '模板管理', 227, 1, 248, NULL);
INSERT INTO `role_privilege_custom` VALUES (627, 'default', '', '2017-09-13 16:02:53', 'basic', NULL, NULL, 1, '模板基本信息', 248, 1, 249, NULL);
INSERT INTO `role_privilege_custom` VALUES (628, 'default', 'delete', '2017-09-13 16:02:53', 'basic', NULL, NULL, 1, '应用模板删除', 249, 0, 250, NULL);
INSERT INTO `role_privilege_custom` VALUES (629, 'default', 'delivery', '2017-09-13 16:02:53', 'basic', NULL, NULL, 1, '发布应用', 249, 0, 251, NULL);
INSERT INTO `role_privilege_custom` VALUES (630, 'default', 'detail', '2017-09-13 16:02:53', 'basic', NULL, NULL, 1, '模板详情', 249, 0, 252, NULL);
INSERT INTO `role_privilege_custom` VALUES (631, 'default', 'update', '2017-09-13 16:02:53', 'basic', NULL, NULL, 1, '模板更新', 249, 0, 253, NULL);
INSERT INTO `role_privilege_custom` VALUES (632, 'default', '', '2017-09-13 16:02:53', 'logs', NULL, NULL, 1, '日志管理', 0, 1, 354, NULL);
INSERT INTO `role_privilege_custom` VALUES (633, 'default', '', '2017-09-13 16:02:53', 'audit', NULL, NULL, 1, '操作审计', 354, 1, 355, NULL);
INSERT INTO `role_privilege_custom` VALUES (634, 'default', 'list', '2017-09-13 16:02:53', 'audit', NULL, NULL, 1, '查询操作审计', 355, 0, 356, NULL);
INSERT INTO `role_privilege_custom` VALUES (635, 'default', '', '2017-09-13 16:02:53', 'alarmCenter', NULL, NULL, 1, '告警中心', 0, 1, 400, NULL);
INSERT INTO `role_privilege_custom` VALUES (636, 'default', '', '2017-09-13 16:02:53', 'alarmRule', NULL, NULL, 1, '告警规则', 400, 1, 401, NULL);
INSERT INTO `role_privilege_custom` VALUES (637, 'default', 'create', '2017-09-13 16:02:53', 'alarmRule', NULL, NULL, 1, '创建告警规则', 401, 0, 402, NULL);
INSERT INTO `role_privilege_custom` VALUES (638, 'default', 'delete', '2017-09-13 16:02:53', 'alarmRule', NULL, NULL, 1, '删除告警规则', 401, 0, 403, NULL);
INSERT INTO `role_privilege_custom` VALUES (639, 'default', 'update', '2017-09-13 16:02:53', 'alarmRule', NULL, NULL, 1, '修改告警规则', 401, 0, 404, NULL);
INSERT INTO `role_privilege_custom` VALUES (640, 'default', 'pause', '2017-09-13 16:02:53', 'alarmRule', NULL, NULL, 1, '暂停/启动告警规则', 401, 0, 405, NULL);
INSERT INTO `role_privilege_custom` VALUES (641, 'default', 'list', '2017-09-13 16:02:53', 'alarmRule', NULL, NULL, 1, '查询告警规则', 401, 0, 406, NULL);
INSERT INTO `role_privilege_custom` VALUES (642, 'default', '', '2017-09-13 16:02:53', 'alarmProcessCenter', NULL, NULL, 1, '告警处理中心', 400, 1, 407, NULL);
INSERT INTO `role_privilege_custom` VALUES (643, 'default', 'list', '2017-09-13 16:02:53', 'alarmProcessCenter', NULL, NULL, 1, '报警处理查询', 407, 0, 408, NULL);
INSERT INTO `role_privilege_custom` VALUES (644, 'default', 'update', '2017-09-13 16:02:53', 'alarmProcessCenter', NULL, NULL, 1, '报警处理', 407, 0, 409, NULL);
INSERT INTO `role_privilege_custom` VALUES (645, 'default', '', '2017-09-13 16:02:53', 'systemConfig', NULL, NULL, 0, '系统设置', 0, 1, 410, NULL);
INSERT INTO `role_privilege_custom` VALUES (646, 'default', 'list', '2017-09-13 16:02:53', 'alarmProcessCenter', NULL, NULL, 0, '查询系统设置', 410, 0, 411, NULL);
INSERT INTO `role_privilege_custom` VALUES (647, 'default', 'update', '2017-09-13 16:02:53', 'alarmProcessCenter', NULL, NULL, 0, '更新系统设置', 410, 0, 412, NULL);
INSERT INTO `role_privilege_custom` VALUES (747, 'ops', '', '2017-09-13 16:02:53', 'tenant', NULL, NULL, 1, '租户管理', 0, 1, 1, NULL);
INSERT INTO `role_privilege_custom` VALUES (748, 'ops', '', '2017-09-13 16:02:53', 'tenant', NULL, NULL, 1, '租户', 1, 1, 2, NULL);
INSERT INTO `role_privilege_custom` VALUES (749, 'ops', 'list', '2017-09-13 16:02:53', 'tenant', NULL, NULL, 1, '租户查询', 2, 0, 3, NULL);
INSERT INTO `role_privilege_custom` VALUES (750, 'ops', 'create', '2017-09-13 16:02:53', 'tenant', NULL, NULL, 1, '租户创建', 2, 0, 4, NULL);
INSERT INTO `role_privilege_custom` VALUES (751, 'ops', 'delete', '2017-09-13 16:02:53', 'tenant', NULL, NULL, 1, '租户删除', 2, 0, 5, NULL);
INSERT INTO `role_privilege_custom` VALUES (752, 'ops', '', '2017-09-13 16:02:53', 'namespace', NULL, NULL, 1, '分区', 1, 1, 6, NULL);
INSERT INTO `role_privilege_custom` VALUES (753, 'ops', 'create', '2017-09-13 16:02:53', 'namespace', NULL, NULL, 1, '租户分区创建', 6, 0, 7, NULL);
INSERT INTO `role_privilege_custom` VALUES (754, 'ops', 'delete', '2017-09-13 16:02:53', 'namespace', NULL, NULL, 1, '租户分区删除', 6, 0, 8, NULL);
INSERT INTO `role_privilege_custom` VALUES (755, 'ops', 'update', '2017-09-13 16:02:53', 'namespace', NULL, NULL, 1, '租户分区修改', 6, 0, 9, NULL);
INSERT INTO `role_privilege_custom` VALUES (756, 'ops', 'list', '2017-09-13 16:02:53', 'namespace', NULL, NULL, 1, '租户分区查询', 6, 0, 10, NULL);
INSERT INTO `role_privilege_custom` VALUES (757, 'ops', '', '2017-09-13 16:02:53', 'mirror', NULL, NULL, 1, '镜像仓库', 1, 1, 11, NULL);
INSERT INTO `role_privilege_custom` VALUES (758, 'ops', 'list', '2017-09-13 16:02:53', 'mirror', NULL, NULL, 1, '租户镜像仓库查询', 11, 0, 12, NULL);
INSERT INTO `role_privilege_custom` VALUES (759, 'ops', 'create', '2017-09-13 16:02:53', 'mirror', NULL, NULL, 1, '租户镜像仓库创建', 11, 0, 13, NULL);
INSERT INTO `role_privilege_custom` VALUES (760, 'ops', 'update', '2017-09-13 16:02:53', 'mirror', NULL, NULL, 1, '租户镜像仓库修改', 11, 0, 14, NULL);
INSERT INTO `role_privilege_custom` VALUES (761, 'ops', 'delete', '2017-09-13 16:02:53', 'mirror', NULL, NULL, 1, '租户镜像仓库删除', 11, 0, 15, NULL);
INSERT INTO `role_privilege_custom` VALUES (762, 'ops', '', '2017-09-13 16:02:53', 'user', NULL, NULL, 1, '用户', 1, 1, 16, NULL);
INSERT INTO `role_privilege_custom` VALUES (763, 'ops', 'create', '2017-09-13 16:02:53', 'user', NULL, NULL, 1, '租户用户添加', 16, 0, 17, NULL);
INSERT INTO `role_privilege_custom` VALUES (764, 'ops', 'list', '2017-09-13 16:02:53', 'user', NULL, NULL, 1, '租户用户查询', 16, 0, 18, NULL);
INSERT INTO `role_privilege_custom` VALUES (765, 'ops', 'delete', '2017-09-13 16:02:53', 'user', NULL, NULL, 1, '租户用户移除', 16, 0, 19, NULL);
INSERT INTO `role_privilege_custom` VALUES (766, 'ops', 'update', '2017-09-13 16:02:54', 'user', NULL, NULL, 1, '租户用户修改', 16, 0, 20, NULL);
INSERT INTO `role_privilege_custom` VALUES (767, 'ops', 'detail', '2017-09-13 16:02:54', 'namespace', NULL, NULL, 1, '租户分区详情', 6, 0, 21, NULL);
INSERT INTO `role_privilege_custom` VALUES (768, 'ops', '', '2017-09-13 16:02:54', 'application', NULL, NULL, 1, '应用中心', 0, 1, 101, NULL);
INSERT INTO `role_privilege_custom` VALUES (769, 'ops', '', '2017-09-13 16:02:54', 'application', NULL, NULL, 1, '应用', 101, 1, 102, NULL);
INSERT INTO `role_privilege_custom` VALUES (770, 'ops', 'create', '2017-09-13 16:02:54', 'application', NULL, NULL, 1, '应用创建', 102, 0, 103, NULL);
INSERT INTO `role_privilege_custom` VALUES (771, 'ops', 'list', '2017-09-13 16:02:54', 'application', NULL, NULL, 1, '应用列表', 102, 0, 104, NULL);
INSERT INTO `role_privilege_custom` VALUES (772, 'ops', 'start', '2017-09-13 16:02:54', 'application', NULL, NULL, 1, '应用启动', 102, 0, 105, NULL);
INSERT INTO `role_privilege_custom` VALUES (773, 'ops', 'pause', '2017-09-13 16:02:54', 'application', NULL, NULL, 1, '应用暂停', 102, 0, 106, NULL);
INSERT INTO `role_privilege_custom` VALUES (774, 'ops', 'delete', '2017-09-13 16:02:54', 'application', NULL, NULL, 1, '应用删除', 102, 0, 107, NULL);
INSERT INTO `role_privilege_custom` VALUES (775, 'ops', 'detail', '2017-09-13 16:02:54', 'application', NULL, NULL, 1, '应用详情', 102, 0, 108, NULL);
INSERT INTO `role_privilege_custom` VALUES (776, 'ops', '', '2017-09-13 16:02:54', 'service', NULL, NULL, 1, '服务', 101, 1, 111, NULL);
INSERT INTO `role_privilege_custom` VALUES (777, 'ops', '', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '服务基本信息', 111, 1, 112, NULL);
INSERT INTO `role_privilege_custom` VALUES (778, 'ops', 'create', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '服务创建', 112, 0, 113, NULL);
INSERT INTO `role_privilege_custom` VALUES (779, 'ops', 'start', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '服务启动', 112, 0, 114, NULL);
INSERT INTO `role_privilege_custom` VALUES (780, 'ops', 'release', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '服务发布', 112, 0, 115, NULL);
INSERT INTO `role_privilege_custom` VALUES (781, 'ops', 'autoScaling', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '服务自动伸缩配置', 112, 0, 116, NULL);
INSERT INTO `role_privilege_custom` VALUES (782, 'ops', 'rollback', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '服务回滚', 112, 0, 117, NULL);
INSERT INTO `role_privilege_custom` VALUES (783, 'ops', 'delete', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '服务删除', 112, 0, 118, NULL);
INSERT INTO `role_privilege_custom` VALUES (784, 'ops', 'stop', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '服务停止', 112, 0, 119, NULL);
INSERT INTO `role_privilege_custom` VALUES (785, 'ops', 'console', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '服务控制台', 112, 0, 120, NULL);
INSERT INTO `role_privilege_custom` VALUES (786, 'ops', 'update', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '服务修改', 112, 0, 121, NULL);
INSERT INTO `role_privilege_custom` VALUES (787, 'ops', '', '2017-09-13 16:02:54', 'log', NULL, NULL, 1, '服务日志信息', 111, 1, 122, NULL);
INSERT INTO `role_privilege_custom` VALUES (788, 'ops', 'get', '2017-09-13 16:02:54', 'log', NULL, NULL, 1, '服务日志查询', 122, 0, 123, NULL);
INSERT INTO `role_privilege_custom` VALUES (789, 'ops', 'export', '2017-09-13 16:02:54', 'log', NULL, NULL, 1, '服务日志导出', 122, 0, 124, NULL);
INSERT INTO `role_privilege_custom` VALUES (790, 'ops', '', '2017-09-13 16:02:54', 'monitor', NULL, NULL, 1, '服务监控信息', 111, 1, 125, NULL);
INSERT INTO `role_privilege_custom` VALUES (791, 'ops', 'get', '2017-09-13 16:02:54', 'monitor', NULL, NULL, 1, '服务监控信息查询', 125, 0, 126, NULL);
INSERT INTO `role_privilege_custom` VALUES (792, 'ops', '', '2017-09-13 16:02:54', 'candyUpdate', NULL, NULL, 1, '灰度升级', 111, 1, 127, NULL);
INSERT INTO `role_privilege_custom` VALUES (793, 'ops', 'start', '2017-09-13 16:02:54', 'candyUpdate', NULL, NULL, 1, '灰度升级开启', 127, 0, 128, NULL);
INSERT INTO `role_privilege_custom` VALUES (794, 'ops', 'edit', '2017-09-13 16:02:54', 'candyUpdate', NULL, NULL, 1, '编辑灰度升级信息', 127, 0, 129, NULL);
INSERT INTO `role_privilege_custom` VALUES (795, 'ops', 'rollback', '2017-09-13 16:02:54', 'candyUpdate', NULL, NULL, 1, '回滚到未开始升级的版本', 127, 0, 130, NULL);
INSERT INTO `role_privilege_custom` VALUES (796, 'ops', 'update', '2017-09-13 16:02:54', 'candyUpdate', NULL, NULL, 1, '全部升级到新版本', 127, 0, 131, NULL);
INSERT INTO `role_privilege_custom` VALUES (797, 'ops', 'upload', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '上传文件到容器', 112, 0, 132, NULL);
INSERT INTO `role_privilege_custom` VALUES (798, 'ops', '', '2017-09-13 16:02:54', 'external', NULL, NULL, 1, '外部服务', 101, 1, 133, NULL);
INSERT INTO `role_privilege_custom` VALUES (799, 'ops', 'create', '2017-09-13 16:02:54', 'external', NULL, NULL, 1, '新建外部服务', 133, 0, 134, NULL);
INSERT INTO `role_privilege_custom` VALUES (800, 'ops', 'list', '2017-09-13 16:02:54', 'external', NULL, NULL, 1, '外部服务列表查询', 133, 0, 135, NULL);
INSERT INTO `role_privilege_custom` VALUES (801, 'ops', 'update', '2017-09-13 16:02:54', 'external', NULL, NULL, 1, '外部服务修改', 133, 0, 136, NULL);
INSERT INTO `role_privilege_custom` VALUES (802, 'ops', 'delete', '2017-09-13 16:02:54', 'external', NULL, NULL, 1, '外部服务删除', 133, 0, 137, NULL);
INSERT INTO `role_privilege_custom` VALUES (803, 'ops', '', '2017-09-13 16:02:54', 'configCenter', NULL, NULL, 1, '配置中心', 101, 1, 138, NULL);
INSERT INTO `role_privilege_custom` VALUES (804, 'ops', 'create', '2017-09-13 16:02:54', 'configCenter', NULL, NULL, 1, '创建配置文件', 138, 0, 139, NULL);
INSERT INTO `role_privilege_custom` VALUES (805, 'ops', 'list', '2017-09-13 16:02:54', 'configCenter', NULL, NULL, 1, '配置文件列表查询', 138, 0, 140, NULL);
INSERT INTO `role_privilege_custom` VALUES (806, 'ops', 'update', '2017-09-13 16:02:54', 'configCenter', NULL, NULL, 1, '配置文件修改', 138, 0, 141, NULL);
INSERT INTO `role_privilege_custom` VALUES (807, 'ops', 'delete', '2017-09-13 16:02:54', 'configCenter', NULL, NULL, 1, '配置文件删除', 138, 0, 143, NULL);
INSERT INTO `role_privilege_custom` VALUES (808, 'ops', 'get', '2017-09-13 16:02:54', 'configCenter', NULL, NULL, 1, '配置文件查看', 138, 0, 144, NULL);
INSERT INTO `role_privilege_custom` VALUES (809, 'ops', '', '2017-09-13 16:02:54', 'storage', NULL, NULL, 1, '存储', 101, 1, 145, NULL);
INSERT INTO `role_privilege_custom` VALUES (810, 'ops', 'list', '2017-09-13 16:02:54', 'storage', NULL, NULL, 1, '存储列表查询', 145, 0, 146, NULL);
INSERT INTO `role_privilege_custom` VALUES (811, 'ops', 'create', '2017-09-13 16:02:54', 'storage', NULL, NULL, 1, '存储创建', 145, 0, 147, NULL);
INSERT INTO `role_privilege_custom` VALUES (812, 'ops', 'update', '2017-09-13 16:02:54', 'storage', NULL, NULL, 1, '存储更新', 145, 0, 148, NULL);
INSERT INTO `role_privilege_custom` VALUES (813, 'ops', 'delete', '2017-09-13 16:02:54', 'storage', NULL, NULL, 1, '存储删除', 145, 0, 149, NULL);
INSERT INTO `role_privilege_custom` VALUES (814, 'ops', '', '2017-09-13 16:02:54', 'cicd', NULL, NULL, 1, 'CICD', 0, 1, 201, NULL);
INSERT INTO `role_privilege_custom` VALUES (815, 'ops', '', '2017-09-13 08:46:35', 'dockerFile', NULL, NULL, 1, 'Docker file', 201, 1, 202, NULL);
INSERT INTO `role_privilege_custom` VALUES (816, 'ops', 'list', '2017-09-13 16:02:54', 'dockerFile', NULL, NULL, 1, 'DockerFile列表查看', 202, 0, 203, NULL);
INSERT INTO `role_privilege_custom` VALUES (817, 'ops', 'get', '2017-09-13 16:02:54', 'dockerFile', NULL, NULL, 1, 'DockerFile查看', 202, 0, 204, NULL);
INSERT INTO `role_privilege_custom` VALUES (818, 'ops', 'create', '2017-09-13 16:02:54', 'dockerFile', NULL, NULL, 1, 'DockerFile创建', 202, 0, 205, NULL);
INSERT INTO `role_privilege_custom` VALUES (819, 'ops', 'update', '2017-09-13 16:02:54', 'dockerFile', NULL, NULL, 1, 'DockerFile修改', 202, 0, 206, NULL);
INSERT INTO `role_privilege_custom` VALUES (820, 'ops', 'delete', '2017-09-13 16:02:54', 'dockerFile', NULL, NULL, 1, 'DockerFile删除', 202, 0, 207, NULL);
INSERT INTO `role_privilege_custom` VALUES (821, 'ops', '', '2017-09-13 08:46:35', 'pipeline', NULL, NULL, 1, '流水线', 201, 1, 208, NULL);
INSERT INTO `role_privilege_custom` VALUES (822, 'ops', '', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '流水线基本信息管理', 208, 1, 209, NULL);
INSERT INTO `role_privilege_custom` VALUES (823, 'ops', 'list', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '流水线列表查询', 209, 0, 210, NULL);
INSERT INTO `role_privilege_custom` VALUES (824, 'ops', 'get', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '流水线查询', 209, 0, 211, NULL);
INSERT INTO `role_privilege_custom` VALUES (825, 'ops', 'create', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '流水线创建', 209, 0, 212, NULL);
INSERT INTO `role_privilege_custom` VALUES (826, 'ops', 'start', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '流水线任务执行', 209, 0, 213, NULL);
INSERT INTO `role_privilege_custom` VALUES (827, 'ops', 'planJob', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '持续集成', 209, 0, 214, NULL);
INSERT INTO `role_privilege_custom` VALUES (828, 'ops', '', '2017-09-13 16:02:54', 'job', NULL, NULL, 1, '流水线job', 208, 1, 215, NULL);
INSERT INTO `role_privilege_custom` VALUES (829, 'ops', 'list', '2017-09-13 16:02:54', 'job', NULL, NULL, 1, 'job列表查询', 215, 0, 216, NULL);
INSERT INTO `role_privilege_custom` VALUES (830, 'ops', 'get', '2017-09-13 16:02:54', 'job', NULL, NULL, 1, 'job查询', 215, 0, 217, NULL);
INSERT INTO `role_privilege_custom` VALUES (831, 'ops', 'create', '2017-09-13 16:02:54', 'job', NULL, NULL, 1, 'job创建', 215, 0, 218, NULL);
INSERT INTO `role_privilege_custom` VALUES (832, 'ops', 'update', '2017-09-13 16:02:54', 'job', NULL, NULL, 1, 'job更新', 215, 0, 219, NULL);
INSERT INTO `role_privilege_custom` VALUES (833, 'ops', 'delete', '2017-09-13 16:02:54', 'job', NULL, NULL, 1, 'job删除', 215, 0, 220, NULL);
INSERT INTO `role_privilege_custom` VALUES (834, 'ops', 'log', '2017-09-13 16:02:54', 'job', NULL, NULL, 1, 'job日志查询', 215, 0, 221, NULL);
INSERT INTO `role_privilege_custom` VALUES (835, 'ops', 'log', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '流水线日志查询', 209, 0, 222, NULL);
INSERT INTO `role_privilege_custom` VALUES (836, 'ops', '', '2017-09-13 16:02:54', 'dependence', NULL, NULL, 1, '依赖管理', 201, 1, 223, NULL);
INSERT INTO `role_privilege_custom` VALUES (837, 'ops', 'list', '2017-09-13 16:02:54', 'dependence', NULL, NULL, 1, '依赖列表查询', 223, 0, 224, NULL);
INSERT INTO `role_privilege_custom` VALUES (838, 'ops', 'create', '2017-09-13 16:02:54', 'dependence', NULL, NULL, 1, '依赖创建', 223, 0, 225, NULL);
INSERT INTO `role_privilege_custom` VALUES (839, 'ops', 'delete', '2017-09-13 16:02:54', 'dependence', NULL, NULL, 1, '依赖删除', 223, 0, 226, NULL);
INSERT INTO `role_privilege_custom` VALUES (840, 'ops', '', '2017-09-13 16:02:54', 'deliveryCenter', NULL, NULL, 1, '交付中心', 0, 1, 227, NULL);
INSERT INTO `role_privilege_custom` VALUES (841, 'ops', '', '2017-09-13 08:46:36', 'mirrorRepertory', NULL, NULL, 1, '镜像管理', 227, 1, 228, NULL);
INSERT INTO `role_privilege_custom` VALUES (842, 'ops', '', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '镜像仓库基本信息', 228, 1, 229, NULL);
INSERT INTO `role_privilege_custom` VALUES (843, 'ops', 'list', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '镜像仓库镜像列表查询', 229, 0, 230, NULL);
INSERT INTO `role_privilege_custom` VALUES (844, 'ops', 'create', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '镜像仓库创建', 229, 0, 231, NULL);
INSERT INTO `role_privilege_custom` VALUES (845, 'ops', 'delete', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '镜像仓库删除', 229, 0, 232, NULL);
INSERT INTO `role_privilege_custom` VALUES (846, 'ops', 'detail', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '镜像仓库详情', 229, 0, 233, NULL);
INSERT INTO `role_privilege_custom` VALUES (847, 'ops', '', '2017-09-13 16:02:54', 'mirror', NULL, NULL, 1, '镜像', 228, 1, 234, NULL);
INSERT INTO `role_privilege_custom` VALUES (848, 'ops', '', '2017-09-13 16:02:54', 'history', NULL, NULL, 1, '镜像历史版本', 234, 1, 235, NULL);
INSERT INTO `role_privilege_custom` VALUES (849, 'ops', 'delete', '2017-09-13 16:02:54', 'history', NULL, NULL, 1, '镜像历史版本删除', 235, 0, 236, NULL);
INSERT INTO `role_privilege_custom` VALUES (850, 'ops', '', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '镜像基本信息', 234, 1, 237, NULL);
INSERT INTO `role_privilege_custom` VALUES (851, 'ops', 'delete', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '镜像删除', 235, 0, 238, NULL);
INSERT INTO `role_privilege_custom` VALUES (852, 'ops', 'push', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '镜像push', 235, 0, 239, NULL);
INSERT INTO `role_privilege_custom` VALUES (853, 'ops', '', '2017-09-13 16:02:54', 'synchronized', NULL, NULL, 1, '镜像仓库同步', 228, 1, 240, NULL);
INSERT INTO `role_privilege_custom` VALUES (854, 'ops', 'create', '2017-09-13 16:02:54', 'synchronized', NULL, NULL, 1, '仓库同步规则创建', 240, 0, 241, NULL);
INSERT INTO `role_privilege_custom` VALUES (855, 'ops', 'delete', '2017-09-13 16:02:54', 'synchronized', NULL, NULL, 1, '仓库同步规则删除', 240, 0, 242, NULL);
INSERT INTO `role_privilege_custom` VALUES (856, 'ops', 'status', '2017-09-13 16:02:54', 'synchronized', NULL, NULL, 1, '仓库同步开启或暂停', 240, 0, 243, NULL);
INSERT INTO `role_privilege_custom` VALUES (857, 'ops', 'update', '2017-09-13 16:02:54', 'synchronized', NULL, NULL, 1, '同步设置', 240, 0, 244, NULL);
INSERT INTO `role_privilege_custom` VALUES (858, 'ops', 'update', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '镜像仓库配额修改', 229, 0, 245, NULL);
INSERT INTO `role_privilege_custom` VALUES (859, 'ops', '', '2017-09-13 16:02:54', 'shop', NULL, NULL, 1, '应用商店', 227, 1, 246, NULL);
INSERT INTO `role_privilege_custom` VALUES (860, 'ops', 'delivery', '2017-09-13 16:02:54', 'shop', NULL, NULL, 1, '应用商店应用发布', 246, 0, 247, NULL);
INSERT INTO `role_privilege_custom` VALUES (861, 'ops', '', '2017-09-13 08:46:36', 'template', NULL, NULL, 1, '模板管理', 227, 1, 248, NULL);
INSERT INTO `role_privilege_custom` VALUES (862, 'ops', '', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '模板基本信息', 248, 1, 249, NULL);
INSERT INTO `role_privilege_custom` VALUES (863, 'ops', 'delete', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '应用模板删除', 249, 0, 250, NULL);
INSERT INTO `role_privilege_custom` VALUES (864, 'ops', 'delivery', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '发布应用', 249, 0, 251, NULL);
INSERT INTO `role_privilege_custom` VALUES (865, 'ops', 'detail', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '模板详情', 249, 0, 252, NULL);
INSERT INTO `role_privilege_custom` VALUES (866, 'ops', 'update', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '模板更新', 249, 0, 253, NULL);
INSERT INTO `role_privilege_custom` VALUES (867, 'ops', '', '2017-09-13 16:02:54', 'logs', NULL, NULL, 1, '日志管理', 0, 1, 354, NULL);
INSERT INTO `role_privilege_custom` VALUES (868, 'ops', '', '2017-09-13 16:02:54', 'audit', NULL, NULL, 1, '操作审计', 354, 1, 355, NULL);
INSERT INTO `role_privilege_custom` VALUES (869, 'ops', 'list', '2017-09-13 16:02:54', 'audit', NULL, NULL, 1, '查询操作审计', 355, 0, 356, NULL);
INSERT INTO `role_privilege_custom` VALUES (870, 'ops', '', '2017-09-13 16:02:54', 'alarmCenter', NULL, NULL, 1, '告警中心', 0, 1, 400, NULL);
INSERT INTO `role_privilege_custom` VALUES (871, 'ops', '', '2017-09-13 16:02:54', 'alarmRule', NULL, NULL, 1, '告警规则', 400, 1, 401, NULL);
INSERT INTO `role_privilege_custom` VALUES (872, 'ops', 'create', '2017-09-13 16:02:54', 'alarmRule', NULL, NULL, 1, '创建告警规则', 401, 0, 402, NULL);
INSERT INTO `role_privilege_custom` VALUES (873, 'ops', 'delete', '2017-09-13 16:02:54', 'alarmRule', NULL, NULL, 1, '删除告警规则', 401, 0, 403, NULL);
INSERT INTO `role_privilege_custom` VALUES (874, 'ops', 'update', '2017-09-13 16:02:54', 'alarmRule', NULL, NULL, 1, '修改告警规则', 401, 0, 404, NULL);
INSERT INTO `role_privilege_custom` VALUES (875, 'ops', 'pause', '2017-09-13 16:02:54', 'alarmRule', NULL, NULL, 1, '暂停/启动告警规则', 401, 0, 405, NULL);
INSERT INTO `role_privilege_custom` VALUES (876, 'ops', 'list', '2017-09-13 16:02:54', 'alarmRule', NULL, NULL, 1, '查询告警规则', 401, 0, 406, NULL);
INSERT INTO `role_privilege_custom` VALUES (877, 'ops', '', '2017-09-13 16:02:54', 'alarmProcessCenter', NULL, NULL, 1, '告警处理中心', 400, 1, 407, NULL);
INSERT INTO `role_privilege_custom` VALUES (878, 'ops', 'list', '2017-09-13 16:02:54', 'alarmProcessCenter', NULL, NULL, 1, '报警处理查询', 407, 0, 408, NULL);
INSERT INTO `role_privilege_custom` VALUES (879, 'ops', 'update', '2017-09-13 16:02:54', 'alarmProcessCenter', NULL, NULL, 1, '报警处理', 407, 0, 409, NULL);
INSERT INTO `role_privilege_custom` VALUES (880, 'ops', '', '2017-09-13 16:02:54', 'systemConfig', NULL, NULL, 0, '系统设置', 0, 1, 410, NULL);
INSERT INTO `role_privilege_custom` VALUES (881, 'ops', 'list', '2017-09-13 16:02:54', 'alarmProcessCenter', NULL, NULL, 0, '查询系统设置', 410, 0, 411, NULL);
INSERT INTO `role_privilege_custom` VALUES (882, 'ops', 'update', '2017-09-13 16:02:54', 'alarmProcessCenter', NULL, NULL, 0, '更新系统设置', 410, 0, 412, NULL);
INSERT INTO `role_privilege_custom` VALUES (982, 'tester', '', '2017-09-13 16:02:54', 'tenant', NULL, NULL, 1, '租户管理', 0, 1, 1, NULL);
INSERT INTO `role_privilege_custom` VALUES (983, 'tester', '', '2017-09-13 16:02:54', 'tenant', NULL, NULL, 1, '租户', 1, 1, 2, NULL);
INSERT INTO `role_privilege_custom` VALUES (984, 'tester', 'list', '2017-09-13 16:02:54', 'tenant', NULL, NULL, 1, '租户查询', 2, 0, 3, NULL);
INSERT INTO `role_privilege_custom` VALUES (985, 'tester', 'create', '2017-09-13 16:02:54', 'tenant', NULL, NULL, 1, '租户创建', 2, 0, 4, NULL);
INSERT INTO `role_privilege_custom` VALUES (986, 'tester', 'delete', '2017-09-13 16:02:54', 'tenant', NULL, NULL, 1, '租户删除', 2, 0, 5, NULL);
INSERT INTO `role_privilege_custom` VALUES (987, 'tester', '', '2017-09-13 16:02:54', 'namespace', NULL, NULL, 1, '分区', 1, 1, 6, NULL);
INSERT INTO `role_privilege_custom` VALUES (988, 'tester', 'create', '2017-09-13 16:02:54', 'namespace', NULL, NULL, 1, '租户分区创建', 6, 0, 7, NULL);
INSERT INTO `role_privilege_custom` VALUES (989, 'tester', 'delete', '2017-09-13 16:02:54', 'namespace', NULL, NULL, 1, '租户分区删除', 6, 0, 8, NULL);
INSERT INTO `role_privilege_custom` VALUES (990, 'tester', 'update', '2017-09-13 16:02:54', 'namespace', NULL, NULL, 1, '租户分区修改', 6, 0, 9, NULL);
INSERT INTO `role_privilege_custom` VALUES (991, 'tester', 'list', '2017-09-13 16:02:54', 'namespace', NULL, NULL, 1, '租户分区查询', 6, 0, 10, NULL);
INSERT INTO `role_privilege_custom` VALUES (992, 'tester', '', '2017-09-13 16:02:54', 'mirror', NULL, NULL, 1, '镜像仓库', 1, 1, 11, NULL);
INSERT INTO `role_privilege_custom` VALUES (993, 'tester', 'list', '2017-09-13 16:02:54', 'mirror', NULL, NULL, 1, '租户镜像仓库查询', 11, 0, 12, NULL);
INSERT INTO `role_privilege_custom` VALUES (994, 'tester', 'create', '2017-09-13 16:02:54', 'mirror', NULL, NULL, 1, '租户镜像仓库创建', 11, 0, 13, NULL);
INSERT INTO `role_privilege_custom` VALUES (995, 'tester', 'update', '2017-09-13 16:02:54', 'mirror', NULL, NULL, 1, '租户镜像仓库修改', 11, 0, 14, NULL);
INSERT INTO `role_privilege_custom` VALUES (996, 'tester', 'delete', '2017-09-13 16:02:54', 'mirror', NULL, NULL, 1, '租户镜像仓库删除', 11, 0, 15, NULL);
INSERT INTO `role_privilege_custom` VALUES (997, 'tester', '', '2017-09-13 16:02:54', 'user', NULL, NULL, 1, '用户', 1, 1, 16, NULL);
INSERT INTO `role_privilege_custom` VALUES (998, 'tester', 'create', '2017-09-13 16:02:54', 'user', NULL, NULL, 1, '租户用户添加', 16, 0, 17, NULL);
INSERT INTO `role_privilege_custom` VALUES (999, 'tester', 'list', '2017-09-13 16:02:54', 'user', NULL, NULL, 1, '租户用户查询', 16, 0, 18, NULL);
INSERT INTO `role_privilege_custom` VALUES (1000, 'tester', 'delete', '2017-09-13 16:02:54', 'user', NULL, NULL, 1, '租户用户移除', 16, 0, 19, NULL);
INSERT INTO `role_privilege_custom` VALUES (1001, 'tester', 'update', '2017-09-13 16:02:54', 'user', NULL, NULL, 1, '租户用户修改', 16, 0, 20, NULL);
INSERT INTO `role_privilege_custom` VALUES (1002, 'tester', 'detail', '2017-09-13 16:02:54', 'namespace', NULL, NULL, 1, '租户分区详情', 6, 0, 21, NULL);
INSERT INTO `role_privilege_custom` VALUES (1003, 'tester', '', '2017-09-13 16:02:54', 'application', NULL, NULL, 1, '应用中心', 0, 1, 101, NULL);
INSERT INTO `role_privilege_custom` VALUES (1004, 'tester', '', '2017-09-13 16:02:54', 'application', NULL, NULL, 1, '应用', 101, 1, 102, NULL);
INSERT INTO `role_privilege_custom` VALUES (1005, 'tester', 'create', '2017-09-13 16:02:54', 'application', NULL, NULL, 1, '应用创建', 102, 0, 103, NULL);
INSERT INTO `role_privilege_custom` VALUES (1006, 'tester', 'list', '2017-09-13 16:02:54', 'application', NULL, NULL, 1, '应用列表', 102, 0, 104, NULL);
INSERT INTO `role_privilege_custom` VALUES (1007, 'tester', 'start', '2017-09-13 16:02:54', 'application', NULL, NULL, 1, '应用启动', 102, 0, 105, NULL);
INSERT INTO `role_privilege_custom` VALUES (1008, 'tester', 'pause', '2017-09-13 16:02:54', 'application', NULL, NULL, 1, '应用暂停', 102, 0, 106, NULL);
INSERT INTO `role_privilege_custom` VALUES (1009, 'tester', 'delete', '2017-09-13 16:02:54', 'application', NULL, NULL, 1, '应用删除', 102, 0, 107, NULL);
INSERT INTO `role_privilege_custom` VALUES (1010, 'tester', 'detail', '2017-09-13 16:02:54', 'application', NULL, NULL, 1, '应用详情', 102, 0, 108, NULL);
INSERT INTO `role_privilege_custom` VALUES (1011, 'tester', '', '2017-09-13 16:02:54', 'service', NULL, NULL, 1, '服务', 101, 1, 111, NULL);
INSERT INTO `role_privilege_custom` VALUES (1012, 'tester', '', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '服务基本信息', 111, 1, 112, NULL);
INSERT INTO `role_privilege_custom` VALUES (1013, 'tester', 'create', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '服务创建', 112, 0, 113, NULL);
INSERT INTO `role_privilege_custom` VALUES (1014, 'tester', 'start', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '服务启动', 112, 0, 114, NULL);
INSERT INTO `role_privilege_custom` VALUES (1015, 'tester', 'release', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '服务发布', 112, 0, 115, NULL);
INSERT INTO `role_privilege_custom` VALUES (1016, 'tester', 'autoScaling', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '服务自动伸缩配置', 112, 0, 116, NULL);
INSERT INTO `role_privilege_custom` VALUES (1017, 'tester', 'rollback', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '服务回滚', 112, 0, 117, NULL);
INSERT INTO `role_privilege_custom` VALUES (1018, 'tester', 'delete', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '服务删除', 112, 0, 118, NULL);
INSERT INTO `role_privilege_custom` VALUES (1019, 'tester', 'stop', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '服务停止', 112, 0, 119, NULL);
INSERT INTO `role_privilege_custom` VALUES (1020, 'tester', 'console', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '服务控制台', 112, 0, 120, NULL);
INSERT INTO `role_privilege_custom` VALUES (1021, 'tester', 'update', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '服务修改', 112, 0, 121, NULL);
INSERT INTO `role_privilege_custom` VALUES (1022, 'tester', '', '2017-09-13 16:02:54', 'log', NULL, NULL, 1, '服务日志信息', 111, 1, 122, NULL);
INSERT INTO `role_privilege_custom` VALUES (1023, 'tester', 'get', '2017-09-13 16:02:54', 'log', NULL, NULL, 1, '服务日志查询', 122, 0, 123, NULL);
INSERT INTO `role_privilege_custom` VALUES (1024, 'tester', 'export', '2017-09-13 16:02:54', 'log', NULL, NULL, 1, '服务日志导出', 122, 0, 124, NULL);
INSERT INTO `role_privilege_custom` VALUES (1025, 'tester', '', '2017-09-13 16:02:54', 'monitor', NULL, NULL, 1, '服务监控信息', 111, 1, 125, NULL);
INSERT INTO `role_privilege_custom` VALUES (1026, 'tester', 'get', '2017-09-13 16:02:54', 'monitor', NULL, NULL, 1, '服务监控信息查询', 125, 0, 126, NULL);
INSERT INTO `role_privilege_custom` VALUES (1027, 'tester', '', '2017-09-13 16:02:54', 'candyUpdate', NULL, NULL, 1, '灰度升级', 111, 1, 127, NULL);
INSERT INTO `role_privilege_custom` VALUES (1028, 'tester', 'start', '2017-09-13 16:02:54', 'candyUpdate', NULL, NULL, 1, '灰度升级开启', 127, 0, 128, NULL);
INSERT INTO `role_privilege_custom` VALUES (1029, 'tester', 'edit', '2017-09-13 16:02:54', 'candyUpdate', NULL, NULL, 1, '编辑灰度升级信息', 127, 0, 129, NULL);
INSERT INTO `role_privilege_custom` VALUES (1030, 'tester', 'rollback', '2017-09-13 16:02:54', 'candyUpdate', NULL, NULL, 1, '回滚到未开始升级的版本', 127, 0, 130, NULL);
INSERT INTO `role_privilege_custom` VALUES (1031, 'tester', 'update', '2017-09-13 16:02:54', 'candyUpdate', NULL, NULL, 1, '全部升级到新版本', 127, 0, 131, NULL);
INSERT INTO `role_privilege_custom` VALUES (1032, 'tester', 'upload', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '上传文件到容器', 112, 0, 132, NULL);
INSERT INTO `role_privilege_custom` VALUES (1033, 'tester', '', '2017-09-13 16:02:54', 'external', NULL, NULL, 1, '外部服务', 101, 1, 133, NULL);
INSERT INTO `role_privilege_custom` VALUES (1034, 'tester', 'create', '2017-09-13 16:02:54', 'external', NULL, NULL, 1, '新建外部服务', 133, 0, 134, NULL);
INSERT INTO `role_privilege_custom` VALUES (1035, 'tester', 'list', '2017-09-13 16:02:54', 'external', NULL, NULL, 1, '外部服务列表查询', 133, 0, 135, NULL);
INSERT INTO `role_privilege_custom` VALUES (1036, 'tester', 'update', '2017-09-13 16:02:54', 'external', NULL, NULL, 1, '外部服务修改', 133, 0, 136, NULL);
INSERT INTO `role_privilege_custom` VALUES (1037, 'tester', 'delete', '2017-09-13 16:02:54', 'external', NULL, NULL, 1, '外部服务删除', 133, 0, 137, NULL);
INSERT INTO `role_privilege_custom` VALUES (1038, 'tester', '', '2017-09-13 16:02:54', 'configCenter', NULL, NULL, 1, '配置中心', 101, 1, 138, NULL);
INSERT INTO `role_privilege_custom` VALUES (1039, 'tester', 'create', '2017-09-13 16:02:54', 'configCenter', NULL, NULL, 1, '创建配置文件', 138, 0, 139, NULL);
INSERT INTO `role_privilege_custom` VALUES (1040, 'tester', 'list', '2017-09-13 16:02:54', 'configCenter', NULL, NULL, 1, '配置文件列表查询', 138, 0, 140, NULL);
INSERT INTO `role_privilege_custom` VALUES (1041, 'tester', 'update', '2017-09-13 16:02:54', 'configCenter', NULL, NULL, 1, '配置文件修改', 138, 0, 141, NULL);
INSERT INTO `role_privilege_custom` VALUES (1042, 'tester', 'delete', '2017-09-13 16:02:54', 'configCenter', NULL, NULL, 1, '配置文件删除', 138, 0, 143, NULL);
INSERT INTO `role_privilege_custom` VALUES (1043, 'tester', 'get', '2017-09-13 16:02:54', 'configCenter', NULL, NULL, 1, '配置文件查看', 138, 0, 144, NULL);
INSERT INTO `role_privilege_custom` VALUES (1044, 'tester', '', '2017-09-13 16:02:54', 'storage', NULL, NULL, 1, '存储', 101, 1, 145, NULL);
INSERT INTO `role_privilege_custom` VALUES (1045, 'tester', 'list', '2017-09-13 16:02:54', 'storage', NULL, NULL, 1, '存储列表查询', 145, 0, 146, NULL);
INSERT INTO `role_privilege_custom` VALUES (1046, 'tester', 'create', '2017-09-13 16:02:54', 'storage', NULL, NULL, 1, '存储创建', 145, 0, 147, NULL);
INSERT INTO `role_privilege_custom` VALUES (1047, 'tester', 'update', '2017-09-13 16:02:54', 'storage', NULL, NULL, 1, '存储更新', 145, 0, 148, NULL);
INSERT INTO `role_privilege_custom` VALUES (1048, 'tester', 'delete', '2017-09-13 16:02:54', 'storage', NULL, NULL, 1, '存储删除', 145, 0, 149, NULL);
INSERT INTO `role_privilege_custom` VALUES (1049, 'tester', '', '2017-09-13 16:02:54', 'cicd', NULL, NULL, 1, 'CICD', 0, 1, 201, NULL);
INSERT INTO `role_privilege_custom` VALUES (1050, 'tester', '', '2017-09-13 08:46:50', 'dockerFile', NULL, NULL, 1, 'Docker file', 201, 1, 202, NULL);
INSERT INTO `role_privilege_custom` VALUES (1051, 'tester', 'list', '2017-09-13 16:02:54', 'dockerFile', NULL, NULL, 1, 'DockerFile列表查看', 202, 0, 203, NULL);
INSERT INTO `role_privilege_custom` VALUES (1052, 'tester', 'get', '2017-09-13 16:02:54', 'dockerFile', NULL, NULL, 1, 'DockerFile查看', 202, 0, 204, NULL);
INSERT INTO `role_privilege_custom` VALUES (1053, 'tester', 'create', '2017-09-13 16:02:54', 'dockerFile', NULL, NULL, 1, 'DockerFile创建', 202, 0, 205, NULL);
INSERT INTO `role_privilege_custom` VALUES (1054, 'tester', 'update', '2017-09-13 16:02:54', 'dockerFile', NULL, NULL, 1, 'DockerFile修改', 202, 0, 206, NULL);
INSERT INTO `role_privilege_custom` VALUES (1055, 'tester', 'delete', '2017-09-13 16:02:54', 'dockerFile', NULL, NULL, 1, 'DockerFile删除', 202, 0, 207, NULL);
INSERT INTO `role_privilege_custom` VALUES (1056, 'tester', '', '2017-09-13 08:46:50', 'pipeline', NULL, NULL, 1, '流水线', 201, 1, 208, NULL);
INSERT INTO `role_privilege_custom` VALUES (1057, 'tester', '', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '流水线基本信息管理', 208, 1, 209, NULL);
INSERT INTO `role_privilege_custom` VALUES (1058, 'tester', 'list', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '流水线列表查询', 209, 0, 210, NULL);
INSERT INTO `role_privilege_custom` VALUES (1059, 'tester', 'get', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '流水线查询', 209, 0, 211, NULL);
INSERT INTO `role_privilege_custom` VALUES (1060, 'tester', 'create', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '流水线创建', 209, 0, 212, NULL);
INSERT INTO `role_privilege_custom` VALUES (1061, 'tester', 'start', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '流水线任务执行', 209, 0, 213, NULL);
INSERT INTO `role_privilege_custom` VALUES (1062, 'tester', 'planJob', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '持续集成', 209, 0, 214, NULL);
INSERT INTO `role_privilege_custom` VALUES (1063, 'tester', '', '2017-09-13 16:02:54', 'job', NULL, NULL, 1, '流水线job', 208, 1, 215, NULL);
INSERT INTO `role_privilege_custom` VALUES (1064, 'tester', 'list', '2017-09-13 16:02:54', 'job', NULL, NULL, 1, 'job列表查询', 215, 0, 216, NULL);
INSERT INTO `role_privilege_custom` VALUES (1065, 'tester', 'get', '2017-09-13 16:02:54', 'job', NULL, NULL, 1, 'job查询', 215, 0, 217, NULL);
INSERT INTO `role_privilege_custom` VALUES (1066, 'tester', 'create', '2017-09-13 16:02:54', 'job', NULL, NULL, 1, 'job创建', 215, 0, 218, NULL);
INSERT INTO `role_privilege_custom` VALUES (1067, 'tester', 'update', '2017-09-13 16:02:54', 'job', NULL, NULL, 1, 'job更新', 215, 0, 219, NULL);
INSERT INTO `role_privilege_custom` VALUES (1068, 'tester', 'delete', '2017-09-13 16:02:54', 'job', NULL, NULL, 1, 'job删除', 215, 0, 220, NULL);
INSERT INTO `role_privilege_custom` VALUES (1069, 'tester', 'log', '2017-09-13 16:02:54', 'job', NULL, NULL, 1, 'job日志查询', 215, 0, 221, NULL);
INSERT INTO `role_privilege_custom` VALUES (1070, 'tester', 'log', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '流水线日志查询', 209, 0, 222, NULL);
INSERT INTO `role_privilege_custom` VALUES (1071, 'tester', '', '2017-09-13 16:02:54', 'dependence', NULL, NULL, 1, '依赖管理', 201, 1, 223, NULL);
INSERT INTO `role_privilege_custom` VALUES (1072, 'tester', 'list', '2017-09-13 16:02:54', 'dependence', NULL, NULL, 1, '依赖列表查询', 223, 0, 224, NULL);
INSERT INTO `role_privilege_custom` VALUES (1073, 'tester', 'create', '2017-09-13 16:02:54', 'dependence', NULL, NULL, 1, '依赖创建', 223, 0, 225, NULL);
INSERT INTO `role_privilege_custom` VALUES (1074, 'tester', 'delete', '2017-09-13 16:02:54', 'dependence', NULL, NULL, 1, '依赖删除', 223, 0, 226, NULL);
INSERT INTO `role_privilege_custom` VALUES (1075, 'tester', '', '2017-09-13 16:02:54', 'deliveryCenter', NULL, NULL, 1, '交付中心', 0, 1, 227, NULL);
INSERT INTO `role_privilege_custom` VALUES (1076, 'tester', '', '2017-09-13 08:46:51', 'mirrorRepertory', NULL, NULL, 1, '镜像管理', 227, 1, 228, NULL);
INSERT INTO `role_privilege_custom` VALUES (1077, 'tester', '', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '镜像仓库基本信息', 228, 1, 229, NULL);
INSERT INTO `role_privilege_custom` VALUES (1078, 'tester', 'list', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '镜像仓库镜像列表查询', 229, 0, 230, NULL);
INSERT INTO `role_privilege_custom` VALUES (1079, 'tester', 'create', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '镜像仓库创建', 229, 0, 231, NULL);
INSERT INTO `role_privilege_custom` VALUES (1080, 'tester', 'delete', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '镜像仓库删除', 229, 0, 232, NULL);
INSERT INTO `role_privilege_custom` VALUES (1081, 'tester', 'detail', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '镜像仓库详情', 229, 0, 233, NULL);
INSERT INTO `role_privilege_custom` VALUES (1082, 'tester', '', '2017-09-13 16:02:54', 'mirror', NULL, NULL, 1, '镜像', 228, 1, 234, NULL);
INSERT INTO `role_privilege_custom` VALUES (1083, 'tester', '', '2017-09-13 16:02:54', 'history', NULL, NULL, 1, '镜像历史版本', 234, 1, 235, NULL);
INSERT INTO `role_privilege_custom` VALUES (1084, 'tester', 'delete', '2017-09-13 16:02:54', 'history', NULL, NULL, 1, '镜像历史版本删除', 235, 0, 236, NULL);
INSERT INTO `role_privilege_custom` VALUES (1085, 'tester', '', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '镜像基本信息', 234, 1, 237, NULL);
INSERT INTO `role_privilege_custom` VALUES (1086, 'tester', 'delete', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '镜像删除', 235, 0, 238, NULL);
INSERT INTO `role_privilege_custom` VALUES (1087, 'tester', 'push', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '镜像push', 235, 0, 239, NULL);
INSERT INTO `role_privilege_custom` VALUES (1088, 'tester', '', '2017-09-13 16:02:54', 'synchronized', NULL, NULL, 1, '镜像仓库同步', 228, 1, 240, NULL);
INSERT INTO `role_privilege_custom` VALUES (1089, 'tester', 'create', '2017-09-13 16:02:54', 'synchronized', NULL, NULL, 1, '仓库同步规则创建', 240, 0, 241, NULL);
INSERT INTO `role_privilege_custom` VALUES (1090, 'tester', 'delete', '2017-09-13 16:02:54', 'synchronized', NULL, NULL, 1, '仓库同步规则删除', 240, 0, 242, NULL);
INSERT INTO `role_privilege_custom` VALUES (1091, 'tester', 'status', '2017-09-13 16:02:54', 'synchronized', NULL, NULL, 1, '仓库同步开启或暂停', 240, 0, 243, NULL);
INSERT INTO `role_privilege_custom` VALUES (1092, 'tester', 'update', '2017-09-13 16:02:54', 'synchronized', NULL, NULL, 1, '同步设置', 240, 0, 244, NULL);
INSERT INTO `role_privilege_custom` VALUES (1093, 'tester', 'update', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '镜像仓库配额修改', 229, 0, 245, NULL);
INSERT INTO `role_privilege_custom` VALUES (1094, 'tester', '', '2017-09-13 16:02:54', 'shop', NULL, NULL, 1, '应用商店', 227, 1, 246, NULL);
INSERT INTO `role_privilege_custom` VALUES (1095, 'tester', 'delivery', '2017-09-13 16:02:54', 'shop', NULL, NULL, 1, '应用商店应用发布', 246, 0, 247, NULL);
INSERT INTO `role_privilege_custom` VALUES (1096, 'tester', '', '2017-09-13 08:46:51', 'template', NULL, NULL, 1, '模板管理', 227, 1, 248, NULL);
INSERT INTO `role_privilege_custom` VALUES (1097, 'tester', '', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '模板基本信息', 248, 1, 249, NULL);
INSERT INTO `role_privilege_custom` VALUES (1098, 'tester', 'delete', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '应用模板删除', 249, 0, 250, NULL);
INSERT INTO `role_privilege_custom` VALUES (1099, 'tester', 'delivery', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '发布应用', 249, 0, 251, NULL);
INSERT INTO `role_privilege_custom` VALUES (1100, 'tester', 'detail', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '模板详情', 249, 0, 252, NULL);
INSERT INTO `role_privilege_custom` VALUES (1101, 'tester', 'update', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '模板更新', 249, 0, 253, NULL);
INSERT INTO `role_privilege_custom` VALUES (1102, 'tester', '', '2017-09-13 16:02:54', 'logs', NULL, NULL, 1, '日志管理', 0, 1, 354, NULL);
INSERT INTO `role_privilege_custom` VALUES (1103, 'tester', '', '2017-09-13 16:02:54', 'audit', NULL, NULL, 1, '操作审计', 354, 1, 355, NULL);
INSERT INTO `role_privilege_custom` VALUES (1104, 'tester', 'list', '2017-09-13 16:02:54', 'audit', NULL, NULL, 1, '查询操作审计', 355, 0, 356, NULL);
INSERT INTO `role_privilege_custom` VALUES (1105, 'tester', '', '2017-09-13 16:02:54', 'alarmCenter', NULL, NULL, 1, '告警中心', 0, 1, 400, NULL);
INSERT INTO `role_privilege_custom` VALUES (1106, 'tester', '', '2017-09-13 16:02:54', 'alarmRule', NULL, NULL, 1, '告警规则', 400, 1, 401, NULL);
INSERT INTO `role_privilege_custom` VALUES (1107, 'tester', 'create', '2017-09-13 16:02:54', 'alarmRule', NULL, NULL, 1, '创建告警规则', 401, 0, 402, NULL);
INSERT INTO `role_privilege_custom` VALUES (1108, 'tester', 'delete', '2017-09-13 16:02:54', 'alarmRule', NULL, NULL, 1, '删除告警规则', 401, 0, 403, NULL);
INSERT INTO `role_privilege_custom` VALUES (1109, 'tester', 'update', '2017-09-13 16:02:54', 'alarmRule', NULL, NULL, 1, '修改告警规则', 401, 0, 404, NULL);
INSERT INTO `role_privilege_custom` VALUES (1110, 'tester', 'pause', '2017-09-13 16:02:54', 'alarmRule', NULL, NULL, 1, '暂停/启动告警规则', 401, 0, 405, NULL);
INSERT INTO `role_privilege_custom` VALUES (1111, 'tester', 'list', '2017-09-13 16:02:54', 'alarmRule', NULL, NULL, 1, '查询告警规则', 401, 0, 406, NULL);
INSERT INTO `role_privilege_custom` VALUES (1112, 'tester', '', '2017-09-13 16:02:54', 'alarmProcessCenter', NULL, NULL, 1, '告警处理中心', 400, 1, 407, NULL);
INSERT INTO `role_privilege_custom` VALUES (1113, 'tester', 'list', '2017-09-13 16:02:54', 'alarmProcessCenter', NULL, NULL, 1, '报警处理查询', 407, 0, 408, NULL);
INSERT INTO `role_privilege_custom` VALUES (1114, 'tester', 'update', '2017-09-13 16:02:54', 'alarmProcessCenter', NULL, NULL, 1, '报警处理', 407, 0, 409, NULL);
INSERT INTO `role_privilege_custom` VALUES (1115, 'tester', '', '2017-09-13 16:02:54', 'systemConfig', NULL, NULL, 0, '系统设置', 0, 1, 410, NULL);
INSERT INTO `role_privilege_custom` VALUES (1116, 'tester', 'list', '2017-09-13 16:02:54', 'alarmProcessCenter', NULL, NULL, 0, '查询系统设置', 410, 0, 411, NULL);
INSERT INTO `role_privilege_custom` VALUES (1117, 'tester', 'update', '2017-09-13 16:02:54', 'alarmProcessCenter', NULL, NULL, 0, '更新系统设置', 410, 0, 412, NULL);
INSERT INTO `role_privilege_custom` VALUES (1217, 'tm', '', '2017-09-13 16:02:54', 'tenant', NULL, NULL, 1, '租户管理', 0, 1, 1, NULL);
INSERT INTO `role_privilege_custom` VALUES (1218, 'tm', '', '2017-09-13 16:02:54', 'tenant', NULL, NULL, 1, '租户', 1, 1, 2, NULL);
INSERT INTO `role_privilege_custom` VALUES (1219, 'tm', 'list', '2017-09-13 16:02:54', 'tenant', NULL, NULL, 1, '租户查询', 2, 0, 3, NULL);
INSERT INTO `role_privilege_custom` VALUES (1220, 'tm', 'create', '2017-09-13 16:02:54', 'tenant', NULL, NULL, 1, '租户创建', 2, 0, 4, NULL);
INSERT INTO `role_privilege_custom` VALUES (1221, 'tm', 'delete', '2017-09-13 16:02:54', 'tenant', NULL, NULL, 1, '租户删除', 2, 0, 5, NULL);
INSERT INTO `role_privilege_custom` VALUES (1222, 'tm', '', '2017-09-13 16:02:54', 'namespace', NULL, NULL, 1, '分区', 1, 1, 6, NULL);
INSERT INTO `role_privilege_custom` VALUES (1223, 'tm', 'create', '2017-09-13 16:02:54', 'namespace', NULL, NULL, 1, '租户分区创建', 6, 0, 7, NULL);
INSERT INTO `role_privilege_custom` VALUES (1224, 'tm', 'delete', '2017-09-13 16:02:54', 'namespace', NULL, NULL, 1, '租户分区删除', 6, 0, 8, NULL);
INSERT INTO `role_privilege_custom` VALUES (1225, 'tm', 'update', '2017-09-13 16:02:54', 'namespace', NULL, NULL, 1, '租户分区修改', 6, 0, 9, NULL);
INSERT INTO `role_privilege_custom` VALUES (1226, 'tm', 'list', '2017-09-13 16:02:54', 'namespace', NULL, NULL, 1, '租户分区查询', 6, 0, 10, NULL);
INSERT INTO `role_privilege_custom` VALUES (1227, 'tm', '', '2017-09-13 16:02:54', 'mirror', NULL, NULL, 1, '镜像仓库', 1, 1, 11, NULL);
INSERT INTO `role_privilege_custom` VALUES (1228, 'tm', 'list', '2017-09-13 16:02:54', 'mirror', NULL, NULL, 1, '租户镜像仓库查询', 11, 0, 12, NULL);
INSERT INTO `role_privilege_custom` VALUES (1229, 'tm', 'create', '2017-09-13 16:02:54', 'mirror', NULL, NULL, 1, '租户镜像仓库创建', 11, 0, 13, NULL);
INSERT INTO `role_privilege_custom` VALUES (1230, 'tm', 'update', '2017-09-13 16:02:54', 'mirror', NULL, NULL, 1, '租户镜像仓库修改', 11, 0, 14, NULL);
INSERT INTO `role_privilege_custom` VALUES (1231, 'tm', 'delete', '2017-09-13 16:02:54', 'mirror', NULL, NULL, 1, '租户镜像仓库删除', 11, 0, 15, NULL);
INSERT INTO `role_privilege_custom` VALUES (1232, 'tm', '', '2017-09-13 16:02:54', 'user', NULL, NULL, 1, '用户', 1, 1, 16, NULL);
INSERT INTO `role_privilege_custom` VALUES (1233, 'tm', 'create', '2017-09-13 16:02:54', 'user', NULL, NULL, 1, '租户用户添加', 16, 0, 17, NULL);
INSERT INTO `role_privilege_custom` VALUES (1234, 'tm', 'list', '2017-09-13 16:02:54', 'user', NULL, NULL, 1, '租户用户查询', 16, 0, 18, NULL);
INSERT INTO `role_privilege_custom` VALUES (1235, 'tm', 'delete', '2017-09-13 16:02:54', 'user', NULL, NULL, 1, '租户用户移除', 16, 0, 19, NULL);
INSERT INTO `role_privilege_custom` VALUES (1236, 'tm', 'update', '2017-09-13 16:02:54', 'user', NULL, NULL, 1, '租户用户修改', 16, 0, 20, NULL);
INSERT INTO `role_privilege_custom` VALUES (1237, 'tm', 'detail', '2017-09-13 16:02:54', 'namespace', NULL, NULL, 1, '租户分区详情', 6, 0, 21, NULL);
INSERT INTO `role_privilege_custom` VALUES (1238, 'tm', '', '2017-09-13 16:02:54', 'application', NULL, NULL, 1, '应用中心', 0, 1, 101, NULL);
INSERT INTO `role_privilege_custom` VALUES (1239, 'tm', '', '2017-09-13 16:02:54', 'application', NULL, NULL, 1, '应用', 101, 1, 102, NULL);
INSERT INTO `role_privilege_custom` VALUES (1240, 'tm', 'create', '2017-09-13 16:02:54', 'application', NULL, NULL, 1, '应用创建', 102, 0, 103, NULL);
INSERT INTO `role_privilege_custom` VALUES (1241, 'tm', 'list', '2017-09-13 16:02:54', 'application', NULL, NULL, 1, '应用列表', 102, 0, 104, NULL);
INSERT INTO `role_privilege_custom` VALUES (1242, 'tm', 'start', '2017-09-13 16:02:54', 'application', NULL, NULL, 1, '应用启动', 102, 0, 105, NULL);
INSERT INTO `role_privilege_custom` VALUES (1243, 'tm', 'pause', '2017-09-13 16:02:54', 'application', NULL, NULL, 1, '应用暂停', 102, 0, 106, NULL);
INSERT INTO `role_privilege_custom` VALUES (1244, 'tm', 'delete', '2017-09-13 16:02:54', 'application', NULL, NULL, 1, '应用删除', 102, 0, 107, NULL);
INSERT INTO `role_privilege_custom` VALUES (1245, 'tm', 'detail', '2017-09-13 16:02:54', 'application', NULL, NULL, 1, '应用详情', 102, 0, 108, NULL);
INSERT INTO `role_privilege_custom` VALUES (1246, 'tm', '', '2017-09-13 16:02:54', 'service', NULL, NULL, 1, '服务', 101, 1, 111, NULL);
INSERT INTO `role_privilege_custom` VALUES (1247, 'tm', '', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '服务基本信息', 111, 1, 112, NULL);
INSERT INTO `role_privilege_custom` VALUES (1248, 'tm', 'create', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '服务创建', 112, 0, 113, NULL);
INSERT INTO `role_privilege_custom` VALUES (1249, 'tm', 'start', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '服务启动', 112, 0, 114, NULL);
INSERT INTO `role_privilege_custom` VALUES (1250, 'tm', 'release', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '服务发布', 112, 0, 115, NULL);
INSERT INTO `role_privilege_custom` VALUES (1251, 'tm', 'autoScaling', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '服务自动伸缩配置', 112, 0, 116, NULL);
INSERT INTO `role_privilege_custom` VALUES (1252, 'tm', 'rollback', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '服务回滚', 112, 0, 117, NULL);
INSERT INTO `role_privilege_custom` VALUES (1253, 'tm', 'delete', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '服务删除', 112, 0, 118, NULL);
INSERT INTO `role_privilege_custom` VALUES (1254, 'tm', 'stop', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '服务停止', 112, 0, 119, NULL);
INSERT INTO `role_privilege_custom` VALUES (1255, 'tm', 'console', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '服务控制台', 112, 0, 120, NULL);
INSERT INTO `role_privilege_custom` VALUES (1256, 'tm', 'update', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '服务修改', 112, 0, 121, NULL);
INSERT INTO `role_privilege_custom` VALUES (1257, 'tm', '', '2017-09-13 16:02:54', 'log', NULL, NULL, 1, '服务日志信息', 111, 1, 122, NULL);
INSERT INTO `role_privilege_custom` VALUES (1258, 'tm', 'get', '2017-09-13 16:02:54', 'log', NULL, NULL, 1, '服务日志查询', 122, 0, 123, NULL);
INSERT INTO `role_privilege_custom` VALUES (1259, 'tm', 'export', '2017-09-13 16:02:54', 'log', NULL, NULL, 1, '服务日志导出', 122, 0, 124, NULL);
INSERT INTO `role_privilege_custom` VALUES (1260, 'tm', '', '2017-09-13 16:02:54', 'monitor', NULL, NULL, 1, '服务监控信息', 111, 1, 125, NULL);
INSERT INTO `role_privilege_custom` VALUES (1261, 'tm', 'get', '2017-09-13 16:02:54', 'monitor', NULL, NULL, 1, '服务监控信息查询', 125, 0, 126, NULL);
INSERT INTO `role_privilege_custom` VALUES (1262, 'tm', '', '2017-09-13 16:02:54', 'candyUpdate', NULL, NULL, 1, '灰度升级', 111, 1, 127, NULL);
INSERT INTO `role_privilege_custom` VALUES (1263, 'tm', 'start', '2017-09-13 16:02:54', 'candyUpdate', NULL, NULL, 1, '灰度升级开启', 127, 0, 128, NULL);
INSERT INTO `role_privilege_custom` VALUES (1264, 'tm', 'edit', '2017-09-13 16:02:54', 'candyUpdate', NULL, NULL, 1, '编辑灰度升级信息', 127, 0, 129, NULL);
INSERT INTO `role_privilege_custom` VALUES (1265, 'tm', 'rollback', '2017-09-13 16:02:54', 'candyUpdate', NULL, NULL, 1, '回滚到未开始升级的版本', 127, 0, 130, NULL);
INSERT INTO `role_privilege_custom` VALUES (1266, 'tm', 'update', '2017-09-13 16:02:54', 'candyUpdate', NULL, NULL, 1, '全部升级到新版本', 127, 0, 131, NULL);
INSERT INTO `role_privilege_custom` VALUES (1267, 'tm', 'upload', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '上传文件到容器', 112, 0, 132, NULL);
INSERT INTO `role_privilege_custom` VALUES (1268, 'tm', '', '2017-09-13 16:02:54', 'external', NULL, NULL, 1, '外部服务', 101, 1, 133, NULL);
INSERT INTO `role_privilege_custom` VALUES (1269, 'tm', 'create', '2017-09-13 16:02:54', 'external', NULL, NULL, 1, '新建外部服务', 133, 0, 134, NULL);
INSERT INTO `role_privilege_custom` VALUES (1270, 'tm', 'list', '2017-09-13 16:02:54', 'external', NULL, NULL, 1, '外部服务列表查询', 133, 0, 135, NULL);
INSERT INTO `role_privilege_custom` VALUES (1271, 'tm', 'update', '2017-09-13 16:02:54', 'external', NULL, NULL, 1, '外部服务修改', 133, 0, 136, NULL);
INSERT INTO `role_privilege_custom` VALUES (1272, 'tm', 'delete', '2017-09-13 16:02:54', 'external', NULL, NULL, 1, '外部服务删除', 133, 0, 137, NULL);
INSERT INTO `role_privilege_custom` VALUES (1273, 'tm', '', '2017-09-13 16:02:54', 'configCenter', NULL, NULL, 1, '配置中心', 101, 1, 138, NULL);
INSERT INTO `role_privilege_custom` VALUES (1274, 'tm', 'create', '2017-09-13 16:02:54', 'configCenter', NULL, NULL, 1, '创建配置文件', 138, 0, 139, NULL);
INSERT INTO `role_privilege_custom` VALUES (1275, 'tm', 'list', '2017-09-13 16:02:54', 'configCenter', NULL, NULL, 1, '配置文件列表查询', 138, 0, 140, NULL);
INSERT INTO `role_privilege_custom` VALUES (1276, 'tm', 'update', '2017-09-13 16:02:54', 'configCenter', NULL, NULL, 1, '配置文件修改', 138, 0, 141, NULL);
INSERT INTO `role_privilege_custom` VALUES (1277, 'tm', 'delete', '2017-09-13 16:02:54', 'configCenter', NULL, NULL, 1, '配置文件删除', 138, 0, 143, NULL);
INSERT INTO `role_privilege_custom` VALUES (1278, 'tm', 'get', '2017-09-13 16:02:54', 'configCenter', NULL, NULL, 1, '配置文件查看', 138, 0, 144, NULL);
INSERT INTO `role_privilege_custom` VALUES (1279, 'tm', '', '2017-09-13 16:02:54', 'storage', NULL, NULL, 1, '存储', 101, 1, 145, NULL);
INSERT INTO `role_privilege_custom` VALUES (1280, 'tm', 'list', '2017-09-13 16:02:54', 'storage', NULL, NULL, 1, '存储列表查询', 145, 0, 146, NULL);
INSERT INTO `role_privilege_custom` VALUES (1281, 'tm', 'create', '2017-09-13 16:02:54', 'storage', NULL, NULL, 1, '存储创建', 145, 0, 147, NULL);
INSERT INTO `role_privilege_custom` VALUES (1282, 'tm', 'update', '2017-09-13 16:02:54', 'storage', NULL, NULL, 1, '存储更新', 145, 0, 148, NULL);
INSERT INTO `role_privilege_custom` VALUES (1283, 'tm', 'delete', '2017-09-13 16:02:54', 'storage', NULL, NULL, 1, '存储删除', 145, 0, 149, NULL);
INSERT INTO `role_privilege_custom` VALUES (1284, 'tm', '', '2017-09-13 16:02:54', 'cicd', NULL, NULL, 1, 'CICD', 0, 1, 201, NULL);
INSERT INTO `role_privilege_custom` VALUES (1285, 'tm', '', '2017-09-13 08:46:15', 'dockerFile', NULL, NULL, 1, 'Docker file', 201, 1, 202, NULL);
INSERT INTO `role_privilege_custom` VALUES (1286, 'tm', 'list', '2017-09-13 16:02:54', 'dockerFile', NULL, NULL, 1, 'DockerFile列表查看', 202, 0, 203, NULL);
INSERT INTO `role_privilege_custom` VALUES (1287, 'tm', 'get', '2017-09-13 16:02:54', 'dockerFile', NULL, NULL, 1, 'DockerFile查看', 202, 0, 204, NULL);
INSERT INTO `role_privilege_custom` VALUES (1288, 'tm', 'create', '2017-09-13 16:02:54', 'dockerFile', NULL, NULL, 1, 'DockerFile创建', 202, 0, 205, NULL);
INSERT INTO `role_privilege_custom` VALUES (1289, 'tm', 'update', '2017-09-13 16:02:54', 'dockerFile', NULL, NULL, 1, 'DockerFile修改', 202, 0, 206, NULL);
INSERT INTO `role_privilege_custom` VALUES (1290, 'tm', 'delete', '2017-09-13 16:02:54', 'dockerFile', NULL, NULL, 1, 'DockerFile删除', 202, 0, 207, NULL);
INSERT INTO `role_privilege_custom` VALUES (1291, 'tm', '', '2017-09-13 08:46:15', 'pipeline', NULL, NULL, 1, '流水线', 201, 1, 208, NULL);
INSERT INTO `role_privilege_custom` VALUES (1292, 'tm', '', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '流水线基本信息管理', 208, 1, 209, NULL);
INSERT INTO `role_privilege_custom` VALUES (1293, 'tm', 'list', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '流水线列表查询', 209, 0, 210, NULL);
INSERT INTO `role_privilege_custom` VALUES (1294, 'tm', 'get', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '流水线查询', 209, 0, 211, NULL);
INSERT INTO `role_privilege_custom` VALUES (1295, 'tm', 'create', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '流水线创建', 209, 0, 212, NULL);
INSERT INTO `role_privilege_custom` VALUES (1296, 'tm', 'start', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '流水线任务执行', 209, 0, 213, NULL);
INSERT INTO `role_privilege_custom` VALUES (1297, 'tm', 'planJob', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '持续集成', 209, 0, 214, NULL);
INSERT INTO `role_privilege_custom` VALUES (1298, 'tm', '', '2017-09-13 16:02:54', 'job', NULL, NULL, 1, '流水线job', 208, 1, 215, NULL);
INSERT INTO `role_privilege_custom` VALUES (1299, 'tm', 'list', '2017-09-13 16:02:54', 'job', NULL, NULL, 1, 'job列表查询', 215, 0, 216, NULL);
INSERT INTO `role_privilege_custom` VALUES (1300, 'tm', 'get', '2017-09-13 16:02:54', 'job', NULL, NULL, 1, 'job查询', 215, 0, 217, NULL);
INSERT INTO `role_privilege_custom` VALUES (1301, 'tm', 'create', '2017-09-13 16:02:54', 'job', NULL, NULL, 1, 'job创建', 215, 0, 218, NULL);
INSERT INTO `role_privilege_custom` VALUES (1302, 'tm', 'update', '2017-09-13 16:02:54', 'job', NULL, NULL, 1, 'job更新', 215, 0, 219, NULL);
INSERT INTO `role_privilege_custom` VALUES (1303, 'tm', 'delete', '2017-09-13 16:02:54', 'job', NULL, NULL, 1, 'job删除', 215, 0, 220, NULL);
INSERT INTO `role_privilege_custom` VALUES (1304, 'tm', 'log', '2017-09-13 16:02:54', 'job', NULL, NULL, 1, 'job日志查询', 215, 0, 221, NULL);
INSERT INTO `role_privilege_custom` VALUES (1305, 'tm', 'log', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '流水线日志查询', 209, 0, 222, NULL);
INSERT INTO `role_privilege_custom` VALUES (1306, 'tm', '', '2017-09-13 16:02:54', 'dependence', NULL, NULL, 1, '依赖管理', 201, 1, 223, NULL);
INSERT INTO `role_privilege_custom` VALUES (1307, 'tm', 'list', '2017-09-13 16:02:54', 'dependence', NULL, NULL, 1, '依赖列表查询', 223, 0, 224, NULL);
INSERT INTO `role_privilege_custom` VALUES (1308, 'tm', 'create', '2017-09-13 16:02:54', 'dependence', NULL, NULL, 1, '依赖创建', 223, 0, 225, NULL);
INSERT INTO `role_privilege_custom` VALUES (1309, 'tm', 'delete', '2017-09-13 16:02:54', 'dependence', NULL, NULL, 1, '依赖删除', 223, 0, 226, NULL);
INSERT INTO `role_privilege_custom` VALUES (1310, 'tm', '', '2017-09-13 16:02:54', 'deliveryCenter', NULL, NULL, 1, '交付中心', 0, 1, 227, NULL);
INSERT INTO `role_privilege_custom` VALUES (1311, 'tm', '', '2017-09-13 08:46:15', 'mirrorRepertory', NULL, NULL, 1, '镜像管理', 227, 1, 228, NULL);
INSERT INTO `role_privilege_custom` VALUES (1312, 'tm', '', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '镜像仓库基本信息', 228, 1, 229, NULL);
INSERT INTO `role_privilege_custom` VALUES (1313, 'tm', 'list', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '镜像仓库镜像列表查询', 229, 0, 230, NULL);
INSERT INTO `role_privilege_custom` VALUES (1314, 'tm', 'create', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '镜像仓库创建', 229, 0, 231, NULL);
INSERT INTO `role_privilege_custom` VALUES (1315, 'tm', 'delete', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '镜像仓库删除', 229, 0, 232, NULL);
INSERT INTO `role_privilege_custom` VALUES (1316, 'tm', 'detail', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '镜像仓库详情', 229, 0, 233, NULL);
INSERT INTO `role_privilege_custom` VALUES (1317, 'tm', '', '2017-09-13 16:02:54', 'mirror', NULL, NULL, 1, '镜像', 228, 1, 234, NULL);
INSERT INTO `role_privilege_custom` VALUES (1318, 'tm', '', '2017-09-13 16:02:54', 'history', NULL, NULL, 1, '镜像历史版本', 234, 1, 235, NULL);
INSERT INTO `role_privilege_custom` VALUES (1319, 'tm', 'delete', '2017-09-13 16:02:54', 'history', NULL, NULL, 1, '镜像历史版本删除', 235, 0, 236, NULL);
INSERT INTO `role_privilege_custom` VALUES (1320, 'tm', '', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '镜像基本信息', 234, 1, 237, NULL);
INSERT INTO `role_privilege_custom` VALUES (1321, 'tm', 'delete', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '镜像删除', 235, 0, 238, NULL);
INSERT INTO `role_privilege_custom` VALUES (1322, 'tm', 'push', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '镜像push', 235, 0, 239, NULL);
INSERT INTO `role_privilege_custom` VALUES (1323, 'tm', '', '2017-09-13 16:02:54', 'synchronized', NULL, NULL, 1, '镜像仓库同步', 228, 1, 240, NULL);
INSERT INTO `role_privilege_custom` VALUES (1324, 'tm', 'create', '2017-09-13 16:02:54', 'synchronized', NULL, NULL, 1, '仓库同步规则创建', 240, 0, 241, NULL);
INSERT INTO `role_privilege_custom` VALUES (1325, 'tm', 'delete', '2017-09-13 16:02:54', 'synchronized', NULL, NULL, 1, '仓库同步规则删除', 240, 0, 242, NULL);
INSERT INTO `role_privilege_custom` VALUES (1326, 'tm', 'status', '2017-09-13 16:02:54', 'synchronized', NULL, NULL, 1, '仓库同步开启或暂停', 240, 0, 243, NULL);
INSERT INTO `role_privilege_custom` VALUES (1327, 'tm', 'update', '2017-09-13 16:02:54', 'synchronized', NULL, NULL, 1, '同步设置', 240, 0, 244, NULL);
INSERT INTO `role_privilege_custom` VALUES (1328, 'tm', 'update', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '镜像仓库配额修改', 229, 0, 245, NULL);
INSERT INTO `role_privilege_custom` VALUES (1329, 'tm', '', '2017-09-13 16:02:54', 'shop', NULL, NULL, 1, '应用商店', 227, 1, 246, NULL);
INSERT INTO `role_privilege_custom` VALUES (1330, 'tm', 'delivery', '2017-09-13 16:02:54', 'shop', NULL, NULL, 1, '应用商店应用发布', 246, 0, 247, NULL);
INSERT INTO `role_privilege_custom` VALUES (1331, 'tm', '', '2017-09-13 08:46:16', 'template', NULL, NULL, 1, '模板管理', 227, 1, 248, NULL);
INSERT INTO `role_privilege_custom` VALUES (1332, 'tm', '', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '模板基本信息', 248, 1, 249, NULL);
INSERT INTO `role_privilege_custom` VALUES (1333, 'tm', 'delete', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '应用模板删除', 249, 0, 250, NULL);
INSERT INTO `role_privilege_custom` VALUES (1334, 'tm', 'delivery', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '发布应用', 249, 0, 251, NULL);
INSERT INTO `role_privilege_custom` VALUES (1335, 'tm', 'detail', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '模板详情', 249, 0, 252, NULL);
INSERT INTO `role_privilege_custom` VALUES (1336, 'tm', 'update', '2017-09-13 16:02:54', 'basic', NULL, NULL, 1, '模板更新', 249, 0, 253, NULL);
INSERT INTO `role_privilege_custom` VALUES (1337, 'tm', '', '2017-09-13 16:02:54', 'logs', NULL, NULL, 1, '日志管理', 0, 1, 354, NULL);
INSERT INTO `role_privilege_custom` VALUES (1338, 'tm', '', '2017-09-13 16:02:54', 'audit', NULL, NULL, 1, '操作审计', 354, 1, 355, NULL);
INSERT INTO `role_privilege_custom` VALUES (1339, 'tm', 'list', '2017-09-13 16:02:54', 'audit', NULL, NULL, 1, '查询操作审计', 355, 0, 356, NULL);
INSERT INTO `role_privilege_custom` VALUES (1340, 'tm', '', '2017-09-13 16:02:54', 'alarmCenter', NULL, NULL, 1, '告警中心', 0, 1, 400, NULL);
INSERT INTO `role_privilege_custom` VALUES (1341, 'tm', '', '2017-09-13 16:02:54', 'alarmRule', NULL, NULL, 1, '告警规则', 400, 1, 401, NULL);
INSERT INTO `role_privilege_custom` VALUES (1342, 'tm', 'create', '2017-09-13 16:02:54', 'alarmRule', NULL, NULL, 1, '创建告警规则', 401, 0, 402, NULL);
INSERT INTO `role_privilege_custom` VALUES (1343, 'tm', 'delete', '2017-09-13 16:02:54', 'alarmRule', NULL, NULL, 1, '删除告警规则', 401, 0, 403, NULL);
INSERT INTO `role_privilege_custom` VALUES (1344, 'tm', 'update', '2017-09-13 16:02:54', 'alarmRule', NULL, NULL, 1, '修改告警规则', 401, 0, 404, NULL);
INSERT INTO `role_privilege_custom` VALUES (1345, 'tm', 'pause', '2017-09-13 16:02:54', 'alarmRule', NULL, NULL, 1, '暂停/启动告警规则', 401, 0, 405, NULL);
INSERT INTO `role_privilege_custom` VALUES (1346, 'tm', 'list', '2017-09-13 16:02:54', 'alarmRule', NULL, NULL, 1, '查询告警规则', 401, 0, 406, NULL);
INSERT INTO `role_privilege_custom` VALUES (1347, 'tm', '', '2017-09-13 16:02:54', 'alarmProcessCenter', NULL, NULL, 1, '告警处理中心', 400, 1, 407, NULL);
INSERT INTO `role_privilege_custom` VALUES (1348, 'tm', 'list', '2017-09-13 16:02:54', 'alarmProcessCenter', NULL, NULL, 1, '报警处理查询', 407, 0, 408, NULL);
INSERT INTO `role_privilege_custom` VALUES (1349, 'tm', 'update', '2017-09-13 16:02:54', 'alarmProcessCenter', NULL, NULL, 1, '报警处理', 407, 0, 409, NULL);
INSERT INTO `role_privilege_custom` VALUES (1350, 'tm', '', '2017-09-13 16:02:54', 'systemConfig', NULL, NULL, 0, '系统设置', 0, 1, 410, NULL);
INSERT INTO `role_privilege_custom` VALUES (1351, 'tm', 'list', '2017-09-13 16:02:54', 'alarmProcessCenter', NULL, NULL, 0, '查询系统设置', 410, 0, 411, NULL);
INSERT INTO `role_privilege_custom` VALUES (1352, 'tm', 'update', '2017-09-13 16:02:54', 'alarmProcessCenter', NULL, NULL, 0, '更新系统设置', 410, 0, 412, NULL);

SET FOREIGN_KEY_CHECKS = 1;
--
-- Table structure for table `service`
--

DROP TABLE IF EXISTS `service`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `service` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) CHARACTER SET latin1 DEFAULT NULL,
  `business_id` int(11) DEFAULT NULL,
  `service_template_id` int(11) DEFAULT NULL,
  `is_external` int(11) DEFAULT '0',
  `pvc` varchar(512) CHARACTER SET latin1 DEFAULT NULL,
  `ingress` varchar(512) CHARACTER SET latin1 DEFAULT NULL,
  `namespace` varchar(512) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `service`
--

LOCK TABLES `service` WRITE;
/*!40000 ALTER TABLE `service` DISABLE KEYS */;
/*!40000 ALTER TABLE `service` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `service_templates`
--

DROP TABLE IF EXISTS `service_templates`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `service_templates` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) CHARACTER SET latin1 DEFAULT NULL,
  `tag` varchar(45) CHARACTER SET latin1 DEFAULT NULL,
  `details` varchar(256) DEFAULT NULL,
  `deployment_content` longtext,
  `image_list` varchar(512) CHARACTER SET latin1 DEFAULT NULL,
  `ingress_content` text CHARACTER SET latin1,
  `status` int(1) DEFAULT NULL,
  `tenant` varchar(45) CHARACTER SET latin1 DEFAULT NULL,
  `create_user` varchar(45) CHARACTER SET latin1 DEFAULT NULL,
  `create_time` timestamp NULL DEFAULT NULL,
  `flag` int(1) DEFAULT '0',
  `node_selector` varchar(225) CHARACTER SET latin1 DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `service_templates`
--

LOCK TABLES `service_templates` WRITE;
/*!40000 ALTER TABLE `service_templates` DISABLE KEYS */;
INSERT INTO `service_templates` (`id`,`name`,`tag`,`details`,`deployment_content`,`image_list`,`ingress_content`,`status`,`tenant`,`create_user`,`create_time`,`flag`,`node_selector`) VALUES (1,'tomcat','1.0',NULL,'[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[],\"command\":[],\"configmap\":[],\"env\":[],\"img\":\"onlineshop/tomcat\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"tomcat\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"8080\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"500m\",\"memory\":\"512\"},\"storage\":[],\"tag\":\"v8.0\"}],\"hostName\":\"\",\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"tomcat\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]','onlineshop/tomcat',NULL,1,'gywtesttenant','admin','2017-08-11 14:18:36',0,'HarmonyCloud_Status=C');
INSERT INTO `service_templates` (`id`,`name`,`tag`,`details`,`deployment_content`,`image_list`,`ingress_content`,`status`,`tenant`,`create_user`,`create_time`,`flag`,`node_selector`) VALUES (2,'redis','1.0',NULL,'[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[],\"command\":[],\"configmap\":[],\"env\":[],\"img\":\"onlineshop/redis\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"redis\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"6379\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"100m\",\"memory\":\"128\"},\"storage\":[],\"tag\":\"3.2-alpine\"}],\"hostName\":\"\",\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"redis\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]','onlineshop/redis',NULL,1,'gywtesttenant','admin','2017-08-11 14:44:14',0,'HarmonyCloud_Status=C');
INSERT INTO `service_templates` (`id`,`name`,`tag`,`details`,`deployment_content`,`image_list`,`ingress_content`,`status`,`tenant`,`create_user`,`create_time`,`flag`,`node_selector`) VALUES (3,'wordpress','1.0',NULL,'[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[],\"command\":[],\"configmap\":[],\"env\":[],\"img\":\"onlineshop/wordpress\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"wordpress\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"80\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"500m\",\"memory\":\"512\"},\"storage\":[],\"tag\":\"4.8.0-php7.1-fpm-alpine\"}],\"hostName\":\"\",\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"wordpress\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]','onlineshop/wordpress',NULL,1,'gywtesttenant','admin','2017-08-11 15:31:54',0,'HarmonyCloud_Status=C');
INSERT INTO `service_templates` (`id`,`name`,`tag`,`details`,`deployment_content`,`image_list`,`ingress_content`,`status`,`tenant`,`create_user`,`create_time`,`flag`,`node_selector`) VALUES (4,'influxdb','1.0',NULL,'[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[],\"command\":[],\"configmap\":[],\"env\":[],\"img\":\"onlineshop/influxdb\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"influxdb2\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"8086\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"500m\",\"memory\":\"512\"},\"storage\":[],\"tag\":\"v1.3.0\"}],\"hostName\":\"\",\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"influxdb2\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]','onlineshop/influxdb',NULL,1,'gywtesttenant','admin','2017-08-11 17:30:21',0,'HarmonyCloud_Status=C');
INSERT INTO `service_templates` (`id`,`name`,`tag`,`details`,`deployment_content`,`image_list`,`ingress_content`,`status`,`tenant`,`create_user`,`create_time`,`flag`,`node_selector`) VALUES (5,'mysql','1.0',NULL,'[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[],\"command\":[],\"configmap\":[],\"env\":[],\"img\":\"onlineshop/mysqls\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"mysql\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"1433\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"1000m\",\"memory\":\"1024\"},\"storage\":[],\"tag\":\"v1\"}],\"hostName\":\"\",\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"mysql\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]','onlineshop/mysqls',NULL,1,'gywtesttenant','admin','2017-08-11 18:03:10',0,'HarmonyCloud_Status=C');
INSERT INTO `service_templates` (`id`,`name`,`tag`,`details`,`deployment_content`,`image_list`,`ingress_content`,`status`,`tenant`,`create_user`,`create_time`,`flag`,`node_selector`) VALUES (6,'webhook','1.0',NULL,'[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[],\"command\":[],\"configmap\":[],\"env\":[],\"img\":\"onlineshop/webhook\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"webhook\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"1433\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"1000m\",\"memory\":\"1024\"},\"storage\":[],\"tag\":\"2.6.5\"}],\"hostName\":\"\",\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"webhook\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]','onlineshop/webhook',NULL,1,'gywtesttenant','admin','2017-08-12 15:54:52',0,'HarmonyCloud_Status=C');
INSERT INTO `service_templates` (`id`,`name`,`tag`,`details`,`deployment_content`,`image_list`,`ingress_content`,`status`,`tenant`,`create_user`,`create_time`,`flag`,`node_selector`) VALUES (7,'mongodb','1.0',NULL,'[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[],\"command\":[],\"configmap\":[],\"env\":[],\"imagePullPolicy\":\"Always\",\"img\":\"onlineshop/mongodb\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"mongodb\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"1234\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"100m\",\"memory\":\"128\"},\"securityContext\":{\"add\":[],\"drop\":[],\"privileged\":false,\"security\":false},\"storage\":[],\"tag\":\"v3.5\"}],\"hostIPC\":false,\"hostName\":\"\",\"hostPID\":false,\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"mongodb\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]','onlineshop/mongodb',NULL,1,'garydemo','admin','2017-09-07 19:10:27',0,'HarmonyCloud_Status=C');
INSERT INTO `service_templates` (`id`,`name`,`tag`,`details`,`deployment_content`,`image_list`,`ingress_content`,`status`,`tenant`,`create_user`,`create_time`,`flag`,`node_selector`) VALUES (8,'rabbitmq','1.0',NULL,'[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[],\"command\":[],\"configmap\":[],\"env\":[],\"imagePullPolicy\":\"Always\",\"img\":\"onlineshop/rabbitmq\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"rabbitmq\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"1231\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"500m\",\"memory\":\"512\"},\"securityContext\":{\"add\":[],\"drop\":[],\"privileged\":false,\"security\":false},\"storage\":[],\"tag\":\"3.6.11\"}],\"hostIPC\":false,\"hostName\":\"\",\"hostPID\":false,\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"rabbitmq\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]','onlineshop/rabbitmq',NULL,1,'garydemo','admin','2017-09-09 10:44:14',0,'HarmonyCloud_Status=C');
INSERT INTO `service_templates` (`id`,`name`,`tag`,`details`,`deployment_content`,`image_list`,`ingress_content`,`status`,`tenant`,`create_user`,`create_time`,`flag`,`node_selector`) VALUES (9,'nginx','1.0',NULL,'[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[],\"command\":[],\"configmap\":[],\"env\":[],\"imagePullPolicy\":\"Always\",\"img\":\"onlineshop/nginx\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"nginxcon\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"80\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"100m\",\"memory\":\"128\"},\"securityContext\":{\"add\":[],\"drop\":[],\"privileged\":false,\"security\":false},\"storage\":[],\"tag\":\"latest\"}],\"hostIPC\":false,\"hostName\":\"\",\"hostPID\":false,\"instance\":\"2\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"nginxsvc\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]','onlineshop/nginx',NULL,1,'garydemo','admin','2017-09-06 15:26:07',0,'HarmonyCloud_Status=C');
INSERT INTO `service_templates` (`id`,`name`,`tag`,`details`,`deployment_content`,`image_list`,`ingress_content`,`status`,`tenant`,`create_user`,`create_time`,`flag`,`node_selector`) VALUES (10,'websphere','1.0',NULL,'[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[],\"command\":[],\"configmap\":[],\"env\":[],\"img\":\"onlineshop/websphere-traditional\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"websphere\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"1231\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"1000m\",\"memory\":\"1024\"},\"securityContext\":{\"add\":[],\"drop\":[],\"privileged\":false,\"security\":false},\"storage\":[],\"tag\":\"8.5.5.9-install\"}],\"hostIPC\":false,\"hostName\":\"\",\"hostPID\":false,\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"websphere\",\"namespace\":\"garydemo-garyns\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]','onlineshop/websphere-traditional',NULL,1,'garydemo','admin','2017-09-06 10:01:33',0,'HarmonyCloud_Status=C');
/*!40000 ALTER TABLE `service_templates` ENABLE KEYS */;
UNLOCK TABLES;
LOCK TABLES `service_templates` WRITE;
/*!40000 ALTER TABLE `service_templates` DISABLE KEYS */;
INSERT INTO `service_templates` (`id`,`name`,`tag`,`details`,`deployment_content`,`image_list`,`ingress_content`,`status`,`tenant`,`create_user`,`create_time`,`flag`,`node_selector`) VALUES (11,'elasticsearch','1.0',NULL,'[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[],\"command\":[],\"configmap\":[],\"env\":[],\"imagePullPolicy\":\"Always\",\"img\":\"onlineshop/elasticsearch\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"elasticsearch\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"2831\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"500m\",\"memory\":\"512\"},\"securityContext\":{\"add\":[],\"drop\":[],\"privileged\":false,\"security\":false},\"storage\":[],\"tag\":\"v2.4.1-1\"}],\"hostIPC\":false,\"hostName\":\"\",\"hostPID\":false,\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"elasticsearch\",\"namespace\":\"garydemo-garyns\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]','onlineshop/elasticsearch',NULL,1,'garydemo','admin','2017-09-07 19:25:49',0,'HarmonyCloud_Status=C');
INSERT INTO `service_templates` (`id`,`name`,`tag`,`details`,`deployment_content`,`image_list`,`ingress_content`,`status`,`tenant`,`create_user`,`create_time`,`flag`,`node_selector`) VALUES (13,'vp0','1.0',NULL,'[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[\"-c\",\"sleep 10; peer node start\"],\"command\":[\"sh\"],\"configmap\":[],\"env\":[{\"key\":\"CORE_PEER_ID\",\"name\":\"\",\"value\":\"vp0\"},{\"key\":\"CORE_SECURITY_ENROLLID\",\"name\":\"\",\"value\":\"test_vp0\"},{\"key\":\"CORE_SECURITY_ENROLLSECRET\",\"name\":\"\",\"value\":\"MwYpmSRjupbT\"},{\"key\":\"CORE_PEER_ADDRESSAUTODETECT\",\"name\":\"\",\"value\":\"true\"},{\"key\":\"CORE_PEER_NETWORKID\",\"name\":\"\",\"value\":\"dev\"},{\"key\":\"CORE_LOGGING_LEVEL\",\"name\":\"\",\"value\":\"info\"},{\"key\":\"CORE_PEER_VALIDATOR_CONSENSUS_PLUGIN\",\"name\":\"\",\"value\":\"pbft\"},{\"key\":\"CORE_PBFT_GENERAL_N\",\"name\":\"\",\"value\":\"6\"},{\"key\":\"CORE_PBFT_GENERAL_TIMEOUT_REQUEST\",\"name\":\"\",\"value\":\"2s\"},{\"key\":\"CORE_SECURITY_ENABLED\",\"name\":\"\",\"value\":\"true\"},{\"key\":\"CORE_PEER_PKI_ECA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"},{\"key\":\"CORE_PEER_PKI_TCA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"},{\"key\":\"CORE_PEER_PKI_TLSCA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"}],\"img\":\"onlineshop/fabric-peer\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"vp0\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7050\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7051\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7053\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7052\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7054\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7055\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7056\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7057\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7058\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7059\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"500m\",\"memory\":\"512\"},\"storage\":[{\"emptyDir\":\"\",\"gitUrl\":\"\",\"hostPath\":\"/var/run/docker.sock\",\"path\":\"/var/run/docker.sock\",\"pvcBindOne\":\"true\",\"pvcCapacity\":\"\",\"pvcName\":\"\",\"pvcTenantid\":\"b04c83f0c7fd4914b2e7ddf2ef309a0b\",\"readOnly\":\"false\",\"revision\":\"\",\"type\":\"hostPath\",\"volume\":\"\"}],\"tag\":\"0.6\"}],\"hostName\":\"\",\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"vp0\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]','onlineshop/fabric-peer',NULL,1,'cfets','admin','2017-08-18 14:44:42',0,'HarmonyCloud_Status=C');
INSERT INTO `service_templates` (`id`,`name`,`tag`,`details`,`deployment_content`,`image_list`,`ingress_content`,`status`,`tenant`,`create_user`,`create_time`,`flag`,`node_selector`) VALUES (14,'vp1','1.0',NULL,'[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[\"-c\",\"sleep 10; peer node start\"],\"command\":[\"sh\"],\"configmap\":[],\"env\":[{\"key\":\"CORE_PEER_ID\",\"name\":\"\",\"value\":\"vp1\"},{\"key\":\"CORE_PEER_DISCOVERY_ROOTNODE\",\"name\":\"\",\"value\":\"vp0:7051\"},{\"key\":\"CORE_SECURITY_ENROLLID\",\"name\":\"\",\"value\":\"test_vp1\"},{\"key\":\"CORE_SECURITY_ENROLLSECRET\",\"name\":\"\",\"value\":\"5wgHK9qqYaPy\"},{\"key\":\"CORE_PEER_ADDRESSAUTODETECT\",\"name\":\"\",\"value\":\"true\"},{\"key\":\"CORE_PEER_NETWORKID\",\"name\":\"\",\"value\":\"dev\"},{\"key\":\"CORE_LOGGING_LEVEL\",\"name\":\"\",\"value\":\"info\"},{\"key\":\"CORE_PEER_VALIDATOR_CONSENSUS_PLUGIN\",\"name\":\"\",\"value\":\"pbft\"},{\"key\":\"CORE_PBFT_GENERAL_N\",\"name\":\"\",\"value\":\"6\"},{\"key\":\"CORE_PBFT_GENERAL_TIMEOUT_REQUEST\",\"name\":\"\",\"value\":\"2s\"},{\"key\":\"CORE_SECURITY_ENABLED\",\"name\":\"\",\"value\":\"true\"},{\"key\":\"CORE_PEER_PKI_ECA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"},{\"key\":\"CORE_PEER_PKI_TCA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"},{\"key\":\"CORE_PEER_PKI_TLSCA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"}],\"img\":\"onlineshop/fabric-peer\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"vp1\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7050\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7051\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7052\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7053\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7054\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7055\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7056\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7057\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7058\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7059\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"500m\",\"memory\":\"512\"},\"storage\":[{\"emptyDir\":\"\",\"gitUrl\":\"\",\"hostPath\":\"/var/run/docker.sock\",\"path\":\"/var/run/docker.sock\",\"pvcBindOne\":\"true\",\"pvcCapacity\":\"\",\"pvcName\":\"\",\"pvcTenantid\":\"b04c83f0c7fd4914b2e7ddf2ef309a0b\",\"readOnly\":\"false\",\"revision\":\"\",\"type\":\"hostPath\",\"volume\":\"\"}],\"tag\":\"0.6\"}],\"hostName\":\"\",\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"vp1\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]','onlineshop/fabric-peer',NULL,1,'cfets','admin','2017-08-18 14:44:42',0,'HarmonyCloud_Status=C');
INSERT INTO `service_templates` (`id`,`name`,`tag`,`details`,`deployment_content`,`image_list`,`ingress_content`,`status`,`tenant`,`create_user`,`create_time`,`flag`,`node_selector`) VALUES (15,'vp2','1.0',NULL,'[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[\"-c\",\"sleep 10; peer node start\"],\"command\":[\"sh\"],\"configmap\":[],\"env\":[{\"key\":\"CORE_PEER_ID\",\"name\":\"\",\"value\":\"vp2\"},{\"key\":\"CORE_PEER_DISCOVERY_ROOTNODE\",\"name\":\"\",\"value\":\"vp0:7051\"},{\"key\":\"CORE_SECURITY_ENROLLID\",\"name\":\"\",\"value\":\"test_vp2\"},{\"key\":\"CORE_SECURITY_ENROLLSECRET\",\"name\":\"\",\"value\":\"vQelbRvja7cJ\"},{\"key\":\"CORE_PEER_ADDRESSAUTODETECT\",\"name\":\"\",\"value\":\"true\"},{\"key\":\"CORE_PEER_NETWORKID\",\"name\":\"\",\"value\":\"dev\"},{\"key\":\"CORE_LOGGING_LEVEL\",\"name\":\"\",\"value\":\"info\"},{\"key\":\"CORE_PEER_VALIDATOR_CONSENSUS_PLUGIN\",\"name\":\"\",\"value\":\"pbft\"},{\"key\":\"CORE_PBFT_GENERAL_N\",\"name\":\"\",\"value\":\"6\"},{\"key\":\"CORE_PBFT_GENERAL_TIMEOUT_REQUEST\",\"name\":\"\",\"value\":\"2s\"},{\"key\":\"CORE_SECURITY_ENABLED\",\"name\":\"\",\"value\":\"true\"},{\"key\":\"CORE_PEER_PKI_ECA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"},{\"key\":\"CORE_PEER_PKI_TCA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"},{\"key\":\"CORE_PEER_PKI_TLSCA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"}],\"img\":\"onlineshop/fabric-peer\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"vp2\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7050\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7051\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7052\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7053\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7054\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7055\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7056\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7057\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7058\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7059\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"500m\",\"memory\":\"512\"},\"storage\":[{\"emptyDir\":\"\",\"gitUrl\":\"\",\"hostPath\":\"/var/run/docker.sock\",\"path\":\"/var/run/docker.sock\",\"pvcBindOne\":\"true\",\"pvcCapacity\":\"\",\"pvcName\":\"\",\"pvcTenantid\":\"b04c83f0c7fd4914b2e7ddf2ef309a0b\",\"readOnly\":\"false\",\"revision\":\"\",\"type\":\"hostPath\",\"volume\":\"\"}],\"tag\":\"0.6\"}],\"hostName\":\"\",\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"vp2\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]','onlineshop/fabric-peer',NULL,1,'cfets','admin','2017-08-18 14:44:42',0,'HarmonyCloud_Status=C');
INSERT INTO `service_templates` (`id`,`name`,`tag`,`details`,`deployment_content`,`image_list`,`ingress_content`,`status`,`tenant`,`create_user`,`create_time`,`flag`,`node_selector`) VALUES (16,'vp3','1.0',NULL,'[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[\"-c\",\"sleep 10; peer node start\"],\"command\":[\"sh\"],\"configmap\":[],\"env\":[{\"key\":\"CORE_PEER_ID\",\"name\":\"\",\"value\":\"vp3\"},{\"key\":\"CORE_PEER_DISCOVERY_ROOTNODE\",\"name\":\"\",\"value\":\"vp0:7051\"},{\"key\":\"CORE_SECURITY_ENROLLID\",\"name\":\"\",\"value\":\"test_vp3\"},{\"key\":\"CORE_SECURITY_ENROLLSECRET\",\"name\":\"\",\"value\":\"9LKqKH5peurL\"},{\"key\":\"CORE_PEER_ADDRESSAUTODETECT\",\"name\":\"\",\"value\":\"true\"},{\"key\":\"CORE_PEER_NETWORKID\",\"name\":\"\",\"value\":\"dev\"},{\"key\":\"CORE_LOGGING_LEVEL\",\"name\":\"\",\"value\":\"info\"},{\"key\":\"CORE_PEER_VALIDATOR_CONSENSUS_PLUGIN\",\"name\":\"\",\"value\":\"pbft\"},{\"key\":\"CORE_PBFT_GENERAL_N\",\"name\":\"\",\"value\":\"6\"},{\"key\":\"CORE_PBFT_GENERAL_TIMEOUT_REQUEST\",\"name\":\"\",\"value\":\"2s\"},{\"key\":\"CORE_SECURITY_ENABLED\",\"name\":\"\",\"value\":\"true\"},{\"key\":\"CORE_PEER_PKI_ECA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"},{\"key\":\"CORE_PEER_PKI_TCA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"},{\"key\":\"CORE_PEER_PKI_TLSCA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"}],\"img\":\"onlineshop/fabric-peer\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"vp3\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7050\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7051\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7052\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7053\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7054\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7055\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7056\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7057\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7058\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7059\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"500m\",\"memory\":\"512\"},\"storage\":[{\"emptyDir\":\"\",\"gitUrl\":\"\",\"hostPath\":\"/var/run/docker.sock\",\"path\":\"/var/run/docker.sock\",\"pvcBindOne\":\"true\",\"pvcCapacity\":\"\",\"pvcName\":\"\",\"pvcTenantid\":\"b04c83f0c7fd4914b2e7ddf2ef309a0b\",\"readOnly\":\"false\",\"revision\":\"\",\"type\":\"hostPath\",\"volume\":\"\"}],\"tag\":\"0.6\"}],\"hostName\":\"\",\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"vp3\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]','onlineshop/fabric-peer',NULL,1,'cfets','admin','2017-08-18 14:44:42',0,'HarmonyCloud_Status=C');
INSERT INTO `service_templates` (`id`,`name`,`tag`,`details`,`deployment_content`,`image_list`,`ingress_content`,`status`,`tenant`,`create_user`,`create_time`,`flag`,`node_selector`) VALUES (17,'vp4','1.0',NULL,'[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[\"-c\",\"sleep 10; peer node start\"],\"command\":[\"sh\"],\"configmap\":[],\"env\":[{\"key\":\"CORE_PEER_ID\",\"name\":\"\",\"value\":\"vp4\"},{\"key\":\"CORE_SECURITY_ENROLLID\",\"name\":\"\",\"value\":\"test_vp4\"},{\"key\":\"CORE_SECURITY_ENROLLSECRET\",\"name\":\"\",\"value\":\"Pqh90CEW5juZ\"},{\"key\":\"CORE_PEER_DISCOVERY_ROOTNODE\",\"name\":\"\",\"value\":\"vp0:7051\"},{\"key\":\"CORE_PEER_ADDRESSAUTODETECT\",\"name\":\"\",\"value\":\"true\"},{\"key\":\"CORE_PEER_NETWORKID\",\"name\":\"\",\"value\":\"dev\"},{\"key\":\"CORE_LOGGING_LEVEL\",\"name\":\"\",\"value\":\"info\"},{\"key\":\"CORE_PEER_VALIDATOR_CONSENSUS_PLUGIN\",\"name\":\"\",\"value\":\"pbft\"},{\"key\":\"CORE_PBFT_GENERAL_N\",\"name\":\"\",\"value\":\"6\"},{\"key\":\"CORE_PBFT_GENERAL_TIMEOUT_REQUEST\",\"name\":\"\",\"value\":\"2s\"},{\"key\":\"CORE_SECURITY_ENABLED\",\"name\":\"\",\"value\":\"true\"},{\"key\":\"CORE_PEER_PKI_ECA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"},{\"key\":\"CORE_PEER_PKI_TCA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"},{\"key\":\"CORE_PEER_PKI_TLSCA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"}],\"img\":\"onlineshop/fabric-peer\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"vp4\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7050\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7051\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7052\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7053\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7054\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7055\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7056\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7057\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7058\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7059\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"500m\",\"memory\":\"512\"},\"storage\":[{\"emptyDir\":\"\",\"gitUrl\":\"\",\"hostPath\":\"/var/run/docker.sock\",\"path\":\"/var/run/docker.sock\",\"pvcBindOne\":\"true\",\"pvcCapacity\":\"\",\"pvcName\":\"\",\"pvcTenantid\":\"b04c83f0c7fd4914b2e7ddf2ef309a0b\",\"readOnly\":\"false\",\"revision\":\"\",\"type\":\"hostPath\",\"volume\":\"\"}],\"tag\":\"0.6\"}],\"hostName\":\"\",\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"vp4\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]','onlineshop/fabric-peer',NULL,1,'cfets','admin','2017-08-18 14:44:42',0,'HarmonyCloud_Status=C');
INSERT INTO `service_templates` (`id`,`name`,`tag`,`details`,`deployment_content`,`image_list`,`ingress_content`,`status`,`tenant`,`create_user`,`create_time`,`flag`,`node_selector`) VALUES (18,'vp5','1.0',NULL,'[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[\"-c\",\"sleep 10; peer node start\"],\"command\":[\"sh\"],\"configmap\":[],\"env\":[{\"key\":\"CORE_PEER_PKI_ECA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"},{\"key\":\"CORE_PEER_PKI_TCA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"},{\"key\":\"CORE_PEER_PKI_TLSCA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"},{\"key\":\"CORE_PEER_ID\",\"name\":\"\",\"value\":\"vp5\"},{\"key\":\"CORE_SECURITY_ENROLLID\",\"name\":\"\",\"value\":\"test_vp5\"},{\"key\":\"CORE_PBFT_GENERAL_N\",\"name\":\"\",\"value\":\"6\"},{\"key\":\"CORE_SECURITY_ENROLLSECRET\",\"name\":\"\",\"value\":\"FfdvDkAdY81P\"},{\"key\":\"CORE_PEER_DISCOVERY_ROOTNODE\",\"name\":\"\",\"value\":\"vp0:7051\"},{\"key\":\"CORE_PEER_ADDRESSAUTODETECT\",\"name\":\"\",\"value\":\"true\"},{\"key\":\"CORE_PEER_NETWORKID\",\"name\":\"\",\"value\":\"dev\"},{\"key\":\"CORE_LOGGING_LEVEL\",\"name\":\"\",\"value\":\"info\"},{\"key\":\"CORE_PEER_VALIDATOR_CONSENSUS_PLUGIN\",\"name\":\"\",\"value\":\"pbft\"},{\"key\":\"CORE_PBFT_GENERAL_TIMEOUT_REQUEST\",\"name\":\"\",\"value\":\"2s\"},{\"key\":\"CORE_SECURITY_ENABLED\",\"name\":\"\",\"value\":\"true\"}],\"img\":\"onlineshop/fabric-peer\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"vp5\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7050\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7051\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7053\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7052\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7054\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7055\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7056\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7057\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7058\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7059\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"500m\",\"memory\":\"512\"},\"storage\":[{\"emptyDir\":\"\",\"gitUrl\":\"\",\"hostPath\":\"/var/run/docker.sock\",\"path\":\"/var/run/docker.sock\",\"pvcBindOne\":\"true\",\"pvcCapacity\":\"\",\"pvcName\":\"\",\"pvcTenantid\":\"b04c83f0c7fd4914b2e7ddf2ef309a0b\",\"readOnly\":\"false\",\"revision\":\"\",\"type\":\"hostPath\",\"volume\":\"\"}],\"tag\":\"0.6\"}],\"hostName\":\"\",\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"vp5\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]','onlineshop/fabric-peer',NULL,1,'cfets','admin','2017-08-18 14:44:42',0,'HarmonyCloud_Status=C');
INSERT INTO `service_templates` (`id`,`name`,`tag`,`details`,`deployment_content`,`image_list`,`ingress_content`,`status`,`tenant`,`create_user`,`create_time`,`flag`,`node_selector`) VALUES (19,'membersrvc','1.0',NULL,'[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[],\"command\":[\"membersrvc\"],\"configmap\":[],\"env\":[{\"key\":\"CORE_LOGGING_SERVER\",\"name\":\"\",\"value\":\"debug\"}],\"img\":\"onlineshop/fabric-membersrvc\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"membersrvc\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7054\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"500m\",\"memory\":\"512\"},\"storage\":[{\"emptyDir\":\"\",\"gitUrl\":\"\",\"hostPath\":\"/var/run/docker.sock\",\"path\":\"/var/run/docker.sock\",\"pvcBindOne\":\"true\",\"pvcCapacity\":\"\",\"pvcName\":\"\",\"pvcTenantid\":\"b04c83f0c7fd4914b2e7ddf2ef309a0b\",\"readOnly\":\"false\",\"revision\":\"\",\"type\":\"hostPath\",\"volume\":\"\"}],\"tag\":\"0.6\"}],\"hostName\":\"\",\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"membersrvc\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]','onlineshop/fabric-membersrvc',NULL,1,'cfets','admin','2017-08-18 14:44:42',0,'HarmonyCloud_Status=C');
/*!40000 ALTER TABLE `service_templates` ENABLE KEYS */;
UNLOCK TABLES;


--
-- Table structure for table `system_config`
--

DROP TABLE IF EXISTS `system_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `system_config` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `config_name` varchar(255) NOT NULL,
  `config_value` varchar(255) NOT NULL,
  `config_type` varchar(255) NOT NULL,
  `create_user` varchar(255) DEFAULT NULL,
  `update_user` varchar(255) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `config_name` (`config_name`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `system_config`
--

LOCK TABLES `system_config` WRITE;
/*!40000 ALTER TABLE `system_config` DISABLE KEYS */;
INSERT INTO `system_config` VALUES (40,'super_sale','1.0','system','admin','admin','2017-07-22 00:47:25','2017-08-12 14:48:03');
/*!40000 ALTER TABLE `system_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tenant_binding`
--

DROP TABLE IF EXISTS `tenant_binding`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tenant_binding` (
  `id` int(10) NOT NULL AUTO_INCREMENT COMMENT '序号',
  `tenant_id` varchar(255) NOT NULL COMMENT '租户id',
  `tenant_name` varchar(255) NOT NULL COMMENT '租户名称',
  `tm_userNames` varchar(255) NOT NULL COMMENT 'tm id列表',
  `harbor_projects` varchar(200) DEFAULT NULL COMMENT 'harbor projects',
  `network_ids` varchar(100) DEFAULT NULL COMMENT '网络idl列表',
  `k8s_pvs` varchar(500) DEFAULT NULL COMMENT 'k8s pv资源',
  `k8s_namespaces` varchar(255) DEFAULT NULL COMMENT 'k8s namespace资源',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `annotation` varchar(255) DEFAULT NULL COMMENT '备注',
  `cluster_id` int(10) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `tenant_id_UNIQUE` (`tenant_id`),
  UNIQUE KEY `tenant_name_UNIQUE` (`tenant_name`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tenant_binding`
--

LOCK TABLES `tenant_binding` WRITE;
/*!40000 ALTER TABLE `tenant_binding` DISABLE KEYS */;
/*!40000 ALTER TABLE `tenant_binding` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `topology`
--

DROP TABLE IF EXISTS `topology`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `topology` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `business_id` int(10) DEFAULT NULL,
  `source` varchar(45) CHARACTER SET latin1 DEFAULT NULL,
  `target` varchar(45) CHARACTER SET latin1 DEFAULT NULL,
  `details` varchar(250) CHARACTER SET latin1 DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `topology`
--

LOCK TABLES `topology` WRITE;
/*!40000 ALTER TABLE `topology` DISABLE KEYS */;
INSERT INTO `topology` (`id`,`business_id`,`source`,`target`,`details`) VALUES (1,13,'13','14',NULL),(2,13,'13','15',NULL),(3,13,'13','17',NULL),(4,13,'13','18',NULL),(5,13,'13','19',NULL),(6,13,'14','13',NULL),(7,13,'14','15',NULL),(8,13,'14','16',NULL);
INSERT INTO `topology` (`id`,`business_id`,`source`,`target`,`details`) VALUES (9,13,'14','18',NULL),(10,13,'15','13',NULL),(11,13,'15','14',NULL),(12,13,'15','17',NULL),(13,13,'15','18',NULL), (14,13,'16','13',NULL),(15,13,'16','14',NULL);
INSERT INTO `topology` (`id`,`business_id`,`source`,`target`,`details`) VALUES (16,13,'16','15',NULL),(17,13,'16','17',NULL),(18,13,'16','18',NULL),(19,13,'17','13',NULL),(20,13,'17','14',NULL),(21,13,'17','15',NULL),(22,13,'17','16',NULL);
INSERT INTO `topology` (`id`,`business_id`,`source`,`target`,`details`) VALUES (23,13,'17','18',NULL),(24,13,'18','13',NULL),(25,13,'18','14',NULL),(26,13,'18','15',NULL),(27,13,'18','17',NULL),(28,13,'18','16',NULL);
/*!40000 ALTER TABLE `topology` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user` (
  `uuid` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `token` varchar(255) DEFAULT NULL,
  `create_time` timestamp NULL DEFAULT NULL,
  `token_create` timestamp NULL DEFAULT NULL,
  `isAdmin` tinyint(1) DEFAULT '0',
  `update_time` timestamp NULL DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `real_name` varchar(255) DEFAULT NULL,
  `isMachine` tinyint(1) DEFAULT '0' COMMENT '是否是机器账号',
  `comment` varchar(255) DEFAULT NULL,
  `pause` varchar(255) DEFAULT NULL COMMENT '用户的状态',
  `phone` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`uuid`),
  UNIQUE KEY `username_UNIQUE` (`username`),
  UNIQUE KEY `email_UNIQUE` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 AVG_ROW_LENGTH=910;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'admin','A0A475CF454CF9A06979034098167B9E','330957b867a3462ea457bec41410624b',NULL,'2016-11-03 14:48:37',1,'2017-06-16 15:43:27',NULL,NULL,0,NULL,'normal',NULL);
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_group`
--

DROP TABLE IF EXISTS `user_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_group` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `groupname` varchar(255) CHARACTER SET utf8 NOT NULL,
  `user_group_describe` varchar(255) CHARACTER SET utf8 DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `groupname_UNIQUE` (`groupname`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_group`
--

LOCK TABLES `user_group` WRITE;
/*!40000 ALTER TABLE `user_group` DISABLE KEYS */;
INSERT INTO `user_group` VALUES (9,'云平台','创建测试使用');
/*!40000 ALTER TABLE `user_group` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_group_relation`
--

DROP TABLE IF EXISTS `user_group_relation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_group_relation` (
  `userid` bigint(20) NOT NULL,
  `groupid` int(11) NOT NULL,
  UNIQUE KEY `user` (`userid`) USING BTREE,
  KEY `group` (`groupid`),
  CONSTRAINT `group` FOREIGN KEY (`groupid`) REFERENCES `user_group` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `user` FOREIGN KEY (`userid`) REFERENCES `user` (`uuid`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_group_relation`
--

LOCK TABLES `user_group_relation` WRITE;
/*!40000 ALTER TABLE `user_group_relation` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_group_relation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_tenant`
--

DROP TABLE IF EXISTS `user_tenant`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_tenant` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `tenantid` varchar(255) NOT NULL,
  `username` varchar(255) NOT NULL,
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `istm` int(11) DEFAULT '0',
  `role` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_tenant`
--

LOCK TABLES `user_tenant` WRITE;
/*!40000 ALTER TABLE `user_tenant` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_tenant` ENABLE KEYS */;
UNLOCK TABLES;

-- ----------------------------
-- Table structure for cluster_loadbalance
-- ----------------------------
DROP TABLE IF EXISTS `cluster_loadbalance`;
CREATE TABLE `cluster_loadbalance` (
  `lb_id` int(11) NOT NULL AUTO_INCREMENT,
  `cluster_id` int(11) DEFAULT NULL,
  `loadbalance_name` varchar(255) DEFAULT NULL,
  `loadbalance_ip` varchar(255) DEFAULT NULL,
  `loadbalance_port` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`lb_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of cluster_loadbalance
-- ----------------------------
INSERT INTO `cluster_loadbalance` VALUES ('1', '188', 'haproxy', '10.10.101.143', '30200');
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2017-08-19 17:07:08
