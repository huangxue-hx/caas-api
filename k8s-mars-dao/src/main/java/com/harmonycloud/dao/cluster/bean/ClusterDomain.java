package com.harmonycloud.dao.cluster.bean;

import java.io.Serializable;

public class ClusterDomain  implements Serializable{
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	private Integer id;
	
	private String domain;
	
}
