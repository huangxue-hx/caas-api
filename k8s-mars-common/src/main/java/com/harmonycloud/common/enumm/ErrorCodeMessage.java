package com.harmonycloud.common.enumm;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

import static com.harmonycloud.common.Constant.CommonConstant.DEFAULT_LANGUAGE_CHINESE;
import static com.harmonycloud.common.Constant.CommonConstant.LANGUAGE_CHINESE;
import static com.harmonycloud.common.Constant.CommonConstant.LANGUAGE_ENGLISH;

/**
 * 错误代码枚举
 * 通用错误 1xxxxx
 * 用户相关 200xxx, 租户 201xxx, 项目 202xxx, 角色权限 203xxx
 * 集群相关错误代码 300xxx, 主机节点 301xxx, 分区 302xxx
 * 应用 400xxx, 模板 401xxx, 存储 402xxx, 弹性伸缩 403xxx, 日志管理 404xxx, 镜像 405xxx, CICD 406xxx
 * 其他 9xxxxx
 */
public enum ErrorCodeMessage {
    //通用错误 1xxxxx
    UNKNOWN(100001, "System error, please contact Administrator.", "系统错误，请联系系统管理员"),
    INVALID_PARAMETER(100002, "Invalid parameter.", "参数错误"),
    INVALID_CONFIG(100003, "Invalid Config.", "配置错误"),
    PARAMETER_VALUE_NOT_PROVIDE(100004, "Parameter cannot be null.", "参数不能为空"),
    NOT_BLANK(100005, "Can not be blank.", "不能为空"),
    ADMIN_NOT_FOUND(100006, "Admin not found.", "未找到admin用户"),
    RUN_COMMAND_ERROR(100007, "Command executed failed.","命令执行失败"),
    OPERATION_FAIL(100008, "Operation failed.","操作失败"),
    OPERATION_DISABLED(100009, "Function disabled.","功能已停用"),
    QUERY_FAIL(100010, "Query failed.","查询失败"),
    DELETE_FAIL(100011, "Delete failed.","删除失败"),
    CREATE_FAIL(100012, "Create fail.","创建失败"),
    UPDATE_FAIL(100013, "Update fail.","更新失败"),
    UPLOAD_FAIL(100014, "Upload fail.","上传失败"),
    SAVE_FAIL(100015, "Save failed.","保存失败"),
    GET_LDAP_CONF_FAIL(100016, "Get ldap config failed.","获取Ldap配置失败"),
    HTTP_EXCUTE_FAILED(100017, "Http execute failed.","http访问失败."),
    FILE_CONTENT_BLANK(100018, "File content is blank.","文件内容不能为空."),
    FILE_TYPE_SUPPORT(100019, "Only support file type","只支持的文件类型"),
    FILE_NOT_FOUND(100020, "File not found","未找到文件"),
    PASSWORD_FORMAT_ERROR(100021, "Password should contain digital and letter, length between 7 and 12.", "密码格式为7-12位数字和字母的组合！"),
    INVALID_MEMORY_UNIT_TYPE(100022, "Invalid memory format.", "内存单位类型错误"),
    NAME_EXIST(100023,"Name exists", "名称已经存在"),
    FORMAT_ERROR(100024,"Format is not correct", "格式错误"),
    CONNECT_TIMEOUT(100025,"Connection timeout.", "连接超时"),
    CONNECT_FAIL(100026,"Connection failed.", "连接失败"),
    STARTED(100027, "Has already started.", "已经启动"),
    STOPPED(100028, "Has already stopped.", "已经停止"),
    NOT_EXIST(100029, "Not exists.", "不存在"),
    EXIST(100030, "Existing.", "已存在"),
    NOT_FOUND(100031, "Not found.", "未找到"),
    AUTH_FAIL(100032, "Auth fail, please check username and password.", "认证未通过, 请检查用户名或密码是否正确"),
    START_DATE_AFTER_END(100033, "Date error, start date is after end date.", "开始时间大于结束时间"),
    RESERVED_KEYWORD(100034, "Reserved keyword, please choose another word", "保留关键字，请修改名称"),
    MESSAGE_SEND_ERROR(100035, "SMS send failed", "短信发送失败"),
    ADMIN_NOT_ENBALE(100036, "Admin account cannot be blocked.", "不能阻止admin用户"),
    HARBOR_HTTPS_ERROR(100037, "Harbor https call error.", "Harbor https访问失败"),
    DOWNLOAD_FAIL(100038, "Download fail.","下载失败"),
    FILE_NOT_EXIST_FAIL(100039, "File is not existing.","文件不存在"),
    DATE_FROM_AFTER_TO(100040, "Date interval error, from date is after to date.","日期区间错误，开始时间在结束时间之后"),
    SYSTEM_IN_MAINTENANCE(100041, "System is under maintenance.", "系统维护中"),
    RESPONSE_TIMEOUT(100042,"Response timeout", "请求获取响应超时"),
    DATE_FORMAT_ERROR(100043,"Date format error", "日期格式错误"),
    EXCEED_MAX_QUERY_COUNT(100044,"Can not query over 100 records at once", "一次查询不能超过100条记录"),
    NAME_LENGTH_LIMIT(100045,"name length must be no more than 63 characters", "名称长度不能超过63个字符"),
    INVALID_CHARACTER(100046,"Format is not correct, contains illegal characters", "格式错误, 包含非法字符或组合"),

    //用户相关 200xxx
    USER_DISABLED(200001, "User is disabled.","该用户暂时停止使用，请联系管理员"),
    USER_NOT_AUTH(200002, "User is not authorized.","该用户未授权，请联系管理员"),
    ONLY_FOR_MANAGER(200003, "Operation is only allowed by admin.","只有管理员用户可以操作"),
    USER_ROLE_DISABLED(200004, "User role is disabled .","当前用户所属角色被禁用"),
    CANNOT_OPERATE_YOURSELF(200005, "The operation is not allowed.","管理员不能操作自己账户"),
    USERNAME_BLANK(200006, "Username can not be blank.","用户名不能为空"),
    USER_REAL_NAME_BLANK(200007, "User real name can not be blank.","用户真实姓名不能为空"),
    USER_REAL_NAME_ERROR(200008, "User real name is not correct.","用户真实姓名错误"),
    USERNAME_PHONE_BLANK(200009, "User phone can not be blank.","用户手机号不能为空"),
    USER_HARBOR_CREATE_FAIL(200010, "Harbor user create fail.","harbor用户创建失败"),
    USERNAME_DUPLICATE(200011, "Username exist.","用户名已存在"),
    USER_EMAIL_BLANK(200012, "User email can not be blank.","邮箱不能为空"),
    USER_EMAIL_DUPLICATE(200013, "User email .","邮箱已经存在"),
    USER_EMAIL_FORMAT_ERROR(200014, "Email format error .","邮箱格式不正确"),
    USER_NOT_EXIST(200015, "User not exist.","用户不存在"),
    USER_UPDATE_INFO_ERROR(200016, "No info need update.","没有需要修改的用户信息"),
    USER_HARBOR_UPDATE_FAIL(200017, "Update harbor user info fail.","更新harbor用户信息失败"),
    USER_HARBOR_UPDATE_PART_FAIL(200018, "Update harbor user info fail. failed harbor server: ","更新harbor用户信息失败,失败的harbor服务器:"),
    PASSWORD_NEW_END_EQ(200019, "New password same with old password","新密码和旧密码相同"),
    PASSWORD_OLD_ERROR(200020, "Old password is not correct","原始密码不正确"),
    INVALID_USERNAME(200021, "User name was invalid.","无效的用户名"),
    USER_EXIST(200022, "User exist.","用户已经存在"),
    USER_NOT_LOGIN(200023, "User not login or timeout.","用户未登录或登录超时|User not login or timeout."),
    USER_STATUS_CHANGED(200024, "User status had already been changed","用户状态已被更改"),
    USER_PERMISSION_DENIED(200025, "Permission denied.","权限不足"),
    USER_NOT_AUTH_OR_TIMEOUT(200026, "User login timeout","用户未授权或登录超时|User not authorized or timeout"),
    USER_GROUP_DELETE_FAIL(200027, "Delete user group failed.","删除用户组失败"),
    USER_GROUP_CREATE_FAIL(200028, "Create user group failed.","创建用户组失败"),
    USER_GROUP_NOT_EXIST(200029, "User group not exist.","用户组不存在"),
    USER_GROUP_BIND_TENANT(200030, "The user group is already bound to the tenant.","用户组已经绑定租户"),
    USER_BIND_TENANT(200031, "The user is already bound to the role.","用户已经绑定角色"),
    USER_GROUP_EXIST(200032, "User group was exist.","用户组已经存在"),
    USER_PERMISSION_DENIED_FOR_PRIVILEGE_CHANGE(200033, "Privilege has changed,permission denied.","权限被管理员修改,权限不足"),
    USER_LOCKED(200034, "The account is locked. Please try after 30 minutes ", "账号已锁定，请30分钟后再试"),
    USER_PASSWORD_CHANGE_SELF(200035, "Only change password for yourself", "只能修改自己的账号密码"),

