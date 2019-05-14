package com.harmonycloud.dao.debug;

import com.harmonycloud.dao.debug.bean.DebugState;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * Created by fengjinliu on 2019/5/5.
 */
@Repository
public interface DebugMapper {

    void insert(DebugState debugState);

    DebugState getStateByUsername(@Param("username")String username);

    DebugState getStateByService(@Param("namespace")String namespace,@Param("service")String service);

    void update(DebugState debugState);
}
