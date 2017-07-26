package com.harmonycloud.service.application.impl;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.k8s.bean.ConfigMap;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import com.harmonycloud.service.application.ConfigMapService;
import org.springframework.stereotype.Service;

/**
 * Created by czm on 2017/6/1.
 */
@Service
public class ConfigMapServiceImpl implements ConfigMapService {
    
    @Override
    public ActionReturnUtil getConfigMapByName(String namespace, String name, String method, Cluster cluster) throws Exception {
        K8SURL url = new K8SURL();
        url.setNamespace(namespace).setName(name).setResource(Resource.CONFIGMAP);
        K8SClientResponse response = new K8SClient().doit(url, HTTPMethod.GET, null, null, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            return ActionReturnUtil.returnErrorWithMsg(response.getBody());
        }
        ConfigMap configMap = JsonUtil.jsonToPojo(response.getBody(), ConfigMap.class);
        return ActionReturnUtil.returnSuccessWithData(configMap);
    }
}
