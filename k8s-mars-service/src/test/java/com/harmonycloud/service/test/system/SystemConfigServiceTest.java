package com.harmonycloud.service.test.system;

import com.harmonycloud.dto.cicd.CicdConfigDto;
import com.harmonycloud.service.system.SystemConfigService;
import com.harmonycloud.service.test.BaseTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;

/**
 * Created by anson on 18/8/27.
 */
public class SystemConfigServiceTest extends BaseTest {

    @Autowired
    SystemConfigService systemConfigService;

    @Test
    public void testGetCicdConfig(){
        CicdConfigDto cicdConfigDto = systemConfigService.getCicdConfig();
        assertNotNull(cicdConfigDto.getRemainNumber());
        assertNotNull(cicdConfigDto.isTypeMerge());
    }
}
