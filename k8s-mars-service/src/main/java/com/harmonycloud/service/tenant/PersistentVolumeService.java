package com.harmonycloud.service.tenant;

import java.util.List;
import java.util.Map;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.dao.network.bean.NamespceBindSubnet;
import com.harmonycloud.dao.network.bean.NetworkCalico;
import com.harmonycloud.dao.network.bean.NetworkTopology;
import com.harmonycloud.dto.tenant.CreateNetwork;
import com.harmonycloud.dto.tenant.PersistentVolumeDto;


/**
 * 
 * 
 * @author  zgl
 *
 */
public interface PersistentVolumeService {
	/**
	 * 查询存储提供者列表
	 * 
	 * @param tenantid
	 * @return
	 */
    public ActionReturnUtil listProvider() throws Exception;
    /**
     * 创建pv
     * @param persistentVolume
     * @return
     * @throws Exception
     */
    public ActionReturnUtil createPv(PersistentVolumeDto persistentVolume) throws Exception;
    /**
     * 根据tenantid查询pv列表
     * @param tenantid
     * @return
     * @throws Exception
     */
    public ActionReturnUtil listPvBytenant(String tenantid) throws Exception;
    /**
     * 根据name查询pv详情
     * @param name
     * @return
     * @throws Exception
     */
    public ActionReturnUtil getPVByName(String name) throws Exception;
    /**
     * 查询所有pv列表
     * @param tenantid
     * @return
     * @throws Exception
     */
    public ActionReturnUtil listAllPv(Cluster cluster) throws Exception;
    /**
     * 根据name删除pv
     * @param name
     * @return
     * @throws Exception
     */
    public ActionReturnUtil deletePvByName(String name) throws Exception;
    /**
     * 根据租户id查询pv是否存在，如果存在则删除
     *
     * @param tenantid
     * @return
     */
    public ActionReturnUtil deletePVBytenantid(String tenantid) throws Exception;
    /**
     * 根据name修改pv
     * @param name
     * @return
     * @throws Exception
     */
    public ActionReturnUtil updatePvByName(String name,String capacity,Boolean readOnly,Boolean multiple) throws Exception;

}
