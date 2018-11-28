package com.harmonycloud.service.platform.bean.harbor;
import io.swagger.annotations.ApiModelProperty;

import java.util.Map;
import java.util.List;

public class HarborReplicationPolicy {
	private String harborHost;
	private Integer harborProjectId;
	private Integer targetId;
	private String name;
	private String description;
	private Boolean replicateNow;
	private Boolean replicateDeletion;
	private String repositories;
	private String tags;
	private List<Integer> labels;
	private String trigger;
	private HarborPolicyScheduledTrigger scheduled;
	private String target_project_name;

	public String getRepositories() {
		return repositories;
	}

	public void setRepositories(String repositories) {
		this.repositories = repositories;
	}

	public HarborPolicyScheduledTrigger getScheduled() {
		return scheduled;
	}

	public void setScheduled(HarborPolicyScheduledTrigger scheduled) {
		this.scheduled = scheduled;
	}

	public String getTrigger() {
		return trigger;
	}

	public void setTrigger(String trigger) {
		this.trigger = trigger;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public List<Integer> getLabels() {
		return labels;
	}

	public void setLabels(List<Integer> labels) {
		this.labels = labels;
	}

	public Boolean getReplicateNow() {
		return replicateNow;
	}

	public void setReplicateNow(Boolean replicateNow) {
		this.replicateNow = replicateNow;
	}

	public Boolean getReplicateDeletion() {
		return replicateDeletion;
	}

	public void setReplicateDeletion(Boolean replicateDeletion) {
		this.replicateDeletion = replicateDeletion;
	}

	public Integer getTargetId() {
		return targetId;
	}

	public void setTargetId(Integer targetId) {
		this.targetId = targetId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getHarborHost() {
		return harborHost;
	}

	public void setHarborHost(String harborHost) {
		this.harborHost = harborHost;
	}

	public Integer getHarborProjectId() {
		return harborProjectId;
	}

	public void setHarborProjectId(Integer harborProjectId) {
		this.harborProjectId = harborProjectId;
	}

	public String getTarget_project_name() { return target_project_name; }

	public void setTarget_project_name(String target_project_name) { this.target_project_name = target_project_name; }
}