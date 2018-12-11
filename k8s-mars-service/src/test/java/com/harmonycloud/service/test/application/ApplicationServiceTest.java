package com.harmonycloud.service.test.application;

import com.harmonycloud.dto.application.ApplicationTemplateDto;
import com.harmonycloud.service.application.ApplicationService;
import com.harmonycloud.service.test.BaseTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

/**
 * @Author w_kyzhang
 * @Description
 * @Date 2018-12-3
 * @Modified
 */
public class ApplicationServiceTest extends BaseTest{
    @Autowired
    private ApplicationService appService;

    private String tag = "1.0";

    @Test
    public void testCreateApplicationTemplate() throws Exception {
        appService.saveApplicationTemplate(applicationTemplateDto, adminUserName);
        assertNotNull(appService.getApplicationTemplate(applicationTemplateDto.getName(), tag, devClusterId, projectId));
    }

    @Test
    public void testDeleteApplicationTemplate() throws Exception {
        appService.deleteApplicationTemplate(applicationTemplateDto.getName(), projectId, devClusterId);
        assertNull(appService.getApplicationTemplate(applicationTemplateDto.getName(), tag, devClusterId, projectId));
    }
}
