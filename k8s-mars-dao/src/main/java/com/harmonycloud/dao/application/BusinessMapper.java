package com.harmonycloud.dao.application;

import com.harmonycloud.dao.application.bean.Business;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface BusinessMapper {

    /**
     * search Business .
     * 
     * @author gurongyun
     * 
     * @param tenant
     *            tenant name
     * @param namespace
     *            namespace
     * @param name
     *            business name
     * @description search Business
     */
    List<Business> search(@Param("tenant") String [] tenant, @Param("namespace") String [] namespace, @Param("name") String name);

    /**
     * search a Business by PrimaryKey.
     * 
     * @author yanli
     * 
     * @param id
     *            business id PrimaryKey
     * @description search a Business by PrimaryKey
     */
    Business selectByPrimaryKey(Integer id);

    /**
     * insert Business
     * 
     * @author yanli
     * 
     * @param business
     *            BusinessBean
     * @description insert Business
     */
    void insert(Business business);

    /**
     * delete Business
     * 
     * @author yanli
     * 
     * @param id
     *            business id PrimaryKey
     * @description delete Business
     */
    void deleteBusinessById(Integer id);
    
    /**
     * delete Business
     * 
     * @author yanli
     * 
     * @param namspace
     *            business id PrimaryKey
     * @description delete Business
     */
    void deleteBusinessByNamespace(String namespace);
    

}
