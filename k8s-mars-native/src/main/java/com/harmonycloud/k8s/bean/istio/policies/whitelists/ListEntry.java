package com.harmonycloud.k8s.bean.istio.policies.whitelists;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.harmonycloud.k8s.bean.BaseResource;

/**
 * update by weg on 18-11-27.
 */
@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ListEntry extends BaseResource {

    private ListEntrySpec spec;

    public ListEntrySpec getSpec() {
        return spec;
    }

    public void setSpec(ListEntrySpec spec) {
        this.spec = spec;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        ListEntry listEntry = (ListEntry) o;
        if (this.spec != null) {
            if (!this.spec.equals(listEntry.spec)) {
                return false;
            }
        } else {
            if (listEntry.spec != null) {
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
