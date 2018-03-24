package com.harmonycloud.dto.application;

import java.util.List;

/**
 * Created by root on 5/18/17.
 */
public class DeployedServiceNamesDto{

    private List<ServiceNameNamespace> serviceList;

	public List<ServiceNameNamespace> getServiceList() {
		return serviceList;
	}

	public void setServiceList(List<ServiceNameNamespace> serviceList) {
		this.serviceList = serviceList;
	}

}
