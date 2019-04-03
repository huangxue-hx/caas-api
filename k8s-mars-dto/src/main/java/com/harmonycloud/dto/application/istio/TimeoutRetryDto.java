package com.harmonycloud.dto.application.istio;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * create by weg on 18-12-5
 */
@ApiModel(value = "TimeoutRetryDto信息")
public class TimeoutRetryDto extends BaseIstioPolicyDto {

    @ApiModelProperty(value = "超时时间", name = "timeout", example = "10")
    private String timeout;

    @ApiModelProperty(value = "重试次数", name = "attempts", example = "10")
    private String attempts;

    @ApiModelProperty(value = "重试间隔时间", name = "perTryTimeout", example = "5")
    private String perTryTimeout;

    @ApiModelProperty(value = "服务域名external", name = "host", example = "0", notes = "external时使用")
    private String host;

    public String getTimeout() {
        return timeout;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

    public String getAttempts() {
        return attempts;
    }

    public void setAttempts(String attempts) {
        this.attempts = attempts;
    }

    public String getPerTryTimeout() {
        return perTryTimeout;
    }

    public void setPerTryTimeout(String perTryTimeout) {
        this.perTryTimeout = perTryTimeout;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
}
