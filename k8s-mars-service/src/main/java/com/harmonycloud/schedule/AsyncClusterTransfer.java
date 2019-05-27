/**
 * @Classname Asssss
 * @Description TODO
 * @Date 2019-05-21 19:06
 * @Created by pyx
 */

package com.harmonycloud.schedule;

import com.harmonycloud.dao.cluster.bean.TransferClusterBackup;
import com.harmonycloud.dto.cluster.ClusterTransferDto;
import com.harmonycloud.dto.cluster.ErrorNamespaceDto;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.service.application.ClusterTransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AsyncClusterTransfer {

    @Autowired
    private ClusterTransferService clusterTransferService;

    @Async("taskExecutor")
    public void transferDeploy(List<ErrorNamespaceDto> namespaces, TransferClusterBackup transferClusterBackup,
                               List<ClusterTransferDto> clusterTransferDto, Cluster targetCluster, boolean isContinue,
                               Cluster sourceCluster) throws Exception {
        clusterTransferService.transferDeploy(namespaces, transferClusterBackup, clusterTransferDto, targetCluster, isContinue, sourceCluster);
    }
}