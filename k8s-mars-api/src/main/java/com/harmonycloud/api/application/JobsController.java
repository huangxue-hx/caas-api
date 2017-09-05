package com.harmonycloud.api.application;

import com.harmonycloud.common.exception.K8sAuthException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.dto.business.JobsDetailDto;
import com.harmonycloud.k8s.constant.Constant;
import com.harmonycloud.service.application.JobsService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

/**
 * Created by root on 7/9/17.
 */
@RequestMapping("/job")
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
    public ActionReturnUtil createJob(@ModelAttribute JobsDetailDto jobDetailDto) throws Exception  {
		logger.info("创建job");
		String userName = (String) session.getAttribute("username");
		if (userName == null) {
			throw new K8sAuthException(Constant.HTTP_401);
		}
		Cluster cluster = (Cluster) session.getAttribute("currentCluster");
		return jobsService.createJob(jobDetailDto, userName, cluster);
    }



    /**
     * list job
     *
     * @param name
     * @param namespace
     * @param labels(可选)
     *            搜索条件
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(method = RequestMethod.GET)
    public ActionReturnUtil listJob(@RequestParam(value = "tenantId", required = false) String tenantId, @RequestParam(value = "namespace", required = false) String namespace, @RequestParam(value = "labels", required = false) String labels, @RequestParam(value = "status", required = false) String status) throws Exception {
		logger.info("获取job列表");
		Cluster cluster = (Cluster) session.getAttribute("currentCluster");
		ActionReturnUtil result = jobsService.listJob(tenantId, namespace, labels, status, cluster);
		return result;
    }

    /**
     * 获取job详情
     * @param name
     * @param namespace
     * @return
     * @throws Exception 
     */
    @ResponseBody
    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    public ActionReturnUtil jobDetail(@RequestParam(value = "name", required = true) String name,
                                             @RequestParam(value = "namespace", required = true) String namespace) throws Exception {
		logger.info("查询job详情");
		if (StringUtils.isEmpty(namespace) || StringUtils.isEmpty(name)) {
			return ActionReturnUtil.returnError();
		}
		String userName = (String) session.getAttribute("username");
		if (userName == null) {
			throw new K8sAuthException(Constant.HTTP_401);
		}
		Cluster cluster = (Cluster) session.getAttribute("currentCluster");
		return jobsService.getJobDetail(namespace, name, cluster);

    }

    /**
     * start job
     * @param name
     * @param namespace
     * @return
     * @throws Exception 
     */
    @ResponseBody
    @RequestMapping(value = "/start", method = RequestMethod.POST)
    public ActionReturnUtil startJob(@RequestParam(value = "name", required = true) String name,
                                            @RequestParam(value = "namespace", required = true) String namespace) throws Exception {
		logger.info("启动job");
		if (StringUtils.isEmpty(namespace) || StringUtils.isEmpty(name)) {
			return ActionReturnUtil.returnError();
		}
		String userName = (String) session.getAttribute("username");
		if (userName == null) {
			throw new K8sAuthException(Constant.HTTP_401);
		}
		Cluster cluster = (Cluster) session.getAttribute("currentCluster");
		return jobsService.startJob(name, namespace, userName, cluster);
    }


    /**
     * stop job
     * @param name
     * @param namespace
     * @return
     * @throws Exception 
     */
    @ResponseBody
    @RequestMapping(value = "/stop", method = RequestMethod.POST)
    public ActionReturnUtil stopJob(@RequestParam(value = "name", required = true) String name,
                                           @RequestParam(value = "namespace", required = true) String namespace) throws Exception {
		logger.info("停止job");
		if (StringUtils.isEmpty(namespace) || StringUtils.isEmpty(name)) {
			return ActionReturnUtil.returnError();
		}
		String userName = (String) session.getAttribute("username");
		if (userName == null) {
			throw new K8sAuthException(Constant.HTTP_401);
		}
		Cluster cluster = (Cluster) session.getAttribute("currentCluster");
		return jobsService.stopJob(name, namespace, userName, cluster);
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
		logger.info("更新job");
		String userName = (String) session.getAttribute("username");
		if (userName == null) {
			throw new K8sAuthException(Constant.HTTP_401);
		}
		Cluster cluster = (Cluster) session.getAttribute("currentCluster");
		return jobsService.replaceJob(jobDetailDto, userName, cluster);
    }

    /**
     * 删除job
     * @param name
     * @param namespace
     * @return
     * @throws Exception 
     */
    @ResponseBody
    @RequestMapping(method = RequestMethod.DELETE)
    public ActionReturnUtil deleteJob(@RequestParam(value = "name", required = true) String name,
                                              @RequestParam(value = "namespace", required = true) String namespace) throws Exception {
		logger.info("删除job");
		String userName = (String) session.getAttribute("username");
		if (userName == null) {
			throw new K8sAuthException(Constant.HTTP_401);
		}
		Cluster cluster = (Cluster) session.getAttribute("currentCluster");
		return jobsService.deleteJob(name, namespace, userName, cluster);
    }

    /**
     * 重启job
     * @param name
     * @param namespace
     * @return
     * @throws Exception 
     */
    @ResponseBody
    @RequestMapping(value = "/reRun",method = RequestMethod.POST)
    public ActionReturnUtil reRunJob(@RequestParam(value = "name", required = true) String name,
                                              @RequestParam(value = "namespace", required = true) String namespace) throws Exception {
		logger.info("reRun job");
		String userName = (String) session.getAttribute("username");
		if (userName == null) {
			throw new K8sAuthException(Constant.HTTP_401);
		}
		Cluster cluster = (Cluster) session.getAttribute("currentCluster");
		return jobsService.reRunJob(name, namespace, userName, cluster);
    }

    /**
     * 更新job 并行数
     * @param name
     * @param namespace
     * @return
     * @throws Exception 
     */
    @ResponseBody
    @RequestMapping(value = "/parallelism",method = RequestMethod.PUT)
    public ActionReturnUtil updateJobParallelism(@RequestParam(value = "name", required = true) String name,
                                              @RequestParam(value = "namespace", required = true) String namespace, @RequestParam(value = "parallelism", required = true) int parallelism) throws Exception {
		logger.info("reRun job");
		String userName = (String) session.getAttribute("username");
		if (userName == null) {
			throw new K8sAuthException(Constant.HTTP_401);
		}
		Cluster cluster = (Cluster) session.getAttribute("currentCluster");
		return jobsService.updateJobParallelism(name, namespace, parallelism, cluster);
    }

}
