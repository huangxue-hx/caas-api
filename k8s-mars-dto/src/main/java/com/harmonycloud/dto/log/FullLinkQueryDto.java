package com.harmonycloud.dto.log;


import org.hibernate.validator.constraints.NotBlank;

/**
 * Created by zhangkui on 2017/3/31.
 * 日志查询参数对象
 */
public class FullLinkQueryDto {

    @NotBlank(message = "分区不能为空")
    private String namespace;
    @NotBlank(message = "服务名不能为空")
    private String deployment;

    /**
     * 绝对时间区间查询方式 日志开始时间
     * format: yyyy-MM-dd hh:mm:ss
     */
    @NotBlank(message = "开始时间不能为空")
    private String fromTime;
    /**
     * 绝对时间区间查询方式 日志结束时间
     */
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

    public String getDeployment() {
        return deployment;
    }

    public void setDeployment(String deployment) {
        this.deployment = deployment;
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
