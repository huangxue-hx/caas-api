package com.harmonycloud.dao.application;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.harmonycloud.dao.application.bean.ApplicationTemplates;

/**
 * @Description 应用模板数据库交互
 * @Date modified at 2017/12/25
 * @Modified 增加projectId
 */
public interface ApplicationTemplatesMapper {


    int saveApplicationTemplates(ApplicationTemplates record);
    
    /**
     * find Max Tag app Templates  on 17/04/07.
     * 
     * @author gurongyun
     * 
     * @param name 
     * 
     * @description find Max Tag app Templates
     */
    List<ApplicationTemplates> listAppTempaltesMaxTagByName(@Param("name")String name, @Param("clusterId")String clusterId, @Param("projectId")String projectId);
    
    /**
     * find a app Templates  on 17/04/07.
     * 
     * @author gurongyun
     * @param name 
     * 
     * @param tag 
     * 
     * @description find a app Templates
     */
    ApplicationTemplates getAppTemplatesByNameAndTag(@Param("name")String name, @Param("tag")String tag, @Param("clusterId")String clusterId, @Param("projectId")String projectId);
    
    /**
     * delete app Templates  on 17/04/07.
     * 
     * @author gurongyun
     * 
     * @param name
     * 
     * @description delete app Templates
     */
    void deleteAppTemplate(@Param("name")String name, @Param("projectId")String projectId, @Param("clusterId")String clusterId);
    
    /**
     * find app Templates  on 17/04/07.
     * 
     * @author gurongyun
     *
     * @param clusterId
     * @param projectId
     *
     * @description find a app Templates
     */
    List<ApplicationTemplates> listNameByProjectId(@Param("projectId")String projectId, @Param("clusterId")String clusterId);
    
    /**
     * find app Templates  on 17/04/07.
     * 
     * @author gurongyun
     * 
     * @param name 
     * 
     * @param projectId
     * 
     * @description find a app Templates
     */
    List<ApplicationTemplates> listAppTemplatesByName(@Param("name")String name, @Param("clusterId")String clusterId, @Param("isPublic")boolean isPublic, @Param("projectId")String projectId);
    
    /**
     * check app Templates name  on 17/04/07.
     * 
     * @author gurongyun
     * 
     * @param name 
     * 
     * @description check app Templates name
     */
    ApplicationTemplates getAppTemplatesByNameAndProjectId(@Param("name")String name, @Param("projectId")String projectId);

    /**
     * find app Templates like by name  on 17/04/07.
     * 
     * @author gurongyun
     * 
     * @param name
     * 
     * @param projectId
     * 
     * @description find a app Templates like
     */
    List<ApplicationTemplates> listNameByName(@Param("name")String name, @Param("clusterId")String clusterId, @Param("projectId")String projectId);

    /**
     * find app Templates like by image  on 17/04/07.
     * 
     * @author gurongyun
     * 
     * @param imageList 
     * 
     * @param projectId
     * 
     * @description find a app Templates like
     */
    List<ApplicationTemplates> listNameByImage(@Param("imageList")String imageList, @Param("clusterId")String clusterId, @Param("projectId")String projectId);
    
    /**
     * find app Templates like by name and image  on 17/04/07.
     * 
     * @author gurongyun
     * 
     * @param imageList
     * 
     * @param tenant
     * 
     * @description find a app Templates like
     */
    List<ApplicationTemplates> listAppTemplatesByNameAndImage(@Param("name")String name, @Param("imageList")String imageList, @Param("tenant")String tenant, @Param("projectId")String projectId);
    
    /**
     * update app Templates image  on 17/04/07.
     * 
     * @author gurongyun
     * 
     * @description find Max Tag app Templates
     */
    void updateImageById(String image,Integer bid);

    void updateDeployById(Integer bid);
    
    /**
     * find a app Templates on 17/04/07.
     * 
     * @author gurongyun
     * 
     * @param id 
     * 
     * @description check app Templates name
     */
    ApplicationTemplates selectById(@Param("id")Integer id);
    
    void deleteByProjectIds(@Param("projectIds")String [] projectIds);

    int deleteByClusterId(@Param("clusterId")String clusterId);

    void updateApplicationTemplate(ApplicationTemplates appTemplate);
    
    List<ApplicationTemplates> listPublic();
    
    /**
     * 共有模板查询
     * */
    List<ApplicationTemplates> listPublicTemplate();
    
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
    List<ApplicationTemplates> listPublicNameByName(@Param("name")String name);
    
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
    List<ApplicationTemplates> listPublicNameByImage(@Param("imageList")String imageList);
    
    void updateAppTemplatePublic(@Param("name")String name,  @Param("isPublic")boolean isPublic);

    ApplicationTemplates selectByNamespaceId(@Param("namespaceId") String namespaceId);

    void deleteApplicationTemplatesById(@Param("id")Integer id);
}