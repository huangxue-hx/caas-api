package com.harmonycloud.service.platform.serviceImpl.harbor;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.ClusterLevelEnum;
import com.harmonycloud.common.enumm.DataResourceTypeEnum;
import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.AssertUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.dao.ci.bean.Trigger;
import com.harmonycloud.dao.ci.bean.TriggerExample;
import com.harmonycloud.dao.harbor.ImageRepositoryMapper;
import com.harmonycloud.dao.harbor.bean.ImageRepository;
import com.harmonycloud.dao.harbor.bean.ImageTagDesc;
import com.harmonycloud.dao.tenant.bean.Project;
import com.harmonycloud.dao.user.bean.UserRoleRelationship;
import com.harmonycloud.dto.dataprivilege.DataPrivilegeDto;
import com.harmonycloud.k8s.bean.DeploymentList;
import com.harmonycloud.k8s.bean.StatefulSetList;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.bean.cluster.HarborServer;
import com.harmonycloud.k8s.service.StatefulSetService;
import com.harmonycloud.service.application.DeploymentsService;
import com.harmonycloud.service.cache.ImageCacheManager;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.common.DataPrivilegeHelper;
import com.harmonycloud.service.common.HarborHttpsClientUtil;
import com.harmonycloud.service.platform.bean.RepositoryInfo;
import com.harmonycloud.service.platform.bean.harbor.*;
import com.harmonycloud.service.platform.client.HarborClient;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.platform.convert.K8sResultConvert;
import com.harmonycloud.service.platform.service.ci.TriggerService;
import com.harmonycloud.service.platform.service.harbor.*;
import com.harmonycloud.service.tenant.NamespaceLocalService;
import com.harmonycloud.service.tenant.ProjectService;
import com.harmonycloud.service.user.RoleLocalService;
import com.harmonycloud.service.user.UserRoleRelationshipService;
import com.harmonycloud.service.user.UserService;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerCertificates;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.Image;
import com.spotify.docker.client.messages.RegistryAuth;
import com.spotify.docker.client.messages.RemovedImage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URI;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import static com.harmonycloud.common.Constant.CommonConstant.*;

@Service
public class HarborProjectServiceImpl implements HarborProjectService {
	private static ExecutorService executorService = Executors.newFixedThreadPool(2);
	private static final Logger logger = LoggerFactory.getLogger(HarborProjectServiceImpl.class);
	private static LinkedBlockingQueue<HarborLog> imageUpdateQueue = new LinkedBlockingQueue();
	//上传镜像之后，最长查询10分钟，如果超过10分钟还没查到，则认为镜像上传失败
	private static final int PUSH_IMAGE_GET_TRY_MINUTES = 10;
	@Autowired
	private HarborService harborService;
	@Autowired
	private ClusterService clusterService;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private UserService userService;
	@Autowired
	private HarborReplicationService harborReplicationService;
	@Autowired
	private ImageRepositoryMapper imageRepositoryMapper;
	@Autowired
	private ImageCacheManager imageCacheManager;
	@Autowired
	private RoleLocalService roleLocalService;
	@Autowired
	private StringRedisTemplate stringRedisTemplate;
	@Autowired
	private UserRoleRelationshipService roleRelationshipService;
	@Autowired
	private HarborImageCleanService harborImageCleanService;
	@Autowired
	private TriggerService triggerService;
	@Autowired
	private HarborImageTagDescService harborImageTagDescService;
    @Autowired
    private NamespaceLocalService namespaceLocalService;
    @Autowired
    private DeploymentsService deploymentsService;
    @Autowired
    private DataPrivilegeHelper dataPrivilegeHelper;
    @Autowired
    private StatefulSetService statefulSetService;

	@Value("#{propertiesReader['upload.path']}")
	private String uploadPath;
	@Value("#{propertiesReader['docker.host']}")
	private String dockerHost;

	@Value("#{propertiesReader['docker.cert.path']}")
	private String dockerCertPath;

	private static DockerClient docker;

	@Override
	public List<HarborOverview> getHarborProjectOverview(String harborHost, String username) throws Exception {
		List<HarborOverview> harborOverviews = new ArrayList<>();
		Set<HarborServer> harborServers = new HashSet<>();
		if(StringUtils.isBlank(harborHost)) {
			List<Cluster> clusters = roleLocalService.listCurrentUserRoleCluster();
			if (!CollectionUtils.isEmpty(clusters)) {
				for (Cluster cluster : clusters) {
					harborServers.add(cluster.getHarborServer());
				}
			}
		}else{
			harborServers.add(clusterService.findHarborByHost(harborHost));
		}
		for(HarborServer harborServer : harborServers){
			HarborOverview harborOverview = new HarborOverview();
			harborServer.setNormal(HarborClient.checkHarborStatus(harborServer));
			harborOverview.setHarborServer(harborServer);
			harborOverview.setRepositories(this.getRepositories(harborServer.getHarborHost(), username));
			harborOverviews.add(harborOverview);
		}
		return harborOverviews;
	}

	/**
	 * 创建镜像仓库
	 * @param repositoryInfo
	 * @return
	 * @throws Exception
	 */
	@Override
	public ActionReturnUtil createRepository(RepositoryInfo repositoryInfo) throws Exception {
		Assert.notNull(repositoryInfo);
		Assert.notNull(repositoryInfo.getIsPublic());
		if(repositoryInfo.getQuotaSize() == null){
			repositoryInfo.setQuotaSize(QUOTA_SIZE);
		}
		if(repositoryInfo.getIsPublic()){
			ImageRepository imageRepository = new ImageRepository();
			imageRepository.setHarborHost(repositoryInfo.getHarborHost());
			imageRepository.setHarborProjectName(repositoryInfo.getHarborProjectName());
			List<ImageRepository> imageRepositories = this.listRepositories(imageRepository);
			if (!CollectionUtils.isEmpty(imageRepositories)) {
				throw new MarsRuntimeException(ErrorCodeMessage.PUBLIC_REPOSITORY_EXIST);
			}
			return createPublicRepository(repositoryInfo);
		}else{
			return createPrivateRepository(repositoryInfo);
		}
	}

	@Override
	public void insertRepository(ImageRepository imageRepository) throws Exception {
		AssertUtil.notNull(imageRepository);
		AssertUtil.notBlank(imageRepository.getHarborProjectName(),DictEnum.REPOSITORY);
		AssertUtil.notBlank(imageRepository.getHarborHost(),DictEnum.HARBOR_HOST);
		if(imageRepository.getIsPublic() == null){
			imageRepository.setIsPublic(Boolean.FALSE);
		}
		if(imageRepository.getIsNormal() == null){
			imageRepository.setIsNormal(Boolean.TRUE);
		}
		if(imageRepository.getIsDefault() == null){
			imageRepository.setIsDefault(Boolean.FALSE);
		}
		imageRepository.setCreateTime(new Date());
		imageRepositoryMapper.insert(imageRepository);
	}

