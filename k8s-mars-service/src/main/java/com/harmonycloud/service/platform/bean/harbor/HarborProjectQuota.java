package com.harmonycloud.service.platform.bean.harbor;

public class HarborProjectQuota {
	private String project_name;
	private Integer project_id;
	private Integer quota_num;
	private Float   quota_size;

	public Integer getQuota_num() {
		return quota_num;
	}

	public void setQuota_num(Integer quota_num) {
		this.quota_num = quota_num;
	}

	public Float getQuota_size() {
		return quota_size;
	}

	public void setQuota_size(Float quota_size) {
		this.quota_size = quota_size;
	}

	public String getProject_name() {
		return project_name;
	}

	public void setProject_name(String project_name) {
		this.project_name = project_name;
	}

	public Integer getProject_id() {
		return project_id;
	}

	public void setProject_id(Integer project_id) {
		this.project_id = project_id;
	}
}
