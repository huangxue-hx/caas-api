package com.harmonycloud.common.enumm;

/**
 * Created by andy on 17-1-22.
 */
public enum HarborProjectRoleEnum {
    DEV("harbor_project_developer"),
    WATCHER("harbor_project_watcher");

    private String role;

    HarborProjectRoleEnum(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
