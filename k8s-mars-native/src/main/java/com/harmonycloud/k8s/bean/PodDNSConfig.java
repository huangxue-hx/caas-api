package com.harmonycloud.k8s.bean;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PodDNSConfig {

	private List<String> nameservers;
	
	private List<PodDNSConfigOption> options;
	
	private List<String> searches;

	public List<String> getNameservers() {
		return nameservers;
	}

	public void setNameservers(List<String> nameservers) {
		this.nameservers = nameservers;
	}

	public List<PodDNSConfigOption> getOptions() {
		return options;
	}

	public void setOptions(List<PodDNSConfigOption> options) {
		this.options = options;
	}

	public List<String> getSearches() {
		return searches;
	}

	public void setSearches(List<String> searches) {
		this.searches = searches;
	}
	
}
