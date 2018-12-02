package com.harmonycloud.service.application;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.application.ApplicationTemplateDto;

import java.util.Map;

/**
 * Created by root on 3/29/17.
 */
public interface ApplicationService {

    /**
     * create a application template service on 17/04/07.
     * 
     * @author gurongyun
     * @param appTemplate
     *            required
     * @param userName
     * 
     * @return ActionReturnUtil
     */
    ActionReturnUtil saveApplicationTemplate(ApplicationTemplateDto appTemplate, String userName) throws Exception;
    
    /**
     * update a application template service on 17/04/07.
     * 
     * @author gurongyun
     * @param appTemplate
     *            required
     * @param userName
     * 
     * @return ActionReturnUtil
     */
    ActionReturnUtil updateApplicationTemplate(ApplicationTemplateDto appTemplate, String userName) throws Exception;

    /**
     * get application template by name and iamge and tenant service on 17/04/07.
     * 
     * @author gurongyun
     * 
     * @param name
     * 
     * @param tag
     *
     * @param clusterId
     *
     * @param projectId
     * @return ActionReturnUtil
     */
    ActionReturnUtil getApplicationTemplate(String name, String tag, String clusterId, String projectId) throws Exception;

    /**
     * get application template by name or image and projectId service on 17/04/07.
     * 
     * @author gurongyun
     * 
     * @param searchKey
     * 
     * @param searchValue
     * 
     * @param projectId
     * @param isPublic 模板是否共有
     * @return ActionReturnUtil
     */
    ActionReturnUtil listApplicationTemplate(String searchKey, String searchValue, boolean isPublic, String projectId, String clusterId) throws Exception;


    ActionReturnUtil getApplicationTemplateYaml(ApplicationTemplateDto appTemplate) throws Exception;


    /**
     * delete application template by name and tenant service on 17/04/07.
     * 
     * @author gurongyun
     * 
     * @param name
     * 
     * @return ActionReturnUtil
     */
    ActionReturnUtil deleteApplicationTemplate(String name, String projectId, String clusterId) throws Exception;
    
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
    ActionReturnUtil getApplicationTemplateByName(String name, String tenant, boolean isPublic, String projectId) throws Exception;
    
    /**
     * add application template by name  on 17/04/07.
     * 
     * @author gurongyun
     * 
     * @param appTemplate
     * 
     * @param userName
     * 
     * @return ActionReturnUtil
     */
    ActionReturnUtil addServiceTemplateByName(ApplicationTemplateDto appTemplate, String userName) throws Exception;
    
    /**
     * update application template by name  on 17/04/07.
     * 
     * @author gurongyun
     * 
     * @param appTemplate
     * 
     * @param userName
     * 
     * @return ActionReturnUtil
     */
    ActionReturnUtil updateServiceTemplateByName(ApplicationTemplateDto appTemplate, String userName) throws Exception;
    
    /**
     * 获取应用商店  on 17/04/07.
     * 
     * @author gurongyun
     * 
     * @return ActionReturnUtil
     */
    ActionReturnUtil listServiceTemplatePublic() throws Exception;
    
    ActionReturnUtil switchPub(String name, boolean isPublic) throws Exception;

    /**
     * 删除项目下的应用模板和服务模板
     * @param projectId
     * @throws Exception
     */
    void deleteTemplatesInProject(String projectId) throws Exception;

    /**
     * 获取应用模板内所需要的cpu和内存
     * @param name
     * @param tag
     * @param clusterId
     * @param projectId
     * @return Map<String, Long>
     * @throws Exception
     */
    Map<String, Long> getAppTemplateResource(String name, String tag, String clusterId, String projectId) throws Exception;

    /**
     * 检查应用模板名称是否重名
     * @param name
     * @return
     * @throws Exception
     */
    ActionReturnUtil checkAppTemplateName(String name, String projectId, String clusterId) throws Exception;

    String convertYaml(String yaml);
}
