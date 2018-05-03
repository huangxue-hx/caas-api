package com.harmonycloud.service.application.impl;

import com.harmonycloud.dao.application.ApplicationTemplatesMapper;
import com.harmonycloud.dao.application.bean.ApplicationTemplates;
import com.harmonycloud.service.application.ApplicationTemplateService;
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
public class ApplicationTemplateServiceImpl implements ApplicationTemplateService{

    @Autowired
    private ApplicationTemplatesMapper applicationTemplatesMapper;

    @Override
    public void saveApplicationTemplates(ApplicationTemplates record) throws Exception {
        applicationTemplatesMapper.saveApplicationTemplates(record);
    }

    @Override
    public List<ApplicationTemplates> listApplicationTempaltesMaxTagByName(String name, String clusterId, String projectId) throws Exception {
        List<ApplicationTemplates> applicationTemplates = applicationTemplatesMapper.listAppTempaltesMaxTagByName(name, clusterId, projectId);
        return applicationTemplates;
    }

    @Override
    public ApplicationTemplates getApplicationTemplatesByNameAndTag(String name, String tag, String clusterId, String projectId) throws Exception {
        ApplicationTemplates applicationTemplate = applicationTemplatesMapper.getAppTemplatesByNameAndTag(name, tag, clusterId, projectId);
        return applicationTemplate;
    }

    @Override
    public void deleteApplicationTemplate(String name, String projectId, String clusterId) throws Exception {
        applicationTemplatesMapper.deleteAppTemplate(name, projectId, clusterId);
    }

    @Override
    public List<ApplicationTemplates> listNameByProjectId(String projectId, String clusterId) throws Exception {
        List<ApplicationTemplates> applicationTemplates = applicationTemplatesMapper.listNameByProjectId(projectId, clusterId);
        return applicationTemplates;
    }

    @Override
    public List<ApplicationTemplates> listApplicationTemplatesByName(String name, String clusterId, boolean isPublic, String projectId) throws Exception {
        List<ApplicationTemplates> applicationTemplates = applicationTemplatesMapper.listAppTemplatesByName(name, clusterId, isPublic, projectId);
        return applicationTemplates;
    }

    @Override
    public ApplicationTemplates getApplicationTemplatesByName(String name) throws Exception {
        return null;
    }

    @Override
    public List<ApplicationTemplates> listNameByName(String name, String clusterId, String projectId) throws Exception {
        List<ApplicationTemplates> applicationTemplates = applicationTemplatesMapper.listNameByName(name, clusterId, projectId);
        return applicationTemplates;
    }

    @Override
    public List<ApplicationTemplates> listNameByImage(String imageList, String clusterId, String projectId) throws Exception {
        List<ApplicationTemplates> applicationTemplates = applicationTemplatesMapper.listNameByImage(imageList, clusterId, projectId);
        return applicationTemplates;
    }

    @Override
    public List<ApplicationTemplates> listApplicationTemplatesByNameAndImage(String name, String imageList, String tenant, String projectId) throws Exception{
        List<ApplicationTemplates> applicationTemplates = applicationTemplatesMapper.listAppTemplatesByNameAndImage(name, imageList, tenant, projectId);
        return applicationTemplates;
    }

    @Override
    public List<ApplicationTemplates> listPublicTemplate() throws Exception {
        List<ApplicationTemplates> applicationTemplates = applicationTemplatesMapper.listPublicTemplate();
        return applicationTemplates;
    }

    @Override
    public List<ApplicationTemplates> listPublicNameByName(String name) throws Exception {
        List<ApplicationTemplates> applicationTemplates = applicationTemplatesMapper.listPublicNameByName(name);
        return applicationTemplates;
    }

    @Override
    public List<ApplicationTemplates> listPublicNameByImage(String imageList) throws Exception {
        List<ApplicationTemplates> applicationTemplates = applicationTemplatesMapper.listPublicNameByImage(imageList);
        return applicationTemplates;
    }

    @Override
    public void updateApplicationTemplatePublic(String name, boolean isPublic) throws Exception {
        applicationTemplatesMapper.updateAppTemplatePublic(name, isPublic);
    }

    @Override
    public void updateImageById(String image, Integer appTemId) throws Exception {
        applicationTemplatesMapper.updateImageById(image, appTemId);
    }

    @Override
    public void deleteByProjectIds(String[] projectIds) throws Exception {
        applicationTemplatesMapper.deleteByProjectIds(projectIds);
    }

    @Override
    public int deleteByClusterId(String clusterId) throws Exception {
        return applicationTemplatesMapper.deleteByClusterId(clusterId);
    }

    @Override
    public void updateApplicationTemplate(ApplicationTemplates appTemplate) throws Exception {
        applicationTemplatesMapper.updateApplicationTemplate(appTemplate);
    }

    @Override
    public List<ApplicationTemplates> listPublic() throws Exception {
        List<ApplicationTemplates> applicationTemplates = applicationTemplatesMapper.listPublic();
        return applicationTemplates;
    }

    @Override
    public void updateDeployById(Integer appTemId) throws Exception {
        applicationTemplatesMapper.updateDeployById(appTemId);
    }

    @Override
    public ApplicationTemplates getApplicationTemplatesByNameAndProjectId(String name, String projectId) throws Exception {
        ApplicationTemplates applicationTemplate = applicationTemplatesMapper.getAppTemplatesByNameAndProjectId(name, projectId);
        return applicationTemplate;
    }

    @Override
    public ApplicationTemplates selectByNamespaceId(String namespaceId) throws Exception {
        ApplicationTemplates applicationTemplate = applicationTemplatesMapper.selectByNamespaceId(namespaceId);
        return applicationTemplate;
    }

    @Override
    public void deleteAppTemplateById(Integer id) throws Exception {
        applicationTemplatesMapper.deleteApplicationTemplatesById(id);
    }
}
