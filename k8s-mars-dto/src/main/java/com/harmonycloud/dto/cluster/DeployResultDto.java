package com.harmonycloud.dto.cluster;
import java.util.List;

import com.harmonycloud.dao.cluster.bean.TransferBindDeploy;
import com.harmonycloud.dto.application.DeploymentTransferDto;
public class DeployResultDto {

    private List<TransferBindDeploy> deploys;
    private List<DeploymentTransferDto> deploymentTransferDtos;
    public List<TransferBindDeploy> getDeploys() {
        return deploys;
    }
    public void setDeploys(List<TransferBindDeploy> deploys) {
        this.deploys = deploys;
    }
    public List<DeploymentTransferDto> getDeploymentTransferDtos() {
        return deploymentTransferDtos;
    }
    public void setDeploymentTransferDtos(List<DeploymentTransferDto> deploymentTransferDtos) {
        this.deploymentTransferDtos = deploymentTransferDtos;
    }

}
