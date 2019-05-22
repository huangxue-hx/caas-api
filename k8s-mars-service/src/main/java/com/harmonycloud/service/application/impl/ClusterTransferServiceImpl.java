package com.harmonycloud.service.application.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.*;
import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.dao.application.ConfigFileItemMapper;
import com.harmonycloud.dao.application.ConfigFileMapper;
import com.harmonycloud.dao.application.bean.ConfigFile;
import com.harmonycloud.dao.application.bean.ConfigFileItem;
import com.harmonycloud.dao.cluster.TransferBindDeployMapper;
import com.harmonycloud.dao.cluster.TransferBindNamespaceMapper;
import com.harmonycloud.dao.cluster.TransferClusterBackupMapper;
import com.harmonycloud.dao.cluster.TransferClusterMapper;
import com.harmonycloud.dao.cluster.bean.TransferBindDeploy;
import com.harmonycloud.dao.cluster.bean.TransferBindNamespace;
import com.harmonycloud.dao.cluster.bean.TransferCluster;
import com.harmonycloud.dao.cluster.bean.TransferClusterBackup;
import com.harmonycloud.dao.tenant.NamespaceLocalMapper;
import com.harmonycloud.dao.tenant.bean.NamespaceLocal;
import com.harmonycloud.dao.tenant.bean.TenantBinding;
import com.harmonycloud.dao.tenant.bean.TenantClusterQuota;
import com.harmonycloud.dto.application.*;
import com.harmonycloud.dto.cluster.*;
import com.harmonycloud.dto.tenant.ClusterQuotaDto;
import com.harmonycloud.dto.tenant.NamespaceDto;
import com.harmonycloud.dto.tenant.QuotaDto;
import com.harmonycloud.k8s.bean.*;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.service.*;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import com.harmonycloud.schedule.AsyncClusterTransfer;
import com.harmonycloud.service.application.ClusterTransferService;
import com.harmonycloud.service.application.DeploymentsService;
import com.harmonycloud.service.application.PersistentVolumeService;
import com.harmonycloud.service.application.RouterService;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.tenant.NamespaceService;
import com.harmonycloud.service.tenant.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import static com.harmonycloud.common.Constant.CommonConstant.*;
import static com.harmonycloud.service.platform.constant.Constant.TOPO_LABEL_KEY;

@Service
public class ClusterTransferServiceImpl implements ClusterTransferService {
	private static final int STATUS_SUCCESS = 1;
	private static final int STATUS_FAIL = 2;
	private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private ConfigFileMapper configFileMapper;

    @Autowired
    private ConfigFileItemMapper configFileItemMapper;

	@Autowired
	private RouterService routerService;
	
	@Autowired
	private DeploymentService dpService;

    @Autowired
    private NamespaceService namespaceService;
	
    @Autowired
    private PVCService pvcService;
	
    @Autowired
    private PvService pvService;
    
    @Autowired
    private PersistentVolumeService persistentVolumeService;
    
    @Autowired
    private ConfigmapService configMapService;
    
    @Autowired
    private ServicesService serviceService;
    
    @Autowired
    private StatefulSetService statefulSetService;

	@Autowired
	private NamespaceLocalMapper namespaceLocalMapper;
    @Autowired
    private NamespaceLocalService namespaceLocalService;

	@Autowired
	private ClusterService clusterService;

	@Autowired
	private TransferClusterMapper transferClusterMapper;

	@Autowired
	private ProjectService projectService;

	@Autowired
	private TenantService tenantService;

	@Autowired
	private TransferBindNamespaceMapper transferBindNamespaceMapper;

	@Autowired
	private TransferBindDeployMapper transferDeployMapper;

	@Autowired
	private TransferClusterBackupMapper transferClusterBackUpMapper;

	@Autowired
	private DeploymentsService deploymentsService;

	@Autowired
	private TprApplication tprApplication;

	@Autowired
	private TenantClusterQuotaService tenantClusterQuotaService;

	@Autowired
    private ReplicasetsService rsService;

	@Autowired
	private AsyncClusterTransfer asyncClusterTransfer;

    /**
     * 迁移对应的服务 在新集群上创建相同的服务 需要创建的有 ingress namespce configmap pv pvc deployment或者statefulset
     */
    @Override
	public ActionReturnUtil transferDeployService(DeploymentTransferDto deploymentTransferDto) throws Exception {
		return ActionReturnUtil.returnSuccess();
	}

	@Override
	public ActionReturnUtil transferCluster(List<ClusterTransferDto> clusterTransferDto) throws Exception {
        if (CollectionUtils.isEmpty(clusterTransferDto)) {
            throw new MarsRuntimeException(ErrorCodeMessage.INVALID_PARAMETER);
        }
		ActionReturnUtil actionReturn = checkNamespace(clusterTransferDto);
        if (!actionReturn.isSuccess()){
			return actionReturn;
		}

		Cluster sourceCluster = clusterService.findClusterById(clusterTransferDto.get(0).getCurrentClusterId());
		Cluster targetCluster = clusterService.findClusterById(clusterTransferDto.get(0).getTargetClusterId());
		TransferClusterBackup transferClusterBackup = generateTransferClusterBackup(clusterTransferDto);
		checkResource(sourceCluster, targetCluster, transferClusterBackup, clusterTransferDto);
		if(!Objects.isNull(transferClusterMapper.queryTransferClusterByParam(clusterTransferDto.get(0).getTenantId(),clusterTransferDto.get(0).getTargetClusterId()))){
			//TODO 增量续传
		}
		return ActionReturnUtil.returnSuccessWithData(excuteTransfer(transferClusterBackup, clusterTransferDto, sourceCluster, targetCluster, clusterTransferDto.get(0).isContinue()));
	}

	private TransferClusterBackup generateTransferClusterBackup(List<ClusterTransferDto> clusterTransferDto) {
		TransferClusterBackup transferClusterBackup = new TransferClusterBackup();
		transferClusterBackup.setCreateTime(new Date());
		transferClusterBackup.setDeployNum(transferDeployMapper.queryMaxNun(clusterTransferDto.get(0).getTenantId(),clusterTransferDto.get(0).getTargetClusterId()));

		transferClusterBackup.setNamespaceNum(transferBindNamespaceMapper.queryLastNamespaceNum(clusterTransferDto.get(0).getTenantId(),clusterTransferDto.get(0).getTargetClusterId()) == null ? 1 :transferBindNamespaceMapper.queryLastNamespaceNum(clusterTransferDto.get(0).getTenantId(),clusterTransferDto.get(0).getTargetClusterId()));
		transferClusterBackup.settenantId(clusterTransferDto.get(0).getTenantId());
		transferClusterBackup.setTransferClusterId(clusterTransferDto.get(0).getTargetClusterId());
		transferClusterBackup.setOldClusterId(clusterTransferDto.get(0).getCurrentClusterId());
		return transferClusterBackup;
	}

	/**
	 * 得到目标迁移集群的剩余容量
	 * @param targetClusterId
	 * @return
	 */
	private Map<String,Double> getClusterUsage(String targetClusterId) throws Exception {
		Map<String, Map<String, Object>> clusterAllocatedResources = clusterService.getClusterAllocatedResources(targetClusterId);
		Map<String, Object> clusterUseage = clusterAllocatedResources.get(targetClusterId);
		String clusterUnUsedMemory = clusterUseage.get("clusterMemoryAllocatedResources").toString();
		String clusterUnUsedCpu = clusterUseage.get("clusterCpuAllocatedResources").toString();
		Map<String,Double> clusterUsage = new HashMap<>();
		clusterUsage.put("clusterUnUsedMemory", Double.valueOf(clusterUnUsedMemory));
		clusterUsage.put("clusterUnUsedCpu", Double.valueOf(clusterUnUsedCpu));
		return clusterUsage;

	}

	/**
	 * 计算当前集群分区所需的容量
	 * @param clusterTransferDtos
	 * @return
	 */
	private Map<String,List<Double>> getNamespaceCpu(List<ClusterTransferDto> clusterTransferDtos) throws Exception {
		List<Double> cpu = new ArrayList<>();
		List<Double> memory = new ArrayList<>();
		Map<String,List<Double>> namespaceResource = new HashMap<>();
		for (ClusterTransferDto clusterTransferDto : clusterTransferDtos) {
			for (BindNameSpaceDto bindNameSpace : clusterTransferDto.getBindNameSpaceDtos()) {
				Map<String, Double> params = this.getQuota(bindNameSpace.getOldNameSpace(),
						clusterService.findClusterById(clusterTransferDto.getCurrentClusterId()));
				cpu.add(params.get("hardCpu"));
				memory.add(params.get("hardMemory"));
			}
		}
		namespaceResource.put("cpu", cpu);
		namespaceResource.put("memory",memory);
		return namespaceResource;
	}

	/**
	 * 分区容量转化
	 * @param namespace
	 * @param transferCluster
	 * @return
	 */
	private Map<String,Double> getQuota(String namespace,Cluster transferCluster) throws Exception {
		Map<String,Double> params = new HashMap<>();
		ResourceQuotaList quotaList = namespaceService.getResouceQuota(namespace,transferCluster);
		if (quotaList == null && quotaList.getItems() == null && quotaList.getItems().size() == 0) {
			throw new MarsRuntimeException(ErrorCodeMessage.NAMESPACE_MEMORY_TYPE_ERROR);
		}
		ResourceQuota resourceQuota = quotaList.getItems().get(0);
		ResourceQuotaSpec resourceQuotaSpec = resourceQuota.getSpec();
		ResourceQuotaStatus resourceQuotaStatus = resourceQuota.getStatus();
		Map<String, String> hard = (Map<String, String>) resourceQuotaSpec.getHard();
		//TODO 没用到
		Map<String, String> used = (Map<String, String>) resourceQuotaStatus.getUsed();
		String hardMemory = getResource(hard.get("memory"));
		String hardCpu = getResource(hard.get("cpu"));
		params.put("hardCpu",Double.valueOf(hardCpu));
		params.put("hardMemory", Double.valueOf(hardMemory));
		return params;
	}

	/**
	 * 比较集群剩余容量和namespace所需容量
	 * @param clusterTransferDtos
	 * @return
	 */
	private boolean compareUsage(Cluster sourceCluster, Cluster targetCluster, List<ClusterTransferDto> clusterTransferDtos) throws Exception {
		// 取出集群总配额信息 & 租户使用信息
		ClusterQuotaDto clusterQuotaDto = new ClusterQuotaDto();
		tenantClusterQuotaService.getClusterUsage(clusterTransferDtos.get(0).getTenantId(), targetCluster.getId(), clusterQuotaDto);
		// 查询租户的集群配额信息
		TenantClusterQuota targetTenantClusterQuota = tenantClusterQuotaService.getClusterQuotaByTenantIdAndClusterId(clusterTransferDtos.get(0).getTenantId(), targetCluster.getId());
		double unUsedCpu;
		double unUsedMemory;
		if (targetTenantClusterQuota == null || targetTenantClusterQuota.getCpuQuota() == 0D) {    // 如果该租户为分配过，则使用集群剩余容量
			unUsedCpu = clusterQuotaDto.getUnUsedCpu();
			unUsedMemory = clusterQuotaDto.getUnUsedMemory();
		} else {    // 已经分配过，则使用租户集群配额剩余容量
			unUsedCpu = targetTenantClusterQuota.getCpuQuota() - clusterQuotaDto.getUsedCpu();
			unUsedMemory = mathMemory(String.valueOf(targetTenantClusterQuota.getMemoryQuota())) - clusterQuotaDto.getUsedMemory();
		}

		double requiredCpu = 0.0;
		double requiredMemory = 0.0;

		for (ClusterTransferDto clusterTransferDto : clusterTransferDtos) {
			List<BindNameSpaceDto> bindNameSpaceDtos = clusterTransferDto.getBindNameSpaceDtos();
			for (BindNameSpaceDto bindNameSpaceDto : bindNameSpaceDtos) {
				NamespaceLocal namespaceLocal = namespaceLocalService.getNamespaceByName(bindNameSpaceDto.getName());
				// 新的分区不存在则需要创建，新分区配额大小为原分区已经使用的量，需要校验新建分区配额是否足够
				if (namespaceLocal == null) {
					if (unUsedCpu <= 0D || unUsedMemory <= 0D) {    // 新建时，配额小于等于0直接返回不足
						return false;
					}

					Map<String,Object> detail = namespaceService.getNamespaceQuota(bindNameSpaceDto.getOldNameSpace());
					List<String> usedCpu = (List<String>) detail.get("cpu");
					List<String> usedMemory = (List<String>) detail.get("memory");
					requiredCpu += Double.parseDouble(usedCpu.get(0));
					if (requiredCpu > unUsedCpu) {
						return false;
					}
					if (detail.get("hardType") != null && (detail.get("hardType").toString().equalsIgnoreCase(MB)
					    || detail.get("hardType").toString().equalsIgnoreCase(MI))) {
						requiredMemory += mathMemory(usedMemory.get(0));
					} else {
						requiredMemory += Double.parseDouble(usedMemory.get(0));
					}
					if (requiredMemory > unUsedMemory) {
						return false;
					}
				} else {
					//已经存在分区判断剩余的资源是否足够创建迁移过来的服务
//					double requiredNsCpu = 0D;
//					double requiredNsMemory = 0D;
//					List<DeploymentDto> deploymentDtos = bindNameSpaceDto.getDeploymentDto();
//					for (DeploymentDto deploymentDto : deploymentDtos) {
//						if (deploymentDto.getServiceType().equals(Constant.DEPLOYMENT)) {
//							// 获取特定的deployment
//							K8SClientResponse depRes = dpService.doSpecifyDeployment(bindNameSpaceDto.getOldNameSpace(), deploymentDto.getDeployName(), null, null, HTTPMethod.GET, sourceCluster);
//							if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus())) {
//								throw new MarsRuntimeException(DictEnum.APPLICATION.phrase(), ErrorCodeMessage.QUERY_FAIL);
//							}
//							Deployment dep = JsonUtil.jsonToPojo(depRes.getBody(), Deployment.class);
////							List<Container> containers = dep.getSpec().getTemplate().getSpec().getContainers();
////							if (CollectionUtils.isNotEmpty(containers)) {
////								for (Container c : containers) {
////									if (c.getResources() != null && c.getResources().getRequests() != null) {
////										Map<String, Object> request = (Map<String, Object>) c.getResources().getRequests();
////										String cpu = (String) request.get(CommonConstant.CPU);
////										String memory = (String) request.get(CommonConstant.MEMORY);
////										if (cpu.contains(CommonConstant.SMALLM)) {
////											requiredNsCpu += Double.parseDouble(cpu.split(CommonConstant.SMALLM)[0]) / CommonConstant.NUM_THOUSAND;
////										} else {
////											requiredNsCpu += Double.parseDouble(cpu);
////										}
////										if (requiredNsCpu > unUsedCpu) {
////											return false;
////										}
////
////										if (memory.contains(CommonConstant.SMALLM)) {
////											requiredNsMemory += mathMemory(memory.split(CommonConstant.SMALLM)[0]);
////										} else if (memory.contains(CommonConstant.MI)) {
////											requiredNsMemory += mathMemory(memory.split(CommonConstant.MI)[0]);
////										} else if (memory.contains(CommonConstant.SMALLG)) {
////											requiredNsMemory += Double.parseDouble(memory.split(CommonConstant.SMALLG)[0]);
////										} else if (memory.contains(CommonConstant.GI)) {
////											requiredNsMemory += Double.parseDouble(memory.split(CommonConstant.GI)[0]);
////										}
////										if (requiredNsMemory > unUsedMemory) {
////											return false;
////										}
////									}
////								}
////							}
//						}
//					}
				}
			}

		}
		return true;
	}

