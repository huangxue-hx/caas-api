package com.harmonycloud.api.ci;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.cicd.ParameterDto;
import com.harmonycloud.service.platform.service.ci.ParameterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * @Author w_kyzhang
 * @Description 流水线参数定义方法实现
 * @Date 2017-12-25
 * @Modified
 */
@Controller
@RequestMapping("/tenants/{tenantId}/projects/{projectId}/cicdjobs/{jobId}/parameters")
public class ParameterController {
    @Autowired
    private ParameterService parameterService;

    /**
     * 根据流水线id获取参数信息
     *
     * @param jobId 流水线id
     * @return
     * @throws Exception
     */
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil getParameter(@PathVariable("jobId") Integer jobId) throws Exception {
        return ActionReturnUtil.returnSuccessWithData(parameterService.getParameter(jobId));
    }

    /**
     * 更新参数信息
     *
     * @param parameterDto 参数DTO对象
     * @return
     * @throws Exception
     */
    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public ActionReturnUtil updateParameter(@RequestBody ParameterDto parameterDto) throws Exception {
        parameterService.updateParameter(parameterDto);
        return ActionReturnUtil.returnSuccess();
    }


}
