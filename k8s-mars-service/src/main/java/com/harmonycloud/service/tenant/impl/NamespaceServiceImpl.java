package com.harmonycloud.service.tenant.impl;

import com.alibaba.fastjson.JSONObject;
import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.enumm.NodeTypeEnum;
import com.harmonycloud.common.enumm.RolebindingsEnum;
import com.harmonycloud.common.exception.K8sAuthException;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.*;
import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.dao.network.bean.NamespceBindSubnet;
import com.harmonycloud.dao.network.bean.NetworkTopology;
import com.harmonycloud.dao.tenant.bean.NamespaceLocal;
import com.harmonycloud.dao.tenant.bean.TenantBinding;
import com.harmonycloud.dao.tenant.bean.TenantClusterQuota;
import com.harmonycloud.dto.application.StorageClassDto;
import com.harmonycloud.dto.cluster.ErrorNamespaceDto;
import com.harmonycloud.dto.tenant.*;
import com.harmonycloud.dto.tenant.show.NamespaceShowDto;
import com.harmonycloud.dto.tenant.show.QuotaDetailShowDto;
import com.harmonycloud.dto.tenant.show.RolebindingShowDto;
import com.harmonycloud.k8s.bean.*;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.service.DeploymentService;
import com.harmonycloud.k8s.service.NetworkPolicyService;
import com.harmonycloud.k8s.service.ResourceQuotaService;
import com.harmonycloud.k8s.service.RoleBindingService;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.service.application.*;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.integration.MicroServiceService;
import com.harmonycloud.service.istio.IstioCommonService;
import com.harmonycloud.service.platform.bean.NodeDetailDto;
import com.harmonycloud.service.platform.bean.NodeDto;
import com.harmonycloud.service.platform.bean.PodDto;
import com.harmonycloud.service.platform.service.NodeService;
import com.harmonycloud.service.platform.service.PodService;
import com.harmonycloud.service.tenant.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static com.harmonycloud.common.Constant.CommonConstant.STORAGE_HARD_INDEX;
import static com.harmonycloud.common.Constant.CommonConstant.STORAGE_USED_INDEX;
import static com.harmonycloud.common.enumm.RolebindingsEnum.DEV_RB;

/**
 * Created by andy on 17-1-20.
 */
@Service
public class NamespaceServiceImpl implements NamespaceService {

    private Logger LOGGER = LoggerFactory.getLogger(NamespaceLocalService.class);

    @Autowired
    private ResourceQuotaService resourceQuotaService;
    @Autowired
    private ServiceService serviceService;
    @Autowired
    private ApplicationDeployService applicationDeployService;
    @Autowired
    private com.harmonycloud.k8s.service.NamespaceService namespaceService;
    @Autowired
    private RoleBindingService roleBindingService;
    @Autowired
    private TenantService tenantService;
    @Autowired
    private DeploymentService deploymentService;
    @Autowired
    private NetworkService networkService;
    @Autowired
    private NetworkPolicyService networkPolicyService;

    @Autowired
    private ClusterService clusterService;
    @Autowired
    private PrivatePartitionService privatePartitionService;
    @Autowired
    private NodeService nodeService;
    @Autowired
    private NamespaceLocalService namespaceLocalService;
    @Autowired
    private TenantClusterQuotaService tenantClusterQuotaService;
    @Autowired
    private SecretService secretService;
    @Autowired
    private PodService podService;
    @Autowired
    private DaemonSetsService daemonSetsService;

    @Autowired
    private MicroServiceService microServiceService;
    @Autowired
    private StorageClassService storageClassService;
    @Autowired
    private IstioCommonService istioCommonService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String TM = "tm";

    private static final String HARBOR = "harbor";

    private static final String QUOTA = "quota";
    private static final String ISPRIVATE = "isPrivate";
    private static final String TENANTID = "nephele_tenantid";
    private static final String PRIVATENODELIST = "privateNodeList";
    private static final String CREATETIME = "createTime";
    private static final String PHASE = "phase";
    private static final String STORAGE_RESOURCE = "storage";
    private static final String STORAGE_TYPE = "storageType";
    private static final String STORAGECLASSES = "storageclasses";

