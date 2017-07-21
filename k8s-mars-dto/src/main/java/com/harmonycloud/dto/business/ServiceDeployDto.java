package com.harmonycloud.dto.business;

public class ServiceDeployDto {
	
	private String namespace;
	
	private ServiceTemplateDto serviceTemplate;

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
