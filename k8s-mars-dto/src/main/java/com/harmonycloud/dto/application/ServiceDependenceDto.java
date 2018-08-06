package com.harmonycloud.dto.application;

import java.io.Serializable;

/**
 * Created by chencheng on 18-8-1
 *
 * 服务依赖
 */
public class ServiceDependenceDto implements Serializable {

    private String serviceName;//服务名称

    private String detectWay;//检测方式

    private String port;//端口

    private String url;//路径

    private Integer intervalTime;//检查间隔时间

    private Integer successThreshold;//成功阈值

    private Integer failThreshold;//失败阈值

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getDetectWay() {
        return detectWay;
    }

    public void setDetectWay(String detectWay) {
        this.detectWay = detectWay;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getIntervalTime() {
        return intervalTime;
    }

    public void setIntervalTime(Integer intervalTime) {
        this.intervalTime = intervalTime;
    }

    public Integer getSuccessThreshold() {
        return successThreshold;
    }

    public void setSuccessThreshold(Integer successThreshold) {
        this.successThreshold = successThreshold;
    }

    public Integer getFailThreshold() {
        return failThreshold;
    }

    public void setFailThreshold(Integer failThreshold) {
        this.failThreshold = failThreshold;
    }
}
