package com.harmonycloud.service.platform.serviceImpl.k8s;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.dao.system.bean.SystemConfig;
import com.harmonycloud.k8s.bean.Event;
import com.harmonycloud.k8s.bean.EventList;
import com.harmonycloud.k8s.bean.Node;
import com.harmonycloud.k8s.bean.NodeList;
import com.harmonycloud.k8s.bean.PodList;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import com.harmonycloud.service.platform.bean.EventDetail;
import com.harmonycloud.service.platform.service.DashboardService;
import com.harmonycloud.service.system.SuperSaleService;
import com.harmonycloud.service.system.SystemConfigService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

//import com.harmonycloud.k8s.util.HttpK8SClientUtil;
//import com.harmonycloud.k8s.util.KubernatesHost;

/**
 * 需要用机器账号去获取
 * @author jmi
 *
 */

@Service
public class DashboardServiceImpl implements DashboardService {

	@Autowired
	SystemConfigService systemConfigService;

	@Autowired
	SuperSaleService superSaleService;

	@SuppressWarnings("unchecked")
	@Override
	public ActionReturnUtil getPodInfo() throws Exception {

		// 获取pod
		K8SURL url = new K8SURL();
		url.setNamespace(null).setResource(Resource.POD);
		K8SClientResponse podRes = new K8sMachineClient().exec(url, HTTPMethod.GET, null, null);
		if (!HttpStatusUtil.isSuccessStatus(podRes.getStatus())) {
			return ActionReturnUtil.returnErrorWithMsg(podRes.getBody());
		}
		System.out.println(podRes.getBody());
		PodList podList = JsonUtil.jsonToPojo(podRes.getBody(), PodList.class);

		// 获取node
		url.setResource(Resource.NODE);
		K8SClientResponse nodeRes = new K8sMachineClient().exec(url, HTTPMethod.GET, null, null);
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
	public ActionReturnUtil getInfraInfo() throws Exception {
		Map<String, Object> res = new HashMap<String, Object>();
		this.getInfraInfo(null);
		return ActionReturnUtil.returnSuccessWithData(res);

//		// 获取node
//		K8SURL url = new K8SURL();
//		url.setResource(Resource.NODE);
//		K8SClientResponse nodeRes = new K8sMachineClient().exec(url, HTTPMethod.GET, null, null);
//		if (!HttpStatusUtil.isSuccessStatus(nodeRes.getStatus())) {
//			return ActionReturnUtil.returnErrorWithMsg(nodeRes.getBody());
//		}
//		NodeList nodeList = JsonUtil.jsonToPojo(nodeRes.getBody(), NodeList.class);
//		List<Node> nodes = nodeList.getItems();
//		Map<String, Object> res = new HashMap<String, Object>();
//		res.put("hosts", nodeList.getItems().size());
//		res.put("cpu", 0);
//		res.put("memory", 0);
//		if (nodes != null && nodes.size() > 0) {
//			int cpu = 0;
//			int mem = 0;
//			for (Node node : nodes) {
//				Object object = node.getStatus().getAllocatable();
//				if (object != null) {
//					cpu += Integer.valueOf(((Map<String, Object>) object).get("cpu").toString());
//					String memory = ((Map<String, Object>) object).get("memory").toString();
//					memory = memory.substring(0, memory.indexOf("Ki"));
//					mem += Integer.valueOf(memory);
//				}
//			}
//			res.put("cpu", cpu);
//			res.put("memory", mem);
//		}
//		return ActionReturnUtil.returnSuccessWithData(res);
	}

	@Override
	public Map<String, Object> getInfraInfo(Cluster cluster) throws Exception {
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
		return res;
	}

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
				cpu += Integer.valueOf(((Map<String, Object>) object).get("cpu").toString());
				String memory = ((Map<String, Object>) object).get("memory").toString();
				memory = memory.substring(0, memory.indexOf("Ki"));
				mem += Double.valueOf(memory);

				double db = Double.valueOf(memory)/1024/1024;
				memGb += new BigDecimal(db).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();

				res.put("cpu", cpu);
				res.put("memory", mem);
				res.put("memoryGb", memGb);
			}
		}


		return res;
	}

	@Override
	public ActionReturnUtil getWarningInfo(String namespace) throws Exception {

		//获取警告事件
		K8SURL url = new K8SURL();
		url.setResource(Resource.EVENT).setNamespace(namespace);
		Map<String, Object> bodys = new HashMap<String, Object>();
		bodys.put("fieldSelector", "type=Warning");
		K8SClientResponse evRes = new K8sMachineClient().exec(url, HTTPMethod.GET, null, bodys);
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
	public ActionReturnUtil getEventInfo(String namespace) throws Exception {
		K8SURL url = new K8SURL();
		url.setResource(Resource.EVENT).setNamespace(namespace);
		K8SClientResponse evRes = new K8sMachineClient().exec(url, HTTPMethod.GET, null, null);
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
	public ActionReturnUtil getＮodeLicense() throws Exception {

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
				if (labels.get(CommonConstant.HARMONYCLOUD_STATUS) != null
						&& node.getMetadata().getLabels().get(CommonConstant.HARMONYCLOUD_STATUS).equals(CommonConstant.LABEL_STATUS_A)) {
					continue;
				}
				if (labels.get(CommonConstant.HARMONYCLOUD_STATUS) != null
						&& node.getMetadata().getLabels().get(CommonConstant.HARMONYCLOUD_STATUS).equals(CommonConstant.LABEL_STATUS_B)) {
					continue;
				}
				if (labels.get(CommonConstant.MASTERNODELABEL) != null) {
					continue;
				}

				Object object = node.getStatus().getAllocatable();
				if (object != null) {
					cpu += Double.valueOf(((Map<String, Object>) object).get("cpu").toString());
					String memory = ((Map<String, Object>) object).get("memory").toString();
					memory = memory.substring(0, memory.indexOf("Ki"));
					mem += Double.valueOf(memory);
					double db = Double.valueOf(memory)/1024/1024;
					memGb += new BigDecimal(db).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();


				}
			}
			cpu = new BigDecimal(cpu).setScale(0, BigDecimal.ROUND_HALF_UP).doubleValue();

			SystemConfig byId = systemConfigService.findById("40");
			String configValue = byId.getConfigValue();
			Double superSaleRate;
			superSaleRate = superSaleService.addSuperSaleRate(Double.parseDouble(configValue));
			res.put("cpu", cpu * superSaleRate);
			res.put("memory", mem * superSaleRate);
			res.put("memoryGb", memGb * superSaleRate);
		}
		return res;
	}

}
