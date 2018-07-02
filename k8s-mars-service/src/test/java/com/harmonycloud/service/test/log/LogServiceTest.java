package com.harmonycloud.service.test.log;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.k8s.bean.Pod;
import com.harmonycloud.k8s.bean.PodList;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.APIGroup;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import com.harmonycloud.service.application.DeploymentsService;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.platform.service.LogService;
import com.harmonycloud.service.test.BaseTest;
import com.harmonycloud.service.test.application.DeploymentsServiceTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertTrue;

/**
 * Created by zqf on 2018/6/28.
 */
public class LogServiceTest extends BaseTest {
    protected Logger logger = LoggerFactory.getLogger(DeploymentsServiceTest.class);

    @Autowired
    LogService logService;
    @Autowired
    DeploymentsService deploymentsService;
    @Autowired
    ClusterService clusterService;

    Cluster cluster = null;
    String path = "/opt/logs";

    public PodList getPodList() throws Exception {

        cluster = clusterService.findClusterById(devClusterId);
        K8SURL url = new K8SURL();
        url.setApiGroup(APIGroup.API_V1_VERSION);
        url.setNamespace(CommonConstant.KUBE_SYSTEM);
        url.setResource(Resource.POD);
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET,null,null, cluster);
        if(HttpStatusUtil.isSuccessStatus(response.getStatus())){
            return K8SClient.converToBean(response,PodList.class);
        }
        return null;
    }

    @Test
    public void testQueryLogFile() throws Exception {
        List<String> fileList = new ArrayList<>();
        PodList podList = getPodList();
        List<Pod> items = podList.getItems();
        for (Pod item : items) {
            if(item.getMetadata().getName().startsWith("webapi-")){
                System.out.println(item.getMetadata().getName());
                 fileList = logService.queryLogFile(item.getMetadata().getName(), CommonConstant.KUBE_SYSTEM, path, cluster.getId());
            }
        }
        assertTrue(!fileList.isEmpty());
        System.out.println(fileList);
    }

}
