package com.harmonycloud.service.dataprivilege;

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
    public <T> void addResource(T t) throws Exception;

    public <T> void deleteResource(T t) throws Exception;
}
