package com.harmonycloud.api.harbor;

import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.platform.service.harbor.HarborProjectService;
import com.harmonycloud.service.platform.service.harbor.HarborSecurityService;
import com.harmonycloud.service.platform.service.harbor.HarborService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;

/**
 * 镜像相关请求url控制，url路径包含/images
 */
@Api(description = "harbor镜像管理")
@Controller
@RequestMapping("/tenants/{tenantId}/projects/{projectId}/repositories")
public class HarborImageController {

	private static final Logger LOGGER = LoggerFactory.getLogger(HarborImageController.class);

	@Autowired
	private HarborProjectService harborProjectService;
	@Autowired
	private HarborSecurityService harborSecurityService;
	@Autowired
	private ClusterService clusterService;
	@Autowired
	private HarborService harborService;

	/**
	 * 获取仓库下的镜像列表
	 *
	 * @return
	 */
	@ApiOperation(value = "查询镜像列表", notes = "获取某个镜像仓库的镜像列表")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "repositoryId", value = "镜像仓库id", paramType = "path",dataType = "Integer"),
			@ApiImplicitParam(name = "pageSize", value = "分页大小", paramType = "query",dataType = "Integer"),
			@ApiImplicitParam(name = "pageNo", value = "分页页码", paramType = "query",dataType = "Integer")})
	@ResponseBody
	@RequestMapping(value = "/{repositoryId}/images", method = RequestMethod.GET)
	public ActionReturnUtil listImages(@PathVariable(value = "repositoryId") Integer repositoryId,
									   @RequestParam(value = "pageSize", required = false) Integer pageSize,
									   @RequestParam(value = "pageNo", required = false) Integer pageNo)throws Exception {
		return harborProjectService.listImages(repositoryId,  pageSize, pageNo);
	}

	/**
	 * 删除镜像
	 *
	 * @return
	 */
	@ApiOperation(value = "删除镜像", notes = "删除某个镜像")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "repositoryId", value = "镜像仓库id", paramType = "path",dataType = "Integer"),
			@ApiImplicitParam(name = "imageName", value = "镜像名称", paramType = "path",dataType = "String"),
			@ApiImplicitParam(name = "tagName", value = "镜像版本", paramType = "query",dataType = "String")})
	@ResponseBody
	@RequestMapping(value = "/{repositoryId}/images/{imageName:.+}", method = RequestMethod.DELETE)
	public ActionReturnUtil deleteImage(@PathVariable(value = "repositoryId") Integer repositoryId,
										@PathVariable(value="imageName") String imageName,
										@RequestParam(value="tagName", required=false) String tagName) throws Exception{
		String image = URLDecoder.decode(imageName,"UTF-8");
		return harborProjectService.deleteImage(repositoryId, image, tagName);
	}

	/**
	 * 获取镜像
	 *
	 * @return
	 */
	@ApiOperation(value = "查询镜像信息", notes = "查询某个镜像")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "repositoryId", value = "镜像仓库id", paramType = "path",dataType = "Integer"),
			@ApiImplicitParam(name = "imageName", value = "镜像名称", paramType = "path",dataType = "String")})
	@ResponseBody
	@RequestMapping(value = "/{repositoryId}/images/{imageName:.+}", method = RequestMethod.GET)
	public ActionReturnUtil getImage(@PathVariable(value = "repositoryId") Integer repositoryId,
									 @PathVariable(value="imageName") String imageName) throws Exception{
		String image = URLDecoder.decode(imageName,"UTF-8");
		return harborProjectService.getImage(repositoryId, image);
	}

	/**
	 * tag detail
	 *
	 * @return
	 */
	@ApiOperation(value = "查询某个镜像版本的信息", notes = "查询某个镜像版本的信息")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "repositoryId", value = "镜像仓库id", paramType = "path",dataType = "Integer"),
			@ApiImplicitParam(name = "imageName", value = "镜像名称", paramType = "path",dataType = "String"),
			@ApiImplicitParam(name = "tagName", value = "镜像版本", paramType = "path",dataType = "String")})
	@ResponseBody
	@RequestMapping(value = "/{repositoryId}/images/{imageName:.+}/tags/{tagName}", method = RequestMethod.GET)
	public ActionReturnUtil getManifest(@PathVariable(value = "repositoryId") Integer repositoryId,
										@PathVariable(value="imageName") String imageName,
										@PathVariable(value="tagName") String tagName)throws Exception {
		String image = URLDecoder.decode(imageName,"UTF-8");
		return harborProjectService.getManifests(repositoryId, image, tagName);
	}

	@ApiOperation(value = "上传镜像", notes = "上传镜像至harbor")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "repositoryId", value = "镜像仓库id", paramType = "path",dataType = "Integer"),
			@ApiImplicitParam(name = "imageName", value = "镜像名称", paramType = "query",dataType = "String"),
			@ApiImplicitParam(name = "tagName", value = "镜像版本", paramType = "query",dataType = "String")})
	@ResponseBody
	@RequestMapping(value = "/{repositoryId}/images/upload", method = RequestMethod.POST)
	public ActionReturnUtil uploadImage(@PathVariable(value = "repositoryId") Integer repositoryId,
										@RequestParam(value="file") MultipartFile file,
										@RequestParam("imageName") String imageName,
										@RequestParam("tagName") String tagName) throws Exception{
		LOGGER.info("上传镜像，imageName：{},tagName:{}", imageName, tagName);
		if(file.isEmpty()){
			return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.FILE_CONTENT_BLANK);
		}
		if(!file.getOriginalFilename().endsWith(".tar")){
			return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.FILE_TYPE_SUPPORT,"tar",false);
		}
		return harborProjectService.uploadImage(repositoryId, file, imageName, tagName);
	}

	/**
	 * 从harbor pull镜像到云平台运行的主机上，作为准备下载的镜像文件
	 * @param repositoryId
	 * @param imageName
	 * @param tagName
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "pull镜像", notes = "从harbor pull 镜像到平台服务器上")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "repositoryId", value = "镜像仓库id", paramType = "path",dataType = "Integer"),
			@ApiImplicitParam(name = "imageName", value = "镜像名称", paramType = "path",dataType = "String"),
			@ApiImplicitParam(name = "tagName", value = "镜像版本", paramType = "query",dataType = "String")})
	@ResponseBody
	@RequestMapping(value="/{repositoryId}/images/{imageName:.+}/pull",method=RequestMethod.GET)
	public ActionReturnUtil pullImage(@PathVariable(value = "repositoryId") Integer repositoryId,
									  @PathVariable("imageName") String imageName, @RequestParam("tagName") String tagName) throws Exception{
		String image = URLDecoder.decode(imageName,"UTF-8");
		harborProjectService.pullImage(repositoryId, image, tagName);
		return ActionReturnUtil.returnSuccess();
	}

	/**
	 * 下载镜像文件，前提需要将镜像拉取到本地主机上，如果本地主机上没有，返回错误
	 * @param repositoryId
	 * @param imageName
	 * @param tagName
	 * @param response
	 * @throws Exception
	 */
	@ApiOperation(value = "下载镜像", notes = "将已经pull到平台服务器上的镜像通过浏览器下载到本地")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "repositoryId", value = "镜像仓库id", paramType = "path",dataType = "Integer"),
			@ApiImplicitParam(name = "imageName", value = "镜像名称", paramType = "path",dataType = "String"),
			@ApiImplicitParam(name = "tagName", value = "镜像版本", paramType = "query",dataType = "String")})
	@RequestMapping(value="/{repositoryId}/images/{imageName:.+}/download",method=RequestMethod.GET) //匹配的是href中的download请求
	public void downloadImage(@PathVariable(value = "repositoryId") Integer repositoryId,
							  @PathVariable("imageName") String imageName, @RequestParam("tagName") String tagName,
							  HttpServletResponse response) throws Exception{
		String image = URLDecoder.decode(imageName,"UTF-8");
		harborProjectService.downloadImage(repositoryId, image, tagName, response);

	}

	/**
	 * 镜像推送，将一个环境的镜像推送到另一个环境
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "推送镜像", notes = "将镜像同步到另一个集群对应的harbor上")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "repositoryId", value = "镜像仓库id", paramType = "path",dataType = "Integer"),
			@ApiImplicitParam(name = "imageName", value = "镜像名称", paramType = "path",dataType = "String"),
			@ApiImplicitParam(name = "tagName", value = "镜像版本", paramType = "path",dataType = "String"),
			@ApiImplicitParam(name = "clusterId", value = "集群id", paramType = "query",dataType = "String"),
			@ApiImplicitParam(name = "overwrite", value = "是否覆盖镜像", paramType = "query",dataType = "Boolean")})
	@RequestMapping(value = "/{repositoryId}/images/{imageName:.+}/tags/{tagName}/syncImage", method = RequestMethod.POST)
	@ResponseBody
	public ActionReturnUtil syncImage(@PathVariable(value = "repositoryId") Integer repositoryId,
									  @PathVariable(value = "imageName") String imageName,
									  @PathVariable(value = "tagName") String tagName,
									  @RequestParam(value = "clusterId") String clusterId,
									  @RequestParam(value = "overwrite",required = false) Boolean overwrite)throws Exception{
		String image = URLDecoder.decode(imageName,"UTF-8");
		boolean result = harborProjectService.syncImage(repositoryId,image,tagName,clusterId, overwrite);
		if(result) {
			return ActionReturnUtil.returnSuccess();
		}else{
			return ActionReturnUtil.returnError();
		}
	}

	/**
	 * 查询镜像可以推送的环境
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "查询镜像可以推送的集群", notes = "获取镜像推送可以推送的集群列表")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "repositoryId", value = "镜像仓库id", paramType = "path",dataType = "Integer"),
			@ApiImplicitParam(name = "imageName", value = "镜像名称", paramType = "path",dataType = "String"),
			@ApiImplicitParam(name = "tagName", value = "镜像版本", paramType = "path",dataType = "String")})
	@RequestMapping(value = "/{repositoryId}/images/{imageName:.+}/tags/{tagName}/syncclusters", method = RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil listSyncClusters(@PathVariable(value = "repositoryId") Integer repositoryId,
											 @PathVariable(value = "imageName") String imageName,
											 @PathVariable(value = "tagName") String tagName)throws Exception{
		String image = URLDecoder.decode(imageName,"UTF-8");
		return ActionReturnUtil.returnSuccessWithData(harborProjectService.listSyncClusters(repositoryId,image,tagName));
	}

	/**
	 * 查看具体某个版本的镜像安全扫描详情
	 *
	 * @return
	 */
	@ApiOperation(value = "查询某个镜像版本的详情", notes = "查询某个镜像版本的详情")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "repositoryId", value = "镜像仓库id", paramType = "path",dataType = "Integer"),
			@ApiImplicitParam(name = "imageName", value = "镜像名称", paramType = "path",dataType = "String"),
			@ApiImplicitParam(name = "tagName", value = "镜像版本", paramType = "path",dataType = "String")})
	@RequestMapping(value = "/{repositoryId}/images/{imageName:.+}/tags/{tagName}/detail", method = RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil getTagDetail(@PathVariable(value = "repositoryId") Integer repositoryId,
										 @PathVariable(value="imageName") String imageName,
										 @PathVariable(value="tagName") String tagName) throws Exception{
		String image = URLDecoder.decode(imageName,"UTF-8");
		return harborSecurityService.manifestsOfTag(repositoryId, image, tagName);
	}

	/**get repoTagList
	 *
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "查询镜像的版本", notes = "查询某个镜像的版本列表")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "repositoryId", value = "镜像仓库id", paramType = "path",dataType = "Integer"),
			@ApiImplicitParam(name = "imageName", value = "镜像名称", paramType = "path",dataType = "String")})
	@RequestMapping(value = "/{repositoryId}/images/{imageName:.+}/tags", method = RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil getImageTags(@PathVariable(value = "repositoryId") Integer repositoryId,
										 @PathVariable(value="imageName") String imageName)throws Exception{
		String image = URLDecoder.decode(imageName,"UTF-8");
		return harborProjectService.getTagsByImageName(repositoryId, image);

	}

	/**查询满足条件的默认一个镜像
	 *
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "查询满足条件的默认一个镜像", notes = "查询满足条件的默认一个镜像")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "projectId", value = "项目id", paramType = "path",dataType = "String"),
			@ApiImplicitParam(name = "projectName", value = "项目名称", paramType = "query",dataType = "String"),
			@ApiImplicitParam(name = "clusterId", value = "集群id", paramType = "query",dataType = "String"),
			@ApiImplicitParam(name = "imageName", value = "镜像名称", paramType = "query",dataType = "String")})
	@RequestMapping(value = "/images/first", method = RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil getFirstImage(@PathVariable(value="projectId") String projectId,
										  @RequestParam(value="projectName", required = false) String projectName,
										  @RequestParam(value="clusterId", required = false) String clusterId,
										  @RequestParam(value="imageName", required = false) String imageName)throws Exception{
		return harborService.getFirstImage(projectId, clusterId,projectName, imageName);
	}

	/**
	 * 获取项目和集群对应仓库下的镜像名称列表（创建配置文件选择镜像）
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "根据项目和集群获取镜像列表", notes = "获取项目和集群对应仓库下的镜像名称列表")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "projectId", value = "项目id", paramType = "path",dataType = "String"),
			@ApiImplicitParam(name = "clusterId", value = "集群id", paramType = "query",dataType = "String")})
	@RequestMapping(value = "/images/search", method = RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil getImage(@PathVariable(value="projectId") String projectId,
									 @RequestParam(value="clusterId", required = false) String clusterId,
									 @RequestParam(value="appStore", required = false) boolean isAppStore)throws Exception{
		return harborService.getImagesByProjectId(projectId, clusterId, isAppStore);
	}

	/**
	 * 获取项目和集群环境对应的镜像仓库下的镜像
	 *
	 * @return
	 */
	@ApiOperation(value = "分页查询镜像", notes = "分页查询某个项目的镜像")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "projectId", value = "项目id", paramType = "path",dataType = "String"),
			@ApiImplicitParam(name = "clusterId", value = "集群id", paramType = "query",dataType = "String"),
			@ApiImplicitParam(name = "pageSize", value = "分页大小", paramType = "query",dataType = "Integer"),
			@ApiImplicitParam(name = "pageNo", value = "分页页码", paramType = "query",dataType = "Integer"),
			@ApiImplicitParam(name = "isPublic", value = "是否公共镜像", paramType = "query",dataType = "Boolean")})
	@ResponseBody
	@RequestMapping(value = "/images", method = RequestMethod.GET)
	public ActionReturnUtil listImages(@PathVariable(value="projectId") String projectId,
									   @RequestParam(value = "clusterId", required = false) String clusterId,
									   @RequestParam(value = "pageSize", required = false) Integer pageSize,
									   @RequestParam(value = "pageNo", required = false) Integer pageNo,
									   @RequestParam(value = "isPublic", required = false) Boolean isPublic,
									   @RequestParam(value = "appStore", required = false) boolean isAppStore)throws Exception {
		return harborProjectService.listImages(projectId,clusterId, pageSize, pageNo, isPublic, isAppStore);
	}


	/**
	 * 增加镜像标签分组
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "增加镜像标签分组", notes = "增加镜像标签分组")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "harborHost", value = "harbor主机地址", paramType = "query",dataType = "String"),
			@ApiImplicitParam(name = "labelId", value = "标签ID", paramType = "query",dataType = "String"),
			@ApiImplicitParam(name = "tag", value = "镜像版本号", paramType = "query",dataType = "String"),
			@ApiImplicitParam(name = "repoName", value = "镜像仓库名", paramType = "query",dataType = "String")})
	@RequestMapping(value = "/images/label", method = RequestMethod.POST)
	@ResponseBody
	public ActionReturnUtil addImgLabel(@RequestParam(value="harborHost") String harborHost,
										@RequestParam String labelId, @RequestParam String tag,@RequestParam String repoName) throws Exception{

		return harborProjectService.addImgLabel(harborHost, repoName, tag, labelId);
	}
	/**
	 * 移除镜像标签分组
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "移除镜像标签分组", notes = "移除镜像标签分组")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "harborHost", value = "harbor主机地址", paramType = "query",dataType = "String"),
			@ApiImplicitParam(name = "labelId", value = "标签ID", paramType = "query",dataType = "String"),
			@ApiImplicitParam(name = "tag", value = "镜像版本号", paramType = "query",dataType = "String"),
			@ApiImplicitParam(name = "repoName", value = "镜像仓库名", paramType = "query",dataType = "String")})
	@RequestMapping(value = "/images/label", method = RequestMethod.DELETE)
	@ResponseBody
	public ActionReturnUtil delImgLabel(@RequestParam(value="harborHost") String harborHost,
										@RequestParam String labelId, @RequestParam String tag,@RequestParam String repoName) throws Exception{

		return harborProjectService.delImgLabel(harborHost, repoName, tag, labelId);
	}

	/**
	 * 获取镜像标签分组
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "获取镜像标签分组", notes = "获取镜像标签分组")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "harborHost", value = "harbor主机地址", paramType = "query",dataType = "String"),
			@ApiImplicitParam(name = "tag", value = "镜像版本号", paramType = "query",dataType = "String"),
			@ApiImplicitParam(name = "repoName", value = "镜像仓库名", paramType = "query",dataType = "String")})
	@RequestMapping(value = "/images/label", method = RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil getImgLabel(@RequestParam(value="harborHost") String harborHost,
										@RequestParam String tag,@RequestParam String repoName) throws Exception{

		return harborProjectService.getImgLabel(harborHost, repoName, tag);
	}

	/**
	 * 查询镜像版本描述
	 *
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "查询镜像版本描述", notes = "查询某个镜像仓库里某个镜像的某个版本的描述")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "repositoryId", value = "镜像仓库id", paramType = "path",dataType = "Integer"),
			@ApiImplicitParam(name = "imageName", value = "镜像名称", paramType = "path",dataType = "String"),
			@ApiImplicitParam(name = "tagName", value = "镜像版本名称", paramType = "path",dataType = "String")})
	@RequestMapping(value = "/{repositoryId}/images/{imageName:.+}/tags/{tagName}/desc", method = RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil getImageTagDesc(@PathVariable(value = "repositoryId") Integer repositoryId,
											@PathVariable(value = "imageName") String imageName,
											@PathVariable(value = "tagName") String tagName) throws Exception {
		String image = URLDecoder.decode(imageName,"UTF-8");
		return harborProjectService.getImageTagDesc(repositoryId, image, tagName);
	}

	/**
	 * 保存镜像版本描述
	 *
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "保存镜像版本描述", notes = "保存某个镜像仓库里某个镜像的某个版本的描述")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "repositoryId", value = "镜像仓库id", paramType = "path",dataType = "Integer"),
			@ApiImplicitParam(name = "imageName", value = "镜像名称", paramType = "path",dataType = "String"),
			@ApiImplicitParam(name = "tagName", value = "镜像版本名称", paramType = "path",dataType = "String"),
			@ApiImplicitParam(name = "tagDesc", value = "镜像版本描述", paramType = "path",dataType = "String")})
	@RequestMapping(value = "/{repositoryId}/images/{imageName:.+}/tags/{tagName}/desc", method = RequestMethod.POST)
	@ResponseBody
	public ActionReturnUtil saveImageTagDesc(@PathVariable(value = "repositoryId") Integer repositoryId,
											 @PathVariable(value="imageName") String imageName,
											 @PathVariable(value="tagName") String tagName,
											 @RequestParam(value = "tagDesc") String tagDesc) throws Exception {
		String image = URLDecoder.decode(imageName,"UTF-8");
		return harborProjectService.saveImageTagDesc(repositoryId, image, tagName, tagDesc);
	}

	/**
	 * 保存镜像版本描述
	 *
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "保存镜像版本描述", notes = "保存某个镜像仓库里某个镜像的某个版本的描述")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "projectId", value = "租户项目id", paramType = "path",dataType = "Integer"),
			@ApiImplicitParam(name = "imageName", value = "镜像名称", paramType = "path",dataType = "String"),
			@ApiImplicitParam(name = "tagName", value = "镜像版本", paramType = "path",dataType = "String"),
			@ApiImplicitParam(name = "fullImageName", value = "镜像版本", paramType = "path",dataType = "String"),
			@ApiImplicitParam(name = "namespace", value = "分区名称", paramType = "path",dataType = "String"),
			@ApiImplicitParam(name = "clusterId", value = "集群id", paramType = "path",dataType = "String")})
	@RequestMapping(value = "/{repositoryId}/images/{imageName:.+}/tags/{tagName}/deploys", method = RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil deploys(@PathVariable(value = "projectId") String projectId,
									@PathVariable(value="imageName") String imageName,
									@PathVariable(value="tagName") String tagName,
									@RequestParam(value = "fullImageName") String fullImageName,
									@RequestParam(value = "namespace") String namespace,
									@RequestParam(value = "clusterId") String clusterId) throws Exception {
		String image = URLDecoder.decode(imageName,"UTF-8");
		return harborProjectService.getDeploysByImage(projectId, fullImageName, image, tagName, namespace, clusterId);
	}

}
