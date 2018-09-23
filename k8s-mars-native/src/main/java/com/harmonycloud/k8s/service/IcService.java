package com.harmonycloud.k8s.service;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.k8s.bean.ConfigMap;
import com.harmonycloud.k8s.bean.DaemonSet;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.APIGroup;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author xc
 * @date 2018/8/1 9:07
 */
@Service
public class IcService {

    public K8SClientResponse listIngressController(Cluster cluster) throws MarsRuntimeException {
        K8SURL url = new K8SURL();
        url.setApiGroup(APIGroup.APIS_APPS_V1);
        url.setNamespace(CommonConstant.KUBE_SYSTEM);
        url.setResource(Resource.DAEMONTSET);
        Map<String, Object> bodys = new HashMap<>();
        String label = "k8s-app=nginx-ingress-lb";
        bodys.put("labelSelector", label);
        return  new K8sMachineClient().exec(url, HTTPMethod.GET,null, bodys, cluster);
    }

    public K8SClientResponse getIngressController(String icName, Cluster cluster) throws MarsRuntimeException {
        K8SURL url = new K8SURL();
        url.setApiGroup(APIGroup.APIS_APPS_V1);
        url.setNamespace(CommonConstant.KUBE_SYSTEM);
        url.setResource(Resource.DAEMONTSET);
        url.setName(icName);
        return new K8sMachineClient().exec(url, HTTPMethod.GET, null, null, cluster);
    }

    public K8SClientResponse createIngressController(DaemonSet daemonSet, Cluster cluster) throws MarsRuntimeException {
        K8SURL url = new K8SURL();
        url.setApiGroup(APIGroup.APIS_APPS_V1);
        url.setNamespace(CommonConstant.KUBE_SYSTEM);
        url.setResource(Resource.DAEMONTSET);
        Map<String, Object> bodys = new HashMap<>();
        bodys.put("metadata", daemonSet.getMetadata());
        bodys.put("kind", daemonSet.getKind());
        bodys.put("spec", daemonSet.getSpec());
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        return new K8sMachineClient().exec(url, HTTPMethod.POST, headers, bodys, cluster);
    }

    public K8SClientResponse getIcConfigMapByName(String cmName, Cluster cluster) throws MarsRuntimeException {
        K8SURL url = new K8SURL();
        url.setApiGroup(APIGroup.APIS_APPS_V1);
        url.setNamespace(CommonConstant.KUBE_SYSTEM);
        url.setResource(Resource.CONFIGMAP);
        url.setName(cmName);
        return new K8sMachineClient().exec(url, HTTPMethod.GET, null, null, cluster);
    }

    public K8SClientResponse createIcConfigMap(ConfigMap configMap, Cluster cluster) throws MarsRuntimeException {
        K8SURL url = new K8SURL();
        url.setApiGroup(APIGroup.APIS_APPS_V1);
        url.setNamespace(CommonConstant.KUBE_SYSTEM);
        url.setResource(Resource.CONFIGMAP);
        Map<String, Object> bodys = new HashMap<>();
        bodys.put("metadata", configMap.getMetadata());
        bodys.put("kind", configMap.getKind());
        bodys.put("data", configMap.getData());
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        return new K8sMachineClient().exec(url, HTTPMethod.POST, headers, bodys, cluster);
    }

    public K8SClientResponse deleteIngressController(String icName, Cluster cluster) throws MarsRuntimeException {
        K8SURL url = new K8SURL();
        url.setApiGroup(APIGroup.APIS_APPS_V1);
        url.setNamespace(CommonConstant.KUBE_SYSTEM);
        url.setResource(Resource.DAEMONTSET);
        url.setName(icName);
        return new K8sMachineClient().exec(url, HTTPMethod.DELETE, null, null, cluster);
    }

    public K8SClientResponse deleteConfigMap(String Name, Cluster cluster) throws MarsRuntimeException {
        K8SURL url = new K8SURL();
        url.setApiGroup(APIGroup.APIS_APPS_V1);
        url.setNamespace(CommonConstant.KUBE_SYSTEM);
        url.setResource(Resource.CONFIGMAP);
        url.setName(Name);
        return new K8sMachineClient().exec(url, HTTPMethod.DELETE, null, null, cluster);
    }

    public K8SClientResponse updateIngressController(String icName, DaemonSet daemonSet, Cluster cluster) throws MarsRuntimeException {
        K8SURL url = new K8SURL();
        url.setApiGroup(APIGroup.APIS_APPS_V1);
        url.setNamespace(CommonConstant.KUBE_SYSTEM);
        url.setResource(Resource.DAEMONTSET);
        url.setName(icName);
        Map<String, Object> bodys = new HashMap<>();
        bodys.put("metadata", daemonSet.getMetadata());
        bodys.put("kind", daemonSet.getKind());
        bodys.put("spec", daemonSet.getSpec());
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        return new K8sMachineClient().exec(url, HTTPMethod.PUT, headers, bodys, cluster);
    }

    public K8SClientResponse getIngressByLabel(String label, Cluster cluster) throws MarsRuntimeException {
        K8SURL url = new K8SURL();
        url.setApiGroup(APIGroup.APIS_EXTENSIONS_V1BETA1_VERSION);
        url.setResource(Resource.INGRESS);
        Map<String, Object> bodys = new HashMap<>();
        bodys.put("labelSelector", label);
        return new K8sMachineClient().exec(url, HTTPMethod.GET, null, bodys, cluster);
    }

}
