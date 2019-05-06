package com.harmonycloud.common.Constant;

import org.omg.CORBA.PUBLIC_MEMBER;

/**
 * Created by andy on 17-1-18.
 */
public class CommonConstant {

    // 逗号
    public static final String COMMA = ",";
    // 点号
    public static final String DOT = ".";
    // 斜杠
    public static final String SLASH = "/";
    // 横杠
    public static final String LINE = "-";
    // 等号
    public static final String EQUALITY_SIGN = "=";
    // 下划线
    public static final String UNDER_LINE = "_";
    public final static String COLON = ":";
    //分号
    public final static String SEMICOLON = ";";
    public final static String AT = "@";
    // 空字符串
    public static final String EMPTYSTRING = "";
    // 空格
    public static final String BLANKSTRING = " ";

    // 左括号
    public static final String LEFT_BRACKET = "(";

    // 右括号
    public static final String RIGHT_BRACKET = ")";

    // 单引号
    public static final String SINGLE_QUOTATION = "'";

    // 逗号（字符）
    public static final char COMMA_CHAR = ',';

    //百分号
    public static final String PERCENT = "%";

    public static final String RPID = "rpid";

    public static final String DEFAULT = "default";

    public static final String ROLE_TM = "tm";

    public static final String ROLE_DEV = "dev";
    // harbor project
    public static final String HARBOR_PROJECT = "/api/projects";

    public static final String NEPHELE_TENANT = "nephele_tenant_";

    public static final String ZERO = "ZERO";

    public static final String ONE = "ONE";

    public static final String TWO = "TWO";

    public static final String THREE = "THREE";

    public static final String FOUR = "FOUR";

    // 网络类型
    public static final String NETWORK_C = "c";

    public static final String NETWORK_N = "n";

    public static final String MONIT_NETWORK = "network";

    public static final String MONIT_TYPE = "pod_container";

    public static final String MONIT_NETWORK_TYPE = "pod";

    public static final String MONIT_VOLUME = "pvc";

