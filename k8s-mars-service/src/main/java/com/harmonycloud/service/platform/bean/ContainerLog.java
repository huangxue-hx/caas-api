package com.harmonycloud.service.platform.bean;


/**
 * Created by zhangkui on 2017/3/31.
 * 日志查询结果对象
 */
public class ContainerLog {

    private String hostIp;
    private String podName;
    private String timeStamp;
    private String message;

    public String getHostIp() {
        return hostIp;
    }

    public void setHostIp(String hostIp) {
        this.hostIp = hostIp;
    }

    public String getPodName() {
        return podName;
    }

    public void setPodName(String podName) {
        this.podName = podName;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
