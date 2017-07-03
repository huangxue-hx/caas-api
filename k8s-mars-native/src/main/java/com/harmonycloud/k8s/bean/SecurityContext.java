package com.harmonycloud.k8s.bean;

/**
 * 
 * @author jmi
 *
 */
public class SecurityContext {
	
	private Capabilities capabilities;
	
	private boolean privileged;
	
	private SELinuxOptions seLinuxOptions;
	
	private Integer runAsUser;
	
	private boolean runAsNonRoot;
	
	private boolean readOnlyRootFilesystem;

	public Capabilities getCapabilities() {
		return capabilities;
	}

	public void setCapabilities(Capabilities capabilities) {
		this.capabilities = capabilities;
	}

	public boolean isPrivileged() {
		return privileged;
	}

	public void setPrivileged(boolean privileged) {
		this.privileged = privileged;
	}

	public SELinuxOptions getSeLinuxOptions() {
		return seLinuxOptions;
	}

	public void setSeLinuxOptions(SELinuxOptions seLinuxOptions) {
		this.seLinuxOptions = seLinuxOptions;
	}

	public Integer getRunAsUser() {
		return runAsUser;
	}

	public void setRunAsUser(Integer runAsUser) {
		this.runAsUser = runAsUser;
	}

	public boolean isRunAsNonRoot() {
		return runAsNonRoot;
	}

	public void setRunAsNonRoot(boolean runAsNonRoot) {
		this.runAsNonRoot = runAsNonRoot;
	}

	public boolean isReadOnlyRootFilesystem() {
		return readOnlyRootFilesystem;
	}

	public void setReadOnlyRootFilesystem(boolean readOnlyRootFilesystem) {
		this.readOnlyRootFilesystem = readOnlyRootFilesystem;
	}

}
