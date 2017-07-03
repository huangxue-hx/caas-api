package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author qg
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeleteOptions {

	private String kind;
	
	private String apiVersion;
	
	private Integer gracePeriodSeconds;
	
	private boolean orphanDependents;
	
	private Preconditions preconditions;
	
	private String propagationPolicy;

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

	public Integer getGracePeriodSeconds() {
		return gracePeriodSeconds;
	}

	public void setGracePeriodSeconds(Integer gracePeriodSeconds) {
		this.gracePeriodSeconds = gracePeriodSeconds;
	}

	public boolean isOrphanDependents() {
		return orphanDependents;
	}

	public void setOrphanDependents(boolean orphanDependents) {
		this.orphanDependents = orphanDependents;
	}

	public Preconditions getPreconditions() {
		return preconditions;
	}

	public void setPreconditions(Preconditions preconditions) {
		this.preconditions = preconditions;
	}

	public String getPropagationPolicy() {
		return propagationPolicy;
	}

	public void setPropagationPolicy(String propagationPolicy) {
		this.propagationPolicy = propagationPolicy;
	}
	
	
}
