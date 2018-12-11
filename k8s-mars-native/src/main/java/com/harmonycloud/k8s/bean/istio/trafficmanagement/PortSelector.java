package com.harmonycloud.k8s.bean.istio.trafficmanagement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Create by weg on 18-11-30.
 */
@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PortSelector {

    private Integer number;

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        PortSelector portSelector = (PortSelector) o;
        if (this.number != null) {
            if (portSelector.number == null) {
                return false;
            } else if (this.number.intValue() != portSelector.number.intValue()) {
                return false;
            }
        } else {
            if (portSelector.number != null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return getNumber() != null ? getNumber().hashCode() : 0;
    }
}
