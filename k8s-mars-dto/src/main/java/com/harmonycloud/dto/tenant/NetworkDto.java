package com.harmonycloud.dto.tenant;

/**
 * Created by andy on 17-1-20.
 */
public class NetworkDto {

    private String name;

    private String  networkid;

    private SubnetDto subnet;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNetworkid() {
        return networkid;
    }

    public void setNetworkid(String networkid) {
        this.networkid = networkid;
    }

    public SubnetDto getSubnet() {
        return subnet;
    }

    public void setSubnet(SubnetDto subnet) {
        this.subnet = subnet;
    }
}
