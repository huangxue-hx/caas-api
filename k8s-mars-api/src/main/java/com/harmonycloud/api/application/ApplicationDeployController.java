package com.harmonycloud.api.application;

import com.harmonycloud.common.exception.K8sAuthException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.application.ApplicationTemplateDto;
import com.harmonycloud.service.application.ApplicationDeployService;
import com.harmonycloud.service.application.ApplicationService;
import com.harmonycloud.service.application.DeploymentsService;
import com.harmonycloud.dto.application.ApplicationDeployDto;
import com.harmonycloud.k8s.constant.Constant;
import com.harmonycloud.service.platform.bean.ApplicationList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

/**
 * Created by root on 4/11/17.
 */
@RequestMapping("/tenants/{tenantId}")
@Controller
public class ApplicationDeployController {

    @Autowired
    HttpSession session;

    @Autowired
    DeploymentsService dpService;

    @Autowired
    ApplicationService applicationService;

    @Autowired
    ApplicationDeployService applicationDeployService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * #1 get application list
     *
     * @param projectId
     *            project Id
     * @param tenantId
     *            tenant Id
     * @param namespace
     *            namespace
     * @param name
     *            application name
     * @param status
     *            application running status 0:abnormal;1:normal
     * @return ActionReturnUtil
     */
    @ResponseBody
    @RequestMapping(value = "/projects/{projectId}/apps", method = RequestMethod.GET)
    public ActionReturnUtil listApplication(@PathVariable(value = "projectId") String projectId,
                                           @PathVariable(value = "tenantId") String tenantId,
                                           @RequestParam(value = "namespace", required = false) String namespace,
                                           @RequestParam(value = "name", required = false) String name,
                                           @RequestParam(value = "status", required = false) String status) throws Exception {
        return applicationDeployService.searchApplication(projectId, tenantId, namespace, name, status);
    }

    /**
     * #2 get application detail
     *
     * @param id
     *            appId
     * @return ActionReturnUtil
     */
    @ResponseBody
    @RequestMapping(value = "/projects/{projectId}/apps/{appName}", method = RequestMethod.GET)
    public ActionReturnUtil getApplicationDetail(@RequestParam(value = "id", required = true) String id,
                                                 @PathVariable(value = "appName") String appName,
                                                 @RequestParam(value = "namespace", required = false) String namespace) throws Exception {
        String userName = (String) session.getAttribute("username");
        if(userName == null){
            throw new K8sAuthException(Constant.HTTP_401);
        }
        return applicationDeployService.selectApplicationById(id, appName, namespace);
    }

    /**
     * #4 deploy application template
     *
     * @param appDeploy
     *            appDeploy Bean
     * @return ActionReturnUtil
     */
    @ResponseBody
    @RequestMapping(value = "/projects/{projectId}/apps", method = RequestMethod.POST)

    public ActionReturnUtil createDeployments(@PathVariable(value = "tenantId") String tenantId,
                                              @ModelAttribute ApplicationDeployDto appDeploy) throws Exception {
        logger.info("deploy application");
        String userName = (String) session.getAttribute("username");
        if(userName == null){
            throw new K8sAuthException(Constant.HTTP_401);
        }
        appDeploy.setTenantId(tenantId);
        return applicationDeployService.deployApplicationTemplate(appDeploy, userName);
    }

    /**
     * #5 delete application template
     *
     * @param applicationList
     *            appId list
     * @returnActionReturnUtil
     */
    @ResponseBody
    @RequestMapping(value = "/projects/{projectId}/apps/{appName}", method = RequestMethod.DELETE)
    public ActionReturnUtil deleteDeployments(@ModelAttribute ApplicationList applicationList) throws Exception {
        logger.info("delete application");
        String userName = (String) session.getAttribute("username");
        if(userName == null){
            throw new K8sAuthException(Constant.HTTP_401);
        }
        return applicationDeployService.deleteApplicationTemplate(applicationList, userName);
    }

    /**
     * #12 deploy application serviceTemplate 在已有发布的业务中
     *
     * @param appDeploy
     *            appDeploy Bean
     * @return ActionReturnUtil
     */
    @ResponseBody
    @RequestMapping(value = "/projects/{projectId}/apps/{appName}/deploys", method = RequestMethod.POST)
    public ActionReturnUtil deployDeployments(@PathVariable(value = "tenantId") String tenantId,
                                              @ModelAttribute ApplicationDeployDto appDeploy) throws Exception {
        logger.info("deploy service in application");
        String userName = (String) session.getAttribute("username");
        if(userName == null){
            throw new K8sAuthException(Constant.HTTP_401);
        }
        appDeploy.setTenantId(tenantId);
        return applicationDeployService.addAndDeployApplicationTemplate(appDeploy, userName);
    }

    /**
     * get application topo
     * 暂未使用
     * @param id
     * @return ActionReturnUtil
     */
    @ResponseBody
    @RequestMapping(value = "/projects/{projectId}/apps/{appName}/topo", method = RequestMethod.GET)
    public ActionReturnUtil getApplicationTopo(@RequestParam(value = "id", required = true) String id) throws Exception {
        return applicationDeployService.getTopo(id);
    }


    /**
     * 获取namespace下的所有应用（包括微服务组件）
     * @param namespace
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/apps", method = RequestMethod.GET)
    public ActionReturnUtil listApplicationInNamespace(@RequestParam(value = "namespace") String namespace) throws Exception {
        return applicationDeployService.getApplicationListInNamespace(namespace);
    }

    @ResponseBody
    @RequestMapping(value = "/projects/{projectId}/apps/yaml", method = RequestMethod.POST)
    public ActionReturnUtil getApplicationTemplateYaml(@ModelAttribute ApplicationTemplateDto appTemplate) throws Exception {
        logger.info("get application yaml");
        return applicationService.getApplicationTemplateYaml(appTemplate);
    }

    @ResponseBody
    @RequestMapping(value = "/projects/{projectId}/apps/{appName}/start", method = RequestMethod.POST)
    public ActionReturnUtil startApplication(@ModelAttribute ApplicationList appList) throws Exception {
        logger.info("start application");
        String userName = (String) session.getAttribute("username");
        if(userName == null){
            throw new K8sAuthException(Constant.HTTP_401);
        }
        return applicationDeployService.startApplication(appList, userName);
    }

    @ResponseBody
    @RequestMapping(value = "/projects/{projectId}/apps/{appName}/stop", method = RequestMethod.POST)
    public ActionReturnUtil stopApplication(@ModelAttribute ApplicationList appList) throws Exception {
        logger.info("stop application");
        String userName = (String) session.getAttribute("username");
        if(userName == null){
            throw new K8sAuthException(Constant.HTTP_401);
        }
        return applicationDeployService.stopApplication(appList, userName);
    }

    /**
     * 更新应用
     * @param appName
     * @param namespace
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/projects/{projectId}/apps/{appName}", method = RequestMethod.PUT)
    public ActionReturnUtil updateApplication(@PathVariable(value = "appName") String appName,
                                              @RequestParam(value = "namespace") String namespace,
                                              @RequestParam(value = "desc") String desc) throws Exception {
        logger.info("update application");
        return applicationDeployService.updateApplication(appName, namespace, desc);
    }

}