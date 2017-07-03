package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author qg
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeploymentSpec {

	private Integer replicas;
	
	private LabelSelector selector;
	
	private PodTemplateSpec template;
	
	private DeploymentStrategy strategy;
	
	private Integer minReadySeconds;
	
	private Integer revisionHistoryLimit;
	
	private boolean paused;
	
	private RollbackConfig rollbackTo;
	
	private Integer progressDeadlineSeconds;

	public DeploymentStrategy getStrategy() {
		return strategy;
	}

	public void setStrategy(DeploymentStrategy strategy) {
		this.strategy = strategy;
	}

	public Integer getMinReadySeconds() {
		return minReadySeconds;
	}

	public void setMinReadySeconds(Integer minReadySeconds) {
		this.minReadySeconds = minReadySeconds;
	}

	public Integer getRevisionHistoryLimit() {
		return revisionHistoryLimit;
	}

	public void setRevisionHistoryLimit(Integer revisionHistoryLimit) {
		this.revisionHistoryLimit = revisionHistoryLimit;
	}

	public boolean isPaused() {
		return paused;
	}

	public void setPaused(boolean paused) {
		this.paused = paused;
	}

	public RollbackConfig getRollbackTo() {
		return rollbackTo;
	}

	public void setRollbackTo(RollbackConfig rollbackTo) {
		this.rollbackTo = rollbackTo;
	}

	public Integer getProgressDeadlineSeconds() {
		return progressDeadlineSeconds;
	}

	public void setProgressDeadlineSeconds(Integer progressDeadlineSeconds) {
		this.progressDeadlineSeconds = progressDeadlineSeconds;
	}

	public Integer getReplicas() {
		return replicas;
	}

	public void setReplicas(Integer replicas) {
		this.replicas = replicas;
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
