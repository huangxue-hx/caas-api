package com.harmonycloud.api.ci;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.ci.bean.Job;
import com.harmonycloud.dto.cicd.JobDto;
import com.harmonycloud.dto.cicd.ParameterDto;
import com.harmonycloud.service.platform.service.ci.JobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;



/**
 * Created by anson on 17/5/27.
 */

@RequestMapping("/tenants/{tenantId}/projects/{projectId}/cicdjobs")
@Controller
public class JobController {

    @Autowired
    JobService jobService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());


    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public ActionReturnUtil addJob(@PathVariable("tenantId") String tenantId,
                                   @PathVariable("projectId") String projectId,
                                   @RequestBody JobDto jobDto) throws Exception{
//        logger.info("create job.");
        jobDto.setTenantId(tenantId);
        jobDto.setProjectId(projectId);
        return ActionReturnUtil.returnSuccessWithData(jobService.createJob(jobDto));
    }


    @RequestMapping(value = "{jobId}", method = RequestMethod.DELETE)
    @ResponseBody
    public ActionReturnUtil deleteJob(@PathVariable("jobId") Integer jobId) throws Exception {
//        logger.info("delete job.");
        jobService.deleteJob(jobId);
        return ActionReturnUtil.returnSuccess();
    }

    @RequestMapping(value = "/{jobId}/rename", method = RequestMethod.POST)
    @ResponseBody
    public ActionReturnUtil renameJob(@PathVariable("jobId") Integer jobId, @RequestParam("newName") String newName) throws Exception {
//        logger.info("rename job.");
        jobService.rename(jobId, newName);
        return ActionReturnUtil.returnSuccess();
    }

    @RequestMapping(value = "/{jobId}", method = RequestMethod.PUT)
    @ResponseBody
    public ActionReturnUtil updateJob(@PathVariable("jobId") Integer jobId, @RequestBody JobDto jobDto) throws Exception {
        jobDto.setId(jobId);
        jobService.updateJob(jobDto);
        return ActionReturnUtil.returnSuccess();
    }

    @RequestMapping(value = "/validateName", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil validateName( @PathVariable("projectId") String projectId,
                                          @RequestParam(value = "clusterId" ) String clusterId,
                                          @RequestParam(value="name") String jobName) throws Exception {
//        logger.info("validate job name.");
        jobService.validateName(jobName, projectId, clusterId);
        return ActionReturnUtil.returnSuccess();
    }


    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil listJob(@PathVariable("projectId") String projectId,
                                    @RequestParam(value = "clusterId" ,required = false) String clusterId,
                                    @RequestParam(value = "type",required = false) String type,
                                    @RequestParam(value="name", required = false) String jobName) throws Exception{
//        logger.info("get job list.");
        return ActionReturnUtil.returnSuccessWithData(jobService.getJobList(projectId, clusterId, type, jobName));
    }

    @RequestMapping(value = "{jobId}", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil getJob(@PathVariable("jobId") Integer jobId) throws Exception {
//        logger.info("get job detail.");
        return jobService.getJobDetail(jobId);
    }

    @RequestMapping(value = "/{jobId}/result", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil listBuildResult(@PathVariable("jobId") Integer jobId,
                                            @RequestParam(value="pageSize", required = false, defaultValue = "10") Integer pageSize,
                                            @RequestParam(value="page", required = false, defaultValue = "1") Integer page) throws Exception {
//        logger.info("get build detail.");
        return jobService.getBuildList(jobId, pageSize, page);
    }

    @RequestMapping(value = "/{jobId}/start", method = RequestMethod.PATCH)
    @ResponseBody
    public ActionReturnUtil startBuild(@RequestBody ParameterDto parameterDto) throws Exception {
//        logger.info("build job.");
        jobService.build(parameterDto.getJobId(), parameterDto.getParameters(), null, null);
        return ActionReturnUtil.returnSuccess();
    }

    @RequestMapping(value = "/{jobId}/stop", method = RequestMethod.PATCH)
    @ResponseBody
    public ActionReturnUtil stopBuild(@PathVariable("jobId") Integer jobId,
                                      @RequestParam(value="buildNum") String buildNum) throws Exception{
//        logger.info("stop build.");
        jobService.stopBuild(jobId, buildNum);
        return ActionReturnUtil.returnSuccess();
    }

    @RequestMapping(value = "/{jobId}/result/{buildNum}/delete", method = RequestMethod.DELETE)
    @ResponseBody
    public ActionReturnUtil deleteBuildResult(@PathVariable("jobId") Integer jobId,
                                              @PathVariable("buildNum") String buildNum) throws Exception {
//        logger.info("delete build.");
        return jobService.deleteBuild(jobId, buildNum);
    }

    @RequestMapping(value = "/validateCredential", method = RequestMethod.POST)
    @ResponseBody
    public ActionReturnUtil validateCredential(@RequestParam(value="type") String repositoryType, @RequestParam(value="repositoryUrl") String repositoryUrl, @RequestParam(value="username") String username, @RequestParam(value="password") String password){
//        logger.info("validate repository credentials.");
        return jobService.validateCredential(repositoryType, repositoryUrl, username, password);
    }


    @RequestMapping(value = "/{jobId}/notification", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil getNotification(@PathVariable("jobId") Integer jobId) throws Exception {
        try {
            return jobService.getNotification(jobId);
        } catch (Exception e) {
            return ActionReturnUtil.returnErrorWithMsg(e.getMessage());
        }
    }

    @RequestMapping(value = "/{jobId}/notification", method = RequestMethod.PUT)
    @ResponseBody
    public ActionReturnUtil updateNotification(@RequestBody JobDto jobDto) throws Exception {
        return jobService.updateNotification(jobDto);
    }


    @RequestMapping(value = "/{jobId}/yaml", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil getYaml(@PathVariable("jobId") Integer jobId) throws Exception {
        return ActionReturnUtil.returnSuccessWithData(jobService.getYaml(jobId));
    }

    @RequestMapping(value = "/{jobId}/log", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil getJobLog(@PathVariable("jobId") Integer jobId, @RequestParam(value="buildNum") Integer buildNum) throws Exception {
        return ActionReturnUtil.returnSuccessWithData(jobService.getJobLog(jobId, buildNum));
    }

    @RequestMapping(value = "/{jobId}/images", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil listDeployImages(@PathVariable("jobId") Integer jobId){
        return ActionReturnUtil.returnSuccessWithData(jobService.listDeployImage(jobId));
    }

    @RequestMapping(value = "/{jobId}/stageresult", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil getStageResult(@PathVariable("jobId")Integer jobId, @RequestParam(value="buildNum")Integer buildNum, @RequestParam(value="status")String status) throws Exception{
        return ActionReturnUtil.returnSuccessWithData(jobService.getStageBuildResult(jobId, buildNum, status));
    }

}


