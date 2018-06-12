package com.harmonycloud.k8s.bean.cluster;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ClusterInfo implements Serializable {
    private static final long serialVersionUID = -6159692303715601870L;
    private String address ;
    private Integer port;
    private String protocol;
    private ClusterNetwork network;
    private ClusterJenkins jenkins;
    private ClusterMysql mysql;
    private ClusterRedis redis;
    private ClusterHarbor harbor;
    private List<ClusterDomain> domain ;
    private List<ClusterExternal> external ;
    private List<ClusterStorage> nfs ;

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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public ClusterHarbor getHarbor() {
        return harbor;
    }

    public void setHarbor(ClusterHarbor harbor) {
        this.harbor = harbor;
    }

    public List<ClusterDomain> getDomain() {
        return domain;
    }

    public void setDomain(List<ClusterDomain> domain) {
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
}