package com.harmonycloud.k8s.bean;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author qg
 *
 */
@JsonInclude(value=JsonInclude.Include.NON_NULL)
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
	
	private String externalTrafficPolicy;
	
	private Integer healthCheckNodePort;
	
	private boolean publishNotReadyAddresses;
	
	private SessionAffinityConfig sessionAffinityConfig;
	
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

	public String getExternalTrafficPolicy() {
		return externalTrafficPolicy;
	}

	public void setExternalTrafficPolicy(String externalTrafficPolicy) {
		this.externalTrafficPolicy = externalTrafficPolicy;
	}

	public Integer getHealthCheckNodePort() {
		return healthCheckNodePort;
	}

	public void setHealthCheckNodePort(Integer healthCheckNodePort) {
		this.healthCheckNodePort = healthCheckNodePort;
	}

	public boolean isPublishNotReadyAddresses() {
		return publishNotReadyAddresses;
	}

	public void setPublishNotReadyAddresses(boolean publishNotReadyAddresses) {
		this.publishNotReadyAddresses = publishNotReadyAddresses;
	}

	public SessionAffinityConfig getSessionAffinityConfig() {
		return sessionAffinityConfig;
	}

	public void setSessionAffinityConfig(SessionAffinityConfig sessionAffinityConfig) {
		this.sessionAffinityConfig = sessionAffinityConfig;
	}

}
