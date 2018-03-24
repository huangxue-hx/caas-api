package com.harmonycloud.dao.application;

import com.harmonycloud.dao.application.bean.ServiceTemplates;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by root on 3/29/17.
 * @Modified 增加projectId
 */
public interface ServiceTemplatesMapper {

    int insert(ServiceTemplates record);

    List<ServiceTemplates> listServiceByTenant(@Param("name") String name, @Param("tenant") String tenant, @Param("projectId") String projectId);
    
    List<ServiceTemplates> listNameByProjectId(@Param("name") String name, @Param("clusterId") String clusterId, @Param("isPublic") boolean isPublic, @Param("projectId") String projectId);

    List<ServiceTemplates> listServiceByImage(@Param("name") String name, @Param("image") String image, @Param("tenant") String tenant, @Param("isPublic") boolean isPublic);
    
    List<ServiceTemplates> listNameByImage(@Param("name") String name, @Param("image") String image, @Param("tenant") String tenant, @Param("projectId") String projectId);
    
    List<ServiceTemplates> listServiceLikeImage(@Param("name") String name, @Param("image") String image, @Param("tenant") String tenant, @Param("projectId") String projectId);
    
    List<ServiceTemplates> listServiceMaxTagByName(String name);

    List<ServiceTemplates> listServiceTemplate(@Param("name") String name, @Param("tag") String tag);

    ServiceTemplates getSpecificService(@Param("name") String name, @Param("tag") String tag, @Param("clusterId") String clusterId, @Param("projectId") String projectId);

//    void updateByNameTag(ServiceTemplates record);

    List<ServiceTemplates> listIDListByTemplateName(String name);

    int deleteByName(String name);

    List<String> listTenantByName(String name);

    ServiceTemplates getServiceTemplatesByID(@Param("ID") Integer id);
    
    List<ServiceTemplates> listSearchByImage(@Param("image") String image, @Param("clusterId") String clusterId, @Param("isPublic") boolean isPublic, @Param("projectId") String projectId);
    
    List<ServiceTemplates> listSearchByName(@Param("name") String name, @Param("clusterId") String clusterId, @Param("isPublic") boolean isPublic, @Param("projectId") String projectId);
    
    void deleteByProjects(@Param("projectIds")String [] projectIds);

    int deleteByClusterId(@Param("clusterId")String clusterId);
    
    List<ServiceTemplates> listByTemplateName(@Param("name") String name, @Param("projectId") String projectId);
    
    void updateServiceTemplate(ServiceTemplates ServiceTemplates);

    void deleteById(@Param("id") int id);
    
    /**
     * 公有模板查询*/
    List<ServiceTemplates> listPublicSearchByName(@Param("name") String name, @Param("isPublic") boolean isPublic);
    
    List<ServiceTemplates> listPublicSearchByImage(@Param("image") String image, @Param("isPublic") boolean isPublic);
    
    List<ServiceTemplates> listPublicNameByTenant(@Param("name") String name, @Param("isPublic") boolean isPublic);
    
    void updateServiceTemplatePublic(@Param("name") String name, @Param("isPublic") boolean isPublic);
}
