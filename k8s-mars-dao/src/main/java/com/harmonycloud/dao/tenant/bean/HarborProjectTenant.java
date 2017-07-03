package com.harmonycloud.dao.tenant.bean;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by zhangsl on 16/11/13.
 * harborProject跟tenant关联bean
 */
public class HarborProjectTenant implements Serializable{

    private static final long serialVersionUID = 405310782098940013L;

    private Long id;
    private Long harborProjectId;
    private String tenantId;
    private String tenantName;
    private Date createTime;
    private String createTimeStr;
    private String harborProjectName;
    private Integer isPublic;

    public Integer getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Integer isPublic) {
        this.isPublic = isPublic;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getHarborProjectId() {
        return harborProjectId;
    }

    public void setHarborProjectId(Long harborProjectId) {
        this.harborProjectId = harborProjectId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getCreateTimeStr() {
        return createTimeStr;
    }

    public void setCreateTimeStr(String createTimeStr) {
        this.createTimeStr = createTimeStr;
    }

    public String getHarborProjectName() {
        return harborProjectName;
    }

    public void setHarborProjectName(String harborProjectName) {
        this.harborProjectName = harborProjectName;
    }
}
