package com.harmonycloud.k8s.bean;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author qg
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoadBalancerStatus {

	private List<LoadBalancerIngress> ingress;

	public List<LoadBalancerIngress> getIngress() {
		return ingress;
	}

	public void setIngress(List<LoadBalancerIngress> ingress) {
		this.ingress = ingress;
	}
	
	
}
