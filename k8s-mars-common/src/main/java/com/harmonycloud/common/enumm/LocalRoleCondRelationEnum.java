package com.harmonycloud.common.enumm;

import org.apache.commons.lang3.StringUtils;

/**
 * 局部角色条件规则：各个条件之间的关系
 */
public enum LocalRoleCondRelationEnum {
    AND("and"), OR("or");

    private String relationStr;
    LocalRoleCondRelationEnum(String relationStr) {
        this.relationStr = relationStr;
    }

    public static LocalRoleCondRelationEnum getEnum(String op){
        LocalRoleCondRelationEnum[] localRoleCondRelationEnums = values();
        for (LocalRoleCondRelationEnum localRoleCondRelationEnum: localRoleCondRelationEnums){
            if (StringUtils.equals(localRoleCondRelationEnum.getRelationStr(), op)){
                return localRoleCondRelationEnum;
            }
        }
        return null;
    }

    public String getRelationStr() {
        return relationStr;
    }



}
