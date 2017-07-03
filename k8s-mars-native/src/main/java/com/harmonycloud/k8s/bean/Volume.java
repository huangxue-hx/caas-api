package com.harmonycloud.k8s.bean;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author qg
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Volume {

	private String name;
	
	private NFSVolumeSource nfs;
	
	private SecretVolumeSource secret;
	
	private GitRepoVolumeSource gitRepo;
	
	private PersistentVolumeClaimVolumeSource persistentVolumeClaim;
	
	private EmptyDirVolumeSource emptyDir;
	
	private HostPath hostPath;
	
	private ConfigMapVolumeSource configMap;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public NFSVolumeSource getNfs() {
		return nfs;
	}

	public void setNfs(NFSVolumeSource nfs) {
		this.nfs = nfs;
	}

	public SecretVolumeSource getSecret() {
		return secret;
	}

	public void setSecret(SecretVolumeSource secret) {
		this.secret = secret;
	}

	public GitRepoVolumeSource getGitRepo() {
		return gitRepo;
	}

	public void setGitRepo(GitRepoVolumeSource gitRepo) {
		this.gitRepo = gitRepo;
	}

	public PersistentVolumeClaimVolumeSource getPersistentVolumeClaim() {
		return persistentVolumeClaim;
	}

	public void setPersistentVolumeClaim(PersistentVolumeClaimVolumeSource persistentVolumeClaim) {
		this.persistentVolumeClaim = persistentVolumeClaim;
	}

	public EmptyDirVolumeSource getEmptyDir() {
		return emptyDir;
	}

	public void setEmptyDir(EmptyDirVolumeSource emptyDir) {
		this.emptyDir = emptyDir;
	}

    public ConfigMapVolumeSource getConfigMap() {
        return configMap;
    }

    public void setConfigMap(ConfigMapVolumeSource configMap) {
        this.configMap = configMap;
    }

	public HostPath getHostPath() {
		return hostPath;
	}

	public void setHostPath(HostPath hostPath) {
		this.hostPath = hostPath;
	}
	
}
