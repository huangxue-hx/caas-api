package com.harmonycloud.service.platform.service.harbor;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.harbor.bean.ImageRepository;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.service.platform.bean.harbor.HarborOverview;
import com.harmonycloud.service.platform.bean.harbor.HarborProject;
import com.harmonycloud.service.platform.bean.harbor.HarborProjectQuota;
import com.harmonycloud.service.platform.bean.RepositoryInfo;
import com.harmonycloud.service.platform.bean.harbor.HarborRepositoryMessage;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Set;

public interface HarborProjectService {

	/**
	 * 获取某个harbor的镜像仓库管理信息，ui调用需要根据角色过滤
	 * @param harborHost
	 * @param username 需要根据用户角色过滤可以查看的镜像仓库
	 * @return
	 * @throws Exception
	 */
	List<HarborOverview> getHarborProjectOverview(String harborHost, String username) throws Exception;

	/**
	 * 创建镜像仓库
	 * @param repositoryInfo
	 * @return
	 * @throws Exception
	 */
	ActionReturnUtil createRepository(RepositoryInfo repositoryInfo) throws Exception;

	void insertRepository(ImageRepository imageRepository) throws Exception;

	void deleteRepositories(ImageRepository imageRepository) throws Exception;

    /**
	 * 根据对象条件查询镜像仓库，属性值设置就作为一个过滤条件
	 * @param imageRepository 查询条件
	 * @return
	 * @throws Exception
	 */
	List<ImageRepository> listRepositories(ImageRepository imageRepository) throws Exception;

	/**
	 * 查询公共镜像仓库列表
	 * @param clusterId
	 * @return
	 * @throws Exception
	 */
	List<ImageRepository> listPublicRepository(String clusterId) throws Exception;

	/**
	 * 查询某个项目的私有镜像仓库列表
	 * @param projectId
	 * @param clusterId
	 * @return
	 * @throws Exception
	 */
	List<ImageRepository> listPrivateRepository(String projectId, String clusterId, Boolean isNormal) throws Exception;

	/**
	 * 根据条件查询镜像仓库列表,不包括镜像数量、磁盘配额和使用量详细信息
	 * @param projectId
	 * @param clusterId 如果为空则查询用户当前角色授权的集群对应的镜像仓库
	 * @param isPublic
	 * @return
	 * @throws Exception
	 */
	List<ImageRepository> listRepositories(String projectId, String clusterId, Boolean isPublic, Boolean isNormal) throws Exception;

	/**
	 * 根据条件查询镜像仓库列表,包括镜像数量、磁盘配额和使用量详细信息
	 * @param projectId
	 * @param clusterId
	 * @param isPublic
	 * @return
	 * @throws Exception
	 */
	List<ImageRepository> listRepositoryDetails(String projectId, String clusterId, Boolean isPublic, Boolean isNormal) throws Exception;
	/**
	 * 根据条件查询镜像仓库列表
	 * @param projectId
	 * @param clusterId
	 * @param isPublic
	 * @return
	 * @throws Exception
	 */
	List<ImageRepository> listRepository(String projectId, String clusterId, Boolean isPublic, Boolean isNormal) throws Exception;
	/**
	 * 查找某个镜像仓库
	 * @param id
	 * @return
	 */
	public ImageRepository findRepositoryById(Integer id);

	ActionReturnUtil getRepositoryDetail(Integer repositoryId) throws Exception;

	ActionReturnUtil getRepositorySummary(Integer repositoryId) throws Exception;

	void addClusterHarborProject(String clusterId) throws Exception ;

	void getPublicHarborProject(String harborHost) throws Exception;

	void deletePublicHarborProject(String harborHost) throws Exception;

	/**
	 * 根据repositoryId获取repo详情 repo+tag+domain
	 *
	 * @param repositoryId repositoryId
	 * @return
	 * @throws Exception
	 */
	ActionReturnUtil listRepoTags(String harborHost,Integer repositoryId) throws Exception;

	boolean enableRepository(Integer repositoryId) throws Exception;

	/**
	 * 删除某个镜像仓库
	 * @param id
	 * @return
	 */
	boolean deleteRepositoryById(Integer id) throws Exception;

	ActionReturnUtil deleteRepository(String projectId) throws Exception;

	void setImagePullStatus(List<HarborRepositoryMessage> harborRepositoryList);

	ActionReturnUtil listImages(Integer repositoryId, Integer pageSize, Integer pageNo) throws Exception ;

	/**
	 * 查询项目和环境对应的镜像仓库下的所有镜像
	 * @param projectId
	 * @param clusterId
	 * @return
	 * @throws Exception
	 */
	ActionReturnUtil listImages(String projectId, String clusterId, Integer pageSize, Integer pageNo) throws Exception ;

	ActionReturnUtil deleteImage(Integer repositoryId, String image, String tag) throws Exception ;

	ActionReturnUtil getImage(Integer repositoryId, String image) throws Exception ;

	ActionReturnUtil getManifests(Integer repositoryId, String repoName, String tag) throws Exception;

	ActionReturnUtil uploadImage(Integer repositoryId, MultipartFile file, String imageName, String tag) throws Exception;

	/**
	 * 删除已经在下载或已经下载的文件
	 * @param repositoryId
	 * @param imageName
	 * @param tag
	 * @return
	 * @throws Exception
	 */
	boolean removeImageFile(Integer repositoryId, String imageName, String tag) throws Exception;

	/**
	 * 删除本地镜像
	 * @param imageFullName
	 * @return
	 * @throws Exception
	 */
	boolean removeImage(String imageFullName) throws Exception;

	/**
	 * 从harbor拉取镜像到本地主机
	 * @param repositoryId
	 * @param imageName
	 * @param tag
	 * @throws Exception
	 */
	void pullImage(Integer repositoryId, String imageName, String tag) throws Exception;

	/**
	 * 判断镜像是否已经pull到本地，可提供下载
	 * @return
	 * @throws Exception
	 */
	boolean isLocalImageExist(String imageFullName) throws Exception;

	/**
	 * 下载镜像，下载之后删除本地的镜像文件
	 * @param repositoryId
	 * @param imageName
	 * @return
	 * @throws Exception
	 */
	void downloadImage(Integer repositoryId, String imageName, String tag, HttpServletResponse response) throws Exception;

	/**
	 * 镜像推送同步，将一个环境的镜像推送到另一个环境的对应仓库下
	 * @param repositoryId
	 * @param repoName
	 * @param tag
	 * @param overwrite 如果目标镜像已经存在，是否覆盖
	 * @throws Exception
	 */
	boolean syncImage(Integer repositoryId, String repoName, String tag, String destClusterId, Boolean overwrite) throws Exception;

	/**
	 * 镜像推送同步，获取镜像可以推送的集群
	 * @param repositoryId
	 * @param repoName
	 * @param tag
	 * @throws Exception
	 */
	Set<Cluster> listSyncClusters(Integer repositoryId, String repoName, String tag) throws Exception;

	ActionReturnUtil updateRepositoryQuota(Integer repositoryId, HarborProjectQuota harborProjectQuota) throws Exception;

	HarborProject getRepositoryQuota(Integer repositoryId) throws Exception;

	ActionReturnUtil getTagsByImageName(Integer repositoryId, String imageName) throws Exception;

	int deleteByClusterId(String clusterId) throws Exception;

}
