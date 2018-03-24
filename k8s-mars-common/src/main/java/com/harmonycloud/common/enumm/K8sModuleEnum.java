package com.harmonycloud.common.enumm;

import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zhangkui on 2017/4/19.
 */
public enum K8sModuleEnum {
    KUBE_APISERVER("kube-apiserver", "k8s API服务"),
    KUBE_CONTROLLER_MANAGER("kube-controller-manager","k8s控制管理器"),
    KUBE_SCHEDULER("kube-scheduler","k8s调度器"),
    KUBE_DNS("kube-dns", "域名解析"),
    ELASTICSEARCH("elasticsearch-logging","ES日志服务"),
    SERVICE_LOADBALANCER("nginx","负载均衡"),
    CALICO("calico","集群网络"),
    ETCD("etcd","etcd存储"),
    NFS("nfs","nfs存储"),
    MONITOR("monitor","监控"),
    HEAPSTER("heapster","资源监控"),
    INFLUXDB("influxdb","资源监控存储");

    private String code;
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

    K8sModuleEnum(String code, String name) {
        this.setCode(code);
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

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
