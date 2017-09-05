package com.harmonycloud.service.application.impl;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.dao.application.BusinessServiceMapper;
import com.harmonycloud.dao.application.ServiceTemplatesMapper;
import com.harmonycloud.dao.application.bean.ServiceTemplates;
import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.dto.business.CreateContainerDto;
import com.harmonycloud.dto.business.CreateVolumeDto;
import com.harmonycloud.dto.business.DeployedServiceNamesDto;
import com.harmonycloud.dto.business.DeploymentDetailDto;
import com.harmonycloud.dto.business.HttpRuleDto;
import com.harmonycloud.dto.business.IngressDto;
import com.harmonycloud.dto.business.ParsedIngressListDto;
import com.harmonycloud.dto.business.SelectorDto;
import com.harmonycloud.dto.business.ServiceDeployDto;
import com.harmonycloud.dto.business.ServiceNameNamespace;
import com.harmonycloud.dto.business.ServiceTemplateDto;
import com.harmonycloud.dto.business.SvcRouterDto;
import com.harmonycloud.dto.business.TcpRuleDto;
import com.harmonycloud.dto.svc.SvcTcpDto;
import com.harmonycloud.k8s.bean.*;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.service.PVCService;
import com.harmonycloud.k8s.service.PvService;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import com.harmonycloud.service.application.DeploymentsService;
import com.harmonycloud.service.application.RouterService;
import com.harmonycloud.service.application.ServiceService;
import com.harmonycloud.service.application.VolumeSerivce;
import com.harmonycloud.service.platform.bean.RouterSvc;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.tenant.PrivatePartitionService;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by root on 3/29/17.
 */

@Service
@Transactional(rollbackFor = Exception.class)
public class ServiceServiceImpl implements ServiceService {

	@Autowired
	private ServiceTemplatesMapper serviceTemplatesMapper;

	@Autowired
	private BusinessServiceMapper businessServiceMapper;

	@Autowired
	private PvService pvService;

	@Autowired
	private RouterService routerService;

	@Autowired
	private DeploymentsService deploymentsService;

	@Autowired
	private VolumeSerivce volumeSerivce;

	@Autowired
	private PVCService pvcService;

	@Autowired
	PrivatePartitionService privatePartitionService;

	@Value("#{propertiesReader['image.url']}")
	private String harborUrl;

	DecimalFormat decimalFormat = new DecimalFormat("######0.0");

	/**
	 * create Service Template implement
	 *
	 * @param serviceTemplate
	 * @param username
	 * @return
	 * @throws Exception
	 */
	@Override
	public ActionReturnUtil saveServiceTemplate(ServiceTemplateDto serviceTemplate, String userName ,int type) throws Exception {

		// check value
		if (StringUtils.isEmpty(userName) || serviceTemplate == null) {
			return ActionReturnUtil.returnErrorWithMsg("username or service template is null");
		}
		int a = Constant.TEMPLATE_STATUS_CREATE;
		double tag = Constant.TEMPLATE_TAG;
		if(type == Constant.TEMPLATE_STATUS_DELETE){
			a = Constant.TEMPLATE_STATUS_DELETE;
		}else{
			// check template name
			List<String> tenentExit = serviceTemplatesMapper.listTenantByName(serviceTemplate.getName());
			boolean flag = false;
			if (tenentExit.size() > 0) {
				for (String tenent : tenentExit) {
					if (!serviceTemplate.getTenant().equals(tenent)) {
						flag = true;
						break;
					}
				}
			}
			if (flag) {
				return ActionReturnUtil.returnErrorWithMsg("The service template name already exists");
			}
			List<ServiceTemplates> list = serviceTemplatesMapper.listByTemplateName(serviceTemplate.getName(),serviceTemplate.getTenant());
			if (list != null && list.size() > 0) {
				tag = Double.valueOf(list.get(0).getTag()) + Constant.TEMPLATE_TAG_INCREMENT;
			}
		}

		// create and insert into db
		ServiceTemplates serviceTemplateDB = new ServiceTemplates();
		serviceTemplateDB.setName(serviceTemplate.getName());

		serviceTemplateDB.setDetails(serviceTemplate.getDesc());

		if (serviceTemplate.getDeploymentDetail() != null) {
			JSONArray deploment = JSONArray.fromObject(serviceTemplate.getDeploymentDetail());
			serviceTemplateDB.setDeploymentContent(deploment.toString());
			List<CreateContainerDto> containers = serviceTemplate.getDeploymentDetail().getContainers();
			String images = "";
			for (CreateContainerDto c : containers) {
				images = images + c.getImg() + ",";
			}
			serviceTemplateDB.setImageList(images.substring(0, images.length() - 1));
		}
		if (serviceTemplate.getIngress() != null) {
			JSONArray ingress = JSONArray.fromObject(serviceTemplate.getIngress());
			serviceTemplateDB.setIngressContent(ingress.toString());
		}
		serviceTemplateDB.setStatus(a);
		serviceTemplateDB.setTenant(serviceTemplate.getTenant());
		serviceTemplateDB.setUser(userName);
		serviceTemplateDB.setCreateTime(new Date());
		serviceTemplateDB.setFlag(serviceTemplate.getExternal());
		if (serviceTemplate.getDeploymentDetail() != null) {
			serviceTemplateDB.setNodeSelector(serviceTemplate.getDeploymentDetail().getNodeSelector());
		}
		serviceTemplateDB.setTag(decimalFormat.format(tag));
		serviceTemplatesMapper.insert(serviceTemplateDB);
		JSONObject json = new JSONObject();
		json.put(serviceTemplateDB.getName(), serviceTemplateDB.getId());
		return ActionReturnUtil.returnSuccessWithData(json);
	}

