package com.harmonycloud.service.platform.bean.monitor;

/**
 * Influxdb 查询参数对象
 */
public class InfluxdbQuery {

    //监控对象类型,EnumMonitorType, pod/container/process/pod_container
    private String type;
    //查询时间区间范围类型,具体参考EnumMonitorQuery
    private String rangeType;
    //对应influxd的表名，如cpu/limit
    private String measurement;
    private String node;
    //服务的启动时间
    private String startTime;
    private String processName;
    private String clusterId;
    private String container;
    private String pod;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRangeType() {
        return rangeType;
    }

    public void setRangeType(String rangeType) {
        this.rangeType = rangeType;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getContainer() {
        return container;
    }

    public void setContainer(String container) {
        this.container = container;
    }

    public String getPod() {
        return pod;
    }

    public void setPod(String pod) {
        this.pod = pod;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public String getMeasurement() {
        return measurement;
    }

    public void setMeasurement(String measurement) {
        this.measurement = measurement;
    }
}
