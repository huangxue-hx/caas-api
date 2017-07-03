package com.harmonycloud.service.platform.socketio.message;

import com.harmonycloud.k8s.bean.ObjectReference;

import java.io.Serializable;

/**
 * Created by andy on 17-2-24.
 */
public class Notification implements Serializable{

	private static final long serialVersionUID = 5096292147184180837L;

	private String title;

	private String message;

	private String type;

	private ObjectReference target;

	private String time;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Object getTarget() {
		return target;
	}

	public void setTarget(ObjectReference target) {
		this.target = target;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}
}
