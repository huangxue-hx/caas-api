package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * 
 * @author jmi
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value=JsonInclude.Include.NON_NULL)
public class SecretVolumeSource {
	
	private String secretName;
	
	private List<KeyToPath> items;
	
	private Integer defaultMode;

	public String getSecretName() {
		return secretName;
	}

	public void setSecretName(String secretName) {
		this.secretName = secretName;
	}

	public List<KeyToPath> getItems() {
		return items;
	}

	public void setItems(List<KeyToPath> items) {
		this.items = items;
	}

	public Integer getDefaultMode() {
		return defaultMode;
	}

	public void setDefaultMode(Integer defaultMode) {
		this.defaultMode = defaultMode;
	}

}
