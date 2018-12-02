package com.harmonycloud.api.application;

import com.harmonycloud.common.exception.K8sAuthException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.application.ApplicationTemplateDto;
import com.harmonycloud.k8s.constant.Constant;
import com.harmonycloud.service.application.ApplicationDeployService;
import com.harmonycloud.service.application.ApplicationService;
import com.harmonycloud.service.tenant.TenantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.ws.rs.Path;

/**
 * Created by root on 3/29/17.
 */
@RequestMapping("/tenants")
@Controller
public class ApplicationController {

    @Autowired
    private HttpSession session;

    @Autowired
    private ApplicationService appService;
    
    @Autowired
    private TenantService tenantService;

    @Autowired
    private ApplicationDeployService appDeployService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * create application template on 17/05/05.
     * 
     * @param appTemplate
     * 
     * @return ActionReturnUtil
     */
    @ResponseBody
    @RequestMapping(value = "/{tenantId}/projects/{projectId}/apptemplates", method = RequestMethod.POST)
    public ActionReturnUtil saveApplicationTemplate(@ModelAttribute ApplicationTemplateDto appTemplate) throws Exception {
        logger.info("create application template");
        String userName = (String) session.getAttribute("username");
        return appService.saveApplicationTemplate(appTemplate, userName);
    }


    /**
     * 创建应用模板时获取对应的yaml格式
     *
     * @param appTemplate
     *
     * @return ActionReturnUtil
     */
    @ResponseBody
    @RequestMapping(value = "/{tenantId}/projects/{projectId}/apptemplates/{templateName}/yaml", method = RequestMethod.POST)
    public ActionReturnUtil getApplicationTemplateYaml(@ModelAttribute ApplicationTemplateDto appTemplate) throws Exception {
        return appService.getApplicationTemplateYaml(appTemplate);
    }

    /**
     * update application template on 17/05/05.
     * 
     * @param appTemplate
     * 
     * @return ActionReturnUtil
     */
    @ResponseBody
    @RequestMapping(value = "/{tenantId}/projects/{projectId}/apptemplates/{templateName}", method = RequestMethod.PUT)
    public ActionReturnUtil updateApplicationTemplate(@ModelAttribute ApplicationTemplateDto appTemplate) throws Exception {
        logger.info("update application template");
        String userName = (String) session.getAttribute("username");
        return appService.updateApplicationTemplate(appTemplate, userName);
    }

    /**
     * get application template by name or image and projectId on 17/05/05.
     * @Modified tenant -> projectId(从租户角度变成项目)
     *
     * @param searchKey 关键字查询key
     * @param searchValue 关键字查询value
     * @param projectId 项目Id
     * @param isPublic 模板是否共有
     * @param clusterId 租户
     * @return ActionReturnUtil
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/{tenantId}/projects/{projectId}/apptemplates", method = RequestMethod.GET)
    public ActionReturnUtil listApplicationTemplate(@RequestParam(value = "searchkey", required = false) String searchKey,
                                                 @RequestParam(value = "searchvalue", required = false) String searchValue,
                                                 @PathVariable(value = "projectId") String projectId,
                                                 @RequestParam(value = "isPublic", required = false) boolean isPublic,
                                                 @RequestParam(value = "clusterId", required = false) String clusterId) throws Exception {
        return appService.listApplicationTemplate(searchKey, searchValue, isPublic, projectId, clusterId);
    }

    /**
     * get application template by name and tag (and tenant) on 17/05/05 .
     *
     * @param name
     * @param tag
     * @param clusterId
     * @param projectId
     * @return ActionReturnUtil
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/{tenantId}/projects/{projectId}/apptemplates/{templateName}", method = RequestMethod.GET)
    public ActionReturnUtil getApplicationTemplate(@PathVariable(value = "templateName") String name,
                                                   @RequestParam(value = "tag", required = false) String tag,
                                                   @RequestParam(value = "clusterId", required = false) String clusterId,
                                                   @PathVariable(value = "projectId") String projectId) throws Exception {
        return appService.getApplicationTemplate(name, tag, clusterId, projectId);
    }

    /**
     * delete application template by name on 17/05/05.
     * 
     * @param name
     * 
     * @return ActionReturnUtil
     */
    @ResponseBody
    @RequestMapping(value = "/{tenantId}/projects/{projectId}/apptemplates/{templateName}", method = RequestMethod.DELETE)
    public ActionReturnUtil deleteApplicationTemplate(@PathVariable(value = "templateName") String name,
                                                      @PathVariable(value = "projectId") String projectId,
                                                      @RequestParam(value = "clusterId") String clusterId) throws Exception {
        logger.info("delete application template");
        return appService.deleteApplicationTemplate(name, projectId, clusterId);
    }

