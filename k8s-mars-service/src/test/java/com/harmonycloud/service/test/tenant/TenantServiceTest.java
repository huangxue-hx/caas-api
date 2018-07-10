package com.harmonycloud.service.test.tenant;

import com.harmonycloud.dto.tenant.TenantDto;
import com.harmonycloud.service.tenant.TenantService;
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
 * @date 2018/7/3 14:32
 */
public class TenantServiceTest extends BaseTest {

    private Logger LOGGER = LoggerFactory.getLogger(TenantServiceTest.class);

    @Autowired
    TenantService tenantService;

    private static TenantDto tenantDto;

    @BeforeClass
    public void createTenantData() {
        tenantDto.setTenantName("test-xc");
        tenantDto.setAliasName("test-xc");
        List<String> tmList = new ArrayList<>();
        tmList.add("xuchao");
        tenantDto.setTmList(tmList);
    }

    @Test
    public void createTenant() throws Exception {
//        assertTrue(tenantService.createTenant(tenantDto));
    }

    @Test
    public void createTenantQuota() {

    }

    @Test
    public void updateTenantQuota() {

    }

    @Test
    public void deleteTenantQuota() {

    }


}
