package com.harmonycloud.service.application.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.enumm.ServiceTypeEnum;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.date.DateStyle;
import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.dto.application.*;
import com.harmonycloud.k8s.bean.StatefulSet;
import com.harmonycloud.service.platform.convert.K8sResultConvert;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.yaml.snakeyaml.Yaml;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.dao.application.AppStoreServiceMapper;
import com.harmonycloud.dao.application.bean.AppStore;
import com.harmonycloud.dao.application.bean.ServiceTemplates;
import com.harmonycloud.k8s.bean.Deployment;
import com.harmonycloud.service.application.AppStoreService;
import com.harmonycloud.service.application.AppStoreServiceService;
import com.harmonycloud.service.application.ApplicationService;
import com.harmonycloud.service.application.ServiceService;
import com.harmonycloud.service.application.Util.TemplateToYamlUtil;
import com.harmonycloud.service.platform.constant.Constant;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import javax.servlet.http.HttpSession;

@Service
@Transactional(rollbackFor = Exception.class)
public class AppStoreServiceImpl implements AppStoreService{
	private static final Logger logger = LoggerFactory.getLogger(AppStoreServiceImpl.class);
	@Autowired
	private com.harmonycloud.dao.application.AppStoreMapper appStoreMapper;

	@Autowired
	private ServiceService serviceService;

	@Autowired
	private ApplicationService applicationService;

	@Autowired
	private AppStoreServiceService appStoreServiceService;

	@Autowired
	private AppStoreServiceMapper appStoreServiceMapper;

	@Autowired
	private HttpSession session;

	private static String IMAGE_PATH = "/appimages/";

	@Value("#{propertiesReader['upload.path']}")
	private String uploadPath;

	/**
	 * 删除应用商店应用
	 * @param id
	 * @return
	 * @throws Exception
	 */
	@Override
	public ActionReturnUtil deleteAppStore(Integer id) throws Exception {
		try {
			appStoreMapper.delete(id);
			serviceService.deleteServiceTemplateByAppId(id);
			appStoreServiceService.delete(id);
		}catch (Exception e){
			logger.error("删除应用商店失败，id:{}", id, e);
			throw new MarsRuntimeException(ErrorCodeMessage.DELETE_FAIL);
		}
		return ActionReturnUtil.returnSuccess();
	}

	/**
	 * 创建应用商店模板
	 * @param app
	 * @param username
	 * @return
	 * @throws Exception
	 */
	@Override
	public ActionReturnUtil addAppStore(AppStoreDto app, String username) throws Exception {
		//验证重名
		if(checkName(app.getName())) {
			throw new MarsRuntimeException(ErrorCodeMessage.NAME_EXIST, app.getName(), true);
		}
		try {
			AppStore appStore = new AppStore();
			appStore.setName(app.getName());
			appStore.setImage(app.getImage());
			appStore.setTag(app.getTag());
			appStore.setDetails(app.getDetails() != null ? app.getDetails() : app.getDesc());
			appStore.setType(app.getType());
			appStore.setUser(username);
			appStore.setCreateTime(new Date());
			appStoreMapper.insert(appStore);
			int appId = appStore.getId();
			this.addServiceTemplateAndMapping(appId, app.getServiceList(), username);
		}catch (Exception e){
			logger.error("新增应用商店失败，name:{}", app.getName(), e);
			throw new MarsRuntimeException(ErrorCodeMessage.CREATE_FAIL, app.getName(), true);
		}
		return ActionReturnUtil.returnSuccess();
	}

