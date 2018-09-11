package com.harmonycloud.dao.application.bean;

import java.io.Serializable;
import java.util.List;

/**
 * Created by miaokun on 18/08/29.
 */
public class ConfigService implements Serializable {

	private String serviceName;
	private String tag;
	private String image;
	private String serviceDomainName;
	private String createTime;
	private String updateTime;
	private String configName;
	private String projectId;
	private String tenantId;

	public ConfigService(){
		super();
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getServiceDomainName() {
		return serviceDomainName;
	}

	public void setServiceDomainName(String serviceDomainName) {
		this.serviceDomainName = serviceDomainName;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}

	public String getConfigName() {
		return configName;
	}

	public void setConfigName(String configName) {
		this.configName = configName;
	}

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}



}
