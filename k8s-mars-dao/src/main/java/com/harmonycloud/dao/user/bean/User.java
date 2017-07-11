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

	public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;

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
}
