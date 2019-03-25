package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * @author yekan
 *
 */
@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class StatefulSetSpec {

	private String podManagementPolicy;

	private Integer replicas;

	private Integer revisionHistoryLimit;

	private LabelSelector selector;

	private String serviceName;

	private PodTemplateSpec template;

	private StatefulSetUpdateStrategy updateStrategy;

	private List<PersistentVolumeClaim> volumeClaimTemplates;

	public String getPodManagementPolicy() {
		return podManagementPolicy;
	}

	public void setPodManagementPolicy(String podManagementPolicy) {
		this.podManagementPolicy = podManagementPolicy;
	}

	public Integer getReplicas() {
		return replicas;
	}

	public void setReplicas(Integer replicas) {
		this.replicas = replicas;
	}

	public Integer getRevisionHistoryLimit() {
		return revisionHistoryLimit;
	}

	public void setRevisionHistoryLimit(Integer revisionHistoryLimit) {
		this.revisionHistoryLimit = revisionHistoryLimit;
	}

	public LabelSelector getSelector() {
		return selector;
	}

	public void setSelector(LabelSelector selector) {
		this.selector = selector;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public PodTemplateSpec getTemplate() {
		return template;
	}

	public void setTemplate(PodTemplateSpec template) {
		this.template = template;
	}

	public StatefulSetUpdateStrategy getUpdateStrategy() {
		return updateStrategy;
	}

	public void setUpdateStrategy(StatefulSetUpdateStrategy updateStrategy) {
		this.updateStrategy = updateStrategy;
	}

	public List<PersistentVolumeClaim> getVolumeClaimTemplates() {
		return volumeClaimTemplates;
	}

	public void setVolumeClaimTemplates(List<PersistentVolumeClaim> volumeClaimTemplates) {
		this.volumeClaimTemplates = volumeClaimTemplates;
	}
}
