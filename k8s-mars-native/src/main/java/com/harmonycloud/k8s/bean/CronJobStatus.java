package com.harmonycloud.k8s.bean;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CronJobStatus {

	private List<ObjectReference> active;
	
	private String lastScheduleTime ;

	public List<ObjectReference> getActive() {
		return active;
	}

	public void setActive(List<ObjectReference> active) {
		this.active = active;
	}

	public String getLastScheduleTime() {
		return lastScheduleTime;
	}

	public void setLastScheduleTime(String lastScheduleTime) {
		this.lastScheduleTime = lastScheduleTime;
	}
}
