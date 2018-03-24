package com.harmonycloud.service.platform.bean.microservice;

import java.util.List;

/**
 * @Author jiangmi
 * @Description 部署微服务接口请求参数
 * @Date created in 2017-12-8
 * @Modified
 */
public class DeployMsfDto {

    private String tenant_id;

    private String cluster_id;

    private String space_id;

    private List<MsfDeployment> deployments;

    public String getTenant_id() {
        return tenant_id;
    }

    public void setTenant_id(String tenant_id) {
        this.tenant_id = tenant_id;
    }

    public String getCluster_id() {
        return cluster_id;
    }

    public void setCluster_id(String cluster_id) {
        this.cluster_id = cluster_id;
    }

    public String getSpace_id() {
        return space_id;
    }

    public void setSpace_id(String space_id) {
        this.space_id = space_id;
    }

    public List<MsfDeployment> getDeployments() {
        return deployments;
    }

    public void setDeployments(List<MsfDeployment> deployments) {
        this.deployments = deployments;
    }

}
