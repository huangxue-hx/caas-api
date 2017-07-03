package com.harmonycloud.k8s.bean;

/**
 * 
 * @author jmi
 *
 */
public class GitRepoVolumeSource {
	
	private String repository;
	
	private String revision;
	
	private String directory;

	public String getRepository() {
		return repository;
	}

	public void setRepository(String repository) {
		this.repository = repository;
	}

	public String getRevision() {
		return revision;
	}

	public void setRevision(String revision) {
		this.revision = revision;
	}

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

}
