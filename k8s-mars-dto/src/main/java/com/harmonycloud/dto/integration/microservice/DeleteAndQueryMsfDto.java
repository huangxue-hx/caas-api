package com.harmonycloud.dto.integration.microservice;

import java.util.List;

/**
 * @Author jiangmi
 * @Description 微服务删除和查询实例状态的接口参数对象
 * @Date created in 2017-12-5
 * @Modified
 */
public class DeleteAndQueryMsfDto {

    private String tenant_id;

    private String space_id;

    private List<String> instance_ids;

    private List<String> tenant_ids;

    public List<String> getTenant_ids() {
        return tenant_ids;
    }

    public void setTenant_ids(List<String> tenant_ids) {
        this.tenant_ids = tenant_ids;
    }

    public String getTenant_id() {
        return tenant_id;
    }

    public void setTenant_id(String tenant_id) {
        this.tenant_id = tenant_id;
    }

    public String getSpace_id() {
        return space_id;
    }

    public void setSpace_id(String space_id) {
        this.space_id = space_id;
    }

    public List<String> getInstance_ids() {
        return instance_ids;
    }

    public void setInstance_ids(List<String> instance_ids) {
        this.instance_ids = instance_ids;
    }
}
