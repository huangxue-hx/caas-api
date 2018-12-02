package com.harmonycloud.service.application;

import java.util.List;
import com.harmonycloud.dao.application.bean.AppStoreService;

public interface AppStoreServiceService {

	/**
	 * 新增应用商店应用与服务模板的关联
	 * @param appService
	 */
	void add(AppStoreService appService);

	/**
	 * 根据id删除应用商店应用与服务模板的关联
	 * @param appId
	 */
	void delete(int appId);

	/**
	 * 根据id获取应用商店应用与服务模板的关联
	 * @param appId
	 * @return
	 */
	List<AppStoreService> list(int appId);

}
