package com.harmonycloud.service.application.impl;

import com.alibaba.fastjson.JSON;
import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.*;
import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.dao.cluster.TransferBindDeployMapper;
import com.harmonycloud.dao.cluster.TransferBindNamespaceMapper;
import com.harmonycloud.dao.cluster.TransferClusterBackupMapper;
import com.harmonycloud.dao.cluster.TransferClusterMapper;
import com.harmonycloud.dao.cluster.bean.TransferBindDeploy;
import com.harmonycloud.dao.cluster.bean.TransferBindNamespace;
import com.harmonycloud.dao.cluster.bean.TransferClusterBackup;
import com.harmonycloud.dao.tenant.bean.NamespaceLocal;
import com.harmonycloud.dao.tenant.bean.TenantBinding;
import com.harmonycloud.dao.tenant.bean.TenantClusterQuota;
import com.harmonycloud.dto.application.*;
import com.harmonycloud.dto.cluster.*;
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
import com.harmonycloud.service.application.ClusterTransferService;
import com.harmonycloud.service.application.DeploymentsService;
import com.harmonycloud.service.application.PersistentVolumeService;
import com.harmonycloud.service.application.RouterService;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.tenant.*;
import com.harmonycloud.service.tenant.NamespaceService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import static com.harmonycloud.service.platform.constant.Constant.TOPO_LABEL_KEY;

@Service
public class ClusterTransferServiceImpl implements ClusterTransferService {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
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

    /**
     * 迁移对应的服务 在新集群上创建相同的服务 需要创建的有 ingress namespce configmap pv pvc deployment或者statefulset
     */
    @Override
	public ActionReturnUtil transferDeployService(DeploymentTransferDto deploymentTransferDto) throws Exception {
		return ActionReturnUtil.returnSuccess();
	}

