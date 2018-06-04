package com.harmonycloud.api.application;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.service.application.PersistentVolumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


@Controller
public class VolumeProviderController {

    @Autowired
    PersistentVolumeService persistentVolumeService;

    /**
     * 获取某个集群下存储类型及服务提供地址
     *
     * @param
     * @return
     */
    @RequestMapping(value = "/clusters/{clusterId}/volumeprovider", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil listProvider(@PathVariable("clusterId") String clusterId) throws Exception {

        return ActionReturnUtil.returnSuccessWithData(persistentVolumeService.listProvider(clusterId));
    }

}
