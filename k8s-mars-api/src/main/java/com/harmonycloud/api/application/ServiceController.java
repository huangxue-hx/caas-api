package com.harmonycloud.api.application;

import com.harmonycloud.common.exception.K8sAuthException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.application.ServiceDeployDto;
import com.harmonycloud.dto.application.ServiceTemplateDto;
import com.harmonycloud.k8s.constant.Constant;
import com.harmonycloud.service.application.ServiceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;


/**
 * Created by root on 3/29/17.
 */
@Controller
@RequestMapping("/tenants/{tenantId}/projects/{projectId}")
public class ServiceController {

    @Autowired
    HttpSession session;

    @Autowired
    ServiceService serviceService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * create service template on 17/05/05.
     * 
     * @param serviceTemplate
     * 
     * @return
     * 
     */
    @ResponseBody
    @RequestMapping(value = "/svctemplates", method = RequestMethod.POST)
    public ActionReturnUtil saveServiceTemplate(@ModelAttribute ServiceTemplateDto serviceTemplate) throws Exception {
        logger.info("create service template");
        String userName = (String) session.getAttribute("username");
        return serviceService.saveServiceTemplate(serviceTemplate, userName, serviceTemplate.getType());
    }

    /**
     * list template by tlistTemplateByImage on 17/05/05.
     *
     * @param name
     * 
     * @param clusterId
     * 
     * @return
     * 
     * @throws Exception
     * 
     */
    @ResponseBody
    @RequestMapping(value = "/svctemplates", method = RequestMethod.GET)
    public ActionReturnUtil listServiceTemplate(@RequestParam(value = "name", required = false) String name,
                                            @RequestParam(value = "clusterId", required = false) String clusterId,
                                            @RequestParam(value = "isPubilc", required = false) boolean isPublic,
                                            @PathVariable(value = "projectId") String projectId) throws Exception {
        return serviceService.listServiceTemplate(name, clusterId, isPublic, projectId);
    }

    /**
     * list template by image on 17/05/05.
     *
     * @param name 服务模板名称
     * 
     * @param projectId 项目Id
     * 
     * @param image 镜像信息
     *
     * @param tenant 租户
     * 
     * @return ActionReturnUtil
     * 
     * @throws Exception
     * 
     */
    @ResponseBody
    @RequestMapping(value = "/svctemplates/images", method = RequestMethod.GET)
    public ActionReturnUtil listDeploymentsByImage(@RequestParam(value = "name", required = false) String name, @RequestParam(value = "tenant", required = false) String tenant,
            @RequestParam(value = "image", required = true) String image, @PathVariable(value = "projectId") String projectId) throws Exception {
        return serviceService.listTemplateByImage(name, tenant, image, projectId);
    }

    /**
     * list template by image on 17/05/05.
     *
     * @param name
     * 
     * @param clusterId
     * 
     * @return
     * 
     * @throws Exception
     * 
     */
    @ResponseBody
    @RequestMapping(value = "/svctemplates/{templateName}", method = RequestMethod.GET)
    public ActionReturnUtil getSpecificServiceTemplate(@PathVariable(value = "templateName") String name,
                                                       @RequestParam(value = "clusterId", required = false) String clusterId,
                                                       @RequestParam(value = "tag", required = false) String tag,
                                                       @PathVariable(value = "projectId") String projectId) throws Exception {
        return ActionReturnUtil.returnSuccessWithData(serviceService.getSpecificTemplate(name, tag, clusterId, projectId));
    }

    /**
     * (useless) #4 update service template on 17/05/05.
     * 
     * @param serviceTemplate
     * 
     * @return
     * 
     * @throws Exception
     * 
     */
    @ResponseBody
    @RequestMapping(value = "/svctemplates/{templateName}", method = RequestMethod.PUT)
    public ActionReturnUtil updateServiceTemplate(@ModelAttribute ServiceTemplateDto serviceTemplate) throws Exception {
        logger.info("update service template");
        String userName = (String) session.getAttribute("username");
        if(userName == null){
			throw new K8sAuthException(Constant.HTTP_401);
		}
        return serviceService.updateServiceTemplate(serviceTemplate, userName);
    }

    /**
     * delete service template on 17/05/05.
     * 
     * @param name
     * 
     * @return ActionReturnUtil 
     * 
     */
    @ResponseBody
    @RequestMapping(value = "/svctemplates/{templateName}",method = RequestMethod.DELETE)
    public ActionReturnUtil deleteServiceTemplate(@PathVariable(value = "templateName") String name,
                                                  @PathVariable(value = "projectId") String projectId,
                                                  @RequestParam(value = "clusterId") String clusterId) throws Exception {
        logger.info("delete service template");
        String userName = (String) session.getAttribute("username");
        return serviceService.deleteServiceTemplate(name, userName, projectId, clusterId);
    }


