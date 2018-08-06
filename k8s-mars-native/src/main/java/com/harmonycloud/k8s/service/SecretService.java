package com.harmonycloud.k8s.service;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.CollectionUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.k8s.bean.PersistentVolumeClaim;
import com.harmonycloud.k8s.bean.UnversionedStatus;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.APIGroup;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class SecretService {
	

	public K8SClientResponse getSpecifiedSecret(String cephAdminrSecretName, String namespace,  Cluster cluster) throws Exception {
        K8SURL url = new K8SURL();
        if(StringUtils.isBlank(namespace)){
            url.setResource(Resource.SECRET);
        }else {
            url.setNamespace(namespace).setResource(Resource.SECRET);
        }
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET, null, null, cluster);
        return response;
    }

    public K8SClientResponse deleteSecret(String cephAdminrSecretName, String namespace, Cluster cluster) throws Exception {
        K8SURL url = new K8SURL();
        url.setName(cephAdminrSecretName).setNamespace(namespace).setResource(Resource.SECRET);
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        Map<String, Object> bodys = new HashMap<>();
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.DELETE, headers, bodys, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            throw new MarsRuntimeException(response.getBody());
        }
        return response;
    }


}
