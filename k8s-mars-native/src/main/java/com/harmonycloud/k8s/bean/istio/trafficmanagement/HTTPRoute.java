package com.harmonycloud.k8s.bean.istio.trafficmanagement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * Create by weg on 18-11-30.
 */
@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class HTTPRoute {

    private List<HTTPMatchRequest> match;

    private List<DestinationWeight> route;

    private String timeout;

    private HTTPRetry retries;

    private HTTPFaultInjection fault;

    public List<HTTPMatchRequest> getMatch() {
        return match;
    }

    public void setMatch(List<HTTPMatchRequest> match) {
        this.match = match;
    }

    public List<DestinationWeight> getRoute() {
        return route;
    }

    public void setRoute(List<DestinationWeight> route) {
        this.route = route;
    }

    public String getTimeout() {
        return timeout;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

    public HTTPRetry getRetries() {
        return retries;
    }

    public void setRetries(HTTPRetry retries) {
        this.retries = retries;
    }

    public HTTPFaultInjection getFault() {
        return fault;
    }

    public void setFault(HTTPFaultInjection fault) {
        this.fault = fault;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        HTTPRoute httpRoute = (HTTPRoute) o;
        if (!CollectionUtils.isEmpty(this.match)) {
            if (CollectionUtils.isEmpty(httpRoute.match)) {
                return false;
            } else if (!checkListMatch(this.match, httpRoute.match)) {
                return false;
            }
        } else {
            if (!CollectionUtils.isEmpty(httpRoute.match)) {
                return false;
            }
        }
        if (!CollectionUtils.isEmpty(this.route)) {
            if (CollectionUtils.isEmpty(httpRoute.route)) {
                return false;
            } else if (!checkListRoute(this.route, httpRoute.route)) {
                return false;
            }
        } else {
            if (!CollectionUtils.isEmpty(httpRoute.route)) {
                return false;
            }
        }
        if (this.timeout != null) {
            if (!this.timeout.equals(httpRoute.timeout)) {
                return false;
            }
        } else {
            if (httpRoute.timeout != null) {
                return false;
            }
        }
        if (this.retries != null) {
            if (!this.retries.equals(httpRoute.retries)) {
                return false;
            }
        } else {
            if (httpRoute.retries != null) {
                return false;
            }
        }
        if (this.fault != null) {
            if (!this.fault.equals(httpRoute.fault)) {
                return false;
            }
        } else {
            if (httpRoute.fault != null) {
                return false;
            }
        }
        return true;
    }

    private boolean checkListMatch(List<HTTPMatchRequest> thisMatch, List<HTTPMatchRequest> objMatch) {
        if (thisMatch.size() != objMatch.size()) {
            return false;
        }
        for (int i = 0; i < thisMatch.size(); i++) {
            if (!thisMatch.get(i).equals(objMatch.get(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean checkListRoute(List<DestinationWeight> thisRoute, List<DestinationWeight> objRoute) {
        if (thisRoute.size() != objRoute.size()) {
            return false;
        }
        for (int i = 0; i < thisRoute.size(); i++) {
            if (!thisRoute.get(i).equals(objRoute.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = getMatch() != null ? getMatch().hashCode() : 0;
        result = 31 * result + (getRoute() != null ? getRoute().hashCode() : 0);
        result = 31 * result + (getTimeout() != null ? getTimeout().hashCode() : 0);
        result = 31 * result + (getRetries() != null ? getRetries().hashCode() : 0);
        result = 31 * result + (getFault() != null ? getFault().hashCode() : 0);
        return result;
    }
}
