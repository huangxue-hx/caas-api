package com.harmonycloud.service.tenant.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.CollectionUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.dao.tenant.TenantBindingMapper;
import com.harmonycloud.dao.tenant.bean.TenantBinding;
import com.harmonycloud.dao.tenant.bean.TenantBindingExample;
import com.harmonycloud.dto.tenant.PersistentVolumeDto;
import com.harmonycloud.k8s.bean.Container;
import com.harmonycloud.k8s.bean.Deployment;
import com.harmonycloud.k8s.bean.NFSVolumeSource;
import com.harmonycloud.k8s.bean.ObjectMeta;
import com.harmonycloud.k8s.bean.PersistentVolume;
import com.harmonycloud.k8s.bean.PersistentVolumeClaim;
import com.harmonycloud.k8s.bean.PersistentVolumeList;
import com.harmonycloud.k8s.bean.PersistentVolumeSpec;
import com.harmonycloud.k8s.bean.Pod;
import com.harmonycloud.k8s.bean.PodSpec;
import com.harmonycloud.k8s.bean.UnversionedStatus;
import com.harmonycloud.k8s.bean.Volume;
import com.harmonycloud.k8s.bean.VolumeMount;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.service.DeploymentService;
import com.harmonycloud.k8s.service.PodService;
import com.harmonycloud.k8s.service.PvService;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import com.harmonycloud.service.platform.bean.PvDto;
import com.harmonycloud.service.platform.bean.PvDto.Tenant;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.tenant.PersistentVolumeService;
import com.harmonycloud.service.tenant.TenantService;
@Service
public class PersistenVolumeServiceImpl implements PersistentVolumeService{
    @Autowired
    private PvService pvService;
    
    @Autowired
    private PodService podService;
    
	@Autowired
	DeploymentService dpService;
    
    @Autowired
    private TenantBindingMapper tenantBindingMapper;
    @Autowired
    private TenantService tenantService;
    
