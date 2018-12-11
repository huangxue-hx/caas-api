package com.harmonycloud.dto.application.istio;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Map;

/**
 * create by weg on 18-12-3
 */
@ApiModel(value = "TrafficShiftingSourceDto信息")
public class TrafficShiftingSourceDto {

    @ApiModelProperty(value = "请求头", name = "headers", example = "", notes = "")
    private Map<String, String> headers;

    @ApiModelProperty(value = "请求服务", name = "sourceName", example = "", notes = "")
    private String sourceName;

    @ApiModelProperty(value = "请求服务版本", name = "sourceVersion", example = "", notes = "")
    private String sourceVersion;

    @ApiModelProperty(value = "目标服务名称", name = "subset", example = "name")
    private String subset;

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getSourceVersion() {
        return sourceVersion;
    }

    public void setSourceVersion(String sourceVersion) {
        this.sourceVersion = sourceVersion;
    }

    public String getSubset() {
        return subset;
    }

    public void setSubset(String subset) {
        this.subset = subset;
    }
}
