package com.harmonycloud.service.application.impl;

import com.alibaba.fastjson.JSONObject;
import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.CollectionUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.common.util.date.DateStyle;
import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.k8s.bean.cluster.Cluster;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.SimpleDateFormat;
import java.util.*;

import static com.harmonycloud.common.Constant.CommonConstant.EVENT_TYPE_NORMAL;
import static com.harmonycloud.common.Constant.CommonConstant.EVENT_TYPE_WARNING;

@Service
public class EventServiceImpl implements EventService {

	private static final Logger LOGGER = LoggerFactory.getLogger(EventServiceImpl.class);
	private static final int EVENTCOUNT = 30;
	private static final int HOURS_1 = 1;
	private static final int HOURS_3 = 3;
	private static final int HOURS_7 = 7;
	private static final int HOURS_8 = 8;
	private static final int HOURS_12 = 12;
	private static final int HOURS_24 = 24;
	
	@Autowired
	private WatchService watchService;
	@Autowired
	ClusterService clusterService;

	@Override
	public EventList getEvents(String name, String namespace, String type, String clusterId) throws Exception {
		return this.getEvents(name,namespace,type,clusterService.findClusterById(clusterId));
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
		if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
			LOGGER.error("get events response:{},name:{},namespace:{},type:{},clusterId:{}",
					new String[]{JSONObject.toJSONString(response),name,namespace,type,cluster.getName()});
			return null;
		}
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
	public Map<String, Object> getEventOverview(String name, String namespace, String type, String clusterId) throws Exception{

		Date currentTime = new Date();

		EventList eventList = this.getEvents(name, namespace, type, clusterId);
        if(eventList == null){
        	return Collections.emptyMap();
		}
		List<Event> eList = eventList.getItems();
		Map<String, Object> map = new LinkedHashMap<>();
		Map<String, List<String[]>> eventMap = new LinkedHashMap<>();
		Map<String, Integer> eventCount = new LinkedHashMap<>();
		Map<String, List<String[]>> events = new HashMap<>();
		for(Event event : eList) {
			String lastTimestamp = event.getLastTimestamp().replace("T", " ").replace("Z", "");
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(DateUtil.StringToDate(lastTimestamp, DateStyle.YYYY_MM_DD_HH_MM_SS));
			calendar.add(Calendar.HOUR, 8);//+8时区
			Date inputDate = calendar.getTime();
			String inputDateStr = DateUtil.DateToString(inputDate, DateStyle.YYYY_MM_DD_HH_MM_SS);

			if (DateUtil.addHour(inputDate, HOURS_1).after(currentTime)) {
				this.setEvent(HOURS_1, event, inputDateStr, events);
				continue;
			}
			if (DateUtil.addHour(inputDate, HOURS_3).after(currentTime)) {
				this.setEvent(HOURS_3, event, inputDateStr, events);
				continue;
			}
			if (DateUtil.addHour(inputDate, HOURS_7).after(currentTime)) {
				this.setEvent(HOURS_7, event, inputDateStr, events);
				continue;
			}
			if (DateUtil.addHour(inputDate, HOURS_12).after(currentTime)) {
				this.setEvent(HOURS_12, event, inputDateStr, events);
				continue;
			}
			if (DateUtil.addHour(inputDate, HOURS_24).after(currentTime)) {
				this.setEvent(HOURS_24, event, inputDateStr, events);
				continue;
			}

		}

		eventCount.put("oneHourWarning", events.get(HOURS_1+CommonConstant.LINE + EVENT_TYPE_WARNING).size());
		eventCount.put("oneHourNormal", events.get(HOURS_1+CommonConstant.LINE + EVENT_TYPE_NORMAL).size());
		eventCount.put("threeHourWarning", events.get(HOURS_3+CommonConstant.LINE + EVENT_TYPE_WARNING).size());
		eventCount.put("threeHourNormal",  events.get(HOURS_3+CommonConstant.LINE + EVENT_TYPE_NORMAL).size());
		eventCount.put("sevenHourWarning", events.get(HOURS_7+CommonConstant.LINE + EVENT_TYPE_WARNING).size());
		eventCount.put("sevenHourNormal", events.get(HOURS_7+CommonConstant.LINE + EVENT_TYPE_NORMAL).size());
		eventCount.put("twelveHourWarning", events.get(HOURS_12+CommonConstant.LINE + EVENT_TYPE_WARNING).size());
		eventCount.put("twelveHourNormal", events.get(HOURS_12+CommonConstant.LINE + EVENT_TYPE_NORMAL).size());
		eventCount.put("twentyFourHourWarning", events.get(HOURS_24+CommonConstant.LINE + EVENT_TYPE_WARNING).size());
		eventCount.put("twentyFourHourNormal", events.get(HOURS_24+CommonConstant.LINE + EVENT_TYPE_NORMAL).size());

		eventMap.put("oneHourWarning", CollectionUtil.limitCount(events.get(HOURS_1+CommonConstant.LINE + EVENT_TYPE_WARNING),EVENTCOUNT));
		eventMap.put("oneHourNormal", CollectionUtil.limitCount(events.get(HOURS_1+CommonConstant.LINE + EVENT_TYPE_NORMAL),EVENTCOUNT));
		eventMap.put("threeHourWarning", CollectionUtil.limitCount(events.get(HOURS_3+CommonConstant.LINE + EVENT_TYPE_WARNING),EVENTCOUNT));
		eventMap.put("threeHourNormal", CollectionUtil.limitCount(events.get(HOURS_3+CommonConstant.LINE + EVENT_TYPE_NORMAL),EVENTCOUNT));
		eventMap.put("sevenHourWarning", CollectionUtil.limitCount(events.get(HOURS_7+CommonConstant.LINE + EVENT_TYPE_WARNING),EVENTCOUNT));
		eventMap.put("sevenHourNormal", CollectionUtil.limitCount(events.get(HOURS_7+CommonConstant.LINE + EVENT_TYPE_NORMAL),EVENTCOUNT));
		eventMap.put("twelveHourWarning", CollectionUtil.limitCount(events.get(HOURS_12+CommonConstant.LINE + EVENT_TYPE_WARNING),EVENTCOUNT));
		eventMap.put("twelveHourNormal", CollectionUtil.limitCount(events.get(HOURS_12+CommonConstant.LINE + EVENT_TYPE_NORMAL),EVENTCOUNT));
		eventMap.put("twentyFourHourWarning", CollectionUtil.limitCount(events.get(HOURS_24+CommonConstant.LINE + EVENT_TYPE_WARNING),EVENTCOUNT));
		eventMap.put("twentyFourHourNormal", CollectionUtil.limitCount(events.get(HOURS_24+CommonConstant.LINE + EVENT_TYPE_NORMAL),EVENTCOUNT));

		map.put("eventMap", eventMap);
		map.put("eventCount",eventCount);

		return map;

	}

	@Override
	public ActionReturnUtil watchEvents(String name, String namespace, String type, String userName, String clusterId) throws Exception {
		Map<String, String> field = new HashMap<String, String>();
		field.put("involvedObject.name", name);
		field.put("involvedObject.namespace", namespace);
		field.put("type", type);
		if (watchService.watch(field, null, null, userName, clusterService.findClusterById(clusterId))) {
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

	private void setEvent(int hourType, Event event, String eventDate, Map<String, List<String[]>> events){
		String[] message = {eventDate, event.getMessage()};
		if(EVENT_TYPE_WARNING.equalsIgnoreCase(event.getType())) {
			this.setEvent(hourType + CommonConstant.LINE + EVENT_TYPE_WARNING, message, events);
		} else if (EVENT_TYPE_NORMAL.equalsIgnoreCase(event.getType())) {
			this.setEvent(hourType + CommonConstant.LINE + EVENT_TYPE_NORMAL, message, events);
		}
	}

	private void setEvent(String key, String[] message, Map<String, List<String[]>> events){
		if(events.get(key) == null){
			List<String[]> eventMessages = new ArrayList<>();
			eventMessages.add(message);
			events.put(key, eventMessages);
		}else {
			events.get(key).add(message);
		}
	}

}
