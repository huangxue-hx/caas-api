package com.harmonycloud.dao.user.bean;

public class UserGroupRelation {
    private Long userid;

    private Integer groupid;

    public Long getUserid() {
        return userid;
    }

    public void setUserid(Long userid) {
        this.userid = userid;
    }

    public Integer getGroupid() {
        return groupid;
    }

    public void setGroupid(Integer groupid) {
        this.groupid = groupid;
    }
}