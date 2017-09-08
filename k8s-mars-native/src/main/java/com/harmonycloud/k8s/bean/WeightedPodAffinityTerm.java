package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.models.auth.In;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WeightedPodAffinityTerm {
    private Integer weight;

    private PodAffinityTerm podAffinityTerm;

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public PodAffinityTerm getPodAffinityTerm() {
        return podAffinityTerm;
    }

    public void setPodAffinityTerm(PodAffinityTerm podAffinityTerm) {
        this.podAffinityTerm = podAffinityTerm;
    }
}
