package com.harmonycloud.dto.dataprivilege;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.DataPrivilegeField;
import com.harmonycloud.common.enumm.DataPrivilegeType;
import com.harmonycloud.common.enumm.DataResourceTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Created by anson on 18/6/21.
 */
@ApiModel(value="数据权限查询对象")
@DataPrivilegeType(type = DataResourceTypeEnum.APPLICATION)
public class DataPrivilegeDto {
    @ApiModelProperty(value="数据名",name="data",example="tomcat-service")
    @DataPrivilegeField(type = CommonConstant.DATA_FIELD)
    private String data;
    @ApiModelProperty(value="项目Id",name="projectId",example="aabc0a6f31d543e6a27f6042cddd91ad")
    @DataPrivilegeField(type = CommonConstant.PROJECTID_FIELD)
    private String projectId;
    @ApiModelProperty(value="集群Id",name="clusterId",example="cluster-dev")
    @DataPrivilegeField(type = CommonConstant.CLUSTERID_FIELD)
    private String clusterId;
    @ApiModelProperty(value="分区名",name="namespace",example="test-ns")
    @DataPrivilegeField(type = CommonConstant.NAMESPACE_FIELD)
    private String namespace;
    private Integer dataResourceType;
    private String parentData;
    private Integer parentDataResourceType;
    private Long creatorId;
    private Integer privilegeType;

    public Integer getPrivilegeType() {
        return privilegeType;
    }

    public void setPrivilegeType(Integer privilegeType) {
        this.privilegeType = privilegeType;
    }

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

    public Integer getDataResourceType() {
        return dataResourceType;
    }

    public void setDataResourceType(Integer dataResourceType) {
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

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

}
