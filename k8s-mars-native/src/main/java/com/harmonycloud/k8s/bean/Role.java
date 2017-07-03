package com.harmonycloud.k8s.bean;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author qg
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Role extends BaseResource{

	private List<PolicyRule> rules;

	public List<PolicyRule> getRules() {
		return rules;
	}

	public void setRules(List<PolicyRule> rules) {
		this.rules = rules;
	}
	
}
