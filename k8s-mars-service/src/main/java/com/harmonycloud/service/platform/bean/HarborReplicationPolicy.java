package com.harmonycloud.service.platform.bean;
import java.util.Map;
import java.util.List;

public class HarborReplicationPolicy {
	private Integer project_id;              
    private Integer target_id;                 
    private String name;                     
    private String description; 
    private Integer enabled;
    private Integer partial;


	public Integer getPartial() {
		return partial;
	}

	public void setPartial(Integer partial) {
		this.partial = partial;
	}

	public Integer getProject_id() {
		return project_id;
	}
	public void setProject_id(Integer project_id) {
		this.project_id = project_id;
	}
	public Integer getTarget_id() {
		return target_id;
	}
	public void setTarget_id(Integer target_id) {
		this.target_id = target_id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Integer getEnabled() {
		return enabled;
	}
	public void setEnabled(Integer enabled) {
		this.enabled = enabled;
	}
}