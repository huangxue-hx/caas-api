package com.harmonycloud.service.platform.serviceImpl.ci;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.dao.ci.DockerFileMapper;
import com.harmonycloud.dao.ci.bean.Depends;
import com.harmonycloud.dao.ci.bean.DockerFile;
import com.harmonycloud.dao.ci.bean.DockerFilePage;
import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.dto.cicd.DockerFileDto;
import com.harmonycloud.k8s.bean.UnversionedStatus;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import com.harmonycloud.service.platform.service.ci.DockerFileService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service

public class DockerFileServiceImpl implements DockerFileService {

    @Autowired
    HttpSession session;

    @Autowired
    private DockerFileMapper dockerFileMapper;

    @Override
    public List<DockerFile> findByAll(DockerFile dockerFile) {
        return dockerFileMapper.findByAll(dockerFile);
    }

    @Override
    public PageInfo<DockerFilePage> findByList(DockerFileDto dockerFileDTO) {
        DockerFile dockerFile = new DockerFile();
        dockerFile.setName(dockerFileDTO.getName());
        dockerFile.setTenant(dockerFileDTO.getTenant());
        PageHelper.startPage(dockerFileDTO.getCurrentPage(), dockerFileDTO.getPageSize());
        List<DockerFilePage> dockerFiles = dockerFileMapper.findPageByAll(dockerFile);
        if(dockerFiles!=null && dockerFiles.size()>0){
            for(DockerFilePage dockerFilePage: dockerFiles){
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
        dockerFileMapper.insertDockerFile(dockerFile);
        ActionReturnUtil result = createConfigMap(dockerFile);
        if(!result.isSuccess()){
            throw new Exception("创建失败");
        }

    }

    @Override
    public void updateDockerFile(DockerFile dockerFile) throws Exception {
        dockerFileMapper.updateDockerFile(dockerFile);
        ActionReturnUtil result = updateConfigMap(dockerFile);
        if(!result.isSuccess()){
            throw new Exception("修改失败");
        }
    }

    @Override
    public void deleteDockerFile(DockerFile dockerFile) throws Exception {
        dockerFileMapper.deleteDockerFile(dockerFile);
        ActionReturnUtil result = deleteConfigMap(dockerFile);
        if(!result.isSuccess()){
            throw new Exception("删除失败");
        }
    }

    @Override
    public DockerFile selectDockerFile(DockerFile dockerFile) {
        return dockerFileMapper.selectDockerFile(dockerFile);
    }

    @Override
    public List<DockerFile> selectNameAndTenant(DockerFile dockerFile) {
        return dockerFileMapper.selectNameAndTenant(dockerFile);
    }

    private ActionReturnUtil createConfigMap(DockerFile dockerFile) throws Exception{
        K8SURL url = new K8SURL();
        url.setNamespace(CommonConstant.CICD_NAMESPACE).setResource(Resource.CONFIGMAP);
        Map<String, Object> bodys = new HashMap<String, Object>();
        Map<String, Object> meta = new HashMap<String, Object>();
        meta.put("namespace", CommonConstant.CICD_NAMESPACE);
        meta.put("name", String.valueOf(dockerFile.getId()));
        bodys.put("metadata", meta);
        Map<String, Object> data = new HashMap<String, Object>();
        data.put(dockerFile.getName(), dockerFile.getContent());
        bodys.put("data", data);
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Content-type", "application/json");
        Cluster cluster = (Cluster) session.getAttribute("currentCluster");
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.POST, headers, bodys, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            UnversionedStatus status = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithData(status.getMessage());
        }
        return ActionReturnUtil.returnSuccess();
    }

    private ActionReturnUtil updateConfigMap(DockerFile dockerFile) throws Exception{
        K8SURL url = new K8SURL();
        url.setNamespace(CommonConstant.CICD_NAMESPACE).setResource(Resource.CONFIGMAP).setName(String.valueOf(dockerFile.getId()));
        Map<String, Object> bodys = new HashMap<String, Object>();
        Map<String, Object> meta = new HashMap<String, Object>();
        meta.put("namespace", CommonConstant.CICD_NAMESPACE);
        meta.put("name", String.valueOf(dockerFile.getId()));
        bodys.put("metadata", meta);
        Map<String, Object> data = new HashMap<String, Object>();
        data.put(dockerFile.getName(), dockerFile.getContent());
        bodys.put("data", data);
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Content-type", "application/json");
        Cluster cluster = (Cluster) session.getAttribute("currentCluster");
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.PUT, headers, bodys, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            UnversionedStatus status = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithData(status.getMessage());
        }
        return ActionReturnUtil.returnSuccess();
    }

    private ActionReturnUtil deleteConfigMap(DockerFile dockerFile) throws Exception{
        K8SURL url = new K8SURL();
        url.setNamespace(CommonConstant.CICD_NAMESPACE).setResource(Resource.CONFIGMAP).setName(String.valueOf(dockerFile.getId()));
        Map<String, Object> bodys = new HashMap<String, Object>();
        Map<String, Object> meta = new HashMap<String, Object>();
        meta.put("namespace", CommonConstant.CICD_NAMESPACE);
        meta.put("name", String.valueOf(dockerFile.getId()));
        bodys.put("metadata", meta);
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Content-type", "application/json");
        Cluster cluster = (Cluster) session.getAttribute("currentCluster");
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.DELETE, headers, bodys, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            UnversionedStatus status = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithData(status.getMessage());
        }
        return ActionReturnUtil.returnSuccess();
    }

}
