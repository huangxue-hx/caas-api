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

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.config.ConnectionConfig;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 * 对集群的httpClient的请求封装<br>
 * 〈功能详细描述〉
 *
 * @author jmi
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public class HttpSslClientUtil {
    
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
        return HttpClients.custom().setDefaultConnectionConfig(connectionConfig).setSSLSocketFactory(sslsf).build();
    }

    
}
