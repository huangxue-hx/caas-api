package com.harmonycloud.dto.tenant;

public class PersistentVolumeDto {
    private String name;
    private String tenantid;
    private String type;
    private boolean readOnly;
    private boolean multiple;
    private String capacity;
    private String path;
    private String nfsServer;
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
    public String getCapacity() {
        return capacity;
    }
    public void setCapacity(String capacity) {
        this.capacity = capacity;
    }
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
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public boolean isReadOnly() {
        return readOnly;
    }
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }
    public boolean isMultiple() {
        return multiple;
    }
    public void setMultiple(boolean multiple) {
        this.multiple = multiple;
    }

}
