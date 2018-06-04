package com.harmonycloud.api.application;

import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.K8sAuthException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.application.DeployedServiceNamesDto;
import com.harmonycloud.dto.application.ServiceDeployDto;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.dto.application.DeploymentDetailDto;
import com.harmonycloud.k8s.constant.Constant;
import com.harmonycloud.service.application.DeploymentsService;
import com.harmonycloud.service.application.EsService;
import com.harmonycloud.service.application.ServiceService;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.platform.bean.UpdateDeployment;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

/**
 * 
 * @author jmi
 *
 */
@RequestMapping("/tenants/{tenantId}/projects/{projectId}/deploys")
@Controller
public class DeploymentController {

	@Autowired
	DeploymentsService dpService;

	@Autowired
	EsService esService;
	@Autowired
	HttpSession session;

	@Autowired
	ClusterService clusterService;

	@Autowired
	ServiceService serviceService;

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * 获取当前namespace下的deployments
	 * 
	 * @param name
	 * @param namespace
	 * @param labels(可选)
	 *            搜索条件
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	public ActionReturnUtil listDeployments(@PathVariable(value = "tenantId") String tenantId,
											@RequestParam(value = "name", required = false) String name,
			                                @RequestParam(value = "namespace", required = false) String namespace,
			                                @RequestParam(value = "labels", required = false) String labels,
											@PathVariable(value = "projectId") String projectId,
											@RequestParam(value = "clusterId", required = false) String clusterId) throws Exception {
		ActionReturnUtil result = dpService.listDeployments(tenantId, name, namespace, labels, projectId, clusterId);
		return result;

	}

	/**
	 * 创建deployment
	 * 
	 * @param serviceDeploy
	 * @return ActionReturnUtil
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	public ActionReturnUtil deployService(@ModelAttribute ServiceDeployDto serviceDeploy) throws Exception {
		logger.info("deploy service");
		String userName = (String) session.getAttribute("username");
		if(userName == null){
			throw new K8sAuthException(Constant.HTTP_401);
		}
		return serviceService.deployService(serviceDeploy, userName);
	}
	/*public ActionReturnUtil createDeployments(@ModelAttribute DeploymentDetailDto deploymentDetail) throws Exception {
		logger.info("创建服务");
		String userName = (String) session.getAttribute("username");
		if (userName == null) {
			throw new K8sAuthException(Constant.HTTP_401);
		}
		Cluster cluster = (Cluster) session.getAttribute("currentCluster");
		return dpService.createDeployment(deploymentDetail, userName, "", cluster);
	}*/

