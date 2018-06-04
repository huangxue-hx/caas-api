package com.harmonycloud.service.platform.serviceipml.configcenter;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.util.*;
import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.dao.application.ConfigFileMapper;
import com.harmonycloud.dao.application.bean.ConfigFile;
import com.harmonycloud.dto.config.ConfigDetailDto;
import com.harmonycloud.k8s.bean.ConfigMap;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.service.ConfigmapService;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.platform.service.ConfigCenterService;
import com.harmonycloud.service.tenant.NamespaceLocalService;
import com.harmonycloud.service.user.RoleLocalService;
import com.harmonycloud.service.user.UserService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by gurongyun on 17/03/24. configcenter serviceImpl
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ConfigCenterServiceImpl implements ConfigCenterService {

	DecimalFormat decimalFormat = new DecimalFormat("######0.0");

	@Autowired
	private ConfigFileMapper configFileMapper;
	@Autowired
	private ClusterService clusterService;
	@Autowired
	private RoleLocalService roleLocalService;
	@Autowired
	private UserService userService;

	@Autowired
	private ConfigmapService configmapService;

	@Autowired
	private NamespaceLocalService namespaceLocalService;

	/**
	 * add or update config serviceImpl on 17/03/24.
	 * 
	 * @author gurongyun
	 * @param configDetail
	 *            required
	 * @return ActionReturnUtil
	 */
	@Override
	public ActionReturnUtil saveConfig(ConfigDetailDto configDetail, String userName) throws Exception {
        Assert.notNull(configDetail);
		double tags = Constant.TEMPLATE_TAG;
		// 检查数据库有没有存在
		List<ConfigFile> list=configFileMapper.listConfigByName(configDetail.getName(), configDetail.getProjectId(),configDetail.getClusterId(),null);
		if (Objects.nonNull(configDetail.getIsCreate()) && Boolean.valueOf(configDetail.getIsCreate())) {
			if (!CollectionUtils.isEmpty(list)) {
				return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.CONFIGMAP_NAME_DUPLICATE);
			}
		}
		ConfigFile configFile = ObjConverter.convert(configDetail, ConfigFile.class);
		// 随机生成64位字符串
		configFile.setId(UUIDUtil.getUUID());
		configFile.setCreateTime(DateUtil.timeFormat.format(new Date()));
		configFile.setUser(userName);
		if (!CollectionUtils.isEmpty(list)) {
			// 存在版本号+0.1
			tags = Double.valueOf(list.get(0).getTags()) + Constant.TEMPLATE_TAG_INCREMENT;
		}
		configFile.setTags(decimalFormat.format(tags) + "");
		configFile.setClusterId(configFile.getClusterId());
		if(StringUtils.isNotBlank(configFile.getClusterId())) {
			configFile.setClusterName(clusterService.findClusterById(configFile.getClusterId()).getName());
		}
		// 入库
		configFileMapper.saveConfigFile(configFile);
		JSONObject resultJson = new JSONObject();
		resultJson.put("filename", configDetail.getName());
		resultJson.put("tag", tags);
		return ActionReturnUtil.returnSuccessWithData(resultJson);
	}
	
	/**
	 * update config serviceImpl on 17/03/24.
	 * 
	 * @author gurongyun
	 * @param configDetail
	 *            required
	 * @return ActionReturnUtil
	 */
	@Override
	public ActionReturnUtil updateConfig(ConfigDetailDto configDetail, String userName) throws Exception {

		ConfigFile configFile = ObjConverter.convert(configDetail, ConfigFile.class);
		configFile.setUser(userName);
		// 入库
		configFileMapper.updateConfig(configFile);
		JSONObject resultJson = new JSONObject();
		resultJson.put("filename", configDetail.getName());
		return ActionReturnUtil.returnSuccessWithData(resultJson);
	}

	/**
	 * delete config serviceImpl on 17/03/24.
	 * 
	 * @author gurongyun
	 * @param id
	 *            required
	 * @param projectId
	 *            required
	 * @return ActionReturnUtil
	 */
	@Override
	public void deleteConfig(String id, String projectId) throws Exception {
		Assert.hasText(id);
		Assert.hasText(projectId);
		configFileMapper.deleteConfig(id, projectId);

	}

	/**
	 * find config lists for center serviceImpl on 17/03/24.
	 * 
	 * @author gurongyun
	 * @return ActionReturnUtil
	 */
	@Override
	public ActionReturnUtil searchConfig(String projectId, String clusterId,  String repoName, String keyword) throws Exception {
		JSONArray array = new JSONArray();
		Set<String> clusterIds = null;
		Map<String, Cluster> userCluster = userService.getCurrentUserCluster();
		// 查询项目下所有的配置文件
		if(StringUtils.isBlank(clusterId)){
			clusterIds = userCluster.keySet();
		}else{
			clusterIds = new HashSet<>();
			clusterIds.add(clusterId);
		}
		List<ConfigFile> list = configFileMapper.listConfigSearch(projectId, clusterIds,repoName, keyword);
		if (CollectionUtils.isEmpty(list)) {
			return ActionReturnUtil.returnSuccess();
		} else {
			// 存在-遍历
			for (ConfigFile configFile : list) {
				JSONObject json = new JSONObject();
				json.put("name", configFile.getName());
				json.put("reponame", configFile.getRepoName());
				json.put("tenantId", configFile.getTenantId());
				json.put("projectId",configFile.getProjectId());
				JSONArray configFileTagsArray = new JSONArray();
				// 查询同一配置文件不同版本
				List<ConfigFile> lis = configFileMapper.listConfigByName(configFile.getName(),
						configFile.getProjectId(), configFile.getClusterId(), repoName);
				int count = 0;
				if (lis != null && lis.size() > 0) {
					count = lis.size();
					for (ConfigFile c : lis) {
						JSONObject tags = new JSONObject();
						// 添加版本号Id
						tags.put("id", c.getId());
						tags.put("tag", c.getTags());
						tags.put("path", c.getPath());
						tags.put("item", c.getItems());
						tags.put("desc", c.getDescription());
						tags.put("path", c.getPath());
						tags.put("reponame",c.getRepoName());
						tags.put("clusterId",c.getClusterId());
						tags.put("clusterName",c.getClusterName());
						tags.put("clusterAliasName",userCluster.get(c.getClusterId()).getAliasName());
						tags.put("create_time", c.getCreateTime());

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
	 * @param projectId
	 *            required
	 * @return ActionReturnUtil
	 */
	@Override
	public ActionReturnUtil listConfig(String projectId, String repoName) throws Exception {
		JSONArray array = new JSONArray();
		Map<String, Cluster> userCluster = userService.getCurrentUserCluster();
		// 查询同一repo下的所有配置文件
		List<ConfigFile> lists = configFileMapper.listConfigOverview(projectId, repoName, userCluster.keySet());
		if (CollectionUtils.isEmpty(lists)) {
			return ActionReturnUtil.returnSuccess();
		} else {
			for (int i = 0; i < lists.size(); i++) {
				JSONObject json = new JSONObject();
				json.put("name", lists.get(i).getName());
				json.put("tenantId", lists.get(i).getTenantId());
				json.put("projectId", lists.get(i).getProjectId());
				json.put("repo", lists.get(i).getRepoName());
				JSONArray jsarr=new JSONArray();
				List<ConfigFile> configli=configFileMapper.listConfigByName(lists.get(i).getName(),lists.get(i).getProjectId(),lists.get(i).getClusterId(),lists.get(i).getRepoName());
				if(configli != null && configli.size() > 0){
					for(ConfigFile con : configli){
						JSONObject js=new JSONObject();
						js.put("id", con.getId());
						js.put("tag", con.getTags());
						js.put("item", con.getItems());
						js.put("desc", con.getDescription());
						js.put("path", con.getPath());
						js.put("reponame",con.getRepoName());
						js.put("createTime",con.getCreateTime());
						js.put("clusterId",con.getClusterId());
						js.put("clusterName",con.getClusterName());
						js.put("clusterAliasName",userCluster.get(con.getClusterId()).getAliasName());
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
		// 查找配置文件
		return ActionReturnUtil.returnSuccessWithData(configFileMapper.getConfig(id));
	}

	/**
	 * delete configs service on 17/03/24.
	 * 
	 * @author gurongyun
	 * @param name
	 *            required
	 * @return ActionReturnUtil
	 */
	@Override
	public ActionReturnUtil deleteConfigMap(String name, String projectId, String clusterId) throws Exception {
		Assert.hasText(name);
		Assert.hasText(projectId);
		Assert.hasText(clusterId);
		configFileMapper.deleteConfigByName(name, projectId, clusterId);
		return ActionReturnUtil.returnSuccess();
	}
	
	/**
	 * delete configs service on 17/03/24.
	 *
	 * @author gurongyun
	 * @return ActionReturnUtil
	 */
	@Override
	public ActionReturnUtil deleteConfigByProject(String projectId) throws Exception {
		Assert.hasText(projectId);
		configFileMapper.deleteConfigByProject(projectId);
		return ActionReturnUtil.returnSuccess();
	}

	@Override
	public int deleteByClusterId(String clusterId){
		return configFileMapper.deleteByClusterId(clusterId);
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
	public ActionReturnUtil getLatestConfigMap(String name, String tenant, String repoName) throws Exception {
		return ActionReturnUtil.returnSuccessWithData(configFileMapper.getLatestConfig(name, tenant, repoName));
	}

	@Override
	public ActionReturnUtil checkDuplicateName(String name, String projectId) throws Exception {
		Assert.hasText(name);
		Assert.hasText(projectId);

		// validate name
		List<ConfigFile> configFiles = configFileMapper.listConfigByName(name, projectId,null, null);
		if (CollectionUtils.isEmpty(configFiles)) {
			return ActionReturnUtil.returnSuccessWithData(false);
		} else {
			return ActionReturnUtil.returnSuccessWithData(true);
		}
	}

	public ActionReturnUtil getConfigMapByName(String namespace, String name) throws Exception {
		Assert.hasText(namespace);
		Assert.hasText(name);
		List<String> names = new ArrayList<>();
		if(name.contains(CommonConstant.COMMA)){
			String [] n = name.split(CommonConstant.COMMA);
			names = Arrays.asList(n);
		}else{
			names.add(name);
		}
		List<ConfigMap> list = new ArrayList<>();
		Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
		if(names != null && names.size() > 0){
			for(String n : names){
				K8SClientResponse response = configmapService.doSepcifyConfigmap(namespace, n, cluster);
				if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
					return ActionReturnUtil.returnErrorWithMsg(response.getBody());
				}
				ConfigMap configMap = JsonUtil.jsonToPojo(response.getBody(), ConfigMap.class);
				list.add(configMap);
			}
		}
		return ActionReturnUtil.returnSuccessWithData(list);
	}

	@Override
	public ConfigFile getConfigByNameAndTag(String name, String tag, String projectId, String clusterId){
		return configFileMapper.getConfigByNameAndTag(name, tag, projectId, clusterId);
	}

}
