package com.harmonycloud.k8s.bean;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @Author jiangmi
 * @Description
 * @Date created in 2017-12-18
 * @Modified
 */
@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DaemonSetStatus {

    private Integer collisionCount;

    private Integer currentNumberScheduled;

    private Integer desiredNumberScheduled;

    private Integer numberAvailable;

    private Integer numberMisscheduled;

    private Integer numberReady;

    private Integer numberUnavailable;

    private Integer observedGeneration;

    private Integer updatedNumberScheduled;
    
    private List<DaemonSetCondition> conditions;

    public Integer getCollisionCount() {
        return collisionCount;
    }

    public void setCollisionCount(Integer collisionCount) {
        this.collisionCount = collisionCount;
    }

    public Integer getCurrentNumberScheduled() {
        return currentNumberScheduled;
    }

    public void setCurrentNumberScheduled(Integer currentNumberScheduled) {
        this.currentNumberScheduled = currentNumberScheduled;
    }

    public Integer getDesiredNumberScheduled() {
        return desiredNumberScheduled;
    }

    public void setDesiredNumberScheduled(Integer desiredNumberScheduled) {
        this.desiredNumberScheduled = desiredNumberScheduled;
    }

    public Integer getNumberAvailable() {
        return numberAvailable;
    }

    public void setNumberAvailable(Integer numberAvailable) {
        this.numberAvailable = numberAvailable;
    }

    public Integer getNumberMisscheduled() {
        return numberMisscheduled;
    }

    public void setNumberMisscheduled(Integer numberMisscheduled) {
        this.numberMisscheduled = numberMisscheduled;
    }

    public Integer getNumberReady() {
        return numberReady;
    }

    public void setNumberReady(Integer numberReady) {
        this.numberReady = numberReady;
    }

    public Integer getNumberUnavailable() {
        return numberUnavailable;
    }

    public void setNumberUnavailable(Integer numberUnavailable) {
        this.numberUnavailable = numberUnavailable;
    }

    public Integer getObservedGeneration() {
        return observedGeneration;
    }

    public void setObservedGeneration(Integer observedGeneration) {
        this.observedGeneration = observedGeneration;
    }

    public Integer getUpdatedNumberScheduled() {
        return updatedNumberScheduled;
    }

    public void setUpdatedNumberScheduled(Integer updatedNumberScheduled) {
        this.updatedNumberScheduled = updatedNumberScheduled;
    }

	public List<DaemonSetCondition> getConditions() {
		return conditions;
	}

	public void setConditions(List<DaemonSetCondition> conditions) {
		this.conditions = conditions;
	}
}