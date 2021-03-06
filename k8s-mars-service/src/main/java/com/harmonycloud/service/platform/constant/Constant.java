package com.harmonycloud.service.platform.constant;

public class Constant {

	public final static String ES_INDEX_AUDIT_LOG = "audit";
	public final static String ES_INDEX_TYPE_AUDIT_LOG = "user_op_audit";
	//dep状态：status code :0 ：stop 1:start 2:stopping 3:starting
	public final static String STOP = "0";
	
	public final static String START = "1";
	
	public final static String STOPPING = "2";
	
	public final static String STARTING = "3";

	public final static String SERVICE_STOP = "stopped";

	public final static String SERVICE_START = "started";

	public final static String SERVICE_STOPPING = "stopping";

	public final static String SERVICE_STARTING = "starting";
	
	public final static String JOB_SUCCEED = "succeed";
	
	public final static String JOB_FAILED = "failed";
	
	public final static String WAITING = "waiting";
	
	public final static String RUNNING = "running";
	
	public final static String TERMINATED = "terminated";
	
	public final static String SECONDS = "seconds";
	
	public final static String MINUTES ="minutes";
	
	public final static String HOURS = "Hours";
	
	public final static String DAYS = "days";
	
	public final static String NETWORK_NUTON = "n";
	
	public final static String NETWORK_CALICO = "calico";
	
	public final static String HARBOR = "harbor";
	
	public final static String INFLUXDB= "influxDB";
	
	public final static String ES="Elastic Search";
	
	public final static String HAPROXY = "HAproxy";
	
	public final static String NETWORK = "network";
	
	public final static String NOTCREATED = "not created";
	
	public final static String TYPE_SECRET = "secret";
	
	public final static String TYPE_PV = "pv";
	
	public final static String TYPE_GIT = "gitRepo";
	
	public final static String HARBORPROJECTROLE_DEV = "harbor_project_developer";
	
	public final static String HARBORPROJECTROLE_WATCHER = "harbor_project_watcher";

	public final static String HARBORPROJECTROLE_ADMIN = "harbor_project_admin";

	public final static Integer HARBOR_ROLE_PROJECT_ADMIN = 1;
	public final static Integer HARBOR_ROLE_DEVELOPER = 2;
	
	public final static String FLAGNAME = "user";
	
	public final static Integer HTTP_404 = 404;

	public final static String DEPLOYMENT = "Deployment";

    public  final static String STATEFULSET = "StatefulSet";

    public final static String DAEMONSET = "DaemonSet";

    public final static String DEPLOYMENT_API_VERSION = "apps/v1";

    public final static String STATEFULSET_API_VERSION = "apps/v1";

	public final static Integer ADMIN_ACCOUNT = 1;
	public final static Integer NON_ADMIN_ACCOUNT = 0;
	public final static Integer NON_MACHINE_ACCOUNT = 0;
	public final static Integer USER_AUTHORIZED = 1;
	public final static Integer USER_NOT_AUTHORIZE = 0;
	public final static Integer DB_BATCH_INSERT_COUNT = 1000;

	//application
	public final static double TEMPLATE_TAG = 1.0;

	public final static double TEMPLATE_TAG_INCREMENT = 0.1;

	public final static Integer TEMPLATE_STATUS_DELETE = 1;

	public final static Integer TEMPLATE_STATUS_CREATE = 0;

	public final static Integer EXTERNAL_SERVICE = 1;

	public final static Integer K8S_SERVICE = 0;


	public final static String STATUS_NORMAL = "0";
	public final static String STATUS_ABNORMAL = "1";

	//PodDisruptionBudget
	public final static String PDB_TYPE_MIN_AVAILABLE = "minAvailable";
	public final static String PDB_TYPE_MAX_UNAVAILABLE = "maxUnavailable";
	public final static String SYSTEM_CONFIG_PDB_MIN_AVAILABLE = "pdb.minAvailable";
	public final static String SYSTEM_CONFIG_PDB_MAX_UNAVAILABLE = "pdb.maxUnavailable";
	public final static String PDB_SUFFIX = "-pdb";


