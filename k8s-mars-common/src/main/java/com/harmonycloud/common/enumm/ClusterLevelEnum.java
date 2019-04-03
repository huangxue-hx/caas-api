package com.harmonycloud.common.enumm;


/**
 * 
 * 环境类型级别枚举定义
 * 
 * @author zhangkui
 *
 */
public enum ClusterLevelEnum {
	PLATFORM(0),
	DEV(1),
	QAS(2),
	UAT(3),
	PRD(4);

	private int level;

	ClusterLevelEnum(int level) {
		this.level = level;
	}
	
	public static ClusterLevelEnum getEnvLevel(String envName) {
		for (ClusterLevelEnum levelEnum : ClusterLevelEnum.values()) {
			if(levelEnum.name().equalsIgnoreCase(envName)) {
				return levelEnum;
			}
		}
		return null;
	}

	public int getLevel() {
		return level;
	}
	

}
