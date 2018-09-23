package com.harmonycloud.dto.application;

public class ServiceDeployDto {

	private String tenantId;

	private String namespace;

	private ServiceTemplateDto serviceTemplate;

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public ServiceTemplateDto getServiceTemplate() {
		return serviceTemplate;
	}

	public void setServiceTemplate(ServiceTemplateDto serviceTemplate) {
		this.serviceTemplate = serviceTemplate;
	}
}
