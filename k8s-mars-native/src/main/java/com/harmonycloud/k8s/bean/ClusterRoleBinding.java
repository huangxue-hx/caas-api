package com.harmonycloud.k8s.bean;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.harmonycloud.k8s.constant.Constant;

/**
 * @author qg
 *
 */
@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClusterRoleBinding extends BaseResource{

	private List<Subjects> subjects;
	
	private ObjectReference roleRef;
	
	public ClusterRoleBinding() {
		this.setApiVersion(Constant.CLUSTERROLEBINDING_VERSION);
		this.setKind("ClusterRoleBinding");
	}

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
