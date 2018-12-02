package com.harmonycloud.api.dataprivilege;

import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.dataprivilege.DataPrivilegeDto;
import com.harmonycloud.service.dataprivilege.DataPrivilegeGroupMappingService;
import com.harmonycloud.service.dataprivilege.DataPrivilegeGroupMemberService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


/**
 * Created by chencheng on 18-6-20
 */
@Api(description = "查询及增删数据权限用户列表")
@Controller
@RequestMapping("privilege")
public class DataPrivilegeGroupMemberController {

    @Autowired
    private DataPrivilegeGroupMemberService dataPrivilegeGroupMemberService;

    @Autowired
    private DataPrivilegeGroupMappingService dataPrivilegeGroupMappingService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 获取资源的两个权限列表（可读写权限列表，只读权限列表）
     * @param dataPrivilegeDto
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "获取资源数据权限列表", notes = "根据数据名称和相关条件")
    @RequestMapping(value = "/group",method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil listMemberInGroup(@ModelAttribute DataPrivilegeDto dataPrivilegeDto) throws Exception{
        Map<String,Object> resultMap = dataPrivilegeGroupMemberService.listGroupMemberForData(dataPrivilegeDto);
        return ActionReturnUtil.returnSuccessWithData(resultMap);
    }

    /**
     * 向组中添加成员
     * @param username
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "数据权限成员列表中新增用户")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "groupId", value = "组Id", paramType = "path",dataType = "Integer"),
            @ApiImplicitParam(name = "userId", value = "用户Id", paramType = "query", dataType = "Integer"),
            @ApiImplicitParam(name = "username", value = "用户名", paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "otherGroupId", value = "另一个组Id", paramType = "query", dataType = "Integer")})
    @RequestMapping(value = "/group/{groupId}/user",method = RequestMethod.POST)
    @ResponseBody
    public ActionReturnUtil addMemberToGroup(@PathVariable("groupId") Integer groupId,
                                             @RequestParam(value = "groupType", required = false) Integer groupType,
                                             @RequestParam(value = "userId", required = false) Integer userId,
                                             @RequestParam(value = "username", required = false) String username,
                                             @RequestParam(value = "otherGroupId", required = false) Integer otherGroupId)throws Exception{
        if (StringUtils.isBlank(username) || userId == null){
            throw new MarsRuntimeException(ErrorCodeMessage.USER_NOT_EXIST);
        }
        dataPrivilegeGroupMemberService.verifyMember(groupId, otherGroupId, username, true, groupType);
        dataPrivilegeGroupMemberService.delMemberFromPrivilegeGroup(Integer.valueOf(otherGroupId), username);
        dataPrivilegeGroupMemberService.addMemberToPrivilegeGroup(groupId, userId, username);

        return  ActionReturnUtil.returnSuccess();
    }


    /**
     * 删除组中的成员
     * @param username
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "数据权限成员列表中删除用户")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "groupId", value = "组Id", paramType = "path",dataType = "Integer"),
            @ApiImplicitParam(name = "username", value = "用户名", paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "otherGroupId", value = "另一个组Id", paramType = "query", dataType = "Integer")})
    @RequestMapping(value = "/group/{groupId}/user",method = RequestMethod.DELETE)
    @ResponseBody
    public ActionReturnUtil delMemberFromGroup(@PathVariable("groupId") Integer groupId,
                                               @RequestParam(value = "username") String username,
                                               @RequestParam(value = "otherGroupId") Integer otherGroupId)throws Exception{

        if (StringUtils.isBlank(username) || groupId == null ){
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        dataPrivilegeGroupMemberService.verifyMember(groupId, otherGroupId, username, false, null);
        dataPrivilegeGroupMemberService.delMemberFromPrivilegeGroup(groupId, username);

        return ActionReturnUtil.returnSuccess();
    }

}
