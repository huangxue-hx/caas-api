package com.harmonycloud.common.enumm;

/**
 * @Author jiangmi
 * @Description 操作审计 需要拦截的url
 * @Date created in 2018-1-10
 * @Modified
 */
public enum AuditUrlEnum {

    USER_LOGIN_SSO("/users/current_GET", "用户登录", "userLogin", null, null, "isLogin", "USER"),
    USER_LOGIN("/users/auth/login", "用户登录", "userLogin", null, null, null, "USER"),
    USER_LOGOUT("/users/auth/logout_POST", "用户登出", "userLogout", null, null, null, "USER"),
    USER_CREATE("/users_POST", "创建用户", "createUser", null, null, "username", "USER"),
    USER_CHANGE_PASSWORD("/users/([^//]+)/password_PUT", "修改用户密码", "changeUserPassword", 1, null, null, "USER"),
    USER_CHANGE_PHONE("/users/([^//]+)/phone_PUT", "修改用户电话", "changeUserPhone", 1, null, null, "USER"),
    USER_CHANGE_REALNAME("/users/([^//]+)/realname_PUT", "修改用户真实姓名", "changeUserRealName", 1, null, null, "USER"),
    USER_CHANGE_EMAIL("/users/([^//]+)/email_PUT", "修改用户email", "changeUserEmail", 1, null, null, "USER"),
    USER_RESET_PASSWORD("/users/([^//]+)/password/reset_PUT", "重置用户密码", "resetUserPassword", 1, null, null, "USER"),
    USER_DELETE("/users/([^//]+)_DELETE", "删除用户", "deleteUser", 1, null, null, "USER"),
    USER_UPDATE_STATUS("/users/([^//]+)/status_PUT", "更新用户状态", "updateUserStatus", 1, null, null, "USER"),
    USER_UPDATE_TYPE("/users/([^//]+)/type_PUT", "更新用户角色", "updateUserRole", 1, null, null, "USER"),
    USER_CREATE_GROUP("/users/groups_POST", "创建用户组", "createUserGroup", null, null, "groupname", "USER"),
    USER_DELETE_GROUP("/users/groups/([^//]+)_DELETE", "删除用户组", "deleteUserGroup", 1, "UserService", null, "USER"),
    USER_UPDATE_GROUP("/users/groups/[^//]+_PUT", "修改用户组", "updateUserGroup", null, null, "usergroup.groupname", "USER"),

    CREATE_APP_TEMPLATE("/tenants/[^//]+/projects/[^//]+/apptemplates_POST", "保存应用模板", "saveAppTemplate",
            null, null, "name", "APP_TEMPLATE"),
    UPDATE_APP_TEMPLATE("/tenants/([^//]+)/projects/([^//]+)/apptemplates/([^//]+)_PUT", "更新应用模板", "updateAppTemplate", 3, null, null, "APP_TEMPLATE"),
    DELETE_APP_TEMPLATE("/tenants/([^//]+)/projects/([^//]+)/apptemplates/([^//]+)_DELETE", "删除应用模板", "deleteAppTemplate", 3, null, null, "APP_TEMPLATE"),
    USE_APP_TEMPLATE_DEPLOY("/tenants/([^//]+)/projects/([^//]+)/apptemplates/([^//]+)/deploys_POST", "使用应用模板发布",
            "useAppTemplateDeploy", 3, null, null, "APP_TEMPLATE"),
    CHANGE_STATUS_APP_TEMPLATE("/tenants/([^//]+)/projects/([^//]+)/apptemplates/([^//]+)/status_PUT", "改变应用模板状态",
            "changeAppTemplateStatus", 3, null, null, "APP_TEMPLATE"),

    DEPLOY_APP("/tenants/([^//]+)/projects/([^//]+)/apps_POST", "发布应用", "deployApp", null, null, "appName", "APP"),
    DELETE_APP("/tenants/([^//]+)/projects/([^//]+)/apps/([^//]+)_DELETE", "删除应用", "deleteApp", 3, null, null, "APP"),
    UPDATE_APP("/tenants/([^//]+)/projects/([^//]+)/apps/([^//]+)_PUT", "修改应用", "updateApp", 3, null, null, "APP"),

