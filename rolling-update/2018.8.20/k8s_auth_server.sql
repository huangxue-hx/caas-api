/*
  ingressController sql

  table
    url_dic: 添加数据
    ingress_controller_port: 创建ingressController 端口占用表
 */
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/clusters/*/ingressController', 'infrastructure', 'clustermgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/clusters/*/ingressController/portRange', 'infrastructure', 'clustermgr');
INSERT INTO `k8s_auth_server`.`url_dic` (`url`, `module`, `resource`) VALUES ('/tenants/*/ingressControllerNames', 'tenant', 'basic');

-- ----------------------------
-- Table structure for `ingress_controller_port`
-- ----------------------------
DROP TABLE IF EXISTS `ingress_controller_port`;
CREATE TABLE `ingress_controller_port` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `name` varchar(64) NOT NULL COMMENT 'ingress-controller名称',
  `cluster_id` varchar(64) NOT NULL COMMENT 'ingress-controller所属集群',
  `http_port` int(3) DEFAULT NULL COMMENT 'ingress-controller的http端口',
  `https_port` int(3) DEFAULT NULL COMMENT 'ingress-controller的https端口',
  `health_port` int(5) DEFAULT NULL COMMENT 'ingress-controller的health端口',
  `status_port` int(5) DEFAULT NULL COMMENT 'ingress-controller的status端口',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8;

ALTER TABLE `tenant_cluster_quota`
ADD COLUMN `ic_names` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '负载均衡器名称，多个以“，”分割';
