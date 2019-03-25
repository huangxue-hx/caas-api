package com.harmonycloud.service.dataprivilege.impl;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.DataResourceTypeEnum;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.dao.application.bean.ConfigFile;
import com.harmonycloud.dao.ci.bean.Job;
import com.harmonycloud.dao.dataprivilege.DataPrivilegeGroupMappingMapper;
import com.harmonycloud.dao.dataprivilege.bean.DataPrivilegeGroupMapping;
import com.harmonycloud.dao.dataprivilege.bean.DataPrivilegeGroupMappingExample;
import com.harmonycloud.dao.dataprivilege.bean.DataPrivilegeStrategy;
import com.harmonycloud.dao.dataprivilege.bean.DataPrivilegeStrategyExample;
import com.harmonycloud.dao.tenant.bean.Project;
import com.harmonycloud.dao.user.bean.User;
import com.harmonycloud.dto.dataprivilege.DataPrivilegeDto;
import com.harmonycloud.k8s.bean.BaseResource;
import com.harmonycloud.k8s.bean.Deployment;
import com.harmonycloud.k8s.bean.DeploymentList;
import com.harmonycloud.k8s.bean.K8sResponseBody;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.service.DeploymentService;
import com.harmonycloud.k8s.service.TprApplication;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.service.application.DeploymentsService;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.dataprivilege.DataPrivilegeGroupMappingService;
import com.harmonycloud.service.dataprivilege.DataPrivilegeGroupMemberService;
import com.harmonycloud.service.dataprivilege.DataPrivilegeGroupService;
import com.harmonycloud.service.dataprivilege.DataPrivilegeStrategyService;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.platform.service.ConfigCenterService;
import com.harmonycloud.service.platform.service.ci.JobService;
import com.harmonycloud.service.tenant.NamespaceLocalService;
import com.harmonycloud.service.tenant.ProjectService;
import com.harmonycloud.service.user.UserService;
import com.harmonycloud.service.util.BizUtil;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.crypto.Data;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by anson on 18/6/20.
 */
@Service
public class DataPrivilegeGroupMappingServiceImpl implements DataPrivilegeGroupMappingService {

    @Autowired
    private DataPrivilegeGroupMappingMapper dataPrivilegeGroupMappingMapper;

    @Autowired
    private DataPrivilegeGroupService dataPrivilegeGroupService;

    @Autowired
    private DataPrivilegeGroupMemberService dataPrivilegeGroupMemberService;

    @Autowired
    private DeploymentsService deploymentsService;

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private DeploymentService deploymentService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private DataPrivilegeStrategyService dataPrivilegeStrategyService;

    @Autowired
    private UserService userService;

    @Autowired
    private TprApplication tprApplication;

    @Autowired
    private NamespaceLocalService namespaceLocalService;

    @Autowired
    private JobService jobService;

    @Autowired
    private ConfigCenterService configCenterService;

