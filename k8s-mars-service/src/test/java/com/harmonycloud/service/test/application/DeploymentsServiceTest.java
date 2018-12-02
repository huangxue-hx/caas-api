package com.harmonycloud.service.test.application;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.k8s.bean.Deployment;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.service.DeploymentService;
import com.harmonycloud.service.application.DeploymentsService;
import com.harmonycloud.service.tenant.NamespaceLocalService;
import com.harmonycloud.service.test.BaseTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertTrue;

public class DeploymentsServiceTest extends BaseTest {
    protected Logger logger = LoggerFactory.getLogger(DeploymentsServiceTest.class);

    @Autowired
    private DeploymentsService deploymentsService;

    @Autowired
    private DeploymentService deploymentService;

    @Autowired
    private NamespaceLocalService namespaceLocalService;

    private String namespace;
    private String deploymentName;
    private Cluster cluster;

    @BeforeMethod
    public void setUp() throws Exception {
        namespace = "shenzhe-fenqu1";
        deploymentName = "apptest";
        cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
    }

    @Test
    public void testUpdateLabels() throws Exception {
        Map<String, Object> label = new HashMap<String, Object>();
        label.put("testKey", "testValue");   //add or update labels
        //label.put("testKey", null);    //delete labels
        assertTrue(!label.isEmpty());
        ActionReturnUtil result = deploymentsService.updateLabels(namespace, deploymentName, cluster, label);
        assertTrue(result.isSuccess());
    }

    @Test
    public void tesListDeploys() throws Exception {
        List<Map<String, Object>> deployments = deploymentsService.listTenantDeploys(tenantId, devClusterId);
        assertTrue(deployments.size() > 0);
    }
}
