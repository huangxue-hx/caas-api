package com.harmonycloud.service.application.impl;

import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.util.*;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.dto.application.CreateConfigMapDto;
import com.harmonycloud.dto.application.CreateContainerDto;
import com.harmonycloud.dto.application.PersistentVolumeDto;
import com.harmonycloud.dto.application.JobsDetailDto;
import com.harmonycloud.k8s.bean.*;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.service.ConfigmapService;
import com.harmonycloud.k8s.service.EventService;
import com.harmonycloud.k8s.service.JobService;
import com.harmonycloud.k8s.service.PVCService;
import com.harmonycloud.k8s.service.PodService;
import com.harmonycloud.k8s.service.PvService;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import com.harmonycloud.service.application.JobsService;
import com.harmonycloud.service.application.PersistentVolumeService;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.platform.bean.EventDetail;
import com.harmonycloud.service.platform.bean.VolumeMountExt;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.platform.convert.K8sResultConvert;
import com.harmonycloud.service.tenant.NamespaceLocalService;
import com.harmonycloud.service.tenant.NamespaceService;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by root on 7/9/17.
 */
@Service
public class JobsServiceImpl implements JobsService{

    @Autowired
    JobService jobService;
    
    @Autowired
    private PersistentVolumeService volumeSerivce;

    @Autowired
    PodService podService;
    
    @Autowired
    private ConfigmapService configmapService;
    
    @Autowired
    private PVCService pvcService;
    
    @Autowired
    private NamespaceService namespaceService;
    
    @Autowired
    private PvService pvService;

	@Autowired
	EventService eventService;

	@Autowired
	NamespaceLocalService namespaceLocalService;
	@Autowired
	ClusterService clusterService;

    @Override
    public ActionReturnUtil createJob(JobsDetailDto detail, String userName) throws Exception {
    	//创建configmap/pvc
		Assert.hasText(detail.getNamespace());
		Cluster cluster = namespaceLocalService.getClusterByNamespaceName(detail.getNamespace());
    	List<CreateContainerDto> containers = detail.getContainers();
    	if(containers != null && containers.size() > 0){
    		for(CreateContainerDto c : containers){
    			List<CreateConfigMapDto> configMaps = c.getConfigmap();
    			//调用创建configmap接口
    			ActionReturnUtil cm = createConfigMap(configMaps, detail.getNamespace(), c.getName(), detail.getName(), cluster, Constant.TYPE_JOB, null);
    			if(!cm.isSuccess()){
    				return cm;
    			}
    			//创建PVC
    			if (c.getStorage() != null) {
                    for (PersistentVolumeDto pvc : c.getStorage()) {
                    	if(pvc.getType() != null && Constant.VOLUME_TYPE_PV.equals(pvc.getType())){
                    		if (pvc.getPvcName() == "" || pvc.getPvcName() == null) {
                                continue;
                            }
							pvc.setNamespace(detail.getNamespace());
							pvc.setServiceType(Constant.TYPE_JOB);
							pvc.setServiceName(detail.getName());
							pvc.setProjectId(detail.getProjectId());
							if(StringUtils.isBlank(pvc.getVolumeName())){
								pvc.setVolumeName(pvc.getPvcName());
							}
							volumeSerivce.createVolume(pvc);
                    	}
                    }
                }
    		}
    	}
    	//组装job
    	Job job = convertJob(detail, userName);
    	return jobService.addJob(detail.getNamespace(), job, cluster);
    }

    @SuppressWarnings("unchecked")
	@Override
    public ActionReturnUtil listJob(String projectId, String namespace, String labels, String status, String clusterId) throws Exception {
        Map<String, Object> bodys = new HashMap<String, Object>();

        if (StringUtils.isNotBlank(labels)) {
        	if(labels.indexOf(Constant.LABEL_PROJECT_ID)>0) {
				bodys.put("labelSelector", labels);
			}else{
				bodys.put("labelSelector", labels+","+Constant.LABEL_PROJECT_ID+"="+projectId);
			}
        }else{
			bodys.put("labelSelector", Constant.LABEL_PROJECT_ID+"="+projectId);
		}
		List<Cluster> clusters = new ArrayList<>();
        if(StringUtils.isNotBlank(namespace)){
			clusters.add(namespaceLocalService.getClusterByNamespaceName(namespace));
		}else if (StringUtils.isBlank(clusterId)) {
			clusters.add(clusterService.findClusterById(clusterId));
		} else {
			clusters = clusterService.listCluster();
		}

        JSONArray array = new JSONArray();
        for(Cluster cluster : clusters){
        	K8SClientResponse listJob = jobService.listJob(namespace,bodys,cluster);
            if (!HttpStatusUtil.isSuccessStatus(listJob.getStatus())) {
                return ActionReturnUtil.returnErrorWithData(listJob.getBody());
            }

            JobList jobList = JsonUtil.jsonToPojo(listJob.getBody(), JobList.class);
            if(jobList != null){
            	List<Job> jobs = jobList.getItems();
            	if(jobs != null && jobs.size() > 0){
            		for(Job job : jobs){
            			JSONObject json = new JSONObject();
            			json.put("name", job.getMetadata().getName());
            			json.put("namespace", job.getMetadata().getNamespace());
            			if(job.getMetadata().getAnnotations() != null && job.getMetadata().getAnnotations().containsKey("nephele/labels")){
            				json.put("labels", job.getMetadata().getAnnotations().get("nephele/labels"));
        				}else{
        					json.put("labels", "");
        				}
            			if(job.getMetadata().getAnnotations() != null && job.getMetadata().getAnnotations().containsKey("nephele/failed")){
            				json.put("failed", job.getMetadata().getAnnotations().get("nephele/failed"));
        				}else{
        					json.put("failed", 0);
        				}
            			if(job.getMetadata().getAnnotations() != null && job.getMetadata().getAnnotations().containsKey("nephele/succeed")){
            				json.put("succeed", job.getMetadata().getAnnotations().get("nephele/succeed"));
        				}else{
        					json.put("succeed", 0);
        				}
            			if(job.getMetadata().getAnnotations() != null && job.getMetadata().getAnnotations().containsKey("nephele/restart")){
            				json.put("restart", job.getMetadata().getAnnotations().get("nephele/restart"));
        				}else{
        					json.put("restart", 0);
        				}
        				List<String> img = new ArrayList<String>();
        				List<Container> containers = job.getSpec().getTemplate().getSpec().getContainers();
        				for (Container container : containers) {
        					img.add(container.getImage());
        				}
        				json.put("img", img);
        				json.put("owner", job.getMetadata().getLabels().get("nephele/user").toString());
        				json.put("completions", job.getSpec().getCompletions());
        				json.put("parallelism", job.getSpec().getParallelism() == 0 ? job.getMetadata().getAnnotations().get("nephele/parallelism").toString() : job.getSpec().getParallelism());
        				json.put("createTime", job.getMetadata().getCreationTimestamp());
        				long diff = 0;
        			    if(job.getStatus() != null && job.getStatus().getCompletionTime() != null){
        			      diff = (job.getStatus().getCompletionTime().getTime() - job.getStatus().getStartTime().getTime())/1000;
        			    }
        			    json.put("executionTime", diff);
        				json.put("selector", job.getSpec().getSelector());
        				String sta = getJobDetail(job);
        				json.put("status", sta);
        				if(StringUtils.isEmpty(status) || "all".equals(status)){
        					// 全部
        					array.add(json);
        				}else if(Constant.RUNNING.equals(status) && Constant.RUNNING.equals(sta)){
        					//运行
        					array.add(json);
        				}else if(Constant.JOB_SUCCEED.equals(status) && Constant.JOB_SUCCEED.equals(sta)){
        					//成功
        					array.add(json);
        				}else if(Constant.JOB_FAILED.equals(status) && Constant.JOB_FAILED.equals(sta)){
        					//失败
        					array.add(json);
        				}else if(Constant.SERVICE_STARTING.equals(status) && Constant.SERVICE_STARTING.equals(sta)){
        					//starting
        					array.add(json);
        				}else if(Constant.SERVICE_STOPPING.equals(status) && Constant.SERVICE_STOPPING.equals(sta)){
        					//stopping
        					array.add(json);
        				}else if(Constant.SERVICE_STOP.equals(status) && Constant.SERVICE_STOP.equals(sta)){
        					//stoped
        					array.add(json);
        				}
            		}
            	}
            }
        }
        
        return ActionReturnUtil.returnSuccessWithData(array);
    }

