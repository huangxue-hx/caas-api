package com.harmonycloud.service.platform.bean;

public class HarborProjectDetail {
	private String project_id;
	private String owner_id;
	private String name;
	private String creation_time;
	private String creation_time_str;
	private String deleted;
	private String owner_name;
	private Object isPublic;
	private Object togglable;
	private String update_time;
	private String current_user_role_id;
	private String repo_count;

	public String getProject_id() {
		return project_id;
	}

	public void setProject_id(String project_id) {
		this.project_id = project_id;
	}

	public String getOwner_id() {
		return owner_id;
	}

	public void setOwner_id(String owner_id) {
		this.owner_id = owner_id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCreation_time() {
		return creation_time;
	}

	public void setCreation_time(String creation_time) {
		this.creation_time = creation_time;
	}

	public String getCreation_time_str() {
		return creation_time_str;
	}

	public Object getIsPublic() {
		return isPublic;
	}

	public void setIsPublic(Object isPublic) {
		this.isPublic = isPublic;
	}

	public Object getTogglable() {
		return togglable;
	}

	public void setTogglable(Object togglable) {
		this.togglable = togglable;
	}

	public void setCreation_time_str(String creation_time_str) {
		this.creation_time_str = creation_time_str;
	}

	public String getDeleted() {
		return deleted;
	}

	public void setDeleted(String deleted) {
		this.deleted = deleted;
	}

	public String getOwner_name() {
		return owner_name;
	}

	public void setOwner_name(String owner_name) {
		this.owner_name = owner_name;
	}

	/*
	 * public String getIsPublic() { return isPublic; }
	 * 
	 * public void setIsPublic(String isPublic) { this.isPublic = isPublic; }
	 * 
	 * public boolean isTogglable() { return Togglable; }
	 * 
	 * public void setTogglable(boolean togglable) { Togglable = togglable; }
	 */

	public String getUpdate_time() {
		return update_time;
	}

	public void setUpdate_time(String update_time) {
		this.update_time = update_time;
	}

	public String getCurrent_user_role_id() {
		return current_user_role_id;
	}

	public void setCurrent_user_role_id(String current_user_role_id) {
		this.current_user_role_id = current_user_role_id;
	}

	public String getRepo_count() {
		return repo_count;
	}

	public void setRepo_count(String repo_count) {
		this.repo_count = repo_count;
	}

}
