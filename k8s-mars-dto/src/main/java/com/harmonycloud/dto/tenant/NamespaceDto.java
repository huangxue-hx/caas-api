package com.harmonycloud.dto.tenant;

/**
 * Created by andy on 17-1-20.
 */
public class NamespaceDto {

    private String name;

    private String tenantid;

    private String annotation;
    
    private String nodename;
    
    private String lastlastmemory;

    private String lastlastcpu;
    
    private Boolean Private = false;

    private NetworkDto network;

    private QuotaDto quota;

    public String getLastlastmemory() {
        return lastlastmemory;
    }

    public void setLastlastmemory(String lastlastmemory) {
        this.lastlastmemory = lastlastmemory;
    }

    public String getLastlastcpu() {
        return lastlastcpu;
    }

    public void setLastlastcpu(String lastlastcpu) {
        this.lastlastcpu = lastlastcpu;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTenantid() {
        return tenantid;
    }

    public void setTenantid(String tenantid) {
        this.tenantid = tenantid;
    }

    public String getAnnotation() {
        return annotation;
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }

    public NetworkDto getNetwork() {
        return network;
    }

    public void setNetwork(NetworkDto network) {
        this.network = network;
    }

    public QuotaDto getQuota() {
        return quota;
    }

    public void setQuota(QuotaDto quota) {
        this.quota = quota;
    }
    public boolean isPrivate() {
        return Private;
    }

    public void setPrivate(boolean private1) {
        Private = private1;
    }

    public String getNodename() {
        return nodename;
    }

    public void setNodename(String nodename) {
        this.nodename = nodename;
    }
}
