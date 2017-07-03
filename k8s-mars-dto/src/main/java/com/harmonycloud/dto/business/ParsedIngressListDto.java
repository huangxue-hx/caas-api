package com.harmonycloud.dto.business;

import java.util.List;
import java.util.Map;

/**
 * Created by czm on 2017/1/18.
 */
public class ParsedIngressListDto {

	private String namespace;
	private String name;
	private Map<String, Object> labels;
	// private List<HttpLabel> labels;
	private String createTime;
	private String host;
	private Object annotaion;
	private List<HttpRuleDto> rules;

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, Object> getLabels() {
		return labels;
	}

	public void setLabels(Map<String, Object> labels) {
		this.labels = labels;
	}

	public String getCreateTime() {
		return createTime;
	}

	public List<HttpRuleDto> getRules() {
		return rules;
	}

	public void setRules(List<HttpRuleDto> rules) {
		this.rules = rules;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public Object getAnnotaion() {
		return annotaion;
	}

	public void setAnnotaion(Object annotaion) {
		this.annotaion = annotaion;
	}

}