    //租户 201xxx
    TENANTNAME_EXIST(201001, "TenantName was existed.","租户简称已经存在"),
    TENANTALIASNAME_EXIST(201002, "Name was existed.","租户名已经存在"),
    INVALID_TENANTID(201003, "TenantId was invalid.","无效的租户id"),
    INVALID_TENANTNAME(201004, "TenantName was invalid.","无效的租户名"),
    TENANT_TM_EXIST(201005, "Tenant  was existed.","租户管理员已经存在"),
    TENANT_NETWORK_EXIST(201006, "Tenant network was existed.","租户网络已创建"),
    TENANT_NODE_EXIST(201007, "Tenant private node was existed.","租户独占主机已经存在"),
    TENANT_NODE_NOT_EXIST(201008, "Tenant private node was not existed.","租户独占主机不存在"),
    TENANT_STRATEGY_NOT_EXIST(201009, "Tenant strategy was not existed.","租户策略不存在"),

    //项目 202xxx
    PROJECT_EXIST(202001, "Project was existed.","项目已经存在"),
    PROJECTID_NOT_BLANK(202002, "ProjectId can not be blank.","项目id不能为空"),
    PROJECTNAME_NOT_BLANK(202003, "ProjectName can not be blank.","项目名不能为空"),
    INVALID_PROJECTID(202004, "ProjectId was invalid.","无效的项目id"),
    INVALID_PROJECTNAME(202005, "ProjectName was invalid.","无效的项目名"),
    PROJECT_NOT_EXIST(202006, "Project was not existed.","项目不存在"),
    PROJECT_ROLE_NOT_EXIST(202007, "Role was not existed.","项目角色不存在"),
    PROJECTNAME_EXIST(202008, "ProjectName was existed.","项目简称已经存在"),
    PROJECTALIASNAME_EXIST(202009, "Name was existed.","项目名已经存在"),
    PROJECT_TM_EXIST(202010, "project was existed.","项目管理员已经存在"),
    PROJECT_DELETE_FIRST(202011,"please delete project first","请先删除项目"),
    PROJECT_ROLE_EXIST(202012, "Role was existed.","项目角色已经存在"),
    PROJECT_PM_CANNOT_DELETE(202013, "PM cannot delete yourself.","项目管理员不能删除自己!"),

    //角色权限 203xxx
    ROLE_NOT_EXIST(203001, "Role not exist.","角色不存在!"),
    ROLE_EXIST(203002, "Role exist.","角色已经存在!"),
    PRIVILEGE_EXIST(203003, "Privilege exist.","权限操作已经存在!"),
    PRIVILEGE_NOT_EXIST(203004, "Privilege not exist.","权限操作不存在!"),
    ROLE_ID_BLANK(203005, "Role Id can not be blank","角色ID不能为空!"),
    ROLE_USER_EXIST(203006, "The role have user bindings.","角色有用户绑定!"),
    ROLE_SCOPE_NOT_BLANK(203007, "Role scope can not be blank.","角色作用域不能为空!"),
    ROLE_PRIVILEGE_REPLICATION_NOT_BLANK(203008, "Privilege replication not exist.","权限副本未找到!"),
    RESET_INIT_ROLE_NOLY(203009, "Only the original roles can be reset","只能重置初始角色!"),
    INVALID_USER_TYPE(203010, "UserType was invalid","无效的角色类型！"),
    LOCAL_ROLE_BINDING_CAN_NOT_DELETE(203011,
            "The local role is binding. Please inform tenant admins unbinding users firstly",
            "局部角色已经绑定用户，请先通知相关租户管理员接触绑定!"),
    LOCAL_ROLE_RESOURCE_RULE_NOT_EXIST(203012, "Resource rule not exist","资源规则不存在"),
    LOCAL_ROLE_CONVERT_SQL_CONDITION_FAILED(203013, "Convert sql condition of privilege failed","转换sql条件失败， 请检查原json格式是否有误"),
    LOCAL_ROLE_DATA_PRIVILEGE_FAILED(203014, "Data privilege not enough","数据权限不足"),
    INIT_ROLE_CANNOT_DISABLE(203015, "You cannot disable the original role.","不能禁用初始角色!"),
    SWITCH_ROLE_INCORRECT(203016, "The current state of the user is not available,please login again.","用户当前状态不可用，请重新登录|The current state of the user is not available,please login again."),
    SWITCH_TENANT_INCORRECT(203017, "The switching tenant is not in the user's toggle list.","切换的租户不在用户可切换的列表之内"),
    URL_PERMISSION_DENIED(203018, "Url is not registered，please contact the administrator to register.","URL未注册，请联系管理员注册URL！"),
    INIT_ROLE_CANNOT_DELETE(203019, "You cannot delete the original role.","不能删除默认角色!"),
    ROLE_MENU_NOT_EXIST(203020, "Role menu was not existed.","角色菜单不存在!"),
    LOCAL_ROLE_USER_NOT_IN_CURRENT_PROJECT(203021, "User is not in current project","用户不在当前项目下"),
    LOCAL_ROLE_ALREADY_ASSIGNED_TO_USER(203022, "The role already assigned to this user","该角色已分配给该用户"),
    ROLE_PRIVILEGE_NOT_ASSIGNED(203023, "User role privilege not assigned.","当前用户所属角色未分配权限"),
    ROLE_DISABLE(203024, "User roles are disabled.","当前用户角色被停用，请联系管理员|User roles are disabled."),
    ROLE_PRIVILEGE_NOT_BLANK(203025, "Role privilege can not be blank.","角色权限不能为空!"),
    ROLE_PRIVILEGE_CANNOT_UPDATE(203026, "Admin role privilege can not be update.","管理员角色权限不能被修改"),
    ADMIN_ROLE_CANNOT_DISABLE(203027, "Admin cannot be disabled.","不能禁用管理员"),

    //数据权限204xxx
    GROUP_EDIT_NO_PRIVILEGE(204001, "You cannot edit the privilege group for this data.", "无权限修改该数据的用户权限列表"),
    GROUP_USER_EXIST(204002, "User already exist in group.", "列表中已存在该用户"),
    PARENT_GROUP_USER_DELETE_FIRST(204003, "User in parent data privilege group, remove it first", "请先删除父资源对应数据权限列表中的此用户"),
    PARENT_RW_GROUP_USER_DELETE_FIRST(204004, "User in parent data read-write privilege group, remove it first", "请先删除父资源读写数据权限列表中的此用户"),
    GROUP_QUERY_ERROR(204005, "Privilege group query failed", "数据权限组查询失败"),
    DATA_PRIVILEGE_UPDATE_ERROR(204006,"Service has conflict with application on data privilege, please update the application first with referring to the operation manual","修改的服务权限与应用权限冲突，请先修改当前用户的应用权限，详细规则请查看平台操作手册"),
    DATA_NOT_FOUND(204007, "Data fetch failed", "数据获取失败"),