	/**
	 * list Service Template implement
	 *
	 * @param name
	 * @param tenant
	 * @return
	 * @throws Exception
	 */
	@Override
	public ActionReturnUtil listTemplateByTenat(String name, String tenant) throws Exception {
		JSONArray array = new JSONArray();
		// check value null

		// list
		List<ServiceTemplates> serviceBytenant = serviceTemplatesMapper.listNameByTenant(name, tenant);

		if (serviceBytenant != null && serviceBytenant.size() > 0) {
			for (ServiceTemplates serviceTemplates : serviceBytenant) {
				List<ServiceTemplates> serviceList = serviceTemplatesMapper.listServiceByImage(
						serviceTemplates.getName(), serviceTemplates.getImageList(), serviceTemplates.getTenant());
				array.add(getServiceTemplates(serviceList));
			}
		}

		return ActionReturnUtil.returnSuccessWithData(array);
	}

	/**
	 * list Service Template by image
	 *
	 * @param name
	 * @param tenant
	 * @return
	 * @throws Exception
	 */
	@Override
	public ActionReturnUtil listTemplateByImage(String name, String tenant, String image) throws Exception {

		JSONArray array = new JSONArray();
		// check value
		if (StringUtils.isEmpty(image)) {
			return ActionReturnUtil.returnErrorWithMsg("image is null");
		}

		// list
		List<ServiceTemplates> serviceBytenant = serviceTemplatesMapper.listNameByImage(name, image, tenant);
		if (serviceBytenant != null && serviceBytenant.size() > 0) {
			for (ServiceTemplates serviceTemplates : serviceBytenant) {
				List<ServiceTemplates> serviceList = serviceTemplatesMapper.listServiceByImage(
						serviceTemplates.getName(), serviceTemplates.getImageList(), serviceTemplates.getTenant());
				array.add(getServiceTemplates(serviceList));
			}
		}
		return ActionReturnUtil.returnSuccessWithData(array);
	}

	@Override
	public ActionReturnUtil updateServiceTemplata(ServiceTemplateDto serviceTemplate, String username, String tag)
			throws Exception {

		// check value
		if (StringUtils.isEmpty(username) || serviceTemplate == null) {
			return ActionReturnUtil.returnErrorWithMsg("username or service template is null");
		}
		ServiceTemplates serviceTemplateDB = new ServiceTemplates();
		serviceTemplateDB.setName(serviceTemplate.getName());
		if(!StringUtils.isEmpty(tag)){
			serviceTemplateDB.setTag(tag);
		}
		serviceTemplateDB.setDetails(serviceTemplate.getDesc());
		serviceTemplateDB.setId(serviceTemplate.getId());
		if (serviceTemplate.getDeploymentDetail() != null) {
			JSONArray deploment = JSONArray.fromObject(serviceTemplate.getDeploymentDetail());
			serviceTemplateDB.setDeploymentContent(deploment.toString());
			List<CreateContainerDto> containers = serviceTemplate.getDeploymentDetail().getContainers();
			String images = "";
			for (CreateContainerDto c : containers) {
				images = images + c.getImg() + ",";
			}
			serviceTemplateDB.setImageList(images.substring(0, images.length() - 1));
		}

		if (serviceTemplate.getIngress() != null) {
			JSONArray ingress = JSONArray.fromObject(serviceTemplate.getIngress());
			serviceTemplateDB.setIngressContent(ingress.toString());
		}
		serviceTemplateDB.setStatus(Constant.TEMPLATE_STATUS_CREATE);
		serviceTemplateDB.setTenant(serviceTemplate.getTenant());
		serviceTemplateDB.setUser(username);
		// serviceTemplateDB.setUpdateTime(new Date());
		serviceTemplateDB.setFlag(serviceTemplate.getFlag());
		serviceTemplatesMapper.updateServiceTemplate(serviceTemplateDB);
		return ActionReturnUtil.returnSuccess();
	}

