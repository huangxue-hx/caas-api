package com.harmonycloud.dto.external;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Subsets {
	private List<EndpointAddress> addresses;

    public List<EndpointAddress> getAddresses() {
            return addresses;
    }

    public void setAddresses(List<EndpointAddress> addresses) {
            this.addresses = addresses;
    }

    public List<EndpointPort> getPorts() {
            return ports;
    }

    public void setPorts(List<EndpointPort> ports) {
            this.ports = ports;
    }

    private List<EndpointPort> ports;

}
