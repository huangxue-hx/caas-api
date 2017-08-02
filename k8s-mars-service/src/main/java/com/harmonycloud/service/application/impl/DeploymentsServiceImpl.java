package com.harmonycloud.service.application.impl;

import com.harmonycloud.common.util.*;
import com.harmonycloud.dao.cluster.ClusterMapper;
import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.dto.business.CreateConfigMapDto;
import com.harmonycloud.dto.business.CreateContainerDto;
import com.harmonycloud.dto.business.DeploymentDetailDto;
import com.harmonycloud.dto.business.ParsedIngressListDto;
import com.harmonycloud.k8s.bean.*;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.service.*;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import com.harmonycloud.service.application.DeploymentsService;
import com.harmonycloud.service.application.RouterService;
import com.harmonycloud.service.platform.bean.*;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.platform.convert.K8sResultConvert;
import com.harmonycloud.service.platform.dto.PodDto;
import com.harmonycloud.service.platform.dto.ReplicaSetDto;
import com.harmonycloud.service.platform.service.WatchService;
import com.harmonycloud.service.tenant.TenantService;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpSession;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class DeploymentsServiceImpl implements DeploymentsService {
	private static final Logger LOGGER = LoggerFactory.getLogger(DeploymentsServiceImpl.class);
    private static final int MAX_LOG_LINES = 2000;
	@Autowired
	WatchService watchService;

	@Autowired
	DeploymentService dpService;
	
	@Autowired
	TenantService tenantService;

	@Autowired
	HorizontalPodAutoscalerService hpaService;

	@Autowired
	PodService podService;

	@Autowired
	ServicesService sService;

	@Autowired
	EventService eventService;

	@Autowired
	ReplicasetsService rsService;

	@Autowired
	RouterService routerService;

	@Autowired
	HttpSession session;

	@Autowired
	ClusterMapper clusterMapper;

	public ActionReturnUtil listDeployments(String tenantId, String name, String namespace, String labels, String status) throws Exception {
		K8SURL url = new K8SURL();
		url.setResource(Resource.DEPLOYMENT);
		DeploymentList deployments = new DeploymentList();

		//labels
		Map<String, Object> bodys = new HashMap<String, Object>();
		if (!checkParamNUll(labels)) {
			bodys.put("labelSelector", labels);
		}
		Cluster cluster=tenantService.getClusterByTenantid(tenantId);
		//namespace
		if (!StringUtils.isEmpty(namespace)){
			String[] ns = namespace.split(",");
			List<Deployment> items = new ArrayList<>();
			 for (int i = 0; i < ns.length; i++){
			 	if (ns[i] != null && !StringUtils.isEmpty(ns[i])){
					url.setNamespace(ns[i]);
					K8SClientResponse depRes = new K8SClient().doit(url, HTTPMethod.GET, null, bodys,cluster);
					if(!HttpStatusUtil.isSuccessStatus(depRes.getStatus()) && depRes.getStatus() != Constant.HTTP_404){
						UnversionedStatus sta = JsonUtil.jsonToPojo(depRes.getBody(), UnversionedStatus.class);
						return ActionReturnUtil.returnErrorWithMsg(sta.getMessage());
					}
					DeploymentList deployment = JsonUtil.jsonToPojo(depRes.getBody(), DeploymentList.class);
					if(deployment != null){
						items.addAll(deployment.getItems());
					}
				}
			 }
			deployments.setItems(items);
		}
		else {
			return ActionReturnUtil.returnErrorWithMsg("namespase 为空");
		}

		return ActionReturnUtil.returnSuccessWithData(K8sResultConvert.convertAppList(deployments));
	}

	public ActionReturnUtil startDeployments(String name, String namespace, String userName, Cluster cluster) throws Exception {
		// 先watch

		K8SClientResponse response = dpService.doSpecifyDeployment(namespace, name, null, null, HTTPMethod.GET, cluster);
        if (response.getStatus() == Constant.HTTP_404) {
            return ActionReturnUtil.returnSuccess();
        }
		if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
			return ActionReturnUtil.returnErrorWithMsg(response.getBody());
		}
		Deployment dep = JsonUtil.jsonToPojo(response.getBody(), Deployment.class);

		// status code :0 stop 1:start 2:stopping 3:starting
		// 先判断状态
		if (dep != null && !dep.equals("")) {
			Map<String, Object> anno = ((Map<String, Object>) dep.getMetadata().getAnnotations());
			if (anno.containsKey("nephele/status") && anno.get("nephele/status") != null) {
				String status = anno.get("nephele/status").toString();
				if (status.equals(Constant.STARTING)) {
					return ActionReturnUtil
							.returnErrorWithMsg("service " + dep.getMetadata().getName() + " is already started");
				} else {
					Integer.valueOf(anno.get("nephele/status").toString());

					int rep = 1;
					if (anno.get("nephele/replicas") != null){
						rep = Integer.valueOf(anno.get("nephele/replicas").toString());
					}

					anno.put("nephele/status", Constant.STARTING);
					dep.getSpec().setReplicas(rep == 0 ? 1 : rep);
					if (anno.get("nephele/replicas") != null){
						anno.put("nephele/replicas", anno.get("nephele/replicas").toString());
					} else {
						anno.put("nephele/replicas",  "1");
					}
				}
			} else {
				anno.put("nephele/status", Constant.STARTING);
				dep.getSpec().setReplicas(1);
				if (anno.get("nephele/replicas") != null){
					anno.put("nephele/replicas", anno.get("nephele/replicas").toString());
				} else {
					anno.put("nephele/replicas",  "1");
				}

			}

			String lrv = watchService.getLatestVersion(namespace, null, cluster);

			Map<String, Object> bodys = new HashMap<String, Object>();
			bodys = CollectionUtil.transBean2Map(dep);
			Map<String, Object> headers = new HashMap<String, Object>();
			headers.put("Content-type", "application/json");
			K8SClientResponse newRes = dpService.doSpecifyDeployment(namespace, name, headers, bodys, HTTPMethod.PUT, cluster);
			if (!HttpStatusUtil.isSuccessStatus(newRes.getStatus())) {
				return ActionReturnUtil.returnErrorWithMsg(newRes.getBody());
			}

			new Thread() {
				@Override
				public void run() {
					try {
						watchAppEvent(name, namespace, null, lrv, userName, cluster);
					} catch (Exception e) {
						e.printStackTrace();
					}
				};
			}.start();

			return ActionReturnUtil.returnSuccess();
		}
		return ActionReturnUtil.returnError();
	}

	public ActionReturnUtil stopDeployments(String name, String namespace, String userName, Cluster cluster) throws Exception {
		Map<String, Object> bodys = new HashMap<String, Object>();

		// 先删除自动扩容
		bodys.put("labelSelector", "app=" + name);
		K8SClientResponse response = hpaService.doHpautoscalerByNamespace(namespace, null, bodys, HTTPMethod.GET, cluster);
        if (response.getStatus() == Constant.HTTP_404) {
            return ActionReturnUtil.returnSuccess();
        }
		if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
			return ActionReturnUtil.returnErrorWithMsg(response.getBody());
		}
		HorizontalPodAutoscalerList hpaList = JsonUtil.jsonToPojo(response.getBody(),
				HorizontalPodAutoscalerList.class);
		Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("Content-type", "application/json");
		if (hpaList.getItems().size() > 0) {
			HorizontalPodAutoscaler hpa = hpaList.getItems().get(0);
			hpa.getSpec().setMinReplicas(0);
			bodys.clear();
			bodys.put("gracePeriodSeconds", 1);
			K8SClientResponse delRes = hpaService.doSpecifyHpautoscaler(namespace, name, headers, bodys,
					HTTPMethod.DELETE,cluster);
			if (!HttpStatusUtil.isSuccessStatus(delRes.getStatus())) {
				return ActionReturnUtil.returnErrorWithMsg(delRes.getBody());
			}
		}
		// String lrv = watchService.getLatestVersion(namespace);
		// watchAppEvent(name, namespace, null, lrv, userName);

		// 将实例减为0
		// dep状态：status code :0 ：stop 1:start 2:stopping 3:starting
		K8SClientResponse depRes = dpService.doSpecifyDeployment(namespace, name, null, null, HTTPMethod.GET, cluster);
		if (depRes.getStatus() == Constant.HTTP_404) {
            return ActionReturnUtil.returnSuccess();
        }
		if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus())) {
			return ActionReturnUtil.returnErrorWithMsg(depRes.getBody());
		}
		Deployment dep = JsonUtil.jsonToPojo(depRes.getBody(), Deployment.class);
		if (dep != null && !dep.equals("")) {
			Map<String, Object> anno = ((Map<String, Object>) dep.getMetadata().getAnnotations());
			if (anno.containsKey("nephele/status")) {
				String status = anno.get("nephele/status").toString();
				if (status.equals(Constant.STOPPING)) {
					return ActionReturnUtil
							.returnErrorWithMsg("service " + dep.getMetadata().getName() + " is already stopped");
				} else {
					anno.put("nephele/status", Constant.STOPPING);
					dep.getSpec().setReplicas(0);
				}
			} else {
				anno.put("nephele/status", Constant.STOPPING);
				dep.getSpec().setReplicas(0);
			}

			bodys.clear();
			bodys = CollectionUtil.transBean2Map(dep);
			String lrv = watchService.getLatestVersion(namespace, null, cluster);
			K8SClientResponse newRes = dpService.doSpecifyDeployment(namespace, name, headers, bodys, HTTPMethod.PUT, cluster);
			if (!HttpStatusUtil.isSuccessStatus(newRes.getStatus())) {
				return ActionReturnUtil.returnErrorWithMsg(newRes.getBody());
			}
			// String lrver = watchService.getLatestVersion(namespace);
			new Thread() {
				@Override
				public void run() {
					try {
						watchAppEvent(name, namespace, null, lrv, userName, cluster);
					} catch (Exception e) {
						e.printStackTrace();
					}
				};
			}.start();
			return ActionReturnUtil.returnSuccess();
		}
		return ActionReturnUtil.returnError();
	}

	public ActionReturnUtil getPodDetail(String name, String namespace, Cluster cluster) throws Exception {
		K8SClientResponse response = podService.getSpecifyPod(namespace, name, null, null, HTTPMethod.GET,cluster);
		if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
			return ActionReturnUtil.returnErrorWithMsg(response.getBody());
		}
		Pod pod = JsonUtil.jsonToPojo(response.getBody(), Pod.class);

		// 获取事件
		Map<String, Object> bodys = new HashMap<String, Object>();
		bodys.put("fieldSelector", "involvedObject.uid=" + pod.getMetadata().getUid());
		K8SClientResponse evResponse = eventService.doEventByNamespace(namespace, null, bodys, HTTPMethod.GET, cluster);
		if (!HttpStatusUtil.isSuccessStatus(evResponse.getStatus())) {
			return ActionReturnUtil.returnErrorWithMsg(evResponse.getBody());
		}
		EventList eventList = JsonUtil.jsonToPojo(evResponse.getBody(), EventList.class);
		List<Event> events = eventList.getItems();
		return ActionReturnUtil.returnSuccessWithData(K8sResultConvert.convertAppPod(pod, events));
	}

	public ActionReturnUtil podList(String name, String namespace, Cluster cluster) throws Exception {
		
		
        //获取rs
		Map<String, Object> bodys = new HashMap<String, Object>();
		bodys.put("labelSelector", "app="+name);
		K8SClientResponse rsresponse = rsService.doRsByNamespace(namespace, null, bodys, HTTPMethod.GET, cluster);
		if (!HttpStatusUtil.isSuccessStatus(rsresponse.getStatus())) {
			UnversionedStatus status = JsonUtil.jsonToPojo(rsresponse.getBody(), UnversionedStatus.class);
			return ActionReturnUtil.returnErrorWithMsg(status.getMessage());
		}
		ReplicaSetList rsList = K8SClient.converToBean(rsresponse,ReplicaSetList.class);
		
		List<ReplicaSet> rss = rsList.getItems();
		List<PodDetail> list = new LinkedList<PodDetail>();
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		if(rss != null && rss.size() > 0){
			// 对时间进行顺序
			Collections.sort(rss, new Comparator<ReplicaSet>() {

				@Override
				public int compare(ReplicaSet o1, ReplicaSet o2){
					try {
						return Long.valueOf(sdf.parse(o1.getMetadata().getCreationTimestamp()).getTime()).compareTo(Long.valueOf(sdf.parse(o2.getMetadata().getCreationTimestamp()).getTime()));
					} catch (ParseException e) {
						e.printStackTrace();
						return 0;
					}
				}

			});
			int tag = 0;
			for(ReplicaSet rs : rss){
				bodys = new HashMap<String, Object>();
				if(rs.getMetadata().getAnnotations()!= null && !StringUtils.isEmpty(rs.getMetadata().getAnnotations().get("deployment.kubernetes.io/revision").toString())){
					tag = Integer.parseInt(rs.getMetadata().getAnnotations().get("deployment.kubernetes.io/revision").toString());
				}else{
					tag++;
				}
				Map<String, Object> labels = new HashMap<String, Object>();
				labels = rs.getMetadata().getLabels();
				if(!StringUtils.isEmpty(labels.get("pod-template-hash").toString())){
					bodys.put("labelSelector", "pod-template-hash="+labels.get("pod-template-hash"));
					K8SClientResponse podRes = podService.getPodByNamespace(namespace, null, bodys, HTTPMethod.GET, cluster);
					if (!HttpStatusUtil.isSuccessStatus(podRes.getStatus())) {
						return ActionReturnUtil.returnErrorWithMsg(podRes.getBody());
					}
					PodList podList = JsonUtil.jsonToPojo(podRes.getBody(), PodList.class);
					list.addAll(K8sResultConvert.podListConvert(podList, "v"+tag));
				}
			}
			;
		}
		return ActionReturnUtil.returnSuccessWithData(list);
	}

	public ActionReturnUtil getNamespaceUserNum(String namespace, Cluster cluster) throws Exception {
		K8SClientResponse response = dpService.doDeploymentsByNamespace(namespace, null, null, HTTPMethod.GET, cluster);
		if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
			return ActionReturnUtil.returnErrorWithMsg(response.getBody());
		}
		DeploymentList dl = JsonUtil.jsonToPojo(response.getBody(), DeploymentList.class);
		int totalNum = 0;
		List<Deployment> depList = dl.getItems();
		totalNum = depList.size();
		List<Map<String, Object>> userArr = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> uniqueUserArr = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < depList.size(); i++) {
			Map<String, Object> labels = depList.get(i).getMetadata().getLabels();
			if (labels.get("nephele/user") != null && !labels.get("nephele/user").equals("")) {
				Map<String, Object> user = new HashMap<String, Object>();
				user.put("name", labels.get("nephele/user").toString());
				user.put("num", 1);
				userArr.add(user);
				uniqueUserArr.add(user);
			}
		}

		// 对uniqueUserArr去重
		List<Map<String, Object>> newUqArr = CollectionUtil.rmDuplicate(uniqueUserArr);
		List<Map<String, Object>> resUserArr = new ArrayList<>();
		for (Map<String, Object> map : newUqArr) {
			Map<String, Object> tMap = new HashMap<String, Object>();
			tMap.put("name", "");
			tMap.put("num", 0);
			for (Map<String, Object> uMap : userArr) {
				if (uMap.get("name").toString().equals(map.get("name").toString())) {
					tMap.put("name", uMap.get("name").toString());
					tMap.put("num", Integer.valueOf(tMap.get("num").toString()) + 1);
				}
			}
			resUserArr.add(tMap);
		}
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("namespace", namespace);
		result.put("totalNum", totalNum);
		result.put("userArr", resUserArr);
		return ActionReturnUtil.returnSuccessWithData(result);
	}

	@Override
	public ActionReturnUtil getDeploymentDetail(String namespace, String name, Cluster cluster) throws Exception {
		// 获取特定的deployment
		K8SClientResponse depRes = dpService.doSpecifyDeployment(namespace, name, null, null, HTTPMethod.GET, cluster);
		if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus())) {
			return ActionReturnUtil.returnErrorWithMsg(depRes.getBody());
		}
		Deployment dep = JsonUtil.jsonToPojo(depRes.getBody(), Deployment.class);

		// 获取service
		Map<String, Object> bodys = new HashMap<String, Object>();
		bodys.put("labelSelector", "app=" + name);
		K8SClientResponse sRes = sService.doServiceByNamespace(namespace, null, bodys, HTTPMethod.GET, cluster);
		if (!HttpStatusUtil.isSuccessStatus(sRes.getStatus())) {
			return ActionReturnUtil.returnErrorWithMsg(sRes.getBody());
		}
		ServiceList serviceList = JsonUtil.jsonToPojo(sRes.getBody(), ServiceList.class);

		// 获取deployment的events
		bodys.clear();
		bodys.put("fieldSelector", "involvedObject.uid=" + dep.getMetadata().getUid());
		K8SClientResponse deResponse = eventService.doEventByNamespace(namespace, null, bodys, HTTPMethod.GET, cluster);
		if (!HttpStatusUtil.isSuccessStatus(deResponse.getStatus())) {
			return ActionReturnUtil.returnErrorWithMsg(deResponse.getBody());
		}
		EventList eventList = JsonUtil.jsonToPojo(deResponse.getBody(), EventList.class);

		// 获取hpaEvents
		bodys.clear();
		bodys.put("fieldSelector", "involvedObject.name=" + name + "-hpa");
		K8SClientResponse hpaeRes = eventService.doEventByNamespace(namespace, null, bodys, HTTPMethod.GET, cluster);
		if (!HttpStatusUtil.isSuccessStatus(hpaeRes.getStatus())) {
			return ActionReturnUtil.returnErrorWithMsg(hpaeRes.getBody());
		}
		EventList hapEve = JsonUtil.jsonToPojo(hpaeRes.getBody(), EventList.class);

		// 获取hascaler
		bodys.clear();
		bodys.put("labelSelector", "app=" + name);
		K8SClientResponse hpaResponse = hpaService.doHpautoscalerByNamespace(namespace, null, bodys, HTTPMethod.GET, cluster);
		if (!HttpStatusUtil.isSuccessStatus(hpaResponse.getStatus())) {
			return ActionReturnUtil.returnErrorWithMsg(hpaResponse.getBody());
		}
		HorizontalPodAutoscalerList hpaList = JsonUtil.jsonToPojo(hpaResponse.getBody(),
				HorizontalPodAutoscalerList.class);

		// 获取pod
		bodys.clear();
		bodys.put("labelSelector", K8sResultConvert.convertExpression(dep, name));
		K8SClientResponse podRes = podService.getPodByNamespace(namespace, null, bodys, HTTPMethod.GET, cluster);
		if (!HttpStatusUtil.isSuccessStatus(podRes.getStatus())) {
			return ActionReturnUtil.returnErrorWithMsg(podRes.getBody());
		}
		PodList podList = JsonUtil.jsonToPojo(podRes.getBody(), PodList.class);
		AppDetail res = K8sResultConvert.convertAppDetail(dep, serviceList, eventList, hapEve, hpaList, podList);
		return ActionReturnUtil.returnSuccessWithData(res);
	}

	@Override
	public ActionReturnUtil getDeploymentEvents(String namespace, String name, Cluster cluster) throws Exception {
		List<EventDetail> allEvents = new ArrayList<EventDetail>();

		// 可利用异步操作，线程
		K8SClientResponse depRes = dpService.doSpecifyDeployment(namespace, name, null, null, HTTPMethod.GET, cluster);

		if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus())) {
			return ActionReturnUtil.returnErrorWithMsg(depRes.getBody());
		}
		Deployment dep = JsonUtil.jsonToPojo(depRes.getBody(), Deployment.class);

		Map<String, Object> bodys = new HashMap<String, Object>();
		String selExp = K8sResultConvert.convertExpression(dep, name);
		bodys.put("labelSelector", selExp);
		K8SClientResponse podRes = podService.getPodByNamespace(namespace, null, bodys, HTTPMethod.GET, cluster);
		if (!HttpStatusUtil.isSuccessStatus(podRes.getStatus())) {
			return ActionReturnUtil.returnErrorWithMsg(podRes.getBody());
		}
		PodList podList = JsonUtil.jsonToPojo(podRes.getBody(), PodList.class);

		K8SClientResponse rsRes = rsService.doRsByNamespace(namespace, null, bodys, null, HTTPMethod.GET, cluster);
		if (!HttpStatusUtil.isSuccessStatus(rsRes.getStatus())) {
			return ActionReturnUtil.returnErrorWithMsg(rsRes.getBody());
		}
		ReplicaSetList rSetList = JsonUtil.jsonToPojo(rsRes.getBody(), ReplicaSetList.class);

		// 循环podlist获取每个pod的事件
		bodys.clear();
		for (Pod pod : podList.getItems()) {
			bodys.put("fieldSelector", "involvedObject.uid=" + pod.getMetadata().getUid());
			K8SClientResponse podevRes = eventService.doEventByNamespace(namespace, null, bodys, HTTPMethod.GET, cluster);
			if (!HttpStatusUtil.isSuccessStatus(podevRes.getStatus())) {
				return ActionReturnUtil.returnErrorWithMsg(podevRes.getBody());
			}
			EventList podeventList = JsonUtil.jsonToPojo(podevRes.getBody(), EventList.class);
			if (podeventList.getItems() != null && podeventList.getItems().size() > 0) {
				allEvents.addAll(K8sResultConvert.convertPodEvent(podeventList.getItems()));
			}
		}

		// 获取dep事件
		bodys.clear();
		bodys.put("fieldSelector", "involvedObject.uid=" + dep.getMetadata().getUid());
		K8SClientResponse evRes = eventService.doEventByNamespace(namespace, null, bodys, HTTPMethod.GET, cluster);
		if (!HttpStatusUtil.isSuccessStatus(evRes.getStatus())) {
			return ActionReturnUtil.returnErrorWithMsg(evRes.getBody());
		}
		EventList depeventList = JsonUtil.jsonToPojo(evRes.getBody(), EventList.class);
		if (depeventList.getItems() != null && depeventList.getItems().size() > 0) {
			allEvents.addAll(K8sResultConvert.convertPodEvent(depeventList.getItems()));
		}

		// rs事件
		if (rSetList.getItems() != null && rSetList.getItems().size() > 0) {
			bodys.clear();
			bodys.put("fieldSelector", "involvedObject.uid=" + rSetList.getItems().get(0));
			K8SClientResponse hpaevRes = eventService.doEventByNamespace(namespace, null, bodys, HTTPMethod.GET, cluster);
			if (!HttpStatusUtil.isSuccessStatus(hpaevRes.getStatus())) {
				return ActionReturnUtil.returnErrorWithMsg(hpaevRes.getBody());
			}
			EventList hpaeventList = JsonUtil.jsonToPojo(evRes.getBody(), EventList.class);
			if (hpaeventList.getItems() != null && hpaeventList.getItems().size() > 0) {
				allEvents.addAll(K8sResultConvert.convertPodEvent(hpaeventList.getItems()));
			}
		}

		// hpaEvents
		bodys.clear();
		bodys.put("fieldSelector", "involvedObject.name=" + name + "-hpa");
		K8SClientResponse hpaevRes = eventService.doEventByNamespace(namespace, null, bodys, HTTPMethod.GET, cluster);
		if (!HttpStatusUtil.isSuccessStatus(hpaevRes.getStatus())) {
			return ActionReturnUtil.returnErrorWithMsg(hpaevRes.getBody());
		}
		EventList hpaeventList = JsonUtil.jsonToPojo(evRes.getBody(), EventList.class);
		if (hpaeventList.getItems() != null && hpaeventList.getItems().size() > 0) {
			allEvents.addAll(K8sResultConvert.convertPodEvent(hpaeventList.getItems()));
		}

		// 对event进行倒序排列
		return ActionReturnUtil.returnSuccessWithData(K8sResultConvert.sortByDesc(allEvents));
	}

    @Override
    public ActionReturnUtil scaleDeployment(String namespace, String name, Integer scale, String userName, Cluster cluster)
            throws Exception {

        String token = String.valueOf(K8SClient.tokenMap.get(userName));
        Map<String, Object> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + token);

        K8SClientResponse depRes = dpService.doSpecifyDeployment(namespace, name, headers, null, HTTPMethod.GET, cluster);
	
	if (depRes.getStatus() == Constant.HTTP_404) {
			return ActionReturnUtil.returnSuccessWithMsg(Constant.HTTP_404.toString());
		}

        if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus())) {
            return ActionReturnUtil.returnErrorWithMsg(depRes.getBody());
        }
        Deployment dep = JsonUtil.jsonToPojo(depRes.getBody(), Deployment.class);
        Map<String, Object> bodys = new HashMap<String, Object>();

        Integer replicas = dep.getSpec().getReplicas();
        if (replicas == scale) {
            return ActionReturnUtil.returnSuccess();
        } else if (scale == 0) {
            dep.getMetadata().getAnnotations().put("nephele/status", Constant.STOPPING);
            dep.getMetadata().getAnnotations().put("nephele/replicas", replicas.toString());
            dep.getSpec().setReplicas(scale);
        } else if (replicas == 0) {
            dep.getMetadata().getAnnotations().put("nephele/status", Constant.STARTING);
            dep.getMetadata().getAnnotations().put("nephele/replicas", scale.toString());
            dep.getSpec().setReplicas(scale);
        } else {
            dep.getMetadata().getAnnotations().put("nephele/replicas", scale.toString());
            dep.getSpec().setReplicas(scale);
        }
        bodys = CollectionUtil.transBean2Map(dep);
        String lrv = watchService.getLatestVersion(namespace, headers, cluster);
        headers.put("Content-Type", "application/json");
        K8SClientResponse newRes = dpService.doSpecifyDeployment(namespace, name, headers, bodys, HTTPMethod.PUT, cluster);
        if (!HttpStatusUtil.isSuccessStatus(newRes.getStatus()) && newRes.getStatus() != Constant.HTTP_404) {
            return ActionReturnUtil.returnErrorWithMsg(newRes.getBody());
        }
        new Thread() {
            @Override
            public void start() {
                try {
                    watchAppEvent(name, namespace, null, lrv, userName, cluster);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

        return ActionReturnUtil.returnSuccess();
    }

	@Override
	public ActionReturnUtil deploymentContainer(String namespace, String name, Cluster cluster) throws Exception {
		K8SClientResponse depRes = dpService.doSpecifyDeployment(namespace, name, null, null, HTTPMethod.GET ,cluster);
		if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus())) {
			return ActionReturnUtil.returnErrorWithMsg(depRes.getBody());
		}
		Deployment dep = JsonUtil.jsonToPojo(depRes.getBody(), Deployment.class);
		return ActionReturnUtil.returnSuccessWithData(K8sResultConvert.convertContainer(dep));
	}

	@Override
	public ActionReturnUtil namespaceContainer(String namespace, Cluster cluster) throws Exception {
		return namespaceContainer(namespace, cluster, null);
	}

	@Override
	public ActionReturnUtil namespaceContainer(String namespace, Cluster cluster, Map<String, Object> headers) throws Exception {
		K8SClientResponse depRes = dpService.doSpecifyDeployment(namespace, null, headers, null, HTTPMethod.GET, cluster);
		if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus())) {
			return ActionReturnUtil.returnErrorWithMsg(depRes.getBody());
		}
		DeploymentList depList = JsonUtil.jsonToPojo(depRes.getBody(), DeploymentList.class);
		if(depList == null || CollectionUtils.isEmpty(depList.getItems())) {
			return ActionReturnUtil.returnSuccessWithData(Collections.emptyList());
		}
		List<Deployment> deployments = depList.getItems();
		List<ContainerOfPodDetail> containers = new ArrayList<>();
		for(int i=0; i<deployments.size();i++){
			containers.addAll(K8sResultConvert.convertContainer(deployments.get(i)));
		}
		return ActionReturnUtil.returnSuccessWithData(containers);
	}

	public ActionReturnUtil getPodAppLog(String namespace, String container, String pod, Integer sinceSeconds, String clusterId) throws Exception {
		Map<String, Object> bodys = new HashMap<String, Object>();
		if (StringUtils.isNotBlank(container)) {
			bodys.put("container", container);
		}
		if(sinceSeconds != null && sinceSeconds>0) {
			bodys.put("sinceSeconds", sinceSeconds);
		}

		Cluster cluster = null;
		if(clusterId != null && !clusterId.equals("")) {
			cluster = this.clusterMapper.findClusterById(clusterId);
		}

		//设置查询最大的日志量
		bodys.put("tailLines", MAX_LOG_LINES);
		K8SClientResponse response = podService.getPodLogByNamespace(namespace,
				pod, "log", null, bodys, HTTPMethod.GET,cluster);
		if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
			LOGGER.error("getPodAppLog failed. message:{}", response.getBody().toString());
			return ActionReturnUtil.returnErrorWithMsg(response.getBody());
		}
		return ActionReturnUtil.returnSuccessWithData(response.getBody());
	}


	private void watchAppEvent(String name, String namespace, String kind, String rv, String userName, Cluster cluster)
			throws Exception {
		String token = String.valueOf(K8SClient.tokenMap.get(userName));
		Map<String, Object> headers = new HashMap<>();
		headers.put("Authorization", "Bearer " + token);
		Map<String, String> field = new HashMap<String, String>();
		field.put("involvedObject.name", name);
		field.put("involvedObject.namespace", namespace);
		watchService.watch(field, kind, rv, userName, cluster);

		// 获取pod
		Map<String, Object> bodys = new HashMap<String, Object>();
		bodys.put("labelSelector", "app=" + name);
		bodys.put("resourceVersion", rv);
		bodys.put("watch", "true");
		bodys.put("timeoutSeconds", 3);
		K8SClientResponse response = podService.getPodByNamespace(namespace, headers, bodys, HTTPMethod.GET,cluster);
		if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
			return;
		}
		PodDto podDto = StringUtils.isEmpty(response.getBody()) ? null
				: JsonUtil.jsonToPojo(response.getBody(), PodDto.class);
		if (null != podDto && null != podDto.getObject()) {
			Pod pod = podDto.getObject();
			field.put("involvedObject.name", pod.getMetadata().getName());
			field.put("involvedObject.namespace", pod.getMetadata().getNamespace());
		}
		watchService.watch(field, kind, rv, userName, cluster);

		// 获取rs
		K8SURL rsUrl = new K8SURL();
		rsUrl.setNamespace(namespace).setResource(Resource.REPLICASET);
		bodys.clear();
		bodys.put("labelSelector", "app=" + name);
		bodys.put("resourceVersion", rv);
		bodys.put("watch", "true");
		bodys.put("timeoutSeconds", 3);

		K8SClientResponse rs = new K8SClient().doit(rsUrl, HTTPMethod.GET, headers, bodys,cluster);
		if (!HttpStatusUtil.isSuccessStatus(rs.getStatus())) {
			return;
		}
		ReplicaSetDto rsDto = JsonUtil.jsonToPojo(rs.getBody(), ReplicaSetDto.class);
		if (null != rsDto && null != rsDto.getObject()) {
			ReplicaSet replicaSet = rsDto.getObject();
			field.put("involvedObject.name", replicaSet.getMetadata().getName());
			field.put("involvedObject.namespace", replicaSet.getMetadata().getNamespace());
		}
		watchService.watch(field, kind, rv, userName, cluster);
	}

	private boolean checkParamNUll(String p) {
		if (StringUtils.isEmpty(p) || StringUtils.isBlank(p) || p == null) {
			return true;
		}
		return false;
	}

	@Override
	public ActionReturnUtil createDeployment(DeploymentDetailDto detail, String userName, String business, Cluster cluster) throws Exception {

		List<CreateContainerDto> containers = detail.getContainers();
		List<ConfigMap> cms = new ArrayList<ConfigMap>();
		if (!containers.isEmpty()) {
			for (CreateContainerDto c : containers) {
				List<CreateConfigMapDto> configMaps = c.getConfigmap();
				if (configMaps != null&&configMaps.size()>0) {
					K8SURL url = new K8SURL();
					url.setNamespace(detail.getNamespace()).setResource(Resource.CONFIGMAP);
					Map<String, Object> bodys = new HashMap<String, Object>();
					Map<String, Object> meta = new HashMap<String, Object>();
					meta.put("namespace", detail.getNamespace());
					meta.put("name", detail.getName() + c.getName());
					Map<String, Object> label = new HashMap<String, Object>();
					label.put("app", detail.getName());
					if (!StringUtils.isEmpty(business)){
						label.put("business",business);
					}
					meta.put("labels", label);
					bodys.put("metadata", meta);
					Map<String, Object> data = new HashMap<String, Object>();
					for (CreateConfigMapDto configMap : configMaps) {
						if (configMap != null && !StringUtils.isEmpty(configMap.getPath())) {
							if (StringUtils.isEmpty(configMap.getFile())) {
								data.put("config.json", configMap.getValue());
							} else {
								data.put(configMap.getFile()+"v"+configMap.getTag(), configMap.getValue());
							}
						}
					}
					bodys.put("data", data);  
					Map<String, Object> headers = new HashMap<String, Object>();
					headers.put("Content-type", "application/json");
					K8SClientResponse response = new K8SClient().doit(url, HTTPMethod.POST, headers, bodys, cluster);
					if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
						UnversionedStatus status = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
						return ActionReturnUtil.returnErrorWithMsg(status.getMessage());
					}
					ConfigMap cm = JsonUtil.jsonToPojo(response.getBody(), ConfigMap.class);
					cms.add(cm);
				}
			}
		}

		if (cms.size() == 0) {
			String lrv = watchService.getLatestVersion(detail.getNamespace(), null, cluster);
			Deployment dep = K8sResultConvert.convertAppCreate(detail, userName);
			K8SURL k8surl = new K8SURL();
			k8surl.setNamespace(detail.getNamespace()).setResource(Resource.DEPLOYMENT);
			Map<String, Object> headers = new HashMap<String, Object>();
			headers.put("Content-type", "application/json");
			Map<String, Object> bodys = new HashMap<String, Object>();
			bodys = CollectionUtil.transBean2Map(dep);
			K8SClientResponse response = new K8SClient().doit(k8surl, HTTPMethod.POST, headers, bodys, cluster);
			if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
				return ActionReturnUtil.returnErrorWithMsg(response.getBody());
			}
			Deployment resD = JsonUtil.jsonToPojo(response.getBody(), Deployment.class);
			com.harmonycloud.k8s.bean.Service service = K8sResultConvert.convertAppCreateOfService(detail);
			k8surl.setNamespace(detail.getNamespace()).setResource(Resource.SERVICE);
			bodys.clear();
			bodys = CollectionUtil.transBean2Map(service);
			K8SClientResponse sResponse = new K8SClient().doit(k8surl, HTTPMethod.POST, headers, bodys, cluster);
			if (!HttpStatusUtil.isSuccessStatus(sResponse.getStatus())) {
				UnversionedStatus status = JsonUtil.jsonToPojo(sResponse.getBody(), UnversionedStatus.class);
				return ActionReturnUtil.returnErrorWithMsg(status.getMessage());
			}
			com.harmonycloud.k8s.bean.Service resS = JsonUtil.jsonToPojo(sResponse.getBody(),
					com.harmonycloud.k8s.bean.Service.class);
			Map<String, Object> resMap = new HashMap<String, Object>();
			resMap.put("deployment", resD);
			resMap.put("service", resS);
			// 开启线程执行
			// watchAppEvent(detail.getName(), detail.getNamespace(), null, lrv,
			// userName);
			new Thread() {
				@Override
				public void run() {
					try {
						watchAppEvent(detail.getName(), detail.getNamespace(), null, lrv, userName, cluster);
					} catch (Exception e) {
						e.printStackTrace();
					}
				};
			}.start();
			return ActionReturnUtil.returnSuccessWithData(resMap);
		} else {
			String lrv = watchService.getLatestVersion(detail.getNamespace(), null, cluster);
			Deployment dep = K8sResultConvert.convertAppCreate(detail, userName);
			K8SURL k8surl = new K8SURL();
			k8surl.setNamespace(detail.getNamespace()).setResource(Resource.DEPLOYMENT);
			Map<String, Object> headers = new HashMap<String, Object>();
			headers.put("Content-type", "application/json");
			Map<String, Object> bodys = new HashMap<String, Object>();
			bodys = CollectionUtil.transBean2Map(dep);
			K8SClientResponse response = new K8SClient().doit(k8surl, HTTPMethod.POST, headers, bodys,cluster);
			if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
				UnversionedStatus status = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
				return ActionReturnUtil.returnErrorWithMsg(status.getMessage());
			}
			Deployment resD = JsonUtil.jsonToPojo(response.getBody(), Deployment.class);
			com.harmonycloud.k8s.bean.Service service = K8sResultConvert.convertAppCreateOfService(detail);
			k8surl.setNamespace(detail.getNamespace()).setResource(Resource.SERVICE);
			bodys.clear();
			bodys = CollectionUtil.transBean2Map(service);
			K8SClientResponse sResponse = new K8SClient().doit(k8surl, HTTPMethod.POST, headers, bodys,cluster);
			if (!HttpStatusUtil.isSuccessStatus(sResponse.getStatus())) {
				UnversionedStatus status = JsonUtil.jsonToPojo(sResponse.getBody(), UnversionedStatus.class);
				return ActionReturnUtil.returnErrorWithMsg(status.getMessage());
			}
			com.harmonycloud.k8s.bean.Service resS = JsonUtil.jsonToPojo(sResponse.getBody(),
					com.harmonycloud.k8s.bean.Service.class);
			Map<String, Object> resMap = new HashMap<String, Object>();
			resMap.put("deployment", resD);
			resMap.put("service", resS);
			// 开启线程执行
			new Thread() {
				@Override
				public void run() {
					try {
						watchAppEvent(detail.getName(), detail.getNamespace(), null, lrv, userName, cluster);
					} catch (Exception e) {
						e.printStackTrace();
					}
				};
			}.start();

			return ActionReturnUtil.returnSuccessWithData(resMap);
		}
	}
	
	@Override
	public ActionReturnUtil deleteDeployment(String name, String namespace, String userName, Cluster cluster) throws Exception {

		// 将实例数变成0
		ActionReturnUtil sReturn = scaleDeployment(namespace, name, 0,userName, cluster);
		if (!sReturn.isSuccess() && (Constant.HTTP_404.toString()).equals(sReturn.get("msg").toString())) {
			return ActionReturnUtil.returnSuccess();
		}

		// 获取deployment
		K8SClientResponse depRes = dpService.doSpecifyDeployment(namespace, name, null, null, HTTPMethod.GET, cluster);
		if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus()) && depRes.getStatus() != Constant.HTTP_404) {
			UnversionedStatus status = JsonUtil.jsonToPojo(depRes.getBody(), UnversionedStatus.class);
			return ActionReturnUtil.returnErrorWithMsg(status.getMessage());
		}
