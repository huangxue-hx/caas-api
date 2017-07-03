package com.harmonycloud.k8s.bean;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author qg
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class LabelSelector {

	private Map<String,Object> matchLabels;
	
	private List<LabelSelectorRequirement> matchExpressions;

	public List<LabelSelectorRequirement> getMatchExpressions() {
		return matchExpressions;
	}

	public void setMatchExpressions(List<LabelSelectorRequirement> matchExpressions) {
		this.matchExpressions = matchExpressions;
	}

	public Map<String, Object> getMatchLabels() {
		return matchLabels;
	}

	public void setMatchLabels(Map<String, Object> matchLabels) {
		this.matchLabels = matchLabels;
	}
	
}
