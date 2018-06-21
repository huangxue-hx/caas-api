package com.harmonycloud.service.dataprivilege.impl;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.DataPrivilegeType;
import com.harmonycloud.common.enumm.DataResourceTypeEnum;
import com.harmonycloud.dao.dataprivilege.DataPrivilegeStrategyMapper;
import com.harmonycloud.dao.dataprivilege.bean.DataPrivilegeGroupMapping;
import com.harmonycloud.dao.dataprivilege.bean.DataPrivilegeStrategy;
import com.harmonycloud.dao.dataprivilege.bean.DataPrivilegeStrategyExample;
import com.harmonycloud.dao.user.bean.User;
import com.harmonycloud.service.dataprivilege.DataPrivilegeGroupMappingService;
import com.harmonycloud.service.dataprivilege.DataPrivilegeGroupMemberService;
import com.harmonycloud.service.dataprivilege.DataPrivilegeGroupService;
import com.harmonycloud.service.dataprivilege.DataPrivilegeService;
import com.harmonycloud.service.user.UserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by anson on 18/6/20.
 */
@Service
public class DataPrivilegeServiceImpl implements DataPrivilegeService{
    private static Logger logger = LoggerFactory.getLogger(DataPrivilegeServiceImpl.class);

    final static String PROJECTID = "projectId";
    final static String NAMESPACE = "namespace";
    final static String CLUSTERID = "clusterId";

    @Autowired
    DataPrivilegeStrategyMapper dataPrivilegeStrategyMapper;

    @Autowired
    DataPrivilegeGroupService dataPrivilegeGroupService;

    @Autowired
    DataPrivilegeGroupMappingService dataPrivilegeGroupMappingService;

    @Autowired
    DataPrivilegeGroupMemberService dataPrivilegeGroupMemberService;

    @Autowired
    HttpSession session;

    @Autowired
    UserService userService;

    /**
     * 增加资源数据
     * @param t
     * @param <T>
     * @throws Exception
     */
    public <T> void addResource(T t) throws Exception {
        String data = null;
        String projectId = null;
        String namespace = null;
        String clusterId = null;
        String username = (String)session.getAttribute(CommonConstant.USERNAME);
        User user = userService.getUser(username);
        int strategy = CommonConstant.DATA_OPEN_STRATEGY;
        DataPrivilegeType anotation = t.getClass().getDeclaredAnnotation(DataPrivilegeType.class);
        DataResourceTypeEnum dataPrivilegeType = anotation.type();
        String dataField = anotation.field().toString();
        Field[] fields = t.getClass().getDeclaredFields();
        for(Field field : fields){
            field.setAccessible(true);
            String value = field.get(t) == null ? null:field.get(t).toString();
            if(dataField.equalsIgnoreCase(field.getName())){
                data = value;
            }else if(PROJECTID.equalsIgnoreCase(field.getName())){
                projectId = value;
            }else if(NAMESPACE.equalsIgnoreCase(field.getName())){
                namespace = value;
            }else if(CLUSTERID.equalsIgnoreCase(field.getName())){
                clusterId = value;
            }


        }

        if(StringUtils.isNotBlank(projectId)) {
            DataPrivilegeStrategyExample example = new DataPrivilegeStrategyExample();
            example.createCriteria().andScopeTypeEqualTo((byte) 2).andScopeIdEqualTo(projectId);
            List<DataPrivilegeStrategy> dataPrivilegeStrategyList = dataPrivilegeStrategyMapper.selectByExample(example);

            if(dataPrivilegeStrategyList != null && dataPrivilegeStrategyList.size() == 1){
                DataPrivilegeStrategy dataPrivilegeStrategy = dataPrivilegeStrategyList.get(0);
                strategy = dataPrivilegeStrategy.getStrategy();

            }
        }

        //新建只读组与读写组
        int roGroupId = dataPrivilegeGroupService.addGroup(CommonConstant.DATA_GROUP ,null,null);
        int rwGroupId = dataPrivilegeGroupService.addGroup(CommonConstant.DATA_GROUP ,null,null);

        //新建数据与只读组、读写组的关联
        DataPrivilegeGroupMapping dataPrivilegeGroupMapping = new DataPrivilegeGroupMapping();
        dataPrivilegeGroupMapping.setDataName(data);
        dataPrivilegeGroupMapping.setResourceTypeId(dataPrivilegeType.getCode());
        dataPrivilegeGroupMapping.setProjectId(projectId);
        dataPrivilegeGroupMapping.setClusterId(clusterId);
        dataPrivilegeGroupMapping.setNamespace(namespace);
        dataPrivilegeGroupMapping.setGroupId(roGroupId);
        dataPrivilegeGroupMapping.setPrivilegeType(CommonConstant.DATA_READONLY);
        dataPrivilegeGroupMappingService.addMapping(dataPrivilegeGroupMapping);
        dataPrivilegeGroupMapping.setGroupId(rwGroupId);
        dataPrivilegeGroupMapping.setPrivilegeType(CommonConstant.DATA_READWRITE);
        dataPrivilegeGroupMappingService.addMapping(dataPrivilegeGroupMapping);


        //根据策略增加读写组与只读组的成员
        switch(strategy){
            case CommonConstant.DATA_CLOSED_STRATEGY:
                dataPrivilegeGroupMemberService.initGroupMember(rwGroupId, user.getId(), null);
                break;
            case CommonConstant.DATA_SEMIOPEN_STRATEGY:
                dataPrivilegeGroupMemberService.initGroupMember(rwGroupId, user.getId(), null);
                dataPrivilegeGroupMemberService.initGroupMember(roGroupId, user.getId(), projectId);
                break;
            case CommonConstant.DATA_OPEN_STRATEGY:
                dataPrivilegeGroupMemberService.initGroupMember(rwGroupId, null, projectId);
                break;
        }


    }

    public <T> void deleteResource(T t){

    }


}