	/**
	 * 根据名称和tag获取应用商店应用详情
	 * @param name
	 * @param tag
	 * @return
	 * @throws Exception
	 */
	@Override
	public AppStoreDto getAppStore(String name, String tag) throws Exception {
		AppStoreDto app = new AppStoreDto();
		AppStore appStore = appStoreMapper.selectByNameAndTag(name, tag);
		if(appStore == null || StringUtils.isEmpty(appStore.getName())) {
			return null;
		}
		app.setId(appStore.getId());
		app.setDetails(appStore.getDetails());
		app.setDesc(appStore.getDetails());
		app.setName(appStore.getName());
		app.setImage(appStore.getImage());
		app.setTag(appStore.getTag());
		app.setUser(appStore.getUser());
		app.setType(appStore.getType());
		app.setCreateTime(appStore.getCreateTime());
		app.setUpdateTime(appStore.getUpdateTime());
		List<ServiceTemplates> list= serviceService.listServiceTemplateByAppId(appStore.getId());
		List<Object> objectListToyaml = new ArrayList<>();
		JSONArray array = new JSONArray();
		if(list != null && list.size() > 0) {
			for(ServiceTemplates serviceTemplates : list) {
				if (serviceTemplates != null) {
					JSONObject json = new JSONObject();
					json.put("id", serviceTemplates.getId());
					json.put("name", serviceTemplates.getName());
					if (serviceTemplates.getTag() != null) {
						json.put("tag", serviceTemplates.getTag());
					} else {
						json.put("tag", "");
					}
					json.put("isExternal", serviceTemplates.getFlag());
					if (serviceTemplates.getNodeSelector() != null) {
						json.put("nodeSelector", serviceTemplates.getNodeSelector());
					} else {
						json.put("nodeSelector", "");
					}
					json.put("serviceType", serviceTemplates.getServiceType());
					String content = (serviceTemplates.getDeploymentContent() != null) ? serviceTemplates.getDeploymentContent().toString().replace("null", "\"\"") : "";
					switch(ServiceTypeEnum.valueOf(serviceTemplates.getServiceType())){
						case DEPLOYMENT:
							json.put("deployment", content);
							break;
						case STATEFULSET:
							json.put("statefulSet", content);
							break;
					}
					objectListToyaml.addAll(applicationService.convertObjectListToYaml(serviceTemplates, content));

					json.put("ingress", (serviceTemplates.getIngressContent() != null) ? serviceTemplates.getIngressContent().toString().replace("null", "\"\"") : "");
					json.put("imageList", (serviceTemplates.getImageList() != null) ? serviceTemplates.getImageList() : "");
					json.put("user", (serviceTemplates.getUser() != null) ? serviceTemplates.getUser() : "");
					json.put("tenant", (serviceTemplates.getTenant() != null) ? serviceTemplates.getTenant() : "");
					json.put("details", (serviceTemplates.getDetails() != null) ? serviceTemplates.getDetails() : "");
					array.add(json);
				}
			}
		}
		app.setServicelist(array);
		Yaml yaml = new Yaml();
		if (objectListToyaml != null){

			String yamlc = applicationService.convertYaml(yaml.dumpAsMap(objectListToyaml));

			app.setYaml(yamlc);
		}
		return app;
	}

	/**
	 * 获取应用商店应用列表
	 * @param name
	 * @return
	 * @throws Exception
	 */
	@Override
	public List<AppStoreDto> listAppStore(String name) throws Exception {
		List<AppStore> list;
		if(StringUtils.isEmpty(name)) {
			list = appStoreMapper.list();
		}else {
			list = appStoreMapper.listByName(name);
		}
		List<AppStoreDto> apps = new ArrayList<AppStoreDto>();
		if(CollectionUtils.isNotEmpty(list)) {
			for(AppStore app : list) {
				AppStoreDto dto = new AppStoreDto();
				dto.setName(app.getName());
				dto.setCreateTime(app.getCreateTime());
				dto.setDetails(app.getDetails());
				dto.setDesc(app.getDetails());
				dto.setImage(app.getImage());
				dto.setType(app.getType());
				dto.setUser(app.getUser());
				dto.setUpdateTime(app.getUpdateTime());
				List<TagDto> tagsList = new ArrayList<TagDto>();
				TagDto tags = new TagDto();
				tags.setId(app.getId());
				tags.setTag(app.getTag());
				tags.setUser(app.getUser());
				tags.setCreateTime(DateUtil.DateToString(app.getCreateTime(), DateStyle.YYYY_MM_DD_HH_MM_SS));
				tagsList.add(tags);
				dto.setTags(tagsList);
				apps.add(dto);
			}
		}
		return apps;
	}

