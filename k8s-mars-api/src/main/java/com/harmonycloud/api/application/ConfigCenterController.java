package com.harmonycloud.api.application;

import javax.servlet.http.HttpSession;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.config.ConfigDetailDto;
import com.harmonycloud.service.platform.service.ConfigCenterService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * Created by gurongyun on 17/03/24.
 */
@RequestMapping("/tenants/{tenantId}/projects/{projectId}/configmap")
@Controller
public class ConfigCenterController {

	@Autowired
	HttpSession session;

	@Autowired
	private ConfigCenterService configCenterService;

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * add config on 17/03/24.
	 * 
	 * @author gurongyun
	 * @param configDetail
	 *            required
	 * @return ActionReturnUtil
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	public ActionReturnUtil saveConfigMap(@PathVariable("tenantId") String tenantId,
									   @PathVariable("projectId") String projectId,
									   @ModelAttribute ConfigDetailDto configDetail) throws Exception {
		logger.info("新增配置文件");
		configDetail.setProjectId(projectId);
		configDetail.setTenantId(tenantId);
		String userName = (String) session.getAttribute("username");
		return configCenterService.saveConfig(configDetail, userName);
	}

	/**
	 * update config on 17/03/24.
	 * 
	 * @author gurongyun
	 * @param configDetail
	 *            required
	 * @return ActionReturnUtil
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.PUT)
	public ActionReturnUtil updateConfigMap(@PathVariable("tenantId") String tenantId,
											@PathVariable("projectId") String projectId,
											@ModelAttribute ConfigDetailDto configDetail) throws Exception {
		logger.info("修改配置文件");
		configDetail.setProjectId(projectId);
		configDetail.setTenantId(tenantId);
		String userName = (String) session.getAttribute("username");
		return configCenterService.updateConfig(configDetail, userName);
	}

	/**
	 * delete config on 17/03/24.
	 * 
	 * @author gurongyun
	 * @return ActionReturnUtil
	 */
	@ResponseBody
	@RequestMapping(value = "/{configMapId}",method = RequestMethod.DELETE)
	public ActionReturnUtil deleteConfigMap(@PathVariable("tenantId") String tenantId,
											@PathVariable("projectId") String projectId,
											@PathVariable("configMapId") String configMapId) throws Exception {
		logger.info("删除配置文件,projectId:{},configMapId:{}",projectId,configMapId);
		configCenterService.deleteConfig(configMapId, projectId);
		return ActionReturnUtil.returnSuccess();
	}

	/**
	 * delete configs on 17/03/24.
	 * 
	 * @author gurongyun
	 * @param name
	 *            required
	 * @return ActionReturnUtil
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.DELETE)
	public ActionReturnUtil deleteConfig(@PathVariable("tenantId") String tenantId,
												  @PathVariable("projectId") String projectId,
			                                      @RequestParam(value = "name") String name,
			                                      @RequestParam(value = "clusterId") String clusterId) throws Exception {
		logger.info("删除配置文件");
		return configCenterService.deleteConfigMap(name, projectId, clusterId);
	}

	/**
	 * find a config on 17/03/24.
	 * 
	 * @author gurongyun
	 * @return ActionReturnUtil
	 */
	@ResponseBody
	@RequestMapping(value = "/{configMapId}", method = RequestMethod.GET)
	public ActionReturnUtil getConfigMap(@PathVariable("tenantId") String tenantId,
										  @PathVariable("projectId") String projectId,
										  @PathVariable("configMapId") String configMapId) throws Exception {
		return configCenterService.getConfigMap(configMapId);
	}

	/**
	 * find a lastest config on 17/03/24.
	 * 
	 * @author gurongyun
	 * @param name
	 *            required
	 * @return ActionReturnUtil
	 */
	@ResponseBody
	@RequestMapping(value = "/latest", method = RequestMethod.GET)
	public ActionReturnUtil getLatestConfigMap(@PathVariable("tenantId") String tenantId,
											@PathVariable("projectId") String projectId,
											@RequestParam(value = "name") String name,
											@RequestParam(value = "reponame") String repoName) throws Exception {
		return configCenterService.getLatestConfigMap(name, projectId, repoName);
	}

	/**
	 * find config lists for center on 17/03/24.
	 * 
	 * @author gurongyun
	 * 
	 * @param keyword
	 * 
	 * @return ActionReturnUtil
	 */
	@ResponseBody
	@RequestMapping(value = "/search",method = RequestMethod.GET)
	public ActionReturnUtil searchConfigMap(@PathVariable("projectId") String projectId,
											@RequestParam(value = "clusterId",required = false) String clusterId,
											@RequestParam(value = "reponame", required = false) String repoName,
			                                @RequestParam(value = "keyword", required = false) String keyword) throws Exception {
		return configCenterService.searchConfig(projectId, clusterId, repoName, keyword);
	}

	/**
	 * find config overview lists on 17/03/24.
	 * 
	 * @author gurongyun
	 * @return ActionReturnUtil
	 */
	@ResponseBody
	@RequestMapping( method = RequestMethod.GET)
	public ActionReturnUtil listConfigMap(@PathVariable("tenantId") String tenantId,
											   @PathVariable("projectId") String projectId,
			                                   @RequestParam(value = "reponame", required = false) String repoName) throws Exception {
		return configCenterService.listConfig(projectId, repoName);
	}
	
	/**
	 * check name on 17/03/24.
	 * 
	 * @author gurongyun
	 * @param name
	 *            required
	 * @return ActionReturnUtil
	 */
	@ResponseBody
	@RequestMapping(value = "/checkName", method = RequestMethod.GET)
	public ActionReturnUtil checkName(@PathVariable("tenantId") String tenantId,
									  @PathVariable("projectId") String projectId,
									  @RequestParam(value = "name") String name) throws Exception {
		return configCenterService.checkDuplicateName(name,projectId);
	}

	@ResponseBody
	@RequestMapping(value = "/content", method = RequestMethod.GET)
	public ActionReturnUtil getConfigMapByName(@RequestParam(value = "namespace")String namespace,
											   @RequestParam(value = "name")String name) throws Exception {
		return configCenterService.getConfigMapByName(namespace, name);
	}
}