    //集群相关错误代码 300xxx
    CLUSTER_NAME_DUPLICATE(300001, "Cluster Name Duplicate.", "集群名称已存在"),
    CLUSTER_HOST_EXIST(300002, "Cluster Host Exist.","集群主机已存在"),
    NODE_CONNECT_ERROR(300003, "Can not connect to node server.","服务器连接失败"),
    CLUSTER_CONNECT_ERROR(300004, "Can not connect to cluster api server.","连接集群ApiServer失败"),
    HARBOR_VERIFY_ERROR(300005, "Harbor info verified error.","验证harbor信息失败"),
    ADD_CLUSTERQUOTA_INCORRECT(300006, "Cluster quota incorrect.","添加的配额不正确"),
    CLUSTERID_NOT_BLANK(300007, "Cluster id can not be blank.","集群id不能为空"),
    IMG_SYNC_CLUSTER_LEVEL_NOT_MATCH(300008, "Source cluster level is bigger than target.", "原集群权重大于目标集群"),
    CLUSTER_NOT_FOUND(300009, "Cluster not found.","未找到集群"),
    NAMESPACE_MEMORY_TYPE_ERROR(300010, "Memory type error.","错误的内存配额"),
    CLUSTER_NAME_UNUPDATE(300011, "Cluster name can not update","集群名字不可以修改"),
    CLUSTER_BODY_DATA_ERROR(300012, "Cluster body data is error", "传递body数据有问题"),
    DATA_CENTER_QUERY_FAIL(300013, "Fail to query data center", "获取数据中心失败"),
    CLUSTER_STATUS_QUERY_FAIL(300014, "Fail to get cluster status.","获取集群状态信息错误"),
    CLUSTER_SERVICE_PORT_ERROR(300015, "Service port error.","服务端口错误"),
    CLUSTER_ES_SERVICE_ERROR(300016, "Cant not connect to es service.","集群es组件连接失败"),
    CLUSTER_DATA_INIT_ERROR(300017, "Cluster data init error.","集群信息初始化失败"),
    CLUSTER_TOKEN_ERROR(300018, "Cluster token error.","获取集群访问k8s apiserver的令牌失败"),
    CLUSTER_LIST_NOT_BLANK(300019, "Cluster list can not blank.","集群列表不能为空"),
    MEMORY_QUOTA_OVER_FLOOR(300020, "Memory quotas exceed available quotas.","memory配额超过可使用的配额"),
    RESOURCE_QUOTA_NOT_LESS_ZERO(300021, "Resource quota error.","资源配额错误"),
    CLUSTER_RESOURCE_NOT_ZERO(300022, "Cluster resource can not be zero.","集群总资源不能为零"),
    NETWORK_ALREADY_BIND(300023, "Network has been binding.","网络已经被绑定"),
    CLUSTER_STATUS_NOT_NEED_UPDATE(300024, "Cluster status is identical, no need to update.","集群状态一致，不需要更新"),
    CPU_QUOTA_OVER_FLOOR(300025, "CPU quotas exceed available quotas.","cpu配额超过可使用的配额"),
    TEMPLATE_NOT_FOUND(300026,"Template not found","模板查询错误"),
    TEMPLATE_STATUS_NOT_DELETE(300027,"Template status(use) is true","模板状态为已经被使用，不可删除"),
    TEMPLATE_NAME_CAN_NOT_UPDATE(300028,"Template name can not be update","模板名字不允许更新"),
    NODE_NOT_FIND_IN_CLUSTER(300029, "No node in cluster.", "集群内没有主机"),
    DOCKER_CONNECT_TYPE_NOT_SET(300030, "Docker connect type is not set.", "未设置docker连接方式"),
    CLUSTER_DATA_DELETE_FAIL(300031, "Cluster data delete fail.", "删除集群数据失败"),
    CLUSTER_NOT_IN_SCOPE(300032, "Cluster is not in the available list.", "集群不在用户能操作的范围之内"),
    DATACENTER_HAS_CLUSTER(300033,"Datacenter has cluster ","该数据中心有集群，不可删除"),
    ROLE_HAVE_DISABLE_CLUSTER(300034,"No cluster or cluster is disabled for this role.","该角色没有集群或者集群被停用"),
    DATACENTER_NOT_HAS_NICENAME(300035,"Datacenter do not have nickname.","该数据中心没有别名"),
    DATACENTER_NICENAME_SAME(300036,"New nickname is same with old nickname.","别名一致"),
    DATACENTER_UPDATE_FAIL(300037,"Datacenter update fail","该数据中心更新失败"),
    LIST_CLUSTERQUOTA_INCORRECT(300038, "Get cluster quota failed.","获取配额失败"),
    RESOURCE_OVER_FLOOR(300039, "Tenant cluster quota cannot be greater than the available storage capacity.","租户集群配额不能大于可使用的存储容量"),
    RESOURCE_BEHIND_FLOOR(300040, "Tenant cluster quota cannot be less than the used storage capacity.","租户集群配额不能小于已使用的存储容量"),
    UPDATE_CLUSTERQUOTA_INCORRECT(300041, "Cluster quota incorrect，please refresh the page resource.","配额不正确,请重新刷新页面资源"),
    STORAGE_QUOTA_OVER_FLOOR(300042, "Storage quotas exceed available quotas.","分区存储配额超过可使用的配额"),
    CLUSTER_QUOTA_DELETE_FAIL(300043, "Failed to delete cluster quota, storage in quota used in cluster partition", "删除集群配额失败，集群分区中已使用配额中的存储"),
    DATACENTER_NAME_DUPLICATE(300044, "Datacenter Name Duplicate.", "数据中心名称已存在"),
    INGRESS_CONTROLLER_NOT_FOUND(300045, "Ingress controller not found under the cluster.", "集群下没有找到负载均衡器"),
    INGRESS_CONTROLLER_PORT_RANGE_NOT_FOUND(300046, "Cannot get the port range of the ingress controller.", "获取不到负载均衡器的端口范围"),
    INGRESS_CONTROLLER_EXIST(300047, "The ingress controller already exists.", "负载均衡器已经存在"),
    INGRESS_CONTROLLER_HTTP_PORT_USED(300048, "The http port of the ingress controller has been used.", "负载均衡器HTTP端口已经被使用"),
    INGRESS_CONTROLLER_OTHER_PORT_USED(300049, "The other port of the ingress controller has been used.", "负载均衡器其他端口已经被使用"),
    INGRESS_CONTROLLER_SA_NOT_FOUND(300050, "Cannot get the default serviceAccount under the kube-system namespace.", "获取不到kube-system分区下默认的serviceAccount"),
    INGRESS_CONTROLLER_DEFAULT_NOT_DELETE(300051, "The default ingress controller is not allowed to be deleted.", "全局负载均衡器不允许被删除"),
    INGRESS_CONTROLLER_HAD_USED(300052, "The ingress controller is in using, cant not be changed or delete.", "已有服务使用该负载均衡器, 不能修改或删除"),
    INGRESS_CONTROLLER_PORT_UPDATE_FAIL(300053, "In the database, the ingress controller port data update failed, please contact the administrator.", "数据库中，ingress controller端口数据更新失败，请联系管理员"),
    INGRESS_CONTROLLER_HAS_USED_BY_TENANTS(300054, "These tenants are using this ingress controller and cannot be removed. Tenant name：", "以下租户已使用该负载均衡器，无法移除。租户名称："),
    INGRESS_CONTROLLER_HTTP_PORT_ERROR(300055, "The port of the ingress controller is not in the specified range.", "负载均衡器的端口不在指定范围"),
    RESOURCE_USED_OVER_QUOTA(300056, "Quota cannot be smaller than used.","配额不能小于已使用的容量"),

