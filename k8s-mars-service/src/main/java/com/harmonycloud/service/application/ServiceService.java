package com.harmonycloud.service.application;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.application.bean.ServiceTemplates;
import com.harmonycloud.dto.application.DeployedServiceNamesDto;
import com.harmonycloud.dto.application.ServiceDeployDto;
import com.harmonycloud.dto.application.ServiceTemplateDto;
import com.harmonycloud.k8s.bean.cluster.Cluster;

import java.util.Map;

/**
 * Created by root on 3/29/17.
 */
public interface ServiceService {
    /**
     * create service template
     * @return
     * @throws Exception
     */
    public ActionReturnUtil saveServiceTemplate(ServiceTemplateDto serviceTemplate, String username, int type) throws Exception;

    public ActionReturnUtil listServiceTemplate(String name, String clusterId, boolean isPublic, String projectId) throws Exception;

    /**
     * 根据镜像查询服务模板
     * @param name
     * @param tenant
     * @param image
     * @param projectId
     * @return
     * @throws Exception
     */
    public ActionReturnUtil listTemplateByImage(String name, String tenant, String image, String projectId) throws Exception;

    public ActionReturnUtil updateServiceTemplate(ServiceTemplateDto serviceTemplate, String username) throws Exception;

    public ActionReturnUtil deleteServiceTemplate(String name, String userName) throws Exception;

    public ServiceTemplates getSpecificTemplate(String name, String tag, String clusterId, String projectId) throws Exception;

    public ActionReturnUtil deleteDeployedService(DeployedServiceNamesDto deployedServiceNamesDto, String userName) throws Exception;
    
    public ActionReturnUtil listServiceTemplate(String searchKey, String searchValue, String clusterId, boolean isPublic, String projectId) throws Exception;
    
    public ActionReturnUtil deleteServiceByNamespace(String namespace) throws Exception;
    
    public ActionReturnUtil deployServiceByName(String app, String tenantId, String name,String clusterId, String namespace, String userName, String nodeSelector, String projectId) throws Exception;
    
    public ActionReturnUtil deployService(ServiceDeployDto serviceDeploy, String userName) throws Exception;
    
    public ActionReturnUtil listTemplateTagsByName(String name, String tenant, String projectId) throws Exception;
    
    public ActionReturnUtil delById(int id)throws Exception;
    
    public ActionReturnUtil switchPub(String name, boolean isPublic) throws Exception;

    ServiceTemplateDto getServiceTemplateDtoByServiceTemplate(ServiceTemplates serviceTemplate, String app, String name, String tag, String namespace, String projectId);

    ActionReturnUtil checkService(ServiceTemplateDto service, Cluster cluster, String namespace) throws Exception;

    int deleteTemplateByClusterId(String clusterId);

    /**
     *  发布模板时检测资源是否满足
     * @param projectId
     * @param namespace
     * @param name
     * @return
     * @throws Exception
     */
    ActionReturnUtil checkResourceQuota(String projectId, String namespace, String name) throws Exception;

    /**
     * 获取服务模板所需要的资源
     * @param serviceTemplates
     * @return
     * @throws Exception
     */
    Map<String, Long> getServiceRequireResource(ServiceTemplates serviceTemplates) throws Exception;

    /**
     * 校验服务模板是否重名
     * @param name
     * @return
     * @throws Exception
     */
    ActionReturnUtil checkServiceTemplateName(String name) throws Exception;
}
