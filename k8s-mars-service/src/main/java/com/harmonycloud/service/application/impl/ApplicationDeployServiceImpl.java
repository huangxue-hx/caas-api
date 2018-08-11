package com.harmonycloud.service.application.impl;

import com.harmonycloud.common.Constant.CommonConstant;
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
import com.harmonycloud.service.platform.convert.K8sResultConvert;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.harmonycloud.common.Constant.CommonConstant.DEFAULT_HTTPS_PORT;


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
            //根据项目获取应用列表
            this.getAllAppList(appCrdList,bodys);
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
    private void getAllAppList(List<BaseResource> appCrdList,Map<String, Object> bodys)throws Exception{
        final List<Cluster> clusterList = this.roleLocalService.listCurrentUserRoleCluster();
        CountDownLatch countDownLatchApp = new CountDownLatch(clusterList.size());
        for (Cluster cluster : clusterList) {
            ThreadPoolExecutorFactory.executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        List<BaseResource> list = getApplicationList(null, bodys,cluster);
                        if (list != null && list.size() > 0) {
                            appCrdList.addAll(list);
                        }
                    } catch (Exception e) {
                        LOGGER.error("获取应用错误", e);
                    } finally {
                        countDownLatchApp.countDown();
                    }
                }
            });
        }
        countDownLatchApp.await();
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
        LOGGER.error("getApplicationList error, namespace:{},response:{}", namespace,
                com.alibaba.fastjson.JSONObject.toJSONString(response));
        return Collections.emptyList();
    }
    private List<BaseResource> getApplicationList(String namespace, Map<String, Object> bodys,Cluster cluster) throws Exception {
        K8SClientResponse response = tprApplication.listApplicationByNamespace(namespace, null, bodys, HTTPMethod.GET, cluster);
        if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            BaseResourceList tpr = JsonUtil.jsonToPojo(response.getBody(), BaseResourceList.class);
            if (tpr != null) {
                return tpr.getItems();
            }
        }
        LOGGER.error("getApplicationList error, namespace:{},response:{}", namespace,
                com.alibaba.fastjson.JSONObject.toJSONString(response));
        return Collections.emptyList();
    }
    private List<ApplicationDto> convertAppListData(List<BaseResource> appCrdList, String status) throws Exception {
        List<ApplicationDto> array = new ArrayList<>();
        //获取可用集群
        final List<Cluster> clusterList = this.roleLocalService.listCurrentUserRoleCluster();
        if (CollectionUtils.isEmpty(clusterList)){
            return array;
        }
        //转换为clusterMap
        Map<String, Cluster> clusterMap = clusterList.stream().collect(Collectors.toMap(Cluster::getId,cluster -> cluster));
        List<Deployment> deployments = new ArrayList<>();
        //开启线程获取项目集群服务列表
        CountDownLatch countDownLatchApp = new CountDownLatch(clusterList.size());
        for (Cluster cluster : clusterList) {
            ThreadPoolExecutorFactory.executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        K8SClientResponse responseDep = dpService.doDeploymentsByNamespace(null, null, null, HTTPMethod.GET, cluster);
                        if (HttpStatusUtil.isSuccessStatus(responseDep.getStatus())) {
                            DeploymentList deploymentList = JsonUtil.jsonToPojo(responseDep.getBody(), DeploymentList.class);
                            if (deploymentList != null && !CollectionUtils.isEmpty(deploymentList.getItems())) {
                                deployments.addAll(deploymentList.getItems());
                            }
                        }
                    } catch (Exception e) {
                        LOGGER.error("获取应用错误", e);
                    } finally {
                        countDownLatchApp.countDown();
                    }
                }
            });
        }
        countDownLatchApp.await();
