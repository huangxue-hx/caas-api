package com.harmonycloud.api.application;

import com.harmonycloud.common.exception.K8sAuthException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.dto.business.DeployedServiceNamesDto;
import com.harmonycloud.dto.business.ServiceDeployDto;
import com.harmonycloud.dto.business.ServiceTemplateDto;
import com.harmonycloud.k8s.constant.Constant;
import com.harmonycloud.service.application.ServiceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;


/**
 * Created by root on 3/29/17.
 */
@Controller
@RequestMapping("/serviceTemplate")
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
    @RequestMapping(method = RequestMethod.POST)
    public ActionReturnUtil saveDeployments(@ModelAttribute ServiceTemplateDto serviceTemplate) throws Exception {
        logger.info("create service template");
        String userName = (String) session.getAttribute("username");
        if (serviceTemplate == null) {
            return ActionReturnUtil.returnErrorWithMsg("serviceTemplate is null");
        }
        return serviceService.saveServiceTemplate(serviceTemplate, userName);
    }

    /**
     * list template by tlistTemplateByImage on 17/05/05.
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
    @RequestMapping(method = RequestMethod.GET)
    public ActionReturnUtil listDeployments(@RequestParam(value = "name", required = false) String name, @RequestParam(value = "tenant", required = false) String tenant)
            throws Exception {
        logger.info("get service template by tenant");
        return serviceService.listTemplateByTenat(name, tenant);
    }

    /**
     * list template by image on 17/05/05.
     *
     * @param name
     * 
     * @param tenant
     * 
     * @param image
     * 
     * @return
     * 
     * @throws Exception
     * 
     */
    @ResponseBody
    @RequestMapping(value = "/image", method = RequestMethod.GET)
    public ActionReturnUtil listDeploymentsByImage(@RequestParam(value = "name", required = false) String name, @RequestParam(value = "tenant", required = false) String tenant,
            @RequestParam(value = "image", required = true) String image) throws Exception {
        logger.info("get service template by image");
        if (StringUtils.isEmpty(image)) {
            return ActionReturnUtil.returnErrorWithMsg("image is null");
        }
        return serviceService.listTemplateByImage(name, tenant, image);
    }

    /**
     * list template by image on 17/05/05.
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
    @RequestMapping(value = "/detial", method = RequestMethod.GET)
    public ActionReturnUtil getSpecificServiceTemplate(@RequestParam(value = "name", required = true) String name, @RequestParam(value = "tenant", required = false) String tenant,
            @RequestParam(value = "tag", required = true) String tag) throws Exception {
        logger.info("get service template by image");
        if (StringUtils.isEmpty(name) || StringUtils.isEmpty(tag)) {
            return ActionReturnUtil.returnErrorWithMsg("name or tag is null");
        }
        return serviceService.getSpecificTemplate(name, tag);
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
    @RequestMapping(method = RequestMethod.PUT)
    public ActionReturnUtil updateDeployments(@ModelAttribute ServiceTemplateDto serviceTemplate) throws Exception {
        logger.info("update service template");
        String userName = (String) session.getAttribute("username");
        if(userName == null){
			throw new K8sAuthException(Constant.HTTP_401);
		}
        return serviceService.updateServiceTemplata(serviceTemplate, userName, serviceTemplate.getTag());
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
    @RequestMapping(method = RequestMethod.DELETE)
    public ActionReturnUtil deleteDeployments(@RequestParam(value = "name", required = true) String name) throws Exception {
        logger.info("delete service template");
        String userName = (String) session.getAttribute("username");
        return serviceService.deleteServiceTemplate(name, userName);
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
    @RequestMapping(value = "/deployedService", method = RequestMethod.DELETE)
    public ActionReturnUtil deleteDeployedService(@ModelAttribute DeployedServiceNamesDto deployedServiceNamesDto) throws Exception {
        logger.info("delete service template");
        String userName = (String) session.getAttribute("username");
        if(userName == null){
			throw new K8sAuthException(Constant.HTTP_401);
		}
		Cluster cluster = (Cluster) session.getAttribute("currentCluster");
		return serviceService.deleteDeployedService(deployedServiceNamesDto, userName, cluster);
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
    @RequestMapping(value = "/list/search", method = RequestMethod.GET)
    public ActionReturnUtil listServiceTemplate(@RequestParam(value = "searchkey", required = false) String searchKey,
            @RequestParam(value = "searchvalue", required = false) String searchValue, @RequestParam(value = "tenant", required = false) String tenant) throws Exception {
        logger.info("delete service template");
        return serviceService.listServiceTemplate(searchKey, searchValue, tenant);
    }
    
    /**
     * deploy service template on 17/05/05.
     *
     * @param name
     * 
     * @param tag
     * 
     * @param namspace
     *
     * @return ActionReturnUtil
     *
     */
    @ResponseBody
    @RequestMapping(value = "/deploy/name", method = RequestMethod.POST)
    public ActionReturnUtil deployServiceTemplateByName(@RequestParam(value = "name", required = true) String name,
            @RequestParam(value = "namespace", required = true) String namespace, @RequestParam(value = "tag", required = true ) String tag) throws Exception {
        logger.info("deploy service template");
        String userName = (String) session.getAttribute("username");
        if(userName == null){
			throw new K8sAuthException(Constant.HTTP_401);
		}
		Cluster cluster = (Cluster) session.getAttribute("currentCluster");
        return serviceService.deployServiceByname(name, tag, namespace, cluster, userName);
    }
    
    /**
     * deploy service template on 17/05/05.
     *
     * @param name
     * 
     * @param tag
     * 
     * @param namspace
     *
     * @return ActionReturnUtil
     *
     */
    @ResponseBody
    @RequestMapping(value = "/deploy", method = RequestMethod.POST)
    public ActionReturnUtil deployServiceTemplate(@ModelAttribute ServiceDeployDto serviceDeploy) throws Exception {
        logger.info("deploy service template");
        String userName = (String) session.getAttribute("username");
        if(userName == null){
			throw new K8sAuthException(Constant.HTTP_401);
		}
		Cluster cluster = (Cluster) session.getAttribute("currentCluster");
        return serviceService.deployService(serviceDeploy, cluster, userName);
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
    @RequestMapping(value = "/tags",method = RequestMethod.GET)
    public ActionReturnUtil listTags(@RequestParam(value = "name", required = false) String name, @RequestParam(value = "tenant", required = false) String tenant)
            throws Exception {
        logger.info("get service template by tenant");
        return serviceService.listTemplateTagsByName(name, tenant);
    }
}