/*		if (depRes.getStatus() == Constant.HTTP_404){
			return ActionReturnUtil.returnSuccessWithData(res);
		}*/
		Deployment dep=new Deployment();
		if(depRes.getStatus() != Constant.HTTP_404){
			dep = JsonUtil.jsonToPojo(depRes.getBody(), Deployment.class);
		}
		

		// 删除configmap
		K8SURL cUrl = new K8SURL();
		cUrl.setNamespace(namespace).setResource(Resource.CONFIGMAP);
		Map<String, Object> queryP = new HashMap<>();
		queryP.put("labelSelector", "app=" + name);
		cUrl.setQueryParams(queryP);
		K8SClientResponse conRes = new K8SClient().doit(cUrl, HTTPMethod.DELETE, null, null,null);
		if (!HttpStatusUtil.isSuccessStatus(conRes.getStatus()) && conRes.getStatus() != Constant.HTTP_404) {
			UnversionedStatus status = JsonUtil.jsonToPojo(conRes.getBody(), UnversionedStatus.class);
			return ActionReturnUtil.returnErrorWithMsg(status.getMessage());
		}

		// 删除hpa
		K8SURL hUrl = new K8SURL();
		hUrl.setName(name + "-hpa").setNamespace(namespace).setResource(Resource.HORIZONTALPODAUTOSCALER);
		K8SClientResponse hpaRes = new K8SClient().doit(hUrl, HTTPMethod.DELETE, null, null,null);
		if (!HttpStatusUtil.isSuccessStatus(hpaRes.getStatus()) && hpaRes.getStatus() != Constant.HTTP_404) {
			UnversionedStatus status = JsonUtil.jsonToPojo(hpaRes.getBody(), UnversionedStatus.class);
			return ActionReturnUtil.returnErrorWithMsg(status.getMessage());
		}

		// 获取service
		K8SClientResponse svcRes = sService.doServiceByNamespace(namespace, null, queryP, HTTPMethod.GET);
		if (!HttpStatusUtil.isSuccessStatus(svcRes.getStatus())) {
			UnversionedStatus status = JsonUtil.jsonToPojo(svcRes.getBody(), UnversionedStatus.class);
			return ActionReturnUtil.returnErrorWithMsg(status.getMessage());
		}
		ServiceList svcs = JsonUtil.jsonToPojo(svcRes.getBody(), ServiceList.class);
		
		// 删除rs
		if(dep != null && dep.getSpec() != null){
			/*String exp = K8sResultConvert.convertExpression(dep, name);*/
			
			// 删除deployment
			/*synchronized (obj) {
			    obj.wait();
	        }*/
			
			K8SClientResponse delRes = dpService.doSpecifyDeployment(namespace, name, null, null, HTTPMethod.DELETE, cluster);
			if (!HttpStatusUtil.isSuccessStatus(delRes.getStatus()) && delRes.getStatus() != Constant.HTTP_404) {
				UnversionedStatus sta = JsonUtil.jsonToPojo(delRes.getBody(), UnversionedStatus.class);
				return ActionReturnUtil.returnErrorWithMsg(sta.getMessage());
			}
			//删除rs
			Map<String, Object> labels = new HashMap<>();
//			labels.put("labelSelector", exp);
			labels.put("labelSelector", "app=" + name);
			K8SClientResponse rsRes = rsService.doRsByNamespace(namespace, null, labels, HTTPMethod.DELETE, cluster);
			if (!HttpStatusUtil.isSuccessStatus(rsRes.getStatus()) && rsRes.getStatus() != Constant.HTTP_404) {
				UnversionedStatus sta = JsonUtil.jsonToPojo(rsRes.getBody(), UnversionedStatus.class);
				return ActionReturnUtil.returnErrorWithMsg(sta.getMessage());
			}
			//删除pod
			K8SClientResponse podRes = podService.getPodByNamespace(namespace, null, labels, HTTPMethod.DELETE, cluster);
			if (!HttpStatusUtil.isSuccessStatus(podRes.getStatus()) && podRes.getStatus() != Constant.HTTP_404) {
				UnversionedStatus sta = JsonUtil.jsonToPojo(podRes.getBody(), UnversionedStatus.class);
				return ActionReturnUtil.returnErrorWithMsg(sta.getMessage());
			}

			// 删除ingress
			cUrl.setResource(Resource.INGRESS);
			K8SClientResponse ingRes = new K8SClient().doit(cUrl, HTTPMethod.DELETE, null, null,null);
			if (!HttpStatusUtil.isSuccessStatus(ingRes.getStatus()) && ingRes.getStatus() != Constant.HTTP_404) {
				UnversionedStatus sta = JsonUtil.jsonToPojo(ingRes.getBody(), UnversionedStatus.class);
				return ActionReturnUtil.returnErrorWithMsg(sta.getMessage());
			}

			//delete database port
			ParsedIngressListDto ingress = new ParsedIngressListDto();
			ingress.setNamespace(namespace);
			ingress.setLabels(dep.getSpec().getTemplate().getMetadata().getLabels());
			@SuppressWarnings("unchecked")
			List<RouterSvc> routerSvcs = (List<RouterSvc>)routerService.listSvcByName(ingress).get("data");

			if (routerSvcs != null && routerSvcs.size() > 0){
				String tenantID = session.getAttribute("tenantId").toString();
				for (RouterSvc onerouterSvcs:routerSvcs){
					for (int i=0;onerouterSvcs.getRules().size() > i ;i++){
						routerService.deleteTcpSvc(namespace,onerouterSvcs.getName(),onerouterSvcs.getRules().get(i).getPort().toString(),tenantID);
					}

				}
			}
		}

		// 循环删除service
		List<com.harmonycloud.k8s.bean.Service> svc = svcs.getItems();
		K8SURL svcUrl = new K8SURL();
		svcUrl.setNamespace(namespace).setResource(Resource.SERVICE);
		for (int i = 0; i < svc.size(); i++) {
			String lrv = watchService.getLatestVersion(namespace, null, cluster);
			svcUrl.setName(svc.get(i).getMetadata().getName());
			K8SClientResponse serviceRes = new K8SClient().doit(svcUrl, HTTPMethod.DELETE, null, null, cluster);
			if (!HttpStatusUtil.isSuccessStatus(serviceRes.getStatus()) && serviceRes.getStatus() != Constant.HTTP_404) {
				UnversionedStatus sta = JsonUtil.jsonToPojo(serviceRes.getBody(), UnversionedStatus.class);
				return ActionReturnUtil.returnErrorWithMsg(sta.getMessage());
			}
			new Thread() {
				public void run() {
					try {
						watchAppEvent(name, namespace, null, lrv, userName, cluster);
					} catch (Exception e) {
						e.printStackTrace();
					}
				};
			}.start();
		}
		return ActionReturnUtil.returnSuccess();
	}

	@Override
	public ActionReturnUtil replaceDeployment(UpdateDeployment detail, String userName, Cluster cluster) throws Exception {

		String lrv = watchService.getLatestVersion(detail.getNamespace(), null, cluster);

		new Thread() {
			@Override
			public void run() {
				try {
					watchAppEvent(detail.getName(), detail.getNamespace(), null, lrv, userName, cluster);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();

		// 获取deployment
		K8SClientResponse depRes = dpService.doSpecifyDeployment(detail.getNamespace(), detail.getName(), null, null,
				HTTPMethod.GET, cluster);
		if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus())) {
			return ActionReturnUtil.returnErrorWithMsg(depRes.getBody());
		}
		Deployment dep = JsonUtil.jsonToPojo(depRes.getBody(), Deployment.class);

		// 获取service
		Map<String, Object> queryP = new HashMap<>();
		queryP.put("labelSelector", "app=" + detail.getName());
		K8SClientResponse svcRes = sService.doServiceByNamespace(detail.getNamespace(), null, queryP, HTTPMethod.GET, cluster);
		if (!HttpStatusUtil.isSuccessStatus(svcRes.getStatus())) {
			return ActionReturnUtil.returnErrorWithMsg(svcRes.getBody());
		}
		ServiceList svcs = JsonUtil.jsonToPojo(svcRes.getBody(), ServiceList.class);

		Map<String, Object> res = K8sResultConvert.convertAppPut(dep, svcs, detail.getContainers(), detail.getName());
		Map<String, Object> bodys = new HashMap<String, Object>();
		bodys = CollectionUtil.transBean2Map((Deployment) res.get("dep"));
		Map<String, Object> headers = new HashMap<>();
		headers.put("Content-Type", "application/json");
		K8SClientResponse putRes = dpService.doSpecifyDeployment(detail.getNamespace(), detail.getName(), headers,
				bodys, HTTPMethod.PUT, cluster);
		if (!HttpStatusUtil.isSuccessStatus(putRes.getStatus())) {
			return ActionReturnUtil.returnErrorWithMsg(putRes.getBody());
		}
		com.harmonycloud.k8s.bean.Service service = (com.harmonycloud.k8s.bean.Service) res.get("service");
		bodys.clear();
		bodys = CollectionUtil.transBean2Map(service);
		if (service != null) {
			K8SClientResponse putSvcRes = sService.doSepcifyService(detail.getNamespace(),
					service.getMetadata().getName(), headers, bodys, HTTPMethod.PUT, cluster);
			if (!HttpStatusUtil.isSuccessStatus(putSvcRes.getStatus())) {
				return ActionReturnUtil.returnErrorWithMsg(putSvcRes.getBody());
			}
		}
		return ActionReturnUtil.returnSuccessWithData("success");
	}

	@Override
	public ActionReturnUtil autoScaleDeployment(String name, String namespace, Integer max, Integer min, Integer cpu, Cluster cluster)
			throws Exception {
		
		Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("Content-Type", "application/json");
		Map<String, Object> bodys = new HashMap<String, Object>();
		bodys = CollectionUtil.transBean2Map(K8sResultConvert.convertHpa(name, namespace, max, min, cpu));
		K8SClientResponse response = hpaService.postHpautoscalerByNamespace(namespace, headers, bodys, HTTPMethod.POST, cluster);
		if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
			return ActionReturnUtil.returnErrorWithMsg(response.getBody());
		}
		return ActionReturnUtil.returnSuccessWithData(response.getBody());
	}

	@Override
	public ActionReturnUtil updateAutoScaleDeployment(String name, String namespace, Integer max, Integer min,
			Integer cpu, Cluster cluster) throws Exception {
		
		Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("Content-Type", "application/json");
		Map<String, Object> bodys = new HashMap<String, Object>();
		bodys = CollectionUtil.transBean2Map(K8sResultConvert.convertHpa(name, namespace, max, min, cpu));
		K8SClientResponse response = hpaService.doHpautoscalerByNamespace(namespace, headers, bodys, HTTPMethod.PUT, cluster);
		if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
			return ActionReturnUtil.returnErrorWithMsg(response.getBody());
		}
		return ActionReturnUtil.returnSuccessWithData(response.getBody());
	}

	@Override
	public ActionReturnUtil deleteAutoScaleDeployment(String name, String namespace, Cluster cluster) throws Exception {
		K8SClientResponse response = hpaService.doSpecifyHpautoscaler(namespace, name, null, null, HTTPMethod.DELETE, cluster);
		if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
			return ActionReturnUtil.returnErrorWithMsg(response.getBody());
		}
		return ActionReturnUtil.returnSuccessWithData(response.getBody());
	}
	
    @Override
    public ActionReturnUtil updateBusinessDeployment(UpdateDeployment deploymentDetail, String userName, Cluster cluster) throws Exception {
        if (deploymentDetail != null && StringUtils.isEmpty(deploymentDetail.getNamespace()) && StringUtils.isEmpty(deploymentDetail.getName())) {
            return ActionReturnUtil.returnErrorWithMsg("deploymentDetail is null");
        }

        String namespace = deploymentDetail.getNamespace();
        String name = deploymentDetail.getName();

        String labels = deploymentDetail.getLabels();
        String annotations = deploymentDetail.getAnnotation();

        String token = String.valueOf(K8SClient.tokenMap.get(userName));
        Map<String, Object> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + token);

        K8SClientResponse depRes = dpService.doSpecifyDeployment(namespace, name, headers, null, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus())) {
            return ActionReturnUtil.returnErrorWithMsg(depRes.getBody());
        }
        Deployment dep = JsonUtil.jsonToPojo(depRes.getBody(), Deployment.class);
        Map<String, Object> bodys = new HashMap<String, Object>();

        if (!StringUtils.isEmpty(annotations) && !StringUtils.isEmpty(labels)){
			//todo 'ingress,engress'

			dep.getMetadata().getAnnotations().put("nephele/annotation", annotations);
			//todo 'qos,'

			dep.getMetadata().getAnnotations().put("nephele/labels", labels);
			Map<String, Object> la = new HashMap<>();
			la.put("app", name);
			Map<String, Object> mla = new HashMap<>();
			mla.put("app", name);
			Map<String, Object> lmMap = new HashMap<String, Object>();
			lmMap.put("nephele/user", userName);
			if (!StringUtils.isEmpty(labels)) {
				String[] ls = labels.split(",");
				for (String label : ls) {
					String[] tmp = label.split("=");
					la.put(tmp[0], tmp[1]);
					lmMap.put(tmp[0], tmp[1]);
				}
			}
			dep.getMetadata().setLabels(lmMap);
			//labels-QOS
			if(!labels.equals("")){
				if(labels.contains(",")){
					String[] labs = labels.split(",");
					if (labs != null && labs.length > 0) {
						for (String s : labs) {
							if (s.contains("qos") && s.contains("=")) {
								la.put("qos",s.split("=")[1]);
							}
						}
					}
				}else{
					if (labels.contains("qos")&& labels.contains("=")) {
						la.put("qos",labels.split("=")[1]);
					}
				}
			}
			LabelSelector las=new LabelSelector();
			las.setMatchLabels(mla);
			dep.getSpec().setSelector(las);
			DeploymentStrategy strategy = new DeploymentStrategy();
			strategy.setType("Recreate");
			dep.getSpec().setStrategy(strategy);
			dep.getSpec().getTemplate().getMetadata().setLabels(la);
			//annotations-QOS
			Map<String, Object> metadataanno = new HashMap<>();
			if(!annotations.equals("")){
				if(annotations.contains(",")){
					String[] qos = annotations.split(",");
					if (qos != null && qos.length > 0) {
						for (String s : qos) {
							if (s.contains("ingress")&& s.contains("=")) {
								metadataanno.put("kubernetes.io/ingress-bandwidth",s.split("=")[1]);
							}
							if (s.contains("egress") && s.contains("=")) {
								metadataanno.put("kubernetes.io/egress-bandwidth",s.split("=")[1]);
							}
						}
					}
				}else{
					if (annotations.contains("ingress")&& annotations.contains("=")) {
						metadataanno.put("kubernetes.io/ingress-bandwidth",annotations.split("=")[1]);
					}
					if (annotations.contains("egress") && annotations.contains("=")) {
						metadataanno.put("kubernetes.io/egress-bandwidth",annotations.split("=")[1]);
					}
				}
			}
			dep.getSpec().getTemplate().getMetadata().setAnnotations(metadataanno);
		}

        if (deploymentDetail.getContainers() != null) {
			List<Container> newC = new ArrayList<Container>();
			List<Container> containers = dep.getSpec().getTemplate().getSpec().getContainers();
			Map<String, Container> ct = new HashMap<String, Container>();
			for (Container c : containers) {
				ct.put(c.getName(), c);
			}
			for (UpdateContainer cc : deploymentDetail.getContainers()) {
				Container container = ct.get(cc.getName());
				if (cc.getResource() != null) {
					Map<String, String> res = new HashMap<String, String>();
					res.put("cpu", cc.getResource().getCpu());
					res.put("memory", (cc.getResource().getMemory().contains("Mi") || cc.getResource().getMemory().contains("Gi")) ? cc.getResource().getMemory() : cc.getResource().getMemory()+"Mi");
					container.getResources().setLimits(res);
				}
				newC.add(container);
			}

			dep.getSpec().getTemplate().getSpec().setContainers(newC);
		}

        bodys = CollectionUtil.transBean2Map(dep);
        String lrv = watchService.getLatestVersion(namespace, headers, cluster);
        headers.put("Content-Type", "application/json");
        K8SClientResponse newRes = dpService.doSpecifyDeployment(namespace, name, headers, bodys, HTTPMethod.PUT, cluster);
        if (!HttpStatusUtil.isSuccessStatus(newRes.getStatus())) {
            return ActionReturnUtil.returnErrorWithMsg(newRes.getBody());
        }
        new Thread() {
            @Override
            public void start() {
                try {
                    watchAppEvent(name, namespace, null, lrv, userName, cluster);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

        return ActionReturnUtil.returnSuccess();
    }

}
