package com.harmonycloud.api.ci;

import com.github.pagehelper.PageInfo;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.dao.ci.bean.DockerFile;
import com.harmonycloud.dao.ci.bean.DockerFilePage;
import com.harmonycloud.dto.cicd.DockerFileDto;
import com.harmonycloud.service.platform.service.ci.DockerFileService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/cicd/dockerfile")
public class DockerFileController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private DockerFileService dockerFileService;

    @RequestMapping(value = "/allList", method = RequestMethod.GET)
    public ActionReturnUtil getAllList(String tenant){
        logger.info("dockerfile getAllList.");
        DockerFile dockerFile = new DockerFile();
        dockerFile.setTenant(tenant);
        List<DockerFile> dockerFiles = dockerFileService.findByAll(dockerFile);
        return ActionReturnUtil.returnSuccessWithData(dockerFiles);
    }

    @RequestMapping(value = "/pageList",method = RequestMethod.POST)
    public ActionReturnUtil getPageList(@RequestBody DockerFileDto dockerFileDTO){
        logger.info("dockerfile getPageList.");
        PageInfo<DockerFilePage> pageInfo = dockerFileService.findByList(dockerFileDTO);
        return ActionReturnUtil.returnSuccessWithData(pageInfo);
    }

    @RequestMapping(value = "/add",method = RequestMethod.POST)
    public ActionReturnUtil addDockerFile(@RequestBody DockerFile dockerFile) throws Exception {
        logger.info("dockerfile addDockerFile.");
        if(dockerFile==null){
            return ActionReturnUtil.returnErrorWithMsg("请输入名称和内容");
        }
        if(StringUtils.isBlank(dockerFile.getName())){
            dockerFile.setName("未命名-"+ DateUtil.DateToString(new Date(),"yyyyMMddHHssmm"));
        }
        List<DockerFile> dockerFiles = dockerFileService.selectNameAndTenant(dockerFile);
        if(dockerFiles!=null && dockerFiles.size()>0){
            if(dockerFiles.size()>1){
                return ActionReturnUtil.returnErrorWithMsg("dockerfile已存在该名称");
            }else{
                if(dockerFiles.get(0).getName().equals(dockerFile.getName())){
                    return ActionReturnUtil.returnErrorWithMsg("dockerfile已存在该名称");
                }
            }
        }
        dockerFile.setCreateTime(new Date());
        dockerFile.setUpdateTime(new Date());
    dockerFileService.insertDockerFile(dockerFile);
    return ActionReturnUtil.returnSuccessWithData(dockerFile.getId());
    }

    @RequestMapping(value = "/update",method = RequestMethod.PUT)
    public ActionReturnUtil updateDockerFile(@RequestBody DockerFile dockerFile) throws Exception {
        logger.info("dockerfile updateDockerFile.");
        if(StringUtils.isNotBlank(dockerFile.getName())){
        List<DockerFile> dockerFiles = dockerFileService.selectNameAndTenant(dockerFile);
        if(dockerFiles!=null && dockerFiles.size()>0){
            if(dockerFiles.size()>1){
                return ActionReturnUtil.returnErrorWithMsg("dockerfile已存在该名称");
            }else{
                if(dockerFiles.get(0).getName().equals(dockerFile.getName()) && !dockerFiles.get(0).getId().equals(dockerFile.getId())){
                    return ActionReturnUtil.returnErrorWithMsg("dockerfile已存在该名称");
                }
            }
        }
        }
        dockerFile.setUpdateTime(new Date());
        dockerFileService.updateDockerFile(dockerFile);
        return ActionReturnUtil.returnSuccessWithData(dockerFile.getId());
    }

    @RequestMapping(value = "/{id}",method = RequestMethod.DELETE)
    public ActionReturnUtil deleteDockerFile(@PathVariable Integer id,@RequestBody DockerFile dockerFile) throws Exception {
        logger.info("dockerfile deleteDockerFile.");
        dockerFile.setId(id);
        dockerFileService.deleteDockerFile(dockerFile);
        return ActionReturnUtil.returnSuccess();
    }

    @RequestMapping(value = "/{id}",method = RequestMethod.GET)
    public ActionReturnUtil getDockerFile(@PathVariable Integer id, String tenant){
        logger.info("dockerfile getDockerFile.");
        DockerFile params = new DockerFile();
        params.setId(id);
        params.setTenant(tenant);
        DockerFile dockerFile = dockerFileService.selectDockerFile(params);
        if(dockerFile!=null){
            return ActionReturnUtil.returnSuccessWithData(dockerFile);
        }
        return ActionReturnUtil.returnSuccess();
    }
}
