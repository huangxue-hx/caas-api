package com.harmonycloud.service.platform.bean;

import com.harmonycloud.k8s.bean.ObjectReference;

public class EventDetail {
	
	private String reason;
	
	private String message;
	
	private String firstTimestamp;
	
	private String lastTimestamp;
	
	private Integer count;
	
	private String type;
	
	private ObjectReference involvedObject;
	
	private Long span;
	
	private String spanMetric;
	
	public EventDetail () {
		
	}
	
	public EventDetail(String reason, String message, String firstTimestamp, String lastTimestamp, Integer count, String type) {
		this.reason = reason;
		this.message = message;
		this.firstTimestamp = firstTimestamp;
		this.lastTimestamp = lastTimestamp;
		this.count = count;
		this.type = type;
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

	public ObjectReference getInvolvedObject() {
		return involvedObject;
	}

	public void setInvolvedObject(ObjectReference involvedObject) {
		this.involvedObject = involvedObject;
	}

	public Long getSpan() {
		return span;
	}

	public void setSpan(Long span) {
		this.span = span;
	}

	public String getSpanMetric() {
		return spanMetric;
	}

	public void setSpanMetric(String spanMetric) {
		this.spanMetric = spanMetric;
	}

}
