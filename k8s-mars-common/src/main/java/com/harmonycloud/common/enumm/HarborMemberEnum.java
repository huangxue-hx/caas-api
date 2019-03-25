package com.harmonycloud.common.enumm;

public enum HarborMemberEnum {

    PROJECTADMIN(1),
    DEVELOPER(2),
    GUEST(3),
    NONE(4);

    private Integer level;
    HarborMemberEnum(Integer level) {
        this.level = level;
    }
    public static HarborMemberEnum getMemberLevel(String memberName) {
        for (HarborMemberEnum levelEnum : HarborMemberEnum.values()) {
            if(levelEnum.name().equalsIgnoreCase(memberName)) {
                return levelEnum;
            }
        }
        return null;
    }

    public Integer getLevel() {
        return level;
    }
}
