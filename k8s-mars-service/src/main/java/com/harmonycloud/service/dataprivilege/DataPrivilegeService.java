package com.harmonycloud.service.dataprivilege;

import com.harmonycloud.common.enumm.DataResourceTypeEnum;

/**
 * Created by anson on 18/6/21.
 */
public interface DataPrivilegeService {

    /**
     * 增加资源数据
     * @param t
     * @param <T>
     * @throws Exception
     */
    public <T> void addResource(T t, String parentData, DataResourceTypeEnum type) throws Exception;

    /**
     * 删除资源数据
     * @param t
     * @param <T>
     * @throws Exception
     */
    public <T> void deleteResource(T t) throws Exception;
}
