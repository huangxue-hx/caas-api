package com.harmonycloud.api.harbor;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.platform.service.harbor.HarborImageCleanService;
import com.harmonycloud.service.platform.service.harbor.HarborProjectService;
import com.harmonycloud.service.platform.service.harbor.HarborService;
import com.harmonycloud.service.platform.service.harbor.HarborUserService;
import com.harmonycloud.service.user.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


/**
 * Created by andy on 17-1-19.
 */
@Api(description = "Harbor总览")
@Controller
public class HarborController {

    @Autowired
    private HarborUserService harborUserService;
    @Autowired
    HarborProjectService harborProjectService;
    @Autowired
    HarborImageCleanService harborImageCleanService;
    @Autowired
    HarborService harborService;
    @Autowired
    UserService userService;
    @Autowired
    ClusterService clusterService;

    /**
     * 获取用户权限下的harborServer列表
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/harbor/servers", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil listHarborServer() throws Exception{
        return ActionReturnUtil.returnSuccessWithData(harborUserService.getUserAvailableHarbor(userService.getCurrentUsername()));
    }

    /**
     * 获取平台所有镜像仓库的总览
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "获取平台所有镜像仓库的总览", notes = "", httpMethod = "GET")
    @RequestMapping(value = "/harbor/harborprojects/overview", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil getHarborProjectsOverview() throws Exception{
        return ActionReturnUtil.returnSuccessWithData(harborProjectService.getHarborProjectOverview(null,userService.getCurrentUsername()));
    }

    /**
     * 获取某个harbor相关的总览信息
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "取某个harbor相关的总览信息", notes = "", httpMethod = "GET")
    @RequestMapping(value = "/harbor/{harborHost}/overview", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil getHarborOverview(@PathVariable(value="harborHost") String harborHost) throws Exception{
        return ActionReturnUtil.returnSuccessWithData(harborService.getHarborOverview(harborHost, userService.getCurrentUsername()));
    }

    /**
     * 对harbor进行垃圾镜像文件清理
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/harbor/{harborHost}/gc", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil cleanImageGarbage(@PathVariable(value="harborHost") String harborHost) throws Exception{
        return ActionReturnUtil.returnSuccessWithData(harborImageCleanService.cleanImageGarbage(harborHost));
    }

    /**
     * docker registry与harbor ui同步
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/harbor/{harborHost}/syncRegistry", method = RequestMethod.PUT)
    @ResponseBody
    public ActionReturnUtil syncRegistry(@PathVariable(value="harborHost") String harborHost) throws Exception{
        return ActionReturnUtil.returnSuccessWithData(harborService.syncRegistry(harborHost));
    }
}


