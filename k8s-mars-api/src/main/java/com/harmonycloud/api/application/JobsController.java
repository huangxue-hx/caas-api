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
    public ActionReturnUtil createJob(@ModelAttribute JobsDetailDto jobDetailDto) {
        try {
            logger.info("创建job");
            String userName = (String) session.getAttribute("username");
            if(userName == null){
                throw new K8sAuthException(Constant.HTTP_401);
            }
            Cluster cluster = (Cluster) session.getAttribute("currentCluster");
            return jobsService.createJob(jobDetailDto, userName, "", cluster);
        } catch (Exception e) {
            logger.error("创建job失败：, error:"+e.getMessage()+e.getCause());
            e.printStackTrace();
            return ActionReturnUtil.returnError();
        }
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
    public ActionReturnUtil listJob(@RequestParam(value = "tenantId", required = false) String tenantId,@RequestParam(value = "name", required = false) String name,
                                            @RequestParam(value = "namespace", required = false) String namespace,
                                            @RequestParam(value = "labels", required = false) String labels, @RequestParam(value = "status", required = false) String status) throws Exception {

        try {
            logger.info("获取job列表");
            ActionReturnUtil result = jobsService.listJob(tenantId,name, namespace, labels, status);
            return result;
        } catch (Exception e) {
            if (e instanceof K8sAuthException) {
                throw e;
            }
            e.printStackTrace();
            logger.error("获取job列表失败：name="+name+ ", namespace="+namespace+", labels="+labels+", error:"+e.getMessage()+e.getCause());
            return ActionReturnUtil.returnError();
        }

    }

    /**
     * 获取job详情
     * @param name
     * @param namespace
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    public ActionReturnUtil jobDetail(@RequestParam(value = "name", required = true) String name,
                                             @RequestParam(value = "namespace", required = true) String namespace) {

        try {
            logger.info("查询job详情");
            if (StringUtils.isEmpty(namespace) || StringUtils.isEmpty(name)) {
                return ActionReturnUtil.returnError();
            }
            String userName = (String) session.getAttribute("username");
            if(userName == null){
                throw new K8sAuthException(Constant.HTTP_401);
            }
            Cluster cluster = (Cluster) session.getAttribute("currentCluster");
            return jobsService.getJobDetail(namespace, name, cluster);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("查询job详情失败：name="+name+ ", namespace="+namespace+", error:"+e.getMessage());
            return ActionReturnUtil.returnError();
        }

    }

    /**
     * start job
     * @param name
     * @param namespace
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/start", method = RequestMethod.POST)
    public ActionReturnUtil startJob(@RequestParam(value = "name", required = true) String name,
                                            @RequestParam(value = "namespace", required = true) String namespace) {

        try {
            logger.info("启动job");
            if (StringUtils.isEmpty(namespace) || StringUtils.isEmpty(name)) {
                return ActionReturnUtil.returnError();
            }
            String userName = (String) session.getAttribute("username");
            if(userName == null){
                throw new K8sAuthException(Constant.HTTP_401);
            }
            Cluster cluster = (Cluster) session.getAttribute("currentCluster");
            return jobsService.startJob(name, namespace, userName,cluster);
        } catch (Exception e) {
            logger.error("启动job失败：name="+name+ ", namespace="+namespace+", error:"+e.getMessage());
            return ActionReturnUtil.returnError();
        }

    }


    /**
     * stop job
     * @param name
     * @param namespace
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/stop", method = RequestMethod.POST)
    public ActionReturnUtil stopJob(@RequestParam(value = "name", required = true) String name,
                                           @RequestParam(value = "namespace", required = true) String namespace) {

        try {
            logger.info("停止job");
            if (StringUtils.isEmpty(namespace) || StringUtils.isEmpty(name)) {
                return ActionReturnUtil.returnError();
            }
            String userName = (String) session.getAttribute("username");
            if(userName == null){
                throw new K8sAuthException(Constant.HTTP_401);
            }
            Cluster cluster = (Cluster) session.getAttribute("currentCluster");
            return jobsService.stopJob(name, namespace, userName, cluster);
        } catch (Exception e) {
            logger.error("停止job失败：name="+name+ ", namespace="+namespace+", error:"+e.getMessage());
            return ActionReturnUtil.returnError();
        }
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
        try {
            logger.info("更新job");
            String userName = (String) session.getAttribute("username");
            if(userName == null){
                throw new K8sAuthException(Constant.HTTP_401);
            }
            Cluster cluster = (Cluster) session.getAttribute("currentCluster");
            return jobsService.replaceJob(jobDetailDto, userName, cluster);
        } catch (Exception e) {
            logger.error("更新job失败：, error:"+e.getMessage()+e.getCause());
            return ActionReturnUtil.returnError();
        }
    }

    /**
     * 删除job
     * @param name
     * @param namespace
     * @return
     */
    @ResponseBody
    @RequestMapping(method = RequestMethod.DELETE)
    public ActionReturnUtil deleteJob(@RequestParam(value = "name", required = true) String name,
                                              @RequestParam(value = "namespace", required = true) String namespace) {

        try {
            logger.info("删除job");
            String userName = (String) session.getAttribute("username");
            if(userName == null){
                throw new K8sAuthException(Constant.HTTP_401);
            }
            Cluster cluster = (Cluster) session.getAttribute("currentCluster");
            return jobsService.deleteJob(name, namespace, userName, cluster);
        } catch (Exception e) {
            logger.error("删除job失败：name="+name+ ", namespace="+namespace+", error:"+e.getMessage());
            return ActionReturnUtil.returnError();
        }
    }

    /**
     * 删除job
     * @param name
     * @param namespace
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/reRun",method = RequestMethod.POST)
    public ActionReturnUtil reRunJob(@RequestParam(value = "name", required = true) String name,
                                              @RequestParam(value = "namespace", required = true) String namespace) {

        try {
            logger.info("reRun job");
            String userName = (String) session.getAttribute("username");
            if(userName == null){
                throw new K8sAuthException(Constant.HTTP_401);
            }
            Cluster cluster = (Cluster) session.getAttribute("currentCluster");
            return jobsService.reRunJob(name, namespace, userName, cluster);
        } catch (Exception e) {
            logger.error("reRun job 失败：name="+name+ ", namespace="+namespace+", error:"+e.getMessage());
            return ActionReturnUtil.returnError();
        }
    }


}
