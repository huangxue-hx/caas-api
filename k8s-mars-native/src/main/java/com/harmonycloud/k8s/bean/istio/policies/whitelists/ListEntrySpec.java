package com.harmonycloud.k8s.bean.istio.policies.whitelists;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * update by weg on 18-11-27.
 */
@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ListEntrySpec {

    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        ListEntrySpec spec = (ListEntrySpec) o;
        if (this.value != null) {
            if (!this.value.equals(spec.value)) {
                return false;
            }
        } else {
            if (spec.value != null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return getValue() != null ? getValue().hashCode() : 0;
    }
}
