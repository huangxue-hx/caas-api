package com.harmonycloud.service.application;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.application.ApplicationDeployDto;
import com.harmonycloud.dto.application.ApplicationDto;
import com.harmonycloud.k8s.bean.BaseResource;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.service.platform.bean.ApplicationList;

import java.util.List;
import java.util.Set;

/**
 * Created by root on 4/10/17.
 */
public interface ApplicationDeployService {

    /**
     * get application by tenant namespace name status service on 17/04/11.
     * 
     * @author gurongyun
     * 
     * @param applicationQuery
     *            查询条件
     * @return ActionReturnUtil
     */
    ActionReturnUtil searchApplication(ApplicationDto applicationQuery) throws Exception;

    /**
     * get application by id service on 17/04/11.
     *
     * @param id
     * @return ActionReturnUtil
     * @throws Exception
     */
    ActionReturnUtil selectApplicationById(String id, String appName, String name) throws Exception;

    /**
     * get application by id service on 17/04/11.
     * 
     * @author gurongyun
     * 
     * @param clusterId
     *            clusterId
     * @return ActionReturnUtil
     */
    ActionReturnUtil searchSumApplication(String clusterId) throws Exception;

    /**
     * deployment application service on 17/04/11.
     * 
     * @author yanli
     * 
     * @param appDeploy
     *            appDeploybean
     * @param username
     *            username
     * @return ActionReturnUtil
     */
    ActionReturnUtil deployApplicationTemplate(ApplicationDeployDto appDeploy, String username) throws Exception;

    /**
     * delete application service on 17/04/11.
     * 
     * @author yanli
     * 
     * @param applicationList
     *            application id list
     * @param username
     *            username
     * @return ActionReturnUtil
     */
    ActionReturnUtil deleteApplicationTemplate(ApplicationList applicationList, String username) throws Exception;

    /**
     * stop application service on 17/04/11.
     * 
     * @author yanli
     * 
     * @param applicationList
     *            appListBean application id list
     * @param username
     *            username
     * @return ActionReturnUtil
     */
    ActionReturnUtil stopApplication(ApplicationList applicationList, String username) throws Exception;

    /**
     * start application service on 17/04/11.
     * 
     * @author yanli
     * 
     * @param applicationList
     *           appListBean application id list
     * @param username
     *            username
     * @return ActionReturnUtil
     */
    ActionReturnUtil startApplication(ApplicationList applicationList, String username) throws Exception;
    
    ActionReturnUtil deleteApplicationByNamespace(String namespace) throws Exception;
    
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
     *            appTemplate name
     * @param tag
     *            appTemplate tag
     * @param namespace
     * 	          namespace
     * @param userName
     *            userName
     * @return ActionReturnUtil
     */
    ActionReturnUtil deployApplicationTemplateByName(String tenantId, String name, String appName, String tag, String namespace, String userName, String pub, String projectId) throws Exception;
    
    /**
     * and and deploy application service 已有的业务  on 17/04/11.
     * 
     * @author yanli
     * 
     * @param appDeploy
     *            appDeploybean
     * @param username
     *            username
     * @return ActionReturnUtil
     */
    ActionReturnUtil addAndDeployApplicationTemplate(ApplicationDeployDto appDeploy, String username)throws Exception;
    
    /**
     * 检查 k8s 资源重复.
     * 
     * @author yanli
     * 
     * @param appDeploy
     *            appDeploybean
     * @param cluster
     *            Cluster
     * @return ActionReturnUtil
     */
    ActionReturnUtil checkK8SName(ApplicationDeployDto appDeploy, Cluster cluster, boolean isNeedCheckAppName)throws Exception;

    ActionReturnUtil unbindApplication(String appName, String tenantId, String name, String namespace, Cluster cluster)throws Exception;

    ActionReturnUtil bindApplication(String appName, String tenantId, String name, String namespace, Cluster cluster)throws Exception;

    ActionReturnUtil getTopo(String id)throws Exception;
    
    /**
     * get application by tenant （应用信息）.
     * 
     * @author gurongyun
     * 
     * @param tenantId
     *            tenant name
     * @return ActionReturnUtil
     */
    ActionReturnUtil listApplication(String tenantId) throws Exception;

    ActionReturnUtil rollBackDeployment(Set<String> names, String namespace, String userName, Cluster cluster)throws Exception;

    /**
     * 获取项目下的所有应用
     * @param projectId
     * @return
     * @throws Exception
     */
    List<BaseResource> listApplicationByProject(String projectId) throws Exception;

    /**
     * 删除应用及其下所有的服务
     * @param projectId
     * @throws Exception
     */
    void deleteProjectAppResource(String projectId) throws Exception;

    /**
     * 获取分区下的所有应用（微服务组件）
     * @param namespace
     * @return
     * @throws Exception
     */
    ActionReturnUtil getApplicationListInNamespace(String namespace) throws Exception;

    /**
     * 检测分区内的内存和cpu
     * @param namespace
     * @param appTemplateName
     * @param projectId
     * @return ActionReturnUtil
     * @throws Exception
     */
    ActionReturnUtil checkAppNamespaceResource(String namespace, String appTemplateName, String projectId) throws Exception;

    ActionReturnUtil updateApplication(String appName, String namespace, String desc) throws Exception;

    /**
     * 回滚有状态服务
     * @param services
     * @param namespace
     * @param userName
     * @param cluster
     * @return
     */
    ActionReturnUtil rollBackStatefulSet(Set<String> services, String namespace, String userName, Cluster cluster) throws Exception;
}
