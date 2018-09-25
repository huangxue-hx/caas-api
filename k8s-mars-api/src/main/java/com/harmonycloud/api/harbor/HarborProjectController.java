package com.harmonycloud.api.harbor;

import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.K8sAuthException;
import com.harmonycloud.dao.harbor.bean.ImageCleanRule;
import com.harmonycloud.dao.harbor.bean.ImageRepository;
import com.harmonycloud.service.platform.bean.RepositoryInfo;
import com.harmonycloud.service.platform.bean.harbor.HarborProject;
import com.harmonycloud.service.platform.bean.harbor.HarborProjectQuota;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.platform.service.harbor.HarborImageCleanService;
import com.harmonycloud.service.platform.service.harbor.HarborSecurityService;
import com.harmonycloud.service.platform.service.harbor.HarborService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.service.platform.service.harbor.HarborProjectService;

import javax.servlet.http.HttpSession;
import java.util.List;


/**
 * 镜像仓库相关请求url控制
 * @author jmi
 *
 */
@Controller
@RequestMapping("/tenants/{tenantId}/projects/{projectId}/repositories")
public class HarborProjectController {

	@Autowired
	HttpSession session;
	@Autowired
	private HarborProjectService harborProjectService;
	@Autowired
	private HarborSecurityService harborSecurityService;
	@Autowired
	private HarborService harborService;
	@Autowired
	private HarborImageCleanService harborImageCleanService;

