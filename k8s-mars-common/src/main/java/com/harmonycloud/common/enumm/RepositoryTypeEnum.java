package com.harmonycloud.common.enumm;

/**
 * Created by anson on 17/6/2.
 */
public enum RepositoryTypeEnum {
    GIT("git"),
    SVN("svn");

    private String type;

    RepositoryTypeEnum(String type){
        this.type = type;
    }

    public String getType() {
        return type;
    }

    private void setType(String type) {
        this.type = type;
    }
}
