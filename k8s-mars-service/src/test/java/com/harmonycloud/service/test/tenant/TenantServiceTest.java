package com.harmonycloud.service.test.tenant;

import com.harmonycloud.dto.tenant.ClusterQuotaDto;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.service.tenant.TenantService;
import com.harmonycloud.service.test.JUnit4ClassRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author lixiang
 * @date 2018-09-04 16:12
 */
@RunWith(JUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
@Transactional
@WebAppConfiguration
public class TenantServiceTest {

    @Autowired
    TenantService tenantService;

    @Test
    public void tesDelCluseterQuota() throws Exception {
        String tenantId="45b43b7aec0d436894c4ffe281efab0c";
        String tenantName="lixiang";
        ClusterQuotaDto clusterQuota = new ClusterQuotaDto();
        clusterQuota.setMemoryQuota(0.0);
        clusterQuota.setCpuQuota(0.0);
        clusterQuota.setId(128);
        clusterQuota.setClusterId("cluster-top--qas");
        tenantService.removeClusterQuota(tenantName,tenantId,clusterQuota);
    }
}