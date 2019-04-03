package com.harmonycloud.service.application.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.harmonycloud.dao.application.AppStoreServiceMapper;
import com.harmonycloud.dao.application.bean.AppStoreService;
import com.harmonycloud.service.application.AppStoreServiceService;

@Service
public class AppStoreServiceServiceImpl implements AppStoreServiceService {

	@Autowired
	private AppStoreServiceMapper appStoreServiceMapper;

	/**
	 * 新增应用商店应用与服务模板的关联
	 * @param appService
	 */
	@Override
	public void add(AppStoreService appService) {
		appStoreServiceMapper.insert(appService);
	}

	/**
	 * 根据id删除应用商店应用与服务模板的关联
	 * @param appId
	 */
	@Override
	public void delete(int appId) {
		appStoreServiceMapper.delete(appId);
	}

	/**
	 * 根据id获取应用商店应用与服务模板的关联
	 * @param appId
	 * @return
	 */
	@Override
	public List<AppStoreService> list(int appId) {
		return appStoreServiceMapper.listByAppId(appId);
	}

}