    public static final String NOTI = "noti";
    public static final String ID = "id";
    public static final String DEFAULT_CHARSET_UTF_8 = "utf-8";
    public static final String LANGUAGE_ENGLISH = "en";
    public static final String LANGUAGE_CHINESE = "ch";
    //role类型
    public static final String DEV = "dev";
    public static final String NULL = null;
    //等待的时间
    public static final String GRACEPERIODSECONDS = "gracePeriodSeconds";
    public static final String EMPTYMETADATA = "{}";
    //返回状态
    public static final String SUCCESS = "success";
    public static final String FAIL = "fail";
    public static final String READY = "ready";
    public static final String INPROCESS = "inprocess";
    public static final String CLOSED = "closed";
    public static final String DATA = "data";
    public static final String ADMIN = "admin";
    public static final String MENU = "menu";
    //备注
    public static final String NEPHELE_ANNOTATION = "nephele/annotation";
    public static final String UNDEFINED = "undefined";
    //时间格式
    public static final String UTCTIME = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    public static final String CREATIONTIMESTAMP = "creationTimestamp";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String HARDTYPE = "hardType";
    public static final String CPUTYPE = "cpuType";
    public static final String MEMORYTYPE = "memoryType";
    public static final String STORAGE_USED = "storageUsed";
    public static final String USEDTYPE = "usedType";
    public static final String TYPE = "type";
    public static final String APPLICATION_JSON = "application/json";
    //时间单位
    public static final String SECOND = "s";
    public static final String MINUTE = "m";
    public static final String HOUR = "h";
    //k8s的基础属性
    public static final String KUBE_SYSTEM = "kube-system";
    public static final String METADATA = "metadata";
    public static final String KIND = "kind";
    public static final String NAMESPACE = "Namespace";
    public static final String INITKUBESYSTEM = "initkubesystem";
    public static final String SUBNETS = "subnets";
    public static final String SUBNETID = "subnetid";
    public static final String ADDRESS = "address";
    public static final String PROVIDERSPEC = "providerSpec";
    public static final String IP = "ip";
    public static final String PATH = "path";
    public static final String NAME = "name";
    public static final String ALIASNAME = "aliasName";
    public static final String URL = "url";
    public static final String ICONNAME = "iconName";
    public static final String SUBMENU = "subMenu";
    public static final String CPU = "cpu";
    public static final String CORE = "Core";
    public static final String MEMORY = "memory";
    public static final String MI = "Mi";
    public static final String KI = "Ki";
    public static final String GI = "Gi";
    public static final String TI = "Ti";
    public static final String PI = "Pi";
    public static final String MB = "MB";
    public static final String KB = "KB";
    public static final String SMALLM = "m";
    public static final String SMALLK = "k";
    public static final String SMALLG = "g";
    public static final String SMALLT = "t";
    public static final String GB = "GB";
    public static final String TB = "TB";
    public static final String PB = "PB";
    public static final String GPU = "gpu";
    public static final String NVIDIA_GPU = "nvidia.com/gpu";
    public static final String GPU_QUOTA_KEY = "requests.nvidia.com/gpu";
    public static final String RECYCLE = "Recycle";
    public static final String RESULTNAMESPACE = "namespace";
    public static final String BINDING = "binding";
    public static final String TIME = "time";
    public static final String NETWORKID = "networkid";
    public static final String ANNOTATION = "annotation";
    public static final String TENANTID = "tenantid";
    public static final String TENANT_ID = "tenantId";
    public static final String TENANTNAME = "tenantname";
    public static final String TENANT_ALIASNAME = "tenantAliasName";
    public static final String NS_ALIASNAME = "aliasName";
    public static final String PROJECT_ALIASNAME = "projectAliasName";
    public static final String NAMESPACEDATA = "namespaceData";
    public static final String HARBORDATA = "harborData";
    public static final String USERDATA = "userData";
    public static final String TENANT = "tenant";
    public static final String TENANTMGR = "tenantmgr";
    public static final String BASIC = "basic";
    public static final String UNCHECKED = "unchecked";
    public static final String USER = "user";
    public static final String USERNAME = "username";
    public static final String PROJECT = "project";
    public static final String PROJECTID = "projectId";
    public static final String VERBS = "verbs";
    public static final String CLUSTORROLE = "kubeadm.alpha.kubernetes.io/role";
    public static final String CLUSTERLIST = "clusterList";
    public static final String TENANTSIZE = "tenantSize";
    public static final String CLUSTERID = "clusterId";
    public static final String CLUSTERALIASID = "clusterAliasName";
    public static final String CLUSTER_NAME = "clusterName";
    public static final String DATACENTER_NAME = "dataCenterName";
    public static final String LABELSELECTOR = "labelSelector";
    public static final String PODSELECTOR = "podSelector";
    public static final String NAMESPACES = "namespaces";
    public static final String NAMESPACESELECTOR = "namespaceSelector";
    public static final String HARBORPROJECTS = "harborProjects";
    public static final String K8SPVS = "k8sPvs";
    public static final String NAMESPACENUM = "namespaceNum";
    public static final String HARBORPUBLICPERJECTNUM = "harborPublicPerjectNum";
    public static final String TENANTUSERNUM = "tenantUserNum";
    public static final String K8SNAMESPACES = "k8sNamespaces";
    public static final String CREATETIME = "createTime";
    public static final String ITEMS = "items";

