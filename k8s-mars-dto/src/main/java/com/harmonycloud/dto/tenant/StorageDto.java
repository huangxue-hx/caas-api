package com.harmonycloud.dto.tenant;

/**
 * @author xc
 * @date 2018/6/30 21:35
 */
public class StorageDto {

    private String name;

    private String totalStorage = "0";

    private String  usedStorage = "0";

    private String unUsedStorage = "0";

    private String storageQuota = "0";

    private String storageType = "GB";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTotalStorage() {
        return totalStorage;
    }

    public void setTotalStorage(String totalStorage) {
        this.totalStorage = totalStorage;
    }

    public String getUsedStorage() {
        return usedStorage;
    }

    public void setUsedStorage(String usedStorage) {
        this.usedStorage = usedStorage;
    }

    public String getUnUsedStorage() {
        return unUsedStorage;
    }

    public void setUnUsedStorage(String unUsedStorage) {
        this.unUsedStorage = unUsedStorage;
    }

    public String getStorageQuota() {
        return storageQuota;
    }

    public void setStorageQuota(String storageQuota) {
        this.storageQuota = storageQuota;
    }

    public String getStorageType() {
        return storageType;
    }

    public void setStorageType(String storageType) {
        this.storageType = storageType;
    }
}
