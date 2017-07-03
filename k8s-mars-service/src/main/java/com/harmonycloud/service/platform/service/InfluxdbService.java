package com.harmonycloud.service.platform.service;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.cluster.bean.Cluster;
import org.influxdb.dto.QueryResult;

import java.util.List;
import java.util.Map;

/**
 * 和influxdb相关的监控接口
 * 
 * @author jmi 2017-1-10
 *
 */
public interface InfluxdbService {
	
	public ActionReturnUtil podMonit(String rangeType, String startTime, String pod, String container, String target) throws Exception;
	
	public ActionReturnUtil nodeQuery(String type, String rangeType, String target, String name, String startTime, String processName) throws Exception;
	
	public ActionReturnUtil getProcessStatus(String name, String processName) throws Exception;
	
	public ActionReturnUtil getProviderList() throws Exception;
	
	public ActionReturnUtil getAlarmList(String id) throws Exception;
	
	public ActionReturnUtil createThreshold(String processName, String measurement, String threshold, String alarmType, String alarmContact) throws Exception;
	
	public ActionReturnUtil deleteThreshold(String id) throws Exception;
	
	public ActionReturnUtil listThreshold(String id) throws Exception;

	public double getClusterResourceUsage(String type, String measurements, String groupBy, Cluster cluster, List<String> notWorkNodeList) throws Exception;

	public double getClusterResourceUsage(String type, String measurements, String groupBy, Cluster cluster, List<String> notWorkNodeList, String nodename) throws Exception;


	public double getClusterAllocatedResources(String type, String measurements, Cluster cluster) throws Exception;

//	public Map<String, Object> getClusterResourceUsage(String clusterId) throws  Exception;
}
