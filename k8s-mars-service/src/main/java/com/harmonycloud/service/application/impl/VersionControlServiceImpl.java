package com.harmonycloud.service.application.impl;

import com.harmonycloud.common.util.*;
import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.dao.cluster.bean.RollbackBean;
import com.harmonycloud.dto.business.*;
import com.harmonycloud.k8s.bean.*;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.service.*;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import com.harmonycloud.k8s.util.RandomNum;
import com.harmonycloud.service.application.VersionControlService;
import com.harmonycloud.service.application.VolumeSerivce;
import com.harmonycloud.service.platform.bean.*;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.platform.convert.K8sResultConvert;
import com.harmonycloud.service.platform.dto.PodDto;
import com.harmonycloud.service.platform.dto.ReplicaSetDto;
import com.harmonycloud.service.platform.service.WatchService;

import com.alibaba.fastjson.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by czm on 2017/4/26.
 */

@Service
public class VersionControlServiceImpl implements VersionControlService {


    @Autowired
    DeploymentService dpService;

    @Autowired
    ServicesService sService;

    @Autowired
    com.harmonycloud.service.application.ServiceService serviceService;

    @Autowired
    WatchService watchService;

    @Autowired
    PodService podService;

    @Autowired
    private VolumeSerivce volumeSerivce;

    @Autowired
    ReplicasetsService rsService;

    @Autowired
    private PvService pvService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("#{propertiesReader['image.url']}")
    private String harborUrl;

