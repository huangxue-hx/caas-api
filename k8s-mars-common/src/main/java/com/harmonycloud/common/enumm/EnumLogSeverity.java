package com.harmonycloud.common.enumm;

/**
 * 
 * 日志级别枚举
 * 
 * @author jmi
 *
 */
public enum EnumLogSeverity {

	debug("D"), info("I"), warn("W"), error("E");

	private String severity;

	EnumLogSeverity(String severity) {
		this.setSeverity(severity);
	}

	public static String getSeverityName(String severity) {
		severity = severity.toUpperCase();
		switch (severity) {
		case "D":
			return EnumLogSeverity.debug.name();
		case "I":
			return EnumLogSeverity.info.name();
		case "W":
			return EnumLogSeverity.warn.name();
		case "E":
			return EnumLogSeverity.error.name();
		default:
			return severity;
		}
	}

	public String getSeverity() {
		return severity;
	}

	public void setSeverity(String severity) {
		this.severity = severity;
	}
}
