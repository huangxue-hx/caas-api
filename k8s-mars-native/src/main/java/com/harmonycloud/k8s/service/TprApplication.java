package com.harmonycloud.k8s.service;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.k8s.bean.BaseResource;
import com.harmonycloud.k8s.bean.UnversionedStatus;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import org.springframework.stereotype.Service;
import com.harmonycloud.k8s.constant.Resource;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by root on 8/3/17.
 */
@Service
public class TprApplication {

    /**
     * 创建 application
     * @param application
     * @param cluster
     * @return application
     */
    public ActionReturnUtil createApplication(BaseResource application, Cluster cluster) throws Exception {
        K8SURL url = new K8SURL();
        url.setResource(Resource.APP).setNamespace(application.getMetadata().getNamespace());
        Map<String, Object> bodys = new HashMap<>();
        bodys.put("metadata", application.getMetadata());
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        K8SClientResponse response = new K8SClient().doit(url, HTTPMethod.POST,headers,bodys,cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            UnversionedStatus us = JsonUtil.jsonToPojo(response.getBody().toString(),UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithMsg(us.getMessage());
        }
        return ActionReturnUtil.returnSuccess();
    }

    /**
     * 删除特定 Application
     * @param cluster
     * @param namespace
     * @param name
     * @return
     */
    public ActionReturnUtil delApplicationByName(String name, String namespace, Cluster cluster) throws Exception {
        K8SURL url = new K8SURL();
        url.setNamespace(namespace).setResource(Resource.APP).setSubpath(name);
        K8SClientResponse response = new K8SClient().doit(url, HTTPMethod.DELETE,null,null,cluster);
        if(HttpStatusUtil.isSuccessStatus(response.getStatus())){
            return ActionReturnUtil.returnSuccess();
        }
        UnversionedStatus us = JsonUtil.jsonToPojo(response.getBody().toString(),UnversionedStatus.class);
        return ActionReturnUtil.returnErrorWithMsg(us.getMessage());
    }


    /**
     * 更新指定的Application
     * @param cluster
     * @param namespace
     * @param
     * @return
     */

    //todo
    public ActionReturnUtil updateApplication(String namespace, String name, BaseResource application, Cluster cluster) throws Exception {
        K8SURL url = new K8SURL();
        url.setNamespace(namespace).setResource(Resource.APP).setName(name);
        Map<String, Object> bodys = new HashMap<>();
        bodys.put("metadata", application.getMetadata());
        bodys.put("kind", application.getKind());
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        K8SClientResponse response = new K8SClient().doit(url, HTTPMethod.PUT,headers,bodys,cluster);
        if(!HttpStatusUtil.isSuccessStatus(response.getStatus())){
            UnversionedStatus us = JsonUtil.jsonToPojo(response.getBody().toString(),UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithMsg(us.getMessage());
        }
        return  ActionReturnUtil.returnSuccess();
    }

    /**
     * 获取某namespace下所有application
     * @param namespace
     * @return
     */
    public K8SClientResponse listApplicationByNamespace(String namespace, Map<String, Object> headers, Map<String, Object> bodys, String method, Cluster cluster) throws Exception {
        K8SURL url = new K8SURL();
        url.setNamespace(namespace).setResource(Resource.APP);
        K8SClientResponse response = new K8SClient().doit(url, method, headers, bodys, cluster);
        return response;
    }

    /**
     * 获取application
     * @param name
     * @param namespace
     * @return
     */
    public K8SClientResponse getApplicationByName(String namespace, String name,Map<String, Object> headers, Map<String, Object> bodys, String method, Cluster cluster) throws Exception {
        K8SURL url = new K8SURL();
        url.setNamespace(namespace).setResource(Resource.APP).setName(name);
        K8SClientResponse response = new K8SClient().doit(url, method, headers, bodys, cluster);
        return response;
    }


}