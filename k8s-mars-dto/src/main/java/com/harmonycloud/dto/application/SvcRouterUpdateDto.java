package com.harmonycloud.dto.application;

import java.util.ArrayList;
import java.util.List;

public class SvcRouterUpdateDto {
	private String namespace;

	private String name;

	private List<HttpLabelDto> labels;

	private String createTime;

	private String app;

	private String annotaion;

	private List<SelectorDto> selector;

	private String service;

	private List<TcpRuleDto> rules = new ArrayList<>();

	public String getNamespace() {
		return namespace;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public List<SelectorDto> getSelector() {
		return selector;
	}

	public void setSelector(List<SelectorDto> selector) {
		this.selector = selector;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<HttpLabelDto> getLabels() {
		return labels;
	}

	public void setLabels(List<HttpLabelDto> labels) {
		this.labels = labels;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getApp() {
		return app;
	}

	public void setApp(String app) {
		this.app = app;
	}

	public String getAnnotaion() {
		return annotaion;
	}

	public void setAnnotaion(String annotaion) {
		this.annotaion = annotaion;
	}

	public List<TcpRuleDto> getRules() {
		return rules;
	}

	public void setRules(List<TcpRuleDto> rules) {
		this.rules = rules;
	}

}