	/**
	 * 获取服务详情
	 * 
	 * @param name
	 * @param namespace
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/{deployName}", method = RequestMethod.GET)
	public ActionReturnUtil deploymentDetail(@PathVariable(value = "deployName") String name,
			@RequestParam(value = "namespace", required = true) String namespace) throws Exception {

		String userName = (String) session.getAttribute("username");
		if (userName == null) {
			throw new K8sAuthException(Constant.HTTP_401);
		}
		return dpService.getDeploymentDetail(namespace, name);

	}

	@ResponseBody
	@RequestMapping(value = "/{deployName}/start", method = RequestMethod.POST)
	public ActionReturnUtil startDeployment(@PathVariable(value = "deployName") String name,
			@RequestParam(value = "namespace", required = true) String namespace) throws Exception {
		logger.info("启动服务");
		String userName = (String) session.getAttribute("username");
		if (userName == null) {
			throw new K8sAuthException(Constant.HTTP_401);
		}
		return dpService.startDeployments(name, namespace, userName);

	}

	@ResponseBody
	@RequestMapping(value = "/{deployName}/stop", method = RequestMethod.POST)
	public ActionReturnUtil stopDeployment(@PathVariable(value = "deployName") String name,
			@RequestParam(value = "namespace", required = true) String namespace) throws Exception {
		logger.info("停止服务");
		String userName = (String) session.getAttribute("username");
		if (userName == null) {
			throw new K8sAuthException(Constant.HTTP_401);
		}
		return dpService.stopDeployments(name, namespace, userName);
	}

	@ResponseBody
	@RequestMapping(value = "/{deployName}/scale", method = RequestMethod.POST)
	public ActionReturnUtil scaleDeployment(@PathVariable(value = "deployName") String name,
			@RequestParam(value = "namespace", required = true) String namespace,
			@RequestParam(value = "scale") Integer scale) throws Exception {
		logger.info("改变服务实例数量");
		String userName = (String) session.getAttribute("username");
		if (userName == null) {
			throw new K8sAuthException(Constant.HTTP_401);
		}
		return dpService.scaleDeployment(namespace, name, scale, userName);
	}

	@ResponseBody
	@RequestMapping(value = "/{deployName}/pods/{podName}", method = RequestMethod.GET)
	public ActionReturnUtil getPodDetail(@RequestParam(value = "name", required = true) String name,
			@RequestParam(value = "namespace") String namespace) throws Exception {

		if (StringUtils.isEmpty(namespace) || StringUtils.isEmpty(name)) {
			return ActionReturnUtil.returnError();
		}
		String userName = (String) session.getAttribute("username");
		if (userName == null) {
			throw new K8sAuthException(Constant.HTTP_401);
		}

		return dpService.getPodDetail(name, namespace);

	}

	@ResponseBody
	@RequestMapping(value = "/{deployName}/pods", method = RequestMethod.GET)
	public ActionReturnUtil podList(@PathVariable(value = "deployName") String name,
			@RequestParam(value = "namespace") String namespace) throws Exception {

		String userName = (String) session.getAttribute("username");
		if (userName == null) {
			throw new K8sAuthException(Constant.HTTP_401);
		}
		return dpService.podList(name, namespace);

	}

	@ResponseBody
	@RequestMapping(value = "/{deployName}/events", method = RequestMethod.GET)
	public ActionReturnUtil getAppEvents(@PathVariable(value = "deployName") String name,
			@RequestParam(value = "namespace", required = true) String namespace,
			@RequestParam(value = "clusterId", required = false) String clusterId) throws Exception {

		String userName = (String) session.getAttribute("username");
		if (userName == null) {
			throw new K8sAuthException(Constant.HTTP_401);
		}
		return dpService.getDeploymentEvents(namespace, name);
	}

	@ResponseBody
	@RequestMapping(value = "/{deployName}/containers", method = RequestMethod.GET)
	public ActionReturnUtil getDeploymentContainer(@PathVariable(value = "deployName") String name,
			@RequestParam(value = "namespace", required = true) String namespace) throws Exception {

		String userName = (String) session.getAttribute("username");
		if (userName == null) {
			throw new K8sAuthException(Constant.HTTP_401);
		}
 		return dpService.deploymentContainer(namespace, name);
	}

	/**
	 * 查询namespace下容器名称列表 （未使用）
	 *
	 * @param namespace
	 * @return
	 */
	@RequestMapping(value = "/containers", method = RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil getContainerList(@RequestParam(value = "namespace") String namespace) throws Exception {
		String userName = (String) session.getAttribute("username");
		if (userName == null) {
			throw new K8sAuthException(Constant.HTTP_401);
		}
		return dpService.namespaceContainer(namespace);
	}

	/**
	 * 检测服务名称是否重复
	 * @param name
	 * @param namespace
	 * @return ActionReturnUtil
	 * @throws Exception
	 */
	@RequestMapping(value = "/{deployName}/checkname", method = RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil checkServiceName(@PathVariable(value = "deployName") String name,
											 @RequestParam(value = "namespace", required = true) String namespace,
											 @RequestParam(value = "isTpl") boolean isTpl) throws Exception {

		ActionReturnUtil result = dpService.checkDeploymentName(name, namespace, isTpl);
		return result;
	}

	/**
	 * #9 update deployment
	 *
	 * @param deploymentDetail
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/{deployName}", method = RequestMethod.PUT)
	public ActionReturnUtil updateDeployments(@ModelAttribute UpdateDeployment deploymentDetail) throws Exception {
		logger.info("update application");
		String userName = (String) session.getAttribute("username");
		if(userName == null){
			throw new K8sAuthException(Constant.HTTP_401);
		}
		return dpService.updateAppDeployment(deploymentDetail,userName);
	}

	/**
	 * delete service template on 17/05/05.
	 *
	 * @param deployedServiceNamesDto 服务模板信息
	 * @return ActionReturnUtil
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/{deployName}", method = RequestMethod.DELETE)
	public ActionReturnUtil deleteDeployedService(@ModelAttribute DeployedServiceNamesDto deployedServiceNamesDto) throws Exception {
		logger.info("delete service template");
		String userName = (String) session.getAttribute("username");
		if(userName == null){
			throw new K8sAuthException(Constant.HTTP_401);
		}
		return serviceService.deleteDeployedService(deployedServiceNamesDto, userName);
	}

}