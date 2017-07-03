package com.harmonycloud.service.platform.bean;


import java.util.Date;

/**
 * Created by zhangkui on 2017/3/31.
 * 日志查询参数对象
 */
public class LogQuery{

    private String logDateStart;
    private String logDateEnd;
    private String namespace;
    private String container;
    private String logDir;
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
}
