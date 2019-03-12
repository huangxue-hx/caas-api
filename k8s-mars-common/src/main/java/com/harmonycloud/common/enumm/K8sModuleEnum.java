package com.harmonycloud.common.enumm;

import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zhangkui on 2017/4/19.
 */
public enum K8sModuleEnum {
    KUBE_APISERVER("kube-apiserver","kube-apiserver","pod", "k8s API服务"),
    KUBE_CONTROLLER_MANAGER("kube-controller-manager","kube-controller-manager","pod", "k8s控制管理器"),
    KUBE_SCHEDULER("kube-scheduler","kube-scheduler","pod","k8s调度器"),
    KUBE_DNS("kube-dns", "kube-dns","deployment","域名解析"),
    ELASTICSEARCH("elasticsearch-logging","elasticsearch-logging","deployment","ES日志服务"),
    FLUENTD("fluentd","fluentd-es-v1.22","daemonset","日志采集服务"),
    //es和fluentd组合的日志组件
    LOGGING("logging","","","日志"),
    SERVICE_LOADBALANCER("nginx","nginx-ingress-controller","daemonset","负载均衡"),
    CALICO("calico","calico-node","daemonset","集群网络"),
    CALICO_KUBE_CONTROLLER("calico-kube-controllers","calico-kube-controllers","deployment","集群网络"),
    HCIPAM("hcipam","hcipam-node","daemonset","集群网络"),
    ETCD("etcd","etcd","pod","etcd存储"),
    NFS("nfs","nfs-client-provisioner","deployment","nfs存储"),
    HEAPSTER("heapster","heapster","deployment","资源监控"),
    WEBAPI("webapi","webapi","deployment","云平台后台"),
    WEBPAGE("webpage","webpage","deployment","云平台前端"),
    OAM_TASK("oam-task","oam-task","deployment","云平台告警任务"),
    OAM_API("oam-api","oam-api","deployment","云平台告警API"),
    INFLUXDB("influxdb","monitoring-influxdb","deployment","资源监控存储"),
    //heapster和influxdb组合的监控组件
    MONITOR("monitor","","","监控");

    private String code;
    private String k8sComponentName;
    //部署方式，pod， deployment， daemonset
    private String deployType;
    private String name;

    /**
     * 存放所有的code和Enmu的转换.
     */
    private static final Map<String, K8sModuleEnum> K8S_MODULE_MAP = new ConcurrentHashMap<>(
            K8sModuleEnum.values().length);


    static {
        /**
         * 将所有的实体类放入到map中,提供查询.
         */
        for (K8sModuleEnum type : EnumSet.allOf(K8sModuleEnum.class)) {
            K8S_MODULE_MAP.put(type.getCode(), type);
        }
    }

    K8sModuleEnum(String code, String k8sComponentName, String deployType, String name) {
        this.setCode(code);
        this.setK8sComponentName(k8sComponentName);
        this.setDeployType(deployType);
        this.setName(name);
    }

    public static Map<String, K8sModuleEnum> getModuleMap(){
        return K8S_MODULE_MAP;
    }

    public static K8sModuleEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        return K8S_MODULE_MAP.get(code);
    }

    public String getCode() {
        return code;
    }

    private void setCode(String code) {
        this.code = code;
    }

    public String getK8sComponentName() {
        return k8sComponentName;
    }

    private void setK8sComponentName(String k8sComponentName) {
        this.k8sComponentName = k8sComponentName;
    }

    public String getDeployType() {
        return deployType;
    }

    private void setDeployType(String deployType) {
        this.deployType = deployType;
    }

    public String getName() {
        return name;
    }

    private void setName(String name) {
        this.name = name;
    }
}
