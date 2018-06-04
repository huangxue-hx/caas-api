package com.harmonycloud.common.enumm;


/**
 * 
 * 集群状态枚举
 * 
 * @author zhangkui
 *
 */
public enum ComponentServiceTypeEnum {

	INFLUXDB("influxdb"),HEAPSTER("heapster"),ES("es"),REDIS("api-redis");

	private String name;

	ComponentServiceTypeEnum(String name) {
		this.name = name;
	}

	public static ComponentServiceTypeEnum getEnum(String name) {
		for (ComponentServiceTypeEnum serviceTypeEnum : ComponentServiceTypeEnum.values()) {
			if(serviceTypeEnum.getName().equals(name)) {
				return serviceTypeEnum;
			}
		}
		return null;
	}


	public String getName() {
		return name;
	}
}
