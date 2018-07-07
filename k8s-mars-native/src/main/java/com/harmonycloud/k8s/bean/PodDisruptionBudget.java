package com.harmonycloud.k8s.bean;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.harmonycloud.k8s.constant.Constant;

@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PodDisruptionBudget extends BaseResource {
    private PodDisruptionBudgetSpec spec;
    private PodDisruptionBudgetStatus status;

    public PodDisruptionBudget() {
        this.setKind("PodDisruptionBudget");
        this.setApiVersion(Constant.APIS_POLICY_V1BETA1);
    }

    public PodDisruptionBudgetSpec getSpec() {
        return spec;
    }

    public void setSpec(PodDisruptionBudgetSpec spec) {
        this.spec = spec;
    }

    public PodDisruptionBudgetStatus getStatus() {
        return status;
    }

    public void setStatus(PodDisruptionBudgetStatus status) {
        this.status = status;
    }
}
