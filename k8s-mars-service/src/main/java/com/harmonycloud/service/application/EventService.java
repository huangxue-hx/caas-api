package com.harmonycloud.service.application;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.k8s.bean.EventList;

/**
 * 
 * @author jmi
 *
 */
public interface EventService {
	
	public ActionReturnUtil getEvents(String name, String namespace, String type) throws Exception;
	
	public ActionReturnUtil watchEvents(String name, String namespace, String type, String userName, Cluster cluster) throws Exception;
	
	public ActionReturnUtil listenEvents() throws Exception;

	public EventList getEvents(String name, String namespace, String type, Cluster cluster) throws Exception;

}
