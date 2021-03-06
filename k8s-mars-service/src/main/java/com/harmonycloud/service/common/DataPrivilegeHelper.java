package com.harmonycloud.service.common;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.DataPrivilegeField;
import com.harmonycloud.common.enumm.DataPrivilegeType;
import com.harmonycloud.common.enumm.DataResourceTypeEnum;
import com.harmonycloud.common.util.StringUtil;
import com.harmonycloud.dao.dataprivilege.bean.DataPrivilegeGroupMapping;
import com.harmonycloud.dao.dataprivilege.bean.DataPrivilegeGroupMember;
import com.harmonycloud.dto.dataprivilege.DataPrivilegeDto;
import com.harmonycloud.service.dataprivilege.DataPrivilegeGroupMappingService;
import com.harmonycloud.service.dataprivilege.DataPrivilegeGroupMemberService;
import com.harmonycloud.service.dataprivilege.DataPrivilegeService;
import com.harmonycloud.service.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpSession;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据权限帮助类
 * Created by chencheng on 18-6-21
 */
@Service
public class DataPrivilegeHelper {

    @Autowired
    private DataPrivilegeGroupMemberService dataPrivilegeGroupMemberService;
    @Autowired
    private DataPrivilegeGroupMappingService dataPrivilegeGroupMappingService;
    @Autowired
    private UserService userService;
    @Autowired
    private DataPrivilegeService dataPrivilegeService;

    private static Logger logger = LoggerFactory.getLogger(DataPrivilegeHelper.class);
    @Autowired
    private HttpSession session;

    public static final String DATA_PRIVILEGE = "dataPrivilege";
    public static final String READONLY = "ro";
    public static final String READWRITE = "rw";
    public static final String READNONE = "rn";
    private final String ID_FILED = "id";
    private final String DATA_FILED = "name";
    private final String CLUSTERID_FIELD = "clusterId";
    private final String PROJECTID_FIELD = "projectId";
    private final String NAMESPACE_FIELD = "namespace";

    private final String SETDATAPRIVILEGE_METHOD = "setDataPrivilege";

    /**
     * 过滤对象列表结果
     * @param list
     * @param <T>
     * @return
     * @throws Exception
     */
    public <T> List<T> filter(List<T> list) throws Exception{
        if(list == null){
            return null;
        }else if(CollectionUtils.isEmpty(list)){
            return Collections.EMPTY_LIST;
        }

        T obj = null;
        for (T t:list) {
            if (!Objects.isNull(t)){
                obj = t;
                break;
            }
        }
        if (Objects.isNull(obj)){
            return null;
        }
        List<T> resultList = new ArrayList<T>();

        for (T t:list) {
            T filterT = filter(t);
            if(filterT != null){
                resultList.add(filterT);
            }
        }

        return resultList;
    }

    /**
     * 过滤对象
     * @param t
     * @param isDisplay
     * @param <T>
     * @return
     * @throws Exception
     */
    public <T> T filter(T t, boolean isDisplay) throws Exception {
        if(Objects.isNull(t)){
            return null;
        }

        int currentRoleId = userService.getCurrentRoleId();
        if(currentRoleId <= CommonConstant.NUM_ROLE_PM){
            t.getClass().getMethod(SETDATAPRIVILEGE_METHOD, String.class).invoke(t, READWRITE);
            return t;
        }

        String username = (String)session.getAttribute(CommonConstant.USERNAME);

        Integer roGroupId = null;//只读权限列表groupId
        Integer rwGroupId = null;//可读写权限列表groupId

        DataPrivilegeDto dataPrivilegeDto = this.getDataPrivilegeDto(t);

        List<DataPrivilegeGroupMapping> mappingList = dataPrivilegeGroupMappingService.listDataPrivilegeGroupMapping(dataPrivilegeDto);

        if(CollectionUtils.isEmpty(mappingList)){
            t.getClass().getMethod(SETDATAPRIVILEGE_METHOD, String.class).invoke(t, READWRITE);
            return t;
        }
        for (DataPrivilegeGroupMapping mapping : mappingList) {
            if(mapping.getPrivilegeType() == CommonConstant.DATA_READONLY){
                roGroupId = mapping.getGroupId();
            }else if(mapping.getPrivilegeType() == CommonConstant.DATA_READWRITE){
                rwGroupId = mapping.getGroupId();
            }

        }

        boolean readable = false;

        List<DataPrivilegeGroupMember> roList = dataPrivilegeGroupMemberService.listMemberInGroup(roGroupId);


        if(!CollectionUtils.isEmpty(roList)){
            List<DataPrivilegeGroupMember> members = null;
            members = roList.stream().filter(member->{ return username.equals(member.getUsername());}).collect(Collectors.toList());
            if(!CollectionUtils.isEmpty(members)){
                readable = true;
                t.getClass().getMethod(SETDATAPRIVILEGE_METHOD, String.class).invoke(t, READONLY);
            }
        }

        List<DataPrivilegeGroupMember> rwList = dataPrivilegeGroupMemberService.listMemberInGroup(rwGroupId);

        if(!CollectionUtils.isEmpty(rwList)){
            List<DataPrivilegeGroupMember> members = null;
            members = rwList.stream().filter(member->{ return username.equals(member.getUsername());}).collect(Collectors.toList());
            if(!CollectionUtils.isEmpty(members)){
                readable = true;
                t.getClass().getMethod(SETDATAPRIVILEGE_METHOD, String.class).invoke(t, READWRITE);
            }
        }
        if(!readable){
            if(!isDisplay) {
                return null;
            }else{
                t.getClass().getMethod(SETDATAPRIVILEGE_METHOD, String.class).invoke(t, READNONE);
            }
        }
        return t;
    }

