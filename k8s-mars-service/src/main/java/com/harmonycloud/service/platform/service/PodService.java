package com.harmonycloud.service.platform.service;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.service.platform.bean.PodDto;

import java.util.List;

public interface PodService {
	public List<Object> PodList(String nodeName,Cluster cluster);

	public List<PodDto> getPodListByNamespace(Cluster cluster, String namespace) throws Exception;
}
