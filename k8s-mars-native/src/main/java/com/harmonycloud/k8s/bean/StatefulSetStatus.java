package com.harmonycloud.k8s.bean;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * @author yekan
 *
 */
@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class StatefulSetStatus {

    private Integer collisionCount;

    private List<StatefulSetCondition> conditions;

    private Integer currentReplicas;

    private String currentRevision;

    private Integer observedGeneration;

    private Integer readyReplicas;

    private Integer replicas;

    private String updateRevision;

    private Integer updatedReplicas;

    public StatefulSetStatus() {
    }

    public Integer getCollisionCount() {
        return collisionCount;
    }

    public void setCollisionCount(Integer collisionCount) {
        this.collisionCount = collisionCount;
    }

    public List<StatefulSetCondition> getConditions() {
        return conditions;
    }

    public void setConditions(List<StatefulSetCondition> conditions) {
        this.conditions = conditions;
    }

    public Integer getCurrentReplicas() {
        return currentReplicas;
    }

    public void setCurrentReplicas(Integer currentReplicas) {
        this.currentReplicas = currentReplicas;
    }

    public String getCurrentRevision() {
        return currentRevision;
    }

    public void setCurrentRevision(String currentRevision) {
        this.currentRevision = currentRevision;
    }

    public Integer getObservedGeneration() {
        return observedGeneration;
    }

    public void setObservedGeneration(Integer observedGeneration) {
        this.observedGeneration = observedGeneration;
    }

    public Integer getReadyReplicas() {
        return readyReplicas;
    }

    public void setReadyReplicas(Integer readyReplicas) {
        this.readyReplicas = readyReplicas;
    }

    public Integer getReplicas() {
        return replicas;
    }

    public void setReplicas(Integer replicas) {
        this.replicas = replicas;
    }

    public String getUpdateRevision() {
        return updateRevision;
    }

    public void setUpdateRevision(String updateRevision) {
        this.updateRevision = updateRevision;
    }

    public Integer getUpdatedReplicas() {
        return updatedReplicas;
    }

    public void setUpdatedReplicas(Integer updatedReplicas) {
        this.updatedReplicas = updatedReplicas;
    }
}
