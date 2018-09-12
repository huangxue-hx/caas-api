package com.harmonycloud.api.application;

import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.K8sAuthException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.k8s.constant.Constant;
import com.harmonycloud.service.application.BlueGreenDeployService;
import com.harmonycloud.service.application.ConfigMapService;
import com.harmonycloud.service.application.VersionControlService;
import com.harmonycloud.service.platform.bean.CanaryDeployment;
import com.harmonycloud.service.platform.bean.UpdateDeployment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

/**
 * Created by czm on 2017/4/27.
 */
@Controller
@RequestMapping("/tenants/{tenantId}/projects/{projectId}/deploys/{deployName}")
public class VersionController {

    @Autowired
    VersionControlService versionControlService;

    @Autowired
    ConfigMapService configMapService;
    
    @Autowired
    HttpSession session;

    @Autowired
    BlueGreenDeployService blueGreenDeployService;

    /**
     * 灰度升级，现在还没有考虑Deployment的自动升级对Service的影响
     * @param detail 灰度升级的Deployment详情
     * @return ActionReturnUtil
     * @throws Exception
     */
    @RequestMapping(value = "/canaryUpdate", method = RequestMethod.PUT)
    @ResponseBody
    public ActionReturnUtil canaryUpdate(@RequestBody CanaryDeployment detail,
                                         @PathVariable(value = "projectId") String projectId) throws Exception {
    	String userName = (String) session.getAttribute("username");
        if(userName == null){
			throw new K8sAuthException(Constant.HTTP_401);
		}
		detail.setProjectId(projectId);
        return versionControlService.canaryUpdate(detail, detail.getInstances(), userName);
    }

