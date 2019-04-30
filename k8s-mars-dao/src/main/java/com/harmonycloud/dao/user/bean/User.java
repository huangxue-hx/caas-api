package com.harmonycloud.dao.user.bean;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by zhangsl on 16/11/4.
 */
public class User implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = -1886649980404325029L;
	/**
	 * 
	 */
	private Long id;
    private String tenantid;
    private String username;
    private String realName;
    private String email;
    private String password;
    private String comment;
    private Date createTime;
    private Date updateTime;
    private Integer isAdmin;
    private Integer isMachine;
    private String token;
    private String pause;
    private Date tokenCreate;
    private Date leftTime;
    private Date rightTime;
    private String phone;

    //devops创建用户
    private boolean isThirdPartyUser;

    //完善功能dingwei
    private String real_name;
    private Date create_time;
    private Date update_time;
    private String groupName;
    private Integer isAuthorize;
    private Boolean isLdapUser;

    private String loginFailTime;//登陆失败时间
    private int loginFailCount;//登陆失败次数

    //用于数据同步标识
    private Integer crowdUserId;

    public String getLoginFailTime() {
        return loginFailTime;
    }

    public void setLoginFailTime(String loginFailTime) {
        this.loginFailTime = loginFailTime;
    }

    public int getLoginFailCount() {
        return loginFailCount;
    }

    public void setLoginFailCount(int loginFailCount) {
        this.loginFailCount = loginFailCount;
    }

    public Integer getIsAuthorize() {
        return isAuthorize;
    }

    public void setIsAuthorize(Integer isAuthorize) {
        this.isAuthorize = isAuthorize;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getReal_name() {
        return real_name;
    }
    public void setReal_name(String real_name) {
        this.real_name = real_name;
    }
    public Date getCreate_time() {
        return create_time;
    }
    public void setCreate_time(Date create_time) {
        this.create_time = create_time;
    }

    public Date getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(Date update_time) {
        this.update_time = update_time;
    }


    //完善结束dingwei


    public String getTenantid() {
		return tenantid;
	}
	public void setTenantid(String tenantid) {
		this.tenantid = tenantid;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Date getLeftTime() {
        return leftTime;
    }
    public void setLeftTime(Date leftTime) {
        this.leftTime = leftTime;
    }
    public Date getRightTime() {
        return rightTime;
    }
    public void setRightTime(Date rightTime) {
        this.rightTime = rightTime;
    }
    public String getPause() {
        return pause;
    }
    public void setPause(String pause) {
        this.pause = pause;
    }
    public User() {
		super();
	}
	public User(String username, String password) {
		super();
		this.username = username;
		this.password = password;
	}
	public Date getTokenCreate() {
		return tokenCreate;
	}
	public void setTokenCreate(Date tokenCreate) {
		this.tokenCreate = tokenCreate;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public Integer getIsMachine() {
		return isMachine;
	}
	public void setIsMachine(Integer isMachine) {
		this.isMachine = isMachine;
	}
	public Integer getIsAdmin() {
		return isAdmin;
	}
	public void setIsAdmin(Integer isAdmin) {
		this.isAdmin = isAdmin;
	}
	
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public Date getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}

    public String getTanantid() {
        return tenantid;
    }

    public void setTanantid(String tenantid) {
        this.tenantid = tenantid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public Boolean getIsLdapUser() {
        return isLdapUser;
    }

    public void setIsLdapUser(Boolean ldapUser) {
        isLdapUser = ldapUser;
    }

    public Boolean getIsThirdPartyUser() {
        return isThirdPartyUser;
    }

    public void setIsThirdPartyUser(Boolean isThirdPartyUser) {
        this.isThirdPartyUser = isThirdPartyUser;
    }

    public Integer getCrowdUserId() {
        return crowdUserId;
    }

    public void setCrowdUserId(Integer crowdUserId) {
        this.crowdUserId = crowdUserId;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", tenantid='" + tenantid + '\'' +
                ", username='" + username + '\'' +
                ", realName='" + realName + '\'' +
                ", email='" + email + '\'' +
                ", comment='" + comment + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", isAdmin=" + isAdmin +
                ", isMachine=" + isMachine +
                ", token='" + token + '\'' +
                ", pause='" + pause + '\'' +
                ", tokenCreate=" + tokenCreate +
                ", leftTime=" + leftTime +
                ", rightTime=" + rightTime +
                ", phone='" + phone + '\'' +
                ", real_name='" + real_name + '\'' +
                ", create_time=" + create_time +
                ", update_time=" + update_time +
                ", groupName='" + groupName + '\'' +
                ", isAuthorize=" + isAuthorize +
                '}';
    }

}
