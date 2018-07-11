package com.harmonycloud.service.platform.serviceImpl.ci;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.CollectionUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.dao.ci.bean.Stage;
import com.harmonycloud.dao.tenant.bean.Project;
import com.harmonycloud.dto.cicd.DependenceDto;
import com.harmonycloud.dto.cicd.DependenceFileDto;
import com.harmonycloud.k8s.bean.*;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.bean.cluster.ClusterStorage;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.service.PVCService;
import com.harmonycloud.k8s.service.PvService;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import com.harmonycloud.k8s.util.RandomNum;
import com.harmonycloud.service.application.PersistentVolumeService;
import com.harmonycloud.service.cluster.ClusterService;

import com.harmonycloud.service.platform.service.ci.DependenceService;
import com.harmonycloud.service.platform.service.ci.StageService;
import com.harmonycloud.service.tenant.ProjectService;
import com.harmonycloud.service.user.RoleLocalService;
import com.harmonycloud.service.user.UserService;
import org.apache.commons.collections.CollectionUtils;
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
        ClusterStorage  storage = persistentvolumeService.getProvider(topCluster,CommonConstant.NFS);
        if(storage == null){
            throw new MarsRuntimeException(ErrorCodeMessage.PV_PROVIDER_NOT_EXIST, CommonConstant.NFS, true);
        }
        List<Map> pvDtos = new ArrayList<>();

        if(projectService.getProjectByProjectId(projectId) == null){
            return Collections.emptyList();
        }

        if(StringUtils.isBlank(clusterId)){
            List<Cluster> clusterList = roleLocalService.listCurrentUserRoleCluster();
            if(CollectionUtils.isNotEmpty(clusterList)){
                for(Cluster cluster : clusterList){
                    pvDtos.addAll(getDenpenceByLabel(projectId, cluster.getId(), name, storage));
                }
            }
        }else{
            pvDtos.addAll(getDenpenceByLabel(projectId, clusterId, name, storage));
        }
        pvDtos.addAll(getDenpenceByLabel(projectId, null, name, storage));
        return pvDtos;
    }

    private List getDenpenceByLabel(String projectId, String clusterId, String name, ClusterStorage storage) throws Exception{
        List<Map> pvDtos = new ArrayList<>();
        Cluster topCluster = clusterService.getPlatformCluster();
        String label = null;
        boolean isPublic = false;
        if(clusterId != null) {
            label = "projectId =" + String.valueOf(projectId) + ",clusterId = " + clusterId + ",common = false";
        }else{
            isPublic = true;
            label = "common = true";
        }
        K8SClientResponse response = pvService.listPvBylabel(label, topCluster);
        if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            PersistentVolumeList persistentVolumeList = K8SClient.converToBean(response, PersistentVolumeList.class);
            List<PersistentVolume> items = persistentVolumeList.getItems();
            // 处理items返回页面需要的对象
            for (PersistentVolume pv : items) {
                Map pvDto = new HashMap();
                String displayName = (String) pv.getMetadata().getLabels().get("name");
                if (name == null || displayName.contains(name)) {
                    pvDto.put("name", displayName);
                    pvDto.put("type", CommonConstant.NFS);
                    pvDto.put("server", storage.getIp());
                    pvDto.put("serverPath", storage.getPath() + "/" + StringUtils.join(pv.getMetadata().getName().split("\\."), "/"));
                    pvDto.put("common", isPublic);
                    pvDto.put("clusterId", clusterId);
                    pvDto.put("pvName", pv.getMetadata().getName());
                    pvDtos.add(pvDto);
                }
            }
        }
        return pvDtos;
    }

    /**
     *新增依赖
     * @param dependenceDto
     * @throws Exception
     */
    public void add(DependenceDto dependenceDto) throws Exception {
        if (StringUtils.isBlank(dependenceDto.getName())) {
            throw new MarsRuntimeException(ErrorCodeMessage.DEPENDENCE_NAME_NOT_BLANK);
        }
        Cluster topCluster = clusterService.getPlatformCluster();
        if (StringUtils.isBlank(dependenceDto.getNfsServer()) || StringUtils.isBlank(dependenceDto.getPath())) {
            ClusterStorage storage = persistentvolumeService.getProvider(topCluster, CommonConstant.NFS);
            if(storage == null){
                throw new MarsRuntimeException(ErrorCodeMessage.PV_PROVIDER_NOT_EXIST, CommonConstant.NFS, true);
            }
            dependenceDto.setNfsServer(storage.getIp());
            dependenceDto.setPath(storage.getPath());
        }
        String pvName;
        String nfsPath;
        String projectName = null;
        String clusterName = null;
        if(!dependenceDto.isCommon()) {
            projectName = projectService.getProjectNameByProjectId(dependenceDto.getProjectId());
            clusterName = clusterService.getClusterNameByClusterId(dependenceDto.getClusterId());

            pvName = CommonConstant.DEPENDENCE_PREFIX + "." + projectName + "-" + clusterName + "-" + dependenceDto.getName();
            nfsPath = dependenceDto.getPath() + "/" + CommonConstant.DEPENDENCE_PREFIX + "/" + projectName + "-" + clusterName + "-" + dependenceDto.getName();
        }else{
            pvName = CommonConstant.DEPENDENCE_PREFIX + "." + dependenceDto.getName();
            nfsPath = dependenceDto.getPath() + "/" + CommonConstant.DEPENDENCE_PREFIX + "/" + dependenceDto.getName();
        }

        //查重
        if (null != pvService.getPvByName(pvName, topCluster)) {
            throw new MarsRuntimeException(ErrorCodeMessage.DEPENDENCE_NAME_DUPLICATE);
        }

        String server = topCluster.getProtocol() + "://" + topCluster.getHost() + ":" + topCluster.getPort();

        Pod fileUploadPod = getFileUploadPod(topCluster);
        String fileUploadPodName = fileUploadPod.getMetadata().getName();
        String remoteDirectory;
        if(!dependenceDto.isCommon()) {
            remoteDirectory = "/nfs/" + projectName + "-" + clusterName + "-" + dependenceDto.getName();
        }else{
            remoteDirectory = "/nfs/" + dependenceDto.getName();
        }

        Process p = null;
        String res;
        String mkdirCmd = "mkdir -p";
        String lsDependenceCommand = String.format("kubectl exec %s -n %s --token=%s --server=%s --insecure-skip-tls-verify=true -- %s %s",
                fileUploadPodName, CommonConstant.KUBE_SYSTEM, topCluster.getMachineToken(), server, mkdirCmd, remoteDirectory);

        try {
            p = Runtime.getRuntime().exec(lsDependenceCommand);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            while ((res = stdInput.readLine()) != null) {
                logger.info("执行创建目录命令：" + res);
            }
            while ((res = stdError.readLine()) != null) {
                logger.error("执行创建目录命令错误：" + res);
            }
            int runningStatus = p.waitFor();
            logger.info("执行创建目录命令结果：" + runningStatus);
        }finally{
            if(p != null){
                p.destroy();
            }
        }

        //创建pv
        PersistentVolume persistentVolume = new PersistentVolume();
        // 设置metadata
        ObjectMeta metadata = new ObjectMeta();
        metadata.setName(pvName);
        Map<String, Object> labels = new HashMap<>();
        labels.put("name",dependenceDto.getName());
        labels.put("projectId", dependenceDto.isCommon()? null : dependenceDto.getProjectId());
        labels.put("clusterId", dependenceDto.getClusterId());
        labels.put("common", dependenceDto.isCommon() ? "true" : "false");
        labels.put(CommonConstant.USERNAME, session.getAttribute(CommonConstant.USERNAME));
        metadata.setLabels(labels);
        // 设置spec
        PersistentVolumeSpec spec = new PersistentVolumeSpec();
        Map<String, Object> cap = new HashMap<>();
        cap.put(CommonConstant.STORAGE, "10Gi");
        spec.setCapacity(cap);
        spec.setPersistentVolumeReclaimPolicy(CommonConstant.PV_RETAIN);
        NFSVolumeSource nfs = new NFSVolumeSource();
        // 设置nfs地址
        nfs.setPath(nfsPath);
        nfs.setServer(dependenceDto.getNfsServer());
        spec.setNfs(nfs);
        List<String> accessModes = new ArrayList<>();
        accessModes.add(CommonConstant.READWRITEMANY);
        spec.setAccessModes(accessModes);
        persistentVolume.setMetadata(metadata);
        persistentVolume.setSpec(spec);
        persistentVolume.setApiVersion("v1");
        persistentVolume.setKind(CommonConstant.PERSISTENTVOLUME);

        try {
            ActionReturnUtil result = pvService.addPv(persistentVolume, topCluster);
        } catch (Exception e) {
            if (e.getMessage().contains("already exists")) {
                throw new MarsRuntimeException(ErrorCodeMessage.DEPENDENCE_NAME_DUPLICATE);
            } else {
                throw new MarsRuntimeException(ErrorCodeMessage.DEPENDENCE_CREATE_FAIL);
            }
        }

        //创建pvc
        PersistentVolumeClaim persistentVolumeClaim = new PersistentVolumeClaim();
        ObjectMeta pvcMetadata = new ObjectMeta();
        pvcMetadata.setName(pvName);
        labels = new HashMap<>();
        labels.put("name", pvName);
        pvcMetadata.setLabels(labels);
        persistentVolumeClaim.setMetadata(pvcMetadata);
        PersistentVolumeClaimSpec pvcSpec = new PersistentVolumeClaimSpec();
        List<String> modes = new ArrayList<String>();
        modes.add(CommonConstant.READWRITEMANY);
        pvcSpec.setAccessModes(modes);
        pvcSpec.setVolumeName(pvName);

        ResourceRequirements resourceRequirements = new ResourceRequirements();
        Map<String, Object> limits = new HashMap<String, Object>();
        limits.put("storage", "10Gi");
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
        String pvName;
        String remoteDirectory;
        if(StringUtils.isNotBlank(clusterId)){
            String projectName = projectService.getProjectNameByProjectId(projectId);
            String clusterName = clusterService.getClusterNameByClusterId(clusterId);
            pvName = CommonConstant.DEPENDENCE_PREFIX + "." + projectName + "-" + clusterName + "-" + name;
            remoteDirectory = "/nfs/" + projectName + "-" + clusterName + "-" + name;
        }else{
            pvName = CommonConstant.DEPENDENCE_PREFIX + "." +  name;
            remoteDirectory = "/nfs/" + name;
        }
        Stage stage = new Stage();
        stage.setDependences("\"pvName\":\"" + pvName + "\"");
        List stageList = stageService.selectByExample(stage);
        if(CollectionUtils.isNotEmpty(stageList)){
            throw new MarsRuntimeException(ErrorCodeMessage.DEPENDENCE_USED);
        }
        PersistentVolume pv = pvService.getPvByName(pvName, topCluster);
        if(pv == null){
            throw new MarsRuntimeException(ErrorCodeMessage.DEPENDENCE_ALREADY_DELETED);
        }
        Map<String, Object> labels = pv.getMetadata().getLabels();
        String createUser = (String)labels.get(CommonConstant.USERNAME);
        String username = (String)session.getAttribute(CommonConstant.USERNAME);
        if(StringUtils.isBlank(clusterId)) {
            if(StringUtils.isNotBlank(username) && !username.equals(createUser)){
                if(!userService.checkCurrentUserIsAdmin()){
                    throw new MarsRuntimeException(ErrorCodeMessage.DEPENDENCE_NO_PRIVILEGE_DELETE);
                }
            }
        }
        pvService.delPvByName(pvName, topCluster);

        Map<String, Object> query = new HashMap<>();
        query.put(CommonConstant.LABELSELECTOR, "name=" + pvName);
        pvcService.doSepcifyPVC(CommonConstant.CICD_NAMESPACE, query, HTTPMethod.DELETE, topCluster);

        Pod fileUploadPod = this.getFileUploadPod(topCluster);
        String fileUploadPodName = fileUploadPod.getMetadata().getName();

        deleteFile(fileUploadPodName, remoteDirectory, topCluster);

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

        String remoteDirectory;
        File tmpDirectory;
        String localFile;
        MultipartFile file = dependenceFileDto.getFile();
        String fileName = file.getOriginalFilename();
        String projectName = null;
        if(StringUtils.isNotBlank(dependenceFileDto.getClusterId())){
            projectName = projectService.getProjectNameByProjectId(dependenceFileDto.getProjectId());
            String clusterName = clusterService.getClusterNameByClusterId(dependenceFileDto.getClusterId());
            remoteDirectory = "/nfs/" + projectName + "-" + clusterName + "-" + dependenceFileDto.getDependenceName() + "/" + dependenceFileDto.getPath() + "/";
            tmpDirectory = new File(uploadPath + "/" + projectName);
            localFile = uploadPath + "/" + projectName + "/" + fileName;
        }else{
            remoteDirectory = "/nfs/" + dependenceFileDto.getDependenceName() + "/" + dependenceFileDto.getPath() + "/";
            tmpDirectory = new File(uploadPath + "/" + dependenceFileDto.getDependenceName());
            localFile = uploadPath + "/" + fileName;
        }

        //创建临时目录下的项目目录

        if (!tmpDirectory.exists()) {
            tmpDirectory.mkdirs();
        }

        //文件上传至临时目录

        File tmpFile = new File(localFile);

        file.transferTo(tmpFile);

        Pod fileUploadPod = this.getFileUploadPod(topCluster);
        String fileUploadPodName = fileUploadPod.getMetadata().getName();

        ProcessBuilder proc;
        Process p = null;
        String res;

        String uploadDependenceCommand = String.format("kubectl cp %s %s/%s:%s --token=%s --server=%s --insecure-skip-tls-verify=true",
                localFile, CommonConstant.KUBE_SYSTEM, fileUploadPodName, remoteDirectory, topCluster.getMachineToken(), server);
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
                    if(res.contains("in the future")){
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
                    logger.error("执行解压文件脚本错误：" + res);
                    error = true;
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
        String fileUploadPodName = getFileUploadPod(topCluster).getMetadata().getName();

        String remoteDependencedir = getRemoteDependenceDir(dependenceName, projectId, clusterId);
        String targetPath = remoteDependencedir + "/" + path;

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

    /**
     * 获取文件上传pod
     *
     * @param cluster 集群对象
     * @return
     * @throws Exception
     */
    private Pod getFileUploadPod(Cluster cluster) throws Exception {
        K8SURL url = new K8SURL();
        url.setNamespace(CommonConstant.KUBE_SYSTEM);
        url.setResource(Resource.POD);
        String label = CommonConstant.FILE_UPLOAD_POD_LABEL;
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
    public void deleteDependenceByProject(String projectId) {
        try {
            Cluster topCluster = clusterService.getPlatformCluster();
            String label = "projectId = " + projectId;
            K8SClientResponse response = pvService.listPvBylabel(label, topCluster);
            if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
                PersistentVolumeList persistentVolumeList = K8SClient.converToBean(response, PersistentVolumeList.class);
                List<PersistentVolume> items = persistentVolumeList.getItems();
                for (PersistentVolume pv : items) {
                    String pvName = pv.getMetadata().getName();
                    String remoteDirectory = "/nfs/" + pvName.replace(CommonConstant.DEPENDENCE_PREFIX + ".", "");
                    pvService.delPvByName(pvName, topCluster);

                    Map<String, Object> query = new HashMap<>();
                    query.put(CommonConstant.LABELSELECTOR, "name=" + pvName);
                    pvcService.doSepcifyPVC(CommonConstant.CICD_NAMESPACE, query, HTTPMethod.DELETE, topCluster);

                    Pod fileUploadPod = this.getFileUploadPod(topCluster);
                    String fileUploadPodName = fileUploadPod.getMetadata().getName();
                    deleteFile(fileUploadPodName, remoteDirectory, topCluster);
                }
            }
        }catch(Exception e){
            logger.error("删除依赖目录失败：" + e);
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

        String remoteDependenceDir = getRemoteDependenceDir(dependenceName, projectId, clusterId);
        String targetDir = remoteDependenceDir + "/" +path;

        Pod fileUploadPod = getFileUploadPod(topCluster);
        String fileUploadPodName = fileUploadPod.getMetadata().getName();
        Process p = null;
        boolean error = false;
        try {
            String lsCmd;
            if(isRecurse){
                lsCmd = "ls -lhRe ";
            }else {
                lsCmd = "ls -lhe ";
            }
            String lsDependenceCommand = String.format("kubectl exec %s -n %s --token=%s --server=%s --insecure-skip-tls-verify=true -- %s %s",
                    fileUploadPodName, CommonConstant.KUBE_SYSTEM, topCluster.getMachineToken(), server, lsCmd, targetDir);
            p = Runtime.getRuntime().exec(lsDependenceCommand);

            String res;
            List<Map<String, Object>> files = new ArrayList();
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));



            String tmpParentDirectory = "/";
            while ((res = stdInput.readLine()) != null) {
                Map<String, Object> file = new HashMap();
                String[] fileAttributes = res.split("\\s+", 11);
                if (fileAttributes.length < 9) {
                    if(fileAttributes[0].endsWith(":")){
                        tmpParentDirectory = fileAttributes[0].substring(fileAttributes[0].lastIndexOf(remoteDependenceDir)+remoteDependenceDir.length());
                        tmpParentDirectory = tmpParentDirectory.substring(0,tmpParentDirectory.length()-1);
                    }
                    continue;
                }
                if(fileAttributes[0].startsWith(CommonConstant.DIRECTORY_TYPE)){
                    file.put("type", "directory");
                    file.put("isDirectory",true);
                } else {
                    file.put("isDirectory", false);
                }

                //处理无后缀的文件
                if(!(boolean)file.get("isDirectory") && fileAttributes[10].contains(".")){
                    file.put("type", fileAttributes[10].substring(fileAttributes[10].lastIndexOf(".")+1));
                    file.put("prefixFilename", fileAttributes[10].substring(0, fileAttributes[10].lastIndexOf(".")));
                }else if(!(boolean)file.get("isDirectory") && !fileAttributes[10].contains(".")){
                    file.put("type", "");
                    file.put("prefixFilename", fileAttributes[10]);
                }


                file.put("fileName", fileAttributes[10]);

                char c = fileAttributes[4].charAt(fileAttributes[4].length()-1);
                if( c >= '0' && c <= '9'){
                    fileAttributes[4] += "Byte";
                }
                file.put("size", fileAttributes[4]);
                file.put("lastModified", fileAttributes[9] + "-" + fileAttributes[6] + "-" + fileAttributes[7] + " " + fileAttributes[8]) ;
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

    private String getRemoteDependenceDir(String dependenceName, String projectId, String clusterId) throws Exception {
        String targetDir;
        if(StringUtils.isNotBlank(clusterId)){
            String projectName = projectService.getProjectNameByProjectId(projectId);
            String clusterName = clusterService.getClusterNameByClusterId(clusterId);
            targetDir = "/nfs/" + projectName + "-" + clusterName + "-" + dependenceName;
        }else{
            targetDir = "/nfs/" + dependenceName;
        }

        return targetDir;

    }
}
