package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class EnvFromSource {

	private ConfigMapEnvSource configMapRef;
	
	private String prefix;
	
	private SecretEnvSource secretRef;

	public ConfigMapEnvSource getConfigMapRef() {
		return configMapRef;
	}

	public void setConfigMapRef(ConfigMapEnvSource configMapRef) {
		this.configMapRef = configMapRef;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public SecretEnvSource getSecretRef() {
		return secretRef;
	}

	public void setSecretRef(SecretEnvSource secretRef) {
		this.secretRef = secretRef;
	}
	
}
