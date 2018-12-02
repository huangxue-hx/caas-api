package com.harmonycloud.service.cluster;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.k8s.bean.cluster.Cluster;

public interface RBACService {
    /**
     *
     * @param namespace
     * @param name
     * @param cluster
     * @return
     */
    public ActionReturnUtil getServiceAccount(String namespace, String name, Cluster cluster);

    /**
     *
     * @param namespace
     * @param name
     * @param cluster
     * @return
     */
    public ActionReturnUtil createServiceAccount(String namespace, String name, Cluster cluster) throws Exception;


    /**
     *
     * @param namespace
     * @param clusterRoleBindingName
     * @param clusterRoleName
     * @param serviceAccountName
     * @return
     */
    public ActionReturnUtil bindServiceWithClusterRole(String namespace, String clusterRoleBindingName, String clusterRoleName, String serviceAccountName, Cluster cluster) throws Exception;

    /**
     *
     * @param name
     * @param cluster
     * @return
     */
    public ActionReturnUtil getClusterRole(String name, Cluster cluster);

    /**
     *
     * @param name
     * @param cluster
     * @return
     */
    public ActionReturnUtil createCluserRole(String name, Cluster cluster) throws Exception;
}