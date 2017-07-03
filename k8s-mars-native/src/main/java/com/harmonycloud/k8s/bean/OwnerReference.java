package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OwnerReference {

	private String apiVersion;
	
	private boolean blockOwnerDeletion;
	
	private boolean controller;
	
	private String kind;
	
	private String name;
	
	private String uid;

	public String getApiVersion() {
		return apiVersion;
	}

	public void setApiVersion(String apiVersion) {
		this.apiVersion = apiVersion;
	}

	public boolean isBlockOwnerDeletion() {
		return blockOwnerDeletion;
	}

	public void setBlockOwnerDeletion(boolean blockOwnerDeletion) {
		this.blockOwnerDeletion = blockOwnerDeletion;
	}

	public boolean isController() {
		return controller;
	}

	public void setController(boolean controller) {
		this.controller = controller;
	}

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

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}
	
}
