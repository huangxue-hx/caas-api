package com.harmonycloud.service.application;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.application.DeploymentDetailDto;
import com.harmonycloud.dto.scale.HPADto;
import com.harmonycloud.k8s.bean.DeploymentList;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.service.platform.bean.UpdateContainer;
import com.harmonycloud.service.platform.bean.UpdateDeployment;

import java.util.List;
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
	 * @return ActionReturnUtil
	 * @throws Exception
	 */
	public ActionReturnUtil listDeployments(String tenantId, String name, String namespace, String labels, String projectId, String clusterId) throws Exception;

	/**
	 * 获取某个集群下的某个namespace的服务列表
	 * @param projectId
	 * @param namespace
	 * @throws Exception
	 */
	DeploymentList listDeployments(String namespace, String projectId) throws Exception;
	/**
	 * 启动应用（需要进行消息推送 watch）
	 * @param name
	 * @param namespace
	 * @param userName
	 * @return
	 * @throws Exception
	 */
	public ActionReturnUtil startDeployments(String name, String namespace, String userName) throws Exception;
	
	/**
	 * 停止应用（需要进行消息推送 watch）
	 * @param name
	 * @param namespace
	 * @return
	 * @throws Exception
	 */
	public ActionReturnUtil stopDeployments(String name, String namespace, String userName) throws Exception;
	
	/**
	 * 获取pod信息（包括事件）
	 * @param name
	 * @param namespace
	 * @return
	 * @throws Exception
	 */
	public ActionReturnUtil getPodDetail(String name, String namespace) throws Exception;
	
	/**
	 * 获取pod列表（先获取deployment）
	 * @param name
	 * @param namespace
	 * @return
	 * @throws Exception
	 */
	public ActionReturnUtil podList(String name, String namespace) throws Exception;
	
	/**
	 * 获取deployment详情
	 * @param namespace
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public ActionReturnUtil getDeploymentDetail(String namespace, String name) throws Exception;
	
	/**
	 * 获取deployment事件
	 * @param namespace
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public ActionReturnUtil getDeploymentEvents(String namespace, String name) throws Exception;
	
	/**
	 * 扩展deployment实例
	 * @param namespace
	 * @param name
	 * @param scale
	 * @param userName
	 * @return
	 * @throws Exception
	 */
	public ActionReturnUtil scaleDeployment(String namespace, String name, Integer scale, String userName) throws Exception;

	/**
	 * deployment详情页内的容器信息
	 * @param namespace
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public ActionReturnUtil deploymentContainer(String namespace, String name) throws Exception;

	/**
	 * deployment详情页内的容器信息
	 * @param namespace
	 * @param name
	 * @param cluster
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
	ActionReturnUtil namespaceContainer(String namespace) throws Exception;

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
	 * @param container
	 * @param pod
	 * @param sinceSeconds
	 * @param clusterId
	 * @return ActionReturnUtil
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
	public ActionReturnUtil createDeployment(DeploymentDetailDto detail, String userName, String app, Cluster cluster) throws Exception;

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

	public ActionReturnUtil getAutoScaleDeployment(String name, String namespace) throws Exception;

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
	public ActionReturnUtil autoScaleDeployment(HPADto hpaDto) throws Exception;

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
	public ActionReturnUtil updateAutoScaleDeployment(HPADto hpaDto) throws Exception;

	/**
	 * 删除自动伸缩设置
	 * @param name
	 * @param namespace
	 * @return
	 * @throws Exception
	 */
	public ActionReturnUtil deleteAutoScaleDeployment(String name, String namespace) throws Exception;


	/**
	 * update deplyment labels annotations service on 17/04/11.
	 *
	 * @param deploymentDetail 服务详情
	 * @param userName 用户名
	 * @return ActionReturnUtil
	 * @throws Exception 更新失败
	 */
    ActionReturnUtil updateAppDeployment(UpdateDeployment deploymentDetail, String userName) throws Exception;

	/**
	 * 判断服务是否重名
	 * @param name
	 * @param namespace
	 * @return ActionReturnUtil
	 * @throws Exception
	 */
    ActionReturnUtil checkDeploymentName(String name, String namespace, boolean isTpl) throws Exception;

	/**
	 * 创建配置文件（）
	 * @param namespace
	 * @param depName
	 * @param containers
	 * @param cluster
	 * @return Map
	 * @throws Exception
	 */
	Map<String, String> createConfigMapInUpdate(String namespace, String depName, Cluster cluster, List<UpdateContainer> containers) throws Exception;

	/**
	 * 更新Deployment的labels
	 * @author bilongchen@harmonycloud.cn
	 * @date 2018.6.14
	 * @param namespace
	 * @param deploymentName
	 * @param cluster
	 * @param label
	 * @return ActionReturnUtil
	 * @throws Exception
	 */
	public ActionReturnUtil updateLabels(String  namespace, String deploymentName, Cluster cluster, Map<String, Object> label) throws Exception;
}
