package com.harmonycloud.dto.business;

/**
 * Created by root on 8/11/17.
 */
public class YamlDto {

    private String tentantName;

    private String tentantID;

    private String appName;

    private String appDesc;

    private String nameSpaces;

    private String yaml;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppDesc() {
        return appDesc;
    }

    public void setAppDesc(String appDesc) {
        this.appDesc = appDesc;
    }

    public String getYaml() {
        return yaml;
    }

    public void setYaml(String yaml) {
        this.yaml = yaml;
    }

    public String getNameSpaces() {
        return nameSpaces;
    }

    public void setNameSpaces(String nameSpaces) {
        this.nameSpaces = nameSpaces;
    }

    public String getTentantName() {
        return tentantName;
    }

    public void setTentantName(String tentantName) {
        this.tentantName = tentantName;
    }

    public String getTentantID() {
        return tentantID;
    }

    public void setTentantID(String tentantID) {
        this.tentantID = tentantID;
    }
}