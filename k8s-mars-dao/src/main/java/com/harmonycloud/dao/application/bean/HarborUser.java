package com.harmonycloud.dao.application.bean;

import java.io.Serializable;

public class HarborUser  implements Serializable{
	
    	private Integer id;

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		private String username;
		
		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		private String password;
}
