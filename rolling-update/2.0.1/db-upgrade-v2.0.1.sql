CREATE TABLE `configfile_item` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id主键,自动生成',
  `configfile_id` varchar(64) NOT NULL COMMENT '外键,配置id',
  `path` varchar(512) DEFAULT NULL COMMENT '路径',
  `content` mediumtext NOT NULL COMMENT '文件内容',
  `file_name` varchar(128) DEFAULT NULL COMMENT '文件名',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=41 DEFAULT CHARSET=utf8;