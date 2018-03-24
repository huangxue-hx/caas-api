package com.harmonycloud.dto.application;

import com.harmonycloud.common.enumm.PrivilegeField;
import com.harmonycloud.common.enumm.PrivilegeType;

/**
 * Created by root on 4/10/17.
 */
@PrivilegeType(name = "app", cnDesc = "应用发布")
public class ApplicationDeployDto {
    private String namespace;

	@PrivilegeField(name = "appName", cnDesc = "应用名称")
    private String appName;

    private String nodeSelector;

	private String projectId;
//
//    private String capacity;
//
//    private String tenantid;
//
//    private String readonly;
//
//    private String bindOne;

	private ApplicationTemplateDto appTemplate;

	public ApplicationTemplateDto getAppTemplate() {
		return appTemplate;
	}

	public void setAppTemplate(ApplicationTemplateDto appTemplate) {
		this.appTemplate = appTemplate;
	}

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getNodeSelector() {
		return nodeSelector;
	}

	public void setNodeSelector(String nodeSelector) {
		this.nodeSelector = nodeSelector;
	}

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
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
