package com.harmonycloud.service.platform.bean.harbor;


import java.util.List;
import java.util.Map;

public class HarborPolicyFilter {
    private String repository;//只能过滤一个镜像
    private String tag;//只能过滤一个版本号
    private List<Map<String,Object>> labels;//可过滤多个标签

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public List<Map<String, Object>> getLabels() {
        return labels;
    }

    public void setLabels(List<Map<String, Object>> labels) {
        this.labels = labels;
    }
}