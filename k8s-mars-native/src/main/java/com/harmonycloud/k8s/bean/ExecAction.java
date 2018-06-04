package com.harmonycloud.k8s.bean;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExecAction {
	
	private List<String> command;

	public List<String> getCommand() {
		return command;
	}

	public void setCommand(List<String> command) {
		this.command = command;
	}

}