    public static final String HARBOR = "harbor";
    public static final String COOKIE = "Cookie";
    public static final String PROJECT_ID = "project_id";
    public static final String TM = "tm";
    public static final String PM = "pm";
    public static final String TMUSER = "tmUser";
    public static final String RESOURCEQUOTA = "ResourceQuota";
    public static final String SPEC = "spec";
    public static final String STATUS = "status";
    public static final String NEPHELE_NETWORKID = "nephele/networkid";
    public static final String NEPHELE_NETWORKNAME = "nephele/networkname";
    public static final String NEPHELE_SUBNETNAME = "nephele/subnetname";
    public static final String NEPHELE_SUBNETID = "nephele/subnetid";
    // k8s基础参数长度限制(pv)
    public static final int K8S_NAME_LENGTH_LIMIT = 63;
    //网络隔离方案
    public static final String NETWORK_POLICY = "net.beta.kubernetes.io/network-policy";
    public static final String NETWORK_POLICY_INGRESS = "{\"ingress\": {\"isolation\": \"DefaultDeny\"}}";
    public static final String INGRESS = "ingress";
    public static final String POLICY = "policy";
    public static final String NETWORKPOLICY = "NetworkPolicy";
    public static final String HAPOLICY = "hapolicy";
    public static final String MATCHLABELS = "matchLabels";
    public static final int KEEP_DECIMAL_3 = 3;
    public static final int PERCENT_HUNDRED = 100;
    public static final String PVC = "persistentvolumeclaims";
    public static final String RC = "replicationcontrollers";
    public static final String PERSISTENTVOLUME = "PersistentVolume";
    public static final String STORAGE = "storage";
    public static final String NFS = "nfs";
    public static final String NFS_SERVER = "NFS_SERVER";
    public static final String NFS_PATH = "NFS_PATH";
    public static final String AUTO_PROVISION_LABEL = "autoProvision";
    public static final String STORAGECLASS = "storageClass";

    public static final String READONLYMANY = "ReadOnlyMany";
    public static final String READWRITEMANY = "ReadWriteMany";
    public static final String READWRITEONCE = "ReadWriteOnce";
    public static final String CLUSTERROLE = "ClusterRole";
    public static final String ROLEBINDING = "RoleBinding";
    public static final String APIVERSION = "apiVersion";
    public static final String SUBJECTS = "subjects";
    public static final String ROLEREF = "roleRef";
    public static final String MASTERNODE = "masterNode";
    public static final String DATANODE = "dataNode";
    public static final String HARMONYCLOUD_TENANTNAME_NS = "HarmonyCloud_TenantName";
    public static final String HARMONYCLOUD_TENANTNAME = "HarmonyCloud_Tenant";
    public static final String HARMONYCLOUD_TENANT_ID = "HarmonyCloud_TenantId";

    //node节点状态
    public static final String HARMONYCLOUD_STATUS = "HarmonyCloud_Status";
    //node节点临时状态
    public static final String HARMONYCLOUD_STATUS_LBS = "lb";
    //node节点子状态
    public static final String HARMONYCLOUD_SUBSTATUS = "HarmonyCloud_SubStatus";

    //node Condition
    public static final String NODE_CONDITION_READY = "Ready";
    public static final String NODE_CONDITION_OUT_OF_DISK = "OutOfDisk";
    public static final String NODE_CONDITION_DISK_PRESSURE = "DiskPressure";
    public static final String NODE_CONDITION_MEMORY_PRESSURE = "MemoryPressure";
    public static final String NODE_CONDITION_PID_PRESSURE = "PIDPressure";
    public static final String NODE_CONDITION_NETWORK_UNAVAILABLE = "NetworkUnavailable";
    public static final String NODE_CONDITION_STATUS_TRUE = "True";
    public static final String NODE_CONDITION_STATUS_FALSE = "False";
    public static final String NODE_CONDITION_STATUS_UNKNOW = "Unknow";
    //begin节点上线开始状态，done节点上线结束状态
    public static final String BEGIN = "begin";
    public static final String DONE = "done";

    public static final String FROM = "from";
    public static final String PAUSE = "pause";
    public static final String NORMAL = "normal";
    //label key
    public static final String LABEL_KEY_APP = "app";
    public static final String LABEL_KEY_DAEMONSET = "daemonset";
    public static final String LABEL_KEY_STATEFULSET = "statefulset";
    public static final String SERVICE_TYPE = "serviceType";
    public static final String LABEL_PODNAME = "podName";
    public static final String LABEL_STATEFULSET_POD = "statefulset.kubernetes.io/pod-name";