    ADD_SERVICE_TEMPLATE_IN_APP_TEMPLATE("/tenants/([^//]+)/projects/([^//]+)/apptemplates/([^//]+)/addsvctemplate_POST",
            "在应用模板内添加服务模板", "addServiceTemplateInAppTemplate", null, null, "serviceList", "SERVICE_TEMPLATE"),
    UPDATE_SERVICE_TEMPLATE_IN_APP_TEMPLATE("/tenants/([^//]+)/projects/([^//]+)/apptemplates/([^//]+)/addsvctemplate_PUT",
            "在应用模板内更新服务模板", "updateServiceTemplateInAppTemplate", null, null, "serviceList", "SERVICE_TEMPLATE"),
    CREATE_SERVICE_TEMPLATE("/tenants/([^//]+)/projects/([^//]+)/svctemplates_POST", "创建服务模板", "createServiceTemplate",
            null, null, "name", "SERVICE_TEMPLATE"),
    UPDATE_SERVICE_TEMPLATE("/tenants/([^//]+)/projects/([^//]+)/svctemplates/([^//]+)_PUT", "更新服务模板",
            "updateServiceTemplate", 3, null, null, "SERVICE_TEMPLATE"),
    DELETE_SERVICE_TEMPLATE("/tenants/([^//]+)/projects/([^//]+)/svctemplates/([^//]+)_DELETE", "删除服务模板",
            "deleteServiceTemplate", 3, null, null, "SERVICE_TEMPLATE"),
    USE_SERVICE_TEMPLATE_DEPLOY("/tenants/([^//]+)/projects/([^//]+)/svctemplates/([^//]+)/deploys_POST", "使用服务模板发布服务",
            "useServiceTemplateDeploy", 3, null, null, "SERVICE_TEMPLATE"),
    DEPLOY_SERVICE_TEMPLATE("/tenants/([^//]+)/projects/([^//]+)/deploys_POST", "发布服务", "deployService",
            null, null, "serviceTemplate.deploymentDetail.name", "SERVICE"),
    CHANGE_STATUS_SERVICE_TEMPLATE("/tenants/([^//]+)/projects/([^//]+)/svctemplates/([^//]+)/status_PUT", "改变应用模板状态",
            "changeServiceTemplateStatus", 3, null, null, "SERVICE_TEMPLATE"),

