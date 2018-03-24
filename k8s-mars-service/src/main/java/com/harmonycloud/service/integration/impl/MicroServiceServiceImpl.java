package com.harmonycloud.service.integration.impl;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.MicroServiceCodeMessage;
import com.harmonycloud.common.util.*;
import com.harmonycloud.common.util.date.DateStyle;
import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.dao.application.ServiceTemplatesMapper;
import com.harmonycloud.dao.application.bean.ApplicationService;
import com.harmonycloud.dao.application.bean.ApplicationTemplates;
import com.harmonycloud.dao.application.bean.ServiceTemplates;
import com.harmonycloud.dao.cluster.bean.NodePortClusterUsage;
import com.harmonycloud.dao.microservice.bean.MicroServiceInstance;
import com.harmonycloud.dao.microservice.bean.MicroServiceOperationTask;
import com.harmonycloud.dao.tenant.bean.NamespaceLocal;
import com.harmonycloud.dao.tenant.bean.TenantBinding;
import com.harmonycloud.dao.user.bean.UserRoleRelationship;
import com.harmonycloud.dto.integration.microservice.DeleteAndQueryMsfDto;
import com.harmonycloud.dto.integration.microservice.NamespaceDetailDto;
import com.harmonycloud.dto.integration.microservice.UpdateMicroServiceDto;
import com.harmonycloud.dto.integration.microservice.UpdateMsfInstanceDto;
import com.harmonycloud.k8s.bean.*;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.service.DeploymentService;
import com.harmonycloud.k8s.service.PodService;
import com.harmonycloud.k8s.service.ServicesService;
import com.harmonycloud.k8s.service.TprApplication;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.service.application.ApplicationTemplateService;
import com.harmonycloud.service.application.ApplicationWithServiceService;
import com.harmonycloud.service.application.DeploymentsService;
import com.harmonycloud.service.application.RouterService;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.cluster.NodePortClusterUsageService;
import com.harmonycloud.service.integration.MicroServiceInstanceService;
import com.harmonycloud.service.integration.MicroServiceOperationTaskService;
import com.harmonycloud.service.integration.MicroServiceService;
import com.harmonycloud.service.integration.MicroServiceWithKubeService;
import com.harmonycloud.service.integration.convert.MsfDataConvert;
import com.harmonycloud.service.platform.bean.microservice.DeployMsfDto;
import com.harmonycloud.service.platform.bean.microservice.MicroServiceInstanceStatusDto;
import com.harmonycloud.service.platform.bean.microservice.MsfDeployment;
import com.harmonycloud.service.platform.bean.microservice.MsfDeploymentPort;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.platform.convert.K8sResultConvert;
import com.harmonycloud.service.tenant.NamespaceLocalService;
import com.harmonycloud.service.tenant.NamespaceService;
import com.harmonycloud.service.tenant.PrivatePartitionService;
import com.harmonycloud.service.tenant.TenantService;
import com.harmonycloud.service.user.UserRoleRelationshipService;
import com.harmonycloud.service.user.UserService;
import net.sf.json.JSONArray;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.swing.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author jiangmi
 * @Description 微服务调用方法实现：更新、删除、部署、查询状态
 * @Date created in 2017-12-4
 * @Modified
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class MicroServiceServiceImpl implements MicroServiceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MicroServiceServiceImpl.class);

    @Autowired
    private MicroServiceInstanceService msfInstanceService;

    @Autowired
    private DeploymentService dpService;

    @Autowired
    private NamespaceLocalService namespaceLocalService;

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private ServicesService sService;

    @Autowired
    private PodService podService;

    @Autowired
    private MicroServiceOperationTaskService operationTaskService;

    @Autowired
    private ApplicationTemplateService appTemplateService;

    @Autowired
    private ApplicationWithServiceService appWithServiceService;

    @Autowired
    private ServiceTemplatesMapper serviceTemplatesMapper;

    @Autowired
    private MicroServiceWithKubeService msfKubeService;

    @Autowired
    private NodePortClusterUsageService portUsageService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRoleRelationshipService userRoleRelationshipService;

    @Autowired
    private TenantService tenantService;

    @Autowired
    private TprApplication tprApplication;

    @Autowired
    private DeploymentsService deploymentsService;

    @Autowired
    private RouterService routerService;

    @Autowired
    private NamespaceService namespaceService;

    @Autowired
    private PrivatePartitionService privatePartitionService;

    @Override
    public ActionReturnUtil updateMicroService(UpdateMicroServiceDto updateParams) throws Exception {
        try {
            if (StringUtils.isBlank(updateParams.getSpace_id()) || StringUtils.isBlank(updateParams.getTenant_id())) {
                return ActionReturnUtil.returnCodeAndMsg(MicroServiceCodeMessage.PARAMS_NULL, "", null);
            }

            //查询namespace名称
            NamespaceLocal namespaceBean = namespaceLocalService.getNamespaceByNamespaceId(updateParams.getSpace_id());
            if (namespaceBean == null) {
                return ActionReturnUtil.returnCodeAndMsg(MicroServiceCodeMessage.NAMESPACE_NOT_EXIST, updateParams.getSpace_id(), null);
            }
            String namespaceName = namespaceBean.getNamespaceName();

            //获取namespace当前配额
            Map<String, String> namespaceQuota = namespaceService.getNamespaceResourceRemainQuota(namespaceName);
            float cpuLeft = Float.valueOf(namespaceQuota.get(CommonConstant.CPU));
            double memLeft = Double.valueOf(namespaceQuota.get(CommonConstant.MEMORY));

            //查询集群
            Cluster cluster = clusterService.findClusterById(namespaceBean.getClusterId());

            //查询msf_instance表
            List<UpdateMsfInstanceDto> instances = updateParams.getInstances();
            for (UpdateMsfInstanceDto updateInstance : instances) {
                MicroServiceInstance msfInstance = msfInstanceService.findByInstanceId(updateInstance.getInstance_id());
                if (msfInstance == null) {
                    return ActionReturnUtil.returnCodeAndMsg(MicroServiceCodeMessage.FIND_FAILURE, updateInstance.getInstance_id(), null);
                }
                //计算该实例原本的cpu和memory
                float cpuOld = msfInstance.getReplicas() * Float.valueOf(msfInstance.getCpu());
                float cpuTotalLeft = cpuLeft + cpuOld;

                long memOld = msfInstance.getReplicas() * Integer.valueOf(msfInstance.getMemory());
                double memTotalLeft = memLeft + memOld;

                //根据deploymentName获取kubernetes Deployment
                K8SClientResponse depRes = dpService.doSpecifyDeployment(namespaceName, msfInstance.getDeploymentName(), null, null, HTTPMethod.GET, cluster);
                if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus())) {
                    return ActionReturnUtil.returnCodeAndMsg(MicroServiceCodeMessage.UPDATE_APP_FAILURE, updateInstance.getInstance_id(), null);
                }
                Deployment dep = JsonUtil.jsonToPojo(depRes.getBody(), Deployment.class);

                if (StringUtils.isNotEmpty(updateInstance.getReplicas())) {
                    dep.getSpec().setReplicas(Integer.valueOf(updateInstance.getReplicas()));
                    msfInstance.setReplicas(Integer.valueOf(updateInstance.getReplicas()));
                }
                if (StringUtils.isNotEmpty(updateInstance.getCpu()) || StringUtils.isNotEmpty(updateInstance.getMemory())) {
                    List<Container> containers = dep.getSpec().getTemplate().getSpec().getContainers();
                    for (Container cc : containers) {
                        Map<String, String> res = new HashMap<String, String>();
                        Map<String, String> resource = (Map<String, String>) cc.getResources().getRequests();
                        res.put("cpu", resource.get("cpu"));
                        res.put("memory", resource.get("memory"));
                        if (StringUtils.isNotEmpty(updateInstance.getCpu())) {
                            Double cpu = Double.valueOf(updateInstance.getCpu()) * Constant.CONTAINER_RESOURCE_CPU_TIMES;
                            res.put("cpu", cpu.intValue() + "m");
                            msfInstance.setCpu(updateInstance.getCpu());
                        }
                        if (StringUtils.isNotEmpty(updateInstance.getMemory())) {
                            res.put("memory", updateInstance.getMemory() + "Mi");
                            msfInstance.setMemory(updateInstance.getMemory());
                        }
                        cc.getResources().setLimits(res);
                        cc.getResources().setRequests(res);
                    }
                    dep.getSpec().getTemplate().getSpec().setContainers(containers);
                }
                //判断配额计算所需要的资源
                float cpuQuota = msfInstance.getReplicas() * Float.valueOf(msfInstance.getCpu());
                long memQuota = msfInstance.getReplicas() * Integer.valueOf(msfInstance.getMemory());
                if (cpuQuota > cpuTotalLeft) {
                    LOGGER.info("cpu不足，{}", cpuTotalLeft);
                    return ActionReturnUtil.returnCodeAndMsg(MicroServiceCodeMessage.UPDATE_APP_FAILURE, "空间CPU不足", null);
                }
                if (memQuota > memTotalLeft) {
                    LOGGER.info("内存不足，{}", memTotalLeft);
                    return ActionReturnUtil.returnCodeAndMsg(MicroServiceCodeMessage.UPDATE_APP_FAILURE, "空间内存不足", null);
                }
                Map<String, Object> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                Map<String, Object> bodys = new HashMap<String, Object>();
                bodys = CollectionUtil.transBean2Map(dep);
                K8SClientResponse putRes = dpService.doSpecifyDeployment(namespaceName, msfInstance.getDeploymentName(), headers,
                        bodys, HTTPMethod.PUT, cluster);
                if (!HttpStatusUtil.isSuccessStatus(putRes.getStatus())) {
                    return ActionReturnUtil.returnCodeAndMsg(MicroServiceCodeMessage.UPDATE_APP_FAILURE, updateInstance.getInstance_id(), null);
                }
                //更新数据库
                msfInstanceService.updateMicroServiceInstance(msfInstance);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        }
        return ActionReturnUtil.returnCodeAndMsg(MicroServiceCodeMessage.SUCCESS, "", null);
    }

    @Override
    public Map<String, Object> queryTaskStatus(String taskId) throws Exception {
        Map<String, Object> res = new HashMap<>();
        res.put("task_id", taskId);
        try {
            //查询任务类型
            MicroServiceOperationTask msfTask = operationTaskService.findByTaskId(taskId);

            //查询namespace
            NamespaceLocal namespaceBean = namespaceLocalService.getNamespaceByNamespaceId(msfTask.getNamespaceId());
            if (namespaceBean == null) {
                return ActionReturnUtil.returnCodeAndMsg(MicroServiceCodeMessage.NAMESPACE_NOT_EXIST, "", taskId);
            }
            String namespaceName = namespaceBean.getNamespaceName();

            //查询集群
            Cluster cluster = clusterService.findClusterById(namespaceBean.getClusterId());

            //根据任务Id查询任务对应的所有实例
            List<MicroServiceInstance> instances = msfInstanceService.queryByTaskId(taskId);
            if (instances == null) {
                return ActionReturnUtil.returnCodeAndMsg(MicroServiceCodeMessage.FIND_FAILURE, "", taskId);
            }

            //判断任务类型,如果是部署类型
            List<Map<String, Object>> resIns = new ArrayList<>();
            if (Constant.TASK_TYPE_DEPLOY == msfTask.getTaskType() || Constant.TASK_TYPE_RESET == msfTask.getTaskType()) {
                for (MicroServiceInstance msfInstance : instances) {
                    Map<String, Object> tmp = new HashMap<>();

                    //查询Deployment
                    K8SClientResponse depRes = dpService.doSpecifyDeployment(namespaceName, msfInstance.getDeploymentName(), null, null, HTTPMethod.GET, cluster);
                    if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus()) && depRes.getStatus() != Constant.HTTP_404) {
                        UnversionedStatus unversionedStatus = JsonUtil.jsonToPojo(depRes.getBody(), UnversionedStatus.class);
                        throw new Exception(unversionedStatus.getMessage());
                    }
                    if (Constant.HTTP_404 == depRes.getStatus()) {
                        res.put("code", MicroServiceCodeMessage.SPACE_TASK_FAILURE.value());
                        res.put("msg", MicroServiceCodeMessage.SPACE_TASK_FAILURE.getMessage() + CommonConstant.COMMA + msfTask.getErrorMsg());
                        return res;
                    }
                    Deployment dep = JsonUtil.jsonToPojo(depRes.getBody(), Deployment.class);
                    String status = MsfDataConvert.getAppStatus(dep);

                    tmp.put("instance_id", msfInstance.getInstanceId());
                    tmp.put("status", status);
                    tmp.put("deployment_name", msfInstance.getDeploymentName());

                    //查询Service
                    K8SClientResponse sRes = sService.doSepcifyService(namespaceName, dep.getMetadata().getName(), null, null, HTTPMethod.GET, cluster);
                    if (!HttpStatusUtil.isSuccessStatus(sRes.getStatus())) {
                        return ActionReturnUtil.returnErrorWithData(sRes.getBody());
                    }
                    com.harmonycloud.k8s.bean.Service svc = JsonUtil.jsonToPojo(sRes.getBody(), com.harmonycloud.k8s.bean.Service.class);

                    //查询configmap和ingress
                    List<Map<String, Object>> externalInfo = msfKubeService.getExternalInfo(dep.getMetadata().getName(), namespaceName, cluster, svc);
                    tmp.put("external_services", externalInfo);
                    resIns.add(tmp);
                }
                res.put("instances", resIns);
            }
            //判断任务状态
            switch (String.valueOf(msfTask.getStatus())) {
                case Constant.SPRINGCLOUD_TASK_SUCCESS:
                    res.put("code", MicroServiceCodeMessage.SUCCESS.value());
                    res.put("msg", MicroServiceCodeMessage.SUCCESS.getMessage());
                    break;
                case Constant.SPRINGCLOUD_TASK_FAILURE:
                    res.put("code", MicroServiceCodeMessage.SPACE_TASK_FAILURE.value());
                    String msg = MicroServiceCodeMessage.SPACE_TASK_FAILURE.getMessage() + CommonConstant.COMMA + msfTask.getErrorMsg();
                    LOGGER.info("任务失败，{}", msg);
                    res.put("msg", msg);
                    break;
                case Constant.SPRINGCLOUD_TASK_DOING:
                    res.put("code", MicroServiceCodeMessage.SPACE_TASK_DOING.value());
                    res.put("msg", MicroServiceCodeMessage.SPACE_TASK_DOING.getMessage());
                    break;
            }
            return res;
        } catch (Exception e) {
            LOGGER.error("查询实例状态失败：", e);
            throw e;
        }
    }

    @Override
    public Map<String, Object> queryInstanceStatus(DeleteAndQueryMsfDto queryParams) throws Exception {
        try {
            if (StringUtils.isEmpty(queryParams.getSpace_id())) {
                return ActionReturnUtil.returnCodeAndMsg(MicroServiceCodeMessage.PARAMS_NULL, "空间Id", null);
            }
            String namespaceId = queryParams.getSpace_id();

            //查询namespace名称
            NamespaceLocal namespaceBean = namespaceLocalService.getNamespaceByNamespaceId(namespaceId);
            if (namespaceBean == null) {
                return ActionReturnUtil.returnCodeAndMsg(MicroServiceCodeMessage.NAMESPACE_NOT_EXIST, "", null);
            }
            String namespaceName = namespaceBean.getNamespaceName();

            //查询集群
            Cluster cluster = clusterService.findClusterById(namespaceBean.getClusterId());
            List<MicroServiceInstanceStatusDto> instanceStatusList = new ArrayList<MicroServiceInstanceStatusDto>();
            for (String insId : queryParams.getInstance_ids()) {

                //查询msf_instance表获取deployment名称
                MicroServiceInstance msfIns = msfInstanceService.findByInstanceId(insId);
                if (msfIns == null) {
                    return ActionReturnUtil.returnCodeAndMsg(MicroServiceCodeMessage.APP_NOT_EXIST, insId, null);
                }

                //查询Deployment
                K8SClientResponse depRes = dpService.doSpecifyDeployment(namespaceName, msfIns.getDeploymentName(), null, null, HTTPMethod.GET, cluster);
                Deployment dep = new Deployment();
                if (depRes.getStatus() != Constant.HTTP_404) {
                    dep = JsonUtil.jsonToPojo(depRes.getBody(), Deployment.class);
                }

                MicroServiceInstanceStatusDto msfServiceInstanceDetail = new MicroServiceInstanceStatusDto();
                if (dep != null) {
                    //查询POD
                    Map<String, Object> bodys = new HashMap<String, Object>();
                    bodys.put("labelSelector", K8sResultConvert.convertExpression(dep, msfIns.getDeploymentName()));
                    K8SClientResponse podRes = podService.getPodByNamespace(namespaceName, null, bodys, HTTPMethod.GET, cluster);
                    if (!HttpStatusUtil.isSuccessStatus(podRes.getStatus())) {
                        return ActionReturnUtil.returnErrorWithData(podRes.getBody());
                    }
                    PodList podList = JsonUtil.jsonToPojo(podRes.getBody(), PodList.class);

                    //查询Service
                    K8SClientResponse sRes = sService.doSepcifyService(namespaceName, msfIns.getServiceName(), null, null, HTTPMethod.GET, cluster);
                    if (!HttpStatusUtil.isSuccessStatus(sRes.getStatus())) {
                        return ActionReturnUtil.returnErrorWithData(sRes.getBody());
                    }
                    com.harmonycloud.k8s.bean.Service svc = JsonUtil.jsonToPojo(sRes.getBody(), com.harmonycloud.k8s.bean.Service.class);

                    //查询configmap和ingress
                    List<Map<String, Object>> externalInfo = msfKubeService.getExternalInfo(dep.getMetadata().getName(), namespaceName, cluster, svc);

                    //将deployment,service, podList转化成返回参数对象
                    msfServiceInstanceDetail = MsfDataConvert.convertMsfInstanceStatusDetail(dep, podList, svc, msfIns, externalInfo);
                }
                instanceStatusList.add(msfServiceInstanceDetail);
            }
            Map<String, Object> res = new HashMap<>();
            res.put("code", MicroServiceCodeMessage.SUCCESS.value());
            res.put("msg", MicroServiceCodeMessage.SUCCESS.getMessage());
            res.put("instances", instanceStatusList);
            return res;
        } catch (Exception e) {
            LOGGER.error("查询微服务组件实例状态失败：", e);
            throw e;
        }
    }

    @Override
    public ActionReturnUtil queryTenantsWithRole(HttpServletRequest request) {
        try {
            List<Map<String, Object>> result = new ArrayList<>();

            String userName = userService.getCurrentUsername();

            //判断是否是管理员
            boolean isAdmin = userService.isAdmin(userName);
            if (isAdmin) {
                //获取租户列表
                List<TenantBinding> tenantList = tenantService.listAllTenant();
                tenantList.forEach(tenantBinding -> {
                    Map<String, Object> tmp = new HashMap<>();
                    tmp.put("tenant_id", tenantBinding.getTenantId());
                    tmp.put("tenant_name", tenantBinding.getTenantName());
                    tmp.put("tenant_alias", tenantBinding.getAliasName());
                    List<String> roles = Arrays.asList(Constant.SPRINGCLOUD_USER_ROLE_ADMIN);
                    tmp.put("role", roles);
                    result.add(tmp);
                });
                return ActionReturnUtil.returnCodeAndMsgWithSuccess(MicroServiceCodeMessage.SUCCESS, result);
            }
            //除管理员外，根据用户名获取租户列表
            List<TenantBinding> tenants = userRoleRelationshipService.listTenantByUsername(userName);
            if (CollectionUtils.isNotEmpty(tenants)) {
                //判断用户是否是租户管理员
                tenants.forEach(tenantBinding -> {
                    Map<String, Object> tmp = new HashMap<>();
                    String tm = tenantBinding.getTmUsernames();
                    List<String> roles = new ArrayList<>();
                    roles.add(Constant.SPRINGCLOUD_USER_ROLE_NORMAL);
                    if (StringUtils.isNotBlank(tm)) {
                        String[] tmList = tm.split(",");
                        for (String tenantManager : tmList) {
                            if (userName.equals(tenantManager)) {
                                roles.clear();
                                roles = Arrays.asList(Constant.SPRINGCLOUD_USER_ROLE_TENANT, Constant.SPRINGCLOUD_USER_ROLE_SPACE);
                                break;
                            }
                        }
                    }
                    tmp.put("tenant_id", tenantBinding.getTenantId());
                    tmp.put("tenant_name", tenantBinding.getTenantName());
                    tmp.put("tenant_alias", tenantBinding.getAliasName());
                    tmp.put("role", roles);
                    result.add(tmp);
                });
            }
            return ActionReturnUtil.returnCodeAndMsgWithSuccess(MicroServiceCodeMessage.SUCCESS, result);
        } catch (Exception e) {
            LOGGER.error("查询租户列表失败：", e);
            return ActionReturnUtil.returnCodeAndMsg(MicroServiceCodeMessage.FIND_FAILURE, "", null);
        }
    }

    @Override
    public ActionReturnUtil deploySpace(DeployMsfDto deployParams) throws Exception {
        try {
            if (StringUtils.isBlank(deployParams.getSpace_id()) || StringUtils.isBlank(deployParams.getTenant_id())) {
                return ActionReturnUtil.returnCodeAndMsg(MicroServiceCodeMessage.PARAMS_NULL, "空间", null);
            }
            String userName = userService.getCurrentUsername();

            final String namespaceId = deployParams.getSpace_id();

            //查询namespace名称
            NamespaceLocal namespaceBean = namespaceLocalService.getNamespaceByNamespaceId(namespaceId);
            if (Objects.isNull(namespaceBean)) {
                return ActionReturnUtil.returnCodeAndMsg(MicroServiceCodeMessage.NAMESPACE_NOT_EXIST, "", null);
            }
            final String namespaceName = namespaceBean.getNamespaceName();

            final List<MsfDeployment> msfDeployments = deployParams.getDeployments();
            //判断空间的资源配额
            ActionReturnUtil checkRes = checkResource(msfDeployments, namespaceName);
            if (!checkRes.containsKey("success")) {
                return checkRes;
            }
            //判断空间内是否有进行中的任务
            List<MicroServiceOperationTask> doingTask = operationTaskService.getTaskByNamespaceAndStatus(namespaceId, Constant.SPRINGCLOUD_TASK_DOING);
            if (doingTask != null && doingTask.size() > 0) {
                return ActionReturnUtil.returnCodeAndMsg(MicroServiceCodeMessage.SPACE_EXIST_DOING_TASK, "", null);
            }
            boolean isCanInit = false;
            //判断空间是否已被清空
            List<MicroServiceOperationTask> deployTasks = operationTaskService.findTaskByType(namespaceId, Constant.TASK_TYPE_DEPLOY);
            List<MicroServiceOperationTask> deleteTasks = operationTaskService.findTaskByType(namespaceId, Constant.TASK_TYPE_DELETE);
            MicroServiceOperationTask deployTask = new MicroServiceOperationTask();
            //判断是否有失败的初始化任务
            if (CollectionUtils.isNotEmpty(deployTasks)) {
                deployTask = deployTasks.get(deployTasks.size() - CommonConstant.NUM_ONE);
                if (CollectionUtils.isNotEmpty(deleteTasks)) {
                    //比较释放和初始化空间的时间
                    Date deployTime = deployTask.getCreateTime();
                    Date deleteTime = deleteTasks.get(deleteTasks.size() - deleteTasks.size()).getCreateTime();
                    if (deployTime.compareTo(deleteTime) < 0) {
                        isCanInit = true;
                    }
                } else {
                    for (MicroServiceOperationTask task : deployTasks) {
                        if (Constant.SPRINGCLOUD_TASK_SUCCESS.equals(task.getStatus().toString())) {
                            return ActionReturnUtil.returnCodeAndMsg(MicroServiceCodeMessage.SPACE_INITIALIZED, "", null);
                        }
                    }
                    isCanInit = true;
                }
            }
            final String taskId = UUIDUtil.getUUID();
            String tenant = namespaceName.substring(0, namespaceName.lastIndexOf('-'));
            final Integer appTemplateId = saveAppTemplate(namespaceId, namespaceName, tenant, userName, namespaceBean.getClusterId());
            if (isCanInit) {
                //更新数据库
                deployTask.setTaskId(taskId);
                deployTask.setUpdateTime(new Date());
                deployTask.setStatus(Integer.valueOf(Constant.SPRINGCLOUD_TASK_DOING));
                deployTask.setErrorMsg(CommonConstant.ZERONUM);
                operationTaskService.updateTask(deployTask);
            } else {
                //将任务信息插入数据库
                MicroServiceOperationTask operationTask = new MicroServiceOperationTask();
                operationTask.setTaskId(taskId);
                operationTask.setStatus(Integer.valueOf(Constant.SPRINGCLOUD_TASK_DOING));
                operationTask.setCreateTime(new Date());
                operationTask.setAppTemplateId(appTemplateId);
                operationTask.setTaskType(Constant.TASK_TYPE_DEPLOY);
                operationTask.setNamespaceId(namespaceId);
                operationTaskService.insertTask(operationTask);
            }
            List<Map<String, Object>> kubeAppFormatList = new ArrayList<>();

            //查询集群
            final Cluster cluster = clusterService.findClusterById(namespaceBean.getClusterId());

            //将服务模板插入数据库并组装成deployment
            final String appName = Constant.MSF + namespaceName;
            //获取分区的独占或者共享的节点标签
            String nodeLabel = privatePartitionService.getPrivatePartitionLabel(deployParams.getTenant_id(), namespaceName);

            //删除该分区下的所有实例信息
            List<MicroServiceInstance> msfInstances = msfInstanceService.getMsfInstancesByNamespaceId(namespaceId);
            for (MicroServiceInstance ins : msfInstances) {
                msfInstanceService.deleteMicroServiceInstance(ins.getInstanceId(), namespaceId);
            }
            for (MsfDeployment deployment : msfDeployments) {
                String serviceName = deployment.getSpec().getService_name();
                String depName = serviceName;
                deployment.getMetadata().setDeployment_name(depName);

                //插入数据库
                saveServiceTemplates(deployment, namespaceName, tenant, userName, appTemplateId, namespaceBean.getClusterId());
                Map<String, Object> tmp = new HashMap<>();

                //组装deployment和service
                tmp.put("deployment", MsfDataConvert.formatAppDeploymentData(deployment, userName, appName, namespaceName, cluster, nodeLabel));
                tmp.put("service", MsfDataConvert.formatAppServiceData(deployment));

                //将暴露端口信息放入msfdeployment中
                tmp.put("msfContent", assignPort(deployment, cluster));
                insertMsfInstance(deployment, namespaceBean.getTenantId(), cluster.getId(), taskId, namespaceId);
                kubeAppFormatList.add(tmp);
            }

            if (!checkNamespaceExist(namespaceId)) {
                return ActionReturnUtil.returnCodeAndMsg(MicroServiceCodeMessage.NAMESPACE_NOT_EXIST, "", null);
            }
            //开启线程部署应用
            ThreadPoolExecutorFactory.executor.execute((new Runnable() {
                @Override
                public void run() {
                    try {
                        //创建应用
                        createAppTPR(namespaceName, cluster, userName, appName);
                        List<String> depNames = deployMsfInstance(kubeAppFormatList, namespaceBean, cluster, null);
                        boolean isFailed = queryInstanceStatusLoop(depNames, namespaceName, cluster, taskId);
                        if (isFailed) {
                            updateTask(taskId, Constant.SPRINGCLOUD_TASK_FAILURE, "组件实例启动失败");
                            //删除deployment和service，以及对外ingress和端口
                            deleteAllMsfInstancesInNamespace(kubeAppFormatList, namespaceName, cluster, userName, true, null);
                        }
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage(), e);
                        //更新任务数据库,任务失败 1:成功 2:失败 3:进行中
                        try {
                            updateTask(taskId, Constant.SPRINGCLOUD_TASK_FAILURE, e.getMessage());

                            //删除deployment和service，以及对外ingress和端口
                            deleteAllMsfInstancesInNamespace(kubeAppFormatList, namespaceName, cluster, userName, true, null);
                        } catch (Exception e1) {
                            LOGGER.error(e1.getMessage(), e1);
                        }
                        return;
                    }
                }
            }));
            return ActionReturnUtil.returnCodeAndMsg(MicroServiceCodeMessage.SUCCESS, "", taskId);
        } catch (Exception e) {
            LOGGER.error("初始化组件失败：", e);
            throw e;
        }
    }

    @Override
    public ActionReturnUtil resetSpace(DeployMsfDto resetParams) throws Exception {
        try {
            if (StringUtils.isBlank(resetParams.getSpace_id()) || StringUtils.isBlank(resetParams.getTenant_id())) {
                return ActionReturnUtil.returnCodeAndMsg(MicroServiceCodeMessage.PARAMS_NULL, "空间", null);
            }
            String userName = userService.getCurrentUsername();

            final String namespaceId = resetParams.getSpace_id();
            //查询namespace名称
            NamespaceLocal namespaceBean = namespaceLocalService.getNamespaceByNamespaceId(namespaceId);
            if (namespaceBean == null) {
                return ActionReturnUtil.returnCodeAndMsg(MicroServiceCodeMessage.NAMESPACE_NOT_EXIST, "", null);
            }
            final String namespaceName = namespaceBean.getNamespaceName();

            //判断空间内是否有进行中的任务
            List<MicroServiceOperationTask> doingTask = operationTaskService.getTaskByNamespaceAndStatus(namespaceId, Constant.SPRINGCLOUD_TASK_DOING);
            if (doingTask != null && doingTask.size() > 0) {
                return ActionReturnUtil.returnCodeAndMsg(MicroServiceCodeMessage.SPACE_EXIST_DOING_TASK, namespaceName, null);
            }

            //判断是否初始化
            List<MicroServiceOperationTask> deployTasks = operationTaskService.findTaskByType(namespaceId, Constant.TASK_TYPE_DEPLOY);
            if (CollectionUtils.isEmpty(deployTasks)) {
                return ActionReturnUtil.returnCodeAndMsg(MicroServiceCodeMessage.SPACE_NOT_INIT, namespaceName, null);
            }
            String taskId = UUIDUtil.getUUID();

            //查询集群
            final Cluster cluster = clusterService.findClusterById(namespaceBean.getClusterId());

            //从数据库中获取该空间下的所有实例
            List<MicroServiceInstance> instances = msfInstanceService.getMsfInstancesByNamespaceId(namespaceId);
            List<MsfDeployment> msfDeployments = resetParams.getDeployments();

            String tenant = namespaceName.substring(0, namespaceName.lastIndexOf('-'));
            Integer appTemplateId = saveAppTemplate(namespaceId, namespaceName, tenant, userName, namespaceBean.getClusterId());

            //判断是否全部重置
            boolean isResetSpace = msfDeployments.size() > CommonConstant.NUM_ONE? true : false;
            LOGGER.info("是否重置空间：{}", isResetSpace);

            //插入任务
            MicroServiceOperationTask operationTask = new MicroServiceOperationTask();
            operationTask.setTaskId(taskId);
            operationTask.setStatus(Integer.valueOf(Constant.SPRINGCLOUD_TASK_DOING));
            operationTask.setCreateTime(new Date());
            operationTask.setAppTemplateId(appTemplateId);
            Integer type = isResetSpace ? Constant.TASK_TYPE_RESET : Constant.TASK_TYPE_RESET_INSTANCE;
            operationTask.setTaskType(type);
            operationTask.setNamespaceId(namespaceId);
            operationTaskService.insertTask(operationTask);

            if (!checkNamespaceExist(namespaceId)) {
                return ActionReturnUtil.returnCodeAndMsg(MicroServiceCodeMessage.NAMESPACE_NOT_EXIST, "", null);
            }
            //获取分区的独占或者共享的节点标签
            final String nodeLabel = privatePartitionService.getPrivatePartitionLabel(resetParams.getTenant_id(), namespaceName);
            ThreadPoolExecutorFactory.executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        String consulPort = null;
                        if (!isResetSpace) {
                            consulPort = msfKubeService.getConsulExposePort(msfDeployments, cluster, namespaceName);
                        }
                        for (MsfDeployment deployment : msfDeployments) {
                            for (MicroServiceInstance ins : instances) {
                                if (deployment.getMetadata().getInstance_id().equals(ins.getInstanceId())) {
                                    String serviceName = ins.getServiceName();
                                    String depName = ins.getDeploymentName();

                                    //删除Deployment,Service,configmap,ingress
                                    msfKubeService.deleteMsfDeployment(namespaceName, depName, cluster, serviceName, consulPort);
                                }
                            }
                        }
                        if (isResetSpace) {
                            //删除service模板
                            List<ApplicationService> applicationServices = appWithServiceService.listApplicationServiceByAppTemplatesId(appTemplateId);
                            if (applicationServices != null && applicationServices.size() > 0) {
                                for (ApplicationService applicationService : applicationServices) {
                                    serviceTemplatesMapper.deleteById(applicationService.getServiceId());
                                }
                            }
                        }

                        List<Map<String, Object>> kubeAppFormatList = new ArrayList<>();
                        String appName = Constant.MSF + namespaceName;
                        for (MsfDeployment deployment : msfDeployments) {
                            String serviceName = deployment.getSpec().getService_name();
                            String depName = serviceName;

                            //保存service模板
                            if (isResetSpace) {
                                saveServiceTemplates(deployment, namespaceName, tenant, userName, appTemplateId, namespaceBean.getClusterId());
                            }

                            //更新instance
                            updateMsfInstance(deployment, taskId);

                            //组装数据
                            deployment.getMetadata().setDeployment_name(depName);
                            Map<String, Object> tmp = new HashMap<>();

                            //组装deployment和service
                            tmp.put("deployment", MsfDataConvert.formatAppDeploymentData(deployment, userName, appName, namespaceName, cluster, nodeLabel));
                            tmp.put("service", MsfDataConvert.formatAppServiceData(deployment));

                            //将暴露端口信息放入msfdeployment中
                            tmp.put("msfContent", assignPort(deployment, cluster));
                            kubeAppFormatList.add(tmp);
                        }
                        if (isResetSpace) {
                            //创建应用
                            createAppTPR(namespaceName, cluster, userName, appName);
                        }
                        //部署实例
                        List<String> depNames = deployMsfInstance(kubeAppFormatList, namespaceBean, cluster, consulPort);
                        boolean isFailed = queryInstanceStatusLoop(depNames, namespaceName, cluster, taskId);
                        if (isFailed) {
                            LOGGER.info("线程中，是否重置空间：{}", isResetSpace);
                            updateTask(taskId, Constant.SPRINGCLOUD_TASK_FAILURE, "组件实例启动失败");
                            //删除需要重置的组件的deployment和service，以及对外ingress和端口
                            deleteAllMsfInstancesInNamespace(kubeAppFormatList, namespaceName, cluster, userName, isResetSpace, consulPort);
                        }
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage(), e);

                        //更新任务数据表
                        try {
                            updateTask(taskId, Constant.SPRINGCLOUD_TASK_FAILURE, e.getMessage());
                        } catch (Exception e1) {
                            LOGGER.error("更新重置空间任务状态失败:", e1);
                            return;
                        }
                        return;
                    }
                }
            });
            return ActionReturnUtil.returnCodeAndMsg(MicroServiceCodeMessage.SUCCESS, "", taskId);
        } catch (Exception e) {
            LOGGER.error("重置空间失败:", e);
            throw e;
        }
    }

    @Override
    public ActionReturnUtil deleteSpace(DeleteAndQueryMsfDto deleteAndQueryMsfDto) throws Exception {
        try {
            if (StringUtils.isBlank(deleteAndQueryMsfDto.getSpace_id())) {
                return ActionReturnUtil.returnCodeAndMsg(MicroServiceCodeMessage.PARAMS_NULL, "空间", null);
            }
            final String namespaceId = deleteAndQueryMsfDto.getSpace_id();

            //查询namespace名称
            NamespaceLocal namespaceBean = namespaceLocalService.getNamespaceByNamespaceId(namespaceId);
            if (namespaceBean == null) {
                return ActionReturnUtil.returnCodeAndMsg(MicroServiceCodeMessage.NAMESPACE_NOT_EXIST, "", null);
            }
            final String namespaceName = namespaceBean.getNamespaceName();

            //判断空间内是否有进行中的任务
            List<MicroServiceOperationTask> doingTask = operationTaskService.getTaskByNamespaceAndStatus(namespaceId, Constant.SPRINGCLOUD_TASK_DOING);
            if (doingTask != null && doingTask.size() > 0) {
                return ActionReturnUtil.returnCodeAndMsg(MicroServiceCodeMessage.SPACE_EXIST_DOING_TASK, namespaceName, null);
            }

            //判断是否初始化
            List<MicroServiceOperationTask> deployTask = operationTaskService.findTaskByType(namespaceId, Constant.TASK_TYPE_DEPLOY);
            if (CollectionUtils.isEmpty(deployTask)) {
                return ActionReturnUtil.returnCodeAndMsg(MicroServiceCodeMessage.SPACE_NOT_INIT, namespaceName, null);
            }

            //查询集群
            final Cluster cluster = clusterService.findClusterById(namespaceBean.getClusterId());

            //查询该namespace下所有的实例
            final List<MicroServiceInstance> instances = msfInstanceService.getMsfInstancesByNamespaceId(namespaceId);

            final String taskId = UUIDUtil.getUUID();

            //插入任务
            ApplicationTemplates appTemplate = appTemplateService.selectByNamespaceId(namespaceId);
            MicroServiceOperationTask operationTask = new MicroServiceOperationTask();
            operationTask.setTaskId(taskId);
            operationTask.setStatus(Integer.valueOf(Constant.SPRINGCLOUD_TASK_DOING));
            operationTask.setCreateTime(new Date());
            if (appTemplate != null) {
                operationTask.setAppTemplateId(appTemplate.getId());
            }
            operationTask.setTaskType(Constant.TASK_TYPE_DELETE);
            operationTask.setNamespaceId(namespaceId);
            operationTaskService.insertTask(operationTask);

            ThreadPoolExecutorFactory.executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        //删除应用
                        ActionReturnUtil tprDelete = tprApplication.delApplicationByName(Constant.MSF + namespaceName, namespaceName, cluster);
                        if (!tprDelete.isSuccess()) {
                            throw new Exception("删除应用失败");
                        }
                        boolean deleteIsSuccess = true;
                        for (MicroServiceInstance ins : instances) {
                            deleteIsSuccess = deleteInstance(ins, namespaceName, cluster);
                        }
                        if (!deleteIsSuccess) {

                            //更新任务数据表:失败
                            updateTask(taskId, Constant.SPRINGCLOUD_TASK_FAILURE, null);
                        } else {

                            //更新任务数据表：成功
                            updateTask(taskId, Constant.SPRINGCLOUD_TASK_SUCCESS, null);
                        }
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage(), e);

                        //更新任务数据表
                        try {
                            updateTask(taskId, Constant.SPRINGCLOUD_TASK_FAILURE, e.getMessage());
                        } catch (Exception e1) {
                            LOGGER.error("更新删除空间任务状态失败:", e1);
                            return;
                        }
                        return;
                    }
                }
            });
            return ActionReturnUtil.returnCodeAndMsg(MicroServiceCodeMessage.SUCCESS, "", taskId);
        } catch (Exception e) {
            LOGGER.error("删除空间失败:", e);
            throw e;
        }
    }

    @Override
    public ActionReturnUtil getSpaceByTenant(DeleteAndQueryMsfDto params) throws Exception {
        try {
            if (Objects.isNull(params) || CollectionUtils.isEmpty(params.getTenant_ids())) {
                return ActionReturnUtil.returnCodeAndMsg(MicroServiceCodeMessage.PARAMS_NULL, null, null);
            }
            List<NamespaceDetailDto> namespaceDetailList = new ArrayList<>();

            //获取空间列表
            List<String> tenantList = params.getTenant_ids();
            for (String tenantId : tenantList) {
                List<NamespaceLocal> namespaceLocalList = namespaceLocalService.getAllNamespaceListByTenantIdWithMSF(tenantId);
                for (NamespaceLocal nsLocal : namespaceLocalList) {
                    NamespaceDetailDto nsDetail = new NamespaceDetailDto();
                    nsDetail.setTenant_id(nsLocal.getTenantId());
                    nsDetail.setCluster_id(nsLocal.getClusterId());
                    nsDetail.setId(nsLocal.getNamespaceId());
                    nsDetail.setNamespace_id(nsLocal.getNamespaceId());
                    nsDetail.setNamespace_name(nsLocal.getNamespaceName());
                    nsDetail.setShared(nsLocal.getIsPrivate() ? Constant.SPRINGCLOUD_NAMESPACE_PRIVATE : Constant.SPRINGCLOUD_NAMESPACE_SHARE);
                    nsDetail.setCreated(DateUtil.DateToString(nsLocal.getCreateTime(), DateStyle.YYYY_MM_DD_HH_MM_SS));
                    nsDetail.setUpdated(DateUtil.DateToString(nsLocal.getUpdateTime(), DateStyle.YYYY_MM_DD_HH_MM_SS));
                    nsDetail.setNamespace_alias(nsLocal.getAliasName());
                    Cluster cluster = clusterService.findClusterById(nsLocal.getClusterId());
                    nsDetail.setCluster_name(cluster.getName());
                    if (CollectionUtils.isNotEmpty(cluster.getExternal())) {
                        nsDetail.setExternal_loadbalancer_ip(cluster.getExternal().get(0).getTopLb());
                    }
                    namespaceDetailList.add(nsDetail);
                }
            }
            return ActionReturnUtil.returnCodeAndMsgWithSuccess(MicroServiceCodeMessage.SUCCESS, namespaceDetailList);
        } catch (Exception e) {
            LOGGER.error("获取空间失败：", e);
            throw e;
        }
    }

    @Override
    public ActionReturnUtil deleteMicroService(DeleteAndQueryMsfDto deleteParams) throws Exception {
        try {
            if (StringUtils.isBlank(deleteParams.getSpace_id())) {
                return ActionReturnUtil.returnCodeAndMsg(MicroServiceCodeMessage.PARAMS_NULL, "空间", null);
            }
            final List<String> instanceIds = deleteParams.getInstance_ids();
            if (instanceIds == null) {
                return ActionReturnUtil.returnCodeAndMsg(MicroServiceCodeMessage.PARAMS_NULL, "实例Id", null);
            }
            final String namespaceId = deleteParams.getSpace_id();

            //查询namespace名称
            NamespaceLocal namespaceBean = namespaceLocalService.getNamespaceByNamespaceId(namespaceId);
            if (namespaceBean == null) {
                return ActionReturnUtil.returnCodeAndMsg(MicroServiceCodeMessage.NAMESPACE_NOT_EXIST, "", null);
            }
            final String namespaceName = namespaceBean.getNamespaceName();

            //判断空间内是否有进行中的任务
            List<MicroServiceOperationTask> doingTask = operationTaskService.getTaskByNamespaceAndStatus(namespaceId, Constant.SPRINGCLOUD_TASK_DOING);
            if (doingTask != null && doingTask.size() > 0) {
                return ActionReturnUtil.returnCodeAndMsg(MicroServiceCodeMessage.SPACE_EXIST_DOING_TASK, null, null);
            }

            //判断是否初始化
            List<MicroServiceOperationTask> deployTask = operationTaskService.findTaskByType(namespaceId, Constant.TASK_TYPE_DEPLOY);
            if (CollectionUtils.isNotEmpty(deployTask)) {
                return ActionReturnUtil.returnCodeAndMsg(MicroServiceCodeMessage.SPACE_NOT_INIT, null, null);
            }

            //查询集群
            final Cluster cluster = clusterService.findClusterById(namespaceBean.getClusterId());
            final String taskId = UUIDUtil.getUUID();

            //插入任务
            ApplicationTemplates appTemplate = appTemplateService.selectByNamespaceId(namespaceId);
            MicroServiceOperationTask operationTask = new MicroServiceOperationTask();
            operationTask.setTaskId(taskId);
            operationTask.setStatus(Integer.valueOf(Constant.SPRINGCLOUD_TASK_DOING));
            operationTask.setCreateTime(new Date());
            if (appTemplate != null) {
                operationTask.setAppTemplateId(appTemplate.getId());
            }
            operationTask.setTaskType(Constant.TASK_TYPE_DELETE);
            operationTask.setNamespaceId(namespaceId);
            operationTaskService.insertTask(operationTask);

            ThreadPoolExecutorFactory.executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        boolean deleteIsSuccess = true;
                        for (String insId : instanceIds) {

                            //查询
                            MicroServiceInstance instance = msfInstanceService.findByInstanceId(insId);
                            deleteIsSuccess = deleteInstance(instance, namespaceName, cluster);
                        }
                        if (!deleteIsSuccess) {

                            //更新任务数据表:失败
                            updateTask(taskId, Constant.SPRINGCLOUD_TASK_FAILURE, null);
                        } else {
                            //更新任务数据表：成功
                            updateTask(taskId, Constant.SPRINGCLOUD_TASK_SUCCESS, null);
                        }
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage(), e);

                        //更新任务数据表
                        try {
                            updateTask(taskId, Constant.SPRINGCLOUD_TASK_FAILURE, e.getMessage());
                        } catch (Exception e1) {
                            LOGGER.error("更新删除组件任务状态失败:", e1);
                            return;
                        }
                        return;
                    }
                }
            });
            return ActionReturnUtil.returnCodeAndMsg(MicroServiceCodeMessage.SUCCESS, "", taskId);
        } catch (Exception e) {
            LOGGER.error("删除组件失败:", e);
            throw e;
        }
    }

    /**
     * 判断是否
     *
     * @param userName
     * @param tenantId
     * @return boolean
     * @throws Exception
     */
    private boolean checkPrivilege(String userName, String tenantId) throws Exception {
        boolean isAdmin = userService.isAdmin(userName);
        if (!isAdmin) {
            List<UserRoleRelationship> tmList = tenantService.listTenantTm(tenantId);
            if (CollectionUtils.isEmpty(tmList)) {
                return false;
            }
            boolean isTm = false;

            //查询用户是否在租户管理员列表内
            for (UserRoleRelationship tm : tmList) {
                if (userName.equals(tm.getUsername())) {
                    isTm = true;
                    break;
                }
            }
            if (!isTm) {
                return false;
            }
        }
        return true;
    }

    /**
     * 保存应用模板内的服务信息
     *
     * @param msfDep
     * @param namespace
     * @param tenant
     * @param userName
     * @param appTmId
     * @throws Exception
     */
    private void saveServiceTemplates(MsfDeployment msfDep, String namespace, String tenant, String userName, Integer appTmId, String clusterId) throws Exception {
        String serviceName = msfDep.getSpec().getService_name();
        //String depPrefix = namespace + "-service-";
        //String depName = namespace + "-" + serviceName.substring(serviceName.indexOf(depPrefix) + depPrefix.length());
        String depName = serviceName;

        // create and insert into db
        ServiceTemplates serviceTemplateDB = new ServiceTemplates();
        serviceTemplateDB.setName(depName);
        JSONArray depParams = JSONArray.fromObject(MsfDataConvert.convertMsfToServiceTemaplate(depName, namespace, msfDep));
        serviceTemplateDB.setDeploymentContent(depParams.toString());
        serviceTemplateDB.setImageList(msfDep.getTemplate().getImage() + "/" + msfDep.getTemplate().getImage());
        serviceTemplateDB.setStatus(Constant.TEMPLATE_STATUS_CREATE);
        serviceTemplateDB.setTenant(tenant);
        serviceTemplateDB.setUser(userName);
        serviceTemplateDB.setCreateTime(new Date());
        serviceTemplateDB.setFlag(Constant.K8S_SERVICE);
        serviceTemplateDB.setTag(String.valueOf(Constant.TEMPLATE_TAG));
        serviceTemplateDB.setPublic(false);
        serviceTemplateDB.setClusterId(clusterId);
        serviceTemplatesMapper.insert(serviceTemplateDB);
        List<ApplicationService> bsList = appWithServiceService.selectAppServiceByAppId(appTmId, serviceTemplateDB.getId());
        if (bsList == null || bsList.size() <= 0) {
            ApplicationService applicationService = new ApplicationService();
            applicationService.setApplicationId(appTmId);
            applicationService.setServiceId(serviceTemplateDB.getId());
            applicationService.setStatus(Constant.TEMPLATE_STATUS_CREATE);
            applicationService.setIsExternal(Constant.K8S_SERVICE);
            appWithServiceService.insert(applicationService);
        }
    }

    /**
     * 分配端口
     *
     * @param deployment
     * @param cluster
     * @return MsfDeployment
     * @throws Exception
     */
    private MsfDeployment assignPort(MsfDeployment deployment, Cluster cluster) throws Exception {
        List<MsfDeploymentPort> msfPorts = deployment.getPorts();
        List<MsfDeploymentPort> newPorts = new ArrayList<>();
        for (MsfDeploymentPort port : msfPorts) {
            String type = port.getExternal_type();
            if (Constant.EXTERNAL_PROTOCOL_TCP.equals(type) || Constant.EXTERNAL_PROTOCOL_UDP.equals(type)) {
                Integer choosePort = routerService.chooseOnePort(cluster);
                NodePortClusterUsage newUsage = new NodePortClusterUsage();
                newUsage.setClusterId(cluster.getId());
                newUsage.setCreateTime(new Date());
                newUsage.setNodeport(choosePort);
                newUsage.setStatus(Constant.EXTERNAL_PORT_STATUS_CONFIRM_USED);
                portUsageService.insertNodeportUsage(newUsage);
                port.setExpose_port(choosePort.toString());
                newPorts.add(port);
            }
            if (Constant.EXTERNAL_PROTOCOL_HTTP.equals(type)) {
                newPorts.add(port);
            }
        }
        deployment.setPorts(newPorts);
        return deployment;
    }

    /**
     * 保存应用模板
     *
     * @param namespaceId
     * @param namespaceName
     * @param tenant
     * @param user
     * @return Integer
     * @throws Exception
     */
    private Integer saveAppTemplate(String namespaceId, String namespaceName, String tenant, String user, String clusterId) throws Exception {
        //查询namespace是否有模板
        ApplicationTemplates applicationTemplates = appTemplateService.selectByNamespaceId(namespaceId);
        boolean btIsExisted = false;
        if (applicationTemplates != null) {
            btIsExisted = true;
        } else {
            applicationTemplates = new ApplicationTemplates();
        }
        applicationTemplates.setName(Constant.MSF + namespaceName);
        applicationTemplates.setUser(user);
        applicationTemplates.setTenant(tenant);
        applicationTemplates.setPublic(false);
        applicationTemplates.setNamespaceId(namespaceId);
        applicationTemplates.setStatus(Constant.TEMPLATE_STATUS_CREATE);
        applicationTemplates.setClusterId(clusterId);
        applicationTemplates.setTag(String.valueOf(Constant.TEMPLATE_TAG));
        if (btIsExisted) {
            //更新模板
            applicationTemplates.setUpdateTime(new Date());
            appTemplateService.updateApplicationTemplate(applicationTemplates);
        } else {
            //插入模板
            applicationTemplates.setCreateTime(new Date());
            appTemplateService.saveApplicationTemplates(applicationTemplates);
        }
        return applicationTemplates.getId();
    }

    /**
     * 保存微服务组件信息
     *
     * @param dep
     * @param tenantId
     * @param clusterId
     * @param taskId
     * @param namespaceId
     * @throws Exception
     */
    private void insertMsfInstance(MsfDeployment dep, String tenantId, String clusterId, String taskId, String namespaceId) throws Exception {
        String insId = dep.getMetadata().getInstance_id();
        //判断是否已经存在对应的实例信息,如果有则删除
        MicroServiceInstance instance = msfInstanceService.findByInstanceId(insId);
        if (Objects.nonNull(instance)) {
            msfInstanceService.deleteMicroServiceInstance(insId, namespaceId);
        }
        //将实例信息插入数据库msf_instance表
        MicroServiceInstance msfInstance = new MicroServiceInstance();
        msfInstance.setInstanceId(dep.getMetadata().getInstance_id());
        msfInstance.setTenantId(tenantId);
        msfInstance.setTaskId(taskId);
        msfInstance.setMemory(StringUtils.isBlank(dep.getSpec().getMemory()) ? Constant.SPRINGCLOUD_INSTANCE_MEMORY : dep.getSpec().getMemory());
        msfInstance.setCpu(StringUtils.isBlank(dep.getSpec().getCpu()) ? Constant.SPRINGCLOUD_INSTANCE_CPU : dep.getSpec().getCpu());
        msfInstance.setServiceName(dep.getSpec().getService_name());
        msfInstance.setReplicas(Integer.valueOf(dep.getSpec().getReplicas()));
        msfInstance.setClusterId(clusterId);
        msfInstance.setCreateTime(new Date());
        msfInstance.setDeploymentName(dep.getMetadata().getDeployment_name());
        JSONArray depParams = JSONArray.fromObject(dep);
        msfInstance.setContent(depParams.toString());
        msfInstance.setNamespaceId(namespaceId);
        msfInstanceService.insertMicroServiceInstance(msfInstance);

    }

    /**
     * 在kubernetes中创建微服务实例
     *
     * @param kubeAppFormatList
     * @param namespace
     * @param cluster
     * @return List<String>
     * @throws Exception
     */
    private List<String> deployMsfInstance(List<Map<String, Object>> kubeAppFormatList, NamespaceLocal namespace, Cluster cluster, String consulExposePort) throws Exception {
        List<String> depNames = new ArrayList<>();
        String consulPort = consulExposePort;
        boolean isExistKong = false;
        //判断是否同时有kong和consul,如果是查询consul的端口
        for (Map<String, Object> map : kubeAppFormatList) {
            Deployment kubeDep = (Deployment) map.get("deployment");
            MsfDeployment depContent = (MsfDeployment) map.get("msfContent");
            Map<String, Object> labels = kubeDep.getMetadata().getLabels();
            if (labels != null && labels.get("componentName") != null) {
                String comName = labels.get("componentName").toString();
                if (Constant.SPRINGCLOUD_CONSUL.equals(comName)) {
                    consulPort = StringUtils.isBlank(consulExposePort) ? MsfDataConvert.getConsulPort(depContent) : consulExposePort;
                }
                if ((Constant.SPRINGCLOUD_KONG).equals(comName)) {
                    isExistKong = true;
                }
            }
        }
        if (StringUtils.isBlank(consulPort) && isExistKong) {
            throw new Exception("consul的对外暴露端口为空");
        }
        for (Map<String, Object> map : kubeAppFormatList) {
            Deployment kubeDep = (Deployment) map.get("deployment");
            com.harmonycloud.k8s.bean.Service kubeService = (com.harmonycloud.k8s.bean.Service) map.get("service");
            MsfDeployment depContent = (MsfDeployment) map.get("msfContent");
            depNames.add(kubeDep.getMetadata().getName());

            //判断是否consul和kong
            Map<String, Object> labels = kubeDep.getMetadata().getLabels();
            if (labels != null && labels.get("componentName") != null) {
                String comName = labels.get("componentName").toString();
                if ((Constant.SPRINGCLOUD_KONG).equals(comName)) {
                    //增加环境变量
                    List<Container> containers = kubeDep.getSpec().getTemplate().getSpec().getContainers();
                    Container container = containers.get(0);
                    List<EnvVar> envs = container.getEnv();
                    EnvVar dnsEnv = new EnvVar();
                    dnsEnv.setName(Constant.SPRINGCLOUD_KONG_ENV);
                    dnsEnv.setValue(msfKubeService.generateKongEnvValue(namespace.getNamespaceName()) + ":" + consulPort);
                    envs.add(dnsEnv);
                    List<Container> newContainers = new ArrayList<>();
                    container.setEnv(envs);
                    newContainers.add(container);
                    kubeDep.getSpec().getTemplate().getSpec().setContainers(newContainers);
                }
            }
            //判断namespace是否存在
            if (!checkNamespaceExist(namespace.getNamespaceId())) {
                throw new Exception(MicroServiceCodeMessage.NAMESPACE_NOT_EXIST.getMessage());
            }
            //下载文件创建挂载的configmap
            msfKubeService.createConfigmap(namespace.getNamespaceName(), kubeDep, cluster);

            //更新nginx configmap(创建tcp/udp服务)
            msfKubeService.updateSystemExposeConfigMap(cluster, namespace.getNamespaceName(), kubeService.getMetadata().getName(), depContent, consulExposePort);

            //创建ingress
            msfKubeService.createHttpIngress(depContent, namespace.getNamespaceName(), cluster);

            //创建应用
            msfKubeService.createApp(kubeDep, kubeService, namespace.getNamespaceName(), cluster);

            //为了保证启动正常设置睡眠时间
            //Thread.sleep(SLEEP_TIME);
        }
        return depNames;
    }

    /**
     * 删除微服务实例表内的实例
     *
     * @param ins
     * @param namespaceName
     * @param cluster
     * @return boolean
     * @throws Exception
     */
    private boolean deleteInstance(MicroServiceInstance ins, String namespaceName, Cluster cluster) throws Exception {
        String serviceName = ins.getServiceName();
        String depName = ins.getDeploymentName();

        //删除Deployment,Service,configmap,ingress
        boolean isSuccess = msfKubeService.deleteMsfDeployment(namespaceName, depName, cluster, serviceName, null);
        if (isSuccess) {
            //删除msf_instance表内的记录
            msfInstanceService.deleteMicroServiceInstance(ins.getInstanceId(), ins.getNamespaceId());
            //删除应用和服务模板
            appTemplateService.deleteApplicationTemplate(Constant.MSF + namespaceName);
            appWithServiceService.deleteApplicationService(Constant.MSF + namespaceName);
        } else {
            //更新msf_instance表的实例删除状态
            ins.setStatus(Integer.valueOf(Constant.SPRINGCLOUD_TASK_FAILURE));
            msfInstanceService.updateMicroServiceInstance(ins);
        }
        return isSuccess;
    }

    /**
     * 更新任务表
     *
     * @param taskId
     * @param status
     * @param errMsg
     * @throws Exception
     */
    private void updateTask(String taskId, String status, String errMsg) throws Exception {
        MicroServiceOperationTask microServiceOperationTask = operationTaskService.findByTaskId(taskId);
        microServiceOperationTask.setStatus(Integer.valueOf(status));
        microServiceOperationTask.setUpdateTime(new Date());
        microServiceOperationTask.setErrorMsg(errMsg);
        operationTaskService.updateTask(microServiceOperationTask);
    }

    /**
     * 循环查询组件状态
     *
     * @param depNames
     * @param namespaceName
     * @param cluster
     * @param taskId
     * @throws Exception
     */
    private boolean queryInstanceStatusLoop(List<String> depNames, String namespaceName, Cluster cluster, String taskId) throws Exception {
        //查询全部状态
        int count = 1;
        boolean loop = true;
        while (loop && count < 10) {
            int flag = 0;
            for (String name : depNames) {
                K8SClientResponse depRes = dpService.doSpecifyDeployment(namespaceName, name, null, null, HTTPMethod.GET, cluster);
                if (depRes.getStatus() == Constant.HTTP_404) {
                    continue;
                }
                if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus()) && depRes.getStatus() != Constant.HTTP_404) {
                    UnversionedStatus unversionedStatus = JsonUtil.jsonToPojo(depRes.getBody(), UnversionedStatus.class);
                    throw new Exception(unversionedStatus.getMessage());
                }
                Deployment dep = JsonUtil.jsonToPojo(depRes.getBody(), Deployment.class);
                if (Constant.START == MsfDataConvert.getAppStatus(dep)) {
                    flag++;
                }
            }
            if (flag == depNames.size()) {

                //更新数据库
                updateTask(taskId, Constant.SPRINGCLOUD_TASK_SUCCESS, null);
                loop = false;
            }
            count++;
            Thread.sleep(Constant.THREAD_SLEEP_TIME_10000);
        }
        return loop;
    }

    /**
     * 更新微服务实例表
     *
     * @param dep
     * @param taskId
     * @throws Exception
     */
    private void updateMsfInstance(MsfDeployment dep, String taskId) throws Exception {

        //将实例信息更新数据库msf_instance表
        MicroServiceInstance msfInstance = msfInstanceService.findByInstanceId(dep.getMetadata().getInstance_id());
        msfInstance.setTaskId(taskId);
        msfInstance.setMemory(dep.getSpec().getMemory());
        msfInstance.setCpu(dep.getSpec().getCpu());
        msfInstance.setServiceName(dep.getSpec().getService_name());
        msfInstance.setReplicas(Integer.valueOf(dep.getSpec().getReplicas()));
        msfInstance.setUpdateTime(new Date());
        msfInstance.setDeploymentName(dep.getSpec().getService_name());
        JSONArray depParams = JSONArray.fromObject(dep);
        msfInstance.setContent(depParams.toString());
        msfInstanceService.updateMicroServiceInstance(msfInstance);
    }

    /**
     * 判断是否有应用，如果没有应用则创建应用
     *
     * @param namespace
     * @param cluster
     * @param username
     * @throws Exception
     */
    private void createAppTPR(String namespace, Cluster cluster, String username, String name) throws Exception {
        //判断是否有应用
        K8SClientResponse response = tprApplication.getApplicationByName(namespace, name, null, null, HTTPMethod.GET, cluster);
        if (Constant.HTTP_404 == response.getStatus()) {
            LOGGER.info("创建应用");
            BaseResource base = new BaseResource();
            ObjectMeta mate = new ObjectMeta();
            mate.setNamespace(namespace);
            mate.setName(name);

            Map<String, Object> appLabels = new HashMap<String, Object>();
            appLabels.put(Constant.APP_CREATER_LABEL, username);
            appLabels.put(Constant.TOPO_LABEL_KEY + CommonConstant.LINE + name, namespace);

            //增加label标注微服务应用
            appLabels.put(Constant.NODESELECTOR_LABELS_PRE + "springcloud", "true");
            mate.setLabels(appLabels);

            base.setMetadata(mate);
            ActionReturnUtil res = tprApplication.createApplication(base, cluster);
            if (!res.isSuccess()) {
                throw new Exception("应用创建失败");
            }
        }
    }

    /**
     * 删除所有部署成功的微服务实例
     *
     * @param kubeAppFormatList
     * @param namespaceName
     * @param cluster
     * @param userName
     * @throws Exception
     */
    private void deleteAllMsfInstancesInNamespace(List<Map<String, Object>> kubeAppFormatList, String namespaceName, Cluster cluster, String userName, boolean isDeleteApp, String consulPort) throws Exception {
        //删除应用
        if (isDeleteApp) {
            tprApplication.delApplicationByName(Constant.MSF + namespaceName, namespaceName, cluster);
        }
        for (Map<String, Object> map : kubeAppFormatList) {
            Deployment kubeDep = (Deployment) map.get("deployment");
            com.harmonycloud.k8s.bean.Service kubeService = (com.harmonycloud.k8s.bean.Service) map.get("service");
            MsfDeployment depContent = (MsfDeployment) map.get("msfContent");

            //删除deployment,service,ingress
            deploymentsService.deleteDeployment(kubeDep.getMetadata().getName(), namespaceName, userName, cluster);

            //更新nginx configmap(创建tcp/udp服务)
            if (StringUtils.isBlank(consulPort)) {
                msfKubeService.deleteTcpUdpRule(Constant.PROTOCOL_UDP, cluster, namespaceName, kubeService.getMetadata().getName());
            }
            msfKubeService.deleteTcpUdpRule(Constant.PROTOCOL_TCP, cluster, namespaceName, kubeService.getMetadata().getName());

            //将已分配的udp端口回收
            List<MsfDeploymentPort> portList = depContent.getPorts();
            if (CollectionUtils.isNotEmpty(portList)) {
                for (MsfDeploymentPort port : portList) {
                    if (StringUtils.isNotBlank(port.getExpose_port())) {
                        portUsageService.deleteNodePortUsage(cluster.getId(), Integer.valueOf(port.getExpose_port()));
                    }
                }
            }
        }
        //删除模板
        deleteTemplate(Constant.MSF + namespaceName);
    }

    /**
     * 删除模板
     *
     * @param appTemplateName
     * @throws Exception
     */
    private void deleteTemplate(String appTemplateName) throws Exception {
        //删除模板
        appTemplateService.deleteApplicationTemplate(appTemplateName);
        appWithServiceService.deleteApplicationService(appTemplateName);
    }

    /**
     * 判断分区配额是否满足条件
     * @param msfDeployments
     * @param namespaceName
     * @return
     * @throws Exception
     */
    private ActionReturnUtil checkResource(List<MsfDeployment> msfDeployments, String namespaceName) throws Exception {
        //获取namespace当前配额
        Map<String, String> namespaceQuota = namespaceService.getNamespaceResourceRemainQuota(namespaceName);
        BigDecimal cpuLeft = new BigDecimal(namespaceQuota.get(CommonConstant.CPU));
        BigDecimal memLeft = new BigDecimal(namespaceQuota.get(CommonConstant.MEMORY));
        BigDecimal cpuTotalNeed = new BigDecimal(CommonConstant.ZERONUM);
        BigDecimal memTotalNeed = new BigDecimal(CommonConstant.ZERONUM);
        for (MsfDeployment deployment : msfDeployments) {
            int replicas = Integer.valueOf(deployment.getSpec().getReplicas());
            BigDecimal cpuNeed = new BigDecimal(deployment.getSpec().getCpu());
            cpuTotalNeed = cpuTotalNeed.add(cpuNeed.multiply(new BigDecimal(replicas)));
            memTotalNeed = memTotalNeed .add(new BigDecimal(replicas).multiply(new BigDecimal(deployment.getSpec().getMemory())));
        }
        if (cpuLeft.subtract(cpuTotalNeed).setScale(CommonConstant.NUM_TWO, BigDecimal.ROUND_HALF_UP).doubleValue() < 0) {
            LOGGER.info("cpu不足，{}", cpuLeft.doubleValue());
            return ActionReturnUtil.returnCodeAndMsg(MicroServiceCodeMessage.SPACE_TASK_FAILURE, "空间CPU不足", null);
        }
        if (memLeft.subtract(memTotalNeed).setScale(CommonConstant.NUM_TWO, BigDecimal.ROUND_HALF_UP).doubleValue() < 0) {
            LOGGER.info("内存不足，{}", memLeft.doubleValue());
            return ActionReturnUtil.returnCodeAndMsg(MicroServiceCodeMessage.SPACE_TASK_FAILURE, "空间内存不足", null);
        }
        return ActionReturnUtil.returnSuccess();
    }

    /**
     * 根据ID判断分区是否存在
     * @param namespaceId
     * @return
     * @throws Exception
     */
    private boolean checkNamespaceExist(String namespaceId) throws Exception {
        NamespaceLocal namespaceBean = namespaceLocalService.getNamespaceByNamespaceId(namespaceId);
        return Objects.nonNull(namespaceBean)? true : false;
    }
}
