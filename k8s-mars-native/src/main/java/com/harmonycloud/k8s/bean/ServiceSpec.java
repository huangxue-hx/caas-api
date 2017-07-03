package com.harmonycloud.k8s.bean;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author qg
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceSpec {

	private List<ServicePort> ports;
	
	private Object selector;
	
	private String clusterIP;
	
	private String type;
	
	private List<String> externalIPs;
	
	private List<String> deprecatedPublicIPs;
	
	private String sessionAffinity;

	private String loadBalancerIP;
	
	private List<String> loadBalancerSourceRanges;
	
	private String externalName;
	
	public List<ServicePort> getPorts() {
		return ports;
	}

	public void setPorts(List<ServicePort> ports) {
		this.ports = ports;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSessionAffinity() {
		return sessionAffinity;
	}

	public void setSessionAffinity(String sessionAffinity) {
		this.sessionAffinity = sessionAffinity;
	}

	public Object getSelector() {
		return selector;
	}

	public void setSelector(Object selector) {
		this.selector = selector;
	}

	public String getClusterIP() {
		return clusterIP;
	}

	public void setClusterIP(String clusterIP) {
		this.clusterIP = clusterIP;
	}

	public List<String> getExternalIPs() {
		return externalIPs;
	}

	public void setExternalIPs(List<String> externalIPs) {
		this.externalIPs = externalIPs;
	}

	public List<String> getDeprecatedPublicIPs() {
		return deprecatedPublicIPs;
	}

	public void setDeprecatedPublicIPs(List<String> deprecatedPublicIPs) {
		this.deprecatedPublicIPs = deprecatedPublicIPs;
	}

	public String getLoadBalancerIP() {
		return loadBalancerIP;
	}

	public void setLoadBalancerIP(String loadBalancerIP) {
		this.loadBalancerIP = loadBalancerIP;
	}

	public List<String> getLoadBalancerSourceRanges() {
		return loadBalancerSourceRanges;
	}

	public void setLoadBalancerSourceRanges(List<String> loadBalancerSourceRanges) {
		this.loadBalancerSourceRanges = loadBalancerSourceRanges;
	}

	public String getExternalName() {
		return externalName;
	}

	public void setExternalName(String externalName) {
		this.externalName = externalName;
	}

}
