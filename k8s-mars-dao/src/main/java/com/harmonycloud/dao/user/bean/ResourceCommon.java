package com.harmonycloud.dao.user.bean;

/**
 * 资源性和非资源性共通类
 * @author yj
 * @date 2017年1月6日
 */
public class ResourceCommon {
	
	private String name;
	private Operate operate;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Operate getOperate() {
		return operate;
	}
	public void setOperate(Operate operate) {
		this.operate = operate;
	}
	
	
}
