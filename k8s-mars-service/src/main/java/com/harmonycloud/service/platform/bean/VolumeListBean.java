package com.harmonycloud.service.platform.bean;

import java.util.List;

public class VolumeListBean {
	
	private String name;
	
	private String status;
	
	private Integer capacity;
	
	private List<String> bounds;
	
	private String createTime;
	
	private Boolean readOnly;
	
	private Boolean multiMount;
	
	private String namespace;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Integer getCapacity() {
		return capacity;
	}

	public void setCapacity(Integer capacity) {
		this.capacity = capacity;
	}

	public List<String> getBounds() {
		return bounds;
	}

	public void setBounds(List<String> bounds) {
		this.bounds = bounds;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public Boolean getReadOnly() {
		return readOnly;
	}

	public void setReadOnly(Boolean readOnly) {
		this.readOnly = readOnly;
	}

	public Boolean getMultiMount() {
		return multiMount;
	}

	public void setMultiMount(Boolean multiMount) {
		this.multiMount = multiMount;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	
	

}
