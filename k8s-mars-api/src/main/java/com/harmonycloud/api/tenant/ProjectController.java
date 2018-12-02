package com.harmonycloud.api.tenant;

import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.AssertUtil;
import com.harmonycloud.dao.tenant.bean.Project;
import com.harmonycloud.dao.user.bean.Role;
import com.harmonycloud.dao.user.bean.UserRoleRelationship;
import com.harmonycloud.dto.tenant.DevOpsProjectUserDto;
import com.harmonycloud.dto.tenant.ProjectDto;
import com.harmonycloud.service.tenant.ProjectService;
import com.harmonycloud.service.user.RolePrivilegeService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Created by zgl on 17-12-19.
 */
@Controller
public class ProjectController {

    @Autowired
    private ProjectService projectService;
    @Autowired
    private RolePrivilegeService rolePrivilegeService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 切换项目
     * @param tenantId
     * @param projectId
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/tenant/{tenantId}/project/{projectId}/switchProject", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil switchProject(@PathVariable("tenantId") String tenantId,
                                          @PathVariable("projectId") String projectId) throws Exception {
        List<Role> roleList = this.projectService.switchProject(tenantId, projectId);
        return ActionReturnUtil.returnSuccessWithData(roleList);
    }

    /**
     * 根据项目id查询项目详情
     *
     * @param projectId
     * @return
     */
    @RequestMapping(value = "/tenants/{tenantId}/projects/{projectId}", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil getProjectDetailByProjectId(@PathVariable(value = "tenantId") String tenantId,
                                                        @PathVariable(value = "projectId") String projectId) throws Exception {

//        logger.info("根据id查询项目详情");
        if (StringUtils.isAnyBlank(projectId,tenantId)){
            throw new MarsRuntimeException(ErrorCodeMessage.PROJECTID_NOT_BLANK);
        }
        ProjectDto projectDto = projectService.getProjectDetailByProjectId(tenantId,projectId);
        return ActionReturnUtil.returnSuccessWithData(projectDto);
    }

    /**
     * 创建项目
     *
     * @return
     */
    @RequestMapping(value = "/tenants/{tenantId}/projects", method = RequestMethod.POST)
    @ResponseBody
    public ActionReturnUtil createProject(@PathVariable(value = "tenantId") String tenantId,
                                          @ModelAttribute ProjectDto projectDto) throws Exception {
//        logger.info("创建项目");
        //项目名空值判断
        if (StringUtils.isEmpty(projectDto.getProjectName()) || StringUtils.isEmpty(tenantId)) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        projectDto.setTenantId(tenantId);
        projectService.createProject(projectDto);
        return ActionReturnUtil.returnSuccess();
    }

    /**
     * 根据项目id删除项目
     * @param tenantId
     * @param projectId
     * @return
     */
    @RequestMapping(value = "/tenants/{tenantId}/projects/{projectId}", method = RequestMethod.DELETE)
    @ResponseBody
    public ActionReturnUtil deleteProject(@PathVariable(value = "tenantId") String tenantId,
                                          @PathVariable(value = "projectId") String projectId) throws Exception {
//        logger.info("根据项目id删除项目");
        if (StringUtils.isAnyBlank(tenantId,projectId)) {
            throw new MarsRuntimeException(ErrorCodeMessage.PROJECTID_NOT_BLANK);
        }
        projectService.deleteProjectByProjectId(tenantId,projectId);
        return ActionReturnUtil.returnSuccess();
    }
    /**
     * 根据项目id修改项目名称或者备注
     * @param tenantId
     * @param projectId
     * @param projectDto
     * @return
     */
    @RequestMapping(value = "/tenants/{tenantId}/projects/{projectId}", method = RequestMethod.PUT)
    @ResponseBody
    public ActionReturnUtil updateProject(@PathVariable(value = "tenantId") String tenantId,
                                          @PathVariable(value = "projectId") String projectId,
                                          @ModelAttribute ProjectDto projectDto) throws Exception {
//        logger.info("修改项目");
        if (StringUtils.isAnyBlank(tenantId,projectId)) {
            throw new MarsRuntimeException(ErrorCodeMessage.PROJECTID_NOT_BLANK);
        }
        projectDto.setTenantId(tenantId);
        projectDto.setProjectId(projectId);
        projectService.updateProject(projectDto);
        return ActionReturnUtil.returnSuccess();
    }

    /**
     * 根据用户名与租户id查询租户下项目列表
     * @param tenantId
     * @param username
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/tenants/{tenantId}/projects", method = RequestMethod.GET)
    public @ResponseBody ActionReturnUtil listProjectsByUserName(@PathVariable(value = "tenantId") String tenantId,
                                                                String username) throws Exception {
//        logger.info("根据用户名与租户id查询租户下项目列表");
        //空值判断
        if (StringUtils.isAnyBlank(tenantId)){
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        List<ProjectDto> projects = null;
        if (StringUtils.isBlank(username)){
            projects = projectService.listTenantProjectByTenantid(tenantId);
        }else {
            projects = this.projectService.listTenantProjectByUsernameDetail(tenantId, username.trim());
        }
        return ActionReturnUtil.returnSuccessWithData(projects);
    }


}
