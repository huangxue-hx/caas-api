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
        persistentVolumeClaimDto.setName("test-xc");
        persistentVolumeClaimDto.setClusterId("cluster-top--dev");
        persistentVolumeClaimDto.setNamespace("ttsx-estr");
        persistentVolumeClaimDto.setStorageClassName("xc-test1");
        persistentVolumeClaimDto.setCapacity("1Gi");
        persistentVolumeClaimDto.setReadOnly(false);
        persistentVolumeClaimDto.setBindOne(false);
        persistentVolumeClaimDto.setProjectId("6dfdd158e414456fb40b2ea586fa63f8");
        persistentVolumeClaimDto.setTenantId("5bde05df703449e5b183d81423322e2c");
    }

    @Test(priority = 0)
    public void createPersistentVolumeClaim() throws Exception {
        assertTrue(persistentVolumeClaimService.createPersistentVolumeClaim(persistentVolumeClaimDto).isSuccess());
    }

    @Test(priority = 1)
    public void listPersistentVolumeClaim() throws Exception {
        assertEquals(((List<PvcDto>)
                (persistentVolumeClaimService.listPersistentVolumeClaim(persistentVolumeClaimDto.getProjectId(),
                persistentVolumeClaimDto.getTenantId(), persistentVolumeClaimDto.getClusterId()).getData())).size(), 1);
    }

    @Test(priority = 2)
    public void recyclePersistentVolumeClaimTest() throws Exception {
        assertTrue(persistentVolumeClaimService.recyclePersistentVolumeClaim(persistentVolumeClaimDto.getNamespace(),
                persistentVolumeClaimDto.getName(), persistentVolumeClaimDto.getClusterId()).isSuccess());
    }

    @Test(priority = 3)
    public void deletePersistentVolumeClaim() throws Exception {
        assertTrue(persistentVolumeClaimService.deletePersistentVolumeClaim(persistentVolumeClaimDto.getNamespace(),
                persistentVolumeClaimDto.getName(), persistentVolumeClaimDto.getClusterId()).isSuccess());
    }
}
