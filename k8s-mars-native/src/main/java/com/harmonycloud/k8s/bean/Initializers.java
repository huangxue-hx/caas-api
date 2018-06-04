package com.harmonycloud.k8s.bean;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Initializers {
	
	private List<Initializer> pending;
	
	private Status result;

	public List<Initializer> getPending() {
		return pending;
	}

	public void setPending(List<Initializer> pending) {
		this.pending = pending;
	}

	public Status getResult() {
		return result;
	}

	public void setResult(Status result) {
		this.result = result;
	}

}
