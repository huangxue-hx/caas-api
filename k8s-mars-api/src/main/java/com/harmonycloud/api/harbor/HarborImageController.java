package com.harmonycloud.api.harbor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.service.platform.integrationService.HarborIntegrationService;
import com.harmonycloud.service.platform.service.harbor.HarborMemberService;
import com.harmonycloud.service.platform.service.harbor.HarborService;
import com.harmonycloud.service.user.RoleService;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;

@Controller
//@RequestMapping(value = "")
public class HarborImageController {

	private static final Logger LOGGER = LoggerFactory.getLogger(HarborImageController.class);

	@Autowired
	private HarborService harborService;

	@Value("#{propertiesReader['image.host']}")
	private String harhorHost;
	
	@Value("#{propertiesReader['image.username']}")
	private String harborUser;
	
	@Value("#{propertiesReader['image.password']}")
	private String harborPassword;
	
	@Autowired
	private HarborMemberService hmService;

	@Autowired
	private HarborIntegrationService harborIntegrationService;
	
	@Autowired
	private RoleService roleService;

	@ResponseBody
	@RequestMapping(value="/image")
	public ActionReturnUtil getImages() throws Exception{
		
		try {
			return harborService.login(harborUser, harborPassword);
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * //获取current user所有project
	 * new version  获取所有project (admin)
	 * 
	 * @return
	 */
	@RequestMapping(value = "/image/project")
	@ResponseBody
	public ActionReturnUtil listImage() throws Exception{
		
		try {
			return harborService.projectList(null, null);
		} catch (Exception e) {
			throw e;
		}
	}

	@ResponseBody
	@RequestMapping(value = "/image/project/userList", method = RequestMethod.GET)
	public ActionReturnUtil getUsersByProject(@RequestParam(value="projectId") String projectId)throws Exception {
		
		try {
			return hmService.usersOfProject(Integer.valueOf(projectId));
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 获取该用户的harbor-project列表
	 * @param user
	 * @return
	 * @throws Exception 
	 */
	@ResponseBody
	@RequestMapping(value = "/image/user/projectList", method = RequestMethod.GET)
	public ActionReturnUtil getProjectsByUser(@RequestParam(value="user") final String user) throws Exception {
		return this.harborService.getProjectByUser(user);
	}

	/**
	 * 获取该projectId下的镜像列表
	 * 
	 * @param projectId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/image/repo", method = RequestMethod.GET)
	public ActionReturnUtil repo(@RequestParam(value = "pid") final Integer projectId)throws Exception {
		try {
			//return harborIntegrationService.getRepoDomainDetailByProjectId(projectId);
			return harborService.getRepositoryDetailByProjectId(projectId);
		} catch (Exception e) {
			throw e;
			
		}
	}

	/**
	 * delete image
	 *
	 * @param repoName repo name
	 * @param tag      tag
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/image/repo", method = RequestMethod.DELETE)
	public ActionReturnUtil delRepo(@RequestParam(value="repoName") String repoName, @RequestParam(value="tag", required=false) String tag) throws Exception{
		try {
			return harborService.deleteRepo(repoName, tag);
		} catch (Exception e) {
			throw e;
		}
	}
	/**
	 * tag detail
	 *
	 * @param repoName repo name
	 * @param tag      tag
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/image/repo/tag", method = RequestMethod.GET)
	public ActionReturnUtil manifest(@RequestParam(value="repoName") String repoName, @RequestParam(value="tag") String tag)throws Exception {
		
		try {
			return harborService.getManifests(repoName, tag);
		} catch (Exception e) {
			throw e;
		}
		
	}

	@ResponseBody
	@RequestMapping(value = "/image/scan", method = RequestMethod.POST)
	public ActionReturnUtil scanImages(String projectId) throws Exception{
		return null;
	}

	@ResponseBody
	@RequestMapping(value = "/image/initCVEStatus", method = RequestMethod.POST)
	public ActionReturnUtil initCVEStatus(String projectId) throws Exception{
		return null;
	}

	@ResponseBody
	@RequestMapping(value = "/image/benchSecurity", method = RequestMethod.POST)
	public ActionReturnUtil benchSecurity(String projectId) throws Exception{
		return null;
	}

	@ResponseBody
	@RequestMapping(value = "/image/benchSecurityRuntime", method = RequestMethod.POST)
	public ActionReturnUtil benchSecurityRuntime(String projectId) throws Exception{
		return null;
	}

	@ResponseBody
	@RequestMapping(value = "/image/getBenchSecurity", method = RequestMethod.GET)
	public ActionReturnUtil getBenchSecurity(String projectId)throws Exception {
		return null;
	}

	@ResponseBody
	@RequestMapping(value = "/image/upload", method = RequestMethod.POST)
	public ActionReturnUtil fileUpload(@RequestParam(value="file") MultipartFile file,
									   @RequestParam("imageFullName") String imageName) {
		if(file.isEmpty()){
			return ActionReturnUtil.returnErrorWithData("文件内容不能为空");
		}
		if(!file.getName().endsWith(".tar")){
			return ActionReturnUtil.returnErrorWithData("只支持tar文件上传");
		}
		return harborService.uploadImage(file, imageName);
	}

	@RequestMapping(value="/image/download",method=RequestMethod.GET) //匹配的是href中的download请求
	public void download(@RequestParam("imageName") String imageName, HttpServletResponse response){
		OutputStream outputStream = null;
		InputStream inputStream = null;
        try {
			String fileName = imageName.substring(imageName.lastIndexOf("/")+1) + ".tar";
			fileName = fileName.replace(":", "_");
			response.setContentType("multipart/form-data");
			response.setHeader("Content-Disposition", "attachment;fileName=" + fileName);
			outputStream = response.getOutputStream();
			inputStream =  harborService.downloadImage(imageName);
			//循环写入输出流
			byte[] b = new byte[2048];
			int length;
			while ((length = inputStream.read(b)) > 0) {
				outputStream.write(b, 0, length);
			}
			outputStream.flush();
			outputStream.close();
			inputStream.close();
			harborService.removeImage(imageName);
		}catch (Exception e){
			LOGGER.error("下载镜像失败", e);
		}
	}
}
