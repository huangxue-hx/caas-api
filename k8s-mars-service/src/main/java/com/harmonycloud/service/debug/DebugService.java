package com.harmonycloud.service.debug;


import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.debug.bean.DebugState;

import java.io.File;
import java.util.List;

/**
 * Created by fengjinliu on 2019/5/5.
 */

public interface DebugService {
    boolean start(String namespace, String username, String service,String port) throws Exception;

    ActionReturnUtil getCommands(String namespace, String username, String service)throws Exception;

    Boolean checkLink(String namespace,String username,String service)throws Exception ;

    DebugState checkUser(String username) throws Exception;

    boolean end(String namespace, String username, String service,String port) throws Exception;
}
