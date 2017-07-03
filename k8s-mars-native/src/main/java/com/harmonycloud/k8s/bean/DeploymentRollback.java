package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author qg
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeploymentRollback {

	private String kind;
	
	private String apiVersion;
	
	private String name;
	
	private Object updatedAnnotations;
	
	private RollbackConfig rollbackTo;

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public String getApiVersion() {
		return apiVersion;
	}

	public void setApiVersion(String apiVersion) {
		this.apiVersion = apiVersion;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Object getUpdatedAnnotations() {
		return updatedAnnotations;
	}

	public void setUpdatedAnnotations(Object updatedAnnotations) {
		this.updatedAnnotations = updatedAnnotations;
	}

	public RollbackConfig getRollbackTo() {
		return rollbackTo;
	}

	public void setRollbackTo(RollbackConfig rollbackTo) {
		this.rollbackTo = rollbackTo;
	}
	
	
}
