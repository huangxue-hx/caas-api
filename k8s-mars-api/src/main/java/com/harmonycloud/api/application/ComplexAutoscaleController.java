package com.harmonycloud.api.application;

import com.alibaba.fastjson.JSONObject;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.MessageUtil;
import com.harmonycloud.dto.scale.AutoScaleDto;
import com.harmonycloud.dto.scale.CustomMetricScaleDto;
import com.harmonycloud.service.application.AutoScaleService;
import com.harmonycloud.service.application.DeploymentsService;
import com.harmonycloud.service.application.EsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Created by root on 5/22/17.
 */
@RequestMapping("/tenants/{tenantId}/projects/{projectId}/deploys/{deployName}/autoscale")
@Controller
public class ComplexAutoscaleController {

    @Autowired
    DeploymentsService dpService;
    @Autowired
    AutoScaleService autoScaleService;

    @Autowired
    EsService esService;
    @Autowired
    HttpSession session;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 设置自动伸缩
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(method = RequestMethod.POST)
    public ActionReturnUtil createAutoScale(@RequestBody @Validated AutoScaleDto autoScaleDto) {
        if(checkParams(autoScaleDto) != null){
            return ActionReturnUtil.returnErrorWithData(checkParams(autoScaleDto));
        }
        try {
            return  autoScaleService.create(autoScaleDto);

        } catch (MarsRuntimeException mre){
            logger.error("创建自动伸缩失败，autoScaleDto:{}", JSONObject.toJSONString(autoScaleDto), mre);
            return ActionReturnUtil.returnErrorWithData(mre.getMessage());
        }catch (Exception e) {
            logger.error("创建自动伸缩失败，autoScaleDto:{}", JSONObject.toJSONString(autoScaleDto), e);
            return ActionReturnUtil.returnError();
        }
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.PUT)
    public ActionReturnUtil updateAutoScaleApp(@RequestBody @Validated AutoScaleDto autoScaleDto) {
        if(checkParams(autoScaleDto) != null){
            return ActionReturnUtil.returnErrorWithData(checkParams(autoScaleDto));
        }
        try {
            return autoScaleService.update(autoScaleDto);

        }catch (MarsRuntimeException mre){
            logger.error("修改自动伸缩失败，autoScaleDto:{}", JSONObject.toJSONString(autoScaleDto), mre);
            return ActionReturnUtil.returnErrorWithData(mre.getMessage());
        } catch (Exception e) {
            logger.error("修改自动伸缩失败， autoScaleDto:{}", JSONObject.toJSONString(autoScaleDto), e);
            return ActionReturnUtil.returnError();
        }
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.DELETE)
    public ActionReturnUtil deleteAutoScale(@PathVariable(value = "deployName") String deploymentName,
                                            @RequestParam(value = "namespace") String namespace) throws Exception {
        try {
            logger.info("删除应用自动伸缩，deploymentName:{},namespace:{}",deploymentName,namespace);
            boolean result = autoScaleService.delete(namespace,deploymentName);
            if(result){
                return ActionReturnUtil.returnSuccess();
            }else{
                return ActionReturnUtil.returnError();
            }
        }catch (Exception e) {
            logger.error("删除自动伸缩失败， deploymentName:{}", deploymentName, e);
            return ActionReturnUtil.returnError();
        }
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET)
    public ActionReturnUtil queryAutoScale(@PathVariable(value = "deployName") String deploymentName,
                                            @RequestParam(value = "namespace") String namespace) throws Exception {
        try {
            AutoScaleDto autoScaleDto = autoScaleService.get(namespace, deploymentName);
            return ActionReturnUtil.returnSuccessWithData(autoScaleDto);
        }catch (Exception e) {
            logger.error("查询自动伸缩失败， deploymentName:{}", deploymentName, e);
            return ActionReturnUtil.returnError();
        }
    }

    private String checkParams( AutoScaleDto autoScale){
        if(autoScale == null){
            return MessageUtil.getMessage(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        if(autoScale.getTargetCpuUsage() == null
                && autoScale.getTargetMemoryUsage() == null
                && autoScale.getTargetTps() == null
                && CollectionUtils.isEmpty(autoScale.getTimeMetricScales())
                && CollectionUtils.isEmpty(autoScale.getCustomMetricScales())){

            return MessageUtil.getMessage(ErrorCodeMessage.INDICATOR);
        }
        List<CustomMetricScaleDto> customMetrics = autoScale.getCustomMetricScales();
        if(!CollectionUtils.isEmpty(customMetrics) && customMetrics.size()>1){
            Set<String> names = customMetrics.stream()
                    .map(CustomMetricScaleDto::getMetricName).collect(Collectors.toSet());
            if(names.size() != customMetrics.size()){
                return MessageUtil.getMessage(ErrorCodeMessage.NOT_REPEATE);
            }
        }
        return null;
    }

}
