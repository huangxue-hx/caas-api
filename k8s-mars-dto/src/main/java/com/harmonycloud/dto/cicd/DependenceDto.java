package com.harmonycloud.dto.cicd;

/**
 * Created by anson on 17/8/1.
 */
public class DependenceDto {
    private String name;
    private String tenantid;
    private String path;
    private String nfsServer;
    private boolean common;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTenantid() {
        return tenantid;
    }

    public void setTenantid(String tenantid) {
        this.tenantid = tenantid;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getNfsServer() {
        return nfsServer;
    }

    public void setNfsServer(String nfsServer) {
        this.nfsServer = nfsServer;
    }

    public boolean isCommon() {
        return common;
    }

    public void setCommon(boolean common) {
        this.common = common;
    }
}
