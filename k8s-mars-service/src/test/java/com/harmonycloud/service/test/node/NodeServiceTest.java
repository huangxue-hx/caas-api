package com.harmonycloud.service.test.node;

import com.alibaba.fastjson.JSONObject;
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
import com.harmonycloud.service.platform.service.NodeService;
import com.harmonycloud.service.test.BaseTest;
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
public class NodeServiceTest extends BaseTest {
    protected Logger logger = LoggerFactory.getLogger(NodeServiceTest.class);

    @Autowired
    private NodeService nodeService;
    @Autowired
    private ClusterService clusterService;

    @Test
    public void testGetPrivateNamespaceNodeList() throws Exception {
        Cluster cluster = clusterService.findClusterById(devClusterId);
        List<String> nodeNames = nodeService.getPrivateNamespaceNodeList(TEST_NAME+"-"+TEST_NAME, cluster);
        logger.info("nodes:{}", JSONObject.toJSONString(nodeNames));
    }

}
