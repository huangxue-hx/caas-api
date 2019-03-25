package com.harmonycloud.dto.apiserver;

/**
 * @author liliang
 * @time 2019-01-10
 * @description apiserver日志传输dto
 */
public class ApiServerAuditSearchDto {

    /**
     * 查询开始时间2019-01-11 11:11:11
     */
    private String startTime;

    /**
     * 查询结束时间2019-01-12 11:11:11
     */
    private String endTime;

    /**
     * 查询关键字
     */
    private String keyWords;
    /**
     * 页码
     */
    private Integer pageNum;
    /**
     * 每页数量
     */
    private Integer size;
    /**
     * ES滚动查询ID
     */
    private String scrollId;

    /**
     * 集群id
     */
    private String clusterId;

    /**
     * 查询动作名create、update、patch、delete等
     */
    private String verbName;

    /**
     * 查询url,不为空时表示只查询该url的不同时间的记录
     */
    private String url;

    /**
     * 查询分区名
     */
    private String namespace;


    public String getStartTime() {
        return startTime;
    }

    public ApiServerAuditSearchDto setStartTime(String startTime) {
        this.startTime = startTime;
        return this;
    }

    public String getEndTime() {
        return endTime;
    }

    public ApiServerAuditSearchDto setEndTime(String endTime) {
        this.endTime = endTime;
        return this;
    }

    public String getKeyWords() {
        return keyWords;
    }

    public ApiServerAuditSearchDto setKeyWords(String keyWords) {
        this.keyWords = keyWords;
        return this;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public ApiServerAuditSearchDto setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
        return this;
    }

    public Integer getSize() {
        return size;
    }

    public ApiServerAuditSearchDto setSize(Integer size) {
        this.size = size;
        return this;
    }

    public String getScrollId() {
        return scrollId;
    }

    public ApiServerAuditSearchDto setScrollId(String scrollId) {
        this.scrollId = scrollId;
        return this;
    }

    public String getVerbName() {
        return verbName;
    }

    public ApiServerAuditSearchDto setVerbName(String verbName) {
        this.verbName = verbName;
        return this;
    }

    public String getNamespace() {
        return namespace;
    }

    public ApiServerAuditSearchDto setNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    public String getClusterId() {
        return clusterId;
    }

    public ApiServerAuditSearchDto setClusterId(String clusterId) {
        this.clusterId = clusterId;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public ApiServerAuditSearchDto setUrl(String url) {
        this.url = url;
        return this;
    }
}
