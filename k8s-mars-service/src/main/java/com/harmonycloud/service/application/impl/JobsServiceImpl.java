package com.harmonycloud.service.application.impl;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.dto.business.CreateConfigMapDto;
import com.harmonycloud.dto.business.CreateContainerDto;
import com.harmonycloud.dto.business.CreateVolumeDto;
import com.harmonycloud.dto.business.JobsDetailDto;
import com.harmonycloud.k8s.bean.*;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.service.ConfigmapService;
import com.harmonycloud.k8s.service.JobService;
import com.harmonycloud.k8s.service.PVCService;
import com.harmonycloud.k8s.service.PodService;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import com.harmonycloud.service.application.JobsService;
import com.harmonycloud.service.application.VolumeSerivce;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.platform.convert.K8sResultConvert;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by root on 7/9/17.
 */
@Service
public class JobsServiceImpl implements JobsService{

    @Autowired
    JobService jobService;
    
    @Autowired
    private VolumeSerivce volumeSerivce;

    @Autowired
    PodService podService;
    
    @Autowired
    private ConfigmapService configmapService;
    
    @Autowired
    private PVCService pvcService;


    @Override
    public ActionReturnUtil createJob(JobsDetailDto detail, String userName, Cluster cluster) throws Exception {
    	//创建configmap/pvc
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
                    for (CreateVolumeDto pvc : c.getStorage()) {
                    	if(pvc.getType() != null && Constant.VOLUME_TYPE_PV.equals(pvc.getType())){
                    		if (pvc.getPvcName() == "" || pvc.getPvcName() == null) {
                                continue;
                            }
                    		volumeSerivce.createVolume(detail.getNamespace(), pvc.getPvcName(), pvc.getPvcCapacity(), pvc.getPvcTenantid(), pvc.getReadOnly(), pvc.getPvcBindOne(), pvc.getVolume(), Constant.TYPE_JOB, detail.getName());
                    	}
                    }
                }
    		}
    	}
    	//组装job
    	Job job = convertJob(detail, userName);
    	return jobService.addJob(job, cluster);
    }

    @Override
    public ActionReturnUtil listJob(String tenantId, String name, String namespace, String labels, String status, Cluster cluster) throws Exception {
        Map<String, Object> bodys = new HashMap<String, Object>();

        if (!StringUtils.isEmpty(labels)) {
            bodys.put("labelSelector", labels);
        }

        K8SClientResponse listJob = jobService.listJob(namespace,bodys,cluster);
        if (!HttpStatusUtil.isSuccessStatus(listJob.getStatus())) {
            return ActionReturnUtil.returnErrorWithMsg(listJob.getBody());
        }

        JobList jobList = JsonUtil.jsonToPojo(listJob.getBody(), JobList.class);
        JSONArray array = new JSONArray();
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
    				List<String> img = new ArrayList<String>();
    				List<Container> containers = job.getSpec().getTemplate().getSpec().getContainers();
    				for (Container container : containers) {
    					img.add(container.getImage());
    				}
    				json.put("img", img);
    				json.put("owner", job.getMetadata().getLabels().get("nephele/user").toString());
    				json.put("completions", job.getSpec().getCompletions());
    				json.put("parallelism", job.getSpec().getParallelism());
    				json.put("createTime", job.getMetadata().getCreationTimestamp());
    				json.put("namespace", job.getMetadata().getNamespace());
    				json.put("selector", job.getSpec().getSelector());
    				String sta = getJobDetail(job);
    				json.put("status", sta);
    				if(StringUtils.isEmpty(status)){
    					// 全部
    					array.add(json);
    				}else if(Constant.RUNNING.equals(status) && Constant.RUNNING.equals(sta)){
    					//运行
    					array.add(json);
    				}else if(Constant.JOB_SUCCEEDED.equals(status) && Constant.JOB_SUCCEEDED.equals(sta)){
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
        return ActionReturnUtil.returnSuccessWithData(jobList);
    }

    @Override
    public ActionReturnUtil getJobDetail(String namespace, String name, Cluster cluster) throws Exception {
        // 获取job
        Map<String, Object> bodys = new HashMap<String, Object>();
        K8SClientResponse deleteJob = jobService.getJob(namespace,name,bodys,cluster);
        if (!HttpStatusUtil.isSuccessStatus(deleteJob.getStatus())) {
            return ActionReturnUtil.returnErrorWithMsg(deleteJob.getBody());
        }
        Job job = JsonUtil.jsonToPojo(deleteJob.getBody(), Job.class);

        // 获取pod
        bodys.clear();
        bodys.put("labelSelector", "jobs=" + name);
        K8SClientResponse podRes = podService.getPodByNamespace(namespace, null, bodys, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(podRes.getStatus())) {
            return ActionReturnUtil.returnErrorWithMsg(podRes.getBody());
        }
        PodList podList = JsonUtil.jsonToPojo(podRes.getBody(), PodList.class);
        
        bodys.clear();
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
			js.put("resources", container.getResources().getLimits());
			js.put("env", container.getEnv());
			js.put("command", container.getCommand());
			js.put("args", container.getArgs());
			js.put("livenessProbe", container.getLivenessProbe());
			js.put("readinessProbe", container.getReadinessProbe());
			js.put("ports", container.getPorts());
			cons.add(js);
		}
		jobjs.put("img", img);
		jobjs.put("owner", job.getMetadata().getLabels().get("nephele/user").toString());
		jobjs.put("completions", job.getSpec().getCompletions());
		jobjs.put("parallelism", job.getSpec().getParallelism());
		jobjs.put("createTime", job.getMetadata().getCreationTimestamp());
		jobjs.put("namespace", job.getMetadata().getNamespace());
		jobjs.put("selector", job.getSpec().getSelector());
		jobjs.put("restartPolicy", job.getSpec().getTemplate().getSpec().getRestartPolicy());
		String sta = getJobDetail(job);
		jobjs.put("status", sta);
        bodys.put("job",jobjs);
        bodys.put("podlist",K8sResultConvert.podListConvert(podList, "v1"));
        return ActionReturnUtil.returnSuccessWithData(bodys);
    }

    @Override
    public ActionReturnUtil startJob(String name, String namespace, String userName, Cluster cluster) throws Exception {
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
    			return ActionReturnUtil.returnErrorWithMsg(status.getMessage());
    		}
    		Job job = JsonUtil.jsonToPojo(jobRes.getBody(),Job.class);
    		if(job != null){
    			if(job.getMetadata() != null && job.getSpec() != null){
    				int para = 1;
    				if(job.getMetadata().getAnnotations() != null){
    					Map<String, Object> anno = ((Map<String, Object>) job.getMetadata().getAnnotations());
    					if (anno.containsKey("nephele/status") && anno.get("nephele/status") != null) {
    						String status = anno.get("nephele/status").toString();
    						if (status.equals(Constant.STARTING)) {
    							return ActionReturnUtil
    									.returnErrorWithMsg("job " + name + " is already started");
    						} else {
    							if (anno.get("nephele/parallelism") != null){
    								para = Integer.valueOf(anno.get("parallelism").toString());
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

    					}
    				}
    				job.getSpec().setParallelism(para);
    			}
    		}
    		//更新
    		return jobService.updateJob(namespace, name, job, cluster);
    	}else{
    		return ActionReturnUtil.returnErrorWithMsg(sb.toString());
    	}
    }

    @Override
    public ActionReturnUtil stopJob(String name, String namespace, String userName, Cluster cluster) throws Exception {
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
    			return ActionReturnUtil.returnErrorWithMsg(status.getMessage());
    		}
    		Job job = JsonUtil.jsonToPojo(jobRes.getBody(),Job.class);
    		if(job != null){
    			if(job.getMetadata() != null && job.getSpec() != null){
    				if(job.getStatus() != null && job.getStatus() != null){
    					if(job.getStatus().getActive() != null && job.getStatus().getActive() == job.getSpec().getParallelism()){
    						int para = 1;
    	    				para = job.getSpec().getParallelism();
    	    				if(job.getMetadata().getAnnotations() != null){
    	    					Map<String, Object> anno = ((Map<String, Object>) job.getMetadata().getAnnotations());
    	    					if (anno.containsKey("nephele/status") && anno.get("nephele/status") != null) {
    	    						String status = anno.get("nephele/status").toString();
    	    						if (status.equals(Constant.STOPPING)) {
    	    							return ActionReturnUtil
    	    									.returnErrorWithMsg("job " + name + " is already stoped");
    	    						} else {
    	    							if (anno.get("nephele/parallelism") == null){
    	    								anno.put("nephele/parallelism",para);
    	    							}

    	    							anno.put("nephele/status", Constant.STOPPING);
    	    						}
    	    					} else {
    	    						anno.put("nephele/status", Constant.STOPPING);
    	    						if (anno.get("nephele/parallelism") != null){
    	    							anno.put("nephele/parallelism", anno.get("nephele/parallelism"));
    	    						} else {
    	    							anno.put("nephele/parallelism", para);
    	    						}

    	    					}
    	    				}
    	    				job.getSpec().setParallelism(para);
    					}else{
    						return ActionReturnUtil
									.returnErrorWithMsg("job " + name + " is already completed or Failed");
    					}
    				}
    			}
    		}
    		//更新
    		return jobService.updateJob(namespace, name, job, cluster);
    	}else{
    		return ActionReturnUtil.returnErrorWithMsg(sb.toString());
    	}
    }

    @Override
    public ActionReturnUtil replaceJob(JobsDetailDto detail, String userName, Cluster cluster) throws Exception {
    	//check param
    	if(detail != null && !StringUtils.isEmpty(detail.getName()) && detail.getContainers() != null){
    		//获取job
    		K8SClientResponse jobRes = jobService.getJob(detail.getNamespace(), detail.getName(), null, cluster);
    		if (!HttpStatusUtil.isSuccessStatus(jobRes.getStatus())) {
    			return ActionReturnUtil.returnErrorWithMsg(jobRes.getBody());
    		}
    		Job job = JsonUtil.jsonToPojo(jobRes.getBody(), Job.class);
    		//删除configMap
            Map<String, Object> queryP = new HashMap<>();
            queryP.put("labelSelector", Constant.TYPE_JOB + "=" + detail.getName());
            K8SClientResponse conRes = configmapService.doSepcifyConfigmap(detail.getNamespace(), queryP, HTTPMethod.DELETE, cluster);
            if (!HttpStatusUtil.isSuccessStatus(conRes.getStatus()) && conRes.getStatus() != Constant.HTTP_404) {
                UnversionedStatus status = JsonUtil.jsonToPojo(conRes.getBody(), UnversionedStatus.class);
                return ActionReturnUtil.returnErrorWithMsg(status.getMessage());
            }

            //delete pvc
            K8SClientResponse pvcRes = pvcService.doSepcifyPVC(detail.getNamespace(), queryP, HTTPMethod.DELETE, cluster);
            if (!HttpStatusUtil.isSuccessStatus(pvcRes.getStatus()) && pvcRes.getStatus() != Constant.HTTP_404) {
                UnversionedStatus status = JsonUtil.jsonToPojo(pvcRes.getBody(), UnversionedStatus.class);
                return ActionReturnUtil.returnErrorWithMsg(status.getMessage());
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
                        for (CreateVolumeDto pvc : c.getStorage()) {
                        	if(pvc.getType() != null && Constant.VOLUME_TYPE_PV.equals(pvc.getType())){
                        		if (pvc.getPvcName() == "" || pvc.getPvcName() == null) {
                                    continue;
                                }
                        		volumeSerivce.createVolume(detail.getNamespace(), pvc.getPvcName(), pvc.getPvcCapacity(), pvc.getPvcTenantid(), pvc.getReadOnly(), pvc.getPvcBindOne(), pvc.getVolume(), Constant.TYPE_JOB, detail.getName());
                        	}
                        }
                    }
        		}
        	}
        	Job newjob = convertJob(job, detail, userName);
        	return jobService.updateJob(detail.getNamespace(), detail.getName(), newjob, cluster);
    	}else{
    		return ActionReturnUtil.returnErrorWithMsg("job为空");
    	}
    }

    @Override
    public ActionReturnUtil deleteJob(String name, String namespace, String userName, Cluster cluster) throws Exception {

        //delete job
        ActionReturnUtil deleteJob = jobService.delJobByName(name,namespace,cluster);
        if (!deleteJob.isSuccess()){
            return deleteJob;
        }

        //delete configmap
        Map<String, Object> queryP = new HashMap<>();
        queryP.put("labelSelector", Constant.TYPE_JOB + "=" + name);
        K8SClientResponse conRes = configmapService.doSepcifyConfigmap(namespace, queryP, HTTPMethod.DELETE, cluster);
        if (!HttpStatusUtil.isSuccessStatus(conRes.getStatus()) && conRes.getStatus() != Constant.HTTP_404) {
            UnversionedStatus status = JsonUtil.jsonToPojo(conRes.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithMsg(status.getMessage());
        }

        //delete pvc
        K8SClientResponse pvcRes = pvcService.doSepcifyPVC(namespace, queryP, HTTPMethod.DELETE, cluster);
        if (!HttpStatusUtil.isSuccessStatus(pvcRes.getStatus()) && pvcRes.getStatus() != Constant.HTTP_404) {
            UnversionedStatus status = JsonUtil.jsonToPojo(pvcRes.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithMsg(status.getMessage());
        }

        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public ActionReturnUtil reRunJob(String name, String namespace, String userName, Cluster cluster) throws Exception {
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
    			return ActionReturnUtil.returnErrorWithMsg(status.getMessage());
    		}
    		Job job = JsonUtil.jsonToPojo(jobRes.getBody(),Job.class);
    		
    		Map<String, Object> label = new HashMap<String, Object>();
    		label.put("labelSelector", Constant.TYPE_JOB+"="+name);
    		//获取config map
    		K8SClientResponse cmRes = configmapService.doSepcifyConfigmap(namespace, label, HTTPMethod.GET, cluster);
    		boolean cmboo = true;
    		if(cmRes.getStatus() == Constant.HTTP_404){
    			cmboo = false;
    		}
    		
    		if(!HttpStatusUtil.isSuccessStatus(cmRes.getStatus()) && cmRes.getStatus() != Constant.HTTP_404){
    			UnversionedStatus status = JsonUtil.jsonToPojo(cmRes.getBody(),UnversionedStatus.class);
    			return ActionReturnUtil.returnErrorWithMsg(status.getMessage());
    		}
			//获取pvc
    		K8SClientResponse pvcRes = pvcService.doSepcifyPVC(namespace, label, HTTPMethod.GET, cluster);
    		boolean pvcboo = true;
    		if(pvcRes.getStatus() == Constant.HTTP_404){
    			pvcboo = false;
    		}
    		if(!HttpStatusUtil.isSuccessStatus(pvcRes.getStatus()) && pvcRes.getStatus() != Constant.HTTP_404){
    			UnversionedStatus status = JsonUtil.jsonToPojo(pvcRes.getBody(),UnversionedStatus.class);
    			return ActionReturnUtil.returnErrorWithMsg(status.getMessage());
    			
    		}
    		
    		if(job != null){
    			//删除已有的job
    			ActionReturnUtil delRes=deleteJob(name, namespace, userName, cluster);
    			if(!delRes.isSuccess()){
    				return delRes;
    			}
    			//更改状态
    			if(job.getMetadata().getAnnotations() != null){
    				Map<String, Object> anno = (Map<String, Object>) job.getMetadata().getAnnotations();
						anno.put("nephele/status", Constant.STARTING);
						job.getMetadata().setAnnotations(anno);
    			}else{
    				Map<String, Object> anno = new HashMap<String, Object>();
    				anno.put("nephele/user", userName);
    				anno.put("nephele/status", Constant.STARTING);
    				anno.put("nephele/parallelism", job.getSpec().getParallelism());
    				job.getMetadata().setAnnotations(anno);
    			}
    			//label
    			if(job.getMetadata().getLabels() != null){
    				Map<String, Object> lmMap = (Map<String, Object>) job.getMetadata().getLabels();
    				lmMap.put("nephele/user", userName);
        			lmMap.put(Constant.TYPE_JOB, name);
        			job.getMetadata().setLabels(lmMap);
    			}else{
    				Map<String, Object> lmMap = new HashMap<String, Object>();
        			lmMap.put("nephele/user", userName);
        			lmMap.put(Constant.TYPE_JOB, name);
        			job.getMetadata().setLabels(lmMap);
    			}
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
        							return ActionReturnUtil.returnErrorWithMsg(status.getMessage());
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
        							return ActionReturnUtil.returnErrorWithMsg(status.getMessage());
        						}
        					}
        				}
        			}
    			}
    			//创建job
    			jobService.addJob(job, cluster);
    		}else{
    			return ActionReturnUtil.returnErrorWithMsg("job:name已不存在");
    		}
    	}else{
    		return ActionReturnUtil.returnErrorWithMsg(sb.toString());
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
			String name, Cluster cluster, String type, String businessName) throws Exception {
		if (configMaps != null && configMaps.size() > 0) {
			K8SURL url = new K8SURL();
			url.setNamespace(namespace).setResource(Resource.CONFIGMAP);
			Map<String, Object> bodys = new HashMap<String, Object>();
			Map<String, Object> meta = new HashMap<String, Object>();
			meta.put("namespace", namespace);
			meta.put("name", name + containerName);
			Map<String, Object> label = new HashMap<String, Object>();
			label.put(type, name);
			if (!StringUtils.isEmpty(businessName)){
				label.put("business",businessName);
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
			K8SClientResponse response = new K8SClient().doit(url, HTTPMethod.POST, headers, bodys, cluster);
			if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
				UnversionedStatus status = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
				return ActionReturnUtil.returnErrorWithMsg(status.getMessage());
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
		anno.put("nephele/parallelism", detail.getParallelism());
		anno.put("nephele/labels", detail.getLabels() == null ? "" : detail.getLabels());
		meta.setAnnotations(anno);
		job.setMetadata(meta);
		JobSpec jobSpec = new JobSpec();
		if(detail.getActiveDeadlineSeconds() != 0){
			jobSpec.setActiveDeadlineSeconds(detail.getActiveDeadlineSeconds());
		}
		jobSpec.setCompletions(detail.getCompletions() == 0 ? 1 : detail.getCompletions());
		PodTemplateSpec template = K8sResultConvert.convertPodTemplate(detail.getName(), detail.getContainers(), detail.getLabels(), detail.getAnnotation(), userName, Constant.TYPE_JOB, detail.getNodeSelector(), detail.getRestartPolicy());
		jobSpec.setTemplate(template);
		job.setSpec(jobSpec);
    	return job ;
    }
    
    private Job convertJob(Job job, JobsDetailDto detail, String userName) throws Exception{
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
		anno.put("nephele/parallelism", detail.getParallelism());
		anno.put("nephele/labels", detail.getLabels() == null ? "" : detail.getLabels());
		meta.setAnnotations(anno);
		job.setMetadata(meta);
		JobSpec jobSpec = job.getSpec();
		if(detail.getActiveDeadlineSeconds() != 0){
			jobSpec.setActiveDeadlineSeconds(detail.getActiveDeadlineSeconds());
		}
		jobSpec.setCompletions(detail.getCompletions() == 0 ? 1 : detail.getCompletions());
		PodTemplateSpec template = K8sResultConvert.convertPodTemplate(detail.getName(), detail.getContainers(), detail.getLabels(), detail.getAnnotation(), userName, Constant.TYPE_JOB, detail.getNodeSelector(), detail.getRestartPolicy());
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
					status = Constant.JOB_SUCCEEDED;
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
					status = Constant.JOB_SUCCEEDED;
				} else if (job.getStatus().getActive() != null && job.getStatus().getActive() >0){
					status = Constant.RUNNING;
				}else if(job.getStatus().getFailed() != null && job.getStatus().getFailed() >0){
					status = Constant.JOB_FAILED;
				}
				break;
			}
		} else {
			if (job.getStatus().getSucceeded() != null&&job.getStatus().getSucceeded() > 0) {
				status = Constant.JOB_SUCCEEDED;
			} else if (job.getStatus().getActive() != null && job.getStatus().getActive() >0){
				status = Constant.RUNNING;
			}else if(job.getStatus().getFailed() != null && job.getStatus().getFailed() >0){
				status = Constant.JOB_FAILED;
			}
		}
    	return status;
    }
}
