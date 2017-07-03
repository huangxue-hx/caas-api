package com.harmonycloud.dto.external;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by ly on 17/3/30.
 * 外部服务bean
 */

public class ExternalServiceBean  implements Serializable{

    /**
         * 
         */
        private static final long serialVersionUID = 1L;

        private String name;

        private String ip;
        
    	private String namespace ;
    	
    	private  Map<String,Object> labels;
    	
    	private String createTime;
    	
    	private String targetPort;
    	
    	private String protocol;
    	
    	private String port = "8080";
    	
    	private String tenantid;

    	
        public String getTenantid() {
            return tenantid;
        }

        public void setTenantid(String tenantid) {
            this.tenantid = tenantid;
        }

        public String getPort() {
			return port;
		}

		public void setPort(String port) {
			this.port = port;
		}

		public String getIp() {
                return ip;
        }

        public void setIp(String ip) {
                this.ip = ip;
        }

        public String getName() {
                return name;
        }

        public void setName(String name) {
                this.name = name;
        }

        public String getNamespace() {
                return namespace;
        }
        public void setNamespace(String namespace) {
            this.namespace = namespace;
    }

    public Map<String,Object> getLabels() {
            return labels;
    }

    public void setLabels(Map<String,Object> labels) {
            this.labels = labels;
    }

    public String getCreateTime() {
            return createTime;
    }

    public void setCreateTime(String createTime) {
            this.createTime = createTime;
    }

    public String getTargetPort() {
            return targetPort;
    }

    public void setTargetPort(String targetPort) {
            this.targetPort = targetPort;
    }

    public String getProtocol() {
            return protocol;
    }

    public void setProtocol(String protocol) {
            this.protocol = protocol;
    }
	
}


