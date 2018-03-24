package com.harmonycloud.service.application.impl;

import com.harmonycloud.dao.application.ApplicationServiceMapper;
import com.harmonycloud.dao.application.bean.ApplicationService;
import com.harmonycloud.service.application.ApplicationTemplateService;
import com.harmonycloud.service.application.ApplicationWithServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author jiangmi
 * @Description
 * @Date created in 2017-12-25
 * @Modified
 */
@Service
public class ApplicationWithServiceServiceImpl implements ApplicationWithServiceService{

    @Autowired
    private ApplicationServiceMapper applicationServiceMapper;

    @Override
    public void insert(ApplicationService record) throws Exception {
        applicationServiceMapper.insert(record);
    }

    @Override
    public int selectByIdList(List<Integer> idList) throws Exception {
        int number = applicationServiceMapper.selectByIdList(idList);
        return number;
    }

    @Override
    public void deleteApplicationService(String name) throws Exception {
        applicationServiceMapper.deleteApplicationService(name);
    }

    @Override
    public List<ApplicationService> listApplicationServiceByAppTemplatesId(int applicationId) throws Exception {
        List<ApplicationService> appServices = applicationServiceMapper.listApplicationServiceByAppTemplatesId(applicationId);
        return appServices;
    }

    @Override
    public List<ApplicationService> selectAppServiceByAppId(int applicationId, int serviceId) throws Exception {
        List<ApplicationService> appServices = applicationServiceMapper.selectAppServiceByAppId(applicationId, serviceId);
        return appServices;
    }

    @Override
    public void deleteByProjectIds(String[] projectIds) throws Exception {
        applicationServiceMapper.deleteByProjectIds(projectIds);
    }

    @Override
    public void deleteApplicationServiceByAppTemplateId(int applicationId) throws Exception {
        applicationServiceMapper.deleteApplicationServiceByAppTemplateId(applicationId);
    }
}
