package com.harmonycloud.dao.application;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.harmonycloud.dao.application.bean.BusinessTemplates;

public interface BusinessTemplatesMapper {


    int saveBusinessTemplates(BusinessTemplates record);
    
    /**
     * find Max Tag Business Templates  on 17/04/07.
     * 
     * @author gurongyun
     * 
     * @param name 
     * 
     * @description find Max Tag Business Templates
     */
    List<BusinessTemplates> listBusinessTempaltesMaxTagByName(String name);
    
    /**
     * find a Business Templates  on 17/04/07.
     * 
     * @author gurongyun
     * @param name 
     * 
     * @param tag 
     * 
     * @description find a Business Templates
     */
    BusinessTemplates getBusinessTemplatesByNameAndTag(@Param("name")String name,@Param("tag")String tag);
    
    /**
     * delete Business Templates  on 17/04/07.
     * 
     * @author gurongyun
     * 
     * @param name
     * 
     * @description delete Business Templates
     */
    void deleteBusinessTemplate(@Param("name")String name);
    
    /**
     * find Business Templates  on 17/04/07.
     * 
     * @author gurongyun
     * 
     * @param tenant
     * 
     * @description find a Business Templates
     */
    List<BusinessTemplates> listNameByTenant(@Param("tenant")String tenant);
    
    /**
     * find Business Templates  on 17/04/07.
     * 
     * @author gurongyun
     * 
     * @param name 
     * 
     * @param tenant 
     * 
     * @description find a Business Templates
     */
    List<BusinessTemplates> listBusinessTemplatesByName(@Param("name")String name,@Param("tenant")String tenant);
    
    /**
     * check Business Templates name  on 17/04/07.
     * 
     * @author gurongyun
     * 
     * @param name 
     * 
     * @description check Business Templates name
     */
    BusinessTemplates getBusinessTemplatesByName(@Param("name")String name);
    
    /**
     * find Business Templates like by name  on 17/04/07.
     * 
     * @author gurongyun
     * 
     * @param name
     * 
     * @param tenant
     * 
     * @description find a Business Templates like
     */
    List<BusinessTemplates> listNameByName(@Param("name")String name,@Param("tenant")String tenant);

    /**
     * find Business Templates like by image  on 17/04/07.
     * 
     * @author gurongyun
     * 
     * @param imageList 
     * 
     * @param tenant
     * 
     * @description find a Business Templates like
     */
    List<BusinessTemplates> listNameByImage(@Param("imageList")String imageList,@Param("tenant")String tenant);
    
    /**
     * find Business Templates like by name and image  on 17/04/07.
     * 
     * @author gurongyun
     * 
     * @param imagelist 
     * 
     * @param tenant
     * 
     * @description find a Business Templates like
     */
    List<BusinessTemplates> listBusinessTemplatesByNameAndImage(@Param("name")String name,@Param("imageList")String imagelist,@Param("tenant")String tenant);
    
    /**
     * update Business Templates image  on 17/04/07.
     * 
     * @author gurongyun
     * 
     * @description find Max Tag Business Templates
     */
    void updateImageById(String image,Integer bid);

    void updateDeployById(Integer bid);
    
    /**
     * find a Business Templates on 17/04/07.
     * 
     * @author gurongyun
     * 
     * @param id 
     * 
     * @description check Business Templates name
     */
    BusinessTemplates selectById(@Param("id")Integer id);
    
    void deleteByTenant(@Param("tenant")String [] tenant);
    
    void updateBusinessTemplate(BusinessTemplates businessTemplate);
    
    List<BusinessTemplates> listPublic();
    
}