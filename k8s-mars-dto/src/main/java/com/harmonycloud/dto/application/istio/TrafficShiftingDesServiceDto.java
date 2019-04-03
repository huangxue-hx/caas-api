package com.harmonycloud.dto.application.istio;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import java.util.Map;

/**
 * create by weg on 18-12-3
 */
@ApiModel(value = "TrafficShiftingDesServicesDto信息")
public class TrafficShiftingDesServiceDto {


    @ApiModelProperty(value = "目标服务名称", name = "subset", example = "name")
    private String subset;

    @ApiModelProperty(value = "分流权重", name = "weight", example = "50")
    private String weight;

    public String getSubset() {
        return subset;
    }

    public void setSubset(String subset) {
        this.subset = subset;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }
}
