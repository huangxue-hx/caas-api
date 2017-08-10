package com.harmonycloud.api.application;

import com.harmonycloud.common.exception.K8sAuthException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.service.application.BusinessDeployService;
import com.harmonycloud.service.application.BusinessService;
import com.harmonycloud.service.application.DeploymentsService;
import com.harmonycloud.dto.business.BusinessDeployDto;
import com.harmonycloud.dto.business.ServiceTemplateDto;
import com.harmonycloud.k8s.constant.Constant;
import com.harmonycloud.service.platform.bean.BusinessList;
import com.harmonycloud.service.platform.bean.UpdateDeployment;

import io.swagger.models.auth.In;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

/**
 * Created by root on 4/11/17.
 */
@RequestMapping("/business")
@Controller
public class BusinessDeployController {

    @Autowired
    HttpSession session;

    @Autowired
    DeploymentsService dpService;
    
    @Autowired
    BusinessService businessService;
    
    @Autowired
    BusinessDeployService businessDeployService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * #1 get application list
     * 
     * @param tenant
     *            tenant name
     * @param namespace
     *            namespace
     * @param name
     *            application name
     * @param status
     *            application running status 0:abnormal;1:normal
     * @return ActionReturnUtil
     */
    @ResponseBody
    @RequestMapping(method = RequestMethod.GET)
    public ActionReturnUtil selectBusiness(@RequestParam(value = "tenantId", required = false) String tenantId,@RequestParam(value = "tenant", required = false) String tenant, @RequestParam(value = "namespace", required = false) String namespace,
            @RequestParam(value = "name", required = false) String name, @RequestParam(value = "status", required = false) String status) throws Exception {
        logger.info("get application");
        return businessDeployService.searchBusiness(tenantId,tenant, namespace, name, status);
    }

    /**
     * #2 get application
     * 
     * @param id
     *            businessId
     * @return ActionReturnUtil
     */
    @ResponseBody
    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    public ActionReturnUtil selectBusinessDetail(@RequestParam(value = "id", required = true) int id) throws Exception {
        logger.info("get application detail");
        String userName = (String) session.getAttribute("username");
        if(userName == null){
			throw new K8sAuthException(Constant.HTTP_401);
		}
		Cluster cluster = (Cluster) session.getAttribute("currentCluster");
        return businessDeployService.selectBusinessById(id, cluster);
    }

    /**
     * #3 get pv
     * 
     * @param tenantId
     *            tenant id
     * @param namespace
     *            namespace
     * @param status
     *            pv usage 0:all;1:used;2:unused
     * @return ActionReturnUtil
     */
    @ResponseBody
    @RequestMapping(value = "/pv", method = RequestMethod.GET)
    public ActionReturnUtil selectPv(@RequestParam(value = "tenantId", required = false) String tenantId, @RequestParam(value = "namespce", required = false) String namespace,
            @RequestParam(value = "status", required = true) int status) throws Exception {
        logger.info("get pvs detail");
        String userName = (String) session.getAttribute("username");
        if(userName == null){
			throw new K8sAuthException(Constant.HTTP_401);
		}
        return businessDeployService.selectPv(tenantId, namespace, status);
    }

    /**
     * #4 deploy application template
     * 
     * @param businessDeploy
     *            BusinessDeploy Bean
     * @return ActionReturnUtil
     */
    @ResponseBody
    @RequestMapping(value = "/deploy", method = RequestMethod.POST)

    public ActionReturnUtil createDeployments(@ModelAttribute BusinessDeployDto businessDeploy) throws Exception {
        logger.info("deploy business");
        String userName = (String) session.getAttribute("username");
        if(userName == null){
			throw new K8sAuthException(Constant.HTTP_401);
		}
		Cluster cluster = (Cluster) session.getAttribute("currentCluster");
		String tenantId = (String) session.getAttribute("tenantId");
		ActionReturnUtil checkRes = businessDeployService.checkK8SName(businessDeploy, cluster);
		if(checkRes.isSuccess()){
			ActionReturnUtil saveRes = businessService.saveBusinessTemplate(businessDeploy.getBusinessTemplate(), userName);
			if(!saveRes.isSuccess()){
				return saveRes;
			}
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> idList = (List<Map<String, Object>>) saveRes.get("data");
			if(idList != null && idList.size() > 0){
				Map<String, Object> busMap = idList.get(0);
				businessDeploy.getBusinessTemplate().setId(Integer.parseInt(busMap.get(businessDeploy.getBusinessTemplate().getName()).toString()));
				if(idList.get(1) != null){
					Map<String, Object> svcMap = idList.get(1);
					if(businessDeploy.getBusinessTemplate().getServiceList() != null && businessDeploy.getBusinessTemplate().getServiceList().size() >0 ){
						for(ServiceTemplateDto s : businessDeploy.getBusinessTemplate().getServiceList()){
							s.setId(Integer.parseInt(svcMap.get(s.getName()).toString()));
						}
					}
				}
			}
			return businessDeployService.deployBusinessTemplate(businessDeploy, userName, cluster, tenantId);
		}else{
			return checkRes;
		}
    }

    /**
     * #5 delete application template
     * 
     * @param businessList
     *            businessId list
     * @returnActionReturnUtil
     */
    @ResponseBody
    @RequestMapping(value = "/deploy", method = RequestMethod.DELETE)
    public ActionReturnUtil deleteDeployments(@ModelAttribute BusinessList businessList) throws Exception {
        logger.info("delete application");
        String userName = (String) session.getAttribute("username");
        if(userName == null){
			throw new K8sAuthException(Constant.HTTP_401);
		}
        return businessDeployService.deleteBusinessTemplate(businessList, userName);
    }

