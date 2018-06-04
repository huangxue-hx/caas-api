package com.harmonycloud.k8s.bean.cluster;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.harmonycloud.k8s.bean.BaseResource;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ClusterSpec  implements Serializable {
    private static final long serialVersionUID = 5335063600024281314L;
    private ClusterInfo info ;
    private List<ClusterTemplate>  template ;

    public ClusterInfo getInfo() {
        return info;
    }

    public void setInfo(ClusterInfo info) {
        this.info = info;
    }

    public List<ClusterTemplate> getTemplate() {
        return template;
    }

    public void setTemplate(List<ClusterTemplate> template) {
        this.template = template;
    }
}
