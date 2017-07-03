package com.harmonycloud.service.platform.bean;

import java.io.Serializable;

public class CreateConfigMap implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String path;
	
	private String file;
	
	private String tag;
	
	private String value;

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

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

}
