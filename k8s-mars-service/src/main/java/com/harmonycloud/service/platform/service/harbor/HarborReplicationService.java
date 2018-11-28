package com.harmonycloud.service.platform.service.harbor;


import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.service.platform.bean.harbor.*;
//import com.harmonycloud.platform.bean.HarborProject;

import java.util.List;
import java.util.Map;

/**
 * Created by lili on 2017/5/18.
 * harbor常规接口
 */
public interface HarborReplicationService {
    /**
     * 新建跨harbor同步对象
     * @return
     * @throws Exception
     */
    ActionReturnUtil createTarget(HarborReplicationTarget harborReplicationTarget) throws Exception;
    /**
     * 修改跨harbor同步对象
     * @return
     * @throws Exception
     */
    ActionReturnUtil updateTarget(HarborReplicationTarget harborReplicationTarget) throws Exception;
    /**
     * 测试账户是否有权限
     * @return
     * @throws Exception
     */
    ActionReturnUtil pingEndpoint(String harborHost,String endpoint,String targetusername,String targetuserpassword)throws Exception;
    /**
     * 删除跨harbor同步对象
     * @return
     * @throws Exception
     */
    ActionReturnUtil deleteTarget(String harborHost,Integer targetID) throws Exception;
    /**
     * 获取跨harbor同步对象
     * @return
     * @throws Exception
     */
    ActionReturnUtil getTarget(String harborHost,Integer targetID) throws Exception;
    /**
     * 列举跨harbor同步对象
     * @return
     * @throws Exception
     */
    ActionReturnUtil listTargets(String harborHost) throws Exception;
    /**
     * 新建跨harbor同步任务
     * @return
     * @throws Exception
     */
    ActionReturnUtil createPolicy(HarborReplicationPolicy harborReplicationPolicy)throws Exception;
    /**
     * 新建跨harbor同步任务
     * @return
     * @throws Exception
     */
    public ActionReturnUtil updatePolicy(HarborReplicationPolicy harborReplicationPolicy,String policyId)throws Exception;
    /**
     * 获取跨harbor同步任务详情
     * @return
     * @throws Exception
     */
    public ActionReturnUtil getPolicyDetail(String harborHost,String policyId)throws Exception;
    /**
     * 删除跨harbor同步任务
     * @return
     * @throws Exception
     */
    ActionReturnUtil deletePolicy(String harborHost,Integer policyID)throws Exception;
    /**
     * 复制某一规则的跨harbor同步任务
     * @return
     * @throws Exception
     */
    ActionReturnUtil startCopyPolicy(String harborHost,String policyID)throws Exception;
    /**
     * 列举指定project跨harbor同步任务
     * @return
     * @throws Exception
     */
    List<HarborPolicyDetail> listProjectPolicies(String harborHost,Integer projectID)throws Exception;
    /**
     * 列举指定target跨harbor同步任务
     * @return
     * @throws Exception
     */
    ActionReturnUtil listTargetPolicies(String harborHost,Integer targetId)throws Exception;
    /**
     * 列举所有跨harbor同步任务
     * @return
     * @throws Exception
     */
    List<HarborPolicyDetail> listPolicies(String harborHost)throws Exception;
    /**
     * 查看指定harbor同步任务的所有子任务
     * @return
     * @throws Exception
     */
    ActionReturnUtil listPolicyJobs(String harborHost,Map<String ,Object> params)throws Exception;
    /**
     * 查看harbor同步任务的具体子任务的日志
     * @return
     * @throws Exception
     */
    ActionReturnUtil listJobLogs(String harborHost,Integer logID)throws Exception;
    /**
     * 更改跨harbor同步任务是否有效
     * @return
     * @throws Exception
     */
    ActionReturnUtil updatePolicyEnable(String harborHost,Integer policyID,Integer enabled)throws Exception;

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
    ActionReturnUtil policyStatus(String harborHost,Integer policyID) throws Exception;

    /**
     * 列举target同步任务Detail
     * @return
     * @throws Exception
     */

    ActionReturnUtil listPoliciesDetail(String harborHost,Integer targetID)throws Exception;

    /**
     * 新建跨harbor同步任务细粒度
     * @return
     * @throws Exception
     */
    ActionReturnUtil createPartialPolicy(ImagePartialSyncInfo imagePartialSyncInfo)throws Exception;

    /**
     *检测规则同名
     * @param harborHost
     * @param name
     * @return
     * @throws Exception
     */
    boolean checkUsingPoliciesName(String harborHost, String name) throws Exception;

}