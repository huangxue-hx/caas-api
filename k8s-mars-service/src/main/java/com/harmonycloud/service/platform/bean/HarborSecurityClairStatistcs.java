package com.harmonycloud.service.platform.bean;

import java.io.Serializable;

/**
 * Created by zsl on 2017/1/22.
 * 镜像安全扫描bean
 */
public class HarborSecurityClairStatistcs implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer image_num;      //该用户或者该项目下的镜像数
    private Integer unsecurity_image_num;   //该用户或者该项目下存在高危漏洞的镜像数
    private Integer clair_not_Support;  //clair不支持的镜像数
    private Integer clair_success;
    private Integer abnormal;
    private Integer mild;

    public Integer getMild() {
        return mild;
    }

    public void setMild(Integer mild) {
        this.mild = mild;
    }

    public Integer getImage_num() {
        return image_num;
    }

    public void setImage_num(Integer image_num) {
        this.image_num = image_num;
    }

    public Integer getUnsecurity_image_num() {
        return unsecurity_image_num;
    }

    public void setUnsecurity_image_num(Integer unsecurity_image_num) {
        this.unsecurity_image_num = unsecurity_image_num;
    }

    public Integer getClair_not_Support() {
        return clair_not_Support;
    }

    public void setClair_not_Support(Integer clair_not_Support) {
        this.clair_not_Support = clair_not_Support;
    }

    public Integer getClair_success() {
        return clair_success;
    }

    public void setClair_success(Integer clair_success) {
        this.clair_success = clair_success;
    }

    public Integer getAbnormal() {
        return abnormal;
    }

    public void setAbnormal(Integer abnormal) {
        this.abnormal = abnormal;
    }
}