    //用户权限模块
    public static final String PRIVILEGE_TENANT = "tenant";
    public static final String PRIVILEGE_MIRROR = "mirror";
    public static final String PRIVILEGE_CLUSTER = "cluster";
    public static final String PRIVILEGE_APPLICATION = "application";
    public static final String PRIVILEGE_CICD = "cicd";
    public static final String PRIVILEGE_STORAGE = "storage";
    public static final String PRIVILEGE_CONFIG = "config";
    public static final String PRIVILEGE = "privlege";
    public static final String SESSION_DATA_PRIVILEGE_LIST = "dataPrivilegeList";


    public static final String ERRMSG = "errMsg";
    public static final String NEPHELE_TENANT_NETWORK = "nephele_tenant_network";
    public static final String USERID = "userId";
    //   public static final String NETWORKNAMETO = "networknameto";
    public static final Boolean FALSE = false;
    public static final Boolean TRUE = true;
    public static final String TRUE_STRING = "true";
    public static final String FALSE_STRING = "false";

    public static final String ZERONUM = "0";
    public static final String ONENUMSTRING = "1";
    //创建镜像仓库默认镜像数量配额
    public static final Integer QUOTA_NUM = 10000;
    //创建镜像仓库默认磁盘配额
    public static final Float QUOTA_SIZE = 512000.0f;
    public static final int NUM_ZERO = 0;
    public static final int NUM_ONE = 1;
    public static final int NUM_TWO = 2;
    public static final int NUM_THREE = 3;
    public static final int NUM_FOUR = 4;
    public static final int NUM_FIVE = 5;
    public static final int NUM_SIX = 6;
    public static final int NUM_SEVEN = 7;
    public static final int NUM_EIGHT = 8;
    public static final int NUM_NINE = 9;
    public static final int NUM_TEN = 10;
    public static final int NUM_ELEVEN = 11;
    public static final int NUM_TWELVE = 12;
    public static final int NUM_THRITY_ONE = 31;
    //1000进制
    public static final int NUM_THOUSAND = 1000;
    public static final int NUM_MINUS_ONE = -1;
    public static final int NUM_GB = 1;

    public static final int NUM_ROLE_ADMIN = 1;
    public static final int NUM_ROLE_TM = 2;
    public static final int NUM_ROLE_PM = 3;
    //存储的单位进制
    public static final int NUM_SIZE_MEMORY = 1024;
    public static final double NUM_ONE_DOUBLE = 1.0;

    public static final String LDAP_IP = "ldap_ip";
    public static final String LDAP_PORT = "ldap_port";
    public static final String LDAP_BASE = "ldap_base";
    public static final String LDAP_USERDN = "ldap_userdn";
    public static final String LDAP_PASSWORD = "ldap_password";
    public static final String LDAP_IS_ON = "ldap_is_on";
    public static final String LDAP_OBJECT_CLASS = "ldap_object_class";
    public static final String LDAP_SEARCH_ATTR = "ldap_search_attribute";
    public static final String CONFIG_TYPE_FULLLINK = "fulllink";
    public static final String CONFIG_TYPE_LDAP = "ldap";

    public static final String MAINTENANCE_STATUS = "maintenanceStatus";
    public static final String CONFIG_TYPE_MAINTENANCE = "maintenance";

    public static final String CONFIG_TYPE_CICD = "cicd";
    public static final String CICD_RESULT_REMAIN_NUM = "cicd_remain_num";
    public static final String CICD_IS_TYPE_MERGE = "cicd_type_merge";

    public static final String TRIAL_TIME = "trial_time";

    public static final String FILE_MAX_SIZE = "file_maxsize";
    public static final String CONFIG_TYPE_FILE = "file";

    public static final Integer ES_SHARDS = 5;
    public static final String COUNT = "count";
    public static final String PROTOCOL_HTTPS = "https";
    public static final String PROTOCOL_HTTP = "http";
    public static final Integer DEFAULT_KUBE_APISERVER_PORT = 6443;
    public static final Integer DEFAULT_HARBOR_PORT = 80;
    public static final Integer DEFAULT_HTTPS_PORT= 443;
    public static final String DEFAULT_NAMESPACE = "default";

