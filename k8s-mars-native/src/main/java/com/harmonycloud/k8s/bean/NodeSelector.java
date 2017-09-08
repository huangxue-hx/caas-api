package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NodeSelector {
    private List<NodeSelectorTerm> nodeSelectorTerms;

    public List<NodeSelectorTerm> getNodeSelectorTerms() {
        return nodeSelectorTerms;
    }

    public void setNodeSelectorTerms(List<NodeSelectorTerm> nodeSelectorTerms) {
        this.nodeSelectorTerms = nodeSelectorTerms;
    }
}
