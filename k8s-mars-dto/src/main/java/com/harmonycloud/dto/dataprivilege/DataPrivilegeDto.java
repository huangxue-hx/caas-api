package com.harmonycloud.dto.dataprivilege;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.DataPrivilegeField;
import com.harmonycloud.common.enumm.DataPrivilegeType;
import com.harmonycloud.common.enumm.DataResourceTypeEnum;

/**
 * Created by anson on 18/6/21.
 */
@DataPrivilegeType(type = DataResourceTypeEnum.APPLICATION)
public class DataPrivilegeDto {
    @DataPrivilegeField(type = CommonConstant.DATA_FIELD)
    String data;
    @DataPrivilegeField(type = CommonConstant.PROJECTID_FIELD)
    String projectId;
    @DataPrivilegeField(type = CommonConstant.CLUSTERID_FIELD)
    String clusterId;
    @DataPrivilegeField(type = CommonConstant.NAMESPACE_FIELD)
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
