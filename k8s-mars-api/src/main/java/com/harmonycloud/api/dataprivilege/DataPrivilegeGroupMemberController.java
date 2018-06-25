package com.harmonycloud.api.dataprivilege;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.dataprivilege.bean.DataPrivilegeGroupMapping;
import com.harmonycloud.dao.dataprivilege.bean.DataPrivilegeGroupMember;
import com.harmonycloud.dto.dataprivilege.DataPrivilegeDto;
import com.harmonycloud.service.dataprivilege.DataPrivilegeGroupMappingService;
import com.harmonycloud.service.dataprivilege.DataPrivilegeGroupMemberService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;


/**
 * Created by chencheng on 18-6-20
 */
@Controller
@RequestMapping("privilege")
public class DataPrivilegeGroupMemberController {

    @Autowired
    DataPrivilegeGroupMemberService dataPrivilegeGroupMemberService;

    @Autowired
    DataPrivilegeGroupMappingService dataPrivilegeGroupMappingService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 获取资源的两个权限列表（可读写权限列表，只读权限列表）
     * @param dataPrivilegeDto
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/resources/",method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil listMemberInGroup(@ModelAttribute DataPrivilegeDto dataPrivilegeDto) throws Exception{


        if (StringUtils.isBlank(dataPrivilegeDto.getData())){
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }

        Integer roGroupId = null;//只读权限列表groupId

        Integer rwGruopId = null;//可读写权限列表groupId


        List<DataPrivilegeGroupMapping> mappingList = dataPrivilegeGroupMappingService.listDataPrivilegeGroupMapping(dataPrivilegeDto);

        if(CollectionUtils.isEmpty(mappingList)){
            return null;
        }
        for (DataPrivilegeGroupMapping mapping : mappingList) {
            if(mapping.getPrivilegeType() == CommonConstant.DATA_READONLY){
                roGroupId = mapping.getGroupId();
            }else if(mapping.getPrivilegeType() == CommonConstant.DATA_READWRITE){
                rwGruopId = mapping.getGroupId();
            }

        }


        HashMap<String,Object> resultMap = new HashMap<String,Object>();
        if(roGroupId != null) {
            List<DataPrivilegeGroupMember> roMemberList = dataPrivilegeGroupMemberService.listMemberInGroup(roGroupId);
            resultMap.put("roList",roMemberList);
        }

        if(rwGruopId != null) {
            List<DataPrivilegeGroupMember> rwMemberList = dataPrivilegeGroupMemberService.listMemberInGroup(rwGruopId);
            resultMap.put("rwList",rwMemberList);
        }

        return ActionReturnUtil.returnSuccessWithData(resultMap);
    }

    /**
     * 向组中添加成员
     * @param username
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/group/{groupId}/user/{username}",method = RequestMethod.POST)
    @ResponseBody
    public ActionReturnUtil addMemberToGroup(@PathVariable("groupId") Integer groupId,
                                             @PathVariable("username") String username)throws Exception{

        if (username != null && groupId != null ){
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }

        DataPrivilegeGroupMember dataPrivilegeGroupMember = new DataPrivilegeGroupMember();
        dataPrivilegeGroupMember.setGroupId(groupId);
        dataPrivilegeGroupMember.setUsername(username);

        dataPrivilegeGroupMemberService.addMemberToGroup(dataPrivilegeGroupMember);

        return  ActionReturnUtil.returnSuccess();
    }


    /**
     * 删除组中的成员
     * @param username
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/group/{groupId}/user/{userId}",method = RequestMethod.DELETE)
    @ResponseBody
    public ActionReturnUtil delMemberFromGroup(@PathVariable("groupId") Integer groupId,
                                               @PathVariable("username") String username)throws Exception{

        if (username != null && groupId != null ){
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }

        DataPrivilegeGroupMember dataPrivilegeGroupMember = new DataPrivilegeGroupMember();
        dataPrivilegeGroupMember.setGroupId(groupId);
        dataPrivilegeGroupMember.setUsername(username);

        dataPrivilegeGroupMemberService.delMemberFromGroup(dataPrivilegeGroupMember);


        return ActionReturnUtil.returnSuccess();
    }

}
