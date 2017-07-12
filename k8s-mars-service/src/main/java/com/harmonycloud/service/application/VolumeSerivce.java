package com.harmonycloud.service.application;


import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.k8s.bean.PersistentVolume;

public interface VolumeSerivce {
	
	public ActionReturnUtil listVolume() throws Exception;
	
	public ActionReturnUtil addVolume(PersistentVolume pv) throws Exception;
	
	public ActionReturnUtil delVolume(String name) throws Exception;
	
	public ActionReturnUtil getPvc(String name, String namespace) throws Exception;

	public ActionReturnUtil listVolume(String namespace , Cluster cluster) throws Exception;

	public ActionReturnUtil createVolume(String namespace, String name, String capacity, String tenantid, String readonly, String bindOne, String PVname) throws Exception;
	
	public ActionReturnUtil createVolume(String namespace, String pvcname, String capacity, String tenantid, String readonly, String bindOne, String PVname, String type, String name) throws Exception;
	
	public ActionReturnUtil deleteVolume(String namespace, String name) throws Exception;
	
	public ActionReturnUtil listVolumeBytenantid(String tenantid) throws Exception;
	
	public ActionReturnUtil listProvider() throws Exception;
	
	public ActionReturnUtil listVolumeprovider() throws Exception;
	
}
