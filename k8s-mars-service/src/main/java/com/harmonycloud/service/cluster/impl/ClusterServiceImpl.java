package com.harmonycloud.service.cluster.impl;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.cluster.ClusterDomainMapper;
import com.harmonycloud.dao.cluster.ClusterMapper;
import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.dao.cluster.bean.ClusterDomain;
import com.harmonycloud.dao.tenant.bean.TenantBinding;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.tenant.TenantService;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by hongjie
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ClusterServiceImpl implements ClusterService {

    @Autowired
    private ClusterMapper clusterMapper;
    @Autowired
    private ClusterDomainMapper clusterDoamin;
    @Autowired
    private TenantService tenantService;

    @Autowired
    private HttpSession session;

    /**
     * 新增集群
     *
     * @param cluster
     * @return
     */
    public ActionReturnUtil addCluster(Cluster cluster) throws Exception {
        try {
            Cluster cluster2 = clusterMapper.findClusterByHost(cluster.getHost());
            if(cluster2 != null) {
                cluster2.setUpdateTime(new Date());
                clusterMapper.updateCluster(cluster2);
            } else {
                if(null == cluster.getName()) {
                    SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd-HH-mm-SS");
                    java.util.Date date=new java.util.Date();
                    String str=sdf.format(date);
                    cluster.setCreateTime(new Date());
                    cluster.setName("Cluster-" + str);
                }

                clusterMapper.addCluster(cluster);
                List<Cluster> list = clusterMapper.listClusters();
                Map<String, Cluster> clusterMap = new HashMap<>();
                for(Cluster c : list) {
                    clusterMap.put(c.getHost(), c);
                }
//                InitialDataUtil.setClusterMap(clusterMap);

            }
            return ActionReturnUtil.returnSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Failed to create cluster.", e);
        }

    }

    /**
     * 获取所有集群
     *
     * @return
     */
    public ActionReturnUtil listClusters()throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            List<Cluster> listClusterss = clusterMapper.listClusters();
            for (Cluster cluster : listClusterss) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", cluster.getId());
                map.put("name", cluster.getName());
                map.put("host", cluster.getHost());
                map.put("protocol", cluster.getProtocol());
                map.put("authType", cluster.getAuthType());
                map.put("username", cluster.getUsername());
                map.put("password", cluster.getPassword());
                map.put("machineToken", cluster.getMachineToken());
                map.put("port", cluster.getPort());
                map.put("entryPoint", cluster.getEntryPoint());
                map.put("haproxyVersion", cluster.getHaproxyVersion());
                map.put("influxdbUrl", cluster.getInfluxdbUrl());
                map.put("influxdbDb", cluster.getInfluxdbUrl());
                map.put("influxdbVersion", cluster.getInfluxdbUrl());
                map.put("esHost", cluster.getInfluxdbUrl());
                map.put("esPort", cluster.getInfluxdbUrl());
                map.put("esClusterName", cluster.getInfluxdbUrl());
                map.put("esVersion", cluster.getInfluxdbUrl());
                map.put("createTime", cluster.getCreateTime());
                list.add(map);
            }

        } catch (Exception e) {
            throw new Exception("Failed to get clusters.", e);
        }
        return ActionReturnUtil.returnSuccessWithData(list);
    }

    @Override
    public ActionReturnUtil clusterListWithTenantOverView() throws Exception {
        ActionReturnUtil listClusters = this.listClusters();
        List<Map<String, Object>> clusterlist =  (List<Map<String, Object>>)listClusters.get(CommonConstant.DATA);
        List<Object> clusterListR = new ArrayList<>();
        int size = 0;
        for (Map<String, Object> map : clusterlist) {
            List<Object> tenantlistR = new ArrayList<>();
            ActionReturnUtil tenantList = tenantService.tenantList(null,Integer.valueOf(map.get("id").toString()));
            List<Map<String, Object>> list = (List<Map<String, Object>>)tenantList.get(CommonConstant.DATA);
            if(list != null && !list.isEmpty()){
                for (Map<String, Object> map2 : list) {
                    Map<String, Object> tenantQuota = tenantService.listTenantQuota(map2.get(CommonConstant.TENANTID).toString());
                    tenantlistR.add(tenantQuota);
                    size++;
                }
            }
            map.put(CommonConstant.TENANTLIST, tenantlistR);
            clusterListR.add(map);
        }
        Map<String, Object> data = new HashMap<String, Object>();
        data.put(CommonConstant.CLUSTERLIST, clusterListR);
        data.put(CommonConstant.TENANTSIZE, size);
        return ActionReturnUtil.returnSuccessWithData(data);
    }

    @Override
    public Cluster findClusterById(String id) {
        return this.clusterMapper.findClusterById(id);
    }

    @Override
    public Cluster findClusterByTenantId(String tenantId) throws Exception{
        if(StringUtils.isBlank(tenantId)){
            return null;
        }
        TenantBinding tenantBinding = tenantService.getTenantByTenantid(tenantId);
        if(tenantBinding == null){
            return null;
        }
        Integer clusterId = tenantBinding.getClusterId();
        return this.findClusterById(clusterId + "");
    }
    /**
     * 根据clusterid查询租户的配额
     * @param clusterId
     * @return
     * @throws Exception
     */
    @Override
    public List<Map> getTenantQuotaByClusterId(String clusterId) throws Exception {
        List<Map> tenantlistR = new ArrayList<>();
        ActionReturnUtil tenantList = tenantService.tenantList(null,Integer.valueOf(clusterId));
        List<Map<String, Object>> list = (List<Map<String, Object>>)tenantList.get(CommonConstant.DATA);
        if(list != null && !list.isEmpty()){
            for (Map<String, Object> map2 : list) {
                Map<String, Object> tenantQuota = tenantService.listTenantQuota(map2.get(CommonConstant.TENANTID).toString());
                tenantlistR.add(tenantQuota);
            }
        }
        return tenantlistR;
    }

    /**
     * 获取所有集群
     *
     * @return
     */
    @Override
    public List<Cluster> listCluster() throws Exception {
        List<Cluster> listClusterss = clusterMapper.listClusters();
        return listClusterss;
    }

//    public Cluster findClusterByHost(String clusterIp) throws Exception {
//        ServletRequestAttributes sa = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
//        Cluster cluster = new Cluster();
//        if(sa != null) {
//            HttpServletRequest request = sa.getRequest();
//            session = request.getSession();
//            String isAdmin = session.getAttribute("isAdmin").toString();
//            if ("1".equals(isAdmin)) {
//                clusterIp = "10.10.102.127";
//                cluster = clusterMapper.findClusterByHost(clusterIp);
//            }else {
//                cluster = (Cluster) session.getAttribute("currentCluster");
//            }
//        }
//
//        return cluster;
//    }
    /**
     * 获取集群domain
     *
     * @return
     */
    @Override
    public ClusterDomain find() throws Exception {
        return  clusterDoamin.find();
    }
    
    /**
     * 修改集群domain
     *
     * @return
     */
    @Override
    public ActionReturnUtil updateDomain(String domain) throws Exception {
    	if(clusterDoamin.find() == null){
        	clusterDoamin.insert(domain);
    		return ActionReturnUtil.returnSuccess();
    	}else{
    		try {
    			clusterDoamin.updateDomain(domain);
    			return ActionReturnUtil.returnSuccess();
			} catch (Exception e) {
				throw new Exception("域名更新失败！", e);
			}
    	}
    }
}