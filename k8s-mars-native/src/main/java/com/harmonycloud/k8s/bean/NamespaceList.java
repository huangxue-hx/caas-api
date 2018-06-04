package com.harmonycloud.k8s.bean;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author qg
 *
 */
@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class NamespaceList {

	private String kind;
	
	private String apiVersion;
	
	private UnversionedListMeta metadata;
	
	private List<Namespace> items;

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

	public List<Namespace> getItems() {
		return items;
	}

	public void setItems(List<Namespace> items) {
		this.items = items;
	}
	
	
}
