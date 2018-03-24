package com.harmonycloud.service.application;

import com.harmonycloud.dao.application.bean.ApplicationTemplates;

import java.util.List;

/**
 * @Author jiangmi
 * @Description 应用模板与数据库交互Service
 * @Date created in 2017-12-25
 * @Modified
 */
public interface ApplicationTemplateService {

    /**
     * 保存应用模板
     * @param record ApplicationTemplates
     *
     * @return null
     */
    void saveApplicationTemplates(ApplicationTemplates record) throws Exception;

    /**
     * 根据模板名称和项目Id获取模板列表
     * @param name
     * @param clusterId
     * @param isPublic
     * @param projectId
     * @return
     * @throws Exception
     */
    List<ApplicationTemplates> listApplicationTemplatesByName(String name, String clusterId, boolean isPublic, String projectId) throws Exception;

    /**
     * 根据应用模板名称获取最大版本
     * @param name 模板名称
     * @param tenant 租户
     * @param projectId 项目
     * @return List<ApplicationTemplates>
     * @throws Exception
     */
    List<ApplicationTemplates> listApplicationTempaltesMaxTagByName(String name, String tenant, String projectId) throws Exception;

    /**
     * 根据应用模板名称和版本获取模板
     * @param name 模板名称
     * @param tag 模板版本
     * @param tenant 模板所在租户
     * @param projectId 模板所在的项目Id
     * @return ApplicationTemplates
     * @throws Exception
     */
    ApplicationTemplates getApplicationTemplatesByNameAndTag(String name, String tag, String tenant, String projectId) throws Exception;

    /**
     * 删除应用模板
     * @param name 模板名称
     * @throws Exception
     */
    void deleteApplicationTemplate(String name) throws Exception;

    /**
     * 根据项目Id获取模板列表
     *
     * @param clusterId 集群
     * @param projectId 项目Id
     * @return List<ApplicationTemplates>
     * @throws Exception
     */
    List<ApplicationTemplates> listNameByProjectId(String projectId, String clusterId) throws Exception;

    /**
     * 根据模板名称查询模板
     * @param name 模板名称
     * @return ApplicationTemplates
     * @throws Exception
     */
    ApplicationTemplates getApplicationTemplatesByName(String name) throws Exception;

    /**
     * 模糊查询应用模板
     * @param name 模板名称模糊字符串
     * @param clusterId 集群
     * @param projectId 项目Id
     * @return List<ApplicationTemplates>
     * @throws Exception
     */
    List<ApplicationTemplates> listNameByName(String name, String clusterId,String projectId) throws Exception;

    /**
     * find app Templates like by image  on 17/04/07.
     *
     * @author gurongyun
     *
     * @param imageList
     *
     * @param tenant
     *
     * @param projectId
     *
     * @description find a app Templates like
     */
    List<ApplicationTemplates> listNameByImage(String imageList, String tenant, String projectId) throws Exception;

    /**
     * find app Templates like by name and image
     * @param name
     * @param imageList
     * @param tenant
     * @param projectId
     * @return
     */
    List<ApplicationTemplates> listApplicationTemplatesByNameAndImage(String name, String imageList, String tenant, String projectId) throws Exception;

    /**
     * 共有模板查询
     * */
    List<ApplicationTemplates> listPublicTemplate() throws Exception;

    /**
     * 共有模板查询
     * find app Templates like by name  on 17/04/07.
     *
     * @author gurongyun
     *
     * @param name
     *
     * @description find a app Templates like
     */
    List<ApplicationTemplates> listPublicNameByName(String name) throws Exception;

    /**
     * 公有模板
     * find app Templates like by image  on 17/04/07.
     *
     * @author gurongyun
     *
     * @param imageList
     *
     * @description find a app Templates like
     */
    List<ApplicationTemplates> listPublicNameByImage(String imageList) throws Exception;

    /**
     *
     * @param name
     * @param isPublic
     * @throws Exception
     */
    void updateApplicationTemplatePublic(String name,  boolean isPublic) throws Exception;

    /**
     * 根据应用模板Id更新模板内镜像信息
     * @param image 镜像
     * @param appTemId 模板
     * @throws Exception
     */
    void updateImageById(String image,Integer appTemId) throws Exception;

    /**
     * 根据项目id删除应用模板
     * @param projectIds
     * @throws Exception
     */
    void deleteByProjectIds(String [] projectIds) throws Exception;

    /**
     * 根据集群id删除应用模板
     * @param clusterId
     * @throws Exception
     */
    int deleteByClusterId(String clusterId) throws Exception;

    /**
     * 更新应用模板
     * @param appTemplate
     * @throws Exception
     */
    void updateApplicationTemplate(ApplicationTemplates appTemplate) throws Exception;

    /**
     * 获取公共应用模板
     * @return List<ApplicationTemplates>
     * @throws Exception
     */
    List<ApplicationTemplates> listPublic() throws Exception;

    /**
     * 更新应用模板的部署状态
     * @param bid
     * @throws Exception
     */
    void updateDeployById(Integer bid) throws Exception;

    /**
     * check app Templates name  on 17/04/07.
     *
     * @author gurongyun
     *
     * @param name
     *
     * @description check app Templates name
     */
    ApplicationTemplates getApplicationTemplatesByNameAndProjectId(String name, String projectId) throws Exception;

    /**
     * 根据分区id查询应用模板
     * @param namespaceId
     * @return
     * @throws Exception
     */
    ApplicationTemplates selectByNamespaceId(String namespaceId) throws Exception;
}