	//onlineshop
	public final static String ONLINESHOP = "onlineshop";
	public final static String CLUSTER_ROLE_ONLINESHOP = "onlineshop";
	public final static String CLUSTER_ROLE_BINDING_ONLINESHOP = "onlineshop";
	public final static String SERVICE_ACCOUNT_ONLINESHOP = "onlineshop";
	public final static String INIT_CONTAINER_IMAGE = "alpine";
	public final static String INIT_CONTAINER_IMAGE_TAG = "3.6";

	//verbs
	public final static String GET = "get";
	public final static String LIST = "list";
	public final static String WATCH = "watch";


	//stroge
	public final static String VOLUME_TYPE_NFS = "nfs";
    public final static String VOLUME_TYPE_PVC = "pvc";
    public final static String VOLUME_TYPE_STORAGECLASS = "storageClass";
	public final static String VOLUME_TYPE_HOSTPASTH = "hostPath";
	public final static String VOLUME_TYPE_GITREPO = "gitRepo";
	public final static String VOLUME_TYPE_EMPTYDIR = "emptyDir";

	//labels资源类型
	public final static String TYPE_JOB = "jobs";
	public final static String TYPE_DEPLOYMENT = "app";
    public final static String TYPE_STATEFULSET = "harmonycloud.cn/statefulset";
    public final static String TYPE_PROJECT_ID = "harmonycloud.cn/projectId";
    public final static String TYPE_DATACENTER = "dataCenter";
	public final static String TOP_DATACENTER = "cluster-top";
    public final static String TYPE_CONTROLLERREVISIONNAME = "controller-revision-hash";
    public final static String TYPE_DEPLOY_VERSION = "version";
	//重启策略
	public final static String RESTARTPOLICY_ALWAYS = "Always";
	public final static String RESTARTPOLICY_NERVER = "Never";
	public final static String RESTARTPOLICY_ONFAILURE = "OnFailure";

	public final static int DEFAULT_PAGE_SIZE = 100;
	public final static int DEFAULT_PAGE_SIZE_1000 = 1000;
	public final static String TIME_ZONE_UTC = "UTC";

    public final static String PIPELINE_STATUS_INPROGRESS = "IN_PROGRESS";
    public final static String PIPELINE_STATUS_BUILDING = "BUILDING";
	public final static String PIPELINE_STATUS_SUCCESS = "SUCCESS";
    public final static String PIPELINE_STATUS_FAILED = "FAILED";
    public final static String PIPELINE_STATUS_FAILURE = "FAILURE";
    public final static String PIPELINE_STATUS_WAITING = "WAITING";
    public final static String PIPELINE_STATUS_NOTBUILT = "NOTBUILT";
    public final static String PIPELINE_STATUS_NOTEXECUTED = "NOT_EXECUTED";
	public final static String PIPELINE_STATUS_ABORTED = "ABORTED";
	public final static String EXTERNAL_SERVICE_NAMESPACE = "external";
	
	public final static String NODESELECTOR_LABELS_PRE = "harmonycloud.cn/";
	
	public final static String AFFINITY_WEIGHT = "weight";
	
	public final static String AFFINITY_TOPOLOGYKEY_POD_DISPERSE = "kubernetes.io/hostname";
	public final static String AFFINITY_TOPOLOGYKEY_GROUP_SCHEDULE = "harmonycloud.cn/group";

	public final static String ANTIAFFINITY_TYPE_GROUP_SCHEDULE = "group_schedule";
	
	public final static String SPACE_TRANS = "%20";
	
	public final static String NODE_IP_INTERNAL = "InternalIP";
	
	public final static String NODE_IP_ECTERNAL = "ExternalIP";
	
	public final static String FALSE = "False";

    //微服务任务状态：成功
	public final static String SPRINGCLOUD_TASK_SUCCESS = "1";

	//微服务任务状态：失败
	public final static String SPRINGCLOUD_TASK_FAILURE = "2";

	//微服务任务状态：进行中
	public final static String SPRINGCLOUD_TASK_DOING = "3";

	public final static Integer WEIGHT = 50;

	public final static String EQUAL = "=";

	public final static String AFFINITY_OPERATOR = "In";

	//TCP协议
	public final static String PROTOCOL_TCP = "TCP";

	//UDP协议
	public final static String PROTOCOL_UDP = "UDP";

	//UDP协议
	public final static String PROTOCOL_HTTP = "HTTP";

	//微服务consul组件名称
	public final static String SPRINGCLOUD_CONSUL = "msf-consul-service";

	//微服务kong组件名称
	public final static String SPRINGCLOUD_KONG = "msf-kong-service";

