package com.harmonycloud.api.tenant;

import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.user.bean.UserRoleRelationship;
import com.harmonycloud.dto.tenant.DevOpsProjectUserDto;
import com.harmonycloud.dto.tenant.ProjectDto;
import com.harmonycloud.dto.user.UserRoleDto;
import com.harmonycloud.service.tenant.ProjectService;
import com.harmonycloud.service.user.RolePrivilegeService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by zgl on 17-12-19.
 */
@Controller
public class ProjectMemberController {

    @Autowired
    ProjectService projectService;
    @Autowired
    RolePrivilegeService rolePrivilegeService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 查询该项目下的所有pm用户
     *
     * @return
     */
    @RequestMapping(value = "/tenants/{tenantId}/projects/{projectId}/projectmember", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil listProjectTm(@PathVariable(value = "tenantId") String tenantId,
                                          @PathVariable(value = "projectId") String projectId) throws Exception {
        logger.info("查询项目下的所有用户角色");
        if (StringUtils.isAnyBlank(tenantId,projectId)){
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        List<Map<String,Object>> userRoles = this.projectService.listProjectUser(tenantId,projectId);
        return ActionReturnUtil.returnSuccessWithData(userRoles);
    }

    /**
     * 创建项目管理员
     * @param projectDto
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/tenants/{tenantId}/projects/{projectId}/projectmember", method = RequestMethod.POST)
    @ResponseBody
    public ActionReturnUtil createPm(@PathVariable(value = "tenantId") String tenantId,
                                                   @PathVariable(value = "projectId") String projectId,
                                                   @ModelAttribute ProjectDto projectDto) throws Exception {
        logger.info("创建项目管理员");
        //空值判断
        List<String> pmList = projectDto.getPmList();
        if (StringUtils.isAnyBlank(tenantId,projectId) || CollectionUtils.isEmpty(pmList)){
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }

        this.projectService.createPm(tenantId,projectId, pmList);
        return ActionReturnUtil.returnSuccess();

    }
    @RequestMapping(value = "/tenants/{tenantId}/projects/{projectId}/projectmember/{username}", method = RequestMethod.DELETE)
    @ResponseBody
    public ActionReturnUtil deletePm(@PathVariable(value = "tenantId") String tenantId,
                                     @PathVariable(value = "projectId") String projectId,
                                     @PathVariable(value = "username") String username) throws Exception {
        logger.info("删除项目管理员");
        //空值判断
        if (StringUtils.isAnyBlank(projectId,tenantId,username)){
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        this.projectService.deletePm(tenantId,projectId, username,Boolean.FALSE);
        return ActionReturnUtil.returnSuccess();

    }
    @RequestMapping(value = "/tenants/{tenantId}/projects/{projectId}/projectmember/projectRole", method = RequestMethod.POST)
    @ResponseBody
    public ActionReturnUtil addUserRole(@PathVariable(value = "tenantId") String tenantId,
                                           @PathVariable(value = "projectId") String projectId,
                                           @ModelAttribute UserRoleDto userRoleDto) throws Exception {
        logger.info("项目添加角色");
        //空值判断
        if (CollectionUtils.isEmpty(userRoleDto.getRoleIdList()) || CollectionUtils.isEmpty(userRoleDto.getUsernameList())){
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        userRoleDto.setTenantId(tenantId);
        userRoleDto.setProjectId(projectId);
        this.projectService.addUserRole(userRoleDto);
        return ActionReturnUtil.returnSuccess();

    }
    @RequestMapping(value = "/tenants/{tenantId}/projects/{projectId}/projectmember/{username}/projectRole", method = RequestMethod.DELETE)
    @ResponseBody
    public ActionReturnUtil removeUserRole(@PathVariable(value = "tenantId") String tenantId,
                                           @PathVariable(value = "projectId") String projectId,
                                           @PathVariable(value = "username") String username,
                                           @ModelAttribute UserRoleDto userRoleDto) throws Exception {
        logger.info("项目移除角色");
        //空值判断
        if (Objects.isNull(userRoleDto.getRoleId())){
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        userRoleDto.setTenantId(tenantId);
        userRoleDto.setProjectId(projectId);
        userRoleDto.setUsername(username);
        this.projectService.removeUserRole(userRoleDto);
        return ActionReturnUtil.returnSuccess();

    }
}
