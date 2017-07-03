package com.harmonycloud.dao.network.bean;

import java.util.Map;

public class Network {
	public String ipv6_address_scope;
	public String mtu;
	public String dns_domain;
	public String id;
	public String status;
	public String availability_zone_hints;
	public String[] subnets;
	public String[] availability_zones;
	public String description;
	public String[] tags;
	public String updated_at;
	public String name;
	public String created_at;
	public String admin_state_up;
	public String tenant_id;
	public Map provider;
	public String getIpv6_address_scope() {
		return ipv6_address_scope;
	}
	public void setIpv6_address_scope(String ipv6_address_scope) {
		this.ipv6_address_scope = ipv6_address_scope;
	}
	public String getMtu() {
		return mtu;
	}
	public void setMtu(String mtu) {
		this.mtu = mtu;
	}
	public String getDns_domain() {
		return dns_domain;
	}
	public void setDns_domain(String dns_domain) {
		this.dns_domain = dns_domain;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getAvailability_zone_hints() {
		return availability_zone_hints;
	}
	public void setAvailability_zone_hints(String availability_zone_hints) {
		this.availability_zone_hints = availability_zone_hints;
	}
	public String[] getSubnets() {
		return subnets;
	}
	public void setSubnets(String[] subnets) {
		this.subnets = subnets;
	}
	public String[] getAvailability_zones() {
		return availability_zones;
	}
	public void setAvailability_zones(String[] availability_zones) {
		this.availability_zones = availability_zones;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String[] getTags() {
		return tags;
	}
	public void setTags(String[] tags) {
		this.tags = tags;
	}
	public String getUpdated_at() {
		return updated_at;
	}
	public void setUpdated_at(String updated_at) {
		this.updated_at = updated_at;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCreated_at() {
		return created_at;
	}
	public void setCreated_at(String created_at) {
		this.created_at = created_at;
	}
	public String getAdmin_state_up() {
		return admin_state_up;
	}
	public void setAdmin_state_up(String admin_state_up) {
		this.admin_state_up = admin_state_up;
	}
	public String getTenant_id() {
		return tenant_id;
	}
	public void setTenant_id(String tenant_id) {
		this.tenant_id = tenant_id;
	}
	public Map getProvider() {
		return provider;
	}
	public void setProvider(Map provider) {
		this.provider = provider;
	}
	
}
