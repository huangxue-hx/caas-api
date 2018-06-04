package com.harmonycloud.service.application;

import org.springframework.web.multipart.MultipartFile;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.application.ContainerFileUploadDto;

/**
 * created by 2017/7/13
 * @author jmi
 *
 */
public interface FileUploadToContainerService {
	
	ActionReturnUtil fileUploadToNode(String pods, String namespace, String deployment, String containerFilePath, MultipartFile file) throws Exception;
	
	ActionReturnUtil fileUploadToContainer(ContainerFileUploadDto containerFileUpload, String shellPath) throws Exception;
	
	ActionReturnUtil queryUploadPhase(ContainerFileUploadDto containerFileUpload) throws Exception;
	
	ActionReturnUtil queryUploadHistory(ContainerFileUploadDto containerFileUpload) throws Exception;
	
	ActionReturnUtil lsContainerFile(String namespace, String containerFilePath, String container, String pod, String shellPath) throws Exception;
	
	ActionReturnUtil deleteFile(ContainerFileUploadDto containerFileUpload) throws Exception;
	
	ActionReturnUtil addFileMaxSizeConfig(String maxSize) throws Exception;
	
	ActionReturnUtil getFileMaxSizeConfig() throws Exception;

	void deleteUploadRecord(String namespace, String deployment) throws Exception;
}
