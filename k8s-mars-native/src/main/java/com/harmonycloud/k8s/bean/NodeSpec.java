package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author qg
 *
 */
@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class NodeSpec {

	private String podCIDR;
	
	private String externalID;
	
	private String providerID;
	
	private boolean unschedulable;

	public String getPodCIDR() {
		return podCIDR;
	}

	public void setPodCIDR(String podCIDR) {
		this.podCIDR = podCIDR;
	}

	public String getExternalID() {
		return externalID;
	}

	public void setExternalID(String externalID) {
		this.externalID = externalID;
	}

	public String getProviderID() {
		return providerID;
	}

	public void setProviderID(String providerID) {
		this.providerID = providerID;
	}

	public boolean isUnschedulable() {
		return unschedulable;
	}

	public void setUnschedulable(boolean unschedulable) {
		this.unschedulable = unschedulable;
	}

	
	
}
