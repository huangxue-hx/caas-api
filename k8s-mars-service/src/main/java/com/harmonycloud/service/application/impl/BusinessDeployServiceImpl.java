package com.harmonycloud.service.application.impl;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.dao.application.*;
import com.harmonycloud.dao.application.bean.Business;
import com.harmonycloud.dao.application.bean.ServiceTemplates;
import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.dao.network.TopologyMapper;
import com.harmonycloud.dao.network.bean.Topology;
import com.harmonycloud.dao.tenant.TenantBindingMapper;
import com.harmonycloud.dao.tenant.bean.TenantBinding;
import com.harmonycloud.dao.tenant.bean.TenantBindingExample;
import com.harmonycloud.dto.business.*;
import com.harmonycloud.k8s.bean.*;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.service.DeploymentService;
import com.harmonycloud.k8s.service.PvService;
import com.harmonycloud.k8s.service.ServicesService;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import com.harmonycloud.service.application.BusinessDeployService;
import com.harmonycloud.service.application.DeploymentsService;
import com.harmonycloud.service.application.RouterService;
import com.harmonycloud.service.application.ServiceService;
import com.harmonycloud.service.application.VolumeSerivce;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.platform.bean.BusinessList;
import com.harmonycloud.service.platform.bean.PvDto;
import com.harmonycloud.service.platform.bean.RouterSvc;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.tenant.NamespaceService;
import com.harmonycloud.service.tenant.PrivatePartitionService;
import com.harmonycloud.service.tenant.TenantService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Created by root on 4/10/17.
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class BusinessDeployServiceImpl implements BusinessDeployService {

    @Autowired
    private BusinessMapper businessMapper;

    @Autowired
    private TopologyMapper topologyMapper;

    @Autowired
    private TenantService tenantService;
    
    @Autowired
    private ClusterService clusterService;
    
    @Autowired
    private ServiceMapper serviceMapper;

    @Autowired
    private VolumeSerivce volumeSerivce;

    @Autowired
    private DeploymentsService deploymentsService;

    @Autowired
    private BusinessTemplatesMapper businessTemplatesMapper;

    @Autowired
    private RouterService routerService;
    
    @Autowired
    private com.harmonycloud.service.application.BusinessService businessService;

    @Autowired
    private TenantBindingMapper tenantBindingMapper;

    @Autowired
    private PvService pvService;

    @Autowired
    private DeploymentService dpService;
    
    @Autowired
    private ServiceTemplatesMapper serviceTemplatesMapper;
    
    @Autowired
    NamespaceService namespaceService;
    
    @Autowired
    PrivatePartitionService privatePartitionService;
    
    @Autowired
    ServiceService serviceService;

    public static final String ABNORMAL = "0";

    public static final String NORMAL = "1";

    @Autowired
    private ServicesService sService;
    

    @Value("#{propertiesReader['image.url']}")
    private String harborUrl;

    /**
     * get application by tenant namespace name status service implement
     * 
     * @author gurongyun
     * 
     * @param tenant
     *            tenant name
     * @param namespace
     *            namespace
     * @param name
     *            application name
     * @param status
     *            application running status 0:abnormal;1:normal
     * @return ActionReturnUtil
     */
	@Override
    public ActionReturnUtil searchBusiness(String tenantId, String tenant, String namespace, String name, String status) throws Exception {
        JSONObject json = new JSONObject();
        // application list
        JSONArray array = new JSONArray();
        List<Business> blist = null;
        // search application
        if(StringUtils.isEmpty(namespace)){
        	String [] namespaces = new String[0];
        	if(StringUtils.isEmpty(tenant)){
        		String [] tenants= new String[0];
        		blist = businessMapper.search(tenants, namespaces, name);
            }else{
            	String [] tenants = {tenant};
                if( tenant.contains(",")){
                	tenants=tenant.split(",");
                }
                blist = businessMapper.search(tenants, namespaces, name);
            }
        }else{
        	String [] namespaces = {namespace};
            if(namespace.contains(",")){
            	namespaces=namespace.split(",");
            }
        	if(StringUtils.isEmpty(tenant)){
        		String [] tenants= new String[0];
        		blist = businessMapper.search(tenants, namespaces, name);
            }else{
            	String [] tenants = {tenant};
                if( tenant.contains(",")){
                	tenants=tenant.split(",");
                }
                blist = businessMapper.search(tenants, namespaces, name);
            }
        }


        Cluster	cluster=tenantService.getClusterByTenantid(tenantId);

        // number of application
        int count = 0;
        if (blist != null && blist.size() > 0) {
            count = blist.size();
            for (Business bs : blist) {
                JSONObject js = new JSONObject();
                // put application info
                js.put("name", bs.getName());
                js.put("id", bs.getId());
                js.put("desc", bs.getDetails());
                js.put("namespace", bs.getNamespaces());
                js.put("createTime", dateToString(bs.getCreateTime()));
                js.put("businessTemplateId", bs.getTemplateId());
                js.put("namespce", bs.getNamespaces());
                js.put("tenant", bs.getTenant());
                js.put("user", bs.getUser());
                JSONObject servicejson = listServiceByBusinessId(bs.getId(), bs.getNamespaces(),cluster);
                js.putAll(servicejson);
                if (StringUtils.isEmpty(status)) {
                    array.add(js);
                } else {
                    if (status.equals(NORMAL)) {
                        // running
                        if (NORMAL.equals(js.get("status"))) {
                            array.add(js);
                        }
                    } else {
                        if (ABNORMAL.equals(js.get("status"))) {
                            array.add(js);
                        }
                    }
                }
            }

        } else {
            return ActionReturnUtil.returnSuccessWithData(null);
        }
        json.put("list", array);
        json.put("count", count);
        return ActionReturnUtil.returnSuccessWithData(json);
    }

    /**
     * get application by id service implement.
     * 
     * @author gurongyun
     * @param id
     *            application id
     * @return ActionReturnUtil
     */
    @Override
    public ActionReturnUtil selectBusinessById(int id, Cluster cluster) throws Exception {
        if (id == 0) {
            return ActionReturnUtil.returnErrorWithMsg("application id is null");
        }
        JSONObject js = new JSONObject();
        // get application
        Business business = businessMapper.selectByPrimaryKey(id);
        if (business != null) {
            // put application info
            js.put("id", business.getId());
            js.put("name", business.getName());
            js.put("createTime", dateToString(business.getCreateTime()));
            js.put("desc", business.getDetails());
            js.put("businessTemplateId", business.getTemplateId());
            js.put("namespace", business.getNamespaces());
            js.put("tenant", business.getTenant());
            js.put("user", business.getUser());
            // get topology
            List<Topology> tolist = topologyMapper.selectByBusinessIdAndServiceTemplateId(business.getTemplateId(), business.getId());
            JSONArray toarray = new JSONArray();
            if (tolist != null && tolist.size() > 0) {
                for (Topology to : tolist) {
                    JSONObject js1 = new JSONObject();
                    // topology info
                    js1.put("id", to.getId());
                    js1.put("businessTemplateId", to.getBusinessId());
                    js1.put("detail", to.getDetails());
                    js1.put("source", to.getSource());
                    js1.put("target", to.getTarget());
                    toarray.add(js1);
                }
            }
            js.put("topologyList", toarray);
            // getService
            List<com.harmonycloud.dao.application.bean.Service> servicelist = serviceMapper.selectByBusinessId(id);
            JSONArray serarray = new JSONArray();
            if (servicelist != null && servicelist.size() > 0) {
                for (com.harmonycloud.dao.application.bean.Service ser : servicelist) {
                    JSONObject json = new JSONObject();
                    // service info
                    json.put("id", ser.getId());
                    json.put("businessId", ser.getBusinessId());
                    json.put("isExternal", ser.getIsExternal());
                    json.put("name", ser.getName());
                    json.put("serviceTemplateId", ser.getServiceTemplateId());
                    if (ser.getIsExternal() == 1) {
                        // external service always is running
                        K8SClientResponse response = sService.doServiceByName(Resource.EXTERNALNAMESPACE, null, null, HTTPMethod.GET,ser.getName());
                        if (!HttpStatusUtil.isSuccessStatus(response.getStatus()) && response.getStatus() != Constant.HTTP_404) {
                            return ActionReturnUtil.returnErrorWithMsg(response.getBody());
                        }
                        if (response.getStatus() != Constant.HTTP_404){
                            com.harmonycloud.k8s.bean.Service svc = JsonUtil.jsonToPojo(response.getBody(), com.harmonycloud.k8s.bean.Service.class);
                            json.put("ip",svc.getMetadata().getLabels().get("ip").toString());
                            json.put("port",svc.getSpec().getPorts().get(0).getTargetPort());
                            json.put("type",svc.getMetadata().getLabels().get("type").toString());
                            json.put("createTime",svc.getMetadata().getCreationTimestamp());
                        }
                        json.put("status", Constant.SERVICE_START);
                    } else {
                        // get deployment by name
                        K8SClientResponse depRes = dpService.doSpecifyDeployment(business.getNamespaces(), ser.getName(), null, null, HTTPMethod.GET,cluster);
                        if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus()) && depRes.getStatus() != Constant.HTTP_404) {
                            return ActionReturnUtil.returnErrorWithMsg(depRes.getBody());
                        }
                        if (depRes.getStatus() == Constant.HTTP_404){
                            continue;
                        }

                        Deployment dep = JsonUtil.jsonToPojo(depRes.getBody(), Deployment.class);
                        // get labels
                        JSONObject labeljson = new JSONObject();
                        String labels = null;
                        if (dep.getMetadata().getAnnotations() != null && dep.getMetadata().getAnnotations().containsKey("nephele/labels")) {
                            labels = dep.getMetadata().getAnnotations().get("nephele/labels").toString();
                        }
                        if (!StringUtils.isEmpty(labels)) {
                            String[] arrLabel = labels.split(",");
                            for (String l : arrLabel) {
                                String[] tmp = l.split("=");
                                labeljson.put(tmp[0], tmp[1]);
                            }
                            json.put("labels", labeljson);
                        }
                        // get status
                        // deploment status
                        String status = null;
                        if (dep.getMetadata().getAnnotations() != null && dep.getMetadata().getAnnotations().containsKey("nephele/status")) {
                            status = dep.getMetadata().getAnnotations().get("nephele/status").toString();
                        }

                        if (!StringUtils.isEmpty(status)) {
                            switch (status) {
                                case Constant.STARTING :
                                    if (dep.getStatus().getReplicas() != null && dep.getStatus().getReplicas() > 0
                                            && (dep.getStatus().getAvailableReplicas() == dep.getStatus().getReplicas())) {
                                        json.put("status", Constant.SERVICE_START);
                                    } else {
                                        json.put("status", Constant.SERVICE_STARTING);
                                    }
                                    break;
                                case Constant.STOPPING :
                                    if (dep.getStatus().getAvailableReplicas() != null && dep.getStatus().getAvailableReplicas() > 0) {
                                        json.put("status", Constant.SERVICE_STOPPING);
                                    } else {
                                        json.put("status", Constant.SERVICE_STOP);
                                    }
                                    break;
                                default :
                                    if (dep.getStatus().getAvailableReplicas() != null && dep.getStatus().getAvailableReplicas() > 0) {
                                        json.put("status", Constant.SERVICE_START);
                                    } else {
                                        json.put("status", Constant.SERVICE_STOP);
                                    }
                                    break;
                            }
                        } else {
                            if (dep.getStatus().getAvailableReplicas() != null && dep.getStatus().getAvailableReplicas() > 0) {
                                json.put("status", Constant.SERVICE_START);
                            } else {
                                json.put("status", Constant.SERVICE_STOP);
                            }
                        }
                        // get version
                        if (dep.getMetadata().getAnnotations() != null && dep.getMetadata().getAnnotations().containsKey("deployment.kubernetes.io/revision")) {
                            json.put("version", "v" + dep.getMetadata().getAnnotations().get("deployment.kubernetes.io/revision"));
                        }
                        // get image
                        List<String> img = new ArrayList<String>();
                        List<Container> containers = dep.getSpec().getTemplate().getSpec().getContainers();
                        for (Container container : containers) {
                            img.add(container.getImage());
                        }
                        json.put("img", img);
                        json.put("instance", dep.getSpec().getReplicas());
                        json.put("createTime", dep.getMetadata().getCreationTimestamp());
                        json.put("namespace", dep.getMetadata().getNamespace());
                        json.put("selector", dep.getSpec().getSelector());
                    }
                    serarray.add(json);
                }
                js.put("serviceList", serarray);
            } else {
                return ActionReturnUtil.returnErrorWithMsg("service is null");
            }
        } else {
            return ActionReturnUtil.returnSuccessWithData(null);
        }
        return ActionReturnUtil.returnSuccessWithData(js);
    }

    /**
     * deployment application service implement.
     * 
     * @author yanli
     * 
     * @param businessDeploy
     *            BusinessDeployBean
     * @param username
     *            username
     * @return ActionReturnUtil
     */
    @Override
    public synchronized ActionReturnUtil deployBusinessTemplate(BusinessDeployDto businessDeploy, String username, Cluster cluster, String tenantId) throws Exception {
        // check value
        if (StringUtils.isEmpty(username) || businessDeploy == null || businessDeploy.getBusinessTemplate().getServiceList().size() <= 0) {
            return ActionReturnUtil.returnErrorWithMsg("username , application deploy or service is null");
        }
        String namespace = businessDeploy.getNamespace();
        //获取nodeslector
		ActionReturnUtil labRes = namespaceService.getPrivatePartitionLabel(tenantId, namespace);
		if(!labRes.isSuccess()){
			return labRes;
		}
        Business business = new Business();
        // application info
        business.setName(businessDeploy.getName());
        business.setTemplateId(businessDeploy.getBusinessTemplate().getId());
        business.setNamespaces(businessDeploy.getNamespace());
        business.setDetails(businessDeploy.getBusinessTemplate().getDesc());
        business.setCreateTime(new Date());
        business.setUser(username);
        business.setTenant(businessDeploy.getBusinessTemplate().getTenant());
        // insert into application
        businessMapper.insert(business);
		List<Map<String, Object>> message = new ArrayList<>();
        // loop businessTemplate
        for (ServiceTemplateDto service : businessDeploy.getBusinessTemplate().getServiceList()) {
        	service.getDeploymentDetail().setNodeSelector((String)labRes.get("data"));
            com.harmonycloud.dao.application.bean.Service svc = new com.harmonycloud.dao.application.bean.Service();
            // is external service
            if (service.getExternal() == Constant.EXTERNAL_SERVICE) {
                svc.setBusinessId(business.getId());
                ServiceTemplates externalservice=serviceTemplatesMapper.getExternalService(service.getName());
                if(externalservice!=null){
                    svc.setServiceTemplateId(externalservice.getId());
                }
                svc.setIsExternal(Constant.EXTERNAL_SERVICE);
                //svc.setName(service.getDeploymentDetaile().getName());
                svc.setName(service.getName());
                svc.setNamespace(namespace);
                serviceMapper.insertService(svc);
                continue;
            }
            // todo retry and rollback
            List<String> pvcList = new ArrayList<>();
            // creat pvc
            for (CreateContainerDto c : service.getDeploymentDetail().getContainers()) {
                if (c.getStorage() != null) {
                    for (CreateVolumeDto pvc : c.getStorage()) {
                    	if(pvc.getType() != null && Constant.VOLUME_TYPE_PV.equals(pvc.getType())){
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
                    if ("HTTP".equals(ingress.getType()) && !StringUtils.isEmpty(ingress.getParsedIngressList().getName())) {
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
                        ingresses.add("{\"type\":\"HTTP\",\"name\":\"" + ingress.getParsedIngressList().getName() + "\"}");
                    } else if ("TCP".equals(ingress.getType()) && !StringUtils.isEmpty(ingress.getSvcRouter().getName())) {
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
			ActionReturnUtil depRes = deploymentsService.createDeployment(service.getDeploymentDetail(), username, null,
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
            svc.setBusinessId(business.getId());
            svc.setNamespace(namespace);
            svc.setIsExternal(0);

            serviceMapper.insertService(svc);
        }
        // update application template isdeploy
        businessTemplatesMapper.updateDeployById(businessDeploy.getBusinessTemplate().getId());

        if (message.size() > 0) {
            return ActionReturnUtil.returnErrorWithData(message);
        }
        return ActionReturnUtil.returnSuccess();
    }

    /**
     * delete application service implement
     * 
     * @author yanli
     * 
     * @param businessList
     *            application id list
     * @param username
     *            username
     * @return ActionReturnUtil
     */
    @Override
    public ActionReturnUtil deleteBusinessTemplate(BusinessList businessList, String username) throws Exception {
        // check value
        if (businessList == null || businessList.getIdList().size() <= 0) {
            return ActionReturnUtil.returnErrorWithMsg("businessList is null");
        }
        List<String> errorMessage = new ArrayList<>();
        Cluster cluster = tenantService.getClusterByTenantid(businessList.getTenantId());
        // loop businessTemplate
        for (int id : businessList.getIdList()) {
            boolean businessFlag = true;
            Business business = businessMapper.selectByPrimaryKey(id);
            if (business != null) {
                List<com.harmonycloud.dao.application.bean.Service> services = serviceMapper.selectByBusinessId(business.getId());
                if (services != null) {
                    List<Integer> idList = new ArrayList<>();
                    for (com.harmonycloud.dao.application.bean.Service service : services) {
                        boolean serviceFlag = true;
                        // is external service
                        if (service.getIsExternal() == 1) {
                            // delete service db
                            idList.add(service.getId());
                            continue;
                        }
                        // delete config map & deploy service deployment
                        ActionReturnUtil deleteDeployReturn = deploymentsService.deleteDeployment(service.getName(), business.getNamespaces(), username, cluster);
                        if (!deleteDeployReturn.isSuccess()) {
                            serviceFlag = false;
                            businessFlag = false;
                            errorMessage.add(business.getName() + "." + service.getName());
                        }


                        //todo sooooooooooooooooooooooo bad
                        // delete ingress
                        Map<String, Object> labelMap = new HashMap<String, Object>();
                        ParsedIngressListDto parsedIngressListDto = new ParsedIngressListDto();
                        parsedIngressListDto.setNamespace(business.getNamespaces());
                        labelMap.put("app",service.getName());
                        parsedIngressListDto.setLabels(labelMap);
                        List<RouterSvc> routerSvcs = new ArrayList<RouterSvc>();
                        routerSvcs = routerService.listIngressByName(parsedIngressListDto);
                        if (routerSvcs !=null && routerSvcs.size() > 0){
                            for (RouterSvc svcone:routerSvcs){
                                if ("HTTP".equals(svcone.getLabels().get("type"))) {
                                    routerService.ingDelete(business.getNamespaces(), svcone.getName());
                                    routerService.svcDelete(business.getNamespaces(), svcone.getName());
                                } else if ("TCP".equals(svcone.getLabels().get("type"))) {
                                    routerService.svcDelete(business.getNamespaces(), svcone.getName());
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
                                        routerService.ingDelete(business.getNamespaces(), name);
                                        routerService.svcDelete(business.getNamespaces(), "routersvc" + name);
                                    } else if ("TCP".equals(type)) {
                                        routerService.svcDelete(business.getNamespaces(), "routersvc" + name);
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
                                    url.setName(pvc).setNamespace(business.getNamespaces()).setResource(Resource.PERSISTENTVOLUMECLAIM);
                                    Map<String, Object> headers = new HashMap<>();
                                    headers.put("Content-Type", "application/json");
                                    Map<String, Object> bodys = new HashMap<>();
                                    bodys.put("gracePeriodSeconds", 1);
                                    K8SClientResponse response = new K8SClient().doit(url, HTTPMethod.DELETE, headers, bodys,cluster);
                                    if (!HttpStatusUtil.isSuccessStatus(response.getStatus()) && response.getStatus() != Constant.HTTP_404) {
                                        serviceFlag = false;
                                        businessFlag = false;
                                        errorMessage.add(response.getBody());
                                    }

                                    // update pv
                                    if (response.getStatus() != Constant.HTTP_404 && pvc.contains(Constant.PVC_BREAK)) {
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
                                            K8SClientResponse responsePV = new K8SClient().doit(urlPV, HTTPMethod.PUT, headersPV, bodysPV,cluster);
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
                }
                // delete application db
                if (businessFlag) {
                    businessMapper.deleteBusinessById(business.getId());
                }
            }

        }

        if (errorMessage.size() > 0) {
            return ActionReturnUtil.returnErrorWithData(errorMessage);
        }
        return ActionReturnUtil.returnSuccess();

    }

    /**
     * stop application service implement
     * 
     * @author yanli
     * 
     * @param businessList
     *            BusinessListBean application id list
     * @param username
     *            username
     * @return ActionReturnUtil
     */
    @Override
    public ActionReturnUtil stopBusinessTemplate(BusinessList businessList, String username, Cluster cluster) throws Exception {

        if (businessList == null || businessList.getIdList().size() <= 0) {
            return ActionReturnUtil.returnErrorWithMsg("businessList is null");
        }

        List<String> errorMessage = new ArrayList<>();

        // loop every service
        for (int id : businessList.getIdList()) {
            Business business = businessMapper.selectByPrimaryKey(id);
            List<com.harmonycloud.dao.application.bean.Service> services = serviceMapper.selectByBusinessId(business.getId());
            if (services != null && services.size() > 0) {
                for (com.harmonycloud.dao.application.bean.Service service : services) {
                    if (service.getIsExternal() == 1) {
                        continue;
                    }
                    ActionReturnUtil stopDeployReturn = deploymentsService.stopDeployments(service.getName(), business.getNamespaces(), username, cluster);
                    if (!stopDeployReturn.isSuccess()) {
                        errorMessage.add(business.getName() + "." + service.getName());
                    }
                }
            }
        }

        if (errorMessage.size() > 0) {
            return ActionReturnUtil.returnErrorWithData(errorMessage);
        }
        return ActionReturnUtil.returnSuccess();
    }

    /**
     * start application service on 17/04/11.
     * 
     * @author yanli
     * 
     * @param businessList
     *            BusinessListBean application id list
     * @param username
     *            username
     * @return ActionReturnUtil
     */
    @Override
    public ActionReturnUtil startBusinessTemplate(BusinessList businessList, String username, Cluster cluster) throws Exception {

        if (businessList == null || businessList.getIdList().size() <= 0) {
            return ActionReturnUtil.returnErrorWithMsg("businessList is null");
        }

        List<String> errorMessage = new ArrayList<>();

        // loop every service
        for (int id : businessList.getIdList()) {
            Business business = businessMapper.selectByPrimaryKey(id);
            List<com.harmonycloud.dao.application.bean.Service> services = serviceMapper.selectByBusinessId(business.getId());
            if (services != null && services.size() > 0) {
                for (com.harmonycloud.dao.application.bean.Service service : services) {
                    if (service.getIsExternal() == 1) {
                        continue;
                    }
                    ActionReturnUtil startDeployReturn = deploymentsService.startDeployments(service.getName(), business.getNamespaces(), username, cluster);
                    if (!startDeployReturn.isSuccess()) {
                        errorMessage.add(business.getName() + "." + service.getName());
                    }
                }
            }
        }

        if (errorMessage.size() > 0) {
            return ActionReturnUtil.returnErrorWithData(errorMessage);
        }
        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public ActionReturnUtil selectPv(String tenantId, String namespace, int status) throws Exception {
        K8SURL url = new K8SURL();
        url.setResource(Resource.PERSISTENTVOLUME);
        Map<String, Object> bodys = new HashMap<>();
        JSONArray array = new JSONArray();
        if (StringUtils.isEmpty(namespace)) {
            if (!StringUtils.isEmpty(tenantId)) {
                // select by tenantid
                String label = "nephele_tenantid=" + tenantId;
                bodys.put("labelSelector", label);
            } else {
                bodys = null;
            }
        } else {
            bodys = null;
            url.setNamespace(namespace);
        }
        Cluster cluster = tenantService.getClusterByTenantid(tenantId);
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET, null, bodys,cluster);
        if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            PersistentVolumeList persistentVolumeList = K8SClient.converToBean(response, PersistentVolumeList.class);
            List<PersistentVolume> items = persistentVolumeList.getItems();
            if (items != null && items.size() > 0) {
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
                            pvDto.setUsage("none");
                        } else {
                            pvDto.setBind(pv.getSpec().getClaimRef());
                            // 设置usage
                            pvDto.setUsage(pv.getSpec().getClaimRef().getName());
                        }
                        // 设置容量
                        @SuppressWarnings("unchecked")
                        Map<String, String> capacity = (Map<String, String>) pv.getSpec().getCapacity();
                        pvDto.setCapacity(capacity.get("storage"));
                        // 设置pv名称
                        pvDto.setName(pv.getMetadata().getName());
                        // 设置time
                        pvDto.setTime(pv.getMetadata().getCreationTimestamp());
                        // 设置type
                        pvDto.setType("nfs");
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
                            String namespaces = pv.getSpec().getClaimRef().getNamespace();
                            // 将namespace处理为tenantname
                            String[] split = namespaces.split("-");
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
                        if (status == 0) {
                            // all
                            array.add(pvDto);
                        } else if (status == 1) {
                            // used
                            if (!pvDto.getBind().equals(false)) {
                                array.add(pvDto);
                            }
                        } else {
                            // unused
                            if (pvDto.getBind().equals(false) || pvDto.getUsage().equals("none")) {
                                array.add(pvDto);
                            }
                        }
                    }
                }
            }
        }

        return ActionReturnUtil.returnSuccessWithData(array);
    }

    /**
     * get application by id service implement.
     * 
     * @author gurongyun
     * 
     * @param tenant
     *            tenant name
     * @return ActionReturnUtil
     */
    @Override
    public ActionReturnUtil searchSumBusiness(String [] tenant, String clusterId) throws Exception {
        JSONObject json = new JSONObject();
        // search application
        String [] namespace=new String[0];
        List<Business> blist = businessMapper.search(tenant, namespace, null);
        // number of application
        int count = 0;
        // number of application 正常
        int normal = 0;
        // number of application 异常
        int abnormal = 0;
        Cluster cluster=clusterService.findClusterById(clusterId);
        if (blist != null && blist.size() > 0) {
            count = blist.size();
            for (Business bs : blist) {
            	JSONObject servicejson=listServiceByBusinessId(bs.getId(), bs.getNamespaces(), cluster);
            	if(NORMAL.equals(servicejson.get("status"))){
            		normal ++ ;
            	}else{
            		abnormal ++ ;
            	}
            	
            }
        } else {
            return ActionReturnUtil.returnSuccessWithData(null);
        }
        json.put("normal", normal);
        json.put("abnormal", abnormal);
        json.put("count", count);
        return ActionReturnUtil.returnSuccessWithData(json);
    }

/*    private static Date stringToDate(String strTime)
            throws Exception {
        String formatType = "yyyy-MM-dd";
        SimpleDateFormat formatter = new SimpleDateFormat(formatType);
        Date date = null;
        date = formatter.parse(strTime);
        return date;
    }
*/
    private static String dateToString(Date time){
        SimpleDateFormat formatter;
        formatter = new SimpleDateFormat("yyyy-MM-dd");
        String ctime = formatter.format(time);

        return ctime;
    }

	@Override
	public ActionReturnUtil deleteBusinessByNamespace(String namespace) throws Exception {
		if(!StringUtils.isEmpty(namespace)){
			businessMapper.deleteBusinessByNamespace(namespace);
			return ActionReturnUtil.returnSuccess();
		}else{
			return ActionReturnUtil.returnErrorWithMsg("namespace为空");
		}
		
	}

	@Override
	public ActionReturnUtil searchSum(String[] tenant) throws Exception {
        // search application
		String [] namespace=new String[0];
        List<Business> blist = businessMapper.search(tenant, namespace, null);
        // number of application
        int count = 0;
        if (blist != null && blist.size() > 0) {
            count = blist.size();
        }
		return ActionReturnUtil.returnSuccessWithData(count);
	}
	
	private JSONObject listServiceByBusinessId(int businessId, String namespace, Cluster cluster) throws Exception {
		JSONObject json = new JSONObject();
        // number of application running
        int start = 0;
        // number of deploment
        int total = 0;
		List<com.harmonycloud.dao.application.bean.Service> services = serviceMapper.selectByBusinessId(businessId);
        if (services != null && services.size() > 0) {
        	total = services.size();
        	K8SURL url = new K8SURL();
        	url.setNamespace(namespace).setResource(Resource.DEPLOYMENT);
    		K8SClientResponse depRes = new K8SClient().doit(url, HTTPMethod.GET, null, null,cluster);
			if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus())
					&& depRes.getStatus() != Constant.HTTP_404 ) {
				JSONObject js = JSONObject.fromObject(depRes.getBody());
				K8sResponseBody k8sresbody = (K8sResponseBody) JSONObject.toBean(js, K8sResponseBody.class);
				return (JSONObject) json.put("获取k8sDeployment错误", k8sresbody.getMessage());
			}
			DeploymentList deplist = JsonUtil.jsonToPojo(depRes.getBody(), DeploymentList.class);
			if(deplist != null && deplist.getItems() != null ){
				List<Deployment> deps = deplist.getItems();
				if(deps != null && deps.size() > 0){
					for (com.harmonycloud.dao.application.bean.Service service : services) {
		                if (service.getIsExternal() == 1) {
		                	start++;
		                }else{
		                	for(Deployment dep : deps){
		                		if (dep != null && service.getName().equals(dep.getMetadata().getName())) {
									String status = getDeploymentStatus(dep);
									if (Constant.START.equals(status)) {
										start++;
									}
								}
		                	}
		                }
		                
		            }
				}
			}
        }
        if (start == total && total != 0) {
        	json.put("status", NORMAL);
        } else {
        	json.put("status", ABNORMAL);
        }
        json.put("start", start);
        json.put("total", total);
		return json;
		
	}
	private String getDeploymentStatus(Deployment dep) throws Exception {
		String status = null;
		String flag = Constant.START;
        if (dep.getMetadata().getAnnotations() != null && dep.getMetadata().getAnnotations().containsKey("nephele/status")) {
            status = dep.getMetadata().getAnnotations().get("nephele/status").toString();
        }
        if (!StringUtils.isEmpty(status)) {
            switch (status) {
                case Constant.STARTING :
                    if (dep.getStatus().getAvailableReplicas() != null && dep.getStatus().getReplicas() > 0 && (dep.getStatus().getAvailableReplicas() == dep.getStatus().getReplicas())) {
                        flag = Constant.START;
                    } else {
                    	flag = status;
                    }
                    break;
                case Constant.STOPPING :
                    if (dep.getStatus().getAvailableReplicas() != null && dep.getStatus().getAvailableReplicas() > 0) {
                    	flag = status;
                    } else {
                    	flag = Constant.STOP;
                    }
                    break;
                default :
                    if (dep.getStatus().getAvailableReplicas() != null && dep.getStatus().getAvailableReplicas() > 0) {
                    	flag = Constant.START;
                    } else {
                    	flag = Constant.STOP;
                    }
                    break;
            }
        } else {
            if (dep.getStatus().getAvailableReplicas() != null && dep.getStatus().getAvailableReplicas() > 0) {
            	flag = Constant.START;
            } else {
            	flag = Constant.STOP;
            }
        }
		return flag;
		
	}

	@Override
	public ActionReturnUtil deployBusinessTemplateByName(String tenantId, String name, String businessame, String tag, String namespace, String userName, Cluster cluster)
			throws Exception {
		if(!StringUtils.isEmpty(name) && !StringUtils.isEmpty(tag) && !StringUtils.isEmpty(namespace)){
			//根据name和tag获取模板信息
			ActionReturnUtil btresponse = businessService.getBusinessTemplate(name, tag);
			if(!btresponse.isSuccess()){
				return ActionReturnUtil.returnErrorWithMsg("业务模板获取失败");
			}
			JSONObject json = (JSONObject) btresponse.get("data");
			/*ActionReturnUtil privatePartitionLabel = this.privatePartitionService.getPrivatePartitionLabel(tenantId, namespace);
			if(!privatePartitionLabel.isSuccess()){
				return privatePartitionLabel;
			}
			String nodeSelector = (String) privatePartitionLabel.get("data");*/
			BusinessDeployDto businessDeploy = new BusinessDeployDto();
			businessDeploy.setName(businessame);
			businessDeploy.setNamespace(namespace);
			BusinessTemplateDto businessTemplate = new BusinessTemplateDto();
			businessTemplate.setDesc(json.getString("desc"));
			businessTemplate.setId(json.getInt("id"));
			businessTemplate.setName(json.getString("name"));
			businessTemplate.setTenant(json.getString("tenant"));
			//应用模板list
			JSONArray stList = json.getJSONArray("servicelist");
			List<ServiceTemplateDto> servicelist = new LinkedList<ServiceTemplateDto>();
			if(stList != null && stList.size() > 0){
				for(int i=0; i < stList.size(); i++){
					ServiceTemplateDto serviceTemplate = new ServiceTemplateDto();
					JSONObject js = stList.getJSONObject(i);
					serviceTemplate.setId(js.getInt("id"));
					serviceTemplate.setName(js.getString("name"));
					serviceTemplate.setTag(js.getString("tag"));
					serviceTemplate.setDesc(js.getString("details"));
					serviceTemplate.setExternal(js.getInt("isExternal"));
					if(js.getInt("isExternal") == Constant.K8S_SERVICE){
						String dep=js.getJSONArray("deployment").getJSONObject(0).toString().replaceAll(":\"\",", ":"+null+",").replaceAll(":\"\"", ":"+null+"");
						DeploymentDetailDto deployment = JsonUtil.jsonToPojo(dep, DeploymentDetailDto.class);
						deployment.setNamespace(namespace);
						/*deployment.setNodeSelector(nodeSelector);*/
						serviceTemplate.setDeploymentDetail(deployment);
					}
					if(!StringUtils.isEmpty(js.getString("ingress"))){
						JSONArray jsarray = js.getJSONArray("ingress");
						List<IngressDto> ingress = new LinkedList<IngressDto>();
						if(jsarray != null && jsarray.size() > 0 ){
							for(int j = 0; j < jsarray.size(); j++){
								JSONObject ingressJson = jsarray.getJSONObject(j);
								IngressDto ing = JsonUtil.jsonToPojo(ingressJson.toString().toString().replaceAll(":\"\",", ":"+null+",").replaceAll(":\"\"", ":"+null+""), IngressDto.class);
								ingress.add(ing);
							}
						}
						serviceTemplate.setIngress(ingress);
					}
					servicelist.add(serviceTemplate);
				}
			}else{
				return ActionReturnUtil.returnErrorWithMsg("该业务模板没有应用模板");
			}
			businessTemplate.setServiceList(servicelist);
			businessDeploy.setBusinessTemplate(businessTemplate);
			JSONObject msg= new JSONObject();
			//check name
			//service name
			K8SURL url = new K8SURL();
        	url.setNamespace(namespace).setResource(Resource.DEPLOYMENT);
    		K8SClientResponse depRes = new K8SClient().doit(url, HTTPMethod.GET, null, null,cluster);
			if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus())
					&& depRes.getStatus() != Constant.HTTP_404 ) {
				JSONObject js = JSONObject.fromObject(depRes.getBody());
				K8sResponseBody k8sresbody = (K8sResponseBody) JSONObject.toBean(js, K8sResponseBody.class);
				return ActionReturnUtil.returnErrorWithMsg(k8sresbody.getMessage());
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
			for(ServiceTemplateDto std : servicelist){
				//check service name
				if(std.getDeploymentDetail() != null && deps != null && deps.size() > 0){
					for(Deployment dep : deps){
						if(std.getDeploymentDetail().getName().equals(dep.getMetadata().getName())){
							msg.put("服务名称:"+std.getDeploymentDetail().getName(), "重复");
							flag = false;
						}
					}
				}
				//check ingress
				if(std.getIngress() != null && std.getIngress().size() > 0){
					for(IngressDto ing : std.getIngress()){
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
			}
			if(flag){
				//发布
				return deployBusinessTemplate(businessDeploy, userName, cluster, tenantId);
			}else{
				return ActionReturnUtil.returnErrorWithData(msg);
			}
		}else{
			return ActionReturnUtil.returnErrorWithMsg("业务模板名称或者版本号或者分区为空");
		}
	}

	@Override
	public ActionReturnUtil addAndDeployBusinessTemplate(BusinessDeployDto businessDeploy, String username, String tenantid,
			Cluster cluster) throws Exception {
		// check value
        if (StringUtils.isEmpty(username) || businessDeploy == null || businessDeploy.getBusinessTemplate().getServiceList().size() <= 0) {
            return ActionReturnUtil.returnErrorWithMsg("username , application deploy or service is null");
        }
        //保存数据库
        for( ServiceTemplateDto s: businessDeploy.getBusinessTemplate().getServiceList()){
        	ActionReturnUtil res = serviceService.saveServiceTemplate(s, username, 1);
        	if(!res.isSuccess()){
        		return res;
        	}
        	JSONObject js = (JSONObject) res.get("data");
        	int id = js.getInt(s.getName());
        	s.setId(id);
        }
        //获取k8s同namespace相关的资源
		//获取 Deployment name
        JSONObject msg= new JSONObject();
        if(StringUtils.isEmpty(businessDeploy.getNamespace())){
        	return ActionReturnUtil.returnErrorWithMsg("namespace为空");
        }
        String namespace = businessDeploy.getNamespace();
		K8SURL url = new K8SURL();
    	url.setNamespace(namespace).setResource(Resource.DEPLOYMENT);
		K8SClientResponse depRes = new K8SClient().doit(url, HTTPMethod.GET, null, null,cluster);
		if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus())
				&& depRes.getStatus() != Constant.HTTP_404 ) {
			JSONObject js = JSONObject.fromObject(depRes.getBody());
			K8sResponseBody k8sresbody = (K8sResponseBody) JSONObject.toBean(js, K8sResponseBody.class);
			return ActionReturnUtil.returnErrorWithMsg(k8sresbody.getMessage());
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
		//获取nodeslector
		ActionReturnUtil labRes = namespaceService.getPrivatePartitionLabel(tenantid, namespace);
		if(!labRes.isSuccess()){
			return labRes;
		}
		for(ServiceTemplateDto std : businessDeploy.getBusinessTemplate().getServiceList()){
			//check service name
			if(std.getDeploymentDetail() != null && deps != null && deps.size() > 0){
				std.getDeploymentDetail().setNamespace(namespace);
				std.getDeploymentDetail().setNodeSelector((String)labRes.get("data"));
				for(Deployment dep : deps){
					if(std.getDeploymentDetail().getName().equals(dep.getMetadata().getName())){
						msg.put("服务名称:"+std.getDeploymentDetail().getName(), "重复");
						flag = false;
					}
				}
			}
			//check ingress
			if(std.getIngress() != null && std.getIngress().size() > 0){
				for(IngressDto ing : std.getIngress()){
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
		}
		
		if(flag){
			List<Map<String, Object>> message = new ArrayList<>();
			for (ServiceTemplateDto service : businessDeploy.getBusinessTemplate().getServiceList()) {
				com.harmonycloud.dao.application.bean.Service svc = new com.harmonycloud.dao.application.bean.Service();
	            // is external service
	            if (service.getExternal() == Constant.EXTERNAL_SERVICE) {
	                svc.setBusinessId(businessDeploy.getBusinessTemplate().getBusinessId());
	                ServiceTemplates externalservice=serviceTemplatesMapper.getExternalService(service.getName());
	                if(externalservice!=null){
	                    svc.setServiceTemplateId(externalservice.getId());
	                }
	                svc.setIsExternal(Constant.EXTERNAL_SERVICE);
	                svc.setNamespace(businessDeploy.getNamespace());
	                svc.setName(service.getName());
	                serviceMapper.insertService(svc);
	                continue;
	            }
	            // todo retry and rollback
	            List<String> pvcList = new ArrayList<>();
	            // creat pvc
	            for (CreateContainerDto c : service.getDeploymentDetail().getContainers()) {
	                if (c.getStorage() != null) {
	                    for (CreateVolumeDto pvc : c.getStorage()) {
	                    	if(pvc.getType() != null && Constant.VOLUME_TYPE_PV.equals(pvc.getType())){
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
	                    if ("HTTP".equals(ingress.getType()) && !StringUtils.isEmpty(ingress.getParsedIngressList().getName())) {
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
    						ingresses.add(
    								"{\"type\":\"HTTP\",\"name\":\"" + ingress.getParsedIngressList().getName() + "\"}");
	                    } else if ("TCP".equals(ingress.getType()) && !StringUtils.isEmpty(ingress.getSvcRouter().getName())) {
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
	            try {
	                //todo so bad
	                service.getDeploymentDetail().setNamespace(namespace);
	                for (CreateContainerDto c:service.getDeploymentDetail().getContainers()){
	                    String[] hou;
	                    String[] qian;
	                    String images;

	                    if (harborUrl.contains("//") && harborUrl.contains(":")) {
	                        hou = harborUrl.split("//");
	                        qian = hou[1].split(":");
	                        images = qian[0]+"/" + c.getImg();
	                    } else {
	                        images = harborUrl;
	                    }
	                    c.setImg(images);
	                }
	                deploymentsService.createDeployment(service.getDeploymentDetail(), username, businessDeploy.getBusinessTemplate().getName(), cluster);
	            } catch (Exception e) {
	            	msg.put("Deployment:", service.getDeploymentDetail().getName());
	            }

	            if (pvcList.size() > 0) {
	                JSONArray jsonArraypvc = JSONArray.fromObject(pvcList);
	                svc.setPvc(jsonArraypvc.toString());
	            }
	            svc.setName(service.getDeploymentDetail().getName());
	            svc.setServiceTemplateId(service.getId());
	            svc.setNamespace(namespace);
	            svc.setBusinessId(businessDeploy.getBusinessTemplate().getBusinessId());
	            svc.setIsExternal(0);
	            serviceMapper.insertService(svc);
			}
			if (message.size() > 0) {
	            return ActionReturnUtil.returnErrorWithData(message);
	        }
		}else{
			return ActionReturnUtil.returnErrorWithData(msg);
		}
        return ActionReturnUtil.returnSuccess();
	}
	
	@Override
	public ActionReturnUtil checkK8SName(BusinessDeployDto businessDeploy, Cluster cluster)throws Exception {
		//获取k8s同namespace相关的资源
		//获取 Deployment name
        JSONObject msg= new JSONObject();
        if(StringUtils.isEmpty(businessDeploy.getNamespace())){
        	return ActionReturnUtil.returnErrorWithMsg("namespace为空");
        }
        String namespace = businessDeploy.getNamespace();
		K8SURL url = new K8SURL();
    	url.setNamespace(namespace).setResource(Resource.DEPLOYMENT);
		K8SClientResponse depRes = new K8SClient().doit(url, HTTPMethod.GET, null, null,cluster);
		if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus())
				&& depRes.getStatus() != Constant.HTTP_404 ) {
			JSONObject js = JSONObject.fromObject(depRes.getBody());
			K8sResponseBody k8sresbody = (K8sResponseBody) JSONObject.toBean(js, K8sResponseBody.class);
			return ActionReturnUtil.returnErrorWithMsg(k8sresbody.getMessage());
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
		
		for(ServiceTemplateDto std : businessDeploy.getBusinessTemplate().getServiceList()){
			//check service name
			if(std.getDeploymentDetail() != null && deps != null && deps.size() > 0){
				std.getDeploymentDetail().setNamespace(namespace);
				for(Deployment dep : deps){
					if(std.getDeploymentDetail().getName().equals(dep.getMetadata().getName())){
						msg.put("服务名称:"+std.getDeploymentDetail().getName(), "重复");
						flag = false;
					}
				}
			}
			//check ingress
			if(std.getIngress() != null && std.getIngress().size() > 0){
				for(IngressDto ing : std.getIngress()){
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
		}
		if(flag){
			return ActionReturnUtil.returnSuccess();
		}else{
			return ActionReturnUtil.returnErrorWithData(msg);
		}
	 }
}
