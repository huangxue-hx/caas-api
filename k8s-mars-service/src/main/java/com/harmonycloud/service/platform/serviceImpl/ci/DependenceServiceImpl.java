package com.harmonycloud.service.platform.serviceImpl.ci;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.CollectionUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.dao.ci.bean.Stage;
import com.harmonycloud.dto.application.StorageClassDto;
import com.harmonycloud.dto.cicd.DependenceDto;
import com.harmonycloud.dto.cicd.DependenceFileDto;
import com.harmonycloud.k8s.bean.*;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.APIGroup;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.service.PVCService;
import com.harmonycloud.k8s.service.PvService;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import com.harmonycloud.k8s.util.RandomNum;
import com.harmonycloud.service.application.PersistentVolumeService;
import com.harmonycloud.service.application.StorageClassService;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.platform.service.ci.DependenceService;
import com.harmonycloud.service.platform.service.ci.StageService;
import com.harmonycloud.service.tenant.ProjectService;
import com.harmonycloud.service.user.RoleLocalService;
import com.harmonycloud.service.user.UserService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author w_kyzhang
 * @Description 依赖管理方法实现
 * @Date 2017-7-29
 * @Modified
 */
@Service
public class DependenceServiceImpl implements DependenceService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("#{propertiesReader['upload.path']}")
    private String uploadPath;

    @Autowired
    private PvService pvService;

    @Autowired
    private PersistentVolumeService persistentvolumeService;

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private PVCService pvcService;

    @Autowired
    private RoleLocalService roleLocalService;

    @Autowired
    private StageService stageService;

    @Autowired
    private UserService userService;

    @Autowired
    private HttpSession session;

    @Autowired
    private StorageClassService storageClassService;

    private ClassLoader classLoader = this.getClass().getClassLoader();

    /**
     *根据项目id和集群id及名称查询依赖列表
     * @param projectId
     * @param clusterId
     * @param name
     * @return
     * @throws Exception
     */
    @Override
    public List<Map> listByProjectIdAndClusterId(String projectId, String clusterId, String name) throws Exception {
        Cluster topCluster = clusterService.getPlatformCluster();
        List<Map> dependenceDtos = new ArrayList<>();
        if(projectService.getProjectByProjectId(projectId) == null){
            return Collections.emptyList();
        }
        List<StorageClassDto> storageClassDtoList = storageClassService.listStorageClass(topCluster.getId());
        Map<String, StorageClassDto> storageClassMap = storageClassDtoList.stream().collect(Collectors.toMap(StorageClassDto::getName, storageClassDto->storageClassDto));

        if(StringUtils.isBlank(clusterId)){
            List<Cluster> clusterList = roleLocalService.listCurrentUserRoleCluster();
            if(CollectionUtils.isNotEmpty(clusterList)){
                for(Cluster cluster : clusterList){
                    dependenceDtos.addAll(getDenpenceByLabel(projectId, cluster.getId(), name, storageClassMap));
                }
            }
        }else{
            dependenceDtos.addAll(getDenpenceByLabel(projectId, clusterId, name, storageClassMap));
        }
        dependenceDtos.addAll(getDenpenceByLabel(projectId, null, name, storageClassMap));
        return dependenceDtos;
    }

    private List getDenpenceByLabel(String projectId, String clusterId, String name, Map<String, StorageClassDto> storageClassMap) throws Exception{
        List<Map> dependenceDtos = new ArrayList<>();
        Cluster topCluster = clusterService.getPlatformCluster();
        String label = null;
        boolean isPublic = false;
        if(clusterId != null) {
            label = "projectId =" + projectId + ",clusterId = " + clusterId + ",common = false";
        }else{
            isPublic = true;
            label = "common = true";
        }
        Map<String, Object> query = new HashMap<>();
        query.put(CommonConstant.LABELSELECTOR, label);
        K8SClientResponse response = pvcService.doSepcifyPVC(CommonConstant.CICD_NAMESPACE, query, HTTPMethod.GET, topCluster);
        if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            PersistentVolumeClaimList persistentVolumeClaimList = K8SClient.converToBean(response, PersistentVolumeClaimList.class);
            List<PersistentVolumeClaim> items = persistentVolumeClaimList.getItems();
            // 处理items返回页面需要的对象
            for (PersistentVolumeClaim pvc : items) {
                Map dependenceDto = new HashMap();
                String displayName = (String) pvc.getMetadata().getLabels().get("name");
                if (name == null || displayName.contains(name)) {
                    dependenceDto.put("name", displayName);
                    dependenceDto.put("storageClassName", pvc.getSpec().getStorageClassName());
                    if(pvc.getSpec().getStorageClassName() != null){
                        StorageClassDto storageClassDto = storageClassMap.get(pvc.getSpec().getStorageClassName());
                        dependenceDto.put("storageClassType", storageClassDto.getType());
                        if(CommonConstant.NFS.equalsIgnoreCase(storageClassDto.getType())) {
                            String nfsServer = storageClassDto.getConfigMap().get(CommonConstant.NFS_SERVER);
                            String nfsPath = storageClassDto.getConfigMap().get(CommonConstant.NFS_PATH);
                            if(StringUtils.isNoneBlank(nfsServer, nfsPath)) {
                                dependenceDto.put("serverPath", nfsServer + ":" + nfsPath + "/" + CommonConstant.CICD_NAMESPACE + "-" + pvc.getMetadata().getName() + "-" + pvc.getSpec().getVolumeName());
                            }
                        }
                    }
                    dependenceDto.put("common", isPublic);
                    dependenceDto.put("clusterId", clusterId);
                    dependenceDto.put("pvName", pvc.getMetadata().getName());
                    dependenceDtos.add(dependenceDto);
                }
            }
        }
        return dependenceDtos;
    }

    /**
     *新增依赖
     * @param dependenceDto
     * @throws Exception
     */
    public void add(DependenceDto dependenceDto) throws Exception {
        Cluster topCluster = clusterService.getPlatformCluster();

        //创建pvc
        String pvcName;
        if(dependenceDto.isCommon()){
            pvcName = dependenceDto.getName();
        }else{
            String projectName = projectService.getProjectNameByProjectId(dependenceDto.getProjectId());
            String clusterName = clusterService.getClusterNameByClusterId(dependenceDto.getClusterId());
            pvcName = projectName + "-" + clusterName + "-" + dependenceDto.getName();
        }

        PersistentVolumeClaim pvc = pvcService.getPVCByNameAndNamespace(pvcName, CommonConstant.CICD_NAMESPACE, topCluster);
        if (pvc != null) {
            throw new MarsRuntimeException(ErrorCodeMessage.NAME_EXIST, dependenceDto.getName(), true);
        }

        PersistentVolumeClaim persistentVolumeClaim = new PersistentVolumeClaim();
        ObjectMeta pvcMetadata = new ObjectMeta();
        pvcMetadata.setName(pvcName);
        Map<String, Object> labels = new HashMap<>();
        labels.put("name",dependenceDto.getName());
        labels.put("projectId", dependenceDto.isCommon()? null : dependenceDto.getProjectId());
        labels.put("clusterId", dependenceDto.isCommon()? null : dependenceDto.getClusterId());
        labels.put("common",String.valueOf(dependenceDto.isCommon()));
        labels.put(CommonConstant.USERNAME, session.getAttribute(CommonConstant.USERNAME));
        pvcMetadata.setLabels(labels);
        persistentVolumeClaim.setMetadata(pvcMetadata);
        PersistentVolumeClaimSpec pvcSpec = new PersistentVolumeClaimSpec();
        List<String> modes = new ArrayList<String>();
        modes.add(CommonConstant.READWRITEMANY);
        pvcSpec.setAccessModes(modes);
        pvcSpec.setStorageClassName(dependenceDto.getStorageClassName());
        ResourceRequirements resourceRequirements = new ResourceRequirements();
        Map<String, Object> limits = new HashMap<String, Object>();
        limits.put("storage", "1Gi");
        resourceRequirements.setRequests(limits);
        pvcSpec.setResources(resourceRequirements);
        persistentVolumeClaim.setSpec(pvcSpec);

        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        Map<String, Object> bodys = CollectionUtil.transBean2Map(persistentVolumeClaim);
        K8SURL url = new K8SURL();
        url.setNamespace(CommonConstant.CICD_NAMESPACE).setResource(Resource.PERSISTENTVOLUMECLAIM);
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.POST, headers, bodys, topCluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            throw new MarsRuntimeException(ErrorCodeMessage.DEPENDENCE_CREATE_FAIL);
        }
    }

    /**
     * 删除依赖
     * @param name 依赖名
     * @param projectId 项目id
     * @param clusterId 集群id
     * @throws Exception
     */
    @Override
    public void delete(String name, String projectId, String clusterId) throws Exception {
        Cluster topCluster = clusterService.getPlatformCluster();
        String pvcName;
        if(StringUtils.isNotBlank(clusterId)){
            String projectName = projectService.getProjectNameByProjectId(projectId);
            String clusterName = clusterService.getClusterNameByClusterId(clusterId);
            pvcName = projectName + "-" + clusterName + "-" + name;
        }else{
            pvcName = name;
        }
        Stage stage = new Stage();
        stage.setDependences("\"pvName\":\"" + pvcName + "\"");
        List stageList = stageService.selectByExample(stage);
        if(CollectionUtils.isNotEmpty(stageList)){
            throw new MarsRuntimeException(ErrorCodeMessage.DEPENDENCE_USED);
        }
        PersistentVolumeClaim pvc = pvcService.getPvcByName(CommonConstant.CICD_NAMESPACE, pvcName, topCluster);
        if(pvc == null){
            throw new MarsRuntimeException(ErrorCodeMessage.DEPENDENCE_ALREADY_DELETED);
        }
        Map<String, Object> labels = pvc.getMetadata().getLabels();
        String createUser = (String)labels.get(CommonConstant.USERNAME);
        String username = (String)session.getAttribute(CommonConstant.USERNAME);
        if(StringUtils.isBlank(clusterId)) {
            if(StringUtils.isNotBlank(username) && !username.equals(createUser)){
                if(!userService.checkCurrentUserIsAdmin()){
                    throw new MarsRuntimeException(ErrorCodeMessage.DEPENDENCE_NO_PRIVILEGE_DELETE);
                }
            }
        }


        K8SURL k8SURL = new K8SURL();
        k8SURL.setApiGroup(APIGroup.API_V1_VERSION);
        k8SURL.setNamespace(CommonConstant.CICD_NAMESPACE);
        k8SURL.setResource(Resource.PERSISTENTVOLUMECLAIM);
        k8SURL.setName(pvcName);
        K8SClientResponse pvcResponse = new K8sMachineClient().exec(k8SURL, HTTPMethod.DELETE, null, null, topCluster);
        if (!HttpStatusUtil.isSuccessStatus(pvcResponse.getStatus())) {
            throw new MarsRuntimeException(ErrorCodeMessage.PVC_CAN_NOT_DELETE);
        }

    }

    /**
     * 上传文件至依赖目录
     *
     * @param dependenceFileDto 依赖文件对象
     * @throws Exception
     */
    @Override
    public void uploadFile(DependenceFileDto dependenceFileDto) throws Exception {

        Cluster topCluster = clusterService.getPlatformCluster();
        String server = topCluster.getProtocol() + "://" + topCluster.getHost() + ":" + topCluster.getPort();


        File tmpDirectory;
        String localFile;
        MultipartFile file = dependenceFileDto.getFile();
        String fileName = file.getOriginalFilename();
        String projectName = null;
        String pvcName;
        if(StringUtils.isNotBlank(dependenceFileDto.getClusterId())){
            projectName = projectService.getProjectNameByProjectId(dependenceFileDto.getProjectId());
            String clusterName = clusterService.getClusterNameByClusterId(dependenceFileDto.getClusterId());
            pvcName = projectName + "-" + clusterName + "-" + dependenceFileDto.getDependenceName();
            tmpDirectory = new File(uploadPath + "/" + projectName + "/" + clusterName);
            localFile = tmpDirectory.getPath() + "/" + fileName;
        }else{
            pvcName = dependenceFileDto.getDependenceName();
            tmpDirectory = new File(uploadPath + "/" + dependenceFileDto.getDependenceName());
            localFile = tmpDirectory.getPath() + "/" + fileName;
        }


        PersistentVolumeClaim pvc = pvcService.getPvcByName(CommonConstant.CICD_NAMESPACE, pvcName, topCluster);
        String storageClassName = pvc.getSpec().getStorageClassName();
        String remoteDirectory = getRemoteDependenceDir(pvc) + "/";

        //创建临时目录下的项目目录

        if (!tmpDirectory.exists()) {
            tmpDirectory.mkdirs();
        }

        //文件上传至临时目录

        File tmpFile = new File(localFile);

        file.transferTo(tmpFile);

        Pod fileUploadPod = this.getFileUploadPod(storageClassName, topCluster);
        String fileUploadPodName = fileUploadPod.getMetadata().getName();

        ProcessBuilder proc;
        Process p = null;
        String res;

        String[] uploadDependenceCommand = {
                "kubectl",
                "cp",
                localFile,
                CommonConstant.KUBE_SYSTEM + "/" + fileUploadPodName + ":" + remoteDirectory,
                "--token=" + topCluster.getMachineToken(),
                "--server="+server,
                "--insecure-skip-tls-verify=true" };
        boolean error = false;
        try {
            if (!dependenceFileDto.isDecompressed()) {
                p = Runtime.getRuntime().exec(uploadDependenceCommand);
                BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                while ((res = stdInput.readLine()) != null) {
                    logger.info("执行上传文件命令：" + res);
                }
                while ((res = stdError.readLine()) != null) {
                    if(res.contains("in the future") || res.contains("implausibly old time stamp")){
                        logger.warn("执行上传文件命令警告：" + res);
                    }else {
                        logger.error("执行上传文件命令错误：" + res);
                        error = true;
                    }
                }
                if (error) {
                    throw new Exception();
                }
                int runningStatus = p.waitFor();
                logger.info("执行容器文件命令结果：" + runningStatus);

            } else {
                String localDirectory;
                if(StringUtils.isNotBlank(dependenceFileDto.getClusterId())){
                    localDirectory = uploadPath + "/" + projectName + "/" + RandomNum.getRandomString(CommonConstant.RANDOM_BIT_8);
                }else{
                    localDirectory = uploadPath + "/" + RandomNum.getRandomString(CommonConstant.RANDOM_BIT_8);
                }
                String shellPath = classLoader.getResource("shell/decompressDependence.sh").getPath();
                proc = new ProcessBuilder("sh", shellPath, localDirectory, localFile, remoteDirectory, fileUploadPodName, CommonConstant.KUBE_SYSTEM, topCluster.getMachineToken(), server);
                p = proc.start();
                BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                while ((res = stdInput.readLine()) != null) {
                    logger.info("执行解压文件脚本：" + res);
                }
                while ((res = stdError.readLine()) != null) {
                    if(res.contains("in the future") || res.contains("implausibly old time stamp")){
                        logger.warn("执行解压文件脚本警告：" + res);
                    }else {
                        logger.error("执行解压文件脚本错误：" + res);
                        error = true;
                    }
                }
                if (error) {
                    throw new Exception();
                }
                int runningStatus = p.waitFor();
                logger.info("执行解压文件脚本结果：" + runningStatus);

            }
        } catch (Exception e) {
            logger.error("执行上传文件脚本错误：", e);
            throw new MarsRuntimeException(ErrorCodeMessage.DEPENDENCE_FILE_UPLOAD_FAIL);
        } finally {
            if (null != p) {
                p.destroy();
            }
            if (tmpFile.exists()) {
                tmpFile.delete();
            }
        }
    }


    /**
     * 返回依赖目录下的文件
     *
     * @param dependenceName 依赖名
     * @param projectId      项目id
     * @param clusterId      集群id
     * @param path           路径
     * @return
     * @throws Exception
     */
    public List listFile(String dependenceName, String projectId, String clusterId, String path) throws Exception {
        return getFileList(dependenceName, projectId, clusterId, path, false);
    }

    /**
     * 删除依赖目录中的文件
     *
     * @param dependenceName 依赖名
     * @param projectId      项目id
     * @param clusterId      集群id
     * @param path           路径
     * @throws Exception
     */
    public void deleteFile(String dependenceName, String projectId, String clusterId, String path) throws Exception {
        Cluster topCluster = clusterService.getPlatformCluster();
        String pvcName;
        if(StringUtils.isNotBlank(clusterId)) {
            String projectName = projectService.getProjectNameByProjectId(projectId);
            String clusterName = clusterService.getClusterNameByClusterId(clusterId);
            pvcName = projectName + "-" + clusterName + "-" + dependenceName;
        }else{
            pvcName = dependenceName;
        }
        PersistentVolumeClaim pvc = pvcService.getPvcByName(CommonConstant.CICD_NAMESPACE, pvcName, topCluster);
        String storageClassName = pvc.getSpec().getStorageClassName();
        if(StringUtils.isBlank(storageClassName)){
            throw new MarsRuntimeException(ErrorCodeMessage.DEPENDENCE_FILE_RM_FAIL);
        }
        String fileUploadPodName = getFileUploadPod(storageClassName, topCluster).getMetadata().getName();
        String remoteDependenceDir = getRemoteDependenceDir(pvc);
        String targetPath = remoteDependenceDir + "/" + path;

        deleteFile(fileUploadPodName, targetPath, topCluster);
    }

    /**
     * 根据文件或目录的名称关键词查询依赖目录下的文件或目录
     *
     * @param dependenceName 依赖名
     * @param projectId      项目id
     * @param clusterId      集群id
     * @param keyWord
     * @return
     * @throws Exception
     */
    @Override
    public List findDependenceFileByKeyword(String dependenceName, String projectId, String clusterId, String keyWord) throws Exception {
        keyWord = keyWord.toLowerCase();
        List<Map<String, Object>> list = getFileList(dependenceName, projectId, clusterId, "", true);
        List<Map<String, Object>> result = new ArrayList();

        for (Map<String, Object> file : list) {
            String fileName = file.get("fileName").toString().toLowerCase();
            if (fileName.contains(keyWord)){
                result.add(file);
            }

        }

        return result;
    }

    @Override
    public List<StorageClassDto> listStorageClass() throws Exception {
        Cluster topCluster = clusterService.getPlatformCluster();
        if(topCluster != null){
            return storageClassService.listStorageClass(topCluster.getId());
        }else{
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
    }

    /**
     * 获取文件上传pod
     *
     * @param cluster 集群对象
     * @return
     * @throws Exception
     */
    private Pod getFileUploadPod(String storageClassName, Cluster cluster) throws Exception {



        K8SURL url = new K8SURL();
        url.setNamespace(CommonConstant.KUBE_SYSTEM);
        url.setResource(Resource.POD);
        String label = CommonConstant.FILE_UPLOAD_POD_LABEL + "-" + storageClassName;
        Map<String, Object> body = new HashMap<>();
        body.put(CommonConstant.LABELSELECTOR, label);
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET, null, body, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            throw new MarsRuntimeException(DictEnum.POD.phrase(), ErrorCodeMessage.NOT_FOUND);
        }
        PodList podList = K8SClient.converToBean(response, PodList.class);
        List<Pod> pods = podList.getItems();
        if (CollectionUtils.isEmpty(pods)) {
            throw new MarsRuntimeException(DictEnum.POD.phrase(), ErrorCodeMessage.NOT_FOUND);
        }
        for(Pod pod : pods){
            if(CommonConstant.POD_STATUS_RUNNING.equalsIgnoreCase(pod.getStatus().getPhase())){
                return pod;
            }
        }
        throw new MarsRuntimeException(DictEnum.POD.phrase(), ErrorCodeMessage.NOT_FOUND);
    }

    @Override
    public void deleteDependenceByProject(String projectId) throws Exception {
        Cluster topCluster = clusterService.getPlatformCluster();
        String label = "projectId = " + projectId;
        Map<String, Object> pvclabel = new HashMap<String, Object>();
        pvclabel.put("labelSelector", label);
        K8SClientResponse response = pvcService.doSepcifyPVC(CommonConstant.CICD_NAMESPACE, pvclabel, HTTPMethod.DELETE,topCluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            logger.error("删除依赖失败：{}", response.getBody());
        }
    }

    private void deleteFile(String fileUploadPodName, String directory, Cluster topCluster){
        String server = topCluster.getProtocol() + "://" + topCluster.getHost() + ":" + topCluster.getPort();
        Process p = null;

        String rmCmd = "rm -rf";
        String rmDependenceCommand = String.format("kubectl exec %s -n %s --token=%s --server=%s --insecure-skip-tls-verify=true -- %s %s",
                fileUploadPodName, CommonConstant.KUBE_SYSTEM, topCluster.getMachineToken(), server, rmCmd,directory);


        try {
            p = Runtime.getRuntime().exec(rmDependenceCommand);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String res;
            boolean error = false;
            while ((res = stdInput.readLine()) != null) {
                logger.info("执行删除命令：" + res);
            }
            while ((res = stdError.readLine()) != null) {
                logger.error("执行删除命令错误：" + res);
                error =true;
            }
            if(error){
                throw new Exception();
            }
            int runningStatus = p.waitFor();
            logger.info("执行删除命令结果：" + runningStatus);
        } catch (Exception e) {
            throw new MarsRuntimeException(ErrorCodeMessage.DEPENDENCE_FILE_RM_FAIL);
        } finally {
            if (p != null) {
                p.destroy();
            }
        }
    }


    private List getFileList(String dependenceName, String projectId, String clusterId, String path, boolean isRecurse) throws Exception {
        Cluster topCluster = clusterService.getPlatformCluster();
        String server = topCluster.getProtocol() + "://" + topCluster.getHost() + ":" + topCluster.getPort();

        String pvcName;
        if(StringUtils.isNotBlank(clusterId)) {
            String projectName = projectService.getProjectNameByProjectId(projectId);
            String clusterName = clusterService.getClusterNameByClusterId(clusterId);
            pvcName = projectName + "-" + clusterName + "-" + dependenceName;
        }else{
            pvcName = dependenceName;
        }
        PersistentVolumeClaim pvc = pvcService.getPvcByName(CommonConstant.CICD_NAMESPACE, pvcName, topCluster);
        String remoteDependenceDir = getRemoteDependenceDir(pvc);
        String targetDir = remoteDependenceDir + "/" + path;

        Pod fileUploadPod = getFileUploadPod(pvc.getSpec().getStorageClassName(), topCluster);
        String fileUploadPodName = fileUploadPod.getMetadata().getName();
        Process p = null;
        boolean error = false;
        try {
            String[] lsCmd;
            if(isRecurse){
                lsCmd = new String[]{"ls", "-lhR", "--full-time", targetDir};
            }else {
                lsCmd = new String[]{"ls", "-lh", "--full-time", targetDir};
            }
            String[] lsDependenceCommand = ArrayUtils.addAll(new String[]{
                            "kubectl",
                            "exec",
                            fileUploadPodName,
                            "-n",
                            CommonConstant.KUBE_SYSTEM,
                            "--token=" + topCluster.getMachineToken(),
                            "--server=" + server,
                            "--insecure-skip-tls-verify=true",
                            "--"}, lsCmd
            );
            p = Runtime.getRuntime().exec(lsDependenceCommand);

            String res;
            List<Map<String, Object>> files = new ArrayList();
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));



            String tmpParentDirectory = "/";
            String lastLine = null;
            while ((res = stdInput.readLine()) != null) {
                Map<String, Object> file = new HashMap();
                String[] fileAttributes = res.split("\\s+", 9);
                if(fileAttributes.length < 9 || StringUtils.isBlank(lastLine)){
                    if (fileAttributes.length == 2 && "total".equalsIgnoreCase(fileAttributes[0]) && StringUtils.isNotBlank(lastLine)){
                        tmpParentDirectory = lastLine.substring(lastLine.lastIndexOf(remoteDependenceDir) + remoteDependenceDir.length());
                        tmpParentDirectory = tmpParentDirectory.substring(0,tmpParentDirectory.length()-1);
                    }
                    lastLine = res;
                    continue;
                }
                if(fileAttributes[0].startsWith(CommonConstant.DIRECTORY_TYPE)){
                    file.put("type", "directory");
                    file.put("isDirectory",true);
                } else {
                    file.put("isDirectory", false);
                }

                //处理无后缀的文件
                if(!(boolean)file.get("isDirectory") && fileAttributes[8].contains(".")){
                    file.put("type", fileAttributes[8].substring(fileAttributes[8].lastIndexOf(".")+1));
                    file.put("prefixFilename", fileAttributes[8].substring(0, fileAttributes[8].lastIndexOf(".")));
                }else if(!(boolean)file.get("isDirectory") && !fileAttributes[8].contains(".")){
                    file.put("type", "");
                    file.put("prefixFilename", fileAttributes[8]);
                }


                file.put("fileName", fileAttributes[8]);

                char c = fileAttributes[4].charAt(fileAttributes[4].length()-1);
                if( c >= '0' && c <= '9'){
                    fileAttributes[4] += "Byte";
                }
                file.put("size", fileAttributes[4]);
                if(!fileAttributes[6].contains(".")){
                    file.put("lastModified", fileAttributes[5] + " " +fileAttributes[6]) ;
                }else {
                    file.put("lastModified", fileAttributes[5] + " " +fileAttributes[6].substring(0, fileAttributes[6].lastIndexOf("."))) ;
                }
                if(isRecurse){
                    file.put("parentDirectory", tmpParentDirectory);
                }
                files.add(file);
            }while ((res = stdError.readLine()) != null) {
                logger.error("执行容器文件目录ls命令错误" + res);
                error = true;
            }
            if(error){
                throw new Exception();
            }
            int runningStatus = p.waitFor();
            logger.info("执行容器文件目录ls命令结果：" + runningStatus);
            return files;
        } catch (Exception e) {
            e.printStackTrace();
            throw new MarsRuntimeException(ErrorCodeMessage.DEPENDENCE_DIRECTORY_LIST_FAIL);
        } finally {
            if (null != p) {
                p.destroy();
            }
        }
    }

    private String getRemoteDependenceDir(PersistentVolumeClaim pvc) throws Exception {
        return "/dependence/" + pvc.getMetadata().getNamespace() + "-" + pvc.getMetadata().getName() + "-" + pvc.getSpec().getVolumeName();

    }
}
