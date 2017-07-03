package com.harmonycloud.service.platform.service.harbor;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.service.platform.bean.HarborRole;

/**
 * Created by zsl on 2017/1/19.
 * harbor member相关接口
 */
public interface HarborMemberService {

    /**
     * 根据projectId获取project下的成员列表
     *
     * @param projectId projectId
     * @return
     * @throws Exception
     */
    ActionReturnUtil usersOfProject(Integer projectId) throws Exception;

    /**
     * 创建project下的role
     *
     * @param projectId  projectId
     * @param harborRole role bean
     * @return
     * @throws Exception
     */
    ActionReturnUtil createRole(Integer projectId, HarborRole harborRole) throws Exception;

    /**
     * 更新project下的role
     *
     * @param projectId  projectId
     * @param userId     projectId
     * @param harborRole role bean
     * @return
     * @throws Exception
     */
    ActionReturnUtil updateRole(Integer projectId, Integer userId, HarborRole harborRole) throws Exception;

    /**
     * 删除project下的role
     *
     * @param projectId projectId
     * @param userId    userId
     * @return
     * @throws Exception
     */
    ActionReturnUtil deleteRole(Integer projectId, Integer userId) throws Exception;


}
