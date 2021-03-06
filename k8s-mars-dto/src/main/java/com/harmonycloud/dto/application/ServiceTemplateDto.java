package com.harmonycloud.dto.application;

import java.util.List;

/**
 * Created by root on 3/30/17.
 */
public class ServiceTemplateDto {
	
    private Integer id;
	
    private String name;
    
    private String tag;

    private String desc;

    private String tenant;

    private String tenantId;

    private Integer external;// 1:external

    private DeploymentDetailDto deploymentDetail;

    private StatefulSetDetailDto statefulSetDetail;

    private List<IngressDto> ingress;
    
    private int type;  //1保存为删除模式；0保存为用户模式

    private Integer flag;    //更新标记  0||null:后台需要save ;1:已存在 后台更新
    
    private boolean isPublic;

    private String projectId;

    private String clusterId;

    private String serviceType;

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public Integer getExternal() {
        return external;
    }

    public void setExternal(Integer external) {
        this.external = external;
    }

    public Integer getFlag() {
        return flag;
    }

    public void setFlag(Integer flag) {
        this.flag = flag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public DeploymentDetailDto getDeploymentDetail() {
		return deploymentDetail;
	}

	public void setDeploymentDetail(DeploymentDetailDto deploymentDetail) {
		this.deploymentDetail = deploymentDetail;
	}

	public List<IngressDto> getIngress() {
		return ingress;
	}

	public void setIngress(List<IngressDto> ingress) {
		this.ingress = ingress;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public boolean isPublic() {
		return isPublic;
	}

	public void setPublic(boolean isPublic) {
		this.isPublic = isPublic;
	}

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public StatefulSetDetailDto getStatefulSetDetail() {
        return statefulSetDetail;
    }

    public void setStatefulSetDetail(StatefulSetDetailDto statefulSetDetail) {
        this.statefulSetDetail = statefulSetDetail;
    }
}
