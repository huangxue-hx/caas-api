package com.harmonycloud.k8s.service;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.k8s.bean.*;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.APIGroup;
import com.harmonycloud.k8s.constant.Constant;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    /**
     * 查询某个分区下的ingress列表
     * @param namespace 分区
     * @param label ingress的标签
     * @param cluster 集群
     * @return
     * @throws MarsRuntimeException
     */
    public List<Ingress> listIngress(String namespace, String label, Cluster cluster) throws MarsRuntimeException {
        List<Ingress> ingresses = new ArrayList<>();
        K8SURL url = new K8SURL();
        url.setApiGroup(APIGroup.APIS_EXTENSIONS_V1BETA1_VERSION);
        url.setResource(Resource.INGRESS);
        url.setNamespace(namespace);
        Map<String, Object> bodys = new HashMap<>();
        if(StringUtils.isNotBlank(label)) {
            bodys.put("labelSelector", label);
        }
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET, null, bodys, cluster);
        if (Constant.HTTP_404 == response.getStatus()) {
            return ingresses;
        }
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus()) && response.getStatus() != Constant.HTTP_404) {
            UnversionedStatus status = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
            throw new MarsRuntimeException(status.getMessage());
        }
        IngressList ingressList = JsonUtil.jsonToPojo(response.getBody(), IngressList.class);
        ingresses = ingressList.getItems();
        return ingresses;
    }

}
