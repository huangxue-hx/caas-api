package com.harmonycloud.k8s.bean.cluster;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import static com.harmonycloud.common.Constant.CommonConstant.COLON;
import static com.harmonycloud.common.Constant.CommonConstant.SLASH;

/**
 * Created by zhangsl on 16/11/4.
 */
public class Cluster implements Serializable,Comparable<Cluster> {

	private static final long serialVersionUID = -1886649980404325029L;

	/**
	 * 集群id，为cluster tpr的uid
	 */
	private String id;
	/**
	 * 集群名称
	 */
	private String name;

	/**
	 * 集群别名
	 */
	private String aliasName;
	/**
	 * 集群所属的数据中心
	 */
	private String dataCenter;

	private String dataCenterName;
	/**
	 * 集群mater的host或高可用集群vip
	 */
	private String host;
	/**
	 * 集群等级，0-上层，1-开发，2-qas，3-uat，4-prod，参考
	 * @ClusterLevelEnum.java
	 */
	private Integer level;
	/**
	 * kube apiserver 协议
	 */
	private String protocol;
	/**
	 * 集群节点的ssh需要的用户名，应用日志文件下载需要ssh登录到节点
	 */
	private String username;
	/**
	 * 集群节点的ssh需要的用户名对应的密码
	 */
	private String password;
	/**
	 * 访问集群kube-apiserver的token
	 */
	private String machineToken;
	/**
	 * 集群kube-apiserver的端口
	 */
	private Integer port;
	/**
	 * influxdb组件的url，获取资源等监控信息
	 */
	private String influxdbUrl;
	/**
	 * influxdb组件的数据库名称，获取资源等监控信息
	 */
	private String influxdbDb;
	/**
	 * elasticsearch组件的host
	 */
	private String esHost;
	/**
	 * elasticsearch组件的port
	 */
	private Integer esPort;
	/**
	 * elasticsearch组件集群名称
	 */
	private String esClusterName;
	private Date createTime;
	/**
	 * 集群http对外服务的域名，有三级和四级域名
	 */
	private ClusterDomain domains;
	/**
	 * 集群是否可用状态
	 */
	private boolean isEnable;
	/**
	 * 集群对应的harborServer信息
	 */
	private HarborServer harborServer;

	/**
	 * 集群对外信息（ngnix和F5）
	 */
	private List<ClusterExternal> external;

	private ClusterMysql mysql;
	private ClusterRedis redis;
	private ClusterNetwork network;
	private ClusterJenkins jenkins;
	private List<ClusterStorage> storages;
	/**
	 * 集群组件服务信息
	 */
	private List<ClusterTemplate> clusterComponent;

	/**
	 * 集群git信息
	 */
	private ClusterGit gitInfo;

	public Cluster() {
		super();
	}

	public static long getSerialVersionUID() {
		return serialVersionUID;
	}

	public boolean isEnable() {
		return isEnable;
	}

	public void setEnable(boolean enable) {
		isEnable = enable;
	}

	public ClusterMysql getMysql() {
		return mysql;
	}

	public void setMysql(ClusterMysql mysql) {
		this.mysql = mysql;
	}

	public ClusterRedis getRedis() {
		return redis;
	}

	public void setRedis(ClusterRedis redis) {
		this.redis = redis;
	}

	public ClusterNetwork getNetwork() {
		return network;
	}

	public void setNetwork(ClusterNetwork network) {
		this.network = network;
	}

	public ClusterJenkins getJenkins() {
		return jenkins;
	}

	public void setJenkins(ClusterJenkins jenkins) {
		this.jenkins = jenkins;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getMachineToken() {
		return machineToken;
	}

	public void setMachineToken(String machineToken) {
		this.machineToken = machineToken;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getInfluxdbUrl() {
		return influxdbUrl;
	}

	public void setInfluxdbUrl(String influxdbUrl) {
		this.influxdbUrl = influxdbUrl;
	}

	public String getInfluxdbDb() {
		return influxdbDb;
	}

	public void setInfluxdbDb(String influxdbDb) {
		this.influxdbDb = influxdbDb;
	}

	public String getEsHost() {
		return esHost;
	}

	public void setEsHost(String esHost) {
		this.esHost = esHost;
	}

	public Integer getEsPort() {
		return esPort;
	}

	public void setEsPort(Integer esPort) {
		this.esPort = esPort;
	}

	public String getEsClusterName() {
		return esClusterName;
	}

	public void setEsClusterName(String esClusterName) {
		this.esClusterName = esClusterName;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public HarborServer getHarborServer() {
		return harborServer;
	}

	public void setHarborServer(HarborServer harborServer) {
		this.harborServer = harborServer;
	}

	public String getDataCenter() {
		return dataCenter;
	}

	public void setDataCenter(String dataCenter) {
		this.dataCenter = dataCenter;
	}

	public String getDataCenterName() {
		return dataCenterName;
	}

	public void setDataCenterName(String dataCenterName) {
		this.dataCenterName = dataCenterName;
	}

	public boolean getIsEnable() {
		return this.isEnable;
	}

	public void setIsEnable(boolean enable) {
		this.isEnable = enable;
	}

	public ClusterDomain getDomains() {
		return domains;
	}

	public void setDomains(ClusterDomain domains) {
		this.domains = domains;
	}

	public List<ClusterExternal> getExternal() {
		return external;
	}

	public void setExternal(List<ClusterExternal> external) {
		this.external = external;
	}

	public List<ClusterStorage> getStorages() {
		return storages;
	}

	public void setStorages(List<ClusterStorage> storages) {
		this.storages = storages;
	}

	public String getApiServerUrl(){
		return protocol + COLON  + SLASH +  SLASH + host + COLON + port;
	}

	public List<ClusterTemplate> getClusterComponent() {
		return clusterComponent;
	}

	public void setClusterComponent(List<ClusterTemplate> clusterComponent) {
		this.clusterComponent = clusterComponent;
	}

	@Override
	public int compareTo(Cluster c){
		int lev = this.level - c.getLevel();
		if(lev != 0){
			return lev;
		}
		int dataCenter = this.getDataCenter().compareTo(c.getDataCenter());
		if(dataCenter != 0){
			return dataCenter;
		}
		return this.getName().compareTo(c.getName());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Cluster cluster = (Cluster) o;

		if (!id.equals(cluster.id)) return false;
		if (!name.equals(cluster.name)) return false;
		if (!aliasName.equals(cluster.aliasName)) return false;
		if (!dataCenter.equals(cluster.dataCenter)) return false;
		if (!dataCenterName.equals(cluster.dataCenterName)) return false;
		return host.equals(cluster.host);
	}

	@Override
	public int hashCode() {
		int result = id.hashCode();
		result = 31 * result + name.hashCode();
		result = 31 * result + aliasName.hashCode();
		result = 31 * result + dataCenter.hashCode();
		result = 31 * result + dataCenterName.hashCode();
		result = 31 * result + host.hashCode();
		return result;
	}

	public String getAliasName() {
		return aliasName;
	}

	public void setAliasName(String aliasName) {
		this.aliasName = aliasName;
	}

	public ClusterGit getGitInfo() {
		return gitInfo;
	}

	public void setGitInfo(ClusterGit gitInfo) {
		this.gitInfo = gitInfo;
	}
}
