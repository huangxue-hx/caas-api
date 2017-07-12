package com.harmonycloud.service.platform.client;

import com.harmonycloud.dao.cluster.bean.Cluster;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.net.InetAddress;

public class EsClient {

	private  Logger LOG = LoggerFactory.getLogger(EsClient.class);
	
	private  String host;

	private  Integer port;

	private  String clusterName;

	private  TransportClient client;
	
	private  String version;
	
	private  String index;

	public EsClient(){
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		HttpSession session = request.getSession();
		Cluster cluster = (Cluster) session.getAttribute("currentCluster");
		this.setHost(cluster.getEsHost());
		this.setPort(cluster.getEsPort());
		this.setClusterName(cluster.getEsClusterName());
		this.setVersion(cluster.getEsVersion());
//		super();
	}

	public EsClient(Cluster cluster){
		this.setHost(cluster.getEsHost());
		this.setPort(cluster.getEsPort());
		this.setClusterName(cluster.getEsClusterName());
		this.setVersion(cluster.getEsVersion());
	}



	public  TransportClient getEsClient() throws Exception{
		if(client != null){
			return client;
		}
		try {

			String[] hosts = host.split(",");
			Settings settings = Settings.settingsBuilder().put("cluster.name", clusterName).build();
			TransportClient transportClient = TransportClient.builder().settings(settings).build();
			for (String host : hosts) {
				transportClient.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host), port));
			}
			this.setClient(transportClient);
//			EsClient.client = transportClient;
		}catch (Exception e){
			LOG.error("创建ElasticSearch Client 失败", e);
			throw e;
		}
		return client;

	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getClusterName() {
		return clusterName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	public TransportClient getClient() {
		return client;
	}

	public void setClient(TransportClient client) {
		this.client = client;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}


}
