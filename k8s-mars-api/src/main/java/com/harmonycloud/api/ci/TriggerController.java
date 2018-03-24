package com.harmonycloud.api.ci;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.cicd.TriggerDto;
import com.harmonycloud.service.platform.service.ci.TriggerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * @Author w_kyzhang
 * @Description 触发规则接口
 * @Date 2017-12-20
 * @Modified
 */
@Controller
public class TriggerController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    TriggerService triggerService;

    /**
     *
     * 更新触发条件
     *
     * @param triggerDto
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/tenants/{tenantId}/projects/{projectId}/cicdjobs/{jobId}/triggers", method = RequestMethod.PUT)
    @ResponseBody
    public ActionReturnUtil updateTrigger(@RequestBody TriggerDto triggerDto) throws Exception {
        triggerService.updateTrigger(triggerDto);
        return ActionReturnUtil.returnSuccess();
    }

    /**
     *
     * 根据jobId获取触发条件
     *
     * @param jobId 流水线id
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/tenants/{tenantId}/projects/{projectId}/cicdjobs/{jobId}/triggers", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil getTrigger(@PathVariable("jobId") Integer jobId) throws Exception {
        return ActionReturnUtil.returnSuccessWithData(triggerService.getTriggerDto(jobId));
    }

    /**
     *
     * webhook触发
     *
     * @param uuid 流水线uuid
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/cicd/jobs/{uuid}/webhook", method = RequestMethod.POST)
    @ResponseBody
    public ActionReturnUtil triggerByWebhook(@PathVariable("uuid") String uuid) throws Exception {
        logger.info("webhook trigger job", uuid);
        triggerService.triggerJob(uuid);
        return ActionReturnUtil.returnSuccess();
    }

}
