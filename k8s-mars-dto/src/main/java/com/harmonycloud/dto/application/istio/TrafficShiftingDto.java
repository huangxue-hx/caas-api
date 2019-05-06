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
    private List<TrafficShiftingDesServiceDto> desServices;

    @ApiModelProperty(value = "源服务", name = "sourceServices", example = "")
    private List<TrafficShiftingMatchDto> matches;

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public List<TrafficShiftingDesServiceDto> getDesServices() {
        return desServices;
    }

    public void setDesServices(List<TrafficShiftingDesServiceDto> desServices) {
        this.desServices = desServices;
    }

    public List<TrafficShiftingMatchDto> getMatches() {
        return matches;
    }

    public void setMatches(List<TrafficShiftingMatchDto> matches) {
        this.matches = matches;
    }
}
