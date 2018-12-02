package com.harmonycloud.service.application;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.cluster.DataCenterDto;

public interface DataCenterService {

    public ActionReturnUtil listDataCenter(Boolean withCluster, Boolean isEnableCluster) throws Exception;

    public ActionReturnUtil getDataCenter(String name) throws Exception;

    public ActionReturnUtil deleteDataCenter(String name) throws Exception;

    public ActionReturnUtil addDataCenter(DataCenterDto dataCenterDto) throws Exception;

    public ActionReturnUtil updateDataCenter(String name, String annotations) throws Exception;
}