    /**
     * get application template by name and tag (and tenant) on 17/05/05 .  （暂未使用）
     * @param name 应用模板名称
     * @param clusterId 应用模板所属的集群
     * @param isPublic 应用模板是否共有
     * @param projectId 应用模板所属的项目id
     * @return ActionReturnUtil
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/{tenantId}/projects/{projectId}/apptemplates/{templateName}/tags", method = RequestMethod.GET)
    public ActionReturnUtil listApplicationTemplateTagsByName(@PathVariable(value = "templateName") String name,
                                                      @RequestParam(value = "clusterId", required = false) String clusterId,
                                                      @RequestParam(value = "isPublic", required = true) boolean isPublic,
                                                      @PathVariable(value = "projectId") String projectId) throws Exception {
        return appService.getApplicationTemplateByName(name, clusterId, isPublic, projectId);
    }
    
    /**
     *add service template to appTemplate on 17/05/05 .
     *  暂未使用
     * @param appTemplate
     * 
     * @return ActionReturnUtil
     */
    @ResponseBody
    @RequestMapping(value = "/{tenantId}/projects/{projectId}/apptemplates/{templateName}/addsvctemplate", method = RequestMethod.POST)
    public ActionReturnUtil addServiceTemplate(@ModelAttribute ApplicationTemplateDto appTemplate)
            throws Exception {
        logger.info("add application template");
        String userName = (String) session.getAttribute("username");
        return appService.addServiceTemplateByName(appTemplate, userName);
    }
    
    /**
     *update service template to appTemplate on 17/05/05 .
     * 暂未使用
     * @param appTemplate
     * 
     * @return ActionReturnUtil
     */
    @ResponseBody
    @RequestMapping(value = "/{tenantId}/projects/{projectId}/apptemplates/{templateName}/updatesvctemplate", method = RequestMethod.PUT)
    public ActionReturnUtil updateServiceTemplate(@ModelAttribute ApplicationTemplateDto appTemplate)
            throws Exception {
        logger.info("update application template");
        String userName = (String) session.getAttribute("username");
        return appService.updateServiceTemplateByName(appTemplate, userName);
    }


    /**
     * 应用模板公私有类型转换  （暂未使用）
     *
     * @param name 模板名称
     *
     * @param status 公有和私有
     *
     * @return ActionReturnUtil
     *
     * @throws Exception
     *
     */
    @ResponseBody
    @RequestMapping(value = "/{tenantId}/projects/{projectId}/apptemplates/{templateName}/status",method = RequestMethod.PUT)
    public ActionReturnUtil switchPublic(@PathVariable(value = "templateName") String name, @RequestParam(value = "status", required = true) boolean status)
            throws Exception {
        logger.info("switch app template status");
        return appService.switchPub(name, status);
    }

    /**
     * 使用应用模板发布
     * @param tenantId
     * @param name
     * @param appName
     * @param tag
     * @param namespace
     * @param pub
     * @param projectId
     * @return ActionReturnUtil
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/{tenantId}/projects/{projectId}/apptemplates/{templateName}/deploys", method = RequestMethod.POST)
    public ActionReturnUtil deployApplicationTemplate(@PathVariable(value = "tenantId") String tenantId,
                                                  @PathVariable(value = "templateName") String name,
                                                  @RequestParam(value = "appName", required = true) String appName,
                                                  @RequestParam(value = "tag", required = false) String tag,
                                                  @RequestParam(value = "namespace", required = true) String namespace,
                                                  @RequestParam(value = "pub", required = true) String pub,
                                                  @PathVariable(value = "projectId") String projectId) throws Exception {
        logger.info("deploy application template.");
        String userName = (String) session.getAttribute("username");
        if(userName == null){
            throw new K8sAuthException(Constant.HTTP_401);
        }
        return appDeployService.deployApplicationTemplateByName(tenantId, name, appName, tag, namespace, userName, pub, projectId);
    }

    @ResponseBody
    @RequestMapping(value = "/{tenantId}/projects/{projectId}/apptemplates/{templateName}/checkResource", method = RequestMethod.GET)
    public ActionReturnUtil checkRemainResourceInNamespace(@PathVariable(value="projectId") String projectId,
                                                           @PathVariable(value = "templateName") String templateName,
                                                           @RequestParam(value = "namespace") String namespace) throws Exception {
        return appDeployService.checkAppNamespaceResource(namespace, templateName, projectId);
    }

    @ResponseBody
    @RequestMapping(value = "/{tenantId}/projects/{projectId}/apptemplates/{templateName}/checkname", method = RequestMethod.GET)
    public ActionReturnUtil checkApplicationTemplateName(@PathVariable(value = "templateName") String templateName,
                                                         @PathVariable(value = "projectId") String projectId,
                                                         @RequestParam(value = "clusterId") String clusterId) throws Exception {
        return appService.checkAppTemplateName(templateName, projectId, clusterId);
    }
} 
