package com.harmonycloud.dto.tenant;

/**
 * @author xc
 * @date 2018/6/28 14:58
 */
public class StorageClassQuotaDto {
    private String name;

    private String quota = "0";

    private String totalQuota = "0";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getQuota() {
        return quota;
    }

    public void setQuota(String quota) {
        this.quota = quota;
    }

    public String getTotalQuota() {
        return totalQuota;
    }

    public void setTotalQuota(String totalQuota) {
        this.totalQuota = totalQuota;
    }
}
