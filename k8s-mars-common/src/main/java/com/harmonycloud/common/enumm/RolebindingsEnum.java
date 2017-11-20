package com.harmonycloud.common.enumm;

/**
 * Created by andy on 17-1-22.
 */
public enum RolebindingsEnum {

    TM_RB("tm-rb"),
    DEV_RB("dev-rb"),
    OPS_RB("ops-rb"),
    TEST_RB("test-rb");

    private String name;

    RolebindingsEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
