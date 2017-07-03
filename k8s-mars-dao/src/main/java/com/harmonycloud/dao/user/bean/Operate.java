package com.harmonycloud.dao.user.bean;

/**
 * 资源对应的操作
 * @author yj
 * @date 2017年1月6日
 */
public class Operate {
	
	private Boolean get;
	private Boolean list;
	private Boolean create;
	private Boolean update;
	private Boolean patch;
	private Boolean watch;
	private Boolean proxy;
	private Boolean redirect;
	private Boolean delete;
	private Boolean deletecollection;
	private Boolean read;
	private Boolean wirte;
	
	public Boolean getGet() {
		return get;
	}
	public void setGet(Boolean get) {
		this.get = get;
	}
	public Boolean getList() {
		return list;
	}
	public void setList(Boolean list) {
		this.list = list;
	}
	public Boolean getCreate() {
		return create;
	}
	public void setCreate(Boolean create) {
		this.create = create;
	}
	public Boolean getUpdate() {
		return update;
	}
	public void setUpdate(Boolean update) {
		this.update = update;
	}
	public Boolean getPatch() {
		return patch;
	}
	public void setPatch(Boolean patch) {
		this.patch = patch;
	}
	public Boolean getWatch() {
		return watch;
	}
	public void setWatch(Boolean watch) {
		this.watch = watch;
	}
	public Boolean getProxy() {
		return proxy;
	}
	public void setProxy(Boolean proxy) {
		this.proxy = proxy;
	}
	public Boolean getRedirect() {
		return redirect;
	}
	public void setRedirect(Boolean redirect) {
		this.redirect = redirect;
	}
	public Boolean getDelete() {
		return delete;
	}
	public void setDelete(Boolean delete) {
		this.delete = delete;
	}
	public Boolean getDeletecollection() {
		return deletecollection;
	}
	public void setDeletecollection(Boolean deletecollection) {
		this.deletecollection = deletecollection;
	}
	public Boolean getRead() {
		return read;
	}
	public void setRead(Boolean read) {
		this.read = read;
	}
	public Boolean getWirte() {
		return wirte;
	}
	public void setWirte(Boolean wirte) {
		this.wirte = wirte;
	}
	
	
	
	
}
