package com.harmonycloud.service.platform.bean;

import java.util.List;

import com.harmonycloud.k8s.bean.ContainerImage;
import com.harmonycloud.k8s.bean.NodeAddress;

public class NodeDetailDto {
	private String architecture;
	private String containerRuntimeVersion;
	private String cpu;
	private String creationTime;
	private String gpu;
	private String kernelVersion;
	private String kubeProxyVersion;
	private String kubeletVersion;
	private String memory;
	private String name;
	private String os;
	private String pods;
	private String type;
	private String disk;
	private String nodeShareStatus;
	private List<NodeAddress> addresses;
	private List<ContainerImage> images;
	private String status;
	private List<String> otherNodeList;
	private String clusterId;
	private String tenantAliasName;
	private List<PodDto> podlist;
	private Boolean scheduable;

	public String getTenantAliasName() {
		return tenantAliasName;
	}

	public void setTenantAliasName(String tenantAliasName) {
		this.tenantAliasName = tenantAliasName;
	}

	public List<PodDto> getPodlist() {
        return podlist;
    }

    public void setPodlist(List<PodDto> podlist) {
        this.podlist = podlist;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public List<String> getOtherNodeList() {
        return otherNodeList;
    }

    public void setOtherNodeList(List<String> otherNodeList) {
        this.otherNodeList = otherNodeList;
    }

    public String getDisk() {
        return disk;
    }

    public void setDisk(String disk) {
        this.disk = disk;
    }

    public String getNodeShareStatus() {
        return nodeShareStatus;
    }

    public void setNodeShareStatus(String nodeShareStatus) {
        this.nodeShareStatus = nodeShareStatus;
    }

    public String getArchitecture() {
		return architecture;
	}

	public void setArchitecture(String architecture) {
		this.architecture = architecture;
	}

	public String getContainerRuntimeVersion() {
		return containerRuntimeVersion;
	}

	public void setContainerRuntimeVersion(String containerRuntimeVersion) {
		this.containerRuntimeVersion = containerRuntimeVersion;
	}

	public String getCpu() {
		return cpu;
	}

	public void setCpu(String cpu) {
		this.cpu = cpu;
	}

	public List<ContainerImage> getImages() {
		return images;
	}

	public void setImages(List<ContainerImage> images) {
		this.images = images;
	}

	public String getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(String creationTime) {
		this.creationTime = creationTime;
	}

	public String getGpu() {
		return gpu;
	}

	public void setGpu(String gpu) {
		this.gpu = gpu;
	}

	public String getKernelVersion() {
		return kernelVersion;
	}

	public void setKernelVersion(String kernelVersion) {
		this.kernelVersion = kernelVersion;
	}

	public String getKubeProxyVersion() {
		return kubeProxyVersion;
	}

	public void setKubeProxyVersion(String kubeProxyVersion) {
		this.kubeProxyVersion = kubeProxyVersion;
	}

	public String getKubeletVersion() {
		return kubeletVersion;
	}

	public void setKubeletVersion(String kubeletVersion) {
		this.kubeletVersion = kubeletVersion;
	}

	public String getMemory() {
		return memory;
	}

	public void setMemory(String memory) {
		this.memory = memory;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOs() {
		return os;
	}

	public void setOs(String os) {
		this.os = os;
	}

	public String getPods() {
		return pods;
	}

	public void setPods(String pods) {
		this.pods = pods;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<NodeAddress> getAddresses() {
		return addresses;
	}

	public void setAddresses(List<NodeAddress> addresses) {
		this.addresses = addresses;
	}

	public Boolean getScheduable() {
		return scheduable;
	}

	public void setScheduable(Boolean scheduable) {
		this.scheduable = scheduable;
	}

	public class Status {
		private String lastHeartbeatTime;
		private String lastTransitionTime;
		private String message;
		private String name;
		private String reason;

		public String getLastHeartbeatTime() {
			return lastHeartbeatTime;
		}

		public void setLastHeartbeatTime(String lastHeartbeatTime) {
			this.lastHeartbeatTime = lastHeartbeatTime;
		}

		public String getLastTransitionTime() {
			return lastTransitionTime;
		}

		public void setLastTransitionTime(String lastTransitionTime) {
			this.lastTransitionTime = lastTransitionTime;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getReason() {
			return reason;
		}

		public void setReason(String reason) {
			this.reason = reason;
		}

	}
}
