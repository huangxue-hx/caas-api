package com.harmonycloud.service.platform.serviceipml.configcenter;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.dao.application.ConfigFileMapper;
import com.harmonycloud.dao.application.bean.ConfigFile;
import com.harmonycloud.dto.config.ConfigDetailDto;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.platform.service.ConfigCenterService;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Created by gurongyun on 17/03/24. configcenter serviceImpl
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ConfigCenterServiceImpl implements ConfigCenterService {

	private static final int STRING_BIT = 64;

	DecimalFormat decimalFormat = new DecimalFormat("######0.0");

	@Autowired
	private ConfigFileMapper configFileMapper;

	/**
	 * add or update config serviceImpl on 17/03/24.
	 * 
	 * @author gurongyun
	 * @param configDetail
	 *            required
	 * @return ActionReturnUtil
	 */
	@Override
	public ActionReturnUtil saveOrUpdateConfig(ConfigDetailDto configDetail, String userName) throws Exception {
		// check params
		if (configDetail == null || StringUtils.isEmpty(configDetail.getName()) || StringUtils.isEmpty(configDetail.getItems())) {
			return ActionReturnUtil.returnErrorWithMsg("配置文件名称 或者配置内容为空");
		}

		if (StringUtils.isEmpty(userName)) {
			return ActionReturnUtil.returnErrorWithMsg("userName 为空");
		}
		
		// validate name
		ConfigFile configFileTenant = configFileMapper.getTenantByName(configDetail.getName());
		if (configFileTenant != null && !configFileTenant.getTenant().equals(configDetail.getTenant())) {
			return ActionReturnUtil.returnErrorWithMsg("配置文件名称已存在");
		}

		double tags = Constant.TEMPLATE_TAG;
		// 检查数据库有没有存在
		List<ConfigFile> list=configFileMapper.listConfigByName(configDetail.getTenant(),configDetail.getName(), configDetail.getRepoName());
		ConfigFile configFile = new ConfigFile();
		// 随机生成64位字符串
		String cfgid = checkRandomString();
		configFile.setId(cfgid);
		configFile.setName(configDetail.getName());
		configFile.setCreateTime(DateUtil.timeFormat.format(new Date()));

		configFile.setDescription(configDetail.getDescription());
		configFile.setTenant(configDetail.getTenant());
		configFile.setUser(userName);
		configFile.setRepoName(configDetail.getRepoName());
		configFile.setItem(configDetail.getItems());
		configFile.setPath(configDetail.getPath());
		boolean equals = false;
		if (list != null && list.size() >0) {
			// 存在版本号+0.1
			tags = Double.valueOf(list.get(0).getTags()) + Constant.TEMPLATE_TAG_INCREMENT;
			for(ConfigFile c : list){
				equals = configFile.equals(c);
				if(equals){
					JSONObject resultJson = new JSONObject();
					resultJson.put("filename", c.getName());
					resultJson.put("tag", c.getTags());
					return ActionReturnUtil.returnSuccessWithData(resultJson);
				}
			}
		}
		configFile.setTags(decimalFormat.format(tags) + "");
		// 入库
		configFileMapper.saveConfigFile(configFile);
		JSONObject resultJson = new JSONObject();
		resultJson.put("filename", configDetail.getName());
		resultJson.put("tag", tags);
		return ActionReturnUtil.returnSuccessWithData(resultJson);
	}

	/**
	 * delete config serviceImpl on 17/03/24.
	 * 
	 * @author gurongyun
	 * @param id
	 *            required
	 * @param tenant
	 *            required
	 * @return ActionReturnUtil
	 */
	@Override
	public ActionReturnUtil removeConfig(String id, String tenant) throws Exception {
		// check params
		if (StringUtils.isEmpty(id) || StringUtils.isEmpty(tenant)) {
			return ActionReturnUtil.returnErrorWithMsg("参数为空");
		}
		// 判断有没有配置文件
		ConfigFile configFile = configFileMapper.getConfigFileById(id);
		// 配置文件存在
		if (configFile != null) {
			// 该配置文件是否属于该租户
			if (tenant.equals(configFile.getTenant())) {
				// 删除配置文件
				configFileMapper.removeConfigFileById(id);
			} else {
				return ActionReturnUtil.returnErrorWithMsg("权限不够");
			}
		} else {
			return ActionReturnUtil.returnErrorWithMsg("配置文件不存在");
		}
		return ActionReturnUtil.returnSuccess();
	}

	/**
	 * find config lists for center serviceImpl on 17/03/24.
	 * 
	 * @author gurongyun
	 * @param tenant
	 *            required
	 * @return ActionReturnUtil
	 */
	@Override
	public ActionReturnUtil listConfigSearch(String tenant, String keyword) throws Exception {
		JSONArray array = new JSONArray();
		// 查询租户下所有的配置文件
		List<ConfigFile> list = configFileMapper.listConfigSearch(tenant, keyword);
		if (list == null || list.size() == 0) {
			return ActionReturnUtil.returnSuccessWithData(null);
		} else {
			// 存在-遍历
			for (ConfigFile configFile : list) {
				JSONObject json = new JSONObject();
				json.put("name", configFile.getName());
				json.put("reponame", configFile.getRepoName());
				JSONArray configFileTagsArray = new JSONArray();
				// 查询同一配置文件不同版本
				List<ConfigFile> lis = configFileMapper.listConfigByNameAsc(configFile.getName(),
						configFile.getTenant(), configFile.getRepoName());
				int count = 0;
				if (lis != null && lis.size() > 0) {
					count = lis.size();
					for (ConfigFile c : lis) {
						JSONObject tags = new JSONObject();
						// 添加版本号Id
						tags.put("id", c.getId());
						tags.put("tag", c.getTags());
						tags.put("path", c.getPath());
						json.put("create_time", c.getCreateTime());
						configFileTagsArray.add(tags);
					}
				}
				json.put("tagcount", count);
				json.put("tags", configFileTagsArray);
				array.add(json);
			}
		}
		return ActionReturnUtil.returnSuccessWithData(array);
	}

	/**
	 * find config overview lists serviceImpl on 17/03/24.
	 * 
	 * @author gurongyun
	 * @param tenant
	 *            required
	 * @return ActionReturnUtil
	 */
	@Override
	public ActionReturnUtil listConfigOverview(String tenant, String repoName) throws Exception {
		JSONArray array = new JSONArray();
		// 查询同一repo下的所有配置文件
		List<ConfigFile> lists = configFileMapper.listConfigOverview(tenant, repoName);
		if (lists == null || lists.size() == 0) {
			return ActionReturnUtil.returnSuccessWithData(null);
		} else {
			for (int i = 0; i < lists.size(); i++) {
				JSONObject json = new JSONObject();
				json.put("name", lists.get(i).getName());
				json.put("tenant", lists.get(i).getTenant());
				json.put("repo", lists.get(i).getRepoName());
				JSONArray jsarr=new JSONArray();
				List<ConfigFile> configli=configFileMapper.listConfigByName(lists.get(i).getTenant(),lists.get(i).getName(),lists.get(i).getRepoName());
				if(configli != null && configli.size() > 0){
					for(ConfigFile con : configli){
						JSONObject js=new JSONObject();
						js.put("id", con.getId());
						js.put("tag", con.getTags());
						js.put("item", con.getItem());
						js.put("desc", con.getDescription());
						js.put("path", con.getPath());
						jsarr.add(js);
					}	
				}
				json.put("tags", jsarr);
				array.add(json);
			}
		}
		return ActionReturnUtil.returnSuccessWithData(array);
	}

	/**
	 * find configMap serviceImpl on 17/03/24.
	 * 
	 * @author gurongyun
	 * @param id
	 *            required
	 * @return ActionReturnUtil
	 */
	@Override
	public ActionReturnUtil getConfigMap(String id) throws Exception {
		JSONObject json = new JSONObject();
		// 查找配置文件
		ConfigFile cfgf = configFileMapper.getConfigFileById(id);
		json.put("name", cfgf.getName());
		json.put("tags", cfgf.getTags());
		json.put("value", cfgf.getItem());
		json.put("path", cfgf.getPath());
		return ActionReturnUtil.returnSuccessWithData(json);
	}

	/**
	 * find a config by id serviceImpl on 17/03/24.
	 * 
	 * @author gurongyun
	 * @param id
	 *            required
	 * @return ActionReturnUtil
	 */
	@Override
	public ActionReturnUtil getById(String id) throws Exception {
		JSONObject json = new JSONObject();
		// 获取配置文件
		ConfigFile cfgf = configFileMapper.getConfigFileById(id);
		json.put("id", cfgf.getId());
		json.put("description", cfgf.getDescription());
		json.put("name", cfgf.getName());
		json.put("tags", cfgf.getTags());
		json.put("tenant", cfgf.getTenant());
		json.put("user", cfgf.getUser());
		json.put("reponame", cfgf.getRepoName());
		json.put("create_time", cfgf.getCreateTime());
		json.put("items", cfgf.getItem());
		json.put("path", cfgf.getPath());
		return ActionReturnUtil.returnSuccessWithData(json);
	}

	/**
	 * to be 64 random String on 17/03/24.
	 * 
	 * @author gurongyun
	 * @return String
	 */
	public static String getRandomString() {
		String base = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		Random random = new Random();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < STRING_BIT; i++) {
			int number = random.nextInt(base.length());
			sb.append(base.charAt(number));
		}
		return sb.toString();
	}

	/**
	 * check random String on DB on 17/03/24.
	 * 
	 * @author gurongyun
	 * @return String
	 */
	public String checkRandomString() {
		// 随机生成64位
		String id = getRandomString();
		boolean blag = true;
		do {
			String cfgid = configFileMapper.getId(id);
			if (cfgid == null) {
				blag = false;
			} else {
				id = getRandomString();
			}
		} while (blag);
		return id;

	}

	/**
	 * delete configs service on 17/03/24.
	 * 
	 * @author gurongyun
	 * @param name
	 *            required
	 * @param tenant
	 *            required
	 * @return ActionReturnUtil
	 */
	@Override
	public ActionReturnUtil deleteConfigs(String name, String tenant, String repoName) throws Exception {
		// 获取配置文件
		List<ConfigFile> list = configFileMapper.listConfigByName(tenant, name, repoName);
		if (list != null && list.size() > 0) {
			// 删除配置文件
			configFileMapper.deleteConfigFileByName(name, tenant, repoName);
		} else {
			return ActionReturnUtil.returnErrorWithMsg("配置文件不存在");
		}
		return ActionReturnUtil.returnSuccess();
	}
	
	/**
	 * delete configs service on 17/03/24.
	 * 
	 * @author gurongyun
	 * @param name
	 *            required
	 * @param tenant
	 *            required
	 * @return ActionReturnUtil
	 */
	@Override
	public ActionReturnUtil deleteConfigsByTenant(String tenant) throws Exception {
		// 获取配置文件
		if(!StringUtils.isEmpty(tenant)){
			configFileMapper.deleteConfigFileByTenant(tenant);
			return ActionReturnUtil.returnSuccess();
		}else{
			return ActionReturnUtil.returnErrorWithMsg("租户不能为空");
		}
	}

	/**
	 * find a lastest config service on 17/03/24.
	 * 
	 * @author gurongyun
	 * @param name
	 *            required
	 * @param tenant
	 *            required
	 * @return ActionReturnUtil
	 */
	@Override
	public ActionReturnUtil getConfigByName(String name, String tenant, String repoName) throws Exception {
		JSONObject json = new JSONObject();
		// 获取配置文件（最新）
		List<ConfigFile> list = configFileMapper.listConfigByNameLatest(name, tenant, repoName);
		if (list == null || list.size() == 0) {
			return ActionReturnUtil.returnErrorWithMsg("没有配置文件");
		}
		ConfigFile cfgf = list.get(0);
		json.put("id", cfgf.getId());
		json.put("description", cfgf.getDescription());
		json.put("name", cfgf.getName());
		json.put("tags", cfgf.getTags());
		json.put("reponame", cfgf.getRepoName());
		json.put("tenant", cfgf.getTenant());
		json.put("create_time", cfgf.getCreateTime());
		json.put("items", cfgf.getItem());
		json.put("path", cfgf.getPath());
		return ActionReturnUtil.returnSuccessWithData(json);
	}

	@Override
	public ActionReturnUtil checkName(String name, String tenant) throws Exception {
		// check params
		if (StringUtils.isEmpty(name) || StringUtils.isEmpty(tenant)) {
			return ActionReturnUtil.returnErrorWithMsg("name or tenant is null");
		}

		// validate name
		ConfigFile configFileTenant = configFileMapper.getTenantByName(name);
		if (configFileTenant != null && !configFileTenant.getTenant().equals(tenant)) {
			return ActionReturnUtil.returnSuccessWithData(false);
		} else {
			return ActionReturnUtil.returnSuccessWithData(true);
		}
	}

}
