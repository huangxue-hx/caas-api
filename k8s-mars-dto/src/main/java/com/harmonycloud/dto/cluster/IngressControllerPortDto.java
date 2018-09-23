package com.harmonycloud.dto.cluster;

/**
 * @author xc
 * @date 2018/8/3 11:01
 */
public class IngressControllerPortDto {

    private int httpPort;

    private int httpsPort;

    private int healthPort;

    private int statusPort;

    public int getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(int httpPort) {
        this.httpPort = httpPort;
    }

    public int getHttpsPort() {
        return httpsPort;
    }

    public void setHttpsPort(int httpsPort) {
        this.httpsPort = httpsPort;
    }

    public int getHealthPort() {
        return healthPort;
    }

    public void setHealthPort(int healthzPort) {
        this.healthPort = healthzPort;
    }

    public int getStatusPort() {
        return statusPort;
    }

    public void setStatusPort(int statusPort) {
        this.statusPort = statusPort;
    }
}