    //主机节点 301xxx
    NODE_LABEL_CREATE_ERROR(301001, "Node label create failed.","主机标签创建失败"),
    NODE_LABEL_ERROR(301002, "Node label error.","主机标签错误"),
    NODENAME_NOT_BLANK(301003, "Node name can not blank.","主机名称不能为空"),
    NODE_NOT_EXIST(301004, "Node was not existed.","主机不存在"),
    NODE_EXIST(301005, "Node was existed.","主机已经存在"),
    WORK_NODE_OFFLINE(301006, "Work node can not removed.","工作主机不能下线"),
    DRAIN_IN_PROCESS(301007, "There is already a migrate progress in process.","主机应用迁移已在进行中"),
    NODE_STATUS_REQUIRE(301008, "Only allow for idle or stateless node","只能修改闲置主机或者无状态主机"),
    PRIVATE_NODE_NOT_EXIST(301009, "Warn: Can not find private node for this namespace, node label has been modified",
            "警告：已经删除当前分区，但是当前分区为私有分区，所属独占主机属性被修改，导致独占主机不存在，请不要随意更改主机私有属性，以免发生冲突，如果在接下来的操作中遇到其它问题，请联系管理员!"),
    NODE_NOT_REMOVE(301010, "The node has other pods ,please delete it.","主机有其他应用pod，请删除pod后再移除"),
    NODE_POD_NONE(301011, "There is no pod on the node.","主机上没有运行pod"),
    NODE_CANNOT_REMOVED(301012, "Node can not be removed.","主机已经分配给租户不能下线，请在对应的租户配额中移除该主机后下线!"),
    NODE_CANNOT_REMOVED_FORTENANT(301013, "Node can not be removed.","主机已经分配给分区不能移除，请在对应的分区中移除该主机!"),
    NODE_LABEL_UPDATE_ERROR(301014, "Node status update failed.","主机状态更新失败"),
    NODE_UNSCHEDULABLE_ONLY(301015, "Only allow for maintain node.","只允许操作维护状态主机"),
    NODE_STATUS_NOT_REMOVE(301016, "The node has other pods ,please drain application.","主机有其他应用pod，请应用迁移后再修改"),

    //分区 302xxx
    NAMESPACE_NOT_BLANK(302001, "Namespace name can not be blank.","分区名不能为空"),
    NAMESPACE_NOT_FOUND(302002, "Namespace not found.","分区未找到"),
    NAMESPACE_EXIST(302003, "Namespace was existed.","分区已经存在"),
    NAMESPACE_CREATE_ERROR(302004, "Create namespace failed.","分区创建失败"),
    NAMESPACE_QUOTA_CREATE_ERROR(302005, "Create namespace quota failed.","分区配额创建失败"),
    NAMESPACE_POLICY_CREATE_ERROR(302006, "Create namespace policy failed.","分区策略创建失败"),
    NAMESPACE_HA_POLICY_CREATE_ERROR(302007, "Create namespace HA policy failed.","分区HA策略创建失败"),
    NAMESPACE_QUOTA_NOT_BLANK(302008, "Namespace quota can not blank.","分区配额不能为空"),
    INVALID_NS_NAME(302009, "Namespace was invalid.","无效的分区名"),
    NAMESPACE_DELETE_FIRST(302010,"Please delete namespace first","请先删除分区"),
    PRIVATE_NAMESPACE_ONLY(302011,"Only private namespaces are allowed.","只允许操作私有分区"),
    NAMESPACE_CREATE_ERROR_DELETED(302012, "Namespace is being deleted: namespaces already exists.","分区正在被删除的过程中,请稍后创建!"),
    //应用 400xxx
    SCRIPT_NOT_EXIST(400001, "Script file not found.","脚本文件未找到"),
    TOPOLOGY_NOT_EXIST(400002, "Network topology not existed.","网络拓扑图不存在"),
    TOPOLOGY_EXIST(400003, "Network topology was existed.","网络拓扑图已存在"),
    TOPOLOGY_ADD_FAIL(400004,"Topology add fail.","网络拓扑图添加失败"),
    DAEMONSET_EXIST(400005, "DaemonSet Exist.", "DaemonSet已存在"),
    DAEMONSET_CREATE_FAILURE(400006, "Create DaemonSet failure.", "创建DaemonSet失败"),
    DAEMONSET_UPDATE_FAILURE(400007, "Update DaemonSet failure.", "更新DaemonSet失败"),
    DAEMONSET_GET_FAILURE(400008, "Get DaemonSet failure.", "获取DaemonSet失败"),
    DAEMONSET_NOT_EXIST(400009, "DaemonSet not exist.", "DaemonSet不存在"),
    DAEMONSET_DETAIL_GET_FAILURE(400010, "Get detail of DaemonSet failure.", "获取DaemonSet详情失败"),
    CONFIGMAP_NOT_EXIST(400011, "ConfigMap not exist.", "配置文件不存在"),
    POD_NOT_EXIST(400012, "Pod not exist.", "POD不存在"),
    DEPLOYMENT_NOT_FIND(400013, "Service not found.", "服务未找到"),
    DEPLOYMENT_GET_FAILURE(400014, "Get Service failure.", "获取服务失败"),
    DEPLOYMENT_UPDATE_FAILURE(400015, "Update Service failure.", "更新服务失败"),
    DEPLOYMENT_NAME_DUPLICATE(400016, "Service name duplicate:", "服务名称已存在:"),
    HTTP_INGRESS_NAME_DUPLICATE(400017, "HTTP Service name duplicate.", "HTTP服务名称已存在"),
    TCP_INGRESS_NAME_DUPLICATE(400018, "Expose port in use or HTTP Service name duplicate.", "对外暴露端口被使用或HTTP服务名称已存在"),
    APPLICATION_NAME_DUPLICATE(400019, "Application name duplicate.", "应用名称已存在"),
    APPLICATION_CREATE_ROLLBACK_FAILURE(400020, "Create application rollback failure.", "创建应用时回滚失败"),
    DEPLOYMENT_CANARY_SCALE_FAILURE(400021, "Canary update service failure.", "灰度升级失败"),
    DEPLOYMENT_PAUSE_CANARY_SCALE_FAILURE(400022, "Pause canary update service failure.", "暂停灰度升级失败"),
    DEPLOYMENT_SCALE_INSTANCE_FAILURE(400023, "Scale instance failure.", "手动伸缩实例失败"),
    APPLICATION_CREATE_FAILURE(400024, "Create application failure.", "应用创建失败"),
    SERVICE_DELETE_FAILURE(400025, "Delete service failure.", "服务删除失败"),
    TCP_SERVICE_DELETE_FAILURE(400026, "Delete TCP Service failure.", "TCP服务删除失败"),
    SERVICE_CREATE_ROLLBACK_FAILURE(400027, "Create service rollback failure.", "创建服务时回滚失败"),
    SERVICE_CREATE_FAILURE(400028, "Create service failure.", "创建服务失败"),
    SERVICE_BLUE_GREEN_FAILURE(400029, "Service blue-green deploy failure.", "蓝绿发布失败"),
    SERVICE_BLUE_GREEN_SWITCH_FLOW_FAILURE(400030, "Service blue-green deploy failure.", "流量切换失败"),
    SERVICE_BLUE_GREEN_UPDATE_FAILURE(400031, "Service blue-green update failure.", "蓝绿发布升级失败"),
    SERVICE_BLUE_GREEN_ROLLBACK_FAILURE(400032, "Service blue-green rollback failure.", "蓝绿发布回滚失败"),
    EXT_SERVICE_DELETE_FAIL(400033, "external service delete error", "外部服务删除失败"),
    SYSTEM_NGINX_CONFIGMAP_NOT_FIND(400034, "External router not find.", "对外访问路由不存在"),
    POD_IS_BLANK(400035, "Pod can not be blank.", "未选择pod"),
    SYSTEM_NO_EXTERNAL_PORT_IN_CLUSTER(400036, "No external port can be used in the cluster.", "集群内没有可用对外端口"),
    SERVICE_NOT_READY(400037, "Service is starting, please try later.", "服务正在启动，请稍后再试"),
    JOB_FINISHED(400038,"job had finished or failed","Job已经完成或者失败了"),
    APP_DELETE_FAILED(400039, "App delete failure", "应用删除失败"),
    NS_POD_CONTAINER_NOT_BLANK(400040, "deploy pod and container can not be blank at same.","服务名、pod名称和容器名称不能都为空"),
    NAMESPACE_RESOURCE_INSUFFICIENT(400041, "Namespace resource insufficient.","分区cpu或内存不足"),
    CONFIGMAP_IS_EMPTY(400042, "ConfigMap is blank.", "配置文件内容为空"),
    APPLICATION_NAME_CONFLICT_MSF(400043, "Application name belong to micro service component", "应用名称所属于微服务组件"),
    SERVICE_EXPOSE_NGINX_FAILED(400044, "Port and protocol is different from in containers.", "选择的端口或协议与容器内的不一致"),
    APPLICATION_CAN_NOT_STOP(400045, "Service in application in the upgrade.", "所属服务的状态处于灰度或蓝绿升级中."),
    SERVICE_TYPE_NOT_EXIST(400046, "ServiceType was not existed.", "服务类型不存在"),
    TCP_IC_DEFAULT_ONLY(400047, "TCP/UDP is accept only with default ingress controller.", "TCP/UDP协议只能使用默认的负载均衡器"),
    DEPLOY_VERSION_IS_NULL_WHEN_ISTIO_ENABLE(400048, "Deploy version can not be null when istio injection is enable.", "开启Istio注入，服务版本标签不能为空"),

