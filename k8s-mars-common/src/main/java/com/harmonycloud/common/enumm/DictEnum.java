

package com.harmonycloud.common.enumm;


import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

import static com.harmonycloud.common.Constant.CommonConstant.DEFAULT_LANGUAGE_CHINESE;
import static com.harmonycloud.common.Constant.CommonConstant.LANGUAGE_CHINESE;
import static com.harmonycloud.common.Constant.CommonConstant.LANGUAGE_ENGLISH;

public enum DictEnum {

    NAME("name", "名称"),
    PARAM("param", "参数"),
    CREATE_TIME("create time", "创建时间"),
    USERNAME("username", "用户名"),
    HARBOR_USER("harbor user","harbor用户"),
    HARBOR_HOST("harbor server address","harbor服务器地址"),
    HARBOR_TARGET("target harbor server","目标harbor服务器"),
    REAL_NAME("real name", "真实姓名"),
    PHONE("phone no.", "手机号"),
    PASSWORD("password","密码"),
    PASSWORD_NEW("new password","新密码"),
    SERVER_HOST("server host", "服务器地址"),
    USER_ID("user id", "用户id"),
    ROLE("role", "角色"),
    NAMESPACE("namespace", "分区"),
    CLUSTER("cluster", "集群"),
    CLUSTER_ID("clusterId", "集群id"),
    POD("pod", "pod"),
    NODE("node", "节点"),
    TENANT("tenant", "租户"),
    TENANT_NAME("tenant name", "租户名称"),
    TENANT_ID("tenantId", "租户编号"),
    TENANT_MANAGER("tenant manager", "租户管理员"),
    PROJECT("project", "项目"),
    PROJECT_ID("project id", "项目编号"),
    PROJECT_NAME("project name", "项目名称"),
    APPLICATION("application", "应用"),
    APPLICATION_ID("applicationId", "应用编号"),
    APPLICATION_TEMPLATE("application template", "应用模板"),
    SERVICE("service","服务"),
    JOB("job", "任务"),
    LABEL("label", "标签"),
    IMAGE("image", "镜像"),
    USER_GROUP("user group", "用户群组"),
    REPOSITORY("repository", "镜像仓库"),
    REPOSITORY_ID("harborRepositoryId", "镜像仓库id"),
    REPOSITORY_QUOTA("repository quota", "镜像仓库配额"),
    REPLICATION_POLICY("replication policy", "镜像同步规则"),
    REPLICATION_POLICY_ID("replication policy id", "镜像同步规则id"),
    REPLICATION_TARGET_ID("replication target id", "镜像同步对方服务器"),
    IMAGE_NAME("image name", "镜像名称"),
    IMAGE_TAG("image tag", "镜像版本"),
    PAGE_SIZE("page size", "分页页码"),
    QUERY("query condition", "查询条件"),
    FLAG_TYPE("flag type", "标识类型"),
    NETWORK("network","网络"),
    NETWORK_ID("network id","网络id"),
    NETWORK_NAME("network name","网络名称"),
    SUB_NETWORK_ID("sub network id","子网id"),
    SUB_NETWORK_NAME("sub network name","子网名称"),
    FILE("file","文件"),
    LINE("line","行号"),
    EMAIL("email","电子邮箱"),
    SERVICE_OUT("out access service", "对外服务"),
    DEPLOYMENT_NAME("deployment name", "服务名称"),
    AUTO_SCALE("auto scale", "自动伸缩"),
    LOCAL_ROLE_ID("local role id", "局部角色编号"),
    LOG_INDEX("log index", "日志索引"),
    CONFIG_MAP("configmap","配置文件"),
    STORAGE_CLASS("storage class", "存储"),
    CONTAINER("container","容器"),
    LOG_DIR("logDir","日志目录"),
    LOG_FILE("logFile","目录文件");


    private final String enPhrase;
    private final String chPhrase;

    DictEnum(String enPhrase, String chPhrase) {
        this.enPhrase = enPhrase;
        this.chPhrase = chPhrase;
    }

    public String phrase() {
        String language = getCurrentLanguage();
        if(LANGUAGE_CHINESE.equalsIgnoreCase(language)){
            return chPhrase;
        }
        if(LANGUAGE_ENGLISH.equalsIgnoreCase(language)){
            return enPhrase;
        }
        return enPhrase;
    }

    public String getEnPhrase() {
        return enPhrase;
    }

    public String getChPhrase() {
        return chPhrase;
    }

    public static String phrase(DictEnum dictEnum){
        String language = getCurrentLanguage();
        if(LANGUAGE_CHINESE.equalsIgnoreCase(language)){
            return dictEnum.getChPhrase();
        }
        if(LANGUAGE_ENGLISH.equalsIgnoreCase(language)){
            return dictEnum.getEnPhrase();
        }
        return dictEnum.getEnPhrase();
    }

    public static String getCurrentLanguage(){
        String language = DEFAULT_LANGUAGE_CHINESE;
        if(RequestContextHolder.getRequestAttributes() != null
                && ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest() != null){
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String sessionLanguage = String.valueOf( request.getSession().getAttribute("language"));
            if(StringUtils.isNotBlank(sessionLanguage) && !"null".equals(sessionLanguage)){
                language = sessionLanguage;
            }
        }
        return language;
    }
}
