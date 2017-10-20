package com.harmonycloud.service.platform.bean;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by zsl on 2017/2/4.
 * harbor manifest bean
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HarborManifest implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String author;  //作者
    private String createTime;  //创建时间
    private String tag;
    private Map<String, Object> vulnerabilitiesByPackage;
    private Map<String, Object> vulnerabilitySummary;
    private Integer vulnerabilityNum;
    private boolean abnormal = false;
    private boolean notSupported = false;

    public Integer getVulnerabilityNum() {
        return vulnerabilityNum;
    }

    public void setVulnerabilityNum(Integer vulnerabilityNum) {
        this.vulnerabilityNum = vulnerabilityNum;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Map<String, Object> getVulnerabilitiesByPackage() {
        return vulnerabilitiesByPackage;
    }

    public void setVulnerabilitiesByPackage(Map<String, Object> vulnerabilitiesByPackage) {
        this.vulnerabilitiesByPackage = vulnerabilitiesByPackage;
    }

    public Map<String, Object> getVulnerabilitySummary() {
        return vulnerabilitySummary;
    }

    public void setVulnerabilitySummary(Map<String, Object> vulnerabilitySummary) {
        this.vulnerabilitySummary = vulnerabilitySummary;
    }


    public boolean getNotSupported() {
        return notSupported;
    }

    public void setNotSupported(boolean notSupported) {
        this.notSupported = notSupported;
    }

    public boolean getAbnormal() {
        return abnormal;
    }

    public void setAbnormal(boolean abnormal) {
        this.abnormal = abnormal;
    }
}