    //模板 401xxx
    SERVICE_TEMPLATE_NOT_EXIST(401001, "Service template not exist.", "服务模板不存在"),
    TEMPLATE_SEARCH_KEY_ERROR(401002, "Application template search key error.", "查询模板的关键字key错误"),
    SERVICE_TEMPLATE_NOT_BLANK(401003, "Service template cannot be blank.", "服务模板不能为空"),
    APPLICATION_TEMPLATE_NOT_EXIST(401004, "Application template not exist.", "应用模板不存在"),
    APPLICATION_TEMPLATE_GET_FAILURE(401005, "Get application template failure.", "应用模板获取失败"),
    SERVICE_TEMPLATE_NOT_EXIST_IN_APPLICATION_TEMPLATE(401006, "Service template not exist in application template.", "在应用模板内没有服务模板"),
    SERVICE_TEMPLATE_IMAGE_INFO_NOT_NULL(401007, "Image info not exist in service template.", "服务模板内不存在镜像信息"),
    APPLICATION_TEMPLATE_NAME_DUPLICATE(401008, "Application template name duplicate", "应用模板名称已存在"),
    SERVICE_TEMPLATE_NAME_DUPLICATE(401009, "Service template name duplicate", "服务模板名称已存在"),
    //存储 402xxx
    PV_DELETE_FAIL(402001, "Fail to delete PV.", "PV删除失败"),
    PV_CREATE_FAIL(402002, "Fail to create PV.", "PV创建失败"),
    PV_NAME_FORMAT_ERROR(402003, "PV name format error.", "PV名称格式错误"),
    PV_QUERY_FAIL(402004, "PV query fail.", "PV查询失败"),
    PV_PROVIDER_NOT_EXIST(402005, "PV is not provided.", "PV存储未提供"),
    PV_RELEASE_FAIL(402006, "PV released failed.", "PV释放失败"),
    NFS_PROVISIONER_CREATE_FAIL(402007, "NFS server address or directory parameter error.", "NFS服务器地址或目录参数错误"),
    STORAGECLASS_TYPE_ERROR(402008, "The storage type is not supported", "StorageClass使用的存储类型目前尚不支持"),
    STORAGECLASS_DELETE_ERROR(402009, "Failed to delete StorageClass. StorageClass is already used. Please delete related storage volume first.", "StorageClass删除失败，StorageClass已经被使用，请先删除相关存储卷"),
    PVC_CAN_NOT_DELETE(402010, "The storage has been used, not to delete.", "该存储已经被使用，不允许删除"),
    NFS_PROVISIONER_CONFIG_ERROR(402011, "The configuration of the nfs plugin image is not found in the system configuration", "系统配置中没有找到nfs插件镜像的配置"),
    RECYCLE_POD_CONFIG_ERROR(402012, "The configuration to clear the storage plugin image was not found in the system configuration.", "系统配置中没有找到清空存储插件镜像的配置"),
    PVC_UPDATE_ERROR(402013, "PVC update failed", "PVC更新失败"),
    CEPH_RBD_PROVISIONER_CREATE_FAIL(402014, "Create ceph rbd plugin failure.", "ceph rbd 插件创建失败"),
    CEPH_RBD_PROVISIONER_CONFIG_ERROR(402015, "The configuration of the ceph rbd plugin image is not found in the system configuration", "系统配置中没有找到ceph rbd插件镜像的配置"),
    CEPH_RBD_SECRET_CREATE_FAIL(402016, "Create ceph rbd secret failure.", "ceph rbd secret创建失败"),
    CEPH_RBD_PROVISIONER_NOT_EXIST(402017, "Ceph rbd plugin not exist.", "Ceph rbd 插件不存在"),
    PV_CAN_NOT_DELETE(402018, "PV status is bound， not to delete.", "PV状态为bound，不允许删除"),

    MYSQL_INSERT_FAIL(402019, "DB insert fail", "数据插入失败"),

    //弹性伸缩 403xxx
    SERVICE_AUTOSCALE_CREATE_FAILURE(403001, "Create autoScale failure.", "自动伸缩创建失败"),
    SERVICE_AUTOSCALE_DELETE_FAILURE(403002, "Delete autoScale failure.", "自动伸缩删除失败"),
    AUTOSCALE_CONDITION_REQUIRE(403003, "can not create autoscale for TPS metric as application do not have create service.", "该应用未对外创建服务，不能根据TPS指标伸缩"),
    AUTOSCALE_NOT_SELECTED(403004, "Please select a metric for autoscale", "至少需要设置一项伸缩指标"),
    AUTOSCALE_METRIC_NOT_SUPPORT(403005, "not support for metric", "不支持的伸缩指标"),
    AUTOSCALE_TIME_MAX_MIN_ERROR(403006,"Max,min replicas value is error", "max ,min 值错误"),
    AUTOSCALE_TIME_ZONE_ERROR(403007,"Time zone value is error","负载均衡时间段错误"),
    SERVICE_AUTOSCALE_UPDATE_FAILURE(403008, "Create autoScale failure.", "自动伸缩升级失败"),
    AUTOSCALE_NOT_FOUND(403009, "Can not found autoscale.", "自动伸缩找寻不到"),
    SERVICE_AUTOSCALE_LABEL_UPDATE_FAILURE(403010, "Update autoscale label failure.", "更新自动伸缩标签失败"),
    AUTOSCALE_TIME_PODS_ERROR(403011,"Time based pods num should be greater than min pods and smaller than max pods.",
            "时间段实例数应大于或等于最小实例数，并小于或等于最大实例数"),


    //日志管理 404xxx
    QUERY_LOG_TOPOLOGY_ERROR(404001, "Get topology failed.","查询全链路拓扑图失败"),
    QUERY_LOG_CONTENT_ERROR(404002, "Get log content failed.","查询全链路日志内容失败"),
    LIST_LOG_ERROR(404003, "list log file list error.","获取服务日志列表失败"),
    LOG_EXPORT_FAILED(404004, "Log export failed","导出日志失败"),
    APP_LOG_RULE_NOT_EXIST(404005,"App log not rule not exist","应用日志规则不存在"),
    APP_LOG_SNAPSHOT_CREATE_REPO_FAILED(404006,"Failed to create snapshot repository","创建快照仓库失败"),
    APP_LOG_SNAPSHOT_CREATE_FAILED(404007,"Failed to create snapshot","创建快照失败"),
    APP_LOG_SNAPSHOT_QUERY_FAILED(404008,"Failed to query snapshot","查询快照失败"),
    APP_LOG_SNAPSHOT_DELETE_FAILED(404009,"Failed to delete snapshot","删除日志快照失败"),
    APP_LOG_SNAPSHOT_RESTORE_FAILED(404010,"Failed to restore snapshot","恢复日志快照失败"),
    APP_LOG_REPO_MISSING(404011,"Repository missing. Please create a new snapshot","快照仓库未找到，请先为该集群创建快照"),
    APP_LOG_SNAPSHOT_INDEX_NOT_EXIST(404012,"There is no log index for this day","该日期没有产生日志"),
    LOG_SNAPSHOT_DATE_ERROR(404013,"log snapshot date error","日志快照时间日期错误"),
    LOG_SNAPSHOT_NO_INDEX(404014,"No log for this date","该日期没有日志"),
    LOG_SNAPSHOT_RESTORE_EXISTS(404015,"log has already been restored.","日志已恢复"),
    LOG_SEARCH_TYPE_NOT_SUPPORT(404016,"search type not support.","查询类型不支持"),

