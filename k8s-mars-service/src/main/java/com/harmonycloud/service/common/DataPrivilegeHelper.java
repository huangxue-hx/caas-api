package com.harmonycloud.service.common;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.DataPrivilegeField;
import com.harmonycloud.common.enumm.DataPrivilegeType;
import com.harmonycloud.common.enumm.DataResourceTypeEnum;
import com.harmonycloud.dao.dataprivilege.bean.DataPrivilegeGroupMapping;
import com.harmonycloud.dao.dataprivilege.bean.DataPrivilegeGroupMember;
import com.harmonycloud.dto.dataprivilege.DataPrivilegeDto;
import com.harmonycloud.service.dataprivilege.DataPrivilegeGroupMappingService;
import com.harmonycloud.service.dataprivilege.DataPrivilegeGroupMemberService;
import com.harmonycloud.service.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpSession;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 数据权限帮助类
 * Created by chencheng on 18-6-21
 */
@Service
public class DataPrivilegeHelper {

    @Autowired
    DataPrivilegeGroupMemberService dataPrivilegeGroupMemberService;
    @Autowired
    DataPrivilegeGroupMappingService dataPrivilegeGroupMappingService;
    @Autowired
    UserService userService;

    private static Logger logger = LoggerFactory.getLogger(DataPrivilegeHelper.class);
    @Autowired
    private HttpSession session;


    /**
     * 过滤结果
     * @param list
     * @param <T>
     * @return
     * @throws Exception
     */
    public <T> List<T> filter(List<T> list) throws Exception{

        if(CollectionUtils.isEmpty(list)){
            return list;
        }

        T obj = null;
        for (T t:list) {
            if (!Objects.isNull(t)){
                obj = t;
                break;
            }
        }
        if (Objects.isNull(obj)){
            return list;
        }

        //当前用户的用户名
        String username = (String)session.getAttribute(CommonConstant.USERNAME);
        List<T> resultList = new ArrayList<>();

        Integer roGroupId = null;//只读权限列表groupId
        Integer rwGroupId = null;//可读写权限列表groupId

        for (T t:list) {
            DataPrivilegeDto dataPrivilegeDto = this.getDataPrivilegeDto(t);

            List<DataPrivilegeGroupMapping> mappingList = dataPrivilegeGroupMappingService.listDataPrivilegeGroupMapping(dataPrivilegeDto);

            if(CollectionUtils.isEmpty(mappingList)){
                return null;
            }
            for (DataPrivilegeGroupMapping mapping : mappingList) {
                if(mapping.getPrivilegeType() == CommonConstant.DATA_READONLY){
                    roGroupId = mapping.getGroupId();
                }else if(mapping.getPrivilegeType() == CommonConstant.DATA_READWRITE){
                    rwGroupId = mapping.getGroupId();
                }

            }

            List<DataPrivilegeGroupMember> roList = dataPrivilegeGroupMemberService.listMemberInGroup(roGroupId);
            List<DataPrivilegeGroupMember> rwList = dataPrivilegeGroupMemberService.listMemberInGroup(rwGroupId);

            if(!CollectionUtils.isEmpty(roList)){
                List<DataPrivilegeGroupMember> members = null;
                members = roList.stream().filter(member->{ return username.equals(member.getUsername());}).collect(Collectors.toList());
                if(!CollectionUtils.isEmpty(members)){
                    resultList.add(t);
                    continue;
                }
            }

            if(!CollectionUtils.isEmpty(rwList)){
                List<DataPrivilegeGroupMember> members = null;
                members = rwList.stream().filter(member->{ return username.equals(member.getUsername());}).collect(Collectors.toList());
                if(!CollectionUtils.isEmpty(members)){
                    resultList.add(t);
                    continue;
                }
            }

        }

        return resultList;
    }




    /**
     * 获取数据的值与过滤字段的值
     * @param t
     * @param <T>
     * @return
     * @throws IllegalAccessException
     */
    private <T> DataPrivilegeDto getDataPrivilegeDto(T t) throws IllegalAccessException {
        DataPrivilegeDto dataPrivilegeDto = new DataPrivilegeDto();
        DataPrivilegeType anotation = t.getClass().getDeclaredAnnotation(DataPrivilegeType.class);
        DataResourceTypeEnum dataPrivilegeType = anotation.type();
        dataPrivilegeDto.setDataResourceType(dataPrivilegeType.getCode());
        Field[] fields = t.getClass().getDeclaredFields();

        for(Field field : fields){
            DataPrivilegeField fieldAnnotation = field.getAnnotation(DataPrivilegeField.class);
            if(fieldAnnotation == null){
                continue;
            }
            field.setAccessible(true);
            String value = field.get(t) == null ? null:field.get(t).toString();
            switch(fieldAnnotation.type()){
                case CommonConstant.DATA_FIELD:
                    dataPrivilegeDto.setData(value);
                    break;
                case CommonConstant.PROJECTID_FIELD:
                    dataPrivilegeDto.setProjectId(value);
                    break;
                case CommonConstant.CLUSTERID_FIELD:
                    dataPrivilegeDto.setClusterId(value);
                    break;
                case CommonConstant.NAMESPACE_FIELD:
                    dataPrivilegeDto.setNamespace(value);
                    break;
            }
        }
        return dataPrivilegeDto;
    }

}
