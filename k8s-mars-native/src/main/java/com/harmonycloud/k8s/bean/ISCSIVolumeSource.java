package com.harmonycloud.k8s.bean;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ISCSIVolumeSource {

	private boolean chapAuthDiscovery;
	
	private boolean chapAuthSession;
	
	private String fsType;
	
	private String initiatorName;
	
	private String iqn;
	
	private String iscsiInterface;
	
	private Integer lun;
	
	private List<String> portals;

	private boolean readOnly;
	
	private LocalObjectReference secretRef;
	
	private String targetPortal;
	
	public boolean isChapAuthDiscovery() {
		return chapAuthDiscovery;
	}

	public void setChapAuthDiscovery(boolean chapAuthDiscovery) {
		this.chapAuthDiscovery = chapAuthDiscovery;
	}

	public boolean isChapAuthSession() {
		return chapAuthSession;
	}

	public void setChapAuthSession(boolean chapAuthSession) {
		this.chapAuthSession = chapAuthSession;
	}

	public String getFsType() {
		return fsType;
	}

	public void setFsType(String fsType) {
		this.fsType = fsType;
	}

	public String getInitiatorName() {
		return initiatorName;
	}

	public void setInitiatorName(String initiatorName) {
		this.initiatorName = initiatorName;
	}

	public String getIqn() {
		return iqn;
	}

	public void setIqn(String iqn) {
		this.iqn = iqn;
	}

	public String getIscsiInterface() {
		return iscsiInterface;
	}

	public void setIscsiInterface(String iscsiInterface) {
		this.iscsiInterface = iscsiInterface;
	}

	public Integer getLun() {
		return lun;
	}

	public void setLun(Integer lun) {
		this.lun = lun;
	}

	public List<String> getPortals() {
		return portals;
	}

	public void setPortals(List<String> portals) {
		this.portals = portals;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	public LocalObjectReference getSecretRef() {
		return secretRef;
	}

	public void setSecretRef(LocalObjectReference secretRef) {
		this.secretRef = secretRef;
	}

	public String getTargetPortal() {
		return targetPortal;
	}

	public void setTargetPortal(String targetPortal) {
		this.targetPortal = targetPortal;
	}
	
	
}
