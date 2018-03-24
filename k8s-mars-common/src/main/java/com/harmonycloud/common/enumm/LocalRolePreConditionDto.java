package com.harmonycloud.common.enumm;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class LocalRolePreConditionDto {
    private short conditionType;
    private Map<String, Set<LocalRolePreFieldDto>> conditionFields;
    private List<LocalRolePreFieldDto> ops;
    private List<LocalRolePreFieldDto> resourceTypes;

    public short getConditionType() {
        return conditionType;
    }

    public void setConditionType(short conditionType) {
        this.conditionType = conditionType;
    }

    public Map<String, Set<LocalRolePreFieldDto>> getConditionFields() {
        return conditionFields;
    }

    public void setConditionFields(Map<String, Set<LocalRolePreFieldDto>> conditionFields) {
        this.conditionFields = conditionFields;
    }

    public List<LocalRolePreFieldDto> getOps() {
        return ops;
    }

    public void setOps(List<LocalRolePreFieldDto> ops) {
        this.ops = ops;
    }

    public List<LocalRolePreFieldDto> getResourceTypes() {
        return resourceTypes;
    }

    public void setResourceTypes(List<LocalRolePreFieldDto> resourceTypes) {
        this.resourceTypes = resourceTypes;
    }
}
