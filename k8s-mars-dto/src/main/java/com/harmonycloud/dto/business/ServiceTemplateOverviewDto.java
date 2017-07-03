package com.harmonycloud.dto.business;

public class ServiceTemplateOverviewDto {
	private String id;
	private String name;
	private String tag;
	private Integer isExternal;
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public Integer getIsExternal() {
		return isExternal;
	}

	public void setIsExternal(Integer isExternal) {
		this.isExternal = isExternal;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
}
