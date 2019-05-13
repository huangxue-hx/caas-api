package com.harmonycloud.dao.debug;

import com.harmonycloud.dao.debug.bean.DebugState;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * Created by fengjinliu on 2019/5/5.
 */
@Repository
public interface DebugMapper {

    void insert(@Param("username")String username, @Param("state") String state,@Param("podname")String podname,@Param("namespace")String namespace,@Param("service")String service,@Param("port")String port);

    DebugState getState(String username);

    void update(@Param("username")String username, @Param("state") String state,@Param("podname")String podname,@Param("namespace")String namespace,@Param("service")String service,@Param("port")String port);
}
