package com.harmonycloud.dto.tenant;

import java.io.Serializable;

/**
 * Created by andy on 17-1-18.
 */
public class TenantDto implements Serializable{

    private static final long serialVersionUID = -6141661628695983123L;

    private String name;

    private String tenantId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
}
