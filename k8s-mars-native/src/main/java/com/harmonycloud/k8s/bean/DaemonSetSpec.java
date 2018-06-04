package com.harmonycloud.k8s.bean;

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
public class DaemonSetSpec {

    private Integer minReadySeconds;

    private Integer revisionHistoryLimit;

    private LabelSelector selector;

    private PodTemplateSpec template;

    private Integer templateGeneration;

    private DaemonSetUpdateStrategy updateStrategy;

    public Integer getMinReadySeconds() {
        return minReadySeconds;
    }

    public void setMinReadySeconds(Integer minReadySeconds) {
        this.minReadySeconds = minReadySeconds;
    }

    public Integer getRevisionHistoryLimit() {
        return revisionHistoryLimit;
    }

    public void setRevisionHistoryLimit(Integer revisionHistoryLimit) {
        this.revisionHistoryLimit = revisionHistoryLimit;
    }

    public LabelSelector getSelector() {
        return selector;
    }

    public void setSelector(LabelSelector selector) {
        this.selector = selector;
    }

    public PodTemplateSpec getTemplate() {
        return template;
    }

    public void setTemplate(PodTemplateSpec template) {
        this.template = template;
    }

    public DaemonSetUpdateStrategy getUpdateStrategy() {
        return updateStrategy;
    }

    public void setUpdateStrategy(DaemonSetUpdateStrategy updateStrategy) {
        this.updateStrategy = updateStrategy;
    }

    public Integer getTemplateGeneration() {
        return templateGeneration;
    }

    public void setTemplateGeneration(Integer templateGeneration) {
        this.templateGeneration = templateGeneration;
    }
}
