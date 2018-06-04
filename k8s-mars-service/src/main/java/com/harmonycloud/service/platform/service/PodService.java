package com.harmonycloud.service.platform.service;

import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.service.platform.bean.KubeModuleStatus;
import com.harmonycloud.service.platform.bean.PodDto;

import java.util.List;
import java.util.Map;

public interface PodService {
	public List<PodDto> PodList(String nodeName,Cluster cluster);

	public List<PodDto> getPodListByNamespace(Cluster cluster, String namespace) throws Exception;

	List<KubeModuleStatus> getKubeModuleStatus() throws Exception;

	Map<String, Object> getPodDetail(String namespace, String name, Cluster cluster) throws Exception;
}
