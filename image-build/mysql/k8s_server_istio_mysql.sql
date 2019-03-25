/*
Source Database       : k8s_server_mysql

Target Server Type    : MYSQL
Target Server Version : 50635
File Encoding         : 65001

Date: 2018-11-14
*/

-- 策略概览表（rule_overview）
DROP TABLE IF EXISTS `rule_overview`;
CREATE TABLE `rule_overview` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `rule_name` varchar(100) NOT NULL COMMENT '策略名称',
  `rule_id` varchar(32) NOT NULL COMMENT '策略关联id',
  `rule_type` varchar(20) DEFAULT NULL COMMENT '策略类型',
  `rule_scope` varchar(10) DEFAULT NULL COMMENT '策略作用范围（0：全局）',
  `rule_cluster_id` varchar(128) NOT NULL COMMENT '集群id',
  `rule_ns` varchar(128) DEFAULT NULL COMMENT '集群中命名空间',
  `rule_svc` varchar(128) DEFAULT NULL COMMENT '策略挂载服务名称',
  `rule_source_num` int(2) DEFAULT NULL COMMENT '正常状态下策略创建资源对象个数',
  `switch_status` int(1) DEFAULT 1 COMMENT '策略开关状态(0:关闭；1:开启)',
  `data_status` int(2) DEFAULT 0 COMMENT '策略状态(0:正常)',
  `data_err_loc` int(2) DEFAULT 0 COMMENT '策略异常位置',
  `user_id` varchar(128) DEFAULT NULL COMMENT '最近一次更新该策略用户id',
  `create_time` timestamp DEFAULT NULL COMMENT '策略创建时间',
  `update_time` timestamp DEFAULT NULL COMMENT '策略最近一次更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='策略概览表';

-- 策略中资源信息表（rule_detail）
DROP TABLE IF EXISTS `rule_detail`;
CREATE TABLE `rule_detail` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `rule_id` varchar(32) NOT NULL COMMENT '策略关联id',
  `rule_detail_order` int(2) DEFAULT NULL COMMENT '资源创建顺序',
  `rule_detail_content` blob DEFAULT NULL COMMENT '策略中资源对象yaml',
  `create_time` timestamp DEFAULT NULL COMMENT '策略创建时间',
  `update_time` timestamp DEFAULT NULL COMMENT '策略最近一次更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='策略中资源信息表';

INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/tenants/*/projects/*/deploys/*/istiopolicies', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/tenants/*/projects/*/deploys/*/istiopolicies/*', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/tenants/*/projects/*/deploys/*/istiopolicies/*/open', 'appcenter', 'app');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/tenants/*/projects/*/deploys/*/istiopolicies/*/close', 'appcenter', 'app');

-- ----------------------- 2018-11-29 --------------------------------------
INSERT INTO `k8s_auth_server`.`url_dic`(url,module,resource) values('/tenants/*/namespaces/*/istiopolicies','appcenter','app');

-- ----------------------- 2018-12-6 --------------------------------------
INSERT INTO `k8s_auth_server`.`url_dic`(url,module,resource) values('/tenants/*/projects/*/deploys/*/istiopolicies/desServiceVersions','appcenter','app');
INSERT INTO `k8s_auth_server`.`url_dic`(url,module,resource) values('/tenants/*/projects/*/deploys/*/istiopolicies/sourceServiceVersions','appcenter','app');