	private List<TransferBindNamespace> addBindNamespaceData(List<ClusterTransferDto> clusterTransferDtos) throws Exception{
		List<TransferBindNamespace> transferBindNamespaces = new ArrayList<>();
		if(CollectionUtils.isEmpty(clusterTransferDtos)){
			throw new MarsRuntimeException(ErrorCodeMessage.TRANSFER_CLUSTER_ERROR);
		}
		for (ClusterTransferDto clusterTransferDto : clusterTransferDtos) {
			for (BindNameSpaceDto bindNameSpace : clusterTransferDto.getBindNameSpaceDtos()) {
				TransferBindNamespace transferBindNamespace = generateBindNamespace(clusterTransferDto, bindNameSpace);
				transferBindNamespaces.add(transferBindNamespace);
			}
		}
		return transferBindNamespaces;
	}

	private String generateDefaultNamespaceName(String tenantId) throws Exception {
		String namespaceName = namespaceLocalService.getNamespaceListByTenantId(tenantId).get(0).getNamespaceName();
		String projectName = projectService.listTenantProjectByTenantid(tenantId).get(0).getProjectName();
		return namespaceName+projectName;
	}

	private TransferBindNamespace generateBindNamespace(ClusterTransferDto clusterTransferDto, BindNameSpaceDto bindNameSpaceDto) throws Exception {
		TransferBindNamespace transferBindNamespace = new TransferBindNamespace();
		transferBindNamespace.setClusterId(clusterTransferDto.getTargetClusterId());
		transferBindNamespace.setCreateNamespace(StringUtils.isBlank(bindNameSpaceDto.getName())?generateDefaultNamespaceName(clusterTransferDto.getTenantId()):bindNameSpaceDto.getName());
		transferBindNamespace.setCreateTime(new Date());
		transferBindNamespace.setCurrentNamespace(bindNameSpaceDto.getOldNameSpace());
		transferBindNamespace.setErrMsg(StringUtils.EMPTY);
		transferBindNamespace.setIsDefault(StringUtils.isBlank(bindNameSpaceDto.getName())?Byte.valueOf(Constant.START):Byte.valueOf(Constant.STOP));
		transferBindNamespace.setIsDelete((byte)0);
		transferBindNamespace.setNamespaceNum(0);
		transferBindNamespace.setStatus(0);
		transferBindNamespace.setTenantId(clusterTransferDto.getTenantId());
		return transferBindNamespace;
	}

	private ActionReturnUtil checkNamespace(List<ClusterTransferDto> clusterTransferDtos) throws MarsRuntimeException {

		List<BindNameSpaceDto>  namespaceDtos = clusterTransferDtos.get(0).getBindNameSpaceDtos();

		for (BindNameSpaceDto namespaceDto : namespaceDtos) {
		    //根据别名查英文名和集群id
		    Map a = namespaceLocalMapper.selectNameByalias_name(namespaceDto.getAliasName());
            //根据英文名查别名和集群id
		    Map b = namespaceLocalMapper.selectAliasNameByName(namespaceDto.getName());
		    //数据库都没有，或者两个都有且都是一个集群且还是目标集群的放过
			if (a == null && b == null  ) {
                continue;
			}
			if (null != a && b!= null && a.get("clusterId").equals(b.get("clusterId")) &&
                    a.get("clusterId").equals(clusterTransferDtos.get(0).getTargetClusterId())){
               continue;
            }
			return  ActionReturnUtil.returnErrorWithData(DictEnum.NAMESPACE_NAME.phrase() + " " + namespaceDto.getName() + " - " + namespaceDto.getAliasName(), ErrorCodeMessage.EXIST);
		}
        return ActionReturnUtil.returnSuccess();
	}

	private List<NamespaceDto> packageNamespaceDto(List<ClusterTransferDto> clusterTransferDtos) throws Exception {
		List<NamespaceDto> namespaceDtos = new ArrayList<>();
		if(CollectionUtils.isEmpty(clusterTransferDtos)){
			throw new MarsRuntimeException(ErrorCodeMessage.TRANSFER_CLUSTER_ERROR);
		}
		for (ClusterTransferDto clusterTransferDto : clusterTransferDtos) {
			for (BindNameSpaceDto bindNameSpace : clusterTransferDto.getBindNameSpaceDtos()) {
				namespaceDtos.add(generate(bindNameSpace, clusterTransferDto));
			}
		}
		return namespaceDtos;
	}

	private NamespaceDto generate(BindNameSpaceDto bindNameSpaceDto,ClusterTransferDto clusterTransferDto) throws Exception {
		Map<String,Object> detail = namespaceService.getNamespaceQuota(bindNameSpaceDto.getOldNameSpace());
		if(detail==null){
			throw new MarsRuntimeException(ErrorCodeMessage.NAMESPACE_NOT_FOUND);
		}
		//TODO 不应该为使用的，改为总配额
		NamespaceDto namespaceDto = new NamespaceDto();
		namespaceDto.setAliasName(bindNameSpaceDto.getAliasName());
		namespaceDto.setName(bindNameSpaceDto.getName());
		namespaceDto.setTenantId(clusterTransferDto.getTenantId());
		QuotaDto quota = new QuotaDto();
		namespaceDto.setClusterId(clusterTransferDto.getTargetClusterId());
		List<String> usedCpu = (List<String>) detail.get("cpu");
		List<String> usedMemory = (List<String>) detail.get("memory");
		String memoryHardType = "Gi";
		if (detail.get("hardType") != null) {
			memoryHardType = detail.get("hardType").toString();
			if (memoryHardType.equalsIgnoreCase("MB")){
				memoryHardType = "Mi";
			}
			if (memoryHardType.equalsIgnoreCase("GB")){
				memoryHardType = "Gi";
			}
		}
		quota.setCpu(usedCpu.get(0));
		quota.setMemory(usedMemory.get(0) + memoryHardType);
		namespaceDto.setQuota(quota);
		/*namespaceDto.setStorageClassQuotaList(detail.get(""));*/
		return namespaceDto;
	}

	private Map<String, List<ErrorNamespaceDto>> createNamespace(List<NamespaceDto> namespaceDtos, Cluster cluster) throws Exception {
		Map<String,List<ErrorNamespaceDto>> updateNamespace = new HashMap<>();
		List<ErrorNamespaceDto>  successList = new ArrayList<>();
		List<ErrorNamespaceDto>  errorList = new ArrayList<>();
		List<Integer> indexs= new ArrayList<>();
		for (NamespaceDto namespaceDto : namespaceDtos) {
			ErrorNamespaceDto errorNamespaceDto =new ErrorNamespaceDto();
			//分区中文名唯一校验
			//根据别名查英文名 如果英文名不存在 或 和新英文名不一致  returen
/*			String name = namespaceLocalMapper.selectNameByalias_name(namespaceDto.getAliasName());
			String aliasName = namespaceLocalMapper.selectAliasNameByName(namespaceDto.getName());
			//
			if (name == null ){ //name为空，aliame不为空的直接返回
				if (aliasName != null){
					//gg
					logger.error("请检查分区名与分区别名{}>>>{}",namespaceDto.getName(),namespaceDto.getAliasName());
					errorNamespaceDto = new ErrorNamespaceDto();
					errorNamespaceDto.setErrMsg("请检查分区名与分区别名" + namespaceDto.getName()+ " - "+namespaceDto.getAliasName());
					errorNamespaceDto.setNamespace(namespaceDto.getName());
					errorList.add(errorNamespaceDto);
					continue;
				}
			}else{  //name 不为空,aliame不为空且需要一致，否则返回
				if (aliasName == null ||  !aliasName.equals(namespaceDto.getAliasName())) {
					logger.error("请检查分区名与分区别名{}>>>{}",namespaceDto.getName(),namespaceDto.getAliasName());
					errorNamespaceDto = new ErrorNamespaceDto();
					errorNamespaceDto.setErrMsg("请检查分区名与分区别名" + namespaceDto.getName()+ " - "+namespaceDto.getAliasName());
					errorNamespaceDto.setNamespace(namespaceDto.getName());
					errorList.add(errorNamespaceDto);
					continue;
				}
			}*/
			//字段长度、必填可放在接口入口处校验
			if (StringUtils.isEmpty(namespaceDto.getName()) || namespaceDto.getName().indexOf(CommonConstant.LINE) < 0) {
				errorNamespaceDto = new ErrorNamespaceDto();
				errorNamespaceDto.setErrMsg(ErrorCodeMessage.NAMESPACE_NOT_BLANK.getReasonChPhrase());
				errorNamespaceDto.setNamespace(namespaceDto.getName());
				errorList.add(errorNamespaceDto);
				continue;
			}

			//组装k8s数据结构
			ObjectMeta objectMeta = new ObjectMeta();
			objectMeta.setAnnotations(this.getAnnotations(namespaceDto));
			objectMeta.setLabels(this.getLables(namespaceDto));
			objectMeta.setName(namespaceDto.getName());
			Map<String, Object> bodys = new HashMap<>();
			bodys.put(CommonConstant.KIND, CommonConstant.NAMESPACE);
			bodys.put(CommonConstant.METADATA, objectMeta);
			Map<String, Object> headers = new HashMap<>();
			headers.put(CommonConstant.CONTENT_TYPE, CommonConstant.APPLICATION_JSON);
			//向k8s创建分区
			K8SClientResponse k8SClientResponse = this.create(headers, bodys, HTTPMethod.POST, cluster);
			Map<String, Object> stringObjectMap = JsonUtil.convertJsonToMap(k8SClientResponse.getBody());
			if (!HttpStatusUtil.isSuccessStatus(k8SClientResponse.getStatus())) {
				if ("409".equals(stringObjectMap.get("code").toString())){
					logger.warn("调用k8s接口迁移分区{}已存在，message：{}：" ,namespaceDto.getName(),k8SClientResponse.getBody());
				}else {
					errorNamespaceDto = new ErrorNamespaceDto();
					logger.error("调用k8s接口迁移namespace失败，错误消息：" + k8SClientResponse.getBody());
					Object message = stringObjectMap.get("message");
					if (!Objects.isNull(message) && message.toString().contains("object is being deleted")){
						errorNamespaceDto.setNamespace(namespaceDto.getName());
						//TODO 改提示
						errorNamespaceDto.setErrMsg(ErrorCodeMessage.NAMESPACE_CREATE_ERROR_DELETED.getReasonChPhrase());
						errorList.add(errorNamespaceDto);
						//失败进行回滚
						namespaceService.deleteNamespace(namespaceDto.getTenantId(), namespaceDto.getName());
						continue;
					}
					errorNamespaceDto.setNamespace(namespaceDto.getName());
					errorNamespaceDto.setErrMsg(ErrorCodeMessage.NAMESPACE_CREATE_ERROR.getReasonChPhrase());
					int index = successList.indexOf(errorNamespaceDto);
					indexs.add(index);
					errorList.add(errorNamespaceDto);
				}
			}else {
				// 3.创建resource quota
				ActionReturnUtil createResult = namespaceService.createQuota(namespaceDto, cluster);

				if (!((Boolean) createResult.get(CommonConstant.SUCCESS))) {
				    logger.info("创建配额失败：",createResult.getData());
					// 失败回滚
					namespaceService.deleteNamespace(namespaceDto.getTenantId(), namespaceDto.getName());
                    logger.info("创建配额失败后删除namespaces");
					continue;
				}
				//保存到本地数据库
				errorNamespaceDto = this.createLocalNamespace(namespaceDto);
			}
			errorNamespaceDto.setNamespace(namespaceDto.getName());
			successList.add(errorNamespaceDto);

		}
		updateNamespace.put(Constant.TRANSFER_NAMESPACE_SUCCESS,successList);
		updateNamespace.put(Constant.TRANSFER_NAMESPACE_ERROR,errorList);
		logger.info("迁移分区完成");
		return updateNamespace;
	}
	//创建本地分区
	private ErrorNamespaceDto createLocalNamespace(NamespaceDto namespaceDto) throws Exception {
		NamespaceLocal namespaceLocal = new NamespaceLocal();
		//组装分区参数
		namespaceLocal.setNamespaceId(StringUtil.getId());
		namespaceLocal.setNamespaceName(namespaceDto.getName());
		namespaceLocal.setClusterId(namespaceDto.getClusterId());
		namespaceLocal.setIsPrivate(namespaceDto.isPrivate());
		namespaceLocal.setTenantId(namespaceDto.getTenantId());
		namespaceLocal.setCreateTime(DateUtil.getCurrentUtcTime());
		namespaceLocal.setAliasName(namespaceDto.getAliasName());
		//创建本地分区
		ErrorNamespaceDto errorNamespaceDto = this.namespaceLocalService.createTransferNamespace(namespaceLocal);
		namespaceService.updateShareNode(namespaceDto);
		return errorNamespaceDto;
	}

