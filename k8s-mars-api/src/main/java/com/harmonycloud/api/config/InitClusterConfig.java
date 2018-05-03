package com.harmonycloud.api.config;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.cluster.ClusterCRDDto;
import com.harmonycloud.k8s.bean.cluster.ClusterNetwork;
import com.harmonycloud.k8s.bean.cluster.ClusterTemplate;
import com.harmonycloud.service.cluster.impl.ClusterCRDServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.stream.Collectors;

public class InitClusterConfig {

    static Logger LOGGER = LoggerFactory.getLogger(InitClusterConfig.class);
    static ClusterCRDServiceImpl impl = new ClusterCRDServiceImpl();

    private static ClusterCRDDto topCluster;

    private static ClusterNetwork clusterNetwork;

    private static Map<String, ClusterTemplate> clusterTemplate;

    public static ClusterCRDDto getTopCluster() throws Exception {
        if (topCluster == null ){
            initTopCluster();
        }
        return topCluster;
    }

    public static Map<String, ClusterTemplate> getTemplateMap() throws Exception {
        if (clusterTemplate == null) {
            initTopCluster();
        }
        return clusterTemplate;
    }

    public static ClusterNetwork getNetworkConfig() throws Exception {
        if (clusterNetwork == null) {
            initTopCluster();
        }
        return clusterNetwork;
    }


    public static void initTopCluster()  throws Exception {
        try {
            ActionReturnUtil cluster = impl.getCluster("cluster-top","top");
            if (!cluster.isSuccess() || cluster.get("data") == null) {
                LOGGER.error("获取上层集群错误");
            }
            ClusterCRDDto clusterTPRDto = (ClusterCRDDto) cluster.get("data");
            topCluster =  clusterTPRDto;
            Map<String, ClusterTemplate>  templateMap = topCluster.getTemplate().stream().collect(Collectors.toMap(ClusterTemplate::getType, condition -> condition));
            clusterTemplate = templateMap;
            clusterNetwork = topCluster.getNetwork();
        } catch (Exception e) {
            LOGGER.error("获取集群信息错误",e);
        }
    }

}
