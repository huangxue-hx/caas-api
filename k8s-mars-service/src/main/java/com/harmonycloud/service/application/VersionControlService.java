package com.harmonycloud.service.application;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.service.platform.bean.CanaryDeployment;

/**
 * Created by czm on 2017/4/26.
 */
public interface VersionControlService {
    /**
     * 对Deployment进行灰度升级
     * @param detail 更新的Deployment参数
     * @param instances 指定的实例个数
     * @param userName 用户名
     * @return 是否开始升级 true表示开始，否则表示失败
     * @throws Exception 异常
     */
    ActionReturnUtil canaryUpdate(CanaryDeployment detail, int instances, String userName) throws Exception;

    /**
     * 暂停灰度升级过程
     * @param namespace Deployment所属命名空间
     * @param name Deployment名字
     * @return 暂停状态
     * @throws Exception
     */
    ActionReturnUtil pauseCanaryUpdate(String namespace, String name) throws Exception;

    /**
     * 这里是有问题的
     * 取消灰度升级
     * @param namespace Deployment 所属命名空间
     * @param name Deployment 名字
     * @return 取消灰度升级是否成功 true表示成功，否则表示失败
     * @throws Exception
     */
    ActionReturnUtil cancelCanaryUpdate(String namespace, String name) throws Exception;

    /**
     * 恢复灰度升级
     * @param namespace Deployment命名空间
     * @param name Deployment 名字
     * @return
     * @throws Exception
     */
    ActionReturnUtil resumeCanaryUpdate(String namespace, String name) throws Exception;

    /**
     * 回去灰度升级状态
     * @param namespace Deployment 所属命名空间
     * @param name Deployment 名字
     * @param serviceType
     * @return 更新了几个POD，原来总共有几个POD
     * @throws Exception
     */
    ActionReturnUtil getUpdateStatus(String namespace, String name, String serviceType) throws Exception;


    /**
     * 查看版本详情
     * @param namespace Deploymment 所属命名空间
     * @param name Deployment 名字
     * @param revision
     * @return 对应版本的模板信息
     * @throws Exception
     */
//    ActionReturnUtil getRevisionDetail(String namespace, String name, String revision) throws Exception;

    /**
     * 查看所有历史版本
     * @param namespace Deployment 所属命名空间
     * @param name  Deployment 名字
     * @return 版本号列表
     * @throws Exception
     */
//    ActionReturnUtil listReversions(String namespace, String name) throws Exception;


    /**
     * 回滚到指定版本
     * @return
     * @throws Exception
     */
    ActionReturnUtil canaryRollback(String namespace, String name, String revision, String podTemplate, String projectId) throws Exception;

    /**
     * 查询出所有版本以及版本信息
     * @param namespace
     * @param name
     * @return
     * @throws Exception
     */
    public ActionReturnUtil listRevisionAndDetails(String namespace, String name) throws Exception;

}
