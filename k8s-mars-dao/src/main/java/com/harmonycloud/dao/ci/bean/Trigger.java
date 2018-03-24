package com.harmonycloud.dao.ci.bean;

import java.io.Serializable;

public class Trigger implements Serializable {
    private Integer id;

    private Integer jobId;

    private Boolean isValid;

    private Integer type;

    private Boolean isCustomised;

    private String cronExp;

    private Integer triggerJobId;

    private static final long serialVersionUID = 1L;

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

    public Integer getTriggerJobId() {
        return triggerJobId;
    }

    public void setTriggerJobId(Integer triggerJobId) {
        this.triggerJobId = triggerJobId;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        Trigger other = (Trigger) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getJobId() == null ? other.getJobId() == null : this.getJobId().equals(other.getJobId()))
            && (this.getValid() == null ? other.getValid() == null : this.getValid().equals(other.getValid()))
            && (this.getType() == null ? other.getType() == null : this.getType().equals(other.getType()))
            && (this.getCustomised() == null ? other.getCustomised() == null : this.getCustomised().equals(other.getCustomised()))
            && (this.getCronExp() == null ? other.getCronExp() == null : this.getCronExp().equals(other.getCronExp()))
            && (this.getTriggerJobId() == null ? other.getTriggerJobId() == null : this.getTriggerJobId().equals(other.getTriggerJobId()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getJobId() == null) ? 0 : getJobId().hashCode());
        result = prime * result + ((getValid() == null) ? 0 : getValid().hashCode());
        result = prime * result + ((getType() == null) ? 0 : getType().hashCode());
        result = prime * result + ((getCustomised() == null) ? 0 : getCustomised().hashCode());
        result = prime * result + ((getCronExp() == null) ? 0 : getCronExp().hashCode());
        result = prime * result + ((getTriggerJobId() == null) ? 0 : getTriggerJobId().hashCode());
        return result;
    }
}