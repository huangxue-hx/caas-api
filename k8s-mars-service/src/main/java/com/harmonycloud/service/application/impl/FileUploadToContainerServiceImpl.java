package com.harmonycloud.service.application.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import javax.servlet.http.HttpSession;

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
import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.dao.system.SystemConfigMapper;
import com.harmonycloud.dao.system.bean.SystemConfig;
import com.harmonycloud.dto.business.ContainerFileUploadDto;
import com.harmonycloud.dto.business.PodContainerDto;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.service.application.FileUploadToContainerService;

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
	private HttpSession session;

	@Value("#{propertiesReader['upload.path']}")
	private String uploadPath;
	
	@Autowired
    private SystemConfigMapper systemConfigMapper;

	private final static Logger logger = LoggerFactory.getLogger(FileUploadToContainerServiceImpl.class);

	@Override
	@Transactional(rollbackFor = Exception.class)
	public ActionReturnUtil fileUploadToNode(ContainerFileUploadDto containerFileUpload, MultipartFile file)
			throws Exception {
		List<PodContainerDto> podList = containerFileUpload.getPods();
		if (podList == null) {
			return ActionReturnUtil.returnErrorWithMsg("未选择pod");
		}
		String fileName = file.getOriginalFilename();
		String path = uploadPath + containerFileUpload.getNamespace();
		File dirFile = new File(path);
		File newFile = new File(path + "/" + fileName);
		logger.info("newFilepath:" + newFile.getPath());
		Long userId = Long.valueOf(session.getAttribute("userId").toString());
		List<Integer> uploadIds = new ArrayList<Integer>();
		for (PodContainerDto pDto : podList) {

			// 查询数据库是否含有相同的记录
			FileUploadContainerExample example = new FileUploadContainerExample();
			FileUploadContainer fUploadContainer = new FileUploadContainer();
			fUploadContainer.setContainerFilePath(containerFileUpload.getContainerFilePath());
			fUploadContainer.setNamespace(containerFileUpload.getNamespace());
			fUploadContainer.setDeployment(containerFileUpload.getDeployment());
			fUploadContainer.setFileName(fileName);
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
							.andFileNameEqualTo(fileName).andContainerEqualTo(c);
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
						.andFileNameEqualTo(fileName);
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
			logger.info("dirFileExist:" + dirFile.exists());
			if (!dirFile.exists()) {
				dirFile.mkdirs();
			}
			try {
				file.transferTo(newFile);
				logger.info("newFileIsExist:" + newFile.exists());
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
		List<PodContainerDto> podList = containerFileUpload.getPods();
		final List<Integer> uploadIds = containerFileUpload.getUploadIdList();
		if (podList == null && uploadIds == null) {
			return ActionReturnUtil.returnErrorWithMsg("未选择pod");
		}
		FileUploadContainer fileUpload = fileUploadContainerMapper.selectByPrimaryKey(uploadIds.get(0));
		final String sPath = shellPath;
		final String localPath = uploadPath + containerFileUpload.getNamespace();
		final String fileAbsolutePath = localPath + "/" + fileUpload.getFileName();
		String username = String.valueOf(session.getAttribute("username"));
		final String token = String.valueOf(K8SClient.tokenMap.get(username));
		Cluster cluster = (Cluster) session.getAttribute("currentCluster");
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
		
		//删除文件
		ESFactory.executor.execute(new Runnable() {

			@Override
			public void run() {
				try {
					if (StringUtils.isNotBlank(fileAbsolutePath)) {
						File f = new File(fileAbsolutePath);
						if (f.isFile() && f.exists()) {
							f.delete();
						}
					}
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}

			}
		});

		// ESFactory.executor.shutdown();
		return ActionReturnUtil.returnSuccess();
	}

	@Override
	public ActionReturnUtil queryUploadPhase(ContainerFileUploadDto containerFileUpload) throws Exception {
		List<PodContainerDto> podList = containerFileUpload.getPods();
		List<Integer> uploadIds = containerFileUpload.getUploadIdList();
		if (podList == null && uploadIds == null) {
			return ActionReturnUtil.returnErrorWithMsg("未选择pod");
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
		Long userId = Long.valueOf(session.getAttribute("userId").toString());
		List<PodContainerDto> podList = containerFileUpload.getPods();
		if (podList == null) {
			return ActionReturnUtil.returnErrorWithMsg("未接收到pod信息");
		}
		List<FileUploadContainer> result = new ArrayList<FileUploadContainer>();
		for (PodContainerDto pContainerDto : podList) {
			FileUploadContainerExample fucExample = new FileUploadContainerExample();
			fucExample.createCriteria().andNamespaceEqualTo(containerFileUpload.getNamespace())
					.andDeploymentEqualTo(containerFileUpload.getDeployment()).andUserIdEqualTo(userId)
					.andPodEqualTo(pContainerDto.getName());
			List<FileUploadContainer> records = fileUploadContainerMapper.selectByExample(fucExample);
			result.addAll(records);
		}

		return ActionReturnUtil.returnSuccessWithData(result);
	}

	@Override
	public ActionReturnUtil lsContainerFile(String namespace, String containerFilePath, String container, String pod,
			String shellPath) throws Exception {
		String username = String.valueOf(session.getAttribute("username"));
		String token = String.valueOf(K8SClient.tokenMap.get(username));
		Cluster cluster = (Cluster) session.getAttribute("currentCluster");
		String server = cluster.getProtocol() + "://" + cluster.getHost() + ":" + cluster.getPort();
		ProcessBuilder proc = new ProcessBuilder("bash", shellPath, pod, containerFilePath, namespace, token, server);
		if (StringUtils.isNotBlank(container)) {
			proc = new ProcessBuilder("bash", shellPath, pod, containerFilePath, namespace, token, server, container);
		}
		Process p = proc.start();
		String res = null;
		List<Map<String, String>> files = new ArrayList<Map<String, String>>();
		BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
		BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
		while ((res = stdInput.readLine()) != null) {
			Map<String, String> file = new HashMap<String, String>();
			file.put("name", res);
			files.add(file);
			logger.debug("文件名：" + res);
		}
		while ((res = stdError.readLine()) != null) {
			logger.error("执行容器文件目录脚本错误：" + res);
			return ActionReturnUtil.returnErrorWithMsg(res);
		}
		int runningStatus = p.waitFor();
		logger.debug("执行容器文件目录结果：" + runningStatus);
		return ActionReturnUtil.returnSuccessWithData(files);
	}

	@Override
	public ActionReturnUtil deleteFile(ContainerFileUploadDto containerFileUpload) throws Exception {
		Long userId = Long.valueOf(session.getAttribute("userId").toString());
		FileUploadContainerExample fucExample = new FileUploadContainerExample();
		fucExample.createCriteria().andNamespaceEqualTo(containerFileUpload.getNamespace())
				.andDeploymentEqualTo(containerFileUpload.getDeployment()).andUserIdEqualTo(userId);
		List<FileUploadContainer> deleteList = fileUploadContainerMapper.selectByExample(fucExample);
		if (!deleteList.isEmpty()) {
			// 删除文件
			for (FileUploadContainer filec : deleteList) {
				String path = uploadPath + containerFileUpload.getNamespace() + "/" + filec.getFileName();
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
		String username = (String) session.getAttribute("username");
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
		String path = null;
		try {
			logger.info("文件是否存在:" + new File(localPath).listFiles().length);
			fileUpload.setPhase(2);
			fileUpload.setStatus("doing");
			fileUploadContainerMapper.updateByPrimaryKeySelective(fileUpload);
			ProcessBuilder proc = new ProcessBuilder("sh", shellPath, localPath + "/" + fileUpload.getFileName(),
					fileUpload.getNamespace(), fileUpload.getPod(), fileUpload.getContainerFilePath(), token, server);
			if (StringUtils.isNotBlank(fileUpload.getContainer())) {
				proc = new ProcessBuilder("sh", shellPath, localPath + "/" + fileUpload.getFileName(),
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
				logger.error("执行上传文件脚本错误：" + res + ":" + localPath + "/" + fileUpload.getFileName());
			}
			int runningStatus = p.waitFor();
			logger.info("执行上传文件脚本结果：" + runningStatus);
			// 0代表成功, 更新数据库
			if (runningStatus == 0) {
				fileUpload.setStatus("success");
				path = localPath + "/" + fileUpload.getFileName();
				logger.info("上传到容器成功：" + path);
			} else {
				logger.info("执行上传文件脚本结果失败:" + exception);
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
