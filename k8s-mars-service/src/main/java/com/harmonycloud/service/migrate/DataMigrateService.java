package com.harmonycloud.service.migrate;

import java.util.List;


/**
 * 版本升级数据迁移
 */
public interface DataMigrateService {

    /**
     * 版本升级数据迁移
     * @param version
     * @return
     */
    List<String> migrateData(String version, boolean execute) throws Exception;

    List<String> stopService(boolean execute) throws Exception;

    List<String> startService(boolean execute) throws Exception;
}
