package com.harmonycloud.service.platform.dto;

import com.harmonycloud.k8s.bean.Pod;

/**
 * Created by andy on 17-2-27.
 */
public class PodDto {

	private String type;

	private Pod object;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Pod getObject() {
		return object;
	}

	public void setObject(Pod object) {
		this.object = object;
	}
}
