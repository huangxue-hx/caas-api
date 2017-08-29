package com.harmonycloud.service.tenant.impl;

import static com.harmonycloud.common.enumm.RolebindingsEnum.DEV_RB;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.service.application.BusinessDeployService;
import com.harmonycloud.service.application.BusinessService;
import com.harmonycloud.service.application.ServiceService;
import com.harmonycloud.service.cluster.ClusterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.HarborProjectRoleEnum;
import com.harmonycloud.common.enumm.RolebindingsEnum;
import com.harmonycloud.common.exception.K8sAuthException;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.dao.network.bean.NamespceBindSubnet;
import com.harmonycloud.dao.network.bean.NetworkCalico;
import com.harmonycloud.dao.network.bean.NetworkTopology;
import com.harmonycloud.dao.tenant.TenantBindingMapper;
import com.harmonycloud.dao.tenant.bean.TenantBinding;
import com.harmonycloud.dao.tenant.bean.TenantBindingExample;
import com.harmonycloud.dao.tenant.bean.UserTenant;
import com.harmonycloud.dto.tenant.NamespaceDto;
import com.harmonycloud.dto.tenant.NetworkDto;
import com.harmonycloud.dto.tenant.QuotaDto;
import com.harmonycloud.dto.tenant.SubnetDto;
import com.harmonycloud.dto.tenant.show.NamespaceShowDto;
import com.harmonycloud.dto.tenant.show.QuotaDetailShowDto;
import com.harmonycloud.dto.tenant.show.QuotaShowDto;
import com.harmonycloud.dto.tenant.show.RolebindingShowDto;
import com.harmonycloud.k8s.bean.Deployment;
import com.harmonycloud.k8s.bean.DeploymentList;
import com.harmonycloud.k8s.bean.LabelSelector;
import com.harmonycloud.k8s.bean.Namespace;
import com.harmonycloud.k8s.bean.NamespaceList;
import com.harmonycloud.k8s.bean.ObjectMeta;
import com.harmonycloud.k8s.bean.ObjectReference;
import com.harmonycloud.k8s.bean.ResourceQuota;
import com.harmonycloud.k8s.bean.ResourceQuotaList;
import com.harmonycloud.k8s.bean.ResourceQuotaSpec;
import com.harmonycloud.k8s.bean.ResourceQuotaStatus;
import com.harmonycloud.k8s.bean.RoleBinding;
import com.harmonycloud.k8s.bean.RoleBindingList;
import com.harmonycloud.k8s.bean.Subjects;
import com.harmonycloud.k8s.constant.Constant;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.service.DeploymentService;
import com.harmonycloud.k8s.service.NetworkPolicyService;
import com.harmonycloud.k8s.service.ResourceQuotaService;
import com.harmonycloud.k8s.service.RoleBindingService;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.service.platform.bean.NodeDetailDto;
import com.harmonycloud.service.platform.service.DashboardService;
import com.harmonycloud.service.platform.service.NodeService;
import com.harmonycloud.service.tenant.NamespaceService;
import com.harmonycloud.service.tenant.NetworkService;
import com.harmonycloud.service.tenant.PrivatePartitionService;
import com.harmonycloud.service.tenant.TenantBindingService;
import com.harmonycloud.service.tenant.TenantService;
import com.harmonycloud.service.tenant.UserTenantService;
import com.harmonycloud.service.user.RoleService;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Created by andy on 17-1-20.
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class NamespaceServiceImpl implements NamespaceService {

    @Autowired
    ResourceQuotaService resourceQuotaService;
    @Autowired
    ServiceService serviceService;
    @Autowired
    BusinessDeployService businessDeployService;
    @Autowired
    com.harmonycloud.k8s.service.NamespaceService namespaceService;
    @Autowired
    TenantBindingService tenantBindingService;
    @Autowired
    RoleBindingService roleBindingService;
    @Autowired
    TenantService tenantService;
    @Autowired
    RoleService roleService;
    @Autowired
    private DeploymentService deploymentService;
    @Autowired
    NetworkService networkService;
    @Autowired
    NetworkPolicyService networkPolicyService;
    @Autowired
    UserTenantService userTenantService;
    @Autowired
    TenantBindingMapper tenantBindingMapper;
    @Autowired
    ClusterService clusterService;
    @Autowired
    PrivatePartitionService privatePartitionService;
    @Autowired
    NodeService nodeService;
    @Autowired
    DashboardService dashboardService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String TM = "tm";

    private static final String HARBOR = "harbor";

    private static final String QUOTA = "quota";

    private ActionReturnUtil checkQuota(Cluster cluster, NamespaceDto namespaceDto) throws Exception {
        Map<String, Object> clusterInfo = dashboardService.getInfraInfoWorkNode(cluster);
        String cpu = clusterInfo.get(CommonConstant.CPU).toString();
        String memory = clusterInfo.get(CommonConstant.MEMORY).toString();
        Map quotaByClusterId = tenantService.getTenantQuotaByClusterId(cluster.getId().toString());
        double memoryUse = (double) quotaByClusterId.get(CommonConstant.MEMORY);
        double cpuUse = (double) quotaByClusterId.get(CommonConstant.CPU);
        String memoryUsedType = quotaByClusterId.get(CommonConstant.USEDTYPE).toString();
        switch (memoryUsedType) {
            case CommonConstant.MB :
                memoryUse = memoryUse * 1024;
                break;
            case CommonConstant.GB :
                memoryUse = memoryUse * 1024 * 1024;
                break;
            case CommonConstant.TB :
                memoryUse = memoryUse * 1024 * 1024 * 1024;
                break;
            case CommonConstant.PB :
                memoryUse = memoryUse * 1024 * 1024 * 1024 * 1024;
                break;
        }
        double canCpu = 0.0;
        if (namespaceDto.getLastlastcpu() == null) {
            canCpu = Double.parseDouble(namespaceDto.getQuota().getCpu());
        } else {
            canCpu = Double.parseDouble(namespaceDto.getQuota().getCpu()) - Double.parseDouble(namespaceDto.getLastlastcpu());
        }
        if (cpu != null && canCpu > 0 && (Double.parseDouble(cpu) - canCpu - cpuUse) < 0) {
            return ActionReturnUtil.returnErrorWithMsg("cpu配额超过集群可使用配额,集群可使用配额为:" + (Double.parseDouble(cpu) - cpuUse) + "核");
        }
        NumberFormat nf = NumberFormat.getNumberInstance();
        // 保留两位小数
        nf.setMaximumFractionDigits(2); 
        if (memory != null) {
            double canMemory = 0.0;
            if (namespaceDto.getQuota().getMemory().contains(CommonConstant.MI)) {
                String[] splitMemory = namespaceDto.getQuota().getMemory().split(CommonConstant.MI);
                if (namespaceDto.getLastlastmemory() == null) {
                    canMemory = (Double.parseDouble(splitMemory[0])) * 1024;
                } else {
                    canMemory = (Double.parseDouble(splitMemory[0]) - Double.parseDouble(namespaceDto.getLastlastmemory())) * 1024;
                }
                if (canMemory > 0 && (Double.parseDouble(memory) - canMemory - memoryUse) < 0) {
                    return ActionReturnUtil.returnErrorWithMsg("memory配额超过集群可使用配额,集群可使用配额为:" + nf.format((Double.parseDouble(memory) - memoryUse) / 1024) + "MB");
                }
            } else if (namespaceDto.getQuota().getMemory().contains(CommonConstant.GI)) {
                String[] splitMemory = namespaceDto.getQuota().getMemory().split(CommonConstant.GI);
                if (namespaceDto.getLastlastmemory() == null) {
                    canMemory = (Double.parseDouble(splitMemory[0])) * 1024 * 1024;
                } else {
                    canMemory = (Double.parseDouble(splitMemory[0]) - Double.parseDouble(namespaceDto.getLastlastmemory())) * 1024 * 1024;
                }
                if (canMemory > 0 && (Double.parseDouble(memory) - canMemory - memoryUse) < 0) {
                    return ActionReturnUtil.returnErrorWithMsg("memory配额超过集群可使用配额,集群可使用配额为:" + nf.format((Double.parseDouble(memory) - memoryUse) / (1024 * 1024)) + "GB");
                }
            } else if (namespaceDto.getQuota().getMemory().contains(CommonConstant.TI)) {
                String[] splitMemory = namespaceDto.getQuota().getMemory().split(CommonConstant.TI);
                if (namespaceDto.getLastlastmemory() == null) {
                    canMemory = (Double.parseDouble(splitMemory[0])) * 1024 * 1024 * 1024;
                } else {
                    canMemory = (Double.parseDouble(splitMemory[0]) - Double.parseDouble(namespaceDto.getLastlastmemory())) * 1024 * 1024 * 1024;
                }
                if (canMemory > 0 && (Double.parseDouble(memory) - canMemory - memoryUse) < 0) {
                    return ActionReturnUtil
                            .returnErrorWithMsg("memory配额超过集群可使用配额,集群可使用配额为:" + nf.format((Double.parseDouble(memory) - memoryUse) / (1024 * 1024 * 1024)) + "TB");
                }
            } else if (namespaceDto.getQuota().getMemory().contains(CommonConstant.PI)) {
                String[] splitMemory = namespaceDto.getQuota().getMemory().split(CommonConstant.PI);
                if (namespaceDto.getLastlastmemory() == null) {
                    canMemory = (Double.parseDouble(splitMemory[0])) * 1024 * 1024 * 1024 * 1024;
                } else {
                    canMemory = (Double.parseDouble(splitMemory[0]) - Double.parseDouble(namespaceDto.getLastlastmemory())) * 1024 * 1024 * 1024 * 1024;
                }
                if (canMemory > 0 && (Double.parseDouble(memory) - canMemory - memoryUse) < 0) {
                    return ActionReturnUtil.returnErrorWithMsg(
                            "memory配额超过集群可使用配额,集群可使用配额为:" + nf.format((Double.parseDouble(memory) / 1024 / 1024 / 1024 / 1024 - memoryUse) / (1024 * 1024 * 1024 * 1024)) + "PB");
                }
            }
        }
        return ActionReturnUtil.returnSuccess();
    }
    @Override
    public ActionReturnUtil createNamespace(NamespaceDto namespaceDto) throws Exception {
        // 初始化判断1
        if (StringUtils.isEmpty(namespaceDto.getName()) || StringUtils.isEmpty(namespaceDto.getTenantid())) {
            return ActionReturnUtil.returnErrorWithMsg("分区名字，租户id 不能为空");
        }
        // 初始化判断2
        if (!namespaceDto.isPrivate()
                && (namespaceDto.getQuota() == null || StringUtils.isEmpty(namespaceDto.getQuota().getCpu()) || StringUtils.isEmpty(namespaceDto.getQuota().getMemory()))) {
            return ActionReturnUtil.returnErrorWithMsg("分区的限额cpu，内存不能为空");
        }
        // 初始化判断3
        if (namespaceDto.isPrivate() && StringUtils.isEmpty(namespaceDto.getNodename())) {
            return ActionReturnUtil.returnErrorWithMsg("如果为私有分区，nodename不能为空");
        }
        // 查询分区是否已经存在
        // 查询namespace详情
        Cluster cluster = this.getClusterByTenantid(namespaceDto.getTenantid());
        if (namespaceDto.isPrivate()) {
            Map node = nodeService.getNode(namespaceDto.getNodename(), cluster);
            NodeDetailDto nodeDetail = (NodeDetailDto) node.get(CommonConstant.DATA);
            if (nodeDetail == null) {
                return ActionReturnUtil.returnErrorWithMsg("获取node:" + namespaceDto.getNodename() + "信息错误");
            }
            QuotaDto quota2 = namespaceDto.getQuota();
            quota2.setCpu(nodeDetail.getCpu());
            quota2.setMemory(nodeDetail.getMemory() + CommonConstant.GI);
            namespaceDto.setQuota(quota2);
        }

        K8SClientResponse namespaceResponse = namespaceService.getNamespace(namespaceDto.getName(), null, null, cluster);
        Map<String, Object> convertJsonToMap = JsonUtil.convertJsonToMap(namespaceResponse.getBody());
        String metadata = convertJsonToMap.get(CommonConstant.METADATA).toString();
        if (!CommonConstant.EMPTYMETADATA.equals(metadata)) {
            logger.error("namespace=" + namespaceDto.getName() + " 已经存在");
            return ActionReturnUtil.returnErrorWithMsg("namespace=" + namespaceDto.getName() + " 已经存在");
        }
        // Map<String, Object> clusterInfo =
        // dashboardService.getInfraInfo(cluster);
        if(!namespaceDto.isPrivate()){
            ActionReturnUtil checkQuota = checkQuota(cluster, namespaceDto);
            if ((Boolean) checkQuota.get(CommonConstant.SUCCESS) == false) {
                return checkQuota;
            }
        }
        
        List<NetworkCalico> networkList = networkService.getnetworkbyTenantid(namespaceDto.getTenantid());
        if (networkList.size() != 1) {
            return ActionReturnUtil.returnErrorWithMsg("租户信息有错，请检查");
        }

        // 1创建分区网络
        NetworkCalico networkCalico = networkList.get(0);
        String networkid = networkCalico.getNetworkid();
        String networkname = networkCalico.getNetworkname();
        String subnetname = networkname + "_subnet_" + namespaceDto.getName();
        String subnetId = null;
        try {
            ActionReturnUtil subnetworkCreate = networkService.subnetworkCreate(networkid, subnetname);
            if ((Boolean) subnetworkCreate.get(CommonConstant.SUCCESS) == false) {
                return subnetworkCreate;
            }
            subnetId = ((NamespceBindSubnet) subnetworkCreate.get(CommonConstant.DATA)).getSubnetId();
        } catch (Exception e) {
            if (e instanceof K8sAuthException) {
                throw e;
            }
            logger.error("创建分区网络失败，错误原因：" + e.getMessage());
            return ActionReturnUtil.returnErrorWithMsg("创建分区网络失败，请检查");
        }

        // 向namespaceDto赋值
        NetworkDto network = new NetworkDto();
        network.setNetworkid(networkid);
        network.setName(networkname);
        SubnetDto subnet = new SubnetDto();
        subnet.setSubnetid(subnetId);
        subnet.setSubnetname(subnetname);
        network.setSubnet(subnet);
        namespaceDto.setNetwork(network);
        // 2.创建namespace
        try {
            ActionReturnUtil createNResult = this.create(namespaceDto, cluster);
            if ((Boolean) createNResult.get(CommonConstant.SUCCESS) == false) {
                // 失败回滚
                this.rollbackNetwork(subnetId);
                networkService.subnetworkDelete(subnetId);
                return createNResult;
            }
        } catch (Exception e) {
            // 失败回滚
            this.rollbackNetwork(subnetId);
            if (e instanceof K8sAuthException) {
                throw e;
            }
            logger.error("创建namespace失败，错误原因：" + e.getMessage());
            return ActionReturnUtil.returnErrorWithMsg("创建namespace失败，错误原因：" + e.getMessage());
        }

        // 3.创建resource quota
        try {
            ActionReturnUtil createRResult = this.createQuota(namespaceDto, cluster);
            if ((Boolean) createRResult.get(CommonConstant.SUCCESS) == false) {
                // 失败回滚
                this.rollbackNetworkAndNamespace(subnetId, namespaceDto.getTenantid(), namespaceDto.getName());
                return createRResult;
            }
        } catch (Exception e) {
            // 失败回滚
            this.rollbackNetworkAndNamespace(subnetId, namespaceDto.getTenantid(), namespaceDto.getName());
            if (e instanceof K8sAuthException) {
                throw e;
            }
            logger.error("创建resource quota，错误原因：" + e.getMessage());
            return ActionReturnUtil.returnErrorWithMsg("创建resource quota，请检查");
        }
        // 4.创建network代理
        try {
            ActionReturnUtil createNPResult = this.createNetworkPolicy(namespaceDto.getName(), namespaceDto.getNetwork().getName(), 2, null, null, cluster);
            if ((Boolean) createNPResult.get(CommonConstant.SUCCESS) == false) {
                // 失败回滚
                this.rollbackNetworkAndNamespace(subnetId, namespaceDto.getTenantid(), namespaceDto.getName());
                return createNPResult;
            }
        } catch (Exception e) {
            // 失败回滚
            this.rollbackNetworkAndNamespace(subnetId, namespaceDto.getTenantid(), namespaceDto.getName());
            if (e instanceof K8sAuthException) {
                throw e;
            }
            logger.error("创建network代理失败，错误原因：" + e.getMessage());
            return ActionReturnUtil.returnErrorWithMsg("创建network代理失败，请检查");
        }

        // 5.初始化ＨＡ
        try {
            ActionReturnUtil createHAResult = this.createHA(namespaceDto, cluster);
            if ((Boolean) createHAResult.get(CommonConstant.SUCCESS) == false) {
                // 失败回滚
                this.rollbackNetworkAndNamespace(subnetId, namespaceDto.getTenantid(), namespaceDto.getName());
                return createHAResult;
            }
        } catch (Exception e) {
            // 失败回滚
            this.rollbackNetworkAndNamespace(subnetId, namespaceDto.getTenantid(), namespaceDto.getName());
            if (e instanceof K8sAuthException) {
                throw e;
            }
            logger.error("初始化ＨＡ失败，错误原因：" + e.getMessage());
            return ActionReturnUtil.returnErrorWithMsg("初始化ＨＡ失败，请检查");
        }

        // 6.添加租户绑定信息
        try {
            ActionReturnUtil createTBResult = tenantBindingService.updateTenantBinding(namespaceDto.getTenantid(), namespaceDto.getName(), null, null);
            if ((Boolean) createTBResult.get(CommonConstant.SUCCESS) == false) {
                // 失败回滚
                this.rollbackNetworkAndNamespace(subnetId, namespaceDto.getTenantid(), namespaceDto.getName());
                return createTBResult;
            }
        } catch (Exception e) {
            // 失败回滚
            this.rollbackNetworkAndNamespace(subnetId, namespaceDto.getTenantid(), namespaceDto.getName());
            if (e instanceof K8sAuthException) {
                throw e;
            }
            logger.error("添加租户绑定信息失败，错误原因：" + e.getMessage());
            return ActionReturnUtil.returnErrorWithMsg("添加租户绑定信息失败，请检查");
        }

        // 7.创建rolebindings
        try {
            ActionReturnUtil createRBResult = this.createRolebindings(namespaceDto, cluster);
            if ((Boolean) createRBResult.get(CommonConstant.SUCCESS) == false) {
                // 失败回滚
                this.rollbackNetworkAndNamespace(subnetId, namespaceDto.getTenantid(), namespaceDto.getName());
                return createRBResult;
            }
        } catch (Exception e) {
            // 失败回滚
            this.rollbackNetworkAndNamespace(subnetId, namespaceDto.getTenantid(), namespaceDto.getName());
            if (e instanceof K8sAuthException) {
                throw e;
            }
            logger.error("创建分区下的rolebindings失败，错误原因：" + e.getMessage());
            return ActionReturnUtil.returnErrorWithMsg("创建分区下的rolebindings失败，请检查");
        }

        // 8.给namespace租户下的用户授予权限
        try {
            ActionReturnUtil bindTMResult = this.bindUser(namespaceDto);
            if ((Boolean) bindTMResult.get(CommonConstant.SUCCESS) == false) {
                // 失败回滚
                this.rollbackNetworkAndNamespace(subnetId, namespaceDto.getTenantid(), namespaceDto.getName());
                return bindTMResult;
            }
        } catch (Exception e) {
            // 失败回滚
            this.rollbackNetworkAndNamespace(subnetId, namespaceDto.getTenantid(), namespaceDto.getName());
            if (e instanceof K8sAuthException) {
                throw e;
            }
            logger.error("给namespace租户下的用户授予权限失败，错误原因：" + e.getMessage());
            return ActionReturnUtil.returnErrorWithMsg("给namespace租户下的用户授予权限失败，请检查");
        }

        // 9.绑定子网络
        try {
            ActionReturnUtil bindSubnet = networkService.subnetworkupdatebinding(subnetId, namespaceDto.getName());
            if ((Boolean) bindSubnet.get(CommonConstant.SUCCESS) == false) {
                // 失败回滚
                this.rollbackNetworkAndNamespace(subnetId, namespaceDto.getTenantid(), namespaceDto.getName());
                return bindSubnet;
            }
        } catch (Exception e) {
            // 失败回滚
            this.rollbackNetworkAndNamespace(subnetId, namespaceDto.getTenantid(), namespaceDto.getName());
            if (e instanceof K8sAuthException) {
                throw e;
            }
            logger.error("绑定子网络失败，错误原因：" + e.getMessage());
            return ActionReturnUtil.returnErrorWithMsg("绑定子网络失败，请检查");
        }

        // 10.查看是否存在网络拓扑，如果有则处理拓扑关系
        // networkService.getnetworkbyNetworkid(networkid);
        try {
            ActionReturnUtil topologyResult = this.dealTopology(namespaceDto, cluster);
            if ((Boolean) topologyResult.get(CommonConstant.SUCCESS) == false) {
                // 失败回滚
                this.rollbackNetworkAndNamespace(subnetId, namespaceDto.getTenantid(), namespaceDto.getName());
                return topologyResult;
            }
        } catch (Exception e) {
            // 失败回滚
            this.rollbackNetworkAndNamespace(subnetId, namespaceDto.getTenantid(), namespaceDto.getName());
            if (e instanceof K8sAuthException) {
                throw e;
            }
            logger.error("处理拓扑关系失败，错误原因：" + e.getMessage());
            return ActionReturnUtil.returnErrorWithMsg("处理拓扑关系失败，请检查");
        }
        // 11.如果为私有分区，创建私有分区,如果为共享分区则处理相应状态
        // 获取networkid
        try {
            if (namespaceDto.isPrivate()) {
                ActionReturnUtil privatePartition = this.createPrivatePartition(namespaceDto, cluster);
                if ((Boolean) privatePartition.get(CommonConstant.SUCCESS) == false) {
                    // 失败回滚
                    this.rollbackNetworkAndNamespace(subnetId, namespaceDto.getTenantid(), namespaceDto.getName());
                    return privatePartition;
                }
            } else {
                ActionReturnUtil updateShareNode = updateShareNode(namespaceDto);
                if ((Boolean) updateShareNode.get(CommonConstant.SUCCESS) == false) {
                    // 失败回滚
                    this.rollbackNetworkAndNamespace(subnetId, namespaceDto.getTenantid(), namespaceDto.getName());
                    return updateShareNode;
                }
            }
        } catch (Exception e) {
            // 失败回滚
            this.rollbackNetworkAndNamespace(subnetId, namespaceDto.getTenantid(), namespaceDto.getName());
            if (e instanceof K8sAuthException) {
                throw e;
            }
            logger.error("设置私有分区失败，错误原因：" + e.getMessage());
            return ActionReturnUtil.returnErrorWithMsg("设置私有分区失败，请检查");
        }
        return ActionReturnUtil.returnSuccess();
    }
    private Cluster getClusterByTenantid(String tenantid) throws Exception {
        Cluster cluster = tenantService.getClusterByTenantid(tenantid);
        return cluster;
    }
    private ActionReturnUtil createPrivatePartition(NamespaceDto namespaceDto, Cluster cluster) throws Exception {
        // 更新node节点状态
        Map<String, String> newLabels = new HashMap<String, String>();
        newLabels.put(CommonConstant.HARMONYCLOUD_STATUS, CommonConstant.LABEL_STATUS_D);
        newLabels.put(CommonConstant.HARMONYCLOUD_TENANTNAME_NS, namespaceDto.getName());
        String[] nodes = namespaceDto.getNodename().split(",");
        if (nodes.length <= 0) {
            return ActionReturnUtil.returnErrorWithMsg("nodename 格式错误！");
        }
        for (String nodename : nodes) {
            ActionReturnUtil addNodeLabels = nodeService.addNodeLabels(nodename, newLabels, cluster.getId().toString());
            if ((Boolean) addNodeLabels.get(CommonConstant.SUCCESS) == false) {
                return addNodeLabels;
            }
        }
        // 更新数据库
        ActionReturnUtil privatePartition = privatePartitionService.setPrivatePartition(namespaceDto.getTenantid(), namespaceDto.getName());
        if ((Boolean) privatePartition.get(CommonConstant.SUCCESS) == false) {
            return privatePartition;
        }
        return ActionReturnUtil.returnSuccess();
    }
    public ActionReturnUtil updateShareNode(NamespaceDto namespaceDto) throws Exception {
        // 更新node节点状态
        /*
         * // 如果共享分区，并且选择了node Map<String, String> newLabels = new
         * HashMap<String, String>(); if
         * (!StringUtils.isEmpty(namespaceDto.getNodename())) {
         * newLabels.put(CommonConstant.HARMONYCLOUD_TENANTNAME_NS,
         * namespaceDto.getName()); String[] nodes =
         * namespaceDto.getNodename().split(","); if (nodes.length <= 0) {
         * return ActionReturnUtil.returnErrorWithMsg("nodename 格式错误！"); } for
         * (String nodename : nodes) { ActionReturnUtil addNodeLabels =
         * nodeService.addNodeLabels(nodename, newLabels); if ((Boolean)
         * addNodeLabels.get(CommonConstant.SUCCESS) == false) { return
         * addNodeLabels; } } // 更新数据库 ActionReturnUtil privatePartition =
         * privatePartitionService.setSharePartition(namespaceDto.getTenantid(),
         * namespaceDto.getName(), true); if ((Boolean)
         * privatePartition.get(CommonConstant.SUCCESS) == false) { return
         * privatePartition; } return ActionReturnUtil.returnSuccess(); }
         */
        // 更新数据库
        ActionReturnUtil privatePartition = privatePartitionService.setSharePartition(namespaceDto.getTenantid(), namespaceDto.getName(), false);
        if ((Boolean) privatePartition.get(CommonConstant.SUCCESS) == false) {
            return privatePartition;
        }
        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public ActionReturnUtil updateNamespace(NamespaceDto namespaceDto) throws Exception {
        Cluster cluster = this.getClusterByTenantid(namespaceDto.getTenantid());
        ActionReturnUtil checkQuota = checkQuota(cluster, namespaceDto);
        if ((Boolean) checkQuota.get(CommonConstant.SUCCESS) == false) {
            return checkQuota;
        }
        // 组装quota
        Map<String, Object> bodys = generateQuotaBodys(namespaceDto);
        Map<String, Object> headers = new HashMap<>();
        headers.put(CommonConstant.CONTENT_TYPE, CommonConstant.APPLICATION_JSON);
        K8SClientResponse k8SClientResponse = resourceQuotaService.update(namespaceDto.getName(), namespaceDto.getName() + QUOTA, headers, bodys, HTTPMethod.PUT);
        if (!HttpStatusUtil.isSuccessStatus(k8SClientResponse.getStatus())) {
            logger.error("调用k8s接口更新namespace下quota失败", k8SClientResponse.getBody());
            return ActionReturnUtil.returnErrorWithMsg(k8SClientResponse.getBody());
        }
        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public ActionReturnUtil deleteNamespace(String tenantid, String namespace) throws Exception {

        if (StringUtils.isEmpty(namespace) || namespace.indexOf(CommonConstant.LINE) < 0) {
            return ActionReturnUtil.returnErrorWithMsg("Invalid namespace name");
        }
        String tenantName = namespace.split(CommonConstant.LINE)[0];
        // 检查tenanid和namespace的正确性
        // TenantBinding tenantByTenantid =
        // tenantService.getTenantByTenantid(tenantid);
        // boolean contains =
        // tenantByTenantid.getK8sNamespaces().contains(namespace);
        // if (!contains) {
        // return
        // ActionReturnUtil.returnErrorWithMsg("请传入正确的tenanid与namespace");
        // }
        Cluster cluster = this.getClusterByTenantid(tenantid);
        // 1.查询namespace下面的rolebindings
        // ActionReturnUtil membResult = this.getNamespaceMember(namespace,
        // tenantName);
        // if((Boolean) membResult.get(CommonConstant.SUCCESS) == false){
        // return membResult;
        // }

        // 1.删除分区网络绑定
        ActionReturnUtil deleteNetwork = networkService.subnetRemoveBing(namespace);
        if ((Boolean) deleteNetwork.get(CommonConstant.SUCCESS) == false) {
            return deleteNetwork;
        }

        // 2.租户绑定信息里删除namepsace
        ActionReturnUtil result = tenantBindingService.deleteNamespace(tenantid, namespace);
        if ((Boolean) result.get(CommonConstant.SUCCESS) == false) {
            return result;
        }

        // 3.调用k8s接口删除namespace
        ActionReturnUtil delbResult = this.delNamespace(namespace, cluster);
        if ((Boolean) delbResult.get(CommonConstant.SUCCESS) == false) {
            return delbResult;
        }

        // 4.删除namespace下的所有service
        ActionReturnUtil serviceRes = this.delServiceNamespace(namespace);
        if (!serviceRes.isSuccess()) {
            return serviceRes;
        }
        // 5.删除namespace下的所有business
        ActionReturnUtil businessRes = this.delBusinessNamespace(namespace);
        if (!businessRes.isSuccess()) {
            return businessRes;
        }
        // 6.如果有私有分区，处理其中的关系
        ActionReturnUtil PrivateNamespace = this.delPrivateNamespace(tenantid, namespace);
        if ((Boolean) PrivateNamespace.get(CommonConstant.SUCCESS) == false) {
            return PrivateNamespace;
        }
        // 7.如果为共享分区，处理其中的关系
        ActionReturnUtil ShareNamespace = this.delShareNamespace(tenantid, namespace);
        if ((Boolean) ShareNamespace.get(CommonConstant.SUCCESS) == false) {
            return ShareNamespace;
        }
        return ActionReturnUtil.returnSuccess();
    }

    private ActionReturnUtil delServiceNamespace(String namespace) throws Exception {
        // 处理数据库
        return serviceService.deleteServiceByNamespace(namespace);
    }

    private ActionReturnUtil delBusinessNamespace(String namespace) throws Exception {
        // 处理数据库
        return businessDeployService.deleteBusinessByNamespace(namespace);
    }

    private ActionReturnUtil delShareNamespace(String tenantid, String namespace) throws Exception {
        // 处理数据库

        boolean privatePartition = privatePartitionService.isPrivatePartition(tenantid, namespace);
        try {
            if (privatePartition) {
                privatePartitionService.removePrivatePartition(tenantid, namespace);
            }
        } catch (Exception e) {
            return ActionReturnUtil.returnErrorWithMsg("处理共享分区出错，错误信息：" + e.getMessage());
        }
        return ActionReturnUtil.returnSuccess();
    }

    private ActionReturnUtil delPrivateNamespace(String tenantid, String namespace) throws Exception {
        Cluster cluster = this.getClusterByTenantid(tenantid);
        boolean privatePartition = privatePartitionService.isPrivatePartition(tenantid, namespace);
        if (privatePartition) {
            // 获取当前私有分区node
            List<String> availableNodeList = nodeService.getPrivateNamespaceNodeList(namespace, cluster);
            if (availableNodeList != null && availableNodeList.isEmpty()) {
                return ActionReturnUtil.returnErrorWithMsg("警告：已经删除当前分区，但是当前分区为私有分区，所属独占节点属性被修改，导致独占节点不存在，请不要随意更改节点私有属性，以免发生冲突，如果在接下来的操作中遇到其它问题，请联系管理员!");
            }
            Map<String, Map<String, String>> oldStatusLabels = new HashMap<String, Map<String, String>>();
            // 更新node节点标签
            for (String nodename : availableNodeList) {
                Map<String, String> nodeStatusLabels = nodeService.listNodeStatusLabels(nodename, cluster);
                oldStatusLabels.put(nodename, nodeStatusLabels);
                Map<String, String> removelabels = new HashMap<String, String>();
                String HarmonyCloud_Status = nodeStatusLabels.get("HarmonyCloud_Status");
                if (org.apache.commons.lang.StringUtils.isBlank(HarmonyCloud_Status)) {
                    return ActionReturnUtil.returnErrorWithMsg("node: " + nodename + "的标签状态有误，没有HarmonyCloud_Status标签");
                }
                if (!HarmonyCloud_Status.equals(CommonConstant.LABEL_STATUS_D)) {
                    return ActionReturnUtil.returnErrorWithMsg("node: " + nodename + "的标签状态有错");
                }
                removelabels.put(CommonConstant.HARMONYCLOUD_TENANTNAME_NS, nodeStatusLabels.get(CommonConstant.HARMONYCLOUD_TENANTNAME_NS));
                nodeStatusLabels.put(CommonConstant.HARMONYCLOUD_STATUS, CommonConstant.LABEL_STATUS_B);
                nodeStatusLabels.remove(CommonConstant.HARMONYCLOUD_TENANTNAME_NS);
                nodeService.addNodeLabels(nodename, nodeStatusLabels, cluster.getId().toString());
                nodeService.removeNodeLabels(nodename, removelabels, cluster);
            }
            // 处理数据库
            try {
                privatePartitionService.removePrivatePartition(tenantid, namespace);
            } catch (Exception e) {
                for (String nodename : availableNodeList) {
                    Map<String, String> map = oldStatusLabels.get(nodename);
                    map.put(CommonConstant.HARMONYCLOUD_STATUS, CommonConstant.LABEL_STATUS_D);
                    nodeService.addNodeLabels(nodename, map, cluster.getId().toString());
                }
                return ActionReturnUtil.returnErrorWithMsg("错误原因:" + e.getMessage());
            }
        }
        return ActionReturnUtil.returnSuccess();
    }

    @SuppressWarnings(CommonConstant.UNCHECKED)
    @Override
    public ActionReturnUtil getNamespaceList(String tenantid, String tenantname) throws Exception {

        // 1.查询租户详情
        ActionReturnUtil tenantBindingResult = tenantService.getSmplTenantDetail(tenantid);
        if ((Boolean) tenantBindingResult.get(CommonConstant.SUCCESS) == false) {
            logger.error("查询租户绑定信息失败,tenantId=" + tenantid);
            return tenantBindingResult;
        }
        Map<String, Object> tenantBinding = new HashMap<>();
        List<Map<String, Object>> tenantData = (List<Map<String, Object>>) tenantBindingResult.get(CommonConstant.DATA);
        if (tenantData != null && !tenantData.isEmpty() && !tenantname.equals(tenantData.get(0).get(CommonConstant.NAME))) {
            logger.error("查询租户绑定信息失败,tenantId=" + tenantid);
            return ActionReturnUtil.returnSuccessWithMsg("租户tenantId=" + tenantid + "与租户名=tenantname" + tenantname + "不一致");
        }
        tenantBinding.putAll(tenantData.get(0));

        // 2.查询租户下namespace列表
        ActionReturnUtil listResult = getSimpleNamespaceListByTenant(tenantid);
        if ((Boolean) listResult.get(CommonConstant.SUCCESS) == false) {
            return listResult;
        }
        NamespaceList list = (NamespaceList) listResult.get(CommonConstant.DATA);
        if (list.getItems() != null && list.getItems().isEmpty()) {
            return ActionReturnUtil.returnSuccessWithData(null);
        }

        List<NamespaceShowDto> namespaces = new LinkedList<>();
        for (Namespace namespace : list.getItems()) {
            NamespaceShowDto namespaceShowDto = new NamespaceShowDto();
            namespaces.add(namespaceShowDto);
            namespaceShowDto.setName(namespace.getMetadata().getName());
            namespaceShowDto.setTime(namespace.getMetadata().getCreationTimestamp());
            if (null != namespace.getMetadata().getAnnotations() && !StringUtils.isEmpty(namespace.getMetadata().getAnnotations().get(CommonConstant.NEPHELE_ANNOTATION))) {
                namespaceShowDto.setAnnotation((String) namespace.getMetadata().getAnnotations().get(CommonConstant.NEPHELE_ANNOTATION));
            }
            namespaceShowDto.setTenant(tenantBinding);
            // 2.1根据namespace查询deployments列表
            ActionReturnUtil dlistResult = getDeploymentByNamespace(namespace.getMetadata().getName());
            if ((Boolean) dlistResult.get(CommonConstant.SUCCESS) == false) {
                return dlistResult;
            }
            DeploymentList dlist = (DeploymentList) dlistResult.get(CommonConstant.DATA);
            this.genrerateService(dlist, namespaceShowDto);

            // 2.2根据namespace查询rolebinding列表
            ActionReturnUtil rlistResult = getRolebindingByNamespace(namespace.getMetadata().getName());
            if ((Boolean) rlistResult.get(CommonConstant.SUCCESS) == false) {
                return rlistResult;
            }
            RoleBindingList rlist = (RoleBindingList) rlistResult.get(CommonConstant.DATA);
            this.genrerateMember(rlist, namespaceShowDto);
        }

        return ActionReturnUtil.returnSuccessWithData(namespaces);
    }

    private void genrerateMember(RoleBindingList rlist, NamespaceShowDto namespaceShowDto) {

        List<RolebindingShowDto> members = new LinkedList<>();
        if (rlist.getItems().size() > 0) {
            for (RoleBinding roleBinding : rlist.getItems()) {

                // 过滤掉harbor与tm的rolebinding
                if (roleBinding.getRoleRef() != null && !StringUtils.isEmpty(roleBinding.getRoleRef().getName())) {
                    String roleName = roleBinding.getRoleRef().getName();

                    if (roleName.startsWith(TM) || roleName.startsWith(HARBOR)) {
                        continue;
                    }
                }

                if (null != roleBinding.getSubjects()) {
                    for (Subjects subjects : roleBinding.getSubjects()) {
                        RolebindingShowDto member = new RolebindingShowDto();
                        member.setName(subjects.getName());
                        member.setRoleBindingName(roleBinding.getMetadata().getName());
                        member.setRole(roleBinding.getRoleRef().getName());
                        member.setNamespace(roleBinding.getMetadata().getNamespace());
                        member.setTime(roleBinding.getMetadata().getCreationTimestamp());
                        members.add(member);
                    }
                }
            }
        }
        namespaceShowDto.setMember(members);
        namespaceShowDto.setMemberNumber(members.size());

    }

    private void genrerateService(DeploymentList dlist, NamespaceShowDto namespaceShowDto) {

        List<String> services = new LinkedList<>();
        if (null != dlist && dlist.getItems().size() > 0) {
            for (Deployment deployment : dlist.getItems()) {
                services.add(deployment.getMetadata().getName());
            }
        }
        namespaceShowDto.setServices(services);
        namespaceShowDto.setServiceNumber(services.size());
    }

    @Override
    public ActionReturnUtil getNamespaceDetail(String name, String tenantid) throws Exception {

        // 获取集群
        TenantBinding ten = tenantService.getTenantByTenantid(tenantid);
        if (ten == null) {
            return ActionReturnUtil.returnErrorWithMsg("tenantid错误");
        }
        List<String> k8sNamespaceList = ten.getK8sNamespaceList();
        if (k8sNamespaceList == null || k8sNamespaceList.size() <= 0) {
            return ActionReturnUtil.returnErrorWithMsg("传入分区名不在传入tenantid的租户下，请检查");
        }
        List<String> otherNamespaceList = new ArrayList<>();
        for (String ns : k8sNamespaceList) {
            if (!name.equals(ns)) {
                otherNamespaceList.add(ns);
            }
        }
        // if(otherNamespaceList==null||otherNamespaceList.size()<=0){
        // otherNamespaceList.add("本租户下暂无其它分区");
        // }
        Cluster cluster = clusterService.findClusterById(ten.getClusterId().toString());
        // 1.查询namespace详情
        // K8SClientResponse namespaceResponse =
        // namespaceService.getNamespace(name, null, null, cluster);
        // if (!HttpStatusUtil.isSuccessStatus(namespaceResponse.getStatus())) {
        // logger.error("调用k8s接口查询namespace详情失败, namespace=" + name,
        // namespaceResponse.getBody());
        // return
        // ActionReturnUtil.returnErrorWithMsg(namespaceResponse.getBody());
        // }
        //
        // Namespace namespace =
        // JsonUtil.jsonToPojo(namespaceResponse.getBody(), Namespace.class);

        // 2.根据namespace名称查询resourceQuota
        // K8SClientResponse quotaResponse =
        // resourceQuotaService.getByNamespace(name, null, null, cluster);
        // if (!HttpStatusUtil.isSuccessStatus(quotaResponse.getStatus())) {
        // logger.error("调用k8s接口查询namespace下quota失败", quotaResponse.getBody());
        // return ActionReturnUtil.returnErrorWithMsg(quotaResponse.getBody());
        // }
        //
        // ResourceQuotaList quotaList =
        // JsonUtil.jsonToPojo(quotaResponse.getBody(),
        // ResourceQuotaList.class);
        //
        // // 3.组装返回数据
        // QuotaShowDto quota = new QuotaShowDto();
        // if (namespace.getMetadata().getAnnotations() != null) {
        // Map<String, Object> annotations =
        // namespace.getMetadata().getAnnotations();
        // quota.setAnnotation((String)
        // annotations.get(CommonConstant.NEPHELE_ANNOTATION));
        // }
        Map<String, Object> namespaceQuota = this.getNamespaceQuota(name, cluster);
        namespaceQuota.put("otherNamespaceList", otherNamespaceList);
        // QuotaDetailShowDto quotaDetailShowDto = new QuotaDetailShowDto();
        // if (quotaList.getItems() != null) {
        // ResourceQuota resourceQuota = quotaList.getItems().get(0);
        // if (resourceQuota.getSpec() != null && resourceQuota.getStatus() !=
        // null) {
        // ResourceQuotaSpec resourceQuotaSpec = resourceQuota.getSpec();
        // ResourceQuotaStatus resourceQuotaStatus = resourceQuota.getStatus();
        // quota.setQuota(generateQuotaDetail(resourceQuotaSpec,
        // resourceQuotaStatus));
        // }
        // }
        //
        // quota.setName(namespace.getMetadata().getName());
        // quota.setTime(namespace.getMetadata().getCreationTimestamp());
        // quota.setTenantName(otherNamespaceList);
        return ActionReturnUtil.returnSuccessWithData(namespaceQuota);
    }

    @SuppressWarnings(CommonConstant.UNCHECKED)
    private QuotaDetailShowDto generateQuotaDetail(ResourceQuotaSpec resourceQuotaSpec, ResourceQuotaStatus resourceQuotaStatus) {
        QuotaDetailShowDto quotaDetailShowDto = new QuotaDetailShowDto();

        Map<String, String> hard = (Map<String, String>) resourceQuotaSpec.getHard();
        Map<String, String> used = (Map<String, String>) resourceQuotaStatus.getUsed();
        List<String> cpu = new LinkedList<>();
        cpu.add(hard.get("cpu"));
        cpu.add(used.get("cpu"));
        quotaDetailShowDto.setCpu(cpu);
        List<String> memory = new LinkedList<>();
        memory.add(hard.get("memory"));
        memory.add(used.get("memory"));
        quotaDetailShowDto.setMemory(memory);

        return quotaDetailShowDto;
    }

    /**
     * 根据namespace名称查询rolebinding列表
     * 
     * @param namespace
     * @return
     */
    public ActionReturnUtil getRolebindingByNamespace(String namespace) throws Exception {

        K8SClientResponse rolebindingResponse = roleBindingService.getRolebindingInNamespacebyLabelSelector(namespace, null);
        if (!HttpStatusUtil.isSuccessStatus(rolebindingResponse.getStatus())) {
            logger.error("调用k8s接口查询namespace下rolebinding列表失败", rolebindingResponse.getBody());
            return ActionReturnUtil.returnErrorWithMsg(rolebindingResponse.getBody());
        }

        RoleBindingList roleBindingList = JsonUtil.jsonToPojo(rolebindingResponse.getBody(), RoleBindingList.class);

        return ActionReturnUtil.returnSuccessWithData(roleBindingList);
    }

    /**
     * 根据namespace名称查询deployment列表
     * 
     * @param namespace
     * @return
     */
    public ActionReturnUtil getDeploymentByNamespace(String namespace) throws Exception {
        K8SClientResponse deploymentResponse = deploymentService.doDeploymentsByNamespace(namespace, null, null, HTTPMethod.GET, null);
        if (!HttpStatusUtil.isSuccessStatus(deploymentResponse.getStatus())) {
            logger.error("调用k8s接口查询namespace下deployment列表失败", deploymentResponse.getBody());
            return ActionReturnUtil.returnErrorWithMsg(deploymentResponse.getBody());
        }

        DeploymentList deploymentList = JsonUtil.jsonToPojo(deploymentResponse.getBody(), DeploymentList.class);

        return ActionReturnUtil.returnSuccessWithData(deploymentList);
    }

    /**
     * 根据租户id称查询租户下namespace列表
     * 
     * @param tenantid
     * @return
     */
    public ActionReturnUtil getSimpleNamespaceListByTenant(String tenantid) throws Exception {
        Map<String, Object> bodys = new HashMap<>();
        bodys.put("labelSelector", "nephele_tenantid=" + tenantid);
        // 获取集群
        Cluster cluster = this.getClusterByTenantid(tenantid);
        K8SClientResponse k8SClientResponse = namespaceService.list(null, bodys, HTTPMethod.GET, cluster);

        if (!HttpStatusUtil.isSuccessStatus(k8SClientResponse.getStatus())) {
            logger.error("调用k8s接口查询租户下namespace列表失败", k8SClientResponse.getBody());
            return ActionReturnUtil.returnErrorWithMsg(k8SClientResponse.getBody());
        }

        NamespaceList list = JsonUtil.jsonToPojo(k8SClientResponse.getBody(), NamespaceList.class);

        return ActionReturnUtil.returnSuccessWithData(list);
    }

    public ActionReturnUtil create(NamespaceDto namespaceDto, Cluster cluster) throws Exception {

        if (StringUtils.isEmpty(namespaceDto.getName()) || namespaceDto.getName().indexOf(CommonConstant.LINE) < 0) {
            return ActionReturnUtil.returnErrorWithMsg("无效的namespace name");
        }

        ObjectMeta objectMeta = new ObjectMeta();
        objectMeta.setAnnotations(this.getAnnotations(namespaceDto));
        objectMeta.setLabels(this.getLables(namespaceDto));
        objectMeta.setName(namespaceDto.getName());
        Map<String, Object> bodys = new HashMap<>();
        bodys.put(CommonConstant.KIND, CommonConstant.NAMESPACE);
        bodys.put(CommonConstant.METADATA, objectMeta);
        Map<String, Object> headers = new HashMap<>();
        headers.put(CommonConstant.CONTENT_TYPE, CommonConstant.APPLICATION_JSON);

        K8SClientResponse k8SClientResponse = namespaceService.create(headers, bodys, HTTPMethod.POST, cluster);

        if (!HttpStatusUtil.isSuccessStatus(k8SClientResponse.getStatus())) {
            logger.error("调用k8s接口创建namespace失败，错误消息：" + k8SClientResponse.getBody());
            return ActionReturnUtil.returnErrorWithMsg(k8SClientResponse.getBody());
        }
        return ActionReturnUtil.returnSuccess();

    }

    private ActionReturnUtil delNamespace(String namespace, Cluster cluster) throws Exception {
        Map<String, Object> bodys = new HashMap<>();
        bodys.put(CommonConstant.GRACEPERIODSECONDS, 3);

        K8SClientResponse k8SClientResponse = namespaceService.delete(null, bodys, HTTPMethod.DELETE, namespace, cluster);

        if (!HttpStatusUtil.isSuccessStatus(k8SClientResponse.getStatus())) {
            logger.error("调用k8s接口创建namespace失败", k8SClientResponse.getBody());
            return ActionReturnUtil.returnErrorWithMsg(k8SClientResponse.getBody());
        }
        return ActionReturnUtil.returnSuccess();
    }

    private ActionReturnUtil getNamespaceMember(String namespace, String tenantName) throws Exception {

        String lable = new StringBuffer().append(CommonConstant.NEPHELE_TENANT).append(tenantName).append(CommonConstant.EQUALITY_SIGN).append(tenantName).toString();
        K8SClientResponse k8SClientResponse = roleBindingService.getRolebindingInNamespacebyLabelSelector(namespace, lable);
        if (!HttpStatusUtil.isSuccessStatus(k8SClientResponse.getStatus())) {
            logger.error("调用k8s接口查询namespace下rolebinding失败", k8SClientResponse.getBody());
            return ActionReturnUtil.returnErrorWithMsg(k8SClientResponse.getBody());
        }
        if (!StringUtils.isEmpty(k8SClientResponse.getBody())) {
            RoleBindingList roleBindingList = JsonUtil.jsonToPojo(k8SClientResponse.getBody(), RoleBindingList.class);
            for (RoleBinding roleBinding : roleBindingList.getItems()) {

                if (DEV_RB.getName().equals(roleBinding.getMetadata().getName()) && roleBinding.getSubjects().size() > 0) {
                    return ActionReturnUtil.returnErrorWithMsg("delete member please before delete project!");
                }
            }
        }
        return ActionReturnUtil.returnSuccess();
    }

    private ActionReturnUtil createQuota(NamespaceDto namespaceDto, Cluster cluster) throws Exception {

        // 组装quota
        Map<String, Object> bodys = generateQuotaBodys(namespaceDto);
        Map<String, Object> headers = new HashMap<>();
        headers.put(CommonConstant.CONTENT_TYPE, CommonConstant.APPLICATION_JSON);

        K8SClientResponse k8SClientResponse = resourceQuotaService.create(namespaceDto.getName(), headers, bodys, HTTPMethod.POST, cluster);
        if (!HttpStatusUtil.isSuccessStatus(k8SClientResponse.getStatus())) {
            logger.error("调用k8s接口创建resourceQuota失败", k8SClientResponse.getBody());
            return ActionReturnUtil.returnErrorWithMsg(k8SClientResponse.getBody());
        }
        return ActionReturnUtil.returnSuccess();
    }

    /**
     * 创建rolebinding
     * 
     * @param namespaceDto
     * @return
     */
    private ActionReturnUtil createRolebindings(NamespaceDto namespaceDto, Cluster cluster) throws Exception {

        String tenantName = namespaceDto.getName().split(CommonConstant.LINE)[0];

        for (RolebindingsEnum rolebindingsEnum : RolebindingsEnum.values()) {

            // 组装objectMeta
            ObjectMeta objectMeta = new ObjectMeta();
            objectMeta.setName(rolebindingsEnum.getName());
            objectMeta.setNamespace(namespaceDto.getName());
            objectMeta.setAnnotations(generateRoleBindingAnnotations(null, null, null));
            objectMeta.setLabels(generateRolebindingLabels(null, tenantName, namespaceDto.getTenantid()));
            // 组装objectReference
            ObjectReference objectReference = new ObjectReference();
            String roleName = rolebindingsEnum.getName().split(CommonConstant.LINE)[0];
            objectReference.setName(roleName);
            objectReference.setKind(CommonConstant.CLUSTERROLE);
            objectReference.setApiVersion(Constant.API_VERSION);

            // 组装rolebinding
            Map<String, Object> bodys = new HashMap<>();
            bodys.put(CommonConstant.KIND, CommonConstant.ROLEBINDING);
            bodys.put(CommonConstant.APIVERSION, Constant.API_VERSION);
            bodys.put(CommonConstant.METADATA, objectMeta);
            bodys.put(CommonConstant.SUBJECTS, generateSubjects(null));
            bodys.put(CommonConstant.ROLEREF, objectReference);
            Map<String, Object> headers = new HashMap<>();
            headers.put(CommonConstant.CONTENT_TYPE, CommonConstant.APPLICATION_JSON);
            // 调用k8s接口生成rolebinding
            K8SClientResponse k8SClientResponse = roleBindingService.create(namespaceDto.getName(), headers, bodys, cluster);
            if (!HttpStatusUtil.isSuccessStatus(k8SClientResponse.getStatus())) {
                logger.error("调用k8s接口创建rolebinding失败", k8SClientResponse.getBody());
                return ActionReturnUtil.returnErrorWithMsg(k8SClientResponse.getBody());
            }
        }
        return ActionReturnUtil.returnSuccess();
    }

    private ActionReturnUtil bindUser(NamespaceDto namespaceDto) throws Exception {

        List<UserTenant> userByTenantid = userTenantService.getUserByTenantid(namespaceDto.getTenantid());
        if (null == userByTenantid || userByTenantid.size() <= 0) {
            logger.error("查询租户绑定信息失败,tenantId=" + namespaceDto.getTenantid());
            return ActionReturnUtil.returnErrorWithMsg("查询租户绑定信息失败,tenantId=" + namespaceDto.getTenantid());
        }
        if (userByTenantid.size() <= 0) {
            return ActionReturnUtil.returnSuccess();
        }
        for (UserTenant userTenant : userByTenantid) {
            TenantBinding tenantBinding = tenantService.getTenantByTenantid(namespaceDto.getTenantid());
            String tenantName = tenantBinding.getTenantName();
            String userName = userTenant.getUsername();
            ActionReturnUtil bindingResult = roleService.rolebinding(tenantName, namespaceDto.getTenantid(), namespaceDto.getName(), "dev", userName);
            if ((Boolean) bindingResult.get(CommonConstant.SUCCESS) == false) {
                logger.error("调用k8s接口向rolebinding绑定租户管理员失败,tenantName=" + tenantName + ",tenantid=" + namespaceDto.getTenantid() + ", namespace=" + namespaceDto.getName()
                        + ", userName=" + userName);
                return bindingResult;
            }

        }
        return ActionReturnUtil.returnSuccess();
    }
    @Override
    public ActionReturnUtil createNetworkPolicy(String namespace, String networkname, Integer type, String networknamefrom, String networknameto, Cluster cluster)
            throws Exception {

        String tenantName = namespace.split(CommonConstant.LINE)[0];

        Map<String, Object> matchLabels = new HashMap<String, Object>();

        Map<String, Object> spec = new HashMap<String, Object>();

        LabelSelector podSelector = new LabelSelector();
        podSelector.setMatchLabels(matchLabels);

        spec.put(CommonConstant.PODSELECTOR, podSelector);
        spec.put(CommonConstant.INGRESS, generateIngress(tenantName, networkname, networknamefrom, networknameto, type));

        ObjectMeta objectMeta = new ObjectMeta();
        String networkpolicyname = null;
        // name+"-"+p.networknamefrom+"-"+p.networknameto+"configpolicy"
        if (StringUtils.isEmpty(networknamefrom) && StringUtils.isEmpty(networknameto)) {
            networkpolicyname = namespace + CommonConstant.POLICY;
            objectMeta.setName(networkpolicyname);
        } else {
            networkpolicyname = namespace + CommonConstant.LINE + networknamefrom.replaceAll(CommonConstant.UNDER_LINE + CommonConstant.MONIT_NETWORK, CommonConstant.EMPTYSTRING)
                    + CommonConstant.LINE + networknameto.replaceAll(CommonConstant.UNDER_LINE + CommonConstant.MONIT_NETWORK, CommonConstant.EMPTYSTRING) + CommonConstant.POLICY;
            objectMeta.setName(networkpolicyname);
        }

        objectMeta.setNamespace(namespace);
        Map<String, Object> bodys = new HashMap<String, Object>();
        bodys.put(CommonConstant.KIND, CommonConstant.NETWORKPOLICY);
        bodys.put(CommonConstant.METADATA, objectMeta);
        bodys.put(CommonConstant.SPEC, spec);
        Map<String, Object> headers = new HashMap<>();
        headers.put(CommonConstant.CONTENT_TYPE, CommonConstant.APPLICATION_JSON);

        K8SClientResponse k8SClientResponse = networkPolicyService.create(headers, bodys, namespace, cluster);

        if (!HttpStatusUtil.isSuccessStatus(k8SClientResponse.getStatus())) {
            logger.error("调用k8s接口创建networkPolicy失败", k8SClientResponse.getBody());
            return ActionReturnUtil.returnErrorWithMsg(k8SClientResponse.getBody());
        }

        return ActionReturnUtil.returnSuccess();
    }

    private ActionReturnUtil createHA(NamespaceDto namespaceDto, Cluster cluster) throws Exception {

        String tenantName = namespaceDto.getName().split(CommonConstant.LINE)[0];

        Map<String, Object> matchLabels = new HashMap<String, Object>();

        Map<String, Object> spec = new HashMap<String, Object>();

        LabelSelector podSelector = new LabelSelector();
        podSelector.setMatchLabels(matchLabels);

        spec.put(CommonConstant.PODSELECTOR, podSelector);
        spec.put(CommonConstant.INGRESS, generateIngress(tenantName, namespaceDto.getNetwork().getName(), null, null, 3));
        /*
         * NetworkPolicySpec spec = new NetworkPolicySpec();
         * List<NetworkPolicyIngressRule> ingress =
         * this.generateIngressRule(tenantName,
         * namespaceDto.getNetwork().getName(), null, null, 3);
         * spec.setIngress(ingress);
         */

        ObjectMeta objectMeta = new ObjectMeta();
        objectMeta.setName(CommonConstant.HAPOLICY + namespaceDto.getName());
        objectMeta.setNamespace(namespaceDto.getName());
        Map<String, Object> bodys = new HashMap<String, Object>();
        bodys.put(CommonConstant.KIND, CommonConstant.NETWORKPOLICY);
        bodys.put(CommonConstant.METADATA, objectMeta);
        bodys.put(CommonConstant.SPEC, spec);
        Map<String, Object> headers = new HashMap<>();
        headers.put(CommonConstant.CONTENT_TYPE, CommonConstant.APPLICATION_JSON);

        K8SClientResponse k8SClientResponse = networkPolicyService.create(headers, bodys, namespaceDto.getName(), cluster);

        if (!HttpStatusUtil.isSuccessStatus(k8SClientResponse.getStatus())) {
            logger.error("调用k8s接口创建networkPolicy失败", k8SClientResponse.getBody());
            return ActionReturnUtil.returnErrorWithMsg(k8SClientResponse.getBody());
        }

        return ActionReturnUtil.returnSuccess();
    }

    private Object generateIngress(String tenantName, String networkName, String networknamefrom, String networknameto, Integer type) throws Exception {

        Map<String, Object> matchLabels = new HashMap<String, Object>();
        switch (type) {
            case 1 :
                String topology_key = new StringBuffer("nephele_topology_").append(networknamefrom).append(CommonConstant.UNDER_LINE).append(networknameto).toString();
                matchLabels.put(topology_key, "1");
                break;
            case 2 :
                matchLabels.put("nephele_tenant", tenantName);
                matchLabels.put(CommonConstant.NEPHELE_TENANT_NETWORK, tenantName + CommonConstant.LINE + networkName);
                break;
            case 3 :
                matchLabels.put(CommonConstant.INITKUBESYSTEM, CommonConstant.KUBE_SYSTEM);
                break;
            default :
                break;
        }

        Map<String, Object> namespaceSelector = new HashMap<String, Object>();
        namespaceSelector.put(CommonConstant.MATCHLABELS, matchLabels);
        Map<String, Object> peer = new HashMap<String, Object>();
        peer.put(CommonConstant.NAMESPACESELECTOR, namespaceSelector);

        List<Map<String, Object>> from = new ArrayList<>();
        from.add(peer);

        Map<String, Object> rule = new HashMap<String, Object>();
        rule.put(CommonConstant.FROM, from);
        List<Map<String, Object>> ingress = new ArrayList<>();
        ingress.add(rule);

        return ingress;
    }

    private Map<String, Object> generateRolebindingLabels(List<String> users, String tenantName, String tenantId) throws Exception {

        Map<String, Object> rolebindingLables = new HashMap<>();

        if (null != users) {
            for (String user : users) {
                rolebindingLables.put("nephele_user_" + user, user);
            }
        }
        rolebindingLables.put("nephele_tenant_" + tenantName, tenantName);
        rolebindingLables.put("nephele_tenantid_" + tenantId, tenantId);

        return rolebindingLables;
    }

    private Map<String, Object> generateRoleBindingAnnotations(String projectId, String role, String userId) throws Exception {

        Map<String, Object> annotations = new HashMap<>();

        // harbor projectId
        if (!StringUtils.isEmpty(projectId)) {
            annotations.put(CommonConstant.PROJECTID, projectId);
        }

        if (!StringUtils.isEmpty(role)) {

            if (role.equals(HarborProjectRoleEnum.DEV.getRole())) {
                annotations.put(CommonConstant.PROJECT, projectId);
                annotations.put(CommonConstant.VERBS, 2);
            }
            if (role.equals(HarborProjectRoleEnum.WATCHER.getRole())) {
                annotations.put(CommonConstant.PROJECT, projectId);
                annotations.put(CommonConstant.VERBS, 3);
            }
        }

        annotations.put(CommonConstant.USERID, userId == null ? "" : userId);
        return annotations;

    }

    private List<Subjects> generateSubjects(List<String> users) throws Exception {
        List<Subjects> subjectsList = new ArrayList<>();

        if (null != users) {
            for (String user : users) {
                Subjects subjects = new Subjects();
                subjects.setKind("User");
                subjects.setName(user);
                subjectsList.add(subjects);
            }
        }

        return subjectsList;
    }

    private Map<String, Object> getLables(NamespaceDto namespaceDto) throws Exception {
        Map<String, Object> lables = new HashMap<>();
        String tenantName = namespaceDto.getName().split(CommonConstant.LINE)[0];
        lables.put("nephele_tenant", tenantName);
        lables.put("nephele_tenantid", namespaceDto.getTenantid());
        lables.put("nephele_tenant_network", new StringBuffer().append(tenantName).append(CommonConstant.LINE).append(namespaceDto.getNetwork().getName()).toString());
        if (namespaceDto.isPrivate()) {
            lables.put("isPrivate", "1");// 私有
        } else {
            lables.put("isPrivate", "0");// 共享
        }
        // lables.put("nephele_tenant_network_subnet", new
        // StringBuffer().append(namespaceDto.getNetwork().getName()).append(CommonConstant.LINE)
        // .append(namespaceDto.getNetwork().getSubnet().getSubnetname()).toString());

        return lables;
    }

    private Map<String, Object> getAnnotations(NamespaceDto namespaceDto) throws Exception {
        Map<String, Object> annotations = new HashMap<>();

        annotations.put(CommonConstant.NEPHELE_SUBNETID, namespaceDto.getNetwork().getSubnet().getSubnetid());
        annotations.put(CommonConstant.NETWORK_POLICY, CommonConstant.NETWORK_POLICY_INGRESS);
        annotations.put(CommonConstant.NEPHELE_SUBNETNAME, namespaceDto.getNetwork().getSubnet().getSubnetname());
        annotations.put(CommonConstant.NEPHELE_ANNOTATION, namespaceDto.getAnnotation());
        annotations.put(CommonConstant.NEPHELE_NETWORKID, namespaceDto.getNetwork().getNetworkid());
        annotations.put(CommonConstant.NEPHELE_NETWORKNAME, namespaceDto.getNetwork().getName());

        return annotations;
    }

    private Map<String, Object> generateQuotaBodys(NamespaceDto namespaceDto) throws Exception {

        ObjectMeta meta = new ObjectMeta();
        meta.setName(namespaceDto.getName() + QUOTA);
        ResourceQuotaSpec spec = new ResourceQuotaSpec();
        Map<String, Object> hard = new HashMap<>();
        // if(namespaceDto.getQuota().getPod()!=null){
        // hard.put("pods", namespaceDto.getQuota().getPod());
        // }
        if (namespaceDto.getQuota().getCpu() != null) {
            hard.put("cpu", namespaceDto.getQuota().getCpu());
        }
        if (namespaceDto.getQuota().getMemory() != null) {
            hard.put("memory", namespaceDto.getQuota().getMemory());
        }
        // if(namespaceDto.getQuota().getConfigmaps()!=null){
        // hard.put("configmaps", namespaceDto.getQuota().getConfigmaps());
        // }
        //
        // hard.put(CommonConstant.PVC, namespaceDto.getQuota().getPvc());
        // hard.put(CommonConstant.RC, namespaceDto.getQuota().getRc());
        // hard.put("resourcequotas",
        // namespaceDto.getQuota().getResourceQuota());
        // hard.put("services", namespaceDto.getQuota().getService());
        // hard.put("secrets", namespaceDto.getQuota().getSecret());
        spec.setHard(hard);

        Map<String, Object> bodys = new HashMap<>();
        Map<String, Object> metadata = new HashMap<>();
        bodys.put(CommonConstant.KIND, CommonConstant.RESOURCEQUOTA);
        metadata.put(CommonConstant.NAME, namespaceDto.getName() + QUOTA);
        bodys.put(CommonConstant.METADATA, metadata);
        bodys.put(CommonConstant.SPEC, spec);

        return bodys;
    }

    private ActionReturnUtil checkSubnet(NamespaceDto namespaceDto) throws Exception {
        SubnetDto subnet = namespaceDto.getNetwork().getSubnet();
        if (null == subnet || StringUtils.isEmpty(subnet.getSubnetid()) || StringUtils.isEmpty(subnet.getSubnetname())) {
            logger.error("验证subnet是否已经被绑定时,Subnetid或者subnetname为空");
            return ActionReturnUtil.returnErrorWithMsg("subnetid or subnetname can not be found");
        }
        NamespceBindSubnet net = networkService.getsubnetbySubnetnameAndSubnetid(subnet.getSubnetid(), subnet.getSubnetname());

        if (null == net) {
            logger.error("验证subnet是否已经被绑定时,subnet未查询到.subnetid=" + subnet.getSubnetid() + "subnetname=" + subnet.getSubnetname());
            return ActionReturnUtil.returnErrorWithMsg("");
        }

        if (net.getBinding() == 1) {
            logger.info("subnet" + subnet.getSubnetname() + "已经被绑定");
            return ActionReturnUtil.returnErrorWithMsg("subent " + subnet.getSubnetname() + " has been bound");
        }
        return ActionReturnUtil.returnSuccess();
    }

    private ActionReturnUtil dealTopology(NamespaceDto namespaceDto, Cluster cluster) throws Exception {

        if (namespaceDto.getNetwork() == null || StringUtils.isEmpty(namespaceDto.getNetwork().getNetworkid())) {
            return ActionReturnUtil.returnErrorWithMsg("创建project时,networkid为空");
        }

        // 根据networkid查询网络拓扑关系
        List<NetworkTopology> networkTopologyList = networkService.getNetworkTopologyList(namespaceDto.getNetwork().getNetworkid());

        String networkName = namespaceDto.getNetwork().getName();

        if (org.apache.commons.lang3.StringUtils.isNotEmpty(networkName) && networkTopologyList.size() > 0) {
            for (NetworkTopology networkTopology : networkTopologyList) {
                String topology = networkTopology.getTopology();
                if (org.apache.commons.lang3.StringUtils.isNotEmpty(topology)) {
                    String[] networkNames = topology.split(CommonConstant.UNDER_LINE);
                    // 该网络为网络拓扑的起点时,更新namespace的lable,添加拓扑关系
                    if (networkName.equals(networkNames[0])) {
                        ActionReturnUtil returnUtil = this.updateNamespaceForTopology(networkNames, namespaceDto.getName(), cluster);
                        if ((Boolean) returnUtil.get(CommonConstant.SUCCESS) == CommonConstant.FALSE) {
                            return returnUtil;
                        }
                        // 该网络为网路拓扑关系的终点时,创建namespace的网路规则
                    } else if (networkName.equals(networkNames[1])) {
                        ActionReturnUtil createNPResult = this.createNetworkPolicy(namespaceDto.getName(), namespaceDto.getNetwork().getName(), 1, networkNames[0], networkNames[1],
                                cluster);
                        if ((Boolean) createNPResult.get(CommonConstant.SUCCESS) == CommonConstant.FALSE) {
                            return createNPResult;
                        }
                    }
                }
            }
        }
        return ActionReturnUtil.returnSuccess();
    }
    @Override
    public ActionReturnUtil updateNamespaceForTopology(String[] networkNames, String name, Cluster cluster) throws Exception {

        // 1.查询namespace
        K8SClientResponse namespaceResponse = namespaceService.getNamespace(name, null, null, cluster);

        if (!HttpStatusUtil.isSuccessStatus(namespaceResponse.getStatus())) {
            logger.error("根据" + name + "查询namespace失败", namespaceResponse.getBody());
            return ActionReturnUtil.returnErrorWithMsg("查询namespace失败");
        }
        Namespace namespace = JsonUtil.jsonToPojo(namespaceResponse.getBody(), Namespace.class);
        // 2.更新namespace
        Map<String, Object> bodys = new HashMap<String, Object>();
        ObjectMeta meta = new ObjectMeta();
        meta.setName(name);
        meta.setAnnotations(namespace.getMetadata().getAnnotations());
        String key = new StringBuffer("nephele_topology_").append(networkNames[0]).append(CommonConstant.UNDER_LINE).append(networkNames[1]).append(CommonConstant.UNDER_LINE).append(networkNames[2]).append(CommonConstant.UNDER_LINE).append(networkNames[3]).toString();
        Map<String, Object> labels = namespace.getMetadata().getLabels();
        // 更新label
        if (labels == null) {
            labels = new HashMap<String, Object>();
        }
        labels.put(key, "1");
        meta.setLabels(labels);
        bodys.put(CommonConstant.KIND, CommonConstant.NAMESPACE);
        bodys.put(CommonConstant.METADATA, meta);
        Map<String, Object> headers = new HashMap<>();
        headers.put(CommonConstant.CONTENT_TYPE, CommonConstant.APPLICATION_JSON);
        K8SClientResponse update = namespaceService.update(headers, bodys, name, cluster);

        if (!HttpStatusUtil.isSuccessStatus(update.getStatus())) {
            logger.error("根据" + name + "更新namespace失败， 错误信息：" + update.getBody());
            return ActionReturnUtil.returnErrorWithMsg("更新namespace失败， 错误信息：" + update.getBody());
        }

        return ActionReturnUtil.returnSuccess();
    }
    public void rollbackNetworkAndNamespace(String subnetId, String tenantid, String namespace) throws Exception {
        // 删除子网
        networkService.subnetworkDelete(subnetId);
        // 删除namespace
        this.deleteNamespace(tenantid, namespace);
    }
    public void rollbackNetwork(String subnetId) throws Exception {
        // 删除子网
        networkService.subnetworkDelete(subnetId);
    }

    @Override
    public ActionReturnUtil removeNamespaceForTopology(String[] networkNames, String name, Cluster cluster) throws Exception {
        // 1.查询namespace
        K8SClientResponse namespaceResponse = namespaceService.getNamespace(name, null, null, cluster);

        if (!HttpStatusUtil.isSuccessStatus(namespaceResponse.getStatus())) {
            logger.error("根据" + name + "查询namespace失败", namespaceResponse.getBody());
            return ActionReturnUtil.returnErrorWithMsg("查询namespace失败");
        }
        Namespace namespace = JsonUtil.jsonToPojo(namespaceResponse.getBody(), Namespace.class);
        // 2.更新namespace
        Map<String, Object> bodys = new HashMap<String, Object>();
        ObjectMeta meta = new ObjectMeta();
        meta.setName(name);
        meta.setAnnotations(namespace.getMetadata().getAnnotations());
        String key = new StringBuffer("nephele_topology_").append(networkNames[0]).append(CommonConstant.UNDER_LINE).append(networkNames[1]).toString();
        Map<String, Object> labels = namespace.getMetadata().getLabels();
        Map<String, Object> newLabels = new HashMap<String, Object>();
        // 更新label
        for (Map.Entry<String, Object> entry : labels.entrySet()) {
            if (!key.equals(entry.getKey())) {
                newLabels.put(entry.getKey(), entry.getValue());
            }
        }
        meta.setLabels(newLabels);
        bodys.put(CommonConstant.KIND, CommonConstant.NAMESPACE);
        bodys.put(CommonConstant.METADATA, meta);
        Map<String, Object> headers = new HashMap<>();
        headers.put(CommonConstant.CONTENT_TYPE, CommonConstant.APPLICATION_JSON);
        K8SClientResponse update = namespaceService.update(headers, bodys, name, cluster);

        if (!HttpStatusUtil.isSuccessStatus(update.getStatus())) {
            logger.error("根据" + name + "更新namespace失败， 错误信息：" + update.getBody());
            return ActionReturnUtil.returnErrorWithMsg("更新namespace失败， 错误信息：" + update.getBody());
        }

        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public ActionReturnUtil removeNetworkPolicy(String namespace, String networknamefrom, String networknameto, Cluster cluster) throws Exception {

        Map<String, Object> headers = new HashMap<>();
        headers.put(CommonConstant.CONTENT_TYPE, CommonConstant.APPLICATION_JSON);
        String networkpolicyname = namespace + CommonConstant.LINE
                + networknamefrom.replaceAll(CommonConstant.UNDER_LINE + CommonConstant.MONIT_NETWORK, CommonConstant.EMPTYSTRING) + CommonConstant.LINE
                + networknameto.replaceAll(CommonConstant.UNDER_LINE + CommonConstant.MONIT_NETWORK, CommonConstant.EMPTYSTRING) + CommonConstant.POLICY;
        K8SClientResponse k8SClientResponse = networkPolicyService.delete(headers, null, namespace, networkpolicyname, cluster);

        if (!HttpStatusUtil.isSuccessStatus(k8SClientResponse.getStatus())) {
            logger.error("调用k8s接口删除networkPolicy失败", k8SClientResponse.getBody());
            return ActionReturnUtil.returnErrorWithMsg(k8SClientResponse.getBody());
        }
        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public ResourceQuotaList getResouceQuota(String namespace, Cluster cluster) throws Exception {

        // 1初始化判断
        if (StringUtils.isEmpty(namespace)) {
            return null;
        }
        // 2.根据namespace名称查询resourceQuota
        K8SClientResponse quotaResponse = resourceQuotaService.getByNamespace(namespace, null, null, cluster);
        if (!HttpStatusUtil.isSuccessStatus(quotaResponse.getStatus())) {
            logger.error("调用k8s接口查询namespace下quota失败", quotaResponse.getBody());
            return null;
        }

        ResourceQuotaList quotaList = JsonUtil.jsonToPojo(quotaResponse.getBody(), ResourceQuotaList.class);
        return quotaList;
    }

    @Override
    public ActionReturnUtil getPrivatePartitionLabel(String tenantid, String namespace) throws Exception {
        ActionReturnUtil privatePartitionLabel = this.privatePartitionService.getPrivatePartitionLabel(tenantid, namespace);
        return privatePartitionLabel;
    }

    @Override
    public ActionReturnUtil getNamespaceListByTenantid(String tenantid) throws Exception {
        // 初始化判断1
        if (StringUtils.isEmpty(tenantid)) {
            return ActionReturnUtil.returnErrorWithMsg("租户id不能为空");
        }
        // 获取集群
        Cluster cluster = this.getClusterByTenantid(tenantid);
        if (cluster == null) {
            return ActionReturnUtil.returnErrorWithMsg("该租户所在的集群无法使用，或者无效的租户id");
        }
        TenantBindingExample example = new TenantBindingExample();
        example.createCriteria().andTenantIdEqualTo(tenantid);
        List<TenantBinding> list = tenantBindingMapper.selectByExample(example);
        if (list == null || list.size() <= 0 || list.get(0) == null) {
            return ActionReturnUtil.returnErrorWithData("tenantid错误");
        }
        TenantBinding tenantBinding = list.get(0);
        List<Object> namespaceData = new ArrayList<Object>();
        List<String> k8sNamespace = tenantBinding.getK8sNamespaceList();
        // 查询namespace信息
        for (String namespace : k8sNamespace) {
            Map<String, Object> namespaceDetail = getNamespaceQuota(namespace, cluster);
            if (namespaceDetail != null) {
                namespaceData.add(namespaceDetail);
            }
        }
        return ActionReturnUtil.returnSuccessWithData(namespaceData);
    }
    public Map<String, Object> getNamespaceQuota(String namespace, Cluster cluster) throws Exception {
        Map<String, Object> namespaceMap = new HashMap<String, Object>();
        K8SClientResponse namespace2 = this.namespaceService.getNamespace(namespace, null, null, cluster);
        Namespace toPojo = JsonUtil.jsonToPojo(namespace2.getBody(), Namespace.class);
        if (!HttpStatusUtil.isSuccessStatus(namespace2.getStatus())) {
            logger.error("调用k8s接口查询namespace失败", namespace2.getBody());
            return ActionReturnUtil.returnErrorWithMsg(namespace2.getBody());
        }
        Object label = toPojo.getMetadata().getLabels().get("isPrivate");
        if (label != null && label.toString().equals("1")) {
            namespaceMap.put("isPrivate", true);
        } else {
            namespaceMap.put("isPrivate", false);
        }
        ResourceQuotaList quotaList = this.getResouceQuota(namespace, cluster);
        namespaceMap.put(CommonConstant.NAME, namespace);
        if (quotaList != null && quotaList.getItems() != null && quotaList.getItems().size() != 0) {
            ResourceQuota resourceQuota = quotaList.getItems().get(0);
            if (resourceQuota.getSpec() != null && resourceQuota.getStatus() != null) {
                ResourceQuotaSpec resourceQuotaSpec = resourceQuota.getSpec();
                ResourceQuotaStatus resourceQuotaStatus = resourceQuota.getStatus();
                Map<String, String> hard = (Map<String, String>) resourceQuotaSpec.getHard();
                Map<String, String> used = (Map<String, String>) resourceQuotaStatus.getUsed();
                List<Object> cpu = new LinkedList<>();
                // 保留两位小数 四舍五入
                NumberFormat nf = NumberFormat.getNumberInstance();
                nf.setMaximumFractionDigits(2);
                nf.setRoundingMode(RoundingMode.UP);
                if (hard.get(CommonConstant.CPU).contains(CommonConstant.SMALLM)) {
                    String chard = hard.get(CommonConstant.CPU).split(CommonConstant.SMALLM)[0];
                    double hardMemory = Double.parseDouble(chard);
                    cpu.add(nf.format(hardMemory / 1000));
                } else {
                    cpu.add(hard.get(CommonConstant.CPU));
                }
                if (used.get(CommonConstant.CPU).contains(CommonConstant.SMALLM)) {
                    String chard = used.get(CommonConstant.CPU).split(CommonConstant.SMALLM)[0];
                    double hardMemory = Double.parseDouble(chard);
                    cpu.add(nf.format(hardMemory / 1000));
                } else {
                    cpu.add(used.get(CommonConstant.CPU));
                }
                // cpu.add(hard.get(CommonConstant.CPU).contains(CommonConstant.SMALLM)?(hard.get(CommonConstant.CPU).split(CommonConstant.SMALLM)[0]+CommonConstant.MB):hard.get(CommonConstant.CPU).split(CommonConstant.GB)[0]+CommonConstant.GB);
                // cpu.add(used.get(CommonConstant.CPU));
                namespaceMap.put(CommonConstant.CPU, cpu);
                List<Object> memory = new LinkedList<>();
                int hardtype = 0;
                int usedtype = 0;
                // 内存总量
                if (hard.get(CommonConstant.MEMORY).contains(CommonConstant.KI) || hard.get(CommonConstant.MEMORY).contains(CommonConstant.SMALLK)) {
                    String mhard = null;
                    if (hard.get(CommonConstant.MEMORY).contains(CommonConstant.SMALLM)) {
                        mhard = hard.get(CommonConstant.MEMORY).split(CommonConstant.SMALLK)[0];
                    } else {
                        mhard = hard.get(CommonConstant.MEMORY).split(CommonConstant.KI)[0];
                    }
                    double hardMemory = Double.parseDouble(mhard);
                    int mum = 0;
                    while (hardMemory >= 1024) {
                        hardMemory = hardMemory / 1024;
                        mum = mum + 1;
                    }
                    hardtype = mum;
                    memory.add(nf.format(hardMemory % 1.0 == 0 ? (long) hardMemory : hardMemory));
                } else if (hard.get(CommonConstant.MEMORY).contains(CommonConstant.SMALLM)) {
                    String mhard = null;
                    mhard = hard.get(CommonConstant.MEMORY).split(CommonConstant.SMALLM)[0];
                    double hardMemory = Double.parseDouble(mhard);
                    int mum = 0;
                    hardMemory = hardMemory / (1000 * 1024);
                    while (hardMemory >= 1024) {
                        hardMemory = hardMemory / 1024;
                        mum = mum + 1;
                    }
                    hardtype = mum;
                    memory.add(nf.format(hardMemory % 1.0 == 0 ? (long) hardMemory : hardMemory));
                } else if (hard.get(CommonConstant.MEMORY).contains(CommonConstant.MI)) {
                    String mhard = null;
                    mhard = hard.get(CommonConstant.MEMORY).split(CommonConstant.MI)[0];
                    double hardMemory = Double.parseDouble(mhard);
                    int mum = 1;
                    while (hardMemory >= 1024) {
                        hardMemory = hardMemory / 1024;
                        mum = mum + 1;
                    }
                    hardtype = mum;
                    memory.add(nf.format(hardMemory % 1.0 == 0 ? (long) hardMemory : hardMemory));
                } else if (hard.get(CommonConstant.MEMORY).contains(CommonConstant.GI) || hard.get(CommonConstant.MEMORY).contains(CommonConstant.SMALLG)) {
                    String mhard = hard.get(CommonConstant.MEMORY).split(CommonConstant.GI)[0];
                    memory.add(mhard);
                    hardtype = 2;
                } else if (hard.get(CommonConstant.MEMORY).contains(CommonConstant.TI) || hard.get(CommonConstant.MEMORY).contains(CommonConstant.SMALLT)) {
                    String mhard = hard.get(CommonConstant.MEMORY).split(CommonConstant.TI)[0];
                    memory.add(mhard);
                    hardtype = 3;
                } else if (hard.get(CommonConstant.MEMORY).contains(CommonConstant.PI)) {
                    String mhard = hard.get(CommonConstant.MEMORY).split(CommonConstant.PI)[0];
                    memory.add(mhard);
                    hardtype = 4;
                }
                // 内存使用量
                if (used.get(CommonConstant.MEMORY).equals(CommonConstant.ZERONUM)) {
                    String mused = used.get(CommonConstant.MEMORY);
                    memory.add(mused);
                    usedtype = hardtype;
                } else if (used.get(CommonConstant.MEMORY).contains(CommonConstant.KI)) {
                    String mused = used.get(CommonConstant.MEMORY).split(CommonConstant.KI)[0];
                    double usedMemory = Double.parseDouble(mused);
                    int mum = 0;
                    while (usedMemory >= 1024) {
                        usedMemory = usedMemory / 1024;
                        mum = mum + 1;
                    }
                    usedtype = mum;
                    memory.add(nf.format(usedMemory % 1.0 == 0 ? (long) usedMemory : usedMemory));
                } else if (used.get(CommonConstant.MEMORY).contains(CommonConstant.MI)) {
                    String mused = used.get(CommonConstant.MEMORY).split(CommonConstant.MI)[0];
                    double usedMemory = Double.parseDouble(mused);
                    int mum = 1;
                    while (usedMemory >= 1024) {
                        usedMemory = usedMemory / 1024;
                        mum = mum + 1;
                    }
                    usedtype = mum;
                    memory.add(nf.format(usedMemory % 1.0 == 0 ? (long) usedMemory : usedMemory));
                } else if (used.get(CommonConstant.MEMORY).contains(CommonConstant.GI)) {
                    String mused = used.get(CommonConstant.MEMORY).split(CommonConstant.GI)[0];
                    memory.add(mused);
                    usedtype = 2;
                } else if (used.get(CommonConstant.MEMORY).contains(CommonConstant.TI)) {
                    String mused = used.get(CommonConstant.MEMORY).split(CommonConstant.TI)[0];
                    memory.add(mused);
                    usedtype = 3;
                } else if (used.get(CommonConstant.MEMORY).contains(CommonConstant.PI)) {
                    String mused = used.get(CommonConstant.MEMORY).split(CommonConstant.PI)[0];
                    memory.add(mused);
                    usedtype = 4;
                }
                // if
                // (hard.get(CommonConstant.MEMORY).contains(CommonConstant.MB))
                // {
                // String mhard =
                // hard.get(CommonConstant.MEMORY).split(CommonConstant.MB)[0];
                // double hardMemory = Double.parseDouble(mhard);
                // String hardmemory = null;
                // if(hardMemory >= 1024){
                // type = 2;
                // double intm = (hardMemory / 1024);
                // while(intm>=1024){
                // intm = intm/1024;
                // type++;
                // }
                // hardmemory = nf.format(intm%1.0==0?(long)intm:intm);
                // }else{
                // hardmemory = mhard;
                // type = 1;
                // }
                // memory.add(hardmemory);
                // } else
                // if(hard.get(CommonConstant.MEMORY).contains(CommonConstant.GB)){
                // double hardMemory =
                // Double.parseDouble(hard.get(CommonConstant.MEMORY).split(CommonConstant.GB)[0]);
                // memory.add(hardMemory%1.0==0?(long)hardMemory:hardMemory);
                // type = 2;
                //
                // }else
                // if(hard.get(CommonConstant.MEMORY).contains(CommonConstant.TB)){
                // double hardMemory =
                // Double.parseDouble(hard.get(CommonConstant.MEMORY).split(CommonConstant.TB)[0]);
                // memory.add(hardMemory%1.0==0?(long)hardMemory:hardMemory);
                // type = 3;
                // }else{
                // //hard.get(CommonConstant.MEMORY)没有单位的时候默认为GB
                // double hardMemory =
                // Double.parseDouble(hard.get(CommonConstant.MEMORY).split(CommonConstant.GB)[0]);
                // memory.add(hardMemory%1.0==0?(long)hardMemory:hardMemory);
                // type = 2;
                // }
                // if
                // (used.get(CommonConstant.MEMORY).contains(CommonConstant.MB))
                // {
                // String mused =
                // used.get(CommonConstant.MEMORY).split(CommonConstant.MB)[0];
                // double usedMemory = Double.parseDouble(mused);
                // String usedmemory = null;
                // double intm = 0;
                // switch (type) {
                // case 1:
                // usedmemory = mused;
                // break;
                // case 2:
                // for(int i = type;i>=0;i++){
                // intm = usedMemory / 1024;
                // }
                // usedmemory = nf.format(intm%1.0==0?(long)intm:intm);
                // break;
                // }
                // memory.add(usedMemory == 0 ? "0":usedmemory);
                // } else
                // if(hard.get(CommonConstant.MEMORY).contains(CommonConstant.GB)){
                // double usedMemory =
                // Double.parseDouble(used.get(CommonConstant.MEMORY).split(CommonConstant.GB)[0]);
                // memory.add(usedMemory%1.0==0?(long)usedMemory:usedMemory);
                // }else
                // if(hard.get(CommonConstant.MEMORY).contains(CommonConstant.TB)){
                // double usedMemory =
                // Double.parseDouble(used.get(CommonConstant.MEMORY).split(CommonConstant.TB)[0]);
                // memory.add(usedMemory%1.0==0?(long)usedMemory:usedMemory);
                // }
                // memory.add(hard.get(CommonConstant.MEMORY).contains(CommonConstant.MB)?(hard.get(CommonConstant.MEMORY).split(CommonConstant.MB)[0]+CommonConstant.MB):hard.get(CommonConstant.MEMORY).split(CommonConstant.GB)[0]+CommonConstant.GB);
                // memory.add(used.get(CommonConstant.MEMORY).contains(CommonConstant.MB)?(used.get(CommonConstant.MEMORY).split(CommonConstant.MB)[0]+CommonConstant.MB):used.get(CommonConstant.MEMORY).split(CommonConstant.GB)[0]+CommonConstant.GB);
                namespaceMap.put(CommonConstant.MEMORY, memory);
                switch (hardtype) {
                    case 0 :
                        namespaceMap.put(CommonConstant.HARDTYPE, CommonConstant.KB);
                        break;
                    case 1 :
                        namespaceMap.put(CommonConstant.HARDTYPE, CommonConstant.MB);
                        break;
                    case 2 :
                        namespaceMap.put(CommonConstant.HARDTYPE, CommonConstant.GB);
                        break;
                    case 3 :
                        namespaceMap.put(CommonConstant.HARDTYPE, CommonConstant.TB);
                        break;
                    case 4 :
                        namespaceMap.put(CommonConstant.HARDTYPE, CommonConstant.PB);
                        break;
                }
                switch (usedtype) {
                    case 1 :
                        namespaceMap.put(CommonConstant.USEDTYPE, CommonConstant.MB);
                        break;
                    case 2 :
                        namespaceMap.put(CommonConstant.USEDTYPE, CommonConstant.GB);
                        break;
                    case 3 :
                        namespaceMap.put(CommonConstant.USEDTYPE, CommonConstant.TB);
                        break;
                    case 4 :
                        namespaceMap.put(CommonConstant.USEDTYPE, CommonConstant.PB);
                        break;
                }
            }
            return namespaceMap;
        }
        return null;
    }
}
