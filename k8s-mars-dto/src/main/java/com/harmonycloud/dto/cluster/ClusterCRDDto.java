package com.harmonycloud.dto.cluster;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.harmonycloud.k8s.bean.cluster.*;

import java.util.Date;
import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClusterCRDDto {
    private String uid;
    private String name;
    private String nickname;
    private String dataCenter;
    private Integer envLabel;
    private String k8sAddress;
    private String compAddress;
    private String protocol;
    private Integer port;
    private String harborAddress;
    private String harborAdminUser;
    private String harborAdminPwd;

    private String harborProtocol;
    private Integer harborPort;
    private Date createTime;

    private ClusterDomain domain ;
    private List<ClusterExternal> external ;
    private ClusterNetwork network;
    private ClusterRedis redis;
    private ClusterMysql mysql;
    private ClusterJenkins jenkins;

    private ElasticsearchConnect elasticsearch;
    private List<ClusterStorage> nfs ;
    private boolean isEnable;
    private List<ClusterTemplate> template;

    private  ClusterGit  gitInfo;

    public ClusterNetwork getNetwork() {
        return network;
    }

    public void setNetwork(ClusterNetwork network) {
        this.network = network;
    }

    public ClusterRedis getRedis() {
        return redis;
    }

    public void setRedis(ClusterRedis redis) {
        this.redis = redis;
    }

    public ClusterMysql getMysql() {
        return mysql;
    }

    public void setMysql(ClusterMysql mysql) {
        this.mysql = mysql;
    }

    public ClusterJenkins getJenkins() {
        return jenkins;
    }

    public void setJenkins(ClusterJenkins jenkins) {
        this.jenkins = jenkins;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
    public Integer getHarborPort() {
        return harborPort;
    }

    public void setHarborPort(Integer harborPort) {
        this.harborPort = harborPort;
    }

    public String getHarborProtocol() {
        return harborProtocol;
    }

    public void setHarborProtocol(String harborProtocol) {
        this.harborProtocol = harborProtocol;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDataCenter() {
        return dataCenter;
    }

    public void setDataCenter(String dataCenter) {
        this.dataCenter = dataCenter;
    }

    public Integer getEnvLabel() {
        return envLabel;
    }

    public void setEnvLabel(Integer envLabel) {
        this.envLabel = envLabel;
    }

    public String getK8sAddress() {
        return k8sAddress;
    }

    public void setK8sAddress(String k8sAddress) {
        this.k8sAddress = k8sAddress;
    }

    public String getHarborAddress() {
        return harborAddress;
    }

    public void setHarborAddress(String harborAddress) {
        this.harborAddress = harborAddress;
    }

    public String getHarborAdminUser() {
        return harborAdminUser;
    }

    public void setHarborAdminUser(String harborAdminUser) {
        this.harborAdminUser = harborAdminUser;
    }

    public String getHarborAdminPwd() {
        return harborAdminPwd;
    }

    public void setHarborAdminPwd(String harborAdminPwd) {
        this.harborAdminPwd = harborAdminPwd;
    }

    public ClusterDomain getDomain() {
        return domain;
    }

    public void setDomain(ClusterDomain domain) {
        this.domain = domain;
    }

    public List<ClusterExternal> getExternal() {
        return external;
    }

    public void setExternal(List<ClusterExternal> external) {
        this.external = external;
    }

    public List<ClusterStorage> getNfs() {
        return nfs;
    }

    public void setNfs(List<ClusterStorage> nfs) {
        this.nfs = nfs;
    }

    public boolean getIsEnable() {
        return isEnable;
    }

    public void setIsEnable(boolean isenable) {
        isEnable = isenable;
    }

    public List<ClusterTemplate> getTemplate() {
        return template;
    }

    public void setTemplate(List<ClusterTemplate> template) {
        this.template = template;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }


    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public ElasticsearchConnect getElasticsearch() {
        return elasticsearch;
    }

    public void setElasticsearch(ElasticsearchConnect elasticsearch) {
        this.elasticsearch = elasticsearch;
    }

    public ClusterGit getGitInfo() {
        return gitInfo;
    }

    public void setGitInfo(ClusterGit gitInfo) {
        this.gitInfo = gitInfo;
    }

    public String getCompAddress() {
        return compAddress;
    }

    public void setCompAddress(String compAddress) {
        this.compAddress = compAddress;
    }
}
