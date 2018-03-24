package com.harmonycloud.service.platform.bean.microservice;

/**
 * @Author jiangmi
 * @Description
 * @Date created in 2017-12-7
 * @Modified
 */
public class MsfDeploymentVolume {
    private String mount_type;

    private String mount_path;

    private String path;

    public String getMount_type() {
        return mount_type;
    }

    public void setMount_type(String mount_type) {
        this.mount_type = mount_type;
    }

    public String getMount_path() {
        return mount_path;
    }

    public void setMount_path(String mount_path) {
        this.mount_path = mount_path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFile_url() {
        return file_url;
    }

    public void setFile_url(String file_url) {
        this.file_url = file_url;
    }

    private String file_url;
}
