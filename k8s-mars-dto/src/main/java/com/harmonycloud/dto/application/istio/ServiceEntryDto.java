package com.harmonycloud.dto.application.istio;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.NotBlank;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * create  by ljf  18/12/5
 * 服务入口bean
 */
@ApiModel(value = "ServiceEntryDto信息")
public class ServiceEntryDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;

    private String ip;

    private String namespace;

    private String namespaceName;

    private Map<String, Object> labels;

    private String createTime;

    private String targetPort;

    private String protocol; //协议

    private String port ;

    private String tenantId;

    private String projectId;

    private String clusterId;

    private String  clusterName;

    private String  hosts;//服务域名

    private Integer   ServiceEntryType; //  0 为外部服务入口 1为内部服务入口

    @ApiModelProperty(value = "内部服务ip列表", name = "ipList", example = "[\"10.10.10.1\", \"10.10.10.2\", ... ]", required = true)
    private List<String> ipList;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public Map<String, Object> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, Object> labels) {
        this.labels = labels;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getTargetPort() {
        return targetPort;
    }

    public void setTargetPort(String targetPort) {
        this.targetPort = targetPort;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getHosts() {
        return hosts;
    }

    public void setHosts(String hosts) {
        this.hosts = hosts;
    }

    public List<String> getIpList() {
        return ipList;
    }

    public void setIpList(List<String> ipList) {
        this.ipList = ipList;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public Integer getServiceEntryType() {
        return ServiceEntryType;
    }

    public void setServiceEntryType(Integer serviceEntryType) {
        ServiceEntryType = serviceEntryType;
    }

    public String getNamespaceName() {
        return namespaceName;
    }

    public void setNamespaceName(String namespaceName) {
        this.namespaceName = namespaceName;
    }
}
