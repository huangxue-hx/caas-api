package com.harmonycloud.dto.log;


import io.swagger.annotations.ApiModelProperty;

/**
 * Created by zhangkui on 2017/3/31.
 * 日志查询参数对象
 */
public class FullLinkQueryDto {

    @ApiModelProperty(value="分区名",name="namespace", required = true)
    private String namespace;
    @ApiModelProperty(value="应用名",name="appName", required = true)
    private String appName;
    @ApiModelProperty(value="服务名",name="deployName")
    private String deployName;
    @ApiModelProperty(value="开始时间", example = "yyyy-MM-dd hh:mm:ss", name="fromTime", required = true)
    private String fromTime;
    @ApiModelProperty(value="结束时间", example = "yyyy-MM-dd hh:mm:ss", name="toTime", required = true)
    private String toTime;


    private String businessId;

    private String agentId;

    private String exceptionType;

    private Integer statusCode;

    private Integer top;

    private String url;

    private String serverUrl;

    private String order;

    private String orderedField;

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getDeployName() {
        return deployName;
    }

    public void setDeployName(String deployName) {
        this.deployName = deployName;
    }

    public String getFromTime() {
        return fromTime;
    }

    public void setFromTime(String fromTime) {
        this.fromTime = fromTime;
    }

    public String getToTime() {
        return toTime;
    }

    public void setToTime(String toTime) {
        this.toTime = toTime;
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getExceptionType() {
        return exceptionType;
    }

    public void setExceptionType(String exceptionType) {
        this.exceptionType = exceptionType;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public Integer getTop() {
        return top;
    }

    public void setTop(Integer top) {
        this.top = top;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public String getOrderedField() {
        return orderedField;
    }

    public void setOrderedField(String orderedField) {
        this.orderedField = orderedField;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }
}
