package com.harmonycloud.service.test.tenant;

import com.harmonycloud.dto.tenant.NamespaceDto;
import com.harmonycloud.dto.tenant.StorageClassQuotaDto;
import com.harmonycloud.service.tenant.NamespaceService;
import com.harmonycloud.service.test.BaseTest;
import com.harmonycloud.service.test.application.storageClassServiceTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xc
 * @date 2018/7/3 14:12
 */
public class NamespaceServiceTest extends BaseTest {

    private Logger LOGGER = LoggerFactory.getLogger(NamespaceServiceTest.class);

    @Autowired
    private NamespaceService namespaceService;

    private static NamespaceDto namespaceDto;

    @BeforeClass
    public void createNamespaceData() {
        namespaceDto.setName("sc");
        namespaceDto.setAliasName("storage-test");
        namespaceDto.setTenantId("3bf2aa5e582d4cfcb941abb0f5a568c6");
        List<StorageClassQuotaDto> storageClassQuotaList = new ArrayList<>();
        StorageClassQuotaDto storageClassQuotaDto = new StorageClassQuotaDto();
//        storageClassQuotaDto.set
//        namespaceDto.setStorageClassQuotaList();
    }

    @Test(priority = 0)
    public void createNamespaceQuota() {

    }

    @Test(priority = 1)
    public void deleteNamespaceQuota() {

    }
}
