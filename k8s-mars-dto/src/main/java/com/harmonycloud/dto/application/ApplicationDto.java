package com.harmonycloud.dto.application;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.*;

/**
 * @Author jiangmi
 * @Description
 * @Date created in 2018-1-9
 * @Modified
 */
@PrivilegeType(name = "app", cnDesc = "应用", enDesc = "application")
@DataPrivilegeType(type = DataResourceTypeEnum.APPLICATION)
public class ApplicationDto {

    @PrivilegeField(name = "appName", cnDesc = "应用名称", enDesc = "name")
    @DataPrivilegeField(type = CommonConstant.DATA_FIELD)
    private String name;

    private String id;

    private String desc;

    @DataPrivilegeField(type = CommonConstant.NAMESPACE_FIELD)
    private String namespace;

    private String createTime;

    private String user;

    private String status;

    private Integer start;

    private Integer total;

    private String clusterId;

    private boolean isMsf;
    
    private String projectId;

    private Integer starting;

    private Integer stop;

    private String aliasNamespace;

    private String realName;

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getAliasNamespace() {
        return aliasNamespace;
    }

    public void setAliasNamespace(String aliasNamespace) {
        this.aliasNamespace = aliasNamespace;
    }

    public Integer getStarting() {
        return starting;
    }

    public void setStarting(Integer starting) {
        this.starting = starting;
    }

    public Integer getStop() {
        return stop;
    }

    public void setStop(Integer stop) {
        this.stop = stop;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public boolean isMsf() {
        return isMsf;
    }

    public void setMsf(boolean msf) {
        isMsf = msf;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
