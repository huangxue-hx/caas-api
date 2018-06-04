package com.harmonycloud.dto.tenant;

public class DevOpsProjectUserDto {
	//用户账号
	private String userAccount;
	//用户姓名
	private String userName;
	//用户邮箱
	private String email;
	//用户电话
	private String tel;
	//用户备注
	private String remark;

	public String getUserAccount() {
		return userAccount;
	}

	public void setUserAccount(String userAccount) {
		this.userAccount = userAccount;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getTel() {
		return tel;
	}

	public void setTel(String tel) {
		this.tel = tel;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}
}
