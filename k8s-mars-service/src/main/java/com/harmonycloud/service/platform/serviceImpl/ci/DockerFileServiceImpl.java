package com.harmonycloud.service.platform.serviceImpl.ci;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.harmonycloud.common.util.StringUtil;
import com.harmonycloud.dao.ci.DockerFileMapper;
import com.harmonycloud.dao.ci.bean.Depends;
import com.harmonycloud.dao.ci.bean.DockerFile;
import com.harmonycloud.dao.ci.bean.DockerFilePage;
import com.harmonycloud.dto.cicd.DockerFileDto;
import com.harmonycloud.service.platform.service.ci.DockerFileService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DockerFileServiceImpl implements DockerFileService {

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
    public void insertDockerFile(DockerFile dockerFile) {
        dockerFileMapper.insertDockerFile(dockerFile);
    }

    @Override
    public void updateDockerFile(DockerFile dockerFile) {
        dockerFileMapper.updateDockerFile(dockerFile);
    }

    @Override
    public void deleteDockerFile(DockerFile dockerFile) {
        dockerFileMapper.deleteDockerFile(dockerFile);
    }

    @Override
    public DockerFile selectDockerFile(DockerFile dockerFile) {
        return dockerFileMapper.selectDockerFile(dockerFile);
    }

    @Override
    public List<DockerFile> selectNameAndTenant(DockerFile dockerFile) {
        return dockerFileMapper.selectNameAndTenant(dockerFile);
    }
}
