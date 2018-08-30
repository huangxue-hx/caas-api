package com.harmonycloud.service.platform.serviceImpl.monitor;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

import com.harmonycloud.common.enumm.*;
import com.harmonycloud.common.util.date.DateStyle;
import com.harmonycloud.k8s.bean.Node;
import com.harmonycloud.k8s.bean.NodeCondition;
import com.harmonycloud.k8s.bean.NodeList;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.platform.bean.monitor.InfluxdbQuery;
import com.harmonycloud.service.platform.client.InfluxDBClient;
import org.apache.commons.lang3.StringUtils;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.HttpClientResponse;
import com.harmonycloud.common.util.HttpClientUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.service.platform.service.InfluxdbService;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpSession;

/**
 * influxdb service:查询监控信息
 *
 *
 * @author jmi
 *
 */
@Service
public class InfluxdbServiceImpl implements InfluxdbService{

	@Autowired
	private ClusterService clusterService;
	@Autowired
	HttpSession session;
	//监控数据最大展示100个监控点
    private static final int MAX_MONITOR_POINT = 100;
    private String nodeName = "nodename";


	public ActionReturnUtil podMonit(InfluxdbQuery query) throws Exception {
		String interval = "";
		String range = "";
		Cluster cluster = clusterService.findClusterById(query.getClusterId());
		if(query.getRangeType().equals(EnumMonitorQuery.FROM_START.getCode())){
			if(StringUtils.isBlank(query.getStartTime())){
				return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.INVALID_PARAMETER, DictEnum.CREATE_TIME.phrase(), true);
			}
			SimpleDateFormat adf = null;
			if(query.getStartTime().length()>20){
				adf = new SimpleDateFormat(DateStyle.YYYY_MM_DD_T_HH_MM_SS_Z_SSS.getValue());
			}else {
				adf = new SimpleDateFormat(DateStyle.YYYY_MM_DD_T_HH_MM_SS_Z.getValue());
			}
			adf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date startDate = adf.parse(query.getStartTime());
			Long timeInterval = System.currentTimeMillis() - startDate.getTime();
			//如果创建时间在30天内，就用目前固定的几种时间查询类型，超过30天，以100个点作为最大的点数显示
			if(timeInterval <= EnumMonitorQuery.THIRTY_DAY.getMillisecond()){
				String newRangeType;
				if(timeInterval <= EnumMonitorQuery.TEN_MINUTE.getMillisecond()){
					newRangeType = EnumMonitorQuery.TEN_MINUTE.getCode();
				}else if(timeInterval <= EnumMonitorQuery.SIX_HOUR.getMillisecond()){
					newRangeType = EnumMonitorQuery.SIX_HOUR.getCode();
				}else if(timeInterval <= EnumMonitorQuery.ONE_DAY.getMillisecond()){
					newRangeType = EnumMonitorQuery.ONE_DAY.getCode();
				}else if(timeInterval <= EnumMonitorQuery.SEVEN_DAY.getMillisecond()){
					newRangeType = EnumMonitorQuery.SEVEN_DAY.getCode();
				}else {
					newRangeType = EnumMonitorQuery.THIRTY_DAY.getCode();
				}
				EnumMonitorQuery monitorQuery = EnumMonitorQuery.getRangeData(newRangeType);
				interval = monitorQuery.getInterval();
				range = monitorQuery.getRange();
			}else{
				Long timeIntervalInDays = timeInterval / (1000 * 60 * 60 * 24);
				range = timeIntervalInDays +"d";
				interval = (timeIntervalInDays/MAX_MONITOR_POINT + 1) + "d";
			}
		}else {
			EnumMonitorQuery monitorQuery = EnumMonitorQuery.getRangeData(query.getRangeType());
			if (monitorQuery == null) {
				return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.INVALID_PARAMETER);
			}
			interval = monitorQuery.getInterval();
			range = monitorQuery.getRange();
		}

		EnumMonitorTarget mTarget = EnumMonitorTarget.getTargetData(query.getMeasurement().toUpperCase());
		if (mTarget == null) {
			return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.INVALID_PARAMETER);
		}
		String target = mTarget.getTarget();
		String sql = null;
		String type = CommonConstant.MONIT_TYPE;
		//判断是否是网络监控
		if (target.indexOf(CommonConstant.MONIT_NETWORK) > -1) {
			type = CommonConstant.MONIT_NETWORK_TYPE;
		}
		if (!checkParamNUll(query.getContainer())) {
			sql = "SELECT mean("+"\"value\""+") FROM "+"\""+target+"\""+" WHERE "+"\"container_name\""+" = "+"\'"+query.getContainer()+"\'"+" AND "+"\"type\""+" = "+"\'"+type+"\'"+" AND "+"\"pod_name\""+" = "+"\'"+query.getPod()+"\'"+" AND time > now() - "+range+" GROUP BY time("+interval+"),"+"\"container_name\""+" fill(null)";
		} else {
			sql = "SELECT mean("+"\"value\""+") FROM "+"\""+target+"\""+" WHERE "+"\"type\""+" = "+"\'"+type+"\'"+" AND "+"\"pod_name\""+" = "+"\'"+query.getPod()+"\'"+" AND time > now() - "+range+" GROUP BY time("+interval+") fill(null)";
		}
		String influxServer = cluster.getInfluxdbUrl() + "?db="+cluster.getInfluxdbDb();
		influxServer = influxServer + "&&q="+URLEncoder.encode(sql, "UTF-8");
		HttpClientResponse response = HttpClientUtil.doGet(influxServer, null, null);
		if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
			return ActionReturnUtil.returnErrorWithMsg(response.getBody());
		}
		return ActionReturnUtil.returnSuccessWithData(JsonUtil.convertJsonToMap(response.getBody()));
	}

	
	/**
	 * 
	 * 获取node的监控数据
	 * 
	 * @param influxdbQuery 查询参数
	 * @return
	 * @throws Exception
	 */
	public ActionReturnUtil nodeQuery(InfluxdbQuery influxdbQuery) throws Exception {
		EnumMonitorQuery query = EnumMonitorQuery.getRangeData(influxdbQuery.getRangeType());
		if (query == null) {
			return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.INVALID_PARAMETER);
		}
		String interval = query.getInterval();
		String range = query.getRange();
		EnumMonitorTarget mTarget = EnumMonitorTarget.getTargetData(influxdbQuery.getMeasurement().toUpperCase());
		if (mTarget == null) {
			return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.INVALID_PARAMETER);
		}
		String target = mTarget.getTarget();
		EnumMonitorType mType = EnumMonitorType.getMonitType(influxdbQuery.getType().toUpperCase());
		if (mType == null) {
			return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.INVALID_PARAMETER);
		}	
		String sql = null;
		switch (mType.getType()) {
		    case "process":
			    sql = "SELECT mean("+"\"value\""+") FROM "+"\""+target+"\""+" WHERE "+"\"container_name\""+" = "+"\'"+influxdbQuery.getProcessName()+"\'"+" AND "+"\"host_id\"" + " = "+"\'"+influxdbQuery.getNode()+"\'"+" AND time > now() - "+range+" GROUP BY time("+interval+") fill(null)";
			    break;
		    case "node":
		    	if (influxdbQuery.getNode() == null) {
		    		sql = "SELECT mean("+"\"value\""+") FROM "+"\""+target+"\""+" WHERE "+"\"type\""+" = "+"\'"+mType.getType()+"\'"+" AND time > now() - "+range+" GROUP BY time("+interval+") fill(null)";
		    	} else {
		    		sql = "SELECT mean("+"\"value\""+") FROM "+"\""+target+"\""+" WHERE "+"\"nodename\""+" = "+"\'"+influxdbQuery.getNode()+"\'"+" AND "+"\"type\"" + " = "+"\'"+mType.getType()+"\'"+" AND time > now() - "+range+" GROUP BY time("+interval+") ,"+"\"nodename\""+"fill(null)";
		    	}
		    	break;
		}
		Cluster cluster = clusterService.findClusterById(influxdbQuery.getClusterId());
		String influxServer = cluster.getInfluxdbUrl() + "?db="+cluster.getInfluxdbDb();
		influxServer = influxServer + "&&q="+URLEncoder.encode(sql, "UTF-8");
		HttpClientResponse response = HttpClientUtil.doGet(influxServer, null, null);
		if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
			return ActionReturnUtil.returnErrorWithMsg(response.getBody());
		}
		return ActionReturnUtil.returnSuccessWithData(JsonUtil.convertJsonToMap(response.getBody()));
	}
	
	public ActionReturnUtil getProcessStatus(String name, String processName, String clusterId) throws Exception {
		String sql = null;
		boolean nodeNameNull = checkParamNUll(name);
		boolean pNameNull = checkParamNUll(processName);
		switch ((nodeNameNull)?1:0) {
		case 0:
			if (pNameNull) {
				sql = "SELECT * FROM "+"\"process/alive_status\""+" WHERE "+"\"host_id\"" + " = "+"\'"+name+"\'"+"group by "+"\"host_id\"" +","+ "\"container_name\"" +" order by time desc limit 1";
			} else {
				sql = "SELECT * FROM "+"\"process/alive_status\""+" WHERE "+"\"container_name\""+" = "+"\'"+processName+"\'"+" AND "+"\"host_id\"" + " = "+"\'"+name+"\'"+"group by "+"\"host_id\"" +","+ "\"container_name\"" +" order by time desc limit 1";
			}
			break;

		case 1:
			if (pNameNull) {
				sql = "SELECT * FROM "+"\"process/alive_status\""+"group by "+"\"host_id\"" +","+ "\"container_name\"" +" order by time desc limit 1";
			} else {
				sql = "SELECT * FROM "+"\"process/alive_status\""+" WHERE "+"\"container_name\""+" = "+"\'"+processName+"\'"+"group by "+"\"host_id\"" +","+ "\"container_name\"" +" order by time desc limit 1";
			}
			break;
		}
		Cluster cluster = clusterService.findClusterById(clusterId);
		String influxServer = cluster.getInfluxdbUrl() + "?db="+cluster.getInfluxdbDb();
		influxServer = influxServer + "&&q="+URLEncoder.encode(sql, "UTF-8");
		HttpClientResponse response = HttpClientUtil.doGet(influxServer, null, null);
		if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
			return ActionReturnUtil.returnErrorWithMsg(response.getBody());
		}
		return ActionReturnUtil.returnSuccessWithData(JsonUtil.convertJsonToMap(response.getBody()));
	}
	
	public ActionReturnUtil getAlarmList(String id) throws Exception {
		String url;
		if(checkParamNUll(id)) {
			url =  "/api/resource/alarm";
		} else {
			url =  "/api/resource/alarm?id="+id;
		}
		ActionReturnUtil response = HttpClientUtil.httpGetRequest(url, null, null);
		return response;
	}
	
	public ActionReturnUtil createThreshold(String processName, String measurement, String threshold, String alarmType, String alarmContact) throws Exception {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("process_name", processName);
		params.put("measurement", measurement);
		params.put("threshold_value", threshold);
		params.put("alarm_type", alarmType);
		params.put("alarm_contact", alarmContact);
		Map<String, Object> paramsFinal = new HashMap<String, Object>();
		paramsFinal.put("threshold", params);
		Map<String, Object> requestHeader = new HashMap<String, Object>();
		requestHeader.put("Content-type", "application/json");
		String url =  "/api/resource/threshold";
		HttpClientResponse response = HttpClientUtil.httpPostJsonRequest(url, requestHeader, paramsFinal);
		if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
			return ActionReturnUtil.returnSuccessWithMsg(response.getBody());
		}
		return ActionReturnUtil.returnSuccessWithData(JsonUtil.convertJsonToMap(response.getBody()));
	}
	
	public ActionReturnUtil deleteThreshold(String id) throws Exception {
		String url =  "/api/resource/threshold/"+id;
		ActionReturnUtil response = HttpClientUtil.httpDoDelete(url, null, null);
		return response;
	}
	
	public ActionReturnUtil listThreshold(String id) throws Exception {
		String url;
		if (checkParamNUll(id)) {
			url =  "/api/resource/threshold";
		} else {
			url =  "/api/resource/threshold?id="+id;
		}
		HttpClientResponse response = HttpClientUtil.httpGetRequestNew(url, null, null);
		if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
			return ActionReturnUtil.returnSuccessWithMsg(response.getBody());
		}
        return ActionReturnUtil.returnSuccessWithData(JsonUtil.convertJsonToMap(response.getBody()));
	}




	private boolean checkParamNUll(String p) {
		if (StringUtils.isEmpty(p) || StringUtils.isBlank(p) || p == null) {
			return true;
		}
		return false;
	}

	@Override
	public Map<String,List<QueryResult.Series>>  getClusterResourceUsage(String type, String measurements, String groupBy, Cluster cluster, List<String> notWorkNode) throws Exception {
//		long startTime=System.currentTimeMillis();   //获取开始时间
		StringBuffer sb = new StringBuffer("SELECT mean(\"value\") FROM \""+measurements+"\" WHERE \"type\" = '"+type+"' ");
		sb.append("  AND time > now() - 10m GROUP BY time(1m),"+groupBy+" order by time desc  limit 5");
		InfluxDB influxDB = InfluxDBClient.getInfluxDB(cluster);
		Query query = new Query(sb.toString(), cluster.getInfluxdbDb());
		QueryResult queryResult = influxDB.query(query);
		List<QueryResult.Series> series = queryResult.getResults().get(0).getSeries();
		Map<String,List<QueryResult.Series>> map = new HashMap<>();
		if (!CollectionUtils.isEmpty(series)){
			for (QueryResult.Series series1:series) {
				if (!Objects.isNull(series1.getTags()) && StringUtils.isNotBlank(series1.getTags().get(nodeName))){
					String name = series1.getTags().get(nodeName);
					List<QueryResult.Series> nodeList = map.get(name);
					if (CollectionUtils.isEmpty(nodeList)){
						nodeList = new ArrayList<>();
						nodeList.add(series1);
						map.put(name,nodeList);
					}else {
						nodeList.add(series1);
						map.put(name,nodeList);
					}
				}

			}
		}
//		long endTime=System.currentTimeMillis(); //获取结束时间
//		long time = endTime - startTime;//获取消耗时间，调试使用
//		System.out.println("消耗时间："+time);
		return map;
	}

	public double getClusterResourceUsage(String type, String measurements, String groupBy, Cluster cluster, List<String> notWorkNode, String nodename) throws Exception {
//		long startTime=System.currentTimeMillis();   //获取开始时间
		StringBuffer sb = new StringBuffer("SELECT mean(\"value\") FROM \""+measurements+"\" WHERE \"type\" = '"+type+"' ");
        if(StringUtils.isNotBlank(nodename)) {
        	sb.append(" and nodename='"+nodename+"' ");
		} else {
			if(notWorkNode != null && notWorkNode.size() > 0) {
				for(String node : notWorkNode) {
					sb.append(" and nodename!='"+node+"' ");
				}
			}
		}
		sb.append("  AND time > now() - 8m GROUP BY time(1m),"+groupBy+" order by time desc  limit 5");


		InfluxDB influxDB = InfluxDBClient.getInfluxDB(cluster);
		Query query = new Query(sb.toString(), cluster.getInfluxdbDb());
		QueryResult queryResult = influxDB.query(query);


		List<QueryResult.Series> series = queryResult.getResults().get(0).getSeries();

		Double totailResult = this.computeNodeInfo(series);
//		long endTime=System.currentTimeMillis(); //获取结束时间
//		long time = endTime - startTime;//获取消耗时间，调试使用
//		System.out.println("消耗时间："+time);
		return totailResult;
	}

	@Override
	public Double computeNodeInfo(List<QueryResult.Series> series) throws Exception{
		Double totailResult = 0.0;
		if(series!=null&&series.size()>0){
			for(int i=0; i<series.size(); i++) {
				Double result = 0.0;
				if (series.get(i)!=null&& !CollectionUtils.isEmpty(series.get(i).getValues())){
					List<List<Object>> values = series.get(i).getValues();
					if (null != values.get(0).get(CommonConstant.NUM_ONE)){
						result = Double.parseDouble(values.get(0).get(CommonConstant.NUM_ONE).toString());
					}
					if (result == 0.0 && values.size() > CommonConstant.NUM_ONE && null != values.get(CommonConstant.NUM_ONE).get(CommonConstant.NUM_ONE)) {
						result = Double.parseDouble(values.get(CommonConstant.NUM_ONE).get(CommonConstant.NUM_ONE).toString());
					}
					if (result == 0.0 && values.size() > CommonConstant.NUM_TWO && null != values.get(CommonConstant.NUM_TWO).get(CommonConstant.NUM_ONE)) {
						result = Double.parseDouble(values.get(CommonConstant.NUM_TWO).get(CommonConstant.NUM_ONE).toString());
					}
					if (result == 0.0 && values.size() > CommonConstant.NUM_THREE && null != values.get(CommonConstant.NUM_THREE).get(CommonConstant.NUM_ONE)) {
						result = Double.parseDouble(values.get(CommonConstant.NUM_THREE).get(CommonConstant.NUM_ONE).toString());
					}
					if (result == 0.0 && values.size() > CommonConstant.NUM_FOUR && null != values.get(CommonConstant.NUM_FOUR).get(CommonConstant.NUM_ONE)) {
						result = Double.parseDouble(values.get(CommonConstant.NUM_FOUR).get(CommonConstant.NUM_ONE).toString());
					}
				}
//	            if(series.get(i)!=null&&series.get(i).getValues()!=null&&series.get(i).getValues().size()>0&&series.get(i).getValues().get(0).size()>0&&series.get(i).getValues().get(0).get(1)!=null){
//	                result = Double.parseDouble(series.get(i).getValues().get(0).get(1).toString());
//	            }
//
//	            if(result == 0.0) {
//					if(series.get(i)!=null&&series.get(i).getValues()!=null&&series.get(i).getValues().size()>0&&series.get(i).getValues().get(1).size()>0&&series.get(i).getValues().get(1).get(1)!=null){
//						result = Double.parseDouble(series.get(i).getValues().get(1).get(1).toString());
//					}
//				}
//
//	            if(result == 0.0) {
//					if(series.get(i)!=null&&series.get(i).getValues()!=null&&series.get(i).getValues().size()>0&&series.get(i).getValues().get(2).size()>0&&series.get(i).getValues().get(2).get(1)!=null){
//						result = Double.parseDouble(series.get(i).getValues().get(2).get(1).toString());
//					}
//				}
//
//	            if(result == 0.0) {
//					if(series.get(i)!=null&&series.get(i).getValues()!=null&&series.get(i).getValues().size()>0&&series.get(i).getValues().get(3).size()>0&&series.get(i).getValues().get(3).get(1)!=null){
//						result = Double.parseDouble(series.get(i).getValues().get(3).get(1).toString());
//					}
//				}
//
//	            if(result == 0.0) {
//					if(series.get(i)!=null&&series.get(i).getValues()!=null&&series.get(i).getValues().size()>0&&series.get(i).getValues().get(4).size()>0&&series.get(i).getValues().get(4).get(1)!=null){
//						result = Double.parseDouble(series.get(i).getValues().get(4).get(1).toString());
//					}
//				}

				result = new BigDecimal(result).setScale(CommonConstant.NUM_ONE, BigDecimal.ROUND_HALF_UP).doubleValue();
				totailResult = totailResult + result;
			}
		}
		return totailResult;
	}


	@Override
	public double getClusterAllocatedResources(String type, String measurements, Cluster cluster) throws Exception {

		String sql = "";

		sql = "SELECT mean(\"value\") FROM \""+measurements+"\" WHERE \"type\" = '"+type+"'  AND time > now() - 30m GROUP BY time(1m) order by time desc  limit 2";

		InfluxDB influxDB = InfluxDBClient.getInfluxDB(cluster);
		Query query = new Query(sql, cluster.getInfluxdbDb());
		QueryResult queryResult = influxDB.query(query);

		List<QueryResult.Series> series = queryResult.getResults().get(0).getSeries();
		Double result = 0.0;

		if(series!=null&&series.size()>0) {
			for (int i = 0; i < series.size(); i++) {
				if(series!=null&&series.size()>0&&series.get(0)!=null&&series.get(0).getValues()!=null&&series.get(0).getValues().size()>0&&series.get(0).getValues().get(0)!=null&&series.get(0).getValues().get(0).get(1)!=null){
					result = Double.parseDouble(series.get(0).getValues().get(0).get(1).toString());

				}

				if(series!=null&&series.size()>0&&series.get(0)!=null&&series.get(0).getValues()!=null&&series.get(0).getValues().size()>0&&series.get(0).getValues().get(1)!=null&&series.get(0).getValues().get(1).get(1)!=null){
					result = Double.parseDouble(series.get(0).getValues().get(1).get(1).toString());

				}

			}
		}

		return result;
	}

	@Override
	public ActionReturnUtil getClusterNodeInfo(String clusterId) throws Exception {
		Cluster cluster = this.clusterService.findClusterById(clusterId);
		// 获取node
		K8SURL url = new K8SURL();
		url.setResource(Resource.NODE);
		K8SClientResponse nodeRes = new K8sMachineClient().exec(url, HTTPMethod.GET, null, null, cluster);
		if (!HttpStatusUtil.isSuccessStatus(nodeRes.getStatus())) {
			return ActionReturnUtil.returnErrorWithMsg(nodeRes.getBody());
		}
		NodeList nodeList = JsonUtil.jsonToPojo(nodeRes.getBody(), NodeList.class);
		List<Node> nodes = nodeList.getItems();
		List<Map<String, Object>> res = new ArrayList<>();
		Map<String, List<QueryResult.Series>> clusterMap = this.getClusterResourceUsage("node", "filesystem/limit", "nodename,resource_id", cluster, null);
		if (nodes != null && nodes.size() > 0) {
			for (Node node : nodes) {
				List<NodeCondition> conditions = node.getStatus().getConditions();
				for (NodeCondition nodeCondition : conditions) {
					if (nodeCondition.getType().equalsIgnoreCase("Ready")) {
						if(!nodeCondition.getStatus().equalsIgnoreCase("True")) {
							continue;
						}
					}
				}
				String nodeName = node.getMetadata().getName();
				double nodeFilesystemCapacity = 0;
				if (!CollectionUtils.isEmpty(clusterMap.get(nodeName))){
					nodeFilesystemCapacity = this.computeNodeInfo(clusterMap.get(nodeName));
				}

				Object object = node.getStatus().getAllocatable();
				if (object != null) {
					Map<String, Object> resourceMap = new HashMap<String, Object>();
					resourceMap.put("ip", node.getMetadata().getName());
					String cpuStr = ((Map<String, Object>) object).get("cpu").toString();
					if(cpuStr.contains("m")) {
						cpuStr = cpuStr.substring(0, cpuStr.indexOf("m"));
						resourceMap.put("cpu", Double.valueOf(cpuStr) / 1000);
					} else {
						resourceMap.put("cpu", Double.valueOf(cpuStr));
					}
					String memory = ((Map<String, Object>) object).get("memory").toString();
					if(memory.contains("Ki")) {
						memory = memory.substring(0, memory.indexOf("Ki"));
						double memoryDouble = Double.parseDouble(memory);
						resourceMap.put("memory", String.format("%.1f", memoryDouble/1024/1024));
					} else if(memory.contains("Mi")) {
						memory = memory.substring(0, memory.indexOf("Mi"));
						double memoryDouble = Double.parseDouble(memory);
						resourceMap.put("memory", String.format("%.1f", memoryDouble/1024));
					} else if(memory.contains("Gi")) {
						memory = memory.substring(0, memory.indexOf("Gi"));
						double memoryDouble = Double.parseDouble(memory);
						resourceMap.put("memory", String.format("%.1f", memoryDouble));
					}

					resourceMap.put("disk", String.format("%.0f", nodeFilesystemCapacity/1024/1024/1024));
					res.add(resourceMap);
				}
			}
		}
		return ActionReturnUtil.returnSuccessWithData(res);
	}


}
