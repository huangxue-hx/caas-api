 package com.harmonycloud.service.application.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import javax.servlet.http.HttpSession;

import com.alibaba.druid.support.json.JSONUtils;
import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.AssertUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.service.tenant.NamespaceLocalService;
import com.harmonycloud.service.user.UserService;
import net.sf.json.JSONArray;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.ESFactory;
import com.harmonycloud.dao.application.FileUploadContainerMapper;
import com.harmonycloud.dao.application.bean.FileUploadContainer;
import com.harmonycloud.dao.application.bean.FileUploadContainerExample;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.dao.system.SystemConfigMapper;
import com.harmonycloud.dao.system.bean.SystemConfig;
import com.harmonycloud.dto.application.ContainerFileUploadDto;
import com.harmonycloud.dto.application.PodContainerDto;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.service.application.FileUploadToContainerService;
import com.harmonycloud.service.platform.constant.Constant;

/**
 * 
 * @author jmi
 *
 */
@Service
public class FileUploadToContainerServiceImpl implements FileUploadToContainerService {

	@Autowired
	private FileUploadContainerMapper fileUploadContainerMapper;

	@Autowired
	private UserService userService;

	@Value("#{propertiesReader['upload.path']}")
	private String uploadPath;
	
	@Autowired
    private SystemConfigMapper systemConfigMapper;

	@Autowired
	private NamespaceLocalService namespaceLocalService;

	private final static Logger logger = LoggerFactory.getLogger(FileUploadToContainerServiceImpl.class);

