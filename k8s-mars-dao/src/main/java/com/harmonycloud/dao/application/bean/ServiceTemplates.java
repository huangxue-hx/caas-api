package com.harmonycloud.dao.application.bean;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by root on 3/29/17.
 */

public class ServiceTemplates implements Serializable {
    private Integer id;

    private String name;

    private String tag;

    private String details;

    private String imageList;

    private String ingressContent;

    private Integer status; // 1:deleted 0:not

    private String tenant;

    private String user;

    private Date createTime;

    private Integer flag; //is

    private String deploymentContent;

    private String nodeSelector;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    private static final long serialVersionUID = 1L;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag == null ? null : tag.trim();
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details == null ? null : details.trim();
    }

    public String getImageList() {
        return imageList;
    }

    public void setImageList(String imageList) {
        this.imageList = imageList == null ? null : imageList.trim();
    }

    public String getIngressContent() {
        return ingressContent;
    }

    public void setIngressContent(String ingressContent) {
        this.ingressContent = ingressContent == null ? null : ingressContent.trim();
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant == null ? null : tenant.trim();
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user == null ? null : user.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getFlag() {
        return flag;
    }

    public void setFlag(Integer flag) {
        this.flag = flag;
    }

    public String getDeploymentContent() {
        return deploymentContent;
    }

    public void setDeploymentContent(String deploymentContent) {
        this.deploymentContent = deploymentContent == null ? null : deploymentContent.trim();
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        ServiceTemplates other = (ServiceTemplates) that;
        return (this.getName() == null ? other.getName() == null : this.getName().equals(other.getName()))
            && (this.getDetails() == null ? other.getDetails() == null : this.getDetails().equals(other.getDetails()))
            && (this.getImageList() == null ? other.getImageList() == null : this.getImageList().equals(other.getImageList()))
            && (this.getIngressContent() == null ? other.getIngressContent() == null : this.getIngressContent().equals(other.getIngressContent()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getTenant() == null ? other.getTenant() == null : this.getTenant().equals(other.getTenant()))
            && (this.getFlag() == null ? other.getFlag() == null : this.getFlag().equals(other.getFlag()))
            && (this.getDeploymentContent() == null ? other.getDeploymentContent() == null : this.getDeploymentContent().equals(other.getDeploymentContent()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
        result = prime * result + ((getTag() == null) ? 0 : getTag().hashCode());
        result = prime * result + ((getDetails() == null) ? 0 : getDetails().hashCode());
        result = prime * result + ((getImageList() == null) ? 0 : getImageList().hashCode());
        result = prime * result + ((getIngressContent() == null) ? 0 : getIngressContent().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getTenant() == null) ? 0 : getTenant().hashCode());
        result = prime * result + ((getUser() == null) ? 0 : getUser().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getFlag() == null) ? 0 : getFlag().hashCode());
        result = prime * result + ((getDeploymentContent() == null) ? 0 : getDeploymentContent().hashCode());
        return result;
    }

    public String getNodeSelector() {
        return nodeSelector;
    }

    public void setNodeSelector(String nodeSelector) {
        this.nodeSelector = nodeSelector;
    }

}