package com.harmonycloud.service.platform.bean;

public class ProjectDto {
	private String name;
	private String harborid;
	private String time;
	private Tenant tenant;

	public class Tenant {
		private String name;
		private String tenantId;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getTenantId() {
			return tenantId;
		}

		public void setTenantId(String tenantId) {
			this.tenantId = tenantId;
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHarborid() {
		return harborid;
	}

	public void setHarborid(String harborid) {
		this.harborid = harborid;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public Tenant getTenant() {
		return tenant;
	}

	public void setTenant(Tenant tenant) {
		this.tenant = tenant;
	}

}
