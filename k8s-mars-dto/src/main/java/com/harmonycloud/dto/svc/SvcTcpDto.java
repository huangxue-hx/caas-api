package com.harmonycloud.dto.svc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.harmonycloud.dto.business.TcpRuleDto;
import com.harmonycloud.dto.svc.SelectorDto;

public class SvcTcpDto {

    private String namespace ;
	
    private String name;
    
    private Map<String, Object> labels;
    
    private String createTime;
    
    private String app;
    
    private String annotaion;
    
    private SelectorDto selector;
    
    private List<TcpRuleDto> rules = new ArrayList<>();
    
    private String tenantId;
    
    public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}
    
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

	public String getAnnotaion() {
		return annotaion;
	}

	public void setAnnotaion(String annotaion) {
		this.annotaion = annotaion;
	}



	

}