	@Override
	public ActionReturnUtil fileUploadToNode(String pods, String namespace, String deployment,
											 String containerFilePath, MultipartFile[] files) throws Exception{
		List<Integer> uploadIds = new ArrayList<>();
		for(MultipartFile file : files){
			ActionReturnUtil response = fileUploadToNode(pods, namespace, deployment, containerFilePath, file);
			if(response.isSuccess() && response.getData() != null){
				uploadIds.addAll((List)response.getData());
			}
		}
		return ActionReturnUtil.returnSuccessWithData(uploadIds);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public ActionReturnUtil fileUploadToNode(String pods, String namespace, String deployment, String containerFilePath, MultipartFile file)
			throws Exception {
		if (StringUtils.isBlank(pods) || StringUtils.isBlank(namespace) || StringUtils.isBlank(deployment) || StringUtils.isBlank(containerFilePath)) {
			throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
		}
		ContainerFileUploadDto containerFileUpload = new ContainerFileUploadDto();
		containerFileUpload.setNamespace(namespace);
		containerFileUpload.setContainerFilePath(containerFilePath);
		containerFileUpload.setDeployment(deployment);
		List<PodContainerDto> podsDto = JsonUtil.jsonToList(pods, PodContainerDto.class);
		containerFileUpload.setPods(podsDto);
		List<PodContainerDto> podList = containerFileUpload.getPods();
		String fileName = file.getOriginalFilename();
		String path = getLocalFilePath(namespace, deployment);
		File dirFile = new File(path);
		String newFileName = fileName;
		
		//对含有空格的文件名进行字符替换
		if (fileName.indexOf(" ") != -1) {
			newFileName = fileName.replaceAll(" ", Constant.SPACE_TRANS);
		}
		File newFile = new File(path + "/" + newFileName);
		Long userId = userService.getCurrentUserId();
		List<Integer> uploadIds = new ArrayList<Integer>();
		for (PodContainerDto pDto : podList) {

			// 查询数据库是否含有相同的记录
			FileUploadContainerExample example = new FileUploadContainerExample();
			FileUploadContainer fUploadContainer = new FileUploadContainer();
			fUploadContainer.setContainerFilePath(containerFileUpload.getContainerFilePath());
			fUploadContainer.setNamespace(containerFileUpload.getNamespace());
			fUploadContainer.setDeployment(containerFileUpload.getDeployment());
			fUploadContainer.setFileName(newFileName);
			fUploadContainer.setPhase(1);
			fUploadContainer.setPod(pDto.getName());
			fUploadContainer.setUserId(userId);
			fUploadContainer.setStatus("doing");
			fUploadContainer.setCreateTime(new Date());
			Integer id = 0;
			List<String> containers = pDto.getContainer();
			if (containers != null && containers.size() > 0) {
				for (String c : containers) {
					example.createCriteria().andNamespaceEqualTo(containerFileUpload.getNamespace())
							.andUserIdEqualTo(userId).andDeploymentEqualTo(containerFileUpload.getDeployment())
							.andPodEqualTo(pDto.getName())
							.andContainerFilePathEqualTo(containerFileUpload.getContainerFilePath())
							.andFileNameEqualTo(newFileName).andContainerEqualTo(c);
					List<FileUploadContainer> uploadList = fileUploadContainerMapper.selectByExample(example);
					if (uploadList != null && uploadList.size() > 0) {
						FileUploadContainer tmp = uploadList.get(0);
						tmp.setUpdateTime(new Date());
						tmp.setPhase(1);
						tmp.setStatus("doing");
						fileUploadContainerMapper.updateByPrimaryKeySelective(tmp);
						uploadIds.add(tmp.getId());
					} else {
						fUploadContainer.setContainer(c);
						fileUploadContainerMapper.insertSelective(fUploadContainer);
						id = fUploadContainer.getId();
						fUploadContainer.setId(0);
						uploadIds.add(id);
					}
					example.clear();
				}
			} else {
				example.createCriteria().andNamespaceEqualTo(containerFileUpload.getNamespace())
						.andUserIdEqualTo(userId).andDeploymentEqualTo(containerFileUpload.getDeployment())
						.andPodEqualTo(pDto.getName())
						.andContainerFilePathEqualTo(containerFileUpload.getContainerFilePath())
						.andFileNameEqualTo(newFileName);
				List<FileUploadContainer> uploadList = fileUploadContainerMapper.selectByExample(example);
				if (uploadList != null && uploadList.size() > 0) {
					FileUploadContainer tmp = uploadList.get(0);
					tmp.setUpdateTime(new Date());
					tmp.setPhase(1);
					tmp.setStatus("doing");
					fileUploadContainerMapper.updateByPrimaryKeySelective(tmp);
					uploadIds.add(tmp.getId());
				} else {
					fileUploadContainerMapper.insertSelective(fUploadContainer);
					id = fUploadContainer.getId();
					uploadIds.add(id);
				}
				example.clear();
			}
		}

		if (uploadIds != null && uploadIds.size() > 0) {
			logger.debug("dirFileExist:" + dirFile.exists());
			if (!dirFile.exists()) {
				dirFile.mkdirs();
			}
			try {
				file.transferTo(newFile);
				logger.debug("newFileIsExist:" + newFile.exists());
				for (Integer tId : uploadIds) {

					// 上传成功更新上传状态
					FileUploadContainer newFileUploadC = fileUploadContainerMapper.selectByPrimaryKey(tId);
					newFileUploadC.setStatus("success");
					fileUploadContainerMapper.updateByPrimaryKeySelective(newFileUploadC);
				}
			} catch (Exception e) {
				for (Integer tId : uploadIds) {

					// 上传失败更新上传状态
					final FileUploadContainer newFileUploadC = fileUploadContainerMapper.selectByPrimaryKey(tId);
					newFileUploadC.setStatus("failed");
					newFileUploadC.setErrMsg(e.getMessage());
					fileUploadContainerMapper.updateByPrimaryKeySelective(newFileUploadC);
				}
				logger.error(e.getMessage(), e);
			}
		}
		return ActionReturnUtil.returnSuccessWithData(uploadIds);
	}

	@Override
	public ActionReturnUtil fileUploadToContainer(ContainerFileUploadDto containerFileUpload, String shellPath)
			throws Exception {
		AssertUtil.notBlank(containerFileUpload.getNamespace(), DictEnum.NAMESPACE);
		AssertUtil.notBlank(containerFileUpload.getDeployment(), DictEnum.DEPLOYMENT_NAME);

		//获取集群
		Cluster cluster = namespaceLocalService.getClusterByNamespaceName(containerFileUpload.getNamespace());
		if (Objects.isNull(cluster)) {
			return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.CLUSTER_NOT_FOUND);
		}
		List<PodContainerDto> podList = containerFileUpload.getPods();
		final List<Integer> uploadIds = containerFileUpload.getUploadIdList();
		if (podList == null && uploadIds == null) {
			throw new MarsRuntimeException(ErrorCodeMessage.POD_IS_BLANK);
		}
		final String sPath = shellPath;
		final String localPath = this.getLocalFilePath(containerFileUpload.getNamespace(), containerFileUpload.getDeployment());
		//获取token
		final String token = cluster.getMachineToken();
		final String server = cluster.getProtocol() + "://" + cluster.getHost() + ":" + cluster.getPort();
        final CountDownLatch begin = new CountDownLatch(uploadIds.size());
		for (int i = 0; i < uploadIds.size(); i++) {
			final Integer id = uploadIds.get(i);
			ESFactory.executor.execute(new Runnable() {

				@Override
				public void run() {
					try {
						doUploadFile(localPath, sPath, id, token, server);
						begin.countDown();
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}

				}
			});
		}
		begin.await();
		//删除该服务的临时文件目录
		File f = new File(localPath);
		if (f.exists()) {
			f.delete();
		}
		// ESFactory.executor.shutdown();
		return ActionReturnUtil.returnSuccess();
	}

