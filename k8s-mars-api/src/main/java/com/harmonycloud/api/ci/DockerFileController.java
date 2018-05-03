package com.harmonycloud.api.ci;

import com.github.pagehelper.PageInfo;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.AssertUtil;
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
@RequestMapping("/tenants/{tenantId}/projects/{projectId}/dockerfile")
public class DockerFileController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private DockerFileService dockerFileService;

    @RequestMapping(method = RequestMethod.GET)
    public ActionReturnUtil listDockerFile(@PathVariable("projectId") String projectId,
                                           @RequestParam(value = "clusterId" , required = false) String clusterId) throws Exception{
//        logger.info("dockerfile getAllList.");
        DockerFile dockerFile = new DockerFile();
        dockerFile.setProjectId(projectId);
        dockerFile.setClusterId(clusterId);
        List<DockerFile> dockerFiles = dockerFileService.findByAll(dockerFile);
        return ActionReturnUtil.returnSuccessWithData(dockerFiles);
    }

    @RequestMapping(value = "/page",method = RequestMethod.GET)
    public ActionReturnUtil listDockerFileByPage(@PathVariable("projectId") String projectId,
                                                 @RequestParam(value = "clusterId", required = false) String clusterId,
                                                 @RequestParam(value = "currentPage") Integer currentPage,
                                                 @RequestParam(value = "pageSize") Integer pageSize,
                                                 @RequestParam(value = "name", required = false) String name) throws Exception {
//        logger.info("dockerfile getPageList.");
        DockerFileDto dockerFileDTO = new DockerFileDto();
        dockerFileDTO.setProjectId(projectId);
        dockerFileDTO.setClusterId(clusterId);
        dockerFileDTO.setCurrentPage(currentPage);
        dockerFileDTO.setPageSize(pageSize);
        dockerFileDTO.setName(name);
        PageInfo<DockerFilePage> pageInfo = dockerFileService.findByList(dockerFileDTO);
        return ActionReturnUtil.returnSuccessWithData(pageInfo);
    }

    @RequestMapping(method = RequestMethod.POST)
    public ActionReturnUtil addDockerFile(@PathVariable("projectId") String projectId,@RequestBody DockerFile dockerFile) throws Exception {
        AssertUtil.notNull(dockerFile);
        dockerFile.setProjectId(projectId);
//        logger.info("dockerfile addDockerFile.");
        if(StringUtils.isBlank(dockerFile.getName())){
            dockerFile.setName("未命名-"+ DateUtil.DateToString(new Date(),"yyyyMMddHHssmm"));
        }
        dockerFile.setCreateTime(new Date());
        dockerFile.setUpdateTime(new Date());
        dockerFileService.insertDockerFile(dockerFile);
        return ActionReturnUtil.returnSuccessWithData(dockerFile.getId());
    }

    @RequestMapping(method = RequestMethod.PUT)
    public ActionReturnUtil updateDockerFile(@RequestBody DockerFile dockerFile) throws Exception {
//        logger.info("dockerfile updateDockerFile.");
        dockerFile.setUpdateTime(new Date());
        dockerFileService.updateDockerFile(dockerFile);
        return ActionReturnUtil.returnSuccessWithData(dockerFile.getId());
    }

    @RequestMapping(value = "/{dockerfileId}",method = RequestMethod.DELETE)
    public ActionReturnUtil deleteDockerFile(@PathVariable("dockerfileId") Integer dockerfileId) throws Exception {
//        logger.info("dockerfile deleteDockerFile.");
        dockerFileService.deleteDockerFile(dockerfileId);
        return ActionReturnUtil.returnSuccess();
    }

    @RequestMapping(value = "/{dockerfileId}",method = RequestMethod.GET)
    public ActionReturnUtil getDockerFile(@PathVariable("dockerfileId") Integer dockerfileId){
//        logger.info("dockerfile getDockerFile.");
        return ActionReturnUtil.returnSuccessWithData(dockerFileService.selectDockerFileById(dockerfileId));
    }
}
