package com.harmonycloud.k8s.bean;

/**
 * 
 * @author jmi
 *
 */
public class ConfigMap extends BaseResource{
	
	private Object data;

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

}
