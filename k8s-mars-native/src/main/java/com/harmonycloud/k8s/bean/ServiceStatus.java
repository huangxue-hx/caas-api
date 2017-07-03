package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author qg
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceStatus {

	private LoadBalancerStatus loadBalancer;

	public LoadBalancerStatus getLoadBalancer() {
		return loadBalancer;
	}

	public void setLoadBalancer(LoadBalancerStatus loadBalancer) {
		this.loadBalancer = loadBalancer;
	}
	
	
}
