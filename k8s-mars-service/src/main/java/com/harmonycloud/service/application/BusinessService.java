package com.harmonycloud.service.application;

import java.util.List;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.business.BusinessTemplateDto;
import com.harmonycloud.dto.business.TopologysDto;

/**
 * Created by root on 3/29/17.
 */
public interface BusinessService {

    /**
     * create a application template service on 17/04/07.
     * 
     * @author gurongyun
     * @param businessTemplate
     *            required
     * @param userName
     * 
     * @return ActionReturnUtil
     */
    ActionReturnUtil saveBusinessTemplate(BusinessTemplateDto businessTemplate, String userName) throws Exception;
    
    /**
     * update a application template service on 17/04/07.
     * 
     * @author gurongyun
     * @param businessTemplate
     *            required
     * @param userName
     * 
     * @return ActionReturnUtil
     */
    ActionReturnUtil updateBusinessTemplate(BusinessTemplateDto businessTemplate, String userName) throws Exception;

    /**
     * get application template by name and iamge and tenant service on 17/04/07.
     * 
     * @author gurongyun
     * 
     * @param name
     * 
     * @param tag
     * 
     * @return ActionReturnUtil
     */
    ActionReturnUtil getBusinessTemplate(String name, String tag) throws Exception;

    /**
     * get application template by name or iamge and tenant service on 17/04/07.
     * 
     * @author gurongyun
     * 
     * @param searchKey
     * 
     * @param searchValue
     * 
     * @param tenant
     * 
     * @return ActionReturnUtil
     */
    ActionReturnUtil listBusinessTemplateByTenant(String searchKey, String searchValue, String tenant) throws Exception;


    ActionReturnUtil getBusinessTemplateYaml(BusinessTemplateDto businessTemplate) throws Exception;


    /**
     * delete application template by name and tenant service on 17/04/07.
     * 
     * @author gurongyun
     * 
     * @param name
     * 
     * @param tenant
     * 
     * @return ActionReturnUtil
     */
    ActionReturnUtil deleteBusinessTemplate(String name) throws Exception;

    /**
     * delete application template by name and tenant service on 17/04/07.
     * 
     * @author gurongyun
     * 
     * @param name
     * 
     * @param tenant
     * 
     * @return ActionReturnUtil
     */
    ActionReturnUtil deleteBusinessTemplateByTenant(String[] tenant) throws Exception;
    
    /**
     * save topology on 17/04/07.
     * 
     * @param topologys
     *            re
     */
    boolean saveTopology(List<TopologysDto> topologys, Integer businessTemplatesId) throws Exception;
    
    /**
     * get application template by name  on 17/04/07.
     * 
     * @author gurongyun
     * 
     * @param name
     * 
     * @param tenant
     * 
     * @return ActionReturnUtil
     */
    ActionReturnUtil getBusinessTemplateByName(String name, String tenant) throws Exception;
    
    /**
     * add application template by name  on 17/04/07.
     * 
     * @author gurongyun
     * 
     * @param name
     * 
     * @param tenant
     * 
     * @return ActionReturnUtil
     */
    ActionReturnUtil addServiceTemplateByName(BusinessTemplateDto businessTemplate, String userName) throws Exception;
    
    /**
     * update application template by name  on 17/04/07.
     * 
     * @author gurongyun
     * 
     * @param name
     * 
     * @param tenant
     * 
     * @return ActionReturnUtil
     */
    ActionReturnUtil updateServiceTemplateByName(BusinessTemplateDto businessTemplate, String userName) throws Exception;
}
