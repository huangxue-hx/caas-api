package com.harmonycloud.k8s.bean.istio.policies.whitelists;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.harmonycloud.k8s.bean.BaseResource;

/**
 * update by weg on 18-11-27.
 */
@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ListChecker extends BaseResource {

    private ListCheckerSpec spec;

    public ListCheckerSpec getSpec() {
        return spec;
    }

    public void setSpec(ListCheckerSpec spec) {
        this.spec = spec;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        ListChecker listChecker = (ListChecker) o;
        if (this.spec != null) {
            if (!this.spec.equals(listChecker.spec)) {
                return false;
            }
        } else {
            if (listChecker.spec != null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return getSpec() != null ? getSpec().hashCode() : 0;
    }
}