    //更新Deployment的时候不断查询状态
    public ActionReturnUtil canaryUpdate(CanaryDeployment detail, int instances, String userName, Cluster cluster) throws Exception {
        CountDownLatch mCountDownLatch = new CountDownLatch(1);

        final RequestAttributes request = RequestContextHolder.getRequestAttributes();

        // 获取deployment
        K8SClientResponse depRes = dpService.doSpecifyDeployment(detail.getNamespace(), detail.getName(), null, null,
                HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus())) {
        	UnversionedStatus status = JsonUtil.jsonToPojo(depRes.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithMsg(status.getMessage());
        }
        Deployment dep = JsonUtil.jsonToPojo(depRes.getBody(), Deployment.class);

        K8SClientResponse rsRes = sService.doSepcifyService(detail.getNamespace(),
                detail.getName(), null, null, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(rsRes.getStatus())) {
        	UnversionedStatus status = JsonUtil.jsonToPojo(rsRes.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithMsg(status.getMessage());
        }

        com.harmonycloud.k8s.bean.Service service = JsonUtil.jsonToPojo(rsRes.getBody(), com.harmonycloud.k8s.bean.Service.class);

        //从数据库获取service的pvc信息
        com.harmonycloud.dao.application.bean.Service svc = serviceService.getServiceByname(detail.getName(), detail.getNamespace());
        JSONArray pvclist =new JSONArray();
        if(svc != null && svc.getPvc() != null){
            pvclist = JSONArray.fromObject(svc.getPvc());
        }
        JSONArray pvclistnew=JSONArray.fromObject(pvclist);
        if(pvclist != null && !pvclist.isEmpty()){
            if(detail.getContainers() != null){
                for(UpdateContainer container : detail.getContainers()){
                    if(container.getStorage() !=null && container.getStorage().size() > 0){
                        for(UpdateVolume pv : container.getStorage()){
                            if(Constant.VOLUME_TYPE_PV.equals(pv.getType())){
                                boolean flag = true;
                                for(int i=0 ; i < pvclist.size(); i++){
                                    String pvc = (String) pvclist.get(i);
                                    if(pvc.equals(pv.getPvcName())){
                                        flag = false;
                                    }
                                }
                                if (flag){
                                	ActionReturnUtil pvRes = volumeSerivce.createVolume(detail.getNamespace(), pv.getPvcName(), pv.getPvcCapacity(), pv.getPvcTenantid(), pv.getReadOnly(), pv.getPvcBindOne(),
                                            pv.getName());
                                	if(!pvRes.isSuccess()){
                                		return pvRes;
                                	}
                                    pvclistnew.add(pv.getPvcName());
                                }
                            }
                        }
                        for(int i=0 ; i < pvclist.size(); i++){
                            boolean flag =  true;
                            for(UpdateVolume pv : container.getStorage()){
                                String pvc = (String) pvclist.get(i);
                                if(pvc.equals(pv.getPvcName())){
                                    flag = false;
                                }
                            }
                            if (flag){
                                //volumeSerivce.deleteVolume(detail.getNamespace(),(String) pvclist.get(i));

                                String pvc = (String) pvclist.get(i);
                                K8SURL url = new K8SURL();
                                url.setName((String) pvclist.get(i)).setNamespace(detail.getNamespace()).setResource(Resource.PERSISTENTVOLUMECLAIM);
                                Map<String, Object> headers = new HashMap<>();
                                headers.put("Content-Type", "application/json");
                                Map<String, Object> bodys = new HashMap<>();
                                bodys.put("gracePeriodSeconds", 1);
                                K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.DELETE, headers, bodys,cluster);
                                if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
                                    // update pv
                                    if (pvc.contains(Constant.PVC_BREAK)) {
                                        String [] str=pvc.split(Constant.PVC_BREAK);
                                        String pvname = str[0];
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
                                            K8SClientResponse responsePV = new K8sMachineClient().exec(urlPV, HTTPMethod.PUT, headersPV, bodysPV,cluster);
                                            if (!HttpStatusUtil.isSuccessStatus(responsePV.getStatus()) && responsePV.getStatus() != Constant.HTTP_404) {
                                            	UnversionedStatus status = JsonUtil.jsonToPojo(responsePV.getBody(), UnversionedStatus.class);
                                            	return ActionReturnUtil.returnSuccessWithMsg(status.getMessage());
                                            }
                                        }
                                    }
                                }else{
                                	UnversionedStatus status = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
                                	return ActionReturnUtil.returnSuccessWithMsg(status.getMessage());
                                }
                            }
                        }


                    }
                }
            }
        }else{
            if(detail.getContainers() != null){
                for(UpdateContainer container : detail.getContainers()){
                    if(container.getStorage() !=null && container.getStorage().size() > 0){
                        for (UpdateVolume pv : container.getStorage()) {
                            if (Constant.VOLUME_TYPE_PV.equals(pv.getType())) {
                                volumeSerivce.createVolume(detail.getNamespace(), pv.getPvcName(), pv.getPvcCapacity(),
                                        pv.getPvcTenantid(), pv.getReadOnly(), pv.getPvcBindOne(), pv.getName());
                                pvclistnew = new JSONArray();
                                pvclistnew.add(pv.getPvcName());
                            }
                        }
                    }
                }
            }
        }
        serviceService.updateServicePvcByname(detail.getName(), pvclistnew.toString(), detail.getNamespace());

        //在这里创建configmap,在convertAppPut当中增加Deployment的注解,返回的是容器和configmap之间的映射关系列表
        Map<String, String> containerToConfigMap = createConfigmaps(detail, cluster);

        Map<String, Object> res = myConvertAppPut(dep, service, detail, detail.getName(), containerToConfigMap);

        //设置灰度升级相关的参数
        Deployment deped = (Deployment) res.get("dep");
        DeploymentStrategy strategy =new DeploymentStrategy();
        strategy.setType("RollingUpdate");
        //当实例数目不为0时才需要灰度更新
        if (detail.getInstances() != 0) {
            if (detail.getSeconds() < 5) {
                deped.getSpec().setMinReadySeconds(5);
            } else {
                deped.getSpec().setMinReadySeconds(detail.getSeconds());
            }
            RollingUpdateDeployment ru =new RollingUpdateDeployment();
            if(detail.getMaxSurge() == 0 && detail.getMaxUnavailable() == 0){
            	ru.setMaxSurge(1);
                ru.setMaxUnavailable(0);
            }else{
            	ru.setMaxSurge(detail.getMaxSurge());
                ru.setMaxUnavailable(detail.getMaxUnavailable());
            }
            strategy.setRollingUpdate(ru);
            deped.getSpec().setStrategy(strategy);;
        }

        deped.getSpec().setPaused(false);
        Map<String, Object> bodys = CollectionUtil.transBean2Map(deped);
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        ESFactory.executor.execute((new Runnable() {
            @Override
            public void run() {
                try {
                    RequestContextHolder.setRequestAttributes(request);

                    for (; ; ) {
                        //如果实例数为0则不需要灰度更新
                        if (detail.getInstances() == 0) {
                            break;
                        }
                        //暂停两秒钟等待灰度升级开始
                        Thread.sleep(2000);

                        K8SClientResponse dp = dpService.doSpecifyDeployment(detail.getNamespace(), detail.getName(), null, null, HTTPMethod.GET, cluster);
                        if (!HttpStatusUtil.isSuccessStatus(dp.getStatus())) {
                            logger.error("灰度升级查询Deployment报错");
                        }
                        Deployment dep1 = JsonUtil.jsonToPojo(dp.getBody(), Deployment.class);
                        if (dep1.getStatus() != null) {
                            System.out.println("更新的实例数量..." + dep1.getStatus().getUpdatedReplicas());
                        }

                        //如果实例数量是副本数的话就说明没有更新必要
                        if (dep1.getStatus() != null && dep1.getStatus().getUpdatedReplicas() != null && dep1.getStatus().getUpdatedReplicas() == dep1.getSpec().getReplicas()) {
                            break;
                        }
                        //升级达到指定个数
                        if (dep1.getStatus() != null && dep1.getStatus().getUpdatedReplicas() != null && dep1.getStatus().getUpdatedReplicas() == instances) {
                            //暂停升级参数设置
                            if (dep1.getSpec() == null) {
                                continue;
                            }
                            dep1.getSpec().setPaused(true);

                            Map<String, Object> bodys = CollectionUtil.transBean2Map(dep1);

                            //暂停升级
                            Map<String, Object> headers = new HashMap<>();
                            headers.put("Content-Type", "application/json");
                            K8SClientResponse pauseRes = dpService.doSpecifyDeployment(detail.getNamespace(), detail.getName(), headers, bodys, HTTPMethod.PUT, cluster);

                            if (!HttpStatusUtil.isSuccessStatus(pauseRes.getStatus())) {
                                logger.error("灰度升级暂停Deployment报错");
                            }
                            break;
                        }else if(dep1.getStatus() != null && dep1.getStatus().getReplicas() != null && dep1.getStatus().getReplicas() == instances){
                        	break;
                        }
                    }
                    mCountDownLatch.countDown();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }));


        //触发灰度升级
        K8SClientResponse putRes = dpService.doSpecifyDeployment(detail.getNamespace(), detail.getName(), headers,
                bodys, HTTPMethod.PUT, cluster);
        if (!HttpStatusUtil.isSuccessStatus(putRes.getStatus())) {
            logger.error("触发灰度升级失败");
            UnversionedStatus status = JsonUtil.jsonToPojo(putRes.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithMsg(status.getMessage());
        }

        //在这里阻塞线程，等待子线程唤醒
        mCountDownLatch.await();

        //更新service端口
        increaseServiceByDeployment(deped, cluster);

        return ActionReturnUtil.returnSuccessWithData("success");

    }


    public ActionReturnUtil getUpdateStatus(String namespace, String name, Cluster cluster) throws Exception {
        // 获取deployment
        K8SClientResponse depRes = dpService.doSpecifyDeployment(namespace, name, null, null,
                HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus())) {
            logger.error("获取灰度升级进程:获得进度出错");
            return ActionReturnUtil.returnErrorWithMsg(depRes.getBody());
        }
        Deployment dep = JsonUtil.jsonToPojo(depRes.getBody(), Deployment.class);

        //返回当前列表中新老实例,格式,顺序为:新实例,老实例,总共实例,当新的实例等于总的实例的时候终止前端定时器
        JSONObject json = new JSONObject();
        List<Integer> counts = new ArrayList<>();
        if("RollingUpdate".equals(dep.getSpec().getStrategy().getType())){
        	Integer updateCounts = 0;
        	if(dep.getStatus().getUpdatedReplicas() != null && dep.getStatus().getUpdatedReplicas() != 0){
        		updateCounts = dep.getStatus().getUpdatedReplicas();
        		counts.add(updateCounts);
        		counts.add(dep.getSpec().getReplicas() - updateCounts);
        	}else{
        		int unavailableReplicas = dep.getStatus().getUnavailableReplicas();
        		updateCounts = dep.getSpec().getReplicas() - unavailableReplicas;
        		if(updateCounts < 0){
        			updateCounts = 0;
        		}
        		counts.add(updateCounts);
        		counts.add(unavailableReplicas);
        	}
        	/*Integer updateCounts = dep.getStatus().getUpdatedReplicas();
            updateCounts = updateCounts == null ? 0 : updateCounts;*/
            
            //counts.add(dep.getSpec().getReplicas() - updateCounts);
        }else{
        	Integer updateCounts = 0;
        	if(dep.getStatus().getUpdatedReplicas() != null && dep.getStatus().getUpdatedReplicas() != 0){
        		updateCounts = dep.getStatus().getUpdatedReplicas();
        		counts.add(updateCounts);
        		counts.add(0);
        	}else{
        		int unavailableReplicas = dep.getStatus().getUnavailableReplicas();
        		updateCounts = dep.getSpec().getReplicas() - unavailableReplicas;
        		if(updateCounts < 0){
        			updateCounts = 0;
        		}
        		counts.add(updateCounts);
        		counts.add(0);
        	}
        	/*Integer updateCounts = dep.getStatus().getUpdatedReplicas();
        	counts.add(updateCounts);
            counts.add(0);*/
        }
        json.put("counts", counts);
        json.put("message", dep.getStatus().getConditions());
        return ActionReturnUtil.returnSuccessWithData(json);
    }


    public ActionReturnUtil resumeCanaryUpdate(String namespace, String name , Cluster cluster) throws Exception {
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        K8SClientResponse dp = dpService.doSpecifyDeployment(namespace, name, null, null, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(dp.getStatus())) {
            logger.error("恢复灰度升级,获取Deployment出错");
            return ActionReturnUtil.returnErrorWithMsg(dp.getBody());
        }
        Deployment dep = JsonUtil.jsonToPojo(dp.getBody(), Deployment.class);

        dep.getSpec().setPaused(false);
        dep.getSpec().setMinReadySeconds(0);

        Map<String, Object> bodys = CollectionUtil.transBean2Map(dep);

        K8SClientResponse dpPut = dpService.doSpecifyDeployment(namespace, name, headers, bodys, HTTPMethod.PUT, cluster);
        if (!HttpStatusUtil.isSuccessStatus(dpPut.getStatus())) {
            logger.error("恢复灰度升级,更新Deployment出错");
            UnversionedStatus status = JsonUtil.jsonToPojo(dpPut.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithMsg(status.getMessage());
        }

        K8SClientResponse dpUpdated = dpService.doSpecifyDeployment(namespace, name, null, null, HTTPMethod.GET, cluster);

        if (!HttpStatusUtil.isSuccessStatus(dpUpdated.getStatus())) {
            logger.error("恢复灰度升级,获得Deployment出错");
            UnversionedStatus status = JsonUtil.jsonToPojo(dpUpdated.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithMsg(status.getMessage());
        }
        Deployment depUpdated = JsonUtil.jsonToPojo(dpUpdated.getBody(), Deployment.class);

        updateServiceByDeployment(depUpdated, cluster);

        return ActionReturnUtil.returnSuccess();
    }


    public ActionReturnUtil pauseCanaryUpdate(String namespace, String name, Cluster cluster) throws Exception {

        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        K8SClientResponse dp = dpService.doSpecifyDeployment(namespace, name, null, null, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(dp.getStatus())) {
            logger.error("暂停灰度升级,获取Deployment出错");
            UnversionedStatus status = JsonUtil.jsonToPojo(dp.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithMsg(status.getMessage());
        }

        Deployment dep = JsonUtil.jsonToPojo(dp.getBody(), Deployment.class);
        dep.getSpec().setPaused(true);
        Map<String, Object> bodys = CollectionUtil.transBean2Map(dep);

        K8SClientResponse dpUpdated = dpService.doSpecifyDeployment(namespace, name, headers, bodys, HTTPMethod.PUT,cluster);
        if (!HttpStatusUtil.isSuccessStatus(dpUpdated.getStatus())) {
            logger.error("暂停灰度升级,更新Deployment出错");
            UnversionedStatus status = JsonUtil.jsonToPojo(dpUpdated.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithMsg(status.getMessage());
        }
        return ActionReturnUtil.returnSuccess();

    }


    public ActionReturnUtil cancelCanaryUpdate(String namespace, String name, Cluster cluster) throws Exception {

        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        K8SClientResponse dp = dpService.doSpecifyDeployment(namespace, name, null, null, HTTPMethod.GET, cluster);

        Deployment dep = JsonUtil.jsonToPojo(dp.getBody(), Deployment.class);

        RollbackConfig rollbackConfig = new RollbackConfig();
        //将参数设置成0自动回滚到最新的版本
        rollbackConfig.setRevision(0);

        dep.getSpec().setRollbackTo(rollbackConfig);
        dep.getSpec().setPaused(false);
        dep.getSpec().setMinReadySeconds(0);
        Map<String, Object> bodys = CollectionUtil.transBean2Map(dep);

        dpService.doSpecifyDeployment(namespace, name, headers, bodys, HTTPMethod.PUT, cluster);

        //拿到最新的Dp,更新Deployment
        K8SClientResponse dpUpdated = dpService.doSpecifyDeployment(namespace, name, null, null, HTTPMethod.GET,cluster);
        Deployment depUpdated = JsonUtil.jsonToPojo(dpUpdated.getBody(), Deployment.class);
        updateServiceByDeployment(depUpdated, cluster);

        return ActionReturnUtil.returnSuccess();

    }


    private Map<String, Object> getRevisionDetail(String namespace, String name, String revision, Cluster cluster) throws Exception {

        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        Map<String, Object> res = new HashedMap();
        //通过获得Deployment的RS来显示版本信息
        //查询出来Deployment
        K8SClientResponse dp = dpService.doSpecifyDeployment(namespace, name, null, null, HTTPMethod.GET,cluster);

        Deployment dep = JsonUtil.jsonToPojo(dp.getBody(), Deployment.class);

        Map<String, Object> query = dep.getSpec().getSelector().getMatchLabels();

        Map<String, Object> body = new HashedMap();
        body.put("labelSelector", "app=" + query.get("app"));
        System.out.println("我是label" + "app=" + query.get("app"));

        //根据label查询出来所有的RS
//        K8SClientResponse rsRes = rsService.doRsByNamespace(namespace, null, query, null, HTTPMethod.GET);
        K8SClientResponse rsRes = rsService.doRsByNamespace(namespace, headers, body, HTTPMethod.GET,cluster);
        if (!HttpStatusUtil.isSuccessStatus(rsRes.getStatus())) {
            return ActionReturnUtil.returnErrorWithMsg(rsRes.getBody());
        }
        ReplicaSetList rSetList = JsonUtil.jsonToPojo(rsRes.getBody(), ReplicaSetList.class);

        for (ReplicaSet rs : rSetList.getItems()) {
            if (revision.equals(rs.getMetadata().getAnnotations().get("deployment.kubernetes.io/revision"))) {
                res.put("podTemplete", JSON.toJSONString(rs.getSpec().getTemplate()));
                res.put("revisionTime", rs.getMetadata().getCreationTimestamp());
                res.put("name", rs.getMetadata().getName());
                res.put("current", "false");

                if (revision.equals(dep.getMetadata().getAnnotations().get("deployment.kubernetes.io/revision"))) {
                    res.put("current", "true");
                }
            }


        }
        return res;
    }


    private Set<String> listReversions(String namespace, String name, Cluster cluster) throws Exception {

        //查询出来Deployment
        K8SClientResponse dp = dpService.doSpecifyDeployment(namespace, name, null, null, HTTPMethod.GET,cluster);

        Deployment dep = JsonUtil.jsonToPojo(dp.getBody(), Deployment.class);

        Map<String, Object> queryMap = dep.getSpec().getSelector().getMatchLabels();
        StringBuilder queryValue = new StringBuilder();
        int i = 0;
        //遍历拼装query
        for (String key : queryMap.keySet()) {
            queryValue.append(key + "=" + queryMap.get(key));
            if (i < queryMap.keySet().size() - 1) {
                queryValue.append(",");
            }
            i++;
        }

        System.out.println(queryValue.toString());
        Map<String, Object> query = new HashedMap();
        query.put("labelSelector", queryValue);


        //根据label查询出来所有的RS
        K8SClientResponse rsRes = rsService.doRsByNamespace(namespace, null, query, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(rsRes.getStatus())) {
            return null;
        }
        ReplicaSetList rSetList = JsonUtil.jsonToPojo(rsRes.getBody(), ReplicaSetList.class);

        Set<String> res = new HashSet<>();

        for (ReplicaSet rs : rSetList.getItems()) {
            res.add(rs.getMetadata().getAnnotations().get("deployment.kubernetes.io/revision").toString());
        }

        return res;
    }



    public ActionReturnUtil listRevisionAndDetails(String namespace, String name, Cluster cluster) throws Exception {

        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        //通过获得Deployment的RS来显示版本信息
        //查询出来Deployment
        K8SClientResponse dp = dpService.doSpecifyDeployment(namespace, name, null, null, HTTPMethod.GET,cluster);

        Deployment dep = JsonUtil.jsonToPojo(dp.getBody(), Deployment.class);

        Map<String, Object> query = dep.getSpec().getSelector().getMatchLabels();

        Map<String, Object> body = new HashMap<String, Object>();
        body.put("labelSelector", "app=" + query.get("app"));

        K8SClientResponse rsRes = rsService.doRsByNamespace(namespace, headers, body, HTTPMethod.GET,cluster);
        if (!HttpStatusUtil.isSuccessStatus(rsRes.getStatus())) {
            return ActionReturnUtil.returnErrorWithMsg(rsRes.getBody());
        }
        List<RollbackBean> reversions = new ArrayList<RollbackBean>();
        ReplicaSetList rSetList = JsonUtil.jsonToPojo(rsRes.getBody(), ReplicaSetList.class);

        for (ReplicaSet rs : rSetList.getItems()) {
            String reversion = rs.getMetadata().getAnnotations().get("deployment.kubernetes.io/revision").toString();
            RollbackBean rollbackBean = new RollbackBean();
            rollbackBean.setName(rs.getMetadata().getName());
            rollbackBean.setRevisionTime(rs.getMetadata().getCreationTimestamp());
            rollbackBean.setPodTemplete(JSON.toJSONString(rs.getSpec().getTemplate()));
            if(rs.getSpec().getTemplate().getSpec().getVolumes() != null){
            	List<Volume> volume = rs.getSpec().getTemplate().getSpec().getVolumes();
            	if(volume != null && volume.size() > 0){
            		List<String> cfgmap = new ArrayList<String>();
            		for(Volume v : volume){
            			if(v.getConfigMap() != null){
            				cfgmap.add(v.getConfigMap().getName());
            			}
            		}
            		rollbackBean.setConfigmap(cfgmap);
            	}
            }
            rollbackBean.setCurrent("false");
            rollbackBean.setRevision(reversion);
            if (reversion.equals(dep.getMetadata().getAnnotations().get("deployment.kubernetes.io/revision"))) {
                rollbackBean.setCurrent("true");
            }

            reversions.add(rollbackBean);
        }

        Collections.sort(reversions);

        return ActionReturnUtil.returnSuccessWithData(reversions);
    }


    public ActionReturnUtil canaryRollback(String namespace, String name, String revision, Cluster cluster) throws Exception {

        //查询出来最新的版本然后进行回滚
        K8SClientResponse dp = dpService.doSpecifyDeployment(namespace, name, null, null, HTTPMethod.GET, cluster);

        Deployment dep = JsonUtil.jsonToPojo(dp.getBody(), Deployment.class);

        RollbackConfig rollbackConfig = new RollbackConfig();
        rollbackConfig.setRevision(Integer.parseInt(revision));
        dep.getSpec().setRollbackTo(rollbackConfig);
        dep.getSpec().setPaused(false);
        dep.getSpec().setMinReadySeconds(0);

        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        Map<String, Object> bodys = CollectionUtil.transBean2Map(dep);

        dpService.doSpecifyDeployment(namespace, name, headers, bodys, HTTPMethod.PUT,cluster);


        //在这里对比新旧Deployment之间挂载卷的区别,创建新的pvc
        //

        K8SClientResponse dpUpdated = dpService.doSpecifyDeployment(namespace, name, null, null, HTTPMethod.GET,cluster);

        if (!HttpStatusUtil.isSuccessStatus(dpUpdated.getStatus())) {
            return ActionReturnUtil.returnErrorWithMsg(dpUpdated.getBody());
        }

        Deployment depUpdated = JsonUtil.jsonToPojo(dpUpdated.getBody(), Deployment.class);

        updateServiceByDeployment(depUpdated, cluster);

        return ActionReturnUtil.returnSuccess();
    }

    @SuppressWarnings("unused")
    private List<String> createPVC(CanaryDeployment dep, Deployment oldDep) throws Exception {
        //这里需要做的是针对已经有有的就不用创建PVC了,针对删除的就需要找到缺少的然后删除pvc
        //最关键的问题是区分哪些是新增的那些是删除的
        List<ContainerOfPodDetail> containerOfPodDetails = K8sResultConvert.convertContainer(oldDep);


        List<UpdateVolume> newPvcList = new ArrayList();
        List<VolumeMountExt> oldPvcList = new ArrayList();
        //获取新的pvc列表
        for (UpdateContainer c : dep.getContainers()) {
            if(c.getStorage()!=null){
                newPvcList.addAll(c.getStorage());
            }

        }
        //获取旧的pvc列表
        for (ContainerOfPodDetail v : containerOfPodDetails) {
            //怎么从Deployment中拿到pv相关的storage
            if (v.getStorage() != null) {

                for (VolumeMountExt vme : v.getStorage()) {
                    if (vme.getType().equals("pv")) {
                        oldPvcList.add(vme);
                    }
                }
            }
        }


        //遍历旧的,如果新的列表里面没有旧的,就解绑pv和pvc
        for (VolumeMountExt v : oldPvcList) {
            boolean flag = true; //默认解绑
            for (UpdateVolume newVol : newPvcList) {
                if (newVol.getName().equals(v.getName())) { //代表新的里面包含了旧的,不解绑
                    flag = false;
                }
            }
            if (flag) {
                //在这里解绑
                //删除pvc

//                String pvcName = v.getName();
//                if (pvcName != null && pvcName.length() > 0) {
//
//                }
//                K8SURL url = new K8SURL();
//                url.setName(pvcName).setNamespace(dep.getNamespace()).setResource(Resource.PERSISTENTVOLUMECLAIM);
//                Map<String, Object> headers = new HashMap<>();
//                headers.put("Content-Type", "application/json");
//                Map<String, Object> bodys = new HashMap<>();
//                bodys.put("gracePeriodSeconds", 1);
//                K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.DELETE, headers, bodys, null);
//                if (!HttpStatusUtil.isSuccessStatus(response.getStatus()) && response.getStatus() != Constant.HTTP_404) {
//                    logger.error("灰度升级删除pvc" + response.getBody());
//                }
//
//                // update pv
//                if (response.getStatus() != Constant.HTTP_404) {
//                    String pvname = v.getName();
//
//                    PersistentVolume pv = pvService.getPvByName(pvname, null);
//                    if (pv != null) {
//                        Map<String, Object> bodysPV = new HashMap<String, Object>();
//                        Map<String, Object> metadata = new HashMap<String, Object>();
//                        metadata.put("name", pv.getMetadata().getName());
//                        metadata.put("labels", pv.getMetadata().getLabels());
//                        bodysPV.put("metadata", metadata);
//                        Map<String, Object> spec = new HashMap<String, Object>();
//                        spec.put("capacity", pv.getSpec().getCapacity());
//                        spec.put("nfs", pv.getSpec().getNfs());
//                        spec.put("accessModes", pv.getSpec().getAccessModes());
//                        bodysPV.put("spec", spec);
//                        K8SURL urlPV = new K8SURL();
//                        urlPV.setResource(Resource.PERSISTENTVOLUME).setSubpath(pvname);
//                        Map<String, Object> headersPV = new HashMap<>();
//                        headersPV.put("Content-Type", "application/json");
//                        K8SClientResponse responsePV = new K8sMachineClient().exec(urlPV, HTTPMethod.PUT, headersPV, bodysPV, null);
//                        if (!HttpStatusUtil.isSuccessStatus(responsePV.getStatus())) {
//                            logger.error("灰度升级更新pv" + responsePV.getBody());
//                        }
//                    }
//                }


            }
        }
        //遍历新的,如果旧的里面没有新的就创建pvc
        for (UpdateVolume newVol : newPvcList) {
            boolean flag = true; //默认创建pvc
            if (oldPvcList != null && oldPvcList.size() > 0) {
                for (VolumeMountExt v : oldPvcList) {
                    if (v.getName().equals(newVol.getName())) {//代表旧的里面有新添加的
                        flag = false;
                    }
                }
            }
            if (flag) {
                //在这里创建pvc
                System.out.println("我在创建pvc:" + newVol.getName());
                if (newVol.getName() == "" || newVol.getName() == null) {
                    continue;
                }
                try {
                    volumeSerivce.createVolume(dep.getNamespace(), newVol.getPvcName(), "500", newVol.getPvcTenantid(), newVol.getReadOnly(), newVol.getPvcBindOne(),
                            newVol.getName());

                } catch (Exception e) {
                    logger.error("灰度升级创建pvc失败");
                    e.printStackTrace();
                }
            }
        }


        //循环打印两个列表
        for (UpdateVolume v : newPvcList) {
            System.out.println("新挂载卷列表:" + v.getName());
        }
        for (VolumeMountExt volumeMountExt : oldPvcList) {
            System.out.println("旧挂载卷列表:" + volumeMountExt.getName());
        }


//        List<String> pvcList = new ArrayList<>();
//        // creat pvc
//        for (UpdateContainer c : dep.getContainers()) {
//            if (c.getStorage() != null) {
//                for (UpdateVolume pvc : c.getStorage()) {
//                    if (pvc.getName() == "" || pvc.getName() == null) {
//                        continue;
//                    }
//                    try {
//                        volumeSerivce.createVolume(dep.getNamespace(), pvc.getPvcName(), "500", pvc.getPvcTenantid(), pvc.getReadOnly(),pvc.getPvcBindOne(),
//                                pvc.getName());
//
//                    } catch (Exception e) {
//                        logger.error("灰度升级创建pvc失败");
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }
        return null;
    }


    private Map<String, String> createConfigmaps(CanaryDeployment detail, Cluster cluster) throws Exception {
        Map<String, String> containerToConfigmapMap = new HashedMap();
        List<UpdateContainer> containers = detail.getContainers();
        List<ConfigMap> cms = new ArrayList<ConfigMap>();
        if (!containers.isEmpty()) {
            for (UpdateContainer c : containers) {

                List<CreateConfigMapDto> configMaps = c.getConfigmap();
                if (configMaps != null && configMaps.size() > 0) {
                    K8SURL url = new K8SURL();
                    url.setNamespace(detail.getNamespace()).setResource(Resource.CONFIGMAP);
                    Map<String, Object> bodys = new HashMap<String, Object>();
                    Map<String, Object> meta = new HashMap<String, Object>();
                    meta.put("namespace", detail.getNamespace());
                    String configmaName = detail.getName() + c.getName() + UUID.randomUUID().toString();
                    meta.put("name", configmaName);
                    Map<String, Object> label = new HashMap<String, Object>();
                    label.put("app", detail.getName());
                    meta.put("labels", label);
                    bodys.put("metadata", meta);
                    Map<String, Object> data = new HashMap<String, Object>();
                    for (CreateConfigMapDto configMap : configMaps) {
                        if (configMap != null && !StringUtils.isEmpty(configMap.getPath())) {
                            if (StringUtils.isEmpty(configMap.getFile())) {
                                data.put("config.json", configMap.getValue().toString());
                                System.out.println();
                            } else {
                                data.put(configMap.getFile()+"v"+configMap.getTag(), configMap.getValue().toString());
                            }
                        }
                    }
                    bodys.put("data", data);
                    Map<String, Object> headers = new HashMap<String, Object>();
                    headers.put("Content-type", "application/json");
                    K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.POST, headers, bodys, cluster);
                    System.out.println();
                    if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
                        throw new RuntimeException();
                    }

                    containerToConfigmapMap.put(c.getName(), configmaName);
                }
            }
        }
        return containerToConfigmapMap;
    }

//    private void createPVCFor(Deployment dep) {
//        List<String> pvcList = new ArrayList<>();
//        // creat pvc
//        for (Volume pvc : dep.getSpec().getTemplate().getSpec().getVolumes()) {
//            if (pvc.getName() == "" || pvc.getName() == null) {
//                continue;
//            }
//
//
//            String pvcName = pvc.getName();
//
//            try {
//                volumeSerivce.createVolume(dep.getMetadata().getNamespace(), pvc.getName(), "500", pvc.getPvcTenantid(), pvc.getReadOnly(), pvc.getPvcBindOne(),
//                        pvc.getName());
//
//            } catch (Exception e) {
//                logger.error("灰度升级创建pvc失败");
//                e.printStackTrace();
//            }
//        }
//
//    }


    @SuppressWarnings("unused")
    private void watchAppEvent(String name, String namespace, String kind, String rv, String userName , Cluster cluster)
            throws Exception {
        String token = String.valueOf(K8SClient.tokenMap.get(userName));
        System.out.println("VersionControlServiceImpl2");
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
        K8SClientResponse response = podService.getPodByNamespace(namespace, headers, bodys, HTTPMethod.GET, cluster);
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

        K8SClientResponse rs = new K8sMachineClient().exec(rsUrl, HTTPMethod.GET, headers, bodys, cluster);
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

    private ActionReturnUtil updateServiceByDeployment(Deployment dep, Cluster cluster) throws Exception {


        K8SClientResponse rsRes = sService.doSepcifyService(dep.getMetadata().getNamespace(),
                dep.getMetadata().getName(), null, null, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(rsRes.getStatus())) {
            return ActionReturnUtil.returnErrorWithMsg(rsRes.getBody());
        }

        com.harmonycloud.k8s.bean.Service service = JsonUtil.jsonToPojo(rsRes.getBody(), com.harmonycloud.k8s.bean.Service.class);
        List<ServicePort> ports = new ArrayList();


        for (Container container : dep.getSpec().getTemplate().getSpec().getContainers()) {
            for (ContainerPort port : container.getPorts()) {
                ServicePort servicePort = new ServicePort();
                servicePort.setName("port-" + UUID.randomUUID());
                servicePort.setTargetPort(port.getContainerPort());
                servicePort.setProtocol(port.getProtocol());
                servicePort.setPort(port.getContainerPort());
                ports.add(servicePort);
            }
        }

        if (service != null) {
            service.getSpec().setPorts(ports);
            Map<String, Object> bodys = CollectionUtil.transBean2Map(service);
            Map<String, Object> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");

            K8SClientResponse sRes = sService.doSepcifyService(service.getMetadata().getNamespace(), service.getMetadata().getName(), headers, bodys, HTTPMethod.PUT, cluster);
            if (!HttpStatusUtil.isSuccessStatus(sRes.getStatus())) {
                return ActionReturnUtil.returnErrorWithMsg(sRes.getBody());
            }
        } else {
            Map<String, Object> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            Map<String, Object> meta = new HashedMap();
            meta.put("name", dep.getMetadata().getName());
            Map<String, Object> labells = new HashedMap();
            labells.put("app", dep.getMetadata().getName());
            meta.put("labels", labells);
            Map<String, Object> spec = new HashedMap();
            spec.put("ports", ports);
            meta.put("spec", spec);

            K8SClientResponse sRes = sService.doSepcifyService(dep.getMetadata().getNamespace(), null, headers, meta, HTTPMethod.POST, cluster);
            if (!HttpStatusUtil.isSuccessStatus(sRes.getStatus())) {
                return ActionReturnUtil.returnErrorWithMsg(sRes.getBody());
            }
        }

        return ActionReturnUtil.returnSuccess();

    }


    private ActionReturnUtil increaseServiceByDeployment(Deployment dep , Cluster cluster) throws Exception {

        Map<String, Object> query = new HashMap<String, Object>();
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");


        K8SClientResponse rsRes = sService.doSepcifyService(dep.getMetadata().getNamespace(),
                dep.getMetadata().getName(), null, null, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(rsRes.getStatus())) {
            return ActionReturnUtil.returnErrorWithMsg(rsRes.getBody());
        }
        com.harmonycloud.k8s.bean.Service service = JsonUtil.jsonToPojo(rsRes.getBody(), com.harmonycloud.k8s.bean.Service.class);
        List<ServicePort> ports = new ArrayList();
        Map<Integer, Object> portsMapping = new HashedMap();


        if (service != null) {
            ports = service.getSpec().getPorts();
        } else {
            ActionReturnUtil.returnErrorWithMsg("service not found");
        }

        for (ServicePort port : ports) {
            portsMapping.put(port.getTargetPort(), port);
        }

        for (Container container : dep.getSpec().getTemplate().getSpec().getContainers()) {
            for (ContainerPort port : container.getPorts()) {
                if (portsMapping.get(port.getContainerPort()) == null) {//容器暴露的端口号被更新了

                    ServicePort servicePort = new ServicePort();
                    servicePort.setName("port-" + UUID.randomUUID());
                    servicePort.setTargetPort(port.getContainerPort());
                    servicePort.setProtocol(port.getProtocol());
                    System.out.println();
                    servicePort.setPort(port.getContainerPort());
                    ports.add(servicePort);

                }
            }
        }

        service.getSpec().setPorts(ports);

        Map<String, Object> bodys = CollectionUtil.transBean2Map(service);


        K8SClientResponse res = sService.doSepcifyService(service.getMetadata().getNamespace(), service.getMetadata().getName(), headers, bodys, HTTPMethod.PUT, cluster);
        if (!HttpStatusUtil.isSuccessStatus(res.getStatus())) {
            return ActionReturnUtil.returnErrorWithMsg(res.getBody());
        }

        return ActionReturnUtil.returnSuccessWithData(res);

    }

    /**
     * @param dep                  需要更新的Deployment
     * @param service
     * @param name
     * @param containerToConfigMap
     * @return
     * @throws Exception
     */
    private Map<String, Object> myConvertAppPut(Deployment dep, com.harmonycloud.k8s.bean.Service service, CanaryDeployment newDep, String name, Map<String, String> containerToConfigMap) throws Exception {

        /*List<String> pvcList = createPVC(newDep, dep);*/

        List<UpdateContainer> newContainers = newDep.getContainers();
       /* Map<String, Container> ct = new HashMap<String, Container>();
        //查询到原先的Deployment对应的container
        List<Container> containers = dep.getSpec().getTemplate().getSpec().getContainers();
        for (Container c : containers) {
            ct.put(c.getName(), c);
        }*/
        //Map<String, Object> vtemp = new HashMap<String, Object>();
        List<Volume> volumes = new ArrayList<Volume>();         //修改后的volume
        List<ServicePort> ports = new ArrayList<ServicePort>(); //修改后的端口
        List<Container> newC = new ArrayList<Container>(); //修改后的容器
        //遍历更新的container参数
        for (UpdateContainer cc : newContainers) {//cc是新的
            //拿到需要修改的container,设置成修改后的参数
            Container container = new Container();
            container.setName(cc.getName());
            //set image
            String[] hou;
            String[] qian;
            String images ="";

            if (harborUrl.contains("//") && harborUrl.contains(":")) {
                hou = harborUrl.split("//");
                qian = hou[1].split(":");
                images = qian[0]+"/" + cc.getImg();
            } else {
                images = harborUrl;
            }
            cc.setImg(images);
            container.setImage(cc.getImg());

            //set cpu memory
            if (cc.getResource() != null) { //如果resource参数有更新
                Map<String, String> res = new HashMap<String, String>();

                if (cc.getResource() != null) {
                    ResourceRequirements limit = new ResourceRequirements();
                    String regEx="[^0-9]";
                    Pattern p = Pattern.compile(regEx);
                    Matcher m = p.matcher(cc.getResource().getCpu());
                    String result = m.replaceAll("").trim();
                    res.put("cpu", result + "m");

                    Matcher mm = p.matcher(cc.getResource().getMemory());
                    String resultm = mm.replaceAll("").trim();
                    res.put("memory", resultm + "Mi");
                    ResourceRequirements rr = new ResourceRequirements();
                    rr.setLimits(res);
                    container.setResources(rr);
                }
            }
            if (!cc.getPorts().isEmpty()) { //如果端口有更新
                List<ContainerPort> ps = new ArrayList<ContainerPort>();

                for (CreatePortDto p : cc.getPorts()) {
                    ContainerPort port = new ContainerPort();
                    port.setContainerPort(Integer.valueOf(p.getContainerPort()));
                    port.setProtocol(p.getProtocol());
                    ps.add(port);
                    container.setPorts(ps);
                    ServicePort servicePort = new ServicePort();
                    servicePort.setTargetPort(Integer.valueOf(p.getContainerPort()));
                    servicePort.setPort(Integer.valueOf(p.getContainerPort()));
                    servicePort.setProtocol(p.getProtocol());
                    servicePort.setName(name + "-port" + ports.size());
                    ports.add(servicePort);
                }

            }

            List<VolumeMount> volumeMounts = new ArrayList<VolumeMount>();
            container.setVolumeMounts(volumeMounts);

            if (cc.getConfigmap() != null && cc.getConfigmap().size() > 0) {
                for (CreateConfigMapDto cm : cc.getConfigmap()) {
                    if (cm != null && !StringUtils.isEmpty(cm.getPath())) {
                        String filename = cm.getFile();
                        if(cm.getPath().contains("/")){
                            int in = cm.getPath().lastIndexOf("/");
                            filename = cm.getPath().substring(in+1, cm.getPath().length());
                        }
                        Volume cMap = new Volume();
                        cMap.setName(cm.getFile() + "v" + cm.getTag().replace(".", "-"));
//                        cMap.setName(containerToConfigMap.get(cc.getName()));
                        ConfigMapVolumeSource coMap = new ConfigMapVolumeSource();
//                        coMap.setName("configmap" + cc.getName());
                        coMap.setName(containerToConfigMap.get(cc.getName()));
                        List<KeyToPath> items = new LinkedList<KeyToPath>();
                        KeyToPath key = new KeyToPath();
                        key.setKey(cm.getFile()+"v"+cm.getTag());
                        key.setPath(filename);
                        items.add(key);
                        coMap.setItems(items);
                        cMap.setConfigMap(coMap);
                        volumes.add(cMap);
                        VolumeMount volm = new VolumeMount();
                        volm.setName(cm.getFile() + "v" + cm.getTag().replace(".", "-"));
//                        volm.setName(containerToConfigMap.get(cc.getName()));
                        volm.setMountPath(cm.getPath());
                        // volm.setMountPath(c.getConfigmap().getPath()+"/"+c.getConfigmap().getFile());
                        volm.setSubPath(filename);
                        volumeMounts.add(volm);
//                        container.setVolumeMounts(volumeMounts);
                    }
                }
            }


            if (!StringUtils.isEmpty(cc.getLog().getMountPath())) {
                Volume emp = new Volume();
                emp.setName("logdir" + cc.getName());
                EmptyDirVolumeSource ed = new EmptyDirVolumeSource();
                ed.setMedium("");
                emp.setEmptyDir(ed);
                System.out.println();
                volumes.add(emp);
                VolumeMount volm = new VolumeMount();
                volm.setName("logdir" + cc.getName());
                volm.setMountPath(cc.getLog().getMountPath());
                volumeMounts.add(volm);
//                container.setVolumeMounts(volumeMounts);
            }


            if (cc.getStorage() != null && !cc.getStorage().isEmpty()) {//如果voume有更新
                List<UpdateVolume> newVolume = cc.getStorage();
                Map<String, Object> volFlag = new HashMap<String, Object>();
                for (int i = 0; i < newVolume.size(); i++) {
                    UpdateVolume vol = newVolume.get(i);
                    switch (vol.getType()) {
                        case Constant.VOLUME_TYPE_PV:
                            if (!volFlag.containsKey(vol.getPvcName())) {
                                PersistentVolumeClaimVolumeSource pvClaim = new PersistentVolumeClaimVolumeSource();
                                volFlag.put(vol.getPvcName(), vol.getPvcName());
                                if (vol.getReadOnly().equals("true")) {
                                    pvClaim.setReadOnly(true);
                                }
                                if (vol.getReadOnly().equals("false")) {
                                    pvClaim.setReadOnly(false);
                                }
                                pvClaim.setClaimName(vol.getPvcName());
                                Volume vole = new Volume();
                                vole.setPersistentVolumeClaim(pvClaim);
                                vole.setName(vol.getPvcName());
                                volumes.add(vole);
                            }
                            VolumeMount volm = new VolumeMount();
                            volm.setName(vol.getPvcName());
                            volm.setReadOnly(Boolean.parseBoolean(vol.getReadOnly()));
                            volm.setMountPath(vol.getMountPath());
                            volumeMounts.add(volm);
                            container.setVolumeMounts(volumeMounts);
                            break;
                        case Constant.VOLUME_TYPE_GITREPO:
                            if (!volFlag.containsKey(vol.getGitUrl())) {
                                volFlag.put(vol.getGitUrl(), RandomNum.randomNumber(8));
                                Volume gitRep = new Volume();
                                gitRep.setName(volFlag.get(vol.getGitUrl()).toString());
                                GitRepoVolumeSource gp = new GitRepoVolumeSource();
                                gp.setRepository(vol.getGitUrl());
                                gp.setRevision(vol.getRevision());
                                gitRep.setGitRepo(gp);
                                volumes.add(gitRep);
                            }
                            VolumeMount volmg = new VolumeMount();
                            volmg.setName(volFlag.get(vol.getGitUrl()).toString());
                            volmg.setReadOnly(Boolean.parseBoolean(vol.getReadOnly()));
                            volmg.setMountPath(vol.getMountPath());
                            volumeMounts.add(volmg);
                            container.setVolumeMounts(volumeMounts);
                            break;
                        case Constant.VOLUME_TYPE_EMPTYDIR:
                            if (!volFlag.containsKey(Constant.VOLUME_TYPE_EMPTYDIR+vol.getEmptyDir()==null ? "": vol.getEmptyDir())) {
                                volFlag.put(Constant.VOLUME_TYPE_EMPTYDIR+vol.getEmptyDir()==null ? "": vol.getEmptyDir(), RandomNum.getRandomString(8));
                                Volume empty = new Volume();
                                empty.setName(volFlag.get(Constant.VOLUME_TYPE_EMPTYDIR+vol.getEmptyDir()==null ? "": vol.getEmptyDir()).toString());
                                EmptyDirVolumeSource ed =new EmptyDirVolumeSource();
                                if(vol.getEmptyDir() != null && "Memory".equals(vol.getEmptyDir())){
                                    ed.setMedium(vol.getEmptyDir());//Memory
                                }
                                empty.setEmptyDir(ed);
                                volumes.add(empty);
                            }
                            VolumeMount volme = new VolumeMount();
                            volme.setName(volFlag.get(Constant.VOLUME_TYPE_EMPTYDIR+vol.getEmptyDir()==null ? "": vol.getEmptyDir()).toString());
                            volme.setMountPath(vol.getMountPath());
                            volumeMounts.add(volme);
                            container.setVolumeMounts(volumeMounts);
                            break;
                        case Constant.VOLUME_TYPE_HOSTPASTH:
                            if (!volFlag.containsKey(Constant.VOLUME_TYPE_HOSTPASTH+vol.getHostPath())) {
                                volFlag.put(Constant.VOLUME_TYPE_HOSTPASTH+vol.getHostPath(), RandomNum.getRandomString(8));
                                Volume empty = new Volume();
                                empty.setName(volFlag.get(Constant.VOLUME_TYPE_HOSTPASTH+vol.getHostPath()).toString());
                                HostPath hp =new HostPath();
                                hp.setPath(vol.getHostPath());
                                empty.setHostPath(hp);
                                volumes.add(empty);
                            }
                            VolumeMount volmh = new VolumeMount();
                            volmh.setName(volFlag.get(Constant.VOLUME_TYPE_HOSTPASTH+vol.getHostPath()).toString());
                            volmh.setMountPath(vol.getMountPath());
                            volumeMounts.add(volmh);
                            container.setVolumeMounts(volumeMounts);
                            break;
                        default:
                            break;
                    }
                }
            }
            container.setVolumeMounts(volumeMounts);
            container.setCommand(cc.getCommand());
            container.setArgs(cc.getArgs());
            container.setLivenessProbe(null);
            container.setReadinessProbe(null);
            if (cc.getLivenessProbe() != null && !cc.getLivenessProbe().isEmpty()) {
                Probe lProbe = new Probe();
                HTTPGetAction httpGet = new HTTPGetAction();
                TCPSocketAction tcp=new TCPSocketAction();
                if (cc.getLivenessProbe().getHttpGet() != null) {
                    httpGet.setPath(cc.getLivenessProbe().getHttpGet().getPath());
                    if (cc.getLivenessProbe().getHttpGet().getPort() == 0) {
                        httpGet.setPort(80);
                    } else {
                        //lProbe.getHttpGet().setPort(c.getLivenessProbe().getHttpGet().getPort());
                        httpGet.setPort(cc.getLivenessProbe().getHttpGet().getPort());
                    }
                    lProbe.setHttpGet(httpGet);
                }

                if (cc.getLivenessProbe().getExec() != null ) {
                    if(cc.getLivenessProbe().getExec().getCommand()!=null){
                        ExecAction exec= new ExecAction();
                        exec.setCommand(cc.getLivenessProbe().getExec().getCommand());
                        lProbe.setExec(exec);
                    }
                }

                if (cc.getLivenessProbe().getTcpSocket() != null) {
                    if (cc.getLivenessProbe().getTcpSocket().getPort() == 0) {
                        tcp.setPort(80);
                    } else {
                        tcp.setPort(cc.getLivenessProbe().getTcpSocket().getPort());
                    }
                    lProbe.setTcpSocket(tcp);
                }
                lProbe.setInitialDelaySeconds(cc.getLivenessProbe().getInitialDelaySeconds());
                lProbe.setTimeoutSeconds(cc.getLivenessProbe().getTimeoutSeconds());
                lProbe.setPeriodSeconds(cc.getLivenessProbe().getPeriodSeconds());
                lProbe.setSuccessThreshold(cc.getLivenessProbe().getSuccessThreshold());
                lProbe.setFailureThreshold(cc.getLivenessProbe().getFailureThreshold());
                container.setLivenessProbe(lProbe);
            }

            if (cc.getReadinessProbe() != null  && !cc.getReadinessProbe().isEmpty()) {
                Probe rProbe = new Probe();
                HTTPGetAction httpGet = new HTTPGetAction();
                TCPSocketAction tcp=new TCPSocketAction();
                if (cc.getReadinessProbe().getHttpGet() != null) {
                    httpGet.setPath(cc.getReadinessProbe().getHttpGet().getPath());
                    if (cc.getReadinessProbe().getHttpGet().getPort() == 0) {
                        rProbe.getHttpGet().setPort(80);
                    } else {
                        // rProbe.getHttpGet().setPort(c.getReadinessProbe().getHttpGet().getPort());
                        httpGet.setPort(cc.getReadinessProbe().getHttpGet().getPort());
                    }
                    rProbe.setHttpGet(httpGet);
                }

                if (cc.getReadinessProbe().getExec() != null) {
                    if (cc.getReadinessProbe().getExec().getCommand() != null) {
                        ExecAction exec = new ExecAction();
                        exec.setCommand(cc.getReadinessProbe().getExec().getCommand());
                        rProbe.setExec(exec);
                    }
                }

                if (cc.getReadinessProbe().getTcpSocket() != null) {
                    if (cc.getReadinessProbe().getTcpSocket().getPort() == 0) {
                        tcp.setPort(80);
                    } else {
                        // rProbe.getTcpSocket().setPort(c.getReadinessProbe().getTcpSocket().getPort());
                        tcp.setPort(cc.getReadinessProbe().getTcpSocket().getPort());
                    }
                    rProbe.setTcpSocket(tcp);
                }
                rProbe.setInitialDelaySeconds(cc.getReadinessProbe().getInitialDelaySeconds());
                rProbe.setTimeoutSeconds(cc.getReadinessProbe().getTimeoutSeconds());
                rProbe.setPeriodSeconds(cc.getReadinessProbe().getPeriodSeconds());
                rProbe.setSuccessThreshold(cc.getReadinessProbe().getSuccessThreshold());
                rProbe.setFailureThreshold(cc.getReadinessProbe().getFailureThreshold());
                container.setReadinessProbe(rProbe);
            }
            if (cc.getEnv() != null && !cc.getEnv().isEmpty()) { //如果环境变量有更新
                List<EnvVar> envVars = new ArrayList<EnvVar>();
                for (CreateEnvDto env : cc.getEnv()) {
                    EnvVar eVar = new EnvVar();
                    eVar.setName(env.getName());
                    eVar.setValue(env.getValue());
                    envVars.add(eVar);
                }
                container.setEnv(envVars);
            }
            newC.add(container);
        }


        dep.getSpec().getTemplate().getSpec().setContainers(newC);  //
        dep.getSpec().getTemplate().getSpec().setVolumes(volumes);
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        String updateTime = sdf.format(now);
        Map<String, Object> anno = new HashMap<String, Object>();
        anno=dep.getMetadata().getAnnotations();
        anno.put("updateTimestamp", updateTime);
        dep.getMetadata().setAnnotations(anno);

        if (service != null && service.getSpec() != null) {
            service.getSpec().setPorts(ports);
        }


        Map<String, Object> res = new HashMap<String, Object>();
        res.put("dep", dep);
        res.put("service", service);
        return res;
    }

}