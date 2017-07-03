package com.harmonycloud.service.application;

import com.harmonycloud.common.util.ActionReturnUtil;

/**
 * Created by czm on 2017/2/14.
 */
public interface TerminalService {
    public ActionReturnUtil getTerminal(String pod, String container, String namespace) throws Exception;

    public ActionReturnUtil getTerminalMassage(String sn) throws Exception;

}
