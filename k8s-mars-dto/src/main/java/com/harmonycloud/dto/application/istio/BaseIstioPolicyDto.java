package com.harmonycloud.dto.application.istio;

import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

public class BaseIstioPolicyDto {

    @ApiModelProperty(value = "策略名称", name = "ruleName", example = "httpbin", required = true)
    private String ruleName;

    @ApiModelProperty(value = "策略关联ID", name = "ruleId", example = "asdf545adf5asd54fa65d4fade47895d")
    private String ruleId;

    @ApiModelProperty(value = "策略类型", name = "ruleType", example = "circuitBreaker")
    private String ruleType;

    @ApiModelProperty(value = "分区名称", name = "namespace", example = "coms-im", required = true)
    private String namespace;

    @ApiModelProperty(value = "服务名称", name = "serviceName", example = "httpbin")
    private String serviceName;

    @ApiModelProperty(value = "策略开关状态", name = "switchStatus", example = "1（开启）", notes = "1：开启；0：关闭")
    private String switchStatus;

    @ApiModelProperty(value = "策略状态", name = "dataStatus", example = "0（正常）", notes = "0：正常；1：异常；(11/12/13)：告警")
    private String dataStatus;

    @ApiModelProperty(value = "作用域", name = "scope", example = "0：全局；1：分区；2：服务")
    private String scope;

    @ApiModelProperty(value = "策略创建时间", name = "createTime", example = "2018-09-13T06:26:34Z")
    private Date createTime;

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public String getRuleType() {
        return ruleType;
    }

    public void setRuleType(String ruleType) {
        this.ruleType = ruleType;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getSwitchStatus() {
        return switchStatus;
    }

    public void setSwitchStatus(String switchStatus) {
        this.switchStatus = switchStatus;
    }

    public String getDataStatus() {
        return dataStatus;
    }

    public void setDataStatus(String dataStatus) {
        this.dataStatus = dataStatus;
    }

    public String getScope() {
        if (StringUtils.isEmpty(scope)) {
            scope = "0";
        }
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