    //检查配额的有效值
    private void checkQuota(Cluster cluster, NamespaceDto namespaceDto) throws Exception {
        Lock lock = new ReentrantLock();
        lock.lock();
        try {
            //获取集群配额
            List<ClusterQuotaDto> clusterQuotaDtos = this.tenantClusterQuotaService.
                    listClusterQuotaByTenantid(namespaceDto.getTenantId(), cluster.getId());
            if (CollectionUtils.isEmpty(clusterQuotaDtos)) {
                throw new MarsRuntimeException(ErrorCodeMessage.UNKNOWN);
            }
            ClusterQuotaDto clusterQuotaDto = clusterQuotaDtos.get(0);
            if (clusterQuotaDto.getTotalMomry() == 0d || clusterQuotaDto.getTotalCpu() == 0d) {
                throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_RESOURCE_NOT_ZERO);
            }
            double cpuQuota = clusterQuotaDto.getCpuQuota();
            double memoryQuota = clusterQuotaDto.getMemoryQuota();
            String memoryQuotaType = clusterQuotaDto.getMemoryQuotaType();
            double usedCpu = clusterQuotaDto.getUsedCpu();
            double usedMemory = clusterQuotaDto.getUsedMemory();
            String usedMemoryType = clusterQuotaDto.getUsedMemoryType();
            //内存配额转换统一内存为MB
            memoryQuota = this.transformValue(memoryQuota, memoryQuotaType);
            //使用内存转换统一内存为MB
            usedMemory = this.transformValue(usedMemory, usedMemoryType);

            double canCpu = 0.0;
            double usedCpuNs = 0.0;
            double usedCpuQuotaNs = 0.0;
            Map<String, Object> namespaceQuota = null;
            if (!namespaceDto.getUpdate()) {
                //第一次配额
                canCpu = Double.parseDouble(namespaceDto.getQuota().getCpu());
            } else {
                namespaceQuota = getNamespaceQuota(namespaceDto.getName());
                //修改cpu配额
                List<String> cpu = (List<String>) namespaceQuota.get(CommonConstant.CPU);
                namespaceDto.setLastCpu(cpu.get(CommonConstant.NUM_ONE));
                canCpu = Double.parseDouble(namespaceDto.getQuota().getCpu()) - Double.parseDouble(cpu.get(0));
                usedCpuQuotaNs = Double.parseDouble(namespaceDto.getQuota().getCpu());
                usedCpuNs = Double.parseDouble(cpu.get(CommonConstant.NUM_ONE));

            }
            //检查cpu配额
            checkResource(canCpu, cpuQuota - usedCpu, usedCpuNs, usedCpuQuotaNs, CommonConstant.DEFAULT);

            List<String> laseUsedMemory = null;
            String laseUsedMemoryType = null;
            String laseHardMemoryType = null;
            //检查内存配额
            if (memoryQuota != 0) {
                double canMemory = 0d;
                double usedMemoryNs = 0d;
                double usedMemoryQuotaNs = 0d;
                if (namespaceDto.getQuota().getMemory().contains(CommonConstant.MI)) {
                    String[] splitMemory = namespaceDto.getQuota().getMemory().split(CommonConstant.MI);
                    if (!namespaceDto.getUpdate()) {
                        canMemory = (Double.parseDouble(splitMemory[0]));
                    } else {
                        //更新内存配额
                        laseUsedMemory = (List<String>) namespaceQuota.get(CommonConstant.MEMORY);
                        laseUsedMemoryType = (String) namespaceQuota.get(CommonConstant.HARDTYPE);
                        double parseDouble = Double.parseDouble(laseUsedMemory.get(0));
                        double lasetUsedMemory = this.transformValue(parseDouble, laseUsedMemoryType);
//                        usedMemory = lasetUsedMemory;
                        canMemory = (Double.parseDouble(splitMemory[0]) - lasetUsedMemory);
                        usedMemoryQuotaNs = Double.parseDouble(splitMemory[0]);
                        usedMemoryNs = Double.parseDouble(laseUsedMemory.get(CommonConstant.NUM_ONE));
                    }
                    checkResource(canMemory, memoryQuota - usedMemory, usedMemoryNs, usedMemoryQuotaNs, CommonConstant.MI);
                } else if (namespaceDto.getQuota().getMemory().contains(CommonConstant.GI)) {
                    String[] splitMemory = namespaceDto.getQuota().getMemory().split(CommonConstant.GI);
                    if (!namespaceDto.getUpdate()) {
                        canMemory = (Double.parseDouble(splitMemory[0])) * CommonConstant.NUM_SIZE_MEMORY;
                    } else {
                        //更新内存配额
                        laseUsedMemory = (List<String>) namespaceQuota.get(CommonConstant.MEMORY);
                        laseUsedMemoryType = (String) namespaceQuota.get(CommonConstant.HARDTYPE);
                        double parseDouble = Double.parseDouble(laseUsedMemory.get(0));
                        double lasetUsedMemory = this.transformValue(parseDouble, laseUsedMemoryType);
//                        usedMemory = lasetUsedMemory;
                        canMemory = (Double.parseDouble(splitMemory[0]) * CommonConstant.NUM_SIZE_MEMORY - lasetUsedMemory);
                        usedMemoryQuotaNs = Double.parseDouble(splitMemory[0]) * CommonConstant.NUM_SIZE_MEMORY;
                        laseHardMemoryType = (String) namespaceQuota.get(CommonConstant.HARDTYPE);
                        usedMemoryNs = this.transformValue(Double.parseDouble(laseUsedMemory.get(CommonConstant.NUM_ONE)), laseHardMemoryType);
                    }
                    checkResource(canMemory, memoryQuota - usedMemory, usedMemoryNs, usedMemoryQuotaNs, CommonConstant.GI);
                } else if (namespaceDto.getQuota().getMemory().contains(CommonConstant.TI)) {
                    String[] splitMemory = namespaceDto.getQuota().getMemory().split(CommonConstant.TI);
                    if (!namespaceDto.getUpdate()) {
                        canMemory = (Double.parseDouble(splitMemory[0])) *
                                CommonConstant.NUM_SIZE_MEMORY * CommonConstant.NUM_SIZE_MEMORY;
                    } else {
                        //更新内存配额
                        laseUsedMemory = (List<String>) namespaceQuota.get(CommonConstant.MEMORY);
                        laseUsedMemoryType = (String) namespaceQuota.get(CommonConstant.HARDTYPE);
                        double parseDouble = Double.parseDouble(laseUsedMemory.get(0));
                        double lasetUsedMemory = this.transformValue(parseDouble, laseUsedMemoryType);
//                        usedMemory = lasetUsedMemory;
                        canMemory = (Double.parseDouble(splitMemory[0]) *
                                CommonConstant.NUM_SIZE_MEMORY * CommonConstant.NUM_SIZE_MEMORY - lasetUsedMemory);
                        usedMemoryQuotaNs = Double.parseDouble(splitMemory[0]) * CommonConstant.NUM_SIZE_MEMORY * CommonConstant.NUM_SIZE_MEMORY;
                        laseHardMemoryType = (String) namespaceQuota.get(CommonConstant.HARDTYPE);
                        usedMemoryNs = this.transformValue(Double.parseDouble(laseUsedMemory.get(CommonConstant.NUM_ONE)), laseHardMemoryType);
                    }
                    checkResource(canMemory, memoryQuota - usedMemory, usedMemoryNs, usedMemoryQuotaNs, CommonConstant.TI);
                } else if (namespaceDto.getQuota().getMemory().contains(CommonConstant.PI)) {
                    String[] splitMemory = namespaceDto.getQuota().getMemory().split(CommonConstant.PI);
                    if (!namespaceDto.getUpdate()) {
                        canMemory = (Double.parseDouble(splitMemory[0])) * Math.pow(CommonConstant.NUM_SIZE_MEMORY, CommonConstant.NUM_THREE);
                    } else {
                        //更新内存配额
                        laseUsedMemory = (List<String>) namespaceQuota.get(CommonConstant.MEMORY);
                        laseUsedMemoryType = (String) namespaceQuota.get(CommonConstant.HARDTYPE);
                        double parseDouble = Double.parseDouble(laseUsedMemory.get(0));
                        double lasetUsedMemory = this.transformValue(parseDouble, laseUsedMemoryType);
//                        usedMemory = lasetUsedMemory;
                        canMemory = (Double.parseDouble(splitMemory[0]) *
                                Math.pow(CommonConstant.NUM_SIZE_MEMORY, CommonConstant.NUM_THREE) - lasetUsedMemory);
                        usedMemoryQuotaNs = Double.parseDouble(splitMemory[0]) * Math.pow(CommonConstant.NUM_SIZE_MEMORY, CommonConstant.NUM_THREE);
                        laseHardMemoryType = (String) namespaceQuota.get(CommonConstant.HARDTYPE);
                        usedMemoryNs = this.transformValue(Double.parseDouble(laseUsedMemory.get(CommonConstant.NUM_ONE)), laseHardMemoryType);
                    }
                    checkResource(canMemory, memoryQuota - usedMemory, usedMemoryNs, usedMemoryQuotaNs, CommonConstant.TI);
                }
            }
            //检查分区存储配额
            List<StorageClassQuotaDto> storageClassQuotaList = namespaceDto.getStorageClassQuotaList();
            if (storageClassQuotaList != null) {
                //集群存储配额
                List<StorageDto> storageDtoList = clusterQuotaDto.getStorageQuota();
                //集群下各个分区的各个存储的配额和使用量
                List<NamespaceStorageDto> namespaceStorages = this.listNamespaceStorage(namespaceDto.getTenantId(), namespaceDto.getClusterId());
                if (storageDtoList != null) {
                    for (StorageClassQuotaDto storageClassQuotaDto : storageClassQuotaList) {
                        String storageName = storageClassQuotaDto.getName();
                        //计算该存储更新之后所有分区的总和是否超过集群给该租户的配额
                        int namespaceTotalQuota = Integer.parseInt(storageClassQuotaDto.getQuota());
                        for (NamespaceStorageDto namespaceStorageDto : namespaceStorages) {
                            if (storageName.equals(namespaceStorageDto.getStorageClass())
                                    && !namespaceStorageDto.getNamespace().equals(namespaceDto.getName())) {
                                namespaceTotalQuota += namespaceStorageDto.getHard();
                            }
                        }
                        if (namespaceTotalQuota > Integer.parseInt(storageClassQuotaDto.getTotalQuota())) {
                            throw new MarsRuntimeException(ErrorCodeMessage.STORAGE_QUOTA_OVER_FLOOR);
                        }
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }

    private double transformValue(double usedMemory, String type) {
        double value = 0d;
        switch (type) {
            case CommonConstant.MB:
                value = usedMemory;
                break;
            case CommonConstant.GB:
                value = usedMemory * CommonConstant.NUM_SIZE_MEMORY;
                break;
            case CommonConstant.TB:
                value = usedMemory * Math.pow(CommonConstant.NUM_SIZE_MEMORY, CommonConstant.NUM_TWO);
                break;
            case CommonConstant.PB:
                value = usedMemory * Math.pow(CommonConstant.NUM_SIZE_MEMORY, CommonConstant.NUM_THREE);
                break;
            default:
                throw new MarsRuntimeException(ErrorCodeMessage.INVALID_MEMORY_UNIT_TYPE);
        }
        return value;
    }

    //检查资源是否可用和主机资源保持算法一直
    private void checkResource(double can, double resource, double use, double usedQuotaNs, String type) throws Exception {
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setGroupingUsed(false);
        nf.setMaximumFractionDigits(1);
        nf.setRoundingMode(RoundingMode.HALF_UP);
//        double fomatResource = new BigDecimal(resource).setScale(CommonConstant.NUM_ONE, BigDecimal.ROUND_HALF_UP).doubleValue();double pow = Math.pow(2, 10);Math.pow(CommonConstant.NUM_SIZE_MEMORY,CommonConstant.NUM_THREE)
        if (usedQuotaNs < use) {
            throw new MarsRuntimeException(ErrorCodeMessage.RESOURCE_USED_OVER_QUOTA, use + type);
        }
        if ((resource - can) < -0.1) {
            switch (type) {
                case CommonConstant.MI:
                    throw new MarsRuntimeException(ErrorCodeMessage.MEMORY_QUOTA_OVER_FLOOR, nf.format(-(resource - can - use)) + "MB", Boolean.FALSE);
                case CommonConstant.GI:
                    throw new MarsRuntimeException(ErrorCodeMessage.MEMORY_QUOTA_OVER_FLOOR, nf.format(-(resource - can - use) / CommonConstant.NUM_SIZE_MEMORY) + "GB", Boolean.FALSE);
                case CommonConstant.TI:
                    throw new MarsRuntimeException(ErrorCodeMessage.MEMORY_QUOTA_OVER_FLOOR, nf.format(-(resource - can - use) / Math.pow(CommonConstant.NUM_SIZE_MEMORY, CommonConstant.NUM_TWO)) + "TB", Boolean.FALSE);
                case CommonConstant.PI:
                    throw new MarsRuntimeException(ErrorCodeMessage.MEMORY_QUOTA_OVER_FLOOR, nf.format(-(resource - can - use) / Math.pow(CommonConstant.NUM_SIZE_MEMORY, CommonConstant.NUM_THREE)) + "PB", Boolean.FALSE);
                default:
                    throw new MarsRuntimeException(ErrorCodeMessage.CPU_QUOTA_OVER_FLOOR, nf.format(-(resource - can - use)) + "Core", Boolean.FALSE);
            }
        }
    }

    //计算预留资源
    private Map<String, Double> computeRemainResource(List<DaemonSet> daemonSets, List<PodDto> podList) throws Exception {
        double remainCpu = 0;
        double remainMemory = 0;
        Map<String, Double> result = new HashMap<>();
        if (!org.springframework.util.CollectionUtils.isEmpty(daemonSets)) {
            //DaemonSet数量极少循环数量10以内 TODO 未调试
            for (DaemonSet daemonSet : daemonSets) {
                String daemonSetName = daemonSet.getMetadata().getName();
                //判断节点pod上是否包含该daemonset
                long count = podList.stream().filter(podDto -> podDto.getName().contains(daemonSetName)).count();
                if (count > 0) {
                    List<Container> containers = daemonSet.getSpec().getTemplate().getSpec().getContainers();
                    if (!org.springframework.util.CollectionUtils.isEmpty(containers)) {
                        for (Container container : containers) {
                            //确定request的值
                            Object requests = container.getResources().getRequests();
                            if (null == requests) {
                                requests = container.getResources().getLimits();
                            }
                            if (null == requests) {
                                continue;
                            }
                            Map<String, Object> map = (Map<String, Object>) requests;
                            //转换cpu的数值 添加到预留资源中
                            Object cpu = map.get(CommonConstant.CPU);
                            Object memory = map.get(CommonConstant.MEMORY);
                            if (!Objects.isNull(cpu)) {
                                String cpuStr = cpu.toString();
                                remainCpu += cpuStr.contains(CommonConstant.SMALLM) ? Double.valueOf(cpuStr.split(CommonConstant.SMALLM)[0]) / CommonConstant.NUM_THOUSAND : Double.valueOf(cpuStr);
                            }
                            //转换memory的数值 添加到预留资源中
                            if (!Objects.isNull(memory)) {
                                String memoryStr = memory.toString();
                                if (memoryStr.contains(CommonConstant.MI)) {
                                    Double mem = Double.valueOf(memoryStr.split(CommonConstant.MI)[0]);
                                    remainMemory += this.transformMemoryToGb(mem, CommonConstant.MB);
                                } else if (memoryStr.contains(CommonConstant.GI)) {
                                    Double mem = Double.valueOf(memoryStr.split(CommonConstant.GI)[0]);
                                    remainMemory += this.transformMemoryToGb(mem, CommonConstant.GB);
                                } else if (memoryStr.contains(CommonConstant.TI)) {
                                    Double mem = Double.valueOf(memoryStr.split(CommonConstant.TI)[0]);
                                    remainMemory += this.transformMemoryToGb(mem, CommonConstant.TB);
                                }
                            }
                        }
                    }
                }
            }
        }
        result.put(CommonConstant.CPU, remainCpu);
        result.put(CommonConstant.MEMORY, remainMemory);
        return result;
    }

    @Transactional
    @Override
    public ActionReturnUtil createNamespace(NamespaceDto namespaceDto) throws Exception {
        String namespace = namespaceDto.getName();
        String clusterId = namespaceDto.getClusterId();
        // 初始化判断1
        if (StringUtils.isEmpty(namespace)
                || StringUtils.isEmpty(namespaceDto.getTenantId())
                || Objects.isNull(clusterId)) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        // 初始化判断2
        if (!namespaceDto.isPrivate()
                && (namespaceDto.getQuota() == null
                || StringUtils.isEmpty(namespaceDto.getQuota().getCpu())
                || StringUtils.isEmpty(namespaceDto.getQuota().getMemory()))
                ) {
            throw new MarsRuntimeException(ErrorCodeMessage.NAMESPACE_QUOTA_NOT_BLANK);
        }
        // 初始化判断3
        if (namespaceDto.isPrivate() && StringUtils.isEmpty(namespaceDto.getNodeName())) {
            throw new MarsRuntimeException(ErrorCodeMessage.NODENAME_NOT_BLANK);
        }

        // 查询namespace详情
        Cluster cluster = clusterService.findClusterById(namespaceDto.getClusterId());
        //集群有效值查询
        if (Objects.isNull(cluster)) {
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        //分区有效值判断
        NamespaceLocal namespaceByName = this.namespaceLocalService.getNamespaceByName(namespace);

        if (!Objects.isNull(namespaceByName)) {
            throw new MarsRuntimeException(ErrorCodeMessage.NAMESPACE_EXIST, namespace, Boolean.TRUE);
        }
        //分区别名检查
        NamespaceLocal namespaceByAliasName = this.namespaceLocalService
                .getNamespace(namespaceDto.getAliasName(),namespaceDto.getTenantId());
        if (!Objects.isNull(namespaceByAliasName)) {
            throw new MarsRuntimeException(ErrorCodeMessage.NAMESPACE_EXIST, namespaceDto.getAliasName(), Boolean.TRUE);
        }

        if (!namespaceDto.isPrivate()) {
            //检查分区分配的配额是否超出可用值
            this.checkQuota(cluster, namespaceDto);
        }
        //处理独占节点配额，配额如果有小数点，处理配额小数点
        if (namespaceDto.isPrivate()) {
            QuotaDto quota2 = namespaceDto.getQuota();
            String nodename = namespaceDto.getNodeName();
            String[] nodenames = nodename.split(CommonConstant.COMMA);
            Map<String, Object> mapNode = new HashMap<>();
            //一次性分配给分区独占节点数量极少循环数量10以内
            for (String name : nodenames) {
                //去掉前端重复的nodename
                if (Objects.isNull(mapNode.get(name))) {
                    Map node = nodeService.getNodeDetail(name, cluster);
                    //获取当前节点上运行的pod列表
                    List<PodDto> podList = podService.PodList(name, cluster);
                    List<DaemonSet> daemonSets = daemonSetsService.listDaemonSets(cluster);
                    //获取daemonset占用节点资源
                    double remainCpu = 0;
                    double remainMemory = 0;
                    Map<String, Double> remainResource = this.computeRemainResource(daemonSets, podList);
                    remainCpu = remainResource.get(CommonConstant.CPU);
                    remainMemory = remainResource.get(CommonConstant.MEMORY);
                    NodeDetailDto nodeDetail = (NodeDetailDto) node.get(CommonConstant.DATA);
                    if (nodeDetail == null && StringUtils.isEmpty(nodeDetail.getCpu())) {
                        throw new MarsRuntimeException(ErrorCodeMessage.NODE_NOT_EXIST);
                    }
                    //处理空对象
                    if (Objects.isNull(quota2)) {
                        quota2 = new QuotaDto();
                    }
                    //获取
                    Double oldCpuValue = (quota2.getCpu() == null) ? 0d : (quota2.getCpu().contains(CommonConstant.SMALLM) ? (Double.parseDouble(quota2.getCpu().split(CommonConstant.SMALLM)[0]) / 1000) : Double.parseDouble(quota2.getCpu()));
                    quota2.setCpu(oldCpuValue + (nodeDetail.getCpu().contains(CommonConstant.SMALLM) ? (Double.parseDouble(nodeDetail.getCpu().split(CommonConstant.SMALLM)[0]) / 1000) : Double.parseDouble(nodeDetail.getCpu()) - remainCpu) + "");
                    Double oldMemoryValue = quota2.getMemory() == null ? 0d : Double.parseDouble(quota2.getMemory().split(CommonConstant.GI)[0]);
                    quota2.setMemory((Double.parseDouble(nodeDetail.getMemory()) + oldMemoryValue - remainMemory) + CommonConstant.GI);
                    Integer oldGpuValue = (quota2.getGpu() == null) ? 0 : Integer.valueOf(quota2.getGpu());
                    quota2.setGpu(String.valueOf(oldGpuValue + (nodeDetail.getGpu() == null ? 0 : Integer.valueOf(nodeDetail.getGpu()))));
                    mapNode.put(name, name);
                }
            }

            namespaceDto.setQuota(quota2);
        }

        // 2.创建namespace
        this.create(namespaceDto, cluster);

        // 3.创建resource quota
        try {
            ActionReturnUtil createRResult = this.createQuota(namespaceDto, cluster);
            if ((Boolean) createRResult.get(CommonConstant.SUCCESS) == false) {
                // 失败回滚
                this.rollbackNetworkAndNamespace(namespaceDto.getTenantId(), namespaceDto.getName(), clusterId);
                return createRResult;
            }
        } catch (Exception e) {
            // 失败回滚
            this.rollbackNetworkAndNamespace(namespaceDto.getTenantId(), namespaceDto.getName(), clusterId);
            logger.error("创建resource quota，错误原因：" + e.getMessage(), e);
            if (e instanceof K8sAuthException) {
                throw e;
            }
            throw new MarsRuntimeException(ErrorCodeMessage.NAMESPACE_QUOTA_CREATE_ERROR);
        }
//        // 4.创建network代理
//        try {
//            ActionReturnUtil createNPResult = this.createNetworkPolicy(namespaceDto.getName(), null, 2, null, null, cluster);
//            if ((Boolean) createNPResult.get(CommonConstant.SUCCESS) == false) {
//                // 失败回滚
//                this.rollbackNetworkAndNamespace(namespaceDto.getTenantId(), namespaceDto.getName(),clusterId);
//                return createNPResult;
//            }
//        } catch (Exception e) {
//            // 失败回滚
//            this.rollbackNetworkAndNamespace(namespaceDto.getTenantId(), namespaceDto.getName(),clusterId);
//            throw e;
//        }

//        // 5.初始化ＨＡ
//        try {
//            ActionReturnUtil createHAResult = this.createHA(namespaceDto, cluster);
//            if ((Boolean) createHAResult.get(CommonConstant.SUCCESS) == false) {
//                // 失败回滚
//                this.rollbackNetworkAndNamespace( namespaceDto.getTenantId(), namespaceDto.getName(),clusterId);
//                return createHAResult;
//            }
//        } catch (Exception e) {
//            // 失败回滚
//            this.rollbackNetworkAndNamespace( namespaceDto.getTenantId(), namespaceDto.getName(),clusterId);
//            throw e;
//        }
        // 6.创建secret
        try {
            ActionReturnUtil actionReturnUtil = this.secretService.doSecret(namespaceDto.getName(), cluster.getHarborServer().getHarborAdminAccount(), cluster.getHarborServer().getHarborAdminPassword(), cluster);
            if ((Boolean) actionReturnUtil.get(CommonConstant.SUCCESS) == false) {
                // 失败回滚
                this.rollbackNetworkAndNamespace(namespaceDto.getTenantId(), namespaceDto.getName(), clusterId);
                return actionReturnUtil;
            }
        } catch (Exception e) {
            // 失败回滚
            this.rollbackNetworkAndNamespace(namespaceDto.getTenantId(), namespaceDto.getName(), clusterId);
            throw e;
        }
        // 6.绑定子网络
//        try {
//            ActionReturnUtil bindSubnet = networkService.subnetworkupdatebinding(subnetId, namespaceDto.getName());
//            if ((Boolean) bindSubnet.get(CommonConstant.SUCCESS) == false) {
//                // 失败回滚
//                this.rollbackNetworkAndNamespace(subnetId, namespaceDto.getTenantid(), namespaceDto.getName());
//                return bindSubnet;
//            }
//        } catch (Exception e) {
//            // 失败回滚
//            this.rollbackNetworkAndNamespace(subnetId, namespaceDto.getTenantid(), namespaceDto.getName());
//            if (e instanceof K8sAuthException) {
//                throw e;
//            }
//            logger.error("绑定子网络失败，错误原因：" + e.getMessage());
//            return ActionReturnUtil.returnErrorWithMsg("绑定子网络失败，请检查");
//        }
        // 10.查看是否存在网络拓扑，如果有则处理拓扑关系
        // networkService.getnetworkbyNetworkid(networkid);
//        try {
//            ActionReturnUtil topologyResult = this.dealTopology(namespaceDto, cluster);
//            if ((Boolean) topologyResult.get(CommonConstant.SUCCESS) == false) {
//                // 失败回滚
//                this.rollbackNetworkAndNamespace(subnetId, namespaceDto.getTenantid(), namespaceDto.getName());
//                return topologyResult;
//            }
//        } catch (Exception e) {
//            // 失败回滚
//            this.rollbackNetworkAndNamespace(subnetId, namespaceDto.getTenantid(), namespaceDto.getName());
//            if (e instanceof K8sAuthException) {
//                throw e;
//            }
//            logger.error("处理拓扑关系失败，错误原因：" + e.getMessage());
//            return ActionReturnUtil.returnErrorWithMsg("处理拓扑关系失败，请检查");
//        }
        // 11.如果为私有分区，创建私有分区,如果为共享分区则处理相应状态
        // 获取networkid
        try {
            if (namespaceDto.isPrivate()) {
                this.createPrivatePartition(namespaceDto, cluster);
            } else {
                updateShareNode(namespaceDto);
            }
        } catch (Exception e) {
            // 失败回滚
            this.rollbackNetworkAndNamespace(namespaceDto.getTenantId(), namespaceDto.getName(), clusterId);
            throw e;
        }
        return ActionReturnUtil.returnSuccess();
    }

    //创建本地分区
    private void createLocalNamespace(NamespaceDto namespaceDto) throws Exception {
        NamespaceLocal namespaceLocal = new NamespaceLocal();
        //组装分区参数
        namespaceLocal.setNamespaceId(UUIDUtil.get16UUID());
        namespaceLocal.setNamespaceName(namespaceDto.getName());
        namespaceLocal.setClusterId(namespaceDto.getClusterId());
        namespaceLocal.setIsPrivate(namespaceDto.isPrivate());
        namespaceLocal.setTenantId(namespaceDto.getTenantId());
        namespaceLocal.setCreateTime(DateUtil.getCurrentUtcTime());
        namespaceLocal.setAliasName(namespaceDto.getAliasName());
        //创建本地分区
        this.namespaceLocalService.createNamespace(namespaceLocal);
    }

    private void createPrivatePartition(NamespaceDto namespaceDto, Cluster cluster) throws Exception {
        // 更新node节点状态
        Map<String, String> newLabels = new HashMap<String, String>();
        newLabels.put(NodeTypeEnum.PRIVATE.getLabelKey(), NodeTypeEnum.PRIVATE.getLabelValue());
        newLabels.put(CommonConstant.HARMONYCLOUD_TENANTNAME_NS, namespaceDto.getName());
        newLabels.put(CommonConstant.HARMONYCLOUD_TENANT_ID, namespaceDto.getTenantId());
        String[] nodes = namespaceDto.getNodeName().split(CommonConstant.COMMA);
        if (nodes.length <= 0) {
            throw new MarsRuntimeException(ErrorCodeMessage.NODENAME_NOT_BLANK);
        }
        Map<String, Object> mapNode = new HashMap<>();
        for (String nodename : nodes) {
            if (Objects.isNull(mapNode.get(nodename))) {
                ActionReturnUtil addNodeLabels = nodeService.addNodeLabels(nodename, newLabels, cluster.getId());
                if ((Boolean) addNodeLabels.get(CommonConstant.SUCCESS) == false) {
                    throw new MarsRuntimeException(ErrorCodeMessage.NODE_LABEL_CREATE_ERROR);
                }
                mapNode.put(nodename, nodename);
            }
        }
        // 更新数据库
        privatePartitionService.setPrivatePartition(namespaceDto.getTenantId(), namespaceDto.getName());
    }

    public void updateShareNode(NamespaceDto namespaceDto) throws Exception {
        // 更新数据库
        privatePartitionService.setSharePartition(namespaceDto.getTenantId(), namespaceDto.getName(), false);
    }

    @Override
    public ActionReturnUtil updateNamespace(NamespaceDto namespaceDto) throws Exception {
        String namespaceName = namespaceDto.getName();
        //获取集群
        Cluster cluster = this.namespaceLocalService.getClusterByNamespaceName(namespaceName);
        //检查有效性
        NamespaceLocal namespaceByName = this.namespaceLocalService.getNamespaceByName(namespaceName);
        if (Objects.isNull(namespaceByName)) {
            throw new MarsRuntimeException(ErrorCodeMessage.NAMESPACE_NOT_FOUND);
        }
        namespaceDto.setClusterId(namespaceByName.getClusterId());
        if (!Objects.isNull(namespaceDto.getQuota())) {
            //检查配额
            namespaceDto.setUpdate(Boolean.TRUE);
            checkQuota(cluster, namespaceDto);
        }
        String updateAliasName = namespaceDto.getUpdateAliasName();
        if (StringUtils.isNotBlank(updateAliasName)) {
            NamespaceLocal updateNamespace = this.namespaceLocalService
                    .getNamespace(updateAliasName,namespaceDto.getTenantId());
            if (!Objects.isNull(updateNamespace)) {
                throw new MarsRuntimeException(ErrorCodeMessage.NAMESPACE_EXIST, updateAliasName, Boolean.TRUE);
            }
            namespaceByName.setAliasName(updateAliasName);
            this.namespaceLocalService.updateNamespace(namespaceByName);
        }
        if(namespaceDto.isPrivate()){
            ResourceQuotaList resourceQuotaList = this.getResouceQuota(namespaceName, cluster);
            if(resourceQuotaList !=null && !CollectionUtils.isEmpty(resourceQuotaList.getItems())){
                ResourceQuota resourceQuota = resourceQuotaList.getItems().get(0);
                QuotaDto quotaDto = new QuotaDto();
                Map<String, Object> hard = (Map<String, Object>)resourceQuota.getSpec().getHard();
                quotaDto.setCpu((String)hard.get(CommonConstant.CPU));
                quotaDto.setMemory((String)hard.get(CommonConstant.MEMORY));
                quotaDto.setGpu((String)hard.get(CommonConstant.GPU_QUOTA_KEY));
                namespaceDto.setQuota(quotaDto);
            }
        }
        if (namespaceDto.getUpdate() || namespaceDto.getPrivate()) {
            // 组装quota
            Map<String, Object> bodys = generateQuotaBodys(namespaceDto);
            Map<String, Object> headers = new HashMap<>();
            headers.put(CommonConstant.CONTENT_TYPE, CommonConstant.APPLICATION_JSON);
            //向K8S发送请求更新配额
            K8SClientResponse k8SClientResponse = resourceQuotaService.update(namespaceName, namespaceName + QUOTA, headers, bodys, HTTPMethod.PUT, cluster);
            if (!HttpStatusUtil.isSuccessStatus(k8SClientResponse.getStatus())) {
                logger.error("调用k8s接口更新namespace下quota失败", k8SClientResponse.getBody());
                return ActionReturnUtil.returnErrorWithMsg(k8SClientResponse.getBody());
            }
        }
        return ActionReturnUtil.returnSuccess();
    }

    @Transactional
    @Override
    public ActionReturnUtil deleteNamespace(String tenantid, String namespace) throws Exception {

        if (StringUtils.isEmpty(namespace) || namespace.indexOf(CommonConstant.LINE) < 0) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.FORMAT_ERROR, DictEnum.NAMESPACE.phrase(), true);
        }

        Cluster cluster = this.namespaceLocalService.getClusterByNamespaceName(namespace);
        NamespaceLocal namespaceLocal = namespaceLocalService.getNamespaceByTenantIdAndName(tenantid, namespace);
//        // 1.删除分区网络绑定
//        ActionReturnUtil deleteNetwork = networkService.subnetRemoveBing(namespace);
//        if ((Boolean) deleteNetwork.get(CommonConstant.SUCCESS) == false) {
//            return deleteNetwork;
//        }

        // 1. 删除Istio分区
        boolean isIstioNamespace = istioCommonService.isIstioEnabled(namespace);
        if (isIstioNamespace) {
            //delete istio policy resource
            istioCommonService.deleteIstioPolicy(namespace, null, cluster.getId());
        }

        // 2.如果有私有分区，处理其中的关系
        ActionReturnUtil PrivateNamespace = this.delPrivateNamespace(tenantid, namespace, cluster.getId());
        if (!(Boolean) PrivateNamespace.get(CommonConstant.SUCCESS)) {
            return PrivateNamespace;
        }

        // 3.如果为共享分区，处理其中的关系
        ActionReturnUtil ShareNamespace = this.delShareNamespace(tenantid, namespace);
        if (!(Boolean) ShareNamespace.get(CommonConstant.SUCCESS)) {
            return ShareNamespace;
        }

        // 4. 删除SpringCloud微服务任务和实例
        ActionReturnUtil deleteMsfTaskAndIns = microServiceService.deleteTaskAndInstance(namespaceLocal.getNamespaceId());
        if (!(Boolean) deleteMsfTaskAndIns.get(CommonConstant.SUCCESS)) {
            return deleteMsfTaskAndIns;
        }

        // 5.调用k8s接口删除namespace
        ActionReturnUtil delbResult = this.delNamespace(namespace, cluster);
        if (!(Boolean) delbResult.get(CommonConstant.SUCCESS)) {
            return delbResult;
        }

        // 6. 删除数据库分区namepsace
        this.namespaceLocalService.deleteNamespace(namespaceLocal);

        return ActionReturnUtil.returnSuccess();
    }

    // 删除分区（给创建分区失败回滚用）
    private ActionReturnUtil deleteNamespace(String tenantid, String namespace, String clusterId) throws Exception {

        if (StringUtils.isEmpty(namespace) || namespace.indexOf(CommonConstant.LINE) < 0) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.FORMAT_ERROR, DictEnum.NAMESPACE.phrase(), true);
        }
        Cluster cluster = this.clusterService.findClusterById(clusterId);

        // 1.调用k8s接口删除namespace
        ActionReturnUtil delbResult = this.delNamespace(namespace, cluster);
        if ((Boolean) delbResult.get(CommonConstant.SUCCESS) == false) {
            return delbResult;
        }
        // 2.如果有私有分区，处理其中的关系
        ActionReturnUtil PrivateNamespace = this.delPrivateNamespace(tenantid, namespace, clusterId);
        if ((Boolean) PrivateNamespace.get(CommonConstant.SUCCESS) == false) {
            return PrivateNamespace;
        }
        // 3.如果为共享分区，处理其中的关系
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

    private ActionReturnUtil delApplicationNamespace(String namespace) throws Exception {
        // 处理数据库
        return applicationDeployService.deleteApplicationByNamespace(namespace);
    }

    private ActionReturnUtil delShareNamespace(String tenantid, String namespace) throws Exception {
        // 处理数据库

        boolean privatePartition = privatePartitionService.isPrivatePartition(tenantid, namespace);
        try {
            if (!privatePartition) {
                privatePartitionService.removePrivatePartition(tenantid, namespace);
            }
        } catch (Exception e) {
            LOGGER.error("处理共享分区出错，", e);
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.UNKNOWN);
        }
        return ActionReturnUtil.returnSuccess();
    }