	/**
	 * delete service template
	 *
	 * @param name
	 * @return
	 * @throws Exception
	 */
	@Override
	public ActionReturnUtil deleteServiceTemplate(String name, String userName) throws Exception {

		// check value
		if (StringUtils.isEmpty(name)) {
			return ActionReturnUtil.returnErrorWithMsg("service template name is null");
		}

		// get id list by service_template_name
		List<ServiceTemplates> idList = serviceTemplatesMapper.listIDListByTemplateName(name);
		List<Integer> ids = new ArrayList<>();
		for (ServiceTemplates id : idList) {
			ids.add(id.getId());
		}

		// check map
		int mapNum = businessServiceMapper.selectByIdList(ids);
		if (mapNum >= 1) {
			return ActionReturnUtil.returnErrorWithMsg(
					"该应用模板已经被其他业务模板绑定，不能删除！");
		}

		// delete
		serviceTemplatesMapper.deleteByName(name);

		return ActionReturnUtil.returnSuccess();
	}

	/**
	 * select service template
	 *
	 * @param name
	 * @return
	 * @throws Exception
	 */
	@Override
	public ActionReturnUtil getSpecificTemplate(String name, String tag) throws Exception {

		// check value
		if (StringUtils.isEmpty(name) || StringUtils.isEmpty(tag)) {
			return ActionReturnUtil.returnErrorWithMsg("service template name or tag is null");
		}

		ServiceTemplates service = serviceTemplatesMapper.getSpecificService(name, tag);
		return ActionReturnUtil.returnSuccessWithData(service);
	}

	/**
	 * get ServiceTemplate overview on 17/05/05.
	 *
	 * @param serviceTemplatesList
	 *
	 * @return {@link JSONObject}
	 */
	private JSONObject getServiceTemplates(List<ServiceTemplates> serviceTemplatesList) throws Exception {
		JSONObject json = new JSONObject();
		if (serviceTemplatesList != null && serviceTemplatesList.size() > 0) {
			json.put("name", serviceTemplatesList.get(0).getName());
			if("all".equals(serviceTemplatesList.get(0).getTenant())){
				json.put("public", true);
			}else{
				json.put("public", false);
			}
			JSONArray tagArray = new JSONArray();
			for (int i = 0; i < serviceTemplatesList.size(); i++) {
				JSONObject idAndTag = new JSONObject();
				idAndTag.put("id", serviceTemplatesList.get(i).getId());
				idAndTag.put("tag", serviceTemplatesList.get(i).getTag());
				idAndTag.put("image", serviceTemplatesList.get(i).getImageList());
				idAndTag.put("user", serviceTemplatesList.get(i).getUser());
				String dep=JSONArray.fromObject(serviceTemplatesList.get(i).getDeploymentContent()).getJSONObject(0).toString().replaceAll(":\"\",", ":"+null+",").replaceAll(":\"\"", ":"+null+"");
				DeploymentDetailDto deployment = JsonUtil.jsonToPojo(dep, DeploymentDetailDto.class);
				idAndTag.put("name", deployment.getName());
				tagArray.add(idAndTag);
				json.put("createtime", dateToString(serviceTemplatesList.get(i).getCreateTime()));
			}
			json.put("tags", tagArray);
		}
		return json;
	}

	private static String dateToString(Date time) {
		SimpleDateFormat formatter;
		formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String ctime = formatter.format(time);

		return ctime;
	}

