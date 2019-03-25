package com.harmonycloud.dto.application.istio;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "服务熔断策略信息")
public class CircuitBreakDto extends BaseIstioPolicyDto {

    @ApiModelProperty(value = "TCP连接数", name = "maxConnections", example = "1", required = true)
    private Integer maxConnections;

    @ApiModelProperty(value = "HTTP最大挂起数", name = "http1MaxPendingRequests", example = "1", required = true)
    private Integer http1MaxPendingRequests;

    @ApiModelProperty(value = "HTTP2最大挂起数", name = "Http2MaxRequests", example = "1", required = true)
    private Integer http2MaxRequests;

    @ApiModelProperty(value = "单连接请求最大数", name = "maxRequestsPerConnection", example = "1", required = true)
    private Integer maxRequestsPerConnection;

    @ApiModelProperty(value = "错误次数", name = "consecutiveErrors", example = "1", required = true)
    private Integer consecutiveErrors;

    @ApiModelProperty(value = "单位时间", name = "interval", example = "1", required = true)
    private Integer interval;

    @ApiModelProperty(value = "熔断持续时间", name = "baseEjectionTime", example = "180", required = true)
    private Integer baseEjectionTime;

    public Integer getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(Integer maxConnections) {
        this.maxConnections = maxConnections;
    }

    public Integer getHttp1MaxPendingRequests() {
        return http1MaxPendingRequests;
    }

    public void setHttp1MaxPendingRequests(Integer http1MaxPendingRequests) {
        this.http1MaxPendingRequests = http1MaxPendingRequests;
    }

    public Integer getHttp2MaxRequests() {
        return http2MaxRequests;
    }

    public void setHttp2MaxRequests(Integer http2MaxRequests) {
        this.http2MaxRequests = http2MaxRequests;
    }

    public Integer getMaxRequestsPerConnection() {
        return maxRequestsPerConnection;
    }

    public void setMaxRequestsPerConnection(Integer maxRequestsPerConnection) {
        this.maxRequestsPerConnection = maxRequestsPerConnection;
    }

    public Integer getConsecutiveErrors() {
        return consecutiveErrors;
    }

    public void setConsecutiveErrors(Integer consecutiveErrors) {
        this.consecutiveErrors = consecutiveErrors;
    }

    public Integer getInterval() {
        return interval;
    }

    public void setInterval(Integer interval) {
        this.interval = interval;
    }

    public Integer getBaseEjectionTime() {
        return baseEjectionTime;
    }

    public void setBaseEjectionTime(Integer baseEjectionTime) {
        this.baseEjectionTime = baseEjectionTime;
    }

}
