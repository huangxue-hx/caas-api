DROP DATABASE IF EXISTS `k8s_auth_server`;
CREATE DATABASE `k8s_auth_server`;

use `k8s_auth_server`;
-- MySQL dump 10.13  Distrib 5.7.17, for Win64 (x86_64)
--
-- Host: 10.100.100.244    Database: k8s_auth_server
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
  `name` varchar(45) DEFAULT NULL,
  `details` varchar(250) DEFAULT NULL,
  `namespaces` varchar(45) DEFAULT NULL,
  `user` varchar(45) DEFAULT NULL,
  `create_time` timestamp NULL DEFAULT NULL,
  `tenant` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=125 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `business`
--

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
) ENGINE=InnoDB AUTO_INCREMENT=364 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `business_service`
--

INSERT INTO `business_service` (`id`, `business_id`, `service_id`, `status`, `is_external`) VALUES (1, 1, 1, 0, 0);
INSERT INTO `business_service` (`id`, `business_id`, `service_id`, `status`, `is_external`) VALUES (2, 2, 2, 0, 0);
INSERT INTO `business_service` (`id`, `business_id`, `service_id`, `status`, `is_external`) VALUES (3, 3, 3, 0, 0);
INSERT INTO `business_service` (`id`, `business_id`, `service_id`, `status`, `is_external`) VALUES (4, 4, 4, 0, 0);
INSERT INTO `business_service` (`id`, `business_id`, `service_id`, `status`, `is_external`) VALUES (5, 5, 5, 0, 0);
INSERT INTO `business_service` (`id`, `business_id`, `service_id`, `status`, `is_external`) VALUES (6, 6, 6, 0, 0);
INSERT INTO `business_service` (`id`, `business_id`, `service_id`, `status`, `is_external`) VALUES (7, 7, 7, 0, 0);
INSERT INTO `business_service` (`id`, `business_id`, `service_id`, `status`, `is_external`) VALUES (8, 8, 8, 0, 0);
INSERT INTO `business_service` (`id`, `business_id`, `service_id`, `status`, `is_external`) VALUES (9, 9, 9, 0, 0);
INSERT INTO `business_service` (`id`, `business_id`, `service_id`, `status`, `is_external`) VALUES (10, 10, 10, 0, 0);
INSERT INTO `business_service` (`id`, `business_id`, `service_id`, `status`, `is_external`) VALUES (11, 11, 11, 0, 0);
INSERT INTO `business_service` (`id`, `business_id`, `service_id`, `status`, `is_external`) VALUES (12, 12, 12, 0, 0);
INSERT INTO `business_service` (`id`, `business_id`, `service_id`, `status`, `is_external`) VALUES (13, 13, 13, 0, 0);
INSERT INTO `business_service` (`id`, `business_id`, `service_id`, `status`, `is_external`) VALUES (14, 13, 14, 0, 0);
INSERT INTO `business_service` (`id`, `business_id`, `service_id`, `status`, `is_external`) VALUES (15, 13, 15, 0, 0);
INSERT INTO `business_service` (`id`, `business_id`, `service_id`, `status`, `is_external`) VALUES (16, 13, 16, 0, 0);
INSERT INTO `business_service` (`id`, `business_id`, `service_id`, `status`, `is_external`) VALUES (17, 13, 17, 0, 0);
INSERT INTO `business_service` (`id`, `business_id`, `service_id`, `status`, `is_external`) VALUES (18, 13, 18, 0, 0);
INSERT INTO `business_service` (`id`, `business_id`, `service_id`, `status`, `is_external`) VALUES (19, 13, 19, 0, 0);

--
-- Table structure for table `business_templates`
--

DROP TABLE IF EXISTS `business_templates`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `business_templates` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) DEFAULT NULL,
  `tag` varchar(45) DEFAULT NULL,
  `details` varchar(512) DEFAULT NULL,
  `status` int(11) DEFAULT NULL,
  `tenant` varchar(45) DEFAULT NULL,
  `user` varchar(45) DEFAULT NULL,
  `create_time` timestamp NULL DEFAULT NULL,
  `update_time` timestamp NULL DEFAULT NULL,
  `is_deploy` int(11) DEFAULT NULL,
  `image_list` varchar(512) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=342 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;


INSERT INTO `business_templates` (`id`, `name`, `tag`, `details`, `status`, `tenant`, `user`, `create_time`, `update_time`, `is_deploy`, `image_list`) VALUES (1, 'Tomcat', '1.0', NULL, 0, 'all', 'admin', '2017-8-11 14:18:36', NULL, 1, 'library/tomcat');
INSERT INTO `business_templates` (`id`, `name`, `tag`, `details`, `status`, `tenant`, `user`, `create_time`, `update_time`, `is_deploy`, `image_list`) VALUES (2, 'OpenJDK', '1.0', NULL, 0, 'all', 'admin', '2017-8-11 14:28:04', NULL, 1, 'library/openjdk');
INSERT INTO `business_templates` (`id`, `name`, `tag`, `details`, `status`, `tenant`, `user`, `create_time`, `update_time`, `is_deploy`, `image_list`) VALUES (3, 'Redis', '1.0', NULL, 0, 'all', 'admin', '2017-8-11 14:44:14', NULL, NULL, 'library/redis');
INSERT INTO `business_templates` (`id`, `name`, `tag`, `details`, `status`, `tenant`, `user`, `create_time`, `update_time`, `is_deploy`, `image_list`) VALUES (4, 'WordPress', '1.0', NULL, 0, 'all', 'admin', '2017-8-11 15:31:54', NULL, NULL, 'library/wordpress');
INSERT INTO `business_templates` (`id`, `name`, `tag`, `details`, `status`, `tenant`, `user`, `create_time`, `update_time`, `is_deploy`, `image_list`) VALUES (5, 'Perl', '1.0', NULL, 0, 'all', 'admin', '2017-8-11 15:55:13', NULL, NULL, 'library/perl');
INSERT INTO `business_templates` (`id`, `name`, `tag`, `details`, `status`, `tenant`, `user`, `create_time`, `update_time`, `is_deploy`, `image_list`) VALUES (6, 'KafkaCluster', '1.0', NULL, 0, 'all', 'admin', '2017-8-11 16:01:15', NULL, NULL, 'library/kafka-cluster');
INSERT INTO `business_templates` (`id`, `name`, `tag`, `details`, `status`, `tenant`, `user`, `create_time`, `update_time`, `is_deploy`, `image_list`) VALUES (7, 'Kafka', '1.0', NULL, 0, 'all', 'admin', '2017-8-11 16:06:30', NULL, NULL, 'library/kafka');
INSERT INTO `business_templates` (`id`, `name`, `tag`, `details`, `status`, `tenant`, `user`, `create_time`, `update_time`, `is_deploy`, `image_list`) VALUES (8, 'InfluxDB', '1.0', NULL, 0, 'all', 'admin', '2017-8-11 16:12:19', NULL, NULL, 'library/influxdb');
INSERT INTO `business_templates` (`id`, `name`, `tag`, `details`, `status`, `tenant`, `user`, `create_time`, `update_time`, `is_deploy`, `image_list`) VALUES (9, 'InfluxDB2', '1.0', NULL, 0, 'all', 'admin', '2017-8-11 17:30:21', NULL, NULL, 'library/influxdb');
INSERT INTO `business_templates` (`id`, `name`, `tag`, `details`, `status`, `tenant`, `user`, `create_time`, `update_time`, `is_deploy`, `image_list`) VALUES (10, 'MySQL', '1.0', NULL, 0, 'all', 'admin', '2017-8-11 18:03:10', NULL, NULL, 'library/mysqls');
INSERT INTO `business_templates` (`id`, `name`, `tag`, `details`, `status`, `tenant`, `user`, `create_time`, `update_time`, `is_deploy`, `image_list`) VALUES (11, 'Nodejs-Alpine', '1.0', NULL, 0, 'all', 'admin', '2017-8-12 11:20:59', NULL, NULL, 'library/nodejs-alpine');
INSERT INTO `business_templates` (`id`, `name`, `tag`, `details`, `status`, `tenant`, `user`, `create_time`, `update_time`, `is_deploy`, `image_list`) VALUES (12, 'Webhook', '1.0', NULL, 0, 'all', 'admin', '2017-8-12 15:54:52', NULL, NULL, 'library/webhook');
INSERT INTO `business_templates` (`id`, `name`, `tag`, `details`, `status`, `tenant`, `user`, `create_time`, `update_time`, `is_deploy`, `image_list`) VALUES (13, 'Fabric0.6', '1.0', NULL, 0, 'all', 'admin', '2017-8-18 14:44:42', NULL, NULL, 'library/fabric-peer,library/fabric-membersrvc');

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
) ENGINE=InnoDB AUTO_INCREMENT=188 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;



