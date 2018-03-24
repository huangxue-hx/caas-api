package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class JobSpec {

	private Integer activeDeadlineSeconds;
	
	private Integer completions ;
	
	private boolean manualSelector;
	
	private Integer parallelism;
	
	private LabelSelector selector;
	
	private PodTemplateSpec template;

	public Integer getActiveDeadlineSeconds() {
		return activeDeadlineSeconds;
	}

	public void setActiveDeadlineSeconds(Integer activeDeadlineSeconds) {
		this.activeDeadlineSeconds = activeDeadlineSeconds;
	}

	public Integer getCompletions() {
		return completions;
	}

	public void setCompletions(Integer completions) {
		this.completions = completions;
	}

	public boolean isManualSelector() {
		return manualSelector;
	}

	public void setManualSelector(boolean manualSelector) {
		this.manualSelector = manualSelector;
	}

	public Integer getParallelism() {
		return parallelism;
	}

	public void setParallelism(Integer parallelism) {
		this.parallelism = parallelism;
	}

	public LabelSelector getSelector() {
		return selector;
	}

	public void setSelector(LabelSelector selector) {
		this.selector = selector;
	}

	public PodTemplateSpec getTemplate() {
		return template;
	}

	public void setTemplate(PodTemplateSpec template) {
		this.template = template;
	}
}