	/**
	 * 创建镜像仓库
	 * @param repositoryInfo
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public ActionReturnUtil createRepository(@ModelAttribute RepositoryInfo repositoryInfo,
											 @PathVariable(value = "tenantId") String tenantId,
											 @PathVariable(value = "projectId") String projectId) throws Exception{
		repositoryInfo.setTenantId(tenantId);
		repositoryInfo.setProjectId(projectId);
		return harborProjectService.createRepository(repositoryInfo);
	}

	/**
	 *  查询镜像仓库列表
	 * @param clusterId
	 * @param projectId
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil listRepositories(@RequestParam(value = "clusterId",required = false) String clusterId,
											 @RequestParam(value = "isPublic",required = false) Boolean isPublic,
											 @PathVariable(value = "projectId") String projectId) throws Exception{
		List<ImageRepository> imageRepositories = harborProjectService.listRepositoryDetails(projectId, clusterId, isPublic,Boolean.TRUE);
		return ActionReturnUtil.returnSuccessWithData(imageRepositories);
	}

	/**
	 *  查询用户可以操作的镜像仓库列表(包括私有镜像仓库和公有镜像仓库)
	 * @param projectId
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/userselect", method = RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil selectRepositories(@PathVariable(value = "projectId") String projectId,
											   @RequestParam(value = "clusterId",required = false) String clusterId,
	                                           @RequestParam(value = "isPublic",required = false) Boolean isPublic) throws Exception{
		List<ImageRepository> imageRepositories = harborProjectService.listRepositories(projectId, clusterId, isPublic, Boolean.TRUE);
		return ActionReturnUtil.returnSuccessWithData(imageRepositories);
	}

	/**
	 * 查找某个镜像仓库
	 * @param repositoryId
	 * @return
	 */
	@RequestMapping(value = "/{repositoryId}", method = RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil getRepository(@PathVariable(value = "repositoryId") Integer repositoryId){
		return ActionReturnUtil.returnSuccessWithData(harborProjectService.findRepositoryById(repositoryId));
	}

	/**查找某个镜像仓库的详情,包括各个镜像的详细信息
	 *
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/{repositoryId}/detail", method = RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil getRepositoryDetail(@PathVariable(value = "repositoryId") Integer repositoryId)throws Exception{
		return harborProjectService.getRepositoryDetail(repositoryId);
	}

	/**查找某个镜像仓库的详情,包括磁盘使用情况和镜像安全分析结果
	 *
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/{repositoryId}/summary", method = RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil getRepositorySummary(@PathVariable(value = "repositoryId") Integer repositoryId)throws Exception{
		return harborProjectService.getRepositorySummary(repositoryId);
	}

	/**
	 * 对状态异常（harbor上创建仓库失败）的镜像仓库重新在harbor上创建
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/{repositoryId}/enable", method = RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil enableRepository(@PathVariable(value = "repositoryId") Integer repositoryId)throws Exception{
		return ActionReturnUtil.returnSuccessWithData(harborProjectService.enableRepository(repositoryId));
	}

	/**
	 * 删除某个镜像仓库
	 * @param repositoryId
	 * @return
	 */
	@RequestMapping(value = "/{repositoryId}", method = RequestMethod.DELETE)
	@ResponseBody
	public ActionReturnUtil deleteRepository(@PathVariable(value = "repositoryId") Integer repositoryId) throws Exception{
		return ActionReturnUtil.returnSuccessWithData(harborProjectService.deleteRepositoryById(repositoryId));
	}


	/**
	 * 以镜像仓库项目维度查看一个镜像仓库下的镜像安全扫描结果
	 *
	 * @return
	 */
	@RequestMapping(value = "/{repositoryId}/clairstatics", method = RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil getClairStatics(@PathVariable(value = "repositoryId") Integer repositoryId) throws Exception{
		return harborSecurityService.getRepositoryClairStatistcs(repositoryId);
	}

	/**update projectquota
	 *
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/{repositoryId}/repositoryquota", method = RequestMethod.PUT)
	@ResponseBody
	public ActionReturnUtil updateQuota(@PathVariable(value = "repositoryId") Integer repositoryId,
										@ModelAttribute HarborProjectQuota harborProjectQuota)throws Exception{
		return harborProjectService.updateRepositoryQuota(repositoryId, harborProjectQuota);

	}
	/**get projectquota
	 *
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/{repositoryId}/repositoryquota", method = RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil getQuota(@PathVariable(value = "repositoryId") Integer repositoryId)throws Exception{
		HarborProject harborProject = harborProjectService.getRepositoryQuota(repositoryId);
		if(harborProject == null){
			return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.QUERY_FAIL);
		}
		return ActionReturnUtil.returnSuccessWithData(harborProject);
	}


	/**镜像repository的模糊查询
	 *
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/search", method = RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil getFuzzySearchResult(@RequestParam(value = "query") String query,
												 @PathVariable(value="projectId") String projectId,
												 @RequestParam(value="isPublic",required = false) Boolean isPublic)throws Exception{
		return harborService.getRepoFuzzySearch(query,projectId,isPublic);
	}

	/**
	 * 根据仓库名查找某个镜像仓库
	 * @return
	 */
	@RequestMapping(value = "/{harborProjectName}/briefinfo", method = RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil getRepositoryByName(@RequestParam("repoName") String repoName,
												@PathVariable(value = "tenantId") String tenantId,
												@PathVariable(value = "projectId") String projectId){
		return ActionReturnUtil.returnSuccessWithData(harborProjectService.getRepositoryByName(repoName,projectId,tenantId));
	}

	/**
	 * 创建镜像清理规则
	 *
	 * @param rule
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/{repositoryId}/cleanrules", method = RequestMethod.POST)
	@ResponseBody
	public ActionReturnUtil createCleanRule(@ModelAttribute ImageCleanRule rule,
											@PathVariable(value="repositoryId") Integer repositoryId) throws Exception{
        rule.setUserName(getSessionUser());
		rule.setRepositoryId(repositoryId);
		return harborImageCleanService.setCleanRule(rule, Constant.DB_OPERATION_FLAG_INSERT);
	}

	/**
	 * 从session中获取用户名
	 *
	 * @return
	 * @throws Exception
	 */
	private String getSessionUser() throws Exception{
		String userName = (String) session.getAttribute("username");
		if(userName == null){
			throw new K8sAuthException(com.harmonycloud.k8s.constant.Constant.HTTP_401);
		}
		return userName;
	}

	/**
	 * 更新镜像清理规则
	 *
	 * @param rule
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/{repositoryId}/cleanrules/{cleanRuleId}", method = RequestMethod.PUT)
	@ResponseBody
	public ActionReturnUtil updateCleanRule(@ModelAttribute ImageCleanRule rule,
											@PathVariable(value="repositoryId") Integer repositoryId) throws Exception{
		rule.setUserName(getSessionUser());
		rule.setRepositoryId(repositoryId);
		return harborImageCleanService.setCleanRule(rule, Constant.DB_OPERATION_FLAG_UPDATE);
	}

	/**
	 * 根据仓库名获取清理规则
	 *
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/{repositoryId}/cleanrules", method = RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil getCleanRule(@PathVariable(value="repositoryId") Integer repositoryId) throws Exception{
		ImageCleanRule rule = new ImageCleanRule();
		rule.setRepositoryId(repositoryId);
		return harborImageCleanService.setCleanRule(rule, Constant.DB_OPERATION_FLAG_QUERY);
	}

	/**
	 * 根据id获取清理规则
	 *
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/cleanrules/{cleanRuleId}", method = RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil getCleanRuleById(@PathVariable(value="cleanRuleId") Long ruleId) throws Exception{
		ImageCleanRule rule = new ImageCleanRule();
		rule.setId(ruleId);
		return harborImageCleanService.setCleanRule(rule, Constant.DB_OPERATION_FLAG_QUERY);
	}

	/**
	 * 删除镜像清理规则
	 *
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/{repositoryId}/cleanrules/{cleanRuleId}", method = RequestMethod.DELETE)
	@ResponseBody
	public ActionReturnUtil deleteCleanRule(@PathVariable(value="cleanRuleId") Long cleanRuleId) throws Exception{
		ImageCleanRule rule = new ImageCleanRule();
		rule.setId(cleanRuleId);
		return harborImageCleanService.setCleanRule(rule, Constant.DB_OPERATION_FLAG_DELETE);
	}

}