--
-- Table structure for table `cluster_domain`
--

DROP TABLE IF EXISTS `cluster_domain`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cluster_domain` (
  `id` int(11) NOT NULL,
  `domain` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;


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
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;


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
INSERT INTO `external_type` VALUES (1,'all'),(2,'mysql'),(3,'oracle'),(4,'zookeeper'),(5,'storm'),(6,'flume'),(7,'redis'),(8,'memcached'),(9,'kafka'),(10,'other');
/*!40000 ALTER TABLE `external_type` ENABLE KEYS */;
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
) ENGINE=InnoDB AUTO_INCREMENT=46 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `harborProject_tenant`
--

LOCK TABLES `harborProject_tenant` WRITE;
/*!40000 ALTER TABLE `harborProject_tenant` DISABLE KEYS */;
INSERT INTO `harborProject_tenant` VALUES (26,1,'1234',NULL,NULL,'library',1);
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
) ENGINE=InnoDB AUTO_INCREMENT=54 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;


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
  `error_msg` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;



--
-- Table structure for table `job`
--

DROP TABLE IF EXISTS `job`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `job` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `job_name` varchar(50) DEFAULT NULL,
  `tenant` varchar(50) DEFAULT NULL,
  `project_type` varchar(10) DEFAULT NULL,
  `build_type` varchar(10) DEFAULT NULL,
  `repository_type` varchar(10) DEFAULT NULL,
  `repository_url` varchar(250) DEFAULT NULL,
  `repository_branch` varchar(50) DEFAULT NULL,
  `credentials_username` varchar(50) DEFAULT NULL,
  `credentials_password` varchar(50) DEFAULT NULL,
  `base_image` varchar(50) DEFAULT NULL,
  `image_name` varchar(50) DEFAULT NULL,
  `image_tag` varchar(20) DEFAULT NULL,
  `harbor_project` varchar(20) DEFAULT NULL,
  `create_user` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;



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
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;



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
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;


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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
  `nodeport` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=76 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;



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
) ENGINE=InnoDB AUTO_INCREMENT=63 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;



/*
Navicat MySQL Data Transfer

Source Server         : 10.10.101.143
Source Server Version : 50635
Source Host           : 10.10.101.143:30306
Source Database       : k8s_auth_server

Target Server Type    : MYSQL
Target Server Version : 50635
File Encoding         : 65001

Date: 2017-06-20 10:51:11
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for resource
-- ----------------------------
DROP TABLE IF EXISTS `resource`;
CREATE TABLE `resource` (
  `id` int(10) NOT NULL COMMENT '序号',
  `name` varchar(100) DEFAULT NULL COMMENT '资源名称',
  `type` varchar(10) DEFAULT 'menu' COMMENT '类型',
  `url` varchar(100) DEFAULT NULL COMMENT '资源路径',
  `parent_id` int(10) DEFAULT '0' COMMENT '父节点',
  `parent_ids` varchar(100) DEFAULT NULL COMMENT '父节点字符串',
  `weight` int(5) DEFAULT '0' COMMENT '权重',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `available` tinyint(1) DEFAULT '1' COMMENT '是否可用 1可用 0不可用',
  `trans_name` varchar(45) DEFAULT NULL COMMENT '译名',
  `icon_name` varchar(45) DEFAULT NULL COMMENT '图标名',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of resource
-- ----------------------------
INSERT INTO `resource` VALUES ('1', 'ALL', 'menu', '', '0', '0', '0', null, null, '1', null, null);
INSERT INTO `resource` VALUES ('2', '总览', 'menu', 'overview', '1', null, '0', null, null, '1', null, 'menu-icon mi-overview');
INSERT INTO `resource` VALUES ('3', '集群', 'menu', 'cluster', '1', null, '0', null, null, '1', null, 'menu-icon mi-cluster');
INSERT INTO `resource` VALUES ('4', '租户管理', 'menu', 'tenant', '1', null, '0', null, null, '1', null, 'menu-icon mi-tenant');
INSERT INTO `resource` VALUES ('5', '业务管理', 'menu', 'manage', '1', null, '0', null, null, '1', null, 'menu-icon mi-application');
INSERT INTO `resource` VALUES ('6', '代码构建', 'menu', 'construct', '1', null, '0', null, null, '1', null, 'menu-icon mi-create');
INSERT INTO `resource` VALUES ('7', '模板管理', 'menu', 'manageAll', '5', '5/7', '0', null, null, '1', null, 'hw_menulist_model');
INSERT INTO `resource` VALUES ('8', '应用管理', 'menu', 'manageApplyList', '5', '5/8', '0', null, null, '1', null, 'hw_menulist_model');
INSERT INTO `resource` VALUES ('9', '镜像仓库', 'menu', 'mirror', '1', null, '0', null, null, '1', null, 'menu-icon mi-mirror');
INSERT INTO `resource` VALUES ('10', '外部服务', 'menu', 'externalService', '1', null, '0', null, null, '1', null, 'menu-icon mi-service');
INSERT INTO `resource` VALUES ('11', '存储方案', 'menu', 'storageScheme', '1', null, '0', null, null, '1', null, 'menu-icon mi-storage');
INSERT INTO `resource` VALUES ('12', '配置中心', 'menu', 'configcenter', '1', null, '0', null, null, '1', null, 'menu-icon mi-config');
INSERT INTO `resource` VALUES ('13', '操作审计', 'menu', 'audit', '1', null, '0', null, null, '1', null, 'menu-icon mi-audit');
INSERT INTO `resource` VALUES ('14', '告警中心', 'menu', 'alarm', '1', null, '0', null, null, '1', null, 'menu-icon mi-alarm');
INSERT INTO `resource` VALUES ('1000', '我的租户', 'menu', 'tenant', '1', null, '0', null, null, '1', null, 'menu-icon mi-tenant');
INSERT INTO `resource` VALUES ('1001', '业务管理', 'menu', 'manage', '1', null, '0', null, null, '1', null, 'menu-icon mi-application');
INSERT INTO `resource` VALUES ('1002', '代码构建', 'menu', 'construct', '1', null, '0', null, null, '1', null, 'menu-icon mi-create');
INSERT INTO `resource` VALUES ('1003', '模板管理', 'menu', 'manageAll', '1001', '1001/1003', '0', null, null, '1', null, 'hw_menulist_model');
INSERT INTO `resource` VALUES ('1004', '应用管理', 'menu', 'manageApplyList', '1001', '1001/1004', '0', null, null, '1', null, 'hw_menulist_model');
INSERT INTO `resource` VALUES ('1005', '镜像仓库', 'menu', 'mirror', '1', null, '0', null, null, '1', null, 'menu-icon mi-mirror');
INSERT INTO `resource` VALUES ('1006', '外部服务', 'menu', 'externalService', '1', null, '0', null, null, '1', null, 'menu-icon mi-service');
INSERT INTO `resource` VALUES ('1007', '存储方案', 'menu', 'storageScheme', '1', null, '0', null, null, '1', null, 'menu-icon mi-storage');
INSERT INTO `resource` VALUES ('1008', '配置中心', 'menu', 'configcenter', '1', null, '0', null, null, '1', null, 'menu-icon mi-config');
INSERT INTO `resource` VALUES ('1009', '告警中心', 'menu', 'alarm', '1', null, '0', null, null, '1', null, 'menu-icon mi-alarm');


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
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_UNIQUE` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `role`
--

