package com.harmonycloud.service.application.impl;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.AssertUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.dao.application.ServiceTemplatesMapper;
import com.harmonycloud.dao.application.bean.ServiceTemplates;
import com.harmonycloud.dto.application.*;
import com.harmonycloud.k8s.bean.Deployment;
import com.harmonycloud.k8s.bean.DeploymentList;
import com.harmonycloud.k8s.bean.UnversionedStatus;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by root on 3/29/17.
 */

@Service
@Transactional(rollbackFor = Exception.class)
public class ServiceServiceImpl implements ServiceService {

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
    PrivatePartitionService privatePartitionService;

    @Autowired
    NamespaceService namespaceService;

    @Autowired
    HttpSession session;

    @Autowired
    TenantService tenantService;

    @Autowired
    ApplicationDeployService applicationDeployService;

    @Autowired
    NamespaceLocalService namespaceLocalService;
    @Autowired
    RoleLocalService roleLocalService;

    @Autowired
    UserService userService;

    @Autowired
    private ClusterService clusterService;

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

        if (serviceTemplate.getDeploymentDetail() != null) {
            JSONArray deployment = JSONArray.fromObject(serviceTemplate.getDeploymentDetail());
            serviceTemplateDB.setDeploymentContent(deployment.toString());
            List<CreateContainerDto> containers = serviceTemplate.getDeploymentDetail().getContainers();
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
    public ActionReturnUtil listServiceTemplate(String name, String clusterId, boolean isPublic, String projectId) throws Exception {
        JSONArray array = new JSONArray();
        // check value null

        // list
        List<ServiceTemplates> serviceBytenant = serviceTemplatesMapper.listNameByProjectId(name, clusterId, isPublic, projectId);

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
                String dep = JSONArray.fromObject(serviceTemplatesList.get(i).getDeploymentContent()).getJSONObject(0).toString().replaceAll(":\"\",", ":" + null + ",").replaceAll(":\"\"", ":" + null + "");
                DeploymentDetailDto deployment = JsonUtil.jsonToPojo(dep, DeploymentDetailDto.class);
                idAndTag.put("name", deployment.getName());
                idAndTag.put("realName", userService.getUser(serviceTemplatesList.get(i).getUser()).getRealName());
                tagArray.add(idAndTag);
                json.put("createtime", dateToString(serviceTemplatesList.get(i).getCreateTime()));
            }
            json.put("tags", tagArray);
            json.put("clusterId", serviceTemplatesList.get(0).getClusterId());
            json.put("clusterName", clusterService.findClusterById(serviceTemplatesList.get(0).getClusterId()).getAliasName());
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

        List<String> errorMessage = new ArrayList<>();
        //获取serviceList
        List<Deployment> items = new ArrayList<>();

        for (ServiceNameNamespace nn : deployedServiceNamesDto.getServiceList()) {
            //获取集群
            Cluster cluster = namespaceLocalService.getClusterByNamespaceName(nn.getNamespace());
            if (Objects.isNull(cluster)) {
                return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.CLUSTER_NOT_FOUND);
            }
            K8SURL url = new K8SURL();
            url.setResource(Resource.DEPLOYMENT);

            //labels
            Map<String, Object> bodys = new HashMap<String, Object>();
            url.setNamespace(nn.getNamespace());
            url.setName(nn.getName());
            K8SClientResponse depRes = new K8sMachineClient().exec(url, HTTPMethod.GET, null, bodys, cluster);
            if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus()) && depRes.getStatus() != Constant.HTTP_404) {
                UnversionedStatus sta = JsonUtil.jsonToPojo(depRes.getBody(), UnversionedStatus.class);
                return ActionReturnUtil.returnErrorWithData(sta.getMessage());
            }
            Deployment deployment = JsonUtil.jsonToPojo(depRes.getBody(), Deployment.class);
            if (deployment != null) {
                items.add(deployment);
            }
        }

        if (items.size() > 0) {

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
        return ActionReturnUtil.returnSuccess();

    }

    @Override
    public ActionReturnUtil listServiceTemplate(String searchKey, String searchValue, String clusterId, boolean isPublic, String projectId) throws Exception {
        JSONArray array = new JSONArray();
        List<ServiceTemplates> serviceBytenant = null;
        //公私有模板
        if (isPublic) {
            //公有模板
            serviceBytenant = listPublicServiceTemplate(searchKey, searchValue);
        } else {
            //私有模板
            serviceBytenant = listPrivateServiceTemplate(searchKey, searchValue, clusterId, projectId);
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
    private List<ServiceTemplates> listPublicServiceTemplate(String searchKey, String searchvalue) throws Exception {
        List<ServiceTemplates> serviceBytenant = null;
        if (!StringUtils.isEmpty(searchKey)) {
            if (searchKey.equals("name")) {
                // search by name
                serviceBytenant = serviceTemplatesMapper.listPublicSearchByName(searchvalue, true);
            } else if (searchKey.equals("image")) {
                // search by image
                serviceBytenant = serviceTemplatesMapper.listPublicSearchByImage(searchvalue, true);
            }
        } else {
            serviceBytenant = serviceTemplatesMapper.listPublicNameByTenant(null, true);
        }
        return serviceBytenant;
    }

    /**
     * 私有模板
     */
    private List<ServiceTemplates> listPrivateServiceTemplate(String searchKey, String searchValue, String clusterId, String projectId) throws Exception {
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
                List<ServiceTemplates> tmpNameList = serviceTemplatesMapper.listSearchByName(searchValue, cId, false, projectId);
                List<ServiceTemplates> tmpImageList = serviceTemplatesMapper.listSearchByImage(searchValue, cId, false, projectId);
                tmpNameList.addAll(tmpImageList);
                tmpList = tmpNameList.stream().distinct().collect(Collectors.toList());
            } else {
                tmpList = serviceTemplatesMapper.listNameByProjectId(null, cId, false, projectId);
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
            return deployService(serviceDeploy, userName);
        } else {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.SERVICE_TEMPLATE_NOT_EXIST);
        }


    }

    @Override
    public ActionReturnUtil deployService(ServiceDeployDto serviceDeploy, String userName) throws Exception {
        if (serviceDeploy == null || StringUtils.isBlank(serviceDeploy.getNamespace()) || Objects.isNull(serviceDeploy.getServiceTemplate()) || Objects.isNull(serviceDeploy.getServiceTemplate().getDeploymentDetail())) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }

        //判断项目是否存在
        if (StringUtils.isBlank(serviceDeploy.getServiceTemplate().getProjectId()) || "null".equals(serviceDeploy.getServiceTemplate().getProjectId())) {
            throw new MarsRuntimeException(ErrorCodeMessage.PROJECT_NOT_EXIST);
        }
        //判断镜像是否为空
        for (CreateContainerDto c : serviceDeploy.getServiceTemplate().getDeploymentDetail().getContainers()) {
            if (StringUtils.isBlank(c.getImg())) {
                return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.SERVICE_TEMPLATE_IMAGE_INFO_NOT_NULL);
            }
        }

        //获取集群
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(serviceDeploy.getNamespace());
        if (Objects.isNull(cluster)) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        List<String> pvcList = new ArrayList<>();
        List<Map<String, Object>> message = new ArrayList<>();
        String namespace = serviceDeploy.getNamespace();
        Set<String> deployments = new HashSet<>();
        ServiceTemplateDto service = serviceDeploy.getServiceTemplate();
        deployments.add(service.getDeploymentDetail().getName());
        //check name
        ActionReturnUtil res = checkService(service, cluster, namespace);
        if (!res.isSuccess()) {
            return res;
        }
        // creat pvc
        for (CreateContainerDto c : service.getDeploymentDetail().getContainers()) {
            if (c.getStorage() != null) {
                for (PersistentVolumeDto pvc : c.getStorage()) {
                    if (pvc.getType() != null && Constant.VOLUME_TYPE_PV.equals(pvc.getType())) {
                        if (StringUtils.isBlank(pvc.getPvcName())) {
                            continue;
                        }
                        pvc.setNamespace(namespace);
                        pvc.setServiceName(serviceDeploy.getServiceTemplate().getDeploymentDetail().getName());
                        pvc.setProjectId(serviceDeploy.getServiceTemplate().getProjectId());
                        pvc.setVolumeName(pvc.getPvcName());
                        ActionReturnUtil pvcres = volumeSerivce.createVolume(pvc);
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
            message.addAll(routerService.createExternalRule(service, serviceDeploy.getNamespace()));
        }

        // creat config map & deploy service deployment & get node label by
        // namespace
        // todo so bad
        service.getDeploymentDetail().setNamespace(namespace);
        for (CreateContainerDto c : service.getDeploymentDetail().getContainers()) {
            c.setImg(cluster.getHarborServer().getHarborHost() + "/" + c.getImg());
        }
        service.getDeploymentDetail().setProjectId(serviceDeploy.getServiceTemplate().getProjectId());
        ActionReturnUtil depRes = deploymentsService.createDeployment(service.getDeploymentDetail(), userName, null,
                cluster);
        if (!depRes.isSuccess()) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(service.getName(), depRes.get("data"));
            message.add(map);
        }
        if (message.size() > 0) {
            ActionReturnUtil rollbackRes = applicationDeployService.rollBackDeployment(deployments, namespace, userName, cluster);
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
        String dep = JSONArray.fromObject(serviceTemplate.getDeploymentContent()).getJSONObject(0).toString().replaceAll(":\"\",", ":" + null + ",").replaceAll(":\"\"", ":" + null + "");
        DeploymentDetailDto deployment = JsonUtil.jsonToPojo(dep, DeploymentDetailDto.class);
        deployment.setNamespace(namespace);
        String oldName = deployment.getName();
        deployment.setName(app);
        List<CreateContainerDto> cons = deployment.getContainers();
        if (cons != null && cons.size() > 0) {
            for (CreateContainerDto c : cons) {
                if (c.getStorage() != null && c.getStorage().size() > 0) {
                    for (PersistentVolumeDto v : c.getStorage()) {
                        if (v.getPvcName() != null && v.getPvcName() != "") {
                            v.setPvcName(v.getPvcName().replace("-" + oldName, "-" + app));
                        }
                    }
                }
            }
        }
        serviceDto.setDeploymentDetail(deployment);
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
        if (StringUtils.isNotBlank(serviceTemplate.getDeploymentContent())) {
            String dep = JSONArray.fromObject(serviceTemplate.getDeploymentContent()).getJSONObject(0).toString().replaceAll(":\"\",", ":" + null + ",").replaceAll(":\"\"", ":" + null + "");
            DeploymentDetailDto deployment = JsonUtil.jsonToPojo(dep, DeploymentDetailDto.class);
            //获取实例数
            int replicas = Integer.parseInt(deployment.getInstance());
            List<CreateContainerDto> containerList = deployment.getContainers();
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
        return res;
    }

    @Override
    public ActionReturnUtil checkServiceTemplateName(String name, String projectId, String clusterId) throws Exception {
        //判断重名
        List<ServiceTemplates> serviceTemplateList = serviceTemplatesMapper.listTplByNameAndProjectAndCluster(name, clusterId, false, projectId);
        if (serviceTemplateList != null && serviceTemplateList.size() > 0) {
            int svcTmpId = serviceTemplateList.get(0).getId();
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.SERVICE_TEMPLATE_NAME_DUPLICATE, String.valueOf(svcTmpId));
        }
        return ActionReturnUtil.returnSuccess();
    }
}