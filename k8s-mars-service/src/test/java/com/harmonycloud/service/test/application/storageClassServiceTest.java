package com.harmonycloud.service.test.application;

import com.google.gson.Gson;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.application.StorageClassDto;
import com.harmonycloud.service.application.StorageClassService;
import com.harmonycloud.service.test.BaseTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author xc
 * @date 2018/6/19 19:38
 */

public class storageClassServiceTest extends BaseTest {

    private Logger LOGGER = LoggerFactory.getLogger(storageClassServiceTest.class);

    @Autowired
    private StorageClassService storageClassService;

    private static StorageClassDto storageClassDto;

    private static StorageClassDto storageClassDto2;

    @BeforeClass
    public void createStorageClassData() {
        Map<String, String> configMap = new HashMap<>();
        configMap.put("NFS_SERVER", "10.10.101.91");
        configMap.put("NFS_PATH", "/nfs/top");
        storageClassDto = new StorageClassDto();
        storageClassDto.setName("test-storage-class");
        storageClassDto.setType("NFS");
        storageClassDto.setClusterId(devClusterId);
        storageClassDto.setStorageLimit("10");
        storageClassDto.setConfigMap(configMap);

        storageClassDto2 = new StorageClassDto();
        storageClassDto.setName("dependence-storage-class");
        storageClassDto.setType("NFS");
        storageClassDto.setClusterId(platformClusterId);
        storageClassDto.setStorageLimit("10");
        storageClassDto.setConfigMap(configMap);

    }

    @Test(priority = 0)
    public void createStorageClassTest() throws Exception {
        assertTrue(storageClassService.createStorageClass(storageClassDto).isSuccess());
        assertTrue(storageClassService.createStorageClass(storageClassDto2).isSuccess());
    }

    @Test(priority = 1)
    public void getStorageClassByNameTest() throws Exception {
        String clusterId = storageClassDto.getClusterId();
        String scName = storageClassDto.getName();
        ActionReturnUtil actionReturnUtil = storageClassService.getStorageClass(scName, clusterId);
        Gson gson = new Gson();
        LOGGER.info("StorageClass: {}", gson.toJson(actionReturnUtil.getData()));
        assertTrue(actionReturnUtil.isSuccess());
    }

    @Test(priority = 2)
    public void listStorageClassByNameTest() throws Exception {
        String clusterId = storageClassDto.getClusterId();
        List<StorageClassDto> storageClassDtos= storageClassService.listStorageClass(clusterId);
        Gson gson = new Gson();
        LOGGER.info("StorageClass List: {}", gson.toJson(storageClassDtos));
        assertNotNull(storageClassDtos);
    }

    @Test(priority = 3)
    public void deleteStorageClassTest() throws Exception {
        String clusterId = storageClassDto.getClusterId();
        String scName = storageClassDto.getName();
        assertTrue(storageClassService.deleteStorageClass(scName, clusterId).isSuccess());

        assertTrue(storageClassService.deleteStorageClass(storageClassDto2.getName(), platformClusterId).isSuccess());
    }
}