LOCK TABLES `role` WRITE;
/*!40000 ALTER TABLE `role` DISABLE KEYS */;
INSERT INTO `role` VALUES (1,'superadmin','','10000,11000,30000,31000,40000,41000,42000,43000,44000,44100,44200,50000,51000,52000,53000,54000,55000,60000,61000,61100,61200,61300,61400,61500,61600,61700,62000,62100,62200,62300,62400,62500,62600,63000,70000,71000',NULL,NULL,1),(2,'admin','','10000,11000,30000,31000,40000,41000,44000,44100,44200,50000,51000,52000,53000,54000,55000,60000,61000,61100,61200,61300,61400,61500,61600,61700,62000,62100,62200,62300,62400,62500,62600,63000,70000,71000',NULL,NULL,1),(3,'tm','','10000,11000,40000,42000',NULL,NULL,1),(4,'pm','','10000,12000,40000,43000',NULL,NULL,1),(5,'dev','','10000,12000,20000,21000,22000,23000,24000,24100,24200',NULL,NULL,1),(6,'tester','','10000,12000,20000,21000,22000,23000,24000,24100,24200',NULL,NULL,1);
/*!40000 ALTER TABLE `role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `service`
--

DROP TABLE IF EXISTS `service`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `service` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) DEFAULT NULL,
  `business_id` int(11) DEFAULT NULL,
  `service_template_id` int(11) DEFAULT NULL,
  `is_external` int(11) DEFAULT '0',
  `pvc` varchar(512) DEFAULT NULL,
  `ingress` varchar(512) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=210 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;



--
-- Table structure for table `service_templates`
--

DROP TABLE IF EXISTS `service_templates`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `service_templates` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) DEFAULT NULL,
  `tag` varchar(45) DEFAULT NULL,
  `details` varchar(256) DEFAULT NULL,
  `deployment_content` longtext,
  `image_list` varchar(512) DEFAULT NULL,
  `ingress_content` text,
  `status` int(11) DEFAULT NULL,
  `tenant` varchar(45) DEFAULT NULL,
  `user` varchar(45) DEFAULT NULL,
  `create_time` timestamp NULL DEFAULT NULL,
  `flag` int(11) DEFAULT '0',
  `node_selector` varchar(225) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=350 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;


INSERT INTO `service_templates` (`id`, `name`, `tag`, `details`, `deployment_content`, `image_list`, `ingress_content`, `status`, `tenant`, `user`, `create_time`, `flag`, `node_selector`) VALUES (1, 'tomcat', '1.0', NULL, '[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[],\"command\":[],\"configmap\":[],\"env\":[],\"img\":\"library/tomcat\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"tomcat\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"8080\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"500m\",\"memory\":\"512\"},\"storage\":[],\"tag\":\"v8.0\"}],\"hostName\":\"\",\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"tomcat\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]', 'library/tomcat', NULL, 1, 'gywtesttenant', 'admin', '2017-8-11 14:18:36', 0, 'HarmonyCloud_Status=C');
INSERT INTO `service_templates` (`id`, `name`, `tag`, `details`, `deployment_content`, `image_list`, `ingress_content`, `status`, `tenant`, `user`, `create_time`, `flag`, `node_selector`) VALUES (2, 'openjdk', '1.0', NULL, '[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[],\"command\":[],\"configmap\":[],\"env\":[],\"img\":\"library/openjdk\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"openjdk\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"3030\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"100m\",\"memory\":\"128\"},\"storage\":[],\"tag\":\"8.0\"}],\"hostName\":\"\",\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"openjdk\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]', 'library/openjdk', NULL, 1, 'gywtesttenant', 'admin', '2017-8-11 14:28:04', 0, 'HarmonyCloud_Status=C');
INSERT INTO `service_templates` (`id`, `name`, `tag`, `details`, `deployment_content`, `image_list`, `ingress_content`, `status`, `tenant`, `user`, `create_time`, `flag`, `node_selector`) VALUES (3, 'redis', '1.0', NULL, '[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[],\"command\":[],\"configmap\":[],\"env\":[],\"img\":\"library/redis\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"redis\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"6379\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"100m\",\"memory\":\"128\"},\"storage\":[],\"tag\":\"3.2-alpine\"}],\"hostName\":\"\",\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"redis\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]', 'library/redis', NULL, 1, 'gywtesttenant', 'admin', '2017-8-11 14:44:14', 0, 'HarmonyCloud_Status=C');
INSERT INTO `service_templates` (`id`, `name`, `tag`, `details`, `deployment_content`, `image_list`, `ingress_content`, `status`, `tenant`, `user`, `create_time`, `flag`, `node_selector`) VALUES (4, 'wordpress', '1.0', NULL, '[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[],\"command\":[],\"configmap\":[],\"env\":[],\"img\":\"library/wordpress\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"wordpress\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"80\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"500m\",\"memory\":\"512\"},\"storage\":[],\"tag\":\"4.8.0-php7.1-fpm-alpine\"}],\"hostName\":\"\",\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"wordpress\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]', 'library/wordpress', NULL, 1, 'gywtesttenant', 'admin', '2017-8-11 15:31:54', 0, 'HarmonyCloud_Status=C');
INSERT INTO `service_templates` (`id`, `name`, `tag`, `details`, `deployment_content`, `image_list`, `ingress_content`, `status`, `tenant`, `user`, `create_time`, `flag`, `node_selector`) VALUES (5, 'perl', '1.0', NULL, '[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[],\"command\":[],\"configmap\":[],\"env\":[],\"img\":\"library/perl\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"perl\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"4598\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"100m\",\"memory\":\"128\"},\"storage\":[],\"tag\":\"latest\"}],\"hostName\":\"\",\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"perl\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]', 'library/perl', NULL, 1, 'gywtesttenant', 'admin', '2017-8-11 15:55:13', 0, 'HarmonyCloud_Status=C');
INSERT INTO `service_templates` (`id`, `name`, `tag`, `details`, `deployment_content`, `image_list`, `ingress_content`, `status`, `tenant`, `user`, `create_time`, `flag`, `node_selector`) VALUES (6, 'kafkacluster', '1.0', NULL, '[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[],\"command\":[],\"configmap\":[],\"env\":[],\"img\":\"library/kafka-cluster\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"kafkacluster\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"80\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"500m\",\"memory\":\"512\"},\"storage\":[],\"tag\":\"0.9.0.1\"}],\"hostName\":\"\",\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"kafkacluster\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]', 'library/kafka-cluster', NULL, 1, 'gywtesttenant', 'admin', '2017-8-11 16:01:15', 0, 'HarmonyCloud_Status=C');
INSERT INTO `service_templates` (`id`, `name`, `tag`, `details`, `deployment_content`, `image_list`, `ingress_content`, `status`, `tenant`, `user`, `create_time`, `flag`, `node_selector`) VALUES (7, 'kafka', '1.0', NULL, '[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[],\"command\":[],\"configmap\":[],\"env\":[],\"img\":\"library/kafka\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"kafka\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"80\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"500m\",\"memory\":\"512\"},\"storage\":[],\"tag\":\"0.11.0.0\"}],\"hostName\":\"\",\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"kafka\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]', 'library/kafka', NULL, 1, 'gywtesttenant', 'admin', '2017-8-11 16:06:30', 0, 'HarmonyCloud_Status=C');
INSERT INTO `service_templates` (`id`, `name`, `tag`, `details`, `deployment_content`, `image_list`, `ingress_content`, `status`, `tenant`, `user`, `create_time`, `flag`, `node_selector`) VALUES (8, 'influxdb', '1.0', NULL, '[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[],\"command\":[],\"configmap\":[],\"env\":[],\"img\":\"library/influxdb\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"influxdb\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"8099\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"500m\",\"memory\":\"512\"},\"storage\":[],\"tag\":\"v1.3.0\"}],\"hostName\":\"\",\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"influxdb\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]', 'library/influxdb', NULL, 1, 'gywtesttenant', 'admin', '2017-8-11 16:12:19', 0, 'HarmonyCloud_Status=C');
INSERT INTO `service_templates` (`id`, `name`, `tag`, `details`, `deployment_content`, `image_list`, `ingress_content`, `status`, `tenant`, `user`, `create_time`, `flag`, `node_selector`) VALUES (9, 'influxdb2', '1.0', NULL, '[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[],\"command\":[],\"configmap\":[],\"env\":[],\"img\":\"library/influxdb\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"influxdb2\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"8086\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"500m\",\"memory\":\"512\"},\"storage\":[],\"tag\":\"v1.3.0\"}],\"hostName\":\"\",\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"influxdb2\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]', 'library/influxdb', NULL, 1, 'gywtesttenant', 'admin', '2017-8-11 17:30:21', 0, 'HarmonyCloud_Status=C');
INSERT INTO `service_templates` (`id`, `name`, `tag`, `details`, `deployment_content`, `image_list`, `ingress_content`, `status`, `tenant`, `user`, `create_time`, `flag`, `node_selector`) VALUES (10, 'mysql', '1.0', NULL, '[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[],\"command\":[],\"configmap\":[],\"env\":[],\"img\":\"library/mysqls\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"mysql\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"1433\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"1000m\",\"memory\":\"1024\"},\"storage\":[],\"tag\":\"v1\"}],\"hostName\":\"\",\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"mysql\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]', 'library/mysqls', NULL, 1, 'gywtesttenant', 'admin', '2017-8-11 18:03:10', 0, 'HarmonyCloud_Status=C');
INSERT INTO `service_templates` (`id`, `name`, `tag`, `details`, `deployment_content`, `image_list`, `ingress_content`, `status`, `tenant`, `user`, `create_time`, `flag`, `node_selector`) VALUES (11, 'nodejs', '1.0', NULL, '[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[],\"command\":[],\"configmap\":[],\"env\":[],\"img\":\"library/nodejs-alpine\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"nodejs\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"30000\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"1000m\",\"memory\":\"1024\"},\"storage\":[],\"tag\":\"v1.0\"}],\"hostName\":\"\",\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"nodejs\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]', 'library/nodejs-alpine', NULL, 1, 'gywtesttenant', 'admin', '2017-8-12 11:20:59', 0, 'HarmonyCloud_Status=C');
INSERT INTO `service_templates` (`id`, `name`, `tag`, `details`, `deployment_content`, `image_list`, `ingress_content`, `status`, `tenant`, `user`, `create_time`, `flag`, `node_selector`) VALUES (12, 'webhook', '1.0', NULL, '[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[],\"command\":[],\"configmap\":[],\"env\":[],\"img\":\"library/webhook\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"webhook\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"1433\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"1000m\",\"memory\":\"1024\"},\"storage\":[],\"tag\":\"2.6.5\"}],\"hostName\":\"\",\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"webhook\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]', 'library/webhook', NULL, 1, 'gywtesttenant', 'admin', '2017-8-12 15:54:52', 0, 'HarmonyCloud_Status=C');
INSERT INTO `service_templates` (`id`, `name`, `tag`, `details`, `deployment_content`, `image_list`, `ingress_content`, `status`, `tenant`, `user`, `create_time`, `flag`, `node_selector`) VALUES (13, 'vp0', '1.0', NULL, '[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[\"-c\",\"sleep 10; peer node start\"],\"command\":[\"sh\"],\"configmap\":[],\"env\":[{\"key\":\"CORE_PEER_ID\",\"name\":\"\",\"value\":\"vp0\"},{\"key\":\"CORE_SECURITY_ENROLLID\",\"name\":\"\",\"value\":\"test_vp0\"},{\"key\":\"CORE_SECURITY_ENROLLSECRET\",\"name\":\"\",\"value\":\"MwYpmSRjupbT\"},{\"key\":\"CORE_PEER_ADDRESSAUTODETECT\",\"name\":\"\",\"value\":\"true\"},{\"key\":\"CORE_PEER_NETWORKID\",\"name\":\"\",\"value\":\"dev\"},{\"key\":\"CORE_LOGGING_LEVEL\",\"name\":\"\",\"value\":\"info\"},{\"key\":\"CORE_PEER_VALIDATOR_CONSENSUS_PLUGIN\",\"name\":\"\",\"value\":\"pbft\"},{\"key\":\"CORE_PBFT_GENERAL_N\",\"name\":\"\",\"value\":\"6\"},{\"key\":\"CORE_PBFT_GENERAL_TIMEOUT_REQUEST\",\"name\":\"\",\"value\":\"2s\"},{\"key\":\"CORE_SECURITY_ENABLED\",\"name\":\"\",\"value\":\"true\"},{\"key\":\"CORE_PEER_PKI_ECA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"},{\"key\":\"CORE_PEER_PKI_TCA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"},{\"key\":\"CORE_PEER_PKI_TLSCA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"}],\"img\":\"library/fabric-peer\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"vp0\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7050\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7051\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7053\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7052\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7054\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7055\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7056\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7057\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7058\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7059\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"500m\",\"memory\":\"512\"},\"storage\":[{\"emptyDir\":\"\",\"gitUrl\":\"\",\"hostPath\":\"/var/run/docker.sock\",\"path\":\"/var/run/docker.sock\",\"pvcBindOne\":\"true\",\"pvcCapacity\":\"\",\"pvcName\":\"\",\"pvcTenantid\":\"b04c83f0c7fd4914b2e7ddf2ef309a0b\",\"readOnly\":\"false\",\"revision\":\"\",\"type\":\"hostPath\",\"volume\":\"\"}],\"tag\":\"0.6\"}],\"hostName\":\"\",\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"vp0\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]', 'library/fabric-peer', NULL, 1, 'cfets', 'admin', '2017-8-18 14:44:42', 0, 'HarmonyCloud_Status=C');
INSERT INTO `service_templates` (`id`, `name`, `tag`, `details`, `deployment_content`, `image_list`, `ingress_content`, `status`, `tenant`, `user`, `create_time`, `flag`, `node_selector`) VALUES (14, 'vp1', '1.0', NULL, '[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[\"-c\",\"sleep 10; peer node start\"],\"command\":[\"sh\"],\"configmap\":[],\"env\":[{\"key\":\"CORE_PEER_ID\",\"name\":\"\",\"value\":\"vp1\"},{\"key\":\"CORE_PEER_DISCOVERY_ROOTNODE\",\"name\":\"\",\"value\":\"vp0:7051\"},{\"key\":\"CORE_SECURITY_ENROLLID\",\"name\":\"\",\"value\":\"test_vp1\"},{\"key\":\"CORE_SECURITY_ENROLLSECRET\",\"name\":\"\",\"value\":\"5wgHK9qqYaPy\"},{\"key\":\"CORE_PEER_ADDRESSAUTODETECT\",\"name\":\"\",\"value\":\"true\"},{\"key\":\"CORE_PEER_NETWORKID\",\"name\":\"\",\"value\":\"dev\"},{\"key\":\"CORE_LOGGING_LEVEL\",\"name\":\"\",\"value\":\"info\"},{\"key\":\"CORE_PEER_VALIDATOR_CONSENSUS_PLUGIN\",\"name\":\"\",\"value\":\"pbft\"},{\"key\":\"CORE_PBFT_GENERAL_N\",\"name\":\"\",\"value\":\"6\"},{\"key\":\"CORE_PBFT_GENERAL_TIMEOUT_REQUEST\",\"name\":\"\",\"value\":\"2s\"},{\"key\":\"CORE_SECURITY_ENABLED\",\"name\":\"\",\"value\":\"true\"},{\"key\":\"CORE_PEER_PKI_ECA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"},{\"key\":\"CORE_PEER_PKI_TCA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"},{\"key\":\"CORE_PEER_PKI_TLSCA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"}],\"img\":\"library/fabric-peer\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"vp1\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7050\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7051\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7052\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7053\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7054\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7055\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7056\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7057\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7058\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7059\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"500m\",\"memory\":\"512\"},\"storage\":[{\"emptyDir\":\"\",\"gitUrl\":\"\",\"hostPath\":\"/var/run/docker.sock\",\"path\":\"/var/run/docker.sock\",\"pvcBindOne\":\"true\",\"pvcCapacity\":\"\",\"pvcName\":\"\",\"pvcTenantid\":\"b04c83f0c7fd4914b2e7ddf2ef309a0b\",\"readOnly\":\"false\",\"revision\":\"\",\"type\":\"hostPath\",\"volume\":\"\"}],\"tag\":\"0.6\"}],\"hostName\":\"\",\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"vp1\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]', 'library/fabric-peer', NULL, 1, 'cfets', 'admin', '2017-8-18 14:44:42', 0, 'HarmonyCloud_Status=C');
INSERT INTO `service_templates` (`id`, `name`, `tag`, `details`, `deployment_content`, `image_list`, `ingress_content`, `status`, `tenant`, `user`, `create_time`, `flag`, `node_selector`) VALUES (15, 'vp2', '1.0', NULL, '[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[\"-c\",\"sleep 10; peer node start\"],\"command\":[\"sh\"],\"configmap\":[],\"env\":[{\"key\":\"CORE_PEER_ID\",\"name\":\"\",\"value\":\"vp2\"},{\"key\":\"CORE_PEER_DISCOVERY_ROOTNODE\",\"name\":\"\",\"value\":\"vp0:7051\"},{\"key\":\"CORE_SECURITY_ENROLLID\",\"name\":\"\",\"value\":\"test_vp2\"},{\"key\":\"CORE_SECURITY_ENROLLSECRET\",\"name\":\"\",\"value\":\"vQelbRvja7cJ\"},{\"key\":\"CORE_PEER_ADDRESSAUTODETECT\",\"name\":\"\",\"value\":\"true\"},{\"key\":\"CORE_PEER_NETWORKID\",\"name\":\"\",\"value\":\"dev\"},{\"key\":\"CORE_LOGGING_LEVEL\",\"name\":\"\",\"value\":\"info\"},{\"key\":\"CORE_PEER_VALIDATOR_CONSENSUS_PLUGIN\",\"name\":\"\",\"value\":\"pbft\"},{\"key\":\"CORE_PBFT_GENERAL_N\",\"name\":\"\",\"value\":\"6\"},{\"key\":\"CORE_PBFT_GENERAL_TIMEOUT_REQUEST\",\"name\":\"\",\"value\":\"2s\"},{\"key\":\"CORE_SECURITY_ENABLED\",\"name\":\"\",\"value\":\"true\"},{\"key\":\"CORE_PEER_PKI_ECA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"},{\"key\":\"CORE_PEER_PKI_TCA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"},{\"key\":\"CORE_PEER_PKI_TLSCA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"}],\"img\":\"library/fabric-peer\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"vp2\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7050\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7051\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7052\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7053\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7054\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7055\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7056\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7057\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7058\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7059\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"500m\",\"memory\":\"512\"},\"storage\":[{\"emptyDir\":\"\",\"gitUrl\":\"\",\"hostPath\":\"/var/run/docker.sock\",\"path\":\"/var/run/docker.sock\",\"pvcBindOne\":\"true\",\"pvcCapacity\":\"\",\"pvcName\":\"\",\"pvcTenantid\":\"b04c83f0c7fd4914b2e7ddf2ef309a0b\",\"readOnly\":\"false\",\"revision\":\"\",\"type\":\"hostPath\",\"volume\":\"\"}],\"tag\":\"0.6\"}],\"hostName\":\"\",\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"vp2\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]', 'library/fabric-peer', NULL, 1, 'cfets', 'admin', '2017-8-18 14:44:42', 0, 'HarmonyCloud_Status=C');
INSERT INTO `service_templates` (`id`, `name`, `tag`, `details`, `deployment_content`, `image_list`, `ingress_content`, `status`, `tenant`, `user`, `create_time`, `flag`, `node_selector`) VALUES (16, 'vp3', '1.0', NULL, '[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[\"-c\",\"sleep 10; peer node start\"],\"command\":[\"sh\"],\"configmap\":[],\"env\":[{\"key\":\"CORE_PEER_ID\",\"name\":\"\",\"value\":\"vp3\"},{\"key\":\"CORE_PEER_DISCOVERY_ROOTNODE\",\"name\":\"\",\"value\":\"vp0:7051\"},{\"key\":\"CORE_SECURITY_ENROLLID\",\"name\":\"\",\"value\":\"test_vp3\"},{\"key\":\"CORE_SECURITY_ENROLLSECRET\",\"name\":\"\",\"value\":\"9LKqKH5peurL\"},{\"key\":\"CORE_PEER_ADDRESSAUTODETECT\",\"name\":\"\",\"value\":\"true\"},{\"key\":\"CORE_PEER_NETWORKID\",\"name\":\"\",\"value\":\"dev\"},{\"key\":\"CORE_LOGGING_LEVEL\",\"name\":\"\",\"value\":\"info\"},{\"key\":\"CORE_PEER_VALIDATOR_CONSENSUS_PLUGIN\",\"name\":\"\",\"value\":\"pbft\"},{\"key\":\"CORE_PBFT_GENERAL_N\",\"name\":\"\",\"value\":\"6\"},{\"key\":\"CORE_PBFT_GENERAL_TIMEOUT_REQUEST\",\"name\":\"\",\"value\":\"2s\"},{\"key\":\"CORE_SECURITY_ENABLED\",\"name\":\"\",\"value\":\"true\"},{\"key\":\"CORE_PEER_PKI_ECA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"},{\"key\":\"CORE_PEER_PKI_TCA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"},{\"key\":\"CORE_PEER_PKI_TLSCA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"}],\"img\":\"library/fabric-peer\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"vp3\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7050\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7051\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7052\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7053\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7054\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7055\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7056\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7057\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7058\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7059\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"500m\",\"memory\":\"512\"},\"storage\":[{\"emptyDir\":\"\",\"gitUrl\":\"\",\"hostPath\":\"/var/run/docker.sock\",\"path\":\"/var/run/docker.sock\",\"pvcBindOne\":\"true\",\"pvcCapacity\":\"\",\"pvcName\":\"\",\"pvcTenantid\":\"b04c83f0c7fd4914b2e7ddf2ef309a0b\",\"readOnly\":\"false\",\"revision\":\"\",\"type\":\"hostPath\",\"volume\":\"\"}],\"tag\":\"0.6\"}],\"hostName\":\"\",\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"vp3\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]', 'library/fabric-peer', NULL, 1, 'cfets', 'admin', '2017-8-18 14:44:42', 0, 'HarmonyCloud_Status=C');
INSERT INTO `service_templates` (`id`, `name`, `tag`, `details`, `deployment_content`, `image_list`, `ingress_content`, `status`, `tenant`, `user`, `create_time`, `flag`, `node_selector`) VALUES (17, 'vp4', '1.0', NULL, '[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[\"-c\",\"sleep 10; peer node start\"],\"command\":[\"sh\"],\"configmap\":[],\"env\":[{\"key\":\"CORE_PEER_ID\",\"name\":\"\",\"value\":\"vp4\"},{\"key\":\"CORE_SECURITY_ENROLLID\",\"name\":\"\",\"value\":\"test_vp4\"},{\"key\":\"CORE_SECURITY_ENROLLSECRET\",\"name\":\"\",\"value\":\"Pqh90CEW5juZ\"},{\"key\":\"CORE_PEER_DISCOVERY_ROOTNODE\",\"name\":\"\",\"value\":\"vp0:7051\"},{\"key\":\"CORE_PEER_ADDRESSAUTODETECT\",\"name\":\"\",\"value\":\"true\"},{\"key\":\"CORE_PEER_NETWORKID\",\"name\":\"\",\"value\":\"dev\"},{\"key\":\"CORE_LOGGING_LEVEL\",\"name\":\"\",\"value\":\"info\"},{\"key\":\"CORE_PEER_VALIDATOR_CONSENSUS_PLUGIN\",\"name\":\"\",\"value\":\"pbft\"},{\"key\":\"CORE_PBFT_GENERAL_N\",\"name\":\"\",\"value\":\"6\"},{\"key\":\"CORE_PBFT_GENERAL_TIMEOUT_REQUEST\",\"name\":\"\",\"value\":\"2s\"},{\"key\":\"CORE_SECURITY_ENABLED\",\"name\":\"\",\"value\":\"true\"},{\"key\":\"CORE_PEER_PKI_ECA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"},{\"key\":\"CORE_PEER_PKI_TCA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"},{\"key\":\"CORE_PEER_PKI_TLSCA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"}],\"img\":\"library/fabric-peer\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"vp4\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7050\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7051\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7052\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7053\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7054\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7055\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7056\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7057\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7058\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7059\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"500m\",\"memory\":\"512\"},\"storage\":[{\"emptyDir\":\"\",\"gitUrl\":\"\",\"hostPath\":\"/var/run/docker.sock\",\"path\":\"/var/run/docker.sock\",\"pvcBindOne\":\"true\",\"pvcCapacity\":\"\",\"pvcName\":\"\",\"pvcTenantid\":\"b04c83f0c7fd4914b2e7ddf2ef309a0b\",\"readOnly\":\"false\",\"revision\":\"\",\"type\":\"hostPath\",\"volume\":\"\"}],\"tag\":\"0.6\"}],\"hostName\":\"\",\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"vp4\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]', 'library/fabric-peer', NULL, 1, 'cfets', 'admin', '2017-8-18 14:44:42', 0, 'HarmonyCloud_Status=C');
INSERT INTO `service_templates` (`id`, `name`, `tag`, `details`, `deployment_content`, `image_list`, `ingress_content`, `status`, `tenant`, `user`, `create_time`, `flag`, `node_selector`) VALUES (18, 'vp5', '1.0', NULL, '[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[\"-c\",\"sleep 10; peer node start\"],\"command\":[\"sh\"],\"configmap\":[],\"env\":[{\"key\":\"CORE_PEER_PKI_ECA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"},{\"key\":\"CORE_PEER_PKI_TCA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"},{\"key\":\"CORE_PEER_PKI_TLSCA_PADDR\",\"name\":\"\",\"value\":\"membersrvc:7054\"},{\"key\":\"CORE_PEER_ID\",\"name\":\"\",\"value\":\"vp5\"},{\"key\":\"CORE_SECURITY_ENROLLID\",\"name\":\"\",\"value\":\"test_vp5\"},{\"key\":\"CORE_PBFT_GENERAL_N\",\"name\":\"\",\"value\":\"6\"},{\"key\":\"CORE_SECURITY_ENROLLSECRET\",\"name\":\"\",\"value\":\"FfdvDkAdY81P\"},{\"key\":\"CORE_PEER_DISCOVERY_ROOTNODE\",\"name\":\"\",\"value\":\"vp0:7051\"},{\"key\":\"CORE_PEER_ADDRESSAUTODETECT\",\"name\":\"\",\"value\":\"true\"},{\"key\":\"CORE_PEER_NETWORKID\",\"name\":\"\",\"value\":\"dev\"},{\"key\":\"CORE_LOGGING_LEVEL\",\"name\":\"\",\"value\":\"info\"},{\"key\":\"CORE_PEER_VALIDATOR_CONSENSUS_PLUGIN\",\"name\":\"\",\"value\":\"pbft\"},{\"key\":\"CORE_PBFT_GENERAL_TIMEOUT_REQUEST\",\"name\":\"\",\"value\":\"2s\"},{\"key\":\"CORE_SECURITY_ENABLED\",\"name\":\"\",\"value\":\"true\"}],\"img\":\"library/fabric-peer\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"vp5\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7050\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7051\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7053\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7052\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7054\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7055\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7056\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7057\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7058\",\"protocol\":\"TCP\"},{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7059\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"500m\",\"memory\":\"512\"},\"storage\":[{\"emptyDir\":\"\",\"gitUrl\":\"\",\"hostPath\":\"/var/run/docker.sock\",\"path\":\"/var/run/docker.sock\",\"pvcBindOne\":\"true\",\"pvcCapacity\":\"\",\"pvcName\":\"\",\"pvcTenantid\":\"b04c83f0c7fd4914b2e7ddf2ef309a0b\",\"readOnly\":\"false\",\"revision\":\"\",\"type\":\"hostPath\",\"volume\":\"\"}],\"tag\":\"0.6\"}],\"hostName\":\"\",\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"vp5\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]', 'library/fabric-peer', NULL, 1, 'cfets', 'admin', '2017-8-18 14:44:42', 0, 'HarmonyCloud_Status=C');
INSERT INTO `service_templates` (`id`, `name`, `tag`, `details`, `deployment_content`, `image_list`, `ingress_content`, `status`, `tenant`, `user`, `create_time`, `flag`, `node_selector`) VALUES (19, 'membersrvc', '1.0', NULL, '[{\"annotation\":\"\",\"clusterIP\":\"\",\"containers\":[{\"args\":[],\"command\":[\"membersrvc\"],\"configmap\":[],\"env\":[{\"key\":\"CORE_LOGGING_SERVER\",\"name\":\"\",\"value\":\"debug\"}],\"img\":\"library/fabric-membersrvc\",\"livenessProbe\":null,\"log\":\"\",\"name\":\"membersrvc\",\"ports\":[{\"containerPort\":\"\",\"expose\":\"true\",\"port\":\"7054\",\"protocol\":\"TCP\"}],\"readinessProbe\":null,\"resource\":{\"cpu\":\"500m\",\"memory\":\"512\"},\"storage\":[{\"emptyDir\":\"\",\"gitUrl\":\"\",\"hostPath\":\"/var/run/docker.sock\",\"path\":\"/var/run/docker.sock\",\"pvcBindOne\":\"true\",\"pvcCapacity\":\"\",\"pvcName\":\"\",\"pvcTenantid\":\"b04c83f0c7fd4914b2e7ddf2ef309a0b\",\"readOnly\":\"false\",\"revision\":\"\",\"type\":\"hostPath\",\"volume\":\"\"}],\"tag\":\"0.6\"}],\"hostName\":\"\",\"instance\":\"1\",\"labels\":\"\",\"logPath\":\"\",\"logService\":\"\",\"name\":\"membersrvc\",\"namespace\":\"\",\"nodeSelector\":\"HarmonyCloud_Status=C\",\"restartPolicy\":\"Always\",\"sessionAffinity\":\"\"}]', 'library/fabric-membersrvc', NULL, 1, 'cfets', 'admin', '2017-8-18 14:44:42', 0, 'HarmonyCloud_Status=C');

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
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;



--
-- Table structure for table `topology`
--

DROP TABLE IF EXISTS `topology`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `topology` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `business_id` int(11) DEFAULT NULL,
  `source` varchar(45) DEFAULT NULL,
  `target` varchar(45) DEFAULT NULL,
  `details` varchar(250) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=96 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;


INSERT INTO `topology` (`id`, `business_id`, `source`, `target`, `details`) VALUES (1, 13, '13', '14', NULL);
INSERT INTO `topology` (`id`, `business_id`, `source`, `target`, `details`) VALUES (2, 13, '13', '15', NULL);
INSERT INTO `topology` (`id`, `business_id`, `source`, `target`, `details`) VALUES (3, 13, '13', '17', NULL);
INSERT INTO `topology` (`id`, `business_id`, `source`, `target`, `details`) VALUES (4, 13, '13', '18', NULL);
INSERT INTO `topology` (`id`, `business_id`, `source`, `target`, `details`) VALUES (5, 13, '13', '19', NULL);
INSERT INTO `topology` (`id`, `business_id`, `source`, `target`, `details`) VALUES (6, 13, '14', '13', NULL);
INSERT INTO `topology` (`id`, `business_id`, `source`, `target`, `details`) VALUES (7, 13, '14', '15', NULL);
INSERT INTO `topology` (`id`, `business_id`, `source`, `target`, `details`) VALUES (8, 13, '14', '16', NULL);
INSERT INTO `topology` (`id`, `business_id`, `source`, `target`, `details`) VALUES (9, 13, '14', '18', NULL);
INSERT INTO `topology` (`id`, `business_id`, `source`, `target`, `details`) VALUES (10, 13, '15', '13', NULL);
INSERT INTO `topology` (`id`, `business_id`, `source`, `target`, `details`) VALUES (11, 13, '15', '14', NULL);
INSERT INTO `topology` (`id`, `business_id`, `source`, `target`, `details`) VALUES (12, 13, '15', '17', NULL);
INSERT INTO `topology` (`id`, `business_id`, `source`, `target`, `details`) VALUES (13, 13, '15', '18', NULL);
INSERT INTO `topology` (`id`, `business_id`, `source`, `target`, `details`) VALUES (14, 13, '16', '13', NULL);
INSERT INTO `topology` (`id`, `business_id`, `source`, `target`, `details`) VALUES (15, 13, '16', '14', NULL);
INSERT INTO `topology` (`id`, `business_id`, `source`, `target`, `details`) VALUES (16, 13, '16', '15', NULL);
INSERT INTO `topology` (`id`, `business_id`, `source`, `target`, `details`) VALUES (17, 13, '16', '17', NULL);
INSERT INTO `topology` (`id`, `business_id`, `source`, `target`, `details`) VALUES (18, 13, '16', '18', NULL);
INSERT INTO `topology` (`id`, `business_id`, `source`, `target`, `details`) VALUES (19, 13, '17', '13', NULL);
INSERT INTO `topology` (`id`, `business_id`, `source`, `target`, `details`) VALUES (20, 13, '17', '14', NULL);
INSERT INTO `topology` (`id`, `business_id`, `source`, `target`, `details`) VALUES (21, 13, '17', '15', NULL);
INSERT INTO `topology` (`id`, `business_id`, `source`, `target`, `details`) VALUES (22, 13, '17', '16', NULL);
INSERT INTO `topology` (`id`, `business_id`, `source`, `target`, `details`) VALUES (23, 13, '17', '18', NULL);
INSERT INTO `topology` (`id`, `business_id`, `source`, `target`, `details`) VALUES (24, 13, '18', '13', NULL);
INSERT INTO `topology` (`id`, `business_id`, `source`, `target`, `details`) VALUES (25, 13, '18', '14', NULL);
INSERT INTO `topology` (`id`, `business_id`, `source`, `target`, `details`) VALUES (26, 13, '18', '15', NULL);
INSERT INTO `topology` (`id`, `business_id`, `source`, `target`, `details`) VALUES (28, 13, '18', '16', NULL);
INSERT INTO `topology` (`id`, `business_id`, `source`, `target`, `details`) VALUES (27, 13, '18', '17', NULL);


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
  PRIMARY KEY (`uuid`),
  UNIQUE KEY `username_UNIQUE` (`username`),
  UNIQUE KEY `email_UNIQUE` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=155003 DEFAULT CHARSET=utf8 AVG_ROW_LENGTH=910;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'admin','A0A475CF454CF9A06979034098167B9E','330957b867a3462ea457bec41410624b',NULL,'2016-11-03 14:48:37',1,NULL,NULL,NULL,0,NULL,'normal'),(15500,'kube','E10ADC3949BA59ABBE56E057F20F883E','4b2abe00454b4399a2fce41215bff499','2016-10-18 17:33:52','2016-11-02 16:54:03',0,NULL,NULL,NULL,1,NULL,'normal'),(155002,'machine_front','A0A475CF454CF9A06979034098167B9E','ee97937bce71432e8c5b0e3dda0f2b1f','2016-10-18 17:33:52','2016-11-02 16:54:03',0,NULL,NULL,NULL,1,NULL,'normal');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
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
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=47 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;


/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2017-06-09 15:22:37
/*
Navicat MySQL Data Transfer

Source Server         : 10.10.101.143
Source Server Version : 50635
Source Host           : 10.10.101.143:30306
Source Database       : k8s_auth_server

Target Server Type    : MYSQL
Target Server Version : 50635
File Encoding         : 65001

Date: 2017-07-07 14:21:27
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for message
-- ----------------------------
DROP TABLE IF EXISTS `message`;
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

-- ----------------------------
-- Records of message
-- ----------------------------
INSERT INTO `message` VALUES ('1', 'e28545b26aa3fa384d07924d9164cc85', 'https://api.netease.im/sms/sendtemplate.action', 'c0bfefaa0d88', '12345', '3056657', '网易云信');
