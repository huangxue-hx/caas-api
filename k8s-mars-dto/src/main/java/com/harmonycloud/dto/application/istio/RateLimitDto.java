package com.harmonycloud.dto.application.istio;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;
import java.util.List;

/**
 * update by weg on 18-11-27
 */
@ApiModel(value = "RateLimitDto信息")
public class RateLimitDto extends BaseIstioPolicyDto {

    @ApiModelProperty(value = "作用域服务", name = "override", example = "override")
    private List<RateLimitOverrideDto> overrides;

    @ApiModelProperty(value = "限速策略", name = "algorithm", example = "FIXED_WINDOW、ROLLING_WINDOW", required = true)
    private String algorithm;

    @ApiModelProperty(value = "最大流量-请求数", name = "maxAmount", example = "> 0", required = true)
    private String maxAmount;

    @ApiModelProperty(value = "最大流量-时间", name = "validDuration", example = "> 0", required = true)
    private String validDuration;

    @ApiModelProperty(value = "缓冲时间", name = "bucketDuration", example = "> 0", required = true)
    private String bucketDuration;

    @ApiModelProperty(value = "匹配请求中包含用户信息", name = "username", example = "admin")
    private String username;

    public List<RateLimitOverrideDto> getOverrides() {
        return overrides;
    }

    public void setOverrides(List<RateLimitOverrideDto> overrides) {
        this.overrides = overrides;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(String maxAmount) {
        this.maxAmount = maxAmount;
    }

    public String getValidDuration() {
        return validDuration;
    }

    public void setValidDuration(String validDuration) {
        this.validDuration = validDuration;
    }

    public String getBucketDuration() {
        return bucketDuration;
    }

    public void setBucketDuration(String bucketDuration) {
        this.bucketDuration = bucketDuration;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
