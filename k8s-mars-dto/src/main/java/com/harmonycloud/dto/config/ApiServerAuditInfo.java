package com.harmonycloud.dto.config;

/**
 * @author liangli
 */
public class ApiServerAuditInfo {

    private String actionTime;
    private String verb;
    private String clusterAliasName;
    private String namespace;
    private String actionObject;
    private String actionObjectName;
    private String requestUrl;
    private String actionResult;
    private String requestSpendTime;
    private long currentUrlCount;

    public String getActionTime() {
        return actionTime;
    }

    public void setActionTime(String actionTime) {
        this.actionTime = actionTime;
    }

    public String getVerb() {
        return verb;
    }

    public void setVerb(String verb) {
        this.verb = verb;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getActionObject() {
        return actionObject;
    }

    public void setActionObject(String actionObject) {
        this.actionObject = actionObject;
    }

    public String getActionObjectName() {
        return actionObjectName;
    }

    public void setActionObjectName(String actionObjectName) {
        this.actionObjectName = actionObjectName;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public String getActionResult() {
        return actionResult;
    }

    public void setActionResult(String actionResult) {
        this.actionResult = actionResult;
    }

    public String getRequestSpendTime() {
        return requestSpendTime;
    }

    public void setRequestSpendTime(String requestSpendTime) {
        this.requestSpendTime = requestSpendTime;
    }

    public long getCurrentUrlCount() {
        return currentUrlCount;
    }

    public void setCurrentUrlCount(long currentUrlCount) {
        this.currentUrlCount = currentUrlCount;
    }

    public String getClusterAliasName() {
        return clusterAliasName;
    }

    public void setClusterAliasName(String clusterAliasName) {
        this.clusterAliasName = clusterAliasName;
    }
}
