package com.harmonycloud.dto.tenant;

import com.harmonycloud.dao.tenant.bean.NamespaceLocal;
import com.harmonycloud.dao.tenant.bean.Project;
import com.harmonycloud.dao.tenant.bean.TenantClusterQuota;
import com.harmonycloud.dao.tenant.bean.TenantPrivateNode;
import com.harmonycloud.dao.user.bean.User;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by andy on 17-1-18.
 */
public class TenantDto implements Serializable {

    private static final long serialVersionUID = -6141661628695983123L;
    //租户名
    private String tenantName;
    //租户别名
    private String aliasName;
    //租户id
    private String tenantId;
    //项目列表
    private List<ProjectDto> projectList;
    //创建时间
    private Date createTime;
    //修改时间
    private Date updateTime;
    //分区列表
    private List<Map<String, Object>> namespaceList;
    //分区数量
    private Integer namespaceNum;
    //项目数量
    private Integer projectNum;
    //租户管理员数量
    private Integer tmNum;
    //租户管理员列表
    private List<String> tmList;
    private List<User> tmUserList;
    //租户的集群配额
    private List<ClusterQuotaDto> clusterQuota;
    //独占主机列表
    private List<TenantPrivateNode> tenantPrivateNode;
    //添加独占主机列表
    private List<TenantPrivateNode> addTenantPrivateNode;
    //删除独占主机列表
    private List<TenantPrivateNode> removeTenantPrivateNode;
    //租户独占主机列表
    private List<String> nodeList;
    //独占主机数量
    private Integer nodeNum;
    //租户的备注
    private String annotation;
    //石化盈科的租户系统编码
    private String tenantSystemCode;
    //石化盈科的更新用户账号
    private String updateUserAccount;
    //石化盈科的更新用户id
    private String updateUserId;
    //石化盈科的更新用户名
    private String updateUserName;
    //石化盈科的创建用户账号
    private String createUserAccount;
    //石化盈科的创建用户id
    private String createUserId;
    //石化盈科的创建用户名
    private String createUserName;
    //已经分配给租户的集群配额
    private Map<String, List> clusterQuotaUsage;
    //石化盈科的系统id
    private String sysId;
    //石化盈科的项目编码
    private String sysCode;
    //石化盈科的项目名称
    private String sysName;
    //石化盈科的项目分类 分类 0：主项目，1：子项目
    private String category;
    //石化盈科的主项目Id 如果是子项目，必填，主项目此字段为空
    private String parentId;
    //石化盈科的项目备注
    private String remark;
    private String applyUserId;
    private String applyUserName;
    private String dataState;
    private String email;
    private String managerId;
    private String managerName;
    private String mobile;
    private String ownOrganiseId;
    private String ownOrganiseName;
    private String runState;
    private String sysOfficalCode;
    private String sysType;
    private String useOrganiseId;
    private String useOrganiseName;
    private String shortFlag;

    public List<User> getTmUserList() {
        return tmUserList;
    }

    public void setTmUserList(List<User> tmUserList) {
        this.tmUserList = tmUserList;
    }

    public List<TenantPrivateNode> getTenantPrivateNode() {
        return tenantPrivateNode;
    }

    public void setTenantPrivateNode(List<TenantPrivateNode> tenantPrivateNode) {
        this.tenantPrivateNode = tenantPrivateNode;
    }

    public List<TenantPrivateNode> getAddTenantPrivateNode() {
        return addTenantPrivateNode;
    }

    public void setAddTenantPrivateNode(List<TenantPrivateNode> addTenantPrivateNode) {
        this.addTenantPrivateNode = addTenantPrivateNode;
    }

    public List<TenantPrivateNode> getRemoveTenantPrivateNode() {
        return removeTenantPrivateNode;
    }

    public void setRemoveTenantPrivateNode(List<TenantPrivateNode> removeTenantPrivateNode) {
        this.removeTenantPrivateNode = removeTenantPrivateNode;
    }

    public List<String> getNodeList() {
        return nodeList;
    }

    public void setNodeList(List<String> nodeList) {
        this.nodeList = nodeList;
    }

    public Integer getNodeNum() {
        return nodeNum;
    }

    public void setNodeNum(Integer nodeNum) {
        this.nodeNum = nodeNum;
    }

    public String getShortFlag() {
        return shortFlag;
    }

    public void setShortFlag(String shortFlag) {
        this.shortFlag = shortFlag;
    }

    public String getOwnOrganiseId() {
        return ownOrganiseId;
    }

    public void setOwnOrganiseId(String ownOrganiseId) {
        this.ownOrganiseId = ownOrganiseId;
    }

    public String getOwnOrganiseName() {
        return ownOrganiseName;
    }

