package com.harmonycloud.service.application;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.dto.business.DeploymentDetailDto;
import com.harmonycloud.service.platform.bean.UpdateDeployment;

import java.util.Map;

/**
 * 
 * @author jmi
 *
 */
public interface DeploymentsService {
	
	
	/**
	 * 获取当前namespace的应用（name参数目前没有用）
	 * 可对label进行搜索
	 * @param name
	 * @param namespace
	 * @param labels
	 * @return
	 * @throws Exception
	 */
	public ActionReturnUtil listDeployments(String tenantId, String name, String namespace, String labels, String status) throws Exception;
	
	/**
	 * 启动应用（需要进行消息推送 watch）
	 * @param name
	 * @param namespace
	 * @param userName
	 * @return
	 * @throws Exception
	 */
	public ActionReturnUtil startDeployments(String name, String namespace, String userName, Cluster cluster) throws Exception;
	
	/**
	 * 停止应用（需要进行消息推送 watch）
	 * @param name
	 * @param namespace
	 * @return
	 * @throws Exception
	 */
	public ActionReturnUtil stopDeployments(String name, String namespace, String userName, Cluster cluster) throws Exception;
	
	/**
	 * 获取pod信息（包括事件）
	 * @param name
	 * @param namespace
	 * @return
	 * @throws Exception
	 */
	public ActionReturnUtil getPodDetail(String name, String namespace, Cluster cluster) throws Exception;
	
	/**
	 * 获取pod列表（先获取deployment）
	 * @param name
	 * @param namespace
	 * @return
	 * @throws Exception
	 */
	public ActionReturnUtil podList(String name, String namespace, Cluster cluster) throws Exception;
	
	/**
	 * 获取该namespace下的用户数
	 * @param namespace
	 * @return
	 * @throws Exception
	 */
	public ActionReturnUtil getNamespaceUserNum(String namespace, Cluster cluster) throws Exception;
	
	/**
	 * 获取deployment详情
	 * @param namespace
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public ActionReturnUtil getDeploymentDetail(String namespace, String name, Cluster cluster) throws Exception;
	
	/**
	 * 获取deployment事件
	 * @param namespace
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public ActionReturnUtil getDeploymentEvents(String namespace, String name, Cluster cluster) throws Exception;
	
	/**
	 * 扩展deployment实例
	 * @param namespace
	 * @param name
	 * @param scale
	 * @param userName
	 * @return
	 * @throws Exception
	 */
	public ActionReturnUtil scaleDeployment(String namespace, String name, Integer scale, String userName, Cluster cluster) throws Exception;

	/**
	 * deployment详情页内的容器信息
	 * @param namespace
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public ActionReturnUtil deploymentContainer(String namespace, String name, Cluster cluster) throws Exception;

	/**
	 * 获取某个namespace下的所有容器列表
	 * @param namespace
	 * @return
	 * @throws Exception
     */
	ActionReturnUtil namespaceContainer(String namespace, Cluster cluster) throws Exception;

	/**
	 * 获取某个namespace下的所有容器列表
	 * @param namespace
	 * @return
	 * @throws Exception
	 */
	ActionReturnUtil namespaceContainer(String namespace, Cluster cluster, Map<String, Object> headers) throws Exception;

	/**
	 * 获取pod内的日志
	 * @param namespace
	 * @param name
	 * @param pod
	 * @return
	 * @throws Exception
	 */
	public ActionReturnUtil getPodAppLog(String namespace, String container, String pod, Integer sinceSeconds, String clusterId) throws Exception;

	/**
	 * 创建deployment
	 * @param detail
	 * @param userName
	 * @return
	 * @throws Exception
	 */
	public ActionReturnUtil createDeployment(DeploymentDetailDto detail, String userName, String business, Cluster cluster) throws Exception;

	/**
	 * 删除deployment
	 * @param name
	 * @param namespace
	 * @param userName
	 * @return
	 * @throws Exception
	 */
	public ActionReturnUtil deleteDeployment(String name, String namespace, String userName, Cluster cluster) throws Exception;

	/**
	 * 更新deployment
	 * @param detail
	 * @param userName
	 * @return
	 * @throws Exception
	 */
	public ActionReturnUtil replaceDeployment(UpdateDeployment detail, String userName, Cluster cluster) throws Exception;

	/**
	 * 设置自动伸缩
	 * @param name
	 * @param namespace
	 * @param max
	 * @param min
	 * @param cpu
	 * @return
	 * @throws Exception
	 */
	public ActionReturnUtil autoScaleDeployment(String name, String namespace, Integer max, Integer min, Integer cpu, Cluster cluster) throws Exception;

	/**
	 * 更新自动伸缩的配置
	 * @param name
	 * @param namespace
	 * @param max
	 * @param min
	 * @param cpu
	 * @return
	 * @throws Exception
	 */
	public ActionReturnUtil updateAutoScaleDeployment(String name, String namespace, Integer max, Integer min, Integer cpu, Cluster cluster) throws Exception;

	/**
	 * 删除自动伸缩设置
	 * @param name
	 * @param namespace
	 * @return
	 * @throws Exception
	 */
	public ActionReturnUtil deleteAutoScaleDeployment(String name, String namespace, Cluster cluster) throws Exception;


    /**
     * update deplyment labels annotations service on 17/04/11.
     *
     * @author yanli
     *
     * @param businessList
     *            BusinessListBean application id list
     * @param username
     *            username
     * @return ActionReturnUtil
     */
    ActionReturnUtil updateBusinessDeployment(UpdateDeployment deploymentDetail, String userName, Cluster cluster) throws Exception;

}
