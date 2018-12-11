package com.harmonycloud.k8s.bean.istio.trafficmanagement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Create by weg on 18-11-30.
 */
@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DestinationWeight {

    private Destination destination;

    private Integer weight;

    public Destination getDestination() {
        return destination;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        DestinationWeight destinationWeight = (DestinationWeight) o;
        if (this.destination != null) {
            if (!this.destination.equals(destinationWeight.destination)) {
                return false;
            }
        } else {
            if (destinationWeight.destination != null) {
                return false;
            }
        }
        if (this.weight != null) {
            if (destinationWeight.weight == null) {
                return false;
            } else if (this.weight.intValue() != destinationWeight.weight.intValue()) {
                return false;
            }
        } else {
            if (destinationWeight.weight != null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = getDestination() != null ? getDestination().hashCode() : 0;
        result = 31 * result + (getWeight() != null ? getWeight().hashCode() : 0);
        return result;
    }
}
