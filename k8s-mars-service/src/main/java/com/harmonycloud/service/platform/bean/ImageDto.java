package com.harmonycloud.service.platform.bean;

import java.util.List;

public class ImageDto {
	private String name;
	private String source;
	private List<ImageTag> tags;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public List<ImageTag> getTags() {
		return tags;
	}

	public void setTags(List<ImageTag> tags) {
		this.tags = tags;
	}

}
