package com.harmonycloud.api.application;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.dto.business.ContainerFileUploadDto;
import com.harmonycloud.dto.business.PodContainerDto;
import com.harmonycloud.dto.business.Progress;
import com.harmonycloud.service.application.FileUploadToContainerService;
/**
 *
 * @author jmi
 *
 */
@RequestMapping("/container")
@Controller
public class ContainerFileUploadController {

	@Autowired
	private FileUploadToContainerService fileUploadToContainerService;

	@Autowired
	protected  HttpServletRequest request;
	
	private ClassLoader classLoader = this.getClass().getClassLoader();
	
	@RequestMapping(value="/file/uploadTonode", method = RequestMethod.POST)
	@ResponseBody
	public ActionReturnUtil fileUploadToNode(
            @RequestParam(value="file") MultipartFile file,
            @RequestParam(value="pods") String pods,
            @RequestParam(value="namespace") String namespace,
            @RequestParam(value="deployment") String deployment,
            @RequestParam(value="containerFilePath") String containerFilePath) throws Exception {
		ContainerFileUploadDto containerFileUploadDto = new ContainerFileUploadDto();
		containerFileUploadDto.setNamespace(namespace);
		containerFileUploadDto.setContainerFilePath(containerFilePath);
		containerFileUploadDto.setDeployment(deployment);
		List<PodContainerDto> podsDto = JsonUtil.jsonToList(pods, PodContainerDto.class);
		containerFileUploadDto.setPods(podsDto);
		return fileUploadToContainerService.fileUploadToNode(containerFileUploadDto, file);
	}
	
	@RequestMapping(value="/file/upload", method = RequestMethod.POST)
	@ResponseBody
	public ActionReturnUtil fileUpload(@ModelAttribute ContainerFileUploadDto containerFileUploadDto) throws Exception {
		String path = classLoader.getResource("shell/uploadFileToContainer.sh")
				.getPath();
		if (StringUtils.isBlank(path)) {
			return ActionReturnUtil.returnErrorWithMsg("获取文件列表的脚本不存在！");
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
	
	@RequestMapping(value="/file/list", method = RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil fileListByPath(@RequestParam(value="namespace") String namespace, 
			                               @RequestParam(value="containerFilePath") String containerFilePath,
			                               @RequestParam(value="container", required=false) String container,
			                               @RequestParam(value="pod") String pod) throws Exception {
		String path = classLoader.getResource("shell/lsContainerFile.sh")
				.getPath();
		if (StringUtils.isBlank(path)) {
			return ActionReturnUtil.returnErrorWithMsg("获取文件列表的脚本不存在！");
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
	public ActionReturnUtil getUpFilePg(HttpServletRequest request) throws Exception {
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
