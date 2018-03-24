package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VolumeProjection {

	private ConfigMapProjection configMap;
	
	private DownwardAPIProjection downwardAPI;
	
	private SecretProjection secret;

	public ConfigMapProjection getConfigMap() {
		return configMap;
	}

	public void setConfigMap(ConfigMapProjection configMap) {
		this.configMap = configMap;
	}

	public DownwardAPIProjection getDownwardAPI() {
		return downwardAPI;
	}

	public void setDownwardAPI(DownwardAPIProjection downwardAPI) {
		this.downwardAPI = downwardAPI;
	}

	public SecretProjection getSecret() {
		return secret;
	}

	public void setSecret(SecretProjection secret) {
		this.secret = secret;
	}
}
