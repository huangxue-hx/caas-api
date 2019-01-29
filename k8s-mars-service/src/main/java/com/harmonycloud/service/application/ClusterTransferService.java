
package com.harmonycloud.service.application;

import java.beans.IntrospectionException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.application.DeploymentTransferDto;
import com.harmonycloud.dto.cluster.ClusterTransferDto;

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
}
