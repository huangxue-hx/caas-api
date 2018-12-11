package com.harmonycloud.dto.application.istio;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * create by weg on 18-12-3
 */
@ApiModel(value = "TrafficShiftingDto信息")
public class TrafficShiftingDto extends BaseIstioPolicyDto {

    @ApiModelProperty(value = "协议", name = "protocol", example = "http", notes = "暂时只支持http")
    private String protocol;

    @ApiModelProperty(value = "目标分流服务", name = "desServices", example = "")
    private List<TrafficShiftingDesServicesDto> desServices;

    @ApiModelProperty(value = "源服务", name = "sourceServices", example = "")
    private List<TrafficShiftingSourceDto> sourceServices;

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public List<TrafficShiftingDesServicesDto> getDesServices() {
        return desServices;
    }

    public void setDesServices(List<TrafficShiftingDesServicesDto> desServices) {
        this.desServices = desServices;
    }

    public List<TrafficShiftingSourceDto> getSourceServices() {
        return sourceServices;
    }

    public void setSourceServices(List<TrafficShiftingSourceDto> sourceServices) {
        this.sourceServices = sourceServices;
    }
}