    public static final Integer HTTP_STATUS_400 = 400;
    public static final Integer HTTP_STATUS_200 = 200;
    public static final Integer HTTP_STATUS_100 = 100;
    //cicd
    public static final String CICD_NAMESPACE = "cicd";
    public static final String DEPENDENCE_PREFIX = "cicd-dependence";
    //cicd 长连接30秒内至少返回一次数据
    public static final long CICD_WEBSOCKET_MAX_DURATION = 30000L;
    public static final long CICD_SLEEP_TIME_30000 = 30000L;
    public static final long CICD_SLEEP_TIME_300000 = 300000L;
    //流水线触发类型
    public static final int PERIODICAL = 1;
    public static final int POLLSCM = 2;
    public static final int WEBHOOK = 3;
    public static final int JOBTRIGGER = 4;
    public static final int IMAGETRIGGER = 5;
    //流水线参数类型
    public static final int STRING_TYPE_PARAMETER = 1;
    public static final int CHOICE_TYPE_PARAMETER = 2;
    //流水线webhook接口
    public static final String WEBHOOK_API = "/rest/cicd/jobs/{uuid}/webhook";
    //流水线reference类型
    public static final String REFERENCES_BRANCH = "branch";
    public static final String REFERENCES_TAG = "tag";
    //文件上传POD的label
    public static final String FILE_UPLOAD_POD_LABEL = "app=file-upload";
    public static final String FILE_UPLOAD_POD_NAME_PREFIX = "file-upload";
    //文件类型
    public static final String DIRECTORY_TYPE = "d";
    //镜像tag类型
    public static final String IMAGE_TAG_TIMESTAMP = "0";
    public static final String IMAGE_TAG_RULE = "1";
    public static final String IMAGE_TAG_CUSTOM = "2";
    //镜像来源
    public static final String IMAGE_FROM_CI = "0";
    public static final String IMAGE_FROM_HARBOR = "1";
    //升级方式
    public static final String FRESH_RELEASE = "0";
    public static final String CANARY_RELEASE = "1";
    public static final String BLUE_GREEN_RELEASE = "2";

    public static final Integer RANDOM_BIT_8 = 8;
    public static final Integer SPLIT_LIMIT = 9;
    public static final Integer FILE_NAME_POSITION = 8;

    public static final String PV_RETAIN = "Retain";

    public static final String PV_RECYCLE = "Recycle";

    public static final String PV_DELETE = "Delete";

    public static final String PV_STATUS_RELEASED = "Released";

    public static final String PV_RECYCLE_POD_NAME = "pv-recycler-";

    public static final String PV_CREATE_POD_NAME = "pv-dir-create-";

    public static final String PV_DELETE_POD_NAME = "pv-dir-delete-";

    public static final String RESTARTPOLICY_NEVER = "Never";

    public static final String IMAGEPULLPOLICY_ALWAYS = "Always";

    public static final String DEFAULT_LANGUAGE_CHINESE = LANGUAGE_CHINESE;

    public static final String DEFAULT_LOG_MOUNT_PATH = "/var/log/containers/";
    //角色id
    public static final String ROLEID = "roleId";
    //用户角色名
    public static final String ROLENAME = "roleName";
    //用户角色
    public static final String ROLE = "role";
    //用户邮箱
    public static final String EMAIL = "email";
    //用户手机
    public static final String MOBILEPHONE = "mobilePhone";
    //用户姓名
    public static final String NICKNAME = "nickName";
    //数据中心
    public static final String DATA_CENTER = "datacenter";
    //devops平台推送用户初始密码
    public static final String INITPASSWORD = "Ab123456";
    //模块
    public static final String MODULE = "module";
    //资源
    public static final String RESOURCE = "resource";
    public static final Integer DEFAULT_CAPACITY = 500;
    public static final String POD_STATUS_RUNNING = "Running";
    public static final String POD_STATUS_PENDING = "Pending";
    public static final String EVENT_TYPE_NORMAL = "Normal";
    public static final String EVENT_TYPE_WARNING = "Warning";
    public static final String POD_CONDITION_PODSCHEDULED = "PodScheduled";

