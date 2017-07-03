package com.harmonycloud.k8s.bean;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class APIResourceList {
	private String kind;
	private String groupVersion;
	private List<Resource> resources;
	
	public String getKind() {
		return kind;
	}
	public void setKind(String kind) {
		this.kind = kind;
	}
	public String getGroupVersion() {
		return groupVersion;
	}
	public void setGroupVersion(String groupVersion) {
		this.groupVersion = groupVersion;
	}
	public List<Resource> getResources() {
		return resources;
	}
	public void setResources(List<Resource> resources) {
		this.resources = resources;
	}
}
