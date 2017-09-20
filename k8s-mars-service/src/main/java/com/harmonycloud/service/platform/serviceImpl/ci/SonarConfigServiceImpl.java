package com.harmonycloud.service.platform.serviceImpl.ci;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.ci.SonarConfigMapper;
import com.harmonycloud.dao.ci.bean.SonarConfig;
import com.harmonycloud.service.platform.service.ci.SonarConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by riven on 17-9-20.
 */
@Service
public class SonarConfigServiceImpl implements SonarConfigService {

    @Autowired
    private SonarConfigMapper sonarConfigMapper;

    @Override
    public ActionReturnUtil getSonarConfig() {
        List<SonarConfig> configs = sonarConfigMapper.queryByAll();
        if(configs!=null && configs.size()>0){
            return ActionReturnUtil.returnSuccessWithData(configs.get(0));
        }else {
            return ActionReturnUtil.returnSuccess();
        }
    }
}
