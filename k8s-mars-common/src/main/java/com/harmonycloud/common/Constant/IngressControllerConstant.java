package com.harmonycloud.common.Constant;

/**
 * @author xc
 * @date 2018/8/7 10:22
 */
public class IngressControllerConstant {

    public static final String HEALTH_MIN_PORT = "healthMinPort";

    public static final String HEALTH_MAX_PORT = "healthMaxPort";

    public static final String HTTP_MIN_PORT = "httpMinPort";

    public static final String HTTP_MAX_PORT = "httpMaxPort";

    public static final String HTTPS_MIN_PORT = "httpsMinPort";

    public static final String HTTPS_MAX_PORT = "httpsMaxPort";

    public static final String STATUS_MIN_PORT = "statusMinPort";

    public static final String STATUS_MAX_PORT = "statusMaxPort";

    public static final String HTTP_PORT = "httpPort";

    public static final String HTTPS_PORT = "httpsPort";

    public static final String HEALTH_PORT = "healthPort";

    public static final String STATUS_PORT = "statusPort";

    public static final String LABEL_KEY_NAME = "name";

    public static final String LABEL_KEY_APP = "k8s-app";

    public static final String LABEL_KEY_INGRESS_CONTROLLER_NAME = "ic-name";

    public static final String ANNOTATIONS_KEY_ALIAS_NAME = "ic-aliasname";
    public static final String ANNOTATIONS_KEY_EXTERNAL_HTTP_PORT = "ic-external-http-port";
    public static final String ANNOTATIONS_KEY_EXTERNAL_HTTPS_PORT = "ic-external-https-port";

    public static final String LABEL_VALUE_APP_NGINX = "nginx-ingress-lb";

    public static final String LABEL_VALUE_NGINX_CUSTOM = "nginx-custom";

    public static final String IC_DEFAULT_ALIAS_NAME = "全局负载均衡";
    //全局负载均衡器默认名称
    public final static String IC_DEFAULT_NAME = "nginx-ingress-controller";
    //全局负载均衡器默认端口
    public final static String IC_DEFAULT_PORT = "80";
    //全局负载均衡tcp配置文件名称
    public final static String EXPOSE_CONFIGMAP_NAME_TCP = "system-expose-nginx-config-tcp";
    //全局负载均衡udp配置文件名称
    public final static String EXPOSE_CONFIGMAP_NAME_UDP = "system-expose-nginx-config-udp";

    public static final String DAEMONSET_SPEC_UPDATESTRATEGY_TYPE = "OnDelete";

    public static final int TEMPLATE_SPEC_TERMINATIONGRACEPERIODSECONDS = 60;

    public static final String TEMPLATE_SPEC_NODESELECTOR_KEY = "lb";

    public static final String TEMPLATE_SPEC_NODESELECTOR_VALUE = "nginx";

    public static final boolean TEMPLATE_SPEC_HOSTNETWORK = true;

    public static final String CONTAINER_IMAGE = "k8s-deploy/nginx-ingress-controller";

    public static final String CONTAINER_READINESSPROBE_PATH = "/healthz";

    public static final String CONTAINER_READINESSPROBE_SCHEME = "HTTP";

    public static final String CONTAINER_LIVENESSPROBE_PATH = "/healthz";

    public static final String CONTAINER_LIVENESSPROBE_SCHEME = "HTTP";

    public static final int CONTAINER_LIVENESSPROBE_INITIALDELAYSECONDS = 10;

    public static final int CONTAINER_LIVENESSPROBE_TIMEOUTSECONDS = 1;

    public static final String CONTAINER_ENV_ONE_NAME = "POD_NAME";

    public static final String CONTAINER_ENV_ONE_FieldPath = "metadata.name";

    public static final String CONTAINER_ENV_TWO_NAME = "POD_NAMESPACE";

    public static final String CONTAINER_ENV_TWO_FieldPath = "metadata.namespace";

    public static final String CONTAINER_ARGS_NAME = "/nginx-ingress-controller";

    public static final String CONTAINER_ARGS_HTTP = "--http-port=";

    public static final String CONTAINER_ARGS_HTTPS = "--https-port=";

    public static final String CONTAINER_ARGS_HEALTH = "--healthz-port=";

    public static final String CONTAINER_ARGS_STATUS = "--status-port=";

    public static final String CONTAINER_ARGS_IC_CM = "--configmap=$(POD_NAMESPACE)/ingress-nginx";

    public static final String CONTAINER_ARGS_UDP_CM = "--udp-services-configmap=$(POD_NAMESPACE)/udp-";

    public static final String CONTAINER_ARGS_TCP_CM = "--tcp-services-configmap=$(POD_NAMESPACE)/tcp-";

    public static final String CONTAINER_ARGS_DEFAULT_BACKEND = "--default-backend-service=$(POD_NAMESPACE)/default-http-backend";

    public static final String CONTAINER_ARGS_INGRESS_CLASS = "--ingress-class=";

    public static final String CONTAINER_ARGS_VOLUMEMOUNT_PATH = "/var/run/secrets/kubernetes.io/serviceaccount";

    public static final boolean CONTAINER_ARGS_VOLUMEMOUNT_READONLY = true;

    public static final int TEMPLATE_SPEC_VOLUMES_DEFAULTMODE = 420;
}
