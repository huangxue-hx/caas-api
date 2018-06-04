package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class NodeAffinity {
    private NodeSelector requiredDuringSchedulingIgnoredDuringExecution;

    private List<PreferredSchedulingTerm> preferredDuringSchedulingIgnoredDuringExecution;

	public NodeSelector getRequiredDuringSchedulingIgnoredDuringExecution() {
		return requiredDuringSchedulingIgnoredDuringExecution;
	}

	public void setRequiredDuringSchedulingIgnoredDuringExecution(
			NodeSelector requiredDuringSchedulingIgnoredDuringExecution) {
		this.requiredDuringSchedulingIgnoredDuringExecution = requiredDuringSchedulingIgnoredDuringExecution;
	}

	public List<PreferredSchedulingTerm> getPreferredDuringSchedulingIgnoredDuringExecution() {
		return preferredDuringSchedulingIgnoredDuringExecution;
	}

	public void setPreferredDuringSchedulingIgnoredDuringExecution(
			List<PreferredSchedulingTerm> preferredDuringSchedulingIgnoredDuringExecution) {
		this.preferredDuringSchedulingIgnoredDuringExecution = preferredDuringSchedulingIgnoredDuringExecution;
	}
}
