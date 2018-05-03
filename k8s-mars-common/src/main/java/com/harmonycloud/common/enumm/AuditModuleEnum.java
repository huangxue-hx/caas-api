package com.harmonycloud.common.enumm;

/**
 * @Author jiangmi
 * @Description 操作审计定义模块
 * @Date created in 2018-1-10
 * @Modified
 */
public enum AuditModuleEnum {

    USER("/users", "用户", "user"),
    APP_TEMPLATE("/tenants/[^//]+/projects/[^//]+/apptemplates", "应用模板", "appTemplate"),
    APP("/tenants/[^//]+/projects/[^//]+/apps", "应用", "app"),
    SERVICE_TEMPLATE("/tenants/[^//]+/projects/[^//]+/svctemplates", "服务模板", "serviceTemplate"),
    SERVICE("/tenants/[^//]+/projects/[^//]+/deploys", "服务", "service"),
    DAEMONSET("/clusters/[^//]+/daemonsets", "守护进程服务", "daemonset"),
    TENANT("/tenants", "租户", "tenant"),
    PROJECT("/tenants/[^//]+/projects", "项目", "project"),
    ROLE("/localroles|roles", "角色", "role"),
    EXTERNAL_SERVICE("/tenants/[^//]+/projects/[^//]+/extservices", "外部服务", "externalService"),
    NAMESPACE("/tenants/[^//]+/namespaces", "分区", "namespace"),
    STORAGE("/tenants/[^//]+/projects/[^//]+/pvs", "存储", "storage"),
    HARBOR("/harbor", "镜像仓库", "repository"),
    CICD("/tenants/{tenantId}/projects/{projectId}/env|dependence|dockerfile|cicdjobs", "CICD", "CICD"),
    MSF("/msf", "微服务平台", "microServicePlatform"),
    CDP("/tenants/addProject", "持续交付平台", "Continue Deliver Platform"),
    CLUSTER("/clusters", "集群", "cluster"),
    LOG("/snapshotrules", "日志管理", "Log Management"),
    ALARM("/oam", "告警", "alarm");


    private final String moduleRegex;
    private final String chDesc;
    private final String enDesc;

    AuditModuleEnum(String moduleRegex, String chDesc, String enDesc) {
        this.moduleRegex = moduleRegex;
        this.chDesc = chDesc;
        this.enDesc = enDesc;
    }

    public String getModuleRegex() {
        return moduleRegex;
    }

    public String getChDesc() {
        return chDesc;
    }

    public String getEnDesc() {
        return enDesc;
    }
}
