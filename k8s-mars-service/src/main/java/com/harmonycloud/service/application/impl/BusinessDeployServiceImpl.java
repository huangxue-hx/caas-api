package com.harmonycloud.service.application.impl;

import com.harmonycloud.common.util.*;
import com.harmonycloud.dao.application.*;
import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.dao.tenant.TenantBindingMapper;
import com.harmonycloud.dao.tenant.bean.TenantBinding;
import com.harmonycloud.dao.tenant.bean.TenantBindingExample;
import com.harmonycloud.dto.business.*;
import com.harmonycloud.k8s.bean.*;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.service.*;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import com.harmonycloud.service.application.BusinessDeployService;
import com.harmonycloud.service.application.DeploymentsService;
import com.harmonycloud.service.application.RouterService;
import com.harmonycloud.service.application.ServiceService;
import com.harmonycloud.service.application.VolumeSerivce;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.cluster.LoadbalanceService;
import com.harmonycloud.service.platform.bean.BusinessList;
import com.harmonycloud.service.platform.bean.PvDto;
import com.harmonycloud.service.platform.bean.RouterSvc;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.tenant.NamespaceService;
import com.harmonycloud.service.tenant.PrivatePartitionService;
import com.harmonycloud.service.tenant.TenantService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Created by root on 4/10/17.
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class BusinessDeployServiceImpl implements BusinessDeployService {

    @Autowired
    private TenantService tenantService;

    @Autowired
    private ClusterService clusterService;

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
    private TprApplication tprApplication;

    @Autowired
    private DeploymentService dpService;

    @Autowired
    NamespaceService namespaceService;

    @Autowired
    PrivatePartitionService privatePartitionService;

    @Autowired
    HttpSession session;

    @Autowired
    ServiceService serviceService;

    @Autowired
    private PVCService pvcService;

    @Autowired
    private HarborUtil harborUtil;
    
    @Autowired
    private LoadbalanceService loadbalanceService;
    
    @Value("#{propertiesReader['kube.topo']}")
    private String kubeTopo;

    public static final String ABNORMAL = "0";
    public static final String NORMAL = "1";

    private static final String SIGN = "-";
    private static final String SIGN_EQUAL = "=";
    private final static String TOPO = "topo";
    private final static String CREATE = "creater";

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
        List<BaseResource> blist = new ArrayList<>();
        Cluster cluster=tenantService.getClusterByTenantid(tenantId);
        // search application
        if(StringUtils.isEmpty(namespace) && !StringUtils.isEmpty(tenantId)){

            @SuppressWarnings("unchecked")
            List<Object> namespaceData = (List<Object>) namespaceService.getNamespaceListByTenantid(tenantId).get("data");

            for (Object oneNamespace : namespaceData ){
                String namespaceName = String.valueOf(((Map)oneNamespace).get("name"));
                if(StringUtils.isBlank(namespaceName)){
                    continue;
                }
                K8SClientResponse response = tprApplication.listApplicationByNamespace(namespaceName,null, null, HTTPMethod.GET, cluster);
                if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
                    BaseResourceList  tpr = JsonUtil.jsonToPojo(response.getBody(), BaseResourceList.class);
                    if (tpr != null){
                        blist.addAll(tpr.getItems());
                    }
                }
            }

        }else{
            String [] namespaces = {namespace};
            if(namespace.contains(",")){
                namespaces=namespace.split(",");
            }

            for (String oneNamespace: namespaces){
                K8SClientResponse response = tprApplication.listApplicationByNamespace(oneNamespace,null, null, HTTPMethod.GET, cluster);
                if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
                    BaseResourceList  tpr = JsonUtil.jsonToPojo(response.getBody(), BaseResourceList.class);
                    if (tpr != null){
                        blist.addAll(tpr.getItems());
                    }
                }
            }
        }

        // number of application
        int count = 0;
        if (blist != null && blist.size() > 0) {
            count = blist.size();
            for (BaseResource bs : blist) {
                JSONObject js = new JSONObject();
                //Map<String,Object> appLable = new HashedMap();
                String label = "";

                for (Map.Entry<String, Object> vo : bs.getMetadata().getLabels().entrySet()) {

                    if (vo.getKey().startsWith(TOPO)){
                        //appLable.put(vo.getKey(),vo.getValue());
                        label = vo.getKey() + "=" + vo.getValue();
                    }

                }

                // put application info
                js.put("name", bs.getMetadata().getName());
                js.put("id", label);
                js.put("desc", bs.getMetadata().getAnnotations());
                js.put("namespace", bs.getMetadata().getNamespace());
                //获取最新更新时间
                String updateTime = getDeploymentTime(label, bs.getMetadata().getNamespace(), cluster);
                if(updateTime == null) {
                	updateTime = bs.getMetadata().getCreationTimestamp();
                }
                js.put("createTime", updateTime);
                js.put("tenant", tenant);
                js.put("user", bs.getMetadata().getLabels());

                JSONObject servicejson = listServiceByBusinessId(label, bs.getMetadata().getNamespace(),cluster);
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
     * 获取应用下的服务最新时间
     * */
    private String getDeploymentTime(String label, String namespace, Cluster cluster) throws Exception{
    	Map<String, Object> bodys = new HashMap<String, Object>();
		if (!checkParamNUll(label)) {
			bodys.put("labelSelector", label);
		}
		K8SURL url = new K8SURL();
		url.setResource(Resource.DEPLOYMENT).setNamespace(namespace);
		K8SClientResponse depRes = new K8sMachineClient().exec(url, HTTPMethod.GET, null, bodys,cluster);
		if(!HttpStatusUtil.isSuccessStatus(depRes.getStatus()) && depRes.getStatus() != Constant.HTTP_404){
			return null;
		}
		DeploymentList deployment = JsonUtil.jsonToPojo(depRes.getBody(), DeploymentList.class);
		if(deployment != null && deployment.getItems().size() > 0){
			List<Deployment> list = deployment.getItems();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
			String max = null;
			for(Deployment dep : list) {
				if(max == null) {
					max = dep.getMetadata().getCreationTimestamp();
				}
				String update = dep.getMetadata().getCreationTimestamp();
				if(dep.getStatus() != null && dep.getStatus().getConditions() != null && dep.getStatus().getConditions().size() > 0) {
					String maxc = null;
					for(DeploymentCondition  c : dep.getStatus().getConditions()) {
						if(maxc == null && c.getLastUpdateTime() != null) {
							maxc = c.getLastUpdateTime();
						}
						if(c.getLastUpdateTime() != null) {
							int b = Long.valueOf(sdf.parse(maxc).getTime()).compareTo(Long.valueOf(sdf.parse(c.getLastUpdateTime()).getTime()));
							if(b == -1) {
								maxc = c.getLastUpdateTime();
							}
						}
					}
					if(maxc != null) {
						update = maxc;
					}
				}
				if(dep.getMetadata() != null && dep.getMetadata().getAnnotations() != null) {
					Map<String, Object> anno = dep.getMetadata().getAnnotations();
					if(anno.containsKey("updateTimestamp") && anno.get("updateTimestamp") != null ) {
						String  updateTime =(String) anno.get("updateTimestamp");
						int c = Long.valueOf(sdf.parse(update).getTime()).compareTo(Long.valueOf(sdf.parse(updateTime).getTime()));
						if(c == -1) {
							update = updateTime;
						}
					}
				}
				int a = Long.valueOf(sdf.parse(max).getTime()).compareTo(Long.valueOf(sdf.parse(update).getTime()));
				if(a == -1) {
					max = update;
				}
			}
			return max;
		}
    	return null;
    }
    
    private boolean checkParamNUll(String p) {
		if (StringUtils.isEmpty(p)  || p == null) {
			return true;
		}
		return false;
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
    public ActionReturnUtil selectBusinessById(String id, Cluster cluster) throws Exception {
        if (StringUtils.isEmpty(id)) {
            return ActionReturnUtil.returnErrorWithMsg("application id is null");
        }
        JSONObject js = new JSONObject();
        // get application
        String[] namespace = {};
        String[] other = {};
        String appName = "";

        if (id.contains(SIGN) && id.contains(SIGN_EQUAL)){
            namespace = id.split(SIGN_EQUAL);

            other = namespace[0].split(SIGN);
            if (other.length >= 3){
                appName = id.substring(id.indexOf(SIGN,id.indexOf(SIGN)+1 )+1,id.indexOf(SIGN_EQUAL));
            }
        }

        BaseResource tpr = new BaseResource();
        K8SClientResponse response = tprApplication.getApplicationByName(namespace[1], appName, null, null, HTTPMethod.GET, cluster);
        if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            tpr = JsonUtil.jsonToPojo(response.getBody(), BaseResource.class);
        }


        if (tpr != null) {
            // put application info
            js.put("name", tpr.getMetadata().getName());
            js.put("createTime", tpr.getMetadata().getCreationTimestamp());
          //获取最新更新时间
            String updateTime = getDeploymentTime(id, tpr.getMetadata().getNamespace(), cluster);
            if(updateTime == null) {
            	updateTime = tpr.getMetadata().getCreationTimestamp();
            }
            js.put("updateTime", updateTime);
            String anno="";
            if (tpr.getMetadata().getAnnotations() != null && tpr.getMetadata().getAnnotations().containsKey("nephele/annotation")) {
                anno = tpr.getMetadata().getAnnotations().get("nephele/annotation").toString();
            }
            js.put("desc", anno);
            js.put("namespace", tpr.getMetadata().getNamespace());
            js.put("user", tpr.getMetadata().getLabels().get("creater"));
            js.put("id",id);


            Map<String, Object> bodys = new HashMap<>();
            bodys.put("labelSelector", id);
            JSONArray serarray = new JSONArray();

            K8SURL url = new K8SURL();
            url.setNamespace(namespace[1]).setResource(Resource.DEPLOYMENT);
            K8SClientResponse depRes = new K8sMachineClient().exec(url, HTTPMethod.GET, null, bodys, cluster);
            if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus())
                    && depRes.getStatus() != Constant.HTTP_404) {
            	UnversionedStatus sta = JsonUtil.jsonToPojo(depRes.getBody(), UnversionedStatus.class);
                return ActionReturnUtil.returnErrorWithMsg(sta.getMessage());
            }
            DeploymentList deplist = JsonUtil.jsonToPojo(depRes.getBody(), DeploymentList.class);
            if (deplist != null && deplist.getItems() != null) {

                List<Deployment> deps = deplist.getItems();
                if (deps != null && deps.size() > 0) {

                    for (Deployment dep : deps) {
                        JSONObject json = new JSONObject();
                        JSONObject labeljson = new JSONObject();

                        json.put("isExternal", "0");
                        json.put("name", dep.getMetadata().getName());

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
                                case Constant.STARTING:
                                    if (dep.getStatus().getReplicas() != null && dep.getStatus().getReplicas() > 0
                                            && (dep.getStatus().getAvailableReplicas() == dep.getStatus().getReplicas())) {
                                        json.put("status", Constant.SERVICE_START);
                                    } else {
                                        json.put("status", Constant.SERVICE_STARTING);
                                    }
                                    break;
                                case Constant.STOPPING:
                                    if (dep.getStatus().getAvailableReplicas() != null && dep.getStatus().getAvailableReplicas() > 0) {
                                        json.put("status", Constant.SERVICE_STOPPING);
                                    } else {
                                        json.put("status", Constant.SERVICE_STOP);
                                    }
                                    break;
                                default:
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
                        List<String> cpu = new ArrayList<String>();
                        List<String> memory = new ArrayList<String>();
                        List<Container> containers = dep.getSpec().getTemplate().getSpec().getContainers();
                        for (Container container : containers) {
                            img.add(container.getImage());
                            if(container.getResources() != null && container.getResources().getLimits() != null){
                                @SuppressWarnings("unchecked")
                                Map<String, String> res = (Map<String, String>) container.getResources().getLimits();
                                cpu.add(res.get("cpu"));
                                memory.add(res.get("memory"));
                            }
                        }
                        boolean isPV = false;
                        if(dep.getSpec().getTemplate().getSpec().getVolumes() != null && dep.getSpec().getTemplate().getSpec().getVolumes().size() > 0) {
        					for(Volume v : dep.getSpec().getTemplate().getSpec().getVolumes()) {
        						if(v.getPersistentVolumeClaim() != null) {
        							isPV = true;
        							break;
        						}
        					}
        				}
                        json.put("isPV", isPV);
                        json.put("img", img);
                        json.put("cpu", cpu);
                        json.put("memory", memory);
                        json.put("instance", dep.getSpec().getReplicas());
                        json.put("createTime", dep.getMetadata().getCreationTimestamp());
                        json.put("namespace", dep.getMetadata().getNamespace());
                        json.put("selector", dep.getSpec().getSelector());

                        serarray.add(json);

                    }
                }
            }

            //get external service by label
            K8SURL urlExternal = new K8SURL();
            urlExternal.setNamespace(Resource.EXTERNALNAMESPACE).setResource(Resource.SERVICE);
            K8SClientResponse serviceRe = new K8sMachineClient().exec(urlExternal, HTTPMethod.GET, null, bodys, cluster);
            if (!HttpStatusUtil.isSuccessStatus(serviceRe.getStatus())
                    && serviceRe.getStatus() != Constant.HTTP_404) {
                UnversionedStatus sta = JsonUtil.jsonToPojo(serviceRe.getBody(), UnversionedStatus.class);
                return ActionReturnUtil.returnErrorWithMsg(sta.getMessage());
            }
            ServiceList svclist = JsonUtil.jsonToPojo(serviceRe.getBody(), ServiceList.class);
            if (svclist != null && svclist.getItems() != null) {
                List<com.harmonycloud.k8s.bean.Service> svcs = svclist.getItems();
                if (svcs != null && svcs.size() > 0) {

                    for (com.harmonycloud.k8s.bean.Service svc : svcs) {
                        JSONObject json = new JSONObject();
                        // service info
                        json.put("isExternal", "1");
                        json.put("name", svc.getMetadata().getName());
                        json.put("ip", svc.getMetadata().getLabels().get("ip").toString());
                        json.put("port", svc.getSpec().getPorts().get(0).getTargetPort());
                        json.put("type", svc.getMetadata().getLabels().get("type").toString());
                        json.put("createTime", svc.getMetadata().getCreationTimestamp());
                        json.put("status", Constant.SERVICE_START);

                        serarray.add(json);
                    }
                }

            }

            js.put("serviceList", serarray);

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
            return ActionReturnUtil.returnErrorWithMsg("用户名 , 应用或者服务为空！");
        }
        String namespace = businessDeploy.getNamespace();

        String topoLabel = TOPO + SIGN + tenantId + SIGN + businessDeploy.getName();
        String namespaceLabel = businessDeploy.getNamespace();

        BaseResource base = new BaseResource();
        ObjectMeta mate = new ObjectMeta();
        mate.setNamespace(businessDeploy.getNamespace());
        mate.setName(businessDeploy.getName());

        Map<String, Object> anno = new HashMap<String, Object>();
        anno.put("nephele/annotation",businessDeploy.getBusinessTemplate().getDesc());
        mate.setAnnotations(anno);

        Map<String, Object> appLabels = new HashMap<String, Object>();
        appLabels.put(topoLabel,namespaceLabel);
        appLabels.put(CREATE ,username);
        mate.setLabels(appLabels);

        base.setMetadata(mate);
        ActionReturnUtil result = tprApplication.createApplication(base,cluster);

        if (!result.isSuccess()){
            return  result;
        }


        List<Map<String, Object>> message = new ArrayList<>();
        // loop businessTemplate
        if (businessDeploy.getBusinessTemplate() != null && businessDeploy.getBusinessTemplate().getServiceList().size() > 0) {
            for (ServiceTemplateDto svcTemplate : businessDeploy.getBusinessTemplate().getServiceList()) {
                // is external service
                if (svcTemplate.getExternal() == Constant.EXTERNAL_SERVICE) {
                    Map<String, Object> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    K8SClientResponse rsRes = sService.doSepcifyService(Constant.EXTERNAL_SERVICE_NAMESPACE, svcTemplate.getName(), null, null, HTTPMethod.GET, cluster);
                    if (!HttpStatusUtil.isSuccessStatus(rsRes.getStatus())) {
                        UnversionedStatus sta = JsonUtil.jsonToPojo(rsRes.getBody(), UnversionedStatus.class);
                        return ActionReturnUtil.returnErrorWithMsg(sta.getMessage());
                    }
                    com.harmonycloud.k8s.bean.Service service = JsonUtil.jsonToPojo(rsRes.getBody(), com.harmonycloud.k8s.bean.Service.class);
                    Map<String, Object> labels = service.getMetadata().getLabels();
                    labels.put(topoLabel,namespaceLabel);
                    service.getMetadata().setLabels(labels);
                    Map<String, Object> bodys = CollectionUtil.transBean2Map(service);
                    K8SClientResponse res = sService.doSepcifyService(Constant.EXTERNAL_SERVICE_NAMESPACE, service.getMetadata().getName(), headers, bodys, HTTPMethod.PUT, cluster);
                    if (!HttpStatusUtil.isSuccessStatus(res.getStatus())) {
                        UnversionedStatus sta = JsonUtil.jsonToPojo(res.getBody(), UnversionedStatus.class);
                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put("externalService:" + service.getMetadata().getName(), sta.getMessage());
                        message.add(map);
                    }

                } else {
                    // creat pvc
                    for (CreateContainerDto c : svcTemplate.getDeploymentDetail().getContainers()) {
                        if (c.getStorage() != null) {
                            for (CreateVolumeDto pvc : c.getStorage()) {
                                if(pvc == null){
                                    continue;
                                }
                                if (pvc.getType() != null && Constant.VOLUME_TYPE_PV.equals(pvc.getType())) {
                                    ActionReturnUtil pvcres = volumeSerivce.createVolume(namespace, pvc.getPvcName(),
                                            pvc.getPvcCapacity(), pvc.getPvcTenantid(), pvc.getReadOnly(), pvc.getPvcBindOne(),
                                            pvc.getVolume(),svcTemplate.getDeploymentDetail().getName());
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
                    if (svcTemplate.getIngress() != null) {
                        for (IngressDto ingress : svcTemplate.getIngress()) {
                            if ("HTTP".equals(ingress.getType()) && !StringUtils.isEmpty(ingress.getParsedIngressList().getName())) {
                                Map<String, Object> labels = new HashMap<String, Object>();
                                labels.put("app", svcTemplate.getDeploymentDetail().getName());
                                ingress.getParsedIngressList().setLabels(labels);
                                ingress.getParsedIngressList().setNamespace(namespace);
                                ActionReturnUtil httpIngRes = routerService.ingCreate(ingress.getParsedIngressList());
                                if (!httpIngRes.isSuccess()) {
                                    Map<String, Object> map = new HashMap<String, Object>();
                                    map.put("ingress:" + ingress.getParsedIngressList().getName(), httpIngRes.get("data"));
                                    message.add(map);
                                }
                            } else if ("TCP".equals(ingress.getType()) && !StringUtils.isEmpty(ingress.getSvcRouter().getName())) {
                                Map<String, Object> labels = new HashMap<String, Object>();
                                labels.put("app", svcTemplate.getDeploymentDetail().getName());
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
                    svcTemplate.getDeploymentDetail().setNamespace(namespace);
                    for (CreateContainerDto c : svcTemplate.getDeploymentDetail().getContainers()) {
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
                    ActionReturnUtil depRes = deploymentsService.createDeployment(svcTemplate.getDeploymentDetail(), username, businessDeploy.getName(),
                            cluster);
                    if (!depRes.isSuccess()) {
                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put(svcTemplate.getName(), depRes.get("data"));
                        message.add(map);
                    }
                }
            }
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
            return ActionReturnUtil.returnErrorWithMsg("应用列表为空！");
        }
        List<String> errorMessage = new ArrayList<>();
        Cluster cluster = tenantService.getClusterByTenantid(businessList.getTenantId());

        // loop businessTemplate
        for (String label : businessList.getIdList()) {
            String namespace = "";
            Map<String,Object> appBodys = new HashMap<String,Object>();
            appBodys.put("labelSelector", label);
            if (label != null && label.contains(SIGN_EQUAL)){
                String[] value = label.split(SIGN_EQUAL);
                if (value != null){
                    namespace = value[1];
                }
            }
            String business = "";
            if (label != null && label.contains(SIGN)){
                String[] value = label.split(SIGN);
                if (value != null){
                    business = value[2];
                }
            }

            K8SClientResponse appResponse = tprApplication.listApplicationByNamespace(namespace,null,appBodys,HTTPMethod.GET,cluster);
            if (!HttpStatusUtil.isSuccessStatus(appResponse.getStatus())) {
                UnversionedStatus sta = JsonUtil.jsonToPojo(appResponse.getBody(), UnversionedStatus.class);
                return ActionReturnUtil.returnErrorWithMsg(sta.getMessage());
            }
            BaseResourceList  tpr = JsonUtil.jsonToPojo(appResponse.getBody(), BaseResourceList.class);

            //check topo if have two unbind, if have one delete
            if (tpr != null && tpr.getItems() != null && tpr.getItems().size() > 0) {
                for(BaseResource br : tpr.getItems()){
                    if(br != null && br.getMetadata() != null && br.getMetadata().getName() != null){
                        boolean businessFlag = true;
                        List<Deployment> items = new ArrayList<>();
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> namespaceData = (List<Map<String, Object>>) namespaceService.getNamespaceListByTenantid(businessList.getTenantId()).get("data");

                        if (namespaceData != null && namespaceData.size() > 0){
                            for (Map<String, Object> oneNamespace : namespaceData ){

                                K8SURL url = new K8SURL();
                                url.setResource(Resource.DEPLOYMENT);
                                //labels
                                Map<String, Object> bodys = new HashMap<String, Object>();
                                bodys.put("labelSelector", label);
                                String n = oneNamespace.get("name").toString();
                                url.setNamespace(n).setResource(Resource.DEPLOYMENT);
                                K8SClientResponse depRes = new K8sMachineClient().exec(url, HTTPMethod.GET, null, bodys,cluster);
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

                        if (items != null && items.size() > 0 ) {
                            for (Deployment dev : items) {
                                if(dev != null && dev.getMetadata() != null && !StringUtils.isEmpty(dev.getMetadata().getName())){
                                    Map<String, Object> devLabe = dev.getMetadata().getLabels();
                                    int topoSum = 0;
                                    for (String key : devLabe.keySet()) {
                                        if(key.startsWith(TOPO)) {
                                            topoSum ++;
                                        }
                                    }

                                    if (topoSum > 1){
                                        //todo unbind deployemnt,service
                                        ActionReturnUtil res = unbindBusiness(business, businessList.getTenantId(), dev.getMetadata().getName(), namespace, cluster);
                                        if(!res.isSuccess()){
                                            businessFlag = false;
                                            errorMessage.add(res.get("data").toString());
                                        }
                                        continue;
                                    } else {
                                        //delete deployment
                                        // delete config map & deploy service deployment
                                        ActionReturnUtil deleteDeployReturn = deploymentsService.deleteDeployment(dev.getMetadata().getName(), dev.getMetadata().getNamespace(), username, cluster);
                                        if (!deleteDeployReturn.isSuccess()) {
                                            businessFlag = false;
                                            errorMessage.add(deleteDeployReturn.get("data").toString());
                                        }
                                        //todo sooooooooooooooooooooooo bad
//                                        // delete ingress
//                                        //delete http
//                                        ActionReturnUtil httpR = routerService.listRoutHttp(dev.getMetadata().getName(),dev.getMetadata().getNamespace(),cluster);
//                                        if(!httpR.isSuccess()){
//                                            businessFlag = false;
//                                            errorMessage.add(httpR.get("data").toString());
//                                        } else {
//                                            IngressList httpList = (IngressList)httpR.get("data");
//                                            if (httpList != null && httpList.getItems() != null && httpList.getItems().size() >0){
//                                                for (Ingress one: httpList.getItems()){
//                                                    ActionReturnUtil httpRes = routerService.ingDelete(dev.getMetadata().getNamespace(), one.getMetadata().getName());
//                                                    if(!httpRes.isSuccess()){
//                                                        businessFlag = false;
//                                                        errorMessage.add(httpRes.get("data").toString());
//                                                    }
//                                                }
//                                            }
//                                        }
//
//                                        //delete tcp
//                                        ActionReturnUtil tcpR = routerService.svcList(dev.getMetadata().getNamespace());
//                                        if(!tcpR.isSuccess()){
//                                            businessFlag = false;
//                                            errorMessage.add(tcpR.get("data").toString());
//                                        } else {
//                                            List<RouterSvc> tcpList = (List<RouterSvc>)tcpR.get("data");
//                                            if (tcpList != null && tcpList.size()>0){
//                                                for (RouterSvc one: tcpList){
//                                                    String aa = one.getLabels().get("app").toString();
//                                                    if (one.getLabels() != null && one.getLabels().get("app") != null && dev.getMetadata().getName().equals(one.getLabels().get("app").toString())){
//                                                        List<Integer> ports = new ArrayList<>();
//                                                        for (ServicePort port: one.getRules()){
//                                                            if (port.getPort() != null){
//                                                                ports.add(port.getPort());
//                                                            }
//                                                        }
//                                                        ActionReturnUtil tcpRes = routerService.deleteTcpSvc(dev.getMetadata().getNamespace(), one.getName(),ports,(String) session.getAttribute("tenantId"));
//                                                        if(!tcpRes.isSuccess()){
//                                                            businessFlag = false;
//                                                            errorMessage.add(tcpRes.get("data").toString());
//                                                        }
//                                                    }
//                                                }
//                                            }
//                                        }

                                        // delete pvc
                                        Map<String, Object> pvclabel = new HashMap<String, Object>();
                                        pvclabel.put("labelSelector", "app="+dev.getMetadata().getName());

                                        K8SClientResponse pvcRes = pvcService.doSepcifyPVC(namespace, pvclabel, HTTPMethod.GET, cluster);
                                        if (!HttpStatusUtil.isSuccessStatus(pvcRes.getStatus()) && pvcRes.getStatus() != Constant.HTTP_404) {
                                            UnversionedStatus status = JsonUtil.jsonToPojo(pvcRes.getBody(), UnversionedStatus.class);
                                            errorMessage.add(status.getMessage());
                                            businessFlag = false;
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
                                                    errorMessage.add(response.getBody());
                                                    businessFlag = false;
                                                }

                                                // update pv
                                                if (response.getStatus() != Constant.HTTP_404 && onePvc.getSpec() != null && onePvc.getSpec().getVolumeName() != null) {
                                                    String pvname = onePvc.getSpec().getVolumeName();
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
                                                            errorMessage.add(responsePV.getBody());
                                                            businessFlag = false;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        //get external service by label
                        Map<String, Object> bodys = new HashMap<>();
                        bodys.put("labelSelector", label);
                        K8SURL urlExternal = new K8SURL();
                        urlExternal.setNamespace(Resource.EXTERNALNAMESPACE).setResource(Resource.SERVICE);
                        K8SClientResponse serviceRe = new K8sMachineClient().exec(urlExternal, HTTPMethod.GET, null, bodys,cluster);
                        if (!HttpStatusUtil.isSuccessStatus(serviceRe.getStatus())
                                && serviceRe.getStatus() != Constant.HTTP_404 ) {
                            errorMessage.add("获取external service错误");
                            businessFlag = false;
                        } else {
                            ServiceList svclist = JsonUtil.jsonToPojo(serviceRe.getBody(), ServiceList.class);
                            if (svclist != null && svclist.getItems() != null){
                                for (com.harmonycloud.k8s.bean.Service oneSvc: svclist.getItems()){
                                    if(oneSvc != null && oneSvc.getMetadata() != null && !StringUtils.isEmpty(oneSvc.getMetadata().getName())){
                                        Map<String, Object> body = new HashMap<>();
                                        Map<String, Object> head = new HashMap<String, Object>();
                                        String[] splitLabel = label.split("=");
                                        Map<String,Object> newLabel = new HashMap<String,Object>();
                                        if (splitLabel.length > 2){
                                            for (Map.Entry<String,Object> oneLabel : oneSvc.getMetadata().getLabels().entrySet()){
                                                if (!oneLabel.getKey().equals(splitLabel[0])){
                                                    newLabel.put(oneLabel.getKey(),oneLabel.getValue());
                                                }
                                            }
                                            oneSvc.getMetadata().setLabels(newLabel);
                                        }
                                        body.put("metadata", oneSvc.getMetadata());
                                        body.put("spec", oneSvc.getSpec());
                                        body.put("kind", oneSvc.getKind());
                                        body.put("apiVersion", oneSvc.getApiVersion());
                                        K8SURL url = new K8SURL();
                                        url.setNamespace(Resource.EXTERNALNAMESPACE).setResource(Resource.SERVICE).setSubpath(oneSvc.getMetadata().getName());
                                        head.put("Content-Type", "application/json");
                                        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.PUT, head, body,cluster);
                                        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())
                                                && response.getStatus() != Constant.HTTP_404 ) {
                                            UnversionedStatus status = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
                                            errorMessage.add(status.getMessage());
                                            businessFlag = false;
                                        }
                                    }
                                }
                            }
                        }
                        // delete application
                        if (businessFlag) {
                            ActionReturnUtil tprDelete = tprApplication.delApplicationByName(br.getMetadata().getName(),br.getMetadata().getNamespace(),cluster);
                            if (!tprDelete.isSuccess()){
                                return tprDelete;
                            }
                        }
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
        for (String id : businessList.getIdList()) {
            String namespace = "";

            if (id != null && id.contains(SIGN_EQUAL)){
                String[] value = id.split(SIGN_EQUAL);
                if (value != null){
                    namespace = value[1];
                }
            }
            ActionReturnUtil deploymentsRes = deploymentsService.listDeployments(businessList.getTenantId(), null, namespace, id, null);
            if(!deploymentsRes.isSuccess()){
                return deploymentsRes;
            }
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> deployments = (List<Map<String, Object>>) deploymentsRes.get("data");
            if (deployments != null && deployments.size() > 0) {
                for (Map<String, Object> oneDeployment : deployments) {
                    if(oneDeployment != null && oneDeployment.containsKey("name") ){
                        ActionReturnUtil stopDeployReturn = deploymentsService.stopDeployments(oneDeployment.get("name").toString(), oneDeployment.get("namespace").toString(), username, cluster);
                        if (!stopDeployReturn.isSuccess()) {
                            errorMessage.add(stopDeployReturn.toString());
                        }
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
        for (String id : businessList.getIdList()) {
            String namespace = "";

            if (id != null && id.contains(SIGN_EQUAL)){
                String[] value = id.split(SIGN_EQUAL);
                if (value != null){
                    namespace = value[1];
                }
            }
            ActionReturnUtil deploymentsRes = deploymentsService.listDeployments(businessList.getTenantId(), null, namespace, id, null);
            if(!deploymentsRes.isSuccess()){
                return deploymentsRes;
            }
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> deployments = (List<Map<String, Object>>) deploymentsRes.get("data");
            if (deployments != null && deployments.size() > 0) {
                for (Map<String, Object> oneDeployment : deployments) {
                    if(oneDeployment != null && oneDeployment.containsKey("name") ){
                        ActionReturnUtil stopDeployReturn = deploymentsService.startDeployments(oneDeployment.get("name").toString(), oneDeployment.get("namespace").toString(), username, cluster);
                        if (!stopDeployReturn.isSuccess()) {
                            errorMessage.add(stopDeployReturn.toString());
                        }
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
    @SuppressWarnings("unchecked")
    @Override
    public ActionReturnUtil searchSumBusiness(String [] tenant, String clusterId) throws Exception {
        JSONObject json = new JSONObject();
        // number of application
        int count = 0;
        // number of application 正常
        int normal = 0;
        // number of application 异常
        int abnormal = 0;

        // search application
        List<BaseResource> blist = new ArrayList<>();

        Cluster cluster=clusterService.findClusterById(clusterId);
        List<Object> namespaceData = new ArrayList<Object>();

        for (String tentantOne:tenant){
            if (namespaceService.getNamespaceListByTenantid(tentantOne).get("data") != null){
                namespaceData.addAll((List<Object>) namespaceService.getNamespaceListByTenantid(tentantOne).get("data"));
            }
        }

        for (Object oneNamespace : namespaceData ){
            @SuppressWarnings("rawtypes")
			Map namespaceMap = (Map) oneNamespace;
            K8SClientResponse response = tprApplication.listApplicationByNamespace(namespaceMap.get("name").toString(),null, null, HTTPMethod.GET, cluster);
            if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            	UnversionedStatus status = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
                return ActionReturnUtil.returnErrorWithMsg(status.getMessage());
            }
            BaseResourceList  tpr = JsonUtil.jsonToPojo(response.getBody(), BaseResourceList.class);
            if (tpr != null && tpr.getItems() != null && tpr.getItems().size() > 0){
                blist.addAll(tpr.getItems());
            }
        }


        if (blist != null && blist.size() > 0) {
            count = blist.size();
            for (BaseResource bs : blist) {

                Map<String,Object> appLable = new HashedMap();
                String label = "";

                for (Map.Entry<String, Object> vo : bs.getMetadata().getLabels().entrySet()) {

                    if (vo.getKey().startsWith(TOPO)){
                        appLable.put(vo.getKey(),vo.getValue());
                        label = vo.getKey() + SIGN_EQUAL + vo.getValue();
                    }

                }

                JSONObject servicejson=listServiceByBusinessId(label, bs.getMetadata().getNamespace(), cluster);

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
    @SuppressWarnings("unused")
	private static String dateToString(Date time){
        SimpleDateFormat formatter;
        formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String ctime = formatter.format(time);

        return ctime;
    }

    @Override
    public ActionReturnUtil deleteBusinessByNamespace(String namespace) throws Exception {
        if(!StringUtils.isEmpty(namespace)){
            return ActionReturnUtil.returnSuccess();
        }else{
            return ActionReturnUtil.returnErrorWithMsg("namespace为空");
        }

    }

    @SuppressWarnings("unchecked")
	@Override
    public ActionReturnUtil searchSum(String[] tenant) throws Exception {
        // search application
        List<BaseResource> blist = new ArrayList<>();

        String tenantID = (String) session.getAttribute("tenantId");
        Cluster cluster = tenantService.getClusterByTenantid(tenantID);
        List<Object> namespaceData = new ArrayList<Object>();

        for (String tentantOne:tenant){
            if (namespaceService.getNamespaceListByTenantid(tentantOne).get("data") != null){
                namespaceData.addAll((List<Object>) namespaceService.getNamespaceListByTenantid(tentantOne).get("data"));
            }
        }

        for (Object oneNamespace : namespaceData ){
            K8SClientResponse response = tprApplication.listApplicationByNamespace(oneNamespace.getClass().getName().toString(),null, null, HTTPMethod.GET, cluster);
            if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            	UnversionedStatus status = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
                return ActionReturnUtil.returnErrorWithMsg(status.getMessage());
            }
            BaseResource  tpr = JsonUtil.jsonToPojo(response.getBody(), BaseResource.class);
            if (tpr.getMetadata().getName() != null){
                if (tpr.getMetadata().getName() != null){
                    blist.add(tpr);
                }
            }
        }

        // number of application
        int count = 0;
        if (blist != null && blist.size() > 0) {
            count = blist.size();
        }
        return ActionReturnUtil.returnSuccessWithData(count);
    }

    private JSONObject listServiceByBusinessId(String label, String namespace, Cluster cluster) throws Exception {
        JSONObject json = new JSONObject();
        // number of application running
        int start = 0;
        // number of deploment
        int total = 0;

        //get deployment by label
        Map<String, Object> bodys = new HashMap<>();
        bodys.put("labelSelector", label);

        K8SURL url = new K8SURL();
        url.setNamespace(namespace).setResource(Resource.DEPLOYMENT);
        K8SClientResponse depRes = new K8sMachineClient().exec(url, HTTPMethod.GET, null, bodys,cluster);
        if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus())
                && depRes.getStatus() != Constant.HTTP_404 ) {
            UnversionedStatus status = JsonUtil.jsonToPojo(depRes.getBody(), UnversionedStatus.class);
            return (JSONObject) json.put("获取k8sDeployment错误", status.getMessage());
        }
        DeploymentList deplist = JsonUtil.jsonToPojo(depRes.getBody(), DeploymentList.class);
        if (deplist != null && deplist.getItems() != null) {
            total = deplist.getItems().size();
            List<Deployment> deps = deplist.getItems();
            if (deps != null && deps.size() > 0) {

                for (Deployment dep : deps) {
                    String status = getDeploymentStatus(dep);
                    if (Constant.START.equals(status)) {
                        start++;
                    }
                }
            }
        }


        //get external service by label
        K8SURL urlExternal = new K8SURL();
        urlExternal.setNamespace(Resource.EXTERNALNAMESPACE).setResource(Resource.SERVICE);
        K8SClientResponse serviceRe = new K8sMachineClient().exec(urlExternal, HTTPMethod.GET, null, bodys,cluster);
        if (!HttpStatusUtil.isSuccessStatus(serviceRe.getStatus())
                && serviceRe.getStatus() != Constant.HTTP_404 ) {
            UnversionedStatus k8sresbody = JsonUtil.jsonToPojo(depRes.getBody(), UnversionedStatus.class);
            return (JSONObject) json.put("获取external service错误", k8sresbody.getMessage());
        }
        ServiceList svclist = JsonUtil.jsonToPojo(serviceRe.getBody(), ServiceList.class);
        if (svclist != null && svclist.getItems() != null){
            start = start + svclist.getItems().size();
            total = total + svclist.getItems().size();
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
    public ActionReturnUtil deployBusinessTemplateByName(String tenantId, String name, String businessame, String tag, String namespace, String userName, Cluster cluster, String pub, String nodeselector)
            throws Exception {
        if(!StringUtils.isEmpty(name) && !StringUtils.isEmpty(tag) && !StringUtils.isEmpty(namespace)){
            TenantBinding t = tenantService.getTenantByTenantid(tenantId);
            ActionReturnUtil btresponse=null;
            //根据name和tag获取模板信息
            if("true".equals(pub)){
                btresponse = businessService.getBusinessTemplate(name, tag, "all");
            }else{
                btresponse = businessService.getBusinessTemplate(name, tag, t.getTenantName());
            }
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
            businessTemplate.setTenant(t.getTenantName());
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
                    serviceTemplate.setTenant(t.getTenantName());
                    serviceTemplate.setExternal(js.getInt("isExternal"));
                    if(js.getInt("isExternal") == Constant.K8S_SERVICE){
                        String dep=js.getJSONArray("deployment").getJSONObject(0).toString().replaceAll(":\"\",", ":"+null+",").replaceAll(":\"\"", ":"+null+"");
                        DeploymentDetailDto deployment = JsonUtil.jsonToPojo(dep, DeploymentDetailDto.class);
                        deployment.setNamespace(namespace);
                        deployment.setNodeSelector(nodeselector);
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
            K8SClientResponse depRes = new K8sMachineClient().exec(url, HTTPMethod.GET, null, null,cluster);
            if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus())
                    && depRes.getStatus() != Constant.HTTP_404 ) {
                UnversionedStatus k8sresbody = JsonUtil.jsonToPojo(depRes.getBody(), UnversionedStatus.class);
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
            return ActionReturnUtil.returnErrorWithMsg("用户名 , 应用或者服务名称为空！");
        }

        //获取k8s同namespace相关的资源
        //获取 Deployment name
        if(StringUtils.isEmpty(businessDeploy.getNamespace())){
            return ActionReturnUtil.returnErrorWithMsg("namespace为空");
        }
        String namespace = businessDeploy.getNamespace();
        //check name
        ActionReturnUtil checkRes = checkK8SName(businessDeploy, cluster);
        if(!checkRes.isSuccess()) {
        	return checkRes;
        }
        List<Map<String, Object>> message = new ArrayList<>();
        for (ServiceTemplateDto service : businessDeploy.getBusinessTemplate().getServiceList()) {
            // is external service
            if (service.getExternal() == Constant.EXTERNAL_SERVICE) {
                Map<String, Object> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                K8SClientResponse rsRes = sService.doSepcifyService(Constant.EXTERNAL_SERVICE_NAMESPACE, service.getName(), null, null, HTTPMethod.GET, cluster);
                if (!HttpStatusUtil.isSuccessStatus(rsRes.getStatus())) {
                    return ActionReturnUtil.returnErrorWithMsg(rsRes.getBody());
                }
                com.harmonycloud.k8s.bean.Service serviceE = JsonUtil.jsonToPojo(rsRes.getBody(), com.harmonycloud.k8s.bean.Service.class);
                Map<String,Object> labels = serviceE.getMetadata().getLabels();
                labels.put(TOPO + "-" + tenantid+ "-" + businessDeploy.getName(), businessDeploy.getNamespace());
                serviceE.getMetadata().setLabels(labels);
                Map<String, Object> bodys = CollectionUtil.transBean2Map(serviceE);
                K8SClientResponse res = sService.doSepcifyService(serviceE.getMetadata().getNamespace(), serviceE.getMetadata().getName(), headers, bodys, HTTPMethod.PUT, cluster);
                if (!HttpStatusUtil.isSuccessStatus(res.getStatus())) {
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put(serviceE.getMetadata().getName(), res.getBody());
                    message.add(map);
                }
            }else {
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
                                        pvc.getVolume(),service.getDeploymentDetail().getName());
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
                    deploymentsService.createDeployment(service.getDeploymentDetail(), username, businessDeploy.getName(), cluster);
                } catch (Exception e) {
                	Map<String, Object> map = new HashMap<String, Object>();
                	map.put("Deployment:", service.getDeploymentDetail().getName());
                	message.add(map);
                }
            }
        }
        if (message.size() > 0) {
            return ActionReturnUtil.returnErrorWithData(message);
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
        K8SClientResponse depRes = new K8sMachineClient().exec(url, HTTPMethod.GET, null, null,cluster);
        if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus())
                && depRes.getStatus() != Constant.HTTP_404 ) {
            UnversionedStatus k8sresbody = JsonUtil.jsonToPojo(depRes.getBody(), UnversionedStatus.class);
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
        //list namespace by tenantid
        @SuppressWarnings("unchecked")
		List<Object> namespaceData = (List<Object>) namespaceService.getNamespaceListByTenantid(session.getAttribute("tenantId").toString()).get("data");
        //loop namespaces get application
        for (Object oneNamespace : namespaceData ){
            K8SClientResponse response = tprApplication.getApplicationByName(oneNamespace.getClass().getName().toString(),businessDeploy.getName(), null,null, HTTPMethod.GET, cluster);
            if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
                msg.put("Application:"+businessDeploy.getName(), "重复");
                flag = false;
                break;
            }
        }

        if(flag){
            return ActionReturnUtil.returnSuccess();
        }else{
            return ActionReturnUtil.returnErrorWithData(msg);
        }
    }

    public ActionReturnUtil unbindBusiness(String businessname, String tenantId, String name, String namespace, Cluster cluster)throws Exception {
        String labelKey = TOPO + SIGN + tenantId + SIGN + businessname;
        //更新Deployment label
        K8SURL url = new K8SURL();
        url.setNamespace(namespace).setResource(Resource.DEPLOYMENT).setName(name);
        K8SClientResponse depRes = new K8sMachineClient().exec(url, HTTPMethod.GET, null, null,cluster);
        if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus())
                && depRes.getStatus() != Constant.HTTP_404 ) {
            UnversionedStatus k8sresbody = JsonUtil.jsonToPojo(depRes.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithMsg(k8sresbody.getMessage());
        }
        Deployment dep = JsonUtil.jsonToPojo(depRes.getBody(), Deployment.class);
        if(dep != null){
                    if(dep != null && dep.getMetadata() != null && dep.getMetadata().getLabels() != null){
                        Map<String, Object> labels = new HashMap<String, Object>();
                        labels = dep.getMetadata().getLabels();
                        labels.remove(labelKey);
                        dep.getMetadata().setLabels(labels);
                        Map<String, Object> bodys = new HashMap<String, Object>();
                        bodys = CollectionUtil.transBean2Map(dep);
                        Map<String, Object> headers = new HashMap<String, Object>();
                        headers.put("Content-type", "application/json");
                        K8SClientResponse newRes = dpService.doSpecifyDeployment(namespace, name, headers, bodys, HTTPMethod.PUT, cluster);
                        if(!HttpStatusUtil.isSuccessStatus(newRes.getStatus())){
                            UnversionedStatus k8sresbody = JsonUtil.jsonToPojo(newRes.getBody(), UnversionedStatus.class);
                            return ActionReturnUtil.returnErrorWithMsg(k8sresbody.getMessage());
                        }
                    }
        }
        //更新service
        url = new K8SURL();
        url.setNamespace(namespace).setResource(Resource.SERVICE).setName(name);
        K8SClientResponse serRes = new K8sMachineClient().exec(url, HTTPMethod.GET, null, null,cluster);
        if (!HttpStatusUtil.isSuccessStatus(serRes.getStatus())
                && serRes.getStatus() != Constant.HTTP_404 ) {
            UnversionedStatus k8sresbody = JsonUtil.jsonToPojo(serRes.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithMsg(k8sresbody.getMessage());
        }
        com.harmonycloud.k8s.bean.Service svc = JsonUtil.jsonToPojo(depRes.getBody(), com.harmonycloud.k8s.bean.Service.class);
            if(svc != null ){
                    if(svc != null && svc.getMetadata() != null && svc.getMetadata().getLabels() != null){
                        Map<String, Object> labels = new HashMap<String, Object>();
                        labels = svc.getMetadata().getLabels();
                        labels.remove(labelKey);
                        svc.getMetadata().setLabels(labels);
                        Map<String, Object> bodys = new HashMap<String, Object>();
                        bodys = CollectionUtil.transBean2Map(svc);
                        Map<String, Object> headers = new HashMap<String, Object>();
                        headers.put("Content-type", "application/json");
                        K8SClientResponse newRes = new K8sMachineClient().exec(url, HTTPMethod.PUT, headers, bodys, cluster);
                        if(!HttpStatusUtil.isSuccessStatus(newRes.getStatus())){
                            UnversionedStatus k8sresbody = JsonUtil.jsonToPojo(newRes.getBody(), UnversionedStatus.class);
                            return ActionReturnUtil.returnErrorWithMsg(k8sresbody.getMessage());
                        }
                    }
                }
        return ActionReturnUtil.returnSuccess();
    }

    public ActionReturnUtil bindBusiness(String businessname, String tenantId, String name, String namespace, Cluster cluster)throws Exception {
        String labelKey = TOPO + SIGN + tenantId + SIGN + businessname;
        //更新Deployment label
        K8SURL url = new K8SURL();
        url.setNamespace(namespace).setResource(Resource.DEPLOYMENT).setName(name);
        K8SClientResponse depRes = new K8sMachineClient().exec(url, HTTPMethod.GET, null, null,cluster);
        if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus())
                && depRes.getStatus() != Constant.HTTP_404 ) {
            UnversionedStatus k8sresbody = JsonUtil.jsonToPojo(depRes.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithMsg(k8sresbody.getMessage());
        }
        Deployment dep = JsonUtil.jsonToPojo(depRes.getBody(), Deployment.class);
            if(dep != null ){
                    if(dep != null && dep.getMetadata() != null && dep.getMetadata().getLabels() != null){
                        Map<String, Object> labels = new HashMap<String, Object>();
                        labels = dep.getMetadata().getLabels();
                        labels.put(labelKey, namespace);
                        dep.getMetadata().setLabels(labels);
                        Map<String, Object> bodys = new HashMap<String, Object>();
                        bodys = CollectionUtil.transBean2Map(dep);
                        Map<String, Object> headers = new HashMap<String, Object>();
                        headers.put("Content-type", "application/json");
                        K8SClientResponse newRes = dpService.doSpecifyDeployment(namespace, name, headers, bodys, HTTPMethod.PUT, cluster);
                        if(!HttpStatusUtil.isSuccessStatus(newRes.getStatus())){
                            UnversionedStatus k8sresbody = JsonUtil.jsonToPojo(newRes.getBody(), UnversionedStatus.class);
                            return ActionReturnUtil.returnErrorWithMsg(k8sresbody.getMessage());
                        }
                    }
        }
        //更新service
        url = new K8SURL();
        url.setNamespace(namespace).setResource(Resource.SERVICE).setName(name);
        K8SClientResponse serRes = new K8sMachineClient().exec(url, HTTPMethod.GET, null, null,cluster);
        if (!HttpStatusUtil.isSuccessStatus(serRes.getStatus())
                && serRes.getStatus() != Constant.HTTP_404 ) {
            UnversionedStatus k8sresbody = JsonUtil.jsonToPojo(serRes.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithMsg(k8sresbody.getMessage());
        }
        com.harmonycloud.k8s.bean.Service svc = JsonUtil.jsonToPojo(depRes.getBody(), com.harmonycloud.k8s.bean.Service.class);
        if(svc != null  ){
                    if(svc != null && svc.getMetadata() != null && svc.getMetadata().getLabels() != null){
                        Map<String, Object> labels = new HashMap<String, Object>();
                        labels = svc.getMetadata().getLabels();
                        labels.put(labelKey, namespace);
                        svc.getMetadata().setLabels(labels);
                        Map<String, Object> bodys = new HashMap<String, Object>();
                        bodys = CollectionUtil.transBean2Map(svc);
                        Map<String, Object> headers = new HashMap<String, Object>();
                        headers.put("Content-type", "application/json");
                        K8SClientResponse newRes = new K8sMachineClient().exec(url, HTTPMethod.PUT, headers, bodys, cluster);
                        if(!HttpStatusUtil.isSuccessStatus(newRes.getStatus())){
                            UnversionedStatus k8sresbody = JsonUtil.jsonToPojo(newRes.getBody(), UnversionedStatus.class);
                            return ActionReturnUtil.returnErrorWithMsg(k8sresbody.getMessage());
                        }
                    }
        }
        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public ActionReturnUtil getTopo(String id) throws Exception {

        if (StringUtils.isEmpty(id)) {
            return ActionReturnUtil.returnErrorWithMsg("id 不能为空！");
        }

        //String url = "http://kube-topo:8000/topo";
        //String url = "http://10.10.101.75:30988/topo";

        Map<String, Object> headers = new HashMap<>();
        headers.put("cookie", harborUtil.checkCookieTimeout());

        Map<String, Object> params = new HashMap<>();
        params.put("topoSelector", id);
        
        ActionReturnUtil res = HttpClientUtil.httpGetRequest(kubeTopo, headers, params);
        if(res.isSuccess()) {
        	String s =  (String) res.get("data");
        	if(s.contains("{")) {
        		JSONObject a =  JSONObject.fromObject(s);
        		JSONArray as = a.getJSONArray("links");
            	//获取应用下所有应用的服务
            	Map<String, Object> bodys = new HashMap<>();
                bodys.put("labelSelector", id);
                String[] namespace = {};
                if (id.contains(SIGN) && id.contains(SIGN_EQUAL)){
                    namespace = id.split(SIGN_EQUAL);
                }
                Cluster cluster = (Cluster) session.getAttribute("currentCluster");
                K8SURL url1 = new K8SURL();
                url1.setNamespace(namespace[1]).setResource(Resource.DEPLOYMENT);
                K8SClientResponse depRes = new K8sMachineClient().exec(url1, HTTPMethod.GET, null, bodys, cluster);
                if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus())
                        && depRes.getStatus() != Constant.HTTP_404) {
                	UnversionedStatus sta = JsonUtil.jsonToPojo(depRes.getBody(), UnversionedStatus.class);
                    return ActionReturnUtil.returnErrorWithMsg(sta.getMessage());
                } 
                DeploymentList deplist = JsonUtil.jsonToPojo(depRes.getBody(), DeploymentList.class);
                if (deplist != null && deplist.getItems() != null) {
                    List<Deployment> deps = deplist.getItems();
                    if (deps != null && deps.size() > 0) {
                        for (Deployment dep : deps) {
                        	ActionReturnUtil ress = routerService.listIngressByName(namespace[1], dep.getMetadata().getName(), cluster);
                        	if(ress.isSuccess()) {
                        		JSONArray has = (JSONArray) ress.get("data");
                        		if(has.isArray() && has.size() > 0) {
                        			ActionReturnUtil balanceRes = loadbalanceService.getStatsByService(dep.getMetadata().getName(), namespace[1]);
                        			if(balanceRes.isSuccess()) {
                        				@SuppressWarnings("unchecked")
										Map<String, Object> map = (Map<String, Object>) balanceRes.get("data");
                        				JSONObject balance = JSONObject.fromObject(map);
                        				balance.put("srcSVC", "HAProxy");
                        				balance.put("dstSVC", dep.getMetadata().getName());
                        				as.add(balance);
                        			}
                        		}
                        	}
                        }
                    }
                }
                return ActionReturnUtil.returnSuccessWithData(a);
            }
        } 
        return res;
    }
}