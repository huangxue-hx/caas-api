package com.harmonycloud.common.Constant;

/**
 * Created by andy on 17-1-18.
 */
public class CommonConstant {

    // 逗号
    public static String COMMA = ",";
    // 斜杠
    public static String SLASH = "/";
    // 横杠
    public static String LINE = "-";
    // 等号
    public static String EQUALITY_SIGN = "=";
    // 下划线
    public static String UNDER_LINE = "_";
    // 下划线
    public static String EMPTYSTRING = "";

    // harbor project
    public static String HARBOR_PROJECT = "/api/projects";

    public static String NEPHELE_TENANT = "nephele_tenant_";

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

    public static final String NOTI = "noti";
    public static final String ID = "id";
    //role类型
    public static final String DEV = "dev";
    public static final String NULL = null;
    //等待的时间
    public static final String GRACEPERIODSECONDS = "gracePeriodSeconds";
    public static final String EMPTYMETADATA = "{}";
    //返回状态
    public static final String SUCCESS = "success";
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
    public static final String USEDTYPE = "usedType";
    public static final String TYPE = "type";
    public static final String APPLICATION_JSON = "application/json";
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
    public static final String URL = "url";
    public static final String ICONNAME = "iconName";
    public static final String SUBMENU = "subMenu";
    public static final String CPU = "cpu";
    public static final String CORE = " Core";
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
    public static final String RECYCLE = "Recycle";
    public static final String RESULTNAMESPACE = "namespace";
    public static final String BINDING = "binding";
    public static final String TIME = "time";
    public static final String NETWORKID = "networkid";
    public static final String ANNOTATION = "annotation";
    public static final String TENANTID = "tenantid";
    public static final String TENANTNAME = "tenantname";
    public static final String NAMESPACEDATA = "namespaceData";
    public static final String HARBORDATA = "harborData";
    public static final String USERDATA = "userData";
    public static final String TENANT = "tenant";
    public static final String TENANTLIST = "tenantList";
    public static final String UNCHECKED = "unchecked";
    public static final String USER = "user";
    public static final String PROJECT = "project";
    public static final String PROJECTID = "projectId";
    public static final String VERBS = "verbs";
    public static final String CLUSTORROLE = "kubeadm.alpha.kubernetes.io/role";
    public static final String CLUSTERLIST = "clusterList";
    public static final String TENANTSIZE = "tenantSize";

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
    public static final String TMUSER = "tmUser";
    public static final String RESOURCEQUOTA = "ResourceQuota";
    public static final String SPEC = "spec";
    public static final String STATUS = "status";
    public static final String NEPHELE_NETWORKID = "nephele/networkid";
    public static final String NEPHELE_NETWORKNAME = "nephele/networkname";
    public static final String NEPHELE_SUBNETNAME = "nephele/subnetname";
    public static final String NEPHELE_SUBNETID = "nephele/subnetid";
    //网络隔离方案
    public static final String NETWORK_POLICY = "net.beta.kubernetes.io/network-policy";
    public static final String NETWORK_POLICY_INGRESS = "{\"ingress\": {\"isolation\": \"DefaultDeny\"}}";
    public static final String INGRESS = "ingress";
    public static final String POLICY = "policy";
    public static final String NETWORKPOLICY = "NetworkPolicy";
    public static final String HAPOLICY = "hapolicy";
    public static final String MATCHLABELS = "matchLabels";
    
    public static final String PVC = "persistentvolumeclaims";
    public static final String RC = "replicationcontrollers";
    public static final String PERSISTENTVOLUME = "PersistentVolume";
    public static final String STORAGE = "storage";
    public static final String NFS = "nfs";
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
    public static final String MASTERNODELABEL = "node-role.kubernetes.io/master"; 
     public static final String HARMONYCLOUD_TENANTNAME_NS = "HarmonyCloud_TenantName";
     //node节点状态
     public static final String HARMONYCLOUD_STATUS = "HarmonyCloud_Status";
     //begin节点上线开始状态，done节点上线结束状态
     public static final String BEGIN = "begin";
     public static final String DONE = "done";
     //A 表示关键组件布置的节点,B 表示闲置状态,C 表示共享状态,D 私有状态，可以供私有分区独占使用
     public static final String LABEL_STATUS_A = "A";
     public static final String LABEL_STATUS_B = "B";
     public static final String LABEL_STATUS_C = "C";
     public static final String LABEL_STATUS_D = "D";
     public static final String FROM = "from";
     public static final String PAUSE = "pause";
     public static final String NORMAL = "normal";

   //用户权限模块
     public static final String PRIVILEGE_TENANT = "tenant";
     public static final String PRIVILEGE_MIRROR = "mirror";
     public static final String PRIVILEGE_CLUSTER = "cluster";
     public static final String PRIVILEGE_APPLICATION = "application";
     public static final String PRIVILEGE_CICD = "cicd";
     public static final String PRIVILEGE_STORAGE = "storage";
     public static final String PRIVILEGE_CONFIG = "config";
     public static final String PRIVILEGE = "privlege";
     
     
     public static final String ERRMSG = "errMsg";
     public static final String NEPHELE_TENANT_NETWORK = "nephele_tenant_network";
   public static final String USERID = "userId";
//   public static final String NETWORKNAMETO = "networknameto";
    public static final Boolean FALSE = false;
    public static final Boolean TRUE = true;

    public static final String ZERONUM = "0";
    public static final Integer QUOTA_NUM = 10000;

    public static final Float QUOTA_SIZE = 10240.0f;

    public static final String LDAP_IP = "ldap_ip";
    public static final String LDAP_PORT = "ldap_port";
    public static final String LDAP_BASE = "ldap_base";
    public static final String LDAP_USERDN = "ldap_userdn";
    public static final String LDAP_PASSWORD = "ldap_password";
    public static final String LDAP_IS_ON = "ldap_is_on";
    public static final String CONFIG_TYPE_LDAP = "ldap";
    
    public static final String FILE_MAX_SIZE = "file_maxsize";
    public static final String CONFIG_TYPE_FILE = "file";
    
    public static final Integer ES_SHARDS = 5;
}
