package com.harmonycloud.service.platform.serviceImpl.monitor;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import com.harmonycloud.dao.cluster.ClusterMapper;
import com.harmonycloud.dao.cluster.bean.Cluster;
import org.apache.commons.lang3.StringUtils;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.EnumMonitorQuery;
import com.harmonycloud.common.enumm.EnumMonitorTarget;
import com.harmonycloud.common.enumm.EnumMonitorType;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.HttpClientResponse;
import com.harmonycloud.common.util.HttpClientUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.service.platform.bean.ProviderPlugin;
import com.harmonycloud.service.platform.client.InfluxdbClient;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.platform.service.InfluxdbService;

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
	private ClusterMapper clusterMapper;
	//监控数据最大展示100个监控点
    private static final int MAX_MONITOR_POINT = 100;


	public ActionReturnUtil podMonit(String rangeType, String startTime, String pod, String container, String target) throws Exception {
		String interval = "";
		String range = "";
		if(rangeType.equals("5")){
			if(StringUtils.isBlank(startTime)){
				return ActionReturnUtil.returnErrorWithMsg("服务创建时间为空!");
			}
			SimpleDateFormat adf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			adf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date startDate = adf.parse(startTime);
			Long timeInterval = System.currentTimeMillis() - startDate.getTime();
			//如果创建时间在30天内，就用目前固定的几种时间查询类型，超过30天，以100个点作为最大的点数显示
			if(timeInterval <= EnumMonitorQuery.THIRTY_DAY.getMillisecond()){
				String newRangeType;
				if(timeInterval <= EnumMonitorQuery.FIVE_MINUTE.getMillisecond()){
					newRangeType = EnumMonitorQuery.FIVE_MINUTE.getCode();
				}else if(timeInterval <= EnumMonitorQuery.SIX_HOUR.getMillisecond()){
					newRangeType = EnumMonitorQuery.SIX_HOUR.getCode();
				}else if(timeInterval <= EnumMonitorQuery.ONE_DAY.getMillisecond()){
					newRangeType = EnumMonitorQuery.ONE_DAY.getCode();
				}else if(timeInterval <= EnumMonitorQuery.SEVEN_DAY.getMillisecond()){
					newRangeType = EnumMonitorQuery.SEVEN_DAY.getCode();
				}else {
					newRangeType = EnumMonitorQuery.THIRTY_DAY.getCode();
				}
				EnumMonitorQuery query = EnumMonitorQuery.getRangeData(newRangeType);
				interval = query.getInterval();
				range = query.getRange();
			}else{
				Long timeIntervalInDays = timeInterval / (1000 * 60 * 60 * 24);
				range = timeIntervalInDays +"d";
				interval = (timeIntervalInDays/MAX_MONITOR_POINT + 1) + "d";
			}
		}else {
			EnumMonitorQuery query = EnumMonitorQuery.getRangeData(rangeType);
			if (query == null) {
				return ActionReturnUtil.returnErrorWithMsg("无效的查询时间段类型!");
			}
			interval = query.getInterval();
			range = query.getRange();
		}

		EnumMonitorTarget mTarget = EnumMonitorTarget.getTargetData(target.toUpperCase());
		if (mTarget == null) {
			return ActionReturnUtil.returnErrorWithMsg("params is null!");
		}
		target = mTarget.getTarget();
		String sql = null;
		String type = CommonConstant.MONIT_TYPE;
		//判断是否是网络监控
		if (target.indexOf(CommonConstant.MONIT_NETWORK) > -1) {
			type = CommonConstant.MONIT_NETWORK_TYPE;
		}
		if (!checkParamNUll(container)) {
			sql = "SELECT mean("+"\"value\""+") FROM "+"\""+target+"\""+" WHERE "+"\"container_name\""+" = "+"\'"+container+"\'"+" AND "+"\"type\""+" = "+"\'"+type+"\'"+" AND "+"\"pod_name\""+" = "+"\'"+pod+"\'"+" AND time > now() - "+range+" GROUP BY time("+interval+"),"+"\"container_name\""+" fill(0)";
		} else {
			sql = "SELECT mean("+"\"value\""+") FROM "+"\""+target+"\""+" WHERE "+"\"type\""+" = "+"\'"+type+"\'"+" AND "+"\"pod_name\""+" = "+"\'"+pod+"\'"+" AND time > now() - "+range+" GROUP BY time("+interval+") fill(0)";
		}
		InfluxdbClient influxdbClient = new InfluxdbClient();
		String influxServer = influxdbClient.getInfluxServer() + "?db="+influxdbClient.getDbName();
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
	 * @param type
	 * @param rangeType
	 * @param target
	 * @param name
	 * @param startTime
	 * @param processName
	 * @return
	 * @throws Exception
	 */
	public ActionReturnUtil nodeQuery(String type, String rangeType, String target, String name, String startTime, String processName) throws Exception {
		EnumMonitorQuery query = EnumMonitorQuery.getRangeData(rangeType);
		if (query == null) {
			return ActionReturnUtil.returnErrorWithMsg("params is null!");
		}
		String interval = query.getInterval();
		String range = query.getRange();
		EnumMonitorTarget mTarget = EnumMonitorTarget.getTargetData(target.toUpperCase());
		if (mTarget == null) {
			return ActionReturnUtil.returnErrorWithMsg("params is null!");
		}
		target = mTarget.getTarget();
		EnumMonitorType mType = EnumMonitorType.getMonitType(type.toUpperCase());
		if (mType == null) {
			return ActionReturnUtil.returnErrorWithMsg("params is null!");
		}	
		String sql = null;
		switch (mType.getType()) {
		    case "process":
			    sql = "SELECT mean("+"\"value\""+") FROM "+"\""+target+"\""+" WHERE "+"\"container_name\""+" = "+"\'"+processName+"\'"+" AND "+"\"host_id\"" + " = "+"\'"+name+"\'"+" AND time > now() - "+range+" GROUP BY time("+interval+") fill(null)";
			    break;
		    case "node":
		    	if (name == null) {
		    		sql = "SELECT mean("+"\"value\""+") FROM "+"\""+target+"\""+" WHERE "+"\"type\""+" = "+"\'"+mType.getType()+"\'"+" AND time > now() - "+range+" GROUP BY time("+interval+") fill(null)";
		    	} else {
		    		sql = "SELECT mean("+"\"value\""+") FROM "+"\""+target+"\""+" WHERE "+"\"nodename\""+" = "+"\'"+name+"\'"+" AND "+"\"type\"" + " = "+"\'"+mType.getType()+"\'"+" AND time > now() - "+range+" GROUP BY time("+interval+") ,"+"\"nodename\""+"fill(null)";
		    	}
		    	break;
		}
		InfluxdbClient influxdbClient = new InfluxdbClient();
		String influxServer = influxdbClient.getInfluxServer() + "?db="+influxdbClient.getDbName();
		influxServer = influxServer + "&&q="+URLEncoder.encode(sql, "UTF-8");
		HttpClientResponse response = HttpClientUtil.doGet(influxServer, null, null);
		if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
			return ActionReturnUtil.returnErrorWithMsg(response.getBody());
		}
		return ActionReturnUtil.returnSuccessWithData(JsonUtil.convertJsonToMap(response.getBody()));
	}
	
	public ActionReturnUtil getProcessStatus(String name, String processName) throws Exception {
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
		InfluxdbClient influxdbClient = new InfluxdbClient();
		String influxServer = influxdbClient.getInfluxServer() + "?db="+influxdbClient.getDbName();
		influxServer = influxServer + "&&q="+URLEncoder.encode(sql, "UTF-8");
		HttpClientResponse response = HttpClientUtil.doGet(influxServer, null, null);
		if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
			return ActionReturnUtil.returnErrorWithMsg(response.getBody());
		}
		return ActionReturnUtil.returnSuccessWithData(JsonUtil.convertJsonToMap(response.getBody()));
	}
	
	public ActionReturnUtil getProviderList() {
		List<ProviderPlugin> provider = new ArrayList<ProviderPlugin>();
		InfluxdbClient influxdbClient = new InfluxdbClient();
		ProviderPlugin providerPlugin = new ProviderPlugin();
		providerPlugin.setIp(influxdbClient.getInfluxServer());
		providerPlugin.setName(Constant.INFLUXDB);
		providerPlugin.setVersion(influxdbClient.getInfluxdbVersion());
		provider.add(providerPlugin);
		return ActionReturnUtil.returnSuccessWithData(provider);
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
	public double getClusterResourceUsage(String type, String measurements, String groupBy, Cluster cluster, List<String> notWorkNode) throws Exception {
		return this.getClusterResourceUsage(type, measurements, groupBy, cluster, notWorkNode, null);
	}

	public double getClusterResourceUsage(String type, String measurements, String groupBy, Cluster cluster, List<String> notWorkNode, String nodename) throws Exception {
		InfluxdbClient influxdbClient = new InfluxdbClient(cluster);
        StringBuffer sb = new StringBuffer("SELECT mean(\"value\") FROM \""+measurements+"\" WHERE \"type\" = '"+type+"' ");
        if(null != nodename && !"".equals(nodename)) {
        	sb.append(" and nodename='"+nodename+"' ");
		} else {
			if(notWorkNode != null && notWorkNode.size() > 0) {
				for(String node : notWorkNode) {
					sb.append(" and nodename!='"+node+"' ");
				}
			}
		}
		sb.append("  AND time > now() - 10m GROUP BY time(1m),"+groupBy+" order by time desc  limit 2");

        System.out.println("XXXXXXX--------------------------------->"+sb.toString());

		String influxServer = influxdbClient.getInfluxServer() + "?db="+influxdbClient.getDbName();
		InfluxDB influxDB = InfluxDBFactory.connect(influxdbClient.getInfluxServer()+"/", "root", "");
		Query query = new Query(sb.toString(), cluster.getInfluxdbDb());
		QueryResult queryResult = influxDB.query(query);


		List<QueryResult.Series> series = queryResult.getResults().get(0).getSeries();

		Double totailResult = 0.0;
		if(series!=null&&series.size()>0){
		    for(int i=0; i<series.size(); i++) {
	            Double result = 0.0;
	            if(series.get(i)!=null&&series.get(i).getValues()!=null&&series.get(i).getValues().size()>0&&series.get(i).getValues().get(0).size()>0&&series.get(i).getValues().get(0).get(1)!=null){
	                result = Double.parseDouble(series.get(i).getValues().get(0).get(1).toString());
	            }

	            if(result == 0.0) {
					if(series.get(i)!=null&&series.get(i).getValues()!=null&&series.get(i).getValues().size()>0&&series.get(i).getValues().get(1).size()>0&&series.get(i).getValues().get(1).get(1)!=null){
						result = Double.parseDouble(series.get(i).getValues().get(1).get(1).toString());
					}
				}

				result = new BigDecimal(result).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
	            totailResult = totailResult + result;
	        }
//			if(measurements.toLowerCase().equals("cpu/node_utilization")) {
//				totailResult = totailResult / series.size();
//			}
		}

		return totailResult;

	}




	@Override
	public double getClusterAllocatedResources(String type, String measurements, Cluster cluster) throws Exception {
		InfluxdbClient influxdbClient = new InfluxdbClient(cluster);
		String sql = "";

		sql = "SELECT mean(\"value\") FROM \""+measurements+"\" WHERE \"type\" = '"+type+"'  AND time > now() - 30m GROUP BY time(1m) order by time desc  limit 2";

		String influxServer = influxdbClient.getInfluxServer() + "?db="+influxdbClient.getDbName();
		InfluxDB influxDB = InfluxDBFactory.connect(influxdbClient.getInfluxServer()+"/", "root", "");
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




}
