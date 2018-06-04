package com.harmonycloud.k8s.bean;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author qg
 *
 */
@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PodSpec {

	private List<Volume> volumes;
	
	private List<Container> containers;
	
	private String restartPolicy;
	
	private Integer terminationGracePeriodSeconds;
	
	private Integer activeDeadlineSeconds;
	
	private String dnsPolicy;
	
	private Map<String,Object> nodeSelector;
	
	private String serviceAccountName;
	
	private String serviceAccount;
	
	private String nodeName;
	
	private boolean hostNetwork;
	
	private boolean hostPID;
	
	private boolean hostIPC;
	
	private List<LocalObjectReference> imagePullSecrets;
	
	private String hostname;
	
	private String subdomain;

	private Affinity affinity;
	
	private boolean automountServiceAccountToken;
	
	private List<HostAlias> hostAliases;
	
	private List<Container> initContainers;
	
	private Integer priority;
	
	private String priorityClassName;
	
	private String schedulerName;
	
	private List<Toleration> tolerations;
	
	private PodDNSConfig dnsConfig;

	public Affinity getAffinity() {
		return affinity;
	}

	public void setAffinity(Affinity affinity) {
		this.affinity = affinity;
	}

	
	public String getDnsPolicy() {
		return dnsPolicy;
	}

	public void setDnsPolicy(String dnsPolicy) {
		this.dnsPolicy = dnsPolicy;
	}

	public String getServiceAccount() {
		return serviceAccount;
	}

	public void setServiceAccount(String serviceAccount) {
		this.serviceAccount = serviceAccount;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public String getSubdomain() {
		return subdomain;
	}

	public void setSubdomain(String subdomain) {
		this.subdomain = subdomain;
	}

	public List<Volume> getVolumes() {
		return volumes;
	}

	public void setVolumes(List<Volume> volumes) {
		this.volumes = volumes;
	}

	public List<Container> getContainers() {
		return containers;
	}

	public List<LocalObjectReference> getImagePullSecrets() {
		return imagePullSecrets;
	}

	public void setImagePullSecrets(List<LocalObjectReference> imagePullSecrets) {
		this.imagePullSecrets = imagePullSecrets;
	}

	public void setContainers(List<Container> containers) {
		this.containers = containers;
	}

	public String getRestartPolicy() {
		return restartPolicy;
	}

	public void setRestartPolicy(String restartPolicy) {
		this.restartPolicy = restartPolicy;
	}

	public Map<String, Object> getNodeSelector() {
		return nodeSelector;
	}

	public void setNodeSelector(Map<String, Object> nodeSelector) {
		this.nodeSelector = nodeSelector;
	}

	public String getServiceAccountName() {
		return serviceAccountName;
	}

	public void setServiceAccountName(String serviceAccountName) {
		this.serviceAccountName = serviceAccountName;
	}

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public boolean isAutomountServiceAccountToken() {
		return automountServiceAccountToken;
	}

	public void setAutomountServiceAccountToken(boolean automountServiceAccountToken) {
		this.automountServiceAccountToken = automountServiceAccountToken;
	}

	public List<HostAlias> getHostAliases() {
		return hostAliases;
	}

	public void setHostAliases(List<HostAlias> hostAliases) {
		this.hostAliases = hostAliases;
	}

	public List<Container> getInitContainers() {
		return initContainers;
	}

	public void setInitContainers(List<Container> initContainers) {
		this.initContainers = initContainers;
	}

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	public String getPriorityClassName() {
		return priorityClassName;
	}

	public void setPriorityClassName(String priorityClassName) {
		this.priorityClassName = priorityClassName;
	}

	public String getSchedulerName() {
		return schedulerName;
	}

	public void setSchedulerName(String schedulerName) {
		this.schedulerName = schedulerName;
	}

	public List<Toleration> getTolerations() {
		return tolerations;
	}

	public void setTolerations(List<Toleration> tolerations) {
		this.tolerations = tolerations;
	}

	public PodDNSConfig getDnsConfig() {
		return dnsConfig;
	}

	public void setDnsConfig(PodDNSConfig dnsConfig) {
		this.dnsConfig = dnsConfig;
	}

	public boolean isHostNetwork() {
		return hostNetwork;
	}

	public void setHostNetwork(boolean hostNetwork) {
		this.hostNetwork = hostNetwork;
	}

	public boolean isHostPID() {
		return hostPID;
	}

	public void setHostPID(boolean hostPID) {
		this.hostPID = hostPID;
	}

	public boolean isHostIPC() {
		return hostIPC;
	}

	public void setHostIPC(boolean hostIPC) {
		this.hostIPC = hostIPC;
	}

	public Integer getTerminationGracePeriodSeconds() {
		return terminationGracePeriodSeconds;
	}

	public void setTerminationGracePeriodSeconds(Integer terminationGracePeriodSeconds) {
		this.terminationGracePeriodSeconds = terminationGracePeriodSeconds;
	}

	public Integer getActiveDeadlineSeconds() {
		return activeDeadlineSeconds;
	}

	public void setActiveDeadlineSeconds(Integer activeDeadlineSeconds) {
		this.activeDeadlineSeconds = activeDeadlineSeconds;
	}
	
}
