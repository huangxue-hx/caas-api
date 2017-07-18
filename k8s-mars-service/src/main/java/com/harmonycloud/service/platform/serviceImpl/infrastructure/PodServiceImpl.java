package com.harmonycloud.service.platform.serviceImpl.infrastructure;

import java.util.ArrayList;
import java.util.List;

import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.k8s.bean.Pod;
import com.harmonycloud.k8s.bean.PodList;
import com.harmonycloud.service.platform.bean.PodDto;
import com.harmonycloud.service.platform.service.PodService;

@Service
public class PodServiceImpl implements PodService {

	@Autowired
	private com.harmonycloud.k8s.service.PodService podService;

	@Override
	public List<Object> PodList(String nodeName,Cluster cluster) {
		PodList listPods = this.podService.listPods(cluster);
		// 处理为页面需要的数据
		List<Object> podDtos = new ArrayList<>();
		if (listPods != null) {
			List<Pod> items = listPods.getItems();
			for (Pod pod : items) {
				if (pod.getSpec() != null && pod.getSpec().getNodeName() != null && pod.getSpec().getNodeName().equals(nodeName)) {
					PodDto podDto = new PodDto();
					podDto.setName(pod.getMetadata().getName());
					podDto.setIp(pod.getStatus().getPodIP());
					podDto.setNamespace(pod.getMetadata().getNamespace());
					podDto.setStartTime(pod.getMetadata().getCreationTimestamp());
					podDto.setStatus(pod.getStatus().getPhase());
					podDtos.add(podDto);
				}
			}
		}
		return podDtos;
	}

	@Override
	public List<PodDto> getPodListByNamespace(Cluster cluster, String namespace) throws Exception {

		K8SURL url = new K8SURL();
		url.setNamespace(namespace);
		url.setResource(Resource.POD);
		K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET, null, null, cluster);
		if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
			throw new Exception("Failed to get pod status from apiserver.Response status is " + response.getStatus());
		}

		PodList podList = K8SClient.converToBean(response, PodList.class);

		List<PodDto> podDtos = new ArrayList<>();
		if (podList != null) {
			List<Pod> items = podList.getItems();
			for (Pod pod : items) {
				if (pod.getSpec() != null && pod.getSpec().getNodeName() != null) {
					PodDto podDto = new PodDto();
					podDto.setName(pod.getMetadata().getName());
					podDto.setIp(pod.getStatus().getPodIP());
					podDto.setNamespace(pod.getMetadata().getNamespace());
					podDto.setStartTime(pod.getMetadata().getCreationTimestamp());
					podDto.setStatus(pod.getStatus().getPhase());
					podDto.setNodeName(pod.getSpec().getNodeName());
					podDtos.add(podDto);
				}
			}
		}
		return podDtos;
	}


}