    public static final Integer IS_ADMIN = 1;
    public static final Integer IS_NOT_ADMIN = 0;
    //获取菜单
    public static final String GETMENU = "getMenu";
    //项目列表
    public static final String PROJECTLIST = "projectList";
    //角色列表
    public static final String ROLELIST = "roleList";
    public static final int ONE_WEEK_DAYS = 7;
    public static final int DEFAULT_PAGE_SIZE_20 = 20;
    public static final int DEFAULT_PAGE_SIZE_200 = 200;
    public static final int MAX_PAGE_SIZE_1000 = 1000;

    // 数据权限条件类型：json格式
    public static final short PRIVILEGE_CONDITION_TYPE_SQL = 1;
    // 数据权限条件类型：自定义格式
    public static final short PRIVILEGE_CONDITION_TYPE_CUSTOM = 2;
    public static final int FLAG_TRUE = 1;
    public static final int FLAG_FALSE = 0;

    //通用规则
    public static final String RULE_ALL = "-1";
    // ES恢复快照，重命名时旧值占位符
    public static final String ES_INDEX_RENAME_REPALCEMENT = "$0";
    public static final String ES_REPOSITORY_LOCATION = "location";
    public static final String ES_REPOSITORY_MAX_SNAPSHOT_SPEED = "max_snapshot_bytes_per_sec";
    public static final String ES_REPOSITORY_MAX_RESTORE_SPEED = "max_restore_bytes_per_sec";
    public static final String ES_REPOSITORY_TYPE = "fs";
    // 重命名时匹配所有索引，非空
    public static final String ES_RESTORE_RENAME_PATTERN = ".+";

    public static final String ES_INDEX_SNAPSHOT_RESTORE = "_snapshot";
    public static final String ES_INDEX_LOGSTASH_DATE_FORMAT = "yyyy.MM.dd";
    // 计算索引开始时间时补充一天
    public static final int ES_INDEX_START_DATE_ADD_DAY_ONE = 1;
    public static final String ES_SNAPSHOT_CREATE_AUTO_PREFIX = "log_auto_";
    public static final String ES_SNAPSHOT_CREATE_MANUAL_PREFIX = "log_manual_";

    public static final String URL_PRFFIX = "/rest";
    //创建
    public static final String CREATE = "create";
    //删除
    public static final String DELETE = "delete";
    //更新
    public static final String UPDATE = "update";
    //查询
    public static final String GET = "get";
    //执行
    public static final String EXECUTE = "execute";
    //集群
    public static final String CLUSTER = "cluster";
    //系统管理员角色id
    public static final Integer ADMIN_ROLEID = 1;
    //租户管理员角色id
    public static final Integer TM_ROLEID = 2;
    //项目管理员角色id
    public static final Integer PM_ROLEID = 3;

    public static final int ROUND_SCALE_2 = 2;

    public static final String DEAULT_MODULE_CH = "moduleChDesc";

    public static final String DEAULT_MODULE_EN = "moduleEnDesc";
    public static final String REPOSITORY_TYPE_PUBLIC = "public";
    public static final String REPOSITORY_TYPE_PRIVATE = "private";

    public static final long ORIGINAL_DATE_MILL_SECOND = 0L;
    public static final String HARBOR_PROJECT_NAME_MSF = "msf-component";
    public static final String HARBOR_PROJECT_NAME_PLATFORM = "k8s-deploy";
    public static final String HARBOR_PROJECT_ISTIO_DEPLOY = "istio-deploy";
    //uat角色id
    public static final int UAT_ROLEID = 7;
    //角色状态
    public static final String ROLESTATUS = "roleStatus";
    public static final String APPCENTER = "appcenter";
    public static final String CICD = "cicd";
    public static final String DELIVERY = "delivery";
    public static final String LOG = "log";
    public static final String ALARM = "alarm";
    public static final String DAEMONSET = "daemonset";
    //默认查询日志30分钟内
    public static final int DEFAULT_LOG_QUERY_TIME = 30;
    public static final String TIME_UNIT_MINUTES = "m";
    public static final String TENANTNOTINTHRCLUSTER = "当前节点的租户不在本集群的库中，请切换其他集群的云管平台查看";
    public static final String REDIS_KEY_IMAGE_PULL_STATUS = "image_pull_status";
    public static final String REDIS_KEY_IMAGE_DELETING = "image_deleting";
    public static final String IMAGE_PULLING_STATUS_PULLING = "pulling";
    public static final String IMAGE_PULLING_STATUS_PULLED = "pulled";
    public static final String IMAGE_FILE_DOWNLOAD_PATH = "image-download";
    public static final String IMAGE_FILE_UPLOAD_PATH = "image-upload";
    public static final int ONE_DAY_SECONDS = 86400;
    public static final int FIVE_MINUTES_SECONDS = 300;

