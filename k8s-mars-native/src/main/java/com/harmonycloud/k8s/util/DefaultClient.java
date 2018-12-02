package com.harmonycloud.k8s.util;

import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.constant.Constant;
import org.apache.commons.lang3.StringUtils;
import com.harmonycloud.common.enumm.ClusterLevelEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Properties;


public class DefaultClient {

    private static final String TOKEN_FILE = "/var/run/secrets/kubernetes.io/serviceaccount/token";
    private static final String ENV_K8S_HOST = "KUBERNETES_SERVICE_HOST";
    private static final String ENV_K8S_PORT = "KUBERNETES_SERVICE_PORT";
    private static final String KUBE_API_SERVER_PROTOCOL = "https";
    private static final Integer KUBE_API_SERVER_PORT = 6443;
    private static final String CLUSTER_ID = "0";
    private static final Integer ES_PORT = 30093;
    private static Cluster  topCluster = new Cluster();
    private static final String PROPERTIES_RESOURCE = "constant.properties";
    private static Properties properties;
    private static boolean isInCluster = false;



    private static Logger logger = LoggerFactory.getLogger(DefaultClient.class);


    public static boolean getIsInCluster() {
        return isInCluster;
    }

    public static void setIsInCluster(boolean isInCluster) {
        DefaultClient.isInCluster = isInCluster;
    }

    public static Cluster getDefaultCluster() throws Exception{
        if (null == topCluster.getHost() ) {
            setDefaultClient();
        }
        return topCluster;
    }

    /**
     * 获取默认上层集群数据
     * @return
     * @throws Exception
     */
    public static  void setDefaultClient() throws Exception{
        /**
         * 由于windows 环境下，不能获得默认上层集群数据，故现在写死client
         */
        try{
            if(!initCluster()){
                if (null == properties){
                    properties = PropertiesLoaderUtils.loadAllProperties(PROPERTIES_RESOURCE);
                }
                initClusterLocal();

            } else {
                isInCluster = true;
            }
        }catch(Exception e){
            logger.error("Init top cluseter exception:", e);
            if (null == properties){
                properties = PropertiesLoaderUtils.loadAllProperties(PROPERTIES_RESOURCE);
            }
            initClusterLocal();

        }
    }

    private static boolean initCluster() throws Exception {
        String host = System.getenv(ENV_K8S_HOST);
        String port = System.getenv(ENV_K8S_PORT);
        if (StringUtils.isBlank(host) || StringUtils.isBlank(port) ){
            return false;
        }
        StringBuilder token = new StringBuilder();
        try{
            BufferedReader br = new BufferedReader(new FileReader(TOKEN_FILE));
            String s = null ;
            while((s = br.readLine()) != null) {
                token.append(s);
            }
        } catch (Exception e){
            throw new Exception("get token error");
        }

        topCluster.setHost(host);
        topCluster.setPort(Integer.valueOf(port));
        topCluster.setProtocol(KUBE_API_SERVER_PROTOCOL);
        topCluster.setMachineToken(token.toString());
        topCluster.setEsHost(host);
        topCluster.setLevel(ClusterLevelEnum.PLATFORM.getLevel());
        topCluster.setEsClusterName(Constant.ES_CLUSTER_NAME);
        topCluster.setEsPort(ES_PORT);
        return true;
    }

    private static void  initClusterLocal() throws Exception {
        topCluster.setId(CLUSTER_ID);
        topCluster.setHost(properties.getProperty("kube.host"));
        topCluster.setPort(KUBE_API_SERVER_PORT);
        topCluster.setProtocol(KUBE_API_SERVER_PROTOCOL);
        topCluster.setMachineToken(properties.getProperty("kube.token"));
        topCluster.setEsHost(properties.getProperty("kube.host"));
        topCluster.setEsClusterName(Constant.ES_CLUSTER_NAME);
        topCluster.setEsPort(ES_PORT);
    }

}
