package com.harmonycloud.api.ci;

import com.harmonycloud.common.util.*;
import com.harmonycloud.dao.ci.bean.Job;
import com.harmonycloud.service.platform.service.ci.JobService;
import com.harmonycloud.service.platform.serviceImpl.ci.JobServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;



import javax.servlet.http.HttpSession;


/**
 * Created by anson on 17/5/27.
 */

@RequestMapping("/ci/job")
@Controller
public class JobController {

    @Autowired
    HttpSession session;

    @Autowired
    JobService jobService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());


    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @ResponseBody
    public ActionReturnUtil createJob(@RequestBody Job job){
        logger.info("create job.");
        String username = (String)session.getAttribute("username");
        return jobService.createJob(job, username);
    }

    @RequestMapping(method = RequestMethod.PUT)
    @ResponseBody
    public ActionReturnUtil updateJob(@RequestBody Job job){
        logger.info("update job.");
        return jobService.updateJob(job);
    }

    @RequestMapping(method = RequestMethod.DELETE)
    @ResponseBody
    public ActionReturnUtil deleteJob(@RequestParam(value="name") String jobName, @RequestParam(value="tenant") String tenantName){
        logger.info("delete job.");
        return jobService.deleteJob(jobName, tenantName);
    }

    @RequestMapping(value = "/nameValidate", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil nameValidate(@RequestParam(value="name") String jobName, @RequestParam(value="tenant") String tenantName){
        logger.info("validate job name.");
        return jobService.nameValidate(jobName, tenantName);
    }


    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil getJobList(@RequestParam(value="tenant") String tenantName, @RequestParam(value="username") String username){
        logger.info("get job list.");
        return jobService.getJobList(tenantName, username);
    }

    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil getJobDetail(@RequestParam(value="tenant") String tenantName, @RequestParam(value="name") String jobName){
        logger.info("get job detail.");
        return jobService.getJobDetail(tenantName, jobName);
    }

    @RequestMapping(value = "/buildDetail", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil getBuildDetail(@RequestParam(value="tenant") String tenantName, @RequestParam(value="name") String jobName, @RequestParam(value="buildNum") String buildNum){
        logger.info("get build detail.");
        return jobService.getBuildDetail(tenantName, jobName, buildNum);
    }

    @RequestMapping(value = "/build", method = RequestMethod.POST)
    @ResponseBody
    public ActionReturnUtil build(@RequestParam(value="name") String jobName, @RequestParam(value="tenant") String tenantName, @RequestParam(value="tag") String tag){
        logger.info("build job.");
        return jobService.build(jobName, tenantName, tag);
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


}


