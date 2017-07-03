package com.harmonycloud.service.platform.service;

import java.util.Map;

import com.harmonycloud.dao.cluster.bean.Cluster;

public interface WatchService {
	
	public boolean watch(Map<String, String> field, String kind, String rv, String userName, Cluster cluster) throws Exception;
	
	public String getLatestVersion(String namespace,Map<String, Object> header, Cluster cluster) throws Exception;
	
	public String listenMessage() throws Exception;

}
