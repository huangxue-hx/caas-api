package com.harmonycloud.dao.istio.bean;

import com.harmonycloud.common.Constant.CommonConstant;

import java.io.Serializable;
import java.util.Date;

public class RuleOverview implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private String ruleName;
    private String ruleId;
    private String ruleType;
    private String ruleScope;
    private String ruleClusterId;
    private String ruleNs;
    private String ruleSvc;
    private Integer ruleSourceNum;
    private Integer switchStatus;//0：关闭状态；1：开启状态；
    private Integer dataStatus = CommonConstant.DATA_IS_OK;//0：正常
    private Integer dataErrLoc = CommonConstant.DATA_IS_OK;
    private String userId;
    private Date createTime;
    private Date updateTime;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

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

    public String getRuleScope() {
        return ruleScope;
    }

    public void setRuleScope(String ruleScope) {
        this.ruleScope = ruleScope;
    }

    public String getRuleClusterId() {
        return ruleClusterId;
    }

    public void setRuleClusterId(String ruleClusterId) {
        this.ruleClusterId = ruleClusterId;
    }

    public String getRuleNs() {
        return ruleNs;
    }

    public void setRuleNs(String ruleNs) {
        this.ruleNs = ruleNs;
    }

    public String getRuleSvc() {
        return ruleSvc;
    }

    public void setRuleSvc(String ruleSvc) {
        this.ruleSvc = ruleSvc;
    }

    public Integer getRuleSourceNum() {
        return ruleSourceNum;
    }

    public void setRuleSourceNum(Integer ruleSourceNum) {
        this.ruleSourceNum = ruleSourceNum;
    }

    public Integer getSwitchStatus() {
        return switchStatus;
    }

    public void setSwitchStatus(Integer switchStatus) {
        this.switchStatus = switchStatus;
    }

    public Integer getDataStatus() {
        return dataStatus;
    }

    public void setDataStatus(Integer dataStatus) {
        this.dataStatus = dataStatus;
    }

    public Integer getDataErrLoc() {
        return dataErrLoc;
    }

    public void setDataErrLoc(Integer dataErrLoc) {
        this.dataErrLoc = dataErrLoc;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}