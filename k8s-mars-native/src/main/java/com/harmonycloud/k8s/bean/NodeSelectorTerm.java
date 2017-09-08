package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NodeSelectorTerm {

    private List<NodeSelectorRequirement> matchExpressions;

    public List<NodeSelectorRequirement> getMatchExpressions() {
        return matchExpressions;
    }

    public void setMatchExpressions(List<NodeSelectorRequirement> matchExpressions) {
        this.matchExpressions = matchExpressions;
    }
}
