package com.harmonycloud.api.application;


import javax.servlet.http.HttpServletRequest;

import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.application.ContainerFileUploadDto;
import com.harmonycloud.dto.application.Progress;
import com.harmonycloud.service.application.FileUploadToContainerService;
/**
 *
 * @author jmi
 *
 */
@RequestMapping("/tenants/{tenantId}/projects/{projectId}/deploys/{deployName}/container")
@Controller
public class ContainerFileUploadController {

	@Autowired
	private FileUploadToContainerService fileUploadToContainerService;

	@Autowired
	protected  HttpServletRequest request;
	
	private ClassLoader classLoader = this.getClass().getClassLoader();
	
	@RequestMapping(value="/file/uploadToNode", method = RequestMethod.POST)
	@ResponseBody
	public ActionReturnUtil uploadFileToNode(
            @RequestParam(value="file") MultipartFile file,
            @RequestParam(value="pods") String pods,
            @RequestParam(value="namespace") String namespace,
            @PathVariable(value="deployName") String deployment,
            @RequestParam(value="containerFilePath") String containerFilePath) throws Exception {
		return fileUploadToContainerService.fileUploadToNode(pods, namespace, deployment, containerFilePath, file);
	}
	
	@RequestMapping(value="/file/upload", method = RequestMethod.POST)
	@ResponseBody
	public ActionReturnUtil uploadFile(@ModelAttribute ContainerFileUploadDto containerFileUploadDto) throws Exception {
		String path = classLoader.getResource("shell/uploadFileToContainer.sh")
				.getPath();
		if (StringUtils.isBlank(path)) {
			throw new MarsRuntimeException(ErrorCodeMessage.SCRIPT_NOT_EXIST);
		}
		return fileUploadToContainerService.fileUploadToContainer(containerFileUploadDto, path);
	}
	
	@RequestMapping(value="/file/upload/status", method = RequestMethod.POST)
	@ResponseBody
	public ActionReturnUtil queryFileUploadPhase(@ModelAttribute ContainerFileUploadDto containerFileUploadDto) throws Exception {
		
		return fileUploadToContainerService.queryUploadPhase(containerFileUploadDto);
	}
	
	@RequestMapping(value="/file/upload/history", method = RequestMethod.POST)
	@ResponseBody
	public ActionReturnUtil getFileUploadHistory(@ModelAttribute ContainerFileUploadDto containerFileUploadDto) throws Exception {
		
		return fileUploadToContainerService.queryUploadHistory(containerFileUploadDto);
	}
	
	@RequestMapping(value="/files", method = RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil listFileByPath(@RequestParam(value="namespace") String namespace,
			                               @RequestParam(value="containerFilePath") String containerFilePath,
			                               @RequestParam(value="container", required=false) String container,
			                               @RequestParam(value="pod") String pod) throws Exception {
		String path = classLoader.getResource("shell/lsContainerFile.sh")
				.getPath();
		if (StringUtils.isBlank(path)) {
			return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.SCRIPT_NOT_EXIST);
		}
		return fileUploadToContainerService.lsContainerFile(namespace, containerFilePath, container, pod, path);
	}
	
	@RequestMapping(value="/file/upload/record", method = RequestMethod.DELETE)
	@ResponseBody
	public ActionReturnUtil deleteFile(@ModelAttribute ContainerFileUploadDto containerFileUploadDto) throws Exception {
		
		return fileUploadToContainerService.deleteFile(containerFileUploadDto);
	}
	
	@RequestMapping(value = "/file/upload/progress", method = RequestMethod.GET )
	@ResponseBody
	public ActionReturnUtil getUploadFileProgress(HttpServletRequest request) throws Exception {
		Progress status = (Progress) request.getSession().getAttribute("upload_ps");
		return ActionReturnUtil.returnSuccessWithData(status);
	}
	
	@RequestMapping(value="/file/maxsize", method = RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil getFileMaxSize() throws Exception {
		return fileUploadToContainerService.getFileMaxSizeConfig();
	}
	
	@RequestMapping(value="/file/maxsize", method = RequestMethod.POST)
	@ResponseBody
	public ActionReturnUtil updateFileMaxSize(@RequestParam(value="maxSize") String maxSize) throws Exception {
		
		return fileUploadToContainerService.addFileMaxSizeConfig(maxSize);
	}

}
