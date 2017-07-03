package com.harmonycloud.service.platform.service.harbor;


import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.service.platform.bean.HarborImageCopy;
import com.harmonycloud.service.platform.bean.HarborReplicationPolicy;
import com.harmonycloud.service.platform.bean.HarborReplicationPolicyEnable;
//import com.harmonycloud.platform.bean.HarborProject;
import com.harmonycloud.service.platform.bean.HarborReplicationTarget;

import java.util.Map;

/**
 * Created by lili on 2017/5/18.
 * harbor常规接口
 */
public interface HarborProjectReplicationService {
	/**
     * 新建跨harbor同步对象
     * @return
     * @throws Exception
     */
    ActionReturnUtil createTarget(HarborReplicationTarget harborReplicationTarget) throws Exception;
    /**
     * 测试账户是否有权限
     * @return
     * @throws Exception
     */
    ActionReturnUtil pingEndpoint(String endpoint,String targetusername,String targetuserpassword)throws Exception;
    /**
     * 删除跨harbor同步对象
     * @return
     * @throws Exception
     */
    ActionReturnUtil deleteTarget(Integer targetID) throws Exception;
    /**
     * 列举跨harbor同步对象
     * @return
     * @throws Exception
     */
    ActionReturnUtil listTargets() throws Exception;
    /**
     * 新建跨harbor同步任务
     * @return
     * @throws Exception
     */
    ActionReturnUtil createPolicy(HarborReplicationPolicy harborReplicationPolicy)throws Exception;
    /**
     * 删除跨harbor同步任务
     * @return
     * @throws Exception
     */
    ActionReturnUtil deletePolicy(Integer policyID)throws Exception;
    /**
     * 列举指定project跨harbor同步任务
     * @return
     * @throws Exception
     */
    ActionReturnUtil listProjectPolicies(Integer projectID)throws Exception;
    /**
     * 列举指定target跨harbor同步任务
     * @return
     * @throws Exception
     */
    ActionReturnUtil listTargetPolicies(Integer projectID)throws Exception;
    /**
     * 列举所有跨harbor同步任务
     * @return
     * @throws Exception
     */
    ActionReturnUtil listPolicies()throws Exception;
    /**
     * 查看指定harbor同步任务的所有子任务
     * @return
     * @throws Exception
     */
    ActionReturnUtil listPolicyJobs(Integer policyID,String page, String pageSize,String end_time, String start_time,String status)throws Exception;
    /**
     * 查看harbor同步任务的具体子任务的日志
     * @return
     * @throws Exception
     */
    ActionReturnUtil listJobLogs(Integer logID)throws Exception;
    /**
     * 更改跨harbor同步任务是否有效
     * @return
     * @throws Exception
     */
    ActionReturnUtil updatePolicyEnable(Integer policyID,HarborReplicationPolicyEnable harborReplicationPolicyEnable)throws Exception;

    /**
     * 复制镜像
     * @return
     * @throws Exception
     */

    ActionReturnUtil copyImage(HarborImageCopy harborImageCopy) throws Exception;
    /**
     * 查看某个同步任务正在进行的job 数目
     * @return
     * @throws Exception
     */
    ActionReturnUtil policyStatus(Integer policyID) throws Exception;

    /**
     * 列举target同步任务Detail
     * @return
     * @throws Exception
     */

    ActionReturnUtil listPoliciesDetail(Integer targetID)throws Exception;
    /**
     * 新建跨harbor同步target,
     * @return
     * @throws Exception
     */
    ActionReturnUtil newTarget(HarborReplicationTarget harborReplicationTarget) throws Exception;

    /**
     * 新建跨harbor同步任务细粒度
     * @return
     * @throws Exception
     */
    ActionReturnUtil createPartialPolicy(Map<String, Object> harborReplicationPolicy)throws Exception;
}