    private ActionReturnUtil delPrivateNamespace(String tenantid, String namespace, String clusterId) throws Exception {
        Cluster cluster = this.clusterService.findClusterById(clusterId);
        boolean privatePartition = privatePartitionService.isPrivatePartition(tenantid, namespace);
        TenantBinding tenant = this.tenantService.getTenantByTenantid(tenantid);
        if (privatePartition) {
            // 获取当前私有分区node
            List<String> availableNodeList = nodeService.getPrivateNamespaceNodeList(namespace, cluster);
            if (CollectionUtils.isEmpty(availableNodeList)) {
                return ActionReturnUtil.returnSuccess();
            }
            Map<String, Map<String, String>> oldStatusLabels = new HashMap<String, Map<String, String>>();
            // 更新node节点标签
            for (String nodename : availableNodeList) {
                Map<String, String> nodeStatusLabels = nodeService.listNodeStatusLabels(nodename, cluster);
                oldStatusLabels.put(nodename, nodeStatusLabels);
                String privateNodeLabelValue = nodeStatusLabels.get(NodeTypeEnum.PRIVATE.getLabelKey());
                if (StringUtils.isBlank(privateNodeLabelValue)
                        || !privateNodeLabelValue.equals(NodeTypeEnum.PRIVATE.getLabelValue())) {
                    return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.NODE_LABEL_ERROR);
                }
//                removelabels.put(CommonConstant.HARMONYCLOUD_TENANTNAME_NS, nodeStatusLabels.get(CommonConstant.HARMONYCLOUD_TENANTNAME_NS));
                nodeStatusLabels.put(NodeTypeEnum.PRIVATE.getLabelKey(), NodeTypeEnum.PRIVATE.getLabelValue());
                nodeStatusLabels.put(CommonConstant.HARMONYCLOUD_TENANTNAME_NS, tenant.getTenantName());
                nodeStatusLabels.put(CommonConstant.HARMONYCLOUD_TENANT_ID, tenant.getTenantId());
                nodeService.addNodeLabels(nodename, nodeStatusLabels, cluster.getId());
//                nodeService.removeNodeLabels(nodename, removelabels, cluster);
            }
            // 处理数据库
            try {
                privatePartitionService.removePrivatePartition(tenantid, namespace);
            } catch (Exception e) {
                for (String nodename : availableNodeList) {
                    Map<String, String> map = oldStatusLabels.get(nodename);
                    map.put(NodeTypeEnum.PRIVATE.getLabelKey(), NodeTypeEnum.PRIVATE.getLabelValue());
                    nodeService.addNodeLabels(nodename, map, cluster.getId());
                }
                LOGGER.error("异常", e);
                return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.UNKNOWN);
            }
        }
        return ActionReturnUtil.returnSuccess();
    }

    @SuppressWarnings(CommonConstant.UNCHECKED)
    @Override
    public ActionReturnUtil getNamespaceList(String tenantid) throws Exception {

        //TODO 后续可能会添加相关信息，代码开发完成调试后删除
//        // 1.查询租户详情
//        ActionReturnUtil tenantBindingResult = tenantService.getSmplTenantDetail(tenantid);
//        if ((Boolean) tenantBindingResult.get(CommonConstant.SUCCESS) == false) {
//            logger.error("查询租户绑定信息失败,tenantId=" + tenantid);
//            return tenantBindingResult;
//        }
//        Map<String, Object> tenantBinding = new HashMap<>();
//        List<Map<String, Object>> tenantData = (List<Map<String, Object>>) tenantBindingResult.get(CommonConstant.DATA);
//        if (tenantData != null && !tenantData.isEmpty() && !tenantname.equals(tenantData.get(0).get(CommonConstant.NAME))) {
//            logger.error("查询租户绑定信息失败,tenantId=" + tenantid);
//            return ActionReturnUtil.returnSuccessWithMsg("租户tenantId=" + tenantid + "与租户名=tenantname" + tenantname + "不一致");
//        }
//        tenantBinding.putAll(tenantData.get(0));

        // 1.查询租户下namespace列表
        ActionReturnUtil listResult = getSimpleNamespaceListByTenant(tenantid);
        if ((Boolean) listResult.get(CommonConstant.SUCCESS) == false) {
            return listResult;
        }
        NamespaceList list = (NamespaceList) listResult.get(CommonConstant.DATA);
        if (CollectionUtils.isEmpty(list.getItems())) {
            return ActionReturnUtil.returnSuccessWithData((Object) null);
        }

        List<NamespaceShowDto> namespaces = new LinkedList<>();
        for (Namespace namespace : list.getItems()) {
            NamespaceShowDto namespaceShowDto = new NamespaceShowDto();
            namespaces.add(namespaceShowDto);
            namespaceShowDto.setName(namespace.getMetadata().getName());
            namespaceShowDto.setTime(namespace.getMetadata().getCreationTimestamp());
            if (null != namespace.getMetadata()
                    && null != namespace.getMetadata().getAnnotations()
                    && null != namespace.getMetadata().getAnnotations().get(CommonConstant.NEPHELE_ANNOTATION)
                    && StringUtils.isNotBlank(namespace.getMetadata().getAnnotations().get(CommonConstant.NEPHELE_ANNOTATION).toString())) {
                namespaceShowDto.setAnnotation((String) namespace.getMetadata().getAnnotations().get(CommonConstant.NEPHELE_ANNOTATION));
            }
//            namespaceShowDto.setTenant(tenantBinding);
            // 1.1根据namespace查询deployments列表
            ActionReturnUtil dlistResult = getDeploymentByNamespace(namespace.getMetadata().getName());
            if ((Boolean) dlistResult.get(CommonConstant.SUCCESS) == false) {
                return dlistResult;
            }
            DeploymentList dlist = (DeploymentList) dlistResult.get(CommonConstant.DATA);
            this.genrerateService(dlist, namespaceShowDto);

//            // 1.2根据namespace查询rolebinding列表
//            ActionReturnUtil rlistResult = getRolebindingByNamespace(namespace.getMetadata().getName());
//            if ((Boolean) rlistResult.get(CommonConstant.SUCCESS) == false) {
//                return rlistResult;
//            }
//            RoleBindingList rlist = (RoleBindingList) rlistResult.get(CommonConstant.DATA);
//            this.genrerateMember(rlist, namespaceShowDto);
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
    public ActionReturnUtil getNamespaceDetail(String name) throws Exception {

        NamespaceLocal namespaceLocal = namespaceLocalService.getNamespaceByName(name);
        if (Objects.isNull(namespaceLocal)) {
            throw new MarsRuntimeException(ErrorCodeMessage.NAMESPACE_NOT_FOUND);
        }
        List<NamespaceLocal> namespaceList = this.namespaceLocalService.getNamespaceListByTenantId(namespaceLocal.getTenantId());
        List<Map<String, Object>> otherNamespaceList = new ArrayList<>();
        for (NamespaceLocal ns : namespaceList) {
            if (!name.equals(ns.getNamespaceName())) {
                Map<String, Object> namespaceMap = new HashMap<>();
                namespaceMap.put(CommonConstant.NAME, ns.getNamespaceName());
                namespaceMap.put(CommonConstant.NS_ALIASNAME, ns.getAliasName());
                namespaceMap.put(CommonConstant.STATUS, ns.getIsPrivate());
                otherNamespaceList.add(namespaceMap);
            }
        }
        NamespaceLocal namespaceByName = this.namespaceLocalService.getNamespaceByName(name);
        //TODO   集群
       // Cluster cluster = clusterService.findClusterById(namespaceByName.getClusterId());

        Map<String, Object> namespaceQuota = this.getNamespaceQuota(name);
        namespaceQuota.put("otherNamespaceList", otherNamespaceList);
        //私有分区获取分区的私有节点列表
        if (namespaceQuota.get(ISPRIVATE) != null && (Boolean) namespaceQuota.get(ISPRIVATE)) {
            List<NodeDto> nodeDtos = nodeService.listNodeByNamespaces(name);
            namespaceQuota.put(PRIVATENODELIST, nodeDtos);
        }
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
        Cluster cluster = this.namespaceLocalService.getClusterByNamespaceName(namespace);
        K8SClientResponse deploymentResponse = deploymentService.doDeploymentsByNamespace(namespace, null, null, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(deploymentResponse.getStatus())) {
            logger.error("调用k8s接口查询namespace下deployment列表失败", deploymentResponse.getBody());
            return ActionReturnUtil.returnSuccessWithData(deploymentResponse.getBody());
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
//        Cluster cluster = this.getClusterByTenantid(tenantid);
        List<Cluster> clusters = this.clusterService.listCluster();
        NamespaceList list = null;
        for (Cluster cluster : clusters) {
            K8SClientResponse k8SClientResponse = namespaceService.list(null, bodys, HTTPMethod.GET, cluster);
            if (HttpStatusUtil.isSuccessStatus(k8SClientResponse.getStatus())) {
                NamespaceList newList = JsonUtil.jsonToPojo(k8SClientResponse.getBody(), NamespaceList.class);
                if (null == list) {
                    list = newList;
                } else {
                    List<Namespace> items = list.getItems();
                    items.addAll(newList.getItems());
                    list.setItems(items);
                }
            }
        }
        return ActionReturnUtil.returnSuccessWithData(list);
    }

    public ActionReturnUtil create(NamespaceDto namespaceDto, Cluster cluster) throws Exception {

        if (StringUtils.isEmpty(namespaceDto.getName()) || namespaceDto.getName().indexOf(CommonConstant.LINE) < 0) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.INVALID_NS_NAME);
        }
        //若分区开启istio自动注入，判断istio全局开关是否开启
        if(namespaceDto.getIstioStatus() && !istioCommonService.getIstioGlobalStatus(namespaceDto.getClusterId())){
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.ISTIO_GLOBAL_NOT_TURN);
        }

        //保存到本地数据库
        this.createLocalNamespace(namespaceDto);
        //组装k8s数据结构
        ObjectMeta objectMeta = new ObjectMeta();
        objectMeta.setAnnotations(this.getAnnotations(namespaceDto));
        objectMeta.setLabels(this.getLabels(namespaceDto));
        objectMeta.setName(namespaceDto.getName());
        Map<String, Object> bodys = new HashMap<>();
        bodys.put(CommonConstant.KIND, CommonConstant.NAMESPACE);
        bodys.put(CommonConstant.METADATA, objectMeta);
        Map<String, Object> headers = new HashMap<>();
        headers.put(CommonConstant.CONTENT_TYPE, CommonConstant.APPLICATION_JSON);
        //向k8s创建分区
        K8SClientResponse k8SClientResponse = namespaceService.create(headers, bodys, HTTPMethod.POST, cluster);
        Map<String, Object> stringObjectMap = JsonUtil.convertJsonToMap(k8SClientResponse.getBody());
        if (!HttpStatusUtil.isSuccessStatus(k8SClientResponse.getStatus())) {
            logger.error("调用k8s接口创建namespace失败，错误消息：" + k8SClientResponse.getBody(), k8SClientResponse.getBody());
            Object message = stringObjectMap.get("message");
            if (!Objects.isNull(message) && message.toString().contains("object is being deleted")) {
                throw new MarsRuntimeException(ErrorCodeMessage.NAMESPACE_CREATE_ERROR_DELETED, namespaceDto.getAliasName(), Boolean.TRUE);
            }
            if (!Objects.isNull(message) && message.toString().contains("no more than 63 characters")) {
                throw new MarsRuntimeException(DictEnum.NAMESPACE_ENGLISH.phrase(), ErrorCodeMessage.NAME_LENGTH_LIMIT);
            }
            throw new MarsRuntimeException(ErrorCodeMessage.NAMESPACE_CREATE_ERROR);
        }
        return ActionReturnUtil.returnSuccess();

    }

    private NamespaceLocal generateNamespaceLocal(NamespaceDto namespaceDto) {
        NamespaceLocal namespaceLocal = new NamespaceLocal();
        namespaceLocal.setTenantId(namespaceDto.getTenantId());
        namespaceLocal.setNamespaceName(namespaceDto.getName());
        namespaceLocal.setClusterId(namespaceDto.getClusterId());
        Date date = DateUtil.getCurrentUtcTime();
        namespaceLocal.setCreateTime(date);
        namespaceLocal.setIsPrivate(namespaceDto.isPrivate());
        namespaceLocal.setNamespaceId(UUIDUtil.get16UUID());
        return namespaceLocal;
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
                    return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.OPERATION_FAIL);
                }
            }
        }
        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public ActionReturnUtil createQuota(NamespaceDto namespaceDto, Cluster cluster) throws Exception {

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
            roleBindingService.createRoleBinding(rolebindingsEnum.getName(), namespaceDto.getName(), namespaceDto.getTenantId(), tenantName, rolebindingsEnum.getName().split(CommonConstant.LINE)[0], cluster);
        }
        return ActionReturnUtil.returnSuccess();
    }