    DEPLOY_SERVICE_IN_APP("/tenants/[^//]+/projects/[^//]+/apps/[^//]+/deploys_POST", "在已有的应用中发布服务", "deployServiceInApp", null, null, "name", "SERVICE"),
    CREATE_AUTOSCALE("/tenants/([^//]+)/projects/([^//]+)/deploys/([^//]+)/autoscale_POST", "设置自动伸缩", "setAutoScale", 3, null, null, "SERVICE"),
    UPDATE_AUTOSCALE("/tenants/([^//]+)/projects/([^//]+)/deploys/([^//]+)/autoscale_PUT", "更新自动伸缩", "updateAutoScale", 3, null, null, "SERVICE"),
    DELETE_AUTOSCALE("/tenants/([^//]+)/projects/([^//]+)/deploys/([^//]+)/autoscale_DELETE", "删除自动伸缩", "deleteAutoScale", 3, null, null, "SERVICE"),
    UPLOAD_FILE_TO_NODE("/tenants/([^//]+)/projects/([^//]+)/deploys/([^//]+)/container/file/uploadToNode_POST", "文件上传到指定节点",
            "uploadFileToNode", 3, null, null, "SERVICE"),
    UPLOAD_FILE_TO_CONTAINER("/tenants/([^//]+)/projects/([^//]+)/deploys/([^//]+)/container/file/upload_POST", "文件上传到容器内部",
            "uploadFileToContainer", 3, null, null, "SERVICE"),
    DELETE_FILE_UPLOAD_RECORD("/tenants/([^//]+)/projects/([^//]+)/deploys/([^//]+)/container/file/upload/records_DELETE", "删除文件上传记录",
            "deleteFileUploadRecord", 3, null, null, "SERVICE"),
    START_SERVICE("/tenants/([^//]+)/projects/([^//]+)/deploys/([^//]+)/start_POST", "启动服务", "startService", 3, null, null, "SERVICE"),
    STOP_SERVICE("/tenants/([^//]+)/projects/([^//]+)/deploys/([^//]+)/stop_POST", "停止服务", "stopService", 3, null, null, "SERVICE"),
    SCALE_SERVICE("/tenants/([^//]+)/projects/([^//]+)/deploys/([^//]+)/scale_POST", "服务实例伸缩", "scaleService", 3, null, null, "SERVICE"),
    DELETE_SERVICE("/tenants/([^//]+)/projects/([^//]+)/deploys/([^//]+)_DELETE", "删除服务", "deleteService", 3, null, null, "SERVICE"),
    UPDATE_SERVICE("/tenants/([^//]+)/projects/([^//]+)/deploys/([^//]+)_PUT", "修改服务", "updateService", 3, null, null, "SERVICE"),
    CANARY_UPDATE_SERVICE("/tenants/([^//]+)/projects/([^//]+)/deploys/([^//]+)/canaryUpdate_PUT", "灰度升级", "canaryUpdate", 3, null, null, "SERVICE"),
    CANCEL_CANARY_UPDATE_SERVICE("/tenants/([^//]+)/projects/([^//]+)/deploys/([^//]+)/canaryupdate/cancel_PUT",
            "取消灰度升级", "cancelCanaryUpdate", 3, null, null, "SERVICE"),
    RESUME_CANARY_UPDATE_SERVICE("/tenants/([^//]+)/projects/([^//]+)/deploys/([^//]+)/canaryupdate/resume_PUT",
            "继续灰度升级", "resumeCanaryUpdate", 3, null, null, "SERVICE"),
    PAUSE_CANARY_UPDATE_SERVICE("/tenants/([^//]+)/projects/([^//]+)/deploys/([^//]+)/canaryupdate/pause_PUT",
            "暂停灰度升级", "pauseCanaryUpdate", 3, null, null, "SERVICE"),
    ROLLBACK_CANARY_UPDATE("/tenants/([^//]+)/projects/([^//]+)/deploys/([^//]+)/canaryrollback_PUT",
            "灰度升级回滚", "rollbackCanaryUpdate", 3, null, null, "SERVICE"),
    BLUEGREEN_DEPLOY("/tenants/([^//]+)/projects/([^//]+)/deploys/([^//]+)/bluegreen_PUT", "蓝绿发布", "blueGreenDeploy", 3, null, null, "SERVICE"),
    SWITCH_FLOW_BETWEEN_SERVICE("/tenants/([^//]+)/projects/([^//]+)/deploys/([^//]+)/bluegreen/switchflow_POST", "蓝绿发布流量切换",
            "switchFlowInBlueGreenDeploy", 3, null, null, "SERVICE"),
    CONFIRM_UPDATE_NEW("/tenants/([^//]+)/projects/([^//]+)/deploys/([^//]+)/bluegreen/confirm_POST", "蓝绿发布确认升级",
            "confirmUpdateInBlueGreenDeploy", 3, null, null, "SERVICE"),
    KEEP_OLD_VERSION("/tenants/([^//]+)/projects/([^//]+)/deploys/([^//]+)/bluegreen/cancel_POST", "蓝绿发布保留旧版本",
            "keepOldVersionInBlueGreenDeploy", 3, null, null, "SERVICE"),

    DEPLOY_STATEFUL_SERVICE("/tenants/([^//]+)/projects/([^//]+)/statefulsets_POST", "发布服务", "deployService",
            null, null, "serviceTemplate.statefulSetDetail.name", "SERVICE"),
    START_STATEFUL_SERVICE("/tenants/([^//]+)/projects/([^//]+)/statefulsets/([^//]+)/start_POST", "启动服务", "startService", 3, null, null, "SERVICE"),
    STOP_STATEFUL_SERVICE("/tenants/([^//]+)/projects/([^//]+)/statefulsets/([^//]+)/stop_POST", "停止服务", "stopService", 3, null, null, "SERVICE"),
    SCALE_STATEFUL_SERVICE("/tenants/([^//]+)/projects/([^//]+)/statefulsets/([^//]+)/scale_POST", "服务实例伸缩", "scaleService", 3, null, null, "SERVICE"),
    DELETE_STATEFUL_SERVICE("/tenants/([^//]+)/projects/([^//]+)/statefulsets_DELETE", "删除服务", "deleteService", null, null, "serviceList[0].name", "SERVICE"),

