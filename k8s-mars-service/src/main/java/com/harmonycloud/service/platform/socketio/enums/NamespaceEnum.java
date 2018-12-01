package com.harmonycloud.service.platform.socketio.enums;

/**
 * Created by andy on 17-2-24.
 */
public enum NamespaceEnum {
	NOTINAMESPACE("/rest/notification"),
	WETTYNAMESPACE("/rest/wetty");

	private String namespace;

	NamespaceEnum(String namespace) {
		this.namespace = namespace;
	}

	public String getNamespace() {
		return namespace;
	}

	private void setNamespace(String namespace) {
		this.namespace = namespace;
	}
}
