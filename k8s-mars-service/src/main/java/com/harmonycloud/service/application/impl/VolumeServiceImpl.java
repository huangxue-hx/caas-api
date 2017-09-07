package com.harmonycloud.service.application.impl;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.CollectionUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.dao.tenant.TenantBindingMapper;
import com.harmonycloud.dao.tenant.bean.TenantBinding;
import com.harmonycloud.dao.tenant.bean.TenantBindingExample;
import com.harmonycloud.k8s.bean.*;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.service.DeploymentService;
import com.harmonycloud.k8s.service.PvService;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import com.harmonycloud.service.application.VolumeSerivce;
import com.harmonycloud.service.platform.bean.PvDto;
import com.harmonycloud.service.platform.bean.VolumeProviderDto;
import com.harmonycloud.service.platform.convert.K8sResultConvert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.*;

@Service
public class VolumeServiceImpl implements VolumeSerivce {

	@Autowired
	private PvService pvService;

	@Autowired
	private TenantBindingMapper tenantBindingMapper;
	
	@Autowired
	private DeploymentService depService;

	@Autowired
	HttpSession session;


	@SuppressWarnings("unchecked")
	@Override
	public ActionReturnUtil listVolume() throws Exception {
		K8SClientResponse response = pvService.listPv(null);
		List<PvDto> pvDtos = new ArrayList<>();
		if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
			PersistentVolumeList persistentVolumeList = K8SClient.converToBean(response, PersistentVolumeList.class);
			List<PersistentVolume> items = persistentVolumeList.getItems();
			// 处理items返回页面需要的对象
			for (PersistentVolume pv : items) {
				PvDto pvDto = new PvDto();
				// 设置读写权限
				if (pv.getSpec().getAccessModes() != null) {
					if (pv.getSpec().getAccessModes().get(0).equals("ReadOnlyMany")) {
						pvDto.setMultiple(true);
						pvDto.setReadOnly(true);
					}
					if (pv.getSpec().getAccessModes().get(0).equals("ReadWriteMany")) {
						pvDto.setMultiple(true);
						pvDto.setReadOnly(false);
					}
					if (pv.getSpec().getAccessModes().get(0).equals("ReadWriteOnce")) {
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
					pvDto.setType("nfs");
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

	/**
	 * 新增PersistentVolume
	 */
	@Override
	public ActionReturnUtil addVolume(PersistentVolume pv) throws Exception{
		return pvService.addPv(pv,null);
	}

	/**
	 * 删除PersistentVolume
	 */
	@Override
	public ActionReturnUtil delVolume(String name) throws Exception{
		return pvService.delPvByName(name,null);
	}

	/**
	 * 获取该namespace下该name的pvc
	 */
	@Override
	public ActionReturnUtil getPvc(String name,String namespace) throws Exception{
		if(pvService.getPvc(name, namespace,null) == null){
			return ActionReturnUtil.returnErrorWithMsg("获取失败");
		}
		return ActionReturnUtil.returnSuccessWithData(pvService.getPvc(name, namespace,null));
	}

	/**
	 * 获取用户的namespace下的存储列表
	 */
	@Override
	public ActionReturnUtil listVolume(String namespace, Cluster cluster) throws Exception{
		K8SClientResponse response = pvService.listPvc(namespace,null);
		if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
			return ActionReturnUtil.returnErrorWithMsg(response.getBody());
		}
		PersistentVolumeClaimList pvcs = JsonUtil.jsonToPojo(response.getBody(), PersistentVolumeClaimList.class);
		
		K8SClientResponse depRes = depService.doDeploymentsByNamespace(namespace, null, null, HTTPMethod.GET, cluster);
		if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus())) {
			return ActionReturnUtil.returnErrorWithMsg(depRes.getBody());
		}
		
		DeploymentList deps = JsonUtil.jsonToPojo(depRes.getBody(), DeploymentList.class);
		return ActionReturnUtil.returnSuccessWithData(K8sResultConvert.convertVolumeList(deps, pvcs));
	}

	@Override
	public ActionReturnUtil createVolume(String namespace, String name, String capacity, String tenantid,
			String readonly, String bindOne,String PVname, String svcName) throws Exception {
		PersistentVolumeClaim pVolumeClaim = new PersistentVolumeClaim();

		//update pv :get deployment name by pv
		Cluster cluster = (Cluster) session.getAttribute("currentCluster");
		PersistentVolume pv = pvService.getPvByName(PVname, null);
		if (pv != null) {
			Map<String, Object> bodysPV = new HashMap<String, Object>();
			Map<String, Object> metadata = new HashMap<String, Object>();
			metadata.put("name", pv.getMetadata().getName());
			Map<String, Object> labelsPV = new HashMap<String, Object>();
			labelsPV = pv.getMetadata().getLabels();
			labelsPV.put("app",svcName);
			metadata.put("labels", labelsPV);
			bodysPV.put("metadata", metadata);
			Map<String, Object> spec = new HashMap<String, Object>();
			spec.put("capacity", pv.getSpec().getCapacity());
			spec.put("nfs", pv.getSpec().getNfs());
			spec.put("accessModes", pv.getSpec().getAccessModes());
			bodysPV.put("spec", spec);
			K8SURL urlPV = new K8SURL();
			urlPV.setResource(Resource.PERSISTENTVOLUME).setSubpath(PVname);
			Map<String, Object> headersPV = new HashMap<>();
			headersPV.put("Content-Type", "application/json");
			K8SClientResponse responsePV = new K8SClient().doit(urlPV, HTTPMethod.PUT, headersPV, bodysPV, cluster);
			if (!HttpStatusUtil.isSuccessStatus(responsePV.getStatus())) {
				UnversionedStatus status = JsonUtil.jsonToPojo(responsePV.getBody(), UnversionedStatus.class);
				return ActionReturnUtil.returnErrorWithMsg(status.getMessage());
			}
		}


		ObjectMeta meta = new ObjectMeta();
		meta.setName(name);
		PersistentVolumeClaimSpec pvSpec = new PersistentVolumeClaimSpec();
		List<String> modes = new ArrayList<String>();
		if ("true".equals(readonly)) {
			modes.add("ReadOnlyMany");
		} else if ("true".equals(bindOne)){
			modes.add("ReadWriteOnce");
		} else {
			modes.add("ReadWriteMany");
		}
		pvSpec.setAccessModes(modes);
		LabelSelector labelSelector = new LabelSelector();
		Map<String, Object> labels = new HashMap<String, Object>();
		labels.put("nephele_tenantid_"+tenantid, tenantid);
		labels.put("app",svcName);
		labelSelector.setMatchLabels(labels);
		meta.setLabels(labels);
		pvSpec.setSelector(labelSelector);
		Map<String, Object> limits = new HashMap<String, Object>();
		if(capacity.contains("Mi") ||capacity.contains("Gi")){
			limits.put("storage", capacity);
		}
		ResourceRequirements resources = new ResourceRequirements();
		resources.setLimits(limits);
		resources.setRequests(limits);
		pvSpec.setResources(resources);
		pvSpec.setVolumeName(PVname);
		pVolumeClaim.setMetadata(meta);
		pVolumeClaim.setSpec(pvSpec);
		Map<String, Object> headers = new HashMap<>();
		headers.put("Content-Type", "application/json");
		Map<String, Object> bodys = CollectionUtil.transBean2Map(pVolumeClaim);
		K8SURL url = new K8SURL();
		url.setNamespace(namespace).setResource(Resource.PERSISTENTVOLUMECLAIM);;
		K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.POST, headers, bodys);
		if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
			UnversionedStatus status = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
			return ActionReturnUtil.returnErrorWithMsg(status.getMessage());
		}
		return ActionReturnUtil.returnSuccessWithData(response.getBody());
	}
	
	@Override
	public ActionReturnUtil createVolume(String namespace, String pvcname, String capacity, String tenantid,
			String readonly, String bindOne,String PVname, String type, String name) throws Exception {
		PersistentVolumeClaim pVolumeClaim = new PersistentVolumeClaim();
		ObjectMeta meta = new ObjectMeta();
		meta.setName(pvcname);
		PersistentVolumeClaimSpec pvSpec = new PersistentVolumeClaimSpec();
		List<String> modes = new ArrayList<String>();
		if ("true".equals(readonly)) {
			modes.add("ReadOnlyMany");
		} else if ("true".equals(bindOne)){
			modes.add("ReadWriteOnce");
		} else {
			modes.add("ReadWriteMany");
		}
		pvSpec.setAccessModes(modes);
		LabelSelector labelSelector = new LabelSelector();
		Map<String, Object> labels = new HashMap<String, Object>();
		labels.put("nephele_tenantid_"+tenantid, tenantid);
		labels.put(type, name);
		labelSelector.setMatchLabels(labels);
		pvSpec.setSelector(labelSelector);
		Map<String, Object> limits = new HashMap<String, Object>();
		if(capacity.contains("Mi") ||capacity.contains("Gi")){
			limits.put("storage", capacity);
		}
		ResourceRequirements resources = new ResourceRequirements();
		resources.setLimits(limits);
		resources.setRequests(limits);
		pvSpec.setResources(resources);
		pvSpec.setVolumeName(PVname);
		pVolumeClaim.setMetadata(meta);
		pVolumeClaim.setSpec(pvSpec);
		Map<String, Object> headers = new HashMap<>();
		headers.put("Content-Type", "application/json");
		Map<String, Object> bodys = CollectionUtil.transBean2Map(pVolumeClaim);
		K8SURL url = new K8SURL();
		url.setNamespace(namespace).setResource(Resource.PERSISTENTVOLUMECLAIM);;
		K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.POST, headers, bodys);
		if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
			return ActionReturnUtil.returnErrorWithMsg(response.getBody());
		}
		return ActionReturnUtil.returnSuccessWithData(response.getBody());
	}

	@Override
	public ActionReturnUtil deleteVolume(String namespace, String name) throws Exception {
		K8SURL url = new K8SURL();
		url.setName(name).setNamespace(namespace).setResource(Resource.PERSISTENTVOLUMECLAIM);
		Map<String, Object> headers = new HashMap<>();
		headers.put("Content-Type", "application/json");
		Map<String, Object> bodys = new HashMap<>();
		bodys.put("gracePeriodSeconds", 1);
		K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.DELETE, headers, bodys);
		if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
			return ActionReturnUtil.returnErrorWithMsg(response.getBody());
		}
		return ActionReturnUtil.returnSuccessWithData(response.getBody());
	}

	@SuppressWarnings("unchecked")
	@Override
	public ActionReturnUtil listVolumeBytenantid(String tenantid) throws Exception{ 
		String label = "nephele_tenantid_"+tenantid + "="+tenantid;
		K8SClientResponse response = pvService.listPvBylabel(label,null);
		List<PvDto> pvDtos = new ArrayList<>();
		if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
			PersistentVolumeList persistentVolumeList = K8SClient.converToBean(response, PersistentVolumeList.class);
			List<PersistentVolume> items = persistentVolumeList.getItems();
			// 处理items返回页面需要的对象
			for (PersistentVolume pv : items) {
				PvDto pvDto = new PvDto();
				// 设置读写权限
				if (pv.getSpec().getAccessModes() != null) {
					if (pv.getSpec().getAccessModes().get(0).equals("ReadOnlyMany")) {
						pvDto.setMultiple(true);
						pvDto.setReadOnly(true);
					}
					if (pv.getSpec().getAccessModes().get(0).equals("ReadWriteMany")) {
						pvDto.setMultiple(true);
						pvDto.setReadOnly(false);
					}
					if (pv.getSpec().getAccessModes().get(0).equals("ReadWriteOnce")) {
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
					pvDto.setType("nfs");
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
		
	@SuppressWarnings("unchecked")
	@Override
	public ActionReturnUtil listProvider() throws Exception {
		K8SURL url = new K8SURL();
		url.setResource(Resource.VOLUMEPROVIDER);
		K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET, null, null);
		if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
			return ActionReturnUtil.returnErrorWithMsg(response.getBody());
		}
		Map<String, Object> result = JsonUtil.convertJsonToMap(response.getBody());
		List<Map<String, Object>> items = (List<Map<String, Object>>)result.get("items");
		List<Map<String, Object>> pro = new ArrayList<Map<String, Object>>();
		if (items != null && items.size() > 0) {	
			for (Map<String, Object> item : items) {
				Map<String, Object> tMap = new HashMap<String, Object>();
				tMap.put("name", ((Map<String, Object>)item.get("metadata")).get("name"));
				tMap.put("time", ((Map<String, Object>)item.get("metadata")).get("creationTimestamp"));
				tMap.put("type", item.get("type"));
				tMap.put("address", ((Map<String, Object>)item.get("providerSpec")).get("ip"));
				tMap.put("path", ((Map<String, Object>)item.get("providerSpec")).get("path"));
				pro.add(tMap);
			}
		}
		return ActionReturnUtil.returnSuccessWithData(pro);
	}

	@Override
	public ActionReturnUtil listVolumeprovider() throws Exception {
		// TODO 值先写死
		List<VolumeProviderDto> VolumeProviderDtoList = new ArrayList<>();
		VolumeProviderDto vp = new VolumeProviderDto();
		vp.setAddress("10.10.102.25");
		vp.setName("nfs-provider");
		vp.setPath("/nfs");
		vp.setTime(new Date().toString());
		vp.setType("nfs");
		VolumeProviderDtoList.add(vp);
		return ActionReturnUtil.returnSuccessWithData(VolumeProviderDtoList);
	}
}