    @SuppressWarnings("unchecked")
    @Override
    public ActionReturnUtil listProvider() throws Exception {
        K8SURL url = new K8SURL();
        url.setResource(Resource.VOLUMEPROVIDER);
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET, null, null,null);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            return ActionReturnUtil.returnErrorWithMsg(response.getBody());
        }
        Map<String, Object> result = JsonUtil.convertJsonToMap(response.getBody());
        List<Map<String, Object>> items = (List<Map<String, Object>>)result.get(CommonConstant.ITEMS);
        List<Map<String, Object>> pro = new ArrayList<Map<String, Object>>();
        if (items != null && items.size() > 0) {    
            for (Map<String, Object> item : items) {
                Map<String, Object> tMap = new HashMap<String, Object>();
                tMap.put(CommonConstant.NAME, ((Map<String, Object>)item.get(CommonConstant.METADATA)).get(CommonConstant.NAME));
                tMap.put(CommonConstant.TIME, ((Map<String, Object>)item.get(CommonConstant.METADATA)).get(CommonConstant.CREATIONTIMESTAMP));
                tMap.put(CommonConstant.TYPE, item.get(CommonConstant.TYPE));
                tMap.put(CommonConstant.ADDRESS, ((Map<String, Object>)item.get(CommonConstant.PROVIDERSPEC)).get(CommonConstant.IP));
                tMap.put(CommonConstant.PATH, ((Map<String, Object>)item.get(CommonConstant.PROVIDERSPEC)).get(CommonConstant.PATH));
                pro.add(tMap);
            }
        }
        return ActionReturnUtil.returnSuccessWithData(pro);
    }

    @Override
    public ActionReturnUtil createPv(PersistentVolumeDto persistentVolumedto) throws Exception {
        String tenant = tenantService.getTenantByTenantid(persistentVolumedto.getTenantid()).getTenantName();
        PersistentVolume persistentVolume = new PersistentVolume();
        // 设置metadata
        ObjectMeta metadata = new ObjectMeta();
        metadata.setName(tenant +"."+persistentVolumedto.getName());
        Map<String, Object> labels = new HashMap<>();
        labels.put("nephele_tenantid", persistentVolumedto.getTenantid());
        metadata.setLabels(labels);
        // 设置spec
        PersistentVolumeSpec spec = new PersistentVolumeSpec();
        Map<String, Object> cap = new HashMap<>();
        cap.put(CommonConstant.STORAGE, persistentVolumedto.getCapacity() + "Mi");
        spec.setCapacity(cap);
//        spec.setPersistentVolumeReclaimPolicy(CommonConstant.RECYCLE);
        NFSVolumeSource nfs = new NFSVolumeSource();
        // 设置nfs地址
        nfs.setPath(persistentVolumedto.getPath() + "/" + tenant + "/" + persistentVolumedto.getName());
        nfs.setServer(persistentVolumedto.getNfsServer());
        spec.setNfs(nfs);
        List<String> accessModes = new ArrayList<>();
        if (persistentVolumedto.isReadOnly() == true && persistentVolumedto.isMultiple() == true) {
            accessModes.add(CommonConstant.READONLYMANY);
        }
        if (persistentVolumedto.isReadOnly() == false && persistentVolumedto.isMultiple() == true) {
            accessModes.add(CommonConstant.READWRITEMANY);
        }
        if (persistentVolumedto.isReadOnly() == false && persistentVolumedto.isMultiple() == false) {
            accessModes.add(CommonConstant.READWRITEONCE);
        }
        spec.setAccessModes(accessModes);
        persistentVolume.setMetadata(metadata);
        persistentVolume.setSpec(spec);
        persistentVolume.setApiVersion("v1");
        persistentVolume.setKind(CommonConstant.PERSISTENTVOLUME);
         ActionReturnUtil addPv = pvService.addPv(persistentVolume,null);
        return addPv;
    }

    @Override
    public ActionReturnUtil listPvBytenant(String tenantid) throws Exception {
        String label = "nephele_tenantid ="+tenantid;
        Cluster cluster = tenantService.getClusterByTenantid(tenantid);
        K8SClientResponse response = pvService.listPvBylabel(label,cluster);
        List<PvDto> pvDtos = new ArrayList<>();
        if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            PersistentVolumeList persistentVolumeList = K8SClient.converToBean(response, PersistentVolumeList.class);
            List<PersistentVolume> items = persistentVolumeList.getItems();
            // 处理items返回页面需要的对象
            for (PersistentVolume pv : items) {
                PvDto pvDto = new PvDto();
                // 设置读写权限
                if (pv.getSpec().getAccessModes() != null) {
                    if (pv.getSpec().getAccessModes().get(0).equals(CommonConstant.READONLYMANY)) {
                        pvDto.setMultiple(true);
                        pvDto.setReadOnly(true);
                    }
                    if (pv.getSpec().getAccessModes().get(0).equals(CommonConstant.READWRITEMANY)) {
                        pvDto.setMultiple(true);
                        pvDto.setReadOnly(false);
                    }
                    if (pv.getSpec().getAccessModes().get(0).equals(CommonConstant.READWRITEONCE)) {
                        pvDto.setMultiple(false);
                        pvDto.setReadOnly(false);
                    }
                    // 设置bind
                    if (pv.getSpec().getClaimRef() == null) {
                        pvDto.setBind(false);
                        // 设置usage
                        pvDto.setUsage("false");
                    } else {
                        // 设置usage
                        pvDto.setUsage(pv.getSpec().getClaimRef().getName());
                    }
                    // 设置容量
                    @SuppressWarnings("unchecked")
                    Map<String, String> capacity = (Map<String, String>) pv.getSpec().getCapacity();
                    pvDto.setCapacity(capacity.get(CommonConstant.STORAGE));
                    // 设置pv名称
                    pvDto.setName(pv.getMetadata().getName());
                    // 设置time
                    pvDto.setTime(pv.getMetadata().getCreationTimestamp());
                    // 设置type
                    pvDto.setType(CommonConstant.NFS);
                    pvDto.setBind(pv.getSpec().getClaimRef());
                    // 设置tenantid
                    Map<String, Object> labels = pv.getMetadata().getLabels();
                    Collection<Object> values = labels.values();
                    String min = null;
                    for (Object object : values) {
                        if (min == null) {
                            min = object.toString();
                        }
                        if (object.toString().length() < min.length()) {
                            min = object.toString();
                        }
                    }
                    // 设置tenant
                    PvDto.Tenant tenant = pvDto.new Tenant();
                    tenant.setTenantid(min);
                    pvDto.setTenant(tenant);
                    pvDto.setTenantid(min);
                    if (pv.getSpec().getClaimRef() != null) {
                        String namespace = pv.getSpec().getClaimRef().getNamespace();
                        // 将namespace处理为tenantname
                        String[] split = namespace.split("-");
                        tenant.setTenantname(split[1]);
                    } else {
                        // 根据tenantId查询tenantName
                        TenantBindingExample example = new TenantBindingExample();
                        example.createCriteria().andTenantIdEqualTo(min);
                        List<TenantBinding> list = tenantBindingMapper.selectByExample(example);
                        if (list != null && list.size() > 0) {
                            tenant.setTenantname(list.get(0).getTenantName());
                        }
                    }
                    pvDtos.add(pvDto);
            }
            }
        }
        return ActionReturnUtil.returnSuccessWithData(pvDtos);
    }
    @Override
    public ActionReturnUtil deletePvByName(String name) throws Exception {
        ActionReturnUtil delPvByName = pvService.delPvByName(name,null);
        return delPvByName;
    }

    @Override
    public ActionReturnUtil getPVByName(String name) throws Exception {
        PersistentVolume pvByName = this.pvService.getPvByName(name,null);
        if(pvByName == null){
            return ActionReturnUtil.returnErrorWithMsg("pv存储券:"+name+"不存在");
        }
        return ActionReturnUtil.returnSuccessWithData(pvByName);
    }

    @Override
    public ActionReturnUtil updatePvByName(String name,String capacity,Boolean readOnly,Boolean multiple) throws Exception {
        PersistentVolume pvByName = this.pvService.getPvByName(name,null);
        if(pvByName == null){
            return ActionReturnUtil.returnErrorWithMsg("pv存储券:"+name+"不存在");
        }
        Map<String, Object> cap = new HashMap<>();
        cap.put(CommonConstant.STORAGE, capacity + "Mi");
        pvByName.getSpec().setCapacity(cap);
       if(readOnly !=null && multiple !=null){
           List<String> accessModes = new ArrayList<>();
           if (readOnly == true && multiple == true) {
               accessModes.add(CommonConstant.READONLYMANY);
           }
           if (readOnly == false && multiple == true) {
               accessModes.add(CommonConstant.READWRITEMANY);
           }
           if (readOnly == false && multiple == false) {
               accessModes.add(CommonConstant.READWRITEONCE);
           }
           pvByName.getSpec().setAccessModes(accessModes);
       }
        K8SClientResponse updatePvByName = pvService.updatePvByName(pvByName,null);
        if(!HttpStatusUtil.isSuccessStatus(updatePvByName.getStatus())){
            return ActionReturnUtil.returnErrorWithMsg("跟新失败，错误信息："+updatePvByName.getBody());
        }
        return ActionReturnUtil.returnSuccess();
    }

    /**
     * 根据租户id查询pv是否存在，如果存在则删除
     *
     * @param tenantid
     * @return
     */
    @Override
    public ActionReturnUtil deletePVBytenantid(String tenantid) throws Exception {
        Map<String, Object> bodys = new HashMap<>();
        // 根据lable查询pv
        bodys.put(CommonConstant.LABELSELECTOR, "nephele_tenantid=" + tenantid);
        K8SURL url = new K8SURL();
        Cluster cluster = tenantService.getClusterByTenantid(tenantid);
        url.setResource(com.harmonycloud.k8s.constant.Resource.PERSISTENTVOLUME);
        K8SClientResponse k8SClientResponse = new K8sMachineClient().exec(url, HTTPMethod.GET, null, bodys,cluster);

        if (HttpStatusUtil.isSuccessStatus(k8SClientResponse.getStatus())) {
            if (StringUtils.isEmpty(k8SClientResponse.getBody())) {
                return ActionReturnUtil.returnSuccessWithData(false);
            }
            PersistentVolumeList persistentVolumeList = JsonUtil.jsonToPojo(k8SClientResponse.getBody(), PersistentVolumeList.class);
            if (persistentVolumeList == null)
                return ActionReturnUtil.returnSuccessWithData(false);
            List<PersistentVolume> items = persistentVolumeList.getItems();
            if (items.size() > 0) {
                for (PersistentVolume pv : items) {
                    ActionReturnUtil delPvByName = this.pvService.delPvByName(pv.getMetadata().getName(),cluster);
                    if ((Boolean) delPvByName.get(CommonConstant.SUCCESS) == false) {
                        return delPvByName;
                    }
                }
                return ActionReturnUtil.returnSuccessWithData(true);
            }

            return ActionReturnUtil.returnSuccessWithData(false);
        } else {
            return ActionReturnUtil.returnErrorWithMsg(k8SClientResponse.getBody());
        }

    }

    @SuppressWarnings("unchecked")
    @Override
    public ActionReturnUtil listAllPv(Cluster cluster) throws Exception {
        K8SClientResponse response = pvService.listPv(cluster);
        List<PvDto> pvDtos = new ArrayList<>();
        if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            PersistentVolumeList persistentVolumeList = K8SClient.converToBean(response, PersistentVolumeList.class);
            List<PersistentVolume> items = persistentVolumeList.getItems();
            // 处理items返回页面需要的对象
            for (PersistentVolume pv : items) {
                PvDto pvDto = new PvDto();
                // 设置读写权限
                if (pv.getSpec().getAccessModes() != null) {
                    if (pv.getSpec().getAccessModes().get(0).equals(CommonConstant.READONLYMANY)) {
                        pvDto.setMultiple(true);
                        pvDto.setReadOnly(true);
                    }
                    if (pv.getSpec().getAccessModes().get(0).equals(CommonConstant.READWRITEMANY)) {
                        pvDto.setMultiple(true);
                        pvDto.setReadOnly(false);
                    }
                    if (pv.getSpec().getAccessModes().get(0).equals(CommonConstant.READWRITEONCE)) {
                        pvDto.setMultiple(false);
                        pvDto.setReadOnly(false);
                    }
                    // 设置bind
                    if (pv.getSpec().getClaimRef() == null) {
                        pvDto.setBind(false);
                        // 设置usage
                        pvDto.setUsage("false");
                    } else {
                        // 设置usage
                        pvDto.setUsage(pv.getSpec().getClaimRef().getName());
                    }
                    // 设置容量
                    Map<String, String> capacity = (Map<String, String>) pv.getSpec().getCapacity();
                    pvDto.setCapacity(capacity.get("storage"));
                    // 设置pv名称
                    pvDto.setName(pv.getMetadata().getName());
                    // 设置time
                    pvDto.setTime(pv.getMetadata().getCreationTimestamp());
                    // 设置type
                    pvDto.setType(CommonConstant.NFS);
                    pvDto.setBind(pv.getSpec().getClaimRef());
                    // 设置tenantid
                    Map<String, Object> labels = pv.getMetadata().getLabels();
                    if (labels.values() != null) {
                        Collection<Object> values = labels.values();
                        String min = null;
                        for (Object object : values) {
                            if (min == null) {
                                min = object.toString();
                            }
                            if (object.toString().length() < min.length()) {
                                min = object.toString();
                            }
                        }

                        // 设置tenant
                        PvDto.Tenant tenant = pvDto.new Tenant();
                        tenant.setTenantid(min);
                        pvDto.setTenant(tenant);
                        pvDto.setTenantid(min);

                        if (pv.getSpec().getClaimRef() != null) {
                            String namespace = pv.getSpec().getClaimRef().getNamespace();
                            // 将namespace处理为tenantname
                            String[] split = namespace.split("-");
                            tenant.setTenantname(split[1]);
                        } else {
                            // 根据tenantId查询tenantName
                            TenantBindingExample example = new TenantBindingExample();
                            example.createCriteria().andTenantIdEqualTo(min);
                            List<TenantBinding> list = tenantBindingMapper.selectByExample(example);
                            if (list != null && list.size() > 0) {
                                tenant.setTenantname(list.get(0).getTenantName());
                            }
                        }
                    }

                }
                pvDtos.add(pvDto);
            }
        }
        return ActionReturnUtil.returnSuccessWithData(pvDtos);
    }

	@Override
	public ActionReturnUtil recyclePvByName(String name, Cluster cluster) throws Exception {
		PersistentVolume pv = this.pvService.getPvByName(name, cluster);
		if(pv != null){
			Pod pod = new Pod();
			//metadata
			ObjectMeta metadata = new ObjectMeta();
			metadata.setName(CommonConstant.PV_RECYCLE_POD_NAME + name);
			metadata.setNamespace(CommonConstant.CICD_NAMESPACE);
			pod.setMetadata(metadata);
			//spec
			PodSpec spec = new PodSpec();
			spec.setRestartPolicy(CommonConstant.RESTARTPOLICY_NEVER);
			//container
			List<Container> cs = new ArrayList<Container>();
			Container con = new Container();
			con.setName("pv-recycler");
			con.setImage("k8s-deploy/busybox");
			con.setImagePullPolicy("IfNotPresent");
			List<String> command = new ArrayList<String>();
			command.add("/bin/sh");
			command.add("-c");
			command.add("test -e /scrub && rm -rf /scrub/..?* /scrub/.[!.]* /scrub/*  && test -z \"$(ls-A /scrub)\" || exit 1");
			con.setCommand(command);
			List<VolumeMount> vms = new ArrayList<VolumeMount>();
			VolumeMount vm = new VolumeMount();
			vm.setMountPath("/scrub");
			vm.setName("vol");
			vms.add(vm);
			con.setVolumeMounts(vms);
			cs.add(con);
			spec.setContainers(cs);
			//volumes
			List<Volume> vs = new ArrayList<Volume>();
			Volume v = new Volume();
			v.setNfs(pv.getSpec().getNfs());
			v.setName("vol");
			vs.add(v);
			spec.setVolumes(vs);
			pod.setSpec(spec);
			ActionReturnUtil res = podService.addPod(CommonConstant.CICD_NAMESPACE, pod, cluster);
			// 开启线程执行
			new Thread() {
				@Override
				public void run() {
					try {
						for (; ; ) {
	                        //暂停两秒钟等待灰度升级开始
	                        Thread.sleep(2000);
	                        K8SClientResponse dp = podService.getPod(CommonConstant.CICD_NAMESPACE, CommonConstant.PV_RECYCLE_POD_NAME + name, cluster);
	                        if (!HttpStatusUtil.isSuccessStatus(dp.getStatus()) && dp.getStatus() != Constant.HTTP_404) {
	                        	break;
	                        }
	                        Pod pod = JsonUtil.jsonToPojo(dp.getBody(), Pod.class);
	                        if(pod != null && pod.getMetadata() != null && pod.getMetadata().getName() != null){
	                        	if(pod.getStatus() != null && pod.getStatus().getPhase() != null){
	                        		if( !"Running".equals(pod.getStatus().getPhase())){
	                        			podService.deletePod(CommonConstant.CICD_NAMESPACE, CommonConstant.PV_RECYCLE_POD_NAME + name, cluster);
	                        			break;
	                        		}
	                        	}
	                        }else{
	                        	break;
	                        }
	                    }
					} catch (Exception e) {
						e.printStackTrace();
					}
				};
			}.start();
			return res;
		}else{
			return ActionReturnUtil.returnErrorWithMsg("不存在pv"+name);
		}
		
	}
}
