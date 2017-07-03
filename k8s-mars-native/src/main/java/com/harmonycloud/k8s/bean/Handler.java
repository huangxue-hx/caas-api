package com.harmonycloud.k8s.bean;

public class Handler {
	
	private ExecAction exec;
	
	private HTTPGetAction httpGet;
	
	private TCPSocketAction tcpSocket;

	public HTTPGetAction getHttpGet() {
		return httpGet;
	}

	public void setHttpGet(HTTPGetAction httpGet) {
		this.httpGet = httpGet;
	}

	public ExecAction getExec() {
		return exec;
	}

	public void setExec(ExecAction exec) {
		this.exec = exec;
	}

	public TCPSocketAction getTcpSocket() {
		return tcpSocket;
	}

	public void setTcpSocket(TCPSocketAction tcpSocket) {
		this.tcpSocket = tcpSocket;
	}

}
