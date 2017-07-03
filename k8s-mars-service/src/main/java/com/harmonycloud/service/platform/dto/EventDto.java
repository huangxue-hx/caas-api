package com.harmonycloud.service.platform.dto;

import com.harmonycloud.k8s.bean.Event;

/**
 * Created by andy on 17-2-27.
 */
public class EventDto {

	private String type;

	private Event object;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Event getObject() {
		return object;
	}

	public void setObject(Event object) {
		this.object = object;
	}
}
