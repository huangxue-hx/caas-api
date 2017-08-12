package com.harmonycloud.dao.tenant.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

public class TenantBinding  implements Serializable{
    private Integer id;

    private String tenantId;

    private String tenantName;

    private String tmUsernames;

    private String harborProjects;

    private String networkIds;

    private String k8sPvs;

    private String k8sNamespaces;
    
    private List<String> k8sNamespaceList;
    
    private List<String> k8sPvList;
    
    private List<String> networkIdList;
    
    private List<String> harborProjectList;
    
    private List<String> tmUsernameList;

    private Date createTime;

    private Date updateTime;

    private String annotation;
    
    private String role;

    private Integer clusterId;

    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }
    public List<String> getTmUsernameList() {
        if(tmUsernameList == null) {
            tmUsernameList = new ArrayList<String>();
        }
        return tmUsernameList;
    }
    public void setTmUsernameList(List<String> tmUsernameList) {
        this.tmUsernameList = tmUsernameList;
    }
    public String getTmUsernames() {
        if(CollectionUtils.isEmpty(tmUsernameList)) {
            return "";
        }
        StringBuilder s = new StringBuilder();
        for(String tmUsername : tmUsernameList) {
            s.append(tmUsername);
            s.append(",");
        }
        String retVal = s.toString();
        if (retVal.length() > 1){
            retVal = retVal.substring(0, retVal.length()-1);
        }
        return retVal;
    }

    public void setTmUsernames(String tmUsernames) {
        if(StringUtils.isEmpty(tmUsernames)) {
            return;
        }
        String[] tmUsernameStrArr = tmUsernames.split(",");
        for(String tmUsernameStr : tmUsernameStrArr) {
            if(StringUtils.isEmpty(tmUsernameStr)) {
                continue;
            }
            getTmUsernameList().add(tmUsernameStr);
        }
    }

    public List<String> getHarborProjectList() {
        if(harborProjectList == null) {
            harborProjectList = new ArrayList<String>();
        }
        return harborProjectList;
    }

    public void setHarborProjectList(List<String> harborProjectList) {
        this.harborProjectList = harborProjectList;
    }

    public String getHarborProjects() {
        if(CollectionUtils.isEmpty(harborProjectList)) {
            return "";
        }
        StringBuilder s = new StringBuilder();
        for(String harborProject : harborProjectList) {
            s.append(harborProject);
            s.append(",");
        }
        String retVal = s.toString();
        if (retVal.length() > 1){
            retVal = retVal.substring(0, retVal.length()-1);
        }
        return retVal;
    }

    public void setHarborProjects(String harborProjects) {
        if(StringUtils.isEmpty(harborProjects)) {
            return;
        }
        String[] harborProjectStrArr = harborProjects.split(",");
        for(String harborProjectStr : harborProjectStrArr) {
            if(StringUtils.isEmpty(harborProjectStr)) {
                continue;
            }
            getHarborProjectList().add(harborProjectStr);
        }
    }

    public String getNetworkIds() {
        if(CollectionUtils.isEmpty(networkIdList)) {
            return "";
        }
        StringBuilder s = new StringBuilder();
        for(String networkId : networkIdList) {
            s.append(networkId);
            s.append(",");
        }
        String retVal = s.toString();
        if (retVal.length() > 1){
            retVal = retVal.substring(0, retVal.length()-1);
        }
        return retVal;
    }

    public void setNetworkIds(String networkIds) {
        if(StringUtils.isEmpty(networkIds)) {
            return;
        }
        String[] networkIdStrArr = networkIds.split(",");
        for(String networkIdStr : networkIdStrArr) {
            if(StringUtils.isEmpty(networkIdStr)) {
                continue;
            }
            getNetworkIdList().add(networkIdStr);
        }
    }

    public List<String> getNetworkIdList() {
        if(networkIdList == null) {
            networkIdList = new ArrayList<String>();
        }
        return networkIdList;
    }

    public void setNetworkIdList(List<String> networkIdList) {
        this.networkIdList = networkIdList;
    }

    public String getK8sPvs() {
        if(CollectionUtils.isEmpty(k8sPvList)) {
            return "";
        }
        StringBuilder s = new StringBuilder();
        for(String k8sPv : k8sPvList) {
            s.append(k8sPv);
            s.append(",");
        }
        String retVal = s.toString();
        if (retVal.length() > 1){
            retVal = retVal.substring(0, retVal.length()-1);
        }
        return retVal;
    }

    public void setK8sPvs(String k8sPvs) {
        if(StringUtils.isEmpty(k8sPvs)) {
            return;
        }
        String[] k8sPvsStrArr = k8sPvs.split(",");
        for(String k8sPvsStr : k8sPvsStrArr) {
            if(StringUtils.isEmpty(k8sPvsStr)) {
                continue;
            }
            getK8sPvList().add(k8sPvsStr);
        }
    }

    public List<String> getK8sPvList() {
        if(k8sPvList == null) {
            k8sPvList = new ArrayList<String>();
        }
        return k8sPvList;
    }

    public void setK8sPvList(List<String> k8sPvList) {
        this.k8sPvList = k8sPvList;
    }

    public String getK8sNamespaces() {
        if(CollectionUtils.isEmpty(k8sNamespaceList)) {
            return "";
        }
        StringBuilder s = new StringBuilder();
        for(String k8sNamespace : k8sNamespaceList) {
            s.append(k8sNamespace);
            s.append(",");
        }
        String retVal = s.toString();
        if (retVal.length() > 1){
            retVal = retVal.substring(0, retVal.length()-1);
        }
        return retVal;
    }

    public void setK8sNamespaces(String k8sNamespaces) {
        if(StringUtils.isEmpty(k8sNamespaces)) {
            return;
        }
        String[] k8sNamespaceStrArr = k8sNamespaces.split(",");
        for(String k8sNamespaceStr : k8sNamespaceStrArr) {
            if(StringUtils.isEmpty(k8sNamespaceStr)) {
                continue;
            }
            getK8sNamespaceList().add(k8sNamespaceStr);
        }
    }

    public List<String> getK8sNamespaceList() {
        if(k8sNamespaceList == null) {
            k8sNamespaceList = new ArrayList<String>();
        }
        return k8sNamespaceList;
    }

    public void setK8sNamespaceList(List<String> k8sNamespaceList) {
        this.k8sNamespaceList = k8sNamespaceList;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId == null ? null : tenantId.trim();
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName == null ? null : tenantName.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getAnnotation() {
        return annotation;
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation == null ? null : annotation.trim();
    }

    public Integer getClusterId() {
        return clusterId;
    }

    public void setClusterId(Integer clusterId) {
        this.clusterId = clusterId;
    }
}