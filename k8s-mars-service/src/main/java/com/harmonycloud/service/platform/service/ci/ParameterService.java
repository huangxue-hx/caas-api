package com.harmonycloud.service.platform.service.ci;

import com.harmonycloud.dto.cicd.ParameterDto;

/**
 * @Author w_kyzhang
 * @Description 流水线参数定义方法接口
 * @Date 2017-12-25
 * @Modified
 */
public interface ParameterService {
    /**
     * 根据流水线id获取参数信息
     *
     * @param jobId
     * @return
     * @throws Exception
     */
    ParameterDto getParameter(Integer jobId) throws Exception;

    /**
     * 更新参数信息
     *
     * @param parameterDto 参数DTO对象
     * @throws Exception
     */
    void updateParameter(ParameterDto parameterDto) throws Exception;

    /**
     * 插入参数
     * @param parameterDto
     * @throws Exception
     */
    void insertParameter(ParameterDto parameterDto) throws Exception;
}
