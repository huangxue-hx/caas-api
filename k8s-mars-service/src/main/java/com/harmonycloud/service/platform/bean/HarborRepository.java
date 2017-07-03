package com.harmonycloud.service.platform.bean;

import java.io.Serializable;
import java.util.List;

/**
 * Created by zsl on 2017/1/20.
 * harbor repository
 */
public class HarborRepository implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String name;    //名称
    private List<HarborRepositoryTags> tags;    //镜像标签列表
    private String source;  //来源

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<HarborRepositoryTags> getTags() {
        return tags;
    }

    public void setTags(List<HarborRepositoryTags> tags) {
        this.tags = tags;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