    public void setOwnOrganiseName(String ownOrganiseName) {
        this.ownOrganiseName = ownOrganiseName;
    }

    public String getRunState() {
        return runState;
    }

    public void setRunState(String runState) {
        this.runState = runState;
    }

    public String getSysOfficalCode() {
        return sysOfficalCode;
    }

    public void setSysOfficalCode(String sysOfficalCode) {
        this.sysOfficalCode = sysOfficalCode;
    }

    public String getSysType() {
        return sysType;
    }

    public void setSysType(String sysType) {
        this.sysType = sysType;
    }

    public String getUseOrganiseId() {
        return useOrganiseId;
    }

    public void setUseOrganiseId(String useOrganiseId) {
        this.useOrganiseId = useOrganiseId;
    }

    public String getUseOrganiseName() {
        return useOrganiseName;
    }

    public void setUseOrganiseName(String useOrganiseName) {
        this.useOrganiseName = useOrganiseName;
    }

    public String getManagerId() {
        return managerId;
    }

    public void setManagerId(String managerId) {
        this.managerId = managerId;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getApplyUserName() {
        return applyUserName;
    }

    public void setApplyUserName(String applyUserName) {
        this.applyUserName = applyUserName;
    }

    public String getDataState() {
        return dataState;
    }

    public void setDataState(String dataState) {
        this.dataState = dataState;
    }

    public String getApplyUserId() {
        return applyUserId;
    }

    public void setApplyUserId(String applyUserId) {
        this.applyUserId = applyUserId;
    }

    public String getAliasName() {
        return aliasName;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    public String getSysId() {
        return sysId;
    }

    public void setSysId(String sysId) {
        this.sysId = sysId;
    }

    public String getSysCode() {
        return sysCode;
    }

    public void setSysCode(String sysCode) {
        this.sysCode = sysCode;
    }

    public String getSysName() {
        return sysName;
    }

    public void setSysName(String sysName) {
        this.sysName = sysName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Map<String, List> getClusterQuotaUsage() {
        return clusterQuotaUsage;
    }

    public void setClusterQuotaUsage(Map<String, List> clusterQuotaUsage) {
        this.clusterQuotaUsage = clusterQuotaUsage;
    }

    public Integer getTmNum() {
        return tmNum;
    }

    public void setTmNum(Integer tmNum) {
        this.tmNum = tmNum;
    }

    public String getTenantSystemCode() {
        return tenantSystemCode;
    }

    public void setTenantSystemCode(String tenantSystemCode) {
        this.tenantSystemCode = tenantSystemCode;
    }

    public String getUpdateUserAccount() {
        return updateUserAccount;
    }

    public void setUpdateUserAccount(String updateUserAccount) {
        this.updateUserAccount = updateUserAccount;
    }

    public String getUpdateUserId() {
        return updateUserId;
    }

    public void setUpdateUserId(String updateUserId) {
        this.updateUserId = updateUserId;
    }

    public String getUpdateUserName() {
        return updateUserName;
    }

    public void setUpdateUserName(String updateUserName) {
        this.updateUserName = updateUserName;
    }

    public String getCreateUserAccount() {
        return createUserAccount;
    }

    public void setCreateUserAccount(String createUserAccount) {
        this.createUserAccount = createUserAccount;
    }

    public String getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(String createUserId) {
        this.createUserId = createUserId;
    }

    public String getCreateUserName() {
        return createUserName;
    }

    public void setCreateUserName(String createUserName) {
        this.createUserName = createUserName;
    }

    public String getAnnotation() {
        return annotation;
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }

    public List<ClusterQuotaDto> getClusterQuota() {
        return clusterQuota;
    }

    public void setClusterQuota(List<ClusterQuotaDto> clusterQuota) {
        this.clusterQuota = clusterQuota;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getNamespaceNum() {
        return namespaceNum;
    }

    public void setNamespaceNum(Integer namespaceNum) {
        this.namespaceNum = namespaceNum;
    }

    public Integer getProjectNum() {
        return projectNum;
    }

    public void setProjectNum(Integer projectNum) {
        this.projectNum = projectNum;
    }

    public List<String> getTmList() {
        return tmList;
    }

    public void setTmList(List<String> tmList) {
        this.tmList = tmList;
    }

    public List<ProjectDto> getProjectList() {
        return projectList;
    }

    public void setProjectList(List<ProjectDto> projectList) {
        this.projectList = projectList;
    }

    public List<Map<String, Object>> getNamespaceList() {
        return namespaceList;
    }

    public void setNamespaceList(List<Map<String, Object>> namespaceList) {
        this.namespaceList = namespaceList;
    }
}
