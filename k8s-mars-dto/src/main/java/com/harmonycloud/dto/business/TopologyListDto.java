package com.harmonycloud.dto.business;

import java.util.List;

public class TopologyListDto {
	private int businessTemplateId;
	private List<TopologysDto> topologyList; // 拓扑关系list

	public List<TopologysDto> getTopologyList() {
		return topologyList;
	}

	public void setTopologyList(List<TopologysDto> topologyList) {
		this.topologyList = topologyList;
	}

	public int getBusinessTemplateId() {
		return businessTemplateId;
	}

	public void setBusinessTemplateId(int businessTemplateId) {
		this.businessTemplateId = businessTemplateId;
	}
}
