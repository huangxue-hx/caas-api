package com.harmonycloud.service.dataprivilege.impl;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.DataPrivilegeField;
import com.harmonycloud.common.enumm.DataPrivilegeType;
import com.harmonycloud.common.enumm.DataResourceTypeEnum;
import com.harmonycloud.dao.dataprivilege.DataPrivilegeStrategyMapper;
import com.harmonycloud.dao.dataprivilege.bean.DataPrivilegeGroupMapping;
import com.harmonycloud.dao.dataprivilege.bean.DataPrivilegeGroupMappingExample;
import com.harmonycloud.dao.dataprivilege.bean.DataPrivilegeStrategy;
import com.harmonycloud.dao.dataprivilege.bean.DataPrivilegeStrategyExample;
import com.harmonycloud.dto.dataprivilege.DataPrivilegeDto;
import com.harmonycloud.service.dataprivilege.DataPrivilegeGroupMappingService;
import com.harmonycloud.service.dataprivilege.DataPrivilegeGroupMemberService;
import com.harmonycloud.service.dataprivilege.DataPrivilegeGroupService;
import com.harmonycloud.service.dataprivilege.DataPrivilegeService;
import com.harmonycloud.service.tenant.TenantService;
import com.harmonycloud.service.user.UserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * Created by anson on 18/6/20.
 */
@Service
public class DataPrivilegeServiceImpl implements DataPrivilegeService{
    private static Logger logger = LoggerFactory.getLogger(DataPrivilegeServiceImpl.class);

    @Autowired
    DataPrivilegeStrategyMapper dataPrivilegeStrategyMapper;

    @Autowired
    DataPrivilegeGroupService dataPrivilegeGroupService;

    @Autowired
    DataPrivilegeGroupMappingService dataPrivilegeGroupMappingService;

    @Autowired
    DataPrivilegeGroupMemberService dataPrivilegeGroupMemberService;

    @Autowired
    TenantService tenantService;

    @Autowired
    UserService userService;

    @Autowired
    HttpSession session;

    /**
     * 增加资源数据
     * @param t
     * @param <T>
     * @throws Exception
     */
    @Override
    public <T> void addResource(T t, String parentData, DataResourceTypeEnum type) throws Exception {
        Long userId = (Long)session.getAttribute(CommonConstant.USERID);
        String projectId = userService.getCurrentProjectId();
        String tenantId = userService.getCurrentTenantId();
        int strategy = CommonConstant.DATA_OPEN_STRATEGY;

        DataPrivilegeDto dataPrivilegeDto = this.getDataPrivilegeDto(t);
        dataPrivilegeDto.setParentData(parentData);
        if(type != null){
            dataPrivilegeDto.setParentDataResourceType(type.getCode());
        }
        dataPrivilegeDto.setCreatorId(userId);

        if(StringUtils.isNotBlank(tenantId)) {
            DataPrivilegeStrategyExample example = new DataPrivilegeStrategyExample();
            example.createCriteria().andScopeTypeEqualTo(CommonConstant.SCOPE_TENANT).andScopeIdEqualTo(tenantId);
            List<DataPrivilegeStrategy> dataPrivilegeStrategyList = dataPrivilegeStrategyMapper.selectByExample(example);

            if(dataPrivilegeStrategyList != null && dataPrivilegeStrategyList.size() == 1){
                DataPrivilegeStrategy dataPrivilegeStrategy = dataPrivilegeStrategyList.get(0);
                strategy = dataPrivilegeStrategy.getStrategy();

            }
        }

        //新建只读组与读写组
        int roGroupId = dataPrivilegeGroupService.addGroup(CommonConstant.DATA_GROUP, null, null);
        int rwGroupId = dataPrivilegeGroupService.addGroup(CommonConstant.DATA_GROUP, null, null);


        //初始化数据与只读组、读写组的关联
        Map<Integer, Object> parentGroupMap = dataPrivilegeGroupMappingService.initMapping(roGroupId, rwGroupId, dataPrivilegeDto);
        Integer parentRoGroupId = (Integer)parentGroupMap.get(CommonConstant.DATA_READONLY);
        Integer parentRwGroupId = (Integer)parentGroupMap.get(CommonConstant.DATA_READWRITE);

        //根据策略增加读写组与只读组的成员
        List<String> rwUserList;
        switch(strategy){
            case CommonConstant.DATA_CLOSED_STRATEGY:
                rwUserList = dataPrivilegeGroupMemberService.initGroupMember(rwGroupId, null, parentRwGroupId, CommonConstant.DATA_READWRITE, null);
                dataPrivilegeGroupMemberService.initGroupMember(roGroupId, null, parentRoGroupId, CommonConstant.DATA_READONLY, rwUserList);
                break;
            case CommonConstant.DATA_SEMIOPEN_STRATEGY:
                rwUserList = dataPrivilegeGroupMemberService.initGroupMember(rwGroupId, null, parentRwGroupId, CommonConstant.DATA_READWRITE, null);
                dataPrivilegeGroupMemberService.initGroupMember(roGroupId, projectId, parentRoGroupId, CommonConstant.DATA_READONLY, rwUserList);
                break;
            case CommonConstant.DATA_OPEN_STRATEGY:
                rwUserList = dataPrivilegeGroupMemberService.initGroupMember(rwGroupId, projectId, parentRwGroupId, CommonConstant.DATA_READWRITE, null);
                dataPrivilegeGroupMemberService.initGroupMember(roGroupId, null, parentRoGroupId, CommonConstant.DATA_READONLY, rwUserList);
                break;
        }

        //读写组中加入管理员权限组
        //dataPrivilegeGroupMemberService.addAdminGroupToGroup(rwGroupId);

    }

    /**
     * 删除资源数据
     * @param t
     * @param <T>
     * @throws Exception
     */
    @Override
    public <T> void deleteResource(T t) throws Exception {
        DataPrivilegeDto dataPrivilegeDto = this.getDataPrivilegeDto(t);
        DataPrivilegeGroupMappingExample example = new DataPrivilegeGroupMappingExample();
        DataPrivilegeGroupMappingExample.Criteria criteria = example.createCriteria().andDataNameEqualTo(dataPrivilegeDto.getData())
                .andResourceTypeIdEqualTo(dataPrivilegeDto.getDataResourceType());
        if(StringUtils.isNotBlank(dataPrivilegeDto.getProjectId())) {
            criteria.andProjectIdEqualTo(dataPrivilegeDto.getProjectId());
        }
        if(StringUtils.isNotBlank(dataPrivilegeDto.getClusterId())) {
            criteria.andClusterIdEqualTo(dataPrivilegeDto.getClusterId());
        }
        if(StringUtils.isNotBlank(dataPrivilegeDto.getNamespace())) {
            criteria.andNamespaceEqualTo(dataPrivilegeDto.getNamespace());
        }
        List<DataPrivilegeGroupMapping> list = dataPrivilegeGroupMappingService.getDataPrivilegeGroupMapping(example);
        for(DataPrivilegeGroupMapping dataPrivilegeGroupMapping : list){
            int groupId = dataPrivilegeGroupMapping.getGroupId();
            dataPrivilegeGroupService.deleteGroupWithMember(groupId);
            dataPrivilegeGroupMappingService.deleteMappingById(dataPrivilegeGroupMapping.getId());
        }

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

    public static void main(String[] args) {


    }
}
