package com.harmonycloud.service.application.impl;

import com.alibaba.fastjson.JSON;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
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

    @Autowired
    private ProjectIpPoolMapper projectIpPoolMapper;
    @Autowired
    private ClusterService clusterService;
    @Autowired
    private TenantClusterQuotaService tenantClusterQuotaService;

    @Value("#{propertiesReader['hc.ipam.url']}")
    private String hcIpamUrl;

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
            throw new MarsRuntimeException(ErrorCodeMessage.PROJECT_IP_POOL_NOT_EXIST);
        }

        List<ProjectIpPoolDto> resList = Lists.newArrayList();
        String url = hcIpamUrl + "/ippool/";    // 接口url

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

            // get调用查询接口
            try {
                JSONObject res = restTemplate.getForObject(url + p.getClusterId() + p.getName(), JSONObject.class);
                if (res == null || res.isEmpty() || res.containsKey("Error")) {
                    throw new MarsRuntimeException(ErrorCodeMessage.QUERY_FAIL);
                }
                // 取出 总数 & 使用数
                poolDto.setIpTotal(res.getInteger("total"));
                poolDto.setIpUsedCount(res.getInteger("used"));
                poolDto.setIpUsedRate(poolDto.getIpTotal() == null || poolDto.getIpTotal() == CommonConstant.NUM_ZERO ?
                        CommonConstant.NUM_ZERO : (poolDto.getIpUsedCount() * CommonConstant.PERCENT_HUNDRED / poolDto.getIpTotal().doubleValue()));
            } catch (RestClientException e) {
                throw new MarsRuntimeException(ErrorCodeMessage.QUERY_FAIL);
            }

            resList.add(poolDto);
        });

        return resList;
    }

    @Override
    public void create(ProjectIpPoolDto poolDto) throws Exception {
        if (poolDto == null || StringUtils.isBlank(poolDto.getProjectId()) || StringUtils.isBlank(poolDto.getClusterId())
                || StringUtils.isBlank(poolDto.getName()) || StringUtils.isBlank(poolDto.getCidr())
                || StringUtils.isBlank(poolDto.getSubnet()) || StringUtils.isBlank(poolDto.getGateway())) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        check(poolDto, true);

        ProjectIpPool rec = new ProjectIpPool();
        rec.setName(poolDto.getName());
        rec.setTenantId(poolDto.getTenantId());
        rec.setProjectId(poolDto.getProjectId());
        rec.setClusterId(poolDto.getClusterId());
        rec.setSubnet(poolDto.getSubnet());
        rec.setCidr(poolDto.getCidr());
        rec.setGateway(IpUtil.ipv4ToInt(poolDto.getGateway()));
        rec.setCreateTime(new Date());

        int count = projectIpPoolMapper.insertSelective(rec);
        if (count != CommonConstant.NUM_ONE) {
            throw new MarsRuntimeException(ErrorCodeMessage.SAVE_FAIL);
        }

        // post调用创建资源池接口
        String url = hcIpamUrl + "/ippool";
        Map<String, Object> headers = Maps.newHashMap();
        headers.put("Content-Type", "application/json; charset=utf-8");
        Map<String, Object> params = Maps.newHashMap();
        params.put("name", poolDto.getClusterId() + poolDto.getName());
        params.put("cidr", poolDto.getCidr());
        params.put("subnet", poolDto.getSubnet());
        params.put("gateway", poolDto.getGateway());

        try {
            HttpClientResponse response = HttpClientUtil.doPost(url, params, headers);
//            System.out.println(response.getStatus() + ":\n" + response.getBody());
            if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
                String body = response.getBody();
                if (StringUtils.isBlank(body) || !StringUtils.startsWith(body, "{") || !StringUtils.endsWith(body, "}")) {
                    throw new MarsRuntimeException(ErrorCodeMessage.CREATE_FAIL);
                }
                JSONObject data = JSON.parseObject(body);
                if (data == null || data.isEmpty() || data.containsKey("Error")) {
                    throw new MarsRuntimeException(ErrorCodeMessage.CREATE_FAIL);
                }
            }
        } catch (IOException e) {
            throw new MarsRuntimeException(ErrorCodeMessage.CREATE_FAIL);
        }
    }

    @Override
    public void update(ProjectIpPoolDto poolDto) throws Exception {
        if (poolDto == null || StringUtils.isBlank(poolDto.getProjectId()) || StringUtils.isBlank(poolDto.getClusterId())
                || StringUtils.isBlank(poolDto.getName())) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        check(poolDto, false);

        // 校验当前资源池是否已使用
        // get调用查询接口
        String url = hcIpamUrl + "/ippool";
        String urlAndPoolName = url + "/" + poolDto.getClusterId() + poolDto.getName();

        try {
            JSONObject res = restTemplate.getForObject(urlAndPoolName, JSONObject.class);
            if (res == null || res.isEmpty() || res.containsKey("Error")) {
                throw new MarsRuntimeException(ErrorCodeMessage.QUERY_FAIL);
            }
            // 校验使用数
            if (res.getIntValue("used") > 0) {
                throw new MarsRuntimeException(ErrorCodeMessage.PROJECT_IP_POOL_CANNOT_MODIFIED);
            }

        } catch (RestClientException e) {
            throw new MarsRuntimeException(ErrorCodeMessage.QUERY_FAIL);
        }

        ProjectIpPool rec = new ProjectIpPool();
        rec.setName(poolDto.getName());
        rec.setClusterId(poolDto.getClusterId());
        rec.setProjectId(poolDto.getProjectId());
        rec.setCidr(poolDto.getCidr());
        rec.setSubnet(poolDto.getSubnet());
        rec.setGateway(IpUtil.ipv4ToInt(poolDto.getGateway()));
        rec.setUpdateTime(new Date());

        int count = projectIpPoolMapper.updateByProjectIdAndClusterIdAndName(rec);
        if (count != CommonConstant.NUM_ONE) {
            throw new MarsRuntimeException(ErrorCodeMessage.UPDATE_FAIL);
        }

        // 先删再增
        try {
            // delete调用删除接口
            HttpClientResponse response = HttpClientUtil.doDelete(urlAndPoolName, null, null);
            if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
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


            // post请求创建接口
            Map<String, Object> headers = Maps.newHashMap();
            headers.put("Content-Type", "application/json; charset=utf-8");
            Map<String, Object> params = Maps.newHashMap();
            params.put("name", poolDto.getClusterId() + poolDto.getName());
            params.put("cidr", poolDto.getCidr());
            params.put("gateway", poolDto.getGateway());
            params.put("subnet", poolDto.getSubnet());

            response = HttpClientUtil.doPost(url, params, headers);
            System.out.println(response.getStatus() + ":\n" + response.getBody());
            if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
                body = response.getBody();
                if (StringUtils.isBlank(body) || !StringUtils.startsWith(body, "{") || !StringUtils.endsWith(body, "}")) {
                    throw new MarsRuntimeException(ErrorCodeMessage.CREATE_FAIL);
                }
                data = JSON.parseObject(body);
                if (data == null || data.isEmpty() || data.containsKey("Error")) {
                    throw new MarsRuntimeException(ErrorCodeMessage.CREATE_FAIL);
                }
            }
        } catch (IOException e) {
            throw new MarsRuntimeException(ErrorCodeMessage.DELETE_FAIL);
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

        // delete调用删除接口
        String url = hcIpamUrl + "/ippool/" + pool.getClusterId() + pool.getName();
        HttpClientResponse response = HttpClientUtil.doDelete(url, null, null);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
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
    }


    // 校验
    private void check(ProjectIpPoolDto poolDto, boolean isCreate) throws MarsRuntimeException {
        // 格式校验
        if (!IpUtil.isCidr(poolDto.getCidr()) || !IpUtil.isCidr(poolDto.getSubnet())) {
            throw new MarsRuntimeException(ErrorCodeMessage.PROJECT_IP_POOL_CIDR_ERROR);
        }
        if (!IpUtil.isIpv4(poolDto.getGateway())) {
            throw new MarsRuntimeException(ErrorCodeMessage.PROJECT_IP_POOL_IP_ERROR);
        }

        // 填写的cidr和子网的范围校验
        long cidrStartIp = IpUtil.ipv4ToLong(IpUtil.getStartIp(poolDto.getCidr()));
        long cidrEndIp = IpUtil.ipv4ToLong(IpUtil.getEndIp(poolDto.getCidr()));
        long subnetStartIp = IpUtil.ipv4ToLong(IpUtil.getStartIp(poolDto.getSubnet()));
        long subnetEndIp = IpUtil.ipv4ToLong(IpUtil.getEndIp(poolDto.getSubnet()));
        if (cidrStartIp < subnetStartIp || cidrEndIp > subnetEndIp) {
            throw new MarsRuntimeException(ErrorCodeMessage.PROJECT_IP_POOL_CIDR_GREATER_THAN_SUBNET_ERROR);
        }

        // 查询已有的ip资源池
        List<ProjectIpPool> existedPoolList = projectIpPoolMapper.selectList(null, null, null);
        if (existedPoolList != null && !existedPoolList.isEmpty()) {    // 不为空才去校验
            int poolNum = 0;
            for (ProjectIpPool pool : existedPoolList) {

                if (isCreate) {    // 新增时
                    if (poolDto.getClusterId().equals(pool.getClusterId()) && poolDto.getName().equals(pool.getName())) {    // 资源库名已存在
                        throw new MarsRuntimeException(ErrorCodeMessage.NAME_EXIST);
                    }
                    if (poolDto.getProjectId().equals(pool.getProjectId())) {
                        if (poolDto.getClusterId().equals(pool.getClusterId())) {    // 一个项目、一个集群、一个ip池
                            throw new MarsRuntimeException(ErrorCodeMessage.PROJECT_IP_POOL_EXIST);
                        }
                        poolNum++;
                    }
                } else {    // 更新时
                    if (poolDto.getProjectId().equals(pool.getProjectId()) && poolDto.getClusterId().equals(pool.getClusterId())) {    // 当前记录
                        if (!poolDto.getName().equals(pool.getName())) {
                            throw new MarsRuntimeException(ErrorCodeMessage.PROJECT_IP_POOL_NAME_CANNOT_MODIFIED);
                        }
                        continue;    // 不做子网校验
                    }
                }

                // 校验子网已被使用
                long existSubnetStartIp = IpUtil.ipv4ToLong(IpUtil.getStartIp(pool.getSubnet()));
                long existSubnetEndIp = IpUtil.ipv4ToLong(IpUtil.getEndIp(pool.getSubnet()));
                if (subnetStartIp <= existSubnetEndIp && subnetEndIp >= existSubnetStartIp) {
                    throw new MarsRuntimeException(ErrorCodeMessage.PROJECT_IP_POOL_SUBNET_EXIST);
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
        return poolList == null ? null : poolList.get(0);
    }

}
