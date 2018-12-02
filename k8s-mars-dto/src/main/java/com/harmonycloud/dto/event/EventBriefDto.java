package com.harmonycloud.dto.event;

public class EventBriefDto {

	private String clusterId;

	private String reason;

	private String message;

	private String firstTimestamp;

	private String lastTimestamp;

	private Integer count;

	private String type;

	private String kind;

	private String namespace;

	private String objectName;

	public EventBriefDto() {

	}

	public EventBriefDto(String clusterId, String reason, String message, String firstTimestamp, String objectName) {
		this.clusterId = clusterId;
		this.reason = reason;
		this.message = message;
		this.firstTimestamp = firstTimestamp;
		this.objectName = objectName;
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

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getObjectName() {
		return objectName;
	}

	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}

	public String getClusterId() {
		return clusterId;
	}

	public void setClusterId(String clusterId) {
		this.clusterId = clusterId;
	}
}
