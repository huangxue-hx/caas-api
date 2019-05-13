package com.harmonycloud.dao.debug.bean;

import java.util.Date;

/**
 * Created by fengjinliu on 2019/5/5.
 */
public class DebugState {
    private Integer id;
    private String username;
    private String state;
    private Date updatetime;
    private String podname;
    private String namespace;
    private String service;
    private String port;

    public String getPodname() {
        return podname;
    }

    public void setPodname(String podname) {
        this.podname = podname;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Date getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(Date updatetime) {
        this.updatetime = updatetime;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }
}
