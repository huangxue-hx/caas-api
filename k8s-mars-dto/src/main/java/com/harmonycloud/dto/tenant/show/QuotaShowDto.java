package com.harmonycloud.dto.tenant.show;

import java.util.List;

import com.harmonycloud.dto.tenant.NetworkDto;

/**
 * Created by andy on 17-2-7.
 */
public class QuotaShowDto {

    private QuotaDetailShowDto quota;

    private NetworkDto network;

    private String annotation;

    private String name;

    private String time;

    private String tenantid;

    private List<String> tenantName;

    public List<String> getTenantName() {
        return tenantName;
    }

    public void setTenantName(List<String> tenantName) {
        this.tenantName = tenantName;
    }

    public QuotaDetailShowDto getQuota() {
        return quota;
    }

    public void setQuota(QuotaDetailShowDto quota) {
        this.quota = quota;
    }

    public NetworkDto getNetwork() {
        return network;
    }

    public void setNetwork(NetworkDto network) {
        this.network = network;
    }

    public String getAnnotation() {
        return annotation;
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTenantid() {
        return tenantid;
    }

    public void setTenantid(String tenantid) {
        this.tenantid = tenantid;
    }
}
