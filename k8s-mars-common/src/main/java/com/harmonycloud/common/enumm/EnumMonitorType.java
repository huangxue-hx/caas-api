package com.harmonycloud.common.enumm;


/**
 * 监控的类型
 * 
 * @author jmi
 *
 */
public enum EnumMonitorType {
	
	NODE("node"), POD("pod"), PROCESS("process"), POD_CONTAINER("pod_container");
	
	private String type;
	
	private EnumMonitorType(String type) {
		this.setType(type);
	}
	
	public static EnumMonitorType getMonitType(String type) {
		for (EnumMonitorType mType : EnumMonitorType.values()) {
			if(mType.name().equals(type)) {
				return mType;
			}
		}
		return null;
	}

	public String getType() {
		return type;
	}

	private void setType(String type) {
		this.type = type;
	}

}
