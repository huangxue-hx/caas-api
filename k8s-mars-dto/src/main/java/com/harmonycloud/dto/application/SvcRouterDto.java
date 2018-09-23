package com.harmonycloud.dto.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SvcRouterDto {

	private String namespace ;
	
    private String name;

    private String icName;

    private Map<String, Object> labels;
    
    private String createTime;
    
    private String app;
    
    private String annotation;
    
    private SelectorDto selector;
    
    private List<TcpRuleDto> rules = new ArrayList<>();

    private String serviceType;

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

	public String getIcName() {
		return icName;
	}

	public void setIcName(String icName) {
		this.icName = icName;
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

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getApp() {
		return app;
	}

	public void setApp(String app) {
		this.app = app;
	}

	public SelectorDto getSelector() {
		return selector;
	}

	public void setSelector(SelectorDto selector) {
		this.selector = selector;
	}

	public List<TcpRuleDto> getRules() {
		return rules;
	}

	public void setRules(List<TcpRuleDto> rules) {
		this.rules = rules;
	}

	public String getAnnotation() {
		return annotation;
	}

	public void setAnnotation(String annotation) {
		this.annotation = annotation;
	}

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }
}
