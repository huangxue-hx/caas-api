package com.harmonycloud.dto.tenant;

import java.io.Serializable;

/**
 * Created by andy on 17-1-18.
 */
public class HarborProjectDto implements Serializable {

    private static final long serialVersionUID = -2857058039054107917L;

    private String name;

    private Long harborid;

    private String time;

    private TenantDto tenant;

    private Float   use_size;
    private Float  use_rate;
    // private Integer use_num;
    private Float   quota_size;

    private Integer repository_num;

    private Integer isPublic;

    private String harborHost;

    public Integer getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Integer isPublic) {
        this.isPublic = isPublic;
    }

    public Integer getRepository_num() {
        return repository_num;
    }

    public void setRepository_num(Integer repository_num) {
        this.repository_num = repository_num;
    }

    public Float getUse_size() {
        return use_size;
    }

    public void setUse_size(Float use_size) {
        this.use_size = use_size;
    }

    public Float getUse_rate() {
        return use_rate;
    }

    public void setUse_rate(Float use_rate) {
        this.use_rate = use_rate;
    }

    public Float getQuota_size() {
        return quota_size;
    }

    public void setQuota_size(Float quota_size) {
        this.quota_size = quota_size;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getHarborid() {
        return harborid;
    }

    public void setHarborid(Long harborid) {
        this.harborid = harborid;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public TenantDto getTenant() {
        return tenant;
    }

    public void setTenant(TenantDto tenant) {
        this.tenant = tenant;
    }

    public String getHarborHost() {
        return harborHost;
    }

    public void setHarborHost(String harborHost) {
        this.harborHost = harborHost;
    }
}
