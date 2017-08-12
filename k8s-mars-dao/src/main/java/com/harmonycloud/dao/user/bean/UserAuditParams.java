package com.harmonycloud.dao.user.bean;

import java.io.Serializable;

public class UserAuditParams  implements Serializable{
	private String name;
	private String value;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