    @SuppressWarnings("unchecked")
	@Override
    public ActionReturnUtil getJobDetail(String namespace, String name) throws Exception {
    	Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        // 获取job
        Map<String, Object> bodys = new HashMap<String, Object>();
        K8SClientResponse deleteJob = jobService.getJob(namespace,name,bodys,cluster);
        if (!HttpStatusUtil.isSuccessStatus(deleteJob.getStatus())) {
            return ActionReturnUtil.returnErrorWithData(deleteJob.getBody());
        }
        Job job = JsonUtil.jsonToPojo(deleteJob.getBody(), Job.class);

        // 获取pod
        bodys.clear();
        bodys.put("labelSelector", "jobs=" + name);
        K8SClientResponse podRes = podService.getPodByNamespace(namespace, null, bodys, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(podRes.getStatus())) {
            return ActionReturnUtil.returnErrorWithData(podRes.getBody());
        }
        PodList podList = JsonUtil.jsonToPojo(podRes.getBody(), PodList.class);
        
        JSONObject jobjs =new JSONObject();
        jobjs.put("name", job.getMetadata().getName());
		jobjs.put("namespace", job.getMetadata().getNamespace());
		if(job.getMetadata().getAnnotations() != null && job.getMetadata().getAnnotations().containsKey("nephele/labels")){
			jobjs.put("labels", job.getMetadata().getAnnotations().get("nephele/labels"));
		}else{
			jobjs.put("labels", "");
		}
		if(job.getMetadata().getAnnotations() != null && job.getMetadata().getAnnotations().containsKey("nephele/annotation")){
			jobjs.put("annotation", job.getMetadata().getAnnotations().get("nephele/annotation"));
		}else{
			jobjs.put("annotation", "");
		}
		if(job.getMetadata().getAnnotations() != null && job.getMetadata().getAnnotations().containsKey("nephele/failed")){
			jobjs.put("failed", job.getMetadata().getAnnotations().get("nephele/failed"));
		}else{
			jobjs.put("failed", 0);
		}
		if(job.getMetadata().getAnnotations() != null && job.getMetadata().getAnnotations().containsKey("nephele/succeed")){
			jobjs.put("succeed", job.getMetadata().getAnnotations().get("nephele/succeed"));
		}else{
			jobjs.put("succeed", 0);
		}
		if(job.getMetadata().getAnnotations() != null && job.getMetadata().getAnnotations().containsKey("nephele/restart")){
			jobjs.put("restart", job.getMetadata().getAnnotations().get("nephele/restart"));
		}else{
			jobjs.put("restart", 0);
		}
		if (!job.getMetadata().getAnnotations().containsKey("updateTimestamp")
				|| StringUtils.isEmpty(job.getMetadata().getAnnotations().get("updateTimestamp").toString())) {
			jobjs.put("updatetime", job.getMetadata().getAnnotations().containsKey("updateTimestamp"));
		} else {
			jobjs.put("updateTime", "");
		}
		List<String> img = new ArrayList<String>();
		List<Container> containers = job.getSpec().getTemplate().getSpec().getContainers();
		JSONArray cons = new JSONArray();
		for (Container container : containers) {
			img.add(container.getImage());
			JSONObject js = new JSONObject();
			js.put("name", container.getName());
			js.put("image", container.getImage());
			if (container.getResources().getLimits() != null) {
				String pattern = ".*m.*";
				Pattern r = Pattern.compile(pattern);
				String cpu = ((Map<Object, Object>) container.getResources().getLimits()).get("cpu").toString();
				Matcher m = r.matcher(cpu);
				if (!m.find()) {
					((Map<Object, Object>) container.getResources().getLimits()).put("cpu",
							Integer.valueOf(cpu) * 1000 + "m");
				}
				js.put("resource", container.getResources().getLimits());
			} else {
				js.put("resource", "");
			}
			js.put("env", container.getEnv());
			js.put("command", container.getCommand());
			js.put("args", container.getArgs());
			if(container.getImagePullPolicy() != null) {
				js.put("imagePullPolicy", container.getImagePullPolicy());
			}else {
				js.put("imagePullPolicy", "");
			}
			/*js.put("livenessProbe", container.getLivenessProbe());
			js.put("readinessProbe", container.getReadinessProbe());*/
			js.put("ports", container.getPorts());
			List<VolumeMount> volumeMounts = container.getVolumeMounts();
			List<VolumeMountExt> vms = new ArrayList<VolumeMountExt>();
			if (volumeMounts != null && volumeMounts.size() > 0) {
				for (VolumeMount vm : volumeMounts) {
					VolumeMountExt vmExt = new VolumeMountExt(vm.getName(), vm.isReadOnly(), vm.getMountPath(),
							vm.getSubPath());
					for (Volume volume : job.getSpec().getTemplate().getSpec().getVolumes()) {
						if (vm.getName().equals(volume.getName())) {
							if (volume.getSecret() != null) {
								vmExt.setType("secret");
							} else if (volume.getPersistentVolumeClaim() != null) {
								vmExt.setType("nfs");
							} else if (volume.getEmptyDir() != null) {
								vmExt.setType("emptyDir");
								if(volume.getEmptyDir() != null){
									vmExt.setEmptyDir(volume.getEmptyDir().getMedium());
								}else{
									vmExt.setEmptyDir(null);
								}
								
								if (vm.getName().indexOf("logdir") == 0) {
									vmExt.setType("logDir");
								}
                            } else if (volume.getConfigMap() != null) {
                                Map<String, Object> configMap = new HashMap<String, Object>();
                                configMap.put("name", volume.getConfigMap().getName());
                                configMap.put("path", vm.getMountPath());
                                vmExt.setType("configMap");
                                vmExt.setConfigMapName(volume.getConfigMap().getName());
                            }else if (volume.getHostPath() != null) {
								vmExt.setType("hostPath");
								vmExt.setHostPath(volume.getHostPath().getPath());
								if (vm.getName().indexOf("logdir") == 0) {
									vmExt.setType("logDir");
								}
                            }
							if (vmExt.getReadOnly() == null) {
								vmExt.setReadOnly(false);
							}
							vms.add(vmExt);
							break;
						}
					}
				}
				js.put("storage", vms);
			}
			
			cons.add(js);
		}
		jobjs.put("container", cons);
		jobjs.put("img", img);
		jobjs.put("owner", job.getMetadata().getLabels().get("nephele/user").toString());
		jobjs.put("completions", job.getSpec().getCompletions());
		jobjs.put("parallelism", job.getSpec().getParallelism() == 0 ? job.getMetadata().getAnnotations().get("nephele/parallelism").toString() : job.getSpec().getParallelism());
		jobjs.put("createTime", job.getMetadata().getCreationTimestamp());
		long diff = 0;
	    if(job.getStatus() != null && job.getStatus().getCompletionTime() != null){
	      diff = (job.getStatus().getCompletionTime().getTime() - job.getStatus().getStartTime().getTime())/1000;
	    }
	    jobjs.put("executionTime", diff);
		jobjs.put("selector", job.getSpec().getSelector());
		jobjs.put("restartPolicy", job.getSpec().getTemplate().getSpec().getRestartPolicy());
		String sta = getJobDetail(job);
		jobjs.put("status", sta);
		//events
		List<EventDetail> allEvents = new ArrayList<EventDetail>();
		//获取job event
		bodys.clear();
		bodys.put("fieldSelector", "involvedObject.uid=" + job.getMetadata().getUid());
		K8SClientResponse evRes = eventService.doEventByNamespace(namespace, null, bodys, HTTPMethod.GET, cluster);
		if (!HttpStatusUtil.isSuccessStatus(evRes.getStatus())) {
			return ActionReturnUtil.returnErrorWithData(evRes.getBody());
		}
		EventList depeventList = JsonUtil.jsonToPojo(evRes.getBody(), EventList.class);
		if (depeventList.getItems() != null && depeventList.getItems().size() > 0) {
			allEvents.addAll(K8sResultConvert.convertPodEvent(depeventList.getItems()));
		}

		//获取pod event
		// 循环podlist获取每个pod的事件
		bodys.clear();
		for (Pod pod : podList.getItems()) {
			bodys.put("fieldSelector", "involvedObject.uid=" + pod.getMetadata().getUid());
			K8SClientResponse podevRes = eventService.doEventByNamespace(namespace, null, bodys, HTTPMethod.GET, cluster);
			if (!HttpStatusUtil.isSuccessStatus(podevRes.getStatus())) {
				return ActionReturnUtil.returnErrorWithData(podevRes.getBody());
			}
			EventList podeventList = JsonUtil.jsonToPojo(podevRes.getBody(), EventList.class);
			if (podeventList.getItems() != null && podeventList.getItems().size() > 0) {
				allEvents.addAll(K8sResultConvert.convertPodEvent(podeventList.getItems()));
			}
		}
		bodys.clear();
        bodys.put("job",jobjs);
        bodys.put("podList",K8sResultConvert.podListConvert(podList, "v1"));
        bodys.put("eventList",K8sResultConvert.sortByDesc(allEvents));
        return ActionReturnUtil.returnSuccessWithData(bodys);
    }

