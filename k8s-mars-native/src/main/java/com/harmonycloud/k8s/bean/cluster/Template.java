package com.harmonycloud.k8s.bean.cluster;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.harmonycloud.k8s.bean.BaseResource;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Template extends BaseResource implements Serializable {
    private static final long serialVersionUID = -8556122079556344167L;
    private List<ClusterTemplate> templateSpec;
    private ClusterStatus status;

    public List<ClusterTemplate> getTemplateSpec() {
        return templateSpec;
    }

    public void setTemplateSpec(List<ClusterTemplate> templateSpec) {
        this.templateSpec = templateSpec;
    }

    public ClusterStatus getStatus() {
        return status;
    }

    public void setStatus(ClusterStatus status) {
        this.status = status;
    }
}
