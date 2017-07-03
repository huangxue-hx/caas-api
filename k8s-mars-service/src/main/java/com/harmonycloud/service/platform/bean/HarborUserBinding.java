package com.harmonycloud.service.platform.bean;

import java.util.List;

public class HarborUserBinding {
	private String namespace;
	private String tenantid;
	private String tenantname;
	private HarborBindingUser user;
	private List<HarborBindingProject> projects;

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getTenantid() {
		return tenantid;
	}

	public void setTenantid(String tenantid) {
		this.tenantid = tenantid;
	}

	public String getTenantname() {
		return tenantname;
	}

	public void setTenantname(String tenantname) {
		this.tenantname = tenantname;
	}

	public HarborBindingUser getUser() {
		return user;
	}

	public void setUser(HarborBindingUser user) {
		this.user = user;
	}

	public List<HarborBindingProject> getProjects() {
		return projects;
	}

	public void setProjects(List<HarborBindingProject> projects) {
		this.projects = projects;
	}

}
