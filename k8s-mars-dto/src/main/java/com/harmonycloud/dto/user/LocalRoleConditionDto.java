package com.harmonycloud.dto.user;

import java.util.List;

/**
 * 局部角色条件规则对应的Dto
 *
 */
public class LocalRoleConditionDto {
    private List<LocalRoleCondRuleDto> rule;
    private String op;

    public List<LocalRoleCondRuleDto> getRule() {
        return rule;
    }

    public void setRule(List<LocalRoleCondRuleDto> rule) {
        this.rule = rule;
    }

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }
}
