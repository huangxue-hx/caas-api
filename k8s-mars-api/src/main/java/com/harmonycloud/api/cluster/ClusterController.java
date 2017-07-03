package com.harmonycloud.api.cluster;

import com.alibaba.fastjson.JSONObject;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.k8s.bean.NodeList;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.tenant.TenantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * dashboard
 * @author jmi
 *
 */

@RestController
public class ClusterController {
	
	@Autowired
	ClusterService clusterService;

	@Autowired
	TenantService tenantService;

	@Autowired
	private com.harmonycloud.k8s.service.NodeService nodeService;

	private Logger logger = LoggerFactory.getLogger(this.getClass());


	/**
	 * 创建集群
	 * @param cluster　集群对象
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/clusters", method = RequestMethod.POST,headers="Accept=application/json")
	public ActionReturnUtil addCluster(@RequestBody Cluster cluster) {

		try {
			logger.info("create cluster");
			if (StringUtils.isEmpty(cluster)) {
				return ActionReturnUtil.returnError();
			}

			return clusterService.addCluster(cluster);

		} catch (Exception e) {
			logger.error("Failed to create cluster.", e.getMessage());
			return ActionReturnUtil.returnError();
		}
	}
	
	@ResponseBody
	@RequestMapping(value = "/clusters", method = RequestMethod.GET)
	public ActionReturnUtil listClusters() throws Exception{
		
		try {
			logger.info("Get all clusters");
			return clusterService.listClusters();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Failed to get all clusters"+e.getMessage());
			return ActionReturnUtil.returnError();
		}
	}

	@RequestMapping(value = "/clusters/list", method = RequestMethod.GET)
	public ResponseEntity<List<Cluster>> listAllClusters() throws Exception{

		try {
			List<Cluster> clusters = clusterService.listCluster();
			return new ResponseEntity(clusters, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Failed to get all clusters",e);
			return new ResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}


	@ResponseBody
    @RequestMapping(value = "/clusters/tenantOverView", method = RequestMethod.GET)
    public ActionReturnUtil clusterListWithTenantOverView() throws Exception{
        
	    return clusterService.clusterListWithTenantOverView();
    }

	@ResponseBody
	@RequestMapping(value = "/clusters/count", method = RequestMethod.GET)
	public ActionReturnUtil clusterCounter() throws Exception{

		try {
			logger.info("Get  cluster count");
			List<Cluster> list = clusterService.listCluster();
			return ActionReturnUtil.returnSuccessWithMap("count", list.size()+"");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Failed to get cluster count"+e.getMessage());
			return ActionReturnUtil.returnError();
		}
	}


	@ResponseBody
	@RequestMapping(value = "/clusters/getClusterBytenantId", method = RequestMethod.GET)
	public ResponseEntity<Cluster>  getClusterBytenantId(@RequestParam(value = "tenantId") String tenantId){
		try {
			Cluster cluster = clusterService.findClusterByTenantId(tenantId);
			logger.info("Get Cluster By tenantId:{},cluster:{}", tenantId, JSONObject.toJSONString(cluster));
			return new ResponseEntity(cluster, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Failed to get Cluster By tenantId", e);
			return new ResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	@RequestMapping(value = "/clusters/getTenantQuotaByClusterId")
    public @ResponseBody ActionReturnUtil getTenantQuotaByClusterId(String clusterId) throws Exception {
	    List<Map> tenantQuotaByClusterId = clusterService.getTenantQuotaByClusterId(clusterId);
	    return ActionReturnUtil.returnSuccessWithData(tenantQuotaByClusterId);
    }
	
	/**
	 * cluster node 列表
	 *
	 * @return
	 */
	@RequestMapping(value = "/clusters/clusterNodeSize")
	@ResponseBody
	public ActionReturnUtil clusterNodeSize() throws Exception {

		try {
			List<Cluster> listCluster = this.clusterService.listCluster();
			int nodeSize = 0;

			if (null != listCluster && listCluster.size() > 0) {
				for (Cluster cluster : listCluster) {
					NodeList nodeList = nodeService.listNode(cluster);
					if (null != nodeList && null != nodeList.getItems()) {
						nodeSize += nodeList.getItems().size();
					}
				}
			}
			return ActionReturnUtil.returnSuccessWithMap("clusterNodeSize", nodeSize + "");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Failed to get cluster node size."+e.getMessage());
			return ActionReturnUtil.returnErrorWithMsg("Failed to get cluster node size.");
		}
	}

	/**
	 * cluster domain
	 *
	 * @return
	 */
    @ResponseBody
    @RequestMapping(value = "/cluster/getClusterDomain", method = RequestMethod.GET)
    public ActionReturnUtil getClusterDomain()throws Exception {
            try {
                    logger.info("获取集群domain");
                    return ActionReturnUtil.returnSuccessWithData(clusterService.find());
            } catch (Exception e) {
                    logger.error("获取集群domain错误"+",e="+e.getMessage());
                    e.printStackTrace();
                    throw e;
            }
    }
    
    /**
	 * update cluster domain
	 *
	 * @return
	 */
    @ResponseBody
    @RequestMapping(value = "/cluster/updateClusterDomain", method = RequestMethod.PUT)
    public ActionReturnUtil updateOutService(@RequestParam(value="domain") final String domain)throws Exception {
            try {
            		logger.info("修改集群domain");
                    return clusterService.updateDomain(domain);
            } catch (Exception e) {
                    logger.error("修改集群domain错误");
                    e.printStackTrace();
                    throw e;
            }
    }
}
