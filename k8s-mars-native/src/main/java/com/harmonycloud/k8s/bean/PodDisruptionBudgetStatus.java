package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PodDisruptionBudgetStatus {
    private Integer currentHealthy;

    private Integer desiredHealthy;

    private Object disruptedPods;

    private Integer disruptionsAllowed;

    private Integer expectedPods;

    private Integer observedGeneration;


    public Integer getCurrentHealthy() {
        return currentHealthy;
    }

    public void setCurrentHealthy(Integer currentHealthy) {
        this.currentHealthy = currentHealthy;
    }

    public Integer getDesiredHealthy() {
        return desiredHealthy;
    }

    public void setDesiredHealthy(Integer desiredHealthy) {
        this.desiredHealthy = desiredHealthy;
    }

    public Object getDisruptedPods() {
        return disruptedPods;
    }

    public void setDisruptedPods(Object disruptedPods) {
        this.disruptedPods = disruptedPods;
    }

    public Integer getDisruptionsAllowed() {
        return disruptionsAllowed;
    }

    public void setDisruptionsAllowed(Integer disruptionsAllowed) {
        this.disruptionsAllowed = disruptionsAllowed;
    }

    public Integer getExpectedPods() {
        return expectedPods;
    }

    public void setExpectedPods(Integer expectedPods) {
        this.expectedPods = expectedPods;
    }

    public Integer getObservedGeneration() {
        return observedGeneration;
    }

    public void setObservedGeneration(Integer observedGeneration) {
        this.observedGeneration = observedGeneration;
    }
}
