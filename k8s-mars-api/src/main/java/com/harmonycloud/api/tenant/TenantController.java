package com.harmonycloud.api.tenant;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.service.tenant.HarborProjectTenantService;
import com.harmonycloud.service.tenant.NamespaceService;
import com.harmonycloud.service.tenant.RolePrivilegeService;
import com.harmonycloud.service.tenant.TenantBindingService;
import com.harmonycloud.service.tenant.TenantService;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import javax.servlet.http.HttpSession;

/**
 * Created by andy on 17-1-9.
 */
@RequestMapping("/tenant")
@Controller
public class TenantController {

    @Autowired
    TenantService tenantService;
    @Autowired
    TenantBindingService tenantBindingService;
    @Autowired
    HarborProjectTenantService harborProjectTenantService;
    @Autowired
    NamespaceService namespaceService;
    @Autowired
    RolePrivilegeService rolePrivilegeService;

    @Autowired
    private HttpSession session;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 根据用户名查询租户列表
     * 
     * @param username
     *            用户名称
     * @return
     */
    @ApiOperation(value = "获取租户列表", httpMethod = "GET", response = ActionReturnUtil.class, notes = "获取租户列表")
    @ApiImplicitParam(name = "name", value = "用户名")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil tenantlist(@RequestParam(value = "username", required = false) String username, Integer clusterId) throws Exception {

        ActionReturnUtil result = tenantService.tenantList(username, clusterId);

        return result;

    }

