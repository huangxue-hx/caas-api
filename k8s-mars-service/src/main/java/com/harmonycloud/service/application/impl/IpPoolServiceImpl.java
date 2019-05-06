package com.harmonycloud.service.application.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.HttpClientResponse;
import com.harmonycloud.common.util.HttpClientUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.IpUtil;
import com.harmonycloud.dao.application.ProjectIpPoolMapper;
import com.harmonycloud.dao.application.bean.ProjectIpPool;
import com.harmonycloud.dto.tenant.ClusterQuotaDto;
import com.harmonycloud.dto.tenant.ProjectIpPoolDto;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.service.application.IpPoolService;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.tenant.TenantClusterQuotaService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by dengyl on 2019-02-28
 * 项目ip资源池相关
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class IpPoolServiceImpl implements IpPoolService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ProjectIpPoolMapper projectIpPoolMapper;
    @Autowired
    private ClusterService clusterService;
    @Autowired
    private TenantClusterQuotaService tenantClusterQuotaService;

    @Value("${hc.ipam.server.port:32222}")
    private String hcIpamServerPort;

    @Autowired
    private RestTemplate restTemplate;


    @Override
    public List<ProjectIpPoolDto> get(String projectId, String clusterId) throws MarsRuntimeException {
        if (StringUtils.isBlank(projectId)) {
            throw new MarsRuntimeException(ErrorCodeMessage.PROJECTID_NOT_BLANK);
        }

        // 查询列表
        List<ProjectIpPool> poolList = projectIpPoolMapper.selectList(projectId, clusterId, null);
        if (CollectionUtils.isEmpty(poolList)) {
            return Collections.emptyList();
        }

        List<ProjectIpPoolDto> resList = Lists.newArrayList();

        poolList.forEach(p -> {
            ProjectIpPoolDto poolDto = new ProjectIpPoolDto();
            poolDto.setId(p.getId());
            poolDto.setName(p.getName());
            poolDto.setTenantId(p.getTenantId());
            poolDto.setProjectId(p.getProjectId());
            poolDto.setClusterId(p.getClusterId());
            poolDto.setCidr(p.getCidr());
            poolDto.setSubnet(p.getSubnet());
            poolDto.setGateway(IpUtil.intToIpv4(p.getGateway()));
            Cluster cluster = clusterService.findClusterById(p.getClusterId());
            if (cluster != null) {
                poolDto.setClusterName(cluster.getName());
            }
            String getUrl = "http://" + cluster.getHost() + ":" + Integer.parseInt(hcIpamServerPort) + "/ippool/" + getPoolName(p.getClusterId(), p.getName());
            // get调用查询接口
            try {
                JSONObject res = restTemplate.getForObject(getUrl, JSONObject.class);
                if (res == null || res.isEmpty() || res.containsKey("Error")) {
                    throw new MarsRuntimeException(ErrorCodeMessage.QUERY_FAIL);
                }
                // 取出cidr、开始结束ip、ip总数&使用数
                poolDto.setCidr(res.getString("cidr"));
                poolDto.setStartIp(res.getString("start"));
                poolDto.setEndIp(res.getString("end"));
                poolDto.setIpTotal(res.getInteger("total"));
                poolDto.setIpUsedCount(res.getInteger("used"));
                poolDto.setIpUsedRate(poolDto.getIpTotal() == null || poolDto.getIpTotal() == CommonConstant.NUM_ZERO ?
                        CommonConstant.NUM_ZERO : (poolDto.getIpUsedCount() * CommonConstant.PERCENT_HUNDRED / poolDto.getIpTotal().doubleValue()));
            } catch (Exception e) {
                logger.error("调用url:{}，查询ip资源池接口异常：{}", getUrl, e.getMessage());
                throw new MarsRuntimeException(ErrorCodeMessage.QUERY_FAIL);
            }

            resList.add(poolDto);
        });

        return resList;
    }

    @Override
    public void create(ProjectIpPoolDto poolDto) throws Exception {
        check(poolDto);    // 校验参数

        ProjectIpPool rec = new ProjectIpPool();
        rec.setName(poolDto.getName());
        rec.setTenantId(poolDto.getTenantId());
        rec.setProjectId(poolDto.getProjectId());
        rec.setClusterId(poolDto.getClusterId());
        rec.setSubnet(poolDto.getSubnet());
        rec.setCidr(poolDto.getCidr());
        rec.setGateway(IpUtil.ipv4ToInt(IpUtil.getNetMask(poolDto.getSubnet().split(CommonConstant.SLASH)[CommonConstant.NUM_ONE])));
        rec.setCreateTime(new Date());

        int count = projectIpPoolMapper.insertSelective(rec);
        if (count != CommonConstant.NUM_ONE) {
            throw new MarsRuntimeException(ErrorCodeMessage.SAVE_FAIL);
        }
        Cluster cluster = clusterService.findClusterById(poolDto.getClusterId());
        // post调用创建资源池接口
        String url = "http://" + cluster.getHost() + ":" + Integer.parseInt(hcIpamServerPort) + "/ippool";
        try {
            createIpPool(url, poolDto);
        } catch (Exception e) {
            logger.error("调用url:{}，创建ip资源池接口异常：{}", url, e.getMessage());
            throw new MarsRuntimeException(ErrorCodeMessage.CREATE_FAIL);
        }
    }

    @Override
    public void delete(String projectId, String clusterId, String name) throws Exception {
        List<ProjectIpPool> poolList = projectIpPoolMapper.selectList(projectId, clusterId, name);
        if (poolList == null || poolList.size() == CommonConstant.NUM_ZERO) {
            throw new MarsRuntimeException(ErrorCodeMessage.PROJECT_IP_POOL_NOT_EXIST);
        }
        ProjectIpPool pool = poolList.get(CommonConstant.NUM_ZERO);

        int count = projectIpPoolMapper.deleteByPrimaryKey(pool.getId());
        if (count != CommonConstant.NUM_ONE) {
            throw new MarsRuntimeException(ErrorCodeMessage.DELETE_FAIL);
        }
        Cluster cluster = clusterService.findClusterById(clusterId);
        String url = "http://" + cluster.getHost() + ":" + Integer.parseInt(hcIpamServerPort) + "/ippool/" + getPoolName(pool.getClusterId(), pool.getName());
        try {
            // delete调用删除接口
            HttpClientResponse response = HttpClientUtil.doDelete(url, null, null);
            if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
                logger.error("调用url:{}，删除ip资源池接口失败：{}:{}", url, response.getStatus(), response.getBody());
                throw new MarsRuntimeException(ErrorCodeMessage.DELETE_FAIL);
            }
            String body = response.getBody();
            if (StringUtils.isBlank(body) || !StringUtils.startsWith(body, "{") || !StringUtils.endsWith(body, "}")) {
                throw new MarsRuntimeException(ErrorCodeMessage.DELETE_FAIL);
            }
            JSONObject data = JSON.parseObject(body);
            if (data == null || data.isEmpty() || data.containsKey("Error")) {
                throw new MarsRuntimeException(ErrorCodeMessage.DELETE_FAIL);
            }
        } catch (Exception e) {
            logger.error("调用url:{}，操作ip资源池接口异常：{}", url, e.getMessage());
            throw new MarsRuntimeException(ErrorCodeMessage.DELETE_FAIL);
        }
    }

    @Override
    public void checkCluster(String tenantId, String projectId) throws Exception {
        // 查询列表
        List<ProjectIpPool> poolList = projectIpPoolMapper.selectList(projectId, null, null);
        if (CollectionUtils.isEmpty(poolList)) {
            return;
        }

        List<ClusterQuotaDto> clusterDtoList = tenantClusterQuotaService.clusterList(tenantId, null);
        if (CollectionUtils.isEmpty(clusterDtoList)) {
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_ALL_DISABLED);
        }
        int hcIpamCount = (int) clusterDtoList.stream().filter(clusterDto ->
                clusterDto.getNetworkType() != null && clusterDto.getNetworkType().equals(CommonConstant.K8S_NETWORK_HCIPAM)).count();
        if (poolList.size() >= hcIpamCount) {
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_ALL_DISABLED);
        }
    }

    @Override
    public boolean checkCluster(String tenantId, String projectId, String clusterId) {
        // 查询列表
        List<ProjectIpPool> poolList = projectIpPoolMapper.selectList(projectId, clusterId, null);
        return !CollectionUtils.isEmpty(poolList);
    }

    @Override
    public ProjectIpPool info(String projectId, String clusterId) {
        List<ProjectIpPool> poolList = projectIpPoolMapper.selectList(projectId, clusterId, null);
        return CollectionUtils.isEmpty(poolList) ? null : poolList.get(0);
    }


    @Override
    public String getPoolName(String clusterId, String poolName) {
        return clusterId + "-" + poolName;
    }


    // 校验
    private void check(ProjectIpPoolDto poolDto) throws MarsRuntimeException {
        // 为空校验
        if (poolDto == null || StringUtils.isBlank(poolDto.getProjectId()) || StringUtils.isBlank(poolDto.getClusterId())
                || StringUtils.isBlank(poolDto.getName()) || StringUtils.isBlank(poolDto.getSubnet())
                || StringUtils.isBlank(poolDto.getCidr()) && (StringUtils.isBlank(poolDto.getStartIp()) || StringUtils.isBlank(poolDto.getEndIp()))) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        // 格式校验
        if (!IpUtil.isCidr(poolDto.getSubnet())) {
            throw new MarsRuntimeException(ErrorCodeMessage.PROJECT_IP_POOL_CIDR_ERROR);
        }
        if (StringUtils.isNotBlank(poolDto.getCidr())) {    // cidr
            if (!IpUtil.isCidr(poolDto.getCidr())) {
                throw new MarsRuntimeException(ErrorCodeMessage.PROJECT_IP_POOL_CIDR_ERROR);
            }
            poolDto.setStartIp(IpUtil.getStartIp(poolDto.getCidr()));
            poolDto.setEndIp(IpUtil.getEndIp(poolDto.getCidr()));
        } else if (IpUtil.isNotIpv4(poolDto.getStartIp()) || IpUtil.isNotIpv4(poolDto.getEndIp())) {    // 开始结束ip
            throw new MarsRuntimeException(ErrorCodeMessage.PROJECT_IP_POOL_IP_ERROR);
        }

        // 校验集群的网络是否为hcipam模式
        Cluster cluster = clusterService.findClusterById(poolDto.getClusterId());
        if (cluster == null || cluster.getNetworkType() == null || !cluster.getNetworkType().equals(CommonConstant.K8S_NETWORK_HCIPAM)) {
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_MATCH);
        }

        // 填写的cidr(或开始结束ip)和子网的范围校验
        long cidrStartIp = IpUtil.ipv4ToLong(poolDto.getStartIp());
        long cidrEndIp = IpUtil.ipv4ToLong(poolDto.getEndIp());
        long subnetStartIp = IpUtil.ipv4ToLong(IpUtil.getStartIp(poolDto.getSubnet()));
        long subnetEndIp = IpUtil.ipv4ToLong(IpUtil.getEndIp(poolDto.getSubnet()));
        if (cidrStartIp < subnetStartIp || cidrEndIp > subnetEndIp) {
            throw new MarsRuntimeException(ErrorCodeMessage.PROJECT_IP_POOL_CIDR_GREATER_THAN_SUBNET_ERROR);
        }

        // 查询本地已有的ip资源池
        List<ProjectIpPool> existedPoolList = projectIpPoolMapper.selectList(null, null, null);
        if (existedPoolList != null && !existedPoolList.isEmpty()) {    // 不为空才去校验
            int poolNum = 0;
            for (ProjectIpPool pool : existedPoolList) {
                if (poolDto.getClusterId().equals(pool.getClusterId()) && poolDto.getName().equals(pool.getName())) {    // 资源库名已存在
                    throw new MarsRuntimeException(ErrorCodeMessage.NAME_EXIST);
                }
                if (poolDto.getProjectId().equals(pool.getProjectId())) {
                    if (poolDto.getClusterId().equals(pool.getClusterId())) {    // 一个项目、一个集群、一个ip池
                        throw new MarsRuntimeException(ErrorCodeMessage.PROJECT_IP_POOL_EXIST);
                    }
                    poolNum++;
                }
            }

            // 校验所有集群是否均已创建资源池
            if (poolNum > CommonConstant.NUM_ZERO) {
                try {
                    // 查询可用的集群数
                    List<ClusterQuotaDto> clusterDtoList = tenantClusterQuotaService.clusterList(poolDto.getTenantId(), null);
                    int hcIpamCount = (int) clusterDtoList.stream().filter(clusterQuotaDto ->
                            clusterQuotaDto.getNetworkType() != null && clusterQuotaDto.getNetworkType().equals(CommonConstant.K8S_NETWORK_HCIPAM)).count();
                    if (poolNum >= hcIpamCount) {
                        throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_ALL_DISABLED);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // get查询hcipam已有的ip资源池，校验ip池是否已被使用
        String getUrl = "http://" + cluster.getHost() + ":" + Integer.parseInt(hcIpamServerPort) + "/getAllPool";
        // get调用查询接口
        try {
            JSONArray res = restTemplate.getForObject(getUrl, JSONArray.class);
            if (res != null && res.size() > 0) {
                for (int i = 0; i < res.size(); i++) {
                    JSONObject j = res.getJSONObject(i);
                    if (cidrStartIp <= IpUtil.ipv4ToLong(j.getString("end"))
                            && cidrEndIp >= IpUtil.ipv4ToLong(j.getString("start"))) {
                        throw new MarsRuntimeException(ErrorCodeMessage.PROJECT_IP_POOL_SUBNET_EXIST);
                    }
                }
            }
        } catch (MarsRuntimeException e) {
            throw e;
        } catch (Exception e) {    // 此处不抛出异常，hcipam会再校验是否使用
            logger.error("调用url:{}，查询ip资源池接口异常：{}", getUrl, e.getMessage());
        }
    }


    // 调接口新增ip资源池
    private void createIpPool(String url, ProjectIpPoolDto pool) throws Exception {
        // post请求创建接口
        Map<String, Object> headers = Maps.newHashMap();
        headers.put("Content-Type", "application/json; charset=utf-8");
        Map<String, Object> params = Maps.newHashMap();
        params.put("name", getPoolName(pool.getClusterId(), pool.getName()));
        params.put("subnet", pool.getSubnet());
        if (StringUtils.isNotBlank(pool.getCidr())) {    // cidr
            params.put("cidr", pool.getCidr());
        } else {    // 开始结束ip
            params.put("start", pool.getStartIp());
            params.put("end", pool.getEndIp());
        }

        HttpClientResponse response = HttpClientUtil.doPost(url, params, headers);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            logger.error("调用url:{}，创建ip资源池接口失败：{}:{}", url, response.getStatus(), response.getBody());
            throw new MarsRuntimeException(ErrorCodeMessage.CREATE_FAIL);
        }
        String body = response.getBody();
        if (StringUtils.isBlank(body) || !StringUtils.startsWith(body, "{") || !StringUtils.endsWith(body, "}")) {
            throw new MarsRuntimeException(ErrorCodeMessage.CREATE_FAIL);
        }
        JSONObject data = JSON.parseObject(body);
        if (data == null || data.isEmpty() || data.containsKey("Error")) {
            throw new MarsRuntimeException(ErrorCodeMessage.CREATE_FAIL);
        }
    }

}
