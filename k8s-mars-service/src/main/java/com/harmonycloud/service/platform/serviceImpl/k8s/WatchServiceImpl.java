package com.harmonycloud.service.platform.serviceImpl.k8s;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.bean.Event;
import com.harmonycloud.k8s.bean.PodList;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.service.EventService;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import com.harmonycloud.service.platform.dto.EventDto;
import com.harmonycloud.service.platform.service.WatchService;
import com.harmonycloud.service.platform.socketio.message.Notification;
import com.harmonycloud.service.platform.socketio.server.SocketIOConfig;

@Service
public class WatchServiceImpl implements WatchService{
	
	private Map<String, Long> watchList = new HashMap<String, Long>();
	
	private List<String> listeners = new ArrayList<String>();

	@Autowired
	SocketIOConfig socketIOConfig;
	@Autowired
	EventService eventService;

	@Override
	public boolean watch(Map<String, String> field, String kind, String resourceVersion, String userName, Cluster cluster) throws Exception {
		
		//判断kind是否为空
		String resourceKind = kind;
		if (StringUtils.isEmpty(kind) && StringUtils.isBlank(kind)) {
			resourceKind = "events";
		}
		//获取token
		String token = String.valueOf(K8SClient.tokenMap.get(userName));
		Map<String, Object> headers = new HashMap<>();
		headers.put("Authorization", "Bearer " + token);
		
		String name = field.get("involvedObject.name");
		String namespace = field.get("involvedObject.namespace");
		if (checkDuplicate(name, namespace, resourceKind)) {
			String rv = getLatestVersion(namespace,headers, cluster);
			Map<String, Object> body = new HashMap<String, Object>();
			String selector = "";
			for (Map.Entry<String, String> m :field.entrySet())  {  
				selector+=m.getKey()+'='+m.getValue()+',';
	        }
			body.put("fieldSelector", selector);
			body.put("watch", "true");
			body.put("timeoutSeconds", 3);
			if (StringUtils.isEmpty(resourceVersion) && StringUtils.isBlank(resourceVersion)) {
				body.put("resourceVersion", rv);
			} else {
				body.put("resourceVersion", resourceVersion);
			}

			K8SClientResponse eventResponse = eventService.doEventByNamespace(namespace, headers, body, HTTPMethod.GET, cluster);
			if(HttpStatusUtil.isSuccessStatus(eventResponse.getStatus()) &&
				StringUtils.isNotEmpty(eventResponse.getBody()) &&
					StringUtils.isNotEmpty(eventResponse.getBody()) &&
					eventResponse.getBody().contains("object")){
				EventDto eventDto = K8SClient.converToBean(eventResponse, EventDto.class);
//				List<Event> events = response.getItems();
				if (null != eventDto &&
					null != eventDto.getObject()) {
					Event event = eventDto.getObject();
					getMsg(event);
					Notification notification = generateNotification(event);

					socketIOConfig.sendMessageToOneClient(userName, CommonConstant.NOTI, notification);
				}
			}

		}
		return true;
	}
	
	@Override
	public String getLatestVersion(String namespace,Map<String, Object> header, Cluster cluster) throws Exception {
		//根据一个比较常用的方法去获取最后一个resourceVersion。每个请求都会产生一个。随之增加。
		K8SURL url = new K8SURL();
		url.setNamespace(namespace).setResource(Resource.POD);
		
		//请求k8sclient
		PodList podList = K8SClient.converToBean(new K8sMachineClient().exec(url, HTTPMethod.GET, header, null, cluster), PodList.class);
		return podList.getMetadata().getResourceVersion();
	}
	
	//查重
	private boolean checkDuplicate(String name, String namespace, String kind) throws Exception {
		String str = kind + name + namespace;
		Long time = 0L;
		if (watchList != null && !watchList.isEmpty()) {
			 time = watchList.get(str)==null ? 0l : watchList.get(str);
		}
		Long now = new Date().getTime();
		if (time > 0) {
			time = now - time;
			if (time > 3000) {
				watchList.put(str, now);
			} else {
				return false;
			}
		} else {
			watchList.put(str, now);
		}
		return true;
	}
	
	
	private Map<String, Object> getMsg(Event event) {
		Map<String, Object> msg = new HashMap<String, Object>();
		msg.put("type", event.getType());
		msg.put("time", event.getLastTimestamp());
		msg.put("message", event.getMessage());
		msg.put("title", event.getReason());
		msg.put("target", event.getInvolvedObject());
		return msg;
	}

	private Notification generateNotification(Event event){

		Notification notification = new Notification();
		notification.setType(event.getType());
		notification.setTime(event.getLastTimestamp());
		notification.setMessage(event.getMessage());
		notification.setTitle(event.getReason());
		notification.setTarget(event.getInvolvedObject());
		return notification;
	}

	@Override
	public String listenMessage() throws Exception {
		String rd = "lis"+ Math.ceil(Math.random()*100000) + new Date().getTime();
		listeners.add(rd);
		return rd;
	}
}