	//微服务kong的环境变量名称
	public final static String SPRINGCLOUD_KONG_ENV = "KONG_DNS_RESOLVER";

	//微服务任务类型—部署
	public final static Integer TASK_TYPE_DEPLOY = 0;

	//微服务任务类型——删除
	public final static Integer TASK_TYPE_DELETE = 1;

	//微服务任务类型——重置
	public final static Integer TASK_TYPE_RESET = 2;

	//微服务任务类型——重置单个组件
	public final static Integer TASK_TYPE_RESET_INSTANCE = 3;

	//使用TCP协议暴露
	public final static String EXTERNAL_PROTOCOL_TCP = "0";

	//使用UDP协议暴露
	public final static String EXTERNAL_PROTOCOL_UDP = "1";

	//使用HTTP协议暴露
	public final static String EXTERNAL_PROTOCOL_HTTP = "2";

	//外部端口正在被占用
	public final static Integer EXTERNAL_PORT_STATUS_USED = 1;

	//外部端口确定被使用
	public final static Integer EXTERNAL_PORT_STATUS_CONFIRM_USED = 2;

	//DB CRUD flag
	// DB插入操作
	public final static int DB_OPERATION_FLAG_INSERT = 1;
	// DB查询操作
	public final static int DB_OPERATION_FLAG_QUERY = 2;
	// DB修改操作
	public final static int DB_OPERATION_FLAG_UPDATE = 3;
	// DB删除操作
	public final static int DB_OPERATION_FLAG_DELETE = 4;
	/**
	 *  有则改之，无则插入
	 */
	public final static int DB_OPERATION_FLAG_SAVE = 5;

	//configmap类型
	public final static String TYPE_DAEMONSET = "harmonycloud/daemonSets";

	public final static String LABEL_DAEMONSET = "harmonycloud.cn/daemonset";

	public final static String KIND_DAEMONSET = "DaemonSet";

	//volume的logdir的名称:logdir
	public final static String VOLUME_LOGDIR_NAME = "logdir";

	//log-pilot日志收集环境变量前缀
	public final static String PILOT_LOG_PREFIX = "logpilot_logs_logstash";
	public final static String PILOT_LOG_PREFIX_TAG = "logpilot_logs_logstash_tags";

	//同步宿主机时区名称
//	public final static String VOLUME_SYNC_TIME_ZONE_NAME = "synctimezone-";

	// 时区环境变量
	public final static String ENV_TIME_ZONE = "TZ";

	//同步宿主机时区hostpath目录
	public final static String VOLUME_SYNC_TIME_ZONE_PATH = "/etc/localtime";

	public final static String VOLUME_TYPE_LOGDIR = "logdir";

	public final static String VOLUME_TYPE_CONFIGMAP = "configMap";

	public final static String NAMESPACE_SYSTEM = "kube-system";

	public final static String LABEL_PROJECT_ID = "projectId";

	public final static String LABEL_TYPE = "type";

	public final static String LABEL_INGRESS_CLASS = "kubernetes.io/ingress.class";

	//Daemonset 更新设置rollingupdate策略
	public final static String ROLLINGUPDATE_MAX_UNAVAILABLE = "100%";

	//容器健康检查默认端口
	public final static Integer LIVENESS_PORT = 80;

	//微服务使用UDP协议暴露
	public final static Integer SPRINGCLOUD_PROTOCOL_UDP = 1;

	// 清理镜像级别：仓库级别
	public final static int IMAGE_CLEAN_RULE_TYPE_REPOSITORY = 1;
	// 清理镜像级别：镜像级别
	public final static int IMAGE_CLEAN_RULE_TYPE_IMAGE = 2;
	// 清理镜像级别：Tag级别（代码中使用）
	public final static int IMAGE_CLEAN_RULE_TYPE_TAG = 3;

	public final static String DEFAULT_REPOSITORY_DISK_SIZE = "500G";

	//pod 最小准备时间（5秒）
	public final static int POD_MIN_READY_FIVE_SECONDS = 5;

	//pod 最小准备时间（5秒）
	public final static int POD_MIN_READY_ZERO_SECOND = 0;

	//灰度升级：当pod的最大可被调度数量和最大不可用数量都为0时，最大可被调度数量设置为1
	public final static int POD_MAX_SURGE = 1;

