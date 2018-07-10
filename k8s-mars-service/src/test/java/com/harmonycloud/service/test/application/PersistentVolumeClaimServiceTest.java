package com.harmonycloud.service.test.application;

import com.harmonycloud.dto.application.PersistentVolumeClaimDto;
import com.harmonycloud.service.application.PersistentVolumeClaimService;
import com.harmonycloud.service.platform.bean.PvcDto;
import com.harmonycloud.service.test.BaseTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author xc
 * @date 2018/7/4 22:05
 */
public class PersistentVolumeClaimServiceTest extends BaseTest {

    private Logger LOGGER = LoggerFactory.getLogger(storageClassServiceTest.class);

    @Autowired
    PersistentVolumeClaimService persistentVolumeClaimService;

    private PersistentVolumeClaimDto persistentVolumeClaimDto;

    @BeforeClass
    public void createPersistentVolumeClaimData() {
        persistentVolumeClaimDto = new PersistentVolumeClaimDto();
        persistentVolumeClaimDto.setName("test2-claim");  //test-xc
        persistentVolumeClaimDto.setClusterId("cluster-top--dev");
        persistentVolumeClaimDto.setNamespace("test-xc-abc");
        persistentVolumeClaimDto.setStorageName("xc-test7");
        persistentVolumeClaimDto.setCapacity("1Gi");
        persistentVolumeClaimDto.setReadOnly(false);
        persistentVolumeClaimDto.setBindOne(false);
        persistentVolumeClaimDto.setProjectId("b655fe76fb3e43799469c518c6751cf3");
        persistentVolumeClaimDto.setTenantId("0c2ae6230a6643bd857a5da19c19ecb3");
    }

    @Test
    public void createPersistentVolumeClaim() throws Exception {
        assertTrue(persistentVolumeClaimService.createPersistentVolumeClaim(persistentVolumeClaimDto).isSuccess());
    }

    @Test
    public void listPersistentVolumeClaim() throws Exception {
        assertEquals(((List<PvcDto>)
                (persistentVolumeClaimService.listPersistentVolumeClaim(persistentVolumeClaimDto.getProjectId(),
                persistentVolumeClaimDto.getTenantId(), persistentVolumeClaimDto.getClusterId()).getData())).size(), 1);
    }

    @Test
    public void deletePersistentVolumeClaim() throws Exception {
        assertTrue(persistentVolumeClaimService.deletePersistentVolumeClaim(persistentVolumeClaimDto.getNamespace(),
                persistentVolumeClaimDto.getName(), persistentVolumeClaimDto.getClusterId()).isSuccess());
    }

    @Test
    public void recyclePersistentVolumeClaimTest() throws Exception {
        assertTrue(persistentVolumeClaimService.recyclePersistentVolumeClaim(persistentVolumeClaimDto.getNamespace(),
                persistentVolumeClaimDto.getName(), persistentVolumeClaimDto.getClusterId()).isSuccess());
    }
}
