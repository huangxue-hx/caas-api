package com.harmonycloud.dto.tenant;

/**
 * @author xc
 * @date 2018/6/30 21:35
 */
public class NamespaceStorageDto {

    private String namespace;

    private String clusterId;

    private String storageClass;

    /**
     * 某个存储分配给该分区的存储配额
     */
    private int hard;

    /**
     * 某个存储在该分区已经使用的存储量（分区创建pvc的存储总量）
     */
    private int used;


    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getStorageClass() {
        return storageClass;
    }

    public void setStorageClass(String storageClass) {
        this.storageClass = storageClass;
    }

    public int getHard() {
        return hard;
    }

    public void setHard(int hard) {
        this.hard = hard;
    }

    public int getUsed() {
        return used;
    }

    public void setUsed(int used) {
        this.used = used;
    }

}