    public static final int ES_MAX_RESULT_WINDOW = 300000;

    //数据权限策略
    public static final int DATA_CLOSED_STRATEGY = 1;
    public static final int DATA_SEMIOPEN_STRATEGY = 2;
    public static final int DATA_OPEN_STRATEGY = 3;

    //数据权限类型
    public static final int DATA_READONLY = 1;
    public static final int DATA_READWRITE = 2;

    //数据权限组类型
    public static final int DATA_GROUP = 1;
    public static final int DATA_GROUP_SYSTEM_ADMIN = 2;
    public static final int DATA_GROUP_TENANT_ADMIN = 3;
    public static final int DATA_GROUP_PROJECT_ADMIN = 4;

    public static final Integer MEMBER_TYPE_USER = 0;//用户
    public static final Integer MEMBER_TYPE_GROUP = 1;//组

    //数据权限
    public static final byte SCOPE_TENANT = 0;
    public static final byte SCOPE_PROJECT = 1;

    //数据权限过滤字段类型
    public static final int DATA_FIELD = 1;
    public static final int NAMESPACE_FIELD = 2;
    public static final int PROJECTID_FIELD = 3;
    public static final int CLUSTERID_FIELD = 4;

    //数据权限过滤字段
    public static final String DATA_NAMESPACE = "namespace";

    //Istio
    public static final String ISTIO_NAMESPACE = "istio-system";
    public static final String NETWORKING_ISTIO_V1ALPHA3 = "networking.istio.io/v1alpha3";
    public static final String DESTINATION_RULE = "DestinationRule";
    public static final String CONFIG_ISTIO_V1ALPHA2 = "config.istio.io/v1alpha2";
    public static final String CIRCUIT_BREAKER = "circuitBreaker";
    public static final String RATE_LIMIT = "rateLimit";
    public static final String RATE_LIMIT_PREFIX = "ratelimit-";
    public static final String WHITE_LISTS = "whiteLists";
    public static final String WHITE_LISTS_PREFIX = "whitelists-";
    public static final String TIMEOUT_RETRY = "timeoutRetry";
    public static final String FAULT_INJECTION = "faultInjection";
    public static final String TRAFFIC_SHIFTING = "trafficShifting";
    public static final String RATE_LIMIT_QUOTA = "quota";
    public static final String RATE_LIMIT_REDIS_QUOTA = "redisquota";
    public static final String RATE_LIMIT_QUOTA_SPEC = "QuotaSpec";
    public static final String RATE_LIMIT_QUOTA_SPEC_BINDING = "QuotaSpecBinding";
    public static final String WHITE_LISTS_LIST_CHECKER = "listchecker";
    public static final String WHITE_LISTS_LIST_ENTRY = "listentry";
    public static final String ISTIO_RULE = "rule";
    public static final String ISTIO_VIRTUALSERVICE = "VirtualService";
    public static final String ISTIO_RULE_TYPE = "istioRuleType";
    public static final String ISTIO_RULE_NAME = "istioRuleName";
    public static final String ISTIO_SERVER_STATUS = "istioStatus";
    public static final int OPEN_GLOBAL_STATUS = 1;
    public static final int CLOSE_GLOBAL_STATUS = 0;
    public static final String OPEN_ISTIO_AUTOMATIC_INJECTION = "enabled";
    public static final String CLOSE_ISTIO_AUTOMATIC_INJECTION = "disabled";
    public static final String ISTIO_INJECTION  =  "istio-injection";
    public static final int ISTIO_POLICY_OPEN = 1;
    public static final int ISTIO_POLICY_CLOSE = 0;
    public static final int DATA_IS_OK = 0;//策略数据状态正常
    public static final int DATA_IS_ERROR = 1;//策略数据异常（灰度后VirtualService版本与DestinationRule版本不一致）
    public static final int K8S_NO_DATA = 11;
    public static final int DATA_NOT_SAME = 12;
    public static final int DATA_NOT_COMPLETE = 13;
    public static final int SWITCH_STATUS_ERROR = 14;