	private Map<String, Object> getAnnotations(NamespaceDto namespaceDto) {
		Map<String, Object> annotations = new HashMap<>();
		annotations.put(CommonConstant.NEPHELE_ANNOTATION, namespaceDto.getAnnotation());
		return annotations;
	}


	private Map<String, Object> getLables(NamespaceDto namespaceDto) throws Exception {
		Map<String, Object> lables = new HashMap<>();
		TenantBinding tenantByTenantid = tenantService.getTenantByTenantid(namespaceDto.getTenantId());
		lables.put("nephele_tenant", tenantByTenantid.getTenantName());
		lables.put("nephele_tenantid", namespaceDto.getTenantId());
		if (namespaceDto.isPrivate()) {
			lables.put("isPrivate", "1");// 私有
		} else {
			lables.put("isPrivate", "0");// 共享
		}
		return lables;
	}

	public K8SClientResponse create(Map<String, Object> headers, Map<String, Object> bodys, String method,Cluster cluster) {
		K8SURL k8SURL = new K8SURL();
		k8SURL.setResource(Resource.NAMESPACE);
		K8SClientResponse response = new K8sMachineClient().exec(k8SURL, method, headers, bodys,cluster);
		return response;
	}

	private Map<String,List<ErrorNamespaceDto>> updateBindNamespace(ClusterTransferDto clusterTransferDtos, List<NamespaceDto> namespaceDtos, Cluster currentCluster ,Cluster sourceCluster) throws Exception {
		//创建该租户在新集群的配额
		TenantClusterQuota tenantClusterQuota = tenantClusterQuotaService.getClusterQuotaByTenantIdAndClusterId(namespaceDtos.get(0).getTenantId(),currentCluster.getId());
        //TODO 创建集群配额应该改为客户手动创，多次从不同集群迁入某个集群会导致不知道该怎样设置配额大小
		if (tenantClusterQuota == null){
			tenantClusterQuota = tenantClusterQuotaService.getClusterQuotaByTenantIdAndClusterId(namespaceDtos.get(0).getTenantId(),sourceCluster.getId());
			//TODO 负载均衡 暂时不迁移
			tenantClusterQuota.setIcNames(null);
			tenantClusterQuota.setClusterName(currentCluster.getName());
			tenantClusterQuota.setClusterId(currentCluster.getId());
			tenantClusterQuota.setId(null);
			tenantClusterQuota.setCreateTime(new Date());
			tenantClusterQuota.setUpdateTime(null);
			try{
				tenantClusterQuotaService.createClusterQuota(tenantClusterQuota);
			}catch (Exception e){
				//TODO 后期放到数据库错误记录中
				logger.error("创建集群配额失败，tenantClusterQuota：{}，error信息：{}", tenantClusterQuota.toString(), e);
			}
		}else {
			if ("0.0".equals(tenantClusterQuota.getCpuQuota().toString())){
				TenantClusterQuota quota = tenantClusterQuotaService.getClusterQuotaByTenantIdAndClusterId(namespaceDtos.get(0).getTenantId(),sourceCluster.getId());
				//TODO 负载均衡 暂时不迁移
				quota.setIcNames(null);
				quota.setClusterName(currentCluster.getName());
				quota.setClusterId(currentCluster.getId());
				quota.setUpdateTime(new Date());
				quota.setId(tenantClusterQuota.getId());
				try{
					tenantClusterQuotaService.updateClusterQuota(quota);
				}catch (Exception e){
					//TODO 后期放到数据库错误记录中
					logger.error("创建集群配额失败，tenantClusterQuota：{}，error信息：{}", tenantClusterQuota.toString(), e);
				}
			}else {
				//TODO 后期放到数据库错误记录中
				logger.warn("创建集群配额失败,配额已存在，TenantClusterQuota：{}", tenantClusterQuota.toString());
			}
		}
		//创建k8s 分区，并返回创建正确和错误的信息
		Map<String,List<ErrorNamespaceDto>> param = createNamespace(namespaceDtos, currentCluster);
/*		断点续传
		List<ErrorNamespaceDto> successNamespaceDtos = param.get(Constant.TRANSFER_NAMESPACE_SUCCESS);
		List<ErrorNamespaceDto> errorNamespaceDtos = param.get(Constant.TRANSFER_NAMESPACE_ERROR);
		if (!errorNamespaceDtos.isEmpty()){
			List errorList = namespaceList(clusterTransferDtos,errorNamespaceDtos, false);
			if (!errorList.isEmpty()){
				transferBindNamespaceMapper.updateErrorListNamespace(errorList);
			}
		}
		if (!successNamespaceDtos.isEmpty()){
			List successList = namespaceList(clusterTransferDtos,successNamespaceDtos, true);
			if (!successList.isEmpty()){
				transferBindNamespaceMapper.updateSuccessListNamespace(successList);
			}
		}*/
		return param;
	}

	private List<TransferBindNamespace> namespaceList(ClusterTransferDto clusterTransferDtos,List<ErrorNamespaceDto> namespaceDtos,boolean isSuccess) throws Exception {
		List<TransferBindNamespace> list = new ArrayList<>();
		for (ErrorNamespaceDto namespaceDto : namespaceDtos) {
			TransferBindNamespace transferBindNamespace = new TransferBindNamespace();
			if(isSuccess){
				transferBindNamespace.setStatus(CommonConstant.NUM_ONE);
				transferBindNamespace.setNamespaceNum(transferBindNamespaceMapper.queryLastNamespaceNum(clusterTransferDtos.getTenantId(),clusterTransferDtos.getTargetClusterId())+1);
				transferBindNamespace.setCreateNamespace(namespaceDto.getNamespace());
			}else{
				transferBindNamespace.setErrMsg(namespaceDto.getErrMsg());
				transferBindNamespace.setCreateNamespace(namespaceDto.getNamespace());
				transferBindNamespace.setStatus(CommonConstant.NUM_ZERO);
			}
			list.add(transferBindNamespace);
		}
		return list;
	}

	/**
	 * 创建ingress
	 * @param deploymentTransferDto
	 * @throws IntrospectionException
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws MarsRuntimeException 
	 */
	private ErrDeployDto createIngress(DeploymentTransferDto deploymentTransferDto,Cluster sourceCluster) throws Exception{
		ActionReturnUtil actionReturnUtil = routerService.listExposedRouterWithIngressAndNginx(deploymentTransferDto.getCurrentNameSpace(),deploymentTransferDto.getCurrentDeployName(),deploymentTransferDto.getProjectId());
		List<Map<String, Object>> routerList = (List<Map<String, Object>>) actionReturnUtil.getData();

		List<TcpRuleDto> rules = new ArrayList<>();
		for (Map<String, Object> map : routerList) {

			if(map.get("type").equals(Constant.PROTOCOL_TCP)||map.get("type").equals(Constant.PROTOCOL_UDP)){
				ErrDeployDto errDeployDto = createTcpIngress(deploymentTransferDto, map, rules);
				if (errDeployDto != null) {
					return errDeployDto;
				}
			}
			if(map.get("type").equals(Constant.PROTOCOL_HTTP)){
				ErrDeployDto errDeployDto = createHttpIngress(deploymentTransferDto, map, sourceCluster);
				if (errDeployDto != null) {
					return errDeployDto;
				}
			}
		}
		return null;
	}
	 
	 
	 private String splitHostname(String hostname){
		 String host = "";
		 if(StringUtils.isNotEmpty(hostname)){
		 	if (hostname.indexOf("/") > 0) {
				host = hostname.split("/")[0];
			} else {
				host = hostname.split(":")[0];
			}

		 }
		 return host;
	 }
	 
	 private String splitPath(String hostname){
		 String path = "";
		 StringJoiner builder = new StringJoiner("/");
		 if(StringUtils.isNotEmpty(hostname)){
			 List<String> list = Arrays.asList(hostname.split("/"));
			 for (int i = 1; i < list.size(); i++) {
				builder.add(list.get(i));
			}
		 }
		 return "/"+path;
	 }

	
	/**
	 * 得到内存或者cpu信息 1h=1024m 
	 * @param resource
	 * @return
	 */
	private  String getResource(String resource){
		if(resource!=null){
		  if(resource.contains("Gi")){
			  return String.valueOf(Long.valueOf(resource.substring(0,resource.indexOf("G")))*1024);
		  }
		  if(resource.contains("Mi")){
			  return resource.substring(0,resource.indexOf("M"));
		  }
		  if(resource.contains("m")){
			  return resource.substring(0,resource.indexOf("m"));
		  }
		  if(Double.valueOf(resource)>99){
			  return resource;
		  }
		  return String.valueOf(Double.valueOf(resource)*1024);
		}
		return null;
	}
	

	/**
	 * 创建或者更新pvc
	 * @param deploymentTransferDto
	 * @param targetCluster
	 * @param sourceCluster
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws IntrospectionException
	 */
	private ErrDeployDto createPVC(DeploymentTransferDto deploymentTransferDto,Cluster targetCluster,Cluster sourceCluster, List<ErrDeployDto> errDeployDtos) throws Exception {
		K8SClientResponse k8sClientResponse = getPVC(deploymentTransferDto.getCurrentDeployName(),deploymentTransferDto.getCurrentNameSpace(), sourceCluster);
		List<PersistentVolumeClaim> persistentVolumeClaims = null;
		//如果返回404代表当前服务并未创建pvc 则可向下创建
		ErrDeployDto errDeployDto = new ErrDeployDto();
		if (k8sClientResponse.getStatus() == Constant.HTTP_404){
			errDeployDto.setDeployName(deploymentTransferDto.getCurrentDeployName());
			logger.error("未获取到pvc，deploymentTransferDto{}", deploymentTransferDto.toString());
			errDeployDto.setErrMsg("创建pvc失败");
			return errDeployDto;
		}
		PersistentVolumeClaimList persistentVolumeClaim = K8SClient.converToBean(k8sClientResponse, PersistentVolumeClaimList.class);
		if(!persistentVolumeClaim.getItems().isEmpty()){
			persistentVolumeClaims = persistentVolumeClaim.getItems();
			for ( PersistentVolumeClaim pvc :persistentVolumeClaims ){
				PersistentVolumeClaim transferPersistentVolumeClaim = null;
				try{
					K8SClientResponse pvcRes = pvcService.getPVC(pvc.getMetadata().getName(), deploymentTransferDto.getNamespace(), targetCluster);
					checkK8SClientResponse(pvcRes,deploymentTransferDto.getCurrentDeployName());
					transferPersistentVolumeClaim = K8SClient.converToBean(pvcRes, PersistentVolumeClaim.class);
				}catch (Exception e){
					logger.warn("集群迁移在新集群未查到pvc{}",e);
				}
				
				if(null != transferPersistentVolumeClaim){
					logger.warn("集群迁移pvc已存在{}",transferPersistentVolumeClaim.toString());
					try {
						errDeployDtos.add(updatePVC(replacePersistentVolumeClaim(transferPersistentVolumeClaim, deploymentTransferDto ,"update"), targetCluster));
					}catch (Exception e) {
						logger.error("更新pvc错误，{}", transferPersistentVolumeClaim, e);
					}

				}else{
					errDeployDtos.add(createPVC(replacePersistentVolumeClaim(pvc, deploymentTransferDto, "create"), targetCluster, sourceCluster,deploymentTransferDto.getNamespace(),deploymentTransferDto.getCurrentDeployName(),errDeployDto));
				}
			}
		}
		return null;
	}
	
