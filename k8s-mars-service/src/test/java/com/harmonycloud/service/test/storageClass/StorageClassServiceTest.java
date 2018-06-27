package com.harmonycloud.service.test.storageClass;

import com.harmonycloud.dto.application.StorageClassDto;
import com.harmonycloud.dto.tenant.NamespaceDto;
import com.harmonycloud.dto.tenant.QuotaDto;
import com.harmonycloud.service.tenant.NamespaceService;
import com.harmonycloud.service.test.BaseTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by lenovo on 2018/6/19.
 */

public class StorageClassServiceTest extends BaseTest {
    protected Logger logger= LoggerFactory.getLogger(StorageClassServiceTest.class);

    @Autowired
    NamespaceService namespaceService;

    @Test
    public void testCreateNamespace() throws  Exception{
        NamespaceDto namespaceDto = new NamespaceDto();
        namespaceDto.setTenantId("c3073d4b19c0429aa4cd0ae18d099f2a");
        namespaceDto.setAliasName("011");
        namespaceDto.setClusterId("cluster-top--dev");
        namespaceDto.setName("gxd-yy122");
        QuotaDto quotaDto = new QuotaDto();
        quotaDto.setCpu("0.2");
        quotaDto.setMemory("0.1Gi");
        namespaceDto.setQuota(quotaDto);
        List<StorageClassDto> storageClassDtos = new ArrayList<>();
        StorageClassDto storageClassDto1 = new StorageClassDto();
        storageClassDto1.setName("storage001");
        storageClassDto1.setQuotaLimit("2");
        StorageClassDto storageClassDto2 = new StorageClassDto();
        storageClassDto2.setName("storage002");
        storageClassDto2.setQuotaLimit("3");
        storageClassDtos.add(storageClassDto1);
        storageClassDtos.add(storageClassDto2);
        namespaceDto.setStorageClassDtos(storageClassDtos);
        namespaceService.createNamespace(namespaceDto);

    }
}