    /**
     * 初始化旧数据的权限组、数据与用户关联
     * @param dataPrivilegeDto
     * @throws Exception
     */
    @Override
    public void initMapping(DataPrivilegeDto dataPrivilegeDto) throws Exception {
        DataResourceTypeEnum dataResourceTypeEnum = DataResourceTypeEnum.valueOf(dataPrivilegeDto.getDataResourceType());
        switch(dataResourceTypeEnum){
            case SERVICE: {
                //若服务属于应用，则查询应用的数据权限
                Cluster cluster = null;
                if (StringUtils.isNotBlank(dataPrivilegeDto.getNamespace())) {
                    cluster = namespaceLocalService.getClusterByNamespaceName(dataPrivilegeDto.getNamespace());
                }
                K8SClientResponse depRes = deploymentService.doSpecifyDeployment(dataPrivilegeDto.getNamespace(), dataPrivilegeDto.getData(), null, null, HTTPMethod.GET, cluster);
                if(!HttpStatusUtil.isSuccessStatus(depRes.getStatus())){
                    throw new MarsRuntimeException(ErrorCodeMessage.DATA_NOT_FOUND);
                }
                Deployment dep = JsonUtil.jsonToPojo(depRes.getBody(), Deployment.class);
                Map<String, Object> label = dep.getMetadata().getLabels();

                //获取服务创建者
                String username = (String) label.get("nephele/user");
                User user = userService.getUser(username);
                dataPrivilegeDto.setCreatorId(user.getId());

                //查询是否存在应用label
                String labelPre = Constant.TOPO_LABEL_KEY + CommonConstant.LINE
                        +dataPrivilegeDto.getProjectId()
                        + CommonConstant.LINE;
                String app = null;

                for (String key : label.keySet()) {
                    if (key.startsWith(labelPre)) {
                        app = key.replaceFirst(labelPre, "");
                        break;
                    }
                }
                if (StringUtils.isNotBlank(app)) {
                    //若应用无权限数据，则初始化应用资源的数据权限
                    DataPrivilegeDto parentDataPrivilegeDto = (DataPrivilegeDto) BeanUtils.cloneBean(dataPrivilegeDto);
                    parentDataPrivilegeDto.setData(app);
                    parentDataPrivilegeDto.setDataResourceType(DataResourceTypeEnum.APPLICATION.getCode());
                    List<DataPrivilegeGroupMapping> mappingList = listDataPrivilegeGroupMapping(parentDataPrivilegeDto);
                    if (CollectionUtils.isEmpty(mappingList)) {
                        initMapping(parentDataPrivilegeDto);
                    }

                    //设置服务的父资源属性
                    dataPrivilegeDto.setParentData(app);
                    dataPrivilegeDto.setParentDataResourceType(DataResourceTypeEnum.APPLICATION.getCode());
                }
                break;
            }
            case APPLICATION: {
                //获取应用的创建者
                Cluster cluster = null;
                if (StringUtils.isNotBlank(dataPrivilegeDto.getNamespace())) {
                    cluster = namespaceLocalService.getClusterByNamespaceName(dataPrivilegeDto.getNamespace());
                }
                K8SClientResponse appRes = tprApplication.getApplicationByName(dataPrivilegeDto.getNamespace(), dataPrivilegeDto.getData(), null, null, HTTPMethod.GET, cluster);
                if(!HttpStatusUtil.isSuccessStatus(appRes.getStatus())) {
                    throw new MarsRuntimeException(ErrorCodeMessage.DATA_NOT_FOUND);
                }
                BaseResource app = JsonUtil.jsonToPojo(appRes.getBody(), BaseResource.class);
                Map<String, Object> label = app.getMetadata().getLabels();
                String username = (String) label.get(Constant.APP_CREATER_LABEL);
                User user = userService.getUser(username);
                dataPrivilegeDto.setCreatorId(user.getId());
                break;
            }
            case PIPELINE: {
                //获取流水线创建者
                Job job = jobService.getJobById(Integer.valueOf(dataPrivilegeDto.getData()));
                if(job == null) {
                    throw new MarsRuntimeException(ErrorCodeMessage.DATA_NOT_FOUND);
                }
                String username = job.getCreateUser();
                User user = userService.getUser(username);
                dataPrivilegeDto.setCreatorId(user.getId());
                break;
            }
            case CONFIGFILE: {
                //获取配置文件最新版本的创建者
                ActionReturnUtil result = configCenterService.getConfigMapByName(dataPrivilegeDto.getData(), dataPrivilegeDto.getClusterId(), dataPrivilegeDto.getProjectId(), false);
                if(result.isSuccess()){
                    List<ConfigFile> list = (List<ConfigFile>)result.getData();
                    if(CollectionUtils.isEmpty(list)) {
                        throw new MarsRuntimeException(ErrorCodeMessage.DATA_NOT_FOUND);
                    }
                    int lastIndex = list.size() - 1;
                    ConfigFile configFile = list.get(lastIndex);
                    if(StringUtils.isNotEmpty(configFile.getUser())) {
                        User user = userService.getUser(configFile.getUser());
                        if(user != null){
                            dataPrivilegeDto.setCreatorId(user.getId());
                        }
                    }
                }
                break;
            }

        }

        //新建只读组与读写组
        int roGroupId = dataPrivilegeGroupService.addGroup(CommonConstant.DATA_GROUP, null, null);
        int rwGroupId = dataPrivilegeGroupService.addGroup(CommonConstant.DATA_GROUP, null, null);
        //初始化数据与权限组的关联
        Map<Integer, Object> parentGroupMap = initMapping(roGroupId, rwGroupId, dataPrivilegeDto);

        Integer parentRoGroupId = (Integer)parentGroupMap.get(CommonConstant.DATA_READONLY);
        Integer parentRwGroupId = (Integer)parentGroupMap.get(CommonConstant.DATA_READWRITE);

        dataPrivilegeGroupMemberService.initGroupMemberByStrategy(CommonConstant.DATA_OPEN_STRATEGY, dataPrivilegeDto.getProjectId(), roGroupId, rwGroupId, parentRoGroupId, parentRwGroupId);
    }

