package com.harmonycloud.service.platform.bean;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.DataPrivilegeField;
import com.harmonycloud.common.enumm.DataPrivilegeType;
import com.harmonycloud.common.enumm.DataResourceTypeEnum;
import com.harmonycloud.dto.application.AffinityDto;
import com.harmonycloud.dto.scale.AutoScaleDto;
import com.harmonycloud.k8s.bean.Event;
import com.harmonycloud.k8s.bean.HostAlias;
import com.harmonycloud.k8s.bean.ServicePort;

import java.util.List;
import java.util.Map;

/**
 *
 * @author jmi
 *
 */
@DataPrivilegeType(type = DataResourceTypeEnum.SERVICE)
public class AppDetail {
    private String serviceType;

    private String clusterIP;

	private String serviceAddress;

	private List<ServicePort> internalPorts;

	private String sessionAffinity;
    @DataPrivilegeField(type = CommonConstant.DATA_FIELD)
	private String name;
    @DataPrivilegeField(type = CommonConstant.NAMESPACE_FIELD)
	private String namespace;

	private String clusterId;

	private String version;

	private String createTime;

	private String updateTime;

	private Integer instance;

	private String owner;

	private String hostName;

	private Map<String, Object> labels;

	private String status;

	private String annotation;

	private List<PodDetail> podList;

	private List<EventDetail> events;

	private AutoScaleDto autoScale;

	private String restartPolicy;

	private List<EventDetail> autoScalingHistory;

	private boolean hostIPC;

	private boolean hostPID;

	private boolean hostNetwork;

	/**节点亲和*/
	private List<AffinityDto> nodeAffinity;

	/**pod 亲和*/
	private AffinityDto podAffinity;

	/**pod 反亲和*/
	private AffinityDto podAntiAffinity;

	/**pod 是否分散*/
	private AffinityDto podDisperse;

	/**pod 按主机分组调度**/
	private AffinityDto podGroupSchedule;

	private String nodeSelector;

	private boolean isOperationable;

	private boolean isMsf;

	private String realName;

	private String aliasNamespace;

    private String dataPrivilege;

	private List<HostAlias> hostAliases;

    private List<ContainerOfPodDetail> initContainers;

    private String podManagementPolicy;

	private Map<String, Object> pullDependence;

	private String serviceDependence;

	private String deployVersion;

	private boolean isPrivateNamespace;

	public String getDeployVersion() {
		return deployVersion;
	}

	public void setDeployVersion(String deployVersion) {
		this.deployVersion = deployVersion;
	}

	public AffinityDto getPodGroupSchedule() {
		return podGroupSchedule;
	}

	public void setPodGroupSchedule(AffinityDto podGroupSchedule) {
		this.podGroupSchedule = podGroupSchedule;
	}

	public String getServiceDependence() {
		return serviceDependence;
	}

	public void setServiceDependence(String serviceDependence) {
		this.serviceDependence = serviceDependence;
	}

	public Map<String, Object> getPullDependence() {
		return pullDependence;
	}

	public void setPullDependence(Map<String, Object> pullDependence) {
		this.pullDependence = pullDependence;
	}

	public List<HostAlias> getHostAliases() {
		return hostAliases;
	}

	public void setHostAliases(List<HostAlias> hostAliases) {
		this.hostAliases = hostAliases;
	}

	public String getRealName() {
		return realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	public String getAliasNamespace() {
		return aliasNamespace;
	}

	public void setAliasNamespace(String aliasNamespace) {
		this.aliasNamespace = aliasNamespace;
	}

	public boolean isMsf() {
		return isMsf;
	}

	public void setMsf(boolean msf) {
		isMsf = msf;
	}

	public boolean isOperationable() {
		return isOperationable;
	}

	public void setOperationable(boolean operationable) {
		isOperationable = operationable;
	}

	public String getClusterIP() {
		return clusterIP;
	}

	public void setClusterIP(String clusterIP) {
		this.clusterIP = clusterIP;
	}

	public String getServiceAddress() {
		return serviceAddress;
	}

	public void setServiceAddress(String serviceAddress) {
		this.serviceAddress = serviceAddress;
	}

	public List<ServicePort> getInternalPorts() {
		return internalPorts;
	}

	public void setInternalPorts(List<ServicePort> internalPorts) {
		this.internalPorts = internalPorts;
	}

	public String getSessionAffinity() {
		return sessionAffinity;
	}

	public void setSessionAffinity(String sessionAffinity) {
		this.sessionAffinity = sessionAffinity;
	}

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

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}

