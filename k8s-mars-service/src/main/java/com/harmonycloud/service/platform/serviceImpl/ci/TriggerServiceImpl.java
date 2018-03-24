package com.harmonycloud.service.platform.serviceImpl.ci;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.dao.ci.TriggerMapper;
import com.harmonycloud.dao.ci.bean.Job;
import com.harmonycloud.dao.ci.bean.Trigger;
import com.harmonycloud.dto.cicd.TimeRuleDto;
import com.harmonycloud.dto.cicd.TriggerDto;

import com.harmonycloud.service.platform.service.ci.JobService;
import com.harmonycloud.service.platform.service.ci.StageService;
import com.harmonycloud.service.platform.service.ci.TriggerService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author w_kyzhang
 * @Description 触发规则处理
 * @Date 2017-12-20
 * @Modified
 */
@Service
public class TriggerServiceImpl implements TriggerService{

    @Value("#{propertiesReader['api.url']}")
    private String apiUrl;

    @Autowired
    TriggerMapper triggerMapper;

    @Autowired
    StageService stageService;

    @Autowired
    JobService jobService;

    /**
     * 获取触发规则
     *
     * @param jobId 流水线id
     * @return
     */
    @Override
    public TriggerDto getTriggerDto(Integer jobId) throws Exception {
        TriggerDto triggerDto = new TriggerDto();
        Trigger trigger = this.getTrigger(jobId);
        if(null == trigger){
            triggerDto.setJobId(jobId);
            triggerDto.setValid(false);
        }else{
            BeanUtils.copyProperties(trigger, triggerDto);
            //自定义规则直接返回cron表达式，非自定义规则先将cron表达式转化为时间规则后再返回
            if(CommonConstant.PERIODICAL == trigger.getType() || CommonConstant.POLLSCM == trigger.getType()) {
                if (trigger.getCustomised()) {
                    triggerDto.setCronExp(trigger.getCronExp());
                } else {
                    List<TimeRuleDto> timeRuleList = new ArrayList<>();
                    if (StringUtils.isNotBlank(trigger.getCronExp())) {
                        String[] cronExpArray = trigger.getCronExp().split("\n");
                        for (String cron : cronExpArray) {
                            TimeRuleDto timeRule = new TimeRuleDto();
                            String[] part = cron.split(" ");
                            timeRule.setDayOfMonth(part[2]);
                            timeRule.setDayOfWeek(part[4]);
                            timeRule.setHour(part[1]);
                            timeRule.setMinute(part[0]);
                            timeRuleList.add(timeRule);
                        }
                    }
                    triggerDto.setTimeRules(timeRuleList);
                }
            }
        }
        //返回webhook接口
        Job job = jobService.getJobById(jobId);
        String uuid = job.getUuid();
        triggerDto.setWebhookUrl(apiUrl + CommonConstant.WEBHOOK_API.replace("{uuid}", uuid));
        return triggerDto;
    }

    /**
     * 更新触发规则
     *
     * @param triggerDto
     * @throws Exception
     */
    @Override
    public void updateTrigger(TriggerDto triggerDto) throws Exception {
        Trigger trigger = new Trigger();

        BeanUtils.copyProperties(triggerDto, trigger);
        //定时或pollscm，且非自定义规则情况下，转换时间规则为cron表达式后再存数据库
        if((CommonConstant.PERIODICAL == triggerDto.getType() || CommonConstant.POLLSCM == triggerDto.getType()) && !triggerDto.getCustomised()){
            List cronExpList = new ArrayList<>();
            String cronExp;
            if(CollectionUtils.isNotEmpty(triggerDto.getTimeRules())) {
                for (TimeRuleDto timeRuleDto : triggerDto.getTimeRules()) {
                    cronExp = "";
                    if (StringUtils.isNotBlank(timeRuleDto.getDayOfMonth()) && StringUtils.isNotBlank(timeRuleDto.getDayOfWeek()) && StringUtils.isNotBlank(timeRuleDto.getHour()) && StringUtils.isNotBlank(timeRuleDto.getMinute())) {
                        cronExp = timeRuleDto.getMinute() + " " + timeRuleDto.getHour() + " " + timeRuleDto.getDayOfMonth() + " * " + timeRuleDto.getDayOfWeek();
                    }
                    cronExpList.add(cronExp);
                }
            }
            trigger.setCronExp(String.join("\n", cronExpList));
        }
        if(trigger.getId() == null){
            triggerMapper.insert(trigger);
        }else{
            triggerMapper.updateByPrimaryKey(trigger);
        }
        //更新触发条件至jenkins中
        jobService.updateJenkinsJob(triggerDto.getJobId());
    }


    /**
     * 获取数据库触发规则对象
     *
     * @param jobId 流水线id
     * @return
     */
    @Override
    public Trigger getTrigger(Integer jobId) throws Exception{
        return triggerMapper.selectByJobId(jobId);
    }

    @Override
    public void insertTrigger(Trigger trigger) throws Exception{
        triggerMapper.insert(trigger);
    }

    /**
     *根据uuid触发流水线
     *
     * @param uuid 流水线uuid
     * @throws Exception
     */
    @Override
    public void triggerJob(String uuid) throws Exception {
        Job job = jobService.getJobByUuid(uuid);
        Trigger trigger = this.getTrigger(job.getId());
        //判断是否开启webhook
        if(null != trigger && trigger.getValid() == true && CommonConstant.WEBHOOK == trigger.getType()){
            jobService.build(job.getId(), null);
        }
    }

}
