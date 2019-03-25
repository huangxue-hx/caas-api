package com.harmonycloud.dto.cluster;

import java.util.List;


/**
 * @author youpeiyuan
 *分区绑定dto
 */
public class BindNameSpaceDto {

    /**
     * 原分区
     */
    private String oldNameSpace;

    /**
     * 绑定的分区别名
     */
    private String aliasName;

    /**
     * 绑定的分区名称名称
     */
    private String name;

    /**
     * 指定分区指定服务时迁移服务所需要的属性
     */
    private List<DeploymentDto> deploymentDto;




    public String getOldNameSpace() {
        return oldNameSpace;
    }

    public void setOldNameSpace(String oldNameSpace) {
        this.oldNameSpace = oldNameSpace;
    }

    public String getAliasName() {
        return aliasName;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<DeploymentDto> getDeploymentDto() {
        return deploymentDto;
    }

    public void setDeploymentDto(List<DeploymentDto> deploymentDto) {
        this.deploymentDto = deploymentDto;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("BindNameSpaceDto [oldNameSpace=");
        builder.append(oldNameSpace);
        builder.append(", aliasName=");
        builder.append(aliasName);
        builder.append(", name=");
        builder.append(name);
        builder.append(", deploymentDto=");
        builder.append(deploymentDto);
        builder.append("]");
        return builder.toString();
    }

}
