package com.harmonycloud.api.ci;

import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.dao.ci.bean.BuildEnvironment;
import com.harmonycloud.service.platform.service.ci.BuildEnvironmentService;
import com.harmonycloud.service.platform.serviceImpl.ci.BuildEnvironmentServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Date;

/**
 * @Author w_kyzhang
 * @Description 自定义构建环境
 * @Date 2017-12-18
 * @Modified
 */

@RequestMapping("/tenants/{tenantId}/projects/{projectId}/env")
@Controller
public class BuildEnvironmentController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private BuildEnvironmentService buildEnvironmentService;

    @Autowired
    private HttpSession session;

    /**
     * 查询环境列表
     *
     * @param name
     * @return
     * @throws Exception
     */
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil listBuildEnvironment(@PathVariable("projectId")String projectId,
                                                 @RequestParam(value = "clusterId",required = false) String clusterId,
                                                 @RequestParam(value = "name", required = false) String name) throws Exception {
//        logger.info("查询环境列表");
        return ActionReturnUtil.returnSuccessWithData(buildEnvironmentService.listBuildEnvironment(projectId, clusterId, name));
    }

    /**
     * 根据id查询环境
     *
     * @param id
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/{id}",method = RequestMethod.POST)
    @ResponseBody
    public ActionReturnUtil getBuildEnvironment(@PathVariable("id")Integer id) throws Exception {
//        logger.info("获取环境信息");
        return ActionReturnUtil.returnSuccessWithData(buildEnvironmentService.getBuildEnvironment(id));
    }

    /**
     * 新增环境
     *
     * @param buildEnvironment
     * @return
     * @throws Exception
     */
    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public ActionReturnUtil addBuildEnvironment(@RequestBody BuildEnvironment buildEnvironment) throws Exception {
//        logger.info("新增环境");
        String username = (String) session.getAttribute("username");
        buildEnvironment.setCreateUser(username);
        buildEnvironment.setCreateTime(new Date());
        buildEnvironmentService.addBuildEnvironment(buildEnvironment);
        return ActionReturnUtil.returnSuccess();
    }

    /**
     * 修改环境
     *
     * @param buildEnvironment
     * @return
     * @throws Exception
     */
    @RequestMapping(method = RequestMethod.PUT)
    @ResponseBody
    public ActionReturnUtil updateBuildEnvironment(@PathVariable("tenantId")String tenantId,
                                                   @PathVariable("projectId")String projectId,
                                                   @RequestBody BuildEnvironment buildEnvironment) throws Exception {
//        logger.info("更新环境");
        String username = (String) session.getAttribute("username");
        buildEnvironment.setUpdateUser(username);
        buildEnvironment.setUpdateTime(new Date());
        buildEnvironmentService.updateBuildEnvironment(buildEnvironment);
        return ActionReturnUtil.returnSuccess();
    }

    /**
     * 根据id删除环境
     *
     * @param id
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/{id}",method = RequestMethod.DELETE)
    @ResponseBody
    public ActionReturnUtil deleteBuildEnvironment(@PathVariable("id")Integer id) throws Exception {
//        logger.info("删除环境");
        buildEnvironmentService.deleteBuildEnvironment(id);
        return ActionReturnUtil.returnSuccess();
    }
}