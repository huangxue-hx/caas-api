package com.harmonycloud.api.harbor;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.platform.service.harbor.HarborProjectService;
import com.harmonycloud.service.platform.service.harbor.HarborSecurityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.service.platform.service.harbor.HarborService;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;

/**
 * 镜像相关请求url控制，url路径包含/images
 */
@Controller
@RequestMapping("/tenants/{tenantId}/projects/{projectId}/repositories")
public class HarborImageController {

	private static final Logger LOGGER = LoggerFactory.getLogger(HarborImageController.class);

	@Autowired
	private HarborProjectService harborProjectService;
	@Autowired
	private HarborSecurityService harborSecurityService;
	@Autowired
	ClusterService clusterService;
	@Autowired
	private HarborService harborService;

	/**
	 * 获取仓库下的镜像列表
	 *
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/{repositoryId}/images", method = RequestMethod.GET)
	public ActionReturnUtil listImages(@PathVariable(value = "repositoryId") Integer repositoryId)throws Exception {
		return harborProjectService.listImages(repositoryId);
	}

	/**
	 * 删除镜像
	 *
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/{repositoryId}/images/{imageName}", method = RequestMethod.DELETE)
	public ActionReturnUtil deleteImage(@PathVariable(value = "repositoryId") Integer repositoryId,
										@PathVariable(value="imageName") String imageName,
										@RequestParam(value="tagName", required=false) String tagName) throws Exception{
		String image = URLDecoder.decode(imageName,"UTF-8");
		return harborProjectService.deleteImage(repositoryId, image, tagName);
	}
	/**
	 * tag detail
	 *
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/{repositoryId}/images/{imageName}/tags/{tagName}", method = RequestMethod.GET)
	public ActionReturnUtil getManifest(@PathVariable(value = "repositoryId") Integer repositoryId,
										@PathVariable(value="imageName") String imageName,
										@PathVariable(value="tagName") String tagName)throws Exception {
		String image = URLDecoder.decode(imageName,"UTF-8");
		return harborProjectService.getManifests(repositoryId, image, tagName);
	}

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
	@ResponseBody
	@RequestMapping(value="/{repositoryId}/images/{imageName}/pull",method=RequestMethod.GET)
	public ActionReturnUtil pullImage(@PathVariable(value = "repositoryId") Integer repositoryId,
							  @PathVariable("imageName") String imageName, @RequestParam("tagName") String tagName) throws Exception{
		String image = URLDecoder.decode(imageName,"UTF-8");
		harborProjectService.pullImage(repositoryId, image, tagName);
		return ActionReturnUtil.returnSuccess();
	}

	/**
	 * 查询云平台服务主机上的镜像
	 * @param repositoryId
	 * @param imageName
	 * @param tagName
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value="/{repositoryId}/images/{imageName}/removelocal",method=RequestMethod.GET)
	public ActionReturnUtil removeLocalImage(@PathVariable(value = "repositoryId") Integer repositoryId,
												 @PathVariable("imageName") String imageName, @RequestParam("tagName") String tagName) throws Exception{
		String image = URLDecoder.decode(imageName,"UTF-8");
		return ActionReturnUtil.returnSuccessWithData(harborProjectService.removeImageFile(repositoryId, image, tagName));
	}

	/**
	 * 下载镜像文件，前提需要将镜像拉取到本地主机上，如果本地主机上没有，返回错误
	 * @param repositoryId
	 * @param imageName
	 * @param tagName
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping(value="/{repositoryId}/images/{imageName}/download",method=RequestMethod.GET) //匹配的是href中的download请求
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
	@RequestMapping(value = "/{repositoryId}/images/{imageName}/tags/{tagName}/syncImage", method = RequestMethod.POST)
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
	@RequestMapping(value = "/{repositoryId}/images/{imageName}/tags/{tagName}/syncclusters", method = RequestMethod.GET)
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
	@RequestMapping(value = "/{repositoryId}/images/{imageName}/tags/{tagName}/detail", method = RequestMethod.GET)
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
	@RequestMapping(value = "/{repositoryId}/images/{imageName}/tags", method = RequestMethod.GET)
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
	@RequestMapping(value = "/images/first", method = RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil getFirstImage(@PathVariable(value="projectId") String projectId,
										  @RequestParam(value="projectName", required = false) String projectName,
										  @RequestParam(value="clusterId", required = false) String clusterId,
										  @RequestParam(value="imageName", required = false) String imageName)throws Exception{
		return harborService.getFirstImage(projectId, clusterId,projectName, imageName);
	}

	/**查询满足条件的默认一个镜像
	 *
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/images/search", method = RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil getImage(@PathVariable(value="projectId") String projectId,
									 @RequestParam(value="clusterId", required = false) String clusterId)throws Exception{
		return harborService.getImagesByProjectId(projectId, clusterId);
	}

	/**
	 * 获取项目和集群环境对应的镜像仓库下的镜像
	 *
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/images", method = RequestMethod.GET)
	public ActionReturnUtil listImages(@PathVariable(value="projectId") String projectId,
										 @RequestParam(value = "clusterId") String clusterId)throws Exception {
		return harborProjectService.listImages(projectId,clusterId);
	}
}
