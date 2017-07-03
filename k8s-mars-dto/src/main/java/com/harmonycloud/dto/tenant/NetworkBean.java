package com.harmonycloud.dto.tenant;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.harmonycloud.dao.tenant.bean.SubNetwork;


public class NetworkBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String tenantName;
	public String networkName;
	public String annotation;
	public List<SubNetwork> subnets = new ArrayList<SubNetwork>();

	public String getTenantName() {
		return tenantName;
	}

	public void setTenantName(String tenantName) {
		this.tenantName = tenantName;
	}

	public String getNetworkName() {
		return networkName;
	}

	public void setNetworkName(String networkName) {
		this.networkName = networkName;
	}

	public String getAnnotation() {
		return annotation;
	}

	public void setAnnotation(String annotation) {
		this.annotation = annotation;
	}

	public List<SubNetwork> getSubnets() {
		return subnets;
	}

	public void setSubnets(List<SubNetwork> subnets) {
		this.subnets = subnets;
	}


}
