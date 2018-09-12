package com.harmonycloud.dto.application;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.DataPrivilegeField;
import io.swagger.annotations.ApiModelProperty;

/**
 * Created by anson on 18/8/9.
 */
public class StatefulSetDetailDto extends DeploymentDetailDto{
    @DataPrivilegeField(type = CommonConstant.DATA_FIELD)
    private String name;

    @DataPrivilegeField(type = CommonConstant.NAMESPACE_FIELD)
    private String namespace;

    @DataPrivilegeField(type = CommonConstant.PROJECTID_FIELD)
    private String projectId;

    @ApiModelProperty(value="启动策略",name="podManagementPolicy",example="OrderedReady, Parallel")
    private String podManagementPolicy;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    @Override
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    @Override
    public String getProjectId() {
        return projectId;
    }

    @Override
    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getPodManagementPolicy() {
        return podManagementPolicy;
    }

    public void setPodManagementPolicy(String podManagementPolicy) {
        this.podManagementPolicy = podManagementPolicy;
    }
}
