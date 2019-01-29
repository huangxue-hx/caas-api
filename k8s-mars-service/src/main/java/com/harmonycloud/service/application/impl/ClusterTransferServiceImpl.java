package com.harmonycloud.service.application.impl;

import java.beans.IntrospectionException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


import com.alibaba.druid.sql.visitor.functions.Bin;
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
import com.harmonycloud.dto.cluster.*;
import com.harmonycloud.k8s.bean.*;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.tenant.ProjectService;
import com.harmonycloud.service.tenant.TenantService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.dto.application.DeploymentTransferDto;
import com.harmonycloud.dto.application.HttpRuleDto;
import com.harmonycloud.dto.application.ParsedIngressListDto;
import com.harmonycloud.dto.application.SvcRouterDto;
import com.harmonycloud.dto.application.TcpRuleDto;
import com.harmonycloud.dto.tenant.NamespaceDto;
import com.harmonycloud.dto.tenant.QuotaDto;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.service.ConfigmapService;
import com.harmonycloud.k8s.service.DeploymentService;
import com.harmonycloud.k8s.service.PVCService;
import com.harmonycloud.k8s.service.PvService;
import com.harmonycloud.k8s.service.ServicesService;
import com.harmonycloud.k8s.service.StatefulSetService;
import com.harmonycloud.service.tenant.NamespaceLocalService;
import com.harmonycloud.service.tenant.NamespaceService;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import com.harmonycloud.service.application.ClusterTransferService;
import com.harmonycloud.service.application.RouterService;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.application.PersistentVolumeService;

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

    /**
     * 迁移对应的服务 在新集群上创建相同的服务 需要创建的有 ingress namespce configmap pv pvc deployment或者statefulset
     */
    @Override
	public ActionReturnUtil transferDeployService(DeploymentTransferDto deploymentTransferDto) throws Exception {
    	Cluster currentCluster = namespaceLocalService.getClusterByNamespaceName(deploymentTransferDto.getCurrentNameSpace());
		return ActionReturnUtil.returnSuccess();
	}

	@Override
	public ActionReturnUtil transferCluster(List<ClusterTransferDto> clusterTransferDto) throws Exception {
		Cluster cluster = clusterService.findClusterById(clusterTransferDto.get(0).getTargetClusterId());
		Cluster currentCluster = clusterService.findClusterById(clusterTransferDto.get(0).getCurrentClusterId());
		TransferClusterBackup transferClusterBackup = generateTransferClusterBackup(clusterTransferDto);
		Map<String,Object> params = new HashMap<>();
		if(Objects.isNull(cluster)||Objects.isNull(currentCluster)){
			transferClusterBackup.setErrMsg(ErrorCodeMessage.CLUSTER_NOT_FOUND.getReasonChPhrase());
			transferClusterBackUpMapper.insert(transferClusterBackup);
			throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
		}
		if(!compareUsage(clusterTransferDto.get(0).getTargetClusterId(), clusterTransferDto)){
			transferClusterBackup.setErrMsg(ErrorCodeMessage.CLUSTER_NOT_FOUND.getReasonChPhrase());
			transferClusterBackUpMapper.insert(transferClusterBackup);
			throw new MarsRuntimeException(ErrorCodeMessage.TRANSFER_CLUSTER_RESOURCE_ERROR);
		}
		if(!checkNamespace(clusterTransferDto)){
			transferClusterBackup.setErrMsg(ErrorCodeMessage.CLUSTER_NOT_FOUND.getReasonChPhrase());
			transferClusterBackUpMapper.insert(transferClusterBackup);
			throw new MarsRuntimeException(ErrorCodeMessage.TRANSFER_CLUSTER_RESOURCE_ERROR);
		}
		if(clusterTransferDto.get(0).isContinue()){
			//断点续传
			transferClusterBackup.setIsContinue((byte) 1);
			List<TransferBindNamespace> bindNamespace = transferBindNamespaceMapper.queryErrorNamespace(clusterTransferDto.get(0).getTenantId(),clusterTransferDto.get(0).getTargetClusterId());
			transferClusterMapper.saveBindNamespaces(bindNamespace);
			Map<String,List<ErrorNamespaceDto>> param = updateBindNamespace(clusterTransferDto.get(0),packageNamespaceDto(clusterTransferDto), currentCluster);
			List<ErrorNamespaceDto> namespaces = param.get("successUpdate");
			transferClusterBackup.setErrMsg(param.get("errorUpdate").get(0).getErrMsg());
			transferClusterBackup.setErrNamespace(JSON.toJSONString(param.get("errorUpdate")));
			DeployResultDto deployResultDto= generateTransferDeploymentAndTransferDeploy(namespaces, clusterTransferDto, currentCluster);
			transferDeployMapper.saveTransferList(deployResultDto.getDeploys());
			params = createDeployment(deployResultDto.getDeploymentTransferDtos(), cluster, currentCluster);
			double progress = (double) params.get("progress");
			transferClusterMapper.updatePercent(clusterTransferDto.get(0).getTargetClusterId(),clusterTransferDto.get(0).getTenantId(),progress==1?progress:1-progress);
		}
		if(!Objects.isNull(transferClusterMapper.queryTransferClusterByParam(clusterTransferDto.get(0).getTenantId(),clusterTransferDto.get(0).getTargetClusterId()))){
			//增量续传
		}
		List<TransferBindNamespace> bindNamespaces = addBindNamespaceData(clusterTransferDto);
		transferClusterMapper.saveBindNamespaces(bindNamespaces);
		Map<String,List<ErrorNamespaceDto>> param = updateBindNamespace(clusterTransferDto.get(0),packageNamespaceDto(clusterTransferDto), currentCluster);
		List<ErrorNamespaceDto> namespaces = param.get("successUpdate");
		DeployResultDto deployResultDto= generateTransferDeploymentAndTransferDeploy(namespaces, clusterTransferDto, currentCluster);
		transferDeployMapper.saveTransferList(deployResultDto.getDeploys());
		params = createDeployment(deployResultDto.getDeploymentTransferDtos(), cluster, currentCluster);
		double progress = (double) params.get("progress");
		transferClusterMapper.updatePercent(clusterTransferDto.get(0).getTargetClusterId(),clusterTransferDto.get(0).getTenantId(),progress);
		return null;
	}

	private TransferClusterBackup generateTransferClusterBackup(List<ClusterTransferDto> clusterTransferDto) {
		TransferClusterBackup transferClusterBackup = new TransferClusterBackup();
		transferClusterBackup.setCreateTime(new Date());
		transferClusterBackup.setDeployNum(transferDeployMapper.queryMaxNun(clusterTransferDto.get(0).getTenantId(),clusterTransferDto.get(0).getTargetClusterId()));
		transferClusterBackup.setNamespaceNum(transferBindNamespaceMapper.queryLastNamespaceNum(clusterTransferDto.get(0).getTenantId(),clusterTransferDto.get(0).getTargetClusterId())+1);
		transferClusterBackup.setTanantId(clusterTransferDto.get(0).getTenantId());
		transferClusterBackup.setTransferClusterId(clusterTransferDto.get(0).getTargetClusterId());
		return transferClusterBackup;
	}

	/**
	 * 得到目标迁移集群的剩余容量
	 * @param targetClusterId
	 * @return
	 */
	private Map<String,Double> getClusterUsage(String targetClusterId) throws Exception {
		Map<String,Double> clusterUsage = new HashMap<>();
		ActionReturnUtil actionReturnUtil = clusterService.getClusterResourceUsage(targetClusterId,null);
		Map<String, Map<String, Object>> mapClusterResourceUsage = (Map<String, Map<String, Object>>) actionReturnUtil.getData();
		Map<String,Object> usageParam = mapClusterResourceUsage.get(targetClusterId);
		double surplusMemory = (Double)usageParam.get("clusterFilesystemCapacity")-(Double)usageParam.get("clusterFilesystemUsage");
		double surplusCpu =(Double) usageParam.get("clusterMemoryCapacity")-(Double)usageParam.get("clusterMemoryUsage");
		clusterUsage.put("surplusMemory", surplusMemory);
		clusterUsage.put("surplusCpu", surplusCpu);
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
		Map<String, String> used = (Map<String, String>) resourceQuotaStatus.getUsed();
		String hardMemory = getResource(hard.get("memory"));
		String hardCpu = getResource(hard.get("cpu"));
		params.put("hardCpu",Double.valueOf(hardCpu));
		params.put("hardMemory", Double.valueOf(hardMemory));
		return params;
	}

	/**
	 * 比较集群剩余容量和namespace所需容量
	 * @param targetClusterId
	 * @param clusterTransferDtos
	 * @return
	 */
	private boolean compareUsage(String targetClusterId,List<ClusterTransferDto> clusterTransferDtos) throws Exception {
		Map<String,Double> clusterResource = getClusterUsage(targetClusterId);
		Map<String,List<Double>> namespaceResource = getNamespaceCpu(clusterTransferDtos);
		double namespaceCpu =  namespaceResource.get("cpu").stream().collect(Collectors.summarizingDouble(x->x)).getSum();
		double memorySum = namespaceResource.get("memory").stream().collect(Collectors.summarizingDouble(x->x)).getSum();
		if(clusterResource.get("surplusCpu")>namespaceCpu&&clusterResource.get("surplusMemory")>memorySum){
			return true;
		}
		return false;
	}

	private List<TransferBindNamespace> addBindNamespaceData(List<ClusterTransferDto> clusterTransferDtos) throws Exception{
		List<TransferBindNamespace> transferBindNamespaces = new ArrayList<>();
		if(CollectionUtils.isEmpty(clusterTransferDtos)){
			throw new MarsRuntimeException(ErrorCodeMessage.TRANSFER_CLUSTER_ERROR);
		}
		for (ClusterTransferDto clusterTransferDto : clusterTransferDtos) {
			for (BindNameSpaceDto bindNameSpace : clusterTransferDto.getBindNameSpaceDtos()) {
				transferBindNamespaces.add(generateBindNamespace(clusterTransferDto, bindNameSpace));
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
		return namespaceDto;
	}

	private Map<String, List<ErrorNamespaceDto>> createNamespace(List<NamespaceDto> namespaceDtos, Cluster cluster) throws Exception {
		Map<String,List<ErrorNamespaceDto>> updateNamespace = new HashMap<>();
		List<ErrorNamespaceDto>  successList = new ArrayList<>();
		List<ErrorNamespaceDto>  errorList = new ArrayList<>();
		for (NamespaceDto namespaceDto : namespaceDtos) {
			ErrorNamespaceDto errorNamespaceDto =null;
			if (StringUtils.isEmpty(namespaceDto.getName()) || namespaceDto.getName().indexOf(CommonConstant.LINE) < 0) {
				errorNamespaceDto = new ErrorNamespaceDto();
				errorNamespaceDto.setErrMsg(ErrorCodeMessage.NAMESPACE_NOT_BLANK.getReasonChPhrase());
				errorNamespaceDto.setNamespace(namespaceDto.getName());
				errorList.add(errorNamespaceDto);
				continue;
			}
			//保存到本地数据库
			errorNamespaceDto = this.createLocalNamespace(namespaceDto);
			if(Objects.nonNull(errorNamespaceDto)){
				successList.add(errorNamespaceDto);
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
				errorNamespaceDto = new ErrorNamespaceDto();
				logger.error("调用k8s接口创建namespace失败，错误消息：" + k8SClientResponse.getBody(),k8SClientResponse.getBody());
				Object message = stringObjectMap.get("message");
				if (!Objects.isNull(message) && message.toString().contains("object is being deleted")){
					errorNamespaceDto.setNamespace(namespaceDto.getName());
					errorNamespaceDto.setErrMsg(ErrorCodeMessage.NAMESPACE_CREATE_ERROR_DELETED.getReasonChPhrase());
					errorList.add(errorNamespaceDto);
					continue;
				}
				errorNamespaceDto.setNamespace(namespaceDto.getName());
				errorNamespaceDto.setErrMsg(ErrorCodeMessage.NAMESPACE_CREATE_ERROR.getReasonChPhrase());
				errorList.add(errorNamespaceDto);
			}
		}
		updateNamespace.put("successUpdate",successList);
		updateNamespace.put("errorUpdate",errorList);
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

	private Map<String,List<ErrorNamespaceDto>> updateBindNamespace(ClusterTransferDto clusterTransferDtos, List<NamespaceDto> namespaceDtos, Cluster cluster) throws Exception {
		Map<String,List<ErrorNamespaceDto>> param = createNamespace(namespaceDtos, cluster);
		List<ErrorNamespaceDto> successNamespaceDtos = param.get("successUpdate");
		List<ErrorNamespaceDto> errorNamespaceDtos = param.get("errorUpdate");
		transferBindNamespaceMapper.updateErrorListNamespace(namespaceList(clusterTransferDtos,errorNamespaceDtos, false));
		transferBindNamespaceMapper.updateSuccessListNamespace(namespaceList(clusterTransferDtos,successNamespaceDtos,true));
		return param;
	}

	private List<TransferBindNamespace> namespaceList(ClusterTransferDto clusterTransferDtos,List<ErrorNamespaceDto> namespaceDtos,boolean isSuccess) throws Exception {
		List<TransferBindNamespace> list = new ArrayList<>();
		for (ErrorNamespaceDto namespaceDto : namespaceDtos) {
			TransferBindNamespace transferBindNamespace = new TransferBindNamespace();
			if(isSuccess){
				transferBindNamespace.setStatus(1);
				transferBindNamespace.setNamespaceNum(transferBindNamespaceMapper.queryLastNamespaceNum(clusterTransferDtos.getTenantId(),clusterTransferDtos.getTargetClusterId())+1);
				transferBindNamespace.setCreateNamespace(namespaceDto.getNamespace());
			}else{
				transferBindNamespace.setErrMsg(namespaceDto.getErrMsg());
				transferBindNamespace.setCreateNamespace(namespaceDto.getNamespace());
			}
			list.add(transferBindNamespace);
		}
		return list;
	}

	/**
	 * 创建ingress
	 * @param deploymentTransferDto
	 * @param deployment
	 * @throws IntrospectionException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws MarsRuntimeException 
	 */
	private ErrDeployDto createIngress(DeploymentTransferDto deploymentTransferDto) throws Exception{
		ActionReturnUtil actionReturnUtil = routerService.listExposedRouterWithIngressAndNginx(deploymentTransferDto.getCurrentNameSpace(),deploymentTransferDto.getCurrentDeployName(),deploymentTransferDto.getCurrentProjectId());
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
	 * 计算当前分区下的剩余资源 计算公式 (分区下的总配额-分区下的已用配额)*1024=分区剩余配额
	 * @param deploymentTransferDto
	 * @return
	 */
	private Map<String,Long> getUsageQuoate(DeploymentTransferDto deploymentTransferDto, Cluster transferCluster) throws Exception {
		Map<String,Long> params = new HashMap<>(); 
		ResourceQuotaList quotaList = namespaceService.getResouceQuota(deploymentTransferDto.getNamespace(),transferCluster);
		 if (quotaList == null && quotaList.getItems() == null && quotaList.getItems().size() == 0) {
		     throw new MarsRuntimeException(ErrorCodeMessage.NAMESPACE_MEMORY_TYPE_ERROR);
		 }
		 ResourceQuota resourceQuota = quotaList.getItems().get(0);
		 ResourceQuotaSpec resourceQuotaSpec = resourceQuota.getSpec();
         ResourceQuotaStatus resourceQuotaStatus = resourceQuota.getStatus();
         Map<String, String> hard = (Map<String, String>) resourceQuotaSpec.getHard();
         Map<String, String> used = (Map<String, String>) resourceQuotaStatus.getUsed();
         String hardMemory = getResource(hard.get("memory"));
         String hardCpu = getResource(hard.get("cpu"));
         String usedMemory = getResource(used.get("memory"));
         String usedCpu = getResource(used.get("cpu"));
         params.put("usedCpu",new BigDecimal(hardCpu).subtract(new BigDecimal(usedCpu)).longValue());
         params.put("usedMemory", new BigDecimal(hardMemory).subtract(new BigDecimal(usedMemory)).longValue());
         return params;
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
		if(persistentVolumeClaim.getItems()!=null){
			K8SClientResponse pvcRes = getPVC(deploymentTransferDto.getCurrentDeployName(), deploymentTransferDto.getNamespace(), transferCluster);
			checkK8SClientResponse(pvcRes,errDeployDto,deploymentTransferDto.getCurrentDeployName());
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
			errDeployDto = checkK8SClientResponse(response,errDeployDto,deployName);
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
		ErrDeployDto errDeployDto = null;
		if (response.getStatus() == Constant.HTTP_404){
			errDeployDto = new ErrDeployDto();
			errDeployDto.setDeployName(deploymentTransferDto.getCurrentDeployName());
			errDeployDto.setErrMsg("迁移configMap失败");
			return errDeployDto;
		}

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
			errDeployDto = checkK8SClientResponse(result,errDeployDto,deploymentTransferDto.getCurrentDeployName());
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
	 * @param transferCluster
	 * @return
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws IntrospectionException
	 */
	private ErrDeployDto createDeployment(DeploymentTransferDto deploymentTransferDto,Cluster currentCluster,
										  Cluster transferCluster,ErrDeployDto errDeployDto,String deployName) throws Exception{
		K8SClientResponse response = null;
		Deployment deployment = null;
		response = dpService.doSpecifyDeployment(deploymentTransferDto.getNamespace(),deploymentTransferDto.getCurrentDeployName(), null, null, HTTPMethod.GET, transferCluster);
		deployment = JsonUtil.jsonToPojo(response.getBody(), Deployment.class);
		if(deployment!=null){
			updateDeployment(deployment, deploymentTransferDto,transferCluster);
		}
		response = dpService.doSpecifyDeployment(deploymentTransferDto.getCurrentNameSpace(),deploymentTransferDto.getCurrentDeployName(), null, null, HTTPMethod.GET, currentCluster);
		checkK8SClientResponse(response,errDeployDto,deployName);
		if(!Objects.isNull(errDeployDto)){
			return errDeployDto;
		}
		deployment = JsonUtil.jsonToPojo(response.getBody(), Deployment.class);
		response = dpService.doSpecifyDeployment(deploymentTransferDto.getNamespace(), null, generateHeader(), CollectionUtil.transBean2Map(replaceDeployment(deployment, deploymentTransferDto)), HTTPMethod.POST, transferCluster);
		checkK8SClientResponse(response,errDeployDto,deployName);
		if(!Objects.isNull(errDeployDto)){
			return errDeployDto;
		}
		return errDeployDto;
	}
	
	/**
	 * 迁移服务 两种类型 stafulset(有状态的) deployment(无状态的)
	 * @param deploymentTransferDto
	 * @param currentCluster
	 * @param transferCluster
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws IntrospectionException
	 */
	private ErrDeployDto create(DeploymentTransferDto deploymentTransferDto,Cluster currentCluster,Cluster transferCluster) throws Exception{
		switch (deploymentTransferDto.getCurrentServiceType()) {
			case Constant.STATEFULSET:
				ErrDeployDto errDeployDto = createStatefulSetService(deploymentTransferDto, currentCluster, transferCluster);
				errDeployDto = createStatefulSet(deploymentTransferDto, currentCluster, transferCluster);
				return errDeployDto;
			case Constant.DEPLOYMENT:
				errDeployDto = createStatefulSetService(deploymentTransferDto, currentCluster, transferCluster);
				errDeployDto = createDeployment(deploymentTransferDto, currentCluster, transferCluster,errDeployDto,deploymentTransferDto.getCurrentDeployName());
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
	private ErrDeployDto createStatefulSetService(DeploymentTransferDto deploymentTransferDto,Cluster currentCluster,Cluster transferCluster) throws Exception{
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
		errDeployDto = checkK8SClientResponse(rsRes,errDeployDto,deploymentTransferDto.getCurrentDeployName());
		if(!Objects.isNull(errDeployDto)){
			return errDeployDto;
		}
		k8surl.setNamespace(deploymentTransferDto.getNamespace()).setResource(Resource.SERVICE);
		service = JsonUtil.jsonToPojo(rsRes.getBody(), com.harmonycloud.k8s.bean.Service.class);
		K8SClientResponse sResponse = new K8sMachineClient().exec(k8surl, HTTPMethod.POST, generateHeader(),CollectionUtil.transBean2Map(replaceService(service, deploymentTransferDto)), transferCluster);
		checkK8SClientResponse(rsRes,errDeployDto,deploymentTransferDto.getCurrentDeployName());
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
		errDeployDto = checkK8SClientResponse(statefulSetRes,errDeployDto,deploymentTransferDto.getCurrentDeployName());
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
	 * @param flag
	 */
	private ErrDeployDto checkK8SClientResponse(K8SClientResponse response,ErrDeployDto errDeployDto,String deployName){
		if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
			errDeployDto.setDeployName(deployName);
			errDeployDto.setErrMsg("从k8s获得响应失败");
		}
		return errDeployDto;
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
		deployment.getMetadata().setLabels(replaceLabels(deployment.getMetadata().getLabels(), deploymentTransferDtos));
		deployment.getSpec().getTemplate().getMetadata().setLabels(replaceLabels(deployment.getSpec().getTemplate().getMetadata().getLabels(),deploymentTransferDtos));
		return deployment;
	}
	
	/**
	 * 替换labels的projectId为迁移集群的projectId
	 * @param labels
	 * @param deploymentTransferDto
	 * @return
	 */
	private Map<String,Object> replaceLabels(Map<String,Object> labels,DeploymentTransferDto deploymentTransferDto) {
		for (String label:labels.keySet()){
	    	 if(labels.get(label).equals(deploymentTransferDto.getCurrentProjectId())){
	    	     labels.put(label, deploymentTransferDto.getProjectId());
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
	 * 替换configmap对应属性
	 * @param configMap
	 * @param deploymentTransferDto
	 * @return
	 */
	private ConfigMap replaceConfigMap(ConfigMap configMap,DeploymentTransferDto deploymentTransferDto){
		configMap.getMetadata().setNamespace(deploymentTransferDto.getNamespace());
		configMap.getMetadata().setResourceVersion(null);
		return configMap;
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
		for (ErrorNamespaceDto errorNamespaceDto : errorNamespaceDtos) {
			K8SClientResponse clientResponse = dpService.doDeploymentsByNamespace(param.get(errorNamespaceDto.getNamespace()),null, null, HTTPMethod.GET, oldCluster);
			DeploymentList deploymentList = JsonUtil.jsonToPojo(clientResponse.getBody(), DeploymentList.class);
			if (deploymentList != null && !CollectionUtils.isEmpty(deploymentList.getItems())) {
				for(Deployment deployment:deploymentList.getItems()){
					list.add(generateTransferBindDeploy(deployment, clusterTransferDtos.get(0), param, errorNamespaceDto));
					deploymentTransferDtos.add(generateTransferDto(deployment,  clusterTransferDtos.get(0), param, errorNamespaceDto));
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
		deploymentTransferDto.setCurrentProjectId(String.valueOf(deployment.getMetadata().getLabels().get("projectId")));
		deploymentTransferDto.setCurrentServiceType(Constant.DEPLOYMENT);
		deploymentTransferDto.setCurrentTenantId(clusterTransferDto.getTenantId());
		deploymentTransferDto.setNamespace(errorNamespaceDto.getNamespace());
		deploymentTransferDto.setProjectId(String.valueOf(deployment.getMetadata().getLabels().get("projectId")));
		deploymentTransferDto.setTenantId(clusterTransferDto.getTenantId());
		return deploymentTransferDto;
	}

	private Map<String,Object> createDeployment(List<DeploymentTransferDto> deploymentTransferDtos,Cluster transferCluster,Cluster currentCluster) throws Exception {
		int index = 0;
		Map<String,Object> params = new HashMap<>();
		List<ErrDeployDto> errDeployDtos = new ArrayList<>();
		for (DeploymentTransferDto deploymentTransferDto : deploymentTransferDtos) {
			ErrDeployDto errDeployDto  = null;
			errDeployDto = createIngress(deploymentTransferDto);
			if(errDeployDto!=null){
				errDeployDtos.add(errDeployDto);
				continue;
			}
			createPVC(deploymentTransferDto, currentCluster, transferCluster,errDeployDtos);
			errDeployDto = createOrUpdateConfigMap(deploymentTransferDto, currentCluster, transferCluster);
			if(errDeployDto!=null){
				errDeployDtos.add(errDeployDto);
				continue;
			}
			errDeployDto = create(deploymentTransferDto, currentCluster, transferCluster);
			if(errDeployDto!=null){
				errDeployDtos.add(errDeployDto);
				continue;
			}
			index = deploymentTransferDtos.indexOf(deploymentTransferDto);
		}
		params.put("errDeployDtos",errDeployDtos);
		double transferProgess = index/deploymentTransferDtos.size();
		params.put("progress",transferProgess);
		return params;
	}

	private TransferBindDeploy generateTransferBindDeploy(Deployment deployment,ClusterTransferDto clusterTransferDto,Map<String,String> param,ErrorNamespaceDto errorNamespaceDto) throws Exception {
		TransferBindDeploy transferBindDeploy = new TransferBindDeploy();
		transferBindDeploy.setClusterId(clusterTransferDto.getTargetClusterId());
		transferBindDeploy.setCreateTime(new Date());
		transferBindDeploy.setDeployName(deployment.getMetadata().getName());
		transferBindDeploy.setDeployNum(transferDeployMapper.queryMaxNun(clusterTransferDto.getTenantId(),clusterTransferDto.getTargetClusterId())+1);
		transferBindDeploy.setIsDelete((byte)0);
		transferBindDeploy.setNamespace(errorNamespaceDto.getNamespace());
		transferBindDeploy.setProjectId(String.valueOf(deployment.getMetadata().getLabels().get("projectId")));
		transferBindDeploy.setStatus(0);
		transferBindDeploy.setTanantId(clusterTransferDto.getTenantId());
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
	
	
}
