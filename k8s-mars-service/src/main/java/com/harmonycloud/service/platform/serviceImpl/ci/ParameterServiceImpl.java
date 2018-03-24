package com.harmonycloud.service.platform.serviceImpl.ci;

import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.dao.ci.ParameterMapper;
import com.harmonycloud.dao.ci.bean.Parameter;
import com.harmonycloud.dao.ci.bean.ParameterExample;
import com.harmonycloud.dto.cicd.ParameterDto;
import com.harmonycloud.service.platform.service.ci.JobService;
import com.harmonycloud.service.platform.service.ci.ParameterService;
import com.harmonycloud.service.platform.service.ci.StageService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author w_kyzhang
 * @Description 流水线参数定义方法实现
 * @Date 2017-12-25
 * @Modified
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ParameterServiceImpl implements ParameterService{
    @Autowired
    ParameterMapper parameterMapper;

    @Autowired
    StageService stageService;

    @Autowired
    JobService jobService;

    /**
     * 根据流水线id获取参数信息
     *
     * @param jobId
     * @return
     * @throws Exception
     */
    @Override
    public ParameterDto getParameter(Integer jobId) throws Exception{
        ParameterDto parameterDto = new ParameterDto();
        ParameterExample parameterExample = new ParameterExample();
        parameterExample.createCriteria().andJobIdEqualTo(jobId);
        List<Parameter> parameterList = parameterMapper.selectByExample(parameterExample);

        parameterDto.setJobId(jobId);
        List<Map<String, Object>> parameters = new ArrayList<>();
        for(Parameter parameter : parameterList){
            Map<String, Object> parameterMap = new HashMap<>();
            parameterMap.put("name", parameter.getName());
            parameterMap.put("type", parameter.getType());
            parameterMap.put("value", parameter.getValue());
            parameters.add(parameterMap);
        }
        parameterDto.setParameters(parameters);
        return parameterDto;
    }

    /**
     * 更新参数信息
     *
     * @param parameterDto 参数DTO对象
     * @throws Exception
     */
    @Override
    public void updateParameter(ParameterDto parameterDto) throws Exception{
        ParameterExample parameterExample = new ParameterExample();
        parameterExample.createCriteria().andJobIdEqualTo(parameterDto.getJobId());
        parameterMapper.deleteByExample(parameterExample);
        insertParameter(parameterDto);

        //更新jenkins中的job配置
        jobService.updateJenkinsJob(parameterDto.getJobId());
    }

    @Override
    public void insertParameter(ParameterDto parameterDto) throws Exception{
        if(CollectionUtils.isNotEmpty(parameterDto.getParameters())) {
            for(Map<String, Object> parameterMap : parameterDto.getParameters()){
                if(StringUtils.isBlank((String)parameterMap.get("name"))){
                    throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_NAME_NOT_BLANK);
                }
                Parameter parameter = new Parameter();
                parameter.setJobId(parameterDto.getJobId());
                parameter.setName((String)parameterMap.get("name"));
                parameter.setType((Integer)parameterMap.get("type"));
                parameter.setValue((String)parameterMap.get("value"));
                parameterMapper.insert(parameter);
            }
        }
    }
}
