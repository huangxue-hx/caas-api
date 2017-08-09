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
    private Long uuid;
    private String username;
    private String realName;
    private String email;
    private String password;
    private String comment;
    private Date createTime;
    private Date updateTime;
    private Integer isAdmin;
    private Integer isadmin;
    private Integer isMachine;
    private Integer ismachine;
    private String token;
    private String pause;
    private Date tokenCreate;
    private Date leftTime;
    private Date rightTime;
    private String phone;

    //完善功能dingwei
    private String real_name;
    private Date create_time;
    private Date update_time;
    private String groupname;
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
    public String getGroupname() {
        return groupname;
    }
    public void setGroupname(String groupname) {
        this.groupname = groupname;
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
	public Long getUuid() {
        return uuid;
    }
    public void setUuid(Long uuid) {
        this.uuid = uuid;
    }
    public Integer getIsadmin() {
        return isadmin;
    }
    public void setIsadmin(Integer isadmin) {
        this.isadmin = isadmin;
    }
    public Integer getIsmachine() {
        return ismachine;
    }
    public void setIsmachine(Integer ismachine) {
        this.ismachine = ismachine;
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
