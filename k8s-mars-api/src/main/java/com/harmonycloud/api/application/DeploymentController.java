package com.harmonycloud.api.application;

import com.harmonycloud.common.exception.K8sAuthException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.dto.business.DeploymentDetailDto;
import com.harmonycloud.k8s.constant.Constant;
import com.harmonycloud.service.application.DeploymentsService;
import com.harmonycloud.service.application.EsService;
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
@RequestMapping("/deployments")
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
	public ActionReturnUtil listDeployments(@RequestParam(value = "tenantId", required = false) String tenantId,@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "namespace", required = false) String namespace,
			@RequestParam(value = "labels", required = false) String labels, @RequestParam(value = "status", required = false) String status) throws Exception {
		
		try {
			logger.info("获取服务列表");
			ActionReturnUtil result = dpService.listDeployments(tenantId,name, namespace, labels, status);
			return result;
		} catch (Exception e) {
			if (e instanceof K8sAuthException) {
				throw e;
			}
			e.printStackTrace();
			logger.error("获取服务列表失败：name="+name+ ", namespace="+namespace+", labels="+labels+", error:"+e.getMessage()+e.getCause());
			return ActionReturnUtil.returnError();
		}
		
	}

	/**
	 * 创建deployment
	 * @param deploymentDetail
	 * @return
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	public ActionReturnUtil createDeployments(@ModelAttribute DeploymentDetailDto deploymentDetail) {
		try {
			logger.info("创建服务");
			String userName = (String) session.getAttribute("username");
			if(userName == null){
				throw new K8sAuthException(Constant.HTTP_401);
			}
			Cluster cluster = (Cluster) session.getAttribute("currentCluster");
			return dpService.createDeployment(deploymentDetail, userName, "", cluster);
		} catch (Exception e) {
			logger.error("创建服务失败：, error:"+e.getMessage()+e.getCause());
			e.printStackTrace();
			return ActionReturnUtil.returnError();
		}
	}

	/**
	 * 删除deployment
	 * @param name
	 * @param namespace
	 * @return
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.DELETE)
	public ActionReturnUtil deleteDeployments(@RequestParam(value = "name", required = true) String name,
			@RequestParam(value = "namespace", required = true) String namespace) {
		
		try {
			logger.info("删除服务");
			String userName = (String) session.getAttribute("username");
			if(userName == null){
				throw new K8sAuthException(Constant.HTTP_401);
			}
			Cluster cluster = (Cluster) session.getAttribute("currentCluster");
			return dpService.deleteDeployment(name, namespace, userName, cluster);
		} catch (Exception e) {
			logger.error("删除服务失败：name="+name+ ", namespace="+namespace+", error:"+e.getMessage());
			return ActionReturnUtil.returnError();
		}
	}

	
	/**
	 * 更新deployment
	 * @param deploymentDetail
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.PUT)
	public ActionReturnUtil updateDeployments(@ModelAttribute UpdateDeployment deploymentDetail) throws Exception {
		try {
			logger.info("更新服务");
			String userName = (String) session.getAttribute("username");
			if(userName == null){
				throw new K8sAuthException(Constant.HTTP_401);
			}
			Cluster cluster = (Cluster) session.getAttribute("currentCluster");
			return dpService.replaceDeployment(deploymentDetail, userName, cluster);
		} catch (Exception e) {
			logger.error("更新服务失败：, error:"+e.getMessage()+e.getCause());
			return ActionReturnUtil.returnError();
		}
	}

	
	/**
	 * 获取应用详情
	 * @param name
	 * @param namespace
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/detail", method = RequestMethod.GET)
	public ActionReturnUtil deploymentDetail(@RequestParam(value = "name", required = true) String name,
			@RequestParam(value = "namespace", required = true) String namespace) {
		
		try {
			logger.info("查询服务详情");
			if (StringUtils.isEmpty(namespace) || StringUtils.isEmpty(name)) {
				return ActionReturnUtil.returnError();
			}
			String userName = (String) session.getAttribute("username");
			if(userName == null){
				throw new K8sAuthException(Constant.HTTP_401);
			}
			Cluster cluster = (Cluster) session.getAttribute("currentCluster");
			return dpService.getDeploymentDetail(namespace, name, cluster);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("查询服务详情失败：name="+name+ ", namespace="+namespace+", error:"+e.getMessage());
			return ActionReturnUtil.returnError();
		}
		
	}

	@ResponseBody
	@RequestMapping(value = "/start", method = RequestMethod.POST)
	public ActionReturnUtil startDeployment(@RequestParam(value = "name", required = true) String name,
			@RequestParam(value = "namespace", required = true) String namespace) {
		
		try {
			logger.info("启动服务");
			if (StringUtils.isEmpty(namespace) || StringUtils.isEmpty(name)) {
				return ActionReturnUtil.returnError();
			}
			String userName = (String) session.getAttribute("username");
			if(userName == null){
				throw new K8sAuthException(Constant.HTTP_401);
			}
			Cluster cluster = (Cluster) session.getAttribute("currentCluster");
			return dpService.startDeployments(name, namespace, userName,cluster);
		} catch (Exception e) {
			logger.error("查询服务详情失败：name="+name+ ", namespace="+namespace+", error:"+e.getMessage());
			return ActionReturnUtil.returnError();
		}
		
	}

	@ResponseBody
	@RequestMapping(value = "/stop", method = RequestMethod.POST)
	public ActionReturnUtil stopDeployment(@RequestParam(value = "name", required = true) String name,
			@RequestParam(value = "namespace", required = true) String namespace) {
		
		try {
			logger.info("停止服务");
			if (StringUtils.isEmpty(namespace) || StringUtils.isEmpty(name)) {
				return ActionReturnUtil.returnError();
			}
			String userName = (String) session.getAttribute("username");
			if(userName == null){
				throw new K8sAuthException(Constant.HTTP_401);
			}
			Cluster cluster = (Cluster) session.getAttribute("currentCluster");
			return dpService.stopDeployments(name, namespace, userName, cluster);
		} catch (Exception e) {
			logger.error("停止服务详情失败：name="+name+ ", namespace="+namespace+", error:"+e.getMessage());
			return ActionReturnUtil.returnError();
		}
	}

    @ResponseBody
    @RequestMapping(value = "/scale", method = RequestMethod.POST)
    public ActionReturnUtil scaleDeployment(@RequestParam(value = "name", required = true) String name, @RequestParam(value = "namespace", required = true) String namespace,
            @RequestParam(value = "scale") Integer scale) throws Exception {
        logger.info("改变服务实例数量");
        if (scale == null) {
            return ActionReturnUtil.returnErrorWithMsg("scale can not be null");
        }
        String userName = (String) session.getAttribute("username");
        if(userName == null){
			throw new K8sAuthException(Constant.HTTP_401);
		}
		Cluster cluster = (Cluster) session.getAttribute("currentCluster");
        return dpService.scaleDeployment(namespace, name, scale, userName, cluster);
    }
	
	@ResponseBody
	@RequestMapping(value="/pod", method = RequestMethod.GET)
	public ActionReturnUtil getPodDetail(@RequestParam(value = "name", required = true) String name,
			@RequestParam(value = "namespace", required = true) String namespace, @RequestParam(value = "clusterId", required = false) String clusterId) {
		
		try {
			logger.info("获取服务中的pod详情");
			if (StringUtils.isEmpty(namespace) || StringUtils.isEmpty(name)) {
				return ActionReturnUtil.returnError();
			}
			String userName = (String) session.getAttribute("username");
			if(userName == null){
				throw new K8sAuthException(Constant.HTTP_401);
			}

			Cluster cluster = null;
			if(null != clusterId && !"".equals(clusterId)) {
				cluster = this.clusterService.findClusterById(clusterId);
			} else {
				cluster = (Cluster) session.getAttribute("currentCluster");
			}
			return dpService.getPodDetail(name, namespace, cluster);
		} catch (Exception e) {
			logger.error("获取服务中的pod详情失败：name="+name+ ", namespace="+namespace+", error:"+e.getMessage());
			return ActionReturnUtil.returnError();
		}
		
	}
	
	@ResponseBody
	@RequestMapping(value="/podlist", method = RequestMethod.GET)
	public ActionReturnUtil podList(@RequestParam(value="name") String name, @RequestParam(value="namespace") String namespace) {
		
		try {
			logger.info("获取服务的pod列表");
			if (StringUtils.isEmpty(namespace) || StringUtils.isEmpty(name)) {
				return ActionReturnUtil.returnError();
			}
			String userName = (String) session.getAttribute("username");
			if(userName == null){
				throw new K8sAuthException(Constant.HTTP_401);
			}
			Cluster cluster = (Cluster) session.getAttribute("currentCluster");
			return dpService.podList(name, namespace, cluster);
		} catch (Exception e) {
			logger.error("获取服务的pod列表失败：name="+name+ ", namespace="+namespace+", error:"+e.getMessage());
			return ActionReturnUtil.returnError();
		}
		
	}
	
	@ResponseBody
	@RequestMapping(value="/events", method = RequestMethod.GET)
	public ActionReturnUtil getAppEvents(@RequestParam(value="name") String name, @RequestParam(value="namespace" , required=true) String namespace, @RequestParam(value="clusterId" , required=false) String clusterId) {
		
		try {
			logger.info("获取服务的事件");
			if (StringUtils.isEmpty(namespace) || StringUtils.isEmpty(name)) {
				return ActionReturnUtil.returnError();
			}
			String userName = (String) session.getAttribute("username");
			if(userName == null){
				throw new K8sAuthException(Constant.HTTP_401);
			}
			Cluster cluster = null;
			if(null != clusterId && !"".equals(clusterId)) {
				cluster = this.clusterService.findClusterById(clusterId);
			} else {
				cluster = (Cluster) session.getAttribute("currentCluster");
			}
			return dpService.getDeploymentEvents(namespace, name, cluster);
		} catch (Exception e) {
			logger.error("获取服务的事件失败：name="+name+ ", namespace="+namespace+", error:"+e.getMessage());
			return ActionReturnUtil.returnError();
		}
	}
	
	@ResponseBody
	@RequestMapping(value="/namespace/userNum", method = RequestMethod.GET)
    public ActionReturnUtil getNamespaceUserNum(@RequestParam(value="namespace", required=true) String namespace) throws Exception{
		
		try {
			logger.info("获取namespace下的用户数量");
			String userName = (String) session.getAttribute("username");
			if(userName == null){
				throw new K8sAuthException(Constant.HTTP_401);
			}
			Cluster cluster = (Cluster) session.getAttribute("currentCluster");
			return dpService.getNamespaceUserNum(namespace, cluster);
		} catch (Exception e) {
			logger.error("获取namespace下的用户数量失败：namespace="+namespace+", error:"+e.getMessage());
			return ActionReturnUtil.returnError();
		}
		
	}
	
	@ResponseBody
	@RequestMapping(value = "/containers", method = RequestMethod.GET)
	public ActionReturnUtil getDeploymentContainer(@RequestParam(value="name") String name, @RequestParam(value="namespace" , required=true) String namespace) {
		try {
			logger.info("获取pod的cantainer");
			String userName = (String) session.getAttribute("username");
			if(userName == null){
				throw new K8sAuthException(Constant.HTTP_401);
			}
			Cluster cluster = (Cluster) session.getAttribute("currentCluster");
			return dpService.deploymentContainer(namespace, name, cluster);
		} catch (Exception e) {
			logger.error("获取pod的cantainer失败：name="+name+ ", namespace="+namespace+", error:"+e.getMessage());
			return ActionReturnUtil.returnError();
		}
	}

	/**
	 * 查询namespace下容器名称列表
	 *
	 * @param namespace
	 * @return
	 */
	@RequestMapping(value = "/namespace/containers", method = RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil getContainerList(@RequestParam(value = "namespace") String namespace)
			throws Exception {
		String userName = (String) session.getAttribute("username");
		if(userName == null){
			throw new K8sAuthException(Constant.HTTP_401);
		}
		Cluster cluster = (Cluster) session.getAttribute("currentCluster");
		return dpService.namespaceContainer(namespace, cluster);

	}

}
