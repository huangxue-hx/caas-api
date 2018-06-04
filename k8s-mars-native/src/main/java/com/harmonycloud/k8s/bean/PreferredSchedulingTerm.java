package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PreferredSchedulingTerm {
    private Integer weight;

    private NodeSelectorTerm preference;

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public NodeSelectorTerm getPreference() {
        return preference;
    }

    public void setPreference(NodeSelectorTerm preference) {
        this.preference = preference;
    }
}
