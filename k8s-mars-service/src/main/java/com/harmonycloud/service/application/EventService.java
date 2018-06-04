package com.harmonycloud.service.application;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.bean.Event;
import com.harmonycloud.k8s.bean.EventList;

import java.util.List;
import java.util.Map;

/**
 * 
 * @author jmi
 *
 */
public interface EventService {
	
	public EventList getEvents(String name, String namespace, String type, String clusterId) throws Exception;
	
	public ActionReturnUtil watchEvents(String name, String namespace, String type, String userName, String clusterId) throws Exception;
	
	public ActionReturnUtil listenEvents() throws Exception;

	public EventList getEvents(String name, String namespace, String type, Cluster cluster) throws Exception;

	EventList getEvents(Map<String, String> fieldSelector, Cluster cluster) throws Exception;

	Map<String,List<Event>> getNodeRestartEvents() throws Exception;

	Map<String, Object> getEventOverview(String name, String namespace, String type, String clusterId) throws Exception;

}
