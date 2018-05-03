package com.harmonycloud.dto.cicd;

import java.util.List;

/**
 * @Author w_kyzhang
 * @Description
 * @Date 2017-12-20
 * @Modified
 */
public class TriggerDto {
    private Integer id;

    private Integer jobId;

    private Boolean isValid;

    private Integer type;

    private Boolean isCustomised;

    private String cronExp;

    private List<TimeRuleDto> timeRules;

    private String webhookUrl;

    private Integer triggerJobId;

    private String triggerImage;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getJobId() {
        return jobId;
    }

    public void setJobId(Integer jobId) {
        this.jobId = jobId;
    }

    public Boolean getValid() {
        return isValid;
    }

    public void setValid(Boolean valid) {
        isValid = valid;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Boolean getCustomised() {
        return isCustomised;
    }

    public void setCustomised(Boolean customised) {
        isCustomised = customised;
    }

    public String getCronExp() {
        return cronExp;
    }

    public void setCronExp(String cronExp) {
        this.cronExp = cronExp == null ? null : cronExp.trim();
    }

    public List<TimeRuleDto> getTimeRules() {
        return timeRules;
    }

    public void setTimeRules(List<TimeRuleDto> timeRules) {
        this.timeRules = timeRules;
    }

    public String getWebhookUrl() {
        return webhookUrl;
    }

    public void setWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    public Integer getTriggerJobId() {
        return triggerJobId;
    }

    public void setTriggerJobId(Integer triggerJobId) {
        this.triggerJobId = triggerJobId;
    }

    public String getTriggerImage() {
        return triggerImage;
    }

    public void setTriggerImage(String triggerImage) {
        this.triggerImage = triggerImage;
    }
}
