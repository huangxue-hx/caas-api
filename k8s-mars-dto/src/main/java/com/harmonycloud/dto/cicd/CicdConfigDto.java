package com.harmonycloud.dto.cicd;

/**
 * @Author w_kyzhang
 * @Description
 * @Date 2018-4-17
 * @Modified
 */
public class CicdConfigDto {
    private Integer remainNumber;
    private boolean isTypeMerge;

    public Integer getRemainNumber() {
        return remainNumber;
    }

    public void setRemainNumber(Integer remainNumber) {
        this.remainNumber = remainNumber;
    }

    public boolean isTypeMerge() {
        return isTypeMerge;
    }

    public void setTypeMerge(boolean isTypeMerge) {
        this.isTypeMerge = isTypeMerge;
    }
}
