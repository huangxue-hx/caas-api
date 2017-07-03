package com.harmonycloud.dto.tenant.show;

/**
 * Created by andy on 17-2-7.
 */
public class RolebindingShowDto {

    private String name;

    private String roleBindingName;

    private String role;

    private String namespace;

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

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
