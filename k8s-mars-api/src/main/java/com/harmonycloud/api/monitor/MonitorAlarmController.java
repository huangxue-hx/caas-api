package com.harmonycloud.api.monitor;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.dao.cluster.bean.Cluster;
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
import com.harmonycloud.service.platform.bean.PodDto;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.platform.service.*;
import com.harmonycloud.service.tenant.TenantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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
//@RequestMapping(value="/monitor")
public class MonitorAlarmController {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	LogMonitService logMonitService;
	
	@Autowired
	InfluxdbService influxdbService;
	
	@Autowired
	EsService esService;

	@Autowired
	ClusterService clusterService;

	@Autowired
	PodService podService;

	@Autowired
	DashboardService dashboardService;

	@Autowired
	TenantService tenantService;

	@Autowired
	com.harmonycloud.k8s.service.NodeService nodeService;
	
	
	/**
	 * 告警列表
	 * @param id
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value="/monitor/alarm/list", method=RequestMethod.GET)
	public ActionReturnUtil listAlarm(@RequestParam(value="id", required=false) String id) throws Exception {
		
		ActionReturnUtil result = influxdbService.getAlarmList(id);
		return result;
	}
	
	@ResponseBody
	@RequestMapping(value="/monitor/log/setMonitor", method=RequestMethod.POST)
	public ActionReturnUtil setLogMonitor(@RequestParam(value="module") String module, @RequestParam(value="logMonitor") String logMonitor,
			 @RequestParam(value="severity") String severity, @RequestParam(value="restartMonitor") String restartMonitor) throws Exception {
		
		ActionReturnUtil result = logMonitService.setLogMonitor(module, logMonitor, severity, restartMonitor);
		return result;
	}
	
	@ResponseBody
	@RequestMapping(value="/monitor/log/listMonitor", method=RequestMethod.GET)
	public ActionReturnUtil listLogMonitor() throws Exception {
		try {
		   ActionReturnUtil result = logMonitService.listLogMonitor();
		   return result;
		} catch (Exception e) {
			
			return ActionReturnUtil.returnError();
		}
		
	}
	
	@ResponseBody
	@RequestMapping(value="/monitor/log/setAlertType", method=RequestMethod.POST)
	public ActionReturnUtil setLogAlertType(@RequestParam(value="alertType") String alertType) throws Exception {
		ActionReturnUtil result = logMonitService.setLogAlertType(alertType);
		return result;
	}
	
	@ResponseBody
	@RequestMapping(value="/monitor/log/listAlertType", method=RequestMethod.GET)
	public ActionReturnUtil listLogAlertType() throws Exception {
		ActionReturnUtil result = logMonitService.listLogAlertType();
		return result;
	}
	
	@ResponseBody
	@RequestMapping(value="/monitor/log/setAlertEmail", method=RequestMethod.POST)
	public ActionReturnUtil setLogAlertEmail(@RequestParam(value="smtp") String smtp, @RequestParam(value="port") String port,
			@RequestParam(value="user") String user, @RequestParam(value="password") String password, @RequestParam(value="from") String from,
			@RequestParam(value="to") String to, @RequestParam(value="cc") String cc) throws Exception {
		ActionReturnUtil result = logMonitService.setLogAlertEmail(smtp, port, user, password, from, to, cc);
		return result;
	}
	
	@ResponseBody
	@RequestMapping(value="/monitor/log/listAlertEmail", method=RequestMethod.GET)
	public ActionReturnUtil listLogAlertEmail() throws Exception {
		ActionReturnUtil result = logMonitService.listLogAlertEmail();
		return result;
	}
	
	@ResponseBody
	@RequestMapping(value="/monitor/node", method=RequestMethod.GET)
	public ActionReturnUtil nodeQuery(@RequestParam(value="type") String type,@RequestParam(value="rangeType", required=false) String rangeType,
			@RequestParam(value="target", required=false) String target, @RequestParam(value="name", required=false) String name,
			@RequestParam(value="startTime", required=false) String startTime, 
			@RequestParam(value="processName", required=false) String processName) throws Exception{
		ActionReturnUtil result = influxdbService.nodeQuery(type, rangeType, target, name, startTime, processName);
		return result;
	}
	
	@ResponseBody
	@RequestMapping(value="/monitor/process/status", method=RequestMethod.GET)
	public ActionReturnUtil getProcessStatus(@RequestParam(value="name", required=false) String name, 
			@RequestParam(value="processName", required=false) String processName) throws Exception {
		ActionReturnUtil result = influxdbService.getProcessStatus(name, processName);
		return result;
	}
	
	@ResponseBody
	@RequestMapping(value="/monitor/providerList", method=RequestMethod.GET)
	public ActionReturnUtil getProviderList() throws Exception {
		ActionReturnUtil result = influxdbService.getProviderList();
		return result;
	}
	
	@ResponseBody
	@RequestMapping(value="/monitor/pod", method=RequestMethod.GET)
	public ActionReturnUtil getPodMonit(@RequestParam(value="rangeType") String rangeType, @RequestParam(value="startTime") String startTime,
			@RequestParam(value="pod") String pod, @RequestParam(value="container") String container, @RequestParam(value="target") String target) throws Exception {
		ActionReturnUtil result = influxdbService.podMonit(rangeType, startTime, pod, container, target, null);
		return result;
	}
	
	@ResponseBody
	@RequestMapping(value="/monitor/log/search", method=RequestMethod.GET)
	public ActionReturnUtil getLogProcess(@RequestParam(value="rangeType") String rangeType, @RequestParam(value="processName") String processName,
			@RequestParam(value="node") String node) throws Exception {
		ActionReturnUtil result = esService.getProcessLog(rangeType, processName, node);
		return result;
	}
	
	@ResponseBody
	@RequestMapping(value="/monitor/threshold/create", method=RequestMethod.POST)
	public ActionReturnUtil createThreshold(@RequestParam(value="processName") String processName,
			@RequestParam(value="measurement") String measurement, @RequestParam(value="threshold") String threshold,
			@RequestParam(value="alarmType") String alarmType, @RequestParam(value="alarmContact") String alarmContact) throws Exception {
		ActionReturnUtil result = influxdbService.createThreshold(processName, measurement, threshold, alarmType, alarmContact);
		return result;
	}
	
	@ResponseBody
	@RequestMapping(value="/monitor/threshold/delete", method=RequestMethod.DELETE)
	public ActionReturnUtil deleteThreshold(@RequestParam(value="id", required=true) String id) throws Exception {
		return influxdbService.deleteThreshold(id);
	}
	
	@ResponseBody
	@RequestMapping(value="/monitor/threshold/list", method=RequestMethod.GET)
	public ActionReturnUtil listThreshold(@RequestParam(value="id", required=false) String id) throws Exception {
		return influxdbService.listThreshold(id);
	}
	
	@ResponseBody
	@RequestMapping(value="/monitor", method=RequestMethod.GET)
	public ActionReturnUtil monitorContainer(@RequestParam(value="rangeType") String rangeType, @RequestParam(value="startTime") String startTime,
			@RequestParam(value="pod") String pod, @RequestParam(value="container", required=false) String container, @RequestParam(value="target") String target, @RequestParam(value="clusterId", required=false) String clusterId) throws Exception {
		try {
			return influxdbService.podMonit(rangeType, startTime, pod, container, target, clusterId);
		} catch (Exception e) {
			throw e;
		}
	}

	@ResponseBody
	@RequestMapping(value = "/monitor/clusterResourceUsage", method = RequestMethod.GET)
	public ActionReturnUtil getClusterResourceUsage(@RequestParam(value="clusterId", required=false) String clusterId, @RequestParam(value="nodename", required=false) String nodename) throws Exception {
//	public ActionReturnUtil getClusterResourceUsage(@RequestParam(value = "clusterId") String clusterId) throws Exception {

		try {

            List<Cluster> listCluster = new ArrayList<>();
            if(null != clusterId && !"".equals(clusterId)) {
		        Cluster cluster = this.clusterService.findClusterById(clusterId);
                listCluster.add(cluster);
            } else {
                listCluster = this.clusterService.listCluster();
            }

			Map<String, Map<String, Object>> mapClusterResourceUsage = new HashMap<String, Map<String, Object>>();
			if(null != listCluster && listCluster.size() > 0) {
				for (Cluster cluster : listCluster) {
					double clusterCpuCapacity = 0;
					double clusterMemoryCapacity = 0;
					double clusterFilesystemCapacity = 0;
					double clusterFilesystemUsage = 0;
					double clusterMemoryUsage = 0;
					double clusterCpuUsage = 0;
					Map<String, Object> res = new HashMap<String, Object>();

					Map<String, Object> allocatableMap = null;
					if(null != nodename && !"".equals(nodename)) {
						allocatableMap = this.dashboardService.getNodeInfo(cluster, nodename);
					} else {
						allocatableMap = this.dashboardService.getInfraInfoWorkNode(cluster);
					}

					clusterCpuCapacity = Double.parseDouble(allocatableMap.get("cpu").toString()) ;
//					clusterCpuCapacity = new BigDecimal(clusterCpuCapacity).setScale(0, BigDecimal.ROUND_HALF_UP).doubleValue();
//					clusterMemoryCapacity = Double.parseDouble(allocatableMap.get("memory").toString()) /1024/1024;
					clusterMemoryCapacity = Double.parseDouble(allocatableMap.get("memoryGb").toString()) ;
//					clusterMemoryCapacity = new BigDecimal(clusterMemoryCapacity).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
					List<String> notWorkNodeList = nodeService.listNotWorkNode(cluster);
//					double clusterCpuUsageRate = this.influxdbService.getClusterResourceUsage("node", "cpu/usage_rate","nodename", cluster, notWorkNodeList, nodename);
					clusterCpuUsage = this.influxdbService.getClusterResourceUsage("node", "cpu/usage_rate","nodename", cluster, notWorkNodeList, nodename);
//					clusterCpuUsage = clusterCpuCapacity * clusterCpuUsageRate;
					clusterCpuUsage = (double) clusterCpuUsage / 1000;
					clusterCpuUsage = new BigDecimal(clusterCpuUsage).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
					Double clusterCpuUsageRate = (double)clusterCpuUsage / clusterCpuCapacity;
					clusterCpuUsageRate = new BigDecimal(clusterCpuUsageRate).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

					clusterMemoryUsage =  this.influxdbService.getClusterResourceUsage("node", "memory/working_set", "nodename",cluster, notWorkNodeList, nodename);
					clusterMemoryUsage = (double)clusterMemoryUsage / 1024 /1024/1024;
					clusterMemoryUsage = new BigDecimal(clusterMemoryUsage).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
					double clusterMemoryUsageRate = (double)clusterMemoryUsage / clusterMemoryCapacity;
					clusterMemoryUsageRate = new BigDecimal(clusterMemoryUsageRate).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();


					clusterFilesystemUsage =  this.influxdbService.getClusterResourceUsage("node", "filesystem/usage", "nodename,resource_id",cluster, notWorkNodeList, nodename);
					clusterFilesystemUsage = (double)clusterFilesystemUsage/1024/1024/1024;
					clusterFilesystemUsage = new BigDecimal(clusterFilesystemUsage).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();

					clusterFilesystemCapacity =  this.influxdbService.getClusterResourceUsage("node", "filesystem/limit", "nodename,resource_id",cluster, notWorkNodeList,nodename);
					clusterFilesystemCapacity = (double)clusterFilesystemCapacity/1024/1024/1024;
					clusterFilesystemCapacity = new BigDecimal(clusterFilesystemCapacity).setScale(0, BigDecimal.ROUND_HALF_UP).doubleValue();
					double clusterFilesystemUsageRate = (double)clusterFilesystemUsage / clusterFilesystemCapacity;
					clusterFilesystemUsageRate = new BigDecimal(clusterFilesystemUsageRate).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

					Map<String, Object> map = new HashMap<String, Object>();

					map.put("clusterCpuUsageRateName", String.format("%.0f", clusterCpuUsageRate*100) + "%");
					String [] cpuUsageRateArray = {clusterCpuUsageRate.toString(), (double)(1-clusterCpuUsageRate) + ""};
					map.put("clusterCpuUsageRateValue", cpuUsageRateArray);

					map.put("clusterMemoryUsageRateName", String.format("%.0f", clusterMemoryUsageRate*100) + "%");
					String [] memoryUsageRateArray = {clusterMemoryUsageRate +"", (double)(1-clusterMemoryUsageRate) + ""};
					map.put("clusterMemoryUsageRateValue", memoryUsageRateArray);

					map.put("clusterFilesystemUsageRateName", String.format("%.0f", clusterFilesystemUsageRate*100) + "%");
					String [] filesystemUsageRateArray = {clusterFilesystemUsageRate +"", (double)(1-clusterFilesystemUsageRate) +""};
					map.put("clusterFilesystemUsageRateValue", filesystemUsageRateArray);

					map.put("clusterCpuCapacity", clusterCpuCapacity);
					map.put("clusterCpuUsage", clusterCpuUsage);
					map.put("clusterMemoryCapacity", String.format("%.1f", clusterMemoryCapacity));
					map.put("clusterMemoryUsage", clusterMemoryUsage);
					map.put("clusterFilesystemCapacity", clusterFilesystemCapacity);
					map.put("clusterFilesystemUsage", clusterFilesystemUsage);
					mapClusterResourceUsage.put(cluster.getId() + "", map);

				}

			}

			return ActionReturnUtil.returnSuccessWithData(mapClusterResourceUsage);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Failed to getClusterResourceUsage.", e);
		}
	}


	@ResponseBody
	@RequestMapping(value="/monitor/clusterComponentStatus", method=RequestMethod.GET)
	public ActionReturnUtil clusterComponentStatus() throws Exception {
		try {
            List<Cluster> listCluster = this.clusterService.listCluster();
            List<Map<String, String>> list = new ArrayList<>();
            Map<String, Object> respMap = new HashMap<>();
            if(null != listCluster && listCluster.size() > 0) {
                for (Cluster cluster : listCluster) {
                    Map<String, String> map = new HashMap<String, String>();
					map.put("clusterId", cluster.getId().toString());
                    map.put("clusterName", cluster.getName());
//                    map.put("cluster", Constant.STATUS_NORMAL);
                    map.put("kubeApiserver", Constant.STATUS_NORMAL);
                    map.put("kubeControllerManager", Constant.STATUS_NORMAL);
					map.put("kubeScheduler", Constant.STATUS_NORMAL);
                    map.put("etcd", Constant.STATUS_NORMAL);
                    map.put("elasticsearchLogging", Constant.STATUS_NORMAL);
                    map.put("calico", Constant.STATUS_NORMAL);
                    map.put("kubeDns", Constant.STATUS_NORMAL);
                    map.put("serviceLoadbalancer", Constant.STATUS_NORMAL);
                    map.put("monitor", Constant.STATUS_NORMAL);
                    map.put("nfs", Constant.STATUS_NORMAL);


                    List<PodDto> podDtoList = this.podService.getPodListByNamespace(cluster, "kube-system");

                    if (podDtoList == null) {
                        throw new Exception("Faild to get pod list.");
                    }
                    int kubeApiserverCount = 0;
                    int kubeControllerManagerCount = 0;
                    int kubeSchedulerCount = 0;
                    int etcdCount = 0;
                    int elasticsearchLoggingCount = 0;
                    int calicoCount = 0;
                    int kubeDnsCount = 0;
                    int monitorCount = 0;
                    int serviceLoadbalancerCount = 0;
                    int nfsCount = 0;
                    for (PodDto pod : podDtoList) {
                        if (pod.getName().contains("kube-apiserver")) {
                            if (!pod.getStatus().equalsIgnoreCase("running")) {
                                map.put("kubeApiserver", Constant.STATUS_ABNORMAL);
                            }
							kubeApiserverCount ++;
                        }
                        if (pod.getName().contains("kube-controller-manager")) {
                            if (!pod.getStatus().equalsIgnoreCase("running")) {
                                map.put("kubeControllerManager", Constant.STATUS_ABNORMAL);
                            }
							kubeControllerManagerCount ++;
                        }
						if (pod.getName().contains("kube-scheduler")) {
							if (!pod.getStatus().equalsIgnoreCase("running")) {
								map.put("kubeScheduler", Constant.STATUS_ABNORMAL);
							}
							kubeSchedulerCount ++;
						}
                        if (pod.getName().startsWith("etcd")) {
                            if (!pod.getStatus().equalsIgnoreCase("running")) {
                                map.put("etcd", Constant.STATUS_ABNORMAL);
                            }
							etcdCount ++;
                        }
                        if (pod.getName().contains("elasticsearch-logging") || pod.getName().contains("fluentd-es")) {
                            if (!pod.getStatus().equalsIgnoreCase("running")) {
                                map.put("elasticsearchLogging", Constant.STATUS_ABNORMAL);
                            }
							elasticsearchLoggingCount ++;
                        }
                        if (pod.getName().contains("calico")) {
                            if (!pod.getStatus().equalsIgnoreCase("running")) {
                                map.put("calico", Constant.STATUS_ABNORMAL);
                            }
							calicoCount ++;
                        }
                        if (pod.getName().contains("kube-dns")) {
                            if (!pod.getStatus().equalsIgnoreCase("running")) {
                                map.put("kubeDns", Constant.STATUS_ABNORMAL);
                            }
							kubeDnsCount ++;
                        }
                        if (pod.getName().contains("service-loadbalancer")) {
                            if (!pod.getStatus().equalsIgnoreCase("running")) {
                                map.put("serviceLoadbalancer", Constant.STATUS_ABNORMAL);
                            }
							serviceLoadbalancerCount ++;
                        }
                        if (pod.getName().contains("heapster") || pod.getName().contains("monitoring-influxdb")) {
                            if (!pod.getStatus().equalsIgnoreCase("running")) {
                                map.put("monitor", Constant.STATUS_ABNORMAL);
                            }
							monitorCount ++;
                        }
                        if (pod.getName().contains("nfs")) {
                            if (!pod.getStatus().equalsIgnoreCase("running")) {
                                map.put("nfs", Constant.STATUS_ABNORMAL);
                            }
							nfsCount ++;
                        }
                    }

					if (kubeApiserverCount == 0) {
						map.put("kubeApiserver", Constant.STATUS_ABNORMAL);
					}

					if (kubeControllerManagerCount == 0) {
						map.put("kubeControllerManager", Constant.STATUS_ABNORMAL);
					}

					if (kubeSchedulerCount == 0) {
						map.put("kubeScheduler", Constant.STATUS_ABNORMAL);
					}

					if (etcdCount == 0) {
						map.put("etcd", Constant.STATUS_NORMAL);
					}

					if (elasticsearchLoggingCount == 0) {
						map.put("elasticsearchLogging", Constant.STATUS_ABNORMAL);
					}

					if (calicoCount == 0) {
						map.put("calico", Constant.STATUS_ABNORMAL);
					}

					if (kubeDnsCount == 0) {
						map.put("kubeDns", Constant.STATUS_ABNORMAL);
					}

					if (serviceLoadbalancerCount == 0) {
						map.put("serviceLoadbalancer", Constant.STATUS_ABNORMAL);
					}

					if (monitorCount == 0) {
						map.put("monitor", Constant.STATUS_ABNORMAL);
					}

					if (nfsCount == 0) {
						map.put("nfs", Constant.STATUS_NORMAL);
					}
                    list.add(map);

                }
            }

            double normalCount = 0;
			double totalCount = 0;
            for(Map<String, String> map : list) {
				totalCount = totalCount + map.size() - 2;
				for (Map.Entry<String, String> entry : map.entrySet()) {
					if(entry.getValue().equalsIgnoreCase(Constant.STATUS_NORMAL)) {
						normalCount ++;
					}

				}
			}

			String health = String.format("%.0f", normalCount / totalCount * 100) ;
			respMap.put("clusterComponentHealth", health);
            respMap.put("clusterComponentStatus", list);

			return ActionReturnUtil.returnSuccessWithData(respMap);
		} catch (Exception e) {
			logger.error("Failed to get cluster component status."+e.getMessage());
			return ActionReturnUtil.returnErrorWithMsg(e.getMessage());
		}
	}

	@ResponseBody
	@RequestMapping(value = "/monitor/getClusterAllocatedResources", method = RequestMethod.GET)
	public ActionReturnUtil getClusterAllocatedResources() throws Exception {
		try {
		List<Cluster> listCluster = this.clusterService.listCluster();
		Map<String, Map<String, Object>> mapClusterResourceUsage = new HashMap<String, Map<String, Object>>();
		if(null != listCluster && listCluster.size() > 0) {
			for (Cluster cluster : listCluster) {
				double clusterCpuCapacity = 0;
				double clusterMemoryCapacity = 0;
				double clusterMemoryUsage = 0;
				double clusterCpuUsage = 0;
				Map<String, Object> res = new HashMap<String, Object>();
				Map<String, Object> allocatableMap = this.dashboardService.getInfraInfoWorkNode(cluster);

				clusterCpuCapacity = Double.parseDouble(allocatableMap.get("cpu").toString());
				clusterMemoryCapacity = Double.parseDouble(allocatableMap.get("memoryGb").toString()) ;

//				double clusterCpuAllocatedResources = this.influxdbService.getClusterAllocatedResources("cluster", "cpu/limit", cluster);
//				clusterCpuAllocatedResources = clusterCpuAllocatedResources / 1000;

//				double clusterMemoryAllocatedResources = this.influxdbService.getClusterAllocatedResources("cluster", "memory/limit", cluster);
//				clusterMemoryAllocatedResources = clusterMemoryAllocatedResources / 1024;

				Map<String, Object> mapQuota = this.tenantService.getTenantQuotaByClusterId(cluster.getId() + "");
				double clusterCpuAllocatedResources = Double.parseDouble(mapQuota.get(CommonConstant.CPU).toString());
				double clusterMemoryAllocatedResources = Double.parseDouble(mapQuota.get(CommonConstant.MEMORY).toString());
				clusterMemoryAllocatedResources = (double) clusterMemoryAllocatedResources / 1024;

				double cpufenzi = (double) clusterCpuCapacity - clusterCpuAllocatedResources;
				cpufenzi = new BigDecimal(cpufenzi).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
				double clusterCpuAllocatedResourcesRate = (double) cpufenzi / clusterCpuCapacity ;

				double memfenz = (double) clusterMemoryCapacity - clusterMemoryAllocatedResources;
				memfenz = new BigDecimal(memfenz).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
				double clusterMemoryAllocatedResourcesRate = memfenz / clusterMemoryCapacity ;

				Map<String, Object> map = new HashMap<String, Object>();

				map.put("clusterCpuAllocatedResourcesRate", String.format("%.0f",clusterCpuAllocatedResourcesRate*100));
				map.put("clusterMemoryAllocatedResourcesRate", String.format("%.0f",clusterMemoryAllocatedResourcesRate*100));
				map.put("clusterCpuCapacity", clusterCpuCapacity);
				map.put("clusterCpuAllocatedResources", cpufenzi);
				map.put("clusterMemoryCapacity", String.format("%.1f", clusterMemoryCapacity));
				map.put("clusterMemoryAllocatedResources", memfenz);
				mapClusterResourceUsage.put(cluster.getId() + "", map);
			}
		}
		return ActionReturnUtil.returnSuccessWithData(mapClusterResourceUsage);
		} catch (Exception e) {
			logger.error("Failed to get cluster allocated resources."+e.getMessage());
			return ActionReturnUtil.returnErrorWithMsg(e.getMessage());
		}
	}

	@ResponseBody
	@RequestMapping(value = "/monitor/getClusterNodeInfo", method = RequestMethod.GET)
	public ActionReturnUtil getClusterNodeInfo() throws Exception {
		try {
			Map<String, List<Map<String, Object>>> mapClusterNodeInfo = new HashMap<>();

			List<Cluster> listCluster = this.clusterService.listCluster();


			if(null != listCluster && listCluster.size() > 0) {
				for (Cluster cluster : listCluster) {
					// 获取node
					K8SURL url = new K8SURL();
					url.setResource(Resource.NODE);
					K8SClientResponse nodeRes = new K8sMachineClient().exec(url, HTTPMethod.GET, null, null, cluster);
					if (!HttpStatusUtil.isSuccessStatus(nodeRes.getStatus())) {
						return ActionReturnUtil.returnErrorWithMsg(nodeRes.getBody());
					}
					NodeList nodeList = JsonUtil.jsonToPojo(nodeRes.getBody(), NodeList.class);

					List<Node> nodes = nodeList.getItems();
//					Map<String, Map<String, Object>> res = new HashMap<String, Map<String, Object>>();
					List<Map<String, Object>> res = new ArrayList<>();
					if (nodes != null && nodes.size() > 0) {
						int cpu = 0;
						int mem = 0;
						for (Node node : nodes) {
							boolean bool = false;
							List<NodeCondition> conditions = node.getStatus().getConditions();
							for (NodeCondition nodeCondition : conditions) {
								if (nodeCondition.getType().equalsIgnoreCase("Ready")) {
									if(!nodeCondition.getStatus().equalsIgnoreCase("True")) {
										continue;
									}
								}
							}
//							if (bool) {
//								continue;
//							}
							double nodeFilesystemCapacity =  this.influxdbService.getClusterResourceUsage("node", "filesystem/limit", "nodename,resource_id",cluster, null, node.getMetadata().getName());
							Object object = node.getStatus().getAllocatable();
							if (object != null) {
								Map<String, Object> resourceMap = new HashMap<String, Object>();
								resourceMap.put("ip", node.getMetadata().getName());
								resourceMap.put("cpu", ((Map<String, Object>) object).get("cpu").toString());
								String memory = ((Map<String, Object>) object).get("memory").toString();
								memory = memory.substring(0, memory.indexOf("Ki"));
								double memoryDouble = Double.parseDouble(memory);
								resourceMap.put("memory", String.format("%.1f", memoryDouble/1024/1024));
								resourceMap.put("disk", String.format("%.0f", nodeFilesystemCapacity/1024/1024/1024));
                                res.add(resourceMap);
//								res.put(node.getMetadata().getName(), resourceMap);

							}
						}
					}

					mapClusterNodeInfo.put(cluster.getId() + "", res);

				}
			}


			return ActionReturnUtil.returnSuccessWithData(mapClusterNodeInfo);
		} catch (Exception e) {
			logger.error("Failed to get cluster allocated resources."+e.getMessage());
			return ActionReturnUtil.returnErrorWithMsg(e.getMessage());
		}
	}

}
