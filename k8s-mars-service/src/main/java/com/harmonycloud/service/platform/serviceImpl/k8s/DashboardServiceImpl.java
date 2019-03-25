package com.harmonycloud.service.platform.serviceImpl.k8s;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.k8s.bean.*;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import com.harmonycloud.service.platform.bean.EventDetail;
import com.harmonycloud.service.platform.bean.PodDto;
import com.harmonycloud.service.platform.service.DashboardService;
import com.harmonycloud.service.platform.service.PodService;
import com.harmonycloud.service.system.SystemConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 需要用机器账号去获取
 * @author jmi
 *
 */

@Service
public class DashboardServiceImpl implements DashboardService {

	@Autowired
	private SystemConfigService systemConfigService;

	@Autowired
	private PodService podService;

	@SuppressWarnings("unchecked")
	@Override
	public ActionReturnUtil getPodInfo(Cluster cluster) throws Exception {

		// 获取pod
		K8SURL url = new K8SURL();
		url.setNamespace(null).setResource(Resource.POD);
		K8SClientResponse podRes = new K8sMachineClient().exec(url, HTTPMethod.GET, null, null, cluster);
		if (!HttpStatusUtil.isSuccessStatus(podRes.getStatus())) {
			return ActionReturnUtil.returnErrorWithMsg(podRes.getBody());
		}
		PodList podList = JsonUtil.jsonToPojo(podRes.getBody(), PodList.class);

		// 获取node
		url.setResource(Resource.NODE);
		K8SClientResponse nodeRes = new K8sMachineClient().exec(url, HTTPMethod.GET, null, null, cluster);
		if (!HttpStatusUtil.isSuccessStatus(nodeRes.getStatus())) {
			return ActionReturnUtil.returnErrorWithMsg(nodeRes.getBody());
		}
		NodeList nodeList = JsonUtil.jsonToPojo(nodeRes.getBody(), NodeList.class);
		Map<String, Object> res = new HashMap<String, Object>();
		res.put("podCount", podList.getItems().size());
		List<Node> nodes = nodeList.getItems();
		res.put("avaliablePods", 0);
		if (nodes != null && nodes.size() > 0) {
			int an = 0;
			for (Node node : nodes) {
				Object object = node.getStatus().getCapacity();
				if (object != null) {
					an += Integer.valueOf(((Map<String, Object>) object).get("pods").toString());
				}
			}
			res.put("avaliablePods", an);
		}
		return ActionReturnUtil.returnSuccessWithData(res);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ActionReturnUtil getInfraInfo(Cluster cluster) throws Exception {
		// 获取node
		K8SURL url = new K8SURL();
		url.setResource(Resource.NODE);
		K8SClientResponse nodeRes = new K8sMachineClient().exec(url, HTTPMethod.GET, null, null, cluster);
		if (!HttpStatusUtil.isSuccessStatus(nodeRes.getStatus())) {
			return ActionReturnUtil.returnErrorWithMsg(nodeRes.getBody());
		}
		NodeList nodeList = JsonUtil.jsonToPojo(nodeRes.getBody(), NodeList.class);
		List<Node> nodes = nodeList.getItems();
		Map<String, Object> res = new HashMap<String, Object>();
		res.put("hosts", nodeList.getItems().size());
		res.put("cpu", 0);
		res.put("memory", 0);
		if (nodes != null && nodes.size() > 0) {
			int cpu = 0;
			int mem = 0;
			for (Node node : nodes) {
				Object object = node.getStatus().getAllocatable();
				if (object != null) {
					cpu += Integer.valueOf(((Map<String, Object>) object).get("cpu").toString());
					String memory = ((Map<String, Object>) object).get("memory").toString();
					memory = memory.substring(0, memory.indexOf("Ki"));
					mem += Integer.valueOf(memory);
				}
			}
			res.put("cpu", cpu);
			res.put("memory", mem);
		}
		return ActionReturnUtil.returnSuccessWithData(res);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getNodeInfo(Cluster cluster, String nodename) throws Exception {
		// 获取node
		K8SURL url = new K8SURL();
		url.setResource(Resource.NODE).setSubpath(nodename);
		K8SClientResponse nodeRes = new K8sMachineClient().exec(url, HTTPMethod.GET, null, null, cluster);
		if (!HttpStatusUtil.isSuccessStatus(nodeRes.getStatus())) {
			return ActionReturnUtil.returnErrorWithMsg(nodeRes.getBody());
		}
		Map<String, Object> res = new HashMap<String, Object>();
		res.put("cpu", 0);
		res.put("memory", 0);

		Node node = JsonUtil.jsonToPojo(nodeRes.getBody(), Node.class);
		if(null != node) {
			Double cpu = 0.0;
			Double mem = 0.0;
			Double memGb = 0.0;
			Object object = node.getStatus().getAllocatable();
			if (object != null) {
				String cpuStr = ((Map<String, Object>) object).get("cpu").toString();
				if(cpuStr.contains("m")) {
					cpuStr = cpuStr.substring(0, cpuStr.indexOf("m"));
					cpu = Double.valueOf(cpuStr) / 1000;
				} else {
					cpu = Double.valueOf(cpuStr);
				}

				String memory = ((Map<String, Object>) object).get("memory").toString();
				double db = 0.0;
				if(memory.contains("Ki")) {
					memory = memory.substring(0, memory.indexOf("Ki"));
					mem += Double.valueOf(memory);
					db = Double.valueOf(memory)/1024/1024;
				} else if(memory.contains("Mi")) {
					memory = memory.substring(0, memory.indexOf("Mi"));
					mem += Double.valueOf(memory);
					db = Double.valueOf(memory)/1024;
				} else if(memory.contains("Gi")) {
					memory = memory.substring(0, memory.indexOf("Gi"));
					mem += Double.valueOf(memory);
					db = Double.valueOf(memory);
				}

				memGb += new BigDecimal(db).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();

				res.put("cpu", cpu);
				res.put("memory", mem);
				res.put("memoryGb", memGb);
			}
		}


		return res;
	}

	@Override
	public ActionReturnUtil getWarningInfo(Cluster cluster, String namespace) throws Exception {

		//获取警告事件
		K8SURL url = new K8SURL();
		url.setResource(Resource.EVENT).setNamespace(namespace);
		Map<String, Object> bodys = new HashMap<String, Object>();
		bodys.put("fieldSelector", "type=Warning");
		K8SClientResponse evRes = new K8sMachineClient().exec(url, HTTPMethod.GET, null, bodys, cluster);
		if (!HttpStatusUtil.isSuccessStatus(evRes.getStatus())) {
			return ActionReturnUtil.returnErrorWithMsg(evRes.getBody());
		}
		EventList eventList = JsonUtil.jsonToPojo(evRes.getBody(), EventList.class);
		Map<String, Object> res = new HashMap<String, Object>();
		List<Event> events = eventList.getItems();
		res.put("errCount", 0);
		if (events != null && events.size() > 0) {
			res.put("errCount", events.size());
			res.put("errMsgSample", events.get(0).getMessage());
		}
		return ActionReturnUtil.returnSuccessWithData(res);
	}

	@Override
	public ActionReturnUtil getEventInfo(Cluster cluster, String namespace) throws Exception {
		K8SURL url = new K8SURL();
		url.setResource(Resource.EVENT).setNamespace(namespace);
		K8SClientResponse evRes = new K8sMachineClient().exec(url, HTTPMethod.GET, null, null,cluster);
		if (!HttpStatusUtil.isSuccessStatus(evRes.getStatus())) {
			return ActionReturnUtil.returnErrorWithMsg(evRes.getBody());
		}
		EventList eventList = JsonUtil.jsonToPojo(evRes.getBody(), EventList.class);
		List<EventDetail> res = new ArrayList<EventDetail>();
		List<Event> events = eventList.getItems();
		if (events != null && events.size() > 0) {
			for (int i = 0; i < events.size() ;i++) {
				if(i > 10) {
					break;
				}
				Event e = events.get(i);
				EventDetail detail = new EventDetail(e.getReason(), e.getMessage(), e.getFirstTimestamp(), e.getLastTimestamp(), e.getCount(), e.getType());
				res.add(detail);
			}
		}
		return ActionReturnUtil.returnSuccessWithData(res);
	}

	//读取node的license
	@Override
	public ActionReturnUtil getNodeLicense() throws Exception {

//		String url = KubernatesHost.getKubeHost()+ "/api/v1/nodeLicense";
//
//		Map<String, Object>  headers = new HashMap<String, Object>();
//		headers.put("Authorization", "Bearer " + KubernatesHost.machineToken);
//		K8SClientResponse evRes = new K8SClientResponse();
//		evRes = HttpK8SClientUtil.httpGetRequest(url, headers, null);
//		if (!HttpStatusUtil.isSuccessStatus(evRes.getStatus())) {
//			return ActionReturnUtil.returnErrorWithMsg(evRes.getBody());
//		}
//		NodeLicense nodelicense =JsonUtil.jsonToPojo(evRes.getBody(), NodeLicense.class);
//		return  ActionReturnUtil.returnSuccessWithData(nodelicense);
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getInfraInfoWorkNode(Cluster cluster) throws Exception {
		// 获取node
		K8SURL url = new K8SURL();
		url.setResource(Resource.NODE);
		K8SClientResponse nodeRes = new K8sMachineClient().exec(url, HTTPMethod.GET, null, null, cluster);
		if (!HttpStatusUtil.isSuccessStatus(nodeRes.getStatus())) {
			return ActionReturnUtil.returnErrorWithMsg(nodeRes.getBody());
		}
		NodeList nodeList = JsonUtil.jsonToPojo(nodeRes.getBody(), NodeList.class);
		List<Node> nodes = nodeList.getItems();
		Map<String, Object> res = new HashMap<String, Object>();
		res.put("hosts", nodeList.getItems().size());
		res.put("cpu", 0);
		res.put("memory", 0);
		res.put("memoryGb", 0);
		if (nodes != null && nodes.size() > 0) {
			Double cpu = 0.0;
			Double mem = 0.0;
			Double memGb = 0.0;
			for (Node node : nodes) {
				Map<String, Object> labels = node.getMetadata().getLabels();
				//只累加共享节点的资源
				if (labels.get(CommonConstant.HARMONYCLOUD_STATUS) == null) {
					continue;
				}
				if ( !labels.get(CommonConstant.HARMONYCLOUD_STATUS).equals(CommonConstant.LABEL_STATUS_C)) {
					continue;
				}
				Object object = node.getStatus().getAllocatable();
				if (object != null) {
					String cpuStr = ((Map<String, Object>) object).get("cpu").toString();
					if(cpuStr.contains("m")) {
						cpuStr = cpuStr.substring(0, cpuStr.indexOf("m"));
						cpu += Double.valueOf(cpuStr) / 1000;
					} else {
						cpu += Double.valueOf(cpuStr);
					}

					String memory = ((Map<String, Object>) object).get("memory").toString();
					double db = 0.0;
					if(memory.contains("Ki")) {
						memory = memory.substring(0, memory.indexOf("Ki"));
						mem += Double.valueOf(memory);
						db = Double.valueOf(memory)/1024/1024;
					} else if(memory.contains("Mi")) {
						memory = memory.substring(0, memory.indexOf("Mi"));
						mem += Double.valueOf(memory);
						db = Double.valueOf(memory)/1024;
					} else if(memory.contains("Gi")) {
						memory = memory.substring(0, memory.indexOf("Gi"));
						mem += Double.valueOf(memory);
						db = Double.valueOf(memory);
					}

					memGb += new BigDecimal(db).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
				}
			}
			cpu = new BigDecimal(cpu).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();

			NumberFormat nf = NumberFormat.getNumberInstance();
			// 保留两位小数
			nf.setMaximumFractionDigits(2);
			nf.setGroupingUsed(false);
			res.put("cpu", cpu %1==0?cpu :nf.format(cpu ));
			res.put("memory", mem%1==0?mem :nf.format(mem));
			res.put("memoryGb", memGb%1==0?memGb:nf.format(memGb ));
		}
		return res;
	}

	@Override
	public ActionReturnUtil listK8sComponentPod(Cluster cluster, String podName, String namespace) throws Exception {
		List<PodDto> podList = podService.getPodListByNamespace(cluster, namespace);
		if (podList == null) {
			throw new MarsRuntimeException(ErrorCodeMessage.POD_NOT_EXIST);
		}
		List<PodDto> resultList = new ArrayList<>();
		for (PodDto pod : podList) {
			String[] pArray = podName.split(",");
			for (String p : pArray) {
				if (pod.getName().startsWith(p)) {
					resultList.add(pod);
				}
			}
		}
		return ActionReturnUtil.returnSuccessWithData(resultList);
	}

	@Override
	public ActionReturnUtil getComponentPodDetail(Cluster cluster, String name, String namespace) throws Exception {
		Map<String, Object> podDetail = podService.getPodDetail(namespace, name, cluster);
		return ActionReturnUtil.returnSuccessWithData(podDetail);
	}

}
