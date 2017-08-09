package com.harmonycloud.dto.business;

import java.io.Serializable;

/**
 * Created by root on 4/10/17.
 */
public class BusinessDeployDto {
    private String namespace;

    private String name;
//
//    private String capacity;
//
//    private String tenantid;
//
//    private String readonly;
//
//    private String bindOne;

    private BusinessTemplateDto businessTemplate;


    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

	public BusinessTemplateDto getBusinessTemplate() {
		return businessTemplate;
	}

	public void setBusinessTemplate(BusinessTemplateDto businessTemplate) {
		this.businessTemplate = businessTemplate;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public String getCapacity() {
//        return capacity;
//    }
//
//    public void setCapacity(String capacity) {
//        this.capacity = capacity;
//    }
//
//    public String getTenantid() {
//        return tenantid;
//    }
//
//    public void setTenantid(String tenantid) {
//        this.tenantid = tenantid;
//    }
//
//    public String getReadonly() {
//        return readonly;
//    }
//
//    public void setReadonly(String readonly) {
//        this.readonly = readonly;
//    }
//
//    public String getBindOne() {
//        return bindOne;
//    }
//
//    public void setBindOne(String bindOne) {
//        this.bindOne = bindOne;
//    }

}