	@Override
	public ActionReturnUtil deleteDeployedService(DeployedServiceNamesDto deployedServiceNamesDto, String userName, Cluster cluster)
			throws Exception {

		// check value
		if (deployedServiceNamesDto == null || deployedServiceNamesDto.getServiceList().size() <= 0) {
			return ActionReturnUtil.returnErrorWithMsg("deployedService is null");
		}


		List<String> errorMessage = new ArrayList<>();
		//获取serviceList
		List<Deployment> items = new ArrayList<>();

		for(ServiceNameNamespace nn : deployedServiceNamesDto.getServiceList()){
			K8SURL url = new K8SURL();
			url.setResource(Resource.DEPLOYMENT);
			DeploymentList deployments = new DeploymentList();

			//labels
			Map<String, Object> bodys = new HashMap<String, Object>();
			url.setNamespace(nn.getNamespace());
			url.setName(nn.getName());
			K8SClientResponse depRes = new K8sMachineClient().exec(url, HTTPMethod.GET, null, bodys,cluster);
			if(!HttpStatusUtil.isSuccessStatus(depRes.getStatus()) && depRes.getStatus() != Constant.HTTP_404){
				UnversionedStatus sta = JsonUtil.jsonToPojo(depRes.getBody(), UnversionedStatus.class);
				return ActionReturnUtil.returnErrorWithMsg(sta.getMessage());
			}
			Deployment deployment = JsonUtil.jsonToPojo(depRes.getBody(), Deployment.class);
			if(deployment != null){
				items.add(deployment);
			}else{
				ActionReturnUtil delRes = deploymentsService.deleteDeployment(nn.getName(), nn.getNamespace(), userName, cluster);
				if(!delRes.isSuccess()){
					errorMessage.add(delRes.get("data").toString());
				}
				// delete ingress
				Map<String, Object> labelMap = new HashMap<String, Object>();
				ParsedIngressListDto parsedIngressListDto = new ParsedIngressListDto();
				parsedIngressListDto.setNamespace(nn.getNamespace());
				labelMap.put("app",nn.getName());
				parsedIngressListDto.setLabels(labelMap);
				List<RouterSvc> routerSvcs = new ArrayList<RouterSvc>();
				routerSvcs = routerService.listIngressByName(parsedIngressListDto);
				if (routerSvcs !=null && routerSvcs.size() > 0){
					for (RouterSvc svcone:routerSvcs){
						if ("HTTP".equals(svcone.getLabels().get("type"))) {
							routerService.ingDelete(nn.getNamespace(), svcone.getName());
							//routerService.svcDelete(nn.getNamespace(), svcone.getName());
						} else if ("TCP".equals(svcone.getLabels().get("type"))) {
							routerService.svcDelete(nn.getNamespace(), svcone.getName());
						}
					}
				}
				// delete pvc
				K8SURL url1 = new K8SURL();
				url1.setNamespace(nn.getNamespace()).setResource(Resource.PERSISTENTVOLUMECLAIM);
				Map<String, Object> headers = new HashMap<>();
				headers.put("Content-Type", "application/json");
				Map<String, Object> bodys1 = new HashMap<>();
				bodys1.put("labelSelector", "app=" + nn.getName());
				K8SClientResponse getResponse = new K8sMachineClient().exec(url1, HTTPMethod.GET, headers, bodys1, cluster);
				if(HttpStatusUtil.isSuccessStatus(getResponse.getStatus())){
					PersistentVolumeClaimList pvcList = JsonUtil.jsonToPojo(getResponse.getBody(), PersistentVolumeClaimList.class);
					if(pvcList != null && pvcList.getItems() != null && pvcList.getItems().size() > 0){
						List<PersistentVolumeClaim> pvcs = pvcList.getItems();
						if(pvcs != null && pvcs.size() > 0){
							for(PersistentVolumeClaim pvc : pvcs){
								//update PV
								if( pvc.getSpec() != null && pvc.getSpec().getVolumeName() != null){
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
											errorMessage.add(status.getMessage());
										}
									}
								}
							}
						}
					}
				}
				K8SClientResponse response = new K8sMachineClient().exec(url1, HTTPMethod.DELETE, headers, bodys1, cluster);
				if (!HttpStatusUtil.isSuccessStatus(response.getStatus())
						&& response.getStatus() != Constant.HTTP_404) {
					UnversionedStatus status = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
					errorMessage.add(status.getMessage());
				}
			}
		}

		if (items.size() > 0) {

			List<Integer> idList = new ArrayList<>();
			List<Integer> businessIdList = new ArrayList<>();
			for (Deployment dev : items) {

				String namespace = dev.getMetadata().getNamespace();
				String devName = dev.getMetadata().getName();

				// delete config map & deploy service deployment
				ActionReturnUtil deleteDeployReturn = deploymentsService.deleteDeployment(devName, namespace,
						userName, cluster);
				if (!deleteDeployReturn.isSuccess()) {
					errorMessage.add(deleteDeployReturn.get("data").toString());
				}


				//todo sooooooooooooooooooo bad
				// delete ingress
				Map<String, Object> labelMap = new HashMap<String, Object>();
				ParsedIngressListDto parsedIngressListDto = new ParsedIngressListDto();
				parsedIngressListDto.setNamespace(namespace);
				labelMap.put("app",devName);
				parsedIngressListDto.setLabels(labelMap);
				List<RouterSvc> routerSvcs = new ArrayList<RouterSvc>();
				routerSvcs = routerService.listIngressByName(parsedIngressListDto);
				if (routerSvcs !=null && routerSvcs.size() > 0){
					for (RouterSvc svcone:routerSvcs){
						if ("HTTP".equals(svcone.getLabels().get("type"))) {
							routerService.ingDelete(namespace, svcone.getName());
						} else if ("TCP".equals(svcone.getLabels().get("type"))) {
							routerService.svcDelete(namespace, svcone.getName());
						}
					}
				}


				// delete pvc
				// delete pvc
				Map<String, Object> pvclabel = new HashMap<String, Object>();
				pvclabel.put("labelSelector", "app=" + devName);

				K8SClientResponse pvcRes = pvcService.doSepcifyPVC(namespace, pvclabel, HTTPMethod.GET, cluster);
				if (!HttpStatusUtil.isSuccessStatus(pvcRes.getStatus()) && pvcRes.getStatus() != Constant.HTTP_404) {
					return ActionReturnUtil.returnErrorWithMsg(pvcRes.getBody());
				}

				PersistentVolumeClaimList persistentVolumeList = K8SClient.converToBean(pvcRes, PersistentVolumeClaimList.class);


				if (persistentVolumeList != null && persistentVolumeList.getItems() != null) {

					for (PersistentVolumeClaim onePvc : persistentVolumeList.getItems()) {
						K8SURL url = new K8SURL();
						url.setName(onePvc.getMetadata().getName()).setNamespace(namespace).setResource(Resource.PERSISTENTVOLUMECLAIM);
						Map<String, Object> headers = new HashMap<>();
						headers.put("Content-Type", "application/json");
						Map<String, Object> bodys = new HashMap<>();
						bodys.put("gracePeriodSeconds", 1);
						K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.DELETE, headers, bodys, cluster);
						if (!HttpStatusUtil.isSuccessStatus(response.getStatus()) && response.getStatus() != Constant.HTTP_404) {
							UnversionedStatus status = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
							errorMessage.add(status.getMessage());
						}

						// update pv
						if (response.getStatus() != Constant.HTTP_404 && onePvc.getMetadata().getName().contains(Constant.PVC_BREAK)) {
							String[] str = onePvc.getMetadata().getName().split(Constant.PVC_BREAK);
							String pvname = str[0];
							PersistentVolume pv = pvService.getPvByName(pvname, null);
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
								K8SClientResponse responsePV = new K8sMachineClient().exec(urlPV, HTTPMethod.PUT, headersPV, bodysPV, cluster);
								if (!HttpStatusUtil.isSuccessStatus(responsePV.getStatus())) {
									UnversionedStatus status = JsonUtil.jsonToPojo(responsePV.getBody(), UnversionedStatus.class);
									errorMessage.add(status.getMessage());
								}
							}
						}
					}
				}
			}
		}

		if (errorMessage.size() > 0) {
			return ActionReturnUtil.returnErrorWithMsg(errorMessage.toString());
		}
		return ActionReturnUtil.returnSuccess();

	}

	@Override
	public ActionReturnUtil listServiceTemplate(String searchKey, String searchvalue, String tenant) throws Exception {
		JSONArray array = new JSONArray();
		// check value null

		// list
		List<ServiceTemplates> serviceBytenant=null;
		if(!StringUtils.isEmpty(searchKey)){
			if (searchKey.equals("name")) {
				// search by name
				serviceBytenant = serviceTemplatesMapper.listSearchByName(searchvalue,tenant);
			} else if (searchKey.equals("image")) {
				// search by image
				serviceBytenant = serviceTemplatesMapper.listSearchByImage(searchvalue,tenant);
			}
		}else{
			serviceBytenant = serviceTemplatesMapper.listNameByTenant(null,tenant);
		}
		if (serviceBytenant != null && serviceBytenant.size() > 0) {
			for (ServiceTemplates serviceTemplates : serviceBytenant) {
				List<ServiceTemplates> serviceList = serviceTemplatesMapper.listServiceByImage(
						serviceTemplates.getName(), serviceTemplates.getImageList(), serviceTemplates.getTenant());
				array.add(getServiceTemplates(serviceList));
			}
		}

		return ActionReturnUtil.returnSuccessWithData(array);
	}

	@Override
	public ActionReturnUtil deleteServiceByNamespace(String namespace) throws Exception {
		if(!StringUtils.isEmpty(namespace)){
			return ActionReturnUtil.returnSuccess();
		}else{
			return ActionReturnUtil.returnErrorWithMsg("namespace为空");
		}
	}

	@Override
	public ActionReturnUtil deleteServiceByTenant(String[] tenant) throws Exception {
		if(tenant != null && tenant.length > 0 ){
			serviceTemplatesMapper.deleteByTenant(tenant);
			return ActionReturnUtil.returnSuccess();
		}else{
			return ActionReturnUtil.returnErrorWithMsg("tenant 不能为空");
		}
	}

	@Override
	public ActionReturnUtil deployServiceByname(String app, String tenantId, String name, String tag, String namespace, Cluster cluster, String userName)
			throws Exception {
		if(StringUtils.isEmpty(name)){
			return ActionReturnUtil.returnErrorWithMsg("应用模板名称为空");
		}
		if(StringUtils.isEmpty(tag)){
			return ActionReturnUtil.returnErrorWithMsg("应用模板版本为空");
		}
		if(StringUtils.isEmpty(namespace)){
			return ActionReturnUtil.returnErrorWithMsg("namespace为空");
		}
		ActionReturnUtil privatePartitionLabel = this.privatePartitionService.getPrivatePartitionLabel(tenantId, namespace);
		if(!privatePartitionLabel.isSuccess()){
			return privatePartitionLabel;
		}
		String nodeSelector = (String) privatePartitionLabel.get("data");
		//获取模板信息
		ServiceTemplates serviceTemplate = serviceTemplatesMapper.getSpecificService(name, tag);
		if(serviceTemplate != null){
			ServiceDeployDto serviceDeploy = new ServiceDeployDto();
			serviceDeploy.setNamespace(namespace);
			ServiceTemplateDto serviceDto = new ServiceTemplateDto();
			serviceDto.setName(name);
			serviceDto.setTag(tag);
			serviceDto.setId(serviceTemplate.getId());
			serviceDto.setTenant(serviceTemplate.getTenant());
			String dep=JSONArray.fromObject(serviceTemplate.getDeploymentContent()).getJSONObject(0).toString().replaceAll(":\"\",", ":"+null+",").replaceAll(":\"\"", ":"+null+"");
			DeploymentDetailDto deployment = JsonUtil.jsonToPojo(dep, DeploymentDetailDto.class);
			deployment.setNamespace(namespace);
			String oldName = deployment.getName();
			deployment.setName(app);
			deployment.setNodeSelector(nodeSelector);
			List<CreateContainerDto> cons = deployment.getContainers();
			if(cons != null && cons.size() > 0){
				for(CreateContainerDto c : cons){
					if(c.getStorage() != null && c.getStorage().size() > 0){
						for(CreateVolumeDto v : c.getStorage()){
							if(v.getPvcName() != null && v.getPvcName() != ""){
								v.setPvcName(v.getPvcName().replace("-" + oldName, "-" + app));
							}
						}
					}
				}
			}
			serviceDto.setDeploymentDetail(deployment);
			if(!StringUtils.isEmpty(serviceTemplate.getIngressContent())){
				JSONArray jsarray = JSONArray.fromObject(serviceTemplate.getIngressContent());
				List<IngressDto> ingress = new LinkedList<IngressDto>();
				if(jsarray != null && jsarray.size() > 0 ){
					for(int j = 0; j < jsarray.size(); j++){
						JSONObject ingressJson = jsarray.getJSONObject(j);
						IngressDto ing = JsonUtil.jsonToPojo(ingressJson.toString().toString().replaceAll(":\"\",", ":"+null+",").replaceAll(":\"\"", ":"+null+""), IngressDto.class);
						if(ing.getParsedIngressList() != null){
							ParsedIngressListDto http = ing.getParsedIngressList();
							http.setNamespace(namespace);
							if(http.getRules() != null && http.getRules().size() > 0){
								for(HttpRuleDto r : http.getRules()){
									r.setService(app);
								}
							}
						}
						if(ing.getSvcRouter() != null){
							SvcRouterDto tcp = ing.getSvcRouter();
							tcp.setNamespace(namespace);
							tcp.setApp(app);
							SelectorDto selector =new SelectorDto();
							selector.setApp(app);
							tcp.setSelector(selector);
						}
						ingress.add(ing);
					}
				}
				serviceDto.setIngress(ingress);
			}
			serviceDeploy.setServiceTemplate(serviceDto);
			ActionReturnUtil res = checkService(serviceDto, cluster, namespace);
			if(!res.isSuccess()){
				return res;
			}
			return deployService(serviceDeploy, cluster, userName);
		}else{
			return ActionReturnUtil.returnErrorWithMsg("不存在名称为"+ name +"，版本为"+tag+"模板");
		}


	}

	@Override
	public ActionReturnUtil deployService(ServiceDeployDto serviceDeploy, Cluster cluster, String userName) throws Exception {
		if(serviceDeploy == null && (serviceDeploy == null || StringUtils.isEmpty(serviceDeploy.getNamespace()))){
			return ActionReturnUtil.returnErrorWithMsg("应用模板或者namespace为空");
		}
		ActionReturnUtil addRes = saveServiceTemplate(serviceDeploy.getServiceTemplate(), userName, 1);
		if(!addRes.isSuccess()){
			return addRes;
		}
		JSONObject js = (JSONObject) addRes.get("data");
		int id = js.getInt(serviceDeploy.getServiceTemplate().getName());
		List<String> pvcList = new ArrayList<>();
		List<Map<String, Object>> message = new ArrayList<>();
		String namespace = serviceDeploy.getNamespace();

		if (serviceDeploy.getServiceTemplate() != null
				&& serviceDeploy.getServiceTemplate().getDeploymentDetail() != null) {
			ServiceTemplateDto service = serviceDeploy.getServiceTemplate();
			service.setId(id);
			//check name
			ActionReturnUtil res = checkService(service, cluster, namespace);
			if(!res.isSuccess()){
				return res;
			}
			// creat pvc
			for (CreateContainerDto c : service.getDeploymentDetail().getContainers()) {
				if (c.getStorage() != null) {
					for (CreateVolumeDto pvc : c.getStorage()) {
						if (pvc.getType() != null && Constant.VOLUME_TYPE_PV.equals(pvc.getType())) {
							if (pvc.getPvcName() == "" || pvc.getPvcName() == null) {
								continue;
							}
							ActionReturnUtil pvcres = volumeSerivce.createVolume(namespace, pvc.getPvcName(),
									pvc.getPvcCapacity(), pvc.getPvcTenantid(), pvc.getReadOnly(), pvc.getPvcBindOne(),
									pvc.getVolume(),serviceDeploy.getServiceTemplate().getDeploymentDetail().getName());
							pvcList.add(pvc.getPvcName());
							if (!pvcres.isSuccess()) {
								Map<String, Object> map = new HashMap<String, Object>();
								map.put(pvc.getPvcName(), pvcres.get("data"));
								message.add(map);
							}
						}
					}
				}
			}
			// creat ingress
			if (service.getIngress() != null) {
				for (IngressDto ingress : service.getIngress()) {
					if ("HTTP".equals(ingress.getType())
							&& !StringUtils.isEmpty(ingress.getParsedIngressList().getName())) {
						Map<String, Object> labels = new HashMap<String, Object>();
						labels.put("app", service.getDeploymentDetail().getName());
						ingress.getParsedIngressList().setLabels(labels);
						ingress.getParsedIngressList().setNamespace(namespace);
						ActionReturnUtil httpIngRes = routerService.ingCreate(ingress.getParsedIngressList());
						if (!httpIngRes.isSuccess()) {
							Map<String, Object> map = new HashMap<String, Object>();
							map.put("ingress:" + ingress.getParsedIngressList().getName(), httpIngRes.get("data"));
							message.add(map);
						}
					} else if ("TCP".equals(ingress.getType())
							&& !StringUtils.isEmpty(ingress.getSvcRouter().getName())) {
						Map<String, Object> labels = new HashMap<String, Object>();
						labels.put("app", service.getDeploymentDetail().getName());
						ingress.getSvcRouter().setLabels(labels);
						ingress.getSvcRouter().setNamespace(namespace);
						ActionReturnUtil tcpSvcRes = routerService.svcCreate(ingress.getSvcRouter());
						if (!tcpSvcRes.isSuccess()) {
							Map<String, Object> map = new HashMap<String, Object>();
							map.put("ingress:" + ingress.getParsedIngressList().getName(), tcpSvcRes.get("data"));
							message.add(map);
						}
					}
				}
			}



			// creat config map & deploy service deployment & get node label by
			// namespace
			// todo so bad
			service.getDeploymentDetail().setNamespace(namespace);
			for (CreateContainerDto c : service.getDeploymentDetail().getContainers()) {
				String[] hou;
				String[] qian;
				String images;

				if (harborUrl.contains("//") && harborUrl.contains(":")) {
					hou = harborUrl.split("//");
					qian = hou[1].split(":");
					images = qian[0] + "/" + c.getImg();
				} else {
					images = harborUrl;
				}
				c.setImg(images);
			}
			ActionReturnUtil depRes = deploymentsService.createDeployment(service.getDeploymentDetail(), userName, null,
					cluster);
			if (!depRes.isSuccess()) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put(service.getName(), depRes.get("data"));
				message.add(map);
			}
		}
		if (message.size() > 0) {
			return ActionReturnUtil.returnErrorWithData(message);
		}
		return ActionReturnUtil.returnSuccess();
	}

	/**
	 * 检查Deployment ingress*/
	private ActionReturnUtil checkService(ServiceTemplateDto service, Cluster cluster, String namespace) throws Exception {
		JSONObject msg= new JSONObject();
		//check name
		//service name
		K8SURL url = new K8SURL();
		url.setNamespace(namespace).setResource(Resource.DEPLOYMENT);
		K8SClientResponse depRes = new K8sMachineClient().exec(url, HTTPMethod.GET, null, null,cluster);
		if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus())
				&& depRes.getStatus() != Constant.HTTP_404 ) {
			UnversionedStatus status = JsonUtil.jsonToPojo(depRes.getBody(), UnversionedStatus.class);
			return ActionReturnUtil.returnErrorWithMsg(status.getMessage());
		}
		DeploymentList deplist = JsonUtil.jsonToPojo(depRes.getBody(), DeploymentList.class);
		List<Deployment> deps = new ArrayList<Deployment>();
		if(deplist != null && deplist.getItems() != null ){
			deps = deplist.getItems();
		}
		//ingress http
		List<ParsedIngressListDto> httplist = routerService.ingList(namespace);
		//ingress tcp
		ActionReturnUtil tcpRes = routerService.svcList(namespace);
		if(!tcpRes.isSuccess()){
			return tcpRes;
		}
		@SuppressWarnings("unchecked")
		List<RouterSvc> tcplist = (List<RouterSvc>) tcpRes.get("data");
		boolean flag = true ;
		if(service.getDeploymentDetail() != null && deps != null && deps.size() > 0){
			for(Deployment dep : deps){
				if(service.getDeploymentDetail().getName().equals(dep.getMetadata().getName())){
					msg.put("服务名称:"+service.getDeploymentDetail().getName(), "重复");
					flag = false;
				}
			}
		}
		//check ingress
		if(service.getIngress() != null && service.getIngress().size() > 0){
			for(IngressDto ing : service.getIngress()){
				if(ing.getType() != null && "HTTP".equals(ing.getType()) && httplist != null && httplist.size() > 0){
					for(ParsedIngressListDto http : httplist){
						if(ing.getParsedIngressList().getName().equals(http.getName())){
							msg.put("Ingress(Http):"+ing.getParsedIngressList().getName(), "重复");
							flag = false;
						}
					}

				}
				if(ing.getType() != null && "TCP".equals(ing.getType()) && tcplist != null && tcplist.size() > 0){
					for(RouterSvc tcp : tcplist){
						if(("routersvc"+ing.getSvcRouter().getName()).equals(tcp.getName())){
							msg.put("Ingress(Tcp):"+ing.getSvcRouter().getName(), "重复");
							flag = false;
						}
					}
				}
			}
		}
		if(flag){
			return ActionReturnUtil.returnSuccess();
		}else{
			return ActionReturnUtil.returnErrorWithData(msg);
		}
	}

	@Override
	public ActionReturnUtil listTemplateTagsByName(String name, String tenant) throws Exception {
		if(StringUtils.isEmpty(name)){
			return ActionReturnUtil.returnErrorWithMsg("name为空");
		}
		if(StringUtils.isEmpty(tenant)){
			return ActionReturnUtil.returnErrorWithMsg("租户为空");
		}
		List<ServiceTemplates> list = serviceTemplatesMapper.listServiceByTenant(name, tenant);
		JSONObject json = new JSONObject();
		if(list != null && list.size() >0){
			JSONArray array = new JSONArray();
			json.put("name", name);
			for(ServiceTemplates s : list){
				JSONObject js = new JSONObject();
				js.put("tag", s.getTag());
				js.put("id", s.getId());
				array.add(js);
			}
			json.put("tags", array);
			return ActionReturnUtil.returnSuccessWithData(json);
		}else{
			return ActionReturnUtil.returnErrorWithMsg("不存在模板");
		}
	}

	@Override
	public ActionReturnUtil delById(int id) throws Exception {
		if(id != 0){
			serviceTemplatesMapper.deleteById(id);
			return ActionReturnUtil.returnSuccess();
		}else{
			return ActionReturnUtil.returnErrorWithMsg("模板id为空");
		}
	}
}