	/**
	 * 修改应用商店应用
	 * @param app
	 * @param username
	 * @return
	 * @throws Exception
	 */
	@Override
	public ActionReturnUtil updateAppStore(AppStoreDto app, String username) throws Exception {
		try {
			AppStore appStore = new AppStore();
			appStore.setId(app.getId());
			appStore.setName(app.getName());
			appStore.setImage(app.getImage());
			appStore.setTag(app.getTag());
			appStore.setDetails(app.getDetails() != null ? app.getDetails() : app.getDesc());
			appStore.setType(app.getType());
			appStore.setUser(username);
			appStore.setCreateTime(app.getCreateTime());
			appStore.setUpdateTime(new Date());
			appStoreMapper.update(appStore);
			int appId = appStore.getId();
			serviceService.deleteServiceTemplateByAppId(appId);
			appStoreServiceMapper.delete(appId);
			this.addServiceTemplateAndMapping(appId, app.getServiceList(), username);
		}catch (Exception e){
			logger.error("更新应用商店失败，name:{}", app.getName(), e);
			throw new MarsRuntimeException(ErrorCodeMessage.UPDATE_FAIL, app.getName(), true);
		}
		return ActionReturnUtil.returnSuccess();
	}

	/**
	 * 校验名称
	 * @param name
	 * @return
	 * @throws Exception
	 */
	@Override
	public Boolean checkName(String name)throws Exception {
		List<AppStore> list = appStoreMapper.listApps(name);
		if(list == null || list.size() < 1) {
			return false;
		}
		return true;
	}

	/**
	 * 上传图片
	 * @param file
	 * @return
	 */
	@Override
	public String uploadImage(MultipartFile file) throws Exception {
		byte[] data = IOUtils.toByteArray(file.getInputStream());
		String imageDirectory = uploadPath + File.separator +IMAGE_PATH;
		String fileName = new Date().getTime() +file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(CommonConstant.DOT));
		File o = new File(imageDirectory, fileName);
		IOUtils.write(data, new FileOutputStream(o));
		return o.getName();
	}

	/**
	 * 获取应用商店某应用的版本
	 * @param name
	 * @return
	 * @throws Exception
	 */
	@Override
	public ActionReturnUtil listAppStoreTags(String name) throws Exception {
		JSONObject json = new JSONObject();
		json.put("name", name);
		List<AppStore> list = appStoreMapper.listApps(name);
		JSONArray array = new JSONArray();
		if(list != null && list.size() > 0) {
			for(AppStore yaml : list) {
				JSONObject js = new JSONObject();
				js.put("id", yaml.getId());
				js.put("tag", yaml.getTag());
				array.add(js);
			}
		}
		json.put("tags", array);
		return ActionReturnUtil.returnSuccessWithData(json);
	}


	/**
	 * 增加服务模板和关联
	 * @param appId
	 * @param serviceTemplateList
	 * @param username
	 * @throws Exception
	 */
	private void addServiceTemplateAndMapping(Integer appId, List<ServiceTemplateDto> serviceTemplateList, String username) throws Exception {
		for (ServiceTemplateDto serviceTemplate : serviceTemplateList) {
			ActionReturnUtil res = serviceService.saveServiceTemplate(serviceTemplate, username, Constant.TEMPLATE_STATUS_DELETE);
			if(res.isSuccess()){
				JSONObject json = (JSONObject) res.get("data");
				//增加应用与服务模板关联
				com.harmonycloud.dao.application.bean.AppStoreService appService = new com.harmonycloud.dao.application.bean.AppStoreService();
				appService.setAppId(appId);
				appService.setServiceId(Integer.parseInt(json.get(serviceTemplate.getName()).toString()));
				appStoreServiceService.add(appService);
			}
		}
	}
}
