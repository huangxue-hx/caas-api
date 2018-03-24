package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class EnvVarSource {

	private ConfigMapKeySelector configMapKeyRef;
	
	private ObjectFieldSelector fieldRef;
	
	private ResourceFieldSelector resourceFieldRef;
	
	private SecretKeySelector secretKeyRef;

	public ConfigMapKeySelector getConfigMapKeyRef() {
		return configMapKeyRef;
	}

	public void setConfigMapKeyRef(ConfigMapKeySelector configMapKeyRef) {
		this.configMapKeyRef = configMapKeyRef;
	}

	public ObjectFieldSelector getFieldRef() {
		return fieldRef;
	}

	public void setFieldRef(ObjectFieldSelector fieldRef) {
		this.fieldRef = fieldRef;
	}

	public ResourceFieldSelector getResourceFieldRef() {
		return resourceFieldRef;
	}

	public void setResourceFieldRef(ResourceFieldSelector resourceFieldRef) {
		this.resourceFieldRef = resourceFieldRef;
	}

	public SecretKeySelector getSecretKeyRef() {
		return secretKeyRef;
	}

	public void setSecretKeyRef(SecretKeySelector secretKeyRef) {
		this.secretKeyRef = secretKeyRef;
	}
	
	
}
