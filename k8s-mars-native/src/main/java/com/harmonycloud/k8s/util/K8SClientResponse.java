package com.harmonycloud.k8s.util;

import com.harmonycloud.k8s.bean.UnversionedStatus;

public class K8SClientResponse {
	
	private int status;
	
	private String body;
	
	public K8SClientResponse() {
	}
	
	public K8SClientResponse(int status, String body) {
		super();
		this.status = status;
		this.body = body;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	
}