//暂时不向分区绑定user
//    private ActionReturnUtil bindUser(NamespaceDto namespaceDto) throws Exception {
//
//        List<UserTenant> userByTenantid = userTenantService.getUserByTenantid(namespaceDto.getTenantid());
//        if (null == userByTenantid || userByTenantid.size() <= 0) {
//            return ActionReturnUtil.returnSuccess();
//        }
//        for (UserTenant userTenant : userByTenantid) {
//            TenantBinding tenantBinding = tenantService.getTenantByTenantid(namespaceDto.getTenantid());
//            String tenantName = tenantBinding.getTenantName();
//            String userName = userTenant.getUsername();
//            ActionReturnUtil bindingResult = roleService.rolebinding(tenantName, namespaceDto.getTenantid(), namespaceDto.getName(), userTenant.getRole(), userName);
//            if ((Boolean) bindingResult.get(CommonConstant.SUCCESS) == false) {
//                logger.error("调用k8s接口向rolebinding绑定租户管理员失败,tenantName=" + tenantName + ",tenantid=" + namespaceDto.getTenantid() + ", namespace=" + namespaceDto.getName()
//                        + ", userName=" + userName);
//                return bindingResult;
//            }
//
//        }
//        return ActionReturnUtil.returnSuccess();
//    }

    /**
     * @param namespace
     * @param networkname
     * @param type            产生ingress类型 1，网络白名单策略 2，租户分区互通网络策略，3，系统组件网络策略
     * @param networknamefrom
     * @param networknameto
     * @param cluster
     * @return
     * @throws Exception
     */
    @Override
    public ActionReturnUtil createNetworkPolicy(String namespace,
                                                String networkname,
                                                Integer type,
                                                String networknamefrom,
                                                String networknameto,
                                                Cluster cluster) throws Exception {

        String tenantName = namespace.split(CommonConstant.LINE)[0];

        Map<String, Object> matchLabels = new HashMap<String, Object>();

        Map<String, Object> spec = new HashMap<String, Object>();

        LabelSelector podSelector = new LabelSelector();
        //设置匹配标签
        podSelector.setMatchLabels(matchLabels);
        spec.put(CommonConstant.PODSELECTOR, podSelector);
        //设置ingress规则 若果为分区策略
        if (type != 2) {
            spec.put(CommonConstant.INGRESS, generateIngress(tenantName, networkname, networknamefrom, networknameto, type));
        }
        ObjectMeta objectMeta = new ObjectMeta();
        String networkpolicyname = null;
        // name+"-"+p.networknamefrom+"-"+p.networknameto+"configpolicy"
        //组装策略名
        if (StringUtils.isEmpty(networknamefrom) && StringUtils.isEmpty(networknameto)) {
            networkpolicyname = namespace + CommonConstant.POLICY;
            objectMeta.setName(networkpolicyname);
        } else {
            networkpolicyname = namespace + CommonConstant.LINE + networknamefrom.replaceAll(CommonConstant.UNDER_LINE + CommonConstant.MONIT_NETWORK, CommonConstant.EMPTYSTRING)
                    + CommonConstant.LINE + networknameto.replaceAll(CommonConstant.UNDER_LINE + CommonConstant.MONIT_NETWORK, CommonConstant.EMPTYSTRING) + CommonConstant.POLICY;
            objectMeta.setName(networkpolicyname);
        }
        //组装元数据
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
            throw new MarsRuntimeException(ErrorCodeMessage.NAMESPACE_POLICY_CREATE_ERROR);
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
        spec.put(CommonConstant.INGRESS, generateIngress(tenantName, null, null, null, 3));
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
            logger.error("调用k8s接口创建HA networkPolicy失败", k8SClientResponse.getBody());
            throw new MarsRuntimeException(ErrorCodeMessage.NAMESPACE_HA_POLICY_CREATE_ERROR);
        }

        return ActionReturnUtil.returnSuccess();
    }

    /**
     * 产生ingress
     *
     * @param tenantName      租户名
     * @param networkName
     * @param networknamefrom
     * @param networknameto
     * @param type            产生ingress类型 1，网络白名单策略 2，租户分区互通网络策略，3，系统组件网络策略
     * @return
     * @throws Exception
     */
    private Object generateIngress(String tenantName, String networkName, String networknamefrom, String networknameto, Integer type) throws Exception {

        Map<String, Object> matchLabels = new HashMap<String, Object>();
        switch (type) {
            case 1:
                String topology_key = new StringBuffer("nephele_topology_").append(networknamefrom).append(CommonConstant.UNDER_LINE).append(networknameto).toString();
                matchLabels.put(topology_key, "1");
                break;
            case 2:
                //如果后续网络在同一个集群的租户下，可以启用整个租户网络
//                matchLabels.put("nephele_tenant", tenantName);
//                matchLabels.put(CommonConstant.NEPHELE_TENANT_NETWORK, tenantName + CommonConstant.LINE + networkName);
                break;
            case 3:
                matchLabels.put(CommonConstant.INITKUBESYSTEM, CommonConstant.KUBE_SYSTEM);
                break;
            default:
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


    private Map<String, Object> getLabels(NamespaceDto namespaceDto) throws Exception {
        //
        Map<String, Object> labels = new HashMap<>();
        TenantBinding tenantByTenantid = this.tenantService.getTenantByTenantid(namespaceDto.getTenantId());
        labels.put("nephele_tenant", tenantByTenantid.getTenantName());
        labels.put("nephele_tenantid", namespaceDto.getTenantId());
//        lables.put("nephele_tenant_network", new StringBuffer().append(tenantName).append(CommonConstant.LINE).append(namespaceDto.getNetwork().getName()).toString());
        if (namespaceDto.isPrivate()) {
            labels.put("isPrivate", "1");// 私有
        } else {
            labels.put("isPrivate", "0");// 共享
        }
        //开启分区开关
        if (namespaceDto.getIstioStatus()) {
            labels.put(CommonConstant.ISTIO_INJECTION,CommonConstant.OPEN_ISTIO_AUTOMATIC_INJECTION);
        } else {
            labels.put(CommonConstant.ISTIO_INJECTION, CommonConstant.CLOSE_ISTIO_AUTOMATIC_INJECTION);
        }
        // lables.put("nephele_tenant_network_subnet", new
        // StringBuffer().append(namespaceDto.getNetwork().getName()).append(CommonConstant.LINE)
        // .append(namespaceDto.getNetwork().getSubnet().getSubnetname()).toString());

        return labels;
    }

    private Map<String, Object> getAnnotations(NamespaceDto namespaceDto) throws Exception {
        Map<String, Object> annotations = new HashMap<>();
        //TODO 后续网络可能会用到
//        annotations.put(CommonConstant.NEPHELE_SUBNETID, namespaceDto.getNetwork().getSubnet().getSubnetid());
//        annotations.put(CommonConstant.NETWORK_POLICY, CommonConstant.NETWORK_POLICY_INGRESS);
//        annotations.put(CommonConstant.NEPHELE_SUBNETNAME, namespaceDto.getNetwork().getSubnet().getSubnetname());
        annotations.put(CommonConstant.NEPHELE_ANNOTATION, namespaceDto.getAnnotation());
//        annotations.put(CommonConstant.NEPHELE_NETWORKID, namespaceDto.getNetwork().getNetworkid());
//        annotations.put(CommonConstant.NEPHELE_NETWORKNAME, namespaceDto.getNetwork().getName());

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
        if (namespaceDto.getQuota().getGpu() != null) {
            hard.put(CommonConstant.GPU_QUOTA_KEY, namespaceDto.getQuota().getGpu());
        }

        //获取租户对存储配额的设定值，塞到Quota.hard中
        List<StorageClassQuotaDto> storageClassQuotaDtoList = namespaceDto.getStorageClassQuotaList();
        if (storageClassQuotaDtoList != null) {
            for (StorageClassQuotaDto storageClassQuotaDto : storageClassQuotaDtoList) {
                if (!StringUtils.isBlank(storageClassQuotaDto.getName()) && Double.parseDouble(storageClassQuotaDto.getQuota()) > 0) {
                    hard.put(storageClassQuotaDto.getName() + ".storageclass.storage.k8s.io/requests.storage", storageClassQuotaDto.getQuota() + "Gi");
                }
            }
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
        AssertUtil.notNull(subnet);
        AssertUtil.notBlank(subnet.getSubnetid(), DictEnum.SUB_NETWORK_ID);
        AssertUtil.notBlank(subnet.getSubnetid(), DictEnum.SUB_NETWORK_NAME);
        AssertUtil.notNull(subnet);
        NamespceBindSubnet net = networkService.getsubnetbySubnetnameAndSubnetid(subnet.getSubnetid(), subnet.getSubnetname());

        if (null == net) {
            logger.error("验证subnet是否已经被绑定时,subnet未查询到.subnetid=" + subnet.getSubnetid() + "subnetname=" + subnet.getSubnetname());
            return ActionReturnUtil.returnErrorWithMsg("");
        }

        if (net.getBinding() == 1) {
            logger.info("subnet" + subnet.getSubnetname() + "已经被绑定");
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.NETWORK_ALREADY_BIND);
        }
        return ActionReturnUtil.returnSuccess();
    }

    private ActionReturnUtil dealTopology(NamespaceDto namespaceDto, Cluster cluster) throws Exception {

        if (namespaceDto.getNetwork() == null || StringUtils.isEmpty(namespaceDto.getNetwork().getNetworkid())) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.INVALID_PARAMETER, DictEnum.NETWORK_ID.phrase(), true);
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
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.QUERY_FAIL, DictEnum.NAMESPACE.phrase(), true);
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
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.UPDATE_FAIL, update.getBody(), false);
        }

        return ActionReturnUtil.returnSuccess();
    }

    public void rollbackNetworkAndNamespace(String tenantid, String namespace, String clusterId) throws Exception {
//        // 删除子网
//        networkService.subnetworkDelete(subnetId);
        // 删除namespace
        this.deleteNamespace(tenantid, namespace, clusterId);
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
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.QUERY_FAIL, DictEnum.NAMESPACE.phrase(), true);
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
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.UPDATE_FAIL, update.getBody(), false);
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
    public String getPrivatePartitionLabel(String tenantid, String namespace) throws Exception {
        String privatePartitionLabel = this.privatePartitionService.getPrivatePartitionLabel(tenantid, namespace);
        return privatePartitionLabel;
    }

    @Override
    public List<Map<String, Object>> getNamespaceListByTenantid(String tenantid) throws Exception {
        // 初始化判断1
        AssertUtil.notBlank(tenantid, DictEnum.TENANT_ID);
        List<NamespaceLocal> namespaceList = this.namespaceLocalService.getAllNamespaceListByTenantId(tenantid);
        List<Map<String, Object>> namespaceData = this.getNameSpaceQuota(namespaceList);
        Map<String, Map<String, StorageClassDto>> clusterStorageMap = new HashMap<>();
        //各个集群各个存储的使用量
        Map<String,Map<String, Integer>> nsStorageUsed = new HashMap<>();
        for (Map<String, Object> data : namespaceData) {
            //String namespace = data.get(CommonConstant.NAME).toString();
            if (data == null) {
                logger.error("获取分区集群配额错误，namespaceData:{}", JSONObject.toJSONString(namespaceData));
                continue;
            }
            if(data.get(CommonConstant.CLUSTERID) == null){
                logger.error("未能获取到集群id");
                continue;
            }
            String clusterId = data.get(CommonConstant.CLUSTERID).toString();
            Map<String, StorageClassDto> storageClassDtoMap = clusterStorageMap.get(clusterId);
            if (storageClassDtoMap == null) {
                List<StorageClassDto> storageClassDtoList = storageClassService.listStorageClass(clusterId);
                clusterStorageMap.put(clusterId, storageClassDtoList.stream().collect(Collectors.toMap(StorageClassDto::getName, storageClassDto -> storageClassDto)));
            }

            Map<String, LinkedList<String>> storageClassData = (Map<String, LinkedList<String>>) data.get(STORAGECLASSES);
            for (String storageClassName : storageClassData.keySet()) {
                if (nsStorageUsed.get(clusterId) == null) {
                    Map<String, Integer> storageUsedMap = new HashMap<>();
                    storageUsedMap.put(storageClassName, Integer.parseInt(storageClassData.get(storageClassName).get(0)));
                    nsStorageUsed.put(clusterId, storageUsedMap);
                } else if (nsStorageUsed.get(clusterId).get(storageClassName) == null) {
                    Map<String, Integer> storageUsedMap = nsStorageUsed.get(clusterId);
                    storageUsedMap.put(storageClassName, Integer.parseInt(storageClassData.get(storageClassName).get(0)));
                } else {
                    Map<String, Integer> storageUsedMap = nsStorageUsed.get(clusterId);
                    storageUsedMap.put(storageClassName, storageUsedMap.get(storageClassName) + Integer.parseInt(storageClassData.get(storageClassName).get(0)));
                }
            }
        }
        //设定每个分区最大存储值
        for (Map<String, Object> data : namespaceData) {
            String clusterId = data.get(CommonConstant.CLUSTERID).toString();
            Map<String, StorageClassDto> storageClassDtoMap = clusterStorageMap.get(clusterId);
            Map<String, LinkedList<String>> storageClassData = (Map<String, LinkedList<String>>) data.get(STORAGECLASSES);
            for (String storageClassName : storageClassData.keySet()) {

                if (nsStorageUsed.get(storageClassName) != null) {
                    LinkedList<String> oldQuotaList = storageClassData.get(storageClassName);
                    LinkedList<String> newQuotaList = new LinkedList<>();
                    newQuotaList.add(oldQuotaList.get(0));
                    newQuotaList.add(oldQuotaList.get(1));
                    if (nsStorageUsed.get(clusterId) == null || nsStorageUsed.get(clusterId).get(storageClassName) == null){
                        newQuotaList.add(String.valueOf(Integer.parseInt(oldQuotaList.get(0)) + Integer.parseInt(oldQuotaList.get(2))));
                    } else {
                        newQuotaList.add(String.valueOf(Integer.parseInt(oldQuotaList.get(0)) + Integer.parseInt(oldQuotaList.get(2)) - nsStorageUsed.get(clusterId).get(storageClassName)));
                    }
                    newQuotaList.add(String.valueOf(Integer.parseInt(newQuotaList.get(2)) - Integer.parseInt(newQuotaList.get(0))));
                    storageClassData.put(storageClassName, newQuotaList);
                }

            }

        }
        return namespaceData;
    }

    /**
     * 根据tenantid查询集群资源使用列表
     *
     * @param tenantid  如果为null查询当前集群下所有的资源使用情况
     * @param clusterId 如果为null查询所有集群的资源使用情况
     * @return
     * @throws Exception
     */
    public Map<String, List> getClusterQuotaListByTenantid(String tenantid, String clusterId) throws Exception {
        Map<String, List> result = new HashMap<>();
        //获取所有的分区列表
        List<NamespaceLocal> namespaceList = null;
        if (StringUtils.isEmpty(tenantid)) {
            namespaceList = this.namespaceLocalService.getPublicNamespaceListByClusterId(clusterId);
        } else {
            namespaceList = this.namespaceLocalService.getAllPublicNamespaceListByTenantId(tenantid);
        }

        if (CollectionUtils.isEmpty(namespaceList)) {
            return result;
        }
        Map<String, List<NamespaceLocal>> clusterNsMap = new HashMap<>();
        //根据集群查询分区的详情
        for (NamespaceLocal ns : namespaceList) {
            if (clusterNsMap.get(ns.getClusterId().toString()) != null) {
                clusterNsMap.get(ns.getClusterId().toString()).add(ns);
            } else {
                List<NamespaceLocal> clusterList = new ArrayList<>();
                clusterList.add(ns);
                clusterNsMap.put(ns.getClusterId().toString(), clusterList);
            }
        }
        //如果集群不为空则筛选指定集群的分区信息
        if ((!Objects.isNull(clusterId)) && StringUtils.isNotBlank(tenantid)) {
            List<NamespaceLocal> namespaceLocals = clusterNsMap.get(clusterId.toString());
            clusterNsMap.clear();
            clusterNsMap.put(clusterId.toString(), namespaceLocals);
        }
        if (CollectionUtils.isEmpty(clusterNsMap)) {
            return result;
        }
        //获取每个集群的使用量
        for (Map.Entry<String, List<NamespaceLocal>> entry : clusterNsMap.entrySet()) {
            List<Map<String, Object>> clusterNamespaceData = this.getNameSpaceQuota(entry.getValue());
            result.put(entry.getKey(), clusterNamespaceData);
        }
        return result;
    }

    /**
     * 根据clusterId查询namespace配额使用量详情列表
     *
     * @param clusterId
     * @return
     * @throws Exception
     */
    @Override
    public List<Map<String, Object>> getNamespaceListByClusterId(String clusterId) throws Exception {
        // 初始化判断1
        if (Objects.isNull(clusterId)) {
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTERID_NOT_BLANK);
        }
        Cluster cluster = clusterService.findClusterById(clusterId);
        List<NamespaceLocal> namespaceList = namespaceLocalService.getNamespaceListByClusterId(clusterId);
        Map<String, NamespaceLocal> namespaceLocalMap = namespaceList.stream().collect(Collectors.toMap(NamespaceLocal::getNamespaceName, ns -> ns));
        List<Map<String, Object>> namespaceData = new ArrayList<Map<String, Object>>();
        List<Namespace> namespaces = this.namespaceService.list(cluster);
        if (CollectionUtils.isEmpty(namespaces)) {
            return namespaceData;
        }
        for (Namespace namespace : namespaces) {
            Map<String, Object> namespaceDetail = this.getNamespaceQuota(namespace, cluster);
            if (namespaceDetail != null) {
                if (namespaceLocalMap.get(namespace.getMetadata().getName()) != null) {
                    namespaceDetail.put(CommonConstant.NS_ALIASNAME, namespaceLocalMap.get(namespace.getMetadata().getName()).getAliasName());
                    namespaceDetail.put(CommonConstant.TENANT_ID, namespaceLocalMap.get(namespace.getMetadata().getName()).getTenantId());
                }
                namespaceData.add(namespaceDetail);
            }
        }
        return namespaceData;
    }

    public List<Map<String, Object>>  getNameSpaceQuota(List<NamespaceLocal> namespaceList) throws Exception {
        List<Map<String, Object>> namespaceData = new CopyOnWriteArrayList<>();
        if (!CollectionUtils.isEmpty(namespaceList)) {
            // 查询namespace信息
            CountDownLatch countDownLatchApp = new CountDownLatch(namespaceList.size());
            for (NamespaceLocal namespace : namespaceList) {
                ThreadPoolExecutorFactory.executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Map<String, Object> namespaceDetail = getNamespaceQuota(namespace.getNamespaceName());
                            if (namespaceDetail != null) {
                                namespaceDetail.put(CommonConstant.NS_ALIASNAME, namespace.getAliasName());
                                namespaceDetail.put(CommonConstant.TENANT_ID, namespace.getTenantId());

                                namespaceData.add(namespaceDetail);
                            }
                        } catch (Exception e) {
                            logger.error("获取分区配额失败", e);
                        } finally {
                            countDownLatchApp.countDown();
                        }
                    }
                });
            }
            countDownLatchApp.await();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            // 对时间进行升序
            if (!CollectionUtils.isEmpty(namespaceData)) {
                try {
                    Collections.sort(namespaceData, new Comparator<Map<String, Object>>() {
                        @Override
                        public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                            try {
                                if(o1.get(CREATETIME) == null || o2.get(CREATETIME) == null){
                                    logger.warn("分区创建时间为空,namesapce1:{},namespace2:{}",
                                            JSONObject.toJSONString(o1), JSONObject.toJSONString(o2));
                                    return 0;
                                }
                                return Long.valueOf(sdf.parse(o1.get(CREATETIME).toString()).getTime())
                                        .compareTo(Long.valueOf(sdf.parse(o2.get(CREATETIME).toString()).getTime()));
                            } catch (ParseException e) {
                                return 0;
                            }
                        }
                    });
                } catch (Exception e) {
                    logger.error("ns排序错误", e);
                }
            }
        }
        return namespaceData;
    }

    @Override
    public List<NamespaceStorageDto> listNamespaceStorage(String tenantId, String clusterId) throws Exception {
        AssertUtil.notBlank(tenantId, DictEnum.TENANT_ID);
        List<NamespaceLocal> namespaceLocals = null;
        if (StringUtils.isBlank(clusterId)) {
            namespaceLocals = namespaceLocalService.getAllNamespaceListByTenantId(tenantId);
        } else {
            namespaceLocals = namespaceLocalService.listNamespace(tenantId, clusterId);
        }
        if (CollectionUtils.isEmpty(namespaceLocals)) {
            return Collections.emptyList();
        }
        List<NamespaceStorageDto> namespaceStorageDtos = new ArrayList<>();
        List<Map<String, Object>> namespaceData = this.getNameSpaceQuota(namespaceLocals);
        for (Map<String, Object> data : namespaceData) {
            if (data == null) {
                logger.error("获取分区集群配额错误，namespaceData:{}", JSONObject.toJSONString(namespaceData));
                continue;
            }
            String namespace = data.get(CommonConstant.NAME).toString();
            String namespaceClusterId = data.get(CommonConstant.CLUSTERID).toString();
            Map<String, LinkedList<String>> storageClassData = (Map<String, LinkedList<String>>) data.get(STORAGECLASSES);
            for (String storageClassName : storageClassData.keySet()) {
                LinkedList<String> quotaList = storageClassData.get(storageClassName);
                NamespaceStorageDto namespaceStorageDto = new NamespaceStorageDto();
                namespaceStorageDto.setNamespace(namespace);
                namespaceStorageDto.setClusterId(namespaceClusterId);
                namespaceStorageDto.setStorageClass(storageClassName);
                namespaceStorageDto.setHard(Integer.parseInt(quotaList.get(STORAGE_HARD_INDEX)));
                namespaceStorageDto.setUsed(Integer.parseInt(quotaList.get(STORAGE_USED_INDEX)));
                namespaceStorageDtos.add(namespaceStorageDto);

            }
        }
        return namespaceStorageDtos;
    }

    @Override
    public Map<String, Object> getNamespaceQuota(String namespace) throws Exception {
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        K8SClientResponse namespace2 = this.namespaceService.getNamespace(namespace, null, null, cluster);
        if (!HttpStatusUtil.isSuccessStatus(namespace2.getStatus())) {
            logger.error("调用k8s接口查询namespace失败", namespace2.getBody());
            return namespace2.getBody() == null ? ActionReturnUtil.returnError() : ActionReturnUtil.returnErrorWithMsg(namespace2.getBody());
        }
        Namespace namespacePojo = JsonUtil.jsonToPojo(namespace2.getBody(), Namespace.class);
        return this.getNamespaceQuota(namespacePojo, cluster);
    }

    public Map<String, Object> getNamespaceQuota(Namespace namespace, Cluster cluster) throws Exception {
        Map<String, Object> namespaceMap = new HashMap<String, Object>();
        String nsName = namespace.getMetadata().getName();
        NamespaceLocal namespaceLocal = namespaceLocalService.getNamespaceByName(nsName);
        if (namespaceLocal == null) {
            return null;
        }
        namespaceMap.put(CommonConstant.CLUSTERID, cluster.getId());
        namespaceMap.put(CommonConstant.CLUSTERALIASID, cluster.getAliasName());
        namespaceMap.put(CommonConstant.CLUSTER_NAME, cluster.getName());
        namespaceMap.put(CommonConstant.DATACENTER_NAME, cluster.getDataCenterName());
        Object label = null;
        if (namespace.getMetadata().getLabels() != null) {
            label = namespace.getMetadata().getLabels().get(ISPRIVATE);
        }
        String phase = namespace.getStatus().getPhase();
        String creationTimestamp = namespace.getMetadata().getCreationTimestamp();
        namespaceMap.put(PHASE, phase);
        namespaceMap.put(CREATETIME, creationTimestamp);
        if (label != null && label.toString().equals(CommonConstant.ONENUMSTRING)) {
            namespaceMap.put(ISPRIVATE, true);
        } else {
            namespaceMap.put(ISPRIVATE, false);
        }
        ResourceQuotaList quotaList = this.getResouceQuota(nsName, cluster);
        namespaceMap.put(CommonConstant.NAME, nsName);
        namespaceMap.put(CommonConstant.ALIASNAME, namespaceLocal.getAliasName());
        //获取namespace对应的tenantId
        String tenantId = (String) namespace.getMetadata().getLabels().get(TENANTID);
        TenantClusterQuota tenantClusterQuota = tenantClusterQuotaService.getClusterQuotaByTenantIdAndClusterId(tenantId, cluster.getId());
        Map<String, String> storageQuotaMap = new HashMap<>();
        if (tenantClusterQuota != null && !StringUtils.isBlank(tenantClusterQuota.getStorageQuotas())) {
            String[] storageQuotasArray = tenantClusterQuota.getStorageQuotas().split(",");
            for (String storageQuota : storageQuotasArray) {
                String[] storageQuotaArray = storageQuota.split("_");
                storageQuotaMap.put(storageQuotaArray[0], storageQuotaArray[1]);
            }
        }
        if (quotaList != null && quotaList.getItems() != null && quotaList.getItems().size() != 0) {
            ResourceQuota resourceQuota = quotaList.getItems().get(0);
            if (resourceQuota.getSpec() != null && resourceQuota.getStatus() != null) {
                ResourceQuotaSpec resourceQuotaSpec = resourceQuota.getSpec();
                ResourceQuotaStatus resourceQuotaStatus = resourceQuota.getStatus();
                Map<String, String> hard = (Map<String, String>) resourceQuotaSpec.getHard();
                Map<String, String> used = (Map<String, String>) resourceQuotaStatus.getUsed();

                //将存储数据放回给前台
                if (hard.size() > 0) {
                    Map<String, Object> storageClass = new HashMap<>();
                    for (String key : hard.keySet()) {
                        if (key.endsWith(STORAGE_RESOURCE)) {

                            List<Object> storage = new LinkedList<>();
                            //total storage value
                            storage.add(hard.get(key).split(CommonConstant.GI)[0]);
                            //used storage value
                            storage.add(used.get(key).split(CommonConstant.GI)[0]);
                            //集群storage quota
                            if (!CollectionUtils.isEmpty(storageQuotaMap)) {
                                if (storageQuotaMap.get(key.split("\\.")[0]) != null) {
                                    storage.add(storageQuotaMap.get(key.split("\\.")[0]));
                                } else {
                                    logger.error("集群设置存储配额错误，namespace：{}", nsName);
                                }
                            }
                            storageClass.put(key.split("\\.")[0], storage);
                            storageQuotaMap.remove(key.split("\\.")[0]);
                        }
                    }
                    for (String storageClassName : storageQuotaMap.keySet()) {
                        List<Object> storage = new LinkedList<>();
                        storage.addAll(Arrays.asList("0", "0", storageQuotaMap.get(storageClassName)));
                        storageClass.put(storageClassName, storage);
                    }
                    namespaceMap.put(Resource.STORAGECLASS, storageClass);
                    namespaceMap.put(STORAGE_TYPE, CommonConstant.GB);
                }

                List<Object> cpu = new LinkedList<>();
                // 保留1位小数 四舍五入
                NumberFormat nf = NumberFormat.getNumberInstance();
                nf.setGroupingUsed(false);
                nf.setMaximumFractionDigits(CommonConstant.NUM_ONE);
                nf.setRoundingMode(RoundingMode.HALF_UP);
                if (hard.get(CommonConstant.CPU).contains(CommonConstant.SMALLM)) {
                    String chard = hard.get(CommonConstant.CPU).split(CommonConstant.SMALLM)[0];
                    double hardMemory = Double.parseDouble(chard);
                    cpu.add(nf.format(hardMemory / CommonConstant.NUM_THOUSAND));
                } else {
                    cpu.add(hard.get(CommonConstant.CPU));
                }
                if (used.get(CommonConstant.CPU).contains(CommonConstant.SMALLM)) {
                    String chard = used.get(CommonConstant.CPU).split(CommonConstant.SMALLM)[0];
                    double hardMemory = Double.parseDouble(chard);
                    cpu.add(nf.format(hardMemory / CommonConstant.NUM_THOUSAND));
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
                if (hard.get(CommonConstant.MEMORY).equals(CommonConstant.ZERONUM)) {
                    String mhard = hard.get(CommonConstant.MEMORY);
                    memory.add(mhard);
                    hardtype = CommonConstant.NUM_GB;
                } else if (hard.get(CommonConstant.MEMORY).contains(CommonConstant.KI) || hard.get(CommonConstant.MEMORY).contains(CommonConstant.SMALLK)) {
                    String mhard = null;
                    if (hard.get(CommonConstant.MEMORY).contains(CommonConstant.SMALLM)) {
                        mhard = hard.get(CommonConstant.MEMORY).split(CommonConstant.SMALLK)[0];
                    } else {
                        mhard = hard.get(CommonConstant.MEMORY).split(CommonConstant.KI)[0];
                    }
                    double hardMemory = Double.parseDouble(mhard);
                    int mum = 0;
                    while (hardMemory >= CommonConstant.NUM_SIZE_MEMORY) {
                        hardMemory = hardMemory / CommonConstant.NUM_SIZE_MEMORY;
                        mum++;
                    }
                    hardtype = mum;
                    memory.add(nf.format(hardMemory % CommonConstant.NUM_ONE_DOUBLE == 0 ? (long) hardMemory : hardMemory));
                } else if (hard.get(CommonConstant.MEMORY).contains(CommonConstant.SMALLM)) {
                    String mhard = null;
                    mhard = hard.get(CommonConstant.MEMORY).split(CommonConstant.SMALLM)[0];
                    double hardMemory = Double.parseDouble(mhard);
                    int mum = 0;
                    hardMemory = hardMemory / (CommonConstant.NUM_THOUSAND * CommonConstant.NUM_SIZE_MEMORY);
                    while (hardMemory >= CommonConstant.NUM_SIZE_MEMORY) {
                        hardMemory = hardMemory / CommonConstant.NUM_SIZE_MEMORY;
                        mum++;
                    }
                    hardtype = mum;
                    memory.add(nf.format(hardMemory % CommonConstant.NUM_ONE_DOUBLE == 0 ? (long) hardMemory : hardMemory));
                } else if (hard.get(CommonConstant.MEMORY).contains(CommonConstant.MI)) {
                    String mhard = null;
                    mhard = hard.get(CommonConstant.MEMORY).split(CommonConstant.MI)[0];
                    double hardMemory = Double.parseDouble(mhard);
                    int mum = CommonConstant.NUM_ONE;
                    while (hardMemory >= CommonConstant.NUM_SIZE_MEMORY) {
                        hardMemory = hardMemory / CommonConstant.NUM_SIZE_MEMORY;
                        mum++;
                    }
                    hardtype = mum;
                    memory.add(nf.format(hardMemory % CommonConstant.NUM_ONE_DOUBLE == 0 ? (long) hardMemory : hardMemory));
                } else if (hard.get(CommonConstant.MEMORY).contains(CommonConstant.GI) || hard.get(CommonConstant.MEMORY).contains(CommonConstant.SMALLG)) {
                    String mhard = hard.get(CommonConstant.MEMORY).split(CommonConstant.GI)[0];
                    memory.add(mhard);
                    hardtype = CommonConstant.NUM_TWO;
                } else if (hard.get(CommonConstant.MEMORY).contains(CommonConstant.TI) || hard.get(CommonConstant.MEMORY).contains(CommonConstant.SMALLT)) {
                    String mhard = hard.get(CommonConstant.MEMORY).split(CommonConstant.TI)[0];
                    memory.add(mhard);
                    hardtype = CommonConstant.NUM_THREE;
                } else if (hard.get(CommonConstant.MEMORY).contains(CommonConstant.PI)) {
                    String mhard = hard.get(CommonConstant.MEMORY).split(CommonConstant.PI)[0];
                    memory.add(mhard);
                    hardtype = CommonConstant.NUM_FOUR;
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
                    while (usedMemory >= CommonConstant.NUM_SIZE_MEMORY) {
                        usedMemory = usedMemory / CommonConstant.NUM_SIZE_MEMORY;
                        mum++;
                    }
                    usedtype = mum;
                    memory.add(nf.format(usedMemory % CommonConstant.NUM_ONE_DOUBLE == 0 ? (long) usedMemory : usedMemory));
                } else if (used.get(CommonConstant.MEMORY).contains(CommonConstant.MI)) {
                    String mused = used.get(CommonConstant.MEMORY).split(CommonConstant.MI)[0];
                    double usedMemory = Double.parseDouble(mused);
                    int mum = CommonConstant.NUM_ONE;
                    while (usedMemory >= CommonConstant.NUM_SIZE_MEMORY) {
                        usedMemory = usedMemory / CommonConstant.NUM_SIZE_MEMORY;
                        mum++;
                    }
                    usedtype = mum;
                    memory.add(nf.format(usedMemory % CommonConstant.NUM_ONE_DOUBLE == 0 ? (long) usedMemory : usedMemory));
                } else if (used.get(CommonConstant.MEMORY).contains(CommonConstant.GI)) {
                    String mused = used.get(CommonConstant.MEMORY).split(CommonConstant.GI)[0];
                    memory.add(mused);
                    usedtype = CommonConstant.NUM_TWO;
                } else if (used.get(CommonConstant.MEMORY).contains(CommonConstant.TI)) {
                    String mused = used.get(CommonConstant.MEMORY).split(CommonConstant.TI)[0];
                    memory.add(mused);
                    usedtype = CommonConstant.NUM_THREE;
                } else if (used.get(CommonConstant.MEMORY).contains(CommonConstant.PI)) {
                    String mused = used.get(CommonConstant.MEMORY).split(CommonConstant.PI)[0];
                    memory.add(mused);
                    usedtype = CommonConstant.NUM_FOUR;
                }

                namespaceMap.put(CommonConstant.MEMORY, memory);
                switch (hardtype) {
                    case 0:
                        namespaceMap.put(CommonConstant.HARDTYPE, CommonConstant.KB);
                        break;
                    case CommonConstant.NUM_ONE:
                        namespaceMap.put(CommonConstant.HARDTYPE, CommonConstant.MB);
                        break;
                    case CommonConstant.NUM_TWO:
                        namespaceMap.put(CommonConstant.HARDTYPE, CommonConstant.GB);
                        break;
                    case CommonConstant.NUM_THREE:
                        namespaceMap.put(CommonConstant.HARDTYPE, CommonConstant.TB);
                        break;
                    case CommonConstant.NUM_FOUR:
                        namespaceMap.put(CommonConstant.HARDTYPE, CommonConstant.PB);
                        break;
                }
                switch (usedtype) {
                    case 0:
                        namespaceMap.put(CommonConstant.USEDTYPE, CommonConstant.KB);
                        break;
                    case CommonConstant.NUM_ONE:
                        namespaceMap.put(CommonConstant.USEDTYPE, CommonConstant.MB);
                        break;
                    case CommonConstant.NUM_TWO:
                        namespaceMap.put(CommonConstant.USEDTYPE, CommonConstant.GB);
                        break;
                    case CommonConstant.NUM_THREE:
                        namespaceMap.put(CommonConstant.USEDTYPE, CommonConstant.TB);
                        break;
                    case CommonConstant.NUM_FOUR:
                        namespaceMap.put(CommonConstant.USEDTYPE, CommonConstant.PB);
                        break;
                }

                //gpu
                List<Object> gpu = new LinkedList<>();
                if(hard.get(CommonConstant.GPU_QUOTA_KEY) != null) {
                    gpu.add(hard.get(CommonConstant.GPU_QUOTA_KEY));
                    if(used.get(CommonConstant.GPU_QUOTA_KEY) != null){
                        gpu.add(used.get(CommonConstant.GPU_QUOTA_KEY));
                    }else{
                        gpu.add(CommonConstant.ZERONUM);
                    }
                    namespaceMap.put(CommonConstant.GPU, gpu);
                }
            }
            return namespaceMap;
        }
        return null;
    }

    /**
     * 根据tenantid查询namespace名称（不包含分区配额信息）
     *
     * @param tenantid
     * @return
     * @throws Exception
     */
    @Override
    public List<NamespaceLocal> listNamespaceNameByTenantid(String tenantid) throws Exception {
        List<NamespaceLocal> namespaceList = this.namespaceLocalService.getNamespaceListByTenantId(tenantid);
        return namespaceList;
    }

    /**
     * 添加私有分区主机状态
     *
     * @param namespaceDto
     * @throws Exception
     */
    @Override
    public void addPrivateNamespaceNodes(NamespaceDto namespaceDto) throws Exception {
        String clusterId = namespaceDto.getClusterId();
        List<String> nodeList = namespaceDto.getNodeList();
        Map<String, String> updateLabel = this.getUpdateLabel(namespaceDto, Boolean.FALSE);
        for (String nodeName : nodeList) {
            this.nodeService.addNodeLabels(nodeName, updateLabel, clusterId);
        }
    }

    /**
     * 移除私有分区主机状态
     *
     * @param namespaceDto
     * @throws Exception
     */
    @Override
    public void removePrivateNamespaceNodes(NamespaceDto namespaceDto) throws Exception {
        String namespace = namespaceDto.getName();
        NamespaceLocal namespaceByName = this.namespaceLocalService.getNamespaceByName(namespace);
        //检查是否为私有分区
        if (!namespaceByName.getIsPrivate()) {
            throw new MarsRuntimeException(ErrorCodeMessage.PRIVATE_NAMESPACE_ONLY);
        }
        //查询集群
        Cluster cluster = this.clusterService.findClusterById(namespaceByName.getClusterId());
        String nodeName = namespaceDto.getNodeName();
        //获取该节点的pod列表
        List<PodDto> podList = podService.PodList(nodeName, cluster);
        for (PodDto podDto : podList) {
            //如果该节点还有其他非系统pod则提示不能移除该独占主机
            if (!(CommonConstant.KUBE_SYSTEM.equals(podDto.getNamespace()) || CommonConstant.DEFAULT.equals(podDto.getNamespace()))) {
                throw new MarsRuntimeException(ErrorCodeMessage.NODE_NOT_REMOVE);
            }
        }
        //获取更新主机标签
//        Map<String, String> deleteLabel = this.getUpdateLabel(namespaceDto,Boolean.TRUE);
        Map<String, String> updateLabel = new HashMap<>();
        //更新主机位闲置状态标签
        updateLabel.put(NodeTypeEnum.PRIVATE.getLabelKey(), NodeTypeEnum.PRIVATE.getLabelValue());
        String tenantId = namespaceDto.getTenantId();
        TenantBinding tenant = this.tenantService.getTenantByTenantid(tenantId);
        updateLabel.put(CommonConstant.HARMONYCLOUD_TENANTNAME_NS, tenant.getTenantName());
        updateLabel.put(CommonConstant.HARMONYCLOUD_TENANT_ID, tenant.getTenantId());
        //更新主机信息
        this.nodeService.removePrivateNamespaceNodes(nodeName, updateLabel, null, cluster);

        //获取分区的配额
        Map<String, Object> namespaceQuota = this.getNamespaceQuota(namespace);
        namespaceDto.setStorageClassQuotaList(this.getStorageQuota(namespaceQuota));
        List<NodeDto> nodeDtos = nodeService.listNodeByNamespaces(namespace);
        //独占分区下没有节点不需要计算配额,配额都为0
        if(CollectionUtils.isEmpty(nodeDtos)){
            namespaceDto.setQuota(new QuotaDto("0","0","0"));
        }else {
            List<String> memoryStatus = (List<String>) namespaceQuota.get(CommonConstant.MEMORY);
            List<String> cpuStatus = (List<String>) namespaceQuota.get(CommonConstant.CPU);
            List<String> gpuStatus = (List<String>) namespaceQuota.get(CommonConstant.GPU);
            Double cpu = Double.valueOf(cpuStatus.get(0));
            String hardUnit = namespaceQuota.get(CommonConstant.HARDTYPE).toString();
            double memoryHard = Double.valueOf(memoryStatus.get(0));
            memoryHard = transformMemoryToGb(memoryHard, hardUnit);
            Integer gpu = null;
            if (gpuStatus != null) {
                gpu = Integer.valueOf(gpuStatus.get(0));
            }

            //处理节点资源使用量
            QuotaDto quota2 = namespaceDto.getQuota();
            Map node = nodeService.getNodeDetail(nodeName, cluster);
            NodeDetailDto nodeDetail = (NodeDetailDto) node.get(CommonConstant.DATA);
            if (nodeDetail == null && StringUtils.isEmpty(nodeDetail.getCpu())) {
                throw new MarsRuntimeException(ErrorCodeMessage.NODE_NOT_EXIST);
            }
            //获取当前节点上运行的pod列表
            List<DaemonSet> daemonSets = daemonSetsService.listDaemonSets(cluster);
            //获取daemonset占用节点资源
            double remainCpu = 0;
            double remainMemory = 0;
            Map<String, Double> remainResource = this.computeRemainResource(daemonSets, podList);
            remainCpu = remainResource.get(CommonConstant.CPU);
            remainMemory = remainResource.get(CommonConstant.MEMORY);
            //处理空对象
            if (Objects.isNull(quota2)) {
                quota2 = new QuotaDto();
            }
            double nodeCpu = (nodeDetail.getCpu().contains(CommonConstant.SMALLM) ? (Double.parseDouble(nodeDetail.getCpu().split(CommonConstant.SMALLM)[0]) / 1000) : Double.parseDouble(nodeDetail.getCpu()));
            double cpuQuota = cpu - (nodeCpu - remainCpu);
            quota2.setCpu((cpuQuota < 0 ? 0 : cpuQuota) + "");
            double nodeMemory = (Double.parseDouble(nodeDetail.getMemory()));
            double memoryQuota = memoryHard - (nodeMemory - remainMemory);
            quota2.setMemory((memoryQuota < 0 ? 0 : memoryQuota) + CommonConstant.GI);
            if (gpu != null) {
                Integer gpuQuota = gpu - (StringUtils.isBlank(nodeDetail.getGpu()) ? 0 : Integer.valueOf(nodeDetail.getGpu()));
                quota2.setGpu(String.valueOf(gpuQuota));
            }
            namespaceDto.setQuota(quota2);
        }
        //向K8S发送请求更新配额
        // 组装quota
        updateNamespaceQuota(namespaceDto, namespace, cluster);
    }

    //isRemoved 是否为更新状态
    private Map<String, String> getUpdateLabel(NamespaceDto namespaceDto, Boolean isRemoved) throws Exception {
        String namespace = namespaceDto.getName();
        String tenantId = namespaceDto.getTenantId();
        Map<String, String> labels = new HashMap<>();
        //查询私有分区独占节点
        String privateLabel = privatePartitionService.getPrivatePartitionLabel(tenantId, namespace);
        String[] appLabel = privateLabel.split(CommonConstant.EQUALITY_SIGN);
        if (appLabel.length > CommonConstant.NUM_ONE) {
            labels.put(appLabel[0], appLabel[CommonConstant.NUM_ONE]);
        } else {
            throw new MarsRuntimeException(ErrorCodeMessage.UNKNOWN);
        }
        if (!isRemoved) {
            labels.put(NodeTypeEnum.PRIVATE.getLabelKey(), NodeTypeEnum.PRIVATE.getLabelValue());
        }
        return labels;
    }

    /**
     * 添加租户分区独占主机
     *
     * @param namespaceDto
     * @throws Exception
     */
    @Override
    public void addPrivilegeNamespaceNodes(NamespaceDto namespaceDto) throws Exception {
        String namespace = namespaceDto.getName();
        Cluster cluster = this.namespaceLocalService.getClusterByNamespaceName(namespace);
        // 更新node节点状态
        Map<String, String> newLabels = new HashMap<String, String>();
        newLabels.put(NodeTypeEnum.PRIVATE.getLabelKey(), NodeTypeEnum.PRIVATE.getLabelValue());
        newLabels.put(CommonConstant.HARMONYCLOUD_TENANTNAME_NS, namespace);
        newLabels.put(CommonConstant.HARMONYCLOUD_TENANT_ID, namespaceDto.getTenantId());
        String[] nodes = namespaceDto.getNodeName().split(CommonConstant.COMMA);
        if (nodes.length <= 0) {
            throw new MarsRuntimeException(ErrorCodeMessage.NODENAME_NOT_BLANK);
        }

        //获取分区的配额
        Map<String, Object> namespaceQuota = this.getNamespaceQuota(namespace);
        List<String> memoryStatus = (List<String>) namespaceQuota.get(CommonConstant.MEMORY);
        List<String> cpuStatus = (List<String>) namespaceQuota.get(CommonConstant.CPU);
        List<String> gpuStatus = (List<String>) namespaceQuota.get(CommonConstant.GPU);
        Double cpu = Double.valueOf(cpuStatus.get(0));
        String hardUnit = namespaceQuota.get(CommonConstant.HARDTYPE).toString();
        double memoryHard = Double.valueOf(memoryStatus.get(0));
        memoryHard = transformMemoryToGb(memoryHard, hardUnit);
        Integer gpu = null;
        if(gpuStatus != null){
            gpu = Integer.valueOf(gpuStatus.get(0));
        }
        namespaceDto.setStorageClassQuotaList(this.getStorageQuota(namespaceQuota));
        for (String nodename : nodes) {
            //更新节点标签
            ActionReturnUtil addNodeLabels = nodeService.addNodeLabels(nodename, newLabels, cluster.getId());
            if ((Boolean) addNodeLabels.get(CommonConstant.SUCCESS) == false) {
                throw new MarsRuntimeException(ErrorCodeMessage.NODE_LABEL_CREATE_ERROR);
            }
            //处理节点资源使用量
            QuotaDto quota2 = namespaceDto.getQuota();
            Map node = nodeService.getNodeDetail(nodename, cluster);
            NodeDetailDto nodeDetail = (NodeDetailDto) node.get(CommonConstant.DATA);
            if (nodeDetail == null && StringUtils.isEmpty(nodeDetail.getCpu())) {
                throw new MarsRuntimeException(ErrorCodeMessage.NODE_NOT_EXIST);
            }
            //获取当前节点上运行的pod列表
            List<PodDto> podList = podService.PodList(nodename, cluster);
            List<DaemonSet> daemonSets = daemonSetsService.listDaemonSets(cluster);
            //获取daemonset占用节点资源
            double remainCpu = 0;
            double remainMemory = 0;
            Map<String, Double> remainResource = this.computeRemainResource(daemonSets, podList);
            remainCpu = remainResource.get(CommonConstant.CPU);
            remainMemory = remainResource.get(CommonConstant.MEMORY);
            //处理空对象
            if (Objects.isNull(quota2)) {
                quota2 = new QuotaDto();
            }
            Double oldCpuValue = (quota2.getCpu() == null) ? 0d : (quota2.getCpu().contains(CommonConstant.SMALLM) ? (Double.parseDouble(quota2.getCpu().split(CommonConstant.SMALLM)[0]) / 1000) : Double.parseDouble(quota2.getCpu()));
            quota2.setCpu(oldCpuValue + (nodeDetail.getCpu().contains(CommonConstant.SMALLM) ? (Double.parseDouble(nodeDetail.getCpu().split(CommonConstant.SMALLM)[0]) / 1000) : Double.parseDouble(nodeDetail.getCpu()) - remainCpu) + "");
            Double oldMemoryValue = quota2.getMemory() == null ? 0d : Double.parseDouble(quota2.getMemory().split(CommonConstant.GI)[0]);
            quota2.setMemory((Double.parseDouble(nodeDetail.getMemory()) + oldMemoryValue - remainMemory) + CommonConstant.GI);

            //gpu
            Integer oldGpuValue = (quota2.getGpu() == null) ? 0 : Integer.valueOf(quota2.getGpu());
            quota2.setGpu(String.valueOf(oldGpuValue + (nodeDetail.getGpu() == null ? 0 : Integer.valueOf(nodeDetail.getGpu()))));
            namespaceDto.setQuota(quota2);
        }
        //添加原有分区的配额
        // 保留一位小数 四舍五入
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setGroupingUsed(false);
        nf.setMaximumFractionDigits(CommonConstant.NUM_FIVE);
        nf.setRoundingMode(RoundingMode.HALF_UP);
        QuotaDto quota = namespaceDto.getQuota();
        Double oldCpuValue = (quota.getCpu() == null) ? 0d : (quota.getCpu().contains(CommonConstant.SMALLM) ? (Double.parseDouble(quota.getCpu().split(CommonConstant.SMALLM)[0]) / 1000) : Double.parseDouble(quota.getCpu()));
        double cpuHard = oldCpuValue + cpu;
        String formatCpu = nf.format(cpuHard % CommonConstant.NUM_ONE_DOUBLE == 0 ? (long) cpuHard : cpuHard);
        quota.setCpu(formatCpu + "");
        Double oldMemoryValue = quota.getMemory() == null ? 0d : Double.parseDouble(quota.getMemory().split(CommonConstant.GI)[0]);
        double memory = (memoryHard + oldMemoryValue);
        String formatMemory = nf.format(memory % CommonConstant.NUM_ONE_DOUBLE == 0 ? (long) memory : memory);
        quota.setMemory(formatMemory + CommonConstant.GI);

        //gpu
        if(quota.getGpu() != null || gpu != null){
            Integer gpuHard = (quota.getGpu() == null ? 0 : Integer.valueOf(quota.getGpu())) + (gpu == null ? 0 : gpu);
            quota.setGpu(String.valueOf(gpuHard));
        }
        namespaceDto.setQuota(quota);
        //向K8S发送请求更新配额
        // 组装quota
        updateNamespaceQuota(namespaceDto, namespace, cluster);
    }

    //向K8S发送请求更新配额
    // 组装quota
    private void updateNamespaceQuota(NamespaceDto namespaceDto, String namespace, Cluster cluster) throws Exception {
        Map<String, Object> bodys = generateQuotaBodys(namespaceDto);
        Map<String, Object> headers = new HashMap<>();
        headers.put(CommonConstant.CONTENT_TYPE, CommonConstant.APPLICATION_JSON);
        K8SClientResponse k8SClientResponse = resourceQuotaService.update(namespace, namespace + QUOTA, headers, bodys, HTTPMethod.PUT, cluster);
        if (!HttpStatusUtil.isSuccessStatus(k8SClientResponse.getStatus())) {
            logger.error("调用k8s接口更新namespace下quota失败", k8SClientResponse.getBody());
            throw new MarsRuntimeException(k8SClientResponse.getBody());
        }
    }

    @Override
    public Map<String, String> getNamespaceResourceRemainQuota(String namespace) throws Exception {
        Map<String, Object> namespaceQuota = this.getNamespaceQuota(namespace);
        List<String> memoryStatus = (List<String>) namespaceQuota.get(CommonConstant.MEMORY);
        List<String> cpuStatus = (List<String>) namespaceQuota.get(CommonConstant.CPU);
        String hardUnit = namespaceQuota.get(CommonConstant.HARDTYPE).toString();
        String usedUnit = namespaceQuota.get(CommonConstant.USEDTYPE).toString();
        double memoryHard = Double.valueOf(memoryStatus.get(0));
        double memoryUsed = Double.valueOf(memoryStatus.get(CommonConstant.NUM_ONE));
        memoryHard = transformMemoryUnit(memoryHard, hardUnit);
        memoryUsed = transformMemoryUnit(memoryUsed, usedUnit);
        BigDecimal cpu1 = new BigDecimal(Float.valueOf(cpuStatus.get(0))).setScale(CommonConstant.NUM_TWO, BigDecimal.ROUND_HALF_UP);
        BigDecimal cpu2 = new BigDecimal(Float.valueOf(cpuStatus.get(CommonConstant.NUM_ONE))).setScale(CommonConstant.NUM_TWO, BigDecimal.ROUND_HALF_UP);
        float cpuLeft = cpu1.subtract(cpu2).floatValue();
        double memLeft = memoryHard - memoryUsed;
        Map<String, String> result = new HashMap<>();
        result.put(CommonConstant.CPU, String.valueOf(cpuLeft));
        result.put(CommonConstant.MEMORY, String.valueOf(memLeft));

        if(namespaceQuota.get(Resource.STORAGECLASS) != null){
            Map<String, Object> storageMap = (Map<String, Object>)namespaceQuota.get(Resource.STORAGECLASS);
            for(String storage : storageMap.keySet()){
                List<String> list = (List<String>)storageMap.get(storage);
                if(!CollectionUtils.isEmpty(list) && list.size()>=2){
                    result.put(CommonConstant.STORAGE + CommonConstant.SLASH + storage ,String.valueOf(Integer.valueOf(list.get(0)) - Integer.valueOf(list.get(1))));
                }
            }
        }
        return result;
    }

    @Override
    public ActionReturnUtil checkResourceInTemplateDeploy(Map<String, Long> requireResource, Map<String, String> remainResource) throws Exception {
        float cpuNeed = Float.valueOf(requireResource.get("cpuNeed")) / CommonConstant.NUM_THOUSAND;
        double memoryNeed = Double.valueOf(requireResource.get("memoryNeed"));
        float cpuRemain = Float.valueOf(remainResource.get(CommonConstant.CPU));
        double memoryRemain = Double.valueOf(remainResource.get(CommonConstant.MEMORY));
        if (cpuRemain - cpuNeed < 0 || memoryRemain - memoryNeed < 0) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.NAMESPACE_RESOURCE_INSUFFICIENT);
        }
        this.checkStorageResource(requireResource, remainResource);
        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public void checkStorageResource(Map<String, Long> requireResource, Map<String, String> remainResource) {
        for(String key : requireResource.keySet()){
            if(key.contains(CommonConstant.STORAGE + CommonConstant.SLASH)){
                Long storageNeed = requireResource.get(key);
                if(remainResource.get(key) != null){
                    long storageRemain = Long.valueOf(remainResource.get(key));
                    if(storageRemain < storageNeed){
                        throw new MarsRuntimeException(ErrorCodeMessage.NAMESPACE_STORAGE_RESOURCE_INSUFFICIENT);
                    }
                }else{
                    throw new MarsRuntimeException(ErrorCodeMessage.NAMESPACE_STORAGE_RESOURCE_INSUFFICIENT);
                }
            }
        }
    }

    @Override
    public boolean checkTransferResource(List<NamespaceDto> namespaceDtos) throws Exception {
        for (NamespaceDto namespaceDto : namespaceDtos) {
            String namespace = namespaceDto.getName();
            String clusterId = namespaceDto.getClusterId();
            // 初始化判断1
            if (StringUtils.isEmpty(namespace)
                    || StringUtils.isEmpty(namespaceDto.getTenantId())
                    || Objects.isNull(clusterId)) {
                throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
            }
            // 初始化判断2
            if (!namespaceDto.isPrivate()
                    && (namespaceDto.getQuota() == null
                    || StringUtils.isEmpty(namespaceDto.getQuota().getCpu())
                    || StringUtils.isEmpty(namespaceDto.getQuota().getMemory()))) {
                throw new MarsRuntimeException(ErrorCodeMessage.NAMESPACE_QUOTA_NOT_BLANK);
            }
            // 初始化判断3
            if (namespaceDto.isPrivate() && StringUtils.isEmpty(namespaceDto.getNodeName())) {
                throw new MarsRuntimeException(ErrorCodeMessage.NODENAME_NOT_BLANK);
            }

            // 查询namespace详情
            Cluster cluster = clusterService.findClusterById(namespaceDto.getClusterId());
            //集群有效值查询
            if (Objects.isNull(cluster)){
                throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
            }
            //分区有效值判断
            NamespaceLocal namespaceByName = this.namespaceLocalService.getNamespaceByName(namespace);

            if (!Objects.isNull(namespaceByName)){
                throw new MarsRuntimeException(ErrorCodeMessage.NAMESPACE_EXIST,namespace,Boolean.TRUE);
            }
            //分区别名检查
            NamespaceLocal namespaceByAliasName = this.namespaceLocalService.getNamespace(namespaceDto.getAliasName(), namespaceDto.getTenantId());
            if (!Objects.isNull(namespaceByAliasName)){
                throw new MarsRuntimeException(ErrorCodeMessage.NAMESPACE_EXIST,namespaceDto.getAliasName(),Boolean.TRUE);
            }
            if(!namespaceDto.isPrivate()){
                //检查分区分配的配额是否超出可用值
                this.checkQuota(cluster, namespaceDto);
            }
        }
        return true;
    }

    /**
     * 内存单位转换成MB(统一单位)
     *
     * @param memory
     * @param unit
     * @return double
     * @throws Exception
     */
    private double transformMemoryUnit(double memory, String unit) throws Exception {
        //判断单位
        double memoryMb = memory;
        switch (unit) {
            case CommonConstant.GB:
                memoryMb = memory * CommonConstant.NUM_SIZE_MEMORY;
                break;
            case CommonConstant.MB:
                memoryMb = memory;
                break;
            case CommonConstant.TB:
                memoryMb = memory * CommonConstant.NUM_SIZE_MEMORY * CommonConstant.NUM_SIZE_MEMORY;
                break;
            case CommonConstant.PB:
                memoryMb = memory * CommonConstant.NUM_SIZE_MEMORY * CommonConstant.NUM_SIZE_MEMORY * CommonConstant.NUM_SIZE_MEMORY;
                break;
            case CommonConstant.KB:
                memoryMb = memory / CommonConstant.NUM_SIZE_MEMORY;
                break;
        }
        return memoryMb;
    }

    private double transformMemoryToGb(double memory, String type) throws Exception {
        //判断单位
        double memoryMb = memory;
        switch (type) {
            case CommonConstant.GB:
                memoryMb = memory;
                break;
            case CommonConstant.MB:
                memoryMb = memory / CommonConstant.NUM_SIZE_MEMORY;
                break;
            case CommonConstant.TB:
                memoryMb = memory * CommonConstant.NUM_SIZE_MEMORY;
                break;
            case CommonConstant.PB:
                memoryMb = memory * CommonConstant.NUM_SIZE_MEMORY * CommonConstant.NUM_SIZE_MEMORY;
                break;
            case CommonConstant.KB:
                memoryMb = memory / CommonConstant.NUM_SIZE_MEMORY / CommonConstant.NUM_SIZE_MEMORY;
                break;
        }
        return memoryMb;
    }

    private List<StorageClassQuotaDto> getStorageQuota(Map<String,Object> namespaceQuota){
        Map<String,List<Object>> storages = (Map)namespaceQuota.get("storageclasses");
        List<StorageClassQuotaDto> storageClassQuotaDtos = new ArrayList<>();
        for(String storageClass : storages.keySet()){
            List<Object> storageClassQuota = storages.get(storageClass);
            if(CollectionUtils.isEmpty(storageClassQuota)){
                continue;
            }
            if(storageClassQuota.get(0).toString().equals("0")){
                continue;
            }
            StorageClassQuotaDto storageClassQuotaDto = new StorageClassQuotaDto();
            storageClassQuotaDto.setName(storageClass);
            storageClassQuotaDto.setQuota(storageClassQuota.get(0).toString());
            storageClassQuotaDtos.add(storageClassQuotaDto);
        }
        return storageClassQuotaDtos;
    }
}