	public Integer getInstance() {
		return instance;
	}

	public void setInstance(Integer instance) {
		this.instance = instance;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public Map<String, Object> getLabels() {
		return labels;
	}

	public void setLabels(Map<String, Object> labels) {
		this.labels = labels;
	}

	public String getAnnotation() {
		return annotation;
	}

	public void setAnnotation(String annotation) {
		this.annotation = annotation;
	}

	public List<PodDetail> getPodList() {
		return podList;
	}

	public void setPodList(List<PodDetail> podList) {
		this.podList = podList;
	}

	public List<EventDetail> getEvents() {
		return events;
	}

	public void setEvents(List<EventDetail> events) {
		this.events = events;
	}

	public List<EventDetail> getAutoScalingHistory() {
		return autoScalingHistory;
	}

	public void setAutoScalingHistory(List<EventDetail> autoScalingHistory) {
		this.autoScalingHistory = autoScalingHistory;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getRestartPolicy() {
		return restartPolicy;
	}

	public void setRestartPolicy(String restartPolicy) {
		this.restartPolicy = restartPolicy;
	}

	public boolean isHostIPC() {
		return hostIPC;
	}

	public void setHostIPC(boolean hostIPC) {
		this.hostIPC = hostIPC;
	}

	public boolean isHostPID() {
		return hostPID;
	}

	public void setHostPID(boolean hostPID) {
		this.hostPID = hostPID;
	}

	public AutoScaleDto getAutoScale() {
		return autoScale;
	}

	public void setAutoScale(AutoScaleDto autoScale) {
		this.autoScale = autoScale;
	}

	public String getNodeSelector() {
		return nodeSelector;
	}

	public void setNodeSelector(String nodeSelector) {
		this.nodeSelector = nodeSelector;
	}

	public boolean isHostNetwork() {
		return hostNetwork;
	}

	public void setHostNetwork(boolean hostNetwork) {
		this.hostNetwork = hostNetwork;
	}

	public List<AffinityDto> getNodeAffinity() {
		return nodeAffinity;
	}

	public void setNodeAffinity(List<AffinityDto> nodeAffinity) {
		this.nodeAffinity = nodeAffinity;
	}

	public AffinityDto getPodAffinity() {
		return podAffinity;
	}

	public void setPodAffinity(AffinityDto podAffinity) {
		this.podAffinity = podAffinity;
	}

	public AffinityDto getPodAntiAffinity() {
		return podAntiAffinity;
	}

	public void setPodAntiAffinity(AffinityDto podAntiAffinity) {
		this.podAntiAffinity = podAntiAffinity;
	}

	public AffinityDto getPodDisperse() {
		return podDisperse;
	}

	public void setPodDisperse(AffinityDto podDisperse) {
		this.podDisperse = podDisperse;
	}

	public String getClusterId() {
		return clusterId;
	}

	public void setClusterId(String clusterId) {
		this.clusterId = clusterId;
	}

    public String getDataPrivilege() {
        return dataPrivilege;
    }

    public void setDataPrivilege(String dataPrivilege) {
        this.dataPrivilege = dataPrivilege;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public List<ContainerOfPodDetail> getInitContainers() {
        return initContainers;
    }

    public void setInitContainers(List<ContainerOfPodDetail> initContainers) {
        this.initContainers = initContainers;
    }

    public String getPodManagementPolicy() {
        return podManagementPolicy;
    }

    public void setPodManagementPolicy(String podManagementPolicy) {
        this.podManagementPolicy = podManagementPolicy;
    }

	public boolean isPrivateNamespace() {
		return isPrivateNamespace;
	}

	public void setPrivateNamespace(boolean privateNamespace) {
		isPrivateNamespace = privateNamespace;
	}
}
