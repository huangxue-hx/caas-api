package com.harmonycloud.k8s.bean.cluster;

import java.io.Serializable;
import java.util.List;

public class ClusterStatus implements Serializable {
    private static final long serialVersionUID = -2766431032517554685L;
    private List<StatusConditions> conditions ;

    public List<StatusConditions> getConditions() {
        return conditions;
    }

    public void setConditions(List<StatusConditions> conditions) {
        this.conditions = conditions;
    }
}
