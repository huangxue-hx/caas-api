package com.harmonycloud.service.application.impl;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.k8s.bean.Event;
import com.harmonycloud.k8s.bean.EventList;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import com.harmonycloud.service.application.EventService;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.platform.service.WatchService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Service
public class EventServiceImpl implements EventService {
	
	@Autowired
	private WatchService watchService;
	@Autowired
	ClusterService clusterService;

	@Override
	public ActionReturnUtil getEvents(String name, String namespace, String type) throws Exception {
		K8SURL url = new K8SURL();
		url.setResource(Resource.EVENT);
		Map<String, Object> bodys = new HashMap<String, Object>();
		String query = "";
		if (!StringUtils.isEmpty(name)) {
			query = "involvedObject.name="+name+",";
		}
		if (!StringUtils.isEmpty(namespace)) {
			query = "involvedObject.namespace="+namespace+",";
		}
		if (!StringUtils.isEmpty(type)) {
			query = "type="+type+",";
		}
		if (!StringUtils.isEmpty(query)) {
			bodys.put("fieldSelector", query.substring(0, query.length()-1));
		}
		K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET, null, bodys);
		if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
			return ActionReturnUtil.returnErrorWithMsg(response.getBody());
		}
		EventList eventList = JsonUtil.jsonToPojo(response.getBody(), EventList.class);
		return ActionReturnUtil.returnSuccessWithData(eventList.getItems());
	}


	@Override
	public EventList getEvents(String name, String namespace, String type, Cluster cluster) throws Exception {
		K8SURL url = new K8SURL();
		url.setResource(Resource.EVENT);
		Map<String, Object> bodys = new HashMap<String, Object>();
		String query = "";
		if (!StringUtils.isEmpty(name)) {
			query = "involvedObject.name="+name+",";
		}
		if (!StringUtils.isEmpty(namespace)) {
			query = "involvedObject.namespace="+namespace+",";
		}
		if (!StringUtils.isEmpty(type)) {
			query = "type="+type+",";
		}
		if (!StringUtils.isEmpty(query)) {
			bodys.put("fieldSelector", query.substring(0, query.length()-1));
		}
		K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET, null, bodys, cluster);

		EventList eventList = JsonUtil.jsonToPojo(response.getBody(), EventList.class);
		return eventList;
	}

	@Override
	public EventList getEvents(Map<String, String> fieldSelector, Cluster cluster) throws Exception {
		K8SURL url = new K8SURL();
		url.setResource(Resource.EVENT);
		Map<String, Object> bodys = new HashMap<String, Object>();
		StringBuffer query = new StringBuffer();
		for(Map.Entry<String, String> selector : fieldSelector.entrySet()){
			String key = selector.getKey();
			String value = selector.getValue();
			if(StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value)){
				query.append(key + "=" + value + ",");
			}
		}
		if (query.length() > 0) {
			String selector = query.toString();
			bodys.put("fieldSelector", selector.substring(0, selector.length()-1));
		}
		K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET, null, bodys, cluster);
		EventList eventList = JsonUtil.jsonToPojo(response.getBody(), EventList.class);
		return eventList;
	}

	@Override
	public Map<String,List<Event>> getNodeRestartEvents() throws Exception {
		List<Cluster> clusters = clusterService.listCluster();
		if (CollectionUtils.isEmpty(clusters)) {
			return Collections.emptyMap();
		}
		Map<String,List<Event>> events = new HashMap<>();
		Map<String, String> eventQueryMap = new HashMap<String, String>();
		eventQueryMap.put("involvedObject.kind", "Node");
		eventQueryMap.put("type", "Warning");
		eventQueryMap.put("reason", "Rebooted");
		for(Cluster cluster : clusters){
			EventList eventResult = this.getEvents(eventQueryMap, cluster);
			if(eventResult != null && !CollectionUtils.isEmpty(eventResult.getItems())){
				events.put(cluster.getName(), eventResult.getItems());
			}
		}
		return events;
	}

	@Override
	public ActionReturnUtil watchEvents(String name, String namespace, String type, String userName, Cluster cluster) throws Exception {
		Map<String, String> field = new HashMap<String, String>();
		field.put("involvedObject.name", name);
		field.put("involvedObject.namespace", namespace);
		field.put("type", type);
		if (watchService.watch(field, null, null, userName, cluster)) {
			return ActionReturnUtil.returnSuccess();
		} else {
			return ActionReturnUtil.returnError();
		}
	}

	@Override
	public ActionReturnUtil listenEvents() throws Exception {
		String id = watchService.listenMessage();
		Map<String, Object> res = new HashMap<String, Object>();
		res.put("listener", id);
		return ActionReturnUtil.returnSuccessWithData(res);
	}

}
