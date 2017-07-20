package com.harmonycloud.api.ci;

import com.harmonycloud.common.util.*;
import com.harmonycloud.dao.ci.bean.Job;
import com.harmonycloud.dto.cicd.JobDto;
import com.harmonycloud.service.platform.service.ci.JobService;
import com.harmonycloud.service.platform.serviceImpl.ci.JobServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;




/**
 * Created by anson on 17/5/27.
 */

@RequestMapping("/cicd/job")
@Controller
public class JobController {

    @Autowired
    JobService jobService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());


    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public ActionReturnUtil createJob(@RequestBody JobDto jobDto){
        logger.info("create job.");
        try {
            return jobService.createJob(jobDto);
        } catch (Exception e) {
            return ActionReturnUtil.returnError();
        }
    }

    @RequestMapping(method = RequestMethod.PUT)
    @ResponseBody
    public ActionReturnUtil updateJob(@RequestBody Job job){
        logger.info("update job.");
        return jobService.updateJob(job);
    }

    @RequestMapping(method = RequestMethod.DELETE)
    @ResponseBody
    public ActionReturnUtil deleteJob(@RequestParam(value="id") Integer id) throws Exception {
        logger.info("delete job.");
        try {
            return jobService.deleteJob(id);
        } catch (Exception e) {
            return ActionReturnUtil.returnErrorWithMsg(e.getMessage());
        }
    }

    @RequestMapping(value = "/nameValidate", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil nameValidate(@RequestParam(value="name") String jobName, @RequestParam(value="tenant") String tenantName){
        logger.info("validate job name.");
        return jobService.nameValidate(jobName, tenantName);
    }


    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil getJobList(@RequestParam(value="tenant") String tenantName){
        logger.info("get job list.");
        return jobService.getJobList(tenantName);
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil getJobDetail(@RequestParam(value="id") Integer id){
        logger.info("get job detail.");
        return jobService.getJobDetail(id);
    }

    @RequestMapping(value = "/buildDetail", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil getBuildDetail(@RequestParam(value="tenant") String tenantName, @RequestParam(value="name") String jobName, @RequestParam(value="buildNum") String buildNum){
        logger.info("get build detail.");
        return jobService.getBuildDetail(tenantName, jobName, buildNum);
    }

    @RequestMapping(value = "/build", method = RequestMethod.POST)
    @ResponseBody
    public ActionReturnUtil build(@RequestParam(value="id") Integer id){
        logger.info("build job.");
        return jobService.build(id);
    }

    @RequestMapping(value = "/stopBuild", method = RequestMethod.POST)
    @ResponseBody
    public ActionReturnUtil stopBuild(@RequestParam(value="name") String jobName, @RequestParam(value="tenant") String tenantName, @RequestParam(value="buildNum") String buildNum){
        logger.info("stop build.");
        return jobService.stopBuild(jobName, tenantName, buildNum);
    }

    @RequestMapping(value = "/build", method = RequestMethod.DELETE)
    @ResponseBody
    public ActionReturnUtil deleteBuild(@RequestParam(value="name") String jobName, @RequestParam(value="tenant") String tenantName, @RequestParam(value="buildNum") String buildNum){
        logger.info("delete build.");
        return jobService.deleteBuild(jobName, tenantName, buildNum);
    }

    @RequestMapping(value = "/credentialsValidate", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil credentialsValidate(@RequestParam(value="type") String repositoryType, @RequestParam(value="repositoryUrl") String repositoryUrl, @RequestParam(value="username") String username, @RequestParam(value="password") String password){
        logger.info("validate repository credentials.");
        return jobService.credentialsValidate(repositoryType, repositoryUrl, username, password);
    }


    @RequestMapping(value = "/notification", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil getNotification(@RequestParam(value="id") Integer id) throws Exception {
        try {
            return jobService.getNotification(id);
        } catch (Exception e) {
            return ActionReturnUtil.returnErrorWithMsg(e.getMessage());
        }
    }

    @RequestMapping(value = "/notification", method = RequestMethod.PUT)
    @ResponseBody
    public ActionReturnUtil updateNotification(@RequestBody JobDto jobDto){
        return jobService.updateNotification(jobDto);
    }

    @RequestMapping(value = "/trigger", method = RequestMethod.PUT)
    @ResponseBody
    public ActionReturnUtil updateTrigger(@RequestBody Job job){
        return jobService.updateTrigger(job);
    }

    @RequestMapping(value = "/trigger", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil getTrigger(@RequestParam(value="id") Integer id) throws Exception {
        try {
            return jobService.getTrigger(id);
        } catch (Exception e) {
            return ActionReturnUtil.returnErrorWithMsg(e.getMessage());
        }
    }


}


