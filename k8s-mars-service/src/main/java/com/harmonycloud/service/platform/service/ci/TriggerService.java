package com.harmonycloud.service.platform.service.ci;

import com.harmonycloud.dao.ci.bean.Trigger;
import com.harmonycloud.dto.cicd.TriggerDto;

/**
 * @Author w_kyzhang
 * @Description 触发规则接口
 * @Date 2017-12-20
 * @Modified
 */
public interface TriggerService {
    /**
     * 获取触发规则
     *
     * @param jobId 流水线id
     * @return
     */
    TriggerDto getTriggerDto(Integer jobId) throws Exception;

    /**
     * 获取数据库触发规则对象
     *
     * @param jobId 流水线id
     * @return
     */
    Trigger getTrigger(Integer jobId) throws Exception;

    /**
     * 更新触发规则
     *
     * @param triggerDto
     * @throws Exception
     */
    void updateTrigger(TriggerDto triggerDto) throws Exception;

    /**
     * 插入触发规则
     *
     * @param trigger
     * @throws Exception
     */
    void insertTrigger(Trigger trigger) throws Exception;

    /**
     *根据uuid触发流水线
     *
     * @param uuid 流水线uuid
     * @throws Exception
     */
    void triggerJob(String uuid) throws Exception;
}