    /**
     * 查询服务模板
     * @param searchKey
     * @param searchValue
     * @param clusterId
     * @param isPublic
     * @param projectId
     * @return ActionReturnUtil
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/svctemplates/search", method = RequestMethod.GET)
    public ActionReturnUtil listServiceTemplate(@RequestParam(value = "searchkey", required = false) String searchKey,
                                                @RequestParam(value = "searchvalue", required = false) String searchValue,
                                                @RequestParam(value = "clusterId", required = false) String clusterId,
                                                @RequestParam(value = "isPubilc", required = false) boolean isPublic,
                                                @PathVariable(value = "projectId") String projectId) throws Exception {
        return serviceService.listServiceTemplate(searchKey, searchValue, clusterId, isPublic, projectId);
    }

    /**
     * deploy service template by name(使用服务模板发布服务)
     * @param name 模板名称
     * @param app 应用名称
     * @param tenantId 租户Id
     * @param namespace 分区
     * @param clusterId 版本
     * @param nodeSelector 节点标签
     * @return ActionReturnUtil
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/svctemplates/{templateName}/deploys", method = RequestMethod.POST)
    public ActionReturnUtil deployServiceTemplateByName(@PathVariable(value = "templateName") String name,
                                                        @RequestParam(value = "serviceName", required = true) String app,
                                                        @PathVariable(value = "tenantId") String tenantId,
                                                        @RequestParam(value = "namespace", required = true) String namespace,
                                                        @RequestParam(value = "clusterId", required = false ) String clusterId,
                                                        @RequestParam(value = "nodeSelector", required = false ) String nodeSelector,
                                                        @PathVariable(value = "projectId") String projectId) throws Exception {
        logger.info("deploy service template");
        String userName = (String) session.getAttribute("username");
        if(userName == null){
			throw new K8sAuthException(Constant.HTTP_401);
		}
        return serviceService.deployServiceByName(app, tenantId, name, clusterId, namespace, userName, nodeSelector, projectId);
    }

    /**
     * deploy service template on 17/05/05.
     * @param serviceDeploy 服务模板信息
     * @return ActionReturnUtil
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/svctemplates/deploys", method = RequestMethod.POST)
    public ActionReturnUtil deployServiceTemplate(@ModelAttribute ServiceDeployDto serviceDeploy) throws Exception {
        logger.info("deploy service template");
        String userName = (String) session.getAttribute("username");
        if(userName == null){
			throw new K8sAuthException(Constant.HTTP_401);
		}
        return serviceService.deployService(serviceDeploy, userName);
    }

    
    /**
     * list template tags by name tenant on 17/05/05.
     *
     * @param name
     * 
     * @param tenant
     * 
     * @return
     * 
     * @throws Exception
     * 
     */
    @ResponseBody
    @RequestMapping(value = "/svctemplates/tags",method = RequestMethod.GET)
    public ActionReturnUtil listTags(@RequestParam(value = "name", required = true) String name,
                                     @RequestParam(value = "tenant") String tenant,
                                     @PathVariable(value = "projectId") String projectId)
            throws Exception {
        return serviceService.listTemplateTagsByName(name, tenant, projectId);
    }

    /**
     * switch public status about service template
     * @param name
     * @param status
     * @return ActionReturnUtil
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/svctemplates/{templateName}/status",method = RequestMethod.PUT)
    public ActionReturnUtil switchPublic(@PathVariable(value = "templateName") String name, @RequestParam(value = "status", required = false ) boolean status)
            throws Exception {
        logger.info("switch public status about service template.");
        return serviceService.switchPub(name, status);
    }

    @ResponseBody
    @RequestMapping(value = "/svctemplates/{templateName}/checkResource", method = RequestMethod.GET)
    public ActionReturnUtil checkRemainResourceInNamespace(@PathVariable(value="projectId") String projectId,
                                                           @PathVariable(value = "templateName") String templateName,
                                                           @RequestParam(value = "namespace") String namespace) throws Exception {
        return serviceService.checkResourceQuota(projectId, namespace, templateName);
    }

    @ResponseBody
    @RequestMapping(value = "/svctemplates/{templateName}/checkname", method = RequestMethod.GET)
    public ActionReturnUtil checkServiceTemplateName(@PathVariable(value = "templateName") String templateName,
                                                     @PathVariable(value = "projectId") String projectId,
                                                     @RequestParam(value = "clusterId") String clusterId) throws Exception {
        return serviceService.checkServiceTemplateName(templateName, projectId, clusterId);
    }
    
}
