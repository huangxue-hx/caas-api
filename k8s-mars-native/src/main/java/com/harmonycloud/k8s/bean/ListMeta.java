package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ListMeta {
	
	//TODO关键字 continue String
	
	private String resourceVersion;
	
	private String selflink;

	public String getResourceVersion() {
		return resourceVersion;
	}

	public void setResourceVersion(String resourceVersion) {
		this.resourceVersion = resourceVersion;
	}

	public String getSelflink() {
		return selflink;
	}

	public void setSelflink(String selflink) {
		this.selflink = selflink;
	}
}
