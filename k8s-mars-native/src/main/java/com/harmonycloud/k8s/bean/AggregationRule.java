package com.harmonycloud.k8s.bean;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AggregationRule {

	private List<LabelSelector> clusterRoleSelectors;

	public List<LabelSelector> getClusterRoleSelectors() {
		return clusterRoleSelectors;
	}

	public void setClusterRoleSelectors(List<LabelSelector> clusterRoleSelectors) {
		this.clusterRoleSelectors = clusterRoleSelectors;
	}
}