//        for (Cluster cluster : clusterList) {
//            try {
//                K8SClientResponse responseDep = dpService.doDeploymentsByNamespace(null, null, null, HTTPMethod.GET, cluster);
//                if (HttpStatusUtil.isSuccessStatus(responseDep.getStatus())) {
//                    DeploymentList deploymentList = JsonUtil.jsonToPojo(responseDep.getBody(), DeploymentList.class);
//                    if (deploymentList != null && !CollectionUtils.isEmpty(deploymentList.getItems())) {
//                        deployments.addAll(deploymentList.getItems());
//                    }
//                }
//            } catch (Exception e) {
//                LOGGER.error("获取应用数量错误", e);
//            }
//        }
        //转化为前端显示数据
        Map<String, List<Deployment>> deploymentMap = this.groupByAppLabel(deployments);
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
            final NamespaceLocal namespaceLocal = namespaceLocalService.getNamespaceByName(app.getNamespace());
            app.setAliasNamespace(namespaceLocal.getAliasName());
            //获取创建时间
            app.setCreateTime(bs.getMetadata().getCreationTimestamp());
            app.setUser(user);
            app.setRealName(userService.getUser(user).getRealName());
            Cluster cluster = clusterMap.get(namespaceLocal.getClusterId());
//            JSONObject serviceJson = listServiceByApplicationId(label, bs.getMetadata().getNamespace(), cluster);
            JSONObject serviceJson = this.getServiceStatus(deploymentMap.get(label));
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
    private String getDeploymentTime(List<Deployment> list) throws Exception {
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
        ApplicationDetailDto applicationDetailDto = new ApplicationDetailDto();
        if(StringUtils.isBlank(namespace)){
            if(StringUtils.isBlank(id) || !id.contains(SIGN) || !id.contains(SIGN_EQUAL)){
                return ActionReturnUtil.returnErrorWithData(DictEnum.NAMESPACE.phrase(),ErrorCodeMessage.NOT_BLANK);
            }
            String[] namespaces = id.split(SIGN_EQUAL);
            namespace = namespaces[1];
        }
        String namespaceAliasName = namespaceLocalService.getNamespaceByName(namespace).getAliasName();
        BaseResource tpr = new BaseResource();
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        K8SClientResponse response = tprApplication.getApplicationByName(namespace, appName, null, null, HTTPMethod.GET, cluster);
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

            // put application info
            applicationDetailDto.setClusterId(cluster.getId());
            applicationDetailDto.setMsf(isMsf);
            applicationDetailDto.setOperationable(isOperationable);
            applicationDetailDto.setName(tpr.getMetadata().getName());
            applicationDetailDto.setCreateTime(tpr.getMetadata().getCreationTimestamp());
            String anno = "";
            if (tpr.getMetadata().getAnnotations() != null && tpr.getMetadata().getAnnotations().containsKey("nephele/annotation") && tpr.getMetadata().getAnnotations().get("nephele/annotation") != null) {
                anno = tpr.getMetadata().getAnnotations().get("nephele/annotation").toString();
            }
            applicationDetailDto.setDesc(anno);
            applicationDetailDto.setNamespace(tpr.getMetadata().getNamespace());
            applicationDetailDto.setUser(String.valueOf(tpr.getMetadata().getLabels().get("creater")));
            if(StringUtils.isBlank(id)) {
                for (Map.Entry<String, Object> vo : tpr.getMetadata().getLabels().entrySet()) {
                    if (vo.getKey().startsWith(TOPO)) {
                        id = vo.getKey() + SIGN_EQUAL + vo.getValue();
                        break;
                    }
                }
            }
            applicationDetailDto.setId(id);
            applicationDetailDto.setRealName(userService.getUser(applicationDetailDto.getUser()).getRealName());
            applicationDetailDto.setAliasNamespace(namespaceAliasName);

            List<ServiceDetailInApplicationDto> svcArray = new ArrayList<>();
            Map<String, Object> bodys = new HashMap<>();
            bodys.put("labelSelector", id);
            K8SURL url = new K8SURL();
            url.setNamespace(namespace).setResource(Resource.DEPLOYMENT);
            K8SClientResponse depRes = new K8sMachineClient().exec(url, HTTPMethod.GET, null, bodys, cluster);
            if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus())
                    && depRes.getStatus() != Constant.HTTP_404) {
                UnversionedStatus sta = JsonUtil.jsonToPojo(depRes.getBody(), UnversionedStatus.class);
                return ActionReturnUtil.returnErrorWithData(sta.getMessage());
            }
            DeploymentList deplist = JsonUtil.jsonToPojo(depRes.getBody(), DeploymentList.class);

            if (deplist != null && deplist.getItems() != null) {
                List<Deployment> deps = deplist.getItems();
                //获取最新更新时间
                String updateTime = getDeploymentTime(deps);
                if (updateTime == null) {
                    updateTime = tpr.getMetadata().getCreationTimestamp();
                }
                applicationDetailDto.setUpdateTime(updateTime);
                if (deps != null && deps.size() > 0) {
                    ApplicationDto applicationDto = new ApplicationDto();
                    for (Deployment dep : deps) {
                        ServiceDetailInApplicationDto serviceDetail = new ServiceDetailInApplicationDto();
                        Map<String, String> labelMap = new HashMap<>();

                        serviceDetail.setIsExternal(CommonConstant.ZERONUM);
                        applicationDto.setName(dep.getMetadata().getName());
                        if (privilegeHelper.isFiltered(applicationDto)) {
                            continue;
                        }
                        serviceDetail.setName(dep.getMetadata().getName());
                        String labels = null;
                        if (dep.getMetadata().getAnnotations() != null && dep.getMetadata().getAnnotations().containsKey("nephele/labels")) {
                            labels = dep.getMetadata().getAnnotations().get("nephele/labels").toString();
                        }
                        if (!StringUtils.isEmpty(labels)) {
                            String[] arrLabel = labels.split(",");
                            for (String l : arrLabel) {
                                String[] tmp = l.split("=");
                                labelMap.put(tmp[0], tmp[1]);
                            }
                            serviceDetail.setLabels(labelMap);
                        }
                        // get status
                        // deploment status
                        serviceDetail.setStatus(K8sResultConvert.getDeploymentStatus(dep));
                        // get version
                        if (dep.getMetadata().getAnnotations() != null && dep.getMetadata().getAnnotations().containsKey("deployment.kubernetes.io/revision")) {
                            serviceDetail.setVersion("v" + dep.getMetadata().getAnnotations().get("deployment.kubernetes.io/revision"));
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
                        serviceDetail.setPV(isPV);
                        serviceDetail.setImg(img);
                        serviceDetail.setCpu(cpu);
                        serviceDetail.setMemory(memory);
                        serviceDetail.setInstance(dep.getSpec().getReplicas());
                        serviceDetail.setCreateTime(dep.getMetadata().getCreationTimestamp());
                        serviceDetail.setNamespace(dep.getMetadata().getNamespace());
                        serviceDetail.setAliasNamespace(namespaceAliasName);
                        serviceDetail.setSelector(dep.getSpec().getSelector());
                        svcArray.add(serviceDetail);
                    }
                }
            }

            applicationDetailDto.setServiceList(svcArray);
        } else {
            return ActionReturnUtil.returnSuccessWithData(null);
        }
        return ActionReturnUtil.returnSuccessWithData(applicationDetailDto);
    }

    /**
     * deployment application service implement.
     *
     * @param appDeploy appDeployBean
     * @param username  username
     * @return ActionReturnUtil
     * @author yanli
     */
    @Override
    public synchronized ActionReturnUtil deployApplicationTemplate(ApplicationDeployDto appDeploy, String username) throws Exception {
        //参数判空
        if (Objects.isNull(appDeploy) || StringUtils.isBlank(appDeploy.getAppName()) || StringUtils.isBlank(appDeploy.getProjectId())
                || StringUtils.isBlank(appDeploy.getNamespace()) || Objects.isNull(appDeploy.getAppTemplate())) {
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
     * @param username        username
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
                        //labels
                        Map<String, Object> bodys = new HashMap<String, Object>();
                        bodys.put("labelSelector", label);
                        K8SClientResponse depRes = dpService.doDeploymentsByNamespace(namespace, null, bodys, HTTPMethod.GET, cluster);
                        if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus()) && depRes.getStatus() != Constant.HTTP_404) {
                            UnversionedStatus sta = JsonUtil.jsonToPojo(depRes.getBody(), UnversionedStatus.class);
                            return ActionReturnUtil.returnErrorWithData(sta.getMessage());
                        }
                        DeploymentList deployment = JsonUtil.jsonToPojo(depRes.getBody(), DeploymentList.class);
                        if (deployment != null) {
                            items.addAll(deployment.getItems());
                        }
                        if (items != null && items.size() > 0) {
                            for (Deployment dev : items) {
                                if (dev != null && dev.getMetadata() != null && !StringUtils.isEmpty(dev.getMetadata().getName())) {
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
     * @param username        username
     * @return ActionReturnUtil
     * @author yanli
     */
    @Override
    public ActionReturnUtil stopApplication(ApplicationList applicationList, String username) throws Exception {
        AssertUtil.notNull(applicationList, DictEnum.APPLICATION);
        AssertUtil.notEmpty(applicationList.getIdList(), DictEnum.APPLICATION);
        List<String> errorMessage = new ArrayList<>();

        //循环应用
        for (String id : applicationList.getIdList()) {
            String namespace = "";
            if (id != null && id.contains(SIGN_EQUAL)) {
                String[] value = id.split(SIGN_EQUAL);
                namespace = value != null ? value[CommonConstant.NUM_ONE] : "";
            }
            Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
            ActionReturnUtil deploymentsRes = deploymentsService.listDeployments(applicationList.getTenantId(), null, namespace, id, applicationList.getProjectId(), cluster.getId());
            if (!deploymentsRes.isSuccess()) {
                return deploymentsRes;
            }
            List<Map<String, Object>> deployments = (List<Map<String, Object>>) deploymentsRes.get("data");
            if (deployments != null && deployments.size() > 0) {
                for (Map<String, Object> oneDeployment : deployments) {
                    if (oneDeployment != null && oneDeployment.containsKey("name")) {
                        String depName = oneDeployment.get("name").toString();
                        //判断该服务是否处在蓝绿或灰度升级状态,即查看paused值
                        K8SClientResponse depRes = dpService.doSpecifyDeployment(namespace, depName, null, null,
                                HTTPMethod.GET, cluster);
                        if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus())) {
                            UnversionedStatus status = JsonUtil.jsonToPojo(depRes.getBody(), UnversionedStatus.class);
                            throw new MarsRuntimeException(status.getMessage());
                        }
                        Deployment dep = JsonUtil.jsonToPojo(depRes.getBody(), Deployment.class);
                        if (dep.getSpec().isPaused()) {
                            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.APPLICATION_CAN_NOT_STOP);
                        }
                        ActionReturnUtil stopDeployReturn = deploymentsService.stopDeployments(depName, oneDeployment.get("namespace").toString(), username);
                        if (!stopDeployReturn.isSuccess()) {
                            errorMessage.add(stopDeployReturn.toString());
                        }
                    }
                }
            }
        }
        return (errorMessage.size() > 0) ? ActionReturnUtil.returnErrorWithData(errorMessage) : ActionReturnUtil.returnSuccess();
    }

    /**
     * start application service on 17/04/11.
     *
     * @param applicationList appListBean application id list
     * @param username        username
     * @return ActionReturnUtil
     * @author yanli
     */
    @Override
    public ActionReturnUtil startApplication(ApplicationList applicationList, String username) throws Exception {
        AssertUtil.notNull(applicationList, DictEnum.APPLICATION);
        AssertUtil.notEmpty(applicationList.getIdList(), DictEnum.APPLICATION);
        List<String> errorMessage = new ArrayList<>();

        //循环应用
        for (String id : applicationList.getIdList()) {
            String namespace = "";
            if (id != null && id.contains(SIGN_EQUAL)) {
                String[] value = id.split(SIGN_EQUAL);
                namespace = value != null ? value[CommonConstant.NUM_ONE] : "";
            }
            Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
            ActionReturnUtil deploymentsRes = deploymentsService.listDeployments(applicationList.getTenantId(), null, namespace, id, applicationList.getProjectId(), cluster.getId());
            if (!deploymentsRes.isSuccess()) {
                return deploymentsRes;
            }
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
        return (errorMessage.size() > 0) ? ActionReturnUtil.returnErrorWithData(errorMessage) : ActionReturnUtil.returnSuccess();
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
        // number of application 启动中
        AtomicInteger starting = new AtomicInteger(0);
        // number of application 异常
        AtomicInteger stop = new AtomicInteger(0);
        AtomicInteger start = new AtomicInteger(0);

        //查询集群下所有分区下所有的服务
        AtomicInteger allDeploymentInCluster = new AtomicInteger(0);

        // search application
        List<BaseResource> blist = new ArrayList<>();
        List<Deployment> deployments = new ArrayList<>();
        List<String> namespaces = new ArrayList<>();
        Cluster cluster = clusterService.findClusterById(clusterId);
        List<NamespaceLocal> namespaceList = namespaceLocalService.getNamespaceListByClusterId(clusterId);
        if (CollectionUtils.isEmpty(namespaceList)) {
            throw new MarsRuntimeException(ErrorCodeMessage.NAMESPACE_NOT_FOUND);
        }
        namespaceList.stream().forEach(namespaceLocal -> namespaces.add(namespaceLocal.getNamespaceName()));
        CountDownLatch countDownLatchApp = new CountDownLatch(CommonConstant.NUM_TWO);
        ThreadPoolExecutorFactory.executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    K8SClientResponse response = tprApplication.listApplicationByNamespace(null, null, null, HTTPMethod.GET, cluster);
                    if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
                        throw new Exception(response.getBody());
                    }
                    BaseResourceList tpr = JsonUtil.jsonToPojo(response.getBody(), BaseResourceList.class);
                    if (tpr != null && CollectionUtils.isNotEmpty(tpr.getItems())) {
                        blist.addAll(tpr.getItems());
                    }
                } catch (Exception e) {
                    LOGGER.error("获取应用数量错误", e);
                } finally {
                    countDownLatchApp.countDown();
                }
            }
        });
        ThreadPoolExecutorFactory.executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    K8SClientResponse responseDep = dpService.doDeploymentsByNamespace(null, null, null, HTTPMethod.GET, cluster);
                    if (HttpStatusUtil.isSuccessStatus(responseDep.getStatus())) {
                        DeploymentList deploymentList = JsonUtil.jsonToPojo(responseDep.getBody(), DeploymentList.class);
                        if (deploymentList != null && !CollectionUtils.isEmpty(deploymentList.getItems())) {
                            allDeploymentInCluster.addAndGet(deploymentList.getItems().size());
                            deployments.addAll(deploymentList.getItems());
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("获取服务数量错误", e);
                } finally {
                    countDownLatchApp.countDown();
                }
            }
        });
        countDownLatchApp.await();
        AtomicInteger totalDeploymentCountInApp = new AtomicInteger(0);
        AtomicInteger normalDeploymentCountInApp = new AtomicInteger(0);
        //将服务列表根据应用的label标签进行分组,key为某个应用的标签，value为该应用下的服务列表
        Map<String, List<Deployment>> deploymentMap = this.groupByAppLabel(deployments);
        if (blist != null && blist.size() > 0) {
            Iterator<BaseResource> iterator = blist.iterator();
            while (iterator.hasNext()) {
                BaseResource bs = iterator.next();
                if (!namespaces.contains(bs.getMetadata().getNamespace())) {
                    iterator.remove();
                    continue;
                }
                Map<String, Object> appLable = new HashedMap();
                String label = "";
                for (Map.Entry<String, Object> vo : bs.getMetadata().getLabels().entrySet()) {
                    if (vo.getKey().startsWith(TOPO)) {
                        appLable.put(vo.getKey(), vo.getValue());
                        label = vo.getKey() + SIGN_EQUAL + vo.getValue();
                    }
                }
                JSONObject serviceJson = this.getServiceStatus(deploymentMap.get(label));
                if (Constant.START.equals(serviceJson.get("status").toString())) {
                    start.incrementAndGet();
                }
                if (Constant.STARTING.equals(serviceJson.get("status").toString())) {
                    starting.incrementAndGet();
                }
                if (Constant.STOP.equals(serviceJson.get("status").toString())) {
                    stop.incrementAndGet();
                }
                totalDeploymentCountInApp.addAndGet(Integer.parseInt(serviceJson.get("total").toString()));
                normalDeploymentCountInApp.addAndGet(Integer.parseInt(serviceJson.get("start").toString()));
            }
        }
        json.put("deployments", allDeploymentInCluster);
        json.put("normal", start);
        json.put("abnormal", starting.intValue() + stop.intValue());
        json.put("start", start);
        json.put("starting", starting);
        json.put("stop", stop);
        json.put("count", blist.size());
        json.put("totalDeploymentCount", totalDeploymentCountInApp);
        json.put("normalDeploymentCount", normalDeploymentCountInApp);
        return ActionReturnUtil.returnSuccessWithData(json);
    }

    @Override
    public ActionReturnUtil deleteApplicationByNamespace(String namespace) throws Exception {
        AssertUtil.notBlank(namespace, DictEnum.NAMESPACE);
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

    /**
     * 将服务列表根据应用的label标签进行分区，标签名称以topo开头的
     *
     * @param deployments
     * @return
     */
    private Map<String,List<Deployment>> groupByAppLabel(List<Deployment> deployments){
        Map<String,List<Deployment>> deploymentMap = new HashMap<>();
        if (CollectionUtils.isEmpty(deployments)){
            return deploymentMap;
        }
        for(Deployment deployment : deployments){
            if (Objects.isNull(deployment) || Objects.isNull(deployment.getMetadata())){
                continue;
            }
            Map<String, Object> lables = deployment.getMetadata().getLabels();
            if (lables == null || lables.size() == 0) {
                continue;
            }
            for (String labelKey : lables.keySet()) {
                String label = labelKey + SIGN_EQUAL + lables.get(labelKey).toString();
                if (labelKey.startsWith(TOPO)) {
                    if (deploymentMap.get(label) == null) {
                        List<Deployment> deploys = new ArrayList<>();
                        deploys.add(deployment);
                        deploymentMap.put(label, deploys);
                    } else {
                        deploymentMap.get(label).add(deployment);
                    }
                }
            }
        }
        return deploymentMap;
    }

    private JSONObject listServiceByApplicationId(String label, String namespace, Cluster cluster) throws Exception {
        JSONObject json = new JSONObject();
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
        if (deplist == null || CollectionUtils.isEmpty(deplist.getItems())) {
            json.put("stop", 0);
            json.put("starting", 0);
            json.put("start", 0);
            json.put("total", 0);
            json.put("status", Constant.STOP);
            return json;
        }
        return this.getServiceStatus(deplist.getItems());

    }

    private JSONObject getServiceStatus(List<Deployment> deps) throws Exception {
        JSONObject json = new JSONObject();
        // number of application running
        int start = 0;
        // number of deploment
        int total = 0;
        int starting = 0;
        int stop = 0;
        if (deps != null && deps.size() > 0) {
            total = deps.size();
            for (Deployment dep : deps) {
                String status = K8sResultConvert.getDeploymentStatus(dep);
                if (Constant.SERVICE_START.equals(status)) {
                    start++;
                }
                if (Constant.SERVICE_STARTING.equals(status)) {
                    starting++;
                }
                if (Constant.SERVICE_STOP.equals(status)) {
                    stop++;
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
                            pvc.setProjectId(appDeploy.getProjectId());
                            if (StringUtils.isBlank(pvc.getVolumeName())) {
                                pvc.setVolumeName(pvc.getPvcName());
                            }
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
                    c.setImg(cluster.getHarborServer().getHarborAddress() + "/" + c.getImg());
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
                        if (routerService.checkIngressName(cluster, ing.getParsedIngressList().getName())) {
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
        if (CollectionUtils.isEmpty(namespaces)) {
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
                                pvc.setProjectId(appDeploy.getProjectId());
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
                    c.setImg(cluster.getHarborServer().getHarborAddress() + "/" + c.getImg());
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
        BaseResource base = new BaseResource();
        ObjectMeta mate = new ObjectMeta();
        mate.setNamespace(appDeploy.getNamespace());
        mate.setName(appDeploy.getAppName());

        Map<String, Object> anno = new HashMap<String, Object>();
        anno.put("nephele/annotation", appDeploy.getAppTemplate().getDesc() != null ? appDeploy.getAppTemplate().getDesc() : "");
        mate.setAnnotations(anno);

        Map<String, Object> appLabels = new HashMap<String, Object>();
        appLabels.put(topoLabel, namespaceLabel);
        appLabels.put(CREATE, username);
        appLabels.put(Constant.NODESELECTOR_LABELS_PRE + Constant.LABEL_PROJECT_ID, appDeploy.getProjectId());
        mate.setLabels(appLabels);

        base.setMetadata(mate);
        ActionReturnUtil result = tprApplication.createApplication(base, cluster);

        if (!result.isSuccess()) {
            rollBackDeployment(deployments, appDeploy.getNamespace(), username, cluster);
            throw new MarsRuntimeException(ErrorCodeMessage.APPLICATION_CREATE_ROLLBACK_FAILURE);
        }
    }

    @Override
    public List<BaseResource> listApplicationByProject(String projectId) throws Exception {
        List<BaseResource> resList = new ArrayList<>();
        Map<String, Object> bodys = new HashMap<>();
        String projectLabel = Constant.NODESELECTOR_LABELS_PRE + Constant.LABEL_PROJECT_ID + "=" + projectId;
        bodys.put("labelSelector", projectLabel);
        this.getAllAppList(resList,bodys);
        return resList;
    }

    public void deleteProjectAppResource(String projectId) throws Exception {
        Project project = projectService.getProjectByProjectId(projectId);
        if (Objects.isNull(project)) {
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
            if (!org.springframework.util.CollectionUtils.isEmpty(appList.getIdList())) {
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
                    .collect(Collectors.groupingBy(ApplicationDto::isMsf, Collectors.toList()));
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

    @Override
    public ActionReturnUtil updateApplication(String appName, String namespace, String desc) throws Exception {
        AssertUtil.notBlank(namespace, DictEnum.PARAM);
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        K8SClientResponse response = tprApplication.getApplicationByName(namespace, appName, null, null, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            throw new MarsRuntimeException(response.getBody());
        }
        BaseResource appCrd = JsonUtil.jsonToPojo(response.getBody(), BaseResource.class);
        Map<String, Object> annotation = appCrd.getMetadata().getAnnotations();
        annotation.put("nephele/annotation", desc);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        String updateTime = sdf.format(new Date());
        annotation.put("updateTimestamp", updateTime);
        appCrd.getMetadata().setAnnotations(annotation);
        return tprApplication.updateApplication(namespace, appName, appCrd, cluster);
    }
}