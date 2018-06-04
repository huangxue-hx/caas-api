package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author qg
 *
 */
@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Event extends BaseResource{
	
	private ObjectReference involvedObject;

	private String reason;
	
	private String message;
	
	private EventSource source;
	
	private String firstTimestamp;
	
	private String lastTimestamp;
	
	private Integer count;
	
	private String type;
	
	private String action;
	
	private String eventTime;

	public ObjectReference getInvolvedObject() {
		return involvedObject;
	}

	public void setInvolvedObject(ObjectReference involvedObject) {
		this.involvedObject = involvedObject;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public EventSource getSource() {
		return source;
	}

	public void setSource(EventSource source) {
		this.source = source;
	}

	public String getFirstTimestamp() {
		return firstTimestamp;
	}

	public void setFirstTimestamp(String firstTimestamp) {
		this.firstTimestamp = firstTimestamp;
	}

	public String getLastTimestamp() {
		return lastTimestamp;
	}

	public void setLastTimestamp(String lastTimestamp) {
		this.lastTimestamp = lastTimestamp;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getEventTime() {
		return eventTime;
	}

	public void setEventTime(String eventTime) {
		this.eventTime = eventTime;
	}
	
	
}
