package com.harmonycloud.api.harbor;

import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.K8sAuthException;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.dao.harbor.bean.ImageCleanRule;
import com.harmonycloud.dao.harbor.bean.ImageRepository;
import com.harmonycloud.service.platform.bean.RepositoryInfo;
import com.harmonycloud.service.platform.bean.harbor.HarborProject;
import com.harmonycloud.service.platform.bean.harbor.HarborProjectLabel;
import com.harmonycloud.service.platform.bean.harbor.HarborProjectQuota;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.platform.service.harbor.HarborImageCleanService;
import com.harmonycloud.service.platform.service.harbor.HarborSecurityService;
import com.harmonycloud.service.platform.service.harbor.HarborService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.service.platform.service.harbor.HarborProjectService;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;


/**
 * 镜像仓库相关请求url控制
 * @author jmi
 *
 */
@Api(description = "harbor仓库项目管理")
@Controller
@RequestMapping("/tenants/{tenantId}/projects/{projectId}/repositories")
public class HarborProjectController {

	@Autowired
	private HttpSession session;
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
	@ApiOperation(value = "创建镜像仓库", notes = "创建镜像仓库")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "projectId", value = "项目id", paramType = "path",dataType = "String"),
			@ApiImplicitParam(name = "tenantId", value = "租户id", paramType = "path",dataType = "String")})
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
	@ApiOperation(value = "查询仓库项目列表", notes = "根据条件查询仓库项目列表")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "projectId", value = "项目id", paramType = "path",dataType = "String"),
			@ApiImplicitParam(name = "clusterId", value = "集群id", paramType = "query",dataType = "String"),
			@ApiImplicitParam(name = "isPublic", value = "是否公共仓库", paramType = "query",dataType = "Boolean")})
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
	@ApiOperation(value = "查询用户权限下的仓库项目列表", notes = "查询用户可以操作的镜像仓库列表(包括私有镜像仓库和公有镜像仓库)")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "projectId", value = "项目id", paramType = "path",dataType = "String"),
			@ApiImplicitParam(name = "clusterId", value = "集群id", paramType = "query",dataType = "String"),
			@ApiImplicitParam(name = "isPublic", value = "是否公仓库", paramType = "query",dataType = "Boolean")})
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
	@ApiOperation(value = "查询单个项目仓库", notes = "查询单个项目仓库信息")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "repositoryId", value = "镜像仓库id", paramType = "path",dataType = "Integer")})
	@RequestMapping(value = "/{repositoryId}", method = RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil getRepository(@PathVariable(value = "repositoryId") Integer repositoryId){
		return ActionReturnUtil.returnSuccessWithData(harborProjectService.findRepositoryById(repositoryId));
	}

	/**
	 * 查询镜像可以推送的环境
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/{repositoryId}/syncclusters", method = RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil listSyncClusters(@PathVariable(value = "repositoryId") Integer repositoryId)throws Exception{
		return ActionReturnUtil.returnSuccessWithData(harborProjectService.listSyncClusters(repositoryId, null, null));
	}

	/**查找某个镜像仓库的详情,包括各个镜像的详细信息
	 *
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "查询单个项目仓库详情", notes = "查找某个镜像仓库的详情,包括各个镜像的详细信息")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "repositoryId", value = "镜像仓库id", paramType = "path",dataType = "Integer")})
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
	@ApiOperation(value = "查询单个项目仓库的总览信息", notes = "查找某个镜像仓库的详情,包括磁盘使用情况和镜像安全分析结果")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "repositoryId", value = "镜像仓库id", paramType = "path",dataType = "Integer")})
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
	@ApiOperation(value = "重建仓库", notes = "对状态异常（harbor上创建仓库失败）的镜像仓库重新在harbor上创建")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "repositoryId", value = "镜像仓库id", paramType = "path",dataType = "Integer")})
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
	@ApiOperation(value = "删除镜像仓库", notes = "删除某个镜像仓库")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "repositoryId", value = "镜像仓库id", paramType = "path",dataType = "Integer")})
	@RequestMapping(value = "/{repositoryId}", method = RequestMethod.DELETE)
	@ResponseBody
	public ActionReturnUtil deleteRepository(@PathVariable(value = "repositoryId") Integer repositoryId) throws Exception{
		boolean result = harborProjectService.deleteRepositoryById(repositoryId);
		if(result){
			return ActionReturnUtil.returnSuccessWithData(result);
		}else{
			return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.DELETE_FAIL);
		}

	}


	/**
	 * 以镜像仓库项目维度查看一个镜像仓库下的镜像安全扫描结果
	 *
	 * @return
	 */
	@ApiOperation(value = "查询镜像安全扫描结果", notes = "以镜像仓库项目维度查看一个镜像仓库下的镜像安全扫描结果")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "repositoryId", value = "镜像仓库id", paramType = "path",dataType = "Integer")})
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
	@ApiOperation(value = "更新仓库配额", notes = "更新仓库配额")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "repositoryId", value = "镜像仓库id", paramType = "path",dataType = "Integer")})
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
	@ApiOperation(value = "查询仓库配额", notes = "查询仓库配额")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "repositoryId", value = "镜像仓库id", paramType = "path",dataType = "Integer")})
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
	@ApiOperation(value = "搜索镜像", notes = "根据名称搜索镜像")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "projectId", value = "项目id", paramType = "path",dataType = "String"),
			@ApiImplicitParam(name = "query", value = "查询关键字", paramType = "query",dataType = "String"),
			@ApiImplicitParam(name = "isPublic", value = "是否公共镜像", paramType = "query",dataType = "Boolean")})
	@RequestMapping(value = "/search", method = RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil getFuzzySearchResult(@RequestParam(value = "query") String query,
												 @PathVariable(value="projectId") String projectId,
												 @RequestParam(value = "clusterId",required = false) String clusterId,
												 @RequestParam(value="isPublic",required = false) Boolean isPublic)throws Exception{
		return harborService.getRepoFuzzySearch(query,projectId,clusterId,isPublic);
	}

	/**
	 * 创建镜像清理规则
	 *
	 * @param rule
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "创建镜像清理规则", notes = "创建镜像清理规则")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "repositoryId", value = "镜像仓库id", paramType = "path",dataType = "Integer")})
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
	@ApiOperation(value = "更新镜像清理规则", notes = "更新镜像清理规则")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "repositoryId", value = "镜像仓库id", paramType = "path",dataType = "Integer")})
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
	@ApiOperation(value = "根据仓库名获取清理规则", notes = "根据仓库名获取清理规则")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "repositoryId", value = "镜像仓库id", paramType = "path",dataType = "Integer")})
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
	@ApiOperation(value = "根据规则id获取清理规则", notes = "根据规则id获取清理规则")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "ruleId", value = "清理规则id", paramType = "path",dataType = "Long")})
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
	@ApiOperation(value = "根据规则id删除清理规则", notes = "根据规则id删除清理规则")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "cleanRuleId", value = "清理规则id", paramType = "path",dataType = "Long")})
	@RequestMapping(value = "/{repositoryId}/cleanrules/{cleanRuleId}", method = RequestMethod.DELETE)
	@ResponseBody
	public ActionReturnUtil deleteCleanRule(@PathVariable(value="cleanRuleId") Long cleanRuleId) throws Exception{
		ImageCleanRule rule = new ImageCleanRule();
		rule.setId(cleanRuleId);
		return harborImageCleanService.setCleanRule(rule, Constant.DB_OPERATION_FLAG_DELETE);
	}

	/**
	 * 增加标签
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "增加标签", notes = "增加标签")
	@RequestMapping(value = "/label", method = RequestMethod.POST)
	@ResponseBody
	public ActionReturnUtil addLabel(@RequestParam(value="harborHost") String harborHost,
									 @RequestParam(value = "name",required = false) String name,
									 @RequestParam(value = "desc",required = false) String desc,
									 @RequestParam(value = "color") String color,
									 @RequestParam(value = "scop",required = false) String scop,
									 @RequestParam(value = "harborProjectId") Integer harborProjectId) throws Exception{

		return harborProjectService.addLabel(harborHost,name,desc,color,scop,harborProjectId);
	}

	/**
	 * 删除标签
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "删除标签", notes = "删除标签")
	@RequestMapping(value = "/label", method = RequestMethod.DELETE)
	@ResponseBody
	public ActionReturnUtil delLabel(@RequestParam(value="harborHost") String harborHost,
									 @RequestParam(value = "lableId") Long lableId) throws Exception{

		return harborProjectService.deleteLabel(harborHost,lableId);
	}

	/**
	 * 获取标签
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "获取标签", notes = "获取标签")
	@RequestMapping(value = "/label", method = RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil getLabels(@RequestParam(value="harborHost") String harborHost,
									  @RequestParam(value = "repoName",required = false) String repoName,
									  @RequestParam(value = "scope",required = false) String scope,
									  @RequestParam(value = "harborProjectId") Long harborProjectId,
									  @RequestParam(value = "labelName", required = false) String labelName) throws Exception{
		return harborProjectService.getLabel(harborHost, repoName, scope, harborProjectId, labelName);
	}
	/**
	 * 修改标签
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "修改标签", notes = "修改标签")
	@RequestMapping(value = "/label", method = RequestMethod.PUT)
	@ResponseBody
	public ActionReturnUtil updateLabel(@RequestParam(value="harborHost") String harborHost,
										@RequestParam(value = "labelId") Long labelId,
										@ModelAttribute HarborProjectLabel harborProjectLabel) throws Exception{

		return harborProjectService.updateLabel(harborHost, labelId, harborProjectLabel);
	}
}