    CREATE_DAEMONSET("/clusters/([^//]+)/daemonsets_POST", "创建守护进程服务", "createDaemonSet", null, null, "name", "DAEMONSET"),
    DELETE_DAEMONSET("/clusters/([^//]+)/daemonsets/([^//]+)_DELETE", "删除守护进程服务", "deleteDaemonSet", 2, null, null, "DAEMONSET"),
    UPDATE_DAEMONSET("/clusters/([^//]+)/daemonsets/([^//]+)_PUT", "更新守护进程服务", "updateDaemonSet", 2, null, null, "DAEMONSET"),

    CREATE_ROLE("/roles_POST", "添加角色", "createRole", null, null, "nickName", "ROLE"),
    UPDATE_ROLE_PRIVILEGE("/roles/([^//]+)/privilege_PUT", "更新角色权限", "updateRolePrivilege", 1, "RoleLocalService", null, "ROLE"),
    UPDATE_MENU_WEIGHT("/roles/([^//]+)/menu_PUT", "更新菜单权重", "updateMenuWeight", 1, "RoleLocalService", null, "ROLE"),
    DELETE_ROLE("/roles/([^//]+)_DELETE", "删除角色", "deleteRole", 1, "RoleLocalService", null, "ROLE"),
    DISABLE_ROLE("/roles/([^//]+)/disable_PUT", "禁用角色", "disableRole", 1, "RoleLocalService", null, "ROLE"),
    ENABLE_ROLE("/roles/([^//]+)/enable_PUT", "启用角色", "enableRole", 1, "RoleLocalService", null, "ROLE"),
    RESET_ROLE_PRIVILEGE("/roles/([^//]+)/privilege/reset_PUT", "重置角色权限", "resetRolePrivilege", 1, "RoleLocalService", null, "ROLE"),
    CREATE_LOCAL_ROLE("/localroles_POST", "创建局部角色", "createLocalRole", 1, "LocalRoleService", null, "ROLE"),
    UPDATE_LOCAL_ROLE("/localroles/([^//]+)_PUT", "更新局部角色", "updateLocalRole", 1, "LocalRoleService", null, "ROLE"),
    DELETE_LOCAL_ROLE("/localroles/([^//]+)_DELETE", "删除局部角色", "deleteLocalRole", 1, "LocalRoleService", null, "ROLE"),
    ADD_DATA_RULE("/localroles/([^//]+)/rules_POST", "增加数据规则", "addDataRule", 1, "LocalRoleService", null, "ROLE"),
    UPDATE_DATA_RULE("/localroles/([^//]+)/rules_PUT", "修改数据规则", "updateDataRule", 1, "LocalRoleService", null, "ROLE"),
    DELETE_DATA_RULE("/localroles/([^//]+)/rules_DELETE", "删除数据规则", "deleteDataRule", 1, "LocalRoleService", null, "ROLE"),
    ADD_USER_IN_LOCAL_ROLE("/localroles/([^//]+)/users/([^//]+)_POST", "添加局部角色的用户", "addUserInLocalRole", 2, null, null, "ROLE"),
    UPDATE_USER_IN_LOCAL_ROLE("/localroles/([^//]+)/users/([^//]+)_PUT", "更新局部角色的用户", "updateUserInLocalRole", 2, null, null, "ROLE"),
    ASSIGN_DATA_PRIVILEGE("/localroles/([^//]+)/privilege_POST", "分配数据权限", "assignDataPrivilege", 1, "LocalRoleService", null, "ROLE"),

    CREATE_EXT_SERVICE("/tenants/([^//]+)/projects/([^//]+)/extservices_POST", "创建外部服务", "createExternalService",
            null, null, "name", "EXTERNAL_SERVICE"),
    DELETE_EXT_SERVICE("/tenants/([^//]+)/projects/([^//]+)/extservices/([^//]+)_DELETE", "删除外部服务", "createExternalService", 3, null, null, "EXTERNAL_SERVICE"),
    UPDATE_EXT_SERVICE("/tenants/([^//]+)/projects/([^//]+)/extservices/([^//]+)_PUT", "更新外部服务", "updateExternalService", 3, null, null, "EXTERNAL_SERVICE"),

    CREATE_NAMESPACE("/tenants/([^//]+)/namespaces_POST", "创建分区", "createNamespace", null, null, "name", "NAMESPACE"),
    UPDATE_NAMESPACE("/tenants/([^//]+)/namespaces_PUT", "更新分区", "updateNamespace", null, null, "name", "NAMESPACE"),
    DELETE_NAMESPACE("/tenants/([^//]+)/namespaces/([^//]+)_DELETE", "删除分区", "deleteNamespace", 2, null, null, "NAMESPACE"),

