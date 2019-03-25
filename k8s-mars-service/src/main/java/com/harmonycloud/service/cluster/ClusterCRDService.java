package com.harmonycloud.service.cluster;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.cluster.AddClusterDto;
import com.harmonycloud.dto.cluster.ClusterCRDDto;
import com.harmonycloud.k8s.bean.cluster.Cluster;


public interface ClusterCRDService {

    public ActionReturnUtil listClusters(String template, String dataCenter) throws Exception ;

    public ActionReturnUtil getCluster(String dataCenter, String name) throws Exception ;

    public ActionReturnUtil deleteCluster(String dataCenter, String clusterId, Boolean deleteData) throws Exception ;

    public ActionReturnUtil addCluster(AddClusterDto clusterTPRDto) throws Exception ;

    public ActionReturnUtil updateCluster(Cluster cluster ,ClusterCRDDto clusterTPRDto) throws Exception;

    public ActionReturnUtil updateClusterStatus(Cluster cluster, boolean status, String type) throws Exception;

}

