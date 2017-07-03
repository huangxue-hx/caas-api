package com.harmonycloud.service.platform.client;

import com.harmonycloud.dao.cluster.bean.Cluster;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * infuxdb client
 * 
 * 
 * @author jmi
 *
 */
//@Component
public class InfluxdbClient {
	
	private  String influxServer;

	private  String dbName;

	private String influxdbVersion;


	public InfluxdbClient(){
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		HttpSession session = request.getSession();
		Cluster cluster = (Cluster) session.getAttribute("currentCluster");
		this.setDbName(cluster.getInfluxdbDb());
		this.setInfluxServer(cluster.getInfluxdbUrl());
		this.setInfluxdbVersion(cluster.getInfluxdbVersion());
//		super();
	}

	public InfluxdbClient(Cluster cluster){
		this.setDbName(cluster.getInfluxdbDb());
		this.setInfluxServer(cluster.getInfluxdbUrl());
		this.setInfluxdbVersion(cluster.getInfluxdbVersion());
	}


	public String getInfluxServer() {
		return influxServer;
	}

	public void setInfluxServer(String influxServer) {
		this.influxServer = influxServer;
	}

	public  String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public String getInfluxdbVersion() {
		return influxdbVersion;
	}

	public void setInfluxdbVersion(String influxdbVersion) {
		this.influxdbVersion = influxdbVersion;
	}
}