    CREATE_STORAGECLASS("/clusters/([^//]+)/storage_POST", "创建存储服务", "createStorageService", null, null, "name", "STORAGE"),
    DELETE_STORAGECLASS("/clusters/([^//]+)/storage_DELETE", "删除存储服务", "deleteStorageService", null, null, "name", "STORAGE"),

    CREATE_PV("/tenants/([^//]+)/projects/([^//]+)/pvc_POST", "创建存储", "createStory", null, null, "name", "STORAGE"),
    DELETE_PV("/tenants/([^//]+)/projects/([^//]+)/pvc/([^//]+)_DELETE", "删除存储", "deleteStory", 3, null, null, "STORAGE"),
    UPDATE_PV("/tenants/([^//]+)/projects/([^//]+)/pvc_PUT", "修改存储", "updateStory", null, null, "name", "STORAGE"),
    RECYCLE_PV("/tenants/([^//]+)/projects/([^//]+)/pvc/([^//]+)/recycle_PUT", "回收存储", "recycleStory", 3, null, null, "STORAGE"),

    CREATE_CICD_ENV("/tenants/([^//]+)/projects/([^//]+)/env_POST", "新增环境", "createEnv", null, null, "name", "CICD"),
    UPDATE_CICD_ENV("/tenants/([^//]+)/projects/([^//]+)/env_PUT", "修改环境", "updateEnv", null, null, "name", "CICD"),
    DELETE_CICD_ENV("/tenants/([^//]+)/projects/([^//]+)/env/([^//]+)_DELETE", "删除环境", "deleteEnv", null, null, "name", "CICD"),
    CREATE_CICD_DEPENDENCE("/tenants/([^//]+)/projects/([^//]+)/dependence_POST", "添加依赖", "createDependence",
            null, null, "name", "CICD"),
    DELETE_CICD_DEPENDENCE("/tenants/([^//]+)/projects/([^//]+)/dependence/([^//]+)_DELETE", "删除依赖", "deleteDependence",
            3, null, null, "CICD"),
    UPLOAD_CICD_DEP_FILE("/tenants/([^//]+)/projects/([^//]+)/dependence/([^//]+)/file_POST", "上传文件", "uploadDependenceFile",
            3, null, null, "CICD"),
    DELETE_CICD_DEP_FILE("/tenants/([^//]+)/projects/([^//]+)/dependence/([^//]+)/file_DELETE", "删除文件", "deleteDependenceFile",
            3, null, null, "CICD"),
    CREATE_DOCKERFILE("/tenants/([^//]+)/projects/([^//]+)/dockerfile_POST", "创建dockerfile", "createDockerfile",
            null, null, "name", "CICD"),
    DELETE_DOCKERFILE("/tenants/([^//]+)/projects/([^//]+)/dockerfile/([^//]+)_DELETE", "删除dockerfile", "deleteDockerfile",
            null, "DockerFileService", null, "CICD"),
    UPDATE_DOCKERFILE("/tenants/([^//]+)/projects/([^//]+)/dockerfile_PUT", "更新dockerfile", "updateDockerfile",
            null, null, "name", "CICD"),
    CREATE_CICD_PIPELINE("/tenants/([^//]+)/projects/([^//]+)/cicdjobs_POST", "创建流水线", "createPipeline",
            null, null, "name", "CICD"),
    DELETE_CICD_PIPELINE("/tenants/([^//]+)/projects/([^//]+)/cicdjobs/([^//]+)_DELETE", "删除流水线", "deletePipeline",
            3, "JobService", null, "CICD"),
    START_CICD_PIPELINE("/tenants/([^//]+)/projects/([^//]+)/cicdjobs/([^//]+)/start_PATCH", "流水线开始构建", "startPipeline",
            3, "JobService", null, "CICD"),
    STOP_CICD_PIPELINE("/tenants/([^//]+)/projects/([^//]+)/cicdjobs/([^//]+)/stop_PATCH", "流水线停止构建", "stopPipeline",
            3, "JobService", null, "CICD"),
    DELETE_CICD_BUILD_RESULT("/tenants/([^//]+)/projects/([^//]+)/cicdjobs/([^//]+)/result/([^//]+)/delete_DELETE", "流水线删除构建结果", "deleteBuildResult",
            3, "JobService", null, "CICD"),
    UPDATE_CICD_NOTIFICATION("/tenants/([^//]+)/projects/([^//]+)/cicdjobs/([^//]+)/notification_PUT", "更新流水线通知配置", "updatePipelineNotification",
            3, "JobService", null, "CICD"),
    UPDATE_CICD_PARAM("/tenants/([^//]+)/projects/([^//]+)/cicdjobs/([^//]+)/parameter_POST", "更新参数信息", "updatePipelineParameter",
            3, "JobService", null, "CICD"),
    ADD_STAGE_IN_PIPELINE("/tenants/([^//]+)/projects/([^//]+)/cicdjobs/([^//]+)/stages_POST", "流水线添加步骤", "addPipelineStage",
            3, "JobService", null, "CICD"),
    UPDATE_STAGE_IN_PIPELINE("/tenants/([^//]+)/projects/([^//]+)/cicdjobs/([^//]+)/stages_PUT", "流水线更新步骤", "updatePipelineStage",
            3, "JobService", null, "CICD"),
    DELETE_STAGE_IN_PIPELINE("/tenants/([^//]+)/projects/([^//]+)/cicdjobs/([^//]+)/stages/([^//]+)_DELETE", "流水线删除步骤", "deletePipelineStage",
            3, "JobService", null, "CICD"),
    UPDATE_CREDENTIALS("/tenants/([^//]+)/projects/([^//]+)/cicdjobs/([^//]+)/stages/updateCredentials_POST", "更新流水线密码", "updateCredential",
            3, "JobService", null, "CICD"),

