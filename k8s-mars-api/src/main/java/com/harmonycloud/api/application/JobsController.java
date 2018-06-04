package com.harmonycloud.api.application;

import com.alibaba.fastjson.JSONObject;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.application.JobsDetailDto;
import com.harmonycloud.service.application.JobsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

/**
 * Created by root on 7/9/17.
 */
@RequestMapping("/tenants/{tenantId}/projects/{projectId}/jobs")
@Controller
public class JobsController {

    @Autowired
    HttpSession session;

    @Autowired
    JobsService jobsService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 创建job
     * @param jobDetailDto
     * @return
     */
    @ResponseBody
    @RequestMapping(method = RequestMethod.POST)
    public ActionReturnUtil createJob(@PathVariable("projectId") String projectId,
									  @ModelAttribute JobsDetailDto jobDetailDto) throws Exception  {
		jobDetailDto.setProjectId(projectId);
		logger.info("创建job:{}", JSONObject.toJSONString(jobDetailDto));
		String userName = (String) session.getAttribute("username");
		return jobsService.createJob(jobDetailDto, userName);
    }



    /**
     * list job
     *
     * @param namespace
     * @param labels(可选)
     *            搜索条件
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(method = RequestMethod.GET)
    public ActionReturnUtil listJob(@PathVariable("projectId") String projectId,
									@RequestParam(value = "clusterId", required = false) String clusterId,
									@RequestParam(value = "namespace", required = false) String namespace,
									@RequestParam(value = "labels", required = false) String labels,
									@RequestParam(value = "status", required = false) String status) throws Exception {
		ActionReturnUtil result = jobsService.listJob(projectId, namespace, labels, status, clusterId);
		return result;
    }

    /**
     * 获取job详情
     * @param namespace
     * @return
     * @throws Exception 
     */
    @ResponseBody
    @RequestMapping(value = "/{jobName}", method = RequestMethod.GET)
    public ActionReturnUtil getJob(@PathVariable("jobName") String jobName,
                                             @RequestParam(value = "namespace") String namespace) throws Exception {
		return jobsService.getJobDetail(namespace, jobName);

    }

    /**
     * start job
     * @param namespace
     * @return
     * @throws Exception 
     */
    @ResponseBody
    @RequestMapping(value = "/{jobName}/start", method = RequestMethod.POST)
    public ActionReturnUtil startJob(@PathVariable("jobName") String jobName,
                                            @RequestParam(value = "namespace") String namespace) throws Exception {
		String userName = (String) session.getAttribute("username");
		logger.info("启动job,jobName:{},namespace:{},userName:{}",new String[]{jobName,namespace,userName});
		return jobsService.startJob(jobName, namespace, userName);
    }


    /**
     * stop job
     * @param namespace
     * @return
     * @throws Exception 
     */
    @ResponseBody
    @RequestMapping(value = "/{jobName}/stop", method = RequestMethod.POST)
    public ActionReturnUtil stopJob(@PathVariable("jobName") String jobName,
                                           @RequestParam(value = "namespace") String namespace) throws Exception {
		String userName = (String) session.getAttribute("username");
		logger.info("停止job,jobName:{},namespace:{},userName:{}",new String[]{jobName,namespace,userName});
		return jobsService.stopJob(jobName, namespace, userName);
    }


    /**
     * 更新job
     * @param jobDetailDto
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(method = RequestMethod.PUT)
    public ActionReturnUtil updateJob(@ModelAttribute JobsDetailDto jobDetailDto) throws Exception {

		String userName = (String) session.getAttribute("username");
		logger.info("更新job:{},username:{}",JSONObject.toJSONString(jobDetailDto),userName);
		return jobsService.replaceJob(jobDetailDto, userName);
    }

    /**
     * 删除job
     * @param namespace
     * @return
     * @throws Exception 
     */
    @ResponseBody
    @RequestMapping(value = "/{jobName}", method = RequestMethod.DELETE)
    public ActionReturnUtil deleteJob(@PathVariable("jobName") String jobName,
                                              @RequestParam(value = "namespace") String namespace) throws Exception {
		String userName = (String) session.getAttribute("username");
		logger.info("删除job,jobName:{},namespace:{},userName:{}",new String[]{jobName,namespace,userName});
		return jobsService.deleteJob(jobName, namespace, userName);
    }

    /**
     * 重启job
     * @param namespace
     * @return
     * @throws Exception 
     */
    @ResponseBody
    @RequestMapping(value = "/{jobName}/reRun",method = RequestMethod.POST)
    public ActionReturnUtil reRunJob(@PathVariable("jobName") String jobName,
                                     @RequestParam(value = "namespace") String namespace) throws Exception {
		String userName = (String) session.getAttribute("username");
		logger.info("reRun job,jobName:{},namespace:{},userName:{}",new String[]{jobName,namespace,userName});
		return jobsService.reRunJob(jobName, namespace, userName);
    }

    /**
     * 更新job 并行数
     * @param namespace
     * @return
     * @throws Exception 
     */
    @ResponseBody
    @RequestMapping(value = "/{jobName}/parallelism",method = RequestMethod.PUT)
    public ActionReturnUtil updateJobParallelism(@PathVariable("jobName") String jobName,
                                                 @RequestParam(value = "namespace") String namespace,
												 @RequestParam(value = "parallelism") int parallelism) throws Exception {
		logger.info("updateJobParallelism,jobName:{},namespace:{},parallelism:{}",new String[]{jobName, namespace, parallelism+""});
		return jobsService.updateJobParallelism(jobName, namespace, parallelism);
    }

}
