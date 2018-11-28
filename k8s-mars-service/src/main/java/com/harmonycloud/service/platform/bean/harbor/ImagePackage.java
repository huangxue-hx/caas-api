package com.harmonycloud.service.platform.bean.harbor;


import java.util.Map;

/**
 * 整合新harbor的根据package扫描漏洞接口返回值为老harbor格式的
 *
 */
public class ImagePackage {

    private String packageName;
    private String packageVersion;
    private String fixedVersion;
    private Map<String,Object> vulnerabilitles;
    private Map<String,Object> pemaning_after_upgrade;//老harbor存在的接口，用fixedVersion版本修复后漏洞情况，新harbor暂时无法获取到，预留该属性

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getPackageVersion() {
        return packageVersion;
    }

    public void setPackageVersion(String packageVersion) {
        this.packageVersion = packageVersion;
    }

    public String getFixedVersion() {
        return fixedVersion;
    }

    public void setFixedVersion(String fixedVersion) {
        this.fixedVersion = fixedVersion;
    }

    public Map<String, Object> getVulnerabilitles() {
        return vulnerabilitles;
    }

    public void setVulnerabilitles(Map<String, Object> vulnerabilitles) {
        this.vulnerabilitles = vulnerabilitles;
    }

    public Map<String, Object> getPemaning_after_upgrade() {
        return pemaning_after_upgrade;
    }

    public void setPemaning_after_upgrade(Map<String, Object> pemaning_after_upgrade) {
        this.pemaning_after_upgrade = pemaning_after_upgrade;
    }
}
