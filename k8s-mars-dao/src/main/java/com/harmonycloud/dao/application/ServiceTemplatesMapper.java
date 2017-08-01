package com.harmonycloud.dao.application;

import com.harmonycloud.dao.application.bean.ServiceTemplates;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by root on 3/29/17.
 */
public interface ServiceTemplatesMapper {

    int insert(ServiceTemplates record);

    List<ServiceTemplates> listServiceByTenant(@Param("name") String name, @Param("tenant") String tenant);
    
    List<ServiceTemplates> listNameByTenant(@Param("name") String name, @Param("tenant") String tenant);

    List<ServiceTemplates> listServiceByImage(@Param("name") String name, @Param("image") String image, @Param("tenant") String tenant);
    
    List<ServiceTemplates> listNameByImage(@Param("name") String name, @Param("image") String image, @Param("tenant") String tenant);
    

    List<ServiceTemplates> listServiceMaxTagByName(String name);

    List<ServiceTemplates> listServiceTemplate(@Param("name") String name, @Param("tag") String tag);

    ServiceTemplates getSpecificService(@Param("name") String name, @Param("tag") String tag);

//    void updateByNameTag(ServiceTemplates record);

    List<ServiceTemplates> listIDListByTemplateName(String name);

    int deleteByName(String name);

    List<String> listTenantByName(String name);

    ServiceTemplates getServiceTemplatesByID(@Param("ID") Integer id);

    ServiceTemplates getExternalService(@Param("name") String name);
    
    List<ServiceTemplates> listSearchByImage(@Param("image") String image, @Param("tenant") String tenant);
    
    List<ServiceTemplates> listSearchByName(@Param("name") String name, @Param("tenant") String tenant);
    
    void deleteByTenant(@Param("tenant") String[] tenant);
    
    List<ServiceTemplates> listByTemplateName(@Param("name") String name, @Param("tenant") String tenant);
    
    void updateServiceTemplate(ServiceTemplates ServiceTemplates);

    void deleteById(@Param("id") int id);
}
