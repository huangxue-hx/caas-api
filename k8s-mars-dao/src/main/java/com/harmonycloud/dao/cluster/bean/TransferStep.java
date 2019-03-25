package com.harmonycloud.dao.cluster.bean;

import java.io.Serializable;

/**
 * transfer_step
 * @author 
 */
public class TransferStep implements Serializable {
    /**
     * 步骤id
     */
    private Integer stepId;

    /**
     * 步骤名称
     */
    private String stepName;

    /**
     * 百分比
     */
    private String percent;

    private static final long serialVersionUID = 1L;

    public Integer getStepId() {
        return stepId;
    }

    public void setStepId(Integer stepId) {
        this.stepId = stepId;
    }

    public String getStepName() {
        return stepName;
    }

    public void setStepName(String stepName) {
        this.stepName = stepName;
    }

    public String getPercent() {
        return percent;
    }

    public void setPercent(String percent) {
        this.percent = percent;
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
        TransferStep other = (TransferStep) that;
        return (this.getStepId() == null ? other.getStepId() == null : this.getStepId().equals(other.getStepId()))
            && (this.getStepName() == null ? other.getStepName() == null : this.getStepName().equals(other.getStepName()))
            && (this.getPercent() == null ? other.getPercent() == null : this.getPercent().equals(other.getPercent()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getStepId() == null) ? 0 : getStepId().hashCode());
        result = prime * result + ((getStepName() == null) ? 0 : getStepName().hashCode());
        result = prime * result + ((getPercent() == null) ? 0 : getPercent().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", stepId=").append(stepId);
        sb.append(", stepName=").append(stepName);
        sb.append(", percent=").append(percent);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}