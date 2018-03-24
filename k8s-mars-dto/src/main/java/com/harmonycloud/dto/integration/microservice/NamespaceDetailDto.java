package com.harmonycloud.dto.integration.microservice;

import java.io.Serializable;

/**
 * @Author jiangmi
 * @Description
 * @Date created in 2018-1-2
 * @Modified
 */
public class NamespaceDetailDto implements Serializable{

    private String tenant_id;

    private String cluster_id;

    private String cluster_name;

    private String id;

    private String namespace_id;

    private String namespace_name;

    private String shared;

    private String external_loadbalancer_ip;

    private String created;

    private String updated;

    private String namespace_alias;

    public String getNamespace_alias() {
        return namespace_alias;
    }

    public void setNamespace_alias(String namespace_alias) {
        this.namespace_alias = namespace_alias;
    }

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

    public String getCluster_name() {
        return cluster_name;
    }

    public void setCluster_name(String cluster_name) {
        this.cluster_name = cluster_name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNamespace_id() {
        return namespace_id;
    }

    public void setNamespace_id(String namespace_id) {
        this.namespace_id = namespace_id;
    }

    public String getNamespace_name() {
        return namespace_name;
    }

    public void setNamespace_name(String namespace_name) {
        this.namespace_name = namespace_name;
    }

    public String getShared() {
        return shared;
    }

    public void setShared(String shared) {
        this.shared = shared;
    }

    public String getExternal_loadbalancer_ip() {
        return external_loadbalancer_ip;
    }

    public void setExternal_loadbalancer_ip(String external_loadbalancer_ip) {
        this.external_loadbalancer_ip = external_loadbalancer_ip;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

}
