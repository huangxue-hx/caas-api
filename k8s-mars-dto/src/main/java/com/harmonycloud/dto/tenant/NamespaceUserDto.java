package com.harmonycloud.dto.tenant;

import java.io.Serializable;

/**
 * Created by andy on 17-1-17.
 */
public class NamespaceUserDto implements Serializable{

    private static final long serialVersionUID = -7854674126525524786L;

    private String name;

    private String roleBindingName;

    private String role;

    private String time;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRoleBindingName() {
        return roleBindingName;
    }

    public void setRoleBindingName(String roleBindingName) {
        this.roleBindingName = roleBindingName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
