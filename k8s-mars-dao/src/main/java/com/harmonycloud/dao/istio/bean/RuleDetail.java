package com.harmonycloud.dao.istio.bean;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;

public class RuleDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private String ruleId;
    private Integer ruleDetailOrder;
    private Date createTime;
    private Date updateTime;
    private byte[] ruleDetailContent;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId == null ? null : ruleId.trim();
    }

    public Integer getRuleDetailOrder() {
        return ruleDetailOrder;
    }

    public void setRuleDetailOrder(Integer ruleDetailOrder) {
        this.ruleDetailOrder = ruleDetailOrder;
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

    public byte[] getRuleDetailContent() {
        return ruleDetailContent;
    }

    public void setRuleDetailContent(byte[] ruleDetailContent) {
        this.ruleDetailContent = ruleDetailContent;
    }

}