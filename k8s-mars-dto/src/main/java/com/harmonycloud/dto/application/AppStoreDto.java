package com.harmonycloud.dto.application;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import net.sf.json.JSONArray;

@JsonInclude(value=JsonInclude.Include.NON_NULL)
public class AppStoreDto {

    private Integer id;

    private String name;

    private String tag;

    private String details;
    
    private String desc;

    private String type;

    private String user;

    private Date createTime;

    private Date updateTime;
    
    private String image;
    
    private String yaml;
    
    private JSONArray servicelist;
    
    private List<TagDto> tags;
    
    private List<ServiceTemplateDto> serviceList;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public List<ServiceTemplateDto> getServiceList() {
		return serviceList;
	}

	public void setServiceList(List<ServiceTemplateDto> serviceList) {
		this.serviceList = serviceList;
	}

	public String getYaml() {
		return yaml;
	}

	public void setYaml(String yaml) {
		this.yaml = yaml;
	}

	public JSONArray getServicelist() {
		return servicelist;
	}

	public void setServicelist(JSONArray servicelist) {
		this.servicelist = servicelist;
	}

	public List<TagDto> getTags() {
		return tags;
	}

	public void setTags(List<TagDto> tags) {
		this.tags = tags;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}
    
    
}
