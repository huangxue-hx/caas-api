package com.harmonycloud.dto.user;

/**
 * 局部角色Dto // created by czl
 */
public class LocalRoleDto {
    private String tenantId;
    private String clusterId;
    private String projectId;
    private  String userName;
    /**
     * 局部角色修改、删除时必填
     */
    private Integer localRoleId;
    private String roleName;
    /**
     * 分区名（预留暂时不用）
     */
    private String namespaces;
    private String resourceType;
    private String resourceId;
    /**
     * 条件规则数据，一般会补充到后台的sql中
     */
    private String condition;
    /**
     * 条件规则数据格式类型：1-json格式；2-自定义
     */
    private Short conditionType;
    /**
     * 角色描述
     */
    private String roleDesc;
    /**
     * 权限规则描述
     */
    private String privilegeRuleDesc;
    private String tables;
    private Integer resourceRuleId;
    /**
     *  权限实例表的编号
     */
    private Integer privilegeId;

    /**
     * 被替换的（旧的）成员名
     */
    private String replacedUserName;

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getNamespaces() {
        return namespaces;
    }

    public void setNamespaces(String namespaces) {
        this.namespaces = namespaces;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public Short getConditionType() {
        return conditionType;
    }

    public void setConditionType(Short conditionType) {
        this.conditionType = conditionType;
    }

    public String getPrivilegeRuleDesc() {
        return privilegeRuleDesc;
    }

    public void setPrivilegeRuleDesc(String privilegeRuleDesc) {
        this.privilegeRuleDesc = privilegeRuleDesc;
    }

    public String getRoleDesc() {
        return roleDesc;
    }

    public void setRoleDesc(String roleDesc) {
        this.roleDesc = roleDesc;
    }

    public Integer getLocalRoleId() {
        return localRoleId;
    }

    public void setLocalRoleId(Integer localRoleId) {
        this.localRoleId = localRoleId;
    }

    public String getTables() {
        return tables;
    }

    public void setTables(String tables) {
        this.tables = tables;
    }

    public Integer getResourceRuleId() {
        return resourceRuleId;
    }

    public void setResourceRuleId(Integer resourceRuleId) {
        this.resourceRuleId = resourceRuleId;
    }

    public Integer getPrivilegeId() {
        return privilegeId;
    }

    public void setPrivilegeId(Integer privilegeId) {
        this.privilegeId = privilegeId;
    }

    public String getReplacedUserName() {
        return replacedUserName;
    }

    public void setReplacedUserName(String replacedUserName) {
        this.replacedUserName = replacedUserName;
    }
}
