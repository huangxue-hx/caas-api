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
     * 查询分区名
     */
    private String namespace;


    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getKeyWords() {
        return keyWords;
    }

    public void setKeyWords(String keyWords) {
        this.keyWords = keyWords;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getScrollId() {
        return scrollId;
    }

    public void setScrollId(String scrollId) {
        this.scrollId = scrollId;
    }

    public String getVerbName() {
        return verbName;
    }

    public void setVerbName(String verbName) {
        this.verbName = verbName;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }
}
