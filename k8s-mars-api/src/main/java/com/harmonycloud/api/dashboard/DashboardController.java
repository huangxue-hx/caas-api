package com.harmonycloud.api.dashboard;

import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.application.ApplicationDeployService;
import com.harmonycloud.service.platform.bean.monitor.InfluxdbQuery;
import com.harmonycloud.service.tenant.NamespaceService;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.service.platform.service.DashboardService;
import com.harmonycloud.service.platform.service.InfluxdbService;


/**
 * dashboard
 * @author jmi
 *
 */

@Controller
@RequestMapping(value = "/dashboard/clusters/{clusterId}")
public class DashboardController {
	
	@Autowired
	private DashboardService dashboardService;
	
	@Autowired
	private InfluxdbService influxdbService;

	@Autowired
	private ClusterService clusterService;
    @Autowired
	private NamespaceService namespaceService;
	@Autowired
    private ApplicationDeployService applicationDeployService;
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@ResponseBody
	@RequestMapping(value = "/pods", method = RequestMethod.GET)
	public ActionReturnUtil getPodInfo(@PathVariable(value = "clusterId") String clusterId) throws Exception{
		
		try {
//			logger.info("dashboard获取pod信息");
			Cluster cluster = clusterService.findClusterById(clusterId);
			if (null == cluster) {
				return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.CLUSTER_NOT_FOUND);
			}
			return dashboardService.getPodInfo(cluster);
		} catch (Exception e) {
			logger.error("dashboard获取pod信息失败,e:"+e.getMessage());
			throw e;
		}
	}
	
	@ResponseBody
	@RequestMapping(value = "/infras", method = RequestMethod.GET)
	public ActionReturnUtil getInfraInfo(@PathVariable(value = "clusterId") String clusterId) throws Exception{
		
		try {
//			logger.info("dashboard获取机器信息");
			Cluster cluster = clusterService.findClusterById(clusterId);
			if (null == cluster) {
				return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.CLUSTER_NOT_FOUND);
			}

			return dashboardService.getInfraInfo(cluster);
		} catch (Exception e) {
			logger.error("dashboard获取机器信息失败,e:"+e.getMessage());
			throw e;
		}
	}
	
	@ResponseBody
	@RequestMapping(value = "/alarms", method = RequestMethod.GET)
	public ActionReturnUtil getWarningInfo(@PathVariable(value = "clusterId") String clusterId, @RequestParam(value = "namespace", required=false) String namespace) throws Exception{
		
		try {
//			logger.info("dashboard获取告警信息");
			Cluster cluster = clusterService.findClusterById(clusterId);
			if (null == cluster) {
				return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.CLUSTER_NOT_FOUND);
			}
			return dashboardService.getWarningInfo(cluster, namespace);
		} catch (Exception e) {
			logger.error("dashboard获取告警信息失败, namspace="+namespace+", e="+e.getMessage());
			throw e;
		}
	}
	
	@ResponseBody
	@RequestMapping(value = "/events" , method = RequestMethod.GET)
	public ActionReturnUtil getEventInfo(@PathVariable(value = "clusterId") String clusterId, @RequestParam(value = "namespace", required=false) String namespace) throws Exception{
		
		try {
//			logger.info("dashboard获取事件信息");
			Cluster cluster = clusterService.findClusterById(clusterId);
			if (null == cluster) {
				return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.CLUSTER_NOT_FOUND);
			}
			return dashboardService.getEventInfo(cluster, namespace);
		} catch (Exception e) {
			logger.error("dashboard获取事件信息失败, namspace="+namespace+", e="+e.getMessage());
			throw e;
		}
	}
	
	@ResponseBody
	@RequestMapping(value = "/monitors" , method = RequestMethod.GET)
	public ActionReturnUtil nodeMonit(@RequestParam(value = "rangeType") String rangeType, 
			@RequestParam(value = "target") String target,
			@RequestParam(value = "type") String type,
			@PathVariable(value = "clusterId") String clusterId) throws Exception{
		
		try {
//			logger.info("dashboard获取事件信息");
			InfluxdbQuery influxdbQuery = new InfluxdbQuery();
			influxdbQuery.setRangeType(rangeType);
			influxdbQuery.setMeasurement(target);
			influxdbQuery.setType(type);
			influxdbQuery.setClusterId(clusterId);
			return influxdbService.nodeQuery(influxdbQuery);
		} catch (Exception e) {
			logger.error("dashboard获取事件信息失败, rangeType="+rangeType+", target="+target+", type="+type+", e="+e.getMessage());
			throw e;
		}
	}

	@ResponseBody
	@RequestMapping(value = "/nodeLicense", method = RequestMethod.GET)
	public ActionReturnUtil getNodeLicense () throws Exception{
		try {
//			logger.info("获取node license");
			return dashboardService.getNodeLicense();
		} catch (Exception e) {
			logger.error("获取node license失败");
			throw e;
		}
	}

	/**
	 * 获取集群内所有应用
	 *
	 * @param clusterId
	 * @return ActionReturnUtil
	 */
	@ResponseBody
	@RequestMapping(value = "/sum", method = RequestMethod.GET)
	public ActionReturnUtil getSumOfApplication(@PathVariable(value = "clusterId") String clusterId) throws Exception {
		return applicationDeployService.searchSumApplication(clusterId);
	}

	/**
	 * 获取某个集群下的所有分区
	 *
	 * @param clusterId
	 * @return ActionReturnUtil
	 */
	@ResponseBody
	@RequestMapping(value = "/namespaces", method = RequestMethod.GET)
	public ActionReturnUtil getNamespaces(@PathVariable(value = "clusterId") String clusterId) throws Exception {
		return ActionReturnUtil.returnSuccessWithData(namespaceService.getNamespaceListByClusterId(clusterId));
	}

	/**
	 * 组件pod 列表
	 * @param clusterId
	 * @param podName
	 * @param namespace
	 * @return ActionReturnUtil
	 * @throws Exception
	 */
	@RequestMapping(value = "/component/pods", method = RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil listK8sComponentPod(@PathVariable(value = "clusterId") String clusterId,
									 @RequestParam(value = "podName") String podName,
									 @RequestParam(value = "namespace") String namespace) throws Exception {
//		logger.info("获取k8s的组件pod列表");
		Cluster cluster = clusterService.findClusterById(clusterId);
		if (null == cluster) {
			return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.CLUSTER_NOT_FOUND);
		}
		return dashboardService.listK8sComponentPod(cluster, podName, namespace);
	}

	@RequestMapping(value = "/component/pods/{podName:.+}", method = RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil getComponentPodDetail(@PathVariable(value = "clusterId") String clusterId,
												  @PathVariable(value = "podName") String name,
												  @RequestParam(value = "namespace") String namespace) throws Exception {

//		logger.info("获取k8s的组件pod详情");
		Cluster cluster = clusterService.findClusterById(clusterId);
		if (null == cluster) {
			return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.CLUSTER_NOT_FOUND);
		}
		return dashboardService.getComponentPodDetail(cluster, name, namespace);
	}

}
