package com.harmonycloud.dto.business;

import java.io.Serializable;

/**
 * Created by gurongyun on 17/04/07.
 * 前台提交表单topology bean
 */
public class TopologysDto implements Serializable {

	private static final long serialVersionUID = -7310894676748009353L;
	private ServiceTemplateOverviewDto source; //拓扑源
	private ServiceTemplateOverviewDto target; //拓扑目的
	private String desc; //描述
	
	public String getDesc() {
		return desc;
	}
	
	public void setDesc(String desc) {
		this.desc = desc;
	}

	public ServiceTemplateOverviewDto getSource() {
		return source;
	}

	public void setSource(ServiceTemplateOverviewDto source) {
		this.source = source;
	}

	public ServiceTemplateOverviewDto getTarget() {
		return target;
	}

	public void setTarget(ServiceTemplateOverviewDto target) {
		this.target = target;
	}

	
}
