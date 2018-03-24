package com.harmonycloud.service.application.impl;

import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.*;
import com.harmonycloud.dao.tenant.bean.NamespaceLocal;
import com.harmonycloud.dao.tenant.bean.Project;
import com.harmonycloud.dao.tenant.bean.TenantBinding;
import com.harmonycloud.dto.application.*;
import com.harmonycloud.k8s.bean.*;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.service.DeploymentService;
import com.harmonycloud.k8s.service.TprApplication;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import com.harmonycloud.service.application.*;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.cluster.LoadbalanceService;
import com.harmonycloud.service.common.PrivilegeHelper;
import com.harmonycloud.service.platform.bean.ApplicationList;
import com.harmonycloud.service.platform.bean.RouterSvc;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.tenant.*;
import com.harmonycloud.service.user.RoleLocalService;
import com.harmonycloud.service.user.UserService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Created by root on 4/10/17.
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ApplicationDeployServiceImpl implements ApplicationDeployService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationDeployServiceImpl.class);

    @Autowired
    private TenantService tenantService;

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private PersistentVolumeService volumeSerivce;

    @Autowired
    private DeploymentsService deploymentsService;

    @Autowired
    private ApplicationTemplateService appTemService;

    @Autowired
    private RouterService routerService;

    @Autowired
    private ApplicationService applicationService;

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
    private LoadbalanceService loadbalanceService;

    @Value("#{propertiesReader['kube.topo']}")
    private String kubeTopo;

    private static final String SIGN = "-";
    private static final String SIGN_EQUAL = "=";
    private final static String TOPO = "topo";
    private final static String CREATE = "creater";

    @Autowired
    private NamespaceLocalService namespaceLocalService;

    @Autowired
    private PrivilegeHelper privilegeHelper;

    @Autowired
    private UserService userService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private RoleLocalService roleLocalService;

    /**
     * get application by tenant namespace name status service implement
     *
     * @param projectId project id
     * @param tenantId  tenant id
     * @param namespace namespace
     * @param name      application name
     * @param status    application running status 0:abnormal;1:normal
     * @return ActionReturnUtil
     * @author gurongyun
     */
    @SuppressWarnings("rawtypes")
    @Override
    public ActionReturnUtil searchApplication(String projectId, String tenantId, String namespace, String name, String status) throws Exception {
        if (StringUtils.isEmpty(projectId) || StringUtils.isEmpty(tenantId)) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        // application list
        List<ApplicationDto> array = new ArrayList<>();
        List<BaseResource> appCrdList = new ArrayList<>();

        //查询应用的第三方资源 http body
        Map<String, Object> bodys = new HashMap<>();
        String projectLabel = Constant.NODESELECTOR_LABELS_PRE + Constant.LABEL_PROJECT_ID + "=" + projectId;
        bodys.put("labelSelector", projectLabel);

        //当projectId不为空,namespace为空
        if (StringUtils.isEmpty(namespace) && StringUtils.isNotEmpty(projectId)) {
            List<NamespaceLocal> namespaceData = this.namespaceLocalService.getNamespaceListByTenantId(tenantId);
            List<Cluster> clusterList = roleLocalService.listCurrentUserRoleCluster();
            for (NamespaceLocal oneNamespace : namespaceData) {
                String namespaceName = oneNamespace.getNamespaceName();
                if (StringUtils.isBlank(namespaceName)) {
                    continue;
                }
                //判断该namespace是否有权限
                if (clusterList.stream().noneMatch((c) -> c.getId().equals(oneNamespace.getClusterId()))) {
                    continue;
                }
                List<BaseResource> list = getApplicationList(namespaceName, bodys);
                if (list != null && list.size() > 0) {
                    appCrdList.addAll(list);
                }
            }
        }
        if (StringUtils.isNotEmpty(namespace) && StringUtils.isNotEmpty(projectId)) {
            String[] namespaces = {namespace};
            if (namespace.contains(",")) {
                namespaces = namespace.split(",");
            }
            for (String oneNamespace : namespaces) {
                List<BaseResource> list = getApplicationList(oneNamespace, bodys);
                if (list != null && list.size() > 0) {
                    appCrdList.addAll(list);
                }
            }
        }

        if (appCrdList != null && appCrdList.size() > 0) {
            array = convertAppListData(appCrdList, status);
        }

        //数据权限过滤
        return ActionReturnUtil.returnSuccessWithData(privilegeHelper.filter(array));
    }

    private List<BaseResource> getApplicationList(String namespace, Map<String, Object> bodys) throws Exception {
        Cluster cluster = this.namespaceLocalService.getClusterByNamespaceName(namespace);
        K8SClientResponse response = tprApplication.listApplicationByNamespace(namespace, null, bodys, HTTPMethod.GET, cluster);
        if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            BaseResourceList tpr = JsonUtil.jsonToPojo(response.getBody(), BaseResourceList.class);
            if (tpr != null) {
                return tpr.getItems();
            }
        }
        LOGGER.error("getApplicationList error, namespace:{},response:{}",namespace,
                com.alibaba.fastjson.JSONObject.toJSONString(response));
        return Collections.emptyList();
    }

    private List<ApplicationDto> convertAppListData(List<BaseResource> appCrdList, String status) throws Exception {
        List<ApplicationDto> array = new ArrayList<>();
        for (BaseResource bs : appCrdList) {
            ApplicationDto app = new ApplicationDto();
            String label = "";
            String user = "";
            boolean isMsf = false;
            String projectId = null;
            for (Map.Entry<String, Object> vo : bs.getMetadata().getLabels().entrySet()) {
                if (vo.getKey().startsWith(TOPO)) {
                    label = vo.getKey() + "=" + vo.getValue();
                }
                if (CREATE.equals(vo.getKey())) {
                    user = vo.getValue().toString();
                }
                if ((Constant.NODESELECTOR_LABELS_PRE + "springcloud").equals(vo.getKey())) {
                    isMsf = true;
                }
                if ((Constant.NODESELECTOR_LABELS_PRE + "projectId").equals(vo.getKey())) {
                    projectId = vo.getValue().toString();
                }
            }
            // put application info
            app.setProjectId(projectId);
            app.setMsf(isMsf);
            app.setName(bs.getMetadata().getName());
            app.setId(label);
            if (Objects.nonNull(bs.getMetadata().getAnnotations()) && Objects.nonNull(bs.getMetadata().getAnnotations().get("nephele/annotation"))) {
                app.setDesc(bs.getMetadata().getAnnotations().get("nephele/annotation").toString());
            }
            app.setNamespace(bs.getMetadata().getNamespace());
            app.setAliasNamespace(namespaceLocalService.getNamespaceByName(app.getNamespace()).getAliasName());
            //获取创建时间
            app.setCreateTime(bs.getMetadata().getCreationTimestamp());
            app.setUser(user);
            app.setRealName(userService.getUser(user).getRealName());
            Cluster cluster = this.namespaceLocalService.getClusterByNamespaceName(bs.getMetadata().getNamespace());
            JSONObject serviceJson = listServiceByBusinessId(label, bs.getMetadata().getNamespace(), cluster);
            app.setClusterId(cluster.getId());
            app.setStatus(serviceJson.get("status").toString());
            app.setStart(Integer.valueOf(serviceJson.get("start").toString()));
            app.setTotal(Integer.valueOf(serviceJson.get("total").toString()));
            app.setStarting(Integer.valueOf(serviceJson.get("starting").toString()));
            app.setStop(Integer.valueOf(serviceJson.get("stop").toString()));
            if (StringUtils.isBlank(status)) {
                array.add(app);
            } else {
                if (status.equals(app.getStatus())) {
                    array.add(app);
                }
            }
        }
        return array;
    }

    /**
     * 获取应用下的服务最新时间
     */
    private String getDeploymentTime(String label, String namespace, Cluster cluster) throws Exception {
        Map<String, Object> bodys = new HashMap<>();
        if (!checkParamNUll(label)) {
            bodys.put("labelSelector", label);
        }
        K8SURL url = new K8SURL();
        url.setResource(Resource.DEPLOYMENT).setNamespace(namespace);
        K8SClientResponse depRes = new K8sMachineClient().exec(url, HTTPMethod.GET, null, bodys, cluster);
        if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus()) && depRes.getStatus() != Constant.HTTP_404) {
            return null;
        }
        DeploymentList deployment = JsonUtil.jsonToPojo(depRes.getBody(), DeploymentList.class);
        if (deployment != null && deployment.getItems().size() > 0) {
            List<Deployment> list = deployment.getItems();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            String max = null;
            for (Deployment dep : list) {
                if (max == null) {
                    max = dep.getMetadata().getCreationTimestamp();
                }
                String update = dep.getMetadata().getCreationTimestamp();
                if (dep.getStatus() != null && dep.getStatus().getConditions() != null && dep.getStatus().getConditions().size() > 0) {
                    String maxc = null;
                    for (DeploymentCondition c : dep.getStatus().getConditions()) {
                        if (maxc == null && c.getLastUpdateTime() != null) {
                            maxc = c.getLastUpdateTime();
                        }
                        if (c.getLastUpdateTime() != null) {
                            int b = Long.valueOf(sdf.parse(maxc).getTime()).compareTo(Long.valueOf(sdf.parse(c.getLastUpdateTime()).getTime()));
                            if (b == -1) {
                                maxc = c.getLastUpdateTime();
                            }
                        }
                    }
                    if (maxc != null) {
                        update = maxc;
                    }
                }
                if (dep.getMetadata() != null && dep.getMetadata().getAnnotations() != null) {
                    Map<String, Object> anno = dep.getMetadata().getAnnotations();
                    if (anno.containsKey("updateTimestamp") && anno.get("updateTimestamp") != null) {
                        String updateTime = (String) anno.get("updateTimestamp");
                        int c = Long.valueOf(sdf.parse(update).getTime()).compareTo(Long.valueOf(sdf.parse(updateTime).getTime()));
                        if (c == -1) {
                            update = updateTime;
                        }
                    }
                }
                int a = Long.valueOf(sdf.parse(max).getTime()).compareTo(Long.valueOf(sdf.parse(update).getTime()));
                if (a == -1) {
                    max = update;
                }
            }
            return max;
        }
        return null;
    }

    private boolean checkParamNUll(String p) {
        if (StringUtils.isEmpty(p) || p == null) {
            return true;
        }
        return false;
    }

    /**
     * get application by id service implement.
     *
     * @param id application id
     * @return ActionReturnUtil
     * @author gurongyun
     */
    @SuppressWarnings("unchecked")
    @Override
    public ActionReturnUtil selectApplicationById(String id, String appName, String namespace) throws Exception {
        AssertUtil.notNull(id, DictEnum.APPLICATION_ID);
        JSONObject js = new JSONObject();
        // get application
        String[] namespaces = {};

        if (id.contains(SIGN) && id.contains(SIGN_EQUAL)) {
            namespaces = id.split(SIGN_EQUAL);
        }
        String newNamespace = StringUtils.isBlank(namespace)? namespaces[1] : namespace;

        BaseResource tpr = new BaseResource();
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(newNamespace);
        K8SClientResponse response = tprApplication.getApplicationByName(newNamespace, appName, null, null, HTTPMethod.GET, cluster);
        if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            tpr = JsonUtil.jsonToPojo(response.getBody(), BaseResource.class);
        }

        if (tpr != null) {
            //判断是否是微服务组件应用是否有权限操作
            boolean isOperationable = true;
            boolean isMsf = false;
            Map<String, Object> tprLabels = tpr.getMetadata().getLabels();
            if (tprLabels.containsKey(Constant.NODESELECTOR_LABELS_PRE + "springcloud")) {
                isOperationable = userService.checkCurrentUserIsAdmin();
                isMsf = true;
            }
            js.put("clusterId", cluster.getId());
            js.put("isMsf", isMsf);
            js.put("isOperationable", isOperationable);
            // put application info
            js.put("name", tpr.getMetadata().getName());
            js.put("createTime", tpr.getMetadata().getCreationTimestamp());
            //获取最新更新时间
            String updateTime = getDeploymentTime(id, tpr.getMetadata().getNamespace(), cluster);
            if (updateTime == null) {
                updateTime = tpr.getMetadata().getCreationTimestamp();
            }
            js.put("updateTime", updateTime);
            String anno = "";
            if (tpr.getMetadata().getAnnotations() != null && tpr.getMetadata().getAnnotations().containsKey("nephele/annotation") && tpr.getMetadata().getAnnotations().get("nephele/annotation") != null) {
                anno = tpr.getMetadata().getAnnotations().get("nephele/annotation").toString();
            }
            js.put("desc", anno);
            js.put("namespace", tpr.getMetadata().getNamespace());
            js.put("user", tpr.getMetadata().getLabels().get("creater"));
            js.put("id", id);
            js.put("realName", userService.getUser(js.get("user").toString()).getRealName());
            js.put("aliasNamespace", namespaceLocalService.getNamespaceByName(tpr.getMetadata().getNamespace()).getAliasName());


            Map<String, Object> bodys = new HashMap<>();
            bodys.put("labelSelector", id);
            JSONArray serarray = new JSONArray();

            K8SURL url = new K8SURL();
            url.setNamespace(newNamespace).setResource(Resource.DEPLOYMENT);
            K8SClientResponse depRes = new K8sMachineClient().exec(url, HTTPMethod.GET, null, bodys, cluster);
            if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus())
                    && depRes.getStatus() != Constant.HTTP_404) {
                UnversionedStatus sta = JsonUtil.jsonToPojo(depRes.getBody(), UnversionedStatus.class);
                return ActionReturnUtil.returnErrorWithData(sta.getMessage());
            }
            DeploymentList deplist = JsonUtil.jsonToPojo(depRes.getBody(), DeploymentList.class);
            if (deplist != null && deplist.getItems() != null) {

                List<Deployment> deps = deplist.getItems();
                if (deps != null && deps.size() > 0) {
                    ApplicationDto applicationDto = new ApplicationDto();
                    for (Deployment dep : deps) {
                        JSONObject json = new JSONObject();
                        JSONObject labelJson = new JSONObject();

                        json.put("isExternal", "0");
                        applicationDto.setName(dep.getMetadata().getName());
                        if (privilegeHelper.isFiltered(applicationDto)) {
                            continue;
                        }
                        json.put("name", dep.getMetadata().getName());

                        String labels = null;
                        if (dep.getMetadata().getAnnotations() != null && dep.getMetadata().getAnnotations().containsKey("nephele/labels")) {
                            labels = dep.getMetadata().getAnnotations().get("nephele/labels").toString();
                        }
                        if (!StringUtils.isEmpty(labels)) {
                            String[] arrLabel = labels.split(",");
                            for (String l : arrLabel) {
                                String[] tmp = l.split("=");
                                labelJson.put(tmp[0], tmp[1]);
                            }
                            json.put("labels", labelJson);
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
                                    if (dep.getStatus().getReadyReplicas() != null && dep.getStatus().getReadyReplicas() > 0
                                            && (dep.getStatus().getAvailableReplicas() == dep.getStatus()
                                            .getReadyReplicas())
                                            && dep.getSpec().getReplicas() != null
                                            && dep.getSpec().getReplicas() == dep.getStatus().getReadyReplicas()) {
                                        json.put("status", Constant.SERVICE_START);
                                    } else {
                                        json.put("status", Constant.SERVICE_STARTING);
                                    }
                                    break;
                                case Constant.STOPPING:
                                    if (dep.getStatus().getAvailableReplicas() != null
                                            && dep.getStatus().getAvailableReplicas() > 0) {
                                        json.put("status", Constant.SERVICE_STOPPING);
                                    } else {
                                        json.put("status", Constant.SERVICE_STOP);
                                    }
                                    break;
                                default:
                                    if (dep.getStatus().getAvailableReplicas() != null
                                            && dep.getStatus().getAvailableReplicas() > 0) {
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
                            if (container.getResources() != null && container.getResources().getRequests() != null) {
                                Map<String, String> res = (Map<String, String>) container.getResources().getRequests();
                                cpu.add(res.get("cpu"));
                                memory.add(res.get("memory"));
                            } else if (container.getResources() != null && container.getResources().getLimits() != null) {
                                Map<String, String> res1 = (Map<String, String>) container.getResources().getLimits();
                                cpu.add(res1.get("cpu"));
                                memory.add(res1.get("memory"));
                            }
                        }
                        boolean isPV = false;
                        if (dep.getSpec().getTemplate().getSpec().getVolumes() != null && dep.getSpec().getTemplate().getSpec().getVolumes().size() > 0) {
                            for (Volume v : dep.getSpec().getTemplate().getSpec().getVolumes()) {
                                if (v.getPersistentVolumeClaim() != null) {
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
                        json.put("aliasNamespace", namespaceLocalService.getNamespaceByName(dep.getMetadata().getNamespace()).getAliasName());
                        json.put("selector", dep.getSpec().getSelector());

                        serarray.add(json);

                    }
                }
            }

            //get external service by label
//            K8SURL urlExternal = new K8SURL();
//            urlExternal.setNamespace(Resource.EXTERNALNAMESPACE).setResource(Resource.SERVICE);
//            K8SClientResponse serviceRe = new K8sMachineClient().exec(urlExternal, HTTPMethod.GET, null, bodys, cluster);
//            if (!HttpStatusUtil.isSuccessStatus(serviceRe.getStatus())
//                    && serviceRe.getStatus() != Constant.HTTP_404) {
//                UnversionedStatus sta = JsonUtil.jsonToPojo(serviceRe.getBody(), UnversionedStatus.class);
//                return ActionReturnUtil.returnErrorWithData(sta.getMessage());
//            }
//            ServiceList svclist = JsonUtil.jsonToPojo(serviceRe.getBody(), ServiceList.class);
//            if (svclist != null && svclist.getItems() != null) {
//                List<com.harmonycloud.k8s.bean.Service> svcs = svclist.getItems();
//                if (svcs != null && svcs.size() > 0) {
//
//                    for (com.harmonycloud.k8s.bean.Service svc : svcs) {
//                        JSONObject json = new JSONObject();
//                        // service info
//                        json.put("isExternal", "1");
//                        json.put("name", svc.getMetadata().getName());
//                        json.put("ip", svc.getMetadata().getLabels().get("ip").toString());
//                        json.put("port", svc.getSpec().getPorts().get(0).getTargetPort());
//                        json.put("type", svc.getMetadata().getLabels().get("type").toString());
//                        json.put("createTime", svc.getMetadata().getCreationTimestamp());
//                        json.put("status", Constant.SERVICE_START);
//
//                        serarray.add(json);
//                    }
//                }
//
//            }

            js.put("serviceList", serarray);


        } else {
            return ActionReturnUtil.returnSuccessWithData(null);
        }
        return ActionReturnUtil.returnSuccessWithData(js);
    }

    /**
     * deployment application service implement.
     *
     * @param appDeploy appDeployBean
     * @param username       username
     * @return ActionReturnUtil
     * @author yanli
     */
    @Override
    public synchronized ActionReturnUtil deployApplicationTemplate(ApplicationDeployDto appDeploy, String username) throws Exception {
        //参数判空
        if (Objects.isNull(appDeploy) || StringUtils.isBlank(appDeploy.getAppName()) || StringUtils.isBlank(appDeploy.getProjectId())
                || StringUtils.isBlank(appDeploy.getNamespace()) || Objects.isNull(appDeploy.getAppTemplate()) ) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }

        //判断项目是否存在
        if ("null".equals(appDeploy.getProjectId())) {
            throw new MarsRuntimeException(ErrorCodeMessage.PROJECT_NOT_EXIST);
        }
        String namespace = appDeploy.getNamespace();
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        ActionReturnUtil checkRes = checkK8SName(appDeploy, cluster, true);
        if (!checkRes.isSuccess()) {
            return checkRes;
        }
        deployApplication(appDeploy, username, cluster);
        return ActionReturnUtil.returnSuccess();
    }

    /**
     * delete application service implement
     *
     * @param applicationList application id list
     * @param username     username
     * @return ActionReturnUtil
     * @author yanli
     */
    @Override
    public ActionReturnUtil deleteApplicationTemplate(ApplicationList applicationList, String username) throws Exception {
        AssertUtil.notNull(applicationList, DictEnum.APPLICATION);
        AssertUtil.notEmpty(applicationList.getIdList(), DictEnum.APPLICATION);
        List<String> errorMessage = new ArrayList<>();
        // loop appTemplate
        for (String label : applicationList.getIdList()) {
            String namespace = "";
            Map<String, Object> appBodys = new HashMap<String, Object>();
            appBodys.put("labelSelector", label);
            if (label != null && label.contains(SIGN_EQUAL)) {
                String[] value = label.split(SIGN_EQUAL);
                if (value != null) {
                    namespace = value[1];
                }
            }
            String app = "";
            if (label != null && label.contains(SIGN)) {
                String[] value = label.split(SIGN);
                if (value != null) {
                    app = value[2];
                }
            }
            Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
            K8SClientResponse appResponse = tprApplication.listApplicationByNamespace(namespace, null, appBodys, HTTPMethod.GET, cluster);
            if (!HttpStatusUtil.isSuccessStatus(appResponse.getStatus())) {
                UnversionedStatus sta = JsonUtil.jsonToPojo(appResponse.getBody(), UnversionedStatus.class);
                return ActionReturnUtil.returnErrorWithData(sta.getMessage());
            }
            BaseResourceList tpr = JsonUtil.jsonToPojo(appResponse.getBody(), BaseResourceList.class);

            //check topo if have two unbind, if have one delete
            if (tpr != null && tpr.getItems() != null && tpr.getItems().size() > 0) {
                for (BaseResource br : tpr.getItems()) {
                    if (br != null && br.getMetadata() != null && br.getMetadata().getName() != null) {
                        boolean appFlag = true;
                        List<Deployment> items = new ArrayList<>();
                        List<NamespaceLocal> namespaceData = this.namespaceLocalService.getNamespaceListByTenantId(applicationList.getTenantId());

                        if (namespaceData != null && namespaceData.size() > 0) {
                            for (NamespaceLocal oneNamespace : namespaceData) {

                                K8SURL url = new K8SURL();
                                url.setResource(Resource.DEPLOYMENT);
                                //labels
                                Map<String, Object> bodys = new HashMap<String, Object>();
                                bodys.put("labelSelector", label);
                                String n = oneNamespace.getNamespaceName();
                                url.setNamespace(n).setResource(Resource.DEPLOYMENT);
                                K8SClientResponse depRes = new K8sMachineClient().exec(url, HTTPMethod.GET, null, bodys, cluster);
                                if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus()) && depRes.getStatus() != Constant.HTTP_404) {
                                    UnversionedStatus sta = JsonUtil.jsonToPojo(depRes.getBody(), UnversionedStatus.class);
                                    return ActionReturnUtil.returnErrorWithData(sta.getMessage());
                                }
                                DeploymentList deployment = JsonUtil.jsonToPojo(depRes.getBody(), DeploymentList.class);
                                if (deployment != null) {
                                    items.addAll(deployment.getItems());
                                }
                            }
                        }

                        if (items != null && items.size() > 0) {
                            for (Deployment dev : items) {
                                if (dev != null && dev.getMetadata() != null && !StringUtils.isEmpty(dev.getMetadata().getName())) {
                                    Map<String, Object> devLabe = dev.getMetadata().getLabels();
                                    int topoSum = 0;
                                    for (String key : devLabe.keySet()) {
                                        if (key.startsWith(TOPO)) {
                                            topoSum++;
                                        }
                                    }

                                    if (topoSum > 1) {
                                        //todo unbind deployemnt,service
                                        ActionReturnUtil res = unbindApplication(app, applicationList.getTenantId(), dev.getMetadata().getName(), namespace, cluster);
                                        if (!res.isSuccess()) {
                                            appFlag = false;
                                            errorMessage.add(res.get("data").toString());
                                        }
                                        continue;
                                    } else {
                                        //delete deployment
                                        // delete config map & deploy service deployment pvc ingress
                                        ActionReturnUtil deleteDeployReturn = deploymentsService.deleteDeployment(dev.getMetadata().getName(), dev.getMetadata().getNamespace(), username, cluster);
                                        if (!deleteDeployReturn.isSuccess()) {
                                            appFlag = false;
                                            errorMessage.add(deleteDeployReturn.get("data").toString());
                                        }
                                    }
                                }
                            }
                        }
                        //get external service by label
//                        Map<String, Object> bodys = new HashMap<>();
//                        bodys.put("labelSelector", label);
//                        K8SURL urlExternal = new K8SURL();
//                        urlExternal.setNamespace(Resource.EXTERNALNAMESPACE).setResource(Resource.SERVICE);
//                        K8SClientResponse serviceRe = new K8sMachineClient().exec(urlExternal, HTTPMethod.GET, null, bodys, cluster);
//                        if (!HttpStatusUtil.isSuccessStatus(serviceRe.getStatus())
//                                && serviceRe.getStatus() != Constant.HTTP_404) {
//                            errorMessage.add("获取external service错误");
//                            appFlag = false;
//                        } else {
//                            ServiceList svclist = JsonUtil.jsonToPojo(serviceRe.getBody(), ServiceList.class);
//                            if (svclist != null && svclist.getItems() != null) {
//                                for (com.harmonycloud.k8s.bean.Service oneSvc : svclist.getItems()) {
//                                    if (oneSvc != null && oneSvc.getMetadata() != null && !StringUtils.isEmpty(oneSvc.getMetadata().getName())) {
//                                        Map<String, Object> body = new HashMap<>();
//                                        Map<String, Object> head = new HashMap<String, Object>();
//                                        String[] splitLabel = label.split("=");
//                                        Map<String, Object> newLabel = new HashMap<String, Object>();
//                                        if (splitLabel.length > 2) {
//                                            for (Map.Entry<String, Object> oneLabel : oneSvc.getMetadata().getLabels().entrySet()) {
//                                                if (!oneLabel.getKey().equals(splitLabel[0])) {
//                                                    newLabel.put(oneLabel.getKey(), oneLabel.getValue());
//                                                }
//                                            }
//                                            oneSvc.getMetadata().setLabels(newLabel);
//                                        }
//                                        body.put("metadata", oneSvc.getMetadata());
//                                        body.put("spec", oneSvc.getSpec());
//                                        body.put("kind", oneSvc.getKind());
//                                        body.put("apiVersion", oneSvc.getApiVersion());
//                                        K8SURL url = new K8SURL();
//                                        url.setNamespace(Resource.EXTERNALNAMESPACE).setResource(Resource.SERVICE).setSubpath(oneSvc.getMetadata().getName());
//                                        head.put("Content-Type", "application/json");
//                                        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.PUT, head, body, cluster);
//                                        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())
//                                                && response.getStatus() != Constant.HTTP_404) {
//                                            UnversionedStatus status = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
//                                            errorMessage.add(status.getMessage());
//                                            appFlag = false;
//                                        }
//                                    }
//                                }
//                            }
//                        }
                        // delete application
                        if (appFlag) {
                            ActionReturnUtil tprDelete = tprApplication.delApplicationByName(br.getMetadata().getName(), br.getMetadata().getNamespace(), cluster);
                            if (!tprDelete.isSuccess()) {
                                return tprDelete;
                            }
                        }
                    }

                }
            }

        }

        if (errorMessage.size() > 0) {
            throw new MarsRuntimeException(ErrorCodeMessage.APP_DELETE_FAILED);
        }
        return ActionReturnUtil.returnSuccess();

    }

    /**
     * stop application service implement
     *
     * @param applicationList appListBean application id list
     * @param username     username
     * @return ActionReturnUtil
     * @author yanli
     */
    @Override
    public ActionReturnUtil stopApplicationTemplate(ApplicationList applicationList, String username, Cluster cluster) throws Exception {
        AssertUtil.notNull(applicationList, DictEnum.APPLICATION);
        AssertUtil.notEmpty(applicationList.getIdList(), DictEnum.APPLICATION);

        List<String> errorMessage = new ArrayList<>();

        // loop every service
        for (String id : applicationList.getIdList()) {
            String namespace = "";

            if (id != null && id.contains(SIGN_EQUAL)) {
                String[] value = id.split(SIGN_EQUAL);
                if (value != null) {
                    namespace = value[1];
                }
            }
            ActionReturnUtil deploymentsRes = deploymentsService.listDeployments(applicationList.getTenantId(), null, namespace, id, null, null);
            if (!deploymentsRes.isSuccess()) {
                return deploymentsRes;
            }
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> deployments = (List<Map<String, Object>>) deploymentsRes.get("data");
            if (deployments != null && deployments.size() > 0) {
                for (Map<String, Object> oneDeployment : deployments) {
                    if (oneDeployment != null && oneDeployment.containsKey("name")) {
                        ActionReturnUtil stopDeployReturn = deploymentsService.stopDeployments(oneDeployment.get("name").toString(), oneDeployment.get("namespace").toString(), username);
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
     * @param applicationList appListBean application id list
     * @param username     username
     * @return ActionReturnUtil
     * @author yanli
     */
    @Override
    public ActionReturnUtil startApplicationTemplate(ApplicationList applicationList, String username, Cluster cluster) throws Exception {
        AssertUtil.notNull(applicationList, DictEnum.APPLICATION);
        AssertUtil.notEmpty(applicationList.getIdList(), DictEnum.APPLICATION);

        List<String> errorMessage = new ArrayList<>();

        // loop every service
        for (String id : applicationList.getIdList()) {
            String namespace = "";

            if (id != null && id.contains(SIGN_EQUAL)) {
                String[] value = id.split(SIGN_EQUAL);
                if (value != null) {
                    namespace = value[1];
                }
            }
            ActionReturnUtil deploymentsRes = deploymentsService.listDeployments(applicationList.getTenantId(), null, namespace, id, null, null);
            if (!deploymentsRes.isSuccess()) {
                return deploymentsRes;
            }
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> deployments = (List<Map<String, Object>>) deploymentsRes.get("data");
            if (deployments != null && deployments.size() > 0) {
                for (Map<String, Object> oneDeployment : deployments) {
                    if (oneDeployment != null && oneDeployment.containsKey("name")) {
                        ActionReturnUtil stopDeployReturn = deploymentsService.startDeployments(oneDeployment.get("name").toString(), oneDeployment.get("namespace").toString(), username);
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
     * get application by id service implement.
     *
     * @param clusterId clusterId
     * @return ActionReturnUtil
     * @author gurongyun
     */
    @SuppressWarnings("unchecked")
    @Override
    public ActionReturnUtil searchSumApplication(String clusterId) throws Exception {
        JSONObject json = new JSONObject();
        // number of application
        int count = 0;
        // number of application 启动中
        int starting = 0;
        // number of application 异常
        int stop = 0;
        int start = 0;

        // search application
        List<BaseResource> blist = new ArrayList<>();

        Cluster cluster = clusterService.findClusterById(clusterId);
        List<NamespaceLocal> namespaceList = namespaceLocalService.getNamespaceListByClusterId(clusterId);
        if (CollectionUtils.isEmpty(namespaceList)) {
            throw new MarsRuntimeException(ErrorCodeMessage.NAMESPACE_NOT_FOUND);
        }
        for (NamespaceLocal oneNamespace : namespaceList) {
            @SuppressWarnings("rawtypes")
            K8SClientResponse response = tprApplication.listApplicationByNamespace(oneNamespace.getNamespaceName(), null, null, HTTPMethod.GET, cluster);
            if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
                UnversionedStatus status = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
                return ActionReturnUtil.returnErrorWithData(status.getMessage());
            }
            BaseResourceList tpr = JsonUtil.jsonToPojo(response.getBody(), BaseResourceList.class);
            if (tpr != null && tpr.getItems() != null && tpr.getItems().size() > 0) {
                blist.addAll(tpr.getItems());
            }
        }
        int totalDeploymentCountInApp = 0;
        int normalDeploymentCountInApp = 0;
        if (blist != null && blist.size() > 0) {
            count = blist.size();
            for (BaseResource bs : blist) {
                Map<String, Object> appLable = new HashedMap();
                String label = "";
                for (Map.Entry<String, Object> vo : bs.getMetadata().getLabels().entrySet()) {
                    if (vo.getKey().startsWith(TOPO)) {
                        appLable.put(vo.getKey(), vo.getValue());
                        label = vo.getKey() + SIGN_EQUAL + vo.getValue();
                    }
                }

                JSONObject serviceJson = listServiceByBusinessId(label, bs.getMetadata().getNamespace(), cluster);
                if (Constant.START.equals(serviceJson.get("status").toString())) {
                    start ++;
                }
                if (Constant.STARTING.equals(serviceJson.get("status").toString())) {
                    starting ++;
                }
                if (Constant.STOP.equals(serviceJson.get("status").toString())) {
                    stop ++;
                }
                totalDeploymentCountInApp += Integer.parseInt(serviceJson.get("total").toString());
                normalDeploymentCountInApp += Integer.parseInt(serviceJson.get("start").toString());
            }
        }
        //查询集群下所有分区下所有的服务
        int allDeploymentInCluster = 0;
        List<NamespaceLocal> namespaceLocalList = namespaceLocalService.getNamespaceListByClusterId(clusterId);
        for (NamespaceLocal namespace : namespaceLocalList) {
            K8SClientResponse response = dpService.doDeploymentsByNamespace(namespace.getNamespaceName(), null, null, HTTPMethod.GET, cluster);
            if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
                DeploymentList deploymentList = JsonUtil.jsonToPojo(response.getBody(), DeploymentList.class);
                allDeploymentInCluster += deploymentList.getItems().size();
            }
        }
        json.put("deployments", allDeploymentInCluster);
        json.put("normal", start);
        json.put("abnormal", starting + stop);
        json.put("start", start);
        json.put("starting", starting);
        json.put("stop", stop);
        json.put("count", count);
        json.put("totalDeploymentCount", totalDeploymentCountInApp);
        json.put("normalDeploymentCount", normalDeploymentCountInApp);
        return ActionReturnUtil.returnSuccessWithData(json);
    }

    @Override
    public ActionReturnUtil deleteApplicationByNamespace(String namespace) throws Exception {
        AssertUtil.notBlank(namespace,  DictEnum.NAMESPACE);
        //todo
        return ActionReturnUtil.returnSuccess();

    }

    @SuppressWarnings("unchecked")
    @Override
    public ActionReturnUtil searchSum(String[] tenant) throws Exception {
        // search application
        List<BaseResource> blist = new ArrayList<>();
        List<NamespaceLocal> namespaceData = new ArrayList<>();
        for (String tenantId : tenant) {
            List<NamespaceLocal> namespaceListByTenantId = this.namespaceLocalService.getNamespaceListByTenantId(tenantId);
            if (!namespaceListByTenantId.isEmpty()) {
                namespaceData.addAll(namespaceListByTenantId);
            }
        }
        for (NamespaceLocal oneNamespace : namespaceData) {
            String namespace = oneNamespace.getNamespaceName();
            Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
            K8SClientResponse response = tprApplication.listApplicationByNamespace(namespace, null, null, HTTPMethod.GET, cluster);
            if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
                UnversionedStatus status = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
                return ActionReturnUtil.returnErrorWithData(status.getMessage());
            }
            BaseResource tpr = JsonUtil.jsonToPojo(response.getBody(), BaseResource.class);
            if (tpr.getMetadata().getName() != null) {
                if (tpr.getMetadata().getName() != null) {
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
        int starting = 0;
        int stop = 0;

        //get deployment by label
        Map<String, Object> bodys = new HashMap<>();
        bodys.put("labelSelector", label);

        K8SURL url = new K8SURL();
        url.setNamespace(namespace).setResource(Resource.DEPLOYMENT);
        K8SClientResponse depRes = new K8sMachineClient().exec(url, HTTPMethod.GET, null, bodys, cluster);
        if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus())
                && depRes.getStatus() != Constant.HTTP_404) {
            throw new MarsRuntimeException(ErrorCodeMessage.DEPLOYMENT_GET_FAILURE);
        }
        DeploymentList deplist = JsonUtil.jsonToPojo(depRes.getBody(), DeploymentList.class);
        if (deplist != null && deplist.getItems() != null) {
            total = deplist.getItems().size();
            List<Deployment> deps = deplist.getItems();
            if (deps != null && deps.size() > 0) {

                for (Deployment dep : deps) {
                    String status = getDeploymentStatus(dep);
                    if (Constant.START.equals(status)) {
                        start ++;
                    }
                    if (Constant.STARTING.equals(status)) {
                        starting ++;
                    }
                    if (Constant.STOP.equals(status)) {
                        stop ++;
                    }
                }
            }
        }

        if (starting > 0) {
            json.put("status", Constant.STARTING);
        } else if (stop == total) {
            json.put("status", Constant.STOP);
        } else {
            json.put("status", Constant.START);
        }
        json.put("stop", stop);
        json.put("starting", starting);
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
                case Constant.STARTING:
                    if (dep.getStatus().getReadyReplicas() != null && dep.getStatus().getReadyReplicas() > 0
                            && (dep.getStatus().getAvailableReplicas() == dep.getStatus().getReadyReplicas())
                            && dep.getSpec().getReplicas() != null
                            && dep.getSpec().getReplicas() == dep.getStatus().getReadyReplicas()) {

                        flag = Constant.START;
                    } else {
                        flag = status;
                    }
                    break;
                case Constant.STOPPING:
                    if (dep.getStatus().getAvailableReplicas() != null && dep.getStatus().getAvailableReplicas() > 0) {
                        flag = status;
                    } else {
                        flag = Constant.STOP;
                    }
                    break;
                default:
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
    public ActionReturnUtil deployApplicationTemplateByName(String tenantId, String name, String appName, String tag, String namespace, String userName, String pub, String projectId) throws Exception {
        //参数判空
        if (StringUtils.isEmpty(name) || StringUtils.isEmpty(tag) || StringUtils.isEmpty(namespace)) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        TenantBinding t = tenantService.getTenantByTenantid(tenantId);
        ActionReturnUtil btresponse = null;
        //根据namespace查询集群
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        //根据name和tag获取模板信息
        if ("true".equals(pub)) {
            btresponse = applicationService.getApplicationTemplate(name, tag, "", "all");
        } else {
            btresponse = applicationService.getApplicationTemplate(name, tag, cluster.getId(), projectId);
        }
        if (!btresponse.isSuccess()) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.APPLICATION_TEMPLATE_GET_FAILURE);
        }
        JSONObject json = (JSONObject) btresponse.get("data");
            /*ActionReturnUtil privatePartitionLabel = this.privatePartitionService.getPrivatePartitionLabel(tenantId, namespace);
            if(!privatePartitionLabel.isSuccess()){
				return privatePartitionLabel;
			}
			String nodeSelector = (String) privatePartitionLabel.get("data");*/
        ApplicationDeployDto appDeploy = new ApplicationDeployDto();
        appDeploy.setAppName(appName);
        appDeploy.setNamespace(namespace);
        ApplicationTemplateDto appTemplate = new ApplicationTemplateDto();
        appTemplate.setDesc(json.getString("desc"));
        appTemplate.setId(json.getInt("id"));
        appTemplate.setName(json.getString("name"));
        appTemplate.setTenant(json.getString("tenant"));
        appTemplate.setTenant(t.getTenantName());
        //应用模板list
        JSONArray stList = json.getJSONArray("servicelist");
        List<ServiceTemplateDto> servicelist = new LinkedList<ServiceTemplateDto>();
        if (stList != null && stList.size() > 0) {
            for (int i = 0; i < stList.size(); i++) {
                ServiceTemplateDto serviceTemplate = new ServiceTemplateDto();
                JSONObject js = stList.getJSONObject(i);
                serviceTemplate.setId(js.getInt("id"));
                serviceTemplate.setName(js.getString("name"));
                serviceTemplate.setTag(js.getString("tag"));
                serviceTemplate.setDesc(js.getString("details"));
                serviceTemplate.setTenant(t.getTenantName());
                serviceTemplate.setExternal(js.getInt("isExternal"));
                if (js.getInt("isExternal") == Constant.K8S_SERVICE) {
                    String dep = js.getJSONArray("deployment").getJSONObject(0).toString().replaceAll(":\"\",", ":" + null + ",").replaceAll(":\"\"", ":" + null + "");
                    DeploymentDetailDto deployment = JsonUtil.jsonToPojo(dep, DeploymentDetailDto.class);
                    deployment.setNamespace(namespace);
                    serviceTemplate.setDeploymentDetail(deployment);
                }
                if (!StringUtils.isEmpty(js.getString("ingress"))) {
                    JSONArray jsarray = js.getJSONArray("ingress");
                    List<IngressDto> ingress = new LinkedList<IngressDto>();
                    if (jsarray != null && jsarray.size() > 0) {
                        for (int j = 0; j < jsarray.size(); j++) {
                            JSONObject ingressJson = jsarray.getJSONObject(j);
                            IngressDto ing = JsonUtil.jsonToPojo(ingressJson.toString().toString().replaceAll(":\"\",", ":" + null + ",").replaceAll(":\"\"", ":" + null + ""), IngressDto.class);
                            ingress.add(ing);
                        }
                    }
                    serviceTemplate.setIngress(ingress);
                }
                servicelist.add(serviceTemplate);
            }
        } else {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.SERVICE_TEMPLATE_NOT_EXIST_IN_APPLICATION_TEMPLATE);
        }
        appTemplate.setServiceList(servicelist);
        appDeploy.setAppTemplate(appTemplate);
        appDeploy.setProjectId(projectId);
        ActionReturnUtil checkRes = checkK8SName(appDeploy, cluster, true);
        if (!checkRes.isSuccess()) {
            return checkRes;
        }
        //发布
        deployApplication(appDeploy, userName, cluster);
        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public ActionReturnUtil addAndDeployApplicationTemplate(ApplicationDeployDto appDeploy, String username) throws Exception {
        //参数判空
        if (Objects.isNull(appDeploy) || CollectionUtils.isEmpty(appDeploy.getAppTemplate().getServiceList()) || StringUtils.isEmpty(appDeploy.getNamespace())) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }

        String namespace = appDeploy.getNamespace();

        //获取集群
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);

        //检查是否有重名
        ActionReturnUtil checkRes = checkK8SName(appDeploy, cluster, false);
        if (!checkRes.isSuccess()) {
            return checkRes;
        }

        //获取k8s同namespace相关的资源
        //获取 Deployment name
        Set<String> deployments = new HashSet<>();
        List<Map<String, Object>> message = new ArrayList<>();
        for (ServiceTemplateDto service : appDeploy.getAppTemplate().getServiceList()) {
            deployments.add(service.getDeploymentDetail().getName());
            // todo retry and rollback
            List<String> pvcList = new ArrayList<>();
            // creat pvc
            for (CreateContainerDto c : service.getDeploymentDetail().getContainers()) {
                if (CollectionUtils.isNotEmpty(c.getStorage())) {
                    for (PersistentVolumeDto pvc : c.getStorage()) {
                        if (pvc.getType() != null && Constant.VOLUME_TYPE_PV.equals(pvc.getType())) {
                            if (StringUtils.isBlank(pvc.getPvcName())) {
                                continue;
                            }
                            pvc.setNamespace(namespace);
                            pvc.setServiceName(service.getDeploymentDetail().getName());
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
                message.addAll(routerService.createExternalRule(service, appDeploy.getNamespace()));
            }
            // creat config map & deploy service deployment & get node label by
            // namespace
            try {
                //todo so bad
                service.getDeploymentDetail().setNamespace(namespace);
                for (CreateContainerDto c : service.getDeploymentDetail().getContainers()) {
                    c.setImg(cluster.getHarborServer().getHarborHost() + "/" + c.getImg());
                }
                service.getDeploymentDetail().setProjectId(appDeploy.getProjectId());
                deploymentsService.createDeployment(service.getDeploymentDetail(), username, appDeploy.getAppName(), cluster);
            } catch (Exception e) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("Deployment:", service.getDeploymentDetail().getName());
                message.add(map);
            }
        }
        if (message.size() > 0) {
            ActionReturnUtil res = rollBackDeployment(deployments, namespace, username, cluster);
            if (!res.isSuccess()) {
                return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.APPLICATION_CREATE_ROLLBACK_FAILURE);
            }
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.APPLICATION_CREATE_FAILURE);
        }
        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public ActionReturnUtil checkK8SName(ApplicationDeployDto appDeploy, Cluster cluster, boolean isNeedCheckAppName) throws Exception {
        //判断是否是msf-tenant-namespace的命名方式
        String msfAppStyle = Constant.MSF + appDeploy.getNamespace();
        if (msfAppStyle.equals(appDeploy.getAppName())) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.APPLICATION_NAME_CONFLICT_MSF);
        }
        //获取k8s同namespace相关的资源
        //获取 Deployment name
        JSONObject msg = new JSONObject();
        String namespace = appDeploy.getNamespace();
        K8SClientResponse depRes = dpService.doDeploymentsByNamespace(namespace, null, null, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus())
                && depRes.getStatus() != Constant.HTTP_404) {
            UnversionedStatus k8sresbody = JsonUtil.jsonToPojo(depRes.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithData(k8sresbody.getMessage());
        }
        DeploymentList deplist = JsonUtil.jsonToPojo(depRes.getBody(), DeploymentList.class);
        List<Deployment> deps = new ArrayList<Deployment>();
        if (Objects.nonNull(deplist) && CollectionUtils.isNotEmpty(deplist.getItems())) {
            deps = deplist.getItems();
        }
        //ingress tcp
        ActionReturnUtil tcpRes = routerService.svcList(namespace);
        if (!tcpRes.isSuccess()) {
            return tcpRes;
        }
        @SuppressWarnings("unchecked")
        List<RouterSvc> tcplist = (List<RouterSvc>) tcpRes.get("data");

        for (ServiceTemplateDto std : appDeploy.getAppTemplate().getServiceList()) {
            //check service name
            if (Objects.nonNull(std.getDeploymentDetail()) && CollectionUtils.isNotEmpty(deps)) {
                std.getDeploymentDetail().setNamespace(namespace);
                for (Deployment dep : deps) {
                    if (std.getDeploymentDetail().getName().equals(dep.getMetadata().getName())) {
                        return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.DEPLOYMENT_NAME_DUPLICATE, std.getDeploymentDetail().getName());
                    }
                }
            }
            //check ingress
            if (CollectionUtils.isNotEmpty(std.getIngress())) {
                for (IngressDto ing : std.getIngress()) {
                    if (ing.getType() != null && "HTTP".equals(ing.getType())) {
                        if(routerService.checkIngressName(cluster, ing.getParsedIngressList().getName())){
                            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.HTTP_INGRESS_NAME_DUPLICATE);
                        }
                    }
                    if (ing.getType() != null && "TCP".equals(ing.getType()) && tcplist != null && tcplist.size() > 0) {
                        for (RouterSvc tcp : tcplist) {
                            if (("routersvc" + ing.getSvcRouter().getName()).equals(tcp.getName())) {
                                return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.TCP_INGRESS_NAME_DUPLICATE);
                            }
                        }
                    }
                }
            }
        }
        //list namespace by tenantid
        if (isNeedCheckAppName) {
            NamespaceLocal namespaceByName = this.namespaceLocalService.getNamespaceByName(namespace);
            List<NamespaceLocal> nsList = this.namespaceLocalService.getNamespaceListByTenantId(namespaceByName.getTenantId());

            //loop namespaces get application
            for (NamespaceLocal oneNamespace : nsList) {
                K8SClientResponse response = tprApplication.getApplicationByName(oneNamespace.getNamespaceName(), appDeploy.getAppName(), null, null, HTTPMethod.GET, cluster);
                if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
                    return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.APPLICATION_NAME_DUPLICATE);
                }
            }
        }
        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public ActionReturnUtil unbindApplication(String appName, String tenantId, String name, String namespace, Cluster cluster) throws Exception {
        String labelKey = TOPO + SIGN + tenantId + SIGN + appName;
        //更新Deployment label
        K8SURL url = new K8SURL();
        url.setNamespace(namespace).setResource(Resource.DEPLOYMENT).setName(name);
        K8SClientResponse depRes = new K8sMachineClient().exec(url, HTTPMethod.GET, null, null, cluster);
        if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus())
                && depRes.getStatus() != Constant.HTTP_404) {
            UnversionedStatus k8sresbody = JsonUtil.jsonToPojo(depRes.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithData(k8sresbody.getMessage());
        }
        Deployment dep = JsonUtil.jsonToPojo(depRes.getBody(), Deployment.class);
        if (dep != null) {
            if (dep != null && dep.getMetadata() != null && dep.getMetadata().getLabels() != null) {
                Map<String, Object> labels = new HashMap<String, Object>();
                labels = dep.getMetadata().getLabels();
                labels.remove(labelKey);
                dep.getMetadata().setLabels(labels);
                Map<String, Object> bodys = new HashMap<String, Object>();
                bodys = CollectionUtil.transBean2Map(dep);
                Map<String, Object> headers = new HashMap<String, Object>();
                headers.put("Content-type", "application/json");
                K8SClientResponse newRes = dpService.doSpecifyDeployment(namespace, name, headers, bodys, HTTPMethod.PUT, cluster);
                if (!HttpStatusUtil.isSuccessStatus(newRes.getStatus())) {
                    UnversionedStatus k8sresbody = JsonUtil.jsonToPojo(newRes.getBody(), UnversionedStatus.class);
                    return ActionReturnUtil.returnErrorWithData(k8sresbody.getMessage());
                }
            }
        }
        //更新service
        url = new K8SURL();
        url.setNamespace(namespace).setResource(Resource.SERVICE).setName(name);
        K8SClientResponse serRes = new K8sMachineClient().exec(url, HTTPMethod.GET, null, null, cluster);
        if (!HttpStatusUtil.isSuccessStatus(serRes.getStatus())
                && serRes.getStatus() != Constant.HTTP_404) {
            UnversionedStatus k8sresbody = JsonUtil.jsonToPojo(serRes.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithData(k8sresbody.getMessage());
        }
        com.harmonycloud.k8s.bean.Service svc = JsonUtil.jsonToPojo(depRes.getBody(), com.harmonycloud.k8s.bean.Service.class);
        if (svc != null) {
            if (svc != null && svc.getMetadata() != null && svc.getMetadata().getLabels() != null) {
                Map<String, Object> labels = new HashMap<String, Object>();
                labels = svc.getMetadata().getLabels();
                labels.remove(labelKey);
                svc.getMetadata().setLabels(labels);
                Map<String, Object> bodys = new HashMap<String, Object>();
                bodys = CollectionUtil.transBean2Map(svc);
                Map<String, Object> headers = new HashMap<String, Object>();
                headers.put("Content-type", "application/json");
                K8SClientResponse newRes = new K8sMachineClient().exec(url, HTTPMethod.PUT, headers, bodys, cluster);
                if (!HttpStatusUtil.isSuccessStatus(newRes.getStatus())) {
                    UnversionedStatus k8sresbody = JsonUtil.jsonToPojo(newRes.getBody(), UnversionedStatus.class);
                    return ActionReturnUtil.returnErrorWithData(k8sresbody.getMessage());
                }
            }
        }
        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public ActionReturnUtil bindApplication(String appName, String tenantId, String name, String namespace, Cluster cluster) throws Exception {
        String labelKey = TOPO + SIGN + tenantId + SIGN + appName;
        //更新Deployment label
        K8SURL url = new K8SURL();
        url.setNamespace(namespace).setResource(Resource.DEPLOYMENT).setName(name);
        K8SClientResponse depRes = new K8sMachineClient().exec(url, HTTPMethod.GET, null, null, cluster);
        if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus())
                && depRes.getStatus() != Constant.HTTP_404) {
            UnversionedStatus k8sresbody = JsonUtil.jsonToPojo(depRes.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithData(k8sresbody.getMessage());
        }
        Deployment dep = JsonUtil.jsonToPojo(depRes.getBody(), Deployment.class);
        if (dep != null) {
            if (dep != null && dep.getMetadata() != null && dep.getMetadata().getLabels() != null) {
                Map<String, Object> labels = new HashMap<String, Object>();
                labels = dep.getMetadata().getLabels();
                labels.put(labelKey, namespace);
                dep.getMetadata().setLabels(labels);
                Map<String, Object> bodys = new HashMap<String, Object>();
                bodys = CollectionUtil.transBean2Map(dep);
                Map<String, Object> headers = new HashMap<String, Object>();
                headers.put("Content-type", "application/json");
                K8SClientResponse newRes = dpService.doSpecifyDeployment(namespace, name, headers, bodys, HTTPMethod.PUT, cluster);
                if (!HttpStatusUtil.isSuccessStatus(newRes.getStatus())) {
                    UnversionedStatus k8sresbody = JsonUtil.jsonToPojo(newRes.getBody(), UnversionedStatus.class);
                    return ActionReturnUtil.returnErrorWithData(k8sresbody.getMessage());
                }
            }
        }
        //更新service
        url = new K8SURL();
        url.setNamespace(namespace).setResource(Resource.SERVICE).setName(name);
        K8SClientResponse serRes = new K8sMachineClient().exec(url, HTTPMethod.GET, null, null, cluster);
        if (!HttpStatusUtil.isSuccessStatus(serRes.getStatus())
                && serRes.getStatus() != Constant.HTTP_404) {
            UnversionedStatus k8sresbody = JsonUtil.jsonToPojo(serRes.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithData(k8sresbody.getMessage());
        }
        com.harmonycloud.k8s.bean.Service svc = JsonUtil.jsonToPojo(depRes.getBody(), com.harmonycloud.k8s.bean.Service.class);
        if (svc != null) {
            if (svc != null && svc.getMetadata() != null && svc.getMetadata().getLabels() != null) {
                Map<String, Object> labels = new HashMap<String, Object>();
                labels = svc.getMetadata().getLabels();
                labels.put(labelKey, namespace);
                svc.getMetadata().setLabels(labels);
                Map<String, Object> bodys = new HashMap<String, Object>();
                bodys = CollectionUtil.transBean2Map(svc);
                Map<String, Object> headers = new HashMap<String, Object>();
                headers.put("Content-type", "application/json");
                K8SClientResponse newRes = new K8sMachineClient().exec(url, HTTPMethod.PUT, headers, bodys, cluster);
                if (!HttpStatusUtil.isSuccessStatus(newRes.getStatus())) {
                    UnversionedStatus k8sresbody = JsonUtil.jsonToPojo(newRes.getBody(), UnversionedStatus.class);
                    return ActionReturnUtil.returnErrorWithData(k8sresbody.getMessage());
                }
            }
        }
        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public ActionReturnUtil getTopo(String id) throws Exception {

        if (StringUtils.isEmpty(id)) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }

        //String url = "http://kube-topo:8000/topo";
        //String url = "http://10.10.101.75:30988/topo";

        Map<String, Object> headers = new HashMap<>();

        Map<String, Object> params = new HashMap<>();
        params.put("topoSelector", id);

        ActionReturnUtil res = HttpClientUtil.httpGetRequest(kubeTopo, headers, params);
        if (res.isSuccess()) {
            String s = (String) res.get("data");
            if (s.contains("{")) {
                JSONObject a = JSONObject.fromObject(s);
                JSONArray as = a.getJSONArray("links");
                //获取应用下所有应用的服务
                Map<String, Object> bodys = new HashMap<>();
                bodys.put("labelSelector", id);
                String[] namespace = {};
                if (id.contains(SIGN) && id.contains(SIGN_EQUAL)) {
                    namespace = id.split(SIGN_EQUAL);
                }
                Cluster cluster = (Cluster) session.getAttribute("currentCluster");
                K8SURL url1 = new K8SURL();
                url1.setNamespace(namespace[1]).setResource(Resource.DEPLOYMENT);
                K8SClientResponse depRes = new K8sMachineClient().exec(url1, HTTPMethod.GET, null, bodys, cluster);
                if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus())
                        && depRes.getStatus() != Constant.HTTP_404) {
                    UnversionedStatus sta = JsonUtil.jsonToPojo(depRes.getBody(), UnversionedStatus.class);
                    return ActionReturnUtil.returnErrorWithData(sta.getMessage());
                }
                DeploymentList deplist = JsonUtil.jsonToPojo(depRes.getBody(), DeploymentList.class);
                if (deplist != null && deplist.getItems() != null) {
                    List<Deployment> deps = deplist.getItems();
                    if (deps != null && deps.size() > 0) {
                        for (Deployment dep : deps) {
                            ActionReturnUtil ress = routerService.listIngressByName(namespace[1], dep.getMetadata().getName());
                            if (ress.isSuccess()) {
                                JSONArray has = (JSONArray) ress.get("data");
                                if (has.isArray() && has.size() > 0) {
                                    ActionReturnUtil balanceRes = loadbalanceService.getStatsByService(dep.getMetadata().getName(), namespace[1]);
                                    if (balanceRes.isSuccess()) {
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

    /**
     * get application by tenant （应用信息）.
     *
     * @param tenantId tenant name
     * @return ActionReturnUtil
     * @author gurongyun
     */
    @Override
    public ActionReturnUtil listApplication(String tenantId) throws Exception {
        JSONObject json = new JSONObject();
        // application list
        JSONArray array = new JSONArray();
        int count = 0;
        List<NamespaceLocal> namespaces = namespaceService.listNamespaceNameByTenantid(tenantId);
        if(CollectionUtils.isEmpty(namespaces)){
            return ActionReturnUtil.returnSuccess();
        }
        for (NamespaceLocal oneNamespace : namespaces) {
            String namespace = oneNamespace.getNamespaceName();
            Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
            K8SClientResponse response = tprApplication.listApplicationByNamespace(namespace, null, null, HTTPMethod.GET, cluster);
            if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
                BaseResourceList tpr = JsonUtil.jsonToPojo(response.getBody(), BaseResourceList.class);
                if (tpr != null && tpr.getItems() != null && tpr.getItems().size() > 0) {
                    for (BaseResource b : tpr.getItems()) {
                        if (b != null && b.getMetadata() != null && b.getMetadata().getName() != null && b.getMetadata().getNamespace() != null) {
                            JSONObject js = new JSONObject();
                            //Map<String,Object> appLable = new HashedMap();
                            String label = "";
                            for (Map.Entry<String, Object> vo : b.getMetadata().getLabels().entrySet()) {
                                if (vo.getKey().startsWith(TOPO)) {
                                    //appLable.put(vo.getKey(),vo.getValue());
                                    label = vo.getKey() + "=" + vo.getValue();
                                }
                            }
                            // put application info
                            js.put("name", b.getMetadata().getName());
                            js.put("id", label);
                            js.put("namespace", b.getMetadata().getNamespace());
                            array.add(js);
                        }
                    }
                }
            }
        }
        json.put("list", array);
        json.put("count", count);
        return ActionReturnUtil.returnSuccessWithData(json);
    }

    @Override
    public ActionReturnUtil rollBackDeployment(Set<String> names, String namespace, String userName, Cluster cluster)
            throws Exception {
        if (names != null && names.size() > 0) {
            for (String dep : names) {
                ActionReturnUtil deleteDeployReturn = deploymentsService.deleteDeployment(dep, namespace, userName, cluster);
                if (!deleteDeployReturn.isSuccess()) {
                    return deleteDeployReturn;
                }
            }
        }
        return ActionReturnUtil.returnSuccess();
    }

    /**
     * 发布应用
     *
     * @param appDeploy
     * @param username
     * @param cluster
     * @throws Exception
     */
    private synchronized void deployApplication(ApplicationDeployDto appDeploy, String username, Cluster cluster) throws Exception {
        String topoLabel = TOPO + SIGN + appDeploy.getProjectId() + SIGN + appDeploy.getAppName();
        String namespaceLabel = appDeploy.getNamespace();

        BaseResource base = new BaseResource();
        ObjectMeta mate = new ObjectMeta();
        mate.setNamespace(appDeploy.getNamespace());
        mate.setName(appDeploy.getAppName());

        Map<String, Object> anno = new HashMap<String, Object>();
        anno.put("nephele/annotation",appDeploy.getAppTemplate().getDesc() != null ? appDeploy.getAppTemplate().getDesc() : "");
        mate.setAnnotations(anno);

        Map<String, Object> appLabels = new HashMap<String, Object>();
        appLabels.put(topoLabel, namespaceLabel);
        appLabels.put(CREATE, username);
        appLabels.put(Constant.NODESELECTOR_LABELS_PRE + Constant.LABEL_PROJECT_ID, appDeploy.getProjectId());
        mate.setLabels(appLabels);

        base.setMetadata(mate);

        Set<String> deployments = new HashSet<>();

        List<Map<String, Object>> message = new ArrayList<>();
        // loop appTemplate
        if (appDeploy.getAppTemplate() != null && appDeploy.getAppTemplate().getServiceList().size() > 0) {
            for (ServiceTemplateDto svcTemplate : appDeploy.getAppTemplate().getServiceList()) {
                // creat pvc
                for (CreateContainerDto c : svcTemplate.getDeploymentDetail().getContainers()) {
                    if (c.getStorage() != null) {
                        for (PersistentVolumeDto pvc : c.getStorage()) {
                            if (pvc == null) {
                                continue;
                            }
                            if (pvc.getType() != null && Constant.VOLUME_TYPE_PV.equals(pvc.getType())) {
                                pvc.setNamespace(appDeploy.getNamespace());
                                pvc.setServiceName(svcTemplate.getDeploymentDetail().getName());
                                pvc.setVolumeName(pvc.getPvcName());
                                ActionReturnUtil pvcres = volumeSerivce.createVolume(pvc);
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
                    message.addAll(routerService.createExternalRule(svcTemplate, appDeploy.getNamespace()));
                }
                // creat config map & deploy service deployment & get node label by
                // namespace
                svcTemplate.getDeploymentDetail().setNamespace(appDeploy.getNamespace());
                for (CreateContainerDto c : svcTemplate.getDeploymentDetail().getContainers()) {
                    c.setImg(cluster.getHarborServer().getHarborHost() + "/" + c.getImg());
                }
                svcTemplate.getDeploymentDetail().setProjectId(appDeploy.getProjectId());
                ActionReturnUtil depRes = deploymentsService.createDeployment(svcTemplate.getDeploymentDetail(), username, appDeploy.getAppName(),
                        cluster);
                deployments.add(svcTemplate.getDeploymentDetail().getName());
                if (!depRes.isSuccess()) {
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put(svcTemplate.getName(), depRes.get("data"));
                    message.add(map);
                }
            }
        }
        // update application template isdeploy
        if (Objects.nonNull(appDeploy.getAppTemplate().getId())) {
            appTemService.updateDeployById(appDeploy.getAppTemplate().getId());
        }

        if (message.size() > 0) {
            ActionReturnUtil res = rollBackDeployment(deployments, appDeploy.getNamespace(), username, cluster);
            if (!res.isSuccess()) {
                throw new MarsRuntimeException(ErrorCodeMessage.APPLICATION_CREATE_ROLLBACK_FAILURE);
            }
        }
        ActionReturnUtil result = tprApplication.createApplication(base, cluster);

        if (!result.isSuccess()) {
            rollBackDeployment(deployments, appDeploy.getNamespace(), username, cluster);
            throw new MarsRuntimeException(ErrorCodeMessage.APPLICATION_CREATE_ROLLBACK_FAILURE);
        }
    }

    @Override
    public List<BaseResource> listApplicationByProject(String projectId) throws Exception {
        Project project = this.projectService.getProjectByProjectId(projectId);
        String tenantId = project.getTenantId();
        List<NamespaceLocal> namespaceList = namespaceLocalService.getNamespaceListByTenantId(tenantId);
        List<BaseResource> resList = new ArrayList<>();
        Map<String, Object> bodys = new HashMap<>();
        String projectLabel = Constant.NODESELECTOR_LABELS_PRE + Constant.LABEL_PROJECT_ID + "=" + projectId;
        bodys.put("labelSelector", projectLabel);
        if (CollectionUtils.isNotEmpty(namespaceList)) {
            for (NamespaceLocal namespaceLocal : namespaceList) {
                try {
                    resList.addAll(getApplicationList(namespaceLocal.getNamespaceName(), bodys));
                }catch (Exception e){
                    LOGGER.error("查询应用列表失败，namespace：{}",namespaceLocal.getNamespaceName(),e);
                }
            }
        }
        return resList;
    }

    public void deleteProjectAppResource(String projectId) throws Exception {
        Project project = projectService.getProjectByProjectId(projectId);
        if (Objects.isNull(project)){
            throw new MarsRuntimeException(ErrorCodeMessage.PROJECT_NOT_EXIST);
        }
        String tenantId = project.getTenantId();
        List<NamespaceLocal> namespaceList = namespaceLocalService.getAllNamespaceListByTenantId(tenantId);
        if (CollectionUtils.isNotEmpty(namespaceList)) {
            Map<String, Object> bodys = new HashMap<>();
            String projectLabel = Constant.NODESELECTOR_LABELS_PRE + Constant.LABEL_PROJECT_ID + "=" + projectId;
            bodys.put("labelSelector", projectLabel);
            ApplicationList appList = new ApplicationList();
            List<String> idList = new ArrayList<>();
            for (NamespaceLocal namespaceLocal : namespaceList) {
                List<BaseResource> appCrdList = getApplicationList(namespaceLocal.getNamespaceName(), bodys);
                //循环应用获取id
                if (appCrdList != null) {
                    for (BaseResource bs : appCrdList) {
                        String label = "";
                        for (Map.Entry<String, Object> vo : bs.getMetadata().getLabels().entrySet()) {
                            if (vo.getKey().startsWith(TOPO)) {
                                label = vo.getKey() + "=" + vo.getValue();
                                idList.add(label);
                                break;
                            }
                        }
                    }
                }
            }
            appList.setIdList(idList);
            appList.setTenantId(tenantId);
            if (!org.springframework.util.CollectionUtils.isEmpty(appList.getIdList())){
                this.deleteApplicationTemplate(appList, null);
            }
        }
    }

    @Override
    public ActionReturnUtil getApplicationListInNamespace(String namespace) throws Exception {
        //判断用户权限
        boolean isPrivilege = userService.checkCurrentUserIsAdminOrTm();
        Map<String, Object> msfBody = new HashMap<>();
        msfBody.put("labelSelector", Constant.NODESELECTOR_LABELS_PRE + "springcloud=true");
        List<BaseResource> list = getApplicationList(namespace, null);
        Map<String, Object> appListMap = new HashMap<>();
        if (list != null && list.size() > 0) {
            List<ApplicationDto> appList = convertAppListData(list, null);
            //分组
            Map<Boolean, List<ApplicationDto>> result = appList.stream()
                    .collect(Collectors.groupingBy(ApplicationDto :: isMsf, Collectors.toList()));
            if (Objects.nonNull(result)) {
                appListMap.put("app", result.get(false));
                //获取微服务应用
                if (isPrivilege) {
                    appListMap.put("msf", result.get(true));
                }
            }
        }
        return ActionReturnUtil.returnSuccessWithData(appListMap);
    }

    @Override
    public ActionReturnUtil checkAppNamespaceResource(String namespace, String appTemplateName, String projectId) throws Exception {
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        //获取应用模板内所需要的cpu和内存
        Map<String, Long> appResource = applicationService.getAppTemplateResource(appTemplateName, null, cluster.getId(), projectId);
        //获取分区内剩下的资源
        Map<String, String> remainResource = namespaceService.getNamespaceResourceRemainQuota(namespace);
        return namespaceService.checkResourceInTemplateDeploy(appResource, remainResource);
    }
}