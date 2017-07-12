package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Job extends BaseResource {

	private JobSpec spec;
	
	private JobStatus status;

	public JobSpec getSpec() {
		return spec;
	}

	public void setSpec(JobSpec spec) {
		this.spec = spec;
	}

	public JobStatus getStatus() {
		return status;
	}

	public void setStatus(JobStatus status) {
		this.status = status;
	}
	public Job(){
		this.setKind("Job");
		this.setApiVersion("");
	}
}
