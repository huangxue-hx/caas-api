
package com.harmonycloud.service.application;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.cluster.bean.TransferClusterBackup;
import com.harmonycloud.dto.application.DeploymentTransferDto;
import com.harmonycloud.dto.cluster.ClusterTransferDetailDto;
import com.harmonycloud.dto.cluster.ClusterTransferDto;
import com.harmonycloud.dto.cluster.ErrorNamespaceDto;
import com.harmonycloud.k8s.bean.cluster.Cluster;

import java.util.List;

/**
 * 集群迁移服务
 * @author youpeiyuan
 *
 */
public interface ClusterTransferService {

	ActionReturnUtil transferDeployService(DeploymentTransferDto deploymentTransferDto) throws Exception;

	/**
	 * 迁移集群
	 * @param clusterTransferDto
	 * @return
	 */
	ActionReturnUtil transferCluster(List<ClusterTransferDto> clusterTransferDto) throws Exception;


	void transferDeploy(List<ErrorNamespaceDto> namespaces, TransferClusterBackup transferClusterBackup,
						List<ClusterTransferDto> clusterTransferDto,
						Cluster targetCluster, boolean isContinue, Cluster sourceCluster) throws Exception;

	ActionReturnUtil getTransferCluster(ClusterTransferDetailDto clusterTransferDto);

	ActionReturnUtil getDeployDetail(ClusterTransferDetailDto clusterTransferDto);

	ActionReturnUtil getTransferDetail(Integer transferBackupId);

	ActionReturnUtil listTransferHistory(ClusterTransferDetailDto clusterTransferDto);

	ActionReturnUtil getDeployAndStatefulSet(List<ClusterTransferDto> clusterTransferDto) throws Exception;
}
