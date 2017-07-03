package com.harmonycloud.dto.business;

import java.io.Serializable;
import java.util.List;

public class BusinessTemplateDto implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8400861610658189494L;
	private int id;
	private String name; // 模板名字
	private String tag; // 版本
	private String desc; // 描述
	private String tenant; // 租户
	private List<ServiceTemplateDto> serviceList; // 应用模板list
	private List<TopologysDto> topologyList; // 拓扑关系list

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getTenant() {
		return tenant;
	}

	public void setTenant(String tenant) {
		this.tenant = tenant;
	}

	public List<ServiceTemplateDto> getServiceList() {
		return serviceList;
	}

	public void setServiceList(List<ServiceTemplateDto> serviceList) {
		this.serviceList = serviceList;
	}

	public List<TopologysDto> getTopologyList() {
		return topologyList;
	}

	public void setTopologyList(List<TopologysDto> topologyList) {
		this.topologyList = topologyList;
	}
}
