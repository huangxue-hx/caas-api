package com.harmonycloud.k8s.service;

import com.alibaba.fastjson.JSONObject;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.CollectionUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.bean.ObjectMeta;
import com.harmonycloud.k8s.bean.cluster.ClusterCRD;
import com.harmonycloud.k8s.bean.cluster.ClusterCRDList;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;


public class ClusterCRDService {
    Logger LOGGER  = LoggerFactory.getLogger(ClusterCRDService.class);
    public K8SClientResponse getCluster(String namespace, String name, Cluster cluster) throws Exception {
        K8SURL url = new K8SURL();
        url.setApiGroup(Resource.getGroupByResource(Resource.CLUSTER));
        url.setNamespace(namespace).setResource(Resource.CLUSTER).setSubpath(name);
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET, null, null, cluster);
        return response;
    }

    public K8SClientResponse getClusterBase(Cluster cluster) throws Exception {
        K8SURL url = new K8SURL();
        url.setApiGroup(Resource.getGroupByResource(Resource.CLUSTERBASE));
        url.setNamespace("kube-system").setResource(Resource.CLUSTERBASE).setSubpath("clusterinfo");
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET, null, null, cluster);
        return response;
    }

    public ClusterCRDList listCluster(String labels, String namespace, Cluster cluster) throws Exception {
        Map<String, Object> bodys = new HashMap<>();
        if (!StringUtils.isBlank(labels)) {
            bodys.put("labelSelector", labels);
        }
        K8SURL url = new K8SURL();
        url.setApiGroup(Resource.getGroupByResource(Resource.CLUSTER));
        if(StringUtils.isBlank(namespace)){
            url.setResource(Resource.CLUSTER);
        }else {
            url.setNamespace(namespace).setResource(Resource.CLUSTER);
        }

        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET, null, bodys, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            LOGGER.error("list cluster response:{},cluster:{}", JSONObject.toJSONString(response),JSONObject.toJSONString(cluster));
            return null;
        }
        ClusterCRDList clusterCRDList = K8SClient.converToBean(response, ClusterCRDList.class);
        return clusterCRDList;
    }

    public K8SClientResponse deleteCluster(String namespace, String name, Cluster cluster) throws Exception {
        K8SURL url = new K8SURL();
        url.setApiGroup(Resource.getGroupByResource(Resource.CLUSTER));
        url.setNamespace(namespace).setResource(Resource.CLUSTER).setSubpath(name);
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.DELETE,null,null, cluster);
        return response;
    }

    public K8SClientResponse addCluster( ClusterCRD clusterCRD,Cluster cluster) throws Exception {
        K8SURL url = new K8SURL();
        url.setApiGroup(Resource.getGroupByResource(Resource.CLUSTER));
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Content-type", "application/json");
        url.setNamespace(clusterCRD.getMetadata().getNamespace()).setResource(Resource.CLUSTER);
        Map<String, Object> bodys = new HashMap<String, Object>();
        bodys = CollectionUtil.transBean2Map(clusterCRD);
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.POST,headers,bodys, cluster);
        return response;
    }



    public K8SClientResponse updateCluster( ClusterCRD clusterCDR,Cluster cluster) throws Exception {
        K8SURL url = new K8SURL();
        url.setApiGroup(Resource.getGroupByResource(Resource.CLUSTER));
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Content-type", "application/json");
        ObjectMeta meta = clusterCDR.getMetadata();
        url.setNamespace(meta.getNamespace()).setResource(Resource.CLUSTER).setSubpath(meta.getName());
        Map<String, Object> bodys = new HashMap<String, Object>();
        bodys = CollectionUtil.transBean2Map(clusterCDR);
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.PUT, headers,bodys, cluster);
        return response;
    }

//    public ClusterTPR initClusterTPR(ClusterTPR clusterTPR) throws Exception {
//        clusterTPR.setApiVersion("harmonycloud.cn/v1");
//        clusterTPR.setKind(Resource.CLUSTER);
//        return clusterTPR;
//    }

}