    public static final String QUOTASPEC_CHARGE_ONE = "1";

    public static final String SERVICE_ENTRY = "ServiceEntry";
    public static final String MESH_EXTERNAL = "MESH_EXTERNAL";
    public static final String MESH_INTERNAL = "MESH_INTERNAL";

    //网络类型
    public static final String K8S_NETWORK_HCIPAM = "hcipam";
    public static final String K8S_NETWORK_CALICO = "calico";

    //对应order字段为该策略创建时对应的crd创建的顺序，为了后期异常修复功能备用
    public static final int RATE_LIMIT_RESOURCE_COUNT = 5;
    public static final int RATE_LIMIT_QUOTA_ORDER = 1;
    public static final int RATE_LIMIT_REDISQUOTA_ORDER = 2;
    public static final int RATE_LIMIT_QUOTASPEC_ORDER = 3;
    public static final int RATE_LIMIT_QUOTASPECBINDING_ORDER = 4;
    public static final int RATE_LIMIT_RULE_ORDER = 5;
    public static final String RATE_LIMIT_ALGORITHM_ROLLING_WINDOW = "ROLLING_WINDOW";
    public static final String RATE_LIMIT_ALGORITHM_FIXED_WINDOW = "FIXED_WINDOW";

    public static final int WHITE_LISTS_RESOURCE_COUNT = 3;
    public static final int WHITE_LISTS_LISTCHECKER_ORDER = 1;
    public static final int WHITE_LISTS_LISTENTRY_ORDER = 2;
    public static final int WHITE_LISTS_RULE_ORDER = 3;

    public static final int TIMEOUT_RETRY_RESOURCE_COUNT = 2;
    public static final int TIMEOUT_RETRY_TIMEOUT_ORDER = 1;
    public static final int TIMEOUT_RETRY_RETRY_ORDER = 2;

    public static final int FAULT_INJECTION_RESOURCE_COUNT = 1;
    public static final int TRAFFIC_SHIFTING_RESOURCE_COUNT = 1;
    public static final int CIRCUIT_BREAKER_RESOURCE_COUNT = 1;

    //serviceEntry的类型定义
    public static final int  EXTERNAL_SERVICE_ENTRY = 0;
    public static final int  INTERNAL_SERVICE_ENTRY = 1;
    public static final String  RESOLUTION_STATIC = "STATIC";
    public static final String  RESOLUTION_DNS = "DNS";
    public static final String  PROTOCOL_TCP = "TCP";
    public static final String  RESOLUTION_NONE =  "NONE";

    //容器环境变量类型定义
    public static final String ENV_TYPE_EQUAL = "equal";
    public static final String ENV_TYPE_FROM = "from";

    public static final String FIELD_SELECTOR = "fieldSelector";
    public static final String SPEC_NODENAME = "spec.nodeName";
    public static final String ENV_ERROR_MESSAGE = "field label not supported";

    //数据库策略对应的字段
    public static final String  RULE_CLUSTER_ID  =  "ruleClusterId";
    public static final String  RULE_NS = "ruleNs";
    public static final String  RULE_SVC = "ruleSvc";
    public static final String  RULE_TYPE = "ruleType";
    public static final String  PROJECT_ID_SERVICEENTRY = "-serviceentry";

    public static final String TENANT_REDIS_KEY_PREFIX = "tenant_id-";

    public static final int STORAGE_HARD_INDEX = 0;
    public static final int STORAGE_USED_INDEX = 1;
}