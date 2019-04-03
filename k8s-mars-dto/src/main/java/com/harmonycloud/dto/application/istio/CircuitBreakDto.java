package com.harmonycloud.dto.application.istio;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

@ApiModel(value = "服务熔断策略信息")
public class CircuitBreakDto extends BaseIstioPolicyDto {

    @ApiModelProperty(value = "TCP连接数", name = "maxConnections", example = "1", required = true)
    private Integer maxConnections;

    @ApiModelProperty(value = "HTTP协议版本", name = "httpVersion", example = "", notes = "http1/http2;")
    private String httpVersion;

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

    @ApiModelProperty(value = "最大熔断百分比", name = "maxEjectionPercent", example = "100", notes = "该字段暂不提供客户编辑")
    private Integer maxEjectionPercent;

    public Integer getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(Integer maxConnections) {
        this.maxConnections = maxConnections;
    }

    public String getHttpVersion() {
        return httpVersion;
    }

    public void setHttpVersion(String httpVersion) {
        this.httpVersion = httpVersion;
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
        //maxEjectionPercent字段不提供给客户编辑，默认设置为100，放到此处处理后期如果有变动相对改动最小
        if (Objects.nonNull(baseEjectionTime) && baseEjectionTime != 0) {
            this.setMaxEjectionPercent(100);
        }
        this.baseEjectionTime = baseEjectionTime;
    }

    public Integer getMaxEjectionPercent() {
        return maxEjectionPercent;
    }

    public void setMaxEjectionPercent(Integer maxEjectionPercent) {
        this.maxEjectionPercent = maxEjectionPercent;
    }
}
