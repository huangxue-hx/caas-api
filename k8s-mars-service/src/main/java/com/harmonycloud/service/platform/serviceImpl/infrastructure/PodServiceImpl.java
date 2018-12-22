package com.harmonycloud.service.platform.serviceImpl.infrastructure;

import com.harmonycloud.common.enumm.K8sModuleEnum;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.k8s.bean.*;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.Constant;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.service.EventService;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.platform.bean.KubeModuleStatus;
import com.harmonycloud.service.platform.bean.PodDto;
import com.harmonycloud.service.platform.convert.K8sResultConvert;
import com.harmonycloud.service.platform.service.PodService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static com.harmonycloud.common.Constant.CommonConstant.KUBE_SYSTEM;

@Service
public class PodServiceImpl implements PodService {

	@Autowired
	private com.harmonycloud.k8s.service.PodService podService;
	@Autowired
	private ClusterService clusterService;

	@Autowired
	private EventService eventService;

	@Override
	public List<PodDto> PodList(String nodeName,Cluster cluster) {
		PodList listPods = this.podService.listPods(cluster,nodeName);
		// 处理为页面需要的数据
		List<PodDto> podDtos = new ArrayList<>();
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
					if(!CollectionUtils.isEmpty(pod.getMetadata().getOwnerReferences())){
						podDto.setOwnerReferenceKind(pod.getMetadata().getOwnerReferences().get(0).getKind());
					}
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
					if (StringUtils.isNotBlank(pod.getMetadata().getDeletionTimestamp())) {
						podDto.setStatus(com.harmonycloud.service.platform.constant.Constant.TERMINATED);
					}
					podDto.setNodeName(pod.getSpec().getNodeName());
					podDtos.add(podDto);
				}
			}
		}
		return podDtos;
	}

	@Override
	public List<KubeModuleStatus> getKubeModuleStatus() throws Exception {
		K8SURL url = new K8SURL();
		url.setNamespace("kube-system");
		url.setResource(Resource.POD);
		List<Cluster> clusters = clusterService.listAllCluster(Boolean.TRUE);
		List<KubeModuleStatus> kubeModuleStatuses = new ArrayList<>();
		for(Cluster cluster : clusters) {
			//组件状态，为集群总览的组件状态
			Map<String, Object> componentStatus = clusterService.getClusterComponentStatus(cluster.getId());
			List<String> abnormalComponents = (List)componentStatus.get("abnormalComponent");
			for(String component : abnormalComponents){
				KubeModuleStatus kubeModuleStatus = new KubeModuleStatus();
				kubeModuleStatus.setClusterName(cluster.getName());
				kubeModuleStatus.setClusterId(cluster.getId());
				kubeModuleStatus.setName(component);
				kubeModuleStatus.setNamespace(KUBE_SYSTEM);
				kubeModuleStatus.setStatus("Abnormal");
				kubeModuleStatus.setMessage(K8sModuleEnum.getByCode(component).getName());
				kubeModuleStatuses.add(kubeModuleStatus);
			}
			K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET, null, null, cluster);
			if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
				throw new Exception("Failed to get pod status from apiserver.Response status is " + response.getStatus());
			}
			PodList podList = K8SClient.converToBean(response, PodList.class);
			if (podList != null && !CollectionUtils.isEmpty(podList.getItems())) {
				List<Pod> items = podList.getItems();
				for (Pod pod : items) {
					List<ContainerStatus> containerStatuses = pod.getStatus().getContainerStatuses();
					if(containerStatuses == null){
						continue;
					}
					for(ContainerStatus containerStatus : containerStatuses){
						KubeModuleStatus kubeModuleStatus = new KubeModuleStatus();
						kubeModuleStatus.setClusterName(cluster.getName());
						kubeModuleStatus.setClusterId(cluster.getId());
						kubeModuleStatus.setName(pod.getMetadata().getName());
						kubeModuleStatus.setNamespace(pod.getMetadata().getNamespace());
						kubeModuleStatus.setStartTime(pod.getMetadata().getCreationTimestamp());
						if(containerStatus.getState().getRunning() != null){
							kubeModuleStatus.setStatus("Running");
						}else if(containerStatus.getState().getWaiting() != null){
							kubeModuleStatus.setStatus(containerStatus.getState().getWaiting().getReason());
							kubeModuleStatus.setMessage(containerStatus.getState().getWaiting().getMessage());
						}else if(containerStatus.getState().getTerminated() != null){
							kubeModuleStatus.setStatus(containerStatus.getState().getTerminated().getReason());
							kubeModuleStatus.setMessage(containerStatus.getState().getTerminated().getMessage());
						}
						kubeModuleStatus.setNodeName(pod.getSpec().getNodeName());
						kubeModuleStatus.setContainer(containerStatus.getName());
						kubeModuleStatus.setRestartCount(containerStatus.getRestartCount());
						kubeModuleStatuses.add(kubeModuleStatus);
					}

				}
			}
		}
		return kubeModuleStatuses;
	}

	@Override
	public  Map<String, Object> getPodDetail(String namespace, String name, Cluster cluster) throws Exception {
		K8SClientResponse response = podService.getSpecifyPod(namespace, name, null, null, HTTPMethod.GET, cluster);
		if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
			return ActionReturnUtil.returnErrorWithData(response.getBody());
		}
		Pod pod = JsonUtil.jsonToPojo(response.getBody(), Pod.class);

		// 获取事件
		Map<String, Object> bodys = new HashMap<String, Object>();
		bodys.put("fieldSelector", "involvedObject.uid=" + pod.getMetadata().getUid());
		K8SClientResponse evResponse = eventService.doEventByNamespace(namespace, null, bodys, HTTPMethod.GET, cluster);
		if (!HttpStatusUtil.isSuccessStatus(evResponse.getStatus())) {
			return ActionReturnUtil.returnErrorWithData(evResponse.getBody());
		}
		EventList eventList = JsonUtil.jsonToPojo(evResponse.getBody(), EventList.class);
		List<Event> events = eventList.getItems();
		return K8sResultConvert.convertAppPod(pod, events);
	}

}
