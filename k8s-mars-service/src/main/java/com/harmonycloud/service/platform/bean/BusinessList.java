package com.harmonycloud.service.platform.bean;

import java.io.Serializable;
import java.util.List;

/**
 * Created by root on 4/11/17.
 */
public class BusinessList implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -2530377623004360744L;
    private List<String> idList;
    private String tenantId;

    public List<String> getIdList() {
        return idList;
    }

    public void setIdList(List<String> idList) {
        this.idList = idList;
    }

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}
}
