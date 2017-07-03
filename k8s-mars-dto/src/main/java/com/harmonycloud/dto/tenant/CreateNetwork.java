package com.harmonycloud.dto.tenant;

import java.util.List;

import com.harmonycloud.dao.tenant.bean.SubNetwork;


public class CreateNetwork {
	
	private String networkname;
	
	private NetworkTenant tenant;
	
	private List<SubNetwork> subnets;
	
	private String annotation;

	public String getNetworkname() {
		return networkname;
	}

	public void setNetworkname(String networkname) {
		this.networkname = networkname;
	}

	public NetworkTenant getTenant() {
		return tenant;
	}

	public void setTenant(NetworkTenant tenant) {
		this.tenant = tenant;
	}

	public List<SubNetwork> getSubnets() {
		return subnets;
	}

	public void setSubnets(List<SubNetwork> subnets) {
		this.subnets = subnets;
	}

	public String getAnnotation() {
		return annotation;
	}

	public void setAnnotation(String annotation) {
		this.annotation = annotation;
	}

}
