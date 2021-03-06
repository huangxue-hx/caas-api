package com.harmonycloud.service.application;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.application.StorageClassDto;

import java.util.List;

/**
 * 存储服务类
 * @author xc
 * @date 2018/6/14 13:55
 */
public interface StorageClassService {

    ActionReturnUtil createStorageClass(StorageClassDto storageClassDto) throws Exception;

    //ActionReturnUtil getNfsProvisionerStatus(String name, String clusterId) throws Exception;

    ActionReturnUtil deleteStorageClass(String name, String clusterId) throws Exception;

    ActionReturnUtil getStorageClass(String name, String clusterId) throws Exception;

    List<StorageClassDto> listStorageClass(String clusterId) throws Exception;

    List<StorageClassDto> listUnusedStorageClass(String clusterId) throws Exception;

    List<StorageClassDto> listStorageClass(String clusterId,String namespace, String tenantId, String isUnused) throws Exception;
}
