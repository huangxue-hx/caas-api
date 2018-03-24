package com.harmonycloud.service.platform.service;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.service.platform.bean.monitor.InfluxdbQuery;

import java.util.List;

/**
 * 和influxdb相关的监控接口
 * 
 * @author jmi 2017-1-10
 *
 */
public interface InfluxdbService {
	
	public ActionReturnUtil podMonit(InfluxdbQuery query) throws Exception;
	
	public ActionReturnUtil nodeQuery(InfluxdbQuery query) throws Exception;
	
	public ActionReturnUtil getProcessStatus(String name, String processName, String clusterId) throws Exception;
	
	public ActionReturnUtil getAlarmList(String id) throws Exception;
	
	public ActionReturnUtil createThreshold(String processName, String measurement, String threshold, String alarmType, String alarmContact) throws Exception;
	
	public ActionReturnUtil deleteThreshold(String id) throws Exception;
	
	public ActionReturnUtil listThreshold(String id) throws Exception;

	public double getClusterResourceUsage(String type, String measurements, String groupBy, Cluster cluster, List<String> notWorkNodeList) throws Exception;

	public double getClusterResourceUsage(String type, String measurements, String groupBy, Cluster cluster, List<String> notWorkNodeList, String nodename) throws Exception;


	public double getClusterAllocatedResources(String type, String measurements, Cluster cluster) throws Exception;

//	public Map<String, Object> getClusterResourceUsage(String clusterId) throws  Exception;
}
