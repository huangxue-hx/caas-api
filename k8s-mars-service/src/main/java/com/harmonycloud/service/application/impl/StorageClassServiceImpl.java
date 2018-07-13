package com.harmonycloud.service.application.impl;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.application.StorageClassDto;
import com.harmonycloud.k8s.util.K8SURL;
import com.harmonycloud.service.application.StorageClassService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author xc
 * @date 2018/6/14 14:36
 */
@Service
public class StorageClassServiceImpl implements StorageClassService {
    private static final Logger LOGGER = LoggerFactory.getLogger(StorageClassServiceImpl.class);

    @Override
    public ActionReturnUtil createStorageClass(StorageClassDto storageClassDto) throws Exception {
        return null;
    }

    @Override
    public ActionReturnUtil deleteStorageClass(String name) throws Exception {
        return null;
    }

    @Override
    public ActionReturnUtil getStorageClass(String name) throws Exception {
        return null;
    }

    @Override
    public ActionReturnUtil listStorageClass(String clusterId) throws Exception {
        K8SURL url = new K8SURL();

        return null;
    }
}
