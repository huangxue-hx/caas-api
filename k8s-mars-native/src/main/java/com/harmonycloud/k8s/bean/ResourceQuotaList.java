package com.harmonycloud.k8s.bean;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author qg
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResourceQuotaList {

	private String kind;
	
	private String apiVersion;
	
	private UnversionedListMeta metadata;
	
	private List<ResourceQuota> items;

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public String getApiVersion() {
		return apiVersion;
	}

	public void setApiVersion(String apiVersion) {
		this.apiVersion = apiVersion;
	}

	public UnversionedListMeta getMetadata() {
		return metadata;
	}

	public void setMetadata(UnversionedListMeta metadata) {
		this.metadata = metadata;
	}

	public List<ResourceQuota> getItems() {
		return items;
	}

	public void setItems(List<ResourceQuota> items) {
		this.items = items;
	}
	
	
}
