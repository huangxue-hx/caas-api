package com.harmonycloud.k8s.bean.istio.trafficmanagement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Create by weg on 18-11-30.
 */
@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class HTTPFaultInjection {

    private Delay delay;

    private Abort abort;

    public Delay getDelay() {
        return delay;
    }

    public void setDelay(Delay delay) {
        this.delay = delay;
    }

    public Abort getAbort() {
        return abort;
    }

    public void setAbort(Abort abort) {
        this.abort = abort;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        HTTPFaultInjection fault = (HTTPFaultInjection) o;
        if (this.delay != null) {
            if (!this.delay.equals(fault.delay)) {
                return false;
            }
        } else {
            if (fault.delay != null) {
                return false;
            }
        }
        if (this.abort != null) {
            if (!this.abort.equals(fault.abort)) {
                return false;
            }
        } else {
            if (fault.abort != null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = getDelay() != null ? getDelay().hashCode() : 0;
        result = 31 * result + (getAbort() != null ? getAbort().hashCode() : 0);
        return result;
    }
}
