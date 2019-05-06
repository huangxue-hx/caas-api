package com.harmonycloud.dto.application.istio;

import com.harmonycloud.k8s.bean.istio.policies.Port;
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
public class ServiceEntryDto {
    @ApiModelProperty(value = "服务入口名称", name = "name", required = true)
    private String name;
    @ApiModelProperty(value = "英文简称", name = "namespace", required = true)
    private String namespace;
    @ApiModelProperty(value = "分区名称", name = "namespaceName")
    private String namespaceName;
    @ApiModelProperty(value = "labels", name = "labels")
    private Map<String, Object> labels;
    @ApiModelProperty(value = "创建时间", name = "createTime")
    private String createTime;
    @ApiModelProperty(value = "targetPort", name = "targetPort")
    private String targetPort;
    @ApiModelProperty(value = "服务端口列表", name = "ipList", example = "[[\"10.10.10.1\",\"HTTP\"],[\"10.10.10.1\",\"HTTP\"],[\"10.10.10.1\",\"HTTP\"]]", required = true)
    private  List<ServiceEntryPortDto>  portList; //协议
    @ApiModelProperty(value = "集群名称", name = "clusterName" , required = true)
    private String  clusterName;
    @ApiModelProperty(value = "服务域名", name = "hosts" , required = true)
    private String  hosts;//服务域名
    @ApiModelProperty(value = "服务入口类型（1为外部服务入口，1为内部服务入口）", name = "ServiceEntryType" )
    private Integer   serviceEntryType; //  0 为外部服务入口 1为内部服务入口
    @ApiModelProperty(value = "内部服务ip列表", name = "ipList", example = "[\"10.10.10.1\", \"10.10.10.2\", ... ]", required = true)
    private List<String> ipList;
    @ApiModelProperty(value = "集群id", name = "clusterId" , required = true)
    private String clusterId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
        return serviceEntryType;
    }

    public void setServiceEntryType(Integer serviceEntryType) {
        this.serviceEntryType = serviceEntryType;
    }

    public String getNamespaceName() {
        return namespaceName;
    }

    public void setNamespaceName(String namespaceName) {
        this.namespaceName = namespaceName;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public List<ServiceEntryPortDto> getPortList() {
        return portList;
    }

    public void setPortList(List<ServiceEntryPortDto> portList) {
        this.portList = portList;
    }
}
