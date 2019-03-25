package com.harmonycloud.service.test.application;

import com.harmonycloud.dao.application.bean.ServiceTemplates;
import com.harmonycloud.dto.application.ServiceTemplateDto;
import com.harmonycloud.service.application.ServiceService;
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
public class ServiceServiceTest extends BaseTest {
    @Autowired
    private ServiceService serviceService;

    private ServiceTemplateDto serviceTemplateDto = serviceDeployDto.getServiceTemplate();

    @Test
    public void testCreateServiceTemplate() throws Exception {

        serviceTemplateDto.setTag("testTag");
        serviceService.saveServiceTemplate(serviceTemplateDto, adminUserName, serviceTemplateDto.getType());
        ServiceTemplates serviceTemplates = serviceService.getSpecificTemplate(serviceTemplateDto.getName(), serviceTemplateDto.getTag(), serviceTemplateDto.getClusterId(), serviceTemplateDto.getProjectId());
        assertNotNull(serviceTemplates);
    }

    @Test
    public void testDeleteServiceTemplate() throws Exception {
        serviceService.deleteServiceTemplate(serviceTemplateDto.getName(), adminUserName, serviceTemplateDto.getProjectId(), serviceTemplateDto.getClusterId());
        ServiceTemplates serviceTemplates = serviceService.getSpecificTemplate(serviceTemplateDto.getName(), serviceTemplateDto.getTag(), serviceTemplateDto.getClusterId(), serviceTemplateDto.getProjectId());
        assertNull(serviceTemplates);
    }

}
