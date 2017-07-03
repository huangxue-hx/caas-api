package com.harmonycloud.k8s.bean;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author qg
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClusterRoleBinding extends BaseResource{

	private List<Subjects> subjects;
	
	private ObjectReference roleRef;

	public List<Subjects> getSubjects() {
		return subjects;
	}

	public void setSubjects(List<Subjects> subjects) {
		this.subjects = subjects;
	}

	public ObjectReference getRoleRef() {
		return roleRef;
	}

	public void setRoleRef(ObjectReference roleRef) {
		this.roleRef = roleRef;
	}
	
}
