package com.harmonycloud.service.application;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.application.StorageClassDto;

/**
 * 存储服务类
 * @author xc
 * @date 2018/6/14 13:55
 */
public interface StorageClassService {

    ActionReturnUtil createStorageClass(StorageClassDto storageClassDto) throws Exception;

    ActionReturnUtil deleteStorageClass(String name) throws Exception;

    ActionReturnUtil getStorageClass(String name) throws Exception;

    ActionReturnUtil listStorageClass(String clusterId) throws Exception;
}
