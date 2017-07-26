package com.harmonycloud.service.application;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.dto.business.DeployedServiceNamesDto;
import com.harmonycloud.dto.business.ServiceDeployDto;
import com.harmonycloud.dto.business.ServiceTemplateDto;

/**
 * Created by root on 3/29/17.
 */
public interface ServiceService {
    /**
     * create service template
     * @return
     * @throws Exception
     */
    public ActionReturnUtil saveServiceTemplate(ServiceTemplateDto serviceTemplate, String username) throws Exception;

    public ActionReturnUtil listTemplateByTenat(String name, String tenat) throws Exception;

    public ActionReturnUtil listTemplateByImage(String name, String tenant, String image) throws Exception;

    public ActionReturnUtil updateServiceTemplata(ServiceTemplateDto serviceTemplate, String username, String tag) throws Exception;

    public ActionReturnUtil deleteServiceTemplate(String name, String userName) throws Exception;

    public ActionReturnUtil getSpecificTemplate(String name, String tag) throws Exception;

    public ActionReturnUtil deleteDeployedService(DeployedServiceNamesDto deployedServiceNamesDto, String userName, Cluster cluster) throws Exception;
    
    public ActionReturnUtil listServiceTemplate(String searchKey, String searchvalue, String tenant) throws Exception;
    
    public ActionReturnUtil deleteServiceByNamespace(String namespace) throws Exception;
    
    public ActionReturnUtil deleteServiceByTenant(String [] tenant) throws Exception;
    
    public com.harmonycloud.dao.application.bean.Service getServiceByname(String name, String namespace) throws Exception;
    
    public ActionReturnUtil updateServicePvcByname(String name,String pvc, String namespace) throws Exception;
    
    public ActionReturnUtil deployServiceByname(String name,String tag, String namespace, Cluster cluster, String userName) throws Exception;
    
    public ActionReturnUtil deployService(ServiceDeployDto serviceDeploy, Cluster cluster, String userName) throws Exception;
    
    public ActionReturnUtil listTemplateTagsByName(String name, String tenant) throws Exception;
}
