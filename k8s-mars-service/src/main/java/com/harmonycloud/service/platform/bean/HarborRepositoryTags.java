package com.harmonycloud.service.platform.bean;

import java.io.Serializable;

/**
 * Created by zsl on 2017/1/20.
 * harbor镜像标签
 */
public class HarborRepositoryTags implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String tag;             //标签名
    private Integer high_num;       //高危数量 镜像安全相关
    private Integer other_num;      //其他数量 镜像安全相关

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Integer getHigh_num() {
        return high_num;
    }

    public void setHigh_num(Integer high_num) {
        this.high_num = high_num;
    }

    public Integer getOther_num() {
        return other_num;
    }

    public void setOther_num(Integer other_num) {
        this.other_num = other_num;
    }
}
