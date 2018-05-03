package com.harmonycloud.common.util;

/**
 * http跨过证书ssl<br> 
 * 
 *
 * @author jmi
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */

import java.security.cert.X509Certificate;
import java.util.Objects;
import java.util.Properties;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Service;

/**
 * 对集群的httpClient的请求封装<br>
 * 〈功能详细描述〉
 *
 * @author jmi
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
@Service
public class HttpSslClientUtil {
    private static final String PROPERTIES_RESOURCE = "constant.properties";
    private static Properties properties;
    private static String max_connection_num;
    private static String max_per_route;

    private static int MAX_CONNECTION_NUM;
    private static int MAX_PER_ROUTE;

    private static int INIT_MAX_CONNECTION_NUM = 200;
    private static int INIT_MAX_PER_ROUTE = 200;

    /**
     * 
     * 功能描述: <br>
     * httpsClient
     *
     * @return
     * @throws Exception
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    public static CloseableHttpClient createHttpsClient() throws Exception {
        if (MAX_CONNECTION_NUM == 0){
            if (null == properties){
                properties = PropertiesLoaderUtils.loadAllProperties(PROPERTIES_RESOURCE);
            }
            max_connection_num = properties.getProperty("http.max_connection_num");
            if (Objects.isNull(max_connection_num)){
                MAX_CONNECTION_NUM = INIT_MAX_CONNECTION_NUM;
            }else {
                MAX_CONNECTION_NUM = Integer.valueOf(max_connection_num);
            }
        }
        if (MAX_PER_ROUTE == 0){
            if (null == properties){
                properties = PropertiesLoaderUtils.loadAllProperties(PROPERTIES_RESOURCE);
            }
            max_per_route = properties.getProperty("http.max_per_route");
            if (Objects.isNull(max_per_route)){
                MAX_PER_ROUTE = INIT_MAX_PER_ROUTE;
            }else {
                MAX_PER_ROUTE = Integer.valueOf(max_per_route);
            }
        }

        ConnectionConfig connectionConfig = ConnectionConfig.custom().setBufferSize(1024*1024).build();
        X509TrustManager x509mgr = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] xcs, String string) {
            }
            @Override
            public void checkServerTrusted(X509Certificate[] xcs, String string) {
            }
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[] { x509mgr }, null);
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        return HttpClients.custom().setDefaultConnectionConfig(connectionConfig)
                .setSSLSocketFactory(sslsf)
                .setMaxConnTotal(MAX_CONNECTION_NUM)
                .setMaxConnPerRoute(MAX_PER_ROUTE).build();
    }

}
