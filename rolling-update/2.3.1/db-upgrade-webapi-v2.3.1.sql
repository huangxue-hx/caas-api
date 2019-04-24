use k8s_auth_server;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for image_tag_desc
-- ----------------------------
DROP TABLE IF EXISTS `image_tag_desc`;
CREATE TABLE `image_tag_desc` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `repository_id` int(11) NOT NULL COMMENT '镜像仓库表id',
  `image_name` varchar(128) NOT NULL COMMENT '镜像名称',
  `tag_name` varchar(15) NOT NULL COMMENT '镜像版本名称',
  `tag_desc` varchar(500) NOT NULL COMMENT '镜像版本描述',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='镜像版本描述表';

SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO `k8s_auth_server`.`url_dic`(`url`, `module`, `resource`) VALUES ('/tenants/*/projects/*/repositories/*/images/*/tags/*/desc', 'delivery', 'image');
INSERT INTO `k8s_auth_server`.`url_dic`(`url`, `module`, `resource`) VALUES ('/tenants/*/projects/*/repositories/*/images/*/tags/*/deploys', 'delivery', 'image');