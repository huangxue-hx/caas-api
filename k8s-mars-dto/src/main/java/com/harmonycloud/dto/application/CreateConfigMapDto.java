package com.harmonycloud.dto.application;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateConfigMapDto implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String configMapId;//配置组id
	
	private String path;
	
	private String file;//配置文件名称
	
	private String tag;
	
	private Object value;

	public String getConfigMapId() {
		return configMapId;
	}

	public void setConfigMapId(String configMapId) {
		this.configMapId = configMapId;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

}