    @Override
    public ActionReturnUtil startJob(String name, String namespace, String userName) throws Exception {
		Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
    	//参数check
    	boolean paramboo = true;
    	StringBuffer sb = new StringBuffer();
    	if(StringUtils.isEmpty(name)){
    		paramboo = false;
    		sb.append("job名称、");
    	}
    	if(StringUtils.isEmpty(namespace)){
    		paramboo = false;
    		sb.append("分区、");
    	}
    	if(paramboo){
    		//根据name、namespace获取job
    		K8SClientResponse jobRes = jobService.getJob(namespace, name, null, cluster);
    		if(!HttpStatusUtil.isSuccessStatus(jobRes.getStatus())){
    			UnversionedStatus status = JsonUtil.jsonToPojo(jobRes.getBody(),UnversionedStatus.class);
    			return ActionReturnUtil.returnErrorWithData(status.getMessage());
    		}
    		Job job = JsonUtil.jsonToPojo(jobRes.getBody(),Job.class);
    		if(job != null){
    			if(job.getMetadata() != null && job.getSpec() != null){
    				int para = 1;
    				int com = 1;
    				if(job.getMetadata().getAnnotations() != null){
    					Map<String, Object> anno = ((Map<String, Object>) job.getMetadata().getAnnotations());
    					if (anno.containsKey("nephele/status") && anno.get("nephele/status") != null) {
    						String status = anno.get("nephele/status").toString();
    						if (status.equals(Constant.STARTING)) {
    							return ActionReturnUtil
    									.returnErrorWithData(ErrorCodeMessage.STOPPED,
												DictEnum.JOB.getChPhrase() + " " + name,true);
    						} else {
    							if (anno.get("nephele/parallelism") != null){
    								para = Integer.valueOf(anno.get("nephele/parallelism").toString());
    							}
    							if (anno.get("nephele/completions") != null){
    								com = Integer.valueOf(anno.get("nephele/completions").toString());
    							}
    							anno.put("nephele/status", Constant.STARTING);
    						}
    					} else {
    						anno.put("nephele/status", Constant.STARTING);
    						if (anno.get("nephele/parallelism") != null){
    							anno.put("nephele/parallelism", anno.get("nephele/parallelism"));
    						} else {
    							anno.put("nephele/parallelism",  "1");
    						}
    						if (anno.get("nephele/completions") != null){
    							anno.put("nephele/completions", anno.get("nephele/completions"));
    						} else {
    							anno.put("nephele/completions",  "1");
    						}

    					}
    				}
    				job.getSpec().setParallelism(para);
    				//job.getSpec().setCompletions(com);
    			}
    		}
    		//更新
    		return jobService.updateJob(namespace, name, job, cluster);
    	}else{
    		return ActionReturnUtil.returnErrorWithData(sb.toString());
    	}
    }

