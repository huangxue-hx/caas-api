package com.harmonycloud.dao.cluster.bean;

import java.util.Date;

/**
 * @Author jiangmi
 * @Description nodePort每个集群的端口范围
 * @Date created in 2017-12-12
 * @Modified
 */
public class NodePortClusterRange {

    private Integer id;

    private Integer startPort;

    private Integer endPort;

    private boolean repeat;

    private Date createTime;

    private String clusterId;

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getStartPort() {
        return startPort;
    }

    public void setStartPort(Integer startPort) {
        this.startPort = startPort;
    }

    public Integer getEndPort() {
        return endPort;
    }

    public void setEndPort(Integer endPort) {
        this.endPort = endPort;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
