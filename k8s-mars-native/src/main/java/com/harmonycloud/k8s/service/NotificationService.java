package com.harmonycloud.k8s.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.k8s.bean.Event;
import com.harmonycloud.k8s.bean.EventList;
import com.harmonycloud.k8s.bean.K8sMessage;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;

@Service
public class NotificationService {
	
	/**
	 * 
	 * @param namespace
	 * @param type
	 * @param resourceVersion 最新版本号
	 * @return
	 * @throws IOException
	 */
	public List<K8sMessage> getK8sMessage(String namespace,String type,String resourceVersion) throws IOException{
		/*URL url = new URL("http://10.10.102.25:8080/api/v1/namespaces/testyjyj-casd/events?watch=true&timeoutSeconds=15&resourceVersion=3743458");
		URLConnection urlConnection = url.openConnection();
		HttpURLConnection httpUrlConnection = (HttpURLConnection) urlConnection;
		httpUrlConnection.setRequestMethod("GET");
		httpUrlConnection.setDoOutput(true);
		httpUrlConnection.setDoInput(true);
		httpUrlConnection.setConnectTimeout(5000);
		httpUrlConnection.connect();
		InputStream inputStream = httpUrlConnection.getInputStream();
		int i;
		StringBuffer buffer = null;
		List<Event> event = new ArrayList<>();
		while( (i = inputStream.read()) != -1){
			//处理返回的值
			if(buffer == null){
				buffer = new StringBuffer();
			}
			if(i != 10){
				buffer.append((char) i);
			}else{
				event.add(JsonUtil.jsonToPojo(buffer.toString(), Event.class));
				buffer = null;
			}
			System.out.println(event.size());
		}
		return null;*/
		K8SURL url = new K8SURL();
		Map<String, Object> head = new HashMap<String, Object>();
		Map<String, Object> queryParams = new HashMap<>();
		queryParams.put("watch", "true");
		head.put("Content-Type", "application/json");
		url.setResource(Resource.EVENT).setNamespace(namespace).setQueryParams(queryParams);
		K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET, null, null);
		EventList eventList = K8SClient.converToBean(response, EventList.class);
		List<Event> items = eventList.getItems();
		List<K8sMessage> messages = new ArrayList<>();
		for (Event event : items) {
			if(Integer.valueOf(event.getInvolvedObject().getResourceVersion()) > Integer.valueOf(resourceVersion)){
				K8sMessage message = new K8sMessage();
				message.setMessage(event.getMessage());
				messages.add(message);
			}
		}
		System.out.println(messages.size());
		System.out.println(JsonUtil.objectToJson(messages));
		return messages;
	}
	
	public static void main(String[] args) throws IOException {
		new NotificationService().getK8sMessage("testyjyj-casd", null, "3745555");
	}
}
