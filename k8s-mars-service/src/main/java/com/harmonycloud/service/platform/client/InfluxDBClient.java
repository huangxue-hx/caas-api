package com.harmonycloud.service.platform.client;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by zgl on 2018/3/22.
 */
public class InfluxDBClient {
	private static Logger logger = LoggerFactory.getLogger(InfluxDBClient.class);

//	private static final  Map<String,InfluxDB> influxDBMap = new HashMap<>();
	private static final String USERNAME = "root";
	public static InfluxDB getInfluxDB(Cluster cluster) {
		if (Objects.isNull(cluster) || StringUtils.isEmpty(cluster.getInfluxdbUrl())){
			throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
		}
		InfluxDB influxDB = null;
		String clusterId = cluster.getId();
//		if (CollectionUtils.isEmpty(influxDBMap) || Objects.isNull(influxDBMap.get(clusterId))) {
//			try {
//				influxDB = InfluxDBFactory.connect(cluster.getInfluxdbUrl()+"/", USERNAME, CommonConstant.EMPTYSTRING);
//
//			} catch (Exception e) {
//				logger.error("创建 influxDB 连接失败", e);
//			}
//		}else {
//			influxDB = influxDBMap.get(clusterId);
//		}
		try {
			influxDB = InfluxDBFactory.connect(cluster.getInfluxdbUrl()+"/", USERNAME, CommonConstant.EMPTYSTRING);
		} catch (Exception e) {
			logger.error("创建 influxDB 连接失败", e);
		}
		return influxDB;
	}

}