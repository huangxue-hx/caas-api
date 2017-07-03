package com.harmonycloud.service.platform.dto;

import com.harmonycloud.k8s.bean.ReplicaSet;

/**
 * Created by andy on 17-2-27.
 */
public class ReplicaSetDto {

	private String type;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public ReplicaSet getObject() {
		return object;
	}

	public void setObject(ReplicaSet object) {
		this.object = object;
	}

	private ReplicaSet object;


}