    //镜像 405xxx
    IMAGE_SET_CLEAN_RULE_FAILED(405001, "Set Cleaning Rule failed", "设置清理规则失败"),
    IMAGE_SYNC_CREATE_DEST_HARBOR_PROJECT_FAILED(405002, "Create project in target harbor failed", "在目标Harbor中创建项目失败"),
    IMAGE_DELETE_DEFAULT_REPOSITORY_NOT_ALLOWED(405003, "Can not delete default repository", "默认镜像仓库不能删除"),
    IMAGE_CREATE_HARBOR_PROJECT_FAILED(405004, "Create harbor project failed", "创建Harbor project失败"),
    IMAGE_TARGET_REPOSITORY_NOT_EXIST(405005, "Target repository not exist.", "目标镜像仓库不存在"),
    IMAGE_SOURCE_DEPOSITORY_NOT_EXIST_NEED_PROJECT_NAME(405006, "Source repository not exist. Please input project name", "原默认镜像仓库不存在，请输入项目名"),
    IMAGE_CREATE_REPOSITORY_FAILED(405007, "Create repository failed.", "创建镜像仓库失败"),
    IMAGE_REPOSITORY_NOT_FOUND(405008, "Image repository not found.", "镜像仓库不存在"),
    REPLICATION_EXIST(405009, "this repository had already created replication.", "仓库已创建同步规则"),
    REPLICATION_ENABLE(405010, "replication is enable, please stop first.", "同步规则已启用，请先停止"),
    REPLICATION_PROCESSING(405011, "please waiting for replication job stop.", "停用同步规则后需要等待正在进行的同步任务停止，请稍后删除"),
    REPLICATION_USING(405012, "please stop and delete replication first.", "请先停止并删除镜像仓库同步规则"),
    REPLICATION_STOPPING(405013, "old replication is stopping, please try later.", "正在停止原同步规则的同步任务，请稍后再试"),
    IMAGE_UPLOADING(405014, "image is uploading.", "镜像正在上传"),
    PUBLIC_REPOSITORY_DELETE(405015, "Public project can not be deleted.", "公共镜像仓库不能删除"),
    IMAGE_LOAD_ERROR(405016, "error to load image from file", "从文件中加载镜像失败"),
    IMAGE_UPLOAD_SINGLE(405017, "only support single image upload at once", "只支持单个镜像上传"),
    REPOSITORY_CREATE_FAIL(405018, "failed to create repository on harbor for cluster", "在以下集群对应的harbor上创建镜像仓库失败"),
    HARBOR_AUTH_ACCESS_FAIL(405019, "failed to authorize image repository access on harbor", "授权用户访问镜像仓库失败"),
    IMAGE_RULE_NAME_ALREADY_EXIST(405020, "Rule name already exists", "规则名称已经存在"),
    IMAGE_RULE_REPO_ALREADY_EXIST(405021, "Rule of the repository already exists", "仓库已创建规则"),
    HARBOR_QUERY_PROJECT_BY_REPOSITORY_FAILED(405022, "Query project by repository failed","查询项目失败"),
    HARBOR_FIND_ERROR(405023, "Harbor server find error, host is","查找harbor服务器错误, host:"),
    HARBOR_AUTH_FAIL(405024, "harbor auth fail", "harbor验证失败"),
    IMAGE_SYNC_NOT_FOUND(405025, "sync image error as not found repository for dest cluster", "镜像推送失败，未找到目标环境的镜像仓库"),
    IMAGE_LIST_ERROR(405026, "List image error", "查询镜像失败"),
    IMAGE_PUBLIC_SYNC_DENIED(405027, "Can not sync image to another cluster for public repository", "不能推送公共镜像仓库下的镜像"),
    IMAGE_PRD_SYNC_DENIED(405028, "Can not sync image to another cluster for production repository", "不能推送生产环境镜像仓库下的镜像"),
    IMAGE_SYNC_DEST_EXIST(405029, "Sync image error. Same image name and tag exist in dest repository", "推送失败, 目标镜像已存在"),
    HARBOR_QUOTA_UPDATE_EXCEED(405030, "used size greater than quota size", "已经使用的磁盘用量超过要设置的配额"),
    REPLICATION_DELETE_FAIL(405031, "Replication policy delete error. Please delete manually.", "镜像同步规则删除失败，请手动删除"),
    REPOSITORY_DELETE_FAIL(405032, "failed to delete repository on harbor for cluster", "在以下集群对应的harbor上删除镜像仓库失败"),
    IMAGE_DOWNLOAD_PREPARE_ERROR(405033, "Image is not present, please pull images first.", "请先拉取镜像，然后下载镜像"),
    IMAGE_IN_PULLING(405034, "There is already a pulling process for this image, please wait", "镜像已经在拉取中，请等待"),
    IMAGE_IN_PULLING_DELETE_ERROR(405035, "Image is pulling, can not be delete now, please try later when image is pulled",
            "镜像正在在拉取中，不能删除文件，请等待镜像文件已经生成之后删除"),
    HARBOR_PROJECT_QUOTA_EXCEED(405036, "Harbor project quota exceeds.", "仓库磁盘配额已用完"),
    HARBOR_IN_GARBAGE_CLEAN(405037, "Harbor is in garbage cleaning, pleas try later.",
            "镜像仓库正在清理镜像垃圾文件，请稍后再试"),
    PUBLIC_HARBOR_PROJECT_CLEAN_ACCESS(405038, "Public harbor project image clean rule is only allow for admin",
            "公共镜像仓库清理规则需要系统管理员权限可以设置"),
    HARBOR_COOKIE_INVALID(405039, "Harbor unauthorized or login timeout, please retry.", "Harbor验证失败或登录超时，请重试"),
    HARBOR_PROTOCOL_INVALID(405040, "If harbor enabled https protocol, please use https replace http",
            "如果harbor启用https协议，请用https访问harbor"),
    IMAGE_DELETE_TIMEOUT(405041, "Maybe image file is too large to delete within 1 minutes, please check result later.",
            "可能由于镜像文件较多，未能在1分钟内全部删除，请稍后查询删除结果"),
    REPLICATION_TARGET_USING(405042, "Target server is used by replication rules, please delete replication rule first.",
            "同步规则正在使用该备份服务器，请先删除同步规则"),
    IMAGE_IN_DELETING(405043, "This image is in deleting, please check later.", "镜像正在删除中,请稍后查看删除结果"),
    LARGE_IMAGE_DELETE(405044, "As image is large,it take some time to delete, please check later.", "由于镜像文件较大，需要一些时间删除，请稍后查看删除结果"),
    HARBOR_PROJECT_NOT_FOUND(405045, "Project not found on harbor.", "在harbor上未找到该镜像仓库，或已被删除"),

