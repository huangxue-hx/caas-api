package com.harmonycloud.service.platform.serviceImpl.ci;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.common.util.PinyinUtil;
import com.harmonycloud.dao.ci.DockerFileMapper;
import com.harmonycloud.dao.ci.bean.Depends;
import com.harmonycloud.dao.ci.bean.DockerFile;
import com.harmonycloud.dao.ci.bean.DockerFilePage;
import com.harmonycloud.dao.ci.bean.Stage;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.dto.cicd.DockerFileDto;
import com.harmonycloud.k8s.bean.UnversionedStatus;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.common.DataPrivilegeHelper;
import com.harmonycloud.service.platform.service.ci.DockerFileService;
import com.harmonycloud.service.platform.service.ci.StageService;
import com.harmonycloud.service.tenant.ProjectService;
import com.harmonycloud.service.user.RoleLocalService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpSession;
import java.util.*;

@Service
public class DockerFileServiceImpl implements DockerFileService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DockerFileServiceImpl.class);

    @Autowired
    private HttpSession session;

    @Autowired
    private DockerFileMapper dockerFileMapper;

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private RoleLocalService roleLocalService;

    @Autowired
    private StageService stageService;

    @Autowired
    private DataPrivilegeHelper dataPrivilegeHelper;

    @Override
    public List<DockerFile> findByAll(DockerFile dockerFile) throws Exception{
        List<Cluster> clusterList = roleLocalService.listCurrentUserRoleCluster();
        List clusterIdList = new ArrayList();
        for(Cluster cluster:clusterList){
            clusterIdList.add(cluster.getId());
        }
        List<DockerFile> dockerFileList = dockerFileMapper.findByAll(dockerFile);
        Iterator<DockerFile> it = dockerFileList.iterator();
        while(it.hasNext()){
            DockerFile d = it.next();
            if(!clusterIdList.contains(d.getClusterId())){
                it.remove();
            }
        }
        return dockerFileMapper.findByAll(dockerFile);
    }

    @Override
    public PageInfo<DockerFilePage> findByList(DockerFileDto dockerFileDTO) throws Exception {
        DockerFile dockerFile = new DockerFile();
        dockerFile.setName(dockerFileDTO.getName());
        dockerFile.setProjectId(dockerFileDTO.getProjectId());
        dockerFile.setClusterId(dockerFileDTO.getClusterId());
        if(StringUtils.isBlank(dockerFileDTO.getClusterId())){
            dockerFile.setClusterId(null);
        }
        dockerFile.setName(dockerFileDTO.getName());
        if(StringUtils.isBlank(dockerFileDTO.getName())){
            dockerFile.setName(null);
        }
        List clusterIdList = new ArrayList();
        //集群id为空则查询用户权限范围内所有集群的dockerfile
        if(StringUtils.isBlank(dockerFileDTO.getClusterId())){
            List<Cluster> clusterList = roleLocalService.listCurrentUserRoleCluster();
            for(Cluster cluster:clusterList){
                clusterIdList.add(cluster.getId());
            }
        }else{
            clusterIdList.add(dockerFileDTO.getClusterId());
        }
        if (CollectionUtils.isEmpty(clusterIdList)) {
            return new PageInfo<>(Collections.emptyList());
        }
        PageHelper.startPage(dockerFileDTO.getCurrentPage(), dockerFileDTO.getPageSize());
        List<DockerFilePage> dockerFiles = dockerFileMapper.findPageByAll(dockerFile, clusterIdList);

        if(dockerFiles!=null && dockerFiles.size()>0){
            Iterator<DockerFilePage> it = dockerFiles.iterator();
            while(it.hasNext()){
                DockerFilePage dockerFilePage = it.next();
                if(!clusterIdList.contains(dockerFilePage.getClusterId())){
                    it.remove();
                    continue;
                }
                //解析查询结果中使用该dockerfile的流水线和步骤信息
                if(StringUtils.isNotBlank(dockerFilePage.getJobNames()) && StringUtils.isNotBlank(dockerFilePage.getStageNames())){
                    String[] jobIds = dockerFilePage.getJobIds().split(",");
                    String[] jobNames = dockerFilePage.getJobNames().split(",");
                    String[] stageIds = dockerFilePage.getStageIds().split(",");
                    String[] stageNames = dockerFilePage.getStageNames().split(",");
                    if(jobIds!=null &&jobNames!=null && stageIds!=null &&stageNames!=null
                            && jobIds.length == jobNames.length
                            && stageIds.length == stageNames.length
                            && stageIds.length == jobIds.length){
                        List<Depends> depends = new ArrayList<>(jobNames.length);
                        for(int i=0;i<jobNames.length;i++){
                            Depends depend = new Depends();
                            depend.setJobId(Integer.parseInt(jobIds[i]));
                            depend.setJobName(jobNames[i]);
                            depend.setStageId(Integer.parseInt(stageIds[i]));
                            depend.setStageName(stageNames[i]);
                            depend.setClusterId(dockerFilePage.getClusterId());
                            depend.setProjectId(dockerFilePage.getProjectId());
                            dataPrivilegeHelper.filter(depend, true);
                            depends.add(depend);
                        }
                        dockerFilePage.setDepends(depends);
                    }
                }
            }
        }
        PageInfo<DockerFilePage> pageInfo = new PageInfo<>(dockerFiles);
        return pageInfo;
    }

    @Override
    public void insertDockerFile(DockerFile dockerFile) throws Exception {
        projectService.getProjectNameByProjectId(dockerFile.getProjectId());
        clusterService.getClusterNameByClusterId(dockerFile.getClusterId());
        //查重
        List<DockerFile> dockerFiles =  dockerFileMapper.selectDockerFile(dockerFile);
        if(CollectionUtils.isNotEmpty(dockerFiles)){
            throw new MarsRuntimeException(ErrorCodeMessage.DOCKERFILE_NAME_DUPLICATE);
        }
        dockerFileMapper.insertDockerFile(dockerFile);
        //创建condifmap
        this.createConfigMap(dockerFile);
    }

    @Override
    public void updateDockerFile(DockerFile dockerFile) throws Exception {
        if(StringUtils.isBlank(dockerFile.getName())){
            throw new MarsRuntimeException(ErrorCodeMessage.DOCKERFILE_NAME_NOT_BLANK);
        }
        if(dockerFileMapper.selectDockerFileById(dockerFile.getId()) == null){
            throw new MarsRuntimeException(ErrorCodeMessage.DOCKERFILE_NOT_EXIST);
        }
        //查重
        List<DockerFile> dockerFiles = this.selectDockerFile(dockerFile);
        if(CollectionUtils.isNotEmpty(dockerFiles)){
            if(dockerFiles.get(0).getName().equals(dockerFile.getName()) && !dockerFiles.get(0).getId().equals(dockerFile.getId())){
                throw new MarsRuntimeException(ErrorCodeMessage.DOCKERFILE_NAME_DUPLICATE);
            }
        }
        dockerFileMapper.updateDockerFile(dockerFile);
        //更新configmap
        updateConfigMap(dockerFile);
    }

    @Override
    public void deleteDockerFile(Integer id) throws Exception {
        if(dockerFileMapper.selectDockerFileById(id) == null){
            throw new MarsRuntimeException(ErrorCodeMessage.DOCKERFILE_NOT_EXIST);
        }
        //校验dockerfile是否被流水线使用
        Stage stage = new Stage();
        stage.setDockerfileId(id);
        List stageList = stageService.selectByExample(stage);
        if(CollectionUtils.isNotEmpty(stageList)){
            throw new MarsRuntimeException(ErrorCodeMessage.DOCKERFILE_USED_BY_PIPELINE);
        }
        dockerFileMapper.deleteDockerFile(id);
        //删除configmap
        deleteConfigMap(id);
    }

    @Override
    public List<DockerFile> selectDockerFile(DockerFile dockerFile) {
        return dockerFileMapper.selectDockerFile(dockerFile);
    }

    @Override
    public List<DockerFile> selectNameAndTenant(DockerFile dockerFile) {
        return dockerFileMapper.selectNameAndTenant(dockerFile);
    }

    @Override
    public DockerFile selectDockerFileById(Integer id) {
        return dockerFileMapper.selectDockerFileById(id);
    }

    @Override
    public int deleteByClusterId(String clusterId){
        return dockerFileMapper.deleteByClusterId(clusterId);
    }

    @Override
    public void deleteDockerfileByProject(String projectId) {
        DockerFile dockerfileCondition = new DockerFile();
        dockerfileCondition.setProjectId(projectId);
        List<DockerFile> dockerfileList = dockerFileMapper.findByAll(dockerfileCondition);
        for(DockerFile dockerfile : dockerfileList){
            try {
                deleteConfigMap(dockerfile.getId());
            } catch (Exception e) {
                LOGGER.error("删除Dockerfile失败: id:{}, {}", dockerfile.getId(), e);
            }
        }
        dockerFileMapper.deleteByProjectId(projectId);
    }

    private void createConfigMap(DockerFile dockerFile) throws Exception{
        K8SURL url = new K8SURL();
        url.setNamespace(CommonConstant.CICD_NAMESPACE).setResource(Resource.CONFIGMAP);
        Map<String, Object> bodys = new HashMap<String, Object>();
        Map<String, Object> meta = new HashMap<String, Object>();
        meta.put("namespace", CommonConstant.CICD_NAMESPACE);
        meta.put("name", String.valueOf(dockerFile.getId()));
        bodys.put("metadata", meta);
        Map<String, Object> data = new HashMap<String, Object>();
        data.put(PinyinUtil.toPinyin(dockerFile.getName()), dockerFile.getContent());
        bodys.put("data", data);
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Content-type", "application/json");
        Cluster cluster = clusterService.getPlatformCluster();
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.POST, headers, bodys, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            UnversionedStatus status = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
            throw new MarsRuntimeException(status.getMessage());
        }
    }

    private void updateConfigMap(DockerFile dockerFile) throws Exception{
        K8SURL url = new K8SURL();
        url.setNamespace(CommonConstant.CICD_NAMESPACE).setResource(Resource.CONFIGMAP).setName(String.valueOf(dockerFile.getId()));
        Map<String, Object> bodys = new HashMap<String, Object>();
        Map<String, Object> meta = new HashMap<String, Object>();
        meta.put("namespace", CommonConstant.CICD_NAMESPACE);
        meta.put("name", String.valueOf(dockerFile.getId()));
        bodys.put("metadata", meta);
        Map<String, Object> data = new HashMap<String, Object>();
        data.put(PinyinUtil.toPinyin(dockerFile.getName()), dockerFile.getContent());
        bodys.put("data", data);
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Content-type", "application/json");
        Cluster cluster = clusterService.getPlatformCluster();
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.PUT, headers, bodys, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            UnversionedStatus status = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
            throw new MarsRuntimeException(status.getMessage());
        }
    }

    private void deleteConfigMap(Integer id) throws Exception{
        K8SURL url = new K8SURL();
        url.setNamespace(CommonConstant.CICD_NAMESPACE).setResource(Resource.CONFIGMAP).setName(String.valueOf(id));
        Map<String, Object> bodys = new HashMap<String, Object>();
        Map<String, Object> meta = new HashMap<String, Object>();
        meta.put("namespace", CommonConstant.CICD_NAMESPACE);
        meta.put("name", String.valueOf(id));
        bodys.put("metadata", meta);
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Content-type", "application/json");
        Cluster cluster = clusterService.getPlatformCluster();
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.DELETE, headers, bodys, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            UnversionedStatus status = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
            throw new MarsRuntimeException(status.getMessage());
        }
    }


}