    /**
     * #6 stop application
     * 
     * @param businessList
     *            businessId list
     * @return ActionReturnUtil
     */
    @ResponseBody
    @RequestMapping(value = "/deploy/stop", method = RequestMethod.POST)
    public ActionReturnUtil stopDeployments(@ModelAttribute BusinessList businessList) throws Exception {
        logger.info("stop application template");
        if (businessList == null || businessList.getIdList().size() <= 0) {
            return ActionReturnUtil.returnErrorWithMsg("businessList is null");
        }
        String userName = (String) session.getAttribute("username");
        if(userName == null){
			throw new K8sAuthException(Constant.HTTP_401);
		}
		Cluster cluster = (Cluster) session.getAttribute("currentCluster");
        return businessDeployService.stopBusinessTemplate(businessList, userName, cluster);
    }

    /**
     * #7 start application
     * 
     * @param businessList
     *            businessId list
     * @return ActionReturnUtil
     */
    @ResponseBody
    @RequestMapping(value = "/deploy/start", method = RequestMethod.POST)
    public ActionReturnUtil startDeployments(@ModelAttribute BusinessList businessList) throws Exception {
        logger.info("start application template");
        if (businessList == null || businessList.getIdList().size() <= 0) {
            return ActionReturnUtil.returnErrorWithMsg("businessList is null");
        }

        String userName = (String) session.getAttribute("username");
        Cluster cluster = (Cluster) session.getAttribute("currentCluster");
        return businessDeployService.startBusinessTemplate(businessList, userName, cluster);
    }

    /**
     * #8 get application statistics
     * 
     * @param tenant
     *            tenant name
     * @return ActionReturnUtil
     */
    @ResponseBody
    @RequestMapping(value = "/sum", method = RequestMethod.GET)
    public ActionReturnUtil selectSumBusiness(@RequestParam(value = "tenant", required = true) String [] tenant, @RequestParam(value = "clusterId", required = false) String clusterId) throws Exception {
        logger.info("get application");
        if(tenant != null && tenant.length > 0 ){
        	return businessDeployService.searchSumBusiness(tenant,clusterId);
        }else{
        	return ActionReturnUtil.returnErrorWithMsg("tenant不能为空");
        }
        
    }
    
    /**
     * #9 update deployment
     * 
     * @param tenant
     *            tenant name
     * @return ActionReturnUtil
     */
    @ResponseBody
    @RequestMapping(value = "/deployment", method = RequestMethod.PUT)
    public ActionReturnUtil updateBusinessDeployment(@ModelAttribute UpdateDeployment deploymentDetail) throws Exception {
        logger.info("update application");
        String userName = (String) session.getAttribute("username");
        if(userName == null){
			throw new K8sAuthException(Constant.HTTP_401);
		}
		Cluster cluster = (Cluster) session.getAttribute("currentCluster");
        return dpService.updateBusinessDeployment(deploymentDetail,userName, cluster);
    }

    /**
     * #10 get application statistics
     * 
     * @param tenant
     *            tenant name
     * @return ActionReturnUtil
     */
    @ResponseBody
    @RequestMapping(value = "/count", method = RequestMethod.GET)
    public ActionReturnUtil selectSum(@RequestParam(value = "tenant", required = false) String [] tenant) throws Exception {
        logger.info("get application");
        return businessDeployService.searchSum(tenant);
    }

    /**
     * #11 deploy application template
     * 
     * @param businessDeploy
     *            BusinessDeploy Bean
     * @return ActionReturnUtil
     */
    @ResponseBody
    @RequestMapping(value = "/deploy/name", method = RequestMethod.POST)
    public ActionReturnUtil deployDeploymentsById(@RequestParam(value = "tenantId", required = false) String tenantId, @RequestParam(value = "name", required = true) String name, @RequestParam(value = "business", required = true) String businessame, @RequestParam(value = "tag", required = true) String tag, @RequestParam(value = "namespace", required = true) String namespace) throws Exception {
        logger.info("deploy business");
        String userName = (String) session.getAttribute("username");
        if(userName == null){
			throw new K8sAuthException(Constant.HTTP_401);
		}
		Cluster cluster = (Cluster) session.getAttribute("currentCluster");
		if(StringUtils.isEmpty(tenantId)){
			tenantId = (String) session.getAttribute("tenantId");
		}
        return businessDeployService.deployBusinessTemplateByName(tenantId, name, businessame, tag, namespace, userName, cluster);
    }
    
    /**
     * #12 deploy application serviceTemplate 在已有发布的业务中
     * 
     * @param businessDeploy
     *            BusinessDeploy Bean
     * @return ActionReturnUtil
     */
    @ResponseBody
    @RequestMapping(value = "/deploy/service", method = RequestMethod.POST)

    public ActionReturnUtil deplotDeployments(@ModelAttribute BusinessDeployDto businessDeploy) throws Exception {
        logger.info("deploy business");
        String userName = (String) session.getAttribute("username");
        if(userName == null){
			throw new K8sAuthException(Constant.HTTP_401);
		}
        String tenantid = (String) session.getAttribute("tenantId");
		Cluster cluster = (Cluster) session.getAttribute("currentCluster");
        return businessDeployService.addAndDeployBusinessTemplate(businessDeploy, userName, tenantid, cluster);
    }
}
