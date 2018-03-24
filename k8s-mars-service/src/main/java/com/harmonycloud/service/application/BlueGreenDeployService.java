package com.harmonycloud.service.application;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.service.platform.bean.UpdateDeployment;

/**
 * @Describe 蓝绿发布接口
 * @Author jiangmi
 * @Date created at 2017/12/28
 */
public interface BlueGreenDeployService {

    /**
     * 进行蓝绿发布
     * @param updateDeployment 更新时候的deployment对象
     * @return ActionReturnUtil
     * @throws Exception
     */
    ActionReturnUtil deployByBlueGreen(UpdateDeployment updateDeployment, String userName) throws Exception;

    /**
     * 新旧版本流量切换
     * @param name
     * @param namespace
     * @param isSwitchNew
     * @return ActionReturnUtil
     * @throws Exception
     */
    ActionReturnUtil switchFlow(String name, String namespace, boolean isSwitchNew) throws Exception;

    /**
     * 确认升级新版本
     * @param name
     * @param namespace
     * @return ActionReturnUtil
     * @throws Exception
     */
    ActionReturnUtil confirmToNewVersion(String name, String namespace) throws Exception;

    /**
     * 回到旧版本
     * @param name
     * @param namespace
     * @return ActionReturnUtil
     * @throws Exception
     */
    ActionReturnUtil rollbackToOldVersion(String name, String namespace) throws Exception;

    /**
     * 获取两个版本的容器信息
     * @param name
     * @param namespace
     * @return ActionReturnUtil
     * @throws Exception
     */
    ActionReturnUtil getInfoAboutTwoVersion(String name, String namespace) throws Exception;

}
