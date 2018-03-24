package com.harmonycloud.dao.cluster.bean;

import java.util.Date;

/**
 * @Author jiangmi
 * @Description
 * @Date created in 2017-12-12
 * @Modified
 */
public class NodePortClusterUsage {

    private Integer nodeport;

    private String clusterId;

    private Integer status;

    private Date createTime;

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getNodeport() {
        return nodeport;
    }

    public void setNodeport(Integer nodeport) {
        this.nodeport = nodeport;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