    /**
     * 初始化数据与权限组的关联
     * @param roGroupId
     * @param rwGroupId
     * @param dataPrivilegeDto
     */
    @Override
    public Map<Integer, Object> initMapping(int roGroupId, int rwGroupId, DataPrivilegeDto dataPrivilegeDto) throws Exception{
        //获取父资源的关联id
        Integer parentRoMappingId = null;
        Integer parentRwMappingId = null;
        Map<Integer, Object> map = new HashMap<>();
        if(StringUtils.isNotBlank(dataPrivilegeDto.getParentData()) && dataPrivilegeDto.getParentDataResourceType() != null){
            List<DataPrivilegeGroupMapping> list = this.getDataPrivilegeGroupMapping(dataPrivilegeDto);
            if(CollectionUtils.isEmpty(list)){
                DataPrivilegeDto parentDataPrivilegeDto = (DataPrivilegeDto) BeanUtils.cloneBean(dataPrivilegeDto);
                parentDataPrivilegeDto.setData(dataPrivilegeDto.getParentData());
                parentDataPrivilegeDto.setDataResourceType(dataPrivilegeDto.getParentDataResourceType());
                parentDataPrivilegeDto.setParentData(null);
                parentDataPrivilegeDto.setParentDataResourceType(null);
                initMapping(parentDataPrivilegeDto);
            }
            list = this.getDataPrivilegeGroupMapping(dataPrivilegeDto);
            for(DataPrivilegeGroupMapping dataPrivilegeGroupMapping : list){
                if(dataPrivilegeGroupMapping.getPrivilegeType() == CommonConstant.DATA_READONLY){
                    parentRoMappingId = dataPrivilegeGroupMapping.getId();
                    map.put(CommonConstant.DATA_READONLY, dataPrivilegeGroupMapping.getGroupId());
                }else if(dataPrivilegeGroupMapping.getPrivilegeType() == CommonConstant.DATA_READWRITE){
                    parentRwMappingId = dataPrivilegeGroupMapping.getId();
                    map.put(CommonConstant.DATA_READWRITE, dataPrivilegeGroupMapping.getGroupId());
                }
            }
        }


        //创建资源与组关联
        DataPrivilegeGroupMapping dataPrivilegeGroupMapping = new DataPrivilegeGroupMapping();
        dataPrivilegeGroupMapping.setDataName(dataPrivilegeDto.getData());
        dataPrivilegeGroupMapping.setResourceTypeId(dataPrivilegeDto.getDataResourceType());
        dataPrivilegeGroupMapping.setProjectId(dataPrivilegeDto.getProjectId());
        dataPrivilegeGroupMapping.setClusterId(dataPrivilegeDto.getClusterId());
        dataPrivilegeGroupMapping.setNamespace(dataPrivilegeDto.getNamespace());
        dataPrivilegeGroupMapping.setCreatorId(dataPrivilegeDto.getCreatorId());

        //只读组
        dataPrivilegeGroupMapping.setGroupId(roGroupId);
        dataPrivilegeGroupMapping.setParentId(parentRoMappingId);
        dataPrivilegeGroupMapping.setPrivilegeType(CommonConstant.DATA_READONLY);
        this.addMapping(dataPrivilegeGroupMapping);

        //读写组
        dataPrivilegeGroupMapping.setGroupId(rwGroupId);
        dataPrivilegeGroupMapping.setParentId(parentRwMappingId);
        dataPrivilegeGroupMapping.setPrivilegeType(CommonConstant.DATA_READWRITE);
        this.addMapping(dataPrivilegeGroupMapping);

        return map;
    }