    CREATE_TENANT("/tenants_POST", "创建租户", "createTenant", null, null, "tenantName", "TENANT"),
    DELETE_TENANT("/tenants/([^//]+)_DELETE", "删除租户", "deleteTenant", 1, "TenantService", null, "TENANT"),
    UPDATE_TENANT("/tenants/([^//]+)_PUT", "修改租户", "updateTenant", 1, "TenantService", null, "TENANT"),
    ADD_MANAGER_IN_TENANT("/tenants/([^//]+)/tms_POST", "增加租户管理员", "addTenantManager", 1, "TenantService", null, "TENANT"),
    DELETE_MANAGER_IN_TENANT("/tenants/([^//]+)/tms/([^//]+)_POST", "移除租户管理员", "deleteTenantManager", 2, null, null, "TENANT"),

    INIT_SPACE("/msf/namespace/deployments_POST", "初始化空间", "initSpace", null, "NamespaceLocalService", "space_id", "MSF"),
    RESET_SPACE("/msf/namespace/reset_POST", "重置空间(组件)", "resetSpace", null, "NamespaceLocalService", "space_id", "MSF"),
    UPDATE_INSTANCE("/msf/namespace/instances_PUT", "更新微服务实例", "updateInstance", null, "NamespaceLocalService", "space_id", "MSF"),
    DELETE_SPACE("/msf/namespace/delete_DELETE", "删除空间", "deleteSpace", null, "NamespaceLocalService", "space_id", "MSF"),
    DELETE_INSTANCE("/msf/deleteInstances_DELETE", "删除微服务实例", "deleteInstance", null, "NamespaceLocalService", "space_id", "MSF"),

