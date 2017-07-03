package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CronJobSpec {

	private String concurrencyPolicy;
	
	private Integer failedJobsHistoryLimit ;
	
	private JobTemplateSpec jobTemplate ;
	
	private String schedule ;
	
	private Integer startingDeadlineSeconds;
	
	private Integer successfulJobsHistoryLimit ;
	
	private boolean suspend ;

	public boolean isSuspend() {
		return suspend;
	}

	public void setSuspend(boolean suspend) {
		this.suspend = suspend;
	}

	public Integer getSuccessfulJobsHistoryLimit() {
		return successfulJobsHistoryLimit;
	}

	public void setSuccessfulJobsHistoryLimit(Integer successfulJobsHistoryLimit) {
		this.successfulJobsHistoryLimit = successfulJobsHistoryLimit;
	}

	public Integer getStartingDeadlineSeconds() {
		return startingDeadlineSeconds;
	}

	public void setStartingDeadlineSeconds(Integer startingDeadlineSeconds) {
		this.startingDeadlineSeconds = startingDeadlineSeconds;
	}

	public String getSchedule() {
		return schedule;
	}

	public void setSchedule(String schedule) {
		this.schedule = schedule;
	}

	public JobTemplateSpec getJobTemplate() {
		return jobTemplate;
	}

	public void setJobTemplate(JobTemplateSpec jobTemplate) {
		this.jobTemplate = jobTemplate;
	}

	public Integer getFailedJobsHistoryLimit() {
		return failedJobsHistoryLimit;
	}

	public void setFailedJobsHistoryLimit(Integer failedJobsHistoryLimit) {
		this.failedJobsHistoryLimit = failedJobsHistoryLimit;
	}

	public String getConcurrencyPolicy() {
		return concurrencyPolicy;
	}

	public void setConcurrencyPolicy(String concurrencyPolicy) {
		this.concurrencyPolicy = concurrencyPolicy;
	}
	
}
