package com.harmonycloud.dto.cluster;

import com.harmonycloud.k8s.bean.NodeCondition;

import java.util.ArrayList;
import java.util.List;

public class NodeBriefDto {
    private String clusterId;
    private String name;
    private List<NodeCondition> conditions;

    public NodeBriefDto() {
        super();
    }

    public NodeBriefDto(String clusterId, String name) {
        this.clusterId = clusterId;
        this.name = name;
        this.conditions = new ArrayList<>();
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<NodeCondition> getConditions() {
        return conditions;
    }

    public void setConditions(List<NodeCondition> conditions) {
        this.conditions = conditions;
    }
}
