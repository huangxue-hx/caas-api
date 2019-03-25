package com.harmonycloud.dto.cluster;

import java.util.List;
import java.util.Map;

public class TransferContinueDto {

    private List<String> namespaceList;

    private Map<String,List<DeploymentDto>> deployments;

    public List<String> getNamespaceList() {
        return namespaceList;
    }

    public void setNamespaceList(List<String> namespaceList) {
        this.namespaceList = namespaceList;
    }

    public Map<String, List<DeploymentDto>> getDeployments() {
        return deployments;
    }

    public void setDeployments(Map<String, List<DeploymentDto>> deployments) {
        this.deployments = deployments;
    }


}
