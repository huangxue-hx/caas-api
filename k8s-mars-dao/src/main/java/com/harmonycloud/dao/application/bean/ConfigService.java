package com.harmonycloud.dao.application.bean;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.DataPrivilegeField;
import com.harmonycloud.common.enumm.DataPrivilegeType;
import com.harmonycloud.common.enumm.DataResourceTypeEnum;

import java.io.Serializable;
import java.util.List;

/**
 * Created by miaokun on 18/08/29.
 */
@DataPrivilegeType(type = DataResourceTypeEnum.SERVICE)
public class ConfigService implements Serializable {
	@DataPrivilegeField(type = CommonConstant.DATA_FIELD)
	private String serviceName;
	@DataPrivilegeField(type = CommonConstant.NAMESPACE_FIELD)
	private String serviceNamespace;
	private String tag;
	private String image;
	private String serviceDomainName;
	private String createTime;
	private String updateTime;
	private String configName;
	@DataPrivilegeField(type = CommonConstant.PROJECTID_FIELD)
	private String projectId;
	private String tenantId;
	private String type;//判断服务或守护进程
	private String status;//服务的运行状态
	private String dataPrivilege;

	public String getServiceNamespace() {
		return serviceNamespace;
	}

	public void setServiceNamespace(String serviceNamespace) {
		this.serviceNamespace = serviceNamespace;
	}

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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getDataPrivilege() {
		return dataPrivilege;
	}

	public void setDataPrivilege(String dataPrivilege) {
		this.dataPrivilege = dataPrivilege;
	}
}
