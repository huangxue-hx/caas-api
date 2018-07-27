package com.harmonycloud.service.application;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.application.PersistentVolumeClaimDto;

/**
 * @author xc
 * @date 2018/7/4 18:05
 */
public interface PersistentVolumeClaimService {

    ActionReturnUtil createPersistentVolumeClaim(PersistentVolumeClaimDto persistentVolumeClaimDto) throws Exception;

    ActionReturnUtil listPersistentVolumeClaim(String projectId, String tenantId, String clusterId, String namespace) throws Exception;

    ActionReturnUtil deletePersistentVolumeClaim(String namespace, String pvcName, String clusterId) throws Exception;

    ActionReturnUtil recyclePersistentVolumeClaim(String namespace, String pvcName, String clusterId) throws Exception;

    ActionReturnUtil getPersistentVolumeClaim(String namespace, String pvcName, String clusterId) throws Exception;

    ActionReturnUtil updatePersistentVolumeClaim(PersistentVolumeClaimDto persistentVolumeClaimDto) throws Exception;
}
