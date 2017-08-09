package com.harmonycloud.api.application;

import com.harmonycloud.common.exception.K8sAuthException;
import com.harmonycloud.common.util.ActionReturnUtil;

import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.k8s.constant.Constant;
import com.harmonycloud.service.application.ConfigMapService;
import com.harmonycloud.service.application.VersionControlService;
import com.harmonycloud.service.platform.bean.CanaryDeployment;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * Created by czm on 2017/4/27.
 */
@Controller
@RequestMapping("/versionControl")
public class VersionController {

    @Autowired
    VersionControlService versionControlService;

    @Autowired
    ConfigMapService configMapService;
    
    @Autowired
    HttpSession session;

    /**
     * 灰度升级，现在还没有考虑Deployment的自动升级对Service的影响
     * @param detail
     * @param instances
     * @param username
     * @return
     * @throws Exception
     */

    @RequestMapping(value = "/canaryUpdate", method = RequestMethod.PUT)
    @ResponseBody
    public ActionReturnUtil canaryUpdate(@RequestBody CanaryDeployment detail) throws Exception {
    	String userName = (String) session.getAttribute("username");
        if(userName == null){
			throw new K8sAuthException(Constant.HTTP_401);
		}
		Cluster cluster = (Cluster) session.getAttribute("currentCluster");
        return versionControlService.canaryUpdate(detail, detail.getInstances(), null, cluster);
    }

    /**
     * 查看灰度升级过程中更新实例以及总的副本数
     * @param namespace
     * @param name
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/getUpdateStatus", method = RequestMethod.GET)
    public ActionReturnUtil getUpdateStatus(String namespace, String name) throws Exception {
    	String userName = (String) session.getAttribute("username");
        if(userName == null){
			throw new K8sAuthException(Constant.HTTP_401);
		}
		Cluster cluster = (Cluster) session.getAttribute("currentCluster");
        return versionControlService.getUpdateStatus(namespace, name, cluster);
    }


    //完成
    @ResponseBody
    @RequestMapping(value = "/cancelCanaryUpdate", method = RequestMethod.PUT)
    public ActionReturnUtil cancelCanaryUpdate(String namespace, String name) throws Exception {
    	String userName = (String) session.getAttribute("username");
        if(userName == null){
			throw new K8sAuthException(Constant.HTTP_401);
		}
		Cluster cluster = (Cluster) session.getAttribute("currentCluster");
        return versionControlService.cancelCanaryUpdate(namespace, name, cluster);
    }

    //完成
    @ResponseBody
    @RequestMapping(value = "/pauseCanaryUpdate", method = RequestMethod.PUT)
    public ActionReturnUtil pauseCanaryUpdate(String namespace, String name) throws Exception {
    	String userName = (String) session.getAttribute("username");
        if(userName == null){
			throw new K8sAuthException(Constant.HTTP_401);
		}
		Cluster cluster = (Cluster) session.getAttribute("currentCluster");
        return versionControlService.pauseCanaryUpdate(namespace, name, cluster);
    }

    //完成
    @ResponseBody
    @RequestMapping(value = "/resumeCanaryUpdate", method = RequestMethod.PUT)
    public ActionReturnUtil resumeCanaryUpdate(String namespace, String name) throws Exception {
    	String userName = (String) session.getAttribute("username");
        if(userName == null){
			throw new K8sAuthException(Constant.HTTP_401);
		}
		Cluster cluster = (Cluster) session.getAttribute("currentCluster");
        return versionControlService.resumeCanaryUpdate(namespace, name, cluster);
    }

    //完成
    @ResponseBody
    @RequestMapping(value = "/listReversions", method = RequestMethod.GET)
    public ActionReturnUtil listReversions(String namespace, String name) throws Exception {
//         return versionControlService.listReversions(namespace,name);
        return ActionReturnUtil.returnErrorWithMsg("暂时取消了");
    }

    //完成
    @ResponseBody
    @RequestMapping(value = "/getRevisionDetail", method = RequestMethod.GET)
    public ActionReturnUtil getRevisionDetail(String namespace, String name, String revision) throws Exception {
//        return versionControlService.getRevisionDetail(namespace,name,revision);
        return ActionReturnUtil.returnErrorWithMsg("暂时取消了");
    }

    //完成
    @ResponseBody
    @RequestMapping(value = "/canaryRollback", method = RequestMethod.PUT)
    public ActionReturnUtil canaryRollback(String namespace, String name, String revision) throws Exception {
    	String userName = (String) session.getAttribute("username");
        if(userName == null){
			throw new K8sAuthException(Constant.HTTP_401);
		}
		Cluster cluster = (Cluster) session.getAttribute("currentCluster");
        return versionControlService.canaryRollback(namespace,name,revision, cluster);
    }

    @ResponseBody
    @RequestMapping(value = "/listRevisionAndDetails", method = RequestMethod.GET)
    public ActionReturnUtil listRevisionAndDetails(String namespace, String name) throws Exception {
    	String userName = (String) session.getAttribute("username");
        if(userName == null){
			throw new K8sAuthException(Constant.HTTP_401);
		}
		Cluster cluster = (Cluster) session.getAttribute("currentCluster");
        return versionControlService.listRevisionAndDetails(namespace,name,cluster);
    }



    @ResponseBody
    @RequestMapping(value = "/configmap", method = RequestMethod.GET)
    public ActionReturnUtil configmap(String namespace, String name,String mountKey) throws Exception {
    	String userName = (String) session.getAttribute("username");
        if(userName == null){
			throw new K8sAuthException(Constant.HTTP_401);
		}
		Cluster cluster = (Cluster) session.getAttribute("currentCluster");
        return  configMapService.getConfigMapByName(namespace,name,null,cluster);
    }

    @ResponseBody
    @RequestMapping(value = "/configmap/list", method = RequestMethod.GET)
    public ActionReturnUtil listconfigmap(String namespace, String name) throws Exception {
    	String userName = (String) session.getAttribute("username");
        if(userName == null){
			throw new K8sAuthException(Constant.HTTP_401);
		}
		Cluster cluster = (Cluster) session.getAttribute("currentCluster");
        return  configMapService.listConfigMapByName(namespace,name,cluster);
    }




}
