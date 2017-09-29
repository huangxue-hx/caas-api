package com.harmonycloud.dto.business;

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

    private String businessTemplate;//todo useless

    private Integer external;// 1:external

    private DeploymentDetailDto deploymentDetail;

    private List<IngressDto> ingress;
    
    private int type;  //1保存为删除模式；0保存为用户模式

    private Integer flag;    //更新标记  0||null:后台需要save ;1:已存在 后台更新
    
    private boolean isPublic;

    public String getBusinessTemplate() {
        return businessTemplate;
    }

    public void setBusinessTemplate(String businessTemplate) {
        this.businessTemplate = businessTemplate;
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
}