	/**
	 * 更新pv pvc
	 * @param persistentVolumeClaim
	 * @param targetCluster
	 */
	private ErrDeployDto updatePVC(PersistentVolumeClaim persistentVolumeClaim,Cluster targetCluster) throws Exception {
		/*PersistentVolume persistentVolume = pvService.getPvByName(persistentVolumeClaim.getSpec().getVolumeName(),targetCluster);
		ErrDeployDto errDeployDto = persistentVolumeService.transferPV(persistentVolume, targetCluster,deployName);
		if(!Objects.isNull(errDeployDto)){
			return errDeployDto;
		}*/
		pvcService.updatePvcByName(persistentVolumeClaim, targetCluster);
		return null;
	}

	/**
	 * 创建pvc
	 * @param persistentVolumeClaim
	 * @param targetCluster
	 * @param namespace
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws IntrospectionException
	 */
	private ErrDeployDto createPVC(PersistentVolumeClaim persistentVolumeClaim,Cluster targetCluster,Cluster sourceCluster,String namespace,String deployName,ErrDeployDto errDeployDto) {
		try {
			Map<String, Object> bodys = CollectionUtil.transBean2Map(persistentVolumeClaim);
			K8SURL url = new K8SURL();
			url.setNamespace(namespace).setResource(Resource.PERSISTENTVOLUMECLAIM);
			K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.POST, generateHeader(), bodys, targetCluster);
			if(409 == response.getStatus()){
				logger.warn("集群迁移pvc:"+persistentVolumeClaim.getMetadata().getName()+"已存在");
			}else {
				errDeployDto = checkK8SClientResponse(response,deployName);
				if(!Objects.isNull(errDeployDto)){
					return errDeployDto;
				}
			}

			PersistentVolume persistentVolume = pvService.getPvByName(persistentVolumeClaim.getSpec().getVolumeName(),sourceCluster);
			persistentVolumeService.transferPV(persistentVolume, targetCluster,deployName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 创建或更新configmap
	 * @param deploymentTransferDto
	 * @param currentCluster
	 * @param sourceCluster
	 */
	private ErrDeployDto createOrUpdateConfigMap(DeploymentTransferDto deploymentTransferDto,Cluster currentCluster,Cluster sourceCluster) throws Exception {
		K8SClientResponse response = configMapService.doSepcifyConfigmap(deploymentTransferDto.getCurrentNameSpace(), deploymentTransferDto.getCurrentDeployName(),sourceCluster);
		if (HttpStatusUtil.isSuccessStatus(response.getStatus())){
			//如果返回404代表当前服务并未创建configmap 则可向下创建
			ErrDeployDto errDeployDto = new ErrDeployDto();
			ConfigMap configMap = JsonUtil.jsonToPojo(response.getBody(), ConfigMap.class);
			if(configMap!=null){
				K8SURL url = new K8SURL();
				url.setNamespace(deploymentTransferDto.getNamespace()).setResource(Resource.CONFIGMAP);
				Map<String, Object> bodys = new HashMap<String, Object>();
				Map<String, Object> meta = new HashMap<String, Object>();
				meta.put("namespace", deploymentTransferDto.getNamespace());
				meta.put("name", configMap.getMetadata().getName());
				Map<String, Object> label = new HashMap<String, Object>();
				label.put("app", deploymentTransferDto.getCurrentDeployName());
				meta.put("labels", label);
				bodys.put("metadata", meta);
				Map<String, Object> data = new HashMap<String, Object>();
				data.put("config.json", com.alibaba.fastjson.JSONObject.toJSON(configMap));
				bodys.put("data", data);
				K8SClientResponse result = new K8sMachineClient().exec(url, HTTPMethod.POST, generateHeader(), bodys, currentCluster);
				errDeployDto = checkK8SClientResponse(result,deploymentTransferDto.getCurrentDeployName());
				if(!Objects.isNull(errDeployDto)){
					return errDeployDto;
				}
			}
		}
		return null;
	}

	/**
	 * 创建deployment 创建前判断当前集群是否有deployment 有则更新
	 * @param deploymentTransferDto
	 * @param currentCluster
	 * @param sourceCluster
	 * @return
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws IntrospectionException
	 */
	private ErrDeployDto createDeployment(DeploymentTransferDto deploymentTransferDto,Cluster targetCluster,
										  Cluster sourceCluster,ErrDeployDto errDeployDto,String deployName) throws Exception{
		K8SClientResponse response = null;
		Deployment deployment = null;
		response = dpService.doSpecifyDeployment(deploymentTransferDto.getNamespace(),deploymentTransferDto.getCurrentDeployName(), null, null, HTTPMethod.GET, targetCluster);
		if (HttpStatusUtil.isSuccessStatus(response.getStatus())){
            deployment = JsonUtil.jsonToPojo(response.getBody(), Deployment.class);
            if(deployment!=null){
                updateDeployment(deployment, deploymentTransferDto,sourceCluster);
            }
        }
		response = dpService.doSpecifyDeployment(deploymentTransferDto.getCurrentNameSpace(),deploymentTransferDto.getCurrentDeployName(), null, null, HTTPMethod.GET, sourceCluster);
        errDeployDto = checkK8SClientResponse(response,deployName);
		if(!Objects.isNull(errDeployDto)){
			return errDeployDto;
		}
		deployment = JsonUtil.jsonToPojo(response.getBody(), Deployment.class);
        List<Volume> volumeList = deployment.getSpec().getTemplate().getSpec().getVolumes();
		if (volumeList!=null && !volumeList.isEmpty()){
		    //cm
			errDeployDto =createVolumes(sourceCluster,targetCluster,volumeList, deploymentTransferDto, deployment);
			if(!Objects.isNull(errDeployDto)){
				return errDeployDto;
			}
		}
		deployment = replaceDeployment(deployment, deploymentTransferDto,sourceCluster.getHarborServer().getHarborHost(), targetCluster.getHarborServer().getHarborHost());
		createApp(sourceCluster,targetCluster, deployment.getMetadata().getLabels(),deploymentTransferDto);
		response = dpService.doSpecifyDeployment(deploymentTransferDto.getNamespace(), null, generateHeader(), CollectionUtil.transBean2Map(deployment), HTTPMethod.POST, targetCluster);
        errDeployDto =checkK8SClientResponse(response,deployName);
		if(!Objects.isNull(errDeployDto)){
			return errDeployDto;
		}
		return errDeployDto;
	}
	private ErrDeployDto createVolumes(Cluster sourceCluster, Cluster newCluster ,List<Volume> volumeList ,DeploymentTransferDto deploymentTransferDto, Deployment deployment){
        ErrDeployDto errDeployDto = null;
        String configId = null;
        for (Volume volume : volumeList){
            ConfigMapVolumeSource cm = volume.getConfigMap();
            /*  PersistentVolumeClaimVolumeSource persistentVolumeClaimVolumeSource = volume.getPersistentVolumeClaim();*/
            if (cm != null) {
                K8SClientResponse response = null;
                try {
                    response = configMapService.doSepcifyConfigmap(deploymentTransferDto.getCurrentNameSpace(), cm.getName(), sourceCluster);
                    if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
                        ConfigMap configMap = JsonUtil.jsonToPojo(response.getBody(), ConfigMap.class);
                        if (configMap != null) {
                            K8SURL url = new K8SURL();
                            url.setNamespace(deploymentTransferDto.getNamespace()).setResource(Resource.CONFIGMAP);
                            Map<String, Object> bodys = new HashMap<String, Object>();
                            Map<String, Object> meta = new HashMap<String, Object>();
                            meta.put("namespace", deploymentTransferDto.getNamespace());
                            meta.put("name", configMap.getMetadata().getName());
                            Map<String, Object> label = new HashMap<String, Object>();
                            label.put("app", deploymentTransferDto.getCurrentDeployName());
                            meta.put("labels", label);
                            bodys.put("metadata", meta);
                            Map<String, Object> data = (Map<String, Object>) configMap.getData();
                            bodys.put("data", data);
                            K8SClientResponse result = new K8sMachineClient().exec(url, HTTPMethod.POST, generateHeader(), bodys, newCluster);
                            errDeployDto = checkK8SClientResponse(result, deploymentTransferDto.getCurrentDeployName());
                            if (!Objects.isNull(errDeployDto)) {
                                return errDeployDto;
                            }
                            // cm入库
                            String[] oldVolumeName = volume.getName().split(CommonConstant.LINE);
                            ConfigFile oldConfig = configFileMapper.getConfig(oldVolumeName[oldVolumeName.length - 1]);    // 查询老集群配置
                            if (oldConfig != null) {
                                ConfigFile newConfig = configFileMapper.getConfigByNameAndTag(oldConfig.getName(), oldConfig.getTags(),
                                        oldConfig.getProjectId(), newCluster.getId());    // 先查询一遍，避免违反configfile表索引的约束条件
                                if (newConfig == null) {    // 没有，先新增
                                    configId = UUIDUtil.get16UUID();

                                    newConfig = new ConfigFile();
                                    newConfig.setId(configId);
                                    newConfig.setName(oldConfig.getName());
                                    newConfig.setDescription(oldConfig.getDescription());
                                    newConfig.setTags(oldConfig.getTags());
                                    newConfig.setCreateTime(DateUtil.timeFormat.format(new Date()));
                                    newConfig.setUpdateTime(newConfig.getCreateTime());
                                    newConfig.setProjectId(oldConfig.getProjectId());
                                    newConfig.setTenantId(oldConfig.getTenantId());
                                    newConfig.setClusterId(newCluster.getId());
                                    newConfig.setClusterName(newCluster.getName());
                                    newConfig.setRepoName(oldConfig.getRepoName());
                                    newConfig.setUser(oldConfig.getUser());

                                    configFileMapper.saveConfigFile(newConfig);    // 保存configFile

                                    // 查询configfileItem列表，遍历 id置为空、传入相应的configId并保存
                                    List<ConfigFileItem> itemList = configFileItemMapper.getConfigFileItem(oldVolumeName[oldVolumeName.length - 1]);
                                    if (CollectionUtils.isNotEmpty(itemList)) {
										for (ConfigFileItem item : itemList) {
											item.setId(null);
											item.setConfigfileId(configId);
											configFileItemMapper.insert(item);
										}
                                    }
                                } else {
                                    configId = newConfig.getId();
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error("集群迁移创建configMap失败,deployDto:{},errrMessage:{}",deploymentTransferDto, e);
                    errDeployDto = new ErrDeployDto();
                    errDeployDto.setErrMsg("创建configMap失败");
                    errDeployDto.setDeployName(deploymentTransferDto.getCurrentDeployName());
                }
                logger.info("创建configmap完成");
            }
/*            if (persistentVolumeClaimVolumeSource != null){
                try {
                    K8SClientResponse k8sClientResponse = getPVC(deploymentTransferDto.getCurrentDeployName(),persistentVolumeClaimVolumeSource.getClaimName(),sourceCluster );
                    PersistentVolumeClaimList persistentVolumeClaim = K8SClient.converToBean(k8sClientResponse, PersistentVolumeClaimList.class);
                    *//*if(!persistentVolumeClaim.getItems().isEmpty()){
                        K8SClientResponse pvcRes = getPVC(deploymentTransferDto.getCurrentDeployName(), deploymentTransferDto.getNamespace(), oldCluster);
                        checkK8SClientResponse(pvcRes,deploymentTransferDto.getCurrentDeployName());
                        PersistentVolumeClaimList transferPersistentVolumeClaimc = K8SClient.converToBean(pvcRes, PersistentVolumeClaimList.class);

                        persistentVolumeClaims = persistentVolumeClaim.getItems();
                        persistentVolumeClaims.stream().forEach(x->errDeployDtos.add(createPVC(replacePersistentVolumeClaim(x, deploymentTransferDto), currentCluster, deploymentTransferDto.getNamespace(),deploymentTransferDto.getCurrentDeployName(),errDeployDto)));

                    }*//*

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }*/
        }

        // 如果取到了新的配置id，则用新的配置id替换
        if (configId != null) {
            // 替换掉deployment里volumes的volumeName和volumeMounts的name
            String volumeName = "%s-%s";
            for (Volume v : deployment.getSpec().getTemplate().getSpec().getVolumes()) {
                if (v.getConfigMap() != null) {
                    v.setName(String.format(volumeName, v.getName().substring(0, v.getName().lastIndexOf(CommonConstant.LINE)), configId));
                }
            }
            for (VolumeMount vm : deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getVolumeMounts()) {
                if (vm.getName().contains(CommonConstant.LINE)) {
                    vm.setName(String.format(volumeName, vm.getName().substring(0, vm.getName().lastIndexOf(CommonConstant.LINE)), configId));
                }
            }
        }
        return errDeployDto;
    }

    // 校验是否在灰度升级or蓝绿升级中
	private ErrDeployDto checkIsUpdating(Cluster sourceCluster, DeploymentTransferDto deploymentTransferDto) throws Exception{
        K8SClientResponse response = dpService.doSpecifyDeployment(deploymentTransferDto.getCurrentNameSpace(), deploymentTransferDto.getCurrentDeployName(), null, null, HTTPMethod.GET, sourceCluster);
        ErrDeployDto errDeployDto = checkK8SClientResponse(response, deploymentTransferDto.getCurrentDeployName());
        if(!Objects.isNull(errDeployDto)){
			return errDeployDto;
        }

        Deployment dep = JsonUtil.jsonToPojo(response.getBody(), Deployment.class);
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        Map<String, Object> body = new HashMap<>();
        body.put("labelSelector", "app=" + dep.getSpec().getSelector().getMatchLabels().get("app"));

        K8SClientResponse rsRes = rsService.doRsByNamespace(deploymentTransferDto.getCurrentNameSpace(), headers, body, HTTPMethod.GET, sourceCluster);
        errDeployDto = checkK8SClientResponse(rsRes, deploymentTransferDto.getCurrentDeployName());
        if(!Objects.isNull(errDeployDto)){
			return errDeployDto;
        }
        ReplicaSetList rSetList = JsonUtil.jsonToPojo(rsRes.getBody(), ReplicaSetList.class);
        if (rSetList == null || CollectionUtils.isEmpty(rSetList.getItems())) {
            return null;
        }

        int num = CommonConstant.NUM_ZERO;
        for (ReplicaSet rs : rSetList.getItems()) {
            if (rs.getSpec() != null && rs.getSpec().getReplicas() != null && rs.getSpec().getReplicas() > CommonConstant.NUM_ZERO) {
                num++;
            }
            if (num >= CommonConstant.NUM_TWO) {    // 如果有两个及以上版本的rs都有实例数，则说明在升级中
				errDeployDto = new ErrDeployDto();
				errDeployDto.setDeployName(deploymentTransferDto.getCurrentDeployName());
				errDeployDto.setErrMsg(ErrorCodeMessage.SERVICE_IS_UPDATING.getReasonChPhrase());
                return errDeployDto;
            }
        }

		return null;
	}

	private void createApp(Cluster sourceCluster, Cluster newCluster ,Map<String,Object> labels,DeploymentTransferDto deploymentTransferDto)  {
		String projectId = (String) labels.get("harmonycloud.cn/projectId");
		String appName = null;
		for (String label:labels.keySet()){
				if (label.contains(projectId)) {
					labels.put(label, deploymentTransferDto.getNamespace());
					appName = StringUtils.substringAfter(label, projectId + "-");
				}
		}
		//非应用
		if (appName == null){
			return;
		}
		try {
			K8SClientResponse response  = tprApplication.getApplicationByName(deploymentTransferDto.getCurrentNameSpace(), appName, null, null, HTTPMethod.GET, sourceCluster);
			if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
				logger.error("查询应用失败deploymentTransferDto:{},异常信息：{}", deploymentTransferDto, response.getBody());
			}
			BaseResource appCrd = JsonUtil.jsonToPojo(response.getBody(), BaseResource.class);
			Map<String,Object> appLebels = appCrd.getMetadata().getLabels();
			for (String label:appLebels.keySet()){
				if (label.contains(projectId)) {
					appLebels.put(label, deploymentTransferDto.getNamespace());
				}
			}

			BaseResource base = new BaseResource();
			ObjectMeta mate = new ObjectMeta();
			mate.setNamespace(deploymentTransferDto.getNamespace());
			mate.setName(appName);

			mate.setAnnotations(appCrd.getMetadata().getAnnotations());
			mate.setLabels(appLebels);
			base.setMetadata(mate);
			//创建app
			ActionReturnUtil res = tprApplication.createApplication(base, newCluster);
			if (!res.isSuccess()) {
				logger.error("创建应用失败deploymentTransferDto:{},异常信息：{}",deploymentTransferDto,res.getData());
			}
		}catch (Exception e){
			logger.error("创建应用失败deploymentTransferDto:{},异常信息：{}",deploymentTransferDto,e);
		}
		logger.info("创建app完成");
	}
	/**
	 * 迁移服务 两种类型 stafulset(有状态的) deployment(无状态的)
	 * @param deploymentTransferDto
	 * @param currentCluster
	 * @param sourceCluster
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws IntrospectionException
	 */
	private ErrDeployDto create(DeploymentTransferDto deploymentTransferDto,Cluster currentCluster,Cluster sourceCluster) throws Exception{
		switch (deploymentTransferDto.getCurrentServiceType()) {
			case Constant.STATEFULSET:
				createService(deploymentTransferDto, currentCluster, sourceCluster);
				ErrDeployDto errDeployDto = createStatefulSet(deploymentTransferDto, currentCluster, sourceCluster);
				return errDeployDto;
			case Constant.DEPLOYMENT:
				errDeployDto = createService(deploymentTransferDto, currentCluster, sourceCluster);
				errDeployDto = createDeployment(deploymentTransferDto, currentCluster, sourceCluster,errDeployDto,deploymentTransferDto.getCurrentDeployName());
				return errDeployDto;
			default:
				break;
		}
		return null;
	}
	
	/**
	 * 创建服务 需要先创建service
	 * @param deploymentTransferDto
	 * @param currentCluster
	 * @param sourceCluster  旧集群id
	 */
	private ErrDeployDto createService(DeploymentTransferDto deploymentTransferDto,Cluster currentCluster,Cluster sourceCluster) throws Exception{
		K8SClientResponse rsRes  =null;
		com.harmonycloud.k8s.bean.Service service =null;
		K8SURL k8surl = new K8SURL();
		ErrDeployDto errDeployDto = new ErrDeployDto();
		rsRes = serviceService.doSepcifyService(deploymentTransferDto.getNamespace(),deploymentTransferDto.getCurrentDeployName(), null, null, HTTPMethod.GET, currentCluster);
		if (rsRes.getStatus() != Constant.HTTP_404){
			errDeployDto = new ErrDeployDto();
			errDeployDto.setDeployName(deploymentTransferDto.getCurrentDeployName());
			errDeployDto.setErrMsg("创建service失败，service已存在");
			return errDeployDto;
		}
		//旧集群service
		rsRes = serviceService.doSepcifyService(deploymentTransferDto.getCurrentNameSpace(),deploymentTransferDto.getCurrentDeployName(), null, null, HTTPMethod.GET, sourceCluster);
		errDeployDto = checkK8SClientResponse(rsRes,deploymentTransferDto.getCurrentDeployName());
		if(!Objects.isNull(errDeployDto)){
			return errDeployDto;
		}
		k8surl.setNamespace(deploymentTransferDto.getNamespace()).setResource(Resource.SERVICE);
		service = JsonUtil.jsonToPojo(rsRes.getBody(), com.harmonycloud.k8s.bean.Service.class);
		//目标集群创建service
		K8SClientResponse sResponse = new K8sMachineClient().exec(k8surl, HTTPMethod.POST, generateHeader(),CollectionUtil.transBean2Map(replaceService(service, deploymentTransferDto)), currentCluster);
		errDeployDto = checkK8SClientResponse(sResponse,deploymentTransferDto.getCurrentDeployName());
		if(!Objects.isNull(errDeployDto)){
			return errDeployDto;
		}
		logger.info("创建server完成");
		return null;
	}
	
	/**
	 * 创建statefulset 创建前判断当前集群是否有statefulset 有则更新
	 * @param deploymentTransferDto
	 * @param currentCluster
	 * @param transferCluster
	 * @throws MarsRuntimeException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws IntrospectionException
	 */
	private ErrDeployDto createStatefulSet(DeploymentTransferDto deploymentTransferDto,Cluster currentCluster,Cluster transferCluster) throws Exception{
		K8SClientResponse statefulSetRes = null;
		StatefulSet statefulSet = null;
		ErrDeployDto errDeployDto = null;
		statefulSetRes = statefulSetService.doSpecifyStatefulSet(deploymentTransferDto.getNamespace(), deploymentTransferDto.getCurrentDeployName(), null, null,
				HTTPMethod.GET, transferCluster);
		statefulSet = JsonUtil.jsonToPojo(statefulSetRes.getBody(), StatefulSet.class);
		if(statefulSet!=null){
			updateStatefulset(statefulSet, deploymentTransferDto, transferCluster);
		}
		statefulSetRes = statefulSetService.doSpecifyStatefulSet(deploymentTransferDto.getCurrentNameSpace(), deploymentTransferDto.getCurrentDeployName(), null, null,
				HTTPMethod.GET, currentCluster);
		errDeployDto = checkK8SClientResponse(statefulSetRes,deploymentTransferDto.getCurrentDeployName());
		if(!Objects.isNull(errDeployDto)){
			return errDeployDto;
		}
		statefulSet = JsonUtil.jsonToPojo(statefulSetRes.getBody(), StatefulSet.class);
		errDeployDto = this.transferStatefulSet(deploymentTransferDto.getNamespace(), statefulSet, transferCluster,errDeployDto,deploymentTransferDto.getCurrentDeployName());
		if(!Objects.isNull(errDeployDto)){
			return errDeployDto;
		}
		return null;
	}
	
	/**
	 * 
	 * 生成请求头
	 * @return
	 */
	private Map<String,Object> generateHeader(){
		Map<String, Object> headers = new HashMap<String, Object>();
	    headers.put("Content-type", "application/json");
	    return headers; 
	}

	/**
	 * 创建tcp规则的ingress
	 * @param deploymentTransferDto
	 * @param map
	 * @param rules
	 * @throws MarsRuntimeException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws IntrospectionException
	 */
	private ErrDeployDto createTcpIngress(DeploymentTransferDto deploymentTransferDto,Map<String, Object> map,List<TcpRuleDto> rules) throws Exception {
		SvcRouterDto svcRouterDto = new SvcRouterDto();
		svcRouterDto.setNamespace(deploymentTransferDto.getNamespace());
		svcRouterDto.setName(deploymentTransferDto.getCurrentDeployName());
		svcRouterDto.setIcName((String)map.get("icName"));
		Map<String, Object> address = (Map<String, Object>)map.get("address");
		TcpRuleDto tcpRuleDto = new TcpRuleDto();
		tcpRuleDto.setTargetPort((String)address.get("containerPort"));
		tcpRuleDto.setPort((String)address.get("externalPort"));
		tcpRuleDto.setProtocol((String)map.get("type"));
		rules.add(tcpRuleDto);
		svcRouterDto.setRules(rules);
		return routerService.transferRuleDeploy(svcRouterDto,deploymentTransferDto.getCurrentDeployName());
	}

	/**
	 * 创建http类型的ingress
	 * @param deploymentTransferDto
	 * @param map
	 * @throws MarsRuntimeException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws IntrospectionException
	 */
	private ErrDeployDto createHttpIngress(DeploymentTransferDto deploymentTransferDto,Map<String, Object> map,
                                           Cluster sourceCluster) throws Exception {
		ParsedIngressListDto parsedIngressListDto = new ParsedIngressListDto();
		parsedIngressListDto.setNamespace(deploymentTransferDto.getNamespace());
		parsedIngressListDto.setName((String)map.get("name"));
		parsedIngressListDto.setIcName((String)map.get("icName"));
		Map<String, Object> labels = new HashMap<>();
		labels.put("app", deploymentTransferDto.getCurrentDeployName());
		labels.put("tenantId", deploymentTransferDto.getProjectId());
		List<Map<String, Object>> address = (List<Map<String, Object>>)map.get("address");
		List<HttpRuleDto> httpRuleDtos = new ArrayList<>();
		for (Map<String, Object> map2 : address) {
			String host = map2.get("hostname").toString();
			parsedIngressListDto.setHost(splitHostname(host));
			HttpRuleDto httpRuleDto = new HttpRuleDto();
			httpRuleDto.setService(deploymentTransferDto.getCurrentDeployName());
			System.out.println(map2.get("port"));
			httpRuleDto.setPort(String.valueOf(map2.get("port")));
			httpRuleDto.setPath(splitPath((String)map2.get("host")));
			httpRuleDtos.add(httpRuleDto);
		}
		parsedIngressListDto.setRules(httpRuleDtos);
		parsedIngressListDto.setLabels(labels);
		parsedIngressListDto.setProtocol(Constant.PROTOCOL_HTTP);
		parsedIngressListDto.setServiceName(deploymentTransferDto.getCurrentDeployName());
		ErrDeployDto errDeployDto = null;
		try{
			 errDeployDto = routerService.transferIngressCreate(parsedIngressListDto,deploymentTransferDto,sourceCluster);
		}catch (Exception e){
			logger.error("errorMessage{}",e);
		}
		return errDeployDto;
	}

	/**
	 * 校验从k8s获取的响应是否正确
	 * @param response
	 * @param
	 */
	private ErrDeployDto checkK8SClientResponse(K8SClientResponse response,String deployName){
		if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            if (409 == response.getStatus()){
                logger.warn("{}已存在，k8s massage:{}",deployName ,response.getBody());
                return null;
            }
		    ErrDeployDto errDeployDto = new ErrDeployDto();
			errDeployDto.setDeployName(deployName);
			errDeployDto.setErrMsg("从k8s获得响应失败:"+ response.getBody());
			return errDeployDto;
		}else {
			return null;
		}
	}
	
	public K8SClientResponse getPVC(String name, String namespace, Cluster cluster) throws Exception {
		Map<String, Object> queryP = new HashMap<>();
	    queryP.put("labelSelector", Constant.TYPE_DEPLOYMENT+"/"+name + Constant.EQUAL + name);
        K8SClientResponse pvcsRes = pvcService.doSepcifyPVC(namespace, queryP, HTTPMethod.GET, cluster);
        return pvcsRes;
    }
	
	/**
	 * 替换原有deployment的属性 替换的有 namespace，selflink，deployment label，podlabel
	 * @param deployment
	 * @param deploymentTransferDtos
	 * @return
	 */
	private Deployment replaceDeployment(Deployment deployment,DeploymentTransferDto deploymentTransferDtos, String sourceHarborHost, String targeHarborHost) {
		deployment.getMetadata().setNamespace(deploymentTransferDtos.getNamespace());
		deployment.getMetadata().setSelfLink(deployment.getMetadata().getSelfLink().replace(deployment.getMetadata().getNamespace(),deploymentTransferDtos.getNamespace()));
		deployment.getMetadata().setResourceVersion(null);
		deployment.getMetadata().setLabels(replaceLabels(deployment.getMetadata().getLabels(), deployment.getMetadata().getNamespace()));
		deployment.getSpec().getTemplate().getMetadata().setLabels(deployment.getSpec().getTemplate().getMetadata().getLabels());
		//源集群与目标集群使用的harbor地址不一样，需要将镜像里的harbor地址替换
		if (!sourceHarborHost.equalsIgnoreCase(targeHarborHost)) {
			List<Container> containers = deployment.getSpec().getTemplate().getSpec().getContainers();
			for (Container container : containers) {
				container.setImage(container.getImage().replace(sourceHarborHost, targeHarborHost));
			}
		}
		return deployment;
	}
	
	/**
	 * 替换labels的应用的namespace
	 * @param labels
	 * @param namespace
	 * @return
	 */
	private Map<String,Object> replaceLabels(Map<String,Object> labels,String namespace) {
		String projectId = (String) labels.get("harmonycloud.cn/projectId");
		for (String label:labels.keySet()){
	    	 if(label.contains(projectId)){
	    	     labels.put(label,namespace);
				 /*String appName = StringUtils.substringAfter(label,projectId+"-");*/
	    	 }
	     }
	     return labels;
	}
	
	/**
	 * 修改当前service对应的属性
	 * @return
	 */
	private com.harmonycloud.k8s.bean.Service replaceService(com.harmonycloud.k8s.bean.Service service,DeploymentTransferDto deploymentTransferDto){
		service.getMetadata().setNamespace(deploymentTransferDto.getNamespace());;
		service.getMetadata().setSelfLink(service.getMetadata().getSelfLink().replace(service.getMetadata().getNamespace(),deploymentTransferDto.getNamespace()));
		service.getSpec().setClusterIP(null);
		service.getMetadata().setResourceVersion(null);
		return service;
	}

	
	/**
	 * 替换pvc属性
	 * @param persistentVolumeClaim
	 * @param deploymentTransferDto
	 * @param type "create / update "
	 * @return
	 */
	private PersistentVolumeClaim replacePersistentVolumeClaim(PersistentVolumeClaim persistentVolumeClaim,DeploymentTransferDto deploymentTransferDto, String type){
		persistentVolumeClaim.getMetadata().setNamespace(deploymentTransferDto.getNamespace());
		persistentVolumeClaim.getMetadata().setResourceVersion(null);
		//persistentVolumeClaim.getSpec().getSelector().setMatchLabels(replacePVCLabels(persistentVolumeClaim.getSpec().getSelector().getMatchLabels(), deploymentTransferDto));
		persistentVolumeClaim.getMetadata().setSelfLink(persistentVolumeClaim.getMetadata().getSelfLink().replace(persistentVolumeClaim.getMetadata().getNamespace(),deploymentTransferDto.getNamespace()));
		Map oldLabels = persistentVolumeClaim.getMetadata().getLabels();
		Map labels = type.equals("create") ?  createLabels(oldLabels, deploymentTransferDto):updateLabels(oldLabels, deploymentTransferDto);
		persistentVolumeClaim.getMetadata().setLabels(labels);
		return persistentVolumeClaim;
	}


	/**
	 * 删除pvc和app的labels绑定关系
	 * @param oldLabels
	 * @param deploymentTransferDto
	 * @return
	 */
	private Map<String, Object> createLabels(Map<String,Object> oldLabels ,DeploymentTransferDto deploymentTransferDto ){
		oldLabels.entrySet().removeIf(entry -> entry.getKey().contains(LABEL_KEY_APP + CommonConstant.SLASH));
		oldLabels.put(LABEL_KEY_APP + CommonConstant.SLASH + deploymentTransferDto.getCurrentDeployName(), deploymentTransferDto.getCurrentDeployName());
		return oldLabels;
	}

	private Map<String, Object> updateLabels(Map<String,Object> oldLabels ,DeploymentTransferDto deploymentTransferDto ){
		oldLabels.put(LABEL_KEY_APP + CommonConstant.SLASH + deploymentTransferDto.getCurrentDeployName(), deploymentTransferDto.getCurrentDeployName());
		return oldLabels;
	}
	
	/***
	 * 替换label key
	 * @param label
	 * @param namespace
	 * @return
	 */
	private String splitPVCLabelKey(String label,String namespace){
		StringJoiner str = new StringJoiner(".");
		List<String> strs = Arrays.asList(label.split("\\."));
		for (int i = 0; i < strs.size()-1; i++) {
			str.add(strs.get(i));
		}
		str.add(namespace);
		return str.toString();
	}
	
	/**
	 * 更新对应的deployment
	 * @param deployment
	 * @param deploymentTransferDto
	 * @return
	 * @throws IntrospectionException 
	 * @throws IllegalAccessException 
	 * @throws InvocationTargetException 
	 */
	private boolean updateDeployment(Deployment deployment,DeploymentTransferDto deploymentTransferDto,Cluster cluster) throws Exception{
		Map<String, Object> bodys = CollectionUtil.transBean2Map(deployment);
		Map<String, Object> headers = new HashMap<>();
		headers.put("Content-Type", "application/json");
		K8SClientResponse newRes = dpService.doSpecifyDeployment(deploymentTransferDto.getNamespace(),deploymentTransferDto.getCurrentDeployName(), headers, bodys, HTTPMethod.PUT, cluster);
		return true;
	}


	/**
	 * 当前存在statefulset时更新statefulset
	 * @param statefulSet
	 * @param deploymentTransferDto
	 * @param cluster
	 * @return
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws IntrospectionException        checkK8SClientResponse(newRes);
	 */
	private boolean updateStatefulset(StatefulSet statefulSet,DeploymentTransferDto deploymentTransferDto,Cluster cluster) throws Exception{
		//更新服务内的容器信息
		Map<String, Object> bodys = CollectionUtil.transBean2Map(statefulSet);
		Map<String, Object> headers = new HashMap<>();
		headers.put("Content-Type", "application/json");
		K8SClientResponse newRes = statefulSetService.doSpecifyStatefulSet(deploymentTransferDto.getNamespace(),deploymentTransferDto.getCurrentDeployName(), headers, bodys, HTTPMethod.PUT, cluster);
		return true;
	}
	
	/**
	 * 计算内存配额
	 * @param memory
	 * @return
	 */
	private static double mathMemory(String memory){
	       BigDecimal bigDecimal = new BigDecimal(memory);
	       BigDecimal bigDecimal2 = new BigDecimal(1024);
	       return bigDecimal.divide(bigDecimal2).add(new BigDecimal(0.05)).setScale(1,BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	private DeployResultDto generateTransferDeploymentAndTransferDeploy(List<ErrorNamespaceDto> errorNamespaceDtos,
																		List<ClusterTransferDto> clusterTransferDtos,
																		TransferClusterBackup transferClusterBackup,Cluster sourceCluster) throws Exception {
		Map<String,String> param = successNamespaceBind(errorNamespaceDtos, clusterTransferDtos);
		List<TransferBindDeploy> list = new ArrayList<>();
		List<DeploymentTransferDto> deploymentTransferDtos = new ArrayList<>();
		DeployResultDto deployResultDto = new DeployResultDto();
		ClusterTransferDto clusterTransferDto = clusterTransferDtos.get(0);
		List<String> deployNames = new ArrayList();
		List<String> appNames = new ArrayList();
		List<BindNameSpaceDto> bindNameSpaceDtos = clusterTransferDto.getBindNameSpaceDtos();
        for (BindNameSpaceDto bindNameSpaceDto : bindNameSpaceDtos){
			List<DeploymentDto> deploymentDtos  = bindNameSpaceDto.getDeploymentDto();
			for (DeploymentDto deploymentDto : deploymentDtos){
				deployNames.add(deploymentDto.getDeployName());
				/*if (deploymentDto.getAppName() != null && !"".equals(deploymentDto.getAppName().trim())){
					appNames.add(deploymentDto.getAppName());
				}*/
			}
		}
		for (ErrorNamespaceDto namespaceDto : errorNamespaceDtos) {
			K8SClientResponse clientResponse = dpService.doDeploymentsByNamespace(param.get(namespaceDto.getNamespace()),null, null, HTTPMethod.GET, sourceCluster);
			DeploymentList deploymentList = JsonUtil.jsonToPojo(clientResponse.getBody(), DeploymentList.class);
			if (deploymentList != null && !CollectionUtils.isEmpty(deploymentList.getItems())) {
				for(Deployment deployment:deploymentList.getItems()){
					if (deployNames.contains(deployment.getMetadata().getName())){
						list.add(generateTransferBindDeploy(deployment, clusterTransferDtos.get(0), transferClusterBackup.getId(), namespaceDto.getNamespace()));
						deploymentTransferDtos.add(generateTransferDto(deployment,  clusterTransferDtos.get(0), param, namespaceDto));
					}
				}
			}
		}
		deployResultDto.setDeploymentTransferDtos(deploymentTransferDtos);
		deployResultDto.setDeploys(list);
		return deployResultDto;
	}

	private Map<String,String> successNamespaceBind(List<ErrorNamespaceDto> errorNamespaceDtos,List<ClusterTransferDto> clusterTransferDtos){
		Map<String,String> map = new HashMap<>();
		if(!CollectionUtils.isEmpty(errorNamespaceDtos)){
			for (ErrorNamespaceDto errorNamespaceDto : errorNamespaceDtos) {
				for(ClusterTransferDto clusterTransferDto:clusterTransferDtos){
					for(BindNameSpaceDto bindNameSpaceDto:clusterTransferDto.getBindNameSpaceDtos()){
						if(errorNamespaceDto.getNamespace().equals(bindNameSpaceDto.getName())){
							map.put(errorNamespaceDto.getNamespace(),bindNameSpaceDto.getOldNameSpace());
						}
					}
				}
			}
		}
		return map;
	}

	private DeploymentTransferDto generateTransferDto(Deployment deployment,ClusterTransferDto clusterTransferDto,Map<String,String> param,ErrorNamespaceDto errorNamespaceDto ){
		DeploymentTransferDto deploymentTransferDto = new DeploymentTransferDto();
		deploymentTransferDto.setClusterId(clusterTransferDto.getTargetClusterId());
		deploymentTransferDto.setCurrentClusterId(clusterTransferDto.getCurrentClusterId());
		deploymentTransferDto.setCurrentDeployName(deployment.getMetadata().getName());
		deploymentTransferDto.setCurrentNameSpace(param.get(errorNamespaceDto.getNamespace()));
//		deploymentTransferDto.setCurrentProjectId(String.valueOf(deployment.getMetadata().getLabels().get("harmonycloud.cn/projectId")));
		deploymentTransferDto.setCurrentServiceType(Constant.DEPLOYMENT);
//		deploymentTransferDto.setCurrentTenantId(clusterTransferDto.getTenantId());
		deploymentTransferDto.setNamespace(errorNamespaceDto.getNamespace());
		deploymentTransferDto.setProjectId(String.valueOf(deployment.getMetadata().getLabels().get("harmonycloud.cn/projectId")));
		deploymentTransferDto.setTenantId(clusterTransferDto.getTenantId());
		return deploymentTransferDto;
	}

	private Map<String,Object> createDeployment(List<DeploymentTransferDto> deploymentTransferDtos,
												TransferClusterBackup transferClusterBackup,
												Cluster sourceCluster,Cluster targetCluster) throws Exception {
	    //TODO 稍后改为先取deployment ，然后根据deployment创建pv、cm等资源
		int index = 0;
		Map<String,Object> params = new HashMap<>();
		List<ErrDeployDto> errDeployDtos = new ArrayList<>();
		List<TransferBindDeploy> errorBindDeploy = new ArrayList<>();
		for (DeploymentTransferDto deploymentTransferDto : deploymentTransferDtos) {
			index++;
			double transferProgress = new Double(index) / deploymentTransferDtos.size();
			TransferBindDeploy query = new TransferBindDeploy();
			query.setTransferBackupId(transferClusterBackup.getId());
			query.setNamespace(deploymentTransferDto.getNamespace());
			query.setDeployName(deploymentTransferDto.getCurrentDeployName());
			TransferBindDeploy transferBindDeploy = transferDeployMapper.selectUnique(query);
			transferBindDeploy.setStepId(1);

			// 校验是否在灰度升级or蓝绿升级中
			ErrDeployDto errDeployDto = checkIsUpdating(sourceCluster, deploymentTransferDto);
			if (!Objects.isNull(errDeployDto)) {
				errDeployDtos.add(errDeployDto);
				transferBindDeploy.setErrMsg(errDeployDto.getErrMsg());
				transferBindDeploy.setDeployName(errDeployDto.getDeployName());
				transferBindDeploy.setStatus(STATUS_FAIL);
				updateStatus(transferProgress, transferBindDeploy, transferClusterBackup, errDeployDtos);
				errDeployDtos.add(errDeployDto);
				continue;
			}

			errDeployDto = createIngress(deploymentTransferDto,sourceCluster);
			if(errDeployDto!=null){
				errDeployDtos.add(errDeployDto);
				transferBindDeploy.setErrMsg(errDeployDto.getErrMsg());
				transferBindDeploy.setDeployName(errDeployDto.getDeployName());
				transferBindDeploy.setStepId(2);
				transferBindDeploy.setStatus(STATUS_FAIL);
				updateStatus(transferProgress, transferBindDeploy, transferClusterBackup, errDeployDtos);
				errorBindDeploy.add(transferBindDeploy);
				continue;
			}
			errDeployDto = createPVC(deploymentTransferDto, targetCluster, sourceCluster,errDeployDtos);
			if(errDeployDto!=null){
				errDeployDtos.add(errDeployDto);
				transferBindDeploy.setErrMsg(errDeployDto.getErrMsg());
				transferBindDeploy.setDeployName(errDeployDto.getDeployName());
				transferBindDeploy.setStepId(3);
				transferBindDeploy.setStatus(STATUS_FAIL);
				updateStatus(transferProgress, transferBindDeploy, transferClusterBackup, errDeployDtos);
				errorBindDeploy.add(transferBindDeploy);
				continue;
			}

			errDeployDto = create(deploymentTransferDto, targetCluster, sourceCluster);
			if(errDeployDto!=null){
				errDeployDtos.add(errDeployDto);
				transferBindDeploy.setErrMsg(errDeployDto.getErrMsg());
				transferBindDeploy.setDeployName(errDeployDto.getDeployName());
				transferBindDeploy.setStepId(5);
				transferBindDeploy.setStatus(STATUS_FAIL);
				updateStatus(transferProgress, transferBindDeploy, transferClusterBackup, errDeployDtos);
				errorBindDeploy.add(transferBindDeploy);
				continue;
			}
			transferBindDeploy.setDeployName(deploymentTransferDto.getCurrentDeployName());
			transferBindDeploy.setStepId(6);
			transferBindDeploy.setStatus(STATUS_SUCCESS);
			errorBindDeploy.add(transferBindDeploy);
			updateStatus(transferProgress, transferBindDeploy, transferClusterBackup, errDeployDtos);
		}
		params.put("errDeployDtos",errDeployDtos);
		params.put("errorBindDeploy", errorBindDeploy);
		return params;
	}

	private void updateStatus(double transferProgess, TransferBindDeploy transferBindDeploy,
							  TransferClusterBackup transferClusterBackup, List<ErrDeployDto> errDeployDtos){
		NumberFormat nf = NumberFormat.getNumberInstance();
		// 保留两位小数
		nf.setMaximumFractionDigits(0);
		nf.setGroupingUsed(false);
		transferDeployMapper.updateByPrimaryKeySelective(transferBindDeploy);
		transferClusterBackup.setTransferClusterPercent(nf.format(transferProgess * 100) + "%");
		transferClusterBackup.setErrDeploy(JSON.toJSONString(errDeployDtos));
		transferClusterBackUpMapper.updateByPrimaryKeySelective(transferClusterBackup);
	}

	/**
	 * 包装transferDTO对象
	 * @param deployment
	 * @param clusterTransferDto
	 * @return
	 */
	private TransferBindDeploy generateTransferBindDeploy(Deployment deployment,ClusterTransferDto clusterTransferDto,Integer transferBackupId,String namespace){
		TransferBindDeploy transferBindDeploy = new TransferBindDeploy();
		transferBindDeploy.setClusterId(clusterTransferDto.getTargetClusterId());
		transferBindDeploy.setCreateTime(new Date());
		transferBindDeploy.setDeployName(deployment.getMetadata().getName());
		Integer num = transferDeployMapper.queryMaxNun(clusterTransferDto.getTenantId(),clusterTransferDto.getTargetClusterId());
		transferBindDeploy.setDeployNum(num != null  ? num + 1 : 1);
		transferBindDeploy.setIsDelete((byte)0);
		transferBindDeploy.setNamespace(namespace);
		transferBindDeploy.setProjectId(String.valueOf(deployment.getMetadata().getLabels().get("projectId")));
		transferBindDeploy.setStatus(0);
		transferBindDeploy.settenantId(clusterTransferDto.getTenantId());
		transferBindDeploy.setSourceNamespace(deployment.getMetadata().getNamespace());
		transferBindDeploy.setSourceClusterId(clusterTransferDto.getCurrentClusterId());
		transferBindDeploy.setTransferBackupId(transferBackupId);
		return transferBindDeploy;
	}

	public ErrDeployDto transferStatefulSet(String namespace, StatefulSet statefulSet, Cluster cluster, ErrDeployDto errDeployDto, String deployName) throws MarsRuntimeException, IllegalAccessException, IntrospectionException, InvocationTargetException {
		K8SURL url = new K8SURL();
		url.setResource(Resource.STATEFULSET).setNamespace(namespace);
		Map<String, Object> headers = new HashMap<>();
		headers.put("Content-Type", "application/json");
		Map<String, Object> bodys = CollectionUtil.transBean2Map(statefulSet);
		url.setNamespace(namespace).setResource(Resource.STATEFULSET);
		K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.POST,headers,bodys,cluster);
		if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
			errDeployDto = new ErrDeployDto();
			errDeployDto.setDeployName(deployName);
			errDeployDto.setErrMsg("创建statefulset失败");
			return errDeployDto;
		}
		return null;
	}

	@Override
	public ActionReturnUtil getTransferCluster(ClusterTransferDetailDto clusterTransferDto) {
		List<TransferCluster> list = transferClusterMapper.queryTransferCluster(clusterTransferDto.getClusterId());  // 老集群id
		if (CollectionUtils.isEmpty(list)) {
			return ActionReturnUtil.returnSuccessWithData(new JSONObject());
		}

		TransferCluster transferCluster = list.get(0);
		ClusterTransferDto dto = new ClusterTransferDto();
		dto.setTenantId(transferCluster.getTenantId());
		dto.setCurrentClusterId(transferCluster.getOldClusterId());
		dto.setTargetClusterId(transferCluster.getClusterId());
		dto.setCreateTime(transferCluster.getCreateTime());
		dto.setPercent(transferCluster.getPercent());

		Cluster cluster = clusterService.findClusterById(transferCluster.getOldClusterId());
		if (cluster != null) {
			dto.setCurrentClusterName(cluster.getName());
		}

		return ActionReturnUtil.returnSuccessWithData(dto);
	}

	@Override
	public ActionReturnUtil getDeployDetail(ClusterTransferDetailDto clusterTransferDto) {
		List<TransferBindDeploy> list = transferDeployMapper.queryTransferDeployDetail(clusterTransferDto.getTenantId(),
				clusterTransferDto.getClusterId());    // 老集群id
		if (CollectionUtils.isEmpty(list)) {
			return ActionReturnUtil.returnSuccessWithData(Lists.newArrayList());
		}
		List<ClusterTransferDetailDto> resList = Lists.newArrayList();
		list.forEach(detail -> {
			ClusterTransferDetailDto dto = new ClusterTransferDetailDto();
			dto.setTenantId(detail.gettenantId());
			dto.setClusterId(detail.getClusterId());
			dto.setNamespace(detail.getNamespace());
			dto.setDeployName(detail.getDeployName());
			try {
				TenantBinding tenant = tenantService.getTenantByTenantid(detail.gettenantId());
				if (tenant != null) {
					dto.setTenantName(tenant.getTenantName());
				}
			} catch (Exception e) {
				logger.warn("select tenantName error, tenantId:{}", detail.gettenantId());
			}

			resList.add(dto);
		});
		return ActionReturnUtil.returnSuccessWithData(resList);
	}

	@Override
	public ActionReturnUtil getTransferDetail(Integer transferBackupId) {
		TransferClusterBackup transferClusterBackup = transferClusterBackUpMapper.selectByPrimaryKey(transferBackupId);
		ClusterTransferBackupDto clusterTransferBackupDto = this.convert(transferClusterBackup);
		List<TransferBindDeploy> deploys = transferDeployMapper.listTransferDeploys(transferBackupId);
		clusterTransferBackupDto.setTransferBindDeploys(deploys);
		return ActionReturnUtil.returnSuccessWithData(clusterTransferBackupDto);
	}

	@Override
	public ActionReturnUtil listTransferHistory(ClusterTransferDetailDto clusterTransferDto) {
		List<TransferClusterBackup> backupList = transferClusterBackUpMapper.queryHistoryBackUp(clusterTransferDto.getClusterId(),
				clusterTransferDto.getTenantId());    // 老集群id
		if (CollectionUtils.isEmpty(backupList)) {
			return ActionReturnUtil.returnSuccessWithData(Lists.newArrayList());
		}
		List<ClusterTransferBackupDto> resList = Lists.newArrayList();
		backupList.forEach(backup -> {
			ClusterTransferBackupDto dto = this.convert(backup);

			// 返回迁移状态：1-迁移失败 2-迁移中 3-部分完成 4-迁移完成
			if (StringUtils.isBlank(dto.getTransferClusterPercent()) || StringUtils.isNotBlank(dto.getErrMsg())) {
				// 迁移失败，状态置为1
				dto.setTransferStatus(NUM_ONE);
			} else if ("100%".equals(dto.getTransferClusterPercent())) {
				// 迁移操作完成，但是得区分3-部分完成和4-全部完成
				List<TransferBindDeploy> deploys = transferDeployMapper.listTransferDeploys(dto.getId());
				int failNum = (int) deploys.stream().filter(deploy ->
						deploy.getStatus() == null || deploy.getStatus() != NUM_ONE).count();
				if (failNum == NUM_ZERO) {
					dto.setTransferStatus(NUM_FOUR);
				} else if (failNum == deploys.size()) {
					dto.setTransferStatus(NUM_ONE);
				} else {
					dto.setTransferStatus(NUM_THREE);
				}
			} else {
				// 迁移中，状态置为2
				dto.setTransferStatus(NUM_TWO);
			}

			resList.add(dto);
		});
		return ActionReturnUtil.returnSuccessWithData(resList);
	}

	private List<DeploymentTransferDto> generateTransferDtoList(List<TransferBindDeploy> transferBindDeploys){
		List<DeploymentTransferDto> list = new ArrayList<>();
		transferBindDeploys.stream().forEach(x->list.add(generateTransferDto(x)));
		return list;
	}

	private DeploymentTransferDto generateTransferDto(TransferBindDeploy transferBindDeploy){
		DeploymentTransferDto deploymentTransferDto = new DeploymentTransferDto();
		deploymentTransferDto.setClusterId(transferBindDeploy.getClusterId());
		deploymentTransferDto.setCurrentClusterId(transferBindDeploy.getSourceClusterId());
		deploymentTransferDto.setCurrentDeployName(transferBindDeploy.getDeployName());
		deploymentTransferDto.setCurrentNameSpace(transferBindDeploy.getNamespace());
		deploymentTransferDto.setCurrentProjectId(transferBindDeploy.getProjectId());
		deploymentTransferDto.setCurrentServiceType(Constant.DEPLOYMENT);
		deploymentTransferDto.setCurrentTenantId(transferBindDeploy.gettenantId());
		deploymentTransferDto.setNamespace(transferBindDeploy.getNamespace());
		deploymentTransferDto.setProjectId(transferBindDeploy.getProjectId());
		return deploymentTransferDto;
	}

	private void checkResource(Cluster sourceCluster,Cluster targetCluster,TransferClusterBackup transferClusterBackup,List<ClusterTransferDto> clusterTransferDto) throws Exception {
		if(Objects.isNull(sourceCluster)||Objects.isNull(targetCluster)){
			transferClusterBackup.setErrMsg(ErrorCodeMessage.CLUSTER_NOT_FOUND.getReasonChPhrase());
			transferClusterBackUpMapper.insert(transferClusterBackup);
			throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
		}
		if(!compareUsage(sourceCluster, targetCluster, clusterTransferDto)){
			transferClusterBackup.setErrMsg(ErrorCodeMessage.TRANSFER_CLUSTER_RESOURCE_ERROR.getReasonChPhrase());
			transferClusterBackUpMapper.insert(transferClusterBackup);
			throw new MarsRuntimeException(ErrorCodeMessage.TRANSFER_CLUSTER_RESOURCE_ERROR);
		}
	}


	/**
	 * 校验完成后开始执行迁移集群
	 * @param transferClusterBackup 迁移备份对象
	 * @param clusterTransferDto 传递的迁移参数
	 * @param sourceCluster 当前集群
	 * @param targetCluster 目标集群
	 * @param isContinue 是否是断点续传
	 * @return
	 * @throws RuntimeException
	 * @throws ReflectiveOperationException
	 * @throws ReflectiveOperationException
	 * @throws Exception
	 */
	private TransferResultDto excuteTransfer(TransferClusterBackup transferClusterBackup,List<ClusterTransferDto> clusterTransferDto,Cluster sourceCluster,Cluster targetCluster,boolean isContinue) throws Exception{

		Map<String,List<ErrorNamespaceDto>> param = saveBindNamespace(clusterTransferDto, targetCluster,sourceCluster, isContinue);
		TransferResultDto transferResultDto = new TransferResultDto();
		transferClusterBackup.setIsContinue(isContinue?(byte)1:(byte)0);
		transferClusterBackup.setTransferClusterPercent("0%");
		transferClusterBackup.setErrNamespace(JSON.toJSONString(param.get(Constant.TRANSFER_NAMESPACE_ERROR)));
		transferClusterBackUpMapper.insert(transferClusterBackup);
		// 这个地方应该是创建成功的namespaces
		List<ErrorNamespaceDto> namespaces = param.get(Constant.TRANSFER_NAMESPACE_SUCCESS);

		// 异步执行服务迁移
		asyncClusterTransfer.transferDeploy(namespaces, transferClusterBackup, clusterTransferDto, targetCluster, isContinue, sourceCluster);


		transferResultDto.setStatus(true);
		transferResultDto.setErrNamespaceDtos(param.get(Constant.TRANSFER_NAMESPACE_ERROR));
		return transferResultDto;
	}

	/**
	 * 保存namespace的绑定关系
	 * @param clusterTransferDto
	 * @param currentCluster
	 * @param isContinue
	 * @return
	 */
	private Map<String,List<ErrorNamespaceDto>> saveBindNamespace(List<ClusterTransferDto> clusterTransferDto,Cluster currentCluster,Cluster sourceCluster,boolean isContinue) throws Exception {
		List<TransferBindNamespace> bindNamespace  =  bindNamespace(isContinue,clusterTransferDto);
		transferBindNamespaceMapper.saveBindNamespaces(bindNamespace);
		Map<String,List<ErrorNamespaceDto>> param = updateBindNamespace(clusterTransferDto.get(0),packageNamespaceDto(clusterTransferDto), currentCluster,sourceCluster);
		return param;
	}

	/**
	 * 得到要保存的namespace的绑定关系 判断是否是断点续传 如果是则查找目前失败的namespace绑定关系 不是则从前端拿到
	 * @param isContinue
	 * @param clusterTransferDto
	 * @return
	 */
	private List<TransferBindNamespace> bindNamespace(boolean isContinue,List<ClusterTransferDto> clusterTransferDto) throws Exception {
		return isContinue?transferBindNamespaceMapper.queryErrorNamespace(clusterTransferDto.get(0).getTenantId(),clusterTransferDto.get(0).getTargetClusterId()):addBindNamespaceData(clusterTransferDto);
	}

	@Override
	public void transferDeploy(List<ErrorNamespaceDto> namespaces,TransferClusterBackup transferClusterBackup,
											  List<ClusterTransferDto> clusterTransferDto,
											  Cluster targetCluster,boolean isContinue,Cluster sourceCluster) throws Exception {
		DeployResultDto deployResultDto= generateTransferDeploymentAndTransferDeploy(namespaces, clusterTransferDto, transferClusterBackup, sourceCluster);
		List<TransferBindDeploy> deployList = deployResultDto.getDeploys();
		if (deployList.isEmpty()) {
			return ;
		}
		transferDeployMapper.saveTransferList(deployList);
		//
		List<DeploymentTransferDto> deploymentTransferDtos =  deployResultDto.getDeploymentTransferDtos();
		if(isContinue){
			//断点续传
			List<TransferBindDeploy> transferBindDeploys = transferDeployMapper.queryErrorBindDeploy(clusterTransferDto.get(0).getTenantId(), clusterTransferDto.get(0).getTargetClusterId());
			deploymentTransferDtos = generateTransferDtoList(transferBindDeploys);
			deploymentTransferDtos.addAll(deployResultDto.getDeploymentTransferDtos());
		}
		createDeployment(deploymentTransferDtos, transferClusterBackup, sourceCluster, targetCluster);
	}

	private void updateDeployResult(Map<String,Object> params){
		List<TransferBindDeploy> updateDeploys = (List<TransferBindDeploy>) params.get(Constant.ERROR_BIND_DEPLOY);
		transferDeployMapper.updateDeploys(updateDeploys);
	}

	@Override
	public ActionReturnUtil getDeployAndStatefulSet(List<ClusterTransferDto> clusterTransferDtoList) throws Exception {
		if (CollectionUtils.isEmpty(clusterTransferDtoList)) {
			return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
		}
		List<Deployment> deploymentK8S = new ArrayList<>();
		/*List<StatefulSet> statefulSetK8S = new ArrayList<>();*/
		List<BaseResource> applications = new ArrayList<>();
		CountDownLatch countDownLatchApp = new CountDownLatch(clusterTransferDtoList.size());
		for (ClusterTransferDto tenantInfo : clusterTransferDtoList) {
			ThreadPoolExecutorFactory.executor.execute(new Runnable() {
				@Override
				public void run() {
					List<BindNameSpaceDto> namespaceList = tenantInfo.getBindNameSpaceDtos();
					namespaceList.stream().forEach(x -> {
						try {
							String namespace = x.getOldNameSpace();
							Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
							DeploymentList deployments = deploymentsService.getDeployments(namespace, null, cluster);
							deploymentK8S.addAll(deployments.getItems());
							/*StatefulSetList statefulSets = statefulSetService.listStatefulSets(namespace, null, null, cluster);
							statefulSetK8S.addAll(statefulSets.getItems());*/
							K8SClientResponse response = tprApplication.listApplicationByNamespace(namespace, null, null, HTTPMethod.GET, cluster);
							if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
								BaseResourceList tpr = JsonUtil.jsonToPojo(response.getBody(), BaseResourceList.class);
								if (tpr != null) {
									applications.addAll(tpr.getItems());
								}
							}
						} catch (Exception e) {
							logger.error("集群迁移获取数量错误", e);
						}
					});
					countDownLatchApp.countDown();
				}
			});
		}
		countDownLatchApp.await();
		List<DeploymentDto> deploymentList = new ArrayList<>();
		Map<String,List<DeploymentDto>> applicationMap = new HashMap<>();
		deploymentK8S.stream().forEach(dep -> {
			if (dep.getSpec().getReplicas() == 0) {
				return;
			}
			DeploymentDto deploymentDto = new DeploymentDto();
			deploymentDto.setServiceType(Constant.DEPLOYMENT);
			deploymentDto.setDeployName(dep.getMetadata().getName());
			deploymentDto.setNamespace(dep.getMetadata().getNamespace());
			Map<String, Object> label = dep.getMetadata().getLabels();
			boolean isInApp = false;
			for (Map.Entry<String, Object> vo : label.entrySet()) {
				if (!vo.getKey().startsWith(TOPO_LABEL_KEY)) {
					continue;
				}
				String appName = getApplication(vo.getKey(), applications, dep.getMetadata().getNamespace());
				if (StringUtils.isNotBlank(appName)){
					if (!applicationMap.containsKey(appName)) {
						List<DeploymentDto> depInApp = new ArrayList<>();
						depInApp.add(deploymentDto);
						applicationMap.put(appName, depInApp);
					} else {
						List<DeploymentDto> oldDepList = applicationMap.get(appName);
						oldDepList.add(deploymentDto);
					}
					isInApp = true;
					break;
				}
			}
			if (isInApp) {
				return;
			}
			deploymentList.add(deploymentDto);
		});
/*		statefulSetK8S.stream().forEach(sta -> {
			if (sta.getSpec().getReplicas() == 0) {
				return;
			}
			DeploymentDto deploymentDto = new DeploymentDto();
			deploymentDto.setServiceType(Constant.STATEFULSET);
			deploymentDto.setDeployName(sta.getMetadata().getName());
			deploymentDto.setNamespace(sta.getMetadata().getNamespace());
			Map<String, Object> label = sta.getMetadata().getLabels();
			boolean isInApp = false;
			for (Map.Entry<String, Object> vo : label.entrySet()) {
				if (!vo.getKey().startsWith(TOPO_LABEL_KEY)) {
					continue;
				}
				String appName = getApplication(vo.getKey(), applications, sta.getMetadata().getNamespace());
				if (StringUtils.isNotBlank(appName)){
					if (!applicationMap.containsKey(appName)) {
						List<DeploymentDto> depInApp = new ArrayList<>();
						depInApp.add(deploymentDto);
						applicationMap.put(appName, depInApp);
					} else {
						List<DeploymentDto> oldDepList = applicationMap.get(appName);
						oldDepList.add(deploymentDto);
					}
					isInApp = true;
					break;
				}
			}
			if (isInApp) {
				return;
			}
			deploymentList.add(deploymentDto);
		});*/
		Map<String, Object> result = new HashMap<>();
		result.put("app", applicationMap);
		result.put("deployment", deploymentList);
		return ActionReturnUtil.returnSuccessWithData(result);
	}

	private String getApplication(String key, List<BaseResource> applications, String namespace) {
		for (BaseResource br : applications) {
			if (br.getMetadata().getLabels().containsKey(key) && namespace.equals(br.getMetadata().getNamespace())) {
				return br.getMetadata().getName();
			}
		}
		return null;
	}

	private ClusterTransferBackupDto convert(TransferClusterBackup backup){
		ClusterTransferBackupDto dto = new ClusterTransferBackupDto();
		dto.setId(backup.getId());
		dto.setErrMsg(backup.getErrMsg());
		dto.setCreateTime(backup.getCreateTime());
		dto.setUpdateTime(backup.getUpdateTime());
		dto.setTransferClusterPercent(backup.getTransferClusterPercent());
		dto.setOldClusterId(backup.getOldClusterId());
		dto.setTenantId(backup.gettenantId());
		try {
			TenantBinding tenant = tenantService.getTenantByTenantid(backup.gettenantId());
			if (tenant != null) {
				dto.setTenantName(tenant.getTenantName());
				dto.setTenantAliasName(tenant.getAliasName());
			}
		} catch (Exception e) {
			logger.warn("select tenantName error, tenantId:{}", backup.gettenantId());
		}

		dto.setTransferClusterId(backup.getTransferClusterId());
		try {
			Cluster cluster = clusterService.findClusterById(backup.getTransferClusterId());
			if (cluster != null) {
				dto.setTransferClusterName(cluster.getName());
				dto.setTransferClusterAliasName(cluster.getAliasName());
			}
		} catch (Exception e) {
			logger.error("集群id未找到对应的集群,clusterId:{}", backup.getTransferClusterId());
			dto.setTransferClusterName("unknown");
			dto.setTransferClusterAliasName("unknown");
		}
		return dto;
	}

	
}