	@Override
	public ActionReturnUtil queryUploadPhase(ContainerFileUploadDto containerFileUpload) throws Exception {
		List<PodContainerDto> podList = containerFileUpload.getPods();
		List<Integer> uploadIds = containerFileUpload.getUploadIdList();
		if (podList == null && uploadIds == null) {
			throw new MarsRuntimeException(ErrorCodeMessage.POD_IS_BLANK);
		}
		List<FileUploadContainer> records = new ArrayList<FileUploadContainer>();
		for (Integer id : uploadIds) {
			FileUploadContainer fContainer = fileUploadContainerMapper.selectByPrimaryKey(id);
			records.add(fContainer);
		}

		return ActionReturnUtil.returnSuccessWithData(records);
	}

	@Override
	public ActionReturnUtil queryUploadHistory(ContainerFileUploadDto containerFileUpload) throws Exception {
		// select file_upload_container表
		Long userId = userService.getCurrentUserId();
		List<PodContainerDto> podList = containerFileUpload.getPods();
		if (podList == null) {
			throw new MarsRuntimeException(ErrorCodeMessage.POD_IS_BLANK);
		}
		List<FileUploadContainer> result = new ArrayList<FileUploadContainer>();
		for (PodContainerDto pContainerDto : podList) {
			FileUploadContainerExample fucExample = new FileUploadContainerExample();
			fucExample.createCriteria().andNamespaceEqualTo(containerFileUpload.getNamespace())
					.andDeploymentEqualTo(containerFileUpload.getDeployment()).andUserIdEqualTo(userId)
					.andPodEqualTo(pContainerDto.getName());
			List<FileUploadContainer> records = fileUploadContainerMapper.selectByExample(fucExample);
			
			//将带有空格的文件名称
			for (FileUploadContainer fContainer : records) {
				if (fContainer.getFileName().indexOf(Constant.SPACE_TRANS) != -1) {
					String fileName = fContainer.getFileName().replaceAll(Constant.SPACE_TRANS, " ");
					fContainer.setFileName(fileName);
				}
			}
			
			result.addAll(records);
		}

		//按照时间排序
		result.sort((lrd, rrd) -> {
	        if (lrd.getUpdateTime() != null && rrd.getUpdateTime() != null) {
	            return rrd.getUpdateTime().compareTo(lrd.getUpdateTime());
	        } else if (lrd.getUpdateTime() != null && rrd.getUpdateTime() == null){
	            return rrd.getCreateTime().compareTo(lrd.getUpdateTime());
	        } else if (lrd.getUpdateTime() == null && rrd.getUpdateTime() != null){
	        	return rrd.getUpdateTime().compareTo(lrd.getCreateTime());
	        } else {
	        	return rrd.getCreateTime().compareTo(lrd.getCreateTime());
	        }
	    });

		return ActionReturnUtil.returnSuccessWithData(result);
	}

	@Override
	public ActionReturnUtil lsContainerFile(String namespace, String containerFilePath, String containers,
			String shellPath) throws Exception {
		if (StringUtils.isBlank(namespace) || StringUtils.isBlank(containerFilePath) || StringUtils.isBlank(containers)) {
			throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
		}
		//获取集群
		Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
		if (Objects.isNull(cluster)) {
			return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.CLUSTER_NOT_FOUND);
		}
		//获取token
		String token = cluster.getMachineToken();
		String server = cluster.getProtocol() + "://" + cluster.getHost() + ":" + cluster.getPort();

