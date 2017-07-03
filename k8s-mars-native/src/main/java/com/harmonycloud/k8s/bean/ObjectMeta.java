package com.harmonycloud.k8s.bean;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author qg
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ObjectMeta {
	
	private String name;
	
	private String generateName;
	
	private String selfLink;
	
	private String uid;
	
	private String resourceVersion;
	
	private Integer generation;
	
	private String creationTimestamp;
	
	private String deletionTimestamp;
	
	private Integer deletionGracePeriodSeconds;
	
	private Map<String, Object> labels;
	
	private Map<String, Object> annotations;
	
	private List<String> finalizers;
	
	private String clusterName;
	
	private String namespace;
	
	private List<OwnerReference> ownerReferences ;

	public String getGenerateName() {
		return generateName;
	}

	public void setGenerateName(String generateName) {
		this.generateName = generateName;
	}

	public String getSelfLink() {
		return selfLink;
	}

	public void setSelfLink(String selfLink) {
		this.selfLink = selfLink;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getResourceVersion() {
		return resourceVersion;
	}

	public void setResourceVersion(String resourceVersion) {
		this.resourceVersion = resourceVersion;
	}

	public Integer getGeneration() {
		return generation;
	}

	public void setGeneration(Integer generation) {
		this.generation = generation;
	}

	public String getCreationTimestamp() {
		return creationTimestamp;
	}

	public void setCreationTimestamp(String creationTimestamp) {
		this.creationTimestamp = creationTimestamp;
	}

	public String getDeletionTimestamp() {
		return deletionTimestamp;
	}

	public void setDeletionTimestamp(String deletionTimestamp) {
		this.deletionTimestamp = deletionTimestamp;
	}

	public Integer getDeletionGracePeriodSeconds() {
		return deletionGracePeriodSeconds;
	}

	public void setDeletionGracePeriodSeconds(Integer deletionGracePeriodSeconds) {
		this.deletionGracePeriodSeconds = deletionGracePeriodSeconds;
	}

	public Map<String, Object> getLabels() {
		return labels;
	}

	public void setLabels(Map<String, Object> labels) {
		this.labels = labels;
	}

	public Map<String, Object> getAnnotations() {
		return annotations;
	}

	public void setAnnotations(Map<String, Object> annotations) {
		this.annotations = annotations;
	}

	public List<String> getFinalizers() {
		return finalizers;
	}

	public void setFinalizers(List<String> finalizers) {
		this.finalizers = finalizers;
	}

	public String getClusterName() {
		return clusterName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
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

	public List<OwnerReference> getOwnerReferences() {
		return ownerReferences;
	}

	public void setOwnerReferences(List<OwnerReference> ownerReferences) {
		this.ownerReferences = ownerReferences;
	}

}
