package com.harmonycloud.dto.cluster;

public class IngressConfigMap {

    String clientHeaderBufferSize ;
    String enableUnderscoresInHeaders ;
    String loadBalance ;
    String maxWorkerConnections ;
    String proxyBodySize ;
    String proxyReadTimeout ;
    String proxySendTimeout ;
    String workerProcesses; //工作进程数
    String useGzip;          //gzip启用或禁用     示例:"ture"   "false"  默认"ture"
    String gzipLevel;	 //gzip压缩级别       示例:"5"              默认:"5"

    public String getClientHeaderBufferSize() {
        return clientHeaderBufferSize;
    }

    public void setClientHeaderBufferSize(String clientHeaderBufferSize) {
        this.clientHeaderBufferSize = clientHeaderBufferSize;
    }

    public String getEnableUnderscoresInHeaders() {
        return enableUnderscoresInHeaders;
    }

    public void setEnableUnderscoresInHeaders(String enableUnderscoresInHeaders) {
        this.enableUnderscoresInHeaders = enableUnderscoresInHeaders;
    }

    public String getLoadBalance() {
        return loadBalance;
    }

    public void setLoadBalance(String loadBalance) {
        this.loadBalance = loadBalance;
    }

    public String getMaxWorkerConnections() {
        return maxWorkerConnections;
    }

    public void setMaxWorkerConnections(String maxWorkerConnections) {
        this.maxWorkerConnections = maxWorkerConnections;
    }

    public String getProxyBodySize() {
        return proxyBodySize;
    }

    public void setProxyBodySize(String proxyBodySize) {
        this.proxyBodySize = proxyBodySize;
    }

    public String getProxyReadTimeout() {
        return proxyReadTimeout;
    }

    public void setProxyReadTimeout(String proxyReadTimeout) {
        this.proxyReadTimeout = proxyReadTimeout;
    }

    public String getProxySendTimeout() {
        return proxySendTimeout;
    }

    public void setProxySendTimeout(String proxySendTimeout) {
        this.proxySendTimeout = proxySendTimeout;
    }

    public String getWorkerProcesses() {
        return workerProcesses;
    }

    public void setWorkerProcesses(String workerProcesses) {
        this.workerProcesses = workerProcesses;
    }

    public String getUseGzip() {
        return useGzip;
    }

    public void setUseGzip(String useGzip) {
        this.useGzip = useGzip;
    }

    public String getGzipLevel() {
        return gzipLevel;
    }

    public void setGzipLevel(String gzipLevel) {
        this.gzipLevel = gzipLevel;
    }
}
