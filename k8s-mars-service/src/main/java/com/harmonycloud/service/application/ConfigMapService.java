package com.harmonycloud.service.application;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.cluster.bean.Cluster;

/**
 * Created by czm on 2017/6/1.
 */
public interface ConfigMapService {

    public ActionReturnUtil getConfigMapByName(String namespace , String name, String method, Cluster cluster) throws Exception;

}
