package com.harmonycloud.service.platform.serviceImpl.ci;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.ci.BuildEnvironmentMapper;
import com.harmonycloud.dao.ci.bean.BuildEnvironment;
import com.harmonycloud.dao.ci.bean.BuildEnvironmentExample;
import com.harmonycloud.dao.ci.bean.Stage;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.service.platform.service.ci.BuildEnvironmentService;
import com.harmonycloud.service.platform.service.ci.JobService;
import com.harmonycloud.service.platform.service.ci.StageService;
import com.harmonycloud.service.tenant.ProjectService;
import com.harmonycloud.service.user.RoleLocalService;
import com.harmonycloud.service.user.UserService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * @Author w_kyzhang
 * @Description 自定义依赖方法实现
 * @Date created in 2017-12-18
 * @Modified
 */
@Service
public class BuildEnvironmentServiceImpl implements BuildEnvironmentService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private BuildEnvironmentMapper buildEnvironmentMapper;

    @Autowired
    private StageService stageService;

    @Autowired
    private JobService jobService;

    @Autowired
    private RoleLocalService roleLocalService;

    @Autowired
    private HttpSession session;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserService userService;

    /**
     * 查询环境列表
     *
     * @param name 查询关键字
     * @return
     * @throws Exception
     */
    @Override
    public List<BuildEnvironment> listBuildEnvironment(String projectId, String clusterId, String name) throws Exception {
        if(projectService.getProjectByProjectId(projectId) == null){
            return Collections.emptyList();
        }

        BuildEnvironmentExample buildEnvironmentExample = new BuildEnvironmentExample();
        BuildEnvironmentExample.Criteria criteria =buildEnvironmentExample.createCriteria().andProjectIdEqualTo(projectId);
        if(StringUtils.isNotBlank(clusterId)){
            criteria.andClusterIdEqualTo(clusterId);
        }else{
            List<Cluster> clusterList = roleLocalService.listCurrentUserRoleCluster();
            if(CollectionUtils.isNotEmpty(clusterList)){
                List clusterIdList = new ArrayList();
                for(Cluster cluster : clusterList){
                    clusterIdList.add(cluster.getId());
                }
                criteria.andClusterIdIn(clusterIdList);
            }
        }
        if (StringUtils.isNotBlank(name)) {
            criteria.andNameLike("%" + name + "%");
        }
        BuildEnvironmentExample.Criteria publicCriteria = buildEnvironmentExample.createCriteria().andIsPublicEqualTo(CommonConstant.FLAG_TRUE);
        if (StringUtils.isNotBlank(name)) {
            publicCriteria.andNameLike("%" + name + "%");
        }
        buildEnvironmentExample.or(publicCriteria);
        return buildEnvironmentMapper.selectByExample(buildEnvironmentExample);

    }

    /**
     * 根据id获取环境信息
     *
     * @param id 环境id
     * @return
     * @throws Exception
     */
    @Override
    public BuildEnvironment getBuildEnvironment(Integer id) throws Exception {
        return buildEnvironmentMapper.selectByPrimaryKey(id);
    }

    /**
     * 新增环境
     *
     * @param buildEnvironment 环境对象
     * @return
     * @throws Exception
     */
    @Override
    public void addBuildEnvironment(BuildEnvironment buildEnvironment) throws Exception {
        BuildEnvironmentExample buildEnvironmentExample = new BuildEnvironmentExample();
        if (StringUtils.isBlank(buildEnvironment.getName())) {
            throw new MarsRuntimeException(ErrorCodeMessage.ENVIRONMENT_NAME_NOT_BLANK);
        }
        if(buildEnvironment.isPublic()){
            buildEnvironment.setProjectId(null);
            buildEnvironment.setClusterId(null);
            buildEnvironmentExample.createCriteria().andNameEqualTo(buildEnvironment.getName()).andIsPublicEqualTo(CommonConstant.FLAG_TRUE);
        }else{
            if (null == buildEnvironment.getClusterId()) {
                throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
            }
            if (null == buildEnvironment.getProjectId()) {
                throw new MarsRuntimeException(ErrorCodeMessage.PROJECT_NOT_EXIST);
            }
            buildEnvironmentExample.createCriteria().andNameEqualTo(buildEnvironment.getName()).andClusterIdEqualTo(buildEnvironment.getClusterId()).andProjectIdEqualTo(buildEnvironment.getProjectId());
        }
        long count = buildEnvironmentMapper.countByExample(buildEnvironmentExample);
        if (count > 0) {
            throw new MarsRuntimeException(ErrorCodeMessage.ENVIRONMENT_NAME_DUPLICATE);
        }
        buildEnvironmentMapper.insert(buildEnvironment);
    }

    /**
     * 更新环境信息
     *
     * @param buildEnvironment 环境对象
     * @return
     * @throws Exception
     */
    @Override
    public void updateBuildEnvironment(BuildEnvironment buildEnvironment) throws Exception {
        if (StringUtils.isBlank(buildEnvironment.getName())) {
            throw new MarsRuntimeException(ErrorCodeMessage.ENVIRONMENT_NAME_NOT_BLANK);
        }
        BuildEnvironment sourceBuildEnvironment = buildEnvironmentMapper.selectByPrimaryKey(buildEnvironment.getId());
        if (null == sourceBuildEnvironment) {
            throw new MarsRuntimeException(ErrorCodeMessage.ENVIRONMENT_UPDATE_FAIL);
        }
        buildEnvironmentMapper.updateByPrimaryKeySelective(buildEnvironment);
        //查询使用该环境的流水线步骤，并更新jenkins配置
        if (!buildEnvironment.getImage().equals(sourceBuildEnvironment.getImage())) {
            Stage stageExample = new Stage();
            stageExample.setBuildEnvironmentId(buildEnvironment.getId());
            List<Stage> stageList = stageService.selectByExample(stageExample);
            if (CollectionUtils.isNotEmpty(stageList)) {
                Set<Integer> jobIdSet = new HashSet();
                for (Stage stage : stageList) {
                    jobIdSet.add(stage.getJobId());
                }

                for (int jobId : jobIdSet) {
                    jobService.updateJenkinsJob(jobId);
                }
            }
        }
    }

    /**
     * 删除环境
     *
     * @param id 环境id
     * @return
     * @throws Exception
     */
    @Override
    public void deleteBuildEnvironment(Integer id) throws Exception {
        //查询环境是否被流水线步骤使用
        Stage stage = new Stage();
        stage.setBuildEnvironmentId(id);
        long count = stageService.countByExample(stage);
        if (count > 0) {
            throw new MarsRuntimeException(ErrorCodeMessage.ENVIRONMENT_USED);
        }
        BuildEnvironment buildEnvironment = buildEnvironmentMapper.selectByPrimaryKey(id);
        if(buildEnvironment.isPublic()){
            String createUser = buildEnvironment.getCreateUser();
            String username = (String)session.getAttribute(CommonConstant.USERNAME);
            if(StringUtils.isNotBlank(createUser) && !createUser.equals(username)){
                if(!userService.checkCurrentUserIsAdmin()) {
                    throw new MarsRuntimeException(ErrorCodeMessage.ENVIRONMENT_NO_PRIVILEGE_DELETE);
                }
            }
        }
        buildEnvironmentMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int deleteByClusterId(String clusterId){
        return buildEnvironmentMapper.deleteByClusterId(clusterId);
    }

    @Override
    public void deleteBuildEnvironmentByProject(String projectId) {
        buildEnvironmentMapper.deleteByProjectId(projectId);
    }
}