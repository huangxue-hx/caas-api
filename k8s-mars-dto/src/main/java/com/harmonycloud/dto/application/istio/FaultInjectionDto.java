package com.harmonycloud.dto.application.istio;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * create by weg on 18-12-5
 */
@ApiModel(value = "FaultInjectionDto信息")
public class FaultInjectionDto extends BaseIstioPolicyDto {

    @ApiModelProperty(value = "延迟注入时间", name = "fixedDelay", example = "10")
    private String fixedDelay;

    @ApiModelProperty(value = "延迟错误注入百分比", name = "percent", example = "10")
    private String delayPercent;

    @ApiModelProperty(value = "错误状态code", name = "httpStatus", example = "500")
    private String httpStatus;

    @ApiModelProperty(value = "错误code注入百分比", name = "httpStatus", example = "10")
    private String codePercent;

    public String getFixedDelay() {
        return fixedDelay;
    }

    public void setFixedDelay(String fixedDelay) {
        this.fixedDelay = fixedDelay;
    }

    public String getDelayPercent() {
        return delayPercent;
    }

    public void setDelayPercent(String delayPercent) {
        this.delayPercent = delayPercent;
    }

    public String getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(String httpStatus) {
        this.httpStatus = httpStatus;
    }

    public String getCodePercent() {
        return codePercent;
    }

    public void setCodePercent(String codePercent) {
        this.codePercent = codePercent;
    }
}