	//灰度升级：当pod的最大可被调度数量和最大不可用数量都为0时，最大不可用数量设置为1
	public final static int POD_MAX_UNAVAILABLE = 0;
	//服务修改设置maxSurge=0，先杀pod，再启新pod
	public final static int DEFAULT_POD_MAX_SURGE = 0;
	//服务修改设置maxUnavailable=1， 逐个pod滚动升级
	public final static int DEFAULT_POD_MAX_UNAVAILABLE = 1;

	//灰度升级：自动回滚到最新的版本
	public final static int ROLLBACK_REVERSION = 0;

	//应用模板部署状态：已部署
    public final static int APPLICATION_TEMPLATE_DEPLOYED = 1;

    //服务蓝绿发布查询实例数的sleep时间
	public final static int THREAD_SLEEP_TIME = 2000;

	public final static int THREAD_SLEEP_TIME_5000 = 5000;

	public final static int THREAD_SLEEP_TIME_10000 = 10000;

	//node分组标签key
	public final static String NODE_LABEL_GROUP = "group";

	//微服务接口请求头部关于token的key
	public final static String SPRINGCLOUD_REQUEST_HEADER_TOKEN = "x-acl-signature";

	//微服务用户角色:管理员
	public final static String SPRINGCLOUD_USER_ROLE_ADMIN = "SUPER-ADMIN";

	//微服务用户角色:租户管理员
	public final static String SPRINGCLOUD_USER_ROLE_TENANT = "TENANT-ADMIN";

	//微服务用户角色:空间管理员
	public final static String SPRINGCLOUD_USER_ROLE_SPACE = "SPACE-ADMIN";

	//微服务用户角色:普通用户
	public final static String SPRINGCLOUD_USER_ROLE_NORMAL = "TENANT-USER";

	//微服务空间独享与共享
	public final static String SPRINGCLOUD_NAMESPACE_SHARE = "1";

	public final static String SPRINGCLOUD_NAMESPACE_PRIVATE = "0";

	//微服务默认的cpu和memory
	public final static String SPRINGCLOUD_INSTANCE_CPU = "500m";

	public final static String SPRINGCLOUD_INSTANCE_MEMORY = "512";

	public final static int CONTAINER_RESOURCE_CPU_TIMES = 1000;

	public final static String CLUSTER_THREE_DOMAIN = "threeLevel";

	public final static String CLUSTER_FOUR_DOMAIN = "fourLevel";

	public final static Integer DOMAIN_LEVEL_FOUR = 4;

	public final static String APP_CREATER_LABEL = "creater";

	public final static String TOPO_LABEL_KEY = "topo";

	public final static int THREAD_SLEEP_TIME_1000 = 1000;

	public final static String MSF = "msf-";

	public final static String INGRESS_MULTIPLE_PORT_ANNOTATION = "nginx.ingress.listen";

	//autoscale
	public final static String LABEL_AUTOSCALE = "autoscale";
	public final static String STATUS_ON = "true";
	public final static String STATUS_OFF = "false";
	//ingress service
	public final static String LABEL_INGRESS_SERVICE = "ingress";
	public final static String INGRESS_SERVICE_TRUE = "true";
	public final static String INGRESS_SERVICE_FALSE = "false";

	public final static Integer MAX_QUERY_COUNT_100 = 100;

//	public final static String VCS_IMAGE = "/library/gitsvn";
	public final static String VCS_IMAGE = "/k8s-deploy/alpine-gitsvn";
	public final static String VCS_IMAGE_TAG = "1.0";
	public final static String PULL_WAY_GIT = "git";
	public final static String PULL_WAY_SVN = "svn";
	public final static String SERVICE_DEPENDENCE_IMAGE = "/k8s-deploy/alpine-net";
	public final static String SERVICE_DEPENDENCE_IMAGE_TAG = "1.0";
	public final static String SERVICE_DETECT_WAY_HTTP = "HTTP";
	public final static String SERVICE_DETECT_WAY_TCP = "TCP";

	public static final String TRANSFER_NAMESPACE_SUCCESS = "successUpdate";
	public static final String TRANSFER_NAMESPACE_ERROR = "errorUpdate";
	public static final String ERROR_BIND_DEPLOY = "errorBindDeploy";
	public static final String ERR_DEPLOY_DTOS ="errDeployDtos";

}