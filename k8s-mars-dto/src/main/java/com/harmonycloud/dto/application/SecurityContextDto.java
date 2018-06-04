package com.harmonycloud.dto.application;

import java.util.List;

public class SecurityContextDto {
	
	private boolean privileged;
	
	private boolean security;
	
	private List<String> add;
	
	private List<String> drop;

	public boolean isPrivileged() {
		return privileged;
	}

	public void setPrivileged(boolean privileged) {
		this.privileged = privileged;
	}

	public boolean isSecurity() {
		return security;
	}

	public void setSecurity(boolean security) {
		this.security = security;
	}

	public List<String> getAdd() {
		return add;
	}

	public void setAdd(List<String> add) {
		this.add = add;
	}

	public List<String> getDrop() {
		return drop;
	}

	public void setDrop(List<String> drop) {
		this.drop = drop;
	}

}
