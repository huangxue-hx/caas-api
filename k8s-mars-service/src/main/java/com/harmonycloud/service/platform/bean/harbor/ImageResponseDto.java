package com.harmonycloud.service.platform.bean.harbor;

import java.util.Date;
import java.util.List;

/**
 * 接收新harbor /repositories接口的返回值
 * 老harbor返回镜像名列表，新harbor返回镜像json数据
 */
public class ImageResponseDto {

    private String id;
    private String name;
    private String project_id;
    private String description;
    private String pull_count;
    private String star_count;
    private String tags_count;
    private List<String> labels;
    private Date creation_time;
    private Date update_time;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProject_id() {
        return project_id;
    }

    public void setProject_id(String project_id) {
        this.project_id = project_id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPull_count() {
        return pull_count;
    }

    public void setPull_count(String pull_count) {
        this.pull_count = pull_count;
    }

    public String getStar_count() {
        return star_count;
    }

    public void setStar_count(String star_count) {
        this.star_count = star_count;
    }

    public String getTags_count() {
        return tags_count;
    }

    public void setTags_count(String tags_count) {
        this.tags_count = tags_count;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public Date getCreation_time() {
        return creation_time;
    }

    public void setCreation_time(Date creation_time) {
        this.creation_time = creation_time;
    }

    public Date getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(Date update_time) {
        this.update_time = update_time;
    }




}
