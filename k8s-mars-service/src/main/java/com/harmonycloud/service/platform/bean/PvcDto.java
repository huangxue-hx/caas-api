package com.harmonycloud.service.platform.bean;

import java.io.Serializable;
import java.util.Date;

/**
 * @author xc
 * @date 2018/7/5 14:08
 */
public class PvcDto implements Serializable {

    private String name;

    private String clusterName;

    private String namespace;

    //容量
    private String capacity;
    //状态
    private String status;
    //绑定的服务（多个的话以“，”分割）
    private String bindingServices;

    private Date createTime;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getCapacity() {
        return capacity;
    }

    public void setCapacity(String capacity) {
        this.capacity = capacity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBindingServices() {
        return bindingServices;
    }

    public void setBindingServices(String bindingServices) {
        this.bindingServices = bindingServices;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
