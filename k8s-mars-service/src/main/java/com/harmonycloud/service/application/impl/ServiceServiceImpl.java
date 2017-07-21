package com.harmonycloud.service.application.impl;

import com.alibaba.fastjson.JSON;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.dao.application.BusinessMapper;
import com.harmonycloud.dao.application.BusinessServiceMapper;
import com.harmonycloud.dao.application.ServiceMapper;
import com.harmonycloud.dao.application.ServiceTemplatesMapper;
import com.harmonycloud.dao.application.bean.ServiceTemplates;
import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.dto.business.CreateContainerDto;
import com.harmonycloud.dto.business.CreateVolumeDto;
import com.harmonycloud.dto.business.DeployedServiceNamesDto;
import com.harmonycloud.dto.business.DeploymentDetailDto;
import com.harmonycloud.dto.business.IngressDto;
import com.harmonycloud.dto.business.ParsedIngressListDto;
import com.harmonycloud.dto.business.ServiceDeployDto;
import com.harmonycloud.dto.business.ServiceTemplateDto;
import com.harmonycloud.dto.business.TcpRuleDto;
import com.harmonycloud.dto.svc.SvcTcpDto;
import com.harmonycloud.k8s.bean.PersistentVolume;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.service.PvService;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import com.harmonycloud.service.application.DeploymentsService;
import com.harmonycloud.service.application.RouterService;
import com.harmonycloud.service.application.ServiceService;
import com.harmonycloud.service.application.VolumeSerivce;
import com.harmonycloud.service.platform.bean.RouterSvc;
import com.harmonycloud.service.platform.constant.Constant;
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
	private ServiceMapper serviceMapper;

	@Autowired
	private BusinessMapper businessMapper;

	@Autowired
	private PvService pvService;

	@Autowired
	private RouterService routerService;

	@Autowired
	private DeploymentsService deploymentsService;
	
	@Autowired
	private VolumeSerivce volumeSerivce;
	
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
	public ActionReturnUtil saveServiceTemplate(ServiceTemplateDto serviceTemplate, String userName) throws Exception {

		// check value
		if (StringUtils.isEmpty(userName) || serviceTemplate == null) {
			return ActionReturnUtil.returnErrorWithMsg("username or service template is null");
		}

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
		// create and insert into db
		ServiceTemplates serviceTemplateDB = new ServiceTemplates();
		serviceTemplateDB.setName(serviceTemplate.getName());
		List<ServiceTemplates> list = serviceTemplatesMapper.listByTemplateName(serviceTemplate.getName(),serviceTemplate.getTenant());
		double tag = Constant.TEMPLATE_TAG;
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

		serviceTemplateDB.setStatus(Constant.TEMPLATE_STATUS_CREATE);
		serviceTemplateDB.setTenant(serviceTemplate.getTenant());
		serviceTemplateDB.setUser(userName);
		serviceTemplateDB.setCreateTime(new Date());
		serviceTemplateDB.setFlag(Constant.K8S_SERVICE);
		if (serviceTemplate.getDeploymentDetail() != null) {
			serviceTemplateDB.setNodeSelector(serviceTemplate.getDeploymentDetail().getNodeSelector());
		}
		if (list != null && list.size() > 0) {
			tag = Double.valueOf(list.get(0).getTag()) + Constant.TEMPLATE_TAG_INCREMENT;
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
	 * @param BusinessTemplatesList
	 * 
	 * @return {@link JSONObject}
	 */
	private JSONObject getServiceTemplates(List<ServiceTemplates> serviceTemplatesList) throws Exception {
		JSONObject json = new JSONObject();
		if (serviceTemplatesList != null && serviceTemplatesList.size() > 0) {
			json.put("name", serviceTemplatesList.get(0).getName());
			JSONArray tagArray = new JSONArray();
			for (int i = 0; i < serviceTemplatesList.size(); i++) {
				JSONObject idAndTag = new JSONObject();
				idAndTag.put("id", serviceTemplatesList.get(i).getId());
				idAndTag.put("tag", serviceTemplatesList.get(i).getTag());
				idAndTag.put("image", serviceTemplatesList.get(i).getImageList());
				idAndTag.put("user", serviceTemplatesList.get(i).getUser());
				tagArray.add(idAndTag);
				json.put("createtime", dateToString(serviceTemplatesList.get(i).getCreateTime()));
			}
			json.put("tags", tagArray);
		}
		return json;
	}

	private static String dateToString(Date time) {
		SimpleDateFormat formatter;
		formatter = new SimpleDateFormat("yyyy-MM-dd");
		String ctime = formatter.format(time);

		return ctime;
	}

	@Override
	public ActionReturnUtil deleteDeployedService(DeployedServiceNamesDto deployedServiceNamesDto, String userName, String namespace, Cluster cluster)
			throws Exception {

		// check value
		if (deployedServiceNamesDto == null || deployedServiceNamesDto.getNameList().size() <= 0) {
			return ActionReturnUtil.returnErrorWithMsg("deployedService is null");
		}
		List<String> errorMessage = new ArrayList<>();

		// get service
		List<com.harmonycloud.dao.application.bean.Service> servicelist = serviceMapper
				.selectServiceByNames(deployedServiceNamesDto.getNameList());

		String[] namespaces={namespace};

		List<com.harmonycloud.dao.application.bean.Business> businessList = businessMapper.search(null,namespaces,"");

		List<com.harmonycloud.dao.application.bean.Service> svclist =  new ArrayList<>();

		for (com.harmonycloud.dao.application.bean.Service svc : servicelist) {

			boolean flag = false;

			for (com.harmonycloud.dao.application.bean.Business business : businessList){

				if (svc.getBusinessId().equals(business.getId())){
					flag = true;
					break;
				}
			}
			if (flag){
				svclist.add(svc);
			}
		}

		if (servicelist != null) {
			List<Integer> idList = new ArrayList<>();
			List<Integer> businessIdList = new ArrayList<>();
			for (com.harmonycloud.dao.application.bean.Service service : svclist) {
				boolean serviceFlag = true;

				List<com.harmonycloud.dao.application.bean.Service> serviceID = serviceMapper.selectByBusinessId(service.getBusinessId());

				if (serviceID.size() <= 1){
					businessIdList.add(service.getBusinessId());
				}

				// is external service
				if (service.getIsExternal() == 1) {
					// delete service db
					idList.add(service.getId());
					continue;
				}

				// delete config map & deploy service deployment
				ActionReturnUtil deleteDeployReturn = deploymentsService.deleteDeployment(service.getName(), namespace,
						userName, cluster);
				if (!deleteDeployReturn.isSuccess()) {
					serviceFlag = false;
					errorMessage.add(service.getName());
				}


				//todo sooooooooooooooooooo bad
				// delete ingress
				Map<String, Object> labelMap = new HashMap<String, Object>();
				ParsedIngressListDto parsedIngressListDto = new ParsedIngressListDto();
				parsedIngressListDto.setNamespace(namespace);
				labelMap.put("app",service.getName());
				parsedIngressListDto.setLabels(labelMap);
				List<RouterSvc> routerSvcs = new ArrayList<RouterSvc>();
				routerSvcs = routerService.listIngressByName(parsedIngressListDto);
				if (routerSvcs !=null && routerSvcs.size() > 0){
					for (RouterSvc svcone:routerSvcs){
						if ("HTTP".equals(svcone.getLabels().get("type"))) {
							routerService.ingDelete(namespace, svcone.getName());
							routerService.svcDelete(namespace, svcone.getName());
						} else if ("TCP".equals(svcone.getLabels().get("type"))) {
							routerService.svcDelete(namespace, svcone.getName());
						}
					}
				}
				if (service.getIngress() != null) {
					JSONArray jsStr = JSONArray.fromObject(service.getIngress());
					if (jsStr.size() > 0) {
						for (int i = 0; i < jsStr.size(); i++) {
							JSONObject ingress = jsStr.getJSONObject(i);
							String type = ingress.get("type").toString();
							String name = ingress.get("name").toString();
							if ("HTTP".equals(type)) {
								routerService.ingDelete(namespace, name);
								routerService.svcDelete(namespace, "routersvc" + name);
							} else if ("TCP".equals(type)) {
								routerService.svcDelete(namespace, "routersvc" + name);
							}
						}
					}
				}



				// delete pvc
				if (service.getPvc() != null) {
					JSONArray js = JSONArray.fromObject(service.getPvc());
					if (js.size() > 0) {
						for (int i = 0; i < js.size(); i++) {
							String pvc = (String) js.get(i);
							K8SURL url = new K8SURL();
							url.setName(pvc).setNamespace(namespace).setResource(Resource.PERSISTENTVOLUMECLAIM);
							Map<String, Object> headers = new HashMap<>();
							headers.put("Content-Type", "application/json");
							Map<String, Object> bodys = new HashMap<>();
							bodys.put("gracePeriodSeconds", 1);
							K8SClientResponse response = new K8SClient().doit(url, HTTPMethod.DELETE, headers, bodys);
							if (!HttpStatusUtil.isSuccessStatus(response.getStatus())
									&& response.getStatus() != Constant.HTTP_404) {
								serviceFlag = false;
								errorMessage.add(response.getBody());
							}

							// update pv
							if (response.getStatus() != Constant.HTTP_404 && pvc.contains(Constant.PVC_BREAK)) {
								String[] str = pvc.split(Constant.PVC_BREAK);
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
									K8SClientResponse responsePV = new K8SClient().doit(urlPV, HTTPMethod.PUT,
											headersPV, bodysPV);
									if (!HttpStatusUtil.isSuccessStatus(responsePV.getStatus())) {
										errorMessage.add(responsePV.getBody());
									}
								}
							}

						}
					}
				}

				// add service
				if (serviceFlag) {
					idList.add(service.getId());
				}
			}

			// delete service list
			if (idList.size() > 0) {
				serviceMapper.deleteSerivceByID(idList);
			}

			// delete business list
			if (businessIdList.size() > 0) {
				for (Integer id : businessIdList) {
					businessMapper.deleteBusinessById(id);
				}
			}
		}

		if (errorMessage.size() > 0) {
			return ActionReturnUtil.returnErrorWithData(errorMessage);
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
			serviceMapper.deleteSerivceByNamespace(namespace);
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
	public com.harmonycloud.dao.application.bean.Service getServiceByname(String name, String namespace) throws Exception {
		if(StringUtils.isEmpty(name)){
			return null;
		}else{
			return serviceMapper.selectServiceByName(name, namespace);
		}
	}

	@Override
	public ActionReturnUtil updateServicePvcByname(String name, String pvc, String namespace) throws Exception {
		if(StringUtils.isEmpty(name)){
			return ActionReturnUtil.returnErrorWithMsg("name 为空");
		}else{
			serviceMapper.updateServicePVC(name, pvc, namespace);
			return ActionReturnUtil.returnSuccess();
		}
		
	}

	@Override
	public ActionReturnUtil deployServiceByname(String name, String tag, String namespace, Cluster cluster, String userName)
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
		//获取模板信息
		ServiceTemplates serviceTemplate = serviceTemplatesMapper.getSpecificService(userName, tag);
		if(serviceTemplate != null){
			ServiceDeployDto serviceDeploy = new ServiceDeployDto();
			serviceDeploy.setNamespace(namespace);
			ServiceTemplateDto serviceDto = new ServiceTemplateDto();
			serviceDto.setName(name);
			serviceDto.setTag(tag);
			serviceDto.setId(serviceTemplate.getId());
			serviceDto.setTenant(serviceTemplate.getTenant());
			DeploymentDetailDto deploymentDetail = JsonUtil.jsonToPojo(serviceTemplate.getDeploymentContent(), DeploymentDetailDto.class);
			serviceDto.setDeploymentDetail(deploymentDetail);
			serviceDto.setIngress(JsonUtil.jsonToList(serviceTemplate.getIngressContent(), IngressDto.class));
			serviceDeploy.setServiceTemplate(serviceDto);
			return deployService(serviceDeploy, cluster, userName);
		}else{
			return ActionReturnUtil.returnErrorWithMsg("不存在名称为name"+"，版本为"+tag+"模板");
		}
		
		
	}

	@Override
	public ActionReturnUtil deployService(ServiceDeployDto serviceDeploy, Cluster cluster, String userName) throws Exception {
		if(serviceDeploy == null && StringUtils.isEmpty(serviceDeploy.getNamespace())){
			return ActionReturnUtil.returnErrorWithMsg("应用模板或者namespace为空");
		}
		List<String> pvcList = new ArrayList<>();
		List<Map<String, Object>> message = new ArrayList<>();
		String namespace = serviceDeploy.getNamespace();
		com.harmonycloud.dao.application.bean.Service svc = new com.harmonycloud.dao.application.bean.Service();
		if (serviceDeploy.getServiceTemplate() != null
				&& serviceDeploy.getServiceTemplate().getDeploymentDetail() != null) {
			ServiceTemplateDto service = serviceDeploy.getServiceTemplate();
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
									pvc.getVolume());
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
			List<String> ingresses = new ArrayList<>();
			// creat ingress
			if (service.getIngress() != null) {
				for (IngressDto ingress : service.getIngress()) {
					if ("HTTP".equals(ingress.getType())
							&& !StringUtils.isEmpty(ingress.getParsedIngressList().getName())) {
						SvcTcpDto one = new SvcTcpDto();
						com.harmonycloud.dto.svc.SelectorDto two = new com.harmonycloud.dto.svc.SelectorDto();
						List<TcpRuleDto> th = new ArrayList<>();
						TcpRuleDto httpsvc = new TcpRuleDto();

						one.setName(ingress.getParsedIngressList().getName());
						one.setNamespace(namespace);
						two.setApp(service.getDeploymentDetail().getName());
						one.setSelector(two);

						httpsvc.setPort("80");
						httpsvc.setProtocol("TCP");
						if (ingress.getParsedIngressList().getRules().size() > 0) {
							httpsvc.setTargetPort(ingress.getParsedIngressList().getRules().get(0).getPort());
						} else {
							httpsvc.setTargetPort("80");
						}

						th.add(httpsvc);
						one.setRules(th);

						ActionReturnUtil httpSvcRes = routerService.createhttpsvc(one);
						if (!httpSvcRes.isSuccess()) {
							Map<String, Object> map = new HashMap<String, Object>();
							map.put("service:" + ingress.getParsedIngressList().getName(), httpSvcRes.get("data"));
							message.add(map);
						}
						ActionReturnUtil httpIngRes = routerService.ingCreate(ingress.getParsedIngressList());
						if (!httpIngRes.isSuccess()) {
							Map<String, Object> map = new HashMap<String, Object>();
							map.put("ingress:" + ingress.getParsedIngressList().getName(), httpIngRes.get("data"));
							message.add(map);
						}
						ingresses.add(
								"{\"type\":\"HTTP\",\"name\":\"" + ingress.getParsedIngressList().getName() + "\"}");

					} else if ("TCP".equals(ingress.getType())
							&& !StringUtils.isEmpty(ingress.getSvcRouter().getName())) {
						ActionReturnUtil tcpSvcRes = routerService.svcCreate(ingress.getSvcRouter());
						if (!tcpSvcRes.isSuccess()) {
							Map<String, Object> map = new HashMap<String, Object>();
							map.put("ingress:" + ingress.getParsedIngressList().getName(), tcpSvcRes.get("data"));
							message.add(map);
						}
						ingresses.add("{\"type\":\"TCP\",\"name\":\"" + ingress.getSvcRouter().getName() + "\"}");
					}
				}
			}
	        // insert into service
	        if (ingresses.size() > 0) {
	            JSONArray jsonArray = JSONArray.fromObject(ingresses);
	            svc.setIngress(jsonArray.toString());
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
			// deploymentsService.createDeployment(service.getDeploymentDetail(),
			// username, businessDeploy.getBusinessTemplate().getName() + "_"
			// +businessDeploy.getBusinessTemplate().getTag());
			ActionReturnUtil depRes = deploymentsService.createDeployment(service.getDeploymentDetail(), userName, null,
					cluster);
			if (!depRes.isSuccess()) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put(service.getName(), depRes.get("data"));
				message.add(map);
			}

	        if (pvcList.size() > 0) {
	            JSONArray jsonArraypvc = JSONArray.fromObject(pvcList);
	            svc.setPvc(jsonArraypvc.toString());
	        }
	        svc.setName(service.getDeploymentDetail().getName());
	        svc.setServiceTemplateId(service.getId());
	        svc.setNamespace(namespace);
	        svc.setIsExternal(0);
	        serviceMapper.insertService(svc);
		}
		if (message.size() > 0) {
            return ActionReturnUtil.returnErrorWithData(message);
        }
        return ActionReturnUtil.returnSuccess();
	}

}