    /**
     * 查询所有租户列表
     * 
     * @return
     */
    @RequestMapping(value = "/allList", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil tenantAlllist() throws Exception {

        ActionReturnUtil result = tenantService.tenantAlllist();
        return result;

    }
    /**
     * 根据clusterid查询租户列表
     * 
     * @return
     */
    @RequestMapping(value = "/listTenantByClusterId", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil listTenantByClusterId(@RequestParam(value = "clusterId", required = true) Integer clusterId) throws Exception {

        ActionReturnUtil result = tenantService.listTenantByClusterId(clusterId);
        return result;

    }
    /**
     * 根据租户id查询租户详情
     * 
     * @param tenantid
     * @return
     */
    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil tenantdetail(@RequestParam(value = "tenantid", required = true) String tenantid) throws Exception {

        logger.info("根据id查询租户详情");
        if (StringUtils.isEmpty(tenantid)) {
            return ActionReturnUtil.returnError();
        }
        return tenantService.tenantdetail(tenantid);

    }

    /**
     * 根据租户名称查询租户详情
     * 
     * @param tenantName
     *            租户名称
     * @return
     */
    @RequestMapping(value = "/detailByName", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil detailByName(@RequestParam(value = "tenantName", required = true) String tenantName) throws Exception {

        logger.info("根据租户名称查询租户详情");
        if (StringUtils.isEmpty(tenantName)) {
            return ActionReturnUtil.returnError();
        }
        return tenantService.tenantdetailByName(tenantName);

    }

    /**
     * 创建租户
     * 
     * @param name
     *            租户名称
     * @param annotation
     *            备注
     * @param userStr
     *            用户信息
     * @return
     */
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @ResponseBody
    public ActionReturnUtil tenantcreate(@RequestParam(value = "name") String name, String annotation, String userStr, @RequestParam(value = "cluster") Integer cluster)
            throws Exception {

        if (StringUtils.isEmpty(name)) {
            return ActionReturnUtil.returnErrorWithMsg("租户名不能为空");
        }

        return tenantService.tenantcreate(name, annotation, userStr, cluster);

    }

    /**
     * 根据租户id删除租户
     * 
     * @param tenantid
     * @return
     */
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    @ResponseBody
    public ActionReturnUtil tenantdelete(@RequestParam(value = "tenantid", required = true) String tenantid) throws Exception {

        if (StringUtils.isEmpty(tenantid)) {
            return ActionReturnUtil.returnError();
        }
        return tenantService.tenantdelete(tenantid);
    }

    /**
     * 更新harbor projects
     * 
     * @param tenantId
     *            租户id
     * @param harborProjectList
     *            harbor project list
     * @return
     */
    @RequestMapping(value = "/updateHarbors", method = RequestMethod.POST)
    public ActionReturnUtil updateHarborProjects(@RequestParam(value = "tenantId", required = true) String tenantId,
            @RequestParam(value = "harborProjects", required = true) List<String> harborProjectList,
            @RequestParam(value = "harborProjectId", required = true) String harborProjectId, @RequestParam(value = "deleted", required = false) Boolean deleted) throws Exception {

        logger.info("更新harbor projects");
        // 默认为增加
        deleted = deleted != null ? deleted : false;
        if (tenantBindingService.updateHarborProjectsByTenantId(tenantId, harborProjectList) < 0) {
            return ActionReturnUtil.returnErrorWithMsg("update failed");
        }

        if (deleted) {
            if (harborProjectTenantService.delete(harborProjectId) < 0) {
                return ActionReturnUtil.returnErrorWithMsg("update failed");
            }
        } else {
            if (harborProjectTenantService.create(harborProjectId, tenantId, null, 0) < 0) {
                return ActionReturnUtil.returnErrorWithMsg("update failed");
            }
        }

        return ActionReturnUtil.returnSuccess();
    }

    /**
     * 添加namespace并设置用户信息
     * 
     * @param tenantid
     * @param namespace
     * @param user
     * @param userList
     * @return
     */
    @RequestMapping(value = "/insert", method = RequestMethod.POST)
    public ActionReturnUtil insert(@RequestParam(value = "tenantid", required = true) String tenantid, String namespace, String user,
            @RequestParam(value = "userList", required = true) List<String> userList) throws Exception {
        logger.info("添加namespace");
        if ("".equals(tenantid)) {
            return ActionReturnUtil.returnErrorWithMsg("tenantId can not be null");
        }
        ActionReturnUtil result = tenantBindingService.updateTenantBinding(tenantid, namespace, user, userList);

        return result;
    }

    /**
     * 删除namespace
     * 
     * @param tenantid
     * @param namespace
     * @return
     */
    @RequestMapping(value = "/namespace/del", method = RequestMethod.POST)
    public ActionReturnUtil delete(@RequestParam(value = "tenantid", required = true) String tenantid, String namespace) throws Exception {

        logger.info("删除namespace");
        if ("".equals(tenantid)) {
            return ActionReturnUtil.returnErrorWithMsg("tenantId can not be null");
        }
        ActionReturnUtil result = tenantBindingService.deleteNamespace(tenantid, namespace);
        return result;
    }

    /**
     * 查询namespace详情
     * 
     * @param tenantid
     * @return
     */
    @RequestMapping(value = "/smpldetail", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil tenantsmpl(@RequestParam(value = "tenantid", required = true) String tenantid) throws Exception {

        logger.info("查询namespace详情");
        if (StringUtils.isEmpty(tenantid)) {
            return ActionReturnUtil.returnError();
        }
        return tenantService.getSmplTenantDetail(tenantid);

    }

    /**
     * 查询namespace下用户列表
     * 
     * @param tenantname
     * @param namespace
     * @return
     */
    @RequestMapping(value = "/namespace/userList", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil userList(@RequestParam(value = "tenantname") String tenantname, @RequestParam(value = "namespace") String namespace) throws Exception {

        logger.info("查询namespace下用户列表");
        if (StringUtils.isEmpty(tenantname) || StringUtils.isEmpty(namespace)) {
            return ActionReturnUtil.returnError();
        }
        return tenantService.getNamespaceUserList(tenantname, namespace);
    }

    /**
     * 创建harbor project
     * 
     * @param name
     * @param tenantid
     * @return
     */
    @RequestMapping(value = "/createProject", method = RequestMethod.POST)
    @ResponseBody
    public ActionReturnUtil createHarborProject(@RequestParam(value = "name") String name, @RequestParam(value = "tenantid") String tenantid,
            @RequestParam(value = "quotaSize") Float quotaSize) throws Exception {

        logger.info("创建harbor project");
        if (StringUtils.isEmpty(name) || StringUtils.isEmpty(tenantid)) {
            return ActionReturnUtil.returnError();
        }
        return harborProjectTenantService.createHarborProject(name, tenantid, quotaSize);
    }

    /**
     * 删除 harbor project
     * 
     * @param tenantid
     * @param tenantname
     * @param projectid
     * @return
     */
    @RequestMapping(value = "/deleteProject", method = RequestMethod.DELETE)
    @ResponseBody
    public ActionReturnUtil deleteHarborProject(@RequestParam(value = "tenantid", required = true) String tenantid,
            @RequestParam(value = "tenantname", required = true) String tenantname, @RequestParam(value = "projectid", required = true) String projectid) throws Exception {
        logger.info("删除 harbor project");
        return harborProjectTenantService.deleteHarborProject(tenantname, tenantid, projectid);
    }

    /**
     * 查询 harbor projectb列表
     * 
     * @param tenantid
     * @return
     */
    @RequestMapping(value = "/projectList", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil getProjectList(@RequestParam(value = "tenantid") String tenantid, @RequestParam(value = "public") Integer isPublic, Integer page,
            Integer page_size) throws Exception {
        logger.info("查询 harbor projectb列表");
        return harborProjectTenantService.getProjectList(tenantid,isPublic,false);
    }

    /**
     * 查询 harbor projectb列表,用于租户管理，需要同时显示仓库配额
     *
     * @param tenantid
     * @return
     */
    @RequestMapping(value = "/projectListQuota", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil getProjectListQuota(@RequestParam(value = "tenantid") String tenantid,@RequestParam(value = "public") Integer isPublic, Integer page, Integer page_size) throws Exception {
        logger.info("查询 harbor projectb列表");
        return harborProjectTenantService.getProjectList(tenantid,isPublic,true);

    }

    /**
     * 查询harbor project 详情
     * 
     * @param tenantid
     * @param projectid
     * @return
     */
    @RequestMapping(value = "/projectDetail", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil getHarborProjectDetail(@RequestParam(value = "tenantid", required = true) String tenantid,
            @RequestParam(value = "projectid", required = true) Integer projectid) throws Exception {
        logger.info("查询查询harbor project 详情");
        return harborProjectTenantService.getProjectDetail(tenantid, projectid);

    }

    /**
     * 查询该tenant下的所有用户
     * 
     * @return
     */
    @RequestMapping(value = "/userList", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil listTenantUsers(@RequestParam(value = "tenantid") String tenantid) throws Exception {

        logger.info("查询tenant下的所有用户");
        return this.tenantService.listTenantUsers(tenantid);
    }
    /**
     * 查询该tenant下的所有tm用户
     * 
     * @return
     */
    @RequestMapping(value = "/TmUserList", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil listTenantTm(@RequestParam(value = "tenantid") String tenantid) throws Exception {
        logger.info("查询tenant下的所有tm用户");
        return this.tenantService.listTenantTm(tenantid);
    }
    /**
     * 向tenant增加用户
     * 
     * @return
     */
    @RequestMapping(value = "/addUser", method = RequestMethod.POST)
    @ResponseBody
    public ActionReturnUtil addUser(@RequestParam(value = "tenantid") String tenantid, @RequestParam(value = "username") String username, @RequestParam(value = "role") String role)
            throws Exception {
        logger.info("向tenant增加用户");
        String[] users = username.split(",");
        if(users.length>=1){
            for (String name : users) {
                this.tenantService.addTenantUser(tenantid, name, role);
            }
        }
        return ActionReturnUtil.returnSuccess();
    }
    /**
     * 向tenant移除用户
     * 
     * @return
     */
    @RequestMapping(value = "/removeUser", method = RequestMethod.DELETE)
    @ResponseBody
    public ActionReturnUtil removeUser(@RequestParam(value = "tenantid") String tenantid, @RequestParam(value = "username") String username) throws Exception {
        logger.info("向tenant移除用户");
        return this.tenantService.removeTenantUser(tenantid, username);
    }
    /**
     * 添加信任白名单
     * 
     * @param tenantid
     * @param
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/addTrustmember", method = RequestMethod.POST)
    @ResponseBody
    public ActionReturnUtil addTrustmember(@RequestParam(value = "tenantid") String tenantid, @RequestParam(value = "trustTenantid") String trustTenantid) throws Exception {
        logger.info("添加信任白名单");
        return this.tenantService.addTrustmember(tenantid, trustTenantid);
    }
    /**
     * 移除信任白名单
     * 
     * @param tenantid
     * @param trustTenantid
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/removeTrustmember", method = RequestMethod.DELETE)
    @ResponseBody
    public ActionReturnUtil removeTrustmember(@RequestParam(value = "tenantid") String tenantid, @RequestParam(value = "trustTenantid") String trustTenantid) throws Exception {
        logger.info("移除信任白名单");
        return this.tenantService.removeTrustmember(tenantid, trustTenantid);
    }
    /**
     * 查询信任白名单列表
     * 
     * @param tenantid
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/listTrustmember", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil listTrustmember(@RequestParam(value = "tenantid") String tenantid) throws Exception {
        logger.info("查询信任白名单列表");
        return ActionReturnUtil.returnSuccessWithData(this.tenantService.listTrustmember(tenantid));
    }
    /**
     * 查询可添加信任白名单租户列表
     * @param tenantid
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/listAvailableTrustmemberTenantList", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil listAvailableTrustmemberTenantList(@RequestParam(value = "tenantid") String tenantid) throws Exception {
        return ActionReturnUtil.returnSuccessWithData(this.tenantService.listAvailableTrustmemberTenantList(tenantid));
    }
    /**
     * 列出tenan下面的资源使用情况
     * 
     * @param tenantid
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/listTenantQuota", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil listTenantQuota(@RequestParam(value = "tenantid") String tenantid) throws Exception {

        logger.info("列出tenan下面的资源使用情况");
        return ActionReturnUtil.returnSuccessWithData(this.tenantService.listTenantQuota(tenantid));

    }
    /**
     * 获取namespace下pod创建时候的nodeSelector的标签
     * 
     * @param tenantid
     * @param namespace
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/podPrivatePartitionLabel", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil getPodPrivatePartitionLabel(@RequestParam(value = "tenantid") String tenantid, @RequestParam(value = "namespace") String namespace) throws Exception {

        logger.info("获取namespace下pod创建时候的nodeSelector的标签");
        return this.namespaceService.getPrivatePartitionLabel(tenantid, namespace);

    }

    @RequestMapping(value = "/listTenantByUserName")
    public @ResponseBody ActionReturnUtil listTenantsByUserName() throws Exception {
        if (session.getAttribute("username") == null) {
            return ActionReturnUtil.returnErrorWithMsg("没有登陆或者登陆超时，请重新登陆！");
        }
        String userName = session.getAttribute("username").toString();
        // 判断是否是admin
        String isAdmin = session.getAttribute("isAdmin").toString();

        return tenantService.listTenantsByUserName(userName, isAdmin.equals("1"));

    }

    @RequestMapping(value = "/listTenantsByUserNameForAudit")
    public @ResponseBody ActionReturnUtil listTenantsByUserNameForAudit() throws Exception {
        if (session.getAttribute("username") == null) {
            return ActionReturnUtil.returnErrorWithMsg("没有登陆或者登陆超时，请重新登陆！");
        }
        String userName = session.getAttribute("username").toString();
        // 判断是否是admin
        String isAdmin = session.getAttribute("isAdmin").toString();

        return tenantService.listTenantsByUserNameForAudit(userName, isAdmin.equals("1"));

    }
    @RequestMapping(value = "/getAllTenantQuotaByClusterId", method = RequestMethod.GET)
    public @ResponseBody ActionReturnUtil getTenantQuotaByClusterId(String clusterId) throws Exception {
        if (StringUtils.isEmpty(clusterId)) {
            return ActionReturnUtil.returnErrorWithMsg("clusterId不能为空！");
        }
        Map tenantQuotaByClusterId = tenantService.getTenantQuotaByClusterId(clusterId);
        return ActionReturnUtil.returnSuccessWithData(tenantQuotaByClusterId);

    }

    /**
     * 删除租户的所有私有镜像
     *
     * @return
     */
    @RequestMapping(value = "/clearImage", method = RequestMethod.DELETE)
    @ResponseBody
    public ActionReturnUtil removeImage(@RequestParam(value = "tenantid") String tenantid) throws Exception {
        logger.info("删除租户的所有的镜像tenantID:"+tenantid);
        return harborProjectTenantService.clearTenantProject(tenantid);
    }
    /**
     * 根据用户名查询是否是admin或者租户管理员
     * @param tenantid
     * @param username
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/isAdmin", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil isAdmin(@RequestParam(value = "tenantid") String tenantid, String username) throws Exception {
        return ActionReturnUtil.returnSuccessWithData(tenantService.isAdmin(tenantid,username));
    }
    @RequestMapping(value = "/user/getRolePrivilege", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil getRolePrivilege(String roleName) throws Exception {
        Map<String, Object> privilegeByRole = rolePrivilegeService.getPrivilegeByRole(roleName);
        return ActionReturnUtil.returnSuccessWithData(privilegeByRole);
    }
}
