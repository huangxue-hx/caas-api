package com.harmonycloud.k8s.bean;

import java.util.List;

public class HTTPGetAction {
	
	private String path;
	
	private long port;
	
	private String hoString;
	
	private String scheme;
	
	private String host;
	
	
	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	private List<HTTPHeader> httpHeaders;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getHoString() {
		return hoString;
	}

	public void setHoString(String hoString) {
		this.hoString = hoString;
	}

	public String getScheme() {
		return scheme;
	}

	public void setScheme(String scheme) {
		this.scheme = scheme;
	}

	public List<HTTPHeader> getHttpHeaders() {
		return httpHeaders;
	}

	public void setHttpHeaders(List<HTTPHeader> httpHeaders) {
		this.httpHeaders = httpHeaders;
	}

    public long getPort() {
        return port;
    }

    public void setPort(long port) {
        this.port = port;
    }

}
