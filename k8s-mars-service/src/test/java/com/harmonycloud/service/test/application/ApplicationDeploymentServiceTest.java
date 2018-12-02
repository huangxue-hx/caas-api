package com.harmonycloud.service.test.application;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.application.ApplicationDto;
import com.harmonycloud.service.application.ApplicationDeployService;
import com.harmonycloud.service.test.BaseTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertNotNull;
public class ApplicationDeploymentServiceTest extends BaseTest {
    protected Logger logger = LoggerFactory.getLogger(ApplicationDeploymentServiceTest.class);

    @Autowired
    private ApplicationDeployService applicationDeployService;

    @Test
    public void testSearchApplication() throws Exception {
        ApplicationDto applicationDto = new ApplicationDto();
        applicationDto.setTenantId(tenantId);
        applicationDto.setProjectId(projectId);
        applicationDto.setClusterId(devClusterId);
        ActionReturnUtil result = applicationDeployService.searchApplication(applicationDto);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertTrue(((List)result.getData()).size()>0);
        applicationDto.setClusterId(qasClusterId);
        result = applicationDeployService.searchApplication(applicationDto);
        assertTrue(result.isSuccess());
        Exception exp = null;
        try {
            applicationDto.setClusterId("not-exist-id");
            result = applicationDeployService.searchApplication(applicationDto);
        }catch (Exception e){
            exp = e;
        }
        assertTrue(exp instanceof IllegalArgumentException);
    }
}