    /**
     * 过滤对象
     * @param t
     * @param <T>
     * @return
     * @throws Exception
     */
    public <T> T filter(T t) throws Exception {
        return this.filter(t, false);
    }


    /**
     * 过滤mapList
     * @param list
     * @param dataResourceType
     * @return
     * @throws Exception
     */
    public List<Map> filterMap(List<Map> list, DataResourceTypeEnum dataResourceType) throws Exception {
        List<Map> resultList = new ArrayList<>();

        for(Map<String, Object> map : list){
            Map resultMap = this.filterMap(map, dataResourceType);
            if(resultMap != null){
                resultList.add(resultMap);
            }
        }
        return resultList;
    }

    /**
     * 过滤map
     * @param map
     * @param dataResourceType
     * @return
     * @throws Exception
     */
    public Map filterMap(Map map, DataResourceTypeEnum dataResourceType) throws Exception {
        DataPrivilegeDto dataPrivilegeDto = this.getDataPrivilegeDto(map, dataResourceType);
        Map resultMap = this.filterMap(map, dataPrivilegeDto);
        return resultMap;
    }


    /**
     * 过滤map
     * @param map
     * @param dataPrivilegeDto
     * @return
     * @throws Exception
     */
    public Map filterMap(Map map, DataPrivilegeDto dataPrivilegeDto) throws Exception {
        if(Objects.isNull(map)){
            return null;
        }
        int currentRoleId = userService.getCurrentRoleId();
        if(currentRoleId <= CommonConstant.NUM_ROLE_PM){
            map.put(DATA_PRIVILEGE, READWRITE);
            return map;
        }

        String username = (String)session.getAttribute(CommonConstant.USERNAME);
        Integer roGroupId = null;//只读权限列表groupId
        Integer rwGroupId = null;//可读写权限列表groupId

        List<DataPrivilegeGroupMapping> mappingList = dataPrivilegeGroupMappingService.listDataPrivilegeGroupMapping(dataPrivilegeDto);

        if(CollectionUtils.isEmpty(mappingList)){
            map.put(DATA_PRIVILEGE, READWRITE);
            return map;
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

        if(!CollectionUtils.isEmpty(rwList)){
            List<DataPrivilegeGroupMember> members = null;
            members = rwList.stream().filter(member->{ return username.equals(member.getUsername());}).collect(Collectors.toList());
            if(!CollectionUtils.isEmpty(members)){
                map.put(DATA_PRIVILEGE, READWRITE);
            }
        }
        if(!CollectionUtils.isEmpty(roList)){
            List<DataPrivilegeGroupMember> members = null;
            members = roList.stream().filter(member->{ return username.equals(member.getUsername());}).collect(Collectors.toList());
            if(!CollectionUtils.isEmpty(members)){
                map.put(DATA_PRIVILEGE, READONLY);
            }
        }
        if(map.get(DATA_PRIVILEGE) == null){
            return  null;
        }

        return map;
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

    private DataPrivilegeDto getDataPrivilegeDto(Map map, DataResourceTypeEnum dataResourceTypeEnum){
        DataPrivilegeDto dataPrivilegeDto = new DataPrivilegeDto();
        switch(dataResourceTypeEnum){
            case APPLICATION:
            case SERVICE:
                dataPrivilegeDto.setData(StringUtil.valueOf(map.get(DATA_FILED)));
                break;
            case PIPELINE:
                dataPrivilegeDto.setData(StringUtil.valueOf(map.get(ID_FILED)));
                break;
        }
        dataPrivilegeDto.setProjectId(StringUtil.valueOf(map.get(PROJECTID_FIELD)));
        dataPrivilegeDto.setClusterId(StringUtil.valueOf(map.get(CLUSTERID_FIELD)));
        dataPrivilegeDto.setNamespace(StringUtil.valueOf(map.get(NAMESPACE_FIELD)));
        return dataPrivilegeDto;
    }

    /**
     * 过滤服务名
     * @param serviceName
     * @param namespace
     * @return
     * @throws Exception
     */
    public Map filterServiceName(Map map, String serviceName, String namespace) throws Exception {
        DataPrivilegeDto dataPrivilegeDto = new DataPrivilegeDto();
        dataPrivilegeDto.setData(serviceName);
        dataPrivilegeDto.setNamespace(namespace);
        dataPrivilegeDto.setDataResourceType(DataResourceTypeEnum.SERVICE.getCode());
        if(this.filterMap(map, dataPrivilegeDto) == null){
            map.put(DATA_PRIVILEGE, READNONE);
        }
        return map;
    }

    /**
     * 判断资源是否可读
     * @param dataPrivilegeDto
     * @return
     * @throws Exception
     */
    private boolean isReadable(DataPrivilegeDto dataPrivilegeDto) throws Exception {
        List<DataPrivilegeGroupMapping> mappingList = dataPrivilegeGroupMappingService.listDataPrivilegeGroupMapping(dataPrivilegeDto);
        Integer roGroupId = null;
        Integer rwGroupId = null;
        if(CollectionUtils.isEmpty(mappingList)){
            return true;
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
        List<DataPrivilegeGroupMember> userList = new ArrayList<>();
        userList.addAll(roList);
        userList.addAll(rwList);
        List<Integer> userIdList = userList.stream().map(DataPrivilegeGroupMember::getMemberId).collect(Collectors.toList());
        int currentUserId = userService.getCurrentUserId().intValue();
        if(userIdList.contains(currentUserId)){
            return true;
        }else {
            return false;
        }
    }

}
