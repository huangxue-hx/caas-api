package com.harmonycloud.k8s.bean.cluster;

import java.io.Serializable;
import java.util.Date;

import static com.harmonycloud.common.Constant.CommonConstant.COLON;
import static com.harmonycloud.common.Constant.CommonConstant.DEFAULT_HARBOR_PORT;
import static com.harmonycloud.common.Constant.CommonConstant.DEFAULT_HTTPS_PORT;

/**
 * Created by zhangkui on 17/12/19.
 */
public class HarborServer implements Serializable {

	private static final long serialVersionUID = 1L;
	private String harborProtocol;
	private String harborHost;
	private Integer harborPort;
	private String harborAdminAccount;
	private String harborAdminPassword;
	/**
	 * admin登录harbor的cookie有效时间
	 */
	private Integer harborLoginTimeOut;
	/**
	 * 哪些集群使用当前harbor（多个集群公用一个）
	 */
	private String referredClusterNames;
	/**
	 * 哪些集群使用当前harbor（多个集群公用一个）
	 */
	private String referredClusterIds;

	private Date createTime;
	private Boolean isNormal;

	/**
	 * 哪些集群使用当前harbor（多个集群公用一个）
	 */
	private String referredClusterAliasNames;

	public HarborServer() {
		super();
	}


	public String getHarborHost() {
		return harborHost;
	}

	public void setHarborHost(String harborHost) {
		this.harborHost = harborHost;
	}

	public String getHarborAdminPassword() {
		return harborAdminPassword;
	}

	public void setHarborAdminPassword(String harborAdminPassword) {
		this.harborAdminPassword = harborAdminPassword;
	}

	public String getHarborAdminAccount() {
		return harborAdminAccount;
	}

	public void setHarborAdminAccount(String harborAdminAccount) {
		this.harborAdminAccount = harborAdminAccount;
	}

	public Integer getHarborPort() {
		return harborPort;
	}

	public void setHarborPort(Integer harborPort) {
		this.harborPort = harborPort;
	}

	public Integer getHarborLoginTimeOut() {
		return harborLoginTimeOut;
	}

	public void setHarborLoginTimeOut(Integer harborLoginTimeOut) {
		this.harborLoginTimeOut = harborLoginTimeOut;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		HarborServer that = (HarborServer) o;

		if (harborHost != null ? !harborHost.equals(that.harborHost) : that.harborHost != null) return false;
		return harborPort != null ? harborPort.equals(that.harborPort) : that.harborPort == null;
	}

	@Override
	public int hashCode() {
		int result = harborHost != null ? harborHost.hashCode() : 0;
		result = 31 * result + (harborPort != null ? harborPort.hashCode() : 0);
		return result;
	}

	public String getHarborProtocol() {
		return harborProtocol;
	}

	public void setHarborProtocol(String harborProtocol) {
		this.harborProtocol = harborProtocol;
	}

	public String getHarborUrl(){
		return harborProtocol + "://" + harborHost +":"+harborPort;
	}

	public String getHarborAddress(){
		if(!DEFAULT_HTTPS_PORT.equals(harborPort) && !DEFAULT_HARBOR_PORT.equals(harborPort)){
			return harborHost + COLON + harborPort;
		}
		return harborHost;
	}

	public String getReferredClusterNames() {
		return referredClusterNames;
	}

	public void setReferredClusterNames(String referredClusterNames) {
		this.referredClusterNames = referredClusterNames;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Boolean getNormal() {
		return isNormal;
	}

	public void setNormal(Boolean normal) {
		isNormal = normal;
	}

	public String getReferredClusterIds() {
		return referredClusterIds;
	}

	public void setReferredClusterIds(String referredClusterIds) {
		this.referredClusterIds = referredClusterIds;
	}

	public String getReferredClusterAliasNames() {
		return referredClusterAliasNames;
	}

	public void setReferredClusterAliasNames(String referredClusterAliasNames) {
		this.referredClusterAliasNames = referredClusterAliasNames;
	}
}
