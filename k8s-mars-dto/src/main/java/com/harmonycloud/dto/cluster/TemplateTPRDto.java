package com.harmonycloud.dto.cluster;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.harmonycloud.k8s.bean.cluster.ClusterStatus;
import com.harmonycloud.k8s.bean.cluster.ClusterTemplate;

import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)
public class TemplateTPRDto {
    private String name;
    private Integer level;
    private String dataCenter;
    private List<ClusterTemplate> template;
    private ClusterStatus status;

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDataCenter() {
        return dataCenter;
    }

    public void setDataCenter(String dataCenter) {
        this.dataCenter = dataCenter;
    }

    public List<ClusterTemplate> getTemplate() {
        return template;
    }

    public void setTemplate(List<ClusterTemplate> template) {
        this.template = template;
    }

    public ClusterStatus getStatus() {
        return status;
    }

    public void setStatus(ClusterStatus status) {
        this.status = status;
    }
}