    @Override
    public ActionReturnUtil stopJob(String name, String namespace, String userName) throws Exception {
		Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
    	//参数check
    	boolean paramboo = true;
    	StringBuffer sb = new StringBuffer();
    	if(StringUtils.isEmpty(name)){
    		paramboo = false;
    		sb.append("job名称、");
    	}
    	if(StringUtils.isEmpty(namespace)){
    		paramboo = false;
    		sb.append("分区、");
    	}
    	if(paramboo){
    		//根据name、namespace获取job
    		K8SClientResponse jobRes = jobService.getJob(namespace, name, null, cluster);
    		if(!HttpStatusUtil.isSuccessStatus(jobRes.getStatus())){
    			UnversionedStatus status = JsonUtil.jsonToPojo(jobRes.getBody(),UnversionedStatus.class);
    			return ActionReturnUtil.returnErrorWithData(status.getMessage());
    		}
    		Job job = JsonUtil.jsonToPojo(jobRes.getBody(),Job.class);
    		if(job != null){
    			if(job.getMetadata() != null && job.getSpec() != null){
    				if(job.getSpec().getParallelism() != 0) {
        				int para = 1;
						int com = 1;
						para = job.getSpec().getParallelism();
						com = job.getSpec().getCompletions();
						if(job.getMetadata().getAnnotations() != null){
							Map<String, Object> anno = ((Map<String, Object>) job.getMetadata().getAnnotations());
							if (anno.containsKey("nephele/status") && anno.get("nephele/status") != null) {
								String status = anno.get("nephele/status").toString();
								if (status.equals(Constant.STOPPING)) {
									return ActionReturnUtil
											.returnErrorWithData(ErrorCodeMessage.STOPPED, DictEnum.JOB.getChPhrase(),true);
								} else {
									if (anno.get("nephele/parallelism") == null){
										anno.put("nephele/parallelism",para+"");
									}
									if (anno.get("nephele/completions") == null){
										anno.put("nephele/completions",com+"");
									}
		
									anno.put("nephele/status", Constant.STOPPING);
								}
							} else {
								anno.put("nephele/status", Constant.STOPPING);
								if (anno.get("nephele/parallelism") != null){
									anno.put("nephele/parallelism", anno.get("nephele/parallelism"));
								} else {
									anno.put("nephele/parallelism", para+"");
								}
								if (anno.get("nephele/completions") != null){
									anno.put("nephele/completions", anno.get("nephele/completions"));
								} else {
									anno.put("nephele/completions", com+"");
								}
							}
						}
        	    		job.getSpec().setParallelism(0);
    				}else {
    					return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.JOB_FINISHED);
    				}
    			}
    		}
    		//更新
    		return jobService.updateJob(namespace, name, job, cluster);
    	}else{
    		return ActionReturnUtil.returnErrorWithData(sb.toString());
    	}
    }

    @Override
    public ActionReturnUtil replaceJob(JobsDetailDto detail, String userName) throws Exception {
		AssertUtil.notNull(detail, DictEnum.JOB);
		AssertUtil.notEmpty(detail.getContainers(), DictEnum.JOB);
    	AssertUtil.notBlank(detail.getNamespace(), DictEnum.NAMESPACE);
		Cluster cluster = namespaceLocalService.getClusterByNamespaceName(detail.getNamespace());
		//获取job
		K8SClientResponse jobRes = jobService.getJob(detail.getNamespace(), detail.getName(), null, cluster);
		if (!HttpStatusUtil.isSuccessStatus(jobRes.getStatus())) {
			return ActionReturnUtil.returnErrorWithData(jobRes.getBody());
		}
		Job job = JsonUtil.jsonToPojo(jobRes.getBody(), Job.class);
		//删除configMap
		Map<String, Object> queryP = new HashMap<>();
		queryP.put("labelSelector", Constant.TYPE_JOB + "=" + detail.getName());
		K8SClientResponse conRes = configmapService.doSepcifyConfigmap(detail.getNamespace(), null, queryP, HTTPMethod.DELETE, cluster);
		if (!HttpStatusUtil.isSuccessStatus(conRes.getStatus()) && conRes.getStatus() != Constant.HTTP_404) {
			UnversionedStatus status = JsonUtil.jsonToPojo(conRes.getBody(), UnversionedStatus.class);
			return ActionReturnUtil.returnErrorWithData(status.getMessage());
		}

		//delete pvc
		K8SClientResponse pvcRes = pvcService.doSepcifyPVC(detail.getNamespace(), queryP, HTTPMethod.DELETE, cluster);
		if (!HttpStatusUtil.isSuccessStatus(pvcRes.getStatus()) && pvcRes.getStatus() != Constant.HTTP_404) {
			UnversionedStatus status = JsonUtil.jsonToPojo(pvcRes.getBody(), UnversionedStatus.class);
			return ActionReturnUtil.returnErrorWithData(status.getMessage());
		}
		List<CreateContainerDto> containers = detail.getContainers();
		if(containers != null && containers.size() > 0){
			for(CreateContainerDto c : containers){
				List<CreateConfigMapDto> configMaps = c.getConfigmap();
				//调用创建configmap接口
				ActionReturnUtil cm = createConfigMap(configMaps, detail.getNamespace(), c.getName(), detail.getName(), cluster, Constant.TYPE_JOB, null);
				if(!cm.isSuccess()){
					return cm;
				}
				//创建PVC
				if (c.getStorage() != null) {
					for (PersistentVolumeDto pvc : c.getStorage()) {
						if(pvc.getType() != null && Constant.VOLUME_TYPE_PV.equals(pvc.getType())){
							if (pvc.getPvcName() == "" || pvc.getPvcName() == null) {
								continue;
							}
							pvc.setNamespace(detail.getNamespace());
							pvc.setServiceType(Constant.TYPE_JOB);
							pvc.setServiceName(detail.getName());
							pvc.setProjectId(detail.getProjectId());
							if(StringUtils.isBlank(pvc.getVolumeName())){
								pvc.setVolumeName(pvc.getPvcName());
							}
							volumeSerivce.createVolume(pvc);
						}
					}
				}
			}
		}
		Job newjob = convertJob(job, detail, userName);
		return jobService.updateJob(detail.getNamespace(), detail.getName(), newjob, cluster);

    }

    @Override
    public ActionReturnUtil deleteJob(String name, String namespace, String userName) throws Exception {
		Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        //delete job
        ActionReturnUtil deleteJob = jobService.delJobByName(name,namespace,cluster);
        if (!deleteJob.isSuccess()){
            return deleteJob;
        }
        
        //delete configmap
        Map<String, Object> queryP = new HashMap<>();
        queryP.put("labelSelector", Constant.TYPE_JOB + "=" + name);
		K8SClientResponse conRes = configmapService.doSepcifyConfigmap(namespace, null, queryP, HTTPMethod.DELETE, cluster);
        if (!HttpStatusUtil.isSuccessStatus(conRes.getStatus()) && conRes.getStatus() != Constant.HTTP_404) {
            UnversionedStatus status = JsonUtil.jsonToPojo(conRes.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithData(status.getMessage());
        }

        //delete pod 
        K8SClientResponse podRes = podService.deletePods(namespace, queryP, cluster);
        if (!HttpStatusUtil.isSuccessStatus(conRes.getStatus()) && podRes.getStatus() != Constant.HTTP_404) {
            UnversionedStatus status = JsonUtil.jsonToPojo(podRes.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithData(status.getMessage());
        }
        
        //get pvc
        K8SClientResponse pvcsRes = pvcService.doSepcifyPVC(namespace, queryP, HTTPMethod.GET, cluster);
        if(!HttpStatusUtil.isSuccessStatus(pvcsRes.getStatus()) && pvcsRes.getStatus() != Constant.HTTP_404){
        	UnversionedStatus status = JsonUtil.jsonToPojo(pvcsRes.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithData(status.getMessage());
        }
        //delete pvc
        K8SClientResponse pvcRes = pvcService.doSepcifyPVC(namespace, queryP, HTTPMethod.DELETE, cluster);
        if (!HttpStatusUtil.isSuccessStatus(pvcRes.getStatus()) && pvcRes.getStatus() != Constant.HTTP_404) {
            UnversionedStatus status = JsonUtil.jsonToPojo(pvcRes.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithData(status.getMessage());
        }
        //update pv
        PersistentVolumeClaimList pvcList = JsonUtil.jsonToPojo(pvcsRes.getBody(), PersistentVolumeClaimList.class);
        if(pvcList != null){
        	List<PersistentVolumeClaim> pvcs = pvcList.getItems();
        	if(pvcs != null && pvcs.size() > 0){
        		for(PersistentVolumeClaim pvc : pvcs){
        			if(pvc != null && pvc.getSpec() != null && pvc.getSpec().getVolumeName() != null){
        				String pvname = pvc.getSpec().getVolumeName();
						PersistentVolume pv = pvService.getPvByName(pvname,null);
						if (pv != null) {
							Map<String, Object> bodysPV = new HashMap<String, Object>();
							Map<String, Object> metadata = new HashMap<String, Object>();
							metadata.put("name", pv.getMetadata().getName());
							metadata.put("labels", pv.getMetadata().getLabels());
							bodysPV.put("metadata", metadata);
							Map<String, Object> spec = new HashMap<String, Object>();
							spec.put("capacity", pv.getSpec().getCapacity());
							spec.put("nfs", pv.getSpec().getNfs());
							spec.put("accessModes", pv.getSpec().getAccessModes());
							bodysPV.put("spec", spec);
							K8SURL urlPV = new K8SURL();
							urlPV.setResource(Resource.PERSISTENTVOLUME).setSubpath(pvname);
							Map<String, Object> headersPV = new HashMap<>();
							headersPV.put("Content-Type", "application/json");
							K8SClientResponse responsePV = new K8sMachineClient().exec(urlPV, HTTPMethod.PUT,
									headersPV, bodysPV);
							if (!HttpStatusUtil.isSuccessStatus(responsePV.getStatus())) {
								UnversionedStatus status = JsonUtil.jsonToPojo(responsePV.getBody(), UnversionedStatus.class);
								return ActionReturnUtil.returnErrorWithData(status.getMessage());
							}
						}
        			}
        		}
        	}
        }

        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public ActionReturnUtil reRunJob(String name, String namespace, String userName) throws Exception {
		Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
    	//参数check
    	boolean paramboo = true;
    	StringBuffer sb = new StringBuffer();
    	if(StringUtils.isEmpty(name)){
    		paramboo = false;
    		sb.append("job名称");
    	}
    	if(StringUtils.isEmpty(namespace)){
    		paramboo = false;
    		sb.append("分区");
    	}
		int restart = 0;
		int succeed = 0;
		int failed = 0;
    	if(paramboo){
    		//根据name、namespace获取job
    		K8SClientResponse jobRes = jobService.getJob(namespace, name, null, cluster);
    		if(!HttpStatusUtil.isSuccessStatus(jobRes.getStatus())){
    			UnversionedStatus status = JsonUtil.jsonToPojo(jobRes.getBody(),UnversionedStatus.class);
    			return ActionReturnUtil.returnErrorWithData(status.getMessage());
    		}
    		Job job = JsonUtil.jsonToPojo(jobRes.getBody(),Job.class);
    		
    		Map<String, Object> label = new HashMap<String, Object>();
    		label.put("labelSelector", Constant.TYPE_JOB+"="+name);
    		//获取config map
			K8SClientResponse cmRes = configmapService.doSepcifyConfigmap(namespace, null, label, HTTPMethod.GET, cluster);
    		boolean cmboo = true;
    		if(cmRes.getStatus() == Constant.HTTP_404){
    			cmboo = false;
    		}
    		
    		if(!HttpStatusUtil.isSuccessStatus(cmRes.getStatus()) && cmRes.getStatus() != Constant.HTTP_404){
    			UnversionedStatus status = JsonUtil.jsonToPojo(cmRes.getBody(),UnversionedStatus.class);
    			return ActionReturnUtil.returnErrorWithData(status.getMessage());
    		}
			//获取pvc
    		K8SClientResponse pvcRes = pvcService.doSepcifyPVC(namespace, label, HTTPMethod.GET, cluster);
    		boolean pvcboo = true;
    		if(pvcRes.getStatus() == Constant.HTTP_404){
    			pvcboo = false;
    		}
    		if(!HttpStatusUtil.isSuccessStatus(pvcRes.getStatus()) && pvcRes.getStatus() != Constant.HTTP_404){
    			UnversionedStatus status = JsonUtil.jsonToPojo(pvcRes.getBody(),UnversionedStatus.class);
    			return ActionReturnUtil.returnErrorWithData(status.getMessage());
    			
    		}
    		
    		if(job != null){
    			//删除已有的job
    			Job newjob = new Job();
    			ActionReturnUtil delRes=deleteJob(name, namespace, userName);
    			if(!delRes.isSuccess()){
    				return delRes;
    			}
				String sta = getJobDetail(job);
				//组装新的job
				ObjectMeta metadata = new ObjectMeta();
				//名称 namespace
				metadata.setName(name);
				metadata.setNamespace(namespace);
    			//更改状态
    			if(job.getMetadata().getAnnotations() != null){
    				Map<String, Object> anno = (Map<String, Object>) job.getMetadata().getAnnotations();
					if(job.getMetadata().getAnnotations().containsKey("nephele/restart")){
						restart = Integer.parseInt(job.getMetadata().getAnnotations().get("nephele/restart").toString());
					}
					if(job.getMetadata().getAnnotations().containsKey("nephele/succeed")){
						succeed = Integer.parseInt(job.getMetadata().getAnnotations().get("nephele/succeed").toString());
					}
					if(job.getMetadata().getAnnotations().containsKey("nephele/failed")){
						failed = Integer.parseInt(job.getMetadata().getAnnotations().get("nephele/failed").toString());
					}
					if(Constant.JOB_SUCCEED.equals(sta)){
						//成功次数
						succeed++;
					}else{
						//失败次数
						failed++;
					}
					//获取重启次数
					restart++;
					anno.put("nephele/status", Constant.STARTING);
					anno.put("nephele/restart", restart+"");
					anno.put("nephele/succeed", succeed+"");
					anno.put("nephele/failed", failed+"");
					metadata.setAnnotations(anno);
    			}else{
    				Map<String, Object> anno = new HashMap<String, Object>();
					if(Constant.JOB_SUCCEED.equals(sta)){
						//成功次数
						succeed++;
					}else{
						//失败次数
						failed++;
					}
					//获取重启次数
					restart++;
					anno.put("nephele/restart", restart+"");
					anno.put("nephele/succeed", succeed+"");
					anno.put("nephele/failed", failed+"");
    				anno.put("nephele/user", userName);
    				anno.put("nephele/status", Constant.STARTING);
    				anno.put("nephele/parallelism", job.getSpec().getParallelism()+"");
    				metadata.setAnnotations(anno);
    			}
    			//label
    			if(job.getMetadata().getLabels() != null){
    				Map<String, Object> lmMap = (Map<String, Object>) job.getMetadata().getLabels();
    				lmMap.put("nephele/user", userName);
        			lmMap.put(Constant.TYPE_JOB, name);
        			metadata.setLabels(lmMap);
    			}else{
    				Map<String, Object> lmMap = new HashMap<String, Object>();
        			lmMap.put("nephele/user", userName);
        			lmMap.put(Constant.TYPE_JOB, name);
        			metadata.setLabels(lmMap);
    			}
    			newjob.setMetadata(metadata);
    			JobSpec jobSpec = new JobSpec();
    			
    			if(job.getSpec() != null){
    				//activeDeadlineSeconds
    				if(job.getSpec().getActiveDeadlineSeconds() != null){
    					jobSpec.setActiveDeadlineSeconds(job.getSpec().getActiveDeadlineSeconds());
    				}
    				/*if(job.getSpec().isManualSelector() ==  true){
    					jobSpec.setManualSelector(job.getSpec().isManualSelector());
    				}*/
    				if(job.getSpec().getCompletions() != null && job.getSpec().getCompletions() != 0 ){
    					jobSpec.setCompletions(job.getSpec().getCompletions());
    				}else{
    					jobSpec.setCompletions(1);
    				}
    				if(job.getSpec().getParallelism() != null && job.getSpec().getParallelism() != 0 ){
    					jobSpec.setParallelism(job.getSpec().getParallelism());
    				}else{
    					jobSpec.setParallelism(1);
    				}
    				//template
    				PodTemplateSpec template = new PodTemplateSpec();
    				ObjectMeta meta = new ObjectMeta();
    				meta.setName(name);
    				meta.setNamespace(namespace);
    				Map<String, Object> lmMap = new HashMap<String, Object>();
        			lmMap.put("nephele/user", userName);
        			lmMap.put(Constant.TYPE_JOB, name);
    				meta.setLabels(lmMap);
    				template.setMetadata(meta);
    				PodSpec spec = job.getSpec().getTemplate().getSpec();
    				template.setSpec(spec);
    				jobSpec.setTemplate(template);
    			}
    			newjob.setSpec(jobSpec);
    			//创建configmap
    			if(cmboo){
        			ConfigMapList cmlist= JsonUtil.jsonToPojo(cmRes.getBody(), ConfigMapList.class);
        			if(cmlist != null){
        				List<ConfigMap> cms = cmlist.getItems();
        				if(cms != null && cms.size() > 0){
        					for(ConfigMap cm : cms){
        						Map<String, Object> headers = new HashMap<String, Object>();
        						headers.put("Content-type", "application/json");
        						Map<String, Object> bodys = new HashMap<>();
        						bodys.put("metadata", cm.getMetadata());
        						bodys.put("kind", cm.getKind());
        						bodys.put("data", cm.getData());
        						K8SClientResponse response = configmapService.doSepcifyConfigmap(namespace, headers, bodys, HTTPMethod.POST, cluster);
        						if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
        							UnversionedStatus status = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
        							return ActionReturnUtil.returnErrorWithData(status.getMessage());
        						}
        					}
        				}
        			}
        		}
    			//创建pvc
    			if(pvcboo){
    				PersistentVolumeClaimList pvcList = JsonUtil.jsonToPojo(pvcRes.getBody(), PersistentVolumeClaimList.class);
    				if(pvcList != null){
        				List<PersistentVolumeClaim> pvcs = pvcList.getItems();
        				if(pvcs != null && pvcs.size() > 0){
        					for(PersistentVolumeClaim pvc : pvcs){
        						Map<String, Object> headers = new HashMap<String, Object>();
        						headers.put("Content-type", "application/json");
        						Map<String, Object> bodys = new HashMap<>();
        						bodys.put("metadata", pvc.getMetadata());
        						bodys.put("kind", pvc.getKind());
        						bodys.put("spec", pvc.getSpec());
        						K8SClientResponse response = pvcService.doSepcifyPVC(namespace, headers, bodys, HTTPMethod.POST, cluster);
        						if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
        							UnversionedStatus status = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
        							return ActionReturnUtil.returnErrorWithData(status.getMessage());
        						}
        					}
        				}
        			}
    			}
    			//创建job
    			jobService.addJob(namespace, newjob, cluster);
    		}else{
    			return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.NAME_EXIST,"job",true);
    		}
    	}else{
    		return ActionReturnUtil.returnErrorWithData(sb.toString()+"为空");
    	}
        return ActionReturnUtil.returnSuccess();
    }
    
    /**
     * create configmap
     * 
     * @param configMaps
     * @param namespace
     * @param containerName
     * 			容器名称
     * @param name
     * 			deployment/job/cronjob名称
     * */
	@Override
	public ActionReturnUtil createConfigMap(List<CreateConfigMapDto> configMaps, String namespace, String containerName,
			String name, Cluster cluster, String type, String appName) throws Exception {
		if (configMaps != null && configMaps.size() > 0) {
			K8SURL url = new K8SURL();
			url.setNamespace(namespace).setResource(Resource.CONFIGMAP);
			Map<String, Object> bodys = new HashMap<String, Object>();
			Map<String, Object> meta = new HashMap<String, Object>();
			meta.put("namespace", namespace);
			meta.put("name", name + containerName);
			Map<String, Object> label = new HashMap<String, Object>();
			label.put(type, name);
			if (!StringUtils.isEmpty(appName)){
				label.put("app",appName);
			}
			meta.put("labels", label);
			bodys.put("metadata", meta);
			Map<String, Object> data = new HashMap<String, Object>();
			for (CreateConfigMapDto configMap : configMaps) {
				if (configMap != null && !StringUtils.isEmpty(configMap.getPath())) {
					if (StringUtils.isEmpty(configMap.getFile())) {
						data.put("config.json", configMap.getValue());
					} else {
						data.put(configMap.getFile() + "v" + configMap.getTag(), configMap.getValue());
					}
				}
			}
			bodys.put("data", data);
			Map<String, Object> headers = new HashMap<String, Object>();
			headers.put("Content-type", "application/json");
			K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.POST, headers, bodys, cluster);
			if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
				UnversionedStatus status = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
				return ActionReturnUtil.returnErrorWithData(status.getMessage());
			}
		}
		return ActionReturnUtil.returnSuccess();
	}
    
    private Job convertJob(JobsDetailDto detail, String userName) throws Exception{
    	Job job = new Job();
    	ObjectMeta meta = new ObjectMeta();
		meta.setName(detail.getName());
		Map<String, Object> lmMap = new HashMap<String, Object>();
		lmMap.put("nephele/user", userName);
		lmMap.put(Constant.TYPE_JOB, detail.getName());
		if (!StringUtils.isEmpty(detail.getLabels())) {
		      String[] ls = detail.getLabels().split(",");
		      for (String label : ls) {
		        String[] tmp = label.split("=");
		        lmMap.put(tmp[0], tmp[1]);
		      }
		    }
		meta.setLabels(lmMap);
		Map<String, Object> anno = new HashMap<String, Object>();
		String annotation=detail.getAnnotation();
		if( !StringUtils.isEmpty(annotation) && annotation.lastIndexOf(",") == 0){
		    annotation=annotation.substring(0, annotation.length()-1);
		}

		anno.put("nephele/annotation", annotation == null ? "" : annotation);
		anno.put("nephele/status", Constant.STARTING);
		anno.put("nephele/restart", "0");
		anno.put("nephele/succeed", "0");
		anno.put("nephele/failed", "0");
		anno.put("nephele/parallelism", detail.getParallelism()+"");
		anno.put("nephele/labels", detail.getLabels() == null ? "" : detail.getLabels());
		meta.setAnnotations(anno);
		meta.setNamespace(detail.getNamespace());
		job.setMetadata(meta);
		JobSpec jobSpec = new JobSpec();
		/*if(detail.getActiveDeadlineSeconds() != null && detail.getActiveDeadlineSeconds() != 0){
			jobSpec.setActiveDeadlineSeconds(detail.getActiveDeadlineSeconds());
		}*/
		jobSpec.setCompletions(detail.getCompletions() == 0 ? 1 : detail.getCompletions());
		jobSpec.setParallelism(detail.getParallelism() == 0 ? 1 : detail.getParallelism());
		PodTemplateSpec template = K8sResultConvert.convertPodTemplate(detail.getName(), detail.getContainers(), detail.getLabels(), detail.getAnnotation(), userName, Constant.TYPE_JOB, detail.getNodeSelector(), detail.getRestartPolicy(), detail.getNamespace());
		jobSpec.setTemplate(template);
		job.setSpec(jobSpec);
    	return job ;
    }
    
    private Job convertJob(Job job, JobsDetailDto detail, String userName) throws Exception{
    	ObjectMeta meta = job.getMetadata();
		meta.setName(detail.getName());
		Map<String, Object> lmMap = meta.getLabels();
		lmMap.put("nephele/user", userName);
		lmMap.put(Constant.TYPE_JOB, detail.getName());
		if (!StringUtils.isEmpty(detail.getLabels())) {
		      String[] ls = detail.getLabels().split(",");
		      for (String label : ls) {
		        String[] tmp = label.split("=");
		        lmMap.put(tmp[0], tmp[1]);
		      }
		    }
		meta.setLabels(lmMap);
		Map<String, Object> anno = job.getMetadata().getAnnotations();
		String annotation=detail.getAnnotation();
		if( !StringUtils.isEmpty(annotation) && annotation.lastIndexOf(",") == 0){
		    annotation=annotation.substring(0, annotation.length()-1);
		}
		Date now = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		String updateTime = sdf.format(now);
		anno.put("updateTimestamp", updateTime);
		anno.put("nephele/annotation", annotation == null ? "" : annotation);
		anno.put("nephele/parallelism", detail.getParallelism()+"");
		anno.put("nephele/labels", detail.getLabels() == null ? "" : detail.getLabels());
		meta.setAnnotations(anno);
		job.setMetadata(meta);
		JobSpec jobSpec = job.getSpec();
		/*if(detail.getActiveDeadlineSeconds() != 0){
			jobSpec.setActiveDeadlineSeconds(detail.getActiveDeadlineSeconds());
		}*/
		jobSpec.setCompletions(detail.getCompletions() == 0 ? 1 : detail.getCompletions());
		PodTemplateSpec template = K8sResultConvert.convertUpdatePodTemplate(job, detail.getContainers(), Constant.TYPE_JOB, detail.getLabels(), detail.getAnnotation(), userName, detail.getNodeSelector());
		jobSpec.setTemplate(template);
		job.setSpec(jobSpec);
    	return job ;
    }
    
    private String getJobDetail(Job job){
    	String status = "";
    	if ( job.getMetadata().getAnnotations() != null &&  job.getMetadata().getAnnotations().containsKey("nephele/status")) {
			status = job.getMetadata().getAnnotations().get("nephele/status").toString();
		}
    	if (!StringUtils.isEmpty(status)) {
			switch (Integer.valueOf(status)) {
			case 3:
				if (job.getStatus().getSucceeded() != null&&job.getStatus().getSucceeded() > 0) {
					status = Constant.JOB_SUCCEED;
				} else if (job.getStatus().getActive() != null && job.getStatus().getActive() >0){
					status = Constant.RUNNING;
				}else if(job.getStatus().getFailed() != null && job.getStatus().getFailed() >0){
					status = Constant.JOB_FAILED;
				}else{
					status = Constant.SERVICE_STARTING;
				}
				break;
			case 2:
				if (job.getSpec().getParallelism() != null
						&& job.getSpec().getParallelism() > 0) {
					status = Constant.SERVICE_STOPPING;
				} else {
					status = Constant.SERVICE_STOP;
				}
				break;
			default:
				if (job.getStatus().getSucceeded() != null&&job.getStatus().getSucceeded() > 0) {
					status = Constant.JOB_SUCCEED;
				} else if (job.getStatus().getActive() != null && job.getStatus().getActive() >0){
					status = Constant.RUNNING;
				}else if(job.getStatus().getFailed() != null && job.getStatus().getFailed() >0){
					status = Constant.JOB_FAILED;
				}
				break;
			}
		} else {
			if (job.getStatus().getSucceeded() != null&&job.getStatus().getSucceeded() > 0) {
				status = Constant.JOB_SUCCEED;
			} else if (job.getStatus().getActive() != null && job.getStatus().getActive() >0){
				status = Constant.RUNNING;
			}else if(job.getStatus().getFailed() != null && job.getStatus().getFailed() >0){
				status = Constant.JOB_FAILED;
			}
		}
    	return status;
    }

	@Override
	public ActionReturnUtil updateJobParallelism(String name, String namespace, int parallelism)
			throws Exception {
		Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
		boolean boo = true;
		StringBuffer sb = new StringBuffer();
		if(StringUtils.isEmpty(name)) {
			boo = false;
			sb.append("job名称");
		}
		if(StringUtils.isEmpty(name)) {
			boo = false;
			sb.append("分区");
		}
		if(parallelism == 0) {
			boo = false;
			sb.append("并行数");
		}
		if(boo) {
			//根据name、namespace获取job
    		K8SClientResponse jobRes = jobService.getJob(namespace, name, null, cluster);
    		if(!HttpStatusUtil.isSuccessStatus(jobRes.getStatus())){
    			UnversionedStatus status = JsonUtil.jsonToPojo(jobRes.getBody(),UnversionedStatus.class);
    			return ActionReturnUtil.returnErrorWithData(status.getMessage());
    		}
    		Job job = JsonUtil.jsonToPojo(jobRes.getBody(),Job.class);
    		if(job != null) {
    			if(job.getMetadata() != null && job.getMetadata().getAnnotations() != null) {
    				Map<String, Object> anno = job.getMetadata().getAnnotations();
    				anno.put("nephele/parallelism", parallelism+"");
    			}else {
    				Map<String, Object> anno = new HashMap<String, Object>();
    				anno.put("nephele/parallelism", parallelism+"");
    				job.getMetadata().setAnnotations(anno);
    			}
    			if(job.getSpec() != null && job.getSpec().getParallelism() != null) {
    				if(job.getSpec().getParallelism() != 0) {
    					job.getSpec().setParallelism(parallelism);
    				}
    			}
    			return jobService.updateJob(namespace, name, job, cluster);
    		}else {
    			return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.NOT_EXIST, DictEnum.JOB.phrase(),true);
    		}
		}else {
			return ActionReturnUtil.returnErrorWithData(sb.toString()+"为空");
		}
	}
}
