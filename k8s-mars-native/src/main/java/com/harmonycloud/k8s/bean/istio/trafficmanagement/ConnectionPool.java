package com.harmonycloud.k8s.bean.istio.trafficmanagement;

public class ConnectionPool {

    private TcpConnection tcp;

    private HttpConnection http;

    public TcpConnection getTcp() {
        return tcp;
    }

    public void setTcp(TcpConnection tcp) {
        this.tcp = tcp;
    }

    public HttpConnection getHttp() {
        return http;
    }

    public void setHttp(HttpConnection http) {
        this.http = http;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        ConnectionPool connectionPool = (ConnectionPool) o;
        if (this.tcp != null) {
            if (!this.tcp.equals(connectionPool.tcp)) {
                return false;
            }
        } else {
            if (connectionPool.tcp != null) {
                return false;
            }
        }
        if (this.http != null) {
            if (!this.http.equals(connectionPool.http)) {
                return false;
            }
        } else {
            if (connectionPool.http != null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = tcp.hashCode();
        result = 31 * result + http.hashCode();
        return result;
    }
}
