package com.harmonycloud.dto.cluster;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author youpeiyuan
 *
 */
public class ClusterTransferDetailDto implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private String tenantId;
    @NotNull(message = "ClusterId can not be null.")
    private String ClusterId;
    public String getTenantId() {
        return tenantId;
    }
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
    public String getClusterId() {
        return ClusterId;
    }
    public void setClusterId(String clusterId) {
        ClusterId = clusterId;
    }
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ClusterTransferDetailDto [tenantId=");
        builder.append(tenantId);
        builder.append(", ClusterId=");
        builder.append(ClusterId);
        builder.append("]");
        return builder.toString();
    }


}
