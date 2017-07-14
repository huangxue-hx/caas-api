package com.harmonycloud.service.application.impl;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.common.util.StringUtil;
import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.dto.business.CreateConfigMapDto;
import com.harmonycloud.dto.business.CreateContainerDto;
import com.harmonycloud.dto.business.CreateVolumeDto;
import com.harmonycloud.dto.business.JobsDetailDto;
import com.harmonycloud.k8s.bean.*;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.service.JobService;
import com.harmonycloud.k8s.service.PodService;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import com.harmonycloud.service.application.JobsService;
import com.harmonycloud.service.application.VolumeSerivce;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.platform.convert.K8sResultConvert;
import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    	
        return null;
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
        bodys.put("job",job);
        bodys.put("podlist",podList);

        return ActionReturnUtil.returnSuccessWithData(bodys);
    }

    @Override
    public ActionReturnUtil startJob(String name, String namespace, String userName, Cluster cluster) throws Exception {
        return null;
    }

    @Override
    public ActionReturnUtil stopJob(String name, String namespace, String userName, Cluster cluster) throws Exception {
        return null;
    }

    @Override
    public ActionReturnUtil replaceJob(JobsDetailDto detail, String userName, Cluster cluster) throws Exception {
        return null;
    }

    @Override
    public ActionReturnUtil deleteJob(String name, String namespace, String userName, Cluster cluster) throws Exception {

        //delete job
        ActionReturnUtil deleteJob = jobService.delJobByName(name,namespace,cluster);
        if (!deleteJob.isSuccess()){
            return deleteJob;
        }

        //delete configmap
        K8SURL cUrl = new K8SURL();
        cUrl.setNamespace(namespace).setResource(Resource.CONFIGMAP);
        Map<String, Object> queryP = new HashMap<>();
        queryP.put("labelSelector", "jobs=" + name);
        cUrl.setQueryParams(queryP);
        K8SClientResponse conRes = new K8SClient().doit(cUrl, HTTPMethod.DELETE, null, null,null);
        if (!HttpStatusUtil.isSuccessStatus(conRes.getStatus()) && conRes.getStatus() != Constant.HTTP_404) {
            UnversionedStatus status = JsonUtil.jsonToPojo(conRes.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithMsg(status.getMessage());
        }

        //delete pvc
        K8SURL pvcUrl = new K8SURL();
        pvcUrl.setNamespace(namespace).setResource(Resource.PERSISTENTVOLUMECLAIM);
        Map<String, Object> queryPvc = new HashMap<>();
        queryPvc.put("labelSelector", "jobs=" + name);
        pvcUrl.setQueryParams(queryPvc);
        K8SClientResponse pvcRes = new K8SClient().doit(pvcUrl, HTTPMethod.DELETE, null, null,null);
        if (!HttpStatusUtil.isSuccessStatus(pvcRes.getStatus()) && pvcRes.getStatus() != Constant.HTTP_404) {
            UnversionedStatus status = JsonUtil.jsonToPojo(pvcRes.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithMsg(status.getMessage());
        }

        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public ActionReturnUtil reRunJob(String name, String namespace, String userName, Cluster cluster) throws Exception {
        return null;
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
    	return null ;
    }
}