    //CICD 406xxx
    ENVIRONMENT_NAME_NOT_BLANK(406001, "Build environment name can not be blank.", "环境名称不能为空"),
    ENVIRONMENT_NAME_DUPLICATE(406002, "Build environment name duplicate.", "环境名称已存在"),
    ENVIRONMENT_ADD_FAIL(406003, "Build environment add fail.", "新增环境失败"),
    ENVIRONMENT_DELETE_FAIL(406004, "Build environment delete fail.", "删除环境失败"),
    ENVIRONMENT_USED(406005, "Build environment is used by pipeline, can not be deleted.", "环境被流水线使用中，无法删除"),
    ENVIRONMENT_UPDATE_FAIL(406006, "Build environment update fail", "更新环境信息失败"),
    CLUSTER_ID_NOT_NULL(406007, "Cluster id can not be null.", "集群id不能为空"),
    PROJECT_ID_NOT_NULL(406008, "Project id can not be null.", "项目id不能为空"),
    DEPENDENCE_NAME_DUPLICATE(406009, "Dependence name duplicate.", "依赖名称已存在"),
    DEPENDENCE_CREATE_FAIL(406010, "Dependence create fail.", "依赖创建失败"),
    DEPENDENCE_NAME_NOT_BLANK(406011, "Dependence name can not be blank.", "依赖名称不能为空"),
    DEPENDENCE_DIRECTORY_LIST_FAIL(406012, "Dependence directory list fail.", "依赖目录查询失败"),
    DEPENDENCE_FILE_RM_FAIL(406013, "Dependence file remove fail.", "依赖文件删除失败"),
    DEPENDENCE_FILE_UPLOAD_FAIL(406014, "Dependence file upload fail.", "依赖文件上传失败"),
    PARAMETER_NAME_NOT_BLANK(406015, "Parameter name can not be blank.", "参数名称不能为空"),
    DOCKERFILE_NAME_DUPLICATE(406016, "Dockerfile name duplicate.", "Dockerfile名称已存在"),
    DOCKERFILE_CREATE_FAIL(406017, "Create Dockerfile failed.", "Dockerfile创建失败"),
    DOCKERFILE_NAME_NOT_BLANK(406018, "Dockerfile name can not be blank.", "Dockerfile名称不能为空"),
    PIPELINE_NAME_NOT_BLANK(406019, "Pipeline name can not be blank.", "流水线名称不能为空"),
    PIPELINE_NAME_DUPLICATE(406020, "Pipeline name duplicate.", "流水线名称已存在"),
    PIPELINE_CREATE_ERROR(406021, "Pipeline create failed.", "流水线创建失败"),
    PIPELINE_NAME_VALIDATE_ERROR(406022, "Pipeline name validate failed.", "流水线名称验证失败"),
    PIPELINE_NOT_EXIST(406023, "Pipeline not exist.", "流水线不存在"),
    PIPELINE_DELETE_ERROR(406024, "Pipeline delete failed.", "流水线删除失败"),
    PIPELINE_BUILD_ERROR(406025, "Pipeline build failed.", "流水线构建失败"),
    PIPELINE_DEPLOY_ERROR(406026, "Pipeline deploy failed.", "流水线部署失败"),
    SERVICE_NAME_NOT_BLANK(406027, "Service name can not be blank.", "服务名不能为空"),
    STAGE_BUILD_NOT_EXIST(406028, "Stage build not exist.", "构建记录不存在"),
    DEPLOY_IMAGE_NAME_ERROR(406029, "Deploy image error.", "部署镜像名错误"),
    STAGE_ADD_ERROR(406030, "Stage add error", "新增步骤失败"),
    STAGE_NOT_EXIST(406031, "Stage not exist.", "流水线步骤不存在"),
    TEST_SUITE_NOT_EXIST(406032, "Test suite not exist.", "测试套件不存在"),
    DOCKERFILE_USED_BY_PIPELINE(406033, "Dockerfile is used by pipeline, can not be deleted.", "DockerFile被流水线使用中，无法删除"),
    STAGE_DELETE_ERROR(406034, "Stage delete error", "删除步骤失败"),
    STAGE_UPDATE_ERROR(406035, "Stage update error", "修改步骤失败"),
    SECRET_ADD_ERROR(406036, "Harbor secret add error", "harbor secret增加失败"),
    SECRET_UPDATE_ERROR(406037, "Harbor secret update error", "harbor secret更新失败"),
    DEPENDENCE_USED(406038, "Dependence is used by pipeline, can not be deleted.", "依赖被流水线使用中，无法删除"),
    STAGE_EMPTY(406038, "Pipeline has no stages, can not be built.", "流水线无步骤，无法执行"),
    DOCKERFILE_NOT_EXIST(406039, "Dockerfile not exist, please retry after refresh", "Dockerfile已被删除,请刷新后重试"),
    COPIED_PIPELINE_NOT_EXIST(4060040, "Copied pipeline not exist", "被复制的流水线不存在"),
    DEFAULT_BUILD_ENVIRONMENT_NOT_EXIST(4060041, "Default build environment not exist", "默认构建环境不存在"),
    PIPELINE_BUILD_STOP_ERROR(406042, "Pipeline build stop error.", "流水线停止失败"),
    PIPELINE_CONFIG_ERROR(406043, "Pipeline config error.", "流水线配置失败"),
    CREDENTIAL_SAVE_ERROR(406044, "Repository credential save error", "代码仓库用户名密码保存失败"),
    PIPELINE_ALREADY_DELETED(406045, "Pipeline already deleted", "流水线已被删除，请重新创建流水线"),
    ENVIRONMENT_ALREADY_DELETED(406046, "Build environment already deleted", "构建环境已被删除，请选择其他环境"),
    DEPENDENCE_ALREADY_DELETED(406047, "Dependence already deleted", "依赖已被删除，请选择其他依赖"),
    DOCKERFILE_ALREADY_DELETED(406048, "Dockerfile already deleted", "Dockerfile已被删除，请选择其他Dockerfile"),
    PIPELINE_RENAME_ERROR(406049, "Pipeline rename error", "流水线重命名失败"),
    PIPELINE_CREDENTIALS_USERNAME_NOT_NULL(406050, "Username can not be blank", "用户名不能为空"),
    DEPENDENCE_NO_PRIVILEGE_DELETE(406051, "Only System admin or create user can delete public dependence", "非系统管理员或创建者无法删除公有依赖"),
    ENVIRONMENT_NO_PRIVILEGE_DELETE(406052, "Only System admin or create user can delete public environment", "非系统管理员或创建者无法删除公有环境"),
    SERVICE_ALREADY_IN_BLUE_GREEN_UPGRADE(406053, "Service is already in blueGreen upgrade, please complete or rollback the upgrade first",
            "Service is already in blueGreen upgrade, please complete or rollback the upgrade first(服务正处于蓝绿升级中，请先完成或回滚升级)"),
    SERVICE_ALREADY_IN_CANARY_UPGRADE(406054, "Service is already in blueGreen upgrade, please complete or rollback the upgrade first",
            "Service is already in canary upgrade, please complete or rollback the upgrade first(服务正处于灰度升级中，请先完成或回滚升级)"),
    SERVICE_NOT_STARTED(406055, "Service is not started, cannot do canary upgrade",
            "Service is not started, cannot upgrade(服务未启动，无法升级)"),
    PIPELINE_CONFIG_UPDATE_ERROR_IN_JENKINS(406056, "Pipeline config update failed in Jenkins", "Jenkins流水线配置更新失败"),
    DEPLOY_IMAGE_NOT_EXIST(406057, "Deploy image not exist", "部署镜像不存在"),
    SYNC_STAGE_ERROR(406058, "Sync stage failed", "同步步骤信息失败"),
    JENKINS_PIPELINE_INFO_GET_ERROR(406059, "Jenkins pipeline info get failed", "获取Jenkins流水线信息失败"),
    PIPELINE_NOT_EXIST_IN_JENKINS(406060, "Pipeline not exist in Jenkins, please delete and recreate.", "流水线不存在于Jenkins中，请删除后重新创建"),
    PIPELINE_UPDATE_ERROR(406061, "Pipeline update failed.", "流水线信息更新失败"),
    SERVICE_ALREADY_IN_BLUE_GREEN_UPGRADE_INFORM(406062, "Service is already in blueGreen upgrade, please complete or rollback the upgrade first",
            "服务正处于蓝绿升级中，请先完成或回滚升级"),
    SERVICE_ALREADY_IN_CANARY_UPGRADE_INFORM(406063, "Service is already in blueGreen upgrade, please complete or rollback the upgrade first",
            "服务正处于灰度升级中，请先完成或回滚升级"),
    SERVICE_NOT_STARTED_INFORM(406064, "Service is not started, cannot do upgrade",
            "服务未启动，无法升级"),
    STAGE_CONFIG_ERROR(406065, "Stage configuration is wrong, please check", "流水线步骤配置有误，请检查步骤"),
    ORIGIN_STAGE_NOT_EXIST(406066, "Stage or pipeline is deleted in deploy stage, please configure", "部署步骤中镜像来源处的流水线或其步骤已被删除，请重新设置"),
    EXECUTE_TEST_SUITE_ERROR(406067, "Test suite execute failed.", "执行测试套件失败"),
    FILE_UPLOAD_POD_CREATE_ERROR(406068, "Create file upload pod failed", "创建上传文件pod失败"),
    FILE_UPLOAD_POD_REMOVE_ERROR(406069, "Remove file upload pod failed", "删除上传文件pod失败"),
    DEPENDENCE_LIST_ERROR(406070, "Dependence query failed", "依赖查询失败"),
    DEPENDENCE_NOT_AVAILABLE(406071, "Dependence is unavailable", "依赖不可用"),
    DEPENDENCE_DIRECTORY_QUERY_ERROR(406072, "Get dependence directory failed", "获取依赖目录失败"),
    NO_ENOUGH_RESOURCE(406073, "Not enough resource，please check the current quota and try again",
            "Not enough resource，please check the current quota and try again(资源配额不足，灰度升级过程中会额外启动一个新实例以确保升级可以顺利进行，请确保当前系统有足够资源)"),


