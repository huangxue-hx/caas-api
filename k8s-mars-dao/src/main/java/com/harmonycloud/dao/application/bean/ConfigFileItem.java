package com.harmonycloud.dao.application.bean;

import java.io.Serializable;

public class ConfigFileItem implements Serializable {
    private Integer id;

    private String configfileId;

    private String path;

    private String fileName;

    private String content;

    private static final long serialVersionUID = 1L;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getConfigfileId() {
        return configfileId;
    }

    public void setConfigfileId(String configfileId) {
        this.configfileId = configfileId == null ? null : configfileId.trim();
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path == null ? null : path.trim();
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName == null ? null : fileName.trim();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content == null ? null : content.trim();
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        ConfigFileItem other = (ConfigFileItem) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getConfigfileId() == null ? other.getConfigfileId() == null : this.getConfigfileId().equals(other.getConfigfileId()))
            && (this.getPath() == null ? other.getPath() == null : this.getPath().equals(other.getPath()))
            && (this.getFileName() == null ? other.getFileName() == null : this.getFileName().equals(other.getFileName()))
            && (this.getContent() == null ? other.getContent() == null : this.getContent().equals(other.getContent()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getConfigfileId() == null) ? 0 : getConfigfileId().hashCode());
        result = prime * result + ((getPath() == null) ? 0 : getPath().hashCode());
        result = prime * result + ((getFileName() == null) ? 0 : getFileName().hashCode());
        result = prime * result + ((getContent() == null) ? 0 : getContent().hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "ConfigFileItem{" +
                "id=" + id +
                ", configfileId='" + configfileId + '\'' +
                ", path='" + path + '\'' +
                ", fileName='" + fileName + '\'' +
                '}';
    }
}