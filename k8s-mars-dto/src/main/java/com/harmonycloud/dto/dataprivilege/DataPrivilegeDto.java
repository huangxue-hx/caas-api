package com.harmonycloud.dto.dataprivilege;

/**
 * Created by anson on 18/6/21.
 */
public class DataPrivilegeDto {
    String data;
    String projectId;
    String clusterId;
    String namespace;
    int dataResourceType;
    String parentData;
    Integer parentDataResourceType;


    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public int getDataResourceType() {
        return dataResourceType;
    }

    public void setDataResourceType(int dataResourceType) {
        this.dataResourceType = dataResourceType;
    }

    public String getParentData() {
        return parentData;
    }

    public void setParentData(String parentData) {
        this.parentData = parentData;
    }

    public Integer getParentDataResourceType() {
        return parentDataResourceType;
    }

    public void setParentDataResourceType(Integer parentDataResourceType) {
        this.parentDataResourceType = parentDataResourceType;
    }
}
