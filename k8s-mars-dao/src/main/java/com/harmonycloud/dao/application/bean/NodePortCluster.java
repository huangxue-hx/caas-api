package com.harmonycloud.dao.application.bean;

public class NodePortCluster {
	private Integer unodeport;
	
    private Integer nodeportid;

    private Integer clusterid;

    private Integer status;

    public Integer getUnodeport() {
		return unodeport;
	}

	public void setUnodeport(Integer unodeport) {
		this.unodeport = unodeport;
	}

	public Integer getNodeportid() {
        return nodeportid;
    }

    public void setNodeportid(Integer nodeportid) {
        this.nodeportid = nodeportid;
    }

    public Integer getClusterid() {
        return clusterid;
    }

    public void setClusterid(Integer clusterid) {
        this.clusterid = clusterid;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}