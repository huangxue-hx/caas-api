package com.harmonycloud.service.application.impl;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.Constant.IngressControllerConstant;
import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.enumm.ServiceTypeEnum;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.AssertUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.dao.application.ServiceTemplatesMapper;
import com.harmonycloud.dao.application.bean.ServiceTemplates;
import com.harmonycloud.dao.tenant.bean.NamespaceLocal;
import com.harmonycloud.dto.application.*;
import com.harmonycloud.dto.cluster.IngressControllerDto;
import com.harmonycloud.k8s.bean.*;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.service.*;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import com.harmonycloud.service.application.*;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.platform.bean.RouterSvc;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.tenant.NamespaceLocalService;
import com.harmonycloud.service.tenant.NamespaceService;
import com.harmonycloud.service.tenant.PrivatePartitionService;
import com.harmonycloud.service.tenant.TenantService;
import com.harmonycloud.service.user.RoleLocalService;
import com.harmonycloud.service.user.UserService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.harmonycloud.common.Constant.CommonConstant.BLANKSTRING;


/**
 * Created by root on 3/29/17.
 */

@Service
@Transactional(rollbackFor = Exception.class)
public class ServiceServiceImpl implements ServiceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceServiceImpl.class);

    @Autowired
    private ServiceTemplatesMapper serviceTemplatesMapper;

    @Autowired
    private ApplicationWithServiceService appWithServiceService;

    @Autowired
    private RouterService routerService;

    @Autowired
    private DeploymentsService deploymentsService;

    @Autowired
    private PersistentVolumeService volumeSerivce;

    @Autowired
    private PrivatePartitionService privatePartitionService;

    @Autowired
    private NamespaceService namespaceService;

    @Autowired
    private HttpSession session;

    @Autowired
    private TenantService tenantService;

    @Autowired
    private ApplicationDeployService applicationDeployService;

    @Autowired
    private NamespaceLocalService namespaceLocalService;
    @Autowired
    private RoleLocalService roleLocalService;

    @Autowired
    private UserService userService;

    @Autowired
    private IcService icService;

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private DeploymentService dpService;

    @Autowired
    private StatefulSetService statefulSetService;

    @Autowired
    private StatefulSetsService statefulSetsService;

    @Autowired
    private AutoScaleService autoScaleService;

    /*@Autowired
    private PodDisruptionBudgetService pdbService;*/

    @Autowired
    private PVCService pvcService;

    @Autowired
    private ServicesService sService;

    @Autowired
    private FileUploadToContainerService fileUploadToContainerService;

    /**
     * create Service Template implement
     *
     * @param serviceTemplate 服务模板信息
     * @param userName        用户名称
     * @param type            模板类型
     * @return ActionReturnUtil
     * @throws Exception
     */
    @Override
    public ActionReturnUtil saveServiceTemplate(ServiceTemplateDto serviceTemplate, String userName, int type) throws Exception {

        // check value
        if (StringUtils.isEmpty(userName) || serviceTemplate == null) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        //判断重名
        int a = Constant.TEMPLATE_STATUS_CREATE;
        if (type == Constant.TEMPLATE_STATUS_DELETE) {
            a = Constant.TEMPLATE_STATUS_DELETE;
        }
        // create and insert into db
        ServiceTemplates serviceTemplateDB = new ServiceTemplates();
        serviceTemplateDB.setName(serviceTemplate.getName());
        serviceTemplateDB.setDetails(serviceTemplate.getDesc());
        List<CreateContainerDto> containers = null;
        if (serviceTemplate.getDeploymentDetail() != null) {
            JSONArray deployment = JSONArray.fromObject(serviceTemplate.getDeploymentDetail());
            serviceTemplateDB.setDeploymentContent(deployment.toString());
            containers = serviceTemplate.getDeploymentDetail().getContainers();
            serviceTemplateDB.setServiceType(ServiceTypeEnum.DEPLOYMENT.getCode());
        }else if(serviceTemplate.getStatefulSetDetail() != null){
            JSONArray statefulSet = JSONArray.fromObject(serviceTemplate.getStatefulSetDetail());
            serviceTemplateDB.setDeploymentContent(statefulSet.toString());
            containers = serviceTemplate.getStatefulSetDetail().getContainers();
            serviceTemplateDB.setServiceType(ServiceTypeEnum.STATEFULSET.getCode());
        }
        if(CollectionUtils.isNotEmpty(containers)){
            String images = "";
            for (CreateContainerDto c : containers) {
                if (StringUtils.isNotBlank(c.getImg())) {
                    if (!images.contains(c.getImg())) {
                        images = images + c.getImg() + ",";
                    }
                }
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
        serviceTemplateDB.setTag(String.valueOf(Constant.TEMPLATE_TAG));
        serviceTemplateDB.setPublic(serviceTemplate.isPublic());
        serviceTemplateDB.setProjectId(serviceTemplate.getProjectId());
        serviceTemplateDB.setClusterId(serviceTemplate.getClusterId());
        serviceTemplatesMapper.insert(serviceTemplateDB);
        JSONObject json = new JSONObject();
        json.put(serviceTemplateDB.getName(), serviceTemplateDB.getId());
        return ActionReturnUtil.returnSuccessWithData(json);
    }

    /**
     * list Service Template implement
     *
     * @param name
     * @param clusterId
     * @return
     * @throws Exception
     */
    @Override
    public ActionReturnUtil listServiceTemplate(String name, String clusterId, boolean isPublic, String projectId, Integer serviceType) throws Exception {
        JSONArray array = new JSONArray();
        // check value null

        // list
        List<ServiceTemplates> serviceBytenant = serviceTemplatesMapper.listNameByProjectId(name, clusterId, isPublic, projectId, serviceType);

        if (serviceBytenant != null && serviceBytenant.size() > 0) {
            for (ServiceTemplates serviceTemplates : serviceBytenant) {
                List<ServiceTemplates> serviceList = serviceTemplatesMapper.listServiceByImage(
                        serviceTemplates.getName(), serviceTemplates.getImageList(), serviceTemplates.getTenant(), isPublic);
                array.add(getServiceTemplates(serviceList));
            }
        }

        return ActionReturnUtil.returnSuccessWithData(array);
    }

    /**
     * list Service Template by image
     *
     * @param name
     * @param projectId
     * @return
     * @throws Exception
     */
    @Override
    public ActionReturnUtil listTemplateByImage(String name, String tenant, String image, String projectId) throws Exception {
        JSONArray array = new JSONArray();

        //参数判空
        if (StringUtils.isEmpty(image)) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }

        // list
        List<ServiceTemplates> serviceByProject = serviceTemplatesMapper.listNameByImage(name, image, tenant, projectId);
        if (serviceByProject != null && serviceByProject.size() > 0) {
            for (ServiceTemplates serviceTemplates : serviceByProject) {
                List<ServiceTemplates> serviceList = serviceTemplatesMapper.listServiceLikeImage(
                        serviceTemplates.getName(), image, serviceTemplates.getTenant(), serviceTemplates.getProjectId());
                array.add(getServiceTemplates(serviceList));
            }
        }
        return ActionReturnUtil.returnSuccessWithData(array);
    }

    @Override
    public ActionReturnUtil updateServiceTemplate(ServiceTemplateDto serviceTemplate, String username)
            throws Exception {

        // check value
        if (StringUtils.isEmpty(username) || serviceTemplate == null) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        ServiceTemplates serviceTemplateDB = new ServiceTemplates();
        serviceTemplateDB.setName(serviceTemplate.getName());
        serviceTemplateDB.setDetails(serviceTemplate.getDesc());
        serviceTemplateDB.setId(serviceTemplate.getId());
        List<CreateContainerDto> containers = null;
        if (serviceTemplate.getDeploymentDetail() != null) {
            JSONArray deploment = JSONArray.fromObject(serviceTemplate.getDeploymentDetail());
            serviceTemplateDB.setDeploymentContent(deploment.toString());
            containers = serviceTemplate.getDeploymentDetail().getContainers();
            serviceTemplateDB.setServiceType(ServiceTypeEnum.DEPLOYMENT.getCode());
        }else if(serviceTemplate.getStatefulSetDetail() != null){
            JSONArray statefulSet = JSONArray.fromObject(serviceTemplate.getStatefulSetDetail());
            serviceTemplateDB.setDeploymentContent(statefulSet.toString());
            containers = serviceTemplate.getStatefulSetDetail().getContainers();
            serviceTemplateDB.setServiceType(ServiceTypeEnum.STATEFULSET.getCode());
        }
        if(CollectionUtils.isNotEmpty(containers)) {
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
        serviceTemplateDB.setClusterId(serviceTemplate.getClusterId());
        serviceTemplateDB.setFlag(serviceTemplate.getFlag());
        serviceTemplateDB.setClusterId(serviceTemplate.getClusterId());
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
    public ActionReturnUtil deleteServiceTemplate(String name, String userName, String projectId, String clusterId) throws Exception {
        AssertUtil.notBlank(name, DictEnum.NAME);
        // get id list by service_template_name
        List<ServiceTemplates> idList = serviceTemplatesMapper.listTplByNameAndProjectAndCluster(name, clusterId, false, projectId);
        List<Integer> ids = new ArrayList<>();
        for (ServiceTemplates id : idList) {
            ids.add(id.getId());
        }
        // check map
        int mapNum = appWithServiceService.selectByIdList(ids);
        if (mapNum >= CommonConstant.NUM_ONE) {
            return ActionReturnUtil.returnErrorWithData("该服务模板已经被其他应用模板绑定，不能删除！");
        }
        // delete
        serviceTemplatesMapper.deleteByName(name, projectId, clusterId);
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
    public ServiceTemplates getSpecificTemplate(String name, String tag, String clusterId, String projectId) throws Exception {
        // check value
        if (StringUtils.isBlank(name) || StringUtils.isEmpty(projectId)) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }

        return serviceTemplatesMapper.getSpecificService(name, tag, clusterId, projectId);
    }

    /**
     * get ServiceTemplate overview on 17/05/05.
     *
     * @param serviceTemplatesList
     * @return {@link JSONObject}
     */
    private JSONObject getServiceTemplates(List<ServiceTemplates> serviceTemplatesList) throws Exception {
        JSONObject json = new JSONObject();
        if (serviceTemplatesList != null && serviceTemplatesList.size() > 0) {
            json.put("name", serviceTemplatesList.get(0).getName());
            if ("all".equals(serviceTemplatesList.get(0).getTenant())) {
                json.put("public", true);
            } else {
                json.put("public", false);
            }
            JSONArray tagArray = new JSONArray();
            for (int i = 0; i < serviceTemplatesList.size(); i++) {
                JSONObject idAndTag = new JSONObject();
                idAndTag.put("id", serviceTemplatesList.get(i).getId());
                idAndTag.put("tag", serviceTemplatesList.get(i).getTag());
                idAndTag.put("image", serviceTemplatesList.get(i).getImageList());
                idAndTag.put("user", serviceTemplatesList.get(i).getUser());
                String content = JSONArray.fromObject(serviceTemplatesList.get(i).getDeploymentContent()).getJSONObject(0).toString().replaceAll(":\"\",", ":" + null + ",").replaceAll(":\"\"", ":" + null + "");
                if(serviceTemplatesList.get(i).getServiceType() == ServiceTypeEnum.DEPLOYMENT.getCode()){
                    DeploymentDetailDto deployment = JsonUtil.jsonToPojo(content, DeploymentDetailDto.class);
                    idAndTag.put("name", deployment.getName());
                }else if(serviceTemplatesList.get(i).getServiceType() == ServiceTypeEnum.STATEFULSET.getCode()){
                    StatefulSetDetailDto statefulSetDetailDto = JsonUtil.jsonToPojo(content, StatefulSetDetailDto.class);
                    idAndTag.put("name", statefulSetDetailDto.getName());
                }
                idAndTag.put("realName", userService.getUser(serviceTemplatesList.get(i).getUser()).getRealName());
                tagArray.add(idAndTag);
                json.put("createtime", dateToString(serviceTemplatesList.get(i).getCreateTime()));
            }
            json.put("tags", tagArray);
            json.put("clusterId", serviceTemplatesList.get(0).getClusterId());
            json.put("clusterName", clusterService.findClusterById(serviceTemplatesList.get(0).getClusterId()).getAliasName());
            json.put(CommonConstant.SERVICE_TYPE, serviceTemplatesList.get(0).getServiceType());
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
    public ActionReturnUtil deleteDeployedService(DeployedServiceNamesDto deployedServiceNamesDto, String userName)
            throws Exception {
        // check value
        if (deployedServiceNamesDto == null || CollectionUtils.isEmpty(deployedServiceNamesDto.getServiceList())) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }

        //获取serviceList(deployment和statefulset)
        List<StatefulSet> statefulSetList = new ArrayList<>();
        List<Deployment> items = new ArrayList<>();

        for (ServiceNameNamespace nn : deployedServiceNamesDto.getServiceList()) {
            //获取集群
            Cluster cluster = namespaceLocalService.getClusterByNamespaceName(nn.getNamespace());
            if (Objects.isNull(cluster)) {
                return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.CLUSTER_NOT_FOUND);
            }

            //判断k8s中是否存在被删除的服务
            // 判断有状态和无状态的类型（Deployment/Statefulset）
            if(StringUtils.isBlank(nn.getServiceType())){
                nn.setServiceType(Constant.DEPLOYMENT);
            }
            ServiceTypeEnum typeEnum = ServiceTypeEnum.valueOf(nn.getServiceType().toUpperCase());
            switch (typeEnum) {
                case DEPLOYMENT:
                    K8SClientResponse depRes = dpService.doSpecifyDeployment(nn.getNamespace(), nn.getName(), null, null, HTTPMethod.GET, cluster);
                    if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus()) && depRes.getStatus() != Constant.HTTP_404) {
                        UnversionedStatus sta = JsonUtil.jsonToPojo(depRes.getBody(), UnversionedStatus.class);
                        return ActionReturnUtil.returnErrorWithData(sta.getMessage());
                    }
                    Deployment deployment = JsonUtil.jsonToPojo(depRes.getBody(), Deployment.class);
                    if (deployment != null) {
                        items.add(deployment);
                    }
                    break;
                case STATEFULSET:
                    StatefulSet statefulSet = statefulSetService.getStatefulSet(nn.getNamespace(), nn.getName(), cluster);
                    statefulSetList.add(statefulSet);
                    break;
                default:
                    break;
            }
        }
        if (!CollectionUtils.isEmpty(items)) {
            for (Deployment dev : items) {
                String namespace = dev.getMetadata().getNamespace();
                String devName = dev.getMetadata().getName();
                //获取集群
                Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
                if (Objects.isNull(cluster)) {
                    return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.CLUSTER_NOT_FOUND);
                }
                // delete config map & deploy service deployment pvc ingress
                deploymentsService.deleteDeployment(devName, namespace, userName, cluster);
            }
        }
        if (!CollectionUtils.isEmpty(statefulSetList)) {
            for (StatefulSet statefulSet : statefulSetList) {
                String namespace = statefulSet.getMetadata().getNamespace();
                Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
                if (Objects.isNull(cluster)) {
                    return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.CLUSTER_NOT_FOUND);
                }
                statefulSetsService.deleteStatefulSet(statefulSet.getMetadata().getName(), namespace, userName, cluster);
            }
        }
        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public ActionReturnUtil listServiceTemplate(String searchKey, String searchValue, String clusterId, boolean isPublic, String projectId, Integer serviceType) throws Exception {
        JSONArray array = new JSONArray();
        List<ServiceTemplates> serviceBytenant = null;
        //公私有模板
        if (isPublic) {
            //公有模板
            serviceBytenant = listPublicServiceTemplate(searchKey, searchValue, serviceType);
        } else {
            //私有模板
            serviceBytenant = listPrivateServiceTemplate(searchKey, searchValue, clusterId, projectId, serviceType);
        }
        if (serviceBytenant != null && serviceBytenant.size() > 0) {
            for (ServiceTemplates serviceTemplates : serviceBytenant) {
                array.add(getServiceTemplates(Arrays.asList(serviceTemplates)));
            }
        }
        return ActionReturnUtil.returnSuccessWithData(array);
    }

    /**
     * 公有模板
     */
    private List<ServiceTemplates> listPublicServiceTemplate(String searchKey, String searchvalue, Integer serviceType) throws Exception {
        List<ServiceTemplates> serviceBytenant = null;
        if (!StringUtils.isEmpty(searchKey)) {
            if (searchKey.equals("name")) {
                // search by name
                serviceBytenant = serviceTemplatesMapper.listPublicSearchByName(searchvalue, true, serviceType);
            } else if (searchKey.equals("image")) {
                // search by image
                serviceBytenant = serviceTemplatesMapper.listPublicSearchByImage(searchvalue, true, serviceType);
            }
        } else {
            serviceBytenant = serviceTemplatesMapper.listPublicNameByTenant(null, true, serviceType);
        }
        return serviceBytenant;
    }

    /**
     * 私有模板
     */
    private List<ServiceTemplates> listPrivateServiceTemplate(String searchKey, String searchValue, String clusterId, String projectId, Integer serviceType) throws Exception {
        List<ServiceTemplates> serviceList = new ArrayList<>();
        Set<String> clusterIdList = new HashSet<>();
        if (StringUtils.isEmpty(clusterId)) {
            //获取当前角色的集群
            clusterIdList = roleLocalService.listCurrentUserRoleClusterIds();
        } else {
            clusterIdList.add(clusterId);
        }
        for (String cId : clusterIdList) {
            List<ServiceTemplates> tmpList = new ArrayList<>();
            if (StringUtils.isNotEmpty(searchValue)) {
                List<ServiceTemplates> tmpNameList = serviceTemplatesMapper.listSearchByName(searchValue, cId, false, projectId, serviceType);
                List<ServiceTemplates> tmpImageList = serviceTemplatesMapper.listSearchByImage(searchValue, cId, false, projectId, serviceType);
                tmpNameList.addAll(tmpImageList);
                tmpList = tmpNameList.stream().distinct().collect(Collectors.toList());
            } else {
                tmpList = serviceTemplatesMapper.listNameByProjectId(null, cId, false, projectId, serviceType);
            }
            serviceList.addAll(tmpList);
        }
        return serviceList;
    }

    @Override
    public ActionReturnUtil deleteServiceByNamespace(String namespace) throws Exception {
        AssertUtil.notBlank(namespace, DictEnum.NAMESPACE);
        //todo
        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public ActionReturnUtil deployServiceByName(String app, String tenantId, String name, String clusterId, String namespace, String userName, String nodeSelector, String projectId)
            throws Exception {
        if (StringUtils.isEmpty(name) || StringUtils.isEmpty(namespace)) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        //获取集群
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        if (Objects.isNull(cluster)) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        //获取模板信息
        ServiceTemplates serviceTemplate = serviceTemplatesMapper.getSpecificService(name, null, cluster.getId(), projectId);
        if (serviceTemplate != null) {
            ServiceDeployDto serviceDeploy = new ServiceDeployDto();
            serviceDeploy.setNamespace(namespace);
            ServiceTemplateDto serviceDto = getServiceTemplateDtoByServiceTemplate(serviceTemplate, app, name, null, namespace, projectId);
            serviceDeploy.setServiceTemplate(serviceDto);
            ActionReturnUtil res = checkService(serviceDto, cluster, namespace);
            if (!res.isSuccess()) {
                return res;
            }
            serviceDeploy.setTenantId(tenantId);
            return deployService(serviceDeploy, userName);
        } else {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.SERVICE_TEMPLATE_NOT_EXIST);
        }


    }

    @Override
    public ActionReturnUtil deployService(ServiceDeployDto serviceDeploy, String userName) throws Exception {
        if (serviceDeploy == null || StringUtils.isBlank(serviceDeploy.getNamespace()) || Objects.isNull(serviceDeploy.getServiceTemplate())) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }

        //判断项目是否存在
        if (StringUtils.isBlank(serviceDeploy.getServiceTemplate().getProjectId()) || "null".equals(serviceDeploy.getServiceTemplate().getProjectId())) {
            throw new MarsRuntimeException(ErrorCodeMessage.PROJECT_NOT_EXIST);
        }
        ServiceTemplateDto service = serviceDeploy.getServiceTemplate();
        String namespace = serviceDeploy.getNamespace();
        String serviceType = null;
        //判断镜像是否为空
        List<CreateContainerDto> containers;
        List<CreateContainerDto> initContainers;
        Set<String> services = new HashSet<>();
        if (serviceDeploy.getServiceTemplate().getDeploymentDetail() != null) {
            serviceType = Constant.DEPLOYMENT;
            containers = service.getDeploymentDetail().getContainers();
            initContainers = service.getDeploymentDetail().getInitContainers();
            service.getDeploymentDetail().setNamespace(namespace);
            services.add(service.getDeploymentDetail().getName());
        }else if (serviceDeploy.getServiceTemplate().getStatefulSetDetail() != null) {
            serviceType = Constant.STATEFULSET;
            containers = service.getStatefulSetDetail().getContainers();
            initContainers = service.getStatefulSetDetail().getInitContainers();
            service.getStatefulSetDetail().setNamespace(namespace);
            services.add(service.getStatefulSetDetail().getName());
        }else{
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }

        for (CreateContainerDto c : containers) {
            if (StringUtils.isBlank(c.getImg())) {
                return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.SERVICE_TEMPLATE_IMAGE_INFO_NOT_NULL);
            }
        }

        //获取集群
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(serviceDeploy.getNamespace());
        if (Objects.isNull(cluster)) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }

        List<Map<String, Object>> message = new ArrayList<>();


        //check name
        ActionReturnUtil res = checkService(service, cluster, namespace);
        if (!res.isSuccess()) {
            return res;
        }

        // create ingress
        if (service.getIngress() != null) {
            service.setTenantId(serviceDeploy.getTenantId());
            message.addAll(routerService.createExternalRule(service, serviceDeploy.getNamespace(), serviceType));
        }

        // creat config map & deploy service deployment & get node label by
        // namespace
        // todo so bad

        for (CreateContainerDto c : containers) {
            c.setImg(cluster.getHarborServer().getHarborAddress() + "/" + c.getImg());
        }
        if(initContainers != null) {
            for (CreateContainerDto c : initContainers) {
                c.setImg(cluster.getHarborServer().getHarborAddress() + "/" + c.getImg());
            }
        }

        if(service.getDeploymentDetail() != null) {
            service.getDeploymentDetail().setProjectId(serviceDeploy.getServiceTemplate().getProjectId());
            res = deploymentsService.createDeployment(service.getDeploymentDetail(), userName, null,
                    cluster, service.getIngress());
        }else if(service.getStatefulSetDetail() != null) {
            service.getStatefulSetDetail().setProjectId(serviceDeploy.getServiceTemplate().getProjectId());
            res = statefulSetsService.createStatefulSet(service.getStatefulSetDetail(), userName, null,
                    cluster, service.getIngress());
        }
        if (!res.isSuccess()) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(service.getName(), res.get("data"));
            message.add(map);
        }
        if (message.size() > 0) {
            ActionReturnUtil rollbackRes = null;
            if(service.getDeploymentDetail() != null) {
                //释放已绑定存储
                for (CreateContainerDto c : service.getDeploymentDetail().getContainers()) {
                    if (c.getStorage() != null) {
                        for (PersistentVolumeDto pvc : c.getStorage()) {
                            if (pvc.getType() != null && Constant.VOLUME_TYPE_NFS.equals(pvc.getType())) {
                                if (StringUtils.isBlank(pvc.getPvcName())) {
                                    continue;
                                }
                                ActionReturnUtil result = volumeSerivce.releasePv(pvc.getPvcName(),cluster.getId(),namespace,serviceDeploy.getServiceTemplate().getDeploymentDetail().getName());
                                if (!result.isSuccess()){
                                    return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.PV_RELEASE_FAIL);
                                }
                            }
                        }
                    }
                }
                rollbackRes = applicationDeployService.rollBackDeployment(services, namespace, userName, cluster);
            }else if(service.getStatefulSetDetail() != null){
                rollbackRes = applicationDeployService.rollBackStatefulSet(services, namespace, userName, cluster);
            }
            if (!rollbackRes.isSuccess()) {
                return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.SERVICE_CREATE_ROLLBACK_FAILURE);
            }
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.SERVICE_CREATE_FAILURE);
        }
        return ActionReturnUtil.returnSuccess();
    }

    /**
     * 检查Deployment ingress
     */
    public ActionReturnUtil checkService(ServiceTemplateDto service, Cluster cluster, String namespace) throws Exception {
        JSONObject msg = new JSONObject();
        //check name
        //service name
        K8SURL url = new K8SURL();
        url.setNamespace(namespace).setResource(Resource.DEPLOYMENT);
        K8SClientResponse depRes = new K8sMachineClient().exec(url, HTTPMethod.GET, null, null, cluster);
        if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus())
                && depRes.getStatus() != Constant.HTTP_404) {
            UnversionedStatus status = JsonUtil.jsonToPojo(depRes.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithData(status.getMessage());
        }
        DeploymentList deplist = JsonUtil.jsonToPojo(depRes.getBody(), DeploymentList.class);
        List<Deployment> deps = new ArrayList<Deployment>();
        if (deplist != null && deplist.getItems() != null) {
            deps = deplist.getItems();
        }
        //ingress tcp
        ActionReturnUtil tcpRes = routerService.svcList(namespace);
        if (!tcpRes.isSuccess()) {
            return tcpRes;
        }
        @SuppressWarnings("unchecked")
        List<RouterSvc> tcplist = (List<RouterSvc>) tcpRes.get("data");
        boolean flag = true;
        if (service.getDeploymentDetail() != null && deps != null && deps.size() > 0) {
            for (Deployment dep : deps) {
                if (service.getDeploymentDetail().getName().equals(dep.getMetadata().getName())) {
                    msg.put("服务名称:" + service.getDeploymentDetail().getName(), "重复");
                    flag = false;
                }
            }
        }
        //check ingress
        if (service.getIngress() != null && service.getIngress().size() > 0) {
            for (IngressDto ing : service.getIngress()) {
                if (ing.getType() != null && "HTTP".equals(ing.getType())) {
                    if (!IngressControllerConstant.IC_DEFAULT_NAME.equals(ing.getParsedIngressList().getIcName())) {
                        K8SClientResponse response = icService.getIngressController(ing.getParsedIngressList().getIcName(), cluster);
                        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
                            msg.put("Ingress(Http):" + ing.getParsedIngressList().getName(), ErrorCodeMessage.INGRESS_CONTROLLER_NOT_FOUND.phrase());
                            flag = false;
                            break;
                        }
                    }
                    boolean isExist = routerService.checkIngressName(cluster, ing.getParsedIngressList().getName());
                    if (isExist) {
                        msg.put("Ingress(Http):" + ing.getParsedIngressList().getName(), "重复");
                        flag = false;
                        break;
                    }
                }
                if (ing.getType() != null && "TCP".equals(ing.getType()) && tcplist != null && tcplist.size() > 0) {
                    for (RouterSvc tcp : tcplist) {
                        if (("routersvc" + ing.getSvcRouter().getName()).equals(tcp.getName())) {
                            msg.put("Ingress(Tcp):" + ing.getSvcRouter().getName(), "重复");
                            flag = false;
                        }
                    }
                }
            }
        }
        if (flag) {
            return ActionReturnUtil.returnSuccess();
        } else {
            return ActionReturnUtil.returnErrorWithData(msg);
        }
    }

    @Override
    public ActionReturnUtil listTemplateTagsByName(String name, String tenant, String projectId) throws Exception {
        if (StringUtils.isBlank(name) || StringUtils.isBlank(tenant) || StringUtils.isBlank(projectId)) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        List<ServiceTemplates> list = serviceTemplatesMapper.listServiceByTenant(name, tenant, projectId);
        JSONObject json = new JSONObject();
        if (list != null && list.size() > 0) {
            JSONArray array = new JSONArray();
            json.put("name", name);
            for (ServiceTemplates s : list) {
                JSONObject js = new JSONObject();
                js.put("tag", s.getTag());
                js.put("id", s.getId());
                array.add(js);
            }
            json.put("tags", array);
            return ActionReturnUtil.returnSuccessWithData(json);
        } else {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.SERVICE_TEMPLATE_NOT_EXIST);
        }
    }

    @Override
    public ActionReturnUtil delById(int id) throws Exception {
        if (id != 0) {
            serviceTemplatesMapper.deleteById(id);
            return ActionReturnUtil.returnSuccess();
        } else {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.INVALID_PARAMETER);
        }
    }

    @Override
    public ActionReturnUtil switchPub(String name, boolean isPublic) throws Exception {
        // TODO Auto-generated method stub
        AssertUtil.notBlank(name, DictEnum.NAME);
        serviceTemplatesMapper.updateServiceTemplatePublic(name, isPublic);
        return null;
    }

    public ServiceTemplateDto getServiceTemplateDtoByServiceTemplate(ServiceTemplates serviceTemplate, String app, String name, String tag, String namespace, String projectId){
        ServiceTemplateDto serviceDto = new ServiceTemplateDto();
        serviceDto.setName(name);
        serviceDto.setTag(tag);
        serviceDto.setId(serviceTemplate.getId());
        serviceDto.setTenant(serviceTemplate.getTenant());
        String content = JSONArray.fromObject(serviceTemplate.getDeploymentContent()).getJSONObject(0).toString().replaceAll(":\"\",", ":" + null + ",").replaceAll(":\"\"", ":" + null + "");
        switch(ServiceTypeEnum.valueOf(serviceTemplate.getServiceType())){
            case DEPLOYMENT:
                DeploymentDetailDto deployment = JsonUtil.jsonToPojo(content, DeploymentDetailDto.class);
                deployment.setNamespace(namespace);
                deployment.setName(app);
                serviceDto.setDeploymentDetail(deployment);
                break;
            case STATEFULSET:
                StatefulSetDetailDto statefulSet = JsonUtil.jsonToPojo(content, StatefulSetDetailDto.class);
                statefulSet.setNamespace(namespace);
                statefulSet.setName(app);
                serviceDto.setStatefulSetDetail(statefulSet);
                break;
        }
        if (!StringUtils.isEmpty(serviceTemplate.getIngressContent())) {
            JSONArray jsarray = JSONArray.fromObject(serviceTemplate.getIngressContent());
            List<IngressDto> ingress = new LinkedList<IngressDto>();
            if (jsarray != null && jsarray.size() > 0) {
                for (int j = 0; j < jsarray.size(); j++) {
                    JSONObject ingressJson = jsarray.getJSONObject(j);
                    IngressDto ing = JsonUtil.jsonToPojo(ingressJson.toString().toString().replaceAll(":\"\",", ":" + null + ",").replaceAll(":\"\"", ":" + null + ""), IngressDto.class);
                    if (ing.getParsedIngressList() != null) {
                        ParsedIngressListDto http = ing.getParsedIngressList();
                        http.setNamespace(namespace);
                        if (http.getRules() != null && http.getRules().size() > 0) {
                            for (HttpRuleDto r : http.getRules()) {
                                r.setService(app);
                            }
                        }
                    }
                    if (ing.getSvcRouter() != null) {
                        SvcRouterDto tcp = ing.getSvcRouter();
                        tcp.setNamespace(namespace);
                        tcp.setApp(app);
                        SelectorDto selector = new SelectorDto();
                        selector.setApp(app);
                        tcp.setSelector(selector);
                    }
                    ingress.add(ing);
                }
            }
            serviceDto.setIngress(ingress);
        }
        serviceDto.setProjectId(projectId);
        return serviceDto;
    }

    @Override
    public int deleteTemplateByClusterId(String clusterId) {
        return serviceTemplatesMapper.deleteByClusterId(clusterId);
    }

    @Override
    public ActionReturnUtil checkResourceQuota(String projectId, String namespace, String name) throws Exception {
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        //获取模板内的cpu和内存
        ServiceTemplates serviceTemplate = serviceTemplatesMapper.getSpecificService(name, null, cluster.getId(), projectId);
        if (null == serviceTemplate) {
            throw new MarsRuntimeException(ErrorCodeMessage.SERVICE_TEMPLATE_NOT_EXIST);
        }
        Map<String, Long> serviceRequireRes = this.getServiceRequireResource(serviceTemplate);
        //获取分区内剩下的资源
        Map<String, String> remainResource = namespaceService.getNamespaceResourceRemainQuota(namespace);
        return namespaceService.checkResourceInTemplateDeploy(serviceRequireRes, remainResource);
    }

    @Override
    public Map<String, Long> getServiceRequireResource(ServiceTemplates serviceTemplate) throws Exception {
        long cpuTotal = 0;          //单位m
        long memoryTotal = 0;       //单位是MB
        Map<String, Long> storage = new HashMap<>();
        List<String> volumeNameList = new ArrayList<>();
        int replicas = 0;
        if (StringUtils.isNotBlank(serviceTemplate.getDeploymentContent())) {
            ServiceTypeEnum serviceType = ServiceTypeEnum.valueOf(serviceTemplate.getServiceType());
            String content = JSONArray.fromObject(serviceTemplate.getDeploymentContent()).getJSONObject(0).toString().replaceAll(":\"\",", ":" + null + ",").replaceAll(":\"\"", ":" + null + "");
            List<CreateContainerDto> containerList = null;
            if(serviceType == ServiceTypeEnum.DEPLOYMENT) {
                DeploymentDetailDto deployment = JsonUtil.jsonToPojo(content, DeploymentDetailDto.class);
                //获取实例数
                replicas = Integer.parseInt(deployment.getInstance());
                containerList = deployment.getContainers();
            }else if(serviceType == ServiceTypeEnum.STATEFULSET){
                StatefulSetDetailDto statefulSet = JsonUtil.jsonToPojo(content, StatefulSetDetailDto.class);
                replicas = Integer.parseInt(statefulSet.getInstance());
                containerList = statefulSet.getContainers();
                this.getStorageRequireResource(statefulSet.getInitContainers(), volumeNameList, storage);
            }
            this.getStorageRequireResource(containerList, volumeNameList, storage);
            for (CreateContainerDto container : containerList) {
                CreateResourceDto resourceDto = container.getResource();
                long cpu = resourceDto.getCpu().indexOf("m") > -1 ? Long.valueOf(resourceDto.getCpu().substring(0, resourceDto.getCpu().length() -1)) : Integer.valueOf(resourceDto.getCpu());
                long memory = Long.valueOf(resourceDto.getMemory());
                cpuTotal += cpu * replicas;
                memoryTotal += memory * replicas;
            }
        }
        Map<String, Long> res = new HashMap<>();
        res.put("cpuNeed", cpuTotal);
        res.put("memoryNeed", memoryTotal);
        for(String storageClassName : storage.keySet()){
            res.put(CommonConstant.STORAGE + CommonConstant.SLASH +storageClassName, storage.get(storageClassName) * replicas);
        }
        return res;
    }

    private void getStorageRequireResource(List<CreateContainerDto> containers, List<String> volumeNameList, Map<String, Long> storage){
        if(CollectionUtils.isNotEmpty(containers)) {
            for(CreateContainerDto container : containers) {
                if (CollectionUtils.isNotEmpty(container.getStorage())) {
                    for (PersistentVolumeDto persistentVolumeDto : container.getStorage()) {
                        if (Constant.VOLUME_TYPE_STORAGECLASS.equals(persistentVolumeDto.getType()) && !volumeNameList.contains(persistentVolumeDto.getVolumeName())) {
                            String storageClassName = persistentVolumeDto.getStorageClassName();
                            String capacity = persistentVolumeDto.getCapacity();
                            if (storage.get(storageClassName) != null) {
                                storage.put(storageClassName, storage.get(storageClassName) + Long.valueOf(capacity));
                            } else {
                                storage.put(storageClassName, Long.valueOf(capacity));
                            }
                            volumeNameList.add(persistentVolumeDto.getVolumeName());
                        }
                    }
                }
            }
        }
    }

    @Override
    public ActionReturnUtil checkServiceTemplateName(String name, String projectId, String clusterId) throws Exception {
        //判断重名
        List<ServiceTemplates> serviceTemplateList = serviceTemplatesMapper.listTplByNameAndProjectAndCluster(name, clusterId, false, projectId);
        if (serviceTemplateList != null && serviceTemplateList.size() > 0) {
            int svcTmpId = serviceTemplateList.get(0).getId();
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.SERVICE_TEMPLATE_NAME_DUPLICATE, BLANKSTRING + String.valueOf(svcTmpId));
        }
        return ActionReturnUtil.returnSuccess();
    }

    /**
     * 服务停止，启动时 修改metadata中的annotations
     * @param anno
     * @param name
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, Object> updateAnnotation(Map<String, Object> anno, String name, String action) throws Exception {
        Map<String, Object> annotation = anno;
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        String updateTime = sdf.format(now);
        annotation.put("updateTimestamp", updateTime);
        switch (action) {
            case Constant.STOPPING:
                if (anno.containsKey("nephele/status")) {
                    String status = anno.get("nephele/status").toString();
                    if (status.equals(Constant.STOPPING)) {
                        throw new MarsRuntimeException(ErrorCodeMessage.STOPPED, DictEnum.SERVICE.phrase() + name, true);
                    } else {
                        anno.put("nephele/status", Constant.STOPPING);
                    }
                } else {
                    anno.put("nephele/status", Constant.STOPPING);
                }
                break;
            case Constant.STARTING:
                if (anno.containsKey("nephele/status") && anno.get("nephele/status") != null) {
                    String status = anno.get("nephele/status").toString();
                    if (status.equals(Constant.STARTING)) {
                        throw new MarsRuntimeException(ErrorCodeMessage.STARTED,
                                DictEnum.SERVICE.phrase() + name, true);
                    } else {
                        anno.put("nephele/status", Constant.STARTING);
                        if (anno.get("nephele/replicas") != null) {
                            anno.put("nephele/replicas", anno.get("nephele/replicas").toString());
                        } else {
                            anno.put("nephele/replicas", "1");
                        }
                    }
                } else {
                    anno.put("nephele/status", Constant.STARTING);
                    if (anno.get("nephele/replicas") != null) {
                        anno.put("nephele/replicas", anno.get("nephele/replicas").toString());
                    } else {
                        anno.put("nephele/replicas", "1");
                    }

                }
                break;
            default:
                break;
        }
        return annotation;
    }

    @Override
    public Map<String, Object> updateAnnotationInScale(Map<String, Object> annotation, Integer scale, Integer replicas) throws Exception {
        Map<String, Object> newAnnotation = annotation;
        if (scale == 0) {
            newAnnotation.put("nephele/status", Constant.STOPPING);
            newAnnotation.put("nephele/replicas", replicas.toString());
        } else if (replicas == 0) {
            newAnnotation.put("nephele/status", Constant.STARTING);
            newAnnotation.put("nephele/replicas", scale.toString());
        } else {
            newAnnotation.put("nephele/replicas", scale.toString());
        }
        return newAnnotation;
    }

    @Override
    public ActionReturnUtil deleteServiceResource(String name, String namespace, Cluster cluster, Map<String, Object> queryP, String serviceType) throws Exception {
        //先删除自动伸缩控制
        boolean scaleDeleted = autoScaleService.delete(namespace, name);
        if (!scaleDeleted) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.SERVICE_AUTOSCALE_DELETE_FAILURE);
        }

        //删除pdb
        /*if(pdbService.existPdb(namespace ,name + Constant.PDB_SUFFIX, cluster)){
            K8SClientResponse pdbRes = pdbService.deletePdb(namespace, name + Constant.PDB_SUFFIX, cluster);
            if(!HttpStatusUtil.isSuccessStatus((pdbRes.getStatus()))){
                UnversionedStatus status = JsonUtil.jsonToPojo(pdbRes.getBody(), UnversionedStatus.class);
                return ActionReturnUtil.returnErrorWithData(status.getMessage());
            }
        }*/


        // 删除configmap
        K8SURL cUrl = new K8SURL();
        cUrl.setNamespace(namespace).setResource(Resource.CONFIGMAP);
        K8SClientResponse conRes = new K8sMachineClient().exec(cUrl, HTTPMethod.DELETE, null, queryP, cluster);
        if (!HttpStatusUtil.isSuccessStatus(conRes.getStatus()) && conRes.getStatus() != Constant.HTTP_404) {
            LOGGER.error("删除configmap失败,{}:{}, error:{}", serviceType, name, conRes.getBody());
            throw new MarsRuntimeException(ErrorCodeMessage.SERVICE_DELETE_FAILURE);
        }

        // 删除ingress
        cUrl.setResource(Resource.INGRESS);
        K8SClientResponse ingRes = new K8sMachineClient().exec(cUrl, HTTPMethod.DELETE, null, queryP, cluster);
        if (!HttpStatusUtil.isSuccessStatus(ingRes.getStatus()) && ingRes.getStatus() != Constant.HTTP_404) {LOGGER.error("删除Ingress失败,DeploymentName:{}, error:{}", name, ingRes.getBody());throw new MarsRuntimeException(ErrorCodeMessage.SERVICE_DELETE_FAILURE);}

        // update pvc
        Map<String, Object> pvclabel = new HashMap<String, Object>();
        ServiceTypeEnum serviceTypeEnum = ServiceTypeEnum.valueOf(serviceType.toUpperCase());
        String key = null;
        String autoPvcKey = null;
        List<PersistentVolumeClaim> pvcList = new ArrayList<>();
        switch(serviceTypeEnum){
            case DEPLOYMENT:
                key = CommonConstant.LABEL_KEY_APP + CommonConstant.SLASH + name;
                break;
            case STATEFULSET:
                key = Constant.NODESELECTOR_LABELS_PRE + CommonConstant.LABEL_KEY_STATEFULSET + CommonConstant.LINE + name;
                autoPvcKey = Constant.NODESELECTOR_LABELS_PRE + CommonConstant.LABEL_KEY_STATEFULSET;
                break;
        }
        pvclabel.put("labelSelector", key + CommonConstant.EQUALITY_SIGN + name);
        K8SClientResponse pvcRes = pvcService.doSepcifyPVC(namespace, pvclabel, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(pvcRes.getStatus()) && pvcRes.getStatus() != Constant.HTTP_404) {
            UnversionedStatus status = JsonUtil.jsonToPojo(pvcRes.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithData(status.getMessage());
        }
        PersistentVolumeClaimList persistentVolumeClaimList = K8SClient.converToBean(pvcRes, PersistentVolumeClaimList.class);
        if(persistentVolumeClaimList != null) {
            pvcList.addAll(persistentVolumeClaimList.getItems());
        }

        //查询有状态服务自动供应的pvc
        if(StringUtils.isNotBlank(autoPvcKey)){
            pvclabel.put("labelSelector", autoPvcKey + CommonConstant.EQUALITY_SIGN + name);
            K8SClientResponse autoPvcRes = pvcService.doSepcifyPVC(namespace, pvclabel, HTTPMethod.GET, cluster);
            if (!HttpStatusUtil.isSuccessStatus(autoPvcRes.getStatus()) && autoPvcRes.getStatus() != Constant.HTTP_404) {
                UnversionedStatus status = JsonUtil.jsonToPojo(autoPvcRes.getBody(), UnversionedStatus.class);
                return ActionReturnUtil.returnErrorWithData(status.getMessage());
            }
            PersistentVolumeClaimList autoPersistentVolumeClaimList = K8SClient.converToBean(autoPvcRes, PersistentVolumeClaimList.class);
            if(autoPersistentVolumeClaimList != null) {
                autoPersistentVolumeClaimList.getItems().stream().forEach(pvc->pvc.getMetadata().getLabels().remove(Constant.NODESELECTOR_LABELS_PRE + CommonConstant.LABEL_KEY_STATEFULSET));
                pvcList.addAll(autoPersistentVolumeClaimList.getItems());
            }
        }

        if (CollectionUtils.isNotEmpty(pvcList)) {
            for (PersistentVolumeClaim onePvc : pvcList) {
                onePvc.getMetadata().getLabels().remove(key);
                K8SClientResponse response = pvcService.updatePvcByName(onePvc,cluster);
                if (!HttpStatusUtil.isSuccessStatus(response.getStatus()) && response.getStatus() != Constant.HTTP_404) {
                    LOGGER.error("更新PVC失败,ServiceName:{}, error:{}", name, response.getBody());
                    throw new MarsRuntimeException(ErrorCodeMessage.SERVICE_DELETE_FAILURE);
                }
            }
        }
        NamespaceLocal namespaceLocal = namespaceLocalService.getNamespaceByName(namespace);
        List<IngressControllerDto> icList = new ArrayList<>();
        if(namespaceLocal != null) {
            //通过tenantId找icName
            icList.addAll(tenantService.getTenantIngressController(namespaceLocal.getTenantId(), cluster.getId()));
        }
        //删除对外暴露端口（nginx和数据库）
        routerService.deleteRulesByName(namespace, name, icList, cluster);


        // 获取service
        K8SClientResponse svcRes = sService.doServiceByNamespace(namespace, null, queryP, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(svcRes.getStatus())) {
            LOGGER.error("获取Service失败,DeploymentName:{}, error:{}", name, svcRes.getBody());
            throw new MarsRuntimeException(ErrorCodeMessage.SERVICE_DELETE_FAILURE);
        }
        ServiceList svcs = JsonUtil.jsonToPojo(svcRes.getBody(), ServiceList.class);

        // 循环删除service,在k8s中service只能根据名称删除
        List<com.harmonycloud.k8s.bean.Service> svc = svcs.getItems();
        K8SURL svcUrl = new K8SURL();
        svcUrl.setNamespace(namespace).setResource(Resource.SERVICE);
        for (int i = 0; i < svc.size(); i++) {
            svcUrl.setName(svc.get(i).getMetadata().getName());
            K8SClientResponse serviceRes = new K8sMachineClient().exec(svcUrl, HTTPMethod.DELETE, null, null, cluster);
            if (!HttpStatusUtil.isSuccessStatus(serviceRes.getStatus()) && serviceRes.getStatus() != Constant.HTTP_404) {
                LOGGER.error("删除Service失败,ServiceName:{}, error:{}", svc.get(i).getMetadata().getName(), serviceRes.getBody());
                throw new MarsRuntimeException(ErrorCodeMessage.SERVICE_DELETE_FAILURE);
            }
        }

        //删除文件上传到容器记录
        fileUploadToContainerService.deleteUploadRecord(namespace, name);
        return ActionReturnUtil.returnSuccess();
    }

    /**
     * 根据id删除应用商店应用下的服务模板
     * @param appId
     * @throws Exception
     */
    @Override
    public void deleteServiceTemplateByAppId(int appId) throws Exception {
        serviceTemplatesMapper.deleteByAppId(appId);
    }

    /**
     * 根据id获取应用商店应用下的服务模板
     * @param appId
     * @return
     * @throws Exception
     */
    @Override
    public List<ServiceTemplates> listServiceTemplateByAppId(int appId) throws Exception {
        return serviceTemplatesMapper.listByAPPId(appId);
    }
}
