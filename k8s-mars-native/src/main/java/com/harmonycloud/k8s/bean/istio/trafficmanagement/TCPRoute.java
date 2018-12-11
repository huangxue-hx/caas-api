package com.harmonycloud.k8s.bean.istio.trafficmanagement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Create by weg on 18-11-30.
 */
@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TCPRoute {

    private List<L4MatchAttributes> match;

    private List<DestinationWeight> route;

    public List<L4MatchAttributes> getMatch() {
        return match;
    }

    public void setMatch(List<L4MatchAttributes> match) {
        this.match = match;
    }

    public List<DestinationWeight> getRoute() {
        return route;
    }

    public void setRoute(List<DestinationWeight> route) {
        this.route = route;
    }
}
