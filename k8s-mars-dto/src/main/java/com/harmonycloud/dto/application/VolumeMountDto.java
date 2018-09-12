package com.harmonycloud.dto.application;

/**
 * Created by anson on 18/8/8.
 */
public class VolumeMountDto {
    private String name;
    private String mountPath;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMountPath() {
        return mountPath;
    }

    public void setMountPath(String mountPath) {
        this.mountPath = mountPath;
    }
}
