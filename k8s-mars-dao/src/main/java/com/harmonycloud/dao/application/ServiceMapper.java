package com.harmonycloud.dao.application;

import com.harmonycloud.dao.application.bean.Service;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * Created by root on 4/12/17.
 */
public interface ServiceMapper {

    Service selectByBusinessIdAndName(Integer id,String name);

    List<Service> selectByBusinessId(@Param("id")Integer id);

    void insertService(Service service);

    void deleteSerivceByID(@Param("ids") List<Integer> ids);

    List<Service> selectServiceByIdList(@Param("idList") List<Integer> idList);

    List<Service> selectServiceByNames(@Param("names") List<String> names);
    
    void deleteSerivceByNamespace(@Param("namespace") String namespace);
    
    Service selectServiceByName(@Param("name") String name, @Param("namespace") String namespace);
    
    void updateServicePVC(@Param("name") String name, @Param("pvc") String pvc, @Param("namespace") String namespace);
}
