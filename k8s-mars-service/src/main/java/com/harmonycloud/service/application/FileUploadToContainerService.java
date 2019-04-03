package com.harmonycloud.service.application;

import org.springframework.web.multipart.MultipartFile;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.application.ContainerFileUploadDto;

import java.util.List;

/**
 * created by 2017/7/13
 * @author jmi
 *
 */
public interface FileUploadToContainerService {

	/**
	 * 上传多个文件
	 */
	ActionReturnUtil fileUploadToNode(String pods, String namespace, String deployment, String containerFilePath, MultipartFile[] files) throws Exception;

	/**
	 * 上传单个文件, 文件上传到平台webapi服务的容器里
	 */
	ActionReturnUtil fileUploadToNode(String pods, String namespace, String deployment, String containerFilePath, MultipartFile file) throws Exception;

	/**
	 * 将平台webapi服务的容器里的文件拷贝到目标服务容器里
	 */
	ActionReturnUtil fileUploadToContainer(ContainerFileUploadDto containerFileUpload, String shellPath) throws Exception;
	
	ActionReturnUtil queryUploadPhase(ContainerFileUploadDto containerFileUpload) throws Exception;
	
	ActionReturnUtil queryUploadHistory(ContainerFileUploadDto containerFileUpload) throws Exception;

	/**
	 * @param containers [[pod,container],[pod,container]...]
	 * @throws Exception
	 */
	ActionReturnUtil lsContainerFile(String namespace, String containerFilePath, String containers, String shellPath) throws Exception;
	
	ActionReturnUtil deleteFile(ContainerFileUploadDto containerFileUpload) throws Exception;
	
	ActionReturnUtil addFileMaxSizeConfig(String maxSize) throws Exception;
	
	ActionReturnUtil getFileMaxSizeConfig() throws Exception;

	void deleteUploadRecord(String namespace, String deployment) throws Exception;
}
