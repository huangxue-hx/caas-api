package com.harmonycloud.service.test.cluster;

import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.test.BaseTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static org.junit.Assert.*;


/**
 * Created by lucia on 2018/6/7.
 */
public class ClusterServiceTest extends BaseTest {

    protected Logger logger= LoggerFactory.getLogger(ClusterServiceTest.class);

    @Autowired
    ClusterService clusterService;

    private List<Cluster> clusters;

    @BeforeMethod
    public void setCluster() throws Exception{
        clusters = clusterService.listAllCluster(null);
    }

    @Test
    public void testGetPlatformCluster() throws Exception {
        Cluster cluster = clusterService.getPlatformCluster();
        assertNotNull(cluster);
        assertNotNull(cluster.getHarborServer());
        assertTrue(cluster.getIsEnable());
        assertEquals(String.valueOf(cluster.getLevel()),"0");
    }

    @Test
    public void testListCluster() throws Exception {
        assertTrue(clusters.size() > 0);
        List<Cluster> enabledBusinessClusters = clusterService.listCluster();
        List<Cluster> enabledBusinessClusterList = clusterService.listCluster(null,true,null);
        assertEquals(enabledBusinessClusters.size(), enabledBusinessClusterList.size());
    }

    @Test
    public void testGetCluster() throws Exception {
        for(int i=0; i<clusters.size();i++){
            Cluster cluster = clusterService.findClusterById(clusters.get(i).getId());
            assertEquals(cluster, clusters.get(i));
        }
    }
}
