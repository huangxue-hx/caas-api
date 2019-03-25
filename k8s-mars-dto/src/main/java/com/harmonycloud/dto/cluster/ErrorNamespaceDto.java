package com.harmonycloud.dto.cluster;

/**
 * 迁移失败的namespace
 * @author youpeiyuan
 *
 */
public class ErrorNamespaceDto {
    /**
     * namespace名称
     */
    private String namespace;
    /**
     * 失败原因
     */
    private String errMsg;
    public String getNamespace() {
        return namespace;
    }
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
    public String getErrMsg() {
        return errMsg;
    }
    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ErrorNamespaceDto [namespace=");
        builder.append(namespace);
        builder.append(", errMsg=");
        builder.append(errMsg);
        builder.append("]");
        return builder.toString();
    }


}