		List<List<String>> containersList = (List<List<String>>) JSONUtils.parse(containers);
		if (CollectionUtils.isEmpty(containersList)) {
			throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
		}
		List<Map<String, String>> files = new ArrayList<Map<String, String>>();
		for (List<String> containerLoc : containersList) {
			String res = "";
			String pod = containerLoc.get(0);
			String container = containerLoc.get(1) == null ? "" : containerLoc.get(1);
			ProcessBuilder proc = new ProcessBuilder("bash", shellPath, pod, containerFilePath, namespace, token, server);
			if (StringUtils.isNotBlank(container)) {
				proc = new ProcessBuilder("bash", shellPath, pod, containerFilePath, namespace, token, server, container);
			}
			logger.debug("执行命令参数：{}",proc.command());

			Process p = proc.start();
			BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			StringBuilder resError = new StringBuilder();
			while ((res = stdError.readLine()) != null) {
				logger.error("执行容器文件目录脚本错误：" + res);
				resError.append(res);
			}
			if (StringUtils.isNotBlank(resError.toString())) {
				return ActionReturnUtil.returnErrorWithData(resError.toString());
			}
			while ((res = stdInput.readLine()) != null) {
				Map<String, String> file = new HashMap<String, String>();

				//将替换的字符重新转成空格
				if (res.indexOf(Constant.SPACE_TRANS) != -1) {
					res = res.replaceAll(Constant.SPACE_TRANS, " ");
				}
				file.put("name", res);
				files.add(file);
				logger.debug("文件名：" + res);
			}
		}
		return ActionReturnUtil.returnSuccessWithData(files);
	}

	@Override
	public ActionReturnUtil deleteFile(ContainerFileUploadDto containerFileUpload) throws Exception {
		Long userId = userService.getCurrentUserId();
		FileUploadContainerExample fucExample = new FileUploadContainerExample();
		fucExample.createCriteria().andNamespaceEqualTo(containerFileUpload.getNamespace())
				.andDeploymentEqualTo(containerFileUpload.getDeployment()).andUserIdEqualTo(userId);
		List<FileUploadContainer> deleteList = fileUploadContainerMapper.selectByExample(fucExample);
		if ( deleteList != null && !deleteList.isEmpty()) {
			// 删除文件
			for (FileUploadContainer filec : deleteList) {
				String path = this.getLocalFilePath(containerFileUpload.getNamespace(), containerFileUpload.getDeployment())
						+ "/" + filec.getFileName();
				File file = new File(path);
				if (file.isFile() && file.exists()) {
					file.delete();
				}
			}
		}
		fileUploadContainerMapper.deleteByExample(fucExample);
		return ActionReturnUtil.returnSuccess();
	}
	

	@Override
	public ActionReturnUtil addFileMaxSizeConfig(String maxSize) throws Exception {
		String username = userService.getCurrentUsername();
		SystemConfig fileConfig = systemConfigMapper.findByConfigName(CommonConstant.FILE_MAX_SIZE);
		boolean flagIsAdd = false;
		if (fileConfig == null) {
			flagIsAdd = true;
			fileConfig = new SystemConfig();
			fileConfig.setCreateTime(new Date());
			fileConfig.setCreateUser(username);
		}
		fileConfig.setConfigName(CommonConstant.FILE_MAX_SIZE);
		fileConfig.setConfigValue(maxSize);
		fileConfig.setConfigType(CommonConstant.CONFIG_TYPE_FILE);
		fileConfig.setUpdateTime(new Date());
		fileConfig.setUpdateUser(username);
		if(flagIsAdd) {
            this.systemConfigMapper.addSystemConfig(fileConfig);
        } else {
            this.systemConfigMapper.updateSystemConfig(fileConfig);
        }
		return ActionReturnUtil.returnSuccess();
	}
	
	@Override
	public ActionReturnUtil getFileMaxSizeConfig() throws Exception {
		
		SystemConfig systemConfig = systemConfigMapper.findByConfigName(CommonConstant.FILE_MAX_SIZE);
		return ActionReturnUtil.returnSuccessWithData(systemConfig);
	}

	private void doUploadFile(String localPath, String shellPath, Integer id, String token, String server)
			throws Exception {
		FileUploadContainer fileUpload = fileUploadContainerMapper.selectByPrimaryKey(id);
		String fullFileName = localPath + "/" + fileUpload.getFileName();
		try {
			logger.debug("文件是否存在:" + new File(localPath).listFiles().length);
			fileUpload.setPhase(2);
			fileUpload.setStatus("doing");
			fileUploadContainerMapper.updateByPrimaryKeySelective(fileUpload);
			ProcessBuilder proc = new ProcessBuilder("sh", shellPath, fullFileName,
					fileUpload.getNamespace(), fileUpload.getPod(), fileUpload.getContainerFilePath(), token, server);
			if (StringUtils.isNotBlank(fileUpload.getContainer())) {
				proc = new ProcessBuilder("sh", shellPath, fullFileName,
						fileUpload.getNamespace(), fileUpload.getPod(), fileUpload.getContainerFilePath(), token,
						server, fileUpload.getContainer());
			}

			Process p = proc.start();
			String res = null;
			String exception = null;
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			while ((res = stdInput.readLine()) != null) {
				logger.debug("执行上传文件脚本：" + res);
			}
			while ((res = stdError.readLine()) != null) {
				exception = res;
				logger.error("执行上传文件脚本错误：" + res + ":" + fullFileName);
			}
			int runningStatus = p.waitFor();
			logger.debug("执行上传文件脚本结果：" + runningStatus);
			// 0代表成功, 更新数据库
			if (runningStatus == 0) {
				fileUpload.setStatus("success");
				logger.debug("上传到容器成功：" + fullFileName);
			} else {
				logger.debug("执行上传文件脚本结果失败:" + exception);
				fileUpload.setStatus("failed");
				fileUpload.setErrMsg(exception);
			}
			fileUploadContainerMapper.updateByPrimaryKeySelective(fileUpload);
		} catch (Exception e) {
			logger.error("执行上传文件脚本结果失败:" + e.getLocalizedMessage() + ";" + e.getMessage() + ";" + e.getCause());
			fileUpload.setStatus("failed");
			fileUpload.setErrMsg(e.getMessage());
			fileUploadContainerMapper.updateByPrimaryKeySelective(fileUpload);
			throw e;
		}
	}

	@Override
	public void deleteUploadRecord(String namespace, String deployment) throws Exception {
		FileUploadContainerExample fucExample = new FileUploadContainerExample();
		fucExample.createCriteria().andNamespaceEqualTo(namespace).andDeploymentEqualTo(deployment);
		fileUploadContainerMapper.deleteByExample(fucExample);
	}

	/**
	 * 获取浏览器文件上传到webapi服务的本地目录
	 */
	private String getLocalFilePath(String namespace, String deployment){
		return (uploadPath.endsWith("/") ? uploadPath : uploadPath + "/") + namespace + "/" + deployment;
	}

	public String getUploadPath() {
		return uploadPath;
	}

	public void setUploadPath(String uploadPath) {
		this.uploadPath = uploadPath;
	}

	public static void main(String[] args) throws IOException {
	}
	/*
	 * String res = null; ProcessBuilder proc = new ProcessBuilder("sh",
	 * "/home/jmi/Documents/工作/2017/实现/test.sh", "sqqweq"); Process p =
	 * proc.start(); System.out.println("sasasas"); BufferedReader stdInput =
	 * new BufferedReader(new InputStreamReader(p.getInputStream()));
	 * BufferedReader stdError = new BufferedReader(new
	 * InputStreamReader(p.getErrorStream())); while ((res =
	 * stdInput.readLine()) != null) { System.out.println(res); } while ((res =
	 * stdError.readLine()) != null) { System.err.println(res); } try { int
	 * runningStatus = p.waitFor(); System.out.println(runningStatus); } catch
	 * (InterruptedException e) { }
	 */
	/*
	 * Runnable task = new Runnable() {
	 * 
	 * @Override public void run() { String res = null; try { ProcessBuilder
	 * proc = new ProcessBuilder("ls");
	 * 
	 * Process p = proc.start(); System.out.println("sasasas"); BufferedReader
	 * stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
	 * BufferedReader stdError = new BufferedReader(new
	 * InputStreamReader(p.getErrorStream())); while ((res =
	 * stdInput.readLine()) != null) { System.out.println(res); } while ((res =
	 * stdError.readLine()) != null) { System.err.println(res); } try { int
	 * runningStatus = p.waitFor(); System.out.println(runningStatus); } catch
	 * (InterruptedException e) { } } catch (Exception e) { System.out.println(
	 * "Error executing notepad."); }
	 * 
	 * } }; ESFactory.executor.execute(task); System.out.println("ytyty");
	 * return;
	 */

}
