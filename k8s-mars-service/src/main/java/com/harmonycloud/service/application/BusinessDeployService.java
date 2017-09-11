package com.harmonycloud.service.application;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.dto.business.BusinessDeployDto;
import com.harmonycloud.service.platform.bean.BusinessList;

/**
 * Created by root on 4/10/17.
 */
public interface BusinessDeployService {

    /**
     * get application by tenant namespace name status service on 17/04/11.
     * 
     * @author gurongyun
     * 
     * @param tenant
     *            tenant name
     * @param namespace
     *            namespace
     * @param name
     *            application name
     * @param status
     *            application running status 0:abnormal;1:normal
     * @return ActionReturnUtil
     */
    ActionReturnUtil searchBusiness(String tenantId, String tenant, String namespace, String name, String status) throws Exception;

    /**
     * get application by id service on 17/04/11.
     * 
     * @author gurongyun
     * 
     * @param id
     *            application id
     * @return ActionReturnUtil
     */
    ActionReturnUtil selectBusinessById(String id, Cluster cluster) throws Exception;

    /**
     * get application by id service on 17/04/11.
     * 
     * @author gurongyun
     * 
     * @param tenant
     *            tenant name
     * @return ActionReturnUtil
     */
    ActionReturnUtil searchSumBusiness(String [] tenant, String clusterId) throws Exception;

    /**
     * get PV service on 17/04/11.
     * 
     * @author gurongyun
     * 
     * @param tenantId
     *            tenant id
     * @param namespace
     *            namespace
     * @param status
     *            pv usage 0:all;1:used;2:unused
     * @return ActionReturnUtil
     */
    ActionReturnUtil selectPv(String tenantId, String namespace, int status) throws Exception;

    /**
     * deployment application service on 17/04/11.
     * 
     * @author yanli
     * 
     * @param businessDeploy
     *            BusinessDeploybean
     * @param username
     *            username
     * @return ActionReturnUtil
     */
    ActionReturnUtil deployBusinessTemplate(BusinessDeployDto businessDeploy, String username, Cluster cluster, String tenantId) throws Exception;

    /**
     * delete application service on 17/04/11.
     * 
     * @author yanli
     * 
     * @param businessList
     *            application id list
     * @param username
     *            username
     * @return ActionReturnUtil
     */
    ActionReturnUtil deleteBusinessTemplate(BusinessList businessList, String username) throws Exception;

    /**
     * stop application service on 17/04/11.
     * 
     * @author yanli
     * 
     * @param businessList
     *            BusinessListBean application id list
     * @param username
     *            username
     * @return ActionReturnUtil
     */
    ActionReturnUtil stopBusinessTemplate(BusinessList businessList, String username, Cluster cluster) throws Exception;

    /**
     * start application service on 17/04/11.
     * 
     * @author yanli
     * 
     * @param businessList
     *            BusinessListBean application id list
     * @param username
     *            username
     * @return ActionReturnUtil
     */
    ActionReturnUtil startBusinessTemplate(BusinessList businessList, String username, Cluster cluster) throws Exception;
    
    ActionReturnUtil deleteBusinessByNamespace(String namespace) throws Exception;
    
    /**
     * get application by id service on 17/04/11.
     * 
     * @author gurongyun
     * 
     * @param tenant
     *            tenant name
     * @return ActionReturnUtil
     */
    ActionReturnUtil searchSum(String [] tenant) throws Exception;

    /**
     * deployment application service on 17/04/11.
     * 
     * @author yanli
     * 
     * @param name
     *            BusinessTemplate name
     * @param tag
     *            BusinessTemplate tag
     * @param namespace
     * 	          namespace
     * @param username
     *            username
     * @param cluster
     *            cluster
     * @return ActionReturnUtil
     */
    ActionReturnUtil deployBusinessTemplateByName(String tenantId, String name, String businessame, String tag, String namespace, String userName, Cluster cluster, String pub, String nodeselector) throws Exception;
    
    /**
     * and and deploy application service 已有的业务  on 17/04/11.
     * 
     * @author yanli
     * 
     * @param businessDeploy
     *            BusinessDeploybean
     * @param username
     *            username
     * @return ActionReturnUtil
     */
    ActionReturnUtil addAndDeployBusinessTemplate(BusinessDeployDto businessDeploy, String username, String tenantid, Cluster cluster)throws Exception;
    
    /**
     * 检查 k8s 资源重复.
     * 
     * @author yanli
     * 
     * @param businessDeploy
     *            BusinessDeploybean
     * @param username
     *            username
     * @return ActionReturnUtil
     */
    ActionReturnUtil checkK8SName(BusinessDeployDto businessDeploy, Cluster cluster)throws Exception;

    ActionReturnUtil unbindBusiness(String businessname, String tenantId, String name, String namespace, Cluster cluster)throws Exception;

    ActionReturnUtil bindBusiness(String businessname, String tenantId, String name, String namespace, Cluster cluster)throws Exception;

}
