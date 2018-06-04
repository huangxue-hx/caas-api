package com.harmonycloud.service.platform.bean.harbor;
import java.util.Map;
import java.util.List;

public class HarborReplicationPolicy {
	private String harborHost;
	private Integer harborProjectId;
    private Integer targetId;
    private String name;                     
    private String description; 
    private Integer enabled;
    private Integer partial;


	public Integer getPartial() {
		return partial;
	}

	public void setPartial(Integer partial) {
		this.partial = partial;
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
	public Integer getEnabled() {
		return enabled;
	}
	public void setEnabled(Integer enabled) {
		this.enabled = enabled;
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
}