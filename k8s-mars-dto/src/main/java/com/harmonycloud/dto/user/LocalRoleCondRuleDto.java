package com.harmonycloud.dto.user;

import java.util.List;

/**
 * 局部角色条件规则：单个条件
 *
 */
public class LocalRoleCondRuleDto {

    private String field;
    private String op;
    private List<String> value;
    private boolean isReady;

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    public List<String> getValue() {
        return value;
    }

    public void setValue(List<String> value) {
        this.value = value;
    }

    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean ready) {
        isReady = ready;
    }
}