    //配置文件 407xxx
    CONFIGMAP_NAME_DUPLICATE(407001, "ConfigMap name duplicate.", "配置文件名称已存在"),

    //其他9xxxxx
    FREE_TRIAL_END(900001, "Free trial end, please contact admin.","试用已结束，请联系管理员"),
    METHOD_FORMAT_ERROR(900002, "Method signature format error.","方法定义格式错误"),

    //istio 409XXX
    POLICY_EXIST(409001, "Policy name exist.", "策略已经存在"),
    POLICY_NOT_EXIST(409002, "Policy name not exist.", "策略不存在"),
    POLICY_LIST_FAILED(409003, "Micro service policy list failed.", "服务治理策略获取失败"),
    POLICY_CREATE_FAILED(409004, "Policy create failed.", "服务治理策略创建失败"),
    POLICY_DELETE_FAILED(409005, "Policy delete failed.", "服务治理策略删除失败"),
    POLICY_UPDATE_FAILED(409006, "Policy update failed.", "服务治理策略更新失败"),
    ISTIO_NAMESPACE_GET_FAILED(409007, "Querying istio namespace failed.", "查询Istio分区失败"),
    ISTIO_STATUS_UPDATE_FAILED(409008, "Update istio status failed.", "更新Istio状态失败"),
    INJECTION_IS_OPENED(409009, "Partition open istio automatic injection under Cluser", "集群下已存在分区开启istio自动注入"),
    NAMESPACE_GET_FAILED(409010, "Querying  namespace failed.", "查询Istio分区失败"),
    FRONT_DATA_ERROR(409011, "FRONT DATA ERROR.", "前端传入数据异常"),
    DB_DATA_ERROR(409012, "DB DATA ERROR.", "数据库中数据异常"),
    DATA_STATUS_ERROR(409013, "DATA IS ERROR.", "该条策略状态异常，不允许更新操作"),
    POLICY_CLOSE_FAILED(409014, "Policy close failed.", "服务治理策略关闭失败"),
    POLICY_OPEN_FAILED(409015, "Policy open failed.", "服务治理策略开启失败"),
    CREATE_POLICY_DATA_IS_EMPTY(409016, "Policy create failed because data is empty.", "创建策略信息为空"),
    UPDATE_POLICY_DATA_IS_EMPTY(409017, "Policy update failed because data is empty.", "更新策略信息为空"),
    DELETE_POLICY_RULETYPE_IS_WRONG(409018, "Policy delete failed because ruleType is wrong.", "无法删除该类型策略"),
    CLOSE_POLICY_RULETYPE_IS_WRONG(409019, "Close policy failed because ruleType is wrong.", "无法关闭该类型策略"),
    OPEN_POLICY_RULETYPE_IS_WRONG(409020, "Open policy failed because ruleType is wrong.", "无法开启该类型策略"),
    GET_POLICY_RULETYPE_IS_WRONG(409021, "Get policy detail failed because ruleType is wrong.", "无法获取该类型策略详情"),
    //istio 成功 4091xx
    POLICY_CREATE_SUCCESS(409101, "Policy create success.", "服务治理策略创建成功"),
    POLICY_DELETE_SUCCESS(409102, "Policy delete success.", "服务治理策略删除成功"),
    POLICY_UPDATE_SUCCESS(409103, "Policy update success.", "服务治理策略更新成功"),
    POLICY_CLOSE_SUCCESS(409104, "Policy close success.", "服务治理策略关闭成功"),
    POLICY_OPEN_SUCCESS(409105, "Policy open success.", "服务治理策略开启成功"),

    TENANTNOTINTHRCLUSTER(951001, "The tenant is not in the current platform","当前主机的租户不在本集群的库中，请切换其他集群的云管平台查看"),
    LOG_NULL(951002, "No log information","没有日志信息"),
    NOT_REPEATE(951003, "Repetitive custom metrics","重复的自定义指标"),
    INDICATOR(951004, "Set at least one indicator","请至少设置一项伸缩指标"),
    PARAMETERS(951005, "Request parameters:","请求参数:");

    private final int value;
    private final String reasonEnPhrase;
    private final String reasonChPhrase;

    ErrorCodeMessage(int value, String reasonEnPhrase, String reasonChPhrase) {
        this.value = value;
        this.reasonEnPhrase = reasonEnPhrase;
        this.reasonChPhrase = reasonChPhrase;
    }

    public int value() {
        return this.value;
    }

    public String phrase() {
        String language = DictEnum.getCurrentLanguage();
        if(language.equalsIgnoreCase(LANGUAGE_CHINESE)){
            return reasonChPhrase;
        }
        if(language.equalsIgnoreCase(LANGUAGE_ENGLISH)){
            return reasonEnPhrase;
        }
        return null;
    }

    public String getReasonEnPhrase() {
        return this.reasonEnPhrase;
    }

    public String getReasonChPhrase() {
        return reasonChPhrase;
    }

    public String toString() {
        return Integer.toString(this.value);
    }

    public static ErrorCodeMessage valueOf(int statusCode) {
        ErrorCodeMessage[] var1 = values();
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            ErrorCodeMessage status = var1[var3];
            if (status.value == statusCode) {
                return status;
            }
        }

        throw new IllegalArgumentException("No matching constant for [" + statusCode + "]");
    }

    public static String getMessageWithLanguage(ErrorCodeMessage error, String extendMessage, boolean prefix){
        String split = " ";
        if(StringUtils.isBlank(extendMessage)){
            split = "";
        }
        String message;
        String language = DEFAULT_LANGUAGE_CHINESE;
        if(RequestContextHolder.getRequestAttributes() != null
                && ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest() != null){
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String sessionLanguage = String.valueOf( request.getSession().getAttribute("language"));
            if(StringUtils.isNotBlank(sessionLanguage) && !"null".equals(sessionLanguage)){
                language = sessionLanguage;
            }
        }
        switch (language){
            case LANGUAGE_CHINESE:
                if(prefix){
                    message = extendMessage +  error.getReasonChPhrase();
                }else{
                    message = error.getReasonChPhrase() +  extendMessage;
                }
                return message;
            case LANGUAGE_ENGLISH:
                if(prefix){
                    message = extendMessage + split +  error.getReasonEnPhrase();
                }else{
                    message = error.getReasonEnPhrase() + split +  extendMessage;
                }
                return message;
            default:
                return error.getReasonEnPhrase();
        }
    }


}