    DELETE_IMAGE("/tenants/([^//]+)/projects/([^//]+)/repositories/([^//]+)/images/([^//]+)_DELETE", "删除镜像", "deleteImage",
            4, null, null, "HARBOR"),
    UPLOAD_IMAGE("/tenants/([^//]+)/projects/([^//]+)/repositories/([^//]+)/images/upload_POST", "上传镜像", "uploadImage",
            3, "HarborProjectService", null, "HARBOR"),
    //该接口中需要从return中获取
    CREATE_REPO("/tenants/([^//]+)/projects/([^//]+)/repositories_POST", "创建镜像仓库", "createRepository",
            null, null, "harborProjectName", "HARBOR"),
    DELETE_REPO("/tenants/([^//]+)/projects/([^//]+)/repositories/([^//]+)_DELETE", "删除镜像仓库", "deleteRepository",
            3, "HarborProjectService", null, "HARBOR"),
    UPDATE_QUOTA("/tenants/([^//]+)/projects/([^//]+)/repositories/([^//]+)/repositoryquota_DELETE", "更新镜像仓库配额", "updateRepositoryQuota",
            3, "HarborProjectService", null, "HARBOR"),
    SET_CLEAN_RULE("/tenants/([^//]+)/projects/([^//]+)/repositories/([^//]+)/cleanrule_POST", "设置镜像清理规则", "setImageCleanRule",
            3, "HarborProjectService", null, "HARBOR"),
    DELETE_CLEAN_RULE("/tenants/([^//]+)/projects/([^//]+)/repositories/([^//]+)/cleanrule/([^//]+)_DELETE", "删除镜像清理规则", "deleteImageCleanRule",
            3, "HarborProjectService", null, "HARBOR"),
    UPDATE_CLEAN_RULE("/tenants/([^//]+)/projects/([^//]+)/repositories/([^//]+)/cleanrule_PUT", "更新镜像清理规则", "updateImageCleanRule",
            3, "HarborProjectService", null, "HARBOR"),
    CREATE_SYNC_TARGET("/harbor/([^//]+)/replicationtargets_POST", "新建跨harbor同步对象", "createSyncTarget", 1, null, null, "HARBOR"),
    DELETE_SYNC_TARGET("/harbor/([^//]+)/replicationtargets/([^//]+)_DELETE", "删除跨harbor同步对象", "deleteSyncTarget", 1, null, null, "HARBOR"),
    CREATE_SYNC_TASK("/harbor/([^//]+)/replicationpolicies_POST", "新建跨harbor同步任务", "createSyncTask", 1, null, null, "HARBOR"),
    DELETE_SYNC_TASK("/harbor/([^//]+)/replicationpolicies/([^//]+)_DELETE", "删除跨harbor同步任务", "deleteSyncTask", 1, null, null, "HARBOR"),
    UPDATE_SYNC_TASK("/harbor/([^//]+)/replicationpolicies/([^//]+)/enable_PUT", "更改跨harbor同步任务是否有效", "updateSyncTaskEffectiveness", 1, null, null, "HARBOR"),
    COPY_IMAGE("/harbor/([^//]+)/replicationpolicies/syncimage_POST", "复制镜像", "", 1, null, null, "HARBOR"),

    ADD_PROJECT("/tenants/addProject_POST", "同步项目", "synchronizeProject", null, null, "sysName", "CDP"),
    ADD_USER("/tenants/addUser_POST", "同步用户", "synchronizeUser", null, null, "userName", "CDP"),
    ADD_RELATIONSHIP("/tenants/project/addUser_POST", "添加项目与用户的关系", "addRelationshipBetweenProjectAndUser", null, null, "userAccount", "CDP"),
    DELETE_RELATIONSHIP("/tenants/project/removeUser_POST", "删除项目与用户的关系", "deleteRelationshipBetweenProjectAndUser", null, null, "userAccount", "CDP"),

    CREATE_PROJECT("/tenants/([^//]+)/projects_POST", "创建项目", "createProject", null, null, "projectName", "PROJECT"),
    DELETE_PROJECT("/tenants/([^//]+)/projects/([^//]+)_DELETE", "删除项目", "deleteProject", 2, "project", null, "PROJECT"),
    UPDATE_PROJECT("/tenants/([^//]+)/projects/([^//]+)_PUT", "修改项目", "updateProject", 2, "project", null, "PROJECT"),


    ADD_CLUSTER("/clusters_POST", "添加集群", "addCluster", null, null, "nickname", "CLUSTER"),
    UPDATE_CLUSTER("/clusters/([^//]+)_PUT", "修改集群", "updateCluster", null, null, "nickname", "CLUSTER"),
    DELETE_CLUSTER("/clusters/([^//]+)_DELETE", "删除集群", "deleteCluster", 1, "ClusterService", null, "CLUSTER"),
    UPDATE_CLUSTER_STATUS("/clusters/([^//]+)/status_PUT", "修改集群状态", "updateClusterStatus", 1, "ClusterService", null, "CLUSTER"),
    ADD_DATACENTER("/datacenters_POST", "添加数据中心", "addDataCenter", null, null, "annotations", "CLUSTER"),
    UPDATE_DATACENTER("/datacenters/([^//]+)_PUT", "修改数据中心", "updateDataCenter", null, null, "annotations", "CLUSTER"),
    DELETE_DATACENTER("/datacenters/([^//]+)_DELETE", "删除数据中心", "deleteDataCenter", 1, null, null, "CLUSTER"),
    ADD_NODE("/clusters/([^//]+)/nodes/([^//]+)/addNode_POST", "主机上线", "addNodeInCluster", 2, null, null, "CLUSTER"),
    DELETE_NODE("/clusters/([^//]+)/nodes/([^//]+)/removeNode_DELETE", "主机下线", "deleteNodeInCluster", 2, null, null, "CLUSTER"),
    UPDATE_NODE_STATUS("/clusters/([^//]+)/nodes/([^//]+)/schedule_PUT", "修改主机维护状态", "changeNodeStatus", 2, null, null, "CLUSTER"),
    UPDATE_IDLENODE_STATUS("/clusters/([^//]+)/nodes/([^//]+)_PUT", "修改闲置节点状态", "updateIdleNodeStatus", 2, null, null, "CLUSTER"),
    DRAIN_POD("/clusters/([^//]+)/nodes/([^//]+)/drainPod_PUT", "主机POD迁移", "drainPod", 2, null, null, "CLUSTER"),