	@Override
	public ActionReturnUtil transferCluster(List<ClusterTransferDto> clusterTransferDto) throws Exception {

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
		Map<String,Double> clusterResource = getClusterUsage(targetCluster.getId());
		Double unUsedCpu = clusterResource.get("clusterUnUsedCpu");
		Double unUsedMemory = clusterResource.get("clusterUnUsedMemory");
		Double requiredCpu = 0.0;
		Double requiredMemory = 0.0;
		for (ClusterTransferDto clusterTransferDto : clusterTransferDtos) {
			List<BindNameSpaceDto> bindNameSpaceDtos = clusterTransferDto.getBindNameSpaceDtos();
			for (BindNameSpaceDto bindNameSpaceDto : bindNameSpaceDtos) {
				NamespaceLocal namespaceLocal = namespaceLocalService.getNamespaceByName(bindNameSpaceDto.getName());
				//新的分区不存在则需要创建，新分区配额大小为原分区已经使用的量，需要校验新建分区配额是否足够
				if (namespaceLocal == null) {
					Map<String,Object> detail = namespaceService.getNamespaceQuota(bindNameSpaceDto.getOldNameSpace());
					List<String> usedCpu = (List<String>) detail.get("cpu");
					List<String> usedMemory = (List<String>) detail.get("memory");
					requiredCpu += Double.parseDouble(usedCpu.get(1));
					if (requiredCpu > unUsedCpu) {
						return false;
					}
					requiredMemory += mathMemory(usedMemory.get(1));
					if (requiredMemory > unUsedMemory) {
						return false;
					}
				} else {
					//已经存在分区判断剩余的资源是否足够创建迁移过来的服务， 暂不校验
					/*Double requiredNsCpu = 0.0;
					Double requiredNsMemory = 0.0;
					List<DeploymentDto> deploymentDtos = bindNameSpaceDto.getDeploymentDto();
					for (DeploymentDto deploymentDto : deploymentDtos) {
						if (deploymentDto.getServiceType().equals(DEPLOYMENT)) {
							// 获取特定的deployment
							K8SClientResponse depRes = dpService.doSpecifyDeployment(bindNameSpaceDto.getOldNameSpace(), deploymentDto.getDeployName(), null, null, HTTPMethod.GET, sourceCluster);
							if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus())) {
								throw new MarsRuntimeException(DictEnum.APPLICATION.phrase(), ErrorCodeMessage.QUERY_FAIL);
							}
							Deployment dep = JsonUtil.jsonToPojo(depRes.getBody(), Deployment.class);

						}
					}*/
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

	private boolean checkNamespace(List<ClusterTransferDto> clusterTransferDtos) throws Exception {
		return namespaceService.checkTransferResource(packageNamespaceDto(clusterTransferDtos));
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
		NamespaceDto namespaceDto = new NamespaceDto();
		namespaceDto.setAliasName(bindNameSpaceDto.getAliasName());
		namespaceDto.setName(bindNameSpaceDto.getName());
		namespaceDto.setTenantId(clusterTransferDto.getTenantId());
		QuotaDto quota = new QuotaDto();
		namespaceDto.setClusterId(clusterTransferDto.getTargetClusterId());
		List<String> usedCpu = (List<String>) detail.get("cpu");
		List<String> usedMemory = (List<String>) detail.get("memory");
		quota.setCpu(usedCpu.get(1));
		quota.setMemory(mathMemory(usedMemory.get(1))+"Gi");
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
					// 失败回滚
					namespaceService.deleteNamespace(namespaceDto.getTenantId(), namespaceDto.getName());
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
		return this.namespaceLocalService.createTransferNamespace(namespaceLocal);
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

	private Map<String,List<ErrorNamespaceDto>> updateBindNamespace(ClusterTransferDto clusterTransferDtos, List<NamespaceDto> namespaceDtos, Cluster currentCluster ,Cluster oldCluster) throws Exception {

		//创建该租户在新集群的配额
		TenantClusterQuota tenantClusterQuota = tenantClusterQuotaService.getClusterQuotaByTenantIdAndClusterId(namespaceDtos.get(0).getTenantId(),oldCluster.getId());
		tenantClusterQuota.setIcNames(null);
		tenantClusterQuota.setClusterName(currentCluster.getName());
		tenantClusterQuota.setClusterId(currentCluster.getId());
		tenantClusterQuota.setId(null);
		try{
			tenantClusterQuotaService.createClusterQuota(tenantClusterQuota);
		}catch (Exception e){
			logger.error("创建集群配额失败，TenantClusterQuota：{}，error信息：{}", tenantClusterQuota, e);
		}
		//创建k8s 分区，并返回创建正确和错误的信息
		Map<String,List<ErrorNamespaceDto>> param = createNamespace(namespaceDtos, currentCluster);
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
		}
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
	private ErrDeployDto createIngress(DeploymentTransferDto deploymentTransferDto) throws Exception{
		ActionReturnUtil actionReturnUtil = routerService.listExposedRouterWithIngressAndNginx(deploymentTransferDto.getCurrentNameSpace(),deploymentTransferDto.getCurrentDeployName(),deploymentTransferDto.getProjectId());
		List<Map<String, Object>> routerList = (List<Map<String, Object>>) actionReturnUtil.getData();
		List<HttpRuleDto> httpRuleDtos = new ArrayList<>();
		List<TcpRuleDto> rules = new ArrayList<>();
		for (Map<String, Object> map : routerList) {
			if(map.get("type").equals(Constant.PROTOCOL_TCP)||map.get("type").equals(Constant.PROTOCOL_UDP)){
				return createTcpIngress(deploymentTransferDto, map, rules);
			}
			if(map.get("type").equals(Constant.PROTOCOL_HTTP)){
				return createHttpIngress(deploymentTransferDto, map, httpRuleDtos);
			}
		}
		return null;
	}
	 
	 
	 private String splitHostname(String hostname){
		 String host = "";
		 if(StringUtils.isNotEmpty(hostname)){
			host = Arrays.asList(hostname.split("/")).get(0);
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
	 * @param currentCluster
	 * @param transferCluster
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws IntrospectionException
	 */
	private ErrDeployDto createPVC(DeploymentTransferDto deploymentTransferDto,Cluster currentCluster,Cluster transferCluster, List<ErrDeployDto> errDeployDtos) throws Exception {
		K8SClientResponse k8sClientResponse = getPVC(deploymentTransferDto.getCurrentDeployName(),deploymentTransferDto.getCurrentNameSpace(), currentCluster);
		List<PersistentVolumeClaim> persistentVolumeClaims = null;
		//如果返回404代表当前服务并未创建pvc 则可向下创建
		ErrDeployDto errDeployDto = new ErrDeployDto();
		if (k8sClientResponse.getStatus() == Constant.HTTP_404){
			errDeployDto.setDeployName(deploymentTransferDto.getCurrentDeployName());
			errDeployDto.setErrMsg("创建pvc失败");
			return errDeployDto;
		}
		PersistentVolumeClaimList persistentVolumeClaim = K8SClient.converToBean(k8sClientResponse, PersistentVolumeClaimList.class);
		if(!persistentVolumeClaim.getItems().isEmpty()){
			K8SClientResponse pvcRes = getPVC(deploymentTransferDto.getCurrentDeployName(), deploymentTransferDto.getNamespace(), transferCluster);
			checkK8SClientResponse(pvcRes,deploymentTransferDto.getCurrentDeployName());
			PersistentVolumeClaimList transferPersistentVolumeClaimc = K8SClient.converToBean(pvcRes, PersistentVolumeClaimList.class);
			if(CollectionUtils.isNotEmpty(transferPersistentVolumeClaimc.getItems())){
				persistentVolumeClaims = transferPersistentVolumeClaimc.getItems();
				persistentVolumeClaims.stream().forEach(x-> {
					try {
						errDeployDtos.add(updatePVC(x, transferCluster, deploymentTransferDto.getCurrentDeployName()));
					}catch (Exception e) {
						logger.error("更新pvc错误，{}", x, e);
					}
				});
			}else{
				persistentVolumeClaims = persistentVolumeClaim.getItems();
				persistentVolumeClaims.stream().forEach(x->errDeployDtos.add(createPVC(replacePersistentVolumeClaim(x, deploymentTransferDto), transferCluster, deploymentTransferDto.getNamespace(),deploymentTransferDto.getCurrentDeployName(),errDeployDto)));
			}
		}
		return null;
	}
	
	/**
	 * 更新pv pvc
	 * @param persistentVolumeClaim
	 * @param transferCluster
	 */
	private ErrDeployDto updatePVC(PersistentVolumeClaim persistentVolumeClaim,Cluster transferCluster,String deployName) throws Exception {
		PersistentVolume persistentVolume = pvService.getPvByName(persistentVolumeClaim.getSpec().getVolumeName(),transferCluster);
		ErrDeployDto errDeployDto = persistentVolumeService.transferPV(persistentVolume, transferCluster,deployName);
		if(!Objects.isNull(errDeployDto)){
			return errDeployDto;
		}
		pvcService.updatePvcByName(persistentVolumeClaim, transferCluster);
		return null;
	}

	/**
	 * 创建pvc
	 * @param persistentVolumeClaim
	 * @param transferCluster
	 * @param namespace
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws IntrospectionException
	 */
	private ErrDeployDto createPVC(PersistentVolumeClaim persistentVolumeClaim,Cluster transferCluster,String namespace,String deployName,ErrDeployDto errDeployDto) {
		try {
			Map<String, Object> bodys = CollectionUtil.transBean2Map(persistentVolumeClaim);
			K8SURL url = new K8SURL();
			url.setNamespace(namespace).setResource(Resource.PERSISTENTVOLUMECLAIM);
			K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.POST, generateHeader(), bodys, transferCluster);
			errDeployDto = checkK8SClientResponse(response,deployName);
			if(!Objects.isNull(errDeployDto)){
				return errDeployDto;
			}
			PersistentVolume persistentVolume = pvService.getPvByName(persistentVolumeClaim.getSpec().getVolumeName(),transferCluster);
			persistentVolumeService.transferPV(persistentVolume, transferCluster,deployName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 创建或更新configmap
	 * @param deploymentTransferDto
	 * @param currentCluster
	 * @param transferCluter
	 */
	private ErrDeployDto createOrUpdateConfigMap(DeploymentTransferDto deploymentTransferDto,Cluster currentCluster,Cluster transferCluter) throws Exception {
		K8SClientResponse response = configMapService.doSepcifyConfigmap(deploymentTransferDto.getCurrentNameSpace(), deploymentTransferDto.getCurrentDeployName(),currentCluster);
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
			K8SClientResponse result = new K8sMachineClient().exec(url, HTTPMethod.POST, generateHeader(), bodys, transferCluter);
			errDeployDto = checkK8SClientResponse(result,deploymentTransferDto.getCurrentDeployName());
			if(!Objects.isNull(errDeployDto)){
				return errDeployDto;
			}
		}
		return null;
	}

	/**
	 * 创建deployment 创建前判断当前集群是否有deployment 有则更新
	 * @param deploymentTransferDto
	 * @param currentCluster
	 * @param oldCluster
	 * @return
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws IntrospectionException
	 */
	private ErrDeployDto createDeployment(DeploymentTransferDto deploymentTransferDto,Cluster currentCluster,
										  Cluster oldCluster,ErrDeployDto errDeployDto,String deployName) throws Exception{
		K8SClientResponse response = null;
		Deployment deployment = null;
		response = dpService.doSpecifyDeployment(deploymentTransferDto.getNamespace(),deploymentTransferDto.getCurrentDeployName(), null, null, HTTPMethod.GET, currentCluster);
		if (HttpStatusUtil.isSuccessStatus(response.getStatus())){
            deployment = JsonUtil.jsonToPojo(response.getBody(), Deployment.class);
            if(deployment!=null){
                updateDeployment(deployment, deploymentTransferDto,oldCluster);
            }
        }
		response = dpService.doSpecifyDeployment(deploymentTransferDto.getCurrentNameSpace(),deploymentTransferDto.getCurrentDeployName(), null, null, HTTPMethod.GET, oldCluster);
        errDeployDto = checkK8SClientResponse(response,deployName);
		if(!Objects.isNull(errDeployDto)){
			return errDeployDto;
		}
		deployment = JsonUtil.jsonToPojo(response.getBody(), Deployment.class);
		deployment= replaceDeployment(deployment, deploymentTransferDto);
		createApp(oldCluster,currentCluster, deployment.getMetadata().getLabels(),deploymentTransferDto);
		response = dpService.doSpecifyDeployment(deploymentTransferDto.getNamespace(), null, generateHeader(), CollectionUtil.transBean2Map(deployment), HTTPMethod.POST, currentCluster);
        errDeployDto =checkK8SClientResponse(response,deployName);
		if(!Objects.isNull(errDeployDto)){
			return errDeployDto;
		}
		return errDeployDto;
	}
	private void createApp(Cluster oldCluster, Cluster newCluster ,Map<String,Object> labels,DeploymentTransferDto deploymentTransferDto)  {
		String projectId = (String) labels.get("harmonycloud.cn/projectId");
		String appName = "";
		for (String label:labels.keySet()){
				if (label.contains(projectId)) {
					labels.put(label, deploymentTransferDto.getCurrentDeployName());
					appName = StringUtils.substringAfter(label, projectId + "-");
				}
		}
		try {
			K8SClientResponse response  = tprApplication.getApplicationByName(deploymentTransferDto.getCurrentNameSpace(), appName, null, null, HTTPMethod.GET, oldCluster);
			if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
				logger.error("查询应用失败deploymentTransferDto:{},异常信息：{}", deploymentTransferDto, response.getBody());
			}
			BaseResource appCrd = JsonUtil.jsonToPojo(response.getBody(), BaseResource.class);
			Map<String,Object> appLebels = appCrd.getMetadata().getLabels();
			for (String label:appLebels.keySet()){
				if (label.contains(projectId)) {
					appLebels.put(label, deploymentTransferDto.getCurrentDeployName());
				}
			}

			BaseResource base = new BaseResource();
			ObjectMeta mate = new ObjectMeta();
			mate.setNamespace(deploymentTransferDto.getNamespace());
			mate.setName(appName);

			Map<String, Object> anno = new HashMap<String, Object>();
			anno.put("nephele/annotation", appCrd.getMetadata().getAnnotations());
			mate.setAnnotations(anno);
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
	}
	/**
	 * 迁移服务 两种类型 stafulset(有状态的) deployment(无状态的)
	 * @param deploymentTransferDto
	 * @param currentCluster
	 * @param oldCluster
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws IntrospectionException
	 */
	private ErrDeployDto create(DeploymentTransferDto deploymentTransferDto,Cluster currentCluster,Cluster oldCluster) throws Exception{
		switch (deploymentTransferDto.getCurrentServiceType()) {
			case Constant.STATEFULSET:
				createService(deploymentTransferDto, currentCluster, oldCluster);
				ErrDeployDto errDeployDto = createStatefulSet(deploymentTransferDto, currentCluster, oldCluster);
				return errDeployDto;
			case Constant.DEPLOYMENT:
				errDeployDto = createService(deploymentTransferDto, currentCluster, oldCluster);
				errDeployDto = createDeployment(deploymentTransferDto, currentCluster, oldCluster,errDeployDto,deploymentTransferDto.getCurrentDeployName());
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
	 * @param transferCluster
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws IntrospectionException
	 */
	private ErrDeployDto createService(DeploymentTransferDto deploymentTransferDto,Cluster currentCluster,Cluster transferCluster) throws Exception{
		K8SClientResponse rsRes  =null;
		com.harmonycloud.k8s.bean.Service service =null;
		K8SURL k8surl = new K8SURL();
		ErrDeployDto errDeployDto = new ErrDeployDto();
		rsRes = serviceService.doSepcifyService(deploymentTransferDto.getNamespace(),deploymentTransferDto.getCurrentDeployName(), null, null, HTTPMethod.GET, transferCluster);
		if (rsRes.getStatus() != Constant.HTTP_404){
			errDeployDto = new ErrDeployDto();
			errDeployDto.setDeployName(deploymentTransferDto.getCurrentDeployName());
			errDeployDto.setErrMsg("创建service失败");
			return errDeployDto;
		}
		rsRes = serviceService.doSepcifyService(deploymentTransferDto.getCurrentNameSpace(),deploymentTransferDto.getCurrentDeployName(), null, null, HTTPMethod.GET, currentCluster);
		errDeployDto = checkK8SClientResponse(rsRes,deploymentTransferDto.getCurrentDeployName());
		if(!Objects.isNull(errDeployDto)){
			return errDeployDto;
		}
		k8surl.setNamespace(deploymentTransferDto.getNamespace()).setResource(Resource.SERVICE);
		service = JsonUtil.jsonToPojo(rsRes.getBody(), com.harmonycloud.k8s.bean.Service.class);
		K8SClientResponse sResponse = new K8sMachineClient().exec(k8surl, HTTPMethod.POST, generateHeader(),CollectionUtil.transBean2Map(replaceService(service, deploymentTransferDto)), transferCluster);
		checkK8SClientResponse(sResponse,deploymentTransferDto.getCurrentDeployName());
		if(!Objects.isNull(errDeployDto)){
			return errDeployDto;
		}
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
		tcpRuleDto.setPort((String)address.get("containerPort"));
		tcpRuleDto.setTargetPort((String)address.get("externalPort"));
		tcpRuleDto.setProtocol((String)map.get("type"));
		rules.add(tcpRuleDto);
		svcRouterDto.setRules(rules);
		return routerService.transferRuleDeploy(svcRouterDto,deploymentTransferDto.getCurrentDeployName());
	}

	/**
	 * 创建http类型的ingress
	 * @param deploymentTransferDto
	 * @param map
	 * @param httpRuleDtos
	 * @throws MarsRuntimeException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws IntrospectionException
	 */
	private ErrDeployDto createHttpIngress(DeploymentTransferDto deploymentTransferDto,Map<String, Object> map,List<HttpRuleDto> httpRuleDtos) throws Exception {
		ParsedIngressListDto parsedIngressListDto = new ParsedIngressListDto();
		parsedIngressListDto.setNamespace(deploymentTransferDto.getNamespace());
		parsedIngressListDto.setName((String)map.get("name"));
		parsedIngressListDto.setIcName((String)map.get("icName"));
		Map<String, Object> labels = new HashMap<>();
		labels.put("app", deploymentTransferDto.getCurrentDeployName());
		labels.put("tenantId", deploymentTransferDto.getProjectId());
		List<Map<String, Object>> address = (List<Map<String, Object>>)map.get("address");
		for (Map<String, Object> map2 : address) {
			List<String> host = (List<String>)map2.get("hostname");
			parsedIngressListDto.setHost(splitHostname(host.get(0)));
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
		return routerService.transferIngressCreate(parsedIngressListDto,deploymentTransferDto.getCurrentDeployName());
	}

	/**
	 * 校验从k8s获取的响应是否正确
	 * @param response
	 * @param
	 */
	private ErrDeployDto checkK8SClientResponse(K8SClientResponse response,String deployName){
		if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
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
	    queryP.put("labelSelector", Constant.TYPE_DEPLOYMENT + Constant.EQUAL + name);
        K8SClientResponse pvcsRes = pvcService.doSepcifyPVC(namespace, queryP, HTTPMethod.GET, cluster);
        return pvcsRes;
    }
	
	/**
	 * 替换原有deployment的属性 替换的有 namespace，selflink，deployment label，podlabel
	 * @param deployment
	 * @param deploymentTransferDtos
	 * @return
	 */
	private Deployment replaceDeployment(Deployment deployment,DeploymentTransferDto deploymentTransferDtos) {
		deployment.getMetadata().setNamespace(deploymentTransferDtos.getNamespace());
		deployment.getMetadata().setSelfLink(deployment.getMetadata().getSelfLink().replace(deployment.getMetadata().getNamespace(),deploymentTransferDtos.getNamespace()));
		deployment.getMetadata().setResourceVersion(null);
		deployment.getMetadata().setLabels(replaceLabels(deployment.getMetadata().getLabels(), deployment.getMetadata().getNamespace()));
		deployment.getSpec().getTemplate().getMetadata().setLabels(deployment.getSpec().getTemplate().getMetadata().getLabels());
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
	 * @return
	 */
	private PersistentVolumeClaim replacePersistentVolumeClaim(PersistentVolumeClaim persistentVolumeClaim,DeploymentTransferDto deploymentTransferDto){
		persistentVolumeClaim.getMetadata().setNamespace(deploymentTransferDto.getNamespace());
		persistentVolumeClaim.getMetadata().setResourceVersion(null);
		persistentVolumeClaim.getSpec().getSelector().setMatchLabels(replacePVCLabels(persistentVolumeClaim.getSpec().getSelector().getMatchLabels(), deploymentTransferDto));
		persistentVolumeClaim.getMetadata().setSelfLink(persistentVolumeClaim.getMetadata().getSelfLink().replace(persistentVolumeClaim.getMetadata().getNamespace(),deploymentTransferDto.getNamespace()));
		persistentVolumeClaim.getMetadata().setLabels(replacePVCLabels(persistentVolumeClaim.getMetadata().getLabels(), deploymentTransferDto));
		return persistentVolumeClaim;
	}
	
	
	/**
	 * 替换pvc的labels
	 * @param labels
	 * @param deploymentTransferDto
	 * @return
	 */
	private Map<String, Object> replacePVCLabels(Map<String,Object> labels, DeploymentTransferDto deploymentTransferDto){
		String removeKey = null;
		String values = null;
		for(String key:labels.keySet()){
			if(key.contains(deploymentTransferDto.getCurrentNameSpace())){
				values = String.valueOf(labels.get(key));
				removeKey = key;
			}
			if(labels.get(key).equals(deploymentTransferDto.getCurrentProjectId())){
				labels.put(key, deploymentTransferDto.getProjectId());
			}
		}
		labels.put(splitPVCLabelKey(removeKey, deploymentTransferDto.getNamespace()),values);
		labels.remove(removeKey);
		return labels;
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

	private DeployResultDto generateTransferDeploymentAndTransferDeploy(List<ErrorNamespaceDto> errorNamespaceDtos,List<ClusterTransferDto> clusterTransferDtos,Cluster oldCluster) throws Exception {
		Map<String,String> param = successNamespceBind(errorNamespaceDtos, clusterTransferDtos);
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
			K8SClientResponse clientResponse = dpService.doDeploymentsByNamespace(param.get(namespaceDto.getNamespace()),null, null, HTTPMethod.GET, oldCluster);
			DeploymentList deploymentList = JsonUtil.jsonToPojo(clientResponse.getBody(), DeploymentList.class);
			if (deploymentList != null && !CollectionUtils.isEmpty(deploymentList.getItems())) {
				for(Deployment deployment:deploymentList.getItems()){
					if (deployNames.contains(deployment.getMetadata().getName())){
						list.add(generateTransferBindDeploy(deployment, clusterTransferDtos.get(0), param, namespaceDto.getNamespace()));
						deploymentTransferDtos.add(generateTransferDto(deployment,  clusterTransferDtos.get(0), param, namespaceDto));
					}
				}
			}
		}
		deployResultDto.setDeploymentTransferDtos(deploymentTransferDtos);
		deployResultDto.setDeploys(list);
		return deployResultDto;
	}

	private Map<String,String> successNamespceBind(List<ErrorNamespaceDto> errorNamespaceDtos,List<ClusterTransferDto> clusterTransferDtos){
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

	private Map<String,Object> createDeployment(List<DeploymentTransferDto> deploymentTransferDtos,Cluster oldCluster,Cluster currentCluster) throws Exception {
		//TODO 稍后改为先取deployment ，然后根据deployment创建pv、cm等资源
		int index = 0;
		Map<String,Object> params = new HashMap<>();
		List<ErrDeployDto> errDeployDtos = new ArrayList<>();
		List<TransferBindDeploy> errorBindDeploy = new ArrayList<>();
		for (DeploymentTransferDto deploymentTransferDto : deploymentTransferDtos) {
			TransferBindDeploy transferBindDeploy = new TransferBindDeploy();
			transferBindDeploy.setStepId(1);
			ErrDeployDto errDeployDto  = null;
			errDeployDto = createIngress(deploymentTransferDto);
			if(errDeployDto!=null){
				errDeployDtos.add(errDeployDto);
				transferBindDeploy.setErrMsg(errDeployDto.getErrMsg());
				transferBindDeploy.setDeployName(errDeployDto.getDeployName());
				transferBindDeploy.setStepId(2);
				errorBindDeploy.add(transferBindDeploy);
				continue;
			}
			errDeployDto = createPVC(deploymentTransferDto, currentCluster, oldCluster,errDeployDtos);
			if(errDeployDto!=null){
				errDeployDtos.add(errDeployDto);
				transferBindDeploy.setErrMsg(errDeployDto.getErrMsg());
				transferBindDeploy.setDeployName(errDeployDto.getDeployName());
				transferBindDeploy.setStepId(3);
				errorBindDeploy.add(transferBindDeploy);
				continue;
			}
			//TODO configmap应该从deployment取 先注释掉
			/*errDeployDto = createOrUpdateConfigMap(deploymentTransferDto, currentCluster, oldCluster);
			if(errDeployDto!=null){
				errDeployDtos.add(errDeployDto);
				transferBindDeploy.setErrMsg(errDeployDto.getErrMsg());
				transferBindDeploy.setDeployName(errDeployDto.getDeployName());
				transferBindDeploy.setStepId(4);
				errorBindDeploy.add(transferBindDeploy);
				continue;
			}*/
			errDeployDto = create(deploymentTransferDto, currentCluster, oldCluster);
			if(errDeployDto!=null){
				errDeployDtos.add(errDeployDto);
				transferBindDeploy.setErrMsg(errDeployDto.getErrMsg());
				transferBindDeploy.setDeployName(errDeployDto.getDeployName());
				transferBindDeploy.setStepId(5);
				errorBindDeploy.add(transferBindDeploy);
				continue;
			}
			transferBindDeploy.setDeployName(deploymentTransferDto.getCurrentDeployName());
			transferBindDeploy.setStepId(6);
			transferBindDeploy.setStatus(1);
			errorBindDeploy.add(transferBindDeploy);
			index = deploymentTransferDtos.indexOf(deploymentTransferDto);
		}
		params.put("errDeployDtos",errDeployDtos);
		double transferProgess = index/deploymentTransferDtos.size();
		params.put("progress",transferProgess);
		params.put("errorBindDeploy", errorBindDeploy);
		return params;
	}

	/**
	 * 包装transferDTO对象
	 * @param deployment
	 * @param clusterTransferDto
	 * @param param
	 * @return
	 */
	private TransferBindDeploy generateTransferBindDeploy(Deployment deployment,ClusterTransferDto clusterTransferDto,Map<String,String> param,String namespace){
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
		transferBindDeploy.setOldClusterId(clusterTransferDto.getCurrentClusterId());
	/*	transferBindDeploy.setOldNamespace(deployment.getMetadata().getNamespace());*/
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
		return ActionReturnUtil.returnSuccessWithData(transferClusterMapper.queryTransferCluster(clusterTransferDto.getClusterId()));
	}

	@Override
	public ActionReturnUtil getDeployDetail(ClusterTransferDetailDto clusterTransferDto) {
		return ActionReturnUtil.returnSuccessWithData(transferDeployMapper.queryTransferDeployDetail(clusterTransferDto.getTenantId(),clusterTransferDto.getClusterId()));
	}

	@Override
	public ActionReturnUtil getDeployDetailBackUp(ClusterTransferDetailDto clusterTransferDto) {

		return ActionReturnUtil.returnSuccessWithData(transferClusterBackUpMapper.queryHistoryBackUp(clusterTransferDto.getClusterId(), clusterTransferDto.getTenantId()));
	}

	private List<DeploymentTransferDto> generateTransferDtoList(List<TransferBindDeploy> transferBindDeploys){
		List<DeploymentTransferDto> list = new ArrayList<>();
		transferBindDeploys.stream().forEach(x->list.add(generateTransferDto(x)));
		return list;
	}

	private DeploymentTransferDto generateTransferDto(TransferBindDeploy transferBindDeploy){
		DeploymentTransferDto deploymentTransferDto = new DeploymentTransferDto();
		deploymentTransferDto.setClusterId(transferBindDeploy.getClusterId());
		deploymentTransferDto.setCurrentClusterId(transferBindDeploy.getOldClusterId());
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
		//todo 暂不需要校验
		/*if(!checkNamespace(clusterTransferDto)){
			transferClusterBackup.setErrMsg(ErrorCodeMessage.TRANSFER_CLUSTER_RESOURCE_ERROR.getReasonChPhrase());
			transferClusterBackUpMapper.insert(transferClusterBackup);
			throw new MarsRuntimeException(ErrorCodeMessage.TRANSFER_CLUSTER_RESOURCE_ERROR);
		}*/
	}


	/**
	 * 校验完成后开始执行迁移集群
	 * @param transferClusterBackup 迁移备份对象
	 * @param clusterTransferDto 传递的迁移参数
	 * @param oldCluster 当前集群
	 * @param currentCluster 目标集群
	 * @param isContinue 是否是断点续传
	 * @return
	 * @throws RuntimeException
	 * @throws ReflectiveOperationException
	 * @throws ReflectiveOperationException
	 * @throws Exception
	 */
	private TransferResultDto excuteTransfer(TransferClusterBackup transferClusterBackup,List<ClusterTransferDto> clusterTransferDto,Cluster oldCluster,Cluster currentCluster,boolean isContinue) throws Exception{
		TransferResultDto transferResultDto = new TransferResultDto();
		transferClusterBackup.setIsContinue(isContinue?(byte)1:(byte)0);
		Map<String,List<ErrorNamespaceDto>> param = saveBindNamespace(clusterTransferDto, currentCluster,oldCluster, isContinue);
		// 这个地方应该是创建成功的namespaces
		List<ErrorNamespaceDto> namespaces = param.get(Constant.TRANSFER_NAMESPACE_SUCCESS);
		Map<String,Object> params = saveBindDeploy(namespaces, clusterTransferDto, currentCluster, isContinue, oldCluster);
		List<ErrDeployDto> errDeployDtos = (List<ErrDeployDto>) params.get(Constant.ERR_DEPLOY_DTOS);
		//updateDeployResult(params);
		transferResultDto.setErrDeployDtos(errDeployDtos);
		transferResultDto.setErrNamespaceDtos(param.get(Constant.TRANSFER_NAMESPACE_ERROR));
		transferClusterBackup.setErrNamespace(JSON.toJSONString(param.get(Constant.TRANSFER_NAMESPACE_ERROR)));
		transferClusterBackup.setErrDeploy(JSON.toJSONString(errDeployDtos));
		transferClusterBackup.setTransferClusterPercent((String) params.get("percent"));
		transferClusterBackup.setTransferClusterPercent("100%");
		transferClusterBackUpMapper.insert(transferClusterBackup);
		return transferResultDto;
	}

	/**
	 * 保存namespace的绑定关系
	 * @param clusterTransferDto
	 * @param currentCluster
	 * @param isContinue
	 * @return
	 */
	private Map<String,List<ErrorNamespaceDto>> saveBindNamespace(List<ClusterTransferDto> clusterTransferDto,Cluster currentCluster,Cluster oldCluster,boolean isContinue) throws Exception {
		List<TransferBindNamespace> bindNamespace  =  bindNamespace(isContinue,clusterTransferDto);
		transferBindNamespaceMapper.saveBindNamespaces(bindNamespace);
		Map<String,List<ErrorNamespaceDto>> param = updateBindNamespace(clusterTransferDto.get(0),packageNamespaceDto(clusterTransferDto), currentCluster,oldCluster);
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


	private Map<String,Object> saveBindDeploy(List<ErrorNamespaceDto> namespaces,List<ClusterTransferDto> clusterTransferDto,
											  Cluster currentCluster,boolean isContinue,Cluster oldCluster) throws Exception {
		DeployResultDto deployResultDto= generateTransferDeploymentAndTransferDeploy(namespaces, clusterTransferDto, oldCluster);
		List<TransferBindDeploy> deployList = deployResultDto.getDeploys();
		if (deployList.isEmpty()) {
			return null;
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
		return createDeployment(deploymentTransferDtos, oldCluster, currentCluster);
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
		List<StatefulSet> statefulSetK8S = new ArrayList<>();
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
							StatefulSetList statefulSets = statefulSetService.listStatefulSets(namespace, null, null, cluster);
							statefulSetK8S.addAll(statefulSets.getItems());
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
		statefulSetK8S.stream().forEach(sta -> {
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
		});
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
	
}
