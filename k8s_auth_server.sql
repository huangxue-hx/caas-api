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
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COMMENT='第三方用户认证之后插入Harbor中的用户';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `auth_user`
--

LOCK TABLES `auth_user` WRITE;
/*!40000 ALTER TABLE `auth_user` DISABLE KEYS */;
INSERT INTO `auth_user` VALUES (1,'testaaa','123456','330','admin');
/*!40000 ALTER TABLE `auth_user` ENABLE KEYS */;
UNLOCK TABLES;

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
) ENGINE=InnoDB AUTO_INCREMENT=622 DEFAULT CHARSET=utf8;
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
  `status` int(11) DEFAULT NULL,
  `is_external` int(1) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1682 DEFAULT CHARSET=utf8;
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
  `status` int(11) DEFAULT NULL,
  `tenant` varchar(45) CHARACTER SET latin1 DEFAULT NULL,
  `user` varchar(45) CHARACTER SET latin1 DEFAULT NULL,
  `create_time` timestamp NULL DEFAULT NULL,
  `update_time` timestamp NULL DEFAULT NULL,
  `is_deploy` int(11) DEFAULT NULL,
  `image_list` varchar(512) CHARACTER SET latin1 DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=912 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `business_templates`
--

LOCK TABLES `business_templates` WRITE;
/*!40000 ALTER TABLE `business_templates` DISABLE KEYS */;
INSERT INTO `business_templates` (`id`,`name`,`tag`,`details`,`status`,`tenant`,`user`,`create_time`,`update_time`,`is_deploy`,`image_list`) VALUES (1,'Tomcat','1.0',NULL,0,'all','admin','2017-08-11 14:18:36',NULL,1,'onlineshop/tomcat'),(2,'Redis','1.0',NULL,0,'all','admin','2017-08-11 14:44:14',NULL,1,'onlineshop/redis'),(3,'WordPress','1.0',NULL,0,'all','admin','2017-08-11 15:31:54',NULL,1,'onlineshop/wordpress'),(4,'InfluxDB','1.0',NULL,0,'all','admin','2017-08-11 16:12:19',NULL,1,'onlineshop/influxdb'),(5,'MySQL','1.0',NULL,0,'all','admin','2017-08-11 18:03:10',NULL,1,'onlineshop/mysqls'),(6,'Webhook','1.0',NULL,0,'all','admin','2017-08-12 15:54:52',NULL,NULL,'onlineshop/webhook'),(7,'Mongodb','1.0',NULL,0,'all','admin','2017-09-05 17:02:28',NULL,NULL,'onlineshop/mongodb'),(8,'Rabbitmq','1.0',NULL,0,'all','admin','2017-09-05 17:03:30',NULL,NULL,'onlineshop/rabbitmq'),(9,'Nginx','1.0',NULL,0,'all','admin','2017-09-05 17:05:19',NULL,NULL,'onlineshop/nginx'),(10,'Websphere','1.0',NULL,0,'all','admin','2017-09-05 17:06:07',NULL,NULL,'onlineshop/websphere'),(11,'Elasticsearch','1.0',NULL,0,'all','admin','2017-09-05 17:07:25',NULL,NULL,'onlineshop/elasticsearch'),(13,'Fabric0.6','1.0',NULL,0,'all','admin','2017-08-18 14:44:42',NULL,1,'onlineshop/fabric-peer,onlineshop/fabric-membersrvc');
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
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
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
  `build_num` varchar(20) DEFAULT NULL,
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
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cicd_stage_type`
--

LOCK TABLES `cicd_stage_type` WRITE;
/*!40000 ALTER TABLE `cicd_stage_type` DISABLE KEYS */;
INSERT INTO `cicd_stage_type` VALUES (1,'代码检出/编译',0,NULL,0),(2,'单元测试',0,NULL,4),(3,'镜像构建',0,NULL,1),(4,'应用部署',0,NULL,2),(5,'集成测试',0,NULL,4),(6,'自定义',0,NULL,3);
/*!40000 ALTER TABLE `cicd_stage_type` ENABLE KEYS */;
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
  `user` varchar(255) DEFAULT NULL,
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
  `id` int(255) NOT NULL AUTO_INCREMENT,
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
INSERT INTO `message` VALUES (1,'e28545b26aa3fa384d07924d9164cc85','https://api.netease.im/sms/sendtemplate.action','c0bfefaa0d88','12345','3056657','网易云信');
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
) ENGINE=InnoDB AUTO_INCREMENT=166 DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB AUTO_INCREMENT=279 DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB AUTO_INCREMENT=122 DEFAULT CHARSET=utf8;
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
INSERT INTO `nodeport` VALUES (1,'30000'),(2,'30001'),(3,'30002'),(4,'30003'),(5,'30004'),(6,'30005'),(7,'30006'),(8,'30007'),(9,'30008'),(10,'30009'),(11,'30010'),(12,'30011'),(13,'30012'),(14,'30013'),(15,'30014'),(16,'30015'),(17,'30016'),(18,'30017'),(19,'30018'),(20,'30019'),(21,'30020'),(22,'30021'),(23,'30022'),(24,'30023'),(25,'30024'),(26,'30025'),(27,'30026'),(28,'30027'),(29,'30028'),(30,'30029'),(31,'30030'),(32,'30031'),(33,'30032'),(34,'30033'),(35,'30034'),(36,'30035'),(37,'30036'),(38,'30037'),(39,'30038'),(40,'30039'),(41,'30040'),(42,'30041'),(43,'30042'),(44,'30043'),(45,'30044'),(46,'30045'),(47,'30046'),(48,'30047'),(49,'30048'),(50,'30049'),(51,'30050');
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
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `tenant_id` varchar(255) DEFAULT NULL,
  `tenant_name` varchar(255) NOT NULL,
  `namespace` varchar(255) NOT NULL,
  `is_private` int(10) NOT NULL DEFAULT '0' COMMENT '是否是私有分区，0共享分区，1表示私有分区',
  `label` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=189 DEFAULT CHARSET=latin1;
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

DROP TABLE IF EXISTS `resource`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `resource` (
  `id` int(10) NOT NULL COMMENT '序号',
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
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `resource`
--

LOCK TABLES `resource` WRITE;
/*!40000 ALTER TABLE `resource` DISABLE KEYS */;
INSERT INTO `resource` VALUES (1,'总览','menu','overview',0,NULL,1,NULL,NULL,1,NULL,'menu-icon mi-overview','admin'),(2,'集群','menu','cluster',0,NULL,2,NULL,NULL,1,NULL,'menu-icon mi-cluster','admin'),(3,'租户管理','menu','tenant',0,NULL,3,NULL,NULL,1,NULL,'menu-icon mi-tenant','admin'),(4,'应用中心','menu','',0,NULL,4,NULL,NULL,1,NULL,'menu-icon mi-application','admin'),(5,'CICD','menu','',0,NULL,5,NULL,NULL,1,NULL,'menu-icon mi-cicd','admin'),(6,'交付中心','menu','',0,NULL,6,NULL,NULL,1,NULL,'menu-icon mi-centers','admin'),(7,'日志管理','menu','',0,NULL,7,NULL,NULL,1,NULL,'menu-icon mi-audit','admin'),(8,'告警中心','menu','',0,NULL,8,NULL,NULL,1,NULL,'menu-icon mi-alarm','admin'),(9,'应用','menu','manageList',4,'4/9',9,NULL,NULL,1,NULL,'','admin'),(10,'服务','menu','manageApplyList',4,'4/10',10,NULL,NULL,1,NULL,'','admin'),(11,'外部服务','menu','externalService',4,'4/11',11,NULL,NULL,1,NULL,'','admin'),(12,'配置中心','menu','configcenter',4,'4/12',12,NULL,NULL,1,NULL,'','admin'),(13,'存储','menu','storageScheme',4,'4/13',13,NULL,NULL,1,NULL,'','admin'),(14,'批量任务','menu','job',4,'4/14',14,NULL,NULL,0,NULL,'','admin'),(15,'Docker file管理','menu','dockerfileList',5,'5/15',15,NULL,NULL,1,NULL,NULL,'admin'),(16,'流水线','menu','pipelineList',5,'5/16',16,NULL,NULL,1,NULL,NULL,'admin'),(17,'依赖管理','menu','dependenceList',5,'5/17',17,NULL,NULL,1,NULL,'','admin'),(18,'镜像仓库','menu','mirrorContent',6,'6/18',18,NULL,NULL,1,NULL,'','admin'),(19,'应用商店','menu','deliveryStore',6,'6/19',19,NULL,NULL,1,NULL,'','admin'),(20,'模板管理','menu','manageAll',6,'6/20',20,NULL,NULL,1,NULL,'','admin'),(21,'操作审计','menu','audit',7,'7/21',21,NULL,NULL,1,NULL,'','admin'),(22,'日志查询','menu','logQuery',7,'7/22',22,NULL,NULL,0,NULL,'','admin'),(23,'告警规则','menu','alarmList',8,'8/23',23,NULL,NULL,1,NULL,'','admin'),(24,'告警处理中心','menu','alarmHandingList',8,'8/24',24,NULL,NULL,1,NULL,'','admin'),(25,'系统设置','menu','system',0,'',45,NULL,NULL,1,NULL,'menu-icon mi-config','admin'),(33,'我的租户','menu','tenant',0,NULL,25,NULL,NULL,1,NULL,'menu-icon mi-tenant','dev'),(34,'应用中心','menu','',0,NULL,26,NULL,NULL,1,NULL,'menu-icon mi-application','dev'),(35,'CICD','menu','',0,NULL,27,NULL,NULL,1,NULL,'menu-icon mi-cicd','dev'),(36,'交付中心','menu','',0,NULL,28,NULL,NULL,1,NULL,'menu-icon mi-centers','dev'),(37,'日志管理','menu','',0,NULL,29,NULL,NULL,0,NULL,'menu-icon mi-audit','dev'),(38,'告警中心','menu','',0,NULL,30,NULL,NULL,1,NULL,'menu-icon mi-alarm','dev'),(39,'应用','menu','manageList',34,'34/39',31,NULL,NULL,1,NULL,'','dev'),(40,'服务','menu','manageApplyList',34,'34/40',32,NULL,NULL,1,NULL,'','dev'),(41,'外部服务','menu','externalService',34,'34/41',33,NULL,NULL,1,NULL,'','dev'),(42,'配置中心','menu','configcenter',34,'34/42',34,NULL,NULL,1,NULL,'','dev'),(43,'存储','menu','storageScheme',34,'34/43',35,NULL,NULL,1,NULL,'','dev'),(44,'批量任务','menu','job',34,'34/44',36,NULL,NULL,0,NULL,'','dev'),(45,'Docker file管理','menu','dockerfileList',35,'35/45',37,NULL,NULL,1,NULL,NULL,'dev'),(46,'流水线','menu','pipelineList',35,'35/46',38,NULL,NULL,1,NULL,NULL,'dev'),(47,'依赖管理','menu','dependenceList',35,'35/47',39,NULL,NULL,1,NULL,'','dev'),(48,'镜像仓库','menu','mirrorContent',36,'36/48',40,NULL,NULL,1,NULL,'','dev'),(49,'应用商店','menu','deliveryStore',36,'36/49',41,NULL,NULL,1,NULL,'','dev'),(50,'模板管理','menu','manageAll',36,'36/50',42,NULL,NULL,1,NULL,'','dev'),(51,'日志查询','menu','logQuery',37,'37/51',43,NULL,NULL,0,NULL,'','dev'),(52,'告警规则','menu','alarmList',38,'38/52',44,NULL,NULL,1,NULL,'','dev'),(53,'告警处理中心','menu','alarmHandingList',38,'38/53',45,NULL,NULL,1,NULL,'','dev'),(54,'系统设置','menu','system',0,'',45,NULL,NULL,0,NULL,'menu-icon mi-config','dev');
/*!40000 ALTER TABLE `resource` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `resource_custom`
--

DROP TABLE IF EXISTS `resource_custom`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `resource_custom` (
  `id` int(10) NOT NULL COMMENT '序号',
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
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `resource_custom`
--

LOCK TABLES `resource_custom` WRITE;
/*!40000 ALTER TABLE `resource_custom` DISABLE KEYS */;
INSERT INTO `resource_custom` VALUES (1,'总览','menu','overview',0,NULL,1,NULL,NULL,1,NULL,'menu-icon mi-overview','admin'),(2,'集群','menu','cluster',0,NULL,2,NULL,NULL,1,NULL,'menu-icon mi-cluster','admin'),(3,'租户管理','menu','tenant',0,NULL,3,NULL,NULL,1,NULL,'menu-icon mi-tenant','admin'),(4,'应用中心','menu','manage',0,NULL,4,NULL,NULL,1,NULL,'menu-icon mi-application','admin'),(5,'CICD','menu','cicd',0,NULL,5,NULL,NULL,1,NULL,'menu-icon mi-cicd','admin'),(6,'交付中心','menu','mirror',0,NULL,6,NULL,NULL,1,NULL,'menu-icon mi-centers','admin'),(7,'日志管理','menu','audit',0,NULL,7,NULL,NULL,1,NULL,'menu-icon mi-audit','admin'),(8,'告警中心','menu','alarm',0,NULL,8,NULL,NULL,1,NULL,'menu-icon mi-alarm','admin'),(9,'应用','menu','manageList',4,'4/9',9,NULL,NULL,1,NULL,'','admin'),(10,'服务','menu','manageApplyList',4,'4/10',10,NULL,NULL,1,NULL,'','admin'),(11,'外部服务','menu','externalService',4,'4/11',11,NULL,NULL,1,NULL,'','admin'),(12,'配置中心','menu','configcenter',4,'4/12',12,NULL,NULL,1,NULL,'','admin'),(13,'存储','menu','storageScheme',4,'4/13',13,NULL,NULL,1,NULL,'','admin'),(14,'批量任务','menu','job',4,'4/14',14,NULL,NULL,0,NULL,'','admin'),(15,'Docker file管理','menu','dockerfileList',5,'5/15',15,NULL,NULL,1,NULL,NULL,'admin'),(16,'流水线','menu','pipelineList',5,'5/16',16,NULL,NULL,1,NULL,NULL,'admin'),(17,'依赖管理','menu','dependenceList',5,'5/17',17,NULL,NULL,1,NULL,'','admin'),(18,'镜像仓库','menu','mirrorContent',6,'6/18',18,NULL,NULL,1,NULL,'','admin'),(19,'应用商店','menu','deliveryStore',6,'6/19',19,NULL,NULL,1,NULL,'','admin'),(20,'模板管理','menu','manageAll',6,'6/20',20,NULL,NULL,1,NULL,'','admin'),(21,'操作审计','menu','audit',7,'7/21',21,NULL,NULL,1,NULL,'','admin'),(22,'日志查询','menu','logQuery',7,'7/22',22,NULL,NULL,0,NULL,'','admin'),(23,'告警规则','menu','alarmList',8,'8/23',23,NULL,NULL,1,NULL,'','admin'),(24,'告警处理中心','menu','alarmHandingList',8,'8/24',24,NULL,NULL,1,NULL,'','admin'),(33,'我的租户','menu','tenant',0,NULL,25,NULL,NULL,1,NULL,'menu-icon mi-tenant','dev'),(34,'应用中心','menu','manage',0,NULL,26,NULL,NULL,1,NULL,'menu-icon mi-application','dev'),(35,'CICD','menu','cicd',0,NULL,27,NULL,NULL,1,NULL,'menu-icon mi-cicd','dev'),(36,'交付中心','menu','mirror',0,NULL,28,NULL,NULL,1,NULL,'menu-icon mi-centers','dev'),(37,'日志管理','menu','audit',0,NULL,29,NULL,NULL,1,NULL,'menu-icon mi-audit','dev'),(38,'告警中心','menu','alarm',0,NULL,30,NULL,NULL,1,NULL,'menu-icon mi-alarm','dev'),(39,'应用','menu','manageList',34,'34/39',31,NULL,NULL,1,NULL,'','dev'),(40,'服务','menu','manageApplyList',34,'34/40',32,NULL,NULL,1,NULL,'','dev'),(41,'外部服务','menu','externalService',34,'34/41',33,NULL,NULL,1,NULL,'','dev'),(42,'配置中心','menu','configcenter',34,'34/42',34,NULL,NULL,1,NULL,'','dev'),(43,'存储','menu','storageScheme',34,'34/43',35,NULL,NULL,1,NULL,'','dev'),(44,'批量任务','menu','job',34,'34/44',36,NULL,NULL,0,NULL,'','dev'),(45,'Docker file管理','menu','dockerfileList',35,'35/45',37,NULL,NULL,1,NULL,NULL,'dev'),(46,'流水线','menu','pipelineList',35,'35/46',38,NULL,NULL,1,NULL,NULL,'dev'),(47,'依赖管理','menu','dependenceList',35,'35/47',39,NULL,NULL,1,NULL,'','dev'),(48,'镜像仓库','menu','mirrorContent',36,'36/48',40,NULL,NULL,1,NULL,'','dev'),(49,'应用商店','menu','deliveryStore',36,'36/49',41,NULL,NULL,1,NULL,'','dev'),(50,'模板管理','menu','manageAll',36,'36/50',42,NULL,NULL,1,NULL,'','dev'),(51,'日志查询','menu','logQuery',37,'37/51',43,NULL,NULL,0,NULL,'','dev'),(52,'告警规则','menu','alarmList',38,'38/52',44,NULL,NULL,1,NULL,'','dev'),(53,'告警处理中心','menu','alarmHandingList',38,'38/53',45,NULL,NULL,1,NULL,'','dev');
/*!40000 ALTER TABLE `resource_custom` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `role`
--

DROP TABLE IF EXISTS `role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `role` (
  `id` int(10) NOT NULL COMMENT '序号',
  `name` varchar(100) DEFAULT NULL COMMENT '角色名称',
  `description` varchar(100) DEFAULT NULL COMMENT '描述',
  `resource_ids` varchar(500) DEFAULT NULL COMMENT '资源id',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `available` tinyint(1) DEFAULT '1' COMMENT '是否可用 1可用 0不可用',
  `second_resource_ids` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_UNIQUE` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `role`
--

LOCK TABLES `role` WRITE;
/*!40000 ALTER TABLE `role` DISABLE KEYS */;
INSERT INTO `role` VALUES (1,'superadmin','','10000,11000,30000,31000,40000,41000,42000,43000,44000,44100,44200,50000,51000,52000,53000,54000,55000,60000,61000,61100,61200,61300,61400,61500,61600,61700,62000,62100,62200,62300,62400,62500,62600,63000,70000,71000',NULL,NULL,1,NULL),(2,'admin','','10000,11000,30000,31000,40000,41000,44000,44100,44200,50000,51000,52000,53000,54000,55000,60000,61000,61100,61200,61300,61400,61500,61600,61700,62000,62100,62200,62300,62400,62500,62600,63000,70000,71000',NULL,NULL,1,NULL),(3,'tm','','10000,11000,40000,42000',NULL,NULL,1,NULL),(4,'pm','','10000,12000,40000,43000',NULL,NULL,1,NULL),(5,'dev','','10000,12000,20000,21000,22000,23000,24000,24100,24200',NULL,NULL,1,NULL),(6,'tester','','10000,12000,20000,21000,22000,23000,24000,24100,24200',NULL,NULL,1,NULL);
/*!40000 ALTER TABLE `role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `role_privilege`
--

DROP TABLE IF EXISTS `role_privilege`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `role_privilege` (
  `id` int(100) NOT NULL,
  `role` varchar(255) NOT NULL DEFAULT '' COMMENT '角色名',
  `privilege` varchar(255) DEFAULT '' COMMENT '权限',
  `update_time` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `first_module` varchar(255) DEFAULT '' COMMENT '一级模块',
  `second_module` varchar(255) DEFAULT NULL COMMENT '二级模块',
  `third_module` varchar(255) DEFAULT NULL COMMENT '三级模块',
  `status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '权限的状态 0 未启用 1 启用',
  `mark` varchar(255) DEFAULT NULL COMMENT '保留字段',
  `parentid` int(100) DEFAULT NULL,
  `isParent` tinyint(1) DEFAULT '0' COMMENT '是否是父节点 0 子节点 1 父节点',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `role_privilege`
--

LOCK TABLES `role_privilege` WRITE;
/*!40000 ALTER TABLE `role_privilege` DISABLE KEYS */;
INSERT INTO `role_privilege` VALUES (1,'dev','','2017-08-10 11:26:07','tenant',NULL,NULL,1,'租户管理',0,1),(2,'dev','','2017-08-10 11:26:09','tenant','',NULL,1,'租户',1,1),(3,'dev','list','2017-08-10 11:26:10','tenant',NULL,NULL,1,'租户查询',2,0),(4,'dev','create','2017-08-12 07:48:42','tenant','',NULL,1,'租户创建',2,0),(5,'dev','delete','2017-08-10 11:26:14','tenant','',NULL,1,'租户删除',2,0),(6,'dev','','2017-08-10 11:26:16','namespace',NULL,NULL,1,'分区',1,1),(7,'dev','create','2017-08-12 07:48:44','namespace','',NULL,1,'租户分区创建',6,0),(8,'dev','delete','2017-08-10 11:26:19','namespace','',NULL,1,'租户分区删除',6,0),(9,'dev','update','2017-08-10 11:26:20','namespace','',NULL,1,'租户分区修改',6,0),(10,'dev','list','2017-08-10 11:28:22','namespace','',NULL,1,'租户分区查询',6,0),(11,'dev','','2017-08-10 11:26:21','mirror',NULL,NULL,1,'镜像仓库',1,1),(12,'dev','list','2017-08-10 11:26:24','mirror','',NULL,1,'租户镜像仓库查询',11,0),(13,'dev','create','2017-08-10 11:26:24','mirror','',NULL,1,'租户镜像仓库创建',11,0),(14,'dev','update','2017-08-10 11:26:27','mirror','',NULL,1,'租户镜像仓库修改',11,0),(15,'dev','delete','2017-08-10 11:26:26','mirror','',NULL,1,'租户镜像仓库删除',11,0),(16,'dev','','2017-08-10 11:26:30','user','',NULL,1,'用户',1,1),(17,'dev','create','2017-08-11 10:34:34','user','',NULL,1,'租户用户添加',16,0),(18,'dev','list','2017-08-10 11:26:32','user','',NULL,1,'租户用户查询',16,0),(19,'dev','delete','2017-08-10 11:26:33','user','',NULL,1,'租户用户移除',16,0),(20,'dev','update','2017-08-10 11:26:37','user',NULL,NULL,1,'租户用户修改',16,0);
/*!40000 ALTER TABLE `role_privilege` ENABLE KEYS */;
UNLOCK TABLES;

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
) ENGINE=InnoDB AUTO_INCREMENT=1158 DEFAULT CHARSET=utf8;
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
  `status` int(11) DEFAULT NULL,
  `tenant` varchar(45) CHARACTER SET latin1 DEFAULT NULL,
  `user` varchar(45) CHARACTER SET latin1 DEFAULT NULL,
  `create_time` timestamp NULL DEFAULT NULL,
  `flag` int(11) DEFAULT '0',
  `node_selector` varchar(225) CHARACTER SET latin1 DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1399 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `service_templates`
--

LOCK TABLES `service_templates` WRITE;
/*!40000 ALTER TABLE `service_templates` DISABLE KEYS */;
INSERT INTO `service_templates` (`id`,`name`,`tag`,`details`,`deployment_content`,`image_list`,`ingress_content`,`status`,`tenant`,`user`,`create_time`,`flag`,`node_selector`) VALUES (1,'tomcat','1.0',NULL,'[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[],\"command\":[],\"configmap\":[],\"env\":[],\"img\":\"onlineshop/tomcat\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"tomcat\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"8080\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"500m\",\"memory\":\"512\"},\"storage\":[],\"tag\":\"v8.0\"}],\"hostName\":\"\",\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"tomcat\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]','onlineshop/tomcat',NULL,1,'gywtesttenant','admin','2017-08-11 14:18:36',0,'HarmonyCloud_Status=C');
INSERT INTO `service_templates` (`id`,`name`,`tag`,`details`,`deployment_content`,`image_list`,`ingress_content`,`status`,`tenant`,`user`,`create_time`,`flag`,`node_selector`) VALUES (2,'redis','1.0',NULL,'[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[],\"command\":[],\"configmap\":[],\"env\":[],\"img\":\"onlineshop/redis\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"redis\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"6379\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"100m\",\"memory\":\"128\"},\"storage\":[],\"tag\":\"3.2-alpine\"}],\"hostName\":\"\",\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"redis\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]','onlineshop/redis',NULL,1,'gywtesttenant','admin','2017-08-11 14:44:14',0,'HarmonyCloud_Status=C');
INSERT INTO `service_templates` (`id`,`name`,`tag`,`details`,`deployment_content`,`image_list`,`ingress_content`,`status`,`tenant`,`user`,`create_time`,`flag`,`node_selector`) VALUES (3,'wordpress','1.0',NULL,'[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[],\"command\":[],\"configmap\":[],\"env\":[],\"img\":\"onlineshop/wordpress\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"wordpress\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"80\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"500m\",\"memory\":\"512\"},\"storage\":[],\"tag\":\"4.8.0-php7.1-fpm-alpine\"}],\"hostName\":\"\",\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"wordpress\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]','onlineshop/wordpress',NULL,1,'gywtesttenant','admin','2017-08-11 15:31:54',0,'HarmonyCloud_Status=C');
INSERT INTO `service_templates` (`id`,`name`,`tag`,`details`,`deployment_content`,`image_list`,`ingress_content`,`status`,`tenant`,`user`,`create_time`,`flag`,`node_selector`) VALUES (4,'influxdb','1.0',NULL,'[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[],\"command\":[],\"configmap\":[],\"env\":[],\"img\":\"onlineshop/influxdb\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"influxdb2\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"8086\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"500m\",\"memory\":\"512\"},\"storage\":[],\"tag\":\"v1.3.0\"}],\"hostName\":\"\",\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"influxdb2\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]','onlineshop/influxdb',NULL,1,'gywtesttenant','admin','2017-08-11 17:30:21',0,'HarmonyCloud_Status=C');
INSERT INTO `service_templates` (`id`,`name`,`tag`,`details`,`deployment_content`,`image_list`,`ingress_content`,`status`,`tenant`,`user`,`create_time`,`flag`,`node_selector`) VALUES (5,'mysql','1.0',NULL,'[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[],\"command\":[],\"configmap\":[],\"env\":[],\"img\":\"onlineshop/mysqls\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"mysql\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"1433\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"1000m\",\"memory\":\"1024\"},\"storage\":[],\"tag\":\"v1\"}],\"hostName\":\"\",\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"mysql\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]','onlineshop/mysqls',NULL,1,'gywtesttenant','admin','2017-08-11 18:03:10',0,'HarmonyCloud_Status=C');
INSERT INTO `service_templates` (`id`,`name`,`tag`,`details`,`deployment_content`,`image_list`,`ingress_content`,`status`,`tenant`,`user`,`create_time`,`flag`,`node_selector`) VALUES (6,'webhook','1.0',NULL,'[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[],\"command\":[],\"configmap\":[],\"env\":[],\"img\":\"onlineshop/webhook\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"webhook\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"1433\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"1000m\",\"memory\":\"1024\"},\"storage\":[],\"tag\":\"2.6.5\"}],\"hostName\":\"\",\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"webhook\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]','onlineshop/webhook',NULL,1,'gywtesttenant','admin','2017-08-12 15:54:52',0,'HarmonyCloud_Status=C');
INSERT INTO `service_templates` (`id`,`name`,`tag`,`details`,`deployment_content`,`image_list`,`ingress_content`,`status`,`tenant`,`user`,`create_time`,`flag`,`node_selector`) VALUES (7,'mongodb','1.0',NULL,'[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[],\"command\":[],\"configmap\":[],\"env\":[],\"imagePullPolicy\":\"Always\",\"img\":\"onlineshop/mongodb\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"mongodb\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"1234\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"100m\",\"memory\":\"128\"},\"securityContext\":{\"add\":[],\"drop\":[],\"privileged\":false,\"security\":false},\"storage\":[],\"tag\":\"v3.5\"}],\"hostIPC\":false,\"hostName\":\"\",\"hostPID\":false,\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"mongodb\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]','onlineshop/mongodb',NULL,1,'garydemo','admin','2017-09-07 19:10:27',0,'HarmonyCloud_Status=C');
INSERT INTO `service_templates` (`id`,`name`,`tag`,`details`,`deployment_content`,`image_list`,`ingress_content`,`status`,`tenant`,`user`,`create_time`,`flag`,`node_selector`) VALUES (8,'rabbitmq','1.0',NULL,'[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[],\"command\":[],\"configmap\":[],\"env\":[],\"imagePullPolicy\":\"Always\",\"img\":\"onlineshop/rabbitmq\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"rabbitmq\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"1231\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"500m\",\"memory\":\"512\"},\"securityContext\":{\"add\":[],\"drop\":[],\"privileged\":false,\"security\":false},\"storage\":[],\"tag\":\"3.6.11\"}],\"hostIPC\":false,\"hostName\":\"\",\"hostPID\":false,\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"rabbitmq\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]','onlineshop/rabbitmq',NULL,1,'garydemo','admin','2017-09-09 10:44:14',0,'HarmonyCloud_Status=C');
INSERT INTO `service_templates` (`id`,`name`,`tag`,`details`,`deployment_content`,`image_list`,`ingress_content`,`status`,`tenant`,`user`,`create_time`,`flag`,`node_selector`) VALUES (9,'nginx','1.0',NULL,'[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[],\"command\":[],\"configmap\":[],\"env\":[],\"imagePullPolicy\":\"Always\",\"img\":\"onlineshop/nginx\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"nginxcon\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"80\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"100m\",\"memory\":\"128\"},\"securityContext\":{\"add\":[],\"drop\":[],\"privileged\":false,\"security\":false},\"storage\":[],\"tag\":\"latest\"}],\"hostIPC\":false,\"hostName\":\"\",\"hostPID\":false,\"instance\":\"2\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"nginxsvc\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]','onlineshop/nginx',NULL,1,'garydemo','admin','2017-09-06 15:26:07',0,'HarmonyCloud_Status=C');
INSERT INTO `service_templates` (`id`,`name`,`tag`,`details`,`deployment_content`,`image_list`,`ingress_content`,`status`,`tenant`,`user`,`create_time`,`flag`,`node_selector`) VALUES (10,'websphere','1.0',NULL,'[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[],\"command\":[],\"configmap\":[],\"env\":[],\"img\":\"onlineshop/websphere-traditional\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"websphere\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"1231\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"1000m\",\"memory\":\"1024\"},\"securityContext\":{\"add\":[],\"drop\":[],\"privileged\":false,\"security\":false},\"storage\":[],\"tag\":\"8.5.5.9-install\"}],\"hostIPC\":false,\"hostName\":\"\",\"hostPID\":false,\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"websphere\",\"namespace\":\"garydemo-garyns\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]','onlineshop/websphere-traditional',NULL,1,'garydemo','admin','2017-09-06 10:01:33',0,'HarmonyCloud_Status=C');
/*!40000 ALTER TABLE `service_templates` ENABLE KEYS */;
UNLOCK TABLES;
LOCK TABLES `service_templates` WRITE;
/*!40000 ALTER TABLE `service_templates` DISABLE KEYS */;
INSERT INTO `service_templates` (`id`,`name`,`tag`,`details`,`deployment_content`,`image_list`,`ingress_content`,`status`,`tenant`,`user`,`create_time`,`flag`,`node_selector`) VALUES (11,'elasticsearch','1.0',NULL,'[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[],\"command\":[],\"configmap\":[],\"env\":[],\"imagePullPolicy\":\"Always\",\"img\":\"onlineshop/elasticsearch\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"elasticsearch\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"2831\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"500m\",\"memory\":\"512\"},\"securityContext\":{\"add\":[],\"drop\":[],\"privileged\":false,\"security\":false},\"storage\":[],\"tag\":\"v2.4.1-1\"}],\"hostIPC\":false,\"hostName\":\"\",\"hostPID\":false,\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"elasticsearch\",\"namespace\":\"garydemo-garyns\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]','onlineshop/elasticsearch',NULL,1,'garydemo','admin','2017-09-07 19:25:49',0,'HarmonyCloud_Status=C');
INSERT INTO `service_templates` (`id`,`name`,`tag`,`details`,`deployment_content`,`image_list`,`ingress_content`,`status`,`tenant`,`user`,`create_time`,`flag`,`node_selector`) VALUES (13,'vp0','1.0',NULL,'[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[\"-c\",\"sleep 10; peer node start\"],\"command\":[\"sh\"],\"configmap\":[],\"env\":[{\"key\":\"CORE_PEER_ID\",\"name\":\"\",\"value\":\"vp0\"},{\"key\":\"CORE_SECURITY_ENROLLID\",\"name\":\"\",\"value\":\"test_vp0\"},{\"key\":\"CORE_SECURITY_ENROLLSECRET\",\"name\":\"\",\"value\":\"MwYpmSRjupbT\"},{\"key\":\"CORE_PEER_ADDRESSAUTODETECT\",\"name\":\"\",\"value\":\"true\"},{\"key\":\"CORE_PEER_NETWORKID\",\"name\":\"\",\"value\":\"dev\"},{\"key\":\"CORE_LOGGING_LEVEL\",\"name\":\"\",\"value\":\"info\"},{\"key\":\"CORE_PEER_VALIDATOR_CONSENSUS_PLUGIN\",\"name\":\"\",\"value\":\"pbft\"},{\"key\":\"CORE_PBFT_GENERAL_N\",\"name\":\"\",\"value\":\"6\"},{\"key\":\"CORE_PBFT_GENERAL_TIMEOUT_REQUEST\",\"name\":\"\",\"value\":\"2s\"},{\"key\":\"CORE_SECURITY_ENABLED\",\"name\":\"\",\"value\":\"true\"},{\"key\":\"CORE_PEER_PKI_ECA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"},{\"key\":\"CORE_PEER_PKI_TCA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"},{\"key\":\"CORE_PEER_PKI_TLSCA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"}],\"img\":\"onlineshop/fabric-peer\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"vp0\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7050\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7051\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7053\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7052\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7054\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7055\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7056\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7057\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7058\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7059\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"500m\",\"memory\":\"512\"},\"storage\":[{\"emptyDir\":\"\",\"gitUrl\":\"\",\"hostPath\":\"/var/run/docker.sock\",\"path\":\"/var/run/docker.sock\",\"pvcBindOne\":\"true\",\"pvcCapacity\":\"\",\"pvcName\":\"\",\"pvcTenantid\":\"b04c83f0c7fd4914b2e7ddf2ef309a0b\",\"readOnly\":\"false\",\"revision\":\"\",\"type\":\"hostPath\",\"volume\":\"\"}],\"tag\":\"0.6\"}],\"hostName\":\"\",\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"vp0\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]','onlineshop/fabric-peer',NULL,1,'cfets','admin','2017-08-18 14:44:42',0,'HarmonyCloud_Status=C');
INSERT INTO `service_templates` (`id`,`name`,`tag`,`details`,`deployment_content`,`image_list`,`ingress_content`,`status`,`tenant`,`user`,`create_time`,`flag`,`node_selector`) VALUES (14,'vp1','1.0',NULL,'[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[\"-c\",\"sleep 10; peer node start\"],\"command\":[\"sh\"],\"configmap\":[],\"env\":[{\"key\":\"CORE_PEER_ID\",\"name\":\"\",\"value\":\"vp1\"},{\"key\":\"CORE_PEER_DISCOVERY_ROOTNODE\",\"name\":\"\",\"value\":\"vp0:7051\"},{\"key\":\"CORE_SECURITY_ENROLLID\",\"name\":\"\",\"value\":\"test_vp1\"},{\"key\":\"CORE_SECURITY_ENROLLSECRET\",\"name\":\"\",\"value\":\"5wgHK9qqYaPy\"},{\"key\":\"CORE_PEER_ADDRESSAUTODETECT\",\"name\":\"\",\"value\":\"true\"},{\"key\":\"CORE_PEER_NETWORKID\",\"name\":\"\",\"value\":\"dev\"},{\"key\":\"CORE_LOGGING_LEVEL\",\"name\":\"\",\"value\":\"info\"},{\"key\":\"CORE_PEER_VALIDATOR_CONSENSUS_PLUGIN\",\"name\":\"\",\"value\":\"pbft\"},{\"key\":\"CORE_PBFT_GENERAL_N\",\"name\":\"\",\"value\":\"6\"},{\"key\":\"CORE_PBFT_GENERAL_TIMEOUT_REQUEST\",\"name\":\"\",\"value\":\"2s\"},{\"key\":\"CORE_SECURITY_ENABLED\",\"name\":\"\",\"value\":\"true\"},{\"key\":\"CORE_PEER_PKI_ECA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"},{\"key\":\"CORE_PEER_PKI_TCA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"},{\"key\":\"CORE_PEER_PKI_TLSCA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"}],\"img\":\"onlineshop/fabric-peer\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"vp1\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7050\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7051\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7052\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7053\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7054\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7055\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7056\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7057\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7058\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7059\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"500m\",\"memory\":\"512\"},\"storage\":[{\"emptyDir\":\"\",\"gitUrl\":\"\",\"hostPath\":\"/var/run/docker.sock\",\"path\":\"/var/run/docker.sock\",\"pvcBindOne\":\"true\",\"pvcCapacity\":\"\",\"pvcName\":\"\",\"pvcTenantid\":\"b04c83f0c7fd4914b2e7ddf2ef309a0b\",\"readOnly\":\"false\",\"revision\":\"\",\"type\":\"hostPath\",\"volume\":\"\"}],\"tag\":\"0.6\"}],\"hostName\":\"\",\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"vp1\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]','onlineshop/fabric-peer',NULL,1,'cfets','admin','2017-08-18 14:44:42',0,'HarmonyCloud_Status=C');
INSERT INTO `service_templates` (`id`,`name`,`tag`,`details`,`deployment_content`,`image_list`,`ingress_content`,`status`,`tenant`,`user`,`create_time`,`flag`,`node_selector`) VALUES (15,'vp2','1.0',NULL,'[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[\"-c\",\"sleep 10; peer node start\"],\"command\":[\"sh\"],\"configmap\":[],\"env\":[{\"key\":\"CORE_PEER_ID\",\"name\":\"\",\"value\":\"vp2\"},{\"key\":\"CORE_PEER_DISCOVERY_ROOTNODE\",\"name\":\"\",\"value\":\"vp0:7051\"},{\"key\":\"CORE_SECURITY_ENROLLID\",\"name\":\"\",\"value\":\"test_vp2\"},{\"key\":\"CORE_SECURITY_ENROLLSECRET\",\"name\":\"\",\"value\":\"vQelbRvja7cJ\"},{\"key\":\"CORE_PEER_ADDRESSAUTODETECT\",\"name\":\"\",\"value\":\"true\"},{\"key\":\"CORE_PEER_NETWORKID\",\"name\":\"\",\"value\":\"dev\"},{\"key\":\"CORE_LOGGING_LEVEL\",\"name\":\"\",\"value\":\"info\"},{\"key\":\"CORE_PEER_VALIDATOR_CONSENSUS_PLUGIN\",\"name\":\"\",\"value\":\"pbft\"},{\"key\":\"CORE_PBFT_GENERAL_N\",\"name\":\"\",\"value\":\"6\"},{\"key\":\"CORE_PBFT_GENERAL_TIMEOUT_REQUEST\",\"name\":\"\",\"value\":\"2s\"},{\"key\":\"CORE_SECURITY_ENABLED\",\"name\":\"\",\"value\":\"true\"},{\"key\":\"CORE_PEER_PKI_ECA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"},{\"key\":\"CORE_PEER_PKI_TCA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"},{\"key\":\"CORE_PEER_PKI_TLSCA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"}],\"img\":\"onlineshop/fabric-peer\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"vp2\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7050\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7051\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7052\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7053\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7054\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7055\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7056\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7057\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7058\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7059\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"500m\",\"memory\":\"512\"},\"storage\":[{\"emptyDir\":\"\",\"gitUrl\":\"\",\"hostPath\":\"/var/run/docker.sock\",\"path\":\"/var/run/docker.sock\",\"pvcBindOne\":\"true\",\"pvcCapacity\":\"\",\"pvcName\":\"\",\"pvcTenantid\":\"b04c83f0c7fd4914b2e7ddf2ef309a0b\",\"readOnly\":\"false\",\"revision\":\"\",\"type\":\"hostPath\",\"volume\":\"\"}],\"tag\":\"0.6\"}],\"hostName\":\"\",\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"vp2\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]','onlineshop/fabric-peer',NULL,1,'cfets','admin','2017-08-18 14:44:42',0,'HarmonyCloud_Status=C');
INSERT INTO `service_templates` (`id`,`name`,`tag`,`details`,`deployment_content`,`image_list`,`ingress_content`,`status`,`tenant`,`user`,`create_time`,`flag`,`node_selector`) VALUES (16,'vp3','1.0',NULL,'[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[\"-c\",\"sleep 10; peer node start\"],\"command\":[\"sh\"],\"configmap\":[],\"env\":[{\"key\":\"CORE_PEER_ID\",\"name\":\"\",\"value\":\"vp3\"},{\"key\":\"CORE_PEER_DISCOVERY_ROOTNODE\",\"name\":\"\",\"value\":\"vp0:7051\"},{\"key\":\"CORE_SECURITY_ENROLLID\",\"name\":\"\",\"value\":\"test_vp3\"},{\"key\":\"CORE_SECURITY_ENROLLSECRET\",\"name\":\"\",\"value\":\"9LKqKH5peurL\"},{\"key\":\"CORE_PEER_ADDRESSAUTODETECT\",\"name\":\"\",\"value\":\"true\"},{\"key\":\"CORE_PEER_NETWORKID\",\"name\":\"\",\"value\":\"dev\"},{\"key\":\"CORE_LOGGING_LEVEL\",\"name\":\"\",\"value\":\"info\"},{\"key\":\"CORE_PEER_VALIDATOR_CONSENSUS_PLUGIN\",\"name\":\"\",\"value\":\"pbft\"},{\"key\":\"CORE_PBFT_GENERAL_N\",\"name\":\"\",\"value\":\"6\"},{\"key\":\"CORE_PBFT_GENERAL_TIMEOUT_REQUEST\",\"name\":\"\",\"value\":\"2s\"},{\"key\":\"CORE_SECURITY_ENABLED\",\"name\":\"\",\"value\":\"true\"},{\"key\":\"CORE_PEER_PKI_ECA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"},{\"key\":\"CORE_PEER_PKI_TCA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"},{\"key\":\"CORE_PEER_PKI_TLSCA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"}],\"img\":\"onlineshop/fabric-peer\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"vp3\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7050\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7051\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7052\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7053\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7054\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7055\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7056\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7057\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7058\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7059\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"500m\",\"memory\":\"512\"},\"storage\":[{\"emptyDir\":\"\",\"gitUrl\":\"\",\"hostPath\":\"/var/run/docker.sock\",\"path\":\"/var/run/docker.sock\",\"pvcBindOne\":\"true\",\"pvcCapacity\":\"\",\"pvcName\":\"\",\"pvcTenantid\":\"b04c83f0c7fd4914b2e7ddf2ef309a0b\",\"readOnly\":\"false\",\"revision\":\"\",\"type\":\"hostPath\",\"volume\":\"\"}],\"tag\":\"0.6\"}],\"hostName\":\"\",\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"vp3\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]','onlineshop/fabric-peer',NULL,1,'cfets','admin','2017-08-18 14:44:42',0,'HarmonyCloud_Status=C');
INSERT INTO `service_templates` (`id`,`name`,`tag`,`details`,`deployment_content`,`image_list`,`ingress_content`,`status`,`tenant`,`user`,`create_time`,`flag`,`node_selector`) VALUES (17,'vp4','1.0',NULL,'[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[\"-c\",\"sleep 10; peer node start\"],\"command\":[\"sh\"],\"configmap\":[],\"env\":[{\"key\":\"CORE_PEER_ID\",\"name\":\"\",\"value\":\"vp4\"},{\"key\":\"CORE_SECURITY_ENROLLID\",\"name\":\"\",\"value\":\"test_vp4\"},{\"key\":\"CORE_SECURITY_ENROLLSECRET\",\"name\":\"\",\"value\":\"Pqh90CEW5juZ\"},{\"key\":\"CORE_PEER_DISCOVERY_ROOTNODE\",\"name\":\"\",\"value\":\"vp0:7051\"},{\"key\":\"CORE_PEER_ADDRESSAUTODETECT\",\"name\":\"\",\"value\":\"true\"},{\"key\":\"CORE_PEER_NETWORKID\",\"name\":\"\",\"value\":\"dev\"},{\"key\":\"CORE_LOGGING_LEVEL\",\"name\":\"\",\"value\":\"info\"},{\"key\":\"CORE_PEER_VALIDATOR_CONSENSUS_PLUGIN\",\"name\":\"\",\"value\":\"pbft\"},{\"key\":\"CORE_PBFT_GENERAL_N\",\"name\":\"\",\"value\":\"6\"},{\"key\":\"CORE_PBFT_GENERAL_TIMEOUT_REQUEST\",\"name\":\"\",\"value\":\"2s\"},{\"key\":\"CORE_SECURITY_ENABLED\",\"name\":\"\",\"value\":\"true\"},{\"key\":\"CORE_PEER_PKI_ECA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"},{\"key\":\"CORE_PEER_PKI_TCA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"},{\"key\":\"CORE_PEER_PKI_TLSCA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"}],\"img\":\"onlineshop/fabric-peer\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"vp4\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7050\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7051\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7052\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7053\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7054\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7055\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7056\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7057\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7058\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7059\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"500m\",\"memory\":\"512\"},\"storage\":[{\"emptyDir\":\"\",\"gitUrl\":\"\",\"hostPath\":\"/var/run/docker.sock\",\"path\":\"/var/run/docker.sock\",\"pvcBindOne\":\"true\",\"pvcCapacity\":\"\",\"pvcName\":\"\",\"pvcTenantid\":\"b04c83f0c7fd4914b2e7ddf2ef309a0b\",\"readOnly\":\"false\",\"revision\":\"\",\"type\":\"hostPath\",\"volume\":\"\"}],\"tag\":\"0.6\"}],\"hostName\":\"\",\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"vp4\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]','onlineshop/fabric-peer',NULL,1,'cfets','admin','2017-08-18 14:44:42',0,'HarmonyCloud_Status=C');
INSERT INTO `service_templates` (`id`,`name`,`tag`,`details`,`deployment_content`,`image_list`,`ingress_content`,`status`,`tenant`,`user`,`create_time`,`flag`,`node_selector`) VALUES (18,'vp5','1.0',NULL,'[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[\"-c\",\"sleep 10; peer node start\"],\"command\":[\"sh\"],\"configmap\":[],\"env\":[{\"key\":\"CORE_PEER_PKI_ECA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"},{\"key\":\"CORE_PEER_PKI_TCA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"},{\"key\":\"CORE_PEER_PKI_TLSCA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"},{\"key\":\"CORE_PEER_ID\",\"name\":\"\",\"value\":\"vp5\"},{\"key\":\"CORE_SECURITY_ENROLLID\",\"name\":\"\",\"value\":\"test_vp5\"},{\"key\":\"CORE_PBFT_GENERAL_N\",\"name\":\"\",\"value\":\"6\"},{\"key\":\"CORE_SECURITY_ENROLLSECRET\",\"name\":\"\",\"value\":\"FfdvDkAdY81P\"},{\"key\":\"CORE_PEER_DISCOVERY_ROOTNODE\",\"name\":\"\",\"value\":\"vp0:7051\"},{\"key\":\"CORE_PEER_ADDRESSAUTODETECT\",\"name\":\"\",\"value\":\"true\"},{\"key\":\"CORE_PEER_NETWORKID\",\"name\":\"\",\"value\":\"dev\"},{\"key\":\"CORE_LOGGING_LEVEL\",\"name\":\"\",\"value\":\"info\"},{\"key\":\"CORE_PEER_VALIDATOR_CONSENSUS_PLUGIN\",\"name\":\"\",\"value\":\"pbft\"},{\"key\":\"CORE_PBFT_GENERAL_TIMEOUT_REQUEST\",\"name\":\"\",\"value\":\"2s\"},{\"key\":\"CORE_SECURITY_ENABLED\",\"name\":\"\",\"value\":\"true\"}],\"img\":\"onlineshop/fabric-peer\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"vp5\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7050\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7051\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7053\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7052\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7054\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7055\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7056\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7057\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7058\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7059\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"500m\",\"memory\":\"512\"},\"storage\":[{\"emptyDir\":\"\",\"gitUrl\":\"\",\"hostPath\":\"/var/run/docker.sock\",\"path\":\"/var/run/docker.sock\",\"pvcBindOne\":\"true\",\"pvcCapacity\":\"\",\"pvcName\":\"\",\"pvcTenantid\":\"b04c83f0c7fd4914b2e7ddf2ef309a0b\",\"readOnly\":\"false\",\"revision\":\"\",\"type\":\"hostPath\",\"volume\":\"\"}],\"tag\":\"0.6\"}],\"hostName\":\"\",\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"vp5\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]','onlineshop/fabric-peer',NULL,1,'cfets','admin','2017-08-18 14:44:42',0,'HarmonyCloud_Status=C');
INSERT INTO `service_templates` (`id`,`name`,`tag`,`details`,`deployment_content`,`image_list`,`ingress_content`,`status`,`tenant`,`user`,`create_time`,`flag`,`node_selector`) VALUES (19,'membersrvc','1.0',NULL,'[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[],\"command\":[\"membersrvc\"],\"configmap\":[],\"env\":[{\"key\":\"CORE_LOGGING_SERVER\",\"name\":\"\",\"value\":\"debug\"}],\"img\":\"onlineshop/fabric-membersrvc\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"membersrvc\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7054\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"500m\",\"memory\":\"512\"},\"storage\":[{\"emptyDir\":\"\",\"gitUrl\":\"\",\"hostPath\":\"/var/run/docker.sock\",\"path\":\"/var/run/docker.sock\",\"pvcBindOne\":\"true\",\"pvcCapacity\":\"\",\"pvcName\":\"\",\"pvcTenantid\":\"b04c83f0c7fd4914b2e7ddf2ef309a0b\",\"readOnly\":\"false\",\"revision\":\"\",\"type\":\"hostPath\",\"volume\":\"\"}],\"tag\":\"0.6\"}],\"hostName\":\"\",\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"membersrvc\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]','onlineshop/fabric-membersrvc',NULL,1,'cfets','admin','2017-08-18 14:44:42',0,'HarmonyCloud_Status=C');
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
  `id` int(100) NOT NULL AUTO_INCREMENT COMMENT '序号',
  `tenant_id` varchar(255) NOT NULL COMMENT '租户id',
  `tenant_name` varchar(255) NOT NULL COMMENT '租户名称',
  `tm_userNames` varchar(255) NOT NULL COMMENT 'tm id列表',
  `harbor_projects` varchar(200) DEFAULT NULL COMMENT 'harbor projects',
  `network_ids` varchar(100) DEFAULT NULL COMMENT '网络idl列表',
  `k8s_pvs` varchar(500) DEFAULT NULL COMMENT 'k8s pv资源',
  `k8s_namespaces` varchar(200) DEFAULT NULL COMMENT 'k8s namespace资源',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `annotation` varchar(255) DEFAULT NULL COMMENT '备注',
  `cluster_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `tenant_id_UNIQUE` (`tenant_id`),
  UNIQUE KEY `tenant_name_UNIQUE` (`tenant_name`)
) ENGINE=InnoDB AUTO_INCREMENT=279 DEFAULT CHARSET=utf8;
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
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `business_id` int(11) DEFAULT NULL,
  `source` varchar(45) CHARACTER SET latin1 DEFAULT NULL,
  `target` varchar(45) CHARACTER SET latin1 DEFAULT NULL,
  `details` varchar(250) CHARACTER SET latin1 DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1836 DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=utf8 AVG_ROW_LENGTH=910;
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
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=latin1;
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
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `tenantid` varchar(255) NOT NULL,
  `username` varchar(255) NOT NULL,
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `istm` int(11) DEFAULT '0',
  `role` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=423 DEFAULT CHARSET=utf8;
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
