package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DownwardAPIVolumeFile {
	
	private ObjectFieldSelector fieldRef;
	
	private Integer mode;
	
	private String path;
	
	private ResourceFieldSelector resourceFieldRef;

	public ObjectFieldSelector getFieldRef() {
		return fieldRef;
	}

	public void setFieldRef(ObjectFieldSelector fieldRef) {
		this.fieldRef = fieldRef;
	}

	public Integer getMode() {
		return mode;
	}

	public void setMode(Integer mode) {
		this.mode = mode;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public ResourceFieldSelector getResourceFieldRef() {
		return resourceFieldRef;
	}

	public void setResourceFieldRef(ResourceFieldSelector resourceFieldRef) {
		this.resourceFieldRef = resourceFieldRef;
	}
	
	
}
