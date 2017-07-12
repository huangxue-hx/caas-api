package com.harmonycloud.dto.business;

import java.util.List;

/**
 * Created by root on 7/9/17.
 */
public class JobsDetailDto {

    private String name;

    private String namespace;

    private String labels;

    private String annotation;
    
    private String nodeSelector;

    private Integer activeDeadlineSeconds;

    private Integer completions;

    private boolean manualSelector;
    
    private String restartPolicy;

    private Integer parallelism;

    private List<CreateContainerDto> containers;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getLabels() {
        return labels;
    }

    public void setLabels(String labels) {
        this.labels = labels;
    }

    public String getAnnotation() {
        return annotation;
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }

    public Integer getActiveDeadlineSeconds() {
        return activeDeadlineSeconds;
    }

    public void setActiveDeadlineSeconds(Integer activeDeadlineSeconds) {
        this.activeDeadlineSeconds = activeDeadlineSeconds;
    }

    public Integer getCompletions() {
        return completions;
    }

    public void setCompletions(Integer completions) {
        this.completions = completions;
    }

    public boolean isManualSelector() {
        return manualSelector;
    }

    public void setManualSelector(boolean manualSelector) {
        this.manualSelector = manualSelector;
    }

    public Integer getParallelism() {
        return parallelism;
    }

    public void setParallelism(Integer parallelism) {
        this.parallelism = parallelism;
    }

    public List<CreateContainerDto> getContainers() {
        return containers;
    }

    public void setContainers(List<CreateContainerDto> containers) {
        this.containers = containers;
    }

	public String getNodeSelector() {
		return nodeSelector;
	}

	public void setNodeSelector(String nodeSelector) {
		this.nodeSelector = nodeSelector;
	}

	public String getRestartPolicy() {
		return restartPolicy;
	}

	public void setRestartPolicy(String restartPolicy) {
		this.restartPolicy = restartPolicy;
	}
}
