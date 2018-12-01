package com.harmonycloud.service.application;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.application.AppStoreDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.util.List;

public interface AppStoreService {
	/**
	 * 删除应用商店应用
	 * @param id
	 * @return
	 * @throws Exception
	 */
	ActionReturnUtil deleteAppStore(Integer id) throws Exception;

	/**
	 * 增加应用商店应用
	 * @param app
	 * @param username
	 * @return
	 * @throws Exception
	 */
	ActionReturnUtil addAppStore(AppStoreDto app, String username) throws Exception;

	/**
	 * 获取应用商店应用详情
	 * @param name
	 * @param tag
	 * @return
	 * @throws Exception
	 */
	AppStoreDto getAppStore(String name, String tag) throws Exception;

	/**
	 * 获取应用商店列表
	 * @param name
	 * @return
	 * @throws Exception
	 */
	List<AppStoreDto> listAppStore(String name) throws Exception;

	/**
	 * 根据名称获取版本
	 * @param name
	 * @return
	 * @throws Exception
	 */
	ActionReturnUtil listAppStoreTags(String name) throws Exception;

	/**
	 * 更新应用商店
	 * @param app
	 * @param username
	 * @return
	 * @throws Exception
	 */
	ActionReturnUtil updateAppStore(AppStoreDto app, String username) throws Exception;

	/**
	 * 校验名称
	 * @param name
	 * @return
	 * @throws Exception
	 */
	Boolean checkName(String name)throws Exception;

	/**
	 * 上传图片
	 * @param file
	 */
    String uploadImage(MultipartFile file) throws FileNotFoundException, Exception;
}
