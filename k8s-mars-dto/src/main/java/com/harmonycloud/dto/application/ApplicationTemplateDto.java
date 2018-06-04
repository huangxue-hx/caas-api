package com.harmonycloud.dto.application;

import java.io.Serializable;
import java.util.List;

public class ApplicationTemplateDto implements Serializable {

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
	private int isDeploy;
	private boolean isPublic;

	//应用模板与项目关联
	private String projectId;

	private String clusterId;   //集群Id

	public String getClusterId() {
		return clusterId;
	}

	public void setClusterId(String clusterId) {
		this.clusterId = clusterId;
	}

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

	public int getIsDeploy() {
		return isDeploy;
	}

	public void setIsDeploy(int isDeploy) {
		this.isDeploy = isDeploy;
	}

	public boolean isPublic() {
		return isPublic;
	}

	public void setPublic(boolean isPublic) {
		this.isPublic = isPublic;
	}

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}
}
