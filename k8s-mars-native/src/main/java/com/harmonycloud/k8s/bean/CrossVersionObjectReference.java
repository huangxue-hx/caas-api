package com.harmonycloud.k8s.bean;

/**
 * 
 * @author jmi
 *
 */
public class CrossVersionObjectReference {
	
	private String kind;
	
	private String name;
	
	private String apiVersion;
	
	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getApiVersion() {
		return apiVersion;
	}

	public void setApiVersion(String apiVersion) {
		this.apiVersion = apiVersion;
	}

}
