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

import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.ThrowsAdvice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.ESFactory;
import com.harmonycloud.dao.application.FileUploadContainerMapper;
import com.harmonycloud.dao.application.bean.FileUploadContainer;
import com.harmonycloud.dao.application.bean.FileUploadContainerExample;
import com.harmonycloud.dto.business.ContainerFileUploadDto;
import com.harmonycloud.dto.business.PodContainerDto;
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
		final File dirFile = new File(path);
		final File newFile = new File(path + "/" + fileName);
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
			if (!dirFile.exists()) {
				dirFile.mkdirs();
			}
			try {
				file.transferTo(newFile);
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
		final String sPath = shellPath;
		for (PodContainerDto pDto : podList) {
			final ContainerFileUploadDto containerFu = containerFileUpload;
			final PodContainerDto pd = pDto;
			final String localPath = uploadPath + "/" + containerFu.getNamespace();
			ESFactory.executor.execute(new Runnable() {

				@Override
				public void run() {
					try {
						for (Integer id : uploadIds) {
							List<String> containers = pd.getContainer();
							if (containers != null && containers.size() > 0) {
								for (String c : containers) {
									doUploadFile(localPath, containerFu.getNamespace(), pd.getName(),
											containerFu.getContainerFilePath(), c, sPath, id);
								}
							} else {
								doUploadFile(localPath, containerFu.getNamespace(), pd.getName(),
										containerFu.getContainerFilePath(), null, sPath, id);
							}
						}
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}

				}
			});
		}
		ESFactory.executor.shutdown();
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
		ProcessBuilder proc = new ProcessBuilder("bash", shellPath, pod, containerFilePath, namespace);
		if (StringUtils.isNotBlank(container)) {
			proc = new ProcessBuilder("bash", shellPath, pod, containerFilePath, namespace, container);
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

	private void doUploadFile(String localPath, String namespace, String pod, String containerPath, String container,
			String shellPath, Integer id) throws Exception {
		FileUploadContainer fileUpload = fileUploadContainerMapper.selectByPrimaryKey(id);
		try {
			fileUpload.setPhase(2);
			fileUpload.setStatus("doing");
			fileUploadContainerMapper.updateByPrimaryKeySelective(fileUpload);
			ProcessBuilder proc = new ProcessBuilder("sh", shellPath, localPath + "/" + fileUpload.getFileName(),
					namespace, pod, containerPath);
			if (StringUtils.isNotBlank(container)) {
				proc = new ProcessBuilder("sh", shellPath, localPath + "/" + fileUpload.getFileName(), namespace, pod,
						containerPath, container);
			}

			Process p = proc.start();
			String res = null;
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			while ((res = stdInput.readLine()) != null) {
				logger.debug("执行上传文件脚本：" + res);
			}
			while ((res = stdError.readLine()) != null) {
				logger.error("执行上传文件脚本错误：" + res);
			}
			int runningStatus = p.waitFor();
			logger.debug("执行上传文件脚本结果：" + runningStatus);
			// 0代表成功, 更新数据库
			if (runningStatus == 0) {
				fileUpload.setStatus("success");
			} else {
				fileUpload.setStatus("failed");
			}
		} catch (Exception e) {
			fileUpload.setStatus("failed");
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