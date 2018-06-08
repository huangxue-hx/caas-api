package com.harmonycloud.service.test.cluster;

import com.alibaba.fastjson.JSONObject;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.test.JUnit4ClassRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.Assert.*;


/**
 * Created by lucia on 2018/6/7.
 */
@RunWith(JUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
@Transactional
@WebAppConfiguration
public class ClusterServiceTest {

    protected Logger logger= LoggerFactory.getLogger(ClusterServiceTest.class);

    @Autowired
    ClusterService clusterService;

    private List<Cluster> clusters;

    @Before
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
        logger.info("test get platform cluster:{}", JSONObject.toJSONString(cluster));
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
