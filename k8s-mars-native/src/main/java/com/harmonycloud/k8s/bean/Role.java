package com.harmonycloud.k8s.bean;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.harmonycloud.k8s.constant.Constant;

/**
 * @author qg
 *
 */
@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Role extends BaseResource{

	private List<PolicyRule> rules;

	public Role() {
		this.setApiVersion(Constant.ROLE_VERSION);
		this.setKind("Role");
	}
	
	public List<PolicyRule> getRules() {
		return rules;
	}

	public void setRules(List<PolicyRule> rules) {
		this.rules = rules;
	}
	
}
