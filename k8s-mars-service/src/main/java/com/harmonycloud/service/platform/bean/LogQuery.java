package com.harmonycloud.service.platform.bean;


/**
 * Created by zhangkui on 2017/3/31.
 * 日志查询参数对象
 */
public class LogQuery{

    private String logDateStart;
    private String logDateEnd;
    private String namespace;
    private String appName;
    private String appType;
    private String container;
    private String logDir;
    private String pod;
    private String clusterId;
    /**
     * Elasticsearch分页id
     */
    private String scrollId;
    /**
     * 日志级别 ，I-info,E-error,W-warn
     */
    private String severity;
    /**
     * 日志内容查询关键字
     */
    private String searchWord;

    private Integer pageSize;

    private String searchType;

    private String[] indexes;

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getContainer() {
        return container;
    }

    public void setContainer(String container) {
        this.container = container;
    }

    public String getLogDateStart() {
        return logDateStart;
    }

    public void setLogDateStart(String logDateStart) {
        this.logDateStart = logDateStart;
    }

    public String getLogDateEnd() {
        return logDateEnd;
    }

    public void setLogDateEnd(String logDateEnd) {
        this.logDateEnd = logDateEnd;
    }

    public String getLogDir() {
        return logDir;
    }

    public void setLogDir(String logDir) {
        this.logDir = logDir;
    }

    public String getScrollId() {
        return scrollId;
    }

    public void setScrollId(String scrollId) {
        this.scrollId = scrollId;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getSearchWord() {
        return searchWord;
    }

    public void setSearchWord(String searchWord) {
        this.searchWord = searchWord;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getPod() {
        return pod;
    }

    public void setPod(String pod) {
        this.pod = pod;
    }

    public String[] getIndexes() {
        return indexes;
    }

    public void setIndexes(String[] indexes) {
        this.indexes = indexes;
    }


    public String getSearchType() {
        return searchType;
    }

    public void setSearchType(String searchType) {
        this.searchType = searchType;
    }
}
