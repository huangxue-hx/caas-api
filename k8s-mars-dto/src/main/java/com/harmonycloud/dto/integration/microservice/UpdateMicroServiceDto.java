package com.harmonycloud.dto.integration.microservice;

import com.harmonycloud.dao.microservice.bean.MicroServiceInstance;

import java.util.List;

/**
 * @Author jiangmi
 * @Description
 * @Date created in 2017-12-4
 * @Modified
 */
public class UpdateMicroServiceDto {

    private String tenant_id;

    private String cluster_id;

    private String space_id;

    private List<UpdateMsfInstanceDto> instances;

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


    public List<UpdateMsfInstanceDto> getInstances() {
        return instances;
    }

    public void setInstances(List<UpdateMsfInstanceDto> instances) {
        this.instances = instances;
    }

}
