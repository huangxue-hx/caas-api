package com.harmonycloud.k8s.service;

import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.common.util.CollectionUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.k8s.bean.ObjectMeta;
import com.harmonycloud.k8s.bean.ServiceAccount;
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
 * Update by xc on 2018/8/7.
 */
@Service
public class ServiceAccountService {

    public K8SClientResponse getServiceAccountByName(String namespace, String saName, Cluster cluster) throws MarsRuntimeException {
        K8SURL url = new K8SURL();
        url.setApiGroup(APIGroup.API_V1_VERSION);
        url.setNamespace(namespace);
        url.setResource(Resource.SERVICEACCOUNT);
        url.setName(saName);
        return new K8sMachineClient().exec(url, HTTPMethod.GET, null, null, cluster);
    }

    public K8SClientResponse createServiceAccount(String namespace, String name, Cluster cluster) throws Exception {
        ServiceAccount serviceAccount = new ServiceAccount();
        serviceAccount.setAutomountServiceAccountToken(true);
        ObjectMeta objectMeta = new ObjectMeta();
        objectMeta.setName(name);
        serviceAccount.setMetadata(objectMeta);

        Map<String, Object> bodys = CollectionUtil.transBean2Map(serviceAccount);
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Content-type", "application/json");

        K8SURL k8SURL = new K8SURL();
        k8SURL.setNamespace(namespace).setResource(Resource.SERVICEACCOUNT);

        return new K8sMachineClient().exec(k8SURL, HTTPMethod.POST, headers, bodys, cluster);

    }


    public K8SClientResponse deleteServiceAccount(String namespace, String name, Cluster cluster) {
        K8SURL k8SURL = new K8SURL();
        k8SURL.setNamespace(namespace).setResource(Resource.SERVICEACCOUNT).setName(name);
        return new K8sMachineClient().exec(k8SURL, HTTPMethod.DELETE, null, null, cluster);
    }

    public boolean existServiceAccount(String namespace, String name, Cluster cluster) {
        K8SURL k8SURL = new K8SURL();
        k8SURL.setNamespace(namespace).setResource(Resource.SERVICEACCOUNT).setName(name);
        K8SClientResponse response = new  K8sMachineClient().exec(k8SURL, HTTPMethod.GET, null, null, cluster);
        return  HttpStatusUtil.isSuccessStatus(response.getStatus());
    }

}
