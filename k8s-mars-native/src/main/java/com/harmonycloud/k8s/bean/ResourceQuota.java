package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author qg
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResourceQuota extends BaseResource{

	private ResourceQuotaSpec spec;
	
	private ResourceQuotaStatus status;

	public ResourceQuotaSpec getSpec() {
		return spec;
	}

	public void setSpec(ResourceQuotaSpec spec) {
		this.spec = spec;
	}

	public ResourceQuotaStatus getStatus() {
		return status;
	}

	public void setStatus(ResourceQuotaStatus status) {
		this.status = status;
	}
	
	
}
