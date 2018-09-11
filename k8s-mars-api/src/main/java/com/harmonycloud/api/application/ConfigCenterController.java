package com.harmonycloud.api.application;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.application.ConfigServiceUpdateDto;
import com.harmonycloud.dto.config.ConfigDetailDto;
import com.harmonycloud.service.platform.service.ConfigCenterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

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
	 * 根据配置名称获取配置名称关联的所有服务
	 */
	@ResponseBody
	@RequestMapping(value = "/{configMapName}/services", method = RequestMethod.GET)
	public ActionReturnUtil getAllServiceByConfigName(@PathVariable("configMapName") String configName,
													  @PathVariable("projectId") String projectId,
													  @PathVariable("tenantId") String tenantId,
													  @RequestParam(value = "clusterId")String clusterId) throws Exception{
		return configCenterService.getAllServiceByConfigName(configName,clusterId,projectId,tenantId);
	}

	/**
	 * 更新所选服务配置版本
	 */
	@ResponseBody
	@RequestMapping(value = "/{configMapName}/deploy", method = RequestMethod.POST,consumes = "application/json")
	public ActionReturnUtil deployConfig(@RequestBody ConfigServiceUpdateDto configServiceUpdateDto,
												@PathVariable("configMapName") String configName,
												@PathVariable("projectId") String projectId,
												@PathVariable("tenantId") String tenantId) throws Exception{
		return configCenterService.updateConfigTag(configServiceUpdateDto.getServiceNameList(),configServiceUpdateDto.getTag(),configName,projectId,tenantId,configServiceUpdateDto.getClusterId());
	}

	/**
	 * 根据配置名称获取所有版本
	 */
	@ResponseBody
	@RequestMapping(value = "/{configMapName}/tags", method = RequestMethod.GET)
	public ActionReturnUtil getTagsByConfigName(@PathVariable("configMapName") String configName,
												   @PathVariable("projectId") String projectId,
												   @PathVariable("tenantId") String tenantId,
												   @RequestParam(value = "clusterId")String clusterId){
		return configCenterService.getTagsByConfigName(configName,clusterId,projectId);
	}

	/**
	 * 返回当前配置组的所有版本信息
	 * @param tenantId
	 * @param projectId
	 * @param configMapName
	 * @param clusterId
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/{configMapName}/detail", method = RequestMethod.GET)
	public ActionReturnUtil listConfigMapByName(@PathVariable("tenantId") String tenantId,
										 @PathVariable("projectId") String projectId,
										 @PathVariable("configMapName") String configMapName,
										 @RequestParam(value = "clusterId")String clusterId) throws Exception {
		return configCenterService.getConfigMapByName(configMapName,clusterId,projectId);
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
											@RequestParam(value = "reponame") String repoName,
											   @RequestParam(value = "clusterId",required = false)String clusterId,
											   @RequestParam(value = "tags") String tags) throws Exception {
		return configCenterService.getLatestConfigMap(name, projectId, repoName,clusterId,tags);
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
	 * 未使用
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

	/**
	 * 获取k8s中配置文件内容，回滚服务时
	 * @param namespace
	 * @param name
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/content", method = RequestMethod.GET)
	public ActionReturnUtil getConfigMapByName(@RequestParam(value = "namespace")String namespace,
											   @RequestParam(value = "name")String name) throws Exception {
		return configCenterService.getConfigMapByName(namespace, name);
	}

//	@ResponseBody
//	@RequestMapping(value = "/services", method = RequestMethod.GET)
//	public ActionReturnUtil getServiceList( @PathVariable("projectId") String projectId,
//											@PathVariable("tenantId") String tenantId,
//										    @RequestParam(value = "configMapId")String configMapId) throws Exception {
//		return configCenterService.getServiceList(projectId, tenantId,configMapId);
//	}
}
