package com.harmonycloud.k8s.bean;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class JobStatus {
	
	private Integer active;
	
	private Date completionTime;
	
	private List<JobCondition> conditions;
	
	private Integer failed;
	
	private Date startTime;
	
	private Integer succeeded; 

	public Integer getActive() {
		return active;
	}

	public void setActive(Integer active) {
		this.active = active;
	}

	public List<JobCondition> getConditions() {
		return conditions;
	}

	public void setConditions(List<JobCondition> conditions) {
		this.conditions = conditions;
	}

	public Integer getFailed() {
		return failed;
	}

	public void setFailed(Integer failed) {
		this.failed = failed;
	}

	public Integer getSucceeded() {
		return succeeded;
	}

	public void setSucceeded(Integer succeeded) {
		this.succeeded = succeeded;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getCompletionTime() {
		return completionTime;
	}

	public void setCompletionTime(Date completionTime) {
		this.completionTime = completionTime;
	}
}
