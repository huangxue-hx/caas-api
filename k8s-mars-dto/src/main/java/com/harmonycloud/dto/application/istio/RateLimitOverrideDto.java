package com.harmonycloud.dto.application.istio;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * create by weg on 18-11-27
 */
@ApiModel(value = "RateLimitDto信息")
public class RateLimitOverrideDto {

    @ApiModelProperty(value = "作用域服务名", name = "serviceName", example = "serviceName")
    private String scopeServiceName;

    @ApiModelProperty(value = "请求头信息", name = "headers", example = "key=value", notes = "key不可以是sourceName")
    private List<String> headers;

    @ApiModelProperty(value = "最大流量-请求数", name = "maxAmount", example = "> 0", required = true)
    private String maxAmount;

    public String getScopeServiceName() {
        return scopeServiceName;
    }

    public void setScopeServiceName(String scopeServiceName) {
        this.scopeServiceName = scopeServiceName;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public void setHeaders(List<String> headers) {
        this.headers = headers;
    }

    public String getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(String maxAmount) {
        this.maxAmount = maxAmount;
    }
}
