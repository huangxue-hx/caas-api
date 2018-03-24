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
public class ClusterRole extends BaseResource{

	private List<PolicyRule> rules;
	
	private AggregationRule aggregationRule;
	
	public ClusterRole() {
		this.setApiVersion(Constant.CLUSTERROLE_VERSION);
		this.setKind("ClusterRole");
	}

	public List<PolicyRule> getRules() {
		return rules;
	}

	public void setRules(List<PolicyRule> rules) {
		this.rules = rules;
	}

	public AggregationRule getAggregationRule() {
		return aggregationRule;
	}

	public void setAggregationRule(AggregationRule aggregationRule) {
		this.aggregationRule = aggregationRule;
	}
	
}
