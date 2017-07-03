package com.harmonycloud.dao.network.bean;

import java.io.Serializable;
import java.util.Date;

public class NetworkTopology implements Serializable{
    private Integer id;

    private String netId;

    private String netName;

    private String topology;

    private Date createtime;

    private Date updatetime;

    private String destinationid;

    private String destinationname;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNetId() {
        return netId;
    }

    public void setNetId(String netId) {
        this.netId = netId == null ? null : netId.trim();
    }

    public String getNetName() {
        return netName;
    }

    public void setNetName(String netName) {
        this.netName = netName == null ? null : netName.trim();
    }

    public String getTopology() {
        return topology;
    }

    public void setTopology(String topology) {
        this.topology = topology == null ? null : topology.trim();
    }

    public Date getCreatetime() {
        return createtime;
    }

    public void setCreatetime(Date createtime) {
        this.createtime = createtime;
    }

    public Date getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(Date updatetime) {
        this.updatetime = updatetime;
    }

    public String getDestinationid() {
        return destinationid;
    }

    public void setDestinationid(String destinationid) {
        this.destinationid = destinationid == null ? null : destinationid.trim();
    }

    public String getDestinationname() {
        return destinationname;
    }

    public void setDestinationname(String destinationname) {
        this.destinationname = destinationname == null ? null : destinationname.trim();
    }
}