	private ActionReturnUtil createPrivateRepository(RepositoryInfo repositoryInfo) throws Exception {
		AssertUtil.notBlank(repositoryInfo.getProjectId(),DictEnum.PROJECT_ID);
		AssertUtil.notBlank(repositoryInfo.getProjectName(),DictEnum.PROJECT_NAME);
		List<Cluster> clusters = new ArrayList<>();
		if(StringUtils.isNotBlank(repositoryInfo.getClusterId())){
			Cluster cluster = clusterService.findClusterById(repositoryInfo.getClusterId());
			clusters.add(cluster);
		}else{
			clusters.addAll(clusterService.listCluster(null,null,null));
		}
		String failedCluster = "";
		List<ImageRepository> createdRepository = new ArrayList<>();
		for(Cluster cluster : clusters) {
			Integer harborProjectId = null;
			try {
				String harborProjectName = generateHarborProjectName(
						cluster.getName(), repositoryInfo.getProjectName(), repositoryInfo.getRepositorySuffixName());
				ImageRepository imageRepository = new ImageRepository();
				imageRepository.setProjectId(repositoryInfo.getProjectId());
				imageRepository.setTenantId(repositoryInfo.getTenantId());
				imageRepository.setCreateTime(new Date());
				imageRepository.setIsNormal(Boolean.TRUE);
				imageRepository.setClusterId(cluster.getId());
				imageRepository.setClusterName(cluster.getName());
				imageRepository.setHarborHost(cluster.getHarborServer().getHarborHost());
				imageRepository.setIsDefault(null == repositoryInfo.getIsDefault() ? Boolean.FALSE : repositoryInfo.getIsDefault());
				imageRepository.setIsPublic(null == repositoryInfo.getIsPublic() ? Boolean.FALSE : repositoryInfo.getIsPublic());
				imageRepository.setRepositoryName(harborProjectName);
				imageRepository.setHarborProjectName(harborProjectName);
				//创建harbor project
				HarborProject harborProject = new HarborProject();
				harborProject.setProjectName(harborProjectName);
				harborProject.setIsPublic(imageRepository.isPublic()?FLAG_TRUE:FLAG_FALSE);
				harborProject.setQuotaSize(repositoryInfo.getQuotaSize());
				String harborHost = cluster.getHarborServer().getHarborHost();
				ActionReturnUtil result = harborService.createProject(harborHost, harborProject);
				//harbor创建镜像仓库是否成功
				if (result.isSuccess()) {
					Map<String, Object> map = (Map) (result.get("data"));
					harborProjectId = Integer.valueOf(map.get("harborProjectId").toString());
					imageRepository.setHarborProjectId(harborProjectId);
				}else {
					failedCluster += cluster.getName() + CommonConstant.COMMA;
					logger.error("创建镜像仓库失败，harborHost：{}，harborProjectName：{}",harborHost,harborProjectName);
					imageRepository.setIsNormal(Boolean.FALSE);
				}
				//记录镜像仓库表
				imageRepositoryMapper.insert(imageRepository);
				createdRepository.add(imageRepository);
			} catch (Exception e) {
				logger.error("创建镜像仓库失败,repositoryInfo:{},cluster:{}",
						new String[]{JSONObject.toJSONString(repositoryInfo), cluster.getName()}, e);
				try {
					if (harborProjectId != null) {
						harborService.deleteProject(cluster.getHarborServer().getHarborHost(), harborProjectId);
					}
				} catch (Exception ex) {
					logger.error("删除新建的harbor project失败,harbor:{}",JSONObject.toJSONString(cluster.getHarborServer()), ex);
				}
				failedCluster += cluster.getName() + CommonConstant.COMMA;
			}
		}
		if(StringUtils.isBlank(failedCluster)){
			return ActionReturnUtil.returnSuccessWithData(createdRepository);
		}else{
			return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.REPOSITORY_CREATE_FAIL,
					failedCluster.substring(0,failedCluster.length()-1),false);
		}
	}

	/**
	 * 创建公共镜像仓库
	 * @param repositoryInfo
	 * @return
	 * @throws Exception
	 */
	private ActionReturnUtil createPublicRepository(RepositoryInfo repositoryInfo) throws Exception {
		AssertUtil.notBlank(repositoryInfo.getHarborHost(),DictEnum.HARBOR_HOST);
		HarborServer harborServer = clusterService.findHarborByHost(repositoryInfo.getHarborHost());
		Integer harborProjectId = null;
		ImageRepository imageRepository = new ImageRepository();
		imageRepository.setCreateTime(new Date());
		imageRepository.setIsNormal(Boolean.TRUE);
		imageRepository.setHarborHost(harborServer.getHarborHost());
		imageRepository.setIsDefault(Boolean.FALSE);
		imageRepository.setIsPublic(repositoryInfo.getIsPublic());
		imageRepository.setRepositoryName(repositoryInfo.getHarborProjectName());
		imageRepository.setHarborProjectName(repositoryInfo.getHarborProjectName());
		//创建harbor project
		HarborProject harborProject = new HarborProject();
		harborProject.setProjectName(repositoryInfo.getHarborProjectName());
		harborProject.setIsPublic(imageRepository.isPublic()?FLAG_TRUE:FLAG_FALSE);
		harborProject.setQuotaSize(repositoryInfo.getQuotaSize());
		ActionReturnUtil result = harborService.createProject(harborServer.getHarborHost(), harborProject);
		//harbor创建镜像仓库是否成功
		if (!result.isSuccess() || result.getData() == null) {
			logger.error("创建公共仓库失败，repositoryInfo:{}， message:{}",
					JSONObject.toJSONString(repositoryInfo),result.getData());
			if(result.getData() != null && String.valueOf(result.getData()).contains("Conflict")){
				return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.PUBLIC_REPOSITORY_EXIST);
			}
			if(result.getData() != null && String.valueOf(result.getData()).contains("contains illegal characters")){
				return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.INVALID_CHARACTER);
			}
			return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.CREATE_FAIL);
		}
		try {
			Map<String, Object> map = (Map) (result.get("data"));
			harborProjectId = Integer.valueOf(map.get("harborProjectId").toString());
			imageRepository.setHarborProjectId(harborProjectId);
			//记录镜像仓库表
			imageRepositoryMapper.insert(imageRepository);
			return ActionReturnUtil.returnSuccess();
		}catch (Exception e){
			logger.error("调harbor创建公共仓库成功，保存数据库失败，回滚创建的仓库，harbor:{}， project:{}",
					harborServer.getHarborHost(),repositoryInfo.getHarborProjectName(),e);
			harborService.deleteProject(harborServer.getHarborHost(), harborProjectId);
			return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.CREATE_FAIL);
		}

	}

	/**
	 *  根据集群和项目生成项目对应集群的harbor项目名称
	 * @param repositorySuffixName 用户自定义镜像仓库名称
	 * @return
	 * @throws Exception
	 */
	private String generateHarborProjectName(String clusterName, String projectName, String repositorySuffixName) throws Exception {

		if (StringUtils.isNotBlank(repositorySuffixName)){
			return projectName + CommonConstant.LINE + clusterName + CommonConstant.LINE + repositorySuffixName;
		}
		return projectName + CommonConstant.LINE + clusterName;
	}


	@Override
	public void deleteRepositories(ImageRepository imageRepository) throws Exception {
		imageRepositoryMapper.deleteRepositories(imageRepository);
	}

	@Override
	public List<ImageRepository> listRepositories(ImageRepository imageRepository) throws Exception {
		return imageRepositoryMapper.listRepositories(imageRepository);
	}

	@Override
	public List<ImageRepository> listRepositories(String projectId, String clusterId, Boolean isPublic, Boolean isNormal) throws Exception {
		//查询公共镜像仓库，公共镜像仓库需要根据harborHost过滤
		List<ImageRepository> imageRepositories = new ArrayList();
		if(isPublic != null && isPublic){
			imageRepositories = this.listPublicRepository(clusterId);
		}else if(isPublic != null && !isPublic){
			AssertUtil.notBlank(projectId, DictEnum.PROJECT_ID);
			imageRepositories = this.listPrivateRepository(projectId, clusterId, isNormal);
		}else{
			imageRepositories.addAll(this.listPublicRepository(clusterId));
			if(StringUtils.isNotBlank(projectId)) {
				imageRepositories.addAll(this.listPrivateRepository(projectId, clusterId, isNormal));
			}
		}
		if(CollectionUtils.isEmpty(imageRepositories)){
			return Collections.emptyList();
		}
		//非admin用户，过滤微服务的镜像仓库
		if(userService.getCurrentRoleId() != null && !userService.checkCurrentUserIsAdmin()){
			imageRepositories = imageRepositories.stream()
					.filter(repo -> !repo.getHarborProjectName().equalsIgnoreCase(HARBOR_PROJECT_NAME_MSF)).collect(Collectors.toList());
		}
		return imageRepositories;
	}

	@Override
	public List<ImageRepository> listPublicRepository(String clusterId) throws Exception{
		Set<String> harborHosts = new HashSet<>();
		if(StringUtils.isBlank(clusterId)){
			List<Cluster> clusters = roleLocalService.listCurrentUserRoleCluster();
			for(Cluster cluster : clusters){
				harborHosts.add(clusterService.getHarborHost(cluster.getId()));
			}
		}else{
			harborHosts.add(clusterService.getHarborHost(clusterId));
		}
		List<ImageRepository> imageRepositories = imageRepositoryMapper
				.selectRepositories(null, harborHosts,null,Boolean.TRUE, null);
		if(CollectionUtils.isEmpty(imageRepositories)){
			return Collections.emptyList();
		}
		for(ImageRepository imageRepository : imageRepositories){
			HarborServer harborServer = clusterService.findHarborByHost(imageRepository.getHarborHost());
			imageRepository.setClusterName(harborServer.getReferredClusterNames());
			imageRepository.setClusterAliasName(harborServer.getReferredClusterAliasNames());
			imageRepository.setClusterId(harborServer.getReferredClusterIds());
			imageRepository.setDataCenterName(harborServer.getDataCenterName());
		}
		return imageRepositories;
	}

	public List<ImageRepository> listPrivateRepository(String projectId, String clusterId, Boolean isNormal) throws Exception{
		Set<String> clusterIds = new HashSet<>();
		if(StringUtils.isBlank(clusterId)){
			clusterIds.addAll(roleLocalService.listCurrentUserRoleClusterIds());
		}else{
			clusterIds.add(clusterId);
		}
		if (CollectionUtils.isEmpty(clusterIds)){
			throw new MarsRuntimeException(ErrorCodeMessage.ROLE_HAVE_DISABLE_CLUSTER);
		}
		List<ImageRepository> imageRepositories = imageRepositoryMapper
				.selectRepositories(projectId, null,clusterIds, Boolean.FALSE, isNormal);
		if(imageRepositories == null){
			return Collections.emptyList();
		}
		for(ImageRepository imageRepository : imageRepositories){
			Cluster cluster = clusterService.findClusterById(imageRepository.getClusterId());
			imageRepository.setClusterAliasName(cluster.getAliasName());
			imageRepository.setDataCenterName(cluster.getDataCenterName());
		}
		return imageRepositories;
	}

	@Override
	public List<ImageRepository> listRepositoryDetails(String projectId, String clusterId, Boolean isPublic, Boolean isNormal) throws Exception {
		List<ImageRepository> imageRepositories = this.listRepositories(projectId, clusterId, isPublic, isNormal);
		if(CollectionUtils.isEmpty(imageRepositories)){
			return imageRepositories;
		}
		Iterator<ImageRepository> iterator = imageRepositories.iterator();
		while(iterator.hasNext()){
			ImageRepository repository = iterator.next();
			ActionReturnUtil repoResponse = this.getRepositoryDetail(repository);
			if(repoResponse.getData() != null && ErrorCodeMessage.HARBOR_PROJECT_NOT_FOUND.value() == repoResponse.getErrorCode()){
				this.deleteRepositoryById(repository.getId());
				iterator.remove();
			}
		}
		return imageRepositories;
	}

	/**
	 * 根据条件查询镜像仓库列表
	 *
	 * @param projectId
	 * @param clusterId
	 * @param isPublic
	 * @param isNormal
	 * @return
	 * @throws Exception
	 */
	@Override
	public List<ImageRepository> listRepository(String projectId, String clusterId, Boolean isPublic, Boolean isNormal) throws Exception {
		List<ImageRepository> imageRepositories = this.listRepositories(projectId, clusterId, isPublic, isNormal);
		return imageRepositories;
	}

	/**
	 * 查找某个镜像仓库
	 * @param id
	 * @return
	 */
	@Override
	public ImageRepository findRepositoryById(Integer id){
		ImageRepository imageRepository = imageRepositoryMapper.findRepositoryById(id);
		if(imageRepository == null){
			return null;
		}
		HarborServer harborServer = clusterService.findHarborByHost(imageRepository.getHarborHost());
		if(imageRepository.getIsPublic()){
			imageRepository.setClusterId(harborServer.getReferredClusterIds());
			imageRepository.setClusterName(harborServer.getReferredClusterAliasNames());
		}
		try {
			List<HarborProject> harborProjects = harborService.listProject(imageRepository.getHarborHost(), imageRepository.getHarborProjectName(),null,null);
			if(!CollectionUtils.isEmpty(harborProjects)) {
				List<HarborProject> projects = harborProjects.stream().filter(project -> imageRepository
						.getHarborProjectName().equals(project.getProjectName())).collect(Collectors.toList());
				if (!CollectionUtils.isEmpty(projects)) {
					imageRepository.setImageCount(projects.get(0).getRepoCount());
				}
			}
		}catch (Exception e){
			logger.error("查询镜像仓库下的镜像数量失败，image：{}",JSONObject.toJSONString(imageRepository),e);
		}
		return imageRepository;
	}

	/**
	 * 根据harborProjectId获取repo详情 repo+tag+domain
	 *
	 * @param harborProjectId harborProjectId
	 * @return
	 * @throws Exception
	 */
	@Override
	public ActionReturnUtil listRepoTags(String harborHost, Integer harborProjectId) throws Exception {
		AssertUtil.notNull(harborProjectId, DictEnum.REPOSITORY_ID);
		ActionReturnUtil repoResponse = harborService.repoListById(harborHost, harborProjectId);
		if (!repoResponse.isSuccess()) {
			logger.error("list repo error, harborHost:{},harborProjectId:{},response:{}",
					new String[]{harborHost,harborProjectId.toString(),JSONObject.toJSONString(repoResponse)});
			return repoResponse;
		}
		List<HarborRepository> harborRepositoryList = new ArrayList<>();
		if (repoResponse.get("data") == null) {
			return ActionReturnUtil.returnSuccessWithData(Collections.emptyList());
		}
		List<String> repoNameList = (List<String>)repoResponse.get("data");
		if (!CollectionUtils.isEmpty(repoNameList)) {
			for (String repoName : repoNameList) {
				if (StringUtils.isNotEmpty(repoName)) {
					HarborRepository harborRepository = new HarborRepository();
					ActionReturnUtil tagResponse = harborService.getTagsByRepoName(harborHost, repoName);
					if (tagResponse.isSuccess() && tagResponse.get("data") != null) {
						harborRepository.setTags(getRepoTagList(tagResponse.get("data").toString()));
					}
					harborRepository.setName(repoName);
					harborRepository.setSource(getSource(harborHost,repoName));
					harborRepositoryList.add(harborRepository);
				}
			}
		}
		return ActionReturnUtil.returnSuccessWithData(harborRepositoryList);
	}

	@Override
	public boolean enableRepository(Integer repositoryId) throws Exception{
		ImageRepository imageRepository = imageRepositoryMapper.findRepositoryById(repositoryId);
		if(imageRepository.isNormal()){
			return true;
		}
		HarborProject harborProject = new HarborProject();
		harborProject.setProjectName(imageRepository.getHarborProjectName());
		ActionReturnUtil result = harborService.createProject(imageRepository.getHarborHost(), harborProject);
		//harbor创建镜像仓库是否成功
		if (result.isSuccess()) {
			Map<String, Object> map = (Map) (result.get("data"));
			Integer harborProjectId = Integer.valueOf(map.get("harborProjectId").toString());
			imageRepository.setHarborProjectId(harborProjectId);
			imageRepository.setIsNormal(Boolean.TRUE);
			imageRepositoryMapper.update(imageRepository);
			return true;
		}else {
			logger.error("镜像仓库harbor创建失败，result:{}",JSONObject.toJSONString(result));
			return false;
		}
	}

	/**
	 * 删除某个镜像仓库
	 * @param id
	 * @return
	 */
	@Override
	public boolean deleteRepositoryById(Integer id) throws Exception{
		ImageRepository imageRepository = findRepositoryById(id);
		if(null == imageRepository){
			throw new MarsRuntimeException(ErrorCodeMessage.IMAGE_REPOSITORY_NOT_FOUND);
		}
		if(imageRepository.getIsNormal()) {
			ActionReturnUtil response = harborService.deleteProject(imageRepository.getHarborHost(),
					imageRepository.getHarborProjectId());
			if (!response.isSuccess()) {
				if(response.getData() != null && response.getData().toString().contains("project does not exist")){
					logger.warn("harbor仓库不存在，删除数据库中的记录，imageRepository:{}",JSONObject.toJSONString(imageRepository));
					imageRepositoryMapper.deleteRepositoryById(id);
					return true;
				}
				logger.error("删除镜像仓库失败，repositoryId：{},response:{}",id,JSONObject.toJSONString(response));
				return false;
			}
		}
		imageRepositoryMapper.deleteRepositoryById(id);
		return true;
	}

	@Override
	public ActionReturnUtil deleteRepository(String projectId) throws Exception{
		ImageRepository queryRepository = new ImageRepository();
		queryRepository.setProjectId(projectId);
		queryRepository.setIsPublic(Boolean.FALSE);
		List<ImageRepository> imageRepositories = this.listRepositories(queryRepository);
		if(CollectionUtils.isEmpty(imageRepositories)){
			return ActionReturnUtil.returnSuccess();
		}
		List<String> failedName = new ArrayList<>();
		for(ImageRepository imageRepository : imageRepositories){
			boolean result = this.deleteRepositoryById(imageRepository.getId());
			if(!result){
				failedName.add(imageRepository.getHarborProjectName());
			}
		}
		if(CollectionUtils.isEmpty(failedName)){
			return ActionReturnUtil.returnSuccess();
		}else{
			throw new MarsRuntimeException(ErrorCodeMessage.REPOSITORY_DELETE_FAIL,failedName.toString(),Boolean.TRUE);
		}
	}

	@Override
	public ActionReturnUtil listImages(Integer repositoryId, Integer pageSize, Integer pageNo) throws Exception {
		ImageRepository imageRepository = imageRepositoryMapper.findRepositoryById(repositoryId);
		ActionReturnUtil response = harborService.getRepositoryDetailByProjectId(imageRepository.getHarborHost(),
				imageRepository.getHarborProjectId(), pageSize, pageNo);
		if (!response.isSuccess()) {
			return response;
		}
		this.setImagePullStatus((List<HarborRepositoryMessage>) response.getData());
		return response;
	}

	public void setImagePullStatus(List<HarborRepositoryMessage> harborRepositoryList){
		try {
			//设置镜像是否已经pull状态
			BoundHashOperations<String, String, String> statusHashOps = stringRedisTemplate
					.boundHashOps(REDIS_KEY_IMAGE_PULL_STATUS);
			Set<String> pullingImages = statusHashOps.keys();
			if (CollectionUtils.isEmpty(pullingImages) || CollectionUtils.isEmpty(harborRepositoryList)) {
				return;
			}
			for (HarborRepositoryMessage repositoryMessage : harborRepositoryList) {
				List<HarborManifest> harborManifests = repositoryMessage.getRepositoryDetial();
				for (HarborManifest harborManifest : harborManifests) {
					String imageFullName = repositoryMessage.getFullNameRepo() + CommonConstant.COLON + harborManifest.getTag();
					if(pullingImages.contains(imageFullName)){
						harborManifest.setPullStatus(statusHashOps.get(imageFullName));
					}
				}
			}
		}catch (Exception e){
			logger.error("查询镜像列表设置是否存在本地镜像已经下载标识失败",e);
		}
	}

	@Override
	public ActionReturnUtil listImages(String projectId, String clusterId, Integer pageSize, Integer pageNo, Boolean isPublic, boolean isAppStore) throws Exception {
		List<ImageRepository> imageRepositories;
		if(isAppStore){
			List<ImageRepository> publicRepositories = this.listRepositories(projectId,null,null,Boolean.TRUE);
			Optional<ImageRepository> onlineshopRepository= publicRepositories.stream().filter(imageRepository -> Constant.ONLINESHOP.equals(imageRepository.getHarborProjectName())).findFirst();
			if(onlineshopRepository.isPresent()) {
				imageRepositories = Arrays.asList(onlineshopRepository.get());
			}else{
				imageRepositories = new ArrayList<>();
			}
		}else {
			imageRepositories = this.listRepositories(projectId, clusterId, isPublic, Boolean.TRUE);
		}
		Map<String, List<HarborRepositoryMessage>> imagesMap = new HashMap<>();
		for(ImageRepository repository : imageRepositories){
			ActionReturnUtil response = harborService.getRepositoryDetailByProjectId(repository.getHarborHost(),
					repository.getHarborProjectId(), pageSize, pageNo);
			if(response.isSuccess() && response.getData() != null){
				List<HarborRepositoryMessage> images = null;
				if(repository.getIsPublic()){
					images = imagesMap.get(REPOSITORY_TYPE_PUBLIC);
					if(images == null){
						images = new ArrayList<>();
						imagesMap.put(REPOSITORY_TYPE_PUBLIC, images);
					}
				}else{
					images = imagesMap.get(REPOSITORY_TYPE_PRIVATE);
					if(images == null){
						images = new ArrayList<>();
						imagesMap.put(REPOSITORY_TYPE_PRIVATE, images);
					}
				}
				List<HarborRepositoryMessage> repositoryMessages = (List<HarborRepositoryMessage>)response.getData();
				repositoryMessages.stream().forEach(repo -> repo.setHarborProjectName(repository.getHarborProjectName()));
				images.addAll(repositoryMessages);
			}
		}
		return ActionReturnUtil.returnSuccessWithData(imagesMap);
	}

	@Override
	public ActionReturnUtil deleteImage(Integer repositoryId, String image, String tag) throws Exception {
		ImageRepository imageRepository = imageRepositoryMapper.findRepositoryById(repositoryId);
		return harborService.deleteRepo(imageRepository.getHarborHost(), image,tag);

	}

	@Override
	public ActionReturnUtil getImage(Integer repositoryId, String image) throws Exception {
        return getImage(repositoryId, image, false);
	}

	@Override
	public ActionReturnUtil getImage(Integer repositoryId, String image, boolean needSize) throws Exception {
		ImageRepository imageRepository = imageRepositoryMapper.findRepositoryById(repositoryId);
		HarborRepositoryMessage harborRepositoryMessage = imageCacheManager.freshRepositoryByTags(imageRepository.getHarborHost(), image);
		if(harborRepositoryMessage == null){
			return ActionReturnUtil.returnErrorWithData(DictEnum.IMAGE.phrase(),ErrorCodeMessage.NOT_EXIST);
		}
		List<HarborRepositoryMessage> harborRepositoryMessages = new ArrayList<>();
		harborRepositoryMessages.add(harborRepositoryMessage);
		this.setImagePullStatus(harborRepositoryMessages);
        if (needSize) {
            this.setImageTagSize(harborRepositoryMessage.getRepositoryDetial(), imageRepository.getHarborHost(), image);
        }

        return ActionReturnUtil.returnSuccessWithData(harborRepositoryMessage);
	}

    // 获取每个版本的镜像大小
    private void setImageTagSize(List<HarborManifest> repoList, String harborHost, String repoName){
        if (CollectionUtils.isEmpty(repoList)) {
            return;
        }
        try {
            ActionReturnUtil response = harborService.getTagsByRepoName(harborHost, repoName);
            if (response.isSuccess() && response.getData() != null) {
                Map<String, Long> sizeMap = Maps.newHashMap();
                List<Map<String, Object>> tagList = JsonUtil.JsonToMapList(response.getData().toString());
                if (!CollectionUtils.isEmpty(tagList)) {
                    for (Map<String, Object> tag : tagList) {
                        if (tag.get("name") != null) {
                            sizeMap.put(tag.get("name").toString(), Long.valueOf(tag.get("size").toString()));
                        }
                    }
                }

                repoList.forEach(r -> r.setSize(sizeMap.getOrDefault(r.getTag(), 0L)));
            }
        } catch (Exception e){
            logger.error("获取镜像版本的大小时异常：",e);
        }
    }

	@Override
	public ActionReturnUtil getManifests(Integer repositoryId, String image, String tag) throws Exception {
		ImageRepository imageRepository = imageRepositoryMapper.findRepositoryById(repositoryId);
		return harborService.getManifests(imageRepository.getHarborHost(),image,tag);
	}

	@Override
	public ActionReturnUtil uploadImage(Integer repositoryId, MultipartFile file, String imageName, String tag) throws Exception{
		HarborProject harborProject = this.getRepositoryQuota(repositoryId);
		if(harborProject != null && harborProject.getUseSize() >= harborProject.getQuotaSize()){
			throw new MarsRuntimeException(ErrorCodeMessage.HARBOR_PROJECT_QUOTA_EXCEED);
		}
		ImageRepository repository = imageRepositoryMapper.findRepositoryById(repositoryId);
		if(repository == null){
			throw new MarsRuntimeException(DictEnum.REPOSITORY.phrase(),ErrorCodeMessage.NOT_FOUND);
		}
		if(harborImageCleanService.isHarborInGc(repository.getHarborHost())){
			throw new MarsRuntimeException(ErrorCodeMessage.HARBOR_IN_GARBAGE_CLEAN);
		}
		HarborServer harborServer = clusterService.findHarborByHost(repository.getHarborHost());
		File imageFile = null;
		String imageFullName = harborServer.getHarborAddress() + SLASH + repository.getHarborProjectName()
				+ SLASH + imageName + COLON + tag;
		String filePath = uploadPath + File.separator + IMAGE_FILE_UPLOAD_PATH + File.separator
				+ imageName + File.separator + tag + File.separator;
		File dir = new File(filePath);
		try {
			if (!dir.exists()) {
				dir.mkdirs();
			}
			// 转存文件
			String fileName = file.getOriginalFilename();
			imageFile = new File(filePath + fileName);
			if(imageFile.exists()){
				return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.IMAGE_UPLOADING,imageFullName ,true);
			}
			file.transferTo(imageFile);
			executorService.submit(new DockerPushTask(getDockerClient(),imageFullName, imageFile,this.dockerAuth(harborServer),imageUpdateQueue));
		} catch (Exception e) {
			logger.error("上传镜像失败",e);
			return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.UPLOAD_FAIL);
		}
		return ActionReturnUtil.returnSuccess();
	}

	@Override
	public void pullImage(Integer repositoryId, String imageName, String tag) throws Exception{
		AssertUtil.notNull(repositoryId, DictEnum.REPOSITORY_ID);
		AssertUtil.notBlank(imageName, DictEnum.IMAGE_NAME);
		AssertUtil.notBlank(tag, DictEnum.IMAGE_TAG);
		ImageRepository repository = imageRepositoryMapper.findRepositoryById(repositoryId);
		HarborServer harborServer = clusterService.findHarborByHost(repository.getHarborHost());
		String imageFullName = harborServer.getHarborAddress() + SLASH + imageName + CommonConstant.COLON + tag;
		String fileName = this.getTarFileName(imageFullName);
		//如果镜像已经在拉取中，不能重复提交拉取git reset --hard
		BoundHashOperations<String, String, String> statusHashOps = stringRedisTemplate
				.boundHashOps(REDIS_KEY_IMAGE_PULL_STATUS);
		if(statusHashOps.get(imageFullName) != null){
			throw new MarsRuntimeException(ErrorCodeMessage.IMAGE_IN_PULLING);
		}
		DockerClient dockerClient = getDockerClient();
		statusHashOps.put(imageFullName, IMAGE_PULLING_STATUS_PULLING);
		executorService.submit(new DockerPullTask(dockerClient,imageFullName,
				this.dockerAuth(harborServer),fileName, stringRedisTemplate));
	}

	@Override
	public boolean isLocalImageExist(String imageFullName) throws Exception{
		List<Image> images = getDockerClient().listImages(DockerClient.ListImagesParam.byName(imageFullName));
		if(CollectionUtils.isEmpty(images) || CollectionUtils.isEmpty(images.get(0).repoTags())) {
			return false;
		}
		return true;
	}

	@Override
	public void downloadImage(Integer repositoryId, String imageName, String tag, HttpServletResponse response) throws Exception{
		String imageFullName = this.getImageFullName(repositoryId,imageName,tag);
		String fileName = imageName.substring(imageName.lastIndexOf("/")+1) + CommonConstant.LINE + tag + ".tar";
		String tarFileName = this.getTarFileName(imageFullName);
		BoundHashOperations<String, String, String> statusHashOps = stringRedisTemplate
				.boundHashOps(REDIS_KEY_IMAGE_PULL_STATUS);
		InputStream inputStream = null;
		OutputStream outputStream = null;
		try {
			File file = new File(tarFileName);
			inputStream = new FileInputStream(file);
			response.setContentType("multipart/form-data");
			response.setHeader("Content-Disposition", "attachment;fileName=" + fileName);
			outputStream = response.getOutputStream();
			//循环写入输出流
			byte[] b = new byte[2048];
			int length;
			while ((length = inputStream.read(b)) > 0) {
				outputStream.write(b, 0, length);
			}
			outputStream.flush();
		}catch (FileNotFoundException e){
			logger.error("下载镜像失败，tar 文件不存在：{}",imageName, e);
			statusHashOps.delete(REDIS_KEY_IMAGE_PULL_STATUS,imageFullName);
			throw new MarsRuntimeException(ErrorCodeMessage.FILE_NOT_EXIST_FAIL);
		}catch (Exception e){
			logger.error("下载镜像失败，imageName：{}",imageName, e);
			throw new MarsRuntimeException(ErrorCodeMessage.DOWNLOAD_FAIL);
		}finally {
			if(inputStream != null){
				inputStream.close();
			}
			if(outputStream != null){
				outputStream.close();
			}
		}

	}

	@Override
	public boolean syncImage(Integer repositoryId, String repoName, String tag, String destClusterId, Boolean overwrite) throws Exception {
		ImageRepository sourceRepository = imageRepositoryMapper.findRepositoryById(repositoryId);
		if(sourceRepository == null){
			return false;
		}
		ImageRepository queryDestRepository = new ImageRepository();
		queryDestRepository.setProjectId(sourceRepository.getProjectId());
		queryDestRepository.setClusterId(destClusterId);
		queryDestRepository.setIsDefault(Boolean.TRUE);
		queryDestRepository.setIsNormal(Boolean.TRUE);
		List<ImageRepository> destRepositories = imageRepositoryMapper.listRepositories(queryDestRepository);
		if(CollectionUtils.isEmpty(destRepositories)){
			logger.error("镜像推送未找到目标环境的仓库,repository:{}",JSONObject.toJSONString(queryDestRepository));
			throw new MarsRuntimeException(ErrorCodeMessage.IMAGE_SYNC_NOT_FOUND);
		}
		ImageRepository destRepository = destRepositories.get(0);
		String destHarborHost = clusterService.getHarborHost(destClusterId);
		if(harborImageCleanService.isHarborInGc(destHarborHost)){
			throw new MarsRuntimeException(ErrorCodeMessage.HARBOR_IN_GARBAGE_CLEAN);
		}
		String destRepoName = repoName.replace(sourceRepository.getHarborProjectName(),
				destRepository.getHarborProjectName());
		if(overwrite == null || overwrite == false) {
			//判断目标镜像是否已经存在,如果已经存在报错
			ActionReturnUtil response = harborService.getManifests(destHarborHost, destRepoName, tag);
			if(response.isSuccess() && response.getData() != null){
				throw new MarsRuntimeException(ErrorCodeMessage.IMAGE_SYNC_DEST_EXIST);
			}
		}
		//同harbor之间镜像同步使用harbor的copyImage接口
		if(sourceRepository.getHarborHost().equals(destHarborHost)){
			HarborImageCopy imageCopy = new HarborImageCopy(destHarborHost, repoName, tag, destRepoName, tag);
			ActionReturnUtil response = harborReplicationService.copyImage(imageCopy);
			if(!response.isSuccess()){
				logger.error("镜像推送失败,response:{}", JSONObject.toJSONString(response));
				return false;
			}
			imageUpdateQueue.put(new HarborLog(destHarborHost, destRepoName, tag));
		}else{
			HarborServer sourceHarborServer = clusterService.findHarborByHost(sourceRepository.getHarborHost());
			HarborServer destHarborServer = clusterService.findHarborByHost(destRepository.getHarborHost());
			String sourceImageName = sourceHarborServer.getHarborAddress() + SLASH + repoName + COLON + tag;
			String destImageName = destHarborServer.getHarborAddress() + SLASH + destRepoName + COLON + tag;
			//登录源harbor服务器
			HarborClient.checkHarborAdminCookie(sourceHarborServer);
			//登录目标harbor服务器
			HarborClient.checkHarborAdminCookie(destHarborServer);
			executorService.submit(new DockerImageSyncTask(getDockerClient(),sourceImageName, destImageName,
					this.dockerAuth(sourceHarborServer), this.dockerAuth(destHarborServer),imageUpdateQueue));
		}
		return true;
	}

	@Override
	public Set<Cluster> listSyncClusters(Integer repositoryId, String repoName, String tag) throws Exception {
		ImageRepository repository = imageRepositoryMapper.findRepositoryById(repositoryId);
		if(repository.getIsPublic()){
			throw new MarsRuntimeException(ErrorCodeMessage.IMAGE_PUBLIC_SYNC_DENIED);
		}
		Cluster cluster = clusterService.findClusterById(repository.getClusterId());
		if(ClusterLevelEnum.PRD.getLevel() == cluster.getLevel()){
			throw new MarsRuntimeException(ErrorCodeMessage.IMAGE_PRD_SYNC_DENIED);
		}
		List<Cluster> clusters = clusterService.listCluster();
		Map<Integer,List<Cluster>> clusterMap = clusters.stream().collect(Collectors.groupingBy(Cluster::getLevel));
		Set<Cluster> syncCluster = new HashSet<>();
		//查找下一级别的集群
		for(int i=cluster.getLevel()+1;i<=ClusterLevelEnum.PRD.getLevel();i++){
			if(clusterMap.get(i) != null){
				syncCluster.addAll(clusterMap.get(i));
				break;
			}
		}
		//用户授权的集群
		Integer roleId = userService.getCurrentRoleId();
		List<Cluster> roleClusters = roleLocalService.getClusterListByRoleId(roleId);
		for(Cluster roleCluster : roleClusters){
        	if(!repository.getClusterId().equals(roleCluster.getId()) && roleCluster.getLevel()>cluster.getLevel()) {
				syncCluster.add(roleCluster);
			}
		}
		return syncCluster;
	}

	@Override
	public ActionReturnUtil updateRepositoryQuota(Integer repositoryId, HarborProjectQuota harborProjectQuota) throws Exception {
		ImageRepository repository = imageRepositoryMapper.findRepositoryById(repositoryId);
		harborProjectQuota.setProject_id(repository.getHarborProjectId());
		harborProjectQuota.setProject_name(repository.getHarborProjectName());
		ActionReturnUtil response = harborService.updateProjectQuota(repository.getHarborHost(),harborProjectQuota);
		if(!response.isSuccess()){
			return response;
		}
		return response;
	}

	@Override
	public HarborProject getRepositoryQuota(Integer repositoryId) throws Exception {
		ImageRepository repository = imageRepositoryMapper.findRepositoryById(repositoryId);
		return harborService.getProjectQuota(repository.getHarborHost(), repository.getHarborProjectName());
	}

	@Override
	public ActionReturnUtil getTagsByImageName(Integer repositoryId, String imageName) throws Exception {
		ImageRepository repository = imageRepositoryMapper.findRepositoryById(repositoryId);
		return harborService.getTagsByRepoName(repository.getHarborHost(), imageName);
	}

	@Override
	public ActionReturnUtil getRepositoryDetail(Integer repositoryId) throws Exception {
		ImageRepository repository = imageRepositoryMapper.findRepositoryById(repositoryId);
		return harborService.getRepositoryDetailByProjectId(repository.getHarborHost(),
				repository.getHarborProjectId(), null, null);
	}

	@Override
	public ActionReturnUtil getRepositorySummary(Integer repositoryId) throws Exception {
		ImageRepository repository = imageRepositoryMapper.findRepositoryById(repositoryId);
		return harborService.getRepositorySummary(repository.getHarborHost(),
				repository.getHarborProjectName());
	}

	@Override
	public void addClusterHarborProject(String clusterId) throws Exception {
		String clusterName = clusterService.findClusterById(clusterId).getName();
		List<Project> projects = projectService.listAllProject();
		if(CollectionUtils.isEmpty(projects)){
			return;
		}
		ImageRepository queryRepository = new ImageRepository();
		queryRepository.setClusterId(clusterId);
		queryRepository.setIsPublic(Boolean.FALSE);
		List<ImageRepository> imageRepositories = this.listRepositories(queryRepository);
		List<String> harborProjectNames = new ArrayList<>();
		if(!CollectionUtils.isEmpty(imageRepositories)){
			harborProjectNames.addAll(imageRepositories.stream().map(ImageRepository::getHarborProjectName).collect(Collectors.toList()));
		}
		for(Project project: projects){
			//已经有该环境和项目对应的镜像仓库，跳过
			if(harborProjectNames.contains(project.getProjectName()+ LINE + clusterName)){
				continue;
			}
			RepositoryInfo repositoryInfo = new RepositoryInfo();
			repositoryInfo.setIsPublic(Boolean.FALSE);
			repositoryInfo.setIsDefault(Boolean.TRUE);
			repositoryInfo.setTenantId(project.getTenantId());
			repositoryInfo.setProjectId(project.getProjectId());
			repositoryInfo.setProjectName(project.getProjectName());
			repositoryInfo.setClusterId(clusterId);
			this.createRepository(repositoryInfo);
		}

	}

	/**
	 * 添加集群将harbor上的公共镜像仓库导入本地数据库，相同仓库名称的只导入一个，并过滤云平台系统和应用商店使用的镜像仓库
	 * @throws Exception
	 */
	public void getPublicHarborProject(String harborHost) throws Exception{
		ImageRepository queryRepository = new ImageRepository();
		queryRepository.setIsNormal(Boolean.TRUE);
		queryRepository.setIsPublic(Boolean.TRUE);
		List<ImageRepository> imageRepositories = this.listRepositories(queryRepository);
		Set<HarborServer> harborServers = new HashSet<>();
		if(StringUtils.isNotBlank(harborHost)){
			harborServers.add(clusterService.findHarborByHost(harborHost));
		}else {
			harborServers.addAll(clusterService.listAllHarbors());
		}
		//当前所有可用的harbor服务器地址
		Set<String> availableHarborHost = new HashSet<>();
		//检查harbor中的公共镜像仓库，将公共镜像仓库记录添加到数据库
		for(HarborServer harborServer : harborServers){
			availableHarborHost.add(harborServer.getHarborHost());
			//如果已经有该harbor的公共镜像仓库,则略过
			List<ImageRepository> publicRepositories = imageRepositories.stream().filter(repo ->
					repo.getHarborHost().equalsIgnoreCase(harborServer.getHarborHost())).collect(Collectors.toList());
			if(!CollectionUtils.isEmpty(publicRepositories)){
				continue;
			}
			List<HarborProject> harborProjects= harborService.listProject(harborServer.getHarborHost(),null,null,null);
			for(HarborProject harborProject: harborProjects) {
				//公共镜像仓库过滤容器云平台使用的镜像仓库以及同名的镜像仓库名称, 即平台使用多个harbor，如果各个harbor的projectname相同，则只使用某个harbor的镜像仓库
				if(harborProject.getIsPublic() == null || harborProject.getIsPublic() == FLAG_FALSE
						|| HARBOR_PROJECT_NAME_PLATFORM.equalsIgnoreCase(harborProject.getProjectName())
						|| HARBOR_PROJECT_ISTIO_DEPLOY.equalsIgnoreCase(harborProject.getProjectName())){
					continue;
				}
				ImageRepository repository = new ImageRepository();
				repository.setHarborHost(harborServer.getHarborHost());
				repository.setHarborProjectName(harborProject.getProjectName());
				repository.setHarborProjectId(harborProject.getProjectId());
				repository.setRepositoryName(harborProject.getProjectName());
				repository.setIsPublic(Boolean.TRUE);
				this.insertRepository(repository);
				logger.info("添加公共镜像仓库:{}", JSONObject.toJSONString(repository));
			}
		}
	}

	/**
	 * 如果平台已经移除该harbor服务器，则将该harbor的公共镜像仓库删除
	 * @throws Exception
	 */
	public void deletePublicHarborProject(String harborHost) throws Exception{
		Set<String> harborHosts = new HashSet<>();
		if(StringUtils.isBlank(harborHost)){
			ImageRepository queryRepository = new ImageRepository();
			queryRepository.setIsPublic(Boolean.TRUE);
			List<ImageRepository> imageRepositories = this.listRepositories(queryRepository);
			harborHosts.addAll(imageRepositories.stream().map(ImageRepository::getHarborHost).collect(Collectors.toSet()));
		}else{
			harborHosts.add(harborHost);
		}
		Set<HarborServer> harborServers = clusterService.listAllHarbors();
		Set<String> availableHarborHosts = harborServers.stream().map(HarborServer::getHarborHost).collect(Collectors.toSet());
		for(String host : harborHosts){
			//harborHost在如果当前可用的harbor服务器列表，则返回，不对公共镜像仓库删除
			if(availableHarborHosts.contains(host)){
				continue;
			}
			ImageRepository repository = new ImageRepository();
			repository.setHarborHost(host);
			repository.setIsPublic(Boolean.TRUE);
			this.deleteRepositories(repository);
			logger.info("删除公共镜像仓库,harborHost:{}",harborHost);
		}

	}

	@Override
	public boolean removeImageFile(Integer repositoryId, String imageName, String tag) throws Exception{
		String imageFullName = this.getImageFullName(repositoryId, imageName, tag);
		String fileName = this.getTarFileName(imageFullName);
		BoundHashOperations<String, String, String> statusHashOps = stringRedisTemplate
				.boundHashOps(REDIS_KEY_IMAGE_PULL_STATUS);
		if(statusHashOps.get(imageFullName) != null){
			if(statusHashOps.get(imageFullName).equals(IMAGE_PULLING_STATUS_PULLED)) {
				statusHashOps.delete(REDIS_KEY_IMAGE_PULL_STATUS, imageFullName);
				File file = new File(fileName);
				if(file.exists()){
					file.delete();
				}
			}else{
				throw new MarsRuntimeException(ErrorCodeMessage.IMAGE_IN_PULLING_DELETE_ERROR);
			}
		}
		return true;
	}

	@Override
	public boolean removeImage(String imageFullName) throws Exception{
		DockerClient docker = this.getDockerClient();
		List<RemovedImage> removedImages = docker.removeImage(imageFullName);
		if(removedImages.size() == 1){
			return true;
		}
		return false;
	}

	@Override
	public int deleteByClusterId(String clusterId) throws Exception{
		ImageRepository imageRepository = new ImageRepository();
		imageRepository.setClusterId(clusterId);
		List<ImageRepository> imageRepositories = imageRepositoryMapper.listRepositories(imageRepository);
		for(ImageRepository repository : imageRepositories){
			//harbor没有创建成功的镜像仓库 不需要删除
			if(repository.getHarborProjectId() == null){
				continue;
			}
			harborService.deleteProject(repository.getHarborHost(), repository.getHarborProjectId());
		}
		//删除公共镜像仓库
		this.deletePublicHarborProject(clusterService.findClusterById(clusterId).getHarborServer().getHarborHost());
		//删除镜像清理规则
		harborImageCleanService.deleteClusterCleanRule(clusterId);
		return imageRepositoryMapper.deleteByClusterId(clusterId);
	}
	/**
	 * 镜像选择标签分组
	 *
	 * @param harborHost
	 * @param repoName
	 * @param tag
	 * @return
	 * @throws Exception
	 */
	@Override
	public ActionReturnUtil addImgLabel(String harborHost, String repoName, String tag, String  labelId) throws Exception {
		HarborServer harborServer = clusterService.findHarborByHost(harborHost);
		String url = HarborClient.getHarborUrl(harborServer) + "/api/repositories/"+repoName+"/tags/"+tag+"/labels";
		Map<String, Object> headers = HarborClient.getAdminCookieHeader(harborServer);
		headers.put("Content-type", "application/json");
		String json = JsonUtil.convertToJson(getLabelDetail(harborHost,labelId));
		ActionReturnUtil response = HarborHttpsClientUtil.httpPostRequestForHarbor(url, headers, JsonUtil.jsonToMap(json));
		if(!response.isSuccess()){
			logger.error("select label error. harborHost:{},res:{}", harborHost, JSONObject.toJSONString(response));
			throw new MarsRuntimeException(response.getData().toString());
		}
		return response;
	}

	/**
	 * 移除镜像选择标签分组
	 *
	 * @param harborHost
	 * @param repoName
	 * @param tag
	 * @param labelId
	 * @return
	 * @throws Exception
	 */
	@Override
	public ActionReturnUtil delImgLabel(String harborHost, String repoName, String tag, String labelId) throws Exception {
		HarborServer harborServer = clusterService.findHarborByHost(harborHost);
		String url = HarborClient.getHarborUrl(harborServer) + "/api/repositories/"+repoName+"/tags/"+tag+"/labels"+"/"+labelId;
		Map<String, Object> headers = HarborClient.getAdminCookieHeader(harborServer);
		ActionReturnUtil response = HarborHttpsClientUtil.httpDoDelete(url, null, headers);
		if(!response.isSuccess()){
			logger.error("delete img label error. harborHost:{},res:{}", harborHost, JSONObject.toJSONString(response));
			throw new MarsRuntimeException(response.getData().toString());
		}
		return response;
	}

	/**
	 * 获取镜像标签分组
	 *
	 * @param harborHost
	 * @param repoName
	 * @param tag
	 * @return
	 * @throws Exception
	 */
	@Override
	public ActionReturnUtil getImgLabel(String harborHost, String repoName, String tag) throws Exception {
		HarborServer harborServer = clusterService.findHarborByHost(harborHost);
		String url = HarborClient.getHarborUrl(harborServer) + "/api/repositories/"+repoName+"/tags/"+tag+"/labels";
		Map<String, Object> headers = HarborClient.getAdminCookieHeader(harborServer);
		Map<String, Object> params = new HashMap<>();
//		params.put("repo_name",repoName);
//		params.put("tag",tag);
		ActionReturnUtil response = HarborHttpsClientUtil.httpGetRequest(url, headers, params);
		if(!response.isSuccess()){
			logger.error("get img label error. harborHost:{},res:{}", harborHost, JSONObject.toJSONString(response));
			throw new MarsRuntimeException(response.getData().toString());
		}
		return ActionReturnUtil.returnSuccessWithData(JsonUtil.jsonToList(response.getData().toString(),HarborProjectLabel.class));
	}

	@Override
	public ActionReturnUtil getLabel(String harborHost,String repoName, String scope, Long projectId, String labelName) throws Exception{
		AssertUtil.notNull(projectId,DictEnum.PROJECT_ID);
		HarborServer harborServer = clusterService.findHarborByHost(harborHost);
		String url = HarborClient.getHarborUrl(harborServer) + "/api/labels";
		Map<String, Object> headers = HarborClient.getAdminCookieHeader(harborServer);
		Map<String, Object> params = new HashMap<>();
		params.put("scope", scope);
		params.put("project_id",projectId);
		ActionReturnUtil response = HarborHttpsClientUtil.httpGetRequest(url, headers, params);
		if(!response.isSuccess()){
			logger.error("sync registry error. harborHost:{},res:{}", harborHost, JSONObject.toJSONString(response));
			return response;
		}

		List<HarborProjectLabel> labelList = JsonUtil.jsonToList(response.getData().toString(), HarborProjectLabel.class);
		if (!CollectionUtils.isEmpty(labelList) && StringUtils.isNotBlank(labelName)) {    // 有数据并且labelName不为空，则进行模糊匹配过滤数据
			labelList.removeIf(label -> !label.getName().contains(labelName));
		}

		return ActionReturnUtil.returnSuccessWithData(labelList);
	}
	public HarborProjectLabel getLabelDetail(String harborHost,String labelId) throws Exception{
		HarborServer harborServer = clusterService.findHarborByHost(harborHost);
		String url = HarborClient.getHarborUrl(harborServer) + "/api/labels/"+labelId;
		Map<String, Object> headers = HarborClient.getAdminCookieHeader(harborServer);
		ActionReturnUtil response = HarborHttpsClientUtil.httpGetRequest(url, headers, null);
		if(!response.isSuccess()){
			logger.error("sync registry error. harborHost:{},res:{}", harborHost, JSONObject.toJSONString(response));
		}
		return JsonUtil.jsonToPojo(response.getData().toString(),HarborProjectLabel.class);
	}
	@Override
	public ActionReturnUtil deleteLabel(String harborHost, Long labelId) throws Exception {
		AssertUtil.notNull(labelId);
		HarborServer harborServer = clusterService.findHarborByHost(harborHost);
		String url = HarborClient.getHarborUrl(harborServer) + "/api/labels/"+labelId;
		Map<String, Object> headers = HarborClient.getAdminCookieHeader(harborServer);
		ActionReturnUtil response = HarborHttpsClientUtil.httpDoDelete(url,null, headers);
		if(!response.isSuccess()){
			logger.error("sync registry error. harborHost:{},res:{}", harborHost, JSONObject.toJSONString(response));
			return response;
		}
		return response;
	}

	@Override
	public ActionReturnUtil addLabel(String harborHost, String name, String desc, String color, String scope, Integer projectId) throws Exception {
		HarborServer harborServer = clusterService.findHarborByHost(harborHost);
		String url = HarborClient.getHarborUrl(harborServer) + "/api/labels";
		Map<String, Object> headers = HarborClient.getAdminCookieHeader(harborServer);
		headers.put("Content-type", "application/json");
		Map<String, Object> params = new HashMap<>();
		params.put("name",name);
		params.put("description",desc);
		params.put("color",color);
		params.put("scope","p");
		params.put("project_id",projectId);
		ActionReturnUtil response = HarborHttpsClientUtil.httpPostRequestForHarbor(url, headers, params);
		if(!response.isSuccess()){
			logger.error("sync registry error. harborHost:{},res:{}", harborHost, JSONObject.toJSONString(response));
			return response;
		}
		return response;
	}

	@Override
	public ActionReturnUtil updateLabel(String harborHost, Long labelId, HarborProjectLabel harborProjectLabel) throws Exception {
		HarborServer harborServer = clusterService.findHarborByHost(harborHost);
		String url = HarborClient.getHarborUrl(harborServer) + "/api/labels/"+labelId;
		Map<String, Object> headers = HarborClient.getAdminCookieHeader(harborServer);
		headers.put("Content-type", "application/json");
		Map<String, Object> params = new HashMap<>();
		params.put("id",harborProjectLabel.getId());
		params.put("name", harborProjectLabel.getName());
		params.put("description", harborProjectLabel.getDescription());
		params.put("color", harborProjectLabel.getColor());
		params.put("scope", harborProjectLabel.getScope());
		params.put("project_id", harborProjectLabel.getProject_id());
		params.put("creation_time" , harborProjectLabel.getCreation_time());
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		params.put("update_time", df.format(System.currentTimeMillis()));
		params.put("deleted",harborProjectLabel.isDeleted());
		ActionReturnUtil response = HarborHttpsClientUtil.httpPutRequestForHarbor(url, headers, params);
		if(!response.isSuccess()){
			logger.error("sync registry error. harborHost:{},res:{}", harborHost, JSONObject.toJSONString(response));
			return response;
		}
		return response;
	}

	@Override
	public void syncLocalHarborLog() throws Exception {
		Map<String,Object> pams = new HashMap<>();
		pams.put("begin_timestamp",(System.currentTimeMillis()-70000)/1000);//70秒
		pams.put("operation","push");
		//获取分配的cd，仓库,镜像，
		TriggerExample triggerExample = new TriggerExample();
		triggerExample.createCriteria().andTriggerImageIsNotNull().andTypeEqualTo(5);
		List<Trigger> triggers = triggerService.findImageTirrger();
		for (Trigger erigger:triggers) {
			String[] str =  erigger.getTriggerImage().split("/");
			String harborHost = str[0];
			String repo = str[1];
			String image = str[2];

			//查询/api/logs接口
			pams.put("repository",repo+"/"+image);
			HarborServer harborServer = clusterService.findHarborByHost(harborHost);
			ActionReturnUtil response=HarborHttpsClientUtil.httpGetRequest(harborServer.getHarborProtocol()+"://"+harborServer.getHarborAddress()+"/api/logs",HarborClient.getAdminCookieHeader(harborServer),pams);
			//如果是401 则登陆后在查询
			if ( !response.isSuccess() && response.getErrorCode() == 401){
				//登录目标harbor服务器
				HarborClient.loginWithAdmin(harborServer);
				//登陆目标harbor后查询
				response = HarborHttpsClientUtil.httpGetRequest(harborServer.getHarborProtocol()+"://"+harborServer.getHarborAddress()+"/api/logs",HarborClient.getAdminCookieHeader(harborServer),pams);
			}
			JSONArray array = JSONArray.parseArray(response.getData().toString());
			if (array.size() != 0){
				String str_op = array.getJSONObject(0).get("log_id").toString();
				JSONObject obj = array.getJSONObject(0);
				//取最新的日志
				for (int i = 1; i < array.size(); i++) {
					if (Integer.valueOf(str_op)<Integer.valueOf(array.getJSONObject(i).get("log_id").toString())){
						str_op=array.getJSONObject(i).get("log_id").toString();
						obj=array.getJSONObject(i);
					}
				}
				//判断redis中的logid 并及时更新
				String a = stringRedisTemplate.opsForValue().get(harborHost+"logid");
				if(StringUtils.isNotBlank(a) && Integer.valueOf(a)>=Integer.valueOf(str_op)){
					return;
				}
				stringRedisTemplate.opsForValue().set(harborHost+"logid",obj.get("log_id").toString());
				//更新镜像触发流水线构建
				triggerService.triggerJobByImage(harborServer.getHarborAddress()+SLASH+repo+"/"+image,obj.get("repo_tag").toString());
			}
		}
	}

	private void pushImage(HarborServer harborServer, File imageFile, String imageFullName) throws Exception{
		DockerClient docker = this.getDockerClient();
		Set<String> loadedImages = docker.load(new FileInputStream(imageFile));
		if(loadedImages.size() == 0){
			throw new MarsRuntimeException(ErrorCodeMessage.IMAGE_LOAD_ERROR);
		}
		if(loadedImages.size() > 1){
			throw new MarsRuntimeException(ErrorCodeMessage.IMAGE_UPLOAD_SINGLE);
		}
		String tarFileImage = "";
		for (String loadedImage : loadedImages) {
			tarFileImage = loadedImage;
		}
		docker.tag(tarFileImage, imageFullName);
		docker.push(imageFullName,this.dockerAuth(harborServer));
		logger.info("{}镜像上传完成" ,imageFullName);
		List<RemovedImage> removedImages = docker.removeImage(tarFileImage);
		removedImages.addAll(docker.removeImage(imageFullName));
		removedImages.stream().forEach( image-> logger.info("删除镜像：{}" ,image.imageId()));
	}

	private ActionReturnUtil getRepositoryDetail(ImageRepository repository) throws Exception{
		//获取镜像仓库下的镜像数量
		ActionReturnUtil response= harborService.repoListById(repository.getHarborHost(), repository.getHarborProjectId());
		if(response.isSuccess()){
			List<String> list =(List<String>)response.get("data");
			repository.setImageCount(list.size());
		}else{
			return response;
		}
		HarborProject harborProject =  harborService.getProjectQuota(repository.getHarborHost(), repository.getHarborProjectName());
		if(harborProject != null){
			repository.setQuotaSize(harborProject.getQuotaSize());
			repository.setUsageSize(harborProject.getUseSize());
			repository.setUsageRate(harborProject.getUseRate());
		}
		return ActionReturnUtil.returnSuccess();
	}

	private List<ImageRepository> getRepositories(String harborHost, String userName) throws Exception{
		boolean filter = false;
		//是否需要根据用户角色进行过滤数据，用户名不为空，且非系统管理员需要过滤权限
		if(StringUtils.isNotBlank(userName) && !userService.isAdmin(userName)){
			filter = true;
		}
		//用户角色为租户管理员的租户列表
		List<String> tmTenantIds = new ArrayList<>();
		if(filter){
			List<UserRoleRelationship> userRoleRelationships = roleRelationshipService.getUserRoleRelationshipList(userName, NUM_ROLE_TM);
			if(!CollectionUtils.isEmpty(userRoleRelationships)){
				tmTenantIds = userRoleRelationships.stream().map(UserRoleRelationship::getTenantId).collect(Collectors.toList());
			}
		}
		List<ImageRepository> imageRepositories = new ArrayList<>();
		ImageRepository queryRepository = new ImageRepository();
		queryRepository.setHarborHost(harborHost);
		queryRepository.setIsNormal(Boolean.TRUE);
		List<ImageRepository> dbRepositories = this.listRepositories(queryRepository);
		if(CollectionUtils.isEmpty(dbRepositories)){
			return Collections.emptyList();
		}
		List<HarborProject> harborProjects= harborService.listProject(harborHost,null,null,null);
		for(HarborProject harborProject : harborProjects){
			HarborProject tempHarborProject = harborService.getProjectQuota(harborHost,harborProject.getProjectName());
			if(tempHarborProject != null){
				harborProject.setQuotaSize(tempHarborProject.getQuotaSize());
				harborProject.setUseSize(tempHarborProject.getUseSize());
				harborProject.setUseRate(tempHarborProject.getUseRate());
			}
		}
		if(CollectionUtils.isEmpty(harborProjects)){
			return Collections.emptyList();
		}
		Map<Integer,HarborProject> projectsMap = harborProjects.stream()
				.collect(Collectors.toMap(HarborProject::getProjectId, project -> project));
		for(ImageRepository imageRepository : dbRepositories){
			if(imageRepository.getHarborProjectId() == null){
				continue;
			}
			//对私有仓库，租户管理员只能查看角色为租户管理员的租户列表下的仓库
			if(filter && !imageRepository.getIsPublic()
					&& !tmTenantIds.contains(imageRepository.getTenantId())){
				continue;
			}
			HarborProject harborProject = projectsMap.get(imageRepository.getHarborProjectId());
			if(harborProject == null){
				logger.error("harbor上未找到仓库，imageRepository:{}", JSONObject.toJSONString(imageRepository));
				continue;
				/*ActionReturnUtil response = harborService.deleteProject(imageRepository.getHarborHost(),
						imageRepository.getHarborProjectId());
				if (!response.isSuccess() && response.getData() != null
						&& response.getData().toString().contains("project does not exist")) {
					logger.warn("harbor上未找到仓库，删除数据库记录，imageRepository:{}", JSONObject.toJSONString(imageRepository));
					imageRepositoryMapper.deleteRepositoryById(imageRepository.getId());
					continue;
				}*/
			}else {
				//不使用harbor的数据库，使用外部数据库获取的时间要早8小时，与使用harbor的mysql不一致
				//imageRepository.setCreateTime(DateUtil.utcToGmtDate(harborProject.getCreateTime()));
				imageRepository.setImageCount(harborProject.getRepoCount());
				imageRepository.setQuotaSize(harborProject.getQuotaSize());
				imageRepository.setUsageSize(harborProject.getUseSize());
				imageRepository.setUsageRate(harborProject.getUseRate());
			}
			imageRepositories.add(imageRepository);
		}
		return imageRepositories;
	}

	/**
	 * 获取docker client
	 * @return
	 * @throws Exception
	 */
	public DockerClient getDockerClient() throws Exception {
		if(docker != null){
			return docker;
		}
		String osName = (String)System.getProperties().get("os.name");
		if(osName.toLowerCase().contains("windows")){
			docker = DefaultDockerClient.builder()
					.uri(URI.create(dockerHost))
					.dockerCertificates(new DockerCertificates(Paths.get(dockerCertPath)))
					.build();
		}else {
			File dockerFile = new File("/var/run/docker.sock");
			if(dockerFile.exists()) {
				docker = new DefaultDockerClient("unix:///var/run/docker.sock");
			}else if(StringUtils.isNotBlank(dockerHost)){
				docker = DefaultDockerClient.builder()
						.uri(URI.create(dockerHost))
						.dockerCertificates(new DockerCertificates(Paths.get(dockerCertPath)))
						.build();
			}else{
				throw new MarsRuntimeException(ErrorCodeMessage.DOCKER_CONNECT_TYPE_NOT_SET);
			}
		}
		return docker;
	}

	private RegistryAuth dockerAuth(HarborServer harborServer) throws Exception{
		return RegistryAuth.builder()
				.serverAddress(harborServer.getHarborUrl())
				.username(harborServer.getHarborAdminAccount())
				.password(harborServer.getHarborAdminPassword())
				.build();
	}

	/**
	 * 得到harbor repository tag list
	 *
	 * @param dataJson json格式返回的data
	 * @return
	 */
	private List<HarborRepositoryTags> getRepoTagList(String dataJson) throws Exception{
		if (StringUtils.isNotEmpty(dataJson)) {
			List<Map<String, Object>> mapList = JsonUtil.JsonToMapList(dataJson);
			if (!CollectionUtils.isEmpty(mapList)) {
				List<HarborRepositoryTags> harborRepositoryTagsList = new ArrayList<>();
				for (Map<String, Object> map : mapList) {
					HarborRepositoryTags harborRepositoryTags = new HarborRepositoryTags();
					if (map.get("tag") != null) {
						harborRepositoryTags.setTag(map.get("tag").toString());
					}
					if (map.get("high_num") != null) {
						harborRepositoryTags.setHigh_num(Integer.parseInt(map.get("high_num").toString()));
					}
					if (map.get("other_num") != null) {
						harborRepositoryTags.setOther_num(Integer.parseInt(map.get("other_num").toString()));
					}
					harborRepositoryTagsList.add(harborRepositoryTags);
				}
				return harborRepositoryTagsList;
			}
		}
		return Collections.emptyList();
	}

	/**
	 * 得到repo source
	 *
	 * @param repoName repo name
	 * @return
	 */
	private String getSource(String harborHost, String repoName) {
		return harborHost + SLASH + repoName;
	}

	private String getImageFullName(Integer repositoryId, String imageName, String tag){
		AssertUtil.notNull(repositoryId, DictEnum.REPOSITORY_ID);
		AssertUtil.notBlank(imageName, DictEnum.IMAGE_NAME);
		AssertUtil.notBlank(tag, DictEnum.IMAGE_TAG);
		ImageRepository repository = imageRepositoryMapper.findRepositoryById(repositoryId);
		HarborServer harborServer = clusterService.findHarborByHost(repository.getHarborHost());
		String imageFullName = harborServer.getHarborAddress() + SLASH + imageName + CommonConstant.COLON + tag;
		return imageFullName;
	}

	private String getTarFileName(String imageFullName){

		String filePath = uploadPath + File.separator + IMAGE_FILE_DOWNLOAD_PATH + File.separator
				+ imageFullName.replace(COLON,File.separator);
		File dir = new File(filePath);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		String fileName = imageFullName.substring(imageFullName.lastIndexOf("/")+1);
		fileName = fileName.replace(COLON,LINE) + ".tar";
		return filePath +  File.separator + fileName;
	}

	/**
	 * 每隔3秒检查镜像是否上传成功，上传成功则更新缓存
	 */
	@Scheduled(fixedRate = 3000)
	public void updateTagCache() {
		List<HarborLog> keepCheckLog = new ArrayList<>();
		while(imageUpdateQueue.size()>0){
			try {
				HarborLog harborLog = imageUpdateQueue.take();
				ActionReturnUtil res = harborService.getManifestsWithVulnerabilitySum(harborLog.getHarborHost(),
						harborLog.getRepoName(), harborLog.getRepoTag());
				if(res.isSuccess() && res.getData() != null){
					logger.info("检查镜像上传结果: 已上传，更新缓存,镜像信息:{}", JSONObject.toJSONString(harborLog));
					HarborManifest harborManifest = (HarborManifest)res.getData();
					//如果镜像扫描结果是异常，可能扫描结果未更新，再检查一次
					if(harborManifest.getAbnormal() && DateUtil.addSecond(harborLog.getOperationTime(),NUM_FIVE).after(new Date())){
						keepCheckLog.add(harborLog);
						continue;
					}
					imageCacheManager.addRepoTag(harborLog.getHarborHost(),harborLog.getRepoName(),harborManifest);
					HarborServer harborServer = clusterService.findHarborByHost(harborLog.getHarborHost());
//					//更新镜像触发流水线构建
//					triggerService.triggerJobByImage(harborServer.getHarborAddress()+SLASH+harborLog.getRepoName(),harborLog.getRepoTag());
					continue;
				}
				//上传之后检查10分钟，超过10分钟不再继续
				if(DateUtil.addMinute(harborLog.getOperationTime(),PUSH_IMAGE_GET_TRY_MINUTES).after(new Date())){
					keepCheckLog.add(harborLog);
				}else{
					logger.info("检查镜像上传结果: 未上传，已超过10分钟，放弃,镜像信息:{}", JSONObject.toJSONString(harborLog));
				}
			}catch (Exception e){
				logger.error("检查镜像上传结果失败，",e);
			}
		}
		if(keepCheckLog.size() == 0){
			return;
		}
		//继续检查上传结果，重新放入queue
		try {
			for (HarborLog harborLog : keepCheckLog) {
				imageUpdateQueue.put(harborLog);
			}
		}catch (Exception e){
			logger.error("继续跟踪镜像上传结果失败，",e);
		}
	}


	@Override
	public ActionReturnUtil getImageTagDesc(Integer repositoryId, String imageName, String tag) {
		return ActionReturnUtil.returnSuccessWithData(harborImageTagDescService.select(repositoryId, imageName, tag));
	}


	@Override
	public ActionReturnUtil saveImageTagDesc(Integer repositoryId, String imageName, String tag, String tagDesc) throws Exception {
		ImageTagDesc desc = harborImageTagDescService.select(repositoryId, imageName, tag);

		if (desc == null) {    // 新增
			desc = new ImageTagDesc();
			desc.setRepositoryId(repositoryId);
			desc.setImageName(imageName);
			desc.setTagName(tag);
			desc.setTagDesc(tagDesc);
			harborImageTagDescService.create(desc);
		} else {    // 修改
			Integer id = desc.getId();
			desc = new ImageTagDesc();
			desc.setId(id);
			desc.setTagDesc(tagDesc);
			harborImageTagDescService.update(desc);
		}

		return ActionReturnUtil.returnSuccess();
	}

    @Override
    public ActionReturnUtil getDeploysByImage(String projectId, String fullImageName, String imageName, String tag,
											  String namespace, String clusterId, String deployName) throws Exception {
	    // 参数为空判断
	    if (StringUtils.isBlank(projectId) || StringUtils.isBlank(fullImageName) || StringUtils.isBlank(imageName)
                || StringUtils.isBlank(tag) || StringUtils.isBlank(namespace)) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }

        // labels
        Map<String, Object> bodys = Maps.newHashMap();
        String labelSelector = Constant.NODESELECTOR_LABELS_PRE + Constant.LABEL_PROJECT_ID + "=" + projectId;
        bodys.put("labelSelector", labelSelector);

        List<Map<String, Object>> result = Lists.newArrayList();
        List<Cluster> clusterList = roleLocalService.listCurrentUserRoleCluster();
		String[] nsArr = namespace.split(",");
		for (String ns : nsArr) {
			if (StringUtils.isNotBlank(ns)) {
				try {
					Cluster cluster = namespaceLocalService.getClusterByNamespaceName(ns);
					//判断该namespace是否有权限
					if (clusterList.stream().noneMatch(c -> c.getId().equals(cluster.getId()))) {
						continue;
					}
					if (StringUtils.isNotBlank(clusterId) && !cluster.getId().equals(clusterId)) {
						continue;
					}

					// deployment
					DeploymentList deployment = deploymentsService.getDeployments(ns, bodys, cluster);
					String aliasNamespace = namespaceLocalService.getNamespaceByName(ns).getAliasName();
					if (deployment != null  && !CollectionUtils.isEmpty(deployment.getItems())) {
						result.addAll(K8sResultConvert.convertAppList(deployment, cluster, aliasNamespace));
					}
					// statefulset
					StatefulSetList statefulSet = statefulSetService.listStatefulSets(ns, null, bodys, cluster);
					if (statefulSet != null && !CollectionUtils.isEmpty(statefulSet.getItems())) {
						result.addAll(K8sResultConvert.convertAppList(statefulSet, cluster, aliasNamespace));
					}
				} catch (Exception e) {
					logger.error("查询deployment或statefulset列表失败，namespace：{}", ns, e);
				}
			}
		}

		// 有服务，再进行进一步过滤
		if (!CollectionUtils.isEmpty(result)) {
			// 权限过滤使用
			DataPrivilegeDto dataPrivilegeDto = new DataPrivilegeDto();
			dataPrivilegeDto.setProjectId(projectId);
			// 镜像名称
			String fullImageTag = fullImageName + COLON + tag;
			String imageTag = imageName + COLON + tag;

			result.removeIf(res -> {
				// 模糊查询过滤数据
				if (StringUtils.isNotBlank(deployName) && !res.get("name").toString().contains(deployName)) {
					return true;
				}
				// 过滤镜像版本不一样的服务
				@SuppressWarnings("unchecked") List<String> img = (List<String>) res.get("img");
				if (img.stream().noneMatch(i -> StringUtils.equals(i, fullImageTag) || StringUtils.equals(i, imageTag))) {
					return true;
				}
				// 权限过滤
				dataPrivilegeDto.setData((String) res.get(CommonConstant.NAME));
				dataPrivilegeDto.setDataResourceType(DataResourceTypeEnum.SERVICE.getCode());
				dataPrivilegeDto.setNamespace((String) res.get(CommonConstant.DATA_NAMESPACE));
				Map filteredMap = null;
				try {
					filteredMap = dataPrivilegeHelper.filterMap(res, dataPrivilegeDto);
				} catch (Exception e) {
					logger.warn("镜像详情服务列表权限过滤异常：", e);
				}
				return filteredMap == null;
			});
		}

        return ActionReturnUtil.returnSuccessWithData(result);
    }


}