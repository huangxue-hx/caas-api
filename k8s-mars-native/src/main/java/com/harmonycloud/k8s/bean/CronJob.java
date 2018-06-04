package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.harmonycloud.k8s.constant.Constant;

@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CronJob extends BaseResource {

	private CronJobSpec spec;
	
	private CronJobStatus status;

	public CronJob() {
		this.setKind("CronJob");
		this.setApiVersion(Constant.CRONJOB_VERSION);
	}
	
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
