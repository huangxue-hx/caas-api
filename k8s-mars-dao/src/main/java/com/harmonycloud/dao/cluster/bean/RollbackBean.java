package com.harmonycloud.dao.cluster.bean;

import java.util.List;

/**
 * Created by czm on 2017/6/8.
 */
public class RollbackBean implements Comparable<RollbackBean>{
    private String revision;
    private String revisionDetail;
    private String podTemplete;
    private String revisionTime;
    private String name;
    private List<String> configmap;
	private String current;
	private String deployVersion;

    public String getDeployVersion() {
        return deployVersion;
    }

    public void setDeployVersion(String deployVersion) {
        this.deployVersion = deployVersion;
    }

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    public String getRevisionDetail() {
        return revisionDetail;
    }

    public void setRevisionDetail(String revisionDetail) {
        this.revisionDetail = revisionDetail;
    }

    public String getPodTemplete() {
        return podTemplete;
    }

    public void setPodTemplete(String podTemplete) {
        this.podTemplete = podTemplete;
    }

    public String getRevisionTime() {
        return revisionTime;
    }

    public void setRevisionTime(String revisionTime) {
        this.revisionTime = revisionTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCurrent() {
        return current;
    }

    public void setCurrent(String current) {
        this.current = current;
    }

    public List<String> getConfigmap() {
		return configmap;
	}

	public void setConfigmap(List<String> configmap) {
		this.configmap = configmap;
	}

	@Override
    public int compareTo(RollbackBean o) {
        return Integer.parseInt(this.getRevision())-Integer.parseInt(o.getRevision());
    }
}