    CREATE_LOG_BACKUP_RULE("/snapshotrules_POST", "创建日志备份规则", "createLogBackupRule", null, "ClusterService", "clusterIds", "LOG"),
    UPDATE_LOG_BACKUP_RULE("/snapshotrules/([^snapshots//]+)_PUT", "更新日志备份规则", "updateLogBackupRule", null, "ClusterService", "clusterIds", "LOG"),
    DELETE_LOG_BACKUP_RULE("/snapshotrules/([^snapshots//]+)_DELETE", "删除日志备份规则", "deleteLogBackupRule", 1, "LogBackupRuleMapper", null, "LOG"),
    STOP_LOG_BACKUP("/snapshotrules/([^//]+)/stop_PUT", "停止日志备份规则", "stopLogBackupRule", 1, "LogBackupRuleMapper", null, "LOG"),
    START_LOG_BACKUP("/snapshotrules/([^//]+)/start_PUT", "启动日志备份规则", "startLogBackupRule", 1, "LogBackupRuleMapper", null, "LOG"),
    CREATE_SNAPSHOT("/snapshotrules/snapshots_POST", "创建日志备份快照", "createLogBackupSnapshot", null, null, "snapshotName", "LOG"),
    DELETE_SNAPSHOT("/snapshotrules/snapshots_DELETE", "删除日志备份快照", "deleteLogBackupSnapshot", null, null, "snapshotName", "LOG"),
    RECOVER_SNAPSHOT("/snapshotrules/snapshots_PUT", "恢复日志备份快照", "recoverLogBackupSnapshot", null, null, "snapshotName", "LOG"),

    ADD_CONFIG("/tenants/([^//]+)/projects/([^//]+)/configmap_POST", "新增配置文件", "addConfigurationFile", null, null, "name", "CONFIG_CENTET"),
    UPDATE_CONFIG("/tenants/([^//]+)/projects/([^//]+)/configmap_PUT", "修改配置文件", "addConfigurationFile", null, null, "name", "CONFIG_CENTET"),
    DELETE_CONFIG("/tenants/([^//]+)/projects/([^//]+)/configmap_DELETE", "删除配置文件", "addConfigurationFile", null, null, "name", "CONFIG_CENTET");


    //url正则表达式
    private final String urlRegex;

    //操作中文描述
    private final String chDesc;

    //操作英文描述
    private final String enDesc;

    //正则表达式匹配组号
    private final Integer paramIndex;

    //查询数据库的service
    private final String queryDb;

    //请求参数名称
    private final String paramName;

    //所属模块
    private final String module;

    AuditUrlEnum(String urlRegex, String chDesc, String enDesc, Integer paramIndex, String queryDb, String paramName, String module) {
        this.urlRegex = urlRegex;
        this.chDesc = chDesc;
        this.enDesc = enDesc;
        this.paramIndex = paramIndex;
        this.queryDb = queryDb;
        this.paramName = paramName;
        this.module = module;
    }

    public String getUrlRegex() {
        return urlRegex;
    }

    public String getChDesc() {
        return chDesc;
    }

    public String getEnDesc() {
        return enDesc;
    }

    public Integer getParamIndex() {
        return paramIndex;
    }

    public String getQueryDb() {
        return queryDb;
    }

    public String getParamName() {
        return paramName;
    }

    public String getModule() {
        return module;
    }
}
