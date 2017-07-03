package com.harmonycloud.service.platform.bean;

public class PvDto {
	private Object bind;
	private String capacity;
	private boolean multiple;
	private String name;
	private boolean readOnly;
	private String tenantid;
	private String time;
	private String type;
	private String usage;
	private Tenant tenant;

	public Object getBind() {
		return bind;
	}

	public void setBind(Object bind) {
		this.bind = bind;
	}

	public String getCapacity() {
		return capacity;
	}

	public void setCapacity(String capacity) {
		this.capacity = capacity;
	}

	public boolean isMultiple() {
		return multiple;
	}

	public void setMultiple(boolean multiple) {
		this.multiple = multiple;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	public String getTenantid() {
		return tenantid;
	}

	public void setTenantid(String tenantid) {
		this.tenantid = tenantid;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getUsage() {
		return usage;
	}

	public void setUsage(String usage) {
		this.usage = usage;
	}

	public Tenant getTenant() {
		return tenant;
	}

	public void setTenant(Tenant tenant) {
		this.tenant = tenant;
	}

	public class Tenant {
		private String tenantid;
		private String tenantname;

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

	}
	
}
