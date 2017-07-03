package com.harmonycloud.service.application.impl;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.service.application.TerminalService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by czm on 2017/2/14.
 */
@Service
public class TerminalServiceImpl implements TerminalService {

    public static Map<String,Map<String,String>> POD_DATA_LIST = new HashMap<String, Map<String, String>>();
    @Override
    public ActionReturnUtil getTerminal(String pod, String container, String namespace) throws Exception{

        Random random = new Random();

        String name = "T" + random.nextInt(100000000);

        Map<String, String> param = new HashMap<String, String>();
        param.put("pod",pod);
        param.put("container",container);
        param.put("namespace",namespace);

        K8SClient k8SClient = new K8SClient();
        param.put("token",k8SClient.getK8sToken());

        POD_DATA_LIST.put(name,param);


        return ActionReturnUtil.returnSuccessWithData(name);

    }

    @Override
    public ActionReturnUtil getTerminalMassage(String sn) throws Exception {
        Map<String, String> podData = TerminalServiceImpl.POD_DATA_LIST.get(sn);

        return ActionReturnUtil.returnSuccessWithData(podData);
    }

}
