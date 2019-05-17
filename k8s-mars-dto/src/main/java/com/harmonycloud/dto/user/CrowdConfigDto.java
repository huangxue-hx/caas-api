package com.harmonycloud.dto.user;

public class CrowdConfigDto {
    private String address;
    private String username;
    private String password;
    private Integer isAccess;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getIsAccess() {
        return isAccess;
    }

    public void setIsAccess(Integer isAccess) {
        this.isAccess = isAccess;
    }
}
