package com.harmonycloud.dto.application.istio;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * create by weg on 18-11-27
 */
@ApiModel(value = "RateLimitDto信息")
public class RateLimitOverrideDto {

    @ApiModelProperty(value = "作用域分区名", name = "scopeNamespace", example = "default")
    private String scopeNamespace;

    @ApiModelProperty(value = "作用域服务名", name = "serviceName", example = "serviceName")
    private String scopeServiceName;

    public String getScopeNamespace() {
        return scopeNamespace;
    }

    public void setScopeNamespace(String scopeNamespace) {
        this.scopeNamespace = scopeNamespace;
    }

    public String getScopeServiceName() {
        return scopeServiceName;
    }

    public void setScopeServiceName(String scopeServiceName) {
        this.scopeServiceName = scopeServiceName;
    }
}
