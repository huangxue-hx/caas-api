package com.harmonycloud.k8s.bean;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class JobStatus {
	
	private Integer active;
	
	private String completionTime;
	
	private List<JobCondition> conditions;

	public Integer getActive() {
		return active;
	}

	public void setActive(Integer active) {
		this.active = active;
	}

	public String getCompletionTime() {
		return completionTime;
	}

	public void setCompletionTime(String completionTime) {
		this.completionTime = completionTime;
	}

	public List<JobCondition> getConditions() {
		return conditions;
	}

	public void setConditions(List<JobCondition> conditions) {
		this.conditions = conditions;
	}
}
