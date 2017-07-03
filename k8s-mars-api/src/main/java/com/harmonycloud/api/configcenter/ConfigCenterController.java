package com.harmonycloud.api.configcenter;

import javax.servlet.http.HttpSession;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.config.ConfigDetailDto;
import com.harmonycloud.service.platform.service.ConfigCenterService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by gurongyun on 17/03/24.
 */
@RequestMapping("/config")
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
	public ActionReturnUtil saveConfig(@ModelAttribute ConfigDetailDto configDetail) throws Exception {
		logger.info("新增配置文件");
		String userName = (String) session.getAttribute("username");
		return configCenterService.saveOrUpdateConfig(configDetail, userName);
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
	public ActionReturnUtil updateConfig(@ModelAttribute ConfigDetailDto configDetail) throws Exception {
		logger.info("修改配置文件");
		String userName = (String) session.getAttribute("username");
		return configCenterService.saveOrUpdateConfig(configDetail, userName);
	}

	/**
	 * delete config on 17/03/24.
	 * 
	 * @author gurongyun
	 * @param id
	 *            required
	 * @param tenant
	 *            required
	 * @return ActionReturnUtil
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.DELETE)
	public ActionReturnUtil removeConfig(@RequestParam(value = "id", required = true) String id,
			@RequestParam(value = "tenant", required = true) String tenant) throws Exception {
		logger.info("删除配置文件");
		return configCenterService.removeConfig(id, tenant);
	}

	/**
	 * delete configs on 17/03/24.
	 * 
	 * @author gurongyun
	 * @param name
	 *            required
	 * @param tenant
	 *            required
	 * @return ActionReturnUtil
	 */
	@ResponseBody
	@RequestMapping(value = "/list", method = RequestMethod.DELETE)
	public ActionReturnUtil deleteConfigs(@RequestParam(value = "name", required = true) String name,
			@RequestParam(value = "tenant", required = true) String tenant,
			@RequestParam(value = "reponame", required = true) String repoName) throws Exception {
		logger.info("删除配置文件");
		return configCenterService.deleteConfigs(name, tenant, repoName);
	}

	/**
	 * find a config on 17/03/24.
	 * 
	 * @author gurongyun
	 * @param id
	 *            required
	 * @return ActionReturnUtil
	 */
	@ResponseBody
	@RequestMapping(value = "/detail", method = RequestMethod.GET)
	public ActionReturnUtil getConfigById(@RequestParam(value = "id", required = true) String id) throws Exception {
		logger.info("获取详细配置文件");
		return configCenterService.getById(id);
	}

	/**
	 * find a lastest config on 17/03/24.
	 * 
	 * @author gurongyun
	 * @param name
	 *            required
	 * @param tenant
	 *            required
	 * @return ActionReturnUtil
	 */
	@ResponseBody
	@RequestMapping(value = "/latest", method = RequestMethod.GET)
	public ActionReturnUtil getConfigByName(@RequestParam(value = "name", required = true) String name,
			@RequestParam(value = "tenant", required = true) String tenant,
			@RequestParam(value = "reponame", required = true) String repoName) throws Exception {
		logger.info("获取详细配置文件");
		return configCenterService.getConfigByName(name, tenant, repoName);
	}

	/**
	 * find config lists for center on 17/03/24.
	 * 
	 * @author gurongyun
	 * @param tenant
	 * 
	 * @param keyword
	 * 
	 * @return ActionReturnUtil
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	public ActionReturnUtil listConfig(@RequestParam(value = "tenant", required = false) String tenant,
			@RequestParam(value = "keyword", required = false) String keyword) throws Exception {
		logger.info("获取配置中心列表");
		return configCenterService.listConfigSearch(tenant, keyword);
	}

	/**
	 * find config overview lists on 17/03/24.
	 * 
	 * @author gurongyun
	 * @param tenant
	 *            required
	 * @return ActionReturnUtil
	 */
	@ResponseBody
	@RequestMapping(value = "/overview", method = RequestMethod.GET)
	public ActionReturnUtil listConfigOverview(@RequestParam(value = "tenant", required = false) String tenant,
			@RequestParam(value = "reponame", required = false) String repoName) throws Exception {
		logger.info("获取配置overview");
		return configCenterService.listConfigOverview(tenant, repoName);
	}

	/**
	 * find configMap on 17/03/24.
	 * 
	 * @author gurongyun
	 * @param id
	 *            required
	 * @return ActionReturnUtil
	 */
	@ResponseBody
	@RequestMapping(value = "/map", method = RequestMethod.GET)
	public ActionReturnUtil getConfigMap(@RequestParam(value = "id", required = true) String id) throws Exception {
		logger.info("获取configmap");
		return configCenterService.getConfigMap(id);

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
	@RequestMapping(value = "/check", method = RequestMethod.GET)
	public ActionReturnUtil checkName(@RequestParam(value = "name", required = true) String name,@RequestParam(value = "tenant", required = true) String tenant) throws Exception {
		logger.info("checkName");
		return configCenterService.checkName(name,tenant);

	}
}
