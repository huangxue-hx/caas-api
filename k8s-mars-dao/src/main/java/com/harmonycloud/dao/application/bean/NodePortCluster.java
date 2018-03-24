package com.harmonycloud.dao.application.bean;

import java.io.Serializable;

public class NodePortCluster  implements Serializable{
	private Integer unodeport;
	
    private Integer nodeportid;

    private String clusterId;

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

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterid(String clusterId) {
        this.clusterId = clusterId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}