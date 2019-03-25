package com.harmonycloud.api.monitor;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.bean.Node;
import com.harmonycloud.k8s.bean.NodeCondition;
import com.harmonycloud.k8s.bean.NodeList;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import com.harmonycloud.service.application.EsService;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.platform.bean.monitor.InfluxdbQuery;
import com.harmonycloud.service.platform.service.*;
import com.harmonycloud.service.tenant.TenantService;
import org.influxdb.dto.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.rmi.MarshalException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 监控告警（日志，k8s组件）
 * 
 * 
 * @author jmi
 *
 */

@RestController
@RequestMapping(value="/clusters/{clusterId}")
public class MonitorAlarmController {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private InfluxdbService influxdbService;
	
	@Autowired
	private EsService esService;

	@Autowired
	private ClusterService clusterService;

	@Autowired
	private PodService podService;

	@Autowired
	private DashboardService dashboardService;

	@Autowired
	private TenantService tenantService;

	@Autowired
	private LogService logService;

	@Autowired
	private com.harmonycloud.k8s.service.NodeService nodeService;

	@Autowired
	private com.harmonycloud.service.platform.service.NodeService nodeService1;

	@ResponseBody
	@RequestMapping(value="/nodes/{nodename}/monitor", method=RequestMethod.GET)
	public ActionReturnUtil nodeQuery(@RequestParam(value="type") String type,@RequestParam(value="rangeType", required=false) String rangeType,
			@RequestParam(value="target", required=false) String target, @PathVariable(value="nodename") String name,
			@RequestParam(value="startTime", required=false) String startTime,
			@RequestParam(value="processName", required=false) String processName,
			@PathVariable(value = "clusterId") String clusterId) throws Exception{
		InfluxdbQuery influxdbQuery = new InfluxdbQuery();
		influxdbQuery.setRangeType(rangeType);
		influxdbQuery.setMeasurement(target);
		influxdbQuery.setNode(name);
		influxdbQuery.setType(type);
		influxdbQuery.setStartTime(startTime);
		influxdbQuery.setProcessName(processName);
		influxdbQuery.setClusterId(clusterId);
		ActionReturnUtil result = influxdbService.nodeQuery(influxdbQuery);
		return result;
	}
	
	@ResponseBody
	@RequestMapping(value="/monitor/process/status", method=RequestMethod.GET)
	public ActionReturnUtil getProcessStatus(@RequestParam(value="name", required=false) String name, 
			@RequestParam(value="processName", required=false) String processName,
	        @RequestParam(value="clusterId", required=false) String clusterId) throws Exception {
		ActionReturnUtil result = influxdbService.getProcessStatus(name, processName, clusterId);
		return result;
	}
	
	@ResponseBody
	@RequestMapping(value="/pod/{podName}/monitor", method=RequestMethod.GET)
	public ActionReturnUtil getPodMonit(@RequestParam(value="rangeType") String rangeType, @RequestParam(value="startTime") String startTime,
			@PathVariable(value="podName") String pod, @RequestParam(value="target") String target,
										@PathVariable(value="clusterId") String clusterId) throws Exception {
		InfluxdbQuery influxdbQuery = new InfluxdbQuery();
		influxdbQuery.setRangeType(rangeType);
		influxdbQuery.setMeasurement(target);
		influxdbQuery.setStartTime(startTime);
		influxdbQuery.setClusterId(clusterId);
		influxdbQuery.setPod(pod);
		ActionReturnUtil result = influxdbService.podMonit(influxdbQuery, null);
		return result;
	}
	
	@ResponseBody
	@RequestMapping(value="/pod/{podName}/container/{containerName}/monitor", method=RequestMethod.GET)
	public ActionReturnUtil monitorContainer(@RequestParam(value="rangeType") String rangeType, @RequestParam(value="startTime") String startTime,
			@PathVariable(value="podName") String pod, @PathVariable(value="containerName") String container,
											 @RequestParam(value="target") String target, @PathVariable(value="clusterId") String clusterId,
											 @RequestParam(value="request", required = false) Integer request) throws ParseException, IOException, NoSuchAlgorithmException, KeyManagementException {
		try {
			InfluxdbQuery influxdbQuery = new InfluxdbQuery();
			influxdbQuery.setRangeType(rangeType);
			influxdbQuery.setMeasurement(target);
			influxdbQuery.setStartTime(startTime);
			influxdbQuery.setClusterId(clusterId);
			influxdbQuery.setPod(pod);
			influxdbQuery.setContainer(container);
			return influxdbService.podMonit(influxdbQuery, request);
		} catch (MarsRuntimeException e) {
			throw e;
		}
	}

	@ResponseBody
	@RequestMapping(value = "/resource/usage", method = RequestMethod.GET)
	public ActionReturnUtil getClusterResourceUsage(@PathVariable(value="clusterId") String clusterId,
													@RequestParam(value="nodename", required=false) String nodename) throws Exception {
		try {
            return clusterService.getClusterResourceUsage(clusterId, nodename);
		} catch (Exception e) {
			logger.warn("获取集群资源使用量失败", e);
			throw new Exception("Failed to getClusterResourceUsage.", e);
		}
	}


	/**
	 * 获取某个集群的组件状态
	 * @param clusterId
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value="/components/status", method=RequestMethod.GET)
	public ActionReturnUtil clusterComponentStatus(@PathVariable(value="clusterId") String clusterId) throws Exception {
        return ActionReturnUtil.returnSuccessWithData(clusterService.getClusterComponentStatus(clusterId));
	}

	@ResponseBody
	@RequestMapping(value = "/resource/allocate", method = RequestMethod.GET)
	public ActionReturnUtil getClusterAllocatedResources(@PathVariable(value = "clusterId") String clusterId) throws Exception {
		try {
		    return ActionReturnUtil.returnSuccessWithData(clusterService.getClusterAllocatedResources(clusterId));
		} catch (Exception e) {
			logger.error("Failed to get cluster allocated resources."+e.getMessage());
			return ActionReturnUtil.returnErrorWithMsg(e.getMessage());
		}
	}

	/**
	 * 获取某个集群的所有节点信息
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/resource/info", method = RequestMethod.GET)
	public ActionReturnUtil getClusterNodeInfo(@PathVariable(value = "clusterId") String clusterId) throws Exception {
		return influxdbService.getClusterNodeInfo(clusterId);
	}

}
