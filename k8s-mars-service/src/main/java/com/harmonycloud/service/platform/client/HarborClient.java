package com.harmonycloud.service.platform.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Created by zsl on 2017/1/18.
 */
@Component
public class HarborClient {

    private static String protocol;

    private static String host;

    private static String domain;
    
    private static String port;


    public static String getPrefix() {
        return protocol + "://" + host+":"+port;
    }

    public String getProtocol() {
        return protocol;
    }

    @Value("#{propertiesReader['image.protocol']}")
    public void setProtocol(String protocol) {
        HarborClient.protocol = protocol;
    }

    public String getHost() {
        return host;
    }

    @Value("#{propertiesReader['image.host']}")
    public void setHost(String host) {
        HarborClient.host = host;
    }

    public String getDomain() {
        return domain;
    }

    @Value("#{propertiesReader['image.domain']}")
    public void setDomain(String domain) {
        HarborClient.domain = domain;
    }

	public String getPort() {
		return port;
	}

	@Value("#{propertiesReader['image.port']}")
	public void setPort(String port) {
		HarborClient.port = port;
	}
	
	public static String getProvider() {
		return host+":"+port;
	}
}
