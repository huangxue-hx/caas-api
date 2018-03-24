package com.harmonycloud.service.platform.bean.harbor;

/**
 * Created by root on 5/20/17.
 */
public class HarborPolicyStatus {
    private Integer unfinishedNum;
    private Integer totalNum;
    private Integer errorNum;

    public Integer getUnfinishedNum() {
        return unfinishedNum;
    }

    public void setUnfinishedNum(Integer unfinishedNum) {
        this.unfinishedNum = unfinishedNum;
    }

    public Integer getTotalNum() {
        return totalNum;
    }

    public void setTotalNum(Integer totalNum) {
        this.totalNum = totalNum;
    }

    public Integer getErrorNum() {
        return errorNum;
    }

    public void setErrorNum(Integer errorNum) {
        this.errorNum = errorNum;
    }
}
