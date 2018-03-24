package com.harmonycloud.dto.log;


/**
 * Created by zhangkui on 2017/3/31.
 * 日志查询参数对象
 */
public class LogQueryDto{

    private String namespace;
    private String container;
    private String deployment;
    /**
     * 查询近XXX时间对应的单位，有分，小时，天（m,h,d）. 与recentTimeNum一起使用
     * 例：查询近30分钟内的log（recentTimeUnit=m,recentTimeNum=30）
     *     查询近2天内的log（recentTimeUnit=d,recentTimeNum=2）
     */
    private String recentTimeUnit;
    /**
     * 查询近XXX时间对应的数字，与recentTimeUnit一起使用
     */
    private Integer recentTimeNum;
    /**
     * 绝对时间区间查询方式 日志开始时间
     * 宿主机时区format: yyyy-MM-dd hh:mm:ss / 零时区format: yyyy-MM-dd'T'HH:mm:ss'Z'
     */
    private String logTimeStart;
    /**
     * 绝对时间区间查询方式 日志结束时间
     */
    private String logTimeEnd;
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

    private String logSource;

    private String pod;

    private String clusterId;

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

    public String getRecentTimeUnit() {
        return recentTimeUnit;
    }

    public void setRecentTimeUnit(String recentTimeUnit) {
        this.recentTimeUnit = recentTimeUnit;
    }

    public Integer getRecentTimeNum() {
        return recentTimeNum;
    }

    public void setRecentTimeNum(Integer recentTimeNum) {
        this.recentTimeNum = recentTimeNum;
    }

    public String getLogTimeStart() {
        return logTimeStart;
    }

    public void setLogTimeStart(String logTimeStart) {
        this.logTimeStart = logTimeStart;
    }

    public String getLogTimeEnd() {
        return logTimeEnd;
    }

    public void setLogTimeEnd(String logTimeEnd) {
        this.logTimeEnd = logTimeEnd;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    @Override
    public String toString() {
        return "LogQueryDto{" +
                "namespace='" + namespace + '\'' +
                ", container='" + container + '\'' +
                ", recentTimeUnit='" + recentTimeUnit + '\'' +
                ", recentTimeNum=" + recentTimeNum +
                ", logTimeStart='" + logTimeStart + '\'' +
                ", logTimeEnd='" + logTimeEnd + '\'' +
                ", logDir='" + logDir + '\'' +
                ", scrollId='" + scrollId + '\'' +
                ", severity='" + severity + '\'' +
                ", searchWord='" + searchWord + '\'' +
                ", pageSize=" + pageSize +
                '}';
    }

    public String getLogSource() {
        return logSource;
    }

    public void setLogSource(String logSource) {
        this.logSource = logSource;
    }

    public String getPod() {
        return pod;
    }

    public void setPod(String pod) {
        this.pod = pod;
    }

    public String getDeployment() {
        return deployment;
    }

    public void setDeployment(String deployment) {
        this.deployment = deployment;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }
}
