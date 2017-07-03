package com.harmonycloud.api.harbor;

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

@Controller
//@RequestMapping(value = "")
public class HarborImageController {

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


}
