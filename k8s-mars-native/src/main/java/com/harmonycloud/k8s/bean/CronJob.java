package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CronJob extends BaseResource {

	private CronJobSpec spec;
	
	private CronJobStatus status;

	public CronJobStatus getStatus() {
		return status;
	}

	public void setStatus(CronJobStatus status) {
		this.status = status;
	}

	public CronJobSpec getSpec() {
		return spec;
	}

	public void setSpec(CronJobSpec spec) {
		this.spec = spec;
	}
}
