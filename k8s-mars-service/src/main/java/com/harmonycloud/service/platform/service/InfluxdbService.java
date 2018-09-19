package com.harmonycloud.service.platform.service;

import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.service.platform.bean.monitor.InfluxdbQuery;
import org.influxdb.dto.QueryResult;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * 和influxdb相关的监控接口
 * 
 * @author jmi 2017-1-10
 *
 */
public interface InfluxdbService {
	
	public ActionReturnUtil podMonit(InfluxdbQuery query) throws ParseException,IOException,NoSuchAlgorithmException,KeyManagementException;
	
	public ActionReturnUtil nodeQuery(InfluxdbQuery query) throws IOException,NoSuchAlgorithmException,KeyManagementException;
	
	public ActionReturnUtil getProcessStatus(String name, String processName, String clusterId) throws IOException,NoSuchAlgorithmException,KeyManagementException;
	
	public ActionReturnUtil getAlarmList(String id) throws URISyntaxException,IOException;
	
	public ActionReturnUtil createThreshold(String processName, String measurement, String threshold, String alarmType, String alarmContact) throws IOException;
	
	public ActionReturnUtil deleteThreshold(String id) throws IOException;
	
	public ActionReturnUtil listThreshold(String id) throws URISyntaxException,IOException;

	public Map<String,List<QueryResult.Series>> getClusterResourceUsage(String type, String measurements, String groupBy, Cluster cluster, List<String> notWorkNodeList) throws MarsRuntimeException;

	public double getClusterResourceUsage(String type, String measurements, String groupBy, Cluster cluster, List<String> notWorkNodeList, String nodename) throws MarsRuntimeException;

    public double getPvResourceUsage(String measurements, Cluster cluster, String pvcName);

    public Double computeNodeInfo(List<QueryResult.Series> series) throws MarsRuntimeException;

	public double getClusterAllocatedResources(String type, String measurements, Cluster cluster) throws MarsRuntimeException;

	ActionReturnUtil getClusterNodeInfo(String clusterId) throws MarsRuntimeException;
}