    /**
     * 查看灰度升级过程中更新实例以及总的副本数
     * @param namespace 分区
     * @param name 服务名称
     * @return ActionReturnUtil
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/updatestatus", method = RequestMethod.GET)
    public ActionReturnUtil getUpdateStatus(@RequestParam(value = "namespace") String namespace,
                                            @PathVariable(value = "deployName") String name,
                                            @RequestParam(value = "serviceType", required = false) String serviceType) throws Exception {
    	String userName = (String) session.getAttribute("username");
        if(userName == null){
			throw new K8sAuthException(Constant.HTTP_401);
		}
        return versionControlService.getUpdateStatus(namespace, name, serviceType);
    }


    /**
     * 取消灰度升级
     * @param namespace 分区
     * @param name 服务名称
     * @return ActionReturnUtil
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/canaryupdate/cancel", method = RequestMethod.PUT)
    public ActionReturnUtil cancelCanaryUpdate(@RequestParam(value = "namespace") String namespace, @PathVariable(value = "deployName") String name) throws Exception {
    	String userName = (String) session.getAttribute("username");
        if(userName == null){
			throw new K8sAuthException(Constant.HTTP_401);
		}
        return versionControlService.cancelCanaryUpdate(namespace, name);
    }

    /**
     * 暂停灰度升级 （未使用）
     * @param namespace
     * @param name
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/canaryupdate/pause", method = RequestMethod.PUT)
    public ActionReturnUtil pauseCanaryUpdate(@RequestParam(value = "namespace") String namespace, @PathVariable(value = "deployName") String name) throws Exception {
    	String userName = (String) session.getAttribute("username");
        if(userName == null){
			throw new K8sAuthException(Constant.HTTP_401);
		}
        return versionControlService.pauseCanaryUpdate(namespace, name);
    }

    /**
     * 继续灰度升级
     * @param namespace 分区
     * @param name 服务名称
     * @return ActionReturnUtil
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/canaryupdate/resume", method = RequestMethod.PUT)
    public ActionReturnUtil resumeCanaryUpdate(@RequestParam(value = "namespace") String namespace, @PathVariable(value = "deployName") String name) throws Exception {
    	String userName = (String) session.getAttribute("username");
        if(userName == null){
			throw new K8sAuthException(Constant.HTTP_401);
		}
        return versionControlService.resumeCanaryUpdate(namespace, name);
    }

    //完成 （未使用）
    @ResponseBody
    @RequestMapping(value = "/reversions", method = RequestMethod.GET)
    public ActionReturnUtil listReversions(@RequestParam(value = "namespace") String namespace, @PathVariable(value = "deployName") String name) throws Exception {
//         return versionControlService.listReversions(namespace,name);
        return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.OPERATION_DISABLED);
    }

    //完成 （未使用）
    @ResponseBody
    @RequestMapping(value = "/reversions/{revision}", method = RequestMethod.GET)
    public ActionReturnUtil getRevisionDetail(@RequestParam(value = "namespace") String namespace, @PathVariable(value = "deployName") String name) throws Exception {
//        return versionControlService.getRevisionDetail(namespace,name,revision);
        return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.OPERATION_DISABLED);
    }

    /**
     * 服务回滚
     * @param namespace 分区
     * @param name 服务名称
     * @param revision 版本
     * @return ActionReturnUtil
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/canaryrollback", method = RequestMethod.PUT)
    public ActionReturnUtil canaryRollback(@RequestParam(value = "namespace") String namespace,
                                           @PathVariable(value = "deployName") String name,
                                           @RequestParam(value = "revision") String revision,
                                           @RequestParam(value = "podTemplate", required = false) String podTemplate,
                                           @PathVariable(value = "projectId") String projectId) throws Exception {
    	String userName = (String) session.getAttribute("username");
        if(userName == null){
			throw new K8sAuthException(Constant.HTTP_401);
		}
        return versionControlService.canaryRollback(namespace,name,revision,podTemplate, projectId);
    }

    /**
     * 服务回滚获取版本以及详情
     * @param namespace 分区
     * @param name 服务名称
     * @return ActionReturnUtil
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/reversions/detail", method = RequestMethod.GET)
    public ActionReturnUtil listRevisionAndDetails(@RequestParam(value = "namespace") String namespace, @PathVariable(value = "deployName") String name) throws Exception {
    	String userName = (String) session.getAttribute("username");
        if(userName == null){
			throw new K8sAuthException(Constant.HTTP_401);
		}
        return versionControlService.listRevisionAndDetails(namespace,name);
    }

    /**
     * 蓝绿发布
     *
     * @param updateDeployment
     * @return ActionReturnUtil
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/bluegreen", method = RequestMethod.PUT)
    public ActionReturnUtil deployByBlueAndGreen(@ModelAttribute UpdateDeployment updateDeployment,
                                                 @PathVariable(value = "projectId") String projectId) throws Exception {
        String userName = (String) session.getAttribute("username");
        if (userName == null) {
            throw new K8sAuthException(Constant.HTTP_401);
        }
        return blueGreenDeployService.deployByBlueGreen(updateDeployment, userName, projectId);
    }

    /**
     * 新旧版本流量切换
     * @param name
     * @param namespace
     * @param isSwitchNew
     * @return ActionReturnUtil
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/bluegreen/switchflow", method = RequestMethod.POST)
    public ActionReturnUtil switchFlowInBlueAndGreen(@PathVariable(value = "deployName") String name,
                                                     @RequestParam(value = "namespace") String namespace,
                                                     @RequestParam(value = "isSwitchNew") boolean isSwitchNew) throws Exception {
        return blueGreenDeployService.switchFlow(name, namespace, isSwitchNew);
    }

    /**
     * 确认升级到新版本
     * @param name
     * @param namespace
     * @return ActionReturnUtil
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/bluegreen/confirm", method = RequestMethod.POST)
    public ActionReturnUtil confirmUpdateToNewVersion(@RequestParam(value = "namespace") String namespace,
                                                      @PathVariable(value = "deployName") String name) throws Exception {
        return blueGreenDeployService.confirmToNewVersion(name, namespace);
    }

    /**
     * 蓝绿发布，保留旧版本
     * @param name
     * @param namespace
     * @return ActionReturnUtil
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/bluegreen/cancel", method = RequestMethod.POST)
    public ActionReturnUtil rollbackToOldVersion(@RequestParam(value = "namespace") String namespace,
                                                 @PathVariable(value = "deployName") String name) throws Exception {
        return blueGreenDeployService.rollbackToOldVersion(name, namespace);
    }

    /**
     *
     * @param name
     * @param namespace
     * @return ActionReturnUtil
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/bluegreen", method = RequestMethod.GET)
    public ActionReturnUtil getBlueGreenInfo(@RequestParam(value = "namespace") String namespace,
                                             @PathVariable(value = "deployName") String name) throws Exception {
        return blueGreenDeployService.getInfoAboutTwoVersion(name, namespace);
    }

}