    /**
     * 获取数据与权限组关联
     * @param dataPrivilegeDto
     * @return
     */
    @Override
    public List<DataPrivilegeGroupMapping> listDataPrivilegeGroupMapping(DataPrivilegeDto dataPrivilegeDto) {
        if(!StringUtils.isBlank(dataPrivilegeDto.getData())){
            DataPrivilegeGroupMappingExample example = new DataPrivilegeGroupMappingExample();
            DataPrivilegeGroupMappingExample.Criteria criteria = example.createCriteria().andDataNameEqualTo(dataPrivilegeDto.getData());
            if(StringUtils.isNotBlank(dataPrivilegeDto.getProjectId())) {
                criteria.andProjectIdEqualTo(dataPrivilegeDto.getProjectId());
            }
            if(StringUtils.isNotBlank(dataPrivilegeDto.getClusterId())) {
                criteria.andClusterIdEqualTo(dataPrivilegeDto.getClusterId());
            }
            if(StringUtils.isNotBlank(dataPrivilegeDto.getNamespace())) {
                criteria.andNamespaceEqualTo(dataPrivilegeDto.getNamespace());
            }
            if(dataPrivilegeDto.getDataResourceType() != null){
                criteria.andResourceTypeIdEqualTo(dataPrivilegeDto.getDataResourceType());
            }
            if(dataPrivilegeDto.getPrivilegeType() != null){
                criteria.andPrivilegeTypeEqualTo(dataPrivilegeDto.getPrivilegeType());
            }
            return this.getDataPrivilegeGroupMapping(example);
        }

         return null;
    }


    /**
     * 新增关联
     * @param dataPrivilegeGroupMapping
     */
    @Override
    public void addMapping(DataPrivilegeGroupMapping dataPrivilegeGroupMapping) {
        dataPrivilegeGroupMappingMapper.insert(dataPrivilegeGroupMapping);
    }

    /**
     * 获取数据与权限组关联
     * @param example
     * @return
     */
    @Override
    public List<DataPrivilegeGroupMapping> getDataPrivilegeGroupMapping(DataPrivilegeGroupMappingExample example) {
        return dataPrivilegeGroupMappingMapper.selectByExample(example);
    }


    @Override
    public List<DataPrivilegeGroupMapping> getDataPrivilegeGroupMapping(DataPrivilegeDto dataPrivilegeDto){
        DataPrivilegeGroupMappingExample example = new DataPrivilegeGroupMappingExample();
        DataPrivilegeGroupMappingExample.Criteria criteria = example.createCriteria();

        if(StringUtils.isNotBlank(dataPrivilegeDto.getParentData())){
            criteria.andDataNameEqualTo(dataPrivilegeDto.getParentData());
        }else if(StringUtils.isNotBlank(dataPrivilegeDto.getData())){
            criteria.andDataNameEqualTo(dataPrivilegeDto.getData());
        }
        if(dataPrivilegeDto.getParentDataResourceType() != null){
            criteria.andResourceTypeIdEqualTo(dataPrivilegeDto.getParentDataResourceType());
        }else if(dataPrivilegeDto.getDataResourceType() != null){
            criteria.andResourceTypeIdEqualTo(dataPrivilegeDto.getDataResourceType());
        }
        if(StringUtils.isNotBlank(dataPrivilegeDto.getProjectId())) {
            criteria.andProjectIdEqualTo(dataPrivilegeDto.getProjectId());
        }
        if(StringUtils.isNotBlank(dataPrivilegeDto.getClusterId())) {
            criteria.andClusterIdEqualTo(dataPrivilegeDto.getClusterId());
        }
        if(StringUtils.isNotBlank(dataPrivilegeDto.getNamespace())) {
            criteria.andNamespaceEqualTo(dataPrivilegeDto.getNamespace());
        }
        return dataPrivilegeGroupMappingMapper.selectByExample(example);
    }

    @Override
    public void deleteMappingById(int id) {
        dataPrivilegeGroupMappingMapper.deleteByPrimaryKey(id);
    }

    @Override
    public List<Integer> getChildDataMappingGroupWithoutUser(int groupId, String username) {
        return dataPrivilegeGroupMappingMapper.getChildDataMappingGroupWithoutUser(groupId, username);
    }
}
