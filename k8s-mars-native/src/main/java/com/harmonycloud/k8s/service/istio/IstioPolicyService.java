package com.harmonycloud.k8s.service.istio;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.Constant;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by weg on 18-12-19.
 */
@Service
public class IstioPolicyService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RateLimitService.class);

    /**
     * 根据label删除资源信息
     */
    public void deleteIstioPolicyResource(String namespace, Map<String, Object> bodys, String resource, Cluster cluster) throws MarsRuntimeException {
        K8SURL url = new K8SURL();
        url.setNamespace(namespace).setResource(resource);
        Map<String, Object> headers = new HashMap<>();
        headers.put(CommonConstant.CONTENT_TYPE, CommonConstant.APPLICATION_JSON);
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.DELETE, headers, bodys, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus()) && Constant.HTTP_404 != response.getStatus()) {
            LOGGER.error(String.format("istio policy resource %s delete failed", resource));
        }
    